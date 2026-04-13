plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "com.dreisamlib.demo"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.dreisam.demo"
        minSdk = 26
        targetSdk = 36
        versionCode = 2
        versionName = "1.0.2"

    }

    signingConfigs {
        create("signConfig") {
            keyAlias = "dreisam"
            keyPassword = "251125"
            storeFile = file("../dreisam.jks")
            storePassword = "251125"
        }
    }

    buildTypes {
        release {
            isShrinkResources = true//混淆是否开启
            isMinifyEnabled = true//资源是否压缩
            isDebuggable = false//是否可调试
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("signConfig")
        }
        debug {
            isShrinkResources = false//混淆是否开启
            isMinifyEnabled = false//资源是否压缩
            isDebuggable = true//是否可调试
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("signConfig")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(files("libs/DreisamLib-release_1.0.2.aar"))
    implementation("androidx.work:work-runtime-ktx:2.9.1")

    implementation("androidx.activity:activity:1.8.0")
    implementation("androidx.core:core-ktx:1.8.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.8.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    //gson
    implementation("com.google.code.gson:gson:2.13.2")
    implementation("me.dm7.barcodescanner:zxing:1.9.4")

    //动态权限框架 XXPermissions
    implementation("com.github.getActivity:XXPermissions:26.0")

}