 package dk.statsbiblioteket.doms.gui;
 
 import dk.statsbiblioteket.doms.client.exceptions.ServerOperationFailed;
 import dk.statsbiblioteket.doms.guiclient.SearchResult;
 import dk.statsbiblioteket.doms.guiclient.SearchResultList;
 import dk.statsbiblioteket.doms.util.Util;
 import org.jboss.seam.Component;
 import org.jboss.seam.ScopeType;
 import org.jboss.seam.annotations.*;
 import org.jboss.seam.annotations.datamodel.DataModel;
 import org.jboss.seam.annotations.datamodel.DataModelSelection;
 import org.jboss.seam.annotations.web.RequestParameter;
 import org.jboss.seam.log.Log;
 import org.jboss.seam.security.Identity;
 
 import javax.annotation.PostConstruct;
 import javax.xml.rpc.ServiceException;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.rmi.RemoteException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 // import dk.statsbiblioteket.doms.repository.SearchResult;
 
 @Name("search")
 @Scope(ScopeType.SESSION)
 public class Search {
 
     @Logger
     private Log log;
 
     @In(create = true)
     private DomsManagerBean domsManager;
     @In
     private Identity identity;
 
     private SimpleDateFormat dateFormat;
 
     //Default text query string the user can search for
     private String queryString;
     private String queryType;
     private Date queryStartTime;
     private Date queryEndTime;
 
 
     @DataModel
     private List<SearchResult> searchResults;
     private long hitCount;
     private int pageSize = 25;
     @RequestParameter
     private Integer page;
     private List<Integer> pages;
 
     @DataModelSelection
     private SearchResult selectedResult;
 
     public Search() {
         dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
     }
 
     @PostConstruct
     public void login() {
         if (identity == null) {
             identity = ((Identity) Component.getInstance(Identity.class));
         }
 
         if (!identity.isLoggedIn()) {
             identity.login();
         }
     }
 
     @Factory("searchResults")
     public void setup()  throws RemoteException, ServiceException
     {
         searchResults = new ArrayList<SearchResult>();
     }
 
     /**
      * A search-query consisting of three elements is created:
      * - free text query (queryString)
      * - a record-type indicator (queryType)
      * - a date-range (queryStartTime + queryEndTime)
      *
      * @throws RemoteException
      * @throws ServiceException
      * @throws MalformedURLException
      * @throws ServerOperationFailed
      */
 
     public void search() throws RemoteException, ServiceException, MalformedURLException, ServerOperationFailed {
         if (page == null) { page = 1; }
 
         String timeSearch = "";
         String typeSearch = "";
 
        if (queryString == null || queryString.length() == 0) {
            queryString = "*";
         } else {
             // queryString is replaced so that the trim will have effect on the next page.
             queryString = queryString.trim();
         }
 
         // To search in dates, both start- and end-time must be set.
         if (queryStartTime != null && queryEndTime != null) {
             // Leading white-space (" ") is significant. Do not remove. There be dragons in Summa.
             timeSearch =  " iso_dateTime:[" + dateFormat.format(queryStartTime) + " TO " + dateFormat.format(queryEndTime) + "]";
         } else if (queryStartTime != null || queryEndTime != null) {
             Util.addFacesMessage("You need to fill out both date fields when searching in time.");
         }
 
         // Limit to specific types of results.
         if (queryType != null) {
             // queryType is replaced so that the trim will have effect on the next page.
             queryType = queryType.trim();
             if (queryType.length() > 0) {
                 // Leading white-space (" ") is significant. Do not remove. There be dragons in Summa.
                 typeSearch = " lma:\"" + queryType + "\"";
             }
         }
 
         /* Summa-specific hack. * isn't necessary to select everything when already searching
            by type or date, and it will actually cause summa to fail. */
         String strippedQueryString =
                 ((timeSearch.length() > 0 || typeSearch.length() > 0) && queryString.equals("*")) ? "" : queryString;
 
 
         String actualQueryString = strippedQueryString + typeSearch + timeSearch;
 
         log.debug("searching for: " + actualQueryString);
         SearchResultList searchResultList = domsManager.getRepository().searchSelection(actualQueryString, pageSize * (page-1), pageSize);
         searchResults = searchResultList.getSearchResults();
         hitCount = searchResultList.getHitCount();
 
         int pageCount = (int) Math.ceil((float) hitCount/pageSize);
         pages = new ArrayList<Integer>();
         for (int i = 1; i<= pageCount; ++i) {
             pages.add(i);
         }
     }
 
 
 
     public String getQueryString()
     {
         return queryString;
     }
 
     public void setQueryString(String value)
     {
         this.queryString = value;
     }
 
     public String getQueryType() {
         return queryType;
     }
 
     public void setQueryType(String queryType) {
         this.queryType = queryType;
     }
 
     public Date getQueryStartTime() {
         return queryStartTime;
     }
 
     public void setQueryStartTime(Date queryStartTime) {
         this.queryStartTime = queryStartTime;
     }
 
     public Date getQueryEndTime() {
         return queryEndTime;
     }
 
     public void setQueryEndTime(Date queryEndTime) {
         this.queryEndTime = queryEndTime;
     }
 
     public long getHitCount() {
         return hitCount;
     }
 
     public void setHitCount(long hitCount) {
         this.hitCount = hitCount;
     }
 
     public Integer getPage() {
         return page;
     }
 
     public List<Integer> getPages() {
         return pages;
     }
 
     public int getPageSize() {
         return pageSize;
     }
 
     public String selectDataObject(String pid) throws IOException, ServerOperationFailed, ServiceException {
         if (pid!=null)
         {
             log.info("selected searchresult pid: " + pid);
             setup();
             domsManager.setRootDataObject(pid);
         }
         else
         {
             log.warn("search.selectDataObject - pid was null");
             Util.addFacesMessage("Could not open record. Invalid Doms Id");
             return "";
         }
 
         return "/editRecord.xhtml";
     }
 }
