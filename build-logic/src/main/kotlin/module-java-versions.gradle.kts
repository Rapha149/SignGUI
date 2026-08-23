import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType

plugins {
    `java-library`
}

val modulesJava16 = setOf("1_17_R1")

val modulesJava17 = setOf(
    "1_18_R1", "1_18_R2",
    "1_19_R1", "1_19_R2", "1_19_R3",
    "1_20_R1", "1_20_R2", "1_20_R3",
)

val modulesJava21 = setOf(
    "1_20_R4",
    "1_21_R1", "1_21_R2", "1_21_R3", "1_21_R4", "1_21_R5", "1_21_R6", "1_21_R7",
    "Mojang1_20_R4",
    "Mojang1_21_R1", "Mojang1_21_R2", "Mojang1_21_R3", "Mojang1_21_R4", "Mojang1_21_R5",
    "Mojang1_21_R6", "Mojang1_21_R7",
    "api",
)

val modulesJava25 = setOf(
    "Mojang26_1", "Mojang26_2",
)

// Shared by every Spigot/Mojang bucket (including Java 8). Compile with a modern
// toolchain so Spigot 1.17+ jars are readable, but emit Java 8 bytecode / attributes.
val sharedApiJava8 = setOf("wrapper")

configure<JavaPluginExtension> {
    toolchain {
        val version = when (project.name) {
            in sharedApiJava8 -> 21
            in modulesJava25 -> 25
            in modulesJava21 -> 21
            in modulesJava17 -> 17
            in modulesJava16 -> 16
            else -> 8
        }
        languageVersion.set(JavaLanguageVersion.of(version))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    // --release needs javac 9+; Java 8 toolchains inherit bytecode level from the toolchain.
    when (project.name) {
        in sharedApiJava8 -> options.release.set(8)
        // Must match toolchain: paper-api 26.x requires JVM 25 attributes.
        in modulesJava25 -> options.release.set(25)
        in modulesJava21 -> options.release.set(21)
        in modulesJava17 -> options.release.set(17)
        in modulesJava16 -> options.release.set(16)
    }
}
