 /*
  * Copyright 2011 the original author or authors.
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.jayway.jaxrs.hateoas.support;
 
 import com.jayway.jaxrs.hateoas.HateoasInjectException;
 import com.jayway.jaxrs.hateoas.HateoasLinkInjector;
 import com.jayway.jaxrs.hateoas.HateoasVerbosity;
 import com.jayway.jaxrs.hateoas.LinkProducer;
 import javassist.*;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * {@link HateoasLinkInjector} implementation that uses javassist to dynamically add a field in the target entities
  * where the links can be injected. This enables usage of the framework without <b>any</b> impact on the actual DTOs.
  *
  * @author Mattias Hellborg Arthursson
  * @author Kalle Stenflo
  */
 public class JavassistHateoasLinkInjector implements HateoasLinkInjector<Object> {
 
     private static final Logger log = LoggerFactory.getLogger(JavassistHateoasLinkInjector.class);
 
     private static final ClassPool CLASS_POOL = ClassPool.getDefault();
 
     private final static Map<String, Class<?>> TRANSFORMED_CLASSES = new HashMap<String, Class<?>>();
 
     static {
         CLASS_POOL.appendClassPath(new LoaderClassPath(
                 JavassistHateoasLinkInjector.class.getClassLoader()));
     }
 
     private HateoasLinkInjector<Object> injector = new HateoasLinkBeanLinkInjector();
 
     @Override
     public boolean canInject(Object entity) {
         return true;
     }
 
     @Override
     public Object injectLinks(Object entity, LinkProducer<Object> linkProducer,
                               final HateoasVerbosity verbosity) {
 
         if (entity == null) {
             return null;
         }
 
 
         String newClassName = entity.getClass().getPackage().getName() + "." + entity.getClass().getSimpleName() + "_generated";
 
         Class<?> clazz;
         if (!TRANSFORMED_CLASSES.containsKey(newClassName)) {
             synchronized (this) {
                 try {
                     log.debug("Creating HATEOAS subclass for DTO : {}", entity.getClass());
 
                     boolean valid = false;
                     for (Constructor<?> c : entity.getClass().getConstructors()) {
                         if (c.getParameterTypes().length == 0) {
                             valid = true;
                             break;
                         }
                     }
                     if (!valid){
                         throw new HateoasInjectException("DTO's must have no arg constructor. Check " + entity.getClass().getName());
                     }
 
                     CtClass newClass = CLASS_POOL.makeClass(newClassName);
                     newClass.setSuperclass(CLASS_POOL.get(entity.getClass().getName()));
                     CtConstructor ctConstructor = new CtConstructor(new CtClass[0], newClass);
                     ctConstructor.setBody("super();");
                     newClass.addConstructor(ctConstructor);
 
                     CtField newField = CtField.make("public java.util.Collection links;", newClass);
                     newClass.addField(newField);
 
                     CtMethod linksGetterMethod = CtMethod.make("public java.util.Collection getLinks(){ return this.links; }", newClass);
                     newClass.addMethod(linksGetterMethod);
 
                     CtMethod linksSetterMethod = CtMethod.make("public void setLinks(java.util.Collection links){ this.links = links; }", newClass);
                     newClass.addMethod(linksSetterMethod);
 
                     newClass.addInterface(CLASS_POOL.get("com.jayway.jaxrs.hateoas.HateoasLinkBean"));
 
 
                     StringBuilder cloneMethodBody = new StringBuilder();
 
                     for (Field field : ReflectionUtils.getFieldsHierarchical(entity.getClass())) {
                         cloneMethodBody.append("com.jayway.jaxrs.hateoas.support.ReflectionUtils.setFieldHierarchical(this, \"" + field.getName() + "\", com.jayway.jaxrs.hateoas.support.ReflectionUtils.getFieldValueHierarchical(other, \"" + field.getName() + "\"));");
                     }
                     String method = "public void hateoasCopy(" + entity.getClass().getName() + " other ){ " + cloneMethodBody.toString() + "}";
 
                     CtMethod cloneMethod = CtMethod.make(method, newClass);
                     newClass.addMethod(cloneMethod);
 
                    URLClassLoader classLoader = new URLClassLoader(new URL[0], this.getClass().getClassLoader());
                    clazz = newClass.toClass(classLoader, this.getClass().getProtectionDomain());
 
                     TRANSFORMED_CLASSES.put(newClassName, clazz);
                 } catch (Exception e) {
                     if(e instanceof HateoasInjectException){
                         throw (HateoasInjectException)e;
                     }
                     throw new RuntimeException(e);
                 }
             }
         } else {
             clazz = TRANSFORMED_CLASSES.get(newClassName);
         }
 
         Object newInstance = null;
         try {
             newInstance = clazz.newInstance();
             Method copyMethod = newInstance.getClass().getMethod("hateoasCopy", entity.getClass());
             copyMethod.invoke(newInstance, entity);
         } catch (Exception e) {
             log.error("could not create instance of " + clazz.getName(), e);
         }
 
         return injector.injectLinks(newInstance, linkProducer, verbosity);
     }
 
 }
