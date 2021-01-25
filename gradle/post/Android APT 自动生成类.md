# Android APT
> 在实现MVVM模式中，使用到了 LiveData 和 ViewModel 组件的帮助，一般是一个页面对应一个 ViewModel 和一个或者多个数据来源 DataRepository，如果用注解指定对应数据来源，就可以自动生成对应的 ViewModel，想想就觉得很爽，而 APT（注解处理器是（Annotation Processor Tool）是javac的一个工具，用来在编译时扫描和编译和处理注解（Annotation）。你可以自己定义注解和注解处理器去搞一些事情。一个注解处理器它以Java代码或者（编译过的字节码）作为输入，生成文件（通常是java文件）） 正好可以帮助我们处理这个功能。

## 定义注解
- 定义一个注解接收对应 ViewModel 的数据源，该注解值在源码中存在，作用接口、类、枚举、注解，[注解详解可看](https://juejin.cn/post/6844903833299058702) 
```
/**
 * @author maoqitian
 * @Description: 自动生成 ViewModel 注解
 * 保存对应 dataRepository 需要生成 ViewModel
 * @date 2021/1/15 0015 10:28
 */

@Retention(RetentionPolicy.SOURCE) //注解仅存在于源码中，在class字节码文件中不包含
@Target({ElementType.TYPE}) //作用接口、类、枚举、注解
public @interface ViewModelAutoGenerate {
    Class[] value();
}

```
## 定义注解处理器
- 光有注解只是接收了赋值，但还是要对注解进行处理，帮助自动生成ViewModel，所以还需要注解处理器，AbstractProcessor
- 定义注解处理器首先要创建一个 java-library

```
public class ViewModelAnnotationProcessor extends AbstractProcessor {

    //会被注解处理工具调用
    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
    }

    //这个注解处理器是注册给哪个注解的。注意，它的返回值是一个字符串的集合，
    // 包含本处理器想要处理的注解类型的合法全称，即注解器所支持的注解类型集合，
    // 如果没有这样的类型，则返回一个空集合
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new LinkedHashSet<>();
        annotations.add(ViewModelAutoGenerate.class.getCanonicalName());
        return annotations;
    }

    //指定使用的Java版本
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    //个处理器的主函数main()，你在这里写你的扫描、评估和处理注解的代码，以及生成Java文件
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        .....
    }
```
- 可以看到抽象类一般需要实现以上四个方法，其中最主要的是 process，用来处理注解，完成其他逻辑，会在后面讲到
- 接着如果要以注解处理器工作，还需在 src/main/resources/ 下建立 /META-INF/services 目录， 新增一个以接口命名的文件 , 内容接口的实现类全路径，这显然很麻烦，幸好谷歌爸爸已经帮解决了，只要使用开源库AutoService，并像如下代码使用
@AutoService 注解就行了，AutoService 会帮助我们自动生成对应路径文件

```
@AutoService(Processor.class)
public class ViewModelAnnotationProcessor extends AbstractProcessor {
    ........
}

## 依赖库引入
implementation 'com.google.auto.service:auto-service:1.0-rc6'
annotationProcessor 'com.google.auto.service:auto-service:1.0-rc6'
```


## javapoet 生成Java类

- 依赖
```
implementation 'com.squareup:javapoet:1.11.1'
```
- 实现 process 方法，处理注解，最终通过 javapoet 框架生成对应 Java 类文件，代码都有注释，就不在展开，也可自行查看 [javapoet api](https://square.github.io/javapoet/1.x/javapoet/) 查看其它方法

```
//个处理器的主函数main()，你在这里写你的扫描、评估和处理注解的代码，以及生成Java文件
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if(roundEnvironment.processingOver())return true;

        for (Element element : roundEnvironment.getElementsAnnotatedWith(ViewModelAutoGenerate.class)) {
            if (element.getKind() != ElementKind.CLASS) {
                error(String.format("Only classes can be annotated with @%s", ViewModelAutoGenerate.class.getSimpleName()));
                continue;
            }
            //注解属性传入类名（Repository类名称）
            final String className = element.toString();
            //Activity 对象类名
            final String simpleClassName = element.getSimpleName().toString();
            //包名
            final String packageName = className.substring(0, className.lastIndexOf("."));

            //注解中设置的值
            String annotationValue = "";
            Element actionElement = processingEnv.getElementUtils().getTypeElement(ViewModelAutoGenerate.class.getName());
            TypeMirror actionType = actionElement.asType();
            for (AnnotationMirror am : element.getAnnotationMirrors()) {
                if (am.getAnnotationType().equals(actionType)) {
                    for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : am.getElementValues().entrySet()) {
                        //注解属性 value 获取传入值
                        if ("value".equals(entry.getKey().getSimpleName().toString())) {
                            annotationValue = entry.getValue().getValue().toString();
                            break;
                        }
                    }
                }
            }
            //生成 JavaFile
            if (className.length() > 0 && annotationValue.length() > 0) {
                generateViewModelJavaFile(packageName, className, simpleClassName, annotationValue);
            }
        }

        return true;
    }

    /**
     * 通过 javapoet 自动生成 ViewModel.java
     * @param packageName
     * @param className
     * @param simpleClassName
     * @param annotationValue
     */
    private void generateViewModelJavaFile(String packageName, String className, String simpleClassName, String annotationValue) {
        note("generateViewModelJavaFile className=" + className
                + ", packageName=" + packageName
                + "，simpleClassName=" + simpleClassName
                + "，annotationValue=" + annotationValue);
        final String[] annotationClassNames = annotationValue.split(",");
        final String[] annotationSimpleNames = new String[annotationClassNames.length];
        for (int i = 0; i < annotationClassNames.length; i++) {
            //Repository类名称
            annotationClassNames[i] = annotationClassNames[i].substring(0, annotationClassNames[i].lastIndexOf("."));
            //Activity 类名称
            annotationSimpleNames[i] = annotationClassNames[i].substring(annotationClassNames[i].lastIndexOf(".") + 1);
        }
        // ViewModel 类构造
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(simpleClassName + "ViewModel")
                .superclass(ClassName.get("androidx.lifecycle", "ViewModel"))
                .addModifiers(Modifier.PUBLIC);
        //多少个 Repository 则构造多少个成员变量 和对应的获取方法
        for (int i = 0; i < annotationClassNames.length; i++) {
            String annotationPackageName = annotationClassNames[i].substring(0, annotationClassNames[i].lastIndexOf("."));
            ClassName annotationClass = ClassName.get(annotationPackageName, annotationSimpleNames[i]);
            //生成成员变量
            classBuilder.addField(FieldSpec.builder(annotationClass, "m" + annotationSimpleNames[i], Modifier.PRIVATE)
                    .initializer("new $T()", annotationClass)
                    .build());
            //生成方法
            classBuilder.addMethod(MethodSpec.methodBuilder("get" + annotationSimpleNames[i])
                    .addModifiers(Modifier.PUBLIC)
                    .addStatement("return $N", "m" + annotationSimpleNames[i])
                    .returns(annotationClass)
                    .build());
        }
        //生成 Java 文件对象
        JavaFile javaFile = JavaFile.builder(packageName, classBuilder.build()).build();
        try {
            javaFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
```
- 最后主工程引入依赖，编译项目自动生成ViewModel

<img src="https://github.com/maoqitian/MaoMdPhoto/blob/master/Android%E7%AC%AC%E4%B8%89%E6%96%B9%E5%BA%93%E5%88%86%E6%9E%90/APT/build%E6%9E%84%E5%BB%BA%E8%87%AA%E5%8A%A8%E7%94%9F%E6%88%90ViewModeljava.png"  height="400" width="600">

## 注解处理器支持 kotlin 

- 使用 kapt 插件让我们注解工具支持在 kt 类注解生效，如下所示，修改对应项目下的 build.gradle 注解处理器依赖引入由annotationProcessor 改为 kapt


```
.....
//加入 kapt 插件 使 kotlin 支持注解处理
apply plugin: 'kotlin-kapt'


android {
    compileSdkVersion 30
    buildToolsVersion "30.0.0"

    defaultConfig {
        applicationId "com.mao.aptgeneratedemo"
        minSdkVersion 19
        targetSdkVersion 30
        versionCode 1
        versionName "1.0"

        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
    }

   ......
}

dependencies {
    implementation fileTree(dir: "libs", include: ["*.jar"])
    ......

    //注解处理
    kapt project(':ViewModelAnnotationProcessor')
    //注解依赖
    implementation project(':ViewModelAnnotationlib')

}
```
 
- 编译生成对应的ViewModel

<img src="https://github.com/maoqitian/MaoMdPhoto/raw/master/Android%E7%AC%AC%E4%B8%89%E6%96%B9%E5%BA%93%E5%88%86%E6%9E%90/APT/build%E6%9E%84%E5%BB%BA%E8%87%AA%E5%8A%A8%E7%94%9F%E6%88%90ViewModelkotlin.png"  height="400" width="600">