package com.example.avito_testing_2026_autum.settings.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.avito_testing_2026_autum.R
import com.example.avito_testing_2026_autum.settings.domain.model.AccentColor
import com.example.avito_testing_2026_autum.ui.theme.AccentBlue
import com.example.avito_testing_2026_autum.ui.theme.AccentGreen
import com.example.avito_testing_2026_autum.ui.theme.AccentOrange
import com.example.avito_testing_2026_autum.ui.theme.AccentPurple
import com.example.avito_testing_2026_autum.ui.theme.Purple40

@Composable
fun AccentColorSelectionSection(
    selectedColor: AccentColor,
    onColorSelected: (AccentColor) -> Unit,
    modifier: Modifier = Modifier
) {
    val colorOptions = remember {
        listOf(
            AccentColor.DEFAULT to Purple40,
            AccentColor.BLUE to AccentBlue,
            AccentColor.GREEN to AccentGreen,
            AccentColor.ORANGE to AccentOrange,
            AccentColor.PURPLE to AccentPurple
        )
    }

    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.color_scheme_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            colorOptions.forEach { (accentColor, colorValue) ->
                val isSelected = selectedColor == accentColor
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(colorValue)
                        .border(
                            width = if (isSelected) 3.dp else 0.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable { onColorSelected(accentColor) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = stringResource(R.string.selected),
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}