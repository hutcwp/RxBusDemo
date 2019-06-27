package com.hutcwp.compiler;

import com.google.auto.service.AutoService;
import com.hutcwp.annotation.BusEvent;
import com.hutcwp.annotation.auxiliary.BusType;
import com.hutcwp.annotation.auxiliary.SchedulerType;
import com.hutcwp.compiler.util.Utils;
import com.squareup.javapoet.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.util.*;


/**
 * Created by hutcwp on 2019-06-26 21:29
 * email: caiwenpeng@yy.com
 * YY: 909076244
 **/
@AutoService(Processor.class)//自动生成 javax.annotation.processing.IProcessor 文件
@SupportedSourceVersion(SourceVersion.RELEASE_7)//java版本支持
public class EventProcessor extends AbstractProcessor {

    private static final ClassName EVENT_BINDER = ClassName.get("com.hutcwp.api.event", "EventBinder");
    private static final ClassName EVENT_PROXY = ClassName.get("com.hutcwp.api.event", "EventProxy");
    private static final ClassName DISPOSABLE = ClassName.get("io.reactivex.disposables", "Disposable");
    private static final ClassName EVENT_API = ClassName.get("com.hutcwp.api.event", "EventApi");
    private static final ClassName ATOMIC_BOOLEAN = ClassName.get("java.util.concurrent.atomic", "AtomicBoolean");
    private static final ClassName CONSUMER = ClassName.get("io.reactivex.functions", "Consumer");
    private static final ClassName EXCEPTION = ClassName.get("java.lang", "Exception");

    private static final ClassName RX_BUS = ClassName.get("com.hutcwp.api.event", "RxBus");

    private static final String DISPOSABLE_LIST = "mSniperDisposableList";

    /**
     * key值ExecutableElement所在的类element，value为带有@BusEvent(SCOPE_PROJECT)的方法element的集合
     */
    private Map<TypeElement, List<ExecutableElement>> projectBusMap = new HashMap<>();

    public Processor mProcessor; //
    public Filer mFiler; //文件相关的辅助类
    public Elements mElements; //元素相关的辅助类
    public Messager mMessager; //日志相关的辅助类
    public Types mTypes;
    public Map<String, String> mOptions;

