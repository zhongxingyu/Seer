 package tools;
 
 import com.google.gson.JsonArray;
 import com.google.gson.JsonObject;
 
 /**
  * 
  * @author bm
  * 
  */
 public class JsonTree {
 
 	/**
 	 * Returns JSON tree
 	 * 
 	 * @param root
 	 *            root node
 	 * @return root node as JSON object
 	 */
 	public static String build(Treeable root) {
 		JsonObject jsonTree = buildSubTree(root);
 		return jsonTree.toString();
 	}
 
 	/**
 	 * Recursively build JSON tree
 	 * 
 	 * @param root
 	 *            root node
 	 * @return root node as JSON object
 	 */
 	private static JsonObject buildSubTree(Treeable root) {
 		JsonObject o = new JsonObject();
 
 		JsonObject data = new JsonObject();
 		data.addProperty("title", root.name());
 
 		JsonObject attr = new JsonObject();
		// Add class name to ID such that uniqueness in DOM is guaranteed
		// if multiple tree views are used.
		attr.addProperty("id", root.id() + "-"
				+ root.getClass().getSimpleName().toLowerCase());
 		attr.addProperty("name", root.name());
 
 		o.add("attr", attr);
 
 		JsonArray children = new JsonArray();
 
 		for (Treeable a : root.childNodes()) {
 			children.add(buildSubTree(a));
 		}
 
 		o.add("children", children);
 		o.add("data", data);
 
 		return o;
 	}
 }
