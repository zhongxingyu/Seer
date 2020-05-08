 /*===========================================================================
   Copyright (C) 2008-2009 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ===========================================================================*/
 
 package net.sf.okapi.filters.html;
 
 import java.util.List;
 import java.util.logging.Logger;
 import java.util.regex.Pattern;
 
 import net.htmlparser.jericho.Attribute;
 import net.htmlparser.jericho.CharacterReference;
 import net.htmlparser.jericho.EndTag;
 import net.htmlparser.jericho.Segment;
 import net.htmlparser.jericho.StartTag;
 import net.htmlparser.jericho.Tag;
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.encoder.HtmlEncoder;
 import net.sf.okapi.common.filters.PropertyTextUnitPlaceholder;
 import net.sf.okapi.common.filters.PropertyTextUnitPlaceholder.PlaceholderType;
 import net.sf.okapi.common.resource.Property;
 import net.sf.okapi.common.resource.TextUnit;
 import net.sf.okapi.common.skeleton.GenericSkeleton;
 import net.sf.okapi.filters.markupfilter.BaseMarkupFilter;
 import net.sf.okapi.filters.yaml.TaggedFilterConfiguration.RULE_TYPE;
 
 public class HtmlFilter extends BaseMarkupFilter {
 
 	private static final Logger logger = Logger.getLogger("net.sf.okapi.filters.html");
 	
 	private StringBuilder bufferedWhitespace;
 
 	/* HTML whitespace
 	 * space (U+0020) 
 	 * tab (U+0009) 
 	 * form feed (U+000C) 
 	 * line feed (U+000A)
 	 * carriage return (U+000D) 
 	 * zero-width space (U+200B) (IE6 does not recognize these, they are treated as unprintable characters)
 	 */
 	private static final String HTML_WHITESPACE_REGEX = "[ \t\r\n\f\u200B]+";
 	private static final Pattern HTML_WHITESPACE_PATTERN = Pattern.compile(HTML_WHITESPACE_REGEX);
 
 	public HtmlFilter() {
 		super();
 		bufferedWhitespace = new StringBuilder();
 		setMimeType("text/html");
 		setDefaultConfig(HtmlFilter.class.getResource("defaultConfiguration.yml"));
 	}
 	
 	/**
 	 * Initialize parameters, rule state and parser. Called before processing of each input.
 	 */
 	@Override
	protected void initialize() {		
		super.initialize();
 		setPreserveWhitespace(false);
 	}
 
 	@Override
 	protected void handleCdataSection(Tag tag) {
 		addToDocumentPart(tag.toString());		
 	}
 
 	@Override
 	protected void preProcess(Segment segment) {
 		boolean isInsideTextRun = false;
 		if (segment instanceof Tag) {
 			isInsideTextRun = getConfig().getMainRuleType(((Tag)segment).getName()) == RULE_TYPE.INLINE_ELEMENT;
 		}
 		
 		// add buffered whitespace to the current translatable text
 		if (bufferedWhitespace.length() > 0 && isInsideTextRun) {
 			if (canStartNewTextUnit()) {
 				startTextUnit(bufferedWhitespace.toString());
 			} else {
 				addToTextUnit(bufferedWhitespace.toString());
 			}
 		} else if (bufferedWhitespace.length() > 0) {
 			// otherwise add it as non-translatable
 			addToDocumentPart(bufferedWhitespace.toString());			
 		}
 		// reset buffer for next pass
 		bufferedWhitespace.setLength(0);
 		bufferedWhitespace.trimToSize();		
 	}
 	
 	@Override
 	protected void handleText(Segment text) {
 		// if in excluded state everything is skeleton including text
 		if (getRuleState().isExludedState()) {
 			addToDocumentPart(text.toString());
 			return;
 		}
 
 		// check for ignorable whitespace and add it to the skeleton
 		// The Jericho html parser always pulls out the largest stretch of text
 		// so standalone whitespace should always be ignorable if we are not
 		// already processing inline text
 		if (text.isWhiteSpace() && !isInsideTextRun()) {
 			if (bufferedWhitespace.length() <= 0) {
 				// buffer the whitespace until we know that we are not inside translatable text.
 				bufferedWhitespace.append(text.toString());				
 			} 
 			return;
 		}
 
 		String decodedText = text.toString();	
 		decodedText = CharacterReference.decode(text.toString(), false);
 
 		// collapse whitespace only if config says we can and preserve
 		// whitespace is false
 		if (!getRuleState().isPreserveWhitespaceState() && getConfig().collapseWhitespace()) {
 			decodedText = collapseWhitespace(decodedText);
 		} else {
 			decodedText = Util.normalizeNewlines(decodedText);
 		}
 
 		if (canStartNewTextUnit()) {
 			startTextUnit(decodedText);
 		} else {
 			addToTextUnit(decodedText);
 		}
 	}
 	
 	@Override
 	protected void endTextUnit() {		
 		if (!getRuleState().isPreserveWhitespaceState() && getConfig().collapseWhitespace()) {			
 			Event e = peekMostRecentTextUnit();
 			TextUnit tu = (TextUnit)e.getResource();
 			tu.getSourceContent().trim();	
 		}			
 		
 		super.endTextUnit();		
 	}
 
 	private String collapseWhitespace(String text) {
 		return HTML_WHITESPACE_PATTERN.matcher(text).replaceAll(" ");
 	}
 
 	@Override
 	protected void handleDocumentPart(Tag tag) {
 		addToDocumentPart(tag.toString());
 	}
 
 	@Override
 	protected void handleStartTag(StartTag startTag) {
 		// if in excluded state everything is skeleton including text
 		if (getRuleState().isExludedState()) {
 			addToDocumentPart(startTag.toString());
 			// process these tag types to update parser state
 			switch (getConfig().getMainRuleType(startTag.getName())) {
 			case EXCLUDED_ELEMENT:
 				getRuleState().pushExcludedRule(startTag.getName());
 				break;
 			case INCLUDED_ELEMENT:
 				getRuleState().pushIncludedRule(startTag.getName());
 				break;
 			case PRESERVE_WHITESPACE:
 				getRuleState().pushPreserverWhitespaceRule(startTag.getName());
 				break;
 			}
 			return;
 		}
 
 		List<PropertyTextUnitPlaceholder> propertyTextUnitPlaceholders;
 		propertyTextUnitPlaceholders = createPropertyTextUnitPlaceholders(startTag);
 
 		switch (getConfig().getMainRuleType(startTag.getName())) {
 		case INLINE_ELEMENT:
 			if (canStartNewTextUnit()) {
 				startTextUnit();
 			}
 			addCodeToCurrentTextUnit(startTag);
 			break;
 		case ATTRIBUTES_ONLY:
 			// we assume we have already ended any (non-complex) TextUnit in
 			// the main while loop in BaseMarkupFilter
 			if (propertyTextUnitPlaceholders != null && !propertyTextUnitPlaceholders.isEmpty()) {
 				startDocumentPart(startTag.toString(), startTag.getName(), propertyTextUnitPlaceholders);
 				endDocumentPart();
 			} else {
 				// no attributes that need processing - just treat as skeleton
 				addToDocumentPart(startTag.toString());
 			}
 			break;
 		case GROUP_ELEMENT:
 			getRuleState().pushGroupRule(startTag.getName());
 			
 			// catch tags which are not listed in the config but have attributes that require processing
 			if (propertyTextUnitPlaceholders != null && !propertyTextUnitPlaceholders.isEmpty()) {
 				startGroup(new GenericSkeleton(startTag.toString()), startTag.getName(), propertyTextUnitPlaceholders);
 			} else {
 				// no attributes that need processing - just treat as skeleton
 				startGroup(new GenericSkeleton(startTag.toString()));
 			}
 			break;
 		case EXCLUDED_ELEMENT:
 			getRuleState().pushExcludedRule(startTag.getName());
 			
 			// catch tags which are not listed in the config but have attributes that require processing
 			if (propertyTextUnitPlaceholders != null && !propertyTextUnitPlaceholders.isEmpty()) {
 				startDocumentPart(startTag.toString(), startTag.getName(), propertyTextUnitPlaceholders);
 				endDocumentPart();
 			} else {
 				// no attributes that need processing - just treat as skeleton
 				addToDocumentPart(startTag.toString());
 			}
 			break;
 		case INCLUDED_ELEMENT:
 			getRuleState().pushIncludedRule(startTag.getName());
 			
 			if (propertyTextUnitPlaceholders != null && !propertyTextUnitPlaceholders.isEmpty()) {
 				startDocumentPart(startTag.toString(), startTag.getName(), propertyTextUnitPlaceholders);
 				endDocumentPart();
 			} else {
 				// no attributes that need processing - just treat as skeleton
 				addToDocumentPart(startTag.toString());
 			}
 
 			break;
 		case TEXT_UNIT_ELEMENT:
 			getRuleState().pushTextUnitRule(startTag.getName());
 			
 			propertyTextUnitPlaceholders = createPropertyTextUnitPlaceholders(startTag);
 			if (propertyTextUnitPlaceholders != null && !propertyTextUnitPlaceholders.isEmpty()) {
 				startTextUnit(new GenericSkeleton(startTag.toString()), propertyTextUnitPlaceholders);
 			} else {
 				startTextUnit(new GenericSkeleton(startTag.toString()));
 			}
 			break;
 		case PRESERVE_WHITESPACE:
 			getRuleState().pushPreserverWhitespaceRule(startTag.getName());
 			
 			setPreserveWhitespace(getRuleState().isPreserveWhitespaceState());
 			
 			if (propertyTextUnitPlaceholders != null && !propertyTextUnitPlaceholders.isEmpty()) {
 				startDocumentPart(startTag.toString(), startTag.getName(), propertyTextUnitPlaceholders);
 				endDocumentPart();
 			} else {
 				// no attributes that need processing - just treat as skeleton
 				addToDocumentPart(startTag.toString());
 			}
 			break;
 		default:
 			// catch tags which are not listed in the config but have attributes that require processing
 			if (propertyTextUnitPlaceholders != null && !propertyTextUnitPlaceholders.isEmpty()) {
 				startDocumentPart(startTag.toString(), startTag.getName(), propertyTextUnitPlaceholders);
 				endDocumentPart();
 			} else {
 				// no attributes that need processing - just treat as skeleton
 				addToDocumentPart(startTag.toString());
 			}
 		}
 	}
 
 	@Override
 	protected void handleEndTag(EndTag endTag) {
 		// if in excluded state everything is skeleton including text
 		if (getRuleState().isExludedState()) {
 			addToDocumentPart(endTag.toString());
 			// process these tag types to update parser state
 			switch (getConfig().getMainRuleType(endTag.getName())) {
 			case EXCLUDED_ELEMENT:
 				getRuleState().popExcludedIncludedRule();
 				break;
 			case INCLUDED_ELEMENT:
 				getRuleState().popExcludedIncludedRule();
 				break;
 			case PRESERVE_WHITESPACE:
 				getRuleState().popPreserverWhitespaceRule();
 				break;
 			}
 
 			return;
 		}
 
 		switch (getConfig().getMainRuleType(endTag.getName())) {
 		case INLINE_ELEMENT:
 			if (canStartNewTextUnit()) {
 				startTextUnit();
 			}
 			addCodeToCurrentTextUnit(endTag);
 			break;
 		case GROUP_ELEMENT:
 			getRuleState().popGroupRule();
 			endGroup(new GenericSkeleton(endTag.toString()));
 			break;
 		case EXCLUDED_ELEMENT:
 			getRuleState().popExcludedIncludedRule();
 			addToDocumentPart(endTag.toString());
 			break;
 		case INCLUDED_ELEMENT:
 			getRuleState().popExcludedIncludedRule();
 			addToDocumentPart(endTag.toString());
 			break;
 		case TEXT_UNIT_ELEMENT:
 			getRuleState().popTextUnitRule();
 			endTextUnit(new GenericSkeleton(endTag.toString()));
 			break;
 		case PRESERVE_WHITESPACE:
 			getRuleState().popPreserverWhitespaceRule();
 			setPreserveWhitespace(getRuleState().isPreserveWhitespaceState());
 			
 			addToDocumentPart(endTag.toString());
 			break;
 		default:
 			addToDocumentPart(endTag.toString());
 			break;
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @seenet.sf.okapi.common.markupfilter.BaseMarkupFilter#handleComment(net.
 	 * htmlparser.jericho.Tag)
 	 */
 	@Override
 	protected void handleComment(Tag tag) {
 		if (!isInsideTextRun()) {
 			handleDocumentPart(tag);
 		} else {
 			addCodeToCurrentTextUnit(tag);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * net.sf.okapi.common.markupfilter.BaseMarkupFilter#handleDocTypeDeclaration
 	 * (net.htmlparser.jericho.Tag)
 	 */
 	@Override
 	protected void handleDocTypeDeclaration(Tag tag) {
 		handleDocumentPart(tag);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * net.sf.okapi.common.markupfilter.BaseMarkupFilter#handleMarkupDeclaration
 	 * (net.htmlparser.jericho.Tag)
 	 */
 	@Override
 	protected void handleMarkupDeclaration(Tag tag) {
 		handleDocumentPart(tag);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * net.sf.okapi.common.markupfilter.BaseMarkupFilter#handleProcessingInstruction
 	 * (net.htmlparser.jericho.Tag)
 	 */
 	@Override
 	protected void handleProcessingInstruction(Tag tag) {
 		if (!isInsideTextRun()) {
 			handleDocumentPart(tag);
 		} else {
 			addCodeToCurrentTextUnit(tag);
 		}
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * net.sf.okapi.common.markupfilter.BaseMarkupFilter#handleServerCommon(
 	 * net.htmlparser.jericho.Tag)
 	 */
 	@Override
 	protected void handleServerCommon(Tag tag) {
 		handleDocumentPart(tag);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * net.sf.okapi.common.markupfilter.BaseMarkupFilter#handleServerCommonEscaped
 	 * (net.htmlparser.jericho.Tag)
 	 */
 	@Override
 	protected void handleServerCommonEscaped(Tag tag) {
 		handleDocumentPart(tag);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * net.sf.okapi.common.markupfilter.BaseMarkupFilter#handleXmlDeclaration
 	 * (net.htmlparser.jericho.Tag)
 	 */
 	@Override
 	protected void handleXmlDeclaration(Tag tag) {
 		handleDocumentPart(tag);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see net.sf.okapi.common.filters.IFilter#getName()
 	 */
 	public String getName() {
 		return "HTML Filter"; //$NON-NLS-1$
 	}
 
 	protected PropertyTextUnitPlaceholder createPropertyTextUnitPlaceholder(PlaceholderType type, String name,
 			String value, Tag tag, Attribute attribute) {
 
 		// Test for charset in meta tag - we need to isolate the position of
 		// charset within the attribute value
 		// i.e., content="text/html; charset=ISO-2022-JP"
 		if (isMetaCharset(name, value, tag) && value.indexOf("charset=") != -1) {
 			// offset of attribute
 			int mainStartPos = attribute.getBegin() - tag.getBegin();
 			int mainEndPos = attribute.getEnd() - tag.getBegin();
 
 			// adjust offset of value of the attribute
 			int charsetValueOffset = value.lastIndexOf("charset=") + "charset=".length();
 
 			int valueStartPos = (attribute.getValueSegment().getBegin() + charsetValueOffset) - tag.getBegin();
 			int valueEndPos = attribute.getValueSegment().getEnd() - tag.getBegin();
 			// get the charset value (encoding)
 			value = tag.toString().substring(valueStartPos, valueEndPos);
 			return new PropertyTextUnitPlaceholder(type, normalizeAttributeName(name, value, tag), value, mainStartPos,
 					mainEndPos, valueStartPos, valueEndPos);
 		}
 
 		// convert all entities to Unicode		
 		String decodedValue = CharacterReference.decode(value, true);
 		
 		if (getConfig().collapseWhitespace() && !getRuleState().isPreserveWhitespaceState()) {
 			decodedValue = collapseWhitespace(decodedValue);
 		} else {
 			decodedValue = Util.normalizeNewlines(decodedValue);
 		}
 		return super.createPropertyTextUnitPlaceholder(type, name, decodedValue, tag, attribute);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * net.sf.okapi.common.markupfilter.BaseMarkupFilter#normalizeName(java.
 	 * lang.String)
 	 */
 	@Override
 	protected String normalizeAttributeName(String attrName, String attrValue, Tag tag) {
 		// normalize values for HTML
 		String normalizedName = attrName;
 
 		// <meta http-equiv="Content-Type"
 		// content="text/html; charset=ISO-2022-JP">
 		if (isMetaCharset(attrName, attrValue, tag)) {
 			normalizedName = Property.ENCODING;
 			return normalizedName;
 		}
 
 		// <meta http-equiv="Content-Language" content="en"
 		if (tag.getName().equals("meta") && attrName.equals(HtmlEncoder.CONTENT)) {
 			StartTag st = (StartTag) tag;
 			if (st.getAttributeValue("http-equiv") != null) {
 				if (st.getAttributeValue("http-equiv").equals("Content-Language")) {
 					normalizedName = Property.LANGUAGE;
 					return normalizedName;
 				}
 			}
 		}
 
 		// <x lang="en"> or <x xml:lang="en">
 		if (attrName.equals("lang") || attrName.equals("xml:lang")) {
 			normalizedName = Property.LANGUAGE;
 		}
 
 		return normalizedName;
 	}
 
 	private boolean isMetaCharset(String attrName, String attrValue, Tag tag) {
 		if (tag.getName().equals("meta") && attrName.equals(HtmlEncoder.CONTENT)) {
 			StartTag st = (StartTag) tag;
 			if (st.getAttributeValue("http-equiv") != null && st.getAttributeValue("content") != null) {
 				if (st.getAttributeValue("http-equiv").equals("Content-Type")
 						&& st.getAttributeValue("content").contains("charset=")) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 }
