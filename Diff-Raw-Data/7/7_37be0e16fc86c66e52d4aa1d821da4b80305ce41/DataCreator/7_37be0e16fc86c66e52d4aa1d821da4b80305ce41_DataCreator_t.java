 package com.nearinfinity.datacreator;
 
 import com.github.javafaker.Faker;
 import org.apache.commons.lang.StringUtils;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.FSDataOutputStream;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.Mapper;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
 
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Random;
 
 public class DataCreator {
 
     private static final String DATA_TYPE_LIST = "data_type_list";
     private static final String NUM_ROWS = "num_rows";
     private static final DateFormat DF = new SimpleDateFormat("yyyyMMdd-HHmmss");
 
     private static class DataCreatorMapper extends Mapper<LongWritable, Text, Text, Text> {
         private Faker faker;
         private Random random;
         private DataType[] dataTypes;
         private int numRows;
 
         @Override
         public void setup(Context context) throws IOException, InterruptedException {
             this.faker = new Faker();
             this.random = new Random();
 
             Configuration conf = context.getConfiguration();
             this.numRows = Integer.parseInt(conf.get(NUM_ROWS));
 
             String[] dataTypeStrings = conf.get(DATA_TYPE_LIST).split(",");
             this.dataTypes = getDataTypeArray(dataTypeStrings);
         }
 
         @Override
         public void map(LongWritable key, Text val, Context context) throws IOException, InterruptedException {
             for (int i = 0; i < numRows; i++) {
                 List<String> dataList = new ArrayList<String>();
                 for (DataType dt : dataTypes) {
                     dataList.add(createData(dt));
                 }
 
                 String row = StringUtils.join(dataList, ",");
 
                context.write(new Text(row), null);
             }
         }
 
         private String createData(DataType dataType) {
             String ret = null;
             switch (dataType) {
                 case NAME:
                     ret = faker.name();
                     break;
                 case ADDRESS:
                     ret = faker.streetAddress(false);
                     break;
                 case PHONE:
                     ret = faker.phoneNumber();
                     break;
                 case LONG:
                     ret = Double.toString(random.nextLong());
                     break;
                 case DOUBLE:
                     ret = Double.toString(random.nextDouble());
                     break;
                 case FK:
                     ret = Integer.toString(random.nextInt(10));
                     break;
             }
             return ret;
         }
 
         private DataType[] getDataTypeArray(String[] dataTypeStrings) {
             DataType[] dataTypes = new DataType[dataTypeStrings.length];
             for (int i = 0; i < dataTypeStrings.length; i++) {
                 dataTypes[i] = DataType.valueOf(dataTypeStrings[i].toUpperCase());
             }
             return dataTypes;
         }
     }
 
     public static void main(String[] args) throws Exception {
         if (args.length < 3) {
             System.out.println("Usage: DataCreator.jar <iterations> <num_rows> <data_type_list>");
             System.exit(-1);
         }
         int iterations = Integer.parseInt(args[0]);
         String numRows = args[1];
         String dataTypeList = args[2];
         String outputPath = buildOutputPath(numRows);
 
         Configuration conf = new Configuration();
         conf.set(DATA_TYPE_LIST, dataTypeList);
         conf.set(NUM_ROWS, numRows);
 
         Job job = new Job(conf);
         job.setJarByClass(DataCreator.class);
         job.setJobName("Data Creator");
 
         Path inputPath = createInputFolder(iterations, conf);
 
         FileInputFormat.addInputPath(job, inputPath);
         FileOutputFormat.setOutputPath(job, new Path(outputPath));
 
         job.setOutputFormatClass(TextOutputFormat.class);
 
         job.setMapperClass(DataCreatorMapper.class);
         job.setNumReduceTasks(0);
 
         job.setMapOutputKeyClass(Text.class);
         job.setMapOutputValueClass(Text.class);
 
         if (!job.waitForCompletion(true)) {
             System.out.println("Error in running the job!");
             System.exit(1);
         }
 
         System.out.println("Successfully completed job with output path: " + outputPath);
 
         // Delete temporary output folder after completion
         FileSystem fileSystem = FileSystem.get(conf);
         fileSystem.delete(inputPath, true);
         fileSystem.close();
     }
 
     private static String buildOutputPath(String numRows) {
         return "created_data/" + DF.format(new Date()) + "-" + numRows;
     }
 
     private static Path createInputFolder(int num_files, Configuration conf) throws Exception {
         Path path = new Path("DataCreator-input-" + System.currentTimeMillis() + ".tmp");
         FileSystem fileSystem = FileSystem.get(conf);
         fileSystem.mkdirs(path);
 
         FSDataOutputStream os;
         for (int i = 0; i < num_files; i++) {
             os = fileSystem.create(path.suffix("/" + i));
             os.writeBytes("foo bah\n");
         }
         fileSystem.close();
         return path;
     }
 }
