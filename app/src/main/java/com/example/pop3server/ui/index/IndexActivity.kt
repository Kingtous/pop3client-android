package com.example.pop3server.ui.index

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pop3server.R
import com.example.pop3server.data.SocketHolder
import com.example.pop3server.data.model.Mail
import com.example.pop3server.data.model.Pop3Commands
import com.example.pop3server.data.parser.MailParser
import com.yanzhenjie.recyclerview.swipe.SwipeMenuItem
import com.yanzhenjie.recyclerview.swipe.SwipeMenuItemClickListener
import kotlinx.android.synthetic.main.activity_index.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Integer.parseInt
import kotlin.system.exitProcess


class IndexActivity : AppCompatActivity() {

    private var titlePrefix: CharSequence? = null
    private var mailList: MutableList<Mail> = ArrayList()
    private lateinit var maillistAdapter: MailListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_index)
        var title = "邮件列表"
        setTitle(title)
        intent.getStringExtra("username").let {
            title += "-$it"
            setTitle(title)
        }
        initMailListView()
        fetchData()
    }

    private fun initMailListView() {
        maillistAdapter = MailListAdapter(this, mailList)
        val linearLayoutManager = LinearLayoutManager(this)
        rv_list.layoutManager = linearLayoutManager

        rv_list.setSwipeMenuCreator { swipeLeftMenu, swipeRightMenu, viewType ->
            swipeRightMenu.addMenuItem(
                SwipeMenuItem(this)
                    .setBackground(R.color.gray)
                    .setImage(R.drawable.ic_delete_black_24dp)
                    .setHeight(ViewGroup.LayoutParams.MATCH_PARENT)
                    .setWidth(150)
            )
        }
        rv_list.setSwipeMenuItemClickListener(SwipeMenuItemClickListener { menuBridge ->
            AlertDialog.Builder(this)
                .setTitle("警告")
                .setIcon(R.drawable.ic_delete_black_24dp)
                .setMessage("确认删除吗")
                .setNegativeButton("取消") { dialogInterface: DialogInterface, i: Int ->
                    dialogInterface.dismiss()
                }
                .setPositiveButton("确定") { dialogInterface: DialogInterface, i: Int ->
                    CoroutineScope(Dispatchers.IO).launch {
                        SocketHolder.send("${Pop3Commands.DELE} ${mailList[menuBridge.adapterPosition].id}")
                        val resp = SocketHolder.receive()
                        if (resp.startsWith("+OK")) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    this@IndexActivity,
                                    "删除成功！正常退出会话即生效！",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                                dialogInterface.dismiss()
                                totalNum -= 1
                                title = "$titlePrefix($totalNum)"
                                mailList.removeAt(menuBridge.adapterPosition)
                                maillistAdapter.notifyItemRemoved(menuBridge.adapterPosition)

                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@IndexActivity, "删除失败请重试", Toast.LENGTH_SHORT)
                                    .show()
                                dialogInterface.dismiss()
                            }
                        }
                    }
                }
                .create()
                .show()
        })
        val defautlItemAnimator = DefaultItemAnimator()
        defautlItemAnimator.addDuration = 300
        defautlItemAnimator.removeDuration = 300
        rv_list.itemAnimator = defautlItemAnimator
        rv_list.adapter = maillistAdapter

    }

    private var totalNum = -99
    private var nextLoadIndex = -99
    // 每次请求10条数据
    private val perRequest = 10

    fun fetchData() {
//        Toast.makeText(this, "正在加载邮件", Toast.LENGTH_SHORT).show()
        if (nextLoadIndex == 0) {
            return
        }
        CoroutineScope(Dispatchers.IO).launch {
            if (totalNum == -99) {
                SocketHolder.send("${Pop3Commands.STAT}")
                val data = SocketHolder.receive()
                if (data.startsWith("+OK")) {
                    totalNum = parseInt(data.split(" ")[1])
                    withContext(Dispatchers.Main) {
                        titlePrefix = title
                        title = "$titlePrefix($totalNum)"
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@IndexActivity, "邮件数量获取失败", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }
            }
            if (nextLoadIndex == -99) {
                nextLoadIndex = totalNum
            }
            rv_list.clearOnScrollListeners()
            for (index in nextLoadIndex downTo nextLoadIndex - perRequest + 1) {
                if (index < 1) {
                    break
                }
                SocketHolder.send("${Pop3Commands.RETR} $index")
                val resp = SocketHolder.receiveEmail()
                withContext(Dispatchers.Main) {
                    mailList.add(Mail(index, MailParser.parseEmail(resp)))
                    maillistAdapter.notifyItemInserted(mailList.size - 1)
                }
            }
            // enable listener
            rv_list.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
                if (!rv_list.canScrollVertically(1)) {
                    Log.d("load", "load next")
                    fetchData()
                }
            }
            // 加载完就减掉
            nextLoadIndex -= perRequest
            if (nextLoadIndex < 0) {
                nextLoadIndex = 0
            }
        }
    }

    // 双击退出计时
    private var firstTime: Long = 0

    override fun onBackPressed() {
        val secondTime = System.currentTimeMillis()
        if (secondTime - firstTime > 2000) {
            Toast.makeText(this, "再按一次与服务器同步并断开连接", Toast.LENGTH_SHORT).show()
            firstTime = secondTime
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                SocketHolder.send("${Pop3Commands.QUIT}")
                val resp = SocketHolder.receive()
                if (resp.startsWith("+OK")) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@IndexActivity, "成功断开连接", Toast.LENGTH_SHORT).show()
                        SocketHolder.reader = null
                        SocketHolder.writer = null
                        SocketHolder.socket = null
                        finish()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@IndexActivity, "断开失败，请重试", Toast.LENGTH_SHORT).show()
                        firstTime = 0
                    }
                }
            }
        }
    }

}