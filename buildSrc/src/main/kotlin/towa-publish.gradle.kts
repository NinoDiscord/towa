/*
 * 👾 Towa: Powerful and advanced command handling library made for Discord.
 * Copyright © 2022 Nino Team <https://nino.sh>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import dev.floofy.utils.gradle.*
import sh.nino.towa.gradle.*
import java.io.StringReader
import java.util.Properties

plugins {
    id("org.jetbrains.dokka")
    `maven-publish`
    kotlin("jvm")
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

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)

    manifest {
        attributes(mapOf(
            "Implementation-Vendor" to "Nino Team <https://nino.sh>",
            "Implementation-Version" to "$VERSION"
        ))
    }
}

val dokkaJar by tasks.registering(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Assemble Kotlin documentation with Dokka"

    archiveClassifier.set("javadoc")
    from(tasks.dokkaHtml)
    dependsOn(tasks.dokkaHtml)
}

publishing {
    publications {
        create<MavenPublication>("towa") {
            from(components["kotlin"])

            artifactId = project.name
            groupId = "sh.nino.towa"
            version = "$VERSION"

            artifact(sourcesJar.get())
            artifact(dokkaJar.get())

            pom {
                description by project.description
                name by project.name
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
                accessKey = properties.getProperty("s3.accessKey") ?: ""
                secretKey = properties.getProperty("s3.secretKey") ?: ""
            }
        }
    }
}
