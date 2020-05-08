 package minerva;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Reads minerva webpage to get the most recent data. Development purpose only.
  * 
  * @return
  */
 public class MinervaSuggestions {
 
	private static Map<String, String> sessionMap = new HashMap<String,String>();
	private static Map<String, String> subjectsMap = new HashMap<String,String>();
	private static Map<String, String> facultiesMap = new HashMap<String,String>();
 
 	public MinervaSuggestions() throws Exception {
 		/**
 		 * Should connect to:
 		 * https://horizon.mcgill.ca/pban1/bwckgens.p_proc_term_date
 		 * https://horizon.mcgill.ca/pban1/bwskfcls.p_sel_crse_search
 		 * 
 		 * Use jsoup for HTML parsing: https://github.com/jhy/jsoup.
 		 * 
 		 * To get results on: https://horizon.mcgill.ca/pban1/bwskfcls.P_GetCrse
 		 * 
 		 * if no results are found return NoInternetConnection
 		 */
 
 	}
 
 	/**
 	 * Matches "201305" to "Summer 2013" for example.
 	 * 
 	 * @return
 	 */
 	@Deprecated
 	public static Map<String, String> getSessions() {
 
 		return sessionMap;
 	}
 
 	/**
 	 * Matches "ACCT" to "ACCT - Accounting" for example.
 	 * 
 	 * @return
 	 */
 	@Deprecated
 	public static Map<String, String> getSubjects() {
 
 		return subjectsMap;
 	}
 
 	/**
 	 * Matches "MG" to "Desautels Faculty Management"
 	 * 
 	 * @return
 	 */
 	@Deprecated
 	public static Map<String, String> getFaculties() {
 
 		return facultiesMap;
 	}
 }
