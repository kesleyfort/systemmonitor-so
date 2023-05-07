package com.dash.dashboard.system

import com.dash.dashboard.models.StorageInfo
import java.io.File

class Storage {/**
 Aprtir da raiz do sistema, calculamos quanto espaço tem no total e disponivel
 através das funções
 Em seguida transformamos o valor de bytes para MegaBytes
 */
    fun getDiskUsage(): StorageInfo {
        val root = File("/")
        val totalSpace = root.totalSpace/1000000
        println("O armazenamento total é de $totalSpace Mb.")

        val freeSpace = root.freeSpace / 1000000// convert to gigabytes
        println("Armazenamento disponível é de $freeSpace Mb")
        return StorageInfo(totalSpace=totalSpace.toDouble(),freeSpace=freeSpace.toDouble())
    }
}