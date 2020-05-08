 /*
  * CDDL HEADER START
  *
  * The contents of this file are subject to the terms of the
  * Common Development and Distribution License, Version 1.0 only
  * (the "License").  You may not use this file except in compliance
  * with the License.
  *
  * You can obtain a copy of the license at license/ESCIDOC.LICENSE
  * or http://www.escidoc.de/license.
  * See the License for the specific language governing permissions
  * and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL HEADER in each
  * file and include the License file at license/ESCIDOC.LICENSE.
  * If applicable, add the following below this CDDL HEADER, with the
  * fields enclosed by brackets "[]" replaced with your own identifying
  * information: Portions Copyright [yyyy] [name of copyright owner]
  *
  * CDDL HEADER END
  */
 
 /*
  * Copyright 2006-2008 Fachinformationszentrum Karlsruhe Gesellschaft
  * fuer wissenschaftlich-technische Information mbH and Max-Planck-
  * Gesellschaft zur Foerderung der Wissenschaft e.V.  
  * All rights reserved.  Use is subject to license terms.
  */
 package de.escidoc.core.om.business.indexer;
 
 import java.io.ByteArrayInputStream;
 import java.lang.reflect.InvocationTargetException;
 import java.net.URL;
 
 import org.apache.http.Header;
 import org.apache.http.HttpResponse;
 import org.esidoc.core.utils.io.EscidocBinaryContent;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Service;
 
 import com.googlecode.ehcache.annotations.Cacheable;
 import com.googlecode.ehcache.annotations.KeyGenerator;
 import com.googlecode.ehcache.annotations.PartialCacheKey;
 import com.googlecode.ehcache.annotations.Property;
 import com.googlecode.ehcache.annotations.TriggersRemove;
 
 import de.escidoc.core.common.business.fedora.TripleStoreUtility;
 import de.escidoc.core.common.exceptions.system.SystemException;
 import de.escidoc.core.common.servlet.invocation.BeanMethod;
 import de.escidoc.core.common.servlet.invocation.MethodMapper;
 import de.escidoc.core.common.servlet.invocation.exceptions.MethodNotFoundException;
 import de.escidoc.core.common.util.service.ConnectionUtility;
 import de.escidoc.core.common.util.xml.XmlUtility;
 import de.escidoc.core.common.util.xml.factory.FoXmlProviderConstants;
 
 /**
  * @author Michael Hoppe
  *         <p/>
  *         Class gets resources for indexer, either from eSciDocCore-Framework or from external URL. Caches resources in
  *         resourcesCache.
  */
 @Service
 public class IndexerResourceRequester {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(IndexerResourceRequester.class);
 
     @Autowired
     @Qualifier("common.CommonMethodMapper")
     private MethodMapper methodMapper;
 
     @Autowired
     @Qualifier("business.TripleStoreUtility")
     private TripleStoreUtility tripleStoreUtility;
 
     @Autowired
     @Qualifier("escidoc.core.common.util.service.ConnectionUtility")
     private ConnectionUtility connectionUtility;
 
     /**
      * Protected constructor to prevent instantiation outside of the Spring-context.
      */
     protected IndexerResourceRequester() {
     }
 
     /**
      * Get resource with given identifier.
      *
      * @param identifier identifier
      * @return EscidocBinaryContent resource-object
      * @throws SystemException e
      */
     @Cacheable(cacheName = "resourcesCache", keyGenerator = @KeyGenerator(name = "StringCacheKeyGenerator", properties = { @Property(name = "includeMethod", value = "false") }))
     public EscidocBinaryContent getResource(final String identifier) throws SystemException {
         final String href = getHref(identifier);
         if (identifier.startsWith("http")) {
             return getExternalResource(href);
         }
         else {
             return getInternalResource(href);
         }
     }
 
     /**
      * Get resource with given identifier.
      *
      * @param identifier identifier
      * @return EscidocBinaryContent resource-object
      * @throws SystemException e
      */
     public EscidocBinaryContent getResourceUncached(final String identifier) throws SystemException {
         final String href = getHref(identifier);
         if (identifier.startsWith("http")) {
             return getExternalResource(href);
         }
         else {
             return getInternalResource(href);
         }
     }
 
     /**
      * Set resource with given identifier in cache.
      *
      * @param identifier identifier
      * @param resource   resource-object
      * @return Object resource-object
      */
     @Cacheable(cacheName = "resourcesCache", keyGenerator = @KeyGenerator(name = "StringCacheKeyGenerator", properties = { @Property(name = "includeMethod", value = "false") }))
    public EscidocBinaryContent setResource(@PartialCacheKey
    final String identifier, final EscidocBinaryContent resource) {
         return resource;
     }
 
     /**
      * delete resource with given identifier from cache.
      *
      * @param identifier identifier
      */
     @TriggersRemove(cacheName = "resourcesCache", keyGenerator = @KeyGenerator(name = "StringCacheKeyGenerator", properties = { @Property(name = "includeMethod", value = "false") }))
     public void deleteResource(final String identifier) {
     }
 
     /**
      * get resource with given identifier from framework.
      *
      * @param identifier identifier
      * @throws SystemException e
      */
     private EscidocBinaryContent getInternalResource(final String identifier) throws SystemException {
         try {
             final BeanMethod method = methodMapper.getMethod(identifier, null, null, "GET", "");
             final Object content = method.invokeWithProtocol(null);
             if (content != null && "EscidocBinaryContent".equals(content.getClass().getSimpleName())) {
                 return (EscidocBinaryContent) content;
             }
             else if (content != null && "String".equals(content.getClass().getSimpleName())) {
                 final EscidocBinaryContent escidocBinaryContent = new EscidocBinaryContent();
                 escidocBinaryContent.setMimeType(XmlUtility.MIME_TYPE_XML);
                 escidocBinaryContent.setContent(new ByteArrayInputStream(((String) content)
                     .getBytes(XmlUtility.CHARACTER_ENCODING)));
                 return escidocBinaryContent;
             }
             else {
                 throw new SystemException("wrong object-type");
             }
         }
         catch (final InvocationTargetException e) {
             if (!"AuthorizationException".equals(e.getTargetException().getClass().getSimpleName())
                 && !"InvalidStatusException".equals(e.getTargetException().getClass().getSimpleName())) {
                 throw new SystemException(e);
             }
         }
         catch (final MethodNotFoundException e) {
             if (LOGGER.isWarnEnabled()) {
                 LOGGER.warn("Error on caching internal resource.");
             }
             if (LOGGER.isDebugEnabled()) {
                 LOGGER.debug("Error on caching internal resource.", e);
             }
         }
         catch (final Exception e) {
             throw new SystemException(e);
         }
         return null;
     }
 
     /**
      * get resource from given URL.
      *
      * @param identifier identifier
      * @throws SystemException e
      */
     private EscidocBinaryContent getExternalResource(final String identifier) {
         try {
             final HttpResponse httpResponse = connectionUtility.getRequestURL(new URL(identifier));
 
             if (httpResponse != null) {
 
                 // TODO testen ob header mitgeschickt wird
                 final Header ctype = httpResponse.getFirstHeader("Content-Type");
                 final String mimeType =
                     ctype != null ? ctype.getValue() : FoXmlProviderConstants.MIME_TYPE_APPLICATION_OCTET_STREAM;
 
                 final EscidocBinaryContent escidocBinaryContent = new EscidocBinaryContent();
                 escidocBinaryContent.setMimeType(mimeType);
                 escidocBinaryContent.setContent(httpResponse.getEntity().getContent());
                 return escidocBinaryContent;
             }
         }
         catch (final Exception e) {
             if (LOGGER.isWarnEnabled()) {
                 LOGGER.warn("Error on caching external resource.");
             }
             if (LOGGER.isDebugEnabled()) {
                 LOGGER.debug("Error on caching external resource.", e);
             }
         }
         return null;
     }
 
     /**
      * generate href out of pid.
      *
      * @param identifier identifier
      * @return String href
      * @throws SystemException e
      */
     private String getHref(final String identifier) throws SystemException {
         String href = identifier;
         if (!href.contains("/")) {
             // objectId provided, generate href
             // get object-type
             href = XmlUtility.getObjidWithoutVersion(href);
             final String objectType = tripleStoreUtility.getObjectType(href);
             if (objectType == null) {
                 throw new SystemException("couldnt get objectType for object " + href);
             }
 
             href = this.tripleStoreUtility.getHref(objectType, identifier);
         }
         if (!href.startsWith("http") && !href.startsWith("/")) {
             href = '/' + href;
         }
         return href;
     }
 }
