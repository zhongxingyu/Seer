 package gr.auth.ee.lcs.data;
 
 import gr.auth.ee.lcs.classifiers.Classifier;
 import gr.auth.ee.lcs.classifiers.ExtendedBitSet;
 
 /**
  * A bridge [GoF] that decouples the chromosome bit representation and the data-set specific meaning
  * The object holds a static instance that implements the actual transformation.
  * All function calls are diverted to the static instance.
  * @author Miltos Allamanis
  */
 public abstract class ClassifierTransformBridge {
 
   /** 
    *  the singleton instance of the bridge
    */
   public static ClassifierTransformBridge instance;
 
   /** 
    *  checks if the visionVector matches the condition of the given chromosome
    */
   public abstract boolean isMatch(double[] visionVector, ExtendedBitSet chromosome);
 
   /** 
    *  converts the given classifier to a natural language rule
    */
   public abstract String toNaturalLanguageString(Classifier aClassifier);
 
   /** 
    *  sets the static instance of the bridge
    */
   public static void setInstance(ClassifierTransformBridge aBridge) {
 	  ClassifierTransformBridge.instance=aBridge;
   }
 
   /** 
    *  Creates a random classifier to cover the visionVector
    */
   public abstract Classifier createRandomCoveringClassifier(double[] visionVector, int advocatingAction);
 
   /** 
    *  Tests the given chromosomes if the baseClassifier is a more general version of the testClassifier
    */
   public abstract boolean isMoreGeneral(Classifier baseClassifier, Classifier testClassifier);
 
   /**
    * Tests if two classifiers are equal
    * @param cl1 the first classifier to be compared
    * @param cl2 the second classifier to be compared
    * @return true if classifiers are equal, else false
    */
   public abstract boolean areEqual(Classifier cl1, Classifier cl2);
   
   /** 
    *  Fixes a chromosome so as to be in the correct value range (e.g. after mutation or crossover)
    *  @param aChromosome the chromosome to be fixed
    */
   public abstract void fixChromosome(ExtendedBitSet aChromosome);
 
  /**
   * Calls to the bridge to fix a classifier
   * @param toBeFixed
   */
  public static void fixClassifier(Classifier toBeFixed){
	  ClassifierTransformBridge.instance.fixChromosome(toBeFixed.chromosome);
  }
  
   /** 
    *  @return the size of the chromosome (used for the chromosome construction)
    */
   public abstract int getChromosomeSize();
 
   /**
    * 
    * @param classifier
    * @return
    */
   public abstract String toBitSetString(Classifier classifier);
 
   /**
    * Each implementation of the ClassifierTransformBridge might choose to save additional data
    * on each classifier. This data is a Serializable object and is representation-specific.
    * @param aClassifier 
    */
   public abstract void setRepresentationSpecificClassifierData(Classifier aClassifier);
 
   public abstract void buildRepresentationModel();
 
 }
