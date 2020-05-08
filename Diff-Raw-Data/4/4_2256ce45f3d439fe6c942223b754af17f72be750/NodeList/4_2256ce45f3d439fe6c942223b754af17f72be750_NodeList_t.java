 package main.java.globals;
 
 import java.net.InetSocketAddress;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Map;
 import java.util.Map.Entry;
 
 /**
  * list of nodes. at the moment it updates only once
  * TODO 
  * @author josh
  *
  */
 public class NodeList {
 	private static final ArrayList<Node> nodeList = new ArrayList<Node>();
 	
 	public static Node get(int index){
 		return nodeList.get(index);
 	}
 	public static void add(String nodeName, InetSocketAddress node){
 		nodeList.add(new Node(nodeName,node));
 	}
 	public static void addAll(ArrayList<Node> nodes){
 		nodeList.addAll(nodes);
 	}
 	public static ArrayList<Node> getAll(){
		ArrayList<Node>  newList = new ArrayList<Node> ();
		newList.addAll(nodeList);
 		return newList;
 	}
 	public static class Node{
 		private final String nodeName;
 		private final InetSocketAddress addr;
 		public Node(String nodeName, InetSocketAddress addr){
 			this.nodeName = nodeName;
 			this.addr = addr;
 		}
 		public String getNodeName() {
 			return nodeName;
 		}
 		public InetSocketAddress getAddr() {
 			return addr;
 		}
 	}
 }
