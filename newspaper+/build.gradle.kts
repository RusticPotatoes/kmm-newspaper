import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.github.jetbrains.rssreader.newspaper"
    compileSdk = (findProperty("android.compileSdk") as String).toInt()

    defaultConfig {
        minSdk = (findProperty("android.minSdk") as String).toInt()
        targetSdk = (findProperty("android.targetSdk") as String).toInt()

        applicationId = "com.github.jetbrains.rssreader.androidApp"
        versionCode = 2
        versionName = "1.1"
    }

    signingConfigs {
        create("release") {
            storeFile = file("./key/key.jks")
            val localProperties = project.rootProject.file("local.properties")
            if (localProperties.exists()) {
                val properties = Properties().apply {
                    localProperties.inputStream().use { load(it) }
                }
                storePassword = properties.getProperty("storePwd")
                keyAlias = properties.getProperty("keyAlias")
                keyPassword = properties.getProperty("keyPwd")
            }
        }
    }

    buildTypes {
        create("debugPG") {
            isDebuggable = false
            isMinifyEnabled = true
            versionNameSuffix = " debugPG"
            matchingFallbacks.add("debug")

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                file("proguard-rules.pro")
            )
        }
        release {
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("release")

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                file("proguard-rules.pro")
            )
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.androidx.compose.compiler.get()
    }
    compileOptions {
        // Flag to enable support for the new language APIs
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    dependencies {
        implementation(project(":shared"))
        //desugar utils
        coreLibraryDesugaring(libs.desugar.jdk.libs)
        //Compose
        implementation(libs.androidx.compose.ui)
        implementation(libs.androidx.compose.ui.tooling)
        implementation(libs.androidx.compose.foundation)
        implementation(libs.androidx.compose.material)
        //Compose Utils
        implementation(libs.coil.compose)
        implementation(libs.activity.compose)
        implementation(libs.accompanist.swiperefresh)
        //Coroutines
        implementation(libs.kotlinx.coroutines.core)
        implementation(libs.kotlinx.coroutines.android)
        //DI
        implementation(libs.koin.core)
        implementation(libs.koin.android)
        //Navigation
        implementation(libs.voyager.navigator)
        //WorkManager
        implementation(libs.work.runtime.ktx)
    }
}
