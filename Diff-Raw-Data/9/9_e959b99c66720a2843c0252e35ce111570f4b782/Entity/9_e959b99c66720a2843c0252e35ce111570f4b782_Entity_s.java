 package de.ismll.database.dao;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Vector;
 
 import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 
 import de.ismll.bootstrap.CommandLineParser;
 
 /**
  * an entity itself is a ContentHolder, backed with non-native entity field names.
  * 
  * @author Busche
  *
  */
 public abstract class Entity implements IContentHolder{
 
 
 	private static Logger logger = LogManager.getLogger(Entity.class);
 
 	//	private Hashtable payload =null;
 
 	//	public String getPayload(final Integer arg0) {
 	//		ensureHashtable();
 	//		return (String) payload.get(arg0);
 	//	}
 	//
 	//	void ensureHashtable() {
 	//		Hashtable ht = payload;
 	//		if (ht == null) {
 	//			synchronized(this) {
 	//				if (ht == null) {
 	//					ht = new Hashtable();
 	//				}
 	//				payload=ht;
 	//			}
 	//		}
 	//	}
 	//
 	//
 	//
 	//	public String setPayload(final Integer arg0, final String arg1) {
 	//		ensureHashtable();
 	//		final Object ret = payload.put(arg0, arg1);
 	//		if (ret == null)
 	//			return null;
 	//		return (String)ret;
 	//	}
 
 	private Column KEY_FIELD;
 
 	private Table type;
 
 	private static int ID_COUNTER = 0;
 
 	public final Object[] data;
 
 	//	public Entity() {
 	//		super();
 	//		initType();
 	//
 	//		this.data = new String[type.getFields().size()];
 	//
 	//		init();
 	//	}
 
 	Map<String, Column> lookup;
 
 	protected Entity(final Table type) {
 		super();
		if (this.type == null) {
			this.type = type;
			KEY_FIELD=type.getKeyField();
		}

 		Vector<Column> fields = type.getFields();
 		lookup=new HashMap<String, Column>(fields.size()+1);
 		for (Column c : fields) {
 			lookup.put(c.nativeName, c);
 		}
 
 		this.data = new Object[type.getFields().size()];
 
 		init();
 	}
 
 	//	private void initType() {
 	//		if (type == null) {
 	//			type = EntityType.getEntityType(getClass());
 	//			if (type == null)
 	//				type = registerType();
 	//			KEY_FIELD=type.getKeyField();
 	//		}
 	//	}
 
 	//	public Entity(final String[] data) {
 	//		super();
 	//		initType();
 	//
 	//		this.data = data;
 	//
 	//		init();
 	//	}
 
 	//	public Entity(final ContentHolder data, final boolean nativeNames) {
 	//		super();
 	//		initType();
 	//
 	//		init();
 	//
 	//		this.data = setValuesImpl(data, nativeNames);
 	//	}
 
 	/**
 	 * calls {@link #setValues(IContentHolder, boolean)} while using normal (non-native) names.
 	 * @param cache
 	 */
 	public void setValues(final IContentHolder cache) {
 		setValues(cache, this.data, false);
 	}
 
 	/**
 	 * sets the values as given in cache to this entity.
 	 * 
 	 * if nativeName is true, the keys in cache are mapped according to the {@link Column#getNativeName()} native names of the entity fields,
 	 * else if native name is false, the {@link Column#getName()} is used to map key-value pairs while setting properties
 	 * 
 	 * @param cache
 	 * @param nativeNames
 	 */
 	public void setValues(final IContentHolder cache, final boolean nativeNames) {
 		setValues(cache, this.data, nativeNames);
 	}
 
 	private Object[] setValuesImpl(final IContentHolder cache, final boolean nativeNames) {
 		final Vector fields = type.getFields();
 		final int size = fields.size();
 		final Object[] d;
 		if (this.data == null) {
 			d = new String[size];
 		} else {
 			d = this.data;
 		}
 		setValues(cache, d, nativeNames);
 		return d;
 	}
 
 	private final void setValues(final IContentHolder cache, final Object[] d, final boolean nativeNames) {
 		final Vector<Column> fields = type.getFields();
 		final int size = fields.size();
 		for (int i = size-1; i >= 0; i--) {
 			final Column ef = fields.elementAt(i);
 
 			d[ef.getOrdinal()] = cache.get((nativeNames?ef.getNativeName():ef.getName()));
 		}
 	}
 
 
 	protected abstract Table registerType();
 
 	public Table getEntityType() {
 		return type;
 	}
 
 	public long id=-1;
 
 
 	public long getId() {
 		return id;
 	}
 
 	public void setId(final long id) {
 		this.id = id;
 
 	}
 
 	public Object get(final int field) {
 		if (field>=data.length || field < 0)
 			return null;
 		return data[field];
 	}
 
 	public Object get(final String key) {
 		return get(key, false);
 	}
 
 
 	@Override
 	public <T> T get(String key, Class<T> type) {
 		return (T) CommandLineParser.convert(get(key), type);
 	}
 
 	public Object get(final String key, final boolean nativeName) {
 		return get(type.getField(key, nativeName));
 	}
 
 	public Object get(final Column field) {
 		return get(field.getOrdinal());
 	}
 	//
 	//	public int getInt32(final Column field) {
 	//		//#if ${development} == "true"
 	//		Tools.assertTrue(field.getDatatype() == Datatypes.INTEGER);
 	//		//#endif
 	//		return Tools.integer(data[field.getOrdinal()]);
 	//	}
 	//
 
 	public void init() {
 		for (int i = 0; i < data.length; i++) {
 			data[i] = null;
 		}
 	}
 
 	public String getPayload(final int identifier) {
 		return null;
 	}
 
 	public static int createId() {
 		return ID_COUNTER++;
 	}
 
 	public boolean isEmpty() {
 		return getId()<0;
 	}
 
 	public Object put(final String key, final Object value) {
 		final Column field = type.getField(key, false);
 		Object old;
 		if (field == KEY_FIELD) {
 			old = getId() + "";
 			// overriding ID!?!?!
 			System.err.println("Overriding ID not yet supported!");
 		} else {
 			old = this.data[type.getField(key, false).getOrdinal()];
 			final int ordinal = field.getOrdinal();
 			if (ordinal>=0)
 				this.data[ordinal] = (value instanceof String?(String)value:value.toString());
 		}
 		return old;
 	}
 
 	public int size() {
 		return data.length+1; // plus KEY field...
 	}
 
 	public void clear() {
 		init();
 	}
 
 	//	public static Entity copy(final Entity entity) {
 	//		final Entity copy = entity.getEntityType().createInstance();
 	//
 	//		System.arraycopy(entity.data, 0, copy.data, 0, entity.data.length);
 	//		copy.KEY_FIELD = entity.KEY_FIELD;
 	////		if (entity.payload != null && entity.payload.size()>0) {
 	////			copy.payload=new Hashtable();
 	////			// todo: copy all
 	////		}
 	//
 	//		// copy values
 	//		//		copy.setValues(entity);
 	//
 	//		// copy ID
 	//		copy.setId(entity.getId());
 	//
 	//		return copy;
 	//	}
 
 	public boolean isNew() {
 		// TODO: revise!
 		return getId()<0;
 	}
 
 	public String toString() {
 		final StringBuffer sb = new StringBuffer();
 		sb.append("Entity of type ");
 		sb.append(type.getNativeName());
 		sb.append(": id=");
 		sb.append(id);
 		sb.append(";data=");
 		sb.append(Arrays.toString(data));
 
 		return sb.toString();
 	}
 
 	public void set(final Column field, final Object value) {
 		data[field.getOrdinal()] = value;
 	}
 
 	public void set(String nativeColumnName, Object value) {
 		Column column = lookup.get(nativeColumnName);
 		if (column==null) {
 			logger.warn("column " + nativeColumnName + " not found!");
 			return;
 		}
 		data[column.getOrdinal()] = value;
 	}
 
 }
