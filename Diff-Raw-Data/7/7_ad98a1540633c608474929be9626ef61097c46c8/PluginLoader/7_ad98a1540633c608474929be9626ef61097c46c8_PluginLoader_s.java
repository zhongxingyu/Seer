 package org.Zeitline;
 /********************************************************************
 
 This file is part of org.Zeitline.Zeitline: a forensic timeline editor
 
 Written by Florian Buchholz and Courtney Falk.
 
 Copyright (c) 2004-2006 Florian Buchholz, Courtney Falk, Purdue
 University. All rights reserved.
 
 Permission is hereby granted, free of charge, to any person obtaining
 a copy of this software and associated documentation files (the
 "Software"), to deal with the Software without restriction, including
 without limitation the rights to use, copy, modify, merge, publish,
 distribute, sublicense, and/or sell copies of the Software, and to
 permit persons to whom the Software is furnished to do so, subject to
 the following conditions:
 
 Redistributions of source code must retain the above copyright notice,
 this list of conditions and the following disclaimers.
 Redistributions in binary form must reproduce the above copyright
 notice, this list of conditions and the following disclaimers in the
 documentation and/or other materials provided with the distribution.
 Neither the names of Florian Buchholz, Courtney Falk, CERIAS, Purdue
 University, nor the names of its contributors may be used to endorse
 or promote products derived from this Software without specific prior
 written permission.
 
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 NON-INFRINGEMENT.  IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT
 HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
 IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE
 SOFTWARE.
 
 **********************************************************************/
 
 import org.Zeitline.InputFilter.InputFilter;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.util.Enumeration;
 import java.util.Vector;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 
 public class PluginLoader extends ClassLoader {
     // used to filter out files in a directory with a ".class" extension
     private FilenameFilter class_filter = new FilenameFilter() {
         public boolean accept(File dir, String name) {
             return name.endsWith(".class");
         } // accept
     };
     private static final String PACKAGE_NAME = "org.Zeitline.filters.";
 
     public Enumeration getPluginsFromJar(String jar_location, String dir_name) {
         Vector result = new Vector();
         
 	String jar_file_name = (new File(jar_location)).getAbsolutePath();
 
         // create the JarFile object
         JarFile jar_file = null;
         try {
             jar_file = new JarFile(jar_file_name);
         }
         catch(java.io.FileNotFoundException fnf) {
             System.err.println(fnf);
             return result.elements();
         }
         catch(java.io.IOException io) {
             System.err.println(io);
             return result.elements();
         }
         if(jar_file == null) {
             System.err.println("Jar file is null");
             return result.elements();
         }
         
         // look through all of the Jar file's entries for classes in the
         // provided plugin directory
         Enumeration jar_file_entries = jar_file.entries();
         String entry_name = "";
         JarEntry jar_entry = null;
         BufferedInputStream instream = null;
         String class_name = "";
         int class_size = -1;
         byte[] class_data = null;
         Class class_def = null;
         InputFilter class_inst = null;
         while(jar_file_entries.hasMoreElements()) {
             entry_name = jar_file_entries.nextElement().toString();
             if(entry_name.startsWith(dir_name) && (entry_name.indexOf(".class") >= 0)) {
                 // get the actual JarEntry
                 jar_entry = jar_file.getJarEntry(entry_name);
                 class_name = jar_entry.toString();
                 class_name = class_name.substring((dir_name.length() + 1),
                     class_name.lastIndexOf(".class"));
                 
                 // pull the data of the Class out of the JarEntry
                 class_size = new Long(jar_entry.getSize()).intValue();
                 class_data = new byte[class_size];
                 try {
                     // open the stream to the JarEntry
                     instream = new BufferedInputStream(jar_file.getInputStream(jar_entry));
                     instream.read(class_data, 0, class_size);
                     instream.close();
                 }
                 catch(IOException io) {
                     System.err.println(io);
                     continue;
                 }
                 
                 // create the Class object
                 class_def = defineClass(class_name, class_data, 0, class_size);
                 resolveClass(class_def);
                 
                 // instantiate a copy of the new org.Zeitline.InputFilter.org.Zeitline.InputFilter object
                 try {
                     class_inst = (InputFilter) class_def.newInstance();
                 }
                 catch(InstantiationException inst) {
                     System.err.println(inst);
                     continue;
                 }
                 catch(IllegalAccessException ill) {
                     System.err.println(ill);
                     continue;
                 }
                 catch(ClassCastException cc) {
                     System.err.println(cc);
                    // It's a bug. Go to context menu Run > Edit Configuration and then Application > Zeitline.
                    System.err.println("If you're using IntelliJ IDEA, " +
                            "make sure you turn off \"Enable Capturing Form Snapshots\"");
                     continue;
                 }
                 
                 // add the new org.Zeitline.InputFilter.org.Zeitline.InputFilter to the results
                 result.addElement(class_inst);
             }
         }
         
         return result.elements();
     } // getPluginsFromJar
     
     public Enumeration getPluginsFromDir(String dir_name) {
         Vector result = new Vector();
         String file_name = "",
             class_name = "";
         int class_size = -1;
         byte[] class_data = null;
         FileInputStream instream = null;
         Class class_def = null;
         InputFilter class_inst = null;
         
         // get all files ending in ".class" from the provided directory
         File plugin_dir = new File(dir_name);
 	if (!plugin_dir.exists())
 	    return null;
         String[] children = plugin_dir.list(class_filter);
         for(int i = 0; i < children.length; i++) {
             file_name = plugin_dir.toString() + "/" + children[i];
             // get the name of the class from the name of the file less the ".class" extension
             class_name = children[i].substring(0, (children[i].length() - 6));
             
             // determine the size of the class file in bytes
             class_size = new Long(new File(file_name).length()).intValue();
             class_data = new byte[class_size];
             
             // read in the class file
             try {
                 instream = new FileInputStream(file_name);
                 instream.read(class_data, 0, class_size);
                 instream.close();
             }
             catch(IOException io) {
                 System.err.println(io);
                 continue;
             }
             
             // create the Class object
 	    if (findLoadedClass(class_name) == null) {
 		class_def = defineClass(PACKAGE_NAME + class_name, class_data, 0, class_size);
 		resolveClass(class_def);
             }
 	    else {
 		System.err.println("Warning -- filter of name: " + class_name + 
 				   " is already loaded, not loading this instance.");
 		continue;
 	    }
             // instantiate a copy of the new org.Zeitline.InputFilter.org.Zeitline.InputFilter object
             try {
                 class_inst = (InputFilter) class_def.newInstance();
             }
             catch(InstantiationException inst) {
                 System.err.println(inst);
                 continue;
             }
             catch(IllegalAccessException ill) {
                 System.err.println(ill);
                 continue;
             }
             catch(ClassCastException cc) {
                 System.err.println(cc);
                 continue;
             }
             
             // add the new org.Zeitline.InputFilter.org.Zeitline.InputFilter to the results
             result.addElement(class_inst);
         }
         
         return result.elements();
     } // getPluginsFromDir
 } // class org.Zeitline.PluginLoader
