 /*
  *  Copyright (C) 2004  The Concord Consortium, Inc.,
  *  10 Concord Crossing, Concord, MA 01742
  *
  *  Web Site: http://www.concord.org
  *  Email: info@concord.org
  *
  *  This library is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU Lesser General Public
  *  License as published by the Free Software Foundation; either
  *  version 2.1 of the License, or (at your option) any later version.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  *  Lesser General Public License for more details.
  *
  *  You should have received a copy of the GNU Lesser General Public
  *  License along with this library; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  * END LICENSE */
 
 /*
  * Last modification information:
  * $Revision: 1.26 $
  * $Date: 2007-10-17 20:25:27 $
  * $Author: scytacki $
  *
  * Licence Information
  * Copyright 2004 The Concord Consortium 
 */
 package org.concord.otrunk.xml;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.StringReader;
 import java.io.Writer;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.swing.JOptionPane;
 
 import org.concord.framework.otrunk.OTID;
 import org.concord.framework.otrunk.OTXMLString;
 import org.concord.otrunk.OTrunkImpl;
 import org.concord.otrunk.OTrunkUtil;
 import org.concord.otrunk.datamodel.BlobResource;
 import org.concord.otrunk.datamodel.OTDataList;
 import org.concord.otrunk.datamodel.OTDataMap;
 import org.concord.otrunk.datamodel.OTDataObject;
 import org.concord.otrunk.datamodel.OTDatabase;
 import org.concord.otrunk.datamodel.OTIDFactory;
 import org.concord.otrunk.datamodel.OTPathID;
 import org.concord.otrunk.datamodel.OTRelativeID;
 import org.concord.otrunk.datamodel.OTUUID;
 import org.jdom.Comment;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.IllegalDataException;
 import org.jdom.input.JDOMParseException;
 import org.jdom.input.SAXBuilder;
 import org.jdom.output.Format;
 import org.jdom.output.XMLOutputter;
 
 
 /**
  * Exporter
  * Class name and description
  *
  * Date created: Nov 17, 2004
  *
  * @author scott<p>
  *
  */
 public class ExporterJDOM
 {
 	public static boolean useFullClassNames = false;
 
 	private static Pattern refidPattern = Pattern.compile("(refid=\")([^\"]*)(\")");
 
 	private static Pattern viewidPattern = Pattern.compile("(viewid=\")([^\"]*)(\")");
 
 	// don't match hrefs with colons those are external links				
 	private static Pattern hrefPattern = Pattern.compile("(href=\")([^:\"]*)(\")");
 
 	OTDatabase otDb;
 	ArrayList writtenIds;
 	ArrayList processedClasses;
 	HashMap containers;
 	
 	private ArrayList processedIds;
 	private ArrayList duplicateClasses;
 	private HashMap incomingReferenceMap;
 	
 	private URL contextURL;
 	
 	public static void export(File outputFile, OTDataObject rootObject, OTDatabase db)
 	throws Exception
 	{
 		ExporterJDOM exporter = new ExporterJDOM();
 		exporter.setContextURL(outputFile.toURL());
 		Document doc = exporter.buildDocument(rootObject, db);
 
 		FileOutputStream outputStream = new FileOutputStream(outputFile);
 		writeDocument(doc, outputStream);
 	}
 
 	
 	/**
 	 * Calling this method might be unsafe when the outputstream is updating
 	 * a File that already exists. 
 	 * When the outputstream is created the File could be erased. So if there is an 
 	 * error while exporting the database, then the original file not be there
 	 * as a backup.
 	 * 
 	 * It is better to create an instance, call buildDocument and then create
 	 * the outputstream and call writeDocument.
 	 * 
 	 * @param outputStream
 	 * @param rootObject
 	 * @param db
 	 * @throws Exception
 	 */
 	public static void export(OutputStream outputStream, OTDataObject rootObject, OTDatabase db)
 	throws Exception
 	{		
 		ExporterJDOM exporter = new ExporterJDOM();
 		Document doc = exporter.buildDocument(rootObject, db);
 		
 		writeDocument(doc, outputStream);
 	}
 
 	/**
 	 * Calling this method is a unsafe when the writer is writing to a File.  
 	 * As soon as the writer is created, the file gets erased.  So if there is an 
 	 * error while exporting the database, then the original file not be there
 	 * as a backup.
 	 * 
 	 * It is better to create an instance, call buildDocument and then create
 	 * the writer and call writeDocument.
 	 * 
 	 * @param writer
 	 * @param rootObject
 	 * @param db
 	 * @throws Exception
 	 */
 	public static void export(Writer writer, OTDataObject rootObject, OTDatabase db)
 	throws Exception
 	{
 		ExporterJDOM exporter = new ExporterJDOM();		
 		Document doc = exporter.buildDocument(rootObject, db);
 		
 		writeDocument(doc, writer);
 	}	
 	
 	public static void writeDocument(Document doc, OutputStream outputStream)
 	throws Exception
 	{
 		OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
 		writeDocument(doc, writer);
 	}
 
 	public static void writeDocument(Document doc, Writer writer)
 	throws Exception
 	{
 		Format format = Format.getPrettyFormat();
 		XMLOutputter outputter = new XMLOutputter(format);
 
 		outputter.output(doc, writer);
 		writer.close();
 	}
 
 	public ExporterJDOM()
 	{
 		writtenIds = new ArrayList();
 		processedClasses = new ArrayList();
 		duplicateClasses = new ArrayList();
 		containers = new HashMap();
 		processedIds = new ArrayList();
 		incomingReferenceMap = new HashMap();		
 	}
 
 	/**
 	 * Create a JDOM document which can then be written out with the writeDocument methods.
 	 * 
 	 * @param rootObject
 	 * @param db
 	 * @return
 	 * @throws Exception
 	 */
 	public Document buildDocument(OTDataObject rootObject, OTDatabase db)
 	throws Exception
 	{		
 		// If this is a XMLDatabase pre-populate the written classes with the databases existing classes.
 		// This preserves any imported classes that might not have been actually used in the otml file
 		// these imported classes are currently the only way to load in packages, so they need to be preserved.
 		if(db instanceof XMLDatabase){
 			ArrayList importedClasses = ((XMLDatabase)db).getImportedOTObjectClasses();
 			processedClasses.addAll(importedClasses);
 		}
 	
 		otDb = db;
 
 		// Make a first pass through the objects reachable from the rootObject
 		// for each one track its incoming references
 		// and the classnames.  Flag duplicate classnames.
 		processObject(rootObject);
 		
 		// Try to create XML. If invalid XML exists AND user chooses to go back
 		// and fix the error, this will throw an exception. We will then NOT
 		// save the file.
 		Element rootObjectElement = null;
 		try {
 			rootObjectElement = exportObject(rootObject, null, null);
 		} catch (Exception e){
 			// break and pass exception on up
 			throw e;
 		}
 
 		Element otrunkEl = new Element("otrunk");
 		
 		OTID dbID = otDb.getDatabaseId();
 		if(dbID == null){
 			// create a temporary Id if there isn't one already
 			dbID = OTUUID.createOTUUID();
 		}
 		otrunkEl.setAttribute("id", dbID.toExternalForm());
 		
 		Element importsEl = new Element("imports");
 		otrunkEl.addContent(importsEl);
 		
 		for(int i=0; i<processedClasses.size(); i++) {
 			Element importEl = new Element("import");
 			importsEl.addContent(importEl);
 
 			importEl.setAttribute("class", (String)processedClasses.get(i));
 		}
 		
 		Element objectsEl = new Element("objects"); 
 		otrunkEl.addContent(objectsEl);
 		
 		objectsEl.addContent(rootObjectElement);
 
 		Document doc = new Document(otrunkEl);
 		
 		return doc;		
 	}
 	
 	private void processObject(OTDataObject dataObject) 
 		throws Exception
     {
 		OTID id = dataObject.getGlobalId();
 		
 		// Check if we have processed this object already
 		if(processedIds.contains(id)) {
 			// we've seen this object before just skip it
 			return;
 		}
 
 		// If we are here then the object hasn't been written out before
 		// and if it has a valid container then we are inside of that container
 		processedIds.add(id);
 		
 		// System.err.println("writting object: " + id);		
 		
 		String objectFullClassName = OTrunkImpl.getClassName(dataObject);
 		
 		// If this is the first time we've seen this class then check it
 		if(!processedClasses.contains(objectFullClassName)) {
 			// If not using full class names check for duplicates
 			if(!useFullClassNames){
 				String objectClassName = getClassName(objectFullClassName);
 				for(int i=0; i<processedClasses.size(); i++){
 					String processedFullClassName = (String) processedClasses.get(i);
 					String processedClassName = getClassName(processedFullClassName);
 					if(processedClassName.equals(objectClassName)){
 						// This is very bad
 						System.err.println("Duplicate Class names found: " +
 								processedClasses.get(i) + " and " +
 								objectFullClassName);
 						System.err.println("This database needs to be saved with useFullClassNames turned on");
 						if(!duplicateClasses.contains(processedFullClassName)){
 							duplicateClasses.add(processedFullClassName);							
 						}
 						duplicateClasses.add(objectFullClassName);
 					}
 				}
 			}
 			
 			processedClasses.add(objectFullClassName);
 		}
 				
 		String resourceKeys [] = dataObject.getResourceKeys();
 				
 		for(int i=0; i<resourceKeys.length; i++) {
 		    
 		    // FIXME: we are ignoring special keys there should way
 		    // to identify special keys.
 			String resourceName = resourceKeys[i];
 
 			if(		resourceName.equals("currentRevision") ||
 					resourceName.equals("localId")) {
 				continue;
 			}
 
 			Object resource = dataObject.getResource(resourceName);			
 			
 			if(resource instanceof OTID) {
 				// this is an object reference
 				// recurse
 				
 				// handle the ID, check to see if it is in our database or not
 				// if it is in our database then we should recurse
 				processReference(dataObject, (OTID)resource);
 			} else if(resource instanceof OTDataList) {
 			    OTDataList list = (OTDataList)resource;
 				for(int j=0;j<list.size(); j++) {
 					Object listElement = list.get(j);
 					processCollectionItem(dataObject, listElement);
 				}
 			} else if(resource instanceof OTDataMap) {
 			    OTDataMap map = (OTDataMap)resource;
 			    String [] mapKeys = map.getKeys();
 			    for(int j=0; j<mapKeys.length; j++) {		
 			    	// We currently support special maps that use object ids as keys.  
 			    	// The code below handles this situation.  This is code is a bit dangerous because
 			    	// we might incorrectly identify a key as an id, but there isn't a better solution 
 			    	// at the moment.
 			    	try {
 			    		OTID otid = OTIDFactory.createOTID(mapKeys[j]);
 			    		if(otid != null){
 			    			processReference(dataObject, otid);
 			    		}
 			    	} catch (Throwable t) {
 			    		// silently ignore ids which can't be parsed correctly
 			    	}
 			    	
 			        Object mapValue = map.get(mapKeys[j]);			        
 			        if(mapValue != null) {
 			        	processCollectionItem(dataObject, mapValue);
 			        }
 			    }
 			} else if(resource instanceof OTXMLString) {
 				// handle xml strings because they might have references to 
 				// objects in them.
 				// first search for any id pattern after find it
 				// record the reference, and possibly substitute it
 				// if it is a local_id reference
 				processXMLString(refidPattern, dataObject, (OTXMLString) resource);
 				processXMLString(hrefPattern, dataObject, (OTXMLString) resource);
 				processXMLString(viewidPattern, dataObject, (OTXMLString) resource);				
 			}
 		}
     }
 
 
 	private void processXMLString(Pattern pattern, OTDataObject dataObject, 
 		OTXMLString xmlString) 
 		throws Exception
 	{
 		Matcher m = pattern.matcher(xmlString.getContent());
 		while(m.find()) {
 			String otidStr = m.group(2);
 			OTID otid = OTIDFactory.createOTID(otidStr);
 			processReference(dataObject, otid);			
 		}
 	}
 	
 	private String exportXMLString(Pattern pattern, String xmlString)
 	{
 		Matcher m = pattern.matcher(xmlString);
 		StringBuffer parsed = new StringBuffer();
 		while(m.find()) {
 			String otidStr = m.group(2);
 			OTID otid = OTIDFactory.createOTID(otidStr);
 			
 			String convertedId = convertId(otid);
 			String escapedConvertedId = OTrunkUtil.escapeReplacement(convertedId);
 			m.appendReplacement(parsed, "$1" + escapedConvertedId + "$3");
 		}
 		m.appendTail(parsed);
 
 		return parsed.toString();
 	}
 	
 	private void processCollectionItem(OTDataObject parent, Object listElement) 
 		throws Exception
 	{
 		if(listElement instanceof OTID) {
 			// this is an object reference, recurse
 			processReference(parent, (OTID)listElement);
 		} else if(listElement instanceof OTDataList  ||
 				listElement instanceof OTDataMap) {
 			System.err.println("nested collections are illegal");
 		}		
 	}
 		
 	private void processReference(OTDataObject parent, OTID id) 
 		throws Exception
     {
         OTDataObject childObject = otDb.getOTDataObject(parent, id);
         if(childObject == null) {
             // our db doesn't contain this object, so we don't need
         	// to do anything with it.
 
         	// FIXME: This should be a little more careful.  The list of
             // databases we require should be saved.  So then we can check
             // if this external object will be resolvable on loading.
         	return;
         } else {
         	// record that this parent object is referencing this object.
     		ArrayList refList = (ArrayList) incomingReferenceMap.get(id);
     		if(refList == null){
     			refList = new ArrayList();
     			incomingReferenceMap.put(id, refList);
     		}
     		
     		refList.add(parent);
     		processObject(childObject);
         }
     }
 
 	public Element exportCollectionItem(OTDataObject parentDataObj, 
 			Object item, String parentResourceName)
 	throws Exception
 	{
 		if(item == null) {
 			return new Element("null");			
 		} else if(item instanceof OTID) {
 			// this is an object reference
 			// recurse
             return exportID(parentDataObj, (OTID)item, parentResourceName);
 		} else if(item instanceof OTDataList  ||
 				item instanceof OTDataMap) {
 			System.err.println("nested collections are illegal");
 			return null;
 		} else {
 			// this is a literal reference in a list or map so we need the type
 			String type = null;
 			type = TypeService.getDataPrimitiveType(item.getClass());
 
 			if(type == null){
				String msg = "ExporterJDOM#exportCollectionItem: ";
				msg += "Unknown list item type: " + item.getClass();
				msg += " parentResourceName=" + parentResourceName;
				System.err.println(msg);				
 				return null;
 			}
 			
 			Element typeEl = new Element(type);
 
 			String itemString;
 			if(!(item instanceof BlobResource)) {
 				itemString = item.toString();
 			} else {
 				BlobResource blob = (BlobResource)item;
 				if(blob.getBlobURL() != null){
 					itemString = blob.getBlobURL().toExternalForm(); 
 				} else {
 					itemString = BlobTypeHandler.base64(blob.getBytes());
 				}
 			}
 			
 			typeEl.setText(itemString);
 			return typeEl;
 		}
 	}
 
     public Element exportID(OTDataObject parent, OTID id, String parentResourceName)
     throws Exception
     {
         OTDataObject childObject = otDb.getOTDataObject(parent, id);
         if(childObject == null) {
             // our db doesn't contain this object
             // so write out a reference to it and hope that we can find 
             // it when we are loaded in.  
             // FIXME: This should be a little more careful.  The list of
             // databases we require should be saved.  So then we can check
             // if this external object will be resolvable on loading.
         	return exportObjectReference(id);
         } else {
             return exportObject(childObject, parent, parentResourceName);
         }
     }
     
     public Element exportObjectReference(OTID id)
     {
 		Element objectEl = new Element("object");
 		String convertedId = convertId(id);
 		objectEl.setAttribute("refid", convertedId);
 		return objectEl;    	
     }
 
     /**
      * This will return true of the passed in a id is a localId in the current
      * database.
      * 
      * @param id
      * @return
      */
     public boolean isLocalId(OTID id)
     {
 		if(id instanceof OTRelativeID){
 			OTRelativeID relId = (OTRelativeID) id;
 			OTID relRelId = relId.getRelativeId();			
 			String externalRelRelId = relRelId.toExternalForm();
 			if(relId.getRootId().equals(otDb.getDatabaseId()) && 
 					relRelId instanceof OTPathID &&
 					externalRelRelId.startsWith("/") &&
 					// This last condition is a hack so path ids which start with an expanded local_id
 					// don't get written out as local_is
 					(externalRelRelId.substring(1).indexOf('/') == -1)){
 				return true;
 			}
 		}
 		return false;
 
     }
     
     /**
      * Check if this id is actually a local id reference.  If it is then
      * return the ${xxx} notation used in the otml files.
      * @param id
      * @return
      */
     public String convertId(OTID id)
     {
     	if(isLocalId(id)){
 			OTRelativeID relId = (OTRelativeID) id;
 			OTID relRelId = relId.getRelativeId();			
 			return "${" + relRelId.toExternalForm().substring(1) + "}";    		
     	} else {
     		return id.toExternalForm();
     	}
     }
     
 	public Element exportObject(OTDataObject dataObj, OTDataObject parent, String parentResourceName)
 	throws Exception
 	{
 		OTID id = dataObj.getGlobalId();
 
 		// Check if we should write out a reference
 		if(shouldWriteReference(dataObj, parent, parentResourceName)) {
 			return exportObjectReference(id);			
 		}
 		
 				
 		// If we are here then the object hasn't been written out before
 		// and if it has a valid container then we are inside of that container
 		writtenIds.add(id);
 		
 		// System.err.println("writting object: " + id);		
 		
 		String objectFullClassName = OTrunkImpl.getClassName(dataObj);
 		String objectElementName =  getObjectElementName(objectFullClassName);
 		
 		Element objectEl = new Element(objectElementName);
 		
 		XMLDataObject xmlDO = null;
 		if(dataObj instanceof XMLDataObject){
 			xmlDO = (XMLDataObject) dataObj;
 		}
 		
 		if(xmlDO != null && xmlDO.getLocalId() != null){
 			// FIXME this is dangerous if the object is being copied from one db 
 			// to another,  the references in this export should be checked to see
 			// if they use the local_id or its global as a reference.
 			objectEl.setAttribute("local_id", xmlDO.getLocalId());
 		} else {
 			// check to see if this object has any references that 
 			// are not containment references.
 			// there is only one containment reference and every object
 			// should have one (with the exception of the root object)
 			// so if there is more than one incoming reference than this object
 			// needs an id.  
 			// We should also check to see if the objects id has been explicitly set
 			// in that case it should be written out anyhow.  I'm not sure how to 
 			// do that yet.
 			ArrayList incomingReferences = (ArrayList) incomingReferenceMap.get(id);
 			if((xmlDO != null && xmlDO.isPreserveUUID() && id instanceof OTUUID) ||
 					(incomingReferences != null && incomingReferences.size() > 1)){
 				objectEl.setAttribute("id", id.toExternalForm());
 			}
 		}
 		
 		String resourceKeys [] = dataObj.getResourceKeys();
 		ArrayList nullResources = new ArrayList();		
 		
 		for(int i=0; i<resourceKeys.length; i++) {
 		    
 		    // FIXME: we are ignoring special keys there should way
 		    // to identify special keys.
 			String resourceName = resourceKeys[i];
 
 			if(		resourceName.equals("currentRevision") ||
 					resourceName.equals("localId")) {
 				continue;
 			}
 
 			Object resource = dataObj.getResource(resourceName);			
 			
 			if(resource instanceof OTID) {
 				// this is an object reference
 				// recurse
                 Element objectIDEl = exportID(dataObj, (OTID)resource, resourceName);
 			    writeResourceElement(dataObj, objectEl, resourceName, objectIDEl);
 			} else if(resource instanceof OTDataList) {
 			    OTDataList list = (OTDataList)resource;
 			    
 			    if(list.size() == 0){
 			    	continue;
 			    }
 			    
 			    ArrayList content = new ArrayList();
 				for(int j=0;j<list.size(); j++) {
 					Object listElement = list.get(j);
 					if(list instanceof XMLDataList){
 						XMLReferenceInfo info = ((XMLDataList)list).getReferenceInfo(j);
 						if(info != null && info.comment != null){
 							content.add(new Comment(info.comment));
 						}
 					}
 					Element collectionEl = exportCollectionItem(dataObj, listElement, resourceName);
 					if(collectionEl != null){
 						content.add(collectionEl);
 					}
 				}
 			    writeResourceElement(dataObj, objectEl, resourceName, content);				
 			} else if(resource instanceof OTDataMap) {
 			    OTDataMap map = (OTDataMap)resource;
 			    String [] mapKeys = map.getKeys();
 			    ArrayList content = new ArrayList();
 			    for(int j=0; j<mapKeys.length; j++) {
 			    	Element entryEl = new Element("entry");
 			    	content.add(entryEl);
 			    	
 			    	// We currently support special maps that use object ids as keys.  Inorder to preserve the otml
 			    	// correctly we need to check if the mapKey is an id and if so then it should be converted.
 			    	// When it is converted it will be turned into a ${} local_id reference.  Because we don't know
 			    	// if the keys of the map are supposed to be ids or just plain strings.  We have to be careful to
 			    	// not screw up regular strings that look like ids.
 			    	// So we only modify the key if it is a local_id in this database.
 			    	String exportedKey = mapKeys[j];
 			    	try {
 			    		OTID otid = OTIDFactory.createOTID(mapKeys[j]);
 			    		if(otid != null){
 			    			if(isLocalId(otid)){
 			    				exportedKey = convertId(otid);
 			    			}
 			    		}
 			    	} catch (Throwable t) {
 			    		// silently ignore ids which can't be parsed correctly
 			    	}
 
 			    	entryEl.setAttribute("key", exportedKey);
 			    	
 			        Object mapValue = map.get(mapKeys[j]);
 			        Element collectionEl = exportCollectionItem(dataObj, mapValue, resourceName);
 			        entryEl.addContent(collectionEl);
 			    }
 			    writeResourceElement(dataObj, objectEl, resourceName, content);
 			} else if(resource instanceof BlobResource){
 				BlobResource blob = (BlobResource) resource;
 				URL blobUrl = blob.getBlobURL();
 				String blobString = null;
 				int defaultType = XMLReferenceInfo.ELEMENT;
 				if(blobUrl != null){
 					if(contextURL != null){
 						blobString = URLUtil.getRelativeURL(contextURL, blobUrl);
 					} else {
 						blobString = blobUrl.toString();
 					}
 					
 					defaultType = XMLReferenceInfo.ATTRIBUTE;
 				} else {
 					blobString = BlobTypeHandler.base64(blob.getBytes());
 				}
 				
 				writeResource(dataObj, objectEl, resourceName, blobString, 
 						defaultType);				
 				
 			} else if(resource == null){
 				if(xmlDO.getSaveNulls()){
 					nullResources.add(resourceName);
 				} else {
 					System.err.println("Got null resource value");
 				}
 			} else if(resource instanceof Integer ||
 			        resource instanceof Float ||
 			        resource instanceof Byte ||
 			        resource instanceof Short ||
 			        resource instanceof Boolean) {
 
 				String primitiveString = resource.toString();
 				writeResource(dataObj, objectEl, resourceName, primitiveString, 
 						XMLReferenceInfo.ATTRIBUTE);				
 			} else if(resource instanceof OTXMLString) {
 				// The xml string is wrapped with a fake root element
 				// and loaded as a JDOM document
 				// and if it doesn't fail then clone the contents of the fake root
 				// element and add it to the resourceElement 
 				// FIXME if it does fail then write it out as CDATA
 				// FIXME we should process any references
 				
 				String xmlString = ((OTXMLString)resource).getContent();
 				xmlString = exportXMLString(refidPattern, xmlString);
 				xmlString = exportXMLString(viewidPattern, xmlString);
 				xmlString = exportXMLString(hrefPattern, xmlString);
 
 				String originalString = xmlString.trim();
 				
 				SAXBuilder builder = new SAXBuilder();
 				xmlString = "<root>" + originalString + "</root>";
 				StringReader reader = new StringReader(xmlString);
 				try{
 					Document xmlStringDoc = builder.build(reader, resourceName);
 					Element rootXMLStringEl = xmlStringDoc.getRootElement();
 					writeResourceElement(dataObj, objectEl, resourceName, rootXMLStringEl.cloneContent());
 				
 				} catch(JDOMParseException e){
 	            	System.err.println("User-generated XML error: "+e.getCause());
 					
 					System.err.println("Invalid xmlString");
 					System.err.println("-----");
 					System.err.println(xmlString);
 					System.err.println("-----");
 					
 					// show warning, and let user choose to go back and edit
 					String warning = "Warning: There is an HTML error in your text.\n " + e.getCause();
 					Object[] options = {"Save anyway and ignore the errors",
 							"I will go back and fix the error"};
 	            	boolean saveAnyway = JOptionPane.showOptionDialog(null, warning,
 	            		    "HTML error",
 	            		    JOptionPane.YES_NO_OPTION,
 	            		    JOptionPane.ERROR_MESSAGE,
 	            		    null,
 	            		    options,
 	            		    options[1]) == 0;
 	            	if (!saveAnyway){
 	            		throw new Exception("JDOMParseException caught. User will edit invalid XML");
 	            	} else {
 						writeResource(dataObj, objectEl, resourceName, XMLStringTypeHandler.INVALID_PREFIX + originalString, XMLReferenceInfo.ELEMENT);
 	            	}
 					e.printStackTrace();					
 				}
 			} else if(resource instanceof String) {
 				writeResource(dataObj, objectEl, resourceName, (String) resource, 
 						XMLReferenceInfo.ATTRIBUTE);								
 			} else {
 				String primitiveString = resource.toString();
 
 				writeResource(dataObj, objectEl, resourceName, primitiveString, 
 						XMLReferenceInfo.ATTRIBUTE);				
 			}
 		}
 		
 		if(nullResources.size() > 0){
 			String unsetList = "";
 			for(int i=0; i<nullResources.size(); i++){
 				unsetList += nullResources.get(i) + " ";
 			}
 			unsetList = unsetList.trim();
 			objectEl.setAttribute("unset", unsetList);
 		}
 		
 		return objectEl;
 	}	
 	
 	public String getObjectElementName(String objectClass)	
 	{
 		/* I don't know why this is being done, but it was here before
 		 * so I'll leave it for now.
 		 */
 		if(objectClass == null) {
 		    return "object";
 		}
 		
 		if(!useFullClassNames){
 			if(duplicateClasses.contains(objectClass)){
 				return objectClass;
 			}
 			return getClassName(objectClass);
 		}
 		
 		return objectClass;
 	}
 
     protected boolean shouldWriteReference(OTDataObject dataObj, OTDataObject parent, 
     	String parentResourceName)
     {
 		OTID id = dataObj.getGlobalId();
 		
 		// Check if we have written out this object already
 		// if so then just write a reference so the object isn't written twice
 		if(writtenIds.contains(id)) {
 			return true;
 		}
 
 		// check if this object has valid container
 		if(!(dataObj instanceof XMLDataObject)){
 			return false;
 		}
 		
 		XMLDataObject	xmlDO = (XMLDataObject) dataObj;
 			
 		XMLDataObject container = xmlDO.getContainer();
 		if(container == null || !processedIds.contains(container.getGlobalId())){
 			// the container isn't set, or the container isn't going to be written out
 			// by this exporter
 			return false;
 		}
 
 		// this is a valid container
 		// does it have the current object in this spot
 		String containerResourceName = xmlDO.getContainerResourceKey();
 				
 		if(parent == container && 
 						parentResourceName.equals(containerResourceName)){
 			// our parent is the container and our parent property is the same as the container property
 			return false;
 		}
 
 		// this isn't the parent, or it isn't the right resource in the parent
 		Object containedValue = container.getResource(containerResourceName);
 		if(containedValue.equals(id)){
 			// the container still contains the correct value						
 			// so just write a reference here
 			return true;
 		}
 					
 		if(containedValue instanceof OTDataList){
 			OTDataList dataListContainer = ((OTDataList)containedValue);
 			for(int i=0; i<dataListContainer.size(); i++){
 				if(id.equals(dataListContainer.get(i))){
 					// our container list still references us
 					return true;
 				}
 			}						
 			// our container list doesn't reference us anymore
 			return false;
 		}
 					
 		if(containedValue instanceof OTDataMap){
 			OTDataMap dataMapContainer = (OTDataMap) containedValue;
 			String [] keys = dataMapContainer.getKeys();
 			for(int i=0; i<keys.length; i++){
 				if(id.equals(dataMapContainer.get(keys[i]))){
 					// our previous container map still references us
 					return true;
 				}
 			}
 			return false;
 		}
 		
 		return false;
     }
     
 	public static String getClassName(String fullClassName)
 	{
 		return fullClassName.substring(fullClassName.lastIndexOf('.')+1);
 	}
 	
 	public static void writeResourceElement(OTDataObject dataObj, Element objectEl, 
 		String resourceName, Object content)
 	{
 		if(content == null){
 			return;
 		}
 
 		XMLReferenceInfo resInfo = null;
 		if(dataObj instanceof XMLDataObject){
 			XMLDataObject xmlObj = (XMLDataObject)dataObj;
 			resInfo = xmlObj.getReferenceInfo(resourceName);
 		}
 
 		if(resInfo != null){
 			if(resInfo.comment != null){
 				Comment comment = new Comment(resInfo.comment);
 				objectEl.addContent(comment);
 			}
 		}
 		
 
 		Element resourceEl = new Element(resourceName);
 		objectEl.addContent(resourceEl);
 		if(content instanceof Element){
 			resourceEl.setContent((Element) content);		
 		} else if(content instanceof Collection){
 			resourceEl.setContent((Collection) content);					
 		}
 	}
 	
 	public static void writeResource(OTDataObject dataObj, Element objectEl, 
 		String resourceName, String resourceValue, 
 			int defaultType)
 	{
 		XMLReferenceInfo resInfo = null;
 		if(dataObj instanceof XMLDataObject){
 			XMLDataObject xmlObj = (XMLDataObject)dataObj;
 			resInfo = xmlObj.getReferenceInfo(resourceName);
 		}
 		
 		boolean writeElement = defaultType == XMLReferenceInfo.ELEMENT;
 
 		if(resInfo != null){
 			writeElement = resInfo.type == XMLReferenceInfo.ELEMENT;
 			if(resInfo.comment != null){
 				Comment comment = new Comment(resInfo.comment);
 				objectEl.addContent(comment);
 			}
 		} else if(resourceValue.indexOf('\n') > 0){
 			writeElement = true;
 		}		
 		
 		try {
 			if(writeElement){
 				Element resourceEl = new Element(resourceName);
 				objectEl.addContent(resourceEl);
 				resourceEl.setText(resourceValue);
 			} else {
 				objectEl.setAttribute(resourceName, resourceValue);					
 			}
 		} catch (IllegalDataException e) {
 			System.err.println("Tried to write: \"" + resourceValue + "\" into \"" + resourceName +"\"");
 			System.err.println("skiping this property");
 		}
 	}
 
 
 	public URL getContextURL()
     {
     	return contextURL;
     }
 
 
 	public void setContextURL(URL contextURL)
     {
     	this.contextURL = contextURL;
     }
 }
