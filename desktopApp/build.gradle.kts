import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    jvmToolchain(21)
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

afterEvaluate {
    tasks.withType<JavaExec> {
        if (System.getProperty("os.name").contains("Mac")) {
            jvmArgs("--add-opens", "java.desktop/sun.lwawt=ALL-UNNAMED")
            jvmArgs("--add-opens", "java.desktop/sun.lwawt.macosx=ALL-UNNAMED")
        }
    }
}

compose.desktop {
    application {
        mainClass = "net.brightroom.uniso.MainKt"

        jvmArgs("--enable-native-access=ALL-UNNAMED")
        jvmArgs("--add-opens", "java.desktop/sun.awt=ALL-UNNAMED")
        jvmArgs("--add-opens", "java.desktop/java.awt.peer=ALL-UNNAMED")

        buildTypes.release.proguard {
            configurationFiles.from("compose-desktop.pro")
        }

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi)
            packageName = "Uniso"
            packageVersion = libs.versions.app.get()
            description = "A unified desktop client for managing multiple SNS accounts in one place."
            vendor = "Bright Room"
            licenseFile.set(rootProject.file("LICENSE"))

            macOS {
                bundleID = "net.brightroom.uniso"
                iconFile.set(project.file("src/main/resources/icons/icon.icns"))
                entitlementsFile.set(project.file("src/main/resources/macOS/entitlements.plist"))
                runtimeEntitlementsFile.set(project.file("src/main/resources/macOS/runtime-entitlements.plist"))
                dmgPackageVersion = libs.versions.app.get()
                dmgPackageBuildVersion = "1"
            }

            windows {
                iconFile.set(project.file("src/main/resources/icons/icon.ico"))
                menuGroup = "Uniso"
                perUserInstall = true
                msiPackageVersion = libs.versions.app.get()
                upgradeUuid = "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
            }
        }
    }
}
