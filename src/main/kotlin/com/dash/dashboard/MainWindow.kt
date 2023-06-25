package com.dash.dashboard

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.stage.Stage

class MainWindow : Application() {
    //Criando uma 'cache'que armaze todas as telas do dashboard para a navegação do projeto
    private lateinit var secundaryStage: Stage
    private lateinit var mainScene: Scene
    private lateinit var fileScreen: Scene
    override fun start(stage: Stage) {
        secundaryStage=stage
        //val fxmlLoader = FXMLLoader(MainWindow::class.java.getResource("mainWindow.fxml"))
        val fxmlLoader = FXMLLoader(MainWindow::class.java.getResource("filesScreen.fxml"))
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
