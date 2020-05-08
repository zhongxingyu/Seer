 package eu.europeana.uim.logging.memory;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import eu.europeana.uim.api.IngestionPlugin;
 import eu.europeana.uim.api.LogEntry;
 import eu.europeana.uim.api.LoggingEngine;
 import eu.europeana.uim.store.Execution;
 import eu.europeana.uim.store.MetaDataRecord;
 
 /**
  * Simplistic implementation of the logging service. In this implementation we do not care to keep
  * track of the MDRs responsible for a duration. This feature would be useful in order to see
  * exactly what MDR is causing what delay.
  * 
  * @param <I>
  *            generic identifier
  * @param <T>
  *            generic message
  * 
  * @author Manuel Bernhardt <bernhardt.manuel@gmail.com>
  * @author Markus Muhr (markus.muhr@kb.nl)
  * @since Mar 22, 2011
  */
 public class MemoryLoggingEngine<I, T> implements LoggingEngine<I, T> {
     private Map<I, List<LogEntry<I, String[]>>> executionLogs           = new HashMap<I, List<LogEntry<I, String[]>>>();
     private Map<I, List<LogEntry<I, T>>>        structuredExecutionLogs = new HashMap<I, List<LogEntry<I, T>>>();
     private Map<IngestionPlugin, List<Long>>    durations               = new HashMap<IngestionPlugin, List<Long>>();
 
     @Override
     public String getIdentifier() {
         return MemoryLoggingEngine.class.getSimpleName();
     }
 
     @Override
     public void log(String module, Execution<I> execution, String scope, Level level,
             String... message) {
        List<LogEntry<I, String[]>> logs = executionLogs.get(execution.getId());
         if (logs == null) {
             logs = new ArrayList<LogEntry<I, String[]>>();
             executionLogs.put(execution.getId(), logs);
         }
         logs.add(new MemoryLogEntry<I, String[]>(module, execution, scope, level, new Date(),
                 message));
     }
 
     @Override
     public void log(IngestionPlugin plugin, Execution<I> execution, MetaDataRecord<I> mdr,
             String scope, Level level, String... message) {
        List<LogEntry<I, String[]>> logs = executionLogs.get(execution.getId());
         if (logs == null) {
             logs = new ArrayList<LogEntry<I, String[]>>();
             executionLogs.put(execution.getId(), logs);
         }
         logs.add(new MemoryLogEntry<I, String[]>(plugin, execution, mdr, scope, level, new Date(),
                 message));
     }
 
     @Override
     public void logStructured(IngestionPlugin plugin, Execution<I> execution,
             MetaDataRecord<I> mdr, String scope, Level level, T payload) {
         List<LogEntry<I, T>> logs = structuredExecutionLogs.get(execution);
         if (logs == null) {
             logs = new ArrayList<LogEntry<I, T>>();
             structuredExecutionLogs.put(execution.getId(), logs);
         }
         logs.add(new MemoryLogEntry<I, T>(plugin, execution, mdr, scope, level, new Date(), payload));
     }
 
     @Override
     public List<LogEntry<I, String[]>> getExecutionLog(Execution<I> execution) {
         return executionLogs.get(execution.getId());
     }
 
     @Override
     public List<LogEntry<I, String[]>> getExecutionLog(I executionID) {
         return executionLogs.get(executionID);
     }
 
     @Override
     public List<LogEntry<I, T>> getStructuredExecutionLog(Execution<I> execution) {
         return structuredExecutionLogs.get(execution.getId());
     }
 
     @Override
     public List<LogEntry<I, T>> getStructuredExecutionLog(I executionID) {
         return structuredExecutionLogs.get(executionID);
     }
 
     private List<Long> getDurations(IngestionPlugin plugin) {
         List<Long> d = durations.get(plugin);
         if (d == null) {
             d = new ArrayList<Long>();
             durations.put(plugin, d);
         }
         return d;
     }
 
     @Override
     public void logDurationDetailed(IngestionPlugin plugin, Long duration, I... mdrs) {
         List<Long> d = getDurations(plugin);
         // don't show this to hardcore statisticians
         for (int i = 0; i < mdrs.length; i++) {
             d.add(duration / mdrs.length);
         }
     }
 
     @Override
     public void logDuration(IngestionPlugin plugin, Long duration, int count) {
         List<Long> d = getDurations(plugin);
         // don't show this to hardcore statisticians
         for (int i = 0; i < count; i++) {
             d.add(duration / count);
         }
     }
 
     @Override
     public Long getAverageDuration(IngestionPlugin plugin) {
         long sum = 0l;
         List<Long> d = getDurations(plugin);
         for (Long l : d) {
             sum += l;
         }
         return sum / d.size();
     }
 
 }
