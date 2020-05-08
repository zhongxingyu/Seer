 package factory;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.lang.StringUtils;
 
 import play.Logger;
 import play.db.jpa.GenericModel;
 import play.db.jpa.Model;
 import play.test.Fixtures;
 import factory.annotation.Factory;
 import factory.callback.BuildCallback;
 
 public class FactoryBoy {
 
     protected static Map<Class<?>, ModelFactory<?>> modelFactoryCacheMap = new HashMap<Class<?>, ModelFactory<?>>();
 
     protected static Map<Class<?>, Integer> modelSequenceMap = new HashMap<Class<?>, Integer>();
 
     protected static ThreadLocal<Set<Class<?>>> _threadLocalModelDeletedSet = new ThreadLocal<Set<Class<?>>>();
 
     protected static synchronized Set<Class<?>> modelDeletedSet() {
         Set<Class<?>> modelDeletedSet = _threadLocalModelDeletedSet.get();
         if (modelDeletedSet == null) {
             modelDeletedSet = new HashSet<Class<?>>();
             _threadLocalModelDeletedSet.set(modelDeletedSet);
         }
         return modelDeletedSet;
     }
 
     protected static void reset() {
         _threadLocalModelDeletedSet.set(null);
     }
 
     /**
      * Only delete the Model when first call create(...) method.
      */
     public static void lazyDelete() {
         reset();
     }
 
     /**
      * Deletes the specified Models.
      * 
      * @param clazzes
      */
     public static void delete(Class<? extends GenericModel>... clazzes) {
         reset();
         for (Class<? extends GenericModel> type : clazzes) {
             deleteModelData(type);
         }
     }
 
     /**
      * Delete all will call the Fixtures.deleteDatabase()
      */
     public static void deleteAll() {
         reset();
         Fixtures.deleteDatabase();
     }
 
     protected static synchronized void checkOrDeleteModel(
                     Class<? extends GenericModel> clazz,
                     ModelFactory<? extends GenericModel> modelFactory) {
 
         Class<?>[] relationModels = modelFactory.relationModels();
         if (relationModels != null) {
             for (Class<?> r : relationModels) {
                 if (GenericModel.class.isAssignableFrom(r)) {
                     Class<? extends GenericModel> gm = (Class<? extends GenericModel>) r;
                     deleteModelData(gm);
                 }
             }
         }
         deleteModelData(clazz);
     }
 
     protected static <T extends GenericModel> void deleteModelData(Class<T> type) {
         try {
             if (!modelDeletedSet().contains(type)) {
                 Model.Manager.factoryFor(type).deleteAll();
                 modelDeletedSet().add(type);
             }
         } catch (Exception e) {
             // Logger.error(e, "While deleting " + type + " instances");
            if (!modelDeletedSet().contains(type)) {
                deleteAll(type);
                modelDeletedSet().add(type);
            }
         }
     }
 
     /**
      * If T.deleteAll() failed, FactoryBoy will call this deleteAll().
      * 
      * @param t
      * @throws SecurityException
      * @throws NoSuchMethodException
      * @throws InvocationTargetException
      * @throws IllegalArgumentException
      * @throws IllegalAccessException
      */
     protected static <T extends GenericModel> void deleteAll(Class<T> type) {
         ModelFactory<T> modelFactory = findModelFactory(type);
         Logger.info("Try Delete all %s item.", type.getName());
 
         Method deleteMethod = null;
         // check the delete(T) had been declared at the concrete ModelFactory
         try {
             deleteMethod = modelFactory.getClass().getMethod("delete",
                             type);
         } catch (Exception e) {
             throw new RuntimeException("Delete " + type.getName()
                             + " Failed! Please define delete("
                             + type.getName() + ") at the class "
                             + modelFactory.getClass().getName() + ".");
         }
 
         List<T> all;
         try {
             Method findAllMethod = type.getMethod("findAll", new Class<?>[] {});
             all = (List<T>) findAllMethod.invoke(type, new Object[] {});
             for (T t : all) {
                 deleteMethod.invoke(modelFactory, t);
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     public static synchronized <T extends GenericModel> ModelFactory<T> findModelFactory(
                     Class<T> clazz) {
         // If the Model has not delete after lazyDelete, delete it all.
         ModelFactory<T> modelFactory = (ModelFactory<T>) modelFactoryCacheMap
                         .get(clazz);
         if (modelFactory != null) {
             return modelFactory;
         }
         String clazzFullName = clazz.getName();
         String modelFactoryName = clazzFullName.replaceAll("^models\\.",
                         "factory.") + "Factory";
         try {
             modelFactory = (ModelFactory<T>) Class.forName(modelFactoryName)
                             .newInstance();
             modelFactoryCacheMap.put(clazz, modelFactory);
             return modelFactory;
         } catch (Exception e) {
             // Don't need throw the exception.
         }
         throw new RuntimeException("Can't find class:" + modelFactoryName);
     }
 
     /**
      * Create the <i>clazz</i> Object and SAVE it to Database.
      * 
      * @param clazz
      * @return
      */
     public static <T extends GenericModel> T create(Class<T> clazz) {
         T t = build(clazz);
         t.save();
         return t;
     }
 
     /**
      * Create the named <i>clazz</i> Object and SAVE it to Database.
      * 
      * @param clazz
      * @param name
      * @return
      */
     public static <T extends GenericModel> T create(Class<T> clazz, String name) {
         T t = build(clazz, name);
         t.save();
         return t;
     }
 
     public static <T extends GenericModel> T create(Class<T> clazz,
                     String name, BuildCallback<T> buildCallback) {
         T t = build(clazz, name, buildCallback);
         t.save();
         return t;
     }
 
     public static <T extends GenericModel> T create(Class<T> clazz,
                     BuildCallback<T> buildCallback) {
         T t = build(clazz, buildCallback);
         t.save();
         return t;
     }
 
     /**
      * Build the <i>clazz</i> Object, but NOT save it.
      * 
      * @param clazz
      * @return
      */
     public static <T extends GenericModel> T build(Class<T> clazz) {
         ModelFactory<T> modelFactory = findModelFactory(clazz);
         checkOrDeleteModel(clazz, modelFactory);
         T t = modelFactory.define();
         return t;
     }
 
     /**
      * Build the named <i>clazz</i> Object, but NOT save it.
      * 
      * @param clazz
      * @param name
      * @return
      */
     public static <T extends GenericModel> T build(Class<T> clazz, String name) {
 
         ModelFactory<T> modelFactory = findModelFactory(clazz);
         checkOrDeleteModel(clazz, modelFactory);
 
         T t = modelFactory.define();
 
         Method method = getModelDefineMethod(clazz, name, modelFactory);
         try {
             // process factory's base define method.
             Factory factory = method.getAnnotation(Factory.class);
             if (factory != null && StringUtils.isNotEmpty(factory.base())) {
                 try {
                     Method baseMethod = getModelDefineMethod(clazz,
                                     factory.base(), modelFactory);
                     t = invokeModelFactoryMethod(modelFactory, t, baseMethod);
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
             }
 
             t = invokeModelFactoryMethod(modelFactory, t, method);
         } catch (Exception e) {
             e.printStackTrace();
         }
 
         return t;
     }
 
     private static <T extends GenericModel> T invokeModelFactoryMethod(
                     ModelFactory<T> modelFactory, T t, Method baseMethod)
                     throws IllegalAccessException, InvocationTargetException {
         int parameterNumber = baseMethod.getParameterTypes().length;
         Object returnObject = null;
         switch (parameterNumber) {
         case 0:
             // void method
             returnObject = baseMethod.invoke(modelFactory, new Object[] {});
             break;
         case 1:
             returnObject = baseMethod.invoke(modelFactory, t);
             break;
         case 2:
             returnObject = baseMethod.invoke(modelFactory, t,
                             sequence(t.getClass()));
         }
         if (returnObject != null) {
             return (T) returnObject;
         }
         return t;
     }
 
     private static <T extends GenericModel> Method getModelDefineMethod(
                     Class<T> clazz, String name, ModelFactory<T> modelFactory)
     {
 
         Method[] allMethods = modelFactory.getClass().getMethods();
 
         for (Method method : allMethods) {
             Factory factory = method.getAnnotation(Factory.class);
             if (factory != null) {
                 String factoryName = factory.name();
                 if (name.equals(factoryName)) {
                     return method;
                 }
             }
         }
         throw new RuntimeException(
                         "Can't find any method with @Factory(name=" + name
                                         + " method at class "
                                         + modelFactory.getClass().getName()
                                         + ", Please define it.");
     }
 
     /**
      * Build the named <i>clazz</i> Object, but NOT save it.
      * 
      * @param clazz
      * @param name
      * @return
      */
     public static <T extends GenericModel> T build(Class<T> clazz, String name,
                     BuildCallback<T> buildCallback) {
         T t = build(clazz, name);
         buildCallback.build(t);
         return t;
     }
 
     public static <T extends GenericModel> T build(Class<T> clazz,
                     BuildCallback<T> buildCallback) {
         T t = build(clazz);
         buildCallback.build(t);
         return t;
     }
 
     /*
      * // TODO public static <T extends GenericModel> List<T> batchCreate(int
      * size, Class<T> clazz) { return null; }
      */
 
     public static <T extends GenericModel> List<T> batchCreate(int size,
                     Class<T> clazz, BuildCallback<T> sequenceCallback) {
         List<T> list = batchBuild(size, clazz, sequenceCallback);
         for (T t : list) {
             t.save();
         }
         return list;
     }
 
     public static <T extends GenericModel> List<T> batchBuild(int size,
                     Class<T> clazz, BuildCallback<T> buildCallback) {
         List<T> list = new ArrayList<T>();
         for (int i = 0; i < size; i++) {
             T t = build(clazz);
             buildCallback.build(t);
             list.add(t);
         }
         return list;
     }
 
     public static <T extends GenericModel> List<T> batchCreate(int size,
                     Class<T> clazz, String name, BuildCallback<T> buildCallback) {
         List<T> list = batchBuild(size, clazz, name, buildCallback);
         for (T t : list) {
             t.save();
         }
         return list;
     }
 
     public static <T extends GenericModel> List<T> batchBuild(int size,
                     Class<T> clazz, String name, BuildCallback<T> buildCallback) {
         List<T> list = new ArrayList<T>();
         for (int i = 0; i < size; i++) {
             T t = build(clazz, name);
             buildCallback.build(t);
             list.add(t);
         }
         return list;
     }
 
     public static synchronized int sequence(Class<?> clazz) {
         Integer seq = modelSequenceMap.get(clazz);
         if (seq == null) {
             seq = 0;
         }
         modelSequenceMap.put(clazz, ++seq);
         return seq;
     }
 }
