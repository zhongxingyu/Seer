 package cx.jeff.sample.command;
 
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Pattern;
 
 import org.joda.time.DateMidnight;
 import org.junit.Before;
 import org.junit.Test;
 
 import cx.jeff.sample.domain.Client;
 import cx.jeff.sample.domain.Currency;
 import cx.jeff.sample.domain.Money;
 import cx.jeff.sample.domain.Trade;
 import static org.mockito.Mockito.*;
 
 public class ListTradesCommandTest extends CommandTestSuper {
 
 	@Before
 	public void setUp() {
 		command = new ListTradesCommand();
 		super.setUp();
 	}
 
 	@Test
 	public void testExecuteNormal() {
 		
 		Client client = mock(Client.class);
 		when(client.getName()).thenReturn("Test McTest");
 		
 		List<Trade> trades = new ArrayList<Trade>();
 		Trade t = mock(Trade.class);
 		when(t.getId()).thenReturn(1L);
		when(t.getTradeDate()).thenReturn(new DateMidnight(2012, 3, 5).toDate());
		when(t.getMaturityDate()).thenReturn(new DateMidnight(2012, 3, 7).toDate());
 		when(t.getClientSellAmount()).thenReturn(new Money(100000, new Currency("GBP")));
 		when(t.getClientBuyAmount()).thenReturn(new Money(120000, new Currency("EUR")));
 		when(t.getBrokerSellAmount()).thenReturn(new Money(98000, new Currency("GBP")));
 		when(t.getBrokerBuyAmount()).thenReturn(new Money(120000, new Currency("EUR")));
 		when(t.getProfit()).thenReturn(new Money(2000, new Currency("GBP")));
 		trades.add(t);
 		
 		when(client.getTrades()).thenReturn(trades);
 		when(company.findClientById(1L)).thenReturn(client);
 		
 		String[] args = new String[] {"list-trades", "1"};
 		command.execute(args);
 		out.flush();
 		
 		verify(company).startTransaction();
 		verify(company).commitTransaction();
 		
 		String output = arr.toString();
 		
 		Pattern p;
 		
 		p = Pattern.compile("^Trades for Client: Test McTest\n");
 		assertTrue(p.matcher(output).find());
 		
 		p = Pattern.compile("ID\\s+Trade Date\\s+Maturity Date\\s+Client Sell\\s+Client Buy\\s+" +
 				"Broker Sell\\s+Broker Buy\\s+Profit");
 		assertTrue(p.matcher(output).find());
 		
		p = Pattern.compile("1\\s+05/03/2012\\s+07/03/2012\\s+1,000.00 GBP\\s+1,200.00 EUR\\s+980.00 GBP\\s+" +
 				"1,200.00 EUR\\s+20.00 GBP");
		
 		assertTrue(p.matcher(output).find());
 	}
 
 	@Test
 	public void testExecuteNoTrades() {
 		
 		Client client = mock(Client.class);
 		when(client.getName()).thenReturn("Test McTest");
 		
 		when(company.findClientById(1L)).thenReturn(client);
 		
 		
 		String[] args = new String[] {"list-trades", "1"};
 		command.execute(args);
 		out.flush();
 		
 		verify(company).startTransaction();
 		verify(company).commitTransaction();
 		
 		String output = arr.toString();
 		
 		Pattern p = Pattern.compile("^Trades for Client: Test McTest\n");
 		assertTrue(p.matcher(output).find());
 		
 		p = Pattern.compile("No Trades Found");
 		assertTrue(p.matcher(output).find());
 	}
 
 	@Test
 	public void testExecutionTooFewArgs() {
 		
 		String[] args = new String[] {"list-trades"};
 		command.execute(args);
 		out.flush();
 		
 		String output = arr.toString();
 		assertEquals("Usage: list-trades <client id>\n", output);
 	}
 	
 	@Test
 	public void testExecutionWithException() {
 		
 		when(company.findClientById(1L)).thenThrow(new RuntimeException());
 		when(company.isActiveTransaction()).thenReturn(true);
 		
 		
 		String[] args = new String[] {"list-trades", "1"};
 		
 		try {
 			command.execute(args);
 			fail("Expected an exception");
 		} catch (RuntimeException e) {
 			
 			verify(company).startTransaction();
 			verify(company).rollBackTransaction();
 			
 		} finally {			
 			out.flush();
 		}
 	}
 }
