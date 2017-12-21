package com.jaredsburrows.license.internal.report

import groovy.xml.MarkupBuilder
import org.gradle.api.Project
import java.io.StringWriter

/**
 * @author <a href="mailto:jaredsburrows@gmail.com">Jared Burrows</a>
 */
class HtmlReport2(val projects: List<Project>) {
    companion object {
        private const val BODY_CSS = "body{font-family: sans-serif}"
        private const val PRE_CSS = "pre{background-color: #eeeeee; padding: 1em; white-space: pre-wrap}"
        private const val CSS_STYLE = BODY_CSS + " " + PRE_CSS
        private const val OPEN_SOURCE_LIBRARIES = "Open source licenses"
        private const val NO_OPEN_SOURCE_LIBRARIES = "No open source libraries"
        private const val NOTICE_LIBRARIES = "Notice for libraries:"
    }

    fun noOpenSourceHtml(): String {
        val writer = StringWriter()
        val markup = MarkupBuilder(writer)


        return writer.toString()
    }
}
