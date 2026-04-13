import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") version "2.3.10"
    id("org.jetbrains.compose") version "1.10.2"
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.10"
}

group = "com.composestudio"
version = "1.0.0"

dependencies {
    implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.compose.material3:material3:1.9.0")
    implementation("org.jetbrains.compose.material:material-icons-extended:1.7.3")
}

compose.desktop {
    application {
        mainClass = "composestudio.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "ComposeStudio"
            packageVersion = "1.0.0"
            description = "Visual UI Designer for Compose Multiplatform"
            vendor = "ComposeStudio"

            windows {
                menuGroup = "ComposeStudio"
                upgradeUuid = "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
            }

            macOS {
                bundleID = "com.composestudio.app"
            }

            linux {
                packageName = "composestudio"
            }
        }
    }
}
