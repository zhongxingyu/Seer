 /*******************************************************************************
  * Copyright (c) 2010 Oobium, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
  ******************************************************************************/
 package org.oobium.utils;
 
 
 public class CharStreamUtils {
 
 	public static boolean isEqual(char[] ca, int start, int end, String test) {
 		return isEqual(ca, start, end, test.toCharArray());
 	}
 	
 	public static boolean isEqual(char[] ca, int start, int end, char...test) {
 		if(test.length != (end-start)) {
 			return false;
 		}
 		for(int i = start, j = 0; i < end && j < test.length; i++, j++) {
 			if(ca[i] != test[j]) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	public static boolean isEqualIgnoreCase(char[] ca, int start, int end, char...test) {
 		if(test.length != (end-start+1)) {
 			return false;
 		}
 		end = Math.min(end, ca.length);
 		for(int i = start, j = 0; i < end && j < test.length; i++, j++) {
 			if(Character.toLowerCase(ca[i]) != Character.toLowerCase(test[j])) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	public static boolean isNext(char[] ca, int start, char...test) {
 		if(test.length == 0) {
 			return false;
 		}
 		for(int i = start, j = 0; i < ca.length && j < test.length; i++, j++) {
 			if(ca[i] != test[j]) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	public static boolean isWhitespace(char[] ca, int start, int end) {
 		end = Math.min(end, ca.length);
 		for(int i = start; i < end; i++) {
 			if(!Character.isWhitespace(ca[i])) {
 				return false;
 			}
 		}
 		return true;
 	}
 	
 	public static int closer(char[] ca, int start) {
 		return closer(ca, start, ca.length, false, false);
 	}
 	
 	public static int closer(char[] ca, int start, int end) {
 		return closer(ca, start, end, false, false);
 	}
 	
 	public static int closer(char[] ca, int start, int end, boolean escapeComments) {
 		return closer(ca, start, end, escapeComments, escapeComments);
 	}
 	
 	public static int closer(char[] ca, int start, int end, boolean escapeLineComments, boolean escapeMultiLineComments) {
 		end = Math.min(end, ca.length);
 		if(start >= 0 && start < end) {
 			char opener = ca[start];
 			char closer = closerChar(opener);
 			if(closer == 0) {
 				return start; // nothing opened...
 			}
 			
 			if(opener == '"') { // quotes beat comments!
 				escapeLineComments = false;
 				escapeMultiLineComments = false;
 			}
 			
 			int count = 1;
 			for(int i = start+1; i >= 0 && i < end; i++) {
 				if(ca[i] == opener && ca[i] != closer) {
 					count++;
 				} else if(ca[i] == closer) {
 					if((closer != '"' && closer != '\'') || ca[i-1] != '\\') { // check for escape char
 						count--;
 						if(count == 0) {
 							return i;
 						}
 					}
 				} else if(ca[i] == '"') {
 					i = closer(ca, i, end); // just entered a string - get out of it
 					if(i == -1) {
 						return -1;
 					}
 				} else if(escapeMultiLineComments && ca[i] == '*' && i > 0 && ca[i-1] == '/') {
 					for(i = i + 1; i < end && !(ca[i] == '/' && ca[i-1] == '*'); i++);
 				} else if(escapeLineComments && ca[i] == '/' && i > 0 && ca[i-1] == '/') {
 					for(i = i + 1; i < end && ca[i] != '\n'; i++);
 				}
 			}
 		}
 		return -1;
 	}
 	
 	public static boolean isOpener(char c) {
 		return closerChar(c) != 0;
 	}
 	
 	public static char closerChar(char c) {
 		switch(c) {
 			case '<':  return '>';
 			case '(':  return ')';
 			case '{':  return '}';
 			case '[':  return ']';
 			case '"':  return '"';
 			case '\'': return '\'';
 			default: return 0;
 		}
 	}
 	
 	public static boolean contains(char[] ca, char c, int start) {
 		return contains(ca, c, start, ca.length, false);
 	}
 	
 	public static boolean contains(char[] ca, char c, int start, boolean skipStrings) {
 		return contains(ca, c, start, ca.length, skipStrings);
 	}
 	
 	public static boolean contains(char[] ca, char c, int start, int end) {
 		return contains(ca, c, start, end, false);
 	}
 
 	public static boolean contains(char[] ca, char c, int start, int end, boolean skipStrings) {
 		end = Math.min(end, ca.length);
 		for(int i = start; i >= 0 && i < end; i++) {
 			if(c == ca[i]) {
 				return true;
 			} else if(skipStrings && '"' == ca[i]) {
 				i = closer(ca, i, end);
 				if(i == -1) {
 					return false;
 				}
 			}
 		}
 		return false;
 	}
 
 	public static boolean contains(char[] ca, char[] cs, int start, boolean skipStrings) {
 		return contains(ca, cs, start, ca.length, skipStrings);
 	}
 	
 	public static boolean contains(char[] ca, char[] cs, int start, int end, boolean skipStrings) {
 		end = Math.min(end, ca.length);
 		for(int i = start; i >= 0 && i < end; i++) {
 			if(ca[i] == cs[0]) {
 				boolean found = true;
 				for(int j = 1; j < cs.length; j++) {
 					if((i+j) == ca.length || ca[i+j] != cs[j]) {
 						found = false;
 						break;
 					}
 				}
 				if(found) {
 					return true;
 				}
 			} else if(skipStrings && '"' == ca[i]) {
 				i = closer(ca, i, end);
 				if(i == -1) {
 					return false;
 				}
 			}
 		}
 		return false;
 	}
 
 	public static int findAll(char[] ca, int start, char...cs) {
 		return findAll(ca, start, ca.length, cs);
 	}
 	
 	public static int findAll(char[] ca, int start, int end, char...cs) {
 		end = Math.min(end, ca.length);
 		for(int i = start; i >= 0 && i < end; i++) {
 			if(ca[i] == cs[0]) {
 				boolean found = true;
 				for(int j = 1; j < cs.length; j++) {
 					if((i+j) == ca.length || ca[i+j] != cs[j]) {
 						found = false;
 						break;
 					}
 				}
 				if(found) {
 					return i;
 				}
 			}
 		}
 		return -1;
 	}
 
 	public static int findAny(char[] ca, int start, char...cs) {
 		return findAny(ca, start, ca.length, cs);
 	}
 	
 	public static int findAny(char[] ca, int start, int end, char...cs) {
 		end = Math.min(end, ca.length);
 		for(int i = start; i >= 0 && i < end; i++) {
 			for(int j = 0; j < cs.length; j++) {
 				if(ca[i] == cs[j]) {
 					return i;
 				}
 			}
 		}
 		return -1;
 	}
 
 	/**
 	 * @param ca
 	 * @param offset
 	 * @return true if the offset is the beginning of a line; false otherwise
 	 */
 	public static boolean isBOL(char[] ca, int offset) {
 		return (offset == 0) || (ca[offset-1] == '\n');
 	}
 	
 	/**
 	 * Finds and returns the last character of the line
 	 * containing the given offset.  Checks for both
 	 * unix style line endings (\n) as well as
 	 * windows style (\r\n).
 	 * If no line ending is found, returns the position
 	 * of the length the array (ca.length()).
 	 */
 	public static int findEOL(char[] ca, int offset) {
 		for(int i = offset; i < ca.length; i++) {
 			if(ca[i] == '\n') {
 				return i;
 			} else if(ca[i] == '\r') {
 				if(i < ca.length-1 && ca[i+1] == '\n') {
 					return i;
 				}
 			}
 		}
 		return ca.length;
 	}
 	
 	public static int find(char[] ca, char c) {
 		return find(ca, c, 0, ca.length);
 	}
 
 	public static int find(char[] ca, char c, int start) {
 		return find(ca, c, start, ca.length);
 	}
 
 	public static int find(char[] ca, char c, int start, int end) {
 		end = Math.min(end, ca.length);
 		for(int i = start; i >= 0 && i < end; i++) {
 			if(c == ca[i]) {
 				return i;
 			}
 		}
 		return -1;
 	}
 
 	public static int findOutside(char[] ca, char c, char... skipBlocks) {
 		return findOutside(ca, c, 0, ca.length, skipBlocks);
 	}
 	
 	public static int findOutside(char[] ca, char c, int start, char... skipBlocks) {
 		return findOutside(ca, c, start, ca.length, skipBlocks);
 	}
 	
 	public static int findOutside(char[] ca, char c, int start, int end, char... skipBlocks) {
 		end = Math.min(end, ca.length);
 		for(int i = start; i >= 0 && i < end; i++) {
 			if(c == ca[i]) {
 				return i;
 			} else {
 				for(char opener : skipBlocks) {
 					if(ca[i] == opener) {
 						i = closer(ca, i, end);
 						if(i == -1) {
 							return -1;
 						}
 					}
 				}
 			}
 		}
 		return -1;
 	}
 
 	public static int forward(char[] ca, int from) {
 		for(int i = from; i >= 0 && i < ca.length; i++) {
 			if(!Character.isWhitespace(ca[i])) {
 				return i;
 			}
 		}
 		return -1;
 	}
 	
 	public static int forward(char[] ca, int start, int end) {
 		end = Math.min(end, ca.length);
 		for(int i = start; i >= 0 && i < end; i++) {
 			if(!Character.isWhitespace(ca[i])) {
 				return i;
 			}
 		}
 		return -1;
 	}
 	
 	public static Double getDouble(char[] ca, int start, int end) {
 		boolean foundDecimal = false;
 		end = Math.min(end, ca.length);
 		for(int i = start; i < end; i++) {
 			if(ca[i] == '-') {
				if(i != start) {
 					return null;
 				}
 			} else if(ca[i] == '.') {
 				if(foundDecimal) {
 					return null;
 				}
 				foundDecimal = true;
 			} else if(!Character.isDigit(ca[i])) {
 				return null;
 			}
 		}
 		if(foundDecimal) {
 			try {
 				return Double.valueOf(new String(ca, start, end-start));
 			} catch(NumberFormatException nfe) {
 			}
 		}
 		return null;
 	}
 	
 	public static Long getLong(char[] ca, int start, int end) {
 		end = Math.min(end, ca.length);
 		for(int i = start; i < end; i++) {
 			if(ca[i] == '-') {
				if(i != start) {
 					return null;
 				}
 			} else if(!Character.isDigit(ca[i])) {
 				return null;
 			}
 		}
 		try {
 			return Long.valueOf(new String(ca, start, end-start));
 		} catch(NumberFormatException nfe) {
 			return null;
 		}
 	}
 	
 	public static Integer getInteger(char[] ca, int start, int end) {
 		end = Math.min(end, ca.length);
 		for(int i = start; i < end; i++) {
 			if(ca[i] == '-') {
 				if(i != start) {
 					return null;
 				}
 			} else if(!Character.isDigit(ca[i])) {
 				return null;
 			}
 		}
 		try {
 			return Integer.valueOf(new String(ca, start, end-start));
 		} catch(NumberFormatException nfe) {
 			return null;
 		}
 	}
 	
 	public static int reverse(char[] ca, char c, int from) {
 		for(int i = from; i >= 0 && i < ca.length; i--) {
 			if(!Character.isWhitespace(ca[i])) {
 				if(c == ca[i]) {
 					return i;
 				}
 			}
 		}
 		return -1;
 	}
 
 	public static int reverse(char[] ca, int from) {
 		for(int i = from; i >= 0 && i < ca.length; i--) {
 			if(!Character.isWhitespace(ca[i])) {
 				return i;
 			}
 		}
 		return -1;
 	}
 	
 }
