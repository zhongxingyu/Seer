 /*******************************************************************************
  * Copyright (c) 2010 Oracle.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * and Apache License v2.0 which accompanies this distribution. 
  * The Eclipse Public License is available at
  *     http://www.eclipse.org/legal/epl-v10.html
  * and the Apache License v2.0 is available at 
  *     http://www.opensource.org/licenses/apache2.0.php.
  * You may elect to redistribute this code under either of these licenses.
  *
  * Contributors:
  *     mkeith - Gemini JPA work 
  ******************************************************************************/
 package org.eclipse.gemini.jpa;
 
 import java.io.Closeable;
 import java.lang.reflect.Array;
 
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.BundleEvent;
 import org.osgi.framework.ServiceReference;
 import org.osgi.service.packageadmin.PackageAdmin;
 
 /**
  * Utility class containing functions that are generally useful during 
  * development and at runtime.
  */
 @SuppressWarnings({"rawtypes","unchecked", "deprecation"})
 public class GeminiUtil {
     
     /*==================*/
     /* Static constants */
     /*==================*/
     
     public static String JPA_JDBC_DRIVER_PROPERTY = "javax.persistence.jdbc.driver";
     public static String JPA_JDBC_URL_PROPERTY = "javax.persistence.jdbc.url";
     public static String JPA_JDBC_USER_PROPERTY = "javax.persistence.jdbc.user";
     public static String JPA_JDBC_PASSWORD_PROPERTY = "javax.persistence.jdbc.password";
     
     /*============================*/
     /* Helper and Utility methods */
     /*============================*/
 
     // Function to obtain the version from a bundle
     public static String bundleVersion(Bundle b) {
         return b.getVersion().toString();
     }
     
     // Function to return a package String formatted with "." instead of "/"
     public static String formattedPackageString(String s, char beingReplaced, char replacer) {
         String formatted = s;
         // Replace all instances of character
         if (formatted.indexOf(beingReplaced) >= 0) 
             formatted = formatted.replace(beingReplaced, replacer);
         // Tack on trailing character if needed
         if (formatted.charAt(formatted.length()-1) != replacer) 
             formatted = formatted + replacer;
         return formatted;
     }
     
     // Function to close a closeable (as much as it can be closed)
     public static void close(Closeable c) {
         try { c.close(); } catch (Throwable ex){}
     }
     
     // Obtain and return the PackageAdmin    
     public static PackageAdmin getPackageAdmin(BundleContext ctx) {
         ServiceReference ref = ctx.getServiceReference(PackageAdmin.class.getName());
         return (ref != null) 
             ? (PackageAdmin) ctx.getService(ref)
             : null;
     }
     
     // Strip off preceding slash, if present, and return the resulting string
     public static String stripPrecedingSlash(String s) {
         if (s == null || s.length()==0 || !s.startsWith("/")) 
             return s;
         return (s.length() == 1) 
             ? "" 
             : s.substring(1, s.length());
     }
     
     // Load a class using the specified bundle; fatal exception if not found
     public static Class<?> loadClassFromBundle(String clsName, Bundle b) {
 
         debug("Loading class ", clsName, " from bundle ", b);
         try {
             return b.loadClass(clsName);
         } catch (ClassNotFoundException cnfEx) {
             fatalError("Could not load class " + clsName + " from bundle " + b, cnfEx);
         }
         return null;
     }
     
     /*==================*/
     /* Status functions */
     /*==================*/
     
     // Function to throw a runtime exception (throws exception)
     public static void fatalError(String s, Throwable t) { 
         System.out.println("*** FATAL ERROR *** " + s);
         if (t != null) 
             t.printStackTrace(System.out);
         throw new RuntimeException(s,t); 
     }
 
     // Function to indicate a warning condition (non-terminating)
     public static void warning(String msg) {
         warning(msg, "");
     }
 
     // Function to indicate a warning condition (non-terminating)
     public static void warning(String msg, Throwable t) {
         String msg2 = (t != null ? (" Exception: " + t) : "");
         warning(msg, msg2);
     }
 
     // Function to indicate a warning condition (non-terminating)
     public static void warning(String msg, String msg2) {
         String outputMsg = "WARNING: " + msg + msg2;  
         System.out.println(outputMsg);
     }
 
     /*=====================*/
     /* Debugging functions */
     /*=====================*/
     
     // Function to print out debug strings for XML parsing purposes
     public static void debugXml(String... msgs) { 
         if (GeminiProperties.debugXml()) {
             privateDebug(msgs);
         }
     }
     
     // Function to print out debug strings for classloading purposes
     public static void debugClassLoader(String... msgs) { 
         if (GeminiProperties.debugClassloader()) {
             privateDebug(msgs);
         }
     }
 
     // Function to print out debug string and classloader info for classloading debugging
     public static void debugClassLoader(String s, ClassLoader cl) { 
         if (GeminiProperties.debugClassloader()) {
             System.out.println(s + String.valueOf(cl));
             ClassLoader p = cl;
             while (p.getParent() != null) {
                 System.out.println("  Parent loader: " + p.getParent());
                 p = p.getParent();
             }
         }
     }
 
     // Function to print out debug strings for weaving purposes
     public static void debugWeaving(String... msgs) { 
         if (GeminiProperties.debugWeaving()) {
             privateDebug(msgs);
         }
     }
     
     // Function to print out series of debug strings
     public static void debug(String... msgs) { 
         if (GeminiProperties.debug()) {
             privateDebug(msgs);
         }
     }
 
     // Function to print out series of objects
     public static void debug(Object... args) {
         if (GeminiProperties.debug()) {
             privateDebug(args);
         }
     }
 
     // Function to print out a string and an object.
     // Handles some objects specially and prints out more info
     public static void debug(String msg, Object obj) { 
         if (GeminiProperties.debug()) {
             if (obj == null) {
                 System.out.println(msg + String.valueOf(obj));
             } else if ((ClassLoader.class.isAssignableFrom(obj.getClass())) &&
                        (GeminiProperties.debugXml())) {
                 debugClassLoader(msg, (ClassLoader)obj);
             } else if (Bundle.class.isAssignableFrom(obj.getClass())) {
                 Bundle b = (Bundle) obj;
                 System.out.println(msg + " bundle=" + b.getSymbolicName() + 
                                          " id=" + b.getBundleId()+ 
                                          " state=" + stringBundleStateFromInt(b.getState()));
             } else if (BundleEvent.class.isAssignableFrom(obj.getClass())) {
                 BundleEvent event = (BundleEvent) obj;
                 System.out.println(msg + " bundle=" + event.getBundle().getSymbolicName() + 
                         ", event=" + stringBundleEventFromInt(event.getType())); 
             } else if (obj.getClass().isArray()) {
                 System.out.println(msg);
                 int len = ((Object[])obj).length;
                 for (int i=0; i<len; i++) {
                     System.out.print("  ");
                     System.out.println(String.valueOf(Array.get(obj, i)));                    
                 }
             } else {
                 System.out.println(msg + String.valueOf(obj));
             }
         }
     }
     
     public static String stringBundleStateFromInt(int bundleState) {
         switch (bundleState) {
             case 1: return "UNINSTALLED";
             case 2: return "INSTALLED";
             case 4: return "RESOLVED";
             case 8: return "STARTING";
             case 16: return "STOPPING";
             case 32: return "ACTIVE";
            default: return "UNDEFINED_STATE";
         }
     }
     
     public static String stringBundleEventFromInt(int eventType) {
         switch (eventType) {
             case 1: return "INSTALLED";
             case 2: return "STARTED";
             case 4: return "STOPPED";
             case 8: return "UPDATED";
             case 16: return "UNINSTALLED";
             case 32: return "RESOLVED";
             case 64: return "UNRESOLVED";
             case 128: return "STARTING";
             case 256: return "STOPPING";
             case 512: return "LAZY_ACTIVATION";
            default: return "UNDEFINED_EVENT";
         }
     }
 
     /*===================*/
     /* Private functions */
     /*===================*/
     
     // Private function to print out series of debug strings
     public static void privateDebug(String... msgs) { 
         StringBuilder sb = new StringBuilder();
         for (String msg : msgs) sb.append(msg);
         System.out.println(sb.toString()); 
     }
 
     // Private function to print out series of debug objects
     public static void privateDebug(Object... args) { 
         String[] msgs = new String[args.length];
         for (int i=0; i<args.length; i++)
             msgs[i] = String.valueOf(args[i]);
         privateDebug(msgs);
     }
 }
