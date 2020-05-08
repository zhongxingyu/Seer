 package org.h2o.eval.script.workload;
 
 import java.io.Serializable;
 import java.text.DateFormat;
 import java.util.Collection;
 import java.util.List;
 
 import uk.ac.standrews.cs.nds.util.PrettyPrinter;
 
 public class LogEntry implements Serializable {
 
     private static final long serialVersionUID = 8404704261377608930L;
 
     public long timeOfCommit;
 
     public LogEntry() {
 
     }
 
     public LogEntry(final long timeOfCommit) {
 
         this.timeOfCommit = timeOfCommit;
     }
 
     public static String toCSVHeader(final Collection<String> tableNames) {
 
         String header = "Time of Transaction (ms), Time of Transaction (s), Time To Execute if Successful, Time to Execute if Unsuccessful, Machine Start Event, Machine Failure Event, Location of Event, Tables Involved, Insert Queries, Select Queries, ";
 
         for (final String tableName : tableNames) {
             header += tableName + ",";
         }
 
         header += "\n";
 
         return header;
     }
 
     public String toCSV(final DateFormat dateformatter, final String locationOfExecution, final Collection<String> tableNames, final long startTime, final Collection<String> tablesInvolved, final boolean successfulExecution, final long timeToExecute, final List<QueryType> queryTypes,
                     final boolean machineFailureEvent, final boolean machineFailure) {
 
         final String tablesInvolvedString = PrettyPrinter.toString(tablesInvolved, ";", false);
        final long timeOfTransactionMS = timeOfCommit - startTime;
         final int timeOfTransactionSec = (int) (timeOfTransactionMS / 1000);
 
         final String successfulExecutionTime = machineFailureEvent ? "=NA()" : (successfulExecution ? timeToExecute : "=NA()") + "";
         final String unsuccessfulExecutionTime = machineFailureEvent ? "=NA()" : (!successfulExecution ? timeToExecute : "=NA()") + "";
         final String machineFailureEventText = !machineFailureEvent ? "=NA()" : machineFailure ? "0" : "=NA()";
         final String machineStartEventText = !machineFailureEvent ? "=NA()" : !machineFailure ? "0" : "=NA()";
 
         String row = timeOfTransactionMS + "," + timeOfTransactionSec + "," + successfulExecutionTime + "," + unsuccessfulExecutionTime + "," + machineStartEventText + "," + machineFailureEventText + "," + locationOfExecution + "," + tablesInvolvedString + ","
                         + getNumberOf(QueryType.INSERT, queryTypes) + "," + getNumberOf(QueryType.SELECT, queryTypes) + ",";
 
         if (tableNames != null) {
             for (final String tableName : tableNames) {
                 if (tableName.equals(tablesInvolvedString)) {
                     row += timeToExecute;
                 }
                 else {
                     row += "=NA()";
                 }
 
                 row += ",";
             }
         }
         row += "\n";
 
         return row;
 
     }
 
     public enum QueryType {
         CREATE, DROP, INSERT, DELETE, UNKNOWN, SELECT
     };
 
     int getNumberOf(final QueryType type, final List<QueryType> queryTypes) {
 
         if (queryTypes == null) { return 0; }
         int count = 0;
         for (final QueryType singleQueryType : queryTypes) {
             if (singleQueryType.equals(type)) {
                 count++;
             }
         }
         return count;
     }
 
 }
