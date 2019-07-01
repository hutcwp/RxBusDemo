package com.hutcwp.luck

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.hutcwp.annotation.BusEvent
import com.hutcwp.api.event.EventCompat
import com.hutcwp.api.event.RxBus

class LuckMainActivity : AppCompatActivity(), EventCompat {
    override fun onEventBind() {
//
    }

    override fun onEventUnBind() {
        //
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_luck_main)
        onEventBind()
    }

    @BusEvent
    fun onStringEvent(event: Event) {
        Toast.makeText(this, "onStringEvent", Toast.LENGTH_LONG).show()
        Log.i("LuckMainActivity", "onStringEvent")
    }

    fun clickView(v: View) {
        Log.i("LuckMainActivity", "clickView")
        RxBus.getDefault().post(Event())
    }

    class Event {

    }
}
