 package engine;
 
 import java.util.ArrayList;
 
 import types.BasicCommand;
 
 public class NodeExecuter extends NodeCommand{
 	public NodeExecuter(BasicCommand command) {
 		super(command);
 		// TODO Auto-generated constructor stub
 	}
 
 	ArrayList<NodeCommand> outEdges;

	public void addNext(NodeCommand curr) {
		outEdges.add(curr);
	}
 }
