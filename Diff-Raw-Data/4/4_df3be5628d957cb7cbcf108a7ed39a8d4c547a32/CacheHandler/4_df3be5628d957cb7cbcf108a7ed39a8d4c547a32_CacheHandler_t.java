 /*
  * <License>
  * The Apache Software License
  *
  * Copyright (c) 2002 lenya. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification,
  * are permitted provided that the following conditions are met:
  *
  * 1. Redistributions of source code must retain the above copyright notice, this
  *    list of conditions and the following disclaimer.
  *
  * 2. Redistributions in binary form must reproduce the above copyright notice, this
  *    list of conditions and the following disclaimer in the documentation and/or
  *    other materials provided with the distribution.
  *
  * 3. All advertising materials mentioning features or use of this software must
  *    display the following acknowledgment: "This product includes software developed
  *    by lenya (http://www.lenya.org)"
  *
  * 4. The name "lenya" must not be used to endorse or promote products derived from
  *    this software without prior written permission. For written permission, please
  *    contact contact@lenya.org
  *
  * 5. Products derived from this software may not be called "lenya" nor may "lenya"
  *    appear in their names without prior written permission of lenya.
  *
  * 6. Redistributions of any form whatsoever must retain the following acknowledgment:
  *    "This product includes software developed by lenya (http://www.lenya.org)"
  *
  * THIS SOFTWARE IS PROVIDED BY lenya "AS IS" WITHOUT ANY WARRANTY EXPRESS OR IMPLIED,
  * INCLUDING THE WARRANTY OF NON-INFRINGEMENT AND THE IMPLIED WARRANTIES OF MERCHANTI-
  * BILITY AND FITNESS FOR A PARTICULAR PURPOSE. lenya WILL NOT BE LIABLE FOR ANY DAMAGES
  * SUFFERED BY YOU AS A RESULT OF USING THIS SOFTWARE. IN NO EVENT WILL lenya BE LIABLE
  * FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL DAMAGES OR LOST PROFITS EVEN IF lenya HAS
  * BEEN ADVISED OF THE POSSIBILITY OF THEIR OCCURRENCE. lenya WILL NOT BE LIABLE FOR ANY
  * THIRD PARTY CLAIMS AGAINST YOU.
  *
  * Lenya includes software developed by the Apache Software Foundation, W3C,
  * DOM4J Project, BitfluxEditor and Xopus.
  * </License>
  */
 
 package ch.unizh.lenya.util;
 
 import java.io.File;
 
 import org.apache.lenya.cms.publication.Publication;
 import org.apache.lenya.cms.publication.Version;
 import org.apache.lenya.cms.publication.SiteTree;
 import org.apache.lenya.cms.publication.SiteTreeNode;
 import org.apache.lenya.cms.publication.SiteTreeException;
 
 import org.apache.avalon.framework.configuration.Configuration;
 import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
 
 
 /**
  * 
  * Delete cache entries of a document. If the document has no parent and the document 
  * is not yet live the whole cache will be deleted (otherwise the navigation of the 
  * cached content is not refreshed. 
  * If the document has parents only the its containing directory will be deleted
  * (i.e. the parent node) if the document is not yet live. Only the actual document will 
  * be deleted if the document is already live. 
  * 
  * @author <a href="mailto:jann@apache.org">Jann Forrer </a>
  */
 
 public class CacheHandler {
 
     private static final String LOCK_FILE = "cache.locked";
     
     public CacheHandler() {    
     }
     
     /**
      * delete the cache entries. 
      * 
      * @param liveVersion The document to deactivate.
      * @throws SiteTreeException
      *             when something went wrong TODO: throw another Exception?
      * 
      */
     public void deleteCache(Version liveVersion, boolean isLive, String task) throws SiteTreeException {
 
         SiteTree siteTree = null;
         String path = File.separator;
         
         Publication pub = liveVersion.getResource().getPublicationWrapper().getPublication();
         
         String area = liveVersion.getDocument().getArea();
         siteTree = pub.getTree(area);
         SiteTreeNode node = siteTree.getNode(liveVersion.getDocument().getId());
         
         if (node != null) {
             path = setPath(node, isLive, task);
         
             File pubDir = pub.getDirectory();
             String pubId = pub.getId();
             File cacheDir = new File(pubDir, "work" + File.separator + "cache" + File.separator + area);
             File cacheLockDir = new  File(pubDir, "work" + File.separator + "cache");
             File cacheFile = new File(cacheDir, path);
 
             String cachePrefix = getCachePrefix(pub);
         
             if (cachePrefix != null) {
                 cacheDir = new File(cachePrefix + File.separator+ pubId + File.separator + area);
                 cacheLockDir = new File(cachePrefix + File.separator + pubId);
                 cacheFile = new File(cacheDir, path);
             }
         
             lockCacheDir(cacheLockDir);
 
             if (cacheFile.isDirectory()) {
                 File oldCacheDir = new File(cacheFile + ".old"); 
                 if (path.equals(File.separator)) {
                     oldCacheDir = new File("index.old");   
                 }
                 if (cacheFile.renameTo(oldCacheDir)) {
                     deleteDir(oldCacheDir);
                 } else {
                     deleteDir(cacheFile);
                 }
             } else {
                 if (cacheFile.exists()) {
                     cacheFile.setReadOnly();
                     cacheFile.delete();           
                 }
             }
             unlockCacheDir(cacheLockDir);
         }
     }
     
     /**
      * delete all Files in a directory recursivley.
      * 
      * @param dir The directory to delete.
      * @return A boolean value.
      */
     protected static boolean deleteDir(File dir) {
         if (dir.isDirectory()) {
             String[] children = dir.list();
             for (int i=0; i<children.length; i++) {
                 boolean success = deleteDir(new File(dir, children[i]));
                 if (!success) {
                     return false;
                 }
             }
         }
         return dir.delete();
     }
 
     /**
      * Set the appropriate path according to the following rules:
      * -> if the task is publish and the document is already live: delete document only
      * -> delete the parent node including the whole subtree, otherwise
      *    Note that there is no parent for top level elements. In that case delete the 
      *    whole cache (i.e. path = File separator).
      * 
      * @param node a SiteTreeNode
      *        isLive whether the node already exists in the live sitetreee
      *        task   the respective task (publish or deactivate)
      * @return A String
      * 
      */  
     protected String setPath(SiteTreeNode node, boolean isLive, String task) {
         
         String path=File.separator;
         
         String parentId = node.getAbsoluteParentId();
        String documentId = node.getAbsoluteId();
         
         if (parentId.indexOf(File.separator) == -1){
             path = File.separator;
         } else  {
             path = parentId;
         }
        if (isLive && task.equals("publish")) path = documentId+"/index_de.xml";
         
         return path;
     }
  
     /**
      * Set the path prefix for the caching directory:
      *  It reads the configuration file publication.xconf
      *  if the cache Element is set it takes the value of its path-prefix attriubte
      *  otherwise the method will return null and the default caching directory is used
      *  i.e.: work/cache/<area> directory in the respective publication. 
      * 
      * @return A String
      * 
      */   
     protected String getCachePrefix(Publication pub){
         
         String cacheElement = "cache";
         String cacheAttr    = "path-prefix";
             
         File configurationFile = new File(pub.getDirectory(), Publication.CONFIGURATION_FILE);
         
         DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();
 
         try {
             Configuration config = builder.buildFromFile(configurationFile);
             return config.getChild(cacheElement).getAttribute(cacheAttr);
         } catch (Exception e) {
             return null;
         }
     }
     
     /**
      * Locking the whole cache by simply creating a file called
      * <it>cache.locked</it> on the top level directory of the cache.
      * 
      * @return boolean
      * 
      */
     protected boolean lockCacheDir(File file){
   
         File cacheLockFile = this.getLockFile(file);
         try {
             cacheLockFile.createNewFile();
         } catch (Exception e ){
             return false;
         }
         return true;
     }
     /**
      * Unlocking the cache by simply deleting the cache.locked file
      * 
      */
     protected void unlockCacheDir(File file){
         
         File cacheLockFile = this.getLockFile(file);
         if (cacheLockFile.exists()) {
             cacheLockFile.delete();
         }
         
     }
     
     protected File getLockFile(File file) {
         File lockFile = new File(file + File.separator + LOCK_FILE);
         return lockFile;
     }
         
 }
