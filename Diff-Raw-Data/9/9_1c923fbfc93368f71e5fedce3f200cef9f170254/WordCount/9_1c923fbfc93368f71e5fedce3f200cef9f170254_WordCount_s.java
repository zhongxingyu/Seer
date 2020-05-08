 package cmu.ds.mr.test;
 
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.StringTokenizer;
 
 import cmu.ds.mr.conf.JobConf;
 import cmu.ds.mr.io.OutputCollector;
 import cmu.ds.mr.mapred.JobClient;
 import cmu.ds.mr.mapred.MapReduceBase;
 import cmu.ds.mr.mapred.Mapper;
 import cmu.ds.mr.mapred.Reducer;
 
 /**
  * Example word count program.
  * Adopted from http://www.cloudera.com/content/cloudera-content/cloudera-docs/HadoopTutorial/CDH4/Hadoop-Tutorial/ht_topic_5_1.html
  * */
 public class WordCount {
 
   public static class Map extends MapReduceBase implements Mapper<Long, String, String, Integer> {
     private final static Integer one = 1;
     private String word = "";
 
     public void map(Long key, String value, OutputCollector<String, Integer> output) throws IOException {
       String line = value.toString();
       StringTokenizer tokenizer = new StringTokenizer(line);
       while (tokenizer.hasMoreTokens()) {
         word = tokenizer.nextToken();
         output.collect(word, one);
       }
     }
   }
 
   public static class Reduce extends MapReduceBase implements Reducer<String, Integer, String, Integer> {
     public void reduce(String key, Iterator<Integer> values, OutputCollector<String, Integer> output) throws IOException {
       int sum = 0;
       while (values.hasNext()) {
         sum += values.next();
       }
       output.collect(key, sum);
     }
   }
 
   public static void main(String[] args) throws Exception {
 //    if(args.length != 2) {
 //      System.err.println("Usage: WordCount <inPath> <outPath>");
 //      return;
 //    }
 //    args[0] = "data/abstract.txt";
 //    args[1] = "data/out/";
     
     JobConf conf = new JobConf();
     conf.setJobName("wordcount");
 
 //    conf.setOutputKeyClass(Text.class);
 //    conf.setOutputValueClass(IntWritable.class);
 
     conf.setNumReduceTasks(4);
     conf.setMapperClass(Map.class);
     conf.setReducerClass(Reduce.class);
 
 //    conf.setInputFormat(TextInputFormat.class);
 //    conf.setOutputFormat(TextOutputFormat.class);
     
 //    conf.setInpath(args[0]);
 //    conf.setOutpath(args[1]);
    conf.setInpath("data/abstract.txt");
     conf.setOutpath("data/out/");
 
     JobClient.runJob(conf);
   }
 }
