 package br.unb.tr2.zeroconf;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.net.*;
 import java.util.Enumeration;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * Copyright (C) 2013 Loop EC - All Rights Reserved
  * Created by sandoval for harmonic-server
  */
 public class AnnouncementThread implements Runnable {
 
     ServiceAnnouncement serviceAnnouncement;
 
     DiscoveryService discoveryService;
 
     DatagramSocket socket;
 
     Logger logger = Logger.getLogger("AnnoucementThread");
 
     public AnnouncementThread(DiscoveryService discoveryService, ServiceAnnouncement serviceAnnouncement) {
         this.discoveryService = discoveryService;
        this.serviceAnnouncement = new ServiceAnnouncement(serviceAnnouncement);
     }
 
     @Override
     public void run() {
         try {
             socket = new DatagramSocket();
             socket.setBroadcast(true);
 
             Enumeration interfaces = NetworkInterface.getNetworkInterfaces();
             while (interfaces.hasMoreElements()) {
                 NetworkInterface networkInterface = (NetworkInterface) interfaces.nextElement();
                 if (networkInterface.isLoopback() || !networkInterface.isUp())
                     continue;
                 for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                     InetAddress broadcastAddress = interfaceAddress.getBroadcast();
                     if (broadcastAddress == null)
                         continue;
                     try {
                         ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                         ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);
                         serviceAnnouncement.setAddress(interfaceAddress.getAddress());
                         objectStream.writeObject(serviceAnnouncement);
                         objectStream.flush();
                         byte[] data = byteStream.toByteArray();
 
                         discoveryService.notifySentServiceAnnouncement(serviceAnnouncement);
                         DatagramPacket packet = new DatagramPacket(data, data.length, broadcastAddress, 44444);
                         socket.send(packet);
                     } catch (Exception e) {
                         logger.severe("Couldn't send broadcast packet: " + e.getMessage());
                     }
                 }
             }
 
         } catch (SocketException e) {
             logger.log(Level.SEVERE, "Failed to open broadcast socket: " + e.getMessage());
         } catch (IOException e) {
             logger.log(Level.SEVERE, "IOException while broadcasting: " + e.getMessage());
         }
     }
 }
