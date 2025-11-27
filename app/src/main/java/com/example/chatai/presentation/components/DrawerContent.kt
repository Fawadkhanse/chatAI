package com.example.chatai.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

data class ChatHistory(
    val id: String,
    val title: String,
    val timestamp: Long,
    val messageCount: Int
)

@Composable
fun DrawerContent(
    chatHistories: List<ChatHistory>,
    onHistoryClick: (ChatHistory) -> Unit,
    onDeleteHistory: (ChatHistory) -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E88E5),
                        Color(0xFF1565C0)
                    )
                )
            )
    ) {
        // Header Section
        DrawerHeader()

        // Content Section
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(Color.White)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // History Section
            Text(
                text = "Chat History",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C2C2C),
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // History List
            if (chatHistories.isEmpty()) {
                EmptyHistoryState()
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(chatHistories) { history ->
                        HistoryItem(
                            history = history,
                            onClick = { onHistoryClick(history) },
                            onDelete = { onDeleteHistory(history) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Divider(
                modifier = Modifier.padding(horizontal = 24.dp),
                color = Color.Gray.copy(alpha = 0.2f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Logout Button
            LogoutButton(onClick = onLogoutClick)

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DrawerHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.Start
        ) {
            // Avatar
            Surface(
                modifier = Modifier.size(72.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.3f)
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🤖",
                        fontSize = 40.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Fintech AI",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Your AI Assistant",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
private fun EmptyHistoryState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Send,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color.Gray.copy(alpha = 0.3f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No chat history yet",
            fontSize = 16.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your conversations will appear here",
            fontSize = 14.sp,
            color = Color.Gray.copy(alpha = 0.7f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryItem(
    history: ChatHistory,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = Color.Blue.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = null,
                        tint = Color.Blue,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = history.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2C2C2C),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatTimestamp(history.timestamp),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "•",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "${history.messageCount} messages",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            // Delete Button
            IconButton(
                onClick = { showDeleteDialog = true }
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = Color.Red
                )
            },
            title = {
                Text(text = "Delete Chat History")
            },
            text = {
                Text(text = "Are you sure you want to delete this chat history? This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color.Red
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun LogoutButton(onClick: () -> Unit) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    Surface(
        onClick = { showLogoutDialog = true },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFFFEBEE)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.ExitToApp,
                contentDescription = null,
                tint = Color.Red,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "Logout",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Red
            )
        }
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = null,
                    tint = Color.Red
                )
            },
            title = {
                Text(text = "Logout")
            },
            text = {
                Text(text = "Are you sure you want to logout?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClick()
                        showLogoutDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color.Red
                    )
                ) {
                    Text("Logout")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60000 -> "Just now"
        diff < 3600000 -> "${diff / 60000}m ago"
        diff < 86400000 -> "${diff / 3600000}h ago"
        diff < 604800000 -> "${diff / 86400000}d ago"
        else -> {
            val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}