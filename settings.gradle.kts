plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "wc3-notification-bot"

include("discord-module")
include("module-api")
include("utilities")
include("masto-module")