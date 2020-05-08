 package org.graphhadoop;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.StringTokenizer;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.ArrayWritable;
 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.Mapper;
 import org.apache.hadoop.mapreduce.Reducer;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 import org.apache.hadoop.util.GenericOptionsParser;
 import org.apache.avro.*;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
 
 public class CreateAdjacency {
 
 	
   public static class AdjacencyMapper 
        extends Mapper<Object, Text, IntWritable, IntWritable>{
 	
     private int n1, n2;
 
     public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
     
 	String line = value.toString();
 	
 	try{
 	StringTokenizer t = new StringTokenizer(line);
 	n1 = Integer.parseInt(t.nextToken());
 	n2 = Integer.parseInt(t.nextToken());
 	}
 	
 	catch (Exception e)
 	{
 		return;
 	}
	
	Schema schema = Schema.parse(getClass().getResourceAsStream("test1.avsc"));
	GenericRecord datum = new GenericData.Record(schema);
	datum.pu
	
	
 	//make the edges undirected
 	context.write(new IntWritable(n1), new IntWritable(n2));
 	context.write(new IntWritable(n2), new IntWritable(n1));
   }
  }
   
   public static class AdjacencyReducer extends Reducer<IntWritable,IntWritable,IntWritable,Text> {
         
 	  private static String line;
     public void reduce(IntWritable key, Iterable<IntWritable> values, 
                        Context context
                        ) throws IOException, InterruptedException {
         
     	String line = "";
 
      	for (IntWritable val : values) {
     	   line += val+" ";
       }
 
         context.write(key,new Text(line));
       }
   }
   
 
 
 
   public static void Adjacency(String inputPath, String outputPath) throws Exception {
 
 	System.out.print(inputPath);		
 	Configuration conf = new Configuration();
       Job job = new Job(conf, "Create Adjacency");
       job.setJarByClass(CreateAdjacency.class);
       job.setMapperClass(AdjacencyMapper.class);
       job.setReducerClass(AdjacencyReducer.class);
       job.setOutputValueClass(Text.class);
       job.setOutputKeyClass(IntWritable.class);
       job.setMapOutputKeyClass(IntWritable.class);
       job.setMapOutputValueClass(IntWritable.class); 
       Configuration conf2 = new Configuration();
       FileSystem fs = FileSystem.get(conf2);
       //Deleting output path if exists
       Path fp = new Path(outputPath);
       if (fs.exists(fp)) {
       	   // remove the file first
       	         fs.delete(fp);
       	       }
       
       FileInputFormat.addInputPath(job, new Path(inputPath));
       FileOutputFormat.setOutputPath(job, new Path(outputPath));
       if(job.waitForCompletion(true)==true)
       {
       	return;
       	
       }
     }
 }
