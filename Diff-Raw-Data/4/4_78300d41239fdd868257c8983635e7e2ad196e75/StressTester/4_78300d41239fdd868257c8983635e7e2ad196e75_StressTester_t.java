 package com.ingemark.perftest;
 
 import static com.ingemark.perftest.Message.DIVISOR;
 import static com.ingemark.perftest.Message.ERROR;
 import static com.ingemark.perftest.Message.INIT;
 import static com.ingemark.perftest.Message.INTENSITY;
 import static com.ingemark.perftest.Message.SHUTDOWN;
 import static com.ingemark.perftest.Message.STATS;
 import static com.ingemark.perftest.StressTestServer.NETTY_PORT;
 import static com.ingemark.perftest.Util.join;
 import static com.ingemark.perftest.Util.nettySend;
 import static com.ingemark.perftest.plugin.StressTestActivator.stressTestPlugin;
 import static java.util.concurrent.Executors.newCachedThreadPool;
 import static java.util.concurrent.Executors.newScheduledThreadPool;
 import static java.util.concurrent.TimeUnit.MICROSECONDS;
 import static java.util.concurrent.TimeUnit.NANOSECONDS;
 import static java.util.concurrent.TimeUnit.SECONDS;
 import static org.eclipse.core.runtime.FileLocator.getBundleFile;
 import static org.eclipse.jdt.launching.JavaRuntime.getDefaultVMInstall;
 import static org.jboss.netty.channel.Channels.pipeline;
 import static org.jboss.netty.channel.Channels.pipelineFactory;
 import static org.jboss.netty.handler.codec.serialization.ClassResolvers.softCachingResolver;
 import static org.mozilla.javascript.ScriptableObject.getTypedProperty;
