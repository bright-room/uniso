import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    jvmToolchain(25)
}

dependencies {
    implementation(projects.shared)
    implementation(compose.desktop.currentOs)
    implementation(libs.compose.runtime)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui)
    implementation(libs.kotlinx.coroutinesSwing)
}

compose.desktop {
    application {
        mainClass = "net.brightroom.uniso.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi)
            packageName = "Uniso"
            packageVersion = "1.0.0"
            description = "A unified desktop client for managing multiple SNS accounts in one place."
            vendor = "Bright Room"

            macOS {
                bundleID = "net.brightroom.uniso"
                iconFile.set(project.file("src/main/resources/icons/icon.icns"))
            }

            windows {
                iconFile.set(project.file("src/main/resources/icons/icon.ico"))
                menuGroup = "Uniso"
            }
        }
    }
}
