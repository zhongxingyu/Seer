 /**
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.apache.hadoop.hdfs.protocol;
 
 import org.apache.hadoop.classification.InterfaceAudience;
 import org.apache.hadoop.classification.InterfaceStability;
 import org.apache.hadoop.hdfs.server.common.HdfsServerConstants.ReplicaState;
 import org.apache.hadoop.hdfs.server.datanode.ReplicaInfo;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * This class provides an interface for accessing list of blocks that
  * has been implemented as long[].
  * This class is useful for block report. Rather than send block reports
  * as a Block[] we can send it as a long[].
  *
  * The structure of the array is as follows:
  * 0: the length of the finalized replica list;
  * 1: the length of the under-construction replica list;
  * - followed by finalized replica list where each replica is represented by
  *   3 longs: one for the blockId, one for the block length, and one for
  *   the generation stamp;
  * - followed by the invalid replica represented with three -1s;
  * - followed by the under-construction replica list where each replica is
  *   represented by 4 longs: three for the block id, length, generation 
  *   stamp, and the forth for the replica state.
  */
 @InterfaceAudience.Private
 @InterfaceStability.Evolving
 public class BlockMetricsAsLongs implements Iterable<Block> {
   /**
    * A block metric has 5 longs
    *   block-id and block length and generation stamp,
    *   read counts and read-load.
    */
   private static final int LONGS_PER_BLOCK = 5;
 
   /** Number of longs in the header */
   private static final int HEADER_SIZE = 1;
 
   /**
    * Returns the index of the first long in blockList
    * belonging to the specified block.
    * The first long contains the block id.
    */
   private int index2BlockId(int blockIndex) {
     if(blockIndex < 0 || blockIndex >= getNumberOfBlocks()) {
       return -1;
     }
 
     return HEADER_SIZE + blockIndex * LONGS_PER_BLOCK;
   }
 
   private long[] blockList;
 
   /**
    * Create block report from finalized and under construction lists of blocks.
    *
    * @param replicas - list of finalized blocks
    */
   public BlockMetricsAsLongs(final List<ReplicaInfo> replicas) {
     int replicaSize = replicas == null ? 0 : replicas.size();
     int len = HEADER_SIZE
               + (replicaSize) * LONGS_PER_BLOCK;
 
     blockList = new long[len];
 
     // set the header
     blockList[0] = replicaSize;
 
     // set finalized blocks
     for (int i = 0; i < replicaSize; i++) {
       setBlock(i, replicas.get(i),
           replicas.get(i).metrics.getNumReads(),
          replicas.get(i).metrics.window.getReadsPerSecondAsLong());
     }
   }
 
 
   /**
    * Constructor
    * @param iBlockList - BlockListALongs create from this long[] parameter
    */
   public BlockMetricsAsLongs(final long[] iBlockList) {
     if (iBlockList == null) {
       blockList = new long[HEADER_SIZE];
       return;
     }
     blockList = iBlockList;
   }
 
   public long[] getBlockMetricsListAsLongs() {
     return blockList;
   }
 
   /**
    * Iterates over blocks in the block report.
    * Avoids object allocation on each iteration.
    */
   @InterfaceAudience.Private
   @InterfaceStability.Evolving
   public class BlockReportIterator implements Iterator<Block> {
     private int currentBlockIndex;
     private Block block;
 
     BlockReportIterator() {
       this.currentBlockIndex = 0;
       this.block = new Block();
     }
 
     public boolean hasNext() {
       return currentBlockIndex < getNumberOfBlocks();
     }
 
     public Block next() {
       block.set(blockId(currentBlockIndex),
                 blockLength(currentBlockIndex),
                 blockGenerationStamp(currentBlockIndex));
       currentBlockIndex++;
       return block;
     }
 
     public long getCurrentReadCount() {
       return readCounts(currentBlockIndex);
     }
 
    public long getCurrentReadLoad() {
      return readLoad(currentBlockIndex);
     }
 
     public void remove() {
       throw new UnsupportedOperationException("Sorry. can't remove.");
     }
   }
 
   /**
    * Returns an iterator over blocks in the block report.
    */
   public Iterator<Block> iterator() {
     return getBlockReportIterator();
   }
 
   /**
    * Returns {@link BlockReportIterator}.
    */
   public BlockReportIterator getBlockReportIterator() {
     return new BlockReportIterator();
   }
 
   /**
    * The number of blocks
    * @return - the number of blocks
    */
   public int getNumberOfBlocks() {
     assert blockList.length == HEADER_SIZE + 
             (blockList[0]) * LONGS_PER_BLOCK:
               "Number of blocks is inconcistent with the array length";
     return (int)blockList[0];
   }
 
   /**
    * Returns the id of the specified replica of the block report.
    */
   private long blockId(int index) {
     return blockList[index2BlockId(index)];
   }
 
   /**
    * Returns the length of the specified replica of the block report.
    */
   private long blockLength(int index) {
     return blockList[index2BlockId(index) + 1];
   }
 
   /**
    * Returns the generation stamp of the specified replica of the block report.
    */
   private long blockGenerationStamp(int index) {
     return blockList[index2BlockId(index) + 2];
   }
 
 
   private long readCounts(int index) {
     return blockList[index2BlockId(index) + 3];
   }
 
   private long readLoad(int index) {
     return blockList[index2BlockId(index) + 4];
   }
 
 
 
   /**
    * Returns the state of the specified replica of the block report.
    */
   private ReplicaState blockReplicaState(int index) {
     if(index < getNumberOfBlocks())
       return ReplicaState.FINALIZED;
     return ReplicaState.getState((int)blockList[index2BlockId(index) + 3]);
   }
 
   /**
    * The block-id of the indexTh block
    * @param index - the block whose block-id is desired
    * @return the block-id
    */
   @Deprecated
   public long getBlockId(final int index)  {
     return blockId(index);
   }
   
   /**
    * The block-len of the indexTh block
    * @param index - the block whose block-len is desired
    * @return - the block-len
    */
   @Deprecated
   public long getBlockLen(final int index)  {
     return blockLength(index);
   }
 
   /**
    * The generation stamp of the indexTh block
    * @param index - the block whose block-len is desired
    * @return - the generation stamp
    */
   @Deprecated
   public long getBlockGenStamp(final int index)  {
     return blockGenerationStamp(index);
   }
   
   /**
    * Set the indexTh block
    * @param index - the index of the block to set
    * @param b - the block is set to the value of the this block
    * @param readCount
    */
   private void setBlock(final int index, final ReplicaInfo b, Long readCount,
                         Long readLoad) {
     int pos = index2BlockId(index);
     blockList[pos] = b.getBlockId();
     blockList[pos + 1] = b.getNumBytes();
     blockList[pos + 2] = b.getGenerationStamp();
     blockList[pos + 3] = readCount;
     blockList[pos + 4] = readLoad;
     if(index < getNumberOfBlocks())
       return;
   }
 
   /**
    * Set the invalid delimiting block between the finalized and
    * the under-construction lists.
    * The invalid block has all three fields set to -1.
    * @param finalizedSzie - the size of the finalized list
    */
   private void setDelimitingBlock(final int finalizedSzie) {
     int idx = HEADER_SIZE + finalizedSzie * LONGS_PER_BLOCK;
     blockList[idx] = -1;
     blockList[idx+1] = -1;
     blockList[idx+2] = -1;
   }
 }
