 package http.server.logger;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import java.io.*;
 import static junit.framework.Assert.assertEquals;
 
 public class ConsoleLoggerTest {
   private File consoleFile;
 
   @Before
   public void setUp() throws IOException {
     String workingDirectory = System.getProperty("user.dir");
     consoleFile = new File(workingDirectory + "/console.txt");
   }
 
   @After
   public void tearDown() {
     consoleFile.delete();
   }
 
   @Test
   public void printMultipleMessagesToConsole() throws IOException {
     Logger logger = new ConsoleLogger();
    System.setOut(new PrintStream(consoleFile));
     logger.logMessage("This message should be printed to the console.");
     logger.logMessage("This message should also be printed to the console.");
 
     String expectedResult = "[" + logger.currentDateAndTime() + "] This message should be printed to the console.\n"
         + "[" + logger.currentDateAndTime() + "] This message should also be printed to the console.\n";
 
     assertEquals(expectedResult, readServerLog(consoleFile));
   }
 
   private String readServerLog(File file) throws IOException {
     InputStream inputStream = new FileInputStream(file);
     ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
     int chr;
 
     while ((chr = inputStream.read()) != -1)
       outputStream.write(chr);
 
     return outputStream.toString();
   }
 }
