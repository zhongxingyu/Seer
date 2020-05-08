 package cloudera.lab;
 
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.conf.Configured;
 import org.apache.hadoop.util.Tool;
 import org.apache.hadoop.util.ToolRunner;
 
 public class ProcessLogs extends Configured implements Tool {
 
 	@Override
 	public int run(String[] args) throws Exception {
 
 		if (args.length != 2) {
 			System.err.printf("Usage: %s [generic options] <input> <output>\n",
 					getClass().getSimpleName());
 			ToolRunner.printGenericCommandUsage(System.err);
 			//System.out.printf("Usage: WordCount <input dir> <output dir>\n");
 			System.exit(-1);
 		}
 
 		Job job = new Job(getConf());
 		job.setJarByClass(ProcessLogs.class);
 		job.setJobName("Web Hits Counter");
 		job.setMapperClass(LogFileMapper.class);
 		job.setReducerClass(WebHitsReducer.class);
 		job.setOutputKeyClass(Text.class);
 		job.setOutputValueClass(IntWritable.class);
 
 		FileInputFormat.setInputPaths(job, new Path(args[0]));
 		FileOutputFormat.setOutputPath(job, new Path(args[1]));
 
 		return (job.waitForCompletion(true) ? 0 : 1);
 	}
 
 	public static void main(String[] args) throws Exception {
 		int res = ToolRunner.run(new ProcessLogs(), args);
 		System.exit(res);
 	}
 }
