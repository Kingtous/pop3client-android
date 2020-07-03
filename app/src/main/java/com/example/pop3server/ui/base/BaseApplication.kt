package com.example.pop3server.ui.base

import android.app.Application
import com.tencent.bugly.crashreport.CrashReport

/**
 * Author: Kingtous
 * Since: 2020/6/29
 * Email: me@kingtous.cn
 */

class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        CrashReport.initCrashReport(applicationContext, "736d47989f", false)
    }

}