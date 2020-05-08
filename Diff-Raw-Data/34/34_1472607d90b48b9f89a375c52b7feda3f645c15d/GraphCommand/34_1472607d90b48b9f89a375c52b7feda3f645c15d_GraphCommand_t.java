 package engine;
 
import java.util.HashMap;
 
public class GraphCommand {
	HashMap<Integer, NodeCommand> vertices;
 	
 	public GraphCommand() {
		vertices = new HashMap<>();
 	}
 	
 	public void addVertex(NodeCommand newNode) {
		vertices.put(1, newNode);
 	}
 	
 	public void addEdge(NodeCommand srcNode, NodeCommand dstNode) {
		//srcNode.addPrev(srcNode.);
 	}
 }
