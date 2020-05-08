 /*
  *  Copyright (C) 2012 Ed Schaller <schallee@darkmist.net>
  *
  *  This library is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU Lesser General Public
  *  License as published by the Free Software Foundation; either
  *  version 2.1 of the License, or (at your option) any later version.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  *  Lesser General Public License for more details.
  *
  *  You should have received a copy of the GNU Lesser General Public
  *  License along with this library; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 
 package net.darkmist.alib.jvm;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class JVMLauncher
 {
 	private static final Class<JVMLauncher> CLASS = JVMLauncher.class;
 	private static final Logger logger = LoggerFactory.getLogger(CLASS);
 	private static File JAVA;
 
 	private static List<String> getPossibleExecutableExtensions()
 	{
 		String pathext;
 			
 		if((pathext = System.getenv("PATHEXT"))== null)
 			return Collections.singletonList("");
 		return Arrays.asList(pathext.split(";"));
 	}
 
 	private static final File getJavaHome() throws LauncherException
 	{
 		String homePath;
 		File home;
 
 		if((homePath = System.getProperty("java.home"))==null)
 			throw new LauncherException("Standard java property java.home does not exist.");
 		home = new File(homePath);
 		if(!home.exists())
 			throw new LauncherException("Java home directory " + homePath + " does not exist.");
 		if(!home.isDirectory())
 			throw new LauncherException("Java home directory " + homePath + " exists but is not a directory.");
 		return home;
 	}
 
 	private static final File getJavaHomeBin() throws LauncherException
 	{
 		File bin;
 
 		bin = new File(getJavaHome(), "bin");
 		if(!bin.exists())
 			throw new LauncherException("Java home bin directory " + bin.getPath() + " does not exist.");
 		if(!bin.isDirectory())
 			throw new LauncherException("Java home bin directory " + bin.getPath() + " exists but is not a directory.");
 		return bin;
 	}
 
 	public static final synchronized File getJavaFile() throws LauncherException
 	{
 		File bin;
 		File java;
 
 		if(JAVA != null)
 			return JAVA;
 		bin = getJavaHomeBin();
 		for(String ext : getPossibleExecutableExtensions())
 		{
 			java = new File(bin, "java" + ext);
 			if(java.isFile() && java.canExecute())
 				return (JAVA = java);
 		}
 		throw new LauncherException("Unable to find java executable in " + bin.getPath());
 	}
 
 	public static String getJavaPath() throws LauncherException
 	{
 		return getJavaFile().getPath();
 	}
 
 	private static List<URL> addClassPathURLsFor(List<URL> list, ClassLoader clsLoader)
 	{
 		ClassLoader parent;
 
 		if((parent = clsLoader.getParent())!=null)
 			addClassPathURLsFor(list, parent);
 		if(clsLoader instanceof URLClassLoader)
 			list.addAll(Arrays.asList(((URLClassLoader)clsLoader).getURLs()));
 		return list;
 	}
 
 	private static List<URL> getClassPathURLsFor(Class<?> cls)
 	{
 		List<URL> urls = addClassPathURLsFor(new ArrayList<URL>(), cls.getClassLoader());
 		logger.debug("Computed urls for {}: {}", cls, urls);
 		return urls;
 	}
 
	private static String getClassPathFor(Class<?> cls)
	{
		return mkPath(getClassPathURLsFor(cls));
	}

 	private static String mkPath(List<URL> urls)
 	{
 		String delim = System.getProperty("path.separator", ":");
 		StringBuilder sb = new StringBuilder();
 
 		for(URL url : urls)
 			sb.append(url.toString()).append(delim);
 		return sb.substring(0, sb.length() - delim.length());
 	}
 
 	/**
 	 * Get the system property java.class.path.
 	 * @return The value of the system property java.class.path
 	 * @deprecated Use {@link System#getProperty(String)} directly.
 	 */
 	@Deprecated
 	public static String getClassPath() throws LauncherException
 	{
 		return System.getProperty("java.class.path");
 	}
 
 	/**
 	 * Tries to find a class by name using the thread context class
 	 * loader and this class's class loader.
 	 * @param name Fully qualified name of the class to find
 	 * @return The class found.
 	 * @throws LauncherException if the class was not found.
 	 */
 	private static Class<?> findClass(String name) throws LauncherException
 	{
 		for(ClassLoader clsLoader : new ClassLoader[]{Thread.currentThread().getContextClassLoader(), CLASS.getClassLoader()})
 			try
 			{
 				return clsLoader.loadClass(name);
 			}
 			catch(ClassNotFoundException ignored)
 			{
 			}
 		throw new LauncherException("Unable to find class " + name + " via either the thread context class loader nor our own class loader.");
 	}
 
 	/**
 	 * Get a process loader for a JVM. The class path for the JVM
 	 * is computed based on the class loaders for the main class.
 	 * @param mainClass Fully qualified class name of the main class
 	 * @param args Additional command line parameters
 	 * @return ProcessBuilder that has not been started.
 	 * @deprecated This acquires the mainClass class by using the
 	 * {@link Thread#getContextClassLoader() thread context class loader}
 	 * or this class's class loader. Depending on where mainClass
 	 * was loaded from neither of these may work.
 	 * @see #getProcessBuilder(Class, String[])
 	 */
 	@Deprecated
 	public static ProcessBuilder getProcessBuilder(String mainClass, String...args) throws LauncherException
 	{
 		return getProcessBuilder(findClass(mainClass), Arrays.asList(args));
 	}
 
 	/**
 	 * Get a process loader for a JVM. The class path for the JVM
 	 * is computed based on the class loaders for the main class.
 	 * @param mainClass Main class to run
 	 * @param args Additional command line parameters
 	 * @return ProcessBuilder that has not been started.
 	 */
 	public static ProcessBuilder getProcessBuilder(Class<?> mainClass, String...args) throws LauncherException
 	{
 		return getProcessBuilder(mainClass, Arrays.asList(args));
 	}
 
 	/**
 	 * Get a process loader for a JVM. The class path for the JVM
 	 * is computed based on the class loaders for the main class.
 	 * @param mainClass Main class to run
 	 * @param args Additional command line parameters
 	 * @return ProcessBuilder that has not been started.
 	 */
 	public static ProcessBuilder getProcessBuilder(Class<?> mainClass, List<String> args) throws LauncherException
 	{
 		return getProcessBuilder(mainClass.getName(), getClassPathURLsFor(mainClass), args);
 	}
 
 	/**
 	 * Get a process loader for a JVM.
 	 * @param mainClass Main class to run
 	 * @param classPath List of urls to use for the new JVM's
 	 * 	class path.
 	 * @param args Additional command line parameters
 	 * @return ProcessBuilder that has not been started.
 	 */
 	public static ProcessBuilder getProcessBuilder(String mainClass, List<URL> classPath, String...args) throws LauncherException
 	{
 		return getProcessBuilder(mainClass, classPath, Arrays.asList(args));
 	}
 
 	/**
 	 * Get a process loader for a JVM.
 	 * @param mainClass Main class to run
 	 * @param classPath List of urls to use for the new JVM's
 	 * 	class path.
 	 * @param args Additional command line parameters
 	 * @return ProcessBuilder that has not been started.
 	 */
 	public static ProcessBuilder getProcessBuilder(String mainClass, List<URL> classPath, List<String> args) throws LauncherException
 	{
 		List<String> cmdList = new ArrayList<String>();
 		String[] cmdArray;
 		
 		cmdList.add(getJavaPath());
 		if(classPath != null && classPath.size() > 0)
 		{
 			cmdList.add("-cp");
 			cmdList.add(mkPath(classPath));
 		}
 		cmdList.add(mainClass);
 		if(args!=null && args.size()>0)
 			cmdList.addAll(args);
 		cmdArray = cmdList.toArray(new String[cmdList.size()]);
 		if(logger.isDebugEnabled())
 			logger.debug("cmdArray=" + Arrays.toString(cmdArray));
 		return new ProcessBuilder(cmdArray);
 	}
 
 	/**
 	 * Get a process for a JVM. The class path for the JVM is computed
 	 * based on the class loaders for the main class.
 	 * @param mainClass Fully qualified class name of the main class
 	 * @param args Additional command line parameters
 	 * @return ProcessBuilder that has not been started.
 	 * @deprecated This acquires the mainClass class by using the
 	 * {@link Thread#getContextClassLoader() thread context class loader}
 	 * or this class's class loader. Depending on where mainClass
 	 * was loaded from neither of these may work.
 	 * @see #getProcessBuilder(Class, String[])
 	 */
 	@Deprecated
 	public static Process getProcess(String mainClass, String... args) throws LauncherException, IOException
 	{
 		return getProcessBuilder(mainClass, args).start();
 	}
 
 	/**
 	 * Get a process for a JVM. The class path for the JVM is computed
 	 * based on the class loaders for the main class.
 	 * @param mainClass Fully qualified class name of the main class
 	 * @param args Additional command line parameters
 	 * @return ProcessBuilder that has not been started.
 	 * @deprecated Use {@link #getProcessBuilder(Class,String[])}
 	 * 	instead.
 	 */
 	@Deprecated
 	public static Process getProcess(Class<?> mainClass, String... args) throws LauncherException, IOException
 	{
 		return getProcessBuilder(mainClass, args).start();
 	}
 }
