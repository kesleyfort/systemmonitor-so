package com.dash.dashboard

import com.dash.dashboard.models.FileData
import com.dash.dashboard.system.FilesInfo
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.control.cell.TreeItemPropertyValueFactory
import javafx.stage.Stage
import java.io.File
import java.net.URL
import java.util.*

/***
 * Essa classe é responsável por adionar os dados obtdos a partir da classe FilesInfo
 * em suas respectivas widgets
 */

class FilesController : Initializable {

    lateinit var treePermissions: TreeTableColumn<FileAttributes, Any>

    lateinit var lastModified: TreeTableColumn<FileAttributes, Any>

    lateinit var treeTotalSpace: TreeTableColumn<FileAttributes, Any>

    lateinit var tree: TreeTableView<FileAttributes>

    lateinit var treeName: TreeTableColumn<FileAttributes, String>

    lateinit var usedSizeFile: TableColumn<Any, Any>

    lateinit var totalSizeFile: TableColumn<Any, Any>

    lateinit var FileTable: TableView<FileData>

    lateinit var mountedFile: TableColumn<String, String>

    lateinit var nameFile: TableColumn<String, String>

    private lateinit var telaPrincipalStage: Stage


    override fun initialize(p0: URL?, p1: ResourceBundle?) {
        setUpFilesTable()
        createFileTreeTable()
    }

    private fun createFileTreeTable() {
        val userDirectory = File("/home/${System.getenv("USER")}")
        val rootNode = TreeItem(FileAttributes("/home/${System.getenv("USER")}", true, "", "", ""))
        treeName = TreeTableColumn<FileAttributes, String>("Nome")
        treeName.cellValueFactory = TreeItemPropertyValueFactory("treeName")
        treeName.prefWidth = 200.0

        treeTotalSpace = TreeTableColumn("Tamanho")
        treeTotalSpace.cellValueFactory = TreeItemPropertyValueFactory("treeTotalSpace")

        treePermissions = TreeTableColumn("Permissões")
        treePermissions.cellValueFactory = TreeItemPropertyValueFactory("permissions")

        lastModified = TreeTableColumn("Último acesso")
        lastModified.cellValueFactory = TreeItemPropertyValueFactory("lastModified")


        tree.columns.addAll(
            treeName,
            treeTotalSpace,
            lastModified,
            treePermissions/*treePermissions treeSize, treeIsDirectory, treeLastModified*/
        )
        tree.isShowRoot = true
        tree.root = rootNode
        tree.root.isExpanded = true
        val userDirectories = userDirectory.listFiles { file ->   file.isDirectory && !file.name.startsWith(".") && !file.name.contains("snap") }
            userDirectories?.forEach {
                val thread = Thread {
                    val directory = FilesInfo().createDirectoryNode(it)
                    Platform.runLater {
                        tree.root.children.add(directory)
                    }
                }
                thread.name = "Thread for creating a tree node for ${it.name}"
                thread.isDaemon = true
                thread.start()
            }
    }

    fun setTelaPrincipalStage(stage: Stage) {
        this.telaPrincipalStage = stage
    }

    fun setUpFilesTable() {
        //Inicializa as colunas da tabela
        nameFile.cellValueFactory = PropertyValueFactory("name")
        totalSizeFile.cellValueFactory = PropertyValueFactory("TotalSpace")
        usedSizeFile.cellValueFactory = PropertyValueFactory("usedSpace")
        mountedFile.cellValueFactory = PropertyValueFactory("mounted")
        val partitionInfo = FilesInfo().getFilesystemInfo()
        FileTable.items = FXCollections.observableArrayList(partitionInfo)


    }

    fun voltarParaTelaPrincipal() {
        telaPrincipalStage.show()
        val stage = telaPrincipalStage.scene.window as Stage
        stage.close()
    }

    data class FileAttributes(
        val treeName: String,
        val isDirectory: Boolean,
        val permissions: String,
        val lastModified: String,
        val treeTotalSpace: String,

        )


}
