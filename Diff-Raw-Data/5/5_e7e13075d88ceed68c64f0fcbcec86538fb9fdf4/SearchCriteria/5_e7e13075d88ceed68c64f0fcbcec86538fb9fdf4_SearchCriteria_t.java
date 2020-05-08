 package org.geworkbench.builtin.projects.remoteresources.query;
 
 public class SearchCriteria {
 	public static final String EXPERIMENT = "experiment";
 	public static final String BIOASSAY = "bioassay";
 	public static final String FILTER = "filter";
 	public static final String VALIDVALUES = "validvalues";
 	private String searchType;
 	private String value;
 	public String getSearchType() {
 		return searchType;
 	}
 	public void setSearchType(String searchType) {
 		this.searchType = searchType;
 	}
 	public String getValue() {
 		return value;
 	}
 	public void setValue(String value) {
 		this.value = value;
 	}
 	public SearchCriteria(String value) {
 		super();
 		searchType = FILTER;
 		this.value = value;
 	}
 	public SearchCriteria() {
 		 searchType = EXPERIMENT;
 	}
	public SearchCriteria(String searchType, String value) {
		super();
		this.searchType = searchType;
		this.value = value;
	}
 
 }
