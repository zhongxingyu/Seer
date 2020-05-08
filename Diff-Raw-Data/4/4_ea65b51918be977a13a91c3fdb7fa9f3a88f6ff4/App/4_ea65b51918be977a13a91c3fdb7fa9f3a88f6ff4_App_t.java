 package com.agroknow.indexer;
 
 import com.agroknow.domain.parser.factory.SimpleMetadataParserFactory;
 import java.io.File;
 import java.lang.management.ManagementFactory;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.DefaultParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.Options;
 import org.apache.commons.io.FileUtils;
 import static org.apache.commons.lang3.StringUtils.substringAfter;
 import static org.apache.commons.lang3.StringUtils.substringBefore;
 import org.apache.commons.lang3.concurrent.BasicThreadFactory;
 import org.elasticsearch.client.Client;
 import org.elasticsearch.client.transport.TransportClient;
 import static org.elasticsearch.common.settings.ImmutableSettings.settingsBuilder;
 import org.elasticsearch.common.transport.InetSocketTransportAddress;
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.util.Assert;
 
 /**
  * agro-indexer
  *
  */
 public class App {
 
     private static final Logger LOG = LoggerFactory.getLogger(App.class);
     private static final Options OPTS = new Options();
 
     /**
      * @param args
      * @throws InterruptedException
      */
     public static void main(String[] args) throws Exception {
         IndexerOptions options = parseOptions(args);
         LOG.info("Starting the agro-indexer with the given options {}", options);
 
         // get the charset from charset (string) option
         Charset charset = Charset.forName(options.charset);
 
         // operations over runtime dir (write pid, read last-check timestamp)
         FileUtils.writeStringToFile(FileUtils.getFile(options.runtimeDirectory, "pid"), String.valueOf(getPid()), charset);
         File lastCheckFile = FileUtils.getFile(options.runtimeDirectory, "last-check");
         long lastCheck = -1;
         if( lastCheckFile.exists() ) {
              lastCheck = Long.valueOf(FileUtils.readFileToString(lastCheckFile, charset));
         }
 
         // the time to be saved after indexing is complete
         // we save the time before start indexing in order to be inclusive
         // but we write the last-check file at the end
         long currentCheckTime = new DateTime().withZone(DateTimeZone.UTC).getMillis();
 
         // find all files under root-directory
         // TODO: change this to something scalable
         File rootDirectory = FileUtils.getFile(options.rootDirectory);
         ArrayList<File> files = new ArrayList(FileUtils.listFiles(rootDirectory, new String[] { "json" }, true));
         int filesSize = files.size();
         //TODO add metrics for files to process
         LOG.info("Found {} files to process.", filesSize);
 
         // create the elasticsearch Client and connect it to the cluster
         Client esClient = getElasticSearchClient(options.esClusterName, options.esClusterNodes.split(","));
 
         // create the threadpool and submit job for processing
         // options.bulkSize number of files
         ExecutorService threadPool = Executors.newFixedThreadPool(4, new BasicThreadFactory.Builder().namingPattern("bulkindexworker-%d").build());
         int step = options.bulkSize;
         for(int i=0; i<filesSize; i+=step) {
             threadPool.submit(new BulkIndexWorker(files.subList(i, Math.min(i+step, filesSize)), options.fileFormat, charset, lastCheck, esClient));
         }
 
         // close everything and go to sleep :)
         // INFO: shutdown and then awaitTermination is a common pattern for waiting
         // a threadpool to finish
         threadPool.shutdown();
         try {
             threadPool.awaitTermination(1L, TimeUnit.DAYS);
         } catch(InterruptedException ex) {
             LOG.error(ex.getMessage(), (LOG.isDebugEnabled() ? ex : null));
             System.exit(1);
         }
 
         // save last-check time
         FileUtils.writeStringToFile(FileUtils.getFile(options.runtimeDirectory, "last-check"), String.valueOf(currentCheckTime), charset);
 
         //TODO print metrics for succeeded/failed index requests
         LOG.info("All {} files were processed.", filesSize);
     }
 
     public static void printHelp() {
         new HelpFormatter().printHelp("agro-indexer", getOptions(), true);
     }
 
     private static int getPid() {
         return Integer.parseInt(ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);
     }
 
     private static Options getOptions() {
         if (OPTS.getOptions().size() < 1) {
             //Option.builder("o").longOpt("long-opt").hasArg(true).required(false).desc("opt description").build() -> Option
             OPTS.addOption(Option.builder("h").longOpt("help").hasArg(false).required(false).desc("print this message").build());
             OPTS.addOption(Option.builder("t").longOpt("file-format").hasArg(true).required(false).desc("the file formats agro-indexer is going to check\n[default akif]").build());
             OPTS.addOption(Option.builder("d").longOpt("root-directory").hasArg(true).required(false).desc("the folder where the files are located\n[default /opt/agroknow/data/akif]").build());
             OPTS.addOption(Option.builder("r").longOpt("runtime-directory").hasArg(true).required(false).desc("the folder where the pid and last-check files are located\n[default /opt/agroknow/run]").build());
             OPTS.addOption(Option.builder("C").longOpt("charset").hasArg(true).required(false).desc("the charset which files are written with\n[default UTF-8]").build());
             OPTS.addOption(Option.builder("b").longOpt("bulk-size").hasArg(true).required(false).desc("the bulk indexing size\n[default 1000]").build());
            OPTS.addOption(Option.builder().longOpt("es-name").hasArg(true).required(false).desc("the elasticsearch cluster name [default agroknow]").build());
            OPTS.addOption(Option.builder().longOpt("es-nodes").hasArg(true).required(false).desc("the elasticsearch cluster nodes [default localhost:9300]").build());
         }
         return OPTS;
     }
 
     private static IndexerOptions parseOptions(String[] args) {
         IndexerOptions indexerOptions = new IndexerOptions();
         CommandLineParser parser = new DefaultParser();
         Options options = getOptions();
 
         try {
             // parse the command line arguments
             CommandLine cli = parser.parse(options, args);
 
             // if --help, print help and exit
             if(cli.hasOption("help")) {
                 printHelp();
                 System.exit(0);
             }
 
             // set indexerOptions argument or default values
             indexerOptions.fileFormat = cli.getOptionValue(options.getOption("file-format").getOpt(), SimpleMetadataParserFactory.AKIF);
             indexerOptions.rootDirectory = cli.getOptionValue(options.getOption("root-directory").getOpt(), "/opt/agroknow/data/akif");
             indexerOptions.runtimeDirectory = cli.getOptionValue(options.getOption("runtime-directory").getOpt(), "/opt/agroknow/run");
             indexerOptions.charset = cli.getOptionValue(options.getOption("charset").getOpt(), "UTF-8");
             indexerOptions.bulkSize = Integer.valueOf(cli.getOptionValue(options.getOption("bulk-size").getOpt(), "1000"));
             indexerOptions.esClusterName = cli.getOptionValue(options.getOption("es-name").getOpt(), "agroknow");
             indexerOptions.esClusterNodes = cli.getOptionValue(options.getOption("es-nodes").getOpt(), "localhost:9300");
 
             // validate indexerOptions
             indexerOptions.validate();
         } catch (Exception ex) {
             LOG.error(ex.getMessage(), (LOG.isDebugEnabled() ? ex : null));
             printHelp();
             System.exit(1);
         }
 
         return indexerOptions;
     }
 
     private static Client getElasticSearchClient(String clusterName, String[] clusterNodes) {
         TransportClient client = new TransportClient(settingsBuilder()
                 .put("cluster.name", clusterName)
                 .put("client.transport.ping_timeout", "15s")
 //                //TODO maybe add more advanced settings
 //                .put("client.transport.sniff", clientTransportSniff)
 //                .put("client.transport.ignore_cluster_name", clientIgnoreClusterName)
 //                .put("client.transport.nodes_sampler_interval", clientNodesSamplerInterval)
                 .build());
 
         Assert.notEmpty(clusterNodes, "[Assertion failed] clusterNodes setting not set");
         for (String clusterNode : clusterNodes) {
             String hostName = substringBefore(clusterNode, ":");
             String port = substringAfter(clusterNode, ":");
             Assert.hasText(hostName, "[Assertion failed] missing host name in 'clusterNodes'");
             Assert.hasText(port, "[Assertion failed] missing port in 'clusterNodes'");
             LOG.info("adding transport node : " + clusterNode);
             client.addTransportAddress(new InetSocketTransportAddress(hostName, Integer.valueOf(port)));
         }
         client.connectedNodes();
         return client;
     }
 }
