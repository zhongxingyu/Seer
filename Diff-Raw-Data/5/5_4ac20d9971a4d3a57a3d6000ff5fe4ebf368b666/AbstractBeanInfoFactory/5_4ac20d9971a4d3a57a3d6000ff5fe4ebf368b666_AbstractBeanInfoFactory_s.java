 /*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
 package org.jboss.beans.info.plugins;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.WeakHashMap;
 
 import org.jboss.beans.info.spi.BeanInfo;
 import org.jboss.beans.info.spi.BeanInfoFactory;
 import org.jboss.beans.info.spi.EventInfo;
 import org.jboss.beans.info.spi.PropertyInfo;
 import org.jboss.classadapter.spi.ClassAdapter;
 import org.jboss.reflect.spi.AnnotationValue;
 import org.jboss.reflect.spi.ClassInfo;
 import org.jboss.reflect.spi.ConstructorInfo;
 import org.jboss.reflect.spi.MethodInfo;
 import org.jboss.reflect.spi.PrimitiveInfo;
 import org.jboss.reflect.spi.TypeInfo;
 import org.jboss.util.JBossStringBuilder;
 import org.jboss.util.collection.WeakValueHashMap;
 
 /**
  * A bean info factory.
  * 
  * @author <a href="adrian@jboss.com">Adrian Brock</a>
  * @version $Revision$
  */
 public class AbstractBeanInfoFactory implements BeanInfoFactory
 {
    /** The cache */
    protected Map<ClassLoader, Map<String, BeanInfo>> cache = new WeakHashMap<ClassLoader, Map<String, BeanInfo>>(); 
    
    protected static boolean isGetter(MethodInfo minfo)
    {
       String name = minfo.getName();
       TypeInfo returnType = minfo.getReturnType();
       TypeInfo[] parameters = minfo.getParameterTypes();
       if ((name.length() > 3 && name.startsWith("get")) || (name.length() > 2 && name.startsWith("is")))
       {
          // isBoolean() is not a getter for java.lang.Boolean
         if (name.startsWith("is") && returnType.equals(PrimitiveInfo.BOOLEAN) == false)
             return false;
          if (parameters.length == 0 && PrimitiveInfo.VOID.equals(returnType) == false)
             return true;
       }
       return false;
    }
    
    protected static boolean isSetter(MethodInfo minfo)
    {
       String name = minfo.getName();
       TypeInfo returnType = minfo.getReturnType();
       TypeInfo[] parameters = minfo.getParameterTypes();
       if ((name.length() > 3 && name.startsWith("set")))
       {
          if (parameters.length == 1 && PrimitiveInfo.VOID.equals(returnType))
             return true;
       }
       return false;
    }
    
    protected static String getUpperPropertyName(String name)
    {
       int start = 3;
       if (name.startsWith("is"))
          start = 2;
       
       return name.substring(start);
    }
    
    protected static String getLowerPropertyName(String name)
    {
       // If the second character is upper case then we don't make
       // the first character lower case
       if (name.length() > 1)
       {
          if (Character.isUpperCase(name.charAt(1)))
             return name;
       }
 
       JBossStringBuilder buffer = new JBossStringBuilder(name.length());
       buffer.append(Character.toLowerCase(name.charAt(0)));
       if (name.length() > 1)
          buffer.append(name.substring(1));
       return buffer.toString();
    }
 
    /**
     * Create a new bean info factory
     */
    public AbstractBeanInfoFactory()
    {
    }
 
    public BeanInfo getBeanInfo(ClassAdapter classAdapter)
    {
       synchronized (cache)
       {
          ClassLoader cl = classAdapter.getClassLoader();
          ClassInfo classInfo = classAdapter.getClassInfo();
          String className = classInfo.getName();
          Map<String, BeanInfo> map = cache.get(cl);
          if (map != null)
          {
             BeanInfo info = map.get(className);
             if (info != null)
                return info;
          }
 
          Set<ConstructorInfo> constructors = getConstructors(classInfo);
          Set<MethodInfo> methods = getMethods(classInfo);
         Set<PropertyInfo> properties = null;
          if (classInfo.isAnnotation())
             properties = getAnnotationProperties(methods);
          else
             properties = getBeanProperties(methods);
          Set<EventInfo> events = getEvents(classInfo);
          
          BeanInfo result = createBeanInfo(classAdapter, properties, constructors, methods, events);
          if (map == null)
          {
             map = new WeakValueHashMap<String, BeanInfo>();
             cache.put(cl, map);
          }
          map.put(className, result);
          return result;
       }
    }
    
    /**
     * Create the bean info
     * 
     * @param classAdapter the class adapter
     * @param properties the properties
     * @param constructors the constructors
     * @param methods the methods
     * @param events the events
     * @return the bean info
     */
    protected BeanInfo createBeanInfo(ClassAdapter classAdapter, Set<PropertyInfo> properties, Set<ConstructorInfo> constructors, Set<MethodInfo> methods, Set<EventInfo> events)
    {
       return new AbstractBeanInfo(this, classAdapter, properties, constructors, methods, events);
    }
    
    /**
     * Get the constructors
     * 
     * @param classInfo the class info
     * @return the constructors
     */
    protected Set<ConstructorInfo> getConstructors(ClassInfo classInfo)
    {
       ConstructorInfo[] cinfos = classInfo.getDeclaredConstructors();
       if (cinfos == null || cinfos.length == 0)
          return null;
 
       HashSet<ConstructorInfo> result = new HashSet<ConstructorInfo>();
       for (int i = 0; i < cinfos.length; ++i)
       {
          if (cinfos[i].isPublic() && cinfos[i].isStatic() == false)
             result.add(cinfos[i]);
       }
       return result;
    }
    
    /**
     * Get the methods
     * 
     * @param classInfo the class info
     * @return the methods
     */
    protected Set<MethodInfo> getMethods(ClassInfo classInfo)
    {
       HashSet<MethodInfo> result = new HashSet<MethodInfo>();
       
       while (classInfo != null)
       {
          MethodInfo[] minfos = classInfo.getDeclaredMethods();
          if (minfos != null && minfos.length > 0)
          {
             for (int i = 0; i < minfos.length; ++i)
             {
                if (result.contains(minfos[i]) == false && minfos[i].isPublic() && minfos[i].isStatic() == false)
                   result.add(minfos[i]);
             }
          }
          
          classInfo = classInfo.getSuperclass();
       }
       
       return result;
    }
    
    /**
     * Get the properties for a bean
     * 
     * @param methods the methods
     * @return the properties
     */
    protected Set<PropertyInfo> getBeanProperties(Set<MethodInfo> methods)
    {
       HashMap<String, MethodInfo> getters = new HashMap<String, MethodInfo>();
       HashMap<String, List<MethodInfo>> setters = new HashMap<String, List<MethodInfo>>();
       if (methods.isEmpty() == false)
       {
          for (MethodInfo methodInfo : methods)
          {
             String name = methodInfo.getName();
             String upperName = getUpperPropertyName(name);
             if (isGetter(methodInfo))
             {
                getters.put(upperName, methodInfo);
             }
             else if (isSetter(methodInfo))
             {
                List<MethodInfo> list = setters.get(upperName);
                if (list == null)
                {
                   list = new ArrayList<MethodInfo>();
                   setters.put(upperName, list);
                }
                list.add(methodInfo);
             }
          }
       }
 
       HashSet<PropertyInfo> properties = new HashSet<PropertyInfo>();
       if (getters.isEmpty() == false)
       {
          for (Iterator<Map.Entry<String, MethodInfo>> i = getters.entrySet().iterator(); i.hasNext();)
          {
             Map.Entry<String, MethodInfo> entry = i.next();
             String name = entry.getKey();
             MethodInfo getter = entry.getValue();
             MethodInfo setter = null;
             List<MethodInfo> setterList = setters.remove(name);
             if (setterList != null && setterList.size() != 0)
             {
                for (int j = 0; j < setterList.size(); ++j)
                {
                   MethodInfo thisSetter = setterList.get(j);
                   TypeInfo pinfo = thisSetter.getParameterTypes()[0];
                   if (getter.getReturnType().equals(pinfo) == true)
                   {
                      setter = thisSetter;
                      break;
                   }
                }
             }
             String lowerName = getLowerPropertyName(name);
             
             // Merge the annotations between the getters and setters
             AnnotationValue[] annotations = getter.getAnnotations();
             AnnotationValue[] setterAnnotations = null;
             if (setter != null)
                setterAnnotations = setter.getAnnotations();
             if (annotations == null || annotations.length == 0)
                annotations = setterAnnotations;
             else if (setterAnnotations != null && setterAnnotations.length > 0)
             {
                HashSet<AnnotationValue> merged = new HashSet<AnnotationValue>();
                for (AnnotationValue annotation : annotations)
                   merged.add(annotation);
                for (AnnotationValue annotation : setterAnnotations)
                   merged.add(annotation);
                annotations = merged.toArray(new AnnotationValue[merged.size()]);
             }
             TypeInfo type = getPropertyType(getter, setter);
             properties.add(new AbstractPropertyInfo(lowerName, name, type, getter, setter, annotations));
          }
       }
       if (setters.isEmpty() == false)
       {
          for (Iterator<Map.Entry<String, List<MethodInfo>>> i = setters.entrySet().iterator(); i.hasNext();)
          {
             Map.Entry<String, List<MethodInfo>> entry = i.next();
             String name = entry.getKey();
             List<MethodInfo> setterList = entry.getValue();
             for(MethodInfo setter : setterList)
             {
                TypeInfo pinfo = setter.getParameterTypes()[0];
                String lowerName = getLowerPropertyName(name);
                AnnotationValue[] annotations = setter.getAnnotations();
                properties.add(new AbstractPropertyInfo(lowerName, name, pinfo, null, setter, annotations));
             }
          }
       }
       return properties;
    }
 
    /**
     * Determine the type of PropertyInfo.
     *
     * @param getter the getter
     * @param setter the setter
     * @return property type
     */
    protected TypeInfo getPropertyType(MethodInfo getter, MethodInfo setter)
    {
       if (getter == null)
          throw new IllegalArgumentException("Getter should not be null!");
       if (setter == null)
          return getter.getReturnType();
       // TODO - determine more restrictive type among getter and setter
       return getter.getReturnType();
    }
 
    /**
     * Get the properties for an annotation
     * 
     * @param methods the methods
     * @return the properties
     */
    protected Set<PropertyInfo> getAnnotationProperties(Set<MethodInfo> methods)
    {
       HashSet<PropertyInfo> properties = new HashSet<PropertyInfo>();
       if (methods != null && methods.isEmpty() == false)
       {
          for (MethodInfo method : methods)
          {
             TypeInfo returnType = method.getReturnType();
             TypeInfo[] parameters = method.getParameterTypes();
             if (parameters.length == 0 && PrimitiveInfo.VOID.equals(returnType) == false)
             {
                String name = method.getName();
                properties.add(new AbstractPropertyInfo(name, name, returnType, method, null, method.getAnnotations()));
             }
          }
       }
       return properties;
    }
    
    /**
     * Get the events
     * 
     * @param classInfo the class info
     * @return the events
     */
    protected Set<EventInfo> getEvents(ClassInfo classInfo)
    {
       return null;
    }
 }
