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
 package org.jboss.reflect.plugins;
 
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.util.Arrays;
 
 import org.jboss.reflect.spi.AnnotationValue;
 import org.jboss.reflect.spi.ClassInfo;
 import org.jboss.reflect.spi.MethodInfo;
 import org.jboss.reflect.spi.ParameterInfo;
 import org.jboss.reflect.spi.TypeInfo;
 import org.jboss.util.JBossStringBuilder;
 
 /**
  * Method info
  *
  * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
  * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
  */
 public class MethodInfoImpl extends AnnotationHolder implements MethodInfo
 {
    /** serialVersionUID */
    private static final long serialVersionUID = 3257007670035756341L;
 
    /** The method name */
    protected String name;
    
    /** The method */
    protected Method method;
    
    /** The declaring class */
    protected ClassInfo declaringClass;
    
    /** The parameter types */
    protected TypeInfo[] parameterTypes;
    
    /** The parameters */
    protected ParameterInfo[] parameters;
    
    /** The exception types */
    protected ClassInfo[] exceptionTypes;
    
    /** The modifiers */
    protected int modifiers;
    
    /** The return type */
    protected TypeInfo returnType;
    
    /** The hash code */
    protected int hash;
 
    /**
     * Create a new method info
     */
    public MethodInfoImpl()
    {
    }
 
    /**
     * Create a new MethodInfo.
     * 
     * @param annotations the annotations
     * @param name the method name
     * @param returnType the return type
     * @param parameterTypes the parameter types
     * @param exceptionTypes the exception types
     * @param modifiers the modifiers
     * @param declaring the declaring class
     */
    public MethodInfoImpl(AnnotationValue[] annotations, String name, TypeInfo returnType, TypeInfo[] parameterTypes, ClassInfo[] exceptionTypes, int modifiers, ClassInfo declaring)
    {
       super(annotations);
       this.name = name;
       if (parameterTypes == null)
       {
          this.parameterTypes = MethodInfo.NO_PARAMS_TYPES;
          this.parameters = MethodInfo.NO_PARAMS;
       }
       else
       {
          this.parameterTypes = parameterTypes;
          this.parameters = new ParameterInfoImpl[parameterTypes.length];
          for (int i = 0; i < parameterTypes.length; ++i)
             this.parameters[i] = new ParameterInfoImpl(null, null, parameterTypes[i]);
       }
       if (exceptionTypes == null)
          this.exceptionTypes = MethodInfo.NO_EXCEPTIONS;
       else
          this.exceptionTypes = exceptionTypes;
       this.modifiers = modifiers;
       this.declaringClass = declaring;
       this.returnType = returnType;
       calculateHash();
    }
 
    /**
     * Create a new MethodInfo.
     * 
     * @param annotations the annotations
     * @param name the method name
     * @param returnType the return type
     * @param parameters the parameters
     * @param exceptionTypes the exception types
     * @param modifiers the modifiers
     * @param declaring the declaring class
     */
    public MethodInfoImpl(AnnotationValue[] annotations, String name, TypeInfo returnType, ParameterInfo[] parameters, ClassInfo[] exceptionTypes, int modifiers, ClassInfo declaring)
    {
       super(annotations);
       this.name = name;
       if (parameters == null || parameters.length == 0)
       {
          this.parameterTypes = MethodInfo.NO_PARAMS_TYPES;
          this.parameters = MethodInfo.NO_PARAMS;
       }
       else
       {
          this.parameters = parameters;
          this.parameterTypes = new TypeInfo[parameters.length];
          for (int i = 0; i < parameters.length; ++i)
             this.parameterTypes[i] = parameters[i].getParameterType();
       }
       if (exceptionTypes == null || exceptionTypes.length == 0)
          this.exceptionTypes = MethodInfo.NO_EXCEPTIONS;
       else
          this.exceptionTypes = exceptionTypes;
       this.modifiers = modifiers;
       this.declaringClass = declaring;
       this.returnType = returnType;
       calculateHash();
    }
 
    /**
     * Set the method
     * 
     * @param method the method
     */
    public void setMethod(Method method)
    {
       this.method = method;
    }
 
    public String getName()
    {
       return name;
    }
 
    public Method getMethod()
    {
       return method;
    }
    
    public ClassInfo getDeclaringClass()
    {
       return declaringClass;
    }
 
    public TypeInfo[] getParameterTypes()
    {
       return parameterTypes;
    }
 
    public ParameterInfo[] getParameters()
    {
       return parameters;
    }
 
    public ClassInfo[] getExceptionTypes()
    {
       return exceptionTypes;
    }
    
    public TypeInfo getReturnType()
    {
       return returnType;
    }
    
    public int getModifiers()
    {
       return modifiers;
    }
    
    public boolean isStatic()
    {
       return Modifier.isStatic(modifiers);
    }
    
    public boolean isPublic()
    {
       return Modifier.isPublic(modifiers);
    }
    
    protected void toString(JBossStringBuilder buffer)
    {
       buffer.append("name=").append(name);
       buffer.append(Arrays.asList(parameterTypes));
       buffer.append(" return=").append(returnType);
    }
   
   public void toShortString(JBossStringBuilder buffer)
   {
      buffer.append(name);
   }
 
    public boolean equals(Object obj)
    {
       if (this == obj) return true;
       if (obj == null || obj instanceof MethodInfoImpl == false)
          return false;
 
       final MethodInfoImpl other = (MethodInfoImpl) obj;
 
       if (declaringClass.equals(other.declaringClass) == false)
          return false;
       if (name.equals(other.name) == false)
          return false;
       if (Arrays.equals(parameterTypes, other.parameterTypes) == false)
          return false;
       if (returnType.equals(other.returnType) == false)
          return false;
 
       return true;
    }
 
    public int hashCode()
    {
       return hash;
    }
 
    /**
     * Calculate the hash code
     */
    protected void calculateHash()
    {
       int result;
       result = name.hashCode();
       result = 29 * result + declaringClass.hashCode();
       if (parameterTypes != null)
       {
          for (int i = 0; i < parameterTypes.length; i++)
             result = 29 * result + parameterTypes[i].hashCode();
       }
       hash = result;
    }
 }
