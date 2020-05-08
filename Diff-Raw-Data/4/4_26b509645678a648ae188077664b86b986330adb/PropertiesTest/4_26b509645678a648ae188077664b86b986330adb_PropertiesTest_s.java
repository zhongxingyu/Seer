 package net.contextfw.web.application.properties;
 
 import static net.contextfw.web.application.properties.Properties.ATTRIBUTE_JSON_SERIALIZER;
 import static net.contextfw.web.application.properties.Properties.DEVELOPMENT_MODE;
 import static net.contextfw.web.application.properties.Properties.LIFECYCLE_LISTENER;
 import static net.contextfw.web.application.properties.Properties.NAMESPACE;
 import static net.contextfw.web.application.properties.Properties.PROPERTY_PROVIDER;
 import static net.contextfw.web.application.properties.Properties.REMOVAL_SCHEDULE_PERIOD;
 import static net.contextfw.web.application.properties.Properties.RESOURCE_PATH;
 import static net.contextfw.web.application.properties.Properties.TRANSFORMER_COUNT;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.util.Date;
 
 import net.contextfw.web.application.SystemPropertyProvider;
 import net.contextfw.web.application.lifecycle.DefaultLifecycleListener;
 
 import org.junit.Test;
 
 public class PropertiesTest {
 
     @Test
     public void test() {
         
         Properties props = Properties.getDefaults()
           .add(RESOURCE_PATH, "net.contextfw.web")
           .add(RESOURCE_PATH, "templates.path")
           .add(NAMESPACE.as("foo", "bar"))
           .add(ATTRIBUTE_JSON_SERIALIZER.as(Date.class, DateSerializer.class));
         
         assertTrue(props.get(DEVELOPMENT_MODE));
         assertEquals(1, props.get(TRANSFORMER_COUNT).intValue());
         
         assertEquals(2, props.get(RESOURCE_PATH).size());
         assertTrue(props.get(RESOURCE_PATH).contains("net.contextfw.web"));
         assertTrue(props.get(RESOURCE_PATH).contains("templates.path"));
        assertEquals(SystemPropertyProvider.class, props.get(PROPERTY_PROVIDER));
         assertEquals(DefaultLifecycleListener.class, props.get(LIFECYCLE_LISTENER));
         //assertEquals(((1*60 + 30) * 1000), props.get(ERROR_TIME).longValue());
         //assertEquals((70 * 1000), props.get(POLL_TIME).longValue());
         assertEquals((60000), props.get(REMOVAL_SCHEDULE_PERIOD).longValue());
         
         assertEquals(1, props.get(NAMESPACE).size());
         
         assertEquals("foo", props.get(NAMESPACE).iterator().next().getKey());
         assertEquals("bar", props.get(NAMESPACE).iterator().next().getValue());
         
         assertEquals(1, props.get(ATTRIBUTE_JSON_SERIALIZER).size());
         
         assertEquals(Date.class, props.get(ATTRIBUTE_JSON_SERIALIZER).iterator().next().getKey());
         assertEquals(DateSerializer.class, props.get(ATTRIBUTE_JSON_SERIALIZER).iterator().next().getValue());
     }
 }
