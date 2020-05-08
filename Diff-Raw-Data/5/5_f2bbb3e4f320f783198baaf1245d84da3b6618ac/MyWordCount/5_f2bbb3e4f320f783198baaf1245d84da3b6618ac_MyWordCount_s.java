 package us.yuxin.demo.hadoop.zero;
 
 import java.io.IOException;
 import java.util.StringTokenizer;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.conf.Configured;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.Mapper;
 import org.apache.hadoop.mapreduce.Reducer;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
 import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
 import org.apache.hadoop.util.Tool;
 import org.apache.hadoop.util.ToolRunner;
 
 public class MyWordCount extends Configured implements Tool {
 	public static class MWCMapper extends
 			Mapper<LongWritable, Text, Text, IntWritable> {
 		private static final IntWritable one = new IntWritable(1);
 		private Text word = new Text();
 
 		protected void map(LongWritable key, Text value, Context context)
 				throws IOException, InterruptedException {
 
 			String line = value.toString();
 			StringTokenizer tokenizer = new StringTokenizer(line);
 
 			while (tokenizer.hasMoreTokens()) {
 				word.set(tokenizer.nextToken());
 				context.write(word, one);
 			}
 		}
 	}
 
 	public static class MWCReducer extends
 			Reducer<Text, IntWritable, Text, IntWritable> {
 		
 		protected void reduce(Text key, Iterable<IntWritable> values,
 				Context context) throws IOException, InterruptedException {
 			int sum = 0;
 			for (IntWritable value: values) {
 				sum += value.get();
 			}
 			context.write(key, new IntWritable(sum));
 		}
 	}
 
 	@Override
 	public int run(String[] args) throws Exception {
 		Configuration conf = getConf();
 		Job job = new Job(conf, "MyWordCount");
 		
 		job.setOutputKeyClass(Text.class);
 		job.setOutputValueClass(IntWritable.class);
 		
 		job.setMapperClass(MWCMapper.class);
 		job.setReducerClass(MWCReducer.class);
 		
 		job.setInputFormatClass(TextInputFormat.class);
 		job.setOutputFormatClass(TextOutputFormat.class);
 		
 		FileInputFormat.addInputPath(job, new Path(args[0]));
 		FileOutputFormat.setOutputPath(job, new Path(args[1]));
 		
 		job.waitForCompletion(true);
 		return 0;
		
		
 	}
 	
 	public static void main(String args[]) throws Exception {
 		ToolRunner.run(new Configuration(), new MyWordCount(), args);
 	}
 }
