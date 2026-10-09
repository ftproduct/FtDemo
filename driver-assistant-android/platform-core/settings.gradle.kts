// Pure-Kotlin (JVM) build holding everything that does not depend on the Android SDK:
// domain models, workflow/business rules, voice intent handling, API contracts and the mock backend.
// It is consumed by the Android build through `includeBuild("platform-core")`, and can also be
// built and tested on its own:  ../gradlew -p platform-core test
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories { mavenCentral() }
    versionCatalogs {
        create("libs") { from(files("../gradle/libs.versions.toml")) }
    }
}

rootProject.name = "platform-core"

include(":core-model", ":domain-workflow", ":core-network")
project(":core-model").projectDir = file("core/model")
project(":domain-workflow").projectDir = file("domain/workflow")
project(":core-network").projectDir = file("core/network")
