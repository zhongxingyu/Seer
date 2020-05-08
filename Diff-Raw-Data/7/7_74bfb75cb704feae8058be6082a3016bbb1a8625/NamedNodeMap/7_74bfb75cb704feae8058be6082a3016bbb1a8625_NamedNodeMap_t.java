 package html2windows.dom;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import org.w3c.dom.DOMException;
 
 public class NamedNodeMap extends HashMap<String, Node> {
 
 	/*
 	 * 目的:取得名稱為name的Node
 	 * 
 	 * 參數: name => 要取得的Node名稱
 	 */
 	public Node getNamedItem(String name) {
 		return get(name);
 	}
 
 	/*
 	 * 目的:將傳入的Node加入到Map中
 	 * 
 	 * 參數:arg => 要加入的Node
 	 */
 
 	public Node setNamedItem(Node arg) throws DOMException {
 		Node returnNode = null;
 		String nodeName = arg.nodeName();
 		if (containsKey(nodeName))
 			returnNode = get(nodeName);
 		put(nodeName, arg);
 
 		return returnNode;
 	}
 
 	/*
 	 * 目的:Removes a node specified by name
 	 * 
 	 * 參數:name => 要刪除的Node Name
 	 */
 	public Node removeNamedItem(String name) throws DOMException {
 		if (containsKey(name) == false) {
 			throw new DOMException(DOMException.NOT_FOUND_ERR,
 					"There is no node named name in this map.");
 		} else {
 			Node returnNode = get(name);
 			remove(name);
 			return returnNode;
 		}
 	}
 
 	public Node item(long index) {
		Collection<Node> collection = values();
		Iterator<Node> iterator = collection.iterator();
 		int i = 0;
 		Node returnNode = null;
 		while (iterator.hasNext()) {
 			if (i == (int) index)
				returnNode = iterator.next();
 			else
 				iterator.next();
 		}
 		return returnNode;
 	}
 
 	// 目的:回傳此Map的Size
 	public long length() {
 		return size();
 	}
 }
