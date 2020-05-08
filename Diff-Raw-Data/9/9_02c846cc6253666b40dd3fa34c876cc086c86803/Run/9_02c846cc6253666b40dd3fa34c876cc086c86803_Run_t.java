 import weka.core.Instance;
 import weka.core.InstanceComparator;
 import weka.core.Instances;
 import weka.core.converters.ArffLoader.ArffReader;
 import weka.core.converters.ConverterUtils.DataSource;
 import weka.core.Utils;
 import weka.classifiers.AbstractClassifier;
 import weka.classifiers.AggregateableEvaluation;
 import weka.classifiers.Classifier;
 import weka.classifiers.Evaluation;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.Console;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Random;
 import java.util.StringTokenizer;
 import java.util.List;
 
 import org.apache.hadoop.mapred.JobConf;
 import org.apache.hadoop.mapreduce.lib.input.FileSplit;
 import org.apache.hadoop.mapreduce.InputSplit;
 import org.apache.hadoop.mapreduce.JobContext;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.BlockLocation;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.BytesWritable;
 import org.apache.hadoop.io.IOUtils;
 import org.apache.hadoop.io.NullWritable;
 import org.apache.hadoop.io.RawComparator;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.JobContext;
 import org.apache.hadoop.mapreduce.JobID;
 import org.apache.hadoop.mapreduce.Mapper;
 import org.apache.hadoop.mapreduce.InputFormat;
 import org.apache.hadoop.mapreduce.RecordReader;
 import org.apache.hadoop.mapreduce.TaskAttemptContext;
 import org.apache.hadoop.mapreduce.TaskAttemptID;
 import org.apache.hadoop.mapreduce.TaskID;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
 //import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
 import org.apache.hadoop.util.GenericOptionsParser;
 import org.apache.hadoop.mapreduce.InputSplit;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.FileStatus;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.io.Text;
 
 import org.apache.hadoop.mapreduce.Reducer;
 import org.apache.hadoop.mapreduce.Mapper.Context;
 
 import org.apache.hadoop.mapreduce.lib.input.FileSplit;
 import org.apache.hadoop.fs.FSDataInputStream;
 import org.apache.hadoop.fs.FSDataOutputStream;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.FileStatus;
 import org.apache.hadoop.fs.BlockLocation;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.JobContext;
 import org.apache.hadoop.mapreduce.Mapper;
 import org.apache.hadoop.mapreduce.InputFormat;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
 import org.apache.hadoop.util.GenericOptionsParser;
 import org.apache.hadoop.mapreduce.InputSplit;
 
 
 public class Run {
 	
 	public static class WekaInputFormat extends TextInputFormat{
 		
 	    public List<InputSplit> getSplits(JobContext job) throws IOException {
 	      long minSize = Math.max(getFormatMinSplitSize(), getMinSplitSize(job));
 	      long maxSize = getMaxSplitSize(job);
 	      // generate splits         
 	      List<InputSplit> splits = new ArrayList<InputSplit>();
 	      for (FileStatus file: listStatus(job)) {
 	         Path path = file.getPath();    
 	         FileSystem fs = path.getFileSystem(job.getConfiguration());
 	         long length = file.getLen();  //number of bytes in this file
 	         BlockLocation[] blkLocations = fs.getFileBlockLocations(file, 0, length);
 	         if(length != 0) {
 	           int count=job.getConfiguration().getInt("Run-num.splits", 1);
 	           for(int t=0;t<count;t++)
 	              splits.add(new FileSplit(path, 0, length, blkLocations[0].getHosts())); //one file on split
 	         } else {
 	           //Create empty hosts array for zero length files
 	           splits.add(new FileSplit(path, 0, length, new String[0]));
 	         }
 	      }
 	      return splits;
 	    }
 	    
 	}	
 	
 	public static class WekaMap extends Mapper<Object, Text, Text, AggregateableEvaluation>{
 		
 	    //private Text word = new Text();
 	    private Instances randData = null;
 	    private Classifier cls = null;
 
 	    private AggregateableEvaluation eval = null;
 	    private Classifier clsCopy = null;
 	    
 	    private String numMaps = "10";
 	    private String classname = "weka.classifiers.lazy.IBk";
 	    private int seed = 20;
 	    
 	    public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
 	      String line=value.toString();
 	      
 	      Configuration conf = new Configuration();
 	      FileSystem fileSystem = FileSystem.get(conf);
 	     
 	      Path path = new Path(line);
 	      if (!fileSystem.exists(path)) {
 	          System.out.println("File does not exists");
 	          return;
 	      }
 	      
 	      JobID test = context.getJobID();
 	      TaskAttemptID tid = context.getTaskAttemptID();
 	      
 	      Configuration wekaConfig = context.getConfiguration();
 	      numMaps = wekaConfig.get("Run-num.splits");
 	      classname = wekaConfig.get("Run.classify");
 	      
 	      String[] splitter = tid.toString().split("_");
 	      String jobNumber = "";
 	      int n = 0;
 	      
 	      if (splitter[4].length() > 0)
 	      {
 	    	  jobNumber = splitter[4].substring(splitter[4].length() - 1);
 	    	  n = Integer.parseInt(jobNumber);
 	      }
 	      
 	      FileSystem fs = FileSystem.get(context.getConfiguration());
 
 	      context.setStatus("About to read in the arff file");
 	      readArff(fs, line);
 	      
 	      context.setStatus("arff complete, initialising aggregateable eval");
 	      try{
 			eval = new AggregateableEvaluation(randData);
 	      }
 	      catch (Exception e1) {
 			e1.printStackTrace();
 	      }
 	      
 	      Instances trainInstance = randData.trainCV(Integer.parseInt(numMaps), n);
 	      Instances testInstance = randData.testCV(Integer.parseInt(numMaps), n);
 	      
 	      //Using IBk
 	      String[] opts = new String[3];
 	      if (classname.equals("weka.classifiers.lazy.IBk"))
 	      {	    	  
 		      opts[0] = "";
 		      opts[1] = "-K";
 		      opts[2] = "1";
 	      }
 	      else if (classname.equals("weka.classifiers.trees.J48"))
 	      {
 	    	  opts[0] = "";
 		      opts[1] = "-C";
 		      opts[2] = "0.25"; 
 	      }
 	      else if (classname.equals("weka.classifiers.bayes.NaiveBayes"))
 	      {
 	    	  opts[0] = "";
 		      opts[1] = "";
 		      opts[2] = "";    			 
 	      }
 	      
 	      try 
 	      {
 			cls = (Classifier) Utils.forName(Classifier.class,classname,opts);
 	      } 
 	      catch (Exception e) 
 	      {
 	    	
 			e.printStackTrace();
 	      }
 	      
 	      long beforeAbstract = 0;
 	      long beforeBuildClass = 0;
 	      long afterBuildClass = 0;
 	      long beforeEvalClass = 0;
 	      long afterEvalClass = 0;
 	      
 	      try{
 	    	  context.setStatus("About to create the classifier");
 	    	  System.out.println(new Timestamp(System.currentTimeMillis()));
 	    	  beforeAbstract = System.currentTimeMillis();
 	    	  clsCopy = AbstractClassifier.makeCopy(cls);
 	    	  beforeBuildClass = System.currentTimeMillis();
 	    	  System.out.println(new Timestamp(System.currentTimeMillis()));
 		      
 	    	  context.setStatus("Training the classifier");
 	    	  clsCopy.buildClassifier(trainInstance);
 		      afterBuildClass = System.currentTimeMillis();
 		      System.out.println(new Timestamp(System.currentTimeMillis()));
 		      beforeEvalClass = System.currentTimeMillis();
 		      
 		      context.setStatus("Evaluating the model");
 		      eval.evaluateModel(clsCopy, testInstance);
 		      afterEvalClass = System.currentTimeMillis();
 		      System.out.println(new Timestamp(System.currentTimeMillis()));
 		      
 		      //System.out.println("This is the map matrix");
 		      //System.out.println(eval.toMatrixString());
 		      context.setStatus("Complete");
 		      
 	      }
 	      catch (Exception e){
 	    	  System.out.println("Debugging strarts here");
 	    	  e.printStackTrace();
 	      }
 	      
 	      long abstractTime = beforeBuildClass - beforeAbstract;
 	      long buildTime = afterBuildClass - beforeBuildClass;
 	      long evalTime = afterEvalClass - beforeEvalClass;
 	      
 	      System.out.println("The value of abstract time: " + abstractTime);
 	      System.out.println("The value of Build time: " + buildTime);
 	      System.out.println("The value of Eval time: " + evalTime);
 	          
 	      context.write(new Text(line), eval);
 	    }
 	    
 	    public void writeResult()
 	    {
 
 	    }
 	    
 	    
 	    public void readArff(FileSystem fs, String filePath) throws IOException, InterruptedException
 	    {
 	    	BufferedReader reader;
 	    	DataInputStream d;
 	    	ArffReader arff;
 	    	Instance inst;
 	    	Instances data;
 	    		    	
 	    	try
 	    	{
 	    		d = new DataInputStream(fs.open(new Path(filePath)));
 			    reader = new BufferedReader(new InputStreamReader(d));
 			    arff = new ArffReader(reader, 1000);
 			    data = arff.getStructure();
 			    data.setClassIndex(data.numAttributes() - 1);
 			    
 			    while ((inst = arff.readInstance(data)) != null) 
 			    {
 			        data.add(inst);
 			    }
 			    		    
 			    reader.close();
 			    
 			    Random rand = new Random(seed);
 			    randData = new Instances(data);
 			    randData.randomize(rand);
 			    if (randData.classAttribute().isNominal())
 			      randData.stratify(Integer.parseInt(numMaps));
 	    	}
 	    	catch (IOException e)
 	    	{
 	    		e.printStackTrace();
 	    	}  	
 	    		    	
 	    }
 	    
 	}
 	
 	public static class WekaReducer extends Reducer<Text, AggregateableEvaluation, Text, IntWritable>{
 		private Text result = new Text();
 		private Evaluation evalAll = null;
 		private IntWritable test = new IntWritable();
 		
 		private AggregateableEvaluation aggEval;
 			
 		public void reduce(Text key, Iterable<AggregateableEvaluation> values, Context context) throws IOException, InterruptedException {
 		
 			int sum = 0;
 			System.out.println(new Timestamp(System.currentTimeMillis()));
 			long beforeReduceTime = System.currentTimeMillis();
 			for (AggregateableEvaluation val : values) 
 			{
 				if (sum == 0)
 				{
 					try {
 						aggEval = val;
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 				}
 				else
 				{
 					aggEval.aggregate(val);
 				}
 				
 				try {
 					//System.out.println("This is the map result");
 					//System.out.println(aggEval.toMatrixString());
 				} catch (Exception e) {
 					e.printStackTrace();
 				}						
 				
 				sum += 1;
 			}
 			
 			try {
 				//System.out.println("This is reduce matrix");
 				//System.out.println(aggEval.toMatrixString());
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			
 			context.write(key, new IntWritable(sum));
 			long afterReduceTime = System.currentTimeMillis();
 			long reduceTime = afterReduceTime - beforeReduceTime;
 			System.out.println("The value of reduce time is: " + reduceTime);
 			System.out.println(new Timestamp(System.currentTimeMillis()));
 		}
 	}
 	
 	public static void main(String[] args) throws Exception {
 		Configuration conf = new Configuration();
 	    
 		if (args.length != 4) {
 	      System.err.println("Usage: run #splits classifier <in> <out>");
 	      System.exit(1);
 	    }
	    conf.setInt("Run-num.splits", Integer.parseInt(args[0]));
	    conf.setStrings("Run.classify", args[1]);
 	    conf.set("io.serializations","org.apache.hadoop.io.serializer.JavaSerialization," + "org.apache.hadoop.io.serializer.WritableSerialization");
 	    
 	    Job job = new Job(conf, "WEKA-MapReduce");
 	    job.setJarByClass(Run.class);
 	    job.setMapperClass(WekaMap.class);
 	    job.setReducerClass(WekaReducer.class);
 	    job.setNumReduceTasks(1);
 	    
 	    //This sections set the values of the <K2, V2>
 	    job.setOutputKeyClass(Text.class);
 	    //job.setOutputValueClass(weka.classifiers.trees.J48.class);
 	    	
 	    job.setOutputValueClass(AggregateableEvaluation.class);
 	    
 	    //Set the input and output directories
	    FileInputFormat.addInputPath(job, new Path(args[2]));
	    FileOutputFormat.setOutputPath(job, new Path(args[3]));
 	    
 	    //Set the input type of the environment
 	    //In this case we are overriding TextInputFormat
 	    job.setInputFormatClass(WekaInputFormat.class);
 	    
 	    System.exit(job.waitForCompletion(true) ? 0 : 1);
 		
 		}
 
 }
