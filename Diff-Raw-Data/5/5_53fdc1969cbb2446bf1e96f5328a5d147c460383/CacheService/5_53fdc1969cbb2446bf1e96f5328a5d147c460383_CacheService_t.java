 /*
 * Copyright 2010 Bizosys Technologies Limited
 *
 * Licensed to the Bizosys Technologies Limited (Bizosys) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Bizosys licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
 package com.bizosys.hsearch.treetable.cache;
 
 import java.io.IOException;
 
 import com.bizosys.hsearch.hbase.HReader;
 import com.bizosys.hsearch.hbase.NV;
 import com.bizosys.hsearch.hbase.RecordScalar;
 import com.bizosys.hsearch.treetable.storage.CacheStorage;
 import com.bizosys.hsearch.util.BatchProcessor;
 import com.bizosys.hsearch.util.HSearchConfig;
 import com.bizosys.hsearch.util.HSearchLog;
 
 public class CacheService {
 	private static CacheService singleton = null;
 	private static boolean INFO_ENABLED = HSearchLog.l.isInfoEnabled(); 
	private static boolean DEBUG_ENABLED = HSearchLog.l.isDebugEnabled(); 
 	
 	/**
 	 * TODO:// Automatically disable cache service if there are more than 3 exception 
 	 * @return
 	 * @throws IOException
 	 */
 
 	public static CacheService getInstance() throws IOException {
 		if ( null == singleton ) {
 			singleton = new CacheService();
 		}
 		return singleton;
 	}
 	
 	boolean cacheSingleQueryCoproc = false;
 	private CacheService() throws IOException {
 		CacheStorage.getInstance(); //Initializes
 		cacheSingleQueryCoproc = HSearchConfig.getInstance().getConfiguration().getBoolean(
			"cache.singlequery.enabled", true);
		if ( DEBUG_ENABLED ) HSearchLog.l.debug("CacheStorage is " + cacheSingleQueryCoproc);
 	}
 	
 	public void setCacheEnable(boolean isEnabled) {
 		this.cacheSingleQueryCoproc = isEnabled;
 	}
 	
 	public final void put(final String singleQury, final byte[] output) {
 		if ( ! cacheSingleQueryCoproc ) return;
 		CacheStoreClientAsync task = new CacheStoreClientAsync(singleQury, output);
 		BatchProcessor.getInstance().addTask(task);
 	}
 
 	public byte[] get(String singleQury) {
 		if ( ! cacheSingleQueryCoproc ) return null;
 		try {
 			RecordScalar record = new RecordScalar(singleQury.getBytes(), 
 					new NV(CacheStorage.CACHE_COLUMN_BYTES, CacheStorage.CACHE_COLUMN_BYTES));
 			HReader.getScalar(CacheStorage.TABLE_NAME, record);
 			if ( null  != record.kv.data) {
 				if ( INFO_ENABLED ) HSearchLog.l.info("Serving from Cache - " + singleQury);
 				return record.kv.data;
 			}
 		} catch (Exception ex) {
 			HSearchLog.l.warn("Error while saving cache objects:" , ex);
 		}
 		return null;
 	}
 }
