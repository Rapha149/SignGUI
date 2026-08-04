import util.mcVersion

plugins {
    id("module-java-versions")
}

repositories {
    mavenCentral()
    maven("https://repo.codemc.io/repository/nms/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
}

dependencies {
    compileOnly("org.spigotmc:spigot:${mcVersion()}-R0.1-SNAPSHOT")
    compileOnly(project(":wrapper"))
}
