package com.yywspace.simplefilemanager.data

import java.nio.file.Path

data class FileItem constructor(val path: Path, var selected: Boolean)