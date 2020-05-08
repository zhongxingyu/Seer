 /*
  * Copyright (c) 2012-2013 Veniamin Isaias.
  *
  * This file is part of web4thejob.
  *
  * Web4thejob is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or any later version.
  *
  * Web4thejob is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with web4thejob.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.web4thejob.util.converter;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.config.BeanDefinition;
 import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
 import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
 import org.springframework.core.type.filter.AssignableTypeFilter;
 import org.springframework.util.ClassUtils;
 import org.web4thejob.module.Joblet;
 import org.web4thejob.module.SystemJoblet;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * @author Veniamin Isaias
  * @since 3.4.3
  */
 public class JobletScanner extends ClassPathScanningCandidateComponentProvider {
     private static final Logger LOG = Logger.getLogger(JobletScanner.class);
 
     public JobletScanner() {
         super(false);
         addIncludeFilter(new AssignableTypeFilter(Joblet.class));
     }
 
     @SuppressWarnings("unchecked")
     public final Collection getComponentClasses(String basePackage) {
         boolean systemJobletFound = false;
 
         basePackage = basePackage == null ? "" : basePackage;
         Set<Class<?>> classes = new HashSet<Class<?>>();
         for (BeanDefinition candidate : findCandidateComponents(basePackage)) {
             try {
                 Class cls = ClassUtils.resolveClassName(candidate.getBeanClassName(),
                         ClassUtils.getDefaultClassLoader());
 
                 if (SystemJoblet.class.isAssignableFrom(cls)) {
                     systemJobletFound = true;
                 }
 
                 classes.add(cls);
             } catch (Throwable ex) {
                 ex.printStackTrace();
             }
         }
 
         if (!systemJobletFound) {
             final ClassLoader classLoader = SystemJoblet.class.getClassLoader();
             setResourceLoader(new PathMatchingResourcePatternResolver(classLoader));
             resetFilters(false);
             addIncludeFilter(new AssignableTypeFilter(SystemJoblet.class));
 
             for (BeanDefinition candidate : findCandidateComponents("org.web4thejob")) {
                 try {
                     Class cls = ClassUtils.resolveClassName(candidate.getBeanClassName(),
                             ClassUtils.getDefaultClassLoader());
 
                     if (SystemJoblet.class.isAssignableFrom(cls)) {
                        systemJobletFound = true;
                     }
 
                     classes.add(cls);
                 } catch (Throwable ex) {
                     ex.printStackTrace();
                 }
             }
         }
 
        if (!systemJobletFound) {
            LOG.error("Unable to scan System Joblet!");

        }

         return classes;
     }
 }
