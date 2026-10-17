plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

// Keep the user's tested model as the source of truth; package it without duplicating
// a 118 MB binary in the source tree. Tokenizer/config assets live under src/main/assets/tts.
val bundledVoiceAssets = layout.buildDirectory.dir("generated/voiceAssets")
val prepareVoiceModel by tasks.registering {
    val sourceModel = rootProject.layout.projectDirectory.file("tests/model.onnx")
    inputs.file(sourceModel)
    outputs.file(bundledVoiceAssets.map { it.file("tts/rasa/model.onnx") })
    doLast {
        check(sourceModel.asFile.isFile) { "Place the validated VITS model at tests/model.onnx before building." }
        project.copy {
            from(sourceModel)
            into(bundledVoiceAssets.map { it.dir("tts/rasa") })
        }
    }
}

android {
    namespace = "com.example.citigrow"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.citigrow"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    sourceSets.getByName("main").assets.srcDir(bundledVoiceAssets)
    androidResources { noCompress += "onnx" }
}

tasks.named("preBuild").configure { dependsOn(prepareVoiceModel) }

dependencies {
    implementation(libs.onnxruntime.android)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
