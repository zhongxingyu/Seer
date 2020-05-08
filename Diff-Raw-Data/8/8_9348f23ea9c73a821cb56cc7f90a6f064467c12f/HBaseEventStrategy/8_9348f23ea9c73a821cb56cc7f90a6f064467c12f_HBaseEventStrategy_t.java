 package com.nesscomputing.hbase.event;
 
 import org.apache.hadoop.hbase.client.Put;
 
 import com.google.inject.Singleton;
 import com.nesscomputing.event.NessEvent;
 
 /**
  * This is a interface representing the operations that the writer performs.
  */
 public interface HBaseEventStrategy
 {
     /** @return The table name to write events to. */
     String getTableName();
 
     /** @return A byte array representing the name of the column family. */
     byte[] getEventColumnFamily();
 
     /**
      * @param event The event to determine if we accept.
      * @return True if this strategy accepts the event, false otherwise.
      */
     boolean acceptEvent(NessEvent event);
 
     /**
      * @param event The event to process into HBase
      * @return An HBase Put object which is translation of event.
      */
     Put encodeEvent(NessEvent event);
 }
