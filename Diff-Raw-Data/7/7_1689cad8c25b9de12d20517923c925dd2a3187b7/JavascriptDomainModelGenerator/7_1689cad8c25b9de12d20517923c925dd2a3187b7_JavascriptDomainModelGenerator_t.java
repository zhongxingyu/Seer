 package com.goda5.jsmodelgenerator;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.lang.reflect.Field;
 import java.lang.reflect.Modifier;
 import java.util.Collection;
 
 public class JavascriptDomainModelGenerator {
 	public void generate(Class<?> clazz, File targetJsFolder) throws Exception {
 		StringBuilder b = new StringBuilder();
 		String prefix = "";
 		String suffix = ";";
 		if (clazz.isAnnotationPresent(JavascriptDomainModel.class)) {
 			b.append("function " + clazz.getSimpleName() + "(");
 			for(Field field:clazz.getDeclaredFields()) {
 				if(!Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers())) {
 					b.append(prefix);
					prefix = ", ";
 					b.append(field.getName());
 				}
 			}
 			b.append(") {" + System.getProperty("line.separator"));
 			for(Field field:clazz.getDeclaredFields()) {
 				if(!Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers())) {
					if(Collection.class.isAssignableFrom(field.getType())) {
						b.append("    //Array" + System.getProperty("line.separator"));
 					}
 					b.append("    this." + field.getName() + " = " + field.getName() + suffix + System.getProperty("line.separator"));
 				}
 			}
 			b.append("}");
 			if(!targetJsFolder.exists()) {
 				targetJsFolder.mkdirs();
 			}
 			FileWriter fw = new FileWriter(targetJsFolder + "/" + clazz.getSimpleName() + ".js");
 			fw.write(b.toString());
 			fw.close();
 		}
 	}
 }
