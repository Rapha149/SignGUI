plugins {
    id("module-java-versions")
}

dependencies {
    api(project(":wrapper"))
    compileOnly("org.spigotmc:spigot:1.8-R0.1-SNAPSHOT")
    compileOnly(libs.folia.api)
}
