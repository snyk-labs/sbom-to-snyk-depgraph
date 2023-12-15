import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import com.github.spotbugs.snom.Confidence
import com.github.spotbugs.snom.Effort
import com.github.spotbugs.snom.SpotBugsTask
import net.ltgt.gradle.errorprone.errorprone

plugins {
    id("java-library")
    id("jacoco")
    id("maven-publish")
    id("checkstyle")
    id("pmd")
    id("com.github.spotbugs") version "6.0.2"
    id("net.ltgt.errorprone") version "3.1.0"
    id("io.freefair.lombok") version "8.4"
    id("com.github.ben-manes.versions") version "0.50.0"
    id("org.cyclonedx.bom") version "1.8.1"
}

repositories {
    mavenLocal()
    mavenCentral()
}

group = "com.mfvanek"
version = "1.5.0"
description = "Hibernate sample app"

dependencies {
    implementation(platform("org.hibernate.orm:hibernate-platform:6.4.0.Final"))
    implementation("org.hibernate.orm:hibernate-core")
    implementation("org.hibernate.orm:hibernate-hikaricp")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("org.postgresql:postgresql:42.7.1")
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("ch.qos.logback:logback-classic:1.4.14")
    implementation("org.liquibase:liquibase-core:4.25.0")
    implementation("com.github.blagerweij:liquibase-sessionlock:1.6.9")
    implementation("org.mongodb:mongodb-driver:3.12.14")
    implementation("org.hibernate.validator:hibernate-validator:8.0.1.Final")

    testImplementation(platform("org.junit:junit-bom:5.10.1"))
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.assertj:assertj-core:3.24.2")

    checkstyle("com.thomasjensen.checkstyle.addons:checkstyle-addons:7.0.1")

    errorprone("com.google.errorprone:error_prone_core:2.23.0")
    errorprone("jp.skypencil.errorprone.slf4j:errorprone-slf4j:0.1.21")

    spotbugsPlugins("jp.skypencil.findbugs.slf4j:bug-pattern:1.5.0")
    spotbugsPlugins("com.h3xstream.findsecbugs:findsecbugs-plugin:1.12.0")
    spotbugsPlugins("com.mebigfatguy.sb-contrib:sb-contrib:7.6.3")
}

java {
//    toolchain {
//        languageVersion = JavaLanguageVersion.of(19)
//    }
    sourceCompatibility = JavaVersion.VERSION_19
    targetCompatibility = JavaVersion.VERSION_19
    withJavadocJar()
    withSourcesJar()
}
tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-parameters")
    options.errorprone {
        disableWarningsInGeneratedCode.set(true)
        disable("Slf4jLoggerShouldBeNonStatic")
    }
}

tasks {
    wrapper {
        gradleVersion = "8.5"
    }

    test {
        useJUnitPlatform()
        dependsOn(checkstyleMain, checkstyleTest, pmdMain, pmdTest, spotbugsMain, spotbugsTest)
        finalizedBy(jacocoTestReport, jacocoTestCoverageVerification)
    }

    javadoc {
        if (JavaVersion.current().isJava9Compatible) {
            (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
        }
    }

    jacocoTestReport {
        dependsOn(test)
        reports {
            xml.required.set(true)
            html.required.set(true)
        }
    }

    jacocoTestCoverageVerification {
        dependsOn(jacocoTestReport)
        violationRules {
            rule {
                limit {
                    counter = "CLASS"
                    value = "MISSEDCOUNT"
                    maximum = "12".toBigDecimal()
                }
            }
            rule {
                limit {
                    counter = "METHOD"
                    value = "MISSEDCOUNT"
                    maximum = "67".toBigDecimal()
                }
            }
            rule {
                limit {
                    counter = "LINE"
                    value = "MISSEDCOUNT"
                    maximum = "253".toBigDecimal()
                }
            }
            rule {
                limit {
                    counter = "BRANCH"
                    value = "COVEREDRATIO"
                    minimum = "0.0".toBigDecimal()
                }
            }
            rule {
                limit {
                    counter = "INSTRUCTION"
                    value = "COVEREDRATIO"
                    minimum = "0.02".toBigDecimal()
                }
            }
        }
    }

    check {
        dependsOn(jacocoTestCoverageVerification)
    }
}

lombok {
    version.set(libs.versions.lombok.get())
}

jacoco {
    toolVersion = libs.versions.jacoco.get()
}

checkstyle {
    toolVersion = libs.versions.checkstyle.get()
    configFile = file("config/checkstyle/checkstyle.xml")
    isIgnoreFailures = false
    maxWarnings = 0
    maxErrors = 0
}

pmd {
    toolVersion = libs.versions.pmd.get()
    isConsoleOutput = true
    ruleSetFiles = files("config/pmd/pmd.xml")
    ruleSets = listOf()
}

spotbugs {
    showProgress.set(true)
    effort.set(Effort.MAX)
    reportLevel.set(Confidence.LOW)
    excludeFilter.set(file("config/spotbugs/exclude.xml"))
}
tasks.withType<SpotBugsTask>().configureEach {
    reports {
        create("xml") { enabled = true }
        create("html") { enabled = true }
    }
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}

tasks.named<DependencyUpdatesTask>("dependencyUpdates").configure {
    checkForGradleUpdate = true
    gradleReleaseChannel = "current"
    checkConstraints = true
    rejectVersionIf {
        isNonStable(candidate.version)
    }
}
