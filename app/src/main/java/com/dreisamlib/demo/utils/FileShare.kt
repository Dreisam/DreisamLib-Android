package com.dreisamlib.demo.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import java.io.File
import kotlin.apply
import kotlin.let

class FileShare(private val context: Context) {

    /**
     * 分享文件
     * @param filePath 文件路径
     * @param mimeType 文件MIME类型，如"image/jpeg"、"application/pdf"等
     * @return 是否成功发起分享
     */
    fun shareFile(file: File, mimeType: String): Boolean {
        if (!file.exists() || !file.isFile) {
            return false
        }

        // 获取文件的Uri
        val fileUri = getFileUri(file) ?: return false

        // 创建分享Intent
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, fileUri)
            // 允许接收方读取Uri
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        // 启动分享活动
        context.startActivity(Intent.createChooser(shareIntent, "分享文件"))
        return true
    }

    /**
     * 获取文件的Uri，适配不同Android版本
     */
    private fun getFileUri(file: File): Uri? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // Android 7.0及以上使用FileProvider
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider", // 需要与AndroidManifest.xml中定义的一致
                    file
                )
            } else {
                // Android 7.0以下直接使用文件Uri
                Uri.fromFile(file)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 分享多个文件
     */
    fun shareMultipleFiles(filePaths: List<String>, mimeType: String): Boolean {
        val uris = mutableListOf<Uri>()

        for (path in filePaths) {
            val file = File(path)
            if (file.exists() && file.isFile) {
                getFileUri(file)?.let { uris.add(it) }
            }
        }

        if (uris.isEmpty()) {
            return false
        }

        val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = mimeType
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, kotlin.collections.ArrayList(uris))
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        context.startActivity(Intent.createChooser(shareIntent, "分享多个文件"))
        return true
    }
}
