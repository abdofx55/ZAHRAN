# Modernization Plan for ZAHRAN Family Tree App — v2.0

The objective is to update and revive the **Zahran Family Tree app** so it can be re-published to the Google Play Store. The updates include upgrading Gradle/build tools, converting Java to Kotlin, implementing the MVI (Model-View-Intent) architecture pattern, replacing the local SQLite/Room database with Firebase Cloud Firestore, and adding Firebase Cloud Messaging (FCM) for remote push notifications.

---

## Upgrade Approach: Start from Scratch

We will start with a fresh project using modern Android tooling:

| Setting | Value |
| :--- | :--- |
| **Application ID** | `com.zahran` *(must stay the same for Play Store)* |
| **Version Code** | `4` *(must be higher than the last published `3`)* |
| **Version Name** | `2.0` |
| **Min SDK** | `24` (Android 7.0 — required by Jetpack Compose; covers 99%+ of active devices) |
| **Target SDK** | `35` (Android 15 — required by Google Play Store for new submissions) |
| **Build System** | Kotlin DSL (`.gradle.kts`) + Gradle Version Catalog (`libs.versions.toml`) |
| **IDE & Tooling** | Android Studio Narwhal 4 Feature Drop (2025.1.4) |
| **Android Gradle Plugin** | `8.11.0` *(optimized for Narwhal)* |
| **Gradle Version** | `8.11.1` *(fully compatible wrapper version)* |
| **Kotlin Version** | `2.1.21` *(native K2 compiler pairing for Narwhal)* |
| **UI Framework** | Jetpack Compose (Material 3) via Kotlin Compose Compiler Plugin `2.1.21` |
| **Architecture** | MVI using Kotlin Coroutines & `StateFlow` |
| **DI** | Dagger-Hilt |
| **Navigation** | Jetpack Navigation Compose 2.8+ (type-safe, `@Serializable`) |
| **Serialization** | Kotlinx Serialization *(no Gson, Moshi, or Jackson)* |
| **Push Notifications** | Firebase Cloud Messaging (FCM) |
| **Locale** | Arabic-first (RTL) |

> [!NOTE]
> WorkManager has been removed from the tooling list. FCM handles all notification delivery remotely. WorkManager can be re-introduced in the future if local background tasks (e.g., tree data pre-fetching) are needed.

---

## Improved Database Architecture (Migration from Names to IDs)

### The Problem with the Old Schema:
1. **Name Collisions**: Two cousins named "Ahmed Zahran" would have identical `father` strings, causing their children to be mixed together under the same node.
2. **Fragility**: Typos or Arabic spelling variations (`أ` / `ا` / `إ`) break all tree links below that node.
3. **Renaming is broken**: Correcting a typo in a father's name requires manually updating every child row's `father` field, or all links break.

### The Solution (ID-Based Hierarchy):
- Each person document gets a unique **`id`** (Firestore auto-generated document ID).
- Each person has a **`parentId`** pointing to their father's `id`.
- Root ancestors (generation 1) have `parentId = "root"`.
- Querying children is simple and fast: `collection("family").whereEqualTo("parentId", personId)`.

### Clarification on `isFamily` Field:
The old `isFamily` integer flag distinguished fully documented family branches (`1`) from individuals whose descendants are not fully recorded (`0`). We rename this to a **Boolean `isDocumented`** for clarity. Undocumented branches will render differently in the UI (e.g., greyed-out or with an icon).

#### Firestore Schema — `family` collection:
```json
{
  "id": "auto_generated_doc_id",
  "parentId": "father_doc_id_or_root",
  "name": "الاسم الأول",
  "fullName": "الاسم الكامل",
  "nickName": "اللقب (اختياري)",
  "gen": 3,
  "gender": 0,
  "isDocumented": true
}
```

#### Kotlin Domain Model (using Kotlinx Serialization):
```kotlin
@Serializable
data class Person(
    val id: String = "",
    val parentId: String = "root",
    val name: String = "",
    val fullName: String = "",
    val nickName: String = "",
    val gen: Int = 0,
    val gender: Int = 0,
    val isDocumented: Boolean = true
)
```

