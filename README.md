# 🏪 Local Business Helper

A production-ready Android app for local business owners to manage customers, orders, invoices, and follow-ups — offline-first with optional Google Sheets sync.

---

## ✨ Features

| Feature | Description |
|---|---|
| **Dashboard** | Live stats — total orders, pending, due payments, today's follow-ups |
| **Customer Management** | Add, search, view history, call directly from the app |
| **Order Management** | Create, update status (Pending → Delivered), track per-customer |
| **Invoices & Payments** | Paid / Unpaid / Partial tracking, filter by payment status |
| **Follow-up Reminders** | Local notifications for follow-up dates, no external API needed |
| **Google Sheets Sync** | Pull data from a Google Sheet automatically every 6 hours |
| **Offline-First** | Room database — works without internet, syncs when connected |

---

## 🏗️ Architecture

```
MVVM + Repository Pattern
├── UI Layer        → Fragments + ViewBinding
├── ViewModel       → LiveData + Coroutines
├── Repository      → Single source of truth
├── Room Database   → Offline storage (Customer, Order)
├── WorkManager     → Background Sheets sync
└── Retrofit        → Google Sheets API v4
```

---

## 🚀 Build Without Android Studio (GitHub Actions)

### Prerequisites
- A GitHub account
- Java 17 (for local builds only)

### Build via GitHub Actions

1. **Fork / push this project to GitHub**

2. **Generate a signing keystore** (run once locally):
   ```bash
   chmod +x scripts/generate_keystore.sh
   ./scripts/generate_keystore.sh
   ```

3. **Add GitHub Secrets** (Settings → Secrets → Actions):

   | Secret Name | Value |
   |---|---|
   | `KEYSTORE_BASE64` | Base64-encoded keystore (printed by script) |
   | `KEYSTORE_PASSWORD` | Your keystore store password |
   | `KEY_ALIAS` | `local-business-helper` (or your alias) |
   | `KEY_PASSWORD` | Your key password |

4. **Push to `main` branch** → GitHub Actions automatically:
   - Builds the signed release APK
   - Uploads it as a workflow artifact
   - Creates a GitHub Release if you push a `v*` tag

5. **Download your APK** from:
   - GitHub → Actions → Latest workflow run → Artifacts
   - Or GitHub → Releases (for tagged versions)

### Create a Release

```bash
git tag v1.0.0
git push origin v1.0.0
```
→ GitHub Actions will build and create a Release with the APK attached.

---

## 📱 Google Sheets Integration Setup

### Sheet Structure (Row 2 onwards)

| Column | Field | Example |
|---|---|---|
| A | Customer Name | Rahul Sharma |
| B | Phone | 9876543210 |
| C | Product | Cotton Saree |
| D | Quantity | 3 |
| E | Price | 1500 |
| F | Order Date | 01/04/2025 |
| G | Delivery Date | 15/04/2025 |
| H | Payment Status | Paid / Unpaid / Partial |
| I | Follow-up Date | 10/04/2025 |

### Enable the API

1. Go to [Google Cloud Console](https://console.cloud.google.com)
2. Create a project → Enable **Google Sheets API**
3. Create an **API Key** (restrict to Sheets API only)
4. Copy your **Spreadsheet ID** from the Sheet URL:
   `https://docs.google.com/spreadsheets/d/`**`SPREADSHEET_ID`**`/edit`

### Configure in App

Open the app → ⚙️ Settings tab → Enter Spreadsheet ID, API Key, Range → Tap **Test Connection**

---

## 🗂️ Project Structure

```
app/src/main/
├── java/com/localbusiness/helper/
│   ├── data/
│   │   ├── local/
│   │   │   ├── dao/         ← CustomerDao, OrderDao
│   │   │   ├── entity/      ← Customer, Order, Enums
│   │   │   └── AppDatabase  ← Room setup
│   │   ├── remote/          ← SheetsApiService, NetworkClient
│   │   └── repository/      ← BusinessRepository
│   ├── ui/
│   │   ├── dashboard/       ← DashboardFragment + ViewModel
│   │   ├── customers/       ← List, Add, Detail fragments
│   │   ├── orders/          ← List, Add, Detail fragments
│   │   ├── invoices/        ← Invoice list + payment filter
│   │   ├── followup/        ← Today + upcoming follow-ups
│   │   └── settings/        ← Sheets API configuration
│   ├── utils/               ← DateUtils, NotificationReceiver
│   ├── workers/             ← SheetsSyncWorker (WorkManager)
│   └── LocalBusinessApp.kt  ← Application class
└── res/
    ├── layout/              ← All XML layouts
    ├── navigation/          ← nav_graph.xml
    ├── drawable/            ← Vector icons
    └── values/              ← Colors, strings, themes
```

---

## 🔐 Permissions Used

| Permission | Why |
|---|---|
| `INTERNET` | Google Sheets sync |
| `ACCESS_NETWORK_STATE` | Check connectivity before sync |
| `RECEIVE_BOOT_COMPLETED` | Re-schedule alarms after reboot |
| `POST_NOTIFICATIONS` | Follow-up reminder notifications |
| `SCHEDULE_EXACT_ALARM` | Precise follow-up timing |

---

## 📦 Dependencies

- **Room 2.6** — Local SQLite database
- **WorkManager 2.9** — Background Sheets sync
- **Retrofit 2.9 + OkHttp 4.12** — Google Sheets API
- **Navigation Component 2.7** — Fragment navigation
- **Material Components 1.11** — Modern UI
- **ThreeTenABP** — Date/time parsing
- **Coroutines + LiveData** — Reactive UI updates

---

## 🛡️ Play Store Readiness

- ✅ Signed APK with release keystore
- ✅ ProGuard / R8 minification enabled
- ✅ Resource shrinking enabled
- ✅ `targetSdk 34` (Android 14)
- ✅ `minSdk 24` (Android 7.0 — covers 95%+ devices)
- ✅ Adaptive launcher icon
- ✅ No test runner in release
- ✅ Offline-first architecture
- ✅ Minimal permissions

---

## 📋 License

MIT License — free to use for personal or commercial projects.
