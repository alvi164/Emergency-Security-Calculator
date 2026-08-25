package com.example.ui.fake

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.DecimalFormat

@Composable
fun FakeCalculatorScreen(
    isRecording: Boolean,
    calculatorPin: String = "1234",
    onStopRecording: () -> Unit,
    onExitToMain: () -> Unit
) {
    var displayExpression by remember { mutableStateOf("0") }
    var previousValue by remember { mutableStateOf<Double?>(null) }
    var pendingOp by remember { mutableStateOf<String?>(null) }
    var isNewNumber by remember { mutableStateOf(true) }

    var showEmergencyModal by remember { mutableStateOf(false) }
    var headerTapCount by remember { mutableStateOf(0) }

    val decimalFormat = remember { DecimalFormat("#,###.########") }

    fun handleDigit(d: String) {
        if (isNewNumber || displayExpression == "0") {
            displayExpression = d
            isNewNumber = false
        } else {
            if (displayExpression.length < 12) {
                displayExpression += d
            }
        }
    }

    fun handleDecimal() {
        if (isNewNumber) {
            displayExpression = "0."
            isNewNumber = false
        } else if (!displayExpression.contains(".")) {
            displayExpression += "."
        }
    }

    fun handleClear() {
        displayExpression = "0"
        previousValue = null
        pendingOp = null
        isNewNumber = true
    }

    fun handleSign() {
        val current = displayExpression.toDoubleOrNull() ?: 0.0
        val toggled = -current
        displayExpression = if (toggled % 1.0 == 0.0) toggled.toLong().toString() else toggled.toString()
    }

    fun handlePercent() {
        val current = displayExpression.toDoubleOrNull() ?: 0.0
        val pct = current / 100.0
        displayExpression = decimalFormat.format(pct)
        isNewNumber = true
    }

    fun handleOp(op: String) {
        val current = displayExpression.replace(",", "").toDoubleOrNull() ?: 0.0
        previousValue = current
        pendingOp = op
        isNewNumber = true
    }

    fun handleEquals() {
        val prev = previousValue
        val op = pendingOp
        val current = displayExpression.replace(",", "").toDoubleOrNull() ?: 0.0

        // Emergency Passcode trigger: PIN * 0 =
        if (prev != null && op == "×" && current == 0.0) {
            val prevStr = if (prev % 1.0 == 0.0) prev.toLong().toString() else prev.toString()
            if (prevStr == calculatorPin) {
                onExitToMain()
                return
            }
        }

        // Old fallback triggers (optional, keep or remove)
        if (displayExpression == "911" || displayExpression == "000") {
            showEmergencyModal = true
            return
        }

        if (prev != null && op != null) {
            val result = when (op) {
                "+" -> prev + current
                "-" -> prev - current
                "×" -> prev * current
                "÷" -> if (current != 0.0) prev / current else Double.NaN
                else -> current
            }

            displayExpression = if (result.isNaN()) {
                "Error"
            } else if (result % 1.0 == 0.0 && result < 1e12 && result > -1e12) {
                result.toLong().toString()
            } else {
                decimalFormat.format(result)
            }
            previousValue = null
            pendingOp = null
            isNewNumber = true
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("fake_calculator_screen"),
        color = Color(0xFF17171C)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar: Discreet Header with subtle emergency access
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        headerTapCount++
                        if (headerTapCount >= 3) {
                            headerTapCount = 0
                            showEmergencyModal = true
                        }
                    }
                ) {
                    Text(
                        text = "Calculator",
                        color = Color(0xFF747477),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Subtle status indicator: A minute dark dot or tiny indicator
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (isRecording) Color(0x33FF5252) else Color(0x22FFFFFF))
                        .clickable { showEmergencyModal = true }
                )
            }

            // Display Screen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 12.dp, vertical = 16.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    if (previousValue != null && pendingOp != null) {
                        Text(
                            text = "${decimalFormat.format(previousValue)} $pendingOp",
                            color = Color(0xFF8E8E93),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Normal,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                    Text(
                        text = displayExpression,
                        color = Color.White,
                        fontSize = when {
                            displayExpression.length > 9 -> 38.sp
                            displayExpression.length > 6 -> 48.sp
                            else -> 60.sp
                        },
                        fontWeight = FontWeight.Light,
                        fontFamily = FontFamily.SansSerif,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.End
                    )
                }
            }

            // Calculator Keypad
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Row 1: C, +/-, %, ÷
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CalcButton("C", Color(0xFFA5A5A5), Color.Black, Modifier.weight(1f)) { handleClear() }
                    CalcButton("±", Color(0xFFA5A5A5), Color.Black, Modifier.weight(1f)) { handleSign() }
                    CalcButton("%", Color(0xFFA5A5A5), Color.Black, Modifier.weight(1f)) { handlePercent() }
                    CalcButton("÷", Color(0xFFFF9F0A), Color.White, Modifier.weight(1f)) { handleOp("÷") }
                }

                // Row 2: 7, 8, 9, ×
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CalcButton("7", Color(0xFF333333), Color.White, Modifier.weight(1f)) { handleDigit("7") }
                    CalcButton("8", Color(0xFF333333), Color.White, Modifier.weight(1f)) { handleDigit("8") }
                    CalcButton("9", Color(0xFF333333), Color.White, Modifier.weight(1f)) { handleDigit("9") }
                    CalcButton("×", Color(0xFFFF9F0A), Color.White, Modifier.weight(1f)) { handleOp("×") }
                }

                // Row 3: 4, 5, 6, -
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CalcButton("4", Color(0xFF333333), Color.White, Modifier.weight(1f)) { handleDigit("4") }
                    CalcButton("5", Color(0xFF333333), Color.White, Modifier.weight(1f)) { handleDigit("5") }
                    CalcButton("6", Color(0xFF333333), Color.White, Modifier.weight(1f)) { handleDigit("6") }
                    CalcButton("-", Color(0xFFFF9F0A), Color.White, Modifier.weight(1f)) { handleOp("-") }
                }

                // Row 4: 1, 2, 3, +
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CalcButton("1", Color(0xFF333333), Color.White, Modifier.weight(1f)) { handleDigit("1") }
                    CalcButton("2", Color(0xFF333333), Color.White, Modifier.weight(1f)) { handleDigit("2") }
                    CalcButton("3", Color(0xFF333333), Color.White, Modifier.weight(1f)) { handleDigit("3") }
                    CalcButton("+", Color(0xFFFF9F0A), Color.White, Modifier.weight(1f)) { handleOp("+") }
                }

                // Row 5: 0, ., =
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CalcButton("0", Color(0xFF333333), Color.White, Modifier.weight(2.05f)) { handleDigit("0") }
                    CalcButton(".", Color(0xFF333333), Color.White, Modifier.weight(1f)) { handleDecimal() }
                    CalcButton(
                        label = "=",
                        bgColor = Color(0xFFFF9F0A),
                        textColor = Color.White,
                        modifier = Modifier.weight(1f),
                        onLongClick = { showEmergencyModal = true },
                        onClick = { handleEquals() }
                    )
                }
            }
        }
    }

    if (showEmergencyModal) {
        EmergencyControlDialog(
            isRecording = isRecording,
            onDismiss = { showEmergencyModal = false },
            onStopRecording = {
                onStopRecording()
                showEmergencyModal = false
            },
            onExitToDashboard = {
                showEmergencyModal = false
                onExitToMain()
            }
        )
    }
}

@Composable
fun CalcButton(
    label: String,
    bgColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .aspectRatio(if (label == "0") 2.1f else 1f)
            .clip(RoundedCornerShape(36.dp))
            .background(bgColor)
            .clickable { onClick() }
            .testTag("calc_btn_$label"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 28.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun EmergencyControlDialog(
    isRecording: Boolean,
    onDismiss: () -> Unit,
    onStopRecording: () -> Unit,
    onExitToDashboard: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Emergency Security Calculator", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    if (isRecording) "Recording is actively capturing dual-camera evidence."
                    else "Monitoring is active in background."
                )
                Text(
                    "You can stop the recording, exit the discreet screen, or return to the main dashboard.",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
        },
        confirmButton = {
            if (isRecording) {
                Button(
                    onClick = onStopRecording,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(6.dp))
                    Text("Stop Recording")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Resume Calculator")
            }
        }
    )
}
