 package nfa;
 
 import java.util.regex.Pattern;
 
 /**
  * NFA transition.
  * 
  * @author toriscope
  * 
  */
 public class Transition {
 	private final String regex;
 	private final State state;
 	private final boolean isEmpty;
 
 	/**
 	 * Create a transition to state with character.
 	 * 
 	 * @param regex
 	 * @param state
 	 */
 	public Transition(final String regex, final State state) {
 		this.regex = regex;
 		this.state = state;
 		this.isEmpty = false;
 	}
 
 	/**
 	 * Factory method for non-empty transition
 	 * 
 	 * @param regex
 	 * @param state
 	 * @return
 	 */
 	public static Transition createTransition(final String regex, final State state) {
 		return new Transition(regex, state);
 	}
 
 	/**
 	 * Factory method for non-empty state
 	 * 
 	 * @param state
 	 */
 	public static Transition createEmptyTransition(final State state) {
 		return new Transition(state);
 	}
 
 	/**
 	 * Create an empty transition to a state.
 	 * 
 	 * @param state
 	 */
 	public Transition(final State state) {
 		this.regex = null;
 		this.state = state;
 		this.isEmpty = true;
 	}
 
 	/**
 	 * Determines whether the given character is accepted by this transition.
 	 * 
 	 * @param character
 	 * @return true if valid, false otherwise.
 	 */
 	public boolean isValid(final char character) {
		return regex.equals(String.valueOf(character));
 	}
 
 	public String getRegex() {
 		return regex;
 	}
 
 	public boolean isEmptyTransition() {
 		return isEmpty;
 	}
 
 	public State getDestinationState() {
 		return state;
 	}
 
 	public String toString() {
 		return (this.isEmpty ? "Empty" : regex) + "->" + this.getDestinationState().getName();
 	}
 
 	public static Transition spawnGoal() {
 		return new Transition(new State("ENDFINAL", true));
 	}
 }
