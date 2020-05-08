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
 * $Revision: 1.13 $
 * $Date: 2006-01-13 21:04:41 $
 * $Author: scytacki $
  *
  * Licence Information
  * Copyright 2004 The Concord Consortium 
 */
 package org.concord.otrunk.xml;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.PrintStream;
 import java.util.Vector;
 
 import org.concord.framework.otrunk.OTID;
 import org.concord.framework.otrunk.OTResourceList;
 import org.concord.framework.otrunk.OTResourceMap;
 import org.concord.otrunk.OTXMLString;
 import org.concord.otrunk.OTrunkImpl;
 import org.concord.otrunk.datamodel.OTDataObject;
 import org.concord.otrunk.datamodel.OTDatabase;
 
 
 /**
  * Exporter
  * Class name and description
  *
  * Date created: Nov 17, 2004
  *
  * @author scott<p>
  *
  */
 public class Exporter
 {
 	static OTDatabase otDb;
 	static Vector writtenIds = null;
 	static Vector writtenClasses = null;
 	
 	public static void export(File outputFile, OTDataObject rootObject, OTDatabase db)
 	throws Exception
 	{
 		writtenIds = new Vector();
 		writtenClasses = new Vector();
 		FileOutputStream outputStream = new FileOutputStream(outputFile);
 		PrintStream printStream = new PrintStream(outputStream);
 	
 		otDb = db;
 		
 		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
 		PrintStream objectPrintStream = new PrintStream(byteStream);
 		exportObject(objectPrintStream, rootObject, 2);
 
 		String objectString = byteStream.toString();
 		
 		printStream.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
 		printStream.println("<otrunk>");
 		indentPrint(1, "<imports>", printStream);
 		for(int i=0; i<writtenClasses.size(); i++) {
 			indentPrint(2, "<import class=\"" + writtenClasses.get(i) + "\"/>", printStream);
 		}
 		
 		indentPrint(1, "</imports>", printStream);
 		
 		indentPrint(1, "<objects>", printStream);
 		indentPrint(0, objectString, printStream);
 		indentPrint(1, "</objects>", printStream);
 		printStream.println("</otrunk>");
 		
 		printStream.close();
 		// write out the root data object using its otObjectClass name to find
 		// its type
 		
 		// For each id-object resource look up its object and write it out, recording
 		// the ids that have been written out before.
 		
 		// Can do this writting directly to the file, but then we'll need to track some
 		// global indentation lets try that.
 		
 		// lets try recursion to start
 		
 	}
 	
 	public static void printIndent(int indent, PrintStream output)
 	{
 		String start = "";
 		for(int i=0; i<indent; i++) {
 			start += "  ";
 		}
 		output.print(start);
 	}
 	
 	public static void indentPrint(int indent, String str, PrintStream output)
 	{
 	    printIndent(indent, output);
 	    
 		output.println(str);
 	}
 	
 	public static void exportCollectionItem(OTDataObject parentDataObj, 
 			PrintStream output, Object item, int indent)
 	throws Exception
 	{
 		if(item instanceof OTID) {
 			// this is an object reference
 			// recurse
             exportID(output, parentDataObj, (OTID)item, indent);
 		} else if(item instanceof OTResourceList  ||
 				item instanceof OTResourceMap) {
 			System.err.println("nested collections are illegal");
 		} else if(item instanceof byte []) {
 			// this is blob
 			// write out the blob and save a reference to it
 			System.err.println("Got a blob reference????");
 		} else {
 			// this is a litteral reference in a list or map so we need the type
 			String type = null;
 			if(item instanceof String) {
 				type = "string";
 			} else if(item instanceof Float) {
 				type = "float";
 			} else if(item instanceof Boolean) {
 				type = "boolean";
 			} else {
 				System.err.println("unknown list item type: " + item.getClass());
 			}
 			printIndent(indent, output);
 			output.print("<" + type + ">");
 			String itemString = item.toString();
 			if(itemString.length() > 40) {
 			    output.println();
 			    indentPrint(indent+1,itemString, output);
 			    printIndent(indent, output);
 			} else {
 			    output.print(itemString);
 			}
 			output.println("</" + type + ">");
 		}
 	}
 
     public static void exportID(PrintStream output, OTDataObject parent, 
             OTID id, int indent)
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
             indentPrint(indent, 
                     "<object" + " refid=\"" + id + "\"/>", output);
         } else {
             exportObject(output, childObject, indent);
         }
     }
     
 	public static void exportObject(PrintStream output, OTDataObject dataObj, int indent)
 	throws Exception
 	{
 		OTID id = dataObj.getGlobalId();
 		if(writtenIds.contains(id)) {
 			// we've seen this object for so just write a reference
 			indentPrint(indent, 
 					"<object" + " refid=\"" + id + "\"/>", output);
 			return;
 		} else {
 			writtenIds.add(id);
 		}
 		// System.err.println("writting object: " + id);		
 		
 		String objectClass = (String)dataObj.getResource(OTrunkImpl.RES_CLASS_NAME);
 		if(!writtenClasses.contains(objectClass)) {
 			writtenClasses.add(objectClass);
 		}
 		
 		int objectIndent = indent;
 		String objectElement =  objectClass;
 		if(objectElement == null) {
 		    objectElement = "object";
 		}
 		indentPrint(objectIndent, "<" + objectElement + 
 		        " id=\"" + id + "\">", output);
 		indent++;
 		String resourceKeys [] = dataObj.getResourceKeys();
 		int resourceIndent = indent;
 		
 		
 		for(int i=0; i<resourceKeys.length; i++) {
 		    
 		    // FIXME: we are ignoring special keys there should way
 		    // to identify special keys.
 			if(resourceKeys[i].equals(OTrunkImpl.RES_CLASS_NAME) ||
 					resourceKeys[i].equals("currentRevision") ||
 					resourceKeys[i].equals("localId")) {
 				continue;
 			}
 
 			Object resource = null;
 			if(dataObj instanceof XMLDataObject) {
 			    XMLDataObject xmlDataObj = (XMLDataObject)dataObj;
 			    if(xmlDataObj.isBlobResource(resourceKeys[i])){
 			        XMLBlobResource blob = xmlDataObj.getBlobResource(resourceKeys[i]);
 			        resource = blob.getBlobURL();
 			    }			    
 			} 
 			
 			if(resource == null) {
 			    resource = dataObj.getResource(resourceKeys[i]);
 			}
 			
 			printIndent(resourceIndent, output);
 			output.print("<" + resourceKeys[i] + ">");
 			if(resource instanceof OTID) {
 				// this is an object reference
 				// recurse
 			    output.println();
                 exportID(output, dataObj, (OTID)resource, resourceIndent+1);
 				printIndent(resourceIndent, output);
 			} else if(resource instanceof OTResourceList) {
 			    output.println();
 				OTResourceList list = (OTResourceList)resource;
 				for(int j=0;j<list.size(); j++) {
 					Object listElement = list.get(j);
 					if(listElement == null) {
 						System.err.println("null list item (allowed??)");
 						continue;
 					}
 					exportCollectionItem(dataObj, output, 
 							listElement, resourceIndent+1);
 				}
 				printIndent(resourceIndent, output);
 			} else if(resource instanceof OTResourceMap) {
 			    output.println();
 			    OTResourceMap map = (OTResourceMap)resource;
 			    String [] mapKeys = map.getKeys();
 			    int entryIndent = resourceIndent+1;
 			    for(int j=0; j<mapKeys.length; j++) {
 			        indentPrint(entryIndent, "<entry key=\"" + mapKeys[j] + "\">", output);
 			        Object mapValue = map.get(mapKeys[j]);
 			        if(mapValue != null) {
 			            exportCollectionItem(dataObj, output, mapValue, entryIndent+1);
 			        }
 			        indentPrint(entryIndent, "</entry>", output);			        
 			    }
 			    printIndent(resourceIndent, output);
 			} else if(resource instanceof byte []) {
 				// this is non xml blob reference
 				// write out the blob and save a reference to it
 				System.err.println("Got a non xml blob reference????");
 			} else if(resource == null){
 			    System.err.println("Got null resource value");
 			} else if(resource instanceof Integer ||
 			        resource instanceof Float ||
 			        resource instanceof Byte ||
 			        resource instanceof Short ||
 			        resource instanceof Boolean) {
 			    output.print(resource.toString());
 			} else if(resource instanceof OTXMLString) {
 			    output.println();
 				indentPrint(resourceIndent+1, 
 				        ((OTXMLString)resource).getContent(), output);
 				printIndent(resourceIndent, output);			    
 			} else if(resource instanceof String && 
 			        ((String)resource).length() < 30) {
 			    // escape xml characters
 			    String text = resource.toString();
 			    String escapedText = escapeElementText(text);
 			    output.print(escapedText);			    
 			} else {
 			    output.println();
 			    // escape xml characters
 			    String text = resource.toString();
 			    String escapedText = escapeElementText(text);
 				indentPrint(resourceIndent+1, text, output);
 				printIndent(resourceIndent, output);
 			}
 			output.println("</" + resourceKeys[i] + ">");
 		}
 		
 		indentPrint(objectIndent, "</" + objectElement + ">", output);
 	}
 	
 	public static String escapeElementText(String text)
 	{
 	    String newText = text.replaceAll("&", "&amp;");
	    newText = newText.replaceAll("<", "&lt;");
 	    newText = newText.replaceAll(">", "&gt;");
 
 	    return newText;
 	}
 }
