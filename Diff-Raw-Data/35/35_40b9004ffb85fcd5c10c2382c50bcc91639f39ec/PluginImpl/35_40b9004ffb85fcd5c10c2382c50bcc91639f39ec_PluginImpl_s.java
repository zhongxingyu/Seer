 package hudson.plugins.hadoop;
 
 import hudson.FilePath;
 import hudson.Plugin;
 import hudson.Proc;
 import hudson.Launcher.LocalLauncher;
 import static hudson.FilePath.TarCompression.GZIP;
 import hudson.model.Computer;
 import hudson.model.Hudson;
 import hudson.model.TaskListener;
 import hudson.remoting.Channel;
 import hudson.remoting.Which;
 import hudson.slaves.Channels;
 import hudson.util.ArgumentListBuilder;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.URL;
 
 /**
  * @author Kohsuke Kawaguchi
  */
 public class PluginImpl extends Plugin {
     /*package*/ Channel channel;
 
     /**
      * Determines the HDFS URL.
      */
     public String getHdfsUrl() throws MalformedURLException {
         // TODO: port should be configurable
         String rootUrl = Hudson.getInstance().getRootUrl();
         if(rootUrl==null)
             return null;
         URL url = new URL(rootUrl);
         return "hdfs://"+url.getHost()+":9000/";
     }
 
     /**
      * Determines the job tracker address.
      */
     public String getJobTrackerAddress() throws MalformedURLException {
         // TODO: port should be configurable
         String rootUrl = Hudson.getInstance().getRootUrl();
         if(rootUrl==null)
             return null;
         URL url = new URL(rootUrl);
         return url.getHost()+":"+JOB_TRACKER_PORT_NUMBER;
     }
 
     /**
      * Launches Hadoop in a separate JVM.
      *
      * @param rootDir
      *      The slave/master root.
      */
     static /*package*/ Channel createHadoopVM(File rootDir, TaskListener listener) throws IOException, InterruptedException {
         // install Hadoop if it's not there
         rootDir = new File(rootDir,"hadoop");
         File distDir = new File(rootDir,"dist");
         File logDir = new File(rootDir,"logs");
        // TODO: if the right bit is already there, don't expand
        listener.getLogger().println("Installing Hadoop binaries");
        new FilePath(distDir).untarFrom(PluginImpl.class.getResourceAsStream("hadoop.tar.gz"),GZIP);
         logDir.mkdirs();
 
         // launch Hadoop in a new JVM and have them connect back to us
         ServerSocket serverSocket = new ServerSocket();
         serverSocket.bind(null);
         serverSocket.setSoTimeout(10*1000);
 
         ArgumentListBuilder args = new ArgumentListBuilder();
         args.add(new File(System.getProperty("java.home"),"bin/java"));
         args.add("-Dhadoop.log.dir="+logDir); // without this job tracker dies with NPE
         args.add("-jar");
         args.add(Which.jarFile(Channel.class));
 
         // build up a classpath
         StringBuilder classpath = new StringBuilder();
         for( String mask : new String[]{"hadoop-*-core.jar","lib/**/*.jar"}) {
             for(FilePath jar : new FilePath(distDir).list(mask)) {
                 if(classpath.length()>0)    classpath.append(File.pathSeparatorChar);
                 classpath.append(jar.getRemote());
             }
         }
         args.add("-cp").add(classpath);
 
         args.add("-connectTo","localhost:"+serverSocket.getLocalPort());
 
         Proc p = new LocalLauncher(listener).launch(args.toCommandArray(), new String[0], listener.getLogger(), null);
 
         Socket s = serverSocket.accept();
         serverSocket.close();
 
         return Channels.forProcess("Channel to Hadoop", Computer.threadPoolForRemoting,
                 new BufferedInputStream(s.getInputStream()), new BufferedOutputStream(s.getOutputStream()), p);
     }
 
     @Override
     public void stop() throws Exception {
         if(channel!=null)
             channel.close();
     }
 
     public static PluginImpl get() {
         return Hudson.getInstance().getPlugin(PluginImpl.class);
     }
 
     /**
      * Job tracker port number.
      */
     public static final int JOB_TRACKER_PORT_NUMBER = 50040;
 }
