 package de.hsb.ms.syn.common.vo;
 
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import de.hsb.ms.syn.common.util.Utils;
 
 /**
  * Each Delegate that is assigned to a DraggableNode holds
  * an instance of this class, which encapsulates necessary parameters
  * for the Delegate's algorithm. This includes general parameters
  * needed for all types of sound manipulation algorithms (volume, pan,
  * frequency), but may also involve specific parameters that may be added
  * indepently
  * @author Marcel
  *
  */
 public class NodeProperties implements Serializable, Iterable<NodeProperty> {
 	
 	private static final long serialVersionUID = 3099702165119324979L;
 	
 	// Type String constants
 	public static final String TYPE_GEN	= "GEN";
 	public static final String TYPE_FX	= "FX";
 	
 	// Parameter constants for general purpose params
 	public static final int PROP_VOLUME		= 0x12;
 	public static final int PROP_FREQUENCY	= 0x13;
 	public static final int PROP_PAN		= 0x14;
 	public static final int PROP_TONE		= 0x15;
 	
 	// Internal map of the parameters connected to specific NodeProperty objects
 	private Map<Integer, NodeProperty> properties;
 	
 	// Name of the DraggableNode that holds these NodeProperties
 	private String name;
 	
 	// Index of the Node that holds these NodeProperties
 	private int nodeIndex;
 	
 	/**
 	 * Constructor
 	 * @param name		Name of the Node these properties belong to
 	 * @param nodeIndex Index of the Node these properties belong to
 	 * @param volume	Initial volume (default should be 0.1f)
 	 * @param frequency	Frequency
 	 * @param pan		Initial pan (from -1.0 (all the way left) to +1.0 (all the way right))
 	 */
 	public NodeProperties(String name, int nodeIndex, float volume, float frequency, float pan) {
 		
 		this.name = name;
 		this.nodeIndex = nodeIndex;
 		
 		properties = new HashMap<Integer, NodeProperty>();
 		properties.put(PROP_VOLUME, new NodeProperty(PROP_VOLUME, "Volume", 0.0f, 1.0f, 0.01f, volume));
 		properties.put(PROP_FREQUENCY, new NodeProperty(PROP_FREQUENCY, "Frequency", 20, 2000, 100, frequency));
 		properties.put(PROP_PAN, new NodeProperty(PROP_PAN, "Pan", -1.0f, 1.0f, 0.1f, pan));
 	}
 	
 	public NodeProperties(NodeProperties toCopyFrom) {
 		this.name = toCopyFrom.name();
 		this.nodeIndex = toCopyFrom.nodeIndex();
 		properties = new HashMap<Integer, NodeProperty>();
 		for (NodeProperty prop : toCopyFrom.properties.values()) {
 			if (!prop.isHidden())
 				properties.put(prop.id(), new NodeProperty(prop, prop.val()));
 		}
 	}
 	
 	/**
 	 * Get the type of Node that these NodeProperties belong to
 	 * @return
 	 */
 	public String name() {
 		return name;
 	}
 	
 	/**
 	 * Get the Node index
 	 * @return
 	 */
 	public int nodeIndex() {
 		return nodeIndex;
 	}
 	
 	/**
 	 * Get a specific NodeProperty using a key either from this class or the specific algorithm implementation
 	 * @param key
 	 * @return
 	 */
 	public NodeProperty get(int key) {
		if (properties.containsKey(key))
 			return properties.get(key);
 		
 		Utils.log("NodeProperties: Can't find Key " + key);
 		return null;
 	}
 	
 	/**
 	 * Put a NodeProperty into the map using the given key
 	 * @param key
 	 * @param prop
 	 */
 	public void put(int key, NodeProperty prop) {
 		properties.put(key, prop);
 	}
 	
 	/**
 	 * Remove a NodeProperty by key from the list of NodeProperties
 	 * @param key
 	 */
 	public void remove(int key) {
		if (properties.containsKey(key))
 			properties.remove(key);
 	}
 	
 	/**
 	 * toString override
 	 */
 	public String toString() {
 		String s = "";
 		for (NodeProperty p : properties.values())
 			s += String.format("{%s},", p);
 		return s;
 	}
 
 	@Override
 	public Iterator<NodeProperty> iterator() {
 		return properties.values().iterator();
 	}
 
 	public void setNodeIndex(int id) {
 		this.nodeIndex = id;
 	}
 
 	public NodeProperties copy() {
 		return new NodeProperties(this);
 	}
 }
