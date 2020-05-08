 // http://cxwangyi.blogspot.com/2009/12/wordcount-tutorial-for-hadoop-0201.html
 
 package com.brool;
         
 import java.io.File;
 import java.io.IOException;
 import java.lang.InterruptedException;
 import java.util.Iterator;
 
 import org.apache.hadoop.io.ObjectWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.Mapper;
 import org.apache.hadoop.mapreduce.Reducer;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 import org.apache.hadoop.util.GenericOptionsParser;
 
 import clojure.lang.RT;
 import clojure.lang.Var;
 
 public class Shim {
     public static class Map extends Mapper<Object, Text, Text, Text> {
         private Text k = new Text();
         private Text v = new Text();
         private Var mapfn;
         
         public void setup(Context context) {
             try {
                 clojure.lang.Compiler.loadFile(context.getConfiguration().get("clojure.file"));
                 mapfn = RT.var("user", "mapper");
             } catch (Exception e) {
                 // we'll throw an error on usage if this failed
             }
         }
 
         public void map(Object key, Text value, Context context) throws IOException, InterruptedException  {
             if (mapfn == null) {
                 throw new InterruptedException("unable to find mapper");
             }
 
             try {
                 java.util.List<Object> lst = (java.util.List<Object>) mapfn.invoke(value.toString());
                 
                 if (lst != null) {
                     for (Iterator<Object> ix = lst.iterator(); ix.hasNext(); ) {
                         java.util.List<Object> pair = (java.util.List<Object>) ix.next();
                         k.set(pair.get(0).toString());
                         v.set(pair.get(1).toString());
                         context.write(k, v);
                     }
                 }
             } catch (Exception e) {
                 throw new InterruptedException(e.toString());
             }
         }
     } 
         
     public static class Reduce extends Reducer<Text, Text, Text, Text> {
         private Text k = new Text();
         private Text v = new Text();
         private Var reducefn;
         
         public void setup(Context context) {
             try {
                 clojure.lang.Compiler.loadFile(context.getConfiguration().get("clojure.file"));
                 reducefn = RT.var("user", "reducer");
             } catch (Exception e) {
                 // we'll throw an error on usage if this failed
             }
         }
 
         public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
             if (reducefn == null) { 
                 throw new InterruptedException("unable to find reducer");
             }
 
             try {
                 java.util.ArrayList<String> vals = new java.util.ArrayList<String>();
                 for (Object o : values) {
                     vals.add(o.toString());
                 }
 
                 java.util.List<Object> lst = (java.util.List<Object>) reducefn.invoke(key.toString(), vals);
                for (Iterator<Object> ix = lst.iterator(); ix.hasNext(); ) {
                    k.set(ix.next().toString());
                    v.set(ix.next().toString());
                    context.write(k, v);
                }
             } catch (Exception e) {
                 throw new InterruptedException(e.toString());
             }
         }
     }
         
     public static void main(String[] args) throws Exception {
         Configuration conf = new Configuration();
         String [] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
 
         // Clojure file is the first temp file
         String [] tmpFiles = conf.get("tmpfiles").split(",");
         String clojure_file = new File(tmpFiles[0]).getName();
         conf.set("clojure.file", clojure_file);
 
         Job job = new Job(conf, clojure_file);
         job.setJarByClass(Shim.class);
         job.setMapperClass(Map.class);
         job.setReducerClass(Reduce.class);
         job.setOutputKeyClass(Text.class);
         job.setOutputValueClass(Text.class);
         FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
         FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
         System.exit(job.waitForCompletion(true) ? 0 : 1);
     }
 }
