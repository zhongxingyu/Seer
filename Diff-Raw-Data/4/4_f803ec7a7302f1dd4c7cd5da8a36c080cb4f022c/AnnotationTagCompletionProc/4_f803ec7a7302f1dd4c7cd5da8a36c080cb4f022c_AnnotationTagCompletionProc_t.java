 /*******************************************************************************
  * Copyright (c) 2003, 2004 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 /*
  * Created on Mar 9, 2004
  *
  * To change the template for this generated file go to
  * Window - Preferences - Java - Code Generation - Code and Comments
  */
 package org.eclipse.jst.common.internal.annotations.ui;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.MissingResourceException;
 import java.util.Set;
 import java.util.TreeSet;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.IField;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.IMethod;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jdt.ui.JavaUI;
 import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
 import org.eclipse.jdt.ui.text.java.IJavadocCompletionProcessor;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.contentassist.IContextInformation;
 import org.eclipse.jst.common.internal.annotations.core.AnnotationTagParser;
 import org.eclipse.jst.common.internal.annotations.core.TagParseEventHandler;
 import org.eclipse.jst.common.internal.annotations.core.Token;
 import org.eclipse.jst.common.internal.annotations.registry.AnnotationTagRegistry;
 import org.eclipse.jst.common.internal.annotations.registry.AttributeValueProposalHelper;
 import org.eclipse.jst.common.internal.annotations.registry.AttributeValuesHelper;
 import org.eclipse.jst.common.internal.annotations.registry.TagAttribSpec;
 import org.eclipse.jst.common.internal.annotations.registry.TagSpec;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.part.FileEditorInput;
 
 
 /**
  * @author Pat Kelley
  * 
  * To change the template for this generated type comment go to Window - Preferences - Java - Code
  * Generation - Code and Comments
  */
 public class AnnotationTagCompletionProc implements IJavadocCompletionProcessor, TagParseEventHandler {
 	private static final String[] BOOLEAN_VALID_VALUES = new String[]{"false", "true"}; //$NON-NLS-1$ //$NON-NLS-2$
 	ICompilationUnit m_icu;
 
 	IDocument m_doc;
 
 	List m_tags;
 
 	// Instance variables active when maybeCompleteAttribute is live.
 	Token m_tagName;
 
 	/**
 	 * Set of all attributes names encountered. Only live when maybeCompleteAttribute is live.
 	 */
 	Set m_attSet = new TreeSet();
 
 	/**
 	 * List of Attribute. Only live when maybeCompleAttribute is live.
 	 */
 	List m_attributes = new ArrayList();
 
 	AnnotationTagParser m_parser = new AnnotationTagParser(this);
 
 	/**
 	 * Scope of the tag. TagSpec.TYPE | TagSpec.METHOD | TagSpec.FIELD. Not valid until
 	 * getAnnotationArea has been called for a completions request, and only then if
 	 * getAnnotationArea() did not return null.
 	 */
 	int m_tagScope;
 
 	public AnnotationTagCompletionProc() {
 		initTagInfo();
 	}
 
 	private void initTagInfo() {
 		if (m_tags == null)
 			m_tags = AnnotationTagRegistry.getAllTagSpecs();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jdt.ui.text.java.IJavadocCompletionProcessor#computeContextInformation(org.eclipse.jdt.core.ICompilationUnit,
 	 *      int)
 	 */
 	public IContextInformation[] computeContextInformation(ICompilationUnit cu, int offset) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jdt.ui.text.java.IJavadocCompletionProcessor#computeCompletionProposals(org.eclipse.jdt.core.ICompilationUnit,
 	 *      int, int, int)
 	 */
 	public IJavaCompletionProposal[] computeCompletionProposals(ICompilationUnit cu, int offset, int length, int flags) {
 		IEditorInput editorInput = new FileEditorInput((IFile) cu.getResource());
 
 		// Set up completion processor state.
 		m_doc = JavaUI.getDocumentProvider().getDocument(editorInput);
 		m_icu = cu;
 
 		try {
 			AnnotationArea area = getAnnotationArea(offset);
 
 			if (area == null) {
 				return null;
 			}
 
 			// Check for tag completion first. ( the easier case )
 			String tsf = getTagSoFarIfNotCompleted(area.beginOffset, offset);
 
 			if (tsf != null) {
 				return getTagCompletionsFor(tsf, area, length);
 			}
 
 			// Ach, have to try the harder case now, where we parse the
 			// annotation
 			return maybeCompleteAttribute(area, offset);
 
 		} catch (JavaModelException e) {
 			// Silently fail.
 			return null;
 		} catch (BadLocationException ex) {
 			return null;
 		}
 	}
 
 	private IJavaCompletionProposal[] maybeCompleteAttribute(AnnotationArea area, int cursorPos) throws BadLocationException {
 		m_attSet.clear();
 		m_attributes.clear();
 
 		m_parser.setParserInput(m_doc.get(area.beginOffset, area.length()));
 		m_parser.parse();
 
		TagSpec ts = null;
		if (m_tagName!=null)
			ts = getTagSpecForTagName(m_tagName.getText());
 
 		// Do we even recognize this tag?
 		if (ts == null) {
 			return null;
 		}
 
 		// Loop through and determine whether the cursor is within a attribute
 		// assignment, or between assignements.
 		Attribute target = null;
 		Attribute last = null;
 		Attribute before = null;
 		Attribute a = null;
 		boolean between = false;
 		int rCurPos = area.relativeCursorPos(cursorPos);
 		Iterator i = m_attributes.iterator();
 		while (i.hasNext()) {
 			a = (Attribute) i.next();
 
 			if (a.contains(rCurPos)) {
 				target = a;
 				break;
 			} else if (last != null) {
 				// See if the cursor is between, but not directly adjacent to
 				// the last two attributes.
 				if (rCurPos > last.maxExtent() + 1 && rCurPos < a.minExtent() - 1) {
 					between = true;
 					break;
 				} else if (a.immediatelyPrecedes(rCurPos)) {
 					before = a;
 					break;
 				}
 			}
 			last = a;
 		}
 
 		if (target == null) {
 			if (between) {
 				// If we're between attributes, suggest all possible attributes.
 				return attributeCompletionsFor(ts, cursorPos, 0, "", true); //$NON-NLS-1$
 			} else if (before != null) {
 				// We're right after the attribute named in 'before', so set the
 				// target to it, and fall
 				//  through to the target handling code.
 				target = before;
 			} else {
 				// not between and not immediately after an attribute. We are
 				// past the end of the parsed annotation.
 				//  Only offer suggestions if it looks like the last annotation
 				// attribute is valid.
 				if (a == null) {
 					// No annotations attributes, suggest everything.
 					return attributeCompletionsFor(ts, cursorPos, 0, "", true); //$NON-NLS-1$
 				} else if (rCurPos > a.maxExtent()) {
 					if (a.hasAssignment() && a.hasValue()) {
 						// Last annotation was good, and we're past it, so do
 						// completions for anything
 						return attributeCompletionsFor(ts, cursorPos, 0, "", true); //$NON-NLS-1$
 					} else if (a.hasAssignment())
 						return attributeValidValuesFor(ts, a, area, cursorPos);
 					else
 						return attributeCompletionsFor(ts, cursorPos - a.name.length(), 0, a.name.getText(), true);
 				} else {
 					// Didn't match anything, not past the end - we're probably
 					// the first attribute
 					// being added to the tag.
 					return attributeCompletionsFor(ts, cursorPos, 0, "", true); //$NON-NLS-1$
 				}
 			}
 		}
 
 		// Completion for a partial attribute name?
 		if (target.name.immediatelyPrecedes(rCurPos)) {
 			return attributeCompletionsFor(ts, area.relativeToAbs(target.name.getBeginning()), target.name.length(), target.name.getText(), !target.hasAssignment());
 		}
 
 		// Are we in the middle of a name?
 		if (target.name.contains(rCurPos)) {
 			// We've opted to replace the entire name for this case, which seems
 			// to make the most sense.
 			return attributeCompletionsFor(ts, area.relativeToAbs(target.name.getBeginning()), target.name.length(), target.name.getText().substring(0, rCurPos - target.name.getBeginning()), !target.hasAssignment());
 		}
 
 		// If we got this far, we're either in a value, or really confused.
 		// try and return valid values or bail?
 		if (a.value != null && (a.value.contains(rCurPos) || (target.hasAssignment() && area.relativeCursorPos(cursorPos) > a.name.getBeginning())))
 			return attributeValidValuesFor(ts, a, area, cursorPos);
 		return attributeCompletionsFor(ts, cursorPos, 0, "", true); //$NON-NLS-1$
 	}
 
 	/**
 	 * @return valid values for the attribute
 	 */
 	private IJavaCompletionProposal[] attributeValidValuesFor(TagSpec ts, Attribute a, AnnotationArea area, int cursorPos) {
 		TagAttribSpec tas = ts.attributeNamed(a.name.getText());
 		if (tas == null)
 			return null;
 		String[] validValues = getValidValues(tas, a, area);
 		String partialValue = calculatePartialValue(a, area, cursorPos);
 		int valueOffset = calculateValueOffset(a, area, cursorPos);
 		if (validValues == null || validValues.length == 0)
 			return createCustomAttributeCompletionProposals(ts, tas, partialValue, valueOffset, a.value.getText(), area.javaElement);
 		return createAttributeCompletionProposals(partialValue, valueOffset, validValues);
 	}
 
 	/**
 	 * @param ts
 	 * @param tas
 	 * @param partialValue
 	 * @param valueOffset
 	 * @param value
 	 * @param javaElement
 	 * @return
 	 */
 	private IJavaCompletionProposal[] createCustomAttributeCompletionProposals(TagSpec ts, TagAttribSpec tas, String partialValue, int valueOffset, String value, IJavaElement javaElement) {
 		AttributeValuesHelper helper = ts.getValidValuesHelper();
 		if (helper == null)
 			return null;
 		AttributeValueProposalHelper[] proposalHelpers = helper.getAttributeValueProposalHelpers(tas, partialValue, valueOffset, javaElement);
 		if (proposalHelpers == null || proposalHelpers.length == 0)
 			return null;
 		IJavaCompletionProposal[] proposals = new IJavaCompletionProposal[proposalHelpers.length];
 		AnnotationTagProposal proposal;
 		for (int i = 0; i < proposalHelpers.length; i++) {
 			proposal = new AnnotationTagProposal(proposalHelpers[i]);
 			//proposal.setPartialValueString(partialValue);
 			proposals[i] = proposal;
 		}
 		return proposals;
 	}
 
 	private IJavaCompletionProposal[] createAttributeCompletionProposals(String partialValue, int valueOffset, String[] validValues) {
 		List resultingValues = new ArrayList();
 		for (int i = 0; i < validValues.length; i++) {
 			String rplString = validValues[i];
 			if (partialValue != null && !rplString.startsWith(partialValue))
 				continue;
 			AnnotationTagProposal prop = new AnnotationTagProposal(rplString, valueOffset, 0, null, rplString, 1);
 			prop.setEnsureQuoted(true);
 			//prop.setPartialValueString(partialValue);
 			resultingValues.add(prop);
 		}
 		if (resultingValues.isEmpty())
 			return null;
 		return (IJavaCompletionProposal[]) resultingValues.toArray(new IJavaCompletionProposal[resultingValues.size()]);
 	}
 
 	private String[] getValidValues(TagAttribSpec tas, Attribute a, AnnotationArea area) {
 		String[] validValues = tas.getValidValues();
 		if (validValues == null || validValues.length == 0) {
 			AttributeValuesHelper helper = tas.getTagSpec().getValidValuesHelper();
 			if (helper == null)
 				return null;
 			validValues = helper.getValidValues(tas, area.javaElement);
 			if ((validValues == null || validValues.length == 0) && tas.valueIsBool())
 				validValues = BOOLEAN_VALID_VALUES;
 		}
 		return validValues;
 	}
 
 	/**
 	 * @param a
 	 * @param area
 	 * @param cursorPos
 	 * @return
 	 */
 	private int calculateValueOffset(Attribute a, AnnotationArea area, int cursorPos) {
 		if (a.value == null)
 			return cursorPos;
 		int nameEnd = a.name.getEnd();
 		int valBeg = a.value.getBeginning();
 		if (valBeg > nameEnd + 2)
 			return area.relativeToAbs(nameEnd + 2); //Value too far away to be correct.
 		return area.relativeToAbs(valBeg);
 	}
 
 	/**
 	 * @param a
 	 * @param area
 	 * @param cursorPos
 	 * @return
 	 */
 	private String calculatePartialValue(Attribute a, AnnotationArea area, int cursorPos) {
 		if (a.value == null)
 			return null;
 		int nameEnd = a.name.getEnd();
 		int valueBeg = a.value.getBeginning();
 		if (valueBeg > nameEnd + 2)
 			return null; //Value is too far away so it must not be part of this attribute.
 		int relativePos = area.relativeCursorPos(cursorPos);
 		if (a.value.contains(relativePos)) {
 			boolean hasBeginQuote = valueBeg - nameEnd == 2;
 			String value = a.value.getText();
 			int end = relativePos - valueBeg;
 			if (hasBeginQuote)
 				end--;
 			if (end > -1) {
 				int length = value.length();
 				if (end < length)
 					return value.substring(0, end);
 				else if (end == length)
 					return value;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * @param tagName
 	 * @return
 	 */
 	private TagSpec getTagSpecForTagName(String tagName) {
 		String simpleName = tagName;
 		if (tagName != null && tagName.length() > 0 && tagName.charAt(0) == '@')
 			simpleName = tagName.length() == 2 ? "" : tagName.substring(1); //$NON-NLS-1$
 		switch (m_tagScope) {
 			case TagSpec.TYPE :
 				return AnnotationTagRegistry.getTypeTag(simpleName);
 			case TagSpec.METHOD :
 				return AnnotationTagRegistry.getMethodTag(simpleName);
 			case TagSpec.FIELD :
 				return AnnotationTagRegistry.getFieldTag(simpleName);
 		}
 		return null;
 	}
 
 	private IJavaCompletionProposal[] attributeCompletionsFor(TagSpec ts, int replaceOffset, int replaceLength, String partialAttributeName, boolean appendEquals) {
 		Iterator i = ts.getAttributes().iterator();
 		List props = new ArrayList();
 		while (i.hasNext()) {
 			TagAttribSpec tas = (TagAttribSpec) i.next();
 			String aname = tas.getAttribName();
 
 			// Don't suggest attributes that have already been specified.
 			if (!m_attSet.contains(aname)) {
 				if (aname.startsWith(partialAttributeName)) {
 					String rtxt = appendEquals ? aname + '=' : aname;
 					AnnotationTagProposal prop = new AnnotationTagProposal(rtxt, replaceOffset, replaceLength, null, aname, 1);
 					prop.setHelpText(lookupAttHelp(tas));
 					props.add(prop);
 				}
 			}
 		}
 		if (props.isEmpty()) {
 			return null;
 		}
 		return (IJavaCompletionProposal[]) props.toArray(new IJavaCompletionProposal[props.size()]);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.ibm.ws.rd.annotations.TagParseEventHandler#annotationTag(com.ibm.ws.rd.annotations.Token)
 	 */
 	public void annotationTag(Token tag) {
 		m_tagName = tag;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.ibm.ws.rd.annotations.TagParseEventHandler#endOfTag(int)
 	 */
 	public void endOfTag(int pos) {
 		// Do nothing
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.ibm.ws.rd.annotations.TagParseEventHandler#attribute(com.ibm.ws.rd.annotations.Token,
 	 *      int, com.ibm.ws.rd.annotations.Token)
 	 */
 	public void attribute(Token name, int equalsPosition, Token value) {
 		m_attributes.add(new Attribute(name, equalsPosition, value));
 		m_attSet.add(name.getText());
 	}
 
 	private String getReplacementForTag(TagSpec ts, int beginIndex) {
 		StringBuffer bud = new StringBuffer(32);
 
 		bud.append('@');
 		bud.append(ts.getTagName());
 
 		String prefix = getArrayPrefixForMultipleAttribs(beginIndex);
 		List attributes = ts.getAttributes();
 
 		for (int i = 0; i < attributes.size(); i++) {
 			TagAttribSpec tas = (TagAttribSpec) attributes.get(i);
 
 			if (tas.isRequired()) {
 				bud.append(prefix);
 				bud.append(tas.getAttribName());
 				bud.append('=');
 			}
 		}
 		return bud.toString();
 	}
 
 	private String getArrayPrefixForMultipleAttribs(int beginIndex) {
 		String result = null;
 		String source = null;
 		// Get source from compilation unit
 		try {
 			source = m_icu.getSource();
 			if (source == null || beginIndex < 0)
 				return result;
 			// trim off everything after our begin index
 			source = source.substring(0, beginIndex + 1);
 			int newLineIndex = source.lastIndexOf('\n');
 			//if we are on first line...
 			if (newLineIndex == -1)
 				newLineIndex = 0;
 			// Get the current line
 			String currentLine = source.substring(newLineIndex, beginIndex + 1);
 			// Currently we have to have the '@' sign to show our menu
 			int annotationIndex = currentLine.lastIndexOf('@');
 			result = currentLine.substring(0, annotationIndex);
 			result = result + "  "; //$NON-NLS-1$
 		} catch (Exception e) {
 			// Do nothing
 		}
 
 		return result;
 	}
 
 	private IJavaCompletionProposal[] getTagCompletionsFor(String partialTagName, AnnotationArea area, int selectLength) {
 		List found = new ArrayList();
 
 		for (int i = 0; i < m_tags.size(); i++) {
 			TagSpec ts = (TagSpec) m_tags.get(i);
 			String tname = ts.getTagName();
 
 			if (ts.getScope() == m_tagScope && tname.startsWith(partialTagName)) {
 				String rtxt = getReplacementForTag(ts, area.beginOffset);
 				String labl = '@' + tname;
 				AnnotationTagProposal prop = new AnnotationTagProposal(rtxt, area.beginOffset, Math.max(selectLength, rtxt.length()), null, labl, 1);
 				prop.setHelpText(lookupTagHelp(ts));
 				found.add(prop);
 			}
 		}
 
 		if (!found.isEmpty()) {
 			return (IJavaCompletionProposal[]) found.toArray(new IJavaCompletionProposal[found.size()]);
 		}
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jdt.ui.text.java.IJavadocCompletionProcessor#getErrorMessage()
 	 */
 	public String getErrorMessage() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	private static boolean isWS1(char c) {
 		return c == ' ' || c == '\t' || c == '*' || c == '\r' || c == '\n';
 	}
 
 	private String getTagSoFarIfNotCompleted(int startingAt, int cursorAt) throws BadLocationException {
 		if (m_doc.getChar(startingAt) != '@') {
 			return null;
 		}
 
 		int firstChar = startingAt + 1;
 
 		if (firstChar == cursorAt) {
 			return ""; //$NON-NLS-1$
 		}
 
 		for (int i = firstChar; i < cursorAt; i++) {
 			char c = m_doc.getChar(i);
 
 			if (isWS1(c)) {
 				return null;
 			}
 		}
 
 		return m_doc.get(firstChar, cursorAt - firstChar);
 	}
 
 	/**
 	 * Calculates the the area of the annotation we're trying to complete. Also initializes
 	 * m_tagScope.
 	 * 
 	 * @param fromOffset
 	 * @return
 	 * @throws JavaModelException
 	 */
 	private AnnotationArea getAnnotationArea(int fromOffset) throws JavaModelException {
 		// First, roughly calculate the end of the comment.
 		IJavaElement el = m_icu.getElementAt(fromOffset);
 		int absmax, absmin;
 		if (el == null)
 			return null;
 		int ty = el.getElementType();
 
 		switch (ty) {
 			case IJavaElement.FIELD :
 				IField f = (IField) el;
 				absmax = f.getNameRange().getOffset();
 				absmin = f.getSourceRange().getOffset();
 				m_tagScope = TagSpec.FIELD;
 				break;
 
 			case IJavaElement.TYPE :
 				IType t = (IType) el;
 				absmax = t.getNameRange().getOffset();
 				absmin = t.getSourceRange().getOffset();
 				m_tagScope = TagSpec.TYPE;
 				break;
 
 			case IJavaElement.METHOD :
 				IMethod m = (IMethod) el;
 				absmax = m.getNameRange().getOffset();
 				absmin = m.getSourceRange().getOffset();
 				m_tagScope = TagSpec.METHOD;
 				break;
 
 			default :
 				m_tagScope = -1;
 				return null;
 		}
 
 		// Make sure we're not after the name for the member.
 		if (absmax < fromOffset) {
 			return null;
 		}
 
 		int min = 0, max = 0;
 		try {
 			// Search backwards for the starting '@'.
 			boolean found = false;
 			for (min = fromOffset; min >= absmin; min--) {
 				if (m_doc.getChar(min) == '@') {
 					found = true;
 					break;
 				}
 			}
 			if (!found) {
 				return null;
 			}
 
 			// Search forwards for the next '@', or the end of the comment.
 			for (max = fromOffset + 1; max < absmax; max++) {
 				if (m_doc.getChar(max) == '@') {
 					break;
 				}
 			}
 		} catch (BadLocationException e) {
 			return null;
 		}
 
 		return new AnnotationArea(el, min, Math.min(absmax, max));
 	}
 
 	private String lookupTagHelp(TagSpec ts) {
 		if (ts != null)
 			try {
 				return ts.lookupTagHelp();
 			} catch (MissingResourceException e) {
 				// Do nothing, return null
 			}
 		return null;
 	}
 
 	private String lookupAttHelp(TagAttribSpec tas) {
 		if (tas != null)
 			try {
 				return tas.lookupTagHelp();
 			} catch (MissingResourceException e) {
 				// Do nothing, return null
 			}
 		return null;
 	}
 
 	/**
 	 * A range that goes from the beginning position up to, but not including, the end position.
 	 */
 	private static class AnnotationArea {
 		/**
 		 * Document offset of the beginning of the javadoc annotation.
 		 */
 		int beginOffset;
 
 		/**
 		 * Document offset of the end of the area that could contain an annotation.
 		 */
 		int endOffset;
 		/**
 		 * The Java element that this annotation is assigned.
 		 * 
 		 * @param beg
 		 * @param end
 		 */
 		IJavaElement javaElement;
 
 		public AnnotationArea(IJavaElement javaElement, int beg, int end) {
 			this.javaElement = javaElement;
 			beginOffset = beg;
 			endOffset = end;
 		}
 
 		public boolean contains(int offset) {
 			return offset >= beginOffset && offset < endOffset;
 		}
 
 		public int length() {
 			return endOffset - beginOffset;
 		}
 
 		/**
 		 * Returns the cursor position relative to the area. Only valid if
 		 * <code>this.contains( absCursorPos )</code>
 		 * 
 		 * @param absCursorPos
 		 * @return
 		 */
 		public int relativeCursorPos(int absCursorPos) {
 			return absCursorPos - beginOffset;
 		}
 
 		public int relativeToAbs(int relPos) {
 			return beginOffset + relPos;
 		}
 	}
 
 	private static class Attribute {
 		Token name;
 
 		Token value;
 
 		int equalsPos;
 
 		Attribute(Token n, int ep, Token v) {
 			name = n;
 			value = v;
 			equalsPos = ep;
 		}
 
 		public boolean hasAssignment() {
 			return equalsPos != -1;
 		}
 
 		public boolean hasValue() {
 			return value.length() != 0;
 		}
 
 		public boolean contains(int srcPos) {
 			return srcPos >= minExtent() && srcPos <= maxExtent();
 		}
 
 		public int minExtent() {
 			return name.getBeginning();
 		}
 
 		public int maxExtent() {
 			if (hasAssignment()) {
 				if (hasValue())
 					return value.getEnd();
 				return equalsPos;
 			}
 			return name.getEnd();
 		}
 
 		public boolean immediatelyPrecedes(int pos) {
 			return maxExtent() + 1 == pos;
 		}
 	}
 
 }
