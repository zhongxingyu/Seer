 /*
  * Copyright (c) 2010 Carman Consulting, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.metastopheles;
 
 import java.beans.*;
 import java.lang.reflect.Method;
 import java.util.*;
 import java.util.concurrent.ConcurrentHashMap;
 
 /**
  * @author James Carman
  * @since 1.0
  */
 public class BeanMetaDataFactory
 {
 //----------------------------------------------------------------------------------------------------------------------
 // Fields
 //----------------------------------------------------------------------------------------------------------------------
 
     public static final String CLASS_PROPERTY = "class";
    private static final Map<String,BeanMetaDataFactory> FACTORY_REGISTRY = new ConcurrentHashMap<String, BeanMetaDataFactory>();
     private final Map<Class, BeanMetaData> metaDataMap = new WeakHashMap<Class, BeanMetaData>();
 
     private List<MetaDataDecorator<BeanMetaData>> beanMetaDataDecorators = new LinkedList<MetaDataDecorator<BeanMetaData>>();
     private List<MetaDataDecorator<MethodMetaData>> methodMetaDataDecorators = new LinkedList<MetaDataDecorator<MethodMetaData>>();
     private List<MetaDataDecorator<PropertyMetaData>> propertyMetaDataDecorators = new LinkedList<MetaDataDecorator<PropertyMetaData>>();
     private final String id = UUID.randomUUID().toString();
 
 //----------------------------------------------------------------------------------------------------------------------
 // Static Methods
 //----------------------------------------------------------------------------------------------------------------------
 
     static BeanMetaDataFactory get(String id)
     {
        return FACTORY_REGISTRY.get(id);
     }
 
 //----------------------------------------------------------------------------------------------------------------------
 // Constructors
 //----------------------------------------------------------------------------------------------------------------------
 
     public BeanMetaDataFactory()
     {
        FACTORY_REGISTRY.put(id, this);
     }
 
 //----------------------------------------------------------------------------------------------------------------------
 // Getter/Setter Methods
 //----------------------------------------------------------------------------------------------------------------------
 
     public List<MetaDataDecorator<BeanMetaData>> getBeanMetaDataDecorators()
     {
         return beanMetaDataDecorators;
     }
 
     public void setBeanMetaDataDecorators(List<MetaDataDecorator<BeanMetaData>> beanMetaDataDecorators)
     {
         this.beanMetaDataDecorators = beanMetaDataDecorators;
     }
 
     public List<MetaDataDecorator<MethodMetaData>> getMethodMetaDataDecorators()
     {
         return methodMetaDataDecorators;
     }
 
     public void setMethodMetaDataDecorators(List<MetaDataDecorator<MethodMetaData>> methodMetaDataDecorators)
     {
         this.methodMetaDataDecorators = methodMetaDataDecorators;
     }
 
     public List<MetaDataDecorator<PropertyMetaData>> getPropertyMetaDataDecorators()
     {
         return propertyMetaDataDecorators;
     }
 
     public void setPropertyMetaDataDecorators(List<MetaDataDecorator<PropertyMetaData>> propertyMetaDataDecorators)
     {
         this.propertyMetaDataDecorators = propertyMetaDataDecorators;
     }
 
 //----------------------------------------------------------------------------------------------------------------------
 // Other Methods
 //----------------------------------------------------------------------------------------------------------------------
 
     public synchronized void clear()
     {
         metaDataMap.clear();
     }
 
     public synchronized BeanMetaData getBeanMetaData(Class beanClass)
     {
         if (metaDataMap.containsKey(beanClass))
         {
             return metaDataMap.get(beanClass);
         }
         else
         {
             try
             {
                 BeanInfo beanInfo = Introspector.getBeanInfo(beanClass);
                 BeanMetaData beanMetaData = new BeanMetaData(id, beanInfo.getBeanDescriptor());
                 for (MetaDataDecorator<BeanMetaData> decorator : beanMetaDataDecorators)
                 {
                     decorator.decorate(beanMetaData);
                 }
                 final Set<Method> visitedMethods = new HashSet<Method>();
                 addPropertyDescriptors(beanInfo, beanMetaData, visitedMethods);
                 addMethodDescriptors(beanInfo, beanMetaData, visitedMethods);
                 metaDataMap.put(beanClass, beanMetaData);
                 return beanMetaData;
             }
             catch (IntrospectionException e)
             {
                 throw new MetaDataException("Unable to lookup bean information for bean class " + beanClass.getName() + ".", e);
             }
         }
     }
 
     private void addPropertyDescriptors(BeanInfo beanInfo, BeanMetaData beanMetaData, Set<Method> visitedMethods)
     {
         for (PropertyDescriptor descriptor : beanInfo.getPropertyDescriptors())
         {
             visitedMethods.add(descriptor.getReadMethod());
             visitedMethods.add(descriptor.getWriteMethod());
             if (!CLASS_PROPERTY.equals(descriptor.getName()))
             {
                 final PropertyMetaData propertyMetaData = new PropertyMetaData(beanMetaData, descriptor);
                 beanMetaData.addPropertyMetaData(propertyMetaData);
                 for (MetaDataDecorator<PropertyMetaData> decorator : propertyMetaDataDecorators)
                 {
                     decorator.decorate(propertyMetaData);
                 }
             }
         }
     }
 
     private void addMethodDescriptors(BeanInfo beanInfo, BeanMetaData beanMetaData, Set<Method> visitedMethods)
     {
         for (MethodDescriptor descriptor : beanInfo.getMethodDescriptors())
         {
             if (!visitedMethods.contains(descriptor.getMethod()) && !Object.class.equals(descriptor.getMethod().getDeclaringClass()))
             {
                 final MethodMetaData methodMetaData = new MethodMetaData(beanMetaData, descriptor);
                 beanMetaData.addMethodMetaData(methodMetaData);
                 for (MetaDataDecorator<MethodMetaData> decorator : methodMetaDataDecorators)
                 {
                     decorator.decorate(methodMetaData);
                 }
             }
         }
     }
 }
