 package factory;
 
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang.StringUtils;
 
 import play.Logger;
 import play.db.jpa.GenericModel;
 import play.db.jpa.Model;
 import play.test.Fixtures;
 import factory.annotation.Factory;
 
 
 public class FactoryBoy {
 
 	protected static Map<Class<?>, ModelFactory<?>> modelFactoryCacheMap = new HashMap<Class<?>, ModelFactory<?>>();
 	
	public static <T extends GenericModel> void init(Class<T>... clazzes) {
 		Fixtures.delete(clazzes);
 		
         for (Class<? extends GenericModel> type : clazzes) {
             try {
                 Model.Manager.factoryFor(type).deleteAll();
             } catch(Exception e) {
                 Logger.error(e, "While deleting " + type + " instances");
             }
         }
     }

 	public static synchronized <T extends GenericModel> ModelFactory<T> findModelFactory(Class<T> clazz) {
 		ModelFactory<T> modelFactory = (ModelFactory<T>) modelFactoryCacheMap.get(clazz);
 		if (modelFactory != null) {
 			return modelFactory;
 		}
 		String clazzFullName = clazz.getName();
 		String modelFactoryName = clazzFullName.replaceAll("^models\\.", "factory.") + "Factory";
 	    try {
 	        modelFactory = (ModelFactory<T>)Class.forName(modelFactoryName).newInstance();
 	        modelFactoryCacheMap.put(clazz, modelFactory);
 	        return modelFactory;
         } catch (Exception e) {
 	        // Don't need throw the exception.
         }
 		throw new RuntimeException("Can't find class:" + modelFactoryName);
     }
 
 	/**
 	 * Create the <i>clazz</i> Object and SAVE it to Database.
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
 	 * @param clazz
 	 * @param name
 	 * @return
 	 */
 	public static <T extends GenericModel> T create(Class<T> clazz, String name) {
 		T t = build(clazz, name);
 		t.save();
 		return t;
     }
 
 	public static <T extends GenericModel> T create(Class<T> clazz, String name, BuildCallBack<T> buildCallBack) {
 		T t = build(clazz, name, buildCallBack);
 		t.save();
 		return t;
     }
 
 	public static <T extends GenericModel> T create(Class<T> clazz,
             BuildCallBack<T> buildCallBack) {
 		T t = build(clazz, buildCallBack);
 		t.save();
 	    return t;
     }
 	
 	/**
 	 * Build the <i>clazz</i> Object, but NOT save it.
 	 * @param clazz
 	 * @return
 	 */
 	public static <T extends GenericModel> T build(Class<T> clazz) {
 		ModelFactory<T> modelFactory = findModelFactory(clazz);
 		T t = modelFactory.define();
 	    return t;
     }	
 	
 	/**
 	 * Build the named <i>clazz</i> Object, but NOT save it.
 	 * @param clazz
 	 * @param name
 	 * @return
 	 */
 	public static <T extends GenericModel> T build(Class<T> clazz, String name) {
 
 		ModelFactory<T> modelFactory = findModelFactory(clazz);
 		T t = modelFactory.define();		
 		
 		try {
 			Method method = getModelDefineMethod(clazz, name, modelFactory);
 	        if (method == null) {
 	        	return t;
 	        }
 	        Factory factory = method.getAnnotation(Factory.class);
 	        if (factory != null && StringUtils.isNotEmpty(factory.base())) {
 	        	try {
 	                Method baseMethod = getModelDefineMethod(clazz, factory.base(), modelFactory);
 	                t = (T) baseMethod.invoke(modelFactory, t);
                 } catch (Exception e) {
 	                e.printStackTrace();
                 }
 	        }
 	        
 	        t = (T) method.invoke(modelFactory, t);
         } catch (Exception e) {
 	        e.printStackTrace();
         }
 
 	    return t;
     }
 
 	private static <T extends GenericModel> Method getModelDefineMethod(Class<T> clazz, String name,
             ModelFactory<T> modelFactory) throws NoSuchMethodException {
 		
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
 		return null;
     }
 	
 	/**
 	 * Build the named <i>clazz</i> Object, but NOT save it.
 	 * @param clazz
 	 * @param name
 	 * @return
 	 */
 	public static <T extends GenericModel> T build(Class<T> clazz, String name, BuildCallBack<T> buildCallBack) {
 		T t = build(clazz, name);
 		buildCallBack.build(t);
 	    return t;
     }
 
 	public static <T extends GenericModel> T build(Class<T> clazz,
             BuildCallBack<T> buildCallBack) {
 	    T t = build(clazz);
 	    buildCallBack.build(t);
 	    return t;
     }
 	
 	/*
 	// TODO
 	public static <T extends GenericModel> List<T> batchCreate(int size, Class<T> clazz) {
 	    return null;
     }
 	*/
 	
 	public static <T extends GenericModel> List<T> batchCreate(int size, Class<T> clazz, SequenceCallBack<T> sequenceCallBack) {
 		List<T> list = batchBuild(size, clazz, sequenceCallBack);
 		for (T t : list) {
 	        t.save();
         }
 		return list;
 	}
 	
 	public static <T extends GenericModel> List<T> batchBuild(int size, Class<T> clazz, SequenceCallBack<T> sequenceCallBack) {
 	    List<T> list = new ArrayList<T>();
 	    for (int i = 0; i < size; i++) {
 	    	T t = build(clazz);
 	    	sequenceCallBack.sequence(t, FactoryBoy.sequence(clazz));
 	    	list.add(t);
 	    }
 	    return list;
     }
 
 	public static <T extends GenericModel> List<T> batchCreate(int size, Class<T> clazz, String name, SequenceCallBack<T> sequenceCallBack) {
 		List<T> list = batchBuild(size, clazz, name, sequenceCallBack);
 		for (T t : list) {
 	        t.save();
         }
 		return list;
 	}
 	
 	public static <T extends GenericModel> List<T> batchBuild(int size, Class<T> clazz, String name, SequenceCallBack<T> sequenceCallBack) {
 	    List<T> list = new ArrayList<T>();
 	    for (int i = 0; i < size; i++) {
 	    	T t = build(clazz, name);
 	    	sequenceCallBack.sequence(t, FactoryBoy.sequence(clazz));
 	    	list.add(t);
 	    }
 	    return list;
     }
 	
 	public static int sequence(Class<?> clazz) {
 		Class<? extends GenericModel> type = (Class<? extends GenericModel>)clazz;
 		ModelFactory<? extends GenericModel> modelFactory = findModelFactory(type);
 	    return modelFactory.sequence();
     }
 }
