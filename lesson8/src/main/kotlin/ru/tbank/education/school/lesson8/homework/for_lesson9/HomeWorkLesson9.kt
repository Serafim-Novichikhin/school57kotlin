package ru.tbank.education.school.lesson8.homework.for_lesson9

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream


fun createArchive(sourceDirPath: String, zipFilePath: String, allowedExtensions: Array<String>) {
    val sourceDir = File(sourceDirPath)

    if (!sourceDir.exists() || !sourceDir.isDirectory) {
        throw IllegalArgumentException("Ошибка: Исходный каталог не найден или не является каталогом: $sourceDirPath")
    }

    try {
        FileOutputStream(zipFilePath).use { fos ->
            ZipOutputStream(fos).use { zos ->
                addFileToZip(sourceDir, sourceDir, zos, allowedExtensions)
            }
        }
        println("\nАрхивация успешно завершена.")
    } catch (e: IOException) {
        throw IOException("Ошибка при создании архива: ${e.message}")
    }
}

private fun addFileToZip(baseDir: File, currentFile: File, zos: ZipOutputStream, allowedExtensions: Array<String>) {
    if (currentFile.isDirectory) {
        currentFile.listFiles()?.forEach { file ->
            addFileToZip(baseDir, file, zos, allowedExtensions)
        }
    } else {
        val fileExtension = currentFile.extension
        if (allowedExtensions.contains(fileExtension.lowercase())) {
            try {
                val relativePath = baseDir.toURI().relativize(currentFile.toURI()).path
                val entry = ZipEntry(relativePath)
                zos.putNextEntry(entry)
                FileInputStream(currentFile).use { fis ->
                    fis.copyTo(zos)
                }
                zos.closeEntry()
            } catch (e: IOException) {
                throw IOException("Не удалось добавить файл ${currentFile.path}: ${e.message}")
            }
        }
    }
}

fun main() {

}