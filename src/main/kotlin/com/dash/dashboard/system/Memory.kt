package com.dash.dashboard.system

import com.dash.dashboard.models.MemUsage
import java.io.File


class Memory {

    fun getMemData(): MemUsage {
        val memFile = File("/proc/meminfo")
        val memInfo = memFile.readLines()

        val totalMem = memInfo[0].split(":")[1].trim().split(" ")[0].toLong()
        val freeMem = memInfo[1].split(":")[1].trim().split(" ")[0].toLong() + memInfo[4].split(":")[1].trim()
            .split(" ")[0].toLong()
        val usedMem = totalMem - freeMem
        val usedMemPercentage = (usedMem.toDouble() / totalMem.toDouble()) * 100
        return MemUsage(totalMem / 1024, freeMem / 1024, usedMem / 1024, usedMemPercentage)
    }
}