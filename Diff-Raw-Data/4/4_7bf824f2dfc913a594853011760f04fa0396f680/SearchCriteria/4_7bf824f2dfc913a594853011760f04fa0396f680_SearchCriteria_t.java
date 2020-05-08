 package com.megalogika.sv.service;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.persistence.Query;
 
 import org.apache.log4j.Logger;
 import org.springframework.security.Authentication;
 import org.springframework.security.context.SecurityContextHolder;
 import org.springframework.util.Assert;
 import org.springframework.util.StringUtils;
 
 import com.megalogika.sv.service.filter.Filter;
 import com.megalogika.sv.service.filter.FirstFilterAware;
 
 public abstract class SearchCriteria {
 	protected transient Logger logger = Logger.getLogger(SearchCriteria.class);
 	
 	public static final String ORDER_BY_DATE = "entryDate";
 	public static final int ITEMS_PER_PAGE = 6;
 	protected String orderBy = ORDER_BY_DATE;
 	protected int page = 0;
 	protected int itemsPerPage = ITEMS_PER_PAGE;
 
 	protected List<Filter> filters = new ArrayList<Filter>();
 
 	long totalItems;
 
 	public SearchCriteria() {
 		// nothing
 	}
 
 	abstract public String getWhereClause();
 	abstract public String getOrderByClause();
 	
 	private boolean hasAuthority(Authentication a, String authorityName) {
 		Assert.notNull(a);
 		Assert.notNull(authorityName);
 		
 		if (a.isAuthenticated()) {
 			for (int i = 0; i < a.getAuthorities().length; i++) {
 				if (authorityName.equals(a.getAuthorities()[i].getAuthority())) {
 					return true;
 				}
 			}
 		}
 		
 		return false;
 	}
 
 	protected boolean hasAuthority(String roleAdmin) {
 		return hasAuthority(SecurityContextHolder.getContext().getAuthentication(), roleAdmin);
 	}
 
 	public String getOrderBy() {
 		return orderBy;
 	}
 
 	public void updateItemsPerPage(int newItemsPerPage) {
 		int currFirstItem = itemsPerPage * page;
 		setItemsPerPage(newItemsPerPage);
 		setPage(currFirstItem / newItemsPerPage);
 	}
 
 	public void setOrderBy(String orderBy) {
 		this.orderBy = orderBy;
 	}
 
 	public int getPage() {
 		return page;
 	}
 
 	public void setPage(int page) {
 		this.page = page;
 	}
 
 	public void setPage(String page) {
 		setPage(Integer.parseInt(page));
 	}
 
 	public int getItemsPerPage() {
 		return itemsPerPage;
 	}
 
 	public void setItemsPerPage(int itemsPerPage) {
 		this.itemsPerPage = itemsPerPage;
 	}
 
 	public long getTotalItems() {
 		return totalItems;
 	}
 
 	public void setTotalItems(long l) {
 		this.totalItems = l;
 	}
 
 	public long getTotalPages() {
 			long ret = totalItems / itemsPerPage;
 	//		if (0 != totalItems % itemsPerPage) {
 				ret += 1;
 	//		}
 			return ret;
 		}
 
 	public void clear() {
 		getFilters().clear();
 		setTotalItems(0);
 		setPage(0);
 		setOrderBy(ORDER_BY_DATE);
 	}
 
 	public void updatePage(String newPage) {
 		if (StringUtils.hasText(newPage)) {
 			setPage(Integer.parseInt(newPage));
 		}
 	}
 
 	public void updatePageSize(String pageSize) {
 		if (StringUtils.hasText(pageSize)) {
 			updateItemsPerPage(Integer.parseInt(pageSize));
 		}
 	}
 
 	/**
 	 * Išmetam visus už nurodyto indekso esančius filtrus
 	 * 
 	 * @param filterIndex
 	 */
 	public void resetToFilter(String filterIndex) {
 		setPage(0);
 		int index = Integer.parseInt(filterIndex);
 		while (filters.size() > index + 1) {
 			filters.remove(filters.size() - 1);
 		}
 	}
 	
 	public void removeFilter(String filterIndex) {
 		setPage(0);
 		if (filters.isEmpty()) {
 			logger.warn("ATTEMPT TO REMOVE FILTER, WHEN NO FILTERS ARE SPECIFIED");
 		} else {
 			filters.remove(Integer.parseInt(filterIndex));
 			refreshFilterOrder();
 		}
 	}
 
 	protected void addFilter(Filter newFilter) {
 		if (! getFilters().contains(newFilter)) {
 			this.getFilters().add(newFilter);
 			logger.debug("FILTERS BEFORE: " + getFilters());
 			Collections.sort(getFilters());
 			Collections.reverse(getFilters());
 			refreshFilterOrder();
 			logger.debug("FILTERS AFTER: " + getFilters());
 		}
 	}
 	
 	private void refreshFilterOrder() {
 		for (Filter f: getFilters()) {
 			((FirstFilterAware) f).setFirstFilter(false);
 		}
 		if (! getFilters().isEmpty()) {
 			((FirstFilterAware)getFilters().get(0)).setFirstFilter(true);
 		}
 	}
 	
 	public void setFilters(List<Filter> filters) {
 		this.filters = filters;
 		refreshFilterOrder();
 	}
 
 	public List<Filter> getFilters() {
 		return filters;
 	}
 	
 	public boolean hasFilter() {
 		return null != getFilters() && ! getFilters().isEmpty();
 	}
 
 	public Map<String,Object> getParameterMap() {
 		Map<String,Object> ret = new HashMap<String, Object>();
 		for (Filter f : getFilters()) {
 			if (null != f.getParameterMap()) {
 				ret.putAll(f.getParameterMap());
 			}
 		}
 		
 		return ret;
 	}
 
 	public Query setParameters(Query query) {
 		Map<String, Object> params = getParameterMap();
 		for (String key : params.keySet()) {
 			query.setParameter(key, params.get(key));
 		}
 		
 		return query;
 	}
 	
	public boolean containsFilter(String filterName) {
 		boolean ret = false;
 		for (Filter f : this.getFilters()) {
 			if (f.getDescriptionArgument().equalsIgnoreCase(filterName)) {
 				ret = true;
 				break;
 			}
 		}
 		return ret;
 	}
 }
