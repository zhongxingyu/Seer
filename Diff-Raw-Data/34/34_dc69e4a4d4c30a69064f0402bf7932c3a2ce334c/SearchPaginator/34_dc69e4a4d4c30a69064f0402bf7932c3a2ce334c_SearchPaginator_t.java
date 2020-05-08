package org.boncey.jsphinx;
 
 
 import org.apache.log4j.Logger;
 
 /**
  * For paginating search results.
  * 
  * @author Darren Greaves
  * @version $Id$ Copyright (c) 2010 Darren Greaves.
  */
 public class SearchPaginator
 {
 
     /**
      * Logger for log4j.
      */
     @SuppressWarnings("unused")
     private static Logger _log = Logger.getLogger(SearchPaginator.class);
 
 
     /**
      * The number of elements to show per page.
      */
     private final int _pageSize;
 
 
     /**
      * The total results returned.
      */
     private int _totalFound;
 
 
     /**
      * The page being viewed currently.
      */
     private int _currentPage;
 
 
     /**
      * Default constructor.
      * 
      * @param pageSize
      * @param currentPage
      */
     public SearchPaginator(int pageSize, int currentPage)
     {
 
         int maxPages = SearchService.MAX_MATCHES / pageSize;
         _pageSize = pageSize;
         _currentPage = currentPage == 0 ? 1 : currentPage;
         _currentPage = _currentPage > maxPages ? maxPages : _currentPage;
     }
 
 
     /**
      * Get the search offset for the given page number.
      * 
      * @return the search offset.
      */
     public int getOffset()
     {
 
         int offset = 0;
         if (_currentPage > 0)
         {
             offset = (_currentPage - 1) * _pageSize;
         }
 
         return offset;
     }
 
 
     /**
      * Get the number of pages returned.
      * 
      * @return the number of pages returned.
      */
     public int getNumPages()
     {
 
         int numPages = 0;
         if (_pageSize > 0)
         {
             numPages = _totalFound / _pageSize;
 
             if (_totalFound % _pageSize > 0)
             {
                 numPages++;
             }
 
         }
 
         return numPages;
     }
 
 
     /**
      * Get the pageSize.
      * 
      * @return the pageSize.
      */
     public int getPageSize()
     {
 
         return _pageSize;
     }
 
 
     /**
      * Get the totalFound.
      * 
      * @return the totalFound.
      */
     public int getTotalFound()
     {
 
         return _totalFound;
     }
 
 
     /**
      * Set the totalFound.
      * 
      * @param totalFound the totalFound to set.
      */
     public void setTotalFound(int totalFound)
     {
 
         _totalFound = totalFound;
     }
 
 
     /**
      * Get the currentPage.
      * 
      * @return the currentPage.
      */
     public int getCurrentPage()
     {
 
         return _currentPage;
     }
 
 
     /**
      * Set the currentPage.
      * 
      * @param currentPage the currentPage to set.
      */
     public void setCurrentPage(int currentPage)
     {
 
         _currentPage = currentPage;
     }
 
 
     /**
      * Is there a next page?
      * 
      * @return if there is a next page.
      */
     public boolean isNextPage()
     {
 
         return _currentPage < getNumPages();
     }
 
 
     /**
      * Is there a previous page?
      * 
      * @return if there is a previous page.
      */
     public boolean isPreviousPage()
     {
 
         return _currentPage > 1;
     }
 
 
     /**
      * Get the maximum number of results Search will return.
      * 
      * @return the maximum.
      */
     public int getMaxResults()
     {
 
         return SearchService.MAX_MATCHES;
     }
 
 
     /**
      * Are there any page links to display?
      * 
      * @return are there any page links to display?
      */
     public boolean isPaginating()
     {
 
         return getNumPages() > 1;
     }
 
 
     /**
      * {@inheritDoc}
      */
     @SuppressWarnings("boxing")
     @Override
     public String toString()
     {
 
         return String.format("SearchPaginator [_currentPage=%s, _pageSize=%s, _totalFound=%s]", _currentPage, _pageSize, _totalFound);
     }
 }
