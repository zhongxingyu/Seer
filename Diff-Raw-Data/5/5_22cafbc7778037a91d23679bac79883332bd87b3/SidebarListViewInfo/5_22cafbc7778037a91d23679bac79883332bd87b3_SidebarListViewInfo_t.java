 package com.antonyh.hutchisontechnical.hippo.componentsinfo;
 
 import org.hippoecm.hst.core.parameters.Parameter;
 
 public interface SidebarListViewInfo extends ListViewInfo {
 	@Parameter(name = "pageSize", defaultValue = "5", displayName = "Page Size")
 	int getPageSize();
 
 	@Parameter(name = "sortBy", displayName = "Sort By Property", defaultValue = "ht:date, hippostdpubwf:publicationDate")
 	String getSortBy();
 
	// @Parameter(name = "docType", displayName = "Document Type", defaultValue
	// = "ht:newsdocument")
	// String getDocType();
 
 	@Parameter(name = "sortOrder", displayName = "Sort Order", defaultValue = "descending")
 	String getSortOrder();
 
 }
