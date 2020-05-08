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
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package edu.mayo.cts2.framework.plugin.service.bioportal.rest;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.annotation.Resource;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.collections.map.LRUMap;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.SerializationUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.mapdb.DB;
 import org.mapdb.DBMaker;
 import org.springframework.beans.factory.DisposableBean;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.http.HttpEntity;
 import org.springframework.http.HttpHeaders;
 import org.springframework.http.HttpMethod;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.ResponseEntity;
 import org.springframework.web.client.HttpStatusCodeException;
 import org.springframework.web.client.RestTemplate;
 
 import com.sun.syndication.feed.synd.SyndEntry;
 import com.sun.syndication.feed.synd.SyndFeed;
 
 import edu.mayo.cts2.framework.core.plugin.PluginConfigManager;
 import edu.mayo.cts2.framework.model.command.Page;
 import edu.mayo.cts2.framework.model.command.ResolvedFilter;
 import edu.mayo.cts2.framework.model.core.PropertyReference;
 import edu.mayo.cts2.framework.model.core.URIAndEntityName;
 import edu.mayo.cts2.framework.model.core.types.TargetReferenceType;
 import edu.mayo.cts2.framework.model.exception.ExceptionFactory;
 import edu.mayo.cts2.framework.service.constant.ExternalCts2Constants;
 import edu.mayo.cts2.framework.service.meta.StandardMatchAlgorithmReference;
 
 /**
  * The Class BioportalRestService.
  *
  * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
  */
 public class BioportalRestService extends BaseCacheObservable 
 	implements InitializingBean, DisposableBean {
 	
 	public static final String BIOPORTAL_CACHE_NAME = "bioportal-cache";
 	
 	public static final String BIOPORTAL_CONFIG_NAMESPACE = "bioportal-service";
 	
 	public static final String CACHE_CONFIG_PROP = "cache";
 
 	private static final String API_KEY_PROP = "apiKey";
 	
 	private static Log log = LogFactory.getLog(BioportalRestService.class);
 	
 	@Resource
 	private BioportalRssFeedClient bioportalRssFeedClient;
 	
 	@Resource
 	private PluginConfigManager pluginConfigManager;
 	
 	private Map<String,String> cache;
 	
 	private RestTemplate restTemplate = new RestTemplate();
 	
 	private DB db;
 	
 	private String apiKey;
 	
 	private static final String API_KEY_PARAM = "apikey";
 	
 	/* every hour default */
 	private static final int DEFAULT_CACHE_UPDATE_PERIOD = 60;
 	
 	/* in minutes */
 	private int cacheUpdatePeriod = DEFAULT_CACHE_UPDATE_PERIOD;
 	
 	private static final long ONE_MINUTE = 60000L;
 	
 	private static int DEFAULT_MEM_CACHE_SIZE = 25;
 	
 	@SuppressWarnings("unchecked")
 	private Map<String,String> memCache = new LRUMap(DEFAULT_MEM_CACHE_SIZE);
 
 	private String cachePath;
 	
 	private boolean propertiesSet = false;
 	
 	public static final String PROPERTIES_NAME = "properties";
 	public static final String PROPERTIES_URI = ExternalCts2Constants.buildModelAttributeUri(PROPERTIES_NAME);
 	public static final PropertyReference PROPERTIES = new PropertyReference();
 	static {
 		PROPERTIES.setReferenceType(TargetReferenceType.PROPERTY);
 		PROPERTIES.setReferenceTarget(new URIAndEntityName());
 		PROPERTIES.getReferenceTarget().setName(PROPERTIES_NAME);
 		PROPERTIES.getReferenceTarget().setUri(PROPERTIES_URI);
 	};
 	
 	public static final String DEFINITIONS_NAME = "definitions";
 	public static final String DEFINITIONS_URI = ExternalCts2Constants.buildModelAttributeUri(DEFINITIONS_NAME);
 	public static final PropertyReference DEFINITIONS = new PropertyReference();
 	static {
 		DEFINITIONS.setReferenceType(TargetReferenceType.PROPERTY);
 		DEFINITIONS.setReferenceTarget(new URIAndEntityName());
 		DEFINITIONS.getReferenceTarget().setName(DEFINITIONS_NAME);
 		DEFINITIONS.getReferenceTarget().setUri(DEFINITIONS_URI);
 	};
 	
 	@Override
 	public void destroy() throws Exception {
 		log.info("Shutting down... writing cache to file.");
 		this.writeCache();
 		this.db.close();
 	}
 
 	protected Map<String, String> createCache(File file) {
 		this.db = DBMaker.newFileDB(file).
				randomAccessFileEnable().
 				closeOnJvmShutdown().
 				make();
 		
 		return db.getHashMap(BIOPORTAL_CACHE_NAME);
 	}
 
 	public String getLatestViews(boolean forceRefresh){
 		String url = buildGetLatestViewsUrl();
 
 		String xml = this.doCallBioportal(url, forceRefresh);
 
 		return xml;
 	}
 	
 	public String getLatestViews(){
 		return this.getLatestViews(false);
 	}
 	
 	/**
 	 * Builds the get latest views url.
 	 *
 	 * @return the string
 	 */
 	private String buildGetLatestViewsUrl(){
 		String url = "http://rest.bioontology.org/bioportal/views?";
 		
 		return url;
 	}
 
 	/**
 	 * Gets the latest ontology versions.
 	 *
 	 * @return the latest ontology versions
 	 */
 	public String getLatestOntologyVersions(){
 		return this.getLatestOntologyVersions(false);
 	}
 	
 	public String getLatestOntologyVersions(boolean forceRefresh){
 		String url = buildGetLatestOntologyVersionsUrl();
 
 		String xml = this.doCallBioportal(url, forceRefresh);
 
 		return xml;
 	}
 	
 	/**
 	 * Builds the get latest ontology versions url.
 	 *
 	 * @return the string
 	 */
 	private String buildGetLatestOntologyVersionsUrl(){
 		String url = "http://rest.bioontology.org/bioportal/ontologies?";
 		
 		return url;
 	}
 	
 	/**
 	 * Purge get latest ontology versions.
 	 */
 	private void purgeGetLatestOntologyVersions(){
 		String url = buildGetLatestOntologyVersionsUrl();
 		
 		this.purgeCache(url);
 	}
 	
 	/**
 	 * Gets the entity by ontology version id and entity id.
 	 *
 	 * @param ontologyVersionId the ontology version id
 	 * @param entityId the entity id
 	 * @return the entity by ontology version id and entity id
 	 */
 	public String getEntityByOntologyVersionIdAndEntityId(String ontologyVersionId, String entityId){		
 		String url = "http://rest.bioontology.org/bioportal/concepts/" + ontologyVersionId + "?" 
 				+ "conceptid=" + entityId;
 
 		String xml = this.doCallBioportalMemCache(url);
 
 		return xml;
 	}
 	
 	/**
 	 * Gets the hierarchy roots by ontoloty version id.
 	 *
 	 * @param ontologyVersionId the ontology version id
 	 * @return the hierarchy roots by ontoloty version id
 	 */
 	public String getHierarchyRootsByOntolotyVersionId(String ontologyVersionId){
 		String url = "http://rest.bioontology.org/bioportal/concepts/" + ontologyVersionId + "/root";
 
 		String xml = this.doCallBioportalMemCache(url);
 
 		return xml;
 	}
 	
 
 	/**
 	 * Gets the entity by uri.
 	 *
 	 * @param uri the uri
 	 * @return the entity by uri
 	 */
 	public String getEntityByUri(String uri) {
 		throw new UnsupportedOperationException();
 	}
 	
 	/**
 	 * Gets the latest ontology version by ontology id.
 	 *
 	 * @param ontologyId the ontology id
 	 * @return the latest ontology version by ontology id
 	 */
 	public String getLatestOntologyVersionByOntologyId(String ontologyId){
 		String url = this.buildGetLatestOntologyVersionByOntologyIdUrl(ontologyId);
 
 		String xml = this.doCallBioportal(url);
 
 		return xml;
 	}
 	
 	/**
 	 * Builds the get latest ontology version by ontology id url.
 	 *
 	 * @param ontologyId the ontology id
 	 * @return the string
 	 */
 	private String buildGetLatestOntologyVersionByOntologyIdUrl(String ontologyId){
 		String url = "http://rest.bioontology.org/bioportal/virtual/ontology/" + ontologyId;
 		
 		return url;
 	}
 	
 	/**
 	 * Purge get latest ontology version by ontology id.
 	 *
 	 * @param ontologyId the ontology id
 	 */
 	private void purgeGetLatestOntologyVersionByOntologyId(String ontologyId){
 		String url = buildGetLatestOntologyVersionByOntologyIdUrl(ontologyId);
 		
 		this.purgeCache(url);
 	}
 	
 	/**
 	 * Gets the ontology by ontology version id.
 	 *
 	 * @param ontologyVersionId the ontology version id
 	 * @return the ontology by ontology version id
 	 */
 	public String getOntologyByOntologyVersionId(String ontologyVersionId){
 		String url = "http://rest.bioontology.org/bioportal/ontologies/" + ontologyVersionId;
 
 		String xml = this.doCallBioportal(url);
 
 		return xml;
 	}
 	
 	/**
 	 * Gets the latest ontology version by virtual id.
 	 *
 	 * @param virtualId the virtual id
 	 * @return the latest ontology version by virtual id
 	 */
 	public String getLatestOntologyVersionByVirtualId(String virtualId){
 		String url = "http://rest.bioontology.org/bioportal/virtual/ontology/" + virtualId;
 
 		String xml = this.doCallBioportal(url);
 
 		return xml;
 	}
 
 	public String getOntologyVersionsByOntologyId(String ontologyId, boolean forceRefresh){
 		String url = buildGetOntologyVersionsByOntologyIdUrl(ontologyId);
 
 		String xml = this.doCallBioportal(url, forceRefresh);
 
 		return xml;
 	}
 	
 	public String getOntologyVersionsByOntologyId(String ontologyId){
 		return this.getOntologyVersionsByOntologyId(ontologyId, false);
 	}
 	
 	/**
 	 * Gets the view versions by ontology id.
 	 *
 	 * @param ontologyId the ontology id
 	 * @return the view versions by ontology id
 	 */
 	public String getViewVersionsByOntologyId(String ontologyId){
 		String url = buildGetViewVersionsByOntologyIdUrl(ontologyId);
 
 		String xml = this.doCallBioportal(url);
 
 		return xml;
 	}
 	
 	/**
 	 * Builds the get ontology versions by ontology id url.
 	 *
 	 * @param ontologyId the ontology id
 	 * @return the string
 	 */
 	private String buildGetOntologyVersionsByOntologyIdUrl(String ontologyId){
 		String url = "http://rest.bioontology.org/bioportal/ontologies/versions/" + ontologyId;
 		
 		return url;
 	}
 	
 	/**
 	 * Builds the get view versions by ontology id url.
 	 *
 	 * @param ontologyId the ontology id
 	 * @return the string
 	 */
 	private String buildGetViewVersionsByOntologyIdUrl(String ontologyId){
 		String url = "http://rest.bioontology.org/bioportal/views/versions/" + ontologyId;
 		
 		return url;
 	}
 	
 	/**
 	 * Purge get ontology versions by ontology id.
 	 *
 	 * @param ontologyId the ontology id
 	 */
 	private void purgeGetOntologyVersionsByOntologyId(String ontologyId){
 		String url = buildGetOntologyVersionsByOntologyIdUrl(ontologyId);
 		
 		this.purgeCache(url);
 	}
 
 	/**
 	 * Gets the all entities by ontology version id.
 	 *
 	 * @param ontologyVersionId the ontology version id
 	 * @param page the page
 	 * @return the all entities by ontology version id
 	 */
 	public String getAllEntitiesByOntologyVersionId(String ontologyVersionId, Page page){
 		String 
 			url = "http://rest.bioontology.org/bioportal/concepts/" + ontologyVersionId + "/all" +
 			"?pagenum=" + page.getPage() +
 			"&pagesize=" + page.getMaxToReturn();
 
 		String xml = this.doCallBioportal(url);
 
 		return xml;
 	}
 	
 	/**
 	 * Search entities by ontology id.
 	 *
 	 * @param ontologyId the ontology id
 	 * @param filter the filter
 	 * @param page the page
 	 * @return the string
 	 */
 	public String searchEntitiesByOntologyId(String ontologyId, ResolvedFilter filter, Page page){
 		return this.doSearchEntities(Arrays.asList(ontologyId), filter, page);
 	}
 	
 	/**
 	 * Search entities by ontology ids.
 	 *
 	 * @param ontologyIds the ontology ids
 	 * @param filter the filter
 	 * @param page the page
 	 * @return the string
 	 */
 	public String searchEntitiesByOntologyIds(Collection<String> ontologyIds, ResolvedFilter filter, Page page){
 		return this.doSearchEntities(ontologyIds, filter, page);
 	}
 	
 	/**
 	 * Search entities of latest ontology versions.
 	 *
 	 * @param filter the filter
 	 * @param page the page
 	 * @return the string
 	 */
 	public String searchEntitiesOfLatestOntologyVersions(ResolvedFilter filter, Page page){
 		return this.doSearchEntities(null, filter, page);
 	}
 	
 	/**
 	 * Gets the updates.
 	 *
 	 * @return the updates
 	 */
 	public String getUpdates(){
 		return this.callBioportal("http://bioportal.bioontology.org/syndication/rss");
 	}
 
 	/**
 	 * Do search entities.
 	 *
 	 * @param ontologyIds the ontology ids
 	 * @param filter the filter
 	 * @param page the page
 	 * @return the string
 	 */
 	protected String doSearchEntities(Collection<String> ontologyIds, ResolvedFilter filter, Page page){
 		String url = "http://rest.bioontology.org/bioportal/search/" + filter.getMatchValue() +
 			"?pagenum=" + (page.getPage() + 1) +
 			"&pagesize=" + page.getMaxToReturn();
 		
 		StringBuffer sb = new StringBuffer();
 		sb.append(url);
 	
 		if(CollectionUtils.isNotEmpty(ontologyIds)){
 			Iterator<String> itr = ontologyIds.iterator();
 			sb.append("&ontologyids=" + itr.next());
 			
 			while(itr.hasNext()){
 				sb.append("," + itr.next());
 			}
 		}
 		
 		String algorithm = filter.getMatchAlgorithmReference().getContent();
 		
 		if(algorithm.equals(StandardMatchAlgorithmReference.EXACT_MATCH.
 			getMatchAlgorithmReference().getContent())){
 			sb.append("&isexactmatch=1");
 		}
 
 		sb.append(
 				this.getBioportalQueryStringForFilter(filter));
 		
 		String xml = this.doCallBioportalMemCache(sb.toString());
 
 		return xml;
 	}
 	
 	/**
 	 * Gets the bioportal query string for filter.
 	 *
 	 * @param filter the filter
 	 * @return the bioportal query string for filter
 	 * 
 	 */
 	protected String getBioportalQueryStringForFilter(ResolvedFilter filter) {
 		StringBuffer sb = new StringBuffer();
 
 		URIAndEntityName target = filter.getPropertyReference().getReferenceTarget();
 		
 		if(StringUtils.equals(target.getName(),DEFINITIONS_NAME)){
 			sb.append("&includedefinitions=true");
 		} else if(StringUtils.equals(target.getName(),PROPERTIES_NAME)){
 			sb.append("&includeproperties=true");
 		}
 			
 		return sb.toString();
 	}
 	
 	/**
 	 * Append api key.
 	 *
 	 * @param url the url
 	 * @return the string
 	 */
 	private String appendApiKey(String url){
 		String fullUrl = url + ( url.contains("?") ? "&" : "?") + API_KEY_PARAM + "=" + this.apiKey;
 		
 		return fullUrl;
 	}
 	
 	protected String doCallBioportal(String url){
 		return this.doCallBioportal(url, false);
 	}
 	/**
 	 * Do call bioportal.
 	 *
 	 * @param url the url
 	 * @return the string
 	 */
 	protected String doCallBioportal(String url, boolean forceRefresh){
 		String fullUrl = this.appendApiKey(url);
 		
 		if(forceRefresh){
 			String xml = this.callBioportal(fullUrl);
 			
 			this.cache.put(fullUrl, xml);
 			
 			this.writeCache();
 			
 			return xml;
 		} else {
 			if(!this.cache.containsKey(fullUrl)){
 		
 				String xml = this.callBioportal(fullUrl);
 				
 				this.cache.put(fullUrl, xml);
 	
 				this.writeCache();
 				
 				return xml;
 			} else {
 				return new String(this.cache.get(fullUrl));
 			}
 		}
 	}
 	
 	/**
 	 * Purge cache.
 	 *
 	 * @param url the url
 	 */
 	protected void purgeCache(String url){
 		String fullUrl = this.appendApiKey(url);
 		
 		synchronized(cache){
 		
 			this.cache.remove(fullUrl);
 			
 			try {
 				this.writeCache();
 			} catch (Exception e) {
 				throw new RuntimeException(e);
 			}
 		}
 	}
 	
 	/**
 	 * Do call bioportal mem cache.
 	 *
 	 * @param url the url
 	 * @return the string
 	 */
 	protected String doCallBioportalMemCache(String url){
 		String fullUrl = url + ( url.contains("?") ? "&" : "?") + API_KEY_PARAM + "=" + this.apiKey;
 		
 		if(! this.memCache.containsKey(fullUrl)){
 	
 			String xml = this.callBioportal(fullUrl);
 			
 			this.memCache.put(fullUrl, xml);
 		}
 		
 		return new String(this.memCache.get(fullUrl));
 	}
 	
 	/**
 	 * Call bioportal.
 	 *
 	 * @param url the url
 	 * @return the string
 	 */
 	protected String callBioportal(String url){
 		log.info("Calling Bioportal REST: " + url);
 		HttpHeaders headers = new HttpHeaders();
 		headers.set( "Accept", "application/xml" );
 		
 		ResponseEntity<String> response;
 		
 		try {
 			response = this.restTemplate.exchange(
 					url, 
 					HttpMethod.GET, 
 					new HttpEntity<Void>(headers), 
 					String.class);
 		} catch (HttpStatusCodeException e) {
 			if(e.getStatusCode().equals(HttpStatus.NOT_FOUND)){
 				throw e;
 			} else {
 				log.error("Error calling BioPortal REST Service", e);
 				throw ExceptionFactory.createUnknownException("Error calling NCBO BioPortal: " + e.getMessage());
 			}
 		}
 		
 		return response.getBody();
 	}
 	
 	protected void setApiKey(){
 		//check for environment variable
 		if(StringUtils.isNotBlank(this.apiKey)){
 			log.info("Using APIKEY from pre-set Property.");
 			
 			return;
 		}
 		
 		String apiKeyEnvVar = System.getProperty(API_KEY_PROP);
 		if(StringUtils.isNotBlank(apiKeyEnvVar)){
 			log.info("Using APIKEY from System Property.");
 			this.apiKey = apiKeyEnvVar;
 			
 			return;
 		}
 
 		log.warn("No Bioportal API Key Set! Please sent one via the System Variable: " 
 				+ API_KEY_PROP + " or in the Bioportal config file.");
 	}
 	
     /* (non-Javadoc)
      * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
      */
 	public void afterPropertiesSet() throws IOException {
     	this.propertiesSet = true;
     	
     	this.setApiKey();
     	
     	if(StringUtils.isBlank(this.cachePath)){
     		this.cachePath = 
 				this.pluginConfigManager.getPluginWorkDirectory(BIOPORTAL_CONFIG_NAMESPACE).
 					getPath() + File.separator + "cache";
     	}
 
 		File file = getCacheFile();
 		
 		if(! file.exists()){
 			log.info("Creating new cache - " + file.getPath());
 				
 			file.getParentFile().mkdirs();
 		}
 			
 		this.cache = this.createCache(file);
 		
 		//not sure this is helping the cause.
 		//this.startRssChangeTimer();
 	}
     
     private File getCacheFile(){
     	String cacheFilePath = 
 	    		this.cachePath + "/cache.out";
     	
     	return new File(cacheFilePath);
     }
     
     private File getUpdateLogFile(){
 	    String updateLogPath = 
 	    		this.cachePath + "/updateLog.out";
 	    
 	    return new File(updateLogPath);
     }
     
     /**
      * Start rss change timer.
      */
     public void startRssChangeTimer(){
     	Timer timer = new Timer(true);
     	timer.scheduleAtFixedRate(new TimerTask(){
 
 			@Override
 			public void run() {
 				checkForUpdates(
 						bioportalRssFeedClient.getBioportalRssFeed());
 			}
     		
     	}, 0, ONE_MINUTE * this.cacheUpdatePeriod);
     }
     
     /**
      * Check for updates.
      *
      * @param feed the feed
      */
     protected void checkForUpdates(SyndFeed feed){
         try {
             Date lastUpdate = this.getLastUpdate();
 
             Date lastUpdateFromFeed = this.getLastUpdateFromFeed(feed);
 
             //account for a null feed coming back from bioportal
             if(lastUpdateFromFeed != null &&
                     (lastUpdate == null || lastUpdateFromFeed.after(lastUpdate)) ){
                 List<String> ontologyIds =
                     this.getUpdatedOntologies(feed, lastUpdateFromFeed);
 
                 for(String ontologyId : ontologyIds) {
                     this.purgeGetLatestOntologyVersions();
                     this.purgeGetLatestOntologyVersionByOntologyId(ontologyId);
                     this.purgeGetOntologyVersionsByOntologyId(ontologyId);
                 }
 
                 this.writeUpdateLog(lastUpdateFromFeed);
 
                 this.fireOnCodeSystemsChangeEvent(ontologyIds);
             }
         } catch(Exception e){
             log.warn("Error reading RSS feed.", e);
         }
     }
     
     /**
      * Gets the last update from feed.
      *
      * @param feed the feed
      * @return the last update from feed
      */
     protected Date getLastUpdateFromFeed(SyndFeed feed){
     	Date latestDate = null;
     	
     	for(Object entry : feed.getEntries()){
 			SyndEntry  syndEntry = (SyndEntry)entry;
 			DateModule date = (DateModule) syndEntry.getModule(DateModule.URI);
 			Date foundDate = date.getDate();
 			if(latestDate == null || foundDate.after(latestDate)){
 				latestDate = foundDate;
 			}
 		}
     	
     	return latestDate;
     }
     
     /**
      * Gets the updated ontologies.
      *
      * @param feed the feed
      * @param fromDate the from date
      * @return the updated ontologies
      */
     protected List<String> getUpdatedOntologies(SyndFeed feed, Date fromDate){
   
     	List<String> ontologyIdList = new ArrayList<String>();
     	
     	for(Object entry : feed.getEntries()){
 			SyndEntry  syndEntry = (SyndEntry)entry;
 			DateModule date = (DateModule) syndEntry.getModule(DateModule.URI);
 			Date foundDate = date.getDate();
 			if(foundDate.after(foundDate)){
 				ontologyIdList.add(
 						StringUtils.substringAfterLast(syndEntry.getLink(), "/"));
 			}
 		}
     	
     	return ontologyIdList;
     }
 
 	/**
 	 * Write cache.
 	 *
 	 * @throws IOException Signals that an I/O exception has occurred.
 	 */
 	private void writeCache() {
 		this.db.commit();
 	}
 	
 	/**
 	 * Gets the last update.
 	 *
 	 * @return the last update
 	 */
 	protected Date getLastUpdate(){
 		File file = this.getUpdateLogFile();
 		if(! file.exists()){
 			return null;
 		} else {
 			byte[] data;
 			try {
 				data = FileUtils.readFileToByteArray(file);
 			} catch (IOException e) {
 				throw new RuntimeException(e);
 			}
 			Date date = (Date)SerializationUtils.deserialize(data);
 			
 			return date;
 		}
 	}
 	
 	/**
 	 * Write update log.
 	 *
 	 * @param lastUpdate the last update
 	 */
 	private void writeUpdateLog(Date lastUpdate) {
 		log.info("Writing update log");
 		
 		File file = this.getUpdateLogFile();
 		
 		try {
 			if(! file.exists()){
 				log.info("Creating new update log file at: " + file.getPath());
 				file.getParentFile().mkdirs();
 				file.createNewFile();
 			}
 
 			byte[] data = SerializationUtils.serialize(lastUpdate);
 			FileUtils.writeByteArrayToFile(file, data);
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	/**
 	 * Sets the cache update period.
 	 *
 	 * @param cacheUpdatePeriod the new cache update period
 	 */
 	public void setCacheUpdatePeriod(int cacheUpdatePeriod) {
 		this.cacheUpdatePeriod = cacheUpdatePeriod;
 	}
 
 	/**
 	 * Gets the cache update period.
 	 *
 	 * @return the cache update period
 	 */
 	public int getCacheUpdatePeriod() {
 		return cacheUpdatePeriod;
 	}
 
 	public String getCachePath() {
 		return cachePath;
 	}
 
 	public void setCachePath(String cachePath) {
 		this.cachePath = cachePath;
 	}
 
 	public String getApiKey() {
 		return apiKey;
 	}
 
 	public void setApiKey(String apiKey) {
 		this.apiKey = apiKey;
 		if(this.propertiesSet){
 			this.fireApiKeyChangeEvent();
 		}
 	}
 }
