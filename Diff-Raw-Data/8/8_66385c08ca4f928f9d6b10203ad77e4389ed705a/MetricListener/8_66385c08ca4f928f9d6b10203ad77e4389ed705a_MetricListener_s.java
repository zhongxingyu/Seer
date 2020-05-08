 package edu.kit.checkstyle.listeners;
 
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.PrintWriter;
 import java.util.List;
 
 import com.google.common.collect.HashBasedTable;
 import com.google.common.collect.Table;
 import com.puppycrawl.tools.checkstyle.api.AuditEvent;
 import com.puppycrawl.tools.checkstyle.api.AuditListener;
 import com.puppycrawl.tools.checkstyle.api.AutomaticBean;
 import com.puppycrawl.tools.checkstyle.api.SeverityLevel;
 
 import static edu.kit.checkstyle.CollectionUtils.*;
 
 
 /**
  * Catches all metric messages and stores them to a file. The messages caught
  * need to start with the prefix 'metric:'. The full format for a message looks
  * like this:
  *
  * <pre>
  * metric:metric-ident:value
  * </pre>
  *
  * where 'metric-ident' denotes the name of the metric and 'value' its
  * calculated value.
  *
  * @since JDK1.7, Jul 15, 2013
  */
 public class MetricListener extends AutomaticBean implements AuditListener {
 
   private PrintWriter writer = new PrintWriter(System.out);
   private boolean closeOut = false;
 
   /**
    * Stores the name of the file in the row and the name of the metric in the column.
    */
   private final Table<String, String, List<Integer>> metrics =
       HashBasedTable.create();
 
   private String currentFile = "";
 
   @Override
   public void auditStarted(final AuditEvent e) {}
 
   @Override
   public void auditFinished(final AuditEvent e) {
    for (final String row : metrics.rowKeySet()) {
      for (final String column : metrics.columnKeySet()) {
        writer.println(row + ":" + column + ":" + metrics.get(row, column));
       }
     }
     writer.flush();
     if (closeOut) {
       writer.close();
     }
   }
 
   @Override
   public void fileStarted(final AuditEvent e) {
     currentFile = e.getFileName();
   }
 
   @Override
   public void fileFinished(final AuditEvent e) {}
 
   @Override
   public void addError(final AuditEvent e) {
     if (SeverityLevel.INFO.equals(e.getSeverityLevel())
         && e.getMessage().startsWith("metric:")) {
       final String[] parts = e.getMessage().split(":");
       final String metric = parts[1];
       final Integer value = Integer.valueOf(parts[2]);
       final List<Integer> values = metrics.get(currentFile, metric);
 
       if (values == null) {
         metrics.put(currentFile, metric, mkList(value));
       } else {
         values.add(value);
       }
     }
   }
 
   @Override
   public void addException(final AuditEvent e, final Throwable throwable) {}
 
   public void setFile(final String fileName) throws FileNotFoundException {
     writer = new PrintWriter(new FileOutputStream(fileName));
     closeOut = true;
   }
 }
