 package com.operativus.senacrs.audit.properties.xpath;
 
 public enum XPathPrefixesEnum {
 
	DASHBOARD("xpath.dashboard"), ;
 
 	private final String keyPrefix;
 
 	private XPathPrefixesEnum(final String keyPrefix) {
 
 		this.keyPrefix = keyPrefix;
 	}
 
 	public String getKeyPrefix() {
 
 		return this.keyPrefix;
 	}
 }
