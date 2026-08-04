import util.mcVersion

plugins {
    id("module-java-versions")
}

repositories {
    mavenCentral()
    // Prefer CodeMC fat NMS jars over Spigot Hub POMs (which lack remapped server classes).
    exclusiveContent {
        forRepository {
            maven("https://repo.codemc.io/repository/nms/")
        }
        filter {
            includeGroup("org.spigotmc")
        }
    }
    exclusiveContent {
        forRepository {
            maven("https://repo.codemc.org/repository/nms/")
        }
        filter {
            includeGroup("org.spigotmc")
        }
    }
}

dependencies {
    compileOnly("org.spigotmc:spigot:${mcVersion()}-R0.1-SNAPSHOT")
    compileOnly(project(":wrapper"))
}
