plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.planner"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.planner"
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
}

dependencies {
    // Обязательно для OnBackPressedDispatcher
    implementation("androidx.activity:activity:1.8.0")
    implementation("androidx.fragment:fragment:1.6.2")
    // Material Design
    implementation("com.google.android.material:material:1.9.0")
    // ViewPager
    implementation("androidx.viewpager:viewpager:1.0.0")
    // ConstraintLayout
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    implementation("de.hdodenhof:circleimageview:3.1.0")

    // Для анимаций и Material Design
    implementation ("com.google.android.material:material:1.11.0")
    implementation ("androidx.constraintlayout:constraintlayout:2.1.")

    // Для Room (если используете)
    implementation ("androidx.room:room-runtime:2.6.1")
    annotationProcessor ("androidx.room:room-compiler:2.6.1")

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)


}