 /** This class provides a test program for InOut.java
  * 
  * @author David Wille
  * 
  */
 
 import java.lang.String;
 import java.util.ArrayList;
 
 public class Test {
 
 	public static void main(String argv[]) {
 		State n0 = new State("0", "q0", State.Type.START);
 		State n1 = new State("1", "q1", State.Type.START);
 		ArrayList<State> states = new ArrayList<State>();
 		states.add(n0);
 		ArrayList<Character> read = new ArrayList<Character>();
 		read.add('0');
 		read.add('1');
 		read.add('2');
 		ArrayList<Character> write = new ArrayList<Character>();
 		write.add('0');
 		write.add('1');
 		write.add('2');
 		ArrayList<Character> action = new ArrayList<Character>();
 		action.add('0');
 		action.add('1');
 		action.add('2');
 		Transition t0 = new Transition("0", read, write, action);
 		ArrayList<Transition> transition = new ArrayList<Transition>();
 		transition.add(t0);
 		Edge e0 = new Edge(n0, n1, transition);
 		ArrayList<Edge> edges = new ArrayList<Edge>();
 		ArrayList<ArrayList<Character>> outerInitial= new ArrayList<ArrayList<Character>>();
 		for (int i = 0; i < 4; i++) {
 			ArrayList<Character> innerInitial = new ArrayList<Character>();
 			innerInitial.add('a');
 			innerInitial.add('b');
 			innerInitial.add('c');
 			innerInitial.add('d');
 			outerInitial.add(innerInitial);
 		}
 		edges.add(e0);
 		String machineName = "test";
 		TuringMachine machine = new TuringMachine(states, edges, machineName, 4, outerInitial);
 		
		InOut.writeXMLToFile("example.xml", machine);
 		InOut.readXMLFromFile("test.xml");
 	}
 	
 }
