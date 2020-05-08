 package edu.osu.cse.hpcs.tableplacement.multifile;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.hive.serde2.ColumnProjectionUtils;
 import org.apache.hadoop.hive.serde2.SerDeException;
 import org.apache.hadoop.hive.serde2.columnar.BytesRefArrayWritable;
 import org.apache.hadoop.hive.serde2.columnar.ColumnarSerDeBase;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.log4j.Logger;
 
 import edu.osu.cse.hpcs.tableplacement.ColumnFileGroup;
 import edu.osu.cse.hpcs.tableplacement.TableProperty;
 import edu.osu.cse.hpcs.tableplacement.column.Column;
 import edu.osu.cse.hpcs.tableplacement.exception.TablePropertyException;
 
 public abstract class MultiFileReader<T> {
 
   public static final Logger log = Logger.getLogger(MultiFileReader.class);
   
   protected TableProperty tableProp;
   protected FileSystem fs;
   protected Configuration conf;
   protected Path inputDir;
   protected Path[] inputFiles;
   protected List<ColumnFileGroup> columnFileGroups;
   protected Map<String, T> readers;
   protected Map<String, ColumnFileGroup> columnFileGroupsMap;
   protected Map<String, Path> columnFileGroupFiles;
   protected Map<String, List<Integer>> readColumns;
   protected Map<String, Configuration> readConf;
   protected Map<String, ColumnarSerDeBase> serdes;
 
   public MultiFileReader(Configuration conf, Path inDir,
       String readColumnsStr)
         throws IOException, ClassNotFoundException, SerDeException,
         InstantiationException, IllegalAccessException, TablePropertyException {
     tableProp = new TableProperty(conf);
     tableProp.prepareColumns();
     String serDeClassName = tableProp.get(TableProperty.SERDE_CLASS);
     if (serDeClassName == null) {
       serDeClassName = TableProperty.DEFAULT_SERDE_CLASS;
     }
     Class serDeClass = Class.forName(serDeClassName);
     columnFileGroups = tableProp.getColumnFileGroups();
     columnFileGroupsMap = new LinkedHashMap<String, ColumnFileGroup>();
     columnFileGroupFiles = new LinkedHashMap<String, Path>();
     readConf = new LinkedHashMap<String, Configuration>(); 
     inputDir = inDir;
     fs = inputDir.getFileSystem(conf);
     inputFiles = new Path[columnFileGroups.size()];
     readers = new LinkedHashMap<String, T>();
 
     for (int i=0; i<columnFileGroups.size(); i++) {
       ColumnFileGroup group = columnFileGroups.get(i);
       columnFileGroupsMap.put(group.getName(), group);
       inputFiles[i] = new Path(inputDir, group.getName());
       columnFileGroupFiles.put(group.getName(), inputFiles[i]);
     }
 
     this.readColumns = parseReadColumnMultiFileStr(
         readColumnsStr, columnFileGroupsMap);
     serdes = new LinkedHashMap<String, ColumnarSerDeBase>();
     for (Entry<String, List<Integer>> entry: readColumns.entrySet()) {
       String groupName = entry.getKey();
       Configuration thisConf = new Configuration(conf);
       ColumnProjectionUtils.setReadColumnIDs(thisConf, new ArrayList(entry.getValue()));
       readConf.put(groupName, thisConf);
       ColumnarSerDeBase serde = (ColumnarSerDeBase) serDeClass.newInstance();
       ColumnFileGroup group = columnFileGroupsMap.get(groupName);
       serde.initialize(thisConf, group.getGroupProp().getProperties());
       serdes.put(group.getName(), serde);
     }
   }
   
   /**
    * Check if there is a row for read
    * @param rowID the object used to store next row's id
    * @return true if there is a row for read
    * @throws IOException 
    */
   public abstract boolean next(LongWritable rowID) throws IOException;
   
   /**
    * Check if there is a value for read for a specific column in a
    * specific column file group 
    * @param rowID rowID the object used to store next row's id
    * @param groupName the name of the column file group
    * @param column the id of the column in the column file group
    * @return true if there is a value for read
    */
   public abstract boolean next(LongWritable rowID, String groupName, int column);
   
   /**
    * Read a row. Make sure call {@link #next(LongWritable)} first.
    * @param ret
    * @throws IOException 
    */
   public abstract void getCurrentRow(Map<String, BytesRefArrayWritable> ret) throws IOException;
   
   /**
    * Read a value of a column. Make sure call {@link #next(LongWritable)} first.
    * @param ret
    * @throws IOException 
    */
  public abstract void getCurrentColumnValue(BytesRefArrayWritable ret, String groupName, int column) throws IOException;
 
   public ColumnarSerDeBase getGroupSerDe(String groupName) {
     return serdes.get(groupName);
   }
 
   public Map<String, List<Integer>> getReadColumns() {
     return readColumns;
   }
 
   public List<ColumnFileGroup> getColumnFileGroups() {
     return columnFileGroups;
   }
   
   public abstract void close() throws IOException;
   
   /*
   public static Map<String, List<Integer>> parseReadColumnMultiFileStr(String str) {
     log.info("Parsing multi file column read string: " + str);
     Map<String, List<Integer>> ret = new HashMap<String, List<Integer>>();
     String[] groups = str.split("\\|");
     for (int i=0; i<groups.length; i++) {
       String[] tmp = groups[i].split(":");
       
       assert tmp.length == 2; // a name of the group and a string for columns
       String groupName = tmp[0];
       String[] columnIds = tmp[1].split(",");
       List<Integer> columns = new ArrayList<Integer>();
       for (int j=0; j<columnIds.length; j++) {
         columns.add(Integer.valueOf(columnIds[j]));
       }
       ret.put(groupName, columns);
     }
     return ret;
   }
   */
   public static Map<String, List<Integer>> parseReadColumnMultiFileStr(
       String str, Map<String, ColumnFileGroup> columnFileGroupMap) {
     log.info("Parsing multi file column read string: " + str);
     Map<String, List<Integer>> ret = new HashMap<String, List<Integer>>();
     String[] groups = str.split("\\|");
     for (int i=0; i<groups.length; i++) {
       String[] tmp = groups[i].split(":");
       
       assert tmp.length == 2; // a name of the group and a string for columns
       String groupName = tmp[0];
       List<Integer> columns = new ArrayList<Integer>();
       if ("all".equals(tmp[1])) {
         ColumnFileGroup group = columnFileGroupMap.get(groupName);
         for (int j=0; j<group.getColumns().size(); j++) {
           columns.add(j);
         }
       } else {
         String[] columnIds = tmp[1].split(",");
         for (int j=0; j<columnIds.length; j++) {
           columns.add(Integer.valueOf(columnIds[j]));
         }
       }
       ret.put(groupName, columns);
     }
     return ret;
   }
   
 }
