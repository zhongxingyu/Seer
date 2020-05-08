 import java.util.*;
 
 public class BFS
 {
	// given the an array of nodes, will traverse the entire tree in a breadth first search,
	// starting from the node at graph[0]
 	public void BFS(ArrayList<Node> graph)
 	{
 		ArrayList<Node> queue = new ArrayList<Node>();
 
		queue.add(graph[0]);
		queue[0].visited = true; // so that we don't visit it again
 		while(!queue.isEmpty())
 		{
 			Node cur_node = queue.remove(0);
			cur_node.visited = true;
			for(int index = 0; index < cur_node.children.size(); index++)
 			{
				if(!cur_node.children[index].visited)
				{
					queue.add(cur_node.children[index];
					cur_node.children[index].visited = true;
				}
 			}
 		}
 	}
 
	class Node
 	{
 		public Node(ArrayList<Node> childs)
 		{
			ArrayList<Node> children = new Arraylist<Node>(childs);
			boolean visited = false;
 		}
 	}
 }
