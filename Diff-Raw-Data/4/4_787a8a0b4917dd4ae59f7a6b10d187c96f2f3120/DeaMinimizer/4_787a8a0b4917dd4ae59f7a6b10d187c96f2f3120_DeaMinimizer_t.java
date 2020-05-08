 package de.hszg.atocc.autoedit.minimize.internal;
 
 import de.hszg.atocc.core.util.AutomatonService;
 import de.hszg.atocc.core.util.Pair;
 import de.hszg.atocc.core.util.automaton.Automaton;
 import de.hszg.atocc.core.util.automaton.InvalidStateException;
 import de.hszg.atocc.core.util.automaton.InvalidTransitionException;
 import de.hszg.atocc.core.util.automaton.Transition;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.Set;
 import java.util.SortedSet;
 
 public class DeaMinimizer {
 
     private Automaton automaton;
     private Automaton minimalAutomaton;
 
     private AutomatonService automatonService;
 
     private String[] states;
     private int[][] stateMatrix;
 
     private static final int INVALID = -1;
     private static final int MERGABLE = 0;
     private static final int UNMERGABLE = 1;
 
     public DeaMinimizer(Automaton anAutomaton, AutomatonService service) {
         automaton = anAutomaton;
         automatonService = service;
     }
 
     public Automaton minimize() throws InvalidStateException, InvalidTransitionException {
         minimalAutomaton = new Automaton(automaton);
 
         step1();
         step2();
         step3();
         step4();
 
         mergeStates();
 
         return minimalAutomaton;
     }
 
     private void step1() {
         initializeStateMatrix();
     }
 
     private void step2() {
         for (int i = 0; i < states.length; ++i) {
             for (int j = i; j < states.length; ++j) {
                 final String state1 = states[i];
                 final String state2 = states[j];
 
                 if (isUnmergable(state1, state2)) {
                     stateMatrix[i][j] = UNMERGABLE;
                 }
             }
         }
     }
 
     private int step3() {
         int numChanges = 0;
         for (int i = 0; i < states.length; ++i) {
             for (int j = i; j < states.length; ++j) {
                 if (stateMatrix[i][j] == MERGABLE) {
                     final String state1 = states[i];
                     final String state2 = states[j];
 
                     for (String character : automaton.getAlphabet()) {
                         final String target1 = automatonService
                                 .getTargetsFor(state1, character, automaton).iterator().next();
                         final String target2 = automatonService
                                 .getTargetsFor(state2, character, automaton).iterator().next();
 
                         final int index1 = getIndexOf(target1);
                         final int index2 = getIndexOf(target2);
 
                         if (stateMatrix[index1][index2] == UNMERGABLE) {
                             if (stateMatrix[i][j] != UNMERGABLE) {
                                 numChanges++;
                             }
 
                             stateMatrix[i][j] = UNMERGABLE;
                         }
                     }
                 }
             }
         }
 
         return numChanges;
     }
 
     private void step4() {
         int numChanges = 0;
         do {
             numChanges = step3();
         } while (numChanges > 0);
     }
 
     private void mergeStates() throws InvalidStateException, InvalidTransitionException {
 
         final Collection<Pair<Integer, Integer>> mergableStates = findMergableStates();
 
         for (Pair<Integer, Integer> statePair : mergableStates) {
             final int i = statePair.getFirst();
             final int j = statePair.getSecond();
 
             final Collection<Transition> influencedTransitions = getInfluencedTransitions(i, j);
 
             final boolean newStateIsFinalState = newStateIsFinalState(i, j);
 
             removeOldStates(i, j);
 
             String newStateName = generateNewStateNameFor(i, j);
             minimalAutomaton.addState(newStateName);
 
             if (newStateIsFinalState) {
                 minimalAutomaton.addFinalState(newStateName);
             }
 
             modifyTransitions(i, j, influencedTransitions, newStateName);
         }
 
     }
 
     private void initializeStateMatrix() {
         final SortedSet<String> stateSet = automaton.getSortedStates();
 
         states = stateSet.toArray(new String[stateSet.size()]);
         stateMatrix = new int[states.length][states.length];
 
         for (int i = 0; i < states.length; ++i) {
             for (int j = 0; j <= i; ++j) {
                 stateMatrix[i][j] = INVALID;
             }
         }
     }
 
     private void modifyTransitions(final int i, final int j,
             final Collection<Transition> influencedTransitions, String newStateName)
             throws InvalidTransitionException {
         for (Transition currentTransition : influencedTransitions) {
             String sourceName = currentTransition.getSource();
             String targetName = currentTransition.getTarget();
 
             if (sourceName.equals(states[i]) || sourceName.equals(states[j])) {
                 sourceName = newStateName;
             }
 
             if (targetName.equals(states[i]) || targetName.equals(states[j])) {
                 targetName = newStateName;
             }
 
             final Transition newTransition = new Transition(sourceName, targetName,
                     currentTransition.getCharacterToRead());
             minimalAutomaton.addTransition(newTransition);
         }
     }
 
     private Collection<Pair<Integer, Integer>> findMergableStates() {
         final Collection<Pair<Integer, Integer>> mergableStates = new LinkedList<>();
 
         for (int i = 0; i < states.length; ++i) {
             for (int j = i; j < states.length; ++j) {
                 if (stateMatrix[i][j] == MERGABLE) {
                     mergableStates.add(new Pair<Integer, Integer>(i, j));
                 }
             }
         }
 
         return Collections.unmodifiableCollection(mergableStates);
     }
 
     private String generateNewStateNameFor(int firstState, int secondState) {
         return states[firstState] + states[secondState];
     }
 
     private void removeOldStates(int firstState, int secondState) {
        automatonService.removeState(states[firstState], minimalAutomaton);
        automatonService.removeState(states[secondState], minimalAutomaton);
     }
 
     private Set<Transition> getInfluencedTransitions(int firstState, int secondState) {
         final Set<Transition> transitionsToFirstState = automatonService.getTransitionsTo(
                 states[firstState], automaton);
         final Set<Transition> transitionsToSecondState = automatonService.getTransitionsTo(
                 states[secondState], automaton);
 
         final Set<Transition> transitionsFromFirstState = automatonService
                 .getTransitionsFrom(states[firstState], automaton);
         final Set<Transition> transitionsFromSecondState = automatonService
                 .getTransitionsFrom(states[secondState], automaton);
 
         final Set<Transition> influencedTransitions = new HashSet<>();
         influencedTransitions.addAll(transitionsToFirstState);
         influencedTransitions.addAll(transitionsToSecondState);
         influencedTransitions.addAll(transitionsFromFirstState);
         influencedTransitions.addAll(transitionsFromSecondState);
 
         return influencedTransitions;
     }
 
     private boolean newStateIsFinalState(int firstState, int secondState) {
         return automaton.getFinalStates().contains(states[firstState])
                 && automaton.getFinalStates().contains(states[secondState]);
     }
 
     private boolean isUnmergable(String state1, String state2) {
         final Set<String> finalStates = automaton.getFinalStates();
 
         if (finalStates.contains(state1) && !finalStates.contains(state2)
                 || finalStates.contains(state2) && !finalStates.contains(state1)) {
             return true;
         }
 
         return false;
     }
 
     private int getIndexOf(String state) {
         for (int i = 0; i < states.length; ++i) {
             if (states[i].equals(state)) {
                 return i;
             }
         }
 
         return INVALID;
     }
 
 }
