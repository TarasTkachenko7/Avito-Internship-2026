package com.example.avito_testing_2026_autum.tasks.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.avito_testing_2026_autum.app.navigation.Screen
import com.example.avito_testing_2026_autum.tasks.presentation.TasksScreenRoot

fun NavGraphBuilder.tasksGraph() {
    composable<Screen.Tasks> {
        TasksScreenRoot()
    }
}