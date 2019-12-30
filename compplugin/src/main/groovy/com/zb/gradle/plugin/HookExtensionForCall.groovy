package com.zb.gradle.plugin;

import org.gradle.api.Project
import org.gradle.internal.metaobject.AbstractDynamicObject
import org.gradle.internal.metaobject.DynamicInvokeResult;
import org.gradle.internal.metaobject.DynamicObject;

class MyExtensionsDynamicObject extends AbstractDynamicObject{
    private AbstractDynamicObject mDymObject;
    private OnExtensionCallListener mExtensionCall;
    private String mExtensionName
    MyExtensionsDynamicObject(AbstractDynamicObject obj,OnExtensionCallListener listener,String extensionName){
        mDymObject = obj;
        mExtensionCall = listener;
        mExtensionName = extensionName;
    }
    @Override
    String getDisplayName() {
        return mDymObject.getDisplayName()
    }
    @Override
    boolean hasProperty(String name) {
        return mDymObject.hasProperty(name)
    }
    @Override
    DynamicInvokeResult tryGetProperty(String name) {
        return mDymObject.tryGetProperty(name)
    }
    @Override
    DynamicInvokeResult trySetProperty(String name, Object value) {
        return mDymObject.trySetProperty(name, value)
    }
    @Override
    Map<String, ?> getProperties() {
        return mDymObject.getProperties()
    }
    @Override
    boolean hasMethod(String name, Object... arguments) {
        return mDymObject.hasMethod(name, arguments)
    }
    @Override
    DynamicInvokeResult tryInvokeMethod(String name, Object... arguments) {
        DynamicInvokeResult result = mDymObject.tryInvokeMethod(name, arguments)
        if(name.equals(mExtensionName)){
            mExtensionCall.onExtensionCallAfter(name);
        }
        return result;
    }
}
public class ExtensionHookUtil {
    /**
     * 这里的作用是hook extensions的dynamicObject,简而言之，这个dynamicObject就是为project提供extension属性和方法的地方
     * 我们需要hook这个地方，然后给方法调用的前后增加监听
     *
     * 1. project.extensibleDynamicObject 这个是一个组合模式的组合对象，他里面有一个DynamicObject数组,
     * 我们在向project调用一个方法或者查找属性的时候，如果方法不在project本身，
     * 那么就会通过这个组合对象去查找各个DynamicObject是否支持
     * 2. extensions.extensionsDynamicObject 这个是extensions容器提供的dynamicObject对象，它为project提供extension属性和方法支持
     * hook的逻辑是
     * 1 拿到extensibleDynamicObject的DynamicObject数组
     * 2 拿到extensionsDynamicObject
     * 3 在数组中找到extensionsDynamicObject的位置
     * 4 利用代理模式生成extensionsDynamicObject代理对象
     * 5 将代理插入到数组中找到的位置
     * 6 在代理对象中进行想进行的操作
     */
    static void hookExtensionForCall(Project project,OnExtensionCallListener listener, String extensionName){
        def extensions = project.extensions;
        try {
            def extensionsDynamicObject= extensions.metaClass.getAttribute(extensions,"extensionsDynamicObject");
            def myDynamicObject = new MyExtensionsDynamicObject(extensionsDynamicObject,listener,extensionName);
            def extDymObjField = project.class.superclass.getDeclaredField("extensibleDynamicObject")
            extDymObjField.setAccessible(true);
            def extensibleDynamicObject =  extDymObjField.get(project);
            def updateObjField = extensibleDynamicObject.class.superclass.superclass.getDeclaredField("updateObjects");
            def objField = extensibleDynamicObject.class.superclass.superclass.getDeclaredField("objects");
            updateObjField.setAccessible(true)
            objField.setAccessible(true)
            def updateObjects = (DynamicObject[])updateObjField.get(extensibleDynamicObject);
            def objects = (DynamicObject[])objField.get(extensibleDynamicObject);
            int idx = -1;
            for (int i=0;i<updateObjects.length;i++){
                if(extensionsDynamicObject == updateObjects[i]){
                    idx = i;
                    break;
                }
            }
            if(idx!=-1){
                updateObjects[idx] = myDynamicObject;
            }
            idx = -1;
            for (int i=0;i<objects.length;i++){
                if(extensionsDynamicObject == objects[i]){
                    idx = i;
                    break;
                }
            }
            if(idx!=-1){
                objects[idx] = myDynamicObject;
            }
        }catch (Exception e){
            e.printStackTrace()
        }
    }
}
