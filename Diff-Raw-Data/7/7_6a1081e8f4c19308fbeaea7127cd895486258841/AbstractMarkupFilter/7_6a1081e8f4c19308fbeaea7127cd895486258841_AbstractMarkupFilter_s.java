 /*===========================================================================
   Copyright (C) 2008-2011 by the Okapi Framework contributors
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
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
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
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.MimeTypeMapper;
 import net.sf.okapi.common.encoder.XMLEncoder;
 import net.sf.okapi.common.exceptions.OkapiBadFilterInputException;
 import net.sf.okapi.common.exceptions.OkapiIOException;
 import net.sf.okapi.common.filters.AbstractFilter;
 import net.sf.okapi.common.filters.EventBuilder;
 import net.sf.okapi.common.filters.IFilter;
 import net.sf.okapi.common.filters.PropertyTextUnitPlaceholder;
 import net.sf.okapi.common.filters.PropertyTextUnitPlaceholder.PlaceholderAccessType;
 import net.sf.okapi.common.filters.SubFilter;
 import net.sf.okapi.common.resource.Code;
 import net.sf.okapi.common.resource.Ending;
 import net.sf.okapi.common.resource.ITextUnit;
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
 	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
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
 	private boolean hasBOM;
 	private EventBuilder eventBuilder;
 	private RawDocument currentRawDocument;
 	private ExtractionRuleState ruleState; 
 //	private SubFilter cdataSubfilter; 
 //	private PcdataSubFilter pcdataSubfilter;
 	private String currentId;
 	private boolean documentEncoding;
 	private String currentDocName;
 	private IFilter cdataFilter;
 	private IFilter pcdataFilter;
 	private int cdataSectionIndex;
 	private int pcdataSectionIndex;
 	
 	static {
 		Config.ConvertNonBreakingSpaces = false;
 		Config.IsHTMLEmptyElementTagRecognised = true;
 		Config.ConvertNonBreakingSpaces = false;
 		Config.NewLine = BOMNewlineEncodingDetector.NewlineType.LF.toString();
 		Config.LoggerProvider = LoggerProvider.SLF4J;
 		//Config.CurrentCompatibilityMode = Config.CompatibilityMode.XHTML;
 	}
 
 	/**
 	 * Default constructor for {@link AbstractMarkupFilter} using default {@link EventBuilder}
 	 */
 	public AbstractMarkupFilter() {
 		this.bufferedWhitespace = new StringBuilder();
 		this.hasUtf8Bom = false;
 		this.hasUtf8Encoding = false;
 		this.hasBOM = false;
 		this.currentId = null;
 		this.documentEncoding = false;		
 	}
 
 	/**
 	 * Default constructor for {@link AbstractMarkupFilter} using default {@link EventBuilder}
 	 */
 	public AbstractMarkupFilter(EventBuilder eventBuilder) {		
 		this.eventBuilder = eventBuilder;
 		this.bufferedWhitespace = new StringBuilder();
 		this.hasUtf8Bom = false;
 		this.hasUtf8Encoding = false;
 		this.hasBOM = false;
 		this.currentId = null;
 		this.documentEncoding = false;
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
 		
 		this.hasUtf8Bom = false;
 		this.hasUtf8Encoding = false;
 		this.currentId = null;
 
 		if (ruleState != null) {
 			ruleState.reset(!getConfig().isGlobalPreserveWhitespace(),
 							getConfig().isGlobalExcludeByDefault());
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
 		
 		LOGGER.debug(getDocumentName() + " has been closed");
 	}
 
 	/*
 	 * Get PREVIEW_BYTE_COUNT bytes so we can sniff out any encoding information in XML or HTML files
 	 */
 	protected Source getParsedHeader(final InputStream inputStream) {
 		try {
 			// Make sure we grab the same buffer for UTF-16/32
 			// this is to avoid round trip problem when not detecting a declaration
 			// between a non-UTF-16/32 and a UTF-16/32 input
 			int charSize = 1;
 			if ( getEncoding().toLowerCase().startsWith("utf-16") ) charSize = 2;
 			else if ( getEncoding().toLowerCase().startsWith("utf-32") ) charSize = 4;
 			
 			final byte[] bytes = new byte[PREVIEW_BYTE_COUNT * charSize];
 			int i;
 			for (i = 0; i < bytes.length; i++) {
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
 	
 	protected String detectEncoding(RawDocument input) {
 		BOMNewlineEncodingDetector detector = new BOMNewlineEncodingDetector(input.getStream(),
 				input.getEncoding());
 		detector.detectAndRemoveBom();
 
 		setEncoding(detector.getEncoding());
 		hasUtf8Bom = detector.hasUtf8Bom();
 		hasUtf8Encoding = detector.hasUtf8Encoding();
 		hasBOM = detector.hasBom();
 		setNewlineType(detector.getNewlineType().toString());
 
 		Source parsedHeader = getParsedHeader(input.getStream());
 		String detectedEncoding = parsedHeader.getDocumentSpecifiedEncoding();
 		documentEncoding = detectedEncoding == null ? false : true; 
 
 		if (detectedEncoding == null && getEncoding() != null) {
 			detectedEncoding = getEncoding();
 			LOGGER.debug(String.format(
 					"Cannot auto-detect encoding. Using the default encoding (%s)", getEncoding()));
 		} else if (getEncoding() == null) {
 			detectedEncoding = parsedHeader.getEncoding(); // get best guess
 			LOGGER.debug(
 					String.format(
 							"Default encoding and detected encoding not found. Using best guess encoding (%s)",
 							detectedEncoding));
 		}
 
 		return detectedEncoding;
 	}
 
 	/**
 	 * Start a new {@link IFilter} using the supplied {@link RawDocument}.
 	 * 
 	 * @param input
 	 *            - input to the {@link IFilter} (can be a {@link CharSequence}, {@link URI} or {@link InputStream})
 	 */
 	public void open(RawDocument input) {
 		open(input, true);
 		LOGGER.debug(getName() + " has opened an input document");
 	}
 
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
 		
 		super.open(input, generateSkeleton);
 		
 		currentRawDocument = input;
 
 		// doc name may be set by sub-classes
 		if (getCurrentDocName() != null) {
 			setDocumentName(getCurrentDocName());
 		} else if (input.getInputURI() != null) {
 			setDocumentName(input.getInputURI().getPath());
 		}
 
 		try {
 			String detectedEncoding = detectEncoding(input);
 			input.setEncoding(detectedEncoding);
 			setOptions(input.getSourceLocale(), input.getTargetLocale(), detectedEncoding,
 					generateSkeleton);
 			document = new StreamedSource(input.getReader());
 		} catch (IOException e) {
 			throw new OkapiIOException("Filter could not open input stream", e);
 		}
 		
 		currentDocName = null;
 
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
 			eventBuilder = new AbstractMarkupEventBuilder(getParentId(), 
 					this, getEncoderManager(), getEncoding(), getNewlineType());
 			eventBuilder.setMimeType(getMimeType());
 		} else {
 			eventBuilder.reset(getParentId(), this);
 		}		
 
 		eventBuilder.addFilterEvent(createStartFilterEvent());		
 
 		// default is to preserve whitespace
 		boolean preserveWhitespace = true;
 		boolean defaultExcludeRule = false;
 		if (getConfig() != null) {
 			preserveWhitespace = getConfig().isGlobalPreserveWhitespace();
 			defaultExcludeRule = getConfig().isGlobalExcludeByDefault();
 		}
 		ruleState = new ExtractionRuleState(preserveWhitespace, defaultExcludeRule);
 		setPreserveWhitespace(ruleState.isPreserveWhitespaceState());
 
 		// This optimizes memory at the expense of performance
 		nodeIterator = document.iterator();
 
 		cdataSectionIndex = 0;
 		pcdataSectionIndex = 0;
 		
 		// initialize cdata sub-filter
 		TaggedFilterConfiguration config = getConfig(); 
 		if (config != null && config.getGlobalCDATASubfilter() != null) {
 			cdataFilter = getFilterConfigurationMapper().createFilter(
 					getConfig().getGlobalCDATASubfilter(), cdataFilter); 
 			//getEncoderManager().mergeMappings(cdataFilter.getEncoderManager());
 			//cdataSubfilter = new SubFilter(cdataFilter, null, null, null);
 		}
 		
 		// initialize pcdata sub-filter
 		if (config != null && config.getGlobalPCDATASubfilter() != null) {
 			String subfilterName = getConfig().getGlobalPCDATASubfilter();
 			pcdataFilter = getFilterConfigurationMapper().createFilter(subfilterName, pcdataFilter); 
 			//getEncoderManager().mergeMappings(pcdataFilter.getEncoderManager());
 			//pcdataSubfilter = new PcdataSubFilter(pcdataFilter);
 		}
 	}
 
 	/**
 	 * End the current filter processing and send the {@link Ending} {@link Event}
 	 */
 	protected void endFilter() {
 		// clear out all unended temp events
 		eventBuilder.flushRemainingTempEvents();
 		
 		// make sure we flush out any whitespace at the end of the file
 		if (bufferedWhitespace.length() > 0) {
 			eventBuilder.addDocumentPart(bufferedWhitespace.toString());
 			bufferedWhitespace.setLength(0);
 			bufferedWhitespace.trimToSize();
 		}
 		
 		// add the final endDocument event
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
 			Tag tag = (Tag)segment;
 			if (getConfig().getElementRuleTypeCandidate(tag.getName()) == RULE_TYPE.INLINE_ELEMENT
 					|| getConfig().getElementRuleTypeCandidate(tag.getName()) == RULE_TYPE.INLINE_EXCLUDED_ELEMENT
 					|| (getEventBuilder().isInsideTextRun() && (tag
 							.getTagType() == StartTagType.COMMENT || tag
 							.getTagType() == StartTagType.XML_PROCESSING_INSTRUCTION))) {
 				isInsideTextRun = true;
 			}
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
 	protected void postProcessTextUnit (ITextUnit textUnit) {
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
 		if (ruleState.isExludedState()) {
 			addToDocumentPart(tag.toString());
 			return;
 		}
 		
 		if (isInsideTextRun()) {
 			if (ruleState.isInlineExcludedState()) {
 				eventBuilder.appendCodeData(tag.toString());
 				eventBuilder.appendCodeOuterData(tag.toString());
 				return;
 			} else {
 				addCodeToCurrentTextUnit(tag);
 			}
 		} else {
 			handleDocumentPart(tag);
 		}
 	}
 
 	/**
 	 * Handle comments.
 	 * 
 	 * @param tag
 	 */
 	protected void handleComment(Tag tag) {
 		if (ruleState.isExludedState()) {
 			addToDocumentPart(tag.toString());
 			return;
 		}
 		if (isInsideTextRun()) {
 			if (ruleState.isInlineExcludedState()) {
 				eventBuilder.appendCodeData(tag.toString());
 				eventBuilder.appendCodeOuterData(tag.toString());
 				return;
 			} else {
 				addCodeToCurrentTextUnit(tag);
 			}
 		} else {
 			handleDocumentPart(tag);
 		}
 	}
 
 	/**
 	 * Handle CDATA sections.
 	 * 
 	 * @param tag
 	 */
 	protected void handleCdataSection(Tag tag) {
 		// end any skeleton so we can start CDATA section with subfilter
 		if (eventBuilder.hasUnfinishedSkeleton()) {
 			endDocumentPart();
 		}
 
 		String cdataWithoutMarkers = CDATA_START_PATTERN.matcher(tag.toString()).replaceFirst("");
 		cdataWithoutMarkers = CDATA_END_PATTERN.matcher(cdataWithoutMarkers).replaceFirst("");
 		
 		if ( ruleState.isExludedState() ) {
 			// Excluded content
 			addToDocumentPart(tag.toString());
 		} else { // Content to extract
 			startTextUnit(new GenericSkeleton("<![CDATA["));
 			
 			if (cdataFilter != null) {				
 //				cdataSubfilter.close();
 				
 				String parentId = eventBuilder.findMostRecentParentId();
 				if (parentId == null) parentId = getDocumentId().getLastId();
 				
 				String parentName = eventBuilder.findMostRecentParentName();
 				if (parentName == null) parentName = getDocumentId().getLastId();
 				
 				SubFilter cdataSubfilter = new SubFilter(cdataFilter, 
 						//getEncoderManager().getEncoder(),
 						null, // we don't encode cdata
 						++cdataSectionIndex, parentId, parentName);
 //				FilterState s = new FilterState(FILTER_STATE.STANDALONE_TEXTUNIT,
 //						parentId, 
 //						new GenericSkeleton("<![CDATA["), 
 //						new GenericSkeleton("]]>"),
 //						null);
 //				s.setParentTextUnitName(eventBuilder.findMostRecentTextUnitName());
 //				cdataSubfilter.setState(s);
 //				cdataSubfilter.open(new RawDocument(cdataWithoutMarkers, getSrcLoc()));	
 //				while (cdataSubfilter.hasNext()) {
 //					Event event = cdataSubfilter.next();
 //					eventBuilder.addFilterEvent(event);
 //				}			
 //				cdataSubfilter.close();
 				eventBuilder.addFilterEvents(cdataSubfilter.getEvents(new RawDocument(cdataWithoutMarkers, getSrcLoc())));
 				addToTextUnit(cdataSubfilter.createRefCode());
 				
 //				// Form CDATA section from the subfiltered content
 //				cdataSubfilter.getStartSubFilter().setSkeleton(new GenericSkeleton("<![CDATA["));
 //				cdataSubfilter.getEndSubFilter().setSkeleton(new GenericSkeleton("]]>"));
 			} else {
 				// we assume the CDATA is plain text take it as is				
 				addToTextUnit(cdataWithoutMarkers);				
 			}
 			setTextUnitType(ITextUnit.TYPE_CDATA);
 			setTextUnitMimeType(MimeTypeMapper.PLAIN_TEXT_MIME_TYPE);			
 			endTextUnit(new GenericSkeleton("]]>"));
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
 		
 		if (ruleState.isInlineExcludedState()) {
 			eventBuilder.appendCodeData(text.toString());
 			eventBuilder.appendCodeOuterData(text.toString());
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
 
 		// reset after each start tag so that we never 
 		// set a TextUnit name that id from a far out tag
 		currentId = null;
 		
 		try {
 			// if in excluded state everything is skeleton including text
 			if (ruleState.isExludedState()) {
 				addToDocumentPart(startTag.toString());
 				if (!startTag.isSyntacticalEmptyElementTag()) {
 					updateStartTagRuleState(startTag.getName(), ruleType, idValue);
 				}
 				return;
 			}
 
 			List<PropertyTextUnitPlaceholder> propertyTextUnitPlaceholders;
 			propertyTextUnitPlaceholders = createPropertyTextUnitPlaceholders(startTag);
 			
 			if (!startTag.isSyntacticalEmptyElementTag()) {
 				updateStartTagRuleState(startTag.getName(), ruleType, idValue);
 			}
 			
 			switch (ruleType) {
 			case INLINE_EXCLUDED_ELEMENT:
 				// special code like: "<ph translate='no'>some protected text with </ph>"
 				// where the start tag, text and end tag are all one code
 				if (canStartNewTextUnit()) {
 					startTextUnit();
 				}
 				addCodeToCurrentTextUnit(startTag, false);
 				// addCodeToCurrentTextUnit puts tag in Code.data by default. 
 				// Move data to Code.outerData
 				String d = eventBuilder.getCurrentCode().getData();
 				eventBuilder.getCurrentCode().setData("");
 				eventBuilder.getCurrentCode().setOuterData(d);
 				break;
 			case INLINE_ELEMENT:
 				// check to see if we are inside a inline run that is excluded 
 				if (ruleState.isInlineExcludedState()) {
 					eventBuilder.appendCodeOuterData(startTag.toString());
 					eventBuilder.appendCodeData(startTag.toString());
 					break;
 				}
 				
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
 				handleAttributesThatAppearAnywhere(propertyTextUnitPlaceholders, startTag);
 				setTextUnitType(getConfig().getElementType(startTag));
 				break;
 			default:
 				handleAttributesThatAppearAnywhere(propertyTextUnitPlaceholders, startTag);
 			}
 		} finally {
 			// A TextUnit may have already been created. Update its preserveWS field
 			if (eventBuilder.isCurrentTextUnit()) {
 				ITextUnit tu = eventBuilder.peekMostRecentTextUnit();
 				tu.setPreserveWhitespaces(ruleState.isPreserveWhitespaceState());
 			}
 		}
 	}
 
 	protected void updateStartTagRuleState(String tag, RULE_TYPE ruleType, String idValue) {
 		RULE_TYPE r = getConfig().getElementRuleTypeCandidate(tag);
 		switch (r) {	
 		case INLINE_EXCLUDED_ELEMENT:
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
 		RULE_TYPE ruleType = getConfig().getElementRuleTypeCandidate(endTag.getName());
 		RuleType currentState = null;
 
 		switch (ruleType) {
 		case INLINE_EXCLUDED_ELEMENT:
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
 		case INLINE_EXCLUDED_ELEMENT:
 			eventBuilder.endCode(endTag.toString());			
 			break;
 		case INLINE_ELEMENT:
 			// check to see if we are inside a inline run that is excluded 
 			if (ruleState.isInlineExcludedState()) {
 				eventBuilder.appendCodeOuterData(endTag.toString());
 				eventBuilder.appendCodeData(endTag.toString());
 				break;
 			}
 			
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
 			// if a pcdata subfilter is configured let it do the processsing
 			if (pcdataFilter != null && isInsideTextRun()) {		
 				// remove the TextUnit we have accumulated since the start tag
 				//ITextUnit pcdata = popTempEvent().getTextUnit();
 				ITextUnit pcdata = peekTempEvent().getTextUnit();
 				
 //				pcdataSubfilter.close();
 
 //				String parentId = eventBuilder.findMostRecentParentId();
 //				parentId = (parentId == null ? getDocumentId().getLastId() : parentId);
 				
 				String parentId = eventBuilder.findMostRecentParentId();
 				if (parentId == null) {
 					parentId = pcdata.getId();
 				}
 				if (parentId == null) {
 					parentId = getDocumentId().getLastId();
 				}
 				
 				String parentName = eventBuilder.findMostRecentParentName();
 				if (parentName == null) parentName = pcdata.getType(); // tag name
 				if (parentName == null) parentName = getDocumentId().getLastId();
 				
 //				PcdataSubFilter pcdataSubfilter = new PcdataSubFilter(pcdataFilter, 
 //						new XMLEncoder(getEncoding(), getNewlineType(), true, true, false, 1), 
 //						++pcdataSectionIndex, parentId, parentName);
 				
 				SubFilter pcdataSubfilter = new SubFilter(pcdataFilter, 
 						new XMLEncoder(getEncoding(), getNewlineType(), true, true, false, 0), 
 						++pcdataSectionIndex, parentId, parentName);
 				
 //				FilterState s = new FilterState(FILTER_STATE.INSIDE_TEXTUNIT, 
 //						parentId, 
 //						new GenericSkeleton(((GenericSkeleton)pcdata.getSkeleton()).getFirstPart().toString()), 
 //						new GenericSkeleton(endTag.toString()),
 //						null);
 //				s.setParentTextUnitName(eventBuilder.findMostRecentTextUnitName());
 //				pcdataSubfilter.setState(s);
 //				pcdataSubfilter.open(new RawDocument(pcdata.getSource().toString(), getSrcLoc()));
 //				while (pcdataSubfilter.hasNext()) {
 //					Event event = pcdataSubfilter.next();
 //					eventBuilder.addFilterEvent(event);
 //				}			
 //				pcdataSubfilter.close();
 				eventBuilder.addFilterEvents(pcdataSubfilter.getEvents(new RawDocument(pcdata.getSource().toString(), getSrcLoc())));
 				// Clear source as we will parse it with the subfilter (skeleton remains)
 				pcdata.getSource().clear();
 				addToTextUnit(pcdataSubfilter.createRefCode()); 
 				endTextUnit(new GenericSkeleton(endTag.toString()));
 			} else {
 				endTextUnit(new GenericSkeleton(endTag.toString()));
 			}
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
 		} else if (ruleState.peekPreserverWhitespaceRule().ruleName.equalsIgnoreCase(endTag.getName())) {
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
 		addCodeToCurrentTextUnit(tag, true);
 	}
 	
 	
 	/**
 	 * Add an {@link Code} to the current {@link TextUnit}. Throws an exception if there is no current {@link TextUnit}.
 	 * 
 	 * @param tag
 	 *            - the Jericho {@link Tag} that is converted to a Okpai {@link Code}
 	 * @param endCodeNow 
 	 *            - do we end the code now or delay so we can add more content to the code?  
 	 * 				
 	 */
 	protected void addCodeToCurrentTextUnit(Tag tag, boolean endCodeNow) {
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
 				if (ruleState.isInlineExcludedState()) {
 					codeType = TextFragment.TagType.PLACEHOLDER;
 				} else {
 					codeType = TextFragment.TagType.OPENING;
 				}
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
 						endCodeNow,
 						propertyTextUnitPlaceholders);
 			} else {
 				// no actionable attributes, just add the code as-is
 				addToTextUnit(new Code(codeType, getConfig().getElementType(tag), literalTag), 
 						endCodeNow);
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
 				// for these non-translatable (but localizable) attributes use the raw value 
 				// given by attribute.getValueSegment() to avoid any entity unescaping that might 
 				// produce illegal output
 				propertyOrTextUnitPlaceholders.add(createPropertyTextUnitPlaceholder(
 						PlaceholderAccessType.WRITABLE_PROPERTY, attribute.getName(),
 						attribute.getValueSegment().toString(), startTag, attribute));
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
 				currentId = attribute.getValue() + "-" + attribute.getName();
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
 	 * Does the input have a BOM?
 	 * 
 	 * @return true if the document has a BOM.
 	 */
 	protected boolean isBOM() {
 		return hasBOM;
 	}
 	
 	/**
 	 * Does this document have a document encoding specified? 
 	 * @return true if has meta tag with encoding, false otherwise
 	 */
 	protected boolean isDocumentEncoding() {
 		return documentEncoding;
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
 
 	protected void setTextUnitPreserveWhitespace(boolean preserveWhitespace) {
 		eventBuilder.setTextUnitPreserveWhitespace(preserveWhitespace);
 	}
 	
 	protected void addToDocumentPart(String part) {
 		eventBuilder.addToDocumentPart(part);
 	}
 
 	protected void addToTextUnit(String text) {
 		eventBuilder.addToTextUnit(text);
 	}
 
 	protected void startTextUnit(String text) {
 		eventBuilder.startTextUnit(text);
 		setTextUnitName(currentId);
 	}
 
 	protected void setTextUnitName(String name) {
 		String n = name;
 		if (name == null) {
 			// we have no name for this TU but lets check for a parent
 			// name. All children will inherit this
 			n = eventBuilder.findMostRecentTextUnitName();
 		}
 		eventBuilder.setTextUnitName(n);
 		currentId = null;
 	}
 
 	protected void setTextUnitType(String type) {
 		eventBuilder.setTextUnitType(type);
 	}
 	
 	protected void setTextUnitTranslatable(boolean translatable) {
 		eventBuilder.setTextUnitTranslatable(translatable);
 	}
 
 	protected void setCurrentDocName(String currentDocName) {
 		this.currentDocName = currentDocName;
 	}
 
 	protected String getCurrentDocName() {
 		return currentDocName;
 	}
 
 	protected boolean canStartNewTextUnit() {
 		return eventBuilder.canStartNewTextUnit();
 	}
 
 	protected boolean isInsideTextRun() {
 		return eventBuilder.isInsideTextRun();
 	}
 
 	protected void addToTextUnit(Code code, boolean endCodeNow) {
 		eventBuilder.addToTextUnit(code, endCodeNow);
 	}
 
 	protected void addToTextUnit(Code code) {
 		eventBuilder.addToTextUnit(code);
 	}
 	
 	protected void addToTextUnit(Code code, boolean endCodeNow, 
 			List<PropertyTextUnitPlaceholder> propertyTextUnitPlaceholders) {
 		eventBuilder.addToTextUnit(code, endCodeNow, propertyTextUnitPlaceholders);
 	}
 	
 	protected void addToTextUnit(Code code,
 			List<PropertyTextUnitPlaceholder> propertyTextUnitPlaceholders) {
 		eventBuilder.addToTextUnit(code, true, propertyTextUnitPlaceholders);
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
 		setTextUnitName(currentId);		
 	}
 
 	protected void startTextUnit(GenericSkeleton startMarker,
 			List<PropertyTextUnitPlaceholder> propertyTextUnitPlaceholders) {
 		eventBuilder.startTextUnit(startMarker, propertyTextUnitPlaceholders);
 		setTextUnitName(currentId);
 	}
 
 	protected void endTextUnit(GenericSkeleton endMarker) {
 		eventBuilder.endTextUnit(endMarker);
 	}
 
 	protected void endGroup(GenericSkeleton endMarker) {
 		eventBuilder.endGroup(endMarker);
 	}
 
 	protected void startTextUnit() {
 		eventBuilder.startTextUnit();
 		setTextUnitName(currentId);
 	}
 
 	protected long getTextUnitId() {
 		return eventBuilder.getTextUnitId();
 	}
 
 	protected void setTextUnitId(long id) {
 		eventBuilder.setTextUnitId(id);
 	}
 	
 	protected void setTextUnitMimeType(String mimeType) {
 		eventBuilder.setTextUnitMimeType(mimeType);
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
 	
 	protected Event popTempEvent() {
 		return eventBuilder.popTempEvent();
 	}
 
 	protected Event peekTempEvent() {
 		return eventBuilder.peekTempEvent();
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
