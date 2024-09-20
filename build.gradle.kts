plugins {
    kotlin("jvm") version "2.0.20"
    id("org.jetbrains.dokka") version "1.9.20"
}

group = "com.aetherui"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.11.0")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}
