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
 
import org.etk.storage.plugins.cache.model.data.FooData;
 import org.etk.storage.plugins.cache.model.data.BarData;
 import org.etk.storage.plugins.cache.model.data.IntegerData;
 import org.etk.storage.plugins.cache.model.data.ListFoosData;
 import org.etk.storage.plugins.cache.model.key.FooFilterKey;
 import org.etk.storage.plugins.cache.model.key.FooKey;
 import org.etk.storage.plugins.cache.model.key.ListFoosKey;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
 
 /**
  * Created by The eXo Platform SAS
  * Author : eXoPlatform
  *          exo@exoplatform.com
  * Jul 22, 2011  
  */
 public class FactoryStorageCacheService {
 
   //FooStorage
   private final ExoCache<FooKey, FooData> fooCache;
   private final ExoCache<FooKey, BarData> barCache;
   private final ExoCache<FooFilterKey, IntegerData> foosCountCache;
   private final ExoCache<ListFoosKey, ListFoosData> foosCache;
   
   public FactoryStorageCacheService(CacheService cacheService) {
     this.fooCache = CacheType.FOO.getFromService(cacheService);
     this.barCache = CacheType.BAR.getFromService(cacheService);
     this.foosCountCache = CacheType.FOOS_COUNT.getFromService(cacheService);
     this.foosCache = CacheType.FOOS.getFromService(cacheService);
   }
   
   /**
    * Gets the foo cache.
    * @return
    */
   public ExoCache<FooKey, FooData> getFooCache() {
     return this.fooCache;
   }
   
   /**
    * Gets BarCache which depends on the Foo.
    * @return
    */
   public ExoCache<FooKey, BarData> getBarCache() {
     return this.barCache;
   }
   
   /**
    * Gets FoosCountCache
    * @return
    */
   public ExoCache<FooFilterKey, IntegerData> getFoosCountCache() {
     return this.foosCountCache;
   }
   /**
    * Gets FoosCache
    * @return
    */
   public ExoCache<ListFoosKey, ListFoosData> getFoosCache() {
     return this.foosCache;
   }
   
 }
