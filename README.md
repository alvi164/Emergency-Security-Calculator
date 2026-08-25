# ES Calculator — Emergency Security Calculator

A discreet, user-triggered emergency personal safety recorder for Android, camouflaged as a fully functional calculator.

**Developed by: [Syad Mehedi Hasan Alvi](https://github.com/alvi164)**  
**Location: Dhaka, Bangladesh**  
**Email: edu.quantumcuriosity@gmail.com**

---

## 🛡️ User Guide

### 1. Initial Setup
- **App Name**: The app appears on your device as **ES Calculator**.
- **Default PIN**: Upon installation, the default unlock PIN is `1234`.

### 2. Accessing the Dashboard (Unlock)
To access your evidence vault and settings:
1. Open **ES Calculator**.
2. Enter your 4-digit PIN (default `1234`).
3. Press the **multiplication (×)** button.
4. Enter **0**.
5. Press the **equals (=)** button.
   - *Example: `1234 × 0 =`*
6. The main dashboard will open.

### 3. Emergency Recording
- **Trigger**: The app monitors for emergency situations in the background. If you triple-shake your phone, it will automatically start recording evidence.
- **Recording Mode**: **Dual-Camera** capture. Both the **Front Camera** and **Back Camera** capture video and audio headlessly and simultaneously.
- **Discreet View**: During recording, the app shows the Calculator interface (or a Black Screen, depending on settings) to remain stealthy.
- **Notification**: A discreet notification titled **Calculating** with a **Green Dot** icon appears in the status bar while recording.

### 4. Security & Privacy
- **Auto-Lock**: The dashboard automatically locks itself if you switch apps, go to the home screen, or lock your phone. You must re-enter your PIN to return.
- **Exceptions**: You can freely view video previews and maps; the app will not lock when returning from these views.
- **Terminate Button**: The "Terminate" button in the recording notification is hidden when the screen is locked to prevent unauthorized stopping.

### 5. Settings
Inside the dashboard, you can:
- **Change PIN**: Update the default `1234` code to your personal security PIN.
- **Sensitivity**: Adjust the triple-shake detection threshold.
- **Camouflage**: Choose between the Calculator or a Stealth Black Screen.

---

## ⚖️ Legal Disclaimer
This application is for personal safety use only. Users are responsible for complying with local laws regarding audio and video recording.

---

## 📦 Installation
To install the latest version:
1.  Download the `app-debug.apk` from the latest build.
2.  Transfer the file to your Android device.
3.  Enable **"Install from Unknown Sources"** in your device settings.
4.  Open the APK and follow the installation prompts.

---

## 👨‍💻 About the Developer
**Syad Mehedi Hasan Alvi** is a computer science student and developer focused on software, AI, and IoT projects based in **Dhaka, Bangladesh**. 

More of his work is available at:
- **GitHub**: [github.com/alvi164](https://github.com/alvi164)
- **Devpost**: [devpost.com/salvi222164](https://devpost.com/salvi222164)
- **Email**: edu.quantumcuriosity@gmail.com

---

## 🚀 Build Instructions
- **Minimum SDK**: 31 (Android 12)
- **Target SDK**: 36
- **Gradle Task**: `./gradlew assembleDebug`
