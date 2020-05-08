 package wikipedia.analysis.drilldown;
 
 import java.util.List;
 
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.joda.time.DateTime;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import util.HTTPUtil;
 import wikipedia.database.DBUtil;
 import wikipedia.http.WikiAPIClient;
 import wikipedia.xml.Api;
 import wikipedia.xml.Page;
 import wikipedia.xml.Rev;
 import wikipedia.xml.XMLTransformer;
 
 public final class NumberOfRecentEditsFetcher {
 
     private final String lang;
     private final WikiAPIClient apiClient = new WikiAPIClient(new DefaultHttpClient());
 
     private static final Logger LOG = LoggerFactory.getLogger(NumberOfRecentEditsFetcher.class.getName());
 
     public NumberOfRecentEditsFetcher(final String lang) {
         this.lang = lang;
     }
 
     public int getNumberOfEditsInLastWeeks(final int numberOfWeeks,
                                            final String pageTitle) {
        try {
            final List<Rev> revisions = downloadPages(numberOfWeeks, pageTitle).getRevisions();
             return revisions.size();
        } catch (Exception e) {
            return 0;
         }
     }
 
     private Page downloadPages(final int numberOfWeeks,
                                         final String pageTitle) {
         final DateTime now = new DateTime();
         final String dateStringNow = now.toString(DBUtil.MYSQL_DATETIME_FORMATTER);
         final String dateStringBackThen = now.minusWeeks(numberOfWeeks).toString(DBUtil.MYSQL_DATETIME_FORMATTER);
 
         final String requestURL = getURL(dateStringBackThen, dateStringNow, pageTitle);
         LOG.info("Requesting: " + requestURL);
         final String httpResultText = apiClient.executeHTTPRequest(requestURL);
         final Api revisionFromXML = XMLTransformer.getRevisionFromXML(httpResultText);
         return revisionFromXML.getQuery().getPages().get(0);
     }
 
     private String getURL(final String startDate,
                           final String endDate,
                           final String pageTitle) {
         final String encodedPageName = HTTPUtil.urlEncode(pageTitle);
         return "http://" + lang + ".wikipedia.org/w/api.php?format=xml&action=query&prop=revisions&titles="
                 + encodedPageName + "&rvlimit=500&rvprop=&rvdir=newer" + "&rvstart=" + HTTPUtil.urlEncode(startDate)
                 + "&rvend=" + HTTPUtil.urlEncode(endDate);
     }
 
     public int getPageID(final String pageTitle) {
         return downloadPages(1, pageTitle).getPageid();
     }
 
 }
