import com.android.tools.r8.internal.im

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.basketballshoesandroidshop"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.basketballshoesandroidshop"
        minSdk = 24
        targetSdk = 35
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
    buildFeatures{
        viewBinding = true;
    }

}

dependencies {
    implementation(":zpdk-release-v3.1@aar")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.database)
    implementation(libs.databinding.runtime)
    implementation(fileTree(mapOf(
        "dir" to "/Users/hoanghai/Desktop/ZaloPayLib",
        "include" to listOf("*.aar", "*.jar"),
        "exclude" to listOf("")
    )))
    implementation(libs.firebase.firestore)
//    implementation(fileTree(mapOf(
//        "dir" to "/Users/hoanghai/Desktop/ZaloPayLib",
//        "include" to listOf("*.aar", "*.jar"),
//        "exclude" to listOf("")
//    )))
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation("com.github.bumptech.glide:glide:4.12.0")
    implementation("com.google.code.gson:gson:2.9.1")
    implementation("com.github.ismaeldivita:chip-navigation-bar:1.4.0")
    implementation("com.squareup.picasso:picasso:2.5.2")
    implementation("com.google.android.material:material:1.9.0")
    //zalo pay
    implementation("com.squareup.okhttp3:okhttp:4.6.0")
    implementation("commons-codec:commons-codec:1.14")
    implementation ("com.android.volley:volley:1.2.1")
    implementation ("com.google.android.gms:play-services-maps:18.2.0")
    implementation ("com.google.maps.android:android-maps-utils:2.3.0")


}