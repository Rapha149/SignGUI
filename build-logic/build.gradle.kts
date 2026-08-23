plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation(libs.paperweight.userdev)
}
