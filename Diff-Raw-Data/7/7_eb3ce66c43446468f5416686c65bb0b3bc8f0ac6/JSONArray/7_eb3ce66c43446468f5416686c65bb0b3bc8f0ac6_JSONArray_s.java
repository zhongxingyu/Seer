 package figglewatts.jarson;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import figglewatts.jarson.exceptions.JSONAccessException;
 
 public class JSONArray extends JSONNode {
 	private List<JSONNode> nodeList = new ArrayList<JSONNode>();
 	
 	@Override
 	public void setNode(JSONNode node, int index) throws JSONAccessException {
 		if (index < 0) {
			throw new JSONAccessException("Tried to access a JSONNode that does not exist!");
 		} else if (index >= nodeList.size()) {
 			nodeList.add(node);
 		} else {
 			nodeList.set(index, node);
 		}
 	}
 	@Override
 	public void setNode(JSONNode node, String key) {
 		nodeList.add(new JSONNode());
 	}
 	
 	@Override
 	public JSONNode getNode(int index) throws JSONAccessException {
 		if (index < 0 || index >= nodeList.size()) {
			throw new JSONAccessException("Tried to access a JSONNode that does not exist!");
 		} else {
 			return nodeList.get(index);
 		}
 	}
 	
 	public int size() {
 		return nodeList.size();
 	}
 	
 	@Override
 	public void Add(String key, JSONNode item) {
 		nodeList.add(item);
 	}
 	
 	@Override
 	public JSONNode Remove(int index) {
 		if (index < 0 || index >= nodeList.size()) {
 			return null;
 		} else {
 			JSONNode temp = nodeList.get(index);
 			nodeList.remove(index);
 			return temp;
 		}
 	}
 	@Override
 	public JSONNode Remove(JSONNode node) {
 		nodeList.remove(node);
 		return node;
 	}
 	
 	@Override
 	public String toString() {
 		String result = "[ ";
 		for (JSONNode node : nodeList) {
 			if (result.length() > 2) {
 				result += ", ";
 			}
 			result += node.toString();
 		}
 		result += " ]";
 		return result;
 	}
 	@Override
 	public String toString(String prefix) {
 		String result = "[ ";
 		for (JSONNode node : nodeList) {
 			if (result.length() > 3) {
 				result += ", ";
 			}
 			result += "\n" + prefix + "   ";
 			result += node.toString(prefix+"   ");
 		}
 		result += "\n" + prefix + "]";
 		return result;
 	}
 	
 	// TODO: add Serialize() method
 }
