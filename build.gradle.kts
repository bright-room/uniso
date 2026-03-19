plugins {
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.sqldelight) apply false
    alias(libs.plugins.spotless)
}

spotless {
    kotlin {
        ktlint()
        target("**/*.kt")
        targetExclude("**/build/**", ".claude/inputs/**")
    }
    kotlinGradle {
        ktlint()
        target("**/*.gradle.kts")
        targetExclude("**/build/**", ".claude/inputs/**")
    }
}
