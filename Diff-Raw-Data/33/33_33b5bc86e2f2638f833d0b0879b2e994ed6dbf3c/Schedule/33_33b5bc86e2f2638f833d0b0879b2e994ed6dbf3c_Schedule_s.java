 import java.lang.*;
 import java.util.*;
 import java.io.*;
 
 /* Contains single copy of questions to be asked */
 
 public class Schedule {
 	private Vector nodes = new Vector();
 	
	public Schedule(String filename) {
 		try {
 			int count=0;
 			String fileLine;
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));				
 			while((fileLine = br.readLine())!= null){
 				Node node = new Node(count,fileLine);
 				++count;
 				nodes.addElement(node);
 			}
			System.out.println("Read " + count + " nodes from " + filename);
 			br.close();
 		}
 		catch(IOException e){
			System.out.println("An error occurred reading the file" + e);
 		}
 		catch(Exception e) {
			System.out.println("Exception: " + e);
 		}
 	}
 	public Node getNode(int index) {
 		if (index < 0) {
 			System.out.println("Node[" + index + "] does't exist");
 			return null;
 		}
 		if (index > nodes.size()) {
 			System.out.println("Node[" + index + "/" + nodes.size() + "] doesn't exist");
 			return null;
 		}
 		return (Node) nodes.elementAt(index);
 	}
 	public int size() {
 		return nodes.size();
 	}
 }
