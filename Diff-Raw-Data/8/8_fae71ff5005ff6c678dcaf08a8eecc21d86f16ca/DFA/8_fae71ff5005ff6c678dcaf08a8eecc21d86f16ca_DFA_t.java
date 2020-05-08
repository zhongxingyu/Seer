 package util;
 
 import java.util.Map;
 
 public class DFA {
    private Map<State, Map<Character, State> transitionTable;
     private State currentState;
     private State startState;
    public DFA(final Map<State, Map<Character, State> transitionTable, final State startState) {
         this.transitionTable = transitionTable;
         this.currentState = startState;
         this.startState = startState;
     }
 
     /**
      * Changes the current state to the next state, based on the input
      * character.
      */
     public State doTransition(final Character input) {
        this.currentState = this.transitionTable.get(this.currentState).get(input);
         return this.currentState; 
     }
 
     public void reset() {
         this.currentState = this.startState;
     }
 
     public boolean canAccept(String inputString) {
         this.reset();
         for (Character inputChar : inputString.toCharArray()) {
             this.doTransition(inputChar);
         }
         return currentState.isAccepting();
     }
 
 }
