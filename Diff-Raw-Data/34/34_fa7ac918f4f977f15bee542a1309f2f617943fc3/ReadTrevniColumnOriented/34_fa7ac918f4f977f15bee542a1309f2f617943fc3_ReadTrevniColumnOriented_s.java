 package edu.osu.cse.hpcs.tableplacement.trevni;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Map.Entry;
 
 import org.apache.hadoop.hive.serde2.SerDeException;
 import org.apache.hadoop.hive.serde2.columnar.BytesRefArrayWritable;
 import org.apache.hadoop.hive.serde2.columnar.ColumnarSerDeBase;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.log4j.Logger;
 import org.apache.trevni.ColumnFileReader;
 import org.apache.trevni.avro.HadoopInput;
 
 import edu.osu.cse.hpcs.tableplacement.ColumnFileGroup;
 import edu.osu.cse.hpcs.tableplacement.ReadFrom;
 import edu.osu.cse.hpcs.tableplacement.ReadFactory;
 import edu.osu.cse.hpcs.tableplacement.exception.TablePropertyException;
 import edu.osu.cse.hpcs.tableplacement.multifile.MultiFileReader;
 import edu.osu.cse.hpcs.tableplacement.multifile.RCFileMultiFileReader;
 import edu.osu.cse.hpcs.tableplacement.multifile.TrevniMultiFileReader;
 
 public class ReadTrevniColumnOriented extends ReadFrom {
   protected static Logger log = Logger.getLogger(ReadTrevniColumnOriented.class);
 
   public ReadTrevniColumnOriented(String propertyFilePath, String inputPath,
       Properties cmdProperties) throws IOException, TablePropertyException,
       SerDeException, InstantiationException, IllegalAccessException,
       ClassNotFoundException {
     super(propertyFilePath, inputPath, cmdProperties, log);
   }
   
   @Override
   public long doRead(MultiFileReader reader, Logger log)
       throws IOException, SerDeException {
     long ts;
     assert totalRowReadTimeInNano == 0;
     //assert totalRowDeserializationTimeInNano == 0;
 
     Map<String, BytesRefArrayWritable> ret = new HashMap<String, BytesRefArrayWritable>();
     List<ColumnFileGroup> groups = reader.getColumnFileGroups();
     Map<String, List<Integer>> readColumns = reader.getReadColumns();
     for (ColumnFileGroup group: groups) {
       if (!readColumns.keySet().contains(group.getName())) {
         continue;
       }
       BytesRefArrayWritable braw = new BytesRefArrayWritable(group.getColumns().size());
       braw.resetValid(group.getColumns().size());
       ret.put(group.getName(), braw);
     }
     rowCount = 0;
     long totalSerializedDataSize = 0;
     for (Entry<String, List<Integer>> groupCols: readColumns.entrySet()) {
       String groupName = groupCols.getKey();
       for (Integer col: groupCols.getValue()) {
         LongWritable rowID = new LongWritable();
         while (reader.next(rowID, groupName, col)) {
           ts = System.nanoTime();
          reader.getCurrentColumnValue(ret.get(groupName), groupName, col);
           totalRowReadTimeInNano += System.nanoTime() - ts;
           totalSerializedDataSize += ret.get(groupName).get(col).getLength();
         }
       }
     }
     log.info("Total serialized data size: " + totalSerializedDataSize);
     return totalSerializedDataSize;
   }
 
   public long read() throws IOException, SerDeException, ClassNotFoundException, InstantiationException, IllegalAccessException, TablePropertyException {
     // For localFS, it uses 
     // org.apache.hadoop.fs.ChecksumFileSystem.ChecksumFSInputChecker.ChecksumFSInputChecker.
     // But it will open and close the file for every read operation.
     // We may need to just use File instead of HadoopInput for local test.
    TrevniMultiFileReader reader = new TrevniMultiFileReader(conf, inputDir, readColumnsStr, isReadLocalFS);
     return doRead(reader, log);
   }
   
   public String getFormatName() {
     return "Trevni";
   }
 
   public static void main(String[] args) throws Exception {
     ReadTrevniColumnOriented readTrevni = (ReadTrevniColumnOriented) ReadFactory.get(args, ReadTrevniColumnOriented.class);
     readTrevni.runTest();
   }
 }
