 import java.io.ByteArrayOutputStream;
 import java.io.PrintStream;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Queue;
 import java.util.LinkedList;
 
 public class DFA extends NFA {
 	/* Constructors */
 	public DFA(State start) {
 		super(start);
 	}
 
 	// Convert NFA to DFA
 	public DFA(NFA nfa) {
 		super(new State());
 		
 		HashMap<StateSet, State> map = new HashMap<StateSet, State>();
 		
 		// First, add start state
 		// This is the NFA start state and all reachable states (epsilon transitions)
 		StateSet startSet = new StateSet();
 		startSet.add(nfa.getStart());
 		startSet.states.addAll(nfa.statesReachableFrom(nfa.getStart()));
 		
 		setStart(startSet.toState());
 		
 		map.put(startSet, getStart());
 		
 		Queue<StateSet> queue = new LinkedList<StateSet>();
 		queue.offer(startSet);
 		
 		while(!queue.isEmpty()) {
 			StateSet set = queue.poll();
 			
 			for (int i = 0; i < 128; i++) {
 				StateSet trans = set.transition((char)i);
 				State to = null;
 				if (!map.containsKey(trans)) {
 					to = trans.toState();
 					map.put(trans, to);
 					if (!queue.contains(trans))
 						queue.offer(trans);
 				} else {
 					to = map.get(trans);
 				}
 				
 				State from = map.get(set);
 				addState(from);
 				addState(to);
 				addTransition(from, (char)i, to);
 			}
 		}
 	}
 
 	/* Export */
 	public State[][] toTable() {
 		// Convert to some kind of table.
 		
 		// First make a linked list so we maintain order
		ArrayList<State> list = new ArrayList<State>();
 		list.addAll(getStates());
 		
 		// Move start state to front
 		list.remove(getStart());
		list.add(getStart());
 		
 		State[][] table = new State[list.size()][128+1]; // first column is from state
 		
 		int i = 0;
 		for (State s : list) {
 			table[i][0] = s;
 			i++;
 		}
 		
 		for (Transition t : getTransitions()) {
 			int index = list.indexOf(t.from);
 			table[index][t.c+1] = t.to;
 		}
 		
 		return table;
 	}
 	
 	public static boolean walkTable(String input, State[][] table) {
 		InputStream is = new InputStream(input);
 		
 		ArrayList<State> states = new ArrayList<State>();
 		for (int i = 0; i < table.length; i++) {
 			states.add(table[i][0]);
 		}
 		
 		State start = states.get(0);
 		State lastAccept = null;
 		int acceptPointer = 0;
 		
 		State currState = start;
 		
 		if(currState.getAccepts()) lastAccept = start;
 		
 		while(!is.isConsumed()) {
 			char c = is.peekToken().charValue();
 			
 			int index = states.indexOf(currState);
 			currState = table[index][c+1];
 			
 			if(currState.getAccepts()) {
 				lastAccept = start;
 				acceptPointer = is.getPointer();
 			}
 		}
 		
 		return false;
 	}
 	
 	public String tableToString(State[][] table) {
 		MultiColumnPrinter tp = new MultiColumnPrinter(table[0].length, 2, "-", MultiColumnPrinter.CENTER, false);
 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
 		tp.stream = new PrintStream(baos);
 		
 		String[] ascii = new String[129];
 		ascii[0] = "State";
 		for (int i = 0; i < 128; i++) {
 			ascii[i+1] = Helpers.niceCharToString((char)i);
 		}
 		
 		tp.addTitle(ascii);
 		
 		for (int i = 0; i < table.length; i++) {
 			String[] row = new String[table[i].length];
 			for (int j = 0; j < row.length; j++) {
 				if (table[i][j] == null) {
 					row[j] = "~~";
 					continue;
 				}
 				row[j] = table[i][j].toString();
//				if (i == 0 && j == 0) {
//					row[j] = "=>" + row[j];
//				}
 			}
 			tp.add(row);
 		}
 		
 		tp.print();
 		try {
 			return baos.toString("UTF8");
 		} catch (UnsupportedEncodingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return null;
 	}
 }
