plugins {
    id("com.scofu.common-build.bukkit") version "1.0-SNAPSHOT"
}

dependencies {
    api(project(":community-api"))
    api("com.scofu:chat-bukkit:1.0-SNAPSHOT")
    api("com.scofu:mojang-profile-api:1.0-SNAPSHOT")
    api("com.scofu:design-bukkit:1.0-SNAPSHOT")
    api("com.scofu:network-instance-bukkit:1.0-SNAPSHOT")
    api("com.scofu:command-standard:1.0-SNAPSHOT")
}