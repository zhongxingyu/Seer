 package org.atomium.metadata;
 
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Maps;
 import org.atomium.CacheType;
 import org.atomium.NamedValues;
 import org.atomium.annotations.Column;
 import org.atomium.annotations.Table;
 
 import java.beans.BeanInfo;
 import java.beans.IntrospectionException;
 import java.beans.Introspector;
 import java.beans.PropertyDescriptor;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Field;
 import java.util.Map;
 import java.util.Set;
 
 import static com.google.common.base.CaseFormat.LOWER_CAMEL;
 import static com.google.common.base.CaseFormat.UPPER_CAMEL;
 import static com.google.common.base.Preconditions.checkState;
 import static com.google.common.base.Throwables.propagate;
 
 /**
  * @author Blackrush
  */
 public class Metadata<T> {
     private final MetadataRegistry registry;
     private final Class<T> target;
     private final Map<String, ColumnMetadata<T>> columns = Maps.newHashMap();
 
     private boolean loaded;
     private String tableName;
     private ColumnMetadata<T> primaryKey;
     private CacheType cacheType;
 
     public Metadata(MetadataRegistry registry, Class<T> target) {
         this.registry = registry;
         this.target = target;
     }
 
     /**
      * gets the {@link MetadataRegistry} where this met
      * @return
      */
     public MetadataRegistry getRegistry() {
         return registry;
     }
 
     /**
      *
      * @return
      */
     public Class<T> getTarget() {
         return target;
     }
 
     /**
      *
      * @return
      */
     public BeanInfo getBeanInfo() {
         try {
             return Introspector.getBeanInfo(target);
         } catch (IntrospectionException e) {
             throw propagate(e);
         }
     }
 
     /**
      * @see Class#getAnnotation(Class)
      */
     public <T extends Annotation> T getAnnotation(Class<T> clazz) {
         return target.getAnnotation(clazz);
     }
 
     /**
      * dome some back-end operations
      */
     public void load() {
         checkState(!loaded, "this metadata is already loaded");
 
         loadMetadata();
         loadColumns();
 
         checkState(primaryKey != null, "a metadata must have a primary key");
 
         loaded = true;
     }
 
     private void loadMetadata() {
         Table table = target.getAnnotation(Table.class);
         if (table != null && !table.value().equals(Table.DEFAULT)) {
             tableName = table.value();
         } else {
             tableName = UPPER_CAMEL.to(LOWER_CAMEL, target.getSimpleName());
         }
        cacheType = table.cache();
     }
 
     private void loadColumns() {
         for (PropertyDescriptor prop : getBeanInfo().getPropertyDescriptors()) {
             Column annotation = prop.getReadMethod().getAnnotation(Column.class);
             if (annotation == null) continue;
 
             String name;
             if (!annotation.value().equals(Column.DEFAULT)) {
                 name = annotation.value();
             } else {
                 name = prop.getName();
             }
 
             PropertyColumnMetadata<T> column = new PropertyColumnMetadata<>(this, name, prop.getReadMethod(), prop.getWriteMethod());
             column.load();
 
             if (column.isPrimaryKey()) {
                 checkState(primaryKey == null, "%s has more than one primary key", target.getName());
                 primaryKey = column;
             }
             columns.put(name, column);
         }
 
         for (Field field : getTarget().getDeclaredFields()) {
             Column annotation = field.getAnnotation(Column.class);
             if (annotation == null) continue;
 
             String name;
             if (!annotation.value().equals(Column.DEFAULT)) {
                 name = annotation.value();
             } else {
                 name = field.getName();
             }
 
             FieldColumnMetadata<T> column = new FieldColumnMetadata<>(this, name, field);
             column.load();
 
             if (column.isPrimaryKey()) {
                 checkState(primaryKey == null, "%s has more than one primary key", target.getName());
                 primaryKey = column;
             }
             columns.put(name, column);
         }
     }
 
     /**
      * a set of all columns
      * @return an immutable colletion of all columns
      */
     public Set<ColumnMetadata<T>> getColumns() {
         return ImmutableSet.copyOf(columns.values());
     }
 
     /**
      * get the column by its name
      * @param name column's name
      * @return found column or null
      */
     public ColumnMetadata<T> getColumn(String name) {
         return columns.get(name);
     }
 
     /**
      * get the table's name
      * @return table's name
      */
     public String getTableName() {
         return tableName;
     }
 
     /**
      * get the cache's type of this metadata
      * @return the non-null cache type
      */
     public CacheType getCacheType() {
         return cacheType;
     }
 
     /**
      *
      * @param instance
      * @return
      */
     public NamedValues map(T instance) {
         NamedValues values = NamedValues.of();
 
         for (ColumnMetadata<T> column : columns.values()) {
             ConverterInterface converter = column.getConverter();
 
             if (converter == null || !converter.export(column, instance, values)) {
                 values.set(column.getName(), column.get(instance));
             }
         }
 
         return values;
     }
 
     /**
      *
      * @param values
      * @return
      */
     public T map(NamedValues values) {
         T instance = newEmpty();
 
         for (ColumnMetadata<T> column : columns.values()) {
             ConverterInterface converter = column.getConverter();
 
             if (converter == null || !converter.extract(column, instance, values)) {
                 column.set(instance, values.get(column.getName()));
             }
         }
 
         registry.onInstantiated(instance);
 
         return instance;
     }
 
     /**
      *
      * @return
      */
     public ColumnMetadata<T> getPrimaryKey() {
         return primaryKey;
     }
 
     protected T newEmpty() {
         try {
             return target.newInstance();
         } catch (InstantiationException | IllegalAccessException e) {
             throw propagate(e);
         }
     }
 
     /**
      *
      * @return
      */
     public T createEmpty() {
         T instance = newEmpty();
         registry.onInstantiated(instance);
         return instance;
     }
 
     /**
      * this methods will return {@code true} if the given entity is not persisted in the database (ie not created)
      * @param instance the entity
      * @return a boolean
      * TODO improve the way to know if an entity is persisted or not
      */
     public boolean isPersisted(T instance) {
         Object pkey = primaryKey.get(instance);
         return pkey != null && !pkey.equals(0);
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         Metadata metadata = (Metadata) o;
 
         if (columns != null ? !columns.equals(metadata.columns) : metadata.columns != null) return false;
         if (primaryKey != null ? !primaryKey.equals(metadata.primaryKey) : metadata.primaryKey != null) return false;
         if (tableName != null ? !tableName.equals(metadata.tableName) : metadata.tableName != null) return false;
         if (target != null ? !target.equals(metadata.target) : metadata.target != null) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = target != null ? target.hashCode() : 0;
         result = 31 * result + (columns != null ? columns.hashCode() : 0);
         result = 31 * result + (tableName != null ? tableName.hashCode() : 0);
         result = 31 * result + (primaryKey != null ? primaryKey.hashCode() : 0);
         return result;
     }
 }
