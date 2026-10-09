package com.example.avito_testing_2026_autum.tasks.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.avito_testing_2026_autum.R
import com.example.avito_testing_2026_autum.tasks.domain.model.TaskFilterType
import com.example.avito_testing_2026_autum.tasks.domain.model.TaskSortOrder

@Composable
fun TaskFilterRow(
    selectedFilter: TaskFilterType,
    selectedSort: TaskSortOrder,
    onFilterSelected: (TaskFilterType) -> Unit,
    onSortSelected: (TaskSortOrder) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TaskFilterTypeChip(
            title = stringResource(R.string.all),
            isSelected = selectedFilter == TaskFilterType.ALL,
            onClick = { onFilterSelected(TaskFilterType.ALL) }
        )

        TaskFilterTypeChip(
            title = stringResource(R.string.active),
            isSelected = selectedFilter == TaskFilterType.ACTIVE,
            onClick = { onFilterSelected(TaskFilterType.ACTIVE) }
        )

        TaskFilterTypeChip(
            title = stringResource(R.string.done),
            isSelected = selectedFilter == TaskFilterType.COMPLETED,
            onClick = { onFilterSelected(TaskFilterType.COMPLETED) }
        )

        TaskSortChip(
            selectedSort = selectedSort,
            onSortSelected = onSortSelected
        )
    }
}

@Composable
private fun TaskFilterTypeChip(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = containerColor
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        )
    }
}

@Composable
private fun TaskSortChip(
    selectedSort: TaskSortOrder,
    onSortSelected: (TaskSortOrder) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    Box(modifier = modifier) {
        Surface(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .clickable { isExpanded = true },
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Sort,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = when (selectedSort) {
                        TaskSortOrder.DATE_DESC -> stringResource(R.string.first_new)
                        TaskSortOrder.DATE_ASC -> stringResource(R.string.first_old)
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.first_new)) },
                onClick = {
                    onSortSelected(TaskSortOrder.DATE_DESC)
                    isExpanded = false
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.first_old)) },
                onClick = {
                    onSortSelected(TaskSortOrder.DATE_ASC)
                    isExpanded = false
                }
            )
        }
    }
}