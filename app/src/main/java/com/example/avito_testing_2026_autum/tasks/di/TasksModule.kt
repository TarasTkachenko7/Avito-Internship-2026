package com.example.avito_testing_2026_autum.tasks.di

import com.example.avito_testing_2026_autum.tasks.data.repository.TaskRepositoryImpl
import com.example.avito_testing_2026_autum.tasks.domain.repository.TaskRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val tasksModule = module {

    singleOf(::TaskRepositoryImpl).bind<TaskRepository>()

}