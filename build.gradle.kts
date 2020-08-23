@file:Suppress("MayBeConstant")

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

fun DependencyHandler.impl(vararg dep: Any) = dep.forEach { implementation(it) }
fun DependencyHandler.testImpl(vararg dep: Any) = dep.forEach { testImplementation(it) }

plugins {
    java
    id("org.jetbrains.kotlin.jvm") version "1.3.72"
    application
    id("com.github.johnrengelman.shadow") version "5.2.0"
    id("org.openjfx.javafxplugin") version "0.0.8"
}

repositories {
    jcenter()
    mavenCentral()
    maven { setUrl("https://plugins.gradle.org/m2") }
}

object This {
    val artifactId = "chip8"
}

dependencies {
    impl(kotlin("stdlib"), "com.beust:jcommander:1.72")
    testImpl(kotlin("test"), "org.testng:testng:7.0.0", "org.assertj:assertj-core:3.10.0")
}

val test by tasks.getting(Test::class) {
    useTestNG()
}

application {
    mainClassName = "com.beust.chip8.MainKt"
}

tasks {
    named<ShadowJar>("shadowJar") {
        archiveBaseName.set(This.artifactId)
        mergeServiceFiles()
        manifest {
            attributes(mapOf(
                "Implementation-Title" to rootProject.name,
                "Implementation-Version" to rootProject.version,
                "Implementation-Vendor-Id" to rootProject.group,
                "Created-By" to "Gradle "+ gradle.gradleVersion,
                    "Main-Class" to "com.beust.cedlinks.MainKt"))
        }
    }
}

javafx {
    version = "14"
    modules = listOf("javafx.controls", "javafx.fxml")
}
