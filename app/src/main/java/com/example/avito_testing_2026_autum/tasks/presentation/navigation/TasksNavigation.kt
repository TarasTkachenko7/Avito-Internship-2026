package com.example.avito_testing_2026_autum.tasks.presentation.presentation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.avito_testing_2026_autum.root.navigation.Screen
import com.example.avito_testing_2026_autum.tasks.presentation.TasksScreenRoot

fun NavGraphBuilder.tasksGraph(navController: NavController) {
    composable<Screen.Tasks> {
        TasksScreenRoot()
    }
}