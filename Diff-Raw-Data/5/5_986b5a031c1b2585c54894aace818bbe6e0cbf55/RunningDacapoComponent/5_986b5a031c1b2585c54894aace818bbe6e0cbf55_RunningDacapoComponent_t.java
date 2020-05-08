 package org.kevoree.microsandbox.samples.benchmark.dacapo;
 
 import org.kevoree.annotation.*;
 import org.kevoree.framework.AbstractComponentType;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.InputStream;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.Hashtable;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 
 /**
  * Created with IntelliJ IDEA.
  * User: inti
  * Date: 9/5/13
  * Time: 2:22 PM
  * To change this template use File | Settings | File Templates.
  */
 @DictionaryType({
         @DictionaryAttribute(name = "dacapo_path",dataType = String.class),
         @DictionaryAttribute(name = "dacapo_test", dataType = String.class)
 })
 @ComponentType
 public class RunningDacapoComponent extends AbstractComponentType {
 
 
     private String path;
     private String test;
     private ClassLoader loader;
 
     class DacapoClassLoader extends ClassLoader {
 
         private Hashtable classes = new Hashtable();
 
         public DacapoClassLoader() {
             super(DacapoClassLoader.class.getClassLoader()); //calls the parent class loader's constructor
         }
 
         @Override
         public Class<?> loadClass(String name) throws ClassNotFoundException {
                 return findClass(name);
         }
 
         public Class findClass(String className) {
             byte classByte[];
             Class result = null;
 
             result = (Class) classes.get(className); //checks in cached classes
             if (result != null) {
                 return result;
             }
 
             try {
                 return findSystemClass(className);
             } catch (Exception e) {
             }
 
             try {
                 JarFile jar = new JarFile(path);
                 JarEntry entry = jar.getJarEntry(className + ".class");
                 InputStream is = jar.getInputStream(entry);
                 ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                 int nextValue = is.read();
                 while (-1 != nextValue) {
                     byteStream.write(nextValue);
                     nextValue = is.read();
                 }
 
                 classByte = byteStream.toByteArray();
                 result = defineClass(className, classByte, 0, classByte.length, null);
                 classes.put(className, result);
                 return result;
             } catch (Exception e) {
                 return null;
             }
         }
     }
 
     class DacapoExecuter implements Runnable {
 
         @Override
         public void run() {
             try {
                 Class<?> cl = loader.loadClass("Harness");
                 Method method = cl.getMethod("main", new Class[]{String[].class});
 
                method.invoke(null,new Object[]{new String[]{"-noDigestOutput",
                        test}});
 
             } catch (ClassNotFoundException e) {
                 e.printStackTrace();
             } catch (IllegalAccessException e) {
                 e.printStackTrace();
             } catch (NoSuchMethodException e) {
                 e.printStackTrace();
             } catch (InvocationTargetException e) {
                 e.printStackTrace();
                System.err.println("Not a big deal if the test has finished");
             }
         }
     }
 
 
     @Start
     public void start() {
         path = getDictionary().get("dacapo_path").toString();
         test = getDictionary().get("dacapo_test").toString();
         try {
             loader = new URLClassLoader(new URL[]{new File(path).toURI().toURL()});
         } catch (MalformedURLException e) {
             e.printStackTrace();
         }
         Thread th = new Thread(new DacapoExecuter());
         th.setContextClassLoader(loader);
         th.start();
     }
 
     @Stop
     public void stop() {
 
     }
 
     @Update
     public void update() {
         stop();
         start();
     }
 }
