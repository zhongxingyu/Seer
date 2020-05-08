 /**********************************************************************************
  * $URL: https://source.sakaiproject.org/contrib/tfd/trunk/sdata/sdata-tool/impl/src/java/org/sakaiproject/sdata/tool/JCRDumper.java $
  * $Id: JCRDumper.java 45207 2008-02-01 19:01:06Z ian@caret.cam.ac.uk $
  ***********************************************************************************
  *
  * Copyright (c) 2008 The Sakai Foundation.
  *
  * Licensed under the Educational Community License, Version 1.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.opensource.org/licenses/ecl1.php
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  **********************************************************************************/
 
 package org.sakaiproject.sdata.services.rss;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.sakaiproject.exception.IdUnusedException;
 import org.sakaiproject.sdata.tool.api.ServiceDefinition;
 import org.sakaiproject.site.api.Site;
 import org.sakaiproject.site.api.SiteService;
 import org.sakaiproject.site.api.SiteService.SelectionType;
 import org.sakaiproject.site.api.SiteService.SortType;
 import org.sakaiproject.tool.api.Session;
 import org.sakaiproject.tool.api.SessionManager;
 
 import java.io.InputStream;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.ErrorHandler;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException;
 
 /**
  * TODO Javadoc
  * 
  * @author
  */
 public class RssBean implements ServiceDefinition {
 
 	private Map<String, Object> map2 = new HashMap<String, Object>();;
 	private RssFeed rssFeed = new RssFeed();
 
 	/**
 	 * TODO Javadoc
 	 * 
 	 * @param sessionManager
 	 * @param siteService
 	 */
 	public RssBean(HttpServletRequest request, HttpServletResponse response) {
 		
 		String[] feeds = request.getParameter("feeds").toString().split("[*]");		
 		
  		for (String feed: feeds){ 	
  			
 			try {
 				
 				String url = feed.split("[_][_][_]")[0];
 				String name = feed.split("[_][_][_]")[1];
 				
 				HttpClient httpClient = new HttpClient();
 				GetMethod getMethod = new GetMethod(url);
 				int responseCode = httpClient.executeMethod(getMethod);
 	
 				if (responseCode != 200) {
 					//map2.put("status", "failed");
 				}
 	
 				DocumentBuilderFactory factory = DocumentBuilderFactory
 						.newInstance();
 				factory.setValidating(false);
 				factory.setNamespaceAware(true);
 				factory.setIgnoringElementContentWhitespace(true);
 				factory.setIgnoringComments(true);
 				DocumentBuilder builder = factory.newDocumentBuilder();
 				builder.setErrorHandler(new ErrorHandler() {
 					public void warning(SAXParseException e) throws SAXException {
 						System.out.println(e);
 						throw e;
 					}
 	
 					public void error(SAXParseException e) throws SAXException {
 						System.out.println(e);
 						throw e;
 					}
 	
 					public void fatalError(SAXParseException e) throws SAXException {
 						System.out.println(e);
 						throw e;
 					}
 				});
 	
 				InputStream in = getMethod.getResponseBodyAsStream();
 				Document doc = builder.parse(in);
 				Node root = doc.getDocumentElement();
 	
 				// this section supports RSS
 				NodeList channels = doc.getElementsByTagName("channel");
 				for (int i = 0; i < channels.getLength(); i++) {
 					NodeList nodes = channels.item(i).getChildNodes();
 					for (int j = 0; j < nodes.getLength(); j++) {
 						Node n = nodes.item(j);
 	
 						if (n.getNodeName().equals("item")) {
 							RssItem rssItem = loadRssItem(n, name);
 							rssFeed.addItem(rssItem);
 						}
 					}
 				}
 	
 				// this section supports RDF (a variation of RSS)
 				// ideally RSS and RDF parsing would be separated, but this will
 				// suffice for this simple example :-)
 				//NodeList items = doc.getElementsByTagName("item");
 				//for (int i = 0; i < items.getLength(); i++) {
 				//	RssItem rssItem = loadRssItem(items.item(i), name);
 				//	rssFeed.addItem(rssItem);
 				//}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 
 		}
  		
  		List<RssItem> items = rssFeed.getItems();
  		Collections.sort(items);
  		
  		map2.put("items", items);
 		
 	}
 
 	private RssItem loadRssItem(Node root, String name) throws Exception {
 		String title = null;
 		String link = null;
 		String description = null;
 		Date pubDate = null;
 
 		NodeList nodes = root.getChildNodes();
 		for (int i = 0; i < nodes.getLength(); i++) {
 			Node n = nodes.item(i);
 
 			if (n.getNodeName().equals("title")) {
 				title = getTextValue(n);
 			}
 
 			if (n.getNodeName().equals("link")) {
 				link = getTextValue(n);
 			}
 			
 			if (n.getNodeName().equals("description")) {
 				description = getTextValue(n);
 			}
 			
 			if (n.getNodeName().equals("pubDate")) {
 				String sDate = getTextValue(n);
 				pubDate = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z").parse(sDate);
 			}
 		}
 
 		RssItem item = new RssItem();
 		item.setTitle(title);
 		item.setLink(link);
 		item.setContent(description);
 		item.setPubDate(pubDate);
 		item.setName(name);
 		
 		return item;
 	}
 
 	private String getTextValue(Node node) {
 		if (node.hasChildNodes()) {
 			return node.getFirstChild().getNodeValue();
 		} else {
 			return "";
 		}
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.sakaiproject.sdata.tool.api.ServiceDefinition#getResponseMap()
 	 */
 	public Map<String, Object> getResponseMap() {
 
 		return map2;
 	}
 
 }
