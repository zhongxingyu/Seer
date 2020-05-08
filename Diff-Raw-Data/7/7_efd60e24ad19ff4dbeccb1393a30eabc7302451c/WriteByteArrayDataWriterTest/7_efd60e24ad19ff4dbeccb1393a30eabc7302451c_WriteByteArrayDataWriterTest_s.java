 package ping.pong.net.connection.io;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.UnknownHostException;
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
 public class WriteByteArrayDataWriterTest
 {
     private static final Logger LOGGER = LoggerFactory.getLogger(WriteByteArrayDataWriterTest.class);
 
     public WriteByteArrayDataWriterTest()
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
 
     @Test
     public void testWriteByteArrayDataWriter() throws InterruptedException
     {
         Runnable serverSocket = new Runnable()
         {
             @Override
             public void run()
             {
                 try
                 {
                     ServerSocket serverSocket = ServerSocketFactory.getDefault().createServerSocket(10122);
                     Socket acceptingSocket = serverSocket.accept();
                     WriteByteArrayDataWriter writeByteArrayDataWriter = new WriteByteArrayDataWriter();
                     writeByteArrayDataWriter.init(acceptingSocket);
 
 
                     for (int i = 0; i < 10; i++)
                     {
                         String messageString = "Test" + i;
                         writeByteArrayDataWriter.writeData(messageString.getBytes());
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
                     readFullyDataReader.init(SocketFactory.getDefault().createSocket("localhost", 10122));
                     int i = 0;
                    while (running)
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
 
        serverThread.join(100);
        readerThread.join(150);
 
         int i = 0;
         for (int j = 0; j < results.length; j++, i++)
         {
             String result = results[j];
             String expected = "Test" + i;
             assertEquals(expected, result);
         }
     }
 }
