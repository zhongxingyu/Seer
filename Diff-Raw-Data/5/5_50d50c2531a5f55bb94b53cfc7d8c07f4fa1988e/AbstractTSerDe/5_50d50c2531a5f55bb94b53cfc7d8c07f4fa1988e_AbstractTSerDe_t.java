 package jp.gr.java_conf.afterthesunrise.thadoop;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 import static org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory.getStandardStructObjectInspector;
 import static org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory.javaBooleanObjectInspector;
 import static org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory.javaByteObjectInspector;
 import static org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory.javaDoubleObjectInspector;
 import static org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory.javaIntObjectInspector;
 import static org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory.javaLongObjectInspector;
 import static org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory.javaShortObjectInspector;
 import static org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory.javaStringObjectInspector;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Properties;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.hive.serde2.SerDe;
 import org.apache.hadoop.hive.serde2.SerDeException;
 import org.apache.hadoop.hive.serde2.SerDeStats;
 import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
 import org.apache.hadoop.hive.serde2.objectinspector.StructField;
 import org.apache.hadoop.hive.serde2.objectinspector.StructObjectInspector;
 import org.apache.hadoop.io.Writable;
 import org.apache.thrift.TBase;
 import org.apache.thrift.TFieldIdEnum;
 import org.apache.thrift.meta_data.FieldMetaData;
 import org.apache.thrift.protocol.TType;
 
 /**
  * Hive SerDe implementation to access thrift objects from hive queries.
  * 
  * @author takanori.takase
  * 
  * @param <F>
  *            Underlying thrift type's field type.
  * @param <W>
  *            Subclass of AbstractTWritable to handle.
  */
public abstract class AbstractTSerDe<F extends TFieldIdEnum, W extends AbstractTWritable<? extends TBase<?, F>>>
 		implements SerDe {
 
 	/**
 	 * A mapping to convert thrift type to hive type.
 	 */
 	private static final Map<Byte, ObjectInspector> INSPECTORS;
 
 	static {
 		Map<Byte, ObjectInspector> map = new HashMap<>();
 		map.put(TType.BOOL, javaBooleanObjectInspector);
 		map.put(TType.BYTE, javaByteObjectInspector);
 		map.put(TType.I16, javaShortObjectInspector);
 		map.put(TType.I32, javaIntObjectInspector);
 		map.put(TType.I64, javaLongObjectInspector);
 		map.put(TType.DOUBLE, javaDoubleObjectInspector);
 		map.put(TType.STRING, javaStringObjectInspector);
 		map.put(TType.ENUM, javaStringObjectInspector);
 		INSPECTORS = Collections.unmodifiableMap(map);
 	}
 
 	private final Class<W> clazz;
 
 	private final SortedMap<F, FieldMetaData> fields;
 
 	private ObjectInspector objectInspector;
 
 	private List<Object> cacheList;
 
 	private W cacheWritable;
 
 	protected AbstractTSerDe(Class<W> clazz, Map<F, FieldMetaData> fields) {
 		this.clazz = checkNotNull(clazz);
 		this.fields = generateMap(fields);
 	}
 
 	private SortedMap<F, FieldMetaData> generateMap(Map<F, FieldMetaData> fields) {
 
 		Comparator<F> comparator = TFieldIdEnumComparator.get();
 
 		SortedMap<F, FieldMetaData> map = new TreeMap<>(comparator);
 
 		map.putAll(fields);
 
 		return Collections.unmodifiableSortedMap(map);
 
 	}
 
 	@Override
 	public void initialize(Configuration c, Properties p) throws SerDeException {
 
 		List<String> keys = new ArrayList<>(fields.size());
 
 		List<ObjectInspector> inspectors = new ArrayList<>(fields.size());
 
 		for (Entry<F, FieldMetaData> entry : fields.entrySet()) {
 
 			F field = entry.getKey();
 
 			FieldMetaData d = entry.getValue();
 
 			ObjectInspector inspector = extractInspector(field, d, INSPECTORS);
 
 			String name = field.getFieldName();
 
 			if (inspector == null) {
 				throw new SerDeException("No inspector for field : " + name);
 			}
 
 			keys.add(name);
 
 			inspectors.add(inspector);
 
 		}
 
 		objectInspector = getStandardStructObjectInspector(keys, inspectors);
 
 	}
 
 	protected ObjectInspector extractInspector(F field, FieldMetaData metaData,
 			Map<Byte, ObjectInspector> inspectors) throws SerDeException {
 
		byte id = metaData.valueMetaData.type;
 
 		return inspectors.get(id);
 
 	}
 
 	@Override
 	public ObjectInspector getObjectInspector() throws SerDeException {
 		return objectInspector;
 	}
 
 	@Override
 	public SerDeStats getSerDeStats() {
 		return null;
 	}
 
 	@Override
 	public Object deserialize(Writable writable) throws SerDeException {
 
 		if (cacheList == null) {
 			cacheList = new ArrayList<>(fields.size());
 		} else {
 			cacheList.clear();
 		}
 
 		W w = clazz.cast(writable);
 
 		for (F field : fields.keySet()) {
 
 			Object val = extractValue(w, field);
 
 			cacheList.add(val);
 
 		}
 
 		return cacheList;
 
 	}
 
 	protected Object extractValue(W writable, F field) throws SerDeException {
 		return writable.get().getFieldValue(field);
 	}
 
 	@Override
 	public Class<? extends Writable> getSerializedClass() {
 		return clazz;
 	}
 
 	@Override
 	public Writable serialize(Object data, ObjectInspector inspector)
 			throws SerDeException {
 
 		if (cacheWritable == null) {
 			try {
 				cacheWritable = clazz.newInstance();
 			} catch (InstantiationException | IllegalAccessException e) {
 				throw new SerDeException(e);
 			}
 		} else {
 			cacheWritable.get().clear();
 		}
 
 		StructObjectInspector i = (StructObjectInspector) inspector;
 
 		for (F field : fields.keySet()) {
 
 			StructField sf = i.getStructFieldRef(field.getFieldName());
 
 			Object value = i.getStructFieldData(data, sf);
 
 			setFieldValue(cacheWritable, field, value);
 
 		}
 
 		return cacheWritable;
 
 	}
 
 	protected void setFieldValue(W writable, F field, Object value) {
 		writable.get().setFieldValue(field, value);
 	}
 
 }
