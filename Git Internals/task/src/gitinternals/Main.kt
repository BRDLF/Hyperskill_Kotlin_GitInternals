package gitinternals

import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.zip.InflaterInputStream

class GitInternals {
    private val separator = File.separator
    private lateinit var dir: String

    private fun ByteArray.toHex(): String = joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }
    private fun catFileTree(rest: List<Byte>) {
        var varRest = rest
        do {
            val anchor = varRest.indexOf(0.toByte())
            if (anchor == -1) continue
            val (permission, filename) = varRest.take(anchor).joinToString("") { it.toInt().toChar().toString() }.split(" ")
            varRest = varRest.drop(anchor + 1)
            val sha = varRest.take(20).toByteArray().toHex()
            println("$permission $sha $filename")
            varRest = varRest.drop(20)
        } while (anchor != -1)
    }
    private fun catFileCommit(block: List<Byte>) {
        val lines = block.joinToString(""){ it.toInt().toChar().toString() }.split("\n")
        var firstParent = true
        var inMessage = false

        fun treeLine(sL: List<String>) = println("tree: ${sL[1]}")
        fun parentLine(sL: List<String>) {
            if (firstParent) {
                print("parents: ")
                firstParent = false
            } else print(" | ")
            print(sL[1])
        }
        fun otherLine(sL: List<String>, n: String, m: String) {
            print("$n: ")
            print("${sL[1]} ${sL[2].drop(1).dropLast(1)} ")
            print("$m timestamp: ")
            println(Instant.ofEpochSecond(sL[3].toLong()).atZone(ZoneOffset.of(sL[4])).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss xxx")))
        }

        for (line in lines) {
            val lineWords = line.split(" ")
            when (lineWords.first()) {
                "tree" -> treeLine(lineWords)
                "parent" -> parentLine(lineWords)
                "author" -> {
                    if (!firstParent) println()
                    otherLine(lineWords, "author", "original")
                }
                "committer" -> otherLine(lineWords, "committer", "commit")
                "" -> if (!inMessage) {
                    println("commit message:")
                    inMessage = true
                } else println("")
                else -> println(line)
            }
        }
    }
    private fun walkBranch(nextCommit: String) {
        val parentCommits: MutableList<String> = mutableListOf(nextCommit)
        do {
            print("Commit: ${parentCommits.last()}")
            val inMerge: Boolean
            if (parentCommits.size > 1) {
                inMerge = true
                print(" (merged)")
            }
            else { inMerge = false }
            println()

            val hash = parentCommits.last().trimIndent().run { "${take(2)}${separator}${substring(2)}" }
            parentCommits.removeAt(parentCommits.lastIndex)
            val fp = "${dir}${separator}objects${separator}${hash}"
            val iis = InflaterInputStream(FileInputStream(fp))
            val commitLines = iis.readAllBytes().dropWhile { it.toInt() != 0 }.drop(1).joinToString("") { it.toInt().toChar().toString() }.split("\n")

            var inCommitMessage = false
            for (line in commitLines) {
                val lineWords = line.split(" ")
                when (lineWords.first()) {
                    "parent" -> {
                        if (!inMerge) {
                            parentCommits.add(lineWords[1])
                        }
                    }
                    "committer" -> {
                        print("${lineWords[1]} ${lineWords[2].drop(1).dropLast(1)} commit timestamp: ")
                        println(Instant.ofEpochSecond(lineWords[3].toLong()).atZone(ZoneOffset.of(lineWords[4])).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss xxx")))
                    } //committerLine
                    "" -> {
                        if (!inCommitMessage) inCommitMessage = true
                    }
                    else -> if (inCommitMessage) println(line)
                }
            }
            println()
        } while (parentCommits.isNotEmpty())

    }

    private fun listBranches() {
        val head = File("${dir}${separator}HEAD").readLines().first().takeLastWhile { it.toString() != "/" }
        val branches = File("${dir}${separator}refs${separator}heads").listFiles()?.map { it.name }?: throw Exception("No branches found?")
        for (branch in branches) {
            print(if (branch == head) "*" else " ")
            println(" $branch")
        }
    }
    private fun catFile() {
        println("Enter git object hash:")
        val objectHash = readln().run { "${take(2)}${separator}${substring(2)}" }
        val fp = "${dir}${separator}objects${separator}${objectHash}"
        val iis = InflaterInputStream(FileInputStream(fp))
        val arrayBytes = iis.readAllBytes()
        val header = arrayBytes.takeWhile { it.toInt() != 0 }.joinToString("") { it.toInt().toChar().toString() }
        val notHeader = arrayBytes.dropWhile { it.toInt() != 0 }.drop(1)
        val type = header.split(" ").first().uppercase()
        println("*$type*")
        when (type) {
            "BLOB" -> {
                notHeader.forEach { print(it.toInt().toChar()) }
            }
            "COMMIT" -> {
                catFileCommit(notHeader)
            }
            "TREE" -> {
                catFileTree(notHeader)
            }
        }
    }
    private fun log() {
        println("Enter branch name:")
        val branchName = readln()
        try{
            val tip = File("${dir}${separator}refs${separator}heads${separator}${branchName}")
            val nextCommit = tip.readText().trimIndent()

            walkBranch(nextCommit)

        } catch (e: FileNotFoundException) { println(e.message) }
    }
    private fun commitTree() {
        println("Enter commit-hash:")
        val objectHash = readln().run { "${take(2)}${separator}${substring(2)}" }
        val fp = "${dir}${separator}objects${separator}${objectHash}"
        val iis = InflaterInputStream(FileInputStream(fp))
        val arrayBytes = iis.readAllBytes()
        val type = arrayBytes.takeWhile { it.toInt() != 0 }.joinToString("") { it.toInt().toChar().toString() }.split(" ").first().uppercase()
        val notHeader = arrayBytes.dropWhile { it.toInt() != 0 }.drop(1)
        when (type) {
            "COMMIT" -> {
                recurseTree(hash = findTree(notHeader))
            }
            else -> throw Exception("invalid commit-hash. Not a commit")
        }
    }
    private fun findTree(block: List<Byte>): String {
        for (line in block.joinToString("") { it.toInt().toChar().toString() }.split("\n")) {
            if (line.split(" ").first() == "tree") return line.split(" ")[1]
        }
        throw Exception("No tree in Commit")
    }
    private fun recurseTree(parent: String = "", ourName: String = "", hash: String) {
        //if I'm a blob
        //print our Name!
        //if I'm a tree
        //keep going
        //  for each hash, run this program, if ourName isn't "", pass ourName${separator} as parent, filename as ourName, and the hash as the hash


        val objectHash = hash.run { "${take(2)}${separator}${substring(2)}" }
        val fp = "${dir}${separator}objects${separator}${objectHash}"
        val iis = InflaterInputStream(FileInputStream(fp))
        val arrayBytes = iis.readAllBytes()
        val type = arrayBytes.takeWhile { it.toInt() != 0 }.joinToString("") { it.toInt().toChar().toString() }.split(" ").first().uppercase()
        when (type) {
            "BLOB" -> { println("$parent$ourName") }
            "TREE" -> {
                var varRest = arrayBytes.dropWhile { it.toInt() != 0 }.drop(1)
                do {
                    val anchor = varRest.indexOf(0.toByte())
                    if (anchor == -1) continue
                    val (_, filename) = varRest.take(anchor).joinToString("") { it.toInt().toChar().toString() }.split(" ")
                    varRest = varRest.drop(anchor + 1)
                    val sha = varRest.take(20).toByteArray().toHex()
                    val tPP = if (ourName == "") "" else "$ourName/"
                    recurseTree(parent = tPP, ourName = filename, hash = sha)
                    varRest = varRest.drop(20)
                } while (anchor != -1)
            }
        }




    }

    fun begin() {

        try {
            println("Enter .git directory location:")
            dir = readln()

            println("Enter command:")
            when (readln()) {
                "list-branches" -> listBranches()
                "cat-file" -> catFile()
                "log" -> log()
                "commit-tree" -> commitTree()
                else -> throw Exception("Invalid command")
            }
        }catch (e: Exception) { println(e.message) }
    }

}

fun main() {
    GitInternals().begin()
}