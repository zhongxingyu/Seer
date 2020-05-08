 package machine.turing;
 import java.util.ArrayList;
 
 /** This class represents an edge with it'ss start and end state and it's transitions
  * 
  * @author David Wille
  * 
  */
 public class Edge {
 
 	protected State from;
 	protected State to;
 	protected ArrayList<Transition> transitions;
 	
 	/**
 	 * Constructs an edge from a state to another with it's transitions
 	 * @param from Start state
 	 * @param to End state
	 * @param transitions Transitions for this edge
 	 */
 	public Edge(State from, State to, ArrayList<Transition> transitions) {
 		this.from = from;
 		this.to = to;
 		this.transitions = transitions;
 	}
 	
 	/**
 	 * Returns the edge's start state
 	 * @return Start state for this edge
 	 */
 	public State getFrom() {
 		return this.from;
 	}
 	
 	/**
 	 * Returns the edge's end state
 	 * @return End state for this edge
 	 */
 	public State getTo() {
 		return this.to;
 	}
 	
 	/**
 	 * Returns the transitions for this edge
 	 * @return Transitions for this edge
 	 */
 	public ArrayList<Transition> getTransitions() {
 		return this.transitions;
 	}
 	
 	/**
 	 * Sets the start state
 	 * @param from Start state
 	 */
 	public void setFrom(State from) {
 		this.from = from;
 	}
 	
 	/**
 	 * Sets the end state
 	 * @param to End state
 	 */
 	public void setTo(State to) {
 		this.to = to;
 	}
 	
 	/**
 	 * Sets the transitions for this edge
	 * @param transitions Transitions for this edge
 	 */ //FIXME: is this method necessary?
 	public void setTransitions(ArrayList<Transition> transitions) {
 		this.transitions = transitions;
 	}
 
 	/**
 	 * Gives a string representation of the edge
 	 */
 	@Override
 	public String toString() {
 		return "Edge [from=" + from + ", to=" + to + ", transition="
 				+ transitions + "]\n";
 	}
 	
 	
 
 }
