 package com.psddev.dari.db;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.atomic.AtomicReference;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.psddev.dari.util.CodeUtils;
 import com.psddev.dari.util.ObjectUtils;
 import com.psddev.dari.util.PeriodicCache;
 import com.psddev.dari.util.PullThroughValue;
 import com.psddev.dari.util.Task;
 import com.psddev.dari.util.TypeDefinition;
 
 public class DatabaseEnvironment implements ObjectStruct {
 
     public static final String GLOBAL_FIELDS_FIELD = "globalFields";
     public static final String GLOBAL_INDEXES_FIELD = "globalIndexes";
 
     private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseEnvironment.class);
 
     private final Database database;
     private final boolean initializeClasses;
 
     {
         CodeUtils.addRedefineClassesListener(new CodeUtils.RedefineClassesListener() {
             @Override
             public void redefined(Set<Class<?>> classes) {
                 for (Class<?> c : classes) {
                     if (Recordable.class.isAssignableFrom(c)) {
                         TypeDefinition.Static.invalidateAll();
                         refreshTypes();
                         break;
                     }
                 }
             }
         });
     }
 
     /** Creates a new instance backed by the given {@code database}. */
     public DatabaseEnvironment(Database database, boolean initializeClasses) {
         this.database = database;
         this.initializeClasses = initializeClasses;
     }
 
     /** Creates a new instance backed by the given {@code database}. */
     public DatabaseEnvironment(Database database) {
         this(database, true);
     }
 
     /** Returns the backing database. */
     public Database getDatabase() {
         return database;
     }
 
     // --- Globals and types cache ---
 
     /** Globals are stored at FFFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFFF. */
     private static final UUID GLOBALS_ID = new UUID(-1L, -1L);
 
     /** Field where the root type is stored within the globals. */
     private static final String ROOT_TYPE_FIELD = "rootType";
 
     private volatile State globals;
     private volatile Date lastGlobalsUpdate;
     private volatile Date lastTypesUpdate;
     private volatile TypesCache permanentTypes = new TypesCache();
     private final ThreadLocal<TypesCache> temporaryTypesLocal = new ThreadLocal<TypesCache>();
 
     /** Aggregate of all maps used to cache type information. */
     private static class TypesCache {
 
         public final Map<String, ObjectType> byClassName = new HashMap<String, ObjectType>();
         public final Map<UUID, ObjectType> byId = new HashMap<UUID, ObjectType>();
         public final Map<String, ObjectType> byName = new HashMap<String, ObjectType>();
         public final Map<String, Set<ObjectType>> byGroup = new HashMap<String, Set<ObjectType>>();
         public final Set<UUID> changed = new HashSet<UUID>();
 
         /** Adds the given {@code type} to all type cache maps. */
         public void add(ObjectType type) {
             String className = type.getObjectClassName();
             if (!ObjectUtils.isBlank(className)) {
                 byClassName.put(className, type);
             }
 
             byId.put(type.getId(), type);
 
             String internalName = type.getInternalName();
             if (!ObjectUtils.isBlank(internalName)) {
                 byName.put(internalName, type);
             }
 
             for (String group : type.getGroups()) {
                 Set<ObjectType> groupTypes = byGroup.get(group);
                 if (groupTypes == null) {
                     groupTypes = new HashSet<ObjectType>();
                     byGroup.put(group, groupTypes);
                 }
                 groupTypes.remove(type);
                 groupTypes.add(type);
             }
         }
     }
 
     private final AtomicBoolean bootstrapDone = new AtomicBoolean();
     private final AtomicReference<Thread> bootstrapThread = new AtomicReference<Thread>();
 
     // Bootstraps the globals and types for the first time. Most methods
     // in this class should call this before performing any action.
     private void bootstrap() {
         if (bootstrapDone.get()) {
             return;
         }
 
         Thread currentThread = Thread.currentThread();
         while (true) {
             if (currentThread.equals(bootstrapThread.get())) {
                 return;
             } else if (bootstrapThread.compareAndSet(null, currentThread)) {
                 break;
             } else {
                 synchronized (bootstrapThread) {
                     while (bootstrapThread.get() != null) {
                         try {
                             bootstrapThread.wait();
                         } catch (InterruptedException ex) {
                             return;
                         }
                     }
                 }
             }
         }
 
         try {
 
             // Fetch the globals, which includes a reference to the root
             // type. References to other objects can't be resolved,
             // because the type definitions haven't been loaded yet.
             refreshGlobals();
 
             ObjectType rootType = getRootType();
             if (rootType != null) {
 
                 // This isn't cleared later, because that's done within
                 // {@link refreshTypes} anyway.
                 TypesCache temporaryTypes = temporaryTypesLocal.get();
                 if (temporaryTypes == null) {
                     temporaryTypes = new TypesCache();
                     temporaryTypesLocal.set(temporaryTypes);
                 }
                 temporaryTypes.add(rootType);
                 LOGGER.info(
                         "Root type ID for [{}] is [{}]",
                         getDatabase().getName(),
                         rootType.getId());
             }
 
             // Load all other types based on the root type. Then globals
             // again in case they reference other typed objects. Then
             // types again using the information from the fully resolved
             // globals.
             refreshTypes();
             refreshGlobals();
             refreshTypes();
 
             refresher.scheduleWithFixedDelay(5.0, 5.0);
             bootstrapDone.set(true);
 
         } finally {
             bootstrapThread.set(null);
             synchronized (bootstrapThread) {
                 bootstrapThread.notifyAll();
             }
         }
     }
 
     /** Task for updating the globals and the types periodically. */
     private final Task refresher = new Task(PeriodicCache.TASK_EXECUTOR_NAME, null) {
         @Override
         public void doTask() {
             Database database = getDatabase();
 
             Date newGlobalsUpdate = Query.
                     from(Object.class).
                     where("_id = ?", GLOBALS_ID).
                     using(database).
                     lastUpdate();
             if (newGlobalsUpdate != null &&
                     (lastGlobalsUpdate == null ||
                     newGlobalsUpdate.after(lastGlobalsUpdate))) {
                 refreshGlobals();
             }
 
             Date newTypesUpdate = Query.
                     from(ObjectType.class).
                     using(database).
                     lastUpdate();
             if (newTypesUpdate != null &&
                     (lastTypesUpdate == null ||
                     newTypesUpdate.after(lastTypesUpdate))) {
                 refreshTypes();
             }
         }
     };
 
     /** Immediately refreshes all globals using the backing database. */
     public synchronized void refreshGlobals() {
         bootstrap();
 
         Database database = getDatabase();
         LOGGER.info("Loading globals from [{}]", database.getName());
 
         State newGlobals = State.getInstance(Query.
                 from(Object.class).
                 where("_id = ?", GLOBALS_ID).
                 using(database).
                 first());
         if (newGlobals == null) {
             newGlobals = new State();
             newGlobals.setDatabase(database);
             newGlobals.setId(GLOBALS_ID);
             newGlobals.save();
         }
 
         globals = newGlobals;
         lastGlobalsUpdate = new Date();
         fieldsCache.invalidate();
         indexesCache.invalidate();
     }
 
     /** Immediately refreshes all types using the backing database. */
     public synchronized void refreshTypes() {
         bootstrap();
 
         Database database = getDatabase();
         try {
 
             TypesCache temporaryTypes = temporaryTypesLocal.get();
             if (temporaryTypes == null) {
                 temporaryTypes = new TypesCache();
                 temporaryTypesLocal.set(temporaryTypes);
             }
 
             List<ObjectType> types = Query.
                     from(ObjectType.class).
                     using(database).
                     selectAll();
             int typesSize = types.size();
             LOGGER.info("Loading [{}] types from [{}]", typesSize, database.getName());
 
             // Load all types from the database first.
             for (ObjectType type : types) {
                 type.getFields().size(); // Pre-fetch.
                 temporaryTypes.add(type);
             }
 
             if (initializeClasses) {
 
                 // Make sure that the root type exists.
                 ObjectType rootType = getRootType();
                 State rootTypeState;
 
                 if (rootType != null) {
                     rootTypeState = rootType.getState();
 
                 } else {
                     rootType = new ObjectType();
                     rootTypeState = rootType.getState();
                     rootTypeState.setDatabase(database);
                 }
 
                 Map<String, Object> rootTypeOriginals = rootTypeState.getSimpleValues();
                 UUID rootTypeId = rootTypeState.getId();
                 rootTypeState.setTypeId(rootTypeId);
                rootTypeState.clear();
                 rootType.setObjectClassName(ObjectType.class.getName());
                 rootType.initialize();
                 temporaryTypes.add(rootType);
 
                 try {
                     database.beginWrites();
 
                     // Make the new root type available to other types.
                     temporaryTypes.add(rootType);
                     if (rootTypeState.isNew()) {
                         State globals = getGlobals();
                         globals.put(ROOT_TYPE_FIELD, rootType);
                         globals.save();
 
                     } else if (!rootTypeState.getSimpleValues().equals(rootTypeOriginals)) {
                         temporaryTypes.changed.add(rootTypeId);
                     }
 
                     Set<Class<? extends Recordable>> objectClasses = ObjectUtils.findClasses(Recordable.class);
                     for (Iterator<Class<? extends Recordable>> i = objectClasses.iterator(); i.hasNext(); ) {
                         Class<? extends Recordable> objectClass = i.next();
                         if (objectClass.isAnonymousClass()) {
                             i.remove();
                         }
                     }
 
                     Set<Class<?>> globalModifications = new HashSet<Class<?>>();
                     Map<ObjectType, List<Class<?>>> typeModifications = new HashMap<ObjectType, List<Class<?>>>();
 
                     // Make sure all types are accessible to the rest of the
                     // system as soon as possible, so that references can be
                     // resolved properly later.
                     for (Class<?> objectClass : objectClasses) {
                         ObjectType type = getTypeByClass(objectClass);
 
                         if (type == null) {
                             type = new ObjectType();
                             type.getState().setDatabase(database);
 
                         } else {
                            type.getState().clear();
                         }
 
                         type.setObjectClassName(objectClass.getName());
                         typeModifications.put(type, new ArrayList<Class<?>>());
                         temporaryTypes.add(type);
                     }
 
                     // Separate out all modifications from regular types.
                     for (Class<?> objectClass : objectClasses) {
                         if (!Modification.class.isAssignableFrom(objectClass)) {
                             continue;
                         }
 
                         @SuppressWarnings("unchecked")
                         Set<Class<?>> modifiedClasses = Modification.Static.getModifiedClasses((Class<? extends Modification<?>>) objectClass);
                         if (modifiedClasses.contains(Object.class)) {
                             globalModifications.add(objectClass);
                             continue;
                         }
 
                         for (Class<?> modifiedClass : modifiedClasses) {
                             List<Class<?>> assignableClasses = new ArrayList<Class<?>>();
 
                             for (Class<?> c : objectClasses) {
                                 if (modifiedClass.isAssignableFrom(c)) {
                                     assignableClasses.add(c);
                                 }
                             }
 
                             for (Class<?> assignableClass : assignableClasses) {
                                 ObjectType type = getTypeByClass(assignableClass);
 
                                 if (type != null) {
                                     List<Class<?>> modifications = typeModifications.get(type);
                                     if (modifications == null) {
                                         modifications = new ArrayList<Class<?>>();
                                         typeModifications.put(type, modifications);
                                     }
                                     modifications.add(objectClass);
                                 }
                             }
                         }
                     }
 
                     // Apply global modifications.
                     for (Class<?> modification : globalModifications) {
                         ObjectType.modifyAll(database, modification);
                     }
 
                     // Initialize all types.
                     List<Class<?>> rootTypeModifications = typeModifications.remove(rootType);
                     initializeAndModify(temporaryTypes, rootType, rootTypeModifications);
 
                     if (rootTypeModifications != null) {
                         for (Class<?> modification : rootTypeModifications) {
                             ObjectType t = getTypeByClass(modification);
                             initializeAndModify(temporaryTypes, t, typeModifications.remove(t));
                         }
                     }
 
                     ObjectType fieldType = getTypeByClass(ObjectField.class);
                     List<Class<?>> fieldModifications = typeModifications.remove(fieldType);
                     initializeAndModify(temporaryTypes, fieldType, fieldModifications);
 
                     if (fieldModifications != null) {
                         for (Class<?> modification : fieldModifications) {
                             ObjectType t = getTypeByClass(modification);
                             initializeAndModify(temporaryTypes, t, typeModifications.remove(t));
                         }
                     }
 
                     for (Map.Entry<ObjectType, List<Class<?>>> entry : typeModifications.entrySet()) {
                         initializeAndModify(temporaryTypes, entry.getKey(), entry.getValue());
                     }
 
                     database.commitWrites();
 
                 } finally {
                     database.endWrites();
                 }
             }
 
             // Merge temporary types into new permanent types.
             TypesCache newPermanentTypes = new TypesCache();
 
             for (ObjectType type : permanentTypes.byId.values()) {
                 newPermanentTypes.add(type);
             }
 
             for (ObjectType type : temporaryTypes.byId.values()) {
                 newPermanentTypes.add(type);
             }
 
             newPermanentTypes.changed.addAll(temporaryTypes.changed);
             newPermanentTypes.changed.addAll(permanentTypes.changed);
 
             permanentTypes = newPermanentTypes;
             lastTypesUpdate = new Date();
 
         } finally {
             temporaryTypesLocal.remove();
         }
 
         ObjectType singletonType = getTypeByClass(Singleton.class);
 
         if (singletonType != null) {
             for (ObjectType type : singletonType.findConcreteTypes()) {
                 if (!Query.fromType(type).hasMoreThan(0)) {
                     try {
                         State.getInstance(type.createObject(null)).save();
                     } catch (Exception error) {
                         LOGGER.warn(String.format("Can't save [%s] singleton!", type.getLabel()), error);
                     }
                 }
             }
         }
     }
 
     private static void initializeAndModify(TypesCache temporaryTypes, ObjectType type, List<Class<?>> modifications) {
         State typeState = type.getState();
         Map<String, Object> typeOriginals = typeState.getSimpleValues();
 
         try {
             type.initialize();
             temporaryTypes.add(type);
 
             // Apply type-specific modifications.
             if (modifications != null) {
                 for (Class<?> modification : modifications) {
                     type.modify(modification);
                 }
             }
 
         } catch (IncompatibleClassChangeError ex) {
             LOGGER.info(
                     "Skipped initializing [{}] because its class is in an inconsistent state! ([{}])",
                     type.getInternalName(),
                     ex.getMessage());
         }
 
         if (typeState.isNew()) {
             type.save();
 
         } else if (!typeState.getSimpleValues().equals(typeOriginals)) {
             temporaryTypes.changed.add(type.getId());
         }
     }
 
     /**
      * Returns all global values.
      *
      * @return May be {@code null}.
      */
     public State getGlobals() {
         bootstrap();
         return globals;
     }
 
     // Returns the root type from the globals.
     private ObjectType getRootType() {
         State globals = getGlobals();
 
         if (globals != null) {
             Object rootType = globals.get(ROOT_TYPE_FIELD);
 
             if (rootType == null) {
                 rootType = globals.get("rootRecordType");
             }
 
             if (rootType instanceof ObjectType) {
                 return (ObjectType) rootType;
             }
         }
 
         return null;
     }
 
     // --- ObjectStruct support ---
 
     @Override
     public DatabaseEnvironment getEnvironment() {
         return this;
     }
 
     @Override
     public List<ObjectField> getFields() {
         return new ArrayList<ObjectField>(fieldsCache.get().values());
     }
 
     private final PullThroughValue<Map<String, ObjectField>> fieldsCache = new PullThroughValue<Map<String, ObjectField>>() {
         @Override
         @SuppressWarnings("unchecked")
         protected Map<String, ObjectField> produce() {
             State globals = getGlobals();
             Object definitions = globals != null ? globals.get(GLOBAL_FIELDS_FIELD) : null;
             return ObjectField.Static.convertDefinitionsToInstances(
                     DatabaseEnvironment.this,
                     definitions instanceof List ?
                             (List<Map<String, Object>>) definitions :
                             null);
         }
     };
 
     @Override
     public ObjectField getField(String name) {
         return fieldsCache.get().get(name);
     }
 
     @Override
     public void setFields(List<ObjectField> fields) {
         getGlobals().put(GLOBAL_FIELDS_FIELD, ObjectField.Static.convertInstancesToDefinitions(fields));
         fieldsCache.invalidate();
     }
 
     @Override
     public List<ObjectIndex> getIndexes() {
         return new ArrayList<ObjectIndex>(indexesCache.get().values());
     }
 
     private final PullThroughValue<Map<String, ObjectIndex>> indexesCache = new PullThroughValue<Map<String, ObjectIndex>>() {
         @Override
         @SuppressWarnings("unchecked")
         protected Map<String, ObjectIndex> produce() {
             State globals = getGlobals();
             Object definitions = globals != null ? globals.get(GLOBAL_INDEXES_FIELD) : null;
             return ObjectIndex.Static.convertDefinitionsToInstances(
                     DatabaseEnvironment.this,
                     definitions instanceof List ?
                             (List<Map<String, Object>>) definitions :
                             null);
         }
     };
 
     @Override
     public ObjectIndex getIndex(String name) {
         return indexesCache.get().get(name);
     }
 
     @Override
     public void setIndexes(List<ObjectIndex> indexes) {
         getGlobals().put(GLOBAL_INDEXES_FIELD, ObjectIndex.Static.convertInstancesToDefinitions(indexes));
         indexesCache.invalidate();
     }
 
     /**
      * Initializes the given {@code objectClasses} so that they are
      * usable as {@linkplain ObjectType types}.
      */
     public void initializeTypes(Iterable<Class<?>> objectClasses) {
         bootstrap();
 
         Set<String> classNames = new HashSet<String>();
         for (Class<?> objectClass : objectClasses) {
             classNames.add(objectClass.getName());
         }
 
         for (ObjectType type : getTypes()) {
             UUID id = type.getId();
             if (classNames.contains(type.getObjectClassName())) {
                 TypesCache temporaryTypes = temporaryTypesLocal.get();
                 if ((temporaryTypes != null &&
                         temporaryTypesLocal.get().changed.contains(id)) ||
                         permanentTypes.changed.contains(id)) {
                     type.save();
                 }
             }
         }
     }
 
     /**
      * Returns all types.
      * @return Never {@code null}. May be modified without any side effects.
      */
     public Set<ObjectType> getTypes() {
         bootstrap();
 
         Set<ObjectType> types = new HashSet<ObjectType>();
 
         TypesCache temporaryTypes = temporaryTypesLocal.get();
         if (temporaryTypes != null) {
             types.addAll(temporaryTypes.byId.values());
         }
 
         types.addAll(permanentTypes.byId.values());
 
         return types;
     }
 
     /**
      * Returns the type associated with the given {@code id}.
      * @return May be {@code null}.
      */
     public ObjectType getTypeById(UUID id) {
         bootstrap();
 
         TypesCache temporaryTypes = temporaryTypesLocal.get();
         if (temporaryTypes != null) {
             ObjectType type = temporaryTypes.byId.get(id);
             if (type != null) {
                 return type;
             }
         }
 
         return permanentTypes.byId.get(id);
     }
 
     /**
      * Returns the type associated with the given {@code objectClass}.
      * @return May be {@code null}.
      */
     public ObjectType getTypeByName(String name) {
         bootstrap();
 
         TypesCache temporaryTypes = temporaryTypesLocal.get();
         if (temporaryTypes != null) {
             ObjectType type = temporaryTypes.byName.get(name);
             if (type != null) {
                 return type;
             }
         }
 
         return permanentTypes.byName.get(name);
     }
 
     /**
      * Returns a set of types associated with the given {@code group}.
      * @return Never {@code null}. May be modified without any side effects.
      */
     public Set<ObjectType> getTypesByGroup(String group) {
         bootstrap();
 
         TypesCache temporaryTypes = temporaryTypesLocal.get();
         Set<ObjectType> tTypes;
         if (temporaryTypes != null) {
             tTypes = temporaryTypes.byGroup.get(group);
         } else {
             tTypes = null;
         }
 
         Set<ObjectType> pTypes = permanentTypes.byGroup.get(group);
 
         if (tTypes == null) {
             return pTypes == null ?
                     new HashSet<ObjectType>() :
                     new HashSet<ObjectType>(pTypes);
 
         } else {
             tTypes = new HashSet<ObjectType>(tTypes);
             if (pTypes != null) {
                 tTypes.addAll(pTypes);
             }
             return tTypes;
         }
     }
 
     /**
      * Returns the type associated with the given {@code objectClass}.
      * @return May be {@code null}.
      */
     public ObjectType getTypeByClass(Class<?> objectClass) {
         bootstrap();
 
         String className = objectClass.getName();
         TypesCache temporaryTypes = temporaryTypesLocal.get();
 
         if (temporaryTypes != null) {
             ObjectType type = temporaryTypes.byClassName.get(className);
             if (type != null) {
                 return type;
             }
         }
 
         return permanentTypes.byClassName.get(className);
     }
 
     /**
      * Creates an object represented by the given {@code typeId} and
      * {@code id}.
      */
     public Object createObject(UUID typeId, UUID id) {
         bootstrap();
 
         Class<?> objectClass = null;
         ObjectType type = null;
         if (typeId != null && !GLOBALS_ID.equals(id)) {
 
             if (typeId.equals(id)) {
                 objectClass = ObjectType.class;
 
             } else {
                 type = getTypeById(typeId);
                 if (type != null) {
                     objectClass = type.isAbstract() ?
                             Record.class :
                             type.getObjectClass();
                 }
             }
         }
 
         boolean hasClass = true;
         if (objectClass == null) {
             objectClass = Record.class;
             hasClass = false;
         }
 
         Object object = TypeDefinition.getInstance(objectClass).newInstance();
         State state = State.getInstance(object);
         state.setDatabase(getDatabase());
         state.setId(id);
         state.setTypeId(typeId);
 
         if (type != null && !hasClass) {
             for (ObjectField field : type.getFields()) {
                 Object defaultValue = field.getDefaultValue();
                 if (defaultValue != null) {
                     state.put(field.getInternalName(), defaultValue);
                 }
             }
         }
 
         return object;
     }
 
     // --- Deprecated ---
 
     /** @deprecated Use {@link #getGlobals} instead. */
     @Deprecated
     public Object getGlobal(String key) {
         State globals = getGlobals();
         return globals != null ? globals.getValue(key) : null;
     }
 
     /** @deprecated Use {@link #getGlobals} instead. */
     @Deprecated
     public void putGlobal(String key, Object value) {
         State globals = getGlobals();
         globals.putValue(key, value);
         globals.save();
     }
 }
