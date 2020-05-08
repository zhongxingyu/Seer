 package com.wishop.utils;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Locale;
 
 import com.wishop.model.BaseObject;
 import com.wishop.model.IAuditable;
 import com.wishop.model.User;
 
 /**
  * Wishop class responsible for printing all the messages
  * @author pmonteiro
  *
  * @param <T>	Base Object
  * @param <ID>	Object Id
  */
 public class WishopMessages<T extends IAuditable<ID>, ID extends Serializable> {
 
 	private static WishopMessages<? extends IAuditable<? extends Serializable>, ? extends Serializable> wishopMessages;
 	
 	
 	private WishopMessages() {}
 	
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	public static WishopMessages<? extends IAuditable<? extends Serializable>, ? extends Serializable> getInstance() {
 		if (wishopMessages == null) {
 			wishopMessages = new WishopMessages();
 		}
 		return wishopMessages;
 	}
 	
 	/**
 	 * Returns the message based on the default Locale
 	 * @param code - code referencing the code inside the properties file
 	 * @param entity - entity that is being audited
 	 * @return message - string message
 	 */
 	public static String getMessage(String code) {
 		return getMessage(code, new Object(), Locale.getDefault());
 	}
 	
 	/**
 	 * Returns the message based on the default Locale and the entity.
 	 * The entity parameter will be used to replace the {0}, {1}, .. {n} in the properties file
 	 * @param code - code referencing the code inside the properties file
 	 * @param entity - entity that is being audited
 	 * @return message - string message
 	 */
 	public static <T> String getMessage(String code, T entity) {
 		return getMessage(code, entity, Locale.getDefault());
 	}
 	
 	/**
 	 * Gets the message depending on the selected Locale
 	 * @param code - code referencing the code inside the properties file
 	 * @param entity - entity that is being audited
 	 * @param locale - local selected
 	 * @return message - string message
 	 */
 	@SuppressWarnings("unchecked")
 	public static <T> String getMessage(String code, T entity, Locale locale) {
 		ArrayList<Object> argList = new ArrayList<Object>();
 		if(entity instanceof BaseObject) {
 			BaseObject<Object, ? extends Serializable> baseObject = (BaseObject<Object, ? extends Serializable>) entity;
 			argList.add(baseObject.getClass().getName());
 			argList.add(baseObject.getId());
		} else if (entity instanceof String) {
			argList.add((String) entity);
		} else if (entity instanceof Number) {
			argList.add((Number) entity);
 		}
 		return WishopApplicationContext.getApplicationContext().getMessage(code, argList.toArray(), locale);
 	}
 	
 	/**
 	 * Gets the message depending on the default Locale
 	 * @param code
 	 * @param persistentClass
 	 * @param id
 	 * @return String 
 	 */
 	public static <T, ID> String getMessage(String code, Class<T> persistentClass, ID id) {
 		return getMessage(code, persistentClass, id, Locale.getDefault());
 	}
 	
 	/**
 	 * Gets the message depending on the selected Locale
 	 * @param code
 	 * @param persistentClass
 	 * @param id
 	 * @return String 
 	 */
 	public static <T, ID> String getMessage(String code, Class<T> persistentClass, ID id, Locale locale) {
 		ArrayList<Object> argList = new ArrayList<Object>();
 		argList.add(persistentClass.getName());
 		argList.add(id);
 		return WishopApplicationContext.getApplicationContext().getMessage(code, argList.toArray(), locale);
 	}
 	
 	/**
 	 * Gets the message depending on the default Locale
 	 * @param code
 	 * @param persistentClass
 	 * @param id
 	 * @return String 
 	 */
 	public static <T> String getMessage(String code, Class<T> persistentClass, String id) {
 		return getMessage(code, persistentClass, id, Locale.getDefault());
 	}
 	
 	/**
 	 * Gets the message depending on the selected Locale
 	 * @param code
 	 * @param persistentClass
 	 * @param id
 	 * @return String
 	 */
 	private static <T> String getMessage(String code, Class<T> persistentClass, String id, Locale locale) {
 		ArrayList<Object> argList = new ArrayList<Object>();
 		argList.add(persistentClass.getName());
 		argList.add(id);
 		return WishopApplicationContext.getApplicationContext().getMessage(code, argList.toArray(), locale);
 	}
 	
 	/**
 	 * Gets the message depending on the default Locale
 	 * @param code
 	 * @param persistentClass
 	 * @param id
 	 * @return String
 	 */
 	public static <T> String getMessage(String code, Class<T> persistentClass, Integer id) {
 		return getMessage(code, persistentClass, id, Locale.getDefault());
 	}
 	
 	/**
 	 * Gets the message depending on the selected Locale
 	 * @param code
 	 * @param persistentClass
 	 * @param id
 	 * @return String
 	 */
 	public static <T> String getMessage(String code, Class<T> persistentClass, Integer id, Locale locale) {
 		ArrayList<Object> argList = new ArrayList<Object>();
 		argList.add(persistentClass.getName());
 		argList.add(id);
 		return WishopApplicationContext.getApplicationContext().getMessage(code, argList.toArray(), locale);
 	}
 	
 	/**
 	 * Gets the message depending on the selected Locale
 	 * @param <T> BaseObject
 	 * @param code Property Message code
 	 * @param entity instance of BaseObject
 	 * @param user User
 	 * @return String 
 	 */
 	public static <T> String getMessage(String code, T entity, User user) {
 		Locale locale = Locale.getDefault();
 		return getMessage(code, entity, user, locale);
 	}
 	
 	/**
 	 * Gets the message depending on the selected Locale
 	 * @param <T> BaseObject
 	 * @param code Property Message code
 	 * @param entity instance of BaseObject
 	 * @param user User
 	 * @param locale Locale
 	 * @return String 
 	 */
 	@SuppressWarnings("unchecked")
 	public static <T> String getMessage(String code, T entity, User user, Locale locale) {
 		ArrayList<Object> argList = new ArrayList<Object>();
 		if(entity instanceof BaseObject) {
 			BaseObject<Object, ? extends Serializable> baseObject = (BaseObject<Object, ? extends Serializable>) entity;
 			argList.add(baseObject.getClass().getName());
 			argList.add(baseObject.getId());
 			if(user !=  null) {
 				argList.add(user.getId());
 			} else {
 				argList.add(-1);
 			}
 		}
 		return WishopApplicationContext.getApplicationContext().getMessage(code, argList.toArray(), locale);
 	}
 
 }
