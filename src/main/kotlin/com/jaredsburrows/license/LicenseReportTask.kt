package com.jaredsburrows.license

import com.android.builder.model.ProductFlavor
import com.jaredsburrows.license.internal.pom.Project
import com.jaredsburrows.license.internal.report.HtmlReport
import com.jaredsburrows.license.internal.report.JsonReport
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.*
import java.io.File
import java.io.InputStreamReader
import java.io.PrintStream

/**
 * @author <a href="mailto:jaredsburrows@gmail.com">Jared Burrows</a>
 */
class LicenseReportTask : DefaultTask() {
    companion object {
        const val POM_CONFIGURATION = "poms"
        const val TEMP_POM_CONFIGURATION = "tempPoms"
        const val ANDROID_SUPPORT_GROUP_ID = "com.android.support"
        const val APACHE_LICENSE_NAME = "The Apache Software License"
        const val APACHE_LICENSE_URL = "http://www.apache.org/licenses/LICENSE-2.0.txt"
        const val OPEN_SOURCE_LICENSES = "open_source_licenses"
        const val HTML_EXT = ".html"
        const val JSON_EXT = ".json"
    }
    @Internal
    var projects = mutableListOf<Project>()
    @Optional
    @Input
    var assetDirs = mutableListOf<File>()
    @Optional
    @Internal
    var productFlavors = mutableListOf<ProductFlavor>()
    @Optional
    @Input
    lateinit var buildType: String
    @Optional
    @Input
    lateinit var applicationVariant: String
    @OutputFile
    lateinit var htmlFile : File
    @OutputFile
    lateinit var jsonFile : File

    @SuppressWarnings("GroovyUnusedDeclaration")
    @TaskAction
    fun licenseReport() {
        setupEnvironment()
        collectDependencies()
        generatePOMInfo()
        createHTMLReport()
        createJsonReport()
    }

    /**
     * Setup configurations to collect dependencies.
     */
    private fun setupEnvironment() {
        // Create temporary configuration in order to store POM information
        project.configurations.create(POM_CONFIGURATION)

        project.configurations.forEach {
            try {
                it.isCanBeResolved = true
            } catch (ignore: Exception) {
            }
        }
    }

    /**
     * Iterate through all configurations and collect dependencies.
     */
    private fun collectDependencies() {
        // Add POM information to our POM configuration
        val configurations = LinkedHashSet<Configuration>()

        // Add 'compile' configuration older java and android gradle plugins
        if (project.configurations.find { it.name == "compile" } != null) configurations.add(project.configurations.getByName("compile"))

        // Add 'api' and 'implementation' configurations for newer java-library and android gradle plugins
        if (project.configurations.find { it.name == "api" } != null) configurations.add(project.configurations.getByName("api"))
        if (project.configurations.find { it.name == "implementation" } != null) configurations.add(project.configurations.getByName("implementation"))

        // If Android project, add extra configurations
        if (applicationVariant.isEmpty()) {
            // Add buildType configurations
            if (project.configurations.find { it.name == "compile" } != null) configurations.add(project.configurations.getByName("${buildType}Compile"))
            if (project.configurations.find { it.name == "api" } != null) configurations.add(project.configurations.getByName("${buildType}Api"))
            if (project.configurations.find { it.name == "implementation" } != null) configurations.add(project.configurations.getByName("${buildType}Implementation"))

            // Add productFlavors configurations
            productFlavors.forEach { flavor ->
                // Works for productFlavors and productFlavors with dimensions
                if (applicationVariant.capitalize().contains(flavor.name.capitalize())) {
                    if (project.configurations.find { it.name == "compile" } != null) configurations.add(project.configurations.getByName("${flavor.name}Compile"))
                    if (project.configurations.find { it.name == "api" } != null) configurations.add(project.configurations.getByName("${flavor.name}Api"))
                    if (project.configurations.find { it.name == "implementation" } != null) configurations.add(project.configurations.getByName("${flavor.name}Implementation"))
                }
            }
        }

        // Iterate through all the configurations's dependencies
        configurations.forEach { configuration ->
            if (configuration.isCanBeResolved) {
                val poms = arrayListOf<String>()
                val artifacts = configuration.resolvedConfiguration.lenientConfiguration.artifacts
                artifacts.forEach { artifact ->
                    val id = artifact.moduleVersion.id
                    poms.add("$id.group:$id.name:$id.version@pom")
                }

                poms.forEach { pom ->
                    project.configurations.getByName(POM_CONFIGURATION).dependencies.add(
                        project.dependencies.add(POM_CONFIGURATION, pom)
                    )
                }


            }
        }
    }

    private fun generatePOMInfo() {
    }

    /**
     * Generated HTML report.
     */
    private fun createHTMLReport() {
        // Remove existing file
        project.file(htmlFile).delete()

        // Create directories and write report for file
        htmlFile.parentFile.mkdirs()
        htmlFile.createNewFile()
        htmlFile.outputStream().use { outputStream ->
            val printStream = PrintStream(outputStream)
            printStream.print(HtmlReport(projects).string())
            printStream.println() // Add new line to file
        }

        // If Android project, copy to asset directory
        if (applicationVariant.isEmpty()) {
            // Iterate through all asset directories
            assetDirs.forEach { directory ->
                val licenseFile = File(directory.path, OPEN_SOURCE_LICENSES + HTML_EXT)

                // Remove existing file
                project.file(licenseFile).delete()

                // Create new file
                licenseFile.parentFile.mkdirs()
                licenseFile.createNewFile()

                // Copy HTML file to the assets directory
//                val currentText = InputStreamReader(project.file(htmlFile).inputStream()).readText()

//                project.file(licenseFile << project.file(htmlFile).text)
            }
        }

        // Log output directory for user
        logger.log(LogLevel.LIFECYCLE, String.format("Wrote HTML report to %s.", htmlFile.absolutePath))
    }

    /**
     * Generated JSON report.
     */
    private fun createJsonReport() {
        // Remove existing file
        project.file(jsonFile).delete()

        // Create directories and write report for file
        jsonFile.parentFile.mkdirs()
        jsonFile.createNewFile()
        jsonFile.outputStream().use { outputStream ->
            val printStream = PrintStream(outputStream)
            printStream.println(JsonReport(projects).string())
            printStream.println() // Add new line to file
        }

        // Log output directory for user
        logger.log(LogLevel.LIFECYCLE, String.format("Wrote JSON report to %s.", jsonFile.absolutePath))
    }
}
