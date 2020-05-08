 package com.shzhangji.mapred_sandbox;
 
 import java.io.IOException;
 
 import org.apache.hadoop.conf.Configured;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.NullWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.Mapper;
 import org.apache.hadoop.mapreduce.Reducer;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 import org.apache.hadoop.util.Tool;
 import org.apache.hadoop.util.ToolRunner;
 
 public class LineCountJob extends Configured implements Tool {
 
     @Override
     public int run(String[] args) throws Exception {
 
         Path input = new Path(args[0]);
        Path output = new Path(args[1]);
 
         Job job = new Job(getConf());
         job.setJarByClass(LineCountJob.class);
 
         FileInputFormat.addInputPath(job, input);
         FileOutputFormat.setOutputPath(job, output);
 
         job.setMapperClass(LineCountMapper.class);
         job.setMapOutputKeyClass(NullWritable.class);
         job.setMapOutputValueClass(LongWritable.class);
 
         job.setReducerClass(LineCountReducer.class);
         job.setNumReduceTasks(1);
         job.setOutputKeyClass(NullWritable.class);
         job.setOutputValueClass(LongWritable.class);
 
         return job.waitForCompletion(true) ? 0 : 1;
     }
 
     public static class LineCountMapper extends Mapper<LongWritable, Text, NullWritable, LongWritable> {
 
         private long count;
 
         @Override
         protected void setup(Context context)
                 throws IOException, InterruptedException {
 
             count = 0;
         }
 
         @Override
         protected void map(LongWritable key, Text value, Context context)
                 throws IOException, InterruptedException {
 
             ++count;
         }
 
         @Override
         protected void cleanup(Context context)
                 throws IOException, InterruptedException {
 
            super.cleanup(context);
             context.write(NullWritable.get(), new LongWritable(count));
         }
 
     }
 
     public static class LineCountReducer extends Reducer<NullWritable, LongWritable, NullWritable, LongWritable> {
 
         private long sum;
 
         @Override
         protected void setup(Context context)
                 throws IOException, InterruptedException {
 
             sum = 0;
         }
 
         @Override
         protected void reduce(NullWritable key, Iterable<LongWritable> values, Context context)
                 throws IOException, InterruptedException {
 
             for (LongWritable value : values) {
                 sum += value.get();
             }
         }
 
         @Override
         protected void cleanup(Context context)
                 throws IOException, InterruptedException {
 
             super.cleanup(context);
             context.write(NullWritable.get(), new LongWritable(sum));
         }
     }
 
     public static void main(String[] args) throws Exception {
         System.exit(ToolRunner.run(new LineCountJob(), args));
     }
 
 }
