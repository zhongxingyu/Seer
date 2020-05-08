 /*
  * JBoss, Home of Professional Open Source
  * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
 package org.jboss.kernel.plugins.config.xml;
 
 import java.util.ArrayList;
 
 import org.jboss.beans.info.spi.BeanInfo;
 import org.jboss.beans.info.spi.PropertyInfo;
 import org.jboss.beans.metadata.plugins.AbstractBeanMetaData;
 import org.jboss.beans.metadata.plugins.AbstractConstructorMetaData;
 import org.jboss.beans.metadata.plugins.AbstractParameterMetaData;
 import org.jboss.beans.metadata.spi.ParameterMetaData;
 import org.jboss.joinpoint.spi.Joinpoint;
 import org.jboss.kernel.plugins.config.Configurator;
 import org.jboss.reflect.spi.ClassInfo;
 import org.jboss.reflect.spi.ConstructorInfo;
 import org.jboss.reflect.spi.TypeInfo;
 
 /**
  * Common classes and static methods
  * 
  * @author Scott.Stark@jboss.org
  * @version $Revision:$
  */
 public class Common
 {
    public static class Holder
    {
       private Object object;      
       
       public Holder()
       {
       }
 
       public Object getValue()
       {
          return object;
       }
       
       public void setValue(Object object)
       {
          this.object = object;
       }
    }
 
    public static class Ctor extends Holder
    {
       String className;
       boolean ctorWasDeclared;
       AbstractConstructorMetaData metaData = new AbstractConstructorMetaData();
       ArrayList<String> paramTypes = new ArrayList<String>();
       ArrayList<Object> argValues = new ArrayList<Object>();
 
       public Ctor(String className)
       {
          this.className = className;
       }
 
       public boolean isCtorWasDeclared()
       {
          return ctorWasDeclared;
       }
       public void setCtorWasDeclared(boolean ctorWasDeclared)
       {
          this.ctorWasDeclared = ctorWasDeclared;
       }
       public String getClassName()
       {
          return className;
       }
       
       public void addParam(PropertyInfo param, Object value)
       {
          paramTypes.add(param.getType().getName());
          argValues.add(value);
       }
       public String[] getParamTypes()
       {
          String[] types = new String[paramTypes.size()];
          paramTypes.toArray(types);
          return types;
       }
       public Object[] getArgs()
       {
          Object[] args = new Object[argValues.size()];
          argValues.toArray(args);
          return args;
       }
       public AbstractConstructorMetaData getMetaData()
       {
          return metaData;
       }
       public Object newInstance()
          throws Throwable
       {
          /*
          ConstructorInfo ctorInfo = getConstructor(this);
          Object[] args = getArgs();
          return ctorInfo.newInstance(args);
          */
          return Common.newInstance(this);
       }
    }
    public static class Property extends Holder
    {
       private String property;
       
       private String type;
       
       public Property()
       {
       }
       
       public String getProperty()
       {
          return property;
       }
       
       public void setProperty(String property)
       {
          this.property = property;
       }
       
       public String getType()
       {
          return type;
       }
       
       public void setType(String type)
       {
          this.type = type;
       }
    }
 
    static BeanInfo getBeanInfo(String name)
       throws Throwable
    {
       BeanInfo beanInfo = KernelConfigInit.config.getBeanInfo(name,
             Thread.currentThread().getContextClassLoader());
       return beanInfo;
    }
    static ConstructorInfo getConstructor(Ctor ctor)
       throws Throwable
    {
       BeanInfo beanInfo = getBeanInfo(ctor.getClassName());
       String[] paramTypes = ctor.getParamTypes();
       ClassInfo classInfo = beanInfo.getClassInfo();
       ConstructorInfo ctorInfo = Configurator.findConstructorInfo(classInfo, paramTypes);
       return ctorInfo;
    }
    static Object newInstance(Ctor ctor)
       throws Throwable
    {
      AbstractBeanMetaData bmd = new AbstractBeanMetaData(ctor.getClassName());
       AbstractConstructorMetaData cmd = ctor.getMetaData();
       bmd.setConstructor(cmd);
       Object[] args = ctor.getArgs();
       if( args.length > 0 )
       {
          String[] paramTypes = ctor.getParamTypes();
          ArrayList<ParameterMetaData> constructorParams = new ArrayList<ParameterMetaData>();
          for(int n = 0; n < args.length; n ++)
          {
             Object arg = args[n];
             AbstractParameterMetaData pmd = new AbstractParameterMetaData(arg);
             pmd.setType(paramTypes[n]);
             constructorParams.add(pmd);
          }
          cmd.setParameters(constructorParams);
       }
       BeanInfo info = getBeanInfo(ctor.getClassName());
       Joinpoint joinPoint = Configurator.getConstructorJoinPoint(KernelConfigInit.config,
             info, cmd, bmd);
       return joinPoint.dispatch();
    }
 
    static PropertyInfo getProperty(Object parent, String property, String type) throws Throwable
    {
       BeanInfo beanInfo = KernelConfigInit.config.getBeanInfo(parent.getClass());
       ClassLoader cl = Thread.currentThread().getContextClassLoader();
       return Configurator.resolveProperty(false, beanInfo, cl, property, type);
    }
    static PropertyInfo getProperty(String className, String property, String type)
       throws Throwable
    {
       ClassLoader cl = Thread.currentThread().getContextClassLoader();
       BeanInfo beanInfo = KernelConfigInit.config.getBeanInfo(className, cl);            
       return Configurator.resolveProperty(false, beanInfo, cl, property, type);
    }
 
    /**
     * Convert a value
     * 
     * @param info the property info
     * @param override the override class
     * @param value the value
     * @return the converted value
     * @throws Throwable for any error
     */
    static Object convertValue(PropertyInfo info, String override, Object value) throws Throwable
    {
       TypeInfo type = info.getType();
       if (override != null)
          type = KernelConfigInit.typeInfoFactory.getTypeInfo(override, null);
       return type.convertValue(value);
    }
 
 }