    /**
     * 保存所有需要处理的类element，用来遍历生成代理类以及集成类判断
     */
    private Set<TypeElement> erasedSet = new LinkedHashSet<>();

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        HashSet<String> supportTypes = new LinkedHashSet<>();
        supportTypes.add(BusEvent.class.getCanonicalName());
        return supportTypes;
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        mProcessor = this;
        mFiler = processingEnv.getFiler();
        mElements = processingEnv.getElementUtils();
        mMessager = processingEnv.getMessager();
        mTypes = processingEnv.getTypeUtils();
        mOptions = processingEnv.getOptions();
        mMessager.printMessage(Diagnostic.Kind.NOTE, "init...");
        generateEventBus(roundEnvironment);
        return true;
    }

    private void generateEventBus(RoundEnvironment roundEnv) {
        Set<ExecutableElement> busSet = ElementFilter.methodsIn(roundEnv.getElementsAnnotatedWith(BusEvent.class));
        checkOverrideMethod(busSet);

        if (ElementFilter.methodsIn(roundEnv.getElementsAnnotatedWith(BusEvent.class)).size() > 0) {
            parseBusEvent(roundEnv);
            produceProxyClass();
        }
    }

    private void produceProxyClass() {
        for (TypeElement element : erasedSet) {
            String packageName = mElements.getPackageOf(element).getQualifiedName().toString();
            String className;
            if (element.getNestingKind().isNested()) {
                className = element.getEnclosingElement().getSimpleName() + "$" + element.getSimpleName()
                        + "$$EventBinder";
            } else {
                className = element.getSimpleName() + "$$EventBinder";
            }
            TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(className)
                    .addModifiers(Modifier.PUBLIC)
                    .superclass(ParameterizedTypeName.get(EVENT_PROXY, ClassName.get(element)));
//                    .addTypeVariable(TypeVariableName.get("T", ClassName.get(element)))
//                    .superclass(ParameterizedTypeName.get(EVENT_PROXY, TypeVariableName.get("T")));
//                    .addSuperinterface(ParameterizedTypeName.get(EVENT_BINDER, TypeVariableName.get("T")));

            generateBindEventMethod(typeBuilder, element);

            /** 为减少方法数unBindEvent()放在父类EventProxy中实现 **/
//            generateUnBindEventMethod(typeBuilder, element);

            try {
                JavaFile javaFile = JavaFile.builder(packageName, typeBuilder.build()).build();
                javaFile.writeTo(mFiler);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 如果重载方法和被重载方法都有@BusEvent，则报错
     *
     * @param set
     */
    private void checkOverrideMethod(Set<ExecutableElement> set) {
        ArrayList<ExecutableElement> executableList = new ArrayList<>();
        executableList.addAll(set);
        for (int i = 0; i < executableList.size(); i++) {
            for (int j = i + 1; j < executableList.size(); j++) {
                if (Utils.isInherit(
                        this,
                        executableList.get(i).getEnclosingElement(),
                        executableList.get(j).getEnclosingElement()) &&
                        Utils.isOverrideMethod(this, executableList.get(i), executableList.get(j))) {
                    String reason = String.format("父类 %s 和 子类 %s 中的重载方法 %s 不能同时添加注解@BusEvent," +
                                    "如果父类添加则父类和子类都会有效，如果子类添加则只会子类生效",
                            executableList.get(i).getEnclosingElement().getSimpleName(),
                            executableList.get(j).getEnclosingElement().getSimpleName(),
                            executableList.get(i));
                    Utils.error(reason, this.mMessager);
                }
            }
        }
    }

    /**
     * 重载EventBinder接口中的bindEvent方法
     *
     * @param typeBuilder
     * @param rootElement
     */
    private void generateBindEventMethod(TypeSpec.Builder typeBuilder, TypeElement rootElement) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("bindEvent")
                .returns(void.class)
                .addAnnotation(Override.class)
//                .addParameter(TypeVariableName.get("T"), "target", Modifier.FINAL)
                .addParameter(ClassName.get(rootElement), "target", Modifier.FINAL)
                .addModifiers(Modifier.PUBLIC)
                .beginControlFlow("if(invoke.compareAndSet(false, true))")
                .addStatement("this.target = target");

        typeBuilder.addMethod(generateEventConsumeMethod(
                MethodSpec.methodBuilder("projectEventConsume"), projectBusMap.get(rootElement)));

        for (ExecutableElement executableElement : projectBusMap.get(rootElement)) {
            if (executableElement.getParameters() != null && executableElement.getParameters().size() == 1) {
                SchedulerInfo schedulerInfo = getSchedulerInfo(executableElement.getAnnotation(BusEvent.class));
                if (schedulerInfo.sync) {
                    builder.addStatement("$N.add($T.getDefault().register($N.class, $L, $L).subscribe($N))",
                            DISPOSABLE_LIST, RX_BUS, executableElement.getParameters().get(0).asType().toString(),
                            schedulerInfo.mainThread, true, "mProjectConsumer");
                } else {
                    builder.addStatement("$N.add($T.getDefault().register($N.class, $L).subscribe($N))",
                            DISPOSABLE_LIST, RX_BUS, executableElement.getParameters().get(0).asType().toString(),
                            schedulerInfo.mainThread, "mProjectConsumer");
                }
            }
        }


        builder.endControlFlow();
        typeBuilder.addMethod(builder.build());
    }


    /**
     * 存储@BusEvent 并根据scope存储在不同的map中
     *
     * @param roundEnv
     */
    private void parseBusEvent(RoundEnvironment roundEnv) {
        Set<ExecutableElement> busSet = ElementFilter.methodsIn(roundEnv.getElementsAnnotatedWith(BusEvent.class));
        checkOverrideMethod(busSet);
        for (ExecutableElement element : busSet) {
            if (element.getParameters() == null || element.getParameters().size() != 1) {
                String reason = String.format("%s 中的带有@BusEvent的方法 %s 必须带有参数并且参数数量只能为1",
                        element.getEnclosingElement().getSimpleName(), element.getSimpleName());
                Utils.error(reason, mMessager);
            }
            TypeElement root = (TypeElement) element.getEnclosingElement();
            erasedSet.add(root);
            if (element.getAnnotation(BusEvent.class).busType() == BusType.SCOPE_PROJECT) {
                if (projectBusMap.get(root) != null) {
                    projectBusMap.get(root).add(element);
                } else {
                    List<ExecutableElement> list = new ArrayList<>();
                    list.add(element);
                    projectBusMap.put(root, list);
                }
            }
        }
    }


    /**
     * 为减少方法数把匿名类Consumer放到父类，下沉projectEventConsume，pluginEventConsume，classEventConsume到子类
     *
     * @return
     */
    private MethodSpec generateEventConsumeMethod(
            MethodSpec.Builder builder, List<ExecutableElement> executableElementList) {
        builder.addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addParameter(Object.class, "event")
                .beginControlFlow("if(invoke.get())");

        for (ExecutableElement executableElement : executableElementList) {
            VariableElement variableElement = executableElement.getParameters().get(0);
            ClassName parameter = ClassName.bestGuess(variableElement.asType().toString());
            builder.beginControlFlow("if(event instanceof $T)", parameter)
                    .addStatement("target.$N(($T)event)", executableElement.getSimpleName(), parameter)
                    .endControlFlow();
        }
        builder.endControlFlow();
        return builder.build();
    }

    /**
     * 根据@BusEvent注解获取RxJava订阅方法需要调用线程调度的信息
     *
     * @param busEvent
     * @return
     */
    private SchedulerInfo getSchedulerInfo(BusEvent busEvent) {
        SchedulerInfo schedulerInfo = new SchedulerInfo();
        switch (busEvent.scheduler()) {
            case SchedulerType.THREAD_CURRENT:
                schedulerInfo.sync = true;
                break;
            case SchedulerType.THREAD_MAIN:
                schedulerInfo.sync = false;
                schedulerInfo.mainThread = true;
                break;
            case SchedulerType.THREAD_IO:
                schedulerInfo.sync = false;
                schedulerInfo.mainThread = false;
                break;
            default:
                schedulerInfo.mainThread = busEvent.mainThread();
                schedulerInfo.sync = busEvent.sync();
        }
        return schedulerInfo;
    }

    private static class SchedulerInfo {
        /**
         * 是否当前线程同步调用
         */
        boolean sync;
        /**
         * 异步调用线程设置为主线程还是io线程
         */
        boolean mainThread;
    }
}
