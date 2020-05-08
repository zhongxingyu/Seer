 package at.yawk.fimfiction.data;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Maps;
 import java.lang.reflect.Constructor;
 import java.util.Map;
 import java.util.Set;
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 
 /**
  * Implementation of the Bundle class that uses an enum as the key (similar to EnumMap). Subclasses can use a few
  * basic methods to create instances of themselves and perform similar tasks.
  *
  * @author Jonas Konrad (yawkat)
  */
 abstract class EnumKeyBundle<T extends EnumKeyBundle, K extends Enum<K> & Key> extends Bundle<T, K> {
     /**
      * Internal map of all subtypes of this class used for easy instantiation.
      */
     private static final Map<Class<?>, BundleTypeData<?, ?>> dataByBundleClass = Maps.newHashMap();
 
    private final BundleTypeData<T, K> bundleTypeData;
 
     /**
      * Whether this object should be immutable.
      */
     boolean immutable = false;
     /**
      * Underlying data array. Indexes are the ordinal of the enum.
      */
     Object[] data;
 
     @SuppressWarnings("unchecked")
    EnumKeyBundle() { this.bundleTypeData = EnumKeyBundle.getBundleTypeData((Class<T>) getClass()); }
 
     // factories
 
     /**
      * Creates a new mutable instance of the given bundle type.
      */
     @Nonnull
     static <T extends EnumKeyBundle<T, K>, K extends Enum<K> & Key> T mutable(@Nonnull Class<T> type) {
         // Create empty instance.
         BundleTypeData<T, K> bundleTypeData = getBundleTypeData(type);
         T t = bundleTypeData.create();
         t.data = new Object[bundleTypeData.getKeyCount()];
         return t;
     }
 
     /**
      * Creates a new mutable instance of the given bundle type and fills it with data.
      */
     @Nonnull
     static <T extends EnumKeyBundle<T, K>, K extends Enum<K> & Key> T mutable(@Nonnull Class<T> type,
                                                                               @Nonnull Map<K, Object> data) {
         Preconditions.checkNotNull(data);
         T t = mutable(type);
         // Copy initial data into bundle (validation is performed in #set).
         for (Map.Entry<K, Object> entry : data.entrySet()) { t.set(entry.getKey(), entry.getValue()); }
         return t;
     }
 
     /**
      * Same as #mutable but with String keys in the initial data map.
      */
     @Nonnull
     static <T extends EnumKeyBundle<T, K>, K extends Enum<K> & Key> T mutableS(@Nonnull Class<T> type,
                                                                                @Nonnull Map<String, Object> data) {
         Preconditions.checkNotNull(data);
         T t = mutable(type);
         for (Map.Entry<String, Object> entry : data.entrySet()) { t.set(entry.getKey(), entry.getValue()); }
         return t;
     }
 
     /**
      * Returns the empty, immutable singleton instance of the type.
      */
     @Nonnull
     static <T extends EnumKeyBundle<T, K>, K extends Enum<K> & Key> T immutable(@Nonnull Class<T> type) {
         return getBundleTypeData(type).emptyImmutableSingleton;
     }
 
     /**
      * Creates a new immutable instance of the given bundle type and fills it with the given data.
      */
     @Nonnull
     static <T extends EnumKeyBundle<T, K>, K extends Enum<K> & Key> T immutable(@Nonnull Class<T> type,
                                                                                 @Nonnull Map<K, Object> data) {
         T t = mutable(type, data);
         t.immutable = true;
         return t;
     }
 
     /**
      * Same as #immutable but with String keys in the initial data map.
      */
     @Nonnull
     static <T extends EnumKeyBundle<T, K>, K extends Enum<K> & Key> T immutableS(@Nonnull Class<T> type,
                                                                                  @Nonnull Map<String, Object> data) {
         T t = mutableS(type, data);
         t.immutable = true;
         return t;
     }
 
     // type management
 
     /**
      * Adds a mapping for a subclass so it can be instantiated.
      *
      * @param bundleType The bundle class.
      * @param keyType    The Enum class used as the key.
      */
     static <T extends EnumKeyBundle, K extends Enum<K> & Key> void addMapping(@Nonnull Class<T> bundleType,
                                                                               @Nonnull Class<K> keyType) {
         assert !dataByBundleClass.containsKey(bundleType);
 
         BundleTypeData<T, K> map = new BundleTypeData<T, K>(bundleType, keyType);
         dataByBundleClass.put(bundleType, map);
     }
 
     /**
      * Tries to find a key of the given bundle type by name.
      */
     @Nullable
     static <T extends EnumKeyBundle<T, K>, K extends Enum<K> & Key> K findKey(@Nonnull Class<T> type,
                                                                               @Nonnull String id) {
         Preconditions.checkNotNull(id);
         return getBundleTypeData(type).getKey(id);
     }
 
     @Nonnull
     @SuppressWarnings("unchecked")
     private static <T extends EnumKeyBundle<T, K>, K extends Enum<K> & Key> BundleTypeData<T, K> getBundleTypeData(@Nonnull Class<T> type) {
         return (BundleTypeData<T, K>) dataByBundleClass.get(type);
     }
 
     @Nonnull
     private BundleTypeData<T, K> getBundleTypeData() { return bundleTypeData; }
 
     private void validateKey(@Nonnull BundleTypeData bundleTypeData, @Nonnull Key key) {
         if (!bundleTypeData.keyType.isInstance(key)) {
             throw new IllegalArgumentException("Invalid key type: " + key);
         }
     }
 
     // overrides (implementation)
 
     /**
      * Validates the value for the key.
      */
     @Override
     protected void _validate(@Nonnull K key, @Nonnull Object object) throws IllegalArgumentException {
         BundleTypeData<T, ?> bundleTypeData = getBundleTypeData();
         // validate and throw if necessary
         validateKey(bundleTypeData, key);
         if (!bundleTypeData.validate(key, object)) {
             throw new IllegalArgumentException("Invalid value on key '" + key.getId() + "': " + object + " " +
                                                "Required type: " + key.getType());
         }
     }
 
     /**
      * Tries to find a valid key with the given name. If no key is found, null is returned.
      */
     @Nullable
     @Override
     protected K _findKey(@Nonnull String id) { return getBundleTypeData().getKey(id); }
 
     /**
      * Creates a copy of this bundle. If immutable is true it will be immutable, otherwise it will be mutable.
      */
     @Nonnull
     @Override
     @SuppressWarnings({"unchecked", "RedundantCast"})
     protected T _copy(boolean immutable) {
         T t = (T) mutable((Class<T>) getClass());
         t.data = this.data.clone();
         if (immutable) {
             t.immutable = true;
             // make entries immutable as well
             for (int i = 0; i < t.data.length; i++) {
                 if (data[i] != null) {
                     ValueType v = getBundleTypeData().getKey(i).getType();
                     data[i] = v.immutableCopy(data[i]);
                 }
             }
         }
         return t;
     }
 
     /**
      * Returns the value for the given key or null if it is unset.
      */
     @Nonnull
     @Override
     protected Object _get(@Nonnull K key) {
         validateKey(getBundleTypeData(), key);
         return data[key.ordinal()];
     }
 
     /**
      * Sets the value for the given key to the given value. Immutability & Validity checks are not performed. If
      * value is null, the entry will be unset.
      */
     @Override
     protected void _set(@Nonnull K key, @Nullable Object value) {
         if (immutable) { throw new UnsupportedOperationException(); }
         validateKey(getBundleTypeData(), key);
         data[key.ordinal()] = value;
     }
 
     /**
      * Set of all possible keys.
      */
     @Nonnull
     @Override
     protected Set<K> _keySet() { return getBundleTypeData().keySet; }
 
     /**
      * Immutability flag.
      */
     @Override
     protected boolean _immutable() { return immutable; }
 
 }
 
 ///////////
 
 /**
  * Helper class used for storing various metadata about different bundle types.
  */
 class BundleTypeData<T extends EnumKeyBundle, K extends Enum<K> & Key> {
     /**
      * A map of all possible keys to their IDs.
      */
     private final Map<String, K> keys = Maps.newHashMap();
     /**
      * The constructor used for instance creation.
      */
     private final Constructor<T> constructor;
     /**
      * The enum class used for the key.
      */
     @Nonnull final Class<K> keyType;
     /**
      * The set of possible keys.
      */
     final Set<K> keySet;
     /**
      * A singleton instance of this bundle that is immutable and contains no elements.
      */
     @Nonnull final T emptyImmutableSingleton;
 
     BundleTypeData(@Nonnull Class<T> bundleType, @Nonnull Class<K> keyType) {
         this.keyType = keyType;
         try { // find constructor
             this.constructor = bundleType.getDeclaredConstructor();
         } catch (NoSuchMethodException e) { throw new AssertionError(e); }
         map();
         keySet = ImmutableSet.copyOf(keys.values());
         // create singleton
         emptyImmutableSingleton = create();
         emptyImmutableSingleton.data = new Object[getKeyCount()];
         emptyImmutableSingleton.immutable = true;
     }
 
     private void map() {
         // map name -> key
         for (K key : keyType.getEnumConstants()) { keys.put(key.getId(), key); }
     }
 
     @Nullable
     K getKey(@Nonnull String id) { return keys.get(id); }
 
     @Nonnull
     K getKey(int ordinal) { return keyType.getEnumConstants()[ordinal]; }
 
     @Nonnull
     T create() {
         try {
             return constructor.newInstance();
         } catch (Exception e) { throw new RuntimeException(e); }
     }
 
     int getKeyCount() { return keys.size(); }
 
     @SuppressWarnings("BooleanMethodIsAlwaysInverted")
     boolean validate(@Nonnull Key key, @Nonnull Object value) { return key.getType().validate(value); }
 }
