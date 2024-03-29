package org.umlreviewer.import

import javafx.application.Platform
import org.umlreviewer.diagramextractor.visualparadigmpdftopdf.VisualParadigmPdfToPdfDiagramExtractor
import org.umlreviewer.errorhandling.ErrorCollector
import org.umlreviewer.utils.file.FileConflictChoice
import org.umlreviewer.utils.file.copyFiles
import org.umlreviewer.utils.file.isPdf
import tornadofx.Controller
import java.io.File

class ImportController: Controller() {
    fun importAndExtractDiagrams(dest: File, files: List<File>) {
        val collector = ErrorCollector("Error/s occurred while extracting diagrams:")
        val filesToCopy = mutableSetOf<File>()
        var fileConflictChoice: FileConflictChoice? = null
        files.forEach { file ->
            if (isPdf(file)) {
                try {
                    val de = VisualParadigmPdfToPdfDiagramExtractor(file, dest, fileConflictChoice)
                    de.extractDiagrams()
                    fileConflictChoice = de.savedFileConflictChoice
                } catch (e: Exception) {
                    collector.addError("Error extracting diagrams from file '${file.name}'", e)
                }
            } else {
                filesToCopy.add(file)
            }
        }
        Platform.runLater {
            collector.verify()
        }
        copyFiles(dest, filesToCopy.map { it.toPath() })
    }
}
