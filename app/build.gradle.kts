import java.util.Properties

plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

val apiKeysPropertiesFile = rootProject.file("secrets.properties")
val apiKeysProperties = Properties()
apiKeysProperties.load(apiKeysPropertiesFile.inputStream())

android {
    namespace = "com.example.finalproject"
    compileSdk = 34
    buildFeatures.buildConfig = true
    defaultConfig {
        applicationId = "com.example.finalproject"
        minSdk = 26
        targetSdk = 34
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

            buildConfigField("String", "MAPS_API_KEY", "\"${apiKeysProperties["MAPS_API_KEY"]}\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Gifs and Glide:
    implementation("pl.droidsonroids.gif:android-gif-drawable:1.2.22")
    implementation("com.github.bumptech.glide:glide:4.12.0")

    // Country code picker:
    implementation("com.hbb20:ccp:2.5.0")

    // Firebase authentication:
    implementation("com.google.firebase:firebase-auth:22.3.0")

    // Firebase authentication UI:
    implementation("com.firebaseui:firebase-ui-auth:8.0.2")

    // Firebase BoM:
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))

    // Firestore library:
    implementation("com.google.firebase:firebase-firestore")

    // Firebase storage:
    implementation("com.google.firebase:firebase-storage")

    // Firebase storage UI (to work with Glide):
    implementation("com.firebaseui:firebase-ui-storage:7.2.0")

    // Google maps services:
    implementation("org.slf4j:slf4j-simple:1.7.25")
    implementation("com.google.maps:google-maps-services:2.2.0")
    implementation("com.google.android.gms:play-services-maps:18.2.0")

    val roomVersion = "2.6.0"
    implementation("androidx.room:room-runtime:$roomVersion")
    annotationProcessor("androidx.room:room-compiler:$roomVersion")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}

secrets {
    propertiesFileName = "secrets.properties"

    defaultPropertiesFileName = "local.defaults.properties"
}
