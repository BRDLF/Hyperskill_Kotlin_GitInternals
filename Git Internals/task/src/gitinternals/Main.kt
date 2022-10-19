package gitinternals

import java.io.FileInputStream
import java.io.FileNotFoundException
import java.util.*
import java.util.zip.InflaterInputStream

fun main() {
    println("Enter git object location:")
    try {
        val iis = InflaterInputStream(FileInputStream(readln()))
        val scanner = Scanner(iis)
        scanner.useDelimiter("\u0000")
        while (scanner.hasNext()) {
            println(scanner.next())
        }
    }catch (e: FileNotFoundException) {
        println(e.message)
    }

}
