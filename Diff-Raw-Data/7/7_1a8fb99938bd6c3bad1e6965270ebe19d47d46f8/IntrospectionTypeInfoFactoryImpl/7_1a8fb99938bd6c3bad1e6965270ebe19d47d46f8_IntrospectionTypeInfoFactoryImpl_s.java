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
 package org.jboss.reflect.plugins.introspection;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.security.AccessController;
 import java.security.PrivilegedAction;
 
 import org.jboss.reflect.plugins.ClassInfoImpl;
 import org.jboss.reflect.plugins.ConstructorInfoImpl;
 import org.jboss.reflect.plugins.FieldInfoImpl;
 import org.jboss.reflect.plugins.InterfaceInfoImpl;
 import org.jboss.reflect.plugins.MethodInfoImpl;
 import org.jboss.reflect.spi.AnnotationValue;
 import org.jboss.reflect.spi.ClassInfo;
 import org.jboss.reflect.spi.InterfaceInfo;
 import org.jboss.reflect.spi.PrimitiveInfo;
 import org.jboss.reflect.spi.TypeInfo;
 import org.jboss.reflect.spi.TypeInfoFactory;
 import org.jboss.util.WeakClassCache;
 
 /**
  * An introspection type factory.
  *
  * FIXME: use lazy loading to avoid reading the entire class model
  * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
  */
 public class IntrospectionTypeInfoFactoryImpl extends WeakClassCache implements TypeInfoFactory
 {
    /**
     * Generate the type info for a class
     * 
     * @param clazz the class
     * @param info the class info
     */
    public void generateTypeInfo(Class clazz, ClassInfoImpl info)
    {
       // Everything is done lazily
    }
 
    public ClassInfo getSuperClass(Class clazz)
    {
       ClassInfoImpl superType = null;
       if (clazz.isInterface() == false)
       {
          Class superClazz = clazz.getSuperclass();
          if (superClazz != null)
             superType = (ClassInfoImpl) getTypeInfo(superClazz);
       }
       return superType;
    }
    
    public AnnotationValue[] getAnnotations(Object obj)
    {
       return new AnnotationValue[0];
    }
 
    /**
     * Get the constructors
     * 
     * @param clazz the class
     * @param declaring the declaring class
     * @return the constructor info
     */
    public ConstructorInfoImpl[] getConstructors(Class clazz, ClassInfo declaring)
    {
       ConstructorInfoImpl[] infos = null;
       if (clazz.isInterface() == false)
       {
          Constructor[] constructors = getDeclaredConstructors(clazz);
          if (constructors != null && constructors.length > 0)
          {
             infos = new ConstructorInfoImpl[constructors.length];
             for (int i = 0; i < constructors.length; ++i)
             {
                AnnotationValue[] annotations = getAnnotations(constructors[i]);
                infos[i] = new ConstructorInfoImpl(annotations, getTypeInfos(constructors[i].getParameterTypes()), getClassInfos(constructors[i].getExceptionTypes()), constructors[i].getModifiers(), (ClassInfo) getTypeInfo(constructors[i].getDeclaringClass()));
                infos[i].setConstructor(constructors[i]);
             }
          }
       }
       return infos;
    }
    
    /**
     * Get the fields
     * 
     * @param clazz the class
     * @param declaring the declaring class
     * @return the field info
     */
    public FieldInfoImpl[] getFields(Class clazz, ClassInfo declaring)
    {
       Field[] fields = getDeclaredFields(clazz);
       if (fields == null || fields.length == 0)
          return null;
       
       FieldInfoImpl[] infos = new FieldInfoImpl[fields.length];
       for (int i = 0; i < fields.length; ++i)
       {
          AnnotationValue[] annotations = getAnnotations(fields[i]);
          infos[i] = new FieldInfoImpl(annotations, fields[i].getName(), getTypeInfo(fields[i].getType()), fields[i].getModifiers(), (ClassInfo) getTypeInfo(fields[i].getDeclaringClass()));
          infos[i].setField(fields[i]);
       }
       return infos;
    }
    
    /**
     * Get the methods
     * 
     * @param clazz the class
     * @param declaring the declaring class
     * @return the method info
     */
    public MethodInfoImpl[] getMethods(Class clazz, ClassInfo declaring)
    {
       Method[] methods = getDeclaredMethods(clazz);
       if (methods == null || methods.length == 0)
          return null;
       
       MethodInfoImpl[] infos = new MethodInfoImpl[methods.length];
       for (int i = 0; i < methods.length; ++i)
       {
          AnnotationValue[] annotations = getAnnotations(methods[i]);
          infos[i] = new MethodInfoImpl(annotations, methods[i].getName(), getTypeInfo(methods[i].getReturnType()), getTypeInfos(methods[i].getParameterTypes()), getClassInfos(methods[i].getExceptionTypes()), methods[i].getModifiers(), (ClassInfo) getTypeInfo(methods[i].getDeclaringClass()));
          infos[i].setMethod(methods[i]);
       }
       return infos;
    }
    
    /**
     * Get the interfaces
     * 
     * @param clazz the class
     * @return the interface info
     */
    public InterfaceInfo[] getInterfaces(Class clazz)
    {
       Class[] interfaces = clazz.getInterfaces();
       if (interfaces == null || interfaces.length == 0)
          return null;
       
       InterfaceInfo[] infos = new InterfaceInfo[interfaces.length];
       for (int i = 0; i < interfaces.length; ++i)
          infos[i] = (InterfaceInfo) getTypeInfo(interfaces[i]);
       
       return infos;
    }
    
    /**
     * Get the type infos for some classes
     * 
     * @param classes the classes
     * @return the type infos
     */
    public TypeInfo[] getTypeInfos(Class[] classes)
    {
       if (classes == null || classes.length == 0)
          return null;
       
       TypeInfo[] result = new TypeInfo[classes.length];
       for (int i = 0; i < classes.length; ++i)
          result[i] = getTypeInfo(classes[i]);
       return result;
    }
    
    /**
     * Get the class infos for some classes
     * 
     * @param classes the classes
     * @return the class infos
     */
    public ClassInfo[] getClassInfos(Class[] classes)
    {
       if (classes == null || classes.length == 0)
          return null;
       
       ClassInfo[] result = new ClassInfo[classes.length];
       for (int i = 0; i < classes.length; ++i)
          result[i] = (ClassInfo) getTypeInfo(classes[i]);
       return result;
    }
 
    public TypeInfo getTypeInfo(Class clazz)
    {
       TypeInfo primitive = PrimitiveInfo.valueOf(clazz.getName());
       if (primitive != null)
          return primitive;
       
       return (TypeInfo) get(clazz);
    }
    
    public TypeInfo getTypeInfo(String name, ClassLoader cl) throws ClassNotFoundException
    {
       Class clazz = cl.loadClass(name);
       return getTypeInfo(clazz);
    }
    
    protected Object instantiate(Class clazz)
    {
       ClassInfoImpl result;
       if (clazz.isInterface())
          result = new InterfaceInfoImpl(clazz.getName());
       else
          result = new ClassInfoImpl(clazz.getName());
       result.setType(clazz);
       result.setTypeInfoFactory(this);
       return result;
    }
 
    protected void generate(Class clazz, Object result)
    {
       generateTypeInfo(clazz, (ClassInfoImpl) result);
    }
    
    protected Constructor[] getDeclaredConstructors(final Class clazz)
    {
       if (System.getSecurityManager() == null)
          return clazz.getDeclaredConstructors();
       else
       {
          PrivilegedAction action = new PrivilegedAction()
          {
             public Object run()
             {
                return clazz.getDeclaredConstructors();
             }
          };
          return (Constructor[]) AccessController.doPrivileged(action);
       }
    }
    
    protected Field[] getDeclaredFields(final Class clazz)
    {
       if (System.getSecurityManager() == null)
          return clazz.getDeclaredFields();
       else
       {
          PrivilegedAction action = new PrivilegedAction()
          {
             public Object run()
             {
                return clazz.getDeclaredFields();
             }
          };
          return (Field[]) AccessController.doPrivileged(action);
       }
    }
    
    protected Method[] getDeclaredMethods(final Class clazz)
    {
       if (System.getSecurityManager() == null)
          return clazz.getDeclaredMethods();
       else
       {
          PrivilegedAction action = new PrivilegedAction()
          {
             public Object run()
             {
                return clazz.getDeclaredMethods();
             }
          };
          return (Method[]) AccessController.doPrivileged(action);
       }
    }
 }
