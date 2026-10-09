plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.google.services) apply false
}

/** Runs the JVM unit/scenario tests of the platform-independent core. */
tasks.register("platformCoreTest") {
    group = "verification"
    description = "Runs platform-core (domain, voice, network, mock backend) tests."
    dependsOn(
        gradle.includedBuild("platform-core").task(":domain-workflow:test"),
        gradle.includedBuild("platform-core").task(":core-network:test"),
    )
}
