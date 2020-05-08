 package org.eclipse.emf.refactor.smells.runtime.core;
 
 import java.util.LinkedList;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.refactor.smells.core.ModelSmell;
 
 
 /**
  * Class handling the execution of a list of <i>ModelSmell</i> objects on a given root elements.
  * <br>- Is a singleton.
  * 
  * @author Matthias Burhenne
  *
  */
 
 public class ModelSmellFinder {
 	private static final ModelSmellFinder instance = new ModelSmellFinder();
 	//private LinkedList<Result> results = new LinkedList<Result>();
 	private static EObject rootElement;
 	
 	private ModelSmellFinder(){
 		
 	}
 	
 	public static ModelSmellFinder instance(){
 		return instance;
 	}
 	
 	/**
 	 * Starts the identification of the passed smells on the root object and its
 	 * <br>- transtitively contained objects.
 	 * @param smells - list of <i>ModelSmell</i> objects
 	 * @param root - root element of the model under inspection
 	 * @return - results of the search
 	 */
 	public static LinkedList<Result> findModelSmells(LinkedList<ModelSmell> smells, EObject root){
 		rootElement = root;
 		String nsUri = ((EPackage)root.eClass().eContainer()).getNsURI();
 		LinkedList<Result> results = new LinkedList<Result>();
 		for(ModelSmell smell : smells){
 			if(smell.getMetamodel().equals(nsUri))
 				results.add(findModelSmell(smell));
 		}
 		return results;
 	}
 	
	private static Result findModelSmell(ModelSmell smell){
 		LinkedList<LinkedList<EObject>> modelelements = smell.getFinderClass().findSmell(rootElement);
 		return new Result(smell, modelelements);
 	}
 
 }
