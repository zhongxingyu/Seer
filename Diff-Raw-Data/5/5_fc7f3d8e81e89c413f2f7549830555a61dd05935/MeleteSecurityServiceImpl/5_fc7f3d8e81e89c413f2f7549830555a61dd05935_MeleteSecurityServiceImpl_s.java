 /**********************************************************************************
  *
  * $URL$
  * $Id$
  ***********************************************************************************
  *
  * Copyright (c) 2008, 2009 Etudes, Inc.
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
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import java.util.Stack;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.ServletOutputStream;
 
 import org.etudes.api.app.melete.MeleteExportService;
 import org.etudes.api.app.melete.MeleteSecurityService;
 import org.etudes.api.app.melete.ModuleService;
 import org.etudes.api.app.melete.MeleteImportfromSiteService;
 import org.etudes.api.app.melete.MeleteImportService;
 import org.sakaiproject.authz.cover.FunctionManager;
 import org.sakaiproject.tool.cover.ToolManager;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.dom4j.Attribute;
 import org.dom4j.DocumentHelper;
 import org.dom4j.Namespace;
 import org.dom4j.QName;
 import org.sakaiproject.authz.api.SecurityAdvisor;
 import org.sakaiproject.authz.cover.SecurityService;
 import org.sakaiproject.content.api.ContentHostingService;
 import org.sakaiproject.content.api.ContentResource;
 import org.sakaiproject.entity.api.Entity;
 import org.sakaiproject.entity.api.EntityAccessOverloadException;
 import org.sakaiproject.entity.api.EntityCopyrightException;
 import org.sakaiproject.entity.cover.EntityManager;
 import org.sakaiproject.entity.api.EntityNotDefinedException;
 import org.sakaiproject.entity.api.EntityPermissionException;
 import org.sakaiproject.entity.api.EntityProducer;
 import org.sakaiproject.entity.api.EntityTransferrer;
 import org.sakaiproject.entity.api.HttpAccess;
 import org.sakaiproject.entity.api.Reference;
 import org.sakaiproject.entity.api.ResourceProperties;
 import org.sakaiproject.tool.cover.SessionManager;
 import org.sakaiproject.component.cover.ServerConfigurationService;
 import org.sakaiproject.util.StringUtil;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.DOMException;
 import org.w3c.dom.Node;
 import org.sakaiproject.site.cover.SiteService;
 import org.sakaiproject.exception.IdUnusedException;
 import org.sakaiproject.util.Xml;
 import org.imsglobal.basiclti.BasicLTIUtil;
 import org.etudes.simpleti.SakaiSimpleLTI;
 import org.etudes.basiclti.SakaiBLTIUtil;
 import org.sakaiproject.thread_local.api.ThreadLocalManager;
 import org.sakaiproject.util.ResourceLoader;
 
 /*
  * MeleteSecurityService is the implementation of MeleteSecurityService
  * that provides the access permissions to the melete
  *
  * @author Foot hill college
  * @version $Revision$
  * Mallika - 5/15/07 - Adding code to enable import from site
  */
 public class MeleteSecurityServiceImpl implements MeleteSecurityService,EntityProducer,EntityTransferrer {
 
 	private ModuleService moduleService;
 	private MeleteImportfromSiteService meleteImportfromSiteService;
 	private MeleteImportService meleteImportService;
 	private MeleteExportService meleteExportService;
 
         // Keep both of these here for upwards compatibility - Chuck
 	public static final String MIME_TYPE_SLTI="ims/simplelti";
 	public static final String MIME_TYPE_BLTI="ims/basiclti";
 
         private static ResourceLoader rb = new ResourceLoader("security_svc");
 
 	// Note: security needs a proper Resource reference
 
 	/*******************************************************************************
 	* Dependencies and their setter methods
 	*******************************************************************************/
 
 	/** Dependency: a logger component. */
 	private Log logger = LogFactory.getLog(MeleteSecurityServiceImpl.class);
 	private ThreadLocalManager threadLocalManager = org.sakaiproject.thread_local.cover.ThreadLocalManager.getInstance();
 /**
 	 * Setup a security advisor.
 	 */
 	public void pushAdvisor()
 	{
 		// setup a security advisor
 		SecurityService.pushAdvisor(new SecurityAdvisor()
 		{
 			public SecurityAdvice isAllowed(String userId, String function, String reference)
 			{
 				  return SecurityAdvice.ALLOWED;
 			}
 		});
 	}
 
 	/**
 	 * Remove our security advisor.
 	 */
 	public void popAdvisor()
 	{
 		SecurityService.popAdvisor();
 	}
 
 	/**
 	 * Check security for this entity.
 	 *
 	 * @param ref
 	 *        The Reference to the entity.
 	 * @return true if allowed, false if not.
 	 */
 	protected boolean checkSecurity(Reference ref)
 	{
 
 		//Need to add additional code here to make sure the section corresponding to the
 		//resource is visible, not deleted or inactivated
 		boolean result = false;
 		try
 		{
 		  result = allowAuthor(ref.getContext()) || allowStudent(ref.getContext());
 		}
 		catch (Exception e)
 		{
 			logger.error(e.toString());
 		}
 		return result;
 	}
 	/*******************************************************************************
 	* Init and Destroy
 	*******************************************************************************/
 
 	/**
 	 * Final initialization, once all dependencies are set.
 	 */
 	public void init()
 	{
 
 
 		logger.info(this +".init()");
 // setup a security advisor
 		pushAdvisor();
 
 		try
 		{
 			// register as an entity producer
 			EntityManager.registerEntityProducer(this,REFERENCE_ROOT);
 
 			//register melete functions
 			FunctionManager.registerFunction(SECURE_AUTHOR);
 		    FunctionManager.registerFunction(SECURE_STUDENT);
 
 
 		}
 		catch (Throwable t)
 		{
 			logger.warn("init(): ", t);
 		}
 
 		finally
 		{
 			// clear the security advisor
 			popAdvisor();
 		}
 	}
 
 	/**
 	 * Final cleanup.
 	 */
 	public void destroy()
 	{
 		logger.info(this +".destroy()");
 	}
 
 	/**
 	 *
 	 */
 	public MeleteSecurityServiceImpl() {
 		super();
 
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public boolean allowAuthor(String reference)throws Exception {
 
       try {
 			return SecurityService.unlock(SECURE_AUTHOR, getContextSiteId(reference));
     	} catch (Exception e) {
 			throw new Exception(this.getClass().getName()+ " : allowAuthor(reference) : " + e.toString());
 		}
 	}
 
 	public boolean allowStudent(String reference)throws Exception{
 
          try {
 			  return SecurityService.unlock(SECURE_STUDENT, getContextSiteId(reference));
 		} catch (Exception e) {
 			throw new Exception(this.getClass().getName()+ " : allowStudent(reference) : " + e.toString());
 		}
 	}
 
 	public boolean allowAuthor()throws Exception {
 
 		try {
 			return SecurityService.unlock(SECURE_AUTHOR, getContextSiteId(ToolManager.getCurrentPlacement().getContext()));
 		} catch (Exception e) {
 			throw new Exception(this.getClass().getName()+ " : allowAuthor() : " + e.toString());
 		}
 	}
 
 	public boolean allowStudent()throws Exception{
 
 		try {
 			return SecurityService.unlock(SECURE_STUDENT, getContextSiteId(ToolManager.getCurrentPlacement().getContext()));
 		} catch (Exception e) {
 			throw new Exception(this.getClass().getName()+ " : allowStudent() : " + e.toString());
 		}
 	}
 
 	public boolean isSuperUser(String userId)
 	{
 		return SecurityService.isSuperUser(userId);
 	}
 
 	/**
 	 * @return siteId
 	 */
 	private String getContextSiteId(String reference) {
 		  return ("/site/" + reference);
 	}
 
 		/*******************************************************************************************************************************
 		 * EntityProducer
 		 ******************************************************************************************************************************/
 
 		/**
 		 * {@inheritDoc}
 		 */
 		public boolean parseEntityReference(String reference, Reference ref)
 		{
 			if (reference.startsWith(REFERENCE_ROOT))
 			{
 				// we will get null, sampleAccess, content, private, sampleAccess, <context>, test.txt
 				// we will store the context, and the ContentHosting reference in our id field.
 				String id = null;
 				String context = null;
 				String[] parts = StringUtil.split(reference, Entity.SEPARATOR);
 
 				if (parts.length > 5)
 				{
 					context = parts[5];
 					//Should the slashes below be entityseparator
 					id = "/" + StringUtil.unsplit(parts, 2, parts.length - 2, "/");
 				}
 
 				ref.set(APPLICATION_ID, null, id, null, context);
 
 				return true;
 			}
 
 			return false;
 		}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public HttpAccess getHttpAccess()
 	{
 		return new HttpAccess()
 		{
 			public void handleAccess(HttpServletRequest req, HttpServletResponse res, Reference ref,
 					Collection copyrightAcceptedRefs) throws EntityPermissionException, EntityNotDefinedException,
 					EntityAccessOverloadException, EntityCopyrightException
 			{
 				// decide on security
 				if (!checkSecurity(ref))
 				{
 					throw new EntityPermissionException(SessionManager.getCurrentSessionUserId(), "meleteDocs", ref
 							.getReference());
 				}
 
 				boolean handled = false;
 				// Find the site we are coming from
 				String contextId = ref.getContext();
 				// isolate the ContentHosting reference
 				Reference contentHostingRef = EntityManager.newReference(ref.getId());
 
 				// setup a security advisor
 				pushAdvisor();
 				try
 				{
 					// make sure we have a valid ContentHosting reference with an entity producer we can talk to
 					EntityProducer service = contentHostingRef.getEntityProducer();
 					if (service == null) throw new EntityNotDefinedException(ref.getReference());
 
 					if ( service instanceof ContentHostingService )
 					{
 						ContentHostingService chService = (ContentHostingService) service;
 						try
 						{
 							ContentResource content = chService.getResource(contentHostingRef.getId());
 							if ( MIME_TYPE_SLTI.equals(content.getContentType()) || 
 							     MIME_TYPE_BLTI.equals(content.getContentType()) )
 							{
 								byte [] bytes = content.getContent();
 								ResourceProperties resprops = content.getProperties();
 								String str = new String(bytes);
 								String postData = null;
 								if ( BasicLTIUtil.validateDescriptor(str) != null ) 
 								{
 									try
                                                                 	{
                                                                         	popAdvisor();
 										// Leave ResourceBundle off for now
 										String [] retval = SakaiBLTIUtil.postLaunchHTML(str, contextId, ref.getId(), resprops, rb);
 										if ( retval != null ) postData = retval[0];
                                                                 	}
                                                                 	catch (Exception e)
                                                                 	{
                                                                         	logger.info("Exception e "+e.getMessage());
                                                                         	e.printStackTrace();
                                                                 	}
                                                                 	finally
                                                                 	{
                                                                         	pushAdvisor();
                                                                 	}
 								}
 								else // Attempt SimpleLTI
 								{
 									Properties props = null;
 									// We must remove our advisor while we do the launch and then put it back
 									// Otherwise the launch will give the user too much power
 									try
 									{
 										popAdvisor();
 										props = SakaiSimpleLTI.doLaunch(str, ref.getContext(), ref.getId());
 									}
 									catch (Exception e)
 									{
 										logger.info("Exception e "+e.getMessage());
 										e.printStackTrace();
 									}
 									finally
 									{
 										pushAdvisor();
 									}
 									if ( props != null ) {
 										postData = props.getProperty("htmltext");
 									}
 								}
 
 								if ( postData == null ) {
 									String msg = rb.getString("not.configured", "Not configured.");
 									postData = "<p>"+msg+"</p>\n<!--\n"+str+"-->\n";
 								} 
 
 								if ( postData != null )
 								{
 									res.setContentType("text/html");
 									ServletOutputStream out = res.getOutputStream();
 									out.println(postData);
 									handled = true;
 								}
 
 							}
 						}
 						catch (Exception e)
 						{
 						//	logger.info("Exception e "+e.getMessage());
 						//	e.printStackTrace();
 						}
 					}
 					if ( !handled ) {
 						// get the producer's HttpAccess helper, it might not support one
 						HttpAccess access = service.getHttpAccess();
 						if (access == null) throw new EntityNotDefinedException(ref.getReference());
 
 						// let the helper do the work
 						access.handleAccess(req, res, contentHostingRef, copyrightAcceptedRefs);
 					}
 				}
 				finally
 				{
 					// clear the security advisor
 					popAdvisor();
 				}
 			}
 		};
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public Entity getEntity(Reference ref)
 	{
 		// decide on security
 		if (!checkSecurity(ref)) return null;
 
 		// isolate the ContentHosting reference
 		Reference contentHostingRef = EntityManager.newReference(ref.getId());
 
 		// setup a security advisor
 		pushAdvisor();
 		try
 		{
 			// make sure we have a valid ContentHosting reference with an entity producer we can talk to
 			EntityProducer service = ref.getEntityProducer();
 			if (service == null) return null;
 
 			// pass on the request
 			return service.getEntity(contentHostingRef);
 		}
 		finally
 		{
 			// clear the security advisor
 			popAdvisor();
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public Collection getEntityAuthzGroups(Reference ref, String userId)
 	{
 		// Since we handle security ourself, we won't support anyone else asking
 		return null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public String getEntityDescription(Reference ref)
 	{
 		// decide on security
 		if (!checkSecurity(ref)) return null;
 
 		// isolate the ContentHosting reference
 		Reference contentHostingRef = EntityManager.newReference(ref.getId());
 
 		// setup a security advisor
 		pushAdvisor();
 		try
 		{
 			// make sure we have a valid ContentHosting reference with an entity producer we can talk to
 			EntityProducer service = ref.getEntityProducer();
 			if (service == null) return null;
 
 			// pass on the request
 			return service.getEntityDescription(contentHostingRef);
 		}
 		finally
 		{
 			// clear the security advisor
 			popAdvisor();
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public ResourceProperties getEntityResourceProperties(Reference ref)
 	{
 		// decide on security
 		if (!checkSecurity(ref)) return null;
 
 		// isolate the ContentHosting reference
 		Reference contentHostingRef = EntityManager.newReference(ref.getId());
 
 		// setup a security advisor
 		pushAdvisor();
 		try
 		{
 			// make sure we have a valid ContentHosting reference with an entity producer we can talk to
 			EntityProducer service = ref.getEntityProducer();
 			if (service == null) return null;
 
 			// pass on the request
 			return service.getEntityResourceProperties(contentHostingRef);
 		}
 		finally
 		{
 			// clear the security advisor
 			popAdvisor();
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public String getEntityUrl(Reference ref)
 	{		return ServerConfigurationService.getAccessUrl() + ref.getReference();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public String getLabel()
 	{
 		return "melete";
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public String merge(String siteId, Element root, String archivePath, String fromSiteId, Map attachmentNames, Map userIdTrans,
 			Set userListAllowImport)
 	{
 		logger.debug("merge of melete" + siteId +"," +fromSiteId + ","+root.toString());
 		int count = 0;
 		try{
 		org.w3c.dom.Document w3doc = Xml.createDocument();
 		org.w3c.dom.Element w3root = (org.w3c.dom.Element)w3doc.importNode(root, true);
 		w3doc.appendChild(w3root);
 
 		//convert to dom4j doc
 		org.dom4j.io.DOMReader domReader = new org.dom4j.io.DOMReader();
 		org.dom4j.Document domDoc =	domReader.read(w3doc);
 		logger.debug("archive str " + archivePath + archivePath.lastIndexOf(File.separator));
 		archivePath = archivePath.substring(0,archivePath.lastIndexOf("/"));
 		count = getMeleteImportService().mergeAndBuildModules(domDoc,archivePath,siteId);
 		}catch(Exception e)
 		{
 			e.printStackTrace();
 			return "error on merging modules content";
 		}
 		return "merging modules content: (" + count+ ") modules \n";
 	}
 
 	private String createdom4jtree(org.dom4j.Element oneelement)
 	{
 	org.dom4j.Document document4jmelete = DocumentHelper.createDocument();
 	org.dom4j.Element document4jmeleteRoot = document4jmelete.getRootElement();
 	org.dom4j.Element organizationNewElement = oneelement.createCopy();
 	organizationNewElement.setParent(document4jmeleteRoot);
 	document4jmelete.add(organizationNewElement);
 	return document4jmelete.asXML();
 	}
 	/**
 	 * {@inheritDoc}
 	 */
 	public String archive(String siteId, Document doc, Stack stack, String archivePath, List attachments)
 	{
 		logger.debug("siteid as arg in archive function is " + siteId);
 		int count = 0;
 		try
 		{
 			Element modulesElement = doc.createElement(MeleteSecurityService.class.getName());
 
 			if (siteId != null && siteId.length() > 0)
 			{
 				List<Module> selectList = getModuleService().getModules(siteId);
 				count = selectList.size();
 				File basePackDir = new File(archivePath);
 				List orgResElements = getMeleteExportService()
 					.generateOrganizationResourceItems(selectList,true,
 							basePackDir, SiteService.getSite(siteId).getTitle(), siteId);
 
 					if (orgResElements != null && orgResElements.size() > 0) {
 
 						String xmlstr =  createdom4jtree((org.dom4j.Element)(org.dom4j.Element)orgResElements.get(0));
 						// read organizations 4j document as w3c document
 						org.w3c.dom.Document meletew3cDocument = Xml.readDocumentFromString(xmlstr);
 						org.w3c.dom.Element meletew3cElement = (org.w3c.dom.Element)meletew3cDocument.getFirstChild();
 						org.w3c.dom.Element meletew3cNewElement = (org.w3c.dom.Element)((Element) stack.peek()).getOwnerDocument().importNode(meletew3cElement,true);
 						modulesElement.appendChild(meletew3cNewElement);
 
 						// now resources document
 						xmlstr =  createdom4jtree((org.dom4j.Element)(org.dom4j.Element)orgResElements.get(1));
 
 						org.w3c.dom.Document meletew3cResDocument = Xml.readDocumentFromString(xmlstr);
 						org.w3c.dom.Element meletew3cElement1 = (org.w3c.dom.Element)meletew3cResDocument.getFirstChild();
 						org.w3c.dom.Element meletew3cNewElement1 = (org.w3c.dom.Element)((Element) stack.peek()).getOwnerDocument().importNode(meletew3cElement1,true);
 						modulesElement.appendChild(meletew3cNewElement1);
 
 						((Element) stack.peek()).appendChild(modulesElement);
 						stack.push(modulesElement);
 				}
 			}
 	//		stack.pop();
 		}
 		catch (IdUnusedException iue)
 		{
 			logger.debug("error in melete during site archive");
 			return "error archiving modules";
 		}
 		catch (Exception ex)
 		{
 			if (logger.isDebugEnabled()) {
 			logger.debug("error in melete during site archive" + ex.toString());
 			ex.printStackTrace();
 			}
 			return "error archiving modules";
 		}
 		return "archiving modules: (" +count + ") modules archived successfully. \n";
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public boolean willArchiveMerge()
 	{
 		return true;
 	}
 
     public String[] myToolIds()
 	{
 		String[] toolIds = { "sakai.melete" };
 		return toolIds;
 	}
 
 	public void transferCopyEntities(String fromContext, String toContext, List ids)
 	{
 		try
 		{
 			logger.debug("transer copy Melete items by transferCopyEntities");
 			 Set<String> importResources =  new HashSet<String>();
 			 Set<String> addNowResources =  new HashSet<String>();
 			 threadLocalManager.set("MELETE_importResources" , importResources);
 			 threadLocalManager.set("MELETE_addedNowResource" , addNowResources);
 
 			getMeleteImportfromSiteService().copyModules(fromContext, toContext);
 			logger.debug("importResources: End importing melete data");
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			logger.error(e.getMessage(), e);
 		}
 	}
 
    public ModuleService getModuleService() {
 	        return moduleService;
 	    }
 
    public void setModuleService(ModuleService moduleService) {
 	        this.moduleService = moduleService;
 	 }
 
 
     public MeleteImportfromSiteService getMeleteImportfromSiteService() {
 	return meleteImportfromSiteService;
    }
 
 
      public void setMeleteImportfromSiteService(
 		MeleteImportfromSiteService meleteImportfromSiteService) {
 	this.meleteImportfromSiteService = meleteImportfromSiteService;
     }
 
 
 	/**
 	 * @return the meleteExportService
 	 */
 	public MeleteExportService getMeleteExportService()
 	{
 		return this.meleteExportService;
 	}
 
 
 	/**
 	 * @param meleteExportService the meleteExportService to set
 	 */
 	public void setMeleteExportService(MeleteExportService meleteExportService)
 	{
 		this.meleteExportService = meleteExportService;
 	}
 
 	public MeleteImportService getMeleteImportService() {
 		return meleteImportService;
 	}
 
 	public void setMeleteImportService(MeleteImportService meleteImportService) {
 		this.meleteImportService = meleteImportService;
 	}
 }
