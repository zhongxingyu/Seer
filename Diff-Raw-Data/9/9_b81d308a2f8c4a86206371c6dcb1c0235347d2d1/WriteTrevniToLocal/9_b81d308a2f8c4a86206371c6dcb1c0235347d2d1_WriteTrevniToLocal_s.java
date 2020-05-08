 package edu.osu.cse.hpcs.tableplacement.trevni;
 
 import java.io.File;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 
 import org.apache.hadoop.fs.FSDataOutputStream;
 import org.apache.hadoop.hive.serde2.SerDeException;
 import org.apache.hadoop.hive.serde2.columnar.BytesRefArrayWritable;
 import org.apache.hadoop.hive.serde2.columnar.BytesRefWritable;
 import org.apache.log4j.Logger;
 import org.apache.trevni.ColumnFileMetaData;
 import org.apache.trevni.ColumnFileWriter;
 import org.apache.trevni.ColumnMetaData;
 import org.apache.trevni.ValueType;
 
 import edu.osu.cse.hpcs.tableplacement.WriteToLocal;
 import edu.osu.cse.hpcs.tableplacement.WriteToLocalFactory;
 import edu.osu.cse.hpcs.tableplacement.column.Column;
 import edu.osu.cse.hpcs.tableplacement.exception.TablePropertyException;
 
 /**
  * Use Trevni with Hive SerDe (Columns defined in trevni are
  * org.apache.trevni.ValueType.BYTES)
  * 
  */
 public class WriteTrevniToLocal extends WriteToLocal {
 
   public static ColumnFileMetaData createFileMeta(String codec, String checksum) {
     return new ColumnFileMetaData().setCodec(codec).setChecksum(checksum);
   }
 
   public static ColumnMetaData[] createColumnMetaData(List<Column> columns,
       int columnCount) {
     ColumnMetaData[] columnMetadata = new ColumnMetaData[columnCount];
     for (int i = 0; i < columnCount; i++) {
       Column col = columns.get(i);
       columnMetadata[i] = new ColumnMetaData(col.getName(), ValueType.BYTES);
     }
     return columnMetadata;
   }
 
   protected static Logger log = Logger.getLogger(WriteTrevniToLocal.class);
 
   public WriteTrevniToLocal(String propertyFilePath, String outputPath,
       long rowCount, Properties cmdProperties) throws IOException,
       TablePropertyException, SerDeException, InstantiationException,
       IllegalAccessException, ClassNotFoundException {
     super(propertyFilePath, outputPath, rowCount, cmdProperties, log);
   }
 
   public long doWrite() throws IOException, SerDeException {
     long totalSerializedDataSize = 0;
     long[] columnSerializedDataSize = new long[columnCount];
     long ts;
     assert totalRowGenerationTimeInNano == 0;
     assert totalRowSerializationTimeInNano == 0;
     long writeRowTime = 0;
     long writeToTime = 0;
     
     ColumnFileWriter out = new ColumnFileWriter(createFileMeta("null", "null"),
         createColumnMetaData(columns, columnCount));
     //FSDataOutputStream trevniOutputStream = localFS.create(file);
     for (long i = 0; i < rowCount; i++) {
       ts = System.nanoTime();
       List<Object> row = new ArrayList<Object>(columnCount);
       for (Column col : columns) {
         row.add(col.nextValue());
       }
       totalRowGenerationTimeInNano += System.nanoTime() - ts;
       
       ts = System.nanoTime();
       BytesRefArrayWritable bytes = (BytesRefArrayWritable) serde.serialize(
           row, rowHiveObjectInspector);
       totalRowSerializationTimeInNano += System.nanoTime() - ts;
       
       ByteBuffer[] buffer = new ByteBuffer[bytes.size()];
       for (int j = 0; j < bytes.size(); j++) {
         BytesRefWritable ref = bytes.get(j);
         int length = ref.getLength();
         totalSerializedDataSize += length;
         columnSerializedDataSize[j] += length;
         buffer[j] = ByteBuffer.wrap(ref.getData(), ref.getStart(),
             ref.getLength());
       }
       ts = System.nanoTime();
       out.writeRow((Object[]) buffer);
       writeRowTime += System.nanoTime() - ts;
     }
     ts = System.nanoTime();
     //out.writeTo(trevniOutputStream);
    out.writeTo(new File(file.toUri()));
     writeToTime += System.nanoTime() - ts;
     //trevniOutputStream.close();
 
     log.info("Total serialized data size: " + totalSerializedDataSize);
     for (int i = 0; i < columnCount; i++) {
       log.info("Column " + i + " serialized data size: "
           + columnSerializedDataSize[i]);
     }
     
     otherMeasures.put("Write row time (ms)", writeRowTime / 1000000);
     otherMeasures.put("Write to time (ms)", writeToTime / 1000000);
     
     return totalSerializedDataSize;
   }
 
   public String getFormatName() {
     return "Trevni";
   }
 
   public static void main(String[] args) throws Exception {
     WriteTrevniToLocal writeTrevniLocal = (WriteTrevniToLocal) WriteToLocalFactory
         .get(args, WriteTrevniToLocal.class);
     writeTrevniLocal.runTest();
   }
 }
