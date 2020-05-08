 /**
  * This file is part of the Paxle project.
  * Visit http://www.paxle.net for more information.
  * Copyright 2007-2009 the original author or authors.
  *
  * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
  * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
  * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
  * or in the file LICENSE.txt in the root directory of the Paxle distribution.
  *
  * Unless required by applicable law or agreed to in writing, this software is distributed
  * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  */
 package org.paxle.data.db.impl;
 import java.io.Serializable;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Types;
 import java.util.Properties;
 
 import org.hibernate.HibernateException;
 import org.hibernate.usertype.EnhancedUserType;
 import org.hibernate.usertype.ParameterizedType;
 
 /**
  * @author Gavin King
  * @see <a href="http://swanbear.blogspot.com/2006/09/enum-type-in-hibernate-mapping.html">Enum type in Hibernate mapping</a>
  */
 public class EnumUserType implements EnhancedUserType, ParameterizedType {
 
    private Class<Enum> enumClass;
 
    public void setParameterValues(Properties parameters) {	   
       String enumClassName = parameters.getProperty("enumClassName");
       try {
    	  ClassLoader cl = Thread.currentThread().getContextClassLoader();
    	  if (cl == null) cl = this.getClass().getClassLoader();
         enumClass = (Class<Enum>) cl.loadClass(enumClassName);
       } catch (ClassNotFoundException cnfe) {
          throw new HibernateException("Enum class not found", cnfe);
       }
    }
 
    public Object assemble(Serializable cached, Object owner) throws HibernateException {
       return cached;
    }
 
    public Object deepCopy(Object value) throws HibernateException {
       return value;
    }
 
    public Serializable disassemble(Object value) throws HibernateException {
       return (Enum) value;
    }
 
    public boolean equals(Object x, Object y) throws HibernateException {
       return x == y;
    }
 
    public int hashCode(Object x) throws HibernateException {
       return x.hashCode();
    }
 
    public boolean isMutable() {
       return false;
    }
 
    public Object nullSafeGet(ResultSet rs, String[] names, Object owner) throws HibernateException, SQLException {
       String name = rs.getString(names[0]);
       return (name==null) ? null : Enum.valueOf(enumClass, name);
    }
 
    public void nullSafeSet(PreparedStatement st, Object value, int index)
          throws HibernateException, SQLException {
       if (value == null) {
          st.setNull(index, Types.VARCHAR);
       } else {
          st.setString(index, ((Enum<?>) value).name());
       }
    }
 
    public Object replace(Object original, Object target, Object owner)
          throws HibernateException {
       return original;
    }
 
    public Class<?> returnedClass() {
       return enumClass;
    }
 
    public int[] sqlTypes() {
       return new int[] { Types.VARCHAR };
    }
 
    public Object fromXMLString(String xmlValue) {
       return Enum.valueOf(enumClass, xmlValue);
    }
 
    public String objectToSQLString(Object value) {
       return '\'' + ((Enum<?>) value).name() + '\'';
    }
 
    public String toXMLString(Object value) {
       return ((Enum<?>) value).name();
    }
 
 }
