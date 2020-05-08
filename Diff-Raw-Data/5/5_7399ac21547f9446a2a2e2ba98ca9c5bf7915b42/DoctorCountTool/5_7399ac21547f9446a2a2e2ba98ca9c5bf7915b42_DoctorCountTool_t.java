 /**
  * 
  */
 package com.zazzercode.doctorhere.models.hadoop;
 
 import org.apache.hadoop.conf.Configured;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapred.FileInputFormat;
 import org.apache.hadoop.mapred.FileOutputFormat;
 import org.apache.hadoop.mapred.JobClient;
 import org.apache.hadoop.mapred.JobConf;
 
 
 import org.apache.hadoop.util.Tool;
 
 /**
  * responsible for triggering the map reduce job in Hadoop
  * @author prayag
  *
  */
 public class DoctorCountTool extends Configured implements Tool{
 
 	@Override
 	public int run(String[] arg0) throws Exception {
 		JobConf jobConf = new JobConf(getConf(), DoctorCountTool.class);
 		jobConf.setJobName("DoctorCountTool");
 
 		//Setting configuration object with the Data Type of output Key and Value
 		jobConf.setOutputKeyClass(Text.class);
 		jobConf.setOutputValueClass(IntWritable.class);
 
         jobConf.setMapperClass(DoctorMapper.class);
         jobConf.setReducerClass(DoctorReducer.class);
 
         //the hdfs input and output directory to be fetched from the command line
        // add any aline to input.txt
		FileInputFormat.addInputPath(jobConf, new Path("/home/prayag/input.txt"));
		FileOutputFormat.setOutputPath(jobConf, new Path("/home/prayag/output"));
 
 		JobClient.runJob(jobConf);
 		return 0; 
 	}
 
 }
