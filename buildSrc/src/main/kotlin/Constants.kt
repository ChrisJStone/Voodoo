import org.gradle.api.tasks.wrapper.Wrapper

fun create(
    group: String,
    name: String,
    version: String? = null
): String = buildString {
    append(group)
    append(':')
    append(name)
    version?.let {
        append(':')
        append(it)
    }
}

object Gradle {
    const val version = "5.1.1"
    val distributionType = Wrapper.DistributionType.ALL
}

object Kotlin {
    const val version = "1.3.20-eap-100"
}

object Coroutines {
    const val version = "1.1.0-alpha"
    val dependency = create(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version = version)
}

object Serialization {
    const val version = "0.10.0-eap-1"
    const val plugin = "kotlinx-serialization"
    const val module = "org.jetbrains.kotlinx:kotlinx-gradle-serialization-plugin"
    val dependency = create(group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-runtime", version = version)
}

object Kotlinpoet {
    const val version = "1.0.0"
    val dependency = create(group = "com.squareup", name = "kotlinpoet", version = version)
}

object Fuel {
    const val version = "bfbe44ca48"
    private const val group = "com.github.NikkyAi.Fuel"
//    private const val group = "com.github.kittinunf.Fuel"
    val dependency = create(group = group, name = "fuel", version = version)
    val dependencyCoroutines = create(group = group, name = "fuel-coroutines", version = version)
    val dependencySerialization = create(group = group, name = "fuel-kotlinx-serialization", version = version)
}

object Argparser {
    const val version = "2.0.7"
    val dependency = create(group = "com.xenomachina", name = "kotlin-argparser", version = version)
}

object KotlinxHtml {
    const val version = "0.6.10"
    val dependency = create(group = "org.jetbrains.kotlinx", name = "kotlinx-html-jvm", version = version)
}

object Logging {
    const val version = "1.6.10"

    val dependency = create(group = "io.github.microutils", name = "kotlin-logging", version = version)
    val dependencyLogbackClassic = create(group = "ch.qos.logback", name = "logback-classic", version = "1.3.0-alpha4")
}

object Spek {
    const val version = "2.0.0-rc.1"
    val dependencyDsl = create(group = "org.spekframework.spek2", name = "spek-dsl-jvm", version = version)
    val dependencyRunner = create(group = "org.spekframework.spek2", name = "spek-runner-junit5", version = version)
    val dependencyJUnit5 = create(group = "org.junit.platform", name = "junit-platform-engine", version = "1.3.0-RC1")
}

object Apache {
    val commonsCompress = create(group = "org.apache.commons", name = "commons-compress", version = "1.18")
}

object Jenkins {
    const val jenkinsUrl: String = "https://ci.elytradev.com"
    const val jenkinsJob: String = "elytra/Voodoo/master"
}
