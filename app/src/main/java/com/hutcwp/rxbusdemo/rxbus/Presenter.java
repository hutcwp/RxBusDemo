package com.hutcwp.rxbusdemo.rxbus;

import android.util.Log;
import com.hutcwp.annotation.BusEvent;
import com.hutcwp.api.event.EventCompat;

/**
 * Created by hutcwp on 2019-06-27 15:23
 * email: caiwenpeng@yy.com
 * YY: 909076244
 **/
public class Presenter implements EventCompat {

    public Presenter() {
        onEventBind();
    }

    @BusEvent()
    public void onEventString(SelfEvent event) {
        //placeholder
        Log.i("Presenter", "presenter,event");
    }

    @Override
    public void onEventBind() {

    }

    @Override
    public void onEventUnBind() {

    }


}
