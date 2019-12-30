package com.zb.gradle.plugin

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.google.common.collect.Sets
import jdk.internal.org.objectweb.asm.Opcodes
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Type


/**
 * 编译后产生的class文件，会经过一个个transform的处理后，再进行dex的生成
 */
class ComponentTransform extends Transform{
    Project mProj;
    ComponentExtension mExtension;
    boolean  isApp;
    ComponentTransform(Project proj,ComponentExtension extension,boolean  isApp){
        mProj = proj;
        mExtension = extension;
        this.isApp = isApp;
        println(">>>>groups:"+extension.groups);
        println(">>>>process:"+extension.process);
    }
    @Override
    String getName() {//自定义的transform的名字
        return "route-component-transform"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        //要处理的输入类型
        //CLASSES 表示要处理编译后的字节码，可能是 jar 包也可能是目录
        //RESOURCES 表示处理标准的 java 资源
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        //transform输入文件的所属范围
        //PROJECT 只处理当前项目
        //SUB_PROJECTS 只处理子项目
        //PROJECT_LOCAL_DEPS 只处理当前项目的本地依赖,例如jar, aar
        //EXTERNAL_LIBRARIES 只处理外部的依赖库
        //PROVIDED_ONLY 只处理本地或远程以provided形式引入的依赖库
        if(isApp){
            return TransformManager.SCOPE_FULL_PROJECT //会传入工程内所有的 class 文件。
        }else{
            return Sets.immutableEnumSet(
                    QualifiedContent.Scope.PROJECT);
        }

    }

    @Override
    boolean isIncremental() {//是否增量编译
        return true
    }

    static File getJarDestFile(JarInput jarInput, TransformOutputProvider outputProvider) {
        def destName = jarInput.name
        // 重名名输出文件,因为可能同名,会覆盖
        def hexName = DigestUtils.md5Hex(jarInput.file.absolutePath)
        if (destName.endsWith(".jar")) {
            destName = destName.substring(0, destName.length() - 4)
        }
        // 获得输出文件
        File dest = outputProvider.getContentLocation(destName + "_" + hexName, jarInput.contentTypes, jarInput.scopes, Format.JAR)
        return dest
    }

    /**
     * 增量式构建的作用是为了避免冗余
     * 一个Task对输入进行操作，然后产生输出，如果多次执行一个Task时的输入和输出是一样的，没有必要重复执行task
     * 每个Task都拥有inputs和outputs属性，他们的类型分别为TaskInputs和TaskOutputs
     * configure过程后，task是组成了一个有向无环图，执行某个task就会按照依赖关系来执行，而下一个 Task 的 inputs 则是上一个 Task 的outputs。
     *
     * 每一个transform其实都对应的生成一个task，transform也形成了链式处理。也就是前一个transform的输出是下一个的输入
     * gradle会在自定义的transform生成task之后，再添加 Proguard, JarMergeTransform, MultiDex, Dex 等 Transform。
     *
     *
     * outputProvider.getContentLocation(dirInput.name, dirInput.contentTypes, dirInput.scopes, Format.DIRECTORY)
     * 用来获取输出路径,也就是说，input里面的每个成员文件（类文件，或者jar）转换后的输出路径是使用outputProvider获取的
     * 其实每个transform都会对应的有一个专门的文件夹来装输出文件所以可以看到/build/intermediates/transforms/下有多个文件夹，
     * 这些文件夹都是以transform名字命名的，每个transform一个
     *
     * */
    @Override
    void transform(Context context,
                   Collection<TransformInput> inputs,//输入的内容，可能是目录或者jar包
                   Collection<TransformInput> referencedInputs,
                   TransformOutputProvider outputProvider,//输出
                   boolean isIncremental) throws IOException, TransformException, InterruptedException {
        //String androidJarPath = mProj.android.bootClasspath[0].toString();//添加android.jar路径到ClassPool
        println(">>>>component transform begin!");
        def outputDirFile="";
        inputs.each { input ->
            input.directoryInputs.each {dir->//遍历输入的目录
                // 获得产物的目录
                File dest = outputProvider.getContentLocation(dir.name, dir.contentTypes, dir.scopes, Format.DIRECTORY)
                outputDirFile = dest;
                println(">>directory input dest:"+dest.getAbsolutePath()+" dir:"+dir.name+" dir path:"+dir.file.getAbsolutePath());
//                dir.file.eachFileRecurse {file->//迭代遍历目录下的文件
//                    if(file.isFile()){//在这里得到的就是一个个的.class文件，可以进行更改
//                        println(">>directory input file:"+file.getAbsolutePath());
//                    }
//                }
                //复制输入路径下的.class文件们到dest下
                FileUtils.copyDirectory(dir.file, dest)
            }
            input.jarInputs.each {jar->//遍历输入的jar包，
                File src = jar.file;
                File dest = getJarDestFile(jar,outputProvider);
               // println(">>jar input dest:"+dest.getAbsolutePath()+" jar:"+jar.name+" jarpath:"+jar.file.getAbsolutePath());

                //复制jar文件到transform目录
                FileUtils.copyFile(src, dest)
            }
        }
        //组件是支持多进程的，那么要生成一个用于进程间通讯的ContentProvider
        //contentProvider主要用来获取路由表，获取了路由表，就知道一个组件内支持的所有服务信息
        if(mExtension.multiAppProcess && mExtension.groups!=null && mExtension.groups.length>0){
            if(isApp) {
                gennerateProviderPrefixClass(mProj, outputDirFile,isApp,mExtension.name);
            }
            generateContentProviders(outputDirFile,mExtension.groups)
            String[] mainProcess = new String[1];
            mainProcess[0] = ComponentConstants.DefaultMainProcessName
            if(isApp){
                generateProcessServices(outputDirFile, mainProcess);
            }
        }
        //组件运行在单独的进程下，生成一个service，用来进行组件间的通讯
        if(mExtension.process!=null && mExtension.process.length>0){
            generateProcessServices(outputDirFile,mExtension.process)
        }

        println(">>>>component transform end!");
    }

