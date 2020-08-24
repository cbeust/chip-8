@file:Suppress("MayBeConstant")

fun DependencyHandler.impl(vararg dep: Any) = dep.forEach { implementation(it) }
fun DependencyHandler.testImpl(vararg dep: Any) = dep.forEach { testImplementation(it) }

plugins {
    java
    id("org.jetbrains.kotlin.jvm") version "1.3.72"
    application
    id("org.openjfx.javafxplugin") version "0.0.8"
}

repositories {
    jcenter()
    mavenCentral()
    maven { setUrl("https://plugins.gradle.org/m2") }
}

defaultTasks(ApplicationPlugin.TASK_RUN_NAME)

object This {
    val artifactId = "chip8"
}

dependencies {
    impl(kotlin("stdlib"), "com.beust:jcommander:1.72")
    testImpl(kotlin("test"), "org.testng:testng:7.0.0", "org.assertj:assertj-core:3.10.0")
}

application {
    mainClassName = "com.beust.chip8.MainKt"
}

tasks {
    withType<Test> {
        useTestNG()
    }
}

javafx {
    version = "14"
    modules = listOf("javafx.controls", "javafx.fxml")
}
