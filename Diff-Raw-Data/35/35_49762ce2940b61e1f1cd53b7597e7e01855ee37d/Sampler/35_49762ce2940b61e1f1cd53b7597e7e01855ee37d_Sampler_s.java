 package edu.umn.cs.spatialHadoop.operations;
 
 import java.io.DataInputStream;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Random;
 
 import org.apache.hadoop.fs.FSDataInputStream;
 import org.apache.hadoop.fs.FileStatus;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.fs.PathFilter;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.MemoryInputStream;
 import org.apache.hadoop.io.NullWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.io.Text2;
 import org.apache.hadoop.io.TextSerializable;
 import org.apache.hadoop.mapred.ClusterStatus;
 import org.apache.hadoop.mapred.Counters;
 import org.apache.hadoop.mapred.Counters.Counter;
 import org.apache.hadoop.mapred.JobClient;
 import org.apache.hadoop.mapred.JobConf;
 import org.apache.hadoop.mapred.MapReduceBase;
 import org.apache.hadoop.mapred.Mapper;
 import org.apache.hadoop.mapred.OutputCollector;
 import org.apache.hadoop.mapred.Reporter;
 import org.apache.hadoop.mapred.RunningJob;
 import org.apache.hadoop.mapred.Task;
 import org.apache.hadoop.mapred.TextOutputFormat;
 import org.apache.hadoop.mapred.spatial.ShapeLineInputFormat;
 import org.apache.hadoop.spatial.CellInfo;
 import org.apache.hadoop.spatial.Point;
 import org.apache.hadoop.spatial.RTree;
 import org.apache.hadoop.spatial.Rectangle;
 import org.apache.hadoop.spatial.ResultCollector;
 import org.apache.hadoop.spatial.Shape;
 import org.apache.hadoop.spatial.SpatialSite;
 import org.apache.hadoop.util.LineReader;
 
 import edu.umn.cs.spatialHadoop.CommandLineArguments;
 
 /**
  * Reads a random sample of a file.
  * @author Ahmed Eldawy
  *
  */
 public class Sampler {
 
   /**Name of the configuration line for sample ratio*/
   private static final String SAMPLE_RATIO = "sampler.SampleRatio";
   /**The threshold in number of samples after which the BIG version is used*/
   private static final int BIG_SAMPLE = 10000;
   
   private static final String InClass = "sampler.InClass";
   private static final String OutClass = "sampler.OutClass";
   
   /**Random seed to use by all mappers to ensure unique result per seed*/
   private static final String RANDOM_SEED = "sampler.RandomSeed";
   
   public static class Map extends MapReduceBase implements
   Mapper<CellInfo, Text, NullWritable, Text> {
 
     /**Ratio of lines to sample*/
     private double sampleRatio;
     
     /**A dummy key for all keys*/
     private final NullWritable dummyKey = NullWritable.get();
     
     /**Random number generator to use*/
     private Random random;
 
     private Shape inShape;
     
     enum Conversion {None, ShapeToPoint, ShapeToRect};
     Conversion conversion;
     
     @Override
     public void configure(JobConf job) {
       sampleRatio = job.getFloat(SAMPLE_RATIO, 0.01f);
       random = new Random(job.getLong(RANDOM_SEED, System.currentTimeMillis()));
       try {
         Class<? extends TextSerializable> inClass =
             job.getClass(InClass, null).asSubclass(TextSerializable.class);
         Class<? extends TextSerializable> outClass =
             job.getClass(OutClass, null).asSubclass(TextSerializable.class);
 
         if (inClass == outClass) {
           conversion = Conversion.None;
         } else {
           TextSerializable inObj = inClass.newInstance();
           TextSerializable outObj = outClass.newInstance();
 
           if (inObj instanceof Shape && outObj instanceof Point) {
             inShape = (Shape) inObj;
             conversion = Conversion.ShapeToPoint;
           } else if (inObj instanceof Shape && outObj instanceof Rectangle) {
             inShape = (Shape) inObj;
             conversion = Conversion.ShapeToRect;
           } else if (outObj instanceof Text) {
             conversion = Conversion.None;
           } else {
             throw new RuntimeException("Don't know how to convert from: "+
                 inClass+" to "+outClass);
           }
         }
       } catch (InstantiationException e) {
         e.printStackTrace();
       } catch (IllegalAccessException e) {
         e.printStackTrace();
       }
     }
     
     public void map(CellInfo cell, Text line,
         OutputCollector<NullWritable, Text> output, Reporter reporter)
             throws IOException {
       if (random.nextFloat() < sampleRatio) {
         switch (conversion) {
         case None:
           output.collect(dummyKey, line);
           break;
         case ShapeToPoint:
           inShape.fromText(line);
           Point center = inShape.getMBR().getCenterPoint();
           line.clear();
           center.toText(line);
           output.collect(dummyKey, line);
           break;
         case ShapeToRect:
           inShape.fromText(line);
           Rectangle mbr = inShape.getMBR();
           line.clear();
           mbr.toText(line);
           output.collect(dummyKey, line);
           break;
         }
       }
     }
   }
 
   public static <T extends TextSerializable, O extends TextSerializable> int sampleLocalWithRatio(
       FileSystem fs, Path[] files, double ratio, long seed,
       final ResultCollector<O> output, T inObj, O outObj) throws IOException {
     long total_size = 0;
     for (Path file : files) {
       total_size += fs.getFileStatus(file).getLen();
     }
     return sampleLocalWithSize(fs, files, (long) (total_size * ratio), seed,
         output, inObj, outObj);
   }
 
   /**
    * Sample a ratio of the file through a MapReduce job
    * @param fs
    * @param files
    * @param ratio
    * @param threshold - Maximum number of elements to be sampled
    * @param output
    * @param inObj
    * @return
    * @throws IOException
    */
   public static <T extends TextSerializable, O extends TextSerializable> int sampleMapReduceWithRatio(
       FileSystem fs, Path[] files, double ratio, long threshold, long seed,
       final ResultCollector<O> output, T inObj, O outObj) throws IOException {
     JobConf job = new JobConf(FileMBR.class);
     
     Path outputPath;
     FileSystem outFs = FileSystem.get(job);
     do {
       outputPath = new Path("/"+files[0].getName()+
           ".sample_"+(int)(Math.random()*1000000));
     } while (outFs.exists(outputPath));
     
     job.setJobName("Sample");
     job.setMapOutputKeyClass(NullWritable.class);
     job.setMapOutputValueClass(Text.class);
     job.setClass(InClass, inObj.getClass(), TextSerializable.class);
     job.setClass(OutClass, outObj.getClass(), TextSerializable.class);
     
     job.setMapperClass(Map.class);
     job.setLong(RANDOM_SEED, seed);
     job.setFloat(SAMPLE_RATIO, (float) ratio);
 
     ClusterStatus clusterStatus = new JobClient(job).getClusterStatus();
     job.setNumMapTasks(clusterStatus.getMaxMapTasks() * 5);
     job.setNumReduceTasks(0);
     
     job.setInputFormat(ShapeLineInputFormat.class);
     job.setOutputFormat(TextOutputFormat.class);
     
     ShapeLineInputFormat.setInputPaths(job, files);
     TextOutputFormat.setOutputPath(job, outputPath);
     
     // Submit the job
     RunningJob run_job = JobClient.runJob(job);
     
     Counters counters = run_job.getCounters();
     Counter outputRecordCounter = counters.findCounter(Task.Counter.MAP_OUTPUT_RECORDS);
     final long resultCount = outputRecordCounter.getValue();
     // Ratio of records to return from output based on the threshold
     // Note that any number greater than or equal to one will cause all
     // elements to be returned
     final double selectRatio = (double)threshold / resultCount;
     
     // Read job result
     int result_size = 0;
     if (output != null) {
       Text line = new Text();
       FileStatus[] results = outFs.listStatus(outputPath);
       
       for (FileStatus fileStatus : results) {
         if (fileStatus.getLen() > 0 && fileStatus.getPath().getName().startsWith("part-")) {
           LineReader lineReader = new LineReader(outFs.open(fileStatus.getPath()));
           try {
             while (lineReader.readLine(line) > 0) {
               if (Math.random() < selectRatio) {
                 if (output != null) {
                   outObj.fromText(line);
                   output.collect(outObj);
                 }
                 result_size++;
               }
             }
           } catch (RuntimeException e) {
             e.printStackTrace();
           }
           lineReader.close();
         }
       }
     }
     
     outFs.delete(outputPath, true);
     
     return result_size;
   }
 
   /**
    * Records as many records as wanted until the total size of the text
    * serialization of sampled records exceed the given limit
    * @param fs
    * @param files
    * @param total_size
    * @param output
    * @param inObj
    * @return
    * @throws IOException
    */
   public static <T extends TextSerializable, O extends TextSerializable> int
     sampleLocalWithSize(
       FileSystem fs, Path[] files, long total_size, long seed,
       final ResultCollector<O> output, final T inObj, final O outObj)
       throws IOException {
     int average_record_size = 1024; // A wild guess for record size
     final LongWritable current_sample_size = new LongWritable();
     int sample_count = 0;
 
     final ResultCollector<T> converter = createConverter(output, inObj, outObj);
 
     final ResultCollector<Text2> counter = new ResultCollector<Text2>() {
       @Override
       public void collect(Text2 r) {
         current_sample_size.set(current_sample_size.get() + r.getLength());
         inObj.fromText(r);
         converter.collect(inObj);
       }
     };
 
     while (current_sample_size.get() < total_size) {
       int count = (int) ((total_size - current_sample_size.get()) / average_record_size);
       if (count < 10)
         count = 10;
       
       if (count < BIG_SAMPLE) {
         
         sample_count += sampleLocalByCount(fs, files, count, seed, counter, new Text2(), new Text2());
         // Change the seed to get different sample next time.
         // Still we need to ensure that repeating the program will generate
         // the same value
         seed += sample_count;
       } else {
         sample_count += sampleLocalBig(fs, files, count, seed, counter, new Text2(), new Text2());
         seed += sample_count;
       }
       // Update average_records_size
       average_record_size = (int) (current_sample_size.get() / sample_count);
     }
     return sample_count;
   }
 
   /**
    * Creates a proxy ResultCollector that takes as input objects of type T
    * and converts them to objects of type O.
    * It returns an object with a collect method that takes as input an object
    * of type T (i.e., inObj). This object converts the given object to the
    * type O (i.e., outObj) and sends the result to the method in output#collect.
    * @param <O>
    * @param <T>
    * @param output
    * @param inObj
    * @param outObj
    * @return
    */
   private static <O extends TextSerializable, T extends TextSerializable> ResultCollector<T> createConverter(
       final ResultCollector<O> output, T inObj, final O outObj) {
     if (output == null)
       return null;
     if (inObj.getClass() == outObj.getClass()) {
       return new ResultCollector<T>() {
         @Override
         public void collect(T r) {
           output.collect((O) r);
         }
       };
     } else if (inObj instanceof Shape && outObj instanceof Point) {
       final Point out_pt = (Point) outObj;
       return new ResultCollector<T>() {
         @Override
         public void collect(T r) {
           Point pt = ((Shape)r).getMBR().getCenterPoint();
           out_pt.x = pt.x;
           out_pt.y = pt.y;
           output.collect(outObj);
         }
       };
     } else if (inObj instanceof Shape && outObj instanceof Rectangle) {
       final Rectangle out_rect = (Rectangle) outObj;
       return new ResultCollector<T>() {
         @Override
         public void collect(T r) {
           out_rect.set((Shape)r);
           output.collect(outObj);
         }
       };
     } else if (outObj instanceof Text) {
       final Text text = (Text) outObj;
       return new ResultCollector<T>() {
         @Override
         public void collect(T r) {
           text.clear();
           r.toText(text);
           output.collect(outObj);
         }
       };
     } else if (inObj instanceof Text) {
       final Text text = (Text) inObj;
       return new ResultCollector<T>() {
         @Override
         public void collect(T r) {
           outObj.fromText(text);
           output.collect(outObj);
         }
       };
     } else {
       throw new RuntimeException("Cannot convert from " + inObj.getClass()
           + " to " + outObj.getClass());
     }
   }
 
   private static final PathFilter hiddenFileFilter = new PathFilter(){
     public boolean accept(Path p){
       String name = p.getName(); 
       return !name.startsWith("_") && !name.startsWith("."); 
     }
   }; 
 
   
   public static <T extends TextSerializable, O extends TextSerializable>
   int sampleLocal(FileSystem fs, Path file, int count, long seed,
       ResultCollector<O> output, T inObj, O outObj) throws IOException {
    if (fs.getFileStatus(file).isDir()) {
      // Retrieve all visible files in the directory
      FileStatus[] fileStatus = fs.listStatus(file, hiddenFileFilter);
      Path[] paths = new Path[fileStatus.length];
      return sampleLocalByCount(fs, paths, count, seed, output, inObj, outObj);
    } else {
      return sampleLocalByCount(fs, new Path[] {file}, count, seed, output, inObj, outObj);
    }
   }
   
   /**
    * Reads a sample of the given file and returns the number of items read.
    * 
    * @param fs
    * @param file
    * @param count
    * @return
    * @throws IOException
    */
   public static <T extends TextSerializable, O extends TextSerializable>
   int sampleLocalByCount(
       FileSystem fs, Path[] files, int count, long seed,
       ResultCollector<O> output, T inObj, O outObj) throws IOException {
     ResultCollector<T> converter = createConverter(output, inObj, outObj);
     long[] files_start_offset = new long[files.length+1]; // Prefix sum of files sizes
     long total_length = 0;
     for (int i_file = 0; i_file < files.length; i_file++) {
       files_start_offset[i_file] = total_length;
       total_length += fs.getFileStatus(files[i_file]).getLen();
     }
     files_start_offset[files.length] = total_length;
 
     // Generate offsets to read from and make sure they are ordered to minimize
     // seeks between different HDFS blocks
     Random random = new Random(seed);
     long[] offsets = new long[count];
     for (int i = 0; i < offsets.length; i++) {
       offsets[i] = Math.abs(random.nextLong()) % total_length;
     }
     Arrays.sort(offsets);
 
     int record_i = 0; // Number of records read so far
     int records_returned = 0;
 
     int file_i = 0; // Index of the current file being sampled
     while (record_i < count) {
       // Skip to the file that contains the next sample
       while (offsets[record_i] > files_start_offset[file_i+1])
         file_i++;
 
       // Open a stream to the current file and use it to read all samples
       // in this file
       FSDataInputStream current_file_in = fs.open(files[file_i]);
       long current_file_size = files_start_offset[file_i+1] - files_start_offset[file_i];
 
       // Keep sampling as long as records offsets are within this file
       while (record_i < count &&
           (offsets[record_i] -= files_start_offset[file_i]) < current_file_size) {
         // Seek to this block and check its type
         current_file_in.seek(offsets[record_i]);
         // The start and end offsets of data within this block
         // offsets are calculated relative to file start
         long data_start_offset = 0;
         if (current_file_in.readLong() == SpatialSite.RTreeFileMarker) {
           // This file is an RTree file. Update the start offset to point
           // to the first byte after the header
           data_start_offset = RTree.getHeaderSize(current_file_in);
         } 
         // Get the end offset of data by searching for the beginning of the
         // last line
         long data_end_offset = files_start_offset[file_i]
             - (file_i == 0 ? 0 : files_start_offset[file_i - 1]);
         // Skip the last line too to ensure to ensure that the mapped position
         // will be before some line in the block
         current_file_in.seek(data_end_offset);
         data_end_offset = Tail.tail(current_file_in, 1, null, null);
         long block_fill_size = data_end_offset - data_start_offset;
 
         // Map file position to element index in this tree assuming fixed
         // size records
         long element_offset_in_file =
             (offsets[record_i] - data_start_offset) *
             block_fill_size / (data_end_offset - data_start_offset) + data_start_offset;
         current_file_in.seek(element_offset_in_file);
         LineReader reader = new LineReader(current_file_in, 4096);
         // Read the first line after that offset
         Text line = new Text();
         reader.readLine(line); // Skip the rest of the current line
         reader.readLine(line); // Read next line
 
         // Report this element to output
         if (converter != null) {
           inObj.fromText(line);
           converter.collect(inObj);
         }
         record_i++;
         records_returned++;
       }
       current_file_in.close();
     }
     return records_returned;
   }
   
   /**
    * Reads a relatively big sample of the file.
    * @param fs
    * @param file
    * @param count
    * @return
    * @throws IOException 
    */
   public static <T extends TextSerializable, O extends TextSerializable>
   int sampleLocalBig(
       FileSystem fs, Path[] files, int count, long seed,
       ResultCollector<O> output, T inObj, O outObj) throws IOException {
     if (fs.getGlobalIndex(fs.getFileStatus(files[0])) == null)
       return sampleLocalByCount(fs, files, count, seed, output, inObj, outObj);
     ResultCollector<T> converter = createConverter(output, inObj, outObj);
     long[] files_start_offset = new long[files.length+1]; // Prefix sum of files sizes
     long total_length = 0;
     for (int i_file = 0; i_file < files.length; i_file++) {
       files_start_offset[i_file] = total_length;
       total_length += fs.getFileStatus(files[i_file]).getLen();
     }
     files_start_offset[files.length] = total_length;
     
     // Generate offsets to read from and make sure they are ordered to minimize
     // seeks between different HDFS blocks
     long[] offsets = new long[count];
     Random random = new Random(seed);
     for (int i = 0; i < offsets.length; i++) {
       offsets[i] = Math.abs(random.nextLong()) % total_length;
     }
     Arrays.sort(offsets);
 
     int record_i = 0; // Number of records read so far
     int records_returned = 0;
     // A temporary text to store one line
     Text line = new Text();
     
     int file_i = 0; // Index of the current file being sampled
     while (record_i < count) {
       // Skip to the file that contains the next sample
       while (offsets[record_i] > files_start_offset[file_i+1])
         file_i++;
 
       // Open a stream to the current file and use it to read all samples
       // in this file
       FSDataInputStream current_file_in = fs.open(files[file_i]);
       long current_file_size = files_start_offset[file_i+1] - files_start_offset[file_i];
       long current_file_block_size = fs.getFileStatus(files[file_i]).getBlockSize();
       byte[] block_data = new byte[(int) current_file_block_size];
       
       // Keep sampling as long as records offsets are within this file
       while (record_i < count &&
           (offsets[record_i] -= files_start_offset[file_i]) < current_file_size) {
         long current_block_start_offset = offsets[record_i] -
             (offsets[record_i] % current_file_block_size);
         long current_block_end_offset = Math.min(current_file_size,
             current_block_start_offset + current_file_block_size);
         int current_block_size =
             (int) (current_block_end_offset - current_block_start_offset);
 
         // Calculate the start and end offset of record data within the block
         // Both offsets are calculated relative to this block
         
         // Initially, start and end are matched with block data boundaries
         // i.e., all the block contains data
         int data_start_offset = 0;
         int data_end_offset = current_block_size;
 
         // Seek to this block and check its type
         if (current_file_in.getPos() != current_block_start_offset)
           current_file_in.seek(current_block_start_offset);
         
         // Read the whole block in memory
         current_file_in.readFully(block_data, 0, data_end_offset);
         
         // Check if this block is an RTree
         int i = 0;
         while (i < SpatialSite.RTreeFileMarkerB.length &&
             SpatialSite.RTreeFileMarkerB[i] == block_data[i]) {
           i++;
         }
 
         // If RTree, update data_start_offset to point right after the index
         if (i == SpatialSite.RTreeFileMarkerB.length) {
           DataInputStream temp_is =
               new DataInputStream(new MemoryInputStream(block_data));
           // This block is an RTree block. Update the start offset to point
           // to the first byte after the header
           data_start_offset = RTree.getHeaderSize(temp_is);
         } 
         // Get the end offset of data by searching for the last non-empty line
         // We perform an exponential search starting from the last offset in
         // block. This assumes that all empty lines occur at the end
         int check_offset = 1;
         while (check_offset < data_end_offset) {
           int check_position = data_end_offset - check_offset * 2;
           if (block_data[check_position] != '\n' || block_data[check_position+1] != '\n') {
             // We found a non-empty line. Perform a binary search till we find
             // the last non-empty line
             int l = data_end_offset - check_offset * 2;
             int h = data_end_offset - check_offset;
             while (l < h) {
               int m = (l + h) / 2;
               if (block_data[m] == '\n' && block_data[m+1] == '\n'){
                 // This is an empty line, check before that
                 h = m-1;
               } else {
                 // This is a non-empty line, check after that
                 l = m+1;
               }
             }
             // Skip last line to ensure that the mapped offset falls BEFORE
             // the start of the last line not IN the last line
             do {
               l--;
             } while (block_data[l] != '\n' && block_data[l] != '\r');
             data_end_offset = l;
             break;
           }
           check_offset *= 2;
         }
 
         
         long block_fill_size = data_end_offset - data_start_offset;
 
         // Consider all positions in this block
         while (record_i < count &&
             offsets[record_i] < current_block_end_offset) {
           // Map file position to element index in this tree assuming fixed
           // size records
           int element_offset_in_block = data_start_offset + (int)
               ((offsets[record_i] - current_block_start_offset) *
                   block_fill_size / current_block_size);
           
           int bol = RTree.skipToEOL(block_data, element_offset_in_block);
           int eol = RTree.skipToEOL(block_data, bol);
           // Skip empty line
           if (eol - bol < 2) {
             // Skip this line
             record_i++;
             continue;
           }
           line.set(block_data, bol, eol - bol);
 
           // Report this element to output
           if (converter != null) {
             inObj.fromText(line);
             converter.collect(inObj);
           }
           record_i++;
           records_returned++;
         }
       }
       current_file_in.close();
     }
     return records_returned;
   }
 
   public static void main(String[] args) throws IOException {
     CommandLineArguments cla = new CommandLineArguments(args);
     JobConf conf = new JobConf(Sampler.class);
     Path[] inputFiles = cla.getPaths();
     FileSystem fs = inputFiles[0].getFileSystem(conf);
     
     if (!fs.exists(inputFiles[0])) {
       throw new RuntimeException("Input file does not exist");
     }
     
     int count = cla.getCount();
     long size = cla.getSize();
     double ratio = cla.getSelectionRatio();
     long seed = cla.getSeed();
     TextSerializable stockObject = cla.getShape(true);
     if (stockObject == null)
       stockObject = new Text2();
 
     TextSerializable outputShape = cla.getOutputShape();
     if (outputShape == null)
       outputShape = new Text2();
     
     ResultCollector<TextSerializable> output =
     new ResultCollector<TextSerializable>() {
       @Override
       public void collect(TextSerializable value) {
         System.out.println(value.toText(new Text()));
       }
     };
     
     if (size != 0) {
       sampleLocalWithSize(fs, inputFiles, size, seed, output, stockObject, outputShape);
     } else if (ratio != -1.0) {
       long threshold = count == 1? Long.MAX_VALUE : count;
       sampleMapReduceWithRatio(fs, inputFiles, ratio, threshold, seed,
           output, stockObject, outputShape);
     } else {
       if (count < BIG_SAMPLE) {
         // Good for small files and the only way to sample heap files
         sampleLocalByCount(fs, inputFiles, count, seed, output, stockObject, outputShape);
       } else {
         sampleLocalBig(fs, inputFiles, count, seed, output, stockObject, outputShape);
       }
     }
   }
 }
