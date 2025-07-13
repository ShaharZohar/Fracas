// Define plugins block
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// Define Compose version once to maintain consistency
val composeVersion = "1.5.4" // Use a version that's available in repositories

// Direct disabling of data binding tasks - early in the build lifecycle
gradle.startParameter.taskRequests.forEach { taskRequest ->
    taskRequest.args.forEach { arg ->
        if (arg.contains("dataBinding", ignoreCase = true) || arg.contains("DataBinding", ignoreCase = true)) {
            gradle.startParameter.excludedTaskNames.add(arg)
        }
    }
}

// Add JDK toolchain configuration
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

android {
    namespace = "com.demo.fracasgame"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.demo.fracasgame"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        
        debug {
            isMinifyEnabled = false
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }
    
    // Explicitly set Kotlin JVM target to match Java's target
    kotlinOptions {
        jvmTarget = "17"
    }
    
    // Explicitly disable all data binding related features
    buildFeatures {
        dataBinding = false
        // Re-enable viewBinding
        viewBinding = true
        compose = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.6" // Match with Kotlin 1.9.21
    }
    
    packaging {
        resources {
            excludes.add("/META-INF/{AL2.0,LGPL2.1}")
        }
    }
    
    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }
    
    testOptions {
        unitTests {
            isReturnDefaultValues = true
        }
    }
}

// Using version catalogs for dependencies helps with Gradle 9.0+ compatibility
dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.3")
    
    // Kotlin dependencies
    implementation(libs.kotlin.stdlib)
    
    // Core Android dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    
    // Compose dependencies
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.activity.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.compose.runtime.livedata)
    implementation(libs.lifecycle.viewmodel.compose)
    
    // Debug tools
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
    
    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.compose.ui.test.junit4)
}
