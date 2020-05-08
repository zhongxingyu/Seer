 package dk.aau.cs.Redirector;
 
/**
 * Created with IntelliJ IDEA.
 * User: Computer
 * Date: 14-02-13
 * Time: 11:27
 * To change this template use File | Settings | File Templates.
 */
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.robolectric.RobolectricTestRunner;
 
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.junit.Assert.assertThat;
 
 @RunWith(RobolectricTestRunner.class)
 public class RedirectorTest {
 
     @Test
     public void shouldHaveProperAppName() throws Exception{
         String appName = new Redirector().getResources().getString(R.string.app_name);
         assertThat(appName, equalTo("Redirector"));
     }
 }
