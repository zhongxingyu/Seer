 package org.switchyard.quickstarts.helloworld.ftp.action;
 
 import static org.hamcrest.Matchers.*;
 import static org.junit.Assert.*;
 
 import java.io.File;
 import java.util.Date;
 import java.util.Queue;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.time.DateFormatUtils;
 import org.apache.ftpserver.FtpServer;
 import org.apache.ftpserver.FtpServerFactory;
 import org.apache.ftpserver.filesystem.nativefs.NativeFileSystemFactory;
 import org.apache.ftpserver.ftplet.FtpException;
 import org.apache.ftpserver.listener.ListenerFactory;
 import org.apache.ftpserver.usermanager.ClearTextPasswordEncryptor;
 import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.switchyard.Exchange;
 import org.switchyard.component.camel.model.RouteScanner;
 import org.switchyard.test.MockHandler;
 import org.switchyard.test.SwitchYardRunner;
 import org.switchyard.test.SwitchYardTestCaseConfig;
 import org.switchyard.test.SwitchYardTestKit;
 
 @RunWith(SwitchYardRunner.class)
@SwitchYardTestCaseConfig(config = SwitchYardTestCaseConfig.SWITCHYARD_XML, scanners = RouteScanner.class)
 public class HelloWorldFtpActionTest {
     
     private static final int FTP_PORT = 2222;
     private static final String FTP_USERS_PROPERTIES = "src/test/resources/users.properties";
     private static final String FTP_DIRECTORY = "target/ftp";
     private static final String FILE_NAME = String.format("HWFtp-%s.dat",
             DateFormatUtils.format(new Date(), "dd-MMM-yy-HH-mm-ss"));
     
     private static final String SERVICE = "myFileListener";
     private static final String PAYLOAD = "Hello World In A File (for Hello World FTP Action test)";
     private static final String COMPLETE_FILE = FTP_DIRECTORY + "/.COMPLETE/" + FILE_NAME;
     
     private static FtpServer _ftpServer;
     
     private SwitchYardTestKit _testKit;
     
     @BeforeClass
     public static void startUp() throws FtpException {
         FileUtils.deleteQuietly(new File(FTP_DIRECTORY));
         _ftpServer = createFtpServer();
         _ftpServer.start();
     }
     
     private static FtpServer createFtpServer() {
         FtpServerFactory serverFactory = new FtpServerFactory();
         ListenerFactory listenerFactory = new ListenerFactory();
         listenerFactory.setPort(FTP_PORT);
         serverFactory.addListener("default", listenerFactory.createListener());
         
         PropertiesUserManagerFactory managerFactory = new PropertiesUserManagerFactory();
         managerFactory.setPasswordEncryptor(new ClearTextPasswordEncryptor());
         managerFactory.setFile(new File(FTP_USERS_PROPERTIES));
         serverFactory.setUserManager(managerFactory.createUserManager());
         
         NativeFileSystemFactory fileSystemFactory = new NativeFileSystemFactory();
         fileSystemFactory.setCreateHome(true);
         serverFactory.setFileSystem(fileSystemFactory);
         
         return serverFactory.createServer();
     }
     
     @AfterClass
     public static void shutDown() {
         _ftpServer.stop();
     }
     
     @Test
     public void receiveFile() throws Exception {
         // replace existing implementation for testing purposes
         _testKit.removeService(SERVICE);
         final MockHandler mockService = _testKit.registerInOnlyService(SERVICE);
         
         FileUtils.write(new File(FTP_DIRECTORY, FILE_NAME), PAYLOAD);
         Thread.sleep(500);
         
         final Queue<Exchange> recievedMessages = mockService.getMessages();
         assertThat(recievedMessages, is(notNullValue()));
         final Exchange recievedExchange = recievedMessages.iterator().next();
         assertThat(recievedExchange.getMessage().getContent(String.class), is(equalTo(PAYLOAD)));
     }
     
     @Test
     public void runTest() throws Exception {
         FileUtils.write(new File(FTP_DIRECTORY, FILE_NAME), PAYLOAD);
         Thread.sleep(500);
         
         File completeFile = new File(COMPLETE_FILE);
         assertThat(completeFile.exists(), is(true));
         assertThat(FileUtils.readFileToString(completeFile), is(equalTo(PAYLOAD)));
     }
     
 }
