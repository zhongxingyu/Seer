 /*******************************************************************************
  * Copyright (c) 2008, 2009 SOPERA GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     SOPERA GmbH - initial API and implementation
  *******************************************************************************/
 package org.eclipse.swordfish.core.test.util.base;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Pattern;
 
 import org.junit.Ignore;
 import org.springframework.core.io.FileSystemResource;
 import org.springframework.core.io.InputStreamResource;
 import org.springframework.core.io.Resource;
 import org.springframework.util.Assert;
 @Ignore
 public class TargetPlatformOsgiTestCase extends BaseOsgiTestCase {
     public static final String TARGET_PLATFORM_SYS_PORPERTY = "swordfishTargetPlatform";
     private static Map<String, Integer> bundlePriorities;
     static {
         bundlePriorities = new HashMap<String, Integer>();
         bundlePriorities.put("common-3.3", 2);
         bundlePriorities.put("org.eclipse.osgi", 2);
         bundlePriorities.put("org.eclipse.equinox", 2);
         bundlePriorities.put("servicemix.transaction", 2);
         bundlePriorities.put("geronimo-jta", 2);
         bundlePriorities.put("spring-osgi-extender", 1);
         bundlePriorities.put("org.eclipse.swordfish.core.configuration", 1);
        bundlePriorities.put("org.eclipse.swordfish.core.configuration", 1);
         bundlePriorities.put("org.apache.servicemix.kernel.management", 1);
         bundlePriorities.put("jaas", -1);
         bundlePriorities.put("servicemix.kernel.filemonitor", -5);
         bundlePriorities.put("servicemix.transaction", -4);
         bundlePriorities.put("servicemix.kernel.filemonitor", -5);
         bundlePriorities.put("cxfendpoint", -6);
         bundlePriorities.put("samples.http", -7);
     }
 
     @Override
     protected Resource getTestingFrameworkBundlesConfiguration() {
         try {
             return new InputStreamResource(
                     TargetPlatformOsgiTestCase.class.getClassLoader().getResource("boot-bundles.properties").openStream());
         } catch (IOException ex) {
             throw new IllegalStateException(ex);
         }
     }
     private String getTargetPlatformPath() {
         String ret = null;
         ret = System.getProperty(TARGET_PLATFORM_SYS_PORPERTY);
         try {
         if (ret == null) {
             File dir = new File(getClass().getClassLoader().getResource(".").getPath());
             dir = dir.getParentFile().getParentFile().getParentFile().getParentFile();
             String path = dir.getPath() + "/org.eclipse.swordfish.bundles/target/bundles/";
             dir = new File(path);
             if (dir.exists() && dir.isDirectory()) {
                 ret = dir.getPath();
             }
         }
         } catch (Exception ex) {
             LOG.error(ex.getMessage(), ex);
         }
         return ret;
     }
 
     protected List<Pattern> getExcludeBundlePatterns() {
        return Arrays.asList(Pattern.compile("org.eclipse.osgi-3.4.2.*"), Pattern.compile("org.eclipse.swordfish.samples.http.*"));
     }
     private int getIndex(Resource[] bundles, String bundleNamePart) {
         int i = -1;
         for (Resource resource : bundles) {
             i++;
             if (resource.getFilename().contains(bundleNamePart)) {
                 return i;
             }
         }
         return i;
     }
     private <T> void swap(T[] arr, int fisrtIndex, int secondIndex) {
             T temp = arr[fisrtIndex];
             arr[fisrtIndex] = arr[secondIndex];
             arr[secondIndex] = temp;
 
     }
     private int getPriority(Resource bundle) {
         for (String key : bundlePriorities.keySet()) {
             if (bundle.getFilename().contains(key)) {
                 return bundlePriorities.get(key);
             }
         }
         return 0;
     }
 
     private void adjustBundleOrder(Resource[] bundles) {
         Arrays.sort(bundles, new Comparator<Resource>() {
             public int compare(Resource resource1, Resource resource2) {
                 return getPriority(resource2) - getPriority(resource1);
             }
         });
     }
 
 
     @Override
     protected Resource[] getTestBundles() {
         String targetPlatformPath = getTargetPlatformPath();
         Assert.notNull(targetPlatformPath, "Either system property [" + TARGET_PLATFORM_SYS_PORPERTY +
                 "] should be set or there should be the org.eclipse.swordfish.bundles project containing bundles " +
                 "needed to launch the Swordfish env");
         List<Resource> bundles = new ArrayList<Resource>();
         bundles.add(BaseMavenOsgiTestCase.getMavenRepositoryBundle("org.eclipse.swordfish", "org.eclipse.swordfish.core.test.util"));
         List<Pattern> excludePatterns = getExcludeBundlePatterns();
         boolean exclude;
         for (File bundle : new File(targetPlatformPath).listFiles()) {
             exclude = false;
             for (Pattern pattern : excludePatterns) {
                 if (pattern.matcher(bundle.getName()).matches()) {
                     exclude = true;
                 }
             }
             if (!exclude && bundle.isFile() && bundle.getName().endsWith("jar")) {
                 bundles.add(new FileSystemResource(bundle));
             }
         }
         /*Because of the strange behavior of the spring osgi
          *test framework we need to load org.eclipse.swordfish.core.configuration and org.eclipse.swordfish.core.event bundles
          *before org.eclipse.swordfish.core
          *TODO: Could anyone propose any less uglier solution  */
         Resource[] bundleArray = bundles.toArray(new Resource[bundles.size()]);
         adjustBundleOrder(bundleArray);
         for (Resource bundle : bundleArray) {
             System.out.println(bundle.getFilename());
         }
         return bundleArray;
     }
 
 }
