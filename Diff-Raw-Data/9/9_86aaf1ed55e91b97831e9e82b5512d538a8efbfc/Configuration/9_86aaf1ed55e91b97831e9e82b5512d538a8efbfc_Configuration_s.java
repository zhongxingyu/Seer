 /*
  * MIPL: Mining Integrated Programming Language
  *
  * File: Configuration.java
  * Author: Jin Hyung Park <jp2105@columbia.edu>
  * Reviewer: YoungHoon Jung <yj2244@columbia.edu>
  * Description: Configuration
  */
 package edu.columbia.mipl.conf;
 
 //package org.jfm.filesystems;
 
 import java.util.*;
 
 import java.io.File;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 public class Configuration extends DefaultHandler {
 	public static final int MODE_LOCAL = 0;
 	public static final int MODE_REMOTE = 1;
 	
 	public static final int GEN_JAVASRC = 0;
 	public static final int GEN_BYTECODE = 1;
 	
 	private static Configuration instance = null;
 	private boolean isXMLAsString;
 	private boolean isXMLAsFile;
 	private String xmlString;
 	private String xmlFileName;
 
 	private static String kFileSystemElementName = "filesystem";
 	private static String kNameElementName = "name";
 	private static String kClassElementName = "class";
 	private static String kDependenciesElementName = "dependencies";
 	private static String kJARElementName = "jar";
 
 	private final static int kFileSystemElement = 0;
 	private final static int kNameElement = 1;
 	private final static int kClassElement = 2;
 	private final static int kDependenciesElement = 3;
 	private final static int kJARElement = 4;
 
 	private int currentElement = -1;
 
 	private String name;
 	private String theClass;
 	private HashMap<String, String> hash = new HashMap<String, String>();
 	private java.util.List<String> dependencies = null;
 	
 	private int mode = MODE_LOCAL;
	private java.util.List<String> servers = new ArrayList<String> ();
 
 	private int gen = GEN_JAVASRC;
 	
 	Configuration() {
 		isXMLAsString = false;
 		isXMLAsFile = false;
 	}
 
 	public static synchronized Configuration getInstance() {
 		if (instance == null) {
 			instance = new Configuration();
 		}
 		return instance;
 	}
 
 	public void setXmlString(String xmlString) {
 		this.xmlString = xmlString;
 		this.isXMLAsString = true;
 	}
 
 	public String getXmlString() {
 		return this.xmlString;
 	}
 
 	public void setXmlFileName(String xmlFileName) {
 		this.xmlFileName = xmlFileName;
 		this.isXMLAsFile = true;
 	}
 
 	public String getXmlFileName() {
 		return this.xmlFileName;
 	}
 
 	private void parseDocument() {
 		//get a factory
 		SAXParserFactory spf = SAXParserFactory.newInstance();
 		try {
 			//get a new instance of parser
 			SAXParser sp = spf.newSAXParser();
 
 			//parse the file and also register this class for call backs
 			if (isXMLAsString) {
 				InputStream is = new ByteArrayInputStream(xmlString.getBytes());
 				sp.parse(is, this);
 			} else if (isXMLAsFile) {
 				File f = new File(xmlFileName);
 				sp.parse(f, this);
 			}
 		} catch (SAXException se) {
 			se.printStackTrace();
 		} catch (ParserConfigurationException pce) {
 			pce.printStackTrace();
 		} catch (IOException ie) {
 			ie.printStackTrace();
 		}
 	}
 	
 	//Event Handlers
 	public void startElement(String uri, String localName, String qName,
 			Attributes attributes) throws SAXException {
 		if (kFileSystemElementName.equals(qName))
 			currentElement = kFileSystemElement;
 		if (kNameElementName.equals(qName))
 			currentElement = kNameElement;
 		if (kClassElementName.equals(qName))
 			currentElement = kClassElement;
 		if (kDependenciesElementName.equals(qName))
 			currentElement = kDependenciesElement;
 		if (kJARElementName.equals(qName))
 			currentElement = kJARElement;
 	}
 
 
 	public void characters(char[] ch, int start, int length) throws SAXException {
 		switch(currentElement) {
 			case kNameElement:
 				name = new String(ch, start, length);
 				break;
 			case kClassElement:
 				theClass = new String(ch, start, length);
 				hash.put(name, theClass);
 				break;
 			case kDependenciesElement:
 				dependencies = new ArrayList<String>();
 				break;
 			case kJARElement:
 				dependencies.add(new String(ch, start, length));
 				break;
 		}
 	}
 
 	public void endElement(String uri, String localName,
 		String qName) throws SAXException {
 		currentElement = -1;
 	}
 
 	/**
 	 * @return the dependencies
 	 */
 	public java.util.List<String> getDependencies() {
 		return dependencies;
 	}
 
 	/**
 	 * @return the name
 	 */
 	public Map<String, String> getMap() {
 		return hash;
 	}
 	
 	public int getMode() {
 		return mode;
 	}
 	
 	public void setMode(int mode) {
 		this.mode = mode;
 	}
 	
 	public java.util.List<String> getServers() {
 		return servers;
 	}
 	
 	public void addServer(String server) {
 		servers.add(server);
 	}
 	
 	public int getGen() {
 		return gen;
 	}
 	
 	public void setGen(int gen) {
 		this.gen = gen;
 	}
 }
