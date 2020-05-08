 package acz.dbmagic;
 
 import java.beans.BeanInfo;
 import java.beans.IntrospectionException;
 import java.beans.Introspector;
 import java.beans.PropertyDescriptor;
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.lang.reflect.Proxy;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.skife.jdbi.v2.StatementContext;
 import org.skife.jdbi.v2.tweak.ResultSetMapper;
 
 public class MagicMapper<T> implements ResultSetMapper<T>
 {
     private final Class<T> type;
     private final Map<String, PropertyDescriptor> properties = new HashMap<String, PropertyDescriptor>();
 
     public static <T> MagicMapper<T> forClass(Class<T> type)
     {
         return new MagicMapper<T>(type);
     }
 
     private MagicMapper(Class<T> type)
     {
         this.type = type;
         try {
             BeanInfo info = Introspector.getBeanInfo(type, type.getSuperclass());
             for (PropertyDescriptor descriptor : info.getPropertyDescriptors()) {
                 Method method = descriptor.getReadMethod();
                 if ((method != null) && Modifier.isAbstract(method.getModifiers())) {
                     properties.put(descriptor.getName().toLowerCase(), descriptor);
                 }
             }
         }
         catch (IntrospectionException e) {
             throw new IllegalArgumentException(e);
         }
         if (type.getMethods().length != properties.size()) {
             throw new IllegalArgumentException("Class has non-bean methods");
         }
     }
 
     @Override
     public T map(int index, ResultSet rs, StatementContext ctx) throws SQLException
     {
         Map<String, Integer> names = new HashMap<String, Integer>();
 
         final Map<String, Object> values = new HashMap<String, Object>();
 
         ResultSetMetaData metadata = rs.getMetaData();
 
         for (int i = 1; i <= metadata.getColumnCount(); i++) {
             String name = metadata.getColumnLabel(i).toLowerCase().replace("_", "");
 
             if (index == 0) {
                 if (names.containsKey(name)) {
                     int dup = names.get(name);
                     throw new IllegalArgumentException(String.format(
                         "Column %d '%s' name is duplicated by column %d '%s'",
                         i, metadata.getColumnLabel(i), dup, metadata.getColumnLabel(dup)));
                 }
                 names.put(name, i);
             }
 
             PropertyDescriptor descriptor = properties.get(name);
             if (descriptor != null) {
                 Object value = getValue(descriptor, rs, i);
                 values.put(descriptor.getReadMethod().getName(), value);
             }
         }
 
         if (values.size() < properties.size()) {
             throw new IllegalArgumentException(String.format(
                 "Class has %d properties, but only %d were mapped to columns",
                 properties.size(), values.size()));
         }
 
         return getProxy(new InvocationHandler()
         {
             @Override
             public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
             {
                 return values.get(method.getName());
             }
         });
     }
 
     @SuppressWarnings({"unchecked"})
     private T getProxy(InvocationHandler handler)
     {
         return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class[]{type}, handler);
     }
 
     private static Object getValue(PropertyDescriptor descriptor, ResultSet rs, int i) throws SQLException
     {
         Class type = descriptor.getPropertyType();
 
        if (rs.wasNull()) {
             if (type.isPrimitive()) {
                 throw new IllegalArgumentException(String.format(
                     "Cannot assign null from column %d '%s' to property '%s' with primitive type '%s'",
                     i, rs.getMetaData().getColumnLabel(i),
                     descriptor.getName(), type.getName()));
             }
             return null;
         }
 
         if (type.isAssignableFrom(Boolean.class) || type.isAssignableFrom(boolean.class)) {
             return rs.getBoolean(i);
         }
         if (type.isAssignableFrom(Byte.class) || type.isAssignableFrom(byte.class)) {
             return rs.getByte(i);
         }
         if (type.isAssignableFrom(Short.class) || type.isAssignableFrom(short.class)) {
             return rs.getShort(i);
         }
         if (type.isAssignableFrom(Integer.class) || type.isAssignableFrom(int.class)) {
             return rs.getInt(i);
         }
         if (type.isAssignableFrom(Long.class) || type.isAssignableFrom(long.class)) {
             return rs.getLong(i);
         }
         if (type.isAssignableFrom(Float.class) || type.isAssignableFrom(float.class)) {
             return rs.getFloat(i);
         }
         if (type.isAssignableFrom(Double.class) || type.isAssignableFrom(double.class)) {
             return rs.getDouble(i);
         }
 
        Object value = rs.getObject(i);
         if (!type.isAssignableFrom(value.getClass())) {
             throw new IllegalArgumentException(String.format(
                 "Value type '%s' from column %d '%s' is not assignable to property '%s' type '%s'",
                 value.getClass().getName(),
                 i, rs.getMetaData().getColumnLabel(i),
                 descriptor.getName(), type.getName()));
         }
         return value;
     }
 }
