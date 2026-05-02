package com.neverno.neverq.auth

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.neverno.neverq.ui.theme.*

@Composable
fun LoginScreen(
    onNavigateTo: (String) -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showGoogleUnavailableDialog by remember { mutableStateOf(false) }
    // false = Customer login, true = Staff login
    var isStaffMode by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.navigateTo) {
        uiState.navigateTo?.let { onNavigateTo(it) }
    }

    if (showGoogleUnavailableDialog) {
        AlertDialog(
            onDismissRequest = { showGoogleUnavailableDialog = false },
            title = { Text("Google Sign In", fontWeight = FontWeight.SemiBold, color = CfNavy) },
            text = {
                Text(
                    "Native Google sign-in is not connected to the NeverQ Android API yet. Please use email and password in the app for now.",
                    color = CfText,
                )
            },
            confirmButton = {
                TextButton(onClick = { showGoogleUnavailableDialog = false }) {
                    Text("OK", color = CfBlue)
                }
            },
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = if (isStaffMode)
                        listOf(Color(0xFF0D1B4B), Color(0xFF1A237E), Color(0xFF1565C0))
                    else
                        listOf(Color(0xFF0D1B2E), Color(0xFF15233B), Color(0xFF1A4D91))
                )
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(24.dp))

            // ── Brand ────────────────────────────────────────────────────────
            Text(
                text = "NeverQ",
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 1.sp,
            )
            Text(
                text = if (isStaffMode) "STAFF PORTAL" else "POWERED BY NEVERNO",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.6f),
                letterSpacing = 2.sp,
            )

            Spacer(Modifier.height(24.dp))

            // ── Mode toggle ──────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.12f))
                    .padding(4.dp),
            ) {
                ModeTab(
                    label = "Customer",
                    icon = Icons.Default.Person,
                    selected = !isStaffMode,
                    onClick = { isStaffMode = false; viewModel.clearError() },
                )
                ModeTab(
                    label = "Staff",
                    icon = Icons.Default.Badge,
                    selected = isStaffMode,
                    onClick = { isStaffMode = true; viewModel.clearError() },
                )
            }

            Spacer(Modifier.height(20.dp))

            // ── Login card ───────────────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 20.dp),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    // Heading
                    Text(
                        text = if (isStaffMode) "Staff Sign In" else "Sign In",
                        fontSize = if (isStaffMode) 20.sp else 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = CfNavy,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        text = if (isStaffMode)
                            "Use your staff email and password"
                        else
                            "Use your registered email to continue",
                        fontSize = 12.sp,
                        color = CfMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 20.dp),
                    )

                    // Email
                    LoginFieldLabel("Email Address")
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text("your@email.com", color = CfMuted, fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Default.Email, null, tint = CfMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CfBlue,
                            unfocusedBorderColor = CfBorder,
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC),
                        ),
                    )

                    Spacer(Modifier.height(12.dp))

                    // Password
                    LoginFieldLabel("Password")
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("••••••••", color = CfMuted, fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = CfMuted) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    null,
                                    tint = CfMuted,
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CfBlue,
                            unfocusedBorderColor = CfBorder,
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC),
                        ),
                    )

                    // Error box
                    AnimatedVisibility(
                        visible = uiState.error != null,
                        modifier = Modifier.padding(top = 12.dp),
                    ) {
                        uiState.error?.let { error ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(CfRedLight)
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Top,
                            ) {
                                Icon(Icons.Default.ErrorOutline, null, tint = CfRed, modifier = Modifier.size(18.dp).padding(top = 1.dp))
                                Text(error, color = CfRed, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    Button(
                        onClick = { viewModel.login(email, password) },
                        enabled = !uiState.isLoading && email.isNotBlank() && password.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isStaffMode) CfNavy else CfBlue,
                        ),
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Login, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Sign In", fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                        }
                    }

                    if (!isStaffMode) {
                        Spacer(Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            HorizontalDivider(modifier = Modifier.weight(1f), color = CfBorder)
                            Text("or", fontSize = 12.sp, color = CfMuted)
                            HorizontalDivider(modifier = Modifier.weight(1f), color = CfBorder)
                        }
                        Spacer(Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = { showGoogleUnavailableDialog = true },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CfText),
                            border = BorderStroke(1.dp, CfBorder),
                        ) {
                            Text("G", color = CfBlue, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(8.dp))
                            Text("Continue with Google", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Text(
                            text = "Google app sign-in needs an Android OAuth client and a NeverQ API token endpoint before it can log into the app.",
                            fontSize = 11.sp,
                            color = CfMuted,
                            modifier = Modifier.padding(top = 8.dp),
                            lineHeight = 15.sp,
                        )
                    }
                }
            }

            // ── Switch mode hint at bottom ───────────────────────────────────
            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.1f))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .clickable { isStaffMode = !isStaffMode; viewModel.clearError() },
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    if (isStaffMode) Icons.Default.Person else Icons.Default.Badge,
                    null,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = if (isStaffMode) "Back to Customer Portal" else "Neverno Staff Sign In",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Medium,
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ModeTab(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) Color.White else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                icon,
                null,
                tint = if (selected) CfNavy else Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp),
            )
            Text(
                label,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) CfNavy else Color.White.copy(alpha = 0.7f),
            )
        }
    }
}

@Composable
private fun LoginFieldLabel(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF4B5563),
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(bottom = 5.dp),
    )
}
