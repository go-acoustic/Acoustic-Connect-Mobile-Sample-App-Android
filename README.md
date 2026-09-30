# Acoustic Connect Sample App — Android

Two sample apps for the Acoustic Connect Android SDK, in one Gradle project, sharing one copy of
their non-UI code.

| Module | UI toolkit | Covers | SDK artifact |
|---|---|---|---|
| `sample-compose` | Jetpack Compose | Push, identity, behaviour showcase | `connect-push` |
| `sample-xml` | Views / XML layouts | Identity, behaviour showcase — **no push** | `connect` |

The second app exists to cover what actually differs between the two UI toolkits: screen-view
emission, control capture and masking all take different routes through the SDK. Push does not —
it is independent of the UI layer — so duplicating the certificates, provider registrations and
config files it needs would add maintenance without adding coverage. `sample-xml` is therefore
analytics-only and needs no Firebase or AppGallery account to build and run.

Everything that is not UI — credential loading, the signal log, app-state instrumentation, the
identity view model, the swipe classifier, the Showcase's SDK calls and the card copy — lives in
`shared/` and is compiled into both apps. Read either app to learn the integration; read both to
see what changes with the UI toolkit.

Both apps follow the same layout as the Acoustic Connect React Native and iOS samples, down to
their element ids and logged screen names, so one set of UI tests drives all of them:

| Tab | Id | Screens |
|---|---|---|
| Push (Compose only) | `tab_notification` | Notification authorization |
| Identity | `tab_identity` | Log a `loggedIn` or `accountRegistered` identity signal; recent identifiers |
| Behaviour | `tab_behaviour` | A hub that opens the **Showcase**: one card per capture feature — screen views, taps, gestures, text entry and masking, custom events, signals, exceptions, dialogs, session replay of modals, app state, and runtime capture control |

Every element a test can address carries the same id in both apps: a Compose `testTag`, published
as a resource id with `testTagsAsResourceId`, or an XML `android:id`. The full list is
`shared/.../contract/SampleId.kt`; the logged screen names are in `ScreenName.kt`.

Use this alongside the Integration Guide to see a working implementation of push registration,
notification handling, and identity logging across both FCM (Firebase) and HMS (Huawei) providers.

---

## What's included

| Feature | Description |
|---|---|
| Push registration | Automatic provider detection (`strictProvider = null`) supporting both FCM and HMS |
| Notification authorization | Request and display push permission status (Android 13+) |
| Analytics capture | Enabled by default — events, screenshots, and screen visits out of the box |
| Identity logging | Log `loggedIn` / `accountRegistered` identity signals and view recent history (last 5 entries) |
| Behaviour showcase | One card per analytics feature, each showing the SDK call it makes and what it sent |
| Dual provider support | FCM via Firebase, HMS via Huawei AppGallery Connect |

---

## SDK dependencies — which artifact to use

The Connect SDK is published to Maven Central under `io.github.go-acoustic` as four separate artifacts. Pick the one that matches your push requirements — the push artifacts pull `connect` (and the matching vendor SDK) transitively, so you never need to declare `connect` yourself when using a push variant.

