import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

val localProperties = Properties().apply {
    val localFile = rootProject.file("local.properties")
    if (localFile.exists()) {
        localFile.inputStream().use { load(it) }
    }
}

val gprUser = localProperties.getProperty("gpr.user") ?: System.getenv("USERNAME")
val gprKey = localProperties.getProperty("gpr.key") ?: System.getenv("TOKEN")

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinCocoapods)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.skie)
    alias(libs.plugins.kotlinx.serialization)
    id("maven-publish")
}

kotlin {
    androidTarget {
        publishLibraryVariants("release")
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_1_8)
                }
            }
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        version = "1.0.8"
        summary = "Utilise Google Sheets API with KMP"
        homepage = "https://github.com/ValeriGYordanov/GSheetSync"
        name = "GSheetSync"

        ios.deploymentTarget = "16.0"
        framework {
            baseName = "GSheetSync"
            isStatic = false
        }
    }
    
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.koin.core)
        }
        androidMain.dependencies {
            implementation(libs.androidx.lifecycle.viewmodel.ktx)
            implementation(libs.ktor.client.android)
            api(libs.google.api.services.sheets)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.vlr.gsheetsync"
    compileSdk = 35
    defaultConfig {
        minSdk = 29
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

publishing {
    publications {
        // Only configure Android publication
        create<MavenPublication>("androidReleaseAar") {
            groupId = "com.github.vlr"
            artifactId = "gsheetsync"
            version = "1.0.8"

            afterEvaluate {
                from(components.findByName("androidRelease") ?: components["release"])
            }
        }
        // Don't configure other publications
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/ValeriGYordanov/GSheetSync")
            credentials {
                username = gprUser
                password = gprKey
            }
        }
    }
}

kotlin {
    val frameworkName = "GSheetSync"
    val xcf = XCFramework(frameworkName)

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>().configureEach {
        binaries.framework {
            baseName = frameworkName
            xcf.add(this)
        }
    }
}