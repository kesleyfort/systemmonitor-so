package com.dash.dashboard.system

import com.dash.dashboard.models.ProcessUsage
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class Process {
    /**
     * A função procura por diretórios que tenham nome número no /proc (pois são os referentes a processos), depois itera por esses diretórios acessando
     * o arquivo status, onde verifica se o uuid corresponde com o do usuário logado (função getCurrentUserUUID() para pegar o uuid do usuário logado)
     * Se sim, ele lê o arquivo e adiciona os dados numa collection de um POJO. Após iterar por todos os diretórios a collection é retornada.
     *
     */
    fun getProcessData(): MutableList<ProcessUsage> {
        val user = getCurrentUserUUID()
        val processMutableList: MutableList<ProcessUsage> = mutableListOf()
        val procDir = File("/proc")
        val processes = procDir.list { _, name -> name.matches(Regex("\\d+")) } ?: emptyArray()
        for (process in processes) {
            val procFile = File(procDir, process)
            val statusFile = File(procFile, "status")
            val pid = process.toIntOrNull()
            if (pid != null && statusFile.exists()) {
                val statusLines = statusFile.readLines()
                val nameLine = statusLines.firstOrNull { it.startsWith("Name:") }
                val uidLine = statusLines.firstOrNull { it.startsWith("Uid:") }
                val threadsLine = statusLines.firstOrNull { it.startsWith("Threads:") }
                val memUsedLIne = statusLines.firstOrNull { it.startsWith("RssAnon:") }
                val uid = uidLine?.substringAfter("Uid:")?.trim()!!.split("\t")[0]
                if (user.first.toString() == uid) {
                    val name = nameLine?.substringAfter("Name:")?.trim()
                    val threads = threadsLine?.substringAfter("Threads:")?.trim()!!.split("\t")[0]
                    val memUsed = memUsedLIne?.substringAfter("RssAnon:")?.trim()!!.split(" ")[0].toDouble()
                    processMutableList.add(ProcessUsage(pid, name, "${(memUsed / 1000)} Mb".replace(".", ","), threads, user.second))
                }
            }
        }
        return processMutableList
    }

    /**
     * Função usa dois comandos do sistema para conseguir o uid e nome de usuário logado, depois retorna um Pair com estes dados.
     */
    private fun getCurrentUserUUID(): Pair<Int?, String> {
        val process = Runtime.getRuntime().exec("id -u --user")
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        val uid = reader.readLine().toIntOrNull()
        var user = ""
        if (uid != null) {
            val userProcess = Runtime.getRuntime().exec("getent passwd $uid")
            val userReader = BufferedReader(InputStreamReader(userProcess.inputStream))
            val userLine = userReader.readLine().trim()
            userProcess.waitFor()
            user = userLine.split(":")[0]
        }
        return Pair(uid, user)
    }
}