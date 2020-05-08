 package bot;
 
 import static org.junit.Assert.*;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class TestConfigHelper {
 
     private ConfigHelper config;

     @Before
     public void setUp() {
        config = new ConfigHelper();
     }

     @Test
    public void testIfPropertiesAreSet() {
        String botName = config.getBotName();
        String serverName = config.getServer();
        String channelName = config.getChannel();
        String identName = config.getIdentName();
        String identifyPassword = config.getIdentifyPassword();
        String webappUrl = config.getWebappUrl();

        assertFalse("".equals(botName));
        assertFalse("".equals(serverName));
        assertFalse("".equals(channelName));
        assertFalse("".equals(identName));
        assertFalse("".equals(identifyPassword));
        assertFalse("".equals(webappUrl));
     }
 }
