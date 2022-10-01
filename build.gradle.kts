@file:Suppress("PropertyName")

val kotlin_version by extra { "1.7.20" }
val spigot_version by extra { "1.18.2-R0.1-SNAPSHOT" }

buildscript {
    dependencies {
        classpath("org.xerial:sqlite-jdbc:3.39.3.0")
    }
}

plugins {
    application
    kotlin("jvm") version "1.7.20"
    id("org.flywaydb.flyway") version "9.3.0"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "com.nevakanezah"
version = "2.0.0"
application {
    mainClass.set("com.nevakanezah.horseenhancer.HorseEnhancerMain")
}

flyway {
    url = "jdbc:sqlite:database.sqlite3"
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }
}

dependencies {
    // Spigot
    compileOnly("org.spigotmc:spigot-api:$spigot_version")
    compileOnly("org.spigotmc:plugin-annotations:1.2.3-SNAPSHOT")
    annotationProcessor("org.spigotmc:plugin-annotations:1.2.3-SNAPSHOT")

    // CSV Reader
    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:1.6.0")

    // YAML Parser / Writer
    implementation("space.arim.dazzleconf:dazzleconf-ext-snakeyaml:1.3.0-M1")
    compileOnly("org.yaml:snakeyaml:1.30")

    // Database
    compileOnly("com.zaxxer:HikariCP:5.0.1")
    compileOnly("org.xerial:sqlite-jdbc:3.39.3.0")
    implementation("org.ktorm:ktorm-core:3.5.0")
    implementation("org.ktorm:ktorm-support-sqlite:3.5.0")
    implementation("org.flywaydb:flyway-core:9.3.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    compileOnly("com.github.shynixn.mccoroutine:mccoroutine-bukkit-api:2.5.0")
    compileOnly("com.github.shynixn.mccoroutine:mccoroutine-bukkit-core:2.5.0")

    testImplementation("org.spigotmc:spigot-api:$spigot_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}
