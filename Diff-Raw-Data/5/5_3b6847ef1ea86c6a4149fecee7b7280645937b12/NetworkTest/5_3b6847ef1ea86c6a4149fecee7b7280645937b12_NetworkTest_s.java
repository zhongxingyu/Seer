 package de.htwg.wzzrd.control.network;
 
 import java.io.IOException;
 import java.net.SocketException;
 
 import junit.framework.TestCase;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 import org.junit.Test;
 
 import resources.TestDataProvider;
 import de.htwg.wzzrd.model.Settings;
 import de.htwg.wzzrd.model.UIData;
 import de.htwg.wzzrd.model.network.GameNetwork;
 import de.htwg.wzzrd.model.network.GameNetwork.ConnectionFailure;
 import de.htwg.wzzrd.model.network.GameNetworkInterface;
 import de.htwg.wzzrd.model.network.IPacket;
 import de.htwg.wzzrd.model.network.PacketQueue;
 import de.htwg.wzzrd.model.network.packet.JoinGamePacket;
 import de.htwg.wzzrd.model.network.packet.StartGamePacket;
 import de.htwg.wzzrd.model.network.packet.UpdateClient;
 import de.htwg.wzzrd.model.tableComponent.impl.Player;
 
 public class NetworkTest extends TestCase {
     private static final Logger LOG = LogManager.getLogger("NetworkTest");
 
     Thread th_sender;
     Runnable sender = new Runnable() {
         @Override
         public void run() {
             try {
                 GameNetworkInterface net = new GameNetwork();
                 net.send(new StartGamePacket());
                 net.stop();
 
                 assertFalse(net.hasPacket());
 
                 net = new GameNetwork();
                 net.send(new JoinGamePacket("mileippe", "blub"));
                 net.stop();
 
                 net = new GameNetwork();
                 net.send(new JoinGamePacket("BaumRaumWiesel", ""));
                 net.send(new StartGamePacket());
 
                 assertTrue(net.receive() instanceof UpdateClient);
                 net.stop();
 
             } catch (IOException e) {
                 e.printStackTrace();
             } catch (ClassNotFoundException e) {
                 e.printStackTrace();
             } catch (ConnectionFailure e) {
                 e.printStackTrace();
             }
         }
     };
 
     @Test
     public void test() throws SocketException, IOException, InterruptedException {
         LOG.trace("1");
         PacketQueue pq = new PacketQueue();
         ConnectionListener cl = new ConnectionListener(Settings.getPortNumber(), "", pq);
         LOG.trace("2");
         th_sender = new Thread(sender);
         th_sender.start();
         LOG.trace("3");
 
         assertNull(pq.getPacket());
         LOG.trace("4");
         pq.waitForPacket();
         LOG.trace("5");
         IPacket p = pq.getPacket();
         assertTrue(p instanceof JoinGamePacket);
         LOG.trace("6");
         JoinGamePacket jgp = (JoinGamePacket) p;
         LOG.trace("7");
         GameNetworkInterface gnet = jgp.getGameNetwork();
         UIData d = new UIData();
         LOG.trace("8");
         d.setActivePlayer(new Player("Knorke"));
         gnet.send(new UpdateClient(d, TestDataProvider.getAllCardsInRandomOrder()));
         cl.interrupt();
         LOG.trace("9");
     }
 }
