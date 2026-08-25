package com.example.ui.fake

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FakeBlackScreen(
    isRecording: Boolean,
    onStopRecording: () -> Unit,
    onExitToMain: () -> Unit
) {
    var tapCount by remember { mutableStateOf(0) }
    var showModal by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("fake_black_screen")
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                tapCount++
                if (tapCount >= 3) {
                    tapCount = 0
                    showModal = true
                }
            }
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Pure black display; subtle ultra-dim emergency hint only during first tap
        if (tapCount in 1..2) {
            Text(
                text = "Tap ${3 - tapCount} more times to unlock",
                color = Color(0x33FFFFFF),
                fontSize = 12.sp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(24.dp)
            )
        }
    }

    if (showModal) {
        EmergencyControlDialog(
            isRecording = isRecording,
            onDismiss = { showModal = false },
            onStopRecording = {
                onStopRecording()
                showModal = false
            },
            onExitToDashboard = {
                showModal = false
                onExitToMain()
            }
        )
    }
}
