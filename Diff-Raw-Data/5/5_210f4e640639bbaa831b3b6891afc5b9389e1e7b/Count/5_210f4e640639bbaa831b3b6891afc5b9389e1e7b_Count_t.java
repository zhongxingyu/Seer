 package com.dsp.ass2;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.logging.Logger;
 import java.util.Arrays;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Counter;
 import org.apache.hadoop.mapreduce.Counters;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.Mapper;
 import org.apache.hadoop.mapreduce.Partitioner;
 import org.apache.hadoop.mapreduce.Reducer;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
 import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 import org.apache.lucene.analysis.TokenStream;
 import org.apache.lucene.analysis.core.StopFilter;
 import org.apache.lucene.analysis.en.EnglishAnalyzer;
 import org.apache.lucene.analysis.standard.StandardTokenizer;
 import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
 import org.apache.lucene.analysis.util.CharArraySet;
 import org.apache.lucene.util.Version;
 
 
 public class Count {
 
     private static final Logger logger = Utils.setLogger(Logger.getLogger(Count.class.getName()));
 
     private static final String ngramDelim = "\t",
             wordsDelim = " ",
             wordHeader = "*";
 
 
     // For every word `w` in n-gram: emit { decade, w, * : c(w) }
     // For every central word `w` in n-gram: emit { decade, w, wi : c(w,wi) } and { " : c(wi,w) } , i=1..4 (its neithbours)
     public static class MapClass extends Mapper<LongWritable, Text, Text, LongWritable> {
 
         private LongWritable num = new LongWritable();
         private Text word = new Text();
 
 
         // Returns the same string with stop words & punctuation removed. removed.
         public String removeStopWords(String words) {
             CharArraySet stopWords = EnglishAnalyzer.getDefaultStopSet();
             TokenStream tokenStream = new StandardTokenizer(Version.LUCENE_48, new StringReader(words.trim()));
             tokenStream = new StopFilter(Version.LUCENE_48, tokenStream, stopWords);
             CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
             StringBuilder sb = new StringBuilder();
             String output;
 
             try {
                 // Append only non stop words.
                 tokenStream.reset();
                 while (tokenStream.incrementToken()) {
                     String term = charTermAttribute.toString();
                     sb.append(term + " ");
                 }
 
                 output = sb.toString().trim();  // Clean trailing whitespace.
                 tokenStream.close();
             } catch (IOException e) {
                 // On any error, return the original n-gram (and notify user).
                 logger.severe("Error filtering stop words from n-gram: " + words);
                 return words;
             }
 
             return output;
         }
 
 
         @Override
         public void map(LongWritable key, Text value, Context context)
             throws IOException, InterruptedException {
 
             // Convert n-gram to lowercase, Split into words, and handle <w,wi> pairs.
             String[] ngram = value.toString().split(ngramDelim),
                 words = ngram[0].toLowerCase().split(wordsDelim);
 
             if (ngram.length < 3) {
                 logger.severe("ngram too short: " + ngram.toString());
                 return;
             }
 
            int decade = Integer.parseInt(ngram[1]) / 10,
                 occurences = Integer.parseInt(ngram[2]);
 
             if (words.length > 0 && decade >= Utils.minDecade) {
                 // Get central word in n-gram.
                 String centralWord = words[words.length / 2];
 
                 // Remove stop words, count c(w) for every word in pair,
                 // and count pairs only if central word wasn't filtered.
                 // That is - it wasn't a stop word.
                 words = removeStopWords(ngram[0]).split(wordsDelim);
 
                 if (words.length > 0) {
                     // Count pairs if central word in n-gram was not a stop word.
                    int centralIndex = Arrays.asList(words).indexOf(centralWord);
                     boolean countPairs = false;
                     if (centralIndex >= 0) {
                        countPairs = true;
                     }
 
                     // TODO What about if n-gram = 'a a a a a'?
                     // Do we emit the same occurence 4 more times than necessary?
                     for (int i=0; i < words.length; i++) {
                         // Emit for every word in n-gram.
                         num.set(occurences);
 
                         // Emit c(w) for every word.
                         word.set(decade + Utils.delim + words[i] + Utils.delim + wordHeader);
                         context.write(word, num);
 
                         // Emit c(w,wi), c(wi,w) for central word, if central word wasn't a stop word.
                         if (countPairs && i != centralIndex) {
                             word.set(decade + Utils.delim + centralWord + Utils.delim + words[i]);
                             context.write(word, num);
 
                             word.set(decade + Utils.delim + words[i] + Utils.delim + centralWord);
                             context.write(word, num);
                         }
                     }
                 }
             }
         }
     }
 
 
     // Sum every identical count values before sending to reducer.
     public static class CombineClass extends Reducer<Text, LongWritable, Text, LongWritable> {
 
         private LongWritable sumWrt = new LongWritable();
 
         @Override
         public void reduce(Text key, Iterable<LongWritable> values, Context context)
             throws IOException, InterruptedException {
             sumWrt.set(Utils.sumValues(values));
             context.write(key, sumWrt);
         }
     }
 
 
     // Partition by 'decade + word' hash code.
     public static class PartitionerClass extends Partitioner<Text, LongWritable> {
         @Override
         public int getPartition(Text key, LongWritable value, int numPartitions) {
             String[] words = key.toString().split(Utils.delim);
             Text data = new Text(words[0] + Utils.delim + words[1]);
             // Calculate data's hash code, and bound by Integer maximum value,
             // then calculate the result(mod numPartition).
             return (data.hashCode() & Integer.MAX_VALUE) % numPartitions;
         }
     }
 
 
     // If key is 'decade, w, *' Write { decade, w, * : c(w) }
     // Else key is <w,wi>: Write { <w,wi> : c(w), c(w,wi) }
     public static class ReduceClass extends Reducer<Text, LongWritable, Text, Text> {
 
         // Corpus word counter by decade.
         public static enum N_COUNTER {
             N_190,  // 1900
             N_191,  // 1910
             N_192,
             N_193,
             N_194,
             N_195,
             N_196,
             N_197,
             N_198,
             N_199,
             N_200,
             N_201;  // 2010
         };
 
         private long cw;  // c(w)
 
 
         // Update decade counter.
         private void updateCounter(String decade, Context context) {
             N_COUNTER currentDecade = N_COUNTER.valueOf("N_" + decade);
             Counter counter = null;
 
             switch (currentDecade) {
                 case N_190:
                     counter = context.getCounter(N_COUNTER.N_190);
                     break;
                 case N_191:
                     counter = context.getCounter(N_COUNTER.N_191);
                     break;
                 case N_192:
                     counter = context.getCounter(N_COUNTER.N_192);
                     break;
                 case N_193:
                     counter = context.getCounter(N_COUNTER.N_193);
                     break;
                 case N_194:
                     counter = context.getCounter(N_COUNTER.N_194);
                     break;
                 case N_195:
                     counter = context.getCounter(N_COUNTER.N_195);
                     break;
                 case N_196:
                     counter = context.getCounter(N_COUNTER.N_196);
                     break;
                 case N_197:
                     counter = context.getCounter(N_COUNTER.N_197);
                     break;
                 case N_198:
                     counter = context.getCounter(N_COUNTER.N_198);
                     break;
                 case N_199:
                     counter = context.getCounter(N_COUNTER.N_199);
                     break;
                 case N_200:
                     counter = context.getCounter(N_COUNTER.N_200);
                     break;
                 case N_201:
                     counter = context.getCounter(N_COUNTER.N_201);
                     break;
             }
 
             if (counter != null) {
                 counter.increment(cw);
             }
         }
 
 
         @Override
         public void reduce(Text key, Iterable<LongWritable> values, Context context)
             throws IOException, InterruptedException {
 
             String[] words = key.toString().split(Utils.delim);
             String decade = words[0], wi = words[2];
             long sum = Utils.sumValues(values);
 
             if (wi.equals(wordHeader)) {
                 // 'decade, w,*' case:
                 cw = sum;
                 updateCounter(decade, context);
             } else {
                 String val = Long.toString(cw) + Utils.delim + Long.toString(sum);
                 context.write(key, new Text(val));
             }
         }
     }
 
 
     // Google N-Gram reader.
     public static class MySequenceFileInputFormat extends SequenceFileInputFormat<LongWritable,Text> {}
 
     public static void main(String[] args) throws Exception {
         Configuration conf = new Configuration();
 
         conf.set("mapred.reduce.slowstart.completed.maps", "1");
 
         // conf.set("mapred.map.tasks", Utils.mapTasks);
         // conf.set("mapred.reduce.tasks", Utils.reduceTasks);
 
         Job job = new Job(conf, "Count");
 
         // Read from Google N-Gram.
         // TODO try to use original SequenceFileInputFormat.class.
         job.setInputFormatClass(MySequenceFileInputFormat.class);
 
         job.setJarByClass(Count.class);
         job.setMapperClass(MapClass.class);
         job.setPartitionerClass(PartitionerClass.class);
         job.setCombinerClass(CombineClass.class);
         job.setReducerClass(ReduceClass.class);
 
         job.setOutputKeyClass(Text.class);
         job.setOutputValueClass(LongWritable.class);
 
         // TODO change args to 1,2 when testing on amazon ecr.
         FileInputFormat.addInputPath(job, new Path(args[Utils.argInIndex]));
         FileOutputFormat.setOutputPath(job, new Path(args[Utils.argInIndex + 1]));
 
         boolean result = job.waitForCompletion(true);
 
         // Set decade counters for next job.
         if (result) {
             Counters counters = job.getCounters();
             long[] decadeCounters = {
                 counters.findCounter(ReduceClass.N_COUNTER.N_190).getValue(),
                 counters.findCounter(ReduceClass.N_COUNTER.N_191).getValue(),
                 counters.findCounter(ReduceClass.N_COUNTER.N_192).getValue(),
                 counters.findCounter(ReduceClass.N_COUNTER.N_193).getValue(),
                 counters.findCounter(ReduceClass.N_COUNTER.N_194).getValue(),
                 counters.findCounter(ReduceClass.N_COUNTER.N_195).getValue(),
                 counters.findCounter(ReduceClass.N_COUNTER.N_196).getValue(),
                 counters.findCounter(ReduceClass.N_COUNTER.N_197).getValue(),
                 counters.findCounter(ReduceClass.N_COUNTER.N_198).getValue(),
                 counters.findCounter(ReduceClass.N_COUNTER.N_199).getValue(),
                 counters.findCounter(ReduceClass.N_COUNTER.N_200).getValue(),
                 counters.findCounter(ReduceClass.N_COUNTER.N_201).getValue()
             };
 
             // Write counters to file.
             StringBuilder sb = new StringBuilder();
             sb.append("counters\t");
             for (int i = 0; i < decadeCounters.length; i++) {
                 sb.append(Long.toString(decadeCounters[i])).append("\t");
             }
             sb.append("\n");
 
             // Write totalRecord from task counter to file, so we could pass it to next steps.
             long totalRecords = counters.findCounter("org.apache.hadoop.mapred.Task$Counter", "MAP_OUTPUT_RECORDS").getValue();
             sb.append("totalrecords\t").append(Long.toString(totalRecords));
 
             Utils.uploadToS3(sb.toString(), Utils.countOutput + Utils.countersFileName);
         }
 
         System.exit(result ? 0 : 1);
     }
 }
