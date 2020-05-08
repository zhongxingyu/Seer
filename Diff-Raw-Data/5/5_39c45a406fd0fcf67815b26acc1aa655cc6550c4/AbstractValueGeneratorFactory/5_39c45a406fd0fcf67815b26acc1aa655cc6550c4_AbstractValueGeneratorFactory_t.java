 package com.stresstest.random;
 
 import java.util.ArrayDeque;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Deque;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Queue;
 import java.util.Set;
 
 import com.google.common.collect.ImmutableMap;
 import com.stresstest.random.constructor.ClassAccessWrapper;
 import com.stresstest.random.constructor.ClassConstructor;
 import com.stresstest.random.constructor.ClassPropertySetter;
 import com.stresstest.random.constructor.ClassPropertySetterRegistry;
 import com.stresstest.random.constructor.ClassValueGenerator;
 import com.stresstest.reflection.ReflectionUtils;
 
 abstract public class AbstractValueGeneratorFactory implements ValueGeneratorFactory {
     /**
      * {@link Collection} random value generator, as a result empty {@link ArrayList} returned.
      */
     @SuppressWarnings("rawtypes")
     final public static ValueGenerator<Collection<?>> COLLECTION_VALUE_GENERATOR = new AbstractValueGenerator<Collection<?>>() {
         @Override
         public Collection<?> generate() {
             return new ArrayList();
         }
     };
 
     /**
      * {@link List} random value generator, as a result generates empty {@link ArrayList} returned.
      */
     @SuppressWarnings("rawtypes")
     final public static ValueGenerator<List<?>> LIST_VALUE_GENERATOR = new AbstractValueGenerator<List<?>>() {
         @Override
         public List<?> generate() {
             return new ArrayList();
         }
     };
 
     /**
      * {@link Queue} random value generator as a result empty {@link ArrayDeque} returned.
      */
     @SuppressWarnings("rawtypes")
     final public static ValueGenerator<Queue<?>> QUEUE_VALUE_GENERATOR = new AbstractValueGenerator<Queue<?>>() {
         @Override
         public Queue<?> generate() {
             return new ArrayDeque();
         }
     };
 
     /**
      * {@link Deque} random value generator, as a result emptu {@link ArrayDeque} returned.
      */
     @SuppressWarnings("rawtypes")
     final public static ValueGenerator<Deque<?>> DEQUE_VALUE_GENERATOR = new AbstractValueGenerator<Deque<?>>() {
         @Override
         public Deque<?> generate() {
             return new ArrayDeque();
         }
     };
 
     /**
      * {@link Set} random value generator, as a result empty {@link HashSet} returned.
      */
     @SuppressWarnings("rawtypes")
     final public static ValueGenerator<Set<?>> SET_VALUE_GENERATOR = new AbstractValueGenerator<Set<?>>() {
         @Override
         public Set<?> generate() {
             return new HashSet();
         }
     };
 
     /**
      * {@link Map} random value generator, as a result empty {@link HashMap} returned.
      */
     @SuppressWarnings("rawtypes")
     final public static ValueGenerator<Map<?, ?>> MAP_VALUE_GENERATOR = new AbstractValueGenerator<Map<?, ?>>() {
         @Override
         public Map<?, ?> generate() {
             return new HashMap();
         }
     };
 
     final private Map<Class<?>, ValueGenerator<?>> DEFAULT_GENERATORS;
 
     final private Map<Class<?>, ValueGenerator<?>> REGISTERED_GENERATORS = new HashMap<Class<?>, ValueGenerator<?>>();
 
     final private ClassPropertySetterRegistry propertySetterManager;
 
     public AbstractValueGeneratorFactory() {
         this(new ClassPropertySetterRegistry());
     }
 
     public AbstractValueGeneratorFactory(final ClassPropertySetterRegistry setterManager) {
         this(setterManager, null);
     }
 
     public AbstractValueGeneratorFactory(final ClassPropertySetterRegistry setterManager, Map<Class<?>, ValueGenerator<?>> defaultGenerators) {
         this.propertySetterManager = setterManager != null ? setterManager : new ClassPropertySetterRegistry();
 
         HashMap<Class<?>, ValueGenerator<?>> standardValueGenerators = new HashMap<Class<?>, ValueGenerator<?>>();
 
         standardValueGenerators.put(Collection.class, COLLECTION_VALUE_GENERATOR);
         standardValueGenerators.put(List.class, LIST_VALUE_GENERATOR);
         standardValueGenerators.put(Set.class, SET_VALUE_GENERATOR);
         standardValueGenerators.put(Queue.class, QUEUE_VALUE_GENERATOR);
         standardValueGenerators.put(Deque.class, DEQUE_VALUE_GENERATOR);
 
         standardValueGenerators.put(Map.class, MAP_VALUE_GENERATOR);
 
         if (defaultGenerators != null)
             standardValueGenerators.putAll(defaultGenerators);
 
         DEFAULT_GENERATORS = ImmutableMap.copyOf(standardValueGenerators);
     }
 
     final public <T> void put(Class<T> klass, ValueGenerator<T> valueGenerator) {
         if (klass != null && valueGenerator != null)
             REGISTERED_GENERATORS.put(klass, valueGenerator);
     }
 
     @Override
     final public Collection<ValueGenerator<?>> getValueGenerators(Class<?>[] parameters) {
         // Step 1. Sanity check
         if (parameters == null || parameters.length == 0)
             return Collections.emptyList();
         // Step 2. Sequential check
         Collection<ValueGenerator<?>> resultGenerators = new ArrayList<ValueGenerator<?>>();
         for (Class<?> parameter : parameters) {
             if (parameter != null)
                 resultGenerators.add(getValueGenerator(parameter));
         }
         return resultGenerators;
     }
 
     @Override
     final public ClassPropertySetterRegistry getPropertySetterManager() {
         return propertySetterManager;
     }
 
     /**
      * Produces {@link ValueGenerator} for specified {@link Class}.
      * 
      * @param klass
      *            generated {@ling Class}
      * @return {@link ValueGenerator} for procided {@link Class}
      */
     @SuppressWarnings("unchecked")
     public <T> ValueGenerator<T> getValueGenerator(Class<T> klass) {
         // Step 1. Checking that it can be replaced with standard constructors
         ValueGenerator<T> valueGenerator = (ValueGenerator<T>) DEFAULT_GENERATORS.get(klass);
         if (valueGenerator != null)
             return valueGenerator;
         // Step 1.1. Checking registered generators
         valueGenerator = (ValueGenerator<T>) REGISTERED_GENERATORS.get(klass);
         if(valueGenerator != null)
             return valueGenerator;
         // Step 2. If this is enum replace with Random value generator
         if (klass.isEnum())
             return enumValueGenerator(klass);
         // Step 3. Initialize value generator with primarily public access
         valueGenerator = construct(ClassAccessWrapper.createPublicAccessor(klass));
         if (valueGenerator != null)
             return valueGenerator;
         // Step 4. Trying to initialize with all available access
         valueGenerator = construct(ClassAccessWrapper.createAllMethodsAccessor(klass));
         if (valueGenerator != null)
             return valueGenerator;
         // Step 5. Special case for an array
         if(klass.isArray()) {
             return arrayValueGenerator(klass);
         }
         for(Class<?> registered: REGISTERED_GENERATORS.keySet()) {
             if(klass.isAssignableFrom(registered)) {
                 return (ValueGenerator<T>) REGISTERED_GENERATORS.get(registered);
             }
         }
         // Step 7. If there is no result throw IllegalArgumentException
        throw new IllegalArgumentException("Can't construct " + klass.getSimpleName() + "(" + klass.getCanonicalName() + ")");
     }
 
     /**
      * Produces {@link ValueGenerator} for provided {@link ClassAccessWrapper}, returned {@link ValueGenerator} can return any subtype of the target
      * {@link Class}.
      * 
      * @param sourceClass
      *            {@link ClassAccessWrapper} with defined level of access.
      * @return {@link ValueGenerator} for provided class if, there is possible to create one.
      */
     @SuppressWarnings("unchecked")
     private <T> ValueGenerator<T> construct(final ClassAccessWrapper<T> sourceClass) {
         ValueGenerator<T> valueGenerator = sourceClass.constructable() ? tryConstruct(sourceClass) : null;
         if (valueGenerator != null)
             return valueGenerator;
         // Step 3.1 Trying to initialize sub classes
         Collection<Class<? extends T>> subClasses = ReflectionUtils.findPossibleImplementations(sourceClass.getSourceClass());
         // Step 3.2 Checking extended list of candidates
         for (Class<?> subClass : subClasses) {
             ClassAccessWrapper<?> childWrapper = sourceClass.wrap(subClass);
             if(REGISTERED_GENERATORS.containsKey(childWrapper.getSourceClass()))
                 return (ValueGenerator<T>) REGISTERED_GENERATORS.get(childWrapper.getSourceClass());
             if (childWrapper.constructable()) {
                 valueGenerator = (ValueGenerator<T>) tryConstruct(childWrapper);
                 if (valueGenerator != null)
                     return valueGenerator;
             }
         }
         return null;
     }
 
     /**
      * Produces {@link ValueGenerator} for a {@link Class} with restricted access.
      * 
      * @param classToGenerate
      *            {@link ClassAccessWrapper} for the class.
      * @return {@link ValueGenerator} if it is possible to create one, with defined access level, <code>null</code> otherwise.
      */
     private <T> ValueGenerator<T> tryConstruct(final ClassAccessWrapper<T> classToGenerate) {
         // Step 1. Selecting appropriate constructor
         ClassConstructor<T> objectConstructor = ClassConstructor.construct(classToGenerate, this);
         if (objectConstructor == null)
             return null;
         // Step 2. Selecting list of applicable specific selectors from specific properties
         ClassPropertySetter<T> classPropertySetter = ClassPropertySetter.constructPropertySetter(classToGenerate, this);
         // Step 3. Generating final ClassGenerator for the type
         return new ClassValueGenerator<T>(objectConstructor, classPropertySetter);
     }
 
     protected abstract <T> ValueGenerator<T> enumValueGenerator(Class<T> klass);
 
     protected abstract <T> ValueGenerator<T> arrayValueGenerator(Class<?> klass);
 
 }
