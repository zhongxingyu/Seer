 package com.zhoujie.test;
 
 import java.io.IOException;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.conf.Configured;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.NullWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;
 import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
 import org.apache.hadoop.util.Tool;
 import org.apache.hadoop.util.ToolRunner;
 
 import com.allyes.carpenter.tianqi.mapreduce.io.ProtobufTianqiSawLogWritable;
 import com.zhoujie.hive.FactPBHive.ShangHai;
 import com.zhoujie.hive.mapreduce.io.ProtobufShangHaiWritable;
 import com.zhoujie.hive.mapreduce.output.LzoShangHaiProtobufB64LineOutputFormat;
 import com.zhoujie.hive.mapreduce.output.LzoShangHaiProtobufBlockOutputFormat;
 
 public class LzoPBMutiMapTest extends Configured implements Tool {
 
     public static class LzoPBMutiMapMaper extends
             AbstractMutilOutputMapper<Text, ProtobufShangHaiWritable> {
 
         @Override
         protected String getBaseOutputPath(String dirname) {
             StringBuilder sb = new StringBuilder();
             sb.append(dirname.toString());
             sb.append(Path.SEPARATOR);
             sb.append(uniquePrefix_);
             return sb.toString();
         }
 
         @Override
         protected void map(LongWritable key, Text value, Context context)
                 throws IOException, InterruptedException {
             String line = value.toString();
             String[] lineArr = line.split(",");
             if (lineArr.length != 2) {
                 return;
             }
             ShangHai.Builder shanghai = ShangHai.newBuilder();
             shanghai.setRegionName(lineArr[0]);
             shanghai.setInfo(lineArr[1]);
 
             ProtobufShangHaiWritable newValue = new ProtobufShangHaiWritable(shanghai.build());
             if (lineArr[0].equals("beijing")) {
                 super.mos_.write("beijing", NullWritable.get(), newValue,
                         getBaseOutputPath("beijing"));
             } else if (lineArr[0].equals("shanghai")) {
                 super.mos_.write("shanghai", NullWritable.get(), newValue,
                         getBaseOutputPath("shanghai"));
             }
 
         }
 
     }
 
     public int run(String[] args) throws Exception {
 
         if (args.length != 2) {
             System.err
                     .println(LzoPBMutiMapTest.class.getSimpleName() + "<in> <out>");
             return 1;
         }
 
         Configuration conf = this.getConf();
         Job job = new Job(conf);
         job.setJarByClass(LzoPBMutiMapTest.class);
         job.setJobName("MutiMapTest");
         job.setMapperClass(LzoPBMutiMapMaper.class);
         job.setNumReduceTasks(0);
 
         // job.setInputFormatClass(LzoLubanLogProtobufB64LineInputFormat.class);
 
//        job.setOutputKeyClass(Text.class);
//        job.setOutputValueClass(Text.class);
 
         job.setMapOutputKeyClass(NullWritable.class);
         job.setMapOutputValueClass(ProtobufShangHaiWritable.class);
 
         FileInputFormat.setInputPaths(job, args[0]);
         FileOutputFormat.setOutputPath(job, new Path(args[1]));
 
 //        MultipleOutputs.addNamedOutput(job, "beijing", LzoShangHaiProtobufB64LineOutputFormat.class,
 //                NullWritable.class, ProtobufShangHaiWritable.class);
         MultipleOutputs.addNamedOutput(job, "beijing", LzoShangHaiProtobufBlockOutputFormat.class,
                 NullWritable.class, ProtobufShangHaiWritable.class);
 //        MultipleOutputs.addNamedOutput(job, "shanghai", LzoShangHaiProtobufB64LineOutputFormat.class,
 //                NullWritable.class, ProtobufShangHaiWritable.class);
         MultipleOutputs.addNamedOutput(job, "shanghai", LzoShangHaiProtobufBlockOutputFormat.class,
                 NullWritable.class, ProtobufShangHaiWritable.class);
 
         return job.waitForCompletion(true) ? 0 : 1;
     }
 
     public static void main(String[] args) throws Exception {
         if (args.length == 0) {
             String usage = LzoPBMutiMapTest.class.getSimpleName() + "<in> <out>";
             System.out.println(usage);
             return;
         }
         int res = ToolRunner.run(new LzoPBMutiMapTest(), args);
         System.exit(res);
     }
 }
