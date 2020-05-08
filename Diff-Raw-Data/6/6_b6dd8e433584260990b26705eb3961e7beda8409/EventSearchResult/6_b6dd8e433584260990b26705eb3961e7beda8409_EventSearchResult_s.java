 package com.celements.calendar.search;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.celements.calendar.Event;
 import com.celements.calendar.IEvent;
 import com.celements.web.service.IWebUtilsService;
 import com.xpn.xwiki.XWikiContext;
 import com.xpn.xwiki.plugin.lucene.LucenePlugin;
 import com.xpn.xwiki.plugin.lucene.SearchResult;
 import com.xpn.xwiki.plugin.lucene.SearchResults;
 import com.xpn.xwiki.web.Utils;
 
 public class EventSearchResult {
 
   private IWebUtilsService webUtils;
 
   private static Log LOGGER = LogFactory.getFactory().getInstance(
       EventSearchResult.class);
 
   private SearchResults searchResultsCache;
   private LucenePlugin lucenePlugin;
 
   private final String luceneQuery;
   private final List<String> sortFields;
   private final boolean skipChecks;
   private final XWikiContext context;
 
   EventSearchResult(String luceneQuery, List<String> sortFields, boolean skipChecks, 
       XWikiContext context) {
     this.luceneQuery = luceneQuery;
     this.sortFields = (sortFields != null ? Collections.unmodifiableList(
         new ArrayList<String>(sortFields)) : null);
     this.skipChecks = skipChecks;
     this.context = context;
   }
 
   public String getLuceneQuery() {
     return luceneQuery;
   }
 
   public List<String> getSortFields() {
     return sortFields;
   }
   
   boolean getSkipChecks() {
     return skipChecks;
   }
 
   /**
    * 
    * @return all events
    */
   public List<IEvent> getEventList() {
     return getEventList(0, 0);
   }
 
   /**
    * 
    * @param offset from 0 to (size - 1)
    * @param limit all remaining events are returned for values < 0 or >= (size - 1)
    * @return selected events
    */
   public List<IEvent> getEventList(int offset, int limit) {
     SearchResults results = luceneSearch();
     if (results != null) {
       List<SearchResult> list;
       if (limit > 0) {
         list = results.getResults(offset + 1, limit);
       } else {
         list = results.getResults(offset + 1, results.getHitcount());
       }
       List<IEvent> eventList = convertToEventList(list);
       if (eventList == null) {
         LOGGER.error("prevent returning null in getEventList of EvemtSearchResult for"
             + " offset [" + offset + "], limit [" + limit + "], list [" + list
             + "], luceneQuery [" + luceneQuery + "].");
         eventList = Collections.emptyList();
       }
       return eventList;
     } else {
       LOGGER.warn("getEventList: luceneSearch returned 'null' value for offset ["
           + offset + "] and limit [" + limit + "] on luceneQuery [" + luceneQuery +"].");
       return null;
     }
   }
 
   private List<IEvent> convertToEventList(List<SearchResult> results) {
     List<IEvent> eventList = new ArrayList<IEvent>();
     for (SearchResult result : results) {
       eventList.add(new Event(getWebUtils().resolveDocumentReference(
           result.getFullName())));
     }
     return eventList;
   }
 
   public int getSize() {
     int hitcount = luceneSearch().getHitcount();
     LOGGER.debug("EventSearchResult got hitcount [" + hitcount + "] for query ["
         + luceneQuery + "].");
     return hitcount;
   }
 
   SearchResults luceneSearch() {
     if (searchResultsCache == null) {
       try {
         String[] sortFieldsArray = (sortFields != null ? sortFields.toArray(
             new String[sortFields.size()]) : null);
         if (skipChecks) {
           searchResultsCache = getLucenePlugin().getSearchResultsWithoutChecks(
              luceneQuery, sortFieldsArray, null, "default,de", context);
         } else {
           searchResultsCache = getLucenePlugin().getSearchResults(luceneQuery, 
              sortFieldsArray, null, "default,de", context);
         }
         LOGGER.trace("luceneSearch: created new searchResults for query [" + luceneQuery
             + "].");
       } catch (Exception exception) {
         LOGGER.error("Unable to get lucene search results for query '" + luceneQuery
             + "'", exception);
       }
     } else {
       LOGGER.trace("luceneSearch: returning cached searchResults for query ["
           + luceneQuery + "].");
     }
     return searchResultsCache;
   }
 
   private LucenePlugin getLucenePlugin() {
     if (lucenePlugin == null) {
       lucenePlugin = (LucenePlugin) context.getWiki().getPlugin("lucene", context);
     }
     return lucenePlugin;
   }
 
   private IWebUtilsService getWebUtils() {
     if(webUtils == null){
       webUtils = Utils.getComponent(IWebUtilsService.class);
     }
     return webUtils;
   }
 
   @Override
   public String toString() {
     return "EventSearchResult [luceneQuery=" + luceneQuery + ", sortFields=" + sortFields 
         + "]";
   }
 
   void injectLucenePlugin(LucenePlugin lucenePlugin) {
     this.lucenePlugin = lucenePlugin;
   }
 
 }
