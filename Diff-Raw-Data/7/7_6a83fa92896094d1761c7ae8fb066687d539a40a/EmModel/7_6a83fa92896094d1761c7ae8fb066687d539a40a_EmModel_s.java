 package em.implementation;
 
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.math.RoundingMode;
 import java.util.HashMap;
 import java.util.Map;
 
 import utils.Misc;
 import data.Alphabet;
 import data.Dataset;
 import data.Instance;
 
 /**
  * Implements a multinomial naive bayes classifier as described in:
  * 
  * Text Classification from Labeled and Unlabeled Documents using EM (1999)
  * Kamal Nigam, Andrew Kachites Mccallum, Sebastian Thrun, and Tom Mitchell
  * 
  * @author dmitriy dligach
  *
  */
 public class EmModel {
 
   // number of classes in training and test data
   protected int numClasses;
   // number of words (features) in training data
   protected int numWords;
   
   // p(w|c) for all classes 
   protected double[][] theta;
   // p(c) for all classes
   protected double[] priors;
 	
   // map labels to ints and ints to labels
   Alphabet labelAlphabet;
   
   /** 
    * Set the label alphabet here. This cannot be done in initialize()
    * using the dataset's alphabet because sometimes the training data
    * may not have all the labels that exist in the test data.
    */
 	public EmModel(Alphabet labelAlphabet) {
 
 	  this.labelAlphabet = labelAlphabet;
 	}
 	
   /**
    * Initialize various counts and data structures 
    * needed for training and train a model using a dataset. 
    * Assume alphabet and vectors have been generated for the dataset. 
    */
   public void train(Dataset dataset) {
 
     // init counts and data structures needed for training
     numClasses = labelAlphabet.size();
     numWords = dataset.getNumberOfDimensions();
     
     priors = new double[numClasses];
     theta = new double[numClasses][numWords];
     
     computeTheta(dataset);
     computePriors(dataset);
   }
   
   /**
    * Calculate p(w|c) for all words in training data 
    * and for each class. Based on equation 5 in the paper.
    */
   private void computeTheta(Dataset dataset) {
     
     // precompute total word count in each class
     double[] totalWordCountInClass = new double[numClasses];
     for(int label = 0; label < numClasses; label++) {
       totalWordCountInClass[label] = getTotalWordCountInClass(dataset, label);
     }
   
     // calculcate p(w|c) for each word in each class
     for(int label = 0; label < numClasses; label++) {
       for(int word = 0; word < numWords; word++) {
         
         theta[label][word] = 
             (1 + getWordCountInClass(dataset, word, label)) / 
             (numWords + totalWordCountInClass[label]);
         
         assert !Double.isNaN(theta[label][word]);
         assert !Double.isInfinite(theta[label][word]);
       }
     }
   }
   
   /**
    * Compute the sum from the denominator of Equation 5. 
    */
   private double getTotalWordCountInClass(Dataset dataset, int classIndex) {
     
     double sum = 0;
     for(int word = 0; word < numWords; word++) {
       sum += getWordCountInClass(dataset, word, classIndex);
     }
     
     return sum;
   }
 
   /**
    * Compute the sum from the numerator of Equation 5.
    */
   private double getWordCountInClass(Dataset dataset, int wordIndex, int classIndex) {
     
     double sum = 0;
     for(Instance instance : dataset.getInstances()) {
       String label = labelAlphabet.getString(classIndex);
       Float wordCount = instance.getDimensionValue(wordIndex); // null if count = 0 for this word
       if(wordCount != null) {
         sum += wordCount * instance.getClassProbability(label); 
       }
     }
     
     return sum;
   }
   
   /**
    * Compute p(c) for each class. Basedon on Equation 6 in the paper.
    */
   private void computePriors(Dataset dataset) {
     
     for(int classIndex = 0; classIndex < numClasses; classIndex++) {
       
       double sum = 0;
       for(Instance instance : dataset.getInstances()) {
         String label = labelAlphabet.getString(classIndex);
         sum += instance.getClassProbability(label);
       }
       
       priors[classIndex] = (1 + sum) / (numClasses + dataset.size());
       
       assert !Double.isNaN(priors[classIndex]);
       assert !Double.isInfinite(priors[classIndex]);
     }
   }
 
   /**
    * Classify instances in a dataset. Return accuracy.
    */
   public double test(Dataset dataset) {
     
     int correct = 0;
     int total = 0;
     
     for(Instance instance : dataset.getInstances()) {
       int prediction = classify(instance);
       if(prediction == labelAlphabet.getIndex(instance.getLabel())) {
         correct++;
       }
       total++;
     }
 
     return (double) correct / total;
   }
   
 	/**
    * Classify instances in a dataset. Also set probability distribution
    * over classes for each instance. Return accuracy.
    */
   public double label(Dataset dataset) {
     
     int correct = 0;
     int total = 0;
     
     for(Instance instance : dataset.getInstances()) {
 
       double[] logSum = getUnnormalizedClassLogProbs(instance);
       double[] p = logToProb(logSum);
       
       Map<String, Float> labelProbabilityDistribution = new HashMap<String, Float>();
       for(int label = 0; label < numClasses; label++) {
         labelProbabilityDistribution.put(labelAlphabet.getString(label), (float)p[label]);
       }
       instance.setLabelProbabilityDistribution(labelProbabilityDistribution); 
        
       int prediction = Misc.getIndexOfLargestElement(p);
      if(prediction == labelAlphabet.getIndex(instance.getLabel())) {
         correct++;
       }
       total++;
     }
 
     return (double) correct / total;
   }
 			
 	 /**
    * Calculate for each class:
    * 
    * p(c|w_0, ..., w_n-1) ~ p(c)p(w_0|c)p(w_1|c)...p(w_n-1|c)
    * where w_0, ..., w_n-1 are words in the document in positions w_0, ..., w_n-1.
    *   
    * Calculations are done in log space. I.e. we need to calculate:
    * log[p(c)p(w_0|c)...p(w_n-1|c)] = log[p(c)] + log[p(w_0|c)] + ... + log[p(w_n-1|c)]
    * 
    * OOV words (i.e. words not seen during training) are currently ignored.
    * 
    * TODO: For the inner loop, iterate over words that exist in this vector.
    * For some of them (OOV words), p(w|c) will be unknown (ignore them).
    */
   public double[] getUnnormalizedClassLogProbs(Instance instance) {
     
     double[] logSum = new double[numClasses];
 
     for(int label = 0; label < numClasses; label++) {
       logSum[label] = Math.log10(priors[label]); 
 
       // iterate over words that were seen during training
       for(int word = 0; word < numWords; word++) {
         Float wordCount = instance.getDimensionValue(word);
         if(wordCount == null) {
           // this instance does not contain the current word; just go on to
           // the next word (i.e. pretend the wordCount for this word is zero)
           continue;
         }
         
         logSum[label] += wordCount * Math.log10(theta[label][word]);
         
         assert !Double.isNaN(logSum[label]);
         assert !Double.isInfinite(logSum[label]);
       }
     }
     
     return logSum;
   }
 	
   /**
    * Calculate 10^exponent when y is very small and fractional.
    * Basically split exponent into its integer i and fractional f parts.
    * I.e. 10^exponent = 10^(i + f) = 10^i * 10^f
    */
   private static BigDecimal powerOfTen(double exponent) {
     
     BigDecimal exponentAsBigDecimal = new BigDecimal(String.valueOf(exponent));
     BigDecimal integerPart = new BigDecimal(exponentAsBigDecimal.intValue());
     BigDecimal fractionalPart = exponentAsBigDecimal.subtract(integerPart);
 
     BigDecimal tenToIntegerPart = new BigDecimal(BigInteger.valueOf(10), -1 * integerPart.intValue());
     BigDecimal tenToFractionalPart = new BigDecimal(Math.pow(10, fractionalPart.doubleValue()));
     
     BigDecimal result = tenToIntegerPart.multiply(tenToFractionalPart);
     
     return result;
   }
     
   /**
    * Convert unnormalized log probabilities to probabilities for each class.
    */
   public double[] logToProb(double[] unnormalizedClassLogProbs) {
    
     double[] probs = new double[unnormalizedClassLogProbs.length];
     
     // unnormalized probabilities are often very small, e.g. 10^-802.345
     // which causes an underflow, so need to use big decimal instead
     BigDecimal[] unnormalizedClassProbs = new BigDecimal[unnormalizedClassLogProbs.length];
       
     // we have log(p(c)p(w_0|c)...p(w_n-1|c)) for each class
     // compute unnormalized probabilities as 10^(p(c)p(w_0|c)...p(w_n-1|c))
     for(int label = 0; label < numClasses; label++) {
       unnormalizedClassProbs[label] = powerOfTen(unnormalizedClassLogProbs[label]);
     }
     
     // compute the normalization constant
     BigDecimal normalizer = new BigDecimal(0);
     for(int label = 0; label < numClasses; label++) {
       normalizer = normalizer.add(unnormalizedClassProbs[label]);
     }
     
     for(int label = 0; label < numClasses; label++) {
       probs[label] = unnormalizedClassProbs[label].divide(normalizer, RoundingMode.HALF_UP).doubleValue();
     }
     
     return probs;
   }
   
   /**
    * Convert two unnormalized log probs to probabilities.
    */
   @Deprecated
   public double[] logToProb(double logP0, double logP1) {
 
     double[] p = new double[2];
     
     // difference logP0 - logP1 larger than a few hundred causes an overflow
     // when evaluating 10 ^ (logP0 - logP1); however the difference of that 
     // size means that p0 is many orders of magnitude larger than p1;
     // so we can just assume that p0 is one and p1 is zero
     // e.g. logP0 = -1000, logP1 = -2000, logP0 - logP1 = 1000;
     // which means that p0 / p1 is 10^1000, i.e. p0 >> p1
     // the underflow is not an issue; e.g. 10^-1000 is just zero
     // TODO: can use Double.MAX_VALUE instead of 300
     final int MAXDIFFERENCE = 300;
     if((logP0 - logP1) > MAXDIFFERENCE) {
       p[0] = 1.0;
       p[1] = 0.0;
       
       return p;
     }
     
     // odds0 is p0/p1 or p0 / (1 - p0)
     double odds0 = Math.pow(10, logP0 - logP1);
 
     p[0] = odds0 / (1 + odds0);
     p[1] = 1 / (1 + odds0);
     
     assert !Double.isNaN(p[0]);
     assert !Double.isNaN(p[1]);
     assert !Double.isInfinite(p[0]);
     assert !Double.isInfinite(p[1]);
 
     return p;
   }
   
   /**
    * Classify a document using a multinomial naive bayes model. 
    */
   public int classify(Instance instance) {
     return Misc.getIndexOfLargestElement(getUnnormalizedClassLogProbs(instance));
   }
 }
