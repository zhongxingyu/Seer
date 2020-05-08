 /*
  * Copyright 2012 INRIA
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
 package com.mymed.controller.core.manager.storage.v2;
 
 import static com.mymed.utils.MiscUtils.decode;
 import static com.mymed.utils.MiscUtils.encode;
 
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.TreeMap;
 
 import org.apache.cassandra.thrift.Column;
 import org.apache.cassandra.thrift.ColumnOrSuperColumn;
 import org.apache.cassandra.thrift.ColumnParent;
 import org.apache.cassandra.thrift.ColumnPath;
 import org.apache.cassandra.thrift.Mutation;
 import org.apache.cassandra.thrift.SlicePredicate;
 import org.apache.cassandra.thrift.SliceRange;
 import org.apache.cassandra.thrift.SuperColumn;
 
 import com.mymed.controller.core.exception.IOBackEndException;
 import com.mymed.controller.core.exception.InternalBackEndException;
 import com.mymed.controller.core.manager.storage.IStorageManager;
 import com.mymed.model.core.configuration.WrapperConfiguration;
 import com.mymed.model.core.wrappers.cassandra.api07.CassandraWrapper;
 import com.mymed.utils.MConverter;
 import com.mymed.utils.MiscUtils;
 
 /**
  * This class represent the DAO pattern: Access to data varies depending on the
  * source of the data. Access to persistent storage, such as to a database,
  * varies greatly depending on the type of storage
  * 
  * Use a Data Access Object (DAO) to abstract and encapsulate all access to the
  * data source. The DAO manages the connection with the data source to obtain
  * and store data.
  * 
  */
 public class StorageManager extends
 		com.mymed.controller.core.manager.storage.StorageManager {
 
     public final int maxNumColumns = 10000; // arbitrary max number of cols, overrides default's 100
 
 	/**
 	 * Default Constructor: will create a ServiceManager on top of a DB Wrapper
 	 */
 	public StorageManager() {
 		this(new WrapperConfiguration(CONFIG_FILE));
 	}
 
 	/**
 	 * will create a ServiceManger on top of the WrapperType And use the
 	 * specific configuration file for the transport layer
 	 * 
 	 * @param conf
 	 *            The configuration of the transport layer
 	 * @throws InternalBackEndException
 	 */
 	public StorageManager(final WrapperConfiguration conf) {
 		super();
 		wrapper = new CassandraWrapper(conf.getCassandraListenAddress(),
 				conf.getThriftPort());
 	}
 	
 	@Override
 	public Map<ByteBuffer, List<ColumnOrSuperColumn>> batch_read(final String tableName,
 			final List<String> keys)throws InternalBackEndException {
 		
 		final SlicePredicate predicate = new SlicePredicate();
 		final SliceRange sliceRange = new SliceRange();
 		sliceRange.setStart(new byte[0]);
 		sliceRange.setFinish(new byte[0]);
 		sliceRange.setCount(maxNumColumns);
 		predicate.setSlice_range(sliceRange);
 
 		final ColumnParent parent = new ColumnParent(tableName);
 		
 		return wrapper.multiget_slice(keys, parent, predicate, consistencyOnRead);
 	}
 
 	@Override
 	public Map<String, Map<String, String>> multiSelectList(
 			final String tableName,
 			final List<String> keys,
 			final String start,
 			final String finish)
 					throws InternalBackEndException {
 
 		final SlicePredicate predicate = new SlicePredicate();
 		final SliceRange sliceRange = new SliceRange();
 		sliceRange.setStart(encode(start));
 		sliceRange.setFinish(encode(finish));
 		sliceRange.setCount(maxNumColumns);
 		predicate.setSlice_range(sliceRange);
 
 		final ColumnParent parent = new ColumnParent(tableName);
 
 		final Map<ByteBuffer, List<ColumnOrSuperColumn>> resultMap = wrapper
 				.multiget_slice(keys, parent, predicate, consistencyOnRead);
 
 		final List<ColumnOrSuperColumn> results = new ArrayList<ColumnOrSuperColumn>();
 
 		for (List<ColumnOrSuperColumn> l : resultMap.values()) {
 			results.addAll(l);
 		}
 
 		final Map<String, Map<String, String>> sliceMap = new TreeMap<String, Map<String, String>>();
 
 		for (final ColumnOrSuperColumn res : results) {
 			if (res.isSetSuper_column()) {
 				final Map<String, String> slice = new HashMap<String, String>();
 
 				for (final Column column : res.getSuper_column().getColumns()) {
 					slice.put(decode(column.getName()),	decode(column.getValue()));
 				}
 				sliceMap.put(decode(res.getSuper_column().getName()), slice);
 			}
 		}
 
 		return sliceMap;
 	}
 	
 	@Override
     public Map<String, String> selectSuperColumn(
             final String tableName, 
             final String key, 
             final String columnName) throws InternalBackEndException 
     {
 
 //		System.out.println("\nREAD: " + tableName + ", " + key + "," + columnName);
 		
     	Map<String, String> resultValue = new HashMap<String, String>();
 
         final ColumnPath colPathName = new ColumnPath(tableName);
         colPathName.setSuper_column(encode(columnName));
 
         LOGGER.info("Selecting column '{}' from table '{}' with key '{}'", new Object[] {columnName, tableName, key});
 
         List<Column> l = wrapper.get(key, colPathName, IStorageManager.consistencyOnRead).getSuper_column().getColumns();
         for (Column c : l){
         	resultValue.put(MiscUtils.decode(c.getName()), MiscUtils.decode(c.getValue()));
         }
 
         LOGGER.info("Column selection performed");
 
         return resultValue;
     }
 	
 	
 	@Override public void insertStr(
             final String key, 
             final ColumnParent parent, 
             final String columnName,
             final String value)
                     throws InternalBackEndException 
     {
         final long timestamp = System.currentTimeMillis();
         final ByteBuffer buffer = ByteBuffer.wrap(encode(value));
         final Column column = new Column(MConverter.stringToByteBuffer(columnName), buffer, timestamp);
         LOGGER.info("Inserting column '{}' into '{}' with key '{}'", new Object[] {columnName, parent.getColumn_family(),
                 key});
         wrapper.insert(key, parent, column, consistencyOnWrite);
 
         LOGGER.info("Column '{}' inserted", columnName);
     }
 	
     @Override public void insertSliceStr(
 			final String tableName,
 			final String primaryKey,
 			final Map<String, String> args) 
 					throws InternalBackEndException {
 		try {
 			final Map<String, Map<String, List<Mutation>>> mutationMap = new HashMap<String, Map<String, List<Mutation>>>();
 			final long timestamp = System.currentTimeMillis();
 			final Map<String, List<Mutation>> tableMap = new HashMap<String, List<Mutation>>();
 			final List<Mutation> sliceMutationList = new ArrayList<Mutation>(5);
 
 			tableMap.put(tableName, sliceMutationList);
 
 			final Iterator<Entry<String, String>> iterator = args.entrySet()
 					.iterator();
 			while (iterator.hasNext()) {
 				final Entry<String, String> entry = iterator.next();
 
 				final Mutation mutation = new Mutation();
 				mutation.setColumn_or_supercolumn(new ColumnOrSuperColumn()
 						.setColumn(new Column(MConverter
 								.stringToByteBuffer(entry.getKey()), ByteBuffer
 								.wrap(encode(entry.getValue())), timestamp)));
 
 				sliceMutationList.add(mutation);
 			}
 
 			// Insertion in the map
 			mutationMap.put(primaryKey, tableMap);
 
 			LOGGER.info(
 					"Performing a batch_mutate on table '{}' with key '{}'",
 					tableName, primaryKey);
 
 			wrapper.batch_mutate(mutationMap, consistencyOnWrite);
 		} catch (final InternalBackEndException e) {
 			LOGGER.debug("Insert slice in table '{}' failed", tableName, e);
 			throw new InternalBackEndException("InsertSlice failed."); // NOPMD
 		}
 
 		LOGGER.info("batch_mutate performed correctly");
 	}
   //@TODO factorize the two below and above
    @Override public void insertSliceStr(
 			final String tableName,
 			final String primaryKey,
 			int ttl,
 			final Map<String, String> args) 
 					throws InternalBackEndException {
 		try {
 			final Map<String, Map<String, List<Mutation>>> mutationMap = new HashMap<String, Map<String, List<Mutation>>>();
 			final long timestamp = System.currentTimeMillis();
 			final Map<String, List<Mutation>> tableMap = new HashMap<String, List<Mutation>>();
 			final List<Mutation> sliceMutationList = new ArrayList<Mutation>(5);
 
 			tableMap.put(tableName, sliceMutationList);
 
 			for (Entry<String, String> entry : args.entrySet()){
 				final Mutation mutation = new Mutation();
 				mutation.setColumn_or_supercolumn(new ColumnOrSuperColumn()
 				.setColumn(new Column(MConverter
 						.stringToByteBuffer(entry.getKey()), ByteBuffer
 						.wrap(encode(entry.getValue())), timestamp)
 					.setTtl(ttl)));
 				sliceMutationList.add(mutation);
 			}
 
 			// Insertion in the map
 			mutationMap.put(primaryKey, tableMap);
 
 			LOGGER.info(
 					"Performing a batch_mutate on table '{}' with key '{}'",
 					tableName, primaryKey);
 
 			wrapper.batch_mutate(mutationMap, consistencyOnWrite);
 		} catch (final InternalBackEndException e) {
 			LOGGER.debug("Insert slice in table '{}' failed", tableName, e);
 			throw new InternalBackEndException("InsertSlice failed."); // NOPMD
 		}
 
 		LOGGER.info("batch_mutate performed correctly");
 	}
 	
 	@Override public Map<String, String> selectAllStr(
 			String tableName,
 			String key)
 					throws InternalBackEndException {
 		
 		// Prepare output
 		HashMap<String, String> res = new HashMap<String, String>();
 
 		final SlicePredicate predicate = new SlicePredicate();
 		final SliceRange sliceRange = new SliceRange();
 		sliceRange.setStart(new byte[0]);
 		sliceRange.setFinish(new byte[0]);
 		predicate.setSlice_range(sliceRange);
 		final ColumnParent parent = new ColumnParent(tableName);
 		final List<ColumnOrSuperColumn> columns = wrapper.get_slice(key,
 				parent, predicate, consistencyOnRead);
 
 		LOGGER.info("Select slice from column family '{}' with key '{}'",
 				parent.getColumn_family(), key);
 
 		for (final ColumnOrSuperColumn c : columns) {
 			final Column col = c.getColumn();
 			res.put(decode(col.getName()), decode(col.getValue()));
 		}
 		return res;
 	}
 	
 	@Override public void insertSuperSliceStr(
 			final String superTableName,
 			final String key,
 			final String superKey,
 			final Map<String, String> args) 
 					throws InternalBackEndException {
 		try {
 			final Map<String, Map<String, List<Mutation>>> mutationMap = new HashMap<String, Map<String, List<Mutation>>>();
 			final long timestamp = System.currentTimeMillis();
 			final Map<String, List<Mutation>> tableMap = new HashMap<String, List<Mutation>>();
 			final List<Mutation> sliceMutationList = new ArrayList<Mutation>(5);
 
 			tableMap.put(superTableName, sliceMutationList);
 
 			final List<Column> columns = new ArrayList<Column>();
 
 			for (Entry<String, String> entry : args.entrySet()) {
 				columns.add(new Column(ByteBuffer.wrap(encode(entry.getKey())),
 						ByteBuffer.wrap(encode(entry.getValue())), timestamp));
 			}
 
 			final Mutation mutation = new Mutation();
 			final SuperColumn superColumn = new SuperColumn(ByteBuffer.wrap(encode(superKey)), columns);
 			mutation.setColumn_or_supercolumn(new ColumnOrSuperColumn().setSuper_column(superColumn));
 			sliceMutationList.add(mutation);
 
 			// Insertion in the map
 			mutationMap.put(key, tableMap);
 
 			wrapper.batch_mutate(mutationMap, consistencyOnWrite);
 
 		} catch (final InternalBackEndException e) {
 			throw new InternalBackEndException(e);
 		}
 	}
 	
 	@Override public void insertSuperSliceListStr(
 			final String superTableName,
 			final List<String> keys,
 			final String superKey,
 			final Map<String, String> args) 
 					throws InternalBackEndException {
 		try {
 			final Map<String, Map<String, List<Mutation>>> mutationMap = new HashMap<String, Map<String, List<Mutation>>>();
 			final long timestamp = System.currentTimeMillis();
 			final Map<String, List<Mutation>> tableMap = new HashMap<String, List<Mutation>>();
 			final List<Mutation> sliceMutationList = new ArrayList<Mutation>(5);
 
 			tableMap.put(superTableName, sliceMutationList);
 
 			final List<Column> columns = new ArrayList<Column>();
 
 			for (Entry<String, String> entry : args.entrySet()) {
 				columns.add(new Column(ByteBuffer.wrap(encode(entry.getKey())),
 						ByteBuffer.wrap(encode(entry.getValue())), timestamp));
 			}
 
 			final Mutation mutation = new Mutation();
 			final SuperColumn superColumn = new SuperColumn(ByteBuffer.wrap(encode(superKey)), columns);
 			mutation.setColumn_or_supercolumn(new ColumnOrSuperColumn().setSuper_column(superColumn));
 			sliceMutationList.add(mutation);
 
 			// Insertion in the map
 			for (String key : keys)
 				mutationMap.put(key, tableMap);
 
 			wrapper.batch_mutate(mutationMap, consistencyOnWrite);
 
 		} catch (final InternalBackEndException e) {
 			throw new InternalBackEndException(e);
 		}
 	}
 	
 	
 	@Override
 	public String selectColumnStr(String tableName, String key,
 			String columnName) throws IOBackEndException,
 			InternalBackEndException {
 		
 		final ColumnPath colPathName = new ColumnPath(tableName);
 		colPathName.setColumn(encode(columnName));
 		byte[] resultValue = wrapper.get(key, colPathName, IStorageManager.consistencyOnRead).getColumn().getValue();
 		return decode(resultValue);
 	}
 
 }
