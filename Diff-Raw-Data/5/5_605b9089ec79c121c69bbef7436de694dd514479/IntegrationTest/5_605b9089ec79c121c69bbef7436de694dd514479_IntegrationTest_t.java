 package me.ilyamirin.anthophila;
 
 import com.google.gson.Gson;
 import lombok.extern.slf4j.Slf4j;
 import me.ilyamirin.anthophila.client.OneNodeClient;
 import me.ilyamirin.anthophila.common.Topology;
 import me.ilyamirin.anthophila.server.Server;
 import me.ilyamirin.anthophila.server.ServerParams;
 import me.ilyamirin.anthophila.server.ServerStorage;
 import org.junit.Ignore;
 import org.junit.Test;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.nio.ByteBuffer;
 import java.nio.channels.SocketChannel;
 import java.util.Arrays;
 import java.util.Random;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 /**
  * @author ilyamirin
  */
 @Slf4j
 public class IntegrationTest {
 
     private Random r = new Random();
 
     @Test
     public void simpleTest() throws IOException, InterruptedException {
        File file = new File("test.bin");
         if (file.exists())
             file.delete();
         file.createNewFile();
 
         String host = "127.0.0.1";
         int port = 7621;
 
         ServerParams serverParams = new ServerParams();
        serverParams.setStorageFile("test.bin");
 
         serverParams.setInitialIndexSize(5000);
 
         serverParams.setHost(host);
         serverParams.setPort(port);
 
         serverParams.setEncrypt(true);
         serverParams.setNewKeysFile("new.keys");
         serverParams.setOldKeysFile("old.keys");
 
         serverParams.setMaxConnections(10);
 
         serverParams.setServeAll(true);
 
         FileWriter writer = new FileWriter("server.json");
         new Gson().toJson(serverParams, ServerParams.class, writer);
         writer.close();
 
         Server.main();
         Thread.sleep(1000);
 
         final OneNodeClient client = OneNodeClient.newClient(host, port);
 
         assertTrue(client.isConnected());
 
         final int clientsNumber = 10;
         final int requestsNumber = 1000;
         final AtomicInteger errorsCounter = new AtomicInteger(0);
         final AtomicInteger requestsPassed = new AtomicInteger(0);
         final AtomicInteger chunksStored = new AtomicInteger(0);
         final CountDownLatch latch = new CountDownLatch(clientsNumber);
 
         long start = System.currentTimeMillis();
 
         for (int i = 0; i < clientsNumber; i++) {
             new Thread() {
                 @Override
                 public void run() {
                     ByteBuffer key = ByteBuffer.allocate(ServerStorage.MD5_HASH_LENGTH);
                     ByteBuffer chunk = ByteBuffer.allocate(ServerStorage.CHUNK_LENGTH);
 
                     for (int j = 0; j < requestsNumber; j++) {
                         r.nextBytes(key.array());
                         r.nextBytes(chunk.array());
 
                         try {
                             client.push(key, chunk);
                             if (!Arrays.equals(chunk.array(), client.pull(key).array()))
                                 log.error("Returned result is incorrect.");
                         } catch (IOException ex) {
                             log.error("Oops!", ex);
                             errorsCounter.incrementAndGet();
                         }
 
                         if (r.nextBoolean()) {
                             try {
                                 if (!client.remove(key))
                                     throw new IOException("OneNodeClient couldn`t remove chunk.");
                                 if (client.pull(key) != null)
                                     throw new IOException("OneNodeClient returned previously removed chunk.");
                             } catch (IOException exception) {
                                 errorsCounter.incrementAndGet();
                                 log.error("Oops!", exception);
                             }
                         } else {
                             chunksStored.incrementAndGet();
                         }
 
                         if (requestsPassed.incrementAndGet() % 1000 == 0)
                             log.info("{} pull/push/?remove/pull request quads passed.", requestsPassed.get());
 
                     }//for
 
                     log.info("one of clients has just finished.");
                     latch.countDown();
 
                 }//run
             }.start();
 
         }//while
 
         latch.await();
 
         assertEquals(0, errorsCounter.get());
         assertTrue(chunksStored.get() * ServerStorage.WHOLE_CHUNK_WITH_META_LENGTH <= file.length());
 
         log.info("Test was passed for {} seconds.", (System.currentTimeMillis() - start) / 1000);
 
         client.close();
     }//simpleTest
 
     @Ignore
     @Test
     public void clusterTest() throws IOException, InterruptedException {
         File file = new File("test.bin");
         if (file.exists())
             file.delete();
         file.createNewFile();
 
         String host = "127.0.0.1";
         int port = 7620;
 
         Topology topology = new Topology();
 
         for (int i = 0; i < 2; i++) {
             ServerParams serverParams = new ServerParams();
             serverParams.setStorageFile(String.format("test%s.bin", i));
 
             serverParams.setInitialIndexSize(5000);
 
             serverParams.setHost(host);
             serverParams.setPort(port + i);
 
             serverParams.setEncrypt(true);
 
             serverParams.setServeAll(true);
             serverParams.setNewKeysFile("new.keys");
             serverParams.setOldKeysFile("old.keys");
 
             serverParams.setMaxConnections(10);
 
             FileWriter writer = new FileWriter("server.json");
             new Gson().toJson(serverParams, ServerParams.class, writer);
             writer.close();
 
             Server.main();
 
             Thread.sleep(1000);
 
             OneNodeClient client = OneNodeClient.newClient(host, port + i);
             assertTrue(client.isConnected());
             client.close();
         }
 
         /*
         final OneNodeClient client = OneNodeClient.newClient(host, port);
 
         assertTrue(client.isConnected());
 
         final int clientsNumber = 10;
         final int requestsNumber = 1000;
         final AtomicInteger errorsCounter = new AtomicInteger(0);
         final AtomicInteger requestsPassed = new AtomicInteger(0);
         final AtomicInteger chunksStored = new AtomicInteger(0);
         final CountDownLatch latch = new CountDownLatch(clientsNumber);
 
         long start = System.currentTimeMillis();
 
         for (int i = 0; i < clientsNumber; i++) {
             new Thread() {
                 @Override
                 public void run() {
                     ByteBuffer key = ByteBuffer.allocate(ServerStorage.MD5_HASH_LENGTH);
                     ByteBuffer chunk = ByteBuffer.allocate(ServerStorage.CHUNK_LENGTH);
 
                     for (int j = 0; j < requestsNumber; j++) {
                         r.nextBytes(key.array());
                         r.nextBytes(chunk.array());
 
                         try {
                             client.push(key, chunk);
                             if (!Arrays.equals(chunk.array(), client.pull(key).array()))
                                 log.error("Returned result is incorrect.");
                         } catch (IOException ex) {
                             log.error("Oops!", ex);
                             errorsCounter.incrementAndGet();
                         }
 
                         if (r.nextBoolean()) {
                             try {
                                 if (!client.remove(key))
                                     throw new IOException("OneNodeClient couldn`t remove chunk.");
                                 if (client.pull(key) != null)
                                     throw new IOException("OneNodeClient returned previously removed chunk.");
                             } catch (IOException exception) {
                                 errorsCounter.incrementAndGet();
                                 log.error("Oops!", exception);
                             }
                         } else {
                             chunksStored.incrementAndGet();
                         }
 
                         if (requestsPassed.incrementAndGet() % 1000 == 0)
                             log.info("{} pull/push/?remove/pull request quads passed.", requestsPassed.get());
 
                     }//for
 
                     log.info("one of clients has just finished.");
                     latch.countDown();
 
                 }//run
             }.start();
 
         }//while
 
         latch.await();
 
         assertEquals(0, errorsCounter.get());
         assertTrue(chunksStored.get() * ServerStorage.WHOLE_CHUNK_WITH_META_LENGTH <= file.length());
 
         log.info("Test was passed for {} seconds.", (System.currentTimeMillis() - start) / 1000);
 
         client.close();
         */
     }//clusterTest
 
 }
