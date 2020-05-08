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
 
 package net.sf.okapi.filters.xliff;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Stack;
 import java.util.logging.Logger;
 
 import javax.xml.XMLConstants;
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLStreamConstants;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 
 import net.sf.okapi.common.BOMNewlineEncodingDetector;
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.EventType;
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.MimeTypeMapper;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.exceptions.OkapiIllegalFilterOperationException;
 import net.sf.okapi.common.exceptions.OkapiIOException;
 import net.sf.okapi.common.filters.FilterConfiguration;
 import net.sf.okapi.common.filters.IFilter;
 import net.sf.okapi.common.filterwriter.GenericFilterWriter;
 import net.sf.okapi.common.filterwriter.IFilterWriter;
 import net.sf.okapi.common.resource.AltTransAnnotation;
 import net.sf.okapi.common.resource.Code;
 import net.sf.okapi.common.resource.DocumentPart;
 import net.sf.okapi.common.resource.Ending;
 import net.sf.okapi.common.resource.RawDocument;
 import net.sf.okapi.common.resource.Property;
 import net.sf.okapi.common.resource.StartDocument;
 import net.sf.okapi.common.resource.StartGroup;
 import net.sf.okapi.common.resource.StartSubDocument;
 import net.sf.okapi.common.resource.TextContainer;
 import net.sf.okapi.common.resource.TextFragment;
 import net.sf.okapi.common.resource.TextUnit;
 import net.sf.okapi.common.resource.TextFragment.TagType;
 import net.sf.okapi.common.skeleton.GenericSkeleton;
 import net.sf.okapi.common.skeleton.GenericSkeletonWriter;
 import net.sf.okapi.common.skeleton.ISkeletonWriter;
 
 import org.codehaus.stax2.XMLInputFactory2;
 
 public class XLIFFFilter implements IFilter {
 
 	private final Logger logger = Logger.getLogger(getClass().getName());
 	
 	private boolean hasNext;
 	private XMLStreamReader reader;
 	private String docName;
 	private int tuId;
 	private int otherId;
 	private int groupId;
 	private String srcLang;
 	private String trgLang;
 	private LinkedList<Event> queue;
 	private boolean canceled;
 	private GenericSkeleton skel;
 	private TextUnit tu;
 	private boolean approved;
 	private Parameters params;
 	private boolean sourceDone;
 	private boolean targetDone;
 	private TextContainer content;
 	private String encoding;
 	private Stack<Integer> parentIds;
 	private AltTransAnnotation altTrans;
 	private Stack<Boolean> preserveSpaces;
 	private String lineBreak;
 	private boolean hasUTF8BOM;
 	
 	public XLIFFFilter () {
 		params = new Parameters();
 	}
 	
 	public void cancel () {
 		canceled = true;
 	}
 
 	public void close () {
 		try {
 			if ( reader != null ) {
 				reader.close();
 				reader = null;
 			}
 			hasNext = false;
 		}
 		catch ( XMLStreamException e) {
 			throw new OkapiIOException(e);
 		}
 	}
 
 	public String getName () {
 		return "okf_xliff";
 	}
 
 	public String getDisplayName () {
 		return "XLIFF Filter";
 	}
 
 	public String getMimeType () {
 		return MimeTypeMapper.XLIFF_MIME_TYPE;
 	}
 
 	public List<FilterConfiguration> getConfigurations () {
 		List<FilterConfiguration> list = new ArrayList<FilterConfiguration>();
 		list.add(new FilterConfiguration(getName(),
 			MimeTypeMapper.XLIFF_MIME_TYPE,
 			getClass().getName(),
 			"XLIFF",
 			"Configuration for XML Localisation Interchange File Format (XLIFF) document."));
 		return list;
 	}
 	
 	public IParameters getParameters () {
 		return params;
 	}
 
 	public boolean hasNext () {
 		return hasNext;
 	}
 
 	public Event next () {
 		try {
 			// Check for cancellation first
 			if ( canceled ) {
 				queue.clear();
 				queue.add(new Event(EventType.CANCELED));
 				hasNext = false;
 			}
 			
 			// Parse next if nothing in the queue
 			if ( queue.isEmpty() ) {
 				if ( !read() ) {
 					Ending ending = new Ending(String.valueOf(++otherId));
 					ending.setSkeleton(skel);
 					queue.add(new Event(EventType.END_DOCUMENT, ending));
 				}
 			}
 			
 			// Return the head of the queue
 			if ( queue.peek().getEventType() == EventType.END_DOCUMENT ) {
 				hasNext = false;
 			}
 			return queue.poll();
 		}
 		catch ( XMLStreamException e ) {
 			throw new OkapiIOException(e);
 		}
 	}
 
 	public void open (RawDocument input) {
 		open(input, true);
 	}
 	
 /*	public void open (RawDocument input,
 		boolean generateSkeleton)
 	{
 		setOptions(input.getSourceLanguage(), input.getTargetLanguage(),
 			input.getEncoding(), generateSkeleton);
 		if ( input.getInputCharSequence() != null ) {
 			open(input.getInputCharSequence());
 		}
 		else if ( input.getInputURI() != null ) {
 			open(input.getInputURI());
 		}
 		else if ( input.getInputStream() != null ) {
 			open(input.getInputStream());
 		}
 		else {
 			throw new OkapiBadFilterInputException("RawDocument has no input defined.");
 		}
 	}
 	
 	private void open (InputStream input) {
 		commonOpen(0, input);
 	}
 	
 	private void open (CharSequence inputText) {
 		docName = null;
 		encoding = "UTF-16";
 		hasUTF8BOM = false;
 		lineBreak = BOMNewlineEncodingDetector.getNewlineType(inputText).toString();
 		commonOpen(1, new StringReader(inputText.toString()));
 	}
 
 	private void open (URI inputURI) {
 		try {
 			docName = inputURI.getPath();
 			commonOpen(0, inputURI.toURL().openStream());
 		}
 		catch ( IOException e ) {
 			throw new OkapiIOException(e);
 		}
 	}
 */
 	
 	public void open (RawDocument input,
 		boolean generateSkeleton)
 	{
 		try {
 			close();
 			canceled = false;
 
 			XMLInputFactory fact = XMLInputFactory.newInstance();
 			fact.setProperty(XMLInputFactory.IS_COALESCING, true);
 			fact.setProperty(XMLInputFactory2.P_REPORT_PROLOG_WHITESPACE, true);
 			
 			//fact.setXMLResolver(new DefaultXMLResolver());
 			//TODO: Resolve the re-construction of the DTD, for now just skip it
 			fact.setProperty(XMLInputFactory.SUPPORT_DTD, false);
 
 			// Determine encoding based on BOM, if any
 			input.setEncoding("UTF-8"); // Default for XML, other should be auto-detected
 			BOMNewlineEncodingDetector detector = new BOMNewlineEncodingDetector(input.getStream(), input.getEncoding());
 			detector.detectBom();
 //TODO: We need to let the XMLStreamReader detect its encoding, only provide a fall back.
 //We could just not use BOMNewlineEncodingDetector, but then we would not have info on BOM and linebreak type.
 			input.setEncoding(detector.getEncoding());
 			reader = fact.createXMLStreamReader(input.getReader());
 
 			String realEnc = reader.getCharacterEncodingScheme();
 			if ( realEnc != null ) encoding = realEnc;
 			else encoding = input.getEncoding();
 
 			srcLang = input.getSourceLanguage();
 			if ( srcLang == null ) throw new NullPointerException("Source language not set.");
 			trgLang = input.getTargetLanguage();
 			if ( trgLang == null ) throw new NullPointerException("Target language not set.");
 			hasUTF8BOM = detector.hasUtf8Bom();
 			lineBreak = detector.getNewlineType().toString();
 			if ( input.getInputURI() != null ) {
 				docName = input.getInputURI().getPath();
 			}
 
 			preserveSpaces = new Stack<Boolean>();
 			preserveSpaces.push(false);
 			parentIds = new Stack<Integer>();
 			parentIds.push(0);
 			groupId = 0;
 			tuId = 0;
 			otherId = 0;
 			// Set the start event
 			hasNext = true;
 			queue = new LinkedList<Event>();
 			
 			StartDocument startDoc = new StartDocument(String.valueOf(++otherId));
 			startDoc.setName(docName);
 			startDoc.setEncoding(encoding, hasUTF8BOM);
 			startDoc.setLanguage(srcLang);
 			startDoc.setFilterParameters(getParameters());
 			startDoc.setFilterWriter(createFilterWriter());
 			startDoc.setType(MimeTypeMapper.XLIFF_MIME_TYPE);
 			startDoc.setMimeType(MimeTypeMapper.XLIFF_MIME_TYPE);
 			startDoc.setMultilingual(true);
 			startDoc.setLineBreak(lineBreak);
 			queue.add(new Event(EventType.START_DOCUMENT, startDoc));
 
 			// The XML declaration is not reported by the parser, so we need to
 			// create it as a document part when starting
 			skel = new GenericSkeleton();
 			startDoc.setProperty(new Property(Property.ENCODING, encoding, false));
 			skel.append("<?xml version=\"1.0\" encoding=\"");
 			skel.addValuePlaceholder(startDoc, Property.ENCODING, "");
 			skel.append("\"?>");
 			startDoc.setSkeleton(skel);
 		}
 		catch ( XMLStreamException e) {
 			throw new OkapiIOException(e);
 		}
 	}
 	
 	public void setParameters (IParameters params) {
 		this.params = (Parameters)params;
 	}
 
 	public ISkeletonWriter createSkeletonWriter() {
 		return new GenericSkeletonWriter();
 	}
 
 	public IFilterWriter createFilterWriter () {
 		return new GenericFilterWriter(createSkeletonWriter());
 	}
 
 	private boolean read () throws XMLStreamException {
 		skel = new GenericSkeleton();
 		int eventType;
 		
 		while ( reader.hasNext() ) {
 			eventType = reader.next();
 			switch ( eventType ) {
 			case XMLStreamConstants.START_ELEMENT:
 				String name = reader.getLocalName();
 				if ( "trans-unit".equals(name) ) {
 					return processTransUnit();
 				}
 				else if ( "file".equals(name) ) {
 					return processStartFile();
 				}
 				else if ( "group".equals(name) ) {
 					if ( processStartGroup() ) return true;
 				}
 				else if ( "bin-unit".equals(name) ) {
 					if ( processStartBinUnit() ) return true;
 				}
 				else storeStartElement();
 				break;
 				
 			case XMLStreamConstants.END_ELEMENT:
 				storeEndElement();
 				if ( "file".equals(reader.getLocalName()) ) {
 					return processEndFile();
 				}
 				else if ( "group".equals(reader.getLocalName()) ) {
 					return processEndGroup();
 				}
 				else if ( "bin-unit".equals(reader.getLocalName()) ) {
 					return processEndBinUnit();
 				}
 				break;
 				
 			case XMLStreamConstants.SPACE:
 			case XMLStreamConstants.CDATA:
 				skel.append(reader.getText().replace("\n", lineBreak));
 				break;
 			case XMLStreamConstants.CHARACTERS: //TODO: escape unsupported chars
 				skel.append(Util.escapeToXML(reader.getText().replace("\n", lineBreak), 0, params.getEscapeGT(), null));
 				break;
 				
 			case XMLStreamConstants.COMMENT:
 				skel.append("<!--"+ reader.getText().replace("\n", lineBreak) + "-->");
 				break;
 				
 			case XMLStreamConstants.PROCESSING_INSTRUCTION:
 				skel.append("<?"+ reader.getPITarget() + " " + reader.getPIData() + "?>");
 				break;
 				
 			case XMLStreamConstants.DTD:
 				//TODO: Reconstruct the DTD declaration
 				// but how? nothing is available to do that
 				break;
 				
 			case XMLStreamConstants.ENTITY_REFERENCE:
 			case XMLStreamConstants.ENTITY_DECLARATION:
 			case XMLStreamConstants.NAMESPACE:
 			case XMLStreamConstants.NOTATION_DECLARATION:
 			case XMLStreamConstants.ATTRIBUTE:
 				break;
 			case XMLStreamConstants.START_DOCUMENT:
 				break;
 			case XMLStreamConstants.END_DOCUMENT:
 				break;
 			}
 		}
 		return false;
 	}
 
 	private boolean processStartFile () {
 		// Make a document part with skeleton between the previous event and now.
 		// Spaces can go with the file element to reduce the number of events.
 		// This allows to have only the file skeleton parts with the sub-document event
 		if ( !skel.isEmpty(true) ) {
 			DocumentPart dp = new DocumentPart(String.valueOf(++otherId), false, skel);
 			skel = new GenericSkeleton(); // And create a new skeleton for the next event
 			queue.add(new Event(EventType.DOCUMENT_PART, dp));
 		}
 		
 		StartSubDocument startSubDoc = new StartSubDocument(String.valueOf(++otherId));
 		storeStartElementFile(startSubDoc);
 		
 		String tmp = reader.getAttributeValue("", "original");
 		if ( tmp == null ) throw new OkapiIllegalFilterOperationException("Missing attribute 'original'.");
 		else startSubDoc.setName(tmp);
 		
 		// Check the source language
 		tmp = reader.getAttributeValue("", "source-language");
 		if ( tmp == null ) throw new OkapiIllegalFilterOperationException("Missing attribute 'source-language'.");
 		if ( !Util.isSameLanguage(tmp, srcLang, false) ) { // Warn about source language
 			logger.warning(String.format("The source language declared in <file> is '%s' not '%s'.", tmp, srcLang));
 		}
 		
 		// Check the target language
 		Property prop = startSubDoc.getProperty("targetLanguage");
 		if ( prop != null ) {
 			if ( !Util.isSameLanguage(prop.getValue(), trgLang, false) ) { // Warn about target language
 				logger.warning(String.format("The target language declared in <file> is '%s' not '%s'. '%s' will be used.",
 					prop.getValue(), trgLang, prop.getValue()));
 				trgLang = prop.getValue();
 			}
 		}
 		
 		startSubDoc.setSkeleton(skel);
 		queue.add(new Event(EventType.START_SUBDOCUMENT, startSubDoc));
 		return true;
 	}
 
 	private boolean processEndFile () {
 		Ending ending = new Ending(String.valueOf(+otherId));
 		ending.setSkeleton(skel);
 		queue.add(new Event(EventType.END_SUBDOCUMENT, ending));
 		return true;
 	}
 	
 	private void storeStartElement () {
 		String prefix = reader.getPrefix();
 		if (( prefix == null ) || ( prefix.length()==0 )) {
 			skel.append("<"+reader.getLocalName());
 		}
 		else {
 			skel.append("<"+prefix+":"+reader.getLocalName());
 		}
 
 		int count = reader.getNamespaceCount();
 		for ( int i=0; i<count; i++ ) {
 			prefix = reader.getNamespacePrefix(i);
 			skel.append(String.format(" xmlns%s=\"%s\"",
 				((prefix!=null) ? ":"+prefix : ""),
 				reader.getNamespaceURI(i)));
 		}
 		String attrName;
 		boolean ps = preserveSpaces.peek();
 		
 		count = reader.getAttributeCount();
 		for ( int i=0; i<count; i++ ) {
 			if ( !reader.isAttributeSpecified(i) ) continue; // Skip defaults
 			prefix = reader.getAttributePrefix(i);
 			attrName = String.format("%s%s",
 				(((prefix==null)||(prefix.length()==0)) ? "" : prefix+":"),
 				reader.getAttributeLocalName(i));
 			skel.append(String.format(" %s=\"%s\"", attrName,
 				Util.escapeToXML(reader.getAttributeValue(i).replace("\n", lineBreak), 3, params.getEscapeGT(), null)));
 			if ( attrName.equals("xml:space") ) {
 				ps = reader.getAttributeValue(i).equals("preserve");
 			}
 		}
 		skel.append(">");
 		preserveSpaces.push(ps);
 	}
 	
 	private void storeStartElementFile (StartSubDocument startSubDoc) {
 		String prefix = reader.getPrefix();
 		if (( prefix == null ) || ( prefix.length()==0 )) {
 			skel.append("<"+reader.getLocalName());
 		}
 		else {
 			skel.append("<"+prefix+":"+reader.getLocalName());
 		}
 
 		int count = reader.getNamespaceCount();
 		for ( int i=0; i<count; i++ ) {
 			prefix = reader.getNamespacePrefix(i);
 			skel.append(String.format(" xmlns%s=\"%s\"",
 				((prefix!=null) ? ":"+prefix : ""),
 				reader.getNamespaceURI(i)));
 		}
 		String attrName;
 		boolean ps = preserveSpaces.peek();
 		boolean hasTargetlanguage = false;
 		
 		count = reader.getAttributeCount();
 		for ( int i=0; i<count; i++ ) {
 			if ( !reader.isAttributeSpecified(i) ) continue; // Skip defaults
 			prefix = reader.getAttributePrefix(i);
 			attrName = String.format("%s%s",
 				(((prefix==null)||(prefix.length()==0)) ? "" : prefix+":"),
 				reader.getAttributeLocalName(i));
 			
 			if ( reader.getAttributeLocalName(i).equals("target-language") ) {
 				// Create a property
 				hasTargetlanguage = true;
 				startSubDoc.setProperty(new Property("targetLanguage", reader.getAttributeValue(i), false));
 				skel.append(String.format(" %s=\"", attrName));
 				skel.addValuePlaceholder(startSubDoc, "targetLanguage", "");
 				skel.append("\"");
 			}
 			else {
 				skel.append(String.format(" %s=\"%s\"", attrName,
 					Util.escapeToXML(reader.getAttributeValue(i).replace("\n", lineBreak), 3, params.getEscapeGT(), null)));
 				if ( attrName.equals("xml:space") ) {
 					ps = reader.getAttributeValue(i).equals("preserve");
 				}
 			}
 		}
 		
 		if ( params.getAddTargetLanguage() && !hasTargetlanguage ) {
 			// Create the attribute (as a property) if not there yet
 			startSubDoc.setProperty(new Property("targetLanguage", trgLang, false));
 			skel.append(" target-language=\"");
 			skel.addValuePlaceholder(startSubDoc, "targetLanguage", "");
 			skel.append("\"");
 		}
 		
 		skel.append(">");
 		preserveSpaces.push(ps);
 	}
 	
 	private void storeEndElement () {
 		String prefix = reader.getPrefix();
 		if (( prefix != null ) && ( prefix.length()>0 )) {
 			skel.append("</"+prefix+":"+reader.getLocalName()+">");
 		}
 		else {
 			skel.append("</"+reader.getLocalName()+">");
 		}
 		preserveSpaces.pop();
 	}
 
 	private boolean processTransUnit () {
 		try {
 			// Make a document part with skeleton between the previous event and now.
 			// Spaces can go with trans-unit to reduce the number of events.
 			// This allows to have only the trans-unit skeleton parts with the TextUnit event
 			if ( !skel.isEmpty(true) ) {
 				DocumentPart dp = new DocumentPart(String.valueOf(++otherId), false, skel);
 				skel = new GenericSkeleton(); // And create a new skeleton for the next event
 				queue.add(new Event(EventType.DOCUMENT_PART, dp));
 			}
 			
 			// Process trans-unit
 			sourceDone = false;
 			targetDone = false;
 			altTrans = null;
 			tu = new TextUnit(String.valueOf(++tuId));
 			storeStartElement();
 
 			String tmp = reader.getAttributeValue("", "translate");
 			if ( tmp != null ) tu.setIsTranslatable(tmp.equals("yes"));
 
 			tmp = reader.getAttributeValue("", "id");
 			if ( tmp == null ) throw new OkapiIllegalFilterOperationException("Missing attribute 'id'.");
 			tu.setId(tmp);
 			
 			tmp = reader.getAttributeValue("", "resname");
 			if ( tmp != null ) tu.setName(tmp);
 			else if ( params.getFallbackToID() ) {
 				tu.setName(tu.getId());
 			}
 
 			approved = false;
 			tmp = reader.getAttributeValue("", Property.APPROVED);
 			if (( tmp != null ) && tmp.equals("yes") ) {
 				approved = true;
 			}
 
 			// Set restype (can be null)
 			tu.setType(reader.getAttributeValue("", "restype"));
 			
 			// Get the content
 			int eventType;
 			while ( reader.hasNext() ) {
 				eventType = reader.next();
 				String name;
 				switch ( eventType ) {
 				case XMLStreamConstants.START_ELEMENT:
 					name = reader.getLocalName();
 					if ( "source".equals(name) ) {
 						storeStartElement();
 						processSource(false);
 						storeEndElement();
 					}
 					else if ( "target".equals(name) ) {
 						storeStartElement();
 						processTarget();
 						storeEndElement();
 					}
 					else if ( "seg-source".equals(name) ) {
 						storeStartElement();
 						processSource(true);
 						storeEndElement();
 					}
 					else if ( "note".equals(name) ) {
 						addTargetIfNeeded();
 						storeStartElement();
 						processNote();
 						storeEndElement();
 					}
 					else if ( "alt-trans".equals(name) ) {
 						addTargetIfNeeded();
 						storeStartElement();
 						processStartAltTrans();
 					}
 					else {
 						addTargetIfNeeded();
 						storeStartElement();
 					}
 					break;
 				
 				case XMLStreamConstants.END_ELEMENT:
 					name = reader.getLocalName();
 					addTargetIfNeeded();
 					if ( "trans-unit".equals(name) ) {
 						storeEndElement();
 						tu.setSkeleton(skel);
 						tu.setMimeType(MimeTypeMapper.XLIFF_MIME_TYPE);
 						queue.add(new Event(EventType.TEXT_UNIT, tu));
 						return true;
 					}
 					// Else: just store the end
 					storeEndElement();
 					break;
 				
 				case XMLStreamConstants.SPACE:
 				case XMLStreamConstants.CDATA:
 				case XMLStreamConstants.CHARACTERS:
 					if ( !targetDone ) {
 						// Faster that separating XMLStreamConstants.SPACE
 						// from other data in the all process
 						tmp = reader.getText();
 						for ( int i=0; i<tmp.length(); i++ ) {
 							if ( !Character.isWhitespace(tmp.charAt(i)) ) {
 								addTargetIfNeeded();
 								break;
 							}
 						}
 					}
 					//TODO: escape unsupported chars
 					skel.append(Util.escapeToXML(reader.getText().replace("\n", lineBreak), 0, params.getEscapeGT(), null));
 					break;
 					
 				case XMLStreamConstants.COMMENT:
 					addTargetIfNeeded();
 					skel.append("<!--"+ reader.getText().replace("\n", lineBreak) + "-->");
 					break;
 				
 				case XMLStreamConstants.PROCESSING_INSTRUCTION:
 					addTargetIfNeeded();
 					skel.append("<?"+ reader.getPITarget() + " " + reader.getPIData() + "?>");
 					break;
 				}
 			}
 		}
 		catch ( XMLStreamException e) {
 			throw new OkapiIOException(e);
 		}
 		return false;
 	}
 	
 	private void processSource (boolean isSegSource) {
 		TextContainer tc;
 		if ( sourceDone ) { // Case of an alt-trans entry
 			// Get the language
 			String lang = reader.getAttributeValue(XMLConstants.XML_NS_URI, "lang");
 			if ( lang == null ) lang = srcLang; // Use default
 			// Get the text content
 			tc = processContent(isSegSource ? "seg-source" : "source", true);
 			// Put the source in the alt-trans annotation
 			if ( !preserveSpaces.peek() ) TextFragment.unwrap(tc.getContent());
 			// Store in altTrans only when we are witnin alt-trans
 			if ( altTrans != null ) {
 				if ( isSegSource ) {
 					// TODO: handle seg-source
 					//TODO: content of seg-source should be the one to use???
 					//TODO: what if they are different?
 				}
 				else {
 					altTrans.addNew(lang, tc);
 					altTrans.getEntry().setPreserveWhitespaces(preserveSpaces.peek());
 				}
 			}
 			else { // It's seg-source just after a <source> (not in alt-trans)
 				//TODO: Handle segmented content
 			}
 		}
 		else {
 			// Get the coord attribute if available
 			String tmp = reader.getAttributeValue("", "coord");
 			if ( tmp != null ) {
 				tu.setSourceProperty(new Property(Property.COORDINATES, tmp, false));
 			}
 
 			skel.addContentPlaceholder(tu);
 			tc = processContent(isSegSource ? "seg-source" : "source", false);
 			if ( !preserveSpaces.peek() ) TextFragment.unwrap(tc.getContent());
 			tu.setPreserveWhitespaces(preserveSpaces.peek());
 			tu.setSource(tc);
 			sourceDone = true;
 		}
 	}
 	
 	private void processTarget () {
 		TextContainer tc;
 		if ( targetDone ) { // Case of an alt-trans entry
 			// Get the language
 			String lang = reader.getAttributeValue(XMLConstants.XML_NS_URI, "lang");
 			if ( lang == null ) lang = trgLang; // Use default
 			// Get the text content
 			tc = processContent("target", true);
 			// Put the target in the alt-trans annotation
 			if ( !preserveSpaces.peek() ) TextFragment.unwrap(tc.getContent());
 			altTrans.setTarget(lang, tc);
 			altTrans.getEntry().setPreserveWhitespaces(preserveSpaces.peek());
 		}
 		else {
 			// Get the state attribute if available
 			//TODO: Need to standardize target-state properties
 			String stateValue = reader.getAttributeValue("", "state");
 			// Get the coord attribute if available
 			String coordValue = reader.getAttributeValue("", Property.COORDINATES);
 
 			// Get the target itself
 			skel.addContentPlaceholder(tu, trgLang);
 			tc = processContent("target", false);
 			if ( !tc.isEmpty() ) {
 				//resource.needTargetElement = false;
 				if ( !preserveSpaces.peek() ) TextFragment.unwrap(tc.getContent());
 				tu.setPreserveWhitespaces(preserveSpaces.peek());
 				tu.setTarget(trgLang, tc);
 			}
 
 			// Set the target properties (after the target container has been set
 			if ( stateValue != null ) {
 				tu.setTargetProperty(trgLang, new Property("state", stateValue, false));
 			}
 		
 			if ( coordValue != null ) {
 				tu.setTargetProperty(trgLang, new Property(Property.COORDINATES, coordValue, false));
 			}
 
 			if ( approved ) {
 				// Note that this property is set to the target at the resource-level
 				tu.setTargetProperty(trgLang, new Property(Property.APPROVED, "yes", false));
 			}
 			
 			targetDone = true;
 		}
 	}
 	
 	private void processStartAltTrans () {
 		// Creates an annotation for the alt-trans if there is none yet.
 		if ( altTrans == null ) {
 			altTrans = new AltTransAnnotation();
 			tu.setAnnotation(altTrans);
 		}
 	}
 	
 	private void addTargetIfNeeded () {
 		if ( targetDone ) return; // Nothing to add
 		// If the target language is the same as the source, we should not create new <target>
 		if ( Util.isSameLanguage(srcLang, trgLang, true) ) return; 
 		//Else: this trans-unit has no target, we add it here in the skeleton
 		// so we can merge target data in it when writing out the skeleton
 		skel.append(String.format("<target xml:lang=\"%s\">", trgLang));
 		skel.addContentPlaceholder(tu, trgLang);
 		skel.append("</target>");
 		skel.append(lineBreak);
 		targetDone = true;
 	}
 	
 	/**
 	 * Processes a segment content.
 	 * @param tagName The name of the element content that is being processed.
 	 * @param store True if the data must be stored in the skeleton.
 	 * This is used to merge later on.
 	 * @param inlineCodes Array where to save the in-line codes. Do not save if this parameter
 	 * is set to null.
 	 * @return A new TextContainer object with the parsed content.
 	 */
 	private TextContainer processContent (String tagName,
 		boolean store)
 	{
 		try {
 			content = new TextContainer();
 			int id = 0;
 			Stack<Integer> idStack = new Stack<Integer>();
 			idStack.push(id);
 			int eventType;
 			String name;
 			String tmp;
 			Code code;
 			TextFragment segment = null;
 			int segIdStack = -1;
 			// The current variable points either to content or segment depending on where
 			// we are currently storing the parsed data, the segments are part of the content
 			// at the end, so all can use the same code/skeleton
 			TextFragment current = content;
 			
 			while ( reader.hasNext() ) {
 				eventType = reader.next();
 				switch ( eventType ) {
 				case XMLStreamConstants.CHARACTERS:
 				case XMLStreamConstants.CDATA:
 				case XMLStreamConstants.SPACE:
 					current.append(reader.getText());
 					if ( store ) { //TODO: escape unsupported chars
 						skel.append(Util.escapeToXML(reader.getText(), 0, params.getEscapeGT(), null));
 					}
 					break;
 		
 				case XMLStreamConstants.END_ELEMENT:
 					name = reader.getLocalName();
 					if ( name.equals(tagName) ) {
 						return content;
 					}
 					if ( name.equals("mrk") ) { // Check of end of segment
						if ( idStack.peek() == segIdStack ) {
 							current = content; // Point back to content
							idStack.pop(); // Pop only after test is true
 							segIdStack = -1; // Reset to not trigger segment ending again
 							// Add the segment to the content
 							content.appendSegment(segment); //TODO: mid should be the segid
 							continue;
 						}
 					}
 					// Other cases
 					if ( name.equals("g") || name.equals("mrk") ) {
 						if ( store ) storeEndElement();
 						// Leave the id set to -1 for balancing
 						code = current.append(TagType.CLOSING, name, "");
 						idStack.pop();
 						tmp = reader.getPrefix();
 						if (( tmp != null ) && ( tmp.length()>0 )) {
 							code.setOuterData("</"+tmp+":"+name+">");
 						}
 						else {
 							code.setOuterData("</"+name+">");
 						}
 					}
 					break;
 					
 				case XMLStreamConstants.START_ELEMENT:
 					if ( store ) storeStartElement();
 					name = reader.getLocalName();
 					if ( name.equals("mrk") ) { // Check for start of segment
 						String type = reader.getAttributeValue(null, "mtype");
 						if (( type != null ) && ( type.equals("seg") )) {
 							idStack.push(++id);
 							segIdStack = id;
 							segment = new TextFragment();
 							current = segment; // Segment is now being built
 							continue;
 						}
 					}
 					// Other cases
 					if ( name.equals("g") || name.equals("mrk") ) {
 						id = retrieveId(id, reader.getAttributeValue(null, "id"));
 						idStack.push(id);
 						code = current.append(TagType.OPENING, name, "", id);
 						// Get the outer code
 						String prefix = reader.getPrefix();
 						StringBuilder tmpg = new StringBuilder();
 						if (( prefix != null ) && ( prefix.length()>0 )) {
 							tmpg.append("<"+prefix+":"+reader.getLocalName());
 						}
 						else {
 							tmpg.append("<"+reader.getLocalName());
 						}
 						int count = reader.getNamespaceCount();
 						for ( int i=0; i<count; i++ ) {
 							prefix = reader.getNamespacePrefix(i);
 							tmpg.append(String.format(" xmlns:%s=\"%s\"",
 								((prefix!=null) ? ":"+prefix : ""),
 								reader.getNamespaceURI(i)));
 						}
 						count = reader.getAttributeCount();
 						for ( int i=0; i<count; i++ ) {
 							if ( !reader.isAttributeSpecified(i) ) continue; // Skip defaults
 							prefix = reader.getAttributePrefix(i); 
 							tmpg.append(String.format(" %s%s=\"%s\"",
 								(((prefix==null)||(prefix.length()==0)) ? "" : prefix+":"),
 								reader.getAttributeLocalName(i),
 								reader.getAttributeValue(i)));
 						}
 						tmpg.append(">");
 						code.setOuterData(tmpg.toString());
 					}
 					else if ( name.equals("x") ) {
 						id = retrieveId(id, reader.getAttributeValue(null, "id"));
 						appendCode(TagType.PLACEHOLDER, id, name, name, store, current);
 					}
 					else if ( name.equals("bx") ) {
 						id = retrieveId(id, reader.getAttributeValue(null, "id"));
 						appendCode(TagType.OPENING, id, name, "Xpt", store, current);
 					}
 					else if ( name.equals("ex") ) {
 						// No support for overlapping codes yet
 						appendCode(TagType.CLOSING, -1, name, "Xpt", store, current);
 					}
 					else if ( name.equals("bpt") ) {
 						id = retrieveId(id, reader.getAttributeValue(null, "id"));
 						appendCode(TagType.OPENING, id, name, "Xpt", store, current);
 					}
 					else if ( name.equals("ept") ) {
 						// We assume balanced codes for now, so we use the current one
 						//TODO: Change this to handle overlapping cases
 						appendCode(TagType.CLOSING, -1, name, "Xpt", store, current);
 					}
 					else if ( name.equals("ph") ) {
 						id = retrieveId(id, reader.getAttributeValue(null, "id"));
 						appendCode(TagType.PLACEHOLDER, id, name, name, store, current);
 					}
 					else if ( name.equals("it") ) {
 						id = retrieveId(id, reader.getAttributeValue(null, "id"));
 						tmp = reader.getAttributeValue(null, "pos");
 						TagType tt = TagType.PLACEHOLDER;
 						if ( tmp == null ) {
 							logger.severe("Missing pos attribute for <it> element.");
 						}
 						else if ( tmp.equals("close") ) {
 							tt = TagType.CLOSING;
 						}
 						else if ( tmp.equals("open") ) {
 							tt = TagType.OPENING;
 						}
 						else {
 							logger.severe(String.format("Invalid value '%s' for pos attribute.", tmp));
 						}
 						appendCode(tt, id, name, name, store, current);
 					}
 					break;
 				}
 			}
 			
 			// current should be content at the end
 			return content;
 		}
 		catch ( XMLStreamException e) {
 			throw new OkapiIOException(e);
 		}
 	}
 
 	private int retrieveId (int currentIdValue,
 		String id)
 	{
 		if (( id == null ) || ( id.length() == 0 )) return ++currentIdValue;
 		try {
 			return Integer.valueOf(id);
 		}
 		catch ( NumberFormatException e ) {
 			// Falls back to the has-code
 			//TODO: At some point code id needs to support a string
 			return id.hashCode();
 		}
 	}
 	
 	/**
 	 * Appends a code, using the content of the node. Do not use for <g>-type tags.
 	 * @param type The type of in-line code.
 	 * @param id the id of the code to add.
 	 * @param tagName the tag name of the in-line element to process.
 	 * @param type the type of code (bpt and ept must use the same one so they can match!) 
 	 * @param store true if we need to store the data in the skeleton.
 	 * @param content the object where to put the code.
 	 * @param inlineCodes array where to save the original in-line code.
 	 * Do not save if this parameter is null.
 	 */
 	private void appendCode (TagType tagType,
 		int id,
 		String tagName,
 		String type,
 		boolean store,
 		TextFragment content)
 	{
 		try {
 			int endStack = 1;
 			StringBuilder innerCode = new StringBuilder();
 			StringBuilder outerCode = null;
 			outerCode = new StringBuilder();
 			outerCode.append("<"+tagName);
 			int count = reader.getAttributeCount();
 			String prefix;
 			for ( int i=0; i<count; i++ ) {
 				if ( !reader.isAttributeSpecified(i) ) continue; // Skip defaults
 				prefix = reader.getAttributePrefix(i); 
 				outerCode.append(String.format(" %s%s=\"%s\"",
 					(((prefix==null)||(prefix.length()==0)) ? "" : prefix+":"),
 					reader.getAttributeLocalName(i),
 					reader.getAttributeValue(i)));
 			}
 			outerCode.append(">");
 			
 			int eventType;
 			while ( reader.hasNext() ) {
 				eventType = reader.next();
 				switch ( eventType ) {
 				case XMLStreamConstants.START_ELEMENT:
 					if ( store ) storeStartElement();
 					StringBuilder tmpg = new StringBuilder();
 					if ( reader.getLocalName().equals("sub") ) {
 						logger.warning("A <sub> element was detected. It will be included in its parent code as <sub> is currently not supported.");
 					}
 					else if ( tagName.equals(reader.getLocalName()) ) {
 						endStack++; // Take embedded elements into account 
 					}
 					prefix = reader.getPrefix();
 					if (( prefix == null ) || ( prefix.length()==0 )) {
 						tmpg.append("<"+reader.getLocalName());
 					}
 					else {
 						tmpg.append("<"+prefix+":"+reader.getLocalName());
 					}
 					count = reader.getNamespaceCount();
 					for ( int i=0; i<count; i++ ) {
 						prefix = reader.getNamespacePrefix(i);
 						tmpg.append(String.format(" xmlns:%s=\"%s\"",
 							((prefix!=null) ? ":"+prefix : ""),
 							reader.getNamespaceURI(i)));
 					}
 					count = reader.getAttributeCount();
 					for ( int i=0; i<count; i++ ) {
 						if ( !reader.isAttributeSpecified(i) ) continue; // Skip defaults
 						prefix = reader.getAttributePrefix(i); 
 						tmpg.append(String.format(" %s%s=\"%s\"",
 							(((prefix==null)||(prefix.length()==0)) ? "" : prefix+":"),
 							reader.getAttributeLocalName(i),
 							reader.getAttributeValue(i)));
 					}
 					tmpg.append(">");
 					innerCode.append(tmpg.toString());
 					outerCode.append(tmpg.toString());
 					break;
 					
 				case XMLStreamConstants.END_ELEMENT:
 					if ( store ) storeEndElement();
 					if ( tagName.equals(reader.getLocalName()) ) {
 						if ( --endStack == 0 ) {
 							Code code = content.append(tagType, type, innerCode.toString(), id);
 							outerCode.append("</"+tagName+">");
 							code.setOuterData(outerCode.toString());
 							return;
 						}
 						// Else: fall thru
 					}
 					// Else store the close tag in the outer code
 					prefix = reader.getPrefix();
 					if (( prefix == null ) || ( prefix.length()==0 )) {
 						innerCode.append("</"+reader.getLocalName()+">");
 						outerCode.append("</"+reader.getLocalName()+">");
 					}
 					else {
 						innerCode.append("</"+prefix+":"+reader.getLocalName()+">");
 						outerCode.append("</"+prefix+":"+reader.getLocalName()+">");
 					}
 					break;
 
 				case XMLStreamConstants.CHARACTERS:
 				case XMLStreamConstants.CDATA:
 				case XMLStreamConstants.SPACE:
 					innerCode.append(reader.getText());//TODO: escape unsupported chars
 					outerCode.append(Util.escapeToXML(reader.getText(), 0, params.getEscapeGT(), null));
 					if ( store ) //TODO: escape unsupported chars
 						skel.append(Util.escapeToXML(reader.getText(), 0, params.getEscapeGT(), null));
 					break;
 				}
 			}
 		}
 		catch ( XMLStreamException e) {
 			throw new OkapiIOException(e);
 		}
 	}
 	
 	private void processNote () {
 		//TODO: Handle 'annotates', etc.
 		try {
 			// Check the destination of the property
 			String dest = reader.getAttributeValue("", "annotates");
 			if ( dest == null ) dest = ""; // like 'general'
 			Property prop = null;
 			StringBuilder tmp = new StringBuilder();
 			if ( dest.equals("source") ) {
 				prop = tu.getSourceProperty(Property.NOTE);
 			}
 			else if ( dest.equals("target") ) {
 				prop = tu.getTargetProperty(trgLang, Property.NOTE);
 			}
 			else {
 				prop = tu.getProperty(Property.NOTE);
 			}
 			if ( prop == null ) {
 				prop = new Property(Property.NOTE, "", true);
 			}
 			else {
 				tmp.append(prop.getValue());
 				tmp.append("\n---\n");
 			}
 
 			// Get the content
 			int eventType;
 			while ( reader.hasNext() ) {
 				eventType = reader.next();
 				switch ( eventType ) {
 				case XMLStreamConstants.CHARACTERS:
 				case XMLStreamConstants.CDATA:
 				case XMLStreamConstants.SPACE: //TODO: escape unsupported chars
 					skel.append(Util.escapeToXML(reader.getText(), 0, params.getEscapeGT(), null));
 					tmp.append(reader.getText());
 					break;
 				case XMLStreamConstants.END_ELEMENT:
 					String name = reader.getLocalName();
 					if ( name.equals("note") ) {
 						prop.setValue(tmp.toString());
 						if ( dest.equals("source") ) {
 							tu.setSourceProperty(prop);
 						}
 						else if ( dest.equals("target") ) {
 							tu.setTargetProperty(trgLang, prop);
 						}
 						else {
 							tu.setProperty(prop);
 						}
 						return;
 					}
 					// Else: This should be an error as note are text only.
 					break;
 				}
 			}
 		}
 		catch ( XMLStreamException e) {
 			throw new OkapiIOException(e);
 		}
 	}
 
 	private boolean processStartGroup () {
 		storeStartElement();
 		// Check if it's a 'merge-trans' group (v1.2)
 		String tmp = reader.getAttributeValue("", "merge-trans");
 		if ( tmp != null ) {
 			// If it's a 'merge-trans' group we do not treat it as a normal group.
 			// The group element was not generated by the extractor.
 			if ( tmp.compareTo("yes") == 0 ) {
 				parentIds.push(-1);
 				return false;
 			}
 		}
 		
 		// Else: it's a structural group
 		StartGroup group = new StartGroup(parentIds.peek().toString(),
 			String.valueOf(++groupId));
 		group.setSkeleton(skel);
 		parentIds.push(groupId);
 		queue.add(new Event(EventType.START_GROUP, group));
 
 		// Get resname (can be null)
 		tmp = reader.getAttributeValue("", "resname");
 		if ( tmp != null ) group.setName(tmp);
 		else if ( params.getFallbackToID() ) {
 			group.setName(reader.getAttributeValue("", "id"));
 		}
 
 		// Get restype (can be null)
 		group.setType(reader.getAttributeValue("", "restype"));
 		return true;
 	}
 	
 	private boolean processEndGroup () {
 		// Pop and checks the value for this group
 		int id = parentIds.pop();
 		if ( id == -1 ) {
 			// This closes a 'merge-trans' non-structural group
 			return false;
 		}
 
 		// Else: it's a structural group
 		Ending ending = new Ending(String.valueOf(id));
 		ending.setSkeleton(skel);
 		queue.add(new Event(EventType.END_GROUP, ending));
 		return true;
 	}
 
 	private boolean processStartBinUnit () {
 		storeStartElement();
 
 		String tmp = reader.getAttributeValue("", "id");
 		if ( tmp == null ) throw new OkapiIllegalFilterOperationException("Missing attribute 'id'.");
 
 		StartGroup group = new StartGroup(parentIds.peek().toString(),
 			String.valueOf(++groupId));
 		group.setSkeleton(skel);
 		parentIds.push(groupId);
 		queue.add(new Event(EventType.START_GROUP, group));
 
 		// Get id for resname
 		tmp = reader.getAttributeValue("", "resname");
 		if ( tmp != null ) group.setName(tmp);
 		else if ( params.getFallbackToID() ) {
 			group.setName(reader.getAttributeValue("", "id"));
 		}
 
 		// Get restype (can be null)
 		group.setType(reader.getAttributeValue("", "restype"));
 		return true;
 	}
 
 	private boolean processEndBinUnit () {
 		// Pop and checks the value for this group
 		int id = parentIds.pop();
 		// Else: it's a structural group
 		Ending ending = new Ending(String.valueOf(id));
 		ending.setSkeleton(skel);
 		queue.add(new Event(EventType.END_GROUP, ending));
 		return true;
 	}
 
 }
