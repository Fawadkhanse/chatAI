package com.example.chatai.presentation.features.chat

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chatai.R
import com.example.chatai.presentation.components.ChatHistory
import com.example.chatai.presentation.components.DrawerContent
import com.example.chatai.utils.SpeechManager
import com.example.chatai.utils.SpeechState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Composable
fun ChatScreenRoute(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val speechManager = remember { SpeechManager(context) }

    var messages by remember { mutableStateOf(listOf<ChatMessage>()) }
    var chatHistories by remember { mutableStateOf(listOf<ChatHistory>()) }
    var showPermissionDialog by remember { mutableStateOf(false) }

    // Speech state
    val speechState by speechManager.speechState.collectAsState()
    val recognizedText by speechManager.recognizedText.collectAsState()
    val isTTSSpeaking by speechManager.isSpeaking.collectAsState()

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            speechManager.startListening()
        } else {
            showPermissionDialog = true
        }
    }

    // Handle speech state changes
    LaunchedEffect(speechState) {
        when (speechState) {
            is SpeechState.Success -> {
                val text = (speechState as SpeechState.Success).text
                if (text.isNotBlank()) {
                    // Add user message
                    val userMessage = ChatMessage(
                        id = UUID.randomUUID().toString(),
                        text = text,
                        isFromUser = true,
                        timestamp = System.currentTimeMillis()
                    )
                    messages = messages + userMessage

                    // Simulate bot response with TTS
                    delay(1000)
                    val botResponse = ChatMessage(
                        id = UUID.randomUUID().toString(),
                        text = "I heard you say: \"$text\". This is a demo AI response. How can I help you further?",
                        isFromUser = false,
                        timestamp = System.currentTimeMillis()
                    )
                    messages = messages + botResponse

                    // Speak the bot response
                    speechManager.speak(botResponse.text)
                }
            }
            is SpeechState.Error -> {
                val error = (speechState as SpeechState.Error).message
                // Show error to user
                val errorMessage = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    text = "Error: $error. Please try again.",
                    isFromUser = false,
                    timestamp = System.currentTimeMillis()
                )
                messages = messages + errorMessage
            }
            else -> { /* Do nothing */ }
        }
    }

    // Add initial welcome message
    LaunchedEffect(Unit) {
        val welcomeMessage = ChatMessage(
            id = "1",
            text = "Hello! Welcome to Voice Chat. Press the microphone button and start speaking!",
            isFromUser = false,
            timestamp = System.currentTimeMillis()
        )
        messages = listOf(welcomeMessage)

        // Speak welcome message
        delay(500)
        speechManager.speak(welcomeMessage.text)

        // Sample chat histories
        chatHistories = listOf(
            ChatHistory(
                id = "1",
                title = "Budget Planning Discussion",
                timestamp = System.currentTimeMillis() - 3600000,
                messageCount = 15
            ),
            ChatHistory(
                id = "2",
                title = "Investment Advice",
                timestamp = System.currentTimeMillis() - 86400000,
                messageCount = 8
            ),
            ChatHistory(
                id = "3",
                title = "Loan Application Help",
                timestamp = System.currentTimeMillis() - 172800000,
                messageCount = 22
            )
        )
    }

    // Cleanup on dispose
    DisposableEffect(Unit) {
        onDispose {
            speechManager.cleanup()
        }
    }

    // Permission dialog
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_mic),
                    contentDescription = null,
                    tint = Color.Red
                )
            },
            title = { Text("Microphone Permission Required") },
            text = { Text("This app needs microphone access to recognize your voice. Please grant the permission in your device settings.") },
            confirmButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    ChatScreen(
        messages = messages,
        chatHistories = chatHistories,
        isListening = speechState is SpeechState.Listening || speechState is SpeechState.Speaking,
        isSpeaking = isTTSSpeaking,
        recognizedText = recognizedText,
        speechState = speechState,
        onVoiceInputClick = {
            when (speechState) {
                is SpeechState.Listening, is SpeechState.Speaking, is SpeechState.Processing -> {
                    speechManager.cancelListening()
                }
                else -> {
                    // Request microphone permission and start listening
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            }
        },
        onStopSpeaking = {
            speechManager.stopSpeaking()
        },
        onHistoryClick = { history ->
            println("Loading history: ${history.title}")
        },
        onDeleteHistory = { history ->
            chatHistories = chatHistories.filter { it.id != history.id }
        },
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    messages: List<ChatMessage>,
    chatHistories: List<ChatHistory>,
    isListening: Boolean,
    isSpeaking: Boolean,
    recognizedText: String,
    speechState: SpeechState,
    onVoiceInputClick: () -> Unit,
    onStopSpeaking: () -> Unit,
    onHistoryClick: (ChatHistory) -> Unit,
    onDeleteHistory: (ChatHistory) -> Unit,
    onBack: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Auto-scroll to bottom when new message arrives
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(320.dp),
                drawerContainerColor = Color.Transparent
            ) {
                DrawerContent(
                    chatHistories = chatHistories,
                    onHistoryClick = { history ->
                        onHistoryClick(history)
                        scope.launch { drawerState.close() }
                    },
                    onDeleteHistory = onDeleteHistory,
                    onLogoutClick = {
                        scope.launch { drawerState.close() }
                        onBack()
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Surface(
                                modifier = Modifier.size(40.dp),
                                shape = CircleShape,
                                color = Color.Blue.copy(alpha = 0.2f)
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
                                    text = if (isSpeaking) "Speaking..." else if (isListening) "Listening..." else "Online",
                                    fontSize = 12.sp,
                                    color = if (isSpeaking) Color(0xFFFF9800) else if (isListening) Color.Red else Color(0xFF4CAF50)
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                if (drawerState.isClosed) {
                                    drawerState.open()
                                } else {
                                    drawerState.close()
                                }
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu"
                            )
                        }
                    },
                    actions = {
                        // Stop speaking button
                        if (isSpeaking) {
                            IconButton(onClick = onStopSpeaking) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Stop speaking",
                                    tint = Color.Red
                                )
                            }
                        }
//
//                        IconButton(onClick = { /* TODO: Show menu */ }) {
//                            Icon(
//                                imageVector = Icons.Default.MoreVert,
//                                contentDescription = "More options"
//                            )
//                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White,
                        titleContentColor = Color(0xFF2C2C2C)
                    )
                )
            },
            bottomBar = {
                VoiceInputBottomBar(
                    isListening = isListening,
                    isSpeaking = isSpeaking,
                    recognizedText = recognizedText,
                    speechState = speechState,
                    onVoiceInputClick = onVoiceInputClick
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF5F5F5))
                    .padding(paddingValues)
            ) {
                if (messages.isEmpty()) {
                    EmptyMessagesState()
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

                // Listening Overlay
                if (isListening) {
                    ListeningOverlay(
                        recognizedText = recognizedText,
                        speechState = speechState
                    )
                }
            }
        }
    }
}

