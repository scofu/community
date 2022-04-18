plugins {
    id("com.scofu.common-build.base") version "1.0-SNAPSHOT"
}

dependencies {
    api(project(":community-api"))
    implementation("com.scofu:app-bootstrap-api:1.0-SNAPSHOT")
    testImplementation("com.scofu:app-bootstrap-api:1.0-SNAPSHOT")
}

app {
    shadowFirstLevel();
    mainClass.set("com.scofu.community.service.CommunityService")
}