> **Beta versions** (`<version>` ending in `-beta`) are not on Maven Central. They are served from the
> [go-acoustic/Android_Maven](https://github.com/go-acoustic/Android_Maven) repository, which Gradle
> reads as a plain Maven repository — add it after `mavenCentral()` in
> `dependencyResolutionManagement.repositories` (this sample already does):
>
> ```kotlin
> maven {
>     url = uri("https://raw.githubusercontent.com/go-acoustic/Android_Maven/master")
>     content { includeGroup("io.github.go-acoustic") }
> }
> ```
>
> Release versions resolve from Maven Central alone; the extra repository can then be removed.

```kotlin
// No push at all — only core analytics / events
implementation("io.github.go-acoustic:connect:<version>")

// FCM only (Google Play Store builds)
implementation("io.github.go-acoustic:connect-push-fcm:<version>")

// HMS only (Huawei AppGallery builds)
implementation("io.github.go-acoustic:connect-push-hms:<version>")

// Both providers (single APK that auto-detects FCM or HMS at runtime)
implementation("io.github.go-acoustic:connect-push:<version>")
```

| Artifact | Pulls in | Use when |
|---|---|---|
| `connect` | Core SDK only | Analytics / events only, no push |
| `connect-push-fcm` | `connect` + Firebase Messaging | Google Play build, FCM push |
| `connect-push-hms` | `connect` + HMS Push Kit | Huawei AppGallery build, HMS push |
| `connect-push` | `connect-push-fcm` + `connect-push-hms` | Single APK supporting both providers |

---

## Getting started

For the complete step-by-step walkthrough — including Firebase project setup, FCM configuration, Huawei AppGallery Connect setup, HMS Push Kit enablement, SHA-256 fingerprint registration, and troubleshooting — see the Integration Guide.

The quick-start steps below assume you have completed the prerequisites in the guide.

### 1. Clone the repository

```bash
git clone https://github.com/go-acoustic/Acoustic-Connect-Mobile-Sample-App-Android.git
cd Acoustic-Connect-Mobile-Sample-App-Android
```

### 2. Open in Android Studio

Open the project root in Android Studio. Gradle will sync and resolve all dependencies automatically.

### 3. Add push provider configuration files

Only `sample-compose` needs these — `sample-xml` builds and runs without them. Place your
provider config files in the `sample-compose/` directory before building:

| File | Provider | Where to obtain |
|---|---|---|
| `google-services.json` | FCM (Firebase) | Firebase Console → Project settings → Your apps |
| `agconnect-services.json` | HMS (Huawei) | AppGallery Connect → My apps → Download config |

> Both files are excluded from version control. The project ships with a placeholder `agconnect-services.json` — replace it with your own.

### 4. Configure your credentials

Both apps read their Acoustic **app key** and **collector URL** from a single asset file,
`shared/src/main/assets/ConnectBasicConfig.properties`. It lives in the shared module, so editing
it once configures both apps. This is the only file you need to edit — every code path resolves
credentials from it:

| Code path | When it runs | How it reads the asset |
|---|---|---|
| Foreground init | User opens the app — `MainActivity` calls `ConnectComposeUI.ConnectWrapper(...)` | `AcousticCredentials.load(context)` parses the asset and passes `appKey` / `postMessageURL` into the wrapper |
| Background init | FCM/HMS delivers a push while the app is not running — SDK bootstraps to log `pushReceived` before `MainActivity` runs | The Connect SDK loads `ConnectBasicConfig.properties` directly (`Background bootstrap: no persisted credentials, using bundled config`) |

Open `shared/src/main/assets/ConnectBasicConfig.properties` and set:

```properties
# your Acoustic collector URL
PostMessageUrl=YOUR_COLLECTOR_URL
# your Acoustic app key
AppKey=YOUR_APP_KEY
```

`AcousticCredentials.kt` is a thin loader — you do not need to edit it:

```kotlin
object AcousticCredentials {
    fun load(context: Context): Credentials { /* reads ConnectBasicConfig.properties */ }
    data class Credentials(val appKey: String, val collectorUrl: String)
}
```

`MainActivity.kt` calls the loader once in `onCreate`, then hands the values to `ConnectComposeUI.ConnectWrapper` so the foreground SDK init uses `AppKey` / `PostMessageUrl`:

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val credentials = AcousticCredentials.load(this)

    setContent {
        ConnectApplicationTheme {
            val navController = rememberNavController()

            ConnectComposeUI.ConnectWrapper(
                navController = navController,
                appKey = credentials.appKey,            // from ConnectBasicConfig.properties
                postMessageURL = credentials.collectorUrl, // from ConnectBasicConfig.properties
                pushConfig = ConnectPushConfig(/* ... */),
            ) {
                MainScreen(navController = navController, notificationViewModel = viewModel)
            }
        }
    }
}
```

`load(this)` is invoked **before** `setContent` so the values are resolved on the main thread once per Activity creation, rather than on every recomposition. The `Credentials` instance is then passed to `ConnectWrapper` exactly as you would with hardcoded constants — but the source of truth stays in the asset.

### 5. Register SHA-256 fingerprint (HMS only)

HMS Push Kit requires your signing certificate fingerprint to be registered in AppGallery Connect.

Get the debug fingerprint:

```bash
keytool -list -v \
  -keystore ~/.android/debug.keystore \
  -alias androiddebugkey \
  -storepass android \
  -keypass android
```

Copy the **SHA-256** value and add it in AppGallery Connect → My apps → General information → SHA-256 certificate fingerprint. Then re-download `agconnect-services.json` and replace the file in `sample-compose/`.

### 6. Build and run

Select the `sample-compose` or `sample-xml` run configuration and run on a device or emulator, or
from the command line:

```bash
./gradlew :sample-compose:assembleDebug     # push + analytics
./gradlew :sample-xml:assembleDebug         # analytics only, no provider config needed
```

The two apps have different application ids, so both can be installed side by side.

- **FCM** — works on any device with Google Play Services
- **HMS** — works on Huawei devices or emulators with HMS Core

---

## Project structure

```
settings.gradle.kts                   # :shared, :sample-compose, :sample-xml
gradle/libs.versions.toml             # one version catalog; connectSdk pins every Acoustic artifact

