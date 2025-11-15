// app/src/main/java/com/example/productivityorganizer/ui/navigation/AppNavigation.kt
package com.example.productivityorganizer.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Timer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.productivityorganizer.ui.screens.AddTaskScreen
import com.example.productivityorganizer.ui.screens.AnalyticsScreen
import com.example.productivityorganizer.ui.screens.AuthScreen
import com.example.productivityorganizer.ui.screens.EditTaskScreen
import com.example.productivityorganizer.ui.screens.EisenhowerMatrixScreen
import com.example.productivityorganizer.ui.screens.HomeScreen
import com.example.productivityorganizer.ui.screens.SplashScreen
import com.example.productivityorganizer.ui.screens.habits.AddEditHabitScreen
import com.example.productivityorganizer.ui.screens.habits.HabitCalendarScreen
import com.example.productivityorganizer.ui.screens.habits.HabitListScreen
import com.example.productivityorganizer.ui.screens.profile.ProfileScreen
import com.example.productivityorganizer.ui.timer.PomodoroScreen

// Define a sealed class for screen routes and properties that will be in bottom nav
sealed class Screen(val route: String, val icon: ImageVector, val title: String) {
    data object Tasks : Screen(AppDestinations.HOME_SCREEN_ROUTE, Icons.Filled.CheckCircle, "Задачи")
    data object Pomodoro : Screen(AppDestinations.POMODORO_SCREEN_ROUTE, Icons.Filled.Timer, "Помодоро")
    data object Habits : Screen(AppDestinations.HABIT_LIST_SCREEN_ROUTE, Icons.AutoMirrored.Filled.ListAlt, "Привычки")
    data object EisenhowerMatrix : Screen(AppDestinations.EISENHOWER_MATRIX_SCREEN_ROUTE, Icons.Filled.Dashboard, "Матрица")
    data object Profile : Screen(AppDestinations.PROFILE_SCREEN_ROUTE, Icons.Filled.Person, "Профиль") // Changed from Achievements
}

// List of items to appear in the BottomNavigationBar
val bottomNavItems = listOf(
    Screen.Tasks,
    Screen.Pomodoro,
    Screen.Habits,
    Screen.EisenhowerMatrix,
    Screen.Profile // Changed from Achievements
)

object AppDestinations {
    const val SPLASH_SCREEN_ROUTE = "splash_screen"
    const val AUTH_SCREEN_ROUTE = "auth_screen"
    const val HOME_SCREEN_ROUTE = "home_screen"
    const val ADD_TASK_SCREEN_ROUTE = "add_task_screen"
    const val EDIT_TASK_SCREEN_ROUTE = "edit_task"
    const val EDIT_TASK_WITH_ARG_ROUTE = "edit_task/{taskId}"
    const val POMODORO_SCREEN_ROUTE = "pomodoro_screen"
    const val ANALYTICS_SCREEN_ROUTE = "analytics_screen"
    const val EISENHOWER_MATRIX_SCREEN_ROUTE = "eisenhower_matrix_screen"

    // Habit Routes
    const val HABIT_LIST_SCREEN_ROUTE = "habit_list_screen"
    const val ADD_EDIT_HABIT_SCREEN_ROUTE = "add_edit_habit_screen"
    const val ADD_HABIT_ROUTE = "add_edit_habit_screen"
    const val EDIT_HABIT_ROUTE = "add_edit_habit_screen/{habitId}"
    const val HABIT_CALENDAR_SCREEN_ROUTE = "habit_calendar_screen"
    const val PROFILE_SCREEN_ROUTE = "profile_screen" // Changed from ACHIEVEMENTS_SCREEN_ROUTE
}

@Composable
fun MainAppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(navController = navController, startDestination = AppDestinations.SPLASH_SCREEN_ROUTE, modifier = modifier) {
        composable(AppDestinations.SPLASH_SCREEN_ROUTE) {
            SplashScreen(navController = navController)
        }
        composable(AppDestinations.AUTH_SCREEN_ROUTE) {
            AuthScreen(
                onSignInSuccess = {
                    navController.navigate(AppDestinations.HOME_SCREEN_ROUTE) {
                        popUpTo(AppDestinations.AUTH_SCREEN_ROUTE) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(AppDestinations.HOME_SCREEN_ROUTE) {
            HomeScreen(navController = navController) // Передаем главный NavController
        }
        composable(AppDestinations.ADD_TASK_SCREEN_ROUTE) {
            AddTaskScreen(navController = navController)
        }
        composable(
            route = AppDestinations.EDIT_TASK_WITH_ARG_ROUTE,
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) { navBackStackEntry ->
            val taskId = navBackStackEntry.arguments?.getString("taskId")
            EditTaskScreen(
                navController = navController,
                taskId = taskId
            )
        }
        composable(AppDestinations.POMODORO_SCREEN_ROUTE) {
            PomodoroScreen()
        }
        composable(AppDestinations.ANALYTICS_SCREEN_ROUTE) {
            AnalyticsScreen(navController = navController)
        }
        // Habit Screen Routes
        composable(AppDestinations.HABIT_LIST_SCREEN_ROUTE) {
            HabitListScreen(
                onNavigateToAddHabit = { navController.navigate(AppDestinations.ADD_HABIT_ROUTE) },
                onNavigateToEditHabit = { habitId ->
                    navController.navigate("${AppDestinations.ADD_EDIT_HABIT_SCREEN_ROUTE}/$habitId")
                },
                onNavigateToCalendar = { navController.navigate(AppDestinations.HABIT_CALENDAR_SCREEN_ROUTE) }
            )
        }
        composable(AppDestinations.ADD_HABIT_ROUTE) {
            AddEditHabitScreen(
                habitId = null,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = AppDestinations.EDIT_HABIT_ROUTE,
            arguments = listOf(navArgument("habitId") { type = NavType.IntType })
        ) { navBackStackEntry ->
            val habitId = navBackStackEntry.arguments?.getInt("habitId")
            AddEditHabitScreen(
                habitId = habitId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(AppDestinations.HABIT_CALENDAR_SCREEN_ROUTE) {
            HabitCalendarScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(AppDestinations.EISENHOWER_MATRIX_SCREEN_ROUTE) {
            EisenhowerMatrixScreen(navController = navController)
        }
        composable(AppDestinations.PROFILE_SCREEN_ROUTE) { // Changed route
            ProfileScreen(navController = navController) // Pass navController to ProfileScreen
        }
    }
}