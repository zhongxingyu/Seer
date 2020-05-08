 package org.apache.giraph.examples.compensations;
 
 import org.apache.giraph.examples.LongSumAggregator;
 import org.apache.giraph.examples.SumAggregator;
 import org.apache.giraph.graph.LongDoubleFloatDoubleVertex;
 import org.apache.hadoop.io.DoubleWritable;
 import org.apache.hadoop.io.LongWritable;
 
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.Random;
 
 public class CompensablePageRankVertex extends LongDoubleFloatDoubleVertex {
 
   static final String L1_DIFF = "l1Diff";
   static final String NON_FAILED_VERTICES = "nonFailedVertices";
   static final String NON_FAILED_RANK = "nonFailedRank";
   static final String DANGLING_RANK = "danglingRank";
 
   private static final double EPSILON = 0.0001;
 
   private static final long FAILING_ITERATION = 3;
   private static final int FAILING_PARTITION = 2;
   private static final double MESSAGE_LOSS = 0.5;
 
   private final Random random = new Random();
 
   @Override
   public void compute(Iterator<DoubleWritable> messages) throws IOException {
 
     double newRank;
 
     if (getSuperstep() == 0) {
       newRank = 1d / getNumVertices();
     } else {
 
       if (isIterationSubsequentToFailingIteration()) {
         compensate();
       }
 
       if (getSuperstep() > 1 && CompensablePageRankWorkerContext.AGGREGATED_L1_DIFF < EPSILON) {
         voteToHalt();
         return;
       }
 
       double summedRank = 0;
       while (messages.hasNext()) {
         summedRank += messages.next().get();
       }
       double danglingRankContribution = CompensablePageRankWorkerContext.DANGLING_RANK / getNumVertices();
 
       newRank = 0.85 * (summedRank + danglingRankContribution) + 0.15 / getNumVertices();
     }
 
     //System.out.println("SS [" + getSuperstep() + "], vertex [" + getVertexId().get() +"], rank [" + newRank + "]");
     //System.out.println("[WORKER_INFO]" + getWorkerContext().getWorkerInfo());
 
     SumAggregator danglingRank = (SumAggregator) getAggregator(CompensablePageRankVertex.DANGLING_RANK);
     SumAggregator l1Diff = (SumAggregator) getAggregator(CompensablePageRankVertex.L1_DIFF);
     LongSumAggregator nonFailedVertices =
         (LongSumAggregator) getAggregator(CompensablePageRankVertex.NON_FAILED_VERTICES);
     SumAggregator nonFailedRank = (SumAggregator) getAggregator(CompensablePageRankVertex.NON_FAILED_RANK);
 
     if (!(isFailingIteration() && inFailedPartition())) {
 
       if (getSuperstep() > 0) {
         double previousRank = getVertexValue().get();
         l1Diff.aggregate(Math.abs(newRank - previousRank));
       }
 
       nonFailedVertices.aggregate(1l);
       nonFailedRank.aggregate(newRank);
 
       if (isDangling()) {
         danglingRank.aggregate(newRank);
       }
     }
 
     DoubleWritable partialRank = new DoubleWritable(newRank / getNumOutEdges());
 
     for (LongWritable neighbor : this) {
       if (!(isFailingIteration() && inFailedPartition())) {
         sendMsg(neighbor, partialRank);
       } else {
         if (random.nextDouble() >= MESSAGE_LOSS) {
           sendMsg(neighbor, partialRank);
         }
       }
     }
 
     setVertexValue(new DoubleWritable(newRank));
   }
 
   private boolean isDangling() {
     return getNumOutEdges() == 0;
   }
 
   private void compensate() {
 
     long nonFailedVertices = CompensablePageRankWorkerContext.AGGREGATED_NON_FAILED_VERTICES;
     double nonFailedRank = CompensablePageRankWorkerContext.AGGREGATED_NON_FAILED_RANK;
 
     if (inFailedPartition()) {
       setVertexValue(new DoubleWritable(1d / getNumVertices()));
     } else {
       double rescaledValue = (nonFailedVertices * getVertexValue().get()) / (nonFailedRank * getNumVertices());
       setVertexValue(new DoubleWritable(rescaledValue));
     }
   }
 
   private boolean isIterationSubsequentToFailingIteration() {
     return getSuperstep() == FAILING_ITERATION + 1;
   }
 
   private boolean isFailingIteration() {
     return getSuperstep() == FAILING_ITERATION;
   }
 
   private boolean inFailedPartition() {
     return getWorkerContext().getWorkerInfo().getPartitionId() == FAILING_PARTITION;
   }
 
 
 }
