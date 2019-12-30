package com.zb.gradle.plugin

import org.gradle.api.Project

class ComponentConstants{
    static String SuperProvider = "com/zb/component/KComponentProvider";
    static String SuperService = "com/zb/component/KRemoteComponentService";

    static String ProviderPrefixClass = "com/zb/component/PROVIDER_PREFIX";

    static String DefaultMainProcessName = "__main";
    static String buildProviderClassName(String group){
       return "com/zb/component/GroupProvider_"+group
    }
    static String componentAuthPrefix(Project project){
        if(!project.rootProject.hasProperty("component_auth_prefix")){
            throw new RuntimeException("Must set component_auth_prefix in rootproject's gradle.properties")
        }
        return project.rootProject.property("component_auth_prefix");
    }
    static String buildProviderAuthorities(Project project, String group){
        String component_auth_prefix =componentAuthPrefix(project);
        return component_auth_prefix+"."+group+".comp_provider";
    }
    static String buildServiceClassName(String process){
        String processName = process.replace(":","_");
        processName =processName.replace(".","_");
        return "com/zb/component/ProcessService_"+processName;
    }
    static String buildServiceAction(String pkgName,String process){
        return  pkgName+".comp_svr"+process;
    }
}