package com.yywspace.simplefilemanager.data

import java.nio.file.Files
import java.nio.file.Path


data class FileItem constructor(
    val path: String,
    val name: String,
    val isDirectory: Boolean,
    val exists: Boolean,
    var selected: Boolean = false,
    var lastModified: Long,
    val childCount: Int,
    val length: Long,
    val extension: String

) {
    companion object {
        fun fromLocalFile(path: Path): FileItem {
            return FileItem(
                path.toAbsolutePath().toString(),
                path.fileName.toString(),
                Files.isDirectory(path),
                Files.exists(path),
                false,
                Files.getLastModifiedTime(path).toMillis(),
                if (Files.isDirectory(path)) Files.newDirectoryStream(path).toList().size else 0,
                Files.size(path),
                path.fileName.toString().substringAfterLast('.', "")
            )
        }

        fun fromSmbFile(): FileItem {
            return FileItem("", "", true, true, false, 1, 1, 1,"")
        }
    }
}