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
 
 import static org.oobium.utils.CharStreamUtils.*;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.oobium.build.esp.EspElement;
 import org.oobium.build.esp.EspPart;
 
 public class JavaElement extends EspElement {
 
 	private EspPart source;
 	private List<EspElement> children;
 	
 	public JavaElement(EspPart parent, int start) {
 		super(parent, start);
 		type = Type.JavaElement;
 		parse();
 	}
 
 	private int createChild(int offset) {
 		int start = forward(ca, offset);
 		if(start == -1) {
 			return offset;
 		} else {
 			EspElement element;
 			if(ca[start] == '-') {
 				element = new JavaElement(this, offset);
 			} else if(InnerTextElement.isInnerText(ca, start)) {
 				element = new InnerTextElement(this, offset);
 			} else if(isNext(start, '/', '/')) {
 				element = new CommentElement(this, offset);
 			} else if(isNext(start, 's', 'c', 'r', 'i', 'p', 't')) {
 				element = new ScriptElement(this, offset);
 			} else if(isNext(start, 's', 't', 'y', 'l', 'e')) {
 				element = new StyleElement(this, offset);
 			} else if(Character.isLowerCase(ca[start])) {
 				element = new HtmlElement(this, offset);
 			} else {
 				return findEOL(ca, offset);
 			}
 			if(children == null) {
 				children = new ArrayList<EspElement>();
 			}
 			children.add(element);
 			return element.getEnd();
 		}
 	}
 	
 	private int findEnd(int offset) {
 		while(offset < ca.length) {
 			switch(ca[offset]) {
 			case '"':
 				offset = closer(ca, offset);
 				break;
 			case '\n':
 				return offset;
 			case '\r':
 				if(offset < ca.length-1 && ca[offset+1] == '\n') {
 					return offset;
 				}
 				break;
 			case '<':
 				if(offset < ca.length-1 && ca[offset+1] == '-') {
 					return offset;
 				}
 				break;
 			case '/':
 				if(offset < ca.length-1 && ca[offset+1] == '/') {
 					return offset;
 				}
 				break;
 			}
 			offset++;
 		}
 		return offset;
 	}
 	
 	public EspElement getChild(int index) {
 		return children.get(index);
 	}
 
 	public List<EspElement> getChildren() {
 		return children;
 	}
 
 	@Override
 	public String getElementText() {
 		if(parts != null) {
 			int end = start;
 			for(EspPart part : parts) {
 				if(part instanceof EspElement) {
 					break;
 				} else {
 					end = part.getEnd();
 				}
 			}
 			end = reverse(ca, end-1) + 1;
 			return new String(ca, start, end - start);
 		}
 		return getText();
 	}
 	
 	public String getSource() {
 		return (source != null) ? source.getText() : "";
 	}
 	
 	public EspPart getSourcePart() {
 		return source;
 	}
 	
 	public boolean hasChildren() {
 		return children != null;
 	}
 
 	private void parse() {
 		int s1 = start;
 		int eoe = findEnd(s1);
 		
 		int sourceStart = forward(ca, s1 + 1, eoe);
 		if(sourceStart == -1) {
 			sourceStart = s1 + 1;
 		}
 		int sourceEnd = reverse(ca, eoe-1) + 1;
 		if(sourceEnd > sourceStart) {
 			source = new EspPart(this, Type.JavaSourcePart, sourceStart, sourceEnd);
 		}
 		
 		s1 = eoe;
 		
 		if(eoe < ca.length && ca[eoe] == '/') { // a comment finishes the line
 			s1 = createChild(eoe);
 		} else if(eoe < ca.length && ca[eoe] == '<') { // in-line child
 			s1 = createChild(eoe + 2);
 		} else {
 			s1 = eoe;
 			while(s1 < ca.length) {
 				int level = 0;
 				while((s1+1+level) < ca.length && ca[s1+1+level] == '\t') {
 					level++;
 				}
 				if(this.level < level) {
 					s1 = createChild(s1+1);
 				} else {
 					break;
 				}
 			}
 		}
 		
 		end = (s1 < ca.length) ? s1 : ca.length;
 	}
 	
 }
