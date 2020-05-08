 package com.herocraftonline.dev.heroes.ui;
 
 import net.minecraft.server.EntityPlayer;
 import net.minecraft.server.Packet;
 import net.minecraft.server.Packet131;
 
 public class UIUpdater extends Thread {
 
     private EntityPlayer player;
     private byte[] bytes;
     private byte mapid;
     private final int interval;
 
     public UIUpdater(EntityPlayer player, byte[] bytes, byte mapid, int interval) {
         this.player = player;
         this.bytes = bytes;
         this.mapid = mapid;
         this.interval = interval;
     }
 
     @Override
     public void run() {
         // for (int i = 0; i < 128; i++) {
         for (int i = 127; 0 <= i; --i) {
             byte[] abyte = new byte[131];
             abyte[1] = (byte) i;
             for (int j = 0; j < 128; j++) {
                 abyte[j + 3] = bytes[j * 128 + i];
             }
            Packet packet = new Packet131((short) net.minecraft.server.Item.MAP.id, mapid, abyte); // Hard coded for Map
                                                                                                   // ID 0 atm.
             if (packet != null) {
                 player.netServerHandler.sendPacket(packet);
                 try {
                     sleep(interval);
                 } catch (InterruptedException e) {
                     e.printStackTrace();
                 }
             }
         }
         interrupt();
     }
 }
