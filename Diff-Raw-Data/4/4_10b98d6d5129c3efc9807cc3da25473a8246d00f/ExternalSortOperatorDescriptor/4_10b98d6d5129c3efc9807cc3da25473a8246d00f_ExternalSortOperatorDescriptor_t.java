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
 package edu.uci.ics.hyracks.dataflow.std.sort;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.nio.ByteBuffer;
 import java.nio.channels.FileChannel;
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.LinkedList;
 import java.util.List;
 
 import edu.uci.ics.hyracks.api.comm.IFrameReader;
 import edu.uci.ics.hyracks.api.comm.IFrameTupleAccessor;
 import edu.uci.ics.hyracks.api.comm.IFrameWriter;
 import edu.uci.ics.hyracks.api.context.IHyracksContext;
 import edu.uci.ics.hyracks.api.dataflow.IActivityGraphBuilder;
 import edu.uci.ics.hyracks.api.dataflow.IOperatorDescriptor;
 import edu.uci.ics.hyracks.api.dataflow.IOperatorNodePushable;
 import edu.uci.ics.hyracks.api.dataflow.value.IBinaryComparator;
 import edu.uci.ics.hyracks.api.dataflow.value.IBinaryComparatorFactory;
 import edu.uci.ics.hyracks.api.dataflow.value.INormalizedKeyComputerFactory;
 import edu.uci.ics.hyracks.api.dataflow.value.IRecordDescriptorProvider;
 import edu.uci.ics.hyracks.api.dataflow.value.RecordDescriptor;
 import edu.uci.ics.hyracks.api.exceptions.HyracksDataException;
 import edu.uci.ics.hyracks.api.job.IOperatorEnvironment;
 import edu.uci.ics.hyracks.api.job.JobSpecification;
 import edu.uci.ics.hyracks.dataflow.common.comm.io.FrameTupleAccessor;
 import edu.uci.ics.hyracks.dataflow.common.comm.io.FrameTupleAppender;
 import edu.uci.ics.hyracks.dataflow.common.comm.util.FrameUtils;
 import edu.uci.ics.hyracks.dataflow.std.base.AbstractActivityNode;
 import edu.uci.ics.hyracks.dataflow.std.base.AbstractOperatorDescriptor;
 import edu.uci.ics.hyracks.dataflow.std.base.AbstractUnaryInputSinkOperatorNodePushable;
 import edu.uci.ics.hyracks.dataflow.std.base.AbstractUnaryOutputSourceOperatorNodePushable;
 import edu.uci.ics.hyracks.dataflow.std.util.ReferenceEntry;
 import edu.uci.ics.hyracks.dataflow.std.util.ReferencedPriorityQueue;
 
 public class ExternalSortOperatorDescriptor extends AbstractOperatorDescriptor {
     private static final String FRAMESORTER = "framesorter";
     private static final String RUNS = "runs";
 
     private static final long serialVersionUID = 1L;
     private final int[] sortFields;
     private final INormalizedKeyComputerFactory firstKeyNormalizerFactory;
     private final IBinaryComparatorFactory[] comparatorFactories;
     private final int framesLimit;
 
     public ExternalSortOperatorDescriptor(JobSpecification spec, int framesLimit, int[] sortFields,
             IBinaryComparatorFactory[] comparatorFactories, RecordDescriptor recordDescriptor) {
         this(spec, framesLimit, sortFields, null, comparatorFactories, recordDescriptor);
     }
 
     public ExternalSortOperatorDescriptor(JobSpecification spec, int framesLimit, int[] sortFields,
             INormalizedKeyComputerFactory firstKeyNormalizerFactory, IBinaryComparatorFactory[] comparatorFactories,
             RecordDescriptor recordDescriptor) {
         super(spec, 1, 1);
         this.framesLimit = framesLimit;
         this.sortFields = sortFields;
         this.firstKeyNormalizerFactory = firstKeyNormalizerFactory;
         this.comparatorFactories = comparatorFactories;
         if (framesLimit <= 1) {
             throw new IllegalStateException();// minimum of 2 fames (1 in,1 out)
         }
         recordDescriptors[0] = recordDescriptor;
     }
 
     @Override
     public void contributeTaskGraph(IActivityGraphBuilder builder) {
         SortActivity sa = new SortActivity();
         MergeActivity ma = new MergeActivity();
 
         builder.addTask(sa);
         builder.addSourceEdge(0, sa, 0);
 
         builder.addTask(ma);
         builder.addTargetEdge(0, ma, 0);
 
         builder.addBlockingEdge(sa, ma);
     }
 
     private class SortActivity extends AbstractActivityNode {
         private static final long serialVersionUID = 1L;
 
         @Override
         public IOperatorDescriptor getOwner() {
             return ExternalSortOperatorDescriptor.this;
         }
 
         @Override
         public IOperatorNodePushable createPushRuntime(final IHyracksContext ctx, final IOperatorEnvironment env,
                 IRecordDescriptorProvider recordDescProvider, int partition, int nPartitions) {
             final FrameSorter frameSorter = new FrameSorter(ctx, sortFields, firstKeyNormalizerFactory,
                     comparatorFactories, recordDescriptors[0]);
             final int maxSortFrames = framesLimit - 1;
             IOperatorNodePushable op = new AbstractUnaryInputSinkOperatorNodePushable() {
                 private LinkedList<File> runs;
 
                 @Override
                 public void open() throws HyracksDataException {
                     runs = new LinkedList<File>();
                     frameSorter.reset();
                 }
 
                 @Override
                 public void nextFrame(ByteBuffer buffer) throws HyracksDataException {
                     if (frameSorter.getFrameCount() >= maxSortFrames) {
                         flushFramesToRun();
                     }
                     frameSorter.insertFrame(buffer);
                 }
 
                 @Override
                 public void close() throws HyracksDataException {
                     if (frameSorter.getFrameCount() > 0) {
                         if (runs.size() <= 0) {
                             frameSorter.sortFrames();
                             env.set(FRAMESORTER, frameSorter);
                         } else {
                             flushFramesToRun();
                         }
                     }
                     env.set(RUNS, runs);
                 }
 
                 private void flushFramesToRun() throws HyracksDataException {
                     frameSorter.sortFrames();
                     File runFile;
                     try {
                         runFile = ctx.getResourceManager().createFile(
                                 ExternalSortOperatorDescriptor.class.getSimpleName(), ".run");
                     } catch (IOException e) {
                         throw new HyracksDataException(e);
                     }
                     RunFileWriter writer = new RunFileWriter(runFile);
                     writer.open();
                     try {
                         frameSorter.flushFrames(writer);
                     } finally {
                         writer.close();
                     }
                     frameSorter.reset();
                     runs.add(runFile);
                 }
 
                 @Override
                 public void flush() throws HyracksDataException {
                 }
             };
             return op;
         }
     }
 
     private class MergeActivity extends AbstractActivityNode {
         private static final long serialVersionUID = 1L;
 
         @Override
         public IOperatorDescriptor getOwner() {
             return ExternalSortOperatorDescriptor.this;
         }
 
         @Override
         public IOperatorNodePushable createPushRuntime(final IHyracksContext ctx, final IOperatorEnvironment env,
                 IRecordDescriptorProvider recordDescProvider, int partition, int nPartitions) {
             final IBinaryComparator[] comparators = new IBinaryComparator[comparatorFactories.length];
             for (int i = 0; i < comparatorFactories.length; ++i) {
                 comparators[i] = comparatorFactories[i].createBinaryComparator();
             }
             IOperatorNodePushable op = new AbstractUnaryOutputSourceOperatorNodePushable() {
                 private List<ByteBuffer> inFrames;
                 private ByteBuffer outFrame;
                 LinkedList<File> runs;
                 private FrameTupleAppender outFrameAppender;
 
                 @Override
                 public void initialize() throws HyracksDataException {
                     runs = (LinkedList<File>) env.get(RUNS);
                     writer.open();
                     try {
                         if (runs.size() <= 0) {
                             FrameSorter frameSorter = (FrameSorter) env.get(FRAMESORTER);
                            if (frameSorter != null) {
                                frameSorter.flushFrames(writer);
                            }
                             env.set(FRAMESORTER, null);
                         } else {
                             inFrames = new ArrayList<ByteBuffer>();
                             outFrame = ctx.getResourceManager().allocateFrame();
                             outFrameAppender = new FrameTupleAppender(ctx);
                             outFrameAppender.reset(outFrame, true);
                             for (int i = 0; i < framesLimit - 1; ++i) {
                                 inFrames.add(ctx.getResourceManager().allocateFrame());
                             }
                             int passCount = 0;
                             while (runs.size() > 0) {
                                 passCount++;
                                 try {
                                     doPass(runs, passCount);
                                 } catch (Exception e) {
                                     throw new HyracksDataException(e);
                                 }
                             }
                         }
                     } finally {
                         writer.close();
                     }
                     env.set(RUNS, null);
                 }
 
                 // creates a new run from runs that can fit in memory.
                 private void doPass(LinkedList<File> runs, int passCount) throws HyracksDataException, IOException {
                     File newRun = null;
                     IFrameWriter writer = this.writer;
                     boolean finalPass = false;
                     if (runs.size() + 1 <= framesLimit) { // + 1 outFrame
                         finalPass = true;
                         for (int i = inFrames.size() - 1; i >= runs.size(); i--) {
                             inFrames.remove(i);
                         }
                     } else {
                         newRun = ctx.getResourceManager().createFile(
                                 ExternalSortOperatorDescriptor.class.getSimpleName(), ".run");
                         writer = new RunFileWriter(newRun);
                         writer.open();
                     }
                     try {
                         RunFileReader[] runCursors = new RunFileReader[inFrames.size()];
                         FrameTupleAccessor[] tupleAccessors = new FrameTupleAccessor[inFrames.size()];
                         Comparator<ReferenceEntry> comparator = createEntryComparator(comparators);
                         ReferencedPriorityQueue topTuples = new ReferencedPriorityQueue(ctx, recordDescriptors[0],
                                 inFrames.size(), comparator);
                         int[] tupleIndexes = new int[inFrames.size()];
                         for (int i = 0; i < inFrames.size(); i++) {
                             tupleIndexes[i] = 0;
                             int runIndex = topTuples.peek().getRunid();
                             runCursors[runIndex] = new RunFileReader(runs.get(runIndex));
                             runCursors[runIndex].open();
                             if (runCursors[runIndex].nextFrame(inFrames.get(runIndex))) {
                                 tupleAccessors[runIndex] = new FrameTupleAccessor(ctx, recordDescriptors[0]);
                                 tupleAccessors[runIndex].reset(inFrames.get(runIndex));
                                 setNextTopTuple(runIndex, tupleIndexes, runCursors, tupleAccessors, topTuples);
                             } else {
                                 closeRun(runIndex, runCursors, tupleAccessors);
                             }
                         }
 
                         while (!topTuples.areRunsExhausted()) {
                             ReferenceEntry top = topTuples.peek();
                             int runIndex = top.getRunid();
                             FrameTupleAccessor fta = top.getAccessor();
                             int tupleIndex = top.getTupleIndex();
 
                             if (!outFrameAppender.append(fta, tupleIndex)) {
                                 FrameUtils.flushFrame(outFrame, writer);
                                 outFrameAppender.reset(outFrame, true);
                                 if (!outFrameAppender.append(fta, tupleIndex)) {
                                     throw new IllegalStateException();
                                 }
                             }
 
                             ++tupleIndexes[runIndex];
                             setNextTopTuple(runIndex, tupleIndexes, runCursors, tupleAccessors, topTuples);
                         }
                         if (outFrameAppender.getTupleCount() > 0) {
                             FrameUtils.flushFrame(outFrame, writer);
                             outFrameAppender.reset(outFrame, true);
                         }
                         runs.subList(0, inFrames.size()).clear();
                         if (!finalPass) {
                             runs.add(0, newRun);
                         }
                     } finally {
                         if (!finalPass) {
                             writer.close();
                         }
                     }
                 }
 
                 private void setNextTopTuple(int runIndex, int[] tupleIndexes, RunFileReader[] runCursors,
                         FrameTupleAccessor[] tupleAccessors, ReferencedPriorityQueue topTuples) throws IOException {
                     boolean exists = hasNextTuple(runIndex, tupleIndexes, runCursors, tupleAccessors);
                     if (exists) {
                         topTuples.popAndReplace(tupleAccessors[runIndex], tupleIndexes[runIndex]);
                     } else {
                         topTuples.pop();
                         closeRun(runIndex, runCursors, tupleAccessors);
                     }
                 }
 
                 private boolean hasNextTuple(int runIndex, int[] tupleIndexes, RunFileReader[] runCursors,
                         FrameTupleAccessor[] tupleAccessors) throws IOException {
                     if (tupleAccessors[runIndex] == null || runCursors[runIndex] == null) {
                         return false;
                     } else if (tupleIndexes[runIndex] >= tupleAccessors[runIndex].getTupleCount()) {
                         ByteBuffer buf = tupleAccessors[runIndex].getBuffer(); // same-as-inFrames.get(runIndex)
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
 
                 private void closeRun(int index, RunFileReader[] runCursors, IFrameTupleAccessor[] tupleAccessor)
                         throws HyracksDataException {
                     runCursors[index].close();
                     runCursors[index] = null;
                     tupleAccessor[index] = null;
                 }
             };
             return op;
         }
     }
 
     private Comparator<ReferenceEntry> createEntryComparator(final IBinaryComparator[] comparators) {
         return new Comparator<ReferenceEntry>() {
             public int compare(ReferenceEntry tp1, ReferenceEntry tp2) {
                 FrameTupleAccessor fta1 = (FrameTupleAccessor) tp1.getAccessor();
                 FrameTupleAccessor fta2 = (FrameTupleAccessor) tp2.getAccessor();
                 int j1 = (Integer) tp1.getTupleIndex();
                 int j2 = (Integer) tp2.getTupleIndex();
                 byte[] b1 = fta1.getBuffer().array();
                 byte[] b2 = fta2.getBuffer().array();
                 for (int f = 0; f < sortFields.length; ++f) {
                     int fIdx = sortFields[f];
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
 
     private class RunFileWriter implements IFrameWriter {
         private final File file;
         private FileChannel channel;
 
         public RunFileWriter(File file) {
             this.file = file;
         }
 
         @Override
         public void open() throws HyracksDataException {
             RandomAccessFile raf;
             try {
                 raf = new RandomAccessFile(file, "rw");
             } catch (FileNotFoundException e) {
                 throw new HyracksDataException(e);
             }
             channel = raf.getChannel();
         }
 
         @Override
         public void nextFrame(ByteBuffer buffer) throws HyracksDataException {
             int remain = buffer.capacity();
             while (remain > 0) {
                 int len;
                 try {
                     len = channel.write(buffer);
                 } catch (IOException e) {
                     throw new HyracksDataException(e);
                 }
                 if (len < 0) {
                     throw new HyracksDataException("Error writing data");
                 }
                 remain -= len;
             }
         }
 
         @Override
         public void close() throws HyracksDataException {
             if (channel != null) {
                 try {
                     channel.close();
                 } catch (IOException e) {
                     throw new HyracksDataException(e);
                 }
             }
         }
 
         @Override
         public void flush() throws HyracksDataException {
         }
     }
 
     public static class RunFileReader implements IFrameReader {
         private final File file;
         private FileChannel channel;
 
         public RunFileReader(File file) throws FileNotFoundException {
             this.file = file;
         }
 
         @Override
         public void open() throws HyracksDataException {
             RandomAccessFile raf;
             try {
                 raf = new RandomAccessFile(file, "r");
             } catch (FileNotFoundException e) {
                 throw new HyracksDataException(e);
             }
             channel = raf.getChannel();
         }
 
         @Override
         public boolean nextFrame(ByteBuffer buffer) throws HyracksDataException {
             buffer.clear();
             int remain = buffer.capacity();
             while (remain > 0) {
                 int len;
                 try {
                     len = channel.read(buffer);
                 } catch (IOException e) {
                     throw new HyracksDataException(e);
                 }
                 if (len < 0) {
                     return false;
                 }
                 remain -= len;
             }
             return true;
         }
 
         @Override
         public void close() throws HyracksDataException {
             try {
                 channel.close();
             } catch (IOException e) {
                 throw new HyracksDataException(e);
             }
         }
     }
 }
