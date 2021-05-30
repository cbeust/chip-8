pluginManagement {
    repositories {
        google()
        jcenter()
        gradlePluginPortal()
        mavenCentral()
        maven(url = "https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
    
}

rootProject.name = "chip-8"

include(":androidApp", ":shared", ":compose-desktop")

