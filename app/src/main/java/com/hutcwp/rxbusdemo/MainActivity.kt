package com.hutcwp.rxbusdemo

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.hutcwp.annotation.BusEvent
import com.hutcwp.api.event.RxBus
import com.hutcwp.rxbusdemo.rxbus.Presenter
import com.hutcwp.rxbusdemo.rxbus.SelfEvent
import com.hutcwp.luck.LuckMainActivity

class MainActivity : AppCompatActivity() {

    val presenter: Presenter = Presenter()

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        onEventBind()
    }

    override fun onDestroy() {
        super.onDestroy()
//        onEventUnBind()
    }

    @BusEvent
    fun onStringEvent(event: SelfEvent) {
        Toast.makeText(this, "onStringEvent", Toast.LENGTH_LONG).show()
        Log.i("MainActivity", "onStringEvent")
    }

    fun clickView(v: View) {
        Log.i("MainActivity", "clickView")
        RxBus.getDefault().post(SelfEvent())
        toLuckMain()
    }

    fun toLuckMain() {
        Log.i("MainActivity", "toLuckMain")
        startActivity(Intent(this, LuckMainActivity::class.java))
    }
}