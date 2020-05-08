 package net.newfoo.logs.analytics.client;
 /*
  * Copyright (c) 2009 Jim Connell
  *
  * Permission is hereby granted, free of charge, to any person
  * obtaining a copy of this software and associated documentation
  * files (the "Software"), to deal in the Software without
  * restriction, including without limitation the rights to use,
  * copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the
  * Software is furnished to do so, subject to the following
  * conditions:
  *
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
  * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
  * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
  * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
  * OTHER DEALINGS IN THE SOFTWARE.
  */
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.google.gdata.client.analytics.AnalyticsService;
 import com.google.gdata.client.analytics.DataQuery;
 import com.google.gdata.data.analytics.AccountEntry;
 import com.google.gdata.data.analytics.AccountFeed;
 import com.google.gdata.data.analytics.DataEntry;
 import com.google.gdata.data.analytics.DataFeed;
 import com.google.gdata.util.AuthenticationException;
 import com.google.gdata.util.ServiceException;
 import com.google.gdata.util.common.base.Pair;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import org.apache.log4j.Logger;
 
 /**
  * Sample program demonstrating how to make a data request to the GA Data Export 
  * using client login authentication as well as accessing important data in the feed.
  */
 public class GdataAnalyticsClient implements AnalyticsClient {
     private AnalyticsService as = new AnalyticsService("gaExportAPI_acctSample_v1.0");
     private static final String BASE_URL = "https://www.google.com/analytics/feeds/data";
     private static final String DEFAULT_FEEDS = "https://www.google.com/analytics/feeds/accounts/default";
 
     private String profileId;
 
     private static final Logger LOG = Logger.getLogger(GdataAnalyticsClient.class.getName());
 
     private DateFormat formatter = new SimpleDateFormat("yyyyMMdd");
 
     public void login(String user, String pass) {
         try {
             as.setUserCredentials(user, pass);
         } catch (AuthenticationException e) {
             exception("Could not login", e);
         }
     }
 
 
     public List<Site> getSites() {
         List<Site> sites = new ArrayList<Site>();
         try {
             AccountFeed accountFeed = as.getFeed(new URL(DEFAULT_FEEDS), AccountFeed.class);
             for (AccountEntry ae : accountFeed.getEntries()) {
 /*              System.out.println(ae.getTitle().getPlainText());
               System.out.println(ae.getTableId()); */
               sites.add(new Site(ae.getTitle().getPlainText(), ae.getProperty("ga:profileId")));
             }
         } catch (MalformedURLException mue) {
             exception("Unexpected error", mue);
         } catch (ServiceException se) {
             exception("Error communicating with Google", se);
         } catch (IOException ioe) {
             exception("Error retrieving data", ioe);
         }
         return sites;
     }
 
     public void setProfileId(String profileId) {
         this.profileId = profileId;
     }
 
     public List<TimeEntry> hits(String start, String end) {
         List<TimeEntry> hits = new ArrayList<TimeEntry>();
         for (DataEntry entry : query(start, end, "ga:date", "ga:visits,ga:bounces,ga:newVisits", "ga:date")) {
             final String d = entry.stringValueOf("ga:date");
             try {
                 hits.add(new TimeEntry(formatter.parse(d), entry.longValueOf("ga:visits"), entry.longValueOf("ga:newVisits"), entry.longValueOf("ga:bounces")));
             } catch (ParseException pe) {
                 LOG.warn("Could not parse date: " + d, pe);
             }
         }
         return hits;
     }
 
    public List<TimeEntry> hits(String date) {
        List<TimeEntry> hits = new ArrayList<TimeEntry>();
        for (DataEntry entry : query(date, null, "ga:hour", "ga:visits,ga:bounces,ga:newVisits", "ga:hour")) {
            hits.add(new TimeEntry(Integer.parseInt(entry.stringValueOf("ga:hour")), entry.longValueOf("ga:visits"), entry.longValueOf("ga:newVisits"), entry.longValueOf("ga:bounces")));
        }
        return hits;
    }

     public List<Pair<String, Long>> topPages(String start, String end) {
         List<DataEntry> entries = query(start, end, "ga:pageTitle", "ga:pageviews", "-ga:pageviews");
         List<Pair<String, Long>> items = new ArrayList<Pair<String, Long>>();
         for (DataEntry entry : entries) {
             items.add(new Pair<String, Long>(entry.stringValueOf("ga:pageTitle"), entry.longValueOf("ga:pageviews")));
         }
         return items;
     }
 
     public List<Pair<String, Long>> topReferrers(String start, String end) {
         List<Pair<String, Long>> items = new ArrayList<Pair<String, Long>>();
         for (DataEntry entry : query(start, end, "ga:source", "ga:pageviews", "-ga:pageviews")) {
             items.add(new Pair<String, Long>(entry.stringValueOf("ga:source"), entry.longValueOf("ga:pageviews")));
         }
         return items;
     }
 
 
     private List<DataEntry> query(String start, String end, String dimensions, String metrics, String sortOrder) {
         DataQuery query;
         //------------------------------------------------------
         // GA Data Feed
         //------------------------------------------------------
         // first build the query
         try {
             query = new DataQuery(new URL(BASE_URL));
         } catch (MalformedURLException e) {
             System.err.println("Malformed URL: " + BASE_URL);
             return null;
         }
         query.setIds("ga:" + profileId);
         query.setDimensions(dimensions);
         query.setMetrics(metrics);
         query.setSort(sortOrder);
         query.setMaxResults(100);
         query.setStartDate(start);
        if (end != null) {
            query.setEndDate(end);
        } else {
            query.setEndDate(start);
        }
         URL url = query.getUrl();
         System.out.println("URL: " + url.toString());
 
         // Send our request to the Analytics API and wait for the results to come back
         DataFeed feed;
         try {
             feed = as.getFeed(url, DataFeed.class);
         } catch (IOException e) {
             System.err.println("Network error trying to retrieve feed: " + e.getMessage());
             return null;
         } catch (ServiceException e) {
             System.err.println("Analytics API responded with an error message: " + e.getMessage());
             return null;
         }
 
         return feed.getEntries();
     }
 
     private void exception(String message, Throwable root) {
         if (root != null) {
             LOG.error(message, root);
         } else {
             LOG.error(message);
         }
         throw new AnalyticsException(message);
     }
 }
