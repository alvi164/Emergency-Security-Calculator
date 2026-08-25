package com.example

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.models.FakeScreenMode
import com.example.data.preferences.SafetyPreferences
import com.example.data.repository.EvidenceRepository
import com.example.service.EmergencyRecorderService
import com.example.ui.components.PrivacyPolicyDialog
import com.example.ui.components.RationalePermissionDialog
import com.example.ui.fake.FakeScreenActivity
import com.example.ui.screens.EvidenceHistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.EmergencyRecorderTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var preferences: SafetyPreferences
    private lateinit var repository: EvidenceRepository

    companion object {
        var isLaunchingExternalActivity = false
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        preferences = SafetyPreferences(this)
        repository = EvidenceRepository.getInstance(this)

        setContent {
            EmergencyRecorderTheme {
                MainAppScreen(
                    preferences = preferences,
                    repository = repository
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        isLaunchingExternalActivity = false
    }

    override fun onStop() {
        super.onStop()
        // Only finish if we aren't launching something like the video player or maps
        if (!isLaunchingExternalActivity) {
            finish()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    preferences: SafetyPreferences,
    repository: EvidenceRepository
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTab by remember { mutableIntStateOf(0) }

    // Service State
    val isMonitoring by EmergencyRecorderService.isMonitoring.collectAsState()
    val isRecording by EmergencyRecorderService.isRecording.collectAsState()
    val durationSeconds by EmergencyRecorderService.currentDurationSeconds.collectAsState()

    // Preferences State
    val shakeThreshold by preferences.shakeThresholdFlow.collectAsState(initial = 2.5f)
    val fakeScreenMode by preferences.fakeScreenModeFlow.collectAsState(initial = FakeScreenMode.CALCULATOR)
    val autoUpload by preferences.autoUploadFlow.collectAsState(initial = true)
    val recordAudio by preferences.recordAudioFlow.collectAsState(initial = true)
    val userId by preferences.userIdFlow.collectAsState(initial = "user_default")
    val hapticFeedback by preferences.hapticFeedbackFlow.collectAsState(initial = true)
    val calculatorPin by preferences.calculatorPinFlow.collectAsState(initial = "1234")
    val disclaimerAccepted by preferences.disclaimerAcceptedFlow.collectAsState(initial = true)

    // Evidence Sessions State
    val evidenceSessions by repository.sessions.collectAsState()

    // Permission Handling
    var showPermissionRationale by remember { mutableStateOf(false) }
    var showPrivacyPolicyModal by remember { mutableStateOf(false) }
    var showFirstRunDisclaimer by remember { mutableStateOf(false) }

    fun checkMissingPermissions(): List<String> {
        val permissions = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        return permissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val allGranted = result.values.all { it }
        if (allGranted) {
            EmergencyRecorderService.startService(context)
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Background emergency shake monitoring activated.")
            }
        } else {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Some permissions were denied. Emergency recording may be limited.")
            }
        }
    }

    LaunchedEffect(Unit) {
        val missing = checkMissingPermissions()
        if (missing.isNotEmpty()) {
            showPermissionRationale = true
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = when (selectedTab) {
                            0 -> "Emergency Security Calculator"
                            1 -> "ES Calculator Vault"
                            else -> "Safety Settings"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .testTag("bottom_nav_bar"),
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            if (selectedTab == 0) Icons.Default.Security else Icons.Outlined.Security,
                            contentDescription = "Safety Hub"
                        )
                    },
                    label = { Text("Safety Hub", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_safety_hub")
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            if (selectedTab == 1) Icons.Default.FolderOpen else Icons.Outlined.Folder,
                            contentDescription = "Evidence"
                        )
                    },
                    label = { Text("Evidence", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_evidence")
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(
                            if (selectedTab == 2) Icons.Default.Settings else Icons.Outlined.Settings,
                            contentDescription = "Settings"
                        )
                    },
                    label = { Text("Settings", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_settings")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> HomeScreen(
                    isMonitoring = isMonitoring,
                    isRecording = isRecording,
                    durationSeconds = durationSeconds,
                    shakeThreshold = shakeThreshold,
                    fakeScreenMode = fakeScreenMode,
                    onToggleMonitoring = { enabled ->
                        if (enabled) {
                            val missing = checkMissingPermissions()
                            if (missing.isNotEmpty()) {
                                showPermissionRationale = true
                            } else {
                                EmergencyRecorderService.startService(context)
                            }
                        } else {
                            EmergencyRecorderService.stopService(context)
                        }
                    },
                    onTriggerPanicRecord = {
                        val missing = checkMissingPermissions()
                        if (missing.isNotEmpty()) {
                            showPermissionRationale = true
                        } else {
                            EmergencyRecorderService.triggerRecording(context)
                        }
                    },
                    onStopRecording = {
                        EmergencyRecorderService.stopRecording(context)
                    },
                    onLaunchDiscreetScreen = {
                        val intent = Intent(context, FakeScreenActivity::class.java).apply {
                            putExtra(FakeScreenActivity.EXTRA_MODE, fakeScreenMode.name)
                        }
                        context.startActivity(intent)
                    }
                )

                1 -> EvidenceHistoryScreen(
                    sessions = evidenceSessions,
                    onRetryUpload = { sessionId ->
                        repository.enqueueUpload(sessionId)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Upload queued for evidence session.")
                        }
                    },
                    onDeleteSession = { sessionId ->
                        repository.deleteSession(sessionId)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Evidence record deleted.")
                        }
                    }
                )

                2 -> SettingsScreen(
                    shakeThreshold = shakeThreshold,
                    fakeScreenMode = fakeScreenMode,
                    autoUpload = autoUpload,
                    recordAudio = recordAudio,
                    userId = userId,
                    hapticFeedback = hapticFeedback,
                    calculatorPin = calculatorPin,
                    onUpdateShakeThreshold = {
                        coroutineScope.launch { preferences.setShakeThreshold(it) }
                    },
                    onUpdateFakeScreenMode = {
                        coroutineScope.launch { preferences.setFakeScreenMode(it) }
                    },
                    onUpdateAutoUpload = {
                        coroutineScope.launch { preferences.setAutoUpload(it) }
                    },
                    onUpdateRecordAudio = {
                        coroutineScope.launch { preferences.setRecordAudio(it) }
                    },
                    onUpdateUserId = {
                        coroutineScope.launch { preferences.setUserId(it) }
                    },
                    onUpdateHapticFeedback = {
                        coroutineScope.launch { preferences.setHapticFeedback(it) }
                    },
                    onUpdateCalculatorPin = {
                        coroutineScope.launch { preferences.setCalculatorPin(it) }
                    },
                    onShowPrivacyPolicy = { showPrivacyPolicyModal = true }
                )
            }
        }
    }

    // Permission Rationale Dialog
    if (showPermissionRationale) {
        RationalePermissionDialog(
            onConfirm = {
                showPermissionRationale = false
                val missing = checkMissingPermissions()
                if (missing.isNotEmpty()) {
                    permissionLauncher.launch(missing.toTypedArray())
                }
            },
            onDismiss = {
                showPermissionRationale = false
            }
        )
    }

    // Privacy Policy Dialog
    if (showPrivacyPolicyModal) {
        PrivacyPolicyDialog(
            isFirstLaunch = false,
            onAccept = { showPrivacyPolicyModal = false },
            onDismiss = { showPrivacyPolicyModal = false }
        )
    }
}
