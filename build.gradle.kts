plugins {
    `java-library`
    alias(libs.plugins.shadow)
    `maven-publish`
}

allprojects {
    group = rootProject.property("group") as String
    version = rootProject.property("version") as String

    repositories {
        mavenCentral()
        mavenLocal()
        maven("https://repo.papermc.io/repository/maven-public/")
        // Prefer CodeMC NMS jars over Spigot Hub POM-only coordinates.
        maven("https://repo.codemc.io/repository/nms/")
        maven("https://repo.codemc.org/repository/nms/")
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
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

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "signgui"
            artifact(tasks.shadowJar) {
                classifier = null
            }
            pom {
                name.set("SignGUI")
                description.set("An api to get input text via a sign in Minecraft (1.8–26.x).")
                url.set("https://github.com/OPmasterLEO/SignGUI")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("http://www.opensource.org/licenses/mit-license.php")
                    }
                }
                developers {
                    developer {
                        id.set("rapha149")
                        name.set("Rapha149")
                    }
                    developer {
                        id.set("opmasterleo")
                        name.set("OPmasterLEO")
                    }
                }
                withXml {
                    asNode().appendNode("dependencies")
                }
            }
        }
    }
    repositories {
        fun org.gradle.api.artifacts.repositories.MavenArtifactRepository.reposiliteAuth() {
            isAllowInsecureProtocol = true
            credentials {
                username = project.findProperty("reposilite.user") as String?
                    ?: System.getenv("REPOSILITE_USER")
                password = project.findProperty("reposilite.token") as String?
                    ?: System.getenv("REPOSILITE_TOKEN")
            }
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
        maven {
            name = "ReposiliteReleases"
            url = uri("http://repo.mastersmp.net/releases")
            reposiliteAuth()
        }
        maven {
            name = "ReposiliteSnapshots"
            url = uri("http://repo.mastersmp.net/snapshots")
            reposiliteAuth()
        }
    }
}

val publishingSnapshot = gradle.startParameter.taskNames.any {
    it == "publishSnapshot" || it.endsWith(":publishSnapshot")
}

tasks.named("publishMavenPublicationToReposiliteReleasesRepository").configure {
    onlyIf { !publishingSnapshot }
}

tasks.named("publishMavenPublicationToReposiliteSnapshotsRepository").configure {
    onlyIf { publishingSnapshot }
}

tasks.named("publishAllPublicationsToReposiliteReleasesRepository").configure {
    onlyIf { !publishingSnapshot }
}

tasks.named("publishAllPublicationsToReposiliteSnapshotsRepository").configure {
    onlyIf { publishingSnapshot }
}

tasks.named("publish").configure {
    group = "publishing"
    description = "Publish fat jar to MasterSMP Reposilite releases"
    dependsOn(tasks.shadowJar)
}

tasks.register("publishSnapshot") {
    group = "publishing"
    description = "Publish fat jar to MasterSMP Reposilite snapshots"
    dependsOn(tasks.shadowJar, "publishMavenPublicationToReposiliteSnapshotsRepository")
}

tasks.named("publishToMavenLocal").configure {
    dependsOn(tasks.shadowJar)
}

tasks.register("printVersion") {
    doLast {
        println(version)
    }
}
