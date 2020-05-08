 package com.cloudera.tools.rmat;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
 import org.apache.hadoop.util.Tool;
 import org.apache.hadoop.util.ToolRunner;
 
 public class RMat implements Tool {
 
 	private Configuration conf;
 	
 	public void setConf(Configuration conf) {
 		this.conf = conf;
 	}
 
 	public Configuration getConf() {
 		return conf;
 	}
 
 	public int run(String[] args) throws Exception {
 		RMatInputFormat.setNodes(conf, 100000);
 		RMatInputFormat.setEdges(conf, 1000000);
 		RMatInputFormat.setMappers(conf, 3);
 		RMatInputFormat.setDistribution(conf, 0.7f, 0.1f, 0.1f);
 		RMatInputFormat.setRandom(conf, false);
 		
 		Job job = new Job(conf, "RMat Graph Generator");
 		
 		job.setCombinerClass(RMatReducer.class);
 		job.setReducerClass(RMatReducer.class);
 		
 		job.setMapOutputKeyClass(Edge.class);
 		job.setMapOutputValueClass(NullWritable.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
 		
 		job.setInputFormatClass(RMatInputFormat.class);
 		job.setOutputFormatClass(TextOutputFormat.class);
 		
 		FileOutputFormat.setOutputPath(job, new Path(args[0]));
 		
 		job.setJarByClass(RMat.class);
 		
 		job.setNumReduceTasks(3);
 		
 		job.setGroupingComparatorClass(Edge.GroupingComparator.class);
 		job.setSortComparatorClass(Edge.SortingComparator.class);
 		
 		job.submit();
 		job.waitForCompletion(true);
 		return 0;
 	}
 	
 	public static void main(String[] args) throws Exception {
 		RMat rmat = new RMat();
 		ToolRunner.run(rmat, args);
 	}
 
 }
