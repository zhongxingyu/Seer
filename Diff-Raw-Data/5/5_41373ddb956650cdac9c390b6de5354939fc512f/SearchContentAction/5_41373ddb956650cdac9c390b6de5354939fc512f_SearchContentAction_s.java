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
 
 package org.infoglue.cms.applications.contenttool.actions;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.analysis.standard.StandardAnalyzer;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.queryParser.QueryParser;
 import org.apache.lucene.search.Hits;
 import org.apache.lucene.search.IndexSearcher;
 import org.apache.lucene.search.Query;
 import org.apache.lucene.search.Searcher;
 import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
 import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
 import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
 import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
 import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionControllerProxy;
 import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
 import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
 import org.infoglue.cms.controllers.kernel.impl.simple.SearchController;
 import org.infoglue.cms.controllers.kernel.impl.simple.UserControllerProxy;
 import org.infoglue.cms.entities.content.ContentVO;
 import org.infoglue.cms.entities.content.ContentVersionVO;
 import org.infoglue.cms.entities.content.DigitalAssetVO;
 import org.infoglue.cms.entities.management.LanguageVO;
 import org.infoglue.cms.util.CmsPropertyHandler;
 
 import webwork.action.Action;
 
 
 /**
  * Action class for usecase SearchContentAction. Was better before but due to wanted support for multiple 
  * databases and lack of time I had to cut down on functionality - sorry Magnus. 
  *
  * @author Magnus Gvenal
  * @author Mattias Bogeblad
  */
 
 public class SearchContentAction extends InfoGlueAbstractAction 
 {
     private final static Logger logger = Logger.getLogger(SearchContentAction.class.getName());
 
 	private static final long serialVersionUID = 1L;
 	
 	private List contentVersionVOList = new ArrayList();
 	private Set contentVOSet;
 	private List<DigitalAssetVO> digitalAssetVOList = null;
 	private Integer repositoryId;
 	private String searchString;
 	private String name;
 	private Integer languageId;
 	private Integer contentTypeDefinitionId;
 	private Integer caseSensitive;
 	private boolean includeAssets = false;
 	private Integer inverseSearch;
 	private Integer stateId;
 	private boolean advancedEnabled = false;
 	private List selectedRepositoryIdList = new ArrayList();
 	private String[] allowedContentTypeIds = null;
 	
 	private int maxRows = 0;
 	
 	//This is for advanced search
 	private List principals 			= null;
 	private List availableLanguages 	= null;
 	private List contentTypeDefinitions = null;
 	private List repositories 			= null;
 	
 	//This is for replace
 	private String replaceString		= null;
 	//private String[] contentVersionId  	= null;
 	
 	public void setSearchString(String s)
 	{
 	    this.searchString = s;
 		//this.searchString = s.replaceAll("'","");
 	}
 	
 	public String getSearchString()
 	{
 		return this.searchString;	
 	}
 	
 	public void setMaxRows(int r)
 	{
 		this.maxRows = r;	
 	}
 	
 	public int getMaxRows()
 	{
 		if(maxRows == 0)
 		    maxRows=100;
 		
 		return this.maxRows;	
 	}
 
 	public List getContentVersionVOList()
 	{
 		return this.contentVersionVOList;		
 	}
 	
 	public String doExecute() throws Exception 
 	{
 		/*
 		try 
 	    {
 			String index = "c:/temp/LuceneIndex/index";
 		    String field = "contents";
 		    String queries = null;
 		    int repeat = 0;
 		    boolean raw = false;
 		    String normsField = null;
 		    		    
 		    IndexReader reader = IndexReader.open(index);
 
 		    Searcher searcher = new IndexSearcher(reader);
 		    Analyzer analyzer = new StandardAnalyzer();
 
 		    QueryParser parser = new QueryParser(field, analyzer);
 
 		   
 			Query query = parser.parse(this.getSearchString());
 			System.out.println("Searching for: " + query.toString(field));
 
 			Hits hits = searcher.search(query);
 
 			if (repeat > 0)
 			{ 
 				Date start = new Date();
 				for (int i = 0; i < repeat; i++)
 				{
 					hits = searcher.search(query);
 				}
 				Date end = new Date();
 				System.out.println("Time: " + (end.getTime() - start.getTime()) + "ms");
 			}
 
 			System.out.println(hits.length() + " total matching documents");
 
 			final int HITS_PER_PAGE = 10;
 			for (int start = 0; start < hits.length(); start += HITS_PER_PAGE)
 			{
 				int end = Math.min(hits.length(), start + HITS_PER_PAGE);
 				for (int i = start; i < end; i++)
 				{
 
 					if (raw)
 					{ // output raw format
 						System.out.println("doc=" + hits.id(i) + " score=" + hits.score(i));
 						continue;
 					}
 
 					Document doc = hits.doc(i);
 					String contentVersionId = doc.get("contentVersionId");
 					String contentId = doc.get("contentId");
 					System.out.println("doc:" + doc);
 					System.out.println("contentVersionId:" + contentVersionId);
 					System.out.println("contentId:" + contentId);
 					//ContentVersionVO cvvo = ContentVersionControllerProxy.getController().getACContentVersionVOWithId(getInfoGluePrincipal(), new Integer(contentVersionId));
 					ContentVersionVO cvvo = ContentVersionController.getContentVersionController().getFullContentVersionVOWithId(new Integer(contentVersionId));
 					System.out.println("cvvo:" + cvvo);
 					contentVersionVOList.add(cvvo);
					/*
 					String path = doc.get("path");
 					//contentVersionVOList
 					if (path != null)
 					{
 						System.out.println((i + 1) + ". " + path);
 						String title = doc.get("title");
 						if (title != null)
 						{
 							System.out.println("   Title: " + doc.get("title"));
 						}
 					} else
 					{
 						System.out.println((i + 1) + ". " + "No path for this document");
 					}
					*/
 				}
 
 				if (queries != null) // non-interactive
 					break;
 			}
 			
 			reader.close();	
 	    } 
 	    catch (Exception e) 
 	    {
 	    	System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
 	    }
 	    */
 
 	    int maxRows = 100;
 		try
 		{
 			maxRows = Integer.parseInt(CmsPropertyHandler.getMaxRows());
 		}
 		catch(Exception e)
 		{
 		}
 
 		String[] repositoryIdToSearch = this.getRequest().getParameterValues("repositoryIdToSearch");
 		if(repositoryIdToSearch != null)
 		{
 			Integer[] repositoryIdAsIntegerToSearch = new Integer[repositoryIdToSearch.length];
 			for(int i=0; i < repositoryIdToSearch.length; i++)
 			{
 				repositoryIdAsIntegerToSearch[i] = new Integer(repositoryIdToSearch[i]);
 				selectedRepositoryIdList.add(repositoryIdToSearch[i]);
 			}
 			
 			contentVersionVOList = SearchController.getContentVersions(repositoryIdAsIntegerToSearch, this.getSearchString(), maxRows, name, languageId, new Integer[]{contentTypeDefinitionId}, caseSensitive, stateId, includeAssets);
 		}
 		else
 		{
 			contentVersionVOList = SearchController.getContentVersions(this.repositoryId, this.getSearchString(), maxRows, name, languageId, new Integer[]{contentTypeDefinitionId}, caseSensitive, stateId, includeAssets);
 			selectedRepositoryIdList.add("" + this.repositoryId);
 		}
 	    
 	    this.principals = UserControllerProxy.getController().getAllUsers();
 	    this.availableLanguages = LanguageController.getController().getLanguageVOList(this.repositoryId);
 	    this.contentTypeDefinitions = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOList();
 		this.repositories = RepositoryController.getController().getAuthorizedRepositoryVOList(this.getInfoGluePrincipal(), false);
 
 		return "success";
 	}
 
 	public String doAAAExecute() throws Exception 
 	{
 	    int maxRows = 100;
 		try
 		{
 			maxRows = Integer.parseInt(CmsPropertyHandler.getMaxRows());
 		}
 		catch(Exception e)
 		{
 		}
 
 		String[] repositoryIdToSearch = this.getRequest().getParameterValues("repositoryIdToSearch");
 		if(repositoryIdToSearch != null)
 		{
 			Integer[] repositoryIdAsIntegerToSearch = new Integer[repositoryIdToSearch.length];
 			for(int i=0; i < repositoryIdToSearch.length; i++)
 			{
 				repositoryIdAsIntegerToSearch[i] = new Integer(repositoryIdToSearch[i]);
 				selectedRepositoryIdList.add(repositoryIdToSearch[i]);
 			}
 			
 			contentVersionVOList = SearchController.getContentVersions(repositoryIdAsIntegerToSearch, this.getSearchString(), maxRows, name, languageId, new Integer[]{contentTypeDefinitionId}, caseSensitive, stateId, includeAssets);
 		}
 		else
 		{
 			contentVersionVOList = SearchController.getContentVersions(this.repositoryId, this.getSearchString(), maxRows, name, languageId, new Integer[]{contentTypeDefinitionId}, caseSensitive, stateId, includeAssets);
 			selectedRepositoryIdList.add("" + this.repositoryId);
 		}
 		
 	    this.principals = UserControllerProxy.getController().getAllUsers();
 	    this.availableLanguages = LanguageController.getController().getLanguageVOList(this.repositoryId);
 	    this.contentTypeDefinitions = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOList();
 		this.repositories = RepositoryController.getController().getAuthorizedRepositoryVOList(this.getInfoGluePrincipal(), false);
 
 		return "success";
 	}
 
 	public String doBindingResult() throws Exception 
 	{
 		Integer[] allowedContentTypeId = new Integer[0];
 		if(allowedContentTypeIds != null && allowedContentTypeIds.length != 0)
 		{
 			allowedContentTypeId = new Integer[allowedContentTypeIds.length];
 			for(int i=0; i<allowedContentTypeIds.length; i++)
 				allowedContentTypeId[i] = new Integer(allowedContentTypeIds[i]);
 		}
 		
 		int maxRows = 100;
 		try
 		{
 			maxRows = Integer.parseInt(CmsPropertyHandler.getMaxRows());
 		}
 		catch(Exception e)
 		{
 		}
 
 		String[] repositoryIdToSearch = this.getRequest().getParameterValues("repositoryIdToSearch");
 		if(repositoryIdToSearch != null)
 		{
 			Integer[] repositoryIdAsIntegerToSearch = new Integer[repositoryIdToSearch.length];
 			for(int i=0; i < repositoryIdToSearch.length; i++)
 			{
 				repositoryIdAsIntegerToSearch[i] = new Integer(repositoryIdToSearch[i]);
 				selectedRepositoryIdList.add(repositoryIdToSearch[i]);
 			}
 			
 			//contentVersionVOList = SearchController.getContentVersions(repositoryIdAsIntegerToSearch, this.getSearchString(), maxRows, name, languageId, contentTypeDefinitionId, caseSensitive, stateId);
 			contentVOSet = SearchController.getContents(repositoryIdAsIntegerToSearch, this.getSearchString(), maxRows, name, languageId, allowedContentTypeId, caseSensitive, stateId);
 		}
 		else
 		{
 			contentVOSet = SearchController.getContents(this.repositoryId, this.getSearchString(), maxRows, name, languageId, allowedContentTypeId, caseSensitive, stateId);
 			selectedRepositoryIdList.add("" + this.repositoryId);
 		}
 
 		return "successBindingResult";
 	}
 
 	public String doInlineAssetResult() throws Exception 
 	{
 		Integer[] allowedContentTypeId = new Integer[0];
 		if(allowedContentTypeIds != null && allowedContentTypeIds.length != 0)
 		{
 			allowedContentTypeId = new Integer[allowedContentTypeIds.length];
 			for(int i=0; i<allowedContentTypeIds.length; i++)
 				allowedContentTypeId[i] = new Integer(allowedContentTypeIds[i]);
 		}
 		
 		int maxRows = 100;
 		try
 		{
 			maxRows = Integer.parseInt(CmsPropertyHandler.getMaxRows());
 		}
 		catch(Exception e)
 		{
 		}
 
 		String[] repositoryIdToSearch = this.getRequest().getParameterValues("repositoryIdToSearch");
 		if(repositoryIdToSearch != null)
 		{
 			Integer[] repositoryIdAsIntegerToSearch = new Integer[repositoryIdToSearch.length];
 			for(int i=0; i < repositoryIdToSearch.length; i++)
 			{
 				repositoryIdAsIntegerToSearch[i] = new Integer(repositoryIdToSearch[i]);
 				selectedRepositoryIdList.add(repositoryIdToSearch[i]);
 			}
 			
 			contentVOSet = SearchController.getContents(repositoryIdAsIntegerToSearch, this.getSearchString(), maxRows, name, languageId, allowedContentTypeId, caseSensitive, stateId, true);
 		}
 		else 
 		{
 			contentVOSet = SearchController.getContents(this.repositoryId, this.getSearchString(), maxRows, name, languageId, allowedContentTypeId, caseSensitive, stateId, true);
 			selectedRepositoryIdList.add("" + this.repositoryId);
 		}
 
 		return "successInlineAssetResult";
 	}
 
 	public String doInlineAssetSearchV3() throws Exception 
 	{
 		int maxRows = 100;
 		try
 		{
 			maxRows = Integer.parseInt(CmsPropertyHandler.getMaxRows());
 		}
 		catch(Exception e)
 		{
 		}
 
 		String[] repositoryIdToSearch = this.getRequest().getParameterValues("repositoryIdToSearch");
 		if(repositoryIdToSearch != null)
 		{
 			Integer[] repositoryIdAsIntegerToSearch = new Integer[repositoryIdToSearch.length];
 			for(int i=0; i < repositoryIdToSearch.length; i++)
 			{
 				repositoryIdAsIntegerToSearch[i] = new Integer(repositoryIdToSearch[i]);
 				selectedRepositoryIdList.add(repositoryIdToSearch[i]);
 			}
 			
 			digitalAssetVOList = SearchController.getDigitalAssets(repositoryIdAsIntegerToSearch, this.getSearchString(), maxRows);
 		}
 		else 
 		{
 			digitalAssetVOList = SearchController.getDigitalAssets(new Integer[]{this.repositoryId}, this.getSearchString(), maxRows);
 			selectedRepositoryIdList.add("" + this.repositoryId);
 		}
 
 		return "successInlineAssetSearchV3";
 	}
 
 	public String doLatestInlineAssetsV3() throws Exception 
 	{
 		int maxRows = 30;
 		/*
 		try
 		{
 			maxRows = Integer.parseInt(CmsPropertyHandler.getMaxRows());
 			if(maxRows > 50)
 				maxRows = 30;
 		}
 		catch(Exception e)
 		{
 		}
 		*/
 
 		String[] repositoryIdToSearch = this.getRequest().getParameterValues("repositoryIdToSearch");
 		if(repositoryIdToSearch != null)
 		{
 			Integer[] repositoryIdAsIntegerToSearch = new Integer[repositoryIdToSearch.length];
 			for(int i=0; i < repositoryIdToSearch.length; i++)
 			{
 				repositoryIdAsIntegerToSearch[i] = new Integer(repositoryIdToSearch[i]);
 				selectedRepositoryIdList.add(repositoryIdToSearch[i]);
 			}
 			
 			digitalAssetVOList = SearchController.getLatestDigitalAssets(repositoryIdAsIntegerToSearch, maxRows);
 		}
 		else 
 		{
 			digitalAssetVOList = SearchController.getLatestDigitalAssets(new Integer[]{this.repositoryId}, maxRows);
 			selectedRepositoryIdList.add("" + this.repositoryId);
 		}
 
 		return "successLatestInlineAssetsV3";
 	}
 
 	/**
 	 * This method returns the advanced search interface to the user.
 	 */
 
 	public String doInput() throws Exception 
 	{
 	    this.principals = UserControllerProxy.getController().getAllUsers();
 	    this.availableLanguages = LanguageController.getController().getLanguageVOList(this.repositoryId);
 	    this.contentTypeDefinitions = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOList();
 		this.repositories = RepositoryController.getController().getAuthorizedRepositoryVOList(this.getInfoGluePrincipal(), false);
 		selectedRepositoryIdList.add("" + this.repositoryId);
 	    
 	    return Action.INPUT;
 	}
 
 	/**
 	 * This method returns the binding search interface to the user.
 	 */
 
 	public String doInputBinding() throws Exception 
 	{
 		this.repositories = RepositoryController.getController().getAuthorizedRepositoryVOList(this.getInfoGluePrincipal(), false);
 	    
 	    return Action.INPUT + "Binding";
 	}
 
 	/**
 	 * This method returns the binding search interface to the user.
 	 */
 
 	public String doInputInlineAsset() throws Exception 
 	{
 		this.repositories = RepositoryController.getController().getAuthorizedRepositoryVOList(this.getInfoGluePrincipal(), false);
 	    
 	    return Action.INPUT + "InlineAsset";
 	}
 
 	/**
 	 * This method returns the binding search interface to the user.
 	 */
 
 	public String doInputInlineAssetV3() throws Exception 
 	{
 		this.repositories = RepositoryController.getController().getAuthorizedRepositoryVOList(this.getInfoGluePrincipal(), false);
 	    
 	    return Action.INPUT + "InlineAssetV3";
 	}
 
 	public ContentVO getContentVO(Integer contentId)
 	{
 		ContentVO contentVO = null;
 		
 		try
 		{
 			if(contentId != null)
 			{
 				contentVO = ContentController.getContentController().getContentVOWithId(contentId);
 			}
 		}
 		catch(Exception e)
 		{
 			logger.error("An error occurred when we tried to get the content for this version:" + e.getMessage(), e);
 		}
 		
 		return contentVO;
 	}
 
 	public String getContentPath(Integer contentId) throws Exception
 	{
 		StringBuffer sb = new StringBuffer();
 		
 		ContentVO contentVO = ContentController.getContentController().getContentVOWithId(contentId);
 		sb.insert(0, contentVO.getName());
 		while(contentVO.getParentContentId() != null)
 		{
 			contentVO = ContentController.getContentController().getContentVOWithId(contentVO.getParentContentId());
 			sb.insert(0, contentVO.getName() + "/");
 		}
 		sb.insert(0, "/");
 		
 		return sb.toString();
 	}
 
 	public LanguageVO getLanguageVO(Integer languageId)
 	{
 		LanguageVO languageVO = null;
 		
 		try
 		{
 			if(languageId != null)
 			{
 				languageVO = LanguageController.getController().getLanguageVOWithId(languageId);
 			}
 		}
 		catch(Exception e)
 		{
 		    logger.error("An error occurred when we tried to get the language for this version:" + e.getMessage(), e);
 		}
 		
 		return languageVO;
 	}
 
 	public Integer getRepositoryId()
 	{
 		return repositoryId;
 	}
 
 	public void setRepositoryId(Integer integer)
 	{
 		repositoryId = integer;
 	}
 
     public List getAvailableLanguages()
     {
         return availableLanguages;
     }
     
     public List getContentTypeDefinitions()
     {
         return contentTypeDefinitions;
     }
     
 	public List getRepositories() 
 	{
 		return repositories;
 	}
 
     public List getPrincipals()
     {
         return principals;
     }
  
    /* 
     public String[] getContentVersionId()
     {
         return contentVersionId;
     }
     
     public void setContentVersionIds(String[] contentVersionId)
     {
         this.contentVersionId = contentVersionId;
     }
     */
     public String getReplaceString()
     {
         return replaceString;
     }
     
     public void setReplaceString(String replaceString)
     {
         this.replaceString = replaceString;
     }
     
     public Integer getCaseSensitive()
     {
         return caseSensitive;
     }
     
     public void setCaseSensitive(Integer caseSensitive)
     {
         this.caseSensitive = caseSensitive;
     }
     
     public Integer getContentTypeDefinitionId()
     {
         return contentTypeDefinitionId;
     }
     
     public void setContentTypeDefinitionId(Integer contentTypeDefinitionId)
     {
         this.contentTypeDefinitionId = contentTypeDefinitionId;
     }
     
     public Integer getInverseSearch()
     {
         return inverseSearch;
     }
     
     public void setInverseSearch(Integer inverseSearch)
     {
         this.inverseSearch = inverseSearch;
     }
     
     public Integer getLanguageId()
     {
         return languageId;
     }
     
     public void setLanguageId(Integer languageId)
     {
         this.languageId = languageId;
     }
     
     public String getName()
     {
         return name;
     }
     
     public void setName(String name)
     {
         this.name = name;
     }
     
     public Integer getStateId()
     {
         return stateId;
     }
     
     public void setStateId(Integer stateId)
     {
         this.stateId = stateId;
     }
     
     public boolean isAdvancedEnabled()
     {
         return advancedEnabled;
     }
     
     public void setAdvancedEnabled(boolean advancedEnabled)
     {
         this.advancedEnabled = advancedEnabled;
     }
 
 	public List getSelectedRepositoryIdList() 
 	{
 		return selectedRepositoryIdList;
 	}
 
     public void setAllowedContentTypeIds(String[] allowedContentTypeIds)
     {
         this.allowedContentTypeIds = allowedContentTypeIds;
     }
     
     public String getAllowedContentTypeIdsAsUrlEncodedString() throws Exception
     {
         StringBuffer sb = new StringBuffer();
         
         for(int i=0; i<allowedContentTypeIds.length; i++)
         {
             if(i > 0)
                 sb.append("&");
             
             sb.append("allowedContentTypeIds=" + URLEncoder.encode(allowedContentTypeIds[i], "UTF-8"));
         }
 
         return sb.toString();
     }
 
 	public Set getContentVOSet()
 	{
 		return contentVOSet;
 	}
 
 	public String[] getAllowedContentTypeIds()
 	{
 		return allowedContentTypeIds;
 	}
 
 	public boolean getIncludeAssets()
 	{
 		return includeAssets;
 	}
 
 	public void setIncludeAssets(boolean includeAssets)
 	{
 		this.includeAssets = includeAssets;
 	}
 	
 	public List<DigitalAssetVO> getDigitalAssetVOList()
 	{
 		return digitalAssetVOList;
 	}
 
 }
