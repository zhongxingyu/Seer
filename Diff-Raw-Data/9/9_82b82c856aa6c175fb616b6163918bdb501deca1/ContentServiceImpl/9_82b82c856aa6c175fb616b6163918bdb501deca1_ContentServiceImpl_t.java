 package com.fauxwerd.service;
 
 import java.io.Closeable;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.Writer;
 import java.net.URL;
 import java.nio.charset.Charset;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.io.FileUtils;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.parser.Tag;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.web.client.RestClientException;
 import org.springframework.web.client.RestTemplate;
 
 import com.fauxwerd.dao.ContentDAO;
 import com.fauxwerd.model.Content;
 import com.fauxwerd.model.ContentStatus;
 import com.fauxwerd.model.Site;
 import com.fauxwerd.model.User;
 import com.fauxwerd.model.UserContent;
 import com.fauxwerd.util.ParseUtil;
 
 @Service("contentService")
 public class ContentServiceImpl implements ContentService, ApplicationContextAware{
 	
 	final Logger log = LoggerFactory.getLogger(getClass());
 	
 	private ApplicationContext appContext;
 	
     @javax.annotation.Resource(name = "site")
     private Map<String, String> siteProperties;
 	
     @Autowired
     private ContentDAO contentDAO;
  
     @Transactional
     public void addContent(Content content) {
         contentDAO.addContent(content);
     }
     
     @Transactional 
     public void addContent(URL url, User user, String title) {
 		String siteHostname = url.getAuthority();
 		
 		if (log.isDebugEnabled()) {
 			log.debug(String.format("Site = %s", siteHostname));
 		}
 		
 		//check if Site has already been saved
 		Site site = null;
 		Site existingSite = getSiteByHostname(siteHostname);
 		
 		if (existingSite != null) {
 			site = existingSite;
 		}
 		else {
 			site = new Site();
 			site.setHostname(siteHostname);
 			addSite(site);
 		}		
 				
 		//check if this content has been saved before
 		Content content = null;
 		UserContent userContent = null;
 		Content existingContent = getContentByUrl(url.toString());
 		
 		if (existingContent != null) {
 			content = existingContent;
 			
 			//TODO check if the content has been updated at the source and update record accordingly
 			//resetting status to saved will cause scheduled jobs to refetch content
 			content.setTitle(title);
 			content.setStatus(ContentStatus.SAVED);
 			updateContent(content);
 			
 			//check if this user has saved this content before
 			if (log.isDebugEnabled()) {
 				log.debug(String.format("Saving an existing piece of content, id = %s", content.getId()));
 			}
 						
 			for (UserContent someUserContent : user.getUserContent()) {
 				if (content.getUrl().equals(someUserContent.getContent().getUrl())) {
 					userContent = someUserContent;
 					if (log.isDebugEnabled()) log.debug(String.format("currently saved for this user: %s", someUserContent.getId()));
 				}
 			}
 		}
 		else {		
 			if (log.isDebugEnabled()) {
 				log.debug("adding new record to content table");
 			}
 			
 			content = new Content();
 			content.setUrl(url.toString());
 			content.setTitle(title);
 			content.setSite(site);
 			content.setStatus(ContentStatus.SAVED);
 	
 			addContent(content);
 		}
 		
 		if (userContent == null) {
 			if (log.isDebugEnabled()) {
 				log.debug("adding new user content record");
 			}			
 			userContent = new UserContent(user, content);					
 			addUserContent(userContent);
 		}
 		else {
 			if (log.isDebugEnabled()) {
 				log.debug("updating existing user content record");
 			}
 			//TODO update - this isn't actually updating anyting at the moment 			
 			updateUserContent(userContent);
 		}
     	
     }
     
     @Transactional
 	public void addSite(Site site) {
     	contentDAO.addSite(site);
     }
     
     @Transactional
     public void addUserContent(UserContent userContent) {
     	contentDAO.addUserContent(userContent);
     }
     
     @Transactional
     public void updateContent(Content content) {
     	contentDAO.updateContent(content);
     }
     
     @Transactional
     public void updateUserContent(UserContent userContent) {
     	contentDAO.updateUserContent(userContent);
     }
     
     @Transactional
     public Content getContentById(Long id) {
     	return contentDAO.getContentById(id);
     }
     
     @Transactional
     public Content getContentByUrl(String url) {
     	return contentDAO.getContentByUrl(url);
     }
     
     @Transactional
     public Site getSiteByHostname(String hostname) {
     	return contentDAO.getSiteByHostname(hostname);
     }
  
     @Transactional
     public List<Content> listContent() { 
         return contentDAO.listContent();
     }
     
     @Transactional
     public List<Content> listContentByStatus(ContentStatus status) {
     	return contentDAO.listContentByStatus(status);
     }
     
     @Transactional
     public List<Content> fetchContent() {
 
 		List<Content> savedContent = listContentByStatus(ContentStatus.SAVED);
 		
 		for (Content content : savedContent) {
 			
 			if (log.isDebugEnabled()) {
 				log.debug(String.format("fetching %s", content.getUrl()));
 			}
 			
 			RestTemplate restTemplate = new RestTemplate();
 
 			String rawHtml = null;
 			
 			try {
 				rawHtml = restTemplate.getForObject(content.getUrl(), String.class);
 												
 				String dataDirectoryPath = siteProperties.get("dataStore");			
 
 				String siteDirectoryPath = content.getSite().getHostname();
 				siteDirectoryPath = siteDirectoryPath.replace('.', '_');
 				
 	        	File file = new File(dataDirectoryPath + "/" + siteDirectoryPath + "/" + content.getId().toString() + "-raw.html");
 	        	
 	        	FileUtils.writeStringToFile(file, rawHtml, "utf-8");
 				
 				content.setRawHtmlPath(file.getAbsolutePath());
 				content.setStatus(ContentStatus.FETCHED);
 				updateContent(content);
 				
 				if (log.isDebugEnabled()) {
 					log.debug(String.format("saved to %s", file.getAbsolutePath()));
 				}
 				
 			}
 			catch (RestClientException e) {
 				if (log.isErrorEnabled()) {
 					log.error("", e);
 				}
 				
 				//set content status to FETCH_ERROR				
 				content.setStatus(ContentStatus.FETCH_ERROR);
 				updateContent(content);				
 			}
 			catch (IOException e) {
 				if (log.isErrorEnabled()) {
 					log.error("", e);
 				}
 				//set content status to FETCH_ERROR				
 				content.setStatus(ContentStatus.FETCH_ERROR);
 				updateContent(content);								
 			}
 			catch (IllegalArgumentException e) {
 				if (log.isErrorEnabled()) {
 					log.error("", e);
 				}
 				//set content status to FETCH_ERROR				
 				content.setStatus(ContentStatus.FETCH_ERROR);
 				updateContent(content);								
 			}
 					
 		}
     	    	
     	return savedContent;
     }
         
     @Transactional
     public List<Content> parseContent() {
 		List<Content> fetchedContent = listContentByStatus(ContentStatus.FETCHED);
 		
 		for (Content content : fetchedContent) {		
 
 			if (log.isDebugEnabled()) {
 				log.debug(String.format("parsing %s", content.getRawHtmlPath()));
 			}
 			
 			try {				
 				File file = new File(content.getRawHtmlPath());
 				
				Long filesize = FileUtils.sizeOf(file);
				
				if (filesize.intValue() == 0) {
					if (log.isErrorEnabled()) log.error("raw html file is empty");					
					content.setStatus(ContentStatus.PARSE_ERROR);
					updateContent(content);
					return null;
				}
				
 				String baseUrl = ParseUtil.findBaseUrl(content.getUrl());
 				
 				if (log.isDebugEnabled()) log.debug(String.format("baseUrl = %s", baseUrl));
 				
 				Document doc = Jsoup.parse(file, "UTF-8", baseUrl);
 				
 				//remove all script tags
 				doc.select("script").remove();
 				
 				ParseUtil.prepDocument(doc);
 								
 				Element body = ParseUtil.grabArticle(doc);
 
 				//TODO this isn't currently used for anything, but should be
 				Element title = ParseUtil.getArticleTitle(doc);
 												
 				if (log.isDebugEnabled()) log.debug(String.format("title = %s", title));
 
 				//TODO this is commented out b/c I don't think we need it anymore - leaving until this is verified				
 //				// fix relative image refs				
 //				List<Element> imgs = body.select("img[src~=^/.]");
 //				
 //				if (log.isDebugEnabled()) log.debug(String.format("fixing %s img tags", imgs.size()));
 //				
 //				for (Element img : imgs) {
 //					img.attr("src", String.format("http://%s%s", content.getSite().getHostname(), img.attr("src")));
 //				}
 //				
 //				// fix relative hrefs
 //				List<Element> hrefs = body.select("a[href~=^/.]");
 //				
 //				if (log.isDebugEnabled()) log.debug(String.format("fixing %s href tags", imgs.size()));
 //				
 //				for (Element href : hrefs) {
 //					href.attr("href", String.format("http://%s$s", content.getSite().getHostname(), href.attr("href")));
 //				}
 				
 				String parsedHtml = body != null ? body.html() : null;
 				
 				if (parsedHtml == null) {
 					Element articleContent = new Element(Tag.valueOf("body"), "");
 					articleContent.attr("id", "fauxwerd-content");
 					articleContent.html("sorry couldn't parse");
 //					nextPageLink = null;
 					parsedHtml = articleContent.html();
 				}
 																
 				String dataDirectoryPath = siteProperties.get("dataStore");
 	        	
 				String siteDirectoryPath = content.getSite().getHostname();
 				siteDirectoryPath = siteDirectoryPath.replace('.', '_');
 	        	
 	        	File parsedFile = new File(dataDirectoryPath + "/" + siteDirectoryPath + "/" + content.getId().toString() + "-parsed.html");
 	        		        		        	
 	        	FileUtils.writeStringToFile(parsedFile, parsedHtml, "utf8");
 	        		        	
 	        	content.setParsedHtmlPath(parsedFile.getAbsolutePath());
 				content.setStatus(ContentStatus.PARSED);
 				updateContent(content);
 				
 				if (log.isDebugEnabled()) {
 					log.debug(String.format("parsed to %s", parsedFile.getAbsolutePath()));
 				}
 								
 			} catch (IOException e) {
 				if (log.isErrorEnabled()) log.error(null, e);
 				
 				content.setStatus(ContentStatus.PARSE_ERROR);
 				updateContent(content);
 				
 			}
 
 		}
 		
 		return fetchedContent;    	
     }
         
     //implement ApplicationContextAware
     public void setApplicationContext(ApplicationContext appContext) {
     	this.appContext = appContext;
     }
 }
 
 
  
