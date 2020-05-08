 /*
  * Plugins.java
  *
  * Created on April 18, 2003, 10:57 AM
  */
 /*****************************************************************************
  * Copyright (c) 2003 Sun Microsystems, Inc.  All Rights Reserved.
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *
  * - Redistribution of source code must retain the above copyright notice,
  *   this list of conditions and the following disclaimer.
  *
  * - Redistribution in binary form must reproduce the above copyright notice,
  *   this list of conditions and the following disclaimer in the documentation
  *   and/or other materails provided with the distribution.
  *
  * Neither the name Sun Microsystems, Inc. or the names of the contributors
  * may be used to endorse or promote products derived from this software
  * without specific prior written permission.
  *
  * This software is provided "AS IS," without a warranty of any kind.
  * ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
  * ANY IMPLIED WARRANT OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
  * NON-INFRINGEMEN, ARE HEREBY EXCLUDED.  SUN MICROSYSTEMS, INC. ("SUN") AND
  * ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS
  * A RESULT OF USING, MODIFYING OR DESTRIBUTING THIS SOFTWARE OR ITS
  * DERIVATIVES.  IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST
  * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
  * INCIDENTAL OR PUNITIVE DAMAGES.  HOWEVER CAUSED AND REGARDLESS OF THE THEORY
  * OF LIABILITY, ARISING OUT OF THE USE OF OUR INABILITY TO USE THIS SOFTWARE,
  * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
  *
  * You acknowledge that this software is not designed or intended for us in
  * the design, construction, operation or maintenance of any nuclear facility
  *
  *****************************************************************************/
 package net.java.games.util.plugins;
 
 /**
  *
  * @author  jeff
  */
 
 import java.io.*;
 import java.util.*;
 import java.util.jar.*;
 
 /** This is the application interface to the Plugin system.
  * One Plugins object should be created for each plug-in
  * directory tree root.
  *
  * On creation the Plugins object will scan its assigned
  * directory tree and examine all Jar files in that tree to
  * see if they qualify as Plug-ins.
  *
  * The Plugin classes may then be retrived from the Plugins object by calling
  * the appropriate get function (see below).
  *
  * If a plugin requires a native code library, that library must be present
  * in the same directory as the plugin Jar file UNLESS the property
  * "net.java.games.util.plugins.nolocalnative" is set.  In that case
  * it will fall abck to the VM or environment's default way of finding
  * native libraries. (This is n ecessary for Java Web Start apps.)
  *
  */
 public class Plugins {
     static final boolean DEBUG = true;
     List pluginList= new ArrayList();
 
     /** Creates a new instance of Plugins
      * @param pluginRoot The root od the directory tree to scan for Jars
      * containing plugins.
      */
     public Plugins(File pluginRoot) throws IOException {
             scanPlugins(pluginRoot);
     }
 
     private void scanPlugins(File dir) throws IOException {
         File[] files = dir.listFiles();
         if (files == null) {
             throw new FileNotFoundException("Plugin directory "+dir.getName()+
                 " not found.");
         }
         for(int i=0;i<files.length;i++){
             File f = files[i];
             if (f.getName().endsWith(".jar")) { // process JAR file
                 processJar(f);
             } else if (f.isDirectory()) {
                 scanPlugins(f); // recurse
             }
         }
     }
 
 
     private void processJar(File f) {
         try {
             //JarFile jf = new JarFile(f);
             if (DEBUG) {
                 System.out.println("Scanning jar: "+f.getName());
             }
             PluginLoader loader = new PluginLoader(f);
             JarFile jf = new JarFile(f);
            for (Enumeration enum = jf.entries();enum.hasMoreElements();){
                JarEntry je = (JarEntry)enum.nextElement();
                 if (DEBUG) {
                     System.out.println("Examining file : "+je.getName());
                 }
                 if (je.getName().endsWith("Plugin.class")) {
                     if (DEBUG) {
                         System.out.println("Found candidate class: "+je.getName());
                     }
                     String cname = je.getName();
                     cname = cname.substring(0,cname.length()-6);
                     cname = cname.replace('/','.'); // required by JDK1.5
                     Class pc = loader.loadClass(cname);
                     if (loader.attemptPluginDefine(pc)) {
                         if (DEBUG) {
                             System.out.println("Adding class to plugins:"+pc.getName());
                         }
                         pluginList.add(pc);
                     }
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     /** This method returns all the Plugins found in the
      * directory passed in at object creation time or any of its
      * sub-directories.
      * @return An array of Plugin objects
      */
     public Class[] get(){
         Class[] pluginArray = new Class[pluginList.size()];
         return (Class[])pluginList.toArray(pluginArray);
     }
 
     /** This method returns a sub-list of all the found Plugin
      * classes that implement <B>any</B> of the passed in set of
      * Interfaces (either directly or through inheritance.)
      * @param interfaces A set of interfaces to match against the interfaces
      * implemented by the plugin classes.
      * @return The list of plugin classes that implement at least
      * one member of the passed in set of interfaces.
      */
     public Class[] getImplementsAny(Class[] interfaces){
         List matchList = new ArrayList(pluginList.size());
         Set interfaceSet = new HashSet();
         for(int i=0;i<interfaces.length;i++){
             interfaceSet.add(interfaces[i]);
         }
         for(Iterator i = pluginList.iterator();i.hasNext();){
             Class pluginClass = (Class)i.next();
             if (classImplementsAny(pluginClass,interfaceSet)){
                 matchList.add(pluginClass);
             }
         }
         Class[] pluginArray = new Class[matchList.size()];
         return (Class[])matchList.toArray(pluginArray);
     }
 
     private boolean classImplementsAny(Class testClass,Set interfaces){
         if (testClass == null) return false; // end of tree
         Class[] implementedInterfaces = testClass.getInterfaces();
         for(int i=0;i<implementedInterfaces.length;i++){
             if (interfaces.contains(implementedInterfaces[i])) {
                 return true;
             }
         }
         for(int i=0;i<implementedInterfaces.length;i++){
             if (classImplementsAny(implementedInterfaces[i],interfaces)){
                 return true;
             }
         }
         return classImplementsAny(testClass.getSuperclass(),interfaces);
     }
 
     /** This method returns a sub-list of all the found Plugin
      * classes that implement <B>all</B> of the passed in set of
      * Interfaces (either directly or through inheritance.)
      * @param interfaces A set of interfaces to match against the interfaces
      * implemented by the plugin classes.
      * @return The list of plugin classes that implement at least
      * one member of the passed in set of interfaces.
      */
     public Class[] getImplementsAll(Class[] interfaces){
         List matchList = new ArrayList(pluginList.size());
         Set interfaceSet = new HashSet();
         for(int i=0;i<interfaces.length;i++){
             interfaceSet.add(interfaces[i]);
         }
         for(Iterator i = pluginList.iterator();i.hasNext();){
             Class pluginClass = (Class)i.next();
             if (classImplementsAll(pluginClass,interfaceSet)){
                 matchList.add(pluginClass);
             }
         }
         Class[] pluginArray = new Class[matchList.size()];
         return (Class[])matchList.toArray(pluginArray);
     }
 
     private boolean classImplementsAll(Class testClass,Set interfaces){
         if (testClass == null) return false; // end of tree
         Class[] implementedInterfaces = testClass.getInterfaces();
         for(int i=0;i<implementedInterfaces.length;i++){
             if (interfaces.contains(implementedInterfaces[i])) {
                 interfaces.remove(implementedInterfaces[i]);
                 if (interfaces.size() == 0) { // found them all
                     return true;
                 }
             }
         }
         for(int i=0;i<implementedInterfaces.length;i++){
             if (classImplementsAll(implementedInterfaces[i],interfaces)){
                 return true;
             }
         }
         return classImplementsAll(testClass.getSuperclass(),interfaces);
     }
 
     /** This method returns a sub-list of all the found Plugin
      * classes that extend the passed in Class
      * (either directly or through inheritance.)
      * @param superclass The class to match.
      * @return The list of plugin classes that extend the passed
      * in class.
      */
     public Class[] getExtends(Class superclass){
         List matchList = new ArrayList(pluginList.size());
         for(Iterator i = pluginList.iterator();i.hasNext();){
             Class pluginClass = (Class)i.next();
             if (classExtends(pluginClass,superclass)){
                 matchList.add(pluginClass);
             }
         }
         Class[] pluginArray = new Class[matchList.size()];
         return (Class[])matchList.toArray(pluginArray);
     }
 
     private boolean classExtends(Class testClass,Class superclass){
         if (testClass == null) { // end of hirearchy
             return false;
         }
         if (testClass == superclass) {
             return true;
         }
         return classExtends(testClass.getSuperclass(),superclass);
     }
 }
