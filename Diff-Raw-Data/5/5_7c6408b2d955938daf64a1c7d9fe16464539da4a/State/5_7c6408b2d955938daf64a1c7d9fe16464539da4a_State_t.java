 package com.psddev.dari.db;
 
 import com.psddev.dari.util.ObjectToIterable;
 import com.psddev.dari.util.ObjectUtils;
 import com.psddev.dari.util.PullThroughCache;
 import com.psddev.dari.util.StorageItem;
 import com.psddev.dari.util.StringUtils;
 import com.psddev.dari.util.TypeDefinition;
 import com.psddev.dari.util.UuidUtils;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Type;
 import java.lang.reflect.TypeVariable;
 import java.net.URI;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /** Represents the state of an object stored in the database. */
 public class State implements Map<String, Object> {
 
     public static final String ID_KEY = "_id";
     public static final String TYPE_KEY = "_type";
     public static final String LABEL_KEY = "_label";
 
     /**
      * {@linkplain Query#getOptions Query option} that contains the object
      * whose fields are being resolved automatically.
      */
     public static final String REFERENCE_RESOLVING_QUERY_OPTION = "dari.referenceResolving";
 
     private static final Logger LOGGER = LoggerFactory.getLogger(State.class);
 
     private static final int STATUS_FLAG_OFFSET = 16;
     private static final int STATUS_FLAG_MASK = -1 >>> STATUS_FLAG_OFFSET;
     private static final int IS_ALL_RESOLVED_FLAG = 1 << 0;
     private static final int IS_RESOLVE_TO_REFERENCE_ONLY_FLAG = 1 << 1;
 
     private final Map<Class<?>, Object> linkedObjects = new LinkedHashMap<Class<?>, Object>();
     private Database database;
     private UUID id;
     private UUID typeId;
     private final Map<String, Object> rawValues = new LinkedHashMap<String, Object>();
     private Map<String, Object> extras;
     private List<AtomicOperation> atomicOperations;
     private Map<ObjectField, List<String>> errors;
     private volatile int flags;
 
     /**
      * Returns the state associated with the given {@code object}.
      *
      * @throws IllegalArgumentException If the given {@code object}
      *         doesn't have any state associated to it.
      */
     public static State getInstance(Object object) {
 
         if (object == null) {
             return null;
 
         } else if (object instanceof State) {
             return (State) object;
 
         } else if (object instanceof Recordable) {
             return ((Recordable) object).getState();
 
         } else {
             throw new IllegalArgumentException(String.format(
                     "Can't retrieve state from an instance of [%s]!",
                     object.getClass().getName()));
         }
     }
 
     /**
      * Links the given {@code object} to this state so that changes on
      * either side are copied over.
      */
     public void linkObject(Object object) {
         if (object != null) {
             linkedObjects.put(object.getClass(), object);
         }
     }
 
     /**
      * Unlinks the given {@code object} from this state so that changes
      * on either side are no longer copied over.
      */
     public void unlinkObject(Object object) {
         if (object != null) {
             linkedObjects.remove(object.getClass());
         }
     }
 
     /** Returns the originating database. */
     public Database getDatabase() {
         if (database == null) {
             setDatabase(Database.Static.getDefault());
         }
         return database;
     }
 
     /** Sets the originating database. */
     public void setDatabase(Database database) {
         this.database = database;
     }
 
     /** Returns the unique ID. */
     public UUID getId() {
         if (id == null) {
             setId(UuidUtils.createSequentialUuid());
         }
         return this.id;
     }
 
     /** Sets the unique ID. */
     public void setId(UUID id) {
         this.id = id;
     }
 
     /** Returns the type ID. */
     public UUID getTypeId() {
         if (typeId != null) {
             return typeId;
 
         } else {
             UUID newTypeId = UuidUtils.ZERO_UUID;
             if (!linkedObjects.isEmpty()) {
                 Database database = getDatabase();
                 if (database != null) {
                     ObjectType type = database.getEnvironment().getTypeByClass(linkedObjects.keySet().iterator().next());
                     if (type != null) {
                         newTypeId = type.getId();
                     }
                 }
             }
 
             setTypeId(newTypeId);
             return newTypeId;
         }
     }
 
     /** Sets the type ID. */
     public void setTypeId(UUID typeId) {
         this.typeId = typeId;
     }
 
     /** Returns the type. */
     public ObjectType getType() {
         ObjectType type = getDatabase().getEnvironment().getTypeById(getTypeId());
         if (type == null) {
 
             // During the bootstrapping process, the type for the root
             // type is not available, so fake it here.
             for (Object object : linkedObjects.values()) {
                 if (object instanceof ObjectType && getId().equals(getTypeId())) {
                     type = (ObjectType) object;
                     type.setObjectClassName(ObjectType.class.getName());
                     type.initialize();
                 }
                 break;
             }
         }
 
         return type;
     }
 
     /**
      * Sets the type. This method may also change the originating
      * database based on the the given {@code type}.
      */
     public void setType(ObjectType type) {
         if (type == null) {
             setTypeId(null);
         } else {
             setTypeId(type.getId());
             setDatabase(type.getState().getDatabase());
         }
     }
 
     public ObjectField getField(String name) {
         ObjectField field = getDatabase().getEnvironment().getField(name);
         if (field == null) {
             ObjectType type = getType();
             if (type != null) {
                 field = type.getField(name);
             }
         }
         return field;
     }
 
     /** Returns the status. */
     public StateStatus getStatus() {
         int statusFlag = flags >>> STATUS_FLAG_OFFSET;
         for (StateStatus status : StateStatus.values()) {
             if (statusFlag == status.getFlag()) {
                 return status;
             }
         }
         return null;
     }
 
     /**
      * Returns {@code true} if this state has never been saved to the
      * database.
      */
     public boolean isNew() {
         return (flags >>> STATUS_FLAG_OFFSET) == 0;
     }
 
     private boolean checkStatus(StateStatus status) {
         return ((flags >>> STATUS_FLAG_OFFSET) & status.getFlag()) > 0;
     }
 
     /**
      * Returns {@code true} if this state was recently deleted from the
      * database.
      */
     public boolean isDeleted() {
         return checkStatus(StateStatus.DELETED);
     }
 
     /**
      * Returns {@code true} if this state only contains the reference
      * to the object (ID and type ID).
      */
     public boolean isReferenceOnly() {
         return checkStatus(StateStatus.REFERENCE_ONLY);
     }
 
     /**
      * Sets the status. This method will also clear all pending atomic
      * operations and existing validation errors.
      */
     public void setStatus(StateStatus status) {
         flags &= STATUS_FLAG_MASK;
         if (status != null) {
             flags |= status.getFlag() << STATUS_FLAG_OFFSET;
         }
         if (atomicOperations != null) {
             atomicOperations.clear();
         }
         if (errors != null) {
             errors.clear();
         }
     }
 
     /** Returns the map of all the fields values. */
     public Map<String, Object> getValues() {
         resolveReferences();
         return this;
     }
 
     /** Sets the map of all the values. */
     public void setValues(Map<String, Object> values) {
         clear();
         putAll(values);
     }
 
     /**
      * Returns a map of all values converted to only simple types:
      * {@code null}, {@link java.lang.Boolean}, {@link java.lang.Number},
      * {@link java.lang.String}, {@link java.util.ArrayList}, or
      * {@link java.util.LinkedHashMap}.
      */
     public Map<String, Object> getSimpleValues() {
         Map<String, Object> values = new LinkedHashMap<String, Object>();
         for (Map.Entry<String, Object> e : getValues().entrySet()) {
             String name = e.getKey();
             ObjectField field = getField(name);
             values.put(name, toSimpleValue(e.getValue(), field != null && field.isEmbedded()));
         }
         values.put(StateValueUtils.ID_KEY, getId().toString());
         values.put(StateValueUtils.TYPE_KEY, getTypeId().toString());
         return values;
     }
 
     /**
      * Similar to {@link #getSimpleValues()} but only returns those values
      * with fields strictly defined on this State's type.
      * @deprecated No replacement
      * @see #getSimpleValues()
      */
     @Deprecated
     public Map<String, Object> getSimpleFieldedValues() {
         Map<String, Object> values = new LinkedHashMap<String, Object>();
         for (Map.Entry<String, Object> e : getValues().entrySet()) {
             String name = e.getKey();
             ObjectField field = getField(name);
             if (field != null) {
                 values.put(name, toSimpleValue(e.getValue(), field.isEmbedded()));
             }
         }
         values.put(StateValueUtils.ID_KEY, getId().toString());
         values.put(StateValueUtils.TYPE_KEY, getTypeId().toString());
         return values;
     }
 
     /**
      * Converts the given {@code value} into an instance of one of
      * the simple types listed in {@link #getSimpleValues}.
      */
     private static Object toSimpleValue(Object value, boolean isEmbedded) {
         if (value == null) {
             return null;
         }
 
         Iterable<Object> valueIterable = ObjectToIterable.iterable(value);
         if (valueIterable != null) {
             List<Object> list = new ArrayList<Object>();
             for (Object item : valueIterable) {
                 list.add(toSimpleValue(item, isEmbedded));
             }
             return list;
 
         } else if (value instanceof Map) {
             Map<String, Object> map = new LinkedHashMap<String, Object>();
             for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                 Object key = entry.getKey();
                 if (key != null) {
                     map.put(key.toString(), toSimpleValue(entry.getValue(), isEmbedded));
                 }
             }
             return map;
 
         } else if (value instanceof Query) {
             return ((Query<?>) value).getState().getSimpleValues();
 
         } else if (value instanceof Recordable) {
             State valueState = ((Recordable) value).getState();
             if (valueState.isNew()) {
                 ObjectType type;
                 if (isEmbedded
                         || ((type = valueState.getType()) != null
                         && type.isEmbedded())) {
                     return valueState.getSimpleValues();
                 }
             }
 
             Map<String, Object> map = new LinkedHashMap<String, Object>();
             map.put(StateValueUtils.REFERENCE_KEY, valueState.getId().toString());
             map.put(StateValueUtils.TYPE_KEY, valueState.getTypeId().toString());
             return map;
 
         } else if (value instanceof Boolean
                 || value instanceof Number
                 || value instanceof String) {
             return value;
 
         } else if (value instanceof Character
                 || value instanceof CharSequence
                 || value instanceof String
                 || value instanceof URI
                 || value instanceof URL
                 || value instanceof UUID) {
             return value.toString();
 
         } else if (value instanceof Date) {
             return ((Date) value).getTime();
 
         } else if (value instanceof Enum) {
             return ((Enum<?>) value).name();
 
         } else {
             return toSimpleValue(ObjectUtils.to(Map.class, value), isEmbedded);
         }
     }
 
     /** Returns the field value associated with the given {@code path}. */
     public Object getValue(String path) {
         if (path == null) {
             return null;
         }
 
         Object value = this;
 
         for (String key; path != null; ) {
             int slashAt = path.indexOf('/');
             if (slashAt > -1) {
                 key = path.substring(0, slashAt);
                 path = path.substring(slashAt + 1);
             } else {
                 key = path;
                 path = null;
             }
 
             if (value instanceof Recordable) {
                 value = ((Recordable) value).getState();
             }
 
             if (value instanceof State) {
                 State valueState = (State) value;
                 if (ID_KEY.equals(key)) {
                     value = valueState.getId();
                 } else if (TYPE_KEY.equals(key)) {
                     value = valueState.getType();
                 } else if (LABEL_KEY.equals(key)) {
                     value = valueState.getLabel();
                 } else {
                     value = valueState.get(key);
                 }
 
             } else if (value instanceof Map) {
                 value = ((Map<?, ?>) value).get(key);
 
             } else if (value instanceof List) {
                 Integer index = ObjectUtils.to(Integer.class, key);
                 if (index != null) {
                     List<?> list = (List<?>) value;
                     int listSize = list.size();
 
                     if (index < 0) {
                         index += listSize;
                     }
                     if (index >= 0 && index < listSize) {
                         value = list.get(index);
                         continue;
                     }
                 }
 
                 return null;
 
             } else {
                 return null;
             }
         }
 
         return value;
     }
 
     public Map<String, Object> getRawValues() {
         return rawValues;
     }
 
     /**
      * Returns the field associated with the given {@code name} as an
      * instance of the given {@code returnType}. This version of get will
      * not trigger reference resolution which avoids a round-trip to the
      * database.
      */
     public Object getRawValue(String name) {
         Object value = rawValues;
 
         for (String part : StringUtils.split(name, "/")) {
 
             if (value == null) {
                 break;
 
             } else if (value instanceof Recordable) {
                 value = ((Recordable) value).getState().getValue(part);
 
             } else if (value instanceof Map) {
                 value = ((Map<?, ?>) value).get(part);
 
             } else if (value instanceof List) {
                 Integer index = ObjectUtils.to(Integer.class, part);
                 if (index != null) {
                     List<?> list = (List<?>) value;
                     if (index >= 0 && index < list.size()) {
                         value = list.get(index);
                         continue;
                     }
                 }
 
                 value = null;
                 break;
             }
         }
 
         return value;
     }
 
     /** Puts the given field value at given name. */
     @SuppressWarnings("unchecked")
     public void putValue(String name, Object value) {
         Map<String, Object> parent = getValues();
         String[] parts = StringUtils.split(name, "/");
         int last = parts.length - 1;
         for (int i = 0; i < last; i ++) {
             String part = parts[i];
             Object child = parent.get(part);
             if (child instanceof Recordable) {
                 parent = ((Recordable) child).getState().getValues();
             } else {
                 if (!(child instanceof Map)) {
                     child = new LinkedHashMap<String, Object>();
                     parent.put(part, child);
                 }
                 parent = (Map<String, Object>) child;
             }
         }
         parent.put(parts[last], value);
     }
 
     /** Returns the indexes for the ObjectType returned by {@link #getType()}
      *  as well as any embedded indexes on this State. */
     public Set<ObjectIndex> getIndexes() {
         Set<ObjectIndex> indexes = new LinkedHashSet<ObjectIndex>();
         ObjectType type = getType();
         if (type != null) {
             indexes.addAll(type.getIndexes());
 
             for (Map.Entry<String, Object> entry : getValues().entrySet()) {
                 ObjectField field = getField(entry.getKey());
                 if (field != null) {
                     getEmbeddedIndexes(indexes, null, field, entry.getValue());
                 }
             }
         }
         return indexes;
     }
 
     /** Recursively gathers all the embedded Indexes for this State object. */
     private void getEmbeddedIndexes(Set<ObjectIndex> indexes,
             String parentFieldName,
             ObjectField field, Object fieldValue) {
 
         if (fieldValue instanceof Recordable) {
             State state = State.getInstance(fieldValue);
             ObjectType type = state.getType();
             if (type == null) {
                 return;
             }
 
             if (field.isEmbedded() || type.isEmbedded()) {
                 for (ObjectIndex i : type.getIndexes()) {
                     ObjectIndex index = new ObjectIndex(getType(), null);
 
                     StringBuilder builder = new StringBuilder();
                     StringBuilder builder2 = new StringBuilder();
 
                     if (parentFieldName != null) {
                         builder.append(parentFieldName).append("/");
                         builder2.append(parentFieldName).append("/");
                     }
                     builder.append(field.getInternalName()).append("/");
                     builder2.append(field.getInternalName()).append("/");
                     builder.append(i.getField());
                     builder2.append(i.getUniqueName());
 
                     String fieldName = builder.toString();
                     index.setField(fieldName);
                     index.setType(i.getType());
                     index.setUnique(i.isUnique());
 
                     // get the declaring class of the first level field name
                     int slashAt = fieldName.indexOf('/');
                     String firstLevelFieldName = fieldName.substring(0, slashAt);
                     ObjectField firstLevelField = getField(firstLevelFieldName);
                     if (firstLevelField != null && firstLevelField.getJavaDeclaringClassName() != null) {
                         index.setJavaDeclaringClassName(firstLevelField.getJavaDeclaringClassName());
                     } else {
                         index.setJavaDeclaringClassName(getType().getObjectClassName());
                     }
 
                     indexes.add(index);
 
                     ObjectIndex index2 = new ObjectIndex(getType(), null);
                     index2.setName(builder2.toString());
                     index2.setField(index.getField());
                     index2.setType(index.getType());
                     index2.setUnique(index.isUnique());
                     index2.setJavaDeclaringClassName(index.getJavaDeclaringClassName());
                     indexes.add(index2);
                 }
 
                 if (parentFieldName == null) {
                     parentFieldName = field.getInternalName();
                 } else {
                     parentFieldName += "/"+field.getInternalName();
                 }
                 Map<String, Object> values = state.getValues();
 
                 for (Map.Entry<String, Object> entry : values.entrySet()) {
                     ObjectField objectField = state.getField(entry.getKey());
                     if (objectField != null) {
                         getEmbeddedIndexes(indexes, parentFieldName,
                                 objectField, entry.getValue());
                     }
                 }
             }
         } else if (fieldValue instanceof Map) {
             for (Map.Entry<?, ?> entry : ((Map<?, ?>) fieldValue).entrySet()) {
                 getEmbeddedIndexes(indexes, parentFieldName, field, entry.getValue());
             }
         } else if (fieldValue instanceof Iterable) {
             for (Object listItem : (Iterable<?>)fieldValue) {
                 getEmbeddedIndexes(indexes, parentFieldName, field, listItem);
             }
         }
     }
 
     /**
      * Returns an unmodifiable list of all the atomic operations pending on
      * this record.
      */
     public List<AtomicOperation> getAtomicOperations() {
         if (atomicOperations == null) {
             atomicOperations = new ArrayList<AtomicOperation>();
         }
         return atomicOperations;
     }
 
     // Queues up an atomic operation and updates the internal state.
     private void queueAtomicOperation(AtomicOperation operation) {
         if (atomicOperations == null) {
             atomicOperations = new ArrayList<AtomicOperation>();
         }
         operation.execute(this);
         atomicOperations.add(operation);
     }
 
     /**
      * Increments the field associated with the given {@code name} by the
      * given {@code value}. If the field contains a non-numeric value, it
      * will be set to 0 first.
      */
     public void incrementAtomically(String name, double value) {
         queueAtomicOperation(new AtomicOperation.Increment(name, value));
     }
 
     /**
      * Decrements the field associated with the given {@code name} by the
      * given {@code value}. If the field contains a non-numeric value, it
      * will be set to 0 first.
      */
     public void decrementAtomically(String name, double value) {
         incrementAtomically(name, -value);
     }
 
     /**
      * Adds the given {@code value} to the collection field associated with
      * the given {@code name}.
      */
     public void addAtomically(String name, Object value) {
         queueAtomicOperation(new AtomicOperation.Add(name, value));
     }
 
     /**
      * Removes all instances of the given {@code value} in the collection
      * field associated with the given {@code name}.
      */
     public void removeAtomically(String name, Object value) {
         queueAtomicOperation(new AtomicOperation.Remove(name, value));
     }
 
     /**
      * Replaces the field value at the given {@code name} with the given
      * {@code value}. If the object changes in the database before
      * {@link #save()} is called, {@link AtomicOperation.ReplacementException}
      * will be thrown.
      */
     public void replaceAtomically(String name, Object value) {
         queueAtomicOperation(new AtomicOperation.Replace(name, getValue(name), value));
     }
 
     /**
      * Puts the given {@code value} into the field associated with the
      * given {@code name} atomically.
      */
     public void putAtomically(String name, Object value) {
         queueAtomicOperation(new AtomicOperation.Put(name, value));
     }
 
     /** Returns all the fields with validation errors from this record. */
     public Set<ObjectField> getErrorFields() {
         return errors != null
                 ? Collections.unmodifiableSet(errors.keySet())
                 : Collections.<ObjectField>emptySet();
     }
 
     /**
      * Returns all the validation errors for the given field from this
      * record.
      */
     public List<String> getErrors(ObjectField field) {
         if (errors != null) {
             List<String> messages = errors.get(field);
             if (messages != null && messages.size() > 0) {
                 return Collections.unmodifiableList(messages);
             }
         }
         return Collections.emptyList();
     }
 
     /** Returns true if this record has any validation errors. */
     public boolean hasAnyErrors() {
         if (errors != null) {
             for (List<String> messages : errors.values()) {
                 if (messages != null && !messages.isEmpty()) {
                     return true;
                 }
             }
         }
 
         ObjectType type = getType();
         if (type != null) {
             for (ObjectField field : type.getFields()) {
                 if (hasErrorsForValue(get(field.getInternalName()), field.isEmbedded())) {
                     return true;
                 }
             }
         }
 
         return false;
     }
 
     private boolean hasErrorsForValue(Object value, boolean embedded) {
         if (value instanceof Map) {
             value = ((Map<?, ?>) value).values();
         }
 
         if (value instanceof Iterable) {
             for (Object item : (Iterable<?>) value) {
                 if (hasErrorsForValue(item, embedded)) {
                     return true;
                 }
             }
 
         } else if (value instanceof Recordable) {
             State valueState = ((Recordable) value).getState();
 
             if (embedded) {
                 if (valueState.hasAnyErrors()) {
                     return true;
                 }
 
             } else {
                 ObjectType valueType = valueState.getType();
                if (valueType != null && valueType.isEmbedded() && valueState.hasAnyErrors()) {
                     return true;
                 }
             }
         }
 
         return false;
     }
 
     /**
      * Returns true if the given field in this record has any validation
      * errors.
      */
     public boolean hasErrors(ObjectField field) {
         return errors != null && errors.get(field).size() > 0;
     }
 
     /** Returns a modifiable map of all the extras values from this state. */
     public Map<String, Object> getExtras() {
         if (extras == null) {
             extras = new LinkedHashMap<String, Object>();
         }
         return extras;
     }
 
     /**
      * Returns the extra value associated with the given {@code name} from
      * this state.
      */
     public Object getExtra(String name) {
         return extras != null ? extras.get(name) : null;
     }
 
     public void addError(ObjectField field, String message) {
 
         if (errors == null) {
             errors = new LinkedHashMap<ObjectField, List<String>>();
         }
 
         List<String> messages = errors.get(field);
         if (messages == null) {
             messages = new ArrayList<String>();
             errors.put(field, messages);
         }
         messages.add(message);
     }
 
     public boolean isResolveToReferenceOnly() {
         return (flags & IS_RESOLVE_TO_REFERENCE_ONLY_FLAG) > 0;
     }
 
     public void setResolveToReferenceOnly(boolean isResolveToReferenceOnly) {
         if (isResolveToReferenceOnly) {
             flags |= IS_RESOLVE_TO_REFERENCE_ONLY_FLAG;
         } else {
             flags &= ~IS_RESOLVE_TO_REFERENCE_ONLY_FLAG;
         }
     }
 
     /** Returns a descriptive label for this state. */
     public String getLabel() {
         Object object = getOriginalObject();
         return object instanceof Record ?
                 ((Record) object).getLabel() :
                 getDefaultLabel();
     }
 
     // To check for circular references in resolving labels.
     private static final ThreadLocal<Map<UUID, String>> LABEL_CACHE = new ThreadLocal<Map<UUID, String>>();
 
     /**
      * Returns the default, descriptive label for this state.
      * Fields specified in {@link Recordable.LabelFields} are used to
      * construct it. If the referenced field value contains a state,
      * it may call itself, but not more than once per state.
      */
     protected String getDefaultLabel() {
         ObjectType type = getType();
         if (type != null) {
 
             StringBuilder label = new StringBuilder();
             for (String field : type.getLabelFields()) {
                 Object value = getValue(field);
                 if (value != null) {
 
                     String valueString;
                     if (value instanceof Recordable) {
                         State valueState = ((Recordable) value).getState();
                         UUID valueId = valueState.getId();
 
                         Map<UUID, String> cache = LABEL_CACHE.get();
                         boolean isFirst = false;
                         if (cache == null) {
                             cache = new HashMap<UUID, String>();
                             LABEL_CACHE.set(cache);
                             isFirst = true;
                         }
 
                         try {
                             if (cache.containsKey(valueId)) {
                                 valueString = cache.get(valueId);
                                 if (valueString == null) {
                                     valueString = valueId.toString();
                                 }
 
                             } else {
                                 cache.put(valueId, null);
                                 valueString = valueState.getLabel();
                                 cache.put(valueId, valueString);
                             }
 
                         } finally {
                             if (isFirst) {
                                 LABEL_CACHE.remove();
                             }
                         }
 
                     } else if (value instanceof Iterable<?>) {
                         StringBuilder iterableLabel = new StringBuilder();
                         iterableLabel.append("[");
 
                         for (Object item : (Iterable<?>) value) {
                             if (item instanceof Recordable) {
                                 iterableLabel.append(((Recordable) item).getState().getLabel());
                             } else {
                                 iterableLabel.append(item.toString());
                             }
                             iterableLabel.append(", ");
                         }
 
                         if (iterableLabel.length() > 2) {
                             iterableLabel.setLength(iterableLabel.length() - 2);
                         }
                         iterableLabel.append("]");
                         valueString = iterableLabel.toString();
 
                     } else {
                         valueString = value.toString();
                     }
 
                     if (valueString.length() > 0) {
                         label.append(valueString);
                         label.append(' ');
                     }
                 }
             }
 
             if (label.length() > 0) {
                 label.setLength(label.length() - 1);
                 return label.toString();
             }
         }
 
         return getId().toString();
     }
 
     /** Returns a storage item that can be used to preview this state. */
     public StorageItem getPreview() {
         ObjectType type = getType();
         if (type != null) {
             String field = type.getPreviewField();
             if (!ObjectUtils.isBlank(field)) {
                 return (StorageItem) getValue(field);
             }
         }
         return null;
     }
 
     public void prefetch() {
         prefetch(getValues());
     }
 
     private void prefetch(Object object) {
         if (object instanceof Map) {
             for (Object item : ((Map<?, ?>) object).values()) {
                 prefetch(item);
             }
         } else if (object instanceof Iterable) {
             for (Object item : (Iterable<?>) object) {
                 prefetch(item);
             }
         }
     }
 
     /**
      * Returns an instance of the given {@code modificationClass} linked
      * to this state.
      */
     public <T> T as(Class<T> objectClass) {
         @SuppressWarnings("unchecked")
         T object = (T) linkedObjects.get(objectClass);
         if (object == null) {
             object = TypeDefinition.getInstance(objectClass).newInstance();
             ((Recordable) object).setState(this);
             copyRawValuesToJavaFields(object);
         }
         return object;
     }
 
     /** Returns the original object. */
     public Object getOriginalObject() {
         for (Object object : linkedObjects.values()) {
             if (!(object instanceof Modification)) {
                 return object;
             }
         }
         throw new IllegalStateException("No original object!");
     }
 
     /** Returns a set of all objects that can be used with this state. */
     @SuppressWarnings("all")
     public Collection<Record> getObjects() {
         List<Record> objects = new ArrayList<Record>();
         objects.addAll((Collection) linkedObjects.values());
         return objects;
     }
 
     /**
      * Resolves all references to other objects in this state. This method
      * shouldn't be used directly, because it's called automatically on
      * demand using {@link LazyLoadEnhancer}.
      */
     public void resolveReferences() {
         if ((flags & IS_ALL_RESOLVED_FLAG) > 0) {
             return;
         }
 
         synchronized (this) {
             if ((flags & IS_ALL_RESOLVED_FLAG) > 0) {
                 return;
             }
 
             flags |= IS_ALL_RESOLVED_FLAG;
 
             if (linkedObjects.isEmpty()) {
                 return;
             }
 
             Object object = linkedObjects.values().iterator().next();
             Map<UUID, Object> references = StateValueUtils.resolveReferences(getDatabase(), object, rawValues.values());
             Map<String, Object> resolved = new HashMap<String, Object>();
 
             for (Map.Entry<? extends String, ? extends Object> e : rawValues.entrySet()) {
                 UUID id = StateValueUtils.toIdIfReference(e.getValue());
                 if (id != null) {
                     resolved.put(e.getKey(), references.get(id));
                 }
             }
 
             for (Map.Entry<String, Object> e : resolved.entrySet()) {
                 put(e.getKey(), e.getValue());
             }
         }
     }
 
     /**
      * Returns {@code true} if the field values in this state is valid.
      * The validation rules are typically read from annotations such as
      * {@link Recordable.FieldRequired}.
      */
     public boolean validate() {
         ObjectType type = getType();
         if (type != null) {
             for (ObjectField field : type.getFields()) {
                 field.validate(this);
                 validateValue(get(field.getInternalName()), field.isEmbedded());
             }
         }
 
         DatabaseEnvironment environment = getDatabase().getEnvironment();
         for (ObjectField field : environment.getFields()) {
             field.validate(this);
         }
 
         return !hasAnyErrors();
     }
 
     private void validateValue(Object value, boolean embedded) {
         if (value instanceof Map) {
             value = ((Map<?, ?>) value).values();
         }
 
         if (value instanceof Iterable) {
             for (Object item : (Iterable<?>) value) {
                 validateValue(item, embedded);
             }
 
         } else if (value instanceof Recordable) {
             State valueState = ((Recordable) value).getState();
 
             if (embedded) {
                 valueState.validate();
 
             } else {
                 ObjectType valueType = valueState.getType();
                if (valueType != null && valueType.isEmbedded()) {
                     valueState.validate();
                 }
             }
         }
     }
 
     private void copyJavaFieldsToRawValues() {
         DatabaseEnvironment environment = getDatabase().getEnvironment();
 
         for (Object object : linkedObjects.values()) {
             Class<?> objectClass = object.getClass();
             ObjectType type = environment.getTypeByClass(objectClass);
             if (type == null) {
                 continue;
             }
 
             for (ObjectField field : type.getFields()) {
                 Field javaField = field.getJavaField(objectClass);
                 if (javaField == null ||
                         !javaField.getDeclaringClass().getName().equals(field.getJavaDeclaringClassName())) {
                     continue;
                 }
 
                 try {
                     rawValues.put(field.getInternalName(), javaField.get(object));
                 } catch (IllegalAccessException ex) {
                     throw new IllegalStateException(ex);
                 }
             }
         }
     }
 
     void copyRawValuesToJavaFields(Object object) {
         Class<?> objectClass = object.getClass();
         ObjectType type = getDatabase().getEnvironment().getTypeByClass(objectClass);
         if (type == null) {
             return;
         }
 
         for (ObjectField field : type.getFields()) {
             String key = field.getInternalName();
             Object value = StateValueUtils.toJavaValue(getDatabase(), object, field, field.getInternalType(), rawValues.get(key));
             rawValues.put(key, value);
 
             Field javaField = field.getJavaField(objectClass);
             if (javaField != null) {
                 setJavaField(field, javaField, object, key, value);
             }
         }
     }
 
     private void setJavaField(
             ObjectField field,
             Field javaField,
             Object object,
             String key,
             Object value) {
 
         if (!javaField.getDeclaringClass().getName().equals(field.getJavaDeclaringClassName())) {
             return;
         }
 
         try {
             Type javaFieldType = javaField.getGenericType();
             Exception fieldSetError = null;
             if ((!javaField.getType().isPrimitive() &&
                     !Number.class.isAssignableFrom(javaField.getType())) &&
                     (javaFieldType instanceof Class ||
                     ((value instanceof StateValueList ||
                     value instanceof StateValueMap ||
                     value instanceof StateValueSet) &&
                     ObjectField.RECORD_TYPE.equals(field.getInternalItemType())))) {
                 try {
                     javaField.set(object, value);
                     return;
                 } catch (IllegalArgumentException ex) {
                     fieldSetError = ex;
                 }
             }
 
             Object converted = javaFieldType instanceof TypeVariable ? value : ObjectUtils.to(javaFieldType, value);
 
             try {
                 javaField.set(object, converted);
             } catch (IllegalArgumentException error) {
                 converted = null;
             }
 
             if (converted == null) {
                 rawValues.put("dari.trash." + key, value);
                 if (LOGGER.isDebugEnabled()) {
                     Class<?> valueClass = value != null ? value.getClass() : null;
                     LOGGER.debug(String.format(
                             "Can't convert [%s] of [%s] to [%s]!",
                             value, valueClass, javaFieldType), fieldSetError);
                 }
             }
 
         } catch (IllegalAccessException ex) {
             throw new IllegalStateException(ex);
         }
     }
 
     @Override
     public void clear() {
         rawValues.clear();
 
         DatabaseEnvironment environment = getDatabase().getEnvironment();
 
         for (Object object : linkedObjects.values()) {
             Class<?> objectClass = object.getClass();
             ObjectType type = environment.getTypeByClass(objectClass);
             if (type == null) {
                 continue;
             }
 
             for (ObjectField field : type.getFields()) {
                 Field javaField = field.getJavaField(objectClass);
                 if (javaField == null) {
                     continue;
                 }
 
                 try {
                     javaField.set(object, ObjectUtils.to(javaField.getGenericType(), null));
                 } catch (IllegalAccessException ex) {
                     throw new IllegalStateException(ex);
                 }
             }
         }
     }
 
     @Override
     public boolean containsKey(Object key) {
         return rawValues.containsKey(key);
     }
 
     @Override
     public boolean containsValue(Object value) {
         copyJavaFieldsToRawValues();
         return rawValues.containsKey(value);
     }
 
     @Override
     public Set<Map.Entry<String, Object>> entrySet() {
         copyJavaFieldsToRawValues();
         return rawValues.entrySet();
     }
 
     @Override
     public Object get(Object key) {
         if (!(key instanceof String)) {
             return null;
         }
 
         resolveReferences();
 
         for (Object object : linkedObjects.values()) {
             Class<?> objectClass = object.getClass();
 
             ObjectType type = getDatabase().getEnvironment().getTypeByClass(objectClass);
             if (type == null) {
                 continue;
             }
 
             ObjectField field = type.getField((String) key);
             if (field == null) {
                 continue;
             }
 
             Field javaField = field.getJavaField(objectClass);
             if (javaField == null) {
                 continue;
             }
 
             Object value;
             try {
                 value = javaField.get(object);
             } catch (IllegalAccessException ex) {
                 throw new IllegalStateException(ex);
             }
 
             rawValues.put(field.getInternalName(), value);
             return value;
         }
 
         return rawValues.get(key);
     }
 
     @Override
     public boolean isEmpty() {
         return false;
     }
 
     @Override
     public Set<String> keySet() {
         return rawValues.keySet();
     }
 
     @Override
     public Object put(String key, Object value) {
         if (key == null) {
             return null;
         }
 
         if (key.startsWith("_")) {
             if (key.equals(StateValueUtils.ID_KEY)) {
                 setId(ObjectUtils.to(UUID.class, value));
             } else if (key.equals(StateValueUtils.TYPE_KEY)) {
                 setTypeId(ObjectUtils.to(UUID.class, value));
             }
             return null;
         }
 
         boolean first =  true;
         for (Object object : linkedObjects.values()) {
             ObjectField field = State.getInstance(object).getField(key);
             if (first) {
                 value = StateValueUtils.toJavaValue(getDatabase(), object, field, field != null ? field.getInternalType() : null, value);
                 first = false;
             }
 
             if (field != null) {
                 Field javaField = field.getJavaField(object.getClass());
                 if (javaField != null) {
                     setJavaField(field, javaField, object, key, value);
                 }
             }
         }
 
         return rawValues.put(key, value);
     }
 
     @Override
     public void putAll(Map<? extends String, ? extends Object> map) {
         if (!linkedObjects.isEmpty()) {
             Object object = linkedObjects.values().iterator().next();
 
             if (object != null && getType() != null && getType().isLazyLoaded()) {
                 for (Map.Entry<? extends String, ? extends Object> e : map.entrySet()) {
                     String key = e.getKey();
                     Object value = e.getValue();
                     if (StateValueUtils.toIdIfReference(value) != null) {
                         rawValues.put(key, value);
                     } else {
                         put(key, value);
                     }
                 }
                 flags &= ~IS_ALL_RESOLVED_FLAG;
                 return;
 
             } else {
                 rawValues.putAll(map);
                 resolveReferences();
             }
         }
 
         for (Map.Entry<? extends String, ? extends Object> e : map.entrySet()) {
             put(e.getKey(), e.getValue());
         }
     }
 
     @Override
     public Object remove(Object key) {
         if (key instanceof String) {
             Object oldValue = put((String) key, null);
             rawValues.remove(key);
             return oldValue;
 
         } else {
             return null;
         }
     }
 
     @Override
     public int size() {
         return rawValues.size() + 2;
     }
 
     @Override
     public Collection<Object> values() {
         copyJavaFieldsToRawValues();
         return rawValues.values();
     }
 
     // --- Object support ---
 
     @Override
     public boolean equals(Object other) {
 
         if (this == other) {
             return true;
 
         } else if (other instanceof State) {
             State otherState = (State) other;
             return getId().equals(otherState.getId());
 
         } else {
             return false;
         }
     }
 
     @Override
     public int hashCode() {
         return getId().hashCode();
     }
 
     @Override
     public String toString() {
         StringBuilder sb = new StringBuilder();
         sb.append("{database=").append(getDatabase());
         sb.append(", status=").append(getStatus());
         sb.append(", id=").append(getId());
         sb.append(", typeId=").append(getTypeId());
         sb.append(", simpleValues=").append(getSimpleValues());
         sb.append(", extras=").append(extras);
         sb.append(", atomicOperations=").append(atomicOperations);
         sb.append(", errors=").append(errors);
         sb.append("}");
         return sb.toString();
     }
 
     // --- JSTL support ---
 
     private transient Map<String, Object> modifications;
 
     public Map<String, Object> getAs() {
         if (modifications == null) {
             modifications = new PullThroughCache<String, Object>() {
                 @Override
                 protected Object produce(String modificationClassName) {
                     Class<?> modificationClass = ObjectUtils.getClassByName(modificationClassName);
                     if (modificationClass != null) {
                         return as(modificationClass);
                     } else {
                         throw new IllegalArgumentException(String.format(
                                 "[%s] isn't a valid class name!", modificationClassName));
                     }
                 }
             };
         }
         return modifications;
     }
     
     // --- Database bridge ---
 
     /** @see Database#beginWrites() */
     public boolean beginWrites() {
         return getDatabase().beginWrites();
     }
 
     /** @see Database#commitWrites() */
     public boolean commitWrites() {
         return getDatabase().commitWrites();
     }
 
     /** @see Database#endWrites() */
     public boolean endWrites() {
         return getDatabase().endWrites();
     }
 
     /**
      * {@linkplain Database#save Saves} this state to the
      * {@linkplain #getDatabase originating database}.
      */
     public void save() {
         getDatabase().save(this);
     }
 
     /**
      * Saves this state {@linkplain Database#beginIsolatedWrites immediately}
      * to the {@linkplain #getDatabase originating database}.
      */
     public void saveImmediately() {
         Database database = getDatabase();
         database.beginIsolatedWrites();
         try {
             database.save(this);
             database.commitWrites();
         } finally {
             database.endWrites();
         }
     }
 
     /**
      * Saves this state {@linkplain Database#commitWritesEventually eventually}
      * to the {@linkplain #getDatabase originating database}.
      */
     public void saveEventually() {
         Database database = getDatabase();
         database.beginWrites();
         try {
             database.save(this);
             database.commitWritesEventually();
         } finally {
             database.endWrites();
         }
     }
 
     /**
      * {@linkplain Database#saveUnsafely Saves} this state to the
      * {@linkplain #getDatabase originating database} without validating
      * the data.
      */
     public void saveUnsafely() {
         getDatabase().saveUnsafely(this);
     }
 
     /**
      * {@linkplain Database#index Indexes} this state data in the
      * {@linkplain #getDatabase originating database}.
      */
     public void index() {
         getDatabase().index(this);
     }
 
     /**
      * {@linkplain Database#delete Deletes} this state from the
      * {@linkplain #getDatabase originating database}.
      */
     public void delete() {
         getDatabase().delete(this);
     }
 
     /**
      * Deletes this state {@linkplain Database#beginIsolatedWrites immediately}
      * from the {@linkplain #getDatabase originating database}.
      */
     public void deleteImmediately() {
         Database database = getDatabase();
         database.beginIsolatedWrites();
         try {
             database.delete(this);
             database.commitWrites();
         } finally {
             database.endWrites();
         }
     }
 
     // --- Deprecated ---
 
     /** @deprecated Use {@link #getSimpleValues} instead. */
     @Deprecated
     public Map<String, Object> getJsonObject() {
         return getSimpleValues();
     }
 
     /** @deprecated Use {@link #getSimpleValues} and {@link ObjectUtils#toJson} instead. */
     @Deprecated
     public String getJsonString() {
         return ObjectUtils.toJson(getSimpleValues());
     }
 
     /** @deprecated Use {@link #setValues} and {@link ObjectUtils#fromJson} instead. */
     @Deprecated
     @SuppressWarnings("unchecked")
     public void setJsonString(String json) {
         setValues((Map<String, Object>) ObjectUtils.fromJson(json));
     }
 
     /** @deprecated Use {@link #setStatus} instead. */
     @Deprecated
     public void markSaved() {
         setStatus(StateStatus.SAVED);
     }
 
     /** @deprecated Use {@link #setStatus} instead. */
     @Deprecated
     public void markDeleted() {
         setStatus(StateStatus.DELETED);
     }
 
     /** @deprecated Use {@link #setStatus} instead. */
     @Deprecated
     public void markReadonly() {
         setStatus(StateStatus.REFERENCE_ONLY);
     }
 
     /** @deprecated Use {@link #isResolveReferenceOnly} instead. */
     @Deprecated
     public boolean isResolveReferenceOnly() {
         return isResolveToReferenceOnly();
     }
 
     /** @deprecated Use {@link #isResolveReferenceOnly} instead. */
     @Deprecated
     public void setResolveReferenceOnly(boolean isResolveReferenceOnly) {
         setResolveToReferenceOnly(isResolveReferenceOnly);
     }
 
     /** @deprecated Use {@link #incrementAtomically} instead. */
     @Deprecated
     public void incrementValue(String name, double value) {
         incrementAtomically(name, value);
     }
 
     /** @deprecated Use {@link #decrementAtomically} instead. */
     @Deprecated
     public void decrementValue(String name, double value) {
         decrementAtomically(name, value);
     }
 
     /** @deprecated Use {@link #addAtomically} instead. */
     @Deprecated
     public void addValue(String name, Object value) {
         addAtomically(name, value);
     }
 
     /** @deprecated Use {@link #removeAtomically} instead. */
     @Deprecated
     public void removeValue(String name, Object value) {
         removeAtomically(name, value);
     }
 
     /** @deprecated Use {@link #replaceAtomically} instead. */
     @Deprecated
     public void replaceValue(String name, Object value) {
         replaceAtomically(name, value);
     }
 
 }
