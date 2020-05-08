 package com.joyveb.support.cassandra;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import lombok.Setter;
 import lombok.extern.slf4j.Slf4j;
 import me.prettyprint.cassandra.model.CqlQuery;
 import me.prettyprint.cassandra.model.CqlRows;
 import me.prettyprint.cassandra.model.thrift.ThriftRangeSlicesQuery;
 import me.prettyprint.cassandra.serializers.ByteBufferSerializer;
 import me.prettyprint.cassandra.serializers.StringSerializer;
 import me.prettyprint.cassandra.service.template.ColumnFamilyResult;
 import me.prettyprint.cassandra.service.template.ColumnFamilyTemplate;
 import me.prettyprint.cassandra.service.template.ColumnFamilyUpdater;
 import me.prettyprint.cassandra.service.template.ThriftColumnFamilyTemplate;
 import me.prettyprint.cassandra.utils.Assert;
 import me.prettyprint.hector.api.Keyspace;
 import me.prettyprint.hector.api.Serializer;
 import me.prettyprint.hector.api.beans.OrderedRows;
 import me.prettyprint.hector.api.beans.Row;
 import me.prettyprint.hector.api.factory.HFactory;
 import me.prettyprint.hector.api.mutation.Mutator;
 import me.prettyprint.hector.api.query.QueryResult;
 import me.prettyprint.hector.api.query.RangeSlicesQuery;
 import me.prettyprint.hom.HectorObjectMapper;
 
 import org.apache.commons.lang3.StringUtils;
 
 /**
  * @author limj
  * @date 2013.02.17
  * 
  * @param <T>
  *            Cassandra DAO父类，仅支持columnFamily数据类型
  * 
  */
 @Slf4j
 public abstract class CassandraDaoSupport<K, T extends CassandraPrimaryKey<K>> {
 
 	protected @Setter Keyspace keyspace;// 相当于数据库名
 	protected @Setter String columnFamilyName;// 相当于表名
 	protected StringSerializer ss = StringSerializer.get();
 	protected Serializer<ByteBuffer> vs = ByteBufferSerializer.get();
 	protected Serializer<K> keySerializer;
 	// protected final ReadWriteLock lock = new ReentrantReadWriteLock();
 	// protected final Lock r = lock.readLock();
 	// protected final Lock w = lock.writeLock();
 
 	private ColumnFamilyTemplate<K, String> columnFamilyTemplate;
 
 	/**
 	 * @param keyspace
 	 * @param columnFamilyName
 	 */
 	public CassandraDaoSupport(Keyspace keyspace, String columnFamilyName,
 			Serializer<K> keySerializer) {
 		this.keyspace = keyspace;
 		this.columnFamilyName = columnFamilyName;
 		this.keySerializer = keySerializer;
 		columnFamilyTemplate = new ThriftColumnFamilyTemplate<K, String>(
 				keyspace, columnFamilyName, keySerializer, ss);
 	}
 
 	public ColumnFamilyTemplate<K, String> getCFTemplate() {
 		return this.columnFamilyTemplate;
 	}
 
 	/**
 	 * 生成columnFamily对象实例
 	 * 
 	 * @return
 	 */
 	protected abstract T createPOJO();
 
 	/**
 	 * 查找_key对应的值
 	 * 
 	 * @param _key
 	 * @return
 	 */
 	public T selectByPrimaryKey(K _key) {
 		long start = System.currentTimeMillis();
 		T t = null;
 		ColumnFamilyResult<K, String> result = null;
 		try {
 			// r.lock();
 			result = getCFTemplate().queryColumns(_key);
 		} finally {
 			// r.unlock();
 		}
 		if (result.hasResults()) {
 			t = cfResult2Pojo(result);
 		}
 		log.debug("selectByPrimaryKey cost:"
 				+ (System.currentTimeMillis() - start) + "ms");
 		return t;
 	}
 
 	/**
 	 * 将查询结果转化成List
 	 * 
 	 * @param result
 	 * @return
 	 */
 	protected List<T> cfResult2List(ColumnFamilyResult<K, String> result) {
 		List<T> list = new ArrayList<T>();
 		if (result.hasResults()) {
 			T t = cfResult2Pojo(result);
 			list.add(t);
 			while (result.hasNext()) {
 				result.next();
 				t = cfResult2Pojo(result);
 				list.add(t);
 			}
 		}
 		return list;
 	}
 
 	/**
 	 * 将查询结果转化成对应的pojo
 	 * 
 	 * @param result
 	 * @return
 	 */
 	protected T cfResult2Pojo(ColumnFamilyResult<K, String> result) {
 		try {
 			T t = createPOJO();
 			t.setKey(result.getKey());
 			Class<?> clazz = t.getClass();
 			Field[] fields = clazz.getDeclaredFields();
 			for (Field field : fields) {
 				String name = field.getName();
 				String setterName = "set" + StringUtils.capitalize(name);
 				Object value = drawColumnValue(field, result);
 				if (value != null) {
 					Method setter = clazz.getMethod(setterName,
 							value.getClass());
 					setter.invoke(t, value);
 				}
 			}
 			return t;
 		} catch (Exception e) {
 			log.info("result to pojo error, result:" + result);
 			throw new CassandraDaoException("result to pojo error", e);
 		}
 	}
 
 	/**
 	 * 根据字段名提取列列值 如果数据库中不存在该列,则返回null;
 	 */
 	private Object drawColumnValue(Field field,
 			ColumnFamilyResult<K, String> result) {
 		byte[] bytes = result.getByteArray(StringUtils.upperCase(field
 				.getName()));
 		if (bytes == null) {
 			return null;
 		}
 		return HectorObjectMapper.determineSerializer(field.getType())
 				.fromBytes(bytes);
 	}
 
 	/**
 	 * 通过cql查找数据(不能分页)
 	 * 
 	 * @param cql
 	 * @return
 	 */
 	public List<T> selectByCQL(String cql) {
 		long start = System.currentTimeMillis();
 		Assert.notNull(cql, "cql is null");
 		List<T> datas = new ArrayList<T>();
 		QueryResult<CqlRows<K, String, ByteBuffer>> result = null;
 		CqlQuery<K, String, ByteBuffer> cqlQuery = new CqlQuery<K, String, ByteBuffer>(
 				this.keyspace, keySerializer, ss, vs);
 		cqlQuery.setQuery(cql);
 		try {
 			// r.lock();
 			result = cqlQuery.execute();
 		} finally {
 			// r.unlock();
 		}
 		if (result.get() != null) {
 			List<Row<K, String, ByteBuffer>> rows = result.get().getList();
 			for (Row<K, String, ByteBuffer> row : rows) {
 				T data = row2POJO(row);
 				datas.add(data);
 			}
 		}
 		log.debug(this.columnFamilyName + ": cql[" + cql + "] query cost:"
 				+ (System.currentTimeMillis() - start) + "ms");
 		return datas;
 	}
 
 	/**
 	 * @param t
 	 */
 	public void insert(T t) {
 		Mutator<K> mutator = HFactory.createMutator(this.keyspace,
 				keySerializer);
 		appendInsertionSelective(mutator, t);
 		try {
 			// w.lock();
 			getCFTemplate().executeBatch(mutator);
 		} finally {
 			// w.unlock();
 		}
 	}
 
 	/**
 	 * 批量insert
 	 * 
 	 * @param list
 	 */
 	public void batchInsert(List<T> list) {
 		Mutator<K> mutator = HFactory.createMutator(this.keyspace,
 				keySerializer);
 		for (T pojo : list) {
 			appendInsertionSelective(mutator, pojo);
 		}
 		try {
 			// w.lock();
 			getCFTemplate().executeBatch(mutator);
 		} finally {
 			// w.unlock();
 		}
 	}
 
 	private void appendInsertionSelective(Mutator<K> mutator, T pojo) {
 		try {
 			Class<?> clazz = pojo.getClass();
 			Field[] fields = clazz.getDeclaredFields();
 			for (Field field : fields) {
 				String name = field.getName();
 				String getterName = "get" + StringUtils.capitalize(name);
 				Method getter = clazz.getDeclaredMethod(getterName);
 				Object result = getter.invoke(pojo);
 				if (result != null) {
 					Serializer serializer = HectorObjectMapper
 							.determineSerializer(result.getClass());
 					mutator.addInsertion(pojo.getKey(), columnFamilyName,
 							HFactory.createColumn(StringUtils.upperCase(name),
 									result, ss, serializer));
 				}
 			}
 		} catch (Exception e) {
 			throw new CassandraDaoException(this.columnFamilyName
 					+ " append insertion exception", e);
 		}
 	}
 
 	/**
 	 * 将查到的行结果数据转成对应的POJO
 	 * 
 	 * @param row
 	 * @return
 	 */
 	private T row2POJO(Row<K, String, ByteBuffer> row) {
 		T pojo = createPOJO();
 		try {
 			pojo.setKey(row.getKey());
 			for (Field field : pojo.getClass().getDeclaredFields()) {
 				String fieldName = StringUtils.upperCase(field.getName());
 				if (row.getColumnSlice().getColumnByName(fieldName) != null) {
 					Serializer<?> serializer = HectorObjectMapper
 							.determineSerializer(field.getType());
 					Object value = serializer.fromByteBuffer(row
 							.getColumnSlice().getColumnByName(fieldName)
 							.getValueBytes());
 					String setterMether = "set"
 							+ StringUtils.capitalize(field.getName());
 					Method setter = pojo.getClass().getMethod(setterMether,
 							value.getClass());
 					setter.invoke(pojo, value);
 				}
 			}
 		} catch (Exception e) {
 			throw new CassandraDaoException("", e);
 		}
 		return pojo;
 	}
 
 	/**
 	 * 更新单个POJO
 	 * 
 	 * @param t
 	 */
 	public void updateByPrimaryKey(T t) {
 		ColumnFamilyUpdater<K, String> updater = getCFTemplate().createUpdater(
 				t.getKey());
 		Mutator<K> mutator = updater.getCurrentMutator();
 		appendInsertionSelective(mutator, t);
 		try {
 			// w.lock();
 			getCFTemplate().update(updater);
 		} finally {
 			// w.unlock();
 		}
 	}
 
 	/**
 	 * 批次更新
 	 * 
 	 * @param list
 	 */
 	public void batchUpdate(List<T> list) {
 		ColumnFamilyUpdater<K, String> updater = getCFTemplate()
 				.createUpdater();
 		Mutator<K> mutator = updater.getCurrentMutator();
 		for (T t : list) {
 			appendInsertionSelective(mutator, t);
 		}
 		try {
 			// w.lock();
 			getCFTemplate().update(updater);
 		} finally {
 			// w.unlock();
 		}
 	}
 
 	/**
 	 * 批次更新
 	 * 
 	 * @param list
 	 */
 	public void delete(K key) {
 		try {
 			// w.lock();
 			getCFTemplate().deleteRow(key);
 		} finally {
 			// w.unlock();
 		}
 	}
 
 	/**
 	 * 条件查询
 	 * 
 	 * @param example
 	 *            :作为条件的字段必须有值，其他字段为空 如果example为null,则该方法抛出异常
 	 */
 	public List<T> selectByExample(Example<K> example) {
 		long start = System.currentTimeMillis();
 		RangeSlicesQuery<K, String, ByteBuffer> rangeSlicesQuery = new ThriftRangeSlicesQuery<K, String, ByteBuffer>(
 				keyspace, keySerializer, ss, vs);
 		rangeSlicesQuery.setColumnFamily(columnFamilyName);
 		rangeSlicesQuery.setKeys(null, null);
 		rangeSlicesQuery.setRange(null, null, false, Integer.MAX_VALUE);
 		rangeSlicesQuery.setRowCount(Integer.MAX_VALUE);
 		example.appendExp2Query(rangeSlicesQuery);
 		QueryResult<OrderedRows<K, String, ByteBuffer>> result = null;
 		try {
 			// r.lock();
 			result = rangeSlicesQuery.execute();
 		} finally {
 			// r.unlock();
 		}
 		OrderedRows<K, String, ByteBuffer> rows = result.get();
 		List<T> datas = null;
 		if(rows != null){
 			datas = orderedRows2ListT(rows.getList());
 		}
 		log.debug(this.columnFamilyName + " query by example cost:"
 				+ (System.currentTimeMillis() - start) + "ms");
 		return datas;
 	}
 
 	/**
 	 * 条件查询
 	 * 
 	 * @param example
 	 *            :作为条件的字段必须有值，其他字段为空 如果example为null,则该方法抛出异常
 	 */
 	public List<T> selectByExampleWithCQL(ExampleByCQL example) {
 		long start = System.currentTimeMillis();
 		StringBuffer cql = new StringBuffer();
 		cql.append("select * from ").append(this.columnFamilyName);
 		if (example != null && example.toString().length() > 0) {
 			cql.append(" WHERE ").append(example.toString());
 		}
 		CqlQuery<K, String, ByteBuffer> cqlQuery = new CqlQuery<K, String, ByteBuffer>(
 				this.keyspace, keySerializer, ss, vs);
 		List<T> datas = null;
 		cqlQuery.setQuery(cql.toString());
 		QueryResult<CqlRows<K, String, ByteBuffer>> result = null;
 		try {
 			// r.lock();
 			result = cqlQuery.execute();
 		} finally {
 			// r.unlock();
 		}
 		if(result != null){			
 			datas = orderedRows2ListT(result.get().getList());
 		}
 		log.debug(this.columnFamilyName + ":cql[" + cql + "] query cost:"
 				+ (System.currentTimeMillis() - start) + "ms");
 		return datas;
 	}
 
 	private List<T> orderedRows2ListT(List<Row<K, String, ByteBuffer>> rows) {
 		List<T> datas = new ArrayList<T>();
 		if (rows != null) {
 			Iterator<Row<K, String, ByteBuffer>> rowsIterator = rows.iterator();
 			while (rowsIterator.hasNext()) {
 				T data = row2POJO(rowsIterator.next());
 				datas.add(data);
 			}
 		}
 		return datas;
 	}
 
 	/**
 	 * 分页查找(如果需要页面使用，推荐使用@findByPages方法)
 	 * 
 	 * @param example
 	 *            ：有值的字段即为条件 example为空,则抛出IllegalArgumentException
 	 */
 	public List<T> findByPage(Example<K> example, int pagesize, K startKey,
 			K endKey) {
 		long start = System.currentTimeMillis();
 		RangeSlicesQuery<K, String, ByteBuffer> rangeSlicesQuery = HFactory
 				.createRangeSlicesQuery(keyspace, keySerializer, ss, vs);
 		rangeSlicesQuery.setColumnFamily(columnFamilyName);
 		rangeSlicesQuery.setKeys(startKey, endKey);
 		// reason from [https://github.com/hector-client/hector/wiki/User-Guide]
 		rangeSlicesQuery.setRowCount(pagesize + 1);// 多取出一条,下次从该条开始查
 		rangeSlicesQuery.setRange(null, null, false, Integer.MAX_VALUE);
 		example.appendExp2Query(rangeSlicesQuery);
 		QueryResult<OrderedRows<K, String, ByteBuffer>> resultQuery = null;
 		try {
 			// r.lock();
 			resultQuery = rangeSlicesQuery.execute();
 		} finally {
 			// r.unlock();
 		}
 		List<T> datas = null;
 		if(resultQuery != null){
 			datas = orderedRows2ListT(resultQuery.get().getList());
 		}
 		log.debug(this.columnFamilyName + " findbypage cost:"
 				+ (System.currentTimeMillis() - start) + "ms");
 		return datas;
 	}
 
 	public PageIterator<K, T> createPageIterator(final K startkey,
 			final Example<K> example) {
 
 		return new PageIterator<K, T>() {
 			/**
 			 * if select with start index of the db, the value should be set up
 			 * with null
 			 */
 			private K startKey = startkey;
 			private boolean first = true;
 
 			public boolean hasNext() {
 				return startKey != null || first;
 			}
 
 			public List<T> next(int pagesize) {
 				first = false;
 				List<T> pageList = findByPage(example, pagesize, startKey, null);
 				int size = pageList.size();
 				if (size > pagesize) {
 					T t = pageList.get(size - 1);
 					startKey = t.getKey();
 					pageList.remove(size - 1);// 移除多查出的行
 				} else {
 					startKey = null;
 				}
 				return pageList;
 			}
 		};
 	}
 
 	/**
 	 * 分页查找(推荐使用)
 	 * 
 	 * @param example
 	 *            ：有值的字段即为条件
 	 * @param pagesize
 	 *            :
 	 * @param startKey
 	 *            :开始key
 	 * @param endKey
 	 *            :结束key
 	 * @return
 	 */
 	public CassandraList<K, T> findByPages(Example<K> example, int pagesize,
 			K startKey, K endKey) {
 		long start = System.currentTimeMillis();
 		CassandraList<K, T> cassandraList = new CassandraList<K, T>();
 		RangeSlicesQuery<K, String, ByteBuffer> rangeSlicesQuery = HFactory
 				.createRangeSlicesQuery(keyspace, keySerializer, ss, vs);
 		rangeSlicesQuery.setColumnFamily(columnFamilyName);
 		rangeSlicesQuery.setKeys(startKey, endKey);
 		// reason from [https://github.com/hector-client/hector/wiki/User-Guide]
 		rangeSlicesQuery.setRowCount(pagesize + 1);
 		rangeSlicesQuery.setRange(null, null, false, Integer.MAX_VALUE);
 		example.appendExp2Query(rangeSlicesQuery);
 		QueryResult<OrderedRows<K, String, ByteBuffer>> resultQuery = rangeSlicesQuery
 				.execute();
 		if (resultQuery.get() != null) {
 			List<Row<K, String, ByteBuffer>> rows = resultQuery.get().getList();
 			if (rows != null && rows.size() > 0) {
 				if (rows.size() > pagesize) {
 					K nextStartKey = resultQuery.get().peekLast().getKey();
 					cassandraList.setStartKey(nextStartKey);
 					rows.remove(rows.size() - 1);// 删除最后一行记录
 				}else{
 					cassandraList.setStartKey(null);
 				}
 				List<T> beans = orderedRows2ListT(rows);
 				cassandraList.setResultList(beans);
 			}
 		}
 		log.debug(this.columnFamilyName + " findbypages cost:" + (System.currentTimeMillis() - start) + "ms");
 		return cassandraList;
 	}
 }
