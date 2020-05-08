 /* ===============================================================================
  *
  * Part of the InfoGlue Content Management Platform (www.infoglue.org)
  *
  * ===============================================================================
  *
  *  Copyright (C)
  * 
  * This program is free software; you can redistribute it and/or modify it under
  * the terms of the GNU General Public License version 2, as published by the
  * Free Software Foundation. See the file LICENSE.html for more information.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY, including the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * this program; if not, write to the Free Software Foundation, Inc. / 59 Temple
  * Place, Suite 330 / Boston, MA 02111-1307 / USA.
  *
  * ===============================================================================
  */
 
 package org.infoglue.cms.applications.structuretool.actions;
 
 import org.exolab.castor.jdo.Database;
 import org.infoglue.cms.applications.common.VisualFormatter;
 import org.infoglue.cms.entities.structure.ServiceBindingVO;
 import org.infoglue.cms.entities.structure.SiteNodeVO;
 import org.infoglue.cms.entities.workflow.EventVO;
 import org.infoglue.cms.entities.content.ContentVO;
 import org.infoglue.cms.entities.content.ContentVersionVO;
 import org.infoglue.cms.entities.management.AvailableServiceBindingVO;
 import org.infoglue.cms.entities.management.LanguageVO;
 import org.infoglue.cms.entities.management.SiteNodeTypeDefinitionVO;
 import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
 import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
 import org.infoglue.cms.controllers.kernel.impl.simple.*;
 import org.infoglue.cms.entities.structure.*;
 import org.infoglue.cms.entities.management.*;
 import org.infoglue.cms.exception.SystemException;
 
 import org.infoglue.cms.util.CmsPropertyHandler;
 
 import com.opensymphony.module.propertyset.PropertySet;
 import com.opensymphony.module.propertyset.PropertySetManager;
 
 import java.net.URLEncoder;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Iterator;
 import java.util.ArrayList;
 import java.util.Map;
 
 
 /** 
  * This class represents the view of a siteNode to the user. In fact - it presents the
  * view of the siteNode as well as the view of the latest siteNodeVersion as well.
  */
 
 public class ViewSiteNodeAction extends InfoGlueAbstractAction
 {
 	private Integer unrefreshedSiteNodeId 	= new Integer(0);
 	private Integer changeTypeId 			= new Integer(0);
 	private Integer repositoryId 			= null;
 	private SiteNodeTypeDefinitionVO siteNodeTypeDefinitionVO;
 	private List availableServiceBindings 	= null;
 	private List serviceBindings 			= null;
 	private List referenceBeanList 			= new ArrayList();
 	private List availableLanguages			= new ArrayList();
 	private List disabledLanguages 			= new ArrayList();
 	private List referencingBeanList 		= new ArrayList();
 
 	private SiteNodeVO siteNodeVO;
 	private SiteNodeVersionVO siteNodeVersionVO;
 	
    	private String stay = null;
    	private String dest = "";
 
 
     public ViewSiteNodeAction()
     {
         this(new SiteNodeVO(), new SiteNodeVersionVO());
     }
     
     public ViewSiteNodeAction(SiteNodeVO siteNodeVO, SiteNodeVersionVO siteNodeVersionVO)
     {
 		getLogger().info("Construction ViewSiteNodeAction");
         this.siteNodeVO = siteNodeVO;
         this.siteNodeVersionVO = siteNodeVersionVO;
     }
 
 	protected void initialize(Integer siteNodeId) throws Exception
 	{
 		this.siteNodeVersionVO = SiteNodeVersionControllerProxy.getSiteNodeVersionControllerProxy().getACLatestActiveSiteNodeVersionVO(this.getInfoGluePrincipal(), siteNodeId);
 		getLogger().info("siteNodeVersionVO:" + siteNodeVersionVO.getId() + ":" + siteNodeVersionVO.getIsActive());
 		this.siteNodeVO = SiteNodeController.getSiteNodeVOWithId(siteNodeId);
 		this.repositoryId = this.siteNodeVO.getRepositoryId();
 		//SiteNodeControllerProxy.getController().getACSiteNodeVOWithId(this.getInfoGluePrincipal(), siteNodeId);
 		
 		if(siteNodeVO.getSiteNodeTypeDefinitionId() != null)
 		{
 			this.siteNodeTypeDefinitionVO = SiteNodeTypeDefinitionController.getController().getSiteNodeTypeDefinitionVOWithId(siteNodeVO.getSiteNodeTypeDefinitionId());
 			this.availableServiceBindings = SiteNodeTypeDefinitionController.getController().getAvailableServiceBindingVOList(siteNodeVO.getSiteNodeTypeDefinitionId());
 			this.serviceBindings = SiteNodeVersionController.getServiceBindningVOList(siteNodeVersionVO.getSiteNodeVersionId());
 		}
 	} 
 
 	protected void initialize(Integer siteNodeId, Database db) throws Exception
 	{
 		this.siteNodeVersionVO = SiteNodeVersionControllerProxy.getSiteNodeVersionControllerProxy().getACLatestActiveSiteNodeVersionVO(this.getInfoGluePrincipal(), siteNodeId, db);
 		getLogger().info("siteNodeVersionVO:" + siteNodeVersionVO.getId() + ":" + siteNodeVersionVO.getIsActive());
 		this.siteNodeVO = SiteNodeController.getSiteNodeVOWithId(siteNodeId, db);
 		
 	    if(this.siteNodeVO.getMetaInfoContentId() == null || this.siteNodeVO.getMetaInfoContentId().intValue() == -1)
 	    {
 	        boolean hadMetaInfo = false;
 	        
 			AvailableServiceBinding availableServiceBinding = AvailableServiceBindingController.getController().getAvailableServiceBindingWithName("Meta information", db, false);
 			
 			Collection serviceBindings = SiteNodeVersionController.getServiceBindningList(this.siteNodeVersionVO.getId(), db);
 			Iterator serviceBindingIterator = serviceBindings.iterator();
 			while(serviceBindingIterator.hasNext())
 			{
 				ServiceBinding serviceBinding = (ServiceBinding)serviceBindingIterator.next();
 				if(serviceBinding.getValueObject().getAvailableServiceBindingId().intValue() == availableServiceBinding.getAvailableServiceBindingId().intValue())
 				{
 					List boundContents = ContentController.getBoundContents(db, serviceBinding.getServiceBindingId()); 			
 					if(boundContents.size() > 0)
 					{
 						ContentVO contentVO = (ContentVO)boundContents.get(0);
 						hadMetaInfo = true;
 						if(siteNodeVO.getMetaInfoContentId() == null || siteNodeVO.getMetaInfoContentId().intValue() == -1)
 						    SiteNodeController.getController().setMetaInfoContentId(siteNodeVO.getId(), contentVO.getContentId(), db);
 						
 						break;
 					}						
 				}
 			}
 
     		if(!hadMetaInfo)
     		{
     		    SiteNode siteNode = SiteNodeController.getController().getSiteNodeWithId(this.siteNodeVO.getId(), db);
     		    SiteNodeController.getController().createSiteNodeMetaInfoContent(db, siteNode, siteNode.getRepository().getId(), this.getInfoGluePrincipal(), null).getValueObject();
     		}
 	    }
 
 		this.repositoryId = this.siteNodeVO.getRepositoryId();
 		//SiteNodeControllerProxy.getController().getACSiteNodeVOWithId(this.getInfoGluePrincipal(), siteNodeId);
 		
 		if(siteNodeVO.getSiteNodeTypeDefinitionId() != null)
 		{
 			this.siteNodeTypeDefinitionVO = SiteNodeTypeDefinitionController.getController().getSiteNodeTypeDefinitionVOWithId(siteNodeVO.getSiteNodeTypeDefinitionId(), db);
 			this.availableServiceBindings = SiteNodeTypeDefinitionController.getController().getAvailableServiceBindingVOList(siteNodeVO.getSiteNodeTypeDefinitionId(), db);
 			this.serviceBindings = SiteNodeVersionController.getServiceBindningVOList(siteNodeVersionVO.getSiteNodeVersionId(), db);
 		}
 	} 
 
 	protected void initializeSiteNodeCover(Integer siteNodeId) throws Exception
 	{
 		try
 		{
 		    this.referenceBeanList = RegistryController.getController().getReferencingObjectsForSiteNode(siteNodeId);
 		    this.referencingBeanList = RegistryController.getController().getReferencedObjects(SiteNodeVersion.class.getName(), siteNodeVersionVO.getSiteNodeVersionId().toString());
 		    getLogger().info("referenceBeanList:" + referenceBeanList.size());
 		    getLogger().info("referencingBeanList:" + referencingBeanList.size());
 		}
 		catch(Exception e)
 		{
 		    e.printStackTrace();
 		}
 		
 		this.availableLanguages = LanguageController.getController().getLanguageVOList(this.repositoryId);
 		
         Map args = new HashMap();
 	    args.put("globalKey", "infoglue");
 	    PropertySet ps = PropertySetManager.getInstance("jdbc", args);
 
 	    String disabledLanguagesString = ps.getString("siteNode_" + siteNodeId + "_disabledLanguages");
 	    getLogger().info("disabledLanguagesString:" + disabledLanguagesString);
 	    if(disabledLanguagesString != null && !disabledLanguagesString.equalsIgnoreCase(""))
 	    {
 	        String[] disabledLanguagesStringArray = disabledLanguagesString.split(",");
 	        for(int i=0; i<disabledLanguagesStringArray.length; i++)
 	        {
 	            try
 	            {
 		            LanguageVO languageVO = LanguageController.getController().getLanguageVOWithId(new Integer(disabledLanguagesStringArray[i]));
 		            getLogger().info("Adding languageVO to disabledLanguages:" + languageVO.getName());
 		    	    this.disabledLanguages.add(languageVO);
 	            }
 	            catch(Exception e)
 	            {
 	                getLogger().warn("An error occurred when we tried to get disabled language:" + e.getMessage(), e);
 	            }
 	        }
 	    }
 	} 
 
 	protected void initializeSiteNodeCover(Integer siteNodeId, Database db) throws Exception
 	{
 		try
 		{
 		    this.referenceBeanList = RegistryController.getController().getReferencingObjectsForSiteNode(siteNodeId, db);
 		    this.referencingBeanList = RegistryController.getController().getReferencedObjects(SiteNodeVersion.class.getName(), siteNodeVersionVO.getSiteNodeVersionId().toString(), db);
 		    getLogger().info("referenceBeanList:" + referenceBeanList.size());
 		    getLogger().info("referencingBeanList:" + referencingBeanList.size());
 		}
 		catch(Exception e)
 		{
 		    e.printStackTrace();
 		}
 		
 		this.availableLanguages = LanguageController.getController().getLanguageVOList(this.repositoryId, db);
 		
         Map args = new HashMap();
 	    args.put("globalKey", "infoglue");
 	    PropertySet ps = PropertySetManager.getInstance("jdbc", args);
 
 	    String disabledLanguagesString = ps.getString("siteNode_" + siteNodeId + "_disabledLanguages");
 	    getLogger().info("disabledLanguagesString:" + disabledLanguagesString);
 	    if(disabledLanguagesString != null && !disabledLanguagesString.equalsIgnoreCase(""))
 	    {
 	        String[] disabledLanguagesStringArray = disabledLanguagesString.split(",");
 	        for(int i=0; i<disabledLanguagesStringArray.length; i++)
 	        {
 	            try
 	            {
 		            LanguageVO languageVO = LanguageController.getController().getLanguageVOWithId(new Integer(disabledLanguagesStringArray[i]), db);
 		            getLogger().info("Adding languageVO to disabledLanguages:" + languageVO.getName());
 		    	    this.disabledLanguages.add(languageVO);
 	            }
 	            catch(Exception e)
 	            {
 	                getLogger().warn("An error occurred when we tried to get disabled language:" + e.getMessage(), e);
 	            }
 	        }
 	    }
 	} 
 
 /*    
     protected void initialize(Integer siteNodeId) throws Exception
     {
     	this.siteNodeVO = SiteNodeController.getSiteNodeVOWithId(siteNodeId);
         this.siteNodeVersionVO = SiteNodeVersionController.getLatestSiteNodeVersionVO(siteNodeId);
 		
         if(siteNodeVO.getSiteNodeTypeDefinitionId() != null)
         {
 	        this.siteNodeTypeDefinitionVO = SiteNodeTypeDefinitionController.getSiteNodeTypeDefinitionVOWithId(siteNodeVO.getSiteNodeTypeDefinitionId());
 			this.availableServiceBindings = SiteNodeTypeDefinitionController.getAvailableServiceBindingVOList(siteNodeVO.getSiteNodeTypeDefinitionId());
 			this.serviceBindings = SiteNodeVersionController.getServiceBindningVOList(siteNodeVersionVO.getSiteNodeVersionId());
 		}
     } 
 */
 
     public String doExecute() throws Exception
     {
     	String result = "success";
     	
         Database db = CastorDatabaseService.getDatabase();
 		
 		beginTransaction(db);
 
 		try
 		{
 			if(getSiteNodeId() != null)
 			{	
 	        	this.initialize(getSiteNodeId(), db);
 
 	            if((this.stay == null || !this.stay.equalsIgnoreCase("true")) && this.siteNodeVO.getSiteNodeTypeDefinitionId() != null && this.siteNodeVersionVO.getStateId().intValue() == SiteNodeVersionVO.WORKING_STATE.intValue() && getShowComponentsFirst().equalsIgnoreCase("true"))
 		        {
 	                boolean isMetaInfoInWorkingState = false;
 	    			LanguageVO masterLanguageVO = LanguageController.getController().getMasterLanguage(this.repositoryId, db);
 	    			Integer languageId = masterLanguageVO.getLanguageId();
 
 	    			AvailableServiceBindingVO availableServiceBindingVO = AvailableServiceBindingController.getController().getAvailableServiceBindingVOWithName("Meta information");
 	    			Integer metaInfoAvailableServiceBindingId = null;
 	    			if(availableServiceBindingVO != null)
 	    			    metaInfoAvailableServiceBindingId = availableServiceBindingVO.getAvailableServiceBindingId();
 	    			
 	    			Integer metaInfoContentId = null;
 	    			
 	    			List serviceBindings = SiteNodeVersionController.getServiceBindningVOList(this.siteNodeVersionVO.getId(), db);
 	    			Iterator serviceBindingIterator = serviceBindings.iterator();
 	    			while(serviceBindingIterator.hasNext())
 	    			{
 	    				ServiceBindingVO serviceBindingVO = (ServiceBindingVO)serviceBindingIterator.next();
 	    				if(serviceBindingVO.getAvailableServiceBindingId().intValue() == metaInfoAvailableServiceBindingId.intValue())
 	    				{
 	    					List boundContents = ContentController.getInTransactionBoundContents(db, serviceBindingVO.getServiceBindingId()); 			
 	    					if(boundContents.size() > 0)
 	    	    			{
 	    	    				ContentVO contentVO = (ContentVO)boundContents.get(0);
 	    	    				metaInfoContentId = contentVO.getId();
 	    	    				ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentVO.getId(), languageId, db);
 	    	    				if(contentVersionVO != null && contentVersionVO.getStateId().equals(ContentVersionVO.WORKING_STATE))
 	    	    					isMetaInfoInWorkingState = true;
 	
 	    	    				break;
 	    	    			}                					
 	    				}
 	    			}
 	
 	    			if(this.siteNodeVO.getMetaInfoContentId() == null || this.siteNodeVO.getMetaInfoContentId().intValue() == -1)
 	    			    SiteNodeController.getController().setMetaInfoContentId(this.siteNodeVO.getId(), metaInfoContentId, db);
 	    			    
 	    			if(isMetaInfoInWorkingState)
 	    			{
 	    			    String url = getComponentRendererUrl() + getComponentRendererAction() + "?siteNodeId=" + getSiteNodeId() + "&languageId=" + masterLanguageVO.getId() + "&contentId=-1&cmsUserName=" + URLEncoder.encode(this.getInfoGluePrincipal().getName(), "UTF-8");
 	    			    url = this.getResponse().encodeURL(url);
 	    				this.getResponse().sendRedirect(url);
 	    				result = NONE;
 	    			}
 	    			else
 	    				result = "success";
 		            
 	    			//if(this.repositoryId == null)
 		            //    this.repositoryId = contentVO.getRepositoryId();
 		            
 		            //this.languageId = getMasterLanguageVO().getId();
 		            //return "viewVersion";
 		        }
 		        else
 		        {
 		            this.initializeSiteNodeCover(getSiteNodeId(), db);
 		            
 		            result = "success";
 		        }
 			}
 			else
 			{
 				result = "blank";
 			}
 	        
 	        commitTransaction(db);
 	    }
 		catch(Exception e)
 		{
 			getLogger().error("An error occurred so we should not complete the transaction:" + e, e);
 			rollbackTransaction(db);
 			throw new SystemException(e.getMessage());
 		}
 		
 		return result;
     }
     
     public String doChangeState() throws Exception
     {
     	getLogger().info("Gonna change state with comment:" + this.siteNodeVersionVO.getVersionComment());
 
     	Database db = CastorDatabaseService.getDatabase();
 		
 		beginTransaction(db);
 
 		try
 		{
 			SiteNodeVersionController.getController().updateStateId(this.siteNodeVersionVO.getSiteNodeVersionId(), getStateId(), this.siteNodeVersionVO.getVersionComment(), this.getInfoGluePrincipal(), this.getSiteNodeId());
 			this.initialize(getSiteNodeId(), db);
 
 			commitTransaction(db);
 	    }
 		catch(Exception e)
 		{
 			getLogger().error("An error occurred so we should not complete the transaction:" + e, e);
 			rollbackTransaction(db);
 			throw new SystemException(e.getMessage());
 		}
         
 		return "success";
     }
         
     public String doCommentVersion() throws Exception
     { 
     	getLogger().info("Gonna show the comment-view");
         return "commentVersion";
     }
         	
     public java.lang.Integer getSiteNodeId()
     {
         return this.siteNodeVO.getSiteNodeId();
     }
         
     public boolean getIsSiteNodeTypeDefinitionAssigned()
     {
         return (this.siteNodeVO.getSiteNodeTypeDefinitionId() != null) ? true : false;
     }
  
     public void setSiteNodeId(java.lang.Integer siteNodeId)
     {
 	    this.siteNodeVO.setSiteNodeId(siteNodeId);
     }
     
     public java.lang.Integer getRepositoryId()
     {
     	if(this.repositoryId != null)
 	        return this.repositoryId;
     	else
     		return this.siteNodeVO.getRepositoryId();
     }
         
     public void setRepositoryId(java.lang.Integer repositoryId)
     {
 	    this.repositoryId = repositoryId;
     }
     
     public java.lang.Integer getUnrefreshedSiteNodeId()
     {
         return this.unrefreshedSiteNodeId;
     }
         
     public void setUnrefreshedSiteNodeId(java.lang.Integer unrefreshedSiteNodeId)
     {
 	    this.unrefreshedSiteNodeId = unrefreshedSiteNodeId;
     }
 
     public java.lang.Integer getChangeTypeId()
     {
         return this.changeTypeId;
     }
         
     public void setChangeTypeId(java.lang.Integer changeTypeId)
     {
 	    this.changeTypeId = changeTypeId;
     }
     
     public String getName()
     {
         return this.siteNodeVO.getName();
     }
 
     public String getPublishDateTime()
     {    		
         return new VisualFormatter().formatDate(this.siteNodeVO.getPublishDateTime(), "yyyy-MM-dd HH:mm");
     }
         
     public String getExpireDateTime()
     {
         return new VisualFormatter().formatDate(this.siteNodeVO.getExpireDateTime(), "yyyy-MM-dd HH:mm");
     }
 
    	public long getPublishDateTimeAsLong()
     {    		
         return this.siteNodeVO.getPublishDateTime().getTime();
     }
         
     public long getExpireDateTimeAsLong()
     {
         return this.siteNodeVO.getExpireDateTime().getTime();
     }
     
 	public Boolean getIsBranch()
 	{
 		return this.siteNodeVO.getIsBranch();
 	}     
 
 	public String getContentType()
 	{
 		return this.siteNodeVersionVO.getContentType();
 	}
 
 	public void setContentType(String contentType)
 	{
 		this.siteNodeVersionVO.setContentType(contentType);
 	}
 
 	public String getPageCacheKey()
 	{
 		return this.siteNodeVersionVO.getPageCacheKey();
 	}
 
 	public void setPageCacheKey(String pageCacheKey)
 	{
 		this.siteNodeVersionVO.setPageCacheKey(pageCacheKey);
 	}
 
 	public Integer getDisableEditOnSight()
 	{
 		return this.siteNodeVersionVO.getDisableEditOnSight();
 	}
 
 	public void setDisableEditOnSight(Integer disableEditOnSight)
 	{
 		this.siteNodeVersionVO.setDisableEditOnSight(disableEditOnSight);
 	}
 
 	public Integer getDisableLanguages()
 	{
 		return this.siteNodeVersionVO.getDisableLanguages();
 	}
 
 	public void setDisableLanguages(Integer disableLanguages)
 	{
 		this.siteNodeVersionVO.setDisableLanguages(disableLanguages);
 	}
 
 	public Integer getDisablePageCache()
 	{
 		return this.siteNodeVersionVO.getDisablePageCache();
 	}
 
 	public void setDisablePageCache(Integer disablePageCache)
 	{
 		this.siteNodeVersionVO.setDisablePageCache(disablePageCache);
 	}
 
 	public Integer getIsProtected()
 	{
 		return this.siteNodeVersionVO.getIsProtected();
 	}
 
 	public void setIsProtected(Integer isProtected)
 	{
 		this.siteNodeVersionVO.setIsProtected(isProtected);
 	}
 
 	public void setStateId(Integer stateId)
 	{
 		this.siteNodeVersionVO.setStateId(stateId);
 	}
 
 	public Integer getStateId()
 	{
 		return this.siteNodeVersionVO.getStateId();
 	}
 	
 	public SiteNodeVersionVO getSiteNodeVersion()
 	{
 		return this.siteNodeVersionVO;
 	}	
 
 	public Integer getSiteNodeVersionId()
 	{
 		return this.siteNodeVersionVO.getSiteNodeVersionId();
 	}	
 
 	public void setSiteNodeVersionId(Integer siteNodeVersionId)
 	{
 		this.siteNodeVersionVO.setSiteNodeVersionId(siteNodeVersionId);
 	}	
 
 	public void setVersionComment(String versionComment)
 	{
 		this.siteNodeVersionVO.setVersionComment(versionComment);
 	}
 	
 	public String getVersionComment()
 	{
 		return this.siteNodeVersionVO.getVersionComment();
 	}
 
 	public SiteNodeTypeDefinitionVO getSiteNodeTypeDefinition()
 	{
 		return this.siteNodeTypeDefinitionVO;
 	}	
 
 	public List getAvailableServiceBindings()
 	{
 		return this.availableServiceBindings;
 	}	
 	
 	public String getShowComponentsFirst()
 	{
 	    return CmsPropertyHandler.getProperty("showComponentsFirst");
 	}
 	
 	/**
 	 * This method sorts a list of available service bindings on the name of the binding.
 	 */
 	
 	public List getSortedAvailableServiceBindings()
 	{
 		List sortedAvailableServiceBindings = new ArrayList();
 		
 		Iterator iterator = this.availableServiceBindings.iterator();
 		while(iterator.hasNext())
 		{
 			AvailableServiceBindingVO availableServiceBinding = (AvailableServiceBindingVO)iterator.next();
 			int index = 0;
 			Iterator sortedListIterator = sortedAvailableServiceBindings.iterator();
 			while(sortedListIterator.hasNext())
 			{
 				AvailableServiceBindingVO sortedAvailableServiceBinding = (AvailableServiceBindingVO)sortedListIterator.next();
 				
 				String currentAttribute = availableServiceBinding.getName();
 				String sortedAttribute  = sortedAvailableServiceBinding.getName();
 				
 				if(currentAttribute != null && sortedAttribute != null && currentAttribute.compareTo(sortedAttribute) < 0)
 		    	{
 		    		break;
 		    	}
 		    	index++;
 			}
 			sortedAvailableServiceBindings.add(index, availableServiceBinding);
 		}
 			
 		return sortedAvailableServiceBindings;
 	}
 	
 	/**
 	 * This method sorts a list of available service bindings on the name of the binding.
 	 */
 
 	public List getSortedAvailableContentServiceBindings()
 	{
 		List sortedAvailableContentServiceBindings = new ArrayList();
 		
 		Iterator sortedListIterator = getSortedAvailableServiceBindings().iterator();
 		while(sortedListIterator.hasNext())
 		{
 			AvailableServiceBindingVO sortedAvailableServiceBinding = (AvailableServiceBindingVO)sortedListIterator.next();
 			if(sortedAvailableServiceBinding.getVisualizationAction().indexOf("Structure") == -1)
 				sortedAvailableContentServiceBindings.add(sortedAvailableServiceBinding);
 		}
 			
 		return sortedAvailableContentServiceBindings;
 	}
 
 	/**
 	 * This method sorts a list of available service bindings on the name of the binding.
 	 */
 
 	public List getSortedAvailableStructureServiceBindings()
 	{
 		List sortedAvailableStructureServiceBindings = new ArrayList();
 		
 		Iterator sortedListIterator = getSortedAvailableServiceBindings().iterator();
 		while(sortedListIterator.hasNext())
 		{
 			AvailableServiceBindingVO sortedAvailableServiceBinding = (AvailableServiceBindingVO)sortedListIterator.next();
 			if(sortedAvailableServiceBinding.getVisualizationAction().indexOf("Structure") > -1)
 				sortedAvailableStructureServiceBindings.add(sortedAvailableServiceBinding);
 		}
 			
 		return sortedAvailableStructureServiceBindings;
 	}
 
 	
 	public List getServiceBindings()
 	{
 		return this.serviceBindings;
 	}	
 	
 	public String getStateDescription(Integer siteNodeId, Integer languageId)
 	{
 		String stateDescription = "Not created";
 		/*
 		try
 		{
 			SiteNodeVersionVO siteNodeVersionVO = SiteNodeVersionController.getLatestSiteNodeVersionVO(siteNodeId, languageId);
 			Integer stateId = siteNodeVersionVO.getStateId();
 			if(stateId.intValue() == 0)
 				stateDescription = "Working";
 			else if(stateId.intValue() == 2)
 				stateDescription = "Publish";
 		}
 		catch(Exception e)
 		{
 			//e.printStackTrace();
 		}
 		*/
 		return stateDescription;
 	}
 	
 	
 	/**
 	 * This method fetches a description of the qualifyer.
 	 */
 	
 	public String getQualifyerDescription(Integer serviceBindingId) throws Exception
 	{
 		String qualifyerDescription = "";
 		
 		List qualifyers = ServiceBindingController.getQualifyerVOList(serviceBindingId);
 		Iterator i = qualifyers.iterator();
 		while(i.hasNext())
 		{
 			QualifyerVO qualifyerVO = (QualifyerVO)i.next();
 			if(!qualifyerDescription.equalsIgnoreCase(""))
 				qualifyerDescription += ",";
 				
 			qualifyerDescription += qualifyerVO.getName() + "=" + qualifyerVO.getValue();
 		}
 		
 		return qualifyerDescription;
 	}
 	
 	public List getListPreparedQualifyers(Integer serviceBindingId) throws Exception
 	{
 		List qualifyers = ServiceBindingController.getQualifyerVOList(serviceBindingId);
 		Iterator i = qualifyers.iterator();
 		while(i.hasNext())
 		{
 			QualifyerVO qualifyerVO = (QualifyerVO)i.next();
 			if(qualifyerVO.getName().equalsIgnoreCase("contentid"))
 			{
 			    try {
 			        ContentVO contentVO = ContentControllerProxy.getController().getACContentVOWithId(this.getInfoGluePrincipal(), new Integer(qualifyerVO.getValue()));
 			        qualifyerVO.setPath(contentVO.getName());
 			    }
 			    catch(Exception e)
 			    {
 			    }
 			}
 		}
 		return qualifyers;
 	}
 	
 	/**
 	 * This method fetches the list of SiteNodeTypeDefinitions
 	 */
 	
 	public List getSiteNodeTypeDefinitions() throws Exception
 	{
 		return SiteNodeTypeDefinitionController.getController().getSiteNodeTypeDefinitionVOList();
 	}      
     
     
     public EventVO getSiteNodeVersionEvent(Integer siteNodeVersionId)
 	{
     	EventVO eventVO = null;
     	
 		try
 		{
 			List events = EventController.getEventVOListForEntity(SiteNodeVersion.class.getName(), siteNodeVersionId);
 			if(events != null && events.size() > 0)
 				eventVO = (EventVO)events.get(0);
 		}
 		catch(Exception e)
 		{
 			getLogger().error("An error occurred when we tried to get any events for this version:" + e.getMessage(), e);
 		}
 
 		return eventVO;
 	}
 
 	public EventVO getSiteNodeEvent(Integer siteNodeId)
 	{
 		EventVO eventVO = null;
 		try
 		{
 			List events = EventController.getEventVOListForEntity(SiteNode.class.getName(), siteNodeId);
 			if(events != null && events.size() > 0)
 				eventVO = (EventVO)events.get(0);
 		}
 		catch(Exception e)
 		{
 			getLogger().error("An error occurred when we tried to get any events for this siteNode:" + e.getMessage(), e);
 		}
 		
 		return eventVO;
 	}
 	
 	public SiteNodeVersionVO getSiteNodeVersionVO()
 	{
 		return siteNodeVersionVO;
 	}
 
     public String getStay()
     {
         return stay;
     }
     
     public void setStay(String stay)
     {
         this.stay = stay;
     }
     
     public String getDest()
     {
         return dest;
     }
     
     public List getReferenceBeanList()
     {
         return referenceBeanList;
     }
     
     public List getAvailableLanguages()
     {
         return availableLanguages;
     }
     
     public List getDisabledLanguages()
     {
         return disabledLanguages;
     }
     
     public List getReferencingBeanList()
     {
         return referencingBeanList;
     }
 }
