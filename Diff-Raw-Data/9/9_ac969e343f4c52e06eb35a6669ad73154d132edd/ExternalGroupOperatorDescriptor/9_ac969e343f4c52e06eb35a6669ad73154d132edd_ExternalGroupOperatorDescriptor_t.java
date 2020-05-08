 /*
  * Copyright 2009-2010 by The Regents of the University of California
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * you may obtain a copy of the License from
  * 
  *     http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package edu.uci.ics.hyracks.dataflow.std.group;
 
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.logging.Logger;
 
 import edu.uci.ics.hyracks.api.comm.IFrameTupleAccessor;
 import edu.uci.ics.hyracks.api.comm.IFrameWriter;
 import edu.uci.ics.hyracks.api.context.IHyracksStageletContext;
 import edu.uci.ics.hyracks.api.dataflow.IActivityGraphBuilder;
 import edu.uci.ics.hyracks.api.dataflow.IOperatorDescriptor;
 import edu.uci.ics.hyracks.api.dataflow.IOperatorNodePushable;
 import edu.uci.ics.hyracks.api.dataflow.value.IBinaryComparator;
 import edu.uci.ics.hyracks.api.dataflow.value.IBinaryComparatorFactory;
 import edu.uci.ics.hyracks.api.dataflow.value.INormalizedKeyComputerFactory;
 import edu.uci.ics.hyracks.api.dataflow.value.IRecordDescriptorProvider;
 import edu.uci.ics.hyracks.api.dataflow.value.RecordDescriptor;
 import edu.uci.ics.hyracks.api.exceptions.HyracksDataException;
 import edu.uci.ics.hyracks.api.io.FileReference;
 import edu.uci.ics.hyracks.api.job.IOperatorEnvironment;
 import edu.uci.ics.hyracks.api.job.JobSpecification;
 import edu.uci.ics.hyracks.dataflow.common.comm.io.ArrayTupleBuilder;
 import edu.uci.ics.hyracks.dataflow.common.comm.io.FrameTupleAccessor;
 import edu.uci.ics.hyracks.dataflow.common.comm.io.FrameTupleAppender;
 import edu.uci.ics.hyracks.dataflow.common.comm.util.FrameUtils;
 import edu.uci.ics.hyracks.dataflow.common.io.RunFileReader;
 import edu.uci.ics.hyracks.dataflow.common.io.RunFileWriter;
 import edu.uci.ics.hyracks.dataflow.std.aggregators.IAggregatorDescriptor;
 import edu.uci.ics.hyracks.dataflow.std.aggregators.IAggregatorDescriptorFactory;
 import edu.uci.ics.hyracks.dataflow.std.base.AbstractActivityNode;
 import edu.uci.ics.hyracks.dataflow.std.base.AbstractOperatorDescriptor;
 import edu.uci.ics.hyracks.dataflow.std.base.AbstractUnaryInputSinkOperatorNodePushable;
 import edu.uci.ics.hyracks.dataflow.std.base.AbstractUnaryOutputSourceOperatorNodePushable;
 import edu.uci.ics.hyracks.dataflow.std.util.ReferenceEntry;
 import edu.uci.ics.hyracks.dataflow.std.util.ReferencedPriorityQueue;
 
 /**
  * @author jarodwen
  */
 public class ExternalGroupOperatorDescriptor extends AbstractOperatorDescriptor {
 
     private static final long serialVersionUID = 1L;
     /**
      * XXX For debugging only. Remove this when deploying.
      */
     private static Logger LOGGER = Logger.getLogger(ExternalGroupOperatorDescriptor.class.getName());
     /**
      * The input frame identifier (in the job environment)
      */
     private static final String GROUPTABLES = "gtables";
     /**
      * The runs files identifier (in the job environment)
      */
     private static final String RUNS = "runs";
     /**
      * The fields used for grouping (grouping keys).
      */
     private final int[] keyFields;
     /**
      * The comparator for checking the grouping keys, corresponding to the
      * {@link #keyFields}.
      */
     private final IBinaryComparatorFactory[] comparatorFactories;
     private final INormalizedKeyComputerFactory firstNormalizerFactory;
     private final IAggregatorDescriptorFactory aggregatorFactory;
     private final IAggregatorDescriptorFactory mergeFactory;
     private final int framesLimit;
     private final ISpillableTableFactory spillableTableFactory;
     private final boolean isOutputSorted;
 
     public ExternalGroupOperatorDescriptor(JobSpecification spec, int[] keyFields, int framesLimit,
             IBinaryComparatorFactory[] comparatorFactories, INormalizedKeyComputerFactory firstNormalizerFactory,
             IAggregatorDescriptorFactory aggregatorFactory, IAggregatorDescriptorFactory mergeFactory,
             RecordDescriptor recordDescriptor, ISpillableTableFactory spillableTableFactory, boolean isOutputSorted) {
         super(spec, 1, 1);
         this.framesLimit = framesLimit;
         if (framesLimit <= 1) {
             // Minimum of 2 frames: 1 for input records, and 1 for output
             // aggregation results.
             throw new IllegalStateException("frame limit should at least be 2, but it is " + framesLimit + "!");
         }
 
         this.aggregatorFactory = aggregatorFactory;
         this.mergeFactory = mergeFactory;
         this.keyFields = keyFields;
         this.comparatorFactories = comparatorFactories;
         this.firstNormalizerFactory = firstNormalizerFactory;
         this.spillableTableFactory = spillableTableFactory;
         this.isOutputSorted = isOutputSorted;
 
         // Set the record descriptor. Note that since
         // this operator is a unary operator,
         // only the first record descriptor is used here.
         recordDescriptors[0] = recordDescriptor;
     }
 
     @Override
     public void contributeTaskGraph(IActivityGraphBuilder builder) {
         AggregateActivity aggregateAct = new AggregateActivity();
         MergeActivity mergeAct = new MergeActivity();
 
         builder.addTask(aggregateAct);
         builder.addSourceEdge(0, aggregateAct, 0);
 
         builder.addTask(mergeAct);
         builder.addTargetEdge(0, mergeAct, 0);
 
         builder.addBlockingEdge(aggregateAct, mergeAct);
     }
 
     private class AggregateActivity extends AbstractActivityNode {
 
         private static final long serialVersionUID = 1L;
 
         @Override
         public IOperatorDescriptor getOwner() {
             return ExternalGroupOperatorDescriptor.this;
         }
 
         @Override
         public IOperatorNodePushable createPushRuntime(final IHyracksStageletContext ctx,
                 final IOperatorEnvironment env, IRecordDescriptorProvider recordDescProvider, int partition,
                 int nPartitions) throws HyracksDataException {
             // Create the spillable table
             final ISpillableTable gTable = spillableTableFactory.buildSpillableTable(ctx, keyFields,
                    comparatorFactories, firstNormalizerFactory, aggregatorFactory,
                     recordDescProvider.getInputRecordDescriptor(getOperatorId(), 0), recordDescriptors[0],
                     ExternalGroupOperatorDescriptor.this.framesLimit);
             // Create the tuple accessor
             final FrameTupleAccessor accessor = new FrameTupleAccessor(ctx.getFrameSize(),
                     recordDescProvider.getInputRecordDescriptor(getOperatorId(), 0));
             // Create the partial aggregate activity node
             IOperatorNodePushable op = new AbstractUnaryInputSinkOperatorNodePushable() {
 
                 /**
                  * Run files
                  */
                 private LinkedList<RunFileReader> runs;
 
                 @Override
                 public void open() throws HyracksDataException {
                     runs = new LinkedList<RunFileReader>();
                     gTable.reset();
                 }
 
                 @Override
                 public void nextFrame(ByteBuffer buffer) throws HyracksDataException {
                     accessor.reset(buffer);
                     int tupleCount = accessor.getTupleCount();
                     for (int i = 0; i < tupleCount; i++) {
                         // If the group table is too large, flush the table into
                         // a run file.
                         if (!gTable.insert(accessor, i)) {
                             flushFramesToRun();
                             if (!gTable.insert(accessor, i))
                                 throw new HyracksDataException(
                                         "Failed to insert a new buffer into the aggregate operator!");
                         }
                     }
                 }
 
                 @Override
                 public void flush() throws HyracksDataException {
 
                 }
 
                 @Override
                 public void close() throws HyracksDataException {
                     if (gTable.getFrameCount() >= 0) {
                         if (runs.size() <= 0) {
                             // All in memory
                             env.set(GROUPTABLES, gTable);
                         } else {
                             // flush the memory into the run file.
                             flushFramesToRun();
                             gTable.close();
                         }
                     }
                     env.set(RUNS, runs);
                 }
 
                 private void flushFramesToRun() throws HyracksDataException {
                     FileReference runFile;
                     try {
                         runFile = ctx.getJobletContext().createWorkspaceFile(
                                 ExternalGroupOperatorDescriptor.class.getSimpleName());
                     } catch (IOException e) {
                         throw new HyracksDataException(e);
                     }
                     RunFileWriter writer = new RunFileWriter(runFile, ctx.getIOManager());
                     writer.open();
                     try {
                         gTable.sortFrames();
                         gTable.flushFrames(writer, true);
                     } catch (Exception ex) {
                         throw new HyracksDataException(ex);
                     } finally {
                         writer.close();
                     }
                     gTable.reset();
                     runs.add(((RunFileWriter) writer).createReader());
                 }
             };
             return op;
         }
     }
 
     private class MergeActivity extends AbstractActivityNode {
 
         private static final long serialVersionUID = 1L;
 
         @Override
         public IOperatorDescriptor getOwner() {
             return ExternalGroupOperatorDescriptor.this;
         }
 
         @Override
         public IOperatorNodePushable createPushRuntime(final IHyracksStageletContext ctx,
                 final IOperatorEnvironment env, IRecordDescriptorProvider recordDescProvider, int partition,
                 int nPartitions) throws HyracksDataException {
             final IBinaryComparator[] comparators = new IBinaryComparator[comparatorFactories.length];
             for (int i = 0; i < comparatorFactories.length; ++i) {
                 comparators[i] = comparatorFactories[i].createBinaryComparator();
             }
             final IAggregatorDescriptor currentWorkingAggregator = mergeFactory.createAggregator(ctx,
                     recordDescriptors[0], recordDescriptors[0], keyFields);
             final int[] storedKeys = new int[keyFields.length];
             // Get the list of the fields in the stored records.
             for (int i = 0; i < keyFields.length; ++i) {
                 storedKeys[i] = i;
             }
             // Tuple builder
             final ArrayTupleBuilder tupleBuilder = new ArrayTupleBuilder(recordDescriptors[0].getFields().length);
 
             IOperatorNodePushable op = new AbstractUnaryOutputSourceOperatorNodePushable() {
                 /**
                  * Input frames, one for each run file.
                  */
                 private List<ByteBuffer> inFrames;
 
                 /**
                  * Output frame.
                  */
                 private ByteBuffer outFrame, writerFrame;
 
                 /**
                  * List of the run files to be merged
                  */
                 private LinkedList<RunFileReader> runs;
 
                 /**
                  * Tuple appender for the output frame {@link #outFrame}.
                  */
                 private final FrameTupleAppender outFrameAppender = new FrameTupleAppender(ctx.getFrameSize());
                 private final FrameTupleAccessor outFrameAccessor = new FrameTupleAccessor(ctx.getFrameSize(),
                         recordDescriptors[0]);
                 private ArrayTupleBuilder finalTupleBuilder;
                 private FrameTupleAppender writerFrameAppender;
 
                 @SuppressWarnings("unchecked")
                 public void initialize() throws HyracksDataException {
                     runs = (LinkedList<RunFileReader>) env.get(RUNS);
                     writer.open();
                     try {
                         if (runs.size() <= 0) {
                             ISpillableTable gTable = (ISpillableTable) env.get(GROUPTABLES);
                             if (gTable != null) {
                                 if (isOutputSorted)
                                     gTable.sortFrames();
                                 gTable.flushFrames(writer, false);
                             }
                             env.set(GROUPTABLES, null);
                         } else {
                             inFrames = new ArrayList<ByteBuffer>();
                             outFrame = ctx.allocateFrame();
                             outFrameAppender.reset(outFrame, true);
                             outFrameAccessor.reset(outFrame);
                             while (runs.size() > 0) {
                                 try {
                                     doPass(runs);
                                 } catch (Exception e) {
                                     throw new HyracksDataException(e);
                                 }
                             }
                             inFrames.clear();
                         }
                     } finally {
                         writer.close();
                     }
                     env.set(RUNS, null);
                 }
 
                 private void doPass(LinkedList<RunFileReader> runs) throws HyracksDataException {
                     FileReference newRun = null;
                     IFrameWriter writer = this.writer;
                     boolean finalPass = false;
 
                     if (runs.size() + 2 <= framesLimit) {
                         // All in-frames can be fit into memory, so no run file
                         // will be produced and this will be the final pass.
                         finalPass = true;
                         // Remove the unnecessary frames.
                         while (inFrames.size() < runs.size()) {
                             inFrames.add(ctx.allocateFrame());
                         }
                     } else {
                         while (inFrames.size() + 2 < framesLimit) {
                             inFrames.add(ctx.allocateFrame());
                         }
                         // Files need to be merged.
                         newRun = ctx.getJobletContext().createWorkspaceFile(
                                 ExternalGroupOperatorDescriptor.class.getSimpleName());
                         writer = new RunFileWriter(newRun, ctx.getIOManager());
                         writer.open();
                     }
                     try {
                         // Create file readers for each input run file, only
                         // for the ones fit into the inFrames
                         RunFileReader[] runFileReaders = new RunFileReader[inFrames.size()];
                         // Create input frame accessor
                         FrameTupleAccessor[] tupleAccessors = new FrameTupleAccessor[inFrames.size()];
 
                         // Build a priority queue for extracting tuples in order
                         Comparator<ReferenceEntry> comparator = createEntryComparator(comparators);
 
                         ReferencedPriorityQueue topTuples = new ReferencedPriorityQueue(ctx.getFrameSize(),
                                 recordDescriptors[0], inFrames.size(), comparator);
                         // Maintain a list of visiting index for all inFrames
                         int[] tupleIndices = new int[inFrames.size()];
                         for (int i = 0; i < inFrames.size(); i++) {
                             tupleIndices[i] = 0;
                             // Get the run file index for this in-frame
                             // Note that empty entry in the queue will be
                             // the minimum one so it is always at the peek
                             int runIndex = topTuples.peek().getRunid();
                             // Load the run file
                             runFileReaders[runIndex] = runs.get(runIndex);
                             runFileReaders[runIndex].open();
                             // Load the first frame of the file into
                             // the inFrame
                             if (runFileReaders[runIndex].nextFrame(inFrames.get(runIndex))) {
                                 // Initialize the tuple accessor for this run
                                 // file
                                 tupleAccessors[runIndex] = new FrameTupleAccessor(ctx.getFrameSize(),
                                         recordDescriptors[0]);
                                 tupleAccessors[runIndex].reset(inFrames.get(runIndex));
                                 setNextTopTuple(runIndex, tupleIndices, runFileReaders, tupleAccessors, topTuples);
                             } else {
                                 closeRun(runIndex, runFileReaders, tupleAccessors);
                             }
                         }
 
                         // Start merging
                         while (!topTuples.areRunsExhausted()) {
                             // Get the top record
                             ReferenceEntry top = topTuples.peek();
                             int tupleIndex = top.getTupleIndex();
                             int runIndex = topTuples.peek().getRunid();
                             FrameTupleAccessor fta = top.getAccessor();
                             int currentTupleInOutFrame = outFrameAccessor.getTupleCount() - 1;
                             if (currentTupleInOutFrame < 0
                                     || compareFrameTuples(fta, tupleIndex, outFrameAccessor, currentTupleInOutFrame) != 0) {
                                 // Initialize the first output record
                                 // Reset the tuple builder
                                 flushOutFrame(writer, finalPass);
 
                                 tupleBuilder.reset();
                                 for (int i = 0; i < keyFields.length; i++) {
                                     tupleBuilder.addField(fta, tupleIndex, i);
                                 }
 
                                 currentWorkingAggregator.init(fta, tupleIndex, tupleBuilder);
                                 outFrameAppender.reset(outFrame, true);
                                 outFrameAppender.append(tupleBuilder.getFieldEndOffsets(), tupleBuilder.getByteArray(),
                                         0, tupleBuilder.getSize());
                             } else {
                                 // if new tuple is in the same group of the
                                 // current aggregator
                                 // do merge and output to the outFrame
                                 int tupleOffset = outFrameAccessor.getTupleStartOffset(currentTupleInOutFrame);
                                 int fieldOffset = outFrameAccessor.getFieldStartOffset(currentTupleInOutFrame,
                                         keyFields.length);
                                 int fieldLength = outFrameAccessor.getFieldLength(currentTupleInOutFrame,
                                         keyFields.length);
                                 currentWorkingAggregator.aggregate(fta, tupleIndex, outFrameAccessor.getBuffer()
                                        .array(), tupleOffset + outFrameAccessor.getFieldSlotsLength() + fieldOffset,
                                        fieldLength);
                             }
                             tupleIndices[runIndex]++;
                             setNextTopTuple(runIndex, tupleIndices, runFileReaders, tupleAccessors, topTuples);
                         }
                         // Flush the outFrame
                         if (outFrameAppender.getTupleCount() > 0) {
                             flushOutFrame(writer, finalPass);
                         }
                         // After processing all records, flush the aggregator
                         currentWorkingAggregator.close();
                         // Remove the processed run files and close open files
                         for (int i = 0; i < inFrames.size(); i++) {
                             RunFileReader reader = runs.get(i);
                             reader.close();
                         }
                         runs.subList(0, inFrames.size()).clear();
                         // insert the new run file into the beginning of the run
                         // file list
                         if (!finalPass) {
                             runs.add(0, ((RunFileWriter) writer).createReader());
                             // XXX Remove this when deploying
                             LOGGER.warning("==== [MRG]\t" + newRun.getFile().getAbsolutePath() + "\t"
                                     + newRun.getFile().length());
                         }
                     } finally {
                         if (!finalPass) {
                             writer.close();
                         }
                     }
                 }
 
                 private void flushOutFrame(IFrameWriter writer, boolean isFinal) throws HyracksDataException {
                     // if (isFinal) {
                     if (finalTupleBuilder == null) {
                         finalTupleBuilder = new ArrayTupleBuilder(recordDescriptors[0].getFields().length);
                     }
                     if (writerFrame == null) {
                         writerFrame = ctx.allocateFrame();
                     }
                     if (writerFrameAppender == null) {
                         writerFrameAppender = new FrameTupleAppender(ctx.getFrameSize());
                         writerFrameAppender.reset(writerFrame, true);
                     }
                     outFrameAccessor.reset(outFrame);
                     for (int i = 0; i < outFrameAccessor.getTupleCount(); i++) {
                         finalTupleBuilder.reset();
                         for (int j = 0; j < keyFields.length; j++) {
                             finalTupleBuilder.addField(outFrameAccessor, i, j);
                         }
                         if (isFinal)
                             currentWorkingAggregator.outputResult(outFrameAccessor, i, finalTupleBuilder);
                         else
                             currentWorkingAggregator.outputPartialResult(outFrameAccessor, i, finalTupleBuilder);
 
                         if (!writerFrameAppender.append(finalTupleBuilder.getFieldEndOffsets(),
                                 finalTupleBuilder.getByteArray(), 0, finalTupleBuilder.getSize())) {
                             FrameUtils.flushFrame(writerFrame, writer);
                             writerFrameAppender.reset(writerFrame, true);
                             if (!writerFrameAppender.append(finalTupleBuilder.getFieldEndOffsets(),
                                     finalTupleBuilder.getByteArray(), 0, finalTupleBuilder.getSize()))
                                 throw new HyracksDataException(
                                         "Failed to write final aggregation result to a writer frame!");
                         }
                     }
                     if (writerFrameAppender.getTupleCount() > 0) {
                         FrameUtils.flushFrame(writerFrame, writer);
                         writerFrameAppender.reset(writerFrame, true);
                     }
                     outFrameAppender.reset(outFrame, true);
                 }
 
                 private void setNextTopTuple(int runIndex, int[] tupleIndices, RunFileReader[] runCursors,
                         FrameTupleAccessor[] tupleAccessors, ReferencedPriorityQueue topTuples)
                         throws HyracksDataException {
                     // Check whether the run file for the given runIndex has
                     // more tuples
                     boolean exists = hasNextTuple(runIndex, tupleIndices, runCursors, tupleAccessors);
                     if (exists) {
                         topTuples.popAndReplace(tupleAccessors[runIndex], tupleIndices[runIndex]);
                     } else {
                         topTuples.pop();
                         closeRun(runIndex, runCursors, tupleAccessors);
                     }
                 }
 
                 private boolean hasNextTuple(int runIndex, int[] tupleIndexes, RunFileReader[] runCursors,
                         FrameTupleAccessor[] tupleAccessors) throws HyracksDataException {
 
                     if (tupleAccessors[runIndex] == null || runCursors[runIndex] == null) {
                         /*
                          * Return false if the targeting run file is not
                          * available, or the frame for the run file is not
                          * available.
                          */
                         return false;
                     } else if (tupleIndexes[runIndex] >= tupleAccessors[runIndex].getTupleCount()) {
                         /*
                          * If all tuples in the targeting frame have been
                          * checked.
                          */
                         ByteBuffer buf = tupleAccessors[runIndex].getBuffer(); // same-as-inFrames.get(runIndex)
                         // Refill the buffer with contents from the run file.
                         if (runCursors[runIndex].nextFrame(buf)) {
                             tupleIndexes[runIndex] = 0;
                             return hasNextTuple(runIndex, tupleIndexes, runCursors, tupleAccessors);
                         } else {
                             return false;
                         }
                     } else {
                         return true;
                     }
                 }
 
                 /**
                  * Close the run file, and also the corresponding readers and
                  * input frame.
                  * 
                  * @param index
                  * @param runCursors
                  * @param tupleAccessor
                  * @throws HyracksDataException
                  */
                 private void closeRun(int index, RunFileReader[] runCursors, IFrameTupleAccessor[] tupleAccessor)
                         throws HyracksDataException {
                     runCursors[index].close();
                     runCursors[index] = null;
                     tupleAccessor[index] = null;
                 }
 
                 private int compareFrameTuples(IFrameTupleAccessor fta1, int j1, IFrameTupleAccessor fta2, int j2) {
                     byte[] b1 = fta1.getBuffer().array();
                     byte[] b2 = fta2.getBuffer().array();
                     for (int f = 0; f < keyFields.length; ++f) {
                         // Note: Since the comparison is only used in the merge
                         // phase,
                         // all the keys are clustered at the beginning of the
                         // tuple.
                         int fIdx = f;
                         int s1 = fta1.getTupleStartOffset(j1) + fta1.getFieldSlotsLength()
                                 + fta1.getFieldStartOffset(j1, fIdx);
                         int l1 = fta1.getFieldLength(j1, fIdx);
                         int s2 = fta2.getTupleStartOffset(j2) + fta2.getFieldSlotsLength()
                                 + fta2.getFieldStartOffset(j2, fIdx);
                         int l2_start = fta2.getFieldStartOffset(j2, fIdx);
                         int l2_end = fta2.getFieldEndOffset(j2, fIdx);
                         int l2 = l2_end - l2_start;
                         int c = comparators[f].compare(b1, s1, l1, b2, s2, l2);
                         if (c != 0) {
                             return c;
                         }
                     }
                     return 0;
                 }
             };
             return op;
         }
 
         private Comparator<ReferenceEntry> createEntryComparator(final IBinaryComparator[] comparators) {
             return new Comparator<ReferenceEntry>() {
 
                 @Override
                 public int compare(ReferenceEntry o1, ReferenceEntry o2) {
                     FrameTupleAccessor fta1 = (FrameTupleAccessor) o1.getAccessor();
                     FrameTupleAccessor fta2 = (FrameTupleAccessor) o2.getAccessor();
                     int j1 = o1.getTupleIndex();
                     int j2 = o2.getTupleIndex();
                     byte[] b1 = fta1.getBuffer().array();
                     byte[] b2 = fta2.getBuffer().array();
                     for (int f = 0; f < keyFields.length; ++f) {
                         // Note: Since the comparison is only used in the merge
                         // phase,
                         // all the keys are clustered at the beginning of the
                         // tuple.
                         int fIdx = f;
                         int s1 = fta1.getTupleStartOffset(j1) + fta1.getFieldSlotsLength()
                                 + fta1.getFieldStartOffset(j1, fIdx);
                         int l1 = fta1.getFieldEndOffset(j1, fIdx) - fta1.getFieldStartOffset(j1, fIdx);
                         int s2 = fta2.getTupleStartOffset(j2) + fta2.getFieldSlotsLength()
                                 + fta2.getFieldStartOffset(j2, fIdx);
                         int l2 = fta2.getFieldEndOffset(j2, fIdx) - fta2.getFieldStartOffset(j2, fIdx);
                         int c = comparators[f].compare(b1, s1, l1, b2, s2, l2);
                         if (c != 0) {
                             return c;
                         }
                     }
                     return 0;
                 }
 
             };
         }
 
     }
 
 }
