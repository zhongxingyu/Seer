 package com.badlogic.ldstats.service;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.logging.Logger;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 
 import org.codehaus.jackson.map.ObjectMapper;
 
 import com.badlogic.ldstats.crawler.LdCrawler;
 import com.badlogic.ldstats.crawler.LdEntry;
 import com.sun.jersey.spi.resource.Singleton;
 
 @Path("/")
 @Singleton
 public class LdStatsService {
 	private static final Logger log = Logger.getLogger(LdStatsService.class.getSimpleName());
 	List<LdEntry> entries = new ArrayList<LdEntry>();
 	volatile boolean crawling = false;
 	
 	public LdStatsService() {
 		InputStream in = getClass().getResourceAsStream("/bootstrap.json");
 		if(in != null) {
 			try {
 				LdEntry[] readEntries = new ObjectMapper().readValue(in, LdEntry[].class);
 				for(LdEntry entry: readEntries) {
 					entries.add(entry);
 				}
 				log.info("loaded entries from bootstrap file");
 			} catch (Exception e) {
 				log.info("Couldn't read bootstrap file");
 			}
 		}
//		TimerTask task = new TimerTask() {
//			@Override
//			public void run() {
//				crawl();
//			}
//		};
//		new Timer().scheduleAtFixedRate(task, 0, 1000 * 3600 * 24);
 	}
 	
 	@GET
	@Produces(MediaType.APPLICATION_JSON)
 	@Path("crawl")
	public boolean crawl() {
		if(crawling) return true;
 		crawling = true;
 		new Thread(new Runnable() {
 			public void run() {
 				try {
 					log.info("crawling LD");
 					List<LdEntry> crawlEntries = new LdCrawler().crawlEntries("http://www.ludumdare.com/compo/ludum-dare-26/");
 					synchronized (entries) {
 						entries = crawlEntries;
 					}
 					crawling = false;
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}).start();
		return true;
 	}
 	
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("isCrawling")
 	public boolean isCrawling() {
 		return crawling;
 	}
 	
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("byComments")
 	public List<LdEntry> byComments(@QueryParam("max") int maxEntries) {
 		List<LdEntry> sortedEntries = new ArrayList<LdEntry>();
 		synchronized(entries) {
 			sortedEntries.addAll(entries);
 		}
 		Collections.sort(entries, new Comparator<LdEntry>() {
 			public int compare(LdEntry o1, LdEntry o2) {
 				return o1.comments.size() - o2.comments.size();
 			}
 		});
 		if(maxEntries == 0) maxEntries = 20;
 		maxEntries = Math.max(0, Math.min(maxEntries, sortedEntries.size()));
 		List<LdEntry> result = new ArrayList<LdEntry>();
 		for(int i = 0; i < maxEntries; i++) {
 			result.add(sortedEntries.get(i));
 		}
 		return result;
 	}
 	
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("linkTypes")
 	public List<String> linkTypes(@QueryParam("q") String query) {
 		Set<String> linkTypes = new HashSet<String>();
 		String[] keywords = query != null? query.split(","): new String[0];
 		synchronized (entries) {
 			if(query == null) {
 				for(LdEntry entry: entries) {
 					linkTypes.addAll(entry.links.keySet());
 				}
 			} else {
 				for(LdEntry entry: entries) {
 					for(String linkType: entry.links.keySet()) {
 						if(match(keywords, linkType)) {
 							linkTypes.add(linkType);
 						}
 					}
 				}
 			}
 		}
 		List<String> sortedTypes = new ArrayList<String>();
 		sortedTypes.addAll(linkTypes);
 		Collections.sort(sortedTypes);
 		return sortedTypes;
 	}
 	
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("query")
 	public List<LdEntry> query(@QueryParam("qlink") String linkQuery, 
 							   @QueryParam("quser") String userQuery, 
 							   @QueryParam("qtext") String textQuery,
 							   @QueryParam("mincomments") int minComments) {
		String[] linkKeywords = linkQuery != null & linkQuery.length() != 0? linkQuery.split(","): new String[0];
		String[] userKeywords = userQuery != null & userQuery.length() != 0? userQuery.split(","): new String[0];
		String[] textKeywords = textQuery != null & textQuery.length() != 0? textQuery.split(","): new String[0];
 		
 		List<LdEntry> results = new ArrayList<LdEntry>();
 		synchronized (entries) {
 			for(LdEntry entry: entries) {
				if(match(linkKeywords, entry.links.keySet()) &&
 				   match(userKeywords, entry.user) &&
 				   match(textKeywords, entry.text)){
 					   results.add(entry);
 				   }
 			}
 		}
 		return results;
 	}
 	
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("count")
 	public int count(@QueryParam("qlink") String linkQuery, 
 							   @QueryParam("quser") String userQuery, 
 							   @QueryParam("qtext") String textQuery,
 							   @QueryParam("mincomments") int minComments) {
 		return query(linkQuery, userQuery, textQuery, minComments).size();
 	}
 
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("raw")
 	public List<LdEntry> raw() {
 		synchronized(entries) {
 			return entries;
 		}
 	}
 	
 	private boolean match(String[] keywords, String text) {
 		if(keywords.length == 0) return true;
 		String lowerText = text.toLowerCase();
 		for(String keyword: keywords) {
			if(keyword.length() == 0) continue;
 			if(lowerText.contains(keyword.toLowerCase())) return true;
 		}
 		return false;
 	}
 	
 	private boolean match(String[] keywords, Collection<String> values) {
 		for(String value: values) {
 			if(match(keywords, value)) return true;
 		}
 		return false;
 	}
 }
