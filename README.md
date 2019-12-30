## 组件框架

采用compplugin(组件化编译管理插件)+ARouter（组件通讯方案）的方式实现组件化

使用ARouter作为组件的通讯方案，主要是比较喜欢ARouter的用法，一个是通过一个Url来获取服务，或者跳转页面，然后服务的调用是使用接口进行的。

本框架是ARouter的上层应用，基于ARouter实现了一些组件化需要具备的一些特性。

### 1 代码边界

各个组件之间都互相没有依赖，依赖都通过接口的形式，因此这些接口和通讯数据结构都下沉到一个基本库里面定义，所有的组件都依赖这个基本库。

当一个组件要访问另一个组件的服务或者UI，都是使用ARouter的方式，利用Url进行访问，UI的话通过Url直接进行跳转，服务则是拿到接口后进行调用

### 2 资源划分

每个组件都需要配置一个resourcePrefix，这个前缀用来区分各个组件的资源。如果多个组件使用了同一个资源文件，那么这个资源文件下沉到基础库中

```
android{
   ... 
    resourcePrefix "compa_"
}
```

### 3 URL命名规范

url的命名是有规范的，通常来说，一个组件内只能有一个group，组件内的所有可访问的服务和activity都放到这个group内

```
@Route(path = "/compa/svr/iml")
public class ServiceCompAImpl implements IServiceCompA{
}

@Route(path="/compa/ui/main")
public class MainActivity extends AppCompatActivity {
}
```

### 4 组件单独运行与作为组件库

两者对资源的选择可能是不同的，比如单独运行的时候AndroidManifest.xml里面需要配置一个LAUNCHER，但是作为组件库的时候则不需要。

这一点通过给组件单独配置一个buildType来进行处理。比如设置aloneBuildType=“compa”，那么在组件a的源文件目录下，新建一个compa目录，里面放上组件单独的资源（res，AndroidManifest.xml）都可以进行替换

组件单独运行的时候要在Android Studio中选择这个组件的BuildVariant

![](./resources/1577692761(1).png)

## compplugin的使用

组件化编译管理插件，这个是组件化的实现核心，这里我只说明下使用方式，实现原理可查看源码

### 功能

1 在编码阶段，如果一个组件可以单独运行，那么编码阶段使用 `com.android.application`插件，如果不能单独运行，那么使用`com.android.library`插件

2 在assemble阶段，如果一个组件是作为主模块，也就是编译成apk的模块运行，那么使用 `com.android.application`插件，如果是作为依赖模块，那么就使用`com.android.library`插件

3 一个模块作为app的时候，与作为lib的时候，他可能有一些配置不一样，比如applicaitonId,和依赖库等等配置，这个时候要单独配置

4 当组件存在多进程的时候，使用插件来生成一些用于多进程通讯的服务，在涉及到这些组件多进程之间的通讯的时候，调用几乎与单进程一致，进程间的通讯由框架做了。

### 使用

```gradle
apply plugin: 'compplugin'
zbcomponent{
    isAlone true  //组件是否可以单独运行
    dependencies {
        implementation "com.zb.comp:comp-a:1.0" //依赖的其他组件
        implementation "com.zb.comp:comp-b:1.0"
        implementation "com.zb.comp:comp-c:1.0"
    }
    applicationId "com.zb.routecomponentdemo" //组件单独运行的时候使用的applicationId
    groups "app" //组件内的分组，如果有多个的话以逗号分隔
    name "comp-app" //组件名字
    aloneBuildType "" //添加一个buildType专门用于单独运行 
    multiDexEnabled true //是否支持multiDex
    multiAppProcess true //是否支持多进程
}
android {
	...
｝
```

zbcomponent{} 配置一定要写在android{} 配置之前，因为只有zbcomponent{} 配置完毕才能确定当前使用`com.android.application`插件还是`com.android.library`插件



##  ARouter改造

### 组件化改造

对于组件化的模块中，我们希望每个组件在程序运行开始即有一个运行的入口点，这一点ARouter的service能很好的契合这个需求，因为service有一个init方法，在service加载的时候就会调用

但是ARouter并不会在程序运行开始就把所有的service加载，而是在使用的时候加载，因此他是懒加载的。我们需要对这些组件service进行特殊处理。为了不改变ARouter本身的代码，我们使用拓展的形式实现。

ARouteComponent类的init方法就是实现这个功能的地方，所有的component组件服务，都实现IProvider的同时，实现IComponentService接口，这样就代表他是一个组件服务

```
@Route(path = "/compa/svr/component")
public class ComponentA implements IProvider,IComponentService{
    @Override
    public void init(Context context) {
        Log.e("RouteComponent","==ComponentA init==");

    }
}
```

IComponentService只是一个空接口，用来代表他是组件服务

