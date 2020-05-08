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
 package org.jboss.aop.annotation;
 
 import javassist.ClassPool;
 import javassist.CtClass;
 import javassist.CtConstructor;
 import javassist.CtField;
 import javassist.CtMethod;
 import javassist.NotFoundException;
 import javassist.bytecode.AnnotationsAttribute;
 import javassist.bytecode.ClassFile;
 import javassist.bytecode.FieldInfo;
 import javassist.bytecode.MethodInfo;
 
 import org.jboss.aop.AspectManager;
 import org.jboss.annotation.factory.javassist.AnnotationProxy;
 import org.jboss.ant.taskdefs.server.StartServerTask;
 import org.jboss.aop.util.ReflectToJavassist;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.lang.reflect.Proxy;
 
 /**
  * This base class is portable between JDK 1.4 and JDK 1.5
  * AnnotationElement will be different for JDK 1.4 and JDK 1.5
  *
  * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
  * @version $Revision$
  */
 public class PortableAnnotationElement
 {
    private static volatile boolean closingDownManager;
    
    public static void setClosingDownManager(boolean closing)
    {
       closingDownManager = closing; 
    }
    
    public static boolean isInvisibleAnnotationPresent(Field field, String annotation) throws Exception
    {
       if (closingDownManager)
       {
          return false;
       }
       if(includeInvisibleAnnotation(annotation))
       {
          CtField ctMethod = ReflectToJavassist.fieldToJavassist(field);
          return AnnotationElement.isInvisibleAnnotationPresent(ctMethod, annotation);
       }
       else
          return false;
    }
 
    public static boolean isInvisibleAnnotationPresent(CtField field, String annotation)
    {
       if (closingDownManager)
       {
          return false;
       }
       if(includeInvisibleAnnotation(annotation))
       {
          FieldInfo mi = field.getFieldInfo2();
 
          AnnotationsAttribute invisible = (AnnotationsAttribute) mi.getAttribute(AnnotationsAttribute.invisibleTag);
          if (invisible == null) return false;
 
          return invisible.getAnnotation(annotation) != null;
       }
       else
          return false;
    }
 
    public static boolean isVisibleAnnotationPresent(CtField field, String annotation)
    {
       if (closingDownManager)
       {
          return false;
       }
       FieldInfo mi = field.getFieldInfo2();
 
       AnnotationsAttribute visible = (AnnotationsAttribute) mi.getAttribute(AnnotationsAttribute.visibleTag);
       if (visible == null) return false;
 
       return visible.getAnnotation(annotation) != null;
    }
 
    public static boolean isAnyAnnotationPresent(CtField ctField, String annotation)
    {
       if (closingDownManager)
       {
          return false;
       }
       FieldInfo mi = ctField.getFieldInfo2();
 
       AnnotationsAttribute visible = (AnnotationsAttribute) mi.getAttribute(AnnotationsAttribute.visibleTag);
       if (visible != null)
       {
          if (visible.getAnnotation(annotation) != null) return true;
       }
 
       if(includeInvisibleAnnotation(annotation))
       {
          AnnotationsAttribute invisible = (AnnotationsAttribute) mi.getAttribute(AnnotationsAttribute.invisibleTag);
          if (invisible != null)
          {
             if (invisible.getAnnotation(annotation) != null) return true;
          }
          return false;
       }
       else
          return false;
    }
 
    public static boolean isInvisibleAnnotationPresent(Method method, String annotation) throws Exception
    {
       if (closingDownManager)
       {
          return false;
       }
       if(includeInvisibleAnnotation(annotation))
       {
          CtMethod ctMethod = ReflectToJavassist.methodToJavassist(method);
          if (ctMethod == null) return false;
          MethodInfo mi = ctMethod.getMethodInfo2();
 
 
          AnnotationsAttribute invisible = (AnnotationsAttribute) mi.getAttribute(AnnotationsAttribute.invisibleTag);
          if (invisible == null) return false;
 
          return invisible.getAnnotation(annotation) != null;
       }
       else
          return false;
    }
 
    public static boolean isAnyAnnotationPresent(Field field, String annotation) throws Exception
    {
       if (closingDownManager)
       {
          return false;
       }
       if(AnnotationElement.isVisibleAnnotationPresent(field, annotation))
          return true;
       if(includeInvisibleAnnotation(annotation))
       {
          CtField ctField = ReflectToJavassist.fieldToJavassist(field);
          return AnnotationElement.isAnyAnnotationPresent(ctField, annotation);
       }
       else
          return false;
 
    }
 
    public static boolean isAnyAnnotationPresent(Method method, String annotation) throws Exception
    {
       if (closingDownManager)
       {
          return false;
       }
       if(AnnotationElement.isVisibleAnnotationPresent(method, annotation))
          return true;
       if(includeInvisibleAnnotation(annotation))
       {
          CtMethod ctMethod = ReflectToJavassist.methodToJavassist(method);
          if (ctMethod == null) return false;
          boolean present = AnnotationElement.isAnyAnnotationPresent(ctMethod, annotation);
          return present;
       }
       else
          return false;
    }
 
    public static boolean isAnyAnnotationPresent(CtMethod ctMethod, String annotation)
    {
       if (closingDownManager)
       {
          return false;
       }
       MethodInfo mi = ctMethod.getMethodInfo2();
 
       AnnotationsAttribute visible = (AnnotationsAttribute) mi.getAttribute(AnnotationsAttribute.visibleTag);
       if (visible != null)
       {
          if (visible.getAnnotation(annotation) != null) return true;
       }
 
       if(includeInvisibleAnnotation(annotation))
       {
          AnnotationsAttribute invisible = (AnnotationsAttribute) mi.getAttribute(AnnotationsAttribute.invisibleTag);
          if (invisible != null)
          {
             if (invisible.getAnnotation(annotation) != null) return true;
          }
          return false;
       }
       else
          return false;
       
    }
 
    public static boolean isInvisibleAnnotationPresent(Constructor con, String annotation) throws Exception
    {
       if (closingDownManager)
       {
          return false;
       }
       if(includeInvisibleAnnotation(annotation))
       {
          CtConstructor ctMethod = ReflectToJavassist.constructorToJavassist(con);
          return AnnotationElement.isInvisibleAnnotationPresent(ctMethod, annotation);
       }
       else
          return false;
    }
 
    public static boolean isInvisibleAnnotationPresent(CtConstructor ctMethod, String annotation)
    {
       if (closingDownManager)
       {
          return false;
       }
       if(includeInvisibleAnnotation(annotation))
       {
          MethodInfo mi = ctMethod.getMethodInfo2();
 
          AnnotationsAttribute invisible = (AnnotationsAttribute) mi.getAttribute(AnnotationsAttribute.invisibleTag);
          if (invisible == null) return false;
 
          return invisible.getAnnotation(annotation) != null;
       }
       else
          return false;
    }
 
    public static boolean isVisibleAnnotationPresent(CtConstructor ctMethod, String annotation)
    {
       if (closingDownManager)
       {
          return false;
       }
       MethodInfo mi = ctMethod.getMethodInfo2();
 
 
       AnnotationsAttribute visible = (AnnotationsAttribute) mi.getAttribute(AnnotationsAttribute.visibleTag);
       if (visible == null) return false;
 
       return visible.getAnnotation(annotation) != null;
    }
 
    public static boolean isAnyAnnotationPresent(Constructor con, String annotation) throws Exception
    {
       if (closingDownManager)
       {
          return false;
       }
       if(AnnotationElement.isVisibleAnnotationPresent(con, annotation))
          return true;
       if(includeInvisibleAnnotation(annotation))
       {
          CtConstructor ctMethod = ReflectToJavassist.constructorToJavassist(con);
          return AnnotationElement.isAnyAnnotationPresent(ctMethod, annotation);
       }
       else
          return false;
    }
 
    public static boolean isAnyAnnotationPresent(CtConstructor ctMethod, String annotation)
    {
       if (closingDownManager)
       {
          return false;
       }
       MethodInfo mi = ctMethod.getMethodInfo2();
 
       AnnotationsAttribute visible = (AnnotationsAttribute) mi.getAttribute(AnnotationsAttribute.visibleTag);
       if (visible != null)
       {
          if (visible.getAnnotation(annotation) != null) return true;
       }
 
       if(includeInvisibleAnnotation(annotation))
       {
          AnnotationsAttribute invisible = (AnnotationsAttribute) mi.getAttribute(AnnotationsAttribute.invisibleTag);
          if (invisible != null)
          {
             if (invisible.getAnnotation(annotation) != null) return true;
          }
          return false;
       }
       else
          return false;
    }
 
    public static boolean isInvisibleAnnotationPresent(Class clazz, String annotation) throws Exception
    {
       if (closingDownManager)
       {
          return false;
       }
       if(includeInvisibleAnnotation(annotation))
       {
          if (clazz == Void.TYPE) return false;
          ClassFile cf = AnnotationElement.getClassFile(clazz);
 
          AnnotationsAttribute invisible = (AnnotationsAttribute) cf.getAttribute(AnnotationsAttribute.invisibleTag);
          if (invisible == null) return false;
 
          return invisible.getAnnotation(annotation) != null;
       }
       else
          return false;
    }
 
    public static boolean isAnyAnnotationPresent(CtClass clazz, String annotation) throws Exception
    {
       if (closingDownManager)
       {
          return false;
       }
       try
       {
          if (clazz == CtClass.voidType) return false;
          
          CtClass theClass = clazz;
          while(theClass.isArray())
          {
             theClass = theClass.getComponentType();
          }
          
          if (theClass.isPrimitive()) return false;
          
          ClassFile cf = theClass.getClassFile2();
          AnnotationsAttribute visible = (AnnotationsAttribute) cf.getAttribute(AnnotationsAttribute.visibleTag);
          if (visible != null)
          {
             if (visible.getAnnotation(annotation) != null) 
                return true;
          }
 
          if(includeInvisibleAnnotation(annotation))
          {
             AnnotationsAttribute invisible = (AnnotationsAttribute) cf.getAttribute(AnnotationsAttribute.invisibleTag);
             if (invisible != null)
             {
                if (invisible.getAnnotation(annotation) != null) return true;
             }
             return false;
          }
          else
             return false;
         
       }
       catch (RuntimeException e)
       {
          String name = (clazz != null) ? clazz.getName() : null;
          throw new RuntimeException("Error looking for " + annotation + " in " + clazz.getName(), e);
       }
    }
 
    public static boolean isAnyAnnotationPresent(Class clazz, String annotation) throws Exception
    {
       if (closingDownManager)
       {
          return false;
       }
       if (clazz == Void.TYPE) return false;
       if(AnnotationElement.isVisibleAnnotationPresent(clazz, annotation))
          return true;
       
 //      ClassFile cf = AnnotationElement.getClassFile(clazz);
 //      AnnotationsAttribute visible = (AnnotationsAttribute) cf.getAttribute(AnnotationsAttribute.visibleTag);
 //      if (visible != null)
 //      {
 //         if (visible.getAnnotation(annotation) != null) return true;
 //      }
 
       if(includeInvisibleAnnotation(annotation))
       {
          ClassFile cf = AnnotationElement.getClassFile(clazz);
          AnnotationsAttribute invisible = (AnnotationsAttribute) cf.getAttribute(AnnotationsAttribute.invisibleTag);
          if (invisible != null)
          {
             if (invisible.getAnnotation(annotation) != null) return true;
          }
          return false;
       }
       else
          return false;
    }
 
    protected static ClassFile getClassFile(Class clazz) throws NotFoundException
    {
       ClassPool pool = AspectManager.instance().findClassPool(clazz.getClassLoader());
       CtClass ct = pool.get(clazz.getName());
       ClassFile cf = ct.getClassFile2();
       return cf;
    }
 
    protected static Object create(AnnotationsAttribute group, Class annotation) throws Exception
    {
       if (group == null) return null;
       javassist.bytecode.annotation.Annotation info = group.getAnnotation(annotation.getName());
       if (info == null) return null;
       return AnnotationProxy.createProxy(info, annotation);
    }
 
    public static Object getInvisibleAnnotation(Method method, Class annotation)
    {
       if (closingDownManager)
       {
          return false;
       }
       
       if(includeInvisibleAnnotation(annotation))
       {
          try
          {
             CtMethod ctMethod = ReflectToJavassist.methodToJavassist(method);
             if (ctMethod == null)
             {
                return null;
             }
             MethodInfo mi = ctMethod.getMethodInfo2();
 
             AnnotationsAttribute invisible = (AnnotationsAttribute) mi.getAttribute(AnnotationsAttribute.invisibleTag);
             if (invisible == null) return null;
 
             return create(invisible, annotation);
          }
          catch (Exception e)
          {
             throw new RuntimeException(e);  //To change body of catch statement use Options | File Templates.
          }
       }
       else
          return false;
    }
 
    public static Object getInvisibleAnnotation(Constructor con, Class annotation)
    {
       if (closingDownManager)
       {
          return false;
       }
       if(includeInvisibleAnnotation(annotation))
       {
          try
          {
             CtConstructor ctMethod = ReflectToJavassist.constructorToJavassist(con);
             MethodInfo mi = ctMethod.getMethodInfo2();
 
 
             AnnotationsAttribute invisible = (AnnotationsAttribute) mi.getAttribute(AnnotationsAttribute.invisibleTag);
             if (invisible == null) return null;
 
             return create(invisible, annotation);
          }
          catch (Exception e)
          {
             throw new RuntimeException(e);  //To change body of catch statement use Options | File Templates.
          }
       }
       else
          return false;
    }
 
    public static Object getInvisibleAnnotation(Field field, Class annotation)
    {
       if (closingDownManager)
       {
          return false;
       }
       try
       {
          CtField ctField = ReflectToJavassist.fieldToJavassist(field);
          FieldInfo mi = ctField.getFieldInfo2();
 
 
          AnnotationsAttribute invisible = (AnnotationsAttribute) mi.getAttribute(AnnotationsAttribute.invisibleTag);
          if (invisible == null) return null;
 
          return create(invisible, annotation);
       }
       catch (Exception e)
       {
          throw new RuntimeException(e);  //To change body of catch statement use Options | File Templates.
       }
    }
 
    public static Object getInvisibleAnnotation(Class clazz, Class annotation)
    {
       if (closingDownManager)
       {
          return false;
       }
       try
       {
          if (clazz == Void.TYPE) return null;
          ClassFile cf = getClassFile(clazz);
 
          AnnotationsAttribute invisible = (AnnotationsAttribute) cf.getAttribute(AnnotationsAttribute.invisibleTag);
          if (invisible == null) return null;
 
          return create(invisible, annotation);
       }
       catch (Exception e)
       {
          throw new RuntimeException(e);  //To change body of catch statement use Options | File Templates.
       }
    }
 
    /**
     * If invisble or visible annotation is present for method, then return it
     *
     * @param method
     * @param annotation
     * @return
     */
    public static Object getAnyAnnotation(Method method, Class annotation)
    {
       Object rtn = AnnotationElement.getVisibleAnnotation(method, annotation);
       if (rtn != null) return rtn;
       if(includeInvisibleAnnotation(annotation))
          return getInvisibleAnnotation(method, annotation);
       else
          return null;
    }
 
    /**
     * If con has a invisible or visible annotation return it
     *
     * @param con
     * @param annotation
     * @return
     */
    public static Object getAnyAnnotation(Constructor con, Class annotation)
    {
       Object rtn = AnnotationElement.getVisibleAnnotation(con, annotation);
       if (rtn != null) return rtn;
       if(includeInvisibleAnnotation(annotation))
          return getInvisibleAnnotation(con, annotation);
       else
          return null;
    }
 
    public static Object getAnyAnnotation(Field field, Class annotation)
    {
       Object rtn = AnnotationElement.getVisibleAnnotation(field, annotation);
       if (rtn != null) return rtn;
       if(includeInvisibleAnnotation(annotation))
          return getInvisibleAnnotation(field, annotation);
       else
          return null;
    }
 
    public static Object getAnyAnnotation(Class clazz, Class annotation)
    {
       if (clazz == Void.TYPE) return null;
       Object rtn = AnnotationElement.getVisibleAnnotation(clazz, annotation);
       if (rtn != null) return rtn;
       if(includeInvisibleAnnotation(annotation))
          return getInvisibleAnnotation(clazz, annotation);
       else
          return null;
    }
 
    public static boolean isAnyAnnotationPresent(Field field, Class annotation) throws Exception
    {
       if (AnnotationElement.isVisibleAnnotationPresent(field, annotation)) 
          return true;
       if (closingDownManager)
       {
          return false;
       }
       if(includeInvisibleAnnotation(annotation))
       {
          CtField ctMethod = ReflectToJavassist.fieldToJavassist(field);
          return isInvisibleAnnotationPresent(ctMethod, annotation.getName());
       }
       else
          return false;
    }
 
    public static boolean isAnyAnnotationPresent(Class clazz, Class annotation) throws Exception
    {
       if (clazz == Void.TYPE) return false;
       if (AnnotationElement.isVisibleAnnotationPresent(clazz, annotation)) 
          return true;
       if (closingDownManager)
       {
          return false;
       }
       if(includeInvisibleAnnotation(annotation))
       {
          ClassFile cf = getClassFile(clazz);
 
          AnnotationsAttribute invisible = (AnnotationsAttribute) cf.getAttribute(AnnotationsAttribute.invisibleTag);
          if (invisible == null) return false;
 
          return invisible.getAnnotation(annotation.getName()) != null;
       }
       else
          return false;
    }
 
    public static boolean isAnyAnnotationPresent(Constructor con, Class annotation) throws Exception
    {
       if (AnnotationElement.isVisibleAnnotationPresent(con, annotation)) 
          return true;
       if (closingDownManager)
       {
          return false;
       }
       if(includeInvisibleAnnotation(annotation))
       {
          CtConstructor ctMethod = ReflectToJavassist.constructorToJavassist(con);
          return isVisibleAnnotationPresent(ctMethod, annotation.getName());
       }
       else
          return false;
    }
 
    public static boolean isAnyAnnotationPresent(Method method, Class annotation) throws Exception
    {
       if (AnnotationElement.isVisibleAnnotationPresent(method, annotation)) 
          return true;
       if (closingDownManager)
       {
          return false;
       }
       if(includeInvisibleAnnotation(annotation))
       {
          CtMethod ctMethod = ReflectToJavassist.methodToJavassist(method);
          if (ctMethod == null) return false;
          MethodInfo mi = ctMethod.getMethodInfo2();
 
          AnnotationsAttribute invisible = (AnnotationsAttribute) mi.getAttribute(AnnotationsAttribute.invisibleTag);
          if (invisible == null) return false;
 
          return invisible.getAnnotation(annotation.getName()) != null;
       }
       else
          return false;
    }
    
    protected static boolean includeInvisibleAnnotation(Class annotation)
    {
       return  includeInvisibleAnnotation(annotation.getPackage().getName());
    }
    
    protected static boolean includeInvisibleAnnotation(String annotation)
    {
       for(String includedAnnotation : AspectManager.instance().getIncludedInvisibleAnnotations())
       {
         if(includedAnnotation.equals("*") || includedAnnotation.startsWith(annotation))
          {
//            System.err.println("IGNORING ANNOTATION: "+annotation);
             return true;
          }
       }
       return false;
    }
 }
