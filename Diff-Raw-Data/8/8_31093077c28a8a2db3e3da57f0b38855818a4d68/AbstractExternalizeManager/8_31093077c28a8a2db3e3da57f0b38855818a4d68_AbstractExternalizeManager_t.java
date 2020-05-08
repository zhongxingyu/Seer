 /*
  * Copyright 2000,2005 wingS development team.
  *
  * This file is part of wingS (http://wingsframework.org).
  *
  * wingS is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  *
  * Please see COPYING for the complete licence.
  */
 package org.wings.externalizer;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.wings.io.Device;
 import org.wings.util.StringUtil;
 import org.wings.resource.ResourceNotFoundException;
 
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.util.*;
 
 /**
  * @author <a href="mailto:haaf@mercatis.de">Armin Haaf</a>
  */
 public abstract class AbstractExternalizeManager {
     protected final static Log LOG = LogFactory.getLog(AbstractExternalizeManager.class);
 
     /**
      * The identifier generated, if the {@link ExternalizeManager} did not find
      * an apropriate {@link Externalizer}.
      */
     public static final String NOT_FOUND_IDENTIFIER = "0";
 
 
     /*---------------------------------------------------------------
      * The externalized ID is just a counter start starts with zero. This
      * happens with each start of the server, and thus generates the same
      * ID if we restart the application (especially, if we are in the
      * development phase). Since we externalize the resource with a long
      * caching timeout, the browser might not refetch a resource externalized
      * in a fresh instance of the web-application, since the browser has cached
      * it already.
      * Thus, we need a unique prefix for each externalized resource, that
      * changes with each start of the server.
      * These static variables create a new ID every UNIQUE_TIMESLICE, which
      * means, that, if we use a 2-character prefix, can offer the browser
      * the timeframe of FINAL_EXPIRES for this resource to be cached (since
      * after that time, we have an roll-over of the ID's).
      *----------------------------------------------------------------*/
 
     /**
      * in seconds
      */
     public final int UNIQUE_TIMESLICE = 20;
 
     /**
      * in seconds; Computed from UNIQUE_TIMESLICE; do not change.
      */
     public final long FINAL_EXPIRES =
             (StringUtil.MAX_RADIX * StringUtil.MAX_RADIX - 1) * UNIQUE_TIMESLICE;
 
     /**
      * Prefix for the externalized ID; long. Computed, do not change.
      */
     protected final long PREFIX_TIMESLICE =
             ((System.currentTimeMillis() / 1000) % FINAL_EXPIRES) / UNIQUE_TIMESLICE;
 
     // Flags
 
     /**
      * for an externalized object with the final flag on the expired date
      * header is set to a big value. If the final flag is off, the browser
      * or proxy does not cache the object.
      */
     public static final int FINAL = 8;
 
     /**
      * for an externalized object with the request flag on, the externalized
      * object is removed from the {@link ExternalizeManager} after one request
      * of the object.
      */
     public static final int REQUEST = 1;
 
     /**
      * for an externalized object with the session flag on, the externalized
      * object only available to requests within the session which created the
      * object. The object is not accessible anymore after the session is
      * destroyed (it is garbage collected after the session is garbage
      * collected)
      */
     public static final int SESSION = 2;
 
     /**
      * for an externalized object with the gobal flag on, the externalized
      * object is available to all requests. Also it is never garbage collected
      * and available for the lifecycle of the servlet container.
      */
     public static final int GLOBAL = 4;
 
     /**
      * To generate the identifier for a externalized object.
      */
     private long counter = 0;
 
     /**
      * To search for an already externalized object. This performs way better
      * than search in the value set of the
      * identifier-{@link ExternalizedResource} map.
      */
     protected final Map<ExternalizedResource,String> reverseExternalized;
 
     /**
      * To support Session local externalizing, the {@link ExternalizeManager}
      * needs to encode the session identifier of the servlet container in the
      * URL of the externalized object. This is set in the constructor
      * and should work (I hope so) with all servlet containers.
      */
     protected String sessionEncoding = "";
 
     /**
      * String prefixed to every created externlizer identifier via {@link #createIdentifier()}
      */
     private String prefix;
     private static final String FOO = "http://foo/foo";
 
 
     public AbstractExternalizeManager() {
         if (LOG.isDebugEnabled()) {
             LOG.debug("Externalizer scope using prefix" + prefix + "expires in " + FINAL_EXPIRES + " seconds ");
         }
 
         reverseExternalized = Collections.synchronizedMap(new HashMap<ExternalizedResource, String>());
         setPrefix(StringUtil.toShortestAlphaNumericString(PREFIX_TIMESLICE, 2));
     }
 
     public void setResponse(HttpServletResponse response) {
         if (response != null) {
             sessionEncoding = response.encodeURL(FOO).substring(FOO.length());
         }
     }
 
     protected final synchronized long getNextIdentifier() {
         return ++counter;
     }
 
     /**
      * String prefixed to every created externlizer identifier via {@link #createIdentifier()}
      */
     public String getPrefix() {
         return prefix;
     }
 
     /**
      * String prefixed to every created externlizer identifier via {@link #createIdentifier()}
      */
     public void setPrefix(final String prefix) {
         if (LOG.isDebugEnabled())
             LOG.debug("Externalizer prefix changed from "+this.prefix + " to "+prefix);
         this.prefix = prefix;
     }
 
 
     protected final String createIdentifier() {
         return getPrefix() + StringUtil.toShortestAlphaNumericString(getNextIdentifier());
     }
 
     /**
      * store the {@link ExternalizedResource} in a map.
      * The {@link ExternalizedResource} should later on accessible by the
      * identifier {@link #getExternalizedResource}, {@link #removeExternalizedResource}
      */
     protected abstract void storeExternalizedResource(String identifier,
                                                       ExternalizedResource extInfo);
 
     /**
      * get the {@link ExternalizedResource} by identifier.
      *
      * @return null, if not found!!
      */
     public abstract ExternalizedResource getExternalizedResource(String identifier);
 
     /**
      * removes the {@link ExternalizedResource} by identifier.
      */
     public abstract void removeExternalizedResource(String identifier);
 
     /**
      * externalizes (make a java object available for a browser) an object with
      * the given {@link Externalizer}. The object is externalized in the
      * {@link #SESSION} scope.
      *
      * @return a URL for accessing the object relative to the base URL.
      */
     public String externalize(Object obj, Externalizer externalizer) {
         return externalize(obj, externalizer, SESSION);
     }
 
     /**
      * externalizes (make a java object available for a browser) an object with
      * the given {@link Externalizer}. If the given headers are !=null the
      * headers overwrite the headers from the {@link Externalizer}.
      * The object is externalized in the
      * {@link #SESSION} scope.
      *
      * @return a URL for accessing the object relative to the base URL.
      */
     public String externalize(Object obj, Externalizer externalizer, Collection headers) {
         return externalize(obj, externalizer, headers, SESSION);
     }
 
     /**
      * externalizes (make a java object available for a browser) an object with
      * the given {@link Externalizer}. Valid flags are (this may change, look
      * also in the static variable section)
      * <ul>
      * <li>{@link #FINAL}</li>
      * <li>{@link #REQUEST}</li>
      * <li>{@link #SESSION}</li>
      * <li>{@link #GLOBAL}</li>
      * </ul>
      *
      * @return a URL for accessing the object relative to the base URL.
      */
     public String externalize(Object obj, Externalizer externalizer, int flags) {
         if (obj == null || externalizer == null)
             throw new IllegalStateException("no externalizer");
 
         return externalize(obj, externalizer, null, null, flags);
     }
 
     /**
      * externalizes (make a java object available for a browser) an object with
      * the given {@link Externalizer}. If the given headers are !=null the
      * headers overwrite the headers from the {@link Externalizer}.
      * Valid flags are (this may change, look
      * also in the static variable section)
      * <ul>
      * <li>{@link #FINAL}</li>
      * <li>{@link #REQUEST}</li>
      * <li>{@link #SESSION}</li>
      * <li>{@link #GLOBAL}</li>
      * </ul>
      *
      * @return a URL for accessing the object relative to the base URL.
      */
     public String externalize(Object obj, Externalizer externalizer,
                               Collection headers, int flags) {
         if (obj == null || externalizer == null)
             throw new IllegalStateException("no externalizer");
 
         return externalize(obj, externalizer, null, headers, flags);
     }
 
     /**
      * externalizes (make a java object available for a browser) an object with
      * the given {@link Externalizer}.
      * If the mimeType!=null, mimeType overwrites the mimeType of the
      * {@link Externalizer}.
      * The object is externalized in the
      * {@link #SESSION} scope.
      *
      * @return a URL for accessing the object relative to the base URL.
      */
     public String externalize(Object obj, Externalizer externalizer,
                               String mimeType) {
         return externalize(obj, externalizer, mimeType, null, SESSION);
     }
 
     /**
      * externalizes (make a java object available for a browser) an object with
      * the given {@link Externalizer}.
      * If the mimeType!=null, mimeType overwrites the mimeType of the
      * {@link Externalizer}.
      * If the given headers are !=null the
      * headers overwrite the headers from the {@link Externalizer}.
      *
      * @return a URL for accessing the object relative to the base URL.
      */
     public String externalize(Object obj, Externalizer externalizer,
                               String mimeType, Collection headers) {
         return externalize(obj, externalizer, mimeType, headers, SESSION);
     }
 
     /**
      * externalizes (make a java object available for a browser) an object with
      * the given {@link Externalizer}.
      * If the mimeType!=null, mimeType overwrites the mimeType of the
      * {@link Externalizer}.
      * If the given headers are !=null the
      * headers overwrite the headers from the {@link Externalizer}.
      * Valid flags are (this may change, look
      * also in the static variable section)
      * <ul>
      * <li>{@link #FINAL}</li>
      * <li>{@link #REQUEST}</li>
      * <li>{@link #SESSION}</li>
      * <li>{@link #GLOBAL}</li>
      * </ul>
      *
      * @return a URL for accessing the object relative to the base URL.
      */
     public String externalize(Object obj, Externalizer externalizer,
                               String mimeType, Collection headers, int flags) {
         if (externalizer == null) {
             throw new IllegalStateException("no externalizer");
         }
         ExternalizedResource extInfo = new ExternalizedResource(obj, externalizer, mimeType, headers, flags);
 
         extInfo.setId(externalizer.getId(obj));
         if ((flags & GLOBAL) > 0) {
             // session encoding is not necessary here
             return SystemExternalizeManager.getSharedInstance().externalize(extInfo);
         } else {
             return externalize(extInfo);
         }
     }
 
 
     /**
      * externalizes (make a java object available for a browser) the object in
      * extInfo.
      *
      * @return a URL for accessing the externalized object relative to the base URL.
      */
     public String externalize(ExternalizedResource extInfo) {
         String identifier = (String) reverseExternalized.get(extInfo);
 
         if (identifier == null) {
             identifier = extInfo.getId();
            if (identifier != null) {
                if (this == SystemExternalizeManager.getSharedInstance()) {
                    identifier = "-" + identifier;
                } else {
                    // do nothing
                }
             } else {
                 identifier = createIdentifier();
             }
 
             String extension = extInfo.getExtension();
             if (extension != null)
                 identifier += ("." + extension);
 
             extInfo.setId(identifier);
             storeExternalizedResource(identifier, extInfo);
             reverseExternalized.put(extInfo, identifier);
         }
 
         return identifier + sessionEncoding;
     }
 
     /**
      * externalizes (make a java object available for a browser) the object in
      * extInfo.
      *
      * @return a URL for accessing the externalized object relative to the base URL.
      */
     public String getId(String url) {
         if (url == null || url.length() == 0) {
             return url;
         }
         String result;
         if (url.charAt(0) == '-') {
             result = url;
         } else {
             result = url.substring(0, url.length() - sessionEncoding.length());
         }
         return result;
     }
 
 
     /**
      * delivers a externalized object identfied with the given identifier to a
      * client.
      * It sends an error (404), if the identifier is not registered.
      */
     public void deliver(String identifier, HttpServletResponse response,
                         Device out) throws IOException {
         ExternalizedResource extInfo = getExternalizedResource(identifier);
 
         if (extInfo == null) {
             LOG.warn("identifier " + identifier + " not found");
             response.sendError(HttpServletResponse.SC_NOT_FOUND);
             return;
         }
         deliver(extInfo, response, out);
     }
 
     public void deliver(ExternalizedResource extInfo, HttpServletResponse response,
                         Device out)
             throws IOException {
         /* FIXME: re-implement.
         if ( extInfo.deliverOnce() ) {
             removeExternalizedResource(identifier);
         }
         */
 
         if (extInfo.getMimeType() != null) {
             response.setContentType(extInfo.getMimeType());
         }
 
         // FIXME find out, if this is correct: if the content length
         // is not size preserving (like a gzip-device), then we must not
         // send the content size we know..
         if (out.isSizePreserving()) {
             int resourceLen = extInfo
                     .getExternalizer().getLength(extInfo.getObject());
             if (resourceLen > 0) {
                 LOG.debug(extInfo.getMimeType() + ": " + resourceLen);
                 response.setContentLength(resourceLen);
             }
         }
 
 
         Collection headers = extInfo.getHeaders();
         if (headers != null) {
             for (Object header : headers) {
                 Map.Entry entry = (Map.Entry) header;
                 if (entry.getValue() instanceof String) {
                     response.addHeader((String) entry.getKey(),
                             (String) entry.getValue());
                 } else if (entry.getValue() instanceof Date) {
                     response.addDateHeader((String) entry.getKey(),
                             ((Date) entry.getValue()).getTime());
 
                 } else if (entry.getValue() instanceof Integer) {
                     response.addIntHeader((String) entry.getKey(),
                             ((Integer) entry.getValue()).intValue());
 
                 } // end of if ()
             }
         }
 
         if (!response.containsHeader("Expires")) {
             /*
             * This would be the correct way to do it; alas, that means, that
             * for static resources, after a day or so, no caching could take
             * place, since the last modification was at the first time, the
             * resource was externalized (since it doesn't change).
             * .. have to think about it.
             */
             //response.setDateHeader("Expires",
             //                      (1000*FINAL_EXPIRES)
             //                       + extInfo.getLastModified());
             // .. so do this for now, which is the best approximation of what
             // we want.
             response.setDateHeader("Expires",
                     System.currentTimeMillis()
                     + (1000 * FINAL_EXPIRES));
         }
 
         try {
             extInfo.getExternalizer().write(extInfo.getObject(), out);
         } catch (ResourceNotFoundException e) {
             LOG.debug("Unable to deliver resource due to:  " + e.getMessage()+". Sending 404!");
             response.reset();
             response.sendError(404, e.getMessage());
         }
         out.flush();
     }
 
     public void clear() {
         reverseExternalized.clear();
     }
 }
 
 
