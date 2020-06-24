package com.kingtous.remote_unlock.FileTransferTool

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.pop3server.R
import java.io.File
import java.math.RoundingMode
import java.text.NumberFormat


/**
 * Author: Kingtous
 * Since: 2020-02-10
 * Email: me@kingtous.cn
 */
class FileBrowserAdapter(internal var list: Array<File>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var nf: NumberFormat = NumberFormat.getNumberInstance()

    init {
        nf.minimumFractionDigits = 2
        nf.roundingMode = RoundingMode.HALF_UP
    }

    interface FileSelectListener {
        fun onFileClick(file: File)
    }

    private var listener: FileSelectListener? = null

    fun setFileSelectListener(listener: FileSelectListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_file, parent, false)
        return MViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val mHolder = holder as MViewHolder
        val fileSizeStr = nf.format(list[position].length().toDouble() / (1024 * 1024)) + " MB"
        val fileNameStr = list[position].name
        mHolder.fileName?.text = fileNameStr
        mHolder.fileSize?.text = fileSizeStr
        mHolder.wholeItemIterface?.setOnClickListener {
            listener?.onFileClick(list[position])
        }
    }

    class MViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var wholeItemIterface: CardView? = null
        var fileName: TextView? = null
        var fileSize: TextView? = null

        init {
            wholeItemIterface = itemView.findViewById(R.id.cv_file)
            fileName = itemView.findViewById(R.id.file_name)
            fileSize = itemView.findViewById(R.id.file_size)
        }
    }


}