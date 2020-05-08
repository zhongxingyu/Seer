 package com.ijg.darklight.core.loader;
 
 import java.lang.reflect.Constructor;
 
 public class DarklightLoader {
 	/**
 	 * 
 	 * @param classToLoad The desired class to load
	 * @return The loaded class returned by classLoader.loadClass
 	 */
 	public static Class<?> loadClass(String classToLoad) {
 		ClassLoader classLoader = DarklightLoader.class.getClassLoader();
 		try {
 			Class<?> loadedClass = classLoader.loadClass(classToLoad);
 			return loadedClass;
 		} catch (ClassNotFoundException e) {
 			System.out.println("[DarklightLoader] Error, class \"" + classToLoad + "\" not found");
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * This method is to load and instantiate classes with constructors
 	 * @param classToLoad The class to load
 	 * @param constructorArgs The arguments for the class constructor
 	 * @param constructorArgTypes The argument types for the class constructor
	 * @return A new instance of the class
 	 */
 	public static Object loadAndInstantiateClass(String classToLoad, Object[] constructorArgs, Class<?>... constructorArgTypes) {
 		Class<?> loadedClass = loadClass(classToLoad);
 		try {
 			Constructor<?> classConstructor = loadedClass.getConstructor(constructorArgTypes);
 			return classConstructor.newInstance(constructorArgs);
 		} catch (Exception e) {
 			System.out
 					.println("[DarklightLoader] There was an error loading class \""
 							+ classToLoad + "\":");
 			e.printStackTrace();
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * This method is to load and instantiate class without constructors
 	 * @param classToLoad The class to load
	 * @return A new instance of the class
 	 */
 	public static Object loadAndInstantiateClass(String classToLoad) {
 		Class<?> loadedClass = loadClass(classToLoad);
 		try {
 			return loadedClass.newInstance();
 		} catch (Exception e) {
 			System.out
 			.println("[DarklightLoader] There was an error loading class \""
 					+ classToLoad + "\":");
 			e.printStackTrace();
 		}
 		return null;
 	}
 }
