 /*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
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
 package org.jboss.javabean.plugins.jaxb;
 
 import java.security.AccessController;
 import java.security.PrivilegedAction;
 import javax.xml.bind.annotation.XmlAttribute;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlNsForm;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlTransient;
 import javax.xml.bind.annotation.XmlType;
 
 import org.jboss.beans.info.spi.BeanInfo;
 import org.jboss.beans.info.spi.PropertyInfo;
 import org.jboss.config.plugins.property.PropertyConfiguration;
 import org.jboss.config.spi.Configuration;
 import org.jboss.joinpoint.plugins.Config;
 import org.jboss.reflect.spi.ConstructorInfo;
 import org.jboss.reflect.spi.MethodInfo;
 import org.jboss.reflect.spi.TypeInfo;
 import org.jboss.xb.annotations.JBossXmlSchema;
 import org.jboss.xb.annotations.JBossXmlType;
 import org.jboss.xb.spi.BeanAdapter;
 import org.jboss.xb.spi.BeanAdapterFactory;
import org.jboss.util.propertyeditor.PropertyEditors;
 
 /**
  * JavaBean.
  *
  * @author <a href="adrian@jboss.com">Adrian Brock</a>
  * @version $Revision: 1.1 $
  */
@JBossXmlSchema(namespace="test:javabean", elementFormDefault=XmlNsForm.QUALIFIED)
 @XmlRootElement(name="javabean")
 @XmlType(name="javabean")
 @JBossXmlType(beanAdapterBuilder=JavaBeanBuilder.class)
 public class JavaBean extends BeanAdapter
 {
    private static Configuration configuration;
 
    static
    {
       init();
    }
 
    static synchronized void init()
    {
       if(configuration == null)
       {
          configuration = AccessController.doPrivileged(new PrivilegedAction<Configuration>()
          {
             public Configuration run()
             {
                return new PropertyConfiguration(System.getProperties());
             }
          });
          PropertyEditors.init();
       }
    }
 
    private String className;
 
    private Constructor constructor;
 
    private Property[] properties;
 
    public JavaBean(BeanAdapterFactory beanAdapterFactory)
    {
       super(beanAdapterFactory);
    }
 
    public String getClassName()
    {
       return className;
    }
 
    @XmlAttribute(name="class")
    public void setClassName(String className)
    {
       this.className = className;
    }
 
    public Constructor getConstructor()
    {
       return constructor;
    }
 
    public void setConstructor(Constructor constructor)
    {
       this.constructor = constructor;
    }
 
    public Property[] getProperties()
    {
       return properties;
    }
 
    @XmlElement(name="property")
    public void setProperties(Property[] properties)
    {
       this.properties = properties;
    }
 
    @XmlTransient
    public Object getValue()
    {
       ClassLoader cl = Thread.currentThread().getContextClassLoader();
       try
       {
          Object result;
 
          BeanInfo beanInfo = null;
          if (className != null)
             beanInfo = configuration.getBeanInfo(className, cl);
 
          if (constructor != null)
          {
             String factoryMethod = constructor.getFactoryMethod();
             if (factoryMethod != null)
             {
                String factoryClass = constructor.getFactoryClass();
                BeanInfo factoryBeanInfo = beanInfo;
                if (factoryClass != null)
                   factoryBeanInfo = configuration.getBeanInfo(factoryClass, cl);
 
                if (factoryBeanInfo == null)
                   throw new IllegalArgumentException("Missing className or factoryClass: " + this);
 
                String[] signature = getSignature(constructor.getParameters());
                MethodInfo factory = Config.findMethodInfo(factoryBeanInfo.getClassInfo(), factoryMethod, signature, true, true);
                Object[] params = getParams(constructor.getParameters(), factory.getParameterTypes());
                result = factory.invoke(null, params);
             }
             else
             {
                if (beanInfo == null)
                   throw new IllegalArgumentException("Missing className: " + this);
 
                String[] signature = getSignature(constructor.getParameters());
                ConstructorInfo constructorInfo = Config.findConstructorInfo(beanInfo.getClassInfo(), signature);
                Object[] params = getParams(constructor.getParameters(), constructorInfo.getParameterTypes());
                result = constructorInfo.newInstance(params);
             }
          }
          else
          {
             if (beanInfo == null)
                throw new IllegalArgumentException("Missing className: " + this);
 
             result = beanInfo.newInstance();
          }
          if (beanInfo == null)
             beanInfo = configuration.getBeanInfo(result.getClass());
 
          if (properties != null)
          {
             for (Property property : properties)
             {
                Object value = property.getValue();
                if (value != null)
                {
                   PropertyInfo propertyInfo = beanInfo.getProperty(property.getName());
                   TypeInfo typeInfo = propertyInfo.getType();
                   String type = property.getType();
                   if (type != null)
                      typeInfo = configuration.getTypeInfo(type, cl);
                   value = typeInfo.convertValue(value, false);
                   propertyInfo.set(result, value);
                }
             }
          }
 
          return result;
       }
       catch (Throwable t)
       {
          throw new RuntimeException("Error constructing javabean", t);
       }
    }
 
    public Object get(PropertyInfo propertyInfo) throws Throwable
    {
       return propertyInfo.get(this);
    }
 
    public void set(PropertyInfo propertyInfo, Object child) throws Throwable
    {
       propertyInfo.set(this, child);
    }
 
    /**
     * Get parameter types.
     *
     * @param parameters the parameters
     * @return parameter types
     */
    protected String[] getSignature(Parameter[] parameters)
    {
       if (parameters == null)
          return new String[0];
       String[] signature = new String[parameters.length];
       for (int i = 0; i < signature.length; ++i)
          signature[i] = parameters[i].getType();
       return signature;
    }
 
    /**
     * Get parameter values.
     *
     * @param parameters the parameters
     * @param paramTypes the types
     * @return parameter values
     */
    protected Object[] getParams(Parameter[] parameters, TypeInfo[] paramTypes)
    {
       if (parameters == null)
          return new String[0];
       Object[] params = new Object[parameters.length];
       for (int i = 0; i < params.length; ++i)
       {
          Object value = parameters[i].getValue();
          try
          {
             params[i] = paramTypes[i].convertValue(value, false);
          }
          catch (Throwable t)
          {
             throw new RuntimeException("Error converting parameter #" + i + " value=" + value + " to type " + paramTypes[i]);
          }
       }
       return params;
    }
 }
