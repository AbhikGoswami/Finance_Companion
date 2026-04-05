# 💰 Finance Companion
**Finance Companion** is a robust, personal expense tracker built for Android. It combines the speed of local storage with the reliability of Google Cloud, ensuring your financial data is always secure, synced, and accessible.

---

## 🌟 Project Overview
Managing money shouldn't be a chore. **Finance Companion** was built to provide a seamless, modern interface for tracking income and expenses. By leveraging **Jetpack Compose**, the app offers a highly responsive UI, while **Firebase** handles the heavy lifting of authentication and cloud backups.

Whether you're tracking your monthly budget or maintaining a "No-Spend" streak, this app provides the insights you need to take control of your financial health.

---

## 🚀 Key Features

* **📊 Dynamic Dashboard:** Real-time calculation of balance, income, and expenses with a visual monthly budget progress bar.
* **🏠 Offline-First Experience:** Powered by **Room SQLite Database**, the app stores all your data locally first. Log expenses anytime, anywhere, even without an internet connection.
* **🔒 Biometric Security:** Keep your financial data private with integrated Fingerprint/Face Unlock.
* **☁️ Cloud Sync & Backup:** One-tap backup to **Firebase Firestore** so you never lose your data.
* **📅 Monthly Insights:** Filter transactions by month to analyze spending patterns over time.
* **📤 CSV Export:** Export your local transaction history to a `.csv` file for external accounting or Excel.
* **🇮🇳 Localized Formatting:** Fully optimized for the Indian market with **₹ (Rupee)** formatting and Indian numbering systems.

---

## 🎨 UI/UX Design (Visual Components)

The app features a modern **Material 3** aesthetic with several custom-built visual components:

* **📈 Budget Progress Indicators:** A color-coded `LinearProgressIndicator` that shifts from Green to Red as you approach your monthly budget limit.
* **💳 Elevation & Glassmorphism:** Custom `Card` components using primary and tertiary containers to create a clear visual hierarchy of your financial data.
* **🗓 Interactive Month Selector:** A smooth `LazyRow` based month-picker for quick navigation through your financial history.
* **✨ Smooth Transitions:** Uses Jetpack Compose **Navigation** and **State** to ensure zero-latency transitions between screens.
* **🖼 Custom Backgrounds:** Integrated high-quality image assets and painters for a premium app feel.
  
---

## 🛠 Tech Stack

| Component | Technology |
| :--- | :--- |
| **Language** | Kotlin |
| **UI Framework** | Jetpack Compose (Material 3) |
| **Architecture** | MVVM (Model-View-ViewModel) |
| **Local Database** | Room Persistence Library |
| **Backend/Cloud** | Firebase (Auth & Firestore) |
| **Asynchronous** | Kotlin Coroutines & Flow |

---

## ⚙️ Setup & Installation

### 1. Prerequisites
* Android Studio (Ladybug or newer)
* A Firebase Project

### 2. Firebase Configuration
1. Go to the [Firebase Console](https://console.firebase.google.com/).
2. Add a new Android App with the package name `com.abhik.financecompanion`.
3. Download the `google-services.json` and place it in the `app/` directory of the project.
4. **Enable Firestore:** Use Native Mode and the ID `(default)`.
5. **Enable Authentication:** Turn on **Google Sign-In**.

---

## 📝 Assumptions

* **Currency:** Hardcoded to `en-IN` locale for **Rupee (₹)** formatting.
* **Biometrics:** Assumes hardware-backed biometric sensors are available.
* **Internet:** Required for **Google Sign-In** and **Cloud Backup**.

---

**Developed with ❤️ by Abhik**
