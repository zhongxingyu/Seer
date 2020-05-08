 package com.yzong.ccproj4;
 
 import java.io.IOException;
 import java.util.*;
 
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapred.FileInputFormat;
 import org.apache.hadoop.mapred.FileOutputFormat;
 import org.apache.hadoop.mapred.JobClient;
 import org.apache.hadoop.mapred.JobConf;
 import org.apache.hadoop.mapred.MapReduceBase;
 import org.apache.hadoop.mapred.Mapper;
 import org.apache.hadoop.mapred.OutputCollector;
 import org.apache.hadoop.mapred.Reducer;
 import org.apache.hadoop.mapred.Reporter;
 import org.apache.hadoop.mapred.TextInputFormat;
 import org.apache.hadoop.mapred.TextOutputFormat;
 
 public class GenerateCorpuses {
 
   public static class Map extends MapReduceBase implements Mapper<LongWritable, Text, Text, IntWritable> {
     private Text word = new Text();
 
     public void map(LongWritable key, Text value, OutputCollector<Text, IntWritable> output,
         Reporter reporter) throws IOException {
       String line = value.toString();
       StringTokenizer tokenizer = new StringTokenizer(line);
       List<String> prevTokens = new ArrayList<String>();  // Store previous five tokens.
       while (tokenizer.hasMoreTokens()) {
         // Remove punctuation from beginning / ending of a token.
         String token = tokenizer.nextToken().toLowerCase().replaceAll("[^A-Za-z]", "");
         // Ignore empty tokens.
         if (token.equals("")) {
           continue;
         }
         prevTokens.add(token);
         if (prevTokens.size() > 5) {
           prevTokens.remove(0); // Only keep last five words. 
         }
         String[] tokenSeg = prevTokens.toArray(new String[prevTokens.size()]);
         for (int i = 0; i < prevTokens.size(); i++) {
           String outputKey = "";
           for (int j = i; j < prevTokens.size(); j++) {
             outputKey += tokenSeg[j] + " ";
           }
           word.set(outputKey.substring(0, outputKey.length() - 1));
           output.collect(word, new IntWritable(1));
         }
       }
       return;
     }
   }
 
   public static class Reduce extends MapReduceBase implements Reducer<Text, IntWritable, Text, IntWritable> {
     public void reduce(Text key, Iterator<IntWritable> values, OutputCollector<Text, IntWritable> output,
         Reporter reporter) throws IOException {
       int sum = 0;
       while (values.hasNext()) {
         sum += values.next().get();
       }
       output.collect(key, new IntWritable(sum));
     }
   }
 
   public static void main(String[] args) throws Exception {
     JobConf conf = new JobConf(GenerateCorpuses.class);
     conf.setJobName("generatecorpuses");
 
     conf.setOutputKeyClass(Text.class);
     conf.setOutputValueClass(IntWritable.class);
 
     conf.setMapperClass(Map.class);
     conf.setCombinerClass(Reduce.class);
     conf.setReducerClass(Reduce.class);
 
     conf.setInputFormat(TextInputFormat.class);
     conf.setOutputFormat(TextOutputFormat.class);
 
    // Have seven Map and Reduce running in parallel.
     conf.setNumMapTasks(7);
     conf.setNumReduceTasks(7);
 
     FileInputFormat.setInputPaths(conf, new Path(args[0]));
     FileOutputFormat.setOutputPath(conf, new Path(args[1]));
 
     JobClient.runJob(conf);
   }
 }

