package com.dash.dashboard

import com.dash.dashboard.models.CpuUsage
import com.dash.dashboard.models.MemUsage
import com.dash.dashboard.models.ProcessUsage
import com.dash.dashboard.system.CPU
import com.dash.dashboard.system.Memory
import com.dash.dashboard.system.Process
import com.dash.dashboard.system.Storage
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.chart.LineChart
import javafx.scene.chart.XYChart
import javafx.scene.chart.XYChart.Series
import javafx.scene.control.*
import javafx.scene.control.cell.TreeItemPropertyValueFactory
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
    lateinit var processTable: TreeTableView<ProcessUsage>

    @FXML // fx:id="idCol"
    lateinit var idCol: TreeTableColumn<ProcessUsage, Number>

    @FXML // fx:id="processCol"
    lateinit var processCol: TreeTableColumn<String, String>

    @FXML // fx:id="memCol"
    lateinit var memCol: TreeTableColumn<String, String>

    @FXML // fx:id="threadsCol"
    lateinit var threadsCol: TreeTableColumn<String, String>

    @FXML // fx:id="userCol"
    lateinit var userCol: TreeTableColumn<String, String>

    @FXML // fx:id="totalMemLabel"
    private lateinit var maxSpeed: Label

    @FXML // fx:id="checkboxProcessos"
    private lateinit var checkboxProcessos: CheckBox

    private var sleep = 5000L
    private var chartCounter = 0
    private var cpuChartCounter = 0


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

        val t = Thread {
            while (true) {
                val allUsers = checkboxProcessos.isSelected
                val processData = Process().getChildrenProcesses(allUsers)
                val root: TreeItem<ProcessUsage> = TreeItem<ProcessUsage>(
                   processData[0]
                )
//                processData.removeAt()
                root.isExpanded = true
                for(process in processData[0].children!!) {
                    root.children.add(TreeItem(process))
                    if(process.children!!.size > 0){
                        for(child in process.children)
                        root.children.find { it.value.id == process.id}!!.children.add(TreeItem(child))
                    }
                }
                Platform.runLater {
                    idCol.cellValueFactory = TreeItemPropertyValueFactory("id")
                    processCol.cellValueFactory = TreeItemPropertyValueFactory("processName")
                    memCol.cellValueFactory = TreeItemPropertyValueFactory("memUsed")
                    threadsCol.cellValueFactory = TreeItemPropertyValueFactory("threads")
                    userCol.cellValueFactory = TreeItemPropertyValueFactory("user")
                    processTable.root = root
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