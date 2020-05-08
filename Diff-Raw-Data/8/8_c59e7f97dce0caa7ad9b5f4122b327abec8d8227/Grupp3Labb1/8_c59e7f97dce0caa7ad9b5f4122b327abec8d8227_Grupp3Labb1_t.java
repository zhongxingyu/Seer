 /*
  *    This program is free software; you can redistribute it and/or modify
  *    it under the terms of the GNU General Public License as published by
  *    the Free Software Foundation; either version 2 of the License, or
  *    (at your option) any later version.
  *
  *    This program is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *    GNU General Public License for more details.
  *
  *    You should have received a copy of the GNU General Public License
  *    along with this program; if not, write to the Free Software
  *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
  */
 
 /*
  *    Id3.java
  *    Copyright (C) 1999 University of Waikato, Hamilton, New Zealand
  *
  */
 package weka.classifiers.trees;
 
 import java.util.*;
 
 import weka.classifiers.AbstractClassifier;
 import weka.core.AdditionalMeasureProducer;
 import weka.core.Attribute;
 import weka.core.Capabilities;
 import weka.core.Instance;
 import weka.core.Instances;
 import weka.core.NoSupportForMissingValuesException;
 import weka.core.Option;
 import weka.core.OptionHandler;
 import weka.core.RevisionUtils;
 import weka.core.SelectedTag;
 import weka.core.Tag;
 import weka.core.TechnicalInformation;
 import weka.core.TechnicalInformationHandler;
 import weka.core.Utils;
 import weka.core.Capabilities.Capability;
 import weka.core.TechnicalInformation.Field;
 import weka.core.TechnicalInformation.Type;
 
 /**
 <!-- globalinfo-start -->
  * Laborationsmall för att skapa ett träd som både kan använda GainRatio och GiniIndex 
  * utifrån parameterval. Det skall även gå att välja mellan att ha binära eller multipla 
  * splits. Vidare skall algoritmen även ha stöd för att använda numeriska variabler. 
  * <br/>
  * Följande är krav på labben:
  * Implementering av GainRatio och GiniIndex
  * Binära och multipla splits
  * Numeriska variabler
  * En korrekt utskrift av trädet
  * Möjlighet att specificera minsta antalet instanser som tillåts i lövnoderna 
  * Debugläge, som skriver ut info om alla viktiga händelser som sker
  * <br/>
  * Extra funktionalitet som kan implementeras:
  * Möjlighet att pruna trädet
  * En översättning av trädet till en java-klass (implementering av interfacet Sourcable)
  * <p/>
 <!-- globalinfo-end -->
  *
  * <p/>
 <!-- technical-bibtex-end -->
  *
 <!-- options-start -->
  * Valid options are: <p/>
  * 
  * <pre> -M &lt;minimum number of instances&gt;
  *  Set minimum number of instances per leaf.
  *  (default 2)</pre>
  * 
  * <pre> -B
  *  Use binary splits only.</pre>
  * 
  * <pre> -Q &lt;seed&gt;
  *  Seed for random data shuffling (default 1).</pre>
  *  
  * <pre> -D
  *  If set, classifier is run in debug mode and
  *  may output additional info to the console</pre>
  * 
 <!-- options-end -->
  *
  * @author Johan, Daniel, Fredrik, Andreas
  * @version $Revision:  $
  */
 public class Grupp3Labb1
         extends AbstractClassifier
         implements TechnicalInformationHandler, OptionHandler, AdditionalMeasureProducer {//, Sourcable {
     public static final int MIN_SPLIT = 3;
 
     /** for serialization */
     @SuppressWarnings("UnusedDeclaration")
     static final long serialVersionUID = -2693678647096322561L;
     /** The node's successors. */
     private Grupp3Labb1[] m_Successors;
     /** Attribute used for splitting. */
     private Attribute m_Attribute;
     /** Instance **/
     private Instances m_Data;
     /** Class value if node is leaf. */
     private double m_ClassValue;
     /** Class distribution if node is leaf. */
     private double[] m_Distribution;
     /** Class attribute of dataset. */
     private Attribute m_ClassAttribute;
     /** Minimum number of instances in leafs*/
     //private int m_minNumObj = 2;
     /** The minimum leaf size */
     private double m_MinimumLeafSize;
     /** The majority class */
     private static int m_MajorityClass;
     /** The spliting method */
     public static Tag[] TAGS_SplitMethod = {
         new Tag(0, "GainRatio"),
         new Tag(1, "GiniIndex")
     };
     public int m_SplitMethod = 0;
     /** Binary splits on nominal attributes? */
     private boolean m_UseBinarySplits;
 
     int m_NumberOfSplits = MIN_SPLIT;
     private double[] splitValues;
 
     /**
      * Returns a string describing the classifier.
      * @return a description suitable for the GUI.
      */
     public String globalInfo() {
         return "Class for constructing an unpruned decision tree based on the ID3 "
                + "algorithm. Can only deal with nominal attributes. No missing values "
                + "allowed. Empty leaves may result in unclassified instances. For more "
                 + "information see: \n\n" + getTechnicalInformation().toString();
     }
 
     /**
      * Returns an instance of a TechnicalInformation object, containing 
      * detailed information about the technical background of this class,
      * e.g., paper reference or book this class is based on.
      * 
      * @return the technical information about this class
      */
     public TechnicalInformation getTechnicalInformation() {
         TechnicalInformation result;
 
         result = new TechnicalInformation(Type.UNPUBLISHED);
         result.setValue(Field.AUTHOR, "A. Westberg");
         result.setValue(Field.AUTHOR, "D. Jansson");
         result.setValue(Field.AUTHOR, "F. Törnvall");
         result.setValue(Field.AUTHOR, "J. Dahlberg");
         result.setValue(Field.YEAR, "2013");
         result.setValue(Field.TYPE, "Laboration");
         result.setValue(Field.SCHOOL, "University of Borås");
         result.setValue(Field.NOTE, "Egna kommentarer");
         result.setValue(Field.NUMBER, "3");
 
         return result;
     }
 
     /**
      * Returns default capabilities of the classifier.
      *
      * @return      the capabilities of this classifier
      */
     public Capabilities getCapabilities() {
         Capabilities result = super.getCapabilities();
         result.disableAll();
 
         // attributes
         result.enable(Capability.NOMINAL_ATTRIBUTES);
         result.enable(Capability.NUMERIC_ATTRIBUTES);
         result.enable(Capability.MISSING_VALUES);
 
         // class
         result.enable(Capability.NOMINAL_CLASS);
         result.enable(Capability.MISSING_CLASS_VALUES);
 
         // instances
         result.setMinimumNumberInstances(0);
 
         return result;
     }
 
     /**
      * Builds Id3 decision tree classifier.
      *
      * @param data the training data
      * @exception Exception if classifier can't be built successfully
      */
     public void buildClassifier(Instances data) throws Exception {
         // can classifier handle the data?
         getCapabilities().testWithFail(data);
 
         // remove instances with missing class
         data = new Instances(data);
         data.deleteWithMissingClass();
 
         m_MajorityClass = getMajorityClass(data);
 
         makeTree(data);
     }
 
     private int getMajorityClass(Instances data) {
         int[] classCounts = new int[data.numClasses()];
         for (int i = 0; i < data.numInstances(); i++)
             classCounts[(int) data.instance(i).classValue()]++;
 
         return Utils.maxIndex(classCounts);
     }
 
     /**
      * Method for building an Id3 tree.
      *
      * @param data the training data
      * @exception Exception if decision tree can't be built successfully
      */
     private void makeTree(Instances data) throws Exception {
         // Check if no instances have reached this node.
         if (data.numInstances() == 0) {
             m_Attribute = null;
             m_ClassValue = Utils.missingValue();
             m_Distribution = new double[data.numClasses()];
             return;
         }
 
         printDebugMessage("\n--------------NEW NODE--------------");
         printDebugMessage("\nNumber of instances in: " + data.numInstances());
 
         // Compute attribute with maximum information gain.
         double[] bestAttr = new double[data.numAttributes()];
         Enumeration attEnum = data.enumerateAttributes();
         while (attEnum.hasMoreElements()) {
             Attribute att = (Attribute) attEnum.nextElement();
             bestAttr[att.index()] = computeAttributeValue(data, att);
             printDebugMessage("\n" + att.index() + ": " + bestAttr[att.index()]);
         }
 
         if (m_SplitMethod == 0) {
             m_Attribute = data.attribute(Utils.maxIndex(bestAttr));
             printDebugMessage("\nGainRatio for Atributes:");
         } else {
             m_Attribute = data.attribute(Utils.minIndex(bestAttr));
             printDebugMessage("\nGiniIndex for Atributes:");
         }
         printDebugMessage("\nChoosing: " + m_Attribute.index() + " (" + m_Attribute.name() + ")\n");
         Instances[] splitData = getSplitData(data, m_Attribute);
         if (Utils.eq(bestAttr[m_Attribute.index()], 0)&& !checkMinLeafSize(splitData)
                 || (m_SplitMethod == 1 && !checkMinLeafSize(splitData))) {
             makeLeaf(data);
         } else {
             m_Successors = new Grupp3Labb1[splitData.length];
 
             for (int i = 0; i < splitData.length; i++) {
                 if (splitData[i].numInstances() < m_MinimumLeafSize)
                     continue;
 
                 m_Successors[i] = new Grupp3Labb1();
                 m_Successors[i].m_Debug = m_Debug;
                 m_Successors[i].m_MinimumLeafSize = m_MinimumLeafSize;
                 m_Successors[i].m_SplitMethod = m_SplitMethod;
                 m_Successors[i].m_UseBinarySplits = m_UseBinarySplits;
                 m_Successors[i].makeTree(splitData[i]);
             }
         }
     }
 
     /**
      * Create leaf
      * - Refactored from makeTree
      *
      * @param data the leaf instance
      */
     private void makeLeaf(Instances data) {
         m_Attribute = null;
         m_Data = data;
         m_Distribution = new double[data.numClasses()];
 
         Enumeration instEnum = data.enumerateInstances();
         while (instEnum.hasMoreElements()) {
             Instance inst = (Instance) instEnum.nextElement();
             m_Distribution[(int) inst.classValue()]++;
         }
 
         Utils.normalize(m_Distribution);
         m_ClassValue = Utils.maxIndex(m_Distribution);
         m_ClassAttribute = data.classAttribute();
     }
 
     private boolean checkMinLeafSize(Instances[] data ){
         if(data == null)
             return false;
         boolean check = true;
         for (Instances instances : data) {
             if(instances == null || instances.numInstances() <= m_MinimumLeafSize){
                 check = false;
             }
         }
         return check;
     }
 
     /**
      * Classifies a given test instance using the decision tree.
      *
      * @param instance the instance to be classified
      * @return the classification
      * @throws NoSupportForMissingValuesException if instance has missing values
      */
     public double classifyInstance(Instance instance)
             throws NoSupportForMissingValuesException {
         int index = 0;
 
         if (m_Attribute == null){
             return m_ClassValue;
         } else {
             if(instance.isMissing(m_Attribute)){
                 instance.setValue(m_Attribute, instance.dataset().meanOrMode(m_Attribute.index()));
             }
 
             if(m_Attribute.isNumeric()) {
                 for(int i = 0; i < splitValues.length; i++){
                     if(instance.value(m_Attribute) <= splitValues[i]){
                         index = i;
                         break;
                     }
                 }
             } else if(m_Attribute.isNominal()){
                 if(m_UseBinarySplits) {
                     if(instance.value(m_Attribute) == splitValues[0])
                         index = 0;
                     else
                         index = 1;
                 } else {
                     index = (int) instance.value(m_Attribute);
                 }
             }
         }
 
         try{
             return m_Successors[index].classifyInstance(instance);
         } catch (Exception e){
             printDebugMessage("nullPointerException");
             return m_Successors[index].classifyInstance(instance);
         }
     }
 
     /**
      * Computes class distribution for instance using decision tree.
      *
      * @param instance the instance for which distribution is to be computed
      * @return the class distribution for the given instance
      * @throws NoSupportForMissingValuesException if instance has missing values
      */
     public double[] distributionForInstance(Instance instance)
             throws NoSupportForMissingValuesException {
         int index = 0;
         if (m_Attribute == null) {
             return m_Distribution;
         } else {
             if(instance.isMissing(m_Attribute)){
 
                 // instance.dataset().meanOrMode(m_Attribute)
                 double meanOrMode = instance.dataset().meanOrMode(m_Attribute.index());
                 instance.setValue(m_Attribute,meanOrMode);
 
                 //instance.setValue(m_Attribute,m_MajorityClass);
                 //instance.setValue(m_Attribute, handleMissingValue());
             }
             if(m_Attribute.isNumeric()) {
                 for(int i = 0; i < splitValues.length; i++){
                     if(instance.value(m_Attribute) <= splitValues[i]){
                         index = i;
                         break;
                     }
                 }
             }
             if(m_Attribute.isNominal()){
                 if(m_UseBinarySplits) {
                     if(instance.value(m_Attribute) == splitValues[0]) index = 0;
                     else index = 1;
                 } else {
                     index = (int) instance.value(m_Attribute);
                 }
             }
         }
         try{
             return m_Successors[index].distributionForInstance(instance);
         } catch (Exception e){
             printDebugMessage("nullPointerException");
             return m_Successors[index].distributionForInstance(instance);
         }
 
     }
 
     /**
     /**
      * TODO: Comment
      *
      * @param data the data for which info gain is to be computed
      * @param att the attribute
      * @return TODO: comment
      * @throws Exception if computation fails
      */
     private double computeAttributeValue(Instances data, Attribute att) throws Exception {
         switch (m_SplitMethod) {
             case 0: //GainRatio
                 double infoGain = computeInfoGain(data, att);
                 double splitInfo = computeSplitInfo(data, att);
                 printDebugMessage("InfoGain: " + infoGain);
                 printDebugMessage("SplitInfo: " + splitInfo);
                 printDebugMessage("GainRatio: " + infoGain/splitInfo);
                 if(splitInfo != 0)
                     return infoGain / splitInfo;
                 else
                     return 0;
             case 1: //GiniIndex
                 double giniIndex = computeGiniIndex(data, att);
                 printDebugMessage("GiniIndex: " + giniIndex);
                 return giniIndex;
         }
         throw new Exception("ComputeAttributeValue: Unreachable code");
     }
 
     /**
      * @param data the data for which gini index is to be computed
      * @param att the attribute
      * @return giniIndex
      */
     private double computeGiniIndex(Instances data, Attribute att){
         return computeGiniIndex(data, getSplitData(data, att));
     }
 
     /**
      * @param data the data for which gini index is to be computed
      * @param splitData the instances
      * @return giniIndex
      */
     private double computeGiniIndex(Instances data, Instances[] splitData){
         double gini = computeNode(data);
 
         if(splitData == null) return gini;
 
         for (Instances aSplitData : splitData) {
             gini -= ((double) aSplitData.size() / (double) data.size()) * computeNode(aSplitData);
         }
 
         return gini;
     }
 
     private double computeNode(Instances data){
         double nodeResult = 1.0;
 
         double[] classCount = new double[data.numClasses()];
         for (int init = 0; init < data.numClasses(); init++) {
             classCount[init] = 0;
         }
 
         // Count class frequency.
         for (int j = 0; j < data.numInstances(); j++) {
             classCount[(int) data.instance(j).classValue()]++;
         }
 
         //for each class result - P(C1)^2.. loop and do P(C2)^2.. and so on
         for (double aClassCount : classCount) {
             double p = aClassCount / (double) data.numInstances();
             nodeResult = nodeResult - (p * p);
         }
 
         return nodeResult;
     }
     /**
      * @param data the data for which info gain is to be computed
      * @param att the attribute
      * @return splitInfo
      */
     private double computeSplitInfo(Instances data, Attribute att){
         return computeSplitInfo(data, getSplitData(data, att));
     }
 
     private double computeSplitInfo(Instances data, Instances[] splitData){
         double splitInfo = 0.0;
 
         if(splitData == null) return splitInfo;
 
         for (Instances aSplitData : splitData) {
             double sInfo = aSplitData.numInstances() / (float)data.numInstances();
             if(sInfo <= 0.0)
                 continue;
 
             splitInfo -= sInfo * Utils.log2(sInfo);
         }
         return splitInfo;
     }
 
     /**
      * Computes information gain for an attribute.
      *
      * @param data the data for which info gain is to be computed
      * @param att the attribute
      * @return the information gain for the given attribute and data
      * @throws Exception if computation fails
      */
     private double computeInfoGain(Instances data, Attribute att)
             throws Exception {
         return computeInfoGain(data, getSplitData(data, att));
     }
 
     private double computeInfoGain(Instances data, Instances[] splitData)
             throws Exception {
         double infoGain = computeEntropy(data);
 
         if(splitData == null) return infoGain;
 
         for (Instances aSplitData : splitData) {
             if (aSplitData.numInstances() <= 0) continue;
             infoGain -= ((double) aSplitData.numInstances() / (double) data.numInstances())
                     * computeEntropy(aSplitData);
         }
 
         return infoGain;
     }
 
     /**
      * Computes the entropy of a dataset.
      * 
      * @param data the data for which entropy is to be computed
      * @return the entropy of the data's class distribution
      * @throws Exception if computation fails
      */
     private double computeEntropy(Instances data) throws Exception {
         double[] classCounts = new double[data.numClasses()];
         Enumeration instEnum = data.enumerateInstances();
         while (instEnum.hasMoreElements()) {
             Instance inst = (Instance) instEnum.nextElement();
             classCounts[(int) inst.classValue()]++;
         }
 
         double entropy = 0;
         for (int j = 0; j < data.numClasses(); j++) {
             if (classCounts[j] > 0.0)
                 entropy -= classCounts[j] * Utils.log2(classCounts[j]);
         }
 
         entropy /= (double) data.numInstances();
         if(data.numInstances() <= 0) return 0;
         else return entropy + Utils.log2(data.numInstances());
     }
 
     /**
      * @param data instances
      * @param att attribute
      * @return splitData according to the current setting and type of attribute
      */
     private Instances[] getSplitData(Instances data, Attribute att) {
         if(m_UseBinarySplits)
             return binarySplitData(data, att);
         else
             return splitData(data, att);
     }
 
     /**
      * Splits a dataset binarily, according to the values of a nominal attribute.
      *
      * @param data the data that is to be split
      * @param att the attribute to be used for splitting
      * @return Best split produced
      */
     private Instances[] binarySplitData(Instances data, Attribute att) {
         m_NumberOfSplits = 1;
 
         if(att.isNominal())
             return binarySplitDataNominal(data, att);
         else if(att.isNumeric())
             return binarySplitDataNumeric(data, att);
         else
             return null;
     }
 
     /**
      * @param data the data that is to be split
      * @param att the attribute to be used for splitting
      * @return Best split produced
      */
     private Instances[] binarySplitDataNominal(Instances data, Attribute att) {
         //Best splitt is saved here.
         Instances[] bestSplitt = null;
         List<Integer> indexBestAttribute = Collections.EMPTY_LIST;
         double bestEvaluationValue;
         if(m_SplitMethod == 0){
             bestEvaluationValue = 0.0;
         }
         else{
            bestEvaluationValue = Double.MIN_VALUE;
         }
 
         ArrayList<String> possibleSplits = getBinaryPossibilitiesCode(att.numValues());
         for (String s : possibleSplits) {
             ArrayList<Integer> attrIndexes = new ArrayList<Integer>();
             for (int y = 0; y < s.length(); y++) {
                 if (s.charAt(y) == '0') {
                     attrIndexes.add(y);
                 }
             }
             //make splits basted on attrIndexes.
             Instances[] splitData = new Instances[2];
             for (int a = 0; a < splitData.length; a++) {
                 splitData[a] = data.stringFreeStructure();
             }
 
             if(data == null)
                 throw new IllegalStateException();
             for(Instance inst : data){
                 // If instance has the same value as splitValue, put in the first bucket
                 Boolean isAttribValue = false;
                 for(int z = 0; z < attrIndexes.size(); z++){
                     if(inst.value(att) == attrIndexes.get(z) ){
                         isAttribValue = true;
                         z = attrIndexes.size();
                     }
                 }
                 if(isAttribValue)
                     splitData[0].add(inst);
                 else
                     splitData[1].add(inst);
 
             }
             if(checkMinLeafSize(splitData)){
                 //evaluate the instances with gainratio/ giniindex.
                 switch (m_SplitMethod) {
                     case 0: //GainRatio
                         double gainRatio = 0.0;
                         double infoGain = 0;
                         try {
                             infoGain = computeInfoGain(data, splitData);
                         } catch (Exception e) {
                             e.printStackTrace();
                         }
                         double splitInfo = computeSplitInfo(data, splitData);
                         if (splitInfo != 0)
                             gainRatio = infoGain / splitInfo;
                         //compare
                         if (gainRatio >= bestEvaluationValue) {
                             bestEvaluationValue = gainRatio;
                             bestSplitt = splitData;
                             indexBestAttribute = (ArrayList<Integer>) attrIndexes.clone();
                         }
                         break;
                     case 1: //GiniIndex
                         double gini = computeGiniIndex(data, splitData);
                        if (gini >= bestEvaluationValue) {
                             bestEvaluationValue = gini;
                             bestSplitt = splitData;
                             indexBestAttribute = (ArrayList<Integer>) attrIndexes.clone();
                         }
                         break;
                 }
             }
 
         }
         if(possibleSplits.isEmpty()){
             return null;
         }else{
             try {
                 splitValues = new double[indexBestAttribute.size()];
             } catch(Exception e) {
                 e.printStackTrace();
             }
             for(int b  = 0; b < indexBestAttribute.size(); b++){
                 splitValues[b] = (double) indexBestAttribute.get(b);
             }
             return bestSplitt;
         }
     }
 
     private void constructBinaryCode(ArrayList<String> list, String grayCode, int nBits, String Flag1, String Flag2, Boolean terminateFlag){
         if(terminateFlag)
             return;
 
         String s1;
         String s2;
         if(nBits > 0){
             s1 = grayCode + Flag1;
             s2 = grayCode + Flag2;
 
             constructBinaryCode(list, s1, nBits-1, Flag1, Flag2, false);
             constructBinaryCode(list, s2, nBits-1, Flag2, Flag1, false);
         }else{
             if (grayCode.charAt(0) != '1') {
                 Boolean onlyContainsZero = true;
                 for(int x = 0; x < grayCode.length(); x++){
                     if(grayCode.charAt(x) == '1'){
                         onlyContainsZero = false;
                     }
                 }
                 if(!onlyContainsZero){
                     list.add(grayCode);
                 }
             }
         }
     }
 
     public ArrayList<String> getBinaryPossibilitiesCode(int nBits){
         String grayCode;
         grayCode = "";
         String Flag1 = "0";
         String Flag2 = "1";
         ArrayList<String> list = new ArrayList<String>();
         constructBinaryCode(list, grayCode, nBits, Flag1, Flag2, false);
         return list;
     }
 
     /**
      * @param data the data that is to be split
      * @param att the attribute to be used for splitting
      * @return Best split produced
      */
     private Instances[] binarySplitDataNumeric(Instances data, Attribute att) {
         List<Instances[]> permutations = new ArrayList<Instances[]>();
         List<Double> condList = new ArrayList<Double>();
         double[] values;
         int numOfDistinctValues = data.numDistinctValues(att);
         splitValues = new double[m_NumberOfSplits];
 
         // Put all distinct values in arraylist
         List<Double> numValues = new ArrayList<Double>();
         Enumeration instEnum = data.enumerateInstances();
         while(instEnum.hasMoreElements()) {
             Instance inst = (Instance)instEnum.nextElement();
 
             for (int i = 0; i < inst.numValues(); i++) {
                 if(!numValues.contains(inst.value(att)))
                     numValues.add(inst.value(att));
             }
         }
         Collections.sort(numValues);
 
         // If not more than one distinct value
         if (numOfDistinctValues <= 1) {
             Instances[] binarySplit = new Instances[2];
             binarySplit[0] = data;
             binarySplit[1] = new Instances(data, data.numInstances());
             splitValues[0] = numValues.get(0);
             return binarySplit;
         }
 
         // If more than one distinct value, create a list of all possible splits
         for (int i = 0; i < numOfDistinctValues; i++) {
             Instances[] binarySplit = new Instances[2];
             binarySplit[0] = new Instances(data, data.numInstances());
             binarySplit[1] = new Instances(data, data.numInstances());
 
             Double value = numValues.get(i);
             instEnum = data.enumerateInstances();
             while(instEnum.hasMoreElements()) {
                 Instance inst = (Instance) instEnum.nextElement();
 
                 Double value2 = inst.value(att);
                 if(value2 <= value)
                     binarySplit[0].add(inst);
                 else
                     binarySplit[1].add(inst);
             }
 
             if(checkMinLeafSize(binarySplit)) {
                 condList.add(value);
                 permutations.add(binarySplit);
             }
         }
 
         if(permutations.isEmpty()) {
             return null;
         }
 
         // If only one permutation
         if(permutations.size() == 1) {
             splitValues[0] = condList.get(0);
             return permutations.get(0);
         }
 
         // Run gini/gain on all
         values = new double[permutations.size()];
         for (int i = 0; i < permutations.size(); i++) {
             if(m_SplitMethod == 1)
                 values[i] = computeGiniIndex(mergeSplit(permutations.get(i)), permutations.get(i));
             else {
                 double infoGain = 0;
                 try {
                     infoGain = computeInfoGain(mergeSplit(permutations.get(i)), permutations.get(i));
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
                 double splitInfo = computeSplitInfo(mergeSplit(permutations.get(i)), permutations.get(i));
 
                 if(splitInfo > 0)
                     values[i] = infoGain / splitInfo;
                 else
                     values[i] = 0;
             }
         }
 
         // Get best split based on splitmethod
         if(m_SplitMethod == 1) {
             splitValues[0] = condList.get(Utils.minIndex(values));
            return permutations.get(Utils.maxIndex(values));
         }
         else {
             splitValues[0] = condList.get(Utils.maxIndex(values));
             return permutations.get(Utils.maxIndex(values));
         }
     }
 
     private Instances mergeSplit(Instances[] splitData) {
         Instances data = new Instances(splitData[0], 0);
 
         for(Instances inst : splitData) {
             Enumeration instEnum = inst.enumerateInstances();
             while(instEnum.hasMoreElements())
                 data.add((Instance)instEnum.nextElement());
         }
 
         return data;
     }
 
 
     /**
      * Splits a dataset according to the values of a nominal attribute.
      *
      * @param data the data which is to be split
      * @param att the attribute to be used for splitting
      * @return the sets of instances produced by the split
      */
     private Instances[] splitData(Instances data, Attribute att) {
         if(att.isNominal())
             return splitDataNominal(data, att);
         else if(att.isNumeric())
             return splitDataNumeric(data, att);
         else
             return null;
     }
 
     /**
      * @param data the data which is to be split
      * @param att the attribute to be used for splitting
      * @return the sets of instances produced by the split
      */
     private Instances[] splitDataNominal(Instances data, Attribute att) {
         // TODO: Not static
         m_NumberOfSplits = 7;
         splitValues = new double[m_NumberOfSplits];
 
         Instances[] splitData = new Instances[att.numValues()];
         for (int j = 0; j < att.numValues(); j++) {
             splitData[j] = new Instances(data, data.numInstances());
         }
 
         Enumeration instEnum = data.enumerateInstances();
         while (instEnum.hasMoreElements()) {
             Instance inst = (Instance) instEnum.nextElement();
             splitData[(int) inst.value(att)].add(inst);
         }
 
         for (Instances aSplitData : splitData) {
             aSplitData.compactify();
         }
         return splitData;
     }
 
     /**
      * @param data the data which is to be split
      * @param att the attribute to be used for splitting
      * @return the sets of instances produced by the split
      */
     private Instances[] splitDataNumeric(Instances data, Attribute att) {
         double maxValue = Double.NEGATIVE_INFINITY, minValue = Double.POSITIVE_INFINITY;
         int numberOfSplitInterval;
         numberOfSplitInterval = getSplitInterval(data.numInstances());
 
         Instance inst;
         splitValues = new double[numberOfSplitInterval];
         //printDebugMessage(String.format("m_NumberOfSplits: %d", m_NumberOfSplits));
 
         printDebugMessage("startloop");
 
         Instances[] splitData = new Instances[numberOfSplitInterval];
         for (int i = 0; i < splitData.length; i++) {
             splitData[i] = data.stringFreeStructure();
         }
         printDebugMessage("slutloop");
 
         // Get min-max values from instances
         Enumeration instEnum = data.enumerateInstances();
         while(instEnum.hasMoreElements()) {
             inst = (Instance) instEnum.nextElement();
             double value = inst.value(att);
 
             if(maxValue < value)
                 maxValue = value;
             if(minValue > value)
                 minValue = value;
         }
 
         double diff = maxValue - minValue;
         double splitValue = diff / numberOfSplitInterval;
 
         // Set distribution
         double value = minValue + splitValue;
         for(int i = 0; i < numberOfSplitInterval; i++) {
             splitValues[i] = value;
             value += splitValue;
         }
 
         // Adding instances to new split attribute
         instEnum = data.enumerateInstances();
         while (instEnum.hasMoreElements()) {
             inst = (Instance) instEnum.nextElement();
 
             int index = -1;
             if(!inst.hasMissingValue()) {
                 // Inst. intervall is between min-max
                 double bound = minValue + splitValue;
 
                 for(int i = 0; i < numberOfSplitInterval; i++) {
                     if(bound < inst.value(att))
                         bound += splitValue;
                     else {
                         index = i;
                         i = numberOfSplitInterval;
                     }
 
                     if(i == numberOfSplitInterval-1)
                         index = i;
                 }
                 //System.out.println(index);
                 splitData[index].add(inst);
             }
 
 
 
 
         }
 
         for (Instances aSplitData : splitData) {
             aSplitData.compactify();
         }
         printDebugMessage("Number of edges: " + splitData.length);
         printDebugMessage("Best split:");
 
         if(m_Debug)
         {
             for(int i = 0; i < splitData.length; i++){
                 if(splitData.length > i && att.index() < splitData[i].numAttributes() && att.index() < splitData[i].numInstances()) {
                     printDebugMessage(String.format("%d: %s = %s", i + 1, splitData[i].attribute(att.index()).toString(), splitData[i].instance(att.index()).toString()));
                     printDebugMessage(String.format("Number of Instances: %s", Integer.toString(splitData[i].numInstances())));
                 }
             }
         }
         return splitData;
     }
 
     private int getSplitInterval(int N) {
         int k = (int)Utils.log2(N);
         if(k < 2)
             k = 2;
         printDebugMessage(String.format("%d", m_NumberOfSplits));
         return k;
     }
 
     /**
      * Returns an enumeration describing the available options.
      * <pre> -M &lt;minimum number of instances&gt;
      *  Set minimum number of instances per leaf.
      *  (default 2)</pre>
      * 
      * <pre> -B
      *  Use binary splits only.</pre>
      *
      * @return an enumeration of all the available options.
      */
     @SuppressWarnings("unchecked")
     public Enumeration listOptions() {
         Vector<Option> newVector = new Vector<Option>();
 
         newVector.addElement(new Option(
                 MinimumLeafSizeTipText(),
                 "M", 1, "-M < default 2 >"));
         newVector.addElement(new Option(
                 UseBinarySplitsTipText(),
                 "B", 0, "-B"));
         newVector.addElement(new Option(
                 SplitMethodTipText(),
                 "S", 1,
                 "-S < spliting method, [GainRatio, GiniIndex] >"));
         Enumeration<Option> enu = super.listOptions();
         while (enu.hasMoreElements()) {
             newVector.addElement(enu.nextElement());
         }
 
         return newVector.elements();
     }
 
     /**
      * Parses a given list of options. Valid options are:<p>
      *
      * -D  <br>
      * If set, classifier is run in debug mode and
      * may output additional info to the console.<p>
      *
      * @param options the list of options as an array of strings
      * @exception Exception if an option is not supported
      */
     public void setOptions(String[] options) throws Exception {
         String tmp = Utils.getOption("M", options);
         if (tmp.length() != 0) {
             setMinimumLeafSize(Integer.parseInt(tmp));
         }
 
         setUseBinarySplits(Utils.getFlag("B", options));
 
         tmp = Utils.getOption("S", options);
         if (tmp.length() != 0) {
             setSplitMethod(
                     new SelectedTag(Integer.parseInt(tmp), TAGS_SplitMethod));
         } else {
             setSplitMethod(
                     new SelectedTag(m_SplitMethod, TAGS_SplitMethod));
         }
     }
 
     /**
      * Gets the current settings of the Classifier.
      *
      * @return an array of strings suitable for passing to setOptions
      */
     public String[] getOptions() {
         Vector<String> result = new Vector<String>();
         String[] options;
 
         result.add("-M");
         result.add("" + getMinimumLeafSize());
         if (getUseBinarySplits()) {
             result.add("-B");
         }
         result.add("-S");
         result.add("" + getSplitMethod());
         options = super.getOptions();
         Collections.addAll(result, options);
 
         String[] array = new String[result.size()];
         for (int i = 0; i < result.size(); i++) {
             array[i] = result.elementAt(i);
         }
 
         return array;
     }
 
     /**
      * Returns the tip text for this property
      * @return tip text for this property suitable for
      * displaying in the explorer/experimenter gui
      */
     public String MinimumLeafSizeTipText() {
         return "Set minimum number of instances per leaf.";
     }
 
     /**
      * Get the value of this property.
      *
      * @return Value of this property.
      */
     public double getMinimumLeafSize() {
         return m_MinimumLeafSize;
     }
 
     /**
      * Set the value of this property.
      *
      * @param value Value to assign to this property.
      */
     public void setMinimumLeafSize(double value) {
         m_MinimumLeafSize = value;
     }
 
     /**
      * Returns the tip text for this property
      * @return tip text for this property suitable for
      * displaying in the explorer/experimenter gui
      */
     public String UseBinarySplitsTipText() {
         return "Use binary or multiple splits";
     }
 
     /**
      * Get the value of this property.
      *
      * @return Value of this property.
      */
     public boolean getUseBinarySplits() {
         return m_UseBinarySplits;
     }
 
     /**
      * Set the value of this property.
      *
      * @param value Value to assign to this property.
      */
     public void setUseBinarySplits(boolean value) {
         m_UseBinarySplits = value;
     }
 
     /**
      * Returns the tip text for this property
      * @return tip text for this property suitable for
      * displaying in the explorer/experimenter gui
      */
     public String SplitMethodTipText() {
         return "Select the method used to determine best split (GainRatio / GiniIndex)";
     }
 
     /**
      * Get the value of this property.
      *
      * @return Value of this property.
      */
     public SelectedTag getSplitMethod() {
         return new SelectedTag(m_SplitMethod, TAGS_SplitMethod);
     }
 
     /**
      * Set the value of this property.
      *
      * @param value Value to assign to this property.
      */
     public void setSplitMethod(SelectedTag value) {
         if (value.getTags() == TAGS_SplitMethod)
             m_SplitMethod = value.getSelectedTag().getID();
     }
 
     /**
      * Prints the decision tree using the private toString method from below.
      *
      * @return a textual description of the classifier
      */
     public String toString() {
         if ((m_Distribution == null) && (m_Successors == null))
             return "Grupp3Labb1: No model built yet.";
 
         return String.format("Grupp3Labb1\n------------------\n%s\n\nSize of the tree: %d\n\nNumber of leaves: %d",
                 toString(0), (int)measureTreeSize(), (int)measureNumLeaves());
     }
 
     /**
      * Outputs a tree at a certain level.
      *
      * @param level the level at which the tree is to be printed
      * @return the tree as string at the given level
      */
     private String toString(int level) {
         StringBuilder text = new StringBuilder();
 
         if (m_Attribute == null) { // isLeaf
             if (Utils.isMissingValue(m_ClassValue))
                 text.append(": null");
             else
                 text.append(": ").append(toStringLeafInfo());
         } else {
             for (int i = 0; i < m_Successors.length; i++) {
                 text.append("\n");
                 for (int j = 0; j < level; j++)
                     text.append("|  ");
 
                 toStringSplitInfo(text, i);
 
                 if(m_Successors[i] != null)
                     text.append(m_Successors[i].toString(level + 1));
             }
         }
         return text.toString();
     }
 
     /**
      * Prints info about the split
      *
      * @param text previous tree info
      * @param i current successor
      */
     private void toStringSplitInfo(StringBuilder text, int i) {
         if(m_UseBinarySplits) {
             if(m_Attribute.isNumeric()) {
                 text.append(m_Attribute.name());
                 if(i == 0)
                     text.append(" <= ");
                 else
                     text.append(" > ");
 
                 if(splitValues.length > 0) {
                     try {
                         text.append(Utils.doubleToString(splitValues[0], 6)); // + Utils.doubleToString(m_splitPoint,6))
                     } catch(Exception e) {
                         e.printStackTrace();
                     }
                 }
             } else {
                 text.append(m_Attribute.name()).append(" = ").append(m_Attribute.value(i));
             }
         } else {
             if(m_Attribute.isNumeric()) {
                 text.append("Condition: ");
                 if (i == 0) {
                     for (int j = 0; j < m_NumberOfSplits; j++) {
                         text.append( m_Attribute.name()).append(" = ").append(m_Attribute.value(j));
                         if (j + 1 != m_NumberOfSplits) {
                             text.append(" or ");
                         }
                     }
                 } else {
                     for (int k = m_NumberOfSplits; k < m_Attribute.numValues(); k++) {
                         text.append( m_Attribute.name()).append(" = ").append(m_Attribute.value(k));
                         if (k + 1 != m_Attribute.numValues()) {
                             text.append(" or ");
                         }
                     }
                 }
             } else {
                 text.append(m_Attribute.name()).append(" = ").append(m_Attribute.value(i));
             }
         }
     }
 
     /**
      * Print info about the leaf
      *
      * @return leaf info
      */
     public String toStringLeafInfo() {
         double sum = 0, error = 0;
         StringBuilder text = new StringBuilder();
         text.append(m_Data.classAttribute().value((int) m_ClassValue));
 
         Enumeration instances = m_Data.enumerateInstances();
         while(instances.hasMoreElements()) {
             Instance inst = (Instance) instances.nextElement();
             sum++;
             printDebugMessage("\nInstances ca: "+ inst.value(inst.classAttribute()));
             printDebugMessage("Correct ca: "+ m_ClassValue);
 
             if(inst.value(inst.classAttribute()) != m_ClassValue)
                 error++;
         }
 
         String info;
         if(error == 0)
             info = " (" + sum + ")";
         else
             info = " (" + sum + "/" + error + ")";
         text.append(info);
 
         return text.toString();
     }
 
     /**
      * Adds this tree recursively to the buffer.
      *
      * @param id          the unqiue id for the method
      * @param buffer      the buffer to add the source code to
      * @return            the last ID being used
      * @throws Exception  if something goes wrong
      */
     protected int toSource(int id, StringBuffer buffer) throws Exception {
         int result, i, newID;
         StringBuffer[] subBuffers;
 
         buffer.append("\n").append("  protected static double node").append(id).append("(Object[] i) {\n");
 
         if (m_Attribute == null) { // isLeaf?
             result = id;
             if (Double.isNaN(m_ClassValue))
                 buffer.append("    return Double.NaN;");
             else
                 buffer.append("    return ").append(m_ClassValue).append(";");
 
             if (m_ClassAttribute != null)
                 buffer.append(" // ").append(m_ClassAttribute.value((int) m_ClassValue));
 
             buffer.append("\n").append("  }\n");
         } else {
             buffer.append("    // ").append(m_Attribute.name()).append("\n");
 
             // subtree calls
             subBuffers = new StringBuffer[m_Attribute.numValues()];
             newID = id;
             for (i = 0; i < m_Attribute.numValues(); i++) {
                 newID++;
 
                 buffer.append("    ");
                 if (i > 0)
                     buffer.append("else ");
 
                 buffer.append("if (((String) i[").append(m_Attribute.index()).append("]).equals(\"").append(m_Attribute.value(i)).append("\"))\n");
                 buffer.append("      return node").append(newID).append("(i);\n");
 
                 subBuffers[i] = new StringBuffer();
                 newID = m_Successors[i].toSource(newID, subBuffers[i]);
             }
             buffer.append("    else\n");
             buffer.append("      throw new IllegalArgumentException(\"Value '\" + i[").append(m_Attribute.index()).append("] + \"' is not allowed!\");\n");
             buffer.append("  }\n");
 
             // output subtree code
             for (i = 0; i < m_Attribute.numValues(); i++)
                 buffer.append(subBuffers[i].toString());
 
             result = newID;
         }
 
         return result;
     }
 
     /**
      * Returns a string that describes the classifier as source. The
      * classifier will be contained in a class with the given name (there may
      * be auxiliary classes),
      * and will contain a method with the signature:
      * <pre><code>
      * public static double classify(Object[] i);
      * </code></pre>
      * where the array <code>i</code> contains elements that are either
      * Double, String, with missing values represented as null. The generated
      * code is public domain and comes with no warranty. <br/>
      * Note: works only if class attribute is the last attribute in the dataset.
      *
      * @param className the name that should be given to the source class.
      * @return the object source described by a string
      * @throws Exception if the source can't be computed
      */
     public String toSource(String className) throws Exception {
         StringBuffer result;
         int id;
 
         result = new StringBuffer();
 
         result.append("class ").append(className).append(" {\n");
         result.append("  public static double classify(Object[] i) {\n");
         id = 0;
         result.append("    return node").append(id).append("(i);\n");
         result.append("  }\n");
         toSource(id, result);
         result.append("}\n");
 
         return result.toString();
     }
 
     /**
      * Print message if debug
      * @param msg the message to be printed
      */
     private void printDebugMessage(String msg) {
         if (m_Debug)
             System.out.println(msg);
     }
 
     /**
      * Returns the revision string.
      *
      * @return		the revision
      */
     public String getRevision() {
         return RevisionUtils.extract("$Revision: 1 $");
     }
 
     /**
      * Main method.
      *
      * @param args the options for the classifier
      */
     public static void main(String[] args) {
         runClassifier(new Grupp3Labb1(), args);
     }
 
     /**
      * Returns an enumeration of the additional measure names
      * @return an enumeration of the measure names
      */
     public Enumeration enumerateMeasures() {
         Vector<String> newVector = new Vector<String>(3);
         newVector.addElement("measureTreeSize");
         newVector.addElement("measureNumLeaves");
         newVector.addElement("measureNumRules");
         return newVector.elements();
     }
 
     /**
      * Returns the value of the named measure
      * @param additionalMeasureName the name of the measure to query for its value
      * @return the value of the named measure
      * @throws IllegalArgumentException if the named measure is not supported
      */
     public double getMeasure(String additionalMeasureName) {
         if (additionalMeasureName.compareToIgnoreCase("measureNumRules") == 0)
             return measureNumRules();
         else if (additionalMeasureName.compareToIgnoreCase("measureTreeSize") == 0)
             return measureTreeSize();
         else if (additionalMeasureName.compareToIgnoreCase("measureNumLeaves") == 0)
             return measureNumLeaves();
         else
             throw new IllegalArgumentException(additionalMeasureName + " not supported (ID3)");
     }
 
     /**
      * @return the number of leaves
      */
     private double measureNumLeaves() {
         double num = 0;
 
         if(isLeaf())
             return 1;
 
         for(Grupp3Labb1 suc : m_Successors) {
             if(suc == null)
                 num++;
             else
                 num += suc.measureNumLeaves();
         }
 
         return num;
     }
 
     /**
      * @return the number of rules
      */
     private double measureNumRules() {
         double num = 1;
 
         if(isLeaf())
             return 0;
 
         for(Grupp3Labb1 suc : m_Successors) {
             if(suc == null)
                 num++;
             else
                 num += suc.measureNumRules();
         }
 
         return num;
     }
 
     /**
      * @return the size of the tree
      */
     private double measureTreeSize() {
         double num = 1;
 
         if(isLeaf())
             return 1;
 
         for(Grupp3Labb1 suc : m_Successors) {
             if(suc == null)
                 num++;
             else
                 num += suc.measureTreeSize();
         }
 
         return num;
     }
 
     /**
      * @return return true if node is a leaf
      */
     private boolean isLeaf() {
         return(m_Successors == null || m_Successors.length == 0);
     }
 }
