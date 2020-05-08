 /*===========================================================================
   Copyright (C) 2008-2010 by the Okapi Framework contributors
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
 
 package net.sf.okapi.filters.abstractmarkup;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Pattern;
 
 import net.htmlparser.jericho.Attribute;
 import net.htmlparser.jericho.CharacterEntityReference;
 import net.htmlparser.jericho.CharacterReference;
 import net.htmlparser.jericho.Config;
 import net.htmlparser.jericho.EndTag;
 import net.htmlparser.jericho.EndTagType;
 import net.htmlparser.jericho.LoggerProvider;
 import net.htmlparser.jericho.NumericCharacterReference;
 import net.htmlparser.jericho.Segment;
 import net.htmlparser.jericho.Source;
 import net.htmlparser.jericho.StartTag;
 import net.htmlparser.jericho.StartTagType;
 import net.htmlparser.jericho.StreamedSource;
 import net.htmlparser.jericho.Tag;
 
 import net.sf.okapi.common.BOMNewlineEncodingDetector;
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.exceptions.OkapiBadFilterInputException;
 import net.sf.okapi.common.exceptions.OkapiIOException;
 import net.sf.okapi.common.filters.AbstractFilter;
 import net.sf.okapi.common.filters.EventBuilder;
 import net.sf.okapi.common.filters.IFilter;
 import net.sf.okapi.common.filters.PropertyTextUnitPlaceholder;
 import net.sf.okapi.common.filters.PropertyTextUnitPlaceholder.PlaceholderAccessType;
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.resource.Code;
 import net.sf.okapi.common.resource.Ending;
 import net.sf.okapi.common.resource.RawDocument;
 import net.sf.okapi.common.resource.StartDocument;
 import net.sf.okapi.common.resource.TextFragment;
 import net.sf.okapi.common.resource.TextUnit;
 import net.sf.okapi.common.skeleton.GenericSkeleton;
 import net.sf.okapi.filters.abstractmarkup.ExtractionRuleState.RuleType;
 import net.sf.okapi.filters.yaml.TaggedFilterConfiguration;
 import net.sf.okapi.filters.yaml.TaggedFilterConfiguration.RULE_TYPE;
 
 /**
  * Abstract class useful for creating an {@link IFilter} around the Jericho parser. Jericho can parse non-wellformed
  * HTML, XHTML, XML and various server side scripting languages such as PHP, Mason, Perl (all configurable from
  * Jericho). AbstractMarkupFilter takes care of the parser initialization and provides default handlers for each token
  * type returned by the parser.
  * <p>
  * Handling of translatable text, inline tags, translatable and read-only attributes are configurable through a user
  * defined YAML file. See the Okapi HtmlFilter with defaultConfiguration.yml and OpenXml filters for examples.
  * 
  */
 public abstract class AbstractMarkupFilter extends AbstractFilter {
 	private static final Logger LOGGER = Logger.getLogger(AbstractMarkupFilter.class.getName());
 	private static final String CDATA_START_REGEX = "<\\!\\[CDATA\\[";
 	private static final String CDATA_END_REGEX = "\\]\\]>";
 	private static final Pattern CDATA_START_PATTERN = Pattern.compile(CDATA_START_REGEX);
 	private static final Pattern CDATA_END_PATTERN = Pattern.compile(CDATA_END_REGEX);
 	private static final int PREVIEW_BYTE_COUNT = 1024;
 
 	private StringBuilder bufferedWhitespace;
 	private StreamedSource document;
 	private Iterator<Segment> nodeIterator;
 	private boolean hasUtf8Bom;
 	private boolean hasUtf8Encoding;
 	private EventBuilder eventBuilder;
 	private RawDocument currentRawDocument;
 	private ExtractionRuleState ruleState;
 	//private String rootId;
 	private AbstractFilter cdataSubfilter;
 	
 	static {
 		Config.ConvertNonBreakingSpaces = false;
 		Config.NewLine = BOMNewlineEncodingDetector.NewlineType.LF.toString();
 		Config.LoggerProvider = LoggerProvider.JAVA;
 	}
 
 	/**
 	 * Default constructor for {@link AbstractMarkupFilter} using default {@link EventBuilder}
 	 */
 	public AbstractMarkupFilter() {
 		this.bufferedWhitespace = new StringBuilder();
 		this.hasUtf8Bom = false;
 		this.hasUtf8Encoding = false;
 	}
 
 	/**
 	 * Default constructor for {@link AbstractMarkupFilter} using default {@link EventBuilder}
 	 */
 	public AbstractMarkupFilter(EventBuilder eventBuilder) {
 		this.eventBuilder = eventBuilder;
 		this.bufferedWhitespace = new StringBuilder();
 		this.hasUtf8Bom = false;
 		this.hasUtf8Encoding = false;
 	}
 
 	/**
 	 * Get the current {@link TaggedFilterConfiguration}. A TaggedFilterConfiguration is the result of reading in a YAML
 	 * configuration file and converting it into Java Objects.
 	 * 
 	 * @return a {@link TaggedFilterConfiguration}
 	 */
 	abstract protected TaggedFilterConfiguration getConfig();
 
 	/**
 	 * Close the filter and all used resources.
 	 */
 	public void close() {	
 		super.close();
 		
 		if (ruleState != null) {
 			ruleState.reset(!getConfig().isGlobalPreserveWhitespace());
 		}
 
 		if (currentRawDocument != null) {
 			currentRawDocument.close();
 		}
 
 		try {
 			if (document != null) {
 				document.close();
 			}
 		} catch (IOException e) {
 			throw new OkapiIOException("Could not close " + getDocumentName(), e);
 		}
 		this.document = null; // help Java GC
 		LOGGER.log(Level.FINE, getDocumentName() + " has been closed");
 	}
 
 	/*
 	 * Get PREVIEW_BYTE_COUNT bytes so we can sniff out any encoding information in XML or HTML files
 	 */
 	private Source getParsedHeader(final InputStream inputStream) {
 		try {
 			final byte[] bytes = new byte[PREVIEW_BYTE_COUNT];
 			int i;
 			for (i = 0; i < PREVIEW_BYTE_COUNT; i++) {
 				final int nextByte = inputStream.read();
 				if (nextByte == -1)
 					break;
 				bytes[i] = (byte) nextByte;
 			}
 			Source parsedInput = new Source(new ByteArrayInputStream(bytes, 0, i));
 			return parsedInput;
 		} catch (IOException e) {
 			throw new OkapiIOException("Could not reset the input stream to it's start position", e);
 		} finally {
 			try {
 				inputStream.reset();
 			} catch (IOException e) {
 
 			}
 		}
 	}
 
 	/**
 	 * Start a new {@link IFilter} using the supplied {@link RawDocument}.
 	 * 
 	 * @param input
 	 *            - input to the {@link IFilter} (can be a {@link CharSequence}, {@link URI} or {@link InputStream})
 	 */
 	public void open(RawDocument input) {
 		open(input, true);
 		LOGGER.log(Level.FINE, getName() + " has opened an input document");
 	}
 
 	/**
 	 * Use this open when the rootId must be different from the document name. Used mostly when filter is called as a
 	 * sub-filters.
 	 * 
 	 * @param input
 	 *            - input to the {@link IFilter} (can be a {@link CharSequence}, {@link URI} or {@link InputStream})
 	 * @param generateSkeleton
 	 *            - true if the {@link IFilter} should store non-translatble blocks (aka skeleton), false otherwise.
 	 * @param rootId
 	 *            - id root used to give resources a unique id
 	 *
 	public void open(RawDocument input, boolean generateSkeleton, String rootId) {		
 		open(input, generateSkeleton);
 		this.rootId = rootId;
 	}*/
 
 	/**
 	 * Start a new {@link IFilter} using the supplied {@link RawDocument}.
 	 * 
 	 * @param input
 	 *            - input to the {@link IFilter} (can be a {@link CharSequence}, {@link URI} or {@link InputStream})
 	 * @param generateSkeleton
 	 *            - true if the {@link IFilter} should store non-translatble blocks (aka skeleton), false otherwise.
 	 * 
 	 * @throws OkapiBadFilterInputException
 	 * @throws OkapiIOException
 	 */
 	public void open(RawDocument input, boolean generateSkeleton) {
 		// close RawDocument from previous run
 		close();
 
 		currentRawDocument = input;
 
 		if (input.getInputURI() != null) {
 			setDocumentName(input.getInputURI().getPath());
 		}
 
 		BOMNewlineEncodingDetector detector = new BOMNewlineEncodingDetector(input.getStream(),
 				input.getEncoding());
 		detector.detectBom();
 
 		setEncoding(detector.getEncoding());
 		hasUtf8Bom = detector.hasUtf8Bom();
 		hasUtf8Encoding = detector.hasUtf8Encoding();
 		setNewlineType(detector.getNewlineType().toString());
 
 		Source parsedHeader = getParsedHeader(input.getStream());
 		String detectedEncoding = parsedHeader.getDocumentSpecifiedEncoding();
 
 		if (detectedEncoding == null && getEncoding() != null) {
 			detectedEncoding = getEncoding();
 			LOGGER.log(Level.FINE, String.format(
 					"Cannot auto-detect encoding. Using the default encoding (%s)", getEncoding()));
 		} else if (getEncoding() == null) {
 			detectedEncoding = parsedHeader.getEncoding(); // get best guess
 			LOGGER.log(
 					Level.FINE,
 					String.format(
 							"Default encoding and detected encoding not found. Using best guess encoding (%s)",
 							detectedEncoding));
 		}
 
 		try {
 			input.setEncoding(detectedEncoding);
 			setOptions(input.getSourceLocale(), input.getTargetLocale(), detectedEncoding,
 					generateSkeleton);
 			document = new StreamedSource(input.getReader());
 		} catch (IOException e) {
 			throw new OkapiIOException("Filter could not open input stream", e);
 		}
 
 		startFilter();
 	}
 
 	public boolean hasNext() {
 		return eventBuilder.hasNext();
 	}
 
 	/**
 	 * Queue up Jericho tokens until we can build an Okapi {@link Event} and return it.
 	 */
 	public Event next() {
 		while (eventBuilder.hasQueuedEvents()) {
 			return eventBuilder.next();
 		}
 
 		while (nodeIterator.hasNext() && !isCanceled()) {
 			Segment segment = nodeIterator.next();
 
 			preProcess(segment);
 
 			if (segment instanceof Tag) {
 				final Tag tag = (Tag) segment;
 
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
 			} else if (segment instanceof CharacterEntityReference) {
 				handleCharacterEntity(segment);
 			} else if (segment instanceof NumericCharacterReference) {
 				handleNumericEntity(segment);
 			} else {
 				// last resort is pure text node
 				handleText(segment);
 			}
 
 			if (eventBuilder.hasQueuedEvents()) {
 				break;
 			}
 		}
 
 		if (!nodeIterator.hasNext()) {
 			endFilter(); // we are done
 		}
 
 		// return one of the waiting events
 		return eventBuilder.next();
 	}
 
 	/**
 	 * Initialize the filter for every input and send the {@link StartDocument} {@link Event}
 	 */
 	protected void startFilter() {
 		// order of execution matters
 		if (eventBuilder == null) {
 			eventBuilder = new AbstractMarkupEventBuilder(getRootId(), isSubFilter());
 			eventBuilder.setMimeType(getMimeType());
 		} else {
 			eventBuilder.reset(getRootId(), isSubFilter());
 		}		
 
 		eventBuilder.addFilterEvent(createStartFilterEvent());		
 
 		// default is to preserve whitespace
 		boolean preserveWhitespace = true;
 		if (getConfig() != null) {
 			preserveWhitespace = getConfig().isGlobalPreserveWhitespace();
 		}
 		ruleState = new ExtractionRuleState(preserveWhitespace);
 		setPreserveWhitespace(ruleState.isPreserveWhitespaceState());
 
 		// This optimizes memory at the expense of performance
 		nodeIterator = document.iterator();
 
 		// initialize sub-filter
		TaggedFilterConfiguration figgy=getConfig(); // DWH to fix crash in OpenXML
		if (figgy!=null && figgy.getGlobalCDATASubfilter() != null) {
//	if (getConfig().getGlobalCDATASubfilter() != null) {
 			cdataSubfilter = (AbstractFilter)getFilterConfigurationMapper().createFilter(
 					getConfig().getGlobalCDATASubfilter(), cdataSubfilter);
 			getEncoderManager().mergeMappings(cdataSubfilter.getEncoderManager());
 		}
 	}
 
 	/**
 	 * End the current filter processing and send the {@link Ending} {@link Event}
 	 */
 	protected void endFilter() {
 		eventBuilder.flushRemainingEvents();	
 		eventBuilder.addFilterEvent(createEndFilterEvent());
 	}
 
 	/**
 	 * Do any handling needed before the current Segment is processed. Default is to do nothing.
 	 * 
 	 * @param segment
 	 */
 	protected void preProcess(Segment segment) {
 		boolean isInsideTextRun = false;
 		if (segment instanceof Tag) {
 			isInsideTextRun = getConfig().getElementRuleType(((Tag) segment).getName()) == RULE_TYPE.INLINE_ELEMENT;
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
 
 	/**
 	 * Do any required post-processing on the TextUnit before the {@link Event} leaves the {@link IFilter}. Default
 	 * implementation leaves Event unchanged. Override this method if you need to do format specific handing such as
 	 * collapsing whitespace.
 	 */
 	protected void postProcessTextUnit(TextUnit textUnit) {
 	}
 
 	/**
 	 * Handle any recognized escaped server tags.
 	 * 
 	 * @param tag
 	 */
 	protected void handleServerCommonEscaped(Tag tag) {
 		handleDocumentPart(tag);
 	}
 
 	/**
 	 * Handle any recognized server tags (i.e., PHP, Mason etc.)
 	 * 
 	 * @param tag
 	 */
 	protected void handleServerCommon(Tag tag) {
 		handleDocumentPart(tag);
 	}
 
 	/**
 	 * Handle an XML markup declaration.
 	 * 
 	 * @param tag
 	 */
 	protected void handleMarkupDeclaration(Tag tag) {
 		handleDocumentPart(tag);
 	}
 
 	/**
 	 * Handle an XML declaration.
 	 * 
 	 * @param tag
 	 */
 	protected void handleXmlDeclaration(Tag tag) {
 		handleDocumentPart(tag);
 	}
 
 	/**
 	 * Handle the XML doc type declaration (DTD).
 	 * 
 	 * @param tag
 	 */
 	protected void handleDocTypeDeclaration(Tag tag) {
 		handleDocumentPart(tag);
 	}
 
 	/**
 	 * Handle processing instructions.
 	 * 
 	 * @param tag
 	 */
 	protected void handleProcessingInstruction(Tag tag) {
 		if (!isInsideTextRun()) {
 			handleDocumentPart(tag);
 		} else {
 			addCodeToCurrentTextUnit(tag);
 		}
 	}
 
 	/**
 	 * Handle comments.
 	 * 
 	 * @param tag
 	 */
 	protected void handleComment(Tag tag) {
 		if (!isInsideTextRun()) {
 			handleDocumentPart(tag);
 		} else {
 			addCodeToCurrentTextUnit(tag);
 		}
 	}
 
 	/**
 	 * Handle CDATA sections.
 	 * 
 	 * @param tag
 	 */
 	protected void handleCdataSection(Tag tag) {
 		if (cdataSubfilter != null) {				
 			String parentId = eventBuilder.findMostRecentParentId();
 			String cdataWithoutMarkers = CDATA_START_PATTERN.matcher(tag.toString()).replaceFirst("");
 			cdataWithoutMarkers = CDATA_END_PATTERN.matcher(cdataWithoutMarkers).replaceFirst("");
 			cdataSubfilter.close();
 			cdataSubfilter.setStartSubFilterSkeleton(new GenericSkeleton("<![CDATA["));
 			cdataSubfilter.setEndSubFilterSkeleton(new GenericSkeleton("]]>"));
 			cdataSubfilter.openAsSubfilter(new RawDocument(cdataWithoutMarkers, getSrcLoc()), 
 					// TODO fully set root id??
 					getDocumentId().getLastId(),
 					parentId == null ? getDocumentId().getLastId() : parentId, 
 					eventBuilder.getGroupId());	
 			while (cdataSubfilter.hasNext()) {
 				Event event = cdataSubfilter.next();
 				eventBuilder.addFilterEvent(event);
 			}
 			cdataSubfilter.close();
 		} else {
 			addToDocumentPart(tag.toString());
 		}
 	}
 
 	/**
 	 * Handle all text (PCDATA).
 	 * 
 	 * @param text
 	 */
 	protected void handleText(Segment text) {
 		// if in excluded state everything is skeleton including text
 		if (ruleState.isExludedState()) {
 			addToDocumentPart(text.toString());
 			return;
 		}
 
 		// check for ignorable whitespace and add it to the skeleton
 		if (text.isWhiteSpace() && !isInsideTextRun()) {
 			if (bufferedWhitespace.length() <= 0) {
 				// buffer the whitespace until we know that we are not inside
 				// translatable text.
 				bufferedWhitespace.append(text.toString());
 			}
 			return;
 		}
 
 		if (canStartNewTextUnit()) {
 			startTextUnit(text.toString());
 		} else {
 			addToTextUnit(text.toString());
 		}
 	}
 
 	/**
 	 * Handle all Character entities. Default implementation converts entity to Unicode character.
 	 * 
 	 * @param entity
 	 *            - the character entity
 	 */
 	protected void handleNumericEntity(Segment entity) {
 		String decodedText = CharacterReference.decode(entity.toString(), false);
 		if (!eventBuilder.isCurrentTextUnit()) {
 			eventBuilder.startTextUnit();
 		}
 		eventBuilder.addToTextUnit(decodedText);
 	}
 
 	/**
 	 * Handle all numeric entities. Default implementation converts entity to Unicode character.
 	 * 
 	 * @param entity
 	 *            - the numeric entity
 	 */
 	protected void handleCharacterEntity(Segment entity) {
 		String decodedText = CharacterReference.decode(entity.toString(), false);
 		if (!eventBuilder.isCurrentTextUnit()) {
 			eventBuilder.startTextUnit();
 		}
 		eventBuilder.addToTextUnit(decodedText);
 	}
 
 	/**
 	 * Handle start tags.
 	 * 
 	 * @param startTag
 	 */
 	protected void handleStartTag(StartTag startTag) {
 		Map<String, String> attributes = new HashMap<String, String>();
 		attributes = startTag.getAttributes().populateMap(attributes, true);
 		String idValue = null;
 		RULE_TYPE ruleType = getConfig().getConditionalElementRuleType(startTag.getName(),
 				attributes);
 
 		try {
 			// if in excluded state everything is skeleton including text
 			if (ruleState.isExludedState()) {
 				addToDocumentPart(startTag.toString());
 				updateStartTagRuleState(startTag.getName(), ruleType, idValue);
 				return;
 			}
 
 			List<PropertyTextUnitPlaceholder> propertyTextUnitPlaceholders;
 			propertyTextUnitPlaceholders = createPropertyTextUnitPlaceholders(startTag);
 
 			updateStartTagRuleState(startTag.getName(), ruleType, idValue);
 
 			switch (ruleType) {
 			case INLINE_ELEMENT:
 				if (canStartNewTextUnit()) {
 					startTextUnit();
 				}
 				addCodeToCurrentTextUnit(startTag);
 				break;
 			case ATTRIBUTES_ONLY:
 				// we assume we have already ended any (non-complex) TextUnit in
 				// the main while loop in AbstractMarkupFilter
 				handleAttributesThatAppearAnywhere(propertyTextUnitPlaceholders, startTag);
 				break;
 			case GROUP_ELEMENT:
 				handleAttributesThatAppearAnywhere(propertyTextUnitPlaceholders, startTag);
 				break;
 			case EXCLUDED_ELEMENT:
 				handleAttributesThatAppearAnywhere(propertyTextUnitPlaceholders, startTag);
 				break;
 			case INCLUDED_ELEMENT:
 				handleAttributesThatAppearAnywhere(propertyTextUnitPlaceholders, startTag);
 				break;
 			case TEXT_UNIT_ELEMENT:
 				// search for an idAttribute and set it on the newly created TextUnit if found
 				for (PropertyTextUnitPlaceholder propOrText : propertyTextUnitPlaceholders) {
 					if (propOrText.getAccessType() == PlaceholderAccessType.NAME) {
 						idValue = propOrText.getValue();
 						break;
 					}
 				}
 
 				handleAttributesThatAppearAnywhere(propertyTextUnitPlaceholders, startTag);
 
 				setTextUnitName(idValue);
 				setTextUnitType(getConfig().getElementType(startTag));
 				break;
 			default:
 				handleAttributesThatAppearAnywhere(propertyTextUnitPlaceholders, startTag);
 			}
 		} finally {
 
 			// A TextUnit may have already been created. Update its preserveWS field
 			if (eventBuilder.isCurrentTextUnit()) {
 				TextUnit tu = eventBuilder.peekMostRecentTextUnit();
 				tu.setPreserveWhitespaces(ruleState.isPreserveWhitespaceState());
 			}
 		}
 	}
 
 	protected void updateStartTagRuleState(String tag, RULE_TYPE ruleType, String idValue) {
 		switch (getConfig().getElementRuleType(tag)) {
 		case INLINE_ELEMENT:
 			ruleState.pushInlineRule(tag, ruleType);
 			break;
 		case ATTRIBUTES_ONLY:
 			// TODO: add a rule state for ATTRIBUTE_ONLY rules
 			break;
 		case GROUP_ELEMENT:
 			ruleState.pushGroupRule(tag, ruleType);
 			break;
 		case EXCLUDED_ELEMENT:
 			ruleState.pushExcludedRule(tag, ruleType);
 			break;
 		case INCLUDED_ELEMENT:
 			ruleState.pushIncludedRule(tag, ruleType);
 			break;
 		case TEXT_UNIT_ELEMENT:
 			ruleState.pushTextUnitRule(tag, ruleType, idValue);
 			break;
 		default:
 			break;
 		}
 
 		// TODO: add conditional support for PRESERVE_WHITESPACE rules
 		// does this tag have a PRESERVE_WHITESPACE rule?
 		if (getConfig().isRuleType(tag, RULE_TYPE.PRESERVE_WHITESPACE)) {
 			ruleState.pushPreserverWhitespaceRule(tag, true);
 			setPreserveWhitespace(ruleState.isPreserveWhitespaceState());
 		}
 	}
 
 	protected RULE_TYPE updateEndTagRuleState(EndTag endTag) {
 		RULE_TYPE ruleType = getConfig().getElementRuleType(endTag.getName());
 		RuleType currentState = null;
 
 		switch (ruleType) {
 		case INLINE_ELEMENT:
 			currentState = ruleState.popInlineRule();
 			ruleType = currentState.ruleType;
 			break;
 		case ATTRIBUTES_ONLY:
 			// TODO: add a rule state for ATTRIBUTE_ONLY rules
 			break;
 		case GROUP_ELEMENT:
 			currentState = ruleState.popGroupRule();
 			ruleType = currentState.ruleType;
 			break;
 		case EXCLUDED_ELEMENT:
 			currentState = ruleState.popExcludedIncludedRule();
 			ruleType = currentState.ruleType;
 			break;
 		case INCLUDED_ELEMENT:
 			currentState = ruleState.popExcludedIncludedRule();
 			ruleType = currentState.ruleType;
 			break;
 		case TEXT_UNIT_ELEMENT:
 			currentState = ruleState.popTextUnitRule();
 			ruleType = currentState.ruleType;
 			break;
 		default:
 			break;
 		}
 
 		if (currentState != null) {
 			// if the end tag is not the same as what we found on the stack all bets are off
 			if (!currentState.ruleName.equalsIgnoreCase(endTag.getName())) {
 				String character = Integer.toString(endTag.getBegin());
 				throw new OkapiBadFilterInputException("End tag " + endTag.getName()
 						+ " and start tag " + currentState.ruleName
 						+ " do not match at character number " + character);
 			}
 		}
 
 		return ruleType;
 	}
 
 	/*
 	 * catch tags which are not listed in the config but have attributes that require processing
 	 */
 	private void handleAttributesThatAppearAnywhere(
 			List<PropertyTextUnitPlaceholder> propertyTextUnitPlaceholders, StartTag tag) {
 
 		HashMap<String, String> attributeMap = new HashMap<String, String>();
 
 		switch (getConfig().getConditionalElementRuleType(tag.getName(),
 				tag.getAttributes().populateMap(attributeMap, true))) {
 
 		case TEXT_UNIT_ELEMENT:
 			if (propertyTextUnitPlaceholders != null && !propertyTextUnitPlaceholders.isEmpty()) {
 				startTextUnit(new GenericSkeleton(tag.toString()), propertyTextUnitPlaceholders);
 			} else {
 				startTextUnit(new GenericSkeleton(tag.toString()));
 			}
 			break;
 		case GROUP_ELEMENT:
 			if (propertyTextUnitPlaceholders != null && !propertyTextUnitPlaceholders.isEmpty()) {
 				startGroup(new GenericSkeleton(tag.toString()), getConfig().getElementType(tag),
 						getSrcLoc(), propertyTextUnitPlaceholders);
 			} else {
 				// no attributes that need processing - just treat as skeleton
 				startGroup(new GenericSkeleton(tag.toString()), getConfig().getElementType(tag));
 			}
 			break;
 		default:
 			if (propertyTextUnitPlaceholders != null && !propertyTextUnitPlaceholders.isEmpty()) {
 				startDocumentPart(tag.toString(), tag.getName(), propertyTextUnitPlaceholders);
 				endDocumentPart();
 			} else {
 				// no attributes that need processing - just treat as skeleton
 				addToDocumentPart(tag.toString());
 			}
 
 			break;
 		}
 	}
 
 	/**
 	 * Handle end tags, including empty tags.
 	 * 
 	 * @param endTag
 	 */
 	protected void handleEndTag(EndTag endTag) {
 		RULE_TYPE ruleType = RULE_TYPE.RULE_NOT_FOUND;
 
 		// if in excluded state everything is skeleton including text
 		if (ruleState.isExludedState()) {
 			addToDocumentPart(endTag.toString());
 			updateEndTagRuleState(endTag);
 			return;
 		}
 
 		ruleType = updateEndTagRuleState(endTag);
 
 		switch (ruleType) {
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
 			addToDocumentPart(endTag.toString());
 			break;
 		case INCLUDED_ELEMENT:
 			addToDocumentPart(endTag.toString());
 			break;
 		case TEXT_UNIT_ELEMENT:
 			endTextUnit(new GenericSkeleton(endTag.toString()));
 			break;
 		default:
 			addToDocumentPart(endTag.toString());
 			break;
 		}
 
 		// TODO: add conditional support for PRESERVE_WHITESPACE rules
 		// does this tag have a PRESERVE_WHITESPACE rule?
 		if (getConfig().isRuleType(endTag.getName(), RULE_TYPE.PRESERVE_WHITESPACE)) {
 			ruleState.popPreserverWhitespaceRule();
 			setPreserveWhitespace(ruleState.isPreserveWhitespaceState());
 			// handle cases such as xml:space where we popped on an element while
 			// processing the attributes
 		} else if (ruleState.peekPreserverWhitespaceRule().ruleName.equalsIgnoreCase(endTag
 				.getName())) {
 			ruleState.popPreserverWhitespaceRule();
 			setPreserveWhitespace(ruleState.isPreserveWhitespaceState());
 		}
 	}
 
 	/**
 	 * Handle anything else not classified by Jericho.
 	 * 
 	 * @param tag
 	 */
 	protected void handleDocumentPart(Tag tag) {
 		addToDocumentPart(tag.toString());
 	}
 
 	/**
 	 * Some attributes names are converted to Okapi standards such as HTML charset to "encoding" and lang to "language"
 	 * 
 	 * @param attrName
 	 *            - the attribute name
 	 * @param attrValue
 	 *            - the attribute value
 	 * @param tag
 	 *            - the Jericho {@link Tag} that contains the attribute
 	 * @return the attribute name after it as passe through the normalization rules
 	 */
 	abstract protected String normalizeAttributeName(String attrName, String attrValue, Tag tag);
 
 	/**
 	 * Add an {@link Code} to the current {@link TextUnit}. Throws an exception if there is no current {@link TextUnit}.
 	 * 
 	 * @param tag
 	 *            - the Jericho {@link Tag} that is converted to a Okpai {@link Code}
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
 			if (startTag.isSyntacticalEmptyElementTag()) {
 				codeType = TextFragment.TagType.PLACEHOLDER;
 			} else if (startTag.isEndTagRequired()) {
 				codeType = TextFragment.TagType.OPENING;
 			} else {
 				codeType = TextFragment.TagType.PLACEHOLDER;
 			}
 
 			// create a list of Property or Text placeholders for this tag
 			// If this list is empty we know that there are no attributes that
 			// need special processing
 			propertyTextUnitPlaceholders = null;
 
 			propertyTextUnitPlaceholders = createPropertyTextUnitPlaceholders(startTag);
 			if (propertyTextUnitPlaceholders != null && !propertyTextUnitPlaceholders.isEmpty()) {
 				// add code and process actionable attributes
 				addToTextUnit(new Code(codeType, getConfig().getElementType(tag), literalTag),
 						propertyTextUnitPlaceholders);
 			} else {
 				// no actionable attributes, just add the code as-is
 				addToTextUnit(new Code(codeType, getConfig().getElementType(tag), literalTag));
 			}
 		} else { // end or unknown tag
 			if (tag.getTagType() == EndTagType.NORMAL
 					|| tag.getTagType() == EndTagType.UNREGISTERED) {
 				codeType = TextFragment.TagType.CLOSING;
 			} else {
 				codeType = TextFragment.TagType.PLACEHOLDER;
 			}
 			addToTextUnit(new Code(codeType, getConfig().getElementType(tag), literalTag));
 		}
 	}
 
 	/**
 	 * For the given Jericho {@link StartTag} parse out all the actionable attributes and and store them as
 	 * {@link PropertyTextUnitPlaceholder}. {@link PlaceholderAccessType} are set based on the filter configuration for
 	 * each attribute. for the attribute name and value.
 	 * 
 	 * @param startTag
 	 *            - Jericho {@link StartTag}
 	 * @return all actionable (translatable, writable or read-only) attributes found in the {@link StartTag}
 	 */
 	protected List<PropertyTextUnitPlaceholder> createPropertyTextUnitPlaceholders(StartTag startTag) {
 		// list to hold the properties or TextUnits
 		List<PropertyTextUnitPlaceholder> propertyOrTextUnitPlaceholders = new LinkedList<PropertyTextUnitPlaceholder>();
 		HashMap<String, String> attributeMap = new HashMap<String, String>();
 		for (Attribute attribute : startTag.parseAttributes()) {
 			attributeMap.clear();
 
 			switch (getConfig().findMatchingAttributeRule(startTag.getName(),
 					startTag.getAttributes().populateMap(attributeMap, true), attribute.getName())) {
 			case ATTRIBUTE_TRANS:
 				propertyOrTextUnitPlaceholders.add(createPropertyTextUnitPlaceholder(
 						PlaceholderAccessType.TRANSLATABLE, attribute.getName(),
 						attribute.getValue(), startTag, attribute));
 				break;
 			case ATTRIBUTE_WRITABLE:
 				propertyOrTextUnitPlaceholders.add(createPropertyTextUnitPlaceholder(
 						PlaceholderAccessType.WRITABLE_PROPERTY, attribute.getName(),
 						attribute.getValue(), startTag, attribute));
 				break;
 			case ATTRIBUTE_READONLY:
 				propertyOrTextUnitPlaceholders.add(createPropertyTextUnitPlaceholder(
 						PlaceholderAccessType.READ_ONLY_PROPERTY, attribute.getName(),
 						attribute.getValue(), startTag, attribute));
 				break;
 			case ATTRIBUTE_ID:
 				propertyOrTextUnitPlaceholders.add(createPropertyTextUnitPlaceholder(
 						PlaceholderAccessType.NAME, attribute.getName(), attribute.getValue(),
 						startTag, attribute));
 				break;
 			case ATTRIBUTE_PRESERVE_WHITESPACE:
 				boolean preserveWS = getConfig().isPreserveWhitespaceCondition(attribute.getName(),
 						attributeMap);
 				boolean defaultWS = getConfig().isDefaultWhitespaceCondition(attribute.getName(),
 						attributeMap);
 				// if its not reserve or default then the rule doesn't apply
 				if (preserveWS || defaultWS) {
 					if (preserveWS) {
 						ruleState.pushPreserverWhitespaceRule(startTag.getName(), true);
 					} else if (defaultWS) {
 						ruleState.pushPreserverWhitespaceRule(startTag.getName(), false);
 					}
 					setPreserveWhitespace(ruleState.isPreserveWhitespaceState());
 					propertyOrTextUnitPlaceholders.add(createPropertyTextUnitPlaceholder(
 							PlaceholderAccessType.WRITABLE_PROPERTY, attribute.getName(),
 							attribute.getValue(), startTag, attribute));
 				}
 				break;
 			default:
 				break;
 			}
 		}
 
 		return propertyOrTextUnitPlaceholders;
 	}
 
 	/**
 	 * Create a {@link PropertyTextUnitPlaceholder} given the supplied type, name and Jericho {@link Tag} and
 	 * {@link Attribute}.
 	 * 
 	 * @param type
 	 *            - {@link PlaceholderAccessType} is one of TRANSLATABLE, READ_ONLY_PROPERTY, WRITABLE_PROPERTY
 	 * @param name
 	 *            - attribute name
 	 * @param value
 	 *            - attribute value
 	 * @param tag
 	 *            - Jericho {@link Tag} which contains the attribute
 	 * @param attribute
 	 *            - attribute as a Jericho {@link Attribute}
 	 * @return a {@link PropertyTextUnitPlaceholder} representing the attribute
 	 */
 	protected PropertyTextUnitPlaceholder createPropertyTextUnitPlaceholder(
 			PlaceholderAccessType type, String name, String value, Tag tag, Attribute attribute) {
 		// offset of attribute
 		int mainStartPos = attribute.getBegin() - tag.getBegin();
 		int mainEndPos = attribute.getEnd() - tag.getBegin();
 
 		// offset of value of the attribute
 		int valueStartPos = attribute.getValueSegment().getBegin() - tag.getBegin();
 		int valueEndPos = attribute.getValueSegment().getEnd() - tag.getBegin();
 
 		return new PropertyTextUnitPlaceholder(type, normalizeAttributeName(name, value, tag),
 				value, mainStartPos, mainEndPos, valueStartPos, valueEndPos);
 	}
 
 	/**
 	 * Is the input encoded as UTF-8?
 	 * 
 	 * @return true if the document is in utf8 encoding.
 	 */
 	@Override
 	protected boolean isUtf8Encoding() {
 		return hasUtf8Encoding;
 	}
 
 	/**
 	 * Does the input have a UTF-8 Byte Order Mark?
 	 * 
 	 * @return true if the document has a utf-8 byte order mark.
 	 */
 	@Override
 	protected boolean isUtf8Bom() {
 		return hasUtf8Bom;
 	}
 
 	/**
 	 * 
 	 * @return the preserveWhitespace boolean.
 	 */
 	protected boolean isPreserveWhitespace() {
 		return ruleState.isPreserveWhitespaceState();
 	}
 
 	protected void setPreserveWhitespace(boolean preserveWhitespace) {
 		eventBuilder.setPreserveWhitespace(preserveWhitespace);
 	}
 
 	protected void addToDocumentPart(String part) {
 		eventBuilder.addToDocumentPart(part);
 	}
 
 	protected void addToTextUnit(String text) {
 		eventBuilder.addToTextUnit(text);
 	}
 
 	protected void startTextUnit(String text) {
 		eventBuilder.startTextUnit(text);
 	}
 
 	protected void setTextUnitName(String name) {
 		eventBuilder.setTextUnitName(name);
 	}
 
 	protected void setTextUnitType(String type) {
 		eventBuilder.setTextUnitType(type);
 	}
 
 	protected boolean canStartNewTextUnit() {
 		return eventBuilder.canStartNewTextUnit();
 	}
 
 	protected boolean isInsideTextRun() {
 		return eventBuilder.isInsideTextRun();
 	}
 
 	protected void addToTextUnit(Code code) {
 		eventBuilder.addToTextUnit(code);
 	}
 
 	protected void addToTextUnit(Code code,
 			List<PropertyTextUnitPlaceholder> propertyTextUnitPlaceholders) {
 		eventBuilder.addToTextUnit(code, propertyTextUnitPlaceholders);
 	}
 
 	protected void endDocumentPart() {
 		eventBuilder.endDocumentPart();
 	}
 
 	protected void startDocumentPart(String part, String name,
 			List<PropertyTextUnitPlaceholder> propertyTextUnitPlaceholders) {
 		eventBuilder.startDocumentPart(part, name, propertyTextUnitPlaceholders);
 	}
 
 	protected void startGroup(GenericSkeleton startMarker, String commonTagType) {
 		eventBuilder.startGroup(startMarker, commonTagType);
 	}
 
 	protected void startGroup(GenericSkeleton startMarker, String commonTagType, LocaleId locale,
 			List<PropertyTextUnitPlaceholder> propertyTextUnitPlaceholders) {
 		eventBuilder.startGroup(startMarker, commonTagType, locale, propertyTextUnitPlaceholders);
 	}
 
 	protected void startTextUnit(GenericSkeleton startMarker) {
 		eventBuilder.startTextUnit(startMarker);
 	}
 
 	protected void startTextUnit(GenericSkeleton startMarker,
 			List<PropertyTextUnitPlaceholder> propertyTextUnitPlaceholders) {
 		eventBuilder.startTextUnit(startMarker, propertyTextUnitPlaceholders);
 	}
 
 	protected void endTextUnit(GenericSkeleton endMarker) {
 		eventBuilder.endTextUnit(endMarker);
 	}
 
 	protected void endGroup(GenericSkeleton endMarker) {
 		eventBuilder.endGroup(endMarker);
 	}
 
 	protected void startTextUnit() {
 		eventBuilder.startTextUnit();
 	}
 
 	protected long getTextUnitId() {
 		return eventBuilder.getTextUnitId();
 	}
 
 	protected void setTextUnitId(long id) {
 		eventBuilder.setTextUnitId(id);
 	}
 
 	protected long getDocumentPartId() {
 		return eventBuilder.getDocumentPartId();
 	}
 
 	protected void setDocumentPartId(long id) {
 		eventBuilder.setDocumentPartId(id);
 	}
 
 	protected void appendToFirstSkeletonPart(String text) {
 		eventBuilder.appendToFirstSkeletonPart(text);
 	}
 
 	protected void addFilterEvent(Event event) {
 		eventBuilder.addFilterEvent(event);
 	}
 
 	protected ExtractionRuleState getRuleState() {
 		return ruleState;
 	}
 
 	/**
 	 * @return the eventBuilder
 	 */
 	public EventBuilder getEventBuilder() {
 		return eventBuilder;
 	}
 
 	/**
 	 * Sets the input document mime type.
 	 * 
 	 * @param mimeType
 	 *            the new mime type
 	 */
 	@Override
 	public void setMimeType(String mimeType) {
 		super.setMimeType(mimeType);
 	}
 
 	public StringBuilder getBufferedWhiteSpace() {
 		return bufferedWhitespace;
 	}
 }
