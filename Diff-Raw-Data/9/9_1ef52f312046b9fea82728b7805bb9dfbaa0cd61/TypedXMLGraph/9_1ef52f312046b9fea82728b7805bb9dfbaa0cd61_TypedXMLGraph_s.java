 // Released under GPLv2 or later. See http://www.gnu.org/ for details.
 package tags.io;
 
 import java.util.Collections;
 
 import tags.io.XMLGraph;
 import tags.io.XMLGraph.Node;
 import tags.io.XMLGraph.Arc;
 
 import tags.util.Range;
 import java.util.Set;
 import java.util.Map;
 import java.util.EnumMap;
 import java.util.HashMap;
 import java.util.TreeMap;
 
 import java.io.Reader;
 import java.io.IOException;
 import javax.xml.parsers.ParserConfigurationException;
 import org.xml.sax.SAXException;
 
 import java.lang.reflect.InvocationTargetException;
 
 /**
 ** An extension of {@link XMLGraph} which can distinguish between multiple
 ** types of nodes.
 **
 ** @param <T> Enum of node types
 ** @param <K> Type of primary key
 ** @param <U> Type of default node attribute
 ** @param <W> Type of default arc attribute
 */
 abstract public class TypedXMLGraph<T extends Enum<T>, K, U, W> extends XMLGraph<K, U, W> {
 
 	final protected Class<T> typecl;
 	final protected T[] types;
 
 	protected EnumMap<T, Range> ranges;
 
 	/**
 	** Create a new graph with types from the given enum class.
 	*/
 	public TypedXMLGraph(Class<T> typecl) throws ParserConfigurationException, SAXException {
 		super();
 		this.typecl = typecl;
 		this.types = membersOf(typecl);
 	}
 
 	@Override protected void postLoad() {
 		this.ranges = makeRanges(getGraphAttributes(), vertices.size());
 	}
 
 	public EnumMap<T, Range> makeRanges(Map<String, Object> attrmap, int order) {
 		Map<Integer, T> bases = new TreeMap<Integer, T>(Collections.reverseOrder());
 		for (T type: types) {
 			String attr = getAttributeNameForType(type);
 			Object base = attrmap.get(attr);
 			if (base == null) { throw new IllegalArgumentException("graph does not contain attribute: " + attr); }
 			bases.put((Integer)base, type);
 		}
 
 		EnumMap<T, Range> ranges = new EnumMap<T, Range>(typecl);
 		int last = order;
 		for (Map.Entry<Integer, T> en: bases.entrySet()) {
 			int key = en.getKey();
 			T type = en.getValue();
 			ranges.put(type, new Range(key, last));
 			last = key;
 		}
 		return ranges;
 	}
 
 	/**
 	** Get the name of the graph-attribute which defines the base ID (lowest
 	** numerical ID) for the given type.
 	**
 	** The mapping must be constant and bijective.
 	*/
 	abstract public String getAttributeNameForType(T type);
 
 	/**
 	** Convert a vertex String id (from the GraphML file, accessible by {@link
	** GraphMLReader#getVertexIDs()}) into an int id.
 	**
 	** The mapping must be constant and bijective.
 	*/
 	abstract public int getIDForString(String vid);
 
 	/**
 	** Get the type for a given vertex id. If the vertex id is not found,
 	** returns {@code null}.
 	**
 	** '''Note''': this implementation is linear in the size of the enum; it is
 	** assumed that this is small enough not to be a problem. OPT LOW.
 	*/
 	public T getIDType(int id) {
 		for (Map.Entry<T, Range> en: ranges.entrySet()) {
 			if (en.getValue().contains(id)) {
 				return en.getKey();
 			}
 		}
 		return null;
 	}
 
 	/**
 	** Get the type for a given node.
 	*/
 	public T getNodeType(Node n) {
 		return getIDType(getIDForString(reader.getVertexIDs().get(n)));
 	}
 
 	/**
 	** Splits the given map of nodes up by type. The returned maps are copies,
 	** not views, of the original.
 	*/
 	public <V> EnumMap<T, Map<Node, V>> splitMapByType(Map<Node, V> map) {
 		//vertices.get(nodes.get(key)).getSecond()
 		EnumMap<T, Map<Node, V>> split = new EnumMap<T, Map<Node, V>>(typecl);
 		for (T type: types) {
 			split.put(type, new HashMap<Node, V>()); // TODO LOW map factory
 		}
 
 		for (Map.Entry<Node, V> en: map.entrySet()) {
 			Node key = en.getKey();
 			split.get(getNodeType(key)).put(key, en.getValue());
 		}
 		return split;
 	}
 
 	public EnumMap<T, Set<K>> keyTypedSet() {
 		throw new UnsupportedOperationException("not implemented");
 	}
 
 	public Map<T, Map<K, W>> getSuccessorTypedMap(K key) {
 		EnumMap<T, Map<K, W>> split = new EnumMap<T, Map<K, W>>(typecl);
 		for (Map.Entry<T, Map<Node, Arc>> en: splitMapByType(vertices.get(nodes.get(key)).getSecond()).entrySet()) {
 			split.put(en.getKey(), new KeyAttrMap(en.getValue()));
 		}
 		return split;
 	}
 
 	final private static long serialVersionUID = 4346264698927502245L;
 
 	@SuppressWarnings("unchecked")
 	public static <T extends Enum<T>> T[] membersOf(Class<T> typecl) {
 		try {
 			return (T[])typecl.getMethod("values").invoke(null);
 		} catch (NoSuchMethodException e) {
 			throw new IllegalArgumentException("could not find values() method on " + typecl, e);
 		} catch (IllegalAccessException e) {
 			throw new IllegalArgumentException("could not access values() method on " + typecl, e);
 		} catch (InvocationTargetException e) {
 			throw new IllegalArgumentException("exception thrown by values() method on " + typecl, e);
 		}
 	}
 
 }
