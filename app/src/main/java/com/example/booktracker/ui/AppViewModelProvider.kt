package com.example.booktracker.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.booktracker.BookTrackerApplication
import com.example.booktracker.ui.addbook.AddBookViewModel
import com.example.booktracker.ui.dashboard.DashboardViewModel
import com.example.booktracker.ui.pending.PendingViewModel
import com.example.booktracker.ui.search.SearchViewModel
import com.example.booktracker.ui.shelf.ShelfViewModel

/** Wires every ViewModel to the [BookTrackerApplication] container. */
object AppViewModelProvider {

    val Factory = viewModelFactory {

        initializer {
            DashboardViewModel(app().container.bookRepository)
        }

        initializer {
            PendingViewModel(app().container.bookRepository)
        }

        initializer {
            ShelfViewModel(
                savedStateHandle = createSavedStateHandle(),
                repository = app().container.bookRepository
            )
        }

        initializer {
            AddBookViewModel(
                bookRepository = app().container.bookRepository,
                openLibraryRepository = app().container.openLibraryRepository
            )
        }

        initializer {
            SearchViewModel(
                bookRepository = app().container.bookRepository,
                openLibraryRepository = app().container.openLibraryRepository
            )
        }
    }
}

private fun CreationExtras.app(): BookTrackerApplication =
    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as BookTrackerApplication
