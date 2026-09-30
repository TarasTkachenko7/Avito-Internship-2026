package com.example.avito_testing_2026_autum.tasks.di

import com.example.avito_testing_2026_autum.tasks.data.repository.TaskRepositoryImpl
import com.example.avito_testing_2026_autum.tasks.domain.repository.TaskRepository
import com.example.avito_testing_2026_autum.tasks.domain.usecases.AddInlineTaskUseCase
import com.example.avito_testing_2026_autum.tasks.domain.usecases.DeleteTaskUseCase
import com.example.avito_testing_2026_autum.tasks.domain.usecases.GetTasksUseCase
import com.example.avito_testing_2026_autum.tasks.domain.usecases.ToggleTaskStatusUseCase
import com.example.avito_testing_2026_autum.tasks.presentation.TasksViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val tasksModule = module {

    singleOf(::TaskRepositoryImpl).bind<TaskRepository>()

    factoryOf(::GetTasksUseCase)
    factoryOf(::DeleteTaskUseCase)
    factoryOf(::AddInlineTaskUseCase)
    factoryOf(::ToggleTaskStatusUseCase)

    viewModelOf(::TasksViewModel)
}