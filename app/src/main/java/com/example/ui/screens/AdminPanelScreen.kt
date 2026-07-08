package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.WalkEarnViewModel
import java.util.Locale

@Composable
fun AdminPanelScreen(viewModel: WalkEarnViewModel) {
    val allUsers by viewModel.allUsers.collectAsState()
    val isAr = viewModel.currentLanguage.collectAsState().value == "ar"
    val context = LocalContext.current

    // Task input states
    var taskTitleEn by remember { mutableStateOf("") }
    var taskTitleAr by remember { mutableStateOf("") }
    var taskPoints by remember { mutableStateOf("100") }
    var taskTargetSteps by remember { mutableStateOf("5000") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Back Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.navigateTo("settings") },
                modifier = Modifier.testTag("admin_back_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = if (isAr) "لوحة الإدارة والمراقبة" else "Central Admin Console",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Creator Panel: Add New Tasks
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AddTask,
                        contentDescription = "Add Task",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = viewModel.getLabel("add_task_btn"),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = taskTitleAr,
                    onValueChange = { taskTitleAr = it },
                    label = { Text("عنوان المهمة (بالعربية)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_title_ar_input"),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = taskTitleEn,
                    onValueChange = { taskTitleEn = it },
                    label = { Text("Task Title (English)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_title_en_input"),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = taskPoints,
                        onValueChange = { taskPoints = it },
                        label = { Text("النقاط / Points") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 4.dp)
                            .testTag("task_points_input"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = taskTargetSteps,
                        onValueChange = { taskTargetSteps = it },
                        label = { Text("الهدف / Steps Target") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 4.dp)
                            .testTag("task_steps_input"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (taskTitleAr.isBlank() || taskTitleEn.isBlank()) {
                            Toast.makeText(context, "الرجاء تعبئة العناوين بالكامل", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val points = taskPoints.toIntOrNull() ?: 100
                        val target = taskTargetSteps.toIntOrNull() ?: 5000
                        viewModel.adminAddNewTask(
                            titleEn = taskTitleEn,
                            titleAr = taskTitleAr,
                            points = points,
                            target = target,
                            type = "DAILY"
                        )
                        Toast.makeText(context, "تم إدراج التحدي الجديد بنجاح!", Toast.LENGTH_SHORT).show()
                        taskTitleAr = ""
                        taskTitleEn = ""
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("admin_submit_task_btn"),
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Text("إدراج التحدي في التطبيق", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3. Members Management
        Text(
            text = "${viewModel.getLabel("users_count")}: ${allUsers.size}",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            textAlign = TextAlign.Start
        )

        allUsers.forEach { member ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = member.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = member.email,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }

                        // Delete Member
                        IconButton(
                            onClick = {
                                viewModel.adminDeleteUser(member.id)
                                Toast.makeText(context, "تم مسح العضو من النظام", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.testTag("delete_member_${member.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Steps: ${String.format(Locale.US, "%,d", member.steps)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Points: ${member.points} PTS",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Modifier Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.adminUpdateUserSteps(member.id, member.steps + 2000)
                                Toast.makeText(context, "+2000 steps added!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.height(32.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("+2K Steps", style = MaterialTheme.typography.labelSmall)
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.adminAddPoints(member.id, 500)
                                Toast.makeText(context, "+500 PTS added!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.height(32.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("+500 PTS", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}
