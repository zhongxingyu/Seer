 import java.util.*;
 
 public class TheoremChecker extends Expression{ //used to check whether Theorem matches expression
 	
 	private HashMap<String, subExpression> subValues;
 	private Expression theorem;
 	private Expression toEvaluate;
 	
 	public TheoremChecker() {
 		subValues = new HashMap<String, subExpression>();
 	}
 	public TheoremChecker(Expression thm, Expression tbe) throws IllegalInferenceException{
 		subValues = new HashMap<String, subExpression>();
 		theorem = thm;
 		toEvaluate = tbe;
 		if (ProofChecker.iAmDebugging){
 			System.out.println("Checking application of "+thm.toString()+" to "+tbe.toString());
 		}
 		constructorHelper(thm.getRoot(), tbe.getRoot()); //fills subValues HashMap
 	}
 	public boolean containsKey(String name) {
 		return subValues.containsKey(name);
 	}
 	public subExpression get(String name) {
 		return subValues.get(name);
 	}
 	
 	//for printing errors with mismatched expressions
 	public String errorPrinting() {
 		String returnVal = "";
 		Set<String> keys = subValues.keySet();
 		if (keys.isEmpty()) {
 			return returnVal;
 		}
 		for (String key : keys) {
 			subExpression temp = subValues.get(key);
 			if (temp == null) {
 				returnVal = returnVal + key + "= NOT SET!, ";
 			} else {
 				returnVal = returnVal + key + "=" + subValues.get(key).toString()+", ";
 			}
 		}
 		returnVal = returnVal.substring(0,(returnVal.length()-2));
 		return returnVal;
 	}
 	
 	private void constructorHelper(subExpression thm, subExpression tbe) throws IllegalInferenceException{
 		if (tbe == null) {
 			throw new IllegalInferenceException("Bad theorem application "+errorPrinting());
 		} else if (thm.isVariable()) {
			try {
				if ((subValues.containsKey(thm.getName())) && 
						(!(subValues.get(thm.getName()).toString().equals(tbe.toString())))) {
					throw new IllegalInferenceException("Bad theorem application "+ errorPrinting());
				}
 				subValues.put(thm.getName(),tbe);
 			} catch (NullPointerException e) {
 				throw new IllegalInferenceException("Bad theorem application "+errorPrinting());
 			}
 		} else if (thm.getName().equals("~")) {
 			try {
 				constructorHelper(thm.getLeft(),tbe.getLeft());
 			} catch (NullPointerException e) {
 				throw new IllegalInferenceException("Bad theorem application "+errorPrinting());
 			}
 		} else {
 			try {
 				constructorHelper(thm.getLeft(),tbe.getLeft());
 				constructorHelper(thm.getRight(),tbe.getRight());
 			} catch (NullPointerException e) {
 				throw new IllegalInferenceException("Bad theorem application "+errorPrinting());
 			}
 		}
 	}
 	public boolean matches() {
 		return matchesHelper(theorem.getRoot(), toEvaluate.getRoot());
 	}
 	private boolean matchesHelper(subExpression thm, subExpression tbe) {
 		if (thm.isVariable()) {
 			return tbe.equals(subValues.get(thm.getName()));
 		} else if (thm.getName().equals(tbe.getName())) {
 			boolean lReturn = false;
 			boolean rReturn = false;
 			if (thm.getLeft() != null && tbe.getLeft() != null) {
 				lReturn = matchesHelper(thm.getLeft(), tbe.getLeft());
 			}
 			if (thm.getRight() != null && tbe.getRight() != null) {
 				rReturn = matchesHelper(thm.getRight(), tbe.getRight());
 			}
 			if (thm.getName().equals("~")) {
 				rReturn = true;
 			}
 			return lReturn && rReturn;
 		} else {
 			return false;
 		}
 	}
 }
