 /*
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License version 2
  * as published by the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, see <http://www.gnu.org/licenses/>
  *
  */
 package net.FriendsUnited.NodeLevel;
 
 import java.net.Inet6Address;
 import java.net.InetAddress;
 import java.net.NetworkInterface;
 import java.net.SocketException;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.UUID;
 import java.util.Vector;
 
 import net.FriendsUnited.FriendPacket;
 import net.FriendsUnited.FriendPacketHandler;
 import net.FriendsUnited.NodePacket;
 import net.FriendsUnited.RemoteServer;
 import net.FriendsUnited.NodeLevel.Packet.ServerListPacket;
 import net.FriendsUnited.NodeLevel.Packet.ServerRequestPacket;
 import net.FriendsUnited.Util.Tool;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
/** Handles incomming and outpoing Packets on Node to Node Level.
  *
  * @author Lars P&ouml;tter
  * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
  */
 public class NodePacketHandler extends Thread implements NodeDirectory
 {
     private final Logger log = LoggerFactory.getLogger(this.getClass().getName());
 
     private final UUID ownNodeId = UUID.randomUUID();
     private Hashtable<String, RemoteNode> allNodes = new Hashtable<String, RemoteNode>();
     private Vector<PacketTransmitter> allPacketTransmitters = new Vector<PacketTransmitter>();
     private final FriendPacketHandler fph;
 
     /**
      *
      */
     public NodePacketHandler(FriendPacketHandler fph)
     {
         this.fph = fph;
         Enumeration<NetworkInterface> en;
         try
         {
             en = NetworkInterface.getNetworkInterfaces();
             while(true == en.hasMoreElements())
             {
                 NetworkInterface curInterface = en.nextElement();
                 log.info("Creating Packet Transmitter for  Interface {} !", curInterface.getDisplayName());
                 Enumeration<InetAddress> ei = curInterface.getInetAddresses();
                 while(true == ei.hasMoreElements())
                 {
                     InetAddress curIp = ei.nextElement();
                     // start Socket: Local - LAN - Internet
                     if(false == curIp.isLoopbackAddress())
                     {
                         if(true == (curIp instanceof Inet6Address ))
                         {
                             // TODO IPv6 support
                         }
                         else
                         {
                             // IPv4
                             PacketTransmitter pt = new PacketTransmitter(ownNodeId, curIp, this);
                             pt.start();
                             allPacketTransmitters.add(pt);
                         }
                     }
                    // else -> Loopback Address cannot be announced
                    // and is not needed we can use the public IP of this Host for loopback communication
                 }
             }
         }
         catch(SocketException e)
         {
             log.error(Tool.fromExceptionToString(e));
         }
     }
 
     public void run()
     {
         while(true)
         {
             Enumeration<RemoteNode> en = allNodes.elements();
             while(true == en.hasMoreElements())
             {
                 RemoteNode rn = en.nextElement();
                 rn.tick();
             }
             try
             {
                 Thread.sleep(1);
             }
             catch(InterruptedException e)
             {
                 log.info("Got Interrupted !");
             }
             if(isInterrupted())
             {
                 break;
             }
         }
     }
 
 
     public void close()
     {
         // The Packet Handler
         this.interrupt();
         // The Remote Nodes
         Enumeration<RemoteNode> er = allNodes.elements();
         while(true == er.hasMoreElements())
         {
             RemoteNode rn = er.nextElement();
             rn.setChannelStateToClosed();
         }
         // The PacketTransmitter
         Enumeration<PacketTransmitter> ept = allPacketTransmitters.elements();
         while(true == ept.hasMoreElements())
         {
             PacketTransmitter pt = ept.nextElement();
             pt.interrupt();
         }
     }
 
     public void addNode(String NodeId, InetAddress host, int port, final PacketTransmitter pt)
     {
         if((null == NodeId) | (null == host) || (0 == port))
         {
             return;
         }
         // check if this is really a new node, or if we already know him
         if(true == ownNodeId.toString().equals(NodeId))
         {
             // we know us
             return;
         }
         RemoteNode rn = getRemoteNodeFor(UUID.fromString(NodeId));
         if(null != rn)
         {
             int rp = rn.getPort();
             InetAddress rip = rn.getLocation();
             if((port != rp) || (false == host.equals(rip)))
             {
                 log.debug("New Node already known but with different Location/Port !");
                 // -> add the new location as alternative location TODO
                 return;
             }
             else
             {
                 // OK we know this Node -> nothing to do anymore
                 return;
             }
         }
         else
         {
             log.info("Added new Node : {}", NodeId);
             // this is a really new Node
             rn = new RemoteNode(ownNodeId,
                                 UUID.fromString(NodeId),
                                 pt,
                                 host,
                                 port,
                                 PacketTransmitter.OUTGOING_CONNECTION_COST,
                                 this);
             addRemoteNode(UUID.fromString(NodeId), rn);
         }
     }
 
     public void sendPacketIntoNetwork(NodePacket np)
     {
         if(true == (ownNodeId.toString()).equals(np.getNodeId()))
         {
             // This is a Packet for us
             FriendPacket fp = new FriendPacket(np.getPayload());
             if(true == fp.isValid())
             {
                 fph.sendPacketIntoNetwork(fp);
             }
         }
         else
         {
             // send the Packet out
             RemoteNode rn = getRemoteNodeFor(UUID.fromString(np.getNodeId()));
             if(null == rn)
             {
                 // Node can not be reached -> drop packet
                 log.info("Dropping Packet to unreachable Node with ID : {} !", np.getNodeId());
             }
             else
             {
                 rn.putPacket(np.getPayload());
             }
         }
     }
 
     public void processServerInfoFrom(String NodeId, ServerListPacket reply)
     {
         for(int i = 0; i < reply.getNumberServersInPacket(); i++)
         {
             String curServerName = reply.getServerAt(i);
             if(false == fph.isServerRegisterd(curServerName))
             {
                 RemoteServer theNewServer = new RemoteServer(curServerName, this, NodeId);
                 fph.registerServer(theNewServer);
             }
         }
     }
 
     public void processServerRequestFrom(String NodeId, ServerRequestPacket request)
     {
         ServerListPacket slp = new ServerListPacket(request.getOffset(), request.getLimit(), fph);
         sendPacketIntoNetwork(new NodePacket(NodeId, slp.toByteArray()));
     }
 
     public RemoteNode getRemoteNodeFor(UUID remNode)
     {
         return allNodes.get(remNode.toString());
     }
 
     public void addRemoteNode(UUID remNode, RemoteNode remoteNode)
     {
         allNodes.put(remNode.toString(), remoteNode);
     }
 
 }