@Composable
fun VoiceInputBottomBar(
    isListening: Boolean,
    isSpeaking: Boolean,
    recognizedText: String,
    speechState: SpeechState,
    onVoiceInputClick: () -> Unit
) {
    Surface(
        shadowElevation = 8.dp,
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Status text
            if (recognizedText.isNotEmpty() && !isListening) {
                Text(
                    text = "You said: \"$recognizedText\"",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Voice button with animation
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                AnimatedVoiceButton(
                    isListening = isListening,
                    isSpeaking = isSpeaking,
                    onClick = onVoiceInputClick
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Instruction text
            Text(
                text = when {
                    isSpeaking -> "AI is speaking..."
                    isListening -> "Listening... Tap to stop"
                    speechState is SpeechState.Processing -> "Processing your speech..."
                    else -> "Tap the microphone to speak"
                },
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun AnimatedVoiceButton(
    isListening: Boolean,
    isSpeaking: Boolean,
    onClick: () -> Unit
) {
    // Pulsing animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening || isSpeaking) 1.2f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    FloatingActionButton(
        onClick = onClick,
        modifier = Modifier
            .size(80.dp)
            .scale(scale),
        containerColor = when {
            isSpeaking -> Color(0xFFFF9800)
            isListening -> Color.Red
            else -> Color(0xFF6A1B9A)
        },
        contentColor = Color.White
    ) {
        when {
            isSpeaking -> Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Speaking",
                modifier = Modifier.size(36.dp)
            )
            isListening -> Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Stop listening",
                modifier = Modifier.size(36.dp)
            )
            else -> Icon(
                painter = painterResource(id = R.drawable.ic_mic),
                contentDescription = "Start voice input",
                modifier = Modifier.size(36.dp)
            )
        }
        /*
        // Original code that doesn't compile due to mixed types
        Icon(
            // This is the problematic part mixing ImageVector and Int (DrawableRes)
            // imageVector = when {
            //     isSpeaking -> Icons.Default.Add
            //     isListening -> Icons.Default.Close
            //     else -> R.drawable.ic_mic // This is an Int, not an ImageVector
            // },
            contentDescription = when {
                isSpeaking -> "Speaking"
                isListening -> "Stop listening"
                else -> "Start voice input"
            },
            modifier = Modifier.size(36.dp)
        )
        */
    }
}

@Composable
fun ListeningOverlay(
    recognizedText: String,
    speechState: SpeechState
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(8.dp),
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Animated microphone icon
                val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 0.5f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(800, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "alpha"
                )

                Icon(
                    painter = painterResource(id = R.drawable.ic_mic),
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color.Red.copy(alpha = alpha)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = when (speechState) {
                        is SpeechState.Initializing -> "Initializing..."
                        is SpeechState.Listening -> "Listening..."
                        is SpeechState.Speaking -> "Listening..."
                        is SpeechState.Processing -> "Processing..."
                        else -> "Ready"
                    },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2C2C2C)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Speak now",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                if (recognizedText.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Divider()

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Recognized:",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "\"$recognizedText\"",
                        fontSize = 16.sp,
                        color = Color(0xFF2C2C2C),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = Color.Red
                )
            }
        }
    }
}

@Composable
fun EmptyMessagesState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_mic),
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color.Gray.copy(alpha = 0.3f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Voice Chat Ready",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Press the microphone button below and start speaking to chat with AI",
            fontSize = 14.sp,
            color = Color.Gray.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ChatMessageItem(message: ChatMessage) {
    val alignment = if (message.isFromUser) Alignment.End else Alignment.Start
    val backgroundColor = if (message.isFromUser) Color.Blue else Color.White
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
                    text = "I need some information about your services.",
                    isFromUser = true,
                    timestamp = System.currentTimeMillis()
                )
            ),
            chatHistories = emptyList(),
            isListening = false,
            isSpeaking = false,
            recognizedText = "",
            speechState = SpeechState.Idle,
            onVoiceInputClick = {},
            onStopSpeaking = {},
            onHistoryClick = {},
            onDeleteHistory = {},
            onBack = {}
        )
    }
}