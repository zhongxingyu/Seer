 package engine;
 
import java.util.ArrayList;
 
public abstract class GraphCommand {
	ArrayList<NodeCommand> vertices;
 	
 	public GraphCommand() {
		vertices = new ArrayList<>();
 	}
 	
 	public void addVertex(NodeCommand newNode) {
		vertices.add(newNode);
 	}
 	
 	public void addEdge(NodeCommand srcNode, NodeCommand dstNode) {
		
 	}
 }
