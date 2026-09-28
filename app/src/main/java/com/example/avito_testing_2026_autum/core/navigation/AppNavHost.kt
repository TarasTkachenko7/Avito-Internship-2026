package com.example.avito_testing_2026_autum.core.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Notes,
        modifier = modifier
    ) {
        composable<Screen.Notes> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Здесь будет список Заметок")
            }
        }

        composable<Screen.Tasks> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Здесь будет список Задач")
            }
        }

        composable<Screen.Settings> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Здесь будут Настройки")
            }
        }

        composable<Screen.NoteEditor> { backStackEntry ->
            val args = backStackEntry.toRoute<Screen.NoteEditor>()

            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    if (args.noteId == -1L) "Создание новой заметки"
                    else "Редактирование заметки с ID: ${args.noteId}"
                )
            }
        }
    }
}