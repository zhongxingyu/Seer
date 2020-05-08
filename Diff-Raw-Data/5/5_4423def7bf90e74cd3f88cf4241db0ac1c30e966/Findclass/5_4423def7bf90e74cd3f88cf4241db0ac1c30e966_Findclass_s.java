 package org.spoofer.findclass;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipException;
 import java.util.zip.ZipFile;
 
 /**
  * The Findclass class scans one or more directory locations for a given java class name.
  * It locates the class file(s) within the given directory structures and contained JAR/zip files.
  * 
  * It's use was prompted by so many deployment headaches when class not found errors are thrown
  * or clashes occurred with two classes of the same name but different versions were caught in a class path.
  * 
 * Simple use is:  fc <class name to find> -classpath <direcotries to search, separated with a ;>
  * 
  * @author Rob Gilham
  *
  */
 public class Findclass {
 
 	private static final String ARG_PATH = "classpath";
 	private static final String ARG_PATH2 = "cp";
 	private static final String ARG_SUB_DIRS = "s";
 	private static final String ARG_HELP = "?";
 	private static final String ARG_VERBOSE = "v";
 	private static final String ARG_IGNORE_CASE = "i";
 
 	private static final String[] ZIP_FORMATS = new String[] {"jar", "zip", "war", "ear"};
 	private static final String CLASS_EXTENTION = ".class";
 	private static final String CLASSPATH_DELIMITER = ";";
 
 	private String searchClassName = null;	// The name of the class file to locate.
 	private boolean searchSubDirs = true;	// Flag to search the sub directories or a single directory
 	private boolean ignoreCase = true;		// Ignore the case of the class name
 	private boolean ignorePackage = false;  // Set true when search class has no package
 	private boolean verboseMode = false;	// Flag to control the output
 
 	private List<String> foundItems = new ArrayList<String>();
 
 	/**
 	 * Find the given class name within the given class path
 	 * @param className	The class name of the class to locate, without the trailing '.class'
 	 * @param classPath The class path to search.  Can be a single directory location or multiple locations, separated with a semi-colon.
 	 */
 	public void findClass(String className, String classPath)
 	{
 		foundItems.clear();
 
 		if (null == className)
 			throw new NullPointerException("class name can not be null");
 
 		searchClassName = className.toLowerCase().endsWith(CLASS_EXTENTION) ? 
 				className.substring(0, className.length() - CLASS_EXTENTION.length()).trim() :
 					className.trim();
 
 				ignorePackage = !searchClassName.contains(".");  // If package stated in search, then match only when full package name matches.
 
 				if (isVerboseMode())
 				{
 					System.out.println("Searching for class '" + searchClassName + "' in '" + classPath + "'");
 					System.out.println("Searching sub-directories: " + Boolean.toString(isSearchSubDirectories()));
 					System.out.println("Ignoring the name case: " + Boolean.toString(isIgnoreCase()));
 					System.out.println("Ignoring package names: " + Boolean.toString(ignorePackage));
 				}
 
 				String[] paths = classPath.split(CLASSPATH_DELIMITER);
 				for (int i=0; i < paths.length; i++)
 				{
 					if (paths[i].trim().length() > 0)
 					{
 						File root = new File(paths[i].trim());
 						if (null == root || !root.exists() || !root.canRead())
 							System.err.println("failed to open path '" + paths[i] + "'.  Ignoring location.");
 						else
 							scanFile(root);
 					}
 				}
 
 				String found;
 				if (foundItems.isEmpty())
 					found = "Did not locate any files called " + searchClassName;
 				else
 				{
 					found = "Found " + foundItems.size() + " file";
 					if (foundItems.size() > 1)
 						found += "s";
 				}
 				System.out.println(found);
 				for (String item: foundItems)
 					System.out.println(item);
 
 	}
 
 	/**
 	 * Recursively called method to scan a single location.
 	 * If the given location is a directory, its contents are scanned and all sub directories are
 	 * Recursively passed into this method.
 	 * All Archive files are extracted and passed to this method and single files are checked for matching the search name.
 	 * @param f The File location to check.  Can be a directory, archive or file.
 	 */
 	private void scanFile(File f)
 	{
 		if (f.isDirectory() && isSearchSubDirectories())
 		{
 			if (isVerboseMode())
 				System.out.println("Reading directory " + f.getName());
 
 			File[] files = f.listFiles();
 			if (null != files)
 				for (int i=0; i < files.length; i++)
 					scanFile(files[i]);
 			else
 				System.err.println("Error: Failed to read the contents of directory '" + f.getName() + "'");
 		}
 		else
 		{ // Otherwise its a file, so process it
 
 			String name = f.getName().toLowerCase();
 			if (name.endsWith(CLASS_EXTENTION))
 				processClass(f);
 
 			else // Check if it's a zip file
 				for(int i=0; i < ZIP_FORMATS.length; i++)
 				{
 					if (name.endsWith("." + ZIP_FORMATS[i]))
 					{
 						try {
 							processZip(f);
 						} catch (IOException e) {
 							if (isVerboseMode())
 								e.printStackTrace();
 							else
 								System.err.println(e.getMessage());
 						}
 						break;
 					}
 				}
 
 		} // End if for file/directory check
 	}
 
 	/**
 	 * Flag to control if a classpath locations'sub directories are included in the search.
 	 * By default all sub directory trees are scanned.  Setting this to false will prevent this,
 	 * and only scan the location given in the class path, ignoring the sub directories.
 	 * @param state true to scan all sub directories (Default), or false to scan just one layer.
 	 */
 	public void setSearchSubDirectories(boolean state)
 	{
 		searchSubDirs = state;
 	}
 	/**
 	 * Checks if the Finder will scan just the locations in the classpath or the sub directories of those locations also.
 	 * 
 	 * @return true (Default) if the sub directories are to be scanned, false to scan just the location.
 	 */
 	public boolean isSearchSubDirectories()
 	{
 		return searchSubDirs;
 	}
 
 
 	/**
 	 * Flag to control the amount of output to the Logger during the search process.
 	 * Default is False, to display just the basic information.
 	 * Setting to true increases the amount of output
 	 * @return true if detailed output is on, false (Default) otherwise
 	 */
 	public boolean isVerboseMode() {
 		return verboseMode;
 	}
 
 	/**
 	 * Flag that controls the amount of output to the Logger during the search process.
 	 * Default is False, to display just the basic information.
 	 * When true, the amount of output is more detailed during the search.
 	 * 
 	 * @param verboseMode true to increase the output or false to have basic output
 	 */
 	public void setVerboseMode(boolean verboseMode) {
 		this.verboseMode = verboseMode;
 	}
 
 
 	/**
 	 * Checks if the Finder will ignore the case of the given class file.
 	 * By default the class name is case sensitive.  Only classes with matching case will be found.
 	 * @return true (Default) if the case of the class must match the search name.
 	 * When false, any class that just matches the name, regardless of case, will be found.
 	 */
 	public boolean isIgnoreCase() {
 		return ignoreCase;
 	}
 
 	/**
 	 * Sets the case sensitivity of the Finder.  Default is true, so all searches are case sensitive.
 	 * @param ignoreCase when false, any class with a matching name, regardless of case is found, otherwise only exact matches are found.
 	 */
 	public void setIgnoreCase(boolean ignoreCase) {
 		this.ignoreCase = ignoreCase;
 	}
 
 
 	/**
 	 * Process the given single file to check if it a class that matches the search name.
 	 * If the given file is a match, then its name is added to the found classes list. 
 	 * @param f The File to check.
 	 */
 	private void processClass(File f)
 	{
 		if (isVerboseMode())
 			System.out.println("Checking class file " + f.getName());
 
 		if (isMatchingName(f.getName()))
 			outputFoundFile(f.getName(), f.getParentFile());
 	}
 
 
 	/**
 	 * Process the given archive file to check if it contains a class that matches the search name.
 	 * If the given file contains a match, then its name is added to the found classes list. 
 	 * @param f The Archive (jar/zip) File to check.
 	 */
 	private void processZip(File f) throws IOException
 	{
 		if (isVerboseMode())
 			System.out.println("Checking archive file " + f.getName());
 		
 		try {
 			ZipFile zip = new ZipFile(f);
 			Enumeration<? extends ZipEntry> enteries = zip.entries();
 			while (enteries.hasMoreElements())
 			{
 				ZipEntry entry = (ZipEntry)enteries.nextElement(); 
 				if (!entry.isDirectory())
 				{
 					String name = entry.getName().replace('/', '.');
 					if (isMatchingName(name))
 						outputFoundFile(name, f);
 				}
 
 			}
 		}catch(ZipException e) {
 			throw new IOException("An error has occured opening the zip file:\n" + f.getPath(), e);
 		}
 	}
 
 	/**
 	 * Adds the given class name, with the given parent directory, to the found list.
 	 * 
 	 * @param name	The name of the class that has matched the find string
 	 * @param parent The parent directory / archive file containing the found file.
 	 */
 	private void outputFoundFile(String name, File parent)
 	{
 		String parentType = parent.isDirectory() ? "directory" : "archive";
 		String item = name + " found in " + parentType + " " + parent.getAbsolutePath();
 		if (isVerboseMode())
 			System.out.println("*** " + name + " found in " + parent.getName() + " ***");
 
 		foundItems.add(item);
 	}
 
 	/**
 	 * Compares the given name with the search class name, according to the rules configured by the finder
 	 * @param name The name to check
 	 * @return true if the given name matches the find name, or false otherwise.
 	 */
 	private boolean isMatchingName(String name)
 	{
 		if (!name.toLowerCase().endsWith(CLASS_EXTENTION))
 			return false;
 		name = name.substring(0, name.length() - CLASS_EXTENTION.length()).trim();
 
 		if (ignorePackage)
 		{
 			int iPos = name.lastIndexOf(".");
 			if (iPos >= 0)
 				name = name.substring(iPos + 1);
 		}else if (name.length() > searchClassName.length())  // Otherwise, strip off any leading dir names from name
 			name = name.substring(0, searchClassName.length());
 
 		if (isIgnoreCase())
 			return searchClassName.equalsIgnoreCase(name);
 		else
 			return searchClassName.equals(name);
 	}
 
 	/**
 	 * Main entry point, processes the arguments and starts the search
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		Arguments arguments = new Arguments(args);
 
 		if (arguments.containsArgument(ARG_HELP))
 		{
 			showUse();
 			System.exit(0);
 		}
 		String className = arguments.getArgument(null);
 		if (null == className)
 		{
 			showUse();
 			System.err.println("No class name stated!");
 			System.exit(-1);
 		}
 
 		String classPath = arguments.containsArgument(ARG_PATH) ? arguments.getArgument(ARG_PATH) : arguments.getArgument(ARG_PATH2);
 		if (null == classPath || classPath.length() < 1)  // If no path stated, use current dir
 			classPath = new File(".").getAbsolutePath();
 
 		Findclass fc = new Findclass();
 		if (arguments.containsArgument(ARG_SUB_DIRS))  // Process the Sub directories switch
 			fc.setSearchSubDirectories(false);
 
 		fc.setVerboseMode(arguments.containsArgument(ARG_VERBOSE));  // Process the Verbose switch
 
 		fc.setIgnoreCase(arguments.containsArgument(ARG_IGNORE_CASE));
 
 		fc.findClass(className, classPath);
 	}
 
 	/**
 	 * Display the usage summary of the Finder class
 	 */
 	public static void showUse()
 	{
 		PrintStream out = System.out;
 
 		out.println();
 		out.println("Locates class files within directory and archive file structures.");
 		out.println("Give the name of the class you wish to find, and an optional path");
 		out.println("location to search to locate any class files with the given name.");
 		out.println("Copyright 2008 - Rob Gilham  (eurospoofer@yahoo.co.uk)\n");
		out.println("FC <class name> [-" + ARG_PATH + " <search file path[;additional paths]>]");
 		out.println("\t\t[-" + ARG_SUB_DIRS + " [-" + ARG_IGNORE_CASE + "]  [-" + ARG_VERBOSE + "]");
 		out.println();
 		out.println("Required arguments:");
 		out.println("<class name>\tThe name of the class to be found.\n\t\tWithout the .class extention, package name is optional.");
 		out.println();
 		out.println("Optional arguments:");
 		out.println("-" + ARG_PATH + " <class path>\tThe full file path(s) of the location(s) to search.");
 		out.println("\t\tIf not stated, default is the current directory.");
 		out.println("\t\tMultiple locations should be seperating with a '" + CLASSPATH_DELIMITER + "'.");
 		out.println("\t\tThe '-" + ARG_PATH + "' argument can be shortened to '-" + ARG_PATH2 + "'.");
 		out.println();
 		out.println("-" + ARG_SUB_DIRS + "\t\tsuppress the search of sub-directories.");
 		out.println("\t\tWhen stated, only searches the directories listed in " + ARG_PATH);
 		out.println("\t\tbut not the sub directories.");
 		out.println();
 		out.println("-" + ARG_IGNORE_CASE + " \t\tIgnore the class name case.");
 		out.println("\t\tWhen present, the case of the search name is ignored and\n\t\tany class matching the name,regardless of case, is found.");
 		out.println("\t\tWhen NOT present, the default, the case of the search\n\t\tclass must match the found class.");
 		out.println();
 		out.println("-" + ARG_VERBOSE + " \t\tVerbose output.\n\t\tOutputs detailed information on which files are being searched.");
 		out.println();
 		out.println("Note: If a package name is stated in the class name, then the matching classes\nmust also match the stated package.");
 		out.println("When no package is stated, then any class with a matching name is found,\nregardless of its package.");
 	}
 }
