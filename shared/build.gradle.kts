plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    jvm()

    jvmToolchain(21)

    sourceSets {
        commonMain.dependencies {
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
