import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.spring") version "1.9.22"
    id("org.springframework.boot") version "3.1.8"
    id("io.spring.dependency-management") version "1.1.4"
}

group = "org.abiri"
version = "1.0"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("com.github.ajalt.clikt:clikt:4.2.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "17"
    }
}

tasks.withType<BootJar> {
    launchScript {
        script = file("src/launchscript.sh")
    }
    archiveFileName = "stomp"
}
