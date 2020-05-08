 /*
  * Last modification information:
 * $Revision: 1.2 $
 * $Date: 2005-03-10 06:01:38 $
  * $Author: scytacki $
  *
  * Licence Information
  * Copyright 2004 The Concord Consortium 
 */
 package org.concord.otrunk.xml;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.net.URL;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Vector;
 
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.input.SAXBuilder;
 
 
 /**
  * SchemaGenerator
  * Class name and description
  *
  * Date created: Dec 9, 2004
  *
  * @author scott<p>
  *
  */
 public class SchemaGenerator
 {
 	public static void main(String args[])
 		throws Exception
 	{
 		File xmlFile = new File(args[0]);
 		FileInputStream xmlStream = new FileInputStream(xmlFile);
 		
 		URL contextURL = xmlFile.toURL();
 		
 		// parse the xml file...
 		TypeService typeService = new TypeService(contextURL);
 		ObjectTypeHandler objectTypeHandler = new ObjectTypeHandler(typeService, null);
 		typeService.registerUserType("object", objectTypeHandler);
 
 		SAXBuilder builder = new SAXBuilder();
 		Document document = builder.build(xmlStream);
 		
 		Element rootElement = document.getRootElement();
 
 		
 		Vector importedOTObjectClasses = new Vector();
 				
 		Element importsElement = rootElement;
 		List imports = importsElement.getChildren();		
 		for(Iterator iterator=imports.iterator();iterator.hasNext();) {
 			Element currentImport=(Element)iterator.next();
 			String className = currentImport.getAttributeValue("class");
 			importedOTObjectClasses.add(className);
 		}		
 		
 		ReflectionTypeDefinitions.registerTypes(importedOTObjectClasses, typeService,
				null, false);	
 		
 		// the type service should now have all registers types so we can write out
 		// the schema
 		Hashtable typeMap = typeService.getHandlerMap();
 
 		Vector printedResources = new Vector();
 		
 		Set entries = typeMap.entrySet();
 		for(Iterator entryItr = entries.iterator(); entryItr.hasNext();) {
 			Map.Entry entry = (Map.Entry)entryItr.next();
 			if(entry.getValue() instanceof ObjectTypeHandler){
 				if(entry.getKey().equals("object")) {
 					continue;
 				}
 
 				printedResources.clear();
 				
 				System.out.println("" + entry.getKey());
 				
 				ObjectTypeHandler objectType = (ObjectTypeHandler)entry.getValue();
 				ResourceDefinition [] resDefs = objectType.getResourceDefinitions();
 				if(resDefs == null) {
 					System.out.println("   no resource definitions ");
 				}
 				for(int i=0; i<resDefs.length; i++) {
 					String resName = resDefs[i].name;
 					if(printedResources.contains(resName)) {
 						continue;
 					}
 					int numSpaces = 20 - resName.length();
 					String spaces = "";
 					for(int j=0; j<numSpaces; j++) spaces += " ";
 					System.out.println("  " + resName + spaces + " : " +
 							resDefs[i].type);
 					printedResources.add(resName);
 				}
 				
 				System.out.println();
 			}
 		}	
 	}
 	
 }
