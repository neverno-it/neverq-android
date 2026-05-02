package com.neverno.neverq.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.neverno.neverq.core.models.StaffUser
import com.neverno.neverq.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminStaffScreen(viewModel: AdminViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadStaff() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Staff", fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 18.sp) },
                actions = {
                    IconButton(onClick = { viewModel.loadStaff() }) {
                        Icon(Icons.Default.Refresh, "Refresh", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CfNavy),
            )
        },
        containerColor = CfSurface,
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when {
                uiState.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center), color = CfBlue)
                uiState.error != null -> Text(uiState.error!!, Modifier.align(Alignment.Center), color = CfRed)
                uiState.staff.isEmpty() -> StaffEmptyState(Modifier.align(Alignment.Center))
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(uiState.staff, key = { it.id }) { user ->
                        StaffCard(user)
                    }
                }
            }
        }
    }
}

@Composable
private fun StaffCard(user: StaffUser) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(10.dp)).background(CfBlueLight),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.People, null, tint = CfBlue, modifier = Modifier.size(22.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(user.name, color = CfNavy, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(user.email, color = CfText, fontSize = 13.sp)
                user.phone?.takeIf { it.isNotBlank() }?.let {
                    Text(it, color = CfMuted, fontSize = 12.sp)
                }
                Text(user.companyName ?: "No company", color = CfMuted, fontSize = 12.sp)
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                BadgeText(user.roleLabel, CfBlue, CfBlueLight)
                BadgeText(if (user.isActive) "Active" else "Inactive", if (user.isActive) CfGreen else CfRed, if (user.isActive) CfGreenLight else CfRedLight)
            }
        }
    }
}

@Composable
private fun StaffEmptyState(modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(Icons.Default.People, null, tint = CfMuted, modifier = Modifier.size(48.dp))
        Text("No staff found", fontWeight = FontWeight.SemiBold, color = CfNavy)
    }
}

@Composable
private fun BadgeText(text: String, color: Color, background: Color) {
    Box(
        modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(background).padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(text, color = color, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}
