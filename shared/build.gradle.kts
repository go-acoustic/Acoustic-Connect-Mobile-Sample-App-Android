plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    // The library's own namespace, used for its R and BuildConfig only. The Kotlin sources keep
    // the `...demo.connect.external` packages they had when they lived in the apps, so neither
    // app needed an import change when they moved here.
    namespace = "com.acoustic.connect.android.demo.connect.external.shared"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    // `api`, not `implementation`: both apps call Connect directly, and the types this module
    // exposes (Token, ConnectScreenviewType) come from it. The base `connect` artifact is
    // deliberate — the shared code is UI-free and push-free, so the analytics-only sample must
    // not pick up a push dependency through here. The Compose sample adds `connect-push` itself.
    api(libs.connect)

    testImplementation(libs.junit)
}
