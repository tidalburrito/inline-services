package com.tb.fw;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;
import com.tb.fw.annotations.InlineService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@AutoService(Processor.class)
public class InlineAnnotationProcessor extends AbstractProcessor {
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(InlineService.class.getName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(InlineService.class)) {
            String packageName = processingEnv.getElementUtils().getPackageOf(element).toString();
            String className = element.getSimpleName().toString();
            createClient(element);
            createFeignClient(element);
            createServer(element);
        }
        return false;
    }

    public void createClient(Element element){
        String packageName = processingEnv.getElementUtils().getPackageOf(element).toString();
        String className = element.getSimpleName().toString();
        TypeSpec.Builder wrapperClassBuilder = TypeSpec.classBuilder(className+"Client")
                .addModifiers(Modifier.PUBLIC)
                .superclass(ClassName.get(
                        processingEnv.getElementUtils().getPackageOf(element).toString(),
                        element.getSimpleName().toString()
                ));
        AnnotationSpec.Builder annotationBuilder = AnnotationSpec.builder(Component.class);
        wrapperClassBuilder.addAnnotation(annotationBuilder.build());
        ClassName feignClientClass = ClassName.get(packageName,className + "FeignClient");

        wrapperClassBuilder.addField(
                FieldSpec.builder(feignClientClass, "delegate", Modifier.PRIVATE, Modifier.FINAL).build()
        );

        FieldSpec fieldSpec = FieldSpec.builder(ClassName.get(InlineClientCache.class), "feignClientCache")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(Autowired.class).build())
                .addAnnotation(AnnotationSpec.builder(Qualifier.class)
                        .addMember("value", "\"InlineClientCache\"").build())
                .build();
        wrapperClassBuilder.addField(fieldSpec);

        wrapperClassBuilder.addMethod(MethodSpec.methodBuilder("init")
                .addAnnotation(AnnotationSpec.builder(PostConstruct.class).build())
                .addStatement("System.out.println(\"Calling feignclient "+element.getSimpleName().toString()+" init\");")
                .addStatement("this.feignClientCache.put(\""
                        + processingEnv.getElementUtils().getPackageOf(element).toString()
                        + "."+element.getSimpleName().toString()
                        +"\", this)")
                .addStatement("System.out.println(\"clientmap=\"+this.feignClientCache);")
                .build());

        wrapperClassBuilder.addMethod(MethodSpec.constructorBuilder()
                .addParameter(feignClientClass, "delegate")
                .addAnnotation(AnnotationSpec.builder(Autowired.class).build())
                .addStatement("this.delegate = delegate")
                .build());

        List<? extends Element> enclosedElements = element.getEnclosedElements();
        List<ExecutableElement> methods = ElementFilter.methodsIn(enclosedElements);
        for (ExecutableElement method : methods) {
            if(isMethodAnnotatedWithInlineService(method)) {
                String methodName = method.getSimpleName().toString();
                TypeMirror returnType = method.getReturnType();
                String returnTypeName = returnType.toString();
                MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodName);
                methodBuilder.returns(TypeName.get(returnType))
                        .addModifiers(Modifier.PUBLIC);

                List<? extends VariableElement> parameters = method.getParameters();
                List<String> paramList = new ArrayList<>();
                for (VariableElement parameter : parameters) {
                    String parameterName = parameter.getSimpleName().toString();
                    ParameterSpec parameterSpec = ParameterSpec.builder(TypeName.get(parameter.asType()), parameterName)
                            .build();
                    methodBuilder.addParameter(parameterSpec);
                    paramList.add(parameterName);
                }
                for(String param: paramList){
                    methodBuilder.addStatement("System.out.println(\"" + param + "=\"+"+param+")");
                }
                if (TypeName.get(returnType).equals(TypeName.VOID)) {
                    methodBuilder.addStatement("delegate." + methodName + "(" + String.join(",", paramList) + ")");
                } else {
                    methodBuilder.addStatement("return delegate." + methodName + "(" + String.join(",", paramList) + ")");
                }
                wrapperClassBuilder.addMethod(methodBuilder.build());
            }
        }

        createJavaFile(packageName, className+"Client", wrapperClassBuilder.build(), element);
    }

    private void createFeignClient(Element element){
        String packageName = processingEnv.getElementUtils().getPackageOf(element).toString();
        String className = element.getSimpleName().toString();
        TypeSpec.Builder wrapperClassBuilder = TypeSpec.interfaceBuilder(className+"FeignClient").addModifiers(Modifier.PUBLIC);

        String urlParam=(packageName+ "."+ className).toLowerCase();
        AnnotationSpec.Builder annotationBuilder = AnnotationSpec.builder(FeignClient.class);
        annotationBuilder.addMember("name", "\""+className.toLowerCase()+"\"");
        annotationBuilder.addMember("url", "\"$${"+urlParam+"}/"+(className+"Server").toLowerCase()+"\"");
        wrapperClassBuilder.addAnnotation(annotationBuilder.build());

        List<? extends Element> enclosedElements = element.getEnclosedElements();
        List<ExecutableElement> methods = ElementFilter.methodsIn(enclosedElements);
        for (ExecutableElement method : methods) {
            if(isMethodAnnotatedWithInlineService(method)) {
                String methodName = method.getSimpleName().toString();
                TypeMirror returnType = method.getReturnType();
                MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodName);
                methodBuilder.returns(TypeName.get(returnType))
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);
                annotationBuilder = AnnotationSpec.builder(GetMapping.class);
                annotationBuilder.addMember("value", "\"/" + method.getSimpleName().toString().toLowerCase() + "\"");
                methodBuilder.addAnnotation(annotationBuilder.build());

                List<? extends VariableElement> parameters = method.getParameters();
                for (VariableElement parameter : parameters) {
                    String parameterName = parameter.getSimpleName().toString();
                    ParameterSpec.Builder pmSpecBuilder=ParameterSpec.builder(TypeName.get(parameter.asType()), parameterName)
                                    .addAnnotation(AnnotationSpec.builder(RequestParam.class).addMember("name", "\""+parameterName+"\"").build());
                    methodBuilder.addParameter(pmSpecBuilder.build());
                }
                wrapperClassBuilder.addMethod(methodBuilder.build());
            }
        }

        createJavaFile(packageName, className+"FeignClient", wrapperClassBuilder.build(), element);
    }

    private void createServer(Element element){

        String packageName = processingEnv.getElementUtils().getPackageOf(element).toString();
        String className = element.getSimpleName().toString();
        TypeSpec.Builder wrapperClassBuilder = TypeSpec.classBuilder(className+"Server")
                .addModifiers(Modifier.PUBLIC);

        String urlParam=(packageName +"." + className).toLowerCase();
        wrapperClassBuilder.addAnnotation(AnnotationSpec.builder(RestController.class).build());
        AnnotationSpec.Builder annotationBuilder = AnnotationSpec.builder(RequestMapping.class);
        annotationBuilder.addMember("value", "\"/"+(className+"Server").toLowerCase()+"\"");
        wrapperClassBuilder.addAnnotation(annotationBuilder.build());

        wrapperClassBuilder.addField(FieldSpec.builder(TypeName.get(element.asType()), "delegate", Modifier.PRIVATE, Modifier.FINAL).build());
        wrapperClassBuilder.addMethod(MethodSpec.constructorBuilder()
                .addParameter(TypeName.get(element.asType()), "delegate")
                .addAnnotation(AnnotationSpec.builder(Autowired.class).build())
                .addStatement("this.delegate = delegate")
                .build());

        List<? extends Element> enclosedElements = element.getEnclosedElements();
        List<ExecutableElement> methods = ElementFilter.methodsIn(enclosedElements);
        for (ExecutableElement method : methods) {
            if(isMethodAnnotatedWithInlineService(method)) {
                String methodName = method.getSimpleName().toString();
                TypeMirror returnType = method.getReturnType();
                String returnTypeName = returnType.toString();
                MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodName);
                methodBuilder.returns(TypeName.get(returnType))
                        .addModifiers(Modifier.PUBLIC);
                annotationBuilder = AnnotationSpec.builder(RequestMapping.class);
                annotationBuilder.addMember("value", "\"/" + methodName.toLowerCase() + "\"");
                methodBuilder.addAnnotation(annotationBuilder.build());

                List<? extends VariableElement> parameters = method.getParameters();
                List<String> paramList = new ArrayList<>();
                for (VariableElement parameter : parameters) {
                    String parameterName = parameter.getSimpleName().toString();
                    ParameterSpec parameterSpec = ParameterSpec.builder(TypeName.get(parameter.asType()), parameterName)
                            .build();
                    methodBuilder.addParameter(parameterSpec);
                    paramList.add(parameterName);
                }
                if (TypeName.get(returnType).equals(TypeName.VOID)) {
                    methodBuilder.addStatement("delegate." + methodName + "(" + String.join(",", paramList) + ")");
                } else {
                    methodBuilder.addStatement("return delegate." + methodName + "(" + String.join(",", paramList) + ")");
                }
                wrapperClassBuilder.addMethod(methodBuilder.build());
            }
        }

        createJavaFile(packageName, className+"Server", wrapperClassBuilder.build(), element);
    }

    private boolean isMethodAnnotatedWithInlineService(ExecutableElement executableElement) {
        for (AnnotationMirror annotationMirror : executableElement.getAnnotationMirrors()) {
            TypeElement annotationTypeElement = (TypeElement) annotationMirror.getAnnotationType().asElement();
            if (annotationTypeElement.getQualifiedName().toString().equals("com.tb.fw.annotations.InlineApi")) {
                return true;
            }
        }
        return false;
    }

    private void createJavaFile(String packageName, String className, TypeSpec classSpec, Element element){
        JavaFile javaFile = JavaFile.builder(packageName, classSpec)
                .build();
        try {
            JavaFileObject sourceFile = processingEnv.getFiler().createSourceFile(packageName + "." + className, element);
            Writer writer = sourceFile.openWriter();
            javaFile.writeTo(writer);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