> [!NOTE]
> We use `@Serializable` from **Kotlinx Serialization** — not Gson, Moshi, or Jackson. This is a Kotlin-first, multiplatform-ready, compile-time safe serializer. Since the official Firebase Android SDK's reflection-based `toObject()` bypasses Kotlinx Serialization, we will implement custom extension functions to map Firestore's `Map<String, Any?>` to/from our `@Serializable` classes using `kotlinx.serialization` Json parsing. This guarantees compile-time safety and proper field resolution under R8/obfuscation.

---

## Firebase Dependencies: BoM (Bill of Materials)

We will use the **Firebase BoM** to manage all Firebase library versions in sync — no need to specify individual versions for each Firebase dependency:

```kotlin
// libs.versions.toml
[versions]
firebaseBom = "33.x.x"  // always use the latest BoM

[libraries]
firebase-bom        = { group = "com.google.firebase", name = "firebase-bom", version.ref = "firebaseBom" }
firebase-firestore  = { group = "com.google.firebase", name = "firebase-firestore-ktx" }
firebase-messaging  = { group = "com.google.firebase", name = "firebase-messaging-ktx" }

// app/build.gradle.kts
dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.messaging)
}
```

---

## Firestore Security Rules

> [!IMPORTANT]
> Without security rules, your Firestore database is publicly accessible for both reads **and** writes.

We will configure the following rules to protect the database:

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    // Family tree: anyone can read, no one can write from the app
    match /family/{personId} {
      allow read: true;
      allow write: false;
    }
  }
}
```

This means only you (via the Firebase Console or the migration script using Admin SDK credentials) can add or edit family members.

---

## Offline Support

The old app worked 100% offline (local SQLite). Firestore has a built-in **offline persistence cache**. We will enable it using the **current (non-deprecated) API**:

```kotlin
// In Application class, inside the Hilt module
val settings = firestoreSettings {
    setLocalCacheSettings(
        persistentCacheSettings {
            setSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
        }
    )
}
FirebaseFirestore.getInstance().firestoreSettings = settings
```

> [!NOTE]
> The old API `FirebaseFirestoreSettings.Builder().setPersistenceEnabled(true)` is **deprecated** in the current Firebase SDK. The `firestoreSettings {}` DSL with `persistentCacheSettings {}` is the replacement.

Once enabled:
- First launch with internet: data is fetched and cached locally.
- Subsequent opens without internet: cached data is served instantly.
- The app will show a subtle offline indicator if data is stale.

---

## Replace BoomMenu with Material 3 Design

We will **completely remove the BoomMenu library** and replace it with:

1. **`CenterAlignedTopAppBar`**: Displays the current ancestor name as the title, with a trailing 3-dot overflow menu icon.
2. **`DropdownMenu` / `DropdownMenuItem`**: Opens on 3-dot tap with items:
   - معلومات (Information)
   - حول التطبيق (About)
   - تقييم (Rate App)
   - مشاركة (Share App)
   - المزيد (More Apps)
   - المطور (Developer)
3. **Extended `FloatingActionButton`**: Shown on the main screen for "إضافة شخص" (Add Person) which redirects to Email/WhatsApp.

---

## Jetpack Navigation Compose — Type-Safe Routes

We will use **Navigation Compose 2.8+** with **type-safe, `@Serializable` destinations** instead of the old fragile string-based routes:

```kotlin
// Old (string-based — error-prone, no compile-time safety):
sealed class Screen(val route: String) {
    object Main : Screen("main")
}

