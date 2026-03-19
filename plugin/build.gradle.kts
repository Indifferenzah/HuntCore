plugins {
    java
    id("com.gradleup.shadow") version "9.0.0-beta4"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
}

dependencies {
    implementation(project(":api"))
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    implementation("io.github.revxrsal:lamp.common:4.0.0-rc.16")
    implementation("io.github.revxrsal:lamp.bukkit:4.0.0-rc.16")
    implementation("com.h2database:h2:2.2.224")
    implementation("com.zaxxer:HikariCP:5.1.0")
}

tasks.shadowJar {
    archiveBaseName.set("HuntCore")
    archiveVersion.set("1.0.0")
    archiveClassifier.set("")
    relocate("io.github.revxrsal.lamp", "com.indifferenzah.huntcore.lib.lamp")
    relocate("org.h2", "com.indifferenzah.huntcore.lib.h2")
    relocate("com.zaxxer.hikari", "com.indifferenzah.huntcore.lib.hikari")
    from(project(":api").sourceSets.main.get().output)
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
