 package org.neo4j.extension.timestamp;
 
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.PropertyContainer;
 import org.neo4j.graphdb.index.AutoIndexer;
 import org.neo4j.graphdb.index.IndexManager;
 import org.neo4j.kernel.lifecycle.LifecycleAdapter;
 
 public class TimestampKernelExtension extends LifecycleAdapter{
   
   private final GraphDatabaseService gdb;
   private boolean setupAutoIndexing = false;
 
   public TimestampKernelExtension(GraphDatabaseService gdb, boolean setupAutoIndexing){
     this.gdb = gdb;
     this.setupAutoIndexing = setupAutoIndexing;
   }
 
   @Override
   public void start() throws Throwable{
     IndexManager indexManager = this.gdb.index();
    setupTimestampIndexing(indexManager.getNodeAutoIndexer());
    setupTimestampIndexing(indexManager.getRelationshipAutoIndexer());
     this.gdb.registerTransactionEventHandler(new TimestampTransactionEventHandler<String>());
   }
 
   void setupTimestampIndexing(AutoIndexer<? extends PropertyContainer> autoIndexer) {
     autoIndexer.startAutoIndexingProperty(TimestampTransactionEventHandler.TIMESTAMP_PROPERTY_NAME);
     autoIndexer.setEnabled(true);
   }
 }
