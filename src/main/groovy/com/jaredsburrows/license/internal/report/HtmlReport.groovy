package com.jaredsburrows.license.internal.report

import com.jaredsburrows.license.internal.LicenseHelper
import com.jaredsburrows.license.internal.pom.License
import com.jaredsburrows.license.internal.pom.Project
import groovy.xml.MarkupBuilder

final class HtmlReport {
  final static BODY_CSS = "body{font-family: sans-serif}"
  final static PRE_CSS = "pre{background-color: #eeeeee; padding: 1em; white-space: pre-wrap}"
  final static CSS_STYLE = BODY_CSS + " " + PRE_CSS
  final static OPEN_SOURCE_LIBRARIES = "Open source licenses"
  final static NO_LIBRARIES = "None"
  final static NOTICE_LIBRARIES = "Notice for packages:"
  final List<Project> projects

  HtmlReport(projects) {
    this.projects = projects
  }

  /**
   * Return Html as a String.
   */
  def string() {
    projects.empty ? noOpenSourceHtml() : openSourceHtml()
  }

  /**
   * Html report when there are open source licenses.
   */
  private def openSourceHtml() {
    final writer = new StringWriter()
    final markup = new MarkupBuilder(writer)
    final Map<License, List<Project>> projectsMap = new HashMap<>()

    // Store packages by license
    projects.each { project ->
      def key = project.licenses[0]
      if (!projectsMap.containsKey(key)) {
        projectsMap.put(key, new ArrayList<>())
      }

      projectsMap.get(key).add(project)
    }

    markup.html {
      head {
        style(CSS_STYLE)
        title(OPEN_SOURCE_LIBRARIES)
      }

      body {
        h3(NOTICE_LIBRARIES)
        ul {

          for (Map.Entry<License, List<Project>> entry : projectsMap.entrySet()) {
            final List<Project> sortedProjects = entry.value.sort {
              left, right -> left.name <=> right.name
            }

            def currentProject = null
            def currentLicense = null
            for (Project project : sortedProjects) {
              currentProject = project
              currentLicense = entry.key.url.hashCode()

              // Display libraries
              li {
                a(href: String.format("%s%s", "#", currentLicense), project.name)
              }
            }

            // Display associated license with libraries
            // Try to find license by URL, name and then default to whatever is listed in the POM.xml
            def licenseMap = LicenseHelper.LICENSE_MAP
            if (licenseMap.containsKey(entry.key.url)) {
              a(name: currentLicense)
              pre(getLicenseText(licenseMap.get(entry.key.url)))
            } else if (licenseMap.containsKey(entry.key.name)) {
              a(name: currentLicense)
              pre(getLicenseText(licenseMap.get(entry.key.name)))
            } else {
              if (currentProject && (currentProject.licenses[0].name.trim() || currentProject.licenses[0].url.trim())) {
                pre(String.format("%s\n%s", currentProject.licenses[0].name.trim(), currentProject.licenses[0].url.trim()))
              } else {
                pre(NO_LIBRARIES)
              }
            }
          }

        }
      }
    }
    writer.toString()
  }

  /**
   * Html report when there are no open source licenses.
   */
  private static noOpenSourceHtml() {
    final writer = new StringWriter()
    final markup = new MarkupBuilder(writer)

    markup.html {
      head {
        style(CSS_STYLE)
        title(OPEN_SOURCE_LIBRARIES)
      }

      body {
        h3(NO_LIBRARIES)
      }
    }
    writer.toString()
  }

  private getLicenseText(def fileName) {
    getClass().getResource("/license/${fileName}").text
  }
}
