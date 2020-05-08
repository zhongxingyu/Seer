 package aima.core.learning.learners;
 
 import aima.core.learning.framework.Attribute;
 import aima.core.learning.framework.DataSet;
 import aima.core.learning.framework.Example;
 import aima.core.learning.framework.Learner;
 import aima.core.learning.inductive.DecisionTree;
 import aima.core.learning.inductive.DecisionTreeLeaf;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * Uses DECISION-TREE-LEARNING from page 702, AIMAv3, to induce decision trees
  * (see DecisionTree).
  *
  * @author Ravi Mohan
  * @author Mike Stampone
  * @author Andrew Brown
  */
 public class DecisionTreeLearner implements Learner {
 
     private DecisionTree trainedTree;
 
     /**
      * Constructor
      */
     public DecisionTreeLearner() {
     }
 
     /**
      * Constructor: used when testing a non-induced tree (i.e. for testing)
      *
      * @param tree
      */
     public DecisionTreeLearner(DecisionTree tree) {
         this.trainedTree = tree;
     }
 
     /**
      * Return the decision tree
      *
      * @return
      */
     public DecisionTree getDecisionTree() {
         return trainedTree;
     }
 
     /**
      * Implements DECISION-TREE-LEARNING function from page 702 of AIMAv3:
      *
      * <pre><code>
      * function DECISION-TREE-LEARNING(examples, attributes, parent_examples) returns a tree
      *  if examples is empty then return PLURALITY-VALUE(parent_examples)
      *  else if all examples have the same classification then return the classification
      *  else if attributes is empty then return PLURALITY-VALUE(examples)
      *  else
      *      A = argmax_(a|attributes) IMPORTANCE(a, examples)
      *      tree = a new decision tree with root test A
      *      for each value v_k of A do
      *          exs = {e : e|examples and e.A = v_k}
      *          subtree = DECISION-TREE-LEARNING(exs, attributes - A, examples)
      *          add a branch to tree with label (A = v_k) and subtree subtree
      *      return tree
      * </code></pre>
      *
      * @param ds
      * @param attributeNames
      * @param defaultTree
      * @return
      */
     private DecisionTree decisionTreeLearning(DataSet examples, List<String> attributeNames, DataSet parent_examples) {
         if (examples.size() == 0) {
             return new DecisionTreeLeaf(this.getPluralityValue(parent_examples));
         }
         if (this.allExamplesHaveSameClassification(examples)) {
             return new DecisionTreeLeaf(examples.getExample(0).getOutput());
         }
         if (attributeNames.isEmpty()) {
             return new DecisionTreeLeaf(this.getPluralityValue(examples));
         }
         // A <- argmax_(a in attributes) IMPORTANCE(a, examples)
         Attribute A = new Attribute(this.getMostImportantAttribute(attributeNames, examples), null);
         DecisionTree tree = new DecisionTree(A);
         for (Object value : examples.getValuesOf(A.getName())) {
             DataSet exs = examples.find(A.getName(), value);
             attributeNames.remove(A.getName()); // attributes - A
             DecisionTree subtree = this.decisionTreeLearning(exs, attributeNames, examples);
            tree.addNode(value, subtree);
         }
         return tree;
     }
 
     /**
      * Return plurality classification, "the most common output value among a
      * set of examples, breaking ties randomly" page 702, AIMAv3. Uses
      * MajorityLearner
      *
      * @param examples
      * @return
      */
     private DecisionTreeLeaf getPluralityValue(DataSet examples) {
         Learner learner = new MajorityLearner();
         learner.train(examples);
         return new DecisionTreeLeaf(learner.predict(null));
     }
 
     /**
      * Find the most important attribute; this method corresponds to "argmax_(a
      * in attributes) IMPORTANCE(a, examples)" on page 702, AIMAv3. IMPORTANCE
      * is implemented as the information gain of the attribute, see page 703.
      *
      * @param examples
      * @param attributeNames
      * @return
      */
     private String getMostImportantAttribute(List<String> attributeNames, DataSet examples) {
         double greatestGain = 0.0;
         String attributeWithGreatestGain = attributeNames.get(0);
         for (String attributeName : attributeNames) {
             // IMPORTANCE(a, examples) is implemented as the information gain of the attribute, page 703
             double gain = examples.getInformationGainOf(attributeName);
             if (gain > greatestGain) {
                 greatestGain = gain;
                 attributeWithGreatestGain = attributeName;
             }
         }
         return attributeWithGreatestGain;
     }
 
     /**
      * Test the example set for classification; if all examples have the same
      * output, return true.
      *
      * @param examples
      * @return
      */
     private boolean allExamplesHaveSameClassification(DataSet examples) {
         Object classification = examples.getExample(0).getOutput();
         Iterator<Example> iter = examples.iterator();
         while (iter.hasNext()) {
             Example element = iter.next();
             if (!element.getOutput().equals(classification)) {
                 return false;
             }
 
         }
         return true;
     }
 
     /**
      * Induces the decision tree from the specified set of examples
      *
      * @param examples a set of examples for constructing the decision tree
      */
     @Override
     public void train(DataSet examples) {
         // get attribute names
         ArrayList<String> attributes = new ArrayList<String>();
         for (Attribute a : examples.getExample(0).getAttributes()) {
             attributes.add(a.getName());
         }
         // train
         this.trainedTree = decisionTreeLearning(examples, attributes, null);
     }
 
     /**
      * Predict a result using the trained decision tree
      *
      * @param e
      * @return
      */
     @Override
     public <T> T predict(Example e) {
         if (this.trainedTree == null) {
             throw new RuntimeException("DecisionTreeLearner has not yet been trained with an example set.");
         }
         return (T) this.trainedTree.predict(e);
     }
 
     /**
     * Returns the accuracy of the decision tree on the specified set of
      * examples
      *
      * @param examples
      * @return
      */
     @Override
     public int[] test(DataSet examples) {
         int[] results = new int[]{0, 0};
 
         for (Example e : examples) {
             if (e.getOutput().equals(this.trainedTree.predict(e))) {
                 results[0]++;
             } else {
                 results[1]++;
             }
         }
         return results;
     }
 }
