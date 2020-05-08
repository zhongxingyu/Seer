 /*******************************************************************************
  * Copyright (c) 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 /*
  *  $RCSfile: FeatureAttributeValue.java,v $
 *  $Revision: 1.6 $  $Date: 2005/06/29 15:16:23 $ 
  */
 package org.eclipse.jem.internal.beaninfo.common;
 
 import java.io.*;
 import java.lang.reflect.Array;
 import java.util.Arrays;
import java.util.logging.Level;
 import java.util.regex.Pattern;
 
import org.eclipse.jem.internal.beaninfo.core.BeaninfoPlugin;
 import org.eclipse.jem.internal.proxy.common.MapTypes;
 
  
 
 /**
  * This is the value for a FeatureAttribute. It wrappers the true java object.
  * Use the getObject method to get the java value.
  * <p>
  * We can only represent Strings, primitives, and arrays. (Primitives will converted
  * to their wrapper class (e.g. Long), and byte, short, and int will move up to Long,
  * and float will move up to Double).  And any kind of valid array on the java BeanInfo side
  * will be converted to an Object array on the IDE side. We don't have the capability to allow more complex objects 
  * because the IDE may not have the necessary classes available to it that 
  * the BeanInfo may of had available to it. Invalid objects will be represented
  * by the singleton instance of {@link org.eclipse.jem.internal.beaninfo.common.InvalidObject}.
  * <p>
  * <b>Note:</b>
  * Class objects that are values of Feature attributes on the java BeanInfo side will be
  * converted to simple strings containing the classname when moved to the client (IDE) side.
  * That is because the classes probably will not be available on the IDE side, but can be
  * used to reconstruct the class when used back on the java vm side. 
  * @since 1.1.0
  */
 public class FeatureAttributeValue implements Serializable {
 	
 	private transient Object value;
 	private transient Object internalValue;
 	private static final long serialVersionUID = 1105717634844L;
 	
 	/**
 	 * Create the value with the given init string.
 	 * <p>
 	 * This is not meant to be used by clients.
 	 * @param initString
 	 * 
 	 * @since 1.1.0
 	 */
 	public FeatureAttributeValue(String initString) {
 		// Use the init string to create the value. This is our
 		// own short-hand for this.
 		value = parseString(initString);
 	}
 	
 	/**
 	 * This is used when customer wants to fluff one up.
 	 * 
 	 * 
 	 * @since 1.1.0
 	 */
 	public FeatureAttributeValue() {
 		
 	}
 
 	/**
 	 * @return Returns the value.
 	 * 
 	 * @since 1.1.0
 	 */
 	public Object getValue() {
 		return value;
 	}
 	
 	/**
 	 * Set a value.
 	 * @param value The value to set.
 	 * @since 1.1.0
 	 */
 	public void setValue(Object value) {
 		this.value = value;
 		this.setInternalValue(null);
 	}
 		
 	/**
 	 * Set the internal value.
 	 * @param internalValue The internalValue to set.
 	 * 
 	 * @since 1.1.0
 	 */
 	public void setInternalValue(Object internalValue) {
 		this.internalValue = internalValue;
 	}
 
 	/**
 	 * This is the internal value. It is the <code>value</code> massaged into an easier to use form
 	 * in the IDE. It will not be serialized out. It will not be reconstructed from an init string.
 	 * <p> 
 	 * It does not need to be used. It will be cleared if
 	 * a new value is set. For example, if the value is a complicated array (because you can't have
 	 * special classes in the attribute value on the BeanInfo side) the first usage of this value can
 	 * be translated into an easier form to use, such as a map.
 	 * 
 	 * @return Returns the internalValue.
 	 * 
 	 * @since 1.1.0
 	 */
 	public Object getInternalValue() {
 		return internalValue;
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 */
 	public String toString() {
 		if (value == null)
 			return super.toString();
 		return makeString(value);
 	}
 	
 
 	/**
 	 * Helper method to take the object and turn it into the
 	 * string form that is required for EMF serialization.
 	 * <p>
 	 * This is used internally. It can be used for development 
 	 * purposes by clients, but they would not have any real
 	 * runtime need for this.
 	 * <p>
 	 * Output format would be (there won't be any newlines in the actual string)
 	 * <pre>
 	 *   String: "zxvzxv"
 	 *   Number: number
 	 *   Boolean: true or false
 	 *   Character: 'c'
 	 *   null: null
 	 * 
 	 *   Array: (all arrays will be turned into Object[])
 	 *     [dim]{e1, e2}
 	 *     [dim1][dim2]{[dim1a]{e1, e2}, [dim2a]{e3, e4}}
 	 *   where en are objects that follow the pattern for single output above.
 	 * 
 	 *   Any invalid object (i.e. not one of the ones we handle) will be:
 	 *     INV
 	 * 
 	 *   Arrays of invalid types (not Object, String, Number, Boolean, Character,
 	 *     or primitives) will be marked as INV.
 	 * </pre>
 	 * @param value
 	 * @return serialized form as a string.
 	 * 
 	 * @since 1.1.0
 	 */
 	public static String makeString(Object value) {
 		StringBuffer out = new StringBuffer(100);
 		makeString(value, out);
 		return out.toString();
 	}
 	
 	private static final Pattern QUOTE = Pattern.compile("\"");	// Pattern for searching for double-quote. Make it static so don't waste time compiling each time. //$NON-NLS-1$
 	private static final String NULL = "null";	// Output string for null //$NON-NLS-1$
 	private static final String INVALID = "INV";	// Invalid object flag. //$NON-NLS-1$
 	
 	/*
 	 * Used for recursive building of the string.
 	 */
 	private static void makeString(Object value, StringBuffer out) {
 		if (value == null)
 			out.append(NULL);
 		else if (value instanceof String || value instanceof Class) {
 			// String: "string" or "string\"stringend" if str included a double-quote.
 			out.append('"');
 			// If class, turn value into the classname.
 			String str = value instanceof String ? (String) value : ((Class) value).getName();
 			if (str.indexOf('"') != -1) {
 				// Replace double-quote with escape double-quote so we can distinquish it from the terminating double-quote.
 				out.append(QUOTE.matcher(str).replaceAll("\\\\\""));	// Don't know why we need the back-slash to be doubled for replaceall, but it doesn't work otherwise. //$NON-NLS-1$
 			} else
 				out.append(str);
 			out.append('\"');
 		} else if (value instanceof Number) {
 			// Will go out as either a integer number or a floating point number. 
 			// When read back in it will be either a Long or a Double.
 			out.append(value);
 		} else if (value instanceof Boolean) {
 			// It will go out as either true or false.
 			out.append(value);
 		} else if (value instanceof Character) {
 			// Character: 'c' or '\'' if char was a quote.
 			out.append('\'');
 			Character c = (Character) value;
 			if (c.charValue() != '\'')
 				out.append(c.charValue());
 			else
 				out.append("\\'"); //$NON-NLS-1$
 			out.append('\'');
 		} else if (value.getClass().isArray()) {
 			// Handle array format.
 			Class type = value.getClass();
 			// See if final type is a valid type.
 			Class ft = type.getComponentType();
 			int dims = 1;
 			while (ft.isArray()) {
 				dims++;
 				ft = ft.getComponentType();
 			}
 			if (ft == Object.class || ft == String.class || ft == Boolean.class || ft == Character.class || ft.isPrimitive() || Number.class.isAssignableFrom(ft)) {
 				// [length][][] {....}
 				out.append('[');
 				int length = Array.getLength(value); 
 				out.append(length);
 				out.append(']');
 				while(--dims > 0) {
 					out.append("[]"); //$NON-NLS-1$
 				}
 				out.append('{');
 				for (int i=0; i < length; i++) {
 					if (i != 0)
 						out.append(',');
 					makeString(Array.get(value, i), out);
 				}
 				out.append('}');
 			} else
 				out.append(INVALID);	// Any other kind of array is invalid.
 		} else {
 			out.append(INVALID);
 		}
 	}
 	
 	
 	/**
 	 * Helper method to take the string input from EMF serialization and turn it
 	 * into an Object.
 	 * <p>
 	 * This is used internally. It can be used for development 
 	 * purposes by clients, but they would not have any real
 	 * runtime need for this.
 	 * <p>
 	 * The object will be an object, null, or an Object array. Any value
 	 * that is invalid will be set to the {@link InvalidObject#INSTANCE} static
 	 * instance.
 	 * 
 	 * @param input
 	 * @return object decoded from the input.
 	 * 
 	 * @see #makeString(Object)
 	 * @since 1.1.0
 	 */
 	public static Object parseString(String input) {
 		return parseString(new StringParser(input));
 	}
 	
 	private static class StringParser {
 		private int next=0;
 		private int length;
 		private String input;
 		
 		public StringParser(String input) {
 			this.input = input;
 			this.length = input.length();
 		}
 		
 		public String toString() {
 			return "StringParser: \""+input+'"';
 		}
 		
 		public void skipWhitespace() {
 			while(next < length) {
 				if (!Character.isWhitespace(input.charAt(next++))) {
 					next--;	// Put it back as not yet read since it is not whitespace.
 					break;
 				}
 			}
 		}
 		
 		/**
 		 * Return the next index
 		 * @return
 		 * 
 		 * @since 1.1.0
 		 */
 		public int nextIndex() {
 			return next;
 		}
 		
 		/**
 		 * Get the length of the input
 		 * @return input length
 		 * 
 		 * @since 1.1.0
 		 */
 		public int getLength() {
 			return length;
 		}
 		
 		
 		/**
 		 * Read the current character and go to next.
 		 * @return current character
 		 * 
 		 * @since 1.1.0
 		 */
 		public char read() {
 			return next<length ? input.charAt(next++) : 0;
 		}
 		
 		/**
 		 * Backup the parser one character.
 		 * 
 		 * 
 		 * @since 1.1.0
 		 */
 		public void backup() {
 			if (--next < 0)
 				next = 0;
 		}
 		
 		/**
 		 * Peek at the char at the next index, but don't increment afterwards.
 		 * @return
 		 * 
 		 * @since 1.1.0
 		 */
 		public char peek() {
 			return next<length ? input.charAt(next) : 0;
 		}
 		
 		/**
 		 * Have we read the last char.
 		 * @return <code>true</code> if read last char.
 		 * 
 		 * @since 1.1.0
 		 */
 		public boolean atEnd() {
 			return next>=length;
 		}
 		
 		/**
 		 * Reset to the given next index.
 		 * @param nextIndex the next index to do a read at.
 		 * 
 		 * @since 1.1.0
 		 */
 		public void reset(int nextIndex) {
 			if (nextIndex<=length)
 				next = nextIndex;
 			else
 				next = length;
 		}
 		
 		/**
 		 * Skip the next number of chars.
 		 * @param skip number of chars to skip.
 		 * 
 		 * @since 1.1.0
 		 */
 		public void skip(int skip) {
 			if ((next+=skip) > length)
 				next = length;
 		}
 		
 		/**
 		 * Return the string input.
 		 * @return the string input
 		 * 
 		 * @since 1.1.0
 		 */
 		public String getInput() {
 			return input;
 		}
 				
 	}
 	
 	/*
 	 * Starting a parse for an object at the given index.
 	 * Return the parsed object or InvalidObject if no
 	 * object or if there was an error parsing.
 	 */
 	private static Object parseString(StringParser parser) {
 		parser.skipWhitespace();
 		if (!parser.atEnd()) {
 			char c = parser.read();
 			switch (c) {
 				case '"':
 					// Start of a quoted string. Scan for closing quote, ignoring escaped quotes.
 					int start = parser.nextIndex();	// Index of first char after '"'
 					char[] dequoted = null;	// Used if there is an escaped quote. That is the only thing we support escape on, quotes.
 					int dequoteIndex = 0;
 					while (!parser.atEnd()) {
 						char cc = parser.read();
 						if (cc == '"') {
 							// If we didn't dequote, then just do substring.
 							if (dequoted == null)
 								return parser.getInput().substring(start, parser.nextIndex()-1);	// next is char after '"', so end of string index is index of '"'
 							else {
 								// We have a dequoted string. So turn into a string.
 								// Gather the last group
 								int endNdx = parser.nextIndex()-1;
 								parser.getInput().getChars(start, endNdx, dequoted, dequoteIndex);
 								dequoteIndex+= (endNdx-start);
 								return new String(dequoted, 0, dequoteIndex);
 							}
 						} else if (cc == '\\') {
 							// We had an escape, see if next is a quote. If it is we need to strip out the '\'.
 							if (parser.peek() == '"') {
 								if (dequoted == null) {
 									dequoted = new char[parser.getLength()];
 								}
 								int endNdx = parser.nextIndex()-1;
 								parser.getInput().getChars(start, endNdx, dequoted, dequoteIndex);	// Get up to, but not including '\'
 								dequoteIndex+= (endNdx-start);
 								// Now also add in the escaped quote.
 								dequoted[dequoteIndex++] = parser.read();
 								start = parser.nextIndex();	// Next group is from next index.
 							}
 						}
 					}
 					break;	// If we got here, it is invalid.
 					
 				case '-':
 				case '0':
 				case '1':
 				case '2':
 				case '3':
 				case '4':
 				case '5':
 				case '6':
 				case '7':
 				case '8':
 				case '9':
 					// Possible number.
 					// Scan to next non-digit, or not part of valid number.
 					boolean numberComplete = false;
 					boolean floatType = false;
 					boolean foundE = false;
 					boolean foundESign = false;
 					start = parser.nextIndex()-1;	// We want to include the sign or first digit in the number.
 					while (!parser.atEnd() && !numberComplete) {
 						char cc = parser.read();
 						switch (cc) {
 							case '0':
 							case '1':
 							case '2':
 							case '3':
 							case '4':
 							case '5':
 							case '6':
 							case '7':
 							case '8':
 							case '9':
 								break;	// This is good, go on.
 							case '.':
 								if (floatType)
 									return InvalidObject.INSTANCE;	// We already found a '.', two are invalid.
 								floatType = true;
 								break;
 							case 'e':
 							case 'E':
 								if (foundE)
 									return InvalidObject.INSTANCE;	// We already found a 'e', two are invalid.
 								foundE = true;
 								floatType = true;	// An 'e' makes it a float, if not already.
 								break;
 							case '+':
 							case '-':
 								if (!foundE || foundESign) 
 									return InvalidObject.INSTANCE;	// A +/- with no 'e' first is invalid. Or more than one sign.
 								foundESign = true;
 								break;
 							default:
 								// Anything else is end of number.
 								parser.backup();	// Back it up so that next parse will start with this char.
 								numberComplete = true;	// So we stop scanning
 								break;
 						}
 					}
 					try {
 						if (!floatType)
 							return Long.valueOf(parser.getInput().substring(start, parser.nextIndex()));
 						else
 							return Double.valueOf(parser.getInput().substring(start, parser.nextIndex()));
 					} catch (NumberFormatException e) {
 					}
 					break; // If we got here, it is invalid.
 					
 				case 't':
 				case 'T':
 				case 'f':
 				case 'F':
 					// Possible boolean.
 					if (parser.getInput().regionMatches(true, parser.nextIndex()-1, "true", 0, 4)) { //$NON-NLS-1$
 						parser.skip(3);	// Skip over rest of string.
 						return Boolean.TRUE;
 					} else if (parser.getInput().regionMatches(true, parser.nextIndex()-1, "false", 0, 5)) { //$NON-NLS-1$
 						parser.skip(4);	// Skip over rest of string.
 						return Boolean.FALSE;						
 					}
 					break; // If we got here, it is invalid.
 					
 				case '\'':
 					// Possible character
 					char cc = parser.read();
 					// We really only support '\\' and '\'' anything else will be treated as ignore '\' because we don't know handle full escapes.
 					if (cc == '\\')
 						cc = parser.read();	// Get what's after it.
 					else if (cc == '\'')
 						break;	// '' is invalid.
 					if (parser.peek() == '\'') {
 						// So next char after "character" is is a quote. This is good.
 						parser.read();	// Now consume the quote
 						return new Character(cc);
 					}
 					break; // If we got here, it is invalid.
 					
 				case 'n':
 					// Possible null.
 					if (parser.getInput().regionMatches(parser.nextIndex()-1, "null", 0, 4)) { //$NON-NLS-1$
 						parser.skip(3);	// Skip over rest of string.
 						return null;
 					}
 					break; // If we got here, it is invalid.
 					
 				case 'I':
 					// Possible invalid value.
 					if (parser.getInput().regionMatches(parser.nextIndex()-1, INVALID, 0, INVALID.length())) {
 						parser.skip(INVALID.length()-1);	// Skip over rest of string.
 						return InvalidObject.INSTANCE;
 					}
 					break; // If we got here, it is invalid.
 					
 				case '[':
 					// Possible array.
 					// The next field should be a number, so we'll use parseString to get the number. 
 					Object size = parseString(parser);
 					if (size instanceof Long) {
 						parser.skipWhitespace();
 						cc = parser.read();	// Se if next is ']'
 						if (cc == ']') {
 							// Good, well-formed first dimension
 							int dim = 1;
 							boolean valid = true;
 							// See if there are more of just "[]". the number of them is the dim.
 							while (true) {
 								parser.skipWhitespace();
 								cc = parser.read();
 								if (cc == '[') {
 									parser.skipWhitespace();
 									cc = parser.read();
 									if (cc == ']')
 										dim++;
 									else {
 										// This is invalid.
 										valid = false;
 										parser.backup();
 										break;	// No more dims.
 									}
 								} else {
 									parser.backup();
 									break;	// No more dims.
 								}
 							}
 							if (valid) {
 								parser.skipWhitespace();
 								cc = parser.read();
 								if (cc == '{') {
 									// Good, we're at the start of the initialization code.
 									int[] dims = new int[dim];
 									int len = ((Long) size).intValue();
 									dims[0] = len;
 									Object array = Array.newInstance(Object.class, dims);
 									Arrays.fill((Object[]) array, null);	// Because newInstance used above fills the array created with empty arrays when a dim>1.
 									
 									// Now we start filling it in.
 									Object invSetting = null;	// What we will use for the invalid setting. If this is a multidim, this needs to be an array. Will not create it until needed.
 									Object entry = parseString(parser);	// Get the first entry
 									Class compType = array.getClass().getComponentType();
 									int i = -1;
 									while (true) {
 										if (++i < len) {
 											if (compType.isInstance(entry)) {
 												// Good, it can be assigned.
 												Array.set(array, i, entry);
 											} else {
 												// Bad. Need to set invalid.
 												if (invSetting == null) {
 													// We haven't created it yet.
 													if (dim == 1)
 														invSetting = InvalidObject.INSTANCE; // Great, one dimensional, we can use invalid directly
 													else {
 														// Multi-dim. Need to create a valid array that we can set.
 														int[] invDims = new int[dim - 1];
 														Arrays.fill(invDims, 1); // Length one all of the way so that the final component can be invalid object
 														invSetting = Array.newInstance(Object.class, invDims);
 														Object finalEntry = invSetting; // Final array (with component type of just Object). Start with the full array and work down.
 														for (int j = invDims.length - 1; j > 0; j--) {
 															finalEntry = Array.get(finalEntry, 0);
 														}
 														Array.set(finalEntry, 0, InvalidObject.INSTANCE);
 													}
 												}
 												Array.set(array, i, invSetting);
 											}
 										}
 										
 										parser.skipWhitespace();
 										cc = parser.read();
 										if (cc == ',') {
 											// Good, get next
 											entry = parseString(parser);
 										} else if (cc == '}') {
 											// Good, reached the end.
 											break;
 										} else {
 											if (!parser.atEnd()) {
 												parser.backup();
 												entry = parseString(parser); // Technically this should be invalid, but we'll let a whitespace also denote next entry.
 											} else {
 												// It's really screwed up. The string just ended.
												BeaninfoPlugin.getPlugin().getLogger().log(new IllegalStateException(parser.toString()), Level.WARNING);
 												return InvalidObject.INSTANCE;
 											}
 										}
 									}
 									
 									return array;
 								}
 							}							
 						}
 					}
 					break; // If we got here, it is invalid.
 			}
 		}
 		return InvalidObject.INSTANCE;
 	}
 	
 	private void writeObject(ObjectOutputStream out) throws IOException {
 		// Write out any hidden stuff
 		out.defaultWriteObject();
 		writeObject(value, out);
 	}
 	
 	private void writeObject(Object value, ObjectOutputStream out) throws IOException {
 		if (value == null)
 			out.writeObject(value);
 		else {
 			if (value instanceof Class)
 				out.writeObject(((Class) value).getName());
 			else if (!value.getClass().isArray()) {
 				if (value instanceof String || value instanceof Number || value instanceof Boolean || value instanceof Character)
 					out.writeObject(value);
 				else
 					out.writeObject(InvalidObject.INSTANCE);
 			} else {
 				// Array is tricky. See if it is one we can handle, if not then invalid. 
 				// To indicate array, we will first write out the Class of the Component type of the array (it will
 				// be converted to be Object or Object[]...).
 				// This will be the clue that an array is coming. Class values will never
 				// be returned, so that is how we can tell it is an array.
 				// Note: The reason we are using the component type (converted to Object) is because to reconstruct on the other side we need
 				// to use the component type plus length of the array's first dimension.
 				// 
 				// We can not just serialize the array in the normal way because it may contain invalid values, and we need to 
 				// handle that. Also, if it wasn't an Object array, we need to turn it into an object array. We need consistency
 				// in that it should always be an Object array.
 				// So output format will be:
 				// Class(component type)
 				// int(size of first dimension)
 				// Object(value of first entry) - Actually use out writeObject() format to allow nesting of arrays.
 				// Object(value of second entry)
 				// ... up to size of dimension.
 				Class type = value.getClass();
 				// See if final type is a valid type.
 				Class ft = type.getComponentType();
 				int dims = 1;
 				while (ft.isArray()) {
 					dims++;
 					ft = ft.getComponentType();
 				}
 				if (ft == Object.class || ft == String.class || ft == Boolean.class || ft == Character.class || ft.isPrimitive() || ft == Class.class || Number.class.isAssignableFrom(ft)) {
 					String jniType = dims == 1 ? "java.lang.Object" : MapTypes.getJNITypeName("java.lang.Object", dims-1); //$NON-NLS-1$ //$NON-NLS-2$
 					try {
 						Class componentType = Class.forName(jniType);
 						out.writeObject(componentType);
 						int length = Array.getLength(value);
 						out.writeInt(length);
 						for (int i = 0; i < length; i++) {
 							writeObject(Array.get(value, i), out);
 						}
 					} catch (ClassNotFoundException e) {
 						// This should never happen. Object arrays are always available.
 					}
 				} else
 					out.writeObject(InvalidObject.INSTANCE);
 			}
 		}
 	}
 	
 	
 	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
 		// Read in any hidden stuff
 		in.defaultReadObject();
 		
 		value = readActualObject(in);
 	}
 	
 	private Object readActualObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
 		Object val = in.readObject();
 		if (val instanceof Class) {
 			// It must be an array. Only Class objects that come in are Arrays of Object.
 			int length = in.readInt();
 			Object array = Array.newInstance((Class) val, length);
 			for (int i = 0; i < length; i++) {
 				Array.set(array, i, readActualObject(in));
 			}
 			return array;
 		} else
 			return val;	// It is the value itself.
 	}
 	
 }