// New (type-safe — compile-time checked, no magic strings):
@Serializable object MainScreen
@Serializable object InformationScreen
@Serializable object AboutScreen
@Serializable object AddPersonScreen
```

The `NavHost` will be set up in `MainActivity` referencing these objects directly. Passing arguments between screens is done via typed properties on the data class, not query strings.

---

## Complete Screens List

| Screen | Description |
| :--- | :--- |
| **Splash** | Handled by AndroidX Splash Screen API — no Activity needed |
| **Main / Tree Screen** | Navigable family tree list. Shows members of current generation under the selected ancestor. Supports back navigation. |
| **Information Screen** | Animated counter showing total number of family members. |
| **About Screen** | App version, developer info, and social links. Replaces `AboutActivity`. |
| **Add Person Screen** | Contact Developer screen with Email and WhatsApp buttons. |

---

## Recommended Tooling Setup

| Tool | Purpose |
| :--- | :--- |
| **Dagger-Hilt** | DI via `@HiltAndroidApp`, `@AndroidEntryPoint`, `@HiltViewModel` |
| **Kotlin Coroutines** | Non-blocking Firestore calls inside `viewModelScope` |
| **StateFlow** | MVI state exposed from ViewModel, collected via `collectAsStateWithLifecycle()` |
| **Kotlinx Serialization** | Kotlin-first JSON serialization for models and navigation args |
| **Gradle Version Catalog** | `libs.versions.toml` to centrally manage all dependency versions |
| **Firebase BoM** | Keeps all Firebase library versions in sync automatically |
| **Navigation Compose 2.8+** | Type-safe `@Serializable` screen routing (powered by Kotlinx Serialization) |

---

## UI State Handling & Custom Compose Views

Every screen that loads data from Firestore will use a shared `UiState<T>` **sealed interface** (preferred over `sealed class` in modern Kotlin — more composable, no constructor overhead):

```kotlin
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class  Success<T>(val data: T) : UiState<T>
    data class  Error(val message: String) : UiState<Nothing>
    data object Empty : UiState<Nothing>
}
```

> [!NOTE]
> `data object` (Kotlin 1.9+) is used for `Loading` and `Empty` instead of plain `object` to get correct `toString()`, `equals()`, and `hashCode()` behaviour automatically.

The following composables will be shared across all screens:

1. **`LoadingView`**: Full-screen centered `CircularProgressIndicator` with a fade-in animation.
2. **`ErrorView`**: Error icon + message + **"Try Again"** button that re-fires the MVI intent.
3. **`EmptyView`**: Illustrative icon + friendly Arabic message (e.g., "لا يوجد أفراد مسجلون في هذا الفرع").

---

## RTL & Arabic Language Support

> [!IMPORTANT]
> The app is Arabic-first. The following must be configured:

- `android:supportsRtl="true"` in `AndroidManifest.xml`.
- `CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl)` at the app root in `MainActivity`.
- All string resources in `res/values/strings.xml` will use Arabic by default.
- The app will use the **Cairo** or **Tajawal** Arabic Google Font for a clean, modern look.

---

## Release Build: R8 Minification

> [!IMPORTANT]
> The old project had `minifyEnabled false`. This results in a large APK that takes more time to install.

For the new project, we will enable **R8** (Google's code shrinker/optimizer):
```kotlin
buildTypes {
    release {
        isMinifyEnabled = true
        isShrinkResources = true
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
    }
}
```
This will significantly reduce the final APK/AAB size for Play Store submission.

---

## Firebase Cloud Messaging (FCM) Push Notifications

### Old Approach vs. New Approach

| | Old (`firebase-jobdispatcher`) | New (FCM) |
| :--- | :--- | :--- |
| **How it works** | Device schedules a local notification on a timer | You send a push from Firebase Console to all users remotely |
| **Requires internet** | No (runs locally) | Yes (to receive the push) |
| **You control timing** | No (fixed interval on device) | Yes (send any time you want) |
| **Works when app is killed** | Partially | Yes, always |
| **Target specific users** | No | Yes (by topic, device, or group) |

### How It Will Work

All users will be automatically subscribed to the **`zahran_family`** topic on first launch. This means:
- You open the **Firebase Console → Cloud Messaging** section.
- You write a notification title and body (e.g., "تم إضافة أفراد جدد للشجرة!").
- You select **Topic → `zahran_family`**.
- You press **Send** — all users receive the push instantly, even if the app is closed.

### Two Scenarios Handled
1. **App in background / killed**: Android displays the notification automatically via FCM. No extra code needed.
2. **App in foreground**: FCM calls `onMessageReceived()` in our `ZahranFirebaseMessagingService` and we manually build and show the notification.

### Is FCM Free?
Yes. FCM is completely free with **no message limits** on the Firebase Spark (free) plan.

---

## Git Workflow for GitHub (v2 Migration)

Using the same repository: https://github.com/abdofx55/ZAHRAN

1. **Branch**: Create a `v2-modernization` branch locally (`git checkout -b v2-modernization`).
2. **Clean**: Remove old project files while preserving `.git` and `.gitignore`.
3. **Build**: Initialize the new modern project template inside the same directory.
4. **Commit & Push**: Push the new code to the `v2-modernization` branch.
5. **Merge**: Once built and tested, merge into `main`/`master` and push.

---

## Proposed Changes (Phases)

### Phase 1: Git Branching & Project Initialization
- Create `v2-modernization` Git branch.
- Clear legacy project files (keep `.git` only).
- Initialize fresh project with Kotlin DSL, Version Catalog, Hilt, Compose, Navigation Compose 2.8+.
- Add `org.jetbrains.kotlin.plugin.serialization` Gradle plugin and `kotlinx-serialization-json` dependency.
- Add Firebase BoM (Firestore + FCM), configure `google-services.json`.
- Set up RTL support and Arabic Google Fonts.

### Phase 2: Database Migration Script (Python)
- Write a **local Python script** using the **Firebase Admin SDK** that:
  1. Opens the old `zahran_family_tree.db` SQLite file (which contains exactly 660 family members).
  2. Reads all rows from the `family` table.
  3. Resolves parent-child relationships using name-string matching combined with whitespace normalization (replacing all multiple spaces with a single space and stripping leading/trailing whitespace).
  4. Explicitly links Generation 2 records whose father is `"زهران"` to the single Root ancestor record `'زهران عمر'` (ID 1).
  5. Assigns stable unique IDs and maps `parentId` values (verified: this exact normalization logic achieves a 100% resolution rate with zero orphans and zero name collisions across all 660 records).
  6. Uploads all records to the Firestore `family` collection via the Admin SDK.
- Configure Firestore Security Rules after migration is complete.

### Phase 3: Core Architecture (MVI) & Data Layer
- Define `Person` domain model annotated with `@Serializable` (Kotlinx Serialization).
- Implement `PersonRepository` with Firestore queries + offline persistence (new `firestoreSettings {}` DSL).
- Build Firestore ↔ Kotlin mapping using Kotlinx Serialization (no Gson).
- Define `UiState<T>` sealed interface with `data object` states.
- Implement `MainViewModel` with MVI intents, `StateFlow`, and Hilt injection.

### Phase 4: UI (Jetpack Compose)
- Implement AndroidX Splash Screen API in `MainActivity`.
- Build shared `LoadingView`, `ErrorView`, `EmptyView` composables.
- Set up `NavHost` with type-safe `@Serializable` destinations.
- Build all 4 screens: Main/Tree, Information, About, Add Person/Contact.
- Apply RTL layout direction and Arabic typography.
- Enable R8 for release builds.

### Phase 5: Firebase Cloud Messaging (FCM)
- Add `firebase-messaging-ktx` via Firebase BoM.
- Implement `ZahranFirebaseMessagingService` extending `FirebaseMessagingService`:
  - Override `onMessageReceived()` to display a foreground notification.
  - Override `onNewToken()` to handle FCM token refresh.
- Subscribe to topic `zahran_family` on app startup.
- Create notification channel with `IMPORTANCE_HIGH` for Android 8+.
- Use `PendingIntent.FLAG_IMMUTABLE` for all intents (required for Android 12+).

---

## Verification Plan

### Automated Tests
- Unit tests for MVI state transitions in `MainViewModel`.
- Unit test the Python migration script's name-to-ID mapping logic.

### Manual Verification
- Run the Python migration script and verify records appear correctly in Firestore.
- Verify Firestore Security Rules block unauthorized writes.
- Disable internet and verify the tree loads from offline cache.
- Verify RTL layout direction renders correctly on all screens.
- Verify the splash screen renders and transitions smoothly.
- Test full tree traversal (navigate down generations and back).
- Test Email and WhatsApp contact redirects from Add Person screen.
- Send a test FCM push from Firebase Console to the `zahran_family` topic; verify receipt in both foreground and background on a real device or emulator.
- Run a Release build and verify APK/AAB size reduction vs. the old build.
