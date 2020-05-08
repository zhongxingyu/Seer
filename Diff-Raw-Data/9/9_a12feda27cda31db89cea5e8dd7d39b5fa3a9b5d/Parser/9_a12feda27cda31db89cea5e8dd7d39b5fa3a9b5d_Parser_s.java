 package com.inc.im.serptracker.util;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import android.app.Activity;
 import android.util.Log;
 
 import com.flurry.android.FlurryAgent;
 import com.inc.im.serptracker.R;
 import com.inc.im.serptracker.data.Keyword;
 import com.inc.im.serptracker.data.access.Download;
 
 /**
  * Parses raw document into objects using Jsoup parsing engine.
  * 
  * @author indrek
  * 
  */
 
 public class Parser {
 
 	private final static int DCOUNT = 100;
 
 	/**
 	 * Gets h3 > a from Document and removes not valid urls
 	 * 
 	 * @param keyword
 	 *            - if parse fails save newrank -2
 	 * @param doc
 	 *            - find h3 > a in here
 	 * @return Elements of results
 	 */
 	public static Elements parse(Activity a, Keyword keyword) {
 
 		Document doc = Download.H3FirstA(a, keyword);
 
 		// @added ver 1.3 - exception fix
 		if (doc == null)
 			return null;
 
 		Elements allResults = doc.select(a
 				.getString(R.string.googleResultParseRule));
 
 		if (allResults == null || allResults.size() == 0) {
 			Log.e("MY", "downloaded allResults h3 first a is null");
 			keyword.newRank = -2;
 
 			return null;
 		}
 
 		removeNotValidUrls(allResults);
 
 		return allResults;
 	}
 
 	/**
 	 * @param allResults
 	 *            where to find
 	 * @param WEBSITE
 	 *            what to find
 	 */
 	public static Keyword getRanking(Keyword keyword, Elements allResults,
 			String WEBSITE) {
 
 		if (keyword == null || allResults == null)
 			return null;
 
 		int numOfResults = allResults.size();
 
 		// DEBUG
 		// for (int i = 0; i < numOfResults; i++) {
 		// Element singleResult = allResults.get(i);
 		// Log.d("MY", i + ". " + singleResult.attr("href"));
 		// }
 
 		Keyword result = new Keyword(keyword.keyword);
 		result.oldRank = keyword.oldRank;
 		result.id = keyword.id;
 
 		for (int i = 0; i < numOfResults; i++) {
 
 			Element singleResult = allResults.get(i);
 
 			if (result.newRank != 0)
 				return result;
 
 			if (singleResult != null) {
 
 				String singleResultUrl = singleResult.attr("href");
 
 				String singleResultUrlModified = removePrefix(singleResultUrl);
 				// if cointains url and is not set yet
 
				if (singleResultUrlModified.startsWith(WEBSITE+"/")){
						//&& !singleResultUrlModified.startsWith(WEBSITE + ".")) {
 
 					// singleResultUrlModified.contains("." + WEBSITE) -
 					// subdomain special case
 
 					if (numOfResults <= DCOUNT) {
 
 						result.newRank = i + 1;
 
 					} else {
 
 						// WE HAVE TO JUSTIFY RANK
 						// there is a authority site with sub links somewhere
 						// probably
 
 						int overTheNormal = numOfResults - DCOUNT;
 						int newRank = i + 1 - overTheNormal;
 
 						if (newRank <= 0)
 							newRank = 1;
 
 						result.newRank = newRank;
 					}
 
 					result.anchorText = singleResult.text();
 					result.url = singleResult.attr("href");
 
 				}
 
 			}
 
 			// flurry loging
 			if (numOfResults <= 95 || numOfResults >= 105) {
 
 				Map<String, Integer> data = new HashMap<String, Integer>();
 
 				data.put("result count", numOfResults);
 
 				FlurryAgent.logEvent("DEBUG: parse count after invalid delete",
 						data);
 			}
 
 		} // for links in keyword
 
 		// not ranked
 		if (result.newRank == 0)
 			result.newRank = -1;
 
 		return result;
 
 	}
 
 	/**
 	 * Removes advertisements links, meaning all local links meaning all links
 	 * starting with /
 	 * 
 	 * @param allResults
 	 */
 	public static void removeNotValidUrls(Elements allResults) {
 
 		// remove ad urls
 		for (int i = 0; i < allResults.size(); i++) {
 
 			String link = allResults.get(i).attr("href").trim();
 
 			Boolean isInvalid = link.startsWith("/");
 
 			if (isInvalid) {
 
 				// DEBUG
 				// Log.e("MY", "removed: " + allResults.get(i));
 
 				allResults.remove(i);
 				i--;
 
 			}
 
 		}
 		// loging - remove
 		if (allResults.size() != 100)
 			Log.w("MY",
 					"WARNING: results after internal link delete != 100, instead:"
 							+ allResults.size());
 	}
 
 	/**
 	 * Removes http, https and www. from the beginning
 	 * 
 	 * @param searchable
 	 * @return
 	 */
 	public static String removePrefix(String searchable) {
 
 		// String result = searchable.replace("http://", "").replace("www.",
 		// "");
 
 		String result = searchable.replace("https://", "").replace("http://",
 				"");
 
 		if (result.startsWith("www."))
 			result = result.replace("www.", "");
 		// result = singleResultUrlModified;
 
 		return result;
 	}
 
 }
