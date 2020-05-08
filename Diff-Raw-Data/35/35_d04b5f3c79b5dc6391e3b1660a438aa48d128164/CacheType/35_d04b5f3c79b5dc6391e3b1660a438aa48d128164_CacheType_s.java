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
 
 import org.etk.storage.plugins.cache.loader.CacheLoader;
 import org.etk.storage.plugins.cache.loader.ServiceContext;
 import org.etk.storage.plugins.cache.model.key.CacheKey;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
 
 /**
  * Created by The eXo Platform SAS
  * Author : eXoPlatform
  *          exo@exoplatform.com
  * Jul 22, 2011  
  */
 public enum CacheType {
 
   FOO("FooCache"),
   BAR("BarCache"),
   FOOS_COUNT("FoosCountCache"),
   FOOS("FoosCache");
   
   private final String name;
   
   private CacheType(final String name) {
     this.name = name;
   }
   
   /**
    * MethodFactory to create the ExoCache<K,V> instance.
    * ExoCache instance will create base on some information 
    * such as timeLive, capacity, max size.
    */
   public <K extends CacheKey, V extends Serializable> ExoCache<K,V> getFromService(CacheService service) {
     //Create the CacheInstance depend on the ExoCache configuration.
     return service.getCacheInstance(name);
   }
   /**
    * Creates the FutureETKCache base on the CacheLoader and eXoCache.
    * 
    * @param <K> Key
    * @param <V> Value
    * @param cache eXoCache
    * @return FututeETKCache
    */
   public <K extends CacheKey, V extends Serializable> FutureETKCache<K, V, ServiceContext<V>> createFutureCache(ExoCache<K,V> cache) {
     return new FutureETKCache<K, V, ServiceContext<V>>(new CacheLoader<K, V>(), cache);
   }
   
 }
