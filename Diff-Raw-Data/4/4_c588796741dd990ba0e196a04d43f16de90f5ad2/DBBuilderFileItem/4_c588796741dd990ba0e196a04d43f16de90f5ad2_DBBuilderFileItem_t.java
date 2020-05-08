 package com.silverpeas.dbbuilder;
 
 /**
  * Titre :        dbBuilder
  * Description :  Builder des BDs Silverpeas
  * Copyright :    Copyright (c) 2001
  * Socit :      Stratlia Silverpeas
  * @author ATH
  * @version 1.0
  */
 
 import org.jdom.*;
 import java.util.*;
 
 public class DBBuilderFileItem extends DBBuilderItem {
 
         public DBBuilderFileItem(DBXmlDocument fileXml) throws Exception {
 
 		setFileXml(fileXml);
 
 		setRoot( ((org.jdom.Document) fileXml.getDocument().clone()).getRootElement() ); // Get the root element
 
 		// rcupre le nom du module une fois pour toutes
        		super.setModule(getRoot().getAttributeValue(MODULENAME_ATTRIB));
         }
 
  @Override
 	public String getVersionFromFile() throws Exception {
 
 		if (versionFromFile == null) {
 
 	        	List listeCurrent = getRoot().getChildren(CURRENT_TAG);
 
			if (listeCurrent == null || listeCurrent.size() ==  0)
 	        		throw new Exception(getModule() + ": no <" + CURRENT_TAG + "> tag found for this module into contribution file.");
 
 			if (listeCurrent.size() != 1)
 	        		throw new Exception(getModule() + ": tag <" + CURRENT_TAG + "> appears more than one.");
 
 			Iterator iterCurrent = listeCurrent.iterator();
 	        	while ( iterCurrent.hasNext() ) {
 
 				Element eltCurrent = ( Element ) iterCurrent.next();
 	        		versionFromFile = eltCurrent.getAttributeValue(VERSION_ATTRIB);
 		        } // while
 
 		} // if
 
 		return versionFromFile;
 	}
 
 }