shared/                               # UI-free, compiled into BOTH apps
  src/main/assets/
    ConnectBasicConfig.properties     # AppKey + PostMessageUrl — single source of truth
    ConnectAdvancedConfig.json        # SDK feature switches
  src/main/res/values/strings_behaviour.xml  # Card copy both apps display
  src/main/java/.../
    AcousticCredentials.kt            # Thin loader that reads ConnectBasicConfig.properties
    contract/SampleId.kt              # Element ids shared with the other platforms' samples
    contract/ScreenName.kt            # Logged screen names
    behaviour/ShowcaseActions.kt      # The Showcase's SDK calls, payloads and result lines
    behaviour/ScreenViewCases.kt      # Screen-name test matrix
    behaviour/Scenarios.kt            # Registry of verified fixes
    analytics/SignalLog.kt            # In-memory record of what the app emitted
    analytics/AppStateSignals.kt      # Foreground/background + orientation custom events
    analytics/ScreenViewTrail.kt      # Previous screen, for screen-view referrers
    identity/IdentityViewModel.kt     # logIdentificationEvent, history (last 5)
    gestures/SwipeName.kt             # Swipe-direction classifier

sample-compose/                       # Compose app: push + analytics
  google-services.json                # Firebase config (add your own — not in repo)
  agconnect-services.json             # Huawei AppGallery Connect config (replace with yours)
  src/main/
    assets/ConnectLayoutConfig.json   # Per-screen capture and masking rules
    java/.../
      MainActivity.kt                 # SDK init, ConnectWrapper, push config
      MainScreen.kt                   # Push / Identity / Behaviour tabs; routes are the logged screen names
      notification/                   # Push authorization UI and permission handling
      identity/IdentityScreen.kt      # Identity logging UI
      behaviour/                      # Behaviour hub, Showcase, detail screen, replay modals
      appstate/AppStateScreen.kt      # Session / logical page / signal log
      gestures/GestureScreen.kt       # Gesture capture targets
      analytics/ScreenviewEffects.kt  # Compose screen-view emission
      ui/components/                  # Cards, buttons and fields every screen is built from
      ui/theme/                       # Acoustic brand colours, Material3 theme, typography
    AndroidManifest.xml               # INTERNET, NETWORK_STATE, POST_NOTIFICATIONS

sample-xml/                           # Views app: analytics only, no push
  src/main/
    assets/ConnectLayoutConfig.json   # Per-screen capture and masking rules
    java/.../
      MainActivity.kt                 # Connect.init + Connect.enable, no ConnectPushConfig
      ui/ScreenFragment.kt            # Logs each screen's screen view, with its referrer
      identity/IdentityFragment.kt    # Identity logging UI
      behaviour/                      # Behaviour hub, Showcase, detail screen, replay modals
      appstate/AppStateFragment.kt    # Session / logical page / signal log
      gestures/GesturesFragment.kt    # Gesture capture targets
    res/layout/                       # Fragment layouts
    res/values/styles.xml             # Cards, buttons and fields every screen is built from
    res/navigation/nav_graph.xml      # Identity tab, and the Behaviour tab's stack
    AndroidManifest.xml               # INTERNET, NETWORK_STATE — no POST_NOTIFICATIONS
```

---

## Configuration notes

The bundled config files are JSON and cannot carry comments, so the settings that differ from the
SDK defaults are explained here.

| File | Setting | Why |
|---|---|---|
| `shared/.../ConnectAdvancedConfig.json` | `EnableFragmentLifeCycleListener: false` | The XML app's fragments log their own screen views, under the shared screen names, from `ScreenFragment`. With the listener on, the SDK also logs a screen view named after the fragment class (`ShowcaseFragment`, …) on the first gesture on each fragment, which doubles the screen views and breaks the shared names. The Compose app has no fragments, so the switch does not affect it. |
| `sample-xml/.../ConnectLayoutConfig.json` | No `MainActivity` entry | An entry with `ScreenChange: false` for the host activity pauses capture on the first touch, and nothing lifts the pause again. |
| both `ConnectLayoutConfig.json` | `HasCustomMask: true` in `GlobalScreenSettings` | A masked value keeps its shape (`SECRET-1234` → `XXXXXX#9999`) instead of arriving empty, which is what the Showcase's masking card asks you to compare. |
| `sample-xml/.../ConnectLayoutConfig.json` | `MaskValueList: ["^SECRET-"]` | The Views capture masks by value. |
| `sample-compose/.../ConnectLayoutConfig.json` | `MaskAccessibilityLabelList` includes `Identifier Value` and `Masked field` | The Compose capture decides masking by a field's label, not its value. |

