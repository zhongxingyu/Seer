 package suite.lcs;
 
 import java.util.ArrayDeque;
 import java.util.ArrayList;
 import java.util.Deque;
 import java.util.List;
 import java.util.Objects;
 
 import suite.search.Search;
 import suite.search.Search.Game;
import suite.util.Util;
 
 /**
  * Longest common subsequence using breadth-first search.
  * 
  * @author ywsing
  */
 public class LcsBfs<T> {
 
 	private class Node {
 		private Node previous;
 		private int pos0;
 		private int pos1;
 
 		private Node(Node previous, int pos0, int pos1) {
 			this.previous = previous;
 			this.pos0 = pos0;
 			this.pos1 = pos1;
 		}
 
 		public int hashCode() {
 			int result = 1;
 			result = 31 * result + pos0;
 			result = 31 * result + pos1;
 			return result;
 		}
 
 		public boolean equals(Object object) {
			if (Util.clazz(object) == Node.class) {
 				LcsBfs<?>.Node node = (LcsBfs<?>.Node) object;
 				return pos0 == node.pos0 && pos1 == node.pos1;
 			} else
 				return false;
 		}
 	}
 
 	public List<T> lcs(final List<T> l0, final List<T> l1) {
 		final int size0 = l0.size();
 		final int size1 = l1.size();
 
 		Node node = Search.breadthFirst(new Game<Node>() {
 			public List<Node> generate(Node node) {
 				List<Node> nodes = new ArrayList<>();
 				if (node.pos0 < size0)
 					nodes.add(jump(new Node(node, node.pos0 + 1, node.pos1)));
 				if (node.pos1 < size1)
 					nodes.add(jump(new Node(node, node.pos0, node.pos1 + 1)));
 				return nodes;
 			}
 
 			public boolean isDone(Node node) {
 				return node.pos0 == size0 && node.pos1 == size1;
 			}
 
 			private Node jump(Node node) {
 				while (node.pos0 < size0 //
 						&& node.pos1 < size1 //
 						&& Objects.equals(l0.get(node.pos0), l1.get(node.pos1))) {
 					node.pos0++;
 					node.pos1++;
 				}
 
 				return node;
 			}
 		}, new Node(null, 0, 0));
 
 		Deque<T> deque = new ArrayDeque<>();
 		Node previous;
 
 		while ((previous = node.previous) != null) {
 			int pos0 = node.pos0;
 			int pos1 = node.pos1;
 
 			while (pos0 > previous.pos0 && pos1 > previous.pos1) {
 				pos0--;
 				pos1--;
 				deque.addFirst(l0.get(pos0));
 			}
 
 			node = previous;
 		}
 
 		return new ArrayList<>(deque);
 	}
 
 }
