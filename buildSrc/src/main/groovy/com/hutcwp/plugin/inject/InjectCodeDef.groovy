package com.hutcwp.plugin.inject

/**
 * 需要注入的固定code的定义类
 */

class InjectCodeDef {

    /* annotation */
    static final String BusEventAnnotation = "com.hutcwp.annotation.BusEvent"
    static final String DartsRegisterAnnotation = "com.yy.android.sniper.annotation.sneak.DartsRegister"
    static final String DartsHitAnnotation = "com.yy.android.sniper.annotation.sneak.DartsHit"
    static final String DartsInitializeAnnotation = "com.yy.android.sniper.annotation.sneak.DartsInitialize"


    static final String METHOD_TAIL = "%s\n" + "}\n"

    /*  EventInjector(activity/fragment/view/eventcompat) 方法  */
    public static final String METHOD_ONATTACHEDTOWINDOW = "protected void onAttachedToWindow() {\n" + METHOD_TAIL

    public static final String SUPER_ONATTACHEDTOWINDOW = "super.onAttachedToWindow();\n"

    public static final String METHOD_ONDETACHEDTOWINDOW = "protected void onDetachedFromWindow() {\n" + METHOD_TAIL

    public static final String SUPER_ONDETACHEDTOWINDOW = "super.onDetachedFromWindow();\n"

    public static final String METHOD_ONCREATE = "protected void onCreate(Bundle savedInstanceState) {\n" + METHOD_TAIL

    public static final String SUPER_ONCREATE = "super.onCreate(savedInstanceState);\n"

    public static final String METHOD_ONDESTROY = "protected void onDestroy() {\n" + METHOD_TAIL

    public static final String SUPER_ONDESTROY = "super.onDestroy();\n"

    public static final String METHOD_ONVIEWCREATED = "protected void onViewCreated(View view, Bundle savedInstanceState) {\n" + METHOD_TAIL

    public static final String SUPER_ONVIEWCREATED = "super.onViewCreated(view, savedInstanceState);\n"

    public static final String METHOD_ONDESTROYVIEW = "protected void onDestroyView(){\n" + METHOD_TAIL

    public static final String SUPER_ONDESTROYVIEW = "super.onDestroyView();\n"

    public static final String METHOD_ONEVENTBIND = "public void onEventBind(){\n" + METHOD_TAIL

    public static final String SUPER_ONEVENTBIND  = "super.onEventBind();\n"

    public static final String METHOD_ONEVENTUNBIND = "public void onEventUnBind(){\n" + METHOD_TAIL

    public static final String SUPER_ONEVENTUNBIND = "super.onEventUnBind();\n"

    /* StatisticsInjector lurk click方法*/
    public static final String METHOD_LURK_CLICK = "com.yy.android.sniper.api.lurk.LurkApi.get().callOnClick(%s);\n"

    public static final String METHOD_LURK_LIST_CLICK = "com.yy.android.sniper.api.lurk.LurkApi.get().callOnItemClick(%s, %s, %s);\n"

    public static final String METHOD_LURK_FRAGMENT_CREATE = "com.yy.android.sniper.api.lurk.LurkApi.get().callFragmentCreate(%s, %s, getClass().getSimpleName());\n"

    public static final String METHOD_LURK_FRAGMENT_DESTROY = "com.yy.android.sniper.api.lurk.LurkApi.get().callFragmentDestroy(getClass().getSimpleName());\n"
}
