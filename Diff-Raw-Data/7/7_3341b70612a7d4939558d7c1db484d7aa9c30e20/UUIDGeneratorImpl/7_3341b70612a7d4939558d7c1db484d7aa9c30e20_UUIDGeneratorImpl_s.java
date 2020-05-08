 /*
  * Copyright (C) 2011, Zenoss Inc.  All Rights Reserved.
  */
 package org.zenoss.zep.impl;
 
 import com.fasterxml.uuid.EthernetAddress;
 import com.fasterxml.uuid.Generators;
 import com.fasterxml.uuid.impl.TimeBasedGenerator;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.zenoss.zep.UUIDGenerator;
 
 import java.net.NetworkInterface;
 import java.net.SocketException;
 import java.nio.ByteBuffer;
 import java.util.Enumeration;
 import java.util.UUID;
 
 /**
  * Generates time-based UUIDs for use in ZEP using the MAC address of the system. Time-based UUIDs are much better
  * for use in the database for keys because they are spaced out sequentially and don't fragment the index.
  */
 public class UUIDGeneratorImpl implements UUIDGenerator {
 
     private final Logger logger = LoggerFactory.getLogger(UUIDGeneratorImpl.class.getName());
     private final TimeBasedGenerator generator;
 
     public UUIDGeneratorImpl() throws SocketException {
         /* Use a generator from a MAC address on the system */
        this.generator = Generators.timeBasedGenerator(new EthernetAddress(getMacAddress()));
     }
 
     /**
      * The EthernetAddress.fromInterface code returns a random ethernet device each time - we would like to try
      * and consistently return the same address each time. This method will return the first network address sorted
      * by name with a valid mac address.
      *
      * @return A byte[] mac address.
      */
     private byte[] getMacAddress() {
         byte[] data = null;
         try {
             String addressName = null;
             final Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
             while (networkInterfaces.hasMoreElements()) {
                 final NetworkInterface ni = networkInterfaces.nextElement();
                 if (ni.isLoopback()) {
                     continue;
                 }
                 byte[] hwaddr = ni.getHardwareAddress();
                 if (hwaddr == null || hwaddr.length != 6) {
                     continue;
                 }
                 if (addressName == null || ni.getName().compareTo(addressName) < 0) {
                     addressName = ni.getName();
                     data = hwaddr;
                 }
             }
             if (addressName != null) {
                 logger.debug("Using network interface {} mac address for UUID generation", addressName);
             }
         } catch (SocketException e) {
             // Fall back to using a randomly generated mac.
         }
         if (data == null) {
             logger.debug("Unable to determine a local mac address for UUID generation - using random address");
         }
         return data;
     }
 
     private static byte[] uuidToBytes(UUID uuid) {
         final ByteBuffer bb = ByteBuffer.allocate(16);
         bb.putLong(uuid.getMostSignificantBits());
         bb.putLong(uuid.getLeastSignificantBits());
         return bb.array();
     }
 
     @Override
     public UUID generate() {
         final UUID uuid = this.generator.generate();
         
         /*
          * Time-based UUIDs (type 1) are not in ascending order and so don't work well as primary keys. We will
          * reshuffle the UUIDs to have the timestamp at the end and the "fixed" information (like the MAC address)
          * at the beginning for less index fragmentation. This means we don't have "valid" UUIDs but they do provide
          * the desired uniqueness and will not fragment the index like random or time based UUIDs.
          */
         final byte[] original = uuidToBytes(uuid);
 
         // UUID                   = time-low "-" time-mid "-"
         //                        time-high-and-version "-"
         //                        clock-seq-and-reserved
         //                        clock-seq-low "-" node
         // time-low               = 4hexOctet
         // time-mid               = 2hexOctet
         // time-high-and-version  = 2hexOctet
         // clock-seq-and-reserved = hexOctet
         // clock-seq-low          = hexOctet
         // node                   = 6hexOctet
         
         final ByteBuffer opt = ByteBuffer.allocate(16);
         /* Copy mac address to first 6 bytes */
         opt.put(original, 10, 6);
         /* Copy clock seq to next 2 bytes */
         opt.put(original, 8, 2);
         /* Copy time-high-and-version to next 2 bytes */
         opt.put(original, 6, 2);
         /* Copy time-mid to next 4 bytes */
         opt.put(original, 4, 2);
         /* Copy time-low to next 4 bytes */
         opt.put(original, 0, 4);
         opt.flip();
         final long mostSig = opt.getLong();
         final long leastSig = opt.getLong();
         final UUID ret = new UUID(mostSig, leastSig);
         return ret;
     }
 }
