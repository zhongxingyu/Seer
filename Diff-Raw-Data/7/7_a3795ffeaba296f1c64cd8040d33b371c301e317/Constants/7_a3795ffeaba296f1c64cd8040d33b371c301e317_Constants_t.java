 package edu.nus.tp.engine.utils;
 
 import java.util.Set;
 
 import com.google.common.collect.Sets;
 
 public class Constants {
 	
 	public static final String EMPTY_STRING="";
 	public static final String SPACE=" ";
 	
 	public static final String TWEET_COUNT_BY_CATEGORY="TWEET_COUNT_BY_CATEGORY";
 	public static final String TERM_COUNT_BY_CATEGORY="TERM_COUNT_BY_CATEGORY";
 	
 	public static final String REDIS_HOST = "50.30.35.9";
 	public static final int REDIS_PORT = 2871;
	public static final String PASSWORD = "5cb88a1bcb1c2baae9335c4f964caf9d";
 	
 	public static final Set<String> STOP_WORDS=Sets.newHashSet("I","a","about","an","are","as","at","be","by","com","for","from","how","in","is","it","of","on","or","that","the","this","to","was","what","when","where","who","will","with","the","www");
 	
 	
 /**STOP WORDS FROM GOOGLE HISTORY **/
 /**********************************
 http://www.ranks.nl/resources/stopwords.html
 
 I 
 a 
 about 
 an 
 are 
 as 
 at 
 be 
 by 
 com 
 for 
 from
 how
 in 
 is 
 it 
 of 
 on 
 or 
 that
 the 
 this
 to 
 was 
 what 
 when
 where
 who 
 will 
 with
 the
 www
 
 */
 	
 }
