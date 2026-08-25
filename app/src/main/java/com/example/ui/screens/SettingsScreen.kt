package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.FakeScreenMode
import com.example.ui.theme.CrimsonPrimary
import java.util.UUID

@Composable
fun SettingsScreen(
    shakeThreshold: Float,
    fakeScreenMode: FakeScreenMode,
    autoUpload: Boolean,
    recordAudio: Boolean,
    userId: String,
    hapticFeedback: Boolean,
    calculatorPin: String,
    onUpdateShakeThreshold: (Float) -> Unit,
    onUpdateFakeScreenMode: (FakeScreenMode) -> Unit,
    onUpdateAutoUpload: (Boolean) -> Unit,
    onUpdateRecordAudio: (Boolean) -> Unit,
    onUpdateUserId: (String) -> Unit,
    onUpdateHapticFeedback: (Boolean) -> Unit,
    onUpdateCalculatorPin: (String) -> Unit,
    onShowPrivacyPolicy: () -> Unit
) {
    val scrollState = rememberScrollState()
    var isEditingUid by remember { mutableStateOf(false) }
    var tempUid by remember { mutableStateOf(userId) }

    var isEditingPin by remember { mutableStateOf(false) }
    var tempPin by remember { mutableStateOf(calculatorPin) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section: Shake Detection Sensitivity
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("shake_settings_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Vibration,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Triple-Shake Sensitivity",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Text(
                    text = "Requires 3 rapid acceleration spikes exceeding threshold within 1.0 second.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Trigger Threshold:", fontSize = 13.sp)
                    Text(
                        text = "${"%.1f".format(shakeThreshold)}g",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp
                    )
                }

                Slider(
                    value = shakeThreshold,
                    onValueChange = onUpdateShakeThreshold,
                    valueRange = 1.8f..4.0f,
                    steps = 21,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("shake_threshold_slider")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("More Sensitive (1.8g)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Hard Shake (4.0g)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Section: Camouflage / Discreet Screen Mode
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("camouflage_settings_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Calculate,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Discreet Fake Screen Style",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Text(
                    text = "Launched automatically during recording without showing any camera feeds.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Option 1: Working Calculator
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = fakeScreenMode == FakeScreenMode.CALCULATOR,
                            onClick = { onUpdateFakeScreenMode(FakeScreenMode.CALCULATOR) },
                            role = Role.RadioButton
                        )
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = fakeScreenMode == FakeScreenMode.CALCULATOR,
                        onClick = null
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("Working Calculator", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text("Interactive calculator; enter 911= or tap header 3x to unlock", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Option 2: Stealth Black Screen
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = fakeScreenMode == FakeScreenMode.BLACK_SCREEN,
                            onClick = { onUpdateFakeScreenMode(FakeScreenMode.BLACK_SCREEN) },
                            role = Role.RadioButton
                        )
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = fakeScreenMode == FakeScreenMode.BLACK_SCREEN,
                        onClick = null
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("Stealth Black Screen", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text("100% black display; tap 3x to unlock", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // Section: Evidence & Cloud Sync
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.CloudSync,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Evidence & Cloud Sync",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Automatic Cloud Upload", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text("Uploads to Firebase Storage & Firestore immediately or queues offline", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = autoUpload,
                        onCheckedChange = onUpdateAutoUpload,
                        modifier = Modifier.testTag("auto_upload_switch")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Record Microphone Audio", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text("Include environmental audio in evidence recordings", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = recordAudio,
                        onCheckedChange = onUpdateRecordAudio,
                        modifier = Modifier.testTag("record_audio_switch")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Haptic Trigger Pulse", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text("Subtle vibration confirmation on start/stop", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = hapticFeedback,
                        onCheckedChange = onUpdateHapticFeedback
                    )
                }
            }
        }

        // Section: Safety Evidence UID
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Fingerprint,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Safety Evidence UID",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Text(
                    text = "Cloud storage path: users/{uid}/{timestamp}/",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (isEditingUid) {
                    OutlinedTextField(
                        value = tempUid,
                        onValueChange = { tempUid = it },
                        label = { Text("User UID") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(onClick = { isEditingUid = false }) {
                            Text("Cancel")
                        }
                        Spacer(Modifier.width(8.dp))
                        OutlinedButton(onClick = {
                            onUpdateUserId(tempUid)
                            isEditingUid = false
                        }) {
                            Text("Save")
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = userId,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row {
                            IconButton(onClick = {
                                isEditingUid = true
                            }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Edit UID", modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }

        // Section: Calculator PIN
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Calculator Unlock PIN",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Text(
                    text = "Enter this PIN and multiply by 0 to open the main app.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (isEditingPin) {
                    OutlinedTextField(
                        value = tempPin,
                        onValueChange = { if (it.length <= 8) tempPin = it },
                        label = { Text("New PIN") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(onClick = { isEditingPin = false }) {
                            Text("Cancel")
                        }
                        Spacer(Modifier.width(8.dp))
                        OutlinedButton(onClick = {
                            if (tempPin.isNotEmpty()) {
                                onUpdateCalculatorPin(tempPin)
                                isEditingPin = false
                            }
                        }) {
                            Text("Save")
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = calculatorPin,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(onClick = {
                            isEditingPin = true
                            tempPin = calculatorPin
                        }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Change PIN", modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        // Section: Legal & Terms
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Gavel,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Legal Compliance & Privacy",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Text(
                    text = "Ensure compliance with local one-party/two-party recording consent laws. Intended solely for personal safety.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedButton(
                    onClick = onShowPrivacyPolicy,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("View Full Privacy Policy & Legal Terms")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
