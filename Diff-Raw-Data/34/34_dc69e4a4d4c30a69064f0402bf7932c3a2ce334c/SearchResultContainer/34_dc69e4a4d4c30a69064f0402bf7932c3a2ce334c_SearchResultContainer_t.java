package org.boncey.jsphinx;
 
 
 import org.apache.log4j.Logger;
 
 import java.util.List;
 
 /**
  * A wrapper around any search results.
  * 
  * @author Darren Greaves
  * @version $Id$ Copyright (c) 2010 Darren Greaves.
  */
 public class SearchResultContainer
 {
 
     /**
      * Default constructor.
      */
     public SearchResultContainer()
     {
 
     }
 
 
     /**
      * Field constructor.
      * 
      * @param searchIds
      * @param totalResults
      */
     public SearchResultContainer(List<Long> searchIds, int totalResults)
     {
 
         this();
         _searchIds = searchIds;
         _totalResults = totalResults;
     }
 
 
     /**
      * Logger for log4j.
      */
     @SuppressWarnings("unused")
     private static Logger _log = Logger.getLogger(SearchResultContainer.class);
 
 
     /**
      * The total number of matches.
      */
     private int _totalResults;
 
 
     /**
      * The ids of the search results.
      */
     private List<Long> _searchIds;
 
 
     /**
      * Get the searchIds.
      * 
      * @return the searchIds.
      */
     public List<Long> getSearchIds()
     {
 
         return _searchIds;
     }
 
 
     /**
      * Get the totalResults.
      * 
      * @return the totalResults.
      */
     public int getTotalResults()
     {
 
         return _totalResults;
     }
 
 
     /**
      * {@inheritDoc}
      */
     @SuppressWarnings("boxing")
     @Override
     public String toString()
     {
 
         return String.format("SearchResultContainer [_searchIds=%s, _totalResults=%s]", _searchIds, _totalResults);
     }
 }
