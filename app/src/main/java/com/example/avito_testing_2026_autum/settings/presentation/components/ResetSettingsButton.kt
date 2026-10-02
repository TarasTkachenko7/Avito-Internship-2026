package com.example.avito_testing_2026_autum.settings.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.avito_testing_2026_autum.R

@Composable
fun ResetSettingsButton(
    onResetClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onResetClicked,
        modifier = modifier.fillMaxWidth(),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
    ) {
        Text(stringResource(R.string.reset_settings))
    }
}