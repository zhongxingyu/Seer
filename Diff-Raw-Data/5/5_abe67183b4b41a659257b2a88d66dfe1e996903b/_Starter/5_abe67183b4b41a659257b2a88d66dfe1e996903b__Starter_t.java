 /*
  * Maven Packaging Plugin,
  * Maven plugin to package a Project (deb, ipk, izpack)
  * Copyright (C) 2000-2008 tarent GmbH
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License,version 2
  * as published by the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
  * 02110-1301, USA.
  *
  * tarent GmbH., hereby disclaims all copyright
  * interest in the program 'Maven Packaging Plugin'
  * Signature of Elmar Geese, 11 March 2008
  * Elmar Geese, CEO tarent GmbH.
  */
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.LinkedList;
 
import org.apache.commons.io.IOUtils;

 /**
  * This class is added to each application that uses the advanced starter
  * and is therefore licensed under the GPL plus linking exception (see
  * disclaimer).
  * 
  * <p>The class' main method reads a "_classpath" file from the
  * resources, gathers the real main classname from it and constructs
  * a complex classpath.</p>
  * 
  * <p>Slashes in filenames are replaced with the platform-specific separator
  * char. Occurances of the real separator char in the filename causes no
  * harm.</p>
  * 
  * <p>The starter class can be used on Windows systems when the maximum length
  * of a command-line is a problem for an application.</p>
  * 
  * @author Robert Schuster
  * 
  */
 public class _Starter {
 	
 	private static String parse(String resource, LinkedList<URL> urls) {
 		String mainClassName = null;
 		
 		try
 		  {
 		    BufferedReader reader = new BufferedReader(new InputStreamReader(
 		    		_Starter.class.getResourceAsStream(resource)));
 		    
 		    String line = null;
 		    while ((line = reader.readLine()) != null)
 		      {
 		    	// Lines starting with a dash are comments
 		    	if (line.startsWith("#")){
 		    	  continue;
 		    	} else if (mainClassName == null) 
 		    	{
 		    		mainClassName = line; // The first non-dashed line is evaluated here.
 		    	} else
 		    	  {
 		    		// Afterwards every other line is interpreted as a class path entry.
 					String fixedFilename = line.replace('/', File.separatorChar);
 					try
 					  {
 					    urls.add(new File(fixedFilename).toURI().toURL());
 					  }
 					catch (MalformedURLException mue)
 					  {
 						System.err.println("Unable to handle classpath entry: " + fixedFilename + " Ignoring ...");
 					  }
 		    	  }
 		      }
 		  }
 		catch (IOException ioe)
 		  {
 			throw new RuntimeException("Unable to load _classpath", ioe);
 		  }
 		
 		return mainClassName;
 	}
 
 	public static void main(String[] args) {
 		LinkedList<URL> urls = new LinkedList<URL>();
 		String mainClassName = parse("_classpath", urls);
 		
 		URLClassLoader ucl = new URLClassLoader((URL[]) urls.toArray(new URL[urls.size()]));
 		Method m = null;
 		try
 		  {
 		    Class<?> klazz = ucl.loadClass(mainClassName);
 		    
 		    m = klazz.getMethod("main", new Class[] { String[].class });
 		  }
 		catch (ClassNotFoundException e)
 		  {
 			throw new RuntimeException("Unable to load main class " + mainClassName, e);
 		  } catch (SecurityException e) {
 				throw new RuntimeException("SecurityManager prohibited operation", e);
 		} catch (NoSuchMethodException e) {
 			throw new RuntimeException("Unable to find a main() method in class " + mainClassName, e);
		} finally {
			IOUtils.closeQuietly(ucl);
 		}
 		
 		try {
 			m.invoke(null, new Object[]{ args });
 		} catch (IllegalArgumentException e) {
 			throw new RuntimeException("Unable to execute main() method", e);
 		} catch (IllegalAccessException e) {
 			throw new RuntimeException("Unable to execute main() method", e);
 		} catch (InvocationTargetException e) {
 			Throwable c = e.getCause();
 			if (c instanceof Error) {
 				throw (Error) c;
 			} else if (c instanceof RuntimeException) {
 				throw (RuntimeException) c;
 			} else {
 				throw new RuntimeException(c);
 			}
 		}
 	}
 }
