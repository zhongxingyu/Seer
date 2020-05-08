 package com.mymed.controller.core.manager;
 
 import java.io.UnsupportedEncodingException;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import com.mymed.controller.core.exception.InternalBackEndException;
 import com.mymed.controller.core.manager.storage.IStorageManager;
 import com.mymed.model.data.AbstractMBean;
 import com.mymed.utils.ClassType;
 import com.mymed.utils.MyMedLogger;
 
 /**
  * Abstract manager the all the managers should extend.
  * <p>
  * This manager provides the basic operation to recreate a bean object.
  * 
  * @author lvanni
  * @author Milo Casagrande
  * 
  */
 public abstract class AbstractManager extends ManagerValues {
 
 	private static final int PRIV_FIN = Modifier.PRIVATE + Modifier.FINAL;
 	private static final int PRIV_STAT_FIN = Modifier.PRIVATE + Modifier.STATIC + Modifier.FINAL;
 
 	private static final String ENCODING = "UTF8";
 
 	protected IStorageManager storageManager;
 
 	public AbstractManager(final IStorageManager storageManager) {
 		this.storageManager = storageManager;
 	}
 
 	/**
 	 * Introspection
 	 * 
 	 * @param mbean
 	 * @param args
 	 * @return
 	 * @throws InternalBackEndException
 	 */
 	public AbstractMBean introspection(final AbstractMBean mbean, final Map<byte[], byte[]> args)
 	        throws InternalBackEndException {
 		for (final Entry<byte[], byte[]> arg : args.entrySet()) {
 			try {
 				final Field field = mbean.getClass().getDeclaredField(new String(arg.getKey(), ENCODING));
 
 				/*
 				 * We check the value of the modifiers of the field: if the
 				 * field is private and final, or private static and final, we
 				 * skip it.
 				 */
 				final int modifiers = field.getModifiers();
 				if (modifiers == PRIV_FIN || modifiers == PRIV_STAT_FIN) {
 					continue;
 				}
 
 				final ClassType classType = ClassType.inferType(field.getGenericType());
 				final String setterName = createSetterName(field, classType);
 				final Method method = mbean.getClass().getMethod(setterName, classType.getPrimitiveType());
 				final Object argument = ClassType.objectFromClassType(classType, arg.getValue());
 
 				method.invoke(mbean, argument);
 			} catch (final NoSuchFieldException e) {
 				try {
 					MyMedLogger.getLog().info("WARNING: {} is not a bean field", new String(arg.getKey(), ENCODING));
 				} catch (final UnsupportedEncodingException ex) {
 					// If we ever get here, there is something seriously wrong.
 					// This should never happen.
 					MyMedLogger.getLog().info("Error in encoding string using {} encoding", ENCODING);
 					MyMedLogger.getDebugLog().debug("Error in eoncoding string", ex.getCause());
 				}
 			} catch (final SecurityException ex) {
 				throw new InternalBackEndException(ex);
 			} catch (final NoSuchMethodException ex) {
 				throw new InternalBackEndException(ex);
 			} catch (final IllegalArgumentException ex) {
 				throw new InternalBackEndException(ex);
 			} catch (final IllegalAccessException ex) {
 				throw new InternalBackEndException(ex);
 			} catch (final InvocationTargetException ex) {
 				throw new InternalBackEndException(ex);
 			} catch (final UnsupportedEncodingException ex) {
 				// If we ever get here, there is something seriously wrong.
 				// This should never happen.
 				MyMedLogger.getLog().info("Error in encoding string using {} encoding", ENCODING);
 				MyMedLogger.getDebugLog().debug("Error in encoding string", ex.getCause());
 			}
 		}
 
 		return mbean;
 	}
 
 	/**
 	 * Create the name of the setter method based on the field name and its
 	 * class.
 	 * <p>
 	 * This is particularly useful due to the fact that boolean fields does not
 	 * have a normal setter name.
 	 * 
 	 * @param field
 	 *            the filed we want the setter method of
 	 * @param classType
 	 *            the class type of the field
 	 * @return the name of the setter method
 	 */
 	private String createSetterName(final Field field, final ClassType classType) {
 		final StringBuilder setterName = new StringBuilder(20);
 		final String fieldName = field.getName();
 
 		setterName.append("set");
 		String subName = fieldName;
 
 		MyMedLogger.getDebugLog().debug("Creating setter name with filed name: {}", fieldName);
 
 		/*
 		 * Check that the boolean field we are on does start with 'is'. This
 		 * should be the default prefix for boolean fields. In this case the
 		 * setter method will be based on the field name, but without the 'is'
 		 * prefix.
 		 */
 		if (classType.equals(ClassType.BOOL) && fieldName.startsWith("is")) {
 			subName = fieldName.substring(2, fieldName.length());
 			MyMedLogger.getDebugLog().debug("Field is a boolean field with sub-name: {}", subName);
 		}
 
		setterName.append(subName.substring(0, 1).toUpperCase());
 		setterName.append(subName.substring(1));
 
 		setterName.trimToSize();
 
 		MyMedLogger.getDebugLog().debug("Setter name created as: {}", setterName.toString());
 
 		return setterName.toString();
 	}
 }
