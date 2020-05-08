 package com.objectpartners.buesing.premock;
 
 import java.io.IOException;
 import java.lang.reflect.Modifier;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 
 import javassist.CannotCompileException;
 import javassist.ClassPool;
 import javassist.CtClass;
 import javassist.CtMethod;
 import javassist.NotFoundException;
 
 public class PreMockClassLoader extends ClassLoader {
 
  private static final int NOT_FINAL_MASK = 0xFFFF ^ Modifier.FINAL;
 
   private static Set<String> classes = Collections.emptySet();
 
   private ClassPool pool;
 
   public static void setClasses(Set<String> set) {
     classes = new HashSet<String>(set);
   }
 
   public PreMockClassLoader(ClassLoader parent) {
     super(parent);
     pool = ClassPool.getDefault();
   }
 
   @Override
   public Class<?> loadClass(String name) throws ClassNotFoundException {
 
     // no mater what, do not allow certain classes to be loaded by this
     // class loader. change this as you see fit (and are able to).
     if (name.startsWith("java.")) {
       return super.loadClass(name);
     } else if (name.startsWith("javax.")) {
       return super.loadClass(name);
     } else if (name.startsWith("sun.")) {
       return super.loadClass(name);
     } else if (name.startsWith("org.junit.")) {
       return super.loadClass(name);
     } else if (name.startsWith("org.mockito.")) {
       return super.loadClass(name);
     } else if (name.startsWith("com.objectpartners.buesing.premock.")) {
       return super.loadClass(name);
     } else {
       if (classes.contains(name)) {
         // only load the classes specified with the class loader,
         // otherwise leave it up to the parent.
         return findClass(name);
       } else {
         return super.loadClass(name);
       }
     }
   }
 
   public Class<?> findClass(String name) throws ClassNotFoundException {
 
     try {
       CtClass cc = pool.get(name);
 
       // remove final modifier from the class
       cc.setModifiers(cc.getModifiers() & NOT_FINAL_MASK);
 
       // remove final and native modifiers from all methods.
       CtMethod[] methods = cc.getDeclaredMethods();
       for (CtMethod method : methods) {
        method.setModifiers(method.getModifiers() & NOT_FINAL_MASK);
       }
 
       byte[] b = cc.toBytecode();
 
       Class<?> result = defineClass(name, b, 0, b.length);
 
       return result;
     } catch (NotFoundException e) {
       throw new ClassNotFoundException();
     } catch (IOException e) {
       throw new ClassNotFoundException();
     } catch (CannotCompileException e) {
       throw new ClassNotFoundException();
     }
   }
 
 }
