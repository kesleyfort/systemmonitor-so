package com.dash.dashboard.system

import com.dash.dashboard.FilesController
import com.dash.dashboard.models.FileData
import javafx.scene.control.TreeItem
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.attribute.PosixFileAttributes
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class FilesInfo {

    /**a função [getFilesystemInfo] executado o comando df -h
     * que retorna as informações das partições do sistema
     * Após isso, as linhas são tratadas e obtem-se as colunas que retornam uma
     * informação sobre o tamanho total e usado, assim como o nome da partição e também onde é montada
     * E por último é adicionado em um lista que será usada na criação da tabela
     * */
    fun getFilesystemInfo(): List<FileData> {

        val filesystemInfo = mutableListOf<FileData>()

        val process = Runtime.getRuntime().exec("df -h")
        val reader = BufferedReader(InputStreamReader(process.inputStream))

        var line: String?
        line = reader.readLine()

        while (line != null) {
            val tokens = line.split("\\s+".toRegex())

            filesystemInfo.add(
                FileData(
                    name = tokens[0],
                    TotalSpace = tokens[1],
                    usedSpace = tokens[2],
                    mounted = tokens[5]
                )
            )
            line = reader.readLine()
        }

        reader.close()




        return filesystemInfo
    }

    /**A função [runCommand] executa um comando
     * do sistema operacional usando o interpretador Bash,
     *lê a saída do comando e retorna essa saída como uma string*/

    private fun runCommand(command: String): String {
        val process = Runtime.getRuntime().exec(arrayOf("/bin/bash", "-c", command))
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        val output = StringBuilder()
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            output.append(line)
        }
        reader.close()
        process.waitFor()
        return output.toString()
    }

    /***A função [getFileSize]obtém o tamanho de um arquivo especificado,
     * usando o comando du para obter o tamanho em bytes,
     * através do interpretador Bash
     * */
    private fun getFileSize(file: File): Long {
        val command = "du -sb ${file.absolutePath.replace(" ", "\\ ")} | awk '{print \$1}'"
        val output = runCommand(command).trim()
        return output.toLongOrNull() ?: 0L
    }

    /***A funçãp [getModifiedDate] recebe um arquivo e obtem sua data da última modificação
     * Em seguida formata a string para dd/MM/yyyy HH:mm:ss"
     * e retorna o a informação
     * */
    fun getModifiedDate(file: File): String {
        val lastModified = file.lastModified()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val modifiedDate = Date(lastModified)

        return dateFormat.format(modifiedDate)
    }

    /***A função [createDirectoryNode] cria um nó de diretório para exibição em uma estrutura de árvore.
     *  Ela recebe um diretório d como parâmetro. em seguida obtem o tamanho e outras informação desse arquivo
     *  Em seguida, percorre os arquivos no diretório e, para cada arquivo, cria um nó correspondente,
     *  seja um diretório ou um arquivo, e adiciona-o como filho do nó de diretório.
     *  E por último, retorna o nó do diretório criado
     * */
    fun createDirectoryNode(d: File): TreeItem<FilesController.FileAttributes> {
        val directory = File(d.absolutePath)
        val totalsize = formatBytes(getFileSize(directory))

        val attrs = Files.readAttributes(d.toPath(), PosixFileAttributes::class.java)
        val permission = attrs.permissions().toString()
        val crudeDate = attrs.lastModifiedTime().toString().replace(Regex("(\\.\\d+Z|Z\\.\\d+|Z)"), "")
        val lastModified = convertDateFormat(crudeDate)

        val directoryNode = TreeItem(
            FilesController.FileAttributes(
                directory.name,
                directory.isDirectory,
                permission,
                lastModified,
                totalsize
            )
        )
       val files = directory.listFiles { _, name -> !name.startsWith(".") || !name.contains("snap") }
        files?.forEach  { file ->
            val fileSize = getFileSize(file)
            val sizeTotal = formatBytes(fileSize)
            var fileNode: TreeItem<FilesController.FileAttributes> = TreeItem()
            if(file.exists()){
                fileNode = if (file.isDirectory && !file.name.startsWith(".") && !file.name.contains("snap")) {
                    createDirectoryNode(file)
                } else
                {
                    val attributes = Files.readAttributes(file.toPath(), PosixFileAttributes::class.java)
                    val perm = attributes.permissions().toString()
                    val date = attributes.lastModifiedTime().toString().replace(Regex("(\\.\\d+Z|Z\\.\\d+|Z)"), "")
                    val lastmod = convertDateFormat(date)
                    TreeItem(
                        FilesController.FileAttributes(
                            file.name,
                            file.isDirectory,
                            perm,
                            lastmod,
                            sizeTotal
                        )
                    )
                }
            }

            directoryNode.children.add(fileNode)
        }

        return directoryNode
    }

    private fun convertDateFormat(input: String): String {
        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        val outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")

        val dateTime = LocalDateTime.parse(input, inputFormatter)
        val output = dateTime.format(outputFormatter)
        return output
    }


    private fun formatBytes(bytes: Long): String {
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var size = bytes.toDouble()
        var unitIndex = 0

        while (size >= 1024 && unitIndex < units.size - 1) {
            size /= 1024
            unitIndex++
        }

        return String.format("%.2f %s", size, units[unitIndex])
    }


}



