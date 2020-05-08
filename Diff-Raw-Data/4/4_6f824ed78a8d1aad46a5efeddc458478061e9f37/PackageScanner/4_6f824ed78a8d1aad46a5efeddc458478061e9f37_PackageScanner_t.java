 package org.snowfk.util;
 
 import java.io.*;
 import java.util.jar.*;
 import java.util.*;
 import java.net.*;
 
 import org.apache.commons.lang.*;
 import org.apache.commons.io.*;
 
 /**
  * Contributed by: Steve van Loben Sels, http://metapossum.com/ (Thank you!)
  *
  */
 public abstract class PackageScanner<T> {
 
     protected abstract T getItemIfAcceptable(File dir, String fileName, Set<T> entries);
 
     protected abstract T getItemIfAcceptable(JarEntry entry, Set<T> entries);
 
     
 
     protected String packageName;
     protected ClassLoader classLoader;
 
     public PackageScanner(String packageName, ClassLoader classLoader) {
         this.packageName = packageName;
         this.classLoader = classLoader;
     }
 
 
     public Set<T> scan(boolean recursive)
         throws IOException
     {
         Set<T> set = new HashSet<T>();
 
        packageName = packageName.replace('.', '/');
         Enumeration<URL> dirs = classLoader.getResources(packageName);
 
         T result;
         while (dirs.hasMoreElements()) {
             String path = URLDecoder.decode(dirs.nextElement().getPath(), "UTF-8");
 
             if (path.contains(".jar!")) {
                 String jarName = path.substring("file:".length());
                 jarName = jarName.substring(0, jarName.indexOf('!'));
 
                 JarFile jarFile = new JarFile(jarName);
 
                 Enumeration<JarEntry> entries = jarFile.entries();
                 while (entries.hasMoreElements()) {
                     JarEntry entry = entries.nextElement();
 
                     String entryPackage = StringUtils.substringBeforeLast(entry.getName(), File.separator);
                     if(packageName.equals(entryPackage) || (recursive && entryPackage.startsWith(packageName))) {
                         if((result = getItemIfAcceptable(entry, set)) != null) {
                             set.add(result);
                         }
                     }
                 }
             } else {
                 File dir = new File(path);
                 for(Object obj : FileUtils.listFiles(dir,  null, recursive)) {
                     File file = (File) obj;
                     if((result = getItemIfAcceptable(file.getParentFile(), file.getName(), set)) != null) {
                         set.add(result);
                     }
                 }
             }
         }
 
         return set;
     }
 }
