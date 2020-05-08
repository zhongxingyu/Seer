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
 
 package net.sf.okapi.filters.versifiedtxt;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.sf.okapi.common.BOMNewlineEncodingDetector;
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.UsingParameters;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.encoder.EncoderManager;
 import net.sf.okapi.common.exceptions.OkapiIOException;
 import net.sf.okapi.common.filters.AbstractFilter;
 import net.sf.okapi.common.filters.EventBuilder;
 import net.sf.okapi.common.filters.FilterConfiguration;
 import net.sf.okapi.common.filters.IFilter;
 import net.sf.okapi.common.filterwriter.GenericFilterWriter;
 import net.sf.okapi.common.filterwriter.IFilterWriter;
 import net.sf.okapi.common.resource.Code;
 import net.sf.okapi.common.resource.ITextUnit;
 import net.sf.okapi.common.resource.RawDocument;
 import net.sf.okapi.common.resource.StartSubDocument;
 import net.sf.okapi.common.resource.TextFragment.TagType;
 import net.sf.okapi.common.skeleton.GenericSkeleton;
 
 /**
  * {@link IFilter} for a Versified text file.
  * 
  * @author HARGRAVEJE
  * @author HiginbothamDW
  */
 @UsingParameters()
 // No parameters
 public class VersifiedTextFilter extends AbstractFilter {
 	private static final Logger LOGGER = Logger.getLogger(VersifiedTextFilter.class.getName());
 	private static final int BUFFER_SIZE = 128000;
 
 	public static final String VERSIFIED_TXT_MIME_TYPE = "text/x-versified-txt";
 
 	private static final String VERSE = "^\\|v.+$";
 	private static final String CHAPTER = "^\\|c.+$";
 	private static final String BOOK = "^\\|b.+$";
 	private static final String TARGET = "^<TARGET>$";
 	private static final String PLACEHOLDER = "(\\{|<)[0-9]+(\\}|>)";
 	private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile(PLACEHOLDER);
 
 	private String newline = "\n";
 	private String currentChapter;
 	private String currentBook;
 	private int currentChar;
 	private EventBuilder eventBuilder;
 	private EncoderManager encoderManager;
 	private boolean hasUtf8Bom;
 	private boolean hasUtf8Encoding;
 	private BufferedReader versifiedFileReader;
 	private RawDocument currentRawDocument;
 	private BOMNewlineEncodingDetector detector;
 	private StartSubDocument startSubDocument;
 	private Parameters params;
 	private StringBuilder filterBuffer;
 
 	/** Creates a new instance of VersifiedCodeNgramIndexer */
 	public VersifiedTextFilter() {
 		super();		
 
 		this.currentChapter = "";
 		this.currentBook = "";		
 
 		setMimeType(VERSIFIED_TXT_MIME_TYPE);
 		setMultilingual(false); // default value, could be multilingual we check below
 		setFilterWriter(new GenericFilterWriter(createSkeletonWriter(), getEncoderManager()));	
 		// Cannot use '_' or '-' in name: conflicts with other filters (e.g. plaintext, table)
 		// for defining different configurations
 		setName("okf_versifiedtxt"); //$NON-NLS-1$
 		setDisplayName("Versified Text Filter"); //$NON-NLS-1$
 		addConfiguration(new FilterConfiguration(getName(), VERSIFIED_TXT_MIME_TYPE, getClass()
 				.getName(), "Versified Text", "Versified Text Documents"));
 		setParameters(new Parameters());
 	}
 
 	@Override
 	public IFilterWriter createFilterWriter() {
 		return super.createFilterWriter();
 	}
 
 	@Override
 	public void open(RawDocument input) {
 		open(input, true);
 	}
 
 	@Override
 	public void open(RawDocument input, boolean generateSkeleton) {
 		// close any previous streams we opened
 		close();
 
 		this.currentRawDocument = input;
 		this.currentChapter = "";
 		this.currentBook = "";
 		this.currentChar = -2;
 		filterBuffer = new StringBuilder(BUFFER_SIZE - 1);
 		
 		if (input.getInputURI() != null) {
 			setDocumentName(input.getInputURI().getPath());
 		}
 
 		detector = new BOMNewlineEncodingDetector(input.getStream(), input.getEncoding());
 		detector.detectAndRemoveBom();
 
 		setEncoding(input.getEncoding());
 		hasUtf8Bom = detector.hasUtf8Bom();
 		hasUtf8Encoding = detector.hasUtf8Encoding();
 		newline = detector.getNewlineType().toString();
 		setNewlineType(newline);
 
 		// set encoding to the user setting
 		String detectedEncoding = getEncoding();
 
 		// may need to override encoding based on what we detect
 		if (detector.isDefinitive()) {
 			detectedEncoding = detector.getEncoding();
 			LOGGER.log(Level.FINE, String.format(
 					"Overridding user set encoding (if any). Setting auto-detected encoding (%s).",
 					detectedEncoding));
 		} else if (!detector.isDefinitive() && getEncoding().equals(RawDocument.UNKOWN_ENCODING)) {
 			detectedEncoding = detector.getEncoding();
 			LOGGER.log(
 					Level.FINE,
 					String.format(
 							"Default encoding and detected encoding not found. Using best guess encoding (%s)",
 							detectedEncoding));
 		}
 
 		input.setEncoding(detectedEncoding);
 		setEncoding(detectedEncoding);
 		setOptions(input.getSourceLocale(), input.getTargetLocale(), detectedEncoding,
 				generateSkeleton);
 
 		versifiedFileReader = new BufferedReader(input.getReader());
 		
 		// is the format multilingual?
 		String line = "";		
 		int bufferCount = 0;
 		try {
 			versifiedFileReader.mark(BUFFER_SIZE);
 			while ((line = versifiedFileReader.readLine()) != null) {
 				bufferCount += line.length();
 				if (bufferCount >= BUFFER_SIZE) {
 					break;
 				}
 				if (line.matches(TARGET)) {
 					setMultilingual(true);
 					break;
 				}
 			}
 			versifiedFileReader.reset();
 		} catch (IOException e) {
 			throw new OkapiIOException("IO error detecting if file is multilingual: "
 					+ (line == null ? "unkown line" : line), e);
 		}		
 		
 		// create EventBuilder with document name as rootId
 		if (eventBuilder == null) {
 			eventBuilder = new EventBuilder();
 		} else {
 			eventBuilder.reset(null, isSubFilter());
 		}
 	}
 
 	@Override
 	public void close() {
 		if (currentRawDocument != null) {
 			currentRawDocument.close();
 		}
 
 		if (versifiedFileReader != null) {
 			try {
 				versifiedFileReader.close();
 			} catch (IOException e) {
 				LOGGER.log(Level.WARNING, "Error closing the versified text buffered reader.", e);
 
 			}
 		}
 	}
 
 	@Override
 	public EncoderManager getEncoderManager() {
 		if (encoderManager == null) {
 			encoderManager = new EncoderManager();
 			encoderManager.setMapping(VERSIFIED_TXT_MIME_TYPE,
 					"net.sf.okapi.common.encoder.DefaultEncoder");
 		}
 		return encoderManager;
 	}
 
 	@Override
 	public IParameters getParameters() {
 		return params;
 	}
 
 	@Override
 	public void setParameters(IParameters params) {
 		this.params = (Parameters) params;
 	}
 
 	@Override
 	public boolean hasNext() {
 		return eventBuilder.hasNext();
 	}
 
 	@Override
 	public Event next() {
 		String currentLine = null;
 
 		// process queued up events before we produce more
 		while (eventBuilder.hasQueuedEvents()) {
 			return eventBuilder.next();
 		}
 
 		// loop over versified file one character at a time
 		while (currentChar != -1 && !isCanceled()) {
 			try {
 				currentChar = versifiedFileReader.read();
 				filterBuffer.append((char)currentChar);
 				if (currentChar == '\r' || currentChar == '\n' || currentChar == -1) {
 					filterBuffer.setLength(filterBuffer.length() - 1);
 					currentLine = filterBuffer.toString();
 					currentLine = Util.trimEnd(currentLine, "\r\n");
 					filterBuffer = new StringBuilder(BUFFER_SIZE-1);
 
 					// break early if we have no more text
 					if (currentChar == -1 && currentLine.isEmpty()) {
 						break;
 					}
 
 					// don't output newline if this is the last text in the file
 					newline = handleNewline();
 					if (currentChar == -1) {
 						newline = "";
 					}
 
 					if (currentLine.matches(VERSE)) {
 						handleDocumentPart(currentLine + newline);
 						handleVerse(versifiedFileReader, currentLine, currentLine.substring(2));
 					} else if (currentLine.matches(BOOK)) {
 						currentBook = currentLine.substring(2);
 						setDocumentName(currentBook);
 						eventBuilder.addFilterEvent(createStartFilterEvent());
 						handleDocumentPart(currentLine + newline);
 					} else if (currentLine.matches(CHAPTER)) {
 						currentChapter = currentLine.substring(2);
 						if (startSubDocument != null) {
 							eventBuilder.endSubDocument();
 						}
 						handleSubDocument(currentChapter);
 						handleDocumentPart(currentLine + newline);
 					} else {
 						handleDocumentPart(currentLine + newline);
 					}
 
 					// break if we have produced at least one event
 					if (eventBuilder.hasQueuedEvents()) {
 						break;
 					}
 				}
 			} catch (IOException e) {
 				throw new OkapiIOException("IO error reading versified file at: "
 						+ (currentLine == null ? "unkown line" : currentLine), e);
 			}
 		} 
 
 		if (currentChar == -1) {
 			// reached the end of the file
 			if (startSubDocument != null) {
 				eventBuilder.endSubDocument();
 			}
 			eventBuilder.flushRemainingTempEvents();
 			eventBuilder.addFilterEvent(createEndFilterEvent());
 		}
 
 		return eventBuilder.next();
 	}
 
 	@Override
 	protected boolean isUtf8Bom() {
 		return hasUtf8Bom;
 	}
 
 	@Override
 	protected boolean isUtf8Encoding() {
 		return hasUtf8Encoding;
 	}
 	
 	private String handleNewline() throws IOException {
 		String newline = "\n";
 		switch (detector.getNewlineType()) {
 		case CR:
 			newline = "\r";
 			break;
 		case CRLF:
 			newline = "\r\n";
 			// eat the \n
 			versifiedFileReader.read();
 			break;
 		case LF:
 			newline = "\n";
 			break;
 		}
 		return newline;
 	}
 
 	private void handleSubDocument(String chapter) {
 		startSubDocument = eventBuilder.startSubDocument();
 		startSubDocument.setName(chapter);
 	}
 
 	private void handleVerse(BufferedReader verse, String currentVerse, String verseNumber)
 			throws IOException {
 		String currentLine = null;
 		StringBuilder source = new StringBuilder(BUFFER_SIZE);
 		StringBuilder target = new StringBuilder(BUFFER_SIZE);
 		boolean trg = false;
 		
 		verse.mark(BUFFER_SIZE);
 		while (currentChar != -1) {			
 			try {
 				currentChar = versifiedFileReader.read();				
 				filterBuffer.append((char)currentChar);
 				if (currentChar == '\r' || currentChar == '\n' || currentChar == -1) {
 					filterBuffer.setLength(filterBuffer.length() - 1);
 					currentLine = filterBuffer.toString();
 					currentLine = Util.trimEnd(currentLine, "\r\n");
 					filterBuffer = new StringBuilder(BUFFER_SIZE - 1);
 					
 					// newline is always normalized to \n inside TextUnit except for skeleton
 					newline = handleNewline();
 
 					if (currentLine.matches(VERSE) || currentLine.matches(BOOK) || currentLine.matches(CHAPTER)) {						
 						verse.reset();
 						break;
 					}
 
 					if (currentLine.matches(TARGET)) {
 						trg = true;			
 						continue;
 					}
 					
 					if (trg) {
 						target.append(currentLine + "\n");
 					} else {
 						source.append(currentLine + "\n");
 					}
 					verse.mark(BUFFER_SIZE);
 				}
 			} catch (IOException e) {
 				throw new OkapiIOException("IO error reading versified file at: "
 						+ (currentLine == null ? "unkown line" : currentLine), e);
 			}
 		}
 
 		eventBuilder.startTextUnit();
 
 		// assume any newlines after the final content goes with the string
 		// but we have to at least remove the extra newline added above
 		String s = source.toString().replaceFirst("\n", "");
 		String t = target.toString().replaceFirst("\n", "");		
 		if (currentChar != -1) {
 			if (trg) {
 				s = s.replaceFirst("\n", "");
 				t = t.replaceFirst("\n", "");
 			} else {
 				s = s.replaceFirst("\n", "").replaceFirst("\n", "");
 			}
 		}
 		
 		processPlaceHolders(s, true);
 		if (trg) {
 			processPlaceHolders(t, false);
 		}
 				
 		// reset for source processing
 		eventBuilder.setTargetLocale(null);
 		ITextUnit tu = eventBuilder.peekMostRecentTextUnit();
 		
 		// if this was a bilingual verse then setup the <TARGET> tag
 		// as skeleton
 		GenericSkeleton skel = new GenericSkeleton();
 		skel.addContentPlaceholder(tu);			
 		if (trg) { // bilingual case			 						 
 			skel.add(newline + "<TARGET>" + newline);
 			skel.addContentPlaceholder(tu, getTrgLoc());			 						
 		} 		
 		// always two newlines after final string of the verse no matter mono or bilingual
 		// not not if its the final string
 		if (currentChar != -1) {			
 			skel.add(newline + newline); 
 		}
 		tu.setSkeleton(skel);
 		
 		tu.setName(currentBook + ":" + currentChapter + ":" + verseNumber);
 		tu.setId(currentChapter + (currentChapter != null && currentChapter.isEmpty() ? "" : ":") + verseNumber);
 		eventBuilder.endTextUnit();
 	}
 
 	private void processPlaceHolders(String text, boolean source) {
 		if (source) {
 			eventBuilder.setTargetLocale(null);
 		} else {
 			eventBuilder.setTargetLocale(getTrgLoc());
 		}
 
 		Matcher m = PLACEHOLDER_PATTERN.matcher(text);
 		if (m.find()) {
 			m.reset();
 			String[] chunks = PLACEHOLDER_PATTERN.split(text);
 			for (int i = 0; i < chunks.length; i++) {
 				eventBuilder.addToTextUnit(chunks[i]);
 				if (m.find()) {
 					String ph = text.substring(m.start(), m.end());
 					eventBuilder.addToTextUnit(new Code(TagType.PLACEHOLDER, ph, ph));
 				}
 			}
 		} else {
 			// no placeholders found - treat is text only
 			eventBuilder.addToTextUnit(text);
 		}
 	}
 
 	private void handleDocumentPart(String part) {
 		eventBuilder.addDocumentPart(part);
 	}	
 }
