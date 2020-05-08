 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package de.hd.cl.haas.distributedcrawl.map;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.HashMap;
 import net.htmlparser.jericho.Source;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.Mapper;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
 import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
 
 /**
  *
  *
  * @author Michael Haas
  */
 public class IndexerMap extends Mapper<LongWritable, Text, Text, String[]> {
 
     @Override
     protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
         System.out.println("key is " + key.toString());
         System.out.println("value is " + value.toString());
         URL url = new URL(value.toString());
         url.openConnection();
         InputStream stream = url.openStream();
         Source source = new Source(stream);
         source.fullSequentialParse();
         String completeContent = source.getTextExtractor().toString();
         System.out.println("CompleteContent: ");
         System.out.println(completeContent);
         // poor man's tokenizer
         String[] tokens = completeContent.split(" ");
 
         // count absolute frequencies for terms
         HashMap<String, Integer> counts = new HashMap<String, Integer>();
         for (int ii = 0; ii < tokens.length; ii++) {
             String term = tokens[ii];
             if (!counts.containsKey(term)) {
                 counts.put(term, 0);
             }
            counts.put(term, counts.get(term) + 1);
         }
        Text tTerm = new Text();
         for (String term : counts.keySet()) {
             // Emit(term t, posting <n,H{t}>)      
             String[] compositeValue = {value.toString(), counts.get(term).toString()};
             System.out.println("Out-key: " + term);
             System.out.println("Out-value: " + compositeValue);
             // TODO: is compositeValue properly serialized?
            tTerm.set(term);
            context.write(tTerm, compositeValue);
         }
     }
 
     public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException {
         Configuration conf = new Configuration();
 
         Job job = new Job(conf, "IndexerMap");
 
         //job.setOutputKeyClass(Text.class);
         //job.setOutputValueClass(IntWritable.class);
 
         job.setJarByClass(IndexerMap.class);
         job.setMapperClass(IndexerMap.class);
         //job.setReducerClass(Reduce.class);
 
         // TextInputFormat: key is offset in file, value is line
         job.setInputFormatClass(TextInputFormat.class);
         //job.setOutputFormatClass(TextOutputFormat.class);
 
         FileInputFormat.addInputPath(job, new Path(args[0]));
         FileOutputFormat.setOutputPath(job, new Path(args[1]));
 
         job.waitForCompletion(true);
     }
 }
