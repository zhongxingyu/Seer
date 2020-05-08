 import java.io.IOException;
 import java.util.Hashtable;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Stack;
 
 public class NDFAtoDFATransformer {
 
 	public Hashtable<String, Action> ndfa;
 	public Hashtable<String, Action> dfa;
 	
 	public static State moveDFA(State state, String input){
 		for (Action a : state.actions)
 			if(a.symbol.equals("input")) return a.toState;
 		return null;
 	}
 	
 	public static Hashtable<String,State> moveNFA(DFAState state, String input){
 		//HashSet<State> states = state.states;
 		Hashtable <String,State> states = state.states;
 		Hashtable <String,State> result = new Hashtable<String,State>();
 		Iterator <State> it = states.values().iterator();
 		while(it.hasNext()){
 			State s = it.next();
 			for (Action a : s.actions){
 				if (a.symbol.equals(input)) result.put(a.toState.id, a.toState);
 			}
 		}
 		return result;
 	}
 	
 	public static Hashtable<String, State> epsilonClosure(Hashtable<String, State> states){
 		Stack<State> remainingStates = new Stack<State>();
 		Hashtable <String, State> result = new Hashtable <String, State>();
 		Iterator <State> it = states.values().iterator();
 		while (it.hasNext()){
 			State s = it.next();
 			remainingStates.add(s);
 		}
 
 		
 		while (!remainingStates.isEmpty()){
 			State s = remainingStates.pop();
 			ArrayList<Action> actions = s.actions;
 			for (Action action : actions){
 				if (action.symbol.equals("epsilon")){
 					State actionState = action.toState;
 					if (!result.contains(actionState)){
 						result.put(actionState.id, actionState);
 						remainingStates.push(actionState);
 					}
 				}
 			}
 		}
 		
 		return result;
 	}
 	
 	public static HashSet<State> _epsilonClosure(HashSet <State> states){
 		Stack<State> remainingStates = new Stack<State>();
 		HashSet<State> result = new HashSet<State>();
 		// Push all states to the stack
 		for (State s : states){
 			remainingStates.add(s);
 		}
 		
 		while (!remainingStates.isEmpty()){
 			State s = remainingStates.pop();
 			ArrayList<Action> actions = s.actions;
 			for (Action action : actions){
 				if (action.symbol.equals("epsilon")){
 					State actionState = action.toState;
 					if (!result.contains(actionState)){
 						result.add(actionState);
 						remainingStates.push(actionState);
 					}
 				}
 			}
 		}
 		
 		return result;
 	}
 	
 	public static Hashtable<String,State> acceptingStates(Hashtable<String, State> states){
 		Hashtable<String,State> acceptingStates = new Hashtable<String,State>();
 		Iterator<State> it = states.values().iterator();
 		while(it.hasNext()){
 			State s = it.next();
 			if(s.type == State.StateType.ACCEPTING ||s.type == State.StateType.STARTACCEPTING ){
 				acceptingStates.put(s.id, s);
 			}
 		}
 
 		return acceptingStates;
 	}
 	
 	public static Hashtable<String,State> startingStates(Hashtable <String,State> states){
 		Hashtable<String,State> startingStates = new Hashtable<String,State>();
 		Iterator<State> it = states.values().iterator();
 		while(it.hasNext()){
 			State s = it.next();
 			if(s.type == State.StateType.START ||s.type == State.StateType.STARTACCEPTING ){
 				startingStates.put(s.id, s);
 			}
 		}
 		return startingStates;
 	}
 	
 	public static DFAState nextUnmarkedState(Hashtable<String,DFAState> states){
 		Iterator<DFAState> it = states.values().iterator();
 		while(it.hasNext()){
 			DFAState s = it.next();
 			if (!s.marked) return s;
 		}
 		return null;
 	}
 	
 	// Change this to use dictionaries instead of sets
	public static Hashtable<String,DFAState> toDFA(String inputFile) throws IOException{
 		Hashtable<String, State> nfaStates = FAReader.parseAutomata(inputFile);
 		Hashtable<String,DFAState> dfaStates = new Hashtable<String,DFAState>();
 		
 		Hashtable <String, State> startingStates = acceptingStates(nfaStates);
 		DFAState startingState = new DFAState();
 		startingState.states = epsilonClosure(startingStates);
 		dfaStates.put(startingState.name(), startingState);
 		
 		DFAState nextUnmarkedState = nextUnmarkedState(dfaStates);
 		
 		while (nextUnmarkedState != null){
 			nextUnmarkedState.marked = true;
 			ArrayList <Action> actions = nextUnmarkedState.actions();
 			for (Action a : actions){
 				Hashtable <String,State> epsStates = epsilonClosure(moveNFA(nextUnmarkedState, a.symbol));
 				DFAState S = new DFAState();
 				S.states = epsStates;
 				if (!dfaStates.contains(S)){
 					//Add S to SDFA (as an unmarked state)
 					S.marked = false;
 					dfaStates.put(S.name(), S);
 				}
 				Action action = new Action(a.symbol, S);
 				nextUnmarkedState.actions.add(action);
 			}
 				 //Set MoveDFA(T,a) to S T=nextUnmarkedState
 			nextUnmarkedState = nextUnmarkedState(dfaStates);
 		}
 		
		return dfaStates;
 	}
 	
 }
