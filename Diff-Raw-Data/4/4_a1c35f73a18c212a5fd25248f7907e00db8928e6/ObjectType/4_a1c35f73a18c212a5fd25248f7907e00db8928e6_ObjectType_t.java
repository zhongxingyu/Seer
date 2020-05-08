 package com.psddev.dari.db;
 
 import com.psddev.dari.util.ObjectUtils;
 import com.psddev.dari.util.PullThroughCache;
 import com.psddev.dari.util.PullThroughValue;
 import com.psddev.dari.util.StringUtils;
 import com.psddev.dari.util.TypeDefinition;
 
 import java.lang.annotation.Annotation;
 import java.lang.annotation.Documented;
 import java.lang.annotation.ElementType;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.annotation.Target;
 import java.lang.reflect.Field;
 import java.lang.reflect.Modifier;
 import java.util.ArrayDeque;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /** Represents how the {@link Record}s are structured. */
 @Record.LabelFields("name")
 public class ObjectType extends Record implements ObjectStruct {
 
     private static final String COMMON_NAME_DATA = "Data";
     private static final String COMMON_NAME_MODIFICATION = "Modification";
 
     private static final Logger
             LOGGER = LoggerFactory.getLogger(ObjectType.class);
 
     // Cache that contains record and field annotation processors.
     private static Map<Class<?>, Object>
             ANNOTATION_PROCESSORS = new PullThroughCache<Class<?>, Object>() {
 
         @Override
         protected Object produce(Class<?> processorClass) {
             return TypeDefinition.getInstance(processorClass).newInstance();
         }
     };
 
     @DisplayName("Display Name")
     @Indexed
     @InternalName("name")
     @Required
     private String displayName;
 
     @Indexed(unique = true)
     @Required
     private String internalName;
 
     private List<String> labelFields;
     private String previewField;
     private boolean isAbstract;
     private boolean isEmbedded;
     private boolean denormalized;
     private Set<String> denormalizedFields;
     private Set<String> groups;
     private List<Map<String, Object>> fields;
     private List<Map<String, Object>> indexes;
 
     private String sourceDatabaseClassName;
     private String sourceDatabaseName;
 
     @DisplayName("Java Object Class")
     @Indexed(unique = true)
     @InternalName("java.objectClass")
     private String objectClassName;
 
     private Set<String> java$modificationClasses;
     private List<String> java$superClasses;
 
     @DisplayName("Java Assignable Classes")
     @InternalName("java.assignableClasses")
     private Set<String> assignableClassNames;
 
     private transient Boolean isLazyLoaded;
 
     @SuppressWarnings("deprecation")
     private static void updateFieldsAndIndexes(
             ObjectType type,
             Database database,
             DatabaseEnvironment environment,
             TypeDefinition<?> definition,
             List<ObjectField> globalFields,
             List<ObjectIndex> globalIndexes,
             List<ObjectField> localFields,
             List<ObjectIndex> localIndexes,
             boolean hasGlobalChanges) {
 
         Object defaultObject = null;
         try {
             defaultObject = definition.newInstance();
         } catch (Exception ex) {
             LOGGER.debug(String.format(
                     "Can't create an instance of [%s] to get the default values!",
                     definition.getObjectClass().getName()), ex);
         }
 
         FieldInternalNamePrefix prefixAnnotation = definition.getObjectClass().getAnnotation(FieldInternalNamePrefix.class);
         String prefix = prefixAnnotation != null ? prefixAnnotation.value() : "";
 
         for (Map.Entry<String, List<Field>> entry : definition.getAllSerializableFields().entrySet()) {
             Field javaField = entry.getValue().get(entry.getValue().size() - 1);
 
             if (javaField.isAnnotationPresent(FieldIgnored.class)) {
                 continue;
             }
 
             Ignored ignored = javaField.getAnnotation(Ignored.class);
             if (ignored != null && ignored.value()) {
                 continue;
             }
 
             String internalName = entry.getKey().replace('$', '.');
             String declaringClass = javaField.getDeclaringClass().getName();
 
             FieldInternalName fieldInternalNameAnnotation = javaField.getAnnotation(FieldInternalName.class);
             if (fieldInternalNameAnnotation != null) {
                 internalName = fieldInternalNameAnnotation.value();
             }
 
             InternalName internalNameAnnotation = javaField.getAnnotation(InternalName.class);
             if (internalNameAnnotation != null) {
                 internalName = internalNameAnnotation.value();
             }
 
             internalName = prefix + internalName;
 
             List<ObjectField> fields;
             List<ObjectIndex> indexes;
 
             if (javaField.isAnnotationPresent(FieldGlobal.class)) {
                 fields = globalFields;
                 indexes = globalIndexes;
                 hasGlobalChanges = true;
 
             } else {
                 fields = localFields;
                 indexes = localIndexes;
             }
 
             // Field definition.
             for (Iterator<ObjectField> i = fields.iterator(); i.hasNext(); ) {
                 ObjectField field = i.next();
                 if (internalName.equals(field.getInternalName())) {
                     i.remove();
                     break;
                 }
             }
 
             ObjectField field = new ObjectField(type != null ? type : environment, null);
             field.getState().setDatabase(database);
             field.setInternalName(internalName);
             field.setInternalType(environment, definition.getObjectClass(), javaField.getGenericType());
             field.setJavaFieldName(javaField.getName());
             field.setJavaDeclaringClassName(declaringClass);
 
             if (defaultObject != null &&
                     !ObjectField.DATE_TYPE.equals(field.getInternalType())) {
                 try {
                     field.setDefaultValue(javaField.get(defaultObject));
                 } catch (Exception ex) {
                     LOGGER.debug(String.format(
                             "Can't get the default value of [%s] from an instance of [%s]!",
                             internalName, defaultObject.getClass().getName()), ex);
                 }
             }
 
             field.setDeprecated(javaField.isAnnotationPresent(Deprecated.class));
 
             for (Annotation annotation : javaField.getAnnotations()) {
                 ObjectField.AnnotationProcessorClass processorClass = annotation.annotationType().getAnnotation(ObjectField.AnnotationProcessorClass.class);
                 if (processorClass != null) {
                     @SuppressWarnings("unchecked")
                     ObjectField.AnnotationProcessor<Annotation> processor = (ObjectField.AnnotationProcessor<Annotation>) ANNOTATION_PROCESSORS.get(processorClass.value());
                     processor.process(type, field, annotation);
                 }
             }
 
             fields.add(field);
 
             // Index definition.
             String[] extraFields = null;
             boolean isUnique = false;
             boolean caseSensitive = false;
 
             FieldUnique uniqueAnnotation = javaField.getAnnotation(FieldUnique.class);
             if (uniqueAnnotation != null) {
                 extraFields = new String[0];
                 isUnique = true;
             }
 
             FieldIndexed fieldIndexedAnnotation = javaField.getAnnotation(FieldIndexed.class);
             if (fieldIndexedAnnotation != null) {
                 extraFields = fieldIndexedAnnotation.extraFields();
                 isUnique = isUnique || fieldIndexedAnnotation.isUnique();
             }
 
             Indexed indexedAnnotation = javaField.getAnnotation(Indexed.class);
             if (indexedAnnotation != null) {
                 extraFields = indexedAnnotation.extraFields();
                 isUnique = isUnique || indexedAnnotation.unique() || indexedAnnotation.isUnique();
                 caseSensitive = indexedAnnotation.caseSensitive();
             }
 
             if (extraFields != null) {
                 for (Iterator<ObjectIndex> i = indexes.iterator(); i.hasNext(); ) {
                     ObjectIndex index = i.next();
                     if (internalName.equals(index.getField())) {
                         i.remove();
                         break;
                     }
                 }
 
                 List<String> indexedFields = new ArrayList<String>();
                 indexedFields.add(internalName);
                 Collections.addAll(indexedFields, extraFields);
 
                 ObjectIndex newIndex = new ObjectIndex(type != null ? type : environment, null);
                 newIndex.setFields(indexedFields);
                 newIndex.setType(field.getInternalItemType());
                 newIndex.setUnique(isUnique);
                 newIndex.setCaseSensitive(caseSensitive);
                 newIndex.setJavaDeclaringClassName(declaringClass);
                 newIndex.getOptions().putAll(new ObjectField(field.getParent(), field.toDefinition()).getOptions());
                 indexes.add(newIndex);
             }
         }
 
         if (hasGlobalChanges) {
             environment.setFields(globalFields);
             environment.setIndexes(globalIndexes);
         }
     }
 
     /**
      * Modifies all the type definitions with the reflection data from
      * the given {@code modificationClass}.
      */
     public static void modifyAll(Database database, Class<?> modificationClass) {
         DatabaseEnvironment environment = database.getEnvironment();
         List<ObjectField> globalFields = environment.getFields();
         List<ObjectIndex> globalIndexes = environment.getIndexes();
 
         TypeDefinition<?> definition = TypeDefinition.getInstance(modificationClass);
         updateFieldsAndIndexes(
                 null, database, environment, definition, globalFields,
                 globalIndexes, globalFields, globalIndexes, true);
     }
 
     /** Returns the display name. */
     public String getDisplayName() {
         return displayName;
     }
 
     /** Sets the display name. */
     public void setDisplayName(String displayName) {
         this.displayName = displayName;
     }
 
     /** Returns the internal name. */
     public String getInternalName() {
         String name = internalName;
         if (ObjectUtils.isBlank(name)) {
             name = getObjectClassName();
             if (ObjectUtils.isBlank(name)) {
                 name = getId().toString();
             }
         }
         return name;
     }
 
     /** Sets the internal name. */
     public void setInternalName(String internalName) {
         this.internalName = internalName;
     }
 
     /**
      * Returns the list of field names that are combined to create labels
      * for the objects of this type.
      */
     public List<String> getLabelFields() {
         if (labelFields == null) {
             labelFields = new ArrayList<String>();
         }
         return labelFields;
     }
 
     /**
      * Sets the list of field names that are combined to create labels
      * for the objects of this type.
      */
     public void setLabelFields(List<String> fields) {
         this.labelFields = fields;
     }
 
     /**
      * Returns the field name used to create the previews of the objects
      * represented by this type.
      */
     public String getPreviewField() {
         return previewField;
     }
 
     /**
      * Sets the field name used to create the previews of the objects
      * represented by this type.
      */
     public void setPreviewField(String field) {
         this.previewField = field;
     }
 
     /** Returns {@code true} if the objects of this type cannot be created. */
     public boolean isAbstract() {
         return isAbstract;
     }
 
     /** Sets whether the objects of this type cannot be created. */
     public void setAbstract(boolean isAbstract) {
         this.isAbstract = isAbstract;
     }
 
     /**
      * Returns {@code true} if the objects of this type can only be embedded
      * within other objects.
      */
     public boolean isEmbedded() {
         return isEmbedded;
     }
 
     /**
      * Sets whether the objects of this type can only be embedded
      * within other objects.
      */
     public void setEmbedded(boolean isEmbedded) {
         this.isEmbedded = isEmbedded;
     }
 
     /**
      * Returns {@code true} if the objects of this type is always denormalized
      * within other objects.
      */
     public boolean isDenormalized() {
         return denormalized;
     }
 
     /**
      * Sets whether the objects of this type is always denormalized
      * within other objects.
      */
     public void setDenormalized(boolean denormalized) {
         this.denormalized = denormalized;
     }
 
     /** Returns the set of all denormalized field names. */
     public Set<String> getDenormalizedFields() {
         if (denormalizedFields == null) {
             denormalizedFields = new HashSet<String>();
         }
         return denormalizedFields;
     }
 
     /** Sets the set of all denormalized field names. */
     public void setDenormalizedFields(Set<String> denormalizedFields) {
         this.denormalizedFields = denormalizedFields;
     }
 
     /**
      * Returns {@code true} if the objects of this type can be saved to the
      * database.
      */
     public boolean isConcrete() {
         return !(isAbstract() || isEmbedded());
     }
 
     /** Returns the list of groups. */
     public Set<String> getGroups() {
         if (groups == null) {
             groups = new LinkedHashSet<String>();
         }
         return groups;
     }
 
     /** Sets the list of groups. */
     public void setGroups(Set<String> groups) {
         this.groups = groups;
     }
 
     @Override
     public DatabaseEnvironment getEnvironment() {
         return getState().getDatabase().getEnvironment();
     }
 
     /** Returns a list of all fields. */
     public List<ObjectField> getFields() {
         return new ArrayList<ObjectField>(fieldsCache.get().values());
     }
 
     private final transient PullThroughValue<Map<String, ObjectField>> fieldsCache = new PullThroughValue<Map<String, ObjectField>>() {
         @Override
         protected Map<String, ObjectField> produce() {
             return ObjectField.Static.convertDefinitionsToInstances(ObjectType.this, fields);
         }
     };
 
     /** Sets the list of all fields. */
     public void setFields(List<ObjectField> fields) {
         this.fields = ObjectField.Static.convertInstancesToDefinitions(fields);
         fieldsCache.invalidate();
     }
 
     /** Returns the field with the given {@code name}. */
     public ObjectField getField(String name) {
        if (name == null) {
            return null;
        }

         int slashAt = name.indexOf('/');
 
         if (slashAt < 0) {
             return fieldsCache.get().get(name);
         }
 
         ObjectField field = fieldsCache.get().get(name.substring(0, slashAt));
 
         if (field != null) {
             for (ObjectType type : field.getTypes()) {
                 ObjectField f = type.getField(name.substring(slashAt + 1));
 
                 if (f != null) {
                     return f;
                 }
             }
         }
 
         return null;
     }
 
     /** Returns all fields that are indexed. */
     public List<ObjectField> getIndexedFields() {
         Set<String> indexed = new HashSet<String>();
 
         for (ObjectIndex index : getIndexes()) {
             List<String> fields = index.getFields();
 
             if (fields != null) {
                 indexed.addAll(fields);
             }
         }
 
         List<ObjectField> fields = getFields();
 
         for (Iterator<ObjectField> i = fields.iterator(); i.hasNext(); ) {
             ObjectField field = i.next();
 
             if (!indexed.contains(field.getInternalName())) {
                 i.remove();
             }
         }
 
         return fields;
     }
 
     /** Returns a list of all the indexes. */
     public List<ObjectIndex> getIndexes() {
         return new ArrayList<ObjectIndex>(indexesCache.get().values());
     }
 
     public ObjectIndex getIndexByFields(String... names) {
         if (names != null && names.length > 0) {
             List<String> namesList = Arrays.asList(names);
 
             for (ObjectIndex index : getIndexes()) {
                 if (namesList.equals(index.getFields())) {
                     return index;
                 }
             }
         }
 
         return null;
     }
 
     private final transient PullThroughValue<Map<String, ObjectIndex>> indexesCache = new PullThroughValue<Map<String, ObjectIndex>>() {
         @Override
         protected Map<String, ObjectIndex> produce() {
             return ObjectIndex.Static.convertDefinitionsToInstances(ObjectType.this, indexes);
         }
     };
 
     /** Sets the list of all indexes. */
     public void setIndexes(List<ObjectIndex> indexes) {
         this.indexes = ObjectIndex.Static.convertInstancesToDefinitions(indexes);
         indexesCache.invalidate();
     }
 
     /** Returns the index with the given {@code name}. */
     public ObjectIndex getIndex(String name) {
         ObjectIndex index = indexesCache.get().get(name);
         if (index == null && name.indexOf('/') != -1) {
             DatabaseEnvironment environment = getState().getDatabase().getEnvironment();
             String field = "";
             for (String part : name.split("/")) {
                 if (environment.getTypeByName(part) == null) {
                     field += part + "/";
                 }
             }
             field = field.substring(0, field.length() - 1);
 
             index = getEmbeddedIndex(field, this);
             if (index != null) {
                 // create a new index with the current type info
                 ObjectIndex embeddedIndex = new ObjectIndex(this, null);
                 embeddedIndex.setName(name);
 
                 // get the declaring class of the first level field name
                 int slashAt = name.indexOf('/');
                 String firstLevelFieldName = name.substring(0, slashAt);
                 ObjectField firstLevelField = getField(firstLevelFieldName);
                 if (firstLevelField != null && firstLevelField.getJavaDeclaringClassName() != null) {
                     embeddedIndex.setJavaDeclaringClassName(firstLevelField.getJavaDeclaringClassName());
                 } else {
                     embeddedIndex.setJavaDeclaringClassName(getObjectClassName());
                 }
 
                 embeddedIndex.setField(field);
 
                 // copy the type and isUnique fields from the old index
                 embeddedIndex.setType(index.getType());
                 embeddedIndex.setUnique(index.isUnique());
 
                 index = embeddedIndex;
             }
         }
         return index;
     }
 
     private ObjectIndex getEmbeddedIndex(String fullName, ObjectType type) {
         String name = null;
         int slashIndex = fullName.indexOf('/');
         if (slashIndex == -1) {
             return type.getIndex(fullName);
         } else {
             name = fullName.substring(0, slashIndex);
         }
 
         ObjectIndex index = null;
         ObjectField field = type.getField(name);
         if (field != null) {
             for (ObjectType valueType : field.findConcreteTypes()) {
                 if (field.isEmbedded() || valueType.isEmbedded()) {
                     index = getEmbeddedIndex(fullName.substring(
                             slashIndex+1), valueType);
                     if (index != null) {
                         break;
                     }
                 }
             }
         }
         return index;
     }
 
     /** Returns name of the class used to create objects of this type. */
     public String getObjectClassName() {
         return "com.psddev.dari.db.RecordType".equals(objectClassName)
                 ? ObjectType.class.getName()
                 : objectClassName;
     }
 
     /** Sets name of the class used to create objects of this type. */
     public void setObjectClassName(String objectClassName) {
         this.objectClassName = objectClassName;
     }
 
     /** Returns the class used to create objects of this type. */
     public Class<?> getObjectClass() {
         return ObjectUtils.getClassByName(getObjectClassName());
     }
 
     /** Returns the source database class name. */
     public String getSourceDatabaseClassName() {
         return sourceDatabaseClassName;
     }
 
     /** Sets the source database class name. */
     public void setSourceDatabaseClassName(String sourceDatabaseClassName) {
         this.sourceDatabaseClassName = sourceDatabaseClassName;
     }
 
     /** Returns the source database name. */
     public String getSourceDatabaseName() {
         return sourceDatabaseName;
     }
 
     /** Sets the source database name. */
     public void setSourceDatabaseName(String sourceDatabaseName) {
         this.sourceDatabaseName = sourceDatabaseName;
     }
 
     /**
      * Returns the source database.
      *
      * @return May be {@code null}.
      */
     @SuppressWarnings("unchecked")
     public Database getSourceDatabase() {
         String name = getSourceDatabaseName();
 
         if (!ObjectUtils.isBlank(name)) {
             return Database.Static.getInstance(name);
 
         } else {
             Class<?> dbClass = ObjectUtils.getClassByName(getSourceDatabaseClassName());
 
             if (dbClass != null &&
                     Database.class.isAssignableFrom(dbClass)) {
                 return Database.Static.getFirst((Class<? extends Database>) dbClass);
             }
         }
 
         return null;
     }
 
     /** Returns the set of modification class names. */
     public Set<String> getModificationClassNames() {
         if (java$modificationClasses == null) {
             java$modificationClasses = new LinkedHashSet<String>();
         }
         return java$modificationClasses;
     }
 
     /** Sets the set of modification class names. */
     public void setModificationClasses(Set<String> modificationClasses) {
         this.java$modificationClasses = modificationClasses;
     }
 
     /** Returns the list of super class names. */
     public List<String> getSuperClassNames() {
         if (java$superClasses == null) {
             java$superClasses = new ArrayList<String>();
         }
         return java$superClasses;
     }
 
     /** Sets the list of super class names. */
     public void setSuperClassNames(List<String> superClassNames) {
         this.java$superClasses = superClassNames;
     }
 
     /**
      * Returns the list of class names whose instances can be assigned
      * from the objects of this type.
      */
     public Set<String> getAssignableClassNames() {
         if (assignableClassNames == null) {
             assignableClassNames = new LinkedHashSet<String>();
         }
         return assignableClassNames;
     }
 
     /**
      * Sets the list of class names whose instances can be assigned
      * from the objects of this type.
      */
     public void setAssignableClassNames(Set<String> assignableClassNames) {
         this.assignableClassNames = assignableClassNames;
     }
 
     public boolean isLazyLoaded() {
         if (isLazyLoaded == null) {
             isLazyLoaded = getObjectClass() == null ? false :
                     getObjectClass().isAnnotationPresent(LazyLoad.class);
         }
 
         return isLazyLoaded;
     }
 
     // Recursively adds all names based on the class hierarchy.
     private static void addNames(
             DatabaseEnvironment environment,
             Set<String> groups,
             List<String> superClassNames,
             Set<String> assignableClassNames,
             Class<?> objectClass) {
 
         String className;
         ObjectType type;
         String interfaceClassName;
 
         for (; objectClass != null; objectClass = objectClass.getSuperclass()) {
             className = objectClass.getName();
             groups.add(className);
             superClassNames.add(className);
             assignableClassNames.add(className);
 
             type = environment.getTypeByClass(objectClass);
             if (type != null) {
                 groups.add(type.getInternalName());
             }
 
             ArrayDeque<Class<?>> interfaceClasses = new ArrayDeque<Class<?>>();
             for (Class<?> interfaceClass : objectClass.getInterfaces()) {
                 interfaceClasses.add(interfaceClass);
             }
 
             while (!interfaceClasses.isEmpty()) {
                 Class<?> interfaceClass = interfaceClasses.poll();
 
                 interfaceClassName = interfaceClass.getName();
                 groups.add(interfaceClassName);
                 assignableClassNames.add(interfaceClassName);
 
                 if (Recordable.class.isAssignableFrom(interfaceClass)) {
                     type = environment.getTypeByClass(interfaceClass);
                     if (type != null) {
                         groups.add(type.getInternalName());
                     }
                 }
 
                 for (Class<?> superInterfaceClass : interfaceClass.getInterfaces()) {
                     interfaceClasses.add(superInterfaceClass);
                 }
             }
         }
     }
 
     /**
      * Initializes this type definition with the reflection data from the
      * object class of this type.
      */
     public void initialize() {
         Class<?> objectClass = getObjectClass();
         if (objectClass == null) {
             return;
         }
 
         String simpleName = objectClass.getSimpleName();
         if (Modification.class.isAssignableFrom(objectClass)) {
             if (simpleName.equals(COMMON_NAME_DATA)) {
                 simpleName = objectClass.getName();
                 int dotAt = simpleName.lastIndexOf('.');
                 if (dotAt > -1) {
                     simpleName = simpleName.substring(dotAt + 1);
                 }
 
             } else if (simpleName.endsWith(COMMON_NAME_MODIFICATION)) {
                 int newLength = simpleName.length() - COMMON_NAME_MODIFICATION.length();
                 if (newLength > 0) {
                     simpleName = simpleName.substring(0, newLength);
                 }
             }
         }
 
         setDisplayName(StringUtils.toLabel(simpleName));
         setInternalName(objectClass.getName());
         getLabelFields().clear();
         setPreviewField(null);
 
         // Set the abstract flag on non-Recordable classes (temporary),
         // interfaces, and abstract classes so that they cannot be saved.
         setAbstract(!Recordable.class.isAssignableFrom(objectClass)
                 || objectClass.isInterface()
                 || Modifier.isAbstract(objectClass.getModifiers()));
 
         setEmbedded(false);
 
         Set<String> groups = new LinkedHashSet<String>();
         List<String> superClassNames = new ArrayList<String>();
         Set<String> assignableClassNames = new LinkedHashSet<String>();
 
         addNames(
                 getState().getDatabase().getEnvironment(),
                 groups,
                 superClassNames,
                 assignableClassNames,
                 objectClass);
 
         setGroups(groups);
         setSuperClassNames(superClassNames);
         setAssignableClassNames(assignableClassNames);
 
         getModificationClassNames().clear();
         modify(objectClass);
 
         // Set the label fields to the first text field if not explicitly
         // set already.
         int labelFieldIndex = -1;
         if (ObjectUtils.isBlank(labelFields)) {
             for (ObjectField field : getFields()) {
                 ++ labelFieldIndex;
                 Class<?> fieldClass = ObjectUtils.getClassByName(field.getJavaDeclaringClassName());
                 if (fieldClass != null &&
                         !Modification.class.isAssignableFrom(fieldClass) &&
                         ObjectField.TEXT_TYPE.equals(field.getInternalType()) &&
                         ObjectUtils.isBlank(field.getJavaEnumClassName())) {
                     getLabelFields().add(field.getInternalName());
                     break;
                 }
             }
         }
 
         // Set the preview field to the first file field before the label
         // field if not explictly set already.
         if (ObjectUtils.isBlank(previewField)) {
             int previewFieldIndex = -1;
             for (ObjectField field : getFields()) {
                 ++ previewFieldIndex;
                 if (previewFieldIndex >= labelFieldIndex) {
                     break;
                 } else if (ObjectField.FILE_TYPE.equals(field.getInternalType())) {
                     setPreviewField(field.getInternalName());
                     break;
                 }
             }
         }
     }
 
     /**
      * Modifies this type definition with the reflection data from the
      * given {@code modificationClass}.
      */
     @SuppressWarnings("deprecation")
     public void modify(Class<?> modificationClass) {
 
         getModificationClassNames().add(modificationClass.getName());
 
         Database database = getState().getDatabase();
         DatabaseEnvironment environment = database.getEnvironment();
         TypeDefinition<?> definition = TypeDefinition.getInstance(modificationClass);
         List<ObjectField> globalFields = environment.getFields();
         List<ObjectIndex> globalIndexes = environment.getIndexes();
         List<ObjectField> localFields = getFields();
         List<ObjectIndex> localIndexes = getIndexes();
 
         updateFieldsAndIndexes(
                 this, database, environment, definition, globalFields,
                 globalIndexes, localFields, localIndexes, false);
 
         FieldInternalNamePrefix prefixAnnotation = definition.getObjectClass().getAnnotation(FieldInternalNamePrefix.class);
         String prefix = prefixAnnotation != null ? prefixAnnotation.value() : "";
 
         // Remove any fields or indexes that are no longer declared.
         boolean isModification = !modificationClass.equals(getObjectClass());
         Set<String> assignableClassNames = getAssignableClassNames();
         Set<String> fieldInternalNames = new HashSet<String>();
 
         for (Map.Entry<String, List<Field>> e : definition.getAllSerializableFields().entrySet()) {
             Field javaField = e.getValue().get(e.getValue().size() - 1);
             String internalName = e.getKey().replace('$', '.');
 
             FieldInternalName fieldInternalNameAnnotation = javaField.getAnnotation(FieldInternalName.class);
             if (fieldInternalNameAnnotation != null) {
                 internalName = fieldInternalNameAnnotation.value();
             }
 
             InternalName internalNameAnnotation = javaField.getAnnotation(InternalName.class);
             if (internalNameAnnotation != null) {
                 internalName = internalNameAnnotation.value();
             }
 
             fieldInternalNames.add(prefix + internalName);
         }
 
         for (Iterator<ObjectField> i = localFields.iterator(); i.hasNext(); ) {
             ObjectField field = i.next();
             String declaringClassName = field.getJavaDeclaringClassName();
             if (!ObjectUtils.isBlank(declaringClassName)) {
                 if ((((isModification && modificationClass.getName().equals(declaringClassName)))
                         || (!isModification && assignableClassNames.contains(declaringClassName)))
                         && !fieldInternalNames.contains(field.getInternalName())) {
                     i.remove();
                 }
             }
         }
 
         for (Iterator<ObjectIndex> i = localIndexes.iterator(); i.hasNext(); ) {
             ObjectIndex index = i.next();
             String declaringClassName = index.getJavaDeclaringClassName();
             if (!ObjectUtils.isBlank(declaringClassName)) {
                 if ((((isModification && modificationClass.getName().equals(declaringClassName)))
                         || (!isModification && assignableClassNames.contains(declaringClassName)))
                         && !fieldInternalNames.contains(index.getField())) {
                     i.remove();
                 }
             }
         }
 
         setFields(localFields);
         setIndexes(localIndexes);
 
         for (Annotation annotation : modificationClass.getAnnotations()) {
             AnnotationProcessorClass processorClass = annotation.annotationType().getAnnotation(AnnotationProcessorClass.class);
             if (processorClass != null) {
                 @SuppressWarnings("unchecked")
                 AnnotationProcessor<Annotation> processor = (AnnotationProcessor<Annotation>) ANNOTATION_PROCESSORS.get(processorClass.value());
                 processor.process(this, annotation);
             }
         }
     }
 
     /**
      * Finds a set of all the other {@linkplain #isConcrete concrete}
      * types that are compatible with this one.
      */
     public Set<ObjectType> findConcreteTypes() {
         Set<ObjectType> concreteTypes = new LinkedHashSet<ObjectType>();
         String className = getObjectClassName();
         if (!ObjectUtils.isBlank(className)) {
             for (ObjectType type : getState().getDatabase().getEnvironment().getTypesByGroup(className)) {
                 if (type.isConcrete()) {
                     concreteTypes.add(type);
                 }
             }
         }
         return concreteTypes;
     }
 
     // --- DatabaseEnvironment bridge ---
 
     /** Returns an instance with the given {@code id}. */
     public static ObjectType getInstance(UUID id) {
         return Database.Static.getDefault().getEnvironment().getTypeById(id);
     }
 
     /** Returns an instance with the given {@code name}. */
     public static ObjectType getInstance(String name) {
         return Database.Static.getDefault().getEnvironment().getTypeByName(name);
     }
 
     /** Returns an instance with the given {@code objectClass}. */
     public static ObjectType getInstance(Class<?> objectClass) {
         return Database.Static.getDefault().getEnvironment().getTypeByClass(objectClass);
     }
 
     /** Creates an object of this type with the given {@code id}. */
     public Object createObject(UUID id) {
         State state = getState();
         return state.getDatabase().getEnvironment().createObject(state.getId(), id);
     }
 
     // --- Nested ---
 
     /**
      * Specifies the class that can manipulate a type using the target
      * annotation.
      */
     @Documented
     @Retention(RetentionPolicy.RUNTIME)
     @Target(ElementType.ANNOTATION_TYPE)
     public @interface AnnotationProcessorClass {
 
         Class<? extends AnnotationProcessor<? extends Annotation>> value();
     }
 
     /** Represents a class that can manipulate a type. */
     public static interface AnnotationProcessor<A extends Annotation> {
 
         public void process(ObjectType type, A annotation);
     }
 }
