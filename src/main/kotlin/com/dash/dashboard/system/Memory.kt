package com.dash.dashboard.system

import com.dash.dashboard.models.MemUsage
import java.io.File


class Memory {
    /**
     * Função criada para pegar os dados do consumo de memória dentro da partição proc, no arquivo meminfo.
     * Após abrir o arquivo, analisamos as linhas que mostram o valor de memória total, memória livre, fazemos o cálculo da memória usada e depois calculamos o percentual de memória usada.
     * Depois é retornado um POJO contendo os dados.
     */
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