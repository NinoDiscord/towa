// modified version of Kord's BOM
// credit: https://github.com/kordlib/kord/blob/0.8.x/bom/build.gradle.kts

import dev.floofy.utils.gradle.*
import sh.nino.towa.gradle.*
import java.io.StringReader
import java.util.Properties

plugins {
    `maven-publish`
    `java-platform`
}

// Find the `publishing.properties` file from the `gradle/` directory of the
// root project.
val publishingConfigFile = file("${rootProject.projectDir}/gradle/publishing.properties")
val properties = Properties()

if (publishingConfigFile.exists()) {
    properties.load(publishingConfigFile.inputStream())
} else {
    // Check if we do in environment variables
    val accessKey = System.getenv("NINO_PUBLISHING_ACCESS_KEY") ?: ""
    val secretKey = System.getenv("NINO_PUBLISHING_SECRET_KEY") ?: ""

    if (accessKey.isNotEmpty() && secretKey.isNotEmpty()) {
        val data = """
        |s3.accessKey=$accessKey
        |s3.secretKey=$secretKey
        """.trimMargin()

        properties.load(StringReader(data))
    }
}

val snapshotRelease: Boolean = run {
    val env = System.getenv("NINO_PUBLISHING_IS_SNAPSHOT") ?: "false"
    env == "true"
}

dependencies {
    constraints {
        rootProject.subprojects.forEach { subproject ->
            if (subproject.plugins.hasPlugin("maven-publish") && subproject.name != name) {
                subproject.publishing.publications.withType<MavenPublication> {
                    if (!artifactId.endsWith("-metadata") && !artifactId.endsWith("-kotlinMultiplatform")) {
                        api("$groupId:$artifactId:$version")
                    }
                }
            }
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("remi") {
            from(components["javaPlatform"])

            artifactId = "bom"
            groupId = "sh.nino.towa"
            version = "$VERSION"

            pom {
                description by "Bill of Materials for the Towa project."
                name by "bom"
                url by "https://towa.nino.sh"

                organization {
                    name by "Nino Team"
                    url by "https://nino.sh"
                }

                developers {
                    developer {
                        name by "Noel"
                        email by "cutie@floofy.dev"
                        url by "https://floofy.dev"
                    }
                }

                issueManagement {
                    system by "GitHub"
                    url by "https://github.com/Noelware/remi/issues"
                }

                licenses {
                    license {
                        name by "Apache-2.0"
                        url by "http://www.apache.org/licenses/LICENSE-2.0"
                    }
                }

                scm {
                    connection by "scm:git:ssh://github.com/NinoDiscord/towa.git"
                    developerConnection by "scm:git:ssh://git@github.com:NinoDiscord/towa.git"
                    url by "https://github.com/NinoDiscord/towa"
                }
            }
        }
    }

    repositories {
        val url = if (snapshotRelease) "s3://maven.floofy.dev/repo/snapshots" else "s3://maven.floofy.dev/repo/releases"
        maven(url) {
            credentials(AwsCredentials::class.java) {
                this.accessKey = properties.getProperty("s3.accessKey") ?: ""
                this.secretKey = properties.getProperty("s3.secretKey") ?: ""
            }
        }
    }
}
