 package jp.gr.java_conf.afterthesunrise.thadoop;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.NullWritable;
 import org.apache.hadoop.mapreduce.InputFormat;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.RecordReader;
 import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
 import org.apache.pig.Expression;
 import org.apache.pig.FileInputLoadFunc;
 import org.apache.pig.LoadMetadata;
 import org.apache.pig.ResourceSchema;
 import org.apache.pig.ResourceStatistics;
 import org.apache.pig.backend.hadoop.executionengine.mapReduceLayer.PigSplit;
 import org.apache.pig.data.DataType;
 import org.apache.pig.data.Tuple;
 import org.apache.pig.data.TupleFactory;
 import org.apache.pig.impl.logicalLayer.schema.Schema;
 import org.apache.pig.impl.logicalLayer.schema.Schema.FieldSchema;
 import org.apache.thrift.TBase;
 import org.apache.thrift.TFieldIdEnum;
 import org.apache.thrift.meta_data.FieldMetaData;
 import org.apache.thrift.protocol.TType;
 
 /**
  * Pig storage implementation to access thrift objects from pig queries.
  * 
  * @author takanori.takase
  * 
  * @param <F>
  *            Underlying thrift type's field type.
  * @param <W>
  *            Subclass of AbstractTWritable to handle.
  */
 public abstract class AbstractTStorage<F extends TFieldIdEnum, W extends AbstractTWritable<? extends TBase<?, F>>>
 		extends FileInputLoadFunc implements LoadMetadata {
 
 	/**
 	 * A mapping to convert thrift data type to Pig data type.
 	 */
 	private static final Map<Byte, Byte> TYPES;
 
 	static {
 		Map<Byte, Byte> map = new HashMap<Byte, Byte>();
 		map.put(TType.BOOL, DataType.BOOLEAN);
 		map.put(TType.BYTE, DataType.BYTE);
 		map.put(TType.DOUBLE, DataType.DOUBLE);
 		map.put(TType.I16, DataType.INTEGER);
 		map.put(TType.I32, DataType.INTEGER);
 		map.put(TType.I64, DataType.LONG);
 		map.put(TType.STRING, DataType.CHARARRAY);
 		map.put(TType.STRUCT, DataType.UNKNOWN);
 		map.put(TType.MAP, DataType.MAP);
 		map.put(TType.SET, DataType.TUPLE);
 		map.put(TType.LIST, DataType.TUPLE);
 		map.put(TType.ENUM, DataType.CHARARRAY);
 		TYPES = Collections.unmodifiableMap(map);
 	}
 
 	private final TupleFactory tupleFactory = TupleFactory.getInstance();
 
 	private final Class<W> clazz;
 
 	private final SortedMap<F, FieldMetaData> fields;
 
 	private final List<Object> cachedList;
 
 	private RecordReader<?, ?> reader;
 
 	protected AbstractTStorage(Class<W> clazz, Map<F, FieldMetaData> fields) {
 		this.clazz = checkNotNull(clazz);
 		this.fields = generateMap(fields);
 		this.cachedList = new ArrayList<Object>(this.fields.size());
 	}
 
 	private <K, V> SortedMap<F, V> generateMap(Map<F, V> fields) {
 
 		Comparator<F> comparator = TFieldIdEnumComparator.get();
 
 		SortedMap<F, V> map = new TreeMap<F, V>(comparator);
 
 		map.putAll(fields);
 
 		return Collections.unmodifiableSortedMap(map);
 
 	}
 
 	@Override
 	public void setLocation(String location, Job job) throws IOException {
 		SequenceFileInputFormat.setInputPaths(job, new Path(location));
 	}
 
 	@Override
 	public InputFormat<?, W> getInputFormat() throws IOException {
 		return new SequenceFileInputFormat<NullWritable, W>();
 	}
 
 	@Override
 	public void prepareToRead(
 			@SuppressWarnings("rawtypes") RecordReader reader, PigSplit split) {
 		this.reader = reader;
 	}
 
 	@Override
 	public Tuple getNext() throws IOException {
 
 		Object val;
 
 		try {
 
 			if (!reader.nextKeyValue()) {
 				return null;
 			}
 
 			val = reader.getCurrentValue();
 
 		} catch (InterruptedException e) {
 			throw new IOException(e);
 		}
 
 		W writable = clazz.cast(val);
 
 		cachedList.clear();
 
 		for (F field : fields.keySet()) {
 
 			Object value = extractValue(writable, field);
 
 			cachedList.add(value);
 
 		}
 
 		return tupleFactory.newTuple(cachedList);
 
 	}
 
 	protected Object extractValue(W writable, F field) throws IOException {
 		return writable.get().getFieldValue(field);
 	}
 
 	@Override
 	public ResourceSchema getSchema(String path, Job job) throws IOException {
 
 		int size = fields.size();
 
 		List<FieldSchema> fieldSchemas = new ArrayList<FieldSchema>(size);
 
 		for (Entry<F, FieldMetaData> entry : fields.entrySet()) {
 
 			F field = entry.getKey();
 
 			FieldMetaData metaData = entry.getValue();
 
 			Byte pigType = extractDataType(field, metaData, TYPES);
 
 			String name = field.getFieldName();
 
 			if (pigType == null) {
 				throw new IOException("Unknown type field : " + name);
 			}
 
 			fieldSchemas.add(new FieldSchema(name, pigType));
 
 		}
 
 		return new ResourceSchema(new Schema(fieldSchemas));
 
 	}
 
 	protected Byte extractDataType(F field, FieldMetaData metaData,
 			Map<Byte, Byte> mapping) throws IOException {
 
 		Byte thriftType = metaData.valueMetaData.type;
 
 		Byte pigType = mapping.get(thriftType);
 
 		return pigType;
 
 	}
 
 	@Override
 	public ResourceStatistics getStatistics(String location, Job job) {
 		return null;
 	}
 
 	@Override
 	public String[] getPartitionKeys(String location, Job job) {
 		return null;
 	}
 
 	@Override
 	public void setPartitionFilter(Expression partitionFilter) {
 	}
 
 }
