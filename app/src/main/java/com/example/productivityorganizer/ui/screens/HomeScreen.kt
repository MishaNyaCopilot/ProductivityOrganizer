// app/src/main/java/com/example/productivityorganizer/ui/screens/HomeScreen.kt
package com.example.productivityorganizer.ui.screens

// Удалены ненужные импорты Icons.AutoMirrored.Filled.ExitToApp и Icons.Filled.Analytics,
// так как они теперь используются только в ProfileScreen.
// import androidx.compose.material.icons.automirrored.filled.ExitToApp
// import androidx.compose.material.icons.filled.Analytics
// Удален LocalDensity, так как не используется
// import androidx.compose.ui.platform.LocalDensity
import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.productivityorganizer.data.model.Task
import com.example.productivityorganizer.ui.navigation.AppDestinations
import com.example.productivityorganizer.ui.navigation.bottomNavItems
import com.example.productivityorganizer.ui.screens.habits.HabitListScreen
import com.example.productivityorganizer.ui.screens.profile.ProfileScreen
import com.example.productivityorganizer.ui.state.AuthenticationState
import com.example.productivityorganizer.ui.timer.PomodoroScreen
import com.example.productivityorganizer.ui.viewmodels.CategoryUIState
import com.example.productivityorganizer.ui.viewmodels.GoogleSignInViewModel
import com.example.productivityorganizer.ui.viewmodels.SortByOption
import com.example.productivityorganizer.ui.viewmodels.SortOrderOption
import com.example.productivityorganizer.ui.viewmodels.StatusFilterOption
import com.example.productivityorganizer.ui.viewmodels.TaskUIState
import com.example.productivityorganizer.ui.viewmodels.TaskViewModel
import com.example.productivityorganizer.ui.viewmodels.TimeFilterOption
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private const val TAG = "HomeScreen"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    navController: NavController, // Главный NavController, который управляет всей навигацией
    authViewModel: GoogleSignInViewModel = hiltViewModel(),
    taskViewModel: TaskViewModel = hiltViewModel()
) {
    val authState by authViewModel.authenticationState.collectAsStateWithLifecycle()
    val taskState by taskViewModel.taskUIState.collectAsStateWithLifecycle()
    val selectedCategoryId by taskViewModel.selectedCategoryId.collectAsStateWithLifecycle()
    val selectedStatusFilter by taskViewModel.selectedStatusFilter.collectAsStateWithLifecycle()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var taskToDelete by remember { mutableStateOf<Task?>(null) }

    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // homeNavController управляет навигацией внутри NavHost на HomeScreen.
    // НЕ путайте его с главным navController, который управляет переключением
    // между HomeScreen, AuthScreen и т.д.
    val homeNavController = rememberNavController()
    val currentHomeBackStackEntry by homeNavController.currentBackStackEntryAsState()
    val currentHomeDestination = currentHomeBackStackEntry?.destination

    // !!! ВАЖНО: ИСПРАВЛЕННЫЙ LaunchedEffect для обработки состояния аутентификации !!!
    // Этот LaunchedEffect будет следить за authState и при необходимости
    // перенаправлять на экран аутентификации.
    LaunchedEffect(authState) {
        Log.d(TAG, "LaunchedEffect(authState) triggered. Current authState: $authState")
        when (authState) {
            is AuthenticationState.Idle, is AuthenticationState.Error -> {
                // Пользователь не аутентифицирован или произошла ошибка сессии.
                // Перенаправляем на экран аутентификации, если мы уже не на нем.
                // Проверяем currentDestination главного navController.
                if (navController.currentDestination?.route != AppDestinations.AUTH_SCREEN_ROUTE) {
                    Log.d(TAG, "Attempting to navigate to AuthScreen. Current route: ${navController.currentDestination?.route}")
                    // Даем немного времени для завершения UI-операций, если есть,
                    // это может помочь в редких случаях с гонками состояний.
                    delay(100)
                    navController.navigate(AppDestinations.AUTH_SCREEN_ROUTE) {
                        // Очищаем весь бэкстек до корневого графа (самое начало MainAppNavigation)
                        popUpTo(navController.graph.id) {
                            inclusive = true // Включаем стартовый экран графа для удаления
                        }
                        launchSingleTop = true // Убеждаемся, что запускаем только одну копию AuthScreen
                    }
                    Log.d(TAG, "Navigation to AuthScreen initiated.")
                } else {
                    Log.d(TAG, "Already on AuthScreen, not navigating again.")
                }
            }
            is AuthenticationState.Success -> {
                // Пользователь аутентифицирован.
                // Загружаем задачи, если они еще не загружены или произошла ошибка.
                // Это условие `taskState is TaskUIState.Idle || taskState is TaskUIState.Error`
                // предотвращает повторную загрузку при каждом изменении authState.
                Log.d(TAG, "User authenticated. Fetching tasks if needed. Current taskState: $taskState")
                if (taskState is TaskUIState.Idle || taskState is TaskUIState.Error) {
                    taskViewModel.getTasks()
                }
            }
            AuthenticationState.Loading -> {
                // Состояние загрузки, ничего не делаем.
                Log.d(TAG, "AuthenticationState is Loading.")
            }
        }
    }

    // Получаем текущий BackStackEntry для ГЛАВНОГО navController (не homeNavController)
    val mainNavBackStackEntry by navController.currentBackStackEntryAsState()

    // !!! НОВОЕ: LaunchedEffect для обновления задач при возвращении на HomeScreen !!!
    LaunchedEffect(mainNavBackStackEntry) {
        // Проверяем, что мы на HomeScreen (route = AppDestinations.HOME_SCREEN_ROUTE)
        // и что экран является текущим в стеке навигации
        if (mainNavBackStackEntry?.destination?.route == AppDestinations.HOME_SCREEN_ROUTE) {
            Log.d(TAG, "HomeScreen is now active or re-activated. Fetching tasks.")
            taskViewModel.getTasks() // Вызываем обновление задач
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    when (currentHomeDestination?.route) {
                        AppDestinations.HOME_SCREEN_ROUTE -> Text("Мои Задачи")
                        AppDestinations.POMODORO_SCREEN_ROUTE -> Text("Помодоро Таймер")
                        AppDestinations.HABIT_LIST_SCREEN_ROUTE -> Text("Мои Привычки")
                        AppDestinations.EISENHOWER_MATRIX_SCREEN_ROUTE -> Text("Матрица Эйзенхауэра")
                        AppDestinations.PROFILE_SCREEN_ROUTE -> Text("Профиль")
                        else -> Text("ProdOrganizer")
                    }
                },
                actions = {
                    // Кнопки "Аналитика" и "Выйти" теперь только на экране профиля
                }
            )
        },
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentHomeDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            homeNavController.navigate(screen.route) {
                                // popUpTo here manages the internal backstack of homeNavController
                                popUpTo(homeNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            val currentRoute = currentHomeDestination?.route
            val showFabs = currentRoute == AppDestinations.HOME_SCREEN_ROUTE || currentRoute == AppDestinations.HABIT_LIST_SCREEN_ROUTE

            if (showFabs) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .zIndex(1f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Левый FAB (Календарь привычек)
                        if (currentRoute == AppDestinations.HABIT_LIST_SCREEN_ROUTE) {
                            FloatingActionButton(
                                onClick = { navController.navigate(AppDestinations.HABIT_CALENDAR_SCREEN_ROUTE) },
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ) {
                                Icon(Icons.Filled.Event, contentDescription = "Календарь привычек")
                            }
                        } else {
                            // Если левый FAB не нужен, вставляем Spacer,
                            // чтобы правый FAB оставался справа при SpaceBetween.
                            Spacer(modifier = Modifier.size(56.dp))
                        }

                        // Правый FAB (Добавить)
                        FloatingActionButton(
                            onClick = {
                                when (currentRoute) {
                                    AppDestinations.HOME_SCREEN_ROUTE -> navController.navigate(AppDestinations.ADD_TASK_SCREEN_ROUTE)
                                    AppDestinations.HABIT_LIST_SCREEN_ROUTE -> navController.navigate(AppDestinations.ADD_HABIT_ROUTE)
                                }
                            }
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Добавить")
                        }
                    }
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { paddingValues ->
        // Этот NavHost отвечает за переключение между внутренними экранами (Задачи, Помодоро и т.д.)
        NavHost(
            navController = homeNavController,
            startDestination = AppDestinations.HOME_SCREEN_ROUTE, // Стартовый экран внутри HomeScreen
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(AppDestinations.HOME_SCREEN_ROUTE) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { showBottomSheet = true },
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        tonalElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Фильтры и Сортировка", style = MaterialTheme.typography.titleSmall)
                            Icon(Icons.Default.FilterList, contentDescription = "Открыть фильтры")
                        }
                    }

                    val categoriesState by taskViewModel.categoryUIState.collectAsStateWithLifecycle()
                    val categoryName = (categoriesState as? CategoryUIState.Success)?.categories?.find { it.id == selectedCategoryId }?.name

                    FlowRow(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (selectedCategoryId != null && categoryName != null) {
                            FilterChip(
                                selected = true,
                                onClick = { taskViewModel.updateSelectedCategory(null) },
                                label = { Text("Категория: $categoryName") },
                                trailingIcon = { Icon(Icons.Default.Close, contentDescription = "Снять фильтр категории", modifier = Modifier.size(16.dp)) }
                            )
                        }
                        if (selectedStatusFilter != StatusFilterOption.ALL) {
                            FilterChip(
                                selected = true,
                                onClick = { taskViewModel.updateStatusFilter(StatusFilterOption.ALL) },
                                label = { Text("Статус: ${selectedStatusFilter.toString().lowercase(Locale.getDefault()).replaceFirstChar { it.uppercaseChar() }}") },
                                trailingIcon = { Icon(Icons.Default.Close, contentDescription = "Снять фильтр статуса", modifier = Modifier.size(16.dp)) }
                            )
                        }
                    }

                    if (showDeleteDialog) {
                        DeleteConfirmationDialog(
                            taskName = taskToDelete?.title ?: "задачу",
                            onConfirmDelete = {
                                taskToDelete?.let {
                                    taskViewModel.deleteTask(it.id)
                                }
                                showDeleteDialog = false
                                taskToDelete = null
                            },
                            onDismiss = {
                                showDeleteDialog = false
                                taskToDelete = null
                            }
                        )
                    }

                    when (val state = taskState) {
                        is TaskUIState.Loading -> {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                        }
                        is TaskUIState.Success -> {
                            if (state.tasks.isEmpty()) {
                                Text(
                                    "Пока нет задач. Нажмите '+' чтобы добавить!",
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(top = 16.dp)
                                )
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    contentPadding = PaddingValues(
                                        top = 16.dp,
                                        bottom = 16.dp
                                    )
                                ) {
                                    items(state.tasks, key = { task -> task.id }) { task ->
                                        TaskItem(
                                            task = task,
                                            viewModel = taskViewModel,
                                            navController = navController, // Передаем главный navController
                                            onConfirmDelete = { selectedTask ->
                                                taskToDelete = selectedTask
                                                showDeleteDialog = true
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        is TaskUIState.Error -> {
                            LaunchedEffect(state.message) {
                                snackbarHostState.showSnackbar("Ошибка: ${state.message}")
                            }
                            Text(
                                "Не удалось загрузить задачи. Попробуйте снова.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 16.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { taskViewModel.getTasks() }) {
                                Text("Повторить")
                            }
                        }
                        is TaskUIState.Idle -> {
                            Text("Добро пожаловать! Проверяем задачи...", style = MaterialTheme.typography.bodyLarge)
                        }
                        is TaskUIState.SuccessSingleTask -> {
                            Text("Отображается одна задача, для списка обновите.", style = MaterialTheme.typography.bodyLarge)
                            LaunchedEffect(Unit) {
                                taskViewModel.getTasks()
                            }
                        }
                    }
                }

                if (showBottomSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showBottomSheet = false },
                        sheetState = sheetState
                    ) {
                        FilterSortBottomSheet(taskViewModel = taskViewModel) {
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                if (!sheetState.isVisible) {
                                    showBottomSheet = false
                                }
                            }
                        }
                    }
                }
            }

            composable(AppDestinations.POMODORO_SCREEN_ROUTE) {
                PomodoroScreen()
            }

            composable(AppDestinations.HABIT_LIST_SCREEN_ROUTE) {
                HabitListScreen(
                    onNavigateToAddHabit = { navController.navigate(AppDestinations.ADD_HABIT_ROUTE) },
                    onNavigateToEditHabit = { habitId ->
                        navController.navigate("${AppDestinations.ADD_EDIT_HABIT_SCREEN_ROUTE}/$habitId")
                    },
                    onNavigateToCalendar = { navController.navigate(AppDestinations.HABIT_CALENDAR_SCREEN_ROUTE) }
                )
            }
            composable(AppDestinations.EISENHOWER_MATRIX_SCREEN_ROUTE) {
                EisenhowerMatrixScreen(navController = navController, viewModel = taskViewModel)
            }
            composable(AppDestinations.PROFILE_SCREEN_ROUTE) {
                ProfileScreen(navController = navController)
            }
        }
    }
}

