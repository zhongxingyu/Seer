 /*******************************************************************************
  * Copyright 2013 Gabriel Siemoneit
  * 
  * This file is part of libteidoc.
  * 
  * libteidoc is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 2 of the License, or
  * (at your option) any later version.
  * 
  * libteidoc is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with libteidoc.  If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package org.libteidoc;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Stack;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 public class TEIDocument extends DefaultHandler {
 	
 	protected static Logger logger = LogManager.getLogger(TEIDocument.class.getName());
 	
 	protected File teiFile = null;
 	protected static SAXParserFactory spf = SAXParserFactory.newInstance();
 	
 	protected TEIElementHandler contentHandler;
 	protected HashMap<String, TEIElementHandler> elementMap = new HashMap<String, TEIElementHandler>();
 	protected Stack<TEIElementHandler> elementStack = new Stack<TEIElementHandler>();
 	
 	protected boolean ignore = true;
 	
 	public TEIDocument(String f) {
 		teiFile = new File(f);
 		if (!teiFile.exists()) {
 			logger.error("TEI file "+f+"does not exist!");
 		}
 		else {
 			logger.debug("TEI file "+f+" loaded.");
 		}
 	}
 	
 	public void setElementHandler(String e, TEIElementHandler h) {
 		elementMap.put(e, h);
 	}
 	
 	public void parse() {
 		if (teiFile == null) {
 			logger.error("Cannot parse: No TEI file loaded!");
 			return;
 		}
 		
 		// parse TEI xml document
 		logger.info("Parsing TEI document...");
 		
 		SAXParser sp;
 		try {
 			sp = spf.newSAXParser();
 			sp.parse(new FileInputStream(teiFile), this);
 		} catch (ParserConfigurationException e) {
 			logger.error("ParserConfigurationException!\n"+e.getMessage());
 		} catch (SAXException e) {
 			logger.error("SAXException!\n"+e.getMessage());
 		} catch (FileNotFoundException e) {
 			logger.error("FileNotFoundException!\n"+e.getMessage());
 		} catch (IOException e) {
 			logger.error("IOException!\n"+e.getMessage());
 		}
 		logger.info("TEI document parsed.");
 	}
 	
 	public void setTEIContentHandler(TEIElementHandler ch) {
 		contentHandler = ch;
 	}
 	
 	// ------------------------------------------------------------------------
 	// Overridden methods from DefaultHandler
 	@Override
     public void startElement (String uri, String localName,
 			      String qN, org.xml.sax.Attributes attributes)
 	throws SAXException {
 		
 		String qName = qN.toLowerCase();
 		//logger.debug("Started element'"+qName+"'");
 		
 		// Never ignore elements 'text' and 'body'
 		if (ignore) {
 			if (qName.equals("text") || qName.equals("body")) {
 				ignore = false;
 				return;
 			}
 		}
 		
		TEIElementHandler handler = elementMap.get(qName);
 		// Do we have to deal with this element?
 		if (handler == null) {
 			ignore = true;
 		}
 		else {
 			//logger.debug("Do not ignore element '"+qName+"'");
 			ignore = false;
 			
 			handler.setAttributes(attributes);
			handler.start();
 			
 			elementStack.push(handler);
 		}
 	}
 	
 	@Override
     public void endElement(String uri, String local, String qN) {
 		
 		//String qName = qN.toLowerCase();
 		if (!elementStack.isEmpty()) {
			elementStack.pop().end();
 		}
 		
		//logger.debug("Ended element '"+qName+"'");
 		
 		// Is this really correct?
 		ignore = false;
 	}
 	
 	@Override
     public void characters(char buf[], int offset, int length) {
 		
 		if (ignore) {
 			return;
 		}
 		
 		String s = new String(buf, offset, length);
 		
 		if (!elementStack.isEmpty()) {
 			elementStack.peek().characters(s);
 		}
 		else
 		if (contentHandler != null) {
 			contentHandler.characters(s);
 		}
     }
 }
