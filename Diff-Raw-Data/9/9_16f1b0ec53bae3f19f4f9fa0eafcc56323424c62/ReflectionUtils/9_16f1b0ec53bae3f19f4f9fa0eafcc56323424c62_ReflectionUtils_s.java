 package com.github.francofabio.jplaintext.utils;
 
 import java.beans.PropertyDescriptor;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.beanutils.PropertyUtils;
 import org.apache.commons.lang3.StringUtils;
 
 import com.github.francofabio.jplaintext.JPlainTextException;
 
 @SuppressWarnings("rawtypes")
 public final class ReflectionUtils {
 
 	public static List<Field> fields(Class cls) {
 		final List<Field> fields = new ArrayList<Field>();
 		final PropertyDescriptor[] propertyDescriptors = PropertyUtils.getPropertyDescriptors(cls);
 
 		for (PropertyDescriptor descriptor : propertyDescriptors) {
 			Class declaringClass = descriptor.getReadMethod().getDeclaringClass();
 			String fieldName = descriptor.getName();
			if (!fieldName.equals("class")) {
 				try {
 					fields.add(declaringClass.getDeclaredField(fieldName));
 				} catch (Exception e) {
 					throw new JPlainTextException("Error getting fields", e);
 				}
 			}
 		}
 
 		return fields;
 	}
 
 	public static List<Field> fieldsWithAnnotation(Class<?> cls, Class<? extends Annotation> annotation) {
 		final List<Field> fields = new ArrayList<Field>();
 		final List<Field> fieldsOfClass = fields(cls);
 		for (Field field : fieldsOfClass) {
 			if (field.isAnnotationPresent(annotation)) {
 				fields.add(field);
 			}
 		}
 
 		return fields;
 	}
 
 	private static Field getRecursiveField(Class<?> cls, String fieldName) throws SecurityException {
 		Field field = null;
 		try {
 			field = cls.getDeclaredField(fieldName);
 		} catch (NoSuchFieldException e) {
 		}
 		if (field == null) {
 			Class<?> parent = cls.getSuperclass();
 			if (parent != null && !parent.equals(Object.class)) {
 				return getRecursiveField(parent, fieldName);
 			}
 		}
 		return field;
 	}
 
 	public static Field getField(Class<?> cls, String fieldName) {
 		String[] nestedField = fieldName.split("\\.");
 		Class<?> clazz = cls;
 		Field lastField = null;
 		for (String field : nestedField) {
 			lastField = getRecursiveField(clazz, field);
 			if (lastField == null) {
 				break;
 			} else {
 				clazz = lastField.getType();
 			}
 		}
 		return lastField;
 	}
 
 	public static void setFieldValue(Object bean, String fieldName, Object value) {
 		String[] nestedField = fieldName.split("\\.");
 		String targetField = nestedField[nestedField.length-1];
 		Object lastObject = bean;
 		Object parent = bean;
 		Field lastField = null;
 		try {
 			for (String field : nestedField) {
 				lastField = getRecursiveField(lastObject.getClass(), field);
 				if (lastField == null) {
 					throw new RuntimeException("Field '" + field + "' not found");
 				} else {
 					lastField.setAccessible(true);
 					Object fieldValue = lastField.get(lastObject);
 					if (fieldValue == null && !field.equals(targetField)) {
 						fieldValue = lastField.getType().newInstance();
 						lastField.set(lastObject, fieldValue);
 					}
 					parent = lastObject;
 					lastObject = fieldValue;
 				}
 			}
 			lastField.set(parent, value);
 		} catch (Exception e) {
 			throw new JPlainTextException("Error setting value on field " + fieldName, e);
 		}
 	}
 	
 	public static Object getPropertyValue(Object bean, String fieldName) {
 		String[] nestedField = fieldName.split("\\.");
 		try {
 			Object value = PropertyUtils.getProperty(bean, nestedField[0]);
 			if (nestedField.length > 1 && value != null) {
 				return getPropertyValue(value, StringUtils.join(nestedField, ".", 1, nestedField.length));
 			} else {
 				return value;
 			}
 		} catch (Exception e) {
 			throw new JPlainTextException("Error getting value to field " + fieldName, e);
 		}
 	}
 
 	public static boolean isProperty(Class<?> cls, String property) {
 		Field field = getField(cls, property);
 		return field != null;
 	}
 	
 	public static Class<?> getPropertyType(Class<?> cls, String property) {
 		Field field = getField(cls, property);
 		if (field != null) {
 			return field.getType();
 		}
 		return null;
 	}
 	
 }
