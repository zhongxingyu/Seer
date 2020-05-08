 /*
  * Licensed to the Sakai Foundation (SF) under one
  * or more contributor license agreements. See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership. The SF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations under the License.
  */
 package org.sakaiproject.nakamura.lite.content;
 
 import static org.sakaiproject.nakamura.lite.content.InternalContent.BLOCKID_FIELD;
 import static org.sakaiproject.nakamura.lite.content.InternalContent.BODY_CREATED_BY_FIELD;
 import static org.sakaiproject.nakamura.lite.content.InternalContent.BODY_CREATED_FIELD;
 import static org.sakaiproject.nakamura.lite.content.InternalContent.BODY_LAST_MODIFIED_BY_FIELD;
 import static org.sakaiproject.nakamura.lite.content.InternalContent.BODY_LAST_MODIFIED_FIELD;
 import static org.sakaiproject.nakamura.lite.content.InternalContent.COPIED_DEEP_FIELD;
 import static org.sakaiproject.nakamura.lite.content.InternalContent.COPIED_FROM_ID_FIELD;
 import static org.sakaiproject.nakamura.lite.content.InternalContent.COPIED_FROM_PATH_FIELD;
 import static org.sakaiproject.nakamura.lite.content.InternalContent.CREATED_BY_FIELD;
 import static org.sakaiproject.nakamura.lite.content.InternalContent.CREATED_FIELD;
 import static org.sakaiproject.nakamura.lite.content.InternalContent.DELETED_FIELD;
 import static org.sakaiproject.nakamura.lite.content.InternalContent.LASTMODIFIED_BY_FIELD;
 import static org.sakaiproject.nakamura.lite.content.InternalContent.LASTMODIFIED_FIELD;
 import static org.sakaiproject.nakamura.lite.content.InternalContent.LENGTH_FIELD;
 import static org.sakaiproject.nakamura.lite.content.InternalContent.LINKED_PATH_FIELD;
 import static org.sakaiproject.nakamura.lite.content.InternalContent.NEXT_VERSION_FIELD;
 import static org.sakaiproject.nakamura.lite.content.InternalContent.PATH_FIELD;
 import static org.sakaiproject.nakamura.lite.content.InternalContent.PREVIOUS_BLOCKID_FIELD;
 import static org.sakaiproject.nakamura.lite.content.InternalContent.PREVIOUS_VERSION_UUID_FIELD;
 import static org.sakaiproject.nakamura.lite.content.InternalContent.READONLY_FIELD;
 import static org.sakaiproject.nakamura.lite.content.InternalContent.STRUCTURE_UUID_FIELD;
 import static org.sakaiproject.nakamura.lite.content.InternalContent.TRUE;
 import static org.sakaiproject.nakamura.lite.content.InternalContent.UUID_FIELD;
 import static org.sakaiproject.nakamura.lite.content.InternalContent.VERSION_HISTORY_ID_FIELD;
 import static org.sakaiproject.nakamura.lite.content.InternalContent.VERSION_NUMBER_FIELD;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import com.google.common.collect.Ordering;
 import org.sakaiproject.nakamura.api.lite.CacheHolder;
 import org.sakaiproject.nakamura.api.lite.Configuration;
 import org.sakaiproject.nakamura.api.lite.StorageClientException;
 import org.sakaiproject.nakamura.api.lite.StorageClientUtils;
 import org.sakaiproject.nakamura.api.lite.StorageConstants;
 import org.sakaiproject.nakamura.api.lite.StoreListener;
 import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessControlManager;
 import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
 import org.sakaiproject.nakamura.api.lite.accesscontrol.Permissions;
 import org.sakaiproject.nakamura.api.lite.accesscontrol.PrincipalTokenResolver;
 import org.sakaiproject.nakamura.api.lite.accesscontrol.Security;
 import org.sakaiproject.nakamura.api.lite.authorizable.User;
 import org.sakaiproject.nakamura.api.lite.content.ActionRecord;
 import org.sakaiproject.nakamura.api.lite.content.Content;
 import org.sakaiproject.nakamura.api.lite.content.ContentManager;
 import org.sakaiproject.nakamura.api.lite.util.PreemptiveIterator;
 import org.sakaiproject.nakamura.lite.CachingManager;
 import org.sakaiproject.nakamura.lite.storage.DisposableIterator;
 import org.sakaiproject.nakamura.lite.storage.SparseRow;
 import org.sakaiproject.nakamura.lite.storage.StorageClient;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.ImmutableMap.Builder;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 
 /**
  * <pre>
  * Content Manager.
  * Manages two types of content,
  * Bundles of content properties and bodies.
  * Bodies are chunked into sizes to aide efficiency when retrieving the content.
  * 
  * CF content stores the structure of the content keyed by path.
  * Each item contains child names in columns + the guid of the item
  * eg
  *   path : {
  *       ':id' : thisitemUUID,
  *       subitemA : subitemAUUID,
  *       subitemB : subitemBUUID
  *   }
  * the guid of the item points to the CF content version where items are keyed by the version.
  * These items also contain child nodes under children as an array
  * 
  * eg
  *    itemUUID : {
  *         'id' : thisitemUUID
  *         'children' : [ 
  *           subitemA : subitemAUUID,
  *           subitemB : subitemBUUID
  *         ],
  *         'nblocks' = numberOfBlocksSetsOfContent
  *         'length' = totalLenghtOftheContent
  *         'blocksize' = storageBlockSize
  *         'blockid' = blockID
  *         ... other properties ...
  *    }
  *    
  * The content blocks are stored in CF content body
  * eg
  *   blockID:blockSetNumber : {
  *         'id' : blockID,
  *         'numblocks' : numberOfBlocksInThisSet,
  *         'blocklength0' : lengthOfThisBlock,
  *         'body0' : byte[]
  *         'blocklength1' : lengthOfThisBlock,
  *         'body1' : byte[]
  *         ...
  *         'blocklengthn' : lengthOfThisBlock,
  *         'bodyn' : byte[]
  *    }
  * 
  * 
  * Versioning:
  * 
  * When a version is saved, the CF contentVersion item is cloned and the CF content :id and any subitems IDs are updated.
  * Block 0 is marked as readonly
  * 
  * When the body is written to its CF content row is checked to see if the block is read only. If so a new block is created with and linked in with 'previousversion'
  * A version object is also created to keep track of the versions.
  * 
  * </pre>
  * 
  * @author ieb
  * 
  */
 public class ContentManagerImpl extends CachingManager implements ContentManager {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(ContentManagerImpl.class);
 
 
     private static final Set<String> PROTECTED_FIELDS = ImmutableSet.of(LASTMODIFIED_FIELD,
                                                                         LASTMODIFIED_BY_FIELD,
                                                                         UUID_FIELD,
                                                                         PATH_FIELD);
 
 
     /**
      * Storage Client
      */
     private StorageClient client;
     /**
      * The access control manager in use.
      */
     private AccessControlManager accessControlManager;
     /**
      * Key space for this content.
      */
     private String keySpace;
     /**
      * Column Family for this content.
      */
     private String contentColumnFamily;
 
     private boolean closed;
 
     private StoreListener eventListener;
  
 
     private PathPrincipalTokenResolver pathPrincipalResolver;
 
     public ContentManagerImpl(StorageClient client, AccessControlManager accessControlManager,
             Configuration config,  Map<String, CacheHolder> sharedCache, StoreListener eventListener) {
         super(client, sharedCache);
         this.client = client;
         keySpace = config.getKeySpace();
         contentColumnFamily = config.getContentColumnFamily();
         closed = false;
         this.eventListener = eventListener;
         String userId = accessControlManager.getCurrentUserId();
         String usersTokenPath = StorageClientUtils.newPath(userId, "private/tokens");
         this.pathPrincipalResolver = new PathPrincipalTokenResolver(usersTokenPath, this);
         this.accessControlManager = new AccessControlManagerTokenWrapper(accessControlManager, pathPrincipalResolver);
     }
   
 
     public boolean exists(String path) {
         try {
             checkOpen();
             accessControlManager.check(Security.ZONE_CONTENT, path, Permissions.CAN_READ);
             Map<String, Object> structure = getCached(keySpace, contentColumnFamily, path);
             if (structure != null && structure.size() > 0) {
                 String contentId = (String)structure.get(STRUCTURE_UUID_FIELD);
                 Map<String, Object> content = getCached(keySpace, contentColumnFamily, contentId);
                 if (content != null && content.size() > 0) {
                     return true;
                 }
             }
         } catch (AccessDeniedException e) {
             LOGGER.debug(e.getMessage(), e);
         } catch (StorageClientException e) {
             LOGGER.debug(e.getMessage(), e);
         }
         return false;
     }
 
     public Content get(String path) throws StorageClientException, AccessDeniedException {
         checkOpen();
         accessControlManager.check(Security.ZONE_CONTENT, path, Permissions.CAN_READ);
         Map<String, Object> structure = getCached(keySpace, contentColumnFamily, path);
         if (structure != null && structure.size() > 0) {
             String contentId = (String)structure.get(STRUCTURE_UUID_FIELD);
             Map<String, Object> content = getCached(keySpace, contentColumnFamily, contentId);
             if (content != null && content.size() > 0 && !TRUE.equals(content.get(DELETED_FIELD))) {
                 Content contentObject = new Content(path, content);
                 ((InternalContent) contentObject).internalize(this, false);
                 return contentObject;
             }
         }
         return null;
 
     }
 
 
     public Iterator<Content> listChildren(String path) throws StorageClientException {
         final DisposableIterator<Map<String, Object>> childContent = client.listChildren(keySpace,
                 contentColumnFamily, path, this);
         return new PreemptiveIterator<Content>() {
 
             private Content content;
 
             @Override
             protected boolean internalHasNext() {
                 content = null;
                 while(content == null && childContent.hasNext()) {
                     try {
                         Map<String, Object> structureMap = childContent.next();
                         LOGGER.debug("Loaded Next as {} ", structureMap);
                         if ( structureMap != null && structureMap.size() > 0 ) {
                             String path = (String) structureMap.get(PATH_FIELD);
                             content = get(path);
                         }
                     } catch (AccessDeniedException e) {
                         LOGGER.debug(e.getMessage(),e);
                     } catch (StorageClientException e) {
                         LOGGER.debug(e.getMessage(),e);
                     }
                 }
                 if  (content == null) {
                     // this is over the top as a disposable iterator should close auto
                     childContent.close();
                     super.close();
                     return false;
                 }
                 return true;
             }
 
             @Override
             protected Content internalNext() {
                 return content;
             }
         };
     }
 
     public Iterator<String> listChildPaths(final String path) throws StorageClientException {
         final Iterator<Map<String, Object>> childContent = client.listChildren(keySpace,
                 contentColumnFamily, path, this);
         return new PreemptiveIterator<String>() {
 
             private String childPath;
 
             @Override
             protected boolean internalHasNext() {
                 while(childContent.hasNext()) {
                     try {
                         Map<String, Object> structureMap = childContent.next();
                         LOGGER.debug("Loaded Next child of {} as {} ", path, structureMap);
                         if ( structureMap != null && structureMap.size() > 0 ) {
                             String testChildPath = (String) structureMap.get(PATH_FIELD);
                                 accessControlManager.check(Security.ZONE_CONTENT, testChildPath, Permissions.CAN_READ);
                                 childPath = testChildPath;
                                 // this is not that efficient since it requires the map is
                                 // loaded, at the moment I dont have a way round this with the
                                 // underlying index strucutre.
                                 LOGGER.debug("Got Next Child of {} as {} ", path, childPath);
                                 return true;
                         }
                     } catch (AccessDeniedException e) {
                         LOGGER.debug(e.getMessage(),e);
                     } catch (StorageClientException e) {
                         LOGGER.debug(e.getMessage(),e);
                     }
                 }
                 LOGGER.debug("No more");
                 childPath = null;
                 super.close();
                 return false;
             }
 
             @Override
             protected String internalNext() {
                 return childPath;
             }
         };
     }
     
     public void triggerRefresh(String path) throws StorageClientException, AccessDeniedException {
         Content c = get(path);
         if ( c != null ) {
             eventListener.onUpdate(Security.ZONE_CONTENT, path,  accessControlManager.getCurrentUserId(), getResourceType(c), false, c.getOriginalProperties(), "op:update");
         }
     }
     
     private String getResourceType(InternalContent c) {
         String resourceType = null;
         if ( c != null ) {
             if ( c.hasProperty(Content.SLING_RESOURCE_TYPE_FIELD)) {
                 resourceType = (String) c.getProperty(Content.SLING_RESOURCE_TYPE_FIELD);
             } else if ( c.hasProperty(Content.RESOURCE_TYPE_FIELD)) {
                 resourceType = (String) c.getProperty(Content.RESOURCE_TYPE_FIELD);
             } else if ( c.hasProperty(Content.MIMETYPE_FIELD)) {
                 resourceType = (String) c.getProperty(Content.MIMETYPE_FIELD);
             }
         }
         return resourceType;
     }
     private String getResourceType(Map<String, Object> c) {
         String resourceType = null;
         if ( c != null ) {
             if ( c.containsKey(Content.SLING_RESOURCE_TYPE_FIELD)) {
                 resourceType = (String) c.get(Content.SLING_RESOURCE_TYPE_FIELD);
             } else if ( c.containsKey(Content.RESOURCE_TYPE_FIELD)) {
                 resourceType = (String) c.get(Content.RESOURCE_TYPE_FIELD);
             } else if ( c.containsKey(Content.MIMETYPE_FIELD)) {
                 resourceType = (String) c.get(Content.MIMETYPE_FIELD);
             }
         }
         return resourceType;
     }
 
 
     public void triggerRefreshAll() throws StorageClientException {
         if (User.ADMIN_USER.equals(accessControlManager.getCurrentUserId()) ) {
             DisposableIterator<SparseRow> all = client.listAll(keySpace, contentColumnFamily);
             try {
                 while(all.hasNext()) {
                     Map<String, Object> c = all.next().getProperties();
                     if ( c.containsKey(PATH_FIELD) && !c.containsKey(STRUCTURE_UUID_FIELD)) {
                         
                         eventListener.onUpdate(Security.ZONE_CONTENT, (String)c.get(PATH_FIELD), User.ADMIN_USER, getResourceType(c), false, ImmutableMap.copyOf(c), "op:update");                    
                     }
                 }
             } finally {
                 all.close();
             }
         }
     }
 
     public void update(Content excontent) throws AccessDeniedException, StorageClientException {
         checkOpen();
         InternalContent content = (InternalContent) excontent;
         String path = content.getPath();
         accessControlManager.check(Security.ZONE_CONTENT, path, Permissions.CAN_WRITE);
         String id = null;
         Map<String, Object> toSave = null;
         // deal with content that already exists.
         if (content.isNew()) {
             Content existingContent = get(path);
             if ( existingContent != null ) {
                 Map<String, Object> properties = content.getProperties();
                 for ( Entry<String, Object> e : properties.entrySet()) {
                    existingContent.setProperty(e.getKey(), e.getValue());
                 }
                 content = existingContent;
             }
         }
 
         Map<String, Object> originalProperties = ImmutableMap.of();
 
         if (content.isNew()) {
             // create the parents if necessary
             if (!StorageClientUtils.isRoot(path)) {
                 String parentPath = StorageClientUtils.getParentObjectPath(path);
                 Content parentContent = get(parentPath);
                 if (parentContent == null) {
                     update(new Content(parentPath, null));
                 }
             }
             toSave =  Maps.newHashMap(content.getPropertiesForUpdate());
             id = StorageClientUtils.getInternalUuid();
             // if the user is admin we allow overwriting of protected fields. This should allow content migration.
             toSave.put(UUID_FIELD, id);
             toSave.put(PATH_FIELD, path);
             toSave.put(CREATED_FIELD, System.currentTimeMillis());
             toSave.put(CREATED_BY_FIELD, accessControlManager.getCurrentUserId());
             toSave.put(LASTMODIFIED_FIELD, System.currentTimeMillis());
             toSave.put(LASTMODIFIED_BY_FIELD,
                     accessControlManager.getCurrentUserId());
             LOGGER.debug("New Content with {} {} ", id, toSave);
         } else if (content.isUpdated()) {
             originalProperties = content.getOriginalProperties();
             toSave =  Maps.newHashMap(content.getPropertiesForUpdate());
 
             for (String field : PROTECTED_FIELDS) {
                 LOGGER.debug ("Resetting value for {} to {}", field, originalProperties.get(field));
                 toSave.put(field, originalProperties.get(field));
             }
 
             id = (String)toSave.get(UUID_FIELD);
             toSave.put(LASTMODIFIED_FIELD, System.currentTimeMillis());
             toSave.put(LASTMODIFIED_BY_FIELD,
                     accessControlManager.getCurrentUserId());
             LOGGER.debug("Updating Content with {} {} ", id, toSave);
         } else {
             // if not new or updated, dont update.
             return;
         }
 
         Map<String, Object> checkContent = getCached(keySpace, contentColumnFamily, id);
         if (TRUE.equals((String)checkContent.get(READONLY_FIELD))) {
             throw new AccessDeniedException(Security.ZONE_CONTENT, path,
                     "update on read only Content Item (possibly a previous version of the item)",
                     accessControlManager.getCurrentUserId());
         }
         boolean isnew = false;
         if (content.isNew()) {
             isnew = true;
             putCached(keySpace, contentColumnFamily, path,
                     ImmutableMap.of(STRUCTURE_UUID_FIELD, (Object)id, PATH_FIELD, path), true);
         }
         // save the content id.
         putCached(keySpace, contentColumnFamily, id, toSave, isnew);
         LOGGER.debug("Saved {} at {} as {} ", new Object[] { path, id, toSave });
         // reset state to unmodified to take further modifications.
         content.reset(getCached(keySpace, contentColumnFamily, id));
         
         eventListener.onUpdate(Security.ZONE_CONTENT, path, accessControlManager.getCurrentUserId(), getResourceType(content),  isnew, originalProperties, "op:update");
     }
     
 
     public void delete(String path) throws AccessDeniedException, StorageClientException {
         checkOpen();
         accessControlManager.check(Security.ZONE_CONTENT, path, Permissions.CAN_DELETE);
         Map<String, Object> structure = getCached(keySpace, contentColumnFamily, path);
         if ( structure != null && structure.size() > 0 ) {
             String uuid = (String)structure.get(STRUCTURE_UUID_FIELD);
             Map<String, Object> content = getCached(keySpace, contentColumnFamily, uuid);
             Map<String, Object> contentBeforeDelete = ImmutableMap.copyOf(content);
             String resourceType = (String) content.get("sling:resourceType");
             putCached(keySpace, contentColumnFamily, uuid,
                     ImmutableMap.of(DELETED_FIELD, (Object) TRUE), false);
             eventListener.onDelete(Security.ZONE_CONTENT, path, accessControlManager.getCurrentUserId(), resourceType, contentBeforeDelete);
         }
     }
 
     public long writeBody(String path, InputStream in) throws StorageClientException,
             AccessDeniedException, IOException {
         return writeBody(path, in, null);
     }
 
     public long writeBody(String path, InputStream in, String streamId)
             throws StorageClientException, AccessDeniedException, IOException {
         checkOpen();
         accessControlManager.check(Security.ZONE_CONTENT, path, Permissions.CAN_WRITE);
         Map<String, Object> structure = getCached(keySpace, contentColumnFamily, path);
         if ( structure == null || structure.size() == 0 ) {
             Content content = new Content(path,null);
             update(content);
             structure = getCached(keySpace, contentColumnFamily, path);
         }
         String contentId = (String)structure.get(STRUCTURE_UUID_FIELD);
         Map<String, Object> content = getCached(keySpace, contentColumnFamily, contentId);
         boolean isnew = true;
         String blockIdField = StorageClientUtils.getAltField(BLOCKID_FIELD, streamId);
         if (content.containsKey(blockIdField)) {
             isnew = false;      
         }
         String contentBlockId = StorageClientUtils.getInternalUuid();
         
         Map<String, Object> metadata = client.streamBodyIn(keySpace, contentColumnFamily,
                 contentId, contentBlockId, streamId, content, in);
         metadata.put(StorageClientUtils.getAltField(BODY_LAST_MODIFIED_FIELD, streamId),
                 System.currentTimeMillis());
         metadata.put(StorageClientUtils.getAltField(BODY_LAST_MODIFIED_BY_FIELD, streamId),
                 accessControlManager.getCurrentUserId());
         if (isnew) {
             metadata.put(StorageClientUtils.getAltField(BODY_CREATED_FIELD, streamId),
                     System.currentTimeMillis());
             metadata.put(StorageClientUtils.getAltField(BODY_CREATED_BY_FIELD, streamId),
                     accessControlManager.getCurrentUserId());
         }
         putCached(keySpace, contentColumnFamily, contentId, metadata, isnew);
         long length = 0;
         String lengthFieldName = StorageClientUtils.getAltField(LENGTH_FIELD, streamId);
         if (metadata.containsKey(lengthFieldName)) {
           length = (Long) metadata.get(lengthFieldName);
         }
         eventListener.onUpdate(Security.ZONE_CONTENT, path, accessControlManager.getCurrentUserId(), getResourceType(content), false, null, "stream", streamId);
         return length;
 
     }
 
     public InputStream getInputStream(String path) throws StorageClientException,
             AccessDeniedException, IOException {
         return getInputStream(path, null);
     }
 
     public InputStream getInputStream(String path, String streamId) throws StorageClientException,
             AccessDeniedException, IOException {
         checkOpen();
         accessControlManager.check(Security.ZONE_CONTENT, path, Permissions.CAN_READ);
         Map<String, Object> structure = getCached(keySpace, contentColumnFamily, path);
         LOGGER.debug("Structure Loaded {} {} ", path, structure);
         String contentId = (String)structure.get(STRUCTURE_UUID_FIELD);
         return internalGetInputStream(contentId, streamId);
     }
 
     private InputStream internalGetInputStream(String contentId, String streamId)
             throws StorageClientException, AccessDeniedException, IOException {
         Map<String, Object> content = getCached(keySpace, contentColumnFamily, contentId);
         String contentBlockId = (String)content.get(StorageClientUtils
                 .getAltField(BLOCKID_FIELD, streamId));
         return client.streamBodyOut(keySpace, contentColumnFamily, contentId, contentBlockId, streamId,
                 content);
     }
 
     public void close() {
         closed = true;
     }
 
     private void checkOpen() throws StorageClientException {
         if (closed) {
             throw new StorageClientException("Content Manager is closed");
         }
     }
 
     // TODO: Unit test
     /**
      * {@inheritDoc}
      * @see org.sakaiproject.nakamura.api.lite.content.ContentManager#copy(java.lang.String, java.lang.String, boolean)
      */
     public void copy(String from, String to, boolean withStreams) throws StorageClientException,
             AccessDeniedException, IOException {
         checkOpen();
         // To Copy, get the to object out and copy everything over.
         Content f = get(from);
         if (f == null) {
             throw new StorageClientException(" Source content " + from + " does not exist");
         }
         if ( f.getProperty(UUID_FIELD) == null ) {
             LOGGER.warn("Bad Content item with no ID cant be copied {} ",f);
             throw new StorageClientException(" Source content " + from + "  Has no "+UUID_FIELD);      
         }
         Content t = get(to);
         if (t != null) {
            LOGGER.debug("Deleting {} ",to);
            delete(to);
         }
         Set<String> streams = Sets.newHashSet();
         Map<String, Object> copyProperties = Maps.newHashMap();
         if (withStreams) {
             for (Entry<String, Object> p : f.getProperties().entrySet()) {
                 // Protected fields (such as ID and path) will differ between
                 // the source and destination, so don't copy them.
                 if (!PROTECTED_FIELDS.contains(p.getKey())) {
                     if (p.getKey().startsWith(BLOCKID_FIELD)) {
                         streams.add(p.getKey());
                     } else {
                         copyProperties.put(p.getKey(), p.getValue());
                     }
                 }
             }
         } else {
             copyProperties.putAll(f.getProperties());
         }
         copyProperties.put(COPIED_FROM_PATH_FIELD, from);
         copyProperties.put(COPIED_FROM_ID_FIELD, f.getProperty(UUID_FIELD));
         copyProperties.put(COPIED_DEEP_FIELD, withStreams);
         t = new Content(to, copyProperties);
         update(t);
         LOGGER.debug("Copy Updated {} {} ",to,t);
 
         for (String stream : streams) {
             String streamId = null;
             if (stream.length() > BLOCKID_FIELD.length()) {
                 streamId = stream.substring(BLOCKID_FIELD.length() + 1);
             }
             InputStream fromStream = getInputStream(from, streamId);
             writeBody(to, fromStream);
             fromStream.close();
         }
         eventListener.onUpdate(Security.ZONE_CONTENT, to, accessControlManager.getCurrentUserId(), getResourceType(f), true, null, "op:copy");
 
     }
 
     // TODO: Unit test
     public void move(String from, String to) throws AccessDeniedException, StorageClientException {
         // to move, get the structure object out and modify, recreating parent
         // objects as necessary.
         checkOpen();
         accessControlManager.check(Security.ZONE_CONTENT, from, Permissions.CAN_ANYTHING);
         accessControlManager.check(Security.ZONE_CONTENT, to,
                 Permissions.CAN_READ.combine(Permissions.CAN_WRITE));
         Map<String, Object> fromStructure = Maps.newHashMap(getCached(keySpace, contentColumnFamily, from));
         if (fromStructure == null || fromStructure.size() == 0) {
            throw new StorageClientException("The source content to move from " + from
                    + " does not exist, move operation failed");
        }
        if (fromStructure != null && fromStructure.size() > 0 ) {
             String contentId = (String)fromStructure.get(STRUCTURE_UUID_FIELD);
             Map<String, Object> content = getCached(keySpace, contentColumnFamily, contentId);
             if (content == null || content.size() == 0 && TRUE.equals(content.get(DELETED_FIELD))) {
                 throw new StorageClientException("The source content to move from " + from
                     + " does not exist, move operation failed");
             }
         }
         Map<String, Object> toStructure = getCached(keySpace, contentColumnFamily, to);
         if (toStructure != null && toStructure.size() > 0) {
             String contentId = (String)toStructure.get(STRUCTURE_UUID_FIELD);
             Map<String, Object> content = getCached(keySpace, contentColumnFamily, contentId);
             if (content != null && content.size() > 0 && !TRUE.equals(content.get(DELETED_FIELD))) {
                 throw new StorageClientException("The destination content to move to " + to
                     + "  exists, move operation failed");
             }
         }
         String idStore = (String) fromStructure.get(STRUCTURE_UUID_FIELD);
 
         // move the content to the new location, then delete the old.
         if (!StorageClientUtils.isRoot(to)) {
             // if not a root, modify the new parent location, creating the
             // structured if necessary
             String parent = StorageClientUtils.getParentObjectPath(to);
             Map<String, Object> parentToStructure = getCached(keySpace, contentColumnFamily,
                     parent);
             if (parentToStructure == null || parentToStructure.size() == 0) {
                 // create a new parent
                 Content content = new Content(parent, null);
                 update(content);
             }
 
         }
         // update the content data to reflect the new primary location.
         putCached(keySpace, contentColumnFamily, idStore,
                 ImmutableMap.of(PATH_FIELD, (Object)to), false);
 
         // insert the new to Structure and remove the from
         fromStructure.put(PATH_FIELD, to);
         putCached(keySpace, contentColumnFamily, to, fromStructure, true);
 
         // remove the old from.
         removeCached(keySpace, contentColumnFamily, from);
         // move does not add resourceTypes to events.
         eventListener.onDelete(Security.ZONE_CONTENT, from, accessControlManager.getCurrentUserId(), null, null, "op:move");
         eventListener.onUpdate(Security.ZONE_CONTENT, to, accessControlManager.getCurrentUserId(), null, true, null, "op:move");
 
     }
 
   public List<ActionRecord> moveWithChildren(String from, String to)
       throws AccessDeniedException,
       StorageClientException {
     List<ActionRecord> record = Lists.newArrayList();
 
     move(from, to);
 
     PreemptiveIterator<String> iter = (PreemptiveIterator<String>) listChildPaths(from);
     while (iter.hasNext()) {
       String childPath = iter.next();
 
       // Since this is a direct child of the previous from, only the last token needs to
       // be appended to "to"
       record.addAll(moveWithChildren(childPath,
           to.concat(childPath.substring(childPath.lastIndexOf("/")))));
     }
 
     record.add(new ActionRecord(from, to));
     return record;
   }
 
     // TODO: Unit test
     public void link(String from, String to) throws AccessDeniedException, StorageClientException {
         // a link places a pointer to the content in the parent of from, but
         // does not delete or modify the structure of to.
         // read from is required and write to.
         checkOpen();
         accessControlManager.check(Security.ZONE_CONTENT, to, Permissions.CAN_READ);
         accessControlManager.check(Security.ZONE_CONTENT, from,
                 Permissions.CAN_READ.combine(Permissions.CAN_WRITE));
         Map<String, Object> toStructure = getCached(keySpace, contentColumnFamily, to);
         if (toStructure == null || toStructure.size() == 0) {
             throw new StorageClientException("The source content to link from " + to
                     + " does not exist, link operation failed");
         }
         Map<String, Object> fromStructure = getCached(keySpace, contentColumnFamily, from);
         if (fromStructure != null && fromStructure.size() > 0) {
             throw new StorageClientException("The destination content to link to " + from
                     + "  exists, link operation failed");
         }
 
         if (StorageClientUtils.isRoot(from)) {
             throw new StorageClientException("The link " + to
                     + "  is a root, not possible to create a soft link");
         }
 
         // create a new structure object pointing back to the shared location
 
         Object idStore = toStructure.get(STRUCTURE_UUID_FIELD);
         // if not a root, modify the new parent location, creating the
         // structured if necessary
         String parent = StorageClientUtils.getParentObjectPath(from);
         Map<String, Object> parentToStructure = getCached(keySpace, contentColumnFamily, parent);
         if (parentToStructure == null || parentToStructure.size() == 0) {
             // create a new parent
             Content content = new Content(parent, null);
             update(content);
         }
 
         // create the new object for the path, pointing to the Object
         putCached(keySpace, contentColumnFamily, from, ImmutableMap.of(STRUCTURE_UUID_FIELD,
                 idStore, PATH_FIELD, from, LINKED_PATH_FIELD, to), true);
 
     }
 
     public String saveVersion(String path) throws StorageClientException, AccessDeniedException {
         checkOpen();
         accessControlManager.check(Security.ZONE_CONTENT, path, Permissions.CAN_WRITE);
         Map<String, Object> structure = getCached(keySpace, contentColumnFamily, path);
         String contentId = (String)structure.get(STRUCTURE_UUID_FIELD);
         Map<String, Object> saveVersion = getCached(keySpace, contentColumnFamily, contentId);
 
         // versionHistoryId is the UUID of the version history for this node.
 
         String saveVersionId = (String)saveVersion.get(UUID_FIELD);
         
         String versionHistoryId = (String)saveVersion.get(VERSION_HISTORY_ID_FIELD);
 
         if (versionHistoryId == null) {
             versionHistoryId = StorageClientUtils.getInternalUuid();
             LOGGER.debug("Created new Version History UUID as {} for Object {} ",versionHistoryId, saveVersionId);
             saveVersion.put(VERSION_HISTORY_ID_FIELD, versionHistoryId);
         } else {
             LOGGER.debug("Created new Version History UUID as {} for Object {} ",versionHistoryId, saveVersionId);
             
         }
 
         Map<String, Object> newVersion = Maps.newHashMap(saveVersion);
         String newVersionId = StorageClientUtils.getInternalUuid();
 
 
         String saveBlockId = (String)saveVersion.get(BLOCKID_FIELD);
 
         newVersion.put(UUID_FIELD, newVersionId);
         newVersion.put(PREVIOUS_VERSION_UUID_FIELD, saveVersionId);
         if (saveBlockId != null) {
             newVersion.put(PREVIOUS_BLOCKID_FIELD, saveBlockId);
         }
 
         saveVersion.put(NEXT_VERSION_FIELD, newVersionId);
         saveVersion.put(READONLY_FIELD, TRUE);
         Object versionNumber = System.currentTimeMillis();
         saveVersion.put(VERSION_NUMBER_FIELD, versionNumber);
 
         putCached(keySpace, contentColumnFamily, saveVersionId, saveVersion, false);
         putCached(keySpace, contentColumnFamily, newVersionId, newVersion, true);
         putCached(keySpace, contentColumnFamily, versionHistoryId,
                 ImmutableMap.of(saveVersionId, versionNumber), true);
         putCached(keySpace, contentColumnFamily, path,
                 ImmutableMap.of(STRUCTURE_UUID_FIELD, (Object)newVersionId), true);
         if ( LOGGER.isDebugEnabled() ) {
             LOGGER.debug("Saved Version History  {} {} ", versionHistoryId,
                     getCached(keySpace, contentColumnFamily, versionHistoryId));
             LOGGER.debug("Saved Version [{}] {}", saveVersionId, saveVersion);
             LOGGER.debug("New Version [{}] {}", newVersionId, newVersion);
             LOGGER.debug("Structure {} ", getCached(keySpace, contentColumnFamily, path));
         }
         return saveVersionId;
     }
 
     public List<String> getVersionHistory(String path) throws AccessDeniedException,
             StorageClientException {
         checkOpen();
         accessControlManager.check(Security.ZONE_CONTENT, path, Permissions.CAN_READ);
         Map<String, Object> structure = getCached(keySpace, contentColumnFamily, path);
         if (structure != null && structure.size() > 0) {
             String contentId = (String)structure.get(STRUCTURE_UUID_FIELD);
             Map<String, Object> content = getCached(keySpace, contentColumnFamily, contentId);
             if (content != null && content.size() > 0) {
                 String versionHistoryId = (String)content
                         .get(VERSION_HISTORY_ID_FIELD);
                 if (versionHistoryId != null) {
                     final Map<String, Object> versionHistory = getCached(keySpace,
                             contentColumnFamily, versionHistoryId);
                     LOGGER.debug("Loaded Version History  {} {} ", versionHistoryId, versionHistory);
                     versionHistory.remove(UUID_FIELD);
                   return Ordering.from(new Comparator<String>() {
                       public int compare(String o1, String o2) {
                         long l1 = (Long) versionHistory.get(o1);
                         long l2 = (Long) versionHistory.get(o2);
                         long r = l2 - l1;
                         if (r == 0) {
                           return 0;
                         } else if (r < 0) {
                           return -1;
                         }
                         return 1;
                       }
                     }).sortedCopy(versionHistory.keySet());
                 }
             }
         }
         return Collections.emptyList();
     }
 
     // TODO: Unit test
     public InputStream getVersionInputStream(String path, String versionId)
             throws AccessDeniedException, StorageClientException, IOException {
         return getVersionInputStream(path, versionId, null);
     }
 
     // TODO: Unit test
     public InputStream getVersionInputStream(String path, String versionId, String streamId)
             throws AccessDeniedException, StorageClientException, IOException {
         accessControlManager.check(Security.ZONE_CONTENT, path, Permissions.CAN_READ);
         checkOpen();
         Map<String, Object> structure = getCached(keySpace, contentColumnFamily, path);
         if (structure != null && structure.size() > 0) {
             String contentId = (String)structure.get(STRUCTURE_UUID_FIELD);
             Map<String, Object> content = getCached(keySpace, contentColumnFamily, contentId);
             if (content != null && content.size() > 0) {
                 String versionHistoryId = (String)content
                         .get(VERSION_HISTORY_ID_FIELD);
                 if (versionHistoryId != null) {
                     Map<String, Object> versionHistory = getCached(keySpace, contentColumnFamily,
                             versionHistoryId);
                     if (versionHistory != null && versionHistory.containsKey(versionId)) {
                         return internalGetInputStream(versionId, streamId);
                     }
                 }
             }
         }
         return null;
     }
 
     public Content getVersion(String path, String versionId) throws StorageClientException,
             AccessDeniedException {
         checkOpen();
         accessControlManager.check(Security.ZONE_CONTENT, path, Permissions.CAN_READ);
         Map<String, Object> structure = getCached(keySpace, contentColumnFamily, path);
         if (structure != null && structure.size() > 0) {
             String contentId = (String)structure.get(STRUCTURE_UUID_FIELD);
             Map<String, Object> content = getCached(keySpace, contentColumnFamily, contentId);
             if (content != null && content.size() > 0) {
                 String versionHistoryId = (String)content
                         .get(VERSION_HISTORY_ID_FIELD);
                 if (versionHistoryId != null) {
                     Map<String, Object> versionHistory = getCached(keySpace, contentColumnFamily,
                             versionHistoryId);
                     if (versionHistory != null && versionHistory.containsKey(versionId)) {
                         Map<String, Object> versionContent = getCached(keySpace,
                                 contentColumnFamily, versionId);
                         if (versionContent != null && versionContent.size() > 0) {
                             Content contentObject = new Content(path, versionContent);
                             ((InternalContent) contentObject).internalize(this, true);
                             return contentObject;
                         } else {
                             LOGGER.debug("No Content for path {} version History Null{} ", path,
                                     versionHistoryId);
 
                         }
                     } else {
                         LOGGER.debug("History null for path {} version History {} {} ",
                                 new Object[] { path, versionHistoryId, versionHistory });
                     }
                 } else {
                     LOGGER.debug("History Id null for path {} ", path);
                 }
             }
         }
         return null;
     }
 
     @Override
     protected Logger getLogger() {
         return LOGGER;
     }
 
     public Iterable<Content> find(Map<String, Object> searchProperties) throws StorageClientException,
         AccessDeniedException {
       checkOpen();
       final Map<String, Object> finalSearchProperties = searchProperties;
       return new Iterable<Content>() {
 
         public Iterator<Content> iterator() {
             Iterator<Content> contentResultsIterator = null;
             try {
               final DisposableIterator<Map<String,Object>> clientSearchKeysIterator = client.find(keySpace, contentColumnFamily, finalSearchProperties, ContentManagerImpl.this);
               contentResultsIterator = new PreemptiveIterator<Content>() {
                   Content contentResult;
 
                   protected boolean internalHasNext() {
                       contentResult = null;
                       while (contentResult == null && clientSearchKeysIterator.hasNext()) {
                           try {
                               Map<String, Object> structureMap = clientSearchKeysIterator.next();
                               LOGGER.debug("Loaded Next as {} ", structureMap);
                               if ( structureMap != null && structureMap.size() > 0 ) {
                                   String path = (String) structureMap.get(PATH_FIELD);
                                   contentResult = get(path);
                               }
                           } catch (AccessDeniedException e) {
                               LOGGER.debug(e.getMessage(),e);
                           } catch (StorageClientException e) {
                               LOGGER.debug(e.getMessage(),e);
                           }
                       }
                       if (contentResult == null) {
                           close();
                           return false;
                       }
                       return true;
                   }
 
                   protected Content internalNext() {
                       return contentResult;
                   }
                   
                   @Override
                   public void close() {
                       clientSearchKeysIterator.close();
                       super.close();
                   };
               };
             } catch (StorageClientException e) {
               LOGGER.error("Unable to iterate over sparsemap search results.", e);
             }
             return contentResultsIterator;
         }
     };
     }
     
     public int count(Map<String, Object> countSearch) throws StorageClientException {
         Builder<String, Object> b = ImmutableMap.builder();
         b.putAll(countSearch);
         b.put(StorageConstants.CUSTOM_STATEMENT_SET, "countestimate");
         b.put(StorageConstants.RAWRESULTS, true);
         DisposableIterator<Map<String,Object>> counts = client.find(keySpace, contentColumnFamily, b.build(), ContentManagerImpl.this);
         try {
             Map<String, Object> count = counts.next();
             return Integer.parseInt(String.valueOf(count.get("1")));
         } finally {
             if ( counts != null ) {
                 counts.close();
             }
         }
     }
 
 
     public boolean hasBody(String path, String streamId) throws StorageClientException, AccessDeniedException {
         Content content = get(path);
         return client.hasBody(content.getProperties(), streamId);
     }
 
     public void setPrincipalTokenResolver(PrincipalTokenResolver principalTokenResolver) {
         accessControlManager.setRequestPrincipalResolver(principalTokenResolver);
     }
 
     public void cleanPrincipalTokenResolver() {
         accessControlManager.clearRequestPrincipalResolver();
     }
 
 
 }
