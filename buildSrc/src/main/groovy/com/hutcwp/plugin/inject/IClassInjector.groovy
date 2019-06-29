package com.hutcwp.plugin.inject


interface IClassInjector {

    /**
     * 具体处理各个功能注入的方法
     * @param sniperInfo
     */
    boolean inject(SniperInfo sniperInfo)
}
