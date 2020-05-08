 package edu.osu.cse.hpcs.tableplacement.trevni;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Properties;
 
 import org.apache.hadoop.hive.serde2.SerDeException;
 import org.apache.hadoop.hive.serde2.columnar.BytesRefArrayWritable;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.log4j.Logger;
 import org.apache.trevni.ColumnFileReader;
 import org.apache.trevni.avro.HadoopInput;
 
 import edu.osu.cse.hpcs.tableplacement.ReadFromLocal;
 import edu.osu.cse.hpcs.tableplacement.ReadFromLocalFactory;
 import edu.osu.cse.hpcs.tableplacement.exception.TablePropertyException;
 
 public class ReadTrevniFromLocal extends ReadFromLocal {
   protected static Logger log = Logger.getLogger(ReadTrevniFromLocal.class);
 
   public ReadTrevniFromLocal(String propertyFilePath, String inputPath,
       Properties cmdProperties) throws IOException, TablePropertyException,
       SerDeException, InstantiationException, IllegalAccessException,
       ClassNotFoundException {
     super(propertyFilePath, inputPath, cmdProperties, log);
   }
 
   public long doRead() throws IOException, SerDeException {
     long ts;
     assert totalRowReadTimeInNano == 0;
     assert totalRowDeserializationTimeInNano == 0;
 	  
     
     log.info("FileSystem class: " + file.getFileSystem(conf).getClass().getCanonicalName());
     
     // For localFS, it uses 
     // org.apache.hadoop.fs.ChecksumFileSystem.ChecksumFSInputChecker.ChecksumFSInputChecker.
     // But it will open and close the file for every read operation.
     // We may need to just use File instead of HadoopInput for local test.
     // ColumnFileReader in = new ColumnFileReader(new HadoopInput(file, conf));
    ColumnFileReader in = new ColumnFileReader(new File(file.toUri()));
     TrevniRowReader reader = new TrevniRowReader(in, columnCount, readCols);
     
     LongWritable rowID = new LongWritable();
     BytesRefArrayWritable braw = new BytesRefArrayWritable(columnCount);
     braw.resetValid(columnCount);
     rowCount = 0;
     long totalSerializedDataSize = 0;
 
     while (reader.next(rowID)) {
       ts = System.nanoTime();
       reader.getCurrentRow(braw);
       totalRowReadTimeInNano += System.nanoTime() - ts;
       
       for (int j = 0; j < braw.size(); j++) {
         totalSerializedDataSize += braw.get(j).getLength();
       }
       
       ts = System.nanoTime();
       @SuppressWarnings("unused")
       Object row = serde.deserialize(braw);
       totalRowDeserializationTimeInNano += System.nanoTime() - ts;
       
       rowCount++;
     }
     in.close();
     log.info("Row count : " + rowCount);
     log.info("Total serialized data size: " + totalSerializedDataSize);
     return totalSerializedDataSize;
   }
   
   public String getFormatName() {
     return "Trevni";
   }
 
   public static void main(String[] args) throws Exception {
     ReadTrevniFromLocal readTrevniLocal = (ReadTrevniFromLocal) ReadFromLocalFactory.get(args, ReadTrevniFromLocal.class);
     readTrevniLocal.runTest();
   }
 }
