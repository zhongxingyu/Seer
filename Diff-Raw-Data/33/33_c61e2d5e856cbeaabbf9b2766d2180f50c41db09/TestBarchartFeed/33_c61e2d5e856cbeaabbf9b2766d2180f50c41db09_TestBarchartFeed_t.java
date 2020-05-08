 package com.barchart.feed.ddf.client.provider;
 
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.barchart.feed.api.Agent;
 import com.barchart.feed.api.Feed;
 import com.barchart.feed.api.MarketObserver;
 import com.barchart.feed.api.connection.ConnectionFuture;
import com.barchart.feed.api.model.data.Cuvol;
 import com.barchart.feed.api.model.data.Market;
 import com.barchart.feed.client.provider.BarchartFeed;
 
 public class TestBarchartFeed {
 	
 	protected static final Logger log = LoggerFactory.getLogger(TestBarchartFeed.class);
 	
 	public static void main(final String[] args) throws Exception {
 		
 		final String username = System.getProperty("barchart.username");
 		final String password = System.getProperty("barchart.password");
 		
 		final Feed feed = BarchartFeed.builder().username(username).
 				password(password).build();
 		
 		final MarketObserver<Market> callback = new MarketObserver<Market>() {
 
 			@Override
 			public void onNext(final Market v) {
 				
 				log.debug(
 				v.instrument().symbol() + "\n" +
 
				printCuvol(v.cuvol().entryList()));
 				
 			}
 			
 		};
 		
 		final ConnectionFuture<Feed> start = feed.startup();
 		
 		start.get();
 		
 		final Agent myAgent = feed.newAgent(Market.class, callback);
 		
 		//myAgent.include(ExchangeFactory.fromName("CME"));
 		myAgent.include("ESU13");
 		
 		Thread.sleep(700000);
 		
 		feed.shutdown();
 		
 		Thread.sleep(2000);
 		
 	}
 	
	public static String printCuvol(final List<Cuvol.Entry> entries) {
 		
 		final StringBuilder sb = new StringBuilder();
 		
		for(final Cuvol.Entry e : entries) {
 			sb.append(e.toString() + "\n");
 		}
 		
 		return sb.toString();
 	}
 	
 }
