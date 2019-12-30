package com.zb.gradle.plugin


class ComponentExtension{
    boolean isAlone = false; //是否是可单独运行组件
    String applicationId = "";//单独部署的时候的applicationId
    Closure dependencies;//依赖的其他组件
    Closure aarDependencies;
    String aloneBuildType = ""//单独运行时的buildType
    boolean multiDexEnabled=false;//是否开启multiDex
    String[] groups;//组件内包含的group（ARouter的那个group）
    String[] process;//组件内包含的进程
    boolean  multiAppProcess=false;//支持跨app多进程，开启后会为组件内的每个组都提供ContentProvider查询路由表
    String name;
}