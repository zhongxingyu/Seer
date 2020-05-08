 // Released under GPLv2 or later. See http://www.gnu.org/ for details.
 package tags.io;
 
 import tags.io.AttrGraphMLMetadata.AttrType;
 
 import edu.uci.ics.jung.algorithms.util.MapSettableTransformer;
 import edu.uci.ics.jung.algorithms.util.SettableTransformer;
 import edu.uci.ics.jung.io.GraphMLReader;
 import edu.uci.ics.jung.io.GraphMLMetadata;
 import edu.uci.ics.jung.graph.Hypergraph;
 
 import org.apache.commons.collections15.Transformer;
 import org.apache.commons.collections15.Factory;
 import org.apache.commons.collections15.BidiMap;
 import org.apache.commons.collections15.bidimap.DualHashBidiMap;
 import org.apache.commons.collections15.bidimap.UnmodifiableBidiMap;
 
 import javax.xml.parsers.ParserConfigurationException;
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXNotSupportedException;
 import org.xml.sax.SAXException;
 
 import java.util.Collections;
 import java.util.Set;
 import java.util.Map;
 import java.util.HashSet;
 import java.util.HashMap;
 import java.util.EnumMap;
 
 /**
 ** Extension of {@link GraphMLReader} which has extra methods for exploring
 ** {@code <key>} elements (ie. attributes) of GraphML.
 **
 ** @see <a href="http://graphml.graphdrawing.org/primer/graphml-primer.html#AttributesDefinition">2.4.2 Declaring GraphML-Attributes</a>
 */
 public class AttrGraphMLReader<G extends Hypergraph<V, E>, V, E> extends GraphMLReader<G, V, E> {
 
 	final static public Map<String, AttrType> attr_type;
 	final static public Map<AttrType, Class<?>> attr_class;
 
 	static {
 		Map<String, AttrType> tmp1 = new HashMap<String, AttrType>();
 		tmp1.put("boolean", AttrType.BOOLEAN);
 		tmp1.put("int", AttrType.INT);
 		tmp1.put("long", AttrType.LONG);
 		tmp1.put("float", AttrType.FLOAT);
 		tmp1.put("double", AttrType.DOUBLE);
 		tmp1.put("string", AttrType.STRING);
 		attr_type = Collections.unmodifiableMap(tmp1);
 
 		Map<AttrType, Class<?>> tmp2 = new EnumMap<AttrType, Class<?>>(AttrType.class);
 		tmp2.put(AttrType.BOOLEAN, boolean.class);
 		tmp2.put(AttrType.INT, int.class);
 		tmp2.put(AttrType.LONG, long.class);
 		tmp2.put(AttrType.FLOAT, float.class);
 		tmp2.put(AttrType.DOUBLE, double.class);
 		tmp2.put(AttrType.STRING, String.class);
 		attr_class = Collections.unmodifiableMap(tmp2);
 	}
 
 	protected BidiMap<String, String> keyname;
 	protected Map<String, AttrType> keytype;
 	protected Map<String, SettableAttrGraphMLMetadata<G>> graph_attr_metadata;
 	protected Map<String, SettableAttrGraphMLMetadata<V>> vertex_attr_metadata;
 	protected Map<String, SettableAttrGraphMLMetadata<E>> edge_attr_metadata;
 
 	protected BidiMap<String, String> _keyname;
 	protected Map<String, AttrType> _keytype;
 	protected Map<String, AttrGraphMLMetadata<G>> _graph_attr_metadata;
 	protected Map<String, AttrGraphMLMetadata<V>> _vertex_attr_metadata;
 	protected Map<String, AttrGraphMLMetadata<E>> _edge_attr_metadata;
 
 	public AttrGraphMLReader(Factory<V> vertex_factory, Factory<E> edge_factory) throws ParserConfigurationException, SAXException {
 		super(vertex_factory, edge_factory);
 	}
 
 	public AttrGraphMLReader() throws ParserConfigurationException, SAXException {
 		super();
 	}
 
 	@Override public void initializeData() {
 		super.initializeData();

 		this.keyname = new DualHashBidiMap<String, String>();
 		this.keytype = new HashMap<String, AttrType>();
 		this.graph_attr_metadata = new HashMap<String, SettableAttrGraphMLMetadata<G>>();
 		this.vertex_attr_metadata = new HashMap<String, SettableAttrGraphMLMetadata<V>>();
 		this.edge_attr_metadata = new HashMap<String, SettableAttrGraphMLMetadata<E>>();

		this._keyname = null;
		this._keytype = null;
		this._graph_attr_metadata = null;
		this._vertex_attr_metadata = null;
		this._edge_attr_metadata = null;
 	}
 
 	@Override protected void createKey(Attributes atts) throws SAXNotSupportedException
 	{
 		Map<String, String> key_atts = getAttributeMap(atts);
 		String id = key_atts.remove("id");
         String for_type = key_atts.remove("for");
 		String aname = key_atts.remove("attr.name");
 		String atype = key_atts.remove("attr.type");
 
 		if (keyname.containsKey(id) || keyname.containsValue(aname)) {
 			throw new SAXNotSupportedException("duplicate key " + id + ":" + aname);
 		}
 
 		keyname.put(id, aname);
 		AttrType t = attr_type.get(atype);
 
 		assert !keytype.containsKey(id);
 		keytype.put(id, t == null? AttrType.STRING: t);
 
 		// Below is basically the same as super.createKey() but with
 		// new MetadataBuilder() instead of new GraphMLMetadata
 
 		if (for_type == null || for_type.equals("") || for_type.equals("all"))
 		{
 			vertex_metadata.put(id, new MetadataBuilder<V>(t));
 			edge_metadata.put(id, new MetadataBuilder<E>(t));
 			graph_metadata.put(id, new MetadataBuilder<G>(t));
 			key_type = KeyType.ALL;
 		}
 		else
 		{
 			TagState type = tag_state.get(for_type);
 			switch (type)
 			{
 				case VERTEX:
 					vertex_metadata.put(id, new MetadataBuilder<V>(t));
 					key_type = KeyType.VERTEX;
 					break;
 				case EDGE:
 				case HYPEREDGE:
 					edge_metadata.put(id, new MetadataBuilder<E>(t));
 					key_type = KeyType.EDGE;
 					break;
 				case GRAPH:
 					graph_metadata.put(id, new MetadataBuilder<G>(t));
 					key_type = KeyType.GRAPH;
 					break;
 				default:
 					throw new SAXNotSupportedException(
 							"Invalid metadata target type: " + for_type);
 			}
 		}
 
 		this.current_key = id;
 	}
 
 	@Override protected <T> void addDatum(Map<String, GraphMLMetadata<T>> metadata,
 		T current_elt, String text) throws SAXNotSupportedException
 	{
 		// do nothing
 	}
 
 	protected <T> void addAttrDatum(Map<String, SettableAttrGraphMLMetadata<T>> metadata,
 		T current_elt, String text) throws SAXNotSupportedException
 	{
 		if (metadata.containsKey(this.current_key))
 		{
 			SettableAttrGraphMLMetadata<T> attr_mt = metadata.get(this.current_key);
 			try
 			{
 				switch (attr_mt.type)
 				{
 					case BOOLEAN:
 						SettableTransformer<T, Boolean> transBoolean = attr_mt.transformerBoolean();
 						transBoolean.set(current_elt, Boolean.parseBoolean(text));
 						break;
 					case INT:
 						SettableTransformer<T, Integer> transInteger = attr_mt.transformerInteger();
 						transInteger.set(current_elt, Integer.parseInt(text));
 						break;
 					case LONG:
 						SettableTransformer<T, Long> transLong = attr_mt.transformerLong();
 						transLong.set(current_elt, Long.parseLong(text));
 						break;
 					case FLOAT:
 						SettableTransformer<T, Float> transFloat = attr_mt.transformerFloat();
 						transFloat.set(current_elt, Float.parseFloat(text));
 						break;
 					case DOUBLE:
 						SettableTransformer<T, Double> transDouble = attr_mt.transformerDouble();
 						transDouble.set(current_elt, Double.parseDouble(text));
 						break;
 					case STRING:
 						SettableTransformer<T, String> transString = attr_mt.transformerString();
 						transString.set(current_elt, text);
 						break;
 
 					default:
 						throw new SAXNotSupportedException("unrecognised type " + attr_mt.type +
 							" for key " + this.current_key + " for element " + current_elt);
 				}
 
 			}
 			catch (NumberFormatException e)
 			{
 				throw (SAXNotSupportedException)new SAXNotSupportedException(
 					"value " + text + " not valid for key " + this.current_key +
 					" of type " + attr_mt.type + " for element " + current_elt).initCause(e);
 			}
 		}
 		else
 			throw new SAXNotSupportedException("key " + this.current_key +
 					" not valid for element " + current_elt);
 	}
 
 	@Override protected <T> void addExtraData(Map<String, String> atts,
 			Map<String, GraphMLMetadata<T>> metadata_map, T current_elt)
 	{
 		super.addExtraData(atts, metadata_map, current_elt);
 		// TODO NORM
 		// should really have a separate transformer for storing unrecognised attributes
 	}
 
 	@Override public void endElement(String uri, String name, String qName) throws SAXNotSupportedException
 	{
 		// Save these fields into local var because super.endElement() might destroy them
 		String current_key = this.current_key;
 		GraphMLReader.TagState prev_state = this.current_states.size() > 1? this.current_states.get(1): null;
 		String text = current_text.toString().trim();
 
 		super.endElement(uri, name, qName);
 
 		switch(tag_state.get(qName.toLowerCase()))
 		{
 			case KEY:
 				assert this.current_key == null; // should have been done by super.endElement()
 				switch (key_type)
 				{
 					case GRAPH:
 						buildAttrMetadata(current_key, graph_metadata, graph_attr_metadata);
 						break;
 					case VERTEX:
 						buildAttrMetadata(current_key, vertex_metadata, vertex_attr_metadata);
 						break;
 					case EDGE:
 						buildAttrMetadata(current_key, edge_metadata, edge_attr_metadata);
 						break;
 					case ALL:
 						buildAttrMetadata(current_key, graph_metadata, graph_attr_metadata);
 						buildAttrMetadata(current_key, vertex_metadata, vertex_attr_metadata);
 						buildAttrMetadata(current_key, edge_metadata, edge_attr_metadata);
 						break;
 					default:
 						throw new SAXNotSupportedException("Invalid key type" +
 								" specified for default: " + key_type);
 				}
 				break;
 
 			case DATA:
 				assert prev_state != null;
 
 				switch (prev_state)
 				{
 					case GRAPH:
 						addAttrDatum(graph_attr_metadata, current_graph, text);
 						break;
 					case VERTEX:
 					case ENDPOINT:
 						addAttrDatum(vertex_attr_metadata, current_vertex, text);
 						break;
 					case EDGE:
 					case HYPEREDGE:
 						addAttrDatum(edge_attr_metadata, current_edge, text);
 						break;
 					default:
 						break;
 				}
 				break;
 
 			default:
 				break;
 		}
 
 	}
 
 	protected <T> AttrGraphMLMetadata<T> buildAttrMetadata(String key,
 		Map<String, GraphMLMetadata<T>> metadata, Map<String, SettableAttrGraphMLMetadata<T>> attr_metadata)
 	{
 		MetadataBuilder<T> mt = (MetadataBuilder<T>)metadata.get(key);
 		SettableAttrGraphMLMetadata<T> attr_mt = mt.build();
 		attr_metadata.put(key, attr_mt);
 		mt.transformer = attr_mt.transformer();
 		return attr_mt;
 	}
 
 	/**
 	** Returns a bidirectional map relating key IDs and key attribute-names.
 	*/
 	public BidiMap<String, String> getKeyNames() {
 		if (_keyname == null) {
 			_keyname = UnmodifiableBidiMap.decorate(this.keyname);
 		}
 		return _keyname;
 	}
 
 	/**
 	** Returns a map from key ID to attribute-type.
 	*/
 	public Map<String, AttrType> getKeyTypes() {
 		if (_keytype == null) {
 			_keytype = Collections.unmodifiableMap(this.keytype);
 		}
 		return _keytype;
 	}
 
 	public Map<String, AttrGraphMLMetadata<G>> getGraphAttrMetadata() {
 		if (_graph_attr_metadata == null) {
 			_graph_attr_metadata = Collections.<String, AttrGraphMLMetadata<G>>unmodifiableMap(this.graph_attr_metadata);
 		}
 		return _graph_attr_metadata;
 	}
 
 	public Map<String, AttrGraphMLMetadata<V>> getVertexAttrMetadata() {
 		if (_vertex_attr_metadata == null) {
 			_vertex_attr_metadata = Collections.<String, AttrGraphMLMetadata<V>>unmodifiableMap(this.vertex_attr_metadata);
 		}
 		return _vertex_attr_metadata;
 	}
 
 	public Map<String, AttrGraphMLMetadata<E>> getEdgeAttrMetadata() {
 		if (_edge_attr_metadata == null) {
 			_edge_attr_metadata = Collections.<String, AttrGraphMLMetadata<E>>unmodifiableMap(this.edge_attr_metadata);
 		}
 		return _edge_attr_metadata;
 	}
 
 
 	private static class MetadataBuilder<T> extends GraphMLMetadata<T> {
 
 		public AttrType type;
 
 		public MetadataBuilder(AttrType type) {
 			super(null, null, null);
 			if (type == null) { throw new NullPointerException(); }
 			this.type = type;
 		}
 
 		public SettableAttrGraphMLMetadata<T> build() {
 			return new SettableAttrGraphMLMetadata<T>(type, default_value, description);
 		}
 
 	}
 
 	private static class SettableAttrGraphMLMetadata<T> extends AttrGraphMLMetadata<T> {
 
 		public SettableAttrGraphMLMetadata(AttrType type, String default_value, String description) {
 			super(type, default_value, description, new MapSettableTransformer<T, Object>(new HashMap<T, Object>()));
 		}
 
 		@Override public SettableTransformer<T, Boolean> transformerBoolean() {
 			return (SettableTransformer<T, Boolean>)super.transformerBoolean();
 		}
 
 		@Override public SettableTransformer<T, Integer> transformerInteger() {
 			return (SettableTransformer<T, Integer>)super.transformerInteger();
 		}
 
 		@Override public SettableTransformer<T, Long> transformerLong() {
 			return (SettableTransformer<T, Long>)super.transformerLong();
 		}
 
 		@Override public SettableTransformer<T, Float> transformerFloat() {
 			return (SettableTransformer<T, Float>)super.transformerFloat();
 		}
 
 		@Override public SettableTransformer<T, Double> transformerDouble() {
 			return (SettableTransformer<T, Double>)super.transformerDouble();
 		}
 
 		@Override public SettableTransformer<T, String> transformerString() {
 			return (SettableTransformer<T, String>)super.transformerString();
 		}
 
 	}
 
 }
