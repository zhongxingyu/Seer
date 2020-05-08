 package us.yuxin.hump;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.util.concurrent.BlockingQueue;
 
 import com.hazelcast.config.Config;
 import com.hazelcast.config.GroupConfig;
 import com.hazelcast.core.Hazelcast;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.conf.Configured;
 import org.apache.hadoop.filecache.DistributedCache;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.NullWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;
 import org.apache.hadoop.util.Tool;
 import org.apache.hadoop.util.ToolRunner;
 
 // TODO Hazelcast default configuration file.
 
 public class Hump extends Configured implements Tool {
   public static final String HUMP_HAZELCAST_GROUP = "hump";
   public static final String HUMP_HAZELCAST_PASSWORD = "humpps";
   public static final String HUMP_HAZELCAST_TASK_QUEUE = "hump.task.queue";
   public static final String HUMP_HAZELCAST_FEEDBACK_QUEUE = "hump.feedback.queue";
 
   public static final int HUMP_TASK_PARALLEL = 20;
 
   public static final String CONF_HUMP_HAZELCAST_ENDPOINT = "hump.hazelcast.endpoint";
   public static final String CONF_HUMP_HAZELCAST_GROUP = "hump.hazelcast.group";
   public static final String CONF_HUMP_HAZELCAST_PASSWORD = "hump.hazelcast.password";
   public static final String CONF_HUMP_TASKS = "hump.tasks";
 
   BlockingQueue<String> taskQueue;
   BlockingQueue<String> feedbackQueue;
 
   public int run(String[] args) throws Exception {
     gridInit();
     runJob();
     gridShutdown();
     return 0;
   }
 
 
   private void gridInit() {
     Configuration conf = getConf();
    conf.setIfUnset(CONF_HUMP_HAZELCAST_GROUP, HUMP_HAZELCAST_GROUP);
    conf.setIfUnset(CONF_HUMP_HAZELCAST_PASSWORD, HUMP_HAZELCAST_PASSWORD);

     Config cfg = new Config();
 
     cfg.setGroupConfig(new GroupConfig(
       conf.get(CONF_HUMP_HAZELCAST_GROUP),
       conf.get(CONF_HUMP_HAZELCAST_PASSWORD)));
     cfg.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
     Hazelcast.init(cfg);
 
     InetSocketAddress addr = Hazelcast.getCluster().getLocalMember().getInetSocketAddress();
     conf.set(CONF_HUMP_HAZELCAST_ENDPOINT, addr.getAddress().toString() + ":" + addr.getPort());
 
     taskQueue = Hazelcast.getQueue(HUMP_HAZELCAST_TASK_QUEUE);
     feedbackQueue = Hazelcast.getQueue(HUMP_HAZELCAST_FEEDBACK_QUEUE);
   }
 
 
   private void gridShutdown() {
     Hazelcast.shutdownAll();
   }
 
 
   private void runJob() throws IOException, ClassNotFoundException, InterruptedException {
     Configuration conf = getConf();
 
     DistributedCache.addArchiveToClassPath(new Path("/is/app/hump/lib/hazelcast-2.0.3.jar"), conf);
     DistributedCache.addArchiveToClassPath(new Path("/is/app/hump/lib/mysql-connector-java-5.1.19.jar"), conf);
     DistributedCache.addArchiveToClassPath(new Path("/is/app/hump/lib/hazelcast-client-2.0.3.jar"), conf);
 
     for (int i = 0; i < HUMP_TASK_PARALLEL * 5; ++i ) {
       taskQueue.put(Integer.toString(i));
     }
 
     for (int i = 0; i < HUMP_TASK_PARALLEL; ++i) {
       taskQueue.put("STOP");
     }
 
     conf.setInt(CONF_HUMP_TASKS, HUMP_TASK_PARALLEL);

 
     Job job = new Job(conf);
 
     job.setJobName("Hump-Sample");
     job.setJarByClass(HumpMapper.class);
     job.setMapperClass(HumpMapper.class);
 
     job.setMapOutputKeyClass(Text.class);
     job.setMapOutputValueClass(NullWritable.class);
     job.setOutputFormatClass(NullOutputFormat.class);
 
     job.setInputFormatClass(HumpInputFormat.class);
     FileInputFormat.addInputPath(job, new Path("ignored"));
 
     job.setNumReduceTasks(0);
     job.waitForCompletion(true);
   }
 
   public static void main(String[] args) throws Exception {
     int res = ToolRunner.run(new Configuration(), new Hump(), args);
     System.exit(res);
   }
 }
