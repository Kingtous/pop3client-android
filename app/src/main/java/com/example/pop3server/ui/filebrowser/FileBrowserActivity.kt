package com.kingtous.remote_unlock.FileTransferTool

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pop3server.BuildConfig
import com.example.pop3server.R
import com.example.pop3server.data.MailHolder
import com.example.pop3server.data.parser.MailParser.Companion.MIME_MapTable
import com.example.pop3server.ui.base.BaseActivity
import kotlinx.android.synthetic.main.activity_folder_browser.*
import java.io.File

/**
 * Author: Kingtous
 * Since: 2020-06-24
 * Email: me@kingtous.cn
 */
class FileBrowserActivity : BaseActivity() {

    lateinit var startPath: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "附件列表"
        if (intent.getStringExtra("path") == null) {
            startPath = filesDir.path + File.separator + MailHolder.loginUser
        } else {
            startPath = intent.getStringExtra("path")
        }
        initList()
    }

    override fun loadLayout(): Int {
        return R.layout.activity_folder_browser
    }


    fun initList() {
        val filePath = File(startPath)
        if (!filePath.exists()) {
            Toast.makeText(this, "还未下载过文件", Toast.LENGTH_LONG).show()
            finish()
        } else {
            val files = filePath.listFiles()
            files?.let {
                if (it.isEmpty()) {
                    Toast.makeText(this, "下载文件夹为空", Toast.LENGTH_LONG).show()
                    finish()
                }
                val adapter = FileBrowserAdapter(it)
                adapter.setFileSelectListener(object : FileBrowserAdapter.FileSelectListener {
                    override fun onFileClick(file: File) {
                        AlertDialog.Builder(this@FileBrowserActivity)
                            .setTitle("接下来")
                            .setPositiveButton("打开") { dialogInterface: DialogInterface, i: Int ->
                                processFile(this@FileBrowserActivity, file)
                                dialogInterface.dismiss()
                            }
                            .setNeutralButton("发送") { dialogInterface: DialogInterface, i: Int ->
                                processFile(this@FileBrowserActivity, file, Intent.ACTION_SEND)
                                dialogInterface.dismiss()
                            }
                            .setNegativeButton("删除") { dialogInterface: DialogInterface, i: Int ->
                                if (file.delete()) {
                                    Toast.makeText(
                                        this@FileBrowserActivity,
                                        "删除成功",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    initList()
                                }
                                dialogInterface.dismiss()
                            }
                            .show()
                    }
                })
                rv_fileList.layoutManager = LinearLayoutManager(this)
                rv_fileList.adapter = adapter

            }

        }
    }

    companion object {
        /// 用于打开和发送文件
        fun processFile(context: Context, file: File, action: String = Intent.ACTION_VIEW) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val builder = StrictMode.VmPolicy.Builder()
                builder.detectAll()
                StrictMode.setVmPolicy(builder.build())
            }
            val intent = Intent()
            intent.action = action
            // 获得拓展名
            var type = "*/*"
            val tmp =
                file.name.split("\\.".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
            if (tmp.size != 1) {
                for (pair in MIME_MapTable) {
                    if (pair[0] == tmp[tmp.size - 1]) {
                        if (pair[0] == "apk" && action == Intent.ACTION_VIEW) {
                            // 打开安装包需要另一个action
                            intent.action = Intent.ACTION_INSTALL_PACKAGE
                        }
                        type = pair[1]
                        break
                    }
                }
            }
            if (action == Intent.ACTION_SEND) {
                intent.setType(type)
                val uri = FileProvider.getUriForFile(
                    context,
                    BuildConfig.APPLICATION_ID + ".fileprovider",
                    file
                )
                intent.putExtra(Intent.EXTRA_STREAM, uri)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                try {
                    context.startActivity(Intent.createChooser(intent, "分享到..."))
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(context, "没有能打开此类型的应用", Toast.LENGTH_SHORT).show()
                }
            } else {
                val uri = FileProvider.getUriForFile(
                    context,
                    BuildConfig.APPLICATION_ID + ".fileprovider",
                    file
                )
                intent.setDataAndType(uri, type)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                try {
                    context.startActivity(Intent.createChooser(intent, "用...打开"))
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(context, "没有能打开此类型的应用", Toast.LENGTH_SHORT).show()
                }
            }


        }

    }


}