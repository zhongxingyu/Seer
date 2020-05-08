 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.timo.paginator;
 
 import java.io.Serializable;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * Core component handling pagination operations. It provides pagination
  * operations and pages information.
  * @param <T> working object type
  * @author Timoteo Ponce
  * @author Rory Sandoval - original implementation
  */
 public class Paginator<T> implements Serializable {
 
     public static final int DEFAULT_PAGE_SIZE = 5;
     private final PaginationData paginationData;
    private final ListProvider<T> listProvider;
     private List<T> resultList;
     private boolean dirty = true;
 
     public Paginator(final ListProvider<T> listProvider) {
         this(listProvider, 1);
     }
 
     public Paginator(final ListProvider<T> listProvider, final int pageSize) {
         this.paginationData = new PaginationData();
         this.listProvider = listProvider;
         this.paginationData.setPageSize(pageSize);
     }
 
     public Paginator(final ListProvider<T> listProvider, final PaginationData paginationData) {
         this.paginationData = paginationData;
         this.listProvider = listProvider;
     }
 
     /**
      * Returns current page's list. If there's not result at all, it returns
      * an empty list.
      * @return current page's list, or an empty list when there's not data
      */
     public List<T> getList() {
         init();
         return resultList;
     }
 
     /**
      * Redirects to the first page.
      */
     public void goFirstPage() {
         paginationData.setCurrentPage(getFirstPage());
         markAsDirty();
     }
 
     /**
      * Redirects to the previous page if available, if not
      * current page's value remains.
      */
     public void goPreviousPage() {
         paginationData.setCurrentPage(getPreviousPage());
         markAsDirty();
     }
 
     /**
      * Redirects to the next page if available, if not
      * current page's value remains.
      */
     public void goNextPage() {
         paginationData.setCurrentPage(getNextPage());
         markAsDirty();
     }
 
     /**
      * Redirects to the last page if available, if not
      * current page's value remains.
      */
     public void goLastPage() {
         paginationData.setCurrentPage(getLastPage());
         markAsDirty();
     }
 
     /**
      * Refreshes current result list.
      */
     public void refresh() {
         this.resultList = listProvider.provideList(paginationData);
         if (resultList == null || resultList.isEmpty()) {
             resultList = Collections.EMPTY_LIST;
         }
         this.dirty = false;
     }
 
     /**
      * Clears current result list. It
      * basically resets the component.
      */
     public void clear() {
         if (resultList != null) {
             this.resultList.clear();
         }
         markAsDirty();
     }
 
     public void clearPagination() {
         this.paginationData.clear();
         markAsDirty();
     }
 
     void init() {
         if (dirty) {
             refresh();
         }
     }
 
     /** masking PaginationData elements *
      * 
      * @return 
      */
     public int getCurrentPage() {
         return paginationData.getCurrentPage();
     }
 
     public void setCurrentPage(int currentPage) {
         this.paginationData.setCurrentPage(currentPage);
     }
 
     public int getPageSize() {
         return paginationData.getPageSize();
     }
 
     public void setPageSize(int pageSize) {
         this.paginationData.setPageSize(pageSize);
     }
 
     public int getTotalSize() {
         return paginationData.getTotalSize();
     }
 
     public int getFirstPage() {
         return paginationData.getFirstPage();
     }
 
     public int getNextPage() {
         return paginationData.getNextPage();
     }
 
     public int getPreviousPage() {
         return paginationData.getPreviousPage();
     }
 
     public int getLastPage() {
         return paginationData.getLastPage();
     }
 
     public boolean hasNextPage() {
         return paginationData.hasNextPage();
     }
 
     public boolean hasPreviousPage() {
         return paginationData.hasPreviousPage();
     }
 
     public boolean hasFirstPage() {
         return paginationData.hasFirstPage();
     }
 
     public boolean hasLastPage() {
         return paginationData.hasLastPage();
     }
 
     public boolean isEmpty() {
         return paginationData.isEmpty();
     }
 
     private void markAsDirty() {
         this.dirty = true;
     }
 
     public Iterator<T> iterator() {
         return new Iterator(this);
     }
 }
