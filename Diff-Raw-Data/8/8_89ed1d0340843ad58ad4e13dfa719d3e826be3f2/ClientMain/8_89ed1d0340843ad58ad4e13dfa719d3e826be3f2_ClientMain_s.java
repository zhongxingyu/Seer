 package com.virtualdisk.main;
 
 import com.virtualdisk.client.*;
 
 import java.lang.*;
 import java.util.*;
 
 public class ClientMain
 {
     public static void main(String... args)
     throws Exception
     {
         String host = args[0];
         int port = Integer.valueOf(args[1]);
         Random random = new Random(2012);
 
         System.out.println("Connecting to the coordinator on " + host + ":" + port + "...");
 
         Client client = new Client(host, port);
         client.connect();
 
         System.out.println("Connected.");
         System.out.println("Creating volume 0...");
 
         client.createVolume(0);
         client.createVolume(1);
 
         System.out.println("Created.");
 
         Thread.sleep(1000);
 
         // ...... issue some reads/writes........
         Thread.sleep(100);
 
         byte[] block = new byte[client.getBlockSize()];
 
 
         /*
         for (int index = 0; index < 4; ++index)
         {
             System.out.println("Writing a block to volume 0, location " + index + "...");
             random.nextBytes(block);
             client.write(0, index, block);
         }
         */
 
        for (int index = 0; index < 2; ++index)
         {
             System.out.println("Reading a block from volume 0, location " + index + "...");
             client.read(0, index);
         }
 
         Thread.sleep(100);
 
         System.out.println("Deleting volume 0...");
 
         client.deleteVolume(0);
 
         Thread.sleep(100);
 
         System.out.println("Deleted.");
         System.out.println("Disconnecting from the coordinator...");
 
         client.disconnect();
 
         System.out.println("Disconnected. Now exiting.");
     }
 }
 
