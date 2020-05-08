 /*******************************************************************************
  * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
  * Licensed under the Apache License, Version 2.0 (the "License"); 
  * you may not use this file except in compliance with the License. 
  * You may obtain a copy of the License at 
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *    
  *******************************************************************************/
 package org.ebayopensource.turmeric.services.ratelimiterservice.provider.config;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.ebayopensource.turmeric.common.v1.types.CommonErrorData;
 import org.ebayopensource.turmeric.errorlibrary.turmericratelimiter.ErrorConstants;
 import org.ebayopensource.turmeric.ratelimiter.provider.RateLimiterProvider;
 import org.ebayopensource.turmeric.runtime.common.exceptions.ErrorDataFactory;
 import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceException;
 import org.ebayopensource.turmeric.runtime.common.impl.utils.LogManager;
 import org.ebayopensource.turmeric.utils.ReflectionUtils;
 import org.ebayopensource.turmeric.utils.config.exceptions.ConfigurationException;
 
 /**
  * A factory for creating RateLimiterServiceProvider objects.
  */
 public class RateLimiterServiceProviderFactory {
 	
 	private static Map<String, RateLimiterProvider>  s_serviceProviderMap = new HashMap<String, RateLimiterProvider>();
 	private static Set<String> s_failedProviders = new HashSet<String>();
 	private static String s_defaultProviderKey;
 	private static volatile CommonErrorData s_errorData;
 	private static Logger s_Logger = LogManager.getInstance(RateLimiterServiceProviderFactory.class);
 	
 	static {
 		// static initialization
 		RateLimiterServiceProviderConfigManager configMngr = RateLimiterServiceProviderConfigManager.getInstance();
 		try {
 			s_defaultProviderKey = configMngr.getConfig().getDefaultProvider();			
 		} catch (ConfigurationException e) {
 			s_errorData = getConfigError(configMngr);
 		}
 	}
 	
 	// disable creating instances
 	private RateLimiterServiceProviderFactory() {
 		
 	}
 	
 	/**
 	 * Creates the.
 	 *
 	 * @return the rate limiter provider
 	 * @throws ServiceException the service exception
 	 */
 	public static RateLimiterProvider create() throws ServiceException {
 		return create(s_defaultProviderKey);
 	}
 
 	/**
 	 * Creates the.
 	 *
 	 * @param providerKey the provider key
 	 * @return the rate limiter provider
 	 * @throws ServiceException the service exception
 	 */
 	public static RateLimiterProvider create(String providerKey) throws ServiceException { 
 		
 		if (s_errorData != null) 
 			throw new ServiceException(s_errorData);
 		
 		if (providerKey == null)
 			providerKey = s_defaultProviderKey;
 		
 		RateLimiterProvider providerImpl = s_serviceProviderMap.get(providerKey);
 		RateLimiterServiceProviderConfigManager configMngr = RateLimiterServiceProviderConfigManager.getInstance();
 		
 		if (providerImpl == null) {
 			// check the failed set
 			if (s_failedProviders.contains(providerKey)) {
				new ServiceException(getConfigError(configMngr));
 			}
 			synchronized (RateLimiterServiceProviderFactory.class) {
 				providerImpl = s_serviceProviderMap.get(providerKey);
 				if (providerImpl == null) {
 					try {
 						String providerImplClassName = configMngr.getConfig().getProviderImplClassName(providerKey);
 						if (providerImplClassName != null) {
 							providerImpl = getServiceDataModelProviderInstance(providerImplClassName);
 							if (providerImpl != null)
 								s_serviceProviderMap.put(providerKey, providerImpl);
 						}
 					} catch (ConfigurationException ce) {
 						s_Logger.log(Level.SEVERE, "invalid configuration" , ce);
 					}
 				}
 				if (providerImpl == null) {
 					s_failedProviders.add(providerKey);
 				}
 			}
 			
 			if (providerImpl == null) {
 				throw new ServiceException(getConfigError(configMngr));
 			}
 		}		
 		
 		return providerImpl;
 	}
 
 	private static RateLimiterProvider getServiceDataModelProviderInstance(String rateLimiterServiceProviderClassName) {
 		
 		RateLimiterProvider serviceProviderImpl = null;
 		ClassLoader cl = Thread.currentThread().getContextClassLoader();
 		try {
 			serviceProviderImpl = ReflectionUtils.createInstance(rateLimiterServiceProviderClassName, RateLimiterProvider.class, cl);
 
 		} catch (Exception e) {
 			s_Logger.log(Level.SEVERE, 
 					"The RateLimiterService Provider class name: " 
 						+ rateLimiterServiceProviderClassName + " is invalid",
 					e);
 			
 		}
 		return serviceProviderImpl;
 	}
 
 	private static CommonErrorData getConfigError(
 			RateLimiterServiceProviderConfigManager configMngr) {
 		return ErrorDataFactory.createErrorData(
 				ErrorConstants.SVC_RATELIMITER_INVALID_PROVIDER_CONFIGURATION, 
 				ErrorConstants.ERRORDOMAIN.toString(),
				new Object[] {new String("RateLimiterService"), 
 					configMngr.getConfigPath() + 
 					configMngr.getConfigFileName()});
 	}
 
 }
