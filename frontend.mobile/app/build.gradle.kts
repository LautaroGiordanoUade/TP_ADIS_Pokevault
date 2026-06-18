import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use(::load)
    }
}

val apiProperties = Properties().apply {
    val file = rootProject.file("api.properties")
    if (file.exists()) {
        file.inputStream().use(::load)
    }
}

fun ensureTrailingSlash(value: String): String =
    if (value.endsWith("/")) value else "$value/"

val apiTarget = (
    localProperties.getProperty("API_TARGET")
        ?: apiProperties.getProperty("API_TARGET")
        ?: System.getenv("API_TARGET")
        ?: "local"
).trim().lowercase()

val configuredApiBaseUrl = (
    localProperties.getProperty("API_BASE_URL")
        ?: apiProperties.getProperty("API_BASE_URL")
        ?: System.getenv("API_BASE_URL")
)?.trim()?.takeIf { it.isNotBlank() }

val apiBaseUrl = ensureTrailingSlash(
    configuredApiBaseUrl ?: when (apiTarget) {
        "local", "current" -> "http://10.0.2.2:8000/api/"
        "server", "deployed", "back4app" -> "https://pokevaultapi-we8cuobk.b4a.run/api/"
        else -> error("Unknown API_TARGET '$apiTarget'. Use local, current, server, deployed, or back4app.")
    }
)

android {
    namespace = "com.pokevault.mobile"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.lgiordano.pokemarket"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        resourceConfigurations.addAll(listOf("es", "en"))

        buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")
        buildConfigField(
            "String",
            "GOOGLE_WEB_CLIENT_ID",
            "\"${localProperties.getProperty("GOOGLE_WEB_CLIENT_ID") ?: System.getenv("GOOGLE_WEB_CLIENT_ID").orEmpty()}\"",
        )
    }

    androidResources {
        generateLocaleConfig = true
    }

    bundle {
        language {
            enableSplit = false
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

}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons)
    debugImplementation(libs.androidx.ui.tooling)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    ksp(libs.hilt.compiler)

    implementation(libs.retrofit)
    implementation(libs.retrofit.moshi)
    implementation(libs.okhttp.logging)
    implementation(libs.moshi.kotlin)
    ksp(libs.moshi.codegen)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation(libs.datastore.preferences)
    implementation(libs.coil.compose)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services)
    implementation(libs.googleid)
}
