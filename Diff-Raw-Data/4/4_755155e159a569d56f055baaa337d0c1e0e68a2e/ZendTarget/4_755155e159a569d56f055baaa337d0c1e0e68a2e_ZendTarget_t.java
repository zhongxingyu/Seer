 /*******************************************************************************
  * Copyright (c) May 18, 2011 Zend Technologies Ltd. 
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at 
  * http://www.eclipse.org/legal/epl-v10.html  
  *******************************************************************************/
 package org.zend.sdklib.internal.target;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 import java.util.Set;
 
 import org.zend.sdklib.target.IZendTarget;
 import org.zend.webapi.core.WebApiClient;
 import org.zend.webapi.core.WebApiException;
 import org.zend.webapi.core.connection.auth.BasicCredentials;
 import org.zend.webapi.core.connection.auth.WebApiCredentials;
 import org.zend.webapi.core.connection.data.SystemInfo;
 import org.zend.webapi.core.connection.response.ResponseCode;
 
 /**
  * Represents a target in the environment
  * 
  * @author Roy, 2011
  */
 public class ZendTarget implements IZendTarget {
 
 	private static final String EXTRA = "extra.";
 	private String id;
 	private URL host;
 	private URL defaultServerURL;
 	private String key;
 	private String secretKey;
 	private Properties properties;
 	private boolean isTemporary;
 
 	/**
 	 * Mainly used for loading
 	 */
 	public ZendTarget() {
 		// empty target
 		this.properties = new Properties();
 	}
 
 	/**
 	 * @param id
 	 * @param host
 	 * @param key
 	 * @param secretKey
 	 */
 	public ZendTarget(String id, URL host, String key, String secretKey) {
 		this(id, host, host, key, secretKey);
 	}
 
 	/**
 	 * @param id
 	 * @param host
 	 * @param defaultServerURL
 	 * @param key
 	 * @param secretKey
 	 */
 	public ZendTarget(String id, URL host, URL defaultServerURL, String key, String secretKey) {
 		super();
 		this.id = id;
 		this.host = host;
 		this.defaultServerURL = defaultServerURL;
 		this.key = key;
 		this.secretKey = secretKey;
 		this.properties = new Properties();
 
 		validateTarget();
 	}
 	
 	public ZendTarget(String id, URL host, URL defaultServerURL, String key, String secretKey, boolean temporary) {
 		this(id, host, defaultServerURL, key, secretKey);
 		this.isTemporary = temporary;
 	}
 
 	public boolean isTemporary() {
 		return isTemporary;
 	}
 	
 	/**
 	 * 
 	 * @return null or success, or an error message otherwise
 	 */
 	public String validateTarget() {
 		if (id == null || host == null || secretKey == null || key == null
 				|| key.length() == 0) {
 			return "Target id, host, key name and secret must not be null.";
 		}
 		// id validation
 		final char f = this.id.charAt(0);
 		if (!Character.isJavaIdentifierStart(f) && !Character.isDigit(f)) {
 			return "Target id must start with valid identifier: letter, number, $ or _";
 		}
 		for (int i = 1; i < this.id.length(); i++) {
 			char c = this.id.charAt(i);
 			if (!Character.isJavaIdentifierPart(c)) {
 				return "Target id is invalid: "+c;
 			}
 		}
 
 		// host validation - port should not be specified
 		if (this.host.getPort() != -1) {
 			return "Target host port should not be specified";
 		}
 
 		return null;
 	}
 
 	@Override
 	public String getId() {
 		return id;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.zend.sdklib.target.IZendTarget#getHost()
 	 */
 	@Override
 	public URL getHost() {
 		return host;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.zend.sdklib.target.IZendTarget#getAppHost()
 	 */
 	@Override
 	public URL getDefaultServerURL() {
 		return defaultServerURL;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.zend.sdklib.target.IZendTarget#getKey()
 	 */
 	@Override
 	public String getKey() {
 		return key;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.zend.sdklib.target.IZendTarget#getSecretKey()
 	 */
 	@Override
 	public String getSecretKey() {
 		return secretKey;
 	}
 
 	/**
 	 * Set target host
 	 * 
 	 * @param host
 	 */
 	public void setHost(URL host) {
 		this.host = host;
 	}
 
 	/**
 	 * Set default server URL
 	 * 
 	 * @param defaultServerURL
 	 */
 	public void setDefaultServerURL(URL defaultServerURL) {
 		this.defaultServerURL = defaultServerURL;
 	}
 
 	/**
 	 * Set target key
 	 * 
 	 * @param key
 	 */
 	public void setKey(String key) {
 		this.key = key;
 	}
 
 	/**
 	 * Set target secret key
 	 * 
 	 * @param secretKey
 	 */
 	public void setSecretKey(String secretKey) {
 		this.secretKey = secretKey;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.zend.sdklib.target.IZendTarget#getProperty(java.lang.String)
 	 */
 	@Override
 	public synchronized String getProperty(String key) {
 		return properties.getProperty(EXTRA + key);
 	}
 
 	/**
 	 * Adds an extra property to the target
 	 * 
 	 * @param key
 	 * @param value
 	 */
 	public synchronized void addProperty(String key, String value) {
 		properties.put(EXTRA + key, value);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.zend.sdklib.target.IZendTarget#load(java.io.InputStream)
 	 */
 	@Override
 	public synchronized void load(InputStream is) throws IOException {
 		Properties properties = new Properties();
 		properties.load(is);
 		this.id = properties.getProperty("_id");
 		this.key = properties.getProperty("_key");
 		this.secretKey = properties.getProperty("_secretKey");
 		this.host = new URL(properties.getProperty("_host"));
 		String url = properties.getProperty("_defaultServerURL");
 		if (url != null) {
 			this.defaultServerURL = new URL(url);
 		}
 		final Set<String> stringPropertyNames = properties
 				.stringPropertyNames();
 		for (String keyName : stringPropertyNames) {
 			if (keyName.startsWith(EXTRA)) {
 				this.properties.put(keyName, properties.getProperty(keyName));
 			}
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.zend.sdklib.target.IZendTarget#store(java.io.OutputStream)
 	 */
 	@Override
 	public synchronized void store(OutputStream os) throws IOException {
 		Properties properties = new Properties();
 		properties.put("_id", getId());
 		properties.put("_key", getKey());
 		properties.put("_secretKey", getSecretKey());
 		properties.put("_host", getHost().toString());
 		properties.put("_defaultServerURL", getDefaultServerURL().toString());
 		properties.putAll(this.properties);
 		properties.store(os, "target properties for " + getId());
 	}
 
 	@Override
 	public boolean connect() throws WebApiException {
 		WebApiCredentials credentials = new BasicCredentials(getKey(),
 				getSecretKey());
 		try {
 			String hostname = getHost().toString();
 			WebApiClient client = new WebApiClient(credentials, hostname, SSLContextInitializer.instance.getRestletContext());
 			final SystemInfo info = client.getSystemInfo();
 			addProperty("edition", info.getEdition().name());
 			addProperty("operatingSystem", info.getEdition().name());
 			addProperty("phpVersion", info.getPhpVersion());
 			addProperty("status", info.getStatus().name());
 			addProperty("serverVersion", info.getVersion());
 			addProperty("supportedApiVersions", info.getSupportedApiVersions()
 					.toString());
 		} catch (MalformedURLException e) {
 			return false;
 		} catch (final WebApiException e) {
 			final String betterMessage = replaceWebApiMessage(e.getMessage());
			if (betterMessage == null) {
				throw e;
			} else {
 				throw new WebApiException() {
 					
 					private static final long serialVersionUID = 1L;
 
 					@Override
 					public ResponseCode getResponseCode() {
 						return e.getResponseCode();
 					}
 					
 					@Override
 					public String getMessage() {
 						return betterMessage;
 					}
 				};
 			}
 		}
 		return true;
 	}
 
 	private String replaceWebApiMessage(String message) {
 		if ("Zend Server Community Edition does not rely on licensing".equals(message)) {
 			return "Zend Server Community Edition does not support deployment";
 		}
 		return null;
 	}
 
 	public String[] getPropertiesKeys() {
 		Set<Object> keyset = properties.keySet();
 		List<String> result = new ArrayList<String>();
 		for (Object o : keyset) {
 			String key = (String) o;
 			if (key.startsWith(EXTRA)) {
 				result.add(key.substring(EXTRA.length()));
 			}
 		}
 		return (String[]) result.toArray(new String[result.size()]);
 	}
 
 }
