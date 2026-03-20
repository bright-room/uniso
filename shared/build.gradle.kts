import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.buildkonfig)
}

kotlin {
    jvm()

    jvmToolchain(25)

    sourceSets {
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

buildkonfig {
    packageName = "net.brightroom.uniso"

    defaultConfigs {
        buildConfigField(STRING, "APP_VERSION", libs.versions.app.get())
    }
}