## Push provider selection

The sample app passes `strictProvider = null` to `ConnectPushConfig`, which lets the SDK auto-detect the best available provider at runtime:

- On devices with Google Play Services only → **FCM**
- On Huawei devices without Play Services → **HMS**
- On devices where **both HMS and FCM are available** → **HMS takes priority**

To override the default priority (for example, to force FCM on a Huawei device that also has Google Play Services), set `strictProvider` explicitly:

```kotlin
ConnectPushConfig(
    application = application,
    iconRes = R.drawable.ic_notification,
    strictProvider = MobileServiceType.HMS,   // or MobileServiceType.FCM
    onTokenReady = { token -> ... },
    onFailure = { exception -> ... },
    onPermissionResult = { isGranted -> ... },
)
```

---

## Setup per scenario

Each of the four SDK artifacts has its own setup path. Pick the section that matches what you want to ship — every one is self-contained and tells you which Gradle plugins, repositories, config files, dependencies, and `ConnectPushConfig` to use.

The sample app in this repository ships configured for the **both-providers** scenario (single APK with FCM + HMS).

---

## Both-providers setup (`connect-push`)

Use `connect-push` when you want a single APK that auto-detects FCM on Google Play devices and HMS on Huawei devices at runtime.

### 1. `settings.gradle.kts`

Enable the Huawei repo for both plugin resolution and dependency resolution:

```kotlin
pluginManagement {
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "com.huawei.agconnect") {
                useModule("com.huawei.agconnect:agcp:${requested.version}")
            }
        }
    }
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://developer.huawei.com/repo/") }
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://developer.huawei.com/repo/") }
    }
}
```

### 2. `build.gradle.kts` (root)

```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.agconnect) apply false
}

buildscript {
    repositories {
        maven { url = uri("https://developer.huawei.com/repo/") }
    }
    dependencies {
        classpath(libs.agcp)
    }
}
```

### 3. `app/build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.services)
    id("com.huawei.agconnect")
}

dependencies {
    implementation("io.github.go-acoustic:connect-push:<version>")
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-messaging-ktx")
}
```

### 4. Config files

Place both in `app/`:

| File | Where to obtain |
|---|---|
| `google-services.json` | Firebase Console → Project settings → Your apps |
| `agconnect-services.json` | AppGallery Connect → My apps → Download config (with SHA-256 fingerprint registered — see step 5 of [Getting started](#getting-started)) |

### 5. `ConnectPushConfig`

```kotlin
ConnectPushConfig(
    application = application,
    iconRes = R.drawable.ic_notification,
    strictProvider = null,   // auto-detect FCM or HMS at runtime 
    onTokenReady = { token -> ... },
    onFailure = { exception -> ... },
    onPermissionResult = { isGranted -> ... },
)
```

---

## FCM-only setup (`connect-push-fcm`)

Use `connect-push-fcm` for Google Play builds that do not need HMS. No Huawei plugin, no Huawei repo, no `agconnect-services.json`.

### 1. `settings.gradle.kts`

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}
```

### 2. `build.gradle.kts` (root)

```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.google.services) apply false
    // no agconnect plugin, no Huawei buildscript block
}

buildscript {
    repositories {
        google()
        mavenCentral()
        // no maven { url = uri("https://developer.huawei.com/repo/") }
    }
    dependencies {
        classpath(libs.gradle)
        // no classpath(libs.agcp)
    }
}
```

### 3. `app/build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.services)
    // no id("com.huawei.agconnect")
}

dependencies {
    implementation("io.github.go-acoustic:connect-push-fcm:<version>")
  
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-messaging-ktx")
    // Optional — add only if you want Firebase Analytics:
    // implementation("com.google.firebase:firebase-analytics-ktx")
}
```

### 4. Config files

Place in `app/`:

| File | Where to obtain |
|---|---|
| `google-services.json` | Firebase Console → Project settings → Your apps |

Do not ship `agconnect-services.json`.

### 5. `ConnectPushConfig`

```kotlin
ConnectPushConfig(
    application = application,
    iconRes = R.drawable.ic_notification,
    strictProvider = MobileServiceType.FCM,
    onTokenReady = { token -> ... },
    onFailure = { exception -> ... },
    onPermissionResult = { isGranted -> ... },
)
```

> Only devices with Google Play Services will receive push notifications.

---

## HMS-only setup (`connect-push-hms`)

Use `connect-push-hms` for AppGallery builds that do not need FCM. No Google Services plugin, no Firebase deps, no `google-services.json`.

### 1. `settings.gradle.kts`

```kotlin
pluginManagement {
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "com.huawei.agconnect") {
                useModule("com.huawei.agconnect:agcp:${requested.version}")
            }
        }
    }
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://developer.huawei.com/repo/") }
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://developer.huawei.com/repo/") }
    }
}
```

### 2. `build.gradle.kts` (root)

```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.agconnect) apply false
    // no google-services plugin
}

