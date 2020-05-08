 /* (c) Copyright by Man YUAN */
 package net.epsilony.mf.util.persistence;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Types;
 import java.util.Arrays;
 import org.hibernate.HibernateException;
 import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.BooleanType;
 
 /**
  *
  * @author <a href="mailto:epsilonyuan@gmail.com">Man YUAN</a>
  */
 public class Boolean3DType extends MutableUserType {
 
     @Override
     public int[] sqlTypes() {
         return new int[]{Types.BOOLEAN, Types.BOOLEAN, Types.BOOLEAN};
     }
 
     @Override
     public Class returnedClass() {
         return boolean[].class;
     }
 
     @Override
     public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner) throws HibernateException, SQLException {
 
         boolean[] result = new boolean[3];
         int i = 0;
         for (String name : names) {
             Object nullSafeGet = org.hibernate.type.BooleanType.INSTANCE.nullSafeGet(rs, name, session, owner);
             if (i == 0 && null == nullSafeGet) {
                 return null;
             } else if (null != nullSafeGet) {
                 result[i++] = (boolean) nullSafeGet;
             } else {
                 break;
             }
         }
         if (i < 3) {
             return Arrays.copyOf(result, i);
         } else {
             return result;
         }
     }
 
     @Override
     public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session) throws HibernateException, SQLException {
         boolean[] xs = (boolean[]) value;
         for (int i = 0; i < 3; i++) {
             if (xs == null || i >= xs.length) {
                BooleanType.INSTANCE.nullSafeSet(st, null, index + i, session);
             } else {
                BooleanType.INSTANCE.nullSafeSet(st, xs[i], index + i, session);
             }
         }
     }
 
     @Override
     public Object deepCopy(Object value) throws HibernateException {
         if (null == value) {
             return null;
         }
         boolean[] xs = (boolean[]) value;
         return Arrays.copyOf(xs, xs.length);
     }
 }
