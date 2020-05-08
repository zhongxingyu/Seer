 /*
  * JBoss, Home of Professional Open Source
  * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
  * as indicated by the @authors tag. All rights reserved.
  */
 package org.jboss.planet.service;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.ejb.Stateless;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import org.jboss.planet.exception.ParserException;
 import org.jboss.planet.exception.ParserException.CAUSE_TYPE;
 import org.jboss.planet.model.Category;
 import org.jboss.planet.model.Configuration;
 import org.jboss.planet.model.Post;
 import org.jboss.planet.model.PostStatus;
 import org.jboss.planet.model.RemoteFeed;
 import org.jboss.planet.util.StringTools;
 
 import com.sun.syndication.feed.synd.SyndCategory;
 import com.sun.syndication.feed.synd.SyndContent;
 import com.sun.syndication.feed.synd.SyndEntry;
 import com.sun.syndication.feed.synd.SyndFeed;
 import com.sun.syndication.io.FeedException;
 import com.sun.syndication.io.SyndFeedInput;
 import com.sun.syndication.io.XmlReader;
 import com.sun.syndication.io.impl.Base64;
 
 /**
  * Service related to parsing feeds
  * 
  * @author Libor Krzyzanek
  * @author Adam Warski (adam at warski dot org)
  */
 @Named
 @Stateless
 public class ParserService {
 
 	@Inject
 	private CategoryService categoryService;
 
 	@Inject
 	private ConfigurationService configurationManager;
 
	public static final String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.17 (KHTML, like Gecko) Ubuntu Chromium/24.0.1312.56 Chrome/24.0.1312.56 Safari/537.17";

 	/**
 	 * Parse feed from defined address
 	 * 
 	 * @param link
 	 * @param username
 	 *            username if basic authentication is needed
 	 * @param password
 	 *            password for basic authentication
 	 * @return
 	 * @throws ParserException
 	 */
 	public RemoteFeed parse(String link, String username, String password) throws ParserException {
 		try {
 
 			Configuration conf = configurationManager.getConfiguration();
 
 			URLConnection conn = new URL(link).openConnection();
 			conn.setReadTimeout(conf.getReadTimeout());
 			conn.setConnectTimeout(conf.getConnectionTimeout());
			conn.setRequestProperty("User-Agent", USER_AGENT);
 
 			if (username != null && password != null) {
 				String encoding = Base64.encode(username + ":" + password);
 				conn.setRequestProperty("Authorization", "Basic " + encoding);
 			}
 
 			conn.connect();
 			return parse(conn.getInputStream());
 		} catch (MalformedURLException e) {
 			throw new ParserException(e, CAUSE_TYPE.CONNECTION_PROBLEM);
 		} catch (IOException e) {
 			throw new ParserException(e, CAUSE_TYPE.CONNECTION_PROBLEM);
 		}
 	}
 
 	public SyndFeed getRemoteFeed(String link) throws IllegalArgumentException, FeedException, IOException {
 		Configuration conf = configurationManager.getConfiguration();
 
 		URLConnection conn = new URL(link).openConnection();
 		conn.setReadTimeout(conf.getReadTimeout());
 		conn.setConnectTimeout(conf.getConnectionTimeout());
 		conn.connect();
 
 		InputStream is = conn.getInputStream();
 
 		SyndFeedInput input = new SyndFeedInput();
 		SyndFeed syndFeed = input.build(new XmlReader(is));
 
 		is.close();
 
 		return syndFeed;
 	}
 
 	public RemoteFeed parse(InputStream is) throws ParserException {
 
 		SyndFeed syndFeed = null;
 		try {
 			SyndFeedInput input = new SyndFeedInput();
 			syndFeed = input.build(new XmlReader(is));
 		} catch (FeedException e) {
 			throw new ParserException(e, CAUSE_TYPE.PARSING_EXCEPTION);
 		} catch (IOException e) {
 			throw new ParserException(e, CAUSE_TYPE.CONNECTION_PROBLEM);
 		}
 
 		RemoteFeed feed = new RemoteFeed();
 
 		feed.setAuthor(syndFeed.getAuthor());
 		feed.setDescription(syndFeed.getDescription());
 		feed.setLink(syndFeed.getLink());
 		feed.setTitle(syndFeed.getTitle());
 
 		List<Post> posts = new ArrayList<Post>();
 		feed.setPosts(posts);
 
 		if (syndFeed.getEntries() != null) {
 			for (Object entryObj : syndFeed.getEntries()) {
 				SyndEntry entry = (SyndEntry) entryObj;
 				Post post = new Post();
 
 				post.setFeed(feed);
 
 				post.setAuthor(StringTools.isEmpty(entry.getAuthor()) ? null : entry.getAuthor());
 
 				// Setting content
 				String longestContent = entry.getDescription() == null ? "" : entry.getDescription().getValue();
 
 				for (Object contentObj : entry.getContents()) {
 					SyndContent content = (SyndContent) contentObj;
 					String currentContent = content == null ? "" : content.getValue();
 
 					if (currentContent.length() > longestContent.length()) {
 						longestContent = currentContent;
 					}
 				}
 
 				post.setContent(longestContent);
 
 				// Setting categories
 				post.setCategories(new ArrayList<Category>());
 				for (Object categoryObj : entry.getCategories()) {
 					SyndCategory category = (SyndCategory) categoryObj;
 					post.getCategories().add(categoryService.getCategory(category.getName()));
 				}
 
 				// Setting the published date
 				Date publishedDate = entry.getPublishedDate();
 				if (publishedDate == null) {
 					publishedDate = entry.getUpdatedDate();
 				}
 
 				post.setPublished(publishedDate);
 
 				// And other properties
 				post.setTitle(entry.getTitle());
 				post.setModified(entry.getUpdatedDate() == null ? post.getPublished() : entry.getUpdatedDate());
 				post.setLink(entry.getLink());
 
 				post.setStatus(PostStatus.CREATED);
 
 				posts.add(post);
 			}
 		}
 
 		// TODO Check if sort is needed
 		// Collections.sort(posts);
 		try {
 			is.close();
 			return feed;
 		} catch (IOException e) {
 			throw new ParserException(e, CAUSE_TYPE.CONNECTION_PROBLEM);
 		}
 	}
 
 }
