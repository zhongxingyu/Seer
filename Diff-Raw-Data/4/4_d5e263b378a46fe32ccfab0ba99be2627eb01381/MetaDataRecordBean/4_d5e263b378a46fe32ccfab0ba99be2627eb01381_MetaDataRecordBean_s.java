 package eu.europeana.uim.store.bean;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import eu.europeana.uim.common.TKey;
 import eu.europeana.uim.store.Collection;
 import eu.europeana.uim.store.MetaDataRecord;
 
 /**
  * In-memory implemenation of {@link MetaDataRecord} that uses Long as ID. It is supposed to be the
  * core class for usage if there is no need for a special implemenation due to special requirements
  * of the storage backend.
  * 
  * @param <I>
  *            unique ID
  * 
  * @author Markus Muhr (markus.muhr@kb.nl)
  * @since Mar 22, 2011
  */
 @SuppressWarnings("unchecked")
 public class MetaDataRecordBean<I> extends AbstractEntityBean<I> implements MetaDataRecord<I> {
     /**
      * the collection that is responsible for this record
      */
     private Collection<I>                                collection;
 
     /**
      * holds for each key a list of known qualified values
      */
     private HashMap<TKey<?, ?>, List<QualifiedValue<?>>> fields = new HashMap<TKey<?, ?>, List<QualifiedValue<?>>>();
 
     /**
      * Creates a new instance of this class.
      */
     public MetaDataRecordBean() {
         super();
     }
 
     /**
      * Creates a new instance of this class.
      * 
      * @param id
      *            unique ID
      * @param collection
      *            the collection that is responsible for this record
      */
     public MetaDataRecordBean(I id, Collection<I> collection) {
         super(id);
         this.collection = collection;
     }
 
     /**
      * @return the collection this mdr belongs to
      */
     @Override
     public Collection<I> getCollection() {
         return collection;
     }
 
     /**
      * @param collection
      */
     public void setCollection(Collection<I> collection) {
         this.collection = collection;
     }
 
     @Override
     public <N, T> T getFirstField(TKey<N, T> key, Enum<?>... qualifiers) {
         T result = null;
         List<QualifiedValue<?>> values = fields.get(key);
         if (values != null && values.size() > 0) {
             for (QualifiedValue<?> value : values) {
                 if (value.getQualifiers().containsAll(Arrays.asList(qualifiers))) {
                     result = (T)value.getValue();
                     break;
                 }
             }
         }
         return result;
     }
 
     @Override
     public <N, T> List<QualifiedValue<T>> getField(TKey<N, T> key) {
         List<QualifiedValue<T>> result = new ArrayList<QualifiedValue<T>>();
         List<QualifiedValue<?>> values = fields.get(key);
         if (values != null && values.size() > 0) {
             for (QualifiedValue<?> value : values) {
                 result.add((QualifiedValue<T>)value);
             }
         }
         return result;
     }
 
     @Override
     public <N, T> List<T> getPlainField(TKey<N, T> key, Enum<?>... qualifiers) {
         List<T> result = new ArrayList<T>();
         List<QualifiedValue<?>> values = fields.get(key);
         if (values != null && values.size() > 0) {
             for (QualifiedValue<?> value : values) {
                 if (value.getQualifiers().containsAll(Arrays.asList(qualifiers))) {
                     result.add((T)value.getValue());
                 }
             }
         }
         return result;
     }
 
     @Override
     public <N, T> void addField(TKey<N, T> key, T value, Enum<?>... qualifiers) {
         if (value == null) { throw new IllegalArgumentException(
                 "Argument 'value' should not be null!"); }
 
         Set<Enum<?>> quals = new HashSet<Enum<?>>();
         for (Enum<?> qualifier : qualifiers) {
             if (qualifier == null) { throw new IllegalArgumentException(
                     "Argument 'qualifiers' should not have null entries!"); }
             quals.add(qualifier);
         }
 
         List<QualifiedValue<?>> values = fields.get(key);
         if (values == null) {
             values = new ArrayList<MetaDataRecord.QualifiedValue<?>>();
             fields.put(key, values);
         }
 
         values.add(new QualifiedValue<T>(value, quals));
     }
 
     @Override
     public <N, T> List<QualifiedValue<T>> deleteField(TKey<N, T> key) {
         List<QualifiedValue<T>> result = new ArrayList<QualifiedValue<T>>();
         List<QualifiedValue<?>> values = fields.remove(key);
         if (values != null && values.size() > 0) {
             for (QualifiedValue<?> value : values) {
                 result.add((QualifiedValue<T>)value);
             }
         }
         return result;
     }
 
     /**
      * @return available keys
      */
     public Set<TKey<?, ?>> getAvailableKeys() {
        return fields.keySet();
     }
 }
