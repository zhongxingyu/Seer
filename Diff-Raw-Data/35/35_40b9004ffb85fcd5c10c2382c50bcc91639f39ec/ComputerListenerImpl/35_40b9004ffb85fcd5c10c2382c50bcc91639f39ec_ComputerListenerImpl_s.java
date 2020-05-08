 package hudson.plugins.hadoop;
 
 import hudson.Extension;
 import hudson.FilePath;
 import hudson.Launcher.LocalLauncher;
 import hudson.model.Computer;
 import hudson.model.Node;
 import hudson.remoting.Callable;
 import hudson.remoting.Channel;
 import hudson.slaves.ComputerListener;
 import hudson.util.StreamTaskListener;
 import hudson.util.IOException2;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.hdfs.server.datanode.DataNode;
 import org.apache.hadoop.mapred.JobConf;
 import org.apache.hadoop.mapred.TaskTracker;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 
 /**
  * When a new computer becomes online, starts a Hadoop data node and task tracker.
  *
  * <p>
  * This will be done on a separate JVM to allow administrators to control the JVM parameters better.
  * This JVM automatically kills itself when the slave JVM gets disconnected.
  *
  * @author Kohsuke Kawaguchi
  */
 @Extension
 public class ComputerListenerImpl extends ComputerListener {
     @Override
    public void onOnline(Computer c) {
         try {
            // TODO: shouldn't ComputerListener gets TaskListener?
             // TODO: allow slave.host.name to be configured
            StreamTaskListener listener = new StreamTaskListener(System.out);
             PluginImpl p = PluginImpl.get();
             String hdfsUrl = p.getHdfsUrl();
             if(hdfsUrl !=null)
                 c.getChannel().call(new NodeStarter(c.getNode(), listener, hdfsUrl, p));
         } catch (IOException e) {
            e.printStackTrace();
         } catch (InterruptedException e) {
            e.printStackTrace();
         }
     }
 
     /**
      * Runs in the slave JVM to start Hadoop JVM.
      *
      * <p>
      * Doing this from the slave JVM and not from the master JVM
      * simplifies establishing the connection with this JVM.
      */
     private static class NodeStarter implements Callable<Void,IOException> {
        private final StreamTaskListener listener;
         private final String hdfsUrl;
         private final String jobTrackerAddress;
         private final FilePath rootPath;
 
        public NodeStarter(Node n, StreamTaskListener listener, String hdfsUrl, PluginImpl p) throws MalformedURLException {
             this.listener = listener;
             this.hdfsUrl = hdfsUrl;
             this.jobTrackerAddress = p.getJobTrackerAddress();
             this.rootPath = n.getRootPath();
         }
 
         @Override
         public Void call() throws IOException {
             try {
                 Channel channel = PluginImpl.createHadoopVM(new File(rootPath.getRemote()), listener);
                 channel.call(new DataNodeStartTask(hdfsUrl, rootPath.getRemote()));
                 channel.call(new TaskTrackerStartTask(hdfsUrl, jobTrackerAddress, rootPath.getRemote()));
                 return null;
             } catch (InterruptedException e) {
                 throw new IOException2(e);
             }
         }
 
         private static final long serialVersionUID = 1L;
     }
 
     /**
      * Starts a {@link DataNode}.
      */
     private static class DataNodeStartTask implements Callable<Void,IOException> {
         private final String hdfsUrl;
         private final String rootPath;
 
         private DataNodeStartTask(String hdfsUrl, String rootPath) {
             this.hdfsUrl = hdfsUrl;
             this.rootPath = rootPath;
         }
 
         public Void call() throws IOException {
             System.out.println("Starting data node");
 
             Configuration conf = new Configuration();
             conf.set("fs.default.name",hdfsUrl);
             conf.set("dfs.data.dir",new File(new File(rootPath),"hadoop/datanode").getAbsolutePath());
             conf.set("dfs.datanode.address", "0.0.0.0:0");
             conf.set("dfs.datanode.http.address", "0.0.0.0:0");
             conf.set("dfs.datanode.ipc.address", "0.0.0.0:0");
 
             // TODO: make this configurable
             // make room for builds
             conf.setLong("dfs.datanode.du.reserved",10L*1024*1024*1024);
 
             DataNode dn = DataNode.instantiateDataNode(new String[0],conf);
             DataNode.runDatanodeDaemon(dn);
             
             return null;
         }
 
         private static final long serialVersionUID = 1L;
     }
 
     /**
      * Starts a {@link TaskTracker}.
      */
     private static class TaskTrackerStartTask implements Callable<Void,IOException> {
         private final String hdfsUrl;
         private final String jobTrackerAddress;
         private final String rootPath;
 
         private TaskTrackerStartTask(String hdfsUrl, String jobTrackerAddress, String rootPath) {
             this.hdfsUrl = hdfsUrl;
             this.jobTrackerAddress = jobTrackerAddress;
             this.rootPath = rootPath;
         }
 
         public Void call() throws IOException {
             System.out.println("Starting data node");
 
             JobConf conf = new JobConf();
             conf.set("fs.default.name",hdfsUrl);
             conf.set("mapred.job.tracker",jobTrackerAddress);
             conf.set("mapred.task.tracker.http.address","0.0.0.0:0");
             conf.set("mapred.task.tracker.report.address","0.0.0.0:0");
             conf.set("mapred.local.dir",new File(new File(rootPath),"hadoop/task-tracker").getAbsolutePath());
 
             new Thread(new TaskTracker(conf)).start();
 
             return null;
         }
 
         private static final long serialVersionUID = 1L;
     }
 }
