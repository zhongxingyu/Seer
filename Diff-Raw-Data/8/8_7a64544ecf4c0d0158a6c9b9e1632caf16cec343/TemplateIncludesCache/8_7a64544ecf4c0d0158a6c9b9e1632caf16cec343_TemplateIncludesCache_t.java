 /*
  * Sewing: a Simple framework for Embedded-OSGi Web Development
  * Copyright (C) 2009 Bug Labs
  * Email: bballantine@buglabs.net
  * Site: http://www.buglabs.net
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Library General Public
  * License as published by the Free Software Foundation; either
  * version 2 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Library General Public License for more details.
  *
  * You should have received a copy of the GNU Library General Public
  * License along with this library; if not, write to the
  * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
  * Boston, MA  02111-1307, USA.
  */
 
 package com.buglabs.osgi.sewing;
 
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.Iterator;
 
 import org.osgi.framework.BundleContext;
 import org.osgi.service.log.LogService;
 
 import freemarker.template.CacheListener;
 import freemarker.template.InputSource;
 import freemarker.template.Template;
 import freemarker.template.cache.Cache;
 import freemarker.template.cache.Cacheable;
 
 /**
  * This class allows us to include templates within templates All included
  * templates must be in includesAlias the default extension for include
  * templates is "inc"
  * 
  * Only the minimum methods for Cache have been implemented, getItem(String) and
  * getItem(String, String)
  * 
  * This is all we need to allow simple <include "name"> directives in Sewing
  * freemarker templates
  * 
  * @author brian
  * 
  */
 public class TemplateIncludesCache implements Cache {
 
 	private static final String UNSUPPORTED_EXCEPTION_MESSAGE_ENDING = "is not supported for type " + TemplateIncludesCache.class.getName();
 	private static final String DEFAULT_EXTENSION = "inc";
 
 	private BundleContext bundle_context;
 	private String includes_alias;
 
 	public TemplateIncludesCache(BundleContext context, String includesAlias) {
 		bundle_context = context;
 		includes_alias = includesAlias;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see freemarker.template.cache.Cache#getItem(java.lang.String)
 	 */
 	public Cacheable getItem(String name) {
 		return getItem(name, DEFAULT_EXTENSION);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see freemarker.template.cache.Cache#getItem(java.lang.String,
 	 * java.lang.String)
 	 */
 	public Cacheable getItem(String name, String type) {
		String path = "/" + includes_alias + "/" + name + "." + type;
		URL templateUrl = bundle_context.getBundle().getResource(path);
		if (templateUrl == null) {
			LogManager.log(LogService.LOG_ERROR, "Failed to get item from input:" + path);
			return null;
		}
		
 		InputSource inputSource;
 		Template t = null;
 		try {
 			inputSource = new InputSource(new InputStreamReader(templateUrl.openStream()));
 			t = new Template(inputSource);
 		} catch (IOException e) {
 			LogManager.log(LogService.LOG_ERROR, "Failed to get item from input.", e);
 		}
 		return t;
 	}
 
 	/*
 	 * we just want to be able to include stuff from the includes folder so the
 	 * following methods are not implemented
 	 */
 
 	/**
 	 * Not implemented - throws UnsupportedOperationException
 	 */
 	public void addCacheListener(CacheListener listener) {
 		throw new UnsupportedOperationException("addCacheListener" + UNSUPPORTED_EXCEPTION_MESSAGE_ENDING);
 	}
 
 	/**
 	 * Not implemented - throws UnsupportedOperationException
 	 */
 	public CacheListener[] getCacheListeners() {
 		throw new UnsupportedOperationException("addCacheListener" + UNSUPPORTED_EXCEPTION_MESSAGE_ENDING);
 	}
 
 	/**
 	 * Not implemented - throws UnsupportedOperationException
 	 */
 	public Iterator listCachedFiles() {
 		throw new UnsupportedOperationException("addCacheListener" + UNSUPPORTED_EXCEPTION_MESSAGE_ENDING);
 	}
 
 	/**
 	 * Not implemented - throws UnsupportedOperationException
 	 */
 	public void removeCacheListener(CacheListener listener) {
 		throw new UnsupportedOperationException("addCacheListener" + UNSUPPORTED_EXCEPTION_MESSAGE_ENDING);
 	}
 
 	/**
 	 * Not implemented - throws UnsupportedOperationException
 	 */
 	public void stopAutoUpdate() {
 		throw new UnsupportedOperationException("addCacheListener" + UNSUPPORTED_EXCEPTION_MESSAGE_ENDING);
 	}
 
 }
