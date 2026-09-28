package com.example.avito_testing_2026_autum.notes.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.avito_testing_2026_autum.notes.presentation.contract.SortType
import com.example.avito_testing_2026_autum.R

@Composable
fun NotesTopBar(
    searchQuery: String,
    isDeleteModeActive: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onSortSelect: (SortType) -> Unit,
    onToggleDeleteMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isSortMenuExpanded by rememberSaveable { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text(stringResource(R.string.find_notes)) },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = stringResource(R.string.find_notes))
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = stringResource(R.string.clear))
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.width(4.dp))

        Box {
            IconButton(onClick = { isSortMenuExpanded = true }) {
                Icon(imageVector = Icons.Default.Sort, contentDescription = stringResource(R.string.sorting))
            }
            DropdownMenu(
                expanded = isSortMenuExpanded,
                onDismissRequest = { isSortMenuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.first_new)) },
                    onClick = {
                        onSortSelect(SortType.DATE_DESC)
                        isSortMenuExpanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.first_old)) },
                    onClick = {
                        onSortSelect(SortType.DATE_ASC)
                        isSortMenuExpanded = false
                    }
                )
            }
        }

        IconButton(onClick = onToggleDeleteMode) {
            Icon(
                imageVector = if (isDeleteModeActive) Icons.Default.Close else Icons.Default.DeleteOutline,
                contentDescription = stringResource(R.string.delete_mode),
                tint = if (isDeleteModeActive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}