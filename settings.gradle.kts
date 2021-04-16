rootProject.name = "kotlin-js-playground"

dependencyResolutionManagement {
  repositories {
    mavenCentral()
    google()
    maven(url = "https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven")
    maven(url = "https://maven.pkg.jetbrains.space/kotlin/p/kotlin/kotlin-js-wrappers")
  }
}

pluginManagement {
  repositories {
    gradlePluginPortal()
    mavenCentral()
    google()
  }
}
