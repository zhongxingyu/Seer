 /**
  * Copyright 2010 CosmoCode GmbH
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
 
 package de.cosmocode.palava.services.media;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.sql.SQLException;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.Map;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.extension.JSONConstructor;
 import org.json.extension.JSONEncoder;
 
 import com.google.common.collect.ImmutableSet;
 
 import de.cosmocode.json.JSON;
 import de.cosmocode.json.JSONRenderer;
 import de.cosmocode.palava.bridge.MimeType;
 import de.cosmocode.palava.bridge.content.ContentConverter;
 import de.cosmocode.palava.bridge.content.ConversionException;
 import de.cosmocode.palava.bridge.content.Convertible;
 import de.cosmocode.palava.bridge.content.KeyValueState;
 import de.cosmocode.palava.bridge.content.StreamContent;
 import de.cosmocode.palava.media.asset.AbstractAsset;
 import de.cosmocode.palava.media.asset.AssetBase;
 import de.cosmocode.palava.media.directory.DirectoryBase;
 import de.cosmocode.palava.model.base.Copyable;
 import de.cosmocode.palava.model.base.EntityBase;
 
 /**
  * 
  * 
  * @deprecated use {@link AssetBase} or {@link AbstractAsset} instead
  *
  * @author Willi Schoenborn
  */
 @Deprecated
 @Entity
 public class Asset implements AssetBase, Copyable<Asset>, JSONEncoder, Convertible {
     
     /**
      * @deprecated use {@code EntityBase.ORDER_BY_AGE.reverse()} instead
      *
      * @author Willi Schoenborn
      */
     @Deprecated
     public static class ByCreationDateComparator implements Comparator<Asset> {
         
         public static final ByCreationDateComparator INSTANCE = new ByCreationDateComparator();
         
         @Override
         public int compare(Asset a, Asset b) {
             // null is less than 
             if (a == null && b == null) return 0;
             if (a == null) return -1;
             if (b == null) return 1;
             return - a.creationDate.compareTo(b.creationDate);
         }
 
     };
 
     @Id
     @GeneratedValue(generator = "entity_id_gen", strategy = GenerationType.TABLE)
     private Long id;
     
     private String storeKey;
     
     private String mime;
     
     private long length;
     
     private transient StreamContent content;
     
     private String name;
     
     private String title;
     
     private String description;
     
     private Date creationDate = new Date();
     
     private Date modificationDate = new Date();
     
     private Date expirationDate;
     
     @Column(nullable = false)
     private boolean expiresNever;
 
     /**
      * JSON Object.
      * {
      *  "key" : "value",
      *  "key" : "value"
      * }
      */
     private String metaData;
 
     @Override
     public long getId() {
         return id;
     }
 
     @Override
     public Date getCreatedAt() {
         return getCreationDate();
     }
     
     @Override
     public void setCreatedAt(Date createdAt) {
         setCreationDate(createdAt);
     }
     
     @Override
     public void setCreated() {
         setCreatedAt(new Date());
     }
     
     @Override
     public Date getModifiedAt() {
         return getModificationDate();
     }
     
     @Override
     public void setModifiedAt(Date modifiedAt) {
         setModificationDate(modifiedAt);
     }
     
     @Override
     public void setModified() {
         setModifiedAt(new Date());
     }
     
     @Override
     public Date getDeletedAt() {
         return null;
     }
     
     @Override
     public void setDeletedAt(Date deletedAt) {
         throw new UnsupportedOperationException();
     }
     
     @Override
     public void setDeleted() {
         throw new UnsupportedOperationException();
     }
     
     @Override
     public boolean isDeleted() {
         return false;
     }
 
     @Override
     public String getName() {
         return name;
     }
 
     @Override
     public void setName(String name) {
         this.name = name;
     }
 
     @Override
     public String getStoreIdentifier() {
         return storeKey;
     }
     
     @Override
     public void setStoreIdentifier(String storeIdentifier) {
         this.storeKey = storeIdentifier;
     }
     
     @Override
     public String getTitle() {
         return title;
     }
 
     @Override
     public void setTitle(String title) {
         this.title = title;
     }
 
     @Override
     public String getDescription() {
         return description;
     }
 
     @Override
     public void setDescription(String description) {
         this.description = description;
     }
     
     @Override
     public Map<String, String> getMetaData() {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public ImmutableSet<? extends DirectoryBase> getDirectories() {
         throw new UnsupportedOperationException();
     }
     
     @Override
     public Date getExpiresAt() {
         return getExpirationDate();
     }
     
     @Override
     public void setExpiresAt(Date expiresAt) {
         setExpirationDate(expiresAt);
     }
     
     @Override
     public boolean isExpirable() {
         return !expiresNever;
     }
     
     @Override
     public boolean isExpired() {
         if (expirationDate == null) {
             return !expiresNever;
         } else {
             return expiresNever ? false : expirationDate.before(new Date());
         }
     }
     
     @Override
     public void setExpired(boolean expired) {
         throw new UnsupportedOperationException();
     }
     
     @Override
     public boolean isExpiring() {
         throw new UnsupportedOperationException();
     }
     
     @Override
     public boolean isUnexpiring() {
         throw new UnsupportedOperationException();
     }
     
     public Date getCreationDate() {
         return creationDate;
     }
 
     public void setCreationDate(Date creationDate) {
         this.creationDate = creationDate;
     }
 
     public Date getModificationDate() {
         return modificationDate;
     }
 
     public void setModificationDate(Date modificationDate) {
         this.modificationDate = modificationDate;
     }
 
     public StreamContent getContent() {
         return content;
     }
     
     /**
      * 
      * 
      * @param content
      * @throws SQLException
      * @throws IOException
      */
     public void setContent(StreamContent content) throws SQLException, IOException {
         this.content = content;
         
         this.mime = content.getMimeType() == null ? MimeType.IMAGE.toString() : content.getMimeType().toString();
         this.length = content.getLength();
     }
     
     @Override
     public InputStream getStream() {
         throw new UnsupportedOperationException();
     }
     
     @Override
     public void setStream(InputStream stream) {
         throw new UnsupportedOperationException();        
     }
 
     public void setId(long id) {
         this.id = id;
     }
 
     /**
      * Retrieves the store key.
      * 
      * @deprecated use {@link Asset#getStoreIdentifier()} instead
      * @return the current store key
      */
     @Deprecated
     public String getStoreKey() {
         return getStoreIdentifier();
     }
 
     /**
      * Sets the store key.
      * 
      * @deprecated use {@link Asset#setStoreIdentifier(String)} instead.
      * @param storeKey the new storeKey
      */
     @Deprecated
     public void setStoreKey(String storeKey) {
         setStoreIdentifier(storeKey);
     }
 
     public Date getExpirationDate() {
         return expirationDate;
     }
 
     public void setExpirationDate(Date expirationDate) {
         this.expirationDate = expirationDate;
     }
 
     public boolean isExpiresNever() {
         return expiresNever;
     }
 
     public boolean getExpiresNever() {
         return expiresNever;
     }
 
     public void setExpiresNever(boolean expiresNever) {
         this.expiresNever = expiresNever;
     }
 
     public void fillMetaData(Map<String, ?> map) throws JSONException {
         if (metaData == null) metaData = "{}";
         
         JSONObject json = new JSONObject(metaData);
         for (String key : map.keySet()) {
             json.put(key, map.get(key));
         }
         
         metaData = json.toString();
     }
     
     public boolean isExpired(boolean oldStyleCheck) {
         if (oldStyleCheck) {
             return expirationDate != null && expirationDate.before(new Date());
         } else {
             return isExpired();
         }
     }
     
     @Override
     public Asset copy() {
         final Asset asset = new Asset();
         
         asset.setCreatedAt(this.getCreatedAt());
         asset.setModifiedAt(this.getModifiedAt());
         asset.setExpiresAt(this.getExpiresAt());
         asset.setExpiresNever(this.getExpiresNever());
         asset.metaData = this.metaData;
         asset.setName(this.getName());
         asset.setTitle(this.getTitle());
         asset.setDescription(this.getDescription());
         
         try {
             asset.setContent(this.getContent());
         } catch (SQLException e) {
             throw new IllegalArgumentException(e);
         } catch (IOException e) {
             throw new IllegalStateException(e);
         }
         
         return asset;
     }
 
     /**
      * object() and endObject() moved to parent context
      */
     public void encodeJSON(JSONConstructor json) throws JSONException 
     {
         json.key("id").value(id);
         json.key("storeKey").value(storeKey);
         json.key("creationDate").value(creationDate == null ? null : creationDate.getTime() / 1000);
         json.key("modificationDate").value(modificationDate == null ? null : modificationDate.getTime() / 1000);
         if ( name != null ) json.key("name").value(name);
         if ( title != null ) json.key("title").value(title);
         if ( description != null ) json.key("description").value(description);
         json.key("mimetype").value(mime);       
         json.key("size").value(length);
         
         json.key("expirationDate").value(expirationDate == null ? null : expirationDate.getTime() / 1000);
         json.key("expired").value(isExpired(true));        
         json.key("expiresNever").value(expiresNever);
         
         if (metaData != null) json.key("metaData").plain(metaData);
     }
 
     public void convert( StringBuffer buf, ContentConverter converter ) throws ConversionException
     {
         converter.convertKeyValue (buf, "id", id, KeyValueState.START);
         converter.convertKeyValue (buf, "storeKey", storeKey, KeyValueState.INSIDE);
         converter.convertKeyValue (buf, "creationDate", creationDate == null ? null : creationDate.getTime() / 1000, KeyValueState.INSIDE);
         converter.convertKeyValue (buf, "modificationDate", modificationDate == null ? null : modificationDate.getTime() / 1000, KeyValueState.INSIDE);
         converter.convertKeyValue (buf, "name", name, KeyValueState.INSIDE);
         converter.convertKeyValue (buf, "title", title, KeyValueState.INSIDE);
         converter.convertKeyValue (buf, "expirationDate", expirationDate == null ? null : expirationDate.getTime() / 1000, KeyValueState.INSIDE);
         converter.convertKeyValue(buf, "expired", isExpired(true), KeyValueState.INSIDE);
         converter.convertKeyValue(buf, "expiresNever", expiresNever, KeyValueState.INSIDE);
         converter.convertKeyValue(buf, "metaData", metaData, KeyValueState.INSIDE);
         converter.convertKeyValue (buf, "description", description, KeyValueState.LAST);
     }
 
     @Override
     public JSONRenderer renderAsMap(JSONRenderer renderer) {
         try {
             encodeJSON(JSON.asJSONConstructor(renderer));
         } catch (JSONException e) {
             throw new IllegalStateException(e);
         }
         return renderer;
     }
     
     @Override
     public int hashCode() {
         final int prime = 31;
         int result = 1;
         result = prime * result + ((id == null) ? 0 : id.hashCode());
         return result;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (this == obj) {
             return true;
         }
         if (obj == null) {
             return false;
         }
         if (!(obj instanceof Asset)) {
             return false;
         }
         final Asset other = (Asset) obj;
         if (id == null) {
             if (other.id != null) {
                 return false;
             }
         } else if (!id.equals(other.id)) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString() {
         return "[" + getId() + "] " + name;
     }
 
 }
