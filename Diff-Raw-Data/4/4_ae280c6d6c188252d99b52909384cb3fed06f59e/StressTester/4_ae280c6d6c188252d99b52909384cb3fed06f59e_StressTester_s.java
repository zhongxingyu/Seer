 package com.ingemark.requestage;
 
 import static ch.qos.logback.classic.Level.INFO;
 import static com.ingemark.requestage.Message.ERROR;
 import static com.ingemark.requestage.Message.EXCEPTION;
 import static com.ingemark.requestage.Message.INITED;
 import static com.ingemark.requestage.Message.INTENSITY;
 import static com.ingemark.requestage.Message.SHUTDOWN;
 import static com.ingemark.requestage.Message.STATS;
 import static com.ingemark.requestage.StressTestServer.NETTY_PORT;
 import static com.ingemark.requestage.Util.excToString;
 import static com.ingemark.requestage.Util.join;
 import static com.ingemark.requestage.Util.nettySend;
 import static com.ingemark.requestage.Util.sneakyThrow;
 import static com.ingemark.requestage.plugin.RequestAgePlugin.requestAgePlugin;
 import static com.ingemark.requestage.script.JsScope.JS_LOGGER_NAME;
 import static java.lang.Runtime.getRuntime;
 import static java.util.Arrays.asList;
 import static java.util.concurrent.Executors.newCachedThreadPool;
 import static java.util.concurrent.Executors.newScheduledThreadPool;
 import static java.util.concurrent.TimeUnit.MICROSECONDS;
 import static java.util.concurrent.TimeUnit.MILLISECONDS;
 import static java.util.concurrent.TimeUnit.NANOSECONDS;
 import static java.util.concurrent.TimeUnit.SECONDS;
 import static org.eclipse.core.runtime.FileLocator.getBundleFile;
 import static org.eclipse.jdt.launching.JavaRuntime.getDefaultVMInstall;
 import static org.jboss.netty.channel.Channels.pipeline;
 import static org.jboss.netty.channel.Channels.pipelineFactory;
 import static org.jboss.netty.handler.codec.serialization.ClassResolvers.softCachingResolver;
 import static org.slf4j.LoggerFactory.getLogger;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.ThreadFactory;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.atomic.AtomicInteger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.eclipse.jdt.launching.IVMInstall;
 import org.jboss.netty.bootstrap.ClientBootstrap;
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.channel.ExceptionEvent;
 import org.jboss.netty.channel.MessageEvent;
 import org.jboss.netty.channel.SimpleChannelHandler;
 import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
 import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
 import org.jboss.netty.handler.codec.serialization.ObjectEncoder;
 import org.mozilla.javascript.ContextFactory;
 import org.slf4j.Logger;
 
 import com.ingemark.requestage.JsHttp.InitParams;
 import com.ingemark.requestage.script.JsScope;
 import com.ning.http.client.AsyncHttpClient;
 import com.ning.http.client.AsyncHttpClientConfig;
 
 public class StressTester implements Runnable
 {
   static class MyThreadFac implements ThreadFactory {
     private final String template;
     MyThreadFac(String name) { this.template = "StressTester " + name + " #"; }
     final AtomicInteger i = new AtomicInteger();
     public Thread newThread(Runnable r) {
       return new Thread(r, template + i.getAndIncrement());
    }
   }
   static final Logger log = getLogger(StressTester.class);
   public static final int TIMESLOTS_PER_SEC = 20, HIST_SIZE = 200;
   static final ContextFactory fac = ContextFactory.getGlobal();
   final ScheduledExecutorService sched;
   final ExecutorService pool; {
     final int cpus = getRuntime().availableProcessors();
     sched = newScheduledThreadPool(cpus, new MyThreadFac("scheduler"));
     pool = new ThreadPoolExecutor(cpus, 4*cpus,
       10, SECONDS, new ArrayBlockingQueue<Runnable>(20*cpus, false),
       new MyThreadFac("pool"), new ThreadPoolExecutor.DiscardPolicy());
   }
   final AsyncHttpClient client;
   boolean explicitLsMap;
   final Map<String, LiveStats> lsmap = new HashMap<String, LiveStats>();
   final JsScope jsScope;
   private final ClientBootstrap netty;
   final Channel channel;
   private volatile int intensity = 0;
   final AtomicInteger scriptsRunning = new AtomicInteger();
   private ScheduledFuture<?> testTask;
 
   public StressTester(final String fname) throws Throwable {
     this.netty = netty();
     log.debug("Connecting to server within Eclipse");
     this.channel = channel(netty);
     log.debug("Connected");
     try {
       this.jsScope = new JsScope(this, fname);
       final AsyncHttpClientConfig.Builder b = new AsyncHttpClientConfig.Builder();
       b.setIdleConnectionInPoolTimeoutInMs((int)SECONDS.toMillis(2));
       b.setMaxRequestRetry(0);
       b.setRequestTimeoutInMs((int)SECONDS.toMillis(20));
       jsScope.call("conf", jsScope.jsHttp.configBuilder(b));
       this.client = new AsyncHttpClient(b.build());
     } catch (Throwable t) {
       nettySend(channel, new Message(ERROR, excToString(t)));
       shutdown();
       throw t;
     }
   }
 
   Channel channel(ClientBootstrap netty) {
     try {
       return netty.connect(new InetSocketAddress("localhost", NETTY_PORT)).await().getChannel();
     } catch (InterruptedException e) {return null;}
   }
 
   ClientBootstrap netty() {
     log.debug("Starting Client Netty");
     final ClientBootstrap b = new ClientBootstrap(
         new NioClientSocketChannelFactory(newCachedThreadPool(),newCachedThreadPool()));
     b.setPipelineFactory(pipelineFactory(pipeline(
       new ObjectDecoder(softCachingResolver(getClass().getClassLoader())),
       new SimpleChannelHandler() {
         @Override public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
           try {
             final Message msg = (Message)e.getMessage();
             switch (msg.type) {
             case INTENSITY:
               scheduleTest((Integer) msg.value);
               break;
             case EXCEPTION:
               nettySend(channel, new Message(EXCEPTION, new DialogInfo(lsmap.get(msg.value))));
               break;
             case SHUTDOWN:
               asyncShutdown();
               break;
             }
           } catch (Throwable t) {t.printStackTrace();}
         }
         @Override
         public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
           log.error("Netty error", e.getCause());
         }
       }
       , new ObjectEncoder()
       )));
     return b;
   }
 
   void runTest() throws Exception {
     try {
       log.debug("Initializing test");
       jsScope.call("init");
       log.debug("Initialized");
       final InitParams ps = jsScope.initDone();
       nettySend(channel, new Message(INITED, new InitInfo(lsmap, ps)), true);
       raiseLogLevel("com.ning", JS_LOGGER_NAME);
       scheduleTest(1);
       sched.scheduleAtFixedRate(new Runnable() { public void run() {
         nettySend(channel, new Message(STATS, new StatsHolder(stats(), scriptsRunning.get())));
       }}, MILLISECONDS.toMicros(100), SECONDS.toMicros(1)/TIMESLOTS_PER_SEC, MICROSECONDS);
     }
     catch (Throwable t) {
       log.error("Error while initializing", t);
       nettySend(channel, new Message(ERROR, excToString(t)));
       shutdown();
     }
   }
   static void raiseLogLevel(String... loggerNames) {
 	  for (String loggerName : loggerNames)
 		  ((ch.qos.logback.classic.Logger)getLogger(loggerName)).setLevel(INFO);
   }
 
   synchronized void scheduleTest(int newIntensity) {
     if (intensity == newIntensity) return;
     intensity = newIntensity;
     if (testTask != null) testTask.cancel(true);
     testTask = intensity > 0?
       sched.scheduleAtFixedRate(this, 0, SECONDS.toNanos(1)/intensity, NANOSECONDS)
       : null;
   }
 
   @Override public void run() {
     try {
       pool.execute(new Runnable() { public void run() {
         final AtomicInteger count = new AtomicInteger(1);
         JsHttp.PENDING_EXECUTIONS.set(count);
         try {
           scriptsRunning.incrementAndGet();
           jsScope.call("test");
         }
         catch (Throwable t) {
           log.error("Error in thread pool", t);
           nettySend(channel, new Message(ERROR, excToString(t)));
           asyncShutdown();
         }
         finally {
           if (count.decrementAndGet() == 0) scriptsRunning.decrementAndGet();
           JsHttp.PENDING_EXECUTIONS.set(null);
         }
       }});
     } catch (Throwable t) {
       log.error("Error in scheduler", t);
       nettySend(channel, new Message(ERROR, excToString(t)));
       asyncShutdown();
     }
   }
 
   Stats[] stats() {
     final Stats[] ret = new Stats[lsmap.size()];
     for (LiveStats ls : lsmap.values()) {
       final Stats stats = ls.stats();
       ret[stats.index] = stats;
     }
     return ret;
   }
 
   void shutdown() {
     log.info("Shutting down");
     sched.shutdown();
     pool.shutdown();
     if (client != null) client.close();
     log.debug("HTTP Client shut down");
     netty.shutdown();
     netty.releaseExternalResources();
     log.debug("Netty client shut down");
   }
 
   void asyncShutdown() {
     sched.submit(new Runnable() { public void run() {shutdown();} });
   }
 
   private static final String[]
     javaCandidates = {"javaw", "javaw.exe", "java", "java.exe", "j9w", "j9w.exe", "j9", "j9.exe"},
     javaDirCandidates = {"bin", "jre" + File.separator + "bin"};
 
   private static String java() {
     final IVMInstall inst = getDefaultVMInstall();
     if (inst == null) return "java";
     final File loc = inst.getInstallLocation();
     if (loc == null) return "java";
     for (String java : javaCandidates) {
       for (String javaDir : javaDirCandidates) {
         final File javaFile = new File(loc, javaDir + File.separator + java);
         if (javaFile.isFile()) return javaFile.getAbsolutePath();
       }
     }
     return "java";
   }
 
   public static Process launchTester(String scriptFname) {
     try {
       final File scriptFile = new File(scriptFname);
       final List<String> command = new ArrayList<String>();
       command.add(java());
       final BufferedReader r = new BufferedReader(new FileReader(scriptFile));
       final String firstLine = r.readLine();
       r.close();
       final Matcher m = Pattern.compile("\\s*//\\s*jvm\\s+(.+)").matcher(firstLine);
       if (m.matches()) command.addAll(asList(m.group(1).split("\\s+")));
 
       final String bpath = getBundleFile(requestAgePlugin().bundle()).getAbsolutePath();
       final String slash = File.separator;
       final String cp = join(File.pathSeparator, bpath, bpath+slash+"bin", bpath+slash+"lib");
       command.addAll(asList("-cp", cp, StressTester.class.getName(), scriptFname));
       log.debug("Launching {}", command);
       return new ProcessBuilder(command).directory(scriptFile.getParentFile()).start();
     } catch (IOException e) { return sneakyThrow(e); }
   }
 
   public static void main(String[] args) {
     try {
       log.info("Loading script {}", args[0]);
       Runtime.getRuntime().addShutdownHook(new Thread() {
         public void run() { System.out.println("Stress Tester shut down");
       }});
       new StressTester(args[0]).runTest();
     } catch (Throwable t) { log.error("Top-level error", t); }
   }
 }
