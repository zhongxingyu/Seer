package stockwatch;
 
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.only;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Vector;
 
 import junit.framework.TestCase;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import stockwatch.WseMarketTypes.EWseMarketTypes;
 
 public class InternalMarketsTest extends TestCase {
 
     InternalMarkets internalMarkets;
     
     @Before
     public void setUp() throws Exception {
         internalMarkets = new InternalMarkets();
     }
 
     @Test
     public void testGetQuotes() {
         EWseMarketTypes allMarkets[] = EWseMarketTypes.values();
         assertEquals(allMarkets.length, internalMarkets.getQuotes().size());
     }
 
     @Test
     public void testGetStatistics() {
         EWseMarketTypes allMarkets[] = EWseMarketTypes.values();
         assertEquals(allMarkets.length, internalMarkets.getStatistics().size());
     }
 
     @Test
     public void testUpdateMarkets() {
         InternalMarkets mockedIntMarkets = mock(InternalMarkets.class);
         
         Map<String, Vector<Security>> internalMarketsTest = new HashMap<String, Vector<Security>>();
         Vector<Security> securities = new Vector<Security>();
 
         for (int i = 0; i < 10; i++)
             securities.add(new Share());
         
         EWseMarketTypes allMarkets[] = EWseMarketTypes.values();
         for (EWseMarketTypes market : allMarkets) {
             internalMarketsTest.put(market.name(), securities);
         }
         
         when(mockedIntMarkets.getQuotes()).thenReturn(internalMarketsTest);
         
         internalMarkets.updateMarkets(mockedIntMarkets);
         
         verify(mockedIntMarkets, only()).getQuotes();
     }
 
 }
