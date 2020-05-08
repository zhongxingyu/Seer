 package net.histos.java2as.core.meta;
 
 import java.beans.BeanInfo;
 import java.beans.IntrospectionException;
 import java.beans.Introspector;
 import java.beans.PropertyDescriptor;
 import java.lang.reflect.Field;
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.List;
 
 public class JavaTransferObject {
 
 	private Class<?> clazz;
 	private List<JavaProperty> properties;
 
 	/**
 	 * Constructs a Java transfer object based off a Java class.
 	 *
 	 * @param clazz Java class
 	 */
 	public JavaTransferObject(Class<?> clazz) {
 		this.clazz = clazz;
 		this.properties = new ArrayList<JavaProperty>();
 
 		// extract properties based off get/set methods
 		try {
 			BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
 			PropertyDescriptor[] props = beanInfo.getPropertyDescriptors();
 			for (PropertyDescriptor prop : props)
				if (!"class".equals(prop.getName()))
					properties.add(new JavaProperty(prop.getReadMethod()));
 		} catch (IntrospectionException e) {
 			throw new RuntimeException(e);
 		}
 
 		// extract public fields
 		for (Field field : clazz.getDeclaredFields()) {
 			int modifiers = field.getModifiers();
 			if (Modifier.isPublic(modifiers) && !Modifier.isStatic(modifiers) && !Modifier.isTransient(modifiers)) {
 				properties.add(new JavaProperty(field));
 			}
 		}
 	}
 
 	//
 	// Public Methods
 	//
 
 	@Override
 	public String toString() {
 		return "JavaTransferObject{" + clazz + '}';
 	}
 
 	//
 	// Getters and Setters
 	//
 
 	public String getPackageName() {
 		return clazz.getPackage().getName();
 	}
 
 	public String getName() {
 		return clazz.getName();
 	}
 
 	public String getSimpleName() {
 		return clazz.getSimpleName();
 	}
 
 	public boolean hasSuperclass() {
 		return !clazz.getSuperclass().equals(Object.class);
 	}
 
 	public Class<?> getSuperclass() {
 		return clazz.getSuperclass();
 	}
 
 	public boolean hasInterfaces() {
 		return clazz.getInterfaces().length > 0;
 	}
 
 	public Class<?>[] getInterfaces() {
 		return clazz.getInterfaces();
 	}
 
 	public List<JavaProperty> getProperties() {
 		return properties;
 	}
 
 }
