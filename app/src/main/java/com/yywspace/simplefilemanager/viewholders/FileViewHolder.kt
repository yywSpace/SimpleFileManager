package com.yywspace.simplefilemanager.viewholders

import android.graphics.Color
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.yywspace.simplefilemanager.R
import com.yywspace.simplefilemanager.data.FileItem
import kotlinx.android.synthetic.main.item_file_list.view.*
import java.nio.file.Files
import java.nio.file.Path
import java.text.SimpleDateFormat
import java.util.*

class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(fileItem: FileItem, isMultiSelect: Boolean) {
        if (!Files.exists(fileItem.path))
            return
        with(itemView) {
            if (isMultiSelect) {
                fileSelectCheckBox.visibility = View.VISIBLE
            } else {
                fileSelectCheckBox.visibility = View.INVISIBLE
            }
            fileSelectCheckBox.isChecked = fileItem.selected
            if (fileSelectCheckBox.isChecked)
                itemView.setBackgroundColor(Color.parseColor("#50999999"))
            else
                itemView.setBackgroundColor(resources.getColor(R.color.colorItemBackground, null))

            fileName.text = fileItem.path.fileName.toString()
            fileModifyDate.text = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(
                Date(
                    Files.getLastModifiedTime(fileItem.path).toMillis()
                )
            )
            if (Files.isDirectory(fileItem.path))
                fileSize.text = context.getString(
                    R.string.folder_size_label,
                    Files.newDirectoryStream(fileItem.path).toMutableList().size
                )
            else {
                val size = Files.size(fileItem.path)
                when (0.toLong()) {
                    size / 1024 ->
                        fileSize.text = String.format("%.2f Byte", size.toFloat())
                    size / 1024 / 1024 ->
                        fileSize.text = String.format("%.2f KB", size.toFloat() / 1024)
                    size / 1024 / 1024 / 1024 ->
                        fileSize.text = String.format("%.2f MB", size.toFloat() / 1024 / 1024)
                    size / 1024 / 1024 / 1024 / 1024 ->
                        fileSize.text =
                            String.format("%.2f GB", size.toFloat() / 1024 / 1024 / 1024)
                }
            }
            setFileTypeImage(fileItem.path, fileTypeImage)
        }
    }


    private fun setFileTypeImage(path: Path, fileTypeImage: ImageView) {
        when (path.toFile().extension.toLowerCase(Locale.getDefault())) {
            "pdf" -> fileTypeImage.setImageResource(R.drawable.ic_file_type_pdf)
            "iso" -> fileTypeImage.setImageResource(R.drawable.ic_file_type_iso)
            "cad" -> fileTypeImage.setImageResource(R.drawable.ic_file_type_cad)
            "psd" -> fileTypeImage.setImageResource(R.drawable.ic_file_type_psd)
            "docx", "docm" -> fileTypeImage.setImageResource(R.drawable.ic_file_type_word)
            "cfg", "conf", "txt" -> fileTypeImage.setImageResource(R.drawable.ic_file_type_text)
            "exe", "com" -> fileTypeImage.setImageResource(R.drawable.ic_file_type_exe)
            "htm", "html", "xhtml" -> fileTypeImage.setImageResource(R.drawable.ic_file_type_web)
            "3gp", "3GPP", "mp4", "ts", "webm" -> fileTypeImage.setImageResource(R.drawable.ic_file_type_video)
            "xlsx", "xlsm", "xltx", "xltm", "xlsb", "xlam" ->
                fileTypeImage.setImageResource(R.drawable.ic_file_type_excel)
            "ppt", "pptx", "pptm", "ppsx", "potx", "ppsm", "potm" ->
                fileTypeImage.setImageResource(R.drawable.ic_file_type_ppt)
            "aac", "flac", "imy", "m4a", "mid", "midi", "mka", "mp3", "ogg", "ota", "wav" ->
                fileTypeImage.setImageResource(R.drawable.ic_file_type_audio)
            "zip", "7z ", "bz2", "bzip2", "cbz", "gz", "gzip", "jar", "tar", "tar.bz2", "tar.gz", "tbz", "tbz2", "tgz",
            "rar", "bz", "ace", "uha", "uda", "zpaq" ->
                fileTypeImage.setImageResource(R.drawable.ic_file_type_archive)
            "webp ", "bmp", "pcx", "tif", "gif", "jpeg", "tga", "exif", "fpx", "svg", "cdr", "pcd",
            "dxf", "ufo", "eps", "ai", "png", "hdri", "raw", "wmf", "flic", "emf", "ico" ->
                fileTypeImage.setImageResource(R.drawable.ic_file_type_image)
            else ->
                fileTypeImage.setImageResource(R.drawable.ic_file_type_other)
        }

        if (Files.isDirectory(path)) {
            fileTypeImage.setImageResource(R.drawable.ic_file_type_folder)
        }
    }
}