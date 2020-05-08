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
 package org.jboss.metadata.spi.signature;
 
 import java.util.Arrays;
 
 /**
  * Signature.
  * 
  * @author <a href="adrian@jboss.com">Adrian Brock</a>
  * @version $Revision: 1.1 $
  */
 public class Signature
 {
    /** No Name */
    public static final String NO_NAME = "?%NO_NAME%?";
 
    /** No Parameters */
    public static final String[] NO_PARAMETERS = new String[0];
 
    /** No Parameters Types */
    public static final Class[] NO_PARAMETER_TYPES = new Class[0];
    
    /** The name */
    private String name;
    
    /** The parameters */
    private String[] parameters;
 
    /** The parameter types */
    private Class[] parameterTypes;
    
    /** The cached hashcode */
    private transient int cachedHashCode = Integer.MIN_VALUE;
 
    /**
     * Convert classes to string
     * 
     * @param parameters the parameters as classes
     * @return the parameters as strings
     */
    private static String[] classesToStrings(Class... parameters)
    {
       if (parameters == null || parameters.length == 0)
          return NO_PARAMETERS;
 
       String[] result = new String[parameters.length];
       for (int i = 0; i < result.length; ++i)
       {
          if (parameters[i] == null)
             throw new IllegalArgumentException("Null class");
          result[i] = parameters[i].getName();
       }
       return result;
    }
 
    /**
     * Convert classes to string
     * 
     * @param clazz the reference class
     * @param parameters the parameters as strings
     * @return the parameters as classes
     */
   private static Class[] stringstoClasses(Class clazz, String... parameters)
    {
       if (clazz == null)
          throw new IllegalArgumentException("Null clazz");
       
       ClassLoader cl = clazz.getClassLoader();
       if (cl == null)
          cl = Class.class.getClassLoader();
       
       return stringsToClasses(cl, parameters);
    }
 
    /**
     * Convert classes to string
     * 
     * @param cl the classloader
     * @param parameters the parameters as strings
     * @return the parameters as classes
     */
    private static Class[] stringsToClasses(ClassLoader cl, String... parameters)
    {
       if (cl == null)
          throw new IllegalArgumentException("Null classloader");
 
       if (parameters == null || parameters.length == 0)
          return NO_PARAMETER_TYPES;
 
       Class[] result = new Class[parameters.length];
       for (int i = 0; i < result.length; ++i)
       {
          try
          {
             result[i] = cl.loadClass(parameters[i]);
          }
          catch (ClassNotFoundException e)
          {
             throw new IllegalStateException("Class not found: " + parameters[i], e);
          }
       }
       return result;
    }
    
    /**
     * Create a new Signature.
     */
    public Signature()
    {
       this(NO_NAME, NO_PARAMETER_TYPES, NO_PARAMETERS);
    }
    
    /**
     * Create a new Signature.
     * 
     * @param name the  name
     */
    public Signature(String name)
    {
       this(name, null, NO_PARAMETERS);
    }
 
    /**
     * Create a new Signature.
     * 
     * @param parameters the parameters
     */
    public Signature(String... parameters)
    {
       this(NO_NAME, null, parameters);
    }
 
    /**
     * Create a new Signature.
     * 
     * @param parameters the parameters
     */
    public Signature(Class... parameters)
    {
       this(NO_NAME, parameters, null);
    }
 
    /**
     * Create a new Signature.
     * 
     * @param name the  name
     * @param parameters the parameters
     */
    public Signature(String name, Class... parameters)
    {
       this(name, parameters, null);
    }
 
    /**
     * Create a new Signature.
     * 
     * @param name the  name
     * @param parameters the parameters
     */
    public Signature(String name, String... parameters)
    {
       this(name, null, parameters);
    }
 
    /**
     * Create a new Signature.
     * 
     * @param name the  name
     * @param parameterTypes the parameterTypes
     * @param parameters the parameters
     */
    private Signature(String name, Class[] parameterTypes, String[] parameters)
    {
       this.name = name;
       this.parameters = parameters;
       this.parameterTypes = parameterTypes; 
       if (name == null)
          this.name = NO_NAME;
       if (parameters == null)
          this.parameters = classesToStrings(parameterTypes);
       for (int i = 0; i < this.parameters.length; ++i)
       {
          if (this.parameters[i] == null)
             throw new IllegalArgumentException("Null parameter");
       }
    }
    
    /**
     * Get the name.
     * 
     * @return the name.
     */
    public String getName()
    {
       return name;
    }
 
    /**
     * Get the parameters.
     * 
     * @return the parameters.
     */
    public String[] getParameters()
    {
       return parameters;
    }
 
    /**
     * Get the parameter types.
     * 
     * @param clazz the reference class
     * @return the parameter types.
     */
    public Class[] getParametersTypes(Class clazz)
    {
       if (parameterTypes == null)
         return stringstoClasses(clazz, parameters);
       return parameterTypes;
    }
    
    public boolean equals(Object obj)
    {
       if (obj == this)
          return true;
       if (obj == null || obj instanceof Signature == false)
          return false;
       
       Signature other = (Signature) obj;
       
       if (getName().equals(other.getName()) == false)
          return false;
       
       if (parameters.length != other.parameters.length)
          return false;
       
       for (int i = 0; i < parameters.length; ++i)
       {
          if (parameters[i].equals(other.parameters[i]) == false)
             return false;
       }
       return true;
    }
    
    public int hashCode()
    {
       if (cachedHashCode == Integer.MIN_VALUE)
          cachedHashCode = toString().hashCode();
       return cachedHashCode;
    }
    
    public String toString()
    {
       StringBuilder builder = new StringBuilder();
       internalToString(builder);
       return builder.toString();
    }
 
    /**
     * Build the to String
     * 
     * @param builder the builder to use
     */
    protected void internalToString(StringBuilder builder)
    {
       if (name != null)
          builder.append(name);
       if (parameters != null)
          builder.append(Arrays.asList(parameters));
    }
 }
