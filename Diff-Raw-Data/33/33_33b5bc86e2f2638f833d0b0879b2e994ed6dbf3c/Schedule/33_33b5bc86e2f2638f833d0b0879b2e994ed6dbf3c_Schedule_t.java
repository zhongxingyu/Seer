 import java.lang.*;
 import java.util.*;
 import java.io.*;
import java.net.*;
 
 /* Contains single copy of questions to be asked */
 
 public class Schedule {
 	private Vector nodes = new Vector();
 	
	public Schedule(String src) {
 		try {
			URL url = new URL(src);
			InputStream is = url.openStream();
 			int count=0;
 			String fileLine;
			BufferedReader br = new BufferedReader(new InputStreamReader(is));				
 			while((fileLine = br.readLine())!= null){
 				Node node = new Node(count,fileLine);
 				++count;
 				nodes.addElement(node);
 			}
			System.out.println("Read " + count + " nodes from " + src);
 			br.close();
 		}
		catch (MalformedURLException e) {
			System.out.println("Malformed url '" + src + "':" + e.getMessage());
		}
 		catch(IOException e){
			System.out.println("An error occurred reading from '" + src + "':" + e.getMessage());
 		}
 		catch(Exception e) {
			System.out.println("Exception: " + e.getMessage());
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
