 package edu.osu.cse.hpcs.tableplacement.multifile;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.LinkedHashMap;
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
 import org.apache.trevni.ColumnFileReader;
 import org.apache.trevni.InputFile;
 import org.apache.trevni.avro.HadoopInput;
 
 import edu.osu.cse.hpcs.tableplacement.ColumnFileGroup;
 import edu.osu.cse.hpcs.tableplacement.TableProperty;
 import edu.osu.cse.hpcs.tableplacement.exception.TablePropertyException;
 import edu.osu.cse.hpcs.tableplacement.trevni.TrevniColumnReader;
 import edu.osu.cse.hpcs.tableplacement.trevni.TrevniRowReader;
 
 public class TrevniMultiFileReader extends MultiFileReader<ColumnFileReader> {
   
   private static Logger log = Logger.getLogger(TrevniMultiFileReader.class);
 
   private Map<String, TrevniRowReader> rowReaders;
   private Map<String, TrevniColumnReader> columnReaders;
 
   public TrevniMultiFileReader(Configuration conf, Path inDir,
       String readColumnsStr, boolean isReadLocalFS) throws IOException,
       ClassNotFoundException, SerDeException, InstantiationException,
       IllegalAccessException, TablePropertyException {
     super(conf, inDir, readColumnsStr);
     rowReaders = new LinkedHashMap<String, TrevniRowReader>();
     columnReaders = new LinkedHashMap<String, TrevniColumnReader>();
     for (String groupName: readColumns.keySet()) {
       ColumnFileGroup group = columnFileGroupsMap.get(groupName);
       Path file = columnFileGroupFiles.get(groupName);
       Configuration groupConf = readConf.get(groupName);
       groupConf.setInt(
               TableProperty.HADOOP_IO_BUFFER_SIZE,
               TableProperty.DEFAULT_HADOOP_IO_BUFFER_SIZE);   // IO buffer size
       ColumnFileReader reader;
       if (isReadLocalFS) {
         log.info("Local file system is used. " +
         		"Use org.apache.trevni.InputFile");
         String pathString = file.toUri().toString();
         if (pathString.startsWith("file:")) {
           pathString = pathString.substring("file:".length());
         }
         reader = new ColumnFileReader(new InputFile(new File(pathString)));
       } else {
         log.info("Distributed file system is used. " +
         		"Use org.apache.trevni.avro.HadoopInput.HadoopInput");
         reader = new ColumnFileReader(new HadoopInput(file, groupConf));
       }
       readers.put(groupName, reader);
       List<Integer> readCols = readColumns.get(groupName);
       rowReaders.put(groupName,
           new TrevniRowReader(reader, group.getColumns().size(), readCols));
       columnReaders.put(groupName,
           new TrevniColumnReader(reader, group.getColumns().size(), readCols));
     }
   }
 
   @Override
   public boolean next(LongWritable rowID) throws IOException {
     boolean ret = true;
     for (Entry<String, TrevniRowReader> entry: rowReaders.entrySet()) {
       TrevniRowReader reader = entry.getValue();
       ret = reader.next(rowID) && ret;
     }
     return ret;
   }
 
   @Override
   public boolean next(LongWritable rowID, String groupName, int column) {
     TrevniColumnReader reader = columnReaders.get(groupName);
     return reader.next(rowID, column);
   }
 
   @Override
   public void getCurrentRow(Map<String, BytesRefArrayWritable> ret)
       throws IOException {
     for (Entry<String, TrevniRowReader> entry: rowReaders.entrySet()) {
       String groupName = entry.getKey();
       TrevniRowReader reader = entry.getValue();
       BytesRefArrayWritable braw = ret.get(groupName);
       reader.getCurrentRow(braw);
     }
   }
 
   @Override
  public void getCurrentColumnValue(Map<String, BytesRefArrayWritable> ret,
       String groupName, int column) throws IOException {
     TrevniColumnReader reader = columnReaders.get(groupName);
    BytesRefArrayWritable braw = ret.get(groupName);
    reader.getCurrentColumnValue(braw, column);
   }
 
   @Override
   public void close() throws IOException {
     for (ColumnFileReader reader: readers.values()) {
       reader.close();
     }
   }
 
 }