import static org.mozilla.javascript.ScriptableObject.putProperty;
 import static org.slf4j.LoggerFactory.getLogger;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.Reader;
 import java.net.InetSocketAddress;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.ThreadFactory;
 
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
 import org.mozilla.javascript.Callable;
 import org.mozilla.javascript.Context;
 import org.mozilla.javascript.ContextAction;
 import org.mozilla.javascript.ContextFactory;
 import org.mozilla.javascript.Function;
 import org.mozilla.javascript.ScriptableObject;
 import org.slf4j.Logger;
 
 import com.ning.http.client.AsyncHttpClient;
 import com.ning.http.client.AsyncHttpClientConfig;
 
 public class StressTester implements Runnable
 {
   static final Logger log = getLogger(StressTester.class);
   public static final int TIMESLOTS_PER_SEC = 20, HIST_SIZE = 200;
   static final ContextFactory fac = ContextFactory.getGlobal();
   final ScheduledExecutorService sched = newScheduledThreadPool(2, new ThreadFactory(){
     volatile int i; public Thread newThread(Runnable r) {
       return new Thread(r, "StressTester scheduler #"+i++);
   }});
   final AsyncHttpClient client;
   final Map<String, LiveStats> lsmap = new HashMap<>();
   final ScriptableObject jsScope;
   private final ClientBootstrap netty;
   private final Channel channel;
   private volatile int intensity = 1, updateDivisor = 1;
   private ScheduledFuture<?> testTask;
 
   public StressTester(String fname) {
     this.jsScope = initJsScope(fname);
     final AsyncHttpClientConfig.Builder b = new AsyncHttpClientConfig.Builder();
     setJsHelper(new JsInitHelper(this));
     jsCall("configure", b);
     this.client = new AsyncHttpClient(b.build());
     this.netty = netty();
     log.debug("Connecting to server");
     this.channel = channel(netty);
     log.debug("Connected");
   }
 
   ScriptableObject initJsScope(final String fname) {
     return (ScriptableObject) fac.call(new ContextAction() {
       public Object run(Context cx) {
         try {
           cx.setOptimizationLevel(9);
           final Reader js = new FileReader(fname);
           final ScriptableObject scope = cx.initStandardObjects(null, true);
           cx.evaluateString(scope,
               "RegExp; getClass; java; Packages; JavaAdapter;", "lazyLoad", 0, null);
           cx.evaluateReader(scope, js, fname, 1, null);
           return scope;
         }
         catch (RuntimeException e) { throw e; }
         catch (Exception e) { throw new RuntimeException(e); }
       }
     });
   }
   Object jsCall(final Callable fn, final Object... args) {
     return fac.call(new ContextAction() { @Override public Object run(Context cx) {
       return fn.call(cx, jsScope, null, args);
     }});
   }
   Object jsCall(String fn, Object... args) {
     return jsCall(getTypedProperty(jsScope, fn, Function.class), args);
   }
 
   LiveStats livestats(String name) {
     final LiveStats liveStats = lsmap.get(name);
     return liveStats != null? liveStats : new LiveStats(0, name) {
       @Override synchronized int registerReq() {
         return super.registerReq();
       }
     };
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
             case DIVISOR: updateDivisor = (Integer) msg.value; break;
             case INTENSITY:
               scheduleTest((Integer) msg.value);
               break;
             case SHUTDOWN:
               sched.schedule(new Runnable() { public void run() {shutdown();} }, 0, SECONDS);
               break;
             }
           } catch (Throwable t) {t.printStackTrace();}
         }
         @Override
         public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
           log.error("Netty error", e);
         }
       }
       , new ObjectEncoder()
       )));
     return b;
   }
 
 
   void runTest() throws Exception {
     fac.call(new ContextAction() { @Override public Object run(Context cx) {
       log.debug("Initializing test");
       jsCall("init");
       log.debug("Initialized");
       setJsHelper(new JsHelper(StressTester.this));
       jsScope.sealObject(); return null;
     }});
     nettySend(channel, new Message(INIT, collectIndices()), true);
     try {
       scheduleTest(1);
       sched.scheduleAtFixedRate(new Runnable() { public void run() {
         final List<Stats> stats = stats();
         if (stats.isEmpty()) return;
         nettySend(channel, new Message(STATS, stats.toArray(new Stats[stats.size()])));
       }}, 100, SECONDS.toMicros(1)/TIMESLOTS_PER_SEC, MICROSECONDS);
     }
     catch (Throwable t) {
       nettySend(channel, new Message(ERROR, t));
       shutdown();
     }
   }
 
   void setJsHelper(final JsHelper helper) {
     fac.call(new ContextAction() { @Override public Object run(Context cx) {
      putProperty(jsScope, "$", helper);
       return null;
     }});
   }
 
   synchronized void scheduleTest(int newIntensity) {
     if (intensity == newIntensity) return;
     intensity = newIntensity;
     if (testTask != null) testTask.cancel(true);
     testTask = intensity > 0?
       sched.scheduleAtFixedRate(this, 0, SECONDS.toNanos(1)/intensity, NANOSECONDS)
       : null;
   }
 
   @Override public void run() { jsCall("test"); }
 
   ArrayList<Integer> collectIndices() {
     final ArrayList<Integer> ret = new ArrayList<>();
     for (LiveStats ls : lsmap.values()) if (ls.name != null) {
       System.out.println("name " + ls.name + " index " + ls.index);
       ret.add(ls.index);
     }
     return ret;
   }
 
   List<Stats> stats() {
     final List<Stats> ret = new ArrayList<>(lsmap.size());
     for (LiveStats ls : lsmap.values()) {
       final Stats stats = ls.stats(updateDivisor);
       if (stats != null) ret.add(stats);
     }
     return ret;
   }
 
   void shutdown() {
     try {
       sched.shutdown();
       sched.awaitTermination(5, SECONDS);
       client.close();
       log.debug("HTTP Client shut down");
       netty.shutdown();
       log.debug("Netty client shut down");
     } catch (InterruptedException e) { }
   }
 
   static final String[]
     javaCandidates = {"javaw", "javaw.exe", "java", "java.exe", "j9w", "j9w.exe", "j9", "j9.exe"},
     javaDirCandidates = {"bin", "jre" + File.separator + "bin"};
 
   static String java() {
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
 
   public static Process launchTester(String scriptFile) {
     try {
       final String bpath = getBundleFile(stressTestPlugin().bundle()).getAbsolutePath();
       final String slash = File.separator;
       final String cp = join(File.pathSeparator, bpath, bpath+slash+"bin", bpath+slash+"lib");
       log.debug("Launching {} with classpath {}", StressTester.class.getSimpleName(), cp);
       return new ProcessBuilder(java(), "-Xmx128m", "-XX:+UseConcMarkSweepGC", "-cp", cp,
           StressTester.class.getName(), scriptFile).inheritIO().start();
     } catch (IOException e) { throw new RuntimeException(e); }
   }
 
   public static void main(String[] args) {
     try {
       log.info("Loading script {}", args[0]);
       new StressTester(args[0]).runTest();
     } catch (Throwable t) { log.error("", t); }
   }
 }
