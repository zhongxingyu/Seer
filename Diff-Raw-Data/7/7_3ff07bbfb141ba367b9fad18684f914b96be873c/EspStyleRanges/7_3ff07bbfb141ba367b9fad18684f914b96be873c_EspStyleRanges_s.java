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
 package org.oobium.eclipse.esp.editor;
 
 import static org.oobium.build.esp.Constants.CSS_PROPERTIES;
 import static org.oobium.build.esp.Constants.DATA_BINDING;
 import static org.oobium.build.esp.Constants.DOM_EVENTS;
 import static org.oobium.build.esp.Constants.MARKUP_TAGS;
 import static org.oobium.build.esp.Constants.JAVA_KEYWORDS;
 import static org.oobium.build.esp.Constants.JS_KEYWORDS;
 import static org.oobium.build.esp.dom.EspPart.Type.Comment;
 
 import java.util.Arrays;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.StyleRange;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.graphics.Color;
 import org.oobium.build.esp.dom.EspDom;
 import org.oobium.build.esp.dom.EspElement;
 import org.oobium.build.esp.dom.EspPart;
 import org.oobium.build.esp.dom.EspPart.Type;
 import org.oobium.eclipse.esp.EspPlugin;
 
 public class EspStyleRanges {
 
 	private static final StyleRange level = new StyleRange(-1, -1, color(0, 0, 0), color(225, 225, 225));
 	private static final StyleRange markupTag = new StyleRange(-1, -1, color(0, 0, 128), null);
 	private static final StyleRange propertyName = new StyleRange(-1, -1, color(0, 64, 128), null);
 	private static final StyleRange javaKeyword = new StyleRange(-1, -1, color(128, 0, 86), null, SWT.BOLD);
 	private static final StyleRange javaString = new StyleRange(-1, -1, color(0, 0, 128), null);
 	private static final StyleRange operator = new StyleRange(-1, -1, color(128, 32, 32), null);
 	private static final StyleRange comment = new StyleRange(-1, -1, color(32, 128, 32), null);
 	private static final StyleRange javadoc = new StyleRange(-1, -1, color(63, 95, 191), null);
 	private static final StyleRange taskTag = new StyleRange(-1, -1, color(127, 159, 191), null, SWT.BOLD);
 	private static final StyleRange innerText = new StyleRange(-1, -1, color(128, 128, 128), null);
 
 	private static final char[][] TASK_TAGS = {
 		" TASK ".toCharArray(),
 		" TODO ".toCharArray(),
 		" XXX ".toCharArray()
 	};
 
 	private static Color color(int r, int g, int b) {
 		return EspPlugin.getDefault().getEspColorProvider().getColor(r, g, b);
 	}
 	
 	private EspDom dom;
 	private int styleCount;
 	private StyleRange[] styles;
 	private int[] ranges; // int[0] = offset; int[1] = length
 	
 	public EspStyleRanges(EspDom dom) {
 		this.dom = dom;
 		evaluate();
 	}
 
 	private int addRange(EspPart part, int offset, StyleRange style) {
 		EspPart next = part.getNextSubPart(offset);
 		int end = (next != null) ? next.getStart() : part.getEnd();
 		return addRange(offset, end-offset, style);
 	}
 	
 	private int addRange(int offset, int length, StyleRange style) {
 		if(styleCount >= styles.length) {
 			styles = Arrays.copyOf(styles, styles.length + 1000);
 			ranges = Arrays.copyOf(ranges, ranges.length + 2000);
 		}
 		ranges[styleCount*2] = offset;
 		ranges[styleCount*2+1] = length;
 		styles[styleCount] = style;
 		styleCount++;
 		return offset + length;
 	}
 	
 	public boolean applyRanges(StyledText widget) {
 		try {
 			if(ranges.length > 1 && (widget.getCharCount() >= (ranges[ranges.length-2] + ranges[ranges.length-1]))) {
 				widget.setStyleRanges(ranges, styles);
 				return true;
 			}
 		} catch(Exception e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 	
 	private void evaluate() {
 		styleCount = 0;
 		styles = new StyleRange[1000];
 		ranges = new int[2000];
 		
 		EspElement current = null;
 		int offset = 0;
 		while(offset < dom.getEnd()) {
 			char c = dom.charAt(offset);
 			if(c == '\t' && 
 					(current == null || !current.isA(Type.ScriptElement)) && 
 					(offset == 0 || dom.charAt(offset-1) == '\n')) {
 				int end = offset;
 				while(end < dom.getEnd()) {
 					if(dom.charAt(end) == '\t') end++;
 					else break;
 				}
 				if(dom.isScript()) {
 					offset = end;
 				} else {
 					offset = addRange(offset, end-offset, level);
 				}
 			} else if(c == '<' && offset < (dom.getEnd()-1) && dom.charAt(offset+1) == '-') {
 				offset = addRange(offset, 2, operator);
 			} else {
 				EspPart part = dom.getPart(offset);
 				if(part != null) {
 					offset = evaluate(part, offset);
 					continue;
 				}
 				offset++;
 			}
 		}
 		
 		styles = Arrays.copyOf(styles, styleCount);
 		ranges = Arrays.copyOf(ranges, styleCount*2);
 	}
 
 	private int evaluate(EspPart part, int offset) {
 		switch(part.getType()) {
 		case Comment:
 		case MarkupComment:     return evaluateComment(part, offset);
 		case JavaEscape:        return addRange(part, offset, operator);
 		case JavaKeyword:       return addRange(part, offset, javaKeyword);
 		case JavaContainer:     return addRange(part, offset, javaKeyword);
 		case JavaSource:        return evaluateJava(part, offset);
 		case JavaString:        return addRange(part, offset, javaString);
 		case MarkupTag:         return evaluateMarkupTag(part, offset);
 		case ScriptPart:        return evaluateScript(part, offset);
 		case StylePropertyName: return evaluateStylePropertyName(part, offset);
 		case StylePart:			return evaluateStyle(part, offset);
 		case VarName:			return evaluateVarName(part, offset);
 		case InnerTextPart:     return evaluateInnerText(part, offset);
 		default:                return offset + 1;
 		}
 	}
 	
 	private int evaluateComment(EspPart part, int offset) {
 		StyleRange style = isJavadoc(part) ? javadoc : comment;
 		int end = part.getEnd();
 		for(int i = offset; i < end; i++) {
 			for(int j = 0; j < TASK_TAGS.length; j++) {
 				if(part.startsWith(TASK_TAGS[j], i)) {
 					addRange(offset, i-offset, style);
 					i = addRange(i+1, TASK_TAGS[j].length-2, taskTag);
 					offset = i;
 					break;
 				}
 			}
 		}
 		if(offset < end) {
 			addRange(offset, end-offset, style);
 		}
 		return end+1;
 	}
 	
 	private int evaluateInnerText(EspPart part, int offset) {
 		return addRange(part, offset, innerText);
 	}
 	
 	private int evaluateJava(EspPart part, int offset) {
 		int end = part.getEnd();
 		for(int s1 = offset; s1 < end; s1++) {
 			if(s1 < end) {
 				int s2 = s1;
 				while(s2 < end && Character.isLetter(dom.charAt(s2))) {
 					s2++;
 				}
 				if(JAVA_KEYWORDS.contains(part.getDom().subSequence(s1, s2))) {
 					s1 = addRange(s1, s2-s1, javaKeyword);
 				} else {
 					s1 = s2;
 				}
 			}
 		}
 		return end;
 	}
 	
 	private int evaluateMarkupTag(EspPart part, int offset) {
 		String tag = part.getText();
 		if("import".equals(tag)) {
 			return addRange(part, offset, javaKeyword);
 		}
 		else if("yield".equals(tag)) {
 			return addRange(part, offset, javaKeyword);
 		}
 		else if("yieldTo".equals(tag)) {
 			return addRange(part, offset, javaKeyword);
 		}
 		else if(MARKUP_TAGS.containsKey(tag)) {
 			return addRange(part, offset, markupTag);
 		}
 		// TODO some kind of 'unknown tag' style... ?
 		return startOfNext(part, offset);
 	}
 
 	private int evaluateScript(EspPart part, int offset) {
 		EspPart next = part.getNextSubPart(offset);
 		int end = (next != null) ? next.getStart() : part.getEnd();
 		for(int s1 = offset; s1 < end; s1++) {
 			if( ! dom.isScript() && dom.charAt(s1) == '\n') {
 				int s2 = s1 + 1;
 				while(s2 < end && dom.charAt(s2) == '\t') {
 					s2++;
 				}
 				addRange(s1, s2-s1, level);
				s1 = s2;
 			}
 			if(s1 < end) {
 				int s2 = s1;
 				while(s2 < end && Character.isLetter(dom.charAt(s2))) {
 					s2++;
 				}
 				if(JS_KEYWORDS.contains(part.getDom().subSequence(s1, s2))) {
 					s1 = addRange(s1, s2-s1, javaKeyword);
				} else {
					s1 = s2;
 				}
 			}
 		}
 		return end;
 	}
 
 	private int evaluateStyle(EspPart part, int offset) {
 		EspPart next = part.getNextSubPart(offset);
 		int end = (next != null) ? next.getStart() : part.getEnd();
 		if(dom.isStyle()) {
 			return end;
 		}
 		for(int s1 = offset; s1 < end; s1++) {
 			if( ! dom.isStyle() && dom.charAt(s1) == '\n') {
 				int s2 = s1 + 1;
 				while(s2 < end && dom.charAt(s2) == '\t') {
 					s2++;
 				}
 				addRange(s1, s2-s1, level);
 				s1 = s2;
 			}
 			if(s1 < end) {
 				int s2 = s1;
 				while(s2 < end && Character.isLetter(dom.charAt(s2))) {
 					s2++;
 				}
 				s1 = addRange(s1, s2-s1, propertyName);
 			}
 		}
 		return end;
 	}
 
 	private int evaluateStylePropertyName(EspPart part, int offset) {
 		if(CSS_PROPERTIES.containsKey(part.getText())) {
 			return addRange(part, offset, propertyName);
 		}
 		return startOfNext(part, offset);
 	}
 	
 	private int evaluateVarName(EspPart part, int offset) {
 		String text = part.getText();
 		if(DATA_BINDING.contains(text) || DOM_EVENTS.contains(text)) {
 			return addRange(part, offset, propertyName);
 		}
 		return startOfNext(part, offset);
 	}
 	
 	private boolean isJavadoc(EspPart part) {
 		if(part.isA(Comment)) {
 			return part.startsWith("/**");
 		}
 		return false;
 	}
 	
 	private int startOfNext(EspPart part, int offset) {
 		EspPart next = part.getNextSubPart(offset);
 		return (next == null) ? part.getEnd() : next.getStart();
 	}
 	
 }
