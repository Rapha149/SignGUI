val java16 = setOf("1_17_R1")

val java17 = setOf(
    "1_18_R1", "1_18_R2",
    "1_19_R1", "1_19_R2", "1_19_R3",
    "1_20_R1", "1_20_R2", "1_20_R3",
)

val java21 = setOf(
    "1_20_R4",
    "1_21_R1", "1_21_R2", "1_21_R3", "1_21_R4", "1_21_R5", "1_21_R6", "1_21_R7",
    "Mojang1_20_R4",
    "Mojang1_21_R1", "Mojang1_21_R2", "Mojang1_21_R3", "Mojang1_21_R4", "Mojang1_21_R5",
    "Mojang1_21_R6", "Mojang1_21_R7",
    "wrapper", "api",
)

val java25 = setOf(
    "Mojang26_1", "Mojang26_2",
)

plugins {
    `java-library`
}

java {
    toolchain {
        val version = when (project.name) {
            in java25 -> 25
            in java21 -> 21
            in java17 -> 17
            in java16 -> 16
            else -> 8
        }
        languageVersion.set(JavaLanguageVersion.of(version))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    when (project.name) {
        in java25 -> options.release.set(21)
        in java21 -> options.release.set(21)
        in java17 -> options.release.set(17)
        in java16 -> options.release.set(16)
        else -> options.release.set(8)
    }
}
