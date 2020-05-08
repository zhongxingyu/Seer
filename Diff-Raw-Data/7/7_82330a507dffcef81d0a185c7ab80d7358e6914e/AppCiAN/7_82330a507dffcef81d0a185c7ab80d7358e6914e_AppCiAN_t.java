 package ext.jist.swans.app;
 
 import java.io.File;
 import java.lang.reflect.Method;
 import java.net.InetAddress;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import jist.runtime.JistAPI;
 import jist.runtime.JistAPI.Continuation;
 import jist.swans.Constants;
 import jist.swans.app.AppInterface;
 import jist.swans.misc.Message;
 import jist.swans.misc.MessageBytes;
 import jist.swans.net.NetAddress;
 import jist.swans.net.NetInterface;
 import jist.swans.trans.TcpServerSocket;
 import jist.swans.trans.TcpSocket;
 import jist.swans.trans.TransInterface.SocketHandler;
 import jist.swans.trans.TransInterface.TransTcpInterface;
 import jist.swans.trans.TransInterface.TransUdpInterface;
 import jist.swans.trans.TransTcp;
 import jist.swans.trans.TransUdp;
 import jist.swans.trans.TransUdp.UdpMessage;
 import ext.util.stats.DucksCompositionStats;
 
 /**
  * Application for every node running CiAN
  * 
  * @author Jordan Alliot
  */
 public class AppCiAN implements AppInterface, AppInterface.TcpApp, AppInterface.UdpApp, SocketHandler
 {
     // network entity.
     protected NetInterface              netEntity;
 
     protected TransTcp                  transTcp;
 
     protected TransUdp                  transUdp;
 
     // self-referencing proxy entity.
     protected Object                    self;
 
     protected int                       nodeId;
 
     // composition stats accumulator
     protected DucksCompositionStats     compositionStats;
 
     protected String[]                  args;
 
     protected CiANAdapter               adapter;
 
     protected BlockingQueue<UdpMessage> udpMessageQueue;
     protected int                       multicastPort;
 
     public AppCiAN(int nodeId, DucksCompositionStats compositionStats, String[] args) {
         this.nodeId = nodeId;
         this.compositionStats = compositionStats;
         this.args = args;
         // init self reference
         this.self = JistAPI.proxyMany(this, new Class[] { AppInterface.class, AppInterface.TcpApp.class,
                 AppInterface.UdpApp.class });
 
         this.transTcp = new TransTcp();
         this.transUdp = new TransUdp();
 
         this.udpMessageQueue = new LinkedBlockingQueue<UdpMessage>();
         this.multicastPort = 0;
     }
 
     public int getNodeId() {
         return nodeId;
     }
 
     /**
      * Handler for new UDP packets.
      * 
      * @see CiANAdapter#receiveUdpPacket()
      */
     public void receive(Message msg, NetAddress src, int srcPort) throws Continuation {
         UdpMessage uMsg;
         if (msg instanceof UdpMessage) {
             uMsg = (UdpMessage) msg;
         } else {
             // We can safely say it is a UDP message coming from the correct
             // port since we only use UDP for beaconning (multicast)
             uMsg = new UdpMessage(srcPort, multicastPort, msg);
         }
         udpMessageQueue.add(uMsg);
     }
 
     public void run() {
         run(this.args);
     }
 
     public void run(String[] args) {
         compositionStats.incrementNumReq();
 
         // Starting a new CiAN application isolated from the others
         adapter = new CiANAdapter(this);
         new CiANThread(adapter, args).start();
     }
 
     /**
      * Set network entity.
      * 
      * @param netEntity
      *            network entity
      */
     public void setNetEntity(NetInterface netEntity) {
         this.netEntity = netEntity;
         this.transTcp.setNetEntity(netEntity);
         this.transUdp.setNetEntity(netEntity);
     }
 
     public AppInterface getAppProxy() {
         return (AppInterface) self;
     }
 
     public AppInterface.TcpApp getTcpAppProxy() {
         return (AppInterface.TcpApp) self;
     }
 
     public AppInterface.UdpApp getUdpAppProxy() {
         return (AppInterface.UdpApp) self;
     }
 
     public TransTcpInterface getTcpEntity() throws Continuation {
         return transTcp.getProxy();
     }
 
     public TransUdpInterface getUdpEntity() throws Continuation {
         return transUdp.getProxy();
     }
 
     /**
      * Thread running CiAN isolated thanks to a new ClassLoader
      * 
      * @author Jordan Alliot
      */
     class CiANThread extends Thread
     {
         private String[]    args;
         private CiANAdapter adapter;
 
         public CiANThread(CiANAdapter adapter, String[] args) {
             super("CiANThread for node " + getNodeId());
             this.args = args;
             this.adapter = adapter;
         }
 
         @Override
         public void run() {
             // Unfortunately we have to do this little hack in order for every
             // node to be isolated from the others.
             // IMPORTANT: it is vital that CiAN.jar is NOT in the classpath!
             try {
                 // We need a new ClassLoader for each node we are creating
                ClassLoader loader = new URLClassLoader(new URL[] { new File("../CiAN/CiAN.jar").toURI().toURL() },
                        null);
 
                 // Loading CiAN class this way ensures us that each node is
                 // isolated from each other
                 Class<?> c = loader.loadClass("system.CiAN");
 
                 // Add a dependency to this node's application into CiAN
                 Method extToolSetter = c.getDeclaredMethod("setExternalTool", Object.class);
                 extToolSetter.invoke(null, adapter);
 
                 // Finally we can start CiAN
                 Method main = c.getDeclaredMethod("main", new Class[] { this.args.getClass() });
                 main.invoke(null, new Object[] { this.args });
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
     }
 
     /**
      * Class of public methods to be used by CiAN
      * 
      * @author Jordan Alliot
      */
     public class CiANAdapter
     {
         public AppCiAN app;
 
         public CiANAdapter(AppCiAN app) {
             this.app = app;
         }
 
         /**
          * @return the InetAddress of this node in Swans
          */
         public InetAddress getInetAddress() {
             return app.netEntity.getAddress().getIP();
         }
 
         /**
          * Opens a new handler for UDP communications on this port
          * 
          * @param port
          */
         public void addUdpHandler(int port) {
             app.transUdp.addSocketHandler(port, app);
             app.multicastPort = port;
         }
 
         /**
          * Removes a previously opened handler for UDP communications on this
          * port
          * 
          * @param port
          */
         public void removeUdpHandler(int port) {
             app.transUdp.delSocketHandler(port);
             app.multicastPort = 0;
         }
 
         /**
          * Sends a multicast packet to all hosts in range
          * 
          * @param packet
          */
         public void sendMulticastPacket(byte[] packet, int port) {
            app.transUdp.send(new MessageBytes(packet), NetAddress.ANY, port, port, Constants.NET_PRIORITY_NORMAL);
         }
 
         /**
          * Blocks until a new UDP packet arrives and retrieves it.
          * CiAN should call this method to receive a UDP packet.
          * 
          * @return the payload of the UDP packet
          * @throws InterruptedException
          */
         public byte[] receiveUdpPacket() throws InterruptedException {
             byte[] packet = null;
             UdpMessage msg = app.udpMessageQueue.take();
             if (msg.getPayload() instanceof MessageBytes) {
                 packet = ((MessageBytes) msg.getPayload()).getBytes();
             } else {
                 packet = new byte[msg.getPayload().getSize()];
                 msg.getPayload().getBytes(packet, 0);
             }
 
             return packet;
         }
 
         /**
          * @param ipAddress
          * @param remotePort
          * @return jist.swans.trans.TcpSocket connected to ipAddress:remotePort
          */
         public TcpSocket createTcpSocket(String ipAddress, int remotePort) {
             TcpSocket socket = new TcpSocket(ipAddress, remotePort);
             socket.setTcpEntity(app.getTcpEntity());
             socket._jistPostInit();
 
             return socket;
         }
 
         /**
          * @param port
          * @return jist.swans.trans.TcpServerSocket listening on port
          */
         public TcpServerSocket createTcpServerSocket(int port) {
             TcpServerSocket serverSocket = new TcpServerSocket(port);
             serverSocket.setTcpEntity(app.getTcpEntity());
             serverSocket._jistPostInit();
 
             return serverSocket;
         }
     }
 }
