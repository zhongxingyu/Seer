 package com.compilation;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 
 public class MyClassLoader extends ClassLoader {
 	
 	private boolean alwaysDefineClass = true;
 	@Override
 	protected Class<?> findClass(String fullClassName)
 			throws ClassNotFoundException {
 		Class<?> clazz = null;
 		// clazz = findLoadedClass(fullClassName);
 		// if(alwaysDefineClass || clazz == null){
 		byte[] raw = readClassBytes(fullClassName);

 		clazz = defineClass(fullClassName, raw, 0, raw.length);
 		resolveClass(clazz);
 		// }
 
 		return clazz;
 	}
 
 	private byte[] readClassBytes(String fullClassName) {
 		byte[] raw = null;
 		InputStream stream = null;
 		File file = new File(MyClassCompiler.classPath + File.separator
 				+ fullClassName.replaceAll("\\.", "/") + ".class");
 
 		try {
 			stream = new FileInputStream(file);
 			raw = new byte[(int) file.length()];
 			stream.read(raw);
 		} catch (Exception e) {
 			e.printStackTrace();
			System.out.println("===============MyClassLoader 1==================");
 		} finally {
 			try {
 				stream.close();
 			} catch (IOException e) {
				System.out.println("===============MyClassLoader 2==================");
 			}
			
 		}
 		return raw;
 	}
 
 	public boolean isAlwaysDefineClass() {
 		return alwaysDefineClass;
 	}
 
 	public void setAlwaysDefineClass(boolean alwaysDefineClass) {
 		this.alwaysDefineClass = alwaysDefineClass;
 	}
 }
