 /*******************************************************************************
  * Copyright (c) 2012 SAP AG
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   SAP AG - initial contribution
  *******************************************************************************/
 
 package org.eclipse.virgo.web.enterprise.services.accessor;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.virgo.util.osgi.manifest.VersionRange;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleEvent;
 import org.osgi.util.tracker.BundleTrackerCustomizer;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 class WebAppBundleTrackerCustomizer implements BundleTrackerCustomizer<String> {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(WebAppBundleTrackerCustomizer.class);
 
     static final String API_BUNDLES = "api.bundles";
 
     static final String IMPL_BUNDLES = "impl.bundles";
 
     private static final String COMMA_SEPARATOR = ",";
 
     private static final String SEMICOLON_SEPARATOR = ";";
 
     private static final String VERSION_SEPARATOR = "=";
 
     static final String HEADER_EXPOSED_CONTENT_TYPE = "Exposed-ContentType";
 
     static final String HEADER_EXPOSED_CONTENT_TYPE_API_VALUE = "API";
 
     static final String HEADER_EXPOSED_CONTENT_TYPE_IMPL_VALUE = "Implementation";
 
     static final String HEADER_EXPOSE_ADDITIONAL_API = "Expose-AdditionalAPI";
 
     private static final char INCLUSIVE_UPPER = ']';
 
     private static final char EXCLUSIVE_UPPER = ')';
 
     private final WebAppBundleClassLoaderDelegateHook wabClassLoaderDelegateHook;
 
     private final Map<String, VersionRange> exposeAdditionalApiBundles = new HashMap<String, VersionRange>();
 
     private Map<String, List<Bundle>> bundlesWithSameBSNMap;
 
     private boolean initialized = false;
 
     // move these values in config ini
     private final List<String> bundleNamesForJarScanner;
 
     // = Arrays.asList(new String[] {"com.springsource.javax.servlet.jsp.jstl", "org.glassfish.com.sun.faces"});
 
     private final Set<Bundle> bundlesForJarScanner = new HashSet<Bundle>();
 
     /**
      * @deprecated Expose-AdditionalAPI header should be used instead of this
      */
     @Deprecated
     private final Map<String, VersionRange> apiBundles;
 
     /**
      * @deprecated Not supported any more
      */
     @Deprecated
     private final Map<String, VersionRange> implBundles;
 
     public WebAppBundleTrackerCustomizer(WebAppBundleClassLoaderDelegateHook wabClassLoaderDelegateHook) {
         String bundlesForJarScanner = System.getProperty("org.eclipse.virgo.jarscanner.bundles");
         if (bundlesForJarScanner != null) {
             this.bundleNamesForJarScanner = Arrays.asList(bundlesForJarScanner.split(","));
         } else {
             this.bundleNamesForJarScanner = new ArrayList<String>();
         }
         this.wabClassLoaderDelegateHook = wabClassLoaderDelegateHook;
         this.apiBundles = Collections.unmodifiableMap(getBundles(System.getProperty(API_BUNDLES)));
         this.implBundles = Collections.unmodifiableMap(getBundles(System.getProperty(IMPL_BUNDLES)));
         if (LOGGER.isDebugEnabled()) {
             LOGGER.debug("Predefined api bundles added to the tracker " + this.apiBundles);
             LOGGER.debug("Predefined impl bundles added to the tracker " + this.implBundles);
         }
     }
 
     @Override
     public String addingBundle(Bundle bundle, BundleEvent event) {
         if (this.bundleNamesForJarScanner.contains(bundle.getSymbolicName())) {
             this.bundlesForJarScanner.add(bundle);
         }
 
         if (isApiBundle(bundle)) {
             this.wabClassLoaderDelegateHook.addApiBundle(bundle);
         }
 
         if (isImplBundle(bundle)) {
             this.wabClassLoaderDelegateHook.addImplBundle(bundle);
         }
 
         processExposeAdditionalAPIHeader(bundle);
 
         return bundle.getSymbolicName();
     }
 
     @Override
     public void modifiedBundle(Bundle bundle, BundleEvent event, String symbolicName) {
         // no-op
     }
 
     @Override
     public void removedBundle(Bundle bundle, BundleEvent event, String symbolicName) {
         this.wabClassLoaderDelegateHook.removeApiBundle(bundle);
         this.wabClassLoaderDelegateHook.removeImplBundle(bundle);
         this.wabClassLoaderDelegateHook.removeWebAppBundle(bundle);
         this.bundlesForJarScanner.remove(bundle);
     }
 
     // TODO more fine tuned synchronization needed
     synchronized void processAdditionalAPIBundles(Bundle[] allBundles) {
         if (allBundles == null || allBundles.length == 0 || this.exposeAdditionalApiBundles == null || this.exposeAdditionalApiBundles.size() == 0) {
             return;
         }
 
         if (!this.initialized) {
             this.bundlesWithSameBSNMap = getBundleMap(allBundles);
             Set<String> keys = this.exposeAdditionalApiBundles.keySet();
             for (String key : keys) {
                 List<Bundle> bundlesWithSameBSN = this.bundlesWithSameBSNMap.get(key);
                 if (bundlesWithSameBSN == null || bundlesWithSameBSN.size() == 0) {
                     if (LOGGER.isErrorEnabled()) {
                         LOGGER.error("Bundle with symbolic name [" + key + "] is marked as additional API."
                             + "Such bundle is not installed on the system and cannot be added to the application classloader."
                             + "This may cause a severe problem during runtime.");
                     }
                 } else {
                     Bundle apiBundle = null;
                     for (int i = 0; i < bundlesWithSameBSN.size(); i++) {
                         VersionRange range = this.exposeAdditionalApiBundles.get(key);
                         Bundle bundle = bundlesWithSameBSN.get(i);
                         if (!range.includes(bundle.getVersion())) {
                             continue;
                         }
 
                         if (apiBundle == null || apiBundle.getVersion().compareTo(bundle.getVersion()) < 0) {
                             apiBundle = bundle;
                         }
                     }
                     if (apiBundle != null) {
                         this.wabClassLoaderDelegateHook.addApiBundle(apiBundle);
                     }
                 }
             }
             this.initialized = true;
         }
     }
 
     Map<String, List<Bundle>> getBundlesWithSameBSNMap() {
         return this.bundlesWithSameBSNMap;
     }
 
     Map<String, VersionRange> getExposeAdditionalApiBundles() {
         return this.exposeAdditionalApiBundles;
     }
 
     private Map<String, List<Bundle>> getBundleMap(Bundle[] allBundles) {
         Map<String, List<Bundle>> result = new HashMap<String, List<Bundle>>();
         for (Bundle bundle : allBundles) {
             String bsn = bundle.getSymbolicName();
             List<Bundle> bundlesWithSameBSN = result.get(bsn);
             if (bundlesWithSameBSN == null) {
                 bundlesWithSameBSN = new ArrayList<Bundle>();
             }
             bundlesWithSameBSN.add(bundle);
             result.put(bsn, bundlesWithSameBSN);
         }
         return result;
     }
 
     private boolean isApiBundle(Bundle bundle) {
         String headerValue = getHeaderValue(bundle, HEADER_EXPOSED_CONTENT_TYPE);
         if (HEADER_EXPOSED_CONTENT_TYPE_API_VALUE.equals(headerValue)) {
             return true;
         }
 
         VersionRange versionRange = this.apiBundles.get(bundle.getSymbolicName());
         if (versionRange != null && versionRange.includes(bundle.getVersion())) {
             return true;
         }
        
        versionRange = this.exposeAdditionalApiBundles.get(bundle.getSymbolicName());
        if (versionRange != null && versionRange.includes(bundle.getVersion())) {
            return true;
        }
 
         return false;
     }
 
     private boolean isImplBundle(Bundle bundle) {
         String headerValue = getHeaderValue(bundle, HEADER_EXPOSED_CONTENT_TYPE);
         if (HEADER_EXPOSED_CONTENT_TYPE_IMPL_VALUE.equals(headerValue)) {
             return true;
         }
 
         VersionRange versionRange = this.implBundles.get(bundle.getSymbolicName());
         if (versionRange != null && versionRange.includes(bundle.getVersion())) {
             return true;
         }
 
         return false;
     }
 
     // TODO more fine tuned synchronization needed
     private synchronized void processExposeAdditionalAPIHeader(Bundle bundle) {
         String headerValue = getHeaderValue(bundle, HEADER_EXPOSE_ADDITIONAL_API);
         if (headerValue != null && !headerValue.equals("")) {
             Map<String, VersionRange> additionalApiBundles = getBundles(headerValue);
             if (additionalApiBundles != null) {
                 Set<String> keys = additionalApiBundles.keySet();
                 for (String key : keys) {
                     VersionRange oldVersionRange = this.exposeAdditionalApiBundles.get(key);
                     VersionRange newVersionRange = additionalApiBundles.get(key);
                     if (oldVersionRange != null) {
                         this.exposeAdditionalApiBundles.put(key, VersionRange.intersection(oldVersionRange, newVersionRange));
                         if (LOGGER.isDebugEnabled()) {
                             LOGGER.debug("Expose additional API bundle with BSN [" + key + "] and merge old version [" + oldVersionRange
                                 + "] with the new one [" + newVersionRange + "]");
                         }
                     } else {
                         this.exposeAdditionalApiBundles.put(key, additionalApiBundles.get(key));
                         if (LOGGER.isDebugEnabled()) {
                             LOGGER.debug("Expose additional API bundle with BSN [" + key + "] and version [" + newVersionRange + "].");
                         }
                     }
                 }
             }
         }
     }
 
     /**
      * The format of the property is BSN;bundle-version=Version|RANGE,BSN;bundle-version=Version|RANGE,... Where BSN
      * stands for bundle symbolic name.
      * 
      * @param property
      * @return
      */
     private Map<String, VersionRange> getBundles(String property) {
         Map<String, VersionRange> bundles = new HashMap<String, VersionRange>();
 
         if (property != null) {
             final List<String> bundleNames = parse(property);
             if (bundleNames.size() > 0) {
                 for (String bundleName : bundleNames) {
                     final String[] parts = bundleName.split(SEMICOLON_SEPARATOR);
                     if (parts == null || parts.length != 2) {
                         continue;
                     }
 
                     final String symbolicName = parts[0];
 
                     final String[] versionParts = parts[1].split(VERSION_SEPARATOR);
 
                     if (versionParts == null || versionParts.length != 2) {
                         continue;
                     }
 
                     final VersionRange bundleVersion = new VersionRange(versionParts[1]);
 
                     bundles.put(symbolicName, bundleVersion);
                 }
             }
         }
 
         return bundles;
     }
 
     private List<String> parse(String property) {
         List<String> result = new ArrayList<String>();
         int ind = property.indexOf(COMMA_SEPARATOR);
         if (ind >= 0) {
             int nextComma = property.indexOf(COMMA_SEPARATOR, ind + 1);
             if (nextComma >= 0) {
                 char prevChar = property.charAt(nextComma - 1);
                 if (prevChar == EXCLUSIVE_UPPER || prevChar == INCLUSIVE_UPPER) {
                     result.add(property.substring(0, nextComma).trim());
                     result.addAll(parse(property.substring(nextComma + 1)));
                 } else {
                     result.add(property.substring(0, ind).trim());
                     result.addAll(parse(property.substring(ind + 1)));
                 }
             } else {
                 int lastIndex = property.charAt(property.length() - 1);
                 if (lastIndex == EXCLUSIVE_UPPER || lastIndex == INCLUSIVE_UPPER) {
                     result.add(property.trim());
                 } else {
                     result.add(property.substring(0, ind).trim());
                     result.add(property.substring(ind + 1).trim());
                 }
             }
         } else {
             result.add(property);
         }
         return result;
     }
 
     private String getHeaderValue(Bundle bundle, String headerName) {
         return bundle.getHeaders().get(headerName);
     }
 
     Set<Bundle> getBundlesForJarScanner() {
         return this.bundlesForJarScanner;
     }
 
 }
