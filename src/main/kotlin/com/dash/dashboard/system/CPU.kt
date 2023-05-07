package com.dash.dashboard.system


import com.dash.dashboard.models.CpuUsage
import java.io.File


class CPU {
    private var prevTotalTime: Long = 0
    private var prevIdleTime: Long = 0

    /**
    O código lê a primeira linha do arquivo "/proc/stat" e descarta a primeira palavra dessa linha,
    a qual é sempre "cpu". Em seguida, ele soma todos os valores de tempo encontrados nessa primeira linha
    para obter o tempo total. Ele divide a quarta coluna "idle" pelo tempo total para obter a fração de tempo gasta ociosa.
    Em seguida, subtrai essa fração anterior da fração atual de 1.0 para obter o tempo gasto não ocioso.
    Multiplica-se por 100 para obter uma porcentagem.
     * */
    fun getCPUData(): CpuUsage {

        val cpuFile = File("/proc/cpuinfo")
        val cpuStats = cpuFile.readLines()
        val model = cpuStats[4].split(":")[1]
        val maxSpeed = (cpuStats[7].split(":")[1].toDouble() / 1000)

        val statsFile = File("/proc/stat")
        val firstLine = statsFile.readLines()[0].drop(5)

        val split = firstLine.split(' ')
        val idleTime = split[3].toLong()
        val totalTime = split.map { it.toLong() }.sum()

        val deltaIdleTime = idleTime - prevIdleTime
        val deltaTotalTime = totalTime - prevTotalTime
        val cpuUsage = (1.0 - deltaIdleTime.toDouble() / deltaTotalTime) * 100.0
        prevIdleTime = idleTime
        prevTotalTime = totalTime
        return CpuUsage(modelName = model, maxSpeed = maxSpeed, percentageUsage = cpuUsage)
    }

}