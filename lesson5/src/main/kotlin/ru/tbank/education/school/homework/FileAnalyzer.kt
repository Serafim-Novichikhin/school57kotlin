package ru.tbank.education.school.homework


import java.nio.file.NoSuchFileException
import java.io.IOException
import java.nio.file.Files
import java.io.*
import java.nio.charset.StandardCharsets
import java.nio.file.Paths


fun println(str: String) {
    val writer = OutputStreamWriter(System.out, StandardCharsets.UTF_8)
    writer.write((str+"\n"))
    writer.flush()
}
/**
 * Интерфейс для подсчёта строк и слов в файле.
 */
interface FileAnalyzer {

    /**
     * Считает количество строк и слов в указанном входном файле и записывает результат в выходной файл.
     *
     * Словом считается последовательность символов, разделённая пробелами,
     * табуляциями или знаками перевода строки. Пустые части после разделения не считаются словами.
     *
     * @param inputFilePath путь к входному текстовому файлу
     * @param outputFilePath путь к выходному файлу, в который будет записан результат
     * @return true если операция успешна, иначе false
     */
    fun countLinesAndWordsInFile(inputFilePath: String, outputFilePath: String): Boolean
}

class IOFileAnalyzer : FileAnalyzer {
    override fun countLinesAndWordsInFile(inputFilePath: String, outputFilePath: String): Boolean {
        return try {
            var lines = 0
            var words = 0
            BufferedReader(FileReader(inputFilePath)).use { reader ->
                var line: String = ""
                while (reader.readLine().also {if (it != null) line = it } != null) {
                    lines++
                    words += line.split(Regex("\\s+")).filter { it.isNotEmpty() }.size

                }
            }
            BufferedWriter(FileWriter(outputFilePath)).use { writer ->
                writer.write("Общее количество строк: $lines\n")
                writer.write("Общее количество слов: $words\n")
            }
            true
        } catch (e: FileNotFoundException) {
            println("Ошибка: Файл не найден - ${e.message}")
            false
        } catch (e: SecurityException) {
            println("Ошибка: Нет прав доступа к файлу - ${e.message}")
            false
        } catch (e: IOException) {
            println("Ошибка ввода-вывода - ${e.message}")
            false
        } catch (e: Exception) {
            println("Непредвиденная ошибка - ${e.message}")
            false
        }
    }

}

class NIOFileAnalyzer : FileAnalyzer {
    override fun countLinesAndWordsInFile(inputFilePath: String, outputFilePath: String): Boolean {
        return try {
            var lines = 0
            var words = 0

            Files.newBufferedReader(Paths.get(inputFilePath)).use { reader ->
                reader.lines().use { stream ->
                    stream.forEach { line ->
                        lines++
                        words += line.split(Regex("\\s+")).filter { it.isNotEmpty() }.size
                    }
                }
            }

            Files.newBufferedWriter(Paths.get(outputFilePath)).use { writer ->
                writer.write("Общее количество строк: $lines\n")
                writer.write("Общее количество слов: $words\n")
            }
            true
        } catch (e: NoSuchFileException) {
            println("Ошибка: Файл не найден - ${e.message}")
            false
        } catch (e: AccessDeniedException) {
            println("Ошибка: Отказано в доступе к файлу - ${e.message}")
            false
        } catch (e: FileSystemException) {
            println("Ошибка файловой системы: ${e.message}")
            false
        } catch (e: SecurityException) {
            println("Ошибка: Отказано в доступе к файлу - ${e.message}")
            false
        } catch (e: IOException) {
            println("Ошибка ввода-вывода: ${e.message}")
            false
        } catch (e: Exception) {
            println("Непредвиденная ошибка: ${e.message}")
            false

        }
    }
}

fun main() {
    val inputPath = "./lesson5/test.txt"
    val outputPath = "./lesson5/example.txt"
//    val IOAnalyzer = IOFileAnalyzer()
    val NIOAnalyzer = NIOFileAnalyzer()
//    IOAnalyzer.countLinesAndWordsInFile(inputPath, outputPath)
    NIOAnalyzer.countLinesAndWordsInFile(inputPath, outputPath)
}