// ... Остальные @Composable функции (FilterSortBottomSheet, TaskItem, DeleteConfirmationDialog)
// Они не изменялись и должны быть скопированы как есть из вашего HomeScreen.kt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterSortBottomSheet(taskViewModel: TaskViewModel, onDismiss: () -> Unit) {
    val categoriesState by taskViewModel.categoryUIState.collectAsStateWithLifecycle()
    val selectedCategoryId by taskViewModel.selectedCategoryId.collectAsStateWithLifecycle()
    val selectedStatusFilter by taskViewModel.selectedStatusFilter.collectAsStateWithLifecycle()
    val selectedSortBy by taskViewModel.selectedSortBy.collectAsStateWithLifecycle()
    val selectedSortOrder by taskViewModel.selectedSortOrder.collectAsStateWithLifecycle()

    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var statusDropdownExpanded by remember { mutableStateOf(false) }
    var sortByDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        taskViewModel.fetchCategories()
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Настройки Фильтров и Сортировки",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Фильтры",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.Start)
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(modifier = Modifier.weight(1f).defaultMinSize(minWidth = 150.dp)) {
                ExposedDropdownMenuBox(
                    expanded = categoryDropdownExpanded,
                    onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
                        value = when (val state = categoriesState) {
                            is CategoryUIState.Success -> state.categories.find { it.id == selectedCategoryId }?.name ?: "Все Категории"
                            is CategoryUIState.Loading -> "Загрузка..."
                            is CategoryUIState.Error -> "Ошибка"
                            else -> "Выбрать Категорию"
                        },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Категория") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Все Категории") },
                            onClick = {
                                taskViewModel.updateSelectedCategory(null)
                                categoryDropdownExpanded = false
                            }
                        )
                        if (categoriesState is CategoryUIState.Success) {
                            (categoriesState as CategoryUIState.Success).categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category.name) },
                                    onClick = {
                                        taskViewModel.updateSelectedCategory(category.id)
                                        categoryDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Box(modifier = Modifier.weight(1f).defaultMinSize(minWidth = 150.dp)) {
                ExposedDropdownMenuBox(
                    expanded = statusDropdownExpanded,
                    onExpandedChange = { statusDropdownExpanded = !statusDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
                        value = selectedStatusFilter.toString().lowercase(Locale.getDefault()).replaceFirstChar { it.uppercaseChar() },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Статус") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusDropdownExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = statusDropdownExpanded,
                        onDismissRequest = { statusDropdownExpanded = false }
                    ) {
                        StatusFilterOption.entries.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.toString().lowercase(Locale.getDefault()).replaceFirstChar { it.uppercaseChar() }) },
                                onClick = {
                                    taskViewModel.updateStatusFilter(option)
                                    statusDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        val selectedTimeFilter by taskViewModel.selectedTimeFilter.collectAsStateWithLifecycle()
        var timeFilterDropdownExpanded by remember { mutableStateOf(false) }

        Box(modifier = Modifier.fillMaxWidth().defaultMinSize(minWidth = 150.dp)) { // Changed to fillMaxWidth for consistency
            ExposedDropdownMenuBox(
                expanded = timeFilterDropdownExpanded,
                onExpandedChange = { timeFilterDropdownExpanded = !timeFilterDropdownExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                TextField(
                    value = selectedTimeFilter.toString().replace("_", " ").lowercase(Locale.getDefault()).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Фильтр по времени") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = timeFilterDropdownExpanded) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    )
                )
                ExposedDropdownMenu(
                    expanded = timeFilterDropdownExpanded,
                    onDismissRequest = { timeFilterDropdownExpanded = false }
                ) {
                    TimeFilterOption.entries.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.toString().replace("_", " ").lowercase(Locale.getDefault()).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }) },
                            onClick = {
                                taskViewModel.updateTimeFilter(option)
                                timeFilterDropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Сортировка",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.Start)
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.weight(1f).defaultMinSize(minWidth = 150.dp)) {
                ExposedDropdownMenuBox(
                    expanded = sortByDropdownExpanded,
                    onExpandedChange = { sortByDropdownExpanded = !sortByDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
                        value = selectedSortBy.toString().lowercase(Locale.getDefault()).replaceFirstChar { it.uppercaseChar() },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Сортировать по") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sortByDropdownExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = sortByDropdownExpanded,
                        onDismissRequest = { sortByDropdownExpanded = false }
                    ) {
                        SortByOption.entries.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.toString().lowercase(Locale.getDefault()).replaceFirstChar { it.uppercaseChar() }) },
                                onClick = {
                                    taskViewModel.updateSortBy(option)
                                    sortByDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            FilledIconButton(
                onClick = {
                    val newOrder = if (selectedSortOrder == SortOrderOption.ASCENDING) SortOrderOption.DESCENDING else SortOrderOption.ASCENDING
                    taskViewModel.updateSortOrder(newOrder)
                },
                enabled = selectedSortBy != SortByOption.NONE,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .size(56.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = if (selectedSortOrder == SortOrderOption.ASCENDING) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                    contentDescription = if (selectedSortOrder == SortOrderOption.ASCENDING) "По возрастанию" else "По убыванию"
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onDismiss,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
        ) {
            Text("Готово", style = MaterialTheme.typography.titleMedium)
        }
    }
}


@Composable
fun TaskItem(
    task: Task,
    viewModel: TaskViewModel,
    navController: NavController,
    onConfirmDelete: (Task) -> Unit
) {
    val isoDateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val displayDateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val displayDateTimeFormatter = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
    val displayTimeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault()) // Not used, but kept for context

    val isOverdue = remember(task.dueDate, task.endTime, task.isCompleted) {
        if (task.isCompleted || task.dueDate == null) {
            false
        } else {
            try {
                val dueCalendar = Calendar.getInstance().apply {
                    time = isoDateFormatter.parse(task.dueDate!!) ?: return@remember false
                    task.endTime?.let {
                        timeInMillis = it
                    } ?: run {
                        set(Calendar.HOUR_OF_DAY, 23)
                        set(Calendar.MINUTE, 59)
                        set(Calendar.SECOND, 59)
                        set(Calendar.MILLISECOND, 999)
                    }
                }
                val now = Calendar.getInstance()
                now.after(dueCalendar)
            } catch (e: ParseException) {
                Log.e("TaskItem", "Error parsing dueDate/endTime for overdue check: ${task.dueDate}, ${task.endTime}", e)
                false
            }
        }
    }

    val textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
    val contentAlpha = if (task.isCompleted) 0.6f else 1.0f
    // <--- ИЗМЕНЕНИЕ ЦВЕТА ФОНА КАРТОЧКИ ---
    // Используем surfaceContainerHigh или другой подходящий токен для чуть более светлого фона

    val cardContainerColor = if (task.isCompleted) {
        MaterialTheme.colorScheme.surfaceVariant // Для выполненных можно оставить как есть или сделать еще светлее
    } else {
        // Попробуем MaterialTheme.colorScheme.surfaceContainerHigh (если доступно в вашей версии Material 3)
        // Или MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp)
        // Или просто Color(0xFF282828) для DarkTheme если current Material 3 theme is too dark for surface
        MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp) // <--- ЭТОТ СПОСОБ ДОБАВИТ ГЛУБИНУ
    }

    // Determine color for due date text, making it red if overdue and not completed
    val dueDateColor = if (isOverdue && !task.isCompleted) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha)
    }

    val priorityColor = when (task.priorityLevel.lowercase(Locale.getDefault())) {
        "highest" -> MaterialTheme.colorScheme.error
        "high" -> MaterialTheme.colorScheme.tertiary
        "medium" -> MaterialTheme.colorScheme.secondary
        "low" -> MaterialTheme.colorScheme.outline
        else -> MaterialTheme.colorScheme.outline
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { navController.navigate("edit_task/${task.id}") },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = cardContainerColor)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween // Для равномерного распределения Checkbox/Кнопок и Column
        ) {
            // Кружок приоритета
            Surface(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .then(
                        if (!task.isCompleted) {
                            Modifier.border(1.dp, priorityColor, CircleShape)
                        } else {
                            Modifier
                        }
                    ),
                color = if (!task.isCompleted) priorityColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            ) {}
            Spacer(modifier = Modifier.width(12.dp))

            // Основной текстовый контент задачи (Title, Description, Due Date, Priority)
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(
                    task.title,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = textDecoration,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha),
                    maxLines = 1, // Ограничиваем название одной строкой
                    overflow = TextOverflow.Ellipsis // Если не умещается, то "..."
                )
                Spacer(modifier = Modifier.height(4.dp))
                task.description?.let {
                    if (it.isNotBlank()) {
                        Text(
                            it,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2, // Ограничиваем описание двумя строками
                            textDecoration = textDecoration,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha),
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
                // --- Срок выполнения и Просрочено ---
                if (task.dueDate != null) {
                    val dueTextBuilder = StringBuilder("Срок: ")
                    if (task.endTime != null) {
                        val cal = Calendar.getInstance().apply { timeInMillis = task.endTime }
                        dueTextBuilder.append(displayDateTimeFormatter.format(cal.time))
                    } else {
                        val cal = Calendar.getInstance().apply { time = isoDateFormatter.parse(task.dueDate) ?: Date() }
                        dueTextBuilder.append(displayDateFormatter.format(cal.time))
                    }
                    if (isOverdue && !task.isCompleted) {
                        dueTextBuilder.append(" (Просрочено)")
                    }
                    Text(
                        text = dueTextBuilder.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = dueDateColor,
                        textDecoration = textDecoration,
                        maxLines = 1, // Ограничиваем одной строкой, чтобы не было съезда
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Text(
                        "Нет срока",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha),
                        textDecoration = textDecoration
                    )
                }
                // --- Конец срока выполнения ---
                Spacer(modifier = Modifier.height(4.dp)) // Добавляем немного отступа
                Text(
                    "Приоритет: ${task.priorityLevel.lowercase(Locale.getDefault()).replaceFirstChar { it.uppercaseChar() }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = priorityColor.copy(alpha = contentAlpha),
                    textDecoration = textDecoration
                )
            }

            // Правая часть с чекбоксом и кнопками
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = { viewModel.toggleTaskCompleted(task.id, it) },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary.copy(alpha = if (task.isCompleted) 0.6f else 1.0f),
                        uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (task.isCompleted) 0.6f else 1.0f)
                    )
                )
                IconButton(onClick = { onConfirmDelete(task) }) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Удалить Задачу",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = contentAlpha)
                    )
                }
            }
        }
    }
}

@Composable
fun DeleteConfirmationDialog(
    taskName: String,
    onConfirmDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Подтвердите удаление") },
        text = { Text("Вы уверены, что хотите удалить задачу \"$taskName\"?") },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmDelete()
                }
            ) {
                Text("Удалить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}