import org.jetbrains.kotlin.config.*
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.targets.js.nodejs.*
import org.jetbrains.kotlin.gradle.targets.js.npm.tasks.*
import org.jetbrains.kotlin.gradle.targets.js.yarn.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    // id("com.google.devtools.ksp") version "1.6.20-M1-1.0.2"
    kotlin("js") version "1.8.0"
    kotlin("plugin.serialization") version "1.8.0"
    id("com.github.ben-manes.versions") version "0.44.0"
    id("com.diffplug.spotless") version "6.13.0"
    id("dev.zacsweers.redacted") version "1.3.0"
    // id("com.github.turansky.kfc.library") version "4.50.0"
}

group = "dev.suresh"
version = "0.1.0"

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
        compilations.all { kotlinOptions {} }
        binaries.executable()
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
    val ktlintVersion = "0.43.2"

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

// Configure the Node and Yarn versions.
plugins.withType<NodeJsRootPlugin> {
    extensions.configure<NodeJsRootExtension> {
        // nodeVersion = "16.13.1"
    }
}

plugins.withType<YarnPlugin> {
    extensions.configure<YarnRootExtension> {
        // version = "1.22.17"
        lockFileDirectory = projectDir
        ignoreScripts = false
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
        kotlinOptions {
            // sourceMap = true
            // sourceMapEmbedSources = "always"
            freeCompilerArgs += listOf("-Xir-per-module")
        }
    }

    // Disable the execution of Yarn’s lifecycle scripts
    withType<KotlinNpmInstallTask> {
        args += "--ignore-scripts"
    }

    wrapper {
        gradleVersion = "7.5-rc-2"
        distributionType = Wrapper.DistributionType.ALL
    }

    // Default task
    defaultTasks("clean", "tasks", "--all")
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.5")
    implementation("org.jetbrains:markdown:0.3.5")
    implementation("org.jetbrains.kotlinx:kotlinx-html:0.8.1")

    // Kotlin wrappers
    implementation(enforcedPlatform(kotlinw("wrappers-bom:1.0.0-pre.345")))
    implementation(kotlinw("styled"))

    implementation(enforcedPlatform("io.ktor:ktor-bom:2.2.2"))
    implementation("io.ktor:ktor-client-js")
    implementation("io.ktor:ktor-client-websockets")

    implementation("app.softwork:kotlinx-uuid-core:0.0.17")
    // implementation("app.softwork:routing-compose:0.1.0")
    // implementation("io.github.artemmey:compose-jb-routing:0.9.2-a2")
    // implementation("moe.tlaster:precompose:0.2.2")
    // implementation("cafe.adriel.voyager:voyager-core-desktop:1.0.0")

    implementation("io.rsocket.kotlin:rsocket-ktor-client:0.15.4")
    implementation("com.russhwolf:multiplatform-settings:1.0.0-alpha01")
    implementation("net.mamoe.yamlkt:yamlkt:0.12.0")

    implementation("io.github.microutils:kotlin-logging:2.1.23")
    implementation("com.github.h0tk3y.betterParse:better-parse:0.4.4")
    implementation("com.benasher44:uuid:0.6.0")
    implementation("io.github.petertrr:kotlin-multiplatform-diff:0.4.0")
    implementation("com.ionspin.kotlin:bignum:0.3.7")

    implementation("com.github.ajalt.colormath:colormath:3.2.1")
    // implementation("com.github.ajalt.mordant:mordant:2.0.0-beta2")
    // implementation("com.github.ajalt.clikt:clikt:3.2.0")

    // Charts
    implementation("org.jetbrains.lets-plot:lets-plot-kotlin-js:3.2.0")
    implementation("space.kscience:plotlykt-core:0.5.0")

    implementation(npm("kotlin-playground", "1.27.2"))
    implementation(npm("highlight.js", "11.5.1"))
    implementation(npm("xterm", "4.18.0"))

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

fun kotlinw(target: String): String = "org.jetbrains.kotlin-wrappers:kotlin-$target"
