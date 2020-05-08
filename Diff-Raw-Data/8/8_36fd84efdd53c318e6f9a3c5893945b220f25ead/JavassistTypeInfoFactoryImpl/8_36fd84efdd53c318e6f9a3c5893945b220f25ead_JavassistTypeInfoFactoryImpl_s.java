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
 package org.jboss.reflect.plugins.javassist;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Type;
 
 import javassist.ClassPool;
 import javassist.CtClass;
 import javassist.CtField;
 import javassist.CtMember;
 import javassist.CtMethod;
 import javassist.CtPrimitiveType;
 import javassist.NotFoundException;
 import org.jboss.reflect.plugins.AnnotationAttributeImpl;
 import org.jboss.reflect.plugins.AnnotationHelper;
 import org.jboss.reflect.plugins.AnnotationValueFactory;
 import org.jboss.reflect.plugins.AnnotationValueImpl;
 import org.jboss.reflect.plugins.EnumConstantInfoImpl;
 import org.jboss.reflect.spi.AnnotationInfo;
 import org.jboss.reflect.spi.AnnotationValue;
 import org.jboss.reflect.spi.NumberInfo;
 import org.jboss.reflect.spi.PrimitiveInfo;
 import org.jboss.reflect.spi.TypeInfo;
 import org.jboss.reflect.spi.TypeInfoFactory;
 import org.jboss.util.JBossStringBuilder;
 import org.jboss.util.collection.WeakClassCache;
 
 /**
  * A javassist type factory.
  *
  * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
  */
 public class JavassistTypeInfoFactoryImpl extends WeakClassCache implements TypeInfoFactory, AnnotationHelper
 {
    static final ClassPool pool = ClassPool.getDefault();
 
    static final AnnotationValue[] NO_ANNOTATIONS = new AnnotationValue[0];
    /**
     * Raise NoClassDefFoundError for javassist not found
     *
     * @param name the name
     * @param e the not found error
     * @return never
     * @throws NoClassDefFoundError always
     */
    public static NoClassDefFoundError raiseClassNotFound(String name, NotFoundException e) throws NoClassDefFoundError
    {
      NoClassDefFoundError ex = new NoClassDefFoundError("Unable to find class");
       if (e.getCause() != null)
          ex.initCause(e.getCause()); // Hide the javassist error
       throw ex;
    }
 
    /**
     * Raise NoClassDefFoundError for javassist not found
     *
     * @param name the name
     * @param e the not found error
     * @return never
     * @throws NoClassDefFoundError always
     */
    public static NoClassDefFoundError raiseClassNotFound(String name, ClassNotFoundException e) throws NoClassDefFoundError
    {
      NoClassDefFoundError ex = new NoClassDefFoundError("Unable to find class");
       ex.initCause(e);
       throw ex;
    }
 
    /**
     * Raise NoClassDefFoundError for javassist not found
     *
     * @param name the name
     * @param e the not found error
     * @return never
     * @throws NoClassDefFoundError always
     */
    public static NoClassDefFoundError raiseMethodNotFound(String name, NotFoundException e) throws NoClassDefFoundError
    {
       NoSuchMethodError ex = new NoSuchMethodError("Unable to find method " + name);
       if (e.getCause() != null)
          ex.initCause(e.getCause()); // Hide the javassist error
       throw ex;
    }
 
    /**
     * Raise NoClassDefFoundError for javassist not found
     *
     * @param name the name
     * @param e the not found error
     * @return never
     * @throws NoClassDefFoundError always
     */
    public static NoClassDefFoundError raiseFieldNotFound(String name, NotFoundException e) throws NoClassDefFoundError
    {
      NoSuchFieldError ex = new NoSuchFieldError("Unable to find field");
       if (e.getCause() != null)
          ex.initCause(e.getCause()); // Hide the javassist error
       throw ex;
    }
 
    @SuppressWarnings("unchecked")
    protected Object instantiate(Class clazz)
    {
       try
       {
          CtClass ctClass = getCtClass(clazz.getName());
 
          if (clazz.isArray())
          {
             TypeInfo componentType = getTypeInfo(clazz.getComponentType());
             return new JavassistArrayInfoImpl(this, ctClass, clazz, componentType);
          }
 
          if (ctClass.isAnnotation())
          {
             JavassistAnnotationInfo result = new JavassistAnnotationInfo(this, ctClass, clazz);
             CtMethod[] methods = ctClass.getDeclaredMethods();
             AnnotationAttributeImpl[] atttributes = new AnnotationAttributeImpl[methods.length];
             for (int i = 0 ; i < methods.length ; i++)
             {
                atttributes[i] = new AnnotationAttributeImpl(methods[i].getName(), getTypeInfo(methods[i].getReturnType()), null);
             }
             result.setAttributes(atttributes);
             return result;
 
          }
          else if (ctClass.isEnum())
          {
             JavassistEnumInfo enumInfo = new JavassistEnumInfo(this, ctClass, clazz);
             CtField[] fields = ctClass.getFields();
             EnumConstantInfoImpl[] constants = new EnumConstantInfoImpl[fields.length];
             int i = 0;
             for (CtField field : fields)
             {
                AnnotationValue[] annotations = getAnnotations(field);
                constants[i++] = new EnumConstantInfoImpl(field.getName(), enumInfo, annotations);
             }
             enumInfo.setEnumConstants(constants);
             return enumInfo;
          }
 
 
          return new JavassistTypeInfo(this, ctClass, clazz);
       }
       catch (NotFoundException e)
       {
          throw new RuntimeException(e);
       }
    }
 
    /**
     * Get the type info
     *
     * @param ctClass the ctClass
     * @return the typeinfo
     */
    protected TypeInfo getTypeInfo(CtClass ctClass)
    {
       try
       {
          String name = convertName(ctClass);
          return getTypeInfo(name, null);
       }
       catch (ClassNotFoundException e)
       {
          throw raiseClassNotFound(ctClass.getName(), e);
       }
    }
 
    /**
     * Convert name
     *
     * @param clazz the class
     * @return the converted name
     */
    protected String convertName(CtClass clazz)
    {
       CtClass temp = clazz;
       if (temp.isArray())
       {
          JBossStringBuilder buffer = new JBossStringBuilder();
          try
          {
             while (temp.isArray())
             {
                buffer.append('[');
                temp = temp.getComponentType();
             }
             if (temp.isPrimitive())
             {
                CtPrimitiveType primitive = (CtPrimitiveType) temp;
                buffer.append(Character.toString(primitive.getDescriptor()));
             }
             else
             {
                buffer.append('L');
                buffer.append(temp.getName());
                buffer.append(';');
             }
             return buffer.toString();
          }
          catch (NotFoundException e)
          {
             throw raiseClassNotFound(clazz.getName(), e);
          }
       }
       return clazz.getName();
    }
 
    /**
     * Get the CtClass
     *
     * @param name the name
     * @return the CtClass
     */
    protected CtClass getCtClass(String name)
    {
       try
       {
          return pool.get(name);
       }
       catch (NotFoundException e)
       {
          throw raiseClassNotFound(name, e);
       }
    }
 
    @SuppressWarnings("unchecked")
    protected void generate(Class clazz, Object result)
    {
       // Everything is done lazily
    }
 
    public TypeInfo getTypeInfo(Class<?> clazz)
    {
       if (clazz == null)
          throw new IllegalArgumentException("Null class");
 
       TypeInfo primitive = PrimitiveInfo.valueOf(clazz.getName());
       if (primitive != null)
          return primitive;
 
       NumberInfo number = NumberInfo.valueOf(clazz.getName());
       if (number != null)
       {
          if (number.isInitialized() == false)
          {
             number.setDelegate((TypeInfo) get(clazz));
          }
          return number;
       }
 
       return (TypeInfo) get(clazz);
    }
 
    public TypeInfo getTypeInfo(String name, ClassLoader cl) throws ClassNotFoundException
    {
       if (name == null)
          throw new IllegalArgumentException("Null class name");
       if (cl == null)
          cl = Thread.currentThread().getContextClassLoader();
 
       TypeInfo primitive = PrimitiveInfo.valueOf(name);
       if (primitive != null)
          return primitive;
 
       NumberInfo number = NumberInfo.valueOf(name);
       if (number != null)
       {
          if (number.isInitialized() == false)
          {
             number.setDelegate((TypeInfo) get(Class.forName(name, false, cl)));
          }
          return number;
       }
 
       Class<?> clazz = Class.forName(name, false, cl);
       return getTypeInfo(clazz);
    }
    
    public TypeInfo getTypeInfo(Type type)
    {
       if (type instanceof Class)
          return getTypeInfo((Class<?>) type);
 
       // TODO JBMICROCONT-129 getTypeInfo + NumberInfo
       throw new org.jboss.util.NotImplementedException("getTypeInfo");
    }
 
    public AnnotationValue[] getAnnotations(Object obj)
    {
       try
       {
          Object[] annotations;
          if (obj instanceof CtMember)
          {
             annotations = ((CtMember)obj).getAvailableAnnotations();
          }
          else if (obj instanceof CtClass)
          {
             annotations = ((CtClass)obj).getAvailableAnnotations();
          }
          else
          {
             throw new RuntimeException("Attempt was made to read annotations from unsupported type " + obj.getClass().getName() + ": " + obj);
          }
 
          if (annotations.length == 0)
          {
             return NO_ANNOTATIONS;
          }
 
          AnnotationValue[] annotationValues = new AnnotationValueImpl[annotations.length];
          for (int i = 0 ; i < annotations.length ; i++)
          {
             Class<?> clazz = ((Annotation)annotations[i]).annotationType();
             
             AnnotationInfo info = (AnnotationInfo)getTypeInfo(clazz);
             annotationValues[i] = AnnotationValueFactory.createAnnotationValue(this, this, info, annotations[i]);
          }
          return annotationValues;
       }
 //      catch (ClassNotFoundException e)
 //      {
 //         throw new RuntimeException(e);
 //      }
       catch (Throwable t)
       {
          throw new RuntimeException(t);
       }
    }
 
    public AnnotationValue createAnnotationValue(AnnotationInfo info, Object ann)
    {
       return AnnotationValueFactory.createAnnotationValue(this, this, info, ann);
    }
    
 }
