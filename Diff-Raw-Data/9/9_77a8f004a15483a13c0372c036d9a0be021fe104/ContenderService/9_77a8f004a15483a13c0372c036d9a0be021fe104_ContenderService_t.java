 /**
  * Copyright (C) 2013 Seajas, the Netherlands.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License version 3, as
  * published by the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.seajas.search.contender.service;
 
 import com.seajas.search.contender.http.SizeRestrictedHttpResponse;
 import com.seajas.search.contender.http.SizeRestrictedResponseHandler;
 import com.seajas.search.media.wsdl.IMediaNotification;
 import com.seajas.search.media.wsdl.MediaException_Exception;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 import org.apache.cxf.helpers.IOUtils;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.message.BasicHeader;
 import org.apache.http.params.CoreProtocolPNames;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.stereotype.Service;
 import org.springframework.util.StringUtils;
 
 /**
  * The contender service.
  *
  * @author Jasper van Veghel <jasper@seajas.com>
  */
 @Service
 public class ContenderService {
 	/**
 	 * The logger.
 	 */
 	private static final Logger logger = LoggerFactory.getLogger(ContenderService.class);
 
 	/**
 	 * The media notification service.
 	 */
 	@Autowired
 	private IMediaNotification mediaNotificationService;
 
 	/**
 	 * Whether to enable media pass-through.
 	 */
 	@Value("${contender.project.media.notification.enabled}")
 	private Boolean mediaEnabled;
 
 	/**
 	 * Whether to fall back to the original URI if a media-lookup fails.
 	 */
 	@Value("${contender.project.media.notification.fallback.to.original}")
 	private Boolean mediaFallback;
 
 	/**
 	 * The retrieval HTTP client.
 	 */
 	@Autowired
 	@Qualifier("retrievalHttpClient")
 	private HttpClient httpClient;
 
 	/**
 	 * The list of hosts excluded from GroupId retrieval considerations.
 	 */
 	private List<String> excludedHosts;
 
 	/**
 	 * Default constructor.
 	 */
 	public ContenderService() {
 		// Do nothing
 	}
 
 	/**
 	 * Default constructor.
 	 *
 	 * @param excludedHosts
 	 */
 	@Autowired
 	public ContenderService(@Value("${bridged.project.retrieval.excluded.hosts}") final String excludedHosts) {
 		this.excludedHosts = Arrays.asList(StringUtils.tokenizeToStringArray(excludedHosts, ",", true, true));
 	}
 
 	/**
 	 * Return the media notification URLs, or either <code>null</code> or the original URL if anything goes wrong.
 	 *
 	 * @param url
 	 * @param context
 	 * @param type
 	 * @return String
 	 */
 	public String getMediaNotificationUrl(final String url, final List<String> context, final String type) {
 		try {
 			new URL(url);
 		} catch (MalformedURLException e) {
 			if (mediaFallback) {
 				logger.error("The given URL is invalid - will use it as the notified value", e);
 
 				return url;
 			} else {
 				logger.error("The given URL is invalid - fallback are disallowed - will not add as original value", e);
 
 				return null;
 			}
 		}
 
 		try {
 			if (mediaEnabled)
 				return mediaNotificationService.storeUrl(url, context, type);
 			else {
 				if (logger.isDebugEnabled())
 					logger.debug("Media notification pass-through disabled - will use the original value");
 
 				return url;
 			}
 		} catch (MediaException_Exception e) {
 			if (mediaFallback) {
 				logger.error("Could not store the given URL in the media service", e);
 
 				return url;
 			} else {
 				logger.error("The given URL could not be stored in the media service - fallback are disallowed - will not add as original value", e);
 
 				return null;
 			}
 		}
 	}
 
 	/**
 	 * Return the media notification URLs, or either <code>null</code> or the original URL if anything goes wrong.
 	 *
 	 * @param urls
 	 * @param context
 	 * @param type
 	 * @return List<String>
 	 */
 	public List<String> getMediaNotificationUrls(final List<String> urls, final List<String> context, final String type) {
 		try {
 			for (String url : urls)
 				new URL(url);
 		} catch (MalformedURLException e) {
 			if (mediaFallback) {
 				logger.error("One of the given URLs is invalid - will use all originals as the notified values", e);
 
 				return urls;
 			} else {
 				logger.error("One of the given URLs is invalid - fallback are disallowed - will not add as original value", e);
 
 				return null;
 			}
 		}
 
 		try {
			if (mediaEnabled)
				return mediaNotificationService.storeUrls(urls, context, type);
			else {
				if (logger.isDebugEnabled())
					logger.debug("Media notification pass-through disabled - will use the original values");

				return urls;
			}
 		} catch (MediaException_Exception e) {
 			if (mediaFallback) {
 				logger.error("Could not store the given URLs in the media service", e);
 
 				return urls;
 			} else {
 				logger.error("The given URL could not be stored in the media service - fallback are disallowed - will not add as original value", e);
 
 				return null;
 			}
 		}
 	}
 
 	/**
 	 * Refine the given content through the media notification service.
 	 *
 	 * @param id
 	 * @param type
 	 */
 	public void refineMediaContent(final String id, final String type) {
 		mediaNotificationService.refineDocument(id, type);
 	}
 
 	/**
 	 * Retrieve content via the retrieval HTTP client and the given handler.
 	 *
 	 * @param handler
 	 * @param userAgent
 	 * @param resultHeaders
 	 * @param allowLocal
 	 * @return SizeRestrictedHttpResponse
 	 */
 	public SizeRestrictedHttpResponse retrieveContent(final SizeRestrictedResponseHandler handler, final String userAgent, final Map<String, String> resultHeaders, final Boolean allowLocal) {
 		try {
 			if (!handler.getUri().getScheme().equalsIgnoreCase("file")) {
 				HttpGet method = new HttpGet(handler.getUri());
 
 				if (resultHeaders != null)
 					for (Map.Entry<String, String> resultHeader : resultHeaders.entrySet())
 						method.setHeader(new BasicHeader(resultHeader.getKey(), resultHeader.getValue()));
 				if (userAgent != null)
 					method.setHeader(CoreProtocolPNames.USER_AGENT, userAgent);
 
 				return httpClient.execute(method, handler);
 			} else {
 				if (allowLocal) {
 					InputStream inputStream = new FileInputStream(handler.getUri().getPath());
 
 					return new SizeRestrictedHttpResponse(null, IOUtils.readBytesFromStream(inputStream));
 				} else {
 					logger.error("Local resource requested, but local retrieval has not explicitly been allowed");
 
 					return null;
 				}
 			}
 		} catch (IOException e) {
 			logger.error("Could not retrieve resource " + handler.getUri(), e);
 
 			return null;
 		}
 	}
 
 	/**
 	 * Retrieve the excludedHosts.
 	 *
 	 * @return List<String>
 	 */
 	public List<String> getExcludedHosts() {
 		return excludedHosts;
 	}
 }
