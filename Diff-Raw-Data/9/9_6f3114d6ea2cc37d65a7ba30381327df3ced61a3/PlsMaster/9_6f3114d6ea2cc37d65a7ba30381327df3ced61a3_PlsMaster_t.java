 package pls;
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.FSDataOutputStream;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.BytesWritable;
 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.io.SequenceFile;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapred.FileInputFormat;
 import org.apache.hadoop.mapred.FileOutputFormat;
 import org.apache.hadoop.mapred.JobClient;
 import org.apache.hadoop.mapred.JobConf;
 import org.apache.hadoop.mapred.SequenceFileInputFormat;
 import org.apache.hadoop.mapred.SequenceFileOutputFormat;
 import org.apache.hadoop.mapred.TextInputFormat;
 import org.apache.hadoop.mapred.TextOutputFormat;
 import org.apache.log4j.Logger;
 
 import pls.stats.PlsJobStats;
 import pls.tsp.Greedy;
 import pls.tsp.TspLsCity;
 import pls.tsp.TspSaSolution;
 import pls.tsp.TspUtils;
 
 public class PlsMaster {
 
 	private static final Logger LOG = Logger.getLogger(PlsMaster.class);
 	
 	private boolean runLocal;
 	
 	public PlsMaster() {
 		
 	}
 	
 	public PlsMaster(boolean runLocal) {
 		this.runLocal = runLocal;
 	}
 	
 	public void run(int numRuns, List<PlsSolution> startSolutions, String dir, Class mapperClass,
 			Class reducerClass, PlsMetadata metadata, String problemName) throws IOException {
 		//write out start solutions to HDFS
 		Configuration conf = new Configuration();
 		
 		FileSystem fs = FileSystem.get(conf);
 		Path dirPath = new Path(dir);
 		fs.delete(dirPath, true);
 		
 		//write out initial input file
 		Path initDirPath = new Path(dirPath, "0/");
 		if (!fs.mkdirs(initDirPath)) {
 			LOG.info("Failed to create directory: " + initDirPath);
 		}
 		
 		Path initFilePath = new Path(initDirPath, "part-00000");
 		
 		long firstFinishTime = System.currentTimeMillis() + metadata.getRoundTime();
 		//write out solutions
 		SequenceFile.Writer writer = new SequenceFile.Writer(fs, conf, initFilePath, BytesWritable.class, BytesWritable.class);
 		for (PlsSolution sol : startSolutions) {
 			ByteArrayOutputStream baos = new ByteArrayOutputStream();
 			DataOutputStream dos = new DataOutputStream(baos);
 			sol.writeToStream(dos);
 			metadata.write(dos);
			dos.close();
 			BytesWritable solWritable = new BytesWritable(baos.toByteArray());
 			writer.append(PlsUtil.getMapSolKey(firstFinishTime), solWritable);
 		}
		writer.close();
 		//write out metadata
 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
 		DataOutputStream dos = new DataOutputStream(baos);
 
 		//int k = Math.max(1, (int)Math.round(startSolutions.size() / 2 + .5));
 		LOG.info("k=" + metadata.getK());
 		
 		PlsJobStats stats = new PlsJobStats();
 		stats.setK(metadata.getK());
 		stats.setLsRunTime(metadata.getRoundTime());
 		stats.setNumMappers(startSolutions.size());
 		stats.setNumRounds(numRuns);
 		stats.setProblemName(problemName);
 		//run the waves
 		for (int i = 0; i < numRuns; i++) {
 			Path inputPath = new Path(dirPath, i + "/");
 			Path outputPath = new Path(dirPath, (i+1) + "/");
 			long start = System.currentTimeMillis();
 			LOG.info("About to run job " + i);
 			runHadoopJob(inputPath, outputPath, startSolutions.size(), mapperClass, reducerClass);
 			long end = System.currentTimeMillis();
 			LOG.info("Took " + (end-start) + " ms");
 			stats.reportRoundTime((int)(end-start));
 		}
 		writeStatsFile(dirPath, stats, fs);
 	}
 
 	/**
 	 * Involves sending a solution (or location of a solution) to each node.
 	 */
 	private void runHadoopJob(Path inputPath, Path outputPath, int numMaps, Class mapperClass, Class reducerClass)
 		throws IOException {
 		JobConf conf = new JobConf();
 
 		if (runLocal) {
 			conf.set("mapred.job.tracker", "local");
 		}
 		
 		conf.setOutputKeyClass(BytesWritable.class);
 		conf.setOutputValueClass(BytesWritable.class);
 		conf.setMapOutputKeyClass(BytesWritable.class);
 		conf.setMapOutputValueClass(BytesWritable.class);
 		
 		conf.setJar("tspls.jar");
 		
 		conf.setMapperClass(mapperClass);
 		conf.setReducerClass(reducerClass);
 		
 		conf.setSpeculativeExecution(false);
 		
 		conf.setInputFormat(SequenceFileInputFormat.class);
 		conf.setOutputFormat(SequenceFileOutputFormat.class);
 		
 		conf.setNumMapTasks(numMaps);
 		conf.setNumReduceTasks(1);
 		
 		FileInputFormat.addInputPath(conf, inputPath);
 		FileOutputFormat.setOutputPath(conf, outputPath);
 		
 		JobClient.runJob(conf);
 	}
 	
 	private void writeStatsFile(Path dir, PlsJobStats stats, FileSystem fs) throws IOException {
 		Path statsPath = new Path(dir, "jobstats.stats");
 		FSDataOutputStream os = fs.create(statsPath);
 		String report = stats.makeReport();
 		os.writeUTF(report);
 		os.close();
 	}
 }
