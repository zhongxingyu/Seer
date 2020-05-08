 /*******************************************************************************
  * Extension points
  * Copyright (C) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * This program and the accompanying materials are dual-licensed under either
  * the terms of the Eclipse Public License v1.0 as published by the Eclipse
  * Foundation
  *  
  *   or (per the licensee's choosing)
  *  
  * under the terms of the GNU General Public License version 2 as published
  * by the Free Software Foundation.
  ******************************************************************************/
 package de.tuilmenau.ics.extensionpoint.simple;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import de.tuilmenau.ics.extensionpoint.Extension;
 import de.tuilmenau.ics.extensionpoint.ExtensionRegistry;
 
 
 /**
  * Re-implements the basic functionality of the IExtensionRegistry of Eclipse.
  * Required to avoid EPL license for FoG simulator. 
  */
 public class XMLExtensionRegistry extends ExtensionRegistry
 {
 	/**
 	 * Enables debug output to std out.
 	 */
 	private static final boolean DEBUG = false;
 	
 	public XMLExtensionRegistry()
 	{
 		this.directory = null;
 	}
 	
 	public XMLExtensionRegistry(String directory, String fileNameFilter)
 	{
 		this.directory = directory;
 		this.fileNameFilter = fileNameFilter;
 	}
 	
 	@Override
 	public Extension[] getExtensionsFor(String extensionPointName)
 	{
 		if(registry == null) {
 			scanForExtensions(directory, fileNameFilter);
 		}
 		
 		LinkedList<Extension> extensions = registry.get(extensionPointName);
 		if(extensions != null) {
 			Extension[] res = new Extension[extensions.size()];
 			int i = 0;
 			for(Extension ext : extensions) {
 				res[i] = ext;
 				i++;
 			}
 			return res;
 		} else {
 			return new Extension[0];
 		}
 	}
 	
 	/**
 	 * Adds extensions from files listed in a directory to the cache.
 	 *  
 	 * @param directory Directory or null, for current working directory
 	 * @param fileNameFilter Regular expression or null for all files
 	 */
 	public void scanForExtensions(String directory, String fileNameFilter)
 	{
 		if(registry == null) {
 			registry = new HashMap<String, LinkedList<Extension>>();
 		}
 		
 		if(directory == null) {
 			directory = ".";
 		}
 		
 		if(DEBUG) System.out.println("current working directory: " +System.getProperty("user.dir"));
 		scanDirectory(directory, fileNameFilter);
 		if(DEBUG) System.out.println(registry.size() +" extension points with " +entryCounter +" extensions.");
 	}
 	
 	/**
 	 * Scans all JAR files in a directory and checks their "plugin.xml" file.
 	 * 
 	 * @param directoryName Name of the directory
 	 * @param fileNameFilter Regular expression for filtering the files in the directory
 	 */
 	private void scanDirectory(String directoryName, String fileNameFilter)
 	{
 		File dir = new File(directoryName);
 		FilenameFilter filter = new FilenameFilterImpl(fileNameFilter);
 		
 		if(DEBUG) System.out.println("Scanning '" +directoryName +"' for file matching '" +fileNameFilter +"'");
 		
 		String[] files = dir.list(filter);
 		if(files != null) {
 			for(String file : files) {
 				String completeFilename = directoryName +"\\" +file;
 				
 				try {
 					InputStream pluginxml = getFileFromJar(completeFilename, "plugin.xml");
 					
 					if(pluginxml != null) {
 						// extract xml structure from plugin.xml
 						Document pluginDescr = parseXml(pluginxml);
 						
 						// cache entries
 						if(DEBUG) System.out.println("Searching for extensions in " +completeFilename);
 						extractExtensionPoints(pluginDescr);
 					} else {
 						if(DEBUG) System.out.println("No plugin.xml in " +completeFilename);
 					}
 				}
 				catch(Exception exc) {
 					if(DEBUG) {
 						System.err.println("Errors while operating with " +completeFilename +" (" +exc +")");
 						exc.printStackTrace(System.err);
 					}
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Opens a file inside a JAR file.
 	 * 
 	 * @param jarFilename Filename of the JAR file
 	 * @param entryFilename Filename of the file inside the JAR
 	 * @return Stream or null, if file does not exist
 	 * @throws IOException On errors during opening the JAR file or extracting the file
 	 */
 	private InputStream getFileFromJar(String jarFilename, String entryFilename) throws IOException
 	{
 		JarFile jarfile = new JarFile(jarFilename);
		Enumeration<JarEntry> entries = jarfile.entries();
		if(entries != null) {
			while(entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();
				
				if(!entry.isDirectory()) {
					if(entry.getName().equals(entryFilename)) {
						return jarfile.getInputStream(entry);
 					}
 				}
 			}
 		}
		
		return null;
 	}
 	
 	/**
 	 * Creates a document object from a XML structure.
 	 */
 	private Document parseXml(InputStream file) throws Exception
 	{
 		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 		factory.setValidating(false);
 		factory.setIgnoringComments(true);
 		factory.setIgnoringElementContentWhitespace(true);
 		DocumentBuilder builder = factory.newDocumentBuilder();
 		
 		return builder.parse(file);
 	}
 	
 	/**
 	 * Searches for extension in an XML document and caches them.
 	 *  
 	 * @param pluginDescr XML document
 	 */
 	private void extractExtensionPoints(Document pluginDescr)
 	{
 		// get all extension subsections of the XML document
 		NodeList extensionSets = pluginDescr.getElementsByTagName("extension");
 		if(extensionSets != null) {
 			// iterate all extension sets and find out their extension point names
 			for(int i = 0; i < extensionSets.getLength(); i++) {
 				Node extensionSet = extensionSets.item(i);
 				
 				NamedNodeMap attributes = extensionSet.getAttributes();
 				if(attributes != null) {
 					Node extensionPointName = attributes.getNamedItem("point");
 					if(extensionPointName != null) {
 						String exPoName = extensionPointName.getNodeValue();
 						NodeList extensions = extensionSet.getChildNodes();
 
 						// iterate all extensions in a set
 						if(extensions != null) {
 							if(DEBUG) System.out.println(exPoName +" : " +extensions.getLength());
 							
 							if(extensions.getLength() > 0) {
 								// create entry for extension point in the cache
 								LinkedList<Extension> extensionList = registry.get(exPoName);
 								if(extensionList == null) {
 									extensionList = new LinkedList<Extension>();
 									registry.put(exPoName, extensionList);
 								}
 								
 								// add all extensions from the set to the cache
 								for(int e = 0; e < extensions.getLength(); e++) {
 									Node extension = extensions.item(e);
 									
 									// check if it is a real entry and not just text inside the XML tag
 									if(extension.hasAttributes() || extension.hasChildNodes()) {
 										extensionList.add(new XMLExtension(extension));
 										entryCounter++;
 									}
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	private class FilenameFilterImpl implements FilenameFilter
 	{
 		public FilenameFilterImpl(String filter)
 		{
 			this.filterRegEx = filter;
 		}
 		
 		@Override
 		public boolean accept(File dir, String name)
 		{
 			if(filterRegEx != null) return name.matches(filterRegEx);
 			else return true;
 		}
 
 		private String filterRegEx;
 	}
 	
 	private String directory;
 	private String fileNameFilter;
 	
 	private HashMap<String, LinkedList<Extension>> registry;
 	private int entryCounter = 0;
 }
