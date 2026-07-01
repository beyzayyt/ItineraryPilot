package com.beyzayyt.itinerarypilot.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.beyzayyt.itinerarypilot.R
import com.beyzayyt.itinerarypilot.domain.model.Interest
import com.beyzayyt.itinerarypilot.ui.theme.ChipBackground
import com.beyzayyt.itinerarypilot.ui.theme.ChipFoodBg
import com.beyzayyt.itinerarypilot.ui.theme.ChipFoodLabel
import com.beyzayyt.itinerarypilot.ui.theme.ChipHiddenGemsBg
import com.beyzayyt.itinerarypilot.ui.theme.ChipHiddenGemsLabel
import com.beyzayyt.itinerarypilot.ui.theme.ChipHistoryBg
import com.beyzayyt.itinerarypilot.ui.theme.ChipHistoryLabel
import com.beyzayyt.itinerarypilot.ui.theme.ChipMuseumBg
import com.beyzayyt.itinerarypilot.ui.theme.ChipMuseumLabel
import com.beyzayyt.itinerarypilot.ui.theme.ChipNightlifeBg
import com.beyzayyt.itinerarypilot.ui.theme.ChipNightlifeLabel
import com.beyzayyt.itinerarypilot.ui.theme.ErrorContainer
import com.beyzayyt.itinerarypilot.ui.theme.ErrorText
import com.beyzayyt.itinerarypilot.ui.theme.HeaderGradientStart
import com.beyzayyt.itinerarypilot.ui.theme.InputBackground
import com.beyzayyt.itinerarypilot.ui.theme.InputBorderUnfocused
import com.beyzayyt.itinerarypilot.ui.theme.InputPlaceholder
import com.beyzayyt.itinerarypilot.ui.theme.Navy
import com.beyzayyt.itinerarypilot.ui.theme.NavyContainer
import com.beyzayyt.itinerarypilot.ui.theme.SectionLabelColor
import com.beyzayyt.itinerarypilot.ui.theme.TextSecondary

// Pastel chip colors per interest
private val interestChipColors = mapOf(
    Interest.FOOD        to Pair(ChipFoodBg, ChipFoodLabel),
    Interest.HISTORY     to Pair(ChipHistoryBg, ChipHistoryLabel),
    Interest.MUSEUM      to Pair(ChipMuseumBg, ChipMuseumLabel),
    Interest.NIGHTLIFE   to Pair(ChipNightlifeBg, ChipNightlifeLabel),
    Interest.HIDDEN_GEMS to Pair(ChipHiddenGemsBg, ChipHiddenGemsLabel),
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    onNavigateToItinerary: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(uiState.navigateToItinerary) {
        if (uiState.navigateToItinerary) {
            viewModel.onNavigationHandled()
            onNavigateToItinerary()
        }
    }

    Scaffold(containerColor = Color.White) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Gradient Header ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(HeaderGradientStart, Color.White)
                        )
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 28.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = stringResource(R.string.home_title),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Navy
                    )
                    Text(
                        text = stringResource(R.string.home_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 8.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(28.dp)
            ) {
                // ── City ──
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionLabel(stringResource(R.string.label_where_going))
                    OutlinedTextField(
                        value = uiState.city,
                        onValueChange = viewModel::onCityChange,
                        placeholder = {
                            Text(
                                stringResource(R.string.hint_city),
                                color = InputPlaceholder,
                                fontSize = 15.sp
                            )
                        },
                        leadingIcon = { Text("🗺️", modifier = Modifier.padding(start = 4.dp)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        isError = uiState.error != null && uiState.city.isBlank(),
                        shape = RoundedCornerShape(18.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Navy,
                            unfocusedBorderColor = InputBorderUnfocused,
                            unfocusedContainerColor = InputBackground,
                            focusedContainerColor = Color.White
                        )
                    )
                }

                // ── Duration ──
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SectionLabel(stringResource(R.string.label_trip_duration))
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = HeaderGradientStart
                        ) {
                            Text(
                                text = "${uiState.days} ${stringResource(if (uiState.days == 1) R.string.label_day else R.string.label_days)}",
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = Navy
                            )
                        }
                    }
                    Slider(
                        value = uiState.days.toFloat(),
                        onValueChange = { viewModel.onDaysChange(it.toInt()) },
                        valueRange = 1f..14f,
                        steps = 12,
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = Navy,
                            activeTrackColor = Navy,
                            inactiveTrackColor = NavyContainer
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            stringResource(R.string.label_1_day),
                            style = MaterialTheme.typography.bodySmall,
                            color = InputPlaceholder
                        )
                        Text(
                            stringResource(R.string.label_14_days),
                            style = MaterialTheme.typography.bodySmall,
                            color = InputPlaceholder
                        )
                    }
                }

                // ── Interests ──
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SectionLabel(stringResource(R.string.label_your_interests))
                        Text(
                            text = stringResource(R.string.label_optional),
                            style = MaterialTheme.typography.labelSmall,
                            color = InputPlaceholder,
                            modifier = Modifier.padding(bottom = 1.dp)
                        )
                    }
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Interest.entries.forEach { interest ->
                            val selected = interest in uiState.selectedInterests
                            val (bgColor, selectedColor) = interestChipColors[interest]
                                ?: Pair(HeaderGradientStart, Navy)
                            FilterChip(
                                selected = selected,
                                onClick = { viewModel.onInterestToggle(interest) },
                                label = {
                                    Text(
                                        stringResource(interest.labelRes),
                                        fontSize = 13.sp
                                    )
                                },
                                shape = RoundedCornerShape(20.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = ChipBackground,
                                    labelColor = TextSecondary,
                                    selectedContainerColor = bgColor,
                                    selectedLabelColor = selectedColor
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = selected,
                                    borderColor = InputBorderUnfocused,
                                    selectedBorderColor = selectedColor.copy(alpha = 0.4f),
                                    borderWidth = 1.dp,
                                    selectedBorderWidth = 1.5.dp
                                )
                            )
                        }
                    }
                }

                // ── Error ──
                AnimatedVisibility(visible = uiState.error != null) {
                    uiState.error?.let { error ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = ErrorContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text("⚠️")
                                Text(
                                    text = error,
                                    color = ErrorText,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                // ── Generate Button ──
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.onGenerateItinerary()
                    },
                    enabled = !uiState.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Navy,
                        disabledContainerColor = Navy.copy(alpha = 0.5f)
                    )
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            stringResource(R.string.msg_generating_plan),
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.btn_generate_itinerary),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = SectionLabelColor,
        letterSpacing = 0.1.sp
    )
}
