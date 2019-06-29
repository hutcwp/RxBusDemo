package com.hutcwp.plugin.inject
/**
 * @author huangfan(kael)
 * @time 2017/8/27 22:35
 */

interface IClassInjector {

    /**
     * 具体处理各个功能注入的方法
     * @param sniperInfo
     */
    boolean inject(SniperInfo sniperInfo)
}
