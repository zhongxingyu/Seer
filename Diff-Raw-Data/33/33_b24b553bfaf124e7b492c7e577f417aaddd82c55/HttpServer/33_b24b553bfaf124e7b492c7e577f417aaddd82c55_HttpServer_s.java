 /**
  * ToureNPlaner Event Based Prototype
  * 
  * @author Niklas Schnelle
  * 
  * Initially based on: 
  * 	http://docs.jboss.org/netty/3.2/xref/org/jboss/netty/example/http/snoop/package-summary.html
  */
 package tourenplaner.server.prototype.event;
 
 import java.net.InetSocketAddress;
 import java.util.concurrent.Executors;
 
 import org.jboss.netty.bootstrap.ServerBootstrap;
 import org.jboss.netty.channel.socket.oio.OioServerSocketChannelFactory;
 
 import algorithms.KnapsackFactory;
 
 import computecore.AlgorithmRegistry;
 import computecore.ComputeCore;
 
 /**
  * @author Niklas Schnelle
  * @version 0.1 Prototype
  */
 public class HttpServer {
     public static void main(String[] args) {
         // Configure the server.
         ServerBootstrap bootstrap = new ServerBootstrap(
                 new OioServerSocketChannelFactory( // Change to Nio* if you want NIO
                         Executors.newCachedThreadPool(),
                         Executors.newCachedThreadPool()));
 
         // Register Algorithms
         AlgorithmRegistry reg = AlgorithmRegistry.getInstance();
         reg.registerAlgorithm("ks", new KnapsackFactory());
         
         // Create our ComputeCore that manages all ComputeThreads
        ComputeCore comCore = new ComputeCore(4, 20);
         
         // Set up the event pipeline factory.
         bootstrap.setPipelineFactory(new HttpServerPipelineFactory(comCore));
 
         // Bind and start to accept incoming connections.
         bootstrap.bind(new InetSocketAddress(8080));
     }
 }
