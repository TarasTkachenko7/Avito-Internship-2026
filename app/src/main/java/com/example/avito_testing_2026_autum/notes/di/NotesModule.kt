package com.example.avito_testing_2026_autum.notes.di

import com.example.avito_testing_2026_autum.notes.data.repository.NoteRepositoryImpl
import com.example.avito_testing_2026_autum.notes.domain.repository.NoteRepository
import com.example.avito_testing_2026_autum.notes.domain.usecases.DeleteNoteUseCase
import com.example.avito_testing_2026_autum.notes.domain.usecases.GetNotesUseCase
import com.example.avito_testing_2026_autum.notes.domain.usecases.editor.CreateTempImageFileUseCase
import com.example.avito_testing_2026_autum.notes.domain.usecases.editor.GetNoteByIdUseCase
import com.example.avito_testing_2026_autum.notes.domain.usecases.editor.SaveImageUseCase
import com.example.avito_testing_2026_autum.notes.domain.usecases.editor.UpsertNoteUseCase
import com.example.avito_testing_2026_autum.notes.presentation.NotesViewModel
import com.example.avito_testing_2026_autum.notes.presentation.editor.NoteEditorViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val notesModule = module {

    singleOf(::NoteRepositoryImpl).bind<NoteRepository>()

    factoryOf(::GetNotesUseCase)
    factoryOf(::DeleteNoteUseCase)
    factoryOf(::GetNoteByIdUseCase)
    factoryOf(::UpsertNoteUseCase)
    factoryOf(::SaveImageUseCase)
    factoryOf(::CreateTempImageFileUseCase)

    viewModelOf(::NotesViewModel)
    viewModelOf(::NoteEditorViewModel)

}