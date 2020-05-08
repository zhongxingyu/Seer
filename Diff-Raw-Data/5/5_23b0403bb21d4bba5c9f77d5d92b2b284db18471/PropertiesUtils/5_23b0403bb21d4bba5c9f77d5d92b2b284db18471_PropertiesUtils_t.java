 /*******************************************************************************
  * Copyright (c) 2007, 2008 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.core.util;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Map;
 import java.util.StringTokenizer;
 
 import org.eclipse.core.runtime.Assert;
 
 /**
  * PropertiesUtils class
  * 
  */
 public final class PropertiesUtils {
 
 	private PropertiesUtils() {
 		// Utility
 	}
 
 	/**
 	 * Answers the String value for the given propValue object. If the propValue
 	 * not compatible to String or String[] answers the given
 	 * <code>returnIfNoString</code> as default. If propValue is an instance of
 	 * String[] then answers the item with index=0.
 	 * 
 	 * @param propValue
 	 * @param returnIfNoString
 	 * @return the String value for propValue or the default value
 	 *         <code>returnIfNoString</code>
 	 */
 	public static String accessProperty(Object propValue, String returnIfNoString) {
 		if (propValue instanceof String) { // if api programmed we receive
 			return (String) propValue; // a String
 		} else if (propValue instanceof String[]) { // for DS we receive a
 			return ((String[]) propValue)[0]; // String array
 		}
 
 		return returnIfNoString;
 	}
 
 	/**
 	 * Check the given data object and if it is a {@code Hashtable} as it may be
 	 * provided by the {@code IExecutableExtension.setInitializationData()}
 	 * method return the {@code Hashtable} with optional checking of the
 	 * expected keys.<br>
 	 * If the data object is a {@code String} than transform the given string
 	 * representation of a map into a map and optionally check the existence of
 	 * the expected keys.
 	 * 
 	 * <pre>
 	 * The format of the string is: 
 	 * string := [ pair ] | pair { , pair }
 	 * pair := key = value
 	 * </pre>
 	 * 
 	 * @param data
 	 *            this can be the data parameter of the
 	 *            IExecutableExtension.setInitializationData
 	 *            (IConfigurationElement config, String propertyName, Object
 	 *            data) method.
 	 * @param expectedKeys
 	 *            optional expected keys
 	 * @return a map
 	 * @throws IllegalArgumentException
 	 *             for any errors
 	 * @see IExecutableExtension.setInitializationData()
 	 */
 	public static Map<String, String> asMap(Object data, String... expectedKeys) {
 		return asMap(data, null, expectedKeys);
 	}
 
 	/**
 	 * Check the given data object and if it is a {@code Hashtable} as it may be
 	 * provided by the {@code IExecutableExtension.setInitializationData()}
 	 * method return the {@code Hashtable} with optional checking of the
 	 * expected keys.<br>
 	 * If the data object is a {@code String} than transform the given string
 	 * representation of a map into a map.<br>
 	 * The optional defaults parameter may be specified to define defaults for
 	 * expected keys. The optional expectedKeys parameter can be used to check
 	 * the existence of expected keys.
 	 * 
 	 * <pre>
 	 * The format of the string is: 
 	 * string := [ pair ] | pair { , pair }
 	 * pair := key = value
 	 * </pre>
 	 * 
 	 * @param data
 	 *            this can be the data parameter of the
 	 *            IExecutableExtension.setInitializationData
 	 *            (IConfigurationElement config, String propertyName, Object
 	 *            data) method.
 	 * @param defaults
 	 *            optional defaults
 	 * @param expectedKeys
 	 *            optional expected keys
 	 * @return a map
 	 * @throws IllegalArgumentException
 	 *             for any errors
 	 * @see IExecutableExtension.setInitializationData()
 	 */
 	@SuppressWarnings("unchecked")
 	public static Map<String, String> asMap(Object data, Map<String, String> defaults, String... expectedKeys) {
 		Map<String, String> result = null;
 		if (data == null) {
			result = new HashMap<String, String>();
 		} else if (data instanceof Hashtable) {
 			result = (Hashtable<String, String>) data;
 		} else if (data instanceof String) {
 			String stringified = (String) data;
 			result = new HashMap<String, String>();
 			if (StringUtils.isEmpty(stringified)) {
 				Assert.isLegal(expectedKeys.length == 0,
 						"Excpeted keys " + Arrays.toString(expectedKeys) + " not found in empty string."); //$NON-NLS-1$ //$NON-NLS-2$
				return new HashMap<String, String>();
 			}
 			StringTokenizer parts = new StringTokenizer(stringified, ","); //$NON-NLS-1$
 			while (parts.hasMoreTokens()) {
 				String part = parts.nextToken();
 				int equal = part.indexOf('=');
 				Assert.isLegal(equal > 0, "Error within definition. Expecting a string of the form: " //$NON-NLS-1$
 						+ " [ <key> \"=\" <value> ] { [ \",\" <key> \"=\" <value> ] }"); //$NON-NLS-1$
 				result.put(part.substring(0, equal).trim(), part.substring(equal + 1).trim());
 			}
 		} else {
 			Assert.isLegal(false, "Can not deal with data type: " + data.getClass().getName()); //$NON-NLS-1$
 		}
 		// if optional defaults add them if necessary
 		if (defaults != null) {
 			for (Map.Entry<String, String> entry : defaults.entrySet()) {
 				if (!result.containsKey(entry.getKey())) {
 					result.put(entry.getKey(), entry.getValue());
 				}
 			}
 		}
 		// validate optional expected keys
 		for (String expectedKey : expectedKeys) {
 			Assert.isLegal(result.containsKey(expectedKey), "data " + data + "does not contain expected key " //$NON-NLS-1$ //$NON-NLS-2$
 					+ expectedKey + "."); //$NON-NLS-1$
 		}
 		return Collections.unmodifiableMap(result);
 	}
 
 	private static final String[] EMPTY_STRING_ARRAY = new String[0];
 	private static final int CHAR = 0;
 	private static final int BACK_SLASH = 1;
 
 	/**
 	 * Transform the string representation of a list into a array.<br>
 	 * This method supports the escaped meta characters: '\,' which is a literal
 	 * ',' and '\\' which is a '\'.
 	 * 
 	 * <pre>
 	 * The format of the string is: 
 	 *  string = [ value ] | value { , value }
 	 * </pre>
 	 * 
 	 * @param data
 	 *            must be of type string; otherwise a IllegalArgumentException
 	 *            will be thrown
 	 * @return
 	 * @throws IllegalArgumentException
 	 *             in case of errors
 	 */
 	public static String[] asArray(Object data) {
 		if (data == null) {
 			return EMPTY_STRING_ARRAY;
 		}
 		Assert.isLegal(data instanceof String, "data must be of type String."); //$NON-NLS-1$
 		String stringified = (String) data;
 		if (StringUtils.isEmpty(stringified)) {
 			return EMPTY_STRING_ARRAY;
 		}
 		List<String> result = new ArrayList<String>();
 		StringBuilder value = new StringBuilder();
 		char ch = 0;
 		int state = CHAR;
 		for (int i = 0; i < stringified.length(); i++) {
 			ch = stringified.charAt(i);
 			switch (state) {
 			case CHAR:
 				if (ch == '\\') {
 					state = BACK_SLASH;
 				} else if (ch == ',') {
 					result.add(value.toString());
 					value.setLength(0);
 				} else {
 					value.append(ch);
 				}
 				break;
 			case BACK_SLASH:
 				if (ch == '\\' || ch == ',') {
 					value.append(ch);
 				} else {
 					Assert.isLegal(false, "Unknown escaped character: " + ch + "."); //$NON-NLS-1$ //$NON-NLS-2$
 				}
 				state = CHAR;
 				break;
 			default:
 				break;
 			}
 		}
 		// final comma
 		if (value.length() != 0 || ch == ',') {
 			result.add(value.toString());
 		}
 		return result.toArray(new String[result.size()]);
 	}
 }
