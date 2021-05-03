package org.vpreportcorrector.statistics

import javafx.scene.layout.Priority
import org.jfree.chart.JFreeChart
import org.jfree.chart.title.TextTitle
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid
import org.kordamp.ikonli.javafx.FontIcon
import org.vpreportcorrector.app.AppColors
import org.vpreportcorrector.app.Styles
import org.vpreportcorrector.app.errorhandling.errorWithStacktrace
import org.vpreportcorrector.components.NoSelectionModel
import org.vpreportcorrector.components.form.loadingOverlay
import org.vpreportcorrector.statistics.charts.barchart1.BarChart1Wizard
import org.vpreportcorrector.statistics.charts.barchart2.BarChart2Wizard
import org.vpreportcorrector.statistics.charts.barchart3.BarChart3Wizard
import org.vpreportcorrector.statistics.charts.ChartHelpers
import org.vpreportcorrector.utils.t
import tornadofx.*

class StatisticsView : View("Statistics") {
    private val vm by inject<StatisticsViewModel>()
    private val taskStatus: TaskStatus by inject()
    private val barChart1Wizard = find<BarChart1Wizard>()
    private val barChart2Wizard = find<BarChart2Wizard>()
    private val barChart3Wizard = find<BarChart3Wizard>()

    override val root = stackpane {
        borderpane {
            style {
                borderColor += box(AppColors.borderGray)
                borderWidth += box(1.px, 0.px, 0.px, 0.px)
            }
            addClass(Styles.paddedContainer)
            center = vbox {
                form {
                    fieldset {
                        field("Bar chart 1: Issue counts in a given week for a given team/seminar group") {
                            button("Add", FontIcon(FontAwesomeSolid.PLUS)) {
                                tooltip = tooltip(this@field.text)
                                action {
                                    barChart1Wizard.openModal(block = true)
                                    if (barChart1Wizard.isComplete) {
                                        runAsync(taskStatus) {
                                            barChart1Wizard.barChart1Vm.getGraphs(this)
                                        } ui {
                                            vm.graphs.addAll(it)
                                        } fail {
                                            log.severe(it.stackTraceToString())
                                            errorWithStacktrace("Error generating charts.", it)
                                        }
                                    }
                                }
                            }
                        }
                        field("Bar chart 2: Comparison of issue counts in a given pair of weeks for a given team/seminar group") {
                            button("Add", FontIcon(FontAwesomeSolid.PLUS)) {
                                tooltip = tooltip(this@field.text)
                                action {
                                    barChart2Wizard.openModal(block = true)
                                    if(barChart2Wizard.isComplete) {
                                        runAsync(taskStatus) {
                                            barChart2Wizard.barChart2Vm.getGraphs(this)
                                        } ui {
                                            vm.graphs.addAll(it)
                                        } fail {
                                            log.severe(it.stackTraceToString())
                                            errorWithStacktrace("Error generating charts.", it)
                                        }
                                    }
                                }
                            }
                        }
                        field("${barChart3Wizard.title}: ${barChart3Wizard.heading}") {
                            button("Add", FontIcon(FontAwesomeSolid.PLUS)) {
                                tooltip = tooltip(this@field.text)
                                action {
                                    barChart3Wizard.openModal(block = true)
                                    if(barChart3Wizard.isComplete) {
                                        runAsync(taskStatus) {
                                            barChart3Wizard.barChart3Vm.getGraph(this)
                                        } ui {
                                            vm.graphs.addAll(it)
                                        } fail {
                                            log.severe(it.stackTraceToString())
                                            errorWithStacktrace("Error generating bar chart 3.", it)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                hbox {
                    hgrow = Priority.ALWAYS
                    hbox {
                        hgrow = Priority.ALWAYS
                        label("Charts:") { addClass(Styles.h4) }
                    }
                    button("Export all to PDF", FontIcon(FontAwesomeSolid.FILE_EXPORT)) {
                        action { vm.exportToPdf() }
                    }
                }
                listview(vm.graphs) {
                    fitToParentSize()
                    selectionModel = NoSelectionModel<JFreeChart>()
                    cellFormat {
                        graphic = hbox {
                            addClass(Styles.paddedContainer)
                            vbox {
                                hgrow = Priority.ALWAYS
                                label(it.title.text) {
                                    addClass(Styles.h4)
                                    isWrapText = true
                                    prefWidthProperty().bind(this@listview.widthProperty().minus(20+80+10))
                                }
                                val hiddenDetails = it.getHiddenDetails()
                                if (hiddenDetails.isNotEmpty()) {
                                    hiddenDetails.forEach { (label, text) ->
                                        textflow {
                                            isWrapText = true
                                            prefWidthProperty().bind(this@listview.widthProperty().minus(20+80+10))
                                            style { padding = box(0.px, 0.px, 0.px, 10.px)}
                                            label("$label: ") {
                                                addClass(Styles.textBold)
                                                isWrapText = true
                                            }
                                            text(text)
                                        }
                                    }
                                }
                            }
                            hbox {
                                vgrow = Priority.ALWAYS
                                button("", FontIcon(FontAwesomeSolid.EYE)) {
                                    addClass(Styles.flatButton)
                                    prefWidth = 40.0
                                    tooltip = tooltip("Open graph preview")
                                    maxHeight = Double.MAX_VALUE
                                    vgrow = Priority.ALWAYS
                                    action {
                                        openChartPreview(it)
                                    }
                                }
                                button("", FontIcon(FontAwesomeSolid.TRASH)) {
                                    addClass(Styles.flatButton)
                                    prefWidth = 40.0
                                    maxHeight = Double.MAX_VALUE
                                    vgrow = Priority.ALWAYS
                                    tooltip = tooltip(t("delete"))
                                    action {
                                        vm.graphs.remove(it)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        loadingOverlay(taskStatus)
    }

    private fun openChartPreview(chart: JFreeChart?) {
        if (chart == null) return
        find<ChartPreview>(mapOf(ChartPreview::chart to chart)).openModal()
    }

    override fun onDock() {
        // TODO KB:
        log.info("statistics on dock")
    }

    override fun onUndock() {
        // TODO: 19.04.21
        log.info("statistics on undock")
    }

    private fun JFreeChart.getHiddenDetails(): MutableList<Pair<String, String>> {
        val detailsTitle = this.subtitles.find { ChartHelpers.isHiddenDetailsTitle(it) } as TextTitle?
        val text = detailsTitle?.text
        val result = mutableListOf<Pair<String, String>>()
        if (text != null) {
            val lines = text.split("\n").filter { it.isNotBlank() }
            lines.forEach {
                val labelAndText = it.split(":")
                val label = labelAndText.getOrElse(0){""}
                val content = labelAndText.subList(1, labelAndText.size).joinToString(":").trim()
                result.add(Pair(label, content))
            }
        }
        return result
    }
}

