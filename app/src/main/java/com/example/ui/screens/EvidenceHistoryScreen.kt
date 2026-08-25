package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.MainActivity
import com.example.data.models.EvidenceSession
import com.example.data.models.UploadStatus
import java.io.File
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningOrange

@Composable
fun EvidenceHistoryScreen(
    sessions: List<EvidenceSession>,
    onRetryUpload: (String) -> Unit,
    onDeleteSession: (String) -> Unit
) {
    val context = LocalContext.current
    var selectedSession by remember { mutableStateOf<EvidenceSession?>(null) }
    var sessionToDelete by remember { mutableStateOf<String?>(null) }

    if (sessions.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
                .testTag("empty_evidence_view"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Text(
                    text = "No Evidence Recorded",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Trigger emergency recording via triple-shake or the panic button to capture dual-camera evidence with cloud backup.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .testTag("evidence_list"),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(sessions, key = { it.id }) { session ->
                EvidenceItemCard(
                    session = session,
                    onClick = { selectedSession = session },
                    onRetryUpload = { onRetryUpload(session.id) },
                    onDelete = { sessionToDelete = session.id }
                )
            }
        }
    }

    // Detail Dialog
    selectedSession?.let { session ->
        EvidenceDetailDialog(
            session = session,
            context = context,
            onDismiss = { selectedSession = null },
            onRetryUpload = {
                onRetryUpload(session.id)
                selectedSession = null
            },
            onDelete = {
                sessionToDelete = session.id
                selectedSession = null
            }
        )
    }

    // Delete Confirmation Dialog
    sessionToDelete?.let { id ->
        AlertDialog(
            onDismissRequest = { sessionToDelete = null },
            title = { Text("Delete Evidence Record?") },
            text = { Text("This will permanently remove the local video recordings from this device.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteSession(id)
                        sessionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { sessionToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun EvidenceItemCard(
    session: EvidenceSession,
    onClick: () -> Unit,
    onRetryUpload: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("evidence_item_${session.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                when (session.uploadStatus) {
                                    UploadStatus.UPLOADED -> SuccessGreen
                                    UploadStatus.UPLOADING -> WarningOrange
                                    UploadStatus.QUEUED_OFFLINE -> Color(0xFF64B5F6)
                                    UploadStatus.FAILED -> CrimsonPrimary
                                    UploadStatus.RECORDED_LOCAL -> Color.Gray
                                }
                            )
                    )
                    Text(
                        text = session.formattedTimestamp,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = session.triggerType,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⏱ ${session.durationSeconds}s",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "📦 ${session.formattedSize}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (session.location != null) {
                        Text(
                            text = "📍 GPS Attached",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                UploadStatusBadge(
                    status = session.uploadStatus,
                    onRetry = onRetryUpload
                )
            }
        }
    }
}

@Composable
fun UploadStatusBadge(
    status: UploadStatus,
    onRetry: () -> Unit
) {
    when (status) {
        UploadStatus.UPLOADED -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Default.CloudDone,
                    contentDescription = "Cloud Uploaded",
                    tint = SuccessGreen,
                    modifier = Modifier.size(16.dp)
                )
                Text("Cloud Saved", fontSize = 11.sp, color = SuccessGreen, fontWeight = FontWeight.SemiBold)
            }
        }
        UploadStatus.UPLOADING -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp)
                Text("Syncing...", fontSize = 11.sp, color = WarningOrange)
            }
        }
        UploadStatus.QUEUED_OFFLINE -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Default.CloudQueue,
                    contentDescription = "Queued",
                    tint = Color(0xFF64B5F6),
                    modifier = Modifier.size(16.dp)
                )
                Text("Queued", fontSize = 11.sp, color = Color(0xFF64B5F6))
            }
        }
        UploadStatus.FAILED -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.clickable { onRetry() }
            ) {
                Icon(
                    Icons.Default.Error,
                    contentDescription = "Failed",
                    tint = CrimsonPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Text("Retry", fontSize = 11.sp, color = CrimsonPrimary, fontWeight = FontWeight.Bold)
            }
        }
        UploadStatus.RECORDED_LOCAL -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.clickable { onRetry() }
            ) {
                Icon(
                    Icons.Default.CloudUpload,
                    contentDescription = "Upload",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Text("Upload", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun EvidenceDetailDialog(
    session: EvidenceSession,
    context: Context,
    onDismiss: () -> Unit,
    onRetryUpload: () -> Unit,
    onDelete: () -> Unit
) {
    fun playVideo(videoPath: String?) {
        if (videoPath == null) return
        val file = File(videoPath)
        if (file.exists()) {
            try {
                val uri = FileProvider.getUriForFile(
                    context,
                    "com.aistudio.emergencyrecorder.svzk.fileprovider",
                    file
                )
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "video/mp4")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                MainActivity.isLaunchingExternalActivity = true
                context.startActivity(intent)
            } catch (e: Exception) {
                // Log error or show toast if needed
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Evidence Session Details", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DetailRow("Timestamp", session.formattedTimestamp)
                DetailRow("Duration", "${session.durationSeconds} seconds")
                DetailRow("Total Size", session.formattedSize)
                DetailRow("Trigger", session.triggerType)
                DetailRow("Device", session.deviceModel)
                DetailRow("Upload Status", session.uploadStatus.name)

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Text("Recorded Footage:", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { playVideo(session.backVideoPath) },
                        modifier = Modifier.weight(1f),
                        enabled = session.backVideoPath != null && File(session.backVideoPath).exists(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.VideoFile, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Back Cam", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = { playVideo(session.frontVideoPath) },
                        modifier = Modifier.weight(1f),
                        enabled = session.frontVideoPath != null && File(session.frontVideoPath).exists(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.VideoFile, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Front Cam", fontSize = 11.sp)
                    }
                }

                if (session.location != null) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    DetailRow("Coordinates", session.location.toFormattedString())

                    OutlinedButton(
                        onClick = {
                            val uri = Uri.parse("geo:${session.location.latitude},${session.location.longitude}?q=${session.location.latitude},${session.location.longitude}(Emergency+Evidence)")
                            val mapIntent = Intent(Intent.ACTION_VIEW, uri)
                            MainActivity.isLaunchingExternalActivity = true
                            context.startActivity(mapIntent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("View Location on Map", fontSize = 12.sp)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val shareText = "Emergency Evidence Report:\nTimestamp: ${session.formattedTimestamp}\nDuration: ${session.durationSeconds}s\nGPS: ${session.location?.toFormattedString() ?: "N/A"}\nDevice: ${session.deviceModel}\nStatus: ${session.uploadStatus.name}"
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            MainActivity.isLaunchingExternalActivity = true
                            context.startActivity(Intent.createChooser(intent, "Share Evidence Summary"))
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Share", fontSize = 12.sp)
                    }

                    if (session.uploadStatus != UploadStatus.UPLOADED) {
                        Button(
                            onClick = onRetryUpload,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Upload", fontSize = 12.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDelete,
                colors = ButtonDefaults.textButtonColors(contentColor = CrimsonPrimary)
            ) {
                Text("Delete")
            }
        }
    )
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
