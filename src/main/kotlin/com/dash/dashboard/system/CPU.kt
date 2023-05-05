package com.dash.dashboard.system

import com.dash.dashboard.models.CPUUsage
import javafx.application.Platform
import java.io.File
import java.text.SimpleDateFormat
import kotlin.concurrent.thread


class CPU {
    public fun testThread(){
        val t = Thread {
            while (true) {
                val sdf = SimpleDateFormat("HH:mm:ss")
                Platform.runLater(Runnable { })
                try {
                    Thread.sleep(100)
                } catch (ex: InterruptedException) {
                    break
                }
            }
        }
        t.name = "Runnable Time Updater"
        t.isDaemon = true
        t.start()
    }
    public fun createThread(sleep: Long){
        val prevCpuTime = LongArray(7)
        val prevIdleTime = LongArray(1)
        val cpuUsage = DoubleArray(1)
        thread(start = true) {
            while (true) {
                val cpuFile = File("/proc/stat")
                val cpuStats = cpuFile.readLines()[0].split("\\s+".toRegex()).map { it.toLong() }

                val user = cpuStats[0]
                val nice = cpuStats[1]
                val system = cpuStats[2]
                val idle = cpuStats[3]
                val iowait = cpuStats[4]
                val irq = cpuStats[5]
                val softirq = cpuStats[6]

                val totalCpuTime = user + nice + system + idle + iowait + irq + softirq
                val cpuTime = totalCpuTime - prevCpuTime.sum()
                val idleTime = idle - prevIdleTime[0]
                cpuUsage[0] = ((cpuTime.toDouble() / (cpuTime + idleTime).toDouble()) * 100)

                prevCpuTime[0] = user
                prevCpuTime[1] = nice
                prevCpuTime[2] = system
                prevCpuTime[3] = idle
                prevCpuTime[4] = iowait
                prevCpuTime[5] = irq
                prevCpuTime[6] = softirq

                prevIdleTime[0] = idle

                Thread.sleep(sleep) // Sleep for 1 second before getting new CPU usage data
            }
        }
    }

    fun getCPUData(): String {
    return ""
    }
}