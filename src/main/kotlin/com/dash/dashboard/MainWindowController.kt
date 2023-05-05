package com.dash.dashboard

import com.dash.dashboard.models.MemUsage
import com.dash.dashboard.system.Memory
import eu.hansolo.tilesfx.Tile
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.chart.LineChart
import javafx.scene.chart.XYChart
import javafx.scene.chart.XYChart.Series
import javafx.scene.control.ChoiceBox
import javafx.scene.control.Label
import java.net.URL
import java.util.*


class MainWindowController: Initializable {

    @FXML
    private lateinit var welcomeText: Label
    @FXML // fx:id="intervaloComboBox"
    private lateinit var intervaloComboBox: ChoiceBox<String>
    @FXML // fx:id="memTile"
    private lateinit var memTile: Tile
    @FXML // fx:id="freeMemLabel"
    private lateinit var freeMemLabel: Label
    @FXML // fx:id="usedMemLabel"
    private lateinit var usedMemLabel: Label
    @FXML // fx:id="totalMemLabel"
    private lateinit var totalMemLabel: Label
    @FXML // fx:id="memoryConsumptionChart"
    lateinit var memoryConsumptionChart: LineChart<String, Number>

    var sleep = 5000L
    var chartCounter = 0

    @FXML
    private fun setSelectedInterval(){
    }
    @FXML
    private fun setUpMemTile(){
    }

    override fun initialize(p0: URL?, p1: ResourceBundle?) {
        intervaloComboBox.items.setAll("5 Segundos", "10 Segundos", "15 segundos")
        setUpMemData()

    }

    private fun setUpMemData() {
        val t = Thread {
            while (true) {
                var memData = Memory().getMemData()
                Platform.runLater {
                    usedMemLabel.text = (memData.usedMem).toString() + " Mb"
                    totalMemLabel.text = (memData.totalMem).toString() + " Mb"
                    freeMemLabel.text = (memData.freeMem ).toString() + " Mb"
                    setUpMemChart(memData)
                    sleep = intervaloComboBox.value.split(" ")[0].toLong() * 1000
                }
                try {
                    Thread.sleep(sleep)
                } catch (ex: InterruptedException) {
                    break
                }
            }
        }
        t.name = "cpu data updater"
        t.isDaemon = true
        t.start()
    }
    private fun setUpMemChart(memData: MemUsage) {
        val series = Series<String, Number>()
        series.data.add(XYChart.Data("$chartCounter segs", memData.usedMem))
        memoryConsumptionChart.data.add(series)
        chartCounter += (sleep / 1000).toInt()

    }


}