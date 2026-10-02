package com.example.avito_testing_2026_autum.tasks.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.avito_testing_2026_autum.R
import com.example.avito_testing_2026_autum.tasks.domain.model.TaskSortOrder

@Composable
fun TasksTopBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSortSelect: (TaskSortOrder) -> Unit,
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
            placeholder = { Text(stringResource(R.string.find_tasks)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = stringResource(R.string.find_tasks)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.clear)
                        )
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

        TasksSortMenuButton(onSortSelect = onSortSelect)
    }
}

@Composable
private fun TasksSortMenuButton(
    onSortSelect: (TaskSortOrder) -> Unit,
    modifier: Modifier = Modifier
) {
    var isSortMenuExpanded by rememberSaveable { mutableStateOf(false) }

    Box(modifier = modifier) {
        IconButton(onClick = { isSortMenuExpanded = true }) {
            Icon(
                imageVector = Icons.Default.Sort,
                contentDescription = stringResource(R.string.sorting)
            )
        }
        DropdownMenu(
            expanded = isSortMenuExpanded,
            onDismissRequest = { isSortMenuExpanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.first_new)) },
                onClick = {
                    onSortSelect(TaskSortOrder.DATE_DESC)
                    isSortMenuExpanded = false
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.first_old)) },
                onClick = {
                    onSortSelect(TaskSortOrder.DATE_ASC)
                    isSortMenuExpanded = false
                }
            )
        }
    }
}