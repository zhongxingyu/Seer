 package ping.pong.net.connection.io;
 
 import java.io.BufferedOutputStream;
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.nio.ByteBuffer;
 
 import javax.net.ServerSocketFactory;
 import javax.net.SocketFactory;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author mfullen
  */
 public class ReadFullyDataReaderTest
 {
     private static final Logger LOGGER = LoggerFactory.getLogger(ReadFullyDataReaderTest.class);
 
     public ReadFullyDataReaderTest()
     {
     }
 
     @BeforeClass
     public static void setUpClass() throws Exception
     {
     }
 
     @AfterClass
     public static void tearDownClass() throws Exception
     {
     }
 
     /**
      * Test of init method, of class ReadFullyDataReader.
      */
     @Test
     public void testReadFullyDataReader() throws IOException,
                                                  InterruptedException
     {
         Runnable serverSocket = new Runnable()
         {
             @Override
             public void run()
             {
                 try
                 {
                     ServerSocket serverSocket = ServerSocketFactory.getDefault().createServerSocket(10121);
                     Socket acceptingSocket = serverSocket.accept();
                     BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(acceptingSocket.getOutputStream());
 
                     for (int i = 0; i < 10; i++)
                     {
                         String messageString = "Test" + i;
                         LOGGER.debug("Message byte[] size: " + messageString.getBytes().length);
                         byte[] size = ByteBuffer.allocate(4).putInt(messageString.getBytes().length).array();
                         byte[] message = messageString.getBytes();
                         bufferedOutputStream.write(size);
                         bufferedOutputStream.write(message);
                         bufferedOutputStream.flush();
                     }
                 }
                 catch (IOException ex)
                 {
                     LOGGER.error("Error", ex);
                 }
             }
         };
         Thread serverThread = new Thread(serverSocket);
         serverThread.start();
         final String[] results = new String[10];
         Runnable reader = new Runnable()
         {
             boolean running = true;
 
             @Override
             public void run()
             {
                 try
                 {
                     ReadFullyDataReader readFullyDataReader = new ReadFullyDataReader();
                     readFullyDataReader.init(SocketFactory.getDefault().createSocket("localhost", 10121));
                     int i = 0;
                    while (i <= 9)
                     {
                         byte[] readData = readFullyDataReader.readData();
                         LOGGER.debug("ReadData: " + readData);
                         results[i] = new String(readData);
                         i++;
                     }
                 }
                 catch (UnknownHostException ex)
                 {
                     LOGGER.error("Error", ex);
                 }
                 catch (IOException ex)
                 {
                     LOGGER.error("Error", ex);
                 }
             }
         };
         Thread readerThread = new Thread(reader);
         readerThread.start();
 
        serverThread.join();
        readerThread.join();
 
 
         int i = 0;
         for (int j = 0; j < results.length; j++, i++)
         {
             String result = results[j];
             String expected = "Test" + i;
             assertEquals(expected, result);
         }
     }
 }
