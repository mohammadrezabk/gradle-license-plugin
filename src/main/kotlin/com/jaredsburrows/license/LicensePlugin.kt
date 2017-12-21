package com.jaredsburrows.license

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.TestExtension
import com.android.build.gradle.api.BaseVariant
import org.gradle.api.DomainObjectSet
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

/**
 * @author <a href="mailto:jaredsburrows@gmail.com">Jared Burrows</a>
 */
class LicensePlugin : Plugin<Project> {
    companion object {
        private const val APPLICATION_PLUGIN: String = "com.android.application"
        private const val LIBRARY_PLUGIN: String = "com.android.library"
        private const val TEST_PLUGIN: String = "com.android.test"
        private val ANDROID_PLUGINS: List<String> = listOf(APPLICATION_PLUGIN, LIBRARY_PLUGIN, TEST_PLUGIN)
        private val JVM_PLUGINS = listOf("kotlin", "groovy", "java", "java-library")
    }

    override fun apply(project: Project) {
        project.evaluationDependsOnChildren()

        when {
          isAndroidProject(project) -> configureAndroidProject(project)
          isJavaProject(project) -> configureJavaProject(project)
          else -> throw IllegalStateException("License report plugin can only be applied to android or java projects.")
        }
    }

    /**
     * Configure project and all variants for Android.
     */
    @Suppress("UNCHECKED_CAST")
    private fun configureAndroidProject(project: Project) {
        // Get correct plugin - Check for android library, default to application variant for application/test plugin
        val variants = getAndroidVariants(project)

        // Configure tasks for all variants
        variants?.all { variant ->
            val variantName = variant.name.capitalize()
            val taskName = "license${variantName}Report"
            val path = "${project.buildDir}/reports/licenses/$taskName"

            // Create tasks based on variant
            val task = project.tasks.create(taskName, LicenseReportTask::class.java) as LicenseReportTask
            task.apply {
                description = "Outputs licenses report for $variantName variant."
                group = "Reporting"
                htmlFile = project.file(path + LicenseReportTask.HTML_EXT)
                jsonFile = project.file(path + LicenseReportTask.JSON_EXT)
                assetDirs = project.extensions.findByType(BaseExtension::class.java)?.sourceSets?.getByName("main")?.assets?.srcDirs as MutableList<File>
                buildType = variant.buildType.name
                applicationVariant = variant.name
                productFlavors = variant.productFlavors
                // Make sure update on each run
                outputs.upToDateWhen { false }
            }
        }
    }

    /**
     * Configure project for Groovy/Java.
     */
    private fun configureJavaProject(project: Project) {
        val taskName = "licenseReport"
        val path = "${project.buildDir}/reports/licenses/$taskName"

        // Create tasks
        val task = project.tasks.create(taskName, LicenseReportTask::class.java) as LicenseReportTask
        task.apply {
            description = "Outputs licenses report."
            group = "Reporting"
            htmlFile = project.file(path + LicenseReportTask.HTML_EXT)
            jsonFile = project.file(path + LicenseReportTask.JSON_EXT)
            // Make sure update on each run
            outputs.upToDateWhen { false }
        }
    }

    /**
     * Check for the android library plugin, default to application variants for applications and test plugin.
     */
    private fun getAndroidVariants(project: Project): DomainObjectSet<out BaseVariant>? {
        return when {
            project.plugins.hasPlugin(APPLICATION_PLUGIN) -> project.extensions.findByType(AppExtension::class.java)?.applicationVariants
            project.plugins.hasPlugin(LIBRARY_PLUGIN) -> project.extensions.findByType(LibraryExtension::class.java)?.libraryVariants
            project.plugins.hasPlugin(TEST_PLUGIN) -> project.extensions.findByType(TestExtension::class.java)?.applicationVariants
            else -> throw IllegalStateException("License report plugin can only be applied to android or java projects.")
        }
    }

    /**
     * Check if the project has Android plugins.
     */
    private fun isAndroidProject(project: Project): Boolean {
        return ANDROID_PLUGINS.find { plugin -> project.plugins.hasPlugin(plugin) } != null
    }

    /**
     * Check if project has Java plugins.
     */
    private fun isJavaProject(project: Project): Boolean {
        return JVM_PLUGINS.find { plugin -> project.plugins.hasPlugin(plugin) } != null
    }
}
