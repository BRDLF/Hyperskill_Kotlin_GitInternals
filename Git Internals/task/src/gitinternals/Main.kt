package gitinternals

import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.zip.InflaterInputStream

fun ByteArray.toHex(): String = joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }
fun parseTree(rest: List<Byte>) {
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
fun parseCommit(block: String) {
    val lines = block.split("\n")
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

fun main() {
    try {
        val separator = File.separator
        println("Enter .git directory location:")
        val directoryLocation = readln()
        println("Enter git object hash:")
        val objectHash = readln().run { "${take(2)}${separator}${substring(2)}" }

        val fp = "${directoryLocation}${separator}objects${separator}${objectHash}"
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
                parseCommit(notHeader.joinToString(""){ it.toInt().toChar().toString()})
            }
            "TREE" -> {
                parseTree(notHeader)
            }
        }
    }catch (e: FileNotFoundException) { println(e.message) }
}