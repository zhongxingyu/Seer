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
 
 package org.infoglue.cms.applications.managementtool.actions;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.exolab.castor.jdo.Database;
 import org.exolab.castor.jdo.TransactionNotInProgressException;
 import org.exolab.castor.mapping.Mapping;
 import org.exolab.castor.xml.Unmarshaller;
 import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
 import org.infoglue.cms.controllers.kernel.impl.simple.AvailableServiceBindingController;
 import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;
 import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
 import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
 import org.infoglue.cms.controllers.kernel.impl.simple.ServiceDefinitionController;
 import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeTypeDefinitionController;
 import org.infoglue.cms.entities.content.Content;
 import org.infoglue.cms.entities.content.ContentVersion;
 import org.infoglue.cms.entities.content.DigitalAsset;
 import org.infoglue.cms.entities.content.impl.simple.ContentImpl;
 import org.infoglue.cms.entities.management.AvailableServiceBinding;
 import org.infoglue.cms.entities.management.ContentTypeDefinition;
 import org.infoglue.cms.entities.management.Language;
 import org.infoglue.cms.entities.management.Repository;
 import org.infoglue.cms.entities.management.RepositoryLanguage;
 import org.infoglue.cms.entities.management.ServiceDefinition;
 import org.infoglue.cms.entities.management.SiteNodeTypeDefinition;
 import org.infoglue.cms.entities.management.impl.simple.AvailableServiceBindingImpl;
 import org.infoglue.cms.entities.management.impl.simple.ContentTypeDefinitionImpl;
 import org.infoglue.cms.entities.management.impl.simple.InfoGlueExportImpl;
 import org.infoglue.cms.entities.management.impl.simple.LanguageImpl;
 import org.infoglue.cms.entities.management.impl.simple.RepositoryImpl;
 import org.infoglue.cms.entities.management.impl.simple.ServiceDefinitionImpl;
 import org.infoglue.cms.entities.management.impl.simple.SiteNodeTypeDefinitionImpl;
 import org.infoglue.cms.entities.structure.Qualifyer;
 import org.infoglue.cms.entities.structure.ServiceBinding;
 import org.infoglue.cms.entities.structure.SiteNode;
 import org.infoglue.cms.entities.structure.SiteNodeVersion;
 import org.infoglue.cms.entities.structure.impl.simple.ServiceBindingImpl;
 import org.infoglue.cms.entities.structure.impl.simple.SiteNodeImpl;
 import org.infoglue.cms.entities.structure.impl.simple.SiteNodeVersionImpl;
 import org.infoglue.cms.exception.SystemException;
 import org.infoglue.cms.util.FileUploadHelper;
 
 import webwork.action.ActionContext;
 
 /**
  * This class handles Exporting of a repository to an XML-file.
  * 
  * @author mattias
  */
 
 public class ImportRepositoryAction extends InfoGlueAbstractAction
 {
 	
 	/**
 	 * This shows the dialog before export.
 	 * @return
 	 * @throws Exception
 	 */	
 
 	public String doInput() throws Exception
 	{
 		return "input";
 	}
 	
 	/**
 	 * This handles the actual importing.
 	 */
 	
 	protected String doExecute() throws SystemException 
 	{
 		Database db = CastorDatabaseService.getDatabase();
 		
 		try 
 		{
 			//now restore the value and list what we get
 			File file = FileUploadHelper.getUploadedFile(ActionContext.getContext().getMultiPartRequest());
 			String encoding = "UTF-8";
 			int version = 1;
 			
 			//Looking up what kind of dialect this is.
 	        
 			FileInputStream fisTemp = new FileInputStream(file);
             InputStreamReader readerTemp = new InputStreamReader(fisTemp, encoding);
             BufferedReader bufferedReaderTemp = new BufferedReader(readerTemp);
             String line = bufferedReaderTemp.readLine();
             int index = 0;
             while(line != null && index < 50)
             {
             	getLogger().info("line:" + line + '\n');
             	if(line.indexOf("contentTypeDefinitionId") > -1)
             	{
             		getLogger().info("This was a new export...");
             		version = 2;
             		break;
             	}
             	line = bufferedReaderTemp.readLine();
             	index++;
             }
             
             bufferedReaderTemp.close();
             readerTemp.close();
             fisTemp.close();
 
             Mapping map = new Mapping();
 			if(version == 1)
 			{
 	            getLogger().info("MappingFile:" + CastorDatabaseService.class.getResource("/xml_mapping_site.xml").toString());
 				map.loadMapping(CastorDatabaseService.class.getResource("/xml_mapping_site.xml").toString());
 			}
 			else if(version == 2)
 			{
 			    getLogger().info("MappingFile:" + CastorDatabaseService.class.getResource("/xml_mapping_site_2.5.xml").toString());
 				map.loadMapping(CastorDatabaseService.class.getResource("/xml_mapping_site_2.5.xml").toString());	
 			}
 
 			// All ODMG database access requires a transaction
 			db.begin();
 			
 
 			//String encoding = "ISO-8859-1";
 	        FileInputStream fis = new FileInputStream(file);
             InputStreamReader reader = new InputStreamReader(fis, encoding);
 			//Reader reader = new FileReader(file);
 
 			Unmarshaller unmarshaller = new Unmarshaller(map);
 			unmarshaller.setWhitespacePreserve(true);
 			InfoGlueExportImpl infoGlueExportImplRead = (InfoGlueExportImpl)unmarshaller.unmarshal(reader);
 			Collection contentTypeDefinitions = infoGlueExportImplRead.getContentTypeDefinitions();
 			getLogger().info("Found " + contentTypeDefinitions.size() + " content type definitions");
 			
 			SiteNode readSiteNode = infoGlueExportImplRead.getRootSiteNode();
 			getLogger().info(readSiteNode.getName());
 			Content readContent = infoGlueExportImplRead.getRootContent();
 			getLogger().info(readContent.getName());
 
 			Repository repositoryRead = readSiteNode.getRepository();
 			getLogger().info(repositoryRead.getName());
 			readContent.setRepository((RepositoryImpl)repositoryRead);
 
 			db.create(repositoryRead);
 
 			Collection repositoryLanguages = repositoryRead.getRepositoryLanguages();
 			Iterator repositoryLanguagesIterator = repositoryLanguages.iterator();
 			while(repositoryLanguagesIterator.hasNext())
 			{
 				RepositoryLanguage repositoryLanguage = (RepositoryLanguage)repositoryLanguagesIterator.next();
 				Language originalLanguage = repositoryLanguage.getLanguage();
 				
 				Language language = LanguageController.getController().getLanguageWithCode(originalLanguage.getLanguageCode(), db);
 				if(language == null)
 				{
 				    db.create(originalLanguage);
 				    language = originalLanguage;
 				}
 				
 				repositoryLanguage.setLanguage(language);
 				repositoryLanguage.setRepository(repositoryRead);
 
 				db.create(repositoryLanguage);
 				
 				getLogger().info("language:" + language);
 				getLogger().info("language.getRepositoryLanguages():" + language.getRepositoryLanguages());
 				language.getRepositoryLanguages().add(repositoryLanguage);
 			}
 			
 			readSiteNode.setRepository((RepositoryImpl)repositoryRead);
 
 			Map contentIdMap = new HashMap();
 			Map siteNodeIdMap = new HashMap();
 			
 			List allContents = new ArrayList();
 			createContents(readContent, contentIdMap, allContents, contentTypeDefinitions, version, db);
 			List allSiteNodes = new ArrayList();
 			createStructure(readSiteNode, contentIdMap, siteNodeIdMap, allSiteNodes, db);
 			
 			updateContentVersions(allContents, contentIdMap, siteNodeIdMap);
 			
 			reader.close();
 			
 			db.commit();
 			db.close();
 
 		} 
 		catch ( Exception e) 
 		{
 			try
             {
                 db.rollback();
                 db.close();
             } 
 			catch (Exception e1)
             {
                 getLogger().error("An error occurred when importing a repository: " + e.getMessage(), e);
     			throw new SystemException("An error occurred when importing a repository: " + e.getMessage(), e);
             }
 			
 			getLogger().error("An error occurred when importing a repository: " + e.getMessage(), e);
 			throw new SystemException("An error occurred when importing a repository: " + e.getMessage(), e);
 		}
 		
 		return "success";
 	}
 
 	
 	/**
 	 * This method copies a sitenode and all it relations.
 	 * 
 	 * @param siteNode
 	 * @param db
 	 * @throws Exception
 	 */
 	private void createStructure(SiteNode siteNode, Map contentIdMap, Map siteNodeIdMap, List allSiteNodes, Database db) throws Exception
 	{
 		getLogger().info("siteNode:" + siteNode.getName());
 
 		Integer originalSiteNodeId = siteNode.getSiteNodeId();
 		
 		SiteNodeTypeDefinition originalSiteNodeTypeDefinition = siteNode.getSiteNodeTypeDefinition();
 		SiteNodeTypeDefinition siteNodeTypeDefinition = SiteNodeTypeDefinitionController.getController().getSiteNodeTypeDefinitionWithName(siteNode.getSiteNodeTypeDefinition().getName(), db, false);
 		if(siteNodeTypeDefinition == null)
 		{
 		    db.create(originalSiteNodeTypeDefinition);
 		    siteNodeTypeDefinition = originalSiteNodeTypeDefinition;
 		}
 		
 		siteNode.setSiteNodeTypeDefinition((SiteNodeTypeDefinitionImpl)siteNodeTypeDefinition);
 		
 		String mappedMetaInfoContentId = "-1";
 		if(siteNode.getMetaInfoContentId() != null)
 		{
 			if(contentIdMap.containsKey(siteNode.getMetaInfoContentId().toString()))
 				mappedMetaInfoContentId = (String)contentIdMap.get(siteNode.getMetaInfoContentId().toString());
 			//System.out.println("siteNode meta info content id was:" + siteNode.getMetaInfoContentId() + " and is now " + mappedMetaInfoContentId);
 		}
 		siteNode.setMetaInfoContentId(new Integer(mappedMetaInfoContentId));
 		
 		db.create(siteNode);
 		
 		allSiteNodes.add(siteNode);
 		    
 		Integer newSiteNodeId = siteNode.getSiteNodeId();
 		getLogger().info(originalSiteNodeId + ":" + newSiteNodeId);
 		siteNodeIdMap.put(originalSiteNodeId.toString(), newSiteNodeId.toString());
 		
 		Collection childSiteNodes = siteNode.getChildSiteNodes();
 		if(childSiteNodes != null)
 		{
 			Iterator childSiteNodesIterator = childSiteNodes.iterator();
 			while(childSiteNodesIterator.hasNext())
 			{
 				SiteNode childSiteNode = (SiteNode)childSiteNodesIterator.next();
 				childSiteNode.setRepository(siteNode.getRepository());
 				childSiteNode.setParentSiteNode((SiteNodeImpl)siteNode);
 				createStructure(childSiteNode, contentIdMap, siteNodeIdMap, allSiteNodes, db);
 			}
 		}
 
 		Collection siteNodeVersions = siteNode.getSiteNodeVersions();
 		Iterator siteNodeVersionsIterator = siteNodeVersions.iterator();
 		while(siteNodeVersionsIterator.hasNext())
 		{
 			SiteNodeVersion siteNodeVersion = (SiteNodeVersion)siteNodeVersionsIterator.next();
 			
 			Collection serviceBindings = siteNodeVersion.getServiceBindings();
 
 			siteNodeVersion.setOwningSiteNode((SiteNodeImpl)siteNode);
 			
 			db.create(siteNodeVersion);
 			
 			Iterator serviceBindingsIterator = serviceBindings.iterator();
 			while(serviceBindingsIterator.hasNext())
 			{
 				ServiceBinding serviceBinding = (ServiceBinding)serviceBindingsIterator.next();
 				getLogger().info("serviceBinding:" + serviceBinding.getName());
 				
 				ServiceDefinition originalServiceDefinition = serviceBinding.getServiceDefinition();
 				String serviceDefinitionName = originalServiceDefinition.getName();
 				ServiceDefinition serviceDefinition = ServiceDefinitionController.getController().getServiceDefinitionWithName(serviceDefinitionName, db, false);
 				if(serviceDefinition == null)
 				{
 				    db.create(originalServiceDefinition);
 				    serviceDefinition = originalServiceDefinition;
 				    //availableServiceBinding.getServiceDefinitions().add(serviceDefinition);
 				}
 				
 				serviceBinding.setServiceDefinition((ServiceDefinitionImpl)serviceDefinition);
 
 				AvailableServiceBinding originalAvailableServiceBinding = serviceBinding.getAvailableServiceBinding();
 				String availableServiceBindingName = originalAvailableServiceBinding.getName();
 				getLogger().info("availableServiceBindingName:" + availableServiceBindingName);
 				AvailableServiceBinding availableServiceBinding = AvailableServiceBindingController.getController().getAvailableServiceBindingWithName(availableServiceBindingName, db, false);
 				if(availableServiceBinding == null)
 				{
 				    getLogger().info("There was no availableServiceBinding registered under:" + availableServiceBindingName);
 				    getLogger().info("originalAvailableServiceBinding:" + originalAvailableServiceBinding.getName() + ":" + originalAvailableServiceBinding.getIsInheritable());
 				    db.create(originalAvailableServiceBinding);
 				    availableServiceBinding = originalAvailableServiceBinding;
 				    getLogger().info("Notifying:" + siteNodeTypeDefinition.getName() + " about the new availableServiceBinding " + availableServiceBinding.getName());
 				    siteNodeTypeDefinition.getAvailableServiceBindings().add((AvailableServiceBindingImpl)availableServiceBinding);
 				    serviceDefinition.getAvailableServiceBindings().add((AvailableServiceBindingImpl)availableServiceBinding);
 				    availableServiceBinding.getSiteNodeTypeDefinitions().add((SiteNodeTypeDefinitionImpl)siteNodeTypeDefinition);
 				    availableServiceBinding.getServiceDefinitions().add((ServiceDefinitionImpl)serviceDefinition);
 				}
 				else
 				{
 					if(!siteNodeTypeDefinition.getAvailableServiceBindings().contains(availableServiceBinding))
 					{
 						siteNodeTypeDefinition.getAvailableServiceBindings().add((AvailableServiceBindingImpl)availableServiceBinding);
 						availableServiceBinding.getSiteNodeTypeDefinitions().add(siteNodeTypeDefinition);
 					}
 				}
 				
 				serviceBinding.setAvailableServiceBinding((AvailableServiceBindingImpl)availableServiceBinding);
 				
 				
 				Collection qualifyers = serviceBinding.getBindingQualifyers();
 				Iterator qualifyersIterator = qualifyers.iterator();
 				while(qualifyersIterator.hasNext())
 				{
 					Qualifyer qualifyer = (Qualifyer)qualifyersIterator.next();
 					qualifyer.setServiceBinding((ServiceBindingImpl)serviceBinding);
 					
 					String entityName 	= qualifyer.getName();
 					String entityId		= qualifyer.getValue();
 					
 					if(entityName.equalsIgnoreCase("contentId"))
 					{
 						String mappedContentId = (String)contentIdMap.get(entityId);
						qualifyer.setValue((mappedContentId == null) ? entityId : mappedContentId);
 					}
 					else if(entityName.equalsIgnoreCase("siteNodeId"))
 					{
 						String mappedSiteNodeId = (String)siteNodeIdMap.get(entityId);
						qualifyer.setValue((mappedSiteNodeId == null) ? entityId : mappedSiteNodeId);						
 					}
 				}
 
 				serviceBinding.setSiteNodeVersion((SiteNodeVersionImpl)siteNodeVersion);				
 
 				db.create(serviceBinding);
 
 			}
 		}		
 		
 	}
 
 
 	/**
 	 * This method copies a content and all it relations.
 	 * 
 	 * @param siteNode
 	 * @param db
 	 * @throws Exception
 	 */
 	
 	private List createContents(Content content, Map idMap, List allContents, Collection contentTypeDefinitions, int version, Database db) throws Exception
 	{
 	    Integer originalContentId = content.getContentId();
 		
 	    if(version == 2)
 	    {
 		    Integer contentTypeDefinitionId = ((ContentImpl)content).getContentTypeDefinitionId();
 		    if(contentTypeDefinitionId != null)
 			{
 		    	ContentTypeDefinition originalContentTypeDefinition = null;
 		    	Iterator contentTypeDefinitionsIterator = contentTypeDefinitions.iterator();
 		    	while(contentTypeDefinitionsIterator.hasNext())
 		    	{
 		    		ContentTypeDefinition contentTypeDefinitionCandidate = (ContentTypeDefinition)contentTypeDefinitionsIterator.next();
 		    		if(contentTypeDefinitionCandidate.getId().intValue() == contentTypeDefinitionId.intValue())
 		    		{
 		    			originalContentTypeDefinition = contentTypeDefinitionCandidate;
 		    			break;
 		    		}
 		    	}
 		    	
 		    	if(originalContentTypeDefinition != null)
 		    	{
 			    	ContentTypeDefinition contentTypeDefinition = ContentTypeDefinitionController.getController().getContentTypeDefinitionWithName(originalContentTypeDefinition.getName(), db);
 					if(contentTypeDefinition == null)
 					{
 					    db.create(originalContentTypeDefinition);
 					    contentTypeDefinition = originalContentTypeDefinition;
 					}
 				
 					content.setContentTypeDefinition((ContentTypeDefinitionImpl)contentTypeDefinition);
 		    	}
 			}
 	    }
 	    else if(version == 2)
 	    {
 			ContentTypeDefinition originalContentTypeDefinition = content.getContentTypeDefinition();
 			if(originalContentTypeDefinition != null)
 			{
 			    ContentTypeDefinition contentTypeDefinition = ContentTypeDefinitionController.getController().getContentTypeDefinitionWithName(originalContentTypeDefinition.getName(), db);
 				if(contentTypeDefinition == null)
 				{
 				    db.create(originalContentTypeDefinition);
 				    contentTypeDefinition = originalContentTypeDefinition;
 				}
 				
 				content.setContentTypeDefinition((ContentTypeDefinitionImpl)contentTypeDefinition);
 			}
 	    }
 	    
 		db.create(content);
 		
 		allContents.add(content);
 		
 		Integer newContentId = content.getContentId();
 		idMap.put(originalContentId.toString(), newContentId.toString());
 		
 		Collection contentVersions = content.getContentVersions();
 		Iterator contentVersionsIterator = contentVersions.iterator();
 		while(contentVersionsIterator.hasNext())
 		{
 			ContentVersion contentVersion = (ContentVersion)contentVersionsIterator.next();
 			Language language = LanguageController.getController().getLanguageWithCode(contentVersion.getLanguage().getLanguageCode(), db);
 
 			contentVersion.setOwningContent((ContentImpl)content);
 			contentVersion.setLanguage((LanguageImpl)language);
 
 			Collection digitalAssets = contentVersion.getDigitalAssets();
 			if(digitalAssets != null)
 			{
 				List initialDigitalAssets = new ArrayList();
 					
 				Iterator digitalAssetsIterator = digitalAssets.iterator();
 				while(digitalAssetsIterator.hasNext())
 				{
 					DigitalAsset digitalAsset = (DigitalAsset)digitalAssetsIterator.next();
 					
 					List initialContentVersions = new ArrayList();
 					initialContentVersions.add(contentVersion);
 					digitalAsset.setContentVersions(initialContentVersions);
 	
 					db.create(digitalAsset);
 					
 					initialDigitalAssets.add(digitalAsset);
 				}
 				
 				contentVersion.setDigitalAssets(initialDigitalAssets);
 			}
 
 			db.create(contentVersion);
 		}		
 		
 		Collection childContents = content.getChildren();
 		if(childContents != null)
 		{
 			Iterator childContentsIterator = childContents.iterator();
 			while(childContentsIterator.hasNext())
 			{
 				Content childContent = (Content)childContentsIterator.next();
 				childContent.setRepository(content.getRepository());
 				childContent.setParentContent((ContentImpl)content);
 				createContents(childContent, idMap, allContents, contentTypeDefinitions, version, db);
 			}
 		}
 		
 		return allContents;
 	}
 
 
 	/**
 	 * This method updates all the bindings in content-versions to reflect the move. 
 	 */
 	private void updateContentVersions(List allContents, Map contentIdMap, Map siteNodeIdMap)
 	{
 	    getLogger().info("allContents:" + allContents.size());
 	    Iterator allContentsIterator = allContents.iterator();
 	    while(allContentsIterator.hasNext())
 	    {
 	        Content content = (Content)allContentsIterator.next();
 	        
 	        getLogger().info("content:" + content);
 	        
 	        Iterator contentVersionIterator = content.getContentVersions().iterator();
 	        while(contentVersionIterator.hasNext())
 	        {
 	            ContentVersion contentVersion = (ContentVersion)contentVersionIterator.next();
 	            String contentVersionValue = contentVersion.getVersionValue();
 
                 contentVersionValue = contentVersionValue.replaceAll("contentId=\"", "contentId=\"old_");
                 contentVersionValue = contentVersionValue.replaceAll("\\?contentId=", "\\?contentId=old_");
                 contentVersionValue = contentVersionValue.replaceAll("getInlineAssetUrl\\(", "getInlineAssetUrl\\(old_");
                 contentVersionValue = contentVersionValue.replaceAll("languageId,", "languageId,old_");
                 contentVersionValue = contentVersionValue.replaceAll("entity=\"Content\" entityId=\"", "entity=\"Content\" entityId=\"old_");
                 contentVersionValue = contentVersionValue.replaceAll("entity='Content'><id>", "entity='Content'><id>old_");
 
                 contentVersionValue = contentVersionValue.replaceAll("siteNodeId=\"", "siteNodeId=\"old_");
                 //if(contentVersionValue.indexOf("getPageUrl") > -1)
                 //	System.out.println("contentVersionValue:" + contentVersionValue);
                 
                 contentVersionValue = contentVersionValue.replaceAll("getPageUrl\\((\\d)", "getPageUrl\\(old_$1");
                 
                 //if(contentVersionValue.indexOf("getPageUrl") > -1)
                 //	System.out.println("contentVersionValue:" + contentVersionValue);
                 
                 contentVersionValue = contentVersionValue.replaceAll("entity=\"SiteNode\" entityId=\"", "entity=\"SiteNode\" entityId=\"old_");
                 contentVersionValue = contentVersionValue.replaceAll("entity='SiteNode'><id>", "entity='SiteNode'><id>old_");
 	            
 	            getLogger().info("contentVersionValue before:" + contentVersionValue);
                 
 	            Iterator contentIdMapIterator = contentIdMap.keySet().iterator();
 	            while (contentIdMapIterator.hasNext()) 
 	            {
 	                String oldContentId = (String)contentIdMapIterator.next();
 	                String newContentId = (String)contentIdMap.get(oldContentId);
 	                
 	                getLogger().info("Replacing all:" + oldContentId + " with " + newContentId);
 	                
 	                contentVersionValue = contentVersionValue.replaceAll("contentId=\"old_" + oldContentId + "\"", "contentId=\"" + newContentId + "\"");
 	                contentVersionValue = contentVersionValue.replaceAll("\\?contentId=old_" + oldContentId + "&", "\\?contentId=" + newContentId + "&");
 	                contentVersionValue = contentVersionValue.replaceAll("getInlineAssetUrl\\(old_" + oldContentId + ",", "getInlineAssetUrl\\(" + newContentId + ",");
 	                contentVersionValue = contentVersionValue.replaceAll("languageId,old_" + oldContentId + "\\)", "languageId," + newContentId + "\\)");
 	                contentVersionValue = contentVersionValue.replaceAll("entity=\"Content\" entityId=\"old_" + oldContentId + "\"", "entity=\"Content\" entityId=\"" + newContentId + "\"");
 	                contentVersionValue = contentVersionValue.replaceAll("entity='Content'><id>old_" + oldContentId + "</id>", "entity='Content'><id>" + newContentId + "</id>");
 	                //contentVersionValue = contentVersionValue.replaceAll("<id>" + oldContentId + "</id>", "<id>" + newContentId + "</id>");
 	            }
 	            
 	            Iterator siteNodeIdMapIterator = siteNodeIdMap.keySet().iterator();
 	            while (siteNodeIdMapIterator.hasNext()) 
 	            {
 	                String oldSiteNodeId = (String)siteNodeIdMapIterator.next();
 	                String newSiteNodeId = (String)siteNodeIdMap.get(oldSiteNodeId);
 	                
 	                getLogger().info("Replacing all:" + oldSiteNodeId + " with " + newSiteNodeId);
 	                
 	                contentVersionValue = contentVersionValue.replaceAll("siteNodeId=\"old_" + oldSiteNodeId + "\"", "siteNodeId=\"" + newSiteNodeId + "\"");
 	                contentVersionValue = contentVersionValue.replaceAll("getPageUrl\\(old_" + oldSiteNodeId + ",", "getPageUrl\\(" + newSiteNodeId + ",");
 	                contentVersionValue = contentVersionValue.replaceAll("entity=\"SiteNode\" entityId=\"old_" + oldSiteNodeId + "\"", "entity=\"SiteNode\" entityId=\"" + newSiteNodeId + "\"");
 	                contentVersionValue = contentVersionValue.replaceAll("entity='SiteNode'><id>old_" + oldSiteNodeId + "</id>", "entity='SiteNode'><id>" + newSiteNodeId + "</id>");
 	            }
 	            
 	            getLogger().info("contentVersionValue after:" + contentVersionValue);
 	            
 	            //Now replace all occurrances of old as they should never be there.
                 contentVersionValue = contentVersionValue.replaceAll("old_", "");
 
 	            getLogger().info("new contentVersionValue:" + contentVersionValue);
 	            contentVersion.setVersionValue(contentVersionValue);
 	        }
 	    }
 	}
 
 	/*
 		private void updateContentVersions(List allContents, Map contentIdMap, Map siteNodeIdMap)
 	{
 	    getLogger().info("allContents:" + allContents.size());
 	    Iterator allContentsIterator = allContents.iterator();
 	    while(allContentsIterator.hasNext())
 	    {
 	        Content content = (Content)allContentsIterator.next();
 	        
 	        getLogger().info("content:" + content);
 	        
 	        Iterator contentVersionIterator = content.getContentVersions().iterator();
 	        while(contentVersionIterator.hasNext())
 	        {
 	            ContentVersion contentVersion = (ContentVersion)contentVersionIterator.next();
 	            String contentVersionValue = contentVersion.getVersionValue();
 
                 contentVersionValue = contentVersionValue.replaceAll("contentId=\"" + oldContentId + "\"", "contentId=\"" + newContentId + "\"");
                 contentVersionValue = contentVersionValue.replaceAll("contentId=" + oldContentId + "&", "contentId=" + newContentId + "&");
                 contentVersionValue = contentVersionValue.replaceAll("getInlineAssetUrl\\(" + oldContentId + ",", "getInlineAssetUrl\\(" + newContentId + ",");
                 contentVersionValue = contentVersionValue.replaceAll("languageId," + oldContentId + "\\)", "languageId," + newContentId + "\\)");
                 contentVersionValue = contentVersionValue.replaceAll("entity=\"Content\" entityId=\"" + oldContentId + "\"", "entity=\"Content\" entityId=\"" + newContentId + "\"");
                 contentVersionValue = contentVersionValue.replaceAll("entity='Content'><id>" + oldContentId + "</id>", "entity='Content'><id>" + newContentId + "</id>");
 
                 contentVersionValue = contentVersionValue.replaceAll("siteNodeId=\"" + oldSiteNodeId + "\"", "siteNodeId=\"" + newSiteNodeId + "\"");
                 contentVersionValue = contentVersionValue.replaceAll("getPageUrl\\(" + oldSiteNodeId + ",", "getPageUrl\\(" + newSiteNodeId + ",");
                 contentVersionValue = contentVersionValue.replaceAll("entity=\"SiteNode\" entityId=\"" + oldSiteNodeId + "\"", "entity=\"SiteNode\" entityId=\"" + newSiteNodeId + "\"");
                 contentVersionValue = contentVersionValue.replaceAll("entity='SiteNode'><id>" + oldSiteNodeId + "</id>", "entity='SiteNode'><id>" + newSiteNodeId + "</id>");
 	            
 	            getLogger().info("contentVersionValue before:" + contentVersionValue);
                 
 	            Iterator contentIdMapIterator = contentIdMap.keySet().iterator();
 	            while (contentIdMapIterator.hasNext()) 
 	            {
 	                String oldContentId = (String)contentIdMapIterator.next();
 	                String newContentId = (String)contentIdMap.get(oldContentId);
 	                
 	                getLogger().info("Replacing all:" + oldContentId + " with " + newContentId);
 	                
 	                contentVersionValue = contentVersionValue.replaceAll("contentId=\"" + oldContentId + "\"", "contentId=\"" + newContentId + "\"");
 	                contentVersionValue = contentVersionValue.replaceAll("contentId=" + oldContentId + "&", "contentId=" + newContentId + "&");
 	                contentVersionValue = contentVersionValue.replaceAll("getInlineAssetUrl\\(" + oldContentId + ",", "getInlineAssetUrl\\(" + newContentId + ",");
 	                contentVersionValue = contentVersionValue.replaceAll("languageId," + oldContentId + "\\)", "languageId," + newContentId + "\\)");
 	                contentVersionValue = contentVersionValue.replaceAll("entity=\"Content\" entityId=\"" + oldContentId + "\"", "entity=\"Content\" entityId=\"" + newContentId + "\"");
 	                contentVersionValue = contentVersionValue.replaceAll("entity='Content'><id>" + oldContentId + "</id>", "entity='Content'><id>" + newContentId + "</id>");
 	                //contentVersionValue = contentVersionValue.replaceAll("<id>" + oldContentId + "</id>", "<id>" + newContentId + "</id>");
 	            }
 	            
 	            Iterator siteNodeIdMapIterator = siteNodeIdMap.keySet().iterator();
 	            while (siteNodeIdMapIterator.hasNext()) 
 	            {
 	                String oldSiteNodeId = (String)siteNodeIdMapIterator.next();
 	                String newSiteNodeId = (String)siteNodeIdMap.get(oldSiteNodeId);
 	                
 	                getLogger().info("Replacing all:" + oldSiteNodeId + " with " + newSiteNodeId);
 	                
 	                contentVersionValue = contentVersionValue.replaceAll("siteNodeId=\"" + oldSiteNodeId + "\"", "siteNodeId=\"" + newSiteNodeId + "\"");
 	                contentVersionValue = contentVersionValue.replaceAll("getPageUrl\\(" + oldSiteNodeId + ",", "getPageUrl\\(" + newSiteNodeId + ",");
 	                contentVersionValue = contentVersionValue.replaceAll("entity=\"SiteNode\" entityId=\"" + oldSiteNodeId + "\"", "entity=\"SiteNode\" entityId=\"" + newSiteNodeId + "\"");
 	                contentVersionValue = contentVersionValue.replaceAll("entity='SiteNode'><id>" + oldSiteNodeId + "</id>", "entity='SiteNode'><id>" + newSiteNodeId + "</id>");
 	            }
 	            
 	            getLogger().info("contentVersionValue after:" + contentVersionValue);
 
 	            getLogger().info("new contentVersionValue:" + contentVersionValue);
 	            contentVersion.setVersionValue(contentVersionValue);
 	        }
 	    }
 	}
 	*/
 }
