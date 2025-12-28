import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import java.util.Properties

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    id("com.android.library")
    id("kotlinx-serialization")
    id("com.codingfeline.buildkonfig")
    id("maven-publish")
    id("com.google.cloud.artifactregistry.gradle-plugin")
}

group = "ai.koda.mobile.sdk"
version = "1.5.0" // <-- Define the version here

kotlin {

    // Target declarations - add or remove as needed below. These define
    // which platforms this KMP module supports.
    // See: https://kotlinlang.org/docs/multiplatform-discover-project.html#targets
    androidTarget()

    // For iOS targets, this is also where you should
    // configure native binary output. For more information, see:
    // https://kotlinlang.org/docs/multiplatform-build-native-binaries.html#build-xcframeworks

    // A step-by-step guide on how to include this library in an XCode
    // project can be found here:
    // https://developer.android.com/kotlin/multiplatform/migrate
    val xcfName = "KodaBotsKit"

    iosX64 {
        binaries.framework {
            baseName = xcfName
            binaryOption("bundleId", "ai.koda.mobile.core.shared.kodabotskit")
        }
    }

    iosArm64 {
        binaries.framework {
            baseName = xcfName
            binaryOption("bundleId", "ai.koda.mobile.core.shared.kodabotskit")
        }
    }

    iosSimulatorArm64 {
        binaries.framework {
            baseName = xcfName
            binaryOption("bundleId", "ai.koda.mobile.core.shared.kodabotskit")
        }
    }

    // Source set declarations.
    // Declaring a target automatically creates a source set with the same name. By default, the
    // Kotlin Gradle Plugin creates additional source sets that depend on each other, since it is
    // common to share sources between related targets.
    // See: https://kotlinlang.org/docs/multiplatform-hierarchy.html
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlin.stdlib)
                implementation(libs.kotlin.serialization)
                implementation(libs.ktor.client.core)

                implementation(libs.ktor.client.logging)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.client.serialization.json)
                // Add KMP dependencies here
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        androidMain {
            dependencies {
                implementation(libs.kotlin.stdlib)
                api(libs.androidx.core)
                api(libs.androidx.appcompat)
                api(libs.androidx.cardview)
                api(libs.androidx.constraintlayout)
                api(libs.google.material)

                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.coroutines.android)

                implementation(libs.androidx.security.crypto)
                api(libs.lottie)

                implementation(libs.ktor.client.okhttp)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.android)
                implementation(libs.ktor.client.serialization.jvm)
                implementation(libs.ktor.client.logging)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.client.serialization.json)
            }
        }

//        getByName("androidDeviceTest") {
//            dependencies {
//                implementation(libs.androidx.runner)
//                implementation(libs.core)
//                implementation(libs.androidx.junit)
//            }
//        }

        iosMain {
            dependencies {
                // Add iOS-specific dependencies here. This a source set created by Kotlin Gradle
                // Plugin (KGP) that each specific iOS target (e.g., iosX64) depends on as
                // part of KMP’s default source set hierarchy. Note that this source set depends
                // on common by default and will correctly pull the iOS artifacts of any
                // KMP dependencies declared in commonMain.
                implementation(libs.ktor.client.darwin)
            }
        }
    }
}

buildkonfig {
    packageName = "ai.koda.mobile.core_shared.config"
    objectName = "AppConfig"

    // Load properties from local.properties
    val localProperties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { localProperties.load(it) }
    }

    // Default config (fallback - Production)
    defaultConfigs {
        buildConfigField(
            STRING, "baseUrl",
            localProperties.getProperty("PROD_BASE_URL") ?: "https://web.eu-pl.koda.ai"
        )
        buildConfigField(
            STRING, "apiVersion",
            localProperties.getProperty("PROD_API_VERSION") ?: "v1"
        )
        buildConfigField(
            STRING, "baseRestUrl",
            localProperties.getProperty("PROD_REST_BASE_URL") ?: "https://bot.eu-pl.koda.ai"
        )
        buildConfigField(
            STRING, "apiRestVersion",
            localProperties.getProperty("PROD_API_REST_VERSION") ?: "v1"
        )
        buildConfigField(STRING, "environment", "production")
    }

    // Staging flavor
    defaultConfigs("staging") {
        buildConfigField(
            STRING, "baseUrl",
            localProperties.getProperty("STAGING_BASE_URL") ?: "https://web.staging.koda.ai"
        )
        buildConfigField(
            STRING, "apiVersion",
            localProperties.getProperty("STAGING_API_VERSION") ?: "v1"
        )
        buildConfigField(
            STRING, "baseRestUrl",
            localProperties.getProperty("STAGING_REST_BASE_URL") ?: "https://bot.staging.koda.ai"
        )
        buildConfigField(
            STRING, "apiRestVersion",
            localProperties.getProperty("STAGING_API_REST_VERSION") ?: "v1"
        )
        buildConfigField(STRING, "environment", "staging")
    }

    // Production flavor
    defaultConfigs("prod") {
        buildConfigField(
            STRING, "baseUrl",
            localProperties.getProperty("PROD_BASE_URL") ?: "https://web.eu-pl.koda.ai"
        )
        buildConfigField(
            STRING, "apiVersion",
            localProperties.getProperty("PROD_API_VERSION") ?: "v1"
        )
        buildConfigField(
            STRING, "baseRestUrl",
            localProperties.getProperty("PROD_REST_BASE_URL") ?: "https://bot.eu-pl.koda.ai"
        )
        buildConfigField(
            STRING, "apiRestVersion",
            localProperties.getProperty("PROD_API_REST_VERSION") ?: "v1"
        )
        buildConfigField(STRING, "environment", "production")
    }
}

android {
    namespace = "ai.koda.mobile.core_shared"
    compileSdk = 36
    defaultConfig {
        minSdk = 26
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }
}

val props: Properties = Properties().apply {
    project.rootProject.file("local.properties").inputStream().use { load(it) }
}

repositories {
    maven {
        url = uri(props.getProperty("publishUrl") ?: "")
    }
}

afterEvaluate {
    configure<PublishingExtension> {
        repositories {
            maven {
                url = uri(props.getProperty("publishUrl") ?: "")
            }
        }
        publications {
            create<MavenPublication>("release") {
                groupId = "ai.koda.mobile.sdk"
                artifactId = "koda-core"
                version = "1.5.0"

                // For Kotlin Multiplatform, publish the AAR artifact
                artifact(tasks.getByName("bundleReleaseAar"))
            }
        }
    }
}