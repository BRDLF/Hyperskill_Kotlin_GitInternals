package gitinternals

import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.util.*
import java.util.zip.InflaterInputStream

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
        if (scanner.hasNext()) {
            val header = scanner.next().split(" ")
            println("type:${header[0]} length:${header[1]}")
        }
    }catch (e: FileNotFoundException) {
        println(e.message)
    }

}
