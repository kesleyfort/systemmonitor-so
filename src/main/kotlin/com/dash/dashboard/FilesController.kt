package com.dash.dashboard

import com.dash.dashboard.models.FileData
import com.dash.dashboard.system.FilesInfo
import javafx.collections.FXCollections
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.control.cell.TreeItemPropertyValueFactory
import javafx.stage.Stage
import java.io.File
import java.net.URL
import java.nio.file.Path
import java.nio.file.Paths
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
        val rootDirectory = File("/home/lucao/IdeaProjects/") //trocar pelo seu diretorio

//Adiciona na raiz da treeTableView o nó criado apartir da função  createDirectoryNode
        val rootNode = FilesInfo().createDirectoryNode(rootDirectory)

        treeName = TreeTableColumn<FileAttributes, String>("NOME")
        treeName.cellValueFactory = TreeItemPropertyValueFactory("treeName")
        treeName.prefWidth=200.0

        treeTotalSpace = TreeTableColumn("TAMANHO")
        treeTotalSpace.cellValueFactory = TreeItemPropertyValueFactory("treeTotalSpace")

        treePermissions = TreeTableColumn("PERMISSÕES")
        treePermissions.cellValueFactory = TreeItemPropertyValueFactory("permissions")

        lastModified = TreeTableColumn("ÚLTIMO ACESSO")
        lastModified.cellValueFactory = TreeItemPropertyValueFactory("lastModified")


        tree.columns.addAll(treeName,treeTotalSpace,lastModified,treePermissions/*treePermissions treeSize, treeIsDirectory, treeLastModified*/)
        tree.isShowRoot = true
        tree.root=rootNode


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

        val directoryPath: Path = Paths.get("/")
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
            val permissions:String,
            val  lastModified:String,
            val treeTotalSpace:String,

    )



}
