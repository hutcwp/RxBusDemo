package com.hutcwp.api.event;

/**
 * Created by hutcwp on 2019-06-26 20:37
 * email: caiwenpeng@yy.com
 * YY: 909076244
 **/

import android.util.Log;
import android.view.View;
import com.jakewharton.rxrelay2.PublishRelay;
import com.jakewharton.rxrelay2.Relay;
import com.trello.rxlifecycle2.RxLifecycle;
import com.trello.rxlifecycle2.android.ActivityEvent;
import com.trello.rxlifecycle2.android.FragmentEvent;
import com.trello.rxlifecycle2.android.RxLifecycleAndroid;
import com.trello.rxlifecycle2.components.RxActivity;
import com.trello.rxlifecycle2.components.support.RxFragmentActivity;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;
import io.reactivex.internal.functions.Functions;
import io.reactivex.schedulers.Schedulers;

import java.security.InvalidParameterException;
import java.util.concurrent.TimeUnit;

/**
 * 基于Rx的事件总线
 * 内置了一个默认实例便于全局使用，也可通过create创建新实例在自定义范围内使用
 * <p/>
 */
public class RxBus {
    private static final String TAG = "RxBus";
    private final static RxBus mDefault = new RxBus(0, "Default");
    private final Relay<Object> mRelay;

    private static final int DEFAULT_TIMEOUT_SECONDS = 5;

    private final int mMaxBufferSize;
    private final String mName;

    public String getName() {
        return mName;
    }

    @Override
    public String toString() {
        return "RxBus{" +
                "MaxBufferSize=" + mMaxBufferSize +
                ", Name='" + mName + '\'' +
                '}';
    }

    private RxBus(final int maxBufferSize, @NonNull final String name) {
        mMaxBufferSize = maxBufferSize;
        mName = name;
        mRelay = PublishRelay.create().toSerialized();
    }

    /**
     * 获得默认总线实例
     *
     * @return
     */
    public static RxBus getDefault() {
        return mDefault;
    }

    /**
     * 创建总线实例
     *
     * @param maxBufferSize
     * @param name
     * @return
     */
    public static RxBus create(final int maxBufferSize,
                               @NonNull final String name) {
        if (EventApi.getPluginBus(name) != null) {
            return (RxBus) EventApi.getPluginBus(name);
        }
        RxBus instance = new RxBus(maxBufferSize, name);
        EventApi.registerPluginBus(name, instance);
        return instance;
    }

    /**
     * sniper调用，给class创建的私有事件总线，其他不需要调用
     *
     * @param maxBufferSize
     * @param name
     * @return
     */
    @Deprecated
    public static RxBus createBySniper(final int maxBufferSize,
                                       @NonNull final String name) {
        return new RxBus(maxBufferSize, name);
    }

    /**
     * 向总线填入一个事件对象
     *
     * @param event
     */
    public void post(Object event) {
        mRelay.accept(event);
    }

