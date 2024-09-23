import org.jetbrains.dokka.gradle.DokkaTask
import java.net.URI

plugins {
    kotlin("jvm") version "2.0.20"
    id("org.jetbrains.dokka") version "1.9.20"
}

group = "com.aetherui"
version = "0.1.0-alpha"

kotlin {
    jvmToolchain(21)
}

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

tasks.withType<DokkaTask>().configureEach {
    moduleName.set("AetherUI")

    dokkaSourceSets.configureEach {
        jdkVersion.set(21)

        sourceLink {
            localDirectory.set(projectDir.resolve("src"))
            remoteUrl.set(URI("https://github.com/aethersim/aetherui/tree/main/src").toURL())
            remoteLineSuffix.set("#L")
        }
    }
}
