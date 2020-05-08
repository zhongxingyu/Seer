 package com.thomasdimson.wikipedia.lda.java;
 
 import cc.mallet.pipe.*;
 import cc.mallet.topics.ParallelTopicModel;
 import cc.mallet.types.Instance;
 import cc.mallet.types.InstanceList;
 import com.google.common.base.Joiner;
 import com.google.common.collect.Lists;
 import com.thomasdimson.wikipedia.Data;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.regex.Pattern;
 
 public class LDAModel {
     public static final Pipe INSTANCE_PIPE;
 
     static {
         List<Pipe> pipeList = Lists.newArrayList();
         Pattern splitPattern = Pattern.compile("[^ ]+");
         pipeList.add(new CharSequence2TokenSequence(splitPattern));
         pipeList.add(new TokenSequence2FeatureSequence());
         INSTANCE_PIPE = new SerialPipes(pipeList);
     }
 
     public static Instance malletInstanceFromDumpPage(Data.DumpPage page) {
         String target = Double.toString(page.getId());
         return new Instance(page.getText(), target, page.getTitle(), page.getTitle());
     }
 
     public static ParallelTopicModel initializeTopicModel(String filename, int numTopics) throws IOException {
         InstanceList instanceList = new InstanceList(INSTANCE_PIPE);
         Iterator<Data.DumpPage> it = WikipediaHandler.newStructuredDumpIterator(filename);
         System.err.println("Loading data");
         while(it.hasNext()) {
             Data.DumpPage p = it.next();
             instanceList.addThruPipe(malletInstanceFromDumpPage(p));
         }
 
         ParallelTopicModel model = new ParallelTopicModel(numTopics, 0.01 * numTopics, ParallelTopicModel.DEFAULT_BETA);
         model.addInstances(instanceList);
         model.setNumIterations(1000);
         model.setNumThreads(Runtime.getRuntime().availableProcessors());
         return model;
     }
 
     public static ParallelTopicModel createFromStructuredDump(String filename, int numTopics, String modelFilePrefix) throws IOException {
         ParallelTopicModel model = initializeTopicModel(filename, numTopics);
         if(modelFilePrefix != null) {
             model.setSaveSerializedModel(250, modelFilePrefix);
         }
         System.err.println("Done! Starting estimation");
         model.estimate();
 
         System.err.println("Topics:\n");
         Object[][] topicWords = model.getTopWords(50);
         for(int i = 0; i < topicWords.length; i++) {
             System.err.println("\tTopic" + i + ": " + Joiner.on(", ").join(topicWords[i]));
         }
         return model;
     }
 
    public static void writeFromModel(String dataFile, int numTopics, String stateFile, String documentTopicFile, String wordFile) throws Exception {
        ParallelTopicModel model = ParallelTopicModel.read(new File(dataFile));
         System.err.println("Initializing from state");
         model.initializeFromState(new File(stateFile));
         System.err.println("Writing document topics");
         model.printDocumentTopics(new File(documentTopicFile));
         System.err.println("Writing topic words");
         model.printTopicWordWeights(new File(wordFile));
     }
 }
