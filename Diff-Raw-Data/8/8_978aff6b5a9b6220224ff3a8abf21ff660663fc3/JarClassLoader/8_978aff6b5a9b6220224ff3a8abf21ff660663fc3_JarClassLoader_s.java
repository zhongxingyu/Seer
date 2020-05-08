 package com.atlassian.plugin.loaders.classloading;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.jar.JarFile;
 import java.util.jar.JarInputStream;
 import java.util.zip.ZipEntry;
 
 /**
  * User: Hani Suleiman & Mike Cannon-Brookes
  * (originally sort-of copied from WebWork1 source)
  */
 public class JarClassLoader extends PluginsClassLoader
 {
     private static class FileBytes
     {
         private byte[] data;
 
         private FileBytes(byte[] data)
         {
             this.data = data;
         }
     }
 
     private static class InnerJar
     {
         private JarFile jar;
         private ZipEntry entry;
 
         private InnerJar(JarFile jar, ZipEntry entry)
         {
             this.jar = jar;
             this.entry = entry;
         }
 
         private byte[] getFile(String path)
         {
             // Check for a null path
             if (path == null) return null;
             // Get the file
             try
             {
                 // Get a new input stream
                 JarInputStream jarStream = new JarInputStream(jar.getInputStream(entry));
                 // Find the entry for this path
                 ZipEntry eachEntry;
                 while ((eachEntry = jarStream.getNextEntry()) != null)
                 {
                     if (path.equals(eachEntry.getName()))
                     {
                         break;
                     }
                 }
                 // Check I found something
                 if (eachEntry == null) return null;
                 // Grab the content
                 ByteArrayOutputStream out = new ByteArrayOutputStream();
                 try
                 {
                     byte[] buffer = new byte[2048];
                     int read;
                     while (jarStream.available() > 0)
                     {
                         read = jarStream.read(buffer, 0, buffer.length);
                         if (read < 0) break;
                         out.write(buffer, 0, read);
                     }
                     // Return the contents
                     return out.toByteArray();
                 } finally
                 {
                     out.close();
                 }
             } catch (IOException e)
             {
                 log.error(e, e);
                 return null;
             }
         }
     }
 
     private JarFile jar;
     private File file;
    private LinkedList innerLibraries;
     private HashMap cachedFiles = new HashMap();
 
     public JarClassLoader(File file, ClassLoader parent)
     {
         super(parent);
         this.file = file;
     }
 
     protected URL getDataURL(String name, byte[] data) throws MalformedURLException
     {
         return new URL(null, file.toURL().toExternalForm() + '!' + name, new BytesURLStreamHandler(data));
     }
 
     private void openJar() throws IOException
     {
         if (jar == null)
         {
             jar = new JarFile(file);
         }
     }
 
     public synchronized void loadInnerLibraries()
     {
         // Check I have a jar
         if (jar == null) return;
         innerLibraries = new LinkedList();
         // Cycle through the entries
         Enumeration entries = jar.entries();
         while (entries.hasMoreElements())
         {
             ZipEntry entry = (ZipEntry) entries.nextElement();
             String name = entry.getName();
             if (name.startsWith("META-INF/lib/") && name.endsWith(".jar"))
             {
                innerLibraries.add(new InnerJar(jar, entry));
             }
         }
     }
 
     public synchronized byte[] getFile(String path)
     {
         // Check the cache
         FileBytes cacheLookup = (FileBytes) cachedFiles.get(path);
         if (cacheLookup != null)
         {
             return cacheLookup.data;
         }
         //
         InputStream in = null;
         try
         {
             openJar();
             ZipEntry entry = jar.getEntry(path);
             // Dan Hardiker :: Check the libraries if there are any
             if (entry == null)
             {
                 if (innerLibraries == null)
                 {
                     loadInnerLibraries();
                 }
                 // Cycle through them trying to grab the file
                 byte[] data;
                 for (Iterator iter = innerLibraries.iterator(); iter.hasNext();)
                 {
                    InnerJar innerJar = (InnerJar) iter.next();
                     data = innerJar.getFile(path);
                     if (data != null)
                     {
                         cachedFiles.put(path, new FileBytes(data));
                         return data;
                     }
                 }
                 // Still nothing? - oh dear, what a pity, never mind
                 cachedFiles.put(path, new FileBytes(null));
                 return null;
             }
             // Dan Hardiker ::
 
             in = jar.getInputStream(entry);
             int size = (int) entry.getSize();
             byte[] data = readStream(in, size);
 
             // Cache it
             cachedFiles.put(path, new FileBytes(data));
 
             return data;
         }
         catch (IOException e)
         {
             return null;
         }
         finally
         {
             // ensure that we close the jar inputStream. Can not rely upon the readStream
             // method to do this.
             if (in != null)
             {
                 try
                 {
                     in.close();
                 }
                 catch (IOException e)
                 {
                     // noop.
                 }
             }
         }
     }
 
     public Object clone()
     {
         JarClassLoader loader = new JarClassLoader(file, getParent());
         loader.packages = packages;
         return loader;
     }
 
     /**
      * Close the jar open jar file.
      */
     public void close()
     {
         try
         {
             if (jar != null)
                 jar.close();
 
             jar = null;
         }
         catch (IOException e)
         {
             log.error("Error closing JAR: " + e, e);
         }
     }
 }
