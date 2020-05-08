 package com.feedly.cassandra.dao;
 
 import java.util.AbstractCollection;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.NoSuchElementException;
 import java.util.Set;
 
 import me.prettyprint.hector.api.Serializer;
 import me.prettyprint.hector.api.beans.AbstractComposite.ComponentEquality;
 import me.prettyprint.hector.api.beans.DynamicComposite;
 import me.prettyprint.hector.api.beans.OrderedRows;
 import me.prettyprint.hector.api.beans.Row;
 import me.prettyprint.hector.api.factory.HFactory;
 import me.prettyprint.hector.api.query.QueryResult;
 import me.prettyprint.hector.api.query.RangeSlicesQuery;
 import me.prettyprint.hector.api.query.SliceQuery;
 
 import com.feedly.cassandra.IKeyspaceFactory;
 import com.feedly.cassandra.entity.EntityMetadata;
 import com.feedly.cassandra.entity.PropertyMetadata;
 
 /*
  * helper class to do gets (reads by row key).
  */
 class GetHelper<K, V> extends LoadHelper<K, V>
 {
     GetHelper(EntityMetadata<V> meta, IKeyspaceFactory factory)
     {
         super(meta, factory);
     }
 
     public V get(K key, V value, GetOptions options)
     {
         switch(options.getColumnFilterStrategy())
         {
             case UNFILTERED:
                 return loadFromGet(key, null, null, null, null);
             case INCLUDES:
                 return get(key, value, options.getIncludes(), options.getExcludes());
             case RANGE:
                 return loadFromGet(key, value, null, propertyName(options.getStartColumn()), propertyName(options.getEndColumn()));
         }
         
         throw new IllegalStateException(); //never happens
     }
 
     private V get(K key, V value, Set<? extends Object> includes, Set<String> excludes)
     {
         _logger.debug("loading {}[{}]", _entityMeta.getFamilyName(), key);
 
         if(includes != null && excludes != null)
             throw new IllegalArgumentException("either includes or excludes should be specified, not both");
 
         List<byte[]> colNames = new ArrayList<byte[]>();
         List<PropertyMetadata> fullCollectionProperties = derivePartialColumns(colNames, includes, excludes);
 
         value = loadFromGet(key, value, colNames, null, null);
 
         if(fullCollectionProperties != null)
         {
             for(PropertyMetadata pm : fullCollectionProperties)
             {
                 DynamicComposite dc = new DynamicComposite();
                 dc.addComponent(0, pm.getName(), ComponentEquality.EQUAL);
                 byte[] colBytes = SER_COMPOSITE.toBytes(dc);
                 dc = new DynamicComposite();
                 dc.addComponent(0, pm.getName(), ComponentEquality.GREATER_THAN_EQUAL); // little strange, this really means the first value
                                                                                         // greater than...
                 byte[] colBytesEnd = SER_COMPOSITE.toBytes(dc);
 
                 value = loadFromGet(key, value, null, colBytes, colBytesEnd);
             }
         }
 
         return value;
     }
 
     public List<V> mget(Collection<K> keys)
     {
         return bulkLoadFromMultiGet(keys, null, null, null, null, false);
     }
 
     public List<V> mget(List<K> keys, List<V> values, GetOptions options)
     {
         if(keys == null)
             throw new IllegalArgumentException("keys parameter is null");
         if(values != null && keys.size() != values.size())
             throw new IllegalArgumentException("key and value list must be same size");
         
         switch(options.getColumnFilterStrategy())
         {
             case UNFILTERED:
                 return bulkLoadFromMultiGet(keys, values, null, null, null, true);
             case INCLUDES:
                 return mget(keys, values, options.getIncludes(), options.getExcludes());
             case RANGE:
                 return bulkLoadFromMultiGet(keys, values, null, propertyName(options.getStartColumn()), propertyName(options.getEndColumn()), true);
         }
 
         throw new IllegalStateException(); //never happens
     }
     
     public Collection<V> mgetAll(GetOptions options)
     {
         return new LazyLoadedCollection(options);
     }
     
     private List<V> mget(List<K> keys, List<V> values, Set<? extends Object> includes, Set<String> excludes)
     {
         if(includes != null && excludes != null)
             throw new IllegalArgumentException("either includes or excludes should be specified, not both");
 
         if(includes == null && excludes == null)
             throw new IllegalArgumentException("includes or excludes should be specified");
 
         List<byte[]> colNames = new ArrayList<byte[]>();
         List<PropertyMetadata> fullCollectionProperties = derivePartialColumns(colNames, includes, excludes);
 
         values = bulkLoadFromMultiGet(keys, values, colNames, null, null, true);
 
         return addFullCollectionProperties(keys, values, fullCollectionProperties);
     }
 
     @SuppressWarnings({ "rawtypes", "unchecked" })
     private V loadFromGet(K key, V value, List<byte[]> cols, byte[] from, byte[] to)
     {
         _logger.debug("loading {}[{}]", _entityMeta.getFamilyName(), key);
 
         PropertyMetadata keyMeta = _entityMeta.getKeyMetadata();
         byte[] keyBytes = ((Serializer) keyMeta.getSerializer()).toBytes(key);
 
         SliceQuery<byte[], byte[], byte[]> query = buildSliceQuery(keyBytes);
 
         if(cols != null)
             query.setColumnNames(cols.toArray(new byte[cols.size()][]));
         else
             query.setRange(from, to, false, CassandraDaoBase.COL_RANGE_SIZE);
 
         return fromColumnSlice(key, value, keyMeta, keyBytes, query, query.execute().get(), to);
     }
 
     
     @SuppressWarnings({ "unchecked", "rawtypes" })
     private byte[] fetch(RangeSlicesQuery<byte[], byte[], byte[]> query, byte[] start, GetOptions options, List<V> values)
     {
         List<K> keys = null;
         List<PropertyMetadata> fullCollectionProperties = null;
         values.clear();
         byte[] endCol = null;
         switch(options.getColumnFilterStrategy())
         {
             case INCLUDES:
                 Set<? extends Object> includes = options.getIncludes();
                 Set<String> excludes = options.getExcludes();
                 if(includes != null && excludes != null)
                     throw new IllegalArgumentException("either includes or excludes should be specified, not both");
 
                 if(includes == null && excludes == null)
                     throw new IllegalArgumentException("either includes or excludes should be specified");
 
                 List<byte[]> colNames = new ArrayList<byte[]>();
                 fullCollectionProperties = derivePartialColumns(colNames, includes, excludes);
                 if(fullCollectionProperties != null)
                     keys = new ArrayList<K>();
                 
                 query.setColumnNames(colNames.toArray(new byte[colNames.size()][]));
                 break;
                 
             case RANGE:
                 byte[] startCol = propertyName(options.getStartColumn());
                 endCol = propertyName(options.getEndColumn()); 
                 query.setRange(startCol, endCol, false, CassandraDaoBase.ROW_RANGE_SIZE);                
                 break;
             
             case UNFILTERED:
                 query.setRange(null, null, false, CassandraDaoBase.ROW_RANGE_SIZE);
                 break;
         }
         
         query.setKeys(start, null);
         QueryResult<OrderedRows<byte[],byte[],byte[]>> result = query.execute();
         
         PropertyMetadata keyMeta = _entityMeta.getKeyMetadata();
         boolean checkStart = true;
         
         OrderedRows<byte[],byte[],byte[]> rows = result.get();
         for(Row<byte[], byte[], byte[]> row : rows)
         {
             if(checkStart)
             {
                 checkStart = false;
                 if(start != null && Arrays.equals(start, row.getKey()))
                     continue;
             }
             
             K key = (K) ((Serializer) keyMeta.getSerializer()).fromBytes(row.getKey());
 
             V value = fromColumnSlice(key, null, keyMeta, row.getKey(), null, row.getColumnSlice(), endCol);
 
             if(value != null)
             {
                 if(keys != null)
                     keys.add(key);
                 values.add(value);
             }
         }
         
         int cnt = rows.getCount();
         
         if(cnt == 0)
             return null;
         else if(keys != null)
             addFullCollectionProperties(keys, values, fullCollectionProperties);
 
         return cnt == 0 ? null : rows.getList().get(cnt - 1).getKey();
     }
     
     private class LazyLoadedIterator implements Iterator<V>
     {
         private List<V> _current;
         private Iterator<V> _currentIter;
         private byte[] _lastKeyOfBatch;
         private V _next;
         private int _iteratedCnt = 0;
         private final GetOptions _options;
         RangeSlicesQuery<byte[], byte[], byte[]> _query;
         private final LazyLoadedCollection _parent;
         
         public LazyLoadedIterator(LazyLoadedCollection parent, 
                                   List<V> first,
                                   byte[] lastKeyOfBatch,
                                   RangeSlicesQuery<byte[], byte[], byte[]> query,
                                   GetOptions options)
         {
             _parent = parent;
             _current = new ArrayList<V>(first);
             _lastKeyOfBatch = lastKeyOfBatch;
             _query = query;
             _currentIter = _current.iterator();
             _next = _currentIter.next();
             _options = options;
         }
 
 
         @Override
         public boolean hasNext()
         {
             return _next != null;
         }
 
         @Override
         public V next()
         {
             if(_next == null)
                 throw new NoSuchElementException();
             
             V rv = _next;
             
             if(_currentIter.hasNext())
                 _next = _currentIter.next();
            else if(_current.size() < CassandraDaoBase.ROW_RANGE_SIZE - 1) //minus one for key boundary overlap
             {
                 _current = null;
                 _currentIter = null;
                 _next = null;
             }
             else //fetch next batch
             {
                 _lastKeyOfBatch = fetch(_query, _lastKeyOfBatch, _options, _current);
                 
                 if(_current.isEmpty())
                 {
                     _current = null;
                     _currentIter = null;
                     _next = null;
                 }
                 else
                 {
                     _currentIter = _current.iterator();
                     _next = _currentIter.next();
                 }
             }
             
             _iteratedCnt++;
             if(_next == null)
                 _parent.setSize(_iteratedCnt); //we have a row count, notify parent so subsequent calls to size() don't have to fetch all rows
             
             return rv;
         }
 
         @Override
         public void remove()
         {
             throw new UnsupportedOperationException();
         }
     }
     
     private class LazyLoadedCollection extends AbstractCollection<V>
     {
         RangeSlicesQuery<byte[], byte[], byte[]> _query;
         private GetOptions _options;
         private List<V> _all = null; //if it is known all rows have been fetched, this field is set
         private List<V> _first = new ArrayList<V>();
         private byte[] _lastKeyOfBatch; //last key of the _first rows
         private int _size = -1;
         public LazyLoadedCollection(GetOptions options)
         {
             _query = HFactory.createRangeSlicesQuery(_keyspaceFactory.createKeyspace(), SER_BYTES, SER_BYTES, SER_BYTES);
             _query.setColumnFamily(_entityMeta.getFamilyName());
             
             _lastKeyOfBatch = fetch(_query, null, options, _first); 
             if(_first.size() < CassandraDaoBase.ROW_RANGE_SIZE)
                 _all = _first;
 
             _options = options;
         }
 
         //override, don't want to invoke size, just to check if empty
         @Override
         public boolean isEmpty()
         {
             return _first.isEmpty();
         }
         
         //can aggressively fetch and retain all values, use with caution
         @Override
         public int size()
         {
             if(_size >= 0)
                 return _size;
             if(_all == null)
             {
                 Iterator<V> iter = iterator();
                 _all = new ArrayList<V>();
                 while(iter.hasNext())
                     _all.add(iter.next());
             }
             
             return _all.size();
         }
         
         @Override
         public java.util.Iterator<V> iterator()
         {
             if(_all != null)
                 return _all.iterator();
             
             return new LazyLoadedIterator(this, _first, _lastKeyOfBatch, _query, _options);
         }
         
         void setSize(int size)
         {
             _size = size;
         }
     }
 }
