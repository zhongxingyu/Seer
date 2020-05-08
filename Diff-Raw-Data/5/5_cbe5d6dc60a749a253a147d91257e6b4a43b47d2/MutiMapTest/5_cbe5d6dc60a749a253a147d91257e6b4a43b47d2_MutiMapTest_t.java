 package com.zhoujie.test;
 
 import java.io.IOException;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.conf.Configured;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.NullWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.Mapper;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;
 import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
 import org.apache.hadoop.util.Tool;
 import org.apache.hadoop.util.ToolRunner;
 
 public class MutiMapTest extends Configured implements Tool {
 
   public static class MutiMapMaper 
        extends Mapper<Object, Text, NullWritable, Text> {
       
 //    public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException{
 //    String line = value.toString();
 //    String[] tokens = line.split("-");
 //
 //    mos.write("MOSInt",new Text(tokens[0]), new IntWritable(Integer.parseInt(tokens[1])));  //（第一处）
 //    mos.write("MOSText", new Text(tokens[0]),tokens[2]);     //（第二处）
 //    mos.write("MOSText", new Text(tokens[0]),line,tokens[0]+"/");  //（第三处）同时也可写到指定的文件或文件夹中
 //  }
 
     private MultipleOutputs<NullWritable, Text> mos;
 
     protected void setup(Context context) throws IOException,InterruptedException {
         mos = new MultipleOutputs<NullWritable, Text>(context);
     }
 
     public void map(Object key, Text value, Context context
                     ) throws IOException, InterruptedException {
 
         String line = value.toString();
         String [] lineArr = line.split(",");
         if (lineArr[0].equals("beijing")) {
            mos.write("beijing", NullWritable.get(), line, "beijing");
         } else if (lineArr[0].equals("shanghai")) {
            mos.write("shanghai", NullWritable.get(), line, "shanghai");
         }
         
     }
     
     
     protected void cleanup(Context context) throws IOException,InterruptedException {
         mos.close();
       }
   }
 
   public int run(String[] args) throws Exception {
  
     if (args.length != 2) {
         System.err.println(MutiMapTest.class.getSimpleName() + "<in> <out>");
         return 1;
     }
 
     Configuration conf = this.getConf(); 
     Job job = new Job(conf);
     job.setJarByClass(MutiMapTest.class);
     job.setJobName("MutiMapTest");
     job.setMapperClass(MutiMapMaper.class);
     job.setNumReduceTasks(0);
     
 //    job.setInputFormatClass(LzoLubanLogProtobufB64LineInputFormat.class);
 
     job.setOutputKeyClass(Text.class);
     job.setOutputValueClass(Text.class);
 
     job.setMapOutputKeyClass(NullWritable.class);
     job.setMapOutputValueClass(Text.class);
 
     FileInputFormat.setInputPaths(job, args[0]);
     FileOutputFormat.setOutputPath(job, new Path(args[1]));
  
     MultipleOutputs.addNamedOutput(job, "beijing", TextOutputFormat.class, NullWritable.class, Text.class);
     MultipleOutputs.addNamedOutput(job, "shanghai", TextOutputFormat.class, NullWritable.class, Text.class);
 
     return job.waitForCompletion(true) ?  0 : 1; 
   }
 
   public static void main(String[] args) throws Exception {
       if (args.length == 0) {
             String usage = MutiMapTest.class.getSimpleName() + "<in> <out>";
             System.out.println(usage);
             return;
       }
       int res = ToolRunner.run(new MutiMapTest(), args);
       System.exit(res);
    }
 }
