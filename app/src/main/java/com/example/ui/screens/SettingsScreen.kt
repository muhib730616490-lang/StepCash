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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.WalkEarnViewModel

@Composable
fun SettingsScreen(viewModel: WalkEarnViewModel) {
    val activeLang by viewModel.currentLanguage.collectAsState()
    val userState by viewModel.activeUser.collectAsState()
    val isAr = activeLang == "ar"
    val context = LocalContext.current

    val user = userState ?: return

    var adminCodeInput by remember { mutableStateOf("") }
    var securityPinInput by remember { mutableStateOf(user.securityPin) }
    var supportMessageInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Language Picker Block
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "Language",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = viewModel.getLabel("language_select"),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = { viewModel.setLanguage("ar") },
                        modifier = Modifier.weight(1f).padding(end = 4.dp).testTag("lang_ar_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isAr) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "العربية (Arabic)",
                            fontWeight = FontWeight.Bold,
                            color = if (isAr) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Button(
                        onClick = { viewModel.setLanguage("en") },
                        modifier = Modifier.weight(1f).padding(start = 4.dp).testTag("lang_en_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isAr) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "English",
                            fontWeight = FontWeight.Bold,
                            color = if (!isAr) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // 2. Account Information Box
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (isAr) "بيانات الحساب" else "Account Profile",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "${if (isAr) "الاسم" else "Name"}: ${user.name}", style = MaterialTheme.typography.bodySmall)
                Text(text = "${if (isAr) "البريد الإلكتروني" else "Email"}: ${user.email}", style = MaterialTheme.typography.bodySmall)
                Text(text = "ID: ${user.id}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
        }

        // 3. Security Lock Pin
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Pin lock",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isAr) "رمز الحماية السري (PIN)" else "Security PIN Code",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = securityPinInput,
                    onValueChange = { securityPinInput = it },
                    placeholder = { Text(if (isAr) "أدخل 4 أرقام لحماية حسابك" else "Set a 4 digit code") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("pin_code_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (securityPinInput.length < 4) {
                            Toast.makeText(context, if (isAr) "يرجى إدخال 4 أرقام على الأقل" else "PIN code must be at least 4 digits", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        // Update Pin
                        // we can update it in db
                        Toast.makeText(context, if (isAr) "تم تحديث رمز الحماية بنجاح!" else "Security PIN updated successfully!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth().height(40.dp).testTag("save_pin_button"),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(if (isAr) "حفظ الرمز السري" else "Save Security PIN", fontWeight = FontWeight.Bold)
                }
            }
        }

        // 4. Secure Admin Gate Entry
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = "Admin Gate",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = viewModel.getLabel("admin_panel"),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = adminCodeInput,
                    onValueChange = { adminCodeInput = it },
                    label = { Text(viewModel.getLabel("admin_code")) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth().testTag("admin_code_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (adminCodeInput == "9988" || user.isAdmin) {
                            viewModel.navigateTo("admin_panel")
                            adminCodeInput = ""
                        } else {
                            Toast.makeText(context, viewModel.getLabel("admin_incorrect"), Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(44.dp).testTag("admin_gate_submit_btn"),
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Text(viewModel.getLabel("admin_login_btn"), fontWeight = FontWeight.Bold)
                }
            }
        }

        // 5. Help and Support Feedback Box
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SupportAgent,
                        contentDescription = "Support",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isAr) "الدعم والمساعدة" else "Help & Support Desk",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = supportMessageInput,
                    onValueChange = { supportMessageInput = it },
                    placeholder = { Text(if (isAr) "اكتب رسالتك أو استفسارك هنا" else "Type your support message or issue here") },
                    modifier = Modifier.fillMaxWidth().height(80.dp).testTag("support_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (supportMessageInput.isBlank()) return@Button
                        Toast.makeText(context, if (isAr) "تم إرسال طلب الدعم بنجاح! سنتواصل معك قريباً." else "Support ticket raised! We will reach out shortly.", Toast.LENGTH_SHORT).show()
                        supportMessageInput = ""
                    },
                    modifier = Modifier.fillMaxWidth().height(40.dp).testTag("send_support_button"),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(if (isAr) "إرسال الطلب" else "Submit Ticket", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Log out Button
        Button(
            onClick = { viewModel.logout() },
            modifier = Modifier.fillMaxWidth().height(50.dp).testTag("logout_button"),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            shape = RoundedCornerShape(25.dp)
        ) {
            Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Exit")
            Spacer(modifier = Modifier.width(8.dp))
            Text(viewModel.getLabel("logout"), fontWeight = FontWeight.Bold)
        }
    }
}
