 package com.cibot.config;
 
 import com.cibot.cimodel.BuildStatus;
 import com.google.common.collect.Lists;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.net.URL;
 
 import static org.junit.Assert.assertEquals;
 
 /**
  * User: Uefix
  * Date: 25.09.12
  * Time: 20:49
  */
 public class CIBotConfigurationLoaderTest {
 
 
     public static final String TEST_CONFIG_FILE = "test-config.xml";
 
 
     private CIBotConfigurationLoader loader = new CIBotConfigurationLoader();
 
 
     @Before
     public void setup() {
         System.setProperty(CIBotConfigurationLoader.CONFIG_FILENAME_SYSPROPERTY, TEST_CONFIG_FILE);
     }
 
 
     @Test
     public void loadConfiguration_testConfig_returnsExpectedResult() throws Exception {
         CIBotConfiguration result = loader.loadConfiguration();
 
         CIBotConfiguration expected = new CIBotConfiguration();
         expected.getThumbi().setConnectionUri("usb://NXT_A");
         expected.getThumbi().setTimeout(10000L);
         expected.getThumbi().setEnabled(false);
/*
         expected.getLabelProperties().setFontSize(24);
         expected.getLabelProperties().setWidth(100);
         expected.getLabelProperties().setHeight(32);
 */
         CIBotConfiguration.FeedReader expectedFeedReader = expected.getFeedReader();
         expectedFeedReader.setLoginsList(Lists.newArrayList(new CIBotConfiguration.Login("jenkins", "develop", "pw123")));
 
         CIBotConfiguration.Feed namedFeed = new CIBotConfiguration.Feed(new URL("http://JENKINS_HOST:8080/jenkins/job/NAME_OF_THE_JOB1/rssAll"), null);
         namedFeed.setJobName("theJob");
         expectedFeedReader.getFeeds().add(namedFeed);
         expectedFeedReader.getFeeds().add(new CIBotConfiguration.Feed(new URL("http://JENKINS_HOST:8080/jenkins/job/NAME_OF_THE_JOB2/rssAll"), "jenkins"));
 
         expectedFeedReader.getStatusMappings().putAll(
                 BuildStatus.BUILD_OK, Lists.newArrayList("stabil", "back to normal"));
 
         expectedFeedReader.getStatusMappings().putAll(
                 BuildStatus.BUILD_UNSTABLE, Lists.newArrayList("tests failures"));
 
         assertEquals(expected, result);
     }
}
