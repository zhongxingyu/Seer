 /**********************************************************************************
  *
  * $URL$
  * $Id$
  ***********************************************************************************
  *
  * Copyright (c) 2008,2009 Etudes, Inc.
  *
  * Portions completed before September 1, 2008 Copyright (c) 2004, 2005, 2006, 2007, 2008 Foothill College, ETUDES Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
  * implied. See the License for the specific language governing
  * permissions and limitations under the License.
  *
  **********************************************************************************/
 
 package org.etudes.component.app.melete;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.URLDecoder;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.dom4j.Document;
 import org.dom4j.DocumentException;
 import org.dom4j.DocumentHelper;
 import org.dom4j.Element;
 import org.dom4j.Namespace;
 import org.dom4j.QName;
 import org.etudes.component.app.melete.MeleteUtil;
 import org.etudes.api.app.melete.MeleteCHService;
 import org.etudes.api.app.melete.MeleteExportService;
 import org.etudes.api.app.melete.MeleteSecurityService;
 import org.etudes.api.app.melete.exception.MeleteException;
 import org.etudes.api.app.melete.util.XMLHelper;
 import org.xml.sax.SAXException;
 import org.sakaiproject.component.cover.ServerConfigurationService;
 import org.sakaiproject.util.Validator;
 import org.sakaiproject.entity.cover.EntityManager;
 import org.sakaiproject.entity.api.Reference;
 import org.sakaiproject.content.api.ContentResource;
 import org.sakaiproject.entity.api.ResourceProperties;
 import org.sakaiproject.exception.IdUnusedException;
 import org.sakaiproject.content.cover.ContentHostingService;
 import org.sakaiproject.entity.api.Entity;
 
 /**
  * @author Faculty
  *
  * TODO To change the template for this generated type comment go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 public class MeleteExportServiceImpl  extends MeleteAbstractExportServiceImpl implements MeleteExportService{
 
 	public void initValues()
 	{
 	 setMetaDataNameSpace("http://www.imsglobal.org/xsd/imsmd_v1p2");
 	 setSchema("IMS Content");
 	 setSchemaVersion("1.1.4");
 	 setLangString("langstring");
 	}
 
 
 	/*
 	 * create copyright element
 	 * add by rashmi
 	 */
 	public Element createMetadataCopyright(int licenseCode)
 	{
 		//imsmd:copyright
 		Element mdCopyright = createLOMElement("imsmd:copyrightandotherrestrictions", "copyrightandotherrestrictions");
 
 		Element mdSource = createLOMElement("imsmd:source", "source");
 		Element mdLangString = createLOMElement("imsmd:"+getLangString(), getLangString());
 		mdLangString.setText("Melete");
 		mdSource.add(mdLangString);
 		mdCopyright.add(mdSource);
 		// if public domain then no restrictions are applied
 		// and for all other licenses restrictions are applied
 		Element mdValue = createLOMElement("imsmd:value", "value");
 		Element mdLangString1 = createLOMElement("imsmd:"+getLangString(), getLangString());
 		if(licenseCode != RESOURCE_LICENSE_PD_CODE)
 			mdLangString1.setText("yes");
 		else mdLangString1.setText("no");
 		mdValue.add(mdLangString1);
 		mdCopyright.add(mdValue);
 
 		return mdCopyright;
 	}
 
 	/*
 	 *  process section type and create resource element object
 	 */
 	public void createResourceElement(Section section, Element resource, byte[] content_data1, File resoucesDir, String imagespath, String sectionFileName,int item_ref_num) throws Exception
 	{
 		logger.debug("create resource ele" + section.getContentType());
 		if (section.getContentType().equals("typeLink"))
 		{
 			String linkData = new String(content_data1);
 			
 			Set recordFiles = (Set)exportThreadLocal.get("MeleteExportFiles");
 			if (recordFiles != null) recordFiles.add(sectionFileName);
 			
 		// LINK POINTS TO SITE RESOURCES OR MELETEDOCS FILE	
 			if(linkData.indexOf("/access/content/group")!= -1|| linkData.indexOf("/access/meleteDocs")!= -1)
 			{
 				ArrayList<String> r = meleteUtil.findResourceSource(linkData, null, null, false);
 				if (r == null) return;
 				String link_resource_id = r.get(0);
 				
 				// read resource and create a file
 				ArrayList link_content = new ArrayList();
 				byte[] linkdata =setContentResourceData(link_resource_id, link_content);
 				if(linkdata == null) {resource =null;return;}	
 				//get referred resource
 				logger.debug("link resource id and first element " + link_resource_id + (String)link_content.get(0));
 				createFileElement(link_resource_id, linkdata,resource,imagespath, resoucesDir, "resources/", false, false);
 			}
 //			 resource will always point to link location otherwise it changes type to upload on import
 			resource.addAttribute("href", linkData);
 			// preserve url title
 			if(!sectionFileName.equals(linkData))
 			{
 			Element urlTitle = createLOMElement("imsmd:title", "title");
 			Element imsmdlangstring = createLOMElement("imsmd:"+getLangString(), getLangString());
 	        imsmdlangstring.setText(sectionFileName);
 	        urlTitle.add(imsmdlangstring);
 	        resource.add(urlTitle);
 			}
 		// COMPOSE SECTIONS	
 		}else if (section.getContentType().equals("typeEditor")){
 			createFileElement(sectionFileName, content_data1,resource,imagespath, resoucesDir,"resources/", false, true);
 		
 		// LTI RESOURCE	
 		} else if (section.getContentType().equals("typeLTI")){
 			resource.addAttribute("type ","SimpleLTI");			
 			String fileName = "simplelti-"+item_ref_num;			
 			createFileElement(fileName, content_data1,resource,imagespath, resoucesDir,"resources/", false, true);
 
 			// add title
 			Element urlTitle = createLOMElement("imsmd:title", "title");
 			Element imsmdlangstring = createLOMElement("imsmd:"+getLangString(), getLangString());
 			imsmdlangstring.setText(sectionFileName);
 			urlTitle.add(imsmdlangstring);
 			resource.add(urlTitle);
 			
 			Set recordFiles = (Set)exportThreadLocal.get("MeleteExportFiles");
 			if (recordFiles != null) recordFiles.add(sectionFileName);
 			
 		// UPLOAD RESOURCE	
 		}else if(section.getContentType().equals("typeUpload")){            
             createFileElement(sectionFileName, content_data1,resource,imagespath, resoucesDir,"resources/", false,true);			
 		}
 	}
 
 	public int createSectionElement(Element ParentSection, Section section, int i, int k, Element resources, File resoucesDir, String imagespath) throws Exception
 	{
 			Element secElement = ParentSection.addElement("item");
 			secElement.addAttribute("identifier", "ITEM"+ k);
 			Element secTitleEle = secElement.addElement("title");
 			secTitleEle.setText(section.getTitle());
 			int item_ref_num = k;
 			logger.debug("now processing createSectionElement" + section.getTitle());
 			// dtd specifies nested item tag to be before imsmd tags.
 			if(currItem.hasChildNodes())
 			{
 				int size = currItem.getChildNodes().getLength();
 				logger.debug("processing childNodes of " + section.getTitle() + "and no of child nodes are:" +  size);
 				int childNo = 0;
 				while(childNo < size)
 				{
 						currItem = sectionUtil.getNextSection(currItem);
 						k = createSectionElement(secElement, sectionDB.getSection(Integer.parseInt(currItem.getAttribute("id"))), i,++k, resources,resoucesDir,imagespath);
 						childNo++;
 				}
 			}
 
 			Element imsmdlom = createLOMElement("imsmd:lom", "lom");
 			//add section instructions
 			if (section.getInstr() != null && section.getInstr().trim().length() > 0)
 			   {
 				Element imsmdgeneral = imsmdlom.addElement("imsmd:general");
 				imsmdgeneral.add(createMetadataDescription(section.getInstr()));
 			   }
 			// add section instructions end
 
 			// if content exists then create resource object otherwise just create item object
 			if(section.getSectionResource() != null)
 			{
 				MeleteResource meleteResource = (MeleteResource)section.getSectionResource().getResource();
 				if(meleteResource == null) return k;
 				String content_resource_id = meleteResource.getResourceId();
 				ArrayList content_data = new ArrayList();
 				logger.debug("calling secContent from create section");
 				byte[] content_data1 =setContentResourceData(content_resource_id,content_data);
 
 				if(content_data1 == null || content_data == null) return k;
 				//Rashmi - if no resources are written then see if createResourceElement needs a return type
 				Element resource = resources.addElement("resource");
 				resource.addAttribute("identifier","RESOURCE"+ item_ref_num);
 				resource.addAttribute("type ","webcontent");
 				createResourceElement(section, resource, content_data1, resoucesDir, imagespath,(String)content_data.get(0),item_ref_num);
 
 					//preserve resource description
 				if (content_data.get(1) != null && ((String)content_data.get(1)).length() != 0)
 					resource.add(createMetadataDescription((String)content_data.get(1)));
 
 				secElement.addAttribute("identifierref", resource.attributeValue("identifier"));
 				// add copyright information - rashmi
 				Element imsmdright = imsmdlom.addElement("imsmd:rights");
 				imsmdright.add(createMetadataCopyright(meleteResource.getLicenseCode()));
 
 				// add license description
 				Element mdLicenseDesc = createLOMElement("imsmd:description", "description");
 				Element mdLangString2 = createLOMElement("imsmd:"+getLangString(), getLangString());
 				String lurl = createLicenseUrl(meleteResource.getLicenseCode(),meleteResource.getCcLicenseUrl(),meleteResource.getCopyrightOwner(),meleteResource.getCopyrightYear());
 				mdLangString2.setText(lurl);
 				mdLicenseDesc.add(mdLangString2);
 				imsmdright.add(mdLicenseDesc);
 				// copyright info add end
 			}	// end if contents
 			secElement.add(imsmdlom);
 	return k;
 	}
 	/**
 	 * adds organization and resource items tomanifest
 	 * @param modDateBeans - module date beans
 	 * @param packagedir - package directory
 	 * @return - returns the list of manifest elements
 	 * @throws Exception
 	 */
 	public List generateOrganizationResourceItems(List modList,boolean allFlag, File packagedir,String maintitle, String courseId)throws Exception{
 		String probEncounteredSections ="";
 		try{
 			String packagedirpath = packagedir.getAbsolutePath();
 			String resourcespath  = packagedirpath + File.separator + "resources";
 			File resoucesDir = new File(resourcespath);
 			if (!resoucesDir.exists())resoucesDir.mkdir();
 			String imagespath  = resoucesDir.getAbsolutePath() + File.separator + "images";
 
 			Element organizations = createOrganizations();
 			Element resources = createResources();
 			Element organization = addOrganization(organizations);
 			organizations.addAttribute("default", organization.attributeValue("identifier"));
 			
 		// record all files being added in the package
 			exportThreadLocal.set("MeleteExportFiles", new HashSet<String>());
 			Iterator modIter = modList.iterator();
 			int i = 0,k=0;
 			//create item for each module and items under the module item for
 			// scetions
 			while (modIter.hasNext()){
 				Module module = (Module) modIter.next();
 
 				Element modMainItem = organization.addElement("item");
 				modMainItem.addAttribute("identifier", "MF01_ORG1_MELETE_MOD"+ ++i);
 
 				if (module.getCoursemodule().isArchvFlag() == true)
 				{
 					modMainItem.addAttribute("isvisible","false");
 				}
 				Element title = modMainItem.addElement("title");
 				if (module.getTitle() != null && module.getTitle().trim().length() > 0)
 					title.setText(module.getTitle());
 
 				String sectionsSeqList = module.getSeqXml();
 				sectionUtil = new SubSectionUtilImpl();
 
 				if (sectionsSeqList != null){
 					sectionUtil.getSubSectionW3CDOM(sectionsSeqList);
 					currItem = null;
 					//create items and resources for sections
 					while ((currItem = sectionUtil.getNextSection(currItem)) != null){
 						try{
 							logger.debug("exporting item from generateOrgan" + currItem.getAttribute("id"));
 							Section section = sectionDB.getSection(Integer.parseInt(currItem.getAttribute("id")));
 
 							// create secElement only if data exists
 							logger.debug("exporting section from generateOrgan" + section.getTitle());
 							k = createSectionElement(modMainItem, section, i,++k, resources,resoucesDir,imagespath);
 							} // if end add secElement only if content exists
 						catch(Exception e){
 							/*Section probSection = sectionDB.getSection(Integer.parseInt(currItem.getAttribute("id")));
 							probEncounteredSections += module.getTitle() +" section: "+ probSection.getTitle();
 							logger.debug("problems found in export impl" + probEncounteredSections);*/
 					//		throw new MeleteException(probEncounteredSections);
 							continue;
 							}
 					}
 
 
 				}
 //				 add next steps as the last section of the module by rashmi
 				if (module.getWhatsNext() != null && module.getWhatsNext().trim().length() > 0)
 				{
 					Element whatsNextElement = modMainItem.addElement("item");
 					whatsNextElement.addAttribute("identifier", "NEXTSTEPS"+ ++k);
 
 					Element nextTitleEle = whatsNextElement.addElement("title");
 					nextTitleEle.setText("NEXTSTEPS");
 
 					Element resource = resources.addElement("resource");
 					resource.addAttribute("identifier","RESOURCE"+ k);
 					resource.addAttribute("type ","webcontent");
 
 //					create the file
 					File resfile = new File(resoucesDir+ "/module_"+ i +"_nextsteps.html");
 					createFileFromContent( module.getWhatsNext().getBytes(), resfile.getAbsolutePath());
 					whatsNextElement.addAttribute("identifierref", resource.attributeValue("identifier"));
 					Element file = resource.addElement("file");
 					file.addAttribute("href", "resources/module_"+ i +"_nextsteps.html");
 					resource.addAttribute("href", "resources/module_"+ i +"_nextsteps.html");
 				}
 			// add next steps end
 				//add module description thru metadata
 				Element imsmdlom = createLOMElement("imsmd:lom", "lom");
 				Element imsmdgeneral = imsmdlom.addElement("imsmd:general");
 
 				if (module.getDescription() != null && module.getDescription().trim().length() > 0)
 					imsmdgeneral.add(createMetadataDescription(module.getDescription()));
 
 				// add keyword if available - rashmi
 				if (module.getKeywords() != null && module.getKeywords().trim().length() > 0)
 					imsmdgeneral.add(createMetadataKeyword(module.getKeywords()));
 
 				modMainItem.add(imsmdlom);
 
 			}
 			if (allFlag == true) resources = transferManageItems(resources, courseId, resoucesDir, k+1);
 			ArrayList manElements = new ArrayList();
 			manElements.add(organizations);
 			manElements.add(resources);
 			manElements.add(probEncounteredSections);
 			return manElements;
 
 		}catch(Exception e){
 			logger.debug("i am catching it");
 			throw e;
 		}
 
 	}
 	
 	public Element transferManageItems(Element resources, String courseId, File resoucesDir, int item_ref_num) throws Exception
 	{
 		logger.debug("recorded files so far: " + exportThreadLocal.get("MeleteExportFiles").toString());
		String imagespath  = resoucesDir.getAbsolutePath();

 		String fromUploadsColl = getMeleteCHService().getUploadCollectionId(courseId);
 		List fromContextList = meleteCHService.getMemberNamesCollection(fromUploadsColl);
 		if ((fromContextList == null)||(fromContextList.size() == 0)) return null;
 
 		List meleteResourceList = null;
 		Set recordFiles = (Set)exportThreadLocal.get("MeleteExportFiles");
 		if(recordFiles != null && recordFiles.size()> 0)
 		{
 			List<String> l = new ArrayList<String>(recordFiles);
 			List<String> new_l = new ArrayList<String>();
 			for(String recordName : l)
 			{
 				recordName = "/private/meleteDocs/" +courseId +"/uploads/" + recordName;
 				new_l.add(recordName);
 			}
 			fromContextList.removeAll(new_l);
 		}
 		logger.debug("left in fromlist collection:" + fromContextList.toString());
 		if ((fromContextList != null)&&(fromContextList.size() > 0))
 		{
 			ListIterator repIt = fromContextList.listIterator();
 			while (repIt != null && repIt.hasNext())
 			{
 				String content_resource_id = (String) repIt.next();
 				ArrayList content_data = new ArrayList();
 
 				Element resource = resources.addElement("resource");
 				resource.addAttribute("identifier","MANAGERESOURCE"+ item_ref_num);
 				resource.addAttribute("type ","webcontent");
 
 				byte[] content_data1 =setContentResourceData(content_resource_id,content_data);
 				String sectionFileName = (String)content_data.get(0);
 				Section sec = new Section();
 				String type = (String)content_data.get(2);
 				if(type.equals(getMeleteCHService().MIME_TYPE_LINK)) sec.setContentType("typeLink");
 				else if (type.equals(getMeleteCHService().MIME_TYPE_LTI)) sec.setContentType("typeLTI");
 				else sec.setContentType("typeUpload");
				createResourceElement(sec, resource, content_data1, resoucesDir, imagespath,sectionFileName,item_ref_num);
 				item_ref_num++;
 			}//End while repIt
 		}
 		return resources;
 	}	
 }
