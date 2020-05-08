 package gr.auth.ee.lcs;
 
 import gr.auth.ee.lcs.classifiers.Classifier;
 import gr.auth.ee.lcs.classifiers.ClassifierSet;
 import gr.auth.ee.lcs.classifiers.DummySizeControlStrategy;
 import gr.auth.ee.lcs.data.ClassifierTransformBridge;
 import gr.auth.ee.lcs.data.UpdateAlgorithmFactoryAndStrategy;
 import gr.auth.ee.lcs.geneticalgorithm.IGeneticAlgorithmStrategy;
 
 public class LCSTrainTemplate {
 
 	public IGeneticAlgorithmStrategy ga;
 	
 	  public void trainWithDataSet(double[][] dataSet) {
 	  }
 
 	  public void trainWithInstance(ClassifierSet population, double[] dataInstance,int expectedAction) {
 		  //Generate MatchSet
 		  ClassifierSet matchSet=new ClassifierSet(new DummySizeControlStrategy());
 		  for (int i=0;i<population.getNumberOfMacroclassifiers();i++){
 			  if ( population.getClassifier(i).isMatch(dataInstance)){
 				  matchSet.addClassifier(population.getClassifier(i), population.getClassifierNumerosity(i));
 			  }
 		  }
 		  
 		  //Generate Correct Set
 		  ClassifierSet correctSet=new ClassifierSet(new DummySizeControlStrategy());
 		  for (int i=0;i<matchSet.getNumberOfMacroclassifiers();i++){
 			  if (matchSet.getClassifier(i).actionAdvocated==expectedAction)
 				  correctSet.addClassifier(matchSet.getClassifier(i), matchSet.getClassifierNumerosity(i));		  
 		  }
 		  
 		  if (correctSet.getNumberOfMacroclassifiers()==0){ //Cover
			  Classifier coveringClassifier= ClassifierTransformBridge.instance.createRandomCoveringClassifier(dataInstance,expectedAction);
 			  coveringClassifier.actionAdvocated=expectedAction;
 			  population.addClassifier(coveringClassifier, 1);
 			  return;
 		  }
 		  
		  UpdateAlgorithmFactoryAndStrategy.updateData(matchSet,correctSet);
 		  ga.evolveSet(correctSet, population); //TODO: Timed activation
 		  
 		  
 	  }
 
 }
