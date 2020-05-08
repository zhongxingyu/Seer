 /*
  * Copyright: (c) 2004-2011 Mayo Foundation for Medical Education and 
  * Research (MFMER). All rights reserved. MAYO, MAYO CLINIC, and the
  * triple-shield Mayo logo are trademarks and service marks of MFMER.
  *
  * Except as contained in the copyright notice above, or as used to identify 
  * MFMER as the author of this software, the trade names, trademarks, service
  * marks, or product names of the copyright holder shall not be used in
  * advertising, promotion or otherwise in connection with this software without
  * prior written authorization of the copyright holder.
  * 
  * Licensed under the Eclipse Public License, Version 1.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at 
  * 
  * 		http://www.eclipse.org/legal/epl-v10.html
  * 
  */
 package edu.mayo.cts2.framework.plugin.service.exist.profile;
 
 import edu.mayo.cts2.framework.core.url.UrlConstructor;
 import edu.mayo.cts2.framework.filter.match.StateAdjustingComponentReference;
 import edu.mayo.cts2.framework.filter.match.StateAdjustingComponentReference.StateUpdater;
 import edu.mayo.cts2.framework.model.command.ResolvedReadContext;
 import edu.mayo.cts2.framework.model.core.*;
 import edu.mayo.cts2.framework.model.directory.DirectoryResult;
 import edu.mayo.cts2.framework.plugin.service.exist.dao.ExistResourceDao;
 import edu.mayo.cts2.framework.plugin.service.exist.dao.SummaryTransform;
 import edu.mayo.cts2.framework.plugin.service.exist.restrict.directory.XpathDirectoryBuilder.XpathState;
 import edu.mayo.cts2.framework.plugin.service.exist.util.ExistServiceUtils;
 import edu.mayo.cts2.framework.plugin.service.exist.xpath.XpathStateUpdater;
 import edu.mayo.cts2.framework.service.meta.StandardMatchAlgorithmReference;
 import edu.mayo.cts2.framework.service.meta.StandardModelAttributeReference;
 import edu.mayo.cts2.framework.service.profile.BaseQueryService;
 import org.apache.commons.lang.StringUtils;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.xmldb.api.base.Resource;
 import org.xmldb.api.base.ResourceIterator;
 import org.xmldb.api.base.ResourceSet;
 import org.xmldb.api.base.XMLDBException;
 
 import java.util.*;
 
 /**
  * The Class AbstractService.
  *
  * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
  */
 public abstract class AbstractExistQueryService
 	<R,Summary,Restrictions,
 	S extends XpathState> extends AbstractExistService
 	implements BaseQueryService, InitializingBean {
 	
 	public static final String REPLACE_INDICATOR = "(:PATH_INSERT_HERE:)";
 	
 	@javax.annotation.Resource
 	private ExistResourceDao existResourceDao;
 	
 	@javax.annotation.Resource
 	private edu.mayo.cts2.framework.core.plugin.PluginConfigManager pluginConfigManager;
 	
 	@Autowired
 	private ResourceUnmarshaller resourceUnmarshaller;
 	
 	private UrlConstructor urlConstructor;
 
 	@Override
 	public void afterPropertiesSet() throws Exception {
 		this.urlConstructor = new UrlConstructor(this.pluginConfigManager.getServerContext());
 	}
 	
 	protected ExistResourceDao getExistResourceDao() {
 		return existResourceDao;
 	}
 	protected void setExistResourceDao(ExistResourceDao existResourceDao) {
 		this.existResourceDao = existResourceDao;
 	}
 	
 	protected String getChangeSetUri(ResolvedReadContext readContext){
 		return readContext == null ? null : readContext.getChangeSetContextUri();
 	}
 	
 	public DirectoryResult<Summary> getResourceSummaries(
 			PathInfo resourceInfo,
 			String changeSetUri,
 			String collectionPath, 
 			String xpath,
 			int start, 
 			int max) {
 		
 		String queryString = (StringUtils.isNotBlank(xpath) ? xpath : "");
 		
 		int pos = queryString.indexOf(REPLACE_INDICATOR);
 		if (pos >=0)
 		{
 			queryString = queryString.substring(0, pos) + this.getResourceInfo().getResourceXpath() + queryString.substring(pos + REPLACE_INDICATOR.length());
 		}
 		else
 		{
 			queryString = this.getResourceInfo().getResourceXpath() + queryString;
 		}
 
 		String allResourcesCollectionPath = ExistServiceUtils.createPath(
 				this.getResourceInfo().getResourceBasePath(),
 				collectionPath);
 
 		ResourceSet collection;
 
 		if(StringUtils.isNotBlank(changeSetUri)){
 			String changeSetDir = 
 					ExistServiceUtils.getTempChangeSetContentDirName(changeSetUri);
 	
 			String changeSetSpecificCollectionPath = ExistServiceUtils.createPath(
 					changeSetDir,
 					this.getResourceInfo().getResourceBasePath(),
 					collectionPath);
 	
 			try {
 				ResourceSet changeSetResources = 
 						this.existResourceDao.query(
 								changeSetSpecificCollectionPath, 
 									queryString, start, max);
 				
 				ResourceSet rs = 
 						this.existResourceDao.query(
 								changeSetSpecificCollectionPath, 
 									"string-join(/"+resourceInfo.getResourceXpath() + "/" + resourceInfo.getResourceNameXpath() +", '<->')", start, max);
 			
 				String allResourcesQueryString;
 				
 				ResourceIterator itr = rs.getIterator();
 				String names = itr.nextResource().getContent().toString();
 				
 				String[] namesArray = StringUtils.split(names, "<->");
 				
 				if(namesArray != null && namesArray.length > 0){
 					
 					XqueryExceptClauseBuilder builder = new XqueryExceptClauseBuilder(resourceInfo.getResourceNameXpath());
 					for(int i=0;i<namesArray.length;i++){
 						builder.except(namesArray[i]);
 					}
 		
 					allResourcesQueryString = queryString + " except " + resourceInfo.getResourceXpath() + builder.build();
 				} else {
 					allResourcesQueryString = queryString;
 				}
 				
 				collection = this.getExistResourceDao().query(
 						allResourcesCollectionPath, allResourcesQueryString, start, max);
 				
 				queryString = queryString + " except " + resourceInfo.getResourceXpath() + "[core:changeDescription/@changeType='DELETE']";
 
 				changeSetResources = 
 						this.existResourceDao.query(
 								changeSetSpecificCollectionPath, 
 									queryString, 
 									start, 
 									max);
 
 				ResourceIterator changeSetItr = changeSetResources.getIterator();
 				
 				while(changeSetItr.hasMoreResources()){
 					collection.addResource(changeSetItr.nextResource());
 				}
 				
 			} catch (XMLDBException e) {
 				throw new IllegalStateException(e);
 			}
 		} else {
 			collection = this.getExistResourceDao().query(
 					allResourcesCollectionPath, queryString, start, max);
 		}
 
		return this.getResourceSummaries(collection, queryString, start, max,
 				transform);
 	}
 	
 	protected static class XqueryExceptClauseBuilder {
 		
 		private Set<String> query = new TreeSet<String>();
 		private String resourceNamePath;
 		
 		protected XqueryExceptClauseBuilder(String resourceNamePath){
 			this.resourceNamePath = resourceNamePath;
 		}
 		
 		protected XqueryExceptClauseBuilder except(String query){
 			this.query.add(query);
 			return this;
 		}
 		
 		protected String build(){
 			if(this.query.isEmpty()){
 				return "";
 			}
 			StringBuilder sb = new StringBuilder();
 			sb.append("[");
 			
 			Iterator<String> itr = this.query.iterator();
 			
 			while(itr.hasNext()){
 				String name = itr.next();
 				sb.append(" ");
 				sb.append(resourceNamePath);
 				sb.append(" = '");
 				sb.append(name);
 				sb.append("'");
 				if(itr.hasNext()){
 					sb.append(" or");
 				}
 			}
 			
 			sb.append("]");
 			
 			return sb.toString();
 		}
 	}
 	
 	private DirectoryResult<Summary> getResourceSummaries(
 			ResourceSet collection,
			String xpath,
 			int start,
 			int max,
 			SummaryTransform<Summary, R> transform) {
 		try {
 
 			long size = collection.getSize();
 			
 			List<Summary> returnList = new ArrayList<Summary>();
 
 		
 			for(int i=start;i< start + max && i < size;i++) {
 				Resource res = collection.getResource(i);
 			
 				@SuppressWarnings("unchecked")
 				R entry = (R) this.resourceUnmarshaller.unmarshallResource(res);
 
 				returnList.add(transform.transform(entry, res));
 			}
 		
 			return new DirectoryResult<Summary>(returnList, (start + max) >= size);
 		} catch (XMLDBException e) {
 			throw new IllegalStateException(e);
 		}
 	}
 	
 	protected <E extends ResourceDescriptionDirectoryEntry, D extends ResourceDescription> E baseTransform(
 			E summary, D resource) {
 
 		summary.setAbout(resource.getAbout());
 		summary.setFormalName(resource.getFormalName());
 		summary.setResourceSynopsis(resource.getResourceSynopsis());
 
 		return summary;
 	}
 	
 	protected <E extends ResourceVersionDescriptionDirectoryEntry, D extends ResourceVersionDescription > E baseTransformResourceVersion(
 			E summary, D resource) {
 
 		summary = this.baseTransform(summary, resource);
 		summary.setDocumentURI(resource.getDocumentURI());
 		summary.setOfficialReleaseDate(resource.getOfficialReleaseDate());
 		summary.setOfficialResourceVersionId(resource.getOfficialResourceVersionId());
 
 		return summary;
 	}
 	
 	protected abstract Summary createSummary();
 
 	protected abstract Summary doTransform(R resource, Summary summary,
 			Resource eXistResource);
 
 	private SummaryTransform<Summary, R> transform = new SummaryTransform<Summary, R>() {
 
 		@Override
 		public Summary createSummary() {
 			return AbstractExistQueryService.this.createSummary();
 		}
 
 		@Override
 		public Summary transform(R resource, Resource eXistResource) {
 			return doTransform(resource,
 					this.createSummary(), eXistResource);
 		}
 	};
 	
 	@Override
 	public Set<MatchAlgorithmReference> getSupportedMatchAlgorithms() {
 		Set<MatchAlgorithmReference> returnSet = new HashSet<MatchAlgorithmReference>();
 		
 		returnSet.add(StandardMatchAlgorithmReference.CONTAINS.getMatchAlgorithmReference());
 		returnSet.add(StandardMatchAlgorithmReference.EXACT_MATCH.getMatchAlgorithmReference());
 		returnSet.add(StandardMatchAlgorithmReference.STARTS_WITH.getMatchAlgorithmReference());
 		returnSet.add(new MatchAlgorithmReference("lucene"));
 		return returnSet;
 	}
 
 	public Set<StateAdjustingComponentReference<S>> getSupportedSearchReferences() {
 		Set<StateAdjustingComponentReference<S>> returnSet =
 				new HashSet<StateAdjustingComponentReference<S>>();
 
         StateAdjustingComponentReference<S> resourceName =
                 StateAdjustingComponentReference.toComponentReference(
 					StandardModelAttributeReference.RESOURCE_NAME.getComponentReference(),
 					getResourceNameStateUpdater());
 		
 		returnSet.add(resourceName);
 		
 		return returnSet;
 	}
 	
 	
 	
 	@Override
 	public Set<? extends ComponentReference> getSupportedSortReferences() {
 		return null;
 	}
 
 	@Override
 	public Set<PredicateReference> getKnownProperties() {
 		return null;
 	}
 
 	private StateUpdater<S> getResourceNameStateUpdater(){
 		
 		return new XpathStateUpdater<S>(
 				this.getResourceInfo().getResourceNameXpath());
 		
 	}
 
 	protected abstract PathInfo getResourceInfo();
 
 	protected UrlConstructor getUrlConstructor() {
 		return urlConstructor;
 	}
 }
