import com.diffplug.spotless.kotlin.KtfmtStep.Style.DEFAULT
import org.jetbrains.kotlin.config.*
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("com.google.devtools.ksp") version "1.4.32-1.0.0-alpha07"
  kotlin("js") version "1.5.0-RC"
  kotlin("plugin.serialization") version "1.5.0-RC"
  id("com.github.ben-manes.versions") version "0.38.0"
  id("com.diffplug.spotless") version "5.12.1"
}

group = "dev.suresh"

version = "0.0.1"

dependencies {
  val ktorVersion = "1.5.3"
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.4.3")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.1.0")
  implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.1.1")
  implementation("io.ktor:ktor-client-js:$ktorVersion")
  implementation("io.ktor:ktor-client-websockets:$ktorVersion")
  implementation("org.jetbrains.kotlinx:kotlinx-html:0.7.3")
  implementation("org.jetbrains:kotlin-styled:5.2.3-pre.153-kotlin-1.4.32")
  testImplementation(kotlin("test-js"))
  // implementation(npm("crypto-js", "4.0.0"))
  // implementation(npm("@types/crypto-js", "4.0.1", generateExternals = true))
  // implementation(devNpm("webpack-bundle-analyzer", "4.4.0"))
}

kotlin {
  js(IR) {
    binaries.executable()

    browser {
      distribution { directory = File("$projectDir/docs") }
      commonWebpackConfig { cssSupport.enabled = true }
    }
  }

  sourceSets.all {
    languageSettings.apply {
      progressiveMode = true
      enableLanguageFeature(LanguageFeature.InlineClasses.name)
      enableLanguageFeature(LanguageFeature.NewInference.name)
      enableLanguageFeature(LanguageFeature.JvmRecordSupport.name)
      useExperimentalAnnotation("kotlin.RequiresOptIn")
      useExperimentalAnnotation("kotlin.ExperimentalStdlibApi")
      useExperimentalAnnotation("kotlin.ExperimentalUnsignedTypes")
      useExperimentalAnnotation("kotlin.ExperimentalMultiplatform")
      useExperimentalAnnotation("kotlin.time.ExperimentalTime")
      useExperimentalAnnotation("kotlinx.coroutines.ExperimentalCoroutinesApi")
      useExperimentalAnnotation("kotlinx.serialization.ExperimentalSerializationApi")
      useExperimentalAnnotation("kotlin.js.ExperimentalJsExport")
    }
  }
}

spotless {
  val ktfmtVersion = "0.24"

  kotlin {
    ktfmt(ktfmtVersion).style(DEFAULT)
    targetExclude("$buildDir/**/*.kt", "bin/**/*.kt")
  }

  kotlinGradle {
    ktfmt(ktfmtVersion).style(DEFAULT)
    target("*.gradle.kts")
  }
}

tasks {
  withType<KotlinCompile>().configureEach {
    kotlinOptions {
      verbose = true
      incremental = true
      allWarningsAsErrors = false
      freeCompilerArgs +=
          listOf(
              "-progressive",
              "-Xallow-result-return-type",
          )
    }
  }

  withType<KotlinJsCompile>().configureEach {
    kotlinOptions.freeCompilerArgs +=
        listOf("-Xir-per-module"
            // "-Xir-property-lazy-initialization",
            )
  }

  wrapper {
    gradleVersion = "7.0"
    distributionType = Wrapper.DistributionType.ALL
  }

  // Default task
  defaultTasks("clean", "tasks", "--all")
}
