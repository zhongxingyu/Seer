 package dk.aau.cs.Redirector;
 
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
