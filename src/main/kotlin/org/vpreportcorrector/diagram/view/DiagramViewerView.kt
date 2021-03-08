package org.vpreportcorrector.diagram.view

import javafx.embed.swing.SwingNode
import javafx.event.EventHandler
import javafx.scene.control.ToggleButton
import javafx.scene.control.Tooltip
import javafx.scene.layout.Priority
import org.icepdf.ri.common.MyAnnotationCallback
import org.icepdf.ri.common.SwingViewBuilder
import org.icepdf.ri.util.FontPropertiesManager
import org.icepdf.ri.util.ViewerPropertiesManager
import org.kordamp.ikonli.javafx.FontIcon
import org.vpreportcorrector.components.form.loadingOverlay
import org.vpreportcorrector.diagram.DiagramController
import org.vpreportcorrector.diagram.components.DiagramErrorsDrawerView
import org.vpreportcorrector.diagram.components.CustomSwingNode
import org.vpreportcorrector.diagram.edit.DEFAULT_PDF_VIEWER_ICON_SIZE
import tornadofx.*
import java.awt.Dimension
import javax.swing.JComponent
import javax.swing.SwingUtilities

// TODO: refactor - shared code with the editor view
// TODO: 15.03.21 refactor - MVVM
// TODO: solve resizing bugs
class DiagramViewerView : View() {
    private val controller: DiagramController by inject()

    private var diagramErrorsBtn: ToggleButton by singleAssign()
    private val diagramErrorsDrawer = find<DiagramErrorsDrawerView>()

    private var swingController = controller.swingController
    private var viewerPanel: JComponent by singleAssign()
    private val swingNode = CustomSwingNode()

    private var toolBar = hbox {
        fitToParentWidth()
        style {
            padding = box(4.px)
        }
    }

    private val centerView = vbox {
        fitToParentSize()
        widthProperty().onChange {
            resizeViewerPanel()
        }
        heightProperty().onChange {
            resizeViewerPanel()
        }
    }

    private fun resizeViewerPanel(){
        val width = centerView.widthProperty().value
        val height = centerView.heightProperty().value
        val dimension = Dimension(width.toInt(), height.toInt())
        swingNode.resize(width, height)
        SwingUtilities.invokeLater {
            viewerPanel.size = dimension
            viewerPanel.minimumSize = dimension
            viewerPanel.preferredSize = dimension
            viewerPanel.maximumSize = dimension
            viewerPanel.repaint()
        }
    }

    private fun createViewer() {
        try {
            SwingUtilities.invokeAndWait {
                swingController.setIsEmbeddedComponent(true)
                FontPropertiesManager.getInstance().loadOrReadSystemFonts()

                val properties = ViewerPropertiesManager.getInstance()
                setPdfViewerPreferences(properties)

                swingController.documentViewController.annotationCallback =
                    MyAnnotationCallback(swingController.documentViewController)
                swingController.documentViewController.documentView

                val factory = SwingViewBuilder(swingController, properties)
                viewerPanel = factory.buildUtilityAndDocumentSplitPane(false)
                buildToolbar(factory)
                viewerPanel.revalidate()
                swingNode.content = viewerPanel
                centerView.add(swingNode)
            }
        } catch (e: Exception) {
            log.severe(e.stackTraceToString())
        }
    }

    init {
        createViewer()
        controller.loadData()
        swingNode.onMouseEntered = EventHandler {
            if (!swingNode.isFocused) {
                swingNode.requestFocus()
            }
        }
        resizeViewerPanel()
    }

    private fun buildToolbar(factory: SwingViewBuilder) {
        with(toolBar) {
            flowpane {
                add(toSwingNode(factory.buildFitPageButton()))
                add(toSwingNode(factory.buildPanToolButton()))
                add(toSwingNode(factory.buildTextSelectToolButton()))
            }

            hbox { hgrow = Priority.ALWAYS }

            diagramErrorsBtn = togglebutton("") {
                addClass(org.vpreportcorrector.app.Styles.flatButton)
                isSelected = false
                graphic = FontIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.TAGS)
                tooltip = Tooltip("Show errors the diagram contains")
            }
        }
    }

    private fun toSwingNode(jComponent: JComponent): SwingNode {
        val sn = SwingNode()
        sn.content = jComponent
        return sn
    }

    private fun setPdfViewerPreferences(properties: ViewerPropertiesManager) {
        properties.clearPreferences()
        properties.setFloat(ViewerPropertiesManager.PROPERTY_DEFAULT_ZOOM_LEVEL, 1.25f)
        properties.set(ViewerPropertiesManager.PROPERTY_ICON_DEFAULT_SIZE, DEFAULT_PDF_VIEWER_ICON_SIZE)
        properties.setBoolean(ViewerPropertiesManager.PROPERTY_SHOW_TOOLBAR_ZOOM, false)
        properties.setBoolean(ViewerPropertiesManager.PROPERTY_SHOW_STATUSBAR, false)
        properties.setBoolean(ViewerPropertiesManager.PROPERTY_SHOW_TOOLBAR_ANNOTATION, false)
    }


    override val root = stackpane {
        fitToParentSize()
        vgrow = Priority.ALWAYS
        hgrow = Priority.ALWAYS
        borderpane {
            fitToParentSize()
            vgrow = Priority.ALWAYS
            hgrow = Priority.ALWAYS

            top = toolBar

            center = centerView
            controller.openDocumentForView(viewerPanel)

            right = diagramErrorsDrawer.root
            diagramErrorsDrawer.drawerExpandedProperty.bind(diagramErrorsBtn.selectedProperty())
        }

        loadingOverlay {
            visibleWhen { controller.model.loadingLatch.isLoading }
        }
    }
}