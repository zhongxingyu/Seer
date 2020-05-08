package test.java.talkative.resource;
 
 
import main.java.talkatives.resource.GreetingService;

 import org.apache.cxf.jaxrs.client.WebClient;
 import org.apache.openejb.jee.SingletonBean;
 import org.apache.openejb.junit.ApplicationComposer;
 import org.apache.openejb.testing.EnableServices;
 import org.apache.openejb.testing.Module;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import java.io.IOException;
 
 import static org.junit.Assert.assertEquals;
 
 @EnableServices(value = "jaxrs")
 @RunWith(ApplicationComposer.class)
 public class GreetingServiceTest {
     @Module
     public SingletonBean app() {
         return (SingletonBean) new SingletonBean(GreetingService.class).localBean();
     }
 
     @Test
     public void get() throws IOException {
         final String message = WebClient.create("http://localhost:4204").path("/GreetingServiceTest/greeting/").get(String.class);
         assertEquals("Hi REST!!!", message);
     }
 
     @Test
     public void post() throws IOException {
         final String message = WebClient.create("http://localhost:4204").path("/GreetingServiceTest/greeting/").post("Hi REST!", String.class);
         assertEquals("hi rest!!!", message);
     }
 }
