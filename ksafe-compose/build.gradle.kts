import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    // Compose compiler plugin — required so @Composable functions in this
    // module (e.g. rememberKSafeState) get the Composer parameter injected
    // and `remember`/`LaunchedEffect` codegen properly. Without it, the
    // Kotlin compiler reports "couldn't find inline method ... remember".
    alias(libs.plugins.kotlin.compose)
    // Add the maven publish plugin
    alias(libs.plugins.vanniktech.mavenPublish)
}

// Set the same group and version as your main library
group = "eu.anifantakis"
version = "2.0.0-RC2-harukeyua.1"

kotlin {
    androidLibrary {
        namespace = "eu.anifantakis.lib.ksafe.compose"
        compileSdk = 36
        minSdk = 24

        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }

        withHostTestBuilder {
        }

        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    val xcfName = "ksafe-composeKit"

    iosX64 {
        binaries.framework {
            baseName = xcfName
        }
    }

    iosArm64 {
        binaries.framework {
            baseName = xcfName
        }
    }

    iosSimulatorArm64 {
        binaries.framework {
            baseName = xcfName
        }
    }

    jvm()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    js(IR) {
        browser()
    }

    // Mirror :ksafe — the default hierarchy gives us the shared webMain/webTest
    // intermediate source sets used by both wasmJs and js(IR). Without this
    // call, `webTest` does not exist.
    applyDefaultHierarchyTemplate()

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlin.stdlib)
                // Use api() for your main library so it's exposed to consumers
                api(project(":ksafe"))

                implementation(libs.runtime)
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.runtime)
            }
        }

        androidMain {
            dependencies {
                // Add Android-specific dependencies here
            }
        }

        getByName("androidDeviceTest") {
            dependencies {
                implementation(libs.androidx.runner)
                implementation(libs.androidx.core)
                implementation(libs.androidx.junit)
            }
        }

        iosMain {
            dependencies {
                // Add iOS-specific dependencies here
            }
        }

        getByName("webTest") {
            dependencies {
                implementation(libs.kotlinx.coroutines.test)
            }
        }
    }

    targets.withType<KotlinAndroidTarget>().configureEach {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
}

mavenPublishing {
    coordinates(
        groupId = group.toString(),
        artifactId = "ksafe-compose",
        version = version.toString()
    )

    pom {
        name = "KSafe Compose - Jetpack Compose Extensions"
        description = "Jetpack Compose extensions for KSafe MultiPlatform Encrypted Persistence library"
        inceptionYear = "2025"
        url = "https://github.com/HarukeyUA/KSafe"
        licenses {
            license {
                name = "Apache-2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0"
            }
        }
        developers {
            developer {
                id = "ioannis-anifantakis"
                name = "Ioannis Anifantakis"
                url = "https://anifantakis.eu"
                email = "ioannisanif@gmail.com"
            }
        }
        scm {
            url = "https://github.com/HarukeyUA/KSafe"
            connection = "scm:git:https://github.com/HarukeyUA/KSafe.git"
            developerConnection = "scm:git:ssh://git@github.com/HarukeyUA/KSafe.git"
        }
    }
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/HarukeyUA/KSafe")
            credentials {
                username = providers.gradleProperty("gpr.user").orNull
                    ?: System.getenv("GITHUB_ACTOR")
                password = providers.gradleProperty("gpr.key").orNull
                    ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
