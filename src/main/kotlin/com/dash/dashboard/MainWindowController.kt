package com.dash.dashboard

import com.dash.dashboard.models.CpuUsage
import com.dash.dashboard.models.MemUsage
import com.dash.dashboard.models.ProcessUsage
import com.dash.dashboard.system.CPU
import com.dash.dashboard.system.Memory
import com.dash.dashboard.system.Process
import com.dash.dashboard.system.Storage
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.chart.LineChart
import javafx.scene.chart.XYChart
import javafx.scene.chart.XYChart.Series
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.stage.Stage
import java.math.RoundingMode
import java.net.URL
import java.text.DecimalFormat
import java.util.*


class MainWindowController : Initializable {

    lateinit var porcentStorage: Label
    lateinit var storageBar: ProgressBar
    lateinit var totalStorage: Label
    lateinit var freeStorage: Label
    lateinit var cpuConsumptionChart: LineChart<String, Number>
    lateinit var cpuUsage: Label
    lateinit var cpuModel: Label

    @FXML // fx:id="intervaloComboBox"
    private lateinit var intervaloComboBox: ChoiceBox<String>

    @FXML // fx:id="freeMemLabel"
    private lateinit var freeMemLabel: Label

    @FXML // fx:id="usedMemLabel"
    private lateinit var usedMemLabel: Label

    @FXML // fx:id="totalMemLabel"
    private lateinit var totalMemLabel: Label

    @FXML // fx:id="memoryConsumptionChart"
    lateinit var memoryConsumptionChart: LineChart<String, Number>

    @FXML // fx:id="processTable"
    lateinit var processTable: TableView<ProcessUsage>

    @FXML // fx:id="idCol"
    lateinit var idCol: TableColumn<ProcessUsage, Number>

    @FXML // fx:id="processCol"
    lateinit var processCol: TableColumn<String, String>

    @FXML // fx:id="memCol"
    lateinit var memCol: TableColumn<String, String>

    @FXML // fx:id="threadsCol"
    lateinit var threadsCol: TableColumn<String, String>

    @FXML // fx:id="userCol"
    lateinit var userCol: TableColumn<String, String>

    @FXML // fx:id="totalMemLabel"
    private lateinit var maxSpeed: Label
    private var sleep = 5000L
    private var chartCounter = 0
    private var cpuChartCounter = 0

    @FXML
    protected fun goToFilesInfo(e: ActionEvent){
        val stage = Stage()
        val loader = FXMLLoader(javaClass.getResource("filesScreen.fxml"))
        val root: Parent = loader.load()
        val scene = Scene(root)

        val controller = loader.getController<FilesController>()
        controller.setTelaPrincipalStage(stage)

        stage.title = "Tela Secundária"
        stage.scene = scene
        stage.show()

    }
    override fun initialize(p0: URL?, p1: ResourceBundle?) {
        intervaloComboBox.items.setAll("5 Segundos", "10 Segundos", "15 segundos")
        setUpMemData()
        setUpCPUData()
        setUpStorageData()
        setUpProcessTable()

    }

    /**
     * Função define as propriedades do POJO relacionadas a cada coluna da tabela, depois cria uma thread para executar a atualização da porção da tela dedicada a tabela de processos.
     * Como não existe a possibilidade de uma thread externa alterar elementos da tela, é usado o comando Platform.runLater para atualizar elementos da tela por uma thread externa a do javaFX.
     * Sleep do gráfico é atualizado dependendo do valor selecionado no comboBox de intervalo.
     */
    private fun setUpProcessTable() {
        idCol.cellValueFactory = PropertyValueFactory("id")
        processCol.cellValueFactory = PropertyValueFactory("processName")
        memCol.cellValueFactory = PropertyValueFactory("memUsed")
        threadsCol.cellValueFactory = PropertyValueFactory("threads")
        userCol.cellValueFactory = PropertyValueFactory("user")
        val t = Thread {
            while (true) {
                val processData = Process().getProcessData()
                Platform.runLater {
                    processTable.items = FXCollections.observableArrayList(processData)
                    sleep = intervaloComboBox.value.split(" ")[0].toLong() * 1000
                }
                try {
                    Thread.sleep(sleep)
                } catch (ex: InterruptedException) {
                    break
                }
            }
        }
        t.name = "Process Table"
        t.isDaemon = true
        t.start()
    }
    /**
    * Função atualiza a barra de progresso da seção storage
    **/
    private fun setUpStorageBar(
        totalSpace: Double, freeSpace: Double
    ) {

        storageBar.progress = 1 - (freeSpace / totalSpace)
        porcentStorage.text = (freeSpace / totalSpace * 100).toInt().toString() + " %"

    }
    /**
    Função cria uma thread para executar a atualização da tela dedicada a armazenamento.
    * Como não existe a possibilidade de uma thread externa alterar elementos da tela, é usado o comando Platform.runLater para atualizar elementos da tela por uma thread externa a do javaFX.
    */
    private fun setUpStorageData() {
        val t = Thread {
            while (true) {
                val storageInfo = Storage().getDiskUsage()
                Platform.runLater {
                    totalStorage.text = storageInfo.totalSpace.toInt().toString() + " Mb"
                    freeStorage.text = storageInfo.freeSpace.toInt().toString() + " Mb"
                    setUpStorageBar(
                        totalSpace = storageInfo.totalSpace, freeSpace = storageInfo.freeSpace
                    )
                }
                try {
                    Thread.sleep(sleep)
                } catch (ex: InterruptedException) {
                    break
                }
            }
        }
        t.name = "Storage Data & Chart"
        t.isDaemon = true
        t.start()

    }
    /**
    Função cria uma thread para executar a atualização da porção da tela dedicada a CPU.
     * Como não existe a possibilidade de uma thread externa alterar elementos da tela, é usado o comando Platform.runLater para atualizar elementos da tela por uma thread externa a do javaFX.
     * Sleep do gráfico é atualizado dependendo do valor selecionado no comboBox de intervalo.
     */
    private fun setUpCPUData() {
        val t = Thread {
            while (true) {
                val data = CPU().getCPUData()
                Platform.runLater {
                    cpuModel.text = data.modelName
                    val df = DecimalFormat("#.##")
                    df.roundingMode = RoundingMode.DOWN
                    maxSpeed.text = df.format(data.maxSpeed).toString() + "GHz"
                    cpuUsage.text = String.format("%.2f", data.percentageUsage) + " %"
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
        t.name = "CPU Data & Chart"
        t.isDaemon = true
        t.start()
    }

    private fun setUpCPUChart(cpuData: CpuUsage) {
        val series = Series<String, Number>()
        series.data.add(XYChart.Data("$cpuChartCounter segs", cpuData.percentageUsage))
        cpuConsumptionChart.data.add(series)
        cpuChartCounter += (sleep / 1000).toInt()

    }
    /**
    Função cria uma thread para executar a atualização da tela dedicada a memória.
     * Como não existe a possibilidade de uma thread externa alterar elementos da tela, é usado o comando Platform.runLater para atualizar elementos da tela por uma thread externa a do javaFX.
     * Sleep do gráfico é atualizado dependendo do valor selecionado no comboBox de intervalo.
     */
    private fun setUpMemData() {
        val t = Thread {
            while (true) {
                val memData = Memory().getMemData()
                Platform.runLater {
                    usedMemLabel.text = (memData.usedMem).toString() + " Mb"
                    totalMemLabel.text = (memData.totalMem).toString() + " Mb"
                    freeMemLabel.text = (memData.freeMem).toString() + " Mb"
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
        t.name = "Mem Data & Chart"
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