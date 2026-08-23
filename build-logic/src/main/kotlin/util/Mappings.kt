package util

import org.gradle.api.Project

/**
 * Exact Spigot NMS versions the wrappers were written against (from pre-Gradle Maven POMs).
 * Patch mismatches (e.g. 1.18 vs 1.18.1) change obfuscation and break compilation.
 */
private val spigotVersions = mapOf(
    "1_8_R1" to "1.8",
    "1_8_R2" to "1.8.3",
    "1_8_R3" to "1.8.8",
    "1_9_R1" to "1.9",
    "1_9_R2" to "1.9.4",
    "1_10_R1" to "1.10.2",
    "1_11_R1" to "1.11",
    "1_12_R1" to "1.12",
    "1_13_R1" to "1.13",
    "1_13_R2" to "1.13.1",
    "1_14_R1" to "1.14",
    "1_15_R1" to "1.15",
    "1_16_R1" to "1.16.1",
    "1_16_R2" to "1.16.2",
    "1_16_R3" to "1.16.4",
    "1_17_R1" to "1.17",
    "1_18_R1" to "1.18",
    "1_18_R2" to "1.18.2",
    "1_19_R1" to "1.19",
    "1_19_R2" to "1.19.3",
    "1_19_R3" to "1.19.4",
    "1_20_R1" to "1.20",
    "1_20_R2" to "1.20.2",
    "1_20_R3" to "1.20.3",
    "1_20_R4" to "1.20.5",
    "1_21_R1" to "1.21.1",
    "1_21_R2" to "1.21.3",
    "1_21_R3" to "1.21.4",
    "1_21_R4" to "1.21.5",
    "1_21_R5" to "1.21.6",
    "1_21_R6" to "1.21.10",
    "1_21_R7" to "1.21.11",
    "26_1" to "26.1.2",
    "26_2" to "26.2",
)

/** Paperweight / Spigot Maven version for the current NMS module project name. */
fun Project.mcVersion(): String {
    val projectName = project.name.replace("Mojang", "")
    return spigotVersions[projectName]
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
        // Paper-nms used 1.21.6; 1.21.7/1.21.8 share the same CraftBukkit revision.
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
