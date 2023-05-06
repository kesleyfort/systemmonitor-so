package com.dash.dashboard

import com.dash.dashboard.models.CpuUsage
import com.dash.dashboard.models.MemUsage
import com.dash.dashboard.system.CPU
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
import java.math.RoundingMode
import java.net.URL
import java.text.DecimalFormat
import java.util.*


class MainWindowController: Initializable {

    lateinit var cpuConsumptionChart: LineChart<String, Number>
    lateinit var cpuUsage: Label
    lateinit var cpuModel: Label
    //fx:id="cpuModel"
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

    @FXML // fx:id="totalMemLabel"

    private lateinit var maxSpeed: Label

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
        setUpCPUData()

    }

    private fun setUpCPUData() {
        val t = Thread {
            while (true) {
               // var memData = Memory().getMemData()
               // var cpuData =CPU().get
                var data= CPU().getCPUData();
                Platform.runLater {
                    cpuModel.text = data.modelname
                    val df =DecimalFormat("#.##")
                     df.roundingMode=RoundingMode.DOWN
                    maxSpeed.text=df.format( data.maxSpeed).toString() +"GHz"
                    cpuUsage.text = String.format("%.2f", data.porcentUsage.toDouble())+ " %"
                     setUpCPUChart(data)
                    sleep = intervaloComboBox.value.split(" ")[0].toLong() * 1000
                }
                try {
                    Thread.sleep(sleep)
                } catch (ex: InterruptedException) {
                    break
                }
            }
        }
        t.name = "CPU INFO"
        t.isDaemon = true
        t.start()
    }
    private fun setUpCPUChart(cpuData: CpuUsage) {
        val series = Series<String, Number>()
        series.data.add(XYChart.Data("$chartCounter segs", cpuData.porcentUsage))
        cpuConsumptionChart.data.add(series)
        chartCounter += (sleep / 1000).toInt()

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