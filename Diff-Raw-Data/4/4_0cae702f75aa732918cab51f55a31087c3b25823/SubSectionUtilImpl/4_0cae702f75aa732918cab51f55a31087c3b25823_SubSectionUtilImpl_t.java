 /**********************************************************************************
  *
  * $URL$
  *
  ***********************************************************************************
  *
  * Copyright (c) 2008 Etudes, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Portions completed before September 1, 2008 Copyright (c) 2004, 2005, 2006, 2007, 2008 Foothill College, ETUDES Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you
  * may not use this file except in compliance with the License. You may
  * obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
  * implied. See the License for the specific language governing
  * permissions and limitations under the License.
  *
  ***********************************************************************************/
 package org.sakaiproject.component.app.melete;
 
 import java.io.StringWriter;
 import java.util.List;
 import java.util.ArrayList;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import com.sun.org.apache.xml.internal.serialize.OutputFormat;
 import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
 import org.dom4j.Document;
 import org.dom4j.DocumentException;
 import org.dom4j.DocumentHelper;
 import org.dom4j.DocumentType;
 import org.dom4j.Element;
 import org.dom4j.dtd.ElementDecl;
 import org.dom4j.dtd.AttributeDecl;
 import org.sakaiproject.api.app.melete.exception.MeleteException;
 import org.sakaiproject.util.Xml;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 /*
   * Mallika - 6/6/07 - Changed bringOneLevelUp as per ME-424
   * Mallika - 6/18/07 - Added check to not add duplicates
   */
 
 public class SubSectionUtilImpl {
 	private Document subSection4jDOM;
 	private org.w3c.dom.Document subSectionW3CDOM;
 	 private Log logger = LogFactory.getLog(SubSectionUtilImpl.class);
 	 private List xmlSecList;
 	 private org.w3c.dom.Node currParent;
 	 private org.w3c.dom.Node currLastSection;
 
 	 public SubSectionUtilImpl() {
 		 xmlSecList = new ArrayList();
 	 }
 
 
 	 private void setInternalDTD()
 	 {
 		 DocumentType docType = subSection4jDOM.getDocType();
 		 List internalDTDList = new ArrayList();
 		 internalDTDList.add(new ElementDecl("module","(section+)"));
 		 internalDTDList.add(new ElementDecl("section","(section*)"));
 		 internalDTDList.add(new AttributeDecl("section","id","ID","#REQUIRED","ID"));
 		 docType.setInternalDeclarations(internalDTDList);
 	 }
 
 	 private void createInitialModule()
 	{
 		subSection4jDOM = DocumentHelper.createDocument();
 		subSection4jDOM.addDocType("module","","");
 		setInternalDTD();
 		Element root = subSection4jDOM.addElement( "module" );
         subSection4jDOM.setRootElement(root);
 	}
 
 	public void addSection(String section_id)
 	{
 		if(subSection4jDOM == null) createInitialModule();
 		Element rootModule = subSection4jDOM.getRootElement();
 		Element newSectionElement = rootModule.addElement("section").
 											addAttribute( "id", section_id);
 	}
 
 	public Element addBlankSection()
 	{
 		Element rootModule = subSection4jDOM.getRootElement();
 		Element newSectionElement = rootModule.addElement("section");
 		return newSectionElement;
 	}
 
 	public String storeSubSections()
 	{
 		if(subSection4jDOM == null) return null;
 		return subSection4jDOM.asXML();
 	}
 
 	public void addSectiontoList(String sectionsSeqXML, String section_id)
 	{
 		try{
 			// if first section is added
 			if(sectionsSeqXML == null || sectionsSeqXML.length() == 0)
 				{
 				addSection(section_id);
 				return;
 				}
 			// add section to existing list
 			subSection4jDOM = DocumentHelper.parseText(sectionsSeqXML);
 			//The parseText call loses the internal DTD definition, so need to set it again
 			setInternalDTD();
 			Element thisElement = subSection4jDOM.elementByID(section_id);
 
 			//This code checks to see if this section id already exists in the xml string
 			if(thisElement == null) addSection(section_id);
 			else logger.debug("Trying to insert duplicate section "+section_id);
 
 		}
 		catch(DocumentException de)
 		{
 			if (logger.isDebugEnabled()) {
 			logger.debug("error reading subsections xml string" + de.toString());
 			de.printStackTrace();
 			}
 		}
 		catch (Exception ex)
 		{	if (logger.isDebugEnabled()) {
 			logger.debug("some other error on reading subsections xml string" + ex.toString());
 			ex.printStackTrace();
 			}
 		}
 	}
 	private String writeDocumentToString(org.w3c.dom.Document doc)
 	{
 		try
 		{
 			StringWriter sw = new StringWriter();
 			// Note: using xerces %%% is there a org.w3c.dom way to do this?
 			XMLSerializer s = new XMLSerializer(sw, new OutputFormat("xml", "UTF-8", false));
 			s.serialize(doc);
 
 			sw.flush();
 			return sw.toString();
 		}
 		catch (Exception any)
 		{
 			logger.warn("writeDocumentToString: " + any.toString());
 			return null;
 		}
 	}
 
 	public String MakeSubSection(String sectionsSeqXML, String section_id) throws MeleteException
 	{
 		try{
 			org.w3c.dom.Document subSectionW3CDOM =Xml.readDocumentFromString(sectionsSeqXML);
 			org.w3c.dom.Element root = subSectionW3CDOM.getDocumentElement();
 			org.w3c.dom.Element indentthisElement = subSectionW3CDOM.getElementById(section_id);
 
 	//		root.selectSingleNode("//*[@id='" + section_id +"']");
 			if(!indentthisElement.getParentNode().getFirstChild().equals(indentthisElement))
 			{
 				logger.debug("actually  creating subsection");
 				org.w3c.dom.Node indentParent = indentthisElement.getPreviousSibling();
 				indentParent.appendChild(indentthisElement);
 			}
 			return writeDocumentToString(subSectionW3CDOM);
 		}
 		catch (Exception ex)
 		{
 			if (logger.isDebugEnabled()) {
 			logger.error("some other error in creating sub section" + ex.toString());
 			ex.printStackTrace();
 			}
 			throw new MeleteException("indent_right_fail");
 		}
 	}
 
 	public String bringOneLevelUp(String sectionsSeqXML, String section_id) throws MeleteException
 	{
 		try{
 			org.w3c.dom.Document subSectionW3CDOM =Xml.readDocumentFromString(sectionsSeqXML);
 			org.w3c.dom.Element root = subSectionW3CDOM.getDocumentElement();
 			org.w3c.dom.Element bringUpThisElement = subSectionW3CDOM.getElementById(section_id);
 
 			if(bringUpThisElement == null) {throw new MeleteException("indent_left_fail");}
 			org.w3c.dom.Node makeSiblingOf = bringUpThisElement.getParentNode();
 			org.w3c.dom.Node bringUpBeforeThisElement = makeSiblingOf.getNextSibling();
 
 			//Clone the node that needs to be moved
 			org.w3c.dom.Node newNode = bringUpThisElement.cloneNode(true);
 			org.w3c.dom.Node nextNode = bringUpThisElement.getNextSibling();
 		    org.w3c.dom.Node prevNode = null;
 		    //Iterate through each of the node's siblings and make them its children
 		    //In the process, also delete the siblings
 				while (nextNode != null)
 				{
 					org.w3c.dom.Node cNode = nextNode.cloneNode(true);
 					prevNode = nextNode;
 					newNode.appendChild(cNode);
 					nextNode = nextNode.getNextSibling();
 					prevNode.getParentNode().removeChild(prevNode);
 				}
 				//Insert the new node, inbetween or end of list, takes null or bringUpBeforeThisElement
 				makeSiblingOf.getParentNode().insertBefore(newNode,bringUpBeforeThisElement);
 				//Delete node from original position
 				bringUpThisElement.getParentNode().removeChild(bringUpThisElement);
 
 			return writeDocumentToString(subSectionW3CDOM);
 		}
 		catch (MeleteException mex)
 		{	throw mex;
 		}
 		catch (Exception ex)
 		{
 			if (logger.isDebugEnabled()) {
 			logger.error("some other error on indenting right" + ex.toString());
 			ex.printStackTrace();
 			}
 			throw new MeleteException("indent_right_fail");
 		}
 	}
 	public String deleteSection(String sectionsSeqXML, String section_id) throws MeleteException
 	{
 		try{
 			org.w3c.dom.Document subSectionW3CDOM =Xml.readDocumentFromString(sectionsSeqXML);
 			org.w3c.dom.Element root = subSectionW3CDOM.getDocumentElement();
 			org.w3c.dom.Element deleteThisElement = subSectionW3CDOM.getElementById(section_id);
			if (deleteThisElement != null)
			{	
 			org.w3c.dom.Node deleteElementParent =deleteThisElement.getParentNode();
 
 			// child nodes becomes children of parent node
 			if(deleteThisElement.hasChildNodes())
 			{
 				NodeList children = deleteThisElement.getChildNodes();
 				 for ( int i=0; i < children.getLength(); i++ ) {
 			        	org.w3c.dom.Node deleteThisElementChild = children.item(i);
 			        	if(deleteThisElementChild.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE)
 			         		deleteElementParent.insertBefore(deleteThisElementChild.cloneNode(true), deleteThisElement) ;
 				 }
 			}
 
 			//remove the element
 			deleteElementParent.removeChild(deleteThisElement);
			}
 
 			return writeDocumentToString(subSectionW3CDOM);
 		}
 		catch (Exception ex)
 		{
 			if (logger.isDebugEnabled()) {
 			logger.debug("some other error on delete subsections xml string" + ex.toString());
 			ex.printStackTrace();
 			}
 			throw new MeleteException("delete_module_fail");
 		}
 	}
 
 	public String moveAllUpSection(String sectionsSeqXML, String section_id)
 	throws MeleteException
 	        {
 	            try{
 	                org.w3c.dom.Document subSectionW3CDOM
 	=Xml.readDocumentFromString(sectionsSeqXML);
 	                org.w3c.dom.Element root = subSectionW3CDOM.getDocumentElement();
 	                org.w3c.dom.Element moveUpThisElement =
 	subSectionW3CDOM.getElementById(section_id);
 	                org.w3c.dom.Node moveUpThisElementParent =
 	moveUpThisElement.getParentNode();
 	                org.w3c.dom.Node cloneMoveElement = moveUpThisElement.cloneNode(true);
 
 	                org.w3c.dom.Node FirstChildOfmoveUpThisElementParent =
 	moveUpThisElementParent.getFirstChild();
 	                if(!FirstChildOfmoveUpThisElementParent.equals(moveUpThisElement))
 	                {
 	                moveUpThisElementParent.insertBefore(cloneMoveElement,
 	FirstChildOfmoveUpThisElementParent);
 	                moveUpThisElementParent.removeChild(moveUpThisElement);
 	                }
 
 	                return writeDocumentToString(subSectionW3CDOM);
 	            }
 	            catch (Exception ex)
 	            {
 					if (logger.isDebugEnabled()) {
 	                logger.debug("some other error on moving up subsections xml string" +
 	ex.toString());
 	                ex.printStackTrace();
 					}
 	                throw new MeleteException("move_up_fail");
 	            }
 	    }
 
 	public String moveUpSection(String sectionsSeqXML, String section_id) throws MeleteException
 	{
 		try{
 			org.w3c.dom.Document subSectionW3CDOM =Xml.readDocumentFromString(sectionsSeqXML);
 			org.w3c.dom.Element root = subSectionW3CDOM.getDocumentElement();
 			org.w3c.dom.Element moveUpThisElement = subSectionW3CDOM.getElementById(section_id);
 			org.w3c.dom.Node moveUpThisElementParent = moveUpThisElement.getParentNode();
 			org.w3c.dom.Node cloneMoveElement = moveUpThisElement.cloneNode(true);
 
 			if(!moveUpThisElementParent.getFirstChild().equals(moveUpThisElement))
 			{
 			org.w3c.dom.Node beforeElement = moveUpThisElement.getPreviousSibling();
 			moveUpThisElementParent.insertBefore(cloneMoveElement, beforeElement);
 			moveUpThisElementParent.removeChild(moveUpThisElement);
 			}
 
 			return writeDocumentToString(subSectionW3CDOM);
 		}
 		catch (Exception ex)
 		{
 			if (logger.isDebugEnabled()) {
 			logger.debug("some other error on moving up subsections xml string" + ex.toString());
 			ex.printStackTrace();
 			}
 			throw new MeleteException("move_up_fail");
 		}
 	}
 
 	public String moveDownSection(String sectionsSeqXML, String section_id) throws MeleteException
 	{
 		try{
 			org.w3c.dom.Document subSectionW3CDOM =Xml.readDocumentFromString(sectionsSeqXML);
 			org.w3c.dom.Element root = subSectionW3CDOM.getDocumentElement();
 			org.w3c.dom.Element moveDownThisElement = subSectionW3CDOM.getElementById(section_id);
 			org.w3c.dom.Node afterElementParent = moveDownThisElement.getParentNode();
 			if(!afterElementParent.getLastChild().equals(moveDownThisElement))
 			{
 			org.w3c.dom.Node afterElement = moveDownThisElement.getNextSibling();
 			org.w3c.dom.Node cloneafterElement = afterElement.cloneNode(true);
 			afterElementParent.insertBefore(cloneafterElement, moveDownThisElement);
 			afterElementParent.removeChild(afterElement);
 			}
 			return writeDocumentToString(subSectionW3CDOM);
 		}
 		catch (Exception ex)
 		{
 			if (logger.isDebugEnabled()) {
 			logger.debug("some other error on moving down subsections xml string" + ex.toString());
 			ex.printStackTrace();
 			}
 			throw new MeleteException("move_down_fail");
 		}
 	}
 
 	public String moveAllDownSection(String sectionsSeqXML, String section_id)
 	throws MeleteException
 	        {
 	            try{
 	                org.w3c.dom.Document subSectionW3CDOM=Xml.readDocumentFromString(sectionsSeqXML);
 	                org.w3c.dom.Element root = subSectionW3CDOM.getDocumentElement();
 	                org.w3c.dom.Element moveDownThisElement =subSectionW3CDOM.getElementById(section_id);
 	                org.w3c.dom.Node cloneMoveElement = moveDownThisElement.cloneNode(true);
 	                org.w3c.dom.Node afterElementParent =moveDownThisElement.getParentNode();
 	                org.w3c.dom.Node LastChildOfafterElementParent=afterElementParent.getLastChild();
 	                org.w3c.dom.Node cloneLastChildElement = LastChildOfafterElementParent.cloneNode(true);
 
 	                if(!LastChildOfafterElementParent.equals(moveDownThisElement))
 	                {
 	                	afterElementParent.replaceChild(cloneMoveElement,LastChildOfafterElementParent);
 	                	org.w3c.dom.Node newLastChild=afterElementParent.getLastChild();
 	                	afterElementParent.insertBefore(cloneLastChildElement,newLastChild);
 	                	afterElementParent.removeChild(moveDownThisElement);
 	                }
 	                return writeDocumentToString(subSectionW3CDOM);
 	            }
 	            catch (Exception ex)
 	            {
 					if (logger.isDebugEnabled()) {
 	                logger.debug("some other error on moving down subsections xml string"
 	+ ex.toString());
 	                ex.printStackTrace();
 					}
 	                throw new MeleteException("move_down_fail");
 	            }
 	    }
 /*
  *
  */
 	public org.w3c.dom.Element getNextSection(org.w3c.dom.Element currItem) throws Exception
 	{
 		if(currItem == null)
 			{
 			currParent = subSectionW3CDOM.getDocumentElement();
 			currLastSection = (org.w3c.dom.Element)currParent.getLastChild();
 			return (org.w3c.dom.Element)currParent.getFirstChild();
 			}
 
 		if (currItem.hasChildNodes())
 			{
 			currParent = currItem;
 			return (org.w3c.dom.Element)currItem.getFirstChild();
 			}
 
 		if(currParent == null) currParent = currItem.getParentNode();
 
 		if (currItem.equals((org.w3c.dom.Element)currParent.getLastChild()))
 		{
 			while(true)
 			{
 			if(currParent.getNodeType() == Node.DOCUMENT_NODE) return null;
 			if(!currParent.equals(currLastSection) && !currItem.equals(currLastSection))
 				{
 				currItem = (org.w3c.dom.Element)currParent.getNextSibling();
 				if (currItem == null) {
 					logger.debug("going a level up to fetch sibling");
 					currItem = (org.w3c.dom.Element)currParent;
 					currParent = currItem.getParentNode();
 					continue;
 				}
 				currParent = currItem.getParentNode();
 				return currItem;
 				} else return null;
 			}
 		}
 		else return (org.w3c.dom.Element)currItem.getNextSibling();
 	}
 
 	/*
 	 *
 	 */
 	public org.w3c.dom.Document getSubSectionW3CDOM(String sectionsSeqXML)
 	{
 		try{
 			subSectionW3CDOM =Xml.readDocumentFromString(sectionsSeqXML);
 		}
 		catch (Exception ex)
 		{
 			if (logger.isDebugEnabled()) {
 			logger.debug("Error in getting subsections dom " + ex.toString());
 			ex.printStackTrace();
 			}
 		}
 		return subSectionW3CDOM;
 	}
 
 	public Document createSubSection4jDOM()
 	{
 		if(subSection4jDOM == null) createInitialModule();
 		return subSection4jDOM;
 	}
 
 	public void traverseDom(String sectionsSeqXML,String dispSeq)
 	{
 		if (sectionsSeqXML != null)
 		{
 		  getSubSectionW3CDOM(sectionsSeqXML);
 		  if (subSectionW3CDOM != null)
 		  {
 		  processDom(subSectionW3CDOM.getDocumentElement(),0,dispSeq,1);
 		  }
 		}
 	}
 
 	private void processDom(org.w3c.dom.Node node, int level, String dispSeq, int displayNum)
 	{
 		org.w3c.dom.NodeList nl = node.getChildNodes();
 		StringBuffer dispSeqBuffer;
 		String displaySequence;
 		for(int i=0, cnt=nl.getLength(); i<cnt; i++)
 		{
 			if(nl.item(i).getNodeType() == org.w3c.dom.Node.ELEMENT_NODE)
 			{
 			dispSeqBuffer = new StringBuffer();
 			dispSeqBuffer.append(dispSeq);
 			dispSeqBuffer.append(".");
 			dispSeqBuffer.append(displayNum++);
 			displaySequence = dispSeqBuffer.toString();
 			dispSeqBuffer = null;
 			xmlSecList.add(new SecLevelObj(Integer.parseInt(((org.w3c.dom.Element)nl.item(i)).getAttribute("id")),level,displaySequence));
 			processDom(nl.item(i), level+1,displaySequence, 1);
 			}
 		}
 	}
 	public List getXmlSecList() {
 		return xmlSecList;
 	}
 
 
 	public List getAllSections(String modSeqXml) throws Exception
 	{
 		List allsections = null;
 		subSection4jDOM = DocumentHelper.parseText(modSeqXml);
 		setInternalDTD();
 		Element root = subSection4jDOM.getRootElement();
 		allsections = subSection4jDOM.selectNodes("//section");
 		return allsections;
 	}
 
 	public org.w3c.dom.Element getPrevSection(org.w3c.dom.Element currItem) throws Exception
 	{
 		org.w3c.dom.Element rootElement = subSectionW3CDOM.getDocumentElement();
 		org.w3c.dom.Element returnElement = null;
 		if(currItem == rootElement.getFirstChild())
 			{
 			return null;
 			}
 		org.w3c.dom.Node returnParrent = currItem.getParentNode();
 		returnElement = (org.w3c.dom.Element)currItem.getPreviousSibling();
 		if(returnElement == null)
 		{
 			returnElement = (org.w3c.dom.Element)returnParrent;
 		}
 		return returnElement;
 	}
 }
 
 
 class SecLevelObj{
 	int sectionId, level;
 	String dispSeq;
 	public SecLevelObj(int sectionId, int level, String dispSeq)
 	{
 		this.sectionId = sectionId;
 		this.level = level;
 		this.dispSeq = dispSeq;
 	}
 	public int getSectionId()
 	{
 		return sectionId;
 	}
 	public int getLevel()
 	{
 		return level;
 	}
 	public String getDispSeq()
 	{
 		return dispSeq;
 	}
 }