注意，一定要单独实现IProvider而不是用一个接口继承IProvider之后再实现。因为在ARouter源码中，所有继承IProvider的服务可以不是唯一的，可以有多个。否则就只能有一个

### 通讯改造

所有的ARouter操作，都在KRouter这个类中有同样的方法，因为我们需要进行某些通讯改造（比如消息回调，比如实现一个专门用于申请权限的Activity，申请完毕通知请求者）

### 为Service的调用添加拦截器

ARouter的拦截器只作用域UI的路由期间，要想给service请求调用添加拦截器的话，需要拓展。在KRouter中，我会给所有的IProvider进行动态代理，这样就可以进行拦截了。

service的拦截器必须实现IProvider的同时，实现IServiceInterceptor接口

```java
public interface IServiceInterceptor {
    KInvokeResult beforeInvoke(KPostcard postcard,String methodName,Object[] args);
    KInvokeResult afterInvoke(KPostcard postcard,String methodName,Object result,Object[] args);
}
```

IServiceInterceptor可以在调用前跟调用后进行拦截，你需要通过postcard和methodName来进行判断，该调用是不是你想截获的，比如通过postcard.getPath()和methodName就可以确定是哪个服务的调用

拦截处理完毕，需要返回一个KInvokeResult来表示直接返回，还是继续调用

```java
public class KInvokeResult {
    public Object result;
    public boolean isContinue;
    public boolean isReturn;
    private KInvokeResult(Object result,boolean isContinue,boolean isReturn){
        this.result = result;
        this.isContinue = isContinue;
        this.isReturn = isReturn;
    }
    public static KInvokeResult onReturn(Object object){//立即返回，并且返回值为object
        return new KInvokeResult(object,false,true);
    }
    public static KInvokeResult onContinue(){//继续调用流程
        return new KInvokeResult(null,true,false);
    }
}
```

下面是一个服务拦截器的示例

```java
public class TestSvrInterceptor implements IProvider,IServiceInterceptor{
    ...
}
```

### 组件间通讯

### 1 activity的setResult

对于Activity来说，避免使用onActivityResult的形式获取结果值，在多进程的形式下会出问题。

推荐使用框架里面内置的方案

```
KRouter.build("/compc/ui/main")
       .navigationForResult(MainActivity.this,new KBaseNavCallback(){
              @Override
     public void onResult(Postcard postcard, KNavResult result) {
      	super.onResult(postcard, result);
     	Log.e("RouteComponent","onResult "+result.getString("data"));
      }
});
```

发送结果使用

```
KRouter.result(getIntent(),new KNavResult().withString("data","hello route comp") );
```

### 2 eventBus

使用eventBus来进行通讯，当然，如果不喜欢用eventBus，支持自行实现其他方式 

### 3 异步调用

对于服务来说，如果是同个进程，那么使用随意的接口作为回调参数都行，如果是不同进程，那么回调接口必须继承IRemoteListener,并且通讯数据类型必须是可序列化的。

## 多进程

一个组件内有多进程，那么非主进程的名字要在配置信息中进行收集

```
apply plugin: 'compplugin'
zbcomponent{
    isAlone true
    dependencies {
    }
    applicationId "com.zb.compb"
    aloneBuildType "compb"
    name "comp-b" 
    groups "compb"
    process ":compb" //组件内包含的非主进程，有多个则逗号分隔
    multiDexEnabled true
    multiAppProcess true
}
```

### 1 服务的多进程

**这里的服务是指的ARouter中的服务的概念，不是android的**

如果服务运行于非主进程，那么要给这个服务用ComponentProcess注解加上多进程的名字

```
@ComponentProcess(process = ":compb")
@Route(path = "/compb/svr/iml")
public class ServiceCompBImpl implements IRemoteSvrCompB{
    
}
```

### 2 Activity的多进程

与provider类似，也需要用ComponentProcess注解标记进程信息，同时要在AndroidManifest.xml中加上这个多进程标识

```
<activity android:name=".MainActivity"
            android:process=":compb"
            />
            
            
@ComponentProcess(process = ":compb")
@Route(path="/compb/ui/main")
public class MainActivity extends AppCompatActivity {
    
}
```

### 3 服务的接口中使用回调

回调接口必须继承IRemoteListener

### 4 接口参数数据类型

如果非java基本类型，对象必须是Serializable类型或者parcelable类型，推荐是Parcelable类型。



### 5 返回值限制

返回值问题，如果函数是带有返回值的，推荐调用线程为非主线程，因为第一次调用会连接目标进程，而连接上之后需要主线程调度才能成功连接，所以如果在主线程中连接目标进程，函数调用期间是没法完成整个跨进程通讯的。如果不想在线程中调用，那么可以使用回调返回结果值



## 使用示例

参见demo