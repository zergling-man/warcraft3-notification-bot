group = "au.rakka.java.mastoapi"

val jackson_version: String by project
val logback_version: String by project

repositories {
	mavenCentral()
}

plugins {
	alias(libs.plugins.kotlin)
}

dependencies {
	implementation(project(":module-api"))
	implementation(project(":utilities"))
	implementation(libs.coroutines)
	implementation(libs.guice)
	implementation("ch.qos.logback:logback-classic:$logback_version")
	implementation("com.fasterxml.jackson.core:jackson-databind:$jackson_version")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jackson_version")
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jackson_version")
}

kotlin {
	explicitApi()
}