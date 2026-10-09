import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

// Firebase is optional: FCM is enabled only when an environment-specific google-services.json is
// present (it is git-ignored). Without it the app relies on demo push / trip-state polling.
if (file("google-services.json").exists()) {
    apply(plugin = libs.plugins.google.services.get().pluginId)
}

/** Reads a Gradle property, then an environment variable, then a default. Never hard-code secrets. */
fun envConfig(property: String, env: String, default: String = ""): String =
    (project.findProperty(property) as String?)?.takeIf { it.isNotBlank() } ?: System.getenv(env) ?: default

fun quoted(value: String) = "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\""

android {
    namespace = "com.freighttiger.driverassistant"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.freighttiger.driverassistant"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "com.freighttiger.driverassistant.HiltTestRunner"

        buildConfigField("String", "API_BASE_URL", quoted(envConfig("ftda.apiBaseUrl", "FTDA_API_BASE_URL")))
        buildConfigField("String", "API_ENVIRONMENT", quoted(envConfig("ftda.apiEnvironment", "FTDA_API_ENVIRONMENT", "unset")))
        buildConfigField("String", "SUPPORT_PHONE_NUMBER", quoted(envConfig("ftda.supportPhone", "FTDA_SUPPORT_PHONE")))
    }

    buildTypes {
        debug {
            buildConfigField("boolean", "DEMO_MODE", envConfig("ftda.demoMode", "FTDA_DEMO_MODE", "true"))
            buildConfigField("boolean", "ALLOW_CLEARTEXT", envConfig("ftda.allowCleartext", "FTDA_ALLOW_CLEARTEXT", "false"))
        }
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            buildConfigField("boolean", "DEMO_MODE", envConfig("ftda.demoMode", "FTDA_DEMO_MODE", "false"))
            buildConfigField("boolean", "ALLOW_CLEARTEXT", "false")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" }
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

kotlin {
    compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
}

dependencies {
    implementation("com.freighttiger.driverassistant:core-network:0.1.0")
    implementation(project(":core:database"))
    implementation(project(":core:security"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.navigation.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)
    implementation(libs.androidx.work.runtime.ktx)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.hilt.android.testing)
    androidTestImplementation(libs.androidx.work.testing)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    kspAndroidTest(libs.hilt.compiler)
}
