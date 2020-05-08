 package jp.gr.java_conf.afterthesunrise.thadoop;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Comparator;
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
  *            Subclass of AbstractTWritable to handle in this storage.
  */
 public abstract class AbstractTStorage<F extends TFieldIdEnum, W extends AbstractTWritable<? extends TBase<?, F>>>
 		extends FileInputLoadFunc implements LoadMetadata {
 
 	/**
 	 * A mapping array indexed by thrift type id, and containing pig type id.<br />
 	 * Usage : {@code byte pidId = THRIFT_2_PIG[thriftId];}
 	 */
 	private static final byte[] THRIFT_2_PIG = new byte[Byte.MAX_VALUE];
 
 	static {
 		// TODO : Handle thrift's TTYPE.STRUCT
 		THRIFT_2_PIG[TType.BOOL] = DataType.BOOLEAN;
 		THRIFT_2_PIG[TType.BYTE] = DataType.BYTE;
 		THRIFT_2_PIG[TType.DOUBLE] = DataType.DOUBLE;
 		THRIFT_2_PIG[TType.I16] = DataType.INTEGER;
 		THRIFT_2_PIG[TType.I32] = DataType.INTEGER;
 		THRIFT_2_PIG[TType.I64] = DataType.LONG;
 		THRIFT_2_PIG[TType.STRING] = DataType.CHARARRAY;
 		THRIFT_2_PIG[TType.STRUCT] = DataType.UNKNOWN;
 		THRIFT_2_PIG[TType.MAP] = DataType.MAP;
 		THRIFT_2_PIG[TType.SET] = DataType.TUPLE;
 		THRIFT_2_PIG[TType.LIST] = DataType.TUPLE;
 		THRIFT_2_PIG[TType.ENUM] = DataType.CHARARRAY;
 	}
 
 	private final TupleFactory tupleFactory = TupleFactory.getInstance();
 
 	private RecordReader<?, W> reader;
 
 	@Override
 	public void setLocation(String location, Job job) throws IOException {
 		SequenceFileInputFormat.setInputPaths(job, new Path(location));
 	}
 
 	@Override
 	public InputFormat<?, W> getInputFormat() {
 		return new SequenceFileInputFormat<NullWritable, W>();
 	}
 
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	@Override
 	public void prepareToRead(RecordReader reader, PigSplit split) {
 		this.reader = reader;
 	}
 
 	@Override
 	public Tuple getNext() throws IOException {
 
 		try {
 
 			if (!reader.nextKeyValue()) {
 				return null;
 			}
 
 			W writable = reader.getCurrentValue();
 
 			TBase<?, F> base = writable.get();
 
 			return getNext(base);
 
 		} catch (InterruptedException e) {
 			throw new IOException(e);
 		}
 
 	}
 
 	private Tuple getNext(TBase<?, F> val) {
 
 		SortedMap<F, Byte> ids = getFieldIds();
 
		List<Object> values = new ArrayList<Object>(ids.size());
 
 		for (Entry<F, Byte> entry : ids.entrySet()) {
 
 			F field = entry.getKey();
 
 			Object value = val.getFieldValue(field);
 
 			value = convertValue(field, entry.getValue(), value);
 
 			values.add(value);
 
 		}
 
 		return tupleFactory.newTupleNoCopy(values);
 
 	}
 
 	/**
 	 * <p>
 	 * Retrieve map of thrift field enum and the thift data type id for the
 	 * field. This mapping is used for both constructing the column schema and
 	 * to retrieve data per column. Therefore, it is mandatory for the
 	 * underlying implementation to provide a consistent mapping for each
 	 * invocation.
 	 * </p>
 	 * <p>
 	 * This method will be invoked every line pig processed, so it is preferred
 	 * that the underlying implementation to cache the map instead of generating
 	 * new map instance every time.
 	 * </p>
 	 * 
 	 * @return Sorted mapping for thrift field enums and thrift type ids.
 	 */
 	protected abstract SortedMap<F, Byte> getFieldIds();
 
 	/**
 	 * <p>
 	 * Convenient method for the subclass to generate map to return for
 	 * {@code AbstractTStorage#getFieldIds()}. Generated thrift classes contains
 	 * a field called {@code metaDataMap}. By providing the field to this
 	 * method, value for {@code AbstractTStorage#getFieldIds()} will be
 	 * returned.
 	 * </p>
 	 * <p>
 	 * The map will be sorted based on the natural order of
 	 * {@code TFieldIdEnum#getThriftFieldId()}.
 	 * </P>
 	 * 
 	 * @param map
 	 *            {@code metaDataMap} field in the underlying thrift class.
 	 * @return Map to be used for {@code AbstractTStorage#getFieldIds()}
 	 * @throws NullPointerException
 	 *             If the given map, or the elements in the map is null.
 	 */
 	protected final SortedMap<F, Byte> transformFields(Map<F, FieldMetaData> map) {
 
 		Comparator<F> comparator = TFieldIdEnumComparator.get();
 
		SortedMap<F, Byte> result = new TreeMap<F, Byte>(comparator);
 
 		for (Entry<F, FieldMetaData> entry : map.entrySet()) {
 
 			F key = entry.getKey();
 
 			byte value = entry.getValue().valueMetaData.type;
 
 			result.put(key, value);
 
 		}
 
 		return result;
 
 	}
 
 	/**
 	 * <p>
 	 * Method to convert a value from thrift type to pig type. Subclass may
 	 * override this method to provide custom rule for converting thrift type
 	 * values to pig type values.
 	 * </p>
 	 * <p>
 	 * By default, all the value types except for the enum will not be
 	 * converted, using the direct value extracted. For enum values, the value
 	 * is converted into {@code String} using the {@code Enum#name()} method.
 	 * </p>
 	 * <p>
 	 * Note that {@code AbstractTStorage#convertType(byte[], byte)} may need to
 	 * overwritten as well if this method is overwritten.
 	 * </p>
 	 * 
 	 * @param field
 	 *            Field enum from which the value was extracted from.
 	 * @param type
 	 *            Thrift type id.
 	 * @param value
 	 *            Extracted thrift value
 	 * @return Converted value in pig type
 	 */
 	protected Object convertValue(F field, byte type, Object value) {
 
 		if (value == null) {
 			return null;
 		}
 
 		if (type != TType.ENUM) {
 			return value;
 		}
 
 		return ((Enum<?>) value).name();
 
 	}
 
 	@Override
 	public ResourceSchema getSchema(String location, Job job) {
 
 		SortedMap<F, Byte> ids = getFieldIds();
 
		List<FieldSchema> fieldSchemas = new ArrayList<FieldSchema>(ids.size());
 
 		byte[] defensiveCopy = THRIFT_2_PIG.clone();
 
 		for (Entry<F, Byte> entry : ids.entrySet()) {
 
 			String name = entry.getKey().getFieldName();
 
 			byte pigType = convertType(defensiveCopy, entry.getValue());
 
 			FieldSchema schema = new FieldSchema(name, pigType);
 
 			fieldSchemas.add(schema);
 
 		}
 
 		return new ResourceSchema(new Schema(fieldSchemas));
 
 	}
 
 	/**
 	 * <p>
 	 * Convert thrift type id to pig type id for constructing schema. Given
 	 * default mapping array is used in the default implementation. Underlying
 	 * implementation may override this method to provide custom mapping.
 	 * </p>
 	 * <p>
 	 * Note that
 	 * {@code AbstractTStorage#convertValue(TFieldIdEnum, byte, Object)} may
 	 * need to overwritten as well if this method is overwritten.
 	 * </p>
 	 * 
 	 * @param thrift2pig
 	 *            Default mapping (defensively copied)
 	 * @param thriftId
 	 *            thrift type id
 	 * @return pig type id
 	 */
 	protected byte convertType(byte[] thrift2pig, byte thriftId) {
 		return thrift2pig[thriftId];
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
