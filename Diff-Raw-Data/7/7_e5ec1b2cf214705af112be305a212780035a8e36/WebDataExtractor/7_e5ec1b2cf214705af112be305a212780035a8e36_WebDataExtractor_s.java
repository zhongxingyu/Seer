 package com.operativus.senacrs.audit.extractors.webdriver;
 
 import com.operativus.senacrs.audit.exceptions.ExceptionFactory;
 import com.operativus.senacrs.audit.extractors.DataExtractor;
 import com.operativus.senacrs.audit.model.config.Configuration;
 import com.operativus.senacrs.audit.model.form.Form;
 
 
 public class WebDataExtractor
 		implements DataExtractor {
 	
 	private final Configuration config;
 	
 	public WebDataExtractor(Configuration config) {
 
 		super();
 		
 		if (config == null) {
 			throw ExceptionFactory.getInstance().getNullArgumentException("config");
 		}
 		this.config = config;
 	}
 
 	@Override
 	public Form extract() {
 
		// TODO Auto-generated method stub
		return null;
 	}
 
 }
