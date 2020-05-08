 import com.thoughtworks.selenium.HttpCommandProcessor;
import org.junit.After;
import org.junit.Before;
 import org.junit.Test;
 
 public class UserExtensionsTest {
    private SeleniumExtension selenium;
 
    @Before
    public void setUp() {
         HttpCommandProcessor processor = new HttpCommandProcessor("beantn0a000189", 4545, "*firefox", "http://localhost");
         selenium = new SeleniumExtension(processor);
         selenium.start();
     }
 
    @After
    public void tearDown() {
         selenium.stop();
     }
 
     @Test
    public void runSeleneseTestCases() {
         selenium.processSeleneseTestCase("src/test/selenese/testcase/assertEqualsTest.html");
         selenium.processSeleneseTestCase("src/test/selenese/testcase/browserWidthTest.html");
         selenium.processSeleneseTestCase("src/test/selenese/testcase/getNumberOfVerticalScrollStepsTest.html");
     }
 }
