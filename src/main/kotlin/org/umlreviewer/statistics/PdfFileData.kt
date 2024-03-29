package org.umlreviewer.statistics

import org.umlreviewer.enums.DiagramIssue
import org.umlreviewer.enums.DiagramIssueGroup
import org.umlreviewer.utils.file.FileTreeHelpers
import org.umlreviewer.utils.PreferencesHelper
import java.io.File

data class PdfFileData(
    val pdfFile: File,
    private val seminarGroup: File
) {
    val relativePath: String
    val week: Int?
    val team: Int?
    val issues = mutableSetOf<DiagramIssue>()
    val issueGroups = mutableSetOf<DiagramIssueGroup>()

    init {
        val workdir = PreferencesHelper.getWorkingDirectory()?.toFile()
        relativePath = if (workdir != null) pdfFile.relativeTo(workdir).toString() else pdfFile.path
        week = FileTreeHelpers.getWeek(relativePath)
        team = FileTreeHelpers.getTeam(relativePath)
        val jsonData = PdfJsonData()
        jsonData.load(pdfFile.toPath())
        issues.addAll(jsonData.diagramIssues)
        issueGroups.addAll(issues.map { DiagramIssueGroup.getGroup(it) }.filterNotNull())
    }

    fun getIssueCountsPerGroup(): Map<DiagramIssueGroup, Int> {
        val result = mutableMapOf<DiagramIssueGroup, Int>()
        issueGroups.forEach { grp -> result[grp] = 0 }
        issues.forEach { di ->
            val key = DiagramIssueGroup.getGroup(di)!!
            result[key] = result[key]?.plus(1) ?: 1
        }
        return result.toMap()
    }
}
