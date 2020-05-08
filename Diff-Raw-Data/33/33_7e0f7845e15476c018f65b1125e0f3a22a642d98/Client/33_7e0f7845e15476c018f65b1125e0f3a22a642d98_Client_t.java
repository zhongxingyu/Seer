 package com.prsc.gs.connection;
 
 import java.util.Queue;
 
 
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.concurrent.atomic.AtomicReference;
 
 import org.jboss.netty.channel.Channel;
 
import com.prsc.config.Formulae;
 import com.prsc.gs.model.Player;
 import com.prsc.gs.plugins.phandler.PacketHandler;
 import com.prsc.gs.registrar.PortRegistrar;
 import com.prsc.gs.registrar.impl.PacketHandlers;
import com.prsc.gs.tools.DataConversions;
 import com.prsc.gs.util.Logger;
 
 public final class Client {
 	
 	private final AtomicReference<Player> player = new AtomicReference<Player>();
 	
     private final AtomicReference<Long> serverKey = new AtomicReference<Long>();
 
     private final Queue<RSCPacket> packets = new ConcurrentLinkedQueue<RSCPacket>();
     
     private Channel channel = null;
 	
 	public Client(Channel channel) {
 		this.channel = channel;
 	}
 	
 	public Channel getChannel() {
 		return channel;
 	}
 	
 	public void setAssociatedPlayer(Player player) {
 		this.player.getAndSet(player);
 	}
 	
 	public void setServerKey(long sessionKey) {
 		this.serverKey.getAndSet(sessionKey);
 	}
 	
 	public long getServerKey() {
 		return this.serverKey.get();
 	}
 	
 	public Player getPlayer() {
 		return player.get();
 	}
 	
 	public void pushToMessageQueue(RSCPacket packet) {
 		packets.add(packet);
 	}
 	
 	public void process() {
 		RSCPacket currentPacket = null;
 		int packetCount = 0;
 		
 		//&& !DataConversions.inArray(Formulae.safePacketIDs, currentPacket.getID())
 		while((currentPacket = packets.poll()) != null && packetCount++ < 60) {
 			PacketHandler ph = PortRegistrar.lookup(PacketHandlers.class).getGameHandlers().get(currentPacket.getID());
             //System.out.println("Packet id: " + currentPacket.getID());
             if(getPlayer() != null) {
             	getPlayer().ping();
             }
             if (ph != null) {
                 try {
                     ph.handlePacket(currentPacket, channel);
                 } catch (Exception e) {
                 	if(getPlayer() != null) {
                 		getPlayer().getActionSender().sendLogout();
                 		getPlayer().destroy(false);
                 	}
                     Logger.error(e);
                 }
             } else {
                 String err = String.format("Unhandled packet from %s: %d len: %d", getPlayer().getCurrentIP(), currentPacket.getID(), currentPacket.getLength());
                 Logger.error(err);
             }
 		}
 	}
 	
 }
