 package loaylitymonitor.cmd;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Scanner;
 
 public class SqoopRunner {
     private static Logger log = LoggerFactory.getLogger(SqoopRunner.class);
 
     private static final String exportCmd = "%s/sqoop export --connect '%s'  --table %s  --export-dir %s --staging-table %s " +
             "--clear-staging-table --columns %s -m 1 --input-fields-terminated-by '\t'";
 
     public SqoopRunner() {
 
     }
 
     public void runExport(String exportDir, String tableName, String... columns) throws IOException {
         StringBuilder columnsStr = new StringBuilder();
         for(String s : columns) {
             columnsStr.append( String.format("%s,", s) );
         }
 
         String cmd = String.format(exportCmd, "/share/sqoop-1.4.4-SNAPSHOT.bin__hadoop-1.0.0/bin/sqoop",
                 "jdbc:mysql://10.25.9.245:3306/loyalitymonitor?relaxAutoCommit=true&autoReconnect=true&useUnicode=true&characterEncoding=UTF8&user=root&password=root",
                 tableName, exportDir, tableName+"_stg", columnsStr.substring(0, columnsStr.length() - 1));
 
         Runtime rt = Runtime.getRuntime();
         Process pr = rt.exec(cmd);
         logCommandExecution(pr.getErrorStream(), true);
         logCommandExecution(pr.getErrorStream(), false);
 
        if( pr.exitValue() != 0 ) {
            log.info("To re-run export execute next command {}", cmd);
            throw new IOException("Sqoop export failed!");
        }
     }
 
     private void logCommandExecution(InputStream is, boolean isError) {
         if( is != null ) {
             Scanner s = new Scanner(is).useDelimiter("\n");
             while(s.hasNext()) {
                 if( isError ) {
                     log.error("SQOOP EXPORT: {}", s.next());
                 } else {
                     log.info("SQOOP EXPORT: {}", s.next());
                 }
             }
         }
     }
 
 }
