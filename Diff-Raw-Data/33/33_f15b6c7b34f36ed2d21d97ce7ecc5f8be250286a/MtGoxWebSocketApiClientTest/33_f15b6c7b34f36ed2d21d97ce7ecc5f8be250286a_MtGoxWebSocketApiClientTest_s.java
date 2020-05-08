 package to.sparks.mtgox.net;
 
import to.sparks.mtgox.service.MtGoxHTTPClient;
import to.sparks.mtgox.service.MtGoxWebSocketClient;
 import java.util.ArrayList;
 import java.util.Currency;
 import java.util.List;
 import java.util.logging.Logger;
 import junit.framework.TestCase;
 import org.springframework.core.task.TaskExecutor;
 import to.sparks.mtgox.model.Depth;
 import to.sparks.mtgox.model.Ticker;
 
 /**
  *
  * @author SparksG
  */
 public class MtGoxWebSocketApiClientTest extends TestCase {
 
     public MtGoxWebSocketApiClientTest(String testName) {
         super(testName);
     }
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
     }
 
     @Override
     protected void tearDown() throws Exception {
         super.tearDown();
     }
 
     /**
      * Test of getAllDepthSince method, of class MtGoxWebSocketApiClient.
      */
     public void testGetAllDepthSince() {
         System.out.println("getAllDepthSince");
 
        long timestamp = 0L;
         TestHarness th = new TestHarness();
         MtGoxWebSocketClient instance = new MtGoxWebSocketClient(th.getLogger(), th.getTaskExecutor());
        List<Depth> expResult = new ArrayList<>();
        List<Depth> result = instance.getAllDepthSince(timestamp);
        assertEquals(expResult, result);

     }
 
     /**
      * Test of tickerEvent method, of class MtGoxWebSocketApiClient.
      */
     public void testTickerEvent() {
         System.out.println("tickerEvent");
 
         TestHarness th = new TestHarness();
         MtGoxWebSocketClient instance = new MtGoxWebSocketClient(th.getLogger(), th.getTaskExecutor());
         instance.tickerEvent(th.getTicker());
         List<Ticker> result = instance.getTickerHistory();
         assertTrue(result.size() == 1);
         assertEquals(th.getTicker(), result.get(0));
     }
 
     /**
      * Test of depthEvent method, of class MtGoxWebSocketApiClient.
      */
     public void testDepthEvent() {
         System.out.println("depthEvent");
         TestHarness th = new TestHarness();
         Depth depth = null;
         MtGoxWebSocketClient instance = new MtGoxWebSocketClient(th.getLogger(), th.getTaskExecutor());
         instance.depthEvent(depth);
         List<Depth> result = instance.getDepthHistory();
         assertTrue(result.size() == 1);
         assertEquals(depth, result.get(0));
     }
 
     /**
      * Test of getDepthHistory method, of class MtGoxWebSocketApiClient.
      */
     public void testGetDepthHistory() {
         System.out.println("getDepthHistory");
         TestHarness th = new TestHarness();
         MtGoxWebSocketClient instance = new MtGoxWebSocketClient(th.getLogger(), th.getTaskExecutor());
         List<Depth> expResult = new ArrayList<>();
         List<Depth> result = instance.getDepthHistory();
         assertEquals(expResult, result);
     }
 
     /**
      * Test of getTickerHistory method, of class MtGoxWebSocketApiClient.
      */
     public void testGetTickerHistory() {
         System.out.println("getTickerHistory");
         TestHarness th = new TestHarness();
         MtGoxWebSocketClient instance = new MtGoxWebSocketClient(th.getLogger(), th.getTaskExecutor());
         List<Ticker> expResult = new ArrayList<>();
         List<Ticker> result = instance.getTickerHistory();
         assertEquals(expResult, result);
     }
 
     class TestHarness {
 
         private Ticker ticker = null;
         private Logger logger = null;
         private TaskExecutor taskExecutor = null;
         private MtGoxHTTPClient mtGoxHTTPApi = null;
         private Currency currency = null;
 
         public TestHarness() {
             ticker = new Ticker(null, null, null, null, null, null, null, null, null, null, null);
             logger = Logger.getGlobal();
             mtGoxHTTPApi = new MtGoxHTTPClient(logger, null, null);
             currency = Currency.getInstance("USD");
         }
 
         public TestHarness(Ticker ticker, Logger logger, TaskExecutor taskExecutor, MtGoxHTTPClient mtGoxHTTPApi, Currency currency) {
             this.ticker = ticker;
             this.logger = logger;
             this.taskExecutor = taskExecutor;
             this.mtGoxHTTPApi = mtGoxHTTPApi;
             this.currency = currency;
         }
 
         public Ticker getTicker() {
             return ticker;
         }
 
         public Logger getLogger() {
             return logger;
         }
 
         public TaskExecutor getTaskExecutor() {
             return taskExecutor;
         }
 
         public MtGoxHTTPClient getMtGoxHTTPApi() {
             return mtGoxHTTPApi;
         }
 
         public Currency getCurrency() {
             return currency;
         }
     }
 }
