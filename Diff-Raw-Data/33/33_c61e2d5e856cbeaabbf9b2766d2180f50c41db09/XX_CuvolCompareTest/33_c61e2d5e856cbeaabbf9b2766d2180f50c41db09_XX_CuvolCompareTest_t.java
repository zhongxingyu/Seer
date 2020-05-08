 package com.barchart.feed.ddf.client.provider.legacy;
 
 import java.util.List;
 
 import com.barchart.feed.api.Agent;
 import com.barchart.feed.api.Feed;
 import com.barchart.feed.api.MarketObserver;
 import com.barchart.feed.api.connection.ConnectionFuture;
 import com.barchart.feed.api.model.data.Cuvol;
 import com.barchart.feed.api.model.meta.Instrument;
 import com.barchart.feed.base.market.api.MarketTaker;
 import com.barchart.feed.base.market.enums.MarketEvent;
 import com.barchart.feed.base.market.enums.MarketField;
 import com.barchart.feed.ddf.instrument.provider.DDF_InstrumentProvider;
 
 public class XX_CuvolCompareTest {
 	
 	final static String SYMBOL = "ESU3";
 	
 	public static void main(final String[] args) throws Exception {
 		
 		final String username = System.getProperty("barchart.username");
 		final String password = System.getProperty("barchart.password");
 		
 		final TestableFeed feed = new TestableFeed(username, password);
 		
 		final MarketObserver<Cuvol> callback = new MarketObserver<Cuvol>() {
 
 			@Override
 			public void onNext(final Cuvol v) {
 				
 				System.out.println("AGENT: " +
 				v.instrument().symbol() + " " +
				printCuvol(v.entryList()));
 				
 			}
 			
 		};
 		
 		final ConnectionFuture<Feed> start = feed.startup();
 		
 		start.get();
 		
 		final Agent myAgent = feed.newAgent(Cuvol.class, callback);
 		
 		final Instrument inst = DDF_InstrumentProvider.find(SYMBOL).get(0);
 		
 		myAgent.include(inst);
 		
 		feed.addTaker(new CuvolTaker(new Instrument[]{inst}));
 		
 		Thread.sleep(700000);
 		
 	}
 	
	public static String printCuvol(final List<Cuvol.Entry> entries) {
 		
 		final StringBuilder sb = new StringBuilder();
 		
		for(final Cuvol.Entry e : entries) {
			sb.append(e.size().asDouble() + "\t");
 		}
 		
 		return sb.toString();
 		
 	}
 	
 	public static class CuvolTaker implements 
 			MarketTaker<com.barchart.feed.base.market.api.Market> {
 
 			final Instrument[] instruments;
 	
 			public CuvolTaker(final Instrument[] instruments) {
 				this.instruments = instruments;
 			}
 	
 			@Override
 			public MarketField<com.barchart.feed.base.market.api.Market> bindField() {
 				return MarketField.MARKET;
 			}
 
 			@Override
 			public MarketEvent[] bindEvents() {
 				return new MarketEvent[] {MarketEvent.NEW_CUVOL_SNAPSHOT,
 						MarketEvent.NEW_CUVOL_UPDATE, MarketEvent.NEW_TRADE};
 			}
 
 			@Override
 			public Instrument[] bindInstruments() {
 				return instruments;
 			}
 
 			@Override
 			public void onMarketEvent(MarketEvent event, Instrument instrument,
 					com.barchart.feed.base.market.api.Market v) {
 				
 				System.out.println("TAKER: " + v.instrument().symbol() +  " " +
						printCuvol(v.cuvol().entryList()) + "\n");
 				
 			}
 		
 	};
 
 }
