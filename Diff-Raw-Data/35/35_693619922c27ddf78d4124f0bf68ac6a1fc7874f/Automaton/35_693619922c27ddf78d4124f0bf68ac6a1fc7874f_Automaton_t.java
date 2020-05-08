 package de.hszg.atocc.core.util.automaton;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 public final class Automaton {
 
     public static final String EPSILON = "EPSILON";
 
     private static final String NO_OUTPUT_SET_FOR_STATE = "No output set for state ";
     private static final String MEALY_OUTPUT_MAPPING_KEY = "%s,%s";
     private static final String FA_STACK_SYMBOL = "Core|FA_STACK_SYMBOL";
     private static final String FA_STACK_ALPHABET = "Core|FA_STACK_ALPHABET";
     private static final String MM_STACK_SYMBOL = "Core|MM_STACK_SYMBOL";
     private static final String MM_FINAL_STATE = "Core|MM_FINAL_STATE";
     private static final String MM_STACK_ALPHABET = "Core|MM_STACK_ALPHABET";
     private static final String MM_OUTPUT_ALPHABET = "Core|MM_OUTPUT_ALPHABET";
     private static final String MOORE_OUTPUT_MAPPING = "Core|MOORE_OUTPUT_MAPPING";
     private static final String MEALY_OUTPUT_MAPPING = "Core|MEALY_OUTPUT_MAPPING";
     private static final String TM_TAPE_ALPHABET = "Core|TM_TAPE_ALPHABET";
     private static final String TM_TAPE_SYMBOL = "Core|TM_TAPE_SYMBOL";
     private static final String TM_STACK_SYMBOL = "Core|TM_STACK_SYMBOL";
     private static final String TM_STACK_ALPHABET = "Core|TM_STACK_ALPHABET";
 
     private AutomatonType type = AutomatonType.DEA;
     private Alphabet alphabet = new Alphabet();
     private Alphabet stackOrTapeAlphabet = new Alphabet();
     private Alphabet outputAlphabet = new Alphabet();
     
     private Map<String, String> outputMappings = new HashMap<>();
 
     private Set<String> states = new HashSet<>();
     private Set<Transition> transitions = new HashSet<>();
 
     private String initialState = "";
     private String initialStackOrTapeSymbol = "";
     private Set<String> finalStates = new HashSet<>();
 
     private boolean containsEpsilonRules;
 
     public Automaton(AutomatonType automatonType) {
         type = automatonType;
     }
 
     public Automaton(Automaton other) throws InvalidStateException, InvalidTransitionException {
         type = other.getType();
         alphabet.setTo(other.alphabet);
         setStates(other.getStates());
 
         stackOrTapeAlphabet.setTo(other.stackOrTapeAlphabet);
 
         outputAlphabet.setTo(other.outputAlphabet);
 
         for (String k : other.outputMappings.keySet()) {
             outputMappings.put(k, other.outputMappings.get(k));
         }
 
         transitions.addAll(other.transitions);
 
         initialState = other.getInitialState();
 
         finalStates.addAll(other.getFinalStates());
 
         initialStackOrTapeSymbol = other.initialStackOrTapeSymbol;
     }
 
     public boolean containsEpsilonRules() {
         return containsEpsilonRules;
     }
 
     public AutomatonType getType() {
         return type;
     }
 
     public Alphabet getAlphabet() {
         return alphabet;
     }
 
     public Alphabet getStackAlphabet() {
         if (isFiniteAutomaton()) {
             throw new UnsupportedOperationException(FA_STACK_ALPHABET);
         }
 
         if (isMealyOrMooreAutomaton()) {
             throw new UnsupportedOperationException(MM_STACK_ALPHABET);
         }
 
         if (isTuringMachine()) {
             throw new UnsupportedOperationException(TM_STACK_ALPHABET);
         }
 
         return stackOrTapeAlphabet;
     }
 
     public Alphabet getTapeAlphabet() {
         if (!isTuringMachine()) {
             throw new UnsupportedOperationException(TM_TAPE_ALPHABET);
         }
 
         return stackOrTapeAlphabet;
     }
 
     public void addOutputAlphabetItem(String alphabetCharacter)
             throws InvalidAlphabetCharacterException {
         if (alphabetCharacter == null || "".equals(alphabetCharacter)) {
             throw new InvalidAlphabetCharacterException(
                     "null or empty string not allowed as output-alphabet item");
         }
 
         outputAlphabet.add(alphabetCharacter);
     }
 
     public Alphabet getOutputAlphabet() {
         if (!isMealyOrMooreAutomaton()) {
             throw new UnsupportedOperationException(MM_OUTPUT_ALPHABET);
         }
 
         return outputAlphabet;
     }
 
     public void addOutputMapping(String state, String output) throws InvalidStateException,
             InvalidAlphabetCharacterException {
         if (type != AutomatonType.MOORE) {
             throw new UnsupportedOperationException(MOORE_OUTPUT_MAPPING);
         }
 
         verifyStateExists(state);
         verifyOutputAlphabetCharacterExists(output);
 
         outputMappings.put(state, output);
     }
 
     public void addOutputMapping(String state, String read, String output)
             throws InvalidOutputMapping {
         if (type != AutomatonType.MEALY) {
             throw new UnsupportedOperationException(MEALY_OUTPUT_MAPPING);
         }
 
         try {
             verifyStateExists(state);
             verifyOutputAlphabetCharacterExists(output);
             verifyTransitionExists(state, read);
 
             outputMappings.put(String.format(MEALY_OUTPUT_MAPPING_KEY, state, read), output);
         } catch (Exception e) {
             throw new InvalidOutputMapping(e);
         }
 
     }
 
     public String getOutputFor(String state) {
         if (type != AutomatonType.MOORE) {
             throw new UnsupportedOperationException(MOORE_OUTPUT_MAPPING);
         }
 
         if (!outputMappings.containsKey(state)) {
             throw new RuntimeException(NO_OUTPUT_SET_FOR_STATE + state);
         }
 
         return outputMappings.get(state);
     }
 
     public String getOutputFor(String state, String read) {
         if (type != AutomatonType.MEALY) {
             throw new UnsupportedOperationException(MEALY_OUTPUT_MAPPING);
         }
 
         final String mappingKey = String.format(MEALY_OUTPUT_MAPPING_KEY, state, read);
 
         if (!outputMappings.containsKey(mappingKey)) {
             throw new RuntimeException(NO_OUTPUT_SET_FOR_STATE + state + " with input " + read);
         }
 
         return outputMappings.get(mappingKey);
     }
 
     public Set<String> getStates() {
         return Collections.unmodifiableSet(states);
     }
 
     public SortedSet<String> getSortedStates() {
         return new TreeSet<String>(states);
     }
 
     public void addState(String state) throws InvalidStateException {
         if (state == null || "".equals(state)) {
             throw new InvalidStateException("null or empty string");
         }
 
         states.add(state);
     }
 
     public void setStates(Set<String> stateNames) throws InvalidStateException {
         states.clear();
 
         for (String state : stateNames) {
             addState(state);
         }
     }
 
     public void removeState(String state) {
         states.remove(state);
     }
 
     public void removeFromFinalStates(String state) {
         finalStates.remove(state);
     }
 
     public Set<Transition> getTransitions() {
         return Collections.unmodifiableSet(transitions);
     }
 
     public void setTransitions(Set<Transition> transitionSet) throws InvalidTransitionException {
         for (Transition transition : transitionSet) {
             addTransition(transition);
         }
     }
 
     public void addTransition(Transition transition) throws InvalidTransitionException {
         try {
             verifyStateExists(transition.getSource());
             verifyStateExists(transition.getTarget());
             verifyAlphabetCharacterExists(transition.getCharacterToRead());
 
             transitions.add(transition);
 
             if (EPSILON.equals(transition.getCharacterToRead())) {
                 containsEpsilonRules = true;
             }
         } catch (final InvalidStateException | InvalidAlphabetCharacterException e) {
             throw new InvalidTransitionException(e);
         }
     }
 
     public void removeTransitions(Set<Transition> transitionsToRemove) {
         transitions.removeAll(transitionsToRemove);
     }
 
     public String getInitialState() {
         return initialState;
     }
 
     public void setInitialState(String state) throws InvalidStateException {
         initialState = state;
         addState(state);
     }
 
     public void resetInitialState() {
         initialState = "";
     }
 
     public String getInitialStackSymbol() {
         if (isFiniteAutomaton()) {
             throw new UnsupportedOperationException(FA_STACK_SYMBOL);
         }
 
         if (isMealyOrMooreAutomaton()) {
             throw new UnsupportedOperationException(MM_STACK_SYMBOL);
         }
 
         if (isTuringMachine()) {
             throw new UnsupportedOperationException(TM_STACK_SYMBOL);
         }
 
         return initialStackOrTapeSymbol;
     }
 
     public String getInitialTapeSymbol() {
         if (!isTuringMachine()) {
             throw new UnsupportedOperationException(TM_TAPE_SYMBOL);
         }
 
         return initialStackOrTapeSymbol;
     }
 
     public void setInitialStackSymbol(String symbol) throws InvalidAlphabetCharacterException {
         if (isFiniteAutomaton()) {
             throw new UnsupportedOperationException(FA_STACK_SYMBOL);
         }
 
         if (isMealyOrMooreAutomaton()) {
             throw new UnsupportedOperationException(MM_STACK_SYMBOL);
         }
 
         if (!stackOrTapeAlphabet.contains(symbol)) {
             throw new InvalidAlphabetCharacterException(
                     "Stack alphabet does not contain this symbol: " + symbol);
         }
 
         initialStackOrTapeSymbol = symbol;
     }
 
     public void setInitialTapeSymbol(String symbol) throws InvalidAlphabetCharacterException {
         if (!isTuringMachine()) {
             throw new UnsupportedOperationException(TM_TAPE_SYMBOL);
         }
 
         if (!stackOrTapeAlphabet.contains(symbol)) {
             throw new InvalidAlphabetCharacterException(
                     "Tape alphabet does not contain this symbol: " + symbol);
         }
 
         initialStackOrTapeSymbol = symbol;
     }
 
     public Set<String> getFinalStates() {
         if (isMealyOrMooreAutomaton()) {
             throw new UnsupportedOperationException(MM_FINAL_STATE);
         }
 
         return finalStates;
     }
 
     public void addFinalState(String state) throws InvalidStateException {
         if (isMealyOrMooreAutomaton()) {
             throw new UnsupportedOperationException(MM_FINAL_STATE);
         }
 
         if (!states.contains(state)) {
             throw new InvalidStateException(state);
         }
 
         finalStates.add(state);
     }
 
     @Override
     public int hashCode() {
         final int prime = 31;
         int result = 1;
 
         result = prime * result + alphabet.getCharacters().hashCode();
         result = prime * result + finalStates.hashCode();
         result = prime * result + initialState.hashCode();
         result = prime * result + states.hashCode();
         result = prime * result + transitions.hashCode();
         result = prime * result + type.hashCode();
         result = prime * result + stackOrTapeAlphabet.getCharacters().hashCode();
         result = prime * result + outputAlphabet.getCharacters().hashCode();
         result = prime * result + outputMappings.hashCode();
         result = prime * result + initialStackOrTapeSymbol.hashCode();
 
         return result;
     }
 
     // CHECKSTYLE:OFF
     @Override
     public boolean equals(Object obj) {
         if (!(obj instanceof Automaton)) {
             return false;
         }
 
         final Automaton other = (Automaton) obj;
 
         return type == other.type
                 && alphabet.getCharacters().equals(other.alphabet.getCharacters())
                 && finalStates.equals(other.finalStates)
                 && initialState.equals(other.initialState)
                 && states.equals(other.states)
                 && transitions.equals(other.transitions)
                 && outputAlphabet.getCharacters().equals(other.outputAlphabet.getCharacters())
                 && stackOrTapeAlphabet.getCharacters().equals(
                         other.stackOrTapeAlphabet.getCharacters());
     }
 
     // CHECKSTYLE:ON
 
     // @Override
     // public String toString() {
     // if (isFiniteAutomaton()) {
     // return finiteAutomatonToString();
     // } else {
     // return pushDownAutomatonToString();
     // }
     // }
     //
     // private String finiteAutomatonToString() {
     // return String.format("%s = (%s, %s, %s, %s, %s)", type.name(),
     // getSortedStates(), alphabet,
     // transitions, initialState, finalStates);
     // }
     //
     // private String pushDownAutomatonToString() {
     // return String.format("%s = (%s, %s, %s, %s, %s, %s, %s)", type.name(),
     // getSortedStates(),
     // alphabet, stackOrTapeAlphabet, transitions, initialState,
     // initialStackOrTapeSymbol,
     // finalStates);
     // }
 
     private void verifyStateExists(String state) throws InvalidStateException {
         if (!states.contains(state)) {
             throw new InvalidStateException(state);
         }
     }
 
     private void verifyAlphabetCharacterExists(String character)
             throws InvalidAlphabetCharacterException {
         if (EPSILON.equals(character) && isNondeterministicAutomaton()) {
             return;
         }
 
         if (!alphabet.contains(character)) {
             throw new InvalidAlphabetCharacterException(character);
         }
     }
 
     private void verifyOutputAlphabetCharacterExists(String character)
             throws InvalidAlphabetCharacterException {
         if (!outputAlphabet.contains(character)) {
             throw new InvalidAlphabetCharacterException(character);
         }
     }
 
     private void verifyTransitionExists(String state, String read)
             throws InvalidTransitionException {
 
         for (Transition transition : transitions) {
             if (transition.getSource().equals(state)
                     && transition.getCharacterToRead().equals(read)) {
                 return;
             }
         }
 
         throw new InvalidTransitionException(String.format(
                 "No transition from state '%s' with input character '%s'", state, read));
     }
 
     private boolean isTuringMachine() {
         return type == AutomatonType.TM;
     }
 
     private boolean isMealyOrMooreAutomaton() {
         return type == AutomatonType.MEALY || type == AutomatonType.MOORE;
     }
 
     private boolean isFiniteAutomaton() {
         return type == AutomatonType.NEA || type == AutomatonType.DEA;
     }
 
     private boolean isNondeterministicAutomaton() {
         return type == AutomatonType.NEA || type == AutomatonType.NKA;
     }
 }
