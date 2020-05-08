 package com.moviepilot.sheldon.compactor.config;
 
 import com.moviepilot.sheldon.compactor.handler.*;
 import gnu.trove.map.TObjectLongMap;
 import org.neo4j.unsafe.batchinsert.BatchInserter;
 import org.neo4j.unsafe.batchinsert.BatchInserterIndexProvider;
 
 /**
  * @author stefanp
  * @since 09.08.12
  */
 public interface Config {
    public boolean isCopyingNodes();
    public boolean isCopyingEdges();

     public int getRingSize();
 
     public int getIndexFlushMinInterval();
     public int getIndexFlushMaxInterval();
 
     public int getNumIndexWriters();
     public int getNumIndexFlushers();
     public int getIndexBatchSize();
 
     public BatchInserter getTargetDatabase();
     public BatchInserterIndexProvider getTargetIndexProvider();
 
     public TObjectLongMap<String> getModMap();
 
     public NodeEventHandler getOptNodeHandler();
     public EdgeEventHandler getOptEdgeHandler();
 
     public NodeIndexer getOptNodeIndexer();
     public EdgeIndexer getOptEdgeIndexer();
 
     public int getNumExtraNodeThreads();
     public int getNumExtraEdgeThreads();
 
     public int getNumIndexEntries();
     public int getNumIndexProps();
 
     public int getNumProps();
 
     public long getDotNodes();
     public long getDotEdges();
     public long getDotOk();
 
     public long getDotKind(final PropertyContainerEventHandler.Kind kind);
 
     public boolean isPrintRingSizeInfo();
 }
