 package com.marklogic.training.helper;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class AdvancedHelper {
 	
 	private static final Logger logger = LoggerFactory.getLogger(AdvancedHelper.class);
 
 	public static String buildQueryString(String keywords, String type, String exclude, String genre, String creator, String songtitle) {
 		
 		String[] tokenArr = keywords.split(" ");
 		List<String> tokens = new ArrayList<String>(Arrays.asList(tokenArr));
 		StringBuilder query = new StringBuilder();
 		if (type.equals("any")) {
 			logger.debug(" type is " + type);
 			for (Iterator<String> i = tokens.iterator(); i.hasNext(); ) {
 				query.append(i.next());
 				if (i.hasNext())
 					query.append(" OR ");
 			}
 		} else if (type.equals("phrase")) {
 			query.append("\"");
 			for (Iterator<String> i = tokens.iterator(); i.hasNext(); ) {
 				query.append(i.next());
 				if (i.hasNext())
 					query.append(" ");
 			}
 			query.append("\"");			
 			
 		} else {
 			query.append(keywords);
 		}
 		if (exclude != "") {		
 			String[] excludeArr = exclude.split(" ");
 			List<String> excludes = new ArrayList<String>(Arrays.asList(excludeArr));
 			query.append(" ");
 			for (Iterator<String> i = excludes.iterator(); i.hasNext(); ) {
 				query.append("-");
 				query.append(i.next());
 				query.append(" ");
 			}		
 		}
 		
		if (genre != "") {	
 			String[] genreArr = genre.split(" ");
 			query.append(" genre:");
 			logger.debug("genre array contains elements "+genreArr.length);
 			if (genreArr.length == 1 ) {
 				query.append(genre);			
 			} else {
 				List<String> genres = new ArrayList<String>(Arrays.asList(genreArr));
 				query.append("\"");
 				for (Iterator<String> i = genres.iterator(); i.hasNext(); ) {
 					query.append(i.next());
 					if (i.hasNext())
 						query.append(" ");
 				}
 				query.append("\"");				
 			}
 			
 		}
 		if (creator != "") {		
 			String[] creatorArr = creator.split(" ");
 			query.append(" creator:");
 			logger.debug("creator array contains elements "+creatorArr.length);
 			if (creatorArr.length == 1 ) {
 				query.append(creator);			
 			} else {
 				List<String> creators = new ArrayList<String>(Arrays.asList(creatorArr));
 				query.append("\"");
 				for (Iterator<String> i = creators.iterator(); i.hasNext(); ) {
 					query.append(i.next());
 					if (i.hasNext())
 						query.append(" ");
 				}
 				query.append("\"");				
 			}
 			
 		}
 		if (songtitle != ""){
 			query.append(" title:\""+songtitle+"\"");
 			query.append(" ");
 		}
 		
 		return query.toString();
 	}
 }
