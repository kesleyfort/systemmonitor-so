package com.dash.dashboard.system

import com.dash.dashboard.models.ProcessUsage
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class Process {

    private fun getUserByUid(uid: Int): Pair<Int, String> {
        val userProcess = Runtime.getRuntime().exec("getent passwd $uid")
        val userReader = BufferedReader(InputStreamReader(userProcess.inputStream))
        val userLine = userReader.readLine().trim()
        userProcess.waitFor()
        val user = userLine.split(":")[0]
        return Pair(uid, user)
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

    /**
     * Retorna uma lista de processos filhos do usuário atual ou de todos os usuários.
     *
     * @param allUsers Se verdadeiro, retorna processos filhos de todos os usuários. Caso contrário, retorna apenas processos filhos do usuário atual.
     * @return Uma lista de objetos [ProcessUsage] representando os processos filhos.
     */
    fun getChildrenProcesses(allUsers: Boolean): MutableList<ProcessUsage> {
        val user = getCurrentUserUUID()
        val childProcessList: MutableList<String> = mutableListOf()
        val processArray: MutableList<ProcessUsage> = mutableListOf()
        if (allUsers) {
            getProcessesForAllUsers(childProcessList, processArray)
        } else {
            getProcessesForCurrentUser(childProcessList, user, processArray)
        }
        return processArray
    }

    /**
     * Preenche as listas com informações sobre os processos filhos de todos os usuários.
     *
     * @param childProcessList Lista mutável para armazenar os IDs dos processos filhos.
     * @param processArray Lista mutável para armazenar objetos [ProcessUsage] representando os processos filhos.
     */
    private fun getProcessesForAllUsers(
        childProcessList: MutableList<String>, processArray: MutableList<ProcessUsage>
    ) {
        val process = Runtime.getRuntime().exec("pstree -p 1")
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        reader.readLines().forEach {
            val regex = Regex("\\((\\d+)\\)")
            val matchResult = regex.find(it)
            matchResult?.groupValues?.get(1)
            childProcessList.add(matchResult?.groupValues?.get(1) ?: "")
        }
        for (childProcess in childProcessList) {
            val procDir = File("/proc")
            val procFile = File(procDir, childProcess)
            val statusFile = File(procFile, "status")
            val pid = childProcess
            if (statusFile.exists()) {
                val statusLines = statusFile.readLines()
                val nameLine = statusLines.firstOrNull { it.startsWith("Name:") }
                val uidLine = statusLines.firstOrNull { it.startsWith("Uid:") }
                val threadsLine = statusLines.firstOrNull { it.startsWith("Threads:") }
                val memUsedLIne = statusLines.firstOrNull { it.startsWith("RssAnon:") }
                val pPidLine = statusLines.firstOrNull { it.startsWith("PPid:") }
                val uid = uidLine?.substringAfter("Uid:")?.trim()!!.split("\t")[0]
                val name = nameLine?.substringAfter("Name:")?.trim()
                val threads = threadsLine?.substringAfter("Threads:")?.trim()!!.split("\t")[0]
                val pPid = pPidLine?.substringAfter("PPid:")?.trim()!!.split("\t")[0]
                var memUsed: Double = 0.0
                if (memUsedLIne != null) {
                    memUsed = memUsedLIne.substringAfter("RssAnon:").trim().split(" ")[0].toDouble()
                }
                if (name!!.contains(Regex(":+\\w*"))) {
                } else if (pid != "1") {
                    val uid = uidLine.substringAfter("Uid:").trim().split("\t")[0]
                    createProcessTree(processArray, pPid, pid, name, memUsed, threads, uid)
                } else {
                    processArray.add(
                        ProcessUsage(
                            pid.toInt(),
                            name,
                            "${(memUsed / 1000)} Mb".replace(".", ","),
                            threads,
                            getUserByUid(uid.toInt()).second,
                            mutableListOf()
                        )
                    )
                }
            }

        }
    }

    /**
     * Preenche as listas com informações sobre os processos filhos do usuário atual.
     *
     * @param childProcessList Lista mutável para armazenar os IDs dos processos filhos.
     * @param user Par contendo o ID e o nome do usuário atual.
     * @param processArray Lista mutável para armazenar objetos [ProcessUsage] representando os processos filhos.
     */
    private fun getProcessesForCurrentUser(
        childProcessList: MutableList<String>, user: Pair<Int?, String>, processArray: MutableList<ProcessUsage>
    ) {
        var process = Runtime.getRuntime().exec("ps -C systemd -o pid")
        var reader = BufferedReader(InputStreamReader(process.inputStream))
        val systemdPid = reader.readLines()[2].trim()
        process = Runtime.getRuntime().exec("pstree -p $systemdPid")
        reader = BufferedReader(InputStreamReader(process.inputStream))
        reader.readLines().forEach {
            val regex = Regex("\\((\\d+)\\)")
            val matchResult = regex.find(it)
            matchResult?.groupValues?.get(1)
            childProcessList.add(matchResult?.groupValues?.get(1) ?: "")
        }
        for (childProcess in childProcessList) {
            val procDir = File("/proc")
            val procFile = File(procDir, childProcess)
            val statusFile = File(procFile, "status")
            val pid = childProcess
            if (statusFile.exists()) {
                val statusLines = statusFile.readLines()
                val nameLine = statusLines.firstOrNull { it.startsWith("Name:") }
                val uidLine = statusLines.firstOrNull { it.startsWith("Uid:") }
                val threadsLine = statusLines.firstOrNull { it.startsWith("Threads:") }
                val memUsedLIne = statusLines.firstOrNull { it.startsWith("RssAnon:") }
                val pPidLine = statusLines.firstOrNull { it.startsWith("PPid:") }
                val uid = uidLine?.substringAfter("Uid:")?.trim()!!.split("\t")[0]
                val name = nameLine?.substringAfter("Name:")?.trim()
                val threads = threadsLine?.substringAfter("Threads:")?.trim()!!.split("\t")[0]
                val pPid = pPidLine?.substringAfter("PPid:")?.trim()!!.split("\t")[0]
                var memUsed: Double = 0.0
                if (memUsedLIne != null) {
                    memUsed = memUsedLIne.substringAfter("RssAnon:").trim().split(" ")[0].toDouble()
                }
                if (name!!.contains(Regex(":+\\w*"))) {
                } else if (pid != systemdPid) {
                    val uid = uidLine.substringAfter("Uid:").trim().split("\t")[0]
                    if (user.first.toString() == uid) {
                        createProcessTree(processArray, pPid, pid, name, memUsed, threads, uid)
                    }
                } else {
                    processArray.add(
                        ProcessUsage(
                            pid.toInt(),
                            name,
                            "${(memUsed / 1000)} Mb".replace(".", ","),
                            threads,
                            getUserByUid(uid.toInt()).second,
                            mutableListOf()
                        )
                    )
                }
            }

        }
    }

    /**
     * Adiciona informações sobre um processo filho à lista de processos.
     *
     * @param processArray Lista mutável de objetos [ProcessUsage] representando os processos filhos.
     * @param pPid ID do processo pai.
     * @param pid ID do processo filho.
     * @param name Nome do processo filho.
     * @param memUsed Memória usada pelo processo filho.
     * @param threads Número de threads do processo filho.
     * @param uid ID do usuário que possui o processo filho.
     */
    private fun createProcessTree(
        processArray: MutableList<ProcessUsage>,
        pPid: String,
        pid: String,
        name: String?,
        memUsed: Double,
        threads: String,
        uid: String
    ) {
        if (processArray[0].children?.firstOrNull { it.id == pPid.toInt() } != null) {
            processArray[0].children?.first { it.id == pPid.toInt() }?.children?.add(
                ProcessUsage(
                    pid.toInt(),
                    name,
                    "${(memUsed / 1000)} Mb".replace(".", ","),
                    threads,
                    getUserByUid(uid.toInt()).second,
                    mutableListOf()
                )
            )
        } else {
            processArray[0].children?.add(
                ProcessUsage(
                    pid.toInt(),
                    name,
                    "${(memUsed / 1000)} Mb".replace(".", ","),
                    threads,
                    getUserByUid(uid.toInt()).second,
                    mutableListOf()
                )
            )
        }
    }

    fun getDataForSpecificProcess(id: String): Triple<String, String, String> {
        val procDir = File("/proc")
        val procFile = File(procDir, id)
        val statusFile = File(procFile, "status")
        if (statusFile.exists()) {
            val statusLines = statusFile.readLines()
            val stateLine = statusLines.firstOrNull { it.startsWith("State:") }
            val pPidLine = statusLines.firstOrNull { it.startsWith("PPid:") }
            val pPid = pPidLine?.substringAfter("PPid:")?.trim()!!.split("\t")[0]
            val state = stateLine?.substringAfter("State:")?.trim()!!.split("\t")[0]
            val process = Runtime.getRuntime().exec("ps -p $id -o %cpu")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val cpuUsage = reader.readLines()[1].trim()
            return Triple(pPid, state, cpuUsage)
        }
        return Triple("", "", "")
    }
}