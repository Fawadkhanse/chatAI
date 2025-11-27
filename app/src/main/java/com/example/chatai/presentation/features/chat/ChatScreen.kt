package com.example.chatai.presentation.features.chat


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chatai.presentation.components.CustomTextField
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChatScreenRoute(
    onBack: () -> Unit
) {
    var messages by remember { mutableStateOf(listOf<ChatMessage>()) }
    var messageText by remember { mutableStateOf("") }

    // Add initial welcome message
    LaunchedEffect(Unit) {
        messages = listOf(
            ChatMessage(
                id = "1",
                text = "Hello! Welcome to ChatApp. How can I help you today?",
                isFromUser = false,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    ChatScreen(
        messages = messages,
        messageText = messageText,
        onMessageTextChange = { messageText = it },
        onSendMessage = {
            if (messageText.isNotBlank()) {
                val newMessage = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    text = messageText,
                    isFromUser = true,
                    timestamp = System.currentTimeMillis()
                )
                messages = messages + newMessage
                messageText = ""

                // Simulate bot response
                GlobalScope.launch {
                    delay(1000)
                    val botResponse = ChatMessage(
                        id = UUID.randomUUID().toString(),
                        text = "Thanks for your message! This is a demo response.",
                        isFromUser = false,
                        timestamp = System.currentTimeMillis()
                    )
                    messages = messages + botResponse
                }
            }
        },
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    messages: List<ChatMessage>,
    messageText: String,
    onMessageTextChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onBack: () -> Unit
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Auto-scroll to bottom when new message arrives
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        // Avatar
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = CircleShape,
                            color =  Color.Blue.copy(alpha = 0.2f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "🤖",
                                    fontSize = 20.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Fintech AI",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Online",
                                fontSize = 12.sp,
                                color = Color(0xFF4CAF50)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Show menu */ }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF2C2C2C)
                )
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                color = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Message Input
                    CustomTextField(
                        value = messageText,
                        onValueChange = onMessageTextChange,
                        label = "",
                        placeholder = "Type a message...",
                        modifier = Modifier.weight(1f),
                        keyboardType = KeyboardType.Text,
                        singleLine = false,
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Send Button
                    FloatingActionButton(
                        onClick = onSendMessage,
                        modifier = Modifier.size(56.dp),
                        containerColor = Color.Blue,
                        contentColor = Color.White
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send message"
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        // Messages List
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(paddingValues)
        ) {
            if (messages.isEmpty()) {
                // Empty State
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "💬",
                        fontSize = 64.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No messages yet",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Start a conversation by sending a message",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(messages) { message ->
                        ChatMessageItem(message = message)
                    }
                }
            }
        }
    }
}

@Composable
fun ChatMessageItem(
    message: ChatMessage
) {
    val alignment = if (message.isFromUser) Alignment.End else Alignment.Start
    val backgroundColor = if (message.isFromUser)
        Color.Blue
    else
        Color.White
    val textColor = if (message.isFromUser) Color.White else Color(0xFF2C2C2C)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 280.dp),
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = if (message.isFromUser) 20.dp else 4.dp,
                bottomEnd = if (message.isFromUser) 4.dp else 20.dp
            ),
            color = backgroundColor,
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = message.text,
                    color = textColor,
                    fontSize = 15.sp,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = formatTimestamp(message.timestamp),
                    color = textColor.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

// Data Model
data class ChatMessage(
    val id: String,
    val text: String,
    val isFromUser: Boolean,
    val timestamp: Long
)

// Utility function
private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

@Preview(showBackground = true)
@Composable
fun ChatScreenPreview() {
    MaterialTheme {
        ChatScreen(
            messages = listOf(
                ChatMessage(
                    id = "1",
                    text = "Hello! How can I help you?",
                    isFromUser = false,
                    timestamp = System.currentTimeMillis()
                ),
                ChatMessage(
                    id = "2",
                    text = "Hi! I need some information about your services.",
                    isFromUser = true,
                    timestamp = System.currentTimeMillis()
                ),
                ChatMessage(
                    id = "3",
                    text = "Of course! I'd be happy to help. What would you like to know?",
                    isFromUser = false,
                    timestamp = System.currentTimeMillis()
                )
            ),
            messageText = "",
            onMessageTextChange = {},
            onSendMessage = {},
            onBack = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ChatScreenEmptyPreview() {
    MaterialTheme {
        ChatScreen(
            messages = emptyList(),
            messageText = "",
            onMessageTextChange = {},
            onSendMessage = {},
            onBack = {}
        )
    }
}