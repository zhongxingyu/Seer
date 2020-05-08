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
 
 
 public class Pairs {
 
     private static final Logger logger = Utils.setLogger(Logger.getLogger(Pairs.class.getName()));
 
     private static String
        pairsDelim = "\t",
         joinStart = "*";  // Join start reducer char.
 
     // Write { (N1, N2), * : True/False, HypernymIndex }  -- (Hypernym Index in pair array).
     public static class MapClass extends Mapper<LongWritable, Text, Text, Text> {
 
         private Text newKey = new Text(),
                 newValue = new Text();
 
         @Override
         public void map(LongWritable key, Text value, Context context)
             throws IOException, InterruptedException {
 
             // Fetch pairs and tag from value.
            String[] words = value.toString().split(pairsDelim);
             String hyponym = words[0],
                    hypernym = words[1],
                    related = words[2];  // True/False
 
 
             // Emit key by lexicographical order.
             if (hyponym.compareTo(hypernym) < 0) {
                 newKey.set(hyponym + Utils.delim + hypernym + Utils.delim + joinStart);
                 // 1 is the hypernym index in the pair. That is, the second word when sorted lexi.
                 newValue.set(related + Utils.delim + 1);
             } else {
                 newKey.set(hypernym + Utils.delim + hyponym + Utils.delim + joinStart);
                 // 1 is the hypernym index in the pair. That is, the first word when sorted lexi.
                 newValue.set(related + Utils.delim + 0);
             }
 
             context.write(newKey, newValue);
         }
     }
 
 
     // Partition by key hash code.
     public static class PartitionerClass extends Partitioner<Text, Text> {
         @Override
         public int getPartition(Text key, Text value, int numPartitions) {
             return (key.hashCode() & Integer.MAX_VALUE) % numPartitions;
         }
 
     }
 
 
     // Reducer just passes on data.
     public static class ReduceClass extends Reducer<Text, Text, Text, Text> {
 
         @Override
         public void reduce(Text key, Iterable<Text> values, Context context)
             throws IOException, InterruptedException {
 
             context.write(key, values.iterator().next());
         }
     }
 
 
     public static void main(String[] args) throws Exception {
         Configuration conf = new Configuration();
 
         Job job = new Job(conf, "Pairs");
 
         job.setJarByClass(Pairs.class);
         job.setMapperClass(MapClass.class);
         job.setPartitionerClass(PartitionerClass.class);
         job.setReducerClass(ReduceClass.class);
 
         job.setOutputKeyClass(Text.class);
         job.setOutputValueClass(Text.class);
 
         FileInputFormat.addInputPath(job, new Path(args[Utils.argInIndex]));
         FileOutputFormat.setOutputPath(job, new Path(args[Utils.argInIndex + 1]));
 
         boolean result = job.waitForCompletion(true);
 
         System.exit(result ? 0 : 1);
     }
 }
