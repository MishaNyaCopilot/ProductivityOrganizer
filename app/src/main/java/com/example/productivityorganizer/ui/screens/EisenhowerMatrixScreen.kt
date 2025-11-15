// app/src/main/java/com/example/productivityorganizer/ui/screens/EisenhowerMatrixScreen.kt
package com.example.productivityorganizer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.productivityorganizer.data.model.Task
import com.example.productivityorganizer.ui.navigation.AppDestinations
import com.example.productivityorganizer.ui.viewmodels.TaskViewModel

// --- Data class for quadrant configuration ---
data class QuadrantInfo(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val containerColor: Color, // Background color of the quadrant
    val onContainerColor: Color, // Content color (text, icons) on the container
    val borderColor: Color, // Subtle border color for the quadrant
    val iconTint: Color, // Specific tint for the quadrant's main icon
    val elevation: Dp // Tonal elevation for the Surface (subtle shadow)
)

// --- Quadrant Design Data (tuned to match the provided image) ---
object QuadrantDesign {
    val importantUrgent = QuadrantInfo(
        title = "Делай", // Do
        description = "Важно и Срочно",
        icon = Icons.Default.CheckCircle,
        containerColor = Color(0xFFFDE7E7), // Light Red/Pink from image
        onContainerColor = Color(0xFF5C1C1C), // Darker Red for main text/icons
        borderColor = Color(0xFFF3DADA), // Slightly darker border
        iconTint = Color(0xFFE53935), // Brighter red for the icon
        elevation = 4.dp
    )
    val importantNotUrgent = QuadrantInfo(
        title = "Планируй", // Schedule
        description = "Важно, но не Срочно",
        icon = Icons.AutoMirrored.Filled.EventNote,
        containerColor = Color(0xFFE7FDE7), // Light Green from image
        onContainerColor = Color(0xFF1B5E20), // Darker Green for main text/icons
        borderColor = Color(0xFFDAF3DA), // Slightly darker border
        iconTint = Color(0xFF4CAF50), // Brighter green for the icon
        elevation = 2.dp
    )
    val notImportantUrgent = QuadrantInfo(
        title = "Делегируй", // Delegate
        description = "Неважно, но Срочно",
        icon = Icons.Default.Archive,
        containerColor = Color(0xFFFFFDE7), // Light Yellow/Orange from image
        onContainerColor = Color(0xFFE65100), // Darker Orange for main text/icons
        borderColor = Color(0xFFF3F3DA), // Slightly darker border
        iconTint = Color(0xFFFF9800), // Brighter orange for the icon
        elevation = 2.dp
    )
    val notImportantNotUrgent = QuadrantInfo(
        title = "Удаляй", // Eliminate
        description = "Неважно и Несрочно",
        icon = Icons.Default.Delete,
        containerColor = Color(0xFFEDEDED), // Very Light Grey from image
        onContainerColor = Color(0xFF424242), // Darker Grey for main text/icons
        borderColor = Color(0xFFDCDCDC), // Slightly darker border
        iconTint = Color(0xFF9E9E9E), // Grey for the icon
        elevation = 1.dp
    )
}

@Composable
fun EisenhowerMatrixScreen(
    viewModel: TaskViewModel = hiltViewModel(),
    navController: NavController,
    paddingValues: PaddingValues = PaddingValues(0.dp)
) {
    val importantUrgentTasks by viewModel.importantUrgentTasks.collectAsState()
    val importantNotUrgentTasks by viewModel.importantNotUrgentTasks.collectAsState()
    val notImportantUrgentTasks by viewModel.notImportantUrgentTasks.collectAsState()
    val notImportantNotUrgentTasks by viewModel.notImportantNotUrgentTasks.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Row(modifier = Modifier.weight(1f)) {
            QuadrantView(
                quadrantInfo = QuadrantDesign.importantUrgent,
                tasks = importantUrgentTasks, // Highest priority tasks
                navController = navController,
                modifier = Modifier.weight(1f)
            )
            QuadrantView(
                quadrantInfo = QuadrantDesign.importantNotUrgent,
                tasks = importantNotUrgentTasks, // High priority tasks
                navController = navController,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.weight(1f)) {
            QuadrantView(
                quadrantInfo = QuadrantDesign.notImportantUrgent,
                tasks = notImportantUrgentTasks, // Medium priority tasks
                navController = navController,
                modifier = Modifier.weight(1f)
            )
            QuadrantView(
                quadrantInfo = QuadrantDesign.notImportantNotUrgent,
                tasks = notImportantNotUrgentTasks, // Low priority tasks
                navController = navController,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun QuadrantView(
    quadrantInfo: QuadrantInfo,
    tasks: List<Task>,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val cornerShape = RoundedCornerShape(12.dp) // Более заметные скругленные углы

    Surface(
        modifier = modifier
            .fillMaxHeight()
            .padding(4.dp) // Отступ между квадрантами
            .clip(cornerShape) // Применяем скругленные углы
            .border(1.dp, quadrantInfo.borderColor, cornerShape), // Тонкая граница
        tonalElevation = quadrantInfo.elevation, // Тональная тень для глубины
        color = quadrantInfo.containerColor // Фоновый цвет квадранта
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp), // Внутренний отступ для содержимого квадранта
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Заголовок квадранта с иконкой
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = quadrantInfo.icon,
                    contentDescription = null,
                    tint = quadrantInfo.iconTint, // Используем specific iconTint
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = quadrantInfo.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                    color = quadrantInfo.onContainerColor
                )
            }
            // Описание квадранта
            Text(
                text = quadrantInfo.description,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = quadrantInfo.onContainerColor.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp) // Отступ после описания
            )

            // Содержимое списка или сообщение о пустом состоянии
            if (tasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Stars, // Иконка для пустого состояния
                            contentDescription = null,
                            tint = quadrantInfo.onContainerColor.copy(alpha = 0.4f),
                            modifier = Modifier.size(56.dp) // Большая иконка
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Пока нет заданий в квадранте '${quadrantInfo.title}'.",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            color = quadrantInfo.onContainerColor.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp) // Отступы между элементами списка
                ) {
                    items(tasks, key = { task -> task.id }) { task ->
                        TaskItem(task, navController, quadrantInfo.iconTint) // Используем iconTint для точки
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskItem(task: Task, navController: NavController, dotColor: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                navController.navigate("${AppDestinations.EDIT_TASK_SCREEN_ROUTE}/${task.id}")
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp), // Небольшая тень для карточки
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface) // Более нейтральный цвет для карточки
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Маленький цветной кружок/точка, как на изображении
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(4.dp)) // Скругленные углы для точки
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface, // Цвет текста на нейтральном фоне
                modifier = Modifier.weight(1f)
            )
        }
    }
}