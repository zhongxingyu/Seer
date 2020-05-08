 /*
  * Copyright (C) 2013 Iorga Group
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program.  If not, see [http://www.gnu.org/licenses/].
  */
 package com.iorga.iraj.servlet;
 
import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.nio.file.FileSystems;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.nio.file.StandardWatchEventKinds;
 import java.nio.file.WatchEvent;
 import java.nio.file.WatchKey;
 import java.nio.file.WatchService;
 import java.util.Date;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 
 import org.apache.commons.codec.digest.DigestUtils;
 import org.apache.commons.lang3.StringUtils;
 import org.jboss.resteasy.util.DateUtil;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Attribute;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.collect.Maps;
 import com.iorga.iraj.servlet.CacheAwareServlet.CacheEntry.Attributes;
 import com.iorga.iraj.servlet.CacheAwareServlet.CacheEntry.Resource;
 
 public class AgglomeratorServlet extends CacheAwareServlet {
 	private static final Logger log = LoggerFactory.getLogger(AgglomeratorServlet.class);
 
 	private static final String ATTRIBUTE_NAME = "iraj-agglomerate";
 	private static final String URL_ATTRIBUTE_ATTRIBUTE_NAME = "urlAttribute";
 	private static final long serialVersionUID = 1L;
 
 	private final Map<String, CacheEntry> caches = Maps.newHashMap();
 	private Mode mode = Mode.PRODUCTION;
 
 	private WatchService watchService;
 	private Thread directoryWatcherThread;
 
 	private static enum Mode {
 		DEVELOPMENT, PRODUCTION
 	}
 
 	@Override
 	public void init(final ServletConfig config) throws ServletException {
 		super.init(config);
 
         if (config.getInitParameter("mode") != null) {
         	mode = Mode.valueOf(config.getInitParameter("mode").toUpperCase());
         }
         if (mode == Mode.DEVELOPMENT) {
         	// development mode, activate the watch service
         	try {
 				watchService = FileSystems.getDefault().newWatchService();
 			} catch (final IOException e) {
 				throw new ServletException("Problem while activating the watch service", e);
 			}
         }
 
 		parseResourcesFromMappings(config);
 
 		if (mode == Mode.DEVELOPMENT) {
 			directoryWatcherThread = new Thread(new DirectoryWatcher(), DirectoryWatcher.class.getName());
 			directoryWatcherThread.setDaemon(true);
 			directoryWatcherThread.start();
 		}
 	}
 
 	@Override
 	public void destroy() {
 		directoryWatcherThread.interrupt();
 		super.destroy();
 	}
 
 	private void parseResourcesFromMappings(final ServletConfig config) throws ServletException {
 		for (final String mapping : config.getServletContext().getServletRegistration(config.getServletName()).getMappings()) {
 			try {
 				parseResource(config, mapping);
 			} catch (final IOException | URISyntaxException e) {
 				throw new ServletException("Problem while parsing the mapping "+mapping, e);
 			}
 		}
 	}
 
 	private class DirectoryWatcher implements Runnable {
 		@Override
 		public void run() {
 			while (true) {
 				// based on http://docs.oracle.com/javase/tutorial/essential/io/notification.html
 				// wait for key to be signaled
 				WatchKey key;
 				try {
 					key = watchService.take();
 				} catch (final InterruptedException e) {
 					if (log.isDebugEnabled()) {
 						log.debug("Interrupted while watchService.take", e);
 					}
 					return;
 				}
 
 				for (final WatchEvent<?> event : key.pollEvents()) {
 					final WatchEvent.Kind<?> kind = event.kind();
 					if (kind != StandardWatchEventKinds.OVERFLOW) {
 						// something happened in the target dir, let's recompute all
 						try {
 							if (log.isDebugEnabled()) {
 								log.debug("Received an event "+kind);
 							}
 							parseResourcesFromMappings(getServletConfig());
 						} catch (final ServletException e) {
 							log.warn("Problem while re-parsing the resources from servlet mappings", e);
 						}
 					}
 				}
 				// Reset the key -- this step is critical if you want to
 				// receive further watch events. If the key is no longer
 				// valid, the directory is inaccessible so exit the
 				// loop.
 				final boolean valid = key.reset();
 				if (!valid) {
 					return;
 				}
 			}
 		}
 	}
 
 	private class ParsedResourceCacheEntry implements CacheEntry, Attributes, Resource {
 		private final String name;
 		private String mimeType;
 		private final byte[] content;
 		private final Date lastModified;
 		private final String lastModifiedHttp;
 		private final String eTag;
 
 		public ParsedResourceCacheEntry(final String path, final Document document, final long lastModified) {
 			this.name = path;
 			this.content = document.outerHtml().getBytes();
 			this.lastModified = new Date(lastModified);
 			this.lastModifiedHttp = DateUtil.formatDate(this.lastModified);
 			this.eTag = DigestUtils.sha512Hex(content);
 		}
 
 		@Override
 		public boolean exists() {
 			return true;
 		}
 
 		@Override
 		public Attributes getAttributes() {
 			return this;
 		}
 
 		@Override
 		public Resource getResource() {
 			return this;
 		}
 
 		@Override
 		public String getName() {
 			return name;
 		}
 
 		@Override
 		public String getMimeType() {
 			return mimeType;
 		}
 
 		@Override
 		public void setMimeType(final String contentType) {
 			this.mimeType = contentType;
 		}
 
 		@Override
 		public String getETag() {
 			return eTag;
 		}
 
 		@Override
 		public String getLastModifiedHttp() {
 			return lastModifiedHttp;
 		}
 
 		@Override
 		public long getContentLength() {
 			return content.length;
 		}
 
 		@Override
 		public long getLastModified() {
 			return lastModified.getTime();
 		}
 
 		@Override
 		public byte[] getContent() {
 			return content;
 		}
 
 		@Override
 		public InputStream streamContent() {
 			return null;
 		}
 
 	}
 
 	private void parseResource(final ServletConfig config, final String path) throws IOException, URISyntaxException {
 		//TODO catch the modifications on the path itself
 		final URL pathUrl = config.getServletContext().getResource(path);
 		long lastModified = pathUrl.openConnection().getLastModified();
 		final InputStream targetIS = pathUrl.openStream();
 		final Document document = Jsoup.parse(targetIS, "UTF-8", "");
 		final Elements elements = document.getElementsByAttribute(ATTRIBUTE_NAME);
 		for (final Element element : elements) {
 			// each element which defines iraj-agglomerate
 			// retrieve the suffix
 			final String suffix = element.attr(ATTRIBUTE_NAME);
 			final String urlAttribute = element.attr(URL_ATTRIBUTE_ATTRIBUTE_NAME);
 			String src = StringUtils.removeEndIgnoreCase(element.attr(urlAttribute), suffix);
 			String prefix = "";
 			if (!src.startsWith("/")) {
 				// this is not an absolute file, let's add the prefix from the given path
				prefix = new File(path).getParent();
 				src = prefix+src;
 			}
 			// searching all scripts inside the folder defined by src attribute
 			lastModified = searchAndAppendAfter(config, element, src, prefix, suffix, urlAttribute, lastModified);
 			// finally remove it
 			element.remove();
 		}
 
 		caches.put(path, new ParsedResourceCacheEntry(path, document, lastModified));
 	}
 
 	private long searchAndAppendAfter(final ServletConfig config, final Element agglomerateElement, final String scriptSrc, final String pathPrefix, final String pathSuffix, final String urlAttribute, long lastModified) throws MalformedURLException, IOException, URISyntaxException {
 		if (mode == Mode.DEVELOPMENT) {
 			// add a watch for that directory
 			final Path path = Paths.get(config.getServletContext().getRealPath(scriptSrc));
 			path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE);
 		}
 		final Set<String> childrenPaths = config.getServletContext().getResourcePaths(scriptSrc);
 		for (final String path : childrenPaths) {
 			if (path.endsWith(pathSuffix)) {
 				// add that JS
 				final StringBuilder targetScript = new StringBuilder("<");
 				targetScript.append(agglomerateElement.tagName());
 				// copy all the origin attributes
 				for (final Attribute attribute : agglomerateElement.attributes()) {
 					final String key = attribute.getKey();
 					if (!ATTRIBUTE_NAME.equalsIgnoreCase(key)
 							&& !urlAttribute.equalsIgnoreCase(key)
 							&& !URL_ATTRIBUTE_ATTRIBUTE_NAME.equalsIgnoreCase(key)) {
 						targetScript.append(" ").append(attribute.html());
 					}
 				}
 				// specify the src path
 				final String childUrl = StringUtils.removeStart(path, pathPrefix);
 				targetScript.append(" ").append(new Attribute(urlAttribute, childUrl).html()).append(" />");
 				agglomerateElement.after(targetScript.toString());
 				lastModified = Math.max(config.getServletContext().getResource(childUrl).openConnection().getLastModified(), lastModified);
 			} else if (path.endsWith("/")) {
 				// it's a directory, recurse search & append
 				lastModified = Math.max(searchAndAppendAfter(config, agglomerateElement, path, pathPrefix, pathSuffix, urlAttribute, lastModified), lastModified);
 			}
 		}
 		return lastModified;
 	}
 
 	@Override
 	protected CacheEntry lookupCache(final String path) {
 		return caches.get(path);
 	}
 
 }
