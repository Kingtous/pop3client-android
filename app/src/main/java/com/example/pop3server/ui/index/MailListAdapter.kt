package com.example.pop3server.ui.index

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pop3server.R
import com.example.pop3server.data.MailHolder
import com.example.pop3server.data.model.Mail
import com.example.pop3server.ui.mail_detail.MailDetailActivity

class MailListAdapter(private val context: Context?, private var data: MutableList<Mail>) :
    RecyclerView.Adapter<MailListAdapter.MailListViewHolder>() {

    class MailListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var subject: TextView = itemView.findViewById(R.id.tv_subject)
        var sender: TextView = itemView.findViewById(R.id.tv_sender)
        var wholeView: LinearLayout = itemView.findViewById(R.id.ll_mail)
        var date: TextView = itemView.findViewById(R.id.tv_date)
        var id: TextView = itemView.findViewById(R.id.tv_mail_id)
        var containAttachment: ImageView = itemView.findViewById(R.id.iv_attach)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MailListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_mail, parent, false)
        return MailListViewHolder(view)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: MailListViewHolder, position: Int) {
        holder.id.text = data[position].id.toString()
        holder.subject.text = data[position].detail.subject
        holder.sender.text = data[position].detail.from
        holder.date.text = data[position].detail.date
        holder.containAttachment.visibility =
            if (data[position].detail.containAttachment) View.VISIBLE else View.GONE
        holder.wholeView.setOnClickListener {
            context?.let {
                val intent = Intent(it, MailDetailActivity::class.java)
                MailHolder.mail = data[position]
                it.startActivity(intent)
            }
        }
    }

}