 package q3.server.reactor;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.nio.channels.SelectionKey;
 import java.nio.channels.Selector;
 import java.nio.channels.ServerSocketChannel;
 import java.util.Iterator;
 
 import org.apache.commons.configuration.ConfigurationException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
import q3.server.configuration.AppConfiguration;
 import q3.server.db.PostgresAgentInitialException;
 import q3.server.reactor.task.ThreadPool;
 
 /**
  * An implementation of the Reactor pattern
  */
 public class ReactorTCPServer extends Thread {
 	static final Logger logger = LoggerFactory.getLogger(ReactorTCPServer.class);
     protected int _port;
     protected int _poolSize;
     protected ThreadPool _pool;
     protected volatile boolean _shouldRun = true;
 	private int _max_client_limit = 50;
 
     /**
      * Creates a new ReactorTCPServer
      * @param poolSize the number of WorkerThreads to include in the ThreadPool
      * @param port the port to bind the ReactorTCPServer to
      * @throws IOException if some I/O problems arise during connection
      */
     public ReactorTCPServer(int poolSize, int port, int max_client_limit) throws IOException {
         _port = port;
         _poolSize = poolSize;
         _max_client_limit = max_client_limit;
     }
 
     /**
      * Main operation of the ReactorTCPServer:
      * <UL>
      * <LI>Uses the <CODE>Selector.select()</CODE> method to find new requests from clients
      * <LI>For each request in the selection set:
      * <UL>If it is <B>acceptable</B>, use the ConnectionAcceptor to accept it,
      * create a new ConnectionReader for it register it to the Selector
      * <LI>If it is <B>readable</B>, use the ConnectionReader to read it,
      * extract messages and insert them to the ThreadPool
      * </UL>
      */
     public void run() {
         try {
             _pool = new ThreadPool(_poolSize);
             _pool.startPool();
 
             /*	Requirement:
              *     - each server thread SHOULD be able to handle multiple clients.
              *     
              *  Design:
              *     Currently, we use NIO's Channel and Selector to reach this requirement.
              *     java nio selector base on Reactor pattern design.
              *     Reactor pattern please refer to wiki http://en.wikipedia.org/wiki/Reactor_pattern 
              */
             ServerSocketChannel ssChannel = ServerSocketChannel.open();
             ssChannel.configureBlocking(false);
             ssChannel.socket().bind(new InetSocketAddress(_port));
 
             Selector selector = Selector.open();
             ssChannel.register(selector, SelectionKey.OP_ACCEPT, new ConnectionAcceptor(selector, ssChannel, _pool, _max_client_limit));
 
             while (_shouldRun) {
                 selector.select();
                 Iterator it = selector.selectedKeys().iterator();
                 while (it.hasNext()) {
                     SelectionKey selKey = (SelectionKey)it.next();
                     it.remove();
 
                     // Check if it's a connection request
                     if (selKey.isValid() && selKey.isAcceptable()) {
                         ConnectionAcceptor connectionAcceptor = (ConnectionAcceptor)selKey.attachment();
                         connectionAcceptor.accept();
                     }
                     // Check if a message has been sent
                     if (selKey.isValid() && selKey.isReadable()) {
                         ConnectionReader connectionReader = (ConnectionReader)selKey.attachment();
                         try {
 							connectionReader.read();
 						} catch (IOException e) {
 							logger.error("connectionReader exception. " + e);
 							selKey.cancel();
 							continue;
 						} catch (ConfigurationException e) {
 							/*
 							 * For this kind of ENV setup relative Exception, 
 							 * we prefer to point OUT as early as possible.
 							 *   
 							 */
 							logger.error("Databases Configuration Error");
 							e.printStackTrace();
 							break;
 						} catch (PostgresAgentInitialException e) {
 							logger.error("Databases Configuration Error");
 							e.printStackTrace();
 							break;
 						}
                     }
                 }
             }
         } catch (IOException e) {
             e.printStackTrace(System.err);
             stopReactor();
         }
         stopReactor();
     }
 
     /**
      * Returns the listening port of the ReactorTCPServer
      * @return the listening port of the ReactorTCPServer
      */
     public int getPort() {
         return _port;
     }
 
     /**
      * Stops the ReactorTCPServer activity, including the ReactorTCPServer thread and the Worker
      * Threads in the Thread Pool.
      */
     public void stopReactor(){
         _shouldRun = false;
         _pool.stopPool();
     }
 
    public static void main(String args[]) throws ConfigurationException {
    	AppConfiguration appConfig = new AppConfiguration("../resource/config.xml");
     	int THREAD_LIMIT = 10;
    	int PORT = Integer.parseInt(appConfig.getServerPort());
         try {
             ReactorTCPServer reactor = new ReactorTCPServer(THREAD_LIMIT, PORT, 50);
             reactor.start();
             System.out.println("ReactorTCPServer is ready on port " + reactor.getPort());
             reactor.join();
         }
         catch (Exception e) {
             e.printStackTrace();
         }
 
     }
 }
