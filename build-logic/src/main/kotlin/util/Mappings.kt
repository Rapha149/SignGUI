package util

import org.gradle.api.Project

private val versionExceptions = mapOf(
    "1_8_R1" to "1.8",
    "1_8_R2" to "1.8.3",
    "1_8_R3" to "1.8.8",
    "1_9_R1" to "1.9",
    "1_9_R2" to "1.9.4",
    "1_10_R1" to "1.10.2",
    "1_11_R1" to "1.11",
    "1_12_R1" to "1.12",
    "1_13_R1" to "1.13",
    "1_13_R2" to "1.13.2",
    "1_14_R1" to "1.14",
    "1_15_R1" to "1.15",
    "1_16_R1" to "1.16.1",
    "1_16_R2" to "1.16.3",
    "1_16_R3" to "1.16.5",
    "1_17_R1" to "1.17.1",
    "1_18_R1" to "1.18.1",
    "1_18_R2" to "1.18.2",
    "1_19_R1" to "1.19.2",
    "1_19_R2" to "1.19.3",
    "1_19_R3" to "1.19.4",
    "1_20_R1" to "1.20.1",
    "1_20_R2" to "1.20.2",
    "1_20_R3" to "1.20.4",
    "1_20_R4" to "1.20.6",
    "1_21_R1" to "1.21.1",
    "1_21_R2" to "1.21.3",
    "1_21_R3" to "1.21.4",
    "1_21_R4" to "1.21.5",
    "1_21_R5" to "1.21.8",
    "1_21_R6" to "1.21.10",
    "1_21_R7" to "1.21.11",
    "26_1" to "26.1.2",
    "26_2" to "26.2",
)

/** Paperweight / Spigot Maven version for the current NMS module project name. */
fun Project.mcVersion(): String {
    val projectName = project.name.replace("Mojang", "")
    return versionExceptions[projectName]
        ?: error("No mcVersion mapping for project '${project.name}'")
}

/** Paperweight paperDevBundle coordinate version (may differ slightly from Spigot). */
fun Project.paperDevBundleVersion(): String {
    val projectName = project.name.replace("Mojang", "")
    return when (projectName) {
        "1_20_R4" -> "1.20.6-R0.1-SNAPSHOT"
        "1_21_R1" -> "1.21.1-R0.1-SNAPSHOT"
        "1_21_R2" -> "1.21.3-R0.1-SNAPSHOT"
        "1_21_R3" -> "1.21.4-R0.1-SNAPSHOT"
        "1_21_R4" -> "1.21.5-R0.1-SNAPSHOT"
        "1_21_R5" -> "1.21.8-R0.1-SNAPSHOT"
        "1_21_R6" -> "1.21.10-R0.1-SNAPSHOT"
        "1_21_R7" -> "1.21.11-R0.1-SNAPSHOT"
        "26_1" -> "26.1.2.build.+"
        "26_2" -> "26.2.build.+"
        else -> "${mcVersion()}-R0.1-SNAPSHOT"
    }
}

/**
 * Shared Mojang era directory for this module, or null when the module keeps hand-written sources
 * (API differences such as pale oak / early 1.21).
 */
fun Project.mojangEra(): String? {
    return when (project.name) {
        "Mojang26_1", "Mojang26_2" -> "shared-mojang-26"
        "Mojang1_21_R2", "Mojang1_21_R3", "Mojang1_21_R4", "Mojang1_21_R5",
        "Mojang1_21_R6", "Mojang1_21_R7" -> "shared-mojang-modern"
        else -> null
    }
}
