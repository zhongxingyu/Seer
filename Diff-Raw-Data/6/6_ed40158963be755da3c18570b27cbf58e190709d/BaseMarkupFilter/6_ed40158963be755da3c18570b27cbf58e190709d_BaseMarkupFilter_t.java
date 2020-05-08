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
 
 package net.sf.okapi.filters.markupfilter;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import net.htmlparser.jericho.Attribute;
 import net.htmlparser.jericho.Config;
 import net.htmlparser.jericho.EndTag;
 import net.htmlparser.jericho.EndTagType;
 import net.htmlparser.jericho.LoggerProvider;
 import net.htmlparser.jericho.Segment;
 import net.htmlparser.jericho.Source;
 import net.htmlparser.jericho.StartTag;
 import net.htmlparser.jericho.StartTagType;
 import net.htmlparser.jericho.Tag;
 
 import net.sf.okapi.common.BOMAwareInputStream;
 import net.sf.okapi.common.BOMNewlineEncodingDetector;
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.filters.BaseFilter;
 import net.sf.okapi.common.filters.PropertyTextUnitPlaceholder;
 import net.sf.okapi.common.filters.PropertyTextUnitPlaceholder.PlaceholderType;
 import net.sf.okapi.common.resource.Code;
 import net.sf.okapi.common.resource.TextFragment;
 import net.sf.okapi.common.resource.TextUnit;
 import net.sf.okapi.filters.yaml.TaggedFilterConfiguration;
 import net.sf.okapi.filters.yaml.TaggedFilterConfiguration.RULE_TYPE;
 
 public abstract class BaseMarkupFilter extends BaseFilter {
 	private static final Logger logger = Logger.getLogger("net.sf.okapi.filters.markupfilter");
 
 	private static final int PREVIEW_BYTE_COUNT = 1024;
 
 	private Source document;
 	private ExtractionRuleState ruleState;
 	private Parameters parameters;
 	private Iterator<Segment> nodeIterator;
 	private URL defaultConfig;
 	private BOMNewlineEncodingDetector bomEncodingDetector;
 	private boolean hasUtf8Bom;
 	private boolean hasUtf8Encoding;
 
 	static {
 		Config.ConvertNonBreakingSpaces = false;
 		Config.NewLine = BOMNewlineEncodingDetector.NewlineType.LF.toString();
 		Config.LoggerProvider = LoggerProvider.JAVA;
 	}
 
 	public BaseMarkupFilter() {
 		super();
 		hasUtf8Bom = false;
 		hasUtf8Encoding = false;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see net.sf.okapi.common.filters.IFilter#getParameters()
 	 */
 	public IParameters getParameters() {
 		if (parameters == null)
 			return new Parameters(defaultConfig);
 		return parameters;
 	}
 
 	public ExtractionRuleState getRuleState() {
 		return ruleState;
 	}
 
 	public void setRuleState(ExtractionRuleState ruleState) {
 		this.ruleState = ruleState;
 	}
 
 	public TaggedFilterConfiguration getConfig() {
 		return parameters.getTaggedConfig();
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
 
 	/**
 	 * Close the filter and all used resources.
 	 */
 	public void close() {
 		this.parameters = null;
 		this.document = null; // help Java GC
 	}
 
 	/*
 	 * Get PREVIEW_BYTE_COUNT bytes so we can sniff out any encoding information in 
 	 * XML or HTML files
 	 */
 	private Source getParsedHeader(final InputStream inputStream) {
 		try {
 			inputStream.mark(0);
 			final byte[] bytes = new byte[PREVIEW_BYTE_COUNT];
 			int i;
 			for (i = 0; i < PREVIEW_BYTE_COUNT; i++) {
 				final int nextByte = inputStream.read();
 				if (nextByte == -1)
 					break;
 				bytes[i] = (byte) nextByte;
 			}
 			Source parsedInput = new Source(new ByteArrayInputStream(bytes, 0,
 					i));
 			inputStream.reset();
 			return parsedInput;
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public void open(CharSequence input) {
 		setNewlineType(BOMNewlineEncodingDetector.getNewlineType(input)
 				.toString());
 		document = new Source(input);
 		initialize();
 	}
 
 	public void open(InputStream input) {
 		try {
 			bomEncodingDetector = new BOMNewlineEncodingDetector(input);
 			hasUtf8Bom = bomEncodingDetector.hasUtf8Bom();
 			hasUtf8Encoding = bomEncodingDetector.getEncoding().equals(
 					BOMNewlineEncodingDetector.UTF_8) ? true : false;
 			setNewlineType(bomEncodingDetector.getNewlineType().toString());
 
 			Source parsedHeader = getParsedHeader(input);
 			String detectedEncoding = parsedHeader
 					.getDocumentSpecifiedEncoding();
 
 			if (detectedEncoding == null && getEncoding() != null) {
 				detectedEncoding = getEncoding();
 				// TODO: do we warn that the detected encoding is different?
 			} else if (getEncoding() == null) {
 				detectedEncoding = parsedHeader.getEncoding(); // get best guess
 				// TODO: do we warn that the detected encoding is different?
 			}
 
 			BOMAwareInputStream bomis = new BOMAwareInputStream(input,
 					detectedEncoding);
 			bomis.detectEncoding(); // TODO: why do we need to call this?
 			document = new Source(
 					new InputStreamReader(bomis, detectedEncoding));
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 		initialize();
 	}
 
 	public void open(URI inputURI) {
 		try {
 			open(inputURI.toURL().openStream());
 		} catch (MalformedURLException e) {
 			throw new RuntimeException(e);
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	/**
 	 * Initialize parameters, rule state and parser.
 	 */
 	@Override
 	protected void initialize() {		
 		super.initialize();
 
 		if (parameters == null) {
 			parameters = new Parameters(defaultConfig);
 		}
 
 		// Segment iterator
 		ruleState = new ExtractionRuleState();
 				
 		// This code optimizes jericho parsing
 		//document.fullSequentialParse();
 		//nodeIterator = document.getNodeIterator();
 		
 		// This optimizes memory at the expense of performance 
 		nodeIterator = new NoCacheNodeIterator(document);
 	}
 
 	/**
 	 * Set the default config file as URL.
 	 * @param classPathToConfig
 	 */
 	protected void setDefaultConfig(URL classPathToConfig) {
 		this.defaultConfig = classPathToConfig;
 	}
 
 	/**
 	 * Initialize filter parameters from a URL.
 	 * @param config
 	 */
 	public void setParametersFromURL(URL config) {
 		parameters = new Parameters(config);
 	}
 
 	/**
 	 * Initialize filter parameters from a Java File.
 	 * @param config
 	 */
 	public void setParametersFromFile(File config) {
 		parameters = new Parameters(config);
 	}
 
 	/**
 	 * Initialize filter parameters from a String.
 	 * @param config
 	 */
 	public void setParametersFromString(String config) {
 		parameters = new Parameters(config);
 	}
 
 	/**
 	 * Queue up Jericho tokens until we can buld an Okapi {@link Event} and return it. 
 	 */
 	@Override
 	public Event next() {
 		// reset state flags and buffers
 		ruleState.reset();
 
 		while (hasQueuedEvents()) {
 			return super.next();
 		}
 
 		while (nodeIterator.hasNext() && !isCanceled()) {
 			Segment segment = nodeIterator.next();
 
 			preProcess(segment);
 
 			if (segment instanceof Tag) {
 				final Tag tag = (Tag) segment;
 
 				// set generic inline tag type
 				setTagType(getConfig().getElementType(tag));
 
 				// We just hit a tag that could close the current TextUnit, but
 				// only if it was not opened with a TextUnit tag (i.e., complex
 				// TextUnits such as <p> etc.)
 				boolean inlineTag = false;
 				if (isInsideTextRun()
 						&& (getConfig().getMainRuleType(tag.getName()) == RULE_TYPE.INLINE_ELEMENT
 						|| tag.getTagType() == StartTagType.COMMENT
 						|| tag.getTagType() == StartTagType.XML_PROCESSING_INSTRUCTION))
 					inlineTag = true;
 
 				if (isCurrentTextUnit() && !isCurrentComplexTextUnit()
 						&& !inlineTag) {
 					endTextUnit();
 				}
 
 				if (tag.getTagType() == StartTagType.NORMAL
 						|| tag.getTagType() == StartTagType.UNREGISTERED) {
 					handleStartTag((StartTag) tag);
 				} else if (tag.getTagType() == EndTagType.NORMAL
 						|| tag.getTagType() == EndTagType.UNREGISTERED) {
 					handleEndTag((EndTag) tag);
 				} else if (tag.getTagType() == StartTagType.DOCTYPE_DECLARATION) {
 					handleDocTypeDeclaration(tag);
 				} else if (tag.getTagType() == StartTagType.CDATA_SECTION) {
 					handleCdataSection(tag);
 				} else if (tag.getTagType() == StartTagType.COMMENT) {
 					handleComment(tag);
 				} else if (tag.getTagType() == StartTagType.XML_DECLARATION) {
 					handleXmlDeclaration(tag);
 				} else if (tag.getTagType() == StartTagType.XML_PROCESSING_INSTRUCTION) {
 					handleProcessingInstruction(tag);
 				} else if (tag.getTagType() == StartTagType.MARKUP_DECLARATION) {
 					handleMarkupDeclaration(tag);
 				} else if (tag.getTagType() == StartTagType.SERVER_COMMON) {
 					handleServerCommon(tag);
 				} else if (tag.getTagType() == StartTagType.SERVER_COMMON_ESCAPED) {
 					handleServerCommonEscaped(tag);
 				} else { // not classified explicitly by Jericho
 					if (tag instanceof StartTag) {
 						handleStartTag((StartTag) tag);
 					} else if (tag instanceof EndTag) {
 						handleEndTag((EndTag) tag);
 					} else {
 						handleDocumentPart(tag);
 					}
 				}
 
 				// unset current generic tag type (bold, underlined etc.)
 				setTagType(null);
 
 			} else {
 				handleText(segment);
 			}
 
 			if (hasQueuedEvents()) {
 				break;
 			}
 		}
 
 		if (!nodeIterator.hasNext()) {
 			super.finalize(); // we are done
 		}
 
 		// return one of the waiting events
 		return super.next();
 	}
 
 	/**
 	 * Do any handling needed before the current Segment is processed.
 	 * 
 	 * @param segment
 	 */
 	protected void preProcess(Segment segment) {};
 
 	/**
 	 * Handle any recognized escaped server tags.
 	 * @param tag
 	 */
 	protected abstract void handleServerCommonEscaped(Tag tag);
 
 	/**
 	 * Handle any recognized server tags (i.e., PHP, Mason etc.)
 	 * @param tag
 	 */
 	protected abstract void handleServerCommon(Tag tag);
 
 	/**
 	 * Handle an XML markup declaration.
 	 * @param tag
 	 */
 	protected abstract void handleMarkupDeclaration(Tag tag);
 
 	/**
 	 * Handle an XML declaration.
 	 * @param tag
 	 */
 	protected abstract void handleXmlDeclaration(Tag tag);
 
 	/**
 	 * Handle the XML doc type declaration (DTD).
 	 * @param tag
 	 */
 	protected abstract void handleDocTypeDeclaration(Tag tag);
 
 	/**
 	 * Handle processing instructions.
 	 * @param tag
 	 */
 	protected abstract void handleProcessingInstruction(Tag tag);
 
 	/**
 	 * Handle comments.
 	 * @param tag
 	 */
 	protected abstract void handleComment(Tag tag);
 
 	/**
 	 * Handle CDATA sections.
 	 * @param tag
 	 */
 	protected abstract void handleCdataSection(Tag tag);
 
 	/**
 	 * Handle all text (PCDATA).
 	 * @param text
 	 */
 	protected abstract void handleText(Segment text);
 
 	/**
 	 * Handle start tags.
 	 * @param startTag
 	 */
 	protected abstract void handleStartTag(StartTag startTag);
 
 	/**
 	 * Handle end tags, including empty tags.
 	 * @param endTag
 	 */
 	protected abstract void handleEndTag(EndTag endTag);
 
 	/**
 	 * Handle anything else not classified by Jericho.
 	 * @param tag
 	 */
 	protected abstract void handleDocumentPart(Tag tag);
 
 	/**
 	 * Some attributes names are converted to Okapi standards such as 
 	 * HTML charset to "encoding" and lang to "language" 
 	 * @param attrName
 	 * @param attrValue
 	 * @param tag
 	 * @return
 	 */
 	abstract protected String normalizeAttributeName(String attrName,
 			String attrValue, Tag tag);
 
 	/**
 	 * Add an {@link Code} to the current {@link TextUnit}. Throws an 
 	 * exception if there is no current {@link TextUnit}. 
 	 * @param tag
 	 */
 	protected void addCodeToCurrentTextUnit(Tag tag) {
 		List<PropertyTextUnitPlaceholder> propertyTextUnitPlaceholders;
 		String literalTag = tag.toString();
 		TextFragment.TagType codeType;
 
 		// start tag or empty tag
 		if (tag.getTagType() == StartTagType.NORMAL
 				|| tag.getTagType() == StartTagType.UNREGISTERED) {
 			StartTag startTag = ((StartTag) tag);
 
 			// is this an empty tag?
 			if (startTag.isSyntacticalEmptyElementTag())
 				codeType = TextFragment.TagType.PLACEHOLDER;
 			else
 				codeType = TextFragment.TagType.OPENING;
 
 			// create a list of Property or Text placeholders for this tag
 			// If this list is empty we know that there are no attributes that
 			// need special processing
 			propertyTextUnitPlaceholders = null;
 
 			propertyTextUnitPlaceholders = createPropertyTextUnitPlaceholders(startTag);
 			if (propertyTextUnitPlaceholders != null
 					&& !propertyTextUnitPlaceholders.isEmpty()) {
 				// add code and process actionable attributes
 				addToTextUnit(codeType, literalTag, startTag.getName(),
 						propertyTextUnitPlaceholders);
 			} else {
 				// no actionable attributes, just add the code as-is
 				addToTextUnit(codeType, literalTag, startTag.getName());
 			}
 		} else { // end or unknown tag
 			if (tag.getTagType() == EndTagType.NORMAL
 					|| tag.getTagType() == EndTagType.UNREGISTERED) {
 				codeType = TextFragment.TagType.CLOSING;
 			} else {
 				codeType = TextFragment.TagType.PLACEHOLDER;
 			}
 			addToTextUnit(codeType, literalTag, tag.getName());
 		}
 	}
 
 	/**
 	 * For the given Jericho {@link StartTag} parse out all the actionable attributes and
 	 * and store them as {@link PropertyTextUnitPlaceholder}. {@link PlaceholderType} are 
 	 * set based on the filter configuration for each attribute.
 	 * for the attribute name and value. 
 	 * @param startTag
 	 * @return
 	 */
 	protected List<PropertyTextUnitPlaceholder> createPropertyTextUnitPlaceholders(
 			StartTag startTag) {
 		// list to hold the properties or TextUnits
 		List<PropertyTextUnitPlaceholder> propertyOrTextUnitPlaceholders = new LinkedList<PropertyTextUnitPlaceholder>();
 
 		// convert Jericho attributes to HashMap
 		Map<String, String> attrs = startTag.getAttributes().populateMap(
 				new HashMap<String, String>(), true);
 		for (Attribute attribute : startTag.parseAttributes()) {
 			if (getConfig().isTranslatableAttribute(startTag.getName(),
 					attribute.getName(), attrs)) {
 				propertyOrTextUnitPlaceholders
 						.add(createPropertyTextUnitPlaceholder(
 								PlaceholderType.TRANSLATABLE, attribute
 										.getName(), attribute.getValue(),
 								startTag, attribute));
 			} else {
 
 				if (getConfig().isReadOnlyLocalizableAttribute(
 						startTag.getName(), attribute.getName(), attrs)) {
 					propertyOrTextUnitPlaceholders
 							.add(createPropertyTextUnitPlaceholder(
 									PlaceholderType.READ_ONLY_PROPERTY,
 									attribute.getName(), attribute.getValue(),
 									startTag, attribute));
 				} else if (getConfig().isWritableLocalizableAttribute(
 						startTag.getName(), attribute.getName(), attrs)) {
 					propertyOrTextUnitPlaceholders
 							.add(createPropertyTextUnitPlaceholder(
 									PlaceholderType.WRITABLE_PROPERTY,
 									attribute.getName(), attribute.getValue(),
 									startTag, attribute));
 				}
 			}
 		}
 
 		return propertyOrTextUnitPlaceholders;
 	}
 
 	/**
 	 * Create a {@link PropertyTextUnitPlaceholder} given the supplied type, 
 	 * name and Jericho {@link Tag} and {@link Attribute}. 
 	 * @param type
 	 * @param name
 	 * @param value
 	 * @param tag
 	 * @param attribute
 	 * @return
 	 */
 	protected PropertyTextUnitPlaceholder createPropertyTextUnitPlaceholder(
 			PlaceholderType type, String name, String value, Tag tag,
 			Attribute attribute) {
 		// offset of attribute
 		int mainStartPos = attribute.getBegin() - tag.getBegin();
 		int mainEndPos = attribute.getEnd() - tag.getBegin();
 
 		// offset of value of the attribute
 		int valueStartPos = attribute.getValueSegment().getBegin()
 				- tag.getBegin();
 		int valueEndPos = attribute.getValueSegment().getEnd() - tag.getBegin();
 
 		return new PropertyTextUnitPlaceholder(type, normalizeAttributeName(
 				name, value, tag), value, mainStartPos, mainEndPos,
 				valueStartPos, valueEndPos);
 	}
 
 	/**
 	 * Return true if the document is in utf8 encoding.
 	 */
 	protected boolean hasUtf8Encoding() {
 		return hasUtf8Encoding;
 	}
 
 	/**
 	 * Return true if the document has a utf-8 byte order mark.
 	 */
 	protected boolean hasUtf8Bom() {
 		return hasUtf8Bom;
 	}
 
 	/**
 	 * Return true if the current filter configuration tells us to 
 	 * preserve whitespace as-is.
 	 * @return
 	 */
 	protected boolean keepOriginalFormatting() {
 		if (getRuleState().isPreserveWhitespaceState()
 				&& !getConfig().collapseWhitespace()) {
 			return true;
 		}
 		return false;
 	}
 }
