 package edu.stanford.cs224u.disentanglement.disentanglers;
 
 import com.google.common.base.Function;
 import com.google.common.base.Preconditions;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import edu.stanford.cs224u.disentanglement.classifier.DataBuilder;
 import edu.stanford.cs224u.disentanglement.features.MinuteDifferenceFeatureFactory;
import edu.stanford.cs224u.disentanglement.features.TfIdfFeatureFactory;
 import edu.stanford.cs224u.disentanglement.structures.*;
 import edu.stanford.cs224u.disentanglement.util.Benchmarker;
 import weka.classifiers.functions.SMO;
 import weka.core.Instance;
 
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
 enum MessagePairCategories {
     NOT_RELATED,
     RELATED,
 }
 
 public class SVMDisentangler implements Disentangler {
     private SMO classifier;
     private DataBuilder dataBuilder;
     private final int numFalseExamples = 1;
     private Random random;
 
     public SVMDisentangler() {
         random = new Random(1); // fixed seed for now
     }
 
     @Override
     public void train(Iterable<MessageTree> trainingData) {
         Benchmarker.push("Generate Vocabulary");
         List<MessageTree> train = Lists.newArrayList(trainingData);
         final List<String> sentences = Lists.newArrayList();
         for(MessageTree tree : train) {
             tree.getRoot().walk(new Function<MessageNode, Void>() {
                 @Override
                 public Void apply(MessageNode messageNode) {
                     sentences.add(messageNode.getMessage().getBody());
                     return null;
                 }
             });
         }
         Benchmarker.pop();
 
         Benchmarker.push("Create data builder");
 
         dataBuilder = new DataBuilder(MessagePairCategories.class, "SVMDisentangler",
            new TfIdfFeatureFactory(),
             new MinuteDifferenceFeatureFactory()
         );
         Benchmarker.pop();
 
         Benchmarker.push("Adding examples");
         for(MessageTree tree : trainingData) {
             List<Message> linearized = tree.linearize();
             DataBuilder.TreeData td = dataBuilder.createTreeData(linearized);
             for(MessagePair p : tree.extractEdges()) {
                 td.addExample(p, MessagePairCategories.RELATED);
                 int foundExamples = 0;
                 int iterations = 0;
                 while(iterations < 100 && foundExamples < numFalseExamples) {
                     iterations++;
                     Message example = linearized.get(random.nextInt(linearized.size()));
                     if(example.equals(p.getSecond()) || example.equals(p.getFirst())) {
                         continue;
                     }
                     MessagePair pReplace = new MessagePair(example, p.getSecond());
                     td.addExample(pReplace, MessagePairCategories.NOT_RELATED);
                     foundExamples++;
                 }
 
             }
         }
 
         classifier = new SMO();
         classifier.setBuildLogisticModels(true);
         classifier.setC(0.0001);
         try {
             Benchmarker.push("Build classifier");
             classifier.buildClassifier(dataBuilder.getInstances());
         } catch (Exception e) {
             Benchmarker.popError();
             throw new RuntimeException(e);
         }
     }
 
     @Override
     public MessageTree predict(List<Message> test) {
         Preconditions.checkArgument(test.size() > 0, "Tried to predict empty tree!");
 
         DataBuilder.TreeData td = dataBuilder.createTreeData(test);
 
         Map<Message, MessageNode> nodeForMessage = Maps.newHashMap();
         MessageNode root = new MessageNode(test.get(0));
         nodeForMessage.put(test.get(0), root);
 
         for(int i = 1; i < test.size(); i++) {
             Message m = test.get(i);
             MessageNode maxParent = null;
             double maxProb = Double.NEGATIVE_INFINITY;
             for(int p = 0; p < i; p++) {
                 MessageNode parentCandidate = nodeForMessage.get(test.get(p));
                 MessagePair candidatePair = MessagePair.of(parentCandidate.getMessage(), m);
                 Instance instance = td.buildClassificationInstance(candidatePair);
                 instance.setDataset(dataBuilder.getInstances());
 
                 double []classProbs;
                 try {
                      classProbs = classifier.distributionForInstance(instance);
                 } catch(Exception e) {
                     throw new RuntimeException(e);
                 }
 
 
                 double prob = classProbs[1];
                 if(prob > maxProb) {
                     maxProb = prob;
                     maxParent = parentCandidate;
                 }
             }
             MessageNode mn = new MessageNode(m);
             maxParent.addChildren(mn);
             nodeForMessage.put(m, mn);
         }
 
         return new MessageTree(root, "Predicted reddit tree");
     }
 
 }
