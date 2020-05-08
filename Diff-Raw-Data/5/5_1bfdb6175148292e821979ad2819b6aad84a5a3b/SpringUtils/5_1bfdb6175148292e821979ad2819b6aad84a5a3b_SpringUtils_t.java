 package com.hazelcast.spring;
 
 import com.hazelcast.common.ClasspathScanEventListener;
 import org.springframework.beans.factory.config.BeanDefinition;
 import org.springframework.beans.factory.support.BeanDefinitionRegistry;
 import org.springframework.beans.factory.support.RootBeanDefinition;
 import org.springframework.beans.factory.xml.ParserContext;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
 import org.springframework.core.type.filter.AssignableTypeFilter;
 import org.springframework.util.ClassUtils;
 
 /**
  * Date: 31/03/2013 12:50
  * Author Yusuf Soysal
  */
 public class SpringUtils {
 
    public void registerNewBean(ParserContext pc, Class<?> clz) {
         BeanDefinitionRegistry registry = pc.getRegistry();
         BeanDefinition definition = new RootBeanDefinition(clz);
         String beanName = SpringConstants.BEAN_NAME_PREFIX + clz.getName();
         registry.registerBeanDefinition(beanName, definition);
     }
 
     public static final void findClasses(String basePackage, ClasspathScanEventListener eventListener) {
         ComponentScanningClass scanningClass = new ComponentScanningClass();
 
         scanningClass.findClasses(basePackage, eventListener);
     }
 
     private static class ComponentScanningClass extends ClassPathScanningCandidateComponentProvider {
 
         public ComponentScanningClass() {
             super(false);
         }
 
         public void findClasses(String basePackage, ClasspathScanEventListener clz) {
             resetFilters(false);
             //addIncludeFilter( new AnnotationTypeFilter(clz));
             addIncludeFilter(new AssignableTypeFilter(Object.class));
 
             basePackage = basePackage == null ? "" : basePackage;
             for (BeanDefinition candidate : findCandidateComponents(basePackage)) {
                 clz.classFound(ClassUtils.resolveClassName(candidate.getBeanClassName(), ClassUtils.getDefaultClassLoader()));
             }
         }
 
     }
 }
