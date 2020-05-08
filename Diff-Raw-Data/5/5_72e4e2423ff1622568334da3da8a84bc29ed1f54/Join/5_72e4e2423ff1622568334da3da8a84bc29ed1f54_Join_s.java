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
 
 
 public class Join {
     private static final String
         isRelated = "True";
 
     private static final Logger logger = Utils.setLogger(Logger.getLogger(Join.class.getName()));
 
 
     // Mapper just passes on data.
     public static class MapClass extends Mapper<LongWritable, Text, Text, Text> {
 
         private Text newKey = new Text(),
              newValue = new Text();
 
         @Override
         public void map(LongWritable key, Text value, Context context)
             throws IOException, InterruptedException {
 
             // Split data by key-value delimeter, and just pass on the data.
             // There are multiple delimeters here, so we just take the first one.
             String v = value.toString();
             int i = v.indexOf(Utils.keyValueDelim);
 
             newKey.set(v.substring(0, i));
            newValue.set(v.substring(i));
 
             context.write(newKey, newValue);
         }
     }
 
 
     // Partition by NOUNS-in-key hash code.
     public static class PartitionerClass extends Partitioner<Text, Text> {
         @Override
         public int getPartition(Text key, Text value, int numPartitions) {
             String[] words = key.toString().split(Utils.delim);
             Text data = new Text(words[0] + Utils.delim + words[1]);
             return (data.hashCode() & Integer.MAX_VALUE) % numPartitions;
         }
     }
 
 
     // Reducer just passes on data.
     public static class ReduceClass extends Reducer<Text, Text, Text, Text> {
         private String nounPair = null;
         private short hypernymIndex;
         private boolean related;
 
         private Text newKey = new Text(),
              newValue = new Text();
 
 
         @Override
         public void reduce(Text key, Iterable<Text> values, Context context)
             throws IOException, InterruptedException {
 
             String[]
                 k = key.toString().split(Utils.delim),  // Split key data.
                 v;  // Will be used for split value data.
 
             // Init new tagged noun pair (N1, N2) join data i.e. hypernym/hyponym, (un)related.
             if (k[2].equals(Utils.joinStart)) {
                  v = values.iterator().next().toString().split(Utils.delim);
 
                 nounPair = k[0] + Utils.delim + k[1];
                 related = v[0].equals(isRelated) ? true : false ;
                 hypernymIndex = Short.parseShort(v[1]);
             } else {
                 // This is not a new noun-pair,
                 // or an untagged one, which we can't learn from.
 
                 // Fetch dep-tree and calculate score if this belongs to a tagged noun-pair.
                 if ((k[0] + Utils.delim + k[1]).equals(nounPair)) {
                     long depTreeScore;
                     String curHypernymIndex, curHyponymIndex;  // CURRENT *nym index.
 
                     for (Text value : values) {
                         // Join current dep-tree with noun-pair,
                         // because they have the same key: (N1, N2).
                         v = value.toString().split(Utils.biarcDelim);
 
                         // Key := dep-tree.
                         newKey.set(v[0]);
 
                         // Init dep-tree count score (for trainig algorithm).
                         // Note we give a negative score for unrelated pairs.
                         depTreeScore = (related ? 1 : -1) * Long.parseLong((v[1]));
 
                         // Init *nym index.
                         if (hypernymIndex == 0) {
                             curHypernymIndex = k[2];
                             curHyponymIndex = k[3];
                         } else {
                             curHypernymIndex = k[3];
                             curHyponymIndex = k[2];
                         }
 
                         // Value := score, hypernym-index, hyponym-index.
                         newValue.set(String.valueOf(depTreeScore) + Utils.delim
                                 + curHypernymIndex + Utils.delim + curHyponymIndex);
 
                         context.write(newKey, newValue);
                     }
                 }
             }
         }
     }
 
 
     public static void main(String[] args) throws Exception {
         Configuration conf = new Configuration();
 
         Job job = new Job(conf, "Join");
 
         job.setJarByClass(Join.class);
         job.setMapperClass(MapClass.class);
         job.setPartitionerClass(PartitionerClass.class);
         job.setReducerClass(ReduceClass.class);
 
         job.setOutputKeyClass(Text.class);
         job.setOutputValueClass(Text.class);
 
         for (int i=0; i < args.length -1; i++) {
             FileInputFormat.addInputPath(job, new Path(args[Utils.argInIndex +i]));
         }
 
         FileOutputFormat.setOutputPath(job, new Path(args[args.length -1]));
 
         boolean result = job.waitForCompletion(true);
 
         System.exit(result ? 0 : 1);
     }
 }
