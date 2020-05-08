 package com.amm.stores.provider;
 
 import java.util.*;
 import com.amm.stores.api.dto.stores.Store;
 
 /**
  * Mock provider storing FigServer objects.
  */
 public class MockStoreProvider implements StoreProvider {
 	private Map<String,Store> map = new HashMap<String,Store>();
 
 	public void upsert(Store store) throws Exception {
 		map.put(store.getStoreId(),store);
		return store;
 	}
 
 	public void delete(String storeId) throws Exception {
 		map.remove(storeId);
 	}
 
 	public Store get(String storeId) throws Exception {
 		return map.get(storeId);
 	}
 }
