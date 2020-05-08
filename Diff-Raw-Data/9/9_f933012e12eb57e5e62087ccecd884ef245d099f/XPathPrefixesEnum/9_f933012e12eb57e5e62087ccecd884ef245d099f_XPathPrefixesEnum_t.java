 package com.operativus.senacrs.audit.properties.xpath;
 
 public enum XPathPrefixesEnum {
 
	DASHBOARD("xpath.dashboard"),
	ABOUT("xpath.about"),
	LOGIN("xpath.login"),
	PORTAL("xpath.portal"),
	CLASS("xpath.class"),
	GRADES("xpath.grades"),
	PLAN("xpath.plan"),
	;
 
 	private final String keyPrefix;
 
 	private XPathPrefixesEnum(final String keyPrefix) {
 
 		this.keyPrefix = keyPrefix;
 	}
 
 	public String getKeyPrefix() {
 
 		return this.keyPrefix;
 	}
 }
