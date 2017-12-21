package com.jaredsburrows.license.internal.report

import com.jaredsburrows.license.internal.pom.Project
import groovy.json.JsonBuilder

/**
 * @author <a href="mailto:jaredsburrows@gmail.com">Jared Burrows</a>
 */
class JsonReport(val projects: List<Project>) {
    companion object {
        private const val PROJECT = "project"
        private const val DEVELOPERS = "developers"
        private const val URL = "url"
        private const val YEAR = "year"
        private const val LICENSE = "license"
        private const val LICENSE_URL = "license_url"
        private const val EMPTY_JSON_ARRAY = "[]"
    }

    /**
     * Return Json as a String.
     */
    fun string(): String {
        return if (projects.isEmpty()) EMPTY_JSON_ARRAY else jsonArray().toPrettyString()
    }

    /**
     * Json report when there are open source licenses.
     */
    private fun jsonArray(): JsonBuilder {
        val contents = arrayListOf<Map<String, Any?>>()

        projects.forEach { project ->
            val map = mutableMapOf<String, Any?>()
            val name = project.name
            val developerList = arrayListOf<String?>()
            project.developers?.forEach { developer ->
                developerList.add(developer.name)
            }
            val developers = developerList.joinToString(separator = ", ")
            val url = project.url
            val year = project.year
            val license = project.license?.name
            val licenseUrl = project.license?.url

            if (!name.isNullOrEmpty()) map[PROJECT] = name
            if (!developers.isEmpty()) map[DEVELOPERS] = developers
            if (!url.isNullOrEmpty()) map[URL] = url
            if (!year.isNullOrEmpty()) map[YEAR] = year
            if (!license.isNullOrEmpty()) map[LICENSE] = license
            if (!licenseUrl.isNullOrEmpty()) map[LICENSE_URL] = licenseUrl

            contents.add(map)
        }

        return JsonBuilder(contents)
    }
}