    static byte[] gennerateClass(String clazz,String superClazz){
        ClassWriter cw = new ClassWriter(0)
        //Opcodes.V1_8代表java版本
        cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, clazz, null, superClazz, null)
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null)//创建一个无参构造函数
        mv.visitCode()//方法代码访问开始
        mv.visitVarInsn(Opcodes.ALOAD, 0)//装载当前第 0 个元素到堆栈中。代码上相当于“this”
        //ALOAD，ILOAD，LLOAD，FLOAD，DLOAD。区分它们的作用就是针对不用数据类型而准备的LOAD指令，此外还有专门负责处理数组的指令 SALOAD。
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, superClazz, "<init>", "()V", false)//调用父类的无参构造函数
        //invokespecial：这个指令是调用系列指令中的一个。其目的是调用对象类的方法。后面需要给上父类的方法完整签名。
        mv.visitInsn(Opcodes.RETURN)
        //RETURN：这也是一系列指令中的一个，其目的是方法调用完毕返回：可用的其他指令有：IRETURN，DRETURN，ARETURN等，用于表示不同类型参数的返回。
        mv.visitMaxs(1, 1)
        mv.visitEnd()

        cw.visitEnd()
        return cw.toByteArray()
    }
    static void gennerateProviderPrefixClass(Project project,File dir,boolean isApp,String runComponent){
        String providerPrefix = ComponentConstants.componentAuthPrefix(project);
        File classFile = new File(dir,ComponentConstants.ProviderPrefixClass+".class");
        if (classFile.exists()) {
            classFile.delete();
        }
        if (!classFile.getParentFile().exists()) {
            classFile.getParentFile().mkdirs()
        }
        classFile.createNewFile();
        ClassWriter cw = new ClassWriter(0)
        cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, ComponentConstants.ProviderPrefixClass, null,
                "java/lang/Object", null)
        FieldVisitor fv = cw.visitField(Opcodes.ACC_PUBLIC+Opcodes.ACC_FINAL+Opcodes.ACC_STATIC,
                "PROVIDER_HEADER",
                Type.getDescriptor(String.class),
                null,providerPrefix)
        if(isApp){
            FieldVisitor fv1 = cw.visitField(Opcodes.ACC_PUBLIC+Opcodes.ACC_FINAL+Opcodes.ACC_STATIC,
                    "runComponent",
                    Type.getDescriptor(String.class),
                    null,runComponent)
            fv1.visitEnd()
        }

        println(">>>>>>>>>provider prefix:"+providerPrefix);
        fv.visitEnd();
        cw.visitEnd();

        byte [] clazzBytes = cw.toByteArray();
        FileOutputStream fos = new FileOutputStream(classFile)
        fos.write(clazzBytes)
        fos.close()
    }
    static void gennerateSubClass(File dir,String className,String superClass){
        String fullClassName = className;
        String fullClassPath = fullClassName+".class"
        File classFile = new File(dir,fullClassPath);
        if (classFile.exists()) {
            classFile.delete();
        }
        if (!classFile.getParentFile().exists()) {
            classFile.getParentFile().mkdirs()
        }
        classFile.createNewFile();
        byte[] clazzBytes = gennerateClass(fullClassName,superClass);
        FileOutputStream fos = new FileOutputStream(classFile)
        fos.write(clazzBytes)
        fos.close()
    }
    //每个group一个contentProvider
    static void  generateContentProviders(File dir,String[] groups){
        for (String group:groups){
            gennerateSubClass(dir,ComponentConstants.buildProviderClassName(group),ComponentConstants.SuperProvider)
        }

    }
    static void generateProcessServices(File dir,String[] processes){
        for (String process:processes){
            gennerateSubClass(dir,ComponentConstants.buildServiceClassName(process),ComponentConstants.SuperService)
        }
    }

}