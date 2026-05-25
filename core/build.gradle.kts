plugins {
    id("com.android.library")
    id("maven-publish")
}

group = "me.carda.awesome_notifications.core"
version = "0.10.0"

android {
    namespace = "me.carda.awesome_notifications.core"
    compileSdk = 37

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
        }
        create("profile") {
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }

        singleVariant("debug") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies {
    implementation("com.google.guava:guava:33.6.0-android")
    implementation("com.github.kumsumit:ShortcutBadger:8bd8c795c7")

    implementation("androidx.annotation:annotation:1.10.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.14.0")
    implementation("androidx.media:media:1.7.0")
    implementation("androidx.localbroadcastmanager:localbroadcastmanager:1.1.0")

    implementation("androidx.sqlite:sqlite:2.6.2")

    implementation("androidx.room:room-runtime:2.8.4")
    implementation("androidx.lifecycle:lifecycle-process:2.10.0")
    annotationProcessor("androidx.room:room-compiler:2.8.4")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])

                groupId = "me.carda"
                artifactId = "AwnAndroidCore"
                version = "0.10.0"
            }
            create<MavenPublication>("debug") {
                from(components["debug"])

                groupId = "me.carda"
                artifactId = "AwnAndroidCore"
                version = "0.10.0"
            }
        }
    }
}
