 package com.mymed.model.data;
 
 import java.io.UnsupportedEncodingException;
 import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
 import java.util.HashMap;
 import java.util.Map;
 
 import com.mymed.controller.core.exception.InternalBackEndException;
 import com.mymed.utils.ClassType;
 
 /**
  * myMed java Beans:
  * 
  * The required conventions are as follows:
  * 
  * -The class must have a public default constructor (no-argument). This allows
  * easy instantiation within editing and activation frameworks.
  * 
  * - The class properties must be accessible using get, set, is (used for
  * boolean properties instead of get) and other methods (so-called accessor
  * methods and mutator methods), following a standard naming-convention. This
  * allows easy automated inspection and updating of bean state within
  * frameworks, many of which include custom editors for various types of
  * properties.
  * 
  * -The class should be serializable. It allows applications and frameworks to
  * reliably save, store, and restore the bean's state in a fashion independent
  * of the VM and of the platform.
  * 
  * - The class must have a getAttributeToMap method, that convert all the fields
  * in a hashMap format for the myMed wrapper
  * 
  * - The class must override toString to have an human readable format
  * 
  * @author lvanni
  */
 public abstract class AbstractMBean {
 
 	private static final int PRIV_FIN = Modifier.PRIVATE + Modifier.FINAL;
 	private static final int PRIV_STAT_FIN = Modifier.PRIVATE + Modifier.STATIC + Modifier.FINAL;
 
 	/**
 	 * @return all the fields in a hashMap format for the myMed wrapper
 	 * @throws IllegalArgumentException
 	 * @throws IllegalAccessException
 	 * @throws UnsupportedEncodingException
 	 */
 	public Map<String, byte[]> getAttributeToMap() throws InternalBackEndException {
 		final Map<String, byte[]> args = new HashMap<String, byte[]>();
 		for (final Field field : this.getClass().getDeclaredFields()) {
 
 			// Set the field as accessible: it is not really secure in this case
 			// TODO maybe we should invoke the get methods and retrieve the
 			// values in that way
 			field.setAccessible(true);
 
 			/*
 			 * We check the value of the modifiers of the field: if the field is
 			 * private and final, or private static and final, we skip it.
 			 */
 			final int modifiers = field.getModifiers();
 			if (modifiers == PRIV_FIN || modifiers == PRIV_STAT_FIN) {
 				continue;
 			}
 
 			try {
 				final ClassType type = ClassType.inferTpye(field.getType());
 
 				args.put(field.getName(), ClassType.objectToByteArray(type, field.get(this)));
 			} catch (final Exception e) {
 				throw new InternalBackEndException("getAttribueToMap failed!: Introspection error");
 			}
 		}
 
 		return args;
 	}
 
 	/**
 	 * override toString to have an human readable format
 	 */
 	@Override
 	public String toString() {
 		String value = "";
 		for (final Field field : this.getClass().getDeclaredFields()) {
 
 			// TODO fix here, not really secure
 			field.setAccessible(true);
 
 			try {
 				if (field.get(this) instanceof String) {
 					value += "\t" + field.getName() + " : " + (String) field.get(this) + "\n";
 				} else {
 					value += "\t" + field.getName() + " : " + field.get(this) + "\n";
 				}
 			} catch (final IllegalArgumentException e) {
 				e.printStackTrace();
 			} catch (final IllegalAccessException e) {
 				e.printStackTrace();
 			}
 		}
 		return value;
 	}
 }
