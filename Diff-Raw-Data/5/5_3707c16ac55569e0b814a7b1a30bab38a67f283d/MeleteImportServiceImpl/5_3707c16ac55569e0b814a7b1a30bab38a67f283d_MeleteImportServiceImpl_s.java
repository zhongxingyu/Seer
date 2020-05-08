 /**********************************************************************************
 *
 * $Header:
 *
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007 Foothill College, ETUDES Project
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
 **********************************************************************************/
 package org.sakaiproject.component.app.melete;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.net.MalformedURLException;
 import java.net.UnknownHostException;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.dom4j.Attribute;
 import org.dom4j.Document;
 import org.dom4j.Element;
 import org.dom4j.Namespace;
 import org.dom4j.Node;
 import org.dom4j.QName;
 import org.dom4j.XPath;
 import org.sakaiproject.api.app.melete.MeleteCHService;
 import org.sakaiproject.api.app.melete.MeleteImportService;
 import org.sakaiproject.api.app.melete.MeleteSecurityService;
 import org.sakaiproject.component.app.melete.MeleteUtil;
 import org.sakaiproject.component.cover.ServerConfigurationService;
 import org.sakaiproject.content.cover.ContentTypeImageService;
 import org.sakaiproject.content.cover.ContentHostingService;
 import org.sakaiproject.content.api.ContentResource;
 import org.sakaiproject.content.api.ContentCollection;
 import org.sakaiproject.entity.api.ResourceProperties;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.sakaiproject.tool.cover.ToolManager;
 import org.sakaiproject.user.cover.UserDirectoryService;
 import org.sakaiproject.api.app.melete.exception.MeleteException;
 import org.sakaiproject.entity.api.Entity;
 import org.sakaiproject.entity.api.ResourcePropertiesEdit;
 import org.sakaiproject.exception.IdUnusedException;
 import org.sakaiproject.util.Validator;
 import org.sakaiproject.entity.cover.EntityManager;
 import org.sakaiproject.entity.api.Reference;
 /**
  * MeleteImportServiceImpl is the implementation of MeleteImportService
  * that provides the methods for import export
  *
  * @author Murthy @ Foothill college
  * @version
  *
  * Mallika - 2/2/07 - adding check on import too for missing files
  * Rashmi - 2/6/07 -  revised wrt Content hosting
  * Rashmi - 2/11/07 - preserve license terms
  * Mallika - 5/2/07 - Added checks to see if resources already exist in uploads collection
  * Mallika - 5/3/07 - Fix for entire url
  * Rashmi - 5/7/07 - revised check for adding resources in uploads
  * Mallika - 5/14/07 - Reordered code so we only read from unzipped dirs if item doesnt' exist in resources and meletedocs
  * Mallika - 5/15/07 - Rearranged code for import from site
  * Mallika - 6/7/07 - Added null check condition for copyIntoFolder
  * Mallika - 6/11/07 - Moved null check to replace method
  * Mallika - 6/22/07 - Fix for ME-433
  * Mallika - 7/24/07 - Added embed tag processing
  */
 public class MeleteImportServiceImpl implements MeleteImportService{
 	/*******************************************************************************
 	* Dependencies
 	*******************************************************************************/
 	/** Dependency:  The logging service. */
 	protected Log logger = LogFactory.getLog(MeleteImportServiceImpl.class);
 
 	protected SectionDB sectionDB;
 	protected ModuleDB moduleDB;
 	protected ModuleShdates moduleShdates;
 	private MeleteCHService meleteCHService;
 	private MeleteLicenseDB meleteLicenseDB;
 	private MeleteUserPreferenceDB meleteUserPrefDB;
 
 	protected String unzippeddirpath = null;
 	private SubSectionUtilImpl sectionUtil;
 	private Document seqDocument;
 
 	/**default namespace and metadata namespace*/
 	protected String DEFAULT_NAMESPACE_URI = "http://www.imsglobal.org/xsd/imscp_v1p1";
 	protected String IMSMD_NAMESPACE_URI ="http://www.imsglobal.org/xsd/imsmd_v1p2";
 
 	protected int RESOURCE_LICENSE_CODE = 0; //not determined yet
 	protected String RESOURCE_LICENSE_URL = "I have not determined copyright yet"; //No license
 	protected int RESOURCE_LICENSE_COPYRIGHT_CODE = 1; //Copyright of author
 	protected int RESOURCE_LICENSE_PD_CODE = 2; //		Public Domain
 	protected int RESOURCE_LICENSE_CC_CODE = 3; //Creative Commons
 	protected int RESOURCE_LICENSE_FAIRUSE_CODE = 4; //FairUse Exception
 
 	private String destinationContext;
 	protected MeleteUtil meleteUtil = new MeleteUtil();
 
 	public void setLogger(Log logger){
 		this.logger = logger;
 	}
 	/**
 	 * Final initialization, once all dependencies are set.
 	 */
 	public void init(){
 		logger.debug(this +".init()");
 	}
 
 	/**
 	 * Final cleanup.
 	 */
 	public void destroy(){
 		logger.debug(this +".destroy()");
 	}
 
 	private void buildModuleTitle(Element titleEle, Module module)
 	{
 		boolean moduleTitleFlag = false;
 		if (titleEle != null)
 		{			
 			String title = titleEle.getTextTrim();
 			if (title != null && title.length() != 0)
 			{
 				module.setTitle(title);
 				moduleTitleFlag = true;
 			}
 		}		
 		if(!moduleTitleFlag) module.setTitle("Untitled Module");
 		return;
 	}
 	
 	private boolean buildModuleDescription(Element descEle, Module module)
 	{
 		boolean descr = false;
 		if (descEle != null && descEle.element("imsmd:langstring") != null)
 		{
 			String desc = descEle.element("imsmd:langstring").getText();
 			module.setDescription(desc.trim());
 			descr = true;
 		}
 		return descr;
 	}
 
 	private boolean buildModuleKeyword(Element keywordEle, Module module)
 	{
 		boolean keywords = false;
 		
 		if (keywordEle != null && keywordEle.element("imsmd:langstring") != null)
 		{
 			String modkeyword = keywordEle.element("imsmd:langstring").getText();
 			module.setKeywords(modkeyword.trim());
 			keywords = true;
 		}
 		return keywords;
 	}
 	
 
 	private void removeNamespaces(Element elem)
 	{
 		elem.setQName(QName.get(elem.getName(), Namespace.NO_NAMESPACE, elem.getQualifiedName()));
 		Node n = null;
 		for (int i = 0; i < elem.content().size(); i++)
 		{
 			n = (Node) elem.content().get(i);
 			if (n.getNodeType() == Node.ATTRIBUTE_NODE) ((Attribute) n).setNamespace(Namespace.NO_NAMESPACE);
 			if (n.getNodeType() == Node.ELEMENT_NODE) removeNamespaces((Element) n);
 		}
 	}
 	 
 	public int mergeAndBuildModules(Document ArchiveDoc, String unZippedDirPath, String fromSiteId) throws Exception
 	{
 		if (logger.isDebugEnabled()) logger.debug("Entering mergeAndBuildModules");
 		setUnzippeddirpath(unZippedDirPath);
 		setDestinationContext(fromSiteId);
 		int count = 0; 
 		try
 		{
 			Element rootEle = ArchiveDoc.getRootElement();
 		
 			Map uris = new HashMap();
 			uris.put("imscp", DEFAULT_NAMESPACE_URI);
 			uris.put("imsmd", IMSMD_NAMESPACE_URI);
 
 			// organizations
 			List elements = rootEle.selectNodes("//organization/item");
 			logger.debug("sz of elements is" + elements.size());
 			count = elements.size();
 			for (Iterator iter = elements.iterator(); iter.hasNext();)
 			{
 				Element element = (Element) iter.next();
 			
 				//build module
 				Module module = new Module();
 				boolean keywords = false;
 				boolean descr = false;
 				for (Iterator iter1 = element.elementIterator(); iter1.hasNext();)
 				{
 					Element childele = (Element) iter1.next();
 			
 					if (childele.getName().equals("title")) buildModuleTitle(childele, module);
 					if (childele.getName().equals("imsmd:lom"))
 					{
 						List<Element> modulegeneralList = childele.elements();
 						List moduleMetadataList = modulegeneralList.get(0).elements();
 						
 						for (Iterator iter2 = moduleMetadataList.iterator(); iter2.hasNext();)
 						{
 							Element metaElement = (Element) iter2.next();
 			
 							if (metaElement.getName().equals("imsmd:description")) descr = buildModuleDescription(metaElement, module);
 							if (!descr) module.setDescription("    ");
 							if (metaElement.getName().equals("imsmd:keyword")) keywords = buildModuleKeyword(metaElement, module);
 							if (!keywords) module.setKeywords(module.getTitle());
 						}
 					}
 					
 				}
 				createModule(module);
 			// build sections
 
 				sectionUtil = new SubSectionUtilImpl();
 		//		String moduleDtdLocation = new String(meleteUtil.readFromFile(new File(getUnzippeddirpath() + File.separator + "moduleSeqdtdLocation.txt")));
 		//		sectionUtil.setDtdLocation(moduleDtdLocation);
 				seqDocument = sectionUtil.createSubSection4jDOM();
 
 				for (Iterator iter3 = element.elementIterator("item"); iter3.hasNext();)
 				{
 					Element itemelement = (Element) iter3.next();
 
 					if (itemelement.attributeValue("identifier").startsWith("NEXTSTEPS"))
 						mergeWhatsNext(itemelement, ArchiveDoc, module);
 					else mergeSection(itemelement, ArchiveDoc, module, addBlankSection(null));
 				}
 
 				// update module seqXml
 				logger.debug("checking seqXML now at the end of buildModule process" + seqDocument.asXML());
 				module.setSeqXml(seqDocument.asXML());
 				moduleDB.updateModule(module);
 				
 			}
 		}
 		catch (Exception e)
 		{
 			// no organization tag so create one flat module
 			e.printStackTrace();
 		}
 
 		if (logger.isDebugEnabled()) logger.debug("Exiting mergeAndBuildModules");
 		return count;
 	}
 	/**
 	 * Parses the manifest and build modules
 	 *
 	 * @param document document
 	 * @param unZippedDirPath unZipped fiels Directory Path
 	 * @exception throws exception
 	 */
 	public void parseAndBuildModules(Document document, String unZippedDirPath) throws Exception
 	{
 		if (logger.isDebugEnabled()) logger.debug("Entering parseAndBuildModules");
 		setUnzippeddirpath(unZippedDirPath);
 		setDestinationContext(ToolManager.getCurrentPlacement().getContext());
 		Map uris = new HashMap();
 		uris.put("imscp", DEFAULT_NAMESPACE_URI);
 		uris.put("imsmd", IMSMD_NAMESPACE_URI);
 
 		try
 		{
 			// organizations
 			XPath xpath = document.createXPath("/imscp:manifest/imscp:organizations/imscp:organization");
 			xpath.setNamespaceURIs(uris);
 
 			Element eleOrg = (Element) xpath.selectSingleNode(document);
 
 			// build module
 			// loop thru organization elements - item elements
 			List elements = eleOrg.elements();
 			for (Iterator iter = elements.iterator(); iter.hasNext();)
 			{
 				Element element = (Element) iter.next();
 				buildModule(element, document);
 			}
 		}
 		catch (Exception e)
 		{
 			// no organization tag so create one flat module
 			buildFlatModule(document);
 		}
 
 		if (logger.isDebugEnabled()) logger.debug("Exiting parseAndBuildModules");
 	}
 
 	/*
 	 * Builds one big module and each resource element becomes a section
 	 */
 	  private void buildFlatModule(Document document) throws Exception
 	  {
 		  if (logger.isDebugEnabled())
 				logger.debug("Entering buildFlatModule..." );
 
 //			create module object
 			Module module = new Module();
 			module.setTitle("Untitled Module");
 			module.setKeywords("Untitled Module");
 			module.setDescription("    ");
 			createModule(module);
 
 			// read all resources tag and create section
 			Map uris = new HashMap();
 			uris.put("imscp", DEFAULT_NAMESPACE_URI);
 			uris.put("imsmd", IMSMD_NAMESPACE_URI);
 
 			// resources
 			XPath xpath = document.createXPath("/imscp:manifest/imscp:resources");
 			xpath.setNamespaceURIs(uris);
 
 			Element eleAllResources = (Element) xpath.selectSingleNode(document);
 
 			sectionUtil = new SubSectionUtilImpl();
 			seqDocument = sectionUtil.createSubSection4jDOM();
 
 			// build section
 			// loop thru resources elements - resource elements
 			List elements = eleAllResources.elements();
 			for (Iterator iter = elements.iterator(); iter.hasNext();)
 			{
 				Element eleRes = (Element) iter.next();
 				Section section = buildDefaultSection(module,addBlankSection(null));
 
 				MeleteResource meleteResource= new MeleteResource();
 				//default to no license
 				meleteResource.setLicenseCode(RESOURCE_LICENSE_CODE);
 				meleteResource.setCcLicenseUrl(RESOURCE_LICENSE_URL);
 
 				Attribute resHrefAttr = eleRes.attribute("href");
 
 				if (resHrefAttr != null)
 				{
 					String hrefVal = resHrefAttr.getValue();
 
 					// check if file is missing
 					if (hrefVal != null && hrefVal.length() != 0
 							&& !(hrefVal.startsWith("http://") || hrefVal.startsWith("https://") || hrefVal.startsWith("mailto:")))
 					{
 						if (!meleteUtil.checkFileExists(getUnzippeddirpath() + File.separator + hrefVal))
 						{
 							logger.info("content file for section is missing so move ON");
 							return;
 						}
 					}
 					// end missing file check
 
 					List resElements = eleRes.elements();
 					createContentResource(module, section, meleteResource, hrefVal, resElements);
 
 				} // resHrefAttr check end
 			}
 			// update module seqXml
 			logger.debug("checking seqXML now at the end of buildModule process" + seqDocument.asXML());
 			module.setSeqXml(seqDocument.asXML());
 			moduleDB.updateModule(module);
 	  }
 
 	  /*
 	   * build default section without reading ims item element
 	   */
 	  private Section buildDefaultSection(Module module, Element sectionElement) throws Exception
 	  {
 		  	String userId = UserDirectoryService.getCurrentUser().getEid();
 			String firstName = UserDirectoryService.getCurrentUser().getFirstName();
 			String lastName = UserDirectoryService.getCurrentUser().getLastName();
 
 			Section section = new Section();
 			section.setTextualContent(true);
 			section.setCreatedByFname(firstName);
 			section.setCreatedByLname(lastName);
 			section.setContentType("notype");
 			section.setTitle("Untitled Section");
 
 			// save section object
 			Integer new_section_id = sectionDB.addSection(module, section, true);
 			section.setSectionId(new_section_id);
 			sectionElement.addAttribute("id", new_section_id.toString());
 
 			return section;
 	  }
 
 	/**
 	 * Builds the module for each Item element under organization
 	 *
 	 * @param eleItem item element
 	 * @exception throws exception
 	 * revised by rashmi - change the whole structure of accessing elements
 	 */
 	private void buildModule(Element eleItem, Document document)
 	throws Exception {
 
 		if (logger.isDebugEnabled())
 			logger.debug("Entering buildModule..." );
 
 //		create module object
 		Module module = new Module();
 		boolean moduleTitleFlag = false;
 		if (eleItem.elements("title") != null && eleItem.elements("title").size() != 0)
 		{
 			Element titleEle = (Element) eleItem.elements("title").get(0);
 			if (titleEle != null)
 			{
 				String title = titleEle.getTextTrim();
 				if (title != null && title.length() != 0)
 				{
 					module.setTitle(title);
 					moduleTitleFlag = true;
 				}
 			}
 		}
 		if(!moduleTitleFlag) module.setTitle("Untitled Module");
 
 		boolean keywords = false;
 		boolean descr = false;
 		if (eleItem.selectNodes("./imsmd:lom/imsmd:general") != null && eleItem.selectNodes("./imsmd:lom/imsmd:general").size() != 0)
 		{
 			Element generalElement = (Element) eleItem.selectNodes("./imsmd:lom/imsmd:general").get(0);
 			List moduleMetadataList = generalElement.elements();
 			for (Iterator iter = moduleMetadataList.iterator(); iter.hasNext();)
 			{
 				Element metaElement = (Element) iter.next();
 
 				if (metaElement.getName().equals("description"))
 				{
 					String desc = metaElement.selectSingleNode(".//imsmd:langstring").getText();
 					module.setDescription(desc.trim());
 					descr = true;
 				}
 
 				if (metaElement.getName().equals("keyword"))
 				{
 					String modkeyword = metaElement.selectSingleNode(".//imsmd:langstring").getText();
 					if (modkeyword != null)
 					{
 						module.setKeywords(modkeyword.trim());
 						keywords = true;
 					}
 				}
 			}
 		}
 		if (!keywords) module.setKeywords(module.getTitle());
 		if (!descr) module.setDescription("    ");
 		createModule(module);
 
 // 		build sections
 		try
 		{
 			sectionUtil = new SubSectionUtilImpl();
 			seqDocument = sectionUtil.createSubSection4jDOM();
 
 			for (Iterator iter = eleItem.elementIterator("item"); iter.hasNext();)
 			{
 				Element element = (Element) iter.next();
 
 				if (element.attributeValue("identifier").startsWith("NEXTSTEPS"))
 					buildWhatsNext(element, document, module);
 				else buildSection(element, document, module, addBlankSection(null));
 			}
 
 			// update module seqXml
 			logger.debug("checking seqXML now at the end of buildModule process" + seqDocument.asXML());
 			module.setSeqXml(seqDocument.asXML());
 			moduleDB.updateModule(module);
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			throw e;
 		}
 		if (logger.isDebugEnabled()) logger.debug("Exiting buildModule...");
 	}
 
 	private Element addBlankSection(Element parentElement)
 	{
 		if(parentElement == null)
 			parentElement = seqDocument.getRootElement();
 		Element newSectionElement = parentElement.addElement("section");
 		return newSectionElement;
 	}
 	/*
 	 * build license information
 	 * add by rashmi
 	 */
 
 	private void buildLicenseInformation(MeleteResource meleteResource,String licenseUrl)
 	{
 		int lcode = RESOURCE_LICENSE_CODE ;
 
 		if(licenseUrl.startsWith("Copyright (c)"))
 		{
 			 lcode = RESOURCE_LICENSE_COPYRIGHT_CODE;
 			 // remove copyright(c) phrase
 			 String owner = licenseUrl.substring(13);
 			 int delimIdx = owner.lastIndexOf(",");
 			 if(delimIdx !=-1){
 			 meleteResource.setCopyrightOwner(owner.substring(0,delimIdx));
 			 meleteResource.setCopyrightYear(owner.substring(delimIdx +1));
 			 }
 
 		}else if(licenseUrl.startsWith("Public Domain"))
 		{
 			lcode = RESOURCE_LICENSE_PD_CODE;
 			int nameIdx = licenseUrl.indexOf(",");
 			String licensename = licenseUrl.trim();
 			if(nameIdx != -1)
 			{
 				 licensename = licenseUrl.substring(0,nameIdx) ;
 				 String otherInfo = licenseUrl.substring(nameIdx +1);
 				 int ownerIdx = otherInfo.lastIndexOf(",");
 				 if(ownerIdx != -1)
 				 {
 				 meleteResource.setCopyrightOwner(otherInfo.substring(0,ownerIdx));
 				 meleteResource.setCopyrightYear(otherInfo.substring(ownerIdx+1));
 				 }
 			}
 			CcLicense ccl = meleteLicenseDB.fetchCcLicenseUrl(licensename);
 			licenseUrl = ccl.getUrl();
 
 		}else if(licenseUrl.startsWith("Creative Commons"))
 		{
 			lcode = RESOURCE_LICENSE_CC_CODE;
 			//remove "creative commons" phrase from the name
 			licenseUrl = licenseUrl.substring(17);
 			int nameIdx = licenseUrl.indexOf(",");
 			String licensename = licenseUrl.trim();
 			if(nameIdx != -1)
 			{
 				 licensename = licenseUrl.substring(0,nameIdx) ;
 				 String otherInfo = licenseUrl.substring(nameIdx +1);
 				 int ownerIdx = otherInfo.lastIndexOf(",");
 				 meleteResource.setCopyrightOwner(otherInfo.substring(0,ownerIdx));
 				 meleteResource.setCopyrightYear(otherInfo.substring(ownerIdx+1));
 			}
 			CcLicense ccl = meleteLicenseDB.fetchCcLicenseUrl(licensename);
 			licenseUrl = ccl.getUrl();
 			meleteResource.setReqAttr(true);
 			meleteResource.setAllowCmrcl(ccl.isAllowCmrcl());
 			meleteResource.setAllowMod(ccl.getAllowMod());
 
 		}else if(licenseUrl.startsWith("Copyrighted Material"))
 		{
 			lcode = RESOURCE_LICENSE_FAIRUSE_CODE;
 			int nameIdx = licenseUrl.indexOf(",");
 			String licensename = licenseUrl.trim();
 			if(nameIdx != -1)
 			{
 				 licensename = licenseUrl.substring(0,nameIdx) ;
 				 String otherInfo = licenseUrl.substring(nameIdx +1);
 				 int ownerIdx = otherInfo.lastIndexOf(",");
 				 meleteResource.setCopyrightOwner(otherInfo.substring(0,ownerIdx));
 				 meleteResource.setCopyrightYear(otherInfo.substring(ownerIdx+1));
 			}
 			licenseUrl = licensename;
 		}
 
 		meleteResource.setLicenseCode(lcode);
 		meleteResource.setCcLicenseUrl(licenseUrl);
 	}
 
 	/*
 	 * build whats next
 	 * added by rashmi
 	 */
 	private void buildWhatsNext(Element eleItem,Document  document,Module module) throws Exception
 	{
 		Attribute identifierref = eleItem.attribute("identifierref");
 		Element eleRes;
 
 		if (identifierref != null) {
 			eleRes = getResource(identifierref.getValue(), document);
 			String hrefVal = eleRes.attributeValue("href");
 			String nextsteps = new String(meleteUtil.readFromFile(new File(getUnzippeddirpath() + File.separator+ hrefVal)));
 			module.setWhatsNext(nextsteps);
 			ModuleDateBean mdbean = new ModuleDateBean();
 			mdbean.setModuleId(module.getModuleId().intValue());
 			mdbean.setModule(module);
 			mdbean.setModuleShdate(module.getModuleshdate());
 			ArrayList mdbeanList = new ArrayList();
 			mdbeanList.add(mdbean);
 			moduleDB.updateModuleDateBeans(mdbeanList);
 		}
 
 	}
 
 	private void mergeWhatsNext(Element eleItem,Document  document,Module module) throws Exception
 	{
 		Attribute identifierref = eleItem.attribute("identifierref");
 		Element eleRes;
 
 		if (identifierref != null) {
 			eleRes = getMergeResource(identifierref.getValue(), document);
 			String hrefVal = eleRes.attributeValue("href");
 			String nextsteps = new String(meleteUtil.readFromFile(new File(getUnzippeddirpath() + File.separator+ hrefVal)));
 			module.setWhatsNext(nextsteps);
 			moduleDB.updateModule(module);
 		}
 
 	}
 	/**
 	 * creates the module
 	 * @param module Module
 	 */
 	private void createModule(Module module)throws Exception {
 		if (logger.isDebugEnabled())
 			logger.debug("Entering createModule...");
 
 		//String courseId = PortalService.getCurrentSiteId();
 		String courseId ="";
         courseId =destinationContext;
 
 		String userId = UserDirectoryService.getCurrentUser().getId();
 		String firstName = UserDirectoryService.getCurrentUser()
 				.getFirstName();
 		String lastName = UserDirectoryService.getCurrentUser()
 				.getLastName();
 
 		module.setUserId(userId);
 		module.setCreatedByFname(firstName);
 		module.setCreatedByLname(lastName);
 		module.setModuleshdate(getModuleShdates());
 		moduleDB.addModule(module, getModuleShdates(), userId, courseId);
 		if (logger.isDebugEnabled())
 			logger.debug("Exiting createModule...");
 	}
 
 	/**
 	 * Builds section for each item under module item
 	 * @param eleItem item element
 	 * @param document document
 	 * @param module Module
 	 * @throws Exception
 	 */
 	private void buildSection(Element eleItem, Document document, Module module, Element seqElement)
 			throws Exception {
 		if (logger.isDebugEnabled())
 			logger.debug("Entering buildSection...");
 
 		Attribute identifier = eleItem.attribute("identifier");
 		logger.debug("importing ITEM " + identifier.getValue());
 
 		Attribute identifierref = eleItem.attribute("identifierref");
 		Element eleRes;
 
 		Section section = new Section();
 		MeleteResource meleteResource = new MeleteResource();
 		boolean sectionTitleFlag = false;
 		boolean sectionCopyrightFlag= false;
 
 		List elements = eleItem.elements();
 		for (Iterator iter = elements.iterator(); iter.hasNext();) {
 			Element element = (Element) iter.next();
 
 			//title
 			if (element.getQualifiedName().equalsIgnoreCase("title")) {
 				section.setTitle(element.getTextTrim());
 				sectionTitleFlag = true;
 			}
 			//item
 			else if (element.getQualifiedName().equalsIgnoreCase("item")) {
 				//call recursive here
 				buildSection(element,document, module, addBlankSection(seqElement));
 			}
 			//metadata
 			else if (element.getQualifiedName().equalsIgnoreCase("imsmd:lom")){
 				// section instructions
 				Element DescElement = null;
 				if(eleItem.selectNodes("./imsmd:lom/imsmd:general/imsmd:description") != null && (eleItem.selectNodes("./imsmd:lom/imsmd:general/imsmd:description").size() != 0))
 					DescElement = (Element)eleItem.selectNodes("./imsmd:lom/imsmd:general/imsmd:description").get(0);
 
 					if(DescElement != null)
 					{
 					String instr = DescElement.selectSingleNode( ".//imsmd:langstring").getText();
 					section.setInstr(instr.trim());
 					}
 
 					//				 read license information
 				Element rightsElement = null;
 				if(eleItem.selectNodes("./imsmd:lom/imsmd:rights") != null && (eleItem.selectNodes("./imsmd:lom/imsmd:rights").size() != 0))
 		            rightsElement = (Element)eleItem.selectNodes("./imsmd:lom/imsmd:rights").get(0);
 
 					if(rightsElement != null)
 					{
 					Element licenseElement = rightsElement.element("description");
 					String licenseUrl = licenseElement.selectSingleNode( ".//imsmd:langstring").getText();
 					if(licenseUrl != null)
 						buildLicenseInformation(meleteResource,licenseUrl);
 						sectionCopyrightFlag = true;
 					}
 				}
 			// license end
 		}
 		// other attributes
 		logger.debug("setting section attribs");
 		String userId = UserDirectoryService.getCurrentUser().getEid();
 		String firstName = UserDirectoryService.getCurrentUser().getFirstName();
 		String lastName = UserDirectoryService.getCurrentUser().getLastName();
 
 		section.setTextualContent(true);
 		section.setCreatedByFname(firstName);
 		section.setCreatedByLname(lastName);
 		section.setContentType("notype");
 
 		if(!sectionTitleFlag)section.setTitle("Untitled Section");
 
 		//default to no license
 		if(!sectionCopyrightFlag)
 		{
 			meleteResource.setLicenseCode(RESOURCE_LICENSE_CODE);
 			meleteResource.setCcLicenseUrl(RESOURCE_LICENSE_URL);
 		}
 		// save section object
 		Integer new_section_id = sectionDB.addSection(module, section, true);
 		section.setSectionId(new_section_id);
 		seqElement.addAttribute("id", new_section_id.toString());
 
 		// now melete resource object
 		if (identifierref != null)
 		{
 			eleRes = getResource(identifierref.getValue(), document);
 			if (eleRes != null)
 			{
 				Attribute resHrefAttr = eleRes.attribute("href");
 
 				if (resHrefAttr != null)
 				{
 					String hrefVal = resHrefAttr.getValue();
 
 					// check if file is missing
 					if (hrefVal != null && hrefVal.length() != 0
 							&& !(hrefVal.startsWith("http://") || hrefVal.startsWith("https://") || hrefVal.startsWith("mailto:")))
 					{
 						if (!meleteUtil.checkFileExists(getUnzippeddirpath() + File.separator + hrefVal))
 						{
 							logger.info("content file for section is missing so move ON");
 							return;
 						}
 					}
 					// end missing file check
 
 					// create meleteResourceObject
 					List resElements = eleRes.elements();
 					createContentResource(module, section, meleteResource, hrefVal, resElements);
 
 				} // resHrefAttr check end
 			}
 		}
 
 		if (logger.isDebugEnabled()) logger.debug("Exiting buildSection...");
 	}
 
 	/**
 	 * Builds section for each item under module item
 	 * @param eleItem item element
 	 * @param document document
 	 * @param module Module
 	 * @throws Exception
 	 */
 	private void mergeSection(Element eleItem, Document document, Module module, Element seqElement)
 			throws Exception {
 		if (logger.isDebugEnabled()) logger.debug("Entering buildSection...");
 
 		Attribute identifier = eleItem.attribute("identifier");
 		logger.debug("importing ITEM " + identifier.getValue());
 
 		Attribute identifierref = eleItem.attribute("identifierref");
 		Element eleRes;
 
 		Section section = new Section();
 		MeleteResource meleteResource = new MeleteResource();
 		boolean sectionTitleFlag = false;
 		boolean sectionCopyrightFlag = false;
 
 		List elements = eleItem.elements();
 		for (Iterator iter = elements.iterator(); iter.hasNext();)
 		{
 			Element element = (Element) iter.next();
 
 			// title
 			if (element.getQualifiedName().equalsIgnoreCase("title"))
 			{
 				section.setTitle(element.getTextTrim());
 				sectionTitleFlag = true;
 			}
 			// item
 			else if (element.getQualifiedName().equalsIgnoreCase("item"))
 			{
 				// call recursive here
 				buildSection(element, document, module, addBlankSection(seqElement));
 			}
 			// metadata
 			else if (element.getName().equalsIgnoreCase("imsmd:lom"))
 			{
 				// section instructions
 				List<Element> modulegeneralList = element.elements();
 				List moduleMetadataList = modulegeneralList.get(0).elements();
 
 				for (Iterator iter2 = moduleMetadataList.iterator(); iter2.hasNext();)
 				{
 					Element metaElement = (Element) iter2.next();
 	
 					if (metaElement.getName().equals("imsmd:description") && metaElement.element("imsmd:langstring") != null)
 					{
 						String instr = metaElement.element("imsmd:langstring").getText();
 						section.setInstr(instr.trim());
 					}
 				}
 
 				// read license information
 				if(modulegeneralList.size() > 1)
 				{
 				List rightList = modulegeneralList.get(1).elements();
 				for (Iterator iter3 = rightList.iterator(); iter3.hasNext();)
 				{
 					Element rightsElement = (Element) iter3.next();
 	
 					if (rightsElement.getName().equals("imsmd:description") && rightsElement.element("imsmd:langstring") !=null )
 					{
 						String licenseUrl = rightsElement.element("imsmd:langstring").getText();
 						if (licenseUrl != null)
 						{
 							buildLicenseInformation(meleteResource, licenseUrl);
 							sectionCopyrightFlag = true;
 						}
 					}
 				 }
 				}
 			 }
 			// license end
 		}
 		// other attributes
 		logger.debug("setting section attribs");
 		String userId = UserDirectoryService.getCurrentUser().getEid();
 		String firstName = UserDirectoryService.getCurrentUser().getFirstName();
 		String lastName = UserDirectoryService.getCurrentUser().getLastName();
 
 		section.setTextualContent(true);
 		section.setCreatedByFname(firstName);
 		section.setCreatedByLname(lastName);
 		section.setContentType("notype");
 
 		if (!sectionTitleFlag) section.setTitle("Untitled Section");
 
 		// default to no license
 		if (!sectionCopyrightFlag)
 		{
 			meleteResource.setLicenseCode(RESOURCE_LICENSE_CODE);
 			meleteResource.setCcLicenseUrl(RESOURCE_LICENSE_URL);
 		}
 		// save section object
 		Integer new_section_id = sectionDB.addSection(module, section, true);
 		section.setSectionId(new_section_id);
 		seqElement.addAttribute("id", new_section_id.toString());
 
 		// now melete resource object
 		if (identifierref != null)
 		{
 			eleRes = getMergeResource(identifierref.getValue(), document);
 			if (eleRes != null)
 			{
 				Attribute resHrefAttr = eleRes.attribute("href");
 
 				if (resHrefAttr != null)
 				{
 					String hrefVal = resHrefAttr.getValue();
 
 					// check if file is missing
 					if (hrefVal != null && hrefVal.length() != 0
 							&& !(hrefVal.startsWith("http://") || hrefVal.startsWith("https://") || hrefVal.startsWith("mailto:")))
 					{
 						if (!meleteUtil.checkFileExists(getUnzippeddirpath() + File.separator + hrefVal))
 						{
 							logger.info("content file for section is missing so move ON");
 							return;
 						}
 					}
 					// end missing file check
 
 					// create meleteResourceObject
 					List resElements = eleRes.elements();
 					createContentResource(module, section, meleteResource, hrefVal, resElements);
 
 				} // resHrefAttr check end
 			}
 		}
 
 		if (logger.isDebugEnabled()) logger.debug("Exiting mergeSection...");
 	}
 	
 	
 	
 	/**
 	 * creates section dependent file
 	 *
 	 * @param hrefVal
 	 *        href value of the item
 	 */
 	private String uploadSectionDependentFile(String hrefVal, String courseId, boolean imsImport) {
 		try {
 			String filename = null;
 			String res_mime_type = null;
 			byte[] melContentData = null;
 
 		 if (hrefVal.lastIndexOf('/') != -1)
 				filename = hrefVal.substring( hrefVal.lastIndexOf('/') + 1);
 
 		 if (filename != null && filename.trim().length() > 0){
 
 			 try{
 			 		String checkResourceId = Entity.SEPARATOR + "private" + Entity.SEPARATOR + "meleteDocs" +Entity.SEPARATOR+courseId+Entity.SEPARATOR+"uploads"+Entity.SEPARATOR+filename;
 			 		getMeleteCHService().checkResource(checkResourceId);
 // 			 	found it so return it
 			 		return getMeleteCHService().getResourceUrl(checkResourceId);
 			 	}catch (IdUnusedException ex)
 				{
 			 		String uploadCollId = "";
 			 		//find mime type and get name and contents
 			 		if (imsImport)
 			 		{
 				 		  //This is executed by IMP import
 				 		  melContentData = meleteUtil.readFromFile(new File(getUnzippeddirpath() + File.separator
 								+ hrefVal));
 				 		 uploadCollId = getMeleteCHService().getUploadCollectionId(destinationContext);
 			 		}
 			 		else
 			 		{
 			 			//This is executed by import from site
 			 			logger.debug("reading resource properties in import from site");
 			 			ContentResource cr = getMeleteCHService().getResource(hrefVal);
 						melContentData = cr.getContent();
 						uploadCollId = getMeleteCHService().getUploadCollectionId();
 			 		}
 
 			 		res_mime_type = filename.substring(filename.lastIndexOf(".")+1);
 					res_mime_type = ContentTypeImageService.getContentType(res_mime_type);
 
 			 		 ResourcePropertiesEdit res = getMeleteCHService().fillEmbeddedImagesResourceProperties(filename);
 			 		 String newResourceId = getMeleteCHService().addResourceItem(filename, res_mime_type,uploadCollId,melContentData,res);
 			 		 // create melete resource object
 					  MeleteResource meleteResource = new MeleteResource();
 		         	 meleteResource.setResourceId(newResourceId);
 		         	 //set default license info to "I have not determined copyright yet" option
 		         	 meleteResource.setLicenseCode(0);
 		         	 sectionDB.insertResource(meleteResource);
 		         	return getMeleteCHService().getResourceUrl(newResourceId);
 				}
 				catch(Exception e)
 				{
 					logger.error(e.toString());
 				}
 		 }
 		} catch (Exception e) {
 			if (logger.isErrorEnabled())
 				logger.error("ExportMeleteModules : uploadSectionDependentFile() :"+ e.toString());
 		}
 		return "";
 	}
 
 	/**
 	 * creates the section
 	 * @param module Module
 	 * @param section Section
 	 * @param hrefVal href value of the item
 	 * @return @throws
 	 *         MalformedURLException
 	 * @throws UnknownHostException
 	 * @throws MeleteException
 	 * @throws Exception
 	 */
 	public void createContentResource(Module module,Section section,MeleteResource meleteResource, String hrefVal,List resElements)
 	throws MalformedURLException, UnknownHostException, MeleteException, Exception {
 		String melResourceName = null;
 		String melResourceDescription = null;
 		String res_mime_type = null;
 		byte[] melContentData = null;
 		boolean encodingFlag = false;
 		String newResourceId = "";
 		String fromCRName = null;
 
 		if (logger.isDebugEnabled())
 			logger.debug("Entering createSection...");
 
 		String courseId ="";
        	courseId = destinationContext;
 
 
 		//This code fixes resource description transfer for import from site
 		if (resElements == null)
 		{
 			 ContentResource cr = getMeleteCHService().getResource(hrefVal);
 		     if (cr == null) return;
 		     melResourceDescription = cr.getProperties().getProperty(ResourceProperties.PROP_DESCRIPTION);
 		     if (section.getContentType().equals("typeLink"))
 		     {
 		    	 hrefVal = new String(cr.getContent());
 		    	 fromCRName = cr.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME);
 		     }
 
 		}//End code
 
 		//html file
 		if (!(hrefVal.startsWith("http://") || hrefVal.startsWith("https://") || hrefVal.startsWith("mailto:"))&&
 				(hrefVal.lastIndexOf('.') != -1	&& (hrefVal.substring(hrefVal.lastIndexOf('.') + 1).equalsIgnoreCase("html")
 				|| hrefVal.substring(hrefVal.lastIndexOf('.') + 1).equalsIgnoreCase("htm")))) {
 			//This is for typeEditor sections
 			section.setContentType("typeEditor");
 			res_mime_type= getMeleteCHService().MIME_TYPE_EDITOR;
 			String contentEditor = null;
 
 			String addCollId = "";
 			if (resElements != null)
 			{
                 //This part called by IMS import
 				contentEditor = new String(meleteUtil.readFromFile(new File(getUnzippeddirpath() + File.separator + hrefVal)));
 //				 create objects for embedded images
 				contentEditor = createContentFile(contentEditor, (Module)module, (Section)section, resElements);
 				addCollId = getMeleteCHService().getCollectionId(section.getContentType(), module.getModuleId());
 			}
 			else
 			{
 				//This part called by import from site
 				  ContentResource cr = getMeleteCHService().getResource(hrefVal);
 				  contentEditor = new String(cr.getContent());
 				  contentEditor = createContentFile(contentEditor, (Module)module, (Section)section, null);
 				  addCollId = getMeleteCHService().getCollectionId(destinationContext, section.getContentType(), module.getModuleId());
 			}
 
              melContentData = new byte[contentEditor.length()];
              melContentData = contentEditor.getBytes();
              encodingFlag = true;
              melResourceName = "Section_" + section.getSectionId().toString();
              melResourceDescription="compose content";
              // no need to perform check just add it for section_xxx.html files
 
              ResourcePropertiesEdit res = getMeleteCHService().fillInSectionResourceProperties(encodingFlag,melResourceName,melResourceDescription);
              newResourceId = getMeleteCHService().addResourceItem(melResourceName, res_mime_type,addCollId,melContentData,res );
  			 meleteResource.setResourceId(newResourceId);
  			 sectionDB.insertMeleteResource((Section)section, (MeleteResource)meleteResource);
  			 return;
 		}
 
 			//This part is executed by both IMS import and import from site
 
 			if (hrefVal.startsWith("http://") || hrefVal.startsWith("https://") || hrefVal.startsWith("mailto:")) {
 //				link
 				section.setContentType("typeLink");
				section.setOpenWindow(true);
 				// get url title if provided in IMS
 				String urlTitle = "";
 				if(resElements != null){
 					for(int i=0; i < resElements.size(); i++)
 					{
 					Element urlTitleElement = (Element)resElements.get(i);
 					if(urlTitleElement.getQualifiedName().equalsIgnoreCase("imsmd:title")){
 						urlTitle = urlTitleElement.selectSingleNode( ".//imsmd:langstring").getText();
 						break;
 						}
 					}
 				} else {
 					// Import from Site
 					if(!hrefVal.equals(fromCRName))
 						urlTitle = fromCRName;
 				}
 
 				// make last part of link as title
 				if(urlTitle.equals(""))
 				{
 					urlTitle = hrefVal.substring(hrefVal.lastIndexOf("/")+1);
 					if(urlTitle == null || urlTitle.length() == 0)
 					{
 						urlTitle = hrefVal.substring(0,hrefVal.lastIndexOf("/"));
 						urlTitle = urlTitle.substring(urlTitle.lastIndexOf("/")+1);
 					}
 
 				}
 				melResourceName = urlTitle;
 			}
            	else
            		{
 					// uploaded file
 					section.setContentType("typeUpload");
 					melResourceName = hrefVal.substring(hrefVal.lastIndexOf("/") + 1);
 
 				}
 
 			// read resource description
 			if (resElements != null)
 			{
 				for (int i = 0; i < resElements.size(); i++)
 				{
 					Element resDescElement = (Element) resElements.get(i);
 					if (resDescElement.getQualifiedName().equalsIgnoreCase("imsmd:description"))
 					{
 						melResourceDescription = resDescElement.selectSingleNode(".//imsmd:langstring").getText();
 						break;
 					}
 				}
 			}
 			 // Everything here is going to uploads collection
 			  try{
 //			  check if the item has already been imported to this site (in uploads collection)
 		 		String checkResourceId = "/private/meleteDocs/"+courseId+"/uploads/"+melResourceName;
 		 		getMeleteCHService().checkResource(checkResourceId);
 		 		meleteResource.setResourceId(checkResourceId);
 		 		sectionDB.insertSectionResource((Section)section, (MeleteResource)meleteResource);
 			 	}catch (IdUnusedException ex)
 				{
 			 		// actual insert
 			 		// if not found in meleteDocs collection include it
 			 		String uploadCollId = getMeleteCHService().getUploadCollectionId(destinationContext);
 			 		
 				  	// data is generally large so read it only if need to insert
 					if(section.getContentType().equals("typeLink"))
 					{
 //					 	 link points to Site Resources item so move it to MeleteDocs collection
 						if(hrefVal.indexOf("/access/content/group") != -1)
 						{
 							String fileResourceName= hrefVal.substring(hrefVal.lastIndexOf("/")+1);
 							if(!(fileResourceName.endsWith(".html") || fileResourceName.endsWith(".htm")))
 							{
 								if(resElements != null){
 									String fileName = ((Element)resElements.get(0)).attributeValue("href");
 									melContentData = meleteUtil.readFromFile(new File(getUnzippeddirpath() + File.separator+ fileName));
 									res_mime_type = fileName.substring(fileName.lastIndexOf(".")+1);
 									res_mime_type = ContentTypeImageService.getContentType(res_mime_type);
 									}
 								else
 						 		  {
 						 			//This is executed by import from site
 						 			String findEntity = hrefVal.substring(hrefVal.indexOf("/access")+7);
 									Reference ref = EntityManager.newReference(findEntity);
 									logger.debug("ref properties" + ref.getType() +"," +ref.getId());
 						 			ContentResource cr = getMeleteCHService().getResource(ref.getId());
 									melContentData = cr.getContent();
 									res_mime_type = cr.getContentType();
 						 		  }
 
 								logger.debug("first add resource" + fileResourceName);
 								ResourcePropertiesEdit res = getMeleteCHService().fillInSectionResourceProperties(encodingFlag,fileResourceName,melResourceDescription);
 								newResourceId = getMeleteCHService().addResourceItem(fileResourceName, res_mime_type,uploadCollId,melContentData,res );
 								MeleteResource firstResource = new MeleteResource();
 								firstResource.setResourceId(newResourceId);
 				            	sectionDB.insertResource(firstResource);
 
 								// this section points to the link location of added resource item
 								String secondResName = getMeleteCHService().getResourceUrl(newResourceId);
 						 		melContentData =secondResName.getBytes();
 						 		res_mime_type=getMeleteCHService().MIME_TYPE_LINK;
 							}
 						}
 						else
 					  	{
 					  	  res_mime_type=getMeleteCHService().MIME_TYPE_LINK;
 						  melContentData = new byte[hrefVal.length()];
 				          melContentData = hrefVal.getBytes();
 					  	}
 					}
 					if (section.getContentType().equals("typeUpload"))
 					{
 					  res_mime_type = melResourceName.substring(melResourceName.lastIndexOf(".")+1);
 					  res_mime_type = ContentTypeImageService.getContentType(res_mime_type);
 			 		  if (resElements != null)
 			 		  {
 			 			melContentData = meleteUtil.readFromFile(new File(getUnzippeddirpath() + File.separator+ hrefVal));
 			 		  }
 			 		  else
 			 		  {
 			 			//This is executed by import from site
 			 			logger.debug("reading resource properties in import from site");
 			 			ContentResource cr = getMeleteCHService().getResource(hrefVal);
 						melContentData = cr.getContent();
 			 		  }
 					}
 			 		logger.debug("add resource again for" + melResourceName);
 			 		ResourcePropertiesEdit res = getMeleteCHService().fillInSectionResourceProperties(encodingFlag,melResourceName,melResourceDescription);
 			 		newResourceId = getMeleteCHService().addResourceItem(melResourceName, res_mime_type,uploadCollId,melContentData,res );
 			 		meleteResource.setResourceId(newResourceId);
 			 		sectionDB.insertMeleteResource((Section)section, (MeleteResource)meleteResource);
 				} // catch end
 
 		if (logger.isDebugEnabled())
 			logger.debug("Exiting createSection...");
 	}
 
 	/* @param document document
 	 * @return resource element
 	 * @throws Exception
 	 */
 	private Element getResource(String resName, Document document)
 			throws Exception {
 		if (logger.isDebugEnabled())
 			logger.debug("Entering getResource...");
 
 		Map uris = new HashMap();
 		uris.put("imscp", DEFAULT_NAMESPACE_URI);
 		uris.put("imsmd", IMSMD_NAMESPACE_URI);
 
 		//resource
 		XPath xpath = document
 				.createXPath("/imscp:manifest/imscp:resources/imscp:resource[@identifier = '"
 						+ resName + "']");
 		xpath.setNamespaceURIs(uris);
 
 		Element eleRes = (Element) xpath.selectSingleNode(document);
 
 		if (logger.isDebugEnabled())
 			logger.debug("Exiting getResource...");
 
 		return eleRes;
 	}
 
 	private Element getMergeResource(String resName, Document document) throws Exception
 	{
 		if (logger.isDebugEnabled()) logger.debug("Entering getResource...");
 
 		Map uris = new HashMap();
 		uris.put("imscp", DEFAULT_NAMESPACE_URI);
 		uris.put("imsmd", IMSMD_NAMESPACE_URI);
 
 		// resource
 		XPath xpath = document.createXPath("//resource[@identifier = '" + resName + "']");
 		xpath.setNamespaceURIs(uris);
 
 		Element eleRes = (Element) xpath.selectSingleNode(document);
 
 		if (logger.isDebugEnabled()) logger.debug("Exiting getResource...");
 
 		return eleRes;
 }
 
 
 	/**
 	 *
 	 * create an instance of moduleshdates. Revised to open a course for one
 	 * year by default --Rashmi 12/6 Revised on 12/20 Rashmi to set start
 	 * default time as 8:00 am and end date time as 11:59 pm
 	 */
 	private ModuleShdates getModuleShdates() {
 		if (moduleShdates == null) {
 			moduleShdates = new ModuleShdates();
 			//comment code below to not assign any dates on IMS CP import
 	/*		GregorianCalendar cal = new GregorianCalendar();
 			cal.set(Calendar.HOUR, 8);
 			cal.set(Calendar.MINUTE, 0);
 			cal.set(Calendar.SECOND, 0);
 			cal.set(Calendar.AM_PM, Calendar.AM);
 			moduleShdates.setStartDate(cal.getTime());
 			cal.add(Calendar.YEAR, 1);
 			cal.set(Calendar.HOUR, 11);
 			cal.set(Calendar.MINUTE, 59);
 			cal.set(Calendar.SECOND, 0);
 			cal.set(Calendar.AM_PM, Calendar.PM);
 			moduleShdates.setEndDate(cal.getTime());*/
 		}
 		return moduleShdates;
 	}
 
 	/**
 	 *
 	 * uploaded or new content written file is temp stored at c:\\uploads.
 	 * filename format of temporary file is moduleidSectionTitle.html later on,
 	 * when saving module, this file will be stored under right directory
 	 * structure under module dir with name as section_"seq".html
 	 *
 	 * IMP NOTE: NEED TO READ IP ADDRESS FROM SESSION OR SOMEWHERE ELSE
 	 */
 	private String createContentFile(String contentEditor, Module module, Section section, List resElements)throws Exception{
 		//save uploaded img inside content editor to destination directory
 		String checkforimgs = contentEditor;
 		int imgindex = -1;
         String imgSrcPath, imgName, imgLoc;
 		String courseId ="";
 
 		courseId =destinationContext;
 
 		int startSrc =0;
 		int endSrc = 0;
 
 		while (checkforimgs != null) {
 			ArrayList embedData = meleteUtil.findEmbedItemPattern(checkforimgs);
 			checkforimgs = (String)embedData.get(0);
 			if (embedData.size() > 1)
 			{
 				startSrc = ((Integer)embedData.get(1)).intValue();
 				endSrc = ((Integer)embedData.get(2)).intValue();
 			}
 			if (endSrc <= 0) break;
 
 			imgSrcPath = checkforimgs.substring(startSrc, endSrc);
 
 			// changed on 10/16/06 - add https condition too
 			if (resElements != null)
 			{
 				//This part executed by IMS import
 				if (!(imgSrcPath.startsWith("http://")|| imgSrcPath.startsWith("https://")) )
 				{
 				// if img src is in library or any other inside sakai path then don't process
 				if(imgSrcPath.startsWith("images"))
 				{
 				checkforimgs = checkforimgs.substring(endSrc);
 				String imgActualPath="";
 				for (Iterator iter = resElements.iterator(); iter.hasNext();) {
 					Element element = (Element) iter.next();
 					if (element.getQualifiedName().equalsIgnoreCase("file")) {
 						Attribute hrefAttr = element.attribute("href");
 						if ((hrefAttr.getValue().indexOf(imgSrcPath)) != -1)
 						{
 							imgActualPath = hrefAttr.getValue().trim();
 							break;
 						}
 					}
 				}
 				contentEditor = ReplaceEmbedMediaWithResourceURL(contentEditor, imgSrcPath, imgActualPath, courseId, true);
 			    } // if check for images
 			    } //if http check end
 			}//IMS import (original code) ends here
 			else
 			{
               //This part executed by import from site
 				String imgActualPath = "";
 			  if (!(imgSrcPath.startsWith("http://")|| imgSrcPath.startsWith("https://")) )
 			  {
 			  // if img src is in library or any other inside sakai path then don't process
 				 if(imgSrcPath.indexOf("/access") !=-1)
 					{
 					String findEntity = imgSrcPath.substring(imgSrcPath.indexOf("/access")+7);
 					Reference ref = EntityManager.newReference(findEntity);
 					logger.debug("ref properties" + ref.getType() +"," +ref.getId());
 
 					if(ref.getType().equals(ContentHostingService.APPLICATION_ID) || ref.getType().equals(MeleteSecurityService.APPLICATION_ID))
 					{
 						if(ref.getType().equals(ContentHostingService.APPLICATION_ID))
 						{
 //							Item resides in resources
 						    checkforimgs = checkforimgs.substring(endSrc);
 						    imgActualPath = ref.getId();
 						    contentEditor = ReplaceEmbedMediaWithResourceURL(contentEditor, imgSrcPath, imgActualPath, courseId, false);
 
 						}
 						if (ref.getType().equals(MeleteSecurityService.APPLICATION_ID))
 						{
 
 							//Item resides in meleteDocs, so need not check under resources
 							checkforimgs = checkforimgs.substring(endSrc);
 							imgActualPath = ref.getId().replaceFirst("/content","");
 							contentEditor = ReplaceEmbedMediaWithResourceURL(contentEditor, imgSrcPath, imgActualPath, courseId, false);
 						}
 					}
 			     }
 			     }
 			     }
 			//Import from site ends here
 			imgindex = -1;
             startSrc=0; endSrc = 0;
 		}
 		return contentEditor;
 	}
 
 	private String ReplaceEmbedMediaWithResourceURL(String contentEditor, String imgSrcPath, String imgActualPath, String courseId, boolean imsImport)
 	{
 		 		String replacementStr = uploadSectionDependentFile(imgActualPath, courseId, imsImport);
 				//Upon import, embedded media was getting full url without code below
 				if (replacementStr.startsWith(ServerConfigurationService.getServerUrl()))
 				{
 					replacementStr = replacementStr.replace(ServerConfigurationService.getServerUrl(), "");
 				}
 				Pattern pattern = Pattern.compile(Pattern.quote(imgSrcPath));
 				// Replace all occurrences of pattern in input
 				contentEditor = meleteUtil.replace(contentEditor,imgSrcPath, replacementStr);
 
 			return contentEditor;
 	}
 
 	/*METHODS USED BY IMPORT FROM SITE BEGIN*/
 	public void copyModules(String fromContext, String toContext)
 	{
 		//Copy the uploads collection
 	    this.destinationContext = toContext;
   	   	buildModules(fromContext, toContext);
   	//   	setMeleteSitePreference(fromContext, toContext);
 	}
 
 	private void setMeleteSitePreference(String fromContext, String toContext)
 	{
 		MeleteSitePreference fromMsp = meleteUserPrefDB.getSitePreferences(fromContext);
 		meleteUserPrefDB.setSitePreferences(toContext,fromMsp.isPrintable(),fromMsp.isAutonumber());
 	}
 
 	private void buildModules(String fromContext, String toContext)
 	{
 //		Get modules in site A
 		Map sectionList = null;
 		MeleteResource toMres = null;
 		int fromSecId, toSecId;
 		List fromModuleList = moduleDB.getModules(fromContext);
 		//Iterate through all modules in site A
 		if (fromModuleList == null || fromModuleList.size() <= 0) return;
 
 		for (ListIterator i = fromModuleList.listIterator(); i.hasNext(); )
 		{
 			Module fromMod = (Module) i.next();
 			String fromModSeqXml = fromMod.getSeqXml();
 
 			//Copy module properties and insert, seqXml is null for now
 			Module toMod = new Module(fromMod.getTitle(), fromMod.getLearnObj(), fromMod.getDescription(), fromMod.getKeywords(), fromMod.getCreatedByFname(), fromMod.getCreatedByLname(), fromMod.getUserId(), fromMod.getModifiedByFname(), fromMod.getModifiedByLname(), fromMod.getInstitute(), fromMod.getWhatsNext(), fromMod.getCreationDate(), fromMod.getModificationDate(), null);
 			ModuleShdates toModshdate = new ModuleShdates(((ModuleShdates)fromMod.getModuleshdate()).getStartDate(), ((ModuleShdates)fromMod.getModuleshdate()).getEndDate());
 			try{
 			moduleDB.addModule(toMod, toModshdate, fromMod.getUserId(), toContext);
 			}catch(Exception ex3){logger.error("error importing module");}
 			sectionList = fromMod.getSections();
 			//Iterate throug sections of a module
 			if (sectionList != null)
 			{
 				int mapSize = sectionList.size();
 				if (mapSize > 0)
 				{
 					Iterator keyValuePairs = sectionList.entrySet().iterator();
 					while (keyValuePairs.hasNext())
 					{
 						Map.Entry entry = (Map.Entry) keyValuePairs.next();
 						Section fromSec = (Section) entry.getValue();
 						fromSecId = fromSec.getSectionId().intValue();						
 						Section toSec = new Section(fromSec.getTitle(), fromSec.getCreatedByFname(), fromSec.getCreatedByLname(), fromSec.getModifiedByFname(), fromSec.getModifiedByLname(), fromSec.getInstr(), fromSec.getContentType(), fromSec.isAudioContent(), fromSec.isVideoContent(), fromSec.isTextualContent(), fromSec.isOpenWindow(), fromSec.isDeleteFlag(), fromSec.getCreationDate(), fromSec.getModificationDate());
 						logger.debug("copied section open window value" + toSec.getTitle()+"," + toSec.isOpenWindow() );
 						try
 						{
 							//Insert into the SECTION table
 							sectionDB.addSection(toMod, toSec, false);
 							toSecId = toSec.getSectionId().intValue();
 							//Replace old references of sections to new references in SEQ xml
 							//TODO : Move the update seqxml lower down so sequence does not update
 							//if exception is thrown
 							if(!fromSec.getContentType().equals("notype") && fromSec.getSectionResource() != null)
 							{
 								toMres = new MeleteResource((MeleteResource)fromSec.getSectionResource().getResource());
 								toMres.setResourceId(null);
 								createContentResource(toMod,toSec,toMres,((MeleteResource)fromSec.getSectionResource().getResource()).getResourceId(),null);
 							}
 							if (fromModSeqXml!=null)
 								fromModSeqXml = fromModSeqXml.replace(Integer.toString(fromSecId), Integer.toString(toSecId));
 
 						}
 						catch(Exception ex)
 						{
 							logger.error("error in inserting section "+ ex.toString());
 							ex.printStackTrace();
 							//rollback and delete section
 							try
 							{
 								sectionDB.deleteSection(toSec,toContext, null);
 							}
 							catch (Exception ex2)
 							{
 								logger.error("Error in deleting section "+ex2.toString());
 							}
 						}
 
 					}
 
 					//Finally, update the seqXml for the module
 					toMod.setSeqXml(fromModSeqXml);
 					try
 					{
 						moduleDB.updateModule(toMod);
 					}
 					catch (Exception ex)
 					{
 						logger.error("error in updating module");
 					}
 
 				}
 			}
 
 		}
 			
 	}
 	/*METHODS USED BY IMPORT FROM SITE END*/
 
 	/**
 	 * deletes the file and its children
 	 * @param delfile - file to be deleted
 	 */
 	public void deleteFiles(File delfile){
 
 		if (delfile.isDirectory()){
 			File files[] = delfile.listFiles();
 			int i = files.length;
 			while (i > 0)
 				deleteFiles(files[--i]);
 
 			delfile.delete();
 		}else
 			delfile.delete();
 
 	}
 
 	public String getDestinationContext()
 	{
 		return this.destinationContext;
 	}
 
 	public void setDestinationContext(String destinationContext)
 	{
 		this.destinationContext = destinationContext;
 	}
 
 	public void setModuleDB(ModuleDB moduleDB) {
 		this.moduleDB = moduleDB;
 	}
 
 	/**
 	 * @param sectionDB The sectionDB to set.
 	 */
 	public void setSectionDB(SectionDB sectionDB) {
 		this.sectionDB = sectionDB;
 	}
 	/**
 	 * @return Returns the unzippeddirpath.
 	 */
 	protected String getUnzippeddirpath() {
 		return unzippeddirpath;
 	}
 	/**
 	 * @param unzippeddirpath The unzippeddirpath to set.
 	 */
 	protected void setUnzippeddirpath(String unzippeddirpath) {
 		this.unzippeddirpath = unzippeddirpath;
 	}
 
 	/**
 	 * @return Returns the meleteCHService.
 	 */
 	public MeleteCHService getMeleteCHService() {
 		return meleteCHService;
 	}
 	/**
 	 * @param meleteCHService The meleteCHService to set.
 	 */
 	public void setMeleteCHService(MeleteCHService meleteCHService) {
 		this.meleteCHService = meleteCHService;
 	}
 	/**
 	 * @return Returns the meleteLicenseDB.
 	 */
 	public MeleteLicenseDB getMeleteLicenseDB() {
 		return meleteLicenseDB;
 	}
 	/**
 	 * @param meleteLicenseDB The meleteLicenseDB to set.
 	 */
 	public void setMeleteLicenseDB(MeleteLicenseDB meleteLicenseDB) {
 		this.meleteLicenseDB = meleteLicenseDB;
 	}
 	/**
 	 * @return the meleteUserPrefDB
 	 */
 	public MeleteUserPreferenceDB getMeleteUserPrefDB()
 	{
 		return this.meleteUserPrefDB;
 	}
 	/**
 	 * @param meleteUserPrefDB the meleteUserPrefDB to set
 	 */
 	public void setMeleteUserPrefDB(MeleteUserPreferenceDB meleteUserPrefDB)
 	{
 		this.meleteUserPrefDB = meleteUserPrefDB;
 	}
 }
