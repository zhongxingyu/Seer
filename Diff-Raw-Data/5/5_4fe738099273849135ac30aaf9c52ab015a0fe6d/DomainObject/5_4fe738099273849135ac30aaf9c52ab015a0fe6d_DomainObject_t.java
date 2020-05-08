 package ru.xrm.app.domain;
 
 import java.lang.reflect.Field;
 
 public abstract class DomainObject {
 
 	public String toString() {
 		StringBuilder sb = new StringBuilder();
 		sb.append(getClass().getName());
 		sb.append("{\n");
 		try {
 			for (Field f : getClass().getDeclaredFields()) {
 				sb.append("\t");
 				sb.append(f.getName());
 				sb.append(" : ");
 
 				sb.append(f.get(this));
 
 				sb.append("\n");
 			}
 		} catch (IllegalArgumentException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		sb.append("}\n");
 		return sb.toString();
 	}

 	public void setProperty(String property, Object value)
 			throws SecurityException, NoSuchFieldException,
 			IllegalArgumentException, IllegalAccessException {
 		@SuppressWarnings("rawtypes")
 		Class aClass = getClass();
 		Field field = aClass.getDeclaredField(property);
 		field.set(this, value);
 	}

 }
