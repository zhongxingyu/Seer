 /*
  * Copyright (c) 2009 Stephan Schloepke and innoQ Deutschland GmbH
  *
  * Stephan Schloepke: http://www.schloepke.de/
  * innoQ Deutschland GmbH: http://www.innoq.com/
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package org.jbasics.types.container;
 
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 
 import org.jbasics.pattern.container.PageableData;
 
 public class PagedList<T> implements PageableData<T> {
 	private final List<T> data;
 	private final int pageSize;
 	private int page;
 	private transient List<T> currentPageData;
 
 	public PagedList(final int pageSize, final List<T> data) {
 		this.pageSize = pageSize < 0 ? 0 : pageSize;
 		this.data = data == null ? Collections.<T> emptyList() : Collections.unmodifiableList(data);
 	}
 
 	public Iterator<T> iterator() {
 		return getCurrentPageList().iterator();
 	}
 
 	public boolean firstPage() {
 		this.page = 0;
 		this.currentPageData = null;
 		return !this.data.isEmpty();
 	}
 
 	public boolean nextPage() {
		final int newPage = this.page + 1;
 		if (this.data.size() < newPage * this.pageSize) {
 			return false;
 		}
 		this.page = newPage;
 		this.currentPageData = null;
 		return true;
 	}
 
 	public boolean previousPage() {
 		if (this.page == 0) {
 			return false;
 		}
 		this.page = this.page - 1;
 		this.currentPageData = null;
 		return true;
 	}
 
 	public boolean lastPage() {
 		this.page = this.data.size() / this.pageSize;
 		this.currentPageData = null;
 		return !this.data.isEmpty();
 	}
 
 	public boolean gotoPage(final int newPage) {
 		if (newPage < 0) {
 			return false;
 		} else if (this.data.size() < newPage * this.pageSize) {
 			return false;
 		} else {
 			this.page = newPage;
 			this.currentPageData = null;
 			return true;
 		}
 	}
 
 	public int totalSize() {
 		return this.data.size();
 	}
 
 	public int currentPageSize() {
 		if (this.pageSize > 0) {
 			return getCurrentPageList().size();
 		} else {
 			return this.data.size();
 		}
 	}
 
 	public int totalPages() {
 		if (this.pageSize > 0) {
 			return (totalSize() + this.pageSize - 1) / this.pageSize;
 		} else {
 			return 1;
 		}
 	}
 
 	public int currentPage() {
 		return this.page;
 	}
 
 	public int pageSize() {
 		return this.pageSize;
 	}
 
 	private List<T> getCurrentPageList() {
 		if (this.currentPageData == null) {
			final int start = this.page * this.pageSize;
			this.currentPageData = this.data.subList(start, Math.min(start + this.pageSize, this.data.size()));
 		}
 		return this.currentPageData;
 	}
 
 }
