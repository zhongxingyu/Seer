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
 
 import org.springframework.beans.BeansException;
 import org.springframework.beans.factory.NoSuchBeanDefinitionException;
 import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
 import org.springframework.beans.factory.config.BeanDefinition;
 import org.springframework.beans.factory.support.DefaultListableBeanFactory;
 import org.springframework.beans.factory.support.RootBeanDefinition;
 
 import java.lang.annotation.Annotation;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 public class ConventionBeanFactory
         extends DefaultListableBeanFactory {
 
     private final NameToClassResolver nameToClassResolver;
 
     private final CandidateEvaluator candidateEvaluator;
 
     public ConventionBeanFactory(NameToClassResolver beanClassResolver,
                                  CandidateEvaluator candidateEvaluator) {
         this.nameToClassResolver = beanClassResolver;
         this.candidateEvaluator = candidateEvaluator;
     }
 
     @Override
     public <T> T getBean(Class<T> requiredType) throws BeansException {
         if (isBeanInBaseClass(requiredType)) {
             return super.getBean(requiredType);
         }
         final Class aClass = resolveClass(requiredType);
        return (T) instantiate(aClass);
     }
 
     private <T> boolean isBeanInBaseClass(Class<T> requiredType) {
         return super.getBeanNamesForType(requiredType).length > 0;
     }
 
     @Override
     public Object getBean(String name) throws BeansException {
         if (super.containsBeanDefinition(name)) {
             return super.getBean(name);
         }
         final Class<?> type = getLocalType(name);
         return type != null ? instantiate(type) : null;
     }
 
     @Override
     public <T> T getBean(String name, Class<T> tClass) throws BeansException {
         if (super.containsBeanDefinition(name)) {
             return super.getBean(name, tClass);
         }
         final Class<?> type = getLocalType(name);
         //noinspection unchecked
         return type != null && isTypeMatch(name, tClass) ? (T) instantiate(type) : null;
     }
 
     @Override
     public Object getBean(String name, Object... objects) throws BeansException {
         if (super.containsBeanDefinition(name)) {
             return super.getBean(name, objects);
         }
         throw new NoSuchBeanDefinitionException("Dont know");
     }
 
     @Override
     public boolean containsBean(String name) {
         if (super.containsBeanDefinition(name)) {
             return true;
         }
         final Class<?> type = getType(name);
         if (type == null) {
             return false;
         }
         // Todo: Use CandidateEvaluator
         final Annotation[] annotations = type.getAnnotations();
         return annotations.length > 0;
     }
 
     @Override
     public boolean isSingleton(String name)
             throws NoSuchBeanDefinitionException {
         if (super.containsBeanDefinition(name)) {
             return super.isSingleton(name);
         }
         return true;
     }
 
     @Override
     public boolean isPrototype(String name) throws NoSuchBeanDefinitionException {
         if (super.containsBean(name)) {
             return super.isPrototype(name);
         }
         return false; // Todo: read annotation on ompl
     }
 
     @Override
     public boolean isTypeMatch(String name, Class aClass) throws NoSuchBeanDefinitionException {
         if (super.containsBeanDefinition(name)) {
             return super.isTypeMatch(name, aClass);
         }
         final Class aClass1 = resolveImplClass(name);
         return aClass1 != null && aClass.isAssignableFrom(aClass1);
     }
 
     @Override
     public Class<?> getType(String name) throws NoSuchBeanDefinitionException {
         if (super.containsBeanDefinition( name)){
             return super.getType(name);
         }
         final Class aClass = resolveImplClass(name);
         if (aClass == null) {
             return null;
         }
         return aClass;
     }
 
     private Class<?> getLocalType(String s) throws NoSuchBeanDefinitionException {
         final Class aClass = resolveImplClass(s);
         return aClass != null && candidateEvaluator.isBean(aClass) ? aClass : null;
     }
 
     @Override
     public String[] getAliases(String name) {
         if (super.containsBean(name)) {
             return super.getAliases(name);
         }
         return new String[0];
     }
 
     @Override
     public String[] getBeanNamesForType(Class type, boolean includeNonSingletons, boolean allowEagerInit) { // LBF, local only.
         final String[] beanNamesForType = super.getBeanNamesForType(type, includeNonSingletons, allowEagerInit);
         if (beanNamesForType.length > 0) return beanNamesForType;
 
         final Class aClass = resolveImplClass(type.getName());
         if (aClass != null) {
             return new String[]{aClass.getName()};
         }
         return new String[]{};
     }
 
     @Override
     public boolean containsBeanDefinition(String beanName) {  // LBF; local only
         final Class<?> type = getLocalType(beanName);
         return type != null;
     }
 
 
 /*    @Override
     public boolean containsSingleton(String beanName) {
         final Class<?> type = getType(beanName);
         return type != null;
     }
   */
 
 
     /*private Class resolveImplClass(final String beanName) {
         return nameToClassResolver.resolveBean(beanName, candidateEvaluator);
     } */
 
     private final Map<String, Class> cache = new ConcurrentHashMap<String, Class>();
 
     private static class CacheMiss {}
 
     private Class resolveImplClass(final String beanName) {
         Class aClass = cache.get(beanName);
         if (aClass != null && !CacheMiss.class.equals(aClass )) return aClass;
         aClass = nameToClassResolver.resolveBean(beanName, candidateEvaluator);
         cache.put( beanName, aClass != null ? aClass : CacheMiss.class);
         return aClass;
     }
 
     private Class resolveClass(final Class beanClass) {
         return resolveImplClass(beanNameFromClass(beanClass));
     }
 
     private String beanNameFromClass(final Class beanClass) {
         return beanClass.getName();
     }
 
    private Object instantiate(Class aClass) throws BeansException {
         return doGetBean(aClass.getName(), null, null, false);
     }
 
 
     @Override
     protected RootBeanDefinition getMergedLocalBeanDefinition(String beanName)
             throws BeansException {
         if (super.containsBeanDefinition(beanName)) return super.getMergedLocalBeanDefinition(beanName);
         final Class<?> type = getType(beanName);
         if (type != null) {
             final RootBeanDefinition rootBeanDefinition = new RootBeanDefinition(type, true);
             rootBeanDefinition.setAutowireMode(AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE);
             return rootBeanDefinition;
         }
 
         return null;
     }
 
     @Override
     public BeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException {
         if (super.containsBeanDefinition(beanName)) {
             return super.getBeanDefinition(beanName);
         }
         final Class<?> type = getType(beanName);
         final RootBeanDefinition rootBeanDefinition = new RootBeanDefinition(type, true);
         rootBeanDefinition.setAutowireMode(AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE);
         return rootBeanDefinition;
     }
 
 }
