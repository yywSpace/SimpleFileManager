package com.yywspace.simplefilemanager.utils

import com.yywspace.simplefilemanager.data.FileItem
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class LocalFileUtil {
    companion object {
        fun delete(list: List<FileItem>?) {
            list?.forEach {
                deleteRecursive(Paths.get(it.path))
            }
        }

        fun copy(resList: List<FileItem>?, dest: String) {
            resList?.forEach {
                val newFile = Paths.get(
                    dest + File.separator + it.name
                )
                copyRecursive(Paths.get(it.path), newFile)
            }
        }

        fun cut(resList: List<FileItem>?, dest: String) {
            resList?.forEach {
                val newFile = Paths.get(
                    dest + File.separator + it.name
                )
                copyRecursive(Paths.get(it.path), newFile)
            }
            resList?.forEach {
                deleteRecursive(Paths.get(it.path))
            }
        }

        private fun deleteRecursive(path: Path) {
            if (Files.isDirectory(path)) {
                for (f in Files.newDirectoryStream(path).toList())
                    deleteRecursive(f)
                Files.delete(path)
            } else {
                Files.delete(path)
            }
        }

        private fun copyRecursive(src: Path, dest: Path) {
            if (Files.isDirectory(src)) {
                if (!Files.exists(dest)) {
                    Files.createDirectory(dest)
                }
                Files.newDirectoryStream(src).toList().forEach {
                    val srcFile = Paths.get(
                        src.toAbsolutePath().toString() + File.separator + it.fileName.toString()
                    )
                    val destFile = Paths.get(
                        dest.toAbsolutePath().toString() + File.separator + it.fileName.toString()
                    )
                    // 递归复制
                    copyRecursive(srcFile, destFile)
                }
            } else {
                Files.copy(
                    src,
                    dest
                )
            }
        }
    }
}