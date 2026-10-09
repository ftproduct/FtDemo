pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "FreightTigerDriverAssistant"

// Platform-independent core (domain, voice intents, API contracts, mock backend).
// Dependencies on com.freighttiger.driverassistant:<module> are substituted with these projects.
includeBuild("platform-core")

include(":app")
include(":core:database")
include(":core:security")
