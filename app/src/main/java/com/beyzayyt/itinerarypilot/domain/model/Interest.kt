package com.beyzayyt.itinerarypilot.domain.model

import androidx.annotation.StringRes
import com.beyzayyt.itinerarypilot.R

enum class Interest(@StringRes val labelRes: Int, val label: String) {
    FOOD(R.string.interest_food, "Food & Dining"),
    HISTORY(R.string.interest_history, "History & Culture"),
    MUSEUM(R.string.interest_museum, "Museums & Art"),
    NIGHTLIFE(R.string.interest_nightlife, "Nightlife"),
    HIDDEN_GEMS(R.string.interest_hidden_gems, "Hidden Gems")
}
