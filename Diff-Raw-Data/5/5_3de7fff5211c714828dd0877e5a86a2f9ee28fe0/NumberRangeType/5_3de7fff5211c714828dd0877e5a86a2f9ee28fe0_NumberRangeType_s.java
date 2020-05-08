 package org.geoserver.catalog.hibernate.types;
 
 import java.io.Serializable;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Types;
 
 import org.geotools.util.NumberRange;
 import org.geotools.util.Utilities;
 import org.hibernate.HibernateException;
 import org.hibernate.usertype.UserType;
 
 /**
  * Hibernate user type for {@link NumberRange}.
  * 
  * @author Justin Deoliveira, The Open Planning Project
  * 
  */
 public class NumberRangeType implements UserType {
 
     public Object assemble(Serializable cached, Object owner) throws HibernateException {
        return null;
     }
 
     public Object deepCopy(Object value) throws HibernateException {
         return value;
     }
 
     public Serializable disassemble(Object value) throws HibernateException {
        return null;
     }
 
     public boolean equals(Object x, Object y) throws HibernateException {
         return Utilities.equals(x, y);
     }
 
     public int hashCode(Object x) throws HibernateException {
         return x.hashCode();
     }
 
     public boolean isMutable() {
         return false;
     }
 
     public Object nullSafeGet(ResultSet rs, String[] names, Object owner)
             throws HibernateException, SQLException {
 
         double min = rs.getDouble(names[0]);
         double max = rs.getDouble(names[1]);
 
         return NumberRange.create(min, max);
     }
 
     public void nullSafeSet(PreparedStatement st, Object value, int index)
             throws HibernateException, SQLException {
 
         if (value == null) {
             st.setDouble(index, Double.NaN);
             st.setDouble(index + 1, Double.NaN);
             return;
         }
 
         NumberRange<Double> numberRange = (NumberRange<Double>) value;
         st.setDouble(index, numberRange.getMinimum());
         st.setDouble(index + 1, numberRange.getMaximum());
 
     }
 
     public Object replace(Object original, Object target, Object owner) throws HibernateException {
         return original;
     }
 
     public Class<NumberRange> returnedClass() {
         return NumberRange.class;
     }
 
     public int[] sqlTypes() {
         return new int[] { Types.DOUBLE, Types.DOUBLE };
     }
 
 }
