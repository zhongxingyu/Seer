 package com.dsp.ass3;
 
 import java.io.IOException;
 import java.util.logging.Logger;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.Mapper;
 import org.apache.hadoop.mapreduce.Partitioner;
 import org.apache.hadoop.mapreduce.Reducer;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 
 
 public class Headers {
     private static String attributeHeader = "@ATTRIBUTE",
             attributeType = "INTEGER";
 
     private static final Logger logger = Utils.setLogger(Logger.getLogger(Headers.class.getName()));
 
 
     // Write { dep-tree, i1, i2 : 1 }
     public static class MapClass extends Mapper<LongWritable, Text, Text, LongWritable> {
 
         private Text newKey = new Text();
         private LongWritable one = new LongWritable(1);
 
         @Override
         public void map(LongWritable key, Text value, Context context)
             throws IOException, InterruptedException {
 
             // Split data by tabs.
             String[] data = value.toString().split(Utils.biarcDelim);
 
             // Fetch dep-tree indexes.
             String indexes = data[3].substring(0, data[3].lastIndexOf(Utils.delim));
 
             newKey.set(data[2] + Utils.biarcDelim + indexes);
             context.write(newKey, one);
         }
     }
 
 
     // Partition by key hash code.
     public static class PartitionerClass extends Partitioner<Text, LongWritable> {
         @Override
         public int getPartition(Text key, LongWritable value, int numPartitions) {
             return (key.hashCode() & Integer.MAX_VALUE) % numPartitions;
         }
     }
 
 
     // Sum value longs.
     public static class CombineClass extends Reducer<Text, LongWritable, Text, LongWritable> {
         private LongWritable newValue = new LongWritable();
 
         @Override
         public void reduce(Text key, Iterable<LongWritable> values, Context context)
             throws IOException, InterruptedException {
 
             newValue.set(Utils.sumValues(values));
             context.write(key, newValue);
         }
     }
 
 
     // Write @ATTRIBUTE 'dep-tree' IINTEGER for every dep-tree.
     // FILTER by DpMin.
     public static class ReduceClass extends Reducer<Text, LongWritable, Text, Text> {
         private int DpMin;
 
         private Text newKey = new Text(),
                 empty = new Text("");
 
 
         // Init DPMin argument.
         @Override
         public void setup(Context context) {
             DpMin = Integer.parseInt(context.getConfiguration().get(Utils.DpMinArg, "-1"));
 
             if (DpMin == -1) {
                 logger.severe("Failed fetching DPMin argument.");
                 return;
             }
         }
 
 
         @Override
         public void reduce(Text key, Iterable<LongWritable> values, Context context)
             throws IOException, InterruptedException {
 
             // Filter (don't write) dep-trees with not enough instances in corpus.
             if (Utils.sumValues(values) < DpMin) {
                 return;
             }
 
            newKey.set(attributeHeader
                    + Utils.delim + weka.core.Utils.quote(key.toString())
                    + Utils.delim + attributeType);
             context.write(newKey, empty);
         }
     }
 
 
     public static void main(String[] args) throws Exception {
         Configuration conf = new Configuration();
 
         // Read DpMin argument.
         conf.set(Utils.DpMinArg, args[Utils.argInIndex +2]);
 
         Job job = new Job(conf, "Headers");
 
         job.setJarByClass(Headers.class);
         job.setMapperClass(MapClass.class);
         job.setPartitionerClass(PartitionerClass.class);
         job.setCombinerClass(CombineClass.class);
         job.setReducerClass(ReduceClass.class);
 
         job.setOutputKeyClass(Text.class);
         job.setOutputValueClass(Text.class);
 
         job.setMapOutputKeyClass(Text.class);
         job.setMapOutputValueClass(LongWritable.class);
 
         FileInputFormat.addInputPath(job, new Path(args[Utils.argInIndex]));
         FileOutputFormat.setOutputPath(job, new Path(args[Utils.argInIndex +1]));
 
         boolean result = job.waitForCompletion(true);
 
         System.exit(result ? 0 : 1);
     }
 }
