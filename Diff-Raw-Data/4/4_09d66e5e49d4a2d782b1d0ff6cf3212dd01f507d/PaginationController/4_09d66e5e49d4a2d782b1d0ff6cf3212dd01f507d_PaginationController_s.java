 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 package org.extrasapache.myfaces.codi.examples.ebean.support.data;
 
 import com.avaje.ebean.Page;
 import com.avaje.ebean.PagingList;
 
 import java.util.*;
 import java.util.concurrent.Future;
 
 /**
  * @author Werner Punz (latest modification by $Author$)
  * @version $Revision$ $Date$
  */
 
 public class PaginationController<T> {
     PagingList<T> _delegate;
     Integer lastPageAccessed = 0;
 
     int pagingWindowSize = 8;
 
     public PaginationController(PagingList<T> delegate) {
 
         _delegate = delegate;
     }
 
     public void refresh() {
         _delegate.refresh();
     }
 
     public PagingList<T> setFetchAhead(boolean b) {
         return _delegate.setFetchAhead(b);
     }
 
     public Future<Integer> getFutureRowCount() {
         return _delegate.getFutureRowCount();
     }
 
     public List<T> getAsList() {
         return _delegate.getAsList();
     }
 
     public int getPageSize() {
         return _delegate.getPageSize();
     }
 
     public int getTotalRowCount() {
         return _delegate.getTotalRowCount();
     }
 
     public int getTotalPageCount() {
         return _delegate.getTotalPageCount();
     }
 
     public Page<T> getPage(int i) {
         lastPageAccessed = i;
         return _delegate.getPage(i);
     }
 
     public Page<T> fetchPage() {
         return _delegate.getPage(lastPageAccessed);
     }
 
     public Integer getLastPageAccessed() {
         return lastPageAccessed;
     }
 
     public void setLastPageAccessed(Integer lastPageAccessed) {
         this.lastPageAccessed = lastPageAccessed;
     }
 
     public boolean isFirstPage() {
         return this.lastPageAccessed == 0;
     }
 
     public boolean isLastPage() {
         return this.lastPageAccessed >= this.getTotalPageCount() - 1;
     }
 
     public boolean isInFirstWindow() {
         if (getPageSize() == 0) {
             return true;
         }
         return lastPageAccessed < pagingWindowSize;
     }
 
     public boolean hasPreviousPagesWindow() {
         return false;
     }
 
     public Integer getFirstPageIdx() {
 
         return 0;
     }
 
     public Integer getLastPageIdx() {
 
         return _delegate.getTotalPageCount() - 1;
     }
 
     public Integer getPreviousPage() {
         return Math.max(0, this.lastPageAccessed -1);
     }
 
     public Integer getNextPage() {
         return Math.max(0, Math.min(this.lastPageAccessed, this.getTotalPageCount() - 1));
     }
 
     public Integer getNextWindowIdx() {
         if (isInLastWindow()) return null;
         return (int) (Math.floor((double) lastPageAccessed / (double) pagingWindowSize) + 1) * pagingWindowSize;
     }
 
     public Integer getPreviousWindowIdx() {
         if (isInFirstWindow()) return null;
         return (int) (Math.floor((double) lastPageAccessed / (double) pagingWindowSize) - 1) * pagingWindowSize;
     }
 
     public boolean isInLastWindow() {
         if (getPageSize() == 0) {
             return true;
         }
         return lastPageAccessed >= getTotalPageCount() - pagingWindowSize;
     }
 
     public List<Integer> fetchPagesWindow() {
         List<Integer> ret = new ArrayList<Integer>(getPageSize());
 
         if (getTotalPageCount() == 1) {
             return ret;
         }
 
         int pagingWindowPos = (int) Math.floor((double) lastPageAccessed / (double) pagingWindowSize);
 
         for (int cnt = pagingWindowPos; cnt < getTotalPageCount(); cnt++) {
             ret.add(cnt);
         }
         return ret;
     }
 
    public List getPageAsList() {
        return fetchPage().getList();
     }
 
     public int getPagingWindowSize() {
         return pagingWindowSize;
     }
 
     public void setPagingWindowSize(int pagingWindowSize) {
         this.pagingWindowSize = pagingWindowSize;
     }
 }
