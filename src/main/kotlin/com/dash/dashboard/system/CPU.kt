package com.dash.dashboard.system



import com.dash.dashboard.models.CpuUsage
import javafx.application.Platform
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import javax.swing.text.html.HTML.Tag.OL
import kotlin.concurrent.thread


class CPU {
   public var prevTotalTime: Long = 0
    public var prevIdleTime: Long= 0
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

                        // processa os valores da CPU
                        // ...



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
/**
O código lê a primeira linha do arquivo "/proc/stat" e descarta a primeira palavra dessa linha,
que é sempre "cpu". Em seguida, ele soma todos os valores de tempo encontrados nessa primeira linha
para obter o tempo total. Ele divide a quarta coluna "idle" pelo tempo total para obter a fração de tempo gasta ociosa.
Em seguida, subtrai essa fração anterior da fração atual de 1.0 para obter o tempo gasto não ocioso.
Multiplica-se por 100 para obter uma porcentagem.
 * */
    fun getCPUData(): CpuUsage {

        val cpuFile = File("/proc/cpuinfo")
        val cpuStats = cpuFile.readLines();
        val model=cpuStats[4].split(":")[1]
        val maxSpeed=(cpuStats[7].split(":")[1].toDouble()/1000)

        val statsFile = File("/proc/stat")
        val firstLine = statsFile.readLines()[0].drop(5)

        val split = firstLine.split(' ')
        val idleTime = split[3].toLong()
        val totalTime = split.map { it.toLong() }.sum()

            val deltaIdleTime  = idleTime  - prevIdleTime
            val deltaTotalTime = totalTime - prevTotalTime
            val cpuUsage = (1.0 - deltaIdleTime.toDouble() / deltaTotalTime) * 100.0
            println(" ${"%6.3f".format(cpuUsage)}")

        prevIdleTime  = idleTime
        prevTotalTime = totalTime
        return CpuUsage(modelname =  model, maxSpeed = maxSpeed, porcentUsage = cpuUsage);
            }

    }