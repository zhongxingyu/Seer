 package fr.zenexity.dbhelper;
 
 import java.lang.reflect.Array;
 import java.lang.reflect.Field;
 import java.lang.reflect.Modifier;
 import java.math.BigDecimal;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 public class JdbcResult {
 
     private JdbcResult() {
     }
 
     public interface Factory<T> {
         void init(ResultSet result) throws SQLException, JdbcResultException;
         T create(ResultSet result) throws SQLException, JdbcResultException;
     }
 
 
     public static <T> boolean primitive(Class<T> objectClass) {
         return objectClass.isPrimitive()
             || Number.class.isAssignableFrom(objectClass)
             || String.class.isAssignableFrom(objectClass)
             || Character.class.isAssignableFrom(objectClass)
             || Boolean.class.isAssignableFrom(objectClass);
     }
 
     public static <T> Factory<T> buildFactory(Class<T> objectClass) {
         return primitive(objectClass)
                 ? primitiveFactory(objectClass)
                 : classFactory(objectClass);
     }
 
     public static <T> Factory<T> buildFactory(Class<T> objectClass, String... fields) {
         return primitive(objectClass)
                 ? primitiveFactory(objectClass, fields == null || fields.length == 0 ? null : fields[0])
                 : classFactory(objectClass, fields);
     }
 
     public static <T> Factory<T> buildFactory(Class<T> objectClass, List<String> fields) {
         return primitive(objectClass)
                 ? primitiveFactory(objectClass, fields == null || fields.isEmpty() ? null : fields.get(0))
                 : classFactory(objectClass, fields);
     }
 
 
     public static <T> PrimitiveFactory<T> primitiveFactory(Class<T> objectClass) {
         return new PrimitiveFactory<T>(objectClass, null);
     }
 
     public static <T> PrimitiveFactory<T> primitiveFactory(Class<T> objectClass, int columnIndex) {
         return new PrimitiveFactory<T>(objectClass, columnIndex);
     }
 
     public static <T> PrimitiveFactory<T> primitiveFactory(Class<T> objectClass, String field) {
         return new PrimitiveFactory<T>(objectClass, field);
     }
 
 
     public static <T> ClassFactory<T> classFactory(Class<T> objectClass) {
         return new ClassFactory<T>(objectClass, null);
     }
 
     public static <T> ClassFactory<T> classFactory(Class<T> objectClass, String... fields) {
         return new ClassFactory<T>(objectClass, Arrays.asList(fields));
     }
 
     public static <T> ClassFactory<T> classFactory(Class<T> objectClass, List<String> fields) {
         return new ClassFactory<T>(objectClass, fields);
     }
 
 
     public static MapFactory mapFactory() {
         return new MapFactory();
     }
 
 
     public static ListFactory<Object> listFactory() {
         return new ListFactory<Object>(Object.class, null);
     }
 
     public static ListFactory<Object> listFactory(String... fields) {
         return new ListFactory<Object>(Object.class, Arrays.asList(fields));
     }
 
     public static ListFactory<Object> listFactory(List<String> fields) {
         return new ListFactory<Object>(Object.class, fields);
     }
 
     public static <T> ListFactory<T> ListFactory(Class<T> objectClass) {
         return new ListFactory<T>(objectClass, null);
     }
 
     public static <T> ListFactory<T> listFactory(Class<T> objectClass, String... fields) {
         return new ListFactory<T>(objectClass, Arrays.asList(fields));
     }
 
     public static <T> ListFactory<T> listFactory(Class<T> objectClass, List<String> fields) {
         return new ListFactory<T>(objectClass, fields);
     }
 
 
     public static ArrayFactory<Object> arrayFactory() {
         return new ArrayFactory<Object>(Object.class, null);
     }
 
     public static ArrayFactory<Object> arrayFactory(String... fields) {
         return new ArrayFactory<Object>(Object.class, Arrays.asList(fields));
     }
 
     public static ArrayFactory<Object> arrayFactory(List<String> fields) {
         return new ArrayFactory<Object>(Object.class, fields);
     }
 
     public static <T> ArrayFactory<T> arrayFactory(Class<T> objectClass) {
         return new ArrayFactory<T>(objectClass, null);
     }
 
     public static <T> ArrayFactory<T> arrayFactory(Class<T> objectClass, String... fields) {
         return new ArrayFactory<T>(objectClass, Arrays.asList(fields));
     }
 
     public static <T> ArrayFactory<T> arrayFactory(Class<T> objectClass, List<String> fields) {
         return new ArrayFactory<T>(objectClass, fields);
     }
 
 
     public static Object normalizeValue(Object value) {
         if (value instanceof BigDecimal) value = new Long(((BigDecimal)value).longValue());
         return value;
     }
 
    @SuppressWarnings({ "unchecked", "rawtypes" })
     public static <T> T castValue(Class<T> clazz, Object value) throws JdbcResultException {
         try {
             value = normalizeValue(value);
 
             //TODO cast to primitive types
             if (clazz.isPrimitive()) {
                 return (T) value;
             }
 
             if (clazz.isEnum()) {
                 if (value instanceof Number) {
                     return clazz.getEnumConstants()[((Number)value).intValue()];
                 } else if (value instanceof String) {
                     return (T) Enum.valueOf((Class)clazz, (String)value);
                 }
             }
 
             return clazz.cast(value);
         } catch (Exception e) {
             throw new JdbcResultException(value+" ("+value.getClass().getName()+") to "+clazz.getName(), e);
         }
     }
 
     public static class PrimitiveFactory<T> implements Factory<T> {
         private final Class<T> objectClass;
         private final String field;
         private int columnIndex;
 
         public PrimitiveFactory(Class<T> objectClass, int columnIndex) {
             this.objectClass = objectClass;
             this.field = null;
             this.columnIndex = columnIndex;
         }
 
         public PrimitiveFactory(Class<T> objectClass, String field) {
             this.objectClass = objectClass;
             this.field = field;
             this.columnIndex = 1;
         }
 
         public void init(ResultSet result) throws SQLException {
             if (field != null) {
                 ResultSetMetaData meta = result.getMetaData();
                 int count = meta.getColumnCount();
                 for (int i = 1; i <= count; i++) {
                     if (meta.getColumnLabel(i).equalsIgnoreCase(field)) {
                         columnIndex = i;
                         break;
                     }
                 }
             }
         }
 
         public T create(ResultSet result) throws SQLException, JdbcResultException {
             try {
                 return castValue(objectClass, result.getObject(columnIndex));
             } catch (SQLException e) {
                 throw e;
             } catch (JdbcResultException e) {
                 throw new JdbcResultException(field+"["+columnIndex+"]: "+e.getMessage(), e.getCause());
             } catch (Exception e) {
                 throw new JdbcResultException(e);
             }
         }
     }
 
     public static class ClassFactory<T> implements Factory<T> {
         private final Class<T> objectClass;
         private final Set<String> fields;
         private List<ColumnInfo> columns;
 
         public ClassFactory(Class<T> objectClass, Collection<String> fields) {
             this.objectClass = objectClass;
             this.fields = fields == null ? null : new HashSet<String>(fields);
         }
 
         public void init(ResultSet result) throws SQLException, JdbcResultException {
             Map<String, String> labelsToFields = new HashMap<String, String>();
             for (Field objectField : objectClass.getFields()) {
                 String fieldName = objectField.getName();
                 labelsToFields.put(fieldName.toLowerCase(), fieldName);
             }
 
             Map<String, Integer> fieldsIndexes = new HashMap<String, Integer>();
 
             ResultSetMetaData meta = result.getMetaData();
             int count = meta.getColumnCount();
             for (int i = 1; i <= count; i++) {
                 String label = meta.getColumnLabel(i);
                 if (label.length() != 0) {
                     String name = labelsToFields.get(label.toLowerCase());
                     if (name != null) label = name;
                     if (fields == null || fields.contains(label))
                         fieldsIndexes.put(label, i);
                 }
             }
 
             if (fields != null) {
                 for (String field : fields) {
                     if (!fieldsIndexes.containsKey(field))
                         throw new JdbcResultException(new NoSuchFieldException(field));
                 }
             }
 
             columns = new ArrayList<ColumnInfo>();
             try {
                 for (Map.Entry<String, Integer> fieldIndex : fieldsIndexes.entrySet()) {
                     Field objField = objectClass.getField(fieldIndex.getKey());
                     if ((objField.getModifiers() & (Modifier.STATIC | Modifier.TRANSIENT)) != 0)
                         throw new IllegalArgumentException(fieldIndex.getKey());
                     columns.add(new ColumnInfo(fieldIndex.getValue(), objField));
                 }
             } catch (Exception e) {
                 throw new JdbcResultException(e);
             }
         }
 
         public T create(ResultSet result) throws SQLException, JdbcResultException {
             ColumnInfo currentColumn = null;
             try {
                 T obj = objectClass.newInstance();
                 for (ColumnInfo column : columns) {
                     currentColumn = column;
                     Object value = result.getObject(column.index);
                     column.field.set(obj, castValue(column.field.getType(), value));
                 }
                 return obj;
             } catch (SQLException e) {
                 throw e;
             } catch (JdbcResultException e) {
                 if (currentColumn == null) throw e;
                 throw new JdbcResultException(currentColumn.field.getName()+"["+currentColumn.index+"]: "+e.getMessage(), e.getCause());
             } catch (Exception e) {
                 throw new JdbcResultException(e);
             }
         }
 
         private static class ColumnInfo {
             public final int index;
             public final Field field;
 
             public ColumnInfo(int index, Field field) {
                 this.index = index;
                 this.field = field;
             }
         }
     }
 
     public static class MapFactory implements Factory<Map<String, Object>> {
         private List<ColumnInfo> columns;
 
         public void init(ResultSet result) throws SQLException, JdbcResultException {
             columns = new ArrayList<ColumnInfo>();
 
             ResultSetMetaData meta = result.getMetaData();
             int count = meta.getColumnCount();
             for (int i = 1; i <= count; i++) {
                 String label = meta.getColumnLabel(i);
                 if (label.length() != 0) columns.add(new ColumnInfo(i, label.toLowerCase()));
             }
         }
 
         public Map<String, Object> create(ResultSet result) throws SQLException, JdbcResultException {
             Map<String, Object> map = new FieldHashMap();
             try {
                 for (ColumnInfo column : columns) {
                     Object value = result.getObject(column.index);
                     map.put(column.name, normalizeValue(value));
                 }
             } catch (SQLException e) {
                 throw e;
             } catch (Exception e) {
                 throw new JdbcResultException(e);
             }
             return map;
         }
 
         private class FieldHashMap extends HashMap<String, Object> {
             @Override
             public Object get(Object key) {
                 return super.get(key.toString().toLowerCase());
             }
         }
 
         private static class ColumnInfo {
             public final int index;
             public final String name;
 
             public ColumnInfo(int index, String name) {
                 this.index = index;
                 this.name = name;
             }
         }
     }
 
     public static abstract class CollectionFactory<T> implements Factory<T> {
         private final List<String> fields;
         private final Set<String> fieldSet;
         protected List<Integer> columns;
 
         public CollectionFactory(Collection<String> fields) {
             if (fields == null || fields.isEmpty()) {
                 this.fields = null;
                 this.fieldSet = null;
             } else {
                 this.fields = new ArrayList<String>(fields.size());
                 for (String field : fields) this.fields.add(field.toLowerCase());
                 fieldSet = new HashSet<String>(this.fields);
             }
         }
 
         public void init(ResultSet result) throws SQLException, JdbcResultException {
             ResultSetMetaData meta = result.getMetaData();
             int count = meta.getColumnCount();
 
             if (fields == null) {
                 columns = new ArrayList<Integer>(count);
                 for (int i = 1; i <= count; i++) columns.add(i);
             } else {
                 Map<String, Integer> fieldsIndexes = new HashMap<String, Integer>();
                 for (int i = 1; i <= count; i++) {
                     String label = meta.getColumnLabel(i);
                     if (label.length() != 0) {
                         label = label.toLowerCase();
                         if (fieldSet.contains(label)) fieldsIndexes.put(label, i);
                     }
                 }
                 columns = new ArrayList<Integer>(fields.size());
                 for (String field : fields) {
                     Integer index = fieldsIndexes.get(field);
                     if (index == null) {
                         throw new JdbcResultException(new NoSuchFieldException(field));
                     }
                     columns.add(index);
                 }
             }
         }
 
         public abstract T create(ResultSet result) throws SQLException, JdbcResultException;
     }
 
     public static class ListFactory<T> extends CollectionFactory<List<T>> {
         private final Class<T> objectClass;
 
         public ListFactory(Class<T> objectClass, Collection<String> fields) {
             super(fields);
             this.objectClass = objectClass;
         }
 
         @Override
         public List<T> create(ResultSet result) throws SQLException, JdbcResultException {
            List<T> list = new ArrayList<T>(columns.size());
             int i = 0;
             try {
                 for (Integer column : columns) {
                     Object value = result.getObject(column);
                     list.add(castValue(objectClass, value));
                     i++;
                 }
             } catch (SQLException e) {
                 throw e;
             } catch (JdbcResultException e) {
                 throw new JdbcResultException("List["+i+"]: "+e.getMessage(), e.getCause());
             } catch (Exception e) {
                 throw new JdbcResultException(e);
             }
             return list;
         }
     }
 
     public static class ArrayFactory<T> extends CollectionFactory<T[]> {
         private final Class<T> objectClass;
 
         public ArrayFactory(Class<T> objectClass, Collection<String> fields) {
             super(fields);
             this.objectClass = objectClass;
         }
 
         @Override
         @SuppressWarnings("unchecked")
         public T[] create(ResultSet result) throws SQLException, JdbcResultException {
             T[] array = (T[]) Array.newInstance(objectClass, columns.size());
             int i = 0;
             try {
                 for (Integer column : columns) {
                     Object value = result.getObject(column);
                     array[i] = castValue(objectClass, value);
                     i++;
                 }
             } catch (SQLException e) {
                 throw e;
             } catch (JdbcResultException e) {
                 throw new JdbcResultException("Array["+i+"]: "+e.getMessage(), e.getCause());
             } catch (Exception e) {
                 throw new JdbcResultException(e);
             }
             return array;
         }
     }
 
 }
