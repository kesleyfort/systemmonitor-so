package com.dash.dashboard

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.stage.Stage

class MainWindow : Application() {

    private lateinit var mainScene: Scene

    override fun start(stage: Stage) {

        val fxmlLoader = FXMLLoader(MainWindow::class.java.getResource("mainWindow.fxml"))

        mainScene=Scene(fxmlLoader.load())



        val scene = mainScene

        stage.title = "Dashboard - System Monitor"
        stage.scene = scene
        stage.isResizable = false
        stage.show()

    }


}

fun main() {
    Application.launch(MainWindow::class.java)
}
