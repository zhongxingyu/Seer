 import com.blogspot.ilialapitan.CamelService;
 import com.blogspot.ilialapitan.CamelServiceJavaDSL;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import java.io.File;
 import java.util.concurrent.TimeUnit;
 
 /**
  * Test integration with Amazon S3
  * with Apache Camel help.
  *
  * @author Ilya Lapitan
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"classpath:com/blogspot/ilialapitan/spring-camel-context.xml"})
 public class CamelServiceTest {
 
     @Autowired
     CamelService camelService;
 
     //Test service provided java configuration.
     @Test
     public void testServiceJava() throws Exception {
        String KEY =   "[key]";
 
         CamelService camelService = new CamelServiceJavaDSL();
 
         File fileOut = new File("[input_file]");
         File fileIn = new File("[output_file]");
 
         camelService.send(KEY, fileOut);
 
         Thread.sleep(TimeUnit.SECONDS.toMillis(3));
 
         camelService.receive(KEY, fileIn);
 
 
     }
 
     //Test service provided Spring configuration.
     @Test
     public void testServiceSpring() throws Exception {
        String KEY =   "[key]";
 
         File fileOut = new File("[input_file]");
         File fileIn = new File("[output_file]");
 
         camelService.send(KEY, fileOut);
 
         Thread.sleep(TimeUnit.SECONDS.toMillis(3));
 
         camelService.receive(KEY, fileIn);
 
     }
 }
