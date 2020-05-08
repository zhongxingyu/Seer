 package com.virtualdisk.datanode;
 
 import java.util.*;
 
 /**
  * A factory method to create DataNodes.
  */
 public class DataNodeFactory
 {
     /**
      * Constructs the DataNode and configures the singleton. Safe to call many times.
      * @param   blockSize       the size of blocks for the datanode, in bytes
      * @param   driveHandles    a list of the handles for the drives for the node
      * @param   driveSizes      a list of the sizes of the drives for the node
      */
     public static DataNode setup( int blockSize
                                , List<String> driveHandles
                                , List<Long> driveSizes
                                )
     {
         return new DataNode(blockSize, driveHandles, driveSizes);
     }
 
 }
 
