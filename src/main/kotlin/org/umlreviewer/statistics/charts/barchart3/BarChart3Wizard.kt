package org.umlreviewer.statistics.charts.barchart3

import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid
import org.kordamp.ikonli.javafx.FontIcon
import org.umlreviewer.statistics.charts.RefreshableWizard
import tornadofx.*

class BarChart3Wizard: RefreshableWizard() {
    val barChart3Vm: BarChart3WizardViewModel by inject()

    init {
        title = "Bar chart 3"
        heading = "Issue counts by teams in a given week for given diagram types."
        graphic = label {
            graphic = FontIcon(FontAwesomeSolid.CHART_BAR)
            style  {
                fontSize = 60.px
            }
        }
        add(find<DiagramTypesChooserStep>())
        add(find<TeamsChooserStep>())
        add(find<WeekChooserStep>())
    }

    override fun onBeforeShow() {
        root.prefWidth = 900.0
        root.prefHeight = 600.0
        resetWizard()
    }

    private fun resetWizard() {
        barChart3Vm.clear()
        refreshAllPages()
        isComplete = false
        currentPage = pages[0]
    }

    override fun onSave() {
        isComplete = barChart3Vm.commit()
    }
}


