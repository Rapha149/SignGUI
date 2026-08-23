import io.papermc.paperweight.userdev.ReobfArtifactConfiguration
import util.mojangEra
import util.paperDevBundleVersion

plugins {
    id("module-java-versions")
    id("io.papermc.paperweight.userdev")
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

val bundleVersion = paperDevBundleVersion()

dependencies {
    paperweight.paperDevBundle(bundleVersion)
    // paperweight's mapped server alone is not always enough for Bukkit/Netty API compile.
    compileOnly("io.papermc.paper:paper-api:$bundleVersion")
    compileOnly("io.netty:netty-transport:4.1.118.Final")
    compileOnly("io.netty:netty-handler:4.1.118.Final")
    compileOnly(project(":wrapper"))
}

paperweight.reobfArtifactConfiguration = ReobfArtifactConfiguration.MOJANG_PRODUCTION

tasks.matching { it.name == "reobfJar" }.configureEach {
    enabled = false
}

configurations.configureEach {
    if (name.contains("reobf", ignoreCase = true)) {
        outgoing.artifacts.clear()
        outgoing.artifact(tasks.named("jar"))
    }
}

tasks.jar {
    archiveClassifier.set("")
}

val eraDirName = mojangEra()
if (eraDirName != null) {
    val sharedOut = layout.buildDirectory.dir("generated-shared")
    val bucketSuffix = project.name.removePrefix("Mojang")
    val wrapperClassName = "MojangWrapper$bucketSuffix"

    val prepareSharedSources = tasks.register("prepareSharedSources") {
        val srcRoot = rootProject.file("$eraDirName/src/main/java")
        inputs.dir(srcRoot)
        inputs.property("wrapperClassName", wrapperClassName)
        outputs.dir(sharedOut)
        doLast {
            val outRoot = sharedOut.get().asFile
            outRoot.deleteRecursively()
            val fromDir = File(srcRoot, "de/rapha149/signgui/version")
            if (!fromDir.isDirectory) {
                throw GradleException("Missing shared sources: ${fromDir.absolutePath}")
            }
            fromDir.walkTopDown().filter { it.isFile && it.extension == "java" }.forEach { file ->
                val destName = file.name.replace("MojangWrapperShared", wrapperClassName)
                val dest = File(outRoot, "de/rapha149/signgui/version/$destName")
                dest.parentFile.mkdirs()
                dest.writeText(file.readText().replace("MojangWrapperShared", wrapperClassName))
            }
        }
    }

    sourceSets.named("main") {
        java {
            setSrcDirs(listOf(sharedOut, file("src/main/java")))
        }
    }

    tasks.named("compileJava") {
        dependsOn(prepareSharedSources)
    }

    tasks.named("jar") {
        dependsOn(prepareSharedSources)
    }
}
