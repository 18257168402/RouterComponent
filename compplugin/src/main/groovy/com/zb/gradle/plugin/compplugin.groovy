package com.zb.gradle.plugin

import org.gradle.StartParameter
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.internal.artifacts.dsl.dependencies.DefaultDependencyHandler
import org.gradle.api.internal.plugins.ExtensionsStorage
import org.gradle.api.internal.plugins.ExtensionsStorage.ExtensionHolder
import org.gradle.internal.metaobject.AbstractDynamicObject
import org.gradle.internal.metaobject.DynamicInvokeResult
import org.gradle.internal.metaobject.DynamicObject

import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Proxy

import static com.zb.gradle.plugin.ExtensionHookUtil.hookExtensionForCall


class StarterTaskInfo{
    boolean isDebug = false;
    boolean isAssemble = false;
    boolean isAARDeploy = false;
    boolean isGenerateSource = false;
    boolean  isUploadArchives = false;

    @Override
    public String toString() {
        return "StarterTaskInfo{" +
                "isDebug=" + isDebug +
                ", isAssemble=" + isAssemble +
                ", isAARDeploy=" + isAARDeploy +
                ", isGenerateSource=" + isGenerateSource +
                ", isUploadArchives=" + isUploadArchives +
                '}';
    }
}


class compplugin implements Plugin<Project> {
    StarterTaskInfo parseStarterTaskInfo(Project project){
        StarterTaskInfo taskInfo = new StarterTaskInfo();
        List<String> taskNames = project.gradle.startParameter.taskNames
        for (String task:taskNames){
            if(task.toLowerCase().contains("assemble") ||
                task.contains("aR") ||
                task.contains("aD")){
                taskInfo.isAssemble = true;
                if(task.toLowerCase().contains("debug") ||
                    task.contains("aD")){
                    taskInfo.isDebug = true;
                }
                break
            }
        }
        for (String task:taskNames){
            if(task.toLowerCase().contains("generate")){
                taskInfo.isGenerateSource = true;
                break
            }
        }
        for (String task:taskNames){
            if(task.contains("uploadArchives")){
                taskInfo.isUploadArchives = true;
                break
            }
        }
        for (String task:taskNames){
            if(task.toLowerCase().contains("aar")&&task.toLowerCase().contains("deploy")){
                taskInfo.isAARDeploy = true;
                break
            }
        }
        return taskInfo;
    }
    String curModule(Project project){
        return project.name;
    }
    String starterModule(Project project){

        if(!project.rootProject.hasProperty("main_component")){
            throw new RuntimeException("Must set main_component in rootproject's gradle.properties")
        }
        String mainComponent = project.rootProject.property("main_component");
        println("mainComponent is " + mainComponent)

        String starterModule = mainComponent;
        List<String> taskNames = project.gradle.startParameter.taskNames;
        for(String task:taskNames){
            if(task.toLowerCase().contains("assemble") ||
                    task.contains("aR") ||
                    task.contains("aD")){
                String[] strs = task.split(":")
                if(strs.length>=2){
                    starterModule = strs[strs.length-2];
                }
                println(">>task split:"+strs)
                break
            }
        }
        if(starterModule.equals("")){
            starterModule = curModule(project)
        }
        return starterModule;
    }
    @Override
    void apply(Project project) {
        StartParameter startParameter = project.gradle.startParameter
        List<String> taskNames = startParameter.taskNames
        String curModule = curModule(project);
        String starterModule = starterModule(project);

        StarterTaskInfo starterTaskInfo = parseStarterTaskInfo(project);

        println("project.path is " + project.path+" "+project.class)
        println("taskNames is " + taskNames.toString())
        println("starterTaskInfo is " + starterTaskInfo)
        println("curModule is " + curModule+" starterModule:"+starterModule)

        project.extensions.create("zbcomponent", ComponentExtension)

        //因为要先获取插件配置，因此要拦截project.zbcomponent方法的调用
        //这也是zbcomponent必须在android插件之前应用的原因
        hookExtensionForCall(project,new OnExtensionCallListener() {
            @Override
            void onExtensionCallAfter(String s) {
                println(">>>>>>>>>onExtensionCallAfter:"+s)

                boolean  isRunAlone = project.zbcomponent.isAlone;
                if(starterTaskInfo.isAssemble){
                    if(!starterModule.equals(curModule)){
                        isRunAlone = false;
                    }else{
                        isRunAlone = true;
                    }
                }
                if(starterTaskInfo.isAARDeploy || starterTaskInfo.isUploadArchives){
                    isRunAlone = false;
                }
                //单独运行的情形下，使用com.android.application插件，否则使用com.android.library插件
                if(isRunAlone){
                    project.apply plugin:'com.android.application'
                    println(">>>>curModule:"+curModule+" com.android.application")
                    project.android.defaultConfig.applicationId = project.zbcomponent.applicationId
                    if(project.zbcomponent.multiDexEnabled){
                        project.android.defaultConfig.multiDexEnabled = true;
                    }
                    Closure closure = project.zbcomponent.dependencies
                    if(closure!=null && starterTaskInfo.isAssemble){
                        closure.delegate = project.dependencies;
                        closure.call();//注入其他组件依赖
                    }
                    //因为可单独运行的时候和作为一个组件的时候，AndroidManifest.xml里面配置的activity是有不同的，可单独运行的组件
                    //必须有一个android.intent.category.LAUNCHER类型的activity，因此我们利用给单独运行的组件一个单独的buildType，这样就可以在源文件目录下
                    //新建一个与buildType值相同的目录，放置AndroidManifest.xml文件，gradle会自行选择正确的文件
                    String alongBuildType = project.zbcomponent.aloneBuildType;
                    if(!alongBuildType.equals("")){
                        project.android.buildTypes{
                            //buildTypes默认是一个NamedDomainObjectContainer
                            def buildType = create(alongBuildType);//往容器内加一个名叫alongBuildType的BuildType
                            def debugBuildType = getByName("debug");//获取debug BuildType实例
                            buildType.initWith(debugBuildType)
                            buildType.matchingFallbacks = [alongBuildType, 'debug', 'release']//如果不指定matchingFallbacks，那么依赖的组件也会需要同名buildType
                        }
                    }
                    def android = project.android;
                    //需要注册一个Transform,用于生成一些辅助类
                    def transform = new ComponentTransform(project,project.zbcomponent,true);
                    android.registerTransform(transform);
                    //需要修改合并后的Manifest文件，添加一些组件通讯必须的东西
                    def manifestModify = new ComponentManifestModify(project,project.zbcomponent,true)
                    android.applicationVariants.all { variant ->
                        manifestModify.modifyManifest(variant)
                    }
                }else{
                    project.apply plugin: 'com.android.library'
                    println(">>>>curModule:"+curModule+" com.android.library")
                    def android = project.android;

                    def transform = new ComponentTransform(project,project.zbcomponent,false);
                    android.registerTransform(transform);

                    def manifestModify = new ComponentManifestModify(project,project.zbcomponent,false)
                    android.libraryVariants.all{variant ->
                        manifestModify.modifyManifest(variant)
                    }
                }
            }
        },"zbcomponent")

    }

}