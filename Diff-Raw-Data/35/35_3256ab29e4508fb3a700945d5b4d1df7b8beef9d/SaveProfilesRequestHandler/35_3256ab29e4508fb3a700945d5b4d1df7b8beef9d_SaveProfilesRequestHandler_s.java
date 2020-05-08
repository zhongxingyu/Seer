 package org.darkquest.ls.packethandler.loginserver;
 
 import org.darkquest.ls.model.PlayerSave;
 
 import org.darkquest.ls.model.World;
 import org.darkquest.ls.net.LSPacket;
 import org.darkquest.ls.net.Packet;
 import org.darkquest.ls.packetbuilder.loginserver.ReplyPacketBuilder;
 import org.darkquest.ls.packethandler.PacketHandler;
 import org.jboss.netty.channel.Channel;
 
 import java.util.Iterator;
 import java.util.Map.Entry;
 
 public final class SaveProfilesRequestHandler implements PacketHandler {
 
     private final ReplyPacketBuilder builder = new ReplyPacketBuilder();
 
     public void handlePacket(Packet p, final Channel session) throws Exception {
         final long uID = ((LSPacket) p).getUID();
         World world = (World) session.getAttachment();
         boolean shutdownEvent = p.readByte() == 1 ? true : false;
         System.out.println("World " + world.getID() + " requested to save all profiles");
 
         Iterator<Entry<Long, PlayerSave>> iterator = world.getAssosiatedSaves().iterator();
 
         while (iterator.hasNext()) { // current players online
             PlayerSave profile = iterator.next().getValue();
             profile.save();
         	iterator.remove(); 
         }
         
        Iterator<Entry<Long, PlayerSave>> cachedIterator = world.getPlayerCache().iterator();
        
        while(cachedIterator.hasNext()) { // cached players 
            System.out.println("Found cached entry, saving and removing...");
            PlayerSave profile = cachedIterator.next().getValue();
             world.getPlayerCache().get(profile.getUser()).save();
            world.getPlayerCache().remove(profile.getUser());
         }
         
         if(shutdownEvent) {
         	System.out.println("Blocking cached access");
         	world.setBlocking(true);
         }
 
         builder.setUID(uID);
         builder.setSuccess(true);
 
         LSPacket packet = builder.getPacket();
         if (packet != null) {
             session.write(packet);
         }
     }
 
 }
