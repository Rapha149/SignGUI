import util.mcVersion

plugins {
    id("module-java-versions")
}

repositories {
    // CodeMC fat NMS jars must win over Spigot Hub POM-only artifacts (e.g. 1.21.1).
    exclusiveContent {
        forRepository {
            maven("https://repo.codemc.io/repository/nms/")
        }
        forRepository {
            maven("https://repo.codemc.org/repository/nms/")
        }
        filter {
            includeModule("org.spigotmc", "spigot")
        }
    }
}

dependencies {
    compileOnly("org.spigotmc:spigot:${mcVersion()}-R0.1-SNAPSHOT")
    compileOnly(project(":wrapper"))
}
