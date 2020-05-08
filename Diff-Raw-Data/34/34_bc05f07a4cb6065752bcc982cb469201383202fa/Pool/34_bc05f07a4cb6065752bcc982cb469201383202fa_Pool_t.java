 /*
  * Made by Wannes 'W' De Smet
  * (c) 2011 Wannes De Smet
  * All rights reserved.
  * 
  */
 package net.wgr.xenmaster.pool;
 
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.TimeUnit;
 import net.wgr.rmi.Remote;
 import net.wgr.utility.GlobalExecutorService;
 import org.apache.log4j.Logger;
 
 /**
  * 
  * @created Oct 23, 2011
  * @author double-u
  */
 public class Pool implements Runnable {
 
     protected HashMap<InetAddress, Worker> workers;
     protected HashMap<InetAddress, Remote> remotes;
     // Pool Orchestrating Protocol
     protected DatagramSocket popSocket;
     protected ServerSocket rmiSocket;
     protected Thread wpop, rmi;
     protected boolean run, master;
     protected Worker local;
     // The port number is completely random ... or is it?
     public final static int PORT = 24515;
     private volatile static Pool instance;
 
     private Pool() {
         workers = new HashMap<>();
         wpop = new Thread(this, "WPOP");
         rmi = new Thread(new RMIListener(), "Pool RMI");
     }
 
     public static Pool get() {
         if (instance == null) {
             instance = new Pool();
         }
         return instance;
     }
 
     public Worker getLocalWorker() {
         if (local == null) {
             local = WorkerFactory.getLocalWorker();
         }
         return local;
     }
 
     public Map<InetAddress, Worker> getWorkers() {
         return workers;
     }
 
     public void boot() throws SocketException, UnknownHostException, IOException {
         popSocket = new DatagramSocket(PORT);
 
         rmiSocket = new ServerSocket(PORT);
 
         run = true;
         wpop.start();
         rmi.start();
         GlobalExecutorService.get().scheduleAtFixedRate(new AliveBroadcaster(), 0, 1, TimeUnit.SECONDS);
     }
    
    public void stop() {
        // Send "going down" msg to pool master or to friend if this is the master
        run = false;
    }
 
     protected class AliveBroadcaster implements Runnable {
 
         protected DatagramSocket sock;
 
         protected AliveBroadcaster() throws SocketException, UnknownHostException {
             sock = new DatagramSocket();
             sock.connect(InetAddress.getByName("255.255.255.255"), PORT);
             sock.setBroadcast(true);
         }
 
         @Override
         public void run() {
             try {
                 XMPacket xmp = new XMPacket();
                 xmp.alive = true;
                 xmp.master = master;
                 byte[] data = xmp.getContents();
                 DatagramPacket packet = new DatagramPacket(data, data.length);
                 try {
                     sock.send(packet);
                 } catch (IOException ex) {
                     Logger.getLogger(getClass()).error("Failed to broadcast alive packet", ex);
                 }
             } catch (Exception ex) {
                 Logger.getLogger(getClass()).error("Exception", ex);
             }
         }
     }
 
     @Override
     public void run() {
         DatagramPacket packet = new DatagramPacket(new byte[1512], 1512);
         while (run) {
             try {
                 popSocket.receive(packet);
                 process(packet);
             } catch (IOException ex) {
                 Logger.getLogger(getClass()).warn("Receiving UDP packet failed", ex);
             }
         }
     }
 
     protected void process(DatagramPacket packet) {
         XMPacket xm = new XMPacket();
         xm.setContents(packet.getData(), packet.getLength());
 
         if (xm.master && master) {
             // Hey! I'm the master! Get out of here you!
             Logger.getLogger(getClass()).warn("Rogue master detected at " + packet.getSocketAddress().toString());
         }
 
         if (!workers.containsKey(packet.getAddress())) {
             Logger.getLogger(getClass()).info("New worker detected at " + packet.getAddress().getCanonicalHostName());
             workers.put(packet.getAddress(), WorkerFactory.getWorkerWithAddress(packet.getAddress()));
         }
     }
 
     protected class RMIListener implements Runnable {
 
         @Override
         public void run() {
             while (run) {
                 try {
                     Socket sock = rmiSocket.accept();
                     Remote r = new Remote();
                     r.boot(sock);
                     r.getReceiver().addInvocationTarget(local);
                 } catch (IOException ex) {
                     Logger.getLogger(getClass()).warn("Accepting RMI connection failed", ex);
                 }
             }
         }
     }
 
     public static boolean isMasterActive() {
         boolean masterDetected = false;
 
         for (Worker w : Pool.get().getWorkers().values()) {
             if (w instanceof Master) {
                 masterDetected = true;
                 break;
             }
         }
 
         if (masterDetected) {
             return true;
         }
 
         Logger.getLogger(Pool.class).info("Waiting for 2 seconds for any master to come online");
         try {
             Thread.sleep(2000);
             for (Worker w : Pool.get().getWorkers().values()) {
                 if (w instanceof Master) {
                     masterDetected = true;
                     break;
                 }
             }
         } catch (InterruptedException ex) {
             Logger.getLogger(Pool.class).error("Waiting for master failed", ex);
         }
 
         if (masterDetected) {
             return true;
         } else {
             return false;
         }
     }
 }
