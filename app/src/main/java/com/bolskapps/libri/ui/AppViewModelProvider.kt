package com.bolskapps.libri.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.bolskapps.libri.LibriApplication
import com.bolskapps.libri.ui.addbook.AddBookViewModel
import com.bolskapps.libri.ui.dashboard.DashboardViewModel
import com.bolskapps.libri.ui.pending.PendingViewModel
import com.bolskapps.libri.ui.search.SearchViewModel
import com.bolskapps.libri.ui.shelf.ShelfViewModel

/** Wires every ViewModel to the [LibriApplication] container. */
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
                savedStateHandle = createSavedStateHandle(),
                bookRepository = app().container.bookRepository,
                openLibraryRepository = app().container.openLibraryRepository
            )
        }
    }
}

private fun CreationExtras.app(): LibriApplication =
    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as LibriApplication
