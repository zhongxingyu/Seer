 /*
  * The contents of this file are subject to the license and copyright
  * detailed in the LICENSE and NOTICE files at the root of the source
  * tree and available online at
  *
  *     http://dspace.org/license/
  */
 
 package org.dspace.ctask.replicate.impl;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Map;
 
 import org.duracloud.client.ContentStore;
 import org.duracloud.client.ContentStoreManager;
 import org.duracloud.client.ContentStoreManagerImpl;
 import org.duracloud.common.model.Credential;
 import org.duracloud.domain.Content;
 import org.duracloud.error.ContentStoreException;
 import org.duracloud.error.NotFoundException;
 
 import org.dspace.core.ConfigurationManager;
 import org.dspace.ctask.replicate.ObjectStore;
 import org.dspace.curate.Utils;
 
 /**
  * SyncReplicaStore uses the DuraCloud 'sync' tool to manage replicas. This
  * consists primarily of a directory monitored by the tool that will synchonize
  * with the remote replica store.
  * 
  * @author richardrodgers
  */
 public class SyncObjectStore implements ObjectStore {
 
     private String syncDir = null;
     // DuraCloud store
     private ContentStore dcStore = null;
     // phony values
     private static final String HOST = "http://www.duracloud.org";
     private static final String PORT = "8080";
     private static final String CONTEXT = "context";
     private static final String USERNAME = "rrodgers";
     private static final String PASSWORD = "pword";
     private static final String SPACEID = "test";
     
     public SyncObjectStore() {
     }
 
     @Override
     public void init() throws IOException
     {
         syncDir = ConfigurationManager.getProperty("replicate", "store.dir");
          // locate Duracloud store
         ContentStoreManager storeManager =
             new ContentStoreManagerImpl(HOST, PORT, CONTEXT);
         Credential credential = new Credential(USERNAME, PASSWORD);
         storeManager.login(credential);
         try
         {
             dcStore = storeManager.getPrimaryContentStore();
         }
         catch (ContentStoreException csE)
         {
             throw new IOException(csE.getMessage(), csE);
         }
     }
 
     @Override
     public long fetchObject(String group, String id, File file) throws IOException {
         // first check for local copy - locate zip file and unzip
         long size = 0L;
         File repFile = new File(syncDir + File.separator + group, id);
         if (repFile.exists()) {
             size = repFile.length();
             Utils.copy(repFile, file);
         } else {
             // check remote store
             try {
                 Content content = dcStore.getContent(group, id);
                size = Long.valueOf(content.getProperties().get(ContentStore.CONTENT_SIZE));
                 FileOutputStream out = new FileOutputStream(file);
                 InputStream in = content.getStream();
                 Utils.copy(in, out);
                 in.close();
                 out.close();
             } catch (NotFoundException nfE) {
                 // not found - no op
             } catch (ContentStoreException csE) {
                 throw new IOException(csE);
             }
         }
         return size;
     }
 
     @Override
     public boolean objectExists(String group, String id) throws IOException {
         // do we have a copy in our managed area?
         // if not check the remote store
         if (new File(syncDir + File.separator + group, id).exists()) {
             return true;
         }
          // RLR TODO - verify that no content I/O occurs here
         try {
             return dcStore.getContent(group, id) != null;
         } catch (NotFoundException nfE) {
             return false;
         } catch (ContentStoreException csE) {
             throw new IOException(csE);
         }
     }
 
     @Override
     public long removeObject(String group, String id) throws IOException {
         // since auto-delete mode of Synch tool supressed, removals
         // managed directly on remote store
         // but get rid of any local copies too
         File remFile = new File(syncDir + File.separator + group, id);
         if (remFile.exists()) {
             remFile.delete();
         }
         // get metadata before blowing away
         long size = 0L;
         try {
            Map<String, String> attrs = dcStore.getContentProperties(group, id);
             size = Long.valueOf(attrs.get(ContentStore.CONTENT_SIZE));
             dcStore.deleteContent(group, id);
         } catch (NotFoundException nfE) {
             // no-op
         } catch (ContentStoreException csE) {
             throw new IOException(csE);
         }
         return size;
     }
 
     @Override
     public long transferObject(String group, File file) throws IOException {
         // for rsynch tool-backed replication transfer is a simple matter
         // of copying into the managed directory - the tool manages the upload.
         // we don't bother checking if replica is really new, since
         // the sync tools worries about this and will optimize
         File archive = new File(syncDir + File.separator + group, file.getName());
         if (archive.exists()) {
             archive.delete();
         }
         Utils.copy(file, archive);
         return file.length();
     }
 
     @Override
     public String objectAttribute(String group, String id, String attrName) throws IOException {
         File archive = new File(syncDir + File.separator + group, id);
         if ("checksum".equals(attrName)) {
             return Utils.checksum(archive, "MD5");
         } else if ("sizebytes".equals(attrName)) {
             return String.valueOf(archive.length());
         } else if ("modified".equals(attrName)) {
             return String.valueOf(archive.lastModified());
         }
         return null;
     }
 }
