 /**
  *  _   _     _       _  __      __ _
  * | | | | __| |_ __ | |/ /__ _ / _| | ____ _
  * | | | |/ _` | '_ \| ' // _` | |_| |/ / _` |
  * | |_| | (_| | |_) | . \ (_| |  _|   < (_| |
  *  \___/ \__,_| .__/|_|\_\__,_|_| |_|\_\__,_|
 *             |_|
  *
  * Route udplog data from UDP multicast to Kafka.
  *
  * Workload is balanced by bucketing messages on the sequence ID value
  * and maintaining a separate instance for each worker. The sequence ID
  * is hashed to account for any periodicity in the data stream.
  *
 * Configuration is read from 'udpkafka.properties' by default. An
 * alt. config file can be specified as the first command-line argument.
  *
  * @author Ori Livneh <ori@wikimedia.org>
  * @version %I%, %G%
  */
 
 import kafka.javaapi.producer.Producer;
 import kafka.producer.KeyedMessage;
 import kafka.producer.ProducerConfig;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.MulticastSocket;
 import java.util.Properties;
 
 
 public class UdpKafka {
 
     private static final Properties props = new Properties();
     private static String topic;
     private static String udpGroup;
     private static int udpPort;
     private static int udpNumWorkers;
     private static int udpWorkerId;
     private static MulticastSocket sock;
 
     public static void main(String args[]) {
         // Allow config file to be specified as the first command-line
         // argument. Default to "udpkafka.properties" if not specified.
         String configFile = args.length == 0
                 ? "udpkafka.properties"
                 : args[0];
         try {
             props.load(new FileInputStream(configFile));
         } catch (IOException e) {
             System.err.format("Config file \"%s\" is missing or invalid.%n", configFile);
             e.printStackTrace(System.err);
             System.exit(1);
         }
         initConfig();
 
         ProducerConfig config = new ProducerConfig(props);
         Producer<String, String> producer = new Producer<String, String>(config);
 
         try {
             sock = new MulticastSocket(null);
             sock.setReuseAddress(true);
             sock.bind(new InetSocketAddress(udpPort));
             sock.joinGroup(InetAddress.getByName(udpGroup));
         } catch (IOException e) {
             System.err.format("Failed to bind/join %s:%d.%n", udpGroup, udpPort);
             e.printStackTrace(System.err);
             System.exit(1);
         }
 
         byte[] buf = new byte[65536];  // Same as Udp2LogConfig::BLOCK_SIZE.
         DatagramPacket packet = new DatagramPacket(buf, buf.length);
 
         try {
             while (true) {
                 sock.receive(packet);
                 String msg = new String(buf, 0, packet.getLength());
                 if (shouldHandle(msg)) {
                     producer.send(new KeyedMessage<String, String>(topic, msg));
                 }
                 packet.setLength(buf.length);
             }
         } catch (IOException e) {
             System.err.println("Died in main receive/send loop.");
             e.printStackTrace(System.err);
         } finally {
             sock.close();
             producer.close();
         }
     }
 
 
     /**
      * Load configuration values.
      */
     private static void initConfig() {
         try {
             topic = props.getProperty("topic");
             udpGroup = props.getProperty("udp.group");
             udpPort = Integer.parseInt(props.getProperty("udp.port"));
             udpWorkerId = Integer.parseInt(props.getProperty("udp.workerId"));
             udpNumWorkers = Integer.parseInt(props.getProperty("udp.numWorkers"));
         } catch (RuntimeException e) {
             System.err.println("Invalid configuration.%n");
             e.printStackTrace(System.err);
             System.exit(1);
         }
     }
 
 
     /**
      * @param msg Udplog log line.
      * @return Whether log line should be handled by this worker.
      */
     private static boolean shouldHandle(String msg) {
         long seqId = getSeqId(msg);
         return seqId != -1 && consistentHash(seqId, udpNumWorkers) == udpWorkerId;
     }
 
 
     /**
      * Extract a long sequence ID from a udplog log line.
      * The sequence ID is assumed to be a long integer in the second
      * field of a set of space-separated fields: blah 123 blah blah ..
      *
      * @param msg Udplog log line.
      * @return Sequence ID or -1 if not found.
      */
     private static long getSeqId(String msg) {
         int start = msg.indexOf(' ');
         if (start == -1) {
             return -1;
         }
 
         int end = msg.indexOf(' ', start + 1);
         if (end == -1) {
             return -1;
         }
 
         try {
             return Long.parseLong(msg.substring(start + 1, end));
         } catch (NumberFormatException e) {
             return -1;
         }
     }
 
 
     /**
      * Assigns to input a bucket in the range [0, buckets].
      * Adapted from Guava; Copyright (C) 2011 The Guava Authors.
      * Licensed under the Apache License, version 2.0.
      *
      * @return Bucket assignment.
      */
     private static int consistentHash(long input, int buckets) {
         long h = input;
         int candidate = 0;
         int next;
 
         while (true) {
             h = 2862933555777941757L * h + 1;
             double inv = 0x1.0p31 / ((int) (h >>> 33) + 1);
             next = (int) ((candidate + 1) * inv);
 
             if (next >= 0 && next < buckets) {
                 candidate = next;
             } else {
                 return candidate;
             }
         }
     }
 }
 
