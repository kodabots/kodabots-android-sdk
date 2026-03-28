import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import java.util.Properties

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlinCocoapods)
    id("com.android.library")
    id("kotlinx-serialization")
    id("com.codingfeline.buildkonfig")
    id("maven-publish")
    id("com.google.cloud.artifactregistry.gradle-plugin")
}

group = "ai.koda.mobile.sdk"
version = "2.2.0"

kotlin {
    androidTarget {
        publishLibraryVariants("release")
    }

    val xcfName = "KodaBotsKit"

    // Replace individual iOS target blocks with a single loop
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { target ->
        target.binaries.framework {
            baseName = xcfName
            binaryOption("bundleId", "ai.koda.mobile.core.shared.kodabotskit")
        }
    }

    cocoapods {
        // Required properties
        // Specify the required Pod version here
        // Otherwise, the Gradle project version is used
        version = "2.2.0"
        summary = "KodaBots SDK for iOS"
        homepage = "https://github.com/kodabots/kodabots-android-sdk"

        name = "KodaBotsKit"
        ios.deploymentTarget = "16.0"

        framework {
            baseName = xcfName
            isStatic = false
        }

        pod("lottie-ios") {
            moduleName = "Lottie"
            version = "4.6.0"
            extraOpts = listOf("-compiler-option", "-fmodules", "-compiler-option", "-fbuiltin-module-map")
        }

        extraSpecAttributes["resources"] = "['src/iosMain/resources/**/*']"
    }

    tasks.matching { it.name.startsWith("syncFramework") }.configureEach {
        doLast {
            val frameworkDir = file("build/cocoapods/framework/KodaBotsKit.framework")
            val resourcesDir = file("src/iosMain/resources")
            if (frameworkDir.exists() && resourcesDir.exists()) {
                copy {
                    from(resourcesDir)
                    into(frameworkDir)
                }
            }
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlin.stdlib)
                implementation(libs.kotlin.serialization)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.logging)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.client.serialization.json)
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

        iosMain {
            dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }
    }
}

buildkonfig {
    packageName = "ai.koda.mobile.core_shared.config"
    objectName = "AppConfig"

    val localProperties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { localProperties.load(it) }
    }

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

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
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
    publishing {
        repositories {
            maven {
                url = uri(props.getProperty("publishUrl") ?: "")
            }
        }

        publications {
            withType<MavenPublication> {
                groupId = "ai.koda.mobile.sdk"
                version = "2.2.0-preview3"

                // Change artifact name from core-shared to koda-core2
                artifactId = artifactId.replace("core-shared", "koda-core2")
            }
        }
    }
}