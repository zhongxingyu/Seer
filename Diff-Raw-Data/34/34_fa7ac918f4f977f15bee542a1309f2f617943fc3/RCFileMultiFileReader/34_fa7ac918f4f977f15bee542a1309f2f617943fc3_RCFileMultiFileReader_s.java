 package edu.osu.cse.hpcs.tableplacement.multifile;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.hive.ql.io.RCFile;
 import org.apache.hadoop.hive.ql.io.RCFile.Reader;
 import org.apache.hadoop.hive.serde2.SerDeException;
 import org.apache.hadoop.hive.serde2.columnar.BytesRefArrayWritable;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.log4j.Logger;
 
 import edu.osu.cse.hpcs.tableplacement.TableProperty;
 import edu.osu.cse.hpcs.tableplacement.exception.TablePropertyException;
 
 public class RCFileMultiFileReader extends MultiFileReader<RCFile.Reader> {
 
   private static Logger log = Logger.getLogger(RCFileMultiFileReader.class);
   
   public RCFileMultiFileReader(Configuration conf,
       Path inDir, String readColumnsStr) throws IOException,
       ClassNotFoundException, SerDeException, InstantiationException,
       IllegalAccessException, TablePropertyException {
     super(conf, inDir, readColumnsStr);
     for (String groupName: readColumns.keySet()) {
       Path file = columnFileGroupFiles.get(groupName);
       Configuration groupConf = readConf.get(groupName);
       RCFile.Reader reader = new RCFile.Reader(fs, file,
           tableProp.getInt(
               TableProperty.HADOOP_IO_BUFFER_SIZE,
               TableProperty.DEFAULT_HADOOP_IO_BUFFER_SIZE),   // IO buffer size
           groupConf,                                          // the conf for this group
           0, fs.getFileStatus(file).getLen());
       readers.put(groupName, reader);
     }
   }
 
   @Override
   public boolean next(LongWritable rowID) throws IOException {
     boolean ret = true;
     for (Entry<String, Reader> entry: readers.entrySet()) {
       RCFile.Reader reader = entry.getValue();
       ret = reader.next(rowID) && ret;
     }
     return ret;
   }
 
   @Override
   public boolean next(LongWritable rowID, String groupName, int column) {
     // not implemented right now
     return false;
   }
 
   @Override
   public void getCurrentRow(Map<String, BytesRefArrayWritable> ret) throws IOException {
     for (Entry<String, Reader> entry: readers.entrySet()) {
       String groupName = entry.getKey();
       RCFile.Reader reader = entry.getValue();
       reader.getCurrentRow(ret.get(groupName));
     }
   }
 
   @Override
  public void getCurrentColumnValue(BytesRefArrayWritable ret, String groupName, int column) {
     // not implemented right now
   }
 
   @Override
   public void close() throws IOException {
     for (Reader reader: readers.values()) {
       reader.close();
     }
   }
 
 }
