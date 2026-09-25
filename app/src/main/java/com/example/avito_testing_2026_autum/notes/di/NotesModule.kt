package com.example.avito_testing_2026_autum.notes.di

import com.example.avito_testing_2026_autum.notes.data.repository.NoteRepositoryImpl
import com.example.avito_testing_2026_autum.notes.domain.repository.NoteRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val notesModule = module {

    singleOf(::NoteRepositoryImpl).bind<NoteRepository>()

}