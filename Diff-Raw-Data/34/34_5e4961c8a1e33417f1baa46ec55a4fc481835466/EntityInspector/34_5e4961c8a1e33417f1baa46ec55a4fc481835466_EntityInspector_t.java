 
 package org.orman.mapper;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
import org.orman.mapper.annotation.Column;
 import org.orman.mapper.annotation.Id;
 import org.orman.mapper.annotation.Index;
 import org.orman.mapper.annotation.ManyToOne;
 import org.orman.mapper.annotation.NotNull;
 import org.orman.mapper.annotation.OneToMany;
 import org.orman.mapper.annotation.OneToOne;
 import org.orman.mapper.exception.NotDeclaredDefaultConstructorException;
 import org.orman.mapper.exception.NotDeclaredGetterException;
 import org.orman.mapper.exception.NotDeclaredSetterException;
 import org.orman.mapper.exception.UnannotatedCollectionFieldException;
 import org.orman.mapper.exception.UnsupportedIdFieldTypeException;
 
 /**
  * Finds fields and getter-setter methods of a given {@link Entity} using Reflection API.
  * 
  * @author alp
  *
  */
 public class EntityInspector {
 	
 	private static final Class<?>[] ID_SUPPORTED_TYPES = { Integer.class,
 			Integer.TYPE, Long.class, Long.TYPE}; // REMOVED String because of SQLite.
 	private Class<?> clazz;
 	private List<Field> fields;
 	private Constructor<?> defaultConstructor;
 	
 	public EntityInspector(Class<?> forClass){
 		this.clazz = forClass;
 		fields = new ArrayList<Field>();
 	}
 
 	/**
 	 * 
 	 * Extracts declared non-<code>transient</code> fields of a given Entity. Makes 
 	 * property bindings for fields using their annotations and
 	 * checks for their getter-and setters (finds them automatically)
 	 * if they are non-public fields.
 	 * 
 	 * @return fields of this class.
 	 */
 	@SuppressWarnings("static-access")
 	private List<Field> extractFields(){
 		this.fields.clear();
 		
 		for(java.lang.reflect.Field f : this.clazz.getDeclaredFields()){
 			// Only non-`transient` (threatened as persistent) fields
 			if(!Modifier.isTransient(f.getModifiers())){
				
 				Class<?> fieldType = f.getType();
 
 				boolean isList = false;
 				// if the field is collection of something, store its generic. 
 				if (fieldType.equals(List.class)
 						|| fieldType.equals(ArrayList.class)
 						|| fieldType.equals(LinkedList.class)){
 					/*
 					 * we have a 1:* or *:* mapping
 					 */
 					if (f.isAnnotationPresent(OneToMany.class)){
 						fieldType = f.getAnnotation(OneToMany.class).toType();
 					} else {
 						// TODO add ManyToMany.
 						throw new UnannotatedCollectionFieldException(f.getName(), this.clazz.getName());
 					}
 					isList = true;
 				} 
 				
				Field newF = new Field(fieldType, f); 
 				if (isList) newF.setList(true);
 					
 				// Find getters and setters for non-public field.
 				int accessibility = f.getModifiers();
 				if (!(Modifier.isPublic(accessibility))){
 					// if setter does not exist, throw exception.
 					Method setter = this.findSetterFor(this.clazz, f.getName());
 					if(setter == null)
 						throw new NotDeclaredSetterException(f.getName(), this.clazz.getName());
 					else 
 						newF.setSetterMethod(setter); // bind setter.
 					
 					// if getter does not exist, throw exception.
 					Method getter = this.findGetterFor(this.clazz, f.getName());
 					if(getter == null)
 						throw new NotDeclaredGetterException(f.getName(), this.clazz.getName());
 					else 
 						newF.setGetterMethod(getter); // bind getter.
 				}
 				
 				// Recognize @Index annotation.
 				if(f.isAnnotationPresent(Index.class)){
 					Index ann = f.getAnnotation(Index.class);
 					newF.setIndex(new FieldIndexHolder(ann.name(), ann.unique()));
 					newF.setNullable(false);
 				}
 				
 				// Recognize @NotNull annotation.
 				if(f.isAnnotationPresent(NotNull.class)){
 					newF.setNullable(false);
 				}
 				
 				// Recognize @Id annotation (covers @Index)
 				if(f.isAnnotationPresent(Id.class)){
 					newF.makeId(true);
 					
 					if (!isSupportedForIdField(f.getType())){
 						throw new UnsupportedIdFieldTypeException(f.getType().getName(), clazz.getName());
 					}
 					
 					// if no custom @Index defined create a default
 					if(newF.getIndex() == null)
 						newF.setIndex(new FieldIndexHolder(null, true));
 				}
 				
 				// Recognize @OnyToOne, @OneToMany, @ManyToMany annotations (covers @Index) 
 				if(f.isAnnotationPresent(OneToOne.class)){ // TODO add other cardinality annotations, too
 					// if no custom @Index defined create a default.
 					if(newF.getIndex() == null)
 						newF.setIndex(new FieldIndexHolder(null, true));
 				}
 				
 				// ManyToOne
 				if(f.isAnnotationPresent(ManyToOne.class)){ // TODO add other cardinality annotations, too
 					// if no custom @Index defined create a default.
 					if(newF.getIndex() == null)
 						newF.setIndex(new FieldIndexHolder(null, false));
 				}
 				
 				// if one to many, or many to many, make setList(true).
 				
 				// Save raw field data for future usage
 				newF.setRawField(f);
 				
 				fields.add(newF);
 			}
 		}
 		return this.fields;
 	}
 
 	/**
 	 * Checks whether this class (of some field) can be a 
 	 * {@link Id} in terms of its variable type.
 	 * 
 	 * @param type of some field to be an {@link Id} candidate.
 	 * @return true if eligible, false otherwise.
 	 */
 	private static boolean isSupportedForIdField(Class<?> type){
 		for(int i = 0 ; i < ID_SUPPORTED_TYPES.length; i++)
 			if(type.equals(ID_SUPPORTED_TYPES[i])) return true;
 		return false;
 	}
 	
 	/**
 	 * Tries to find a setter method for given field name within
 	 * specified {@link Class}. First match (according to the 
 	 * Java naming standards) will be returned, null if not
 	 * found.
 	 * 
 	 * Warning: If setter method does not have exactly 1 argument
 	 * in parameter list, it will not be matched.
 	 */
 	private static Method findSetterFor(Class<?> forClass, final String fieldName) {
 		if (fieldName == null || "".equals(fieldName))
 			throw new IllegalArgumentException("Field name cannot be empty for finding setter method.");
 		
 		@SuppressWarnings("serial")
 		List<String> methodNameCandidates = new ArrayList<String>(){{
 			//"set"+FieldName
 			add("set"+Character.toUpperCase(fieldName.charAt(0))+(fieldName.length()>1?fieldName.substring(1):""));
 			// "set"+fieldName
 			add("set"+fieldName);
 			//"is"+FieldName
 			add("is"+Character.toUpperCase(fieldName.charAt(0))+(fieldName.length()>1?fieldName.substring(1):""));
 			// "is"+fieldName
 			add("is"+fieldName);
 			// fieldName
 			add(fieldName);
 		}};
 		
 		Method m = findMethodLike(forClass, methodNameCandidates);
 		
 		if (m != null && m.getParameterTypes().length == 1) // only 1 argument
 			return m;
 		else
 			return null;
 	}
 	
 	/**
 	 * Tries to find a getter method for given field name within
 	 * specified {@link Class}. First match (according to the 
 	 * Java naming standards) will be returned, null if not
 	 * found.
 	 * 
 	 * Warning: If getter method has arguments in its parameter
 	 * list, it will not be matched.
 	 */
 	private static Method findGetterFor(Class<?> forClass, final String fieldName) {
 		if (fieldName == null || "".equals(fieldName))
 			throw new IllegalArgumentException("Field name cannot be empty for finding getter method.");
 		
 		@SuppressWarnings("serial")
 		List<String> methodNameCandidates = new ArrayList<String>(){{
 			//"get"+FieldName
 			add("get"+Character.toUpperCase(fieldName.charAt(0))+(fieldName.length()>1?fieldName.substring(1):""));
 			// "get"+fieldName
 			add("get"+fieldName);
 			// fieldName
 			add(fieldName);
 		}};
 		
 		Method m = findMethodLike(forClass, methodNameCandidates);
 		
 		if (m != null && m.getParameterTypes().length == 0
 				&& !m.getReturnType().equals(Void.TYPE)) // non-void and no
 															// arguments
 			return m;
 		else return null;
 	}
 	
 	/**
 	 * Scans all the declared methods in a given class and
 	 * returns the first <code>public </code> one which is in
 	 * <code>candidateNames</code>.   
 	 */
 	private static Method findMethodLike(Class<?> forClass, List<String> candidateNames){
 		for(Method m : forClass.getDeclaredMethods()){ // methods declared only in this class
 			if (Modifier.isPublic(m.getModifiers())){ // only if method is public.
 				if (candidateNames.indexOf(m.getName()) > -1){ // method exists?
 					return m;
 				}
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Returns fields of this entity. Note that fields are
 	 * extracted once if this method is used, they are saved
 	 * for future requests. 
 	 */
 	public List<Field> getFields(){
 		if(this.fields.isEmpty()) return extractFields();
 		else return this.fields;
 	}
 	
 	/**
 	 * Returns default constructor of this entity. If there are no
 	 * default constructors are declared, throws exception. 
 	 * 
 	 * @throws NotDeclaredDefaultConstructorException if no defaults
 	 * @return
 	 */
 	public Constructor<?> getDefaultConstructor(){
 		if(this.defaultConstructor == null) return extractDefaultConstructor();
 		return this.defaultConstructor;
 	}
 
 	private Constructor<?> extractDefaultConstructor() {
 		Constructor<?>[] cs = this.clazz.getDeclaredConstructors();
 		
 		if (cs == null) // if no constructors are defined.
 			throw new NotDeclaredDefaultConstructorException(this.clazz.getName());
 		
 		for(Constructor<?> c : cs){
 			if (Modifier.isPublic(c.getModifiers())){
 				Class<?>[] params = c.getParameterTypes();
 				if (params == null || params.length == 0)
 					return c;
 			}
 		}
 		
 		// if not found 
 		throw new NotDeclaredDefaultConstructorException(this.clazz.getName());
 	}
 }
