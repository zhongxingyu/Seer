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
 package org.etk.storage.core.impl;
 
 import java.util.ArrayList;
 import java.util.List;
 
import org.etk.kernel.cache.ExoCache;
 import org.etk.service.foo.FooFilter;
 import org.etk.service.foo.model.Bar;
 import org.etk.service.foo.model.Foo;
 import org.etk.storage.api.FooStorage;
 import org.etk.storage.core.impl.jcr.FooStorageImpl;
 import org.etk.storage.plugins.cache.CacheType;
 import org.etk.storage.plugins.cache.FactoryStorageCacheService;
 import org.etk.storage.plugins.cache.FutureETKCache;
 import org.etk.storage.plugins.cache.loader.ServiceContext;
 import org.etk.storage.plugins.cache.model.data.BarData;
 import org.etk.storage.plugins.cache.model.data.FooData;
 import org.etk.storage.plugins.cache.model.data.IntegerData;
 import org.etk.storage.plugins.cache.model.data.ListFoosData;
 import org.etk.storage.plugins.cache.model.key.FooFilterKey;
 import org.etk.storage.plugins.cache.model.key.FooKey;
 import org.etk.storage.plugins.cache.model.key.ListFoosKey;

 
 /**
  * Created by The eXo Platform SAS
  * Author : eXoPlatform
  *          exo@exoplatform.com
  * Jul 22, 2011  
  */
 public class CachedFooStorage implements FooStorage {
 
   private final ExoCache<FooKey, FooData> etkFooCache;
   private final ExoCache<FooKey, BarData> etkBarCache;
   private final ExoCache<FooFilterKey, IntegerData> etkFoosCountCache;
   private final ExoCache<ListFoosKey, ListFoosData> etkFoosCache;
   
   
   private final FutureETKCache<FooKey, FooData, ServiceContext<FooData>> fooCache;
   private final FutureETKCache<FooKey, BarData, ServiceContext<BarData>> barCache;
   private final FutureETKCache<FooFilterKey, IntegerData, ServiceContext<IntegerData>> foosCountCache;
   private final FutureETKCache<ListFoosKey, ListFoosData, ServiceContext<ListFoosData>> foosCache;
   
   
   private final FooStorageImpl storage;
   
   /**
    * Build the Foo list from the caches Ids.
    * 
    * @param data ids
    * @return foos
    */
   private List<Foo> buildFoos(ListFoosData data) {
     List<Foo> foos = new ArrayList<Foo>();
     for (FooKey k : data.getIds()) {
       Foo got = findById(k.getId());
       foos.add(got);
     }
     return foos;
   }
   
   /**
    * Build the list of FooKey provides ListFoosData.
    * @param foos
    * @return
    */
   private ListFoosData buildIds(List<Foo> foos) {
     List<FooKey> fooKeys = new ArrayList<FooKey>();
     for (Foo foo : foos) {
       FooKey key = new FooKey(foo);
       this.etkFooCache.put(key, new FooData(foo));
       this.etkBarCache.put(key, new BarData(foo.getBar()));
       fooKeys.add(new FooKey(foo));
     }
     return new ListFoosData(fooKeys);
   }
   
   public CachedFooStorage(final FooStorageImpl storage, final FactoryStorageCacheService cacheService) {
     this.storage = storage;
     this.storage.setStorage(this);
     //Initializes the eXoCache which is managed by CacheService
     this.etkFooCache = cacheService.getFooCache();
     this.etkBarCache = cacheService.getBarCache();
     this.etkFoosCountCache = cacheService.getFoosCountCache();
     this.etkFoosCache = cacheService.getFoosCache();
     
     //Initializes the EtkFutureCache which executes for caching ServiceContext
     this.fooCache = CacheType.FOO.createFutureCache(etkFooCache);
     this.foosCache = CacheType.FOOS.createFutureCache(etkFoosCache);
     this.barCache = CacheType.BAR.createFutureCache(etkBarCache);
     this.foosCountCache = CacheType.FOOS_COUNT.createFutureCache(etkFoosCountCache);
   }
   
   @Override
   public List<Foo> getFooByFilter(final FooFilter fooFilter, final int offset, final int limit) {
     FooFilterKey key = new FooFilterKey(fooFilter);
     ListFoosKey listKey = new ListFoosKey(key, offset, limit);
     
     ListFoosData keys = foosCache.get(new ServiceContext<ListFoosData>() {
 
       @Override
       public ListFoosData execute() {
         List<Foo> got = storage.getFooByFilter(fooFilter, offset, limit);
         return buildIds(got);
       }
 
     }, listKey);
     
     return buildFoos(keys);
   }
 
   @Override
   public Foo findById(final String id) {
     FooKey key = new FooKey(new Foo(id));
     Foo foo = this.fooCache.get(new ServiceContext<FooData>() {
       public FooData execute() {
         return new FooData(storage.findById(id));
       }
     }, key).build();
     
     if (foo != null) {
       foo.setBar(loadBar(foo));
     }
     return null;
   }
   
   public Bar loadBar(final Foo foo) {
     FooKey key = new FooKey(new Foo(foo.getId()));
     return barCache.get(new ServiceContext<BarData>() {
 
       @Override
       public BarData execute() {
         return new BarData(storage.loadBar(foo));
       }
       
     }, key).build();
   }
 
   
   
 }
