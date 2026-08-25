package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PrivacyPolicyDialog(
    isFirstLaunch: Boolean = false,
    onAccept: () -> Unit,
    onDismiss: () -> Unit
) {
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isFirstLaunch) "Legal Consent & Terms of Use" else "Privacy Policy & Legal Terms",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp)
                    .verticalScroll(scrollState)
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "1. Personal Safety & Emergency Purpose",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Emergency Recorder (ES Calculator) is designed strictly as an evidence preservation tool for situations where your personal safety or physical security is threatened. Developed by Syad Mehedi Hasan Alvi.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "2. One-Party & Two-Party Recording Consent Laws",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Audio and video recording laws vary significantly by jurisdiction. Many regions require all parties to consent to recordings (two-party/all-party consent states/countries), while others require only one party's consent. In some jurisdictions, emergency circumstances grant legal exceptions. You are solely responsible for compliance with local and national recording laws.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "3. Foreground Notification Transparency",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Per Google Play developer policies and Android 14+ platform requirements, an ongoing foreground service notification remains permanently visible in the system status bar whenever emergency monitoring or camera recording is in progress.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "4. Evidence Storage & Cloud Encryption",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Evidence files are saved to private application storage on your device and securely uploaded to Firebase Storage and Firestore under your configured safety UID. No evidence is shared with unauthorized third parties.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onAccept,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (isFirstLaunch) "I Understand & Accept" else "Close")
            }
        },
        dismissButton = if (isFirstLaunch) {
            {
                TextButton(onClick = onDismiss) {
                    Text("Decline")
                }
            }
        } else null
    )
}
