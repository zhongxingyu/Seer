 package pl.softmil.simpeweb.containers;
 
 import java.io.IOException;
 
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.hamcrest.Matchers;
import org.junit.*;
 
import pl.softmil.simpeweb.containers.LogStub.LogLevel;
 import pl.softmil.simpeweb.interceptor.response.SetHeadersInterceptor;
 import pl.softmil.simpeweb.junit.StartStopSimpleWebServerRule;
 
 public class DumbRequestResponseHeadersContainerTest {
     private CompositeContainer dumbRequestResponseHeadersContainer = new CompositeContainer()
             .withContainer(new DumbRequestResponseHeadersContainer())
             .withResponseInterceptor(new SetHeadersInterceptor("aaa", "bbb"))
             .withContainer(new FlushContainer());
     @Rule
     public StartStopSimpleWebServerRule startStopSimpleWebServerRule = new StartStopSimpleWebServerRule(
             32331, dumbRequestResponseHeadersContainer);
 
     @Rule
     public LogStub logStub = new LogStub() {
         {
             record(LogLevel.INFO);
             recordLoggingForType(DumbRequestResponseHeadersContainer.class);
         }
     };
 
    @Test(timeout = 1000)
     public void requestResponseHeadersDumped() throws IOException {
         HttpClient httpclient = new DefaultHttpClient();
         httpclient.execute(new HttpGet("http://localhost:32331"));
 
         logStub.assertLoggingEntryMatches(Matchers.containsString("Connection"));
         logStub.assertLoggingEntryMatches(Matchers.containsString("aaa"));
         logStub.assertLoggingEntryMatches(Matchers.containsString("bbb"));
     }
 
 }
