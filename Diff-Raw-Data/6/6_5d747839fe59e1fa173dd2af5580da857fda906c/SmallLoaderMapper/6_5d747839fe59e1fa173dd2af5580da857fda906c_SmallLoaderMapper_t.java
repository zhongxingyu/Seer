 package com.nearinfinity.bulkloader;
 
 import com.nearinfinity.hbaseclient.Index;
 import com.nearinfinity.hbaseclient.TableInfo;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.hbase.client.Put;
 import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Mapper;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.util.List;
 
 public class SmallLoaderMapper extends Mapper<LongWritable, Text, ImmutableBytesWritable, Put> {
     private static final Log LOG = LogFactory.getLog(SmallLoaderMapper.class);
     private TableInfo tableInfo = null;
     private String[] columnNames = null;
     private List<List<String>> indexColumns;
 
     @Override
     protected void setup(Context context) throws IOException, InterruptedException {
         Configuration conf = context.getConfiguration();
 
         tableInfo = BulkLoader.extractTableInfo(conf);
         columnNames = conf.get("my_columns").split(",");
         indexColumns = Index.indexForTable(tableInfo.tableMetadata());
     }
 
     @Override
     public void map(LongWritable offset, Text line, Context context) {
         try {
             List<Put> puts = BulkLoader.createPuts(line, tableInfo, columnNames, indexColumns);
             for (Put put : puts) {
                 context.write(new ImmutableBytesWritable(put.getRow()), put);
             }
 
             context.getCounter(BulkLoader.Counters.ROWS).increment(1);
         } catch (Exception e) {
             LOG.info("Error:", e);
             Writer traceWriter = new StringWriter();
             PrintWriter printWriter = new PrintWriter(traceWriter);
             e.printStackTrace(printWriter);
             context.setStatus(e.getMessage() + ". See logs for details. Stack Trace: " + traceWriter.toString());
             context.getCounter(BulkLoader.Counters.FAILED_ROWS).increment(1);
         }
     }
 }
