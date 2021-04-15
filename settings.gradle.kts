rootProject.name = "kotlin-js-playground"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven(url = "https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven")
    }
}

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}
