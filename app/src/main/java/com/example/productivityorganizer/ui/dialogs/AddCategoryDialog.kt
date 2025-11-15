package com.example.productivityorganizer.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.productivityorganizer.ui.viewmodels.CategoryUIState
import com.example.productivityorganizer.ui.viewmodels.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class) // Нужен для TextFieldDefaults.colors
@Composable
fun AddCategoryDialog(
    taskViewModel: TaskViewModel,
    onDismissRequest: () -> Unit,
    onCategoryAdded: (newCategoryName: String) -> Unit
) {
    var categoryName by remember { mutableStateOf("") }
    val categoryUiState by taskViewModel.categoryUIState.collectAsState()
    var submitted by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Добавить новую категорию") },
        text = {
            Column {
                TextField( // Изменяем на TextField
                    value = categoryName,
                    onValueChange = { categoryName = it },
                    label = { Text("Название категории") },
                    isError = categoryUiState is CategoryUIState.Error && submitted,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(), // Занимает всю ширину
                    shape = RoundedCornerShape(8.dp), // Закругленные углы
                    colors = TextFieldDefaults.colors( // Те же цвета, что и в других формах
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        errorIndicatorColor = MaterialTheme.colorScheme.error // Цвет индикатора ошибки
                    )
                )
                if (categoryUiState is CategoryUIState.Error && submitted) {
                    Text(
                        text = (categoryUiState as CategoryUIState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                if (categoryUiState is CategoryUIState.Loading && submitted) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (categoryName.isNotBlank()) {
                        submitted = true
                        taskViewModel.addCategory(categoryName.trim())
                    }
                },
                enabled = categoryName.isNotBlank() && !(categoryUiState is CategoryUIState.Loading && submitted)
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            Button(onClick = onDismissRequest) {
                Text("Отмена")
            }
        }
    )

    LaunchedEffect(categoryUiState) {
        if (submitted) {
            if (categoryUiState is CategoryUIState.Success) {
                onCategoryAdded(categoryName.trim())
                onDismissRequest()
            }
        }
    }
}