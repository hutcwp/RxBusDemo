package com.hutcwp.rxbusdemo

import android.annotation.SuppressLint
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.hutcwp.api.event.EventBinder
import com.hutcwp.api.event.EventCompat
import com.hutcwp.api.event.RxBus

class MainActivity : AppCompatActivity(), EventCompat {

    override fun onEventBind() {
        if (this.mMainActivitySniperEventBinder == null) {
            this.mMainActivitySniperEventBinder = `MainActivity$$EventBinder`()
        }
        this.mMainActivitySniperEventBinder?.bindEvent(this)
    }

    override fun onEventUnBind() {
        if (this.mMainActivitySniperEventBinder != null) {
            this.mMainActivitySniperEventBinder?.unBindEvent()
        }
    }

    private var mMainActivitySniperEventBinder: EventBinder<MainActivity>? = null

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        onEventBind()
        RxBus.getDefault().post("s1")
    }

    fun onStringEvent(event: String) {
        Log.i("MainActivity", "onStringEvent")
    }

    fun onIntEvent(event: Integer) {
        Log.i("MainActivity", "onIntEvent")
    }
}