package com.example.productivityorganizer.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import co.yml.charts.axis.AxisData
import co.yml.charts.common.model.PlotType
import co.yml.charts.common.model.Point
import co.yml.charts.ui.barchart.BarChart
import co.yml.charts.ui.barchart.models.BarChartData
import co.yml.charts.ui.barchart.models.BarData
import co.yml.charts.ui.barchart.models.BarStyle
import co.yml.charts.ui.piechart.charts.PieChart
import co.yml.charts.ui.piechart.models.PieChartConfig
import co.yml.charts.ui.piechart.models.PieChartData
import com.example.productivityorganizer.ui.viewmodels.AnalyticsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    navController: NavController,
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val tasksCompletedDaily by viewModel.tasksCompletedDaily.collectAsState()
    val pomodoroDistribution by viewModel.pomodoroSessionDistribution.collectAsState()
    val totalCompletedTasks by viewModel.totalCompletedTasks.collectAsState()
    val totalPomodoroFocusMinutes by viewModel.totalPomodoroFocusMinutes.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Аналитика продуктивности") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                OverallProgressCard(totalCompletedTasks, totalPomodoroFocusMinutes)
            }

            item {
                TaskCompletionChartCard(tasksCompletedDaily, totalCompletedTasks)
            }

            item {
                PomodoroDistributionChartCard(pomodoroDistribution)
            }
        }
    }
}


@Composable
fun OverallProgressCard(completedTasks: Int, focusMinutes: Long) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        // Changed containerColor to MaterialTheme.colorScheme.surface
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Выполнено заданий", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(completedTasks.toString(), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Время фокуса", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Text("${focusMinutes} min", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
fun TaskCompletionChartCard(
    tasksCompletedDaily: List<Pair<String, Int>>,
    totalCompletedTasks: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        // Changed containerColor to MaterialTheme.colorScheme.surface
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Выполнено задач ежедневно (последние 7 дней)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp).align(Alignment.Start),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (tasksCompletedDaily.isEmpty() && totalCompletedTasks == 0) {
                Text(
                    "Данных о выполнении задач пока нет. Выполните несколько задач, чтобы увидеть свой прогресс!",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 50.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else if (tasksCompletedDaily.any { it.second > 0 }) {
                TasksDailyBarChart(tasksCompletedDaily)
            } else {
                Text(
                    "На этой неделе задачи еще не выполнены. Продолжайте!",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 50.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun PomodoroDistributionChartCard(pomodoroDistribution: List<Pair<String, Float>>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        // Changed containerColor to MaterialTheme.colorScheme.surface
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Распределение времени Помодоро",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp).align(Alignment.Start),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (pomodoroDistribution.isEmpty() || pomodoroDistribution.all { it.second == 0f }) {
                Text(
                    "Данных Помодоро пока нет. Начните свою первую сессию!",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 50.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                PomodoroPieChart(pomodoroDistribution)
            }
        }
    }
}


@Composable
fun TasksDailyBarChart(data: List<Pair<String, Int>>) {
    val points = data.mapIndexed { index, pair ->
        Point(index.toFloat(), pair.second.toFloat())
    }

    if (points.isEmpty() || points.all { it.y == 0f }) {
        Box(
            modifier = Modifier.height(200.dp).fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text("Задачи на этой неделе не выполнены для отображения.", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    val maxDataValue = points.maxOfOrNull { it.y } ?: 0f
    val maxRange = (maxDataValue + 2).toInt().coerceAtLeast(5)

    val barChartData = BarChartData(
        chartData = points.map { BarData(point = it, color = MaterialTheme.colorScheme.primary, label = "") },
        xAxisData = AxisData.Builder()
            .axisStepSize(55.dp) // Увеличено для лучшей видимости меток дней
            .steps(data.size - 1)
            .bottomPadding(16.dp)
            .labelData { index -> data[index].first.take(3) }
            .axisLineColor(MaterialTheme.colorScheme.outline)
            .axisLabelColor(MaterialTheme.colorScheme.onSurfaceVariant)
            .build(),
        yAxisData = AxisData.Builder()
            .steps(maxRange.coerceAtLeast(1).coerceAtMost(5))
            .labelAndAxisLinePadding(20.dp)
            .axisOffset(0.dp)
            .labelData { value -> value.toInt().toString() }
            .axisLineColor(MaterialTheme.colorScheme.outline)
            .axisLabelColor(MaterialTheme.colorScheme.onSurfaceVariant)
            .build(),
        // Changed backgroundColor to MaterialTheme.colorScheme.surface
        backgroundColor = MaterialTheme.colorScheme.surface,
        barStyle = BarStyle(
            barWidth = 35.dp,
            selectionHighlightData = null,
            paddingBetweenBars = 8.dp,
            cornerRadius = 4.dp
        )
    )

    BarChart(
        modifier = Modifier
            .height(280.dp)
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        barChartData = barChartData
    )
}

@Composable
fun PomodoroPieChart(distribution: List<Pair<String, Float>>) {
    if (distribution.isEmpty() || distribution.all { it.second == 0f }) {
        Box(
            modifier = Modifier.height(200.dp).fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text("Сессии Помодоро для отображения отсутствуют.", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    // Определяем более яркие и мотивирующие цвета ПРЯМО ЗДЕСЬ
    val focusChartColor = Color(0xFF4CAF50) // Ярко-зеленый
    val breakChartColor = Color(0xFF2196F3) // Ярко-синий

    val pieChartConfig = PieChartConfig(
        strokeWidth = 90f,
        showSliceLabels = true,
        sliceLabelTextSize = 14.sp,
        sliceLabelTextColor = MaterialTheme.colorScheme.onSurface,
        activeSliceAlpha = .9f,
        isAnimationEnable = true,
        chartPadding = 40,
        backgroundColor = MaterialTheme.colorScheme.surface,

        labelVisible = true,
        labelType = PieChartConfig.LabelType.PERCENTAGE,
        labelFontSize = 16.sp,
        labelColor = MaterialTheme.colorScheme.onSurface, // Цвет процентов
        labelColorType = PieChartConfig.LabelColorType.SPECIFIED_COLOR,
    )

    val pieChartData = PieChartData(
        slices = distribution.map { pair ->
            val sliceColor = if (pair.first.equals("Фокус", ignoreCase = true)) focusChartColor else breakChartColor // <--- ИСПОЛЬЗУЕМ НОВЫЕ ЦВЕТА
            PieChartData.Slice(
                label = pair.first,
                value = pair.second,
                color = sliceColor // <--- Здесь цвет должен быть применен
            )
        },
        plotType = PlotType.Pie
    )

    PieChart(
        modifier = Modifier
            .height(230.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        pieChartData = pieChartData,
        pieChartConfig = pieChartConfig
    )
}