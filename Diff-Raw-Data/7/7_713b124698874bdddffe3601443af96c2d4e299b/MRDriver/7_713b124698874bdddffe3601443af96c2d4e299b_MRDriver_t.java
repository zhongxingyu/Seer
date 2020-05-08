 /**************************************************************************************************************************
 
  * File: MRDriver.java
  * Authors: Justin A. DeBrabant (debrabant@cs.brown.edu)
 			Matteo Riondato (matteo@cs.brown.edu)
  * Last Modified: 12/27/2011
  
  * Description:
 	Driver for Hadoop implementation of parallel association rule mining.
  
  * Usage: java MRDriver <mapper id> <path to input database> <path to output local FIs> <path to output global FIs>
 	* mapper id - specifies which Map method should be used
 		1 for partition mapper, 2 for binomial mapper, 3 for weighted coin flip sampler 
 	* path to input database - path to file containing transactions in .dat format (1 transaction per line)
 	* local FI output - path to directory to write local (per-reducer) FIs
 	* global FI output - path to directory to write global FIs (combined from all local FIs)
  
 ***************************************************************************************************************************/
 
 import java.net.URI;
 import java.util.Arrays;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Hashtable;
 import java.util.Random;
 
 import org.apache.hadoop.conf.Configured;
 import org.apache.hadoop.filecache.DistributedCache;
 import org.apache.hadoop.fs.FileStatus;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.FSDataOutputStream;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.DefaultStringifier;
 import org.apache.hadoop.io.DoubleWritable;
 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.MapWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapred.lib.IdentityMapper;
 import org.apache.hadoop.mapred.FileOutputFormat;
 import org.apache.hadoop.mapred.JobClient;
 import org.apache.hadoop.mapred.JobConf;
 import org.apache.hadoop.mapred.SequenceFileInputFormat;
 import org.apache.hadoop.mapred.SequenceFileOutputFormat;
 import org.apache.hadoop.util.Tool;
 import org.apache.hadoop.util.ToolRunner;
 
 public class MRDriver extends Configured implements Tool
 {
 	public final int MR_TIMEOUT_MILLI = 60000000;
 	
 	public static void main(String args[]) throws Exception
 	{
 		if (args.length != 11)
 		{
 			System.out.println("usage: java MRDriver <epsilon> <delta> <minFreqPercent> <d> <datasetSize> <numSamples> <phi> <mapper id> <path to input database> " + 
 							   "<path to output local FIs> <path to output global FIs>");
 			System.exit(1); 
 		}
 
 		int res = ToolRunner.run(new MRDriver(), args);
 
 		System.exit(res);
 	}
 
 	public int run(String args[]) throws Exception
 	{
 		FileSystem fs = null;
 		Path samplesMapPath = null;
 
 		long job_start_time, job_end_time; 
 		long job_runtime; 
 		float epsilon = Float.parseFloat(args[0]);
 		double delta = Double.parseDouble(args[1]);
 		int minFreqPercent = Integer.parseInt(args[2]);
 		int d = Integer.parseInt(args[3]);
 		int datasetSize = Integer.parseInt(args[4]);
 		int numSamples = Integer.parseInt(args[5]);
 		double phi = Double.parseDouble(args[6]);
 		Random rand;
 
 
 		/************************ Job 1 (local FIM) Configuration ************************/
 		
 		JobConf conf = new JobConf(getConf()); 
 
 
 		/*
 		 * Compute the number of required "votes" for an itemsets to be
 		 * declared frequent 		 */
 		int reqApproxNum = reqApproxNum = (int)
 			Math.floor(numSamples*(1-phi)-Math.sqrt(numSamples*(1-phi)*2*Math.log(1/delta))
 					+ 1);
 		int sampleSize = (int) Math.ceil((2 / Math.pow(epsilon, 2))*(d + Math.log(1/ phi)));
 
 		conf.setInt("PARMM.reducersNum", numSamples);
 		conf.setInt("PARMM.datasetSize", datasetSize);
 		conf.setInt("PARMM.minFreqPercent", minFreqPercent);
 		conf.setInt("PARMM.sampleSize", sampleSize);
 		conf.setFloat("PARMM.epsilon", epsilon);
 
 		// Set the number of reducers equal to the number of samples, to
 		// maximize parallelism. Required by our Partitioner.
 		conf.setNumReduceTasks(numSamples);
 
 		// XXX: why do we disable the speculative execution? MR
 		conf.setBoolean("mapred.reduce.tasks.speculative.execution", false); 
 		conf.setInt("mapred.task.timeout", MR_TIMEOUT_MILLI); 
 
 		/* 
 		 * Enable compression of map output.
 		 *
 		 * We do it for this job and not for the aggregation one because
 		 * each mapper there only print out one record for each itemset,
 		 * so there isn't much to compress, I'd say. MR
 		 *
 		 * XXX: We should use LZO compression because it's faster. We
 		 * should check whether it is the default for Amazon MapReduce.
 		 * MR
 		 */
 		conf.setBoolean("mapred.compress.map.output", true); 
 
 		conf.setJarByClass(MRDriver.class);
 			
 		conf.setMapOutputKeyClass(IntWritable.class); 
 		conf.setMapOutputValueClass(Text.class); 
 			
 		conf.setOutputKeyClass(Text.class); 
 		conf.setOutputValueClass(DoubleWritable.class); 
 
 		conf.setInputFormat(SequenceFileInputFormat.class);
 		// We write the collections found in a reducers as a SequenceFile 
 		conf.setOutputFormat(SequenceFileOutputFormat.class);
 		SequenceFileOutputFormat.setOutputPath(conf, new Path(args[9]));
 
 		
 		// set the mapper class based on command line option
 		switch(Integer.parseInt(args[7]))
 		{
 			case 1:
 				System.out.println("running partition mapper..."); 
 				SequenceFileInputFormat.addInputPath(conf, new Path(args[8]));
 				conf.setMapperClass(PartitionMapper.class);
 				break;
 			case 2:
 				System.out.println("running binomial mapper..."); 
 				SequenceFileInputFormat.addInputPath(conf, new Path(args[8]));
 				conf.setMapperClass(BinomialSamplerMapper.class);
 				break;
 			case 3:
 				System.out.println("running coin mapper..."); 
 				SequenceFileInputFormat.addInputPath(conf, new Path(args[8]));
 				conf.setMapperClass(CoinFlipSamplerMapper.class);
 			case 4:
 				System.out.println("running sampler mapper..."); 
 				SequenceFileInputFormat.addInputPath(conf, new Path(args[8]));
 				conf.setMapperClass(InputSamplerMapper.class);
 
 				// create a random sample of size T*m
 				rand = new Random();
 				job_start_time = System.nanoTime(); 
 				int[] samples = new int[numSamples * sampleSize];
 				for (int i = 0; i < numSamples * sampleSize; i++)
 				{
 					samples[i] = rand.nextInt(datasetSize);
 				}
 
 				// for each key in the sample, create a list of all T samples to which this key belongs
 				Hashtable<LongWritable, ArrayList<IntWritable>> hashTable = new Hashtable<LongWritable, ArrayList<IntWritable>>();
 				for (int i=0; i < numSamples * sampleSize; i++) 
 				{
 					ArrayList<IntWritable> sampleIDs = null;
 					LongWritable key = new LongWritable(samples[i]);
 					if (hashTable.containsKey(key))  
 						sampleIDs = hashTable.get(key);
 					else
 						sampleIDs = new ArrayList<IntWritable>();
 					sampleIDs.add(new IntWritable(i / sampleSize));
 					hashTable.put(key, sampleIDs);
 				}
 
 				/*
 				 * Convert the Hastable to a MapWritable which we will
 				 * write to HDFS and distribute to all Mappers using
 				 * DistributedCache
 				 */
 				MapWritable map = new MapWritable();
 				for (LongWritable key : hashTable.keySet())
 				{
 					ArrayList<IntWritable> sampleIDs = hashTable.get(key);
 					IntArrayWritable sampleIDsIAW = new IntArrayWritable();
 					sampleIDsIAW.set(sampleIDs.toArray(new IntWritable[sampleIDs.size()]));
 					map.put(key, sampleIDsIAW);
 				}
 
 				fs = FileSystem.get(URI.create("samplesMap.ser"), conf);
 				samplesMapPath = new Path("samplesMap.ser");
 				FSDataOutputStream out = fs.create(samplesMapPath, true);
 				map.write(out);
 				out.sync();
 				out.close();
 				DistributedCache.addCacheFile(new URI(fs.getWorkingDirectory() + "/samplesMap.ser#samplesMap.ser"), conf);
 				// stop the sampling timer	
 				job_end_time = System.nanoTime(); 
 				job_runtime = (job_end_time-job_start_time) / 1000000; 
 				System.out.println("sampling runtime (milliseconds): " + job_runtime);	
 				break; // end switch case
 			case 5:	
 				System.out.println("running random integer partition mapper..."); 
 				conf.setInputFormat(WholeSplitInputFormat.class);
 				Path inputFilePath = new Path(args[8]);
 				WholeSplitInputFormat.addInputPath(conf, inputFilePath);
 				conf.setMapperClass(RandIntPartSamplerMapper.class);
 				// Compute number of map tasks.
 				// XXX I hope this is correct =)
 				fs = inputFilePath.getFileSystem(conf);
 				FileStatus inputFileStatus = fs.getFileStatus(inputFilePath);
 				long len = inputFileStatus.getLen();
 				long blockSize = inputFileStatus.getBlockSize();
 				conf.setInt("mapred.min.split.size", (int) blockSize);
 				int mapTasksNum = ((int) (len / blockSize)) + 1;
 				// TODO 2) Extract random integer partition of total sample
 				// size into up to mapTasksNum partitions.
 				// XXX I'm not sure this is a correct way to do
 				// it.
 				rand = new Random();
 				int sum = 0;
 				int i = 0;
 				IntWritable[] toSampleArr = new IntWritable[mapTasksNum];
 				for (i = 0; i < mapTasksNum -1; i++) 
 				{
 					int size = rand.nextInt(numSamples * sampleSize - sum);
 					toSampleArr[i]= new IntWritable(size);
 					sum += size;
					if (sum > numSamples * sampleSize)
					{
						System.out.println("Something went wrong generating the sample Sizes");
						System.exit(1);
					}
					if (sum == numSamples * sampleSize)
 					{
 						break;
 					}
 				}
 				if (i == mapTasksNum -1) 
 				{
 					toSampleArr[i] = new IntWritable(numSamples * sampleSize - sum);
 				}
 				else 
 				{
 					for (; i < mapTasksNum; i++)
 					{
 						toSampleArr[i] = new IntWritable(0); 
 					}
 				}
 				Collections.shuffle(Arrays.asList(toSampleArr));
 				
 				DefaultStringifier.storeArray(conf, toSampleArr, "PARMM.toSampleArr");
 
 				break;
 			default:
 				System.err.println("Wrong Mapper ID. Can only be in [1,5]");
 				System.exit(1);
 				break;
 		}
 		
 		/*
 		 * We don't use the default hash partitioner because we want to
 		 * maximize the parallelism. That's why we also fix the number
 		 * of reducers.
 		 */
 		conf.setPartitionerClass(FIMPartitioner.class);
 
 		conf.setReducerClass(FIMReducer.class);
 			
 		job_start_time = System.nanoTime(); 
 		JobClient.runJob(conf);
 		job_end_time = System.nanoTime(); 
 
 		job_runtime = (job_end_time-job_start_time) / 1000000; 
 			
 		System.out.println("local FIM runtime (milliseconds): " + job_runtime);	
 	
 		/************************ Job 2 (aggregation) Configuration ************************/
 		
 		JobConf confAggr = new JobConf(getConf());
 
 		confAggr.setInt("PARMM.reducersNum", numSamples);
 		confAggr.setInt("PARMM.reqApproxNum", reqApproxNum);
 		confAggr.setInt("PARMM.sampleSize", sampleSize);
 		confAggr.setFloat("PARMM.epsilon", epsilon);
 
 		// XXX: Why do we disable speculative execution? MR
 		confAggr.setBoolean("mapred.reduce.tasks.speculative.execution", false); 
 		confAggr.setInt("mapred.task.timeout", MR_TIMEOUT_MILLI); 
 
 		confAggr.setJarByClass(MRDriver.class);
 			
 		confAggr.setMapOutputKeyClass(Text.class); 
 		confAggr.setMapOutputValueClass(DoubleWritable.class); 
 			
 		confAggr.setOutputKeyClass(Text.class); 
 		confAggr.setOutputValueClass(Text.class); 
 			
 		confAggr.setMapperClass(IdentityMapper.class);
 		confAggr.setReducerClass(AggregateReducer.class);
 			
 		confAggr.setInputFormat(SequenceFileInputFormat.class);
 		SequenceFileInputFormat.addInputPath(confAggr, new Path(args[9]));
 
 		FileOutputFormat.setOutputPath(confAggr, new Path(args[10]));
 
 		job_start_time = System.nanoTime(); 
 		JobClient.runJob(confAggr);
 		job_end_time = System.nanoTime(); 
 			
 		job_runtime = (job_end_time-job_start_time) / 1000000; 
 			
 		System.out.println("aggregation runtime (milliseconds): " +
 				job_runtime); 
 
 		if (args[7].equals("4")) {
 			// Remove samplesMap file 
 			fs.delete(samplesMapPath, false);
 		}
 
 		return 0;
 	}
 }
 
