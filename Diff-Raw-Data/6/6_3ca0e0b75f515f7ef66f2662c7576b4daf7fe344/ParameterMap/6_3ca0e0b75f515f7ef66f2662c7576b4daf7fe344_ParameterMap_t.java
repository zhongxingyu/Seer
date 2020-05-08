 /******************************************************************************
  * JBoss, a division of Red Hat                                               *
  * Copyright 2009, Red Hat Middleware, LLC, and individual                    *
  * contributors as indicated by the @authors tag. See the                     *
  * copyright.txt in the distribution for a full listing of                    *
  * individual contributors.                                                   *
  *                                                                            *
  * This is free software; you can redistribute it and/or modify it            *
  * under the terms of the GNU Lesser General Public License as                *
  * published by the Free Software Foundation; either version 2.1 of           *
  * the License, or (at your option) any later version.                        *
  *                                                                            *
  * This software is distributed in the hope that it will be useful,           *
  * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU           *
  * Lesser General Public License for more details.                            *
  *                                                                            *
  * You should have received a copy of the GNU Lesser General Public           *
  * License along with this software; if not, write to the Free                *
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA         *
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.                   *
  ******************************************************************************/
 package org.gatein.common.util;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 /**
  * A decorator that enforce the map content to be <String,String[]>. It also provides capabilities for making a copy of
  * the value either on a read or on a write.
  *
  * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
  * @version $Revision: 6671 $
  */
 @SuppressWarnings("serial")
 public final class ParameterMap extends AbstractTypedMap<String, String[], String, String[]> implements Serializable
 {
 
    /** Just a string to string converter. */
    private static final KeyConverter keyConv = new KeyConverter();
 
    /**
     * Copy the parameter map.
     *
     * @param map the parameter map to copy
     * @return a parameter map initialized from the argument map
     * @throws NullPointerException     if the map contains a null key or a null value
     * @throws IllegalArgumentException if the map is null or it contains a value with a zero length array or a null
     *                                  element in the array
     * @throws ClassCastException       if the map contains a key that is not a string or a value that is not a string
     *                                  array
     */
    public static ParameterMap clone(Map<String, String[]> map) throws NullPointerException, ClassCastException, IllegalArgumentException
    {
       return clone(map, AccessMode.A);
    }
 
    /**
     * Copy the parameter map.
     *
     * @param map        the parameter map to copy
     * @param accessMode the access mode
     * @return a parameter map initialized from the argument map
     * @throws NullPointerException     if the map contains a null key or a null value
     * @throws IllegalArgumentException if the map is null or it contains a value with a zero length array or a null
     *                                  element in the array
     * @throws ClassCastException       if the map contains a key that is not a string or a value that is not a string
     *                                  array
     */
    public static ParameterMap clone(Map<String, String[]> map, AccessMode accessMode) throws NullPointerException, ClassCastException, IllegalArgumentException
    {
       ParameterValidation.throwIllegalArgExceptionIfNull(map, "map to be cloned");
 
       //
       HashMap<String, String[]> delegate = new HashMap<String, String[]>(map);
       return new ParameterMap(delegate, accessMode);
    }
 
    /**
     * Safely wrap the map as a portlet parameters object. If the map is already a parameter map object, just return that
     * object otherwise return a wrapper around the map.
     *
     * @param map the map
     * @return the portlet parameters
     */
    public static ParameterMap wrap(Map<String, String[]> map)
    {
       return wrap(map, AccessMode.A);
    }
 
    /**
     * Safely wrap the map as a portlet parameters object. If the map is already a parameter map object, just return that
     * object with the new access mode otherwise return a wrapper around the map.
     *
     * @param map        the map
     * @param accessMode the access mode
     * @return the portlet parameters
     */
    public static ParameterMap wrap(Map<String, String[]> map, AccessMode accessMode)
    {
       if (map instanceof ParameterMap)
       {
          return new ParameterMap((ParameterMap)map, accessMode);
       }
       else
       {
          return new ParameterMap(map, accessMode);
       }
    }
 
    /** . */
   private transient AccessMode accessMode;
 
    /** . */
    private Map<String, String[]> delegate;
 
    public ParameterMap()
    {
       this(AccessMode.A);
    }
 
    public ParameterMap(AccessMode accessMode)
    {
       this(new HashMap<String, String[]>(), accessMode);
    }
 
    public ParameterMap(Map<String, String[]> delegate)
    {
       this(delegate, AccessMode.A);
    }
 
    public ParameterMap(Map<String, String[]> delegate, AccessMode accessMode)
    {
       ParameterValidation.throwIllegalArgExceptionIfNull(delegate, "delegate");
       ParameterValidation.throwIllegalArgExceptionIfNull(accessMode, "access");
 
       //
       this.delegate = delegate;
       this.accessMode = accessMode;
    }
 
    /**
     * Exposed through the <code>as(AccessMode accessMode)</code> method.
     *
     * @param that       the map to rewrap
     * @param accessMode the new access mode
     */
    private ParameterMap(ParameterMap that, AccessMode accessMode)
    {
       this(that != null ? that.delegate : null, accessMode);
    }
 
    public AccessMode getAccessMode()
    {
       return accessMode;
    }
 
    public ParameterMap as(AccessMode accessMode)
    {
       return new ParameterMap(this, accessMode);
    }
 
    public Converter<String, String> getKeyConverter()
    {
       return keyConv;
    }
 
    public Converter<String[], String[]> getValueConverter()
    {
       return accessMode.converter;
    }
 
    protected Map<String, String[]> getDelegate()
    {
       return delegate;
    }
 
    /**
     * Return the parameter value or null if it does not exist.
     *
     * @param name the parameter name
     * @return the parameter value or null if it does not exist
     * @throws IllegalArgumentException if the name is null
     */
    public String getValue(String name) throws IllegalArgumentException
    {
       ParameterValidation.throwIllegalArgExceptionIfNull(name, "parameter name");
 
       // Access delegate directly to avoid copy on read when it is enabled
       String[] value = delegate.get(name);
 
       //
       return value == null ? null : value[0];
    }
 
    /**
     * Return the parameter values or null if it does not exist.
     *
     * @param name the value to get
     * @return the parameter values
     * @throws IllegalArgumentException if the name is null
     */
    public String[] getValues(String name) throws IllegalArgumentException
    {
       ParameterValidation.throwIllegalArgExceptionIfNull(name, "parameter name");
 
       //
       return get(name);
    }
 
    /**
     * Set the a parameter value.
     *
     * @param name  the parameter name
     * @param value the parameter value
     * @throws IllegalArgumentException if the name or the value is null
     */
    public void setValue(String name, String value) throws IllegalArgumentException
    {
       ParameterValidation.throwIllegalArgExceptionIfNull(name, "parameter name");
       ParameterValidation.throwIllegalArgExceptionIfNull(value, "parrameter value");
 
       // Access delegate directly to avoid copy on read write it is enabled
       delegate.put(name, new String[]{value});
    }
 
    /**
     * Set the parameter values. This method does not make a defensive copy of the values.
     *
     * @param name   the parameter name
     * @param values the parameter values
     * @throws NullPointerException     if the name or the value is null
     * @throws IllegalArgumentException if the name is null or the values is null or the values length is zero or
     *                                  contains a null element
     */
    public void setValues(String name, String[] values) throws IllegalArgumentException
    {
       ParameterValidation.throwIllegalArgExceptionIfNull(name, "parameter name");
       ParameterValidation.throwIllegalArgExceptionIfNull(values, "parameter values");
 
       //
       put(name, values);
    }
 
    /**
     * Append the content of the argument map to that map. If both maps contains an entry sharing the same key, then the
     * string arrays or the two entries will be concatenated into a single array. Each entry present on the argument map
     * and not in the current map will be kept as is. The argument validation is performed before the state is updated.
     *
     * @param params the parameters to appends
     * @throws NullPointerException     if the map contains a null key or a null value
     * @throws IllegalArgumentException if the map is null or it contains a value with a zero length array or a null
     *                                  element in the array
     * @throws ClassCastException       if the map contains a key that is not a string or a value that is not a string
     *                                  array
     */
    public void append(Map<String, String[]> params) throws ClassCastException, NullPointerException, IllegalArgumentException
    {
       // Clone to have an atomic operation
       params = new HashMap<String, String[]>(params);
 
       //
       for (Map.Entry<String, String[]> entry : params.entrySet())
       {
          String[] appendedValue = entry.getValue();
 
          //
          String[] existingValue = delegate.get(entry.getKey());
          if (existingValue != null)
          {
             // Perform the concatenation operation if the entry exist
             String[] newValue = new String[existingValue.length + appendedValue.length];
             System.arraycopy(existingValue, 0, newValue, 0, existingValue.length);
             System.arraycopy(appendedValue, 0, newValue, existingValue.length, appendedValue.length);
             appendedValue = newValue;
          }
          else
          {
             // Clone the new value otherwise we would modify an the passed map
             appendedValue = appendedValue.clone();
          }
 
          //
          entry.setValue(appendedValue);
       }
 
       //
       putAll(params);
    }
 
    public String toString()
    {
       StringBuffer buffer = new StringBuffer("ParameterMap[");
       for (Iterator<Map.Entry<String, String[]>> i = entrySet().iterator(); i.hasNext();)
       {
          Map.Entry<String, String[]> entry = i.next();
          String name = (String)entry.getKey();
          String[] values = (String[])entry.getValue();
          buffer.append(name);
          for (int j = 0; j < values.length; j++)
          {
             buffer.append(j > 0 ? ',' : '=').append(values[j]);
          }
          if (i.hasNext())
          {
             buffer.append(" | ");
          }
       }
       buffer.append(']');
       return buffer.toString();
    }
 
    private void writeObject(java.io.ObjectOutputStream out) throws IOException
    {
      out.defaultWriteObject();
       out.writeBoolean(accessMode.copyValueOnRead);
       out.writeBoolean(accessMode.copyValueOnWrite);
       out.writeObject(delegate);
    }
 
    @SuppressWarnings("unchecked")
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
    {
      in.defaultReadObject();
       boolean copyValueOnRead = in.readBoolean();
       boolean copyValueOnWrite = in.readBoolean();
 
       //
       accessMode = AccessMode.get(copyValueOnRead, copyValueOnWrite);
       delegate = (Map<String, String[]>)in.readObject();
    }
 
    private static class KeyConverter extends Converter<String, String>
    {
       protected String getInternal(String external) throws IllegalArgumentException, ClassCastException
       {
          return external;
       }
 
       protected String getExternal(String internal)
       {
          return internal;
       }
 
       protected boolean equals(String left, String right)
       {
          return left.equals(right);
       }
    }
 
    /** Defines how the state of the values of a parameter map are managed. */
    public static class AccessMode
    {
 
       /**
        * Factory method for an access mode.
        *
        * @param copyValueOnRead  if true the value will be copied on a read
        * @param copyValueOnWrite if true the value will be copied on a write
        * @return the convenient access mode
        */
       public static AccessMode get(boolean copyValueOnRead, boolean copyValueOnWrite)
       {
          return copyValueOnRead ? copyValueOnWrite ? D : C : copyValueOnWrite ? B : A;
       }
 
       /** . */
       private static final AccessMode A = new AccessMode(false, false);
 
       /** . */
       private static final AccessMode B = new AccessMode(false, true);
 
       /** . */
       private static final AccessMode C = new AccessMode(true, false);
 
       /** . */
       private static final AccessMode D = new AccessMode(true, true);
 
       /** . */
       private final boolean copyValueOnRead;
 
       /** . */
       private final boolean copyValueOnWrite;
 
       /** . */
       private final ValueConverter converter;
 
       private AccessMode(boolean copyValueOnRead, boolean copyOnWrite)
       {
          this.copyValueOnRead = copyValueOnRead;
          this.copyValueOnWrite = copyOnWrite;
          this.converter = new ValueConverter(this);
       }
 
       public boolean getCopyValueOnRead()
       {
          return copyValueOnRead;
       }
 
       public boolean getCopyValueOnWrite()
       {
          return copyValueOnWrite;
       }
    }
 
    private static class ValueConverter extends Converter<String[], String[]>
    {
 
       /** . */
       private final AccessMode accessMode;
 
       private ValueConverter(AccessMode accessMode)
       {
          this.accessMode = accessMode;
       }
 
       /**
        * Only check are made to the value. The only valid values accepted are string arrays with non zero length and
        * containing non null values.
        *
        * @param external
        * @return
        * @throws NullPointerException     if the value is null
        * @throws ClassCastException       if the value type is not an array of string
        * @throws IllegalArgumentException if the array length is zero or one of the array value is null
        */
       protected String[] getInternal(String[] external) throws IllegalArgumentException, ClassCastException, NullPointerException
       {
          if (external.length == 0)
          {
             throw new IllegalArgumentException("Array must not be zero length");
          }
 
          //
          for (int i = external.length - 1; i >= 0; i--)
          {
             if (external[i] == null)
             {
                throw new IllegalArgumentException("No null entries allowed in String[]");
             }
          }
 
          //
          if (accessMode.copyValueOnWrite)
          {
             external = external.clone();
          }
 
          //
          return external;
       }
 
       protected String[] getExternal(String[] internal)
       {
          if (accessMode.copyValueOnRead)
          {
             internal = internal.clone();
          }
          return internal;
       }
 
       protected boolean equals(String[] left, String[] right)
       {
          return Arrays.equals(left, right);
       }
    }
 }
 
 
