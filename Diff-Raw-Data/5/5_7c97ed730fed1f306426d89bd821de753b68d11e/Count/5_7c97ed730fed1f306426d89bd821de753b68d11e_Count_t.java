 package v2;
 import java.io.IOException;
 import org.apache.hadoop.conf.*;
 
 import org.apache.hadoop.filecache.DistributedCache;
 import org.apache.hadoop.fs.FileStatus;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.*;
 
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.Mapper;
 import org.apache.hadoop.mapreduce.Reducer;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
 import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
 import org.apache.hadoop.util.Tool;
 import org.apache.hadoop.util.ToolRunner;
 import org.apache.hadoop.util.GenericOptionsParser;
 
 public class Count extends Configured implements Tool{
 
 	public Count(){
 
 	}
 
  public static class Map extends Mapper<IntWritable, ProjectWritable, Text, IntWritable> {
  	Text text = new Text("count");
  	IntWritable one = new IntWritable(1);
     @Override
 	public void map(IntWritable key, ProjectWritable value, Context context) throws IOException, InterruptedException {
 
     	
     	context.write(text, one);
     }
 
   @Override
 public void run (Context context) throws IOException, InterruptedException {
         setup(context);
         while (context.nextKeyValue()) {
               map(context.getCurrentKey(), context.getCurrentValue(), context);
             }
         cleanup(context);
   }
  }
 
  
 public static class Reduce extends Reducer<Text, IntWritable, Text, IntWritable> {
 
     @Override
	public void reduce(Text key, Iterable<IntWritable> values, Context context) 
     		throws IOException, InterruptedException {
 		 
 		int count = 0;
     	
     	for(IntWritable c: values){
     		count+=c.get();
     	}
     	context.write(key, new IntWritable(count));
     }
  }
 
 @Override
 public int run(String[] args) throws Exception {
 
 	
 	
     Job job = new Job();
     Configuration conf = job.getConfiguration();
 
     FileSystem fs = FileSystem.get(conf);
 	FileStatus[] jarFiles = fs.listStatus(new Path("/libs"));
 	 for (FileStatus status : jarFiles) {
 	      Path disqualified = new Path(status.getPath().toUri().getPath());
 	      DistributedCache.addFileToClassPath(disqualified, conf, fs);
 	 }
 
     job.setOutputKeyClass(IntWritable.class);
     job.setOutputValueClass(IntWritable.class);
 
     job.setMapperClass(Map.class);
     job.setReducerClass(Reduce.class);
     job.setCombinerClass(Reduce.class);
     
     job.setNumReduceTasks(1);
 
 
     job.setInputFormatClass(SequenceFileInputFormat.class);
     job.setOutputFormatClass(SequenceFileOutputFormat.class);
     FileInputFormat.setInputPaths(job, new Path(args[0]));
     FileOutputFormat.setOutputPath(job, new Path(args[1]));
 
     job.setJarByClass(Count.class);
     job.waitForCompletion(true);
    
     return 0;
     }
 }
 
