package com.example.noteshiftapp.navigation

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.noteshiftapp.ui.alarms.AlarmsScreen
import com.example.noteshiftapp.ui.calendar.CalendarScreen
import com.example.noteshiftapp.ui.note_details.NoteDetailsScreen
import com.example.noteshiftapp.ui.notes.NotesScreen
import com.example.noteshiftapp.ui.tables.TablesScreen
import com.example.noteshiftapp.viewmodel.AlarmViewModel
import com.example.noteshiftapp.viewmodel.CalendarViewModel
import com.example.noteshiftapp.viewmodel.NoteDetailsViewModel
import com.example.noteshiftapp.viewmodel.NotesViewModel
import com.example.noteshiftapp.viewmodel.SheetsViewModel

@Composable
fun AppNavHost(
    openNoteId: Long? = null,
    onOpenNoteConsumed: () -> Unit = {},
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    LaunchedEffect(openNoteId) {
        val noteId = openNoteId ?: return@LaunchedEffect
        if (noteId > 0L) {
            navController.navigate(AppDestination.NoteDetails.createRoute(noteId)) {
                launchSingleTop = true
            }
        }
        onOpenNoteConsumed()
    }

    val navigateToDestination: (AppDestination) -> Unit = { destination ->
        navController.navigate(destination.route) {
            popUpTo(navController.graph.startDestinationId) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = AppDestination.Notes.route,
        modifier = Modifier,
    ) {
        composable(AppDestination.Notes.route) {
            val vm: NotesViewModel = viewModel(factory = NotesViewModel.factory(application))
            NotesScreen(
                viewModel = vm,
                currentRoute = currentRoute,
                onDestinationClick = navigateToDestination,
            ) { noteId ->
                navController.navigate(AppDestination.NoteDetails.createRoute(noteId))
            }
        }
        composable(AppDestination.Calendar.route) {
            val vm: CalendarViewModel = viewModel(factory = CalendarViewModel.factory(application))
            CalendarScreen(
                viewModel = vm,
                currentRoute = currentRoute,
                onDestinationClick = navigateToDestination,
            )
        }
        composable(AppDestination.Tables.route) {
            val vm: SheetsViewModel = viewModel(factory = SheetsViewModel.factory(application))
            TablesScreen(
                viewModel = vm,
                currentRoute = currentRoute,
                onDestinationClick = navigateToDestination,
            )
        }
        composable(AppDestination.Alarms.route) {
            val vm: AlarmViewModel = viewModel(factory = AlarmViewModel.factory(application))
            AlarmsScreen(
                viewModel = vm,
                currentRoute = currentRoute,
                onDestinationClick = navigateToDestination,
            )
        }
        composable(
            route = AppDestination.NoteDetails.route,
            arguments = listOf(navArgument(AppDestination.NoteDetails.NOTE_ID_ARG) { type = NavType.LongType }),
        ) { entry ->
            val noteId = entry.arguments?.getLong(AppDestination.NoteDetails.NOTE_ID_ARG) ?: 0L
            val vm: NoteDetailsViewModel = viewModel(
                key = "note_$noteId",
                factory = NoteDetailsViewModel.factory(application, noteId),
            )
            NoteDetailsScreen(
                viewModel = vm,
                onBackClick = { navController.popBackStack() },
                onNoteDeleted = { navController.popBackStack() },
                onOpenNote = { childNoteId ->
                    navController.navigate(AppDestination.NoteDetails.createRoute(childNoteId))
                },
            )
        }
    }
}
