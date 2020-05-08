 /**
  * File: PersistenceFactory.java
  * Date: 26.04.2012
  */
 package org.smartsnip.persistence;
 
 import java.io.IOException;
 
 import org.apache.log4j.Logger;
 
 /**
  * This factory class is a singleton. It holds an instance of a persistence
  * class. Only one kind of persistence object selected of multiple
  * implementations can be instantiated at a time.
  * 
  * @author littlelion
  * 
  */
 public class PersistenceFactory {
 
 	/**
 	 * Constant to use with the {@code getInstance(int)} method. Defines which
 	 * Persistence class used:
 	 * <nl>
 	 * <li>none
 	 * </nl>
 	 * <p>
 	 * The persistence class is still uninitialized.
 	 */
 	public static final int PERSIST_UNINITIALIZED = 0;
 
 	/**
 	 * Constant to use with the {@code getInstance(int)} method. Defines which
 	 * Persistence class used:
 	 * <nl>
 	 * <li>BlackholePersistenceImpl
 	 * </nl>
 	 * <p>
 	 * This class provides getters for predefined outputs and setters which
 	 * don't persist any data.
 	 */
 	public static final int PERSIST_BLACKHOLE = 1;
 
 	/**
 	 * Constant to use with the {@code getInstance(int)} method. Defines which
 	 * Persistence class used:
 	 * <nl>
 	 * <li>MemPersistence
 	 * </nl>
 	 * <p>
 	 * This class provides access to a simple in-memory database. The data are
 	 * stored in a non volatile mode.
 	 */
 	public static final int PERSIST_MEMORY_VOLATILE = 2;
 
 	/**
 	 * Constant to use with the {@code getInstance(int)} method. Defines which
 	 * Persistence class used:
 	 * <nl>
 	 * <li>SqlPersistence
 	 * </nl>
 	 * <p>
 	 * This class provides access to a SQL database using the hibernate
 	 * framework.
 	 */
 	public static final int PERSIST_SQL_DB = 3;
 
 	/**
 	 * the default value for the method getInstance();
 	 */
 	private static int defaultType = PERSIST_SQL_DB;
 
 	/**
 	 * Keeps the type of the instantiated persistence object. If another type as
 	 * this initialized type is requested an {@link IOException} is thrown.
 	 */
 	private static int persistenceType = PERSIST_UNINITIALIZED;
 
 	/**
 	 * Holds a reference to the instance of a pesistence class.
 	 */
 	private static IPersistence instance = null;
 
 	/**
 	 * Static only class: refuse creation of an object even on trial to override
 	 * private access by using java's reflection mechanism.
 	 * 
 	 * @throws IllegalAccessException
 	 */
 	private PersistenceFactory() throws IllegalAccessException {
 		throw new IllegalAccessException("This is a static only class.");
 	}
 
 	/**
 	 * This getter provides access to a singleton instance of one of the
 	 * {@link IPersistence} implementing classes.
 	 * 
 	 * @param type
 	 *            one of the constants PERSIST_XXX defining the type which is to
 	 *            build.
 	 * @return the instance of a persistence class.
 	 * @throws IllegalAccessException
 	 *             if the singleton has been initialized with another type as
 	 *             given by the parameter {@code type}. Also if no instance
 	 *             couldn't be created due to a trial of creating an instance of
 	 *             a non existing type, removing an initialized instance or a
 	 *             violation to an access policy.
 	 */
 	public synchronized static IPersistence getInstance(int type)
 			throws IllegalAccessException {
 		IPersistence result = null;
 		Logger log = Logger.getLogger(PersistenceFactory.class);
 		if (instance == null) {
 			switch (type) {
 			case PERSIST_BLACKHOLE:
 				result = new BlackholePersistence();
 				persistenceType = PERSIST_BLACKHOLE;
 				break;
 			case PERSIST_MEMORY_VOLATILE:
 				result = new MemPersistence();
 				persistenceType = PERSIST_MEMORY_VOLATILE;
 				break;
 			case PERSIST_SQL_DB:
 				result = new SqlPersistence();
 				persistenceType = PERSIST_SQL_DB;
 				break;
 			default:
 				throw new IllegalAccessException("Type of persistence unknown.");
 			}
 			instance = result;
 		} else if (type != persistenceType) {
 			throw new IllegalAccessException(
 					"Mismatch between requested and initialized persistence object.");
 		}
 		log.info("Session Factory " + result.getClass().getSimpleName()
 				+ " opened.");
 		return instance;
 	}
 
 	/**
 	 * On first time this getter method is accessed a new instance is created
 	 * with the default persistence type, see
 	 * {@link PersistenceFactory#defaultType}.
 	 * <p>
 	 * If the singleten instance has previously been initialized the existing
 	 * instance is returned.
 	 * 
 	 * @see PersistenceFactory#getInstance(int)
 	 * @return the instance
 	 * @throws IllegalAccessException
 	 *             on policy violations.
 	 */
 	public static IPersistence getInstance() throws IllegalAccessException {
 		if (instance == null) {
 			getInstance(defaultType);
 		}
 		return instance;
 	}
 
 	/**
 	 * close the persistence factory. In this Method some logging is done so it
 	 * is recommended to use it instead of the method
 	 * {@link IPersistence#close()}.
 	 */
 	public static void closeFactory() {
 		Logger log = Logger.getLogger(PersistenceFactory.class);
 		String name = null;
 		try {
 			if (instance != null) {
				name = instance.getClass().getSimpleName();
 				instance.close();
 				log.info("Session Factory " + name + " closed.");
 			}
 		} catch (IOException e) {
 			log.warn("Session Factory " + name + " not closed regulary.", e);
 		} finally {
 			instance = null;
			persistenceType = PERSIST_UNINITIALIZED;
 		}
 	}
 
 	/**
 	 * @return the type of the instantiated persistence object.
 	 */
 	public static int getPersistenceType() {
 		return persistenceType;
 	}
 
 	/**
 	 * @return the defaultType
 	 * @see PersistenceFactory#setDefaultType(int)
 	 */
 	public static int getDefaultType() {
 		return defaultType;
 	}
 
 	/**
 	 * @param defaultType
 	 *            the defaultType which is used to create an instance with the
 	 *            {@link PersistenceFactory#getInstance()} method if it has been
 	 *            in uninitialized state.
 	 */
 	public static void setDefaultType(int defaultType) {
 		PersistenceFactory.defaultType = defaultType;
 	}
 }
