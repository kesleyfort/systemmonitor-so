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


class FilesController : Initializable {

    lateinit var treePermissions: TreeTableColumn<FileAttributes, Any>
    lateinit var lastModified: TreeTableColumn<FileAttributes, Any>
    lateinit var treeTotalSpace: TreeTableColumn<FileAttributes, Any>
    lateinit var simpleTree: TreeView<String>
    lateinit var tree: TreeTableView<FileAttributes>
    lateinit var treeName: TreeTableColumn<FileAttributes, String>

    //para a tabela de partições
    lateinit var usedSizeFile: TableColumn<Any, Any>
    lateinit var totalSizeFile: TableColumn<Any, Any>
    lateinit var FileTable: TableView<FileData>
    lateinit var permissionFIle: TableColumn<String, String>

    lateinit var nameFile: TableColumn<String, String>


    private lateinit var telaPrincipalStage: Stage


    override fun initialize(p0: URL?, p1: ResourceBundle?) {
        setUpFilesTable()
        val rootDirectory = File("/home/lucao/IdeaProjects/") // Substitua pelo diretório desejado
     /*   val rootNode = createDirectoryNode(rootDirectory)

        simpleTree.root =rootNode
        simpleTree.isShowRoot = true*/
        val rootNode = FilesInfo().createDirectoryNode(rootDirectory)
        //tree = TreeTableView<FileAttributes>()
        treeName = TreeTableColumn<FileAttributes, String>("NOME")
        treeName.cellValueFactory = TreeItemPropertyValueFactory("treeName")
        treeName.prefWidth=200.0

        treeTotalSpace = TreeTableColumn("TAMANHO")
        treeTotalSpace.cellValueFactory = TreeItemPropertyValueFactory("treeTotalSpace")

        treePermissions = TreeTableColumn("PERMISSoES")
        treePermissions.cellValueFactory = TreeItemPropertyValueFactory("permissions")

        lastModified = TreeTableColumn("ÚLTIMO ACESSO")
        lastModified.cellValueFactory = TreeItemPropertyValueFactory("lastModified")

       /* treePermissions= TreeTableColumn("permissoes")
        treePermissions.cellValueFactory = TreeItemPropertyValueFactory("treePermissions")*/

        tree.columns.addAll(treeName,treeTotalSpace,lastModified,treePermissions/*treePermissions treeSize, treeIsDirectory, treeLastModified*/)
        tree.isShowRoot = true
        tree.root=rootNode
        /*  val treeSize = TreeTableColumn<FileAttributes, Long>("Size")
          treeSize.cellValueFactory = TreeItemPropertyValueFactory<FileAttributes, Long>("size")

          val treeIsDirectory = TreeTableColumn<FileAttributes, Boolean>("Is Directory")
          treeIsDirectory.cellValueFactory = TreeItemPropertyValueFactory<FileAttributes, Boolean>("isDirectory")

          val treeLastModified = TreeTableColumn<FileAttributes, Long>("Last Modified")
          treeLastModified.cellValueFactory = TreeItemPropertyValueFactory<FileAttributes, Long>("lastModified")*/







    /*    val root = TreeItem(FileAttributes("DIRETORIOS", true))
        tree.root = populateTreeTableView("/",root)*/

     //   val directory = "/home/" // substitua pelo caminho do diretório desejado


        //populateTreeTableView(directory, root)

    }

    fun setTelaPrincipalStage(stage: Stage) {
        this.telaPrincipalStage = stage
    }

    fun setUpFilesTable() {
        // data class FileData (val name:String, val size:String, val usedSpace: String, val TotalSpace:String,val permission: String)
        nameFile.cellValueFactory = PropertyValueFactory("name")
        totalSizeFile.cellValueFactory = PropertyValueFactory("TotalSpace")
        usedSizeFile.cellValueFactory = PropertyValueFactory("usedSpace")
        permissionFIle.cellValueFactory = PropertyValueFactory("permission")
        val directoryPath: Path = Paths.get("/")
        //val info = FilesInfo().listFilesWithAttributes(directoryPath)


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
            /* val treePermissions:String,*/
            val permissions:String,
            val  lastModified:String,
            val treeTotalSpace:String,
            /* val size: Long,
            val isDirectory: Boolean,
            val lastModified: Long*/
    )
    private fun createTreeItem(file: File): TreeItem<File> {
        val treeItem = TreeItem(file)
        if (file.isDirectory) {
            val children = file.listFiles()
            children?.forEach { child ->
                val childTreeItem = createTreeItem(child)
                treeItem.children.add(childTreeItem)
            }
        }
        return treeItem
    }


}
