 import java.awt.Point;
 import java.util.ArrayList;
 
 import edu.cwru.sepia.environment.model.state.State.StateView;
 
 
 public class Node implements Comparable<Object>{
 	
 	private StateView state;
 	private Node parent;
 	private Act toState;
 	private ArrayList<Literal> stateLits;
 	private int costToNode;
 	private Point goal;
 	private int costToGoal;
 	private Point peasantLoc;
 
 	/**
 	 * 
 	 * @param state - The StateView of the node
 	 * @param parent - Parent node
 	 * @param toState - The Act to get to this node
 	 * @param stateLits - The list of closed world assumptions
 	 * @param costToNode - The total cost to get to this node
 	 * @param goal - The point the next goal resides at
 	 * @param costToGoal - Estimated cost to goal
 	 */
 	public Node(StateView state, Node parent, Act toState, ArrayList<Literal> stateLits, 
 			int costToNode, Point goal, int costToGoal, Point peasantLoc) {
 		this.state = state;
 		this.parent = parent;
 		this.toState = toState;
 		this.stateLits = stateLits;
 		this.costToNode = costToNode;
 		this.goal = goal;
 		this.costToGoal = costToGoal;
 //		if(peasantLoc != null) {
 //			this.peasantLoc.x = peasantLoc.x;
 //			this.peasantLoc.y = peasantLoc.y;
 //		} else {
 			this.peasantLoc = new Point((int)peasantLoc.getX(), (int)peasantLoc.getY());
 //		}
 	}
 	
 	public StateView getState() {
 		return state;
 	}
 	
 	public Node getParentNode() {
 		return parent;
 	}
 	
 	public void setParentNode(Node parent) {
 		this.parent = parent;
 	}
 	
 	public int getCostToNode() {
 		return costToNode;
 	}
 
 	public void setCostToNode(int costToNode) {
 		this.costToNode = costToNode;
 	}
 	
 	public Point getGoal() {
 		return goal;
 	}
 	
 	public void setGoal(Point goal) {
 		this.goal = goal;
 	}
 	
 	public int getCostToGoal() {
 		return costToGoal;
 	}
 	
 	public Act getToState() {
 		return toState;
 	}
 	
 	public ArrayList<Literal> getStateLits() {
 		return stateLits;
 	}
 	
 	/**
 	 * 
 	 * @param toFind - The literal you are searching for
 	 * @return True if the node contains the queried literal.
 	 */
 	public boolean containsLit(Literal toFind) {
 		String toFindClass = toFind.getClass().toString();
 		for(Literal stateLit : stateLits) {
 			String classType = stateLit.getClass().toString();
 			if(classType.equals(toFindClass)) {
				if(stateLit.equals(toFind)){ //needs to check if objects are storing the same info (not just the same class)
					return true;
				}
 			}
 		}
 		return false;
 	}
 	
 	@Override
 	public int compareTo(Object o) {
 		Node n = (Node)o;
 		if(this.costToNode + this.costToNode < n.costToNode + n.costToNode) {
 			return -1;
 		} else if(this.costToNode + this.costToNode == n.costToNode + n.costToNode) {
 			return 0;
 		} else {
 			return 1;
 		}
 	}
 
 	public Point getPeasantLoc() {
 		return peasantLoc;
 	}
 
 	public void setPeasantLoc(Point peasantLoc) {
 		this.peasantLoc = peasantLoc;
 	}
 
 }
