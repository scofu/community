plugins {
    id("paper-conventions")
}

dependencies {
    api(project(":community-api"))
    api("com.scofu:chat-bukkit:1.0-SNAPSHOT")
    api("com.scofu:mojang-profile-api:1.0-SNAPSHOT")
    api("com.scofu:design-bukkit:1.0-SNAPSHOT")
    api("com.scofu:command-standard:1.0-SNAPSHOT")
}