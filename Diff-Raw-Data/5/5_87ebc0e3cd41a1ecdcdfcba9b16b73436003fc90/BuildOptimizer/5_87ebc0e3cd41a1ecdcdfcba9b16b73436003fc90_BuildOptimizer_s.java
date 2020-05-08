 package sc2build.optimizer;
 
 import java.util.ArrayList;
 import java.util.Deque;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Queue;
 
 import sc2build.optimizer.SC2Planner.Entity;
 import sc2build.optimizer.SC2Planner.Race;
 
 public class BuildOptimizer
 {
	private static final int TIME_THRESHOLD =  60 * 5 * 100; // 5 min
 	
 	private static class Node
 	{
 		private final Entity entity;
 		
 		private int time;
 		private Node parent;
 		private List<Node> children = new LinkedList<>();
 		
 		private boolean leafNode = false; 
 		
 		public Node(Node parent, Entity entity, int time)
 		{
 			this.setParent(this.parent);
 			this.setTime(time);
 			this.entity = entity;
 		}
 		
 		public void addNode(Node node)
 		{
 			this.children.add(node);
 		}
 		
 		public List<Node> getChildren()
 		{
 			return this.children;
 		}
 
 		public Node getParent()
 		{
 			return parent;
 		}
 
 		public void setParent(Node parent)
 		{
 			this.parent = parent;
 		}
 
 		public int getTime()
 		{
 			return time;
 		}
 
 		public void setTime(int time)
 		{
 			this.time = time;
 		}
 		
 		public void setLeafNode(boolean isFailed)
 		{
 			this.leafNode = isFailed;
 		}
 		
 		public int getAccumTime()
 		{
 			return (this.parent == null ? 0 : this.parent.time) + this.time;
 		}
 		
 		public boolean isBuildDone(List<Entity> requried)
 		{
 			List<Entity> entitiesToDone = new ArrayList<>(requried);
 			Node node = this;
 			do
 			{
 				entitiesToDone.remove(node.entity);
 			}
			while ((node = this.parent) != null);
 			
 			return entitiesToDone.isEmpty();
 		}
 	}
 
 	private List<Node> curentLevelNodes = new LinkedList<>();
 	private int minTime = Integer.MAX_VALUE;
 	private Node minNode = null;
 	
 	private void fillBuild(Node node, Deque<String> build)
 	{
 		build.addFirst(node.entity.name);
 		if (node.parent != null)
 		{
 			this.fillBuild(node.parent, build);
 		}
 	}
 	
 	private void printBuild(Node node)
 	{
 		LinkedList<String> build = new LinkedList<>();
 		this.fillBuild(node, build);
 		
 		for (String item : build)
 		{
 			System.out.print(item);
 			System.out.print("-");
 		}
 	}
 	
 	private void putEntity(Node parent, Entity entity, List<Entity> requried)
 	{
 		// put to planner and get time
 		int time = entity.time;
 		
 		Node node = new Node(parent, entity, time);
 		parent.addNode(node);
 		this.calcMinTime(node);
 		if (node.isBuildDone(requried) || 
 				node.getAccumTime() > TIME_THRESHOLD)
 		{
 			this.calcMinTime(node);
 			node.leafNode = true;
 		}
 		else
 		{
 			this.curentLevelNodes.add(node);
 		}
 	}
 	
 	private void calcMinTime(Node node)
 	{
 		if (node.getAccumTime() < this.minTime)
 		{
 			this.minTime = node.getAccumTime();
 			this.minNode  = node;
 		}
 	}
 
 	public void buildRaceTree(Race race, List<Entity> requried)
 	{
 		Node root = new Node(null, null, 0);
 		this.curentLevelNodes.add(root);
 		this.buildNewLevel(race, requried);
 	}
 	
 	private void buildNewLevel(Race race, List<Entity> requried)
 	{
 		if (this.curentLevelNodes.size() == 0) return;
 		
 		List<Node> pastLevelNodes = new LinkedList<>(this.curentLevelNodes);
 		this.curentLevelNodes.clear();
 		
 		for (Node node : pastLevelNodes)
 		{
 			for (Entity entity : race.entities)
 			{
 				if (this.isAllowedToAdd(entity))
 				{
 					this.putEntity(node, entity, requried);
 				}
 			}
 		}
 		this.buildNewLevel(race, requried);
 	}
 
 	private boolean isAllowedToAdd(Entity entity)
 	{
 		return true;
 	}
 	
 	
 }
