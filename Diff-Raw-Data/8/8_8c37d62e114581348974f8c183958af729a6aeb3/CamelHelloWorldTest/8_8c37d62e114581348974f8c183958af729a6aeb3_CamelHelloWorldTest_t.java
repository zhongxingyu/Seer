 package org.switchyard.quickstarts.camel.helloworld;
 
 import static org.hamcrest.Matchers.*;
 import static org.junit.Assert.*;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import org.apache.commons.io.FileUtils;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.switchyard.Exchange;
 import org.switchyard.test.MockHandler;
 import org.switchyard.test.SwitchYardRunner;
 import org.switchyard.test.SwitchYardTestCaseConfig;
 import org.switchyard.test.SwitchYardTestKit;
 
 @RunWith(SwitchYardRunner.class)
 @SwitchYardTestCaseConfig(config = SwitchYardTestCaseConfig.SWITCHYARD_XML)
 public class CamelHelloWorldTest {
     
     private static final String SERVICE = "service1";
     private static final String INPUT1_DIR = "target/input1";
     private static final String INPUT2_DIR = "target/input2";
     private static final String FILE_NAME = "test.txt";
     private static final String FILE_CONTENTS1 = String.format("Hello %s! (1)", System.getProperty("user.name"));
     private static final String FILE_CONTENTS2 = String.format("Hello %s! (2)", System.getProperty("user.name"));
     
     private SwitchYardTestKit _testKit;
     
     @Before
     public void setUp() {
         new File(INPUT1_DIR, FILE_NAME).delete();
         new File(INPUT2_DIR, FILE_NAME).delete();
     }
     
     @Test
     public void receiveFile() throws Exception {
         _testKit.removeService(SERVICE);
         MockHandler service = _testKit.registerInOnlyService(SERVICE);
         
         FileUtils.writeStringToFile(new File(INPUT1_DIR, FILE_NAME), FILE_CONTENTS1);
         FileUtils.writeStringToFile(new File(INPUT2_DIR, FILE_NAME), FILE_CONTENTS2);
         
         service.waitForOKMessage();
         LinkedBlockingQueue<Exchange> receivedMessages = service.getMessages();
         assertThat(receivedMessages, is(notNullValue()));
         List<String> receivedContents = new ArrayList<String>();
         for (Exchange receivedExchange : receivedMessages) {
             receivedContents.add(receivedExchange.getMessage().getContent(String.class));
         }
         assertThat(receivedContents.size(), is(equalTo(2)));
         assertThat(receivedContents, hasItems(FILE_CONTENTS1, FILE_CONTENTS2));
     }
     
     @Test
     public void runTest() throws Exception {
         File inputFile1 = new File(INPUT1_DIR, FILE_NAME);
         File inputFile2 = new File(INPUT2_DIR, FILE_NAME);
         FileUtils.writeStringToFile(inputFile1, FILE_CONTENTS1);
         FileUtils.writeStringToFile(inputFile2, FILE_CONTENTS2);
        Thread.sleep(500);
         assertThat(inputFile1.exists(), is(false));
         assertThat(inputFile2.exists(), is(false));
     }
     
}
