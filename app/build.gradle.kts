import java.io.FileInputStream
import java.util.Base64
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val localProps = Properties()
val localPropsFile = rootProject.file("local.properties")
if (localPropsFile.exists()) {
    localProps.load(FileInputStream(localPropsFile))
}

fun getSecret(key: String): String {
    return (localProps.getProperty(key)
        ?: System.getenv(key)
        ?: throw GradleException("$key is missing.")).trim()
}

android {
    namespace = "com.xapps.utils.xsigner"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.xapps.utils.xsigner"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
    }

    signingConfigs {
        create("release") {
            val base64Key = getSecret("KEYSTORE_BASE64")
            val tmpFile = file("build/tmp_keystore.jks")
            tmpFile.parentFile.mkdirs()
            tmpFile.writeBytes(Base64.getMimeDecoder().decode(base64Key))

            storeFile = tmpFile
            storePassword = getSecret("KEYSTORE_PASSWORD")
            keyAlias = getSecret("KEY_ALIAS")
            keyPassword = getSecret("ALIAS_PASSWORD")
        }

        getByName("debug") {
            storeFile = file(System.getProperty("user.home") + "/.android/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }

        create("preview") {
            initWith(getByName("release"))
        }
    }

    buildTypes {
        getByName("debug") {
            isDebuggable = true
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
        }

        create("preview") {
            initWith(getByName("release"))
            matchingFallbacks += listOf("release", "debug")
            isDebuggable = false
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("preview")
        }

        getByName("release") {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.splashscreen)
    implementation(libs.androidx.compose.material.icons.extended)

    debugImplementation(libs.androidx.compose.ui.tooling)
}
