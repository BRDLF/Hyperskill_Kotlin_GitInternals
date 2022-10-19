package gitinternals

import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.zip.InflaterInputStream

fun parse(block: String) {
    val lines = block.split("\n")
    var firstParent = true
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

    var inMessage = false
    for (line in lines) {
        val sL = line.split(" ")
        when (sL.first()) {
            "tree" -> treeLine(sL)
            "parent" -> parentLine(sL)
            "author" -> {
                if (!firstParent) println()
                otherLine(sL, "author", "original")
            }
            "committer" -> otherLine(sL, "committer", "commit")
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

        val scanner = Scanner(InflaterInputStream(FileInputStream(fp)))
        scanner.useDelimiter("\u0000")

        //determine BLOB or COMMIT
        val header = scanner.next()
        when (header.split(" ").first().lowercase()) {
            "blob" -> {
                println("*BLOB*")
                while (scanner.hasNext()) {
                    println(scanner.next())
                }
            }
            "commit" -> {
                println("*COMMIT*")
                while (scanner.hasNext()) {
                    val line = scanner.next()
                    parse(line)
                }
            }
        }
    }catch (e: FileNotFoundException) {
        println(e.message)
    }
}