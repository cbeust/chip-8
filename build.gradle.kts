@file:Suppress("MayBeConstant")

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

buildscript {
    repositories {
        jcenter()
        mavenCentral()
        maven { setUrl("https://plugins.gradle.org/m2") }
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.72")
    }
}

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
    val version = "0.1"
    val groupId = "com.beust"
    val artifactId = "chip8"
    val description = "A CHIP8 emulator"
    val url = "https://github.com/cbeust/chip8"
    val scm = "github.com/cbeust/chip8.git"

    // Should not need to change anything below
    val issueManagementUrl = "https://$scm/issues"
    val isSnapshot = version.contains("SNAPSHOT")
}

allprojects {
    group = This.groupId
    version = This.version
}

dependencies {
    listOf(kotlin("stdlib"), "com.beust:jcommander:1.72").forEach {
        implementation(it)
    }

    listOf(kotlin("test"), "org.testng:testng:7.0.0", "org.assertj:assertj-core:3.10.0").forEach {
        testImplementation(it)
    }
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
//        excludes = listOf("META-INF/*.DSA", "META-INF/*.RSA", "META-INF/*.SF")
        manifest {
            attributes(mapOf(
                    "Implementation-Title" to rootProject.name,
                    "Implementation-Version" to rootProject.version,
                    "Implementation-Vendor-Id" to rootProject.group,
                    //        attributes "Build-Time": ZonedDateTime.now(ZoneId.of("UTC"))
                    //                .format(DateTimeFormatter.ISO_ZONED_DATE_TIME)
//                    "Built-By" to java.net.InetAddress.localHost.hostName,
                    "Created-By" to "Gradle "+ gradle.gradleVersion,
                    "Main-Class" to "com.beust.cedlinks.MainKt"))
        }
    }
}

javafx {
    version = "14"
    modules = listOf("javafx.controls", "javafx.fxml")
}
