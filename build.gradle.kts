import org.intellij.jewel.workshop.build.patchLafFile
import org.intellij.jewel.workshop.build.patchRegistryFile
import org.jetbrains.intellij.tasks.PrepareSandboxTask
import java.nio.file.Paths

plugins {
    kotlin("jvm") version "1.9.21"
    id("org.jetbrains.intellij") version "1.16.1"
    id("org.jetbrains.compose") version "1.5.11"
    kotlin("plugin.serialization") version "1.9.21"
}

group = "org.jetbrains.jewel"
version = "1.0-SNAPSHOT"

intellij {
    version = "2023.2.5"
    instrumentCode = false
}

repositories {
    mavenCentral()
    maven("https://packages.jetbrains.team/maven/p/kpm/public")
    maven("https://packages.jetbrains.team/maven/p/grazi/grazie-platform-public/")

}

configurations.all {
    exclude("org.jetbrains.compose.material")
}

kotlin {
    sourceSets {
        all {
            languageSettings {
                optIn("org.jetbrains.jewel.foundation.ExperimentalJewelApi")
            }
        }
    }
}

dependencies {
    implementation("org.jetbrains.jewel:jewel-ide-laf-bridge:0.8.1-ij-232")
    implementation("org.jetbrains.jewel:jewel-int-ui-standalone:0.8.1")
    implementation(compose.desktop.currentOs)
    implementation("io.lacuna:bifurcan:0.2.0-alpha7")
    implementation("ai.grazie.api:api-gateway-client-jvm:0.3.62"){
        exclude("org.slf4j")
    }
    implementation("ai.grazie.client:client-ktor-jvm:0.3.62"){
        exclude("org.slf4j")
    }
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("dev.langchain4j:langchain4j-open-ai:0.30.0")
    implementation("io.lacuna:bifurcan:0.2.0-alpha7")

}

tasks {
    val enableNewUi by registering {
        dependsOn(prepareSandbox)

        // disable me to enable old ui :)
        onlyIf { true }

        doLast {
            prepareSandbox.registryFile.get().patchRegistryFile()
            prepareSandbox.lafFile.get().patchLafFile()
        }
    }
    runIde {
        dependsOn(enableNewUi)
    }
}

kotlin {
    jvmToolchain(17)

    sourceSets {
        all {
            languageSettings {
                optIn("org.jetbrains.jewel.ExperimentalJewelApi")
                optIn("androidx.compose.ui.ExperimentalComposeUiApi")
            }
        }
    }
}

val TaskProvider<PrepareSandboxTask>.registryFile: Provider<File>
    get() = flatMap { it.configDir }
        .map { Paths.get(it).resolve("early-access-registry.txt").toFile() }

val TaskProvider<PrepareSandboxTask>.lafFile: Provider<File>
    get() = flatMap { it.configDir }
        .map { Paths.get(it).resolve("options/laf.xml").toFile() }
