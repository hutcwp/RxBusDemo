package com.hutcwp.rxbusdemo

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.hutcwp.annotation.BusEvent
import com.hutcwp.api.event.EventCompat
import com.hutcwp.api.event.RxBus
import com.hutcwp.rxbusdemo.rxbus.Presenter
import com.hutcwp.rxbusdemo.rxbus.SelfEvent

class MainActivity : AppCompatActivity() {
    //    override fun onEventBind() {
//
//    }
//
//    override fun onEventUnBind() {
//
//    }
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

//    @BusEvent
//    fun onStringEvent(event: String) {
//        Toast.makeText(this, "onStringEvent", Toast.LENGTH_LONG).show()
//        Log.i("MainActivity", "onStringEvent")
//    }

    fun clickView(v: View) {
        Log.i("MainActivity", "clickView")
        RxBus.getDefault().post(SelfEvent())
    }
}