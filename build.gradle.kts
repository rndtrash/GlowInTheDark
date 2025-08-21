plugins {
    kotlin("jvm") version "2.2.10"
    id("com.gradleup.shadow") version "9.0.2"
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("xyz.jpenilla.resource-factory-paper-convention") version "1.3.0"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.18"
}

group = "ru.teasanctuary"
version = "1.0-SNAPSHOT"
description = "A plugin that logs the user interactions in a parsable format"

val mcVersion = "1.21.8"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc-repo"
    }
    maven("https://oss.sonatype.org/content/groups/public/") {
        name = "sonatype"
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:${mcVersion}-R0.1-SNAPSHOT")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    paperweight.paperDevBundle("${mcVersion}-R0.1-SNAPSHOT")
}

val targetJavaVersion = 21
kotlin {
    jvmToolchain(targetJavaVersion)
}

tasks.build {
    dependsOn("shadowJar")
}

tasks.withType(xyz.jpenilla.runtask.task.AbstractRun::class) {
    javaLauncher = javaToolchains.launcherFor {
        vendor = JvmVendorSpec.JETBRAINS
        languageVersion = JavaLanguageVersion.of(21)
    }
    jvmArgs("-XX:+AllowEnhancedClassRedefinition")
}

tasks.runServer {
    minecraftVersion(mcVersion)
}

paperPluginYaml {
    main = "ru.teasanctuary.gitd.Gitd"
    prefix = "GITD"
    authors.add("rndtrash")
    website = "https://teasanctuary.ru"
    apiVersion = "1.21.8"
}