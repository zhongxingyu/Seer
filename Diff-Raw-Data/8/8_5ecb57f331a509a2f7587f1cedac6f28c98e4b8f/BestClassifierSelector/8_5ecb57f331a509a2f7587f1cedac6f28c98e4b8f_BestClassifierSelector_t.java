 package gr.auth.ee.lcs.geneticalgorithm;
 
 import gr.auth.ee.lcs.classifiers.ClassifierSet;
 
 /**
  * Selects and adds the best classifier (based on fitness) from the inital ClassifierSet to the target set.
  * It adds the best classifier with howManyToSelect numerosity
  * @author Miltos Allamanis
  *
  */
 public class BestClassifierSelector implements INaturalSelector {
 
 	private boolean max=true;
 	private int mode;
 	
 	/**
 	 * Default constructor
 	 * @param max if by best we mean the max fitness then true, else false
 	 * @param mode the mode of the values taken
 	 */
 	public BestClassifierSelector(boolean max, int mode){
 		this.max=max;
 		this.mode=mode;
 	}
 	
 	/**
 	 * Implementation of abstract method
 	 * Selects the best classifier in the fromPopulation and adds it to toPopulation
 	 * @param howManyToSelect the numerosity that the best classifier is going to be added
 	 * @param fromPopulation the source set of classifiers
 	 * @param toPopulation the target set of classifiers. In this set the best classifier will be added
 	 */
 	public void select(int howManyToSelect, ClassifierSet fromPopulation, ClassifierSet toPopulation) {
 	   //Add it toPopulation
	  int bestIndex=select(fromPopulation);
	  if (bestIndex==-1)
		  return;
	  toPopulation.addClassifier(fromPopulation.getClassifier(bestIndex), howManyToSelect);
   }
 
 	@Override
 	public int select(ClassifierSet fromPopulation) {
 		 //Search for the best classifier
 		  double bestFitness=max?Double.NEGATIVE_INFINITY:Double.POSITIVE_INFINITY;
 		  int bestExp=0;
 		  int bestIndex=-1;
 		  for (int i=0;i<fromPopulation.getNumberOfMacroclassifiers();i++){
 			  double temp=fromPopulation.getClassifier(i).getComparisonValue(mode);
 			  if ((max?1.:-1.)*(temp-bestFitness)>0){
 				  bestFitness=temp;
 				  bestIndex=i;
 				  bestExp=fromPopulation.getClassifier(i).experience;
			  }else if (temp==bestFitness && fromPopulation.getClassifier(i).experience>bestExp){
 				  bestFitness=temp;
 				  bestIndex=i;
 				  bestExp=fromPopulation.getClassifier(i).experience;
 			  }
 		  }
 		  
 		return bestIndex;
 	}
 
 }
