plugins {
    `java-library`
    alias(libs.plugins.shadow)
    id("mastersmp-publish")
}

allprojects {
    group = rootProject.property("group") as String
    version = rootProject.property("version") as String

    repositories {
        mavenCentral()
        mavenLocal()
        maven("https://repo.codemc.io/repository/nms/")
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

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

dependencies {
    implementation(project(":wrapper"))
    implementation(project(":api"))
    spigotBuckets.forEach { implementation(project(":$it")) }
    mojangBuckets.forEach { implementation(project(":$it")) }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.jar {
    enabled = false
    dependsOn(tasks.shadowJar)
}

tasks.shadowJar {
    archiveBaseName.set("signgui")
    archiveClassifier.set("")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    exclude(
        "META-INF/*.SF",
        "META-INF/*.DSA",
        "META-INF/*.RSA",
        "META-INF/maven/**",
    )
    mergeServiceFiles()
}

listOf("apiElements", "runtimeElements").forEach { configName ->
    configurations.named(configName).configure {
        outgoing.artifacts.clear()
        outgoing.artifact(tasks.shadowJar)
    }
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
