package com.example.avito_testing_2026_autum.app.navigation

import kotlinx.serialization.Serializable

sealed interface Screen {

    @Serializable
    data object Notes : Screen

    @Serializable
    data object Tasks : Screen

    @Serializable
    data object Settings : Screen

    @Serializable
    data class NoteEditor (val noteId: Long? = null) : Screen

}