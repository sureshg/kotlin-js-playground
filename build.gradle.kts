import org.jetbrains.kotlin.config.*
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.google.devtools.ksp") version "1.5.30-1.0.0"
    kotlin("js") version "1.5.31"
    kotlin("plugin.serialization") version "1.5.31"
    id("com.github.ben-manes.versions") version "0.39.0"
    id("com.diffplug.spotless") version "5.15.2"
    id("dev.zacsweers.redacted") version "0.8.0"
}

group = "dev.suresh"
version = "0.0.1"

kotlin {
    js(IR) {
        browser {
            distribution {
                directory = File("$projectDir/docs")
            }

            commonWebpackConfig {
                cssSupport.enabled = true
                outputFileName = "app.js"
            }

            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
        }
        binaries.executable()
        compilations.all { kotlinOptions {} }
    }

    sourceSets.all {
        languageSettings.apply {
            progressiveMode = true
            enableLanguageFeature(LanguageFeature.InlineClasses.name)
            optIn("kotlin.RequiresOptIn")
            optIn("kotlin.ExperimentalStdlibApi")
            optIn("kotlin.ExperimentalUnsignedTypes")
            optIn("kotlin.ExperimentalMultiplatform")
            optIn("kotlin.time.ExperimentalTime")
            optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
            optIn("kotlinx.serialization.ExperimentalSerializationApi")
            optIn("kotlin.js.ExperimentalJsExport")
        }
    }
}

spotless {
    val ktlintVersion = "0.42.1"

    kotlin {
        ktlint(ktlintVersion).userData(mapOf("disabled_rules" to "no-wildcard-imports"))
        targetExclude("$buildDir/**/*.kt", "bin/**/*.kt")
    }

    kotlinGradle {
        ktlint(ktlintVersion).userData(mapOf("disabled_rules" to "no-wildcard-imports"))
        target("*.gradle.kts")
    }

    format("misc") {
        target("**/*.md", "**/.gitignore")
        trimTrailingWhitespace()
        endWithNewline()
    }
}

redacted {
    redactedAnnotation.set("Redacted")
    enabled.set(true)
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
        kotlinOptions.freeCompilerArgs += listOf("-Xir-per-module")
    }

    wrapper {
        gradleVersion = "7.2"
        distributionType = Wrapper.DistributionType.ALL
    }

    // Default task
    defaultTasks("clean", "tasks", "--all")
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.0")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.3.0")
    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.4")
    implementation("org.jetbrains:markdown:0.2.4")
    implementation("org.jetbrains.kotlinx:kotlinx-html:0.7.3")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-styled:5.3.1-pre.251-kotlin-1.5.31")

    val ktorVersion = "1.6.3"
    implementation("io.ktor:ktor-client-js:$ktorVersion")
    implementation("io.ktor:ktor-client-websockets:$ktorVersion")

    implementation("app.softwork:kotlinx-uuid-core:0.0.11")
    // implementation("app.softwork:routing-compose:0.0.31")
    // implementation("io.github.artemmey:compose-jb-routing:0.9.2-a2")
    // implementation("moe.tlaster:precompose:0.2.2")

    implementation("io.rsocket.kotlin:rsocket-core:0.13.1")
    implementation("io.rsocket.kotlin:rsocket-transport-ktor-client:0.13.1")
    implementation("com.russhwolf:multiplatform-settings:0.8.1")
    implementation("net.mamoe.yamlkt:yamlkt:0.10.2")

    implementation("io.github.microutils:kotlin-logging:2.0.11")
    implementation("com.github.h0tk3y.betterParse:better-parse:0.4.2")
    implementation("com.benasher44:uuid:0.3.1")
    implementation("io.github.petertrr:kotlin-multiplatform-diff:0.2.0")
    implementation("com.ionspin.kotlin:bignum:0.3.2")

    implementation("com.github.ajalt.colormath:colormath:3.1.1")
    // implementation("com.github.ajalt.mordant:mordant:2.0.0-beta2")
    // implementation("com.github.ajalt.clikt:clikt:3.2.0")

    implementation(npm("kotlin-playground", "1.25.0"))
    implementation(npm("highlight.js", "11.2.0"))
    implementation(npm("xterm", "4.14.1"))

    testImplementation(kotlin("test-js"))

    // implementation("org.jetbrains.lets-plot:lets-plot-kotlin:3.0.3-alpha1")
    // implementation("com.github.alllex:parsus:v0.1.2")
    // implementation("co.touchlab:kermit:0.1.8")
    // implementation("com.soywiz.korlibs.klock:klock:2.0.7")
    // implementation("dev.inmo:krontab:0.5.1")
    // implementation("com.badoo.reaktive:reaktive:1.1.22")
    // implementation("app.moviebase:tmdb-api:0.4.0")
    // implementation("org.hildan.chrome:chrome-devtools-kotlin:1.3.0-873728")
    // implementation("dev.zacsweers.redacted:redacted-compiler-plugin-annotations:0.8.0")
    // implementation("com.michael-bull.kotlin-retry:kotlin-retry:1.0.8")

    // implementation(npm("vega-lite", "5.1.0",generateExternals = true))
    // implementation(npm("@types/crypto-js", "4.0.1", generateExternals = true))
    // implementation(devNpm("webpack-bundle-analyzer", "4.4.0"))
}
