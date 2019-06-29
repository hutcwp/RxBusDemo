package com.hutcwp.plugin.inject

/**
 * Created by huangfan on 2017/11/3.
 */

class SniperConstant {

    /** 定义在rootProject的配置信息，例如
     ext{
        ignoredProjects = ['a', 'b']
        rxBusClassConfig = "com.yy.mobile.RxBus"
        rxBusPluginPackage = 'com.duowan.mobile.entlive'
        rxBusPluginName = 'pluginIm'
        rxBusPluginBuffer = '64'
     }
     * */
    def static final USER_CONFIG = 'sniper'

    /** EventCompat类 限定名路径 **/
    def static final EVENT_COMPAT = 'com.hutcwp.api.event.EventCompat'

    /** 默认RxBus的包路径 **/
    def static final RxBus = 'com.yy.mobile.RxBus'

    /** Android Activity **/
    def static final ACTIVITY_RULES = [
            'android.app.Activity'                    ,
            'android.app.TabActivity'                 ,
            'android.app.ListActivity'                ,
            'android.app.ActivityGroup'               ,
            'android.support.v4.app.FragmentActivity' ,
            'android.support.v7.app.AppCompatActivity',
            'android.preference.PreferenceActivity'   ,
            'android.app.ExpandableListActivity'
    ]

    /** Android Fragment **/
    def static final FRAGMENT_RULES = [
            'android.support.v4.app.Fragment' ,
            'android.app.Fragment'
    ]

    /** Android View **/
    def static final VIEW_RULES = [
            'android.view.View' ,
            'android.view.ViewGroup'
    ]

    /** plugin publish info **/
    def static final MODULE_GROUP = 'com.yy.mobile.plugin'
    def static final MODULE_NAME = 'sniper'

    /** apt生成DartsFactory的类名前缀 **/
    def static final DARTS_FACTORY_PREFIX = '$$$DartsFactory$$$'
}
