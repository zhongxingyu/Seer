 package reasoner;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import model.OutcomeSequence;
 import model.PreferenceMetaData;
 import model.WorkingPreferenceModel;
 
 import util.Constants;
 import util.OutcomeFormatter;
 import verify.ModelCheckingDelegate;
 import verify.SpecHelper;
 import verify.TraceFormatterFactory;
 
 /**
  * A Preference Reasoner for (T)CP-nets and CI-nets with support for the following reasoning tasks:
  * <ol>
  * 	<li>Consistency: Is the given preference specification consistent (induced preference graph is cycle-free)?</li>
  * 	<li>Dominance Testing: Does one outcome dominate another with respect to the given preference specification?</li>
  * 	<li>Next Preferred: Compute a sequence of outcomes in order such that an outcome that comes later in the sequence does not dominate a preceding outcome.</li>
  * </ol>
  * Note: Dominance Testing and Next Preferred reasoning tasks work only for consistent (T)CP-nets and CI-nets.
  * See "Dominance Testing via Model Checking", Santhanam et al. (AAAI 2010) for more details. 
  * @author gsanthan
  *
  */
 public class AcyclicPreferenceReasoner extends PreferenceReasoner {
 	
 	/**
 	 * Initializes the SMV file, parses the model and retrieves the variables, and makes the reasoner ready for reasoning tasks 
 	 * @param smvFile
 	 */
 	public AcyclicPreferenceReasoner(String smvFile) {
 		super(smvFile);
 	}
 	
 	/* (non-Javadoc)
 	 * @see translate.PreferenceReasoner#dominates(java.lang.String[], java.lang.String[])
 	 */
 	public boolean dominates(Set<String> morePreferredOutcome, Set<String>  lessPreferredOutcome) throws Exception {
 		
 		System.out.println("Does " + morePreferredOutcome + " dominate " + lessPreferredOutcome + "?");
 		
		//Don't need to compute anything if the outcomes are the same
		if(morePreferredOutcome.equals(lessPreferredOutcome)) {
			System.out.println("Dominance does not hold");
			return false;
		}
		
 		//Make a copy the original SMV file containing the model so that we can append specs for computing dominance 
 		PreferenceMetaData pmd = new PreferenceMetaData(smvFile);
 		pmd.setWorkingFile(smvFile+"-copy-dominance.smv");
 		
 		//Append the spec corresponding to the existence of a path from less preferred to more preferred outcome in the induced preference graph
 		List<String> appendix = new ArrayList<String>();
 		String spec = getDominanceSpec(lessPreferredOutcome,morePreferredOutcome);
 		appendix.add(spec);
 		
 		//Verify
 		ModelCheckingDelegate.verify(WorkingPreferenceModel.getPrefMetaData(), appendix, "dominates");
 		boolean dominates = ModelCheckingDelegate.findVerificationResult(WorkingPreferenceModel.getPrefMetaData());
 		
 		if(dominates) {
 			
 			//Return the proof of dominance: a path from the less preferred to the more preferred outcome
 			appendix.clear();
 			//Negate the spec used to test dominance, so that the model checker can provide the proof of dominance
 			spec = getNegatedDominanceSpec(lessPreferredOutcome,morePreferredOutcome);
 /*			
 			//Set the less preferred outcome as the initial state; 
 			//the model checker need only search for a path to the more preferred outcome  
 			String initSpec = SpecHelper.getInitOutcomeSpec(lessPreferredOutcome);
 			
 			//Append the initial state contraints and the property to be verified to the model 
 			appendix.add(initSpec);*/
 			
 			appendix.add(spec);
 			
 			//Verify 
 			ModelCheckingDelegate.verify(WorkingPreferenceModel.getPrefMetaData(), appendix, "counterToDominates");
 			//Model checker must return false, i.e., property is not verified 
 			ModelCheckingDelegate.findVerificationResult(WorkingPreferenceModel.getPrefMetaData());
 			//Counter example provided by the model checker corresponds to the proof of dominance in the induced preference graph 
 			OutcomeSequence c = TraceFormatterFactory.createTraceFormatter().parsePathFromTrace(WorkingPreferenceModel.getPrefMetaData());
 			System.out.print("Proof of dominance: ");
 			c.printOutcomeSequence();
 		} else {
 			System.out.println("Dominance does not hold");
 		}
 		return dominates;
 	}
 	
 	/* (non-Javadoc)
 	 * @see translate.PreferenceReasoner#isConsistent()
 	 */
 	public boolean isConsistent() throws Exception {
 
 		//Append the spec corresponding to the existence of a cycle in the model (corresponds to a cycle in the induced preference graph)
 		List<String> appendix = new ArrayList<String>();
 		String spec = getConsistencySpec();
 		appendix.add(spec);
 		
 		//Verify
 		ModelCheckingDelegate.verify(WorkingPreferenceModel.getPrefMetaData(), appendix, "consistency");
 		boolean consistent = ModelCheckingDelegate.findVerificationResult(WorkingPreferenceModel.getPrefMetaData());
 		
 		if(!consistent) {
 			//Parse and return the cycle 
 			OutcomeSequence c = TraceFormatterFactory.createTraceFormatter().parseCycleFromTrace(WorkingPreferenceModel.getPrefMetaData());
 
 			System.out.print("Not consistent; Cycle found: ");
 			c.printOutcomeSequence();
 			System.out.println();
 		} else {
 			System.out.println("Consistent");
 		}
 		return consistent;
 	}
 
 	/* (non-Javadoc)
 	 * @see translate.PreferenceReasoner#resetReasoner()
 	 */
 	public void resetReasoner() {
 		//Prepare for a new set of reasoning tasks; 
 		//particularly forget the previously computed outcomes at the current level and model constraints   
 		currentMaximalOutcomes = new ArrayList<String[]>();
 		invariants = new ArrayList<String>();
 	}
 	
 	/* (non-Javadoc)
 	 * @see translate.PreferenceReasoner#nextPreferred()
 	 */
 	public Set<String> nextPreferred() throws IOException {
 		
 		//Append the spec corresponding to the property that there is no (maximal) outcome 
 		//in the current (induced preference graph) model 
 		//that has no (improving flip) outgoing transition in which a preference variable is changed  
 		List<String> appendix = new ArrayList<String>();
 		String spec = getNextPreferredSpec();
 		appendix.addAll(invariants);
 		appendix.add(spec);
 		
 		if(Constants.LOG_VERIFICATION_SPECS) {
 			System.out.print("   "+" CTL: "+spec);
 		}
 		
 		//Verify
 		ModelCheckingDelegate.verify(WorkingPreferenceModel.getPrefMetaData(), appendix, "nextPreferred");
 		boolean result = ModelCheckingDelegate.findVerificationResult(WorkingPreferenceModel.getPrefMetaData());
 		
 		if(Constants.LOG_VERIFICATION_SPECS) {
 			System.out.println(result);
 		}
 		
 		Set<String> pref;
 		if(result == true) {
 			OutcomeSequence currentMaximal = new OutcomeSequence();
 			currentMaximal.addOutcomeSequenceAsArray(currentMaximalOutcomes);
 			removeOutcomes(currentMaximal);
 			currentMaximalOutcomes.clear();
 			//All maximal outcomes at the current level have been computed
 			pref = null;
 		} else {
 			//Parse the found next preferred outcome from the model checker's output file
 			String[] currentPreferred = TraceFormatterFactory.createTraceFormatter().parseCounterExampleFromTrace(WorkingPreferenceModel.getPrefMetaData());
 			
 			//Keep track of the maximal outcomes at the current level 
 			currentMaximalOutcomes.add(currentPreferred);
 			
 			//Return the found next preferred outcome at the current level
 			pref = new HashSet<String>(Arrays.asList(currentPreferred));
 		}
 		return pref;
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see translate.PreferenceReasoner#generateWeakOrder()
 	 */
 	public List<OutcomeSequence> generateWeakOrder() throws IOException {
 		//The to-be-computed weak order as a list of levels - each level has a set of outcome
 		List<OutcomeSequence> weakOrder = new ArrayList<OutcomeSequence>();
 		resetGeneratedOutcomes();
 		OutcomeSequence nextPreferredSet = null;
 		do {
 			//Sequentially compute the outcomes at subsequent levels
 			nextPreferredSet = computeCurrentPreferredSet();
 			if(nextPreferredSet != null && nextPreferredSet.getOutcomeSequence().size()>0) {
 				weakOrder.add(nextPreferredSet);
 			}
 			PreferenceReasoner.currentMaximalOutcomes = new ArrayList<String[]>();
 			if(getOutcomeCount() >= Constants.NUM_OUTCOMES) {
 				return weakOrder;
 			}
 			//After each level has been computed, remove outcomes just computed from the model so that the next level can be computed
 			removeOutcomes(nextPreferredSet);
 		} while (nextPreferredSet != null && !nextPreferredSet.getOutcomeSequence().isEmpty() && !(getOutcomeCount() >= Constants.NUM_OUTCOMES));
 		return weakOrder;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see translate.PreferenceReasoner#computeCurrentPreferredSet()
 	 */
 	public OutcomeSequence computeCurrentPreferredSet() throws IOException {
 		
 		Set<String> next = null;
 		OutcomeSequence visited = new OutcomeSequence();
 		boolean computedEnoughOutcomes = false;
 		do {
 			//Iteratively generate all outcomes at the top-most level (non-dominated set)
 			next = nextPreferred();
 			
 			if(next != null) {
 				visited.addOutcome(next);
 				
 				//Stop if the user has set a maximum number of outcomes to be computed
 				if(addOutcomeSequenceToGeneratedSequence(new OutcomeSequence(next)) >= Constants.NUM_OUTCOMES) {
 					computedEnoughOutcomes = true;
 				}
 			}
 		} while(next != null && !computedEnoughOutcomes);
 		
 		return visited;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see translate.PreferenceReasoner#computeNextPreferredSetIgnoring(test.OutcomeSequence)
 	 */
 	public OutcomeSequence computeNextPreferredSetIgnoring(OutcomeSequence ignoredOutcomes) throws IOException {
 		//Remove ignored outcomes from the model
 		removeOutcomes(ignoredOutcomes);
 		return computeCurrentPreferredSet();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see translate.PreferenceReasoner#removeOutcomes(test.OutcomeSequence)
 	 */
 	public void removeOutcomes(OutcomeSequence outcomes) {
 		//Remove outcomes from the model by making the negation of each outcome invariant 
 		for (String[] outcome : outcomes.getOutcomeSequenceAsListOfStringArray()) {
 			if(Constants.CURRENT_MODEL_CHECKER==Constants.MODEL_CHECKER.CadenceSMV) {
 				invariants.add("INVAR !(" + OutcomeFormatter.formatOutcome(outcome) + ");");
 			} else {
 				invariants.add("INVAR !(" + OutcomeFormatter.formatOutcome(outcome) + ")");
 			}
 		}
 	}
 
 	/**
 	 * Returns a CTL property that specifies that there is always an improving flip beginning from the current node 
 	 * @return spec
 	 */
 	private String getNextPreferredSpec() {
 		String formattedOutcomes = new String();
 		for (String[] outcome : currentMaximalOutcomes) {
 			String currentFormattedOutcome = OutcomeFormatter.formatOutcome(outcome);
 			formattedOutcomes = formattedOutcomes + " | (" + currentFormattedOutcome + ")";
 		}
 		//CTL property that specifies that there is always an improving flip beginning from the current node
 		String spec = SpecHelper.getCTLSpec("EF (gch=1)" + formattedOutcomes, "topElement","top element (non-dominated)");
 		return spec;
 	}
 
 	/**
 	 * Returns an LTL property that specifies that there is a cycle in the induced preference graph (sequence of improving flips leading from and to the same outcome)
 	 * @return spec
 	 */
 	private String getConsistencySpec() {
 		//LTL property that specifies that there is a cycle in the induced preference graph (sequence of improving flips leading from and to the same outcome)
 		String spec = SpecHelper.getLTLSpec("F G (gch=0)", "consistency", "consistency: in all paths there exists a future state where there is no more improvement/worsening (there is no cycle); counter example: a cyclic path");
 		return spec;
 	}
 	
 	/**
 	 * Returns a CTL property specifying that there is a path from outcome1 to outcome 2 (outcome2 is better than outcome1)
 	 * @param worse Less preferred outcome
 	 * @param better More preferred outcomes
 	 * @return
 	 */
 	private String getDominanceSpec(Set<String> worse, Set<String> better) {
 		String spec = new String();
 		String outcome1 = new String();
 		String outcome2 = new String();
 		String readableOutcome1 = new String();
 		String readableOutcome2 = new String();
 		for (int j = 0; j < variables.length; j++) {
 			String variable = variables[j];
 			
 			if(outcome1.length()>0) {
 				outcome1 = outcome1 + " & ";
 				readableOutcome1 = readableOutcome1 + ",";
 			}
 			
 			if(outcome2.length()>0) {
 				outcome2 = outcome2 + " & ";
 				readableOutcome2 = readableOutcome2 + ",";
 			}
 			
 			if(worse.contains(variable)) {
 				//outcome1 is worse than outcome2
 				outcome1 = outcome1 + variables[j] + "=" + "1";
 				readableOutcome1 = readableOutcome1 + variable;
 			} else {
 				outcome1 = outcome1 + variables[j] + "=" + "0";
 			}
 
 			if(better.contains(variable)) {
 				//outcome2 is better than outcome1
 				outcome2 = outcome2 + variables[j] + "=" + "1";
 				readableOutcome2 = readableOutcome2 + variable;
 			} else {
 				outcome2 = outcome2 + variables[j] + "=" + "0";
 			}
 		}
 		//CTL property specifying that there is a path from outcome1 to outcome 2 (outcome2 is better than outcome1) 	
 		spec = SpecHelper.getCTLSpec("("+ outcome1 + " -> EX EF (" + outcome2 + ")) ","dominance","-- "+ " (" + readableOutcome1 + ") -> (" + readableOutcome2 + ")");
 		return spec;
 	}
 	
 	/**
 	 * Returns a CTL property specifying that there is no path from outcome1 to outcome 2 (outcome2 is better than outcome1)
 	 * @param worse Less preferred outcome
 	 * @param better More preferred outcomes
 	 * @return
 	 */
 	private String getNegatedDominanceSpec(Set<String> worse, Set<String> better) {
 		String spec = new String();
 		String outcome1 = new String();
 		String outcome2 = new String();
 		String readableOutcome1 = new String();
 		String readableOutcome2 = new String();
 		for (int j = 0; j < variables.length; j++) {
 			String variable = variables[j];
 			if(outcome1.length()>0) {
 				outcome1 = outcome1 + " & ";
 				readableOutcome1 = readableOutcome1 + ",";
 			}
 			
 			if(outcome2.length()>0) {
 				outcome2 = outcome2 + " & ";
 				readableOutcome2 = readableOutcome2 + ",";
 			}
 			
 			if(worse.contains(variable)) {
 				//outcome1 is worse than outcome2
 				outcome1 = outcome1 + variables[j] + "=" + "1";
 				readableOutcome1 = readableOutcome1 + variable;
 			} else {
 				outcome1 = outcome1 + variables[j] + "=" + "0";
 			}
 			
 			if(better.contains(variable)) {
 				//outcome2 is better than outcome1
 				outcome2 = outcome2 + variables[j] + "=" + "1";
 				readableOutcome2 = readableOutcome2 + variable;
 			} else {
 				outcome2 = outcome2 + variables[j] + "=" + "0";
 			}
 		}
 		//CTL property specifying that there is no path from outcome1 to outcome 2 (outcome2 is better than outcome1) 	
		spec = SpecHelper.getCTLSpec("(("+ outcome1 + " -> !EX EF (" + outcome2 + "))) ","counterExampleForDominanceTest"," (" + readableOutcome1 + ") -> (" + readableOutcome2 + ")");
 		return spec;
 	}
 }
