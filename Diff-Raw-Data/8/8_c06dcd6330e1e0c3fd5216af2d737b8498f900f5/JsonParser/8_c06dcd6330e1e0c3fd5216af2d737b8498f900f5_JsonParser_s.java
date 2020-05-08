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
 package org.oobium.utils.json;
 
 import static org.oobium.utils.CharStreamUtils.closer;
 import static org.oobium.utils.CharStreamUtils.find;
 import static org.oobium.utils.CharStreamUtils.findOutside;
 import static org.oobium.utils.CharStreamUtils.forward;
 import static org.oobium.utils.CharStreamUtils.getDouble;
 import static org.oobium.utils.CharStreamUtils.getInteger;
 import static org.oobium.utils.CharStreamUtils.getLong;
 import static org.oobium.utils.CharStreamUtils.isEqual;
 import static org.oobium.utils.CharStreamUtils.isNext;
 import static org.oobium.utils.CharStreamUtils.reverse;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.oobium.utils.Base64;
 
 public class JsonParser {
 
 	private boolean keepOrder;
 	private boolean stringsOnly;
 	private IConverter converter;
 	
 	public void setKeepOrder(boolean keepOrder) {
 		this.keepOrder = keepOrder;
 	}
 	
 	private Object getObject(char[] ca, int start, int end) {
 		if(ca == null || start >= end) {
 			return null;
 		}
 		
 		int s1 = forward(ca, start, end);
 		int s2 = reverse(ca, end-1) + 1;
 
 		if(ca[s1] == '"' || ca[s1] == '\'') {
 			if((ca[s1] == '"' && ca[s2-1] == '"') || (ca[s1] == '\'' && ca[s2-1] == '\'')) {
 				if(ca[s1] == '\'') {
 					if(s2 - s1 == 3) { // this actually means they are 1 character apart: '1'
 						return new Character(ca[s1+1]);
 					} else if(s2 - s1 == 4) {
 						if(ca[s1+1] == '\\') {
 							switch(ca[s1+2]) {
 							case 'b':  return new Character('\b');
 							case 't':  return new Character('\t');
 							case 'n':  return new Character('\n');
 							case 'f':  return new Character('\f');
 							case 'r':  return new Character('\r');
 							case '"':  return new Character('\"');
 							case '\'': return new Character('\'');
 							case '\\': return new Character('\\');
 							}
 						}
 					} else if(s2 - s1 == 8) {
 						if(ca[s1+1] == '\\' && ca[s1+2] == 'u') {
 							try {
 								Integer u = Integer.parseInt(new String(ca, s1+3, 4), 16);
 								return new Character((char) u.intValue());
 							} catch(NumberFormatException e) {
 								// discard and fall through
 							}
 						}
 					}
 				}
 				if(isNext(ca, s1+1, new char[] {'/','D','a','t','e','('}) && ca[s2-3] == ')' && ca[s2-2] == '/') {
 					return new Date(getLong(ca, s1+7, s2-3));
 				} else if(isNext(ca, s1+1, new char[] {'/','B','a','s','e','6','4','('}) && ca[s2-3] == ')' && ca[s2-2] == '/') {
 					return Base64.decode(new String(ca, s1+9, s2-s1-12));
 				} else {
 					// remove escape characters, if any
 					StringBuilder sb = new StringBuilder(s2-s1-2);
 					for(int i = s1+1; i < s2-1; i++) {
 						if(ca[i] == '\\' && i < ca.length && ca[i+1] == ca[s1]) {
 							continue;
 						}
 						sb.append(ca[i]);
 					}
 					return sb.toString();
 				}
 			}
 			return null;
 		}
 		if(isEqual(ca, s1, s2, new char[] {'n','u','l','l'})) {
 			return null;
 		}
 		if(isEqual(ca, s1, s2, new char[] {'t','r','u','e'})) {
 			return true;
 		}
 		if(isEqual(ca, s1, s2, new char[] {'f','a','l','s','e'})) {
 			return false;
 		}
 		if(ca[s1] == '[') {
 			if(ca[s2-1] == ']') {
 				return toList(ca, s1, s2);
 			}
 		}
 		if(ca[s1] == '{') {
 			if(ca[s2-1] == '}') {
 				return toMap(ca, s1, s2);
 			}
 		}
 		
 		Object obj = getDouble(ca, s1, s2);
 		if(obj == null) {
 			obj = getInteger(ca, s1, s2);
 		}
 		if(obj == null) {
 			obj = getLong(ca, s1, s2);
 		}
 
 		return (obj != null) ? obj : new String(ca, s1, s2-s1);
 	}
 	
 	public void setConverter(IConverter converter) {
 		this.converter = converter;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public Map<String, String> toStringMap(String json) {
 		if(json == null || json.length() == 0) {
 			return keepOrder ? new LinkedHashMap<String, String>() : new HashMap<String, String>();
 		}
 		stringsOnly = true;
 		return (Map<String, String>) toMap(json.toCharArray(), 0, json.length());
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<String> toStringList(String json) {
 		if(json == null || json.length() == 0) {
 			return new ArrayList<String>(0);
 		}
 		stringsOnly = true;
 		return (List<String>) toList(json.toCharArray(), 0, json.length());
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<Object> toList(String json) {
 		if(json == null || json.length() == 0) {
 			return new ArrayList<Object>(0);
 		}
 		return (List<Object>) toList(json.toCharArray(), 0, json.length());
 	}
 	
 	private List<?> toList(char[] ca, int start, int end) {
 		int s = forward(ca, start, end);
 		
 		if(s == -1) {
 			// empty string - exit quick
 			return new ArrayList<Object>(0);
 		}
 
 		int e = end;
 		
 		List<Object> list = new ArrayList<Object>();
 
 		// container found, try again
 		if(ca[s] == '(') {
 			s = forward(ca, s+1, end);
 			e = reverse(ca, ')', end-1);
 		}
 		
 		if(ca[s] == '[') {
 			s = forward(ca, s+1, e);
 			e = reverse(ca, ']', e-1);
 		}
 		e = reverse(ca, e - 1) + 1;
 
 		int s1 = s;
 		int s2 = s;
 
 		while (s1 > -1 && s2 < e) {
 			if (ca[s1] == '{' || ca[s1] == '[' || ca[s1] == '"' || ca[s1] == '\'') {
 				s2 = closer(ca, s1, e) + 1;
 				if(s2 == 0) break; // invalid format - no closer
 			} else {
 				s2 = find(ca, ',', s1, e);
 				if (s2 == -1) {
 					s2 = e;
 				} else {
 					s2 = reverse(ca, s2-1) + 1;
 				}
 			}
 			if(stringsOnly) {
 				if(ca[s1] == '\'' || ca[s1] == '"') {
 					s1++;
 				}
 				if(ca[s2-1] == '\'' || ca[s2-1] == '"') {
 					s2--;
 				}
 				list.add(new String(ca, s1, s2-s1));
 			} else {
 				list.add(toObject(ca, s1, s2));
 			}
 			s1 = find(ca, ',', s2, e);
 			if (s1 == -1) {
 				break;
 			} else {
 				s1 = forward(ca, s1+1, e);
 			}
 		}
 
 		return list;
 	}
 
 	@SuppressWarnings("unchecked")
 	public Map<String, Object> toMap(String json) {
 		if(json == null || json.length() == 0) {
 			return keepOrder ? new LinkedHashMap<String, Object>(0) : new HashMap<String, Object>(0);
 		}
 		return (Map<String, Object>) toMap(json.toCharArray(), 0, json.length());
 	}
 	
 	private Map<String, ?> toMap(char[] ca, int start, int end) {
 		int s = forward(ca, start, end);
 		
 		if(s == -1) {
 			// empty string - exit quick
 			return keepOrder ? new LinkedHashMap<String, Object>(0) : new HashMap<String, Object>(0);
 		}
 
 		int e = end;
 		
 		// container found, try again
 		if(ca[s] == '(') {
 			s = forward(ca, s+1, end);
 			e = reverse(ca, ')', end-1);
 		}
 		
 		int s1;
 		if(ca[s] == '{') {
 			s1 = forward(ca, s+1, e);
 			e = reverse(ca, '}', e-1);
 		} else {
 			s1 = s;
 			e = reverse(ca, e-1) + 1;
 		}
 
 		if(s1 < -1 || e < -1) {
 			return null;
 		}
 		
 		Map<String, Object> map = keepOrder ? new LinkedHashMap<String, Object>() : new HashMap<String, Object>();
 		
 		s = findOutside(ca, ':', s1, e, '\'', '"');
 		int s2 = reverse(ca, s-1);
 
 		while(s1 > -1 && s1 <= s2 && s2 < e) {
 			if(ca[s1] == '\'' || ca[s1] == '"') {
 				s1 = forward(ca, s1+1, s2);
 			}
 			if(ca[s2] == '\'' || ca[s2] == '"') {
 				s2 = reverse(ca, s2-1);
 			}
 			
 			String key = new String(ca, s1, s2-s1+1);
 			Object value = null;
 
 			s1 = forward(ca, s+1, e);
 			if(stringsOnly) {
				s2 = find(ca, ',', s1, e);
 				if(s2 < 0 || s2 >= e) {
 					s2 = reverse(ca, e-1);
 				} else {
 					s2--;
 				}
 				if(ca[s1] == '\'' || ca[s1] == '"') {
 					s1++;
 				}
 				if(ca[s2] == '\'' || ca[s2] == '"') {
 					s2--;
 				}
 				value = new String(ca, s1, s2-s1+1);
 			} else {
 				switch(ca[s1]) {
 				case '[':
 					s2 = closer(ca, s1, e) + 1;
 					value = toList(ca, s1, s2);
 					break;
 				case '{':
 					s2 = closer(ca, s1, e) + 1;
 					value = toMap(ca, s1, s2);
 					break;
 				case '\'':
 				case '"':
 					s2 = closer(ca, s1, e) + 1;
 					value = toObject(ca, s1, s2);
 					break;
 				default:
 					s2 = find(ca, ',', s1, e);
 					if(s2 < 0 || s2 >= e) {
 						s2 = reverse(ca, e-1) + 1;
 					}
 					value = toObject(ca, s1, s2);
 					break;
 				}
 			}
 			
 			map.put(key, value);
 	
 			s1 = find(ca, ',', s2, e);
 			if(s1 == -1) {
 				break;
 			}
 			s1 = forward(ca, s1+1, e);
 			s = findOutside(ca, ':', s1, e, '\'', '"');
 			s2 = reverse(ca, s-1);
 		}
 	
 		return map;
 	}
 	
 	private Object toObject(char[] ca, int start, int end) {
 		Object object = getObject(ca, start, end);
 		if(converter != null) {
 			object = converter.convert(object);
 		}
 		if(stringsOnly) {
 			return String.valueOf(object);
 		}
 		return object;
 	}
 
 	public Object toObject(String json) {
 		if(json == null || json.length() == 0) {
 			return null;
 		}
 		return toObject(json.toCharArray(), 0, json.length());
 	}
 	
 }
