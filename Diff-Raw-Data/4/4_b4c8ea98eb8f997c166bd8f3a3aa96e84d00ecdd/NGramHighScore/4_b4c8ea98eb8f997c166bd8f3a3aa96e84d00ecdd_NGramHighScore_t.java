 import java.io.BufferedReader;
 import java.io.DataInput;
 import java.io.DataOutput;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.FSDataInputStream;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.IOUtils;
 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.io.Writable;
 import org.apache.hadoop.mapred.FileInputFormat;
 import org.apache.hadoop.mapred.FileOutputFormat;
 import org.apache.hadoop.mapred.FileSplit;
 import org.apache.hadoop.mapred.InputSplit;
 import org.apache.hadoop.mapred.JobClient;
 import org.apache.hadoop.mapred.JobConf;
 import org.apache.hadoop.mapred.MapReduceBase;
 import org.apache.hadoop.mapred.Mapper;
 import org.apache.hadoop.mapred.OutputCollector;
 import org.apache.hadoop.mapred.RecordReader;
 import org.apache.hadoop.mapred.Reducer;
 import org.apache.hadoop.mapred.Reporter;
 import org.apache.hadoop.mapred.TextOutputFormat;
 
 public class NGramHighScore {
 
   public static class FullFileInputFormat extends FileInputFormat<LongWritable, Text> {
 
     @Override
     public boolean isSplitable(FileSystem fs, Path filename) {
       return false;
     }
 
     @Override
     public RecordReader<LongWritable, Text> getRecordReader(InputSplit split,
         JobConf job, Reporter reporter) throws IOException {
       return new FullFileRecordReader((FileSplit) split, job);
     }
 
   }
 
   public static class FullFileRecordReader implements RecordReader<LongWritable, Text> {
 
     private boolean hasProcessed;
 
     private FileSplit fileSplit;
     private Configuration conf;
 
     public FullFileRecordReader(FileSplit split, JobConf job) {
       hasProcessed = false;
       this.fileSplit = split;
       this.conf = job;
     }
 
     /*Example from sritchie: https://gist.github.com/sritchie/808035*/
     @Override
     public boolean next(LongWritable arg0, Text value) throws IOException {
       if (!hasProcessed) {
         byte[] contents = new byte[(int) fileSplit.getLength()];
         Path file = fileSplit.getPath();
 
         FileSystem fs = file.getFileSystem(conf);
         FSDataInputStream in = null;
         try {
           in = fs.open(file);
           IOUtils.readFully(in, contents, 0, contents.length);                
           value.set(contents, 0, contents.length);
         } finally {
           IOUtils.closeStream(in);
         }
         hasProcessed = true;
         return true;
       }
       return false;
     }
 
     @Override
     public void close() throws IOException {}
 
     @Override
     public LongWritable createKey() {
       return new LongWritable();
     }
 
     @Override
     public Text createValue() {
       return new Text();
     }
 
     @Override
     public long getPos() throws IOException {
       if (hasProcessed) return fileSplit.getLength();
       return 0;
     }
 
     @Override
     public float getProgress() throws IOException {
       if (hasProcessed) return 1.0f;
       return 0.0f;
     }
 
 
   }
 
 
 
   public static class Map extends MapReduceBase implements Mapper<LongWritable, Text, Text, PageScorePair> {
 
     private static final String DUMMY_KEY = "dummy";
 
     /* TODO: check if value is the entire chunk*/
     public void map(LongWritable key, Text value, OutputCollector<Text, PageScorePair> output, Reporter reporter) throws IOException {
       String chunk = value.toString();
 
       System.out.println("chunk length is " + chunk.length() + " units");
       
       //TODO: replace this with an nGram set passed in as a parameter, and nGramSize from JobConf
       Set<String> nGrams = new HashSet<String>();
       int nGramSize = 4;
 
       Pattern p = Pattern.compile("<title>([^<]*)</title>");
       Matcher m = p.matcher(chunk);
      if (!m.find()) {
        return;
      }
 
       boolean noPagesLeft = false;
       while (true) {
         String title = m.group(1);
 
         int pageStartIndex = m.end();
         int pageEndIndex;
         if (m.find()) {
           pageEndIndex = m.start();
           noPagesLeft = false;
         } else {
           pageEndIndex = chunk.length();
           noPagesLeft = true;
         }
 
         String pageText = chunk.substring(pageStartIndex, pageEndIndex);
         Tokenizer tokenizer = new Tokenizer(pageText);
 
         int count = 0;
         int i = 0;
         String justProcessedNGram = "";
         while (tokenizer.hasNext()) {
           String word = tokenizer.next();
           if (i != nGramSize) {
             i++;
             if (i == nGramSize) {
               justProcessedNGram += word;
               if (nGrams.contains(justProcessedNGram)) {
                 count++;
               }
             } else {
               justProcessedNGram += word + " ";
             }
           } else {
             justProcessedNGram = justProcessedNGram.substring(justProcessedNGram.indexOf(" ") + 1) + " " + word;
             if (nGrams.contains(justProcessedNGram)) {
               count++;
             }
           }        
         }
 
         output.collect(new Text(DUMMY_KEY), new PageScorePair(title, count));
 
         if (noPagesLeft) {
           break;
         }
       }
     }    
 
   }
 
   public static class Reduce extends MapReduceBase implements Reducer<Text, PageScorePair, Text, PageScorePair> { 
 
     private static final String DUMMY_KEY = "dummy";
 
     public void reduce(Text key, Iterator<PageScorePair> values, OutputCollector<Text, PageScorePair> output, Reporter reporter) throws IOException {
       PageScorePair top = null;
       PageScorePair tracker = null;
       while (values.hasNext()) {
         tracker = values.next();
         if (top == null) {
           top = tracker;
           continue;
         }
         if (top.getScore() < tracker.getScore()) {
           top = tracker;
         } else if (top.getScore() == tracker.getScore()) {
           if (top.getPageTitle().compareTo(tracker.getPageTitle()) < 0) {
             top = tracker;
           }
         }
       }
       if (top != null)
         output.collect(new Text(DUMMY_KEY), top);
     }
 
   }
 
   public static class PageScorePair implements Writable{
     private String pageTitle;
     private int score;
 
     public PageScorePair() {}
 
     public PageScorePair(String pageTitle, int score) {
       this.pageTitle = pageTitle;
       this.score = score;
     }
 
     public String getPageTitle() {
       return pageTitle;
     }
 
     public int getScore() {
       return score;
     }
 
     @Override
     public void readFields(DataInput in) throws IOException {
       pageTitle = in.readLine();
       score = in.readInt();
     }
 
     @Override
     public void write(DataOutput out) throws IOException {
       out.writeBytes(pageTitle);
       out.writeInt(score);
     }
 
     public static PageScorePair read(DataInput in) throws IOException {
       PageScorePair psp = new PageScorePair();
       psp.readFields(in);
       return psp;
     }
   }
 
 
   public static void main(String[] args) throws Exception {
     File file = new File(args[1]);
     BufferedReader reader = null;
     int nGramSize = Integer.parseInt(args[0]);
     Set<String> nGrams = new HashSet<String>();
 
     createNGramSetFromFile(file, reader, nGramSize, nGrams);
 
     JobConf conf = new JobConf(NGramHighScore.class);
     conf.setJobName("NGramHighScore");
 
     conf.setOutputKeyClass(Text.class);
     conf.setOutputValueClass(IntWritable.class);
 
     conf.setMapperClass(Map.class);
     conf.setCombinerClass(Reduce.class);
     conf.setReducerClass(Reduce.class);
 
     conf.setInputFormat(FullFileInputFormat.class);
     conf.setOutputFormat(TextOutputFormat.class);
 
     FileInputFormat.setInputPaths(conf, new Path(args[2]));
     FileOutputFormat.setOutputPath(conf, new Path(args[3]));
 
     JobClient.runJob(conf);
   }
 
   private static void createNGramSetFromFile(File file, BufferedReader reader,
       int nGramSize, Set<String> nGrams) {
     try {
       reader = new BufferedReader(new FileReader(file));
       createNGramSet(reader, nGramSize, nGrams);
     } catch (IOException e) {
       e.printStackTrace();
     } finally {
       try {
         if (reader != null) {
           reader.close();
         }
       } catch (IOException e) {
         e.printStackTrace();
       }
     }
   }
 
   private static void createNGramSet(BufferedReader reader, int nGramSize, Set<String> nGrams) throws IOException {
     String text = null;
 
     int i = 0;
     String justAddedNGram = "";
 
     while ((text = reader.readLine()) != null) {
       Tokenizer tokenizer = new Tokenizer(text);
       while (tokenizer.hasNext()) {
         if (nGrams.size() == 0) {
           i++;
           if (i != nGramSize) {
             justAddedNGram += tokenizer.next() + " ";
           } else {
             justAddedNGram += tokenizer.next();
             nGrams.add(justAddedNGram);
           }
         } else {
           justAddedNGram = justAddedNGram.substring(justAddedNGram.indexOf(" ") + 1) + " " + tokenizer.next();
           nGrams.add(justAddedNGram);
         }        
       }
     }
   }
 }
