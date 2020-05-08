 /*
  * Copyright 2012 Michael Hoffer <info@michaelhoffer.de>. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are
  * permitted provided that the following conditions are met:
  *
  *    1. Redistributions of source code must retain the above copyright notice, this list of
  *       conditions and the following disclaimer.
  *
  *    2. Redistributions in binary form must reproduce the above copyright notice, this list
  *       of conditions and the following disclaimer in the documentation and/or other materials
  *       provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY Michael Hoffer <info@michaelhoffer.de> "AS IS" AND ANY EXPRESS OR IMPLIED
  * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
  * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL Michael Hoffer <info@michaelhoffer.de> OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
  * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * The views and conclusions contained in the software and documentation are those of the
  * authors and should not be interpreted as representing official policies, either expressed
  * or implied, of Michael Hoffer <info@michaelhoffer.de>.
  */
 package eu.mihosoft.vtk;
 
 import java.lang.reflect.Field;
 import java.util.*;
 import java.util.Map.Entry;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
 * Simplifies handling of native libraries, i.e., returning a list of libraries
 * that are currently loaded.
 * 
 * @see http://stackoverflow.com/questions/1007861/how-do-i-get-a-list-of-jni-libraries-which-are-loaded/1008631#1008631
  *
  * @author Michael Hoffer
  */
 public class NativeUtil {
 
     private static Field loadedLibraryNames;
     private static Field systemNativeLibraries;
     private static Field nativeLibraries;
     private static Field nativeLibraryFromClass;
     private static Field nativeLibraryName;
     
     private static boolean valid;
 
 
     static {
         try {
             loadedLibraryNames = ClassLoader.class.getDeclaredField("loadedLibraryNames");
             loadedLibraryNames.setAccessible(true);
 
             systemNativeLibraries = ClassLoader.class.getDeclaredField("systemNativeLibraries");
             systemNativeLibraries.setAccessible(true);
 
             nativeLibraries = ClassLoader.class.getDeclaredField("nativeLibraries");
             nativeLibraries.setAccessible(true);
 
             Class<?> nativeLibrary = null;
             for (Class<?> nested : ClassLoader.class.getDeclaredClasses()) {
                 if (nested.getSimpleName().equals("NativeLibrary")) {
                     nativeLibrary = nested;
                     break;
                 }
             }
             nativeLibraryFromClass = nativeLibrary.getDeclaredField("fromClass");
             nativeLibraryFromClass.setAccessible(true);
 
             nativeLibraryName = nativeLibrary.getDeclaredField("name");
             nativeLibraryName.setAccessible(true);
             
             valid = true;
         } catch (NoSuchFieldException ex) {
             Logger.getLogger(NativeUtil.class.getName()).log(Level.SEVERE, null, ex);
         } catch (SecurityException ex) {
             Logger.getLogger(NativeUtil.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     /**
      * Indicates whether native util class has been correclty initialized.
      * @return the valid
      */
     public static boolean isValid() {
         return valid;
     }
 
     /**
      * Returns the names of native libraries loaded across all class loaders.
      * <p/>
      * @return a list of native libraries loaded
      */
     public static List<String> getLoadedLibraries() {
         try {
             @SuppressWarnings("UseOfObsoleteCollectionType")
             final Vector<String> result = (Vector<String>) loadedLibraryNames.get(null);
             return result;
         } catch (IllegalAccessException ex) {
             throw new AssertionError(ex);
         } catch (IllegalArgumentException ex) {
             throw new AssertionError(ex);
         }
     }
 
     /**
      * Returns the native libraries loaded by the system class loader.
      * <p/>
      * @return a Map from the names of native libraries to the classes that
      * loaded them
      */
     public static Map<String, Class<?>> getSystemNativeLibraries() {
         try {
             Map<String, Class<?>> result = new HashMap<String, Class<?>>();
             @SuppressWarnings("UseOfObsoleteCollectionType")
             final Vector<Object> libraries = (Vector<Object>) systemNativeLibraries.get(null);
             for (Object nativeLibrary : libraries) {
                 String libraryName = (String) nativeLibraryName.get(nativeLibrary);
                 Class<?> fromClass = (Class<?>) nativeLibraryFromClass.get(nativeLibrary);
                 result.put(libraryName, fromClass);
             }
             return result;
         } catch (IllegalAccessException ex) {
             throw new AssertionError(ex);
         } catch (IllegalArgumentException ex) {
             throw new AssertionError(ex);
         }
     }
 
     /**
      * Returns a Map from the names of native libraries to the classes that
      * loaded them.
      * <p/>
      * @param loader the ClassLoader that loaded the libraries
      * @return an empty Map if no native libraries were loaded
      */
     public static Map<String, Class<?>> getNativeLibraries(final ClassLoader loader) {
         try {
             Map<String, Class<?>> result = new HashMap<String, Class<?>>();
             @SuppressWarnings("UseOfObsoleteCollectionType")
             final Vector<Object> libraries = (Vector<Object>) nativeLibraries.get(loader);
             for (Object nativeLibrary : libraries) {
                 String libraryName = (String) nativeLibraryName.get(nativeLibrary);
                 Class<?> fromClass = (Class<?>) nativeLibraryFromClass.get(nativeLibrary);
                 result.put(libraryName, fromClass);
             }
             return result;
         } catch (IllegalAccessException ex) {
             throw new AssertionError(ex);
         } catch (IllegalArgumentException ex) {
             throw new AssertionError(ex);
         }
     }
 
     /**
      * The same as {@link #getNativeLibraries()} except that all ancestor
      * classloaders are processed as well.
      * <p/>
      * @param loader the ClassLoader that loaded (or whose ancestors loaded) the
      * libraries
      * @return an empty Map if no native libraries were loaded
      */
     public static Map<String, Class<?>> getTransitiveNativeLibraries(final ClassLoader loader) {
         Map<String, Class<?>> result = new HashMap<String,Class<?>>();
         ClassLoader parent = loader.getParent();
         while (parent != null) {
             result.putAll(getTransitiveNativeLibraries(parent));
             parent = parent.getParent();
         }
         result.putAll(getNativeLibraries(loader));
         return result;
     }
 
     /**
      * Converts a map of library names to the classes that loaded them to a map
      * of library names to the classloaders that loaded them.
      * <p/>
      * @param libraryToClass a map of library names to the classes that loaded
      * them
      * @return a map of library names to the classloaders that loaded them
      */
     public static Map<String, ClassLoader> getLibraryClassLoaders(Map<String, Class<?>> libraryToClass) {
         Map<String, ClassLoader> result = new HashMap<String,ClassLoader>();
         for (Entry<String, Class<?>> entry : libraryToClass.entrySet()) {
             result.put(entry.getKey(), entry.getValue().getClassLoader());
         }
         return result;
     }
 
     /**
      * Returns a list containing the classloader and its ancestors.
      * <p/>
      * @param loader the classloader
      * @return a list containing the classloader, its parent, and so on
      */
     public static List<ClassLoader> getTransitiveClassLoaders(ClassLoader loader) {
         List<ClassLoader> result = new ArrayList<ClassLoader>();
         ClassLoader parent = loader.getParent();
         result.add(loader);
         while (parent != null) {
             result.add(parent);
             parent = parent.getParent();
         }
         return result;
     }
 }
