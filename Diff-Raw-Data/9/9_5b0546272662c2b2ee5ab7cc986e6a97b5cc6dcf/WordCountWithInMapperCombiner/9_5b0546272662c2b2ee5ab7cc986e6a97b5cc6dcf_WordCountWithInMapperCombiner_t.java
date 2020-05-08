package com.dbtsai.hadoop.main;
 
 import com.dbtsai.hadoop.mapreduce.WordCountMR;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
 import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
 
 /**
  * Created with IntelliJ IDEA.
  * User: dbtsai
  * Date: 11/11/13
  * Time: 5:41 PM
  * To change this template use File | Settings | File Templates.
  */
 public class WordCountWithInMapperCombiner {
     public static void main(String[] args) throws Exception {
         Configuration conf = new Configuration();
 
         Job job = new Job(conf, "Word Count With Combiner");
 
         job.setOutputKeyClass(Text.class);
         job.setOutputValueClass(IntWritable.class);
 
         job.setMapperClass(WordCountMR.WordCountMapperWithInMapperCombiner.class);
         job.setReducerClass(WordCountMR.WordCountReducer.class);
 
         job.setInputFormatClass(TextInputFormat.class);
         job.setOutputFormatClass(TextOutputFormat.class);
 
         FileInputFormat.addInputPath(job, new Path(args[0]));
         FileOutputFormat.setOutputPath(job, new Path(args[1]));
 
         job.waitForCompletion(true);
     }
 }
