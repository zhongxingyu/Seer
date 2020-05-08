 package jib.client;
 
 import jvm.util.DataInputStream;
 
 import gwtjava.io.File;
 import gwtjava.io.fs.FileSystem;
 import gwtjava.lang.System;
 import jvm.classparser.JClass;
 import jvm.execution.JClassLoader;
 
 public class JibClassLoader extends JClassLoader {
     private static FileSystem fs = FileSystem.instance();
 
     @Override
     public JClass loadClass(String name) {
         for (String path : System.getProperty("java.class.path").split(File.pathSeparator)) {
            String fileName = path + name;
             if (fs.exists(fileName)) {
                 byte [] content = fs.readFile(fileName);
                 JClass jc = new JClass(new DataInputStream(content));
                 return jc;
             }
         }
         throw new AssertionError();
     }
 }
