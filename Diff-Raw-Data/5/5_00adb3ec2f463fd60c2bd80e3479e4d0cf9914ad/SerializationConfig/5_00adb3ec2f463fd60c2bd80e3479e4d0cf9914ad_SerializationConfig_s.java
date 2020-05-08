 package me.main__.util.SerializationConfig;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.WeakHashMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.bukkit.configuration.serialization.ConfigurationSerializable;
 import org.bukkit.configuration.serialization.ConfigurationSerialization;
 
 /**
  * An abstract class that can be extended to create a type that can easily be stored in the Bukkit config.
  * <p>
  * <b>IMPORTANT: Before custom types can be loaded/deserialized by Bukkit, they have to be registered
  * with {@link ConfigurationSerialization#registerClass(Class)}!</b>
  * <p>
  * You may create {@code protected static Map<String, String> getAliases()} to easily define aliases.
  */
 @SuppressWarnings({ "unchecked", "rawtypes" })
 public abstract class SerializationConfig implements ConfigurationSerializable {
     private static final InstanceCache<Serializor<?, ?>> serializorCache = new InstanceCache<Serializor<?,?>>();
     private static final InstanceCache<Validator<?>> validatorCache = new InstanceCache<Validator<?>>();
 
     private static final Map<Class<? extends SerializationConfig>, Map<String, String>> aliasMap =
             new WeakHashMap<Class<? extends SerializationConfig>, Map<String, String>>();
 
     private static Logger logger = null;
 
     private final Map<Field, Object> pendingVPropChanges = new HashMap<Field, Object>();
 
     /**
      * Initializes SerializationConfig with a logger to which to spout out any errors.  If this is not called, error
      * logging will not occur.
      *
      * @param log The logger to log errors to.
      */
     public static void initLogging(Logger log) {
         logger = log;
     }
 
     /**
      * Registers an alias.
      * <p>
      * If the alias given here is later used as property-name in the
      * other methods, I automatically know what you want to tell me :D
      *
      * @param clazz The class extending {@link SerializationConfig} the alias should be registered for.
      * @param alias The alias.
      * @param property The property's "real" name.
      */
     public static void registerAlias(Class<? extends SerializationConfig> clazz, String alias, String property) {
         Map<String, String> myAliasMap = getAliasMap(clazz);
         myAliasMap.put(alias, property);
     }
 
     /**
      * Gets the alias-map for a class using {@link SerializationConfig}.
      * @param clazz The class.
      * @return The alias-map.
      */
     public static Map<String, String> getAliasMap(Class<? extends SerializationConfig> clazz) {
         if (!aliasMap.containsKey(clazz)) {
             Map<String, String> defaultMap;
             // let's try to get it with the static getAliases()-method:
             Method defaultsMethod = null;
             try {
                 defaultsMethod = clazz.getDeclaredMethod("getAliases");
                 defaultsMethod.setAccessible(true);
                 defaultMap = (Map<String, String>) defaultsMethod.invoke(null);
             } catch (Exception e) {
                 defaultMap = null;
             } finally {
                 if (defaultsMethod != null)
                     defaultsMethod.setAccessible(false);
             }
             if (defaultMap == null)
                 defaultMap = new HashMap<String, String>();
 
             aliasMap.put(clazz, defaultMap);
         }
         return aliasMap.get(clazz);
     }
 
     /**
      * Registers an alias.
      *
      * @param alias The alias.
      * @param property The property's "real" name.
      */
     protected final void registerAlias(String alias, String property) {
         registerAlias(this.getClass(), alias, property);
     }
 
     /**
      * Gets our alias-map.
      * @return The alias-map.
      */
     protected final Map<String, String> getAliasMap() {
         return getAliasMap(this.getClass());
     }
 
     /**
      * Registers the specified Class with Bukkit.
      * @param clazz The class.
      */
     public static void registerAll(Class<? extends SerializationConfig> clazz) {
         ConfigurationSerialization.registerClass(clazz);
         Field[] fields = clazz.getDeclaredFields();
         for (Field f : fields) {
             f.setAccessible(true);
             if (f.isAnnotationPresent(Property.class)) {
                 Class<?> fieldclazz = f.getType();
                 if (SerializationConfig.class.isAssignableFrom(fieldclazz)) {
                     Class<? extends SerializationConfig> subclass = fieldclazz.asSubclass(SerializationConfig.class);
                     SerializationConfig.registerAll(subclass);
                 } else if (ConfigurationSerializable.class.isAssignableFrom(fieldclazz)) {
                     Class<? extends ConfigurationSerializable> subclass = fieldclazz.asSubclass(ConfigurationSerializable.class);
                     ConfigurationSerialization.registerClass(subclass);
                 }
             }
             f.setAccessible(false);
         }
     }
 
     /**
      * Unregisters the specified Class from Bukkit.
      * @param clazz The class.
      */
     public static void unregisterAll(Class<? extends SerializationConfig> clazz) {
         ConfigurationSerialization.unregisterClass(clazz);
         Field[] fields = clazz.getDeclaredFields();
         for (Field f : fields) {
             f.setAccessible(true);
             if (f.isAnnotationPresent(Property.class)) {
                 Class<?> fieldclazz = f.getType();
                 if (ConfigurationSerializable.class.isAssignableFrom(fieldclazz)) {
                     Class<? extends ConfigurationSerializable> subclass = fieldclazz.asSubclass(ConfigurationSerializable.class);
                     ConfigurationSerialization.unregisterClass(subclass);
                 }
             }
             f.setAccessible(false);
         }
     }
 
     /**
      * This constructor does nothing (okay, it sets the defaults...), it's just here for
      * you to provide a super constructor that can be overridden and called.
      */
     public SerializationConfig() {
         setDefaults();
     }
 
     /**
      * This is the constructor used by Bukkit to deserialize the object.
      * Yep, this does the actual deserialization-work so make sure to have a constructor
      * that takes a {@code Map<String, Object>} and passes it to this super implementation.
      * @param values The map bukkit passes to us.
      */
     public SerializationConfig(Map<String, Object> values) {
         this.loadValues(values);
     }
 
     private void log(Level level, String message) {
         this.log(level, message, null);
     }
 
     private void log(Level level, String message, Exception e) {
         if (logger != null) {
             logger.log(level, message);
         } else if (e != null) {
             e.printStackTrace();
         }
     }
 
     /**
      * This is basically the same as {@link #SerializationConfig(Map)}, however it's very useful for object-recycling.
      * <p>
      * Override this with a public version if you want to.
      * @param values The map bukkit passes to us.
      */
     protected void loadValues(Map<String, Object> values) {
         setDefaults();
 
         Field[] fields = this.getClass().getDeclaredFields();
         for (Field f : fields) {
             f.setAccessible(true);
             Property propertyInfo = f.getAnnotation(Property.class);
             final Object serializedValue = values.get(f.getName());
             if ((serializedValue != null) && (propertyInfo != null) && (!VirtualProperty.class.isAssignableFrom(f.getType()) || propertyInfo.persistVirtual())) {
                 try {
                     // yay, this field is a property :D
                     // let's continue and try to serialize it
                     Class<? extends Serializor<?, ?>> serializorClass = (Class<? extends Serializor<?, ?>>) propertyInfo.serializor();
                     // get the serializor from SerializorCache
                     Serializor serializor = serializorCache.getInstance(serializorClass, this);
                     // deserialize it and set the field
                     final Object value = serializor.deserialize(serializedValue, getFieldType(f));
                     if (value != null) {
                         if (!VirtualProperty.class.isAssignableFrom(f.getType()))
                             f.set(this, value);
                         else synchronized (pendingVPropChanges) {
                             this.pendingVPropChanges.put(f, value);
                         }
                     }
                 } catch (IllegalAccessException e) {
                    logger.log(Level.WARNING, "Access exception while loading value for " + f.getName(), e);
                 } catch (IllegalPropertyValueException e) {
                    logger.log(Level.WARNING, "Exception while loading value for " + f.getName(), e);
                 }
             }
             f.setAccessible(false);
         }
     }
 
     /**
      * Calls all virtual property setters with the loaded values.
      */
     protected void flushPendingVPropChanges() {
         synchronized (pendingVPropChanges) {
             for (Map.Entry<Field, Object> entry : pendingVPropChanges.entrySet()) {
                 try {
                     entry.getKey().setAccessible(true);
                     ((VirtualProperty) entry.getKey().get(this)).set(entry.getValue());
                     entry.getKey().setAccessible(false);
                 } catch (IllegalAccessException e) {
                     throw new RuntimeException(e);
                 }
             }
 
             pendingVPropChanges.clear();
         }
     }
 
     /**
      * I'm too lazy to explain this...
      *
      * @see com.onarandombox.MultiverseCore.MVWorld
      */
     protected void buildVPropChanges() {
         synchronized (pendingVPropChanges) {
             if (!pendingVPropChanges.isEmpty())
                 throw new IllegalStateException("pendingVPropChanges has to be empty!");
 
             try {
                 for (Field f : this.getClass().getDeclaredFields()) {
                     f.setAccessible(true);
                     if (VirtualProperty.class.isAssignableFrom(f.getType())
                             && f.isAnnotationPresent(Property.class)
                             && f.getAnnotation(Property.class).persistVirtual()) {
                         pendingVPropChanges.put(f, ((VirtualProperty) f.get(this)).get());
                     }
                     f.setAccessible(false);
                 }
             } catch (IllegalAccessException e) {
                 throw new RuntimeException(e); // should never happen
             }
         }
     }
 
     /**
      * Copies all values.
      * @param other The other object.
      */
     protected void copyValues(SerializationConfig other) {
         this.loadValues(other.serialize());
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public final Map<String, Object> serialize() {
         Field[] fields = this.getClass().getDeclaredFields();
         Map<String, Object> ret = new LinkedHashMap<String, Object>();
         for (Field f : fields) {
             if (pendingVPropChanges.containsKey(f)) {
                 ret.put(f.getName(), serializorCache.getInstance(
                         f.getAnnotation(Property.class).serializor())
                         .serialize(pendingVPropChanges.get(f)));
                 continue;
             }
             f.setAccessible(true);
             Property propertyInfo = f.getAnnotation(Property.class);
             if ((propertyInfo != null) && (!VirtualProperty.class.isAssignableFrom(f.getType()) || propertyInfo.persistVirtual())) {
                 try {
                     // yay, this field is a property :D
                     // let's continue and try to serialize it
                     Class<Serializor<?, ?>> serializorClass = (Class<Serializor<?, ?>>) propertyInfo.serializor();
                     // get the serializor from SerializorCache
                     Serializor serializor = serializorCache.getInstance(serializorClass, this);
                     // serialize it and put it into the output-map
                     Object raw = f.get(this);
                     if (raw instanceof VirtualProperty)
                         raw = ((VirtualProperty) raw).get();
                     ret.put(f.getName(), serializor.serialize(raw));
                 } catch (Exception e) {
                 }
             }
             f.setAccessible(false);
         }
 
         return ret;
     }
 
     private String fixupName(String name, boolean ignoreCase) {
         if (getAliasMap().containsKey(name)) {
             return getAliasMap().get(name);
         } else if (ignoreCase) {
             for (Map.Entry<String, String> entry : getAliasMap().entrySet()) {
                 if (entry.getKey().equalsIgnoreCase(name))
                     return entry.getValue();
             }
         }
         return name;
     }
 
     /**
      * Sets a property.
      *
      * @param property The name of the property. You can specify paths to subconfigs with '.'. Example: 'childconfig.value'
      * @param value The new value for the property. It will be automatically casted.
      * @return True at success, false if the operation failed.
      * @throws ClassCastException When the property is unable to hold {@code value}.
      * @throws NoSuchPropertyException When the property was not found.
      * @see #setPropertyValueUnchecked(String, Object)
      */
     public final boolean setPropertyValue(String property, Object value) throws ClassCastException, NoSuchPropertyException {
         return setPropertyValue(property, value, false);
     }
 
     /**
      * Sets a property.
      *
      * @param property The name of the property. You can specify paths to subconfigs with '.'. Example: 'childconfig.value'
      * @param value The new value for the property. It will be automatically casted.
      * @param ignoreCase Whether we should ignore case while searching.
      * @return True at success, false if the operation failed.
      * @throws ClassCastException When the property is unable to hold {@code value}.
      * @throws NoSuchPropertyException When the property was not found.
      * @see #setPropertyValueUnchecked(String, Object, boolean)
      */
     public final boolean setPropertyValue(String property, Object value, boolean ignoreCase) throws ClassCastException, NoSuchPropertyException {
         return this.setPropertyValue(property, value, ignoreCase, false);
     }
 
     private final boolean setPropertyValue(String property, Object value, boolean ignoreCase, boolean recursive) throws ClassCastException, NoSuchPropertyException {
         if (!recursive) {
             property = fixupName(property, ignoreCase);
         }
         try {
             String[] nodes = property.split("\\."); // this is a regex so we have to escape the '.'
             if (nodes.length == 1) {
                 Field field = null;
                 try {
                     field = ReflectionUtils.getField(nodes[0], this.getClass(), ignoreCase);
                     field.setAccessible(true);
                     if (field.isAnnotationPresent(Property.class)) {
                         if (!field.getType().isAssignableFrom(value.getClass()) && !field.getType().isPrimitive()
                                 && !VirtualProperty.class.isAssignableFrom(field.getType()))
                             throw new ClassCastException(value.getClass().toString() + " cannot be cast to " + field.getType().toString());
 
                         Property propertyInfo = field.getAnnotation(Property.class);
                         if (VirtualProperty.class.isAssignableFrom(field.getType())) {
                             // validate
                             try {
                                 value = validate(field, propertyInfo, value);
                             } catch (ChangeDeniedException e) {
                                 return false;
                             }
 
                             // it's virtual!
                             VirtualProperty<Object> vProp = (VirtualProperty<Object>) field.get(this);
                             // auto-cast FTW :D
                             vProp.set(value);
                             return true;
                         } else {
                             return validateAndDoChange(field, value);
                         }
                     } else {
                         throw new MissingAnnotationException("Property");
                     }
                 } catch (MissingAnnotationException e) {
                     throw new NoSuchPropertyException(e);
                 } catch (NoSuchFieldException e) {
                     throw new NoSuchPropertyException(e);
                 } catch (ClassCastException e) {
                     throw e;
                 } catch (Exception e) {
                     return false;
                 } finally {
                     if (field != null)
                         field.setAccessible(false);
                 }
             }
             // recursion...
             String nextNode = nodes[0];
             Field nodeField = ReflectionUtils.getField(nextNode, this.getClass(), ignoreCase);
             nodeField.setAccessible(true);
             if (!nodeField.isAnnotationPresent(Property.class))
                 throw new Exception();
             SerializationConfig child = (SerializationConfig) nodeField.get(this);
             StringBuilder sb = new StringBuilder();
             for (int i = 1; i < nodes.length; i++) {
                 sb.append(nodes[i]).append('.');
             }
             sb.deleteCharAt(sb.length() - 1);
 
             Exception ex = null;
             boolean ret;
             try {
                 ret = child.setPropertyValue(sb.toString(), value, ignoreCase, true);
             } catch (Exception e) {
                 ex = e;
                 ret = false;
             }
             // call validator...
             try {
                 validate(nodeField, nodeField.getAnnotation(Property.class), child);
             } catch (Exception e) {
                 // we just ignore what he says, however
             }
             if (ex != null)
                 throw ex;
             return ret;
         } catch (ClassCastException e) {
             throw e;
         } catch (NoSuchPropertyException e) {
             throw e;
         } catch (Exception e) {
             // we fail sliently
         }
         return false;
     }
 
     /**
      * Sets a property using a {@link String}.
      *
      * @param property The name of the property. You can specify paths to subconfigs with '.'. Example: 'childconfig.value'
      * @param value The new value for the property. Only works if the {@link Serializor} supports deserialization from a {@link String}.
      * @return True at success, false if the operation failed.
      * @throws NoSuchPropertyException When the property was not found.
      * @see #setPropertyUnchecked(String, String)
      */
     public final boolean setProperty(String property, String value) throws NoSuchPropertyException {
         return setProperty(property, value, false);
     }
 
     private final static Class<?> getFieldType(Field field) {
         if (VirtualProperty.class.isAssignableFrom(field.getType()))
             return field.getAnnotation(Property.class).virtualType(); // virtual property
         else
             return field.getType();
     }
 
     /**
      * Sets a property using a {@link String}.
      *
      * @param property The name of the property. You can specify paths to subconfigs with '.'. Example: 'childconfig.value'
      * @param value The new value for the property. Only works if the {@link Serializor} supports deserialization from a {@link String}.
      * @param ignoreCase Whether we should ignore case while searching.
      * @return True at success, false if the operation failed.
      * @throws NoSuchPropertyException When the property was not found.
      * @see #setPropertyUnchecked(String, String, boolean)
      */
     public final boolean setProperty(String property, String value, boolean ignoreCase) throws NoSuchPropertyException {
         return this.setProperty(property, value, ignoreCase, false);
     }
 
     private final boolean setProperty(String property, String value, boolean ignoreCase, boolean recursive) throws NoSuchPropertyException {
         if (!recursive) {
             property = fixupName(property, ignoreCase);
         }
         try {
             String[] nodes = property.split("\\."); // this is a regex so we have to escape the '.'
             if (nodes.length == 1) {
                 Field field = null;
                 try {
                     field = ReflectionUtils.getField(nodes[0], this.getClass(), ignoreCase);
                     field.setAccessible(true);
                     if (field.isAnnotationPresent(Property.class)) {
                         Property propertyInfo = field.getAnnotation(Property.class);
                         Class<? extends Serializor<?, ?>> serializorClass = (Class<? extends Serializor<?, ?>>) propertyInfo.serializor();
                         Serializor serializor = serializorCache.getInstance(serializorClass, this);
                         Object oVal;
                         try {
                             oVal = serializor.deserialize(value, getFieldType(field));
                         } catch (IllegalPropertyValueException e) {
                             return false;
                         } catch (RuntimeException e) {
                             // throw new IllegalPropertyValueException(e);
                             return false;
                         }
                         if (VirtualProperty.class.isAssignableFrom(field.getType())) {
                             // validate
                             try {
                                 oVal = validate(field, propertyInfo, oVal);
                             } catch (ChangeDeniedException e) {
                                 return false;
                             }
 
                             // it's virtual!
                             VirtualProperty<Object> vProp = (VirtualProperty<Object>) field.get(this);
                             // auto-cast FTW :D
                             vProp.set(oVal);
                             return true;
                         } else {
                             return validateAndDoChange(field, oVal);
                         }
                     } else {
                         throw new MissingAnnotationException("Property");
                     }
                 } catch (MissingAnnotationException e) {
                     throw new NoSuchPropertyException(e);
                 } catch (NoSuchFieldException e) {
                     throw new NoSuchPropertyException(e);
                 } catch (Exception e) {
                     return false;
                 } finally {
                     if (field != null)
                         field.setAccessible(false);
                 }
             }
             // recursion...
             String nextNode = nodes[0];
             Field nodeField = ReflectionUtils.getField(nextNode, this.getClass(), ignoreCase);
             nodeField.setAccessible(true);
             if (!nodeField.isAnnotationPresent(Property.class))
                 throw new Exception();
             SerializationConfig child = (SerializationConfig) nodeField.get(this);
             StringBuilder sb = new StringBuilder();
             for (int i = 1; i < nodes.length; i++) {
                 sb.append(nodes[i]).append('.');
             }
             sb.deleteCharAt(sb.length() - 1);
 
             Exception ex = null;
             boolean ret;
             try {
                 ret = child.setProperty(sb.toString(), value, ignoreCase, true);
             } catch (Exception e) {
                 ex = e;
                 ret = false;
             }
             // call validator...
             try {
                 validate(nodeField, nodeField.getAnnotation(Property.class), child);
             } catch (Exception e) {
                 // we just ignore what he says, however
             }
             if (ex != null)
                 throw ex;
             return ret;
         } catch (NoSuchPropertyException e) {
             throw e;
         } catch (Exception e) {
             // we fail sliently
         }
         return false;
     }
 
     /**
      * Gets a property's value as {@link String}.
      * @param property The property's name. You can specify paths to subconfigs with '.'. Example: 'childconfig.value'
      * @return The property's value.
      * @throws NoSuchPropertyException When the property was not found.
      * @see #getPropertyUnchecked(String)
      */
     public final String getProperty(String property) throws NoSuchPropertyException {
         return getProperty(property, false);
     }
 
     /**
      * Gets a property's value as {@link String}.
      * @param property The property's name. You can specify paths to subconfigs with '.'. Example: 'childconfig.value'
      * @param ignoreCase Whether we should ignore case while searching.
      * @return The property's value.
      * @throws NoSuchPropertyException When the property was not found.
      * @see #getPropertyUnchecked(String, boolean)
      */
     public final String getProperty(String property, boolean ignoreCase) throws NoSuchPropertyException {
         try {
             String[] nodes = property.split("\\."); // this is a regex so we have to escape the '.'
             if (nodes.length == 1) {
                 Field field = null;
                 try {
                     field = ReflectionUtils.getField(fixupName(nodes[0], ignoreCase), this.getClass(), ignoreCase);
                     field.setAccessible(true);
                     if (field.isAnnotationPresent(Property.class)) {
                         Property propertyInfo = field.getAnnotation(Property.class);
                         Class<? extends Serializor<?, ?>> serializorClass = (Class<? extends Serializor<?, ?>>) propertyInfo.serializor();
                         Serializor serializor = serializorCache.getInstance(serializorClass, this);
                         Object rawValue;
                         if (VirtualProperty.class.isAssignableFrom(field.getType())) {
                             // it's virtual!
                             VirtualProperty<Object> vProp = (VirtualProperty<Object>) field.get(this);
                             rawValue = vProp.get();
                         } else {
                             rawValue = field.get(this);
                         }
                         Object serialized = serializor.serialize(rawValue);
                         return serialized.toString();
                     } else {
                         throw new MissingAnnotationException("Property");
                     }
                 } catch (MissingAnnotationException e) {
                     throw new NoSuchPropertyException(e);
                 } catch (NoSuchFieldException e) {
                     throw new NoSuchPropertyException(e);
                 } catch (Exception e) {
                     throw e;
                 } finally {
                     if (field != null)
                         field.setAccessible(false);
                 }
             }
             // recursion...
             String nextNode = nodes[0];
             Field nodeField = ReflectionUtils.getField(fixupName(nextNode, ignoreCase), this.getClass(), ignoreCase);
             nodeField.setAccessible(true);
             if (!nodeField.isAnnotationPresent(Property.class))
                 throw new Exception();
             SerializationConfig child = (SerializationConfig) nodeField.get(this);
             StringBuilder sb = new StringBuilder();
             for (int i = 1; i < nodes.length; i++) {
                 sb.append(nodes[i]).append('.');
             }
             sb.deleteCharAt(sb.length() - 1);
             return child.getProperty(sb.toString(), ignoreCase);
         } catch (NoSuchPropertyException e) {
             throw e;
         } catch (Exception e) {
             throw new RuntimeException("Unexpected exception in getProperty(String, boolean)!", e);
         }
     }
 
     /**
      * Gets a property's description.
      * @param property The property's name. You can specify paths to subconfigs with '.'. Example: 'childconfig.value'
      * @return The property's description.
      * @throws NoSuchPropertyException When the property was not found.
      * @see #getPropertyUnchecked(String)
      */
     public final String getPropertyDescription(String property) throws NoSuchPropertyException {
         return getPropertyDescription(property, false);
     }
 
     /**
      * Gets a property's description.
      * @param property The property's name. You can specify paths to subconfigs with '.'. Example: 'childconfig.value'
      * @param ignoreCase Whether we should ignore case while searching.
      * @return The property's description.
      * @throws NoSuchPropertyException When the property was not found.
      * @see #getPropertyDescriptionUnchecked(String, boolean)
      */
     public final String getPropertyDescription(String property, boolean ignoreCase) throws NoSuchPropertyException {
         try {
             String[] nodes = property.split("\\."); // this is a regex so we have to escape the '.'
             if (nodes.length == 1) {
                 Field field = null;
                 try {
                     field = ReflectionUtils.getField(fixupName(nodes[0], ignoreCase), this.getClass(), ignoreCase);
                     field.setAccessible(true);
                     if (field.isAnnotationPresent(Property.class)) {
                         Property propertyInfo = field.getAnnotation(Property.class);
                         return propertyInfo.description();
                     } else {
                         throw new MissingAnnotationException("Property");
                     }
                 } catch (MissingAnnotationException e) {
                     throw new NoSuchPropertyException(e);
                 } catch (NoSuchFieldException e) {
                     throw new NoSuchPropertyException(e);
                 } catch (Exception e) {
                     throw e;
                 } finally {
                     if (field != null)
                         field.setAccessible(false);
                 }
             }
             // recursion...
             String nextNode = nodes[0];
             Field nodeField = ReflectionUtils.getField(fixupName(nextNode, ignoreCase), this.getClass(), ignoreCase);
             nodeField.setAccessible(true);
             if (!nodeField.isAnnotationPresent(Property.class))
                 throw new Exception();
             SerializationConfig child = (SerializationConfig) nodeField.get(this);
             StringBuilder sb = new StringBuilder();
             for (int i = 1; i < nodes.length; i++) {
                 sb.append(nodes[i]).append('.');
             }
             sb.deleteCharAt(sb.length() - 1);
             return child.getProperty(sb.toString(), ignoreCase);
         } catch (NoSuchPropertyException e) {
             throw e;
         } catch (Exception e) {
             throw new RuntimeException("Unexpected exception in getPropertyDescription(String, boolean)!", e);
         }
     }
 
     /**
      * We trust you, so you're allowed to use this awesome method that throws an
      * <b>unchecked</b> {@link RuntimeException} instead of the usual <b>checked</b> {@link NoSuchPropertyException}.
      *
      * @param property The name of the property. You can specify paths to subconfigs with '.'. Example: 'childconfig.value'
      * @param value The new value for the property. It will be automatically casted.
      * @return True at success, false if the operation failed.
      * @see #setPropertyValue(String, Object)
      */
     protected final boolean setPropertyValueUnchecked(String property, Object value) {
         try {
             return this.setPropertyValue(property, value);
         } catch (NoSuchPropertyException e) {
             throw new RuntimeException(e);
         }
     }
 
     /**
      * We trust you, so you're allowed to use this awesome method that throws an
      * <b>unchecked</b> {@link RuntimeException} instead of the usual <b>checked</b> {@link NoSuchPropertyException}.
      *
      * @param property The name of the property. You can specify paths to subconfigs with '.'. Example: 'childconfig.value'
      * @param value The new value for the property. It will be automatically casted.
      * @param ignoreCase Whether we should ignore case while searching.
      * @return True at success, false if the operation failed.
      * @see #setPropertyValue(String, Object, boolean)
      */
     protected final boolean setPropertyValueUnchecked(String property, Object value, boolean ignoreCase) {
         try {
             return this.setPropertyValue(property, value, ignoreCase);
         } catch (NoSuchPropertyException e) {
             throw new RuntimeException(e);
         }
     }
 
     /**
      * Sets a property using a {@link String}.
      *
      * @param property The name of the property. You can specify paths to subconfigs with '.'. Example: 'childconfig.value'
      * @param value The new value for the property. Only works if the {@link Serializor} supports deserialization from a {@link String}.
      * @return True at success, false if the operation failed.
      * @see #setProperty(String, String)
      */
     protected final boolean setPropertyUnchecked(String property, String value) {
         try {
             return this.setProperty(property, value);
         } catch (NoSuchPropertyException e) {
             throw new RuntimeException(e);
         }
     }
 
     /**
      * Sets a property using a {@link String}.
      *
      * @param property The name of the property. You can specify paths to subconfigs with '.'. Example: 'childconfig.value'
      * @param value The new value for the property. Only works if the {@link Serializor} supports deserialization from a {@link String}.
      * @param ignoreCase Whether we should ignore case while searching.
      * @return True at success, false if the operation failed.
      * @see #setProperty(String, String, boolean)
      */
     protected final boolean setPropertyUnchecked(String property, String value, boolean ignoreCase) {
         try {
             return this.setProperty(property, value, ignoreCase);
         } catch (NoSuchPropertyException e) {
             throw new RuntimeException(e);
         }
     }
 
     /**
      * We trust you, so you're allowed to use this awesome method that throws an
      * <b>unchecked</b> {@link RuntimeException} instead of the usual <b>checked</b> {@link NoSuchPropertyException}.
      *
      * @param property The property's name. You can specify paths to subconfigs with '.'. Example: 'childconfig.value'
      * @return The property's value.
      * @see #getProperty(String)
      */
     protected final String getPropertyUnchecked(String property) {
         try {
             return this.getProperty(property);
         } catch (NoSuchPropertyException e) {
             throw new RuntimeException(e);
         }
     }
 
     /**
      * We trust you, so you're allowed to use this awesome method that throws an
      * <b>unchecked</b> {@link RuntimeException} instead of the usual <b>checked</b> {@link NoSuchPropertyException}.
      *
      * @param property The property's name. You can specify paths to subconfigs with '.'. Example: 'childconfig.value'
      * @param ignoreCase Whether we should ignore case while searching.
      * @return The property's value.
      * @see #getProperty(String, boolean)
      */
     protected final String getPropertyUnchecked(String property, boolean ignoreCase) {
         try {
             return this.getProperty(property, ignoreCase);
         } catch (NoSuchPropertyException e) {
             throw new RuntimeException(e);
         }
     }
 
     /**
      * We trust you, so you're allowed to use this awesome method that throws an
      * <b>unchecked</b> {@link RuntimeException} instead of the usual <b>checked</b> {@link NoSuchPropertyException}.
      *
      * @param property The property's name. You can specify paths to subconfigs with '.'. Example: 'childconfig.value'
      * @return The property's description.
      * @see #getProperty(String, boolean)
      */
     protected final String getPropertyDescriptionUnchecked(String property) {
         try {
             return getPropertyDescription(property);
         } catch (NoSuchPropertyException e) {
            throw new RuntimeException(e);
         }
     }
 
     /**
      * We trust you, so you're allowed to use this awesome method that throws an
      * <b>unchecked</b> {@link RuntimeException} instead of the usual <b>checked</b> {@link NoSuchPropertyException}.
      *
      * @param property The property's name. You can specify paths to subconfigs with '.'. Example: 'childconfig.value'
      * @param ignoreCase Whether we should ignore case while searching.
      * @return The property's description.
      * @see #getProperty(String, boolean)
      */
     protected final String getPropertyDescriptionUnchecked(String property, boolean ignoreCase) {
         try {
             return getPropertyDescription(property, ignoreCase);
         } catch (NoSuchPropertyException e) {
             throw new RuntimeException(e);
         }
     }
 
     private Object validate(Field field, Property propertyInfo, Object newVal) throws IllegalAccessException, ChangeDeniedException {
         Validator<Object> validator = null;
         if (propertyInfo.validator() != Validator.class) { // only if a validator was set
             validator = validatorCache.getInstance(propertyInfo.validator(), this);
         } else if (this.getClass().isAnnotationPresent(ValidateAllWith.class)) {
             ValidateAllWith validAll = this.getClass().getAnnotation(ValidateAllWith.class);
             validator = validatorCache.getInstance(validAll.value(), this);
         }
         if (validator != null) {
             try {
                 if (validator instanceof ObjectUsingValidator)
                     newVal = ((ObjectUsingValidator) validator).validateChange(field.getName(), newVal, getValue(field), this);
                 else
                     newVal = validator.validateChange(field.getName(), newVal, getValue(field));
             } catch (ClassCastException e) {
                 throw new IllegalArgumentException("Illegal validator!", e);
             }
         }
         return newVal;
     }
 
     private Object getValue(Field field) throws IllegalArgumentException, IllegalAccessException {
         if (VirtualProperty.class.isAssignableFrom(field.getType()))
             return ((VirtualProperty) field.get(this)).get();
         else
             return field.get(this);
     }
 
     private <T> boolean validateAndDoChange(Field field, T newVal) throws Exception {
         if (!field.getType().isAssignableFrom(newVal.getClass()) && !field.getType().isPrimitive()) // types don't match
             return false;
         Property propInfo = field.getAnnotation(Property.class);
         try {
             newVal = (T) validate(field, propInfo, newVal);
         } catch (ChangeDeniedException e) {
             return false;
         }
 
         field.set(this, newVal);
 
         return true;
     }
 
     /**
      * This method sets properties in this object to their default-values.
      * <p>
      * <b>IMPORTANT: All properties have to be initialized HERE, never in/before the constructor!</b>
      */
     protected abstract void setDefaults();
 }