buildscript {
    repositories {
        maven { url = uri("https://developer.huawei.com/repo/") }
    }
    dependencies {
        classpath(libs.agcp)
    }
}
```

### 3. `app/build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.huawei.agconnect")
    // no alias(libs.plugins.google.services)
}

dependencies {
    implementation("io.github.go-acoustic:connect-push-hms:<version>")
    // connect-push-hms transitively pulls connect and HMS Push.
    // Do not add any Firebase dependencies.
}
```

### 4. Config files

Place in `app/`:

| File | Where to obtain |
|---|---|
| `agconnect-services.json` | AppGallery Connect → My apps → Download config (with SHA-256 fingerprint registered — see step 5 of [Getting started](#getting-started)) |

Do not ship `google-services.json`.

### 5. `ConnectPushConfig`

```kotlin
ConnectPushConfig(
    application = application,
    iconRes = R.drawable.ic_notification,
    strictProvider = MobileServiceType.HMS,
    onTokenReady = { token -> ... },
    onFailure = { exception -> ... },
    onPermissionResult = { isGranted -> ... },
)
```

---

## Analytics-only setup (`connect`)

Use `connect` when you only need analytics capture and do not want to register for push. No push plugins, no push config files, no `ConnectPushConfig`.

> **`sample-xml` in this repo is exactly this setup**, already wired up. If you want a working
> reference rather than a recipe, read `sample-xml/build.gradle.kts` and
> `sample-xml/src/main/java/.../MainActivity.kt` instead of following the steps below.

### 1. `settings.gradle.kts`

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}
```

### 2. `build.gradle.kts` (root)

```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    // no google-services, no agconnect
}
```

### 3. `app/build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // no push plugins
}

dependencies {
    implementation("io.github.go-acoustic:connect:<version>")
    // No Firebase or HMS dependencies.
}
```

### 4. Config files

Neither `google-services.json` nor `agconnect-services.json` is needed.

### 5. Initialise the SDK without push

In `MainActivity.kt`, call `Connect.enable` without a `ConnectPushConfig`:

```kotlin
Connect.enable(application, appKey, collectorUrl)
```

> The SDK will capture events, screen visits, and screenshots as normal — push notifications will simply not be registered or delivered.

---

## Analytics capture

The SDK captures user interactions, screen visits, and screenshots automatically with no additional configuration.

- User events (taps, text changes)
- Screen transition tracking
- Screenshots for session replay
- Sensitive data masking

---

## Troubleshooting

| Error | Cause | Fix |
|---|---|---|
| `907135700: get scope error` | Push Kit not enabled in AppGallery Connect | Enable Push Kit: My apps → Develop → APIs enabled |
| `907135702: certificate fingerprint empty` | SHA-256 not registered in AppGallery Connect | Add fingerprint and re-download `agconnect-services.json` |
| `Failed to resolve: com.google.firebase:firebase-messaging:null` | `google-services.json` missing | Add your `google-services.json` to the `sample-compose/` directory |
| Push token never arrives | Notification permission denied (Android 13+) | Grant `POST_NOTIFICATIONS` permission when prompted |
| Push sent from dashboard but app receives nothing | Identity signal not yet flushed to collector when push was dispatched | Wait for the identity signal collector POST to return HTTP 200 (visible in logcat) before sending the push — the SDK flushes on a ~30s interval; a network interruption can delay the flush further |

---

## Requirements

- Android Studio Hedgehog or later
- Android 8.0 (API 26) minimum
- Target SDK 36
- Kotlin 1.9.x
- For FCM: device or emulator with Google Play Services
- For HMS: Huawei device or emulator with HMS Core 5.0+

---

## Documentation

- Integration Guide — Full step-by-step guide covering Firebase and AppGallery Connect setup, SDK installation, push configuration, testing, and troubleshooting

---

## License

Copyright (C) 2026 Acoustic, L.P. All rights reserved.
