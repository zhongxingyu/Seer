 package org.whitesource.bamboo.plugins;
 
 import static org.hamcrest.CoreMatchers.equalTo;
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
    public void testProjectTokenIsNull()
     {
        final Map<String, Object> context = new HashMap<String, Object>();
        assertTrue(context.isEmpty());
        configurator.populateContextForCreate(context);
        final String token = (String) context.get(AgentTaskConfigurator.PROJECT_TOKEN);
        assertThat(token, equalTo(null));
     }
 }
