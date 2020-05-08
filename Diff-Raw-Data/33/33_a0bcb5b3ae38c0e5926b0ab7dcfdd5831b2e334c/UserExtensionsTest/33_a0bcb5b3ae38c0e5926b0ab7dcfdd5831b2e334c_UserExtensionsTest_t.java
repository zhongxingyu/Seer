 import com.thoughtworks.selenium.HttpCommandProcessor;
import org.junit.AfterClass;
import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class UserExtensionsTest {
    private static SeleniumExtension selenium;
 
    @BeforeClass
    static public void beforeClass() {
         HttpCommandProcessor processor = new HttpCommandProcessor("beantn0a000189", 4545, "*firefox", "http://localhost");
         selenium = new SeleniumExtension(processor);
         selenium.start();
     }
 

    @AfterClass
    static public void tearDown() {
         selenium.stop();
     }
 
     @Test
    public void assertEqualsTest() {
         selenium.processSeleneseTestCase("src/test/selenese/testcase/assertEqualsTest.html");
    }

    @Test
    public void runBrowserWidthTest() {
         selenium.processSeleneseTestCase("src/test/selenese/testcase/browserWidthTest.html");
    }

    @Test
    public void testVerticalScrollFunctionality() {
         selenium.processSeleneseTestCase("src/test/selenese/testcase/getNumberOfVerticalScrollStepsTest.html");
     }

 }
