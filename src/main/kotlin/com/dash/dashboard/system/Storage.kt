package com.dash.dashboard.system

import com.dash.dashboard.models.StorageUsage
import java.io.File

class Storage {

    /**
    A partir da raiz do sistema, calculamos quanto espaço total e disponível existe através das funções.
    Em seguida transformamos o valor de bytes para MegaBytes
     */
    fun getDiskUsage(): StorageUsage {
        val root = File("/")
        val totalSpace = root.totalSpace / 1000000
        val freeSpace = root.freeSpace / 1000000
        return StorageUsage(totalSpace.toDouble(), freeSpace.toDouble())
    }
}