rootProject.name = "SignGUI"

pluginManagement {
    includeBuild("build-logic")
    repositories {
        maven("https://plugins.gradle.org/m2/")
        gradlePluginPortal()
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
    plugins {
        id("io.papermc.paperweight.userdev") version "2.0.0-beta.21"
        id("com.gradleup.shadow") version "9.6.0"
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(":wrapper")
include(":api")

val spigotBuckets = listOf(
    "1_8_R1", "1_8_R2", "1_8_R3",
    "1_9_R1", "1_9_R2",
    "1_10_R1",
    "1_11_R1",
    "1_12_R1",
    "1_13_R1", "1_13_R2",
    "1_14_R1",
    "1_15_R1",
    "1_16_R1", "1_16_R2", "1_16_R3",
    "1_17_R1",
    "1_18_R1", "1_18_R2",
    "1_19_R1", "1_19_R2", "1_19_R3",
    "1_20_R1", "1_20_R2", "1_20_R3", "1_20_R4",
    "1_21_R1", "1_21_R2", "1_21_R3", "1_21_R4", "1_21_R5", "1_21_R6", "1_21_R7",
)

val mojangBuckets = listOf(
    "Mojang1_20_R4",
    "Mojang1_21_R1", "Mojang1_21_R2", "Mojang1_21_R3", "Mojang1_21_R4", "Mojang1_21_R5",
    "Mojang1_21_R6", "Mojang1_21_R7",
    "Mojang26_1", "Mojang26_2",
)

spigotBuckets.forEach { include(":$it") }
mojangBuckets.forEach { include(":$it") }
