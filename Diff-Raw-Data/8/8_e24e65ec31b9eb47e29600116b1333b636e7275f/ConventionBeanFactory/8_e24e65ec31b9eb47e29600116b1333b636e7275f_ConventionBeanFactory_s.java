 package org.rosenvold.spring.convention;
 
 /*
  * Copyright 2011 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 import org.rosenvold.spring.convention.beanclassresolvers.DefaultBeanClassResolver;
 import org.springframework.beans.BeansException;
 import org.springframework.beans.factory.NoSuchBeanDefinitionException;
 import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
 import org.springframework.beans.factory.support.DefaultListableBeanFactory;
 import org.springframework.beans.factory.support.RootBeanDefinition;
 import org.springframework.context.ConfigurableApplicationContext;
 
 import java.lang.annotation.Annotation;
 
 public class ConventionBeanFactory
         extends DefaultListableBeanFactory {
 
     private final ConfigurableApplicationContext parent;
 
     private final NameToClassResolver beanClassResolver;
 
     public ConventionBeanFactory(ConfigurableApplicationContext parent) {
         super(parent);
         this.parent = parent;
 
         final NameToClassResolver bean = parent.getBean(NameToClassResolver.class);
         this.beanClassResolver = bean != null ? bean : new DefaultBeanClassResolver();
     }
 
 
     public ConventionBeanFactory(ConfigurableApplicationContext parentBeanFactory,
                                  NameToClassResolver beanClassResolver) {
         super(parentBeanFactory);
         this.parent = parentBeanFactory;
         this.beanClassResolver = beanClassResolver;
     }
 
     @Override
     public <T> T getBean(Class<T> requiredType)
             throws BeansException {
         final Class aClass = resolveClass(requiredType);
         if (aClass == null) {
             return parent.getBean(requiredType);
         }
         return instantiate(aClass);
     }
 
     @Override
     public Object getBean(String name)
             throws BeansException {
         final Class<?> type = getType(name);
         return type != null ? instantiate(type) : parent.getBean(name);
     }
 
     @Override
     public <T> T getBean(String s, Class<T> tClass)
             throws BeansException {
         final Class<?> type = getType(s);
         //noinspection unchecked
         return type != null && isTypeMatch(s, tClass) ? (T) instantiate(type) : parent.getBean(s, tClass);
     }
 
     @Override
     public Object getBean(String s, Object... objects)
             throws BeansException {
         throw new NoSuchBeanDefinitionException("Dont know");
     }
 
     @Override
     public boolean containsBean(String s) {
         final Class<?> type = getType(s);
         if (type == null) {
             return false;
         }
         // Todo: Use CandidateEvaluator
         final Annotation[] annotations = type.getAnnotations();
         return annotations.length > 0;
     }
 
     @Override
     public boolean isSingleton(String s)
             throws NoSuchBeanDefinitionException {
         return true;
     }
 
     @Override
     public boolean isPrototype(String s)
             throws NoSuchBeanDefinitionException {
         return false;
     }
 
     @Override
     public boolean isTypeMatch(String s, Class aClass)
             throws NoSuchBeanDefinitionException {
         final Class aClass1 = resolveImplClass(s);
         if (aClass1 == null) {
             return parent.isTypeMatch(s, aClass);
         }
         return aClass.isAssignableFrom(aClass1);
     }
 
     @Override
     public Class<?> getType(String s)
             throws NoSuchBeanDefinitionException {
         final Class aClass = resolveImplClass(s);
         if (aClass == null){
             return parent.containsBean( s) ? parent.getType( s) : null;
         }
         return aClass;
     }
 
     public Class<?> getLocalType(String s)
             throws NoSuchBeanDefinitionException {
         final Class aClass = resolveImplClass(s);
         return aClass;
     }
 
     @Override
     public String[] getAliases(String s) {
         return new String[0];
     }
 
 
     private Class resolveImplClass(final String beanName) {
         return beanClassResolver.resolveBean(beanName);
     }
 
     private Class resolveClass(final Class beanClass) {
         return resolveImplClass(beanNameFromClass(beanClass));
     }
 
     private String beanNameFromClass(final Class beanClass) {
         return beanClass.getName();
     }
 
     private <T> T instantiate(Class aClass)
             throws BeansException {
         return doGetBean(aClass.getName(), null, null, false);
     }
 
     @Override
    public String[] getBeanNamesForType(Class type, boolean includeNonSingletons, boolean allowEagerInit) {
         final Class aClass = resolveImplClass(type.getName());
         if (aClass != null) {
             return new String[]{aClass.getName()};
         }
        return parent.getBeanNamesForType(type, includeNonSingletons, allowEagerInit);
     }
 
     @Override
     protected RootBeanDefinition getMergedLocalBeanDefinition(String beanName)
             throws BeansException {
         final Class<?> type = getType(beanName);
         if (type != null) {
             final RootBeanDefinition rootBeanDefinition = new RootBeanDefinition(type, true);
             rootBeanDefinition.setAutowireMode(AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE);
             return rootBeanDefinition;
         }
 
         return null;
     }
 
     @Override
    public boolean containsBeanDefinition(String beanName) {
         final Class<?> type = getLocalType(beanName);
         return type != null;
     }
 
 
     @Override
     public boolean containsSingleton(String beanName) {
         final Class<?> type = getType(beanName);
         return type != null;
     }
 
 
 
 }
