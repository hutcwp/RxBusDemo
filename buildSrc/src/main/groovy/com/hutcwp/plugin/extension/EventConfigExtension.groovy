package com.hutcwp.plugin.extension
/**
 *
 */
class EventConfigExtension {

    /** 不需要进行sniper插件进行编译出来的子project */
    def sniperIgnoredProjects = []

    /** 定义生成的插件事件总线的buffer大小，默认128**/
    def sniperRxBusPluginBuffer = '128'
}