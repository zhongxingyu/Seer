 /*
  * Copyright (C) 2003-2011 eXo Platform SAS.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.etk.storage.plugins.cache;
 
 import java.io.Serializable;
 
 import org.etk.storage.plugins.cache.loader.Loader;
import org.exoplatform.services.cache.ExoCache;
 
 /**
  * Created by The eXo Platform SAS
  * Author : eXoPlatform
  *          exo@exoplatform.com
  * Jul 22, 2011  
  */
 public class FutureETKCache <K extends Serializable, V, C> extends FutureCache<K, V, C> {
 
   private final ExoCache<K, V> cache;
   
   public FutureETKCache(Loader<K, V, C> loader, ExoCache<K, V> cache) {
     super(loader);
     this.cache = cache;
     
   }
   
   public void remove(K key) {
     cache.remove(key);
   }
   
   public void clear() {
     cache.clearCache();
   }
   @Override
   protected V get(K key) {
     return cache.get(key);
   }
 
   @Override
   protected void put(K key, V entry) {
     cache.put(key, entry);
   }
   
 
 }
