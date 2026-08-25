package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.FakeScreenMode
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.CrimsonPrimaryDark
import com.example.ui.theme.SuccessGreen

@Composable
fun HomeScreen(
    isMonitoring: Boolean,
    isRecording: Boolean,
    durationSeconds: Long,
    shakeThreshold: Float,
    fakeScreenMode: FakeScreenMode,
    onToggleMonitoring: (Boolean) -> Unit,
    onTriggerPanicRecord: () -> Unit,
    onStopRecording: () -> Unit,
    onLaunchDiscreetScreen: () -> Unit
) {
    val scrollState = rememberScrollState()

    // Pulse animation for active recording
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Status Header Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("status_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isRecording) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)
                } else if (isMonitoring) {
                    MaterialTheme.colorScheme.surfaceVariant
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                }
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (isRecording) CrimsonPrimary
                                else if (isMonitoring) SuccessGreen
                                else Color.Gray.copy(alpha = 0.4f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isRecording) Icons.Default.FiberManualRecord
                            else if (isMonitoring) Icons.Default.Sensors
                            else Icons.Default.PowerSettingsNew,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = if (isRecording) "RECORDING ACTIVE"
                            else if (isMonitoring) "SHAKE MONITOR ACTIVE"
                            else "MONITORING IDLE",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (isRecording) CrimsonPrimaryDark
                            else if (isMonitoring) SuccessGreen
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (isRecording) "Dual-cam evidence capturing"
                            else if (isMonitoring) "Triple-shake detection > ${"%.1f".format(shakeThreshold)}g"
                            else "Toggle switch to activate background shake",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = isMonitoring,
                    onCheckedChange = onToggleMonitoring,
                    enabled = !isRecording,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = SuccessGreen
                    ),
                    modifier = Modifier.testTag("monitoring_switch")
                )
            }
        }

        // Active Recording Live Banner (if recording)
        AnimatedVisibility(
            visible = isRecording,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("active_recording_banner"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(listOf(CrimsonPrimary, Color(0xFFFF8A80)))
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(CrimsonPrimary)
                        )
                        val min = durationSeconds / 60
                        val sec = durationSeconds % 60
                        Text(
                            text = "%02d:%02d".format(min, sec),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = "Synchronized 1080p Back + Front Camera Stream",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onStopRecording,
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("stop_recording_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Stop & Save", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onLaunchDiscreetScreen,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("discreet_screen_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                if (fakeScreenMode == FakeScreenMode.CALCULATOR) Icons.Default.Calculate
                                else Icons.Default.Visibility,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Discreet UI", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // Master Panic / Record Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .scale(if (isRecording) pulseScale else 1f)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = if (isRecording) {
                                listOf(CrimsonPrimary, CrimsonPrimaryDark, Color(0xFF3B0B0B))
                            } else {
                                listOf(CrimsonPrimary, Color(0xFFB71C1C), Color(0xFF1E1014))
                            }
                        )
                    )
                    .border(
                        width = 4.dp,
                        color = if (isRecording) Color.White.copy(alpha = 0.8f) else Color(0x33FF5252),
                        shape = CircleShape
                    )
                    .clickable {
                        if (isRecording) {
                            onStopRecording()
                        } else {
                            onTriggerPanicRecord()
                        }
                    }
                    .testTag("master_panic_button"),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Emergency,
                        contentDescription = if (isRecording) "Stop Emergency Recording" else "Trigger Emergency Recording",
                        tint = Color.White,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isRecording) "STOP & SAVE" else "PANIC RECORD",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = if (isRecording) "Dual Cameras Active" else "Or Shake 3 Times",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Quick Feature Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // App Branding & Credits
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Emergency Security Calculator",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    fontFamily = FontFamily.SansSerif
                )
                Text(
                    text = "By Syad Mehedi Hasan Alvi",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Discreet Fake UI Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onLaunchDiscreetScreen() }
                    .testTag("quick_fake_ui_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Calculate,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Text(
                        text = "Discreet UI",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Open camouflage ${fakeScreenMode.name.lowercase().replace('_', ' ')}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Triple Shake Guide Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .testTag("shake_guide_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SuccessGreen.copy(alpha = 0.15f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Vibration,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Text(
                        text = "Triple Shake",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "3 quick shakes (>2.5g in 1s) triggers recorder",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Safety Specs Brief Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Default.Security,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Evidence Protection Safeguards",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
                Text(
                    text = "• Dual simultaneous video recorded to app-private sandbox\n• GPS coordinates recorded at moment of emergency\n• Immediate automatic cloud synchronization with retry queue\n• Persistent notification complies with Android safety standards",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
