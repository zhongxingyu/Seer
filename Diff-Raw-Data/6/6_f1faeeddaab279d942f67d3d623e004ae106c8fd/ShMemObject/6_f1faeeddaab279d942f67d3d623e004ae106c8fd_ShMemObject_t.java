 package mp;
 
 
 import mp.ITimestamp.Comparison;
 
 import org.codehaus.jackson.*;
 import org.codehaus.jackson.node.*;
 import org.codehaus.jackson.node.ArrayNode;
 import org.codehaus.jackson.node.ObjectNode;
 
 import java.math.BigDecimal;
 import java.util.*;
 
 import org.apache.commons.lang3.tuple.*;
 
 /*
  * Users should use ShMemObject where the intended to use JSONObjects. A user may still
  * user JSONObjects but they will not be interpreted in any way. If both the acquiring and
  * releasing processes make changes to unrelated parts of a JSONObject, the updates will be
  * marked conflicting. 
  */
 public class ShMemObject extends ObjectNode {
 	
 	private static JsonNodeFactory s_factory;
 	
 	// Points to the parent of this object in the recursive ShMemObject hierarchy. 
 	private ShMemObject parent;
 	
 	// The key the parent uses to refer to this object. We need the key to update timestamp
 	// information in the parent recursively. 
 	private String parent_key;
 	
 	// The current value of "time". All puts to an ShMemObject will take this value. The value
 	// is changed whenever we fork. 
 	public static ArrayNode m_now;
 	
 	// Index of the current node in the timestamps. 
 	public static int cur_node_;
 	
 	private static int s_first_free = 1;
 	private static ListNode[] s_list_nodes = new ListNode[10000000];
 	
 	// Initialize an empty ShMemObject. 
 	public ShMemObject() {
 		super(JsonNodeFactory.instance);
 		this.parent = null;
 		this.parent_key = "";
 		m_key_map = new HashMap<String, Integer>();
 		m_sorted_keys = new InternalLinkedList();
 	}
 	
 	private HashMap<String, Integer> m_key_map;
 	private InternalLinkedList m_sorted_keys;
 	
 	private class ListNode {
 		
 		public final String m_key;
 		public final ArrayNode m_timestamp;
 		
 		public ListNode m_prev;
 		public ListNode m_next;
 		
 		public ListNode(String key, ArrayNode timestamp) {
 			m_key = key;
 			m_timestamp = timestamp;
 			m_prev = null;
 			m_next = null;
 		}
 	}
 	
 	private class InternalLinkedList {
 		
 		public ListNode m_head;
 		public ListNode m_tail;
 		
 		public InternalLinkedList() {
 			m_head = null;
 			m_tail = null;
 		}
 		
 		public void InsertFront(ListNode cur) {
 			if (m_head == null) {
 				m_head = cur;
 				m_tail = cur;
 			}
 			else {
 				cur.m_prev = null;
 				cur.m_next = m_head;
 				m_head = cur;
 			}
 		}
 		
 		public void InsertLast(ListNode cur) {
 			if (m_head == null) {
 				m_head = cur;
 				m_tail = cur;
 			}
 			else {
 				cur.m_prev = m_tail;
 				m_tail.m_next = cur;
 				cur.m_next = null;
 			}
 		}
 		
 		public void Remove(ListNode cur) {
 			ListNode prev = cur.m_prev;
 			ListNode next = cur.m_next;
 			
 			if (prev != null) {
 				prev.m_next = next;
 			}
 			if (cur != null) {
 				next.m_prev = prev;
 			}
 			
 			cur.m_prev = null;
 			cur.m_next = null;
 		}
 	}
 	
 	// This method is used to parse out an ShMemObject after it has been serialized to
 	// a JSONObject. Used when we transmit diffs to the acquirer. 
 	
 	/*
 	public static ShMemObject json2shmem(JSONObject root) throws ShMemFailure {
 		ShMemObject ret = new ShMemObject();
 		
 		// 1. Go through the keys and wrap them into JSONObjects. 
 		// 2. Add each wrapped value into the ShMemObject's map. 
 		// Do the above recursively.. 
 		for (Object k : root.keySet()) {
 				
 			// Get the key.
 			String key = (String)k;
 			
 			// Get the wrapped value and its timestamp.  
 			JSONObject wrapper_value = (JSONObject)root.get(k);
 			Object value = wrapper_value.get("value");
 			if (value.getClass() == JSONObject.class) {
 				
 				// Recursively do this on this object's children. 
 				ShMemObject child = json2shmem((JSONObject)value);
 				value = child;
 			}
 			
 			ITimestamp child_ts = VectorTimestamp.Deserialize((String)wrapper_value.get("shmem_timestamp"),
 															  cur_node_);
 			
 			// Temporarily switch timestamps so that the inserted values have the right
 			// timestamp. 
 			// 
 			// The child's timestamp shouldn't be subsumed by now_.
 			assert(child_ts.Compare(now_) != Comparison.LT);	
 			ret.insert(key,  value,  child_ts);
 		}
 		return ret;
 	}
 	
 	*/
 	
 	private static void fixTime(ShMemObject cur, ArrayNode time) {
 		while (cur.parent != null) {
 			String key = cur.parent_key;
 			ObjectNode val = (ObjectNode)cur.parent.getWrapper(key);
 			ArrayNode curr_timestamp = (ArrayNode)val.get("shmem_timestamp");
 			Comparison comp = VectorTimestamp.Compare(curr_timestamp,  time);
 			if (comp == Comparison.LT) {
 				VectorTimestamp.Union(curr_timestamp,  time);
				
				int node_index = cur.parent.m_key_map.get(cur.parent_key);
				ListNode cur_node = s_list_nodes[node_index];
				cur.parent.m_sorted_keys.Remove(cur_node);
				cur.parent.m_sorted_keys.InsertFront(cur_node);
				
 				cur = cur.parent;
 			}
 			else {
 				break;
 			}
 		}
 	}
 	
 	private ArrayNode getNodeTimestamp(String key) {
 		return (ArrayNode)super.get(key).get("shmem_timestamp");
 	}
 	
 	
 	
 	public static ObjectNode get_diff_tree(JsonNode obj, ArrayNode ts) {
 		ObjectNode ret = ShMem.mapper.createObjectNode();
 		Iterator<String> keys = obj.getFieldNames();
 		
 		while (keys.hasNext()) {
 			String cur = keys.next();
 			JsonNode value = obj.get(cur);
 			ArrayNode cur_time = ((ShMemObject)obj).getNodeTimestamp(cur);
 			
 			if (VectorTimestamp.Compare(ts, cur_time) == Comparison.LT) {
 				JsonNode value_tree;
 				if (value.isObject()) {
 					value_tree = get_diff_tree(value, ts);
 				}
 				else {
 					value_tree = value;
 				}
 				ObjectNode temp = ShMem.mapper.createObjectNode();
 				temp.put("shmem_timestamp", cur_time);
 				temp.put("value", value_tree);
 				ret.put(cur, temp);
 			}
 		}
 		return ret;
 	}
 	
 	/*
 	//
 	// Recursively select the timestamps from the object for merging.
 	//
 	public static JSONObject get_diff_tree(ShMemObject obj, ITimestamp from_ts) throws ShMemFailure{
 		JSONObject ret = new JSONObject();
 		
 		// Get the set of keys from this object. 
 		for (Object k : obj.keySet()) {
 			assert k.getClass() == String.class;
 			
 			// All values are encapsulated in a JSONObject to record timestamp
 			// information, get the wrapper. 
 			JSONObject val = (JSONObject)obj.get(k);
 			
 			// We only bother adding the subtree to diffs if it has been updated since the
 			// reference timestamp. 
 			// INVARIANT: The root's timestamp is the maximum timestamp of any of its leaves. 
 			// Hence, if the root's timestamp is less than or equal to from_ts, none of the internal
 			// nodes can ever have a timestamp greater than from_ts. 
 			ITimestamp timestamp = (ITimestamp)val.get("shmem_timestamp");
 			if (timestamp.Compare(from_ts) == Comparison.GE) {
 			
 				JSONObject to_add = new JSONObject();
 				to_add.put("shmem_timestamp",  timestamp.Serialize());
 				Object value = val.get("value");
 				
 				// If the value is an ShMemObject, recursively add the right diffs. 
 				if (value.getClass() == ShMemObject.class) {
 					to_add.put("value",  get_diff_tree((ShMemObject)value, from_ts)); 
 				}
 				
 				// This is a leaf, add the diff. 
 				else {
 					to_add.put("value",  value);
 				}
 				ret.put(k,  to_add);
 			}
 		}
 		return ret;
 	}
 	*/
 	/*
 	public ITimestamp MergeTime() throws ShMemFailure {
 		ITimestamp ret = now_.Copy();
 		for (Object k : this.keySet()) {
 			ITimestamp temp = (ITimestamp)((JSONObject)super.get(k)).get("shmem_timestamp");
 			ret.Union(temp);
 		}
 		return ret;
 	}
 	*/
 	
 	private ObjectNode getWrapper(String key) {
 		return (ObjectNode)super.get(key);
 	}
 	
 	public ArrayNode getTime() {
 		ArrayNode max = VectorTimestamp.s_zero;
 		Iterator<Map.Entry<String,JsonNode>> fields = super.getFields();
 		
 		while (fields.hasNext()) {
 			Map.Entry<String, JsonNode> cur = fields.next();
 			ArrayNode cur_ts = (ArrayNode)cur.getValue().get("shmem_timestamp");
 			if (VectorTimestamp.Compare(cur_ts, max) == Comparison.GT) {
 				max = cur_ts;
 			}
 		}
 		
 		// If the timestamp is zero then it means that this ShMemNode doesn't
 		// yet have any fields. 
 		if (max.equals(VectorTimestamp.s_zero)) {
 			return m_now;
 		}
 		else {
 			return max;
 		}
 		
 	}
 	
 	@Override
 	public void put(String fieldname, BigDecimal v) {
 		
 		int node_index;
 		try {
 			node_index = m_key_map.get(fieldname);
 		}
 		catch (Exception e) {
 			node_index = 0;
 		}
 		if (node_index == 0) {
 			
 			// Allocate a list node for the new key. 
 			node_index = s_first_free;
 			s_first_free += 1;
 			ArrayNode new_timestamp = VectorTimestamp.Copy(m_now);
 			s_list_nodes[node_index] = new ListNode(fieldname, new_timestamp);
 			m_key_map.put(fieldname,  node_index);
 			
 			ObjectNode to_add = ShMem.mapper.createObjectNode();
 			to_add.put("value",  v);
 			to_add.put("shmem_timestamp",  new_timestamp);
 			super.put(fieldname,  to_add);
 			
 			m_sorted_keys.InsertFront(s_list_nodes[node_index]);
 		}
 		else {	// There already exists a list node and timestamp. 
 			ObjectNode wrapper_value = (ObjectNode)super.get(fieldname);
 			wrapper_value.put("value",  v);
 			ArrayNode old_timestamp = (ArrayNode)wrapper_value.get("shmem_timestamp");
 			VectorTimestamp.CopyFromTo(m_now, old_timestamp);
 			
 			m_sorted_keys.Remove(s_list_nodes[node_index]);
 			m_sorted_keys.InsertFront(s_list_nodes[node_index]);
 		}
 		
 		fixTime(this, m_now);
 	}
 	
 	@Override
 	public void put(String fieldname, boolean v) {
 		int node_index;
 		try {
 			node_index = m_key_map.get(fieldname);
 		}
 		catch (Exception e) {
 			node_index = 0;
 		}
 		if (node_index == 0) {
 			
 			// Allocate a list node for the new key. 
 			node_index = s_first_free;
 			s_first_free += 1;
 			ArrayNode new_timestamp = VectorTimestamp.Copy(m_now);
 			s_list_nodes[node_index] = new ListNode(fieldname, new_timestamp);
 			m_key_map.put(fieldname,  node_index);
 			
 			ObjectNode to_add = ShMem.mapper.createObjectNode();
 			to_add.put("value",  v);
 			to_add.put("shmem_timestamp",  new_timestamp);
 			super.put(fieldname,  to_add);
 			
 			m_sorted_keys.InsertFront(s_list_nodes[node_index]);
 		}
 		else {	// There already exists a list node and timestamp. 
 			ObjectNode wrapper_value = (ObjectNode)super.get(fieldname);
 			wrapper_value.put("value",  v);
 			ArrayNode old_timestamp = (ArrayNode)wrapper_value.get("shmem_timestamp");
 			VectorTimestamp.CopyFromTo(m_now, old_timestamp);
 			
 			m_sorted_keys.Remove(s_list_nodes[node_index]);
 			m_sorted_keys.InsertFront(s_list_nodes[node_index]);
 		}
 		
 		fixTime(this, m_now);
 	}
 	
 	@Override
 	public void put(String fieldname, byte[] v) {
 		int node_index;
 		try {
 			node_index = m_key_map.get(fieldname);
 		}
 		catch (Exception e) {
 			node_index = 0;
 		}
 		if (node_index == 0) {
 			
 			// Allocate a list node for the new key. 
 			node_index = s_first_free;
 			s_first_free += 1;
 			ArrayNode new_timestamp = VectorTimestamp.Copy(m_now);
 			s_list_nodes[node_index] = new ListNode(fieldname, new_timestamp);
 			m_key_map.put(fieldname,  node_index);
 			
 			ObjectNode to_add = ShMem.mapper.createObjectNode();
 			to_add.put("value",  v);
 			to_add.put("shmem_timestamp",  new_timestamp);
 			super.put(fieldname,  to_add);
 			
 			m_sorted_keys.InsertFront(s_list_nodes[node_index]);
 		}
 		else {	// There already exists a list node and timestamp. 
 			ObjectNode wrapper_value = (ObjectNode)super.get(fieldname);
 			wrapper_value.put("value",  v);
 			ArrayNode old_timestamp = (ArrayNode)wrapper_value.get("shmem_timestamp");
 			VectorTimestamp.CopyFromTo(m_now, old_timestamp);
 			
 			m_sorted_keys.Remove(s_list_nodes[node_index]);
 			m_sorted_keys.InsertFront(s_list_nodes[node_index]);
 		}
 		
 		fixTime(this, m_now);
 	}
 	
 	@Override
 	public void put(String fieldname, double v) {
 		int node_index;
 		try {
 			node_index = m_key_map.get(fieldname);
 		}
 		catch (Exception e) {
 			node_index = 0;
 		}
 		if (node_index == 0) {
 			
 			// Allocate a list node for the new key. 
 			node_index = s_first_free;
 			s_first_free += 1;
 			ArrayNode new_timestamp = VectorTimestamp.Copy(m_now);
 			s_list_nodes[node_index] = new ListNode(fieldname, new_timestamp);
 			m_key_map.put(fieldname,  node_index);
 			
 			ObjectNode to_add = ShMem.mapper.createObjectNode();
 			to_add.put("value",  v);
 			to_add.put("shmem_timestamp",  new_timestamp);
 			super.put(fieldname,  to_add);
 			
 			m_sorted_keys.InsertFront(s_list_nodes[node_index]);
 		}
 		else {	// There already exists a list node and timestamp. 
 			ObjectNode wrapper_value = (ObjectNode)super.get(fieldname);
 			wrapper_value.put("value",  v);
 			ArrayNode old_timestamp = (ArrayNode)wrapper_value.get("shmem_timestamp");
 			VectorTimestamp.CopyFromTo(m_now, old_timestamp);
 			
 			m_sorted_keys.Remove(s_list_nodes[node_index]);
 			m_sorted_keys.InsertFront(s_list_nodes[node_index]);
 		}
 		
 		fixTime(this, m_now);
 	}
 	
 	@Override
 	public void put(String fieldname, float v) {
 		int node_index;
 		try {
 			node_index = m_key_map.get(fieldname);
 		}
 		catch (Exception e) {
 			node_index = 0;
 		}
 		if (node_index == 0) {
 			
 			// Allocate a list node for the new key. 
 			node_index = s_first_free;
 			s_first_free += 1;
 			ArrayNode new_timestamp = VectorTimestamp.Copy(m_now);
 			s_list_nodes[node_index] = new ListNode(fieldname, new_timestamp);
 			m_key_map.put(fieldname,  node_index);
 			
 			ObjectNode to_add = ShMem.mapper.createObjectNode();
 			to_add.put("value",  v);
 			to_add.put("shmem_timestamp",  new_timestamp);
 			super.put(fieldname,  to_add);
 			
 			m_sorted_keys.InsertFront(s_list_nodes[node_index]);
 		}
 		else {	// There already exists a list node and timestamp. 
 			ObjectNode wrapper_value = (ObjectNode)super.get(fieldname);
 			wrapper_value.put("value",  v);
 			ArrayNode old_timestamp = (ArrayNode)wrapper_value.get("shmem_timestamp");
 			VectorTimestamp.CopyFromTo(m_now, old_timestamp);
 			
 			m_sorted_keys.Remove(s_list_nodes[node_index]);
 			m_sorted_keys.InsertFront(s_list_nodes[node_index]);
 		}
 		
 		fixTime(this, m_now);
 	}
 	
 	@Override
 	public void put(String fieldname, int v) {
 		int node_index;
 		try {
 			node_index = m_key_map.get(fieldname);
 		}
 		catch (Exception e) {
 			node_index = 0;
 		}
 		if (node_index == 0) {
 			
 			// Allocate a list node for the new key. 
 			node_index = s_first_free;
 			s_first_free += 1;
 			ArrayNode new_timestamp = VectorTimestamp.Copy(m_now);
 			s_list_nodes[node_index] = new ListNode(fieldname, new_timestamp);
 			m_key_map.put(fieldname,  node_index);
 			
 			ObjectNode to_add = ShMem.mapper.createObjectNode();
 			to_add.put("value",  v);
 			to_add.put("shmem_timestamp",  new_timestamp);
 			super.put(fieldname,  to_add);
 			
 			m_sorted_keys.InsertFront(s_list_nodes[node_index]);
 		}
 		else {	// There already exists a list node and timestamp. 
 			ObjectNode wrapper_value = (ObjectNode)super.get(fieldname);
 			wrapper_value.put("value",  v);
 			ArrayNode old_timestamp = (ArrayNode)wrapper_value.get("shmem_timestamp");
 			VectorTimestamp.CopyFromTo(m_now, old_timestamp);
 			
 			m_sorted_keys.Remove(s_list_nodes[node_index]);
 			m_sorted_keys.InsertFront(s_list_nodes[node_index]);
 		}
 		
 		fixTime(this, m_now);
 	}
 	
 	@Override
 	public JsonNode put(String fieldname, JsonNode v) {
 		int node_index;
 		try {
 			node_index = m_key_map.get(fieldname);
 		}
 		catch (Exception e) {
 			node_index = 0;
 		}
 		if (node_index == 0) {
 			
 			// Allocate a list node for the new key. 
 			node_index = s_first_free;
 			s_first_free += 1;
 			ArrayNode new_timestamp = VectorTimestamp.Copy(m_now);
 			s_list_nodes[node_index] = new ListNode(fieldname, new_timestamp);
 			m_key_map.put(fieldname,  node_index);
 			
 			ObjectNode to_add = ShMem.mapper.createObjectNode();
 			to_add.put("value",  v);
 			to_add.put("shmem_timestamp",  new_timestamp);
 			super.put(fieldname,  to_add);
 			
 			m_sorted_keys.InsertFront(s_list_nodes[node_index]);
 		}
 		else {	// There already exists a list node and timestamp. 
 			ObjectNode wrapper_value = (ObjectNode)super.get(fieldname);
 			wrapper_value.put("value",  v);
 			ArrayNode old_timestamp = (ArrayNode)wrapper_value.get("shmem_timestamp");
 			VectorTimestamp.CopyFromTo(m_now, old_timestamp);
 			
 			m_sorted_keys.Remove(s_list_nodes[node_index]);
 			m_sorted_keys.InsertFront(s_list_nodes[node_index]);
 		}
 		
 		fixTime(this, m_now);
 		return null;
 	}
 	
 	@Override
 	public void put(String fieldname, long v) {
 		int node_index;
 		try {
 			node_index = m_key_map.get(fieldname);
 		}
 		catch (Exception e) {
 			node_index = 0;
 		}
 		if (node_index == 0) {
 			
 			// Allocate a list node for the new key. 
 			node_index = s_first_free;
 			s_first_free += 1;
 			ArrayNode new_timestamp = VectorTimestamp.Copy(m_now);
 			s_list_nodes[node_index] = new ListNode(fieldname, new_timestamp);
 			m_key_map.put(fieldname,  node_index);
 			
 			ObjectNode to_add = ShMem.mapper.createObjectNode();
 			to_add.put("value",  v);
 			to_add.put("shmem_timestamp",  new_timestamp);
 			super.put(fieldname,  to_add);
 			
 			m_sorted_keys.InsertFront(s_list_nodes[node_index]);
 		}
 		else {	// There already exists a list node and timestamp. 
 			ObjectNode wrapper_value = (ObjectNode)super.get(fieldname);
 			wrapper_value.put("value",  v);
 			ArrayNode old_timestamp = (ArrayNode)wrapper_value.get("shmem_timestamp");
 			VectorTimestamp.CopyFromTo(m_now, old_timestamp);
 			
 			m_sorted_keys.Remove(s_list_nodes[node_index]);
 			m_sorted_keys.InsertFront(s_list_nodes[node_index]);
 		}
 		
 		fixTime(this, m_now);
 	}
 	
 	@Override
 	public void put(String fieldname, String v) {
 		int node_index;
 		try {
 			node_index = m_key_map.get(fieldname);
 		}
 		catch (Exception e) {
 			node_index = 0;
 		}
 		if (node_index == 0) {
 			
 			// Allocate a list node for the new key. 
 			node_index = s_first_free;
 			s_first_free += 1;
 			ArrayNode new_timestamp = VectorTimestamp.Copy(m_now);
 			s_list_nodes[node_index] = new ListNode(fieldname, new_timestamp);
 			m_key_map.put(fieldname,  node_index);
 			
 			ObjectNode to_add = ShMem.mapper.createObjectNode();
 			to_add.put("value",  v);
 			to_add.put("shmem_timestamp",  new_timestamp);
 			super.put(fieldname,  to_add);
 			
 			m_sorted_keys.InsertFront(s_list_nodes[node_index]);
 		}
 		else {	// There already exists a list node and timestamp. 
 			ObjectNode wrapper_value = (ObjectNode)super.get(fieldname);
 			wrapper_value.put("value",  v);
 			ArrayNode old_timestamp = (ArrayNode)wrapper_value.get("shmem_timestamp");
 			VectorTimestamp.CopyFromTo(m_now, old_timestamp);
 			
 			m_sorted_keys.Remove(s_list_nodes[node_index]);
 			m_sorted_keys.InsertFront(s_list_nodes[node_index]);
 		}
 		
 		fixTime(this, m_now);
 	}
 	
 	public void put(String fieldname, ShMemObject v) {
 		
 		int node_index;
 		try {
 			node_index = m_key_map.get(fieldname);
 		}
 		catch (Exception e) {
 			node_index = 0;
 		}
 		if (node_index == 0) {
 			
 			// Allocate a list node for the new key. 
 			node_index = s_first_free;
 			s_first_free += 1;
 			ArrayNode new_timestamp = VectorTimestamp.Copy(m_now);
 			s_list_nodes[node_index] = new ListNode(fieldname, new_timestamp);
 			m_key_map.put(fieldname,  node_index);
 			
 			ObjectNode to_add = ShMem.mapper.createObjectNode();
 			to_add.put("value",  v);
 			to_add.put("shmem_timestamp",  new_timestamp);
 			super.put(fieldname,  to_add);
 			
 			m_sorted_keys.InsertFront(s_list_nodes[node_index]);
 		}
 		else {	// There already exists a list node and timestamp. 
 			ObjectNode wrapper_value = (ObjectNode)super.get(fieldname);
 			wrapper_value.put("value",  v);
 			ArrayNode old_timestamp = (ArrayNode)wrapper_value.get("shmem_timestamp");
 			VectorTimestamp.CopyFromTo(m_now, old_timestamp);
 			
 			m_sorted_keys.Remove(s_list_nodes[node_index]);
 			m_sorted_keys.InsertFront(s_list_nodes[node_index]);
 		}
 		
 		v.parent = this;
 		v.parent_key = fieldname;
 		fixTime(this, v.getTimestamp());
 	}
 	
 	public ArrayNode getTimestamp() {
 		return (ArrayNode)super.get("shmem_timestamp");
 	}
 	
 	@Override
 	public JsonNode get(String fieldname) throws NullPointerException {
 		JsonNode ret = super.get(fieldname).get("value");
 		return ret;
 	}
 	
 	@Override
 	public JsonNode remove(String fieldname) {
 		ObjectNode value = (ObjectNode)super.get(fieldname);
 		JsonNode ret = value.remove("value");
 		fixTime(this, m_now);
 		return ret;
 	}
 	
 	
 	
 	
 	/*
 	private Object insert(String key, Object value, ITimestamp ts) throws ShMemFailure {
 		
 		// We wrap the value in the "to_add" JSONObject.
 		JSONObject to_add = new JSONObject();
 		
 		// Add the timestamp to the wrapper. 
 		to_add.put("shmem_timestamp",  ts);
 		
 		// If the value is an ShMemObject class, update its parent and parent_key information.
 		// The parent is the current object (this) and the key is the 'key' argument. 
 		if (value.getClass() == ShMemObject.class) {
 			ShMemObject sh_mem_value = (ShMemObject)value;
 			sh_mem_value.parent = this;
 			sh_mem_value.parent_key = key;
 		}
 		
 		// Recursively update this ShMemObject's parents' timestamps. 
 		update_difftree(this, ts);
 		
 		// Finally, put the actual value into the wrapper and insert the wrapper into
 		// 'this'. 
 		to_add.put("value", value);
 		return super.put(key, to_add);
 	}
 	
 	private Object delete(String key, ITimestamp ts) throws ShMemFailure {
 		
 		Object ret = super.remove(key);
 		
 		// Update diff meta data
 		JSONObject to_add = new JSONObject();
 		to_add.put("shmem_timestamp",  ts);
 		update_difftree(this, ts);
 		return ret;
 	}
 	
 	*/
 	
 	
 	// 
 	// We can't just remove a key from the map because the remove is still an UPDATE. 
 	// We insert a tombstone as the leaf (null), which will be interpreted by the merge
 	// process appropriately. Merge understands when to garbage collect tombstones. 
 	//
 	// XXX: This method looks buggy!
 	
 	/*
 	public Object remove(Object key) {
 		Object ret = super.remove(key);
 		
 		// Update diff meta data
 		update_difftree(this, now_);
 		JSONObject to_add = new JSONObject();
 		to_add.put("shmem_timestamp",  now_);
 		return ret;
 	}
 	*/
 	
 	/*
 	
 	
 	private void put_force(ShMemObject other) throws ShMemFailure {
 		for (Object k : other.keySet()) {
 			Object other_value = other.get((String)k);
 			if (this.containsKey(k)) {
 				Object this_value = this.get((String)k);
 				if ((other_value.getClass() != ShMemObject.class) || 
 					(this_value.getClass() != ShMemObject.class)) {
 					ITimestamp saved = now_;
 					now_ = (ITimestamp)((JSONObject)other.get(k)).get("shmem_timestamp");
 					this.put((String)k, other_value);
 					now_ = saved;
 				}
 				else {
 					((ShMemObject)this_value).put_force((ShMemObject)other_value);
 				}
 			}
 			else {
 				ITimestamp saved = now_;
 				now_ = (ITimestamp)((JSONObject)other.get(k)).get("shmem_timestamp");
 				this.put((String)k, other_value);
 				now_ = saved;
 			}
 		}
 	}
 	*/
 	
 	
 	public void InsertAt(String fieldname, JsonNode node, ArrayNode time) {
 		super.put(fieldname,  node);
 		fixTime(this, time);
 	}
 	
 	private static ShMemObject DeserializeObjectNode(ObjectNode obj) {
 		ShMemObject ret = new ShMemObject();
 		Iterator<Map.Entry<String,JsonNode>> fields = obj.getFields();
 		
 		while (fields.hasNext()) {
 			Map.Entry<String, JsonNode> cur = fields.next();
 			String cur_key = cur.getKey();
 			JsonNode wrapped_value = cur.getValue();
 			JsonNode real_value = wrapped_value.get("value");
 			ArrayNode timestamp = (ArrayNode)wrapped_value.get("shmem_timestamp");
 			
 			if (real_value.isObject()) {
 				ShMemObject to_insert = DeserializeObjectNode((ObjectNode)real_value);
 				ret.InsertAt(cur_key, 
 							 to_insert, 
 							 (ArrayNode)wrapped_value.get("shmem_timestamp"));
 				to_insert.parent = ret;
 				to_insert.parent_key = cur_key;
 				
 			}
 			else {
 				ret.InsertAt(cur_key,  wrapped_value, timestamp);
 			}
 		}
 		return ret;
 	}
 	
 	public void merge(JsonNode release, ArrayNode orig_timestamp) {
 		Iterator<Map.Entry<String,JsonNode>> fields = release.getFields();
 		
 		while (fields.hasNext()) {
 			Map.Entry<String, JsonNode> cur = fields.next();
 			String key = cur.getKey();
 			JsonNode wrapped_value = cur.getValue();
 			ArrayNode other_timestamp = (ArrayNode)wrapped_value.get("shmem_timestamp");
 			JsonNode other_value = wrapped_value.get("value");
 			
 			JsonNode my_wrapper_value = super.get(key);
 			if (my_wrapper_value != null) {
 				
 				// We need to compare timestamps, so use ObjectNode's get. 
 				ArrayNode my_timestamp = (ArrayNode)my_wrapper_value.get("shmem_timestamp");
 				JsonNode my_value = my_wrapper_value.get("value");
 				
 				Comparison comp = VectorTimestamp.Compare(my_timestamp,  other_timestamp);
 				
 				// Both have written to the same node.
 				if (comp == Comparison.NONE) {
 					if (other_value.isObject() && my_value.isObject()) {
 						((ShMemObject)my_value).merge(other_value,  other_timestamp);
 					}
 					else {
 						// We've just detected a write-write conflict. 
 						System.out.println("Write-write conflict!");
 						System.exit(-1);
 					}
 				}
 				else if (comp == Comparison.LT) {
 					// Take the other guy's changes because we're subsumed. 
 					if (other_value.isObject()) {
 						ShMemObject deserialized_value = DeserializeObjectNode((ObjectNode)other_value);
 						ObjectNode to_put = ShMem.mapper.createObjectNode();
 						to_put.put("value", deserialized_value);
 						to_put.put("shmem_timestamp",  other_timestamp);
 						this.InsertAt(key, to_put, other_timestamp);
 						deserialized_value.parent = this;
 						deserialized_value.parent_key = key;
 					}
 					else {
 						this.InsertAt(key,  wrapped_value,  other_timestamp);
 					}
 					
 				}
 				else {
 					// Two cases: Either we are greater, in which case we can keep our changes. 
 					// or we're equal, in which case it's also safe to keep our changes. 
 					continue;
 				}
 			}
 			else {
 				if (other_value.isObject()) {
 					ShMemObject deserialized_value = DeserializeObjectNode((ObjectNode)other_value);
 					
 					ObjectNode to_put = ShMem.mapper.createObjectNode();
 					to_put.put("value",  deserialized_value);
 					to_put.put("shmem_timestamp",  other_timestamp);
 					this.InsertAt(key, to_put, other_timestamp);
 					
 					deserialized_value.parent = this;
 					deserialized_value.parent_key = key;
 				}
 				else {
 					this.InsertAt(key,  wrapped_value, other_timestamp);
 				}
 			}
 		}
 	}
 				
 	
 	//
 	// This method takes the serialized "release" deltas and merges them into the acquirer's 
 	// ShMemObject. orig_timestamp is the timestamp at which the acquirer forked the releasing
 	// process. 
 	//
 	/*
 	public void merge(JSONObject release, ITimestamp orig_timestamp) 
 		throws ShMemFailure {
 		
 		// Go through all the objects in the release set. 
 		for (Object k : release.keySet()) {
 			String key = (String)k;
 			
 			// Get the wrapper value. 
 			JSONObject release_cur = (JSONObject)release.get(k);
 			
 			// The acquirer also contains the current key. 
 			if (this.containsKey(k)) {
 				
 				// Pull the acquirer's timestamp from its wrapper. 
 				JSONObject acquire_cur = (JSONObject)this.get(k);
 				Object acquire_value = acquire_cur.get("value");
 				ITimestamp acquire_timestamp = (ITimestamp)acquire_cur.get("shmem_timestamp");
 				
 				// Check if the releasing object contains a tombstone, if it does, then
 				// the timestamps should not conflict. 
 				if (!release_cur.containsKey("value")) {
 					
 					// The timestamps conflict: the acquiring process has modified/removed 
 					// this subtree, while the releasing process has removed it altogether. 
 					if (orig_timestamp.Compare(acquire_timestamp) == Comparison.LT) {
 						throw new ShMemFailure("Merge failed!");
 					}
 					
 					// We're safe, the acquiring process hasn't touched this subtree, we can 
 					// safely remove it. The remove method will add a tombstone to the acquiring
 					// process' ShMemObject. 
 					else {
 						ITimestamp released_ts = 
 								VectorTimestamp.Deserialize((String)release_cur.get("shmem_timestamp"),
 															cur_node_);
 						this.delete(key, released_ts);
 					}
 				}
 				
 				// If we've reached this point, the value is NOT A TOMBSTONE. 
 				Object release_value = release_cur.get("value");
 				
 				// If the acquiring process hasn't modified the timestamp since the original 
 				// timestamp, we will always be safe. 
 				if (orig_timestamp.Compare(acquire_timestamp) == Comparison.GE) {
 					
 					// If the releasing process contains a leaf here, just put the leaf value
 					// into the acquiring process. 
 					if (release_value.getClass() != JSONObject.class) {
 						ITimestamp ts = (ITimestamp)VectorTimestamp.
 								Deserialize((String)release_cur.get("shmem_timestamp"), cur_node_);
 						insert(key, release_value, ts);
 					}
 					else {
 						// 	XXX: Bug. We should be adding leaves here, not modifying the keys which
 						// have not been modified by both the acuiring process and releasing process. 
 						ShMemObject mod_child = json2shmem((JSONObject)release_value);
 						if (acquire_value.getClass() == ShMemObject.class) {
 							((ShMemObject)acquire_value).put_force(mod_child);
 						}
 						else {
 							this.put(key,  mod_child);
 						}
 					}
 				}
 				
 				// One of them is a leaf and both have been modified. 
 				// This is a write-write conflict. 
 				else if (acquire_value.getClass() != ShMemObject.class ||
 						 release_value.getClass() != JSONObject.class) {
 					throw new ShMemFailure("Merge failed!");
 				}
 				
 				// They are both ShMemObjects (technically the releaser is a JSONObject,
 				// but this is just a serialized version of a ShMemObject). Recursively
 				// try to merge them. 
 				else {
 					((ShMemObject)acquire_value).merge((JSONObject)release_value, orig_timestamp);
 				}
 			}
 			
 			// The acquiring process does not contain a key here (that is, no tombstone and 
 			// no subtree). 
 			else {
 				
 				// If the acquirer does not contain anything, we're fine, we just do nothing. 
 				// This condition garbage collects tombstones automatically. 
 				if (release_cur.get("value") != null) {
 					Object release_value = release_cur.get("value");
 					ITimestamp ts = (ITimestamp)VectorTimestamp.
 							Deserialize((String)release_cur.get("shmem_timestamp"), cur_node_);
 					
 					// If the releasing process contains an ShMemObject here, first convert from JSON
 					// to ShMemObject and then we can just "put" it
 					// into the acquiring process. 
 					if (release_value.getClass() == JSONObject.class) {
 						ShMemObject mod_child = json2shmem((JSONObject)release_value);
 						insert(key, mod_child, ts);
 					}
 					else {
 						insert(key, release_value, ts);
 					}
 				}
 			}
 		}
 	}
 	*/
 }
