 package org.denevell.rocklobster.utils;
 
 import java.io.File;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.log4j.Logger;
 import org.reflections.Reflections;
 import org.reflections.scanners.ResourcesScanner;
 import org.reflections.scanners.SubTypesScanner;
 import org.reflections.util.ClasspathHelper;
 import org.reflections.util.ConfigurationBuilder;
 import org.reflections.util.FilterBuilder;
 
 import com.google.common.collect.Lists;
 
 public class ClassUtils {
 	
 	private static Logger LOG = LogUtils.getLog(ClassUtils.class);
 	
 	public static <T>List<T> getClasses(String packageName, T typeOfClass) throws Exception {
 		return getClasses(typeOfClass, packageName);
 	}
 	
 	/**
 	 * @param directory MUST end with a slash
 	 */
 	public static <T extends Class>List<T> getClassesIncludingDirecory(String packageName, T typeOfClass, String directory) throws Exception {
 		List<T> classes = setClassesFromDirectory(directory, typeOfClass);
 		List<T> classesInSourceFiles = getClasses(typeOfClass, packageName);
 		classes.addAll(classesInSourceFiles);
 		return classes;
 	}
 
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	private static <T extends Class>List<T> setClassesFromDirectory(String directory, T typeOfClass) throws Exception {
 		ArrayList<T> allClasses = new ArrayList<T>();
 		File file = new File(directory);
 		// Get class loader
 		URL url = file.toURI().toURL(); 
 		URL[] urls = new URL[]{url};
 		ClassLoader cl = new URLClassLoader(urls); 		
 		
 		// Load the classes
 		Collection<File> files = FileUtils.listFiles(file, new String[] {"class"}, true);
 		for (File f: files) {
 			String name = f.getCanonicalPath();
 			String n = name.replaceFirst(".*?"+directory, "");
 			n = n.replace(".class", "");
 			n = n.replace(File.separator, ".");
 			Class<?> loadClass = cl.loadClass(n);
 			if(typeOfClass.isAssignableFrom(loadClass)) {
 				T c = (T) loadClass;
 				allClasses.add(c);
 			} else {
 				LOG.warn(typeOfClass.getCanonicalName() + "was not assignable from " + n);
 			}
 		}
 		return allClasses;
 	}
 
 	@SuppressWarnings("unchecked")
 	private static <T> List<T> getClasses(T typeOfClass, String packageName) {
 		List<ClassLoader> classLoadersList = new LinkedList<ClassLoader>();
 		classLoadersList.add(ClasspathHelper.contextClassLoader());
 		classLoadersList.add(ClasspathHelper.staticClassLoader());
 		
 		Reflections reflections = new Reflections(new ConfigurationBuilder()
 		    .setScanners(new SubTypesScanner(false /* don't exclude Object.class */), new ResourcesScanner())
 		    .setUrls(ClasspathHelper.forClassLoader(classLoadersList.toArray(new ClassLoader[0])))
 		    .filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix(packageName))));	
 		
 		Class<T> type = (Class<T>) typeOfClass;
 		Set<T> allClasses = (Set<T>) reflections.getSubTypesOf(type);
 		ArrayList<T> newArrayList = Lists.newArrayList(allClasses);
 		return newArrayList;
 	}
 	
 }
