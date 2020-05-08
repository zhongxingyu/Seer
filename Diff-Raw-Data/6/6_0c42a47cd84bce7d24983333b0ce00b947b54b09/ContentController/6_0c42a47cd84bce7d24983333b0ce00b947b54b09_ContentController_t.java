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
 
 package org.infoglue.cms.controllers.kernel.impl.simple;
 
 import org.exolab.castor.jdo.Database;
 
 import org.infoglue.cms.applications.contenttool.wizards.actions.CreateContentWizardInfoBean;
 import org.infoglue.cms.entities.kernel.*;
 import org.infoglue.cms.entities.content.*;
 import org.infoglue.cms.entities.content.ContentVO;
 import org.infoglue.cms.entities.management.*;
 import org.infoglue.cms.entities.management.impl.simple.*;
 import org.infoglue.cms.entities.management.ContentTypeDefinition;
 import org.infoglue.cms.entities.content.impl.simple.ContentImpl;
 import org.infoglue.cms.entities.content.impl.simple.MediumContentImpl;
 import org.infoglue.cms.entities.content.impl.simple.SmallContentImpl;
 import org.infoglue.cms.entities.structure.*;
 
 import org.infoglue.cms.exception.Bug;
 import org.infoglue.cms.exception.ConstraintException;
 import org.infoglue.cms.exception.SystemException;
 import org.infoglue.cms.util.ConstraintExceptionBuffer;
 import org.infoglue.cms.util.CmsLogger;
 import org.infoglue.cms.services.*;
 import org.infoglue.deliver.util.CacheController;
 
 
 import org.exolab.castor.jdo.OQLQuery;
 import org.exolab.castor.jdo.QueryResults;
 
 import java.util.List;
 import java.util.Iterator;
 import java.util.Collection;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 /**
  * @author Mattias Bogeblad
  */
 
 public class ContentController extends BaseController 
 {
 	/**
 	 * Factory method
 	 */
 	
 	public static ContentController getContentController()
 	{
 		return new ContentController();
 	}
 
 	public ContentVO getContentVOWithId(Integer contentId) throws SystemException, Bug
     {
     	return (ContentVO) getVOWithId(SmallContentImpl.class, contentId);
     } 
 
 	public ContentVO getSmallContentVOWithId(Integer contentId, Database db) throws SystemException, Bug
     {
     	return (ContentVO) getVOWithId(SmallContentImpl.class, contentId, db);
     } 
 
     public Content getContentWithId(Integer contentId, Database db) throws SystemException, Bug
     {
 		return (Content) getObjectWithId(ContentImpl.class, contentId, db);
     }
 
     public List getContentVOList() throws SystemException, Bug
     {
         return getAllVOObjects(ContentImpl.class, "contentId");
     }
 	
 	/**
 	 * This method finishes what the create content wizard initiated and resulted in.
 	 */
 	
 	public ContentVO create(CreateContentWizardInfoBean createContentWizardInfoBean) throws ConstraintException, SystemException
 	{
 		Database db = CastorDatabaseService.getDatabase();
 		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
 
 		Content content = null;
 
 		beginTransaction(db);
 
 		try
 		{
 			content = create(db, createContentWizardInfoBean.getParentContentId(), createContentWizardInfoBean.getContentTypeDefinitionId(), createContentWizardInfoBean.getRepositoryId(), createContentWizardInfoBean.getContent().getValueObject());
 			
 			Iterator it = createContentWizardInfoBean.getContentVersions().keySet().iterator();
 			while (it.hasNext()) 
 			{
 				Integer languageId = (Integer)it.next();
 				CmsLogger.logInfo("languageId:" + languageId);
 				ContentVersionVO contentVersionVO = (ContentVersionVO)createContentWizardInfoBean.getContentVersions().get(languageId);
 				ContentVersionController.getContentVersionController().create(content.getContentId(), languageId, contentVersionVO, null, db);
 			}
 			
 			//Bind if needed?
 			
 			ceb.throwIfNotEmpty();
             
 			commitTransaction(db);	
 		}
 		catch(ConstraintException ce)
 		{
 			CmsLogger.logWarning("An error occurred so we should not complete the transaction:" + ce, ce);
 			rollbackTransaction(db);
 			throw ce;
 		}
 		catch(Exception e)
 		{
 			CmsLogger.logSevere("An error occurred so we should not complete the transaction:" + e, e);
 			rollbackTransaction(db);
 			throw new SystemException(e.getMessage());
 		}
 
 		return content.getValueObject();
 	}
 	
 	/**
 	 * This method creates a new content-entity and references the entities it should know about.
 	 * As castor is lousy at this in my opinion we also add the new entity to the surrounding entities.
 	 */
 	
     public ContentVO create(Integer parentContentId, Integer contentTypeDefinitionId, Integer repositoryId, ContentVO contentVO) throws ConstraintException, SystemException
     {
         Database db = CastorDatabaseService.getDatabase();
         ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
 
         Content content = null;
 
         beginTransaction(db);
 
         try
         {
             content = create(db, parentContentId, contentTypeDefinitionId, repositoryId, contentVO);
             ceb.throwIfNotEmpty();
             
             commitTransaction(db);	
         }
         catch(ConstraintException ce)
         {
             CmsLogger.logWarning("An error occurred so we should not complete the transaction:" + ce, ce);
             rollbackTransaction(db);
             throw ce;
         }
         catch(Exception e)
         {
             CmsLogger.logSevere("An error occurred so we should not complete the transaction:" + e, e);
             rollbackTransaction(db);
             throw new SystemException(e.getMessage());
         }
 
         return content.getValueObject();
     }
 
 	/**
 	 * This method creates a new content-entity and references the entities it should know about.
 	 * As castor is lousy at this in my opinion we also add the new entity to the surrounding entities.
 	 */
 	    
     public Content create(Database db, Integer parentContentId, Integer contentTypeDefinitionId, Integer repositoryId, ContentVO contentVO) throws ConstraintException, SystemException, Exception
     {
 	    Content content = null;
 		
         try
         {            
             Content parentContent = null;
           	ContentTypeDefinition contentTypeDefinition = null;
 
             if(parentContentId != null)
             {
             	parentContent = getContentWithId(parentContentId, db);
            	
             	if(repositoryId == null)
 					repositoryId = parentContent.getRepository().getRepositoryId();	
             }
             
             if(contentTypeDefinitionId != null)
             	contentTypeDefinition = ContentTypeDefinitionController.getController().getContentTypeDefinitionWithId(contentTypeDefinitionId, db);
 
             Repository repository = RepositoryController.getController().getRepositoryWithId(repositoryId, db);
 			
             content = new ContentImpl();
             content.setValueObject(contentVO);
             content.setParentContent((ContentImpl)parentContent);
             content.setRepository((RepositoryImpl)repository);
             content.setContentTypeDefinition((ContentTypeDefinitionImpl)contentTypeDefinition);
             
 			db.create(content);
 			
 			//Now we add the content to the knowledge of the related entities.
 			if(parentContent != null)
			{
 				parentContent.getChildren().add(content);
				parentContent.setIsBranch(new Boolean(true));
			}
 			
 			//repository.getContents().add(content);			
         }
         catch(Exception e)
         {
         	CmsLogger.logSevere("An error occurred so we should not complete the transaction:" + e, e);
         	e.printStackTrace();
         	//rollbackTransaction(db);
             throw new SystemException(e.getMessage());    
         }
         
         return content;
     }
        
 
 	/**
 	 * This method deletes a content and also erases all the children and all versions.
 	 */
 	    
     public void delete(ContentVO contentVO) throws ConstraintException, SystemException
     {
     	Database db = CastorDatabaseService.getDatabase();
         beginTransaction(db);
 		try
         {		
 	    	delete(contentVO, db);
 	    	
 	    	commitTransaction(db);
         }
         catch(ConstraintException ce)
         {
         	CmsLogger.logSevere("An error occurred so we should not complete the transaction:" + ce, ce);
             rollbackTransaction(db);
             throw ce;
         }
         catch(Exception e)
         {
             CmsLogger.logSevere("An error occurred so we should not complete the transaction:" + e, e);
             rollbackTransaction(db);
             throw new SystemException(e.getMessage());
         }
 
     }  
     
     
 	/**
 	 * This method deletes a content and also erases all the children and all versions.
 	 */
 	    
 	public void delete(ContentVO contentVO, Database db) throws ConstraintException, SystemException, Exception
 	{
 		Content content = getContentWithId(contentVO.getContentId(), db);
 	    Content parent = content.getParentContent();
 	    if(parent != null)
 		{
 			Iterator childContentIterator = parent.getChildren().iterator();
 			while(childContentIterator.hasNext())
 			{
 			    Content candidate = (Content)childContentIterator.next();
 			    if(candidate.getId().equals(contentVO.getContentId()))
 			    {
 			        deleteRecursive(content, childContentIterator, db);
 			    }
 			}
 		}
 		else
 		{
 		    deleteRecursive(content, null, db);
 		}
 	}        
 
 	/**
 	 * Recursively deletes all contents and their versions. Also updates related entities about the change.
 	 */
 	
     private static void deleteRecursive(Content content, Iterator parentIterator, Database db) throws ConstraintException, SystemException, Exception
     {
 		//Content parent = content.getParentContent();
 		//Repository repository = content.getRepository();
 		
         Collection children = content.getChildren();
 		Iterator childrenIterator = children.iterator();
 		while(childrenIterator.hasNext())
 		{
 			Content childContent = (Content)childrenIterator.next();
 			deleteRecursive(childContent, childrenIterator, db);   			
    		}
 		content.setChildren(new ArrayList());
    		
    		if(getIsDeletable(content))
 	    {		 
 			ContentVersionController.getContentVersionController().deleteVersionsForContent(content, db);    	
 			
 			ServiceBindingController.deleteServiceBindingsReferencingContent(content, db);
 			
 			if(parentIterator != null) 
 			    parentIterator.remove();
 	    	
 	    	db.remove(content);
 	    }
 	    else
     	{
     		throw new ConstraintException("ContentVersion.stateId", "3300");
     	}			
     }        
 
 	
 	/**
 	 * This method returns true if the content does not have any published contentversions or 
 	 * are restricted in any other way.
 	 */
 	
 	private static boolean getIsDeletable(Content content)
 	{
 		boolean isDeletable = true;
 	
         Collection contentVersions = content.getContentVersions();
     	Iterator versionIterator = contentVersions.iterator();
 		while (versionIterator.hasNext()) 
         {
         	ContentVersion contentVersion = (ContentVersion)versionIterator.next();
         	if(contentVersion.getStateId().intValue() == ContentVersionVO.PUBLISHED_STATE.intValue() && contentVersion.getIsActive().booleanValue() == true)
         	{
         		CmsLogger.logInfo("The content had a published version so we cannot delete it..");
 				isDeletable = false;
         		break;
         	}
 	    }		
 			
 		return isDeletable;	
 	}
 
 	
     public ContentVO update(ContentVO contentVO) throws ConstraintException, SystemException
     {
         return update(contentVO, null);
     }        
 
 
     public ContentVO update(ContentVO contentVO, Integer contentTypeDefinitionId) throws ConstraintException, SystemException
     {
         Database db = CastorDatabaseService.getDatabase();
 
         Content content = null;
 
         beginTransaction(db);
 
         try
         {
             content = (Content)getObjectWithId(ContentImpl.class, contentVO.getId(), db);
             content.setVO(contentVO);
             
             if(contentTypeDefinitionId != null)
             {
                 ContentTypeDefinition contentTypeDefinition = ContentTypeDefinitionController.getController().getContentTypeDefinitionWithId(contentTypeDefinitionId, db);
                 content.setContentTypeDefinition((ContentTypeDefinitionImpl)contentTypeDefinition);
             }
             
             commitTransaction(db);
         }
         catch(Exception e)
         {
             CmsLogger.logSevere("An error occurred so we should not complete the transaction:" + e, e);
             rollbackTransaction(db);
             throw new SystemException(e.getMessage());
         }
 
         return content.getValueObject();
     }        
 
 	public List getAvailableLanguagesForContentWithId(Integer contentId, Database db) throws ConstraintException, SystemException, Exception
 	{
 		List availableLanguageVOList = new ArrayList();
 		
 		Content content = getContentWithId(contentId, db);
 		if(content != null)
 		{
 			Repository repository = content.getRepository();
 			if(repository != null)
 			{
 			    List availableRepositoryLanguageList = RepositoryLanguageController.getController().getRepositoryLanguageListWithRepositoryId(repository.getId(), db);
 				Iterator i = availableRepositoryLanguageList.iterator();
 				while(i.hasNext())
 				{
 					RepositoryLanguage repositoryLanguage = (RepositoryLanguage)i.next();
 					availableLanguageVOList.add(repositoryLanguage.getLanguage().getValueObject());
 				}
 			}
 		}
 		
 		return availableLanguageVOList;
 	}
 /*
 	public List getAvailableLanguagesForContentWithId(Integer contentId, Database db) throws ConstraintException, SystemException
 	{
 		List availableLanguageVOList = new ArrayList();
 		
 		Content content = getContentWithId(contentId, db);
 		if(content != null)
 		{
 			Repository repository = content.getRepository();
 			if(repository != null)
 			{
 				Collection availableLanguages = repository.getRepositoryLanguages();
 				Iterator i = availableLanguages.iterator();
 				while(i.hasNext())
 				{
 					RepositoryLanguage repositoryLanguage = (RepositoryLanguage)i.next();
 					
 					int position = 0;
 					Iterator availableLanguageVOListIterator = availableLanguageVOList.iterator();
 					while(availableLanguageVOListIterator.hasNext())
 					{
 						LanguageVO availableLanguageVO = (LanguageVO)availableLanguageVOListIterator.next();
 						if(repositoryLanguage.getLanguage().getValueObject().getId().intValue() < availableLanguageVO.getId().intValue())
 							break; 
 						
 						position++;
 					}
 					
 					availableLanguageVOList.add(position, repositoryLanguage.getLanguage().getValueObject());
 				}
 			}
 		}
 		
 		return availableLanguageVOList;
 	}
 */
 	
 	/**
 	 * This method returns the value-object of the parent of a specific content. 
 	 */
 	
     public static ContentVO getParentContent(Integer contentId) throws SystemException, Bug
     {
     	CmsLogger.logInfo("Coming in with:" + contentId);
         Database db = CastorDatabaseService.getDatabase();
 		ContentVO parentContentVO = null;
 		
         beginTransaction(db);
 
         try
         {
 			Content content = (Content) getObjectWithId(ContentImpl.class, contentId, db);
 			CmsLogger.logInfo("CONTENT:" + content.getName());
 			Content parent = content.getParentContent();
 			if(parent != null)
 				parentContentVO = parent.getValueObject();
             
             commitTransaction(db);
         }
         catch(Exception e)
         {
             CmsLogger.logSevere("An error occurred so we should not complete the transaction:" + e, e);
             rollbackTransaction(db);
             throw new SystemException(e.getMessage());
         }
 		return parentContentVO;    	
     }
 
 
 	public static void addChildContent(ContentVO parentVO, ContentVO childVO)
 		throws ConstraintException, SystemException
 	{
 
         Database db = CastorDatabaseService.getDatabase();
         ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
 
         beginTransaction(db);
 
         try
         {
 			Content parent = (Content) getObjectWithId(ContentImpl.class, parentVO.getContentId(), db);
 			Content child = (Content) getObjectWithId(ContentImpl.class, childVO.getContentId(), db);
 			parent.getChildren().add(child);
 
             ceb.throwIfNotEmpty();            
             commitTransaction(db);
         }
         catch(ConstraintException ce)
         {
             CmsLogger.logWarning("An error occurred so we should not complete the transaction:" + ce, ce);
             rollbackTransaction(db);
             throw ce;
         }
         catch(Exception e)
         {
             CmsLogger.logSevere("An error occurred so we should not complete the transaction:" + e, e);
             rollbackTransaction(db);
             throw new SystemException(e.getMessage());
         }
 		
 	}
 
 	public static void removeChildContent(ContentVO parentVO, ContentVO childVO)
 		throws ConstraintException, SystemException
 	{
 
         Database db = CastorDatabaseService.getDatabase();
         ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
 
         beginTransaction(db);
 
         try
         {
 			Content parent = (Content) getObjectWithId(ContentImpl.class, parentVO.getContentId(), db);
 			Content child = (Content) getObjectWithId(ContentImpl.class, childVO.getContentId(), db);
 			parent.getChildren().remove(child);
 
             ceb.throwIfNotEmpty();            
             commitTransaction(db);
         }
         catch(ConstraintException ce)
         {
             CmsLogger.logWarning("An error occurred so we should not complete the transaction:" + ce, ce);
             rollbackTransaction(db);
             throw ce;
         }
         catch(Exception e)
         {
             CmsLogger.logSevere("An error occurred so we should not complete the transaction:" + e, e);
             rollbackTransaction(db);
             throw new SystemException(e.getMessage());
         }
 		
 	}
 
 	
 
 	/**
 	 * This method moves a content from one parent-content to another. First we check so no illegal actions are 
 	 * in process. For example the target folder must not be the item to be moved or a child to the item.
 	 * Such actions would result in model-errors.
 	 */
 		
 	public void moveContent(ContentVO contentVO, Integer newParentContentId) throws ConstraintException, SystemException
     {
         Database db = CastorDatabaseService.getDatabase();
         ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
 
         Content content = null;
 		Content newParentContent = null;
 		Content oldParentContent = null;
 
         beginTransaction(db);
 
         try
         {
             //Validation that checks the entire object
             contentVO.validate();
 			
 			if(newParentContentId == null)
             {
             	CmsLogger.logWarning("You must specify the new parent-content......");
             	throw new ConstraintException("Content.parentContentId", "3303");
             }
 
             if(contentVO.getId().intValue() == newParentContentId.intValue())
             {
             	CmsLogger.logWarning("You cannot have the content as it's own parent......");
             	throw new ConstraintException("Content.parentContentId", "3301");
             }
 			
 			content          = getContentWithId(contentVO.getContentId(), db);
             oldParentContent = content.getParentContent();
             newParentContent = getContentWithId(newParentContentId, db);
                         
             if(oldParentContent.getId().intValue() == newParentContentId.intValue())
             {
             	CmsLogger.logWarning("You cannot specify the same folder as it originally was located in......");
             	throw new ConstraintException("Content.parentContentId", "3304");
             }
 
 			Content tempContent = newParentContent.getParentContent();
 			while(tempContent != null)
 			{
 				if(tempContent.getId().intValue() == content.getId().intValue())
 				{
 					CmsLogger.logWarning("You cannot move the content to a child under it......");
             		throw new ConstraintException("Content.parentContentId", "3302");
 				}
 				tempContent = tempContent.getParentContent();
 			}				            
             
             oldParentContent.getChildren().remove(content);
             content.setParentContent((ContentImpl)newParentContent);
             newParentContent.getChildren().add(content);
             
             //If any of the validations or setMethods reported an error, we throw them up now before create.
             ceb.throwIfNotEmpty();
             
             commitTransaction(db);
         }
         catch(ConstraintException ce)
         {
             CmsLogger.logWarning("An error occurred so we should not complete the transaction:" + ce, ce);
             rollbackTransaction(db);
             throw ce;
         }
         catch(Exception e)
         {
             CmsLogger.logSevere("An error occurred so we should not complete the transaction:" + e, e);
             rollbackTransaction(db);
             throw new SystemException(e.getMessage());
         }
 
     }   
     
 	/**
 	 * This method is sort of a sql-query-like method where you can send in arguments in form of a list
 	 * of things that should match. The input is a Hashmap with a method and a List of HashMaps.
 	 */
 	
     public List getContentVOList(HashMap argumentHashMap) throws SystemException, Bug
     {
     	List contents = null;
     	
     	String method = (String)argumentHashMap.get("method");
     	CmsLogger.logInfo("method:" + method);
     	
     	if(method.equalsIgnoreCase("selectContentListOnIdList"))
     	{
 			contents = new ArrayList();
 			List arguments = (List)argumentHashMap.get("arguments");
 			CmsLogger.logInfo("Arguments:" + arguments.size());  
 			Iterator argumentIterator = arguments.iterator();
 			while(argumentIterator.hasNext())
 			{ 		
 				HashMap argument = (HashMap)argumentIterator.next(); 
 				Integer contentId = new Integer((String)argument.get("contentId"));
 				CmsLogger.logInfo("Getting the content with Id:" + contentId);
 				contents.add(getContentVOWithId(contentId));
 			}
     	}
         else if(method.equalsIgnoreCase("selectListOnContentTypeName"))
     	{
 			List arguments = (List)argumentHashMap.get("arguments");
 			CmsLogger.logInfo("Arguments:" + arguments.size());   		
 			contents = getContentVOListByContentTypeNames(arguments);
     	}
         return contents;
     }
 	
 	/**
 	 * This method is sort of a sql-query-like method where you can send in arguments in form of a list
 	 * of things that should match. The input is a Hashmap with a method and a List of HashMaps.
 	 */
 	
     public List getContentVOList(HashMap argumentHashMap, Database db) throws SystemException, Exception
     {
     	List contents = null;
     	
     	String method = (String)argumentHashMap.get("method");
     	CmsLogger.logInfo("method:" + method);
     	
     	if(method.equalsIgnoreCase("selectContentListOnIdList"))
     	{
 			contents = new ArrayList();
 			List arguments = (List)argumentHashMap.get("arguments");
 			CmsLogger.logInfo("Arguments:" + arguments.size());  
 			Iterator argumentIterator = arguments.iterator();
 			while(argumentIterator.hasNext())
 			{ 		
 				HashMap argument = (HashMap)argumentIterator.next(); 
 				Integer contentId = new Integer((String)argument.get("contentId"));
 				CmsLogger.logInfo("Getting the content with Id:" + contentId);
 				contents.add(getSmallContentVOWithId(contentId, db));
 			}
     	}
         else if(method.equalsIgnoreCase("selectListOnContentTypeName"))
     	{
 			List arguments = (List)argumentHashMap.get("arguments");
 			CmsLogger.logInfo("Arguments:" + arguments.size());   		
 			contents = getContentVOListByContentTypeNames(arguments, db);
     	}
         return contents;
     }
     
 
 	/**
 	 * The input is a list of hashmaps.
 	 */
 	
 	protected List getContentVOListByContentTypeNames(List arguments) throws SystemException, Bug
 	{
 		Database db = CastorDatabaseService.getDatabase();
 	
 		List contents = new ArrayList();
 		
         beginTransaction(db);
 
         try
         {
 			Iterator i = arguments.iterator();
 	    	while(i.hasNext())
 	    	{
 		        HashMap argument = (HashMap)i.next();
 	    		String contentTypeDefinitionName = (String)argument.get("contentTypeDefinitionName");
 				//OQLQuery oql = db.getOQLQuery("CALL SQL SELECT c.contentId, c.name, c.publishDateTime, c.expireDateTime, c.isBranch, c.isProtected, c.creator, ctd.contentTypeDefinitionId, r.repositoryId FROM cmContent c, cmContentTypeDefinition ctd, cmRepository r where c.repositoryId = r.repositoryId AND c.contentTypeDefinitionId = ctd.contentTypeDefinitionId AND ctd.name = $1 AS org.infoglue.cms.entities.content.impl.simple.SmallContentImpl");
 				//OQLQuery oql = db.getOQLQuery("CALL SQL SELECT contentId, name FROM cmContent c, cmContentTypeDefinition ctd WHERE c.contentTypeDefinitionId = ctd.contentTypeDefinitionId AND ctd.name = $1 AS org.infoglue.cms.entities.content.impl.simple.ContentImpl");
 	    		OQLQuery oql = db.getOQLQuery("SELECT c FROM org.infoglue.cms.entities.content.impl.simple.MediumContentImpl c WHERE c.contentTypeDefinition.name = $1");
 	        	oql.bind(contentTypeDefinitionName);
 	        	
 	        	QueryResults results = oql.execute(Database.ReadOnly);
 				
 				while(results.hasMore()) 
 	            {
 	            	MediumContentImpl content = (MediumContentImpl)results.next();
 					contents.add(content.getValueObject());
 	            }
 		   	}
             
             commitTransaction(db);
         }
         catch(Exception e)
         {
             CmsLogger.logSevere("An error occurred so we should not complete the transaction:" + e, e);
             rollbackTransaction(db);
             throw new SystemException(e.getMessage());
         }
 		
 		return contents;    	
 	}
 	
 	
 	/**
 	 * The input is a list of hashmaps.
 	 */
 	
 	protected List getContentVOListByContentTypeNames(List arguments, Database db) throws SystemException, Exception
 	{
 		List contents = new ArrayList();
 
 		Iterator i = arguments.iterator();
     	while(i.hasNext())
     	{
 	        HashMap argument = (HashMap)i.next();
     		String contentTypeDefinitionName = (String)argument.get("contentTypeDefinitionName");
 			//OQLQuery oql = db.getOQLQuery("CALL SQL SELECT c.contentId, c.name, c.publishDateTime, c.expireDateTime, c.isBranch, c.isProtected, c.creator, ctd.contentTypeDefinitionId, r.repositoryId FROM cmContent c, cmContentTypeDefinition ctd, cmRepository r where c.repositoryId = r.repositoryId AND c.contentTypeDefinitionId = ctd.contentTypeDefinitionId AND ctd.name = $1 AS org.infoglue.cms.entities.content.impl.simple.SmallContentImpl");
 			//OQLQuery oql = db.getOQLQuery("CALL SQL SELECT contentId, name FROM cmContent c, cmContentTypeDefinition ctd WHERE c.contentTypeDefinitionId = ctd.contentTypeDefinitionId AND ctd.name = $1 AS org.infoglue.cms.entities.content.impl.simple.ContentImpl");
     		OQLQuery oql = db.getOQLQuery("SELECT c FROM org.infoglue.cms.entities.content.impl.simple.MediumContentImpl c WHERE c.contentTypeDefinition.name = $1");
         	oql.bind(contentTypeDefinitionName);
         	
         	QueryResults results = oql.execute(Database.ReadOnly);
 			
 			while(results.hasMore()) 
             {
             	MediumContentImpl content = (MediumContentImpl)results.next();
 				contents.add(content.getValueObject());
             }
 	   	}
     	
 		return contents;    	
 	}
 
 	/**
 	 * The input is a list of hashmaps.
 	 */
 	/*
 	private static List getContentVOListByContentTypeNames(List arguments) throws SystemException, Bug
 	{
 		Database db = CastorDatabaseService.getDatabase();
 		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
 	
 		List contents = new ArrayList();
 		
 		beginTransaction(db);
 
 		try
 		{
 			Iterator i = arguments.iterator();
 			while(i.hasNext())
 			{
 				HashMap argument = (HashMap)i.next();
 				String contentTypeDefinitionName = (String)argument.get("contentTypeDefinitionName");
 	    		
 				OQLQuery oql = db.getOQLQuery("SELECT ctd FROM org.infoglue.cms.entities.management.impl.simple.ContentTypeDefinitionImpl ctd WHERE ctd.name = $1");
 				oql.bind(contentTypeDefinitionName);
 	        	
 				QueryResults results = oql.execute(Database.ReadOnly);
 				
 				if (results.hasMore()) 
 				{
 					ContentTypeDefinition contentTypeDefinition = (ContentTypeDefinition)results.next();
 					Collection contentList = contentTypeDefinition.getContents();
 					contents = toVOList(contentList);
 				}
 			}
             
 			commitTransaction(db);
 		}
 		catch(Exception e)
 		{
 			CmsLogger.logSevere("An error occurred so we should not complete the transaction:" + e, e);
 			rollbackTransaction(db);
 			throw new SystemException(e.getMessage());
 		}
 		
 		return contents;    	
 	}
 	*/
    	
    	/**
 	 * This method fetches the root content for a particular repository.
 	 * If there is no such content we create one as all repositories need one to work.
 	 */
 	        
    	public ContentVO getRootContentVO(Integer repositoryId, String userName) throws ConstraintException, SystemException
    	{
         Database db = CastorDatabaseService.getDatabase();
         ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
 
         Content content = null;
 
         beginTransaction(db);
 
         try
         {
             CmsLogger.logInfo("Fetching the root content for the repository " + repositoryId);
 			//OQLQuery oql = db.getOQLQuery( "SELECT c FROM org.infoglue.cms.entities.content.impl.simple.ContentImpl c WHERE is_undefined(c.parentContent) AND c.repository.repositoryId = $1");
 			OQLQuery oql = db.getOQLQuery( "SELECT c FROM org.infoglue.cms.entities.content.impl.simple.SmallContentImpl c WHERE is_undefined(c.parentContentId) AND c.repositoryId = $1");
 			oql.bind(repositoryId);
 			
         	QueryResults results = oql.execute(Database.ReadOnly);			
 			if (results.hasMore()) 
             {
             	content = (Content)results.next();
 	        }
             else
             {
 				//None found - we create it and give it the name of the repository.
 				CmsLogger.logInfo("Found no rootContent so we create a new....");
 				ContentVO rootContentVO = new ContentVO();
 				RepositoryVO repositoryVO = RepositoryController.getController().getRepositoryVOWithId(repositoryId);
 				rootContentVO.setCreatorName(userName);
 				rootContentVO.setName(repositoryVO.getName());
 				rootContentVO.setIsBranch(new Boolean(true));
             	content = create(db, null, null, repositoryId, rootContentVO);
             }
             
             //If any of the validations or setMethods reported an error, we throw them up now before create. 
             ceb.throwIfNotEmpty();
             commitTransaction(db);
             
         }
         catch(ConstraintException ce)
         {
             CmsLogger.logWarning("An error occurred so we should not complete the transaction:" + ce, ce);
             rollbackTransaction(db);
             throw ce;
         }
         catch(Exception e)
         {
             CmsLogger.logSevere("An error occurred so we should not complete the transaction:" + e, e);
             rollbackTransaction(db);
             throw new SystemException(e.getMessage());
         }
 
         return (content == null) ? null : content.getValueObject();
    	}
 
 
    	
 	/**
 	 * This method fetches the root content for a particular repository.
 	 * If there is no such content we create one as all repositories need one to work.
 	 */
 	        
 	public ContentVO getRootContentVO(Integer repositoryId, String userName, boolean createIfNonExisting) throws ConstraintException, SystemException
 	{
 		Database db = CastorDatabaseService.getDatabase();
 		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
 
 		Content content = null;
 
 		beginTransaction(db);
 
 		try
 		{
 		    content = getRootContent(db, repositoryId, userName, createIfNonExisting);
             
 			//If any of the validations or setMethods reported an error, we throw them up now before create. 
 			ceb.throwIfNotEmpty();
 			commitTransaction(db);
             
 		}
 		catch(ConstraintException ce)
 		{
 			CmsLogger.logWarning("An error occurred so we should not complete the transaction:" + ce, ce);
 			rollbackTransaction(db);
 			throw ce;
 		}
 		catch(Exception e)
 		{
 			CmsLogger.logSevere("An error occurred so we should not complete the transaction:" + e, e);
 			rollbackTransaction(db);
 			throw new SystemException(e.getMessage());
 		}
 
 		return (content == null) ? null : content.getValueObject();
 	}
    	
 	
 	/**
 	 * This method fetches the root content for a particular repository within a transaction.
 	 * If there is no such content we create one as all repositories need one to work.
 	 */
 	        
 	public Content getRootContent(Database db, Integer repositoryId, String userName, boolean createIfNonExisting) throws ConstraintException, SystemException, Exception
 	{
 		Content content = null;
 
 		CmsLogger.logInfo("Fetching the root content for the repository " + repositoryId);
 		OQLQuery oql = db.getOQLQuery( "SELECT c FROM org.infoglue.cms.entities.content.impl.simple.ContentImpl c WHERE is_undefined(c.parentContent) AND c.repository.repositoryId = $1");
 		oql.bind(repositoryId);
 			
 		QueryResults results = oql.execute(Database.ReadOnly);			
 		if (results.hasMore()) 
 		{
 			content = (Content)results.next();
 		}
 		else
 		{
 			if(createIfNonExisting)
 			{
 				//None found - we create it and give it the name of the repository.
 				CmsLogger.logInfo("Found no rootContent so we create a new....");
 				ContentVO rootContentVO = new ContentVO();
 				RepositoryVO repositoryVO = RepositoryController.getController().getRepositoryVOWithId(repositoryId);
 				rootContentVO.setCreatorName(userName);
 				rootContentVO.setName(repositoryVO.getName());
 				rootContentVO.setIsBranch(new Boolean(true));
 				content = create(db, null, null, repositoryId, rootContentVO);
 			}
 		}
 		
 		return content;
 	}
 
    	
 	/**
 	 * This method fetches the root content for a particular repository.
 	 * If there is no such content we create one as all repositories need one to work.
 	 */
 	        
 	public Content getRootContent(Integer repositoryId, Database db) throws ConstraintException, SystemException, Exception
 	{
 		Content content = null;
 
 		OQLQuery oql = db.getOQLQuery( "SELECT c FROM org.infoglue.cms.entities.content.impl.simple.ContentImpl c WHERE is_undefined(c.parentContent) AND c.repository.repositoryId = $1");
 		oql.bind(repositoryId);
 			
 		QueryResults results = oql.execute();			
 		if (results.hasMore()) 
 		{
 			content = (Content)results.next();
 		}
 
 		return content;
 	}
 	
    	/**
    	 * This method returns a list of the children a content has.
    	 */
    	
    	public List getContentChildrenVOList(Integer parentContentId) throws ConstraintException, SystemException
     {
    		String key = "" + parentContentId;
 		CmsLogger.logInfo("key:" + key);
 		List cachedChildContentVOList = (List)CacheController.getCachedObject("childContentCache", key);
 		if(cachedChildContentVOList != null)
 		{
 			CmsLogger.logInfo("There was an cached childContentVOList:" + cachedChildContentVOList.size());
 			return cachedChildContentVOList;
 		}
 		
 		Database db = CastorDatabaseService.getDatabase();
         ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
 
         List childrenVOList = null;
 
         beginTransaction(db);
 
         try
         {
             Content content = getContentWithId(parentContentId, db);
             Collection children = content.getChildren();
         	childrenVOList = ContentController.toVOList(children);
         	
             //If any of the validations or setMethods reported an error, we throw them up now before create.
             ceb.throwIfNotEmpty();
             
             commitTransaction(db);
         }
         catch(ConstraintException ce)
         {
             CmsLogger.logWarning("An error occurred so we should not complete the transaction:" + ce, ce);
             rollbackTransaction(db);
             throw ce;
         }
         catch(Exception e)
         {
             CmsLogger.logSevere("An error occurred so we should not complete the transaction:" + e, e);
             rollbackTransaction(db);
             throw new SystemException(e.getMessage());
         }
         
 		CacheController.cacheObject("childContentCache", key, childrenVOList);
         
         return childrenVOList;
     } 
    	
 	/**
 	 * This method returns the contentTypeDefinitionVO which is associated with this content.
 	 */
 	
 	public ContentTypeDefinitionVO getContentTypeDefinition(Integer contentId) throws ConstraintException, SystemException
     {
         Database db = CastorDatabaseService.getDatabase();
         ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
         
         ContentTypeDefinitionVO contentTypeDefinitionVO = null;
         
         beginTransaction(db);
 
         try
         {
 	        Content content = getContentWithId(contentId, db);
         	if(content != null && content.getContentTypeDefinition() != null)
 	        	contentTypeDefinitionVO = content.getContentTypeDefinition().getValueObject();
     
             //If any of the validations or setMethods reported an error, we throw them up now before create.
             ceb.throwIfNotEmpty();
             
             commitTransaction(db);
         }
         catch(ConstraintException ce)
         {
             CmsLogger.logWarning("An error occurred so we should not complete the transaction:" + ce, ce);
             rollbackTransaction(db);
             throw ce;
         }
         catch(Exception e)
         {
             CmsLogger.logSevere("An error occurred so we should not complete the transaction:" + e, e);
             rollbackTransaction(db);
             throw new SystemException(e.getMessage());
         }
         
     	return contentTypeDefinitionVO;
     }        
 
 	/**
 	 * This method reurns a list of available languages for this content.
 	 */
 	
     public List getRepositoryLanguages(Integer contentId) throws ConstraintException, SystemException
     {
         Database db = CastorDatabaseService.getDatabase();
         ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
 
         List languages = null;
         
         beginTransaction(db);
 
         try
         {
             languages = getAvailableLanguagesForContentWithId(contentId, db);
             
             //If any of the validations or setMethods reported an error, we throw them up now before create.
             ceb.throwIfNotEmpty();
             
             commitTransaction(db);
         }
         catch(ConstraintException ce)
         {
             CmsLogger.logWarning("An error occurred so we should not complete the transaction:" + ce, ce);
             rollbackTransaction(db);
             throw ce;
         }
         catch(Exception e)
         {
             CmsLogger.logSevere("An error occurred so we should not complete the transaction:" + e, e);
             rollbackTransaction(db);
             throw new SystemException(e.getMessage());
         }
 
         return languages;
     }        
 
 
 	/**
 	 * This method returns the bound contents based on a servicebinding.
 	 */
 	
 	public static List getBoundContents(Integer serviceBindingId) throws SystemException, Exception
 	{
 		List result = new ArrayList();
 		
 		Database db = CastorDatabaseService.getDatabase();
 
 		beginTransaction(db);
 		
 		try
 		{
 			ServiceBinding serviceBinding = ServiceBindingController.getServiceBindingWithId(serviceBindingId, db);
 	        
 			if(serviceBinding != null)
 			{
 				ServiceDefinition serviceDefinition = serviceBinding.getServiceDefinition();
 				if(serviceDefinition != null)
 				{
 					String serviceClassName = serviceDefinition.getClassName();
 					BaseService service = (BaseService)Class.forName(serviceClassName).newInstance();
 	        		 
 					HashMap arguments = new HashMap();
 					arguments.put("method", "selectContentListOnIdList");
 	            		
 					List qualifyerList = new ArrayList();
 					Collection qualifyers = serviceBinding.getBindingQualifyers();
 
 					qualifyers = sortQualifyers(qualifyers);
 
 					Iterator iterator = qualifyers.iterator();
 					while(iterator.hasNext())
 					{
 						Qualifyer qualifyer = (Qualifyer)iterator.next();
 						HashMap argument = new HashMap();
 						argument.put(qualifyer.getName(), qualifyer.getValue());
 						qualifyerList.add(argument);
 					}
 					arguments.put("arguments", qualifyerList);
 	        		
 					List contents = service.selectMatchingEntities(arguments);
 	        		
 					if(contents != null)
 					{
 						Iterator i = contents.iterator();
 						while(i.hasNext())
 						{
 							ContentVO candidate = (ContentVO)i.next();
 							result.add(candidate);        		
 						}
 					}
 				}
 			}
 	       	  
 			commitTransaction(db);
 		}
 		catch(Exception e)
 		{
 			CmsLogger.logSevere("An error occurred so we should not complete the transaction:" + e, e);
 			rollbackTransaction(db);
 			throw new SystemException(e.getMessage());
 		}
 		
 		return result;
 	}
 
 
 	/**
 	 * This method just sorts the list of qualifyers on sortOrder.
 	 */
 	
 	private static List sortQualifyers(Collection qualifyers)
 	{
 		List sortedQualifyers = new ArrayList();
 
 		try
 		{		
 			Iterator iterator = qualifyers.iterator();
 			while(iterator.hasNext())
 			{
 				Qualifyer qualifyer = (Qualifyer)iterator.next();
 				int index = 0;
 				Iterator sortedListIterator = sortedQualifyers.iterator();
 				while(sortedListIterator.hasNext())
 				{
 					Qualifyer sortedQualifyer = (Qualifyer)sortedListIterator.next();
 					if(sortedQualifyer.getSortOrder().intValue() > qualifyer.getSortOrder().intValue())
 					{
 						break;
 					}
 					index++;
 				}
 				sortedQualifyers.add(index, qualifyer);
 			    					
 			}
 		}
 		catch(Exception e)
 		{
 			CmsLogger.logWarning("The sorting of qualifyers failed:" + e.getMessage(), e);
 		}
 			
 		return sortedQualifyers;
 	}
  
 	/**
 	 * This method returns the contents belonging to a certain repository.
 	 */
 	
 	public List getRepositoryContents(Integer repositoryId, Database db) throws SystemException, Exception
 	{
 		List contents = new ArrayList();
 		
 		OQLQuery oql = db.getOQLQuery("SELECT c FROM org.infoglue.cms.entities.content.impl.simple.MediumContentImpl c WHERE c.repositoryId = $1");
     	oql.bind(repositoryId);
     	
     	QueryResults results = oql.execute(Database.ReadOnly);
 		
 		while(results.hasMore()) 
         {
         	MediumContentImpl content = (MediumContentImpl)results.next();
 			contents.add(content);
         }
 		
 		return contents;    	
 	}
 	
 	/**
 	 * This is a method that gives the user back an newly initialized ValueObject for this entity that the controller
 	 * is handling.
 	 */
 
 	public BaseEntityVO getNewVO()
 	{
 		return new ContentVO();
 	}
 
  
 }
