package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.viewmodel.WalkEarnViewModel
import java.util.Locale

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DashboardScreen(viewModel: WalkEarnViewModel) {
    val userState by viewModel.activeUser.collectAsState()
    val antiCheatAlert by viewModel.antiCheatAlert.collectAsState()
    val isAr = viewModel.currentLanguage.collectAsState().value == "ar"

    val user = userState ?: return

    val stepGoal = 5000f
    val progress = if (user.steps < stepGoal) user.steps / stepGoal else 1.0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000),
        label = "Step progress animation"
    )

    // Simulation states
    var simStepsInput by remember { mutableStateOf("1000") }
    var simSpeedInput by remember { mutableStateOf("6.5") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Top Bar Greeting
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Profile Avatar Simulation
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "User Avatar",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MilitaryTech,
                            contentDescription = "Level Icon",
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${viewModel.getLabel("level")} ${user.level}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            // Streak Badges
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Streak",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${user.streak} ${viewModel.getLabel("days")}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Circular Step Progress
        Box(
            modifier = Modifier
                .size(240.dp)
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            // Circular progress background
            CircularProgressIndicator(
                progress = 1.0f,
                modifier = Modifier.fillMaxSize(),
                strokeWidth = 14.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            )

            // Real animated progress
            CircularProgressIndicator(
                progress = animatedProgress,
                modifier = Modifier.fillMaxSize(),
                strokeWidth = 14.dp,
                color = MaterialTheme.colorScheme.primary,
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )

            // Inner Text stats
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.DirectionsWalk,
                    contentDescription = "Walking",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = String.format(Locale.US, "%,d", user.steps),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "/ ${String.format(Locale.US, "%,d", stepGoal.toInt())} ${viewModel.getLabel("steps")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Mini Dashboard Stats Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                title = viewModel.getLabel("distance"),
                value = String.format(Locale.US, "%.2f km", user.distance),
                icon = Icons.Default.Map,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            StatCard(
                modifier = Modifier.weight(1f),
                title = viewModel.getLabel("calories"),
                value = String.format(Locale.US, "%.0f kcal", user.steps * 0.04f),
                icon = Icons.Default.Whatshot,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.width(8.dp))
            StatCard(
                modifier = Modifier.weight(1f),
                title = viewModel.getLabel("points"),
                value = "${user.points} PTS",
                icon = Icons.Default.MonetizationOn,
                color = MaterialTheme.colorScheme.tertiary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 4. Live Sensor Indicator Tag
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color.Green)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isAr) "حساسات الهاتف وجهاز التتبع نشطة بنجاح" else "Hardware step sensors connected & working",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 5. Walk Simulator Block (Treadmill Workout)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DirectionsRun,
                        contentDescription = "Treadmill",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = viewModel.getLabel("treadmill_mode"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = simStepsInput,
                        onValueChange = { simStepsInput = it },
                        label = { Text(viewModel.getLabel("steps")) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("sim_steps_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = simSpeedInput,
                        onValueChange = { simSpeedInput = it },
                        label = { Text(viewModel.getLabel("speed_kmh")) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("sim_speed_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val steps = simStepsInput.toIntOrNull() ?: 0
                        val speed = simSpeedInput.toFloatOrNull() ?: 0f
                        viewModel.simulateSteps(steps, speed)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("simulate_steps_button"),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Start Workout"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = viewModel.getLabel("simulate_walk_btn"),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // Anti-Cheat Warning Popup Dialog
    if (antiCheatAlert != null) {
        Dialog(onDismissRequest = { viewModel.dismissAntiCheatAlert() }) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Alert",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = viewModel.getLabel("anti_cheat_title"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (antiCheatAlert == "SUSPICIOUS_SPEED") {
                            viewModel.getLabel("anti_cheat_speed")
                        } else {
                            viewModel.getLabel("anti_cheat_simulator")
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { viewModel.dismissAntiCheatAlert() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text(
                            text = viewModel.getLabel("dismiss"),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
        }
    }
}
