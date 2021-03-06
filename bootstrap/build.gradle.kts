import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

version = project.version

var moduleName = ""
var fileRegex = ""
var fileName = "unconfigured"

if (project.hasProperty("target")) {
    val target: String by project
    val (_moduleName, _fileRegex, _baseName) = when (target) {
        "voodoo" -> Triple(
                "voodoo",
                """^[Vv]oodoo(-[^-]*)?\.jar$""",
                "bootstrap-voodoo"
        )
        "multimc-installer" -> Triple(
                "multimc-installer",
                """^[Mm]ultimc-[Ii]nstaller(-[^-]*)\.jar$""",
                "bootstrap-multimc-installer"
        )
        else -> throw InvalidUserDataException("invalid target property")
    }
    moduleName = _moduleName
    fileRegex = _fileRegex
    fileName = _baseName
}

base {
    archivesBaseName = "$fileName-${Env.versionSuffix}"
}
val shadowJar by tasks.getting(ShadowJar::class) {
    classifier = ""
    archiveName = "$baseName.$extension"
}

val build by tasks.getting(Task::class) {
    dependsOn(shadowJar)
}

configure<ConstantsExtension> {
    constantsObject(
        pkg = "voodoo.bootstrap",
        className = "Config"
    ) {
        field("JENKINS_URL") value Env.buildNumber
        field("JENKINS_URL") value Jenkins.jenkinsUrl
        field("JENKINS_JOB") value Jenkins.jenkinsJob
        field("MODULE_NAME") value moduleName
        field("FILE_REGEX") value fileRegex
    }
}
