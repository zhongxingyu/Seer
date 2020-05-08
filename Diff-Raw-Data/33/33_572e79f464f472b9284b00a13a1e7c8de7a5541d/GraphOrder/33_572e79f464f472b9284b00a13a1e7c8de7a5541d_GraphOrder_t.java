 package engine;
 
 import generated.Flow;
 
 import java.util.HashSet;
 import java.util.Queue;
 import java.util.Set;
 
 public class GraphOrder {
 	
 	private GraphCommand graph;
 	private Queue<NodeCommand> queue;
 	private Set<NodeCommand> evaluated;
 
 	public GraphOrder(GraphCommand graph) {
 		this.graph = graph;
 		this.evaluated = new HashSet<>();
 		this.fillStarts();
 	}
 	
 	public GraphOrder(Flow flow) {
 		this(new GraphCommand(flow));
 	}
 
 	public NodeCommand getNext() {
 		NodeCommand current = queue.poll();
 		if(NodeExecuter.class.isInstance(current))
 		{
 			this.evaluated.add(current);
 			for(NodeCommand next : ((NodeExecuter)current).outEdges)
 			{
				if(this.canRun(next))
 				{
 					this.queue.add(next);
 				}
 			}
 		}
 		else if(NodePredicate.class.isInstance(current))
 		{
 			NodePredicate currentPredicate = ((NodePredicate)current);
 			if(currentPredicate.getAnswer())
 			{
 				this.queue.add(currentPredicate.getTrueCommand());
 			}
 			else
 			{
 				this.queue.add(currentPredicate.getFalseCommand());
 			}
 		}
 		
 		return current;
 	}
 	
	private boolean isRoot(NodeCommand command)
	{
		return (command.prevEdges.isEmpty());
	}
	
	private boolean canRun(NodeCommand command)
	{
		if(command.executed)
		{
			return true;
		}
		else if(isRoot(command))
		{
			return false;
		}
		else
		{
			for(NodeCommand prev : command.prevEdges)
			{
				if(!canRun(prev))
					return false;
			}
			
			return true;
		}
	}
	
 	private void fillStarts() {
 		for (NodeCommand cmd : graph.vertices.values()) {
			if(isRoot(cmd)) {
 				this.queue.add(cmd);
 			}
 		}
 	}
 }
