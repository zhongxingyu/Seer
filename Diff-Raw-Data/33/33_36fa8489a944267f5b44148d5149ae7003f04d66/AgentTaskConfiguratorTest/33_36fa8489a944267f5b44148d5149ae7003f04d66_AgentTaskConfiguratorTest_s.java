 package org.whitesource.bamboo.plugins;
 
 import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
 import static org.junit.Assert.assertThat;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import junit.framework.TestCase;
 
 import org.junit.Test;
 
 public class AgentTaskConfiguratorTest extends TestCase
 {
     private AgentTaskConfigurator configurator;
 
     @Override
     protected void setUp() throws Exception
     {
         super.setUp();
         configurator = new AgentTaskConfigurator();
     }
 
     @Test
    public void testProjectTokenIsRandomOnCreate()
     {
        final Map<String, Object> firstContext = new HashMap<String, Object>();
        assertTrue(firstContext.isEmpty());
        configurator.populateContextForCreate(firstContext);
        final String firstUUID = (String) firstContext.get(AgentTaskConfigurator.PROJECT_TOKEN);

        final Map<String, Object> secondContext = new HashMap<String, Object>();
        configurator.populateContextForCreate(secondContext);
        final String secondUUID = (String) secondContext.get(AgentTaskConfigurator.PROJECT_TOKEN);
        assertThat(firstUUID, not(equalTo(secondUUID)));
     }
 }
