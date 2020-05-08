 package com.tngtech.internal.telnet;
 
 import com.google.common.collect.Lists;
 import com.tngtech.internal.helpers.IntegrationTest;
 import com.tngtech.internal.plug.Plug;
 import com.tngtech.internal.plug.PlugConfig;
 import com.tngtech.internal.telnet.notifications.NotificationHandler;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.experimental.categories.Category;
 
 import java.util.Collection;
 
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 
 @Category(IntegrationTest.class)
 public class AsynchronousTelnetClientIntegrationTest {
 
     private AsynchronousTelnetClient telnetClient;
 
     @Before
     public void setUp() {
         PlugConfig plugConfig = new PlugConfig("192.168.0.120", 1234, "admin", "admin", Plug.PLUG1.toString());
         telnetClient = new TelnetCreator().getAsynchronousTelnetClient(plugConfig);
     }
 
     @Test
     public void testTelnetClient() throws Exception {
         final Collection<String> textLines = Lists.newArrayList();
 
         telnetClient.addNotificationHandler(new NotificationHandler() {
             public void getNotification(String message) {
                 textLines.add(message);
             }
         });
         telnetClient.connect();
         Thread.sleep(500);
 
         telnetClient.disconnect();
 
         assertThat(textLines.size(), is(1));
         String message = textLines.iterator().next();
         assertTrue(message.startsWith("100 HELLO"));
         assertTrue(message.endsWith("- KSHELL V1.3"));
     }
 }
