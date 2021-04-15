plugins {
    idea
    // id("com.google.devtools.ksp") version "1.4.32-1.0.0-alpha07"
    kotlin("js") version "1.5.0-RC"
    kotlin("plugin.serialization") version "1.5.0-RC"
    id("com.github.ben-manes.versions") version "0.38.0"
}

group = "dev.suresh"
version = "0.0.1"

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-html:0.7.3")
    testImplementation(kotlin("test-js"))
}

kotlin {
    js(IR) {
        binaries.executable()
        browser {
            commonWebpackConfig {
                cssSupport.enabled = true
            }
        }
    }

    sourceSets.all {
        languageSettings.apply {
            progressiveMode = true
            enableLanguageFeature(org.jetbrains.kotlin.config.LanguageFeature.InlineClasses.name)
            enableLanguageFeature(org.jetbrains.kotlin.config.LanguageFeature.NewInference.name)
            enableLanguageFeature(org.jetbrains.kotlin.config.LanguageFeature.JvmRecordSupport.name)
            useExperimentalAnnotation("kotlin.RequiresOptIn")
            useExperimentalAnnotation("kotlin.ExperimentalStdlibApi")
            useExperimentalAnnotation("kotlin.ExperimentalUnsignedTypes")
            useExperimentalAnnotation("kotlin.time.ExperimentalTime")
            useExperimentalAnnotation("kotlinx.serialization.ExperimentalSerializationApi")
            useExperimentalAnnotation("kotlin.ExperimentalMultiplatform")
            useExperimentalAnnotation("kotlin.js.ExperimentalJsExport")
        }
    }
}

tasks {
    idea {
        module {
            isDownloadJavadoc = true
            isDownloadSources = true
        }
    }

    wrapper {
        gradleVersion = "7.0"
        distributionType = Wrapper.DistributionType.ALL
    }

    // Default task
    defaultTasks("clean", "tasks", "--all")
}
