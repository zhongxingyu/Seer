 /**
  * Copyright 1&1 Internet AG, https://github.com/1and1/
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package net.oneandone.lavender.filter;
 
 import net.oneandone.lavender.index.Index;
 import net.oneandone.lavender.processor.ProcessorFactory;
 import net.oneandone.lavender.publisher.Resource;
 import net.oneandone.lavender.publisher.Source;
 import net.oneandone.lavender.publisher.config.Settings;
 import net.oneandone.lavender.publisher.pustefix.PustefixSource;
 import net.oneandone.lavender.rewrite.RewriteEngine;
 import net.oneandone.lavender.rewrite.UrlCalculator;
 import net.oneandone.sushi.fs.Node;
 import net.oneandone.sushi.fs.World;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URL;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Map.Entry;
 
 /**
  * A servlet filter that <em>lavendelizes</em> the response content.
  */
 public class Lavender implements Filter {
     private static final Logger LOG = LoggerFactory.getLogger(Lavender.class);
 
     public static final String LAVENDEL_IDX = "/WEB-INF/lavender.idx";
     public static final String LAVENDEL_NODES = "/WEB-INF/lavender.nodes";
 
 
     private World world;
 
     /** The filter configuration. */
     protected FilterConfig filterConfig;
 
     protected ProcessorFactory processorFactory;
 
     protected Map<String, Resource> develResources;
 
     public void init(FilterConfig config) {
         this.filterConfig = config;
     }
 
     /**
      * {@inheritDoc}
      */
     public void lazyInit() throws ServletException {
         long started;
         URL src;
         Index index;
         UrlCalculator urlCalculator;
         RewriteEngine rewriteEngine;
         Settings settings;
         Node webapp;
 
         if (world != null) {
             return;
         }
         this.world = new World();
         src = resourceOpt(LAVENDEL_IDX);
         LOG.info("init");
         try {
             if (src == null) {
                 started = System.currentTimeMillis();
                 processorFactory = null;
                 settings = Settings.load(world);
                 webapp = world.file(filterConfig.getServletContext().getRealPath(""));
                 develResources = new HashMap<>();
                 for (Source source : PustefixSource.fromWebapp(webapp, settings.svnUsername, settings.svnPassword)) {
                     for (Resource resource : source) {
                         LOG.debug("resource: " + resource);
                         develResources.put(resource.getPath(), resource);
                     }
                 }
                 LOG.info("Lavender devel filter for " + webapp + ", " + develResources.size()
                         + " resources. Init in " + (System.currentTimeMillis() - started + " ms"));
             } else {
                 index = new Index(src);
                 urlCalculator = new UrlCalculator(resourceOpt(LAVENDEL_NODES));
                 rewriteEngine = new RewriteEngine(index, urlCalculator);
                 processorFactory = new ProcessorFactory(rewriteEngine);
                 LOG.info("Lavender prod filter");
             }
         } catch (IOException ie) {
             LOG.error("Error in Lavendelizer.init()", ie);
             throw new ServletException("io error", ie);
         } catch (ServletException | RuntimeException se) {
             LOG.error("Error in Lavendelizer.init()", se);
             throw se;
         }
     }
 
     private URL resourceOpt(String path) throws ServletException {
         try {
             return filterConfig.getServletContext().getResource(path);
         } catch (MalformedURLException e) {
             throw new IllegalStateException(e);
         }
     }
 
     public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
         lazyInit();
         if (processorFactory == null) {
             doDevelFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
         } else {
             doProdFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
         }
     }
 
     public void doDevelFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
             throws IOException, ServletException {
         String path;
         Resource resource;
 
         path = request.getPathInfo();
         if (path.startsWith("/")) {
             resource = develResources.get(path.substring(1));
         } else {
             resource = null;
         }
         if (resource != null) {
            LOG.info("lavender: " + path);
             serve(resource.getNode(), response);
         } else {
             chain.doFilter(request, response);
         }
     }
 
     public void doProdFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException,
             ServletException {
 
         StringBuffer url;
         LavendelizeHttpServletRequest lavenderRequest;
         LavendelizeHttpServletResponse lavenderResponse;
 
         try {
             url = request.getRequestURL();
             URI requestURI = URI.create(url.toString());
 
             // use custom request and response objects
             lavenderRequest = new LavendelizeHttpServletRequest(request);
             lavenderResponse = new LavendelizeHttpServletResponse(response, processorFactory,
                     requestURI, request.getHeader("User-Agent"),
                     request.getContextPath() + "/", Gzip.canGzip(request));
             logRequest(url, request);
         } catch (RuntimeException re) {
             LOG.error("Error in Lavendelizer.doFilter()", re);
             throw re;
         }
 
         // continue the request
         // No exception handling at this point. Exceptions in processors are handled in LavendelizeOutputStream/Writer
         chain.doFilter(lavenderRequest, lavenderResponse);
 
         try {
             // close the response to make sure all buffers are flushed
             lavenderResponse.close();
 
             logResponse(url, lavenderResponse);
         } catch (IOException | RuntimeException e) {
             LOG.error("Error in Lavendelizer.doFilter()", e);
             throw e;
         }
     }
 
     private void logRequest(StringBuffer url, HttpServletRequest httpRequest) {
         if (LOG.isDebugEnabled()) {
             LOG.debug("Entering doFilter: url=" + url);
         }
         if (LOG.isTraceEnabled()) {
             LOG.trace("  Request headers: ");
             Enumeration<String> headerNames = httpRequest.getHeaderNames();
             while (headerNames.hasMoreElements()) {
                 String key = headerNames.nextElement();
                 String value = httpRequest.getHeader(key);
                 LOG.trace("    " + key + ": " + value);
             }
         }
     }
 
     private void logResponse(StringBuffer url, LavendelizeHttpServletResponse lavendelResponse) {
         if (LOG.isDebugEnabled()) {
             LOG.debug("Leaving doFilter:  url=" + url);
         }
         if (LOG.isTraceEnabled()) {
             LOG.trace("  Response headers: ");
             for (Entry<String, String> entry : lavendelResponse.getHeaders().entrySet()) {
                 LOG.trace("    " + entry.getKey() + ": " + entry.getValue());
             }
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void destroy() {
     }
 
     //--
 
 
     public void serve(Node file,  HttpServletResponse response) throws IOException {
         String contentType;
         long contentLength;
         ServletOutputStream out;
 
         setCacheExpireDate(response, 10);
         response.setDateHeader("Last-Modified", file.getLastModified());
         contentType = filterConfig.getServletContext().getMimeType(file.getName());
         if (contentType != null) {
             response.setContentType(contentType);
         }
         contentLength = file.length();
         if (contentLength >= Integer.MAX_VALUE) {
             throw new IOException("file to big: " + contentLength);
         }
         response.setContentLength((int) contentLength);
         out = response.getOutputStream();
         try {
             response.setBufferSize(4096);
         } catch (IllegalStateException e) {
             // Silent catch
         }
         file.writeTo(out);
     }
 
     private static void setCacheExpireDate(HttpServletResponse response, int years) {
         Calendar cal = Calendar.getInstance();
         long now = cal.getTimeInMillis();
         cal.roll(Calendar.YEAR, years);
         long seconds = (cal.getTimeInMillis() - now) / 1000;
         response.setHeader("Cache-Control", "PUBLIC, max-age=" + seconds + ", strict-revalidate");
         response.setHeader("Expires", EXPIRES_FORMAT.format(cal.getTime()));
     }
 
     private static final DateFormat EXPIRES_FORMAT = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
 
 }
