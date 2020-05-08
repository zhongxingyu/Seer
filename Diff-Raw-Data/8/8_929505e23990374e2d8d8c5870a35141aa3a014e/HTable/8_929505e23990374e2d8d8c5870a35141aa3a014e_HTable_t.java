 package pt.inescid.gsd.cachemining;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.NavigableSet;
 import java.util.Set;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.hbase.HTableDescriptor;
 import org.apache.hadoop.hbase.KeyValue;
 import org.apache.hadoop.hbase.client.Append;
 import org.apache.hadoop.hbase.client.Delete;
 import org.apache.hadoop.hbase.client.Get;
 import org.apache.hadoop.hbase.client.HTableInterface;
 import org.apache.hadoop.hbase.client.Increment;
 import org.apache.hadoop.hbase.client.Put;
 import org.apache.hadoop.hbase.client.Result;
 import org.apache.hadoop.hbase.client.ResultScanner;
 import org.apache.hadoop.hbase.client.Row;
 import org.apache.hadoop.hbase.client.RowLock;
 import org.apache.hadoop.hbase.client.RowMutations;
 import org.apache.hadoop.hbase.client.Scan;
 import org.apache.hadoop.hbase.client.coprocessor.Batch.Call;
 import org.apache.hadoop.hbase.client.coprocessor.Batch.Callback;
 import org.apache.hadoop.hbase.ipc.CoprocessorProtocol;
 import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;
 
 public class HTable implements HTableInterface {
 
     private static boolean IS_MONITORING = false;
 
    private Logger log = Logger.getLogger(HTable.class);

     private static Map<String, org.apache.hadoop.hbase.client.HTable> htables = new HashMap<String, org.apache.hadoop.hbase.client.HTable>();
 
     private static Cache<Result> cache = new Cache<Result>();
 
     private static SequenceEngine sequenceEngine = new SequenceEngine();
 
     private org.apache.hadoop.hbase.client.HTable htable;
     private File filePut, fileGet;
     private String tableName;
 
     public HTable(Configuration conf, String tableName) throws IOException {
         filePut = new File("put-operations.log");
         fileGet = new File("get-operations.log");
         this.tableName = tableName;
         htable = new org.apache.hadoop.hbase.client.HTable(conf, tableName);
         htables.put(tableName, htable);
     }
 
     public void setScannerCaching(int scannerCaching) {
         htable.setScannerCaching(scannerCaching);
     }
 
     @Override
     public Result append(Append arg0) throws IOException {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public Object[] batch(List<? extends Row> arg0) throws IOException, InterruptedException {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public void batch(List<? extends Row> arg0, Object[] arg1) throws IOException, InterruptedException {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public boolean checkAndDelete(byte[] arg0, byte[] arg1, byte[] arg2, byte[] arg3, Delete arg4) throws IOException {
         // TODO Auto-generated method stub
         return false;
     }
 
     @Override
     public boolean checkAndPut(byte[] arg0, byte[] arg1, byte[] arg2, byte[] arg3, Put arg4) throws IOException {
         // TODO Auto-generated method stub
         return false;
     }
 
     @Override
     public void close() throws IOException {
         htable.close();
 
     }
 
     @Override
     public <T extends CoprocessorProtocol, R> Map<byte[], R> coprocessorExec(Class<T> arg0, byte[] arg1, byte[] arg2,
             Call<T, R> arg3) throws IOException, Throwable {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public <T extends CoprocessorProtocol, R> void coprocessorExec(Class<T> arg0, byte[] arg1, byte[] arg2, Call<T, R> arg3,
             Callback<R> arg4) throws IOException, Throwable {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public <T extends CoprocessorProtocol> T coprocessorProxy(Class<T> arg0, byte[] arg1) {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public void delete(Delete arg0) throws IOException {
         htable.delete(arg0);
     }
 
     @Override
     public void delete(List<Delete> arg0) throws IOException {
         htable.delete(arg0);
     }
 
     @Override
     public boolean exists(Get arg0) throws IOException {
         // TODO Auto-generated method stub
         return false;
     }
 
     @Override
     public void flushCommits() throws IOException {
         htable.flushCommits();
     }
 
     @Override
     public Result get(Get get) throws IOException {

        log.info("get called (row: " + get.getRow() + ")");

         if (IS_MONITORING) {
             Result result = htable.get(get);
             FileWriter fw = new FileWriter(fileGet.getAbsoluteFile(), true);
             BufferedWriter bw = new BufferedWriter(fw);
 
             long ts = System.currentTimeMillis();
             Set<byte[]> families = get.familySet();
             for (byte[] f : families) {
                 NavigableSet<byte[]> qualifiers = get.getFamilyMap().get(f);
                 if (qualifiers != null) {
                     for (byte[] q : qualifiers) {
                         bw.write(ts + ":" + tableName + ":" + Bytes.toInt(get.getRow()) + ":" + Bytes.toString(f) + ":"
                                 + Bytes.toString(q));
                         bw.newLine();
                     }
                 } else {
                     bw.write(ts + ":" + tableName + ":" + Bytes.toInt(get.getRow()) + ":" + Bytes.toString(f));
                     bw.newLine();
                 }
             }
             bw.close();
             return result;
         }
 
         Result result = null;
         // build key
         final String tableName = this.tableName;
         final String row = Bytes.toString(get.getRow());
         String colFamily = null;
         String colQualifier = null;
         Set<byte[]> families = get.familySet();
         for (byte[] family : families) {
             colFamily = Bytes.toString(family);
             NavigableSet<byte[]> qualifiers = get.getFamilyMap().get(family);
             if (qualifiers != null) {
                 for (byte[] qualifier : qualifiers) {
                     colQualifier = Bytes.toString(qualifier);
                     break;
                 }
                 // FIXME
                 break;
             }
         }
         String key = tableName + SequenceEngine.SEPARATOR + colFamily;
         if (colQualifier != null)
             key += SequenceEngine.SEPARATOR + colQualifier;
 
         CacheEntry<Result> entry = cache.get(key);
         if (entry == null) {
             Set<String> sequence = sequenceEngine.getSequence(key);
             if (sequence == null) {
                 result = htable.get(get);
             } else {
                 // batch updates to the same tables
                 Map<String, Get> gets = new HashMap<String, Get>();
                 for (String container : sequence) {
 
                     String[] elements = container.split(":");
                     String containerTableName = elements[0];
                     String containerColFamily = elements[1];
                     String containerColQualifier = elements.length == 3 ? elements[2] : "";
 
                     Get g = gets.get(containerTableName);
                     if (g == null) {
                         g = new Get(get.getRow());
                     }
                     g.addColumn(Bytes.toBytes(containerColFamily), Bytes.toBytes(containerColQualifier));
                     gets.put(containerTableName, g);
                 }
                 for (String k : gets.keySet()) {
                     cache.put(k, new CacheEntry<Result>(htables.get(k).get(gets.get(k))));
                 }
             }
             result = htable.get(get);
         } else {
             result = entry.getResult();
         }
 
         return result;
     }
 
     @Override
     public Result[] get(List<Get> arg0) throws IOException {
         return htable.get(arg0);
     }
 
     @Override
     public Configuration getConfiguration() {
         return htable.getConfiguration();
     }
 
     @Override
     public Result getRowOrBefore(byte[] arg0, byte[] arg1) throws IOException {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public ResultScanner getScanner(Scan arg0) throws IOException {
         return htable.getScanner(arg0);
     }
 
     @Override
     public ResultScanner getScanner(byte[] arg0) throws IOException {
         return htable.getScanner(arg0);
     }
 
     @Override
     public ResultScanner getScanner(byte[] arg0, byte[] arg1) throws IOException {
         return htable.getScanner(arg0, arg1);
     }
 
     @Override
     public HTableDescriptor getTableDescriptor() throws IOException {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public byte[] getTableName() {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public long getWriteBufferSize() {
         // TODO Auto-generated method stub
         return 0;
     }
 
     @Override
     public Result increment(Increment arg0) throws IOException {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public long incrementColumnValue(byte[] arg0, byte[] arg1, byte[] arg2, long arg3) throws IOException {
         // TODO Auto-generated method stub
         return 0;
     }
 
     @Override
     public long incrementColumnValue(byte[] arg0, byte[] arg1, byte[] arg2, long arg3, boolean arg4) throws IOException {
         // TODO Auto-generated method stub
         return 0;
     }
 
     @Override
     public boolean isAutoFlush() {
         return htable.isAutoFlush();
     }
 
     @Override
     public RowLock lockRow(byte[] arg0) throws IOException {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public void mutateRow(RowMutations arg0) throws IOException {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public void put(Put put) throws IOException {
         htable.put(put);
         if (IS_MONITORING) {
             FileWriter fw = new FileWriter(filePut.getAbsoluteFile(), true);
             BufferedWriter bw = new BufferedWriter(fw);
             long ts = System.currentTimeMillis();
             Set<byte[]> families = put.getFamilyMap().keySet();
             for (byte[] f : families) {
                 List<KeyValue> qualifiers = put.getFamilyMap().get(f);
                 for (KeyValue q : qualifiers) {
                     bw.write(ts + ":" + tableName + ":" + Bytes.toInt(put.getRow()) + ":" + Bytes.toString(f) + ":"
                             + Bytes.toString(q.getQualifier()));
                     bw.newLine();
                 }
             }
             bw.close();
         }
     }
 
     @Override
     public void put(List<Put> arg0) throws IOException {
         htable.put(arg0);
     }
 
     @Override
     public void setAutoFlush(boolean arg0) {
         htable.setAutoFlush(arg0);
     }
 
     @Override
     public void setAutoFlush(boolean arg0, boolean arg1) {
         htable.setAutoFlush(arg0, arg1);
     }
 
     @Override
     public void setWriteBufferSize(long arg0) throws IOException {
         htable.setWriteBufferSize(arg0);
 
     }
 
     @Override
     public void unlockRow(RowLock arg0) throws IOException {
         // TODO Auto-generated method stub
 
     }
 
 }
