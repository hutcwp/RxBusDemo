package com.hutcwp.api.event;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author huangfan(kael)
 * @time 2018/9/21 14:55
 */
public abstract class EventProxy<T> implements EventBinder<T> {

    public final AtomicBoolean invoke = new AtomicBoolean(false);

    public T target;

    protected final ArrayList<Disposable> mSniperDisposableList = new ArrayList<>();

    public final Consumer mProjectConsumer = new Consumer() {
        @Override
        public void accept(Object event) throws Exception {
            projectEventConsume(event);
        }
    };

    public final Consumer mClassConsumer = new Consumer() {
        @Override
        public void accept(Object event) throws Exception {
            classEventConsume(event);
        }
    };

    public final Consumer mPluginConsumer = new Consumer() {
        @Override
        public void accept(Object event) throws Exception {
            pluginEventConsume(event);
        }
    };

    public void projectEventConsume(Object event) {

    }

    public void pluginEventConsume(Object event) {

    }

    public void classEventConsume(Object event) {

    }

    @Override
    public void unBindEvent() {
        if (invoke.compareAndSet(true, false)) {
            for (int i = 0; i < mSniperDisposableList.size(); i++) {
                Disposable disposable = mSniperDisposableList.get(i);
                if (!disposable.isDisposed()) {
                    disposable.dispose();
                }
            }
            mSniperDisposableList.clear();
            target = null;
        }
    }

}
