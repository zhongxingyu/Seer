 package edu.nus.tp.engine.saver;
 
 import static edu.nus.tp.engine.utils.Constants.*;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.atomic.AtomicLong;
 
 import redis.clients.jedis.Jedis;
 import redis.clients.jedis.Tuple;
 import edu.nus.tp.engine.utils.Category;
 
 public class RedisPersistence implements Persistence {
 
 	private ConcurrentHashMap <String, AtomicLong> positiveTermMap = new ConcurrentHashMap <String, AtomicLong>();
 	private ConcurrentHashMap <String, AtomicLong> negativeTermMap = new ConcurrentHashMap <String, AtomicLong>();
 	private ConcurrentHashMap <String, AtomicLong> neutralTermMap = new ConcurrentHashMap <String, AtomicLong>();
 	
 	private Jedis jedis=null;
 	
 	public RedisPersistence() {
 		
 		jedis=new Jedis(REDIS_HOST, REDIS_PORT);
		jedis.auth(PASSWORD);
 		jedis.connect();
 	    System.out.println("Connected");
 	}
 	
 	@Override
 	public void saveTermsAndClassification(Collection<String> eachParsedTweet,
 			Category category) {
 
 		//for calculation of priors
 		incrementTweetCountFor(category);
 		
 		for (String eachTerm : eachParsedTweet) {
 		
 			incrementTermCountFor(category);
 			
 			switch (category) {
 				case POSITIVE:
 					//TODO need to check if this is expensive
 					positiveTermMap.putIfAbsent(eachTerm, new AtomicLong(0)); //init value set at 1 for Laplace smoothing
 					positiveTermMap.get(eachTerm).incrementAndGet();
 					break;
 				
 				case NEGATIVE:
 					negativeTermMap.putIfAbsent(eachTerm, new AtomicLong(0));
 					negativeTermMap.get(eachTerm).incrementAndGet();
 					break;
 					
 				case NEUTRAL:
 					neutralTermMap.putIfAbsent(eachTerm, new AtomicLong(0));
 					neutralTermMap.get(eachTerm).incrementAndGet();
 					break;
 					
 				default:
 					break;
 				}
 			
 		}
 		
 		System.out.println("Done");
 	
 	}
 	
 
 	public long getUniqueTermsInVocabulary(){
 		
 		Set<String> uniqueTermsInVocabulary=new HashSet<String>();
 		uniqueTermsInVocabulary.addAll(positiveTermMap.keySet());
 		uniqueTermsInVocabulary.addAll(negativeTermMap.keySet());
 		uniqueTermsInVocabulary.addAll(neutralTermMap.keySet());
 		
 		return uniqueTermsInVocabulary.size();
 		
 	}
 	
 	
 	
 	/**
 	 * Gets prior for a category
 	 * @param category
 	 * @return
 	 */
 	public double getPriorClassificationForCategory(Category category){
 		
 		long totalInstances=0,thisCategoryCount=0;
 		
 		Set<Tuple> tweetCountByCategory = jedis.zrangeWithScores(TWEET_COUNT_BY_CATEGORY, 0, -1);
 		
 		for (Tuple eachCategory : tweetCountByCategory) {
 			totalInstances+=eachCategory.getScore();
 		}
 	
 		Double thisCategoryCountDbl=jedis.zscore(TWEET_COUNT_BY_CATEGORY, category.toString());
 		
 		thisCategoryCount=thisCategoryCountDbl==null?1:thisCategoryCountDbl.longValue();
 		
 		return (double)thisCategoryCount/totalInstances;
 		
 	}
 	
 	
 	public long getTotalUniquePositiveTerms(){
 		return positiveTermMap.size();
 	}
 	
 	public long getTotalUniqueNegativeTerms(){
 		return negativeTermMap.size();
 	}
 	
 	public long getTotalUniqueNeutralTerms(){
 		return neutralTermMap.size();
 	}
 
 	
 	public long getTermFrequencyInCategory(String term, Category category){
 		
 		switch (category) {
 		
 		case POSITIVE: return positiveTermMap.get(term)==null?1:positiveTermMap.get(term).longValue();
 			
 		case NEGATIVE:  return negativeTermMap.get(term)==null?1:negativeTermMap.get(term).longValue();
 			
 		case NEUTRAL:  return neutralTermMap.get(term)==null?1:neutralTermMap.get(term).longValue();
 			
 		default: return 0;
 		}
 		
 	}
 
 	public long getTermCountByCategory(Category category) {
 		Double thisCategoryCountDbl=jedis.zscore(TERM_COUNT_BY_CATEGORY, category.toString());
 		return thisCategoryCountDbl==null?1:thisCategoryCountDbl.longValue();
 		
 	}
 	
 	
 	private void incrementTermCountFor(Category category) {
 		jedis.zincrby(TERM_COUNT_BY_CATEGORY, 1, category.toString());
 	}
 
 
 	private void incrementTweetCountFor(Category category) {
 		jedis.zincrby(TWEET_COUNT_BY_CATEGORY, 1, category.toString());
 		
 	}
 	
 	//No lifecycle method here 
 	//FIXME Didn't disconnect any connections here 
 
 }
