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
 package org.oobium.build.esp.elements;
 
 import static org.oobium.build.esp.elements.StyleElement.findEOL;
 import static org.oobium.build.esp.parts.EmbeddedJavaPart.skipEmbeddedJava;
 import static org.oobium.utils.CharStreamUtils.closer;
 import static org.oobium.utils.CharStreamUtils.find;
 import static org.oobium.utils.CharStreamUtils.forward;
 import static org.oobium.utils.CharStreamUtils.reverse;
 import static org.oobium.utils.StringUtils.camelCase;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.oobium.build.esp.EspElement;
 import org.oobium.build.esp.EspPart;
 import org.oobium.build.esp.parts.CommentPart;
 import org.oobium.build.esp.parts.JavaSourcePart;
 import org.oobium.build.esp.parts.StylePropertyValuePart;
 
 public class StyleChildElement extends MethodSignatureElement {
 
 	private boolean isParameterized; // used for both args and signatureArgs (in super)
 	private List<JavaSourcePart> args;
 	
 	private List<EspPart> selectorGroups;
 	private List<StyleChildElement> properties;
 	private List<EspElement> children;
 	
 	private EspPart name;
 	private StylePropertyValuePart value;
 	
 	private StyleChildElement(EspElement parent, int start, int end) {
 		super(parent, start);
 		if(end == -1) {
 			type = Type.StyleChildElement;
 			if(start > 0) { // start == 0 if 1st selector is the 1st char of an ESS file
 				level = 0;
 				for(int i = start; ca[i-1] != '\n'; i--) {
 					level++;
 				}
 			}
 		} else {
 			type = Type.StylePropertyPart;
 			this.end = end;
 		}
 		parse();
 	}
 
 	public StyleChildElement(StyleChildElement parent, int start) {
 		this((EspElement) parent, start, -1);
 	}
 	
 	public StyleChildElement(StyleChildElement parent, int start, int end) {
 		this((EspElement) parent, start, end);
 	}
 
 	public StyleChildElement(StyleElement parent, int start) {
 		this((EspElement) parent, start, -1);
 	}
 
 	protected void addArg(int start, int end) {
 		if(args == null) {
 			args = new ArrayList<JavaSourcePart>();
 		}
 		args.add(new JavaSourcePart(this, Type.JavaSourcePart, start, end));
 	}
 
 	private int addChildOrProperty(int start, int end) {
 		int s1 = forward(ca, start, end);
 		int s2 = reverse(ca, end-1) + 1;
 		if(s1 != -1 && s2 > s1) {
 			int ix = find(ca, ':', s1, s2);
 			if(ix != -1) {
 				if(ix == s1 || (ix+1 < ca.length && ix == s1+1 && ca[s1] == '&')) {
 					ix = -1; // it's a pseudo-class: treat as if ':' wasn't found
 				}
 			}
 			boolean isProperty = (ix != -1);
 			StyleChildElement child = new StyleChildElement(this, s1, isProperty ? s2 : -1);
 			if(isProperty || child.isMixin()) {
 				if(properties == null) {
 					properties = new ArrayList<StyleChildElement>();
 				}
 				properties.add(child);
 			} else {
 				if(children == null) {
 					children = new ArrayList<EspElement>();
 				}
 				children.add(child);
 				return child.getEnd();
 			}			
 		}
 		return end;
 	}
 
 	private int addChildrenAndProperties(int start) {
 		int s1 = start;
 		int s2 = start;
 		// static CSS files don't use levels
 		int eol = dom.isStatic() ? ca.length : findEOL(ca, start);
 		while(s2 < eol) {
 			s2 = skipEmbeddedJava(ca, s2, eol);
 			if(s2 < eol) {
 				if(ca[s2] == '}') {
 					return addChildOrProperty(s1, s2) + 1;
 				}
 				if(ca[s2] == ';') {
 					s1 = s2 = addChildOrProperty(s1, s2) + 1;
 				} else {
 					int s = commentCheck(this, s2);
 					if(s >= eol) {
 						return addChildOrProperty(s1, s2);
 					} else {
 						s2 = s + 1;
 					}
 				}
 			}
 		}
 		return addChildOrProperty(s1, s2);
 	}
 	
 	private void addSelectorGroup(int start, int end) {
 		start = forward(ca, start, end);
 		end = reverse(ca, end-1) + 1;
 		if(start != -1 && end > start) {
 			if(selectorGroups == null) {
 				selectorGroups = new ArrayList<EspPart>();
 			}
 			selectorGroups.add(new EspPart(this, Type.StyleSelectorPart, start, end));
 		}
 	}
 
 	
 	protected int commentCheck(EspPart parent, char[] ca, int ix) {
 		if(ix >= 0) {
 			if(ix < ca.length) {
 				if(ca[ix] == '"') {
 					ix = closer(ca, ix, ca.length, true, true);
 					if(ix == -1) {
 						ix = ca.length;
 					}
 				} else if(ca[ix] == '/' && (ca[ix+1] == '*' || ca[ix+1] == '/')) {
 					CommentPart comment = new CommentPart(parent, ix);
 					ix = comment.getEnd();
 				}
 			}
 		}
 		return ix;
 	}
 	
 	public List<JavaSourcePart> getArgs() {
 		return (args != null) ? args : new ArrayList<JavaSourcePart>(0);
 	}
 	
 	public List<EspElement> getChildren() {
 		return children;
 	}
 	
 	public EspElement getElement() {
 		return (EspElement) parent;
 	}
 	
 	public String getLastSelectorText() {
 		return selectorGroups.get(selectorGroups.size()-1).getText();
 	}
 	
 	@Override
 	public String getMethodName() {
 		String s = selectorGroups.get(0).getText().replace('-', '_');
 		s = ((s.charAt(0) == '.') ? "class" : "id") + camelCase(s.substring(1));
 		return s;
 	}
 
 	public EspPart getName() {
 		return name;
 	}
 	
 	public List<StyleChildElement> getProperties() {
 		return properties;
 	}
 	
 	@Override
 	public String getReturnType() {
 		return "String";
 	}
 	
 	public List<EspPart> getSelectorGroups() {
 		return selectorGroups;
 	}
 	
 	public StylePropertyValuePart getValue() {
 		return value;
 	}
 	
 	public boolean hasArgs() {
 		return args != null && !args.isEmpty();
 	}
 
 	public boolean hasChildren() {
 		return children != null;
 	}
 	
 	public boolean hasName() {
 		return name != null;
 	}
 	
 	public boolean hasProperties() {
 		return properties != null;
 	}
 	
 	public boolean hasSelectors() {
 		return selectorGroups != null;
 	}
 
 	public boolean hasValue() {
 		return value != null;
 	}
 	
 	public boolean isMixin() {
 		return (parent instanceof StyleChildElement)
 				&& !hasProperties() && !hasChildren()
 				&& selectorGroups != null && selectorGroups.size() == 1
 				&& (selectorGroups.get(0).charAt(0) == '.' || selectorGroups.get(0).charAt(0) == '#');
 	}
 	
 	public boolean isNestedChild() {
 		return (parent instanceof StyleChildElement) && (hasProperties() || hasChildren());
 	}
 	
 	public boolean isParameterized() {
 		return isParameterized;
 	}
 
 	public boolean isProperty() {
 		return name != null && value != null;
 	}
 
 	@Override
 	public boolean isStatic() {
 		return true;
 	}
 	
 	private void parse() {
 		if(type == Type.StylePropertyPart) {
 			parseAsProperty();
 		} else {
 			parseAsElement();
 		}
 	}
 	
 	private int parseArgs(int start, int end) {
 		int eoa = closer(ca, start, end);
 		if(eoa == -1) {
 			eoa = end;
 		}
 		
 		int s1 = forward(ca, start+1, eoa);
 		if(s1 != -1) {
 			for(int s = s1; s1 != -1 && s < eoa; s++) {
 				if(ca[s] == '"' || ca[s] == '{' || ca[s] == '(') {
 					int s2 = closer(ca, s, eoa, true);
 					if(s2 == -1) {
 						s2 = eoa;
 					}
 					s = s2;
 					if(s >= eoa-1) {
 						addArg(s1, reverse(ca, eoa-1) + 1);
 					}
 				} else if(ca[s] == ',') {
 					if(s != 0) {
 						addArg(s1, reverse(ca, s-1) + 1);
 						s1 = forward(ca, s + 1, eoa);
 					}
 				} else if(s == eoa-1) {
 					addArg(s1, reverse(ca, s) + 1);
 				}
 			}
 		}
 		return eoa + 1;
 	}
 
 	private void parseAsElement() {
 		int s1 = start;
 		int eol = findEOL(ca, s1);
 		
		boolean simple = true;
		
 		int s2 = s1;
 		while(s2 < eol) {
 			if(ca[s2] == '{') {
 				addSelectorGroup(s1, s2);
 				s1 = s2;
 				break;
 			}
 			if(ca[s2] == ',') {
 				addSelectorGroup(s1, s2);
 				s1 = forward(ca, s2+1, eol);
 				if(s1 == -1) {
 					s2 = eol;
 				} else {
 					s2 = s1;
 				}
				simple = true; // reset
 			}
			else if(simple && ca[s2] == '(') {
 				isParameterized = true;
 				addSelectorGroup(s1, s2);
 				if(parent instanceof StyleElement) {
 					s1 = parseSignatureArgs(ca, s2, eol);
 				} else {
 					s1 = parseArgs(s2, eol);
 				}
 				s1 = forward(ca, s1, eol);
 				if(s1 == -1) {
 					s2 = eol;
 				} else {
 					s2 = s1;
 				}
 			}
 			else {
 				s2 = commentCheck(this, s2);
 				if(s2 >= eol) {
 					s2 = eol;
 				} else {
 					s2++;
 				}
				simple = Character.isLetterOrDigit(ca[s2]) || ca[s2] == '_' || ca[s2] == '-';
 			}
 		}
 		
 		if(s1 != -1) {
 			if(ca[s1] == '{') {
 				if(ca[s2-1] == '$') {
 					s2 = closer(ca, s2, ca.length, true);
 					if(s2 == -1) {
 						s2 = ca.length;
 					}
 				} else {
 					s1 = addChildrenAndProperties(s1+1);
 					if(ca[s1-1] == '}') { // element has been completed
 						end = s1;
 						return;
 					}
 				}
 			} else if(s2 > s1) {
 				addSelectorGroup(s1, s2);
 			}
 		}
 		
 		s1 = eol;
 		if(dom.isStatic()) { // does not use levels...
 			while(s1 < ca.length) {
 				s1 = forward(ca, s1);
 				if(s1 == -1) {
 					end = ca.length;
 					return;
 				}
 				s1 = addChildrenAndProperties(s1+1);
 				if(ca[s1-1] == '}') { // element has been completed, ignore the rest of line
 					end = findEOL(ca, s1);
 					return;
 				}
 			}
 		} else {
 			while(s1 < ca.length) {
 				int level = 0;
 				while((s1+1+level) < ca.length && ca[s1+1+level] == '\t') {
 					level++;
 				}
 				if(this.level < level) {
 					s1 = addChildrenAndProperties(s1+1+level);
 				} else {
 					break;
 				}
 			}
 		}
 		
 		end = (s1 < ca.length) ? s1 : ca.length;
 	}
 
 	private void parseAsProperty() {
 		int s1 = start;
 		s1 = commentCheck(this, s1); // before comment
 		s1 = forward(ca, s1, end);
 		if(s1 != -1) {
 			for(int s = s1; s < end; s++) {
 				s = commentCheck(this, s);
 				if(s < end) {
 					if(ca[s] == ':') {
 						int s2 = reverse(ca, s-1) + 1;
 						if(s2 > s1) {
 							name = new EspPart(this, Type.StylePropertyNamePart, s1, s2);
 						}
 						s1 = forward(ca, s+1, end);
 						if(s1 != -1) {
 							s2 = reverse(ca, end-1) + 1;
 							if(s2 > s1) {
 								value = new StylePropertyValuePart(this, s1, s2);
 							}
 						}
 						return;
 					}
 				}
 			}
 			
 			// no ':' found
 			int s2 = s1;
 			while(s2 < end) {
 				if(Character.isWhitespace(ca[s2])) {
 					name = new EspPart(this, Type.StylePropertyNamePart, s1, s2);
 					break;
 				}
 				if(s2 == (end-1)) {
 					name = new EspPart(this, Type.StylePropertyNamePart, s1, s2+1);
 					break;
 				}
 				s2 = commentCheck(this, s2);
 				if(s2 >= end) {
 					name = new EspPart(this, Type.StylePropertyNamePart, s1, end);
 					break;
 				}
 				s2++;
 			}
 			
 			commentCheck(this, s2+1); // after comment
 		}
 	}
 	
 }
