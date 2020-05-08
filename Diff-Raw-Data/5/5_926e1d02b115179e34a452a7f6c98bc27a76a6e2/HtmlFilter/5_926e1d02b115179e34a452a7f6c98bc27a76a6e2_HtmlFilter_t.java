 /*===========================================================================*/
 /* Copyright (C) 2008 Jim Hargrave                                           */
 /*---------------------------------------------------------------------------*/
 /* This library is free software; you can redistribute it and/or modify it   */
 /* under the terms of the GNU Lesser General Public License as published by  */
 /* the Free Software Foundation; either version 2.1 of the License, or (at   */
 /* your option) any later version.                                           */
 /*                                                                           */
 /* This library is distributed in the hope that it will be useful, but       */
 /* WITHOUT ANY WARRANTY; without even the implied warranty of                */
 /* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser   */
 /* General Public License for more details.                                  */
 /*                                                                           */
 /* You should have received a copy of the GNU Lesser General Public License  */
 /* along with this library; if not, write to the Free Software Foundation,   */
 /* Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA              */
 /*                                                                           */
 /* See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html */
 /*===========================================================================*/
 
 package net.sf.okapi.filters.html;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import net.htmlparser.jericho.Attribute;
 import net.htmlparser.jericho.EndTag;
 import net.htmlparser.jericho.EndTagType;
 import net.htmlparser.jericho.Segment;
 import net.htmlparser.jericho.Source;
 import net.htmlparser.jericho.StartTag;
 import net.htmlparser.jericho.StartTagType;
 import net.htmlparser.jericho.Tag;
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.filters.BaseFilter;
 import net.sf.okapi.common.filters.FilterEvent;
 import net.sf.okapi.common.groovy.GroovyConfigurationReader;
 import net.sf.okapi.common.groovy.GroovyFilterConfiguration;
 import net.sf.okapi.common.resource.Code;
 import net.sf.okapi.common.resource.TextFragment;
 import net.sf.okapi.common.skeleton.GenericSkeleton;
 
 public class HtmlFilter extends BaseFilter {
 	private Source htmlDocument;
 	private Iterator<Segment> nodeIterator;
 	private ExtractionRuleState ruleState;
 	private Parameters parameters;
 
 	public HtmlFilter() {
 		super();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see net.sf.okapi.common.filters.IFilter#getParameters()
 	 */
 	public IParameters getParameters() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public void setOptions(String language, String defaultEncoding, boolean generateSkeleton) {
 		setOptions(language, null, defaultEncoding, generateSkeleton);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * net.sf.okapi.common.filters.IFilter#setParameters(net.sf.okapi.common
 	 * .IParameters)
 	 */
 	public void setParameters(IParameters params) {
 		this.parameters = (Parameters) params;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see net.sf.okapi.common.filters.IFilter#setOptions(java.lang.String,
 	 * java.lang.String, java.lang.String, boolean)
 	 */
 	public void setOptions(String sourceLanguage, String targetLanguage, String defaultEncoding,
 			boolean generateSkeleton) {
 		// TODO: Implement generateSkeleton
 		setEncoding(defaultEncoding);
 		setSrcLang(sourceLanguage);
 	}
 
 	public void close() {
 
 	}
 
 	public void open(CharSequence input) {
 		htmlDocument = new Source(input);
 		initialize();
 	}
 
 	public void open(InputStream input) {
 		try {
 			if (getEncoding() != null) {
 				BufferedReader r = new BufferedReader(new InputStreamReader(input, getEncoding()));
 				htmlDocument = new Source(r);
 			} else {
 				// try to guess encoding
 				htmlDocument = new Source(input);
 			}
 		} catch (IOException e) {
 			// TODO Wrap unchecked exception
 			throw new RuntimeException(e);
 		}
 		initialize();
 	}
 
 	public void open(URL input) {
 		try {
 			if (getEncoding() != null) {
 				BufferedReader r = new BufferedReader(new InputStreamReader(input.openStream(), getEncoding()));
 				htmlDocument = new Source(r);
 			} else {
 				// try to guess encoding
 				htmlDocument = new Source(input);
 			}
 		} catch (IOException e) {
 			// TODO: Wrap unchecked exception
 			throw new RuntimeException(e);
 		}
 		initialize();
 	}
 	
 	@Override
 	public void initialize() {
 		super.initialize();
 
 		setMimeType("text/html"); //$NON-NLS-1$
 
 		if (parameters == null) {
 			parameters = new Parameters();
 			URL url = GroovyConfigurationReader.class.getResource("/net/sf/okapi/filters/html/defaultConfiguration.groovy");			 //$NON-NLS-1$
 			parameters.setGroovyConfig(new GroovyFilterConfiguration(url));
 		}
 
 		// Segment iterator
 		ruleState = new ExtractionRuleState();
 		htmlDocument.fullSequentialParse();
 		nodeIterator = htmlDocument.getNodeIterator();
 	}
 
 	@Override
 	public FilterEvent next() {
 		// reset state flags and buffers
 		ruleState.reset();
 
 		while (hasQueuedEvents()) {
 			return super.next();
 		}
 
 		while (nodeIterator.hasNext() && !isCanceled()) {
 			Segment segment = nodeIterator.next();
 
 			if (segment instanceof Tag) {
 				final Tag tag = (Tag) segment;
 
 				// We just hit a tag that could close the current TextUnit, but
 				// only if it was not opened with a tag (i.e., complex
 				// TextUnits)
 				if (isCurrentTextUnit() && !isCurrentComplexTextUnit()) {
 					endTextUnit();
 				}
 
 				if (tag.getTagType() == StartTagType.NORMAL || tag.getTagType() == StartTagType.UNREGISTERED) {
 					handleStartTag((StartTag) tag);
 				} else if (tag.getTagType() == EndTagType.NORMAL || tag.getTagType() == EndTagType.UNREGISTERED) {
 					handleEndTag((EndTag) tag);
 				} else if (tag.getTagType() == StartTagType.DOCTYPE_DECLARATION) {
 					handleSkeleton(tag);
 				} else if (tag.getTagType() == StartTagType.CDATA_SECTION) {
 					handleCdataSection(tag);
 				} else if (tag.getTagType() == StartTagType.COMMENT) {
 					handleSkeleton(tag);
 				} else if (tag.getTagType() == StartTagType.XML_DECLARATION) {
 					handleSkeleton(tag);
 				} else if (tag.getTagType() == StartTagType.XML_PROCESSING_INSTRUCTION) {
 					handleSkeleton(tag);
 				} else if (tag.getTagType() == StartTagType.MARKUP_DECLARATION) {
 					handleSkeleton(tag);
 				} else if (tag.getTagType() == StartTagType.SERVER_COMMON) {
 					// TODO: Handle server formats
 					handleSkeleton(tag);
 				} else if (tag.getTagType() == StartTagType.SERVER_COMMON_ESCAPED) {
 					// TODO: Handle server formats
 					handleSkeleton(tag);
 				} else { // not classified explicitly by Jericho
 					if (tag instanceof StartTag) {
 						handleStartTag((StartTag) tag);
 					} else if (tag instanceof EndTag) {
 						handleEndTag((EndTag) tag);
 					} else {
 						handleSkeleton(tag);
 					}
 				}
 			} else {
 				handleText(segment);
 			}
 
 			if (hasQueuedEvents()) {
 				break;
 			}
 		}
 
 		if (!nodeIterator.hasNext()) {
 			finalize(); // we are done
 		}
 
 		// return one of the waiting events
 		return super.next();
 	}
 
 	private void handleCdataSection(Tag tag) {
 		addToSkeleton(tag.toString());
 		// TODO: special handling for CDATA sections (may call sub-filters or
 		// unescape content etc.)
 	}
 
 	private void handleText(Segment text) {
 		// if in excluded state everything is skeleton including text
 		if (ruleState.isExludedState()) {
 			addToSkeleton(text.toString());
 			return;
 		}
 
 		// check for ignorable whitespace and add it to the skeleton
 		// The Jericho html parser always pulls out the largest stretch of text
 		// so standalone whitespace should always be ignorable if we are not
 		// already processing inline text
 		if (text.isWhiteSpace() && !isInsideTextRun()) {
 			addToSkeleton(text.toString());
 			return;
 		}
 
 		if (canStartNewTextUnit()) {
 			startTextUnit(text.toString());
 		} else {
 			addToTextUnit(text.toString());
 		}
 	}
 
 	private void handleSkeleton(Tag tag) {
 		addToSkeleton(tag.toString());
 	}
 
 	private void handleStartTag(StartTag startTag) {
 		// if in excluded state everything is skeleton including text
 		if (ruleState.isExludedState()) {
 			addToSkeleton(startTag.toString());
 			// process these tag types to update parser state
 			switch (parameters.getGroovyConfig().getMainRuleType(startTag.getName())) {
 			case EXCLUDED_ELEMENT:
 				ruleState.pushExcludedRule(startTag.getName());
 				break;
 			case INCLUDED_ELEMENT:
 				ruleState.pushIncludedRule(startTag.getName());
 				break;
 			case PRESERVE_WHITESPACE:
 				ruleState.pushPreserverWhitespaceRule(startTag.getName());
 				break;
 			}
 			return;
 		}
 
 		switch (parameters.getGroovyConfig().getMainRuleType(startTag.getName())) {
 		case INLINE_ELEMENT:
 			if (canStartNewTextUnit()) {
 				startTextUnit();
 			}
 			addCodeToCurrentTextUnit(startTag);
 			break;
 
 		case ATTRIBUTES_ONLY:
 			if (parameters.getGroovyConfig().hasActionableAttributes(startTag.getName())) {
 			}
 			break;
 		case GROUP_ELEMENT:
 			startGroup(new GenericSkeleton(startTag.toString()));
 			break;
 		case EXCLUDED_ELEMENT:
 			ruleState.pushExcludedRule(startTag.getName());
 			addToSkeleton(startTag.toString());
 			break;
 		case INCLUDED_ELEMENT:
 			ruleState.pushIncludedRule(startTag.getName());
 			addToSkeleton(startTag.toString());
 			break;
 		case TEXT_UNIT_ELEMENT:
 			startTextUnit(new GenericSkeleton(startTag.toString()));
 			break;
 		case PRESERVE_WHITESPACE:
 			ruleState.pushPreserverWhitespaceRule(startTag.getName());
 			addToSkeleton(startTag.toString());
 			break;
 		default:
 			addToSkeleton(startTag.toString());
 		}
 	}
 
 	private void handleEndTag(EndTag endTag) {
 		// if in excluded state everything is skeleton including text
 		if (ruleState.isExludedState()) {
 			addToSkeleton(endTag.toString());
 			// process these tag types to update parser state
 			switch (parameters.getGroovyConfig().getMainRuleType(endTag.getName())) {
 			case EXCLUDED_ELEMENT:
 				ruleState.popExcludedIncludedRule();
 				break;
 			case INCLUDED_ELEMENT:
 				ruleState.popExcludedIncludedRule();
 				break;
 			case PRESERVE_WHITESPACE:
 				ruleState.popPreserverWhitespaceRule();
 				break;
 			}
 
 			return;
 		}
 
 		switch (parameters.getGroovyConfig().getMainRuleType(endTag.getName())) {
 		case INLINE_ELEMENT:
 			if (canStartNewTextUnit()) {
 				startTextUnit();
 			}
 			addCodeToCurrentTextUnit(endTag);
 			break;
 		case GROUP_ELEMENT:
 			endGroup(new GenericSkeleton(endTag.toString()));
 			break;
 		case EXCLUDED_ELEMENT:
 			ruleState.popExcludedIncludedRule();
 			addToSkeleton(endTag.toString());
 			break;
 		case INCLUDED_ELEMENT:
 			ruleState.popExcludedIncludedRule();
 			addToSkeleton(endTag.toString());
 			break;
 		case TEXT_UNIT_ELEMENT:
 			endTextUnit(new GenericSkeleton(endTag.toString()));
 			break;
 		case PRESERVE_WHITESPACE:
 			ruleState.popPreserverWhitespaceRule();
 			addToSkeleton(endTag.toString());
 			break;
 		default:
 			addToSkeleton(endTag.toString());
 			break;
 		}
 	}
 
 	private void addAttribute(StartTag startTag) {
 		// convert Jericho attributes to HashMap
 		Map<String, String> attrs = new HashMap<String, String>();
 		attrs = startTag.getAttributes().populateMap(attrs, true);
 		for (Attribute attribute : startTag.getAttributes()) {
 			if (parameters.getGroovyConfig().isTranslatableAttribute(startTag.getName(), attribute.getName(), attrs)) {
 
 			} else if (parameters.getGroovyConfig().isLocalizableAttribute(startTag.getName(), attribute.getName(), attrs)) {
 
 			}
 		}
 	}
 
 	private void addCodeToCurrentTextUnit(Tag tag) {
 		TextFragment.TagType tagType;
 		if (tag.getTagType() == StartTagType.NORMAL || tag.getTagType() == StartTagType.UNREGISTERED) {
 			if (((StartTag) tag).isSyntacticalEmptyElementTag())
 				tagType = TextFragment.TagType.PLACEHOLDER;
 			else
 				tagType = TextFragment.TagType.OPENING;
 		} else if (tag.getTagType() == EndTagType.NORMAL || tag.getTagType() == EndTagType.UNREGISTERED) {
 			tagType = TextFragment.TagType.CLOSING;
 		} else {
 			tagType = TextFragment.TagType.PLACEHOLDER;
 		}
 		startCode(new Code(tagType, tag.getName(), tag.toString()));
 		endCode();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see net.sf.okapi.common.filters.IFilter#getName()
 	 */
 	public String getName() {
		return "HTMLFilter"; //$NON-NLS-1$
 	}
 }
