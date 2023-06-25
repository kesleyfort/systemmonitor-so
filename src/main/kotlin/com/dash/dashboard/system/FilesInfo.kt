package com.dash.dashboard.system
import com.dash.dashboard.FilesController
import com.dash.dashboard.models.FileData
import javafx.scene.control.TreeItem
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import java.text.SimpleDateFormat
import java.util.*

class FilesInfo {
  /*  fun listFilesWithAttributes(directoryPath: Path): MutableList<FileData> {
        val entries = Files.list(directoryPath)
        val filesmutableList: MutableList<FileData> = mutableListOf()
        for (entry in entries) {
            if (Files.isRegularFile(entry)) {
                val attributes = Files.readAttributes(entry, BasicFileAttributes::class.java)
                val name=entry.fileName.toString()
                val size=attributes.size().toString()
                val permission=Files.getPosixFilePermissions(entry).toString()

                        filesmutableList.add(FileData(name = name,size=size,permission=permission))

                println()
            }
        }
        return filesmutableList
}
*/
    fun getFilesystemInfo(): List<FileData> {
        val filesystemInfo = mutableListOf<FileData>()

        val partitions = java.io.File("/proc/mounts").readLines()
                .filter { it.startsWith("/dev/") }
                .map { it.split(" ")[1] }

        for (partition in partitions) {
            val file = File(partition)
            val totalSpace = file.totalSpace
            val usableSpace = file.usableSpace
            val usedSpace = totalSpace - usableSpace
            val usagePercentage = (usedSpace.toDouble() / totalSpace.toDouble()) * 100.0

            val nome=partition.toString()
            val sizeTotal=formatBytes(totalSpace).toString()
            val usedSpacePartion=formatBytes(usedSpace).toString()
            val permission=Files.getPosixFilePermissions(file.toPath()).toString()
                   /* "Total Space: ${formatBytes(totalSpace)}\n" +
                    "Used Space: ${formatBytes(usedSpace)}\n" +
                    "Usage Percentage: ${String.format("%.2f", usagePercentage)}%\n"*/

            filesystemInfo.add(FileData(name=nome, TotalSpace = sizeTotal, usedSpace = usedSpacePartion, permission = permission))
        }

        return filesystemInfo
    }
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

    private fun getFileSize(file: File): Long {
        val command = "du -sb ${file.absolutePath} | awk '{print \$1}'"
        val output = runCommand(command).trim()
        return output.toLongOrNull() ?: 0L
    }

    fun getModifiedDate(file: File): String {
        val lastModified = file.lastModified()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val modifiedDate = Date(lastModified)

        return dateFormat.format(modifiedDate)
    }

    fun createDirectoryNode(d: File): TreeItem<FilesController.FileAttributes> {
        val directory = File(d.absolutePath)
        val totalsize = formatBytes(getFileSize( directory))

        val lastModified=getModifiedDate(directory)
        val permission=Files.getPosixFilePermissions(directory.toPath()).toString()

        val directoryNode = TreeItem(FilesController.FileAttributes(directory.name, directory.isDirectory, permission, lastModified, totalsize))

        val files = directory.listFiles()

        if (files != null) {
            for (file in files) {
                val fileSize = getFileSize(file)
                val sizeTotal = formatBytes(fileSize).toString()


                val fileNode = if (file.isDirectory) {
                    createDirectoryNode(file)
                } else {  val permission=Files.getPosixFilePermissions(file.toPath()).toString()
                    TreeItem(FilesController.FileAttributes(file.name, file.isDirectory,permission, file.lastModified().toString(), sizeTotal))
                }
                directoryNode.children.add(fileNode)
            }
        }

        return directoryNode
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



