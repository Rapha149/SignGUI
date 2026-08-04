plugins {
    `java-library`
    `maven-publish`
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = rootProject.group.toString()
            artifactId = "signgui"
            version = rootProject.version.toString()
            artifact(tasks.named("shadowJar")) {
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

listOf(
    "publishMavenPublicationToReposiliteReleasesRepository",
    "publishAllPublicationsToReposiliteReleasesRepository",
).forEach { taskName ->
    tasks.matching { it.name == taskName }.configureEach {
        onlyIf { !publishingSnapshot }
    }
}

listOf(
    "publishMavenPublicationToReposiliteSnapshotsRepository",
    "publishAllPublicationsToReposiliteSnapshotsRepository",
).forEach { taskName ->
    tasks.matching { it.name == taskName }.configureEach {
        onlyIf { publishingSnapshot }
    }
}

tasks.named("publish") {
    group = "publishing"
    description = "Publish fat jar to MasterSMP Reposilite releases"
    dependsOn(tasks.named("shadowJar"))
}

tasks.register("publishSnapshot") {
    group = "publishing"
    description = "Publish fat jar to MasterSMP Reposilite snapshots"
    dependsOn(tasks.named("shadowJar"), "publishMavenPublicationToReposiliteSnapshotsRepository")
}

tasks.register("printVersion") {
    doLast {
        println(version)
    }
}
