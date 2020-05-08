 package edu.berkeley.gamesman.hadoop;
 
 import java.io.IOException;
 
 import org.apache.hadoop.fs.FSDataInputStream;
 import org.apache.hadoop.fs.FSDataOutputStream;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.conf.Configured;
 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.mapred.*;
 import org.apache.hadoop.util.Tool;
 
 import edu.berkeley.gamesman.core.*;
 import edu.berkeley.gamesman.database.util.SplitDatabaseWritable;
 import edu.berkeley.gamesman.database.util.SplitDatabaseWritableList;
 import edu.berkeley.gamesman.hadoop.util.*;
 import edu.berkeley.gamesman.util.DebugFacility;
 import edu.berkeley.gamesman.util.Util;
 
 /**
  * The TieredHadoopTool is the code that runs on the master node. It loops over
  * all tiers, and for each tier, it sets "tier" in the JobConf. Then, it uses
  * SequenceInputFormat to subdivide the hash space into a set of inputs for each
  * mapper.
  * 
  * @author Patrick Horn
  */
 @SuppressWarnings("deprecation")
 public class TieredHadoopTool extends Configured implements Tool {
 	edu.berkeley.gamesman.core.Configuration myConf;
 
 	public int run(String[] args) throws Exception {
 		Configuration conf = getConf();
 		assert Util.debug(DebugFacility.HADOOP,
 				"Hadoop launching with configuration " + args[0]);
 		myConf = edu.berkeley.gamesman.core.Configuration.load(Util
 				.decodeBase64(args[0]));
 
 		conf.set("configuration_data", args[0]);
 		conf.setStrings("args", args);
 
 		// Determine last index
 		TieredGame<?> g = Util.checkedCast(myConf.getGame());
 
 		int primitiveTier = g.numberOfTiers() - 1;
 		int tier = primitiveTier;
 		Util.debug(DebugFacility.HADOOP, "Processing first tier " + tier);
 		processRun(conf, tier, -1);
 		for (tier--; tier >= 0; tier--) {
 			processRun(conf, tier, tier+1);
 		}
 		
 		FileSystem fs = FileSystem.get(conf);
 		SplitDatabaseWritableList allDatabases = new SplitDatabaseWritableList();
 		myConf.setProperty("gamesman.database", "SplitSolidDatabase");
 		allDatabases.setConf(myConf);
 		for (tier = 0; tier <= primitiveTier; tier++) {
 			SplitDatabaseWritableList thisTier = new SplitDatabaseWritableList();
 			FSDataInputStream di = fs.open(HadoopUtil.getTierIndexPath(conf, myConf, tier));
 			thisTier.readFields(di);
 			di.close();
 			for (SplitDatabaseWritable w : thisTier) {
 				w.setFilename(HadoopUtil.getTierDirectoryName(tier)+
 						"/"+w.getFilename());
 				allDatabases.add(w);
 			}
 		}
 		FSDataOutputStream dout = fs.create(new Path(HadoopUtil.getParentPath(conf, myConf), "index.db"));
 		allDatabases.write(dout);
 		dout.close();
 
 		return 0;
 	}
 
 	private void processRun(Configuration conf, int tier, int lastTier) throws IOException {
 		TieredHasher<?> h = Util.checkedCast(myConf.getHasher());
 		long firstHash = h.hashOffsetForTier(tier);
 		long endHash = h.hashOffsetForTier(tier+1);
 
 		JobConf job = new JobConf(conf, TierMap.class);
 
 		job.setMapOutputKeyClass(IntWritable.class);
 		job.setMapOutputValueClass(HadoopSplitDatabaseWritable.class);
 		job.setOutputKeyClass(IntWritable.class);
 		job.setOutputValueClass(HadoopSplitDatabaseWritableList.class);
 
 		Util.debug(DebugFacility.HADOOP, "Processing tier " + tier+" from "+firstHash+" to "+endHash);
 
 		int numMappers = myConf.getInteger("gamesman.hadoop.numMappers", 60);
 		int minSplit = myConf.getInteger("gamesman.hadoop.minSplit",
 			myConf.getInteger("gamesman.minSplit", myConf.recordsPerGroup));
 		if (numMappers < 1) {
 			numMappers = 1;
 		}
 		if (minSplit > 0) {
			if (((int)(endHash - firstHash))/numMappers < minSplit) {
				numMappers = ((int)(endHash - firstHash))/minSplit;
 				if (numMappers < 1) {
 					numMappers = 1;
 				}
 			}
 		}
 		job.set("first", Long.toString(firstHash));
 		job.set("end", Long.toString(endHash));
 		job.set("numMappersHack", Integer.toString(numMappers));
 		job.set("tier", Integer.toString(tier));
 		job.set("recordsPerGroup", Integer.toString(myConf.recordsPerGroup));
 		if (lastTier >= 0) {
 			job.set("previousTierDb", HadoopUtil.getTierIndexPath(conf, myConf, lastTier).toString());
 		}
 
 		Path outputPath = HadoopUtil.getTierPath(conf, myConf, tier);
 		FileOutputFormat.setOutputPath(job, outputPath);
 		FileSystem.get(job).mkdirs(outputPath);
 
 		job.setJobName("Tier Map-Reduce");
 		FileInputFormat.setInputPaths(job, new Path("in"));
 		job.setInputFormat(SequenceInputFormat.class);
 		job.setOutputFormat(SplitDatabaseOutputFormat.class);
 		job.setMapperClass(TierMap.class);
 		job.setNumMapTasks(numMappers);
 		job.setNumReduceTasks(1);
 		job.setReducerClass(SplitDatabaseReduce.class);
 
 		JobClient.runJob(job);
 		return;
 	}
 
 }
