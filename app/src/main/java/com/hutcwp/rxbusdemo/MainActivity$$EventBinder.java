package com.hutcwp.rxbusdemo;

import com.hutcwp.api.event.EventProxy;
import com.hutcwp.api.event.RxBus;

/**
 * Created by hutcwp on 2019-06-27 14:13
 * email: caiwenpeng@yy.com
 * YY: 909076244
 **/
public class MainActivity$$EventBinder extends EventProxy<MainActivity> {


    @Override
    public void projectEventConsume(Object event) {
        if (invoke.get()) {
            if (event instanceof String) {
                target.onStringEvent((String) event);
            }
        }
    }

    @Override
    public void bindEvent(MainActivity target) {
        if (invoke.compareAndSet(false, true)) {
            this.target = target;
            mSniperDisposableList.add(RxBus.getDefault()
                    .register(String.class, true, false).subscribe(mProjectConsumer));
        }
    }
}
