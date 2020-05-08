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
 import org.infoglue.cms.entities.kernel.*;
 import org.infoglue.cms.entities.management.GroupProperties;
 import org.infoglue.cms.entities.management.LanguageVO;
 import org.infoglue.cms.entities.management.RoleProperties;
 import org.infoglue.cms.entities.management.UserProperties;
 import org.infoglue.cms.entities.content.*;
 import org.infoglue.cms.entities.content.impl.simple.*;
 import org.infoglue.cms.exception.Bug;
 import org.infoglue.cms.exception.ConstraintException;
 import org.infoglue.cms.exception.SystemException;
 import org.infoglue.cms.util.ConstraintExceptionBuffer;
 import org.infoglue.cms.util.CmsPropertyHandler;
 import org.infoglue.cms.util.graphics.*;
 import org.infoglue.cms.util.CmsLogger;
 import org.infoglue.deliver.controllers.kernel.impl.simple.LanguageDeliveryController;
 
 import java.util.List;
 import java.util.Iterator;
 import java.util.Collection;
 import java.util.ArrayList;
 import java.io.*;
 import java.net.URLEncoder;
 
 /**
  * @author Mattias Bogeblad
  */
 
 public class DigitalAssetController extends BaseController 
 {
 	
     public static DigitalAssetController getController()
     {
         return new DigitalAssetController();
     }
 
     
    	/**
    	 * returns the digital asset VO
    	 */
    	
    	public static DigitalAssetVO getDigitalAssetVOWithId(Integer digitalAssetId) throws SystemException, Bug
     {
 		return (DigitalAssetVO) getVOWithId(DigitalAssetImpl.class, digitalAssetId);
     }
     
     /**
      * returns a digitalasset
      */
     
     public static DigitalAsset getDigitalAssetWithId(Integer digitalAssetId, Database db) throws SystemException, Bug
     {
 		return (DigitalAsset) getObjectWithId(DigitalAssetImpl.class, digitalAssetId, db);
     }
 
 
    	/**
    	 * This method creates a new digital asset in the database and connects it to the contentVersion it belongs to.
    	 * The asset is send in as an InputStream which castor inserts automatically.
    	 */
 
    	public static DigitalAssetVO create(DigitalAssetVO digitalAssetVO, InputStream is, Integer contentVersionId) throws SystemException
    	{
 		Database db = CastorDatabaseService.getDatabase();
 
		DigitalAsset digitalAsset = null;
		
 		beginTransaction(db);
 		
 		try
 		{
 			ContentVersion contentVersion = ContentVersionController.getContentVersionController().getContentVersionWithId(contentVersionId, db);
 
			create(digitalAssetVO, is, contentVersion, db);
 		    
 			commitTransaction(db);
 		}
 		catch(Exception e)
 		{
 			CmsLogger.logSevere("An error occurred so we should not complete the transaction:" + e, e);
 			rollbackTransaction(db);
 			throw new SystemException(e.getMessage());
 		}		
 				
        return digitalAsset.getValueObject();
    	}
 
    	/**
    	 * This method creates a new digital asset in the database and connects it to the contentVersion it belongs to.
    	 * The asset is send in as an InputStream which castor inserts automatically.
    	 */
 
    	public static DigitalAssetVO create(DigitalAssetVO digitalAssetVO, InputStream is, ContentVersion contentVersion, Database db) throws SystemException, Exception
    	{
 		DigitalAsset digitalAsset = null;
 		
 		Collection contentVersions = new ArrayList();
 		contentVersions.add(contentVersion);
 		CmsLogger.logInfo("Added contentVersion:" + contentVersion.getId());
 	
 		digitalAsset = new DigitalAssetImpl();
 		digitalAsset.setValueObject(digitalAssetVO.createCopy());
 		digitalAsset.setAssetBlob(is);
 		digitalAsset.setContentVersions(contentVersions);
 
 		db.create(digitalAsset);
         
 		//if(contentVersion.getDigitalAssets() == null)
 		//    contentVersion.setDigitalAssets(new ArrayList());
 		
 		contentVersion.getDigitalAssets().add(digitalAsset);
 						
         return digitalAsset.getValueObject();
    	}
 
   	/**
    	 * This method creates a new digital asset in the database and connects it to the contentVersion it belongs to.
    	 * The asset is send in as an InputStream which castor inserts automatically.
    	 */
 
    	public static DigitalAssetVO create(DigitalAssetVO digitalAssetVO, InputStream is, String entity, Integer entityId) throws ConstraintException, SystemException
    	{
 		Database db = CastorDatabaseService.getDatabase();
         ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
 
 		DigitalAsset digitalAsset = null;
 		
 		beginTransaction(db);
 		
 		try
 		{
 		    if(entity.equalsIgnoreCase("ContentVersion"))
 		    {
 				ContentVersion contentVersion = ContentVersionController.getContentVersionController().getContentVersionWithId(entityId, db);
 				Collection contentVersions = new ArrayList();
 				contentVersions.add(contentVersion);
 				CmsLogger.logInfo("Added contentVersion:" + contentVersion.getId());
 	   		
 				digitalAsset = new DigitalAssetImpl();
 				digitalAsset.setValueObject(digitalAssetVO);
 				digitalAsset.setAssetBlob(is);
 				digitalAsset.setContentVersions(contentVersions);
 
 				db.create(digitalAsset);
 	        
 				contentVersion.getDigitalAssets().add(digitalAsset);		        
 		    }
 		    else if(entity.equalsIgnoreCase(UserProperties.class.getName()))
 		    {
 				UserProperties userProperties = UserPropertiesController.getController().getUserPropertiesWithId(entityId, db);
 				Collection userPropertiesList = new ArrayList();
 				userPropertiesList.add(userProperties);
 				CmsLogger.logInfo("Added userProperties:" + userProperties.getId());
 	   		
 				digitalAsset = new DigitalAssetImpl();
 				digitalAsset.setValueObject(digitalAssetVO);
 				digitalAsset.setAssetBlob(is);
 				digitalAsset.setUserProperties(userPropertiesList);
 				
 				db.create(digitalAsset);
 	        
 				userProperties.getDigitalAssets().add(digitalAsset);		        
 		    }
 		    else if(entity.equalsIgnoreCase(RoleProperties.class.getName()))
 		    {
 		        RoleProperties roleProperties = RolePropertiesController.getController().getRolePropertiesWithId(entityId, db);
 				Collection rolePropertiesList = new ArrayList();
 				rolePropertiesList.add(roleProperties);
 				CmsLogger.logInfo("Added roleProperties:" + roleProperties.getId());
 	   		
 				digitalAsset = new DigitalAssetImpl();
 				digitalAsset.setValueObject(digitalAssetVO);
 				digitalAsset.setAssetBlob(is);
 				digitalAsset.setRoleProperties(rolePropertiesList);
 				
 				db.create(digitalAsset);
 	        
 				roleProperties.getDigitalAssets().add(digitalAsset);		        		        
 		    }
 		    else if(entity.equalsIgnoreCase(GroupProperties.class.getName()))
 		    {
 		        GroupProperties groupProperties = GroupPropertiesController.getController().getGroupPropertiesWithId(entityId, db);
 				Collection groupPropertiesList = new ArrayList();
 				groupPropertiesList.add(groupProperties);
 				CmsLogger.logInfo("Added groupProperties:" + groupProperties.getId());
 	   		
 				digitalAsset = new DigitalAssetImpl();
 				digitalAsset.setValueObject(digitalAssetVO);
 				digitalAsset.setAssetBlob(is);
 				digitalAsset.setGroupProperties(groupPropertiesList);
 				
 				db.create(digitalAsset);
 	        
 				groupProperties.getDigitalAssets().add(digitalAsset);		        		        
 		    }
 		
 			commitTransaction(db);
 		}
 		catch(Exception e)
 		{
 			CmsLogger.logSevere("An error occurred so we should not complete the transaction:" + e, e);
 			rollbackTransaction(db);
 			throw new SystemException(e.getMessage());
 		}		
 				
         return digitalAsset.getValueObject();
    	}
 
    	/**
    	 * This method creates a new digital asset in the database.
    	 * The asset is send in as an InputStream which castor inserts automatically.
    	 */
 
    	public DigitalAsset create(Database db, DigitalAssetVO digitalAssetVO, InputStream is) throws SystemException, Exception
    	{
 		DigitalAsset digitalAsset = new DigitalAssetImpl();
 		digitalAsset.setValueObject(digitalAssetVO);
 		digitalAsset.setAssetBlob(is);
 
 		db.create(digitalAsset);
 				
         return digitalAsset;
    	}
 
    	/**
    	 * This method creates a new digital asset in the database and connects it to the contentVersion it belongs to.
    	 * The asset is send in as an InputStream which castor inserts automatically.
    	 */
 
 	public DigitalAssetVO createByCopy(Integer originalContentVersionId, String oldAssetKey, Integer newContentVersionId, String newAssetKey, Database db) throws ConstraintException, SystemException
 	{
 		CmsLogger.logInfo("Creating by copying....");
 		CmsLogger.logInfo("originalContentVersionId:" + originalContentVersionId);
 		CmsLogger.logInfo("oldAssetKey:" + oldAssetKey);
 		CmsLogger.logInfo("newContentVersionId:" + newContentVersionId);
 		CmsLogger.logInfo("newAssetKey:" + newAssetKey);
 		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
 		
 		DigitalAsset oldDigitalAsset = getDigitalAsset(originalContentVersionId, oldAssetKey, db);
 		
 		ContentVersion contentVersion = ContentVersionController.getContentVersionController().getContentVersionWithId(newContentVersionId, db);
 		Collection contentVersions = new ArrayList();
 		contentVersions.add(contentVersion);
 		CmsLogger.logInfo("Added contentVersion:" + contentVersion.getId());
    		
 		DigitalAssetVO digitalAssetVO = new DigitalAssetVO();
 		digitalAssetVO.setAssetContentType(oldDigitalAsset.getAssetContentType());
 		digitalAssetVO.setAssetFileName(oldDigitalAsset.getAssetFileName());
 		digitalAssetVO.setAssetFilePath(oldDigitalAsset.getAssetFilePath());
 		digitalAssetVO.setAssetFileSize(oldDigitalAsset.getAssetFileSize());
 		digitalAssetVO.setAssetKey(newAssetKey);
 		
 		DigitalAsset digitalAsset = new DigitalAssetImpl();
 		digitalAsset.setValueObject(digitalAssetVO);
 		digitalAsset.setAssetBlob(oldDigitalAsset.getAssetBlob());
 		digitalAsset.setContentVersions(contentVersions);
 		
 		try
 		{
 			db.create(digitalAsset);
 		}
 		catch(Exception e)
 		{
 			CmsLogger.logSevere("An error occurred so we should not complete the transaction:" + e, e);
 			throw new SystemException(e.getMessage());
 		}
 		//contentVersion.getDigitalAssets().add(digitalAsset);
 		
 		return digitalAsset.getValueObject();
 	}
 
 	/**
 	 * This method gets a asset with a special key inside the given transaction.
 	 */
 	
 	public DigitalAsset getDigitalAsset(Integer contentVersionId, String assetKey, Database db) throws SystemException
 	{
 		DigitalAsset digitalAsset = null;
 		
 		ContentVersion contentVersion = ContentVersionController.getContentVersionController().getContentVersionWithId(contentVersionId, db);
 		Collection digitalAssets = contentVersion.getDigitalAssets();
 		Iterator assetIterator = digitalAssets.iterator();
 		while(assetIterator.hasNext())
 		{
 			DigitalAsset currentDigitalAsset = (DigitalAsset)assetIterator.next();
 			if(currentDigitalAsset.getAssetKey().equals(assetKey))
 			{
 				digitalAsset = currentDigitalAsset;
 				break;
 			}
 		}
 		
 		return digitalAsset;
 	}
 	
     
    	/**
    	 * This method deletes a digital asset in the database.
    	 */
 
    	public static void delete(Integer digitalAssetId) throws ConstraintException, SystemException
    	{
 		deleteEntity(DigitalAssetImpl.class, digitalAssetId);
    	}
 
    	/**
    	 * This method deletes a digital asset in the database.
    	 */
 
    	public void delete(Integer digitalAssetId, Database db) throws ConstraintException, SystemException
    	{
 		deleteEntity(DigitalAssetImpl.class, digitalAssetId, db);
    	}
 
    	/**
    	 * This method deletes a digital asset in the database.
    	 */
 
    	public void delete(Integer digitalAssetId, String entity, Integer entityId) throws ConstraintException, SystemException
    	{
     	Database db = CastorDatabaseService.getDatabase();
         ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
 
         beginTransaction(db);
 
         try
         {           
     		DigitalAsset digitalAsset = DigitalAssetController.getDigitalAssetWithId(digitalAssetId, db);			
     		
     		if(entity.equalsIgnoreCase("ContentVersion"))
                 ContentVersionController.getContentVersionController().deleteDigitalAssetRelation(entityId, digitalAsset, db);
             else if(entity.equalsIgnoreCase(UserProperties.class.getName()))
                 UserPropertiesController.getController().deleteDigitalAssetRelation(entityId, digitalAsset, db);
             else if(entity.equalsIgnoreCase(RoleProperties.class.getName()))
                 RolePropertiesController.getController().deleteDigitalAssetRelation(entityId, digitalAsset, db);
             else if(entity.equalsIgnoreCase(GroupProperties.class.getName()))
                 GroupPropertiesController.getController().deleteDigitalAssetRelation(entityId, digitalAsset, db);
 
             db.remove(digitalAsset);
 
             commitTransaction(db);
         }
         catch(Exception e)
         {
             CmsLogger.logSevere("An error occurred so we should not completes the transaction:" + e, e);
             rollbackTransaction(db);
             throw new SystemException(e.getMessage());
         }
    	}
    	
 
    	/**
 	 * This method removes all images in the digitalAsset directory which belongs to a certain digital asset.
 	 */
 	
 	public static void deleteCachedDigitalAssets(Integer digitalAssetId) throws SystemException, Exception
 	{ 
 		try
 		{
 			File assetDirectory = new File(CmsPropertyHandler.getProperty("digitalAssetPath"));
 			File[] files = assetDirectory.listFiles(new FilenameFilterImpl(digitalAssetId.toString())); 	
 			for(int i=0; i<files.length; i++)
 			{
 				File file = files[i];
 				file.delete();
 			}
 	
 		}
 		catch(Exception e)
 		{
 			CmsLogger.logSevere("Could not delete the assets for the digitalAsset " + digitalAssetId + ":" + e.getMessage(), e);
 		}
 	}
 	
    	/**
    	 * This method updates a digital asset in the database.
    	 */
    	
    	public static DigitalAssetVO update(DigitalAssetVO digitalAssetVO, InputStream is) throws ConstraintException, SystemException
     {
 		Database db = CastorDatabaseService.getDatabase();
 		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
 		
 		DigitalAsset digitalAsset = null;
 		
 		beginTransaction(db);
 
 		try
 		{
 			digitalAsset = getDigitalAssetWithId(digitalAssetVO.getId(), db);
 			
 			digitalAsset.setValueObject(digitalAssetVO);
 			if(is != null)
 			    digitalAsset.setAssetBlob(is);
 
 			commitTransaction(db);
 		}
 		catch(Exception e)
 		{
 			CmsLogger.logSevere("An error occurred so we should not complete the transaction:" + e, e);
 			rollbackTransaction(db);
 			throw new SystemException(e.getMessage());
 		}
 
 		return digitalAsset.getValueObject();		
     } 
     
    	/**
 	 * This method deletes all contentVersions for the content sent in and also clears all the digital Assets.
 	 * Should not be available probably as you might destroy for other versions and other contents.
 	 * /
 	/*
 	public static void deleteDigitalAssetsForContentVersionWithId(Integer contentVersionId) throws ConstraintException, SystemException, Bug
     {
     	Database db = CastorDatabaseService.getDatabase();
         ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
 
         beginTransaction(db);
 		List digitalAssets = new ArrayList();
         try
         {        
             ContentVersion contentVersion = ContentVersionController.getContentVersionWithId(contentVersionId, db);
             
         	Collection digitalAssetList = contentVersion.getDigitalAssets();
 			Iterator assets = digitalAssetList.iterator();
 			while (assets.hasNext()) 
             {
             	DigitalAsset digitalAsset = (DigitalAsset)assets.next();
 				digitalAssets.add(digitalAsset.getValueObject());
             }
             
             commitTransaction(db);
         }
         catch(Exception e)
         {
         	e.printStackTrace();
             CmsLogger.logSevere("An error occurred so we should not completes the transaction:" + e, e);
             rollbackTransaction(db);
             throw new SystemException(e.getMessage());
         }
 
 		Iterator i = digitalAssets.iterator();
 		while(i.hasNext())
 		{
 			DigitalAssetVO digitalAssetVO = (DigitalAssetVO)i.next();
 			CmsLogger.logInfo("Deleting digitalAsset:" + digitalAssetVO.getDigitalAssetId());
 			delete(digitalAssetVO.getDigitalAssetId());
 		}    	
 
     }
 	*/
 
 	/**
 	 * This method should return a list of those digital assets the contentVersion has.
 	 */
 	   	
 	public static List getDigitalAssetVOList(Integer contentVersionId) throws SystemException, Bug
     {
     	Database db = CastorDatabaseService.getDatabase();
         ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
 
     	List digitalAssetVOList = new ArrayList();
 
         beginTransaction(db);
 
         try
         {
 			ContentVersion contentVersion = ContentVersionController.getContentVersionController().getReadOnlyContentVersionWithId(contentVersionId, db); 
 			if(contentVersion != null)
 			{
 				Collection digitalAssets = contentVersion.getDigitalAssets();
 				digitalAssetVOList = toVOList(digitalAssets);
 			}
 			            
             commitTransaction(db);
         }
         catch(Exception e)
         {
             CmsLogger.logInfo("An error occurred when we tried to fetch the list of digitalAssets belonging to this contentVersion:" + e);
             e.printStackTrace();
             rollbackTransaction(db);
             throw new SystemException(e.getMessage());
         }
     	
 		return digitalAssetVOList;
     }
 
 
 	/**
 	 * This method should return a String containing the URL for this digital asset.
 	 */
 	   	
 	public static List getDigitalAssetVOList(Integer contentId, Integer languageId, boolean useLanguageFallback) throws SystemException, Bug
     {
     	Database db = CastorDatabaseService.getDatabase();
         ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
 
         List digitalAssetVOList = null;
 
         beginTransaction(db);
 
         try
         {
             digitalAssetVOList = getDigitalAssetVOList(contentId, languageId, useLanguageFallback, db);
         	
             commitTransaction(db);
         }
         catch(Exception e)
         {
             e.printStackTrace();
             CmsLogger.logInfo("An error occurred when we tried to cache and show the digital asset:" + e);
             rollbackTransaction(db);
             throw new SystemException(e.getMessage());
         }
     	
 		return digitalAssetVOList;
     }
 
 	/**
 	 * This method should return a String containing the URL for this digital asset.
 	 */
 	   	
 	public static List getDigitalAssetVOList(Integer contentId, Integer languageId, boolean useLanguageFallback, Database db) throws SystemException, Bug, Exception
     {
 	    List digitalAssetVOList = new ArrayList();
 
     	Content content = ContentController.getContentController().getContentWithId(contentId, db);
     	CmsLogger.logInfo("content:" + content.getName());
     	CmsLogger.logInfo("repositoryId:" + content.getRepository().getId());
     	CmsLogger.logInfo("languageId:" + languageId);
     	ContentVersion contentVersion = ContentVersionController.getContentVersionController().getLatestActiveContentVersion(contentId, languageId, db);
     	LanguageVO masterLanguageVO = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForRepository(content.getRepository().getRepositoryId(), db);	
 		
     	CmsLogger.logInfo("contentVersion:" + contentVersion);
 		if(contentVersion != null)
 		{
 		    Collection digitalAssets = contentVersion.getDigitalAssets();
 			digitalAssetVOList = toModifiableVOList(digitalAssets);
 			
 			CmsLogger.logInfo("digitalAssetVOList:" + digitalAssetVOList.size());
 			if(useLanguageFallback && languageId.intValue() != masterLanguageVO.getId().intValue())
 			{
 			    List masterDigitalAssetVOList = getDigitalAssetVOList(contentId, masterLanguageVO.getId(), useLanguageFallback, db);
 			    Iterator digitalAssetVOListIterator = digitalAssetVOList.iterator();
 			    while(digitalAssetVOListIterator.hasNext())
 			    {
 			        DigitalAssetVO currentDigitalAssetVO = (DigitalAssetVO)digitalAssetVOListIterator.next();
 			        
 			        Iterator masterDigitalAssetVOListIterator = masterDigitalAssetVOList.iterator();
 				    while(masterDigitalAssetVOListIterator.hasNext())
 				    {
 				        DigitalAssetVO masterCurrentDigitalAssetVO = (DigitalAssetVO)masterDigitalAssetVOListIterator.next();
 				        if(currentDigitalAssetVO.getAssetKey().equalsIgnoreCase(masterCurrentDigitalAssetVO.getAssetKey()))
 				            masterDigitalAssetVOListIterator.remove();
 				    }
 			    }
 			    digitalAssetVOList.addAll(masterDigitalAssetVOList);
 			}
 		}
 		else if(useLanguageFallback && languageId.intValue() != masterLanguageVO.getId().intValue())
 		{
 		    contentVersion = ContentVersionController.getContentVersionController().getLatestActiveContentVersion(contentId, masterLanguageVO.getId(), db);
 	    	
 	    	CmsLogger.logInfo("contentVersion:" + contentVersion);
 			if(contentVersion != null)
 			{
 			    Collection digitalAssets = contentVersion.getDigitalAssets();
 				digitalAssetVOList = toModifiableVOList(digitalAssets);				
 			}
 		}
 		
 		return digitalAssetVOList;
     }
 	
 	
 	
 	/**
 	 * This method should return a String containing the URL for this digital asset.
 	 */
 
 	public static String getDigitalAssetUrl(Integer digitalAssetId) throws SystemException, Bug
     {
     	Database db = CastorDatabaseService.getDatabase();
         ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
 
     	String assetUrl = null;
 
         beginTransaction(db);
 
         try
         {
 			DigitalAsset digitalAsset = getDigitalAssetWithId(digitalAssetId, db);
 						
 			if(digitalAsset != null)
 			{
 				CmsLogger.logInfo("Found a digital asset:" + digitalAsset.getAssetFileName());
 				String fileName = digitalAsset.getDigitalAssetId() + "_" + digitalAsset.getAssetFileName();
 				//String filePath = digitalAsset.getAssetFilePath();
 				String filePath = CmsPropertyHandler.getProperty("digitalAssetPath");
 				dumpDigitalAsset(digitalAsset, fileName, filePath);
 				assetUrl = CmsPropertyHandler.getProperty("webServerAddress") + "/" + CmsPropertyHandler.getProperty("digitalAssetBaseUrl") + "/" + fileName;
 			}			       	
 
             commitTransaction(db);
         }
         catch(Exception e)
         {
             CmsLogger.logInfo("An error occurred when we tried to cache and show the digital asset:" + e);
             e.printStackTrace();
             rollbackTransaction(db);
             throw new SystemException(e.getMessage());
         }
     	
 		return assetUrl;
     }
 
 	/**
 	 * This method should return a String containing the URL for this digital asset.
 	 */
 
 	public String getDigitalAssetUrl(DigitalAssetVO digitalAssetVO, Database db) throws SystemException, Bug, Exception
     {
     	String assetUrl = null;
 
 		DigitalAsset digitalAsset = getDigitalAssetWithId(digitalAssetVO.getId(), db);
 					
 		if(digitalAsset != null)
 		{
 			CmsLogger.logInfo("Found a digital asset:" + digitalAsset.getAssetFileName());
 			String fileName = digitalAsset.getDigitalAssetId() + "_" + digitalAsset.getAssetFileName();
 			String filePath = CmsPropertyHandler.getProperty("digitalAssetPath");
 			dumpDigitalAsset(digitalAsset, fileName, filePath);
 			assetUrl = CmsPropertyHandler.getProperty("webServerAddress") + "/" + CmsPropertyHandler.getProperty("digitalAssetBaseUrl") + "/" + fileName;
 		}			       	
     	
 		return assetUrl;
     }
 
 
 	/**
 	 * This method should return a String containing the URL for this digital assets icon/thumbnail.
 	 * In the case of an image the downscaled image is returned - otherwise an icon that represents the
 	 * content-type of the file. It always fetches the latest one if several assets exists.
 	 */
 	   	
 	public static String getDigitalAssetThumbnailUrl(Integer digitalAssetId) throws SystemException, Bug
     {
     	Database db = CastorDatabaseService.getDatabase();
         ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
 
     	String assetUrl = null;
 
         beginTransaction(db);
 
         try
         {
 			DigitalAsset digitalAsset = getDigitalAssetWithId(digitalAssetId, db);
 			
 			if(digitalAsset != null)
 			{
 				CmsLogger.logInfo("Found a digital asset:" + digitalAsset.getAssetFileName());
 				String contentType = digitalAsset.getAssetContentType();
 				
 				if(contentType.equalsIgnoreCase("image/gif") || contentType.equalsIgnoreCase("image/jpg") || contentType.equalsIgnoreCase("image/pjpeg") || contentType.equalsIgnoreCase("image/jpeg"))
 				{
 					String fileName = digitalAsset.getDigitalAssetId() + "_" + digitalAsset.getAssetFileName();
 					CmsLogger.logInfo("fileName:" + fileName);
 					String filePath = CmsPropertyHandler.getProperty("digitalAssetPath");
 					CmsLogger.logInfo("filePath:" + filePath);
 					String thumbnailFileName = digitalAsset.getDigitalAssetId() + "_thumbnail_" + digitalAsset.getAssetFileName();
 					//String thumbnailFileName = "thumbnail_" + fileName;
 					File thumbnailFile = new File(filePath + File.separator + thumbnailFileName);
 					if(!thumbnailFile.exists())
 					{
 						CmsLogger.logInfo("transforming...");
 						ThumbnailGenerator tg = new ThumbnailGenerator();
 						tg.transform(filePath + File.separator + fileName, filePath + File.separator + thumbnailFileName, 75, 75, 100);
 						CmsLogger.logInfo("transform done...");
 					}
 					assetUrl = CmsPropertyHandler.getProperty("webServerAddress") + "/" + CmsPropertyHandler.getProperty("digitalAssetBaseUrl") + "/" + thumbnailFileName;
 					CmsLogger.logInfo("assetUrl:" + assetUrl);
 				}
 				else
 				{
 					if(contentType.equalsIgnoreCase("application/pdf"))
 					{
 						assetUrl = "images/pdf.gif"; 
 					}
 					else if(contentType.equalsIgnoreCase("application/msword"))
 					{
 						assetUrl = "images/msword.gif"; 
 					}
 					else if(contentType.equalsIgnoreCase("application/vnd.ms-excel"))
 					{
 						assetUrl = "images/msexcel.gif"; 
 					}
 					else if(contentType.equalsIgnoreCase("application/vnd.ms-powerpoint"))
 					{
 						assetUrl = "images/mspowerpoint.gif"; 
 					}
 					else
 					{
 						assetUrl = "images/digitalAsset.gif"; 
 					}		
 				}	
 			}	
 			            
             commitTransaction(db);
         }
         catch(Exception e)
         {
             CmsLogger.logInfo("An error occurred when we tried to cache and show the digital asset thumbnail:" + e);
             rollbackTransaction(db);
             throw new SystemException(e.getMessage());
         }
     	
 		return assetUrl;
     }
 
    	
 	/**
 	 * This method should return a String containing the URL for this digital asset.
 	 */
 	   	
 	public static String getDigitalAssetUrl(Integer contentId, Integer languageId) throws SystemException, Bug
     {
     	Database db = CastorDatabaseService.getDatabase();
         ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
 
     	String assetUrl = null;
 
         beginTransaction(db);
 
         try
         {
 			ContentVersion contentVersion = ContentVersionController.getContentVersionController().getLatestContentVersion(contentId, languageId, db); 
 			if(contentVersion != null)
 			{
 				DigitalAsset digitalAsset = getLatestDigitalAsset(contentVersion);
 				
 				if(digitalAsset != null)
 				{
 					CmsLogger.logInfo("Found a digital asset:" + digitalAsset.getAssetFileName());
 					String fileName = digitalAsset.getDigitalAssetId() + "_" + digitalAsset.getAssetFileName();
 					//String filePath = digitalAsset.getAssetFilePath();
 					String filePath = CmsPropertyHandler.getProperty("digitalAssetPath");
 					
 					dumpDigitalAsset(digitalAsset, fileName, filePath);
 					assetUrl = CmsPropertyHandler.getProperty("webServerAddress") + "/" + CmsPropertyHandler.getProperty("digitalAssetBaseUrl") + "/" + fileName;
 				}			       	
 			}
 			            
             commitTransaction(db);
         }
         catch(Exception e)
         {
             CmsLogger.logInfo("An error occurred when we tried to cache and show the digital asset:" + e);
             e.printStackTrace();
             rollbackTransaction(db);
             throw new SystemException(e.getMessage());
         }
     	
 		return assetUrl;
     }
 
 	/**
 	 * This method should return a String containing the URL for this digital asset.
 	 */
 	   	
 	public static String getDigitalAssetUrl(Integer contentId, Integer languageId, String assetKey, boolean useLanguageFallback) throws SystemException, Bug
     {
     	Database db = CastorDatabaseService.getDatabase();
         ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
 
     	String assetUrl = null;
 
         beginTransaction(db);
 
         try
         {
         	assetUrl = getDigitalAssetUrl(contentId, languageId, assetKey, useLanguageFallback, db);
         	
             commitTransaction(db);
         }
         catch(Exception e)
         {
             CmsLogger.logInfo("An error occurred when we tried to cache and show the digital asset:" + e);
             e.printStackTrace();
             rollbackTransaction(db);
             throw new SystemException(e.getMessage());
         }
     	
 		return assetUrl;
     }
 
 	/**
 	 * This method should return a String containing the URL for this digital asset.
 	 */
 	   	
 	public static String getDigitalAssetUrl(Integer contentId, Integer languageId, String assetKey, boolean useLanguageFallback, Database db) throws SystemException, Bug, Exception
     {
     	String assetUrl = null;
 
     	Content content = ContentController.getContentController().getContentWithId(contentId, db);
     	CmsLogger.logInfo("content:" + content.getName());
     	CmsLogger.logInfo("repositoryId:" + content.getRepository().getId());
     	CmsLogger.logInfo("languageId:" + languageId);
     	CmsLogger.logInfo("assetKey:" + assetKey);
     	ContentVersion contentVersion = ContentVersionController.getContentVersionController().getLatestActiveContentVersion(contentId, languageId, db);
     	LanguageVO masterLanguageVO = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForRepository(content.getRepository().getRepositoryId(), db);	
 		CmsLogger.logInfo("contentVersion:" + contentVersion);
 		if(contentVersion != null)
 		{
 			DigitalAsset digitalAsset = getLatestDigitalAsset(contentVersion, assetKey);
 			CmsLogger.logInfo("digitalAsset:" + digitalAsset);
 			if(digitalAsset != null)
 			{
 				CmsLogger.logInfo("digitalAsset:" + digitalAsset.getAssetKey());
 				CmsLogger.logInfo("Found a digital asset:" + digitalAsset.getAssetFileName());
 				String fileName = digitalAsset.getDigitalAssetId() + "_" + digitalAsset.getAssetFileName();
 				String filePath = CmsPropertyHandler.getProperty("digitalAssetPath");
 				
 				dumpDigitalAsset(digitalAsset, fileName, filePath);
 				assetUrl = CmsPropertyHandler.getProperty("webServerAddress") + "/" + CmsPropertyHandler.getProperty("digitalAssetBaseUrl") + "/" + fileName;
 			}
 			else
 			{
 				//LanguageVO masterLanguageVO = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForRepository(content.getRepository().getRepositoryId(), db);
 				if(useLanguageFallback && languageId.intValue() != masterLanguageVO.getId().intValue())
 					return getDigitalAssetUrl(contentId, masterLanguageVO.getId(), assetKey, useLanguageFallback, db);
 			}
 		}
 		else if(useLanguageFallback && languageId.intValue() != masterLanguageVO.getId().intValue())
 		{
 		    contentVersion = ContentVersionController.getContentVersionController().getLatestActiveContentVersion(contentId, masterLanguageVO.getId(), db);
 	    	
 	    	CmsLogger.logInfo("contentVersion:" + contentVersion);
 			if(contentVersion != null)
 			{
 			    DigitalAsset digitalAsset = getLatestDigitalAsset(contentVersion, assetKey);
 				CmsLogger.logInfo("digitalAsset:" + digitalAsset);
 				if(digitalAsset != null)
 				{
 					CmsLogger.logInfo("digitalAsset:" + digitalAsset.getAssetKey());
 					CmsLogger.logInfo("Found a digital asset:" + digitalAsset.getAssetFileName());
 					String fileName = digitalAsset.getDigitalAssetId() + "_" + digitalAsset.getAssetFileName();
 					String filePath = CmsPropertyHandler.getProperty("digitalAssetPath");
 					
 					dumpDigitalAsset(digitalAsset, fileName, filePath);
 					assetUrl = CmsPropertyHandler.getProperty("webServerAddress") + "/" + CmsPropertyHandler.getProperty("digitalAssetBaseUrl") + "/" + fileName;
 				}
 			}
 		}
 		
 		return assetUrl;
     }
 
 	/**
 	 * This method should return a String containing the URL for this digital asset.
 	 */
 	   	
 	public static DigitalAssetVO getDigitalAssetVO(Integer contentId, Integer languageId, String assetKey, boolean useLanguageFallback) throws SystemException, Bug
     {
     	Database db = CastorDatabaseService.getDatabase();
         ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
 
     	DigitalAssetVO digitalAssetVO = null;
 
         beginTransaction(db);
 
         try
         {
         	digitalAssetVO = getDigitalAssetVO(contentId, languageId, assetKey, useLanguageFallback, db);
         	
             commitTransaction(db);
         }
         catch(Exception e)
         {
             CmsLogger.logInfo("An error occurred when we tried to get a digitalAssetVO:" + e);
             e.printStackTrace();
             rollbackTransaction(db);
             throw new SystemException(e.getMessage());
         }
     	
 		return digitalAssetVO;
     }
 
 	/**
 	 * This method should return a DigitalAssetVO
 	 */
 	   	
 	public static DigitalAssetVO getDigitalAssetVO(Integer contentId, Integer languageId, String assetKey, boolean useLanguageFallback, Database db) throws SystemException, Bug, Exception
     {
     	DigitalAssetVO digitalAssetVO = null;
 
     	Content content = ContentController.getContentController().getContentWithId(contentId, db);
     	ContentVersion contentVersion = ContentVersionController.getContentVersionController().getLatestActiveContentVersion(contentId, languageId, db);
 		if(contentVersion != null)
 		{
 			DigitalAsset digitalAsset = getLatestDigitalAsset(contentVersion, assetKey);
 			if(digitalAsset != null)
 			{
 				digitalAssetVO = digitalAsset.getValueObject();
 			}
 			else
 			{
 				LanguageVO masterLanguageVO = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForRepository(content.getRepository().getRepositoryId(), db);
 				if(useLanguageFallback && languageId.intValue() != masterLanguageVO.getId().intValue())
 					return getDigitalAssetVO(contentId, masterLanguageVO.getId(), assetKey, useLanguageFallback, db);
 			}
 		}
 		
 		return digitalAssetVO;
     }
 
 	
 	
 	/**
 	 * This method should return a String containing the URL for this digital assets icon/thumbnail.
 	 * In the case of an image the downscaled image is returned - otherwise an icon that represents the
 	 * content-type of the file. It always fetches the latest one if several assets exists.
 	 */
 	   	
 	public static String getDigitalAssetThumbnailUrl(Integer contentId, Integer languageId) throws SystemException, Bug
     {
     	Database db = CastorDatabaseService.getDatabase();
         ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
 
     	String assetUrl = null;
 
         beginTransaction(db);
 
         try
         {
 			ContentVersion contentVersion = ContentVersionController.getContentVersionController().getLatestContentVersion(contentId, languageId, db); 
 			if(contentVersion != null)
 			{
 				DigitalAsset digitalAsset = getLatestDigitalAsset(contentVersion);
 				
 				if(digitalAsset != null)
 				{
 					CmsLogger.logInfo("Found a digital asset:" + digitalAsset.getAssetFileName());
 					String contentType = digitalAsset.getAssetContentType();
 					
 					if(contentType.equalsIgnoreCase("image/gif") || contentType.equalsIgnoreCase("image/jpg"))
 					{
 						String fileName = digitalAsset.getDigitalAssetId() + "_" + digitalAsset.getAssetFileName();
 						//String filePath = digitalAsset.getAssetFilePath();
 						String filePath = CmsPropertyHandler.getProperty("digitalAssetPath");
 						String thumbnailFileName = digitalAsset.getDigitalAssetId() + "_thumbnail_" + digitalAsset.getAssetFileName();
 						//String thumbnailFileName = "thumbnail_" + fileName;
 						File thumbnailFile = new File(filePath + File.separator + thumbnailFileName);
 						if(!thumbnailFile.exists())
 						{
 							ThumbnailGenerator tg = new ThumbnailGenerator();
 							tg.transform(filePath + File.separator + fileName, filePath + File.separator + thumbnailFileName, 150, 150, 100);
 						}
 						assetUrl = CmsPropertyHandler.getProperty("webServerAddress") + "/" + CmsPropertyHandler.getProperty("digitalAssetBaseUrl") + "/" + thumbnailFileName;
 					}
 					else
 					{
 						if(contentType.equalsIgnoreCase("application/pdf"))
 						{
 							assetUrl = "images/pdf.gif"; 
 						}
 						else if(contentType.equalsIgnoreCase("application/msword"))
 						{
 							assetUrl = "images/msword.gif"; 
 						}
 						else if(contentType.equalsIgnoreCase("application/vnd.ms-excel"))
 						{
 							assetUrl = "images/msexcel.gif"; 
 						}
 						else if(contentType.equalsIgnoreCase("application/vnd.ms-powerpoint"))
 						{
 							assetUrl = "images/mspowerpoint.gif"; 
 						}
 						else
 						{
 							assetUrl = "images/digitalAsset.gif"; 
 						}		
 					}	
 				}	
 				else
 				{
 					assetUrl = "images/notDefined.gif";
 				}		       	
 			}
 			            
             commitTransaction(db);
         }
         catch(Exception e)
         {
             CmsLogger.logInfo("An error occurred when we tried to cache and show the digital asset thumbnail:" + e);
             rollbackTransaction(db);
             throw new SystemException(e.getMessage());
         }
     	
 		return assetUrl;
     }
 
 	/**
 	 * Returns the latest digital asset for a contentversion.
 	 */
 	
 	private static DigitalAsset getLatestDigitalAsset(ContentVersion contentVersion)
 	{
 		Collection digitalAssets = contentVersion.getDigitalAssets();
 		Iterator iterator = digitalAssets.iterator();
 		
 		DigitalAsset digitalAsset = null;
 		while(iterator.hasNext())
 		{
 			DigitalAsset currentDigitalAsset = (DigitalAsset)iterator.next();	
 			if(digitalAsset == null || currentDigitalAsset.getDigitalAssetId().intValue() > digitalAsset.getDigitalAssetId().intValue())
 				digitalAsset = currentDigitalAsset;
 		}
 		return digitalAsset;
 	}
 
 	/**
 	 * Returns the latest digital asset for a contentversion.
 	 */
 	
 	private static DigitalAsset getLatestDigitalAsset(ContentVersion contentVersion, String assetKey)
 	{
 		Collection digitalAssets = contentVersion.getDigitalAssets();
 		Iterator iterator = digitalAssets.iterator();
 		
 		DigitalAsset digitalAsset = null;
 		while(iterator.hasNext())
 		{
 			DigitalAsset currentDigitalAsset = (DigitalAsset)iterator.next();	
 			if((digitalAsset == null || currentDigitalAsset.getDigitalAssetId().intValue() > digitalAsset.getDigitalAssetId().intValue()) && currentDigitalAsset.getAssetKey().equalsIgnoreCase(assetKey))
 				digitalAsset = currentDigitalAsset;
 		}
 		return digitalAsset;
 	}
 
 
 	/**
 	 * This method checks if the given file exists on disk. If it does it's ignored because
 	 * that means that the file is allready cached on the server. If not we take out the stream from the 
 	 * digitalAsset-object and dumps it.
 	 */
    	
 	public static void dumpDigitalAsset(DigitalAsset digitalAsset, String fileName, String filePath) throws Exception
 	{
 		long timer = System.currentTimeMillis();
 
 		File outputFile = new File(filePath + File.separator + fileName);
 		if(outputFile.exists())
 		{
 			CmsLogger.logInfo("The file allready exists so we don't need to dump it again..");
 			return;
 		}
 		
 		FileOutputStream fis = new FileOutputStream(outputFile);
 		BufferedOutputStream bos = new BufferedOutputStream(fis);
 		
 		BufferedInputStream bis = new BufferedInputStream(digitalAsset.getAssetBlob());
 		
 		int character;
 		while ((character = bis.read()) != -1)
 		{
 			bos.write(character);
 		}
 		bos.flush();
 		
 		bis.close();
 		fis.close();
 		bos.close();
 		CmsLogger.logInfo("Time for dumping file " + fileName + ":" + (System.currentTimeMillis() - timer));
 	}
 
 	/**
 	 * This is a method that gives the user back an newly initialized ValueObject for this entity that the controller
 	 * is handling.
 	 */
 
 	public BaseEntityVO getNewVO()
 	{
 		return new DigitalAssetVO();
 	}
 
 }
 
 class FilenameFilterImpl implements FilenameFilter 
 {
 	private String filter = ".";
 	
 	public FilenameFilterImpl(String aFilter)
 	{
 		filter = aFilter;
 	}
 	
 	public boolean accept(File dir, String name) 
 	{
     	return name.startsWith(filter);
 	}
 };
