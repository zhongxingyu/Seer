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
 
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToMany;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 
 import org.hibernate.annotations.IndexColumn;
 import org.json.JSONException;
 import org.json.extension.JSONConstructor;
 import org.json.extension.JSONEncoder;
 
 import com.google.inject.internal.Lists;
 
 import de.cosmocode.json.JSON;
import de.cosmocode.json.JSONMapable;
 import de.cosmocode.json.JSONRenderer;
 import de.cosmocode.palava.bridge.content.ContentConverter;
 import de.cosmocode.palava.bridge.content.ConversionException;
 import de.cosmocode.palava.bridge.content.Convertible;
 import de.cosmocode.palava.bridge.content.KeyValueState;
 import de.cosmocode.palava.media.directory.DirectoryBase;
import de.cosmocode.rendering.Renderer;
import de.cosmocode.rendering.RenderingException;
import de.cosmocode.rendering.RenderingLevel;
 
 /**
  * A directory is just a collection/list of Asset Ids.
  * Asset ids may be contained in different directories at the same time.
  * 
  * @deprecated use {@link DirectoryBase} or {@link AbstractDirectory} instead
  * 
  * @author huettemann
  * @author Willi Schoenborn
  */
 @Deprecated
 @NamedQueries({
     @NamedQuery(
         name = Directory.BY_ASSET_ID,
         query = 
             "select " +
                 "d.id, " +
                 "d.name " +
             "from " +
                 "Directory d " +
             "inner join " +
                 "d.assets a " +
             "where " +
                 "a.id = :assetId"
     )
 })
 @Entity
public class Directory implements DirectoryBase, JSONMapable, JSONEncoder, Convertible, Iterable<Asset> {
 
     public static final String BY_ASSET_ID = "Directory by Asset id";
     
     @Id
     @GeneratedValue(generator = "entity_id_gen", strategy = GenerationType.TABLE)
     private Long id;
 
     private String name;
 
     @ManyToMany(fetch = FetchType.LAZY)
     @JoinColumn(unique = true)
     @IndexColumn(name = "dirIdx", nullable = false, base = 0)
     private List<Asset> assets = Lists.newArrayList();
 
     @Override
     public long getId() {
         return id;
     }
     
     @Override
     public Date getCreatedAt() {
         throw new UnsupportedOperationException();
     }
     
     @Override
     public void setCreatedAt(Date createdAt) {
         throw new UnsupportedOperationException();
     }
     
     @Override
     public void setCreated() {
         throw new UnsupportedOperationException();
     }
     
     @Override
     public Date getModifiedAt() {
         throw new UnsupportedOperationException();
     }
     
     @Override
     public void setModifiedAt(Date modifiedAt) {
         throw new UnsupportedOperationException();
     }
     
     @Override
     public void setModified() {
         throw new UnsupportedOperationException();
     }
     
     @Override
     public Date getDeletedAt() {
         throw new UnsupportedOperationException();
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
     public List<Asset> getAssets() {
         // TODO forward list and intercept add methods or use UniqueList
         return assets;
     }
     
     public void setAssets(List<Asset> assets) {
         this.assets = assets;
     }
     
     /**
      * @deprecated use {@link Directory#getAssets()} and {@link List#add(Object)} instead
      * 
      * @param asset
      */
     @Deprecated
     public void addAsset(Asset asset) {
         // only add the asset to this directory if it doesn't already contain it
         if (!this.assets.contains(asset))
             this.assets.add(asset);
     }
     
     public void setId(long id) {
         this.id = id;
     }
     
     public void setName(String name) {
         this.name = name;
     }
     
     public String getName() {
         return name;
     }
 
     public void sort(Comparator<Asset> comparator) {
         Collections.sort(assets, comparator);
     }
 
     @Override
    public void render(Renderer renderer, RenderingLevel level) throws RenderingException {
        throw new UnsupportedOperationException();
    }
    
    @Override
     public void encodeJSON(JSONConstructor json) throws JSONException {
         json.array();            
         for (Asset asset : assets) {
             json.object();
             asset.encodeJSON(json);
             json.endObject();
         }
         json.endArray();
     }
     
     @Override
     public void convert(StringBuffer buf, ContentConverter converter) throws ConversionException {
         converter.convertKeyValue(buf, "name", name, KeyValueState.START);
         converter.convertKeyValue(buf, "assets", assets, KeyValueState.LAST);
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
     
     public int getSize() {
         return assets != null ? assets.size() : 0;
     }
     
     public boolean isEmpty() {
         return getSize() == 0;
     }
     
     @Override
     public Iterator<Asset> iterator() {
         return assets == null ? new LinkedList<Asset>().iterator() : assets.iterator();
     }
     
 }
