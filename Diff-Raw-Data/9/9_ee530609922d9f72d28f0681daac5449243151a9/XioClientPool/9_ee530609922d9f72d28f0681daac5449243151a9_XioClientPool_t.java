 package org.xmodel.net;
 
import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Queue;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.TimeUnit;
 
 import org.xmodel.GlobalSettings;
 import org.xmodel.log.Log;
 
 public class XioClientPool
 {
   public final static int defaultIdleTimeout = 3 * 60 * 1000;
   
   public XioClientPool( IXioClientFactory factory)
   {
     this( factory, GlobalSettings.getInstance().getScheduler());
   }
   
   public XioClientPool( IXioClientFactory factory, ScheduledExecutorService scheduler)
   {
     this.factory = factory;
     this.scheduler = scheduler;
     this.clients = new HashMap<String, Queue<XioClient>>();
     this.timers = new ConcurrentHashMap<XioClient, ScheduledFuture<?>>();
     this.idleTimeout = defaultIdleTimeout;
   }
   
   /**
    * Set the connection idle timeout.
    * @param timeout The timeout in milliseconds.
    */
   public void setIdleTimeout( int timeout)
   {
     idleTimeout = timeout;
   }
 
   /**
    * Lease a client connection to the specified address for the specified context.
    * @param address The host address.
    * @return Returns a connected client.
    */
   public XioClient lease( InetSocketAddress address)
   {
     Queue<XioClient> queue = getClients( address);
     
     XioClient client = queue.poll();
     if ( client == null || !client.isConnected())
     {
       client = factory.newInstance( address);
     }
     else
     {
       //
       // If the timer has expired, then the client will be disconnected, so get a new one.
       // A null future means that the timer has expired and TimeoutTask has removed the future from the map.
       //
       ScheduledFuture<?> future = timers.remove( client);
       if ( future == null || !future.cancel( false)) return lease( address);
     }
     
     log.debugf( "Leasing XioClient %X, state=%s", client.hashCode(), client.isConnected()? "connected": "disconnected");
     return client;
   }
   
   /**
    * Return a client connection to the pool.
    * @param client The XIO client connection.
    */
   public void release( XioClient client)
   {
     log.debugf( "Releasing XioClient %X, state=%s", client.hashCode(), client.isConnected()? "connected": "disconnected");
 
     if ( client.isConnected()) 
     {
       //
       // Create idle timeout for connected client. The client will be removed and disconnected when the timer fires.
       //
       ScheduledFuture<?> future = scheduler.schedule( new TimeoutTask( client), idleTimeout, TimeUnit.MILLISECONDS);
       timers.put( client, future);
       
       // 
       // Put client in reuse queue.
       //
       Queue<XioClient> queue = getClients( client.getRemoteAddress());
       queue.offer( client);
     }
   }
   
   /**
    * Returns the queue containing clients connected to the specified address.
    * @param socketAddress The remote address.
    * @return Returns the queue containing clients connected to the specified address.
    */
   private Queue<XioClient> getClients( InetSocketAddress socketAddress)
   {
     String address = toString( socketAddress);
     synchronized( clients)
     {
       Queue<XioClient> queue = clients.get( address);
       if ( queue == null)
       {
         log.debugf( "Creating new queue for address, %s", address);
         queue = new ConcurrentLinkedQueue<XioClient>();
         clients.put( address, queue);
       }
       return queue;
     }
   }
   
   /**
    * Remove the specified client.
    * @param client The client.
    */
   private void removeClient( XioClient client)
   {
     log.debugf( "Deleting XioClient %X, state=%s", client.hashCode(), client.isConnected()? "connected": "disconnected");
     
     InetSocketAddress address = client.getRemoteAddress();
     
     Queue<XioClient> queue = getClients( client.getRemoteAddress());
     synchronized( clients)
     {
       queue.remove( client);
       if ( queue.isEmpty()) clients.remove( toString( address));
     }
     
     timers.remove( client);
   }
   
   /**
    * Returns a consistent string representation of the specified address.
    * @param address The address.
    * @return Returns a consistent string representation of the specified address.
    */
  private static String toString( InetSocketAddress socketAddress)
   {
    InetAddress address = socketAddress.getAddress();
    if ( address == null) return "";
    return address.getHostAddress() + ":" + socketAddress.getPort();
   }
   
   private class TimeoutTask implements Runnable
   {
     public TimeoutTask( XioClient client)
     {
       this.client = client;
     }
     
     /* (non-Javadoc)
      * @see java.lang.Runnable#run()
      */
     @Override
     public void run()
     {
       removeClient( client);
       client.close();
     }
     
     private XioClient client;
   }
   
   private final static Log log = Log.getLog( XioClientPool.class);
 
   private IXioClientFactory factory;
   private ScheduledExecutorService scheduler;
   private Map<String, Queue<XioClient>> clients;
   private Map<XioClient, ScheduledFuture<?>> timers;
   private int idleTimeout;
 }
