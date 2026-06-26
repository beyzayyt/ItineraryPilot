const { onRequest } = require("firebase-functions/v2/https");
const { defineSecret } = require("firebase-functions/params");

const geminiApiKey = defineSecret("GEMINI_API_KEY");

const GEMINI_URL =
  "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

exports.generateItinerary = onRequest(
  { secrets: ["GEMINI_API_KEY"], maxInstances: 10, cors: false, invoker: "public", timeoutSeconds: 120 },
  async (req, res) => {
    if (req.method !== "POST") {
      res.status(405).json({ error: "Method not allowed" });
      return;
    }

    const appCheckToken = req.header("X-Firebase-AppCheck");
    const isDebugBuild = req.header("X-Debug-Build") === "true";

    if (!isDebugBuild && !appCheckToken) {
      res.status(401).json({ error: "Unauthorized" });
      return;
    }

    const { city, days, interests } = req.body;
    if (!city || !days) {
      res.status(400).json({ error: "Missing required fields: city, days" });
      return;
    }

    const interestText =
      !interests || interests.length === 0
        ? "general sightseeing"
        : interests.join(", ");

    const prompt = buildPrompt(city, days, interestText);

    const geminiResponse = await fetch(`${GEMINI_URL}?key=${geminiApiKey.value()}`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        generationConfig: {
          responseMimeType: "application/json",
          temperature: 0.7,
        },
        contents: [{ parts: [{ text: prompt }] }],
      }),
    });

    if (!geminiResponse.ok) {
      const err = await geminiResponse.text();
      res.status(geminiResponse.status).json({ error: err });
      return;
    }

    const data = await geminiResponse.json();
    const text = data.candidates[0].content.parts[0].text;
    res.status(200).json(JSON.parse(text));
  }
);

function buildPrompt(city, days, interests) {
  return `Create a ${days}-day travel itinerary for ${city} focusing on: ${interests}.

Return ONLY valid JSON matching this exact structure (no markdown, no extra text):
{
  "city": "${city}",
  "totalDays": ${days},
  "days": [
    {
      "dayNumber": 1,
      "theme": "Descriptive day theme",
      "places": [
        {
          "name": "Place Name",
          "description": "1-2 sentence description",
          "latitude": 41.9028,
          "longitude": 12.4964,
          "category": "landmark",
          "estimatedDurationMinutes": 90,
          "bestTimeToVisit": "Morning"
        }
      ]
    }
  ],
  "tips": ["Practical tip 1", "Practical tip 2", "Practical tip 3"]
}

Rules:
- Include 4-5 places per day
- Use real GPS coordinates for every place
- category must be one of: landmark, museum, restaurant, park, nightlife, hidden_gem, shopping
- Order places logically to minimize travel time`;
}
