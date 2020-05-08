 package edu.gatech.oad.rocket.findmythings.server.util;
 
 import com.googlecode.objectify.Objectify;
 import com.googlecode.objectify.cmd.LoadType;
 import com.googlecode.objectify.cmd.Query;
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.analysis.TokenStream;
 import org.apache.lucene.analysis.en.EnglishAnalyzer;
 import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
 import org.apache.lucene.util.Version;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Logger;
 
 /**
  * Utility class for indexing and searching items conforming to the Searchable
  * interface.
  *
  * User: zw
  * Date: 4/8/13
  * Time: 2:35 AM
  */
 public class SearchableHelper {
 	private static final Logger log = Logger.getLogger(SearchableHelper.class.getName());
 
 	// Magic numbers!
 	private static final int MAXIMUM_NUMBER_OF_WORDS_TO_SEARCH = 10;
 
 	private static final int MAX_NUMBER_OF_WORDS_TO_PUT_IN_INDEX = 200;
 
	public static <T extends Searchable> Query<T> search(Objectify objectify, Class<T> clazz, String query) {
		Set<String> queryTokens = getSearchTokens(query, MAXIMUM_NUMBER_OF_WORDS_TO_SEARCH);
 		return objectify.load().type(clazz).filter("searchTokens in", queryTokens);
 	}
 
 	public static <T extends Searchable> Query<T> search(LoadType<T> query, String searchString) {
 		Set<String> queryTokens = getSearchTokens(searchString, MAXIMUM_NUMBER_OF_WORDS_TO_SEARCH);
 		return query.filter("searchTokens in", queryTokens);
 	}
 
 	public static void addSearchFilter(Map<String, Object> queryFilters, String searchString) {
 		Set<String> queryTokens = getSearchTokens(searchString, MAXIMUM_NUMBER_OF_WORDS_TO_SEARCH);
 		queryFilters.put("searchTokens in", queryTokens);
 	}
 
 	public static void updateSearchTokens(Searchable item) {
 		Set<String> ftsTokens = item.getSearchTokens();
 		ftsTokens.clear();
 		
 		if (!item.canGetSearchableContent()) {
 			return;
 		}
 		String sb = item.getSearchableContent();
 		Set<String> new_ftsTokens = getSearchTokens(sb, MAX_NUMBER_OF_WORDS_TO_PUT_IN_INDEX);
 
 		for (String token : new_ftsTokens) {
 			ftsTokens.add(token);
 		}
 	}
 
 	/**
 	 * Uses Apache Lucene English stemming for indexing similar words.
 	 *
 	 * @param searchableContext A string describing the object to be indexes
 	 * @param maximumNumberOfTokens The limit number of tokens to index
 	 * @return A set containing indexed, searchable tokens
 	 */
 	private static Set<String> getSearchTokens(String searchableContext, int maximumNumberOfTokens) {
 
 		String indexCleanedOfHTMLTags = searchableContext.replaceAll("<.*?>"," ");
 		Set<String> returnSet = new HashSet<>();
 
 		try (Analyzer analyzer = new EnglishAnalyzer(Version.LUCENE_42)) {
 			TokenStream tokenStream = analyzer.tokenStream(null, new StringReader(indexCleanedOfHTMLTags));
 			CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
 			tokenStream.reset();
 			while (tokenStream.incrementToken()) {
 				String term = charTermAttribute.toString();
 				if (term != null) {
 					returnSet.add(term);
 				}
 				if (returnSet.size() == maximumNumberOfTokens - 1) break;
 			}
 		} catch (IOException e) {
 			log.severe(e.getMessage());
 		}
 
 		return returnSet;
 	}
 
 
 
 }
