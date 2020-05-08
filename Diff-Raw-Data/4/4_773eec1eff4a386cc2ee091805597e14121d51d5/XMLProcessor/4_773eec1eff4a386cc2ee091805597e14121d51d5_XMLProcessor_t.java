 /**
  * Copyright (C) 2003, 2004 Maynard Demmon, maynard@organic.com
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or 
  * without modification, are permitted provided that the 
  * following conditions are met:
  * 
  *  - Redistributions of source code must retain the above copyright 
  *    notice, this list of conditions and the following disclaimer. 
  * 
  *  - Redistributions in binary form must reproduce the above 
  *    copyright notice, this list of conditions and the following 
  *    disclaimer in the documentation and/or other materials provided 
  *    with the distribution. 
  * 
  *  - Neither the names "Java Outline Editor", "JOE" nor the names of its 
  *    contributors may be used to endorse or promote products derived 
  *    from this software without specific prior written permission. 
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
  * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE 
  * COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, 
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
  * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT 
  * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN 
  * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
  * POSSIBILITY OF SUCH DAMAGE.
  */
 
 package com.organic.maynard.xml;
 
 import java.io.*;
 import java.util.*;
 import com.organic.maynard.xml.SimpleSAXErrorHandler;
 import com.organic.maynard.xml.XMLParserConstants;
 import com.organic.maynard.util.TruthTest;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import org.xml.sax.SAXException;
 import org.xml.sax.Attributes;
 import org.xml.sax.XMLReader;
 import org.xml.sax.InputSource;
 import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.AttributesImpl;
 
 /**
  * A base class for SAX2 parsers used by this webapp. This class is responsible 
  * for reading in an XML file and populating and parsing it using the SAX2 API.
  * 
  * Verbosity can be turned on with a processing instruction in the XML file being
  * processed. To do this place something like this right after the xml declaration:
  * <?verbose enabled="true" level="4"?> where the level is one of the values of
  * the verbosity constants.
  */
 public abstract class XMLProcessor extends DefaultHandler implements XMLParserConstants {
 	
 	// Constants
 	
 	
 	// XML PARSING
 	/** The factory that produces the XML parser. */
 	private SAXParserFactory factory = null;
 	
 	/** Intermediary SAX parser we obtain the SAX2 parser from. */
 	private SAXParser parser = null;
 	
 	/** The SAX2 parser that processes the XML.*/
 	private XMLReader reader = null;
 	
 	/**
 	 * Maintains a stack of the current objects as the XML is processed. This is 
 	 * effectively an extremely simplified custom DOM implementation allowing 
 	 * access to the objects as we build them from the XML.
 	 */
 	protected Stack component_stack = null;
 	
 	/**
 	 * Maintains a stack of the current element character data as the XML is processed.
 	 */
 	protected Stack characters_stack = null;
 	
 	/**
 	 * Maintains a stack of the current element QNames as the XML is processed.
 	 */
 	protected Stack elements_stack = null;
 	
 	/**
 	 * Maintains a stack of the current element attributes as the XML is processed.
 	 */
 	protected Stack attributes_stack = null;
 	
 	
 	// Instance Fields
 	/**
 	 * Indicates if we should process verbosely or not. The value defined in the 
 	 * XML PI is mapped to this during processing.
 	 */
 	private boolean verbose = false;
 	
 	/**
 	 * Indicates the level of verbosity. The value defined in the XML PI is mapped 
 	 * to this during processing.
 	 */
 	private int verbose_level = 0;
 	
 	
 	// Constructors
 	/**
 	 * Constructs a new XMLProcessor and initializes it.
 	 */
 	public XMLProcessor() {
 		init();
 		reset();
 	}
 	
 	/**
 	 * Does initalization for this object. Creates a SAXParserFactory and obtains 
 	 * a SAX2 parser from it. Configures the parser with a content handler and an 
 	 * error handler.
 	 */
 	private void init() {
 		try {
 			// Setup Parser
 			factory = SAXParserFactory.newInstance();
 			factory.setValidating(false);
 			
 			parser = factory.newSAXParser();
 			reader = parser.getXMLReader();
 			
 			reader.setContentHandler(this);
 			reader.setErrorHandler(new SimpleSAXErrorHandler());
 		} catch (ParserConfigurationException e) {
 			e.printStackTrace();
 		} catch (SAXException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Initializes the DOM implementation allowing reuse of this object.
 	 */
 	protected void reset() {
 		component_stack = new Stack();
 		elements_stack = new Stack();
 		attributes_stack = new Stack();
 		characters_stack = new Stack();
 	}
 	
 	
 	// Accessors
 	/**
 	 * Enables/Disables verbose output during XML processing.
 	 *
 	 * @param verbose true to turn verbose output on, false to turn it off.
 	 */
 	public void setVerbose(boolean verbose) {
 		this.verbose = verbose;
 	}
 	
 	/**
 	 * Tests if verbose output is currently enabled.
 	 *
 	 * @return true if verbose output is in effect, false otherwise.
 	 */
 	public boolean isVerbose() {
 		return this.verbose;
 	}
 	
 	/**
 	 * Sets the cutoff for which categories of messages are output.
 	 *
 	 * @param level the verbosity level to set.
 	 */
 	public void setVerboseLevel(int level) {
 		this.verbose_level = level;
 	}
 	
 	/**
 	 * Gets the level of verbosity.
 	 *
 	 * @return the current verbosity level.
 	 */
 	public int getVerboseLevel() {
 		return this.verbose_level;
 	}
 	
 	/**
 	 * Tests if the provided message level should be output based on the level of
 	 * verbosity and if verbose output is enabled.
 	 *
 	 * @param level the verbosity level to test.
 	 * 
 	 * @return true if the provided message level should be output.
 	 */
 	public boolean isVerboseEnough(int level) {
 		if (isVerbose() && level <= getVerboseLevel()) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	/**
 	 * Processes the XML file found at the provided filepath.
 	 *
 	 * @param filepath the path to the XML file on the file system.
 	 */
 	public void process(String filepath) throws SAXException, IOException {
 		reset();
 			
 		File file = new File(filepath);
 		reader.parse(new InputSource(new BufferedInputStream(new FileInputStream(file))));
 	}
 	
 	/**
 	 * Processes the XML from the InputStream using the provided encoding.
 	 */
 	public void process(InputStream stream, String encoding) throws SAXException, IOException {
 		reset();
 		reader.parse(new InputSource(new BufferedReader(new InputStreamReader(stream, encoding))));
 	}
 	
 	
 	// SAX ContentHandler Implementation
 	/**
 	 * Start of document processing.
 	 */
 	public void startDocument() {}
 	
 	/**
 	 * End of document processing.
 	 */
 	public void endDocument() {}
 	
 	/**
 	 * Processing instruction processing.
 	 */
 	public void processingInstruction(String target, String data) {
 		if (PI_VERBOSE.equalsIgnoreCase(target)) {
 			HashMap map = parseAttributes(data);
 			
 			String enabled = (String) map.get(PI_NAME_ENABLED);
 			setVerbose(TruthTest.getBooleanTruthValue(enabled));
 			
 			try {
 				String level = (String) map.get(PI_NAME_LEVEL);
 				setVerboseLevel(Integer.parseInt(level));
 			} catch (NumberFormatException e) {
 				StringBuffer errorMsg = new StringBuffer();
 				errorMsg.append("NumberFormatException when parsing level during processing instruction verbose: ");
 				errorMsg.append(map.get(PI_NAME_LEVEL));
 				System.out.println(errorMsg.toString());
 				return;
 			}
 		}
 	}
 	
 	/**
 	 * The current component to be pushed and poped from the stack. 
 	 */
 	protected Object component = null;
 	
 	/**
 	 * Start of element processing. This should be called at the end of startElement
 	 * implementations by subclasses so that the component can be setup there and
 	 * stored here.
 	 */
 	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) {
 		component_stack.push(component);
 		component = null;
 		elements_stack.push(qName);
		attributes_stack.push(new AttributesImpl(atts));
 		characters_stack.push(new StringBuffer());
 		
 		if (isVerboseEnough(VERBOSITY_DEBUG)) {
 			System.out.println("startElement: [" + qName + "]");
 			for (int i = 0; i < atts.getLength(); i++) {
 				System.out.println("   attribute:   [" + atts.getQName(i) + "] [" + atts.getValue(i) + "]");
 			}
 		}
 	}
 	
 	/**
 	 * End of element processing.
 	 */
 	public void endElement(String namespaceURI, String localName, String qName) {
 		component_stack.pop();
 		elements_stack.pop();
 		attributes_stack.pop();
 		characters_stack.pop();
 		
 		if (isVerboseEnough(VERBOSITY_DEBUG)) {
 			System.out.println("  endElement: [" + qName + "]");
 		}
 	}
 	
 	/**
 	 * Characters processing.
 	 */
 	public void characters(char ch[], int start, int length) throws SAXException {
 		StringBuffer buf = (StringBuffer) characters_stack.peek();
 		buf.append(new String(ch, start, length));
 		
 		if (isVerboseEnough(VERBOSITY_DEBUG)) {
 			System.out.println("  characters: [" + new String(ch, start, length) + "]");
 		}
 	}
 	
 	
 	// Utility Methods
 	/**
 	 * Used when handling PIs to convert them into a HashMap of name/value pairs. 
 	 * The PI content must adhere to the following syntax for this to work:
 	 * [name="value"]?[ name="value"]*
 	 * 
 	 * For example:
 	 * <code>&lt;?verbose enabled="true" level="3"?&gt;</code>
 	 */
 	private static HashMap parseAttributes(String s) {
 		StringTokenizer tok = new StringTokenizer(s);
 		
 		HashMap map = new HashMap();
 		String key = null;
 		String value = null;
 		String garbage = null;
 		
 		while (tok.hasMoreElements()) {
 			key = tok.nextToken("=");
 			garbage = tok.nextToken("\"");
 			value = tok.nextToken("\"");
 			garbage = tok.nextToken(" \t\n\r\f");
 			
 			if (key != null) {
 				map.put(key.trim(), value);
 				key = null;
 				value = null;
 			}
 		}
 		
 		return map;
 	}
 }
