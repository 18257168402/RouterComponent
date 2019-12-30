package com.zb.gradle.plugin

import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.LibraryVariant
import groovy.xml.MarkupBuilder
import org.gradle.api.Project

class ComponentManifestModify{

    Project mProj;
    ComponentExtension mExtension;
    boolean  isApp;
    ComponentManifestModify(Project project,ComponentExtension extension,boolean  isApp){
        mProj = project;
        mExtension = extension;
        this.isApp = isApp;
    }
    void modifyManifest(BaseVariant variant){
        String appId = variant.mergedFlavor.applicationId;//变体的applicationId
        String appIdSuffix = variant.buildType.applicationIdSuffix;//applicationId 每个构建体可以给一个后缀

        println(">>>>appId "+appId+" buildType:"+variant.buildType.name+"  appIdSuffix:"+appIdSuffix);
        variant.outputs.each { output ->
            output.processManifest.doLast {
                File manifestOutDir = output.processManifest.manifestOutputDirectory;
                File manifestOutFile = new File(manifestOutDir.absolutePath+File.separator+"AndroidManifest.xml");
                println(">>>>>manifestOutFile:"+manifestOutFile.absolutePath);
                modifyFile(manifestOutFile);
            }
        }
    }
    void modifyFile(File manifestOutFile){
        def manifestContent = manifestOutFile.getText("utf-8");
        println(">>>>>manifestContent:"+manifestContent);

        def manifestXmlParser = new XmlSlurper().parse(manifestOutFile);
        String manifestPkgName = manifestXmlParser.'@package';//通过@+属性名来访问节点属性值，使用text方法来访问内容值。

        if(!isApp){
            manifestPkgName = "_!_placeholder_!_";
        }
        def appNode = manifestXmlParser.application;

        def writer = new StringWriter()
        def xml = new MarkupBuilder(writer)//使用markupBuilder来组装xml

        if(mExtension.multiAppProcess && mExtension.groups!=null && mExtension.groups.length>0){
            for (String group:mExtension.groups){
                String providerClass = ComponentConstants.buildProviderClassName(group).replace("/",".");
                String providerAuthorities = ComponentConstants.buildProviderAuthorities(mProj,group);
                xml.placeholder{
                    provider(
                            "android:name":providerClass,
                            "android:authorities":providerAuthorities,
                            "android:exported": "true")
                }
            }
            if(isApp){
                xml.placeholder{
                    service(
                            "android:name":ComponentConstants.buildServiceClassName(ComponentConstants.DefaultMainProcessName).replace("/","."),
                            "android:enabled":"true",
                            "android:exported": "true"
                    ){
                        "intent-filter"{
                            action(
                                    "android:name":ComponentConstants.buildServiceAction(manifestPkgName,"")
                            )
                        }
                    }
                }
            }
        }
        if(mExtension.process!=null && mExtension.process.length>0){
            for(String process:mExtension.process){
                xml.placeholder{
                    service(
                            "android:name":ComponentConstants.buildServiceClassName(process).replace("/","."),
                            "android:process":process,
                            "android:enabled":"true",
                            "android:exported": "true"
                    ){
                        "intent-filter"{
                            action(
                                    "android:name":ComponentConstants.buildServiceAction(manifestPkgName,process)
                            )
                        }
                    }
                }
            }
        }
        if(isApp){
            manifestContent = manifestContent.replaceAll("_!_placeholder_!_",manifestPkgName)
        }


        String appendStr = writer.toString();
        String appendXml = appendStr.replace("<placeholder>","").replace("</placeholder>","");//把根标签去掉，这个是占位用的
        println(">>>appendXml: "+appendXml)
        int index = manifestContent.lastIndexOf("</application>")
        if(index>=0){
            String outContent =   manifestContent.substring(0, index) + appendXml + manifestContent.substring(index)//把插入的文本放到相应位置
            println(">>>>>manifestPkgName:"+manifestPkgName+" isApp:"+isApp+"  outContent:"+outContent);
            manifestOutFile.write(outContent,"utf-8")//重写输出的menifest.xml
        }else{
            index = manifestContent.lastIndexOf("</manifest>")
            appendXml =  appendStr.replace("<placeholder>","").replace("</placeholder>","");
            String outContent =   manifestContent.substring(0, index)+"<application>" + appendXml+"</application>" + manifestContent.substring(index)//把插入的文本放到相应位置
            manifestOutFile.write(outContent,"utf-8")//重写输出的menifest.xml
        }

    }
}