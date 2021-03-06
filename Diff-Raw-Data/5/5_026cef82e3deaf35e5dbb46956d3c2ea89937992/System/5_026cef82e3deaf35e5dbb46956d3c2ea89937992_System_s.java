 /*
  * Java core library component.
  *
  * Copyright (c) 1997, 1998
  *      Transvirtual Technologies, Inc.  All rights reserved.
  *
  * See the file "license.terms" for information on usage and redistribution
  * of this file.
  */
 
 package java.lang;
 
 import gnu.classpath.SystemProperties;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.FileDescriptor;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.PrintStream;
 import java.lang.reflect.Array;
 import java.util.Properties;
 import java.util.PropertyPermission;
 
 import kaffe.lang.ThreadStack;
 
 public final class System {
 	final public static InputStream in;
 	final public static PrintStream out;
 	final public static PrintStream err;
 
 // When trying to debug Java code that gets executed early on during
 // JVM initialization, eg, before System.err is initialized, debugging
 // println() statements don't work. In these cases, the following routines
 // are very handy. Simply uncomment the following two lines to enable them.
 /****
 public static native void debug(String s);	// print s to stderr, then \n
 public static native void debugE(Throwable t);	// print stack trace to stderr
 ****/
 static {
 	// Initialise the I/O
 	if (SystemProperties.getProperty("kaffe.embedded", "false").equals("false")) {
 		in = new BufferedInputStream(new FileInputStream(FileDescriptor.in), 128);
 		out = new PrintStream(new BufferedOutputStream(new FileOutputStream(FileDescriptor.out), 128), true);
 		err = new PrintStream(new BufferedOutputStream(new FileOutputStream(FileDescriptor.err), 128), true);
 	} else {
 		in = new BufferedInputStream(new kaffe.io.StdInputStream(), 128);
 		out = new PrintStream(new BufferedOutputStream(new kaffe.io.StdOutputStream(), 128), true);
 		err = new PrintStream(new BufferedOutputStream(new kaffe.io.StdErrorStream(), 128), true);
 	}
 }
 
 private System() { }
 
 public static void arraycopy(Object src, int src_position, Object dst, int dst_position, int length) {
     
     if (src == null)
 	throw new NullPointerException("src == null");
 
     if (dst == null)
 	throw new NullPointerException("dst == null");
 
     if (length == 0)
 	return; 	 
 
     final Class source_class = src.getClass();
 
     if (!source_class.isArray())
 	throw new ArrayStoreException("source is not an array: " + source_class.getName());
 
     final Class destination_class = dst.getClass();
 
     if (!destination_class.isArray())
 	throw new ArrayStoreException("destination is not an array: " + destination_class.getName());
 
     if (src_position < 0)
 	throw new ArrayIndexOutOfBoundsException("src_position < 0: " + src_position);
 
     final int src_length = Array.getLength(src);
 
    if (src_position + length > src_length)
 	throw new ArrayIndexOutOfBoundsException("src_position + length > src.length: " + src_position + " + " + length + " > " + src_length);
 
     if (dst_position < 0)
 	throw new ArrayIndexOutOfBoundsException("dst_position < 0: " + dst_position);
 
     final int dst_length = Array.getLength(dst);
 
    if (dst_position + length > dst_length)
 	throw new ArrayIndexOutOfBoundsException("dst_position + length > dst.length: " + dst_position + " + " + length + " > " + dst_length);
 
     if (length < 0)
 	throw new ArrayIndexOutOfBoundsException("length < 0: " + length);
 
     arraycopy0(src, src_position, dst, dst_position, length);
 }
 
 private static native void arraycopy0(Object src, int src_position, Object dst, int dst_position, int length);
 
 private static void checkPropertiesAccess() {
 	SecurityManager sm = getSecurityManager();
 	if (sm != null)
 		sm.checkPropertiesAccess();
 }
 
 private static void checkPropertyAccess(String key) {
 	SecurityManager sm = getSecurityManager();
 	if (key.length() == 0)
 		throw new IllegalArgumentException("key can't be empty");
 	if (sm != null)
 		sm.checkPropertyAccess(key);
 }
 
 native public static long currentTimeMillis();
 
 public static void exit (int status) {
 	Runtime.getRuntime().exit(status);
 }
 
 public static void gc() {
 	Runtime.getRuntime().gc();
 }
 
   /* taken from GNUClasspath */
   public static Properties getProperties()
   {
     SecurityManager sm = SecurityManager.current; // Be thread-safe.
     if (sm != null)
       sm.checkPropertiesAccess();
     return SystemProperties.getProperties();
   }
   public static String getProperty(String key)
   {
     SecurityManager sm = SecurityManager.current; // Be thread-safe.
     if (sm != null)
       sm.checkPropertyAccess(key);
     else if (key.length() == 0)
       throw new IllegalArgumentException("key can't be empty");
     return SystemProperties.getProperty(key);
   }
   public static String getProperty(String key, String def)
   {
     SecurityManager sm = SecurityManager.current; // Be thread-safe.
     if (sm != null)
       sm.checkPropertyAccess(key);
     return SystemProperties.getProperty(key, def);
   }
 
 public static SecurityManager getSecurityManager() {
 	return Runtime.securityManager;
 }
 
 /* Adapted from GNU Classpath */
 public static String getenv(String name) {
     if (name == null)
       throw new NullPointerException();
     SecurityManager sm = Runtime.securityManager; // Be thread-safe.
     if (sm != null)
       sm.checkPermission(new RuntimePermission("getenv." + name));
     return getenv0(name);
 }
 native private static String getenv0(String name);
 
 native public static int identityHashCode(Object x);
 
 native private static Properties initProperties(Properties properties);
 
 public static void load(String filename) {
 	Runtime.getRuntime().load(filename,
 	    ThreadStack.getCallersClassLoader(false));
 }
 
 public static void loadLibrary(String libname) {
 	Runtime.getRuntime().loadLibrary(libname,
 	    ThreadStack.getCallersClassLoader(false));
 }
 
 public static String mapLibraryName(String fn) {
 	return Runtime.getRuntime().mapLibraryName(fn);
 }
 
 public static void runFinalization() {
 	Runtime.getRuntime().runFinalization();
 }
 
 public static void runFinalizersOnExit(boolean value) {
 	Runtime.runFinalizersOnExit(value);
 }
 
 private static void exitJavaCleanup() {
 	Runtime.getRuntime().exitJavaCleanup();
 }
 
 public static void setErr(PrintStream err) {
 	// XXX call security manager for RuntimePermission("SetIO")
 	setErr0(err);
 }
 
 native private static void setErr0(PrintStream err);
 
 public static void setIn(InputStream in) {
 	// XXX call security manager for RuntimePermission("SetIO")
 	setIn0(in);
 }
 
 native private static void setIn0(InputStream in);
 
 public static void setOut(PrintStream out) {
 	// XXX call security manager for RuntimePermission("SetIO")
 	setOut0(out);
 }
 
 native private static void setOut0(PrintStream out);
 
 
   /* taken from GNU Classpath */
   public static String setProperty(String key, String value)
   {
     SecurityManager sm = SecurityManager.current; // Be thread-safe.
     if (sm != null)
       sm.checkPermission(new PropertyPermission(key, "write"));
     return SystemProperties.setProperty(key, value);
   }
   public static void setProperties(Properties properties)
   {
     SecurityManager sm = SecurityManager.current; // Be thread-safe.
     if (sm != null)
       sm.checkPropertiesAccess();
     SystemProperties.setProperties(properties);
   }
 
 public static void setSecurityManager(SecurityManager s) {
 	if (Runtime.securityManager != null) {
 		Runtime.securityManager.checkPermission(
 			new RuntimePermission("setSecurityManager"));
 	}
 	Runtime.securityManager = s;
 }
 }
