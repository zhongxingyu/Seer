 package com.edge.twitter_research.relevance_filter;
 
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.conf.Configured;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.hbase.HBaseConfiguration;
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 
 import org.kiji.mapreduce.MapReduceJob;
 
 import org.kiji.mapreduce.gather.KijiGatherJobBuilder;
 
 import org.kiji.mapreduce.output.TextMapReduceJobOutput;
 
 import org.kiji.schema.KijiURI;
 
 import java.io.IOException;
 
 
 public class TopEmoticonsCalculator extends Configured{
 
 
     public MapReduceJob mapReduceJob = null;
 
     public static Logger logger =
             Logger.getLogger(TopEmoticonsCalculator.class);
 
     public static int pageSize;
     public static int maxVersions;
 
 
     public TopEmoticonsCalculator (String outputFilePath,
                                     String log4jPropertiesFilePath){
 
         PropertyConfigurator.configure(log4jPropertiesFilePath);
 
         try{
 
             setConf(HBaseConfiguration.addHbaseResources(new Configuration(true)));
 
             KijiURI tableUri =
                     KijiURI.newBuilder(String.format("kiji://.env/default/%s", Constants.EMOTICON_STORE_TABLE_NAME)).build();
 
             this.mapReduceJob = KijiGatherJobBuilder.create()
                     .withConf(getConf())
                     .withGatherer(TopEmoticonsGatherer.class)
                     .withReducer(TopEmoticonsReducer.class)
                     .withInputTable(tableUri)
                     .withOutput(new TextMapReduceJobOutput(new Path(outputFilePath), 1))
                     .build();
         }catch (IOException ioException){
             logger.error("IO Exception while configuring MapReduce Job", ioException);
             System.exit(1);
         } catch (Exception unknownException){
             logger.error("Unknown Exception while configuring MapReduce Job", unknownException);
             System.exit(1);
         }
     }
 
 
     public static void main(String[] args){
 
         if (args.length < 4){
             System.out.println("Usage: TopEmoticonsCalculator " +
                     "<relevance_filter_root> " +
                     "<HDFS_output_file_path> " +
                     "<paging value> " +
                     "<max_num_versions>");
             return;
         }
 
        pageSize = Integer.parseInt(args[2]);
        maxVersions = Integer.parseInt(args[3]);

         TopEmoticonsCalculator topEmoticonsCalculator =
                 new TopEmoticonsCalculator(args[1],
                         args[0] + "/" + Constants.LOG4J_PROPERTIES_FILE_PATH);
 
         boolean isSuccessful = false;
         if (topEmoticonsCalculator.mapReduceJob != null){
             try{
                 isSuccessful = topEmoticonsCalculator.mapReduceJob.run();
             }catch (Exception unknownException){
                 logger.error("Unknown Exception while running MapReduce Job", unknownException);
                 System.exit(1);
             }
         }
 
         String result = isSuccessful ? "Successful" : "Failure";
         logger.info(result);
     }
 }