    /**
     * 向总线填入一个事件对象，延迟发送
     *
     * @param event
     * @param milliSecs 延迟毫秒时间
     */
    public void postDelay(final Object event, long milliSecs) {
        Observable.timer(milliSecs, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(@io.reactivex.annotations.NonNull Long aLong) throws Exception {
                        mRelay.accept(event);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@io.reactivex.annotations.NonNull Throwable throwable) throws Exception {
                        Log.e(TAG, "Post Delay failed.", throwable);
                    }
                });
    }

    /**
     * 过滤事件类型，获得一个可订阅的事件源
     * 注意：订阅该事件源后必须自行调用 unsubscribe 进行释放，避免内存泄露
     *
     * @param cls 要过滤的事件对象类型
     * @return 事件源 Observable
     */
    public <T> Observable<T> register(final Class<T> cls) {
        if (mMaxBufferSize > 0) {
            return mRelay.toFlowable(BackpressureStrategy.BUFFER)
                    .filter(new Predicate<Object>() {
                        @Override
                        public boolean test(@io.reactivex.annotations.NonNull Object o) throws Exception {
                            return cls.isInstance(o);
                        }
                    })
                    .onBackpressureBuffer(mMaxBufferSize)
                    .cast(cls).toObservable();
        }
        return mRelay
                .filter(new Predicate<Object>() {
                    @Override
                    public boolean test(@io.reactivex.annotations.NonNull Object o) throws Exception {
                        return cls.isInstance(o);
                    }
                }).cast(cls);
    }

    /**
     * 订阅cls事件并执行一次,不关注onError
     *
     * @param cls
     * @param onSuccess
     * @param <T>
     */
    public <T> Disposable singleCall(final Class<T> cls, Consumer<? super T> onSuccess) {
        return singleCall(cls, onSuccess, Functions.ON_ERROR_MISSING);
    }

    /**
     * 订阅cls事件并执行一次,关注onError
     *
     * @param cls
     * @param onSuccess
     * @param <T>
     */
    public <T> Disposable singleCall(
            final Class<T> cls, Consumer<? super T> onSuccess, Consumer<? super Throwable> onError) {
        return register(cls)
                .firstOrError()
                .subscribe(onSuccess, onError);
    }

    /**
     * 订阅cls事件可能执行0或者1次，不关注onError,超过默认时间5s会自动取消订阅
     *
     * @param cls
     * @param onSuccess
     * @param <T>
     */
    public <T> void maybeCall(final Class<T> cls, Consumer<? super T> onSuccess) {
        maybeCall(cls, onSuccess, DEFAULT_TIMEOUT_SECONDS);
    }

    /**
     * 订阅cls事件可能执行0或者1次，不关注onError,超过second会自动取消订阅
     *
     * @param cls
     * @param onSuccess
     * @param second
     * @param <T>
     */
    public <T> void maybeCall(final Class<T> cls, Consumer<? super T> onSuccess, int second) {
        maybeCall(cls, onSuccess, Functions.ON_ERROR_MISSING, second);
    }

    /**
     * 订阅cls事件可能执行0或者1次，关注onError,超过默认时间5s会自动取消订阅
     *
     * @param cls
     * @param onSuccess
     * @param onError
     * @param <T>
     */
    public <T> void maybeCall(final Class<T> cls, Consumer<? super T> onSuccess, Consumer<? super Throwable> onError) {
        maybeCall(cls, onSuccess, onError, DEFAULT_TIMEOUT_SECONDS);
    }

    /**
     * 订阅cls事件可能执行0或者1次，关注onError,超过second会自动取消订阅
     *
     * @param cls
     * @param onSuccess
     * @param onError
     * @param second
     * @param <T>
     */
    public <T> void maybeCall(
            final Class<T> cls,
            Consumer<? super T> onSuccess,
            Consumer<? super Throwable> onError,
            int second) {
        register(cls)
                .timeout(second, TimeUnit.SECONDS)
                .firstOrError()
                .toMaybe()
                .subscribe(onSuccess, onError);
    }


    /**
     * 过滤事件类型，获得一个可订阅的事件源
     * 使用该函数可以不用调用者管理 Subscription 的退订，在 activity onDestroy 时将自动销毁。
     *
     * @param cls             要过滤的事件载体类型
     * @param lifecycleObject 绑定订阅的生命周期到一个支持生命周期的对象上,这个对象的类型可以是:
     *                        RxActivity/RxFragmentActivity/RxFragment/RxDialogFragment/View
     * @return 事件源Observable
     */
    public <T> Observable<T> register(@NonNull final Class<T> cls, final Object lifecycleObject) {
        if (lifecycleObject == null) {
            throw new InvalidParameterException("lifecycleObject can not be null");
        }

        Log.v(TAG, "Register for class: " + cls.getName() + ", lifecycleObject type: "
                + lifecycleObject.getClass().getName());

        if (lifecycleObject instanceof RxActivity) {
            RxActivity rxActivity = (RxActivity) lifecycleObject;
            return registerOnActivity(cls, rxActivity);
        }

        if (lifecycleObject instanceof RxFragmentActivity) {
            RxFragmentActivity rxActivity = (RxFragmentActivity) lifecycleObject;
            return registerOnActivity(cls, rxActivity);
        }

        if (lifecycleObject instanceof com.trello.rxlifecycle2.components.support.RxFragment) {
            com.trello.rxlifecycle2.components.support.RxFragment rxFragment
                    = (com.trello.rxlifecycle2.components.support.RxFragment) lifecycleObject;
            return registerOnFragment(cls, rxFragment);
        }

        if (lifecycleObject instanceof com.trello.rxlifecycle2.components.RxFragment) {
            com.trello.rxlifecycle2.components.RxFragment rxFragment
                    = (com.trello.rxlifecycle2.components.RxFragment) lifecycleObject;
            return registerOnFragment(cls, rxFragment);
        }

        if (lifecycleObject instanceof com.trello.rxlifecycle2.components.RxDialogFragment) {
            com.trello.rxlifecycle2.components.RxDialogFragment rxFragment
                    = (com.trello.rxlifecycle2.components.RxDialogFragment) lifecycleObject;
            return registerOnDialogFragment(cls, rxFragment);
        }

        if (lifecycleObject instanceof com.trello.rxlifecycle2.components.support.RxDialogFragment) {
            com.trello.rxlifecycle2.components.support.RxDialogFragment rxFragment
                    = (com.trello.rxlifecycle2.components.support.RxDialogFragment) lifecycleObject;
            return registerOnDialogFragment(cls, rxFragment);
        }

        if (lifecycleObject instanceof View) {
            View view = (View) lifecycleObject;
            return registerOnView(cls, view);
        }

        Log.w(TAG, "Type of lifecycleObject is: ["
                + lifecycleObject.getClass().getName()
                + "], which is not supported. You should un-subscribe from the returned Observable object yourself.");

        throw new IllegalArgumentException("lifecycleObject is not supported.");
    }

    /**
     * 过滤事件类型，获得一个可订阅的事件源
     * 使用该函数可以不用调用者管理 Subscription 的退订，在 activity onDestroy 时将自动销毁。
     *
     * @param cls      要过滤的事件载体类型
     * @param activity 绑定订阅的生命周期到一个 activity 上
     * @return 事件源Observable
     */
    public <T> Observable<T> registerOnActivity(final Class<T> cls, final RxActivity activity) {
        if (activity == null) {
            throw new InvalidParameterException("activity can not be null");
        }

        return register(cls).compose(
                RxLifecycle.<T, ActivityEvent>bindUntilEvent(
                        activity.lifecycle(),
                        ActivityEvent.DESTROY));
    }

    /**
     * 过滤事件类型，获得一个可订阅的事件源
     * 使用该函数可以不用调用者管理 Subscription 的退订，在 activity onDestroy 时将自动销毁。
     *
     * @param cls      要过滤的事件载体类型
     * @param activity 绑定订阅的生命周期到一个 activity 上
     * @return 事件源Observable
     */
    public <T> Observable<T> registerOnActivity(final Class<T> cls, final RxFragmentActivity activity) {
        if (activity == null) {
            throw new InvalidParameterException("activity can not be null");
        }

        return register(cls).compose(RxLifecycle.<T, ActivityEvent>bindUntilEvent(
                activity.lifecycle(),
                ActivityEvent.DESTROY));
    }

    /**
     * 过滤事件类型，获得一个可订阅的事件源
     * 使用该函数可以不用调用者管理 Subscription 的退订，在 fragment onDestroy 时将自动销毁。
     *
     * @param cls      要过滤的事件载体类型
     * @param fragment 绑定订阅的生命周期到一个 fragment 上
     * @return 事件源Observable
     */
    public <T> Observable<T> registerOnFragment(final Class<T> cls,
                                                final com.trello.rxlifecycle2.components.support.RxFragment fragment) {
        if (fragment == null) {
            throw new InvalidParameterException("fragment can not be null");
        }

        return register(cls).compose(
                RxLifecycle.<T, FragmentEvent>bindUntilEvent(fragment.lifecycle(),
                        FragmentEvent.DESTROY));
    }


    /**
     * 过滤事件类型，获得一个可订阅的事件源
     * 使用该函数可以不用调用者管理 Subscription 的退订，在 fragment onDestroy 时将自动销毁。
     *
     * @param cls      要过滤的事件载体类型
     * @param fragment 绑定订阅的生命周期到一个 fragment 上
     * @return 事件源Observable
     */
    public <T> Observable<T> registerOnFragment(final Class<T> cls,
                                                final com.trello.rxlifecycle2.components.RxFragment fragment) {
        if (fragment == null) {
            throw new InvalidParameterException("fragment can not be null");
        }

        return register(cls).compose(
                RxLifecycle.<T, FragmentEvent>bindUntilEvent(fragment.lifecycle(), FragmentEvent.DESTROY));
    }

    /**
     * 过滤事件类型，获得一个可订阅的事件源
     * 使用该函数可以不用调用者管理 Subscription 的退订，在 fragment onDestroy 时将自动销毁。
     *
     * @param cls         要过滤的事件载体类型
     * @param dlgFragment 绑定订阅的生命周期到一个 DialogFragment 上
     * @return 事件源Observable
     */
    public <T> Observable<T> registerOnDialogFragment(
            final Class<T> cls,
            final com.trello.rxlifecycle2.components.RxDialogFragment dlgFragment) {
        if (dlgFragment == null) {
            throw new InvalidParameterException("dlgFragment can not be null");
        }

        return register(cls).compose(RxLifecycle.<T,
                FragmentEvent>bindUntilEvent(dlgFragment.lifecycle(), FragmentEvent.DESTROY));
    }

    public <T> Observable<T> registerOnDialogFragment(
            final Class<T> cls,
            final com.trello.rxlifecycle2.components.support.RxDialogFragment dlgFragment) {
        if (dlgFragment == null) {
            throw new InvalidParameterException("dlgFragment can not be null");
        }

        return register(cls).compose(
                RxLifecycle.<T, FragmentEvent>bindUntilEvent(
                        dlgFragment.lifecycle(),
                        FragmentEvent.DESTROY));
    }

    /**
     * 过滤事件类型，获得一个可订阅的事件源
     * 不同于 register，该函数可以不用调用者管理 Subscription 的退订，在view detached时将自动销毁。
     * 注意：该函数必须在UI现场调用
     *
     * @param cls  要过滤的事件载体类型
     * @param view 绑定订阅的生命周期到一个 view 上
     * @return 事件源Observable
     */
    public <T> Observable<T> registerOnView(final Class<T> cls, final View view) {
        if (view == null) {
            throw new InvalidParameterException("view can not be null");
        }

        return register(cls).compose(RxLifecycleAndroid.<T>bindView(view));
    }

    /**
     * 配合sniper gradle插件编译时静态织入注册代码
     * 请使用{@link #register(Class, boolean, boolean)}代替
     *
     * @param cls
     * @param mainThread
     * @param <T>
     * @return
     * @deprecated
     */
    @Deprecated
    public <T> Observable<T> register(final Class<T> cls, final boolean mainThread) {
        return register(cls, mainThread, false);
    }

    /**
     * 配合sniper gradle插件编译时静态织入注册代码
     *
     * @param cls        事件类
     * @param mainThread 是否在主线程接收, sync为false的时候才生效
     * @param sync       是否同步调用
     * @param <T>
     * @return
     */
    public <T> Observable<T> register(final Class<T> cls, final boolean mainThread, boolean sync) {
        Observable<T> observable;
        if (mMaxBufferSize > 0) {
            observable = mRelay.toFlowable(BackpressureStrategy.BUFFER)
                    .filter(new Predicate<Object>() {
                        @Override
                        public boolean test(@io.reactivex.annotations.NonNull Object o) throws Exception {
                            return cls.isInstance(o);
                        }
                    })
                    .onBackpressureBuffer(mMaxBufferSize)
                    .cast(cls)
                    .toObservable();
        } else {
            observable = mRelay
                    .filter(new Predicate<Object>() {
                        @Override
                        public boolean test(@io.reactivex.annotations.NonNull Object o) throws Exception {
                            return cls.isInstance(o);
                        }
                    })
                    .cast(cls);
        }
        if (!sync) {
            if (mainThread) {
                observable = observable.observeOn(AndroidSchedulers.mainThread());
            } else {
                observable = observable.subscribeOn(Schedulers.computation());
            }
            observable = observable.unsubscribeOn(Schedulers.computation());
        }
        return observable;

    }
}

