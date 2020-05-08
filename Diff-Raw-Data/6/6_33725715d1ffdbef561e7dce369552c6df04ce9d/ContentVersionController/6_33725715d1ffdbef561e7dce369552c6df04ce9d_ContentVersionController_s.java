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
 
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.apache.xerces.parsers.DOMParser;
 import org.exolab.castor.jdo.Database;
 import org.exolab.castor.jdo.OQLQuery;
 import org.exolab.castor.jdo.QueryResults;
 import org.infoglue.cms.applications.common.VisualFormatter;
 import org.infoglue.cms.entities.content.Content;
 import org.infoglue.cms.entities.content.ContentVO;
 import org.infoglue.cms.entities.content.ContentVersion;
 import org.infoglue.cms.entities.content.ContentVersionVO;
 import org.infoglue.cms.entities.content.DigitalAsset;
 import org.infoglue.cms.entities.content.DigitalAssetVO;
 import org.infoglue.cms.entities.content.impl.simple.ContentImpl;
 import org.infoglue.cms.entities.content.impl.simple.ContentVersionImpl;
 import org.infoglue.cms.entities.kernel.BaseEntityVO;
 import org.infoglue.cms.entities.management.ContentTypeDefinition;
 import org.infoglue.cms.entities.management.Language;
 import org.infoglue.cms.entities.management.RegistryVO;
 import org.infoglue.cms.entities.management.impl.simple.LanguageImpl;
 import org.infoglue.cms.entities.structure.SiteNode;
 import org.infoglue.cms.entities.structure.SiteNodeVersion;
 import org.infoglue.cms.exception.Bug;
 import org.infoglue.cms.exception.ConstraintException;
 import org.infoglue.cms.exception.SystemException;
 import org.infoglue.cms.security.InfoGluePrincipal;
 import org.infoglue.cms.util.ConstraintExceptionBuffer;
 import org.w3c.dom.CDATASection;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 
 /**
  * @author Mattias Bogeblad
  *
  */
 
 public class ContentVersionController extends BaseController 
 {
     private final static Logger logger = Logger.getLogger(ContentVersionController.class.getName());
 
 	private static final ContentCategoryController contentCategoryController = ContentCategoryController.getController();
 	private final RegistryController registryController = RegistryController.getController();
 
 	/**
 	 * Factory method to get object
 	 */
 	
 	public static ContentVersionController getContentVersionController()
 	{
 		return new ContentVersionController();
 	}
 	
 	private static Map contentMap = Collections.synchronizedMap(new HashMap());
 
     public Integer getContentIdForContentVersion(Integer contentVersionId) throws SystemException, Bug
     {
     	Integer contentId = (Integer)contentMap.get(contentVersionId);
     	if(contentId == null)
     	{
     		ContentVersionVO ContentVersionVO = (ContentVersionVO) getVOWithId(ContentVersionImpl.class, contentVersionId);
     		contentId = ContentVersionVO.getContentId();
     		contentMap.put(contentVersionId, contentId);
     	}
     	
     	return contentId;
     }
 
     public Integer getContentIdForContentVersion(Integer contentVersionId, Database db) throws SystemException, Bug
     {
     	Integer contentId = (Integer)contentMap.get(contentVersionId);
     	if(contentId == null)
     	{
     		System.out.println("Not cached the first time:" + contentVersionId);
     		ContentVersionVO ContentVersionVO = (ContentVersionVO) getVOWithId(ContentVersionImpl.class, contentVersionId, db);
     		contentId = ContentVersionVO.getContentId();
     		contentMap.put(contentVersionId, contentId);
     	}
     	
     	return contentId;
     }
 
     public ContentVersionVO getContentVersionVOWithId(Integer contentVersionId) throws SystemException, Bug
     {
 		return (ContentVersionVO) getVOWithId(ContentVersionImpl.class, contentVersionId);
     }
 
     public ContentVersion getContentVersionWithId(Integer contentVersionId, Database db) throws SystemException, Bug
     {
 		return (ContentVersion) getObjectWithId(ContentVersionImpl.class, contentVersionId, db);
     }
 
     public ContentVersion getReadOnlyContentVersionWithId(Integer contentVersionId, Database db) throws SystemException, Bug
     {
 		return (ContentVersion) getObjectWithIdAsReadOnly(ContentVersionImpl.class, contentVersionId, db);
     }
 
     public List getContentVersionVOList() throws SystemException, Bug
     {
         return getAllVOObjects(ContentVersionImpl.class, "contentVersionId");
     }
 
 	/**
 	 * Recursive methods to get all contentVersions of a given state under the specified parent content.
 	 */ 
 	
     public List getContentVersionVOWithParentRecursive(Integer contentId, Integer stateId, boolean mustBeFirst) throws ConstraintException, SystemException
 	{
 		return getContentVersionVOWithParentRecursive(contentId, stateId, new ArrayList(), mustBeFirst);
 	}
 	
 	private List getContentVersionVOWithParentRecursive(Integer contentId, Integer stateId, List resultList, boolean mustBeFirst) throws ConstraintException, SystemException
 	{
 		// Get the versions of this content.
 		resultList.addAll(getLatestContentVersionVOWithParent(contentId, stateId, mustBeFirst));
 		// Get the children of this content and do the recursion
 		List childContentList = ContentController.getContentController().getContentChildrenVOList(contentId);
 		Iterator cit = childContentList.iterator();
 		while (cit.hasNext())
 		{
 			ContentVO contentVO = (ContentVO) cit.next();
 			getContentVersionVOWithParentRecursive(contentVO.getId(), stateId, resultList, mustBeFirst);
 		}
 	
 		return resultList;
 	}
 
 	
 	/**
 	 * Recursive methods to get all contentVersions of a given state under the specified parent content.
 	 */ 
 	
     public List getContentVersionVOWithParentRecursiveAndRelated(Integer contentId, Integer stateId, boolean mustBeFirst) throws ConstraintException, SystemException
 	{
         List contentVersionVOList = new ArrayList();
         
         Database db = CastorDatabaseService.getDatabase();
 
 	    beginTransaction(db);
 
         try
         {
             List contentVersionList = getContentVersionWithParentRecursiveAndRelated(contentId, stateId, new ArrayList(), new ArrayList(), db, mustBeFirst);
             contentVersionVOList = toVOList(contentVersionList);
             
             commitTransaction(db);
         }
         catch(Exception e)
         {
             logger.error("An error occurred so we should not completes the transaction:" + e, e);
             rollbackTransaction(db);
             throw new SystemException(e.getMessage());
         }
         
         return contentVersionVOList;
 	}
 	
 	private List getContentVersionWithParentRecursiveAndRelated(Integer contentId, Integer stateId, List resultList, List checkedContents, Database db, boolean mustBeFirst) throws ConstraintException, SystemException, Exception
 	{
         checkedContents.add(contentId);
         
 		// Get the versions of this content.
 		List contentVersions = getLatestContentVersionWithParent(contentId, stateId, db, mustBeFirst);
 		resultList.addAll(contentVersions);
 	    
 		Iterator contentVersionsIterator = contentVersions.iterator();
 	    while(contentVersionsIterator.hasNext())
 	    {
 	        ContentVersion contentVersion = (ContentVersion)contentVersionsIterator.next();
 	        List relatedEntities = RegistryController.getController().getMatchingRegistryVOListForReferencingEntity(ContentVersion.class.getName(), contentVersion.getId().toString(), db);
 	        Iterator relatedEntitiesIterator = relatedEntities.iterator();
 	        
 	        while(relatedEntitiesIterator.hasNext())
 	        {
 	            RegistryVO registryVO = (RegistryVO)relatedEntitiesIterator.next();
 	            logger.info("registryVO:" + registryVO.getEntityName() + ":" + registryVO.getEntityId());
 	            if(registryVO.getEntityName().equals(Content.class.getName()) && !checkedContents.contains(new Integer(registryVO.getEntityId())))
 	            {
 	                List relatedContentVersions = getLatestContentVersionWithParent(new Integer(registryVO.getEntityId()), stateId, db, mustBeFirst);
 	    		    resultList.addAll(relatedContentVersions);
 	    		    checkedContents.add(new Integer(registryVO.getEntityId()));
 	            }
 	        }
 	    }
 	    
 		
 		// Get the children of this content and do the recursion
 		List childContentList = ContentController.getContentController().getContentChildrenVOList(contentId);
 		Iterator cit = childContentList.iterator();
 		while (cit.hasNext())
 		{
 			ContentVO contentVO = (ContentVO) cit.next();
 			getContentVersionWithParentRecursiveAndRelated(contentVO.getId(), stateId, resultList, checkedContents, db, mustBeFirst);
 		}
         
 		return resultList;
 	}
 
 	
 	public List getContentVersionVOWithParent(Integer contentId) throws SystemException, Bug
     {
         List resultList = new ArrayList();
     	Database db = CastorDatabaseService.getDatabase();
     	ContentVersionVO contentVersionVO = null;
 
         beginTransaction(db);
 
         try
         {
             List contentVersions = getContentVersionWithParent(contentId, db);
             resultList = toVOList(contentVersions);
             /*
             OQLQuery oql = db.getOQLQuery( "SELECT cv FROM org.infoglue.cms.entities.content.impl.simple.ContentVersionImpl cv WHERE cv.owningContent.contentId = $1 ORDER BY cv.contentVersionId desc");
         	oql.bind(contentId);
         	
         	QueryResults results = oql.execute(Database.ReadOnly);
 			
 			while (results.hasMore()) 
             {
             	ContentVersion contentVersion = (ContentVersion)results.next();
             	logger.info("found one:" + contentVersion.getValueObject());
             	contentVersionVO = contentVersion.getValueObject();
             	resultList.add(contentVersionVO);
             }
             */
             
             commitTransaction(db);
         }
         catch(Exception e)
         {
             logger.error("An error occurred so we should not completes the transaction:" + e, e);
             rollbackTransaction(db);
             throw new SystemException(e.getMessage());
         }
     	
 		return resultList;
     }
 
 	public List getContentVersionWithParent(Integer contentId, Database db) throws SystemException, Bug, Exception
     {
         ArrayList resultList = new ArrayList();
     	ContentVersionVO contentVersionVO = null;
 
         OQLQuery oql = db.getOQLQuery( "SELECT cv FROM org.infoglue.cms.entities.content.impl.simple.ContentVersionImpl cv WHERE cv.owningContent.contentId = $1 ORDER BY cv.contentVersionId desc");
     	oql.bind(contentId);
     	
     	QueryResults results = oql.execute(Database.ReadOnly);
 		
 		while (results.hasMore()) 
         {
         	ContentVersion contentVersion = (ContentVersion)results.next();
         	resultList.add(contentVersion);
         }
     	
 		results.close();
 		oql.close();
 
 		return resultList;
     }
 	
 	/**
 	 * This method returns a list of active contentversions, and only one / language in the specified state
 	 * 
 	 * @param contentId The content to look for versions in
 	 * @param stateId  The state of the versions
 	 * @return A list of the latest versions matching the given state
 	 * @throws SystemException
 	 * @throws Bug
 	 */
 
 	public List getLatestContentVersionVOWithParent(Integer contentId, Integer stateId, boolean mustBeFirst) throws SystemException, Bug
 	{
 		List resultList = new ArrayList();
 		
 		Database db = CastorDatabaseService.getDatabase();
 		
 		beginTransaction(db);
 
 		try
 		{
 		    resultList = getLatestContentVersionWithParent(contentId, stateId, db, mustBeFirst);
 		    resultList = toVOList(resultList);
 		    
 			commitTransaction(db);
 		}
 		catch(Exception e)
 		{
 			logger.error("An error occurred so we should not completes the transaction:" + e, e);
 			rollbackTransaction(db);
 			throw new SystemException(e.getMessage());
 		}
     	
 		return resultList;
 	}	
 	
 	/**
 	 * This method returns a list of active contentversions, and only one / language in the specified state
 	 * 
 	 * @param contentId The content to look for versions in
 	 * @param stateId  The state of the versions
 	 * @return A list of the latest versions matching the given state
 	 * @throws SystemException
 	 * @throws Bug
 	 */
 
 	public List getLatestContentVersionWithParent(Integer contentId, Integer stateId, Database db, boolean mustBeFirst) throws SystemException, Bug, Exception
 	{
 		ArrayList resultList = new ArrayList();
 		ArrayList langCheck = new ArrayList();
 		
 		OQLQuery oql = db.getOQLQuery( "SELECT cv FROM org.infoglue.cms.entities.content.impl.simple.ContentVersionImpl cv WHERE cv.owningContent.contentId = $1 ORDER BY cv.contentVersionId desc");
 		oql.bind(contentId);
 		// oql.bind(stateId);
     	
 		QueryResults results = oql.execute(Database.ReadOnly);
 		
 		// New improved
 		while (results.hasMore()) 
 		{
 			ContentVersion contentVersion = (ContentVersion)results.next();
 			logger.info("contentVersion:" + contentVersion.getValueObject().getContentName());
 			if(contentVersion.getIsActive().booleanValue())
 			{
 			    if ( (contentVersion.getStateId().compareTo(stateId)==0) && (!langCheck.contains(contentVersion.getLanguage().getLanguageId())))
 				{
 				    logger.info("Added contentVersion:" + contentVersion.getValueObject().getContentName() + ":" + contentVersion.getId() + ":" + contentVersion.getIsActive() + ":" + contentVersion.getStateId());
 					resultList.add(contentVersion);
 					langCheck.add(contentVersion.getLanguage().getLanguageId());
 				}
 				
 			    if(mustBeFirst)
 					langCheck.add(contentVersion.getLanguage().getLanguageId());
 			}
 		}
     	
 		results.close();
 		oql.close();
 
 		logger.info("getLatestContentVersionWithParent done...");
 		
 		return resultList;
 	}
    
 	
 	/**
 	 * This method returns the latest contentVersion there is for the given content if it is active and is the latest made.
 	 */
 	
 	public List getLatestActiveContentVersionIfInState(Content content, Integer stateId, Database db) throws SystemException, Exception
 	{
 		List resultList = new ArrayList();
 	    Map lastLanguageVersions = new HashMap();
 	    Map languageVersions = new HashMap();
 	    
 		Collection contentVersions = content.getContentVersions();
 		
 		Iterator versionIterator = contentVersions.iterator();
 		while(versionIterator.hasNext())
 		{
 		    ContentVersion contentVersionCandidate = (ContentVersion)versionIterator.next();	
 			
 		    ContentVersion lastVersionInThatLanguage = (ContentVersion)lastLanguageVersions.get(contentVersionCandidate.getLanguage().getId());
 			if(lastVersionInThatLanguage == null || (lastVersionInThatLanguage.getId().intValue() < contentVersionCandidate.getId().intValue() && contentVersionCandidate.getIsActive().booleanValue()))
 			    lastLanguageVersions.put(contentVersionCandidate.getLanguage().getId(), contentVersionCandidate);
 			
 			if(contentVersionCandidate.getIsActive().booleanValue() && contentVersionCandidate.getStateId().intValue() == stateId.intValue())
 			{
 				if(contentVersionCandidate.getOwningContent().getContentId().intValue() == content.getId().intValue())
 				{
 				    ContentVersion versionInThatLanguage = (ContentVersion)languageVersions.get(contentVersionCandidate.getLanguage().getId());
 					if(versionInThatLanguage == null || versionInThatLanguage.getContentVersionId().intValue() < contentVersionCandidate.getId().intValue())
 					{
 					    languageVersions.put(contentVersionCandidate.getLanguage().getId(), contentVersionCandidate);
 					}
 				}
 			}
 		}
 
 		logger.info("Found languageVersions:" + languageVersions.size());
 		logger.info("Found lastLanguageVersions:" + lastLanguageVersions.size());
 		Iterator i = languageVersions.values().iterator();
 		while(i.hasNext())
 		{
 		    ContentVersion contentVersion = (ContentVersion)i.next();
 		    ContentVersion lastVersionInThatLanguage = (ContentVersion)lastLanguageVersions.get(contentVersion.getLanguage().getId());
 
 		    logger.info("contentVersion:" + contentVersion.getId());
 		    logger.info("lastVersionInThatLanguage:" + lastVersionInThatLanguage.getId());
 
 		    if(contentVersion == lastVersionInThatLanguage)
 			    resultList.add(contentVersion);
 		}
 		
 		return resultList;
 	}
 	
     
     /**
      * This method returns the latest active content version.
      */
     
    	public ContentVersionVO getLatestActiveContentVersionVO(Integer contentId, Integer languageId) throws SystemException, Bug
     {
     	Database db = CastorDatabaseService.getDatabase();
     	ContentVersionVO contentVersionVO = null;
 
         beginTransaction(db);
 
         try
         {
         	ContentVersion contentVersion = null;
         	
 			contentVersion = getLatestActiveContentVersion(contentId, languageId, db);
             /*
             Collection contentVersions = content.getContentVersions();
             
             Iterator i = contentVersions.iterator();
             
             while(i.hasNext())
             {
             	ContentVersion currentContentVersion = (ContentVersion)i.next();
             	logger.info("found one candidate:" + currentContentVersion.getValueObject());
 				if(contentVersion == null || (currentContentVersion.getId().intValue() > contentVersion.getId().intValue()))
 				{
 					if(currentContentVersion.getIsActive().booleanValue() &&  currentContentVersion.getLanguage().getId().intValue() == languageId.intValue())
 						contentVersion = currentContentVersion;
 				}
             }
             */
             
             if(contentVersion != null)
 	            contentVersionVO = contentVersion.getValueObject();
             
             commitTransaction(db);
         }
         catch(Exception e)
         {
             logger.error("An error occurred so we should not completes the transaction:" + e, e);
             rollbackTransaction(db);
             throw new SystemException(e.getMessage());
         }
     	
 		return contentVersionVO;
     }
 
     /**
      * This method returns the latest active content version.
      */
     
    	public ContentVersionVO getLatestActiveContentVersionVO(Integer contentId, Integer languageId, Database db) throws SystemException, Bug
     {
     	ContentVersionVO contentVersionVO = null;
 
        	ContentVersion contentVersion = getLatestActiveContentVersion(contentId, languageId, db);
             
         if(contentVersion != null)
             contentVersionVO = contentVersion.getValueObject();
     	
 		return contentVersionVO;
     }
 
    	/**
 	 * This method returns the latest active content version.
 	 */
     
 	public ContentVersion getLatestActiveContentVersion(Integer contentId, Integer languageId, Database db) throws SystemException, Bug
 	{
 		ContentVersion contentVersion = null;
     	
 		Content content = ContentController.getContentController().getContentWithId(contentId, db);
     	logger.info("contentId:" + contentId);
     	logger.info("languageId:" + languageId);
     	logger.info("content:" + content.getName());
 		Collection contentVersions = content.getContentVersions();
 		logger.info("contentVersions:" + contentVersions.size());
         
 		Iterator i = contentVersions.iterator();
         while(i.hasNext())
 		{
 			ContentVersion currentContentVersion = (ContentVersion)i.next();
 			logger.info("found one candidate:" + currentContentVersion.getValueObject());
 			if(contentVersion == null || (currentContentVersion.getId().intValue() > contentVersion.getId().intValue()))
 			{
 				logger.info("currentContentVersion:" + currentContentVersion.getIsActive());
 				logger.info("currentContentVersion:" + currentContentVersion.getLanguage().getId());
 				if(currentContentVersion.getIsActive().booleanValue() &&  currentContentVersion.getLanguage().getId().intValue() == languageId.intValue())
 					contentVersion = currentContentVersion;
 			}
 		}
         
 		return contentVersion;
 	}
     
 
 	public ContentVersionVO getLatestContentVersionVO(Integer contentId, Integer languageId) throws SystemException, Bug
     {
     	Database db = CastorDatabaseService.getDatabase();
     	ContentVersionVO contentVersionVO = null;
 
         beginTransaction(db);
 
         try
         {
            
             OQLQuery oql = db.getOQLQuery( "SELECT cv FROM org.infoglue.cms.entities.content.impl.simple.ContentVersionImpl cv WHERE cv.owningContent.contentId = $1 AND cv.language.languageId = $2 ORDER BY cv.contentVersionId desc");
         	oql.bind(contentId);
         	oql.bind(languageId);
         	
         	QueryResults results = oql.execute(Database.ReadOnly);
 			
 			if (results.hasMore()) 
             {
             	ContentVersion contentVersion = (ContentVersion)results.next();
             	logger.info("found one:" + contentVersion.getValueObject());
             	contentVersionVO = contentVersion.getValueObject();
             }
             
 			results.close();
 			oql.close();
 
 			commitTransaction(db);
         }
         catch(Exception e)
         {
             logger.error("An error occurred so we should not completes the transaction:" + e, e);
             rollbackTransaction(db);
             throw new SystemException(e.getMessage());
         }
     	
         
 		return contentVersionVO;
     }
 
 
 	public ContentVersion getContentVersionWithId(Integer contentVersionId) throws SystemException, Bug
     {
     	Database db = CastorDatabaseService.getDatabase();
     	ContentVersion contentVersion = null;
 
         beginTransaction(db);
 
         try
         {
            	contentVersion = getContentVersionWithId(contentVersionId, db);
             
             commitTransaction(db);
         }
         catch(Exception e)
         {
             logger.error("An error occurred so we should not completes the transaction:" + e, e);
             rollbackTransaction(db);
             throw new SystemException(e.getMessage());
         }
     	
 		return contentVersion;
     }
 
 
 	public ContentVersion getLatestContentVersion(Integer contentId, Integer languageId, Database db) throws SystemException, Bug, Exception
     {
         ContentVersion contentVersion = null;
         
         OQLQuery oql = db.getOQLQuery( "SELECT cv FROM org.infoglue.cms.entities.content.impl.simple.ContentVersionImpl cv WHERE cv.owningContent.contentId = $1 AND cv.language.languageId = $2 ORDER BY cv.contentVersionId desc");
     	oql.bind(contentId);
     	oql.bind(languageId);
     	
     	QueryResults results = oql.execute(Database.ReadOnly);
 		
 		if (results.hasMore()) 
         {
         	contentVersion = (ContentVersion)results.next();
         }
 		
 		results.close();
 		oql.close();
 		
 		return contentVersion;
     }
 
    	
 	/**
 	 * This method created a new contentVersion in the database.
 	 */
 	
     public ContentVersionVO create(Integer contentId, Integer languageId, ContentVersionVO contentVersionVO, Integer oldContentVersionId) throws ConstraintException, SystemException
     {
 		Database db = CastorDatabaseService.getDatabase();
         ContentVersion contentVersion = null;
 
         beginTransaction(db);
 		try
         {
 			contentVersion = create(contentId, languageId, contentVersionVO, oldContentVersionId, db);
 			commitTransaction(db);
 		}
         catch(Exception e)
         {
         	logger.error("An error occurred so we should not completes the transaction:" + e, e);
             rollbackTransaction(db);
             throw new SystemException(e.getMessage());
         }
         
         return contentVersion.getValueObject();
     }     
 
 	/**
 	 * This method created a new contentVersion in the database. It also updates the owning content
 	 * so it recognises the change. 
 	 */
 	
     public ContentVersion create(Integer contentId, Integer languageId, ContentVersionVO contentVersionVO, Integer oldContentVersionId, Database db) throws ConstraintException, SystemException, Exception
     {
 		Content content   = ContentController.getContentController().getContentWithId(contentId, db);
     	Language language = LanguageController.getController().getLanguageWithId(languageId, db);
 		return create(content, language, contentVersionVO, oldContentVersionId, db);
     }     
     
 	/**
 	 * This method created a new contentVersion in the database. It also updates the owning content
 	 * so it recognises the change. 
 	 */
 	
     public ContentVersion create(Content content, Language language, ContentVersionVO contentVersionVO, Integer oldContentVersionId, Database db) throws ConstraintException, SystemException, Exception
     {
 		ContentVersion contentVersion = new ContentVersionImpl();
 		contentVersion.setLanguage((LanguageImpl)language);
 		logger.info("Content:" + content.getContentId() + ":" + db.isPersistent(content));
 		contentVersion.setOwningContent((ContentImpl)content);
 		
 		contentVersion.setValueObject(contentVersionVO);
         db.create(contentVersion); 
 
         content.getContentVersions().add(contentVersion);
 
         if(oldContentVersionId != null && oldContentVersionId.intValue() != -1)
 		    copyDigitalAssets(getContentVersionWithId(oldContentVersionId, db), contentVersion, db);
 		    //contentVersion.setDigitalAssets(getContentVersionWithId(oldContentVersionId, db).getDigitalAssets());
 				
         return contentVersion;
     }     
 
 	/**
 	 * This method deletes an contentversion and notifies the owning content.
 	 */
 	
     public void delete(ContentVersionVO contentVersionVO) throws ConstraintException, SystemException
     {
     	Database db = CastorDatabaseService.getDatabase();
         beginTransaction(db);
 		try
         {
 			delete(contentVersionVO, db);
 			commitTransaction(db);
 		}
         catch(Exception e)
         {
         	logger.error("An error occurred so we should not completes the transaction:" + e, e);
             rollbackTransaction(db);
             throw new SystemException(e.getMessage());
         }
     }        
 	
 	/**
 	 * This method deletes an contentversion and notifies the owning content.
 	 */
 	
     public void delete(ContentVersionVO contentVersionVO, Database db) throws ConstraintException, SystemException, Exception
     {
 		ContentVersion contentVersion = getContentVersionWithId(contentVersionVO.getContentVersionId(), db);
 		delete(contentVersion, db, false);
     }        
 
 	/**
 	 * This method deletes an contentversion and notifies the owning content.
 	 */
 	
  	public void delete(ContentVersion contentVersion, Database db) throws ConstraintException, SystemException, Exception
 	{
  	    delete(contentVersion, db, false);
 	}
 	
 	/**
 	 * This method deletes an contentversion and notifies the owning content.
 	 */
 	
  	public void delete(ContentVersion contentVersion, Database db, boolean forceDelete) throws ConstraintException, SystemException, Exception
 	{
 		if (!forceDelete && contentVersion.getStateId().intValue() == ContentVersionVO.PUBLISHED_STATE.intValue() && contentVersion.getIsActive().booleanValue() == true)
			throw new ConstraintException("ContentVersion.stateId", "3300");

 		contentCategoryController.deleteByContentVersion(contentVersion, db);
 
 		Content content = contentVersion.getOwningContent();
 
 		if(content != null)
 		    content.getContentVersions().remove(contentVersion);
 
 		db.remove(contentVersion);
 	}
 
 
 
 	/**
 	 * This method deletes all contentVersions for the content sent in.
 	 * The contentVersion is related to digital assets but we don't remove the asset itself in case 
 	 * other versions or contents reference the same asset.
 	 */
 	
 	public void deleteVersionsForContent(Content content, Database db) throws ConstraintException, SystemException, Bug, Exception
     {
 	    deleteVersionsForContent(content, db, false);
     }
 	
 	/**
 	 * This method deletes all contentVersions for the content sent in.
 	 * The contentVersion is related to digital assets but we don't remove the asset itself in case 
 	 * other versions or contents reference the same asset.
 	 */
 	
 	public void deleteVersionsForContent(Content content, Database db, boolean forceDelete) throws ConstraintException, SystemException, Bug, Exception
     {
         Collection contentVersions = Collections.synchronizedCollection(content.getContentVersions());
        	Iterator contentVersionIterator = contentVersions.iterator();
 			
 		while (contentVersionIterator.hasNext()) 
         {
         	ContentVersion contentVersion = (ContentVersion)contentVersionIterator.next();
 			
         	Collection digitalAssetList = contentVersion.getDigitalAssets();
 			Iterator assets = digitalAssetList.iterator();
 			while (assets.hasNext()) 
             {
             	DigitalAsset digitalAsset = (DigitalAsset)assets.next();
 				assets.remove();
 				db.remove(digitalAsset);
 			}
 			
         	logger.info("Deleting contentVersion:" + contentVersion.getContentVersionId());
         	contentVersionIterator.remove();
         	delete(contentVersion, db, forceDelete);
         }
         content.setContentVersions(new ArrayList());
     }
 
 	/**
 	 * This method deletes a digitalAsset.
 	 */
 	
     public void deleteDigitalAsset(Integer contentId, Integer languageId, String assetKey) throws ConstraintException, SystemException
     {
     	Database db = CastorDatabaseService.getDatabase();
         beginTransaction(db);
 		try
         {
 		    ContentVersion contentVersion = this.getLatestActiveContentVersion(contentId, languageId, db);
 		    
 		    Collection digitalAssets = contentVersion.getDigitalAssets();
 			Iterator assetIterator = digitalAssets.iterator();
 			while(assetIterator.hasNext())
 			{
 				DigitalAsset currentDigitalAsset = (DigitalAsset)assetIterator.next();
 				if(currentDigitalAsset.getAssetKey().equals(assetKey))
 				{
 					assetIterator.remove();
 					db.remove(currentDigitalAsset);
 					break;
 				}
 			}
 			
 			commitTransaction(db);
 		}
         catch(Exception e)
         {
         	logger.error("An error occurred so we should not completes the transaction:" + e, e);
             rollbackTransaction(db);
             throw new SystemException(e.getMessage());
         }
     }        
 	
 	
     /**
      * This method updates the contentversion.
      */
     
     public ContentVersionVO update(Integer contentId, Integer languageId, ContentVersionVO contentVersionVO) throws ConstraintException, SystemException
     {
         ContentVersionVO updatedContentVersionVO;
 		
         Database db = CastorDatabaseService.getDatabase();
 
         beginTransaction(db);
         
         try
         {     
             Content content = ContentController.getContentController().getContentWithId(contentId, db);
             ContentTypeDefinition contentTypeDefinition = content.getContentTypeDefinition();
             ConstraintExceptionBuffer ceb = contentVersionVO.validateAdvanced(contentTypeDefinition.getValueObject());
             ceb.throwIfNotEmpty();
             
             ContentVersion contentVersion = null;
             
 	        if(contentVersionVO.getId() == null)
 	    	{
 	    		logger.info("Creating the entity because there was no version at all for: " + contentId + " " + languageId);
 	    		contentVersion = create(contentId, languageId, contentVersionVO, null, db);
 	    	}
 	    	else
 	    	{
 	    	    contentVersion = ContentVersionController.getContentVersionController().getContentVersionWithId(contentVersionVO.getId(), db);
 	    	    contentVersion.setValueObject(contentVersionVO);
 	    	}
 
 	    	registryController.updateContentVersion(contentVersion, db);
 
 	    	updatedContentVersionVO = contentVersion.getValueObject();
 	    	
 	    	commitTransaction(db);  
         }
         catch(ConstraintException ce)
         {
         	logger.error("Validation error:" + ce, ce);
             rollbackTransaction(db);
             throw ce;
         }
         catch(Exception e)
         {
         	logger.error("An error occurred so we should not completes the transaction:" + e, e);
             rollbackTransaction(db);
             throw new SystemException(e.getMessage());
         }
         
     	return updatedContentVersionVO; //(ContentVersionVO) updateEntity(ContentVersionImpl.class, realContentVersionVO);
     }        
 
 
 	public List getPublishedActiveContentVersionVOList(Integer contentId) throws SystemException, Bug, Exception
     {
         List contentVersionVOList = new ArrayList();
         
         Database db = CastorDatabaseService.getDatabase();
         beginTransaction(db);
         try
         {        
 	        OQLQuery oql = db.getOQLQuery( "SELECT cv FROM org.infoglue.cms.entities.content.impl.simple.ContentVersionImpl cv WHERE cv.owningContent.contentId = $1 AND cv.stateId = $2 AND cv.isActive = $3 ORDER BY cv.contentVersionId desc");
 	    	oql.bind(contentId);
 	    	oql.bind(ContentVersionVO.PUBLISHED_STATE);
 	    	oql.bind(true);
 	    	
 	    	QueryResults results = oql.execute(Database.ReadOnly);
 			
 			while (results.hasMore()) 
 	        {
 	        	ContentVersion contentVersion = (ContentVersion)results.next();
 	        	contentVersionVOList.add(contentVersion.getValueObject());
 	        }
 			
 			results.close();
 			oql.close();
 
             commitTransaction(db);            
         }
         catch(Exception e)
         {
         	logger.error("An error occurred so we should not completes the transaction:" + e, e);
             rollbackTransaction(db);
             throw new SystemException(e.getMessage());
         }
             
 		return contentVersionVOList;
     }
 
     
 	public ContentVersion getLatestPublishedContentVersion(Integer contentId) throws SystemException, Bug, Exception
     {
         ContentVersion contentVersion = null;
         
         Database db = CastorDatabaseService.getDatabase();
         beginTransaction(db);
         try
         {        
 	        OQLQuery oql = db.getOQLQuery( "SELECT cv FROM org.infoglue.cms.entities.content.impl.simple.ContentVersionImpl cv WHERE cv.owningContent.contentId = $1 AND cv.stateId = $2 AND cv.isActive = $3 ORDER BY cv.contentVersionId desc");
 	    	oql.bind(contentId);
 	    	oql.bind(ContentVersionVO.PUBLISHED_STATE);
 	    	oql.bind(true);
 	    	
 	    	QueryResults results = oql.execute(Database.ReadOnly);
 			
 			if (results.hasMore()) 
 	        {
 	        	contentVersion = (ContentVersion)results.next();
 	        }
 
 			results.close();
 			oql.close();
 
             commitTransaction(db);            
         }
         catch(Exception e)
         {
         	logger.error("An error occurred so we should not completes the transaction:" + e, e);
             rollbackTransaction(db);
             throw new SystemException(e.getMessage());
         }
             
 		return contentVersion;
     }
 
 
 	public ContentVersion getLatestPublishedContentVersion(Integer contentId, Integer languageId) throws SystemException, Bug, Exception
     {
         ContentVersion contentVersion = null;
         
         Database db = CastorDatabaseService.getDatabase();
         beginTransaction(db);
         try
         {        
 	        OQLQuery oql = db.getOQLQuery( "SELECT cv FROM org.infoglue.cms.entities.content.impl.simple.ContentVersionImpl cv WHERE cv.owningContent.contentId = $1 AND cv.language.languageId = $2 AND cv.stateId = $3 AND cv.isActive = $4 ORDER BY cv.contentVersionId desc");
 	    	oql.bind(contentId);
 	    	oql.bind(languageId);
 	    	oql.bind(ContentVersionVO.PUBLISHED_STATE);
 	    	oql.bind(true);
 	    	
 	    	QueryResults results = oql.execute(Database.ReadOnly);
 			
 			if (results.hasMore()) 
 	        {
 	        	contentVersion = (ContentVersion)results.next();
 	        }
 			
 			results.close();
 			oql.close();
 
 			commitTransaction(db);            
         }
         catch(Exception e)
         {
         	logger.error("An error occurred so we should not completes the transaction:" + e, e);
             rollbackTransaction(db);
             throw new SystemException(e.getMessage());
         }
             
 		return contentVersion;
     }
 
 
 	public ContentVersion getLatestPublishedContentVersion(Integer contentId, Integer languageId, Database db) throws SystemException, Bug, Exception
     {
         ContentVersion contentVersion = null;
         
         OQLQuery oql = db.getOQLQuery( "SELECT cv FROM org.infoglue.cms.entities.content.impl.simple.ContentVersionImpl cv WHERE cv.owningContent.contentId = $1 AND cv.language.languageId = $2 AND cv.stateId = $3 AND cv.isActive = $4 ORDER BY cv.contentVersionId desc");
     	oql.bind(contentId);
     	oql.bind(languageId);
     	oql.bind(ContentVersionVO.PUBLISHED_STATE);
     	oql.bind(true);
     	
     	QueryResults results = oql.execute();
 		this.logger.info("Fetching entity in read/write mode");
 
 		if (results.hasMore()) 
         {
         	contentVersion = (ContentVersion)results.next();
         }
             
 		results.close();
 		oql.close();
 
 		return contentVersion;
     }
 
 
 	/**
 	 * This method returns the version previous to the one sent in.
 	 */
 	
 	public ContentVersionVO getPreviousContentVersionVO(Integer contentId, Integer languageId, Integer contentVersionId) throws SystemException, Bug
     {
     	Database db = CastorDatabaseService.getDatabase();
     	ContentVersionVO contentVersionVO = null;
 
         beginTransaction(db);
 
         try
         {           
             OQLQuery oql = db.getOQLQuery( "SELECT cv FROM org.infoglue.cms.entities.content.impl.simple.ContentVersionImpl cv WHERE cv.owningContent.contentId = $1 AND cv.language.languageId = $2 AND cv.contentVersionId < $3 ORDER BY cv.contentVersionId desc");
         	oql.bind(contentId);
         	oql.bind(languageId);
         	oql.bind(contentVersionId);
         	
         	QueryResults results = oql.execute(Database.ReadOnly);
 			
 			if (results.hasMore()) 
             {
             	ContentVersion contentVersion = (ContentVersion)results.next();
             	logger.info("found one:" + contentVersion.getValueObject());
             	contentVersionVO = contentVersion.getValueObject();
             }
             
 			results.close();
 			oql.close();
 
 			commitTransaction(db);
         }
         catch(Exception e)
         {
             logger.error("An error occurred so we should not completes the transaction:" + e, e);
             rollbackTransaction(db);
             throw new SystemException(e.getMessage());
         }
     	
 		return contentVersionVO;
     }
 
 
 	/**
 	 * This method returns the version previous to the one sent in.
 	 */
 	
 	public ContentVersionVO getPreviousActiveContentVersionVO(Integer contentId, Integer languageId, Integer contentVersionId, Database db) throws SystemException, Bug, Exception
     {
     	ContentVersionVO contentVersionVO = null;
 
         OQLQuery oql = db.getOQLQuery( "SELECT cv FROM org.infoglue.cms.entities.content.impl.simple.ContentVersionImpl cv WHERE cv.owningContent.contentId = $1 AND cv.language.languageId = $2 AND cv.isActive = $3 AND cv.contentVersionId < $4 ORDER BY cv.contentVersionId desc");
     	oql.bind(contentId);
     	oql.bind(languageId);
     	oql.bind(new Boolean(true));
     	oql.bind(contentVersionId);
     	
     	QueryResults results = oql.execute(Database.ReadOnly);
 		
 		if (results.hasMore()) 
         {
         	ContentVersion contentVersion = (ContentVersion)results.next();
         	logger.info("found one:" + contentVersion.getValueObject());
         	contentVersionVO = contentVersion.getValueObject();
         }
     	
 		results.close();
 		oql.close();
 
 		return contentVersionVO;
     }
 
 
 	/**
 	 * This method deletes the relation to a digital asset - not the asset itself.
 	 */
 	public void deleteDigitalAssetRelation(Integer contentVersionId, Integer digitalAssetId) throws SystemException, Bug
     {
     	Database db = CastorDatabaseService.getDatabase();
         beginTransaction(db);
 
         try
         {           
         	ContentVersion contentVersion = getContentVersionWithId(contentVersionId, db);
 			DigitalAsset digitalAsset = DigitalAssetController.getDigitalAssetWithId(digitalAssetId, db);			
 			contentVersion.getDigitalAssets().remove(digitalAsset);
             digitalAsset.getContentVersions().remove(contentVersion);
             commitTransaction(db);
         }
         catch(Exception e)
         {
             logger.error("An error occurred so we should not completes the transaction:" + e, e);
             rollbackTransaction(db);
             throw new SystemException(e.getMessage());
         }
     }
     
 	
 	/**
 	 * This method deletes the relation to a digital asset - not the asset itself.
 	 */
 	public void deleteDigitalAssetRelation(Integer contentVersionId, DigitalAsset digitalAsset, Database db) throws SystemException, Bug
     {
     	ContentVersion contentVersion = getContentVersionWithId(contentVersionId, db);
 		contentVersion.getDigitalAssets().remove(digitalAsset);
         digitalAsset.getContentVersions().remove(contentVersion);
     }
     
 	
 	/**
 	 * This method assigns the same digital assets the old content-version has.
 	 * It's ofcourse important that noone deletes the digital asset itself for then it's lost to everyone.
 	 */
 	
 	public void copyDigitalAssets(ContentVersion originalContentVersion, ContentVersion newContentVersion, Database db) throws ConstraintException, SystemException, Exception
 	{
 	    Collection digitalAssets = originalContentVersion.getDigitalAssets();	
 
 		//List newDigitalAssets = new ArrayList();
 	    Iterator digitalAssetsIterator = digitalAssets.iterator();
 		while(digitalAssetsIterator.hasNext())
 		{
 		    DigitalAsset digitalAsset = (DigitalAsset)digitalAssetsIterator.next();
 		    logger.info("Copying digitalAssets " + digitalAsset.getAssetKey());
 		    DigitalAssetVO digitalAssetVO = digitalAsset.getValueObject();
 		    
 		    DigitalAssetController.create(digitalAssetVO, DigitalAssetController.getController().getAssetInputStream(digitalAsset), newContentVersion, db);
 		    //DigitalAssetController.create(digitalAssetVO, digitalAsset.getAssetBlob(), newContentVersion, db);
 		    logger.info("digitalAssets:" + digitalAssets.size());
 		}
 		//newContentVersion.setDigitalAssets(digitalAssets);
 	}	
 
 	
 	/**
 	 * This method fetches a value from the xml that is the contentVersions Value. If the 
 	 * contentVersioVO is null the contentVersion has not been created yet and no values are present.
 	 */
 	public String getAttributeValue(Integer contentVersionId, String attributeName, boolean escapeHTML) throws SystemException
 	{
 		String value = "";
 		ContentVersionVO contentVersionVO = getContentVersionVOWithId(contentVersionId);
 		
 		if(contentVersionVO != null)
 		{
 			try
 			{
 				logger.info("attributeName:" + attributeName);
 				logger.info("VersionValue:"  + contentVersionVO.getVersionValue());
 				value = getAttributeValue(contentVersionVO, attributeName, escapeHTML);
 			}
 			catch (Exception e)
 			{
 				e.printStackTrace();
 			}
 		}
 		//logger.info("value:" + value);
 		return value;
 	}
 
 	/**
 	 * Returns an attribute value from the ContentVersionVO
 	 *
 	 * @param contentVersionVO The version on which to find the value
 	 * @param attributeName THe name of the attribute whose value is wanted
 	 * @param escapeHTML A boolean indicating if the result should be escaped
 	 * @return The String vlaue of the attribute, or blank if it doe snot exist.
 	 */
 	public String getAttributeValue(ContentVersionVO contentVersionVO, String attributeName, boolean escapeHTML)
 	{
 		String value = "";
 		String xml = contentVersionVO.getVersionValue();
 
 		int startTagIndex = xml.indexOf("<" + attributeName + ">");
 		int endTagIndex   = xml.indexOf("]]></" + attributeName + ">");
 
 		if(startTagIndex > 0 && startTagIndex < xml.length() && endTagIndex > startTagIndex && endTagIndex <  xml.length())
 		{
 			value = xml.substring(startTagIndex + attributeName.length() + 11, endTagIndex);
 			if(escapeHTML)
 				value = new VisualFormatter().escapeHTML(value);
 		}		
 
 		return value;
 	}
 
 
 	/**
 	 * This method fetches a value from the xml that is the contentVersions Value. If the 
 	 * contentVersioVO is null the contentVersion has not been created yet and no values are present.
 	 */
 	 
 	public void updateAttributeValue(Integer contentVersionId, String attributeName, String attributeValue, InfoGluePrincipal infogluePrincipal) throws SystemException, Bug
 	{
 		ContentVersionVO contentVersionVO = getContentVersionVOWithId(contentVersionId);
 		
 		if(contentVersionVO != null)
 		{
 			try
 			{
 				logger.info("attributeName:"  + attributeName);
 				logger.info("versionValue:"   + contentVersionVO.getVersionValue());
 				logger.info("attributeValue:" + attributeValue);
 				InputSource inputSource = new InputSource(new StringReader(contentVersionVO.getVersionValue()));
 				
 				DOMParser parser = new DOMParser();
 				parser.parse(inputSource);
 				Document document = parser.getDocument();
 				
 				NodeList nl = document.getDocumentElement().getChildNodes();
 				Node attributesNode = nl.item(0);
 				
 				boolean existed = false;
 				nl = attributesNode.getChildNodes();
 				for(int i=0; i<nl.getLength(); i++)
 				{
 					Node n = nl.item(i);
 					if(n.getNodeName().equalsIgnoreCase(attributeName))
 					{
 						if(n.getFirstChild() != null && n.getFirstChild().getNodeValue() != null)
 						{
 							n.getFirstChild().setNodeValue(attributeValue);
 							existed = true;
 							break;
 						}
 						else
 						{
 							CDATASection cdata = document.createCDATASection(attributeValue);
 							n.appendChild(cdata);
 							existed = true;
 							break;
 						}
 					}
 				}
 				
 				if(existed == false)
 				{
 					org.w3c.dom.Element attributeElement = document.createElement(attributeName);
 					attributesNode.appendChild(attributeElement);
 					CDATASection cdata = document.createCDATASection(attributeValue);
 					attributeElement.appendChild(cdata);
 				}
 				
 				StringBuffer sb = new StringBuffer();
 				org.infoglue.cms.util.XMLHelper.serializeDom(document.getDocumentElement(), sb);
 				logger.info("sb:" + sb);
 				contentVersionVO.setVersionValue(sb.toString());
 				contentVersionVO.setVersionModifier(infogluePrincipal.getName());
 				update(contentVersionVO.getContentId(), contentVersionVO.getLanguageId(), contentVersionVO);
 			}
 			catch(Exception e)
 			{
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * This is a method that gives the user back an newly initialized ValueObject for this entity that the controller
 	 * is handling.
 	 */
 
 	public BaseEntityVO getNewVO()
 	{
 		return new ContentVersionVO();
 	}
 
 	/**
 	 * Recursive methods to get all contentVersions of a given state under the specified parent content.
 	 */ 
 	
     public void getContentAndAffectedItemsRecursive(Integer contentId, Integer stateId, List siteNodeVersionVOList, List contenteVersionVOList, boolean mustBeFirst, boolean includeMetaInfo) throws ConstraintException, SystemException
 	{
         Database db = CastorDatabaseService.getDatabase();
 
 	    beginTransaction(db);
 
         try
         {
             Content content = ContentController.getContentController().getContentWithId(contentId, db);
 
             getContentAndAffectedItemsRecursive(content, stateId, new ArrayList(), new ArrayList(), db, siteNodeVersionVOList, contenteVersionVOList, mustBeFirst, includeMetaInfo);
             
             commitTransaction(db);
         }
         catch(Exception e)
         {
             logger.error("An error occurred so we should not completes the transaction:" + e, e);
             rollbackTransaction(db);
             throw new SystemException(e.getMessage());
         }
 	}
 	
 	private void getContentAndAffectedItemsRecursive(Content content, Integer stateId, List checkedSiteNodes, List checkedContents, Database db, List siteNodeVersionVOList, List contentVersionVOList, boolean mustBeFirst, boolean includeMetaInfo) throws ConstraintException, SystemException, Exception
 	{
 	    checkedSiteNodes.add(content.getId());
         
 	    List contentVersions = getLatestContentVersionWithParent(content.getId(), stateId, db, mustBeFirst);
 	    
 		Iterator contentVersionsIterator = contentVersions.iterator();
 	    while(contentVersionsIterator.hasNext())
 	    {
 	        ContentVersion contentVersion = (ContentVersion)contentVersionsIterator.next();
 	        contentVersionVOList.add(contentVersion.getValueObject());
 	        
 	        List relatedEntities = RegistryController.getController().getMatchingRegistryVOListForReferencingEntity(ContentVersion.class.getName(), contentVersion.getId().toString(), db);
 	        logger.info("relatedEntities:" + relatedEntities);
 	        Iterator relatedEntitiesIterator = relatedEntities.iterator();
 	        
 	        while(relatedEntitiesIterator.hasNext())
 	        {
 	            RegistryVO registryVO = (RegistryVO)relatedEntitiesIterator.next();
 	            logger.info("registryVO:" + registryVO.getEntityName() + ":" + registryVO.getEntityId());
 	            if(registryVO.getEntityName().equals(SiteNode.class.getName()) && !checkedSiteNodes.contains(new Integer(registryVO.getEntityId())))
 	            {
 	                try
 	                {
 		                SiteNode relatedSiteNode = SiteNodeController.getController().getSiteNodeWithId(new Integer(registryVO.getEntityId()), db);
 		                SiteNodeVersion relatedSiteNodeVersion = SiteNodeVersionController.getController().getLatestActiveSiteNodeVersionIfInState(relatedSiteNode, stateId, db);
 		                if(relatedSiteNodeVersion != null && content.getRepository().getId().intValue() == relatedSiteNodeVersion.getOwningSiteNode().getRepository().getId().intValue())
 		                {
 		                    siteNodeVersionVOList.add(relatedSiteNodeVersion.getValueObject());
 		                }
 	                }
 	                catch(Exception e)
 	                {
 	                    logger.warn("The related siteNode with id:" + registryVO.getEntityId() + " could not be loaded.", e);
 	                }
 	                
 	    		    checkedSiteNodes.add(new Integer(registryVO.getEntityId()));
 	            }
 	            else if(registryVO.getEntityName().equals(Content.class.getName()) && !checkedContents.contains(new Integer(registryVO.getEntityId())))
 	            {
 	                try
 	                {
 		                Content relatedContent = ContentController.getContentController().getContentWithId(new Integer(registryVO.getEntityId()), db);
 		                if(includeMetaInfo || (!includeMetaInfo && (relatedContent.getContentTypeDefinition() == null || !relatedContent.getContentTypeDefinition().getName().equalsIgnoreCase("Meta info"))))
 		                {
 			                List relatedContentVersions = ContentVersionController.getContentVersionController().getLatestActiveContentVersionIfInState(relatedContent, stateId, db);
 			                logger.info("relatedContentVersions:" + relatedContentVersions.size());
 			                
 			                Iterator relatedContentVersionsIterator = relatedContentVersions.iterator();
 			                while(relatedContentVersionsIterator.hasNext())
 			                {
 			                    ContentVersion relatedContentVersion = (ContentVersion)relatedContentVersionsIterator.next();
 				                if(relatedContentVersion != null && content.getRepository().getId().intValue() == relatedContentVersion.getOwningContent().getRepository().getId().intValue())
 				                {
 				        	        contentVersionVOList.add(relatedContentVersion.getValueObject());
 				                    logger.info("Added:" + relatedContentVersion.getId());
 					            }
 			
 			                }
 			            }
 	                }
 	                catch(Exception e)
 	                {
 	                    logger.warn("The related content with id:" + registryVO.getEntityId() + " could not be loaded.", e);
 	                }
 	                
 	    		    checkedContents.add(new Integer(registryVO.getEntityId()));
 	            }
 	        }	    
 
 		}
 		
 	    //	  Get the children of this content and do the recursion
 		Collection childContentList = content.getChildren();
 		Iterator cit = childContentList.iterator();
 		while (cit.hasNext())
 		{
 			Content citContent = (Content) cit.next();
 			getContentAndAffectedItemsRecursive(citContent, stateId, checkedSiteNodes, checkedContents, db, siteNodeVersionVOList, contentVersionVOList, mustBeFirst, includeMetaInfo);
 		}
 		
 	}
 
 
 }
