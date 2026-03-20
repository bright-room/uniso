plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.sqldelight)
}

val generateBuildConfig by tasks.registering {
    val outputDir = layout.buildDirectory.dir("generated/buildconfig")
    val appVersion = libs.versions.app

    outputs.dir(outputDir)

    doLast {
        val dir = outputDir.get().asFile.resolve("net/brightroom/uniso")
        dir.mkdirs()
        dir.resolve("BuildConfig.kt").writeText(
            """
            |package net.brightroom.uniso
            |
            |object BuildConfig {
            |    const val APP_VERSION = "${appVersion.get()}"
            |}
            """.trimMargin(),
        )
    }
}

kotlin {
    jvm()

    jvmToolchain(25)

    sourceSets {
        commonMain {
            kotlin.srcDir(generateBuildConfig)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.kotlinx.coroutinesCore)
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutinesExtensions)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutinesTest)
        }
        jvmMain.dependencies {
            implementation(libs.sqldelight.sqliteDriver)
            api(libs.compose.webview)
        }
        jvmTest.dependencies {
            implementation(libs.sqldelight.sqliteDriver)
        }
    }
}

sqldelight {
    databases {
        create("UnisoDatabase") {
            packageName.set("net.brightroom.uniso.data.db")
        }
    }
}
