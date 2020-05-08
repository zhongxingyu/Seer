 package com.lenin.project.service;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.codehaus.jettison.json.JSONObject;
 
 import com.lenin.project.domain.TickerQuote;
 import com.lenin.project.domain.Trade;
 import com.lenin.project.domain.TradeStats;
 import com.lenin.project.domain.Transaction;
 import com.lenin.project.domain.User;
 import com.lenin.project.repositories.TickerRepository;
 import com.lenin.project.repositories.TradeRepository;
 import com.lenin.project.repositories.TradeStatsRepository;
 import com.lenin.project.repositories.TransactionRepository;
 import com.lenin.project.repositories.UserRepository;
 
 public class DataProcessor {
 	
 	Map<String, TickerQuote> tickerMap = new HashMap<String, TickerQuote>();
 	
 	private Long tickerCounter = 0L;
 	private Long lastTickerTime = 0L;
 	
 	private Long lastTradeTime = 0L;
 	
 	private TransactionRepository transactionRepository;
 	private TradeStatsRepository tradeStatsRepository;
 	private TradeRepository tradeRepository;
 	private TickerRepository tickerRepository;
 	
 	
 	public DataProcessor(UserRepository userRepsoitory, TradeStatsRepository tradeStatsRepository, 
 			TransactionRepository transactionRepository,
 			TradeRepository tradeRepository, TickerRepository tickerRepository) {
 		
 		this.transactionRepository = transactionRepository;
 		this.tradeStatsRepository = tradeStatsRepository;
 		this.tradeRepository = tradeRepository;
 		this.tickerRepository = tickerRepository;
 		
 	}
 	
 	
 	public DataProcessor() {
 		
 	}
 
 	protected void updateTicker() {
 		
 		TickerQuote tickerLtcUsd = getTickerQuote("ltc_usd");
 		TickerQuote tickerBtcUsd = getTickerQuote("btc_usd");
 		TickerQuote tickerLtcBtc = getTickerQuote("ltc_btc");
 		
 		List<TickerQuote> tickerQuotes = new ArrayList<TickerQuote>();
 		tickerQuotes.add(tickerLtcUsd);
 		tickerQuotes.add(tickerBtcUsd);
 		tickerQuotes.add(tickerLtcBtc);
 		tickerRepository.save(tickerQuotes);
 		
 		lastTickerTime = tickerLtcUsd.getTime();
 		
 		tickerMap = new HashMap<String, TickerQuote>();
 		tickerMap.put("ltc_usd", tickerLtcUsd);
 		tickerMap.put("btc_usd", tickerBtcUsd);
 		tickerMap.put("ltc_btc", tickerLtcBtc);
 		
 		
 		if(tickerCounter > 0) {
 		
 			if(tickerCounter % 4 == 0) {
 				createAverageQuotes("1min");
 			}
 		
 			if(tickerCounter % 40 == 0) {
 				createAverageQuotes("10min");
 			}
 		
 			if(tickerCounter % 120 == 0) {
 				createAverageQuotes("30min");
 			}
 		
 			if(tickerCounter % 960 == 0) {
 				createAverageQuotes("4h");
 			}
 		
 			if(tickerCounter % 1440 == 0) {
 				createAverageQuotes("6h");
 				tickerCounter = 0L;
 			}
 			
 		}
 		
 		tickerCounter++;
 		
 	}
 	
 	
 	private void createAverageQuotes(String setType) {
 		
 		TickerQuote avgLtcUsd = getAverageQuote("ltc_usd", setType);
 		TickerQuote avgBtcUsd = getAverageQuote("btc_usd", setType);
 		TickerQuote avgLtcBtc = getAverageQuote("ltc_btc", setType);
 		
 		List<TickerQuote> avgQuotes = new ArrayList<TickerQuote>();
 		avgQuotes.add(avgLtcUsd);
 		avgQuotes.add(avgBtcUsd);
 		avgQuotes.add(avgLtcBtc);
 		
 		tickerRepository.save(avgQuotes);
 		
 	}
 	
 	
 	private TickerQuote getAverageQuote(String pair, String setType) {
 		
 		Long period = 0L;
 		
 		if(setType.equals("1min")) {
 			period = 60L;
 		} else if(setType.equals("10min")) {
 			period = 600L;
 		} else if(setType.equals("30min")) {
 			period = 1800L;
 		} else if(setType.equals("4h")) {
 			period = 14400L;
 		} else if(setType.equals("6h")) {
 			period = 21600L;
 		}
 		
 		List<TickerQuote> tickerQuotes = tickerRepository.findByPairAndTimeGreaterThanOrderByTimeAsc(pair, lastTickerTime - period);
 		
 		Integer count = 0;
 		Double totalLast = 0.0;
 		Double totalBuy = 0.0;
 		Double totalSell = 0.0;
 		
 		for(TickerQuote quote : tickerQuotes) {
 			
 			totalLast += quote.getLast();
 			totalBuy += quote.getBuy();
 			totalSell += quote.getSell();
 			
 			count++;
 		
 		}
 		
 		Double avgLast = 0.0;
 		Double avgBuy = 0.0;
 		Double avgSell = 0.0;
 		
 		if(count > 0) {
 			avgLast = totalLast/count;
 			avgBuy = totalBuy/count;
 			avgSell = totalSell/count;
 		}
 		
 		TickerQuote avgQuote = new TickerQuote();
 		avgQuote.setSetType(setType);
 		avgQuote.setPair(pair);
 		avgQuote.setTime(lastTickerTime);
 		
 		avgQuote.setLast(avgLast);
 		avgQuote.setBuy(avgBuy);
 		avgQuote.setSell(avgSell);
 		
 		return avgQuote;
 		
 	}
 	
 	
 	private TickerQuote getTickerQuote(String pair) {
 		
 		try {
 		
 			TickerQuote tickerQuote = new TickerQuote();
 		
 			JSONObject rates = BtceApi.getRates(pair);
 			JSONObject ticker = rates.getJSONObject("ticker");
 			
 			tickerQuote.setSetType("15s");
 			tickerQuote.setPair(pair);
 			tickerQuote.setLast(ticker.getDouble("last"));
 			tickerQuote.setBuy(ticker.getDouble("buy"));
 			tickerQuote.setSell(ticker.getDouble("sell"));
 			tickerQuote.setTime(ticker.getLong("server_time"));
 			
 			return tickerQuote;
 			
 		} catch(Exception e) {
 			
 			e.printStackTrace();
 			return null;
 			
 		}
     	
 	}
 	
 	
 	protected void updateTradeHistory() {
 		
 		List<Trade> savedTrades = tradeRepository.findAll();
 		for(Trade trade : savedTrades) {
 			if(trade.getLive() == true && trade.getTime() > lastTradeTime) {
 				lastTradeTime = trade.getTime();
 			}
 		}
 		
 		JSONObject tradeListResult = BtceApi.getTradeList(lastTradeTime+1);
 		
 		try {
 			
 			if(tradeListResult.getInt("success") == 1) {
 				
 				JSONObject tradeListResultData = tradeListResult.getJSONObject("return");
 				Iterator<String> tradeIds = tradeListResultData.keys();
 				
 				List<Trade> trades = new ArrayList<Trade>();
 				
 				while(tradeIds.hasNext()) {
 					
 					String tradeId = tradeIds.next();
 					JSONObject tradeData = tradeListResultData.getJSONObject(tradeId);
 					
 					String orderId = tradeData.getString("order_id");
 					String pair = tradeData.getString("pair");
 					Double amount = tradeData.getDouble("amount");
 					Double rate = tradeData.getDouble("rate");
 					String type = tradeData.getString("type");
 					Long time = tradeData.getLong("timestamp");
 					
 					Trade trade = new Trade();
 					trade.setLive(true);
 					trade.setOrderId(orderId);
 					trade.setPair(pair);
 					trade.setAmount(amount);
 					trade.setRate(rate);
 					trade.setType(type);
 					trade.setTime(time);
 					
 					trades.add(trade);
 					
 					if(time > lastTradeTime) {
 						lastTradeTime = time;
 					}
 					
 				}
 				
 				if(trades.size() > 0) {
 					System.out.println("New trades: "+trades.size()+"; Last trade time: "+lastTradeTime);
 					tradeRepository.save(trades);
 				}
 				
 			} else {
 				
 				String error = tradeListResult.getString("error");
 				if(!error.equals("no trades")) {
					BtceApi._nonce = System.currentTimeMillis() / 10000L;
 					System.out.println("Trades update unsuccessful: "+error);
 				}
 				
 			}
 			
 		} catch(Exception e) {
 			e.printStackTrace();
 		}
 		
 		
 	}
 	
 	
 	protected void updateTransactions() {
 		
 		List<Transaction> transactions = transactionRepository.findAll();
 		List<Transaction> changedTransactions = new ArrayList<Transaction>();
 		
 		for(Transaction transaction : transactions) {
 			
 			
 			if(transaction.getFilledAmount() < transaction.getBrokerAmount() && transaction.getIsReversed() != true) {
 				
 				List<Trade> trades = tradeRepository.findByOrderId(transaction.getOrderId());
 				
 				Double amount = 0.0;
 				
 				if(trades.size() > 0) {
 					
 					for(Trade trade : trades) {
 						amount += trade.getAmount();
 					}
 				
 				}
 				
 				
 				if(amount > transaction.getFilledAmount()) {
 					
 					Double amountChange = amount-transaction.getFilledAmount();
 					
 					transaction.setFilledAmount(amount);
 					changedTransactions.add(transaction);
 					
 					Transaction reversedTransaction = transaction.getReversedTransaction();
 					
 					if(reversedTransaction != null) {
 						
 						Double totalFeeFactor = (1-UserTrader.transactionFee) * (1-BtceApi.transactionFee);
 						
 						reversedTransaction.setFilledAmount(transaction.getFilledAmount());
 						changedTransactions.add(reversedTransaction);
 						
 						Trade trade = new Trade();
 						trade.setAmount(amountChange);
 						
 						TradeStats tradeStats = transaction.getTradeStats();
 						tradeStats.setProfitLeft(tradeStats.getProfitLeft() + (transaction.calcTradeRevenue(trade)*totalFeeFactor));
 						tradeStatsRepository.save(tradeStats);
 						
 						if(transaction.getFilledAmount() >= transaction.getBrokerAmount()) {
 							changedTransactions.remove(transaction);
 							changedTransactions.remove(reversedTransaction);
 							transactionRepository.delete(transaction);
 							transactionRepository.delete(reversedTransaction);
 						}
 						
 					}
 					
 				}
 				
 				
 			}
 			
 			if(transaction.getLive() == false && transaction.getFilledAmount() < transaction.getBrokerAmount()) {
 				
 				Long unixTime = System.currentTimeMillis() / 1000L;
 				
 				Trade trade = new Trade();
 				trade.setLive(false);
 				trade.setOrderId(transaction.getOrderId());
 				trade.setAmount(transaction.getBrokerAmount()-transaction.getFilledAmount());
 				trade.setTime(unixTime);
 				
 				tradeRepository.save(trade);
 				
 			}
 			
 		}
 		
 		transactionRepository.save(changedTransactions);
 		
 		
 	}
 	
 	
 	protected void updateTradeStats() {
 		
 		List<TradeStats> allTradeStats = tradeStatsRepository.findAll();
 		//System.out.println("Trade stats total: "+allTradeStats.size());
 		
 		for(int i=0; i<allTradeStats.size(); i++) {
 			
 			TradeStats tradeStats = allTradeStats.get(i);
 			
 			//System.out.println(tradeStats.getId()+": "+tradeStats.getLive());
 			
 			
 			if(tradeStats.getLive() == true) {
 				
 				TickerQuote tickerQuote = tickerMap.get(tradeStats.getPair());
 				
 				//System.out.println("ticker for "+tradeStats.getPair()+": "+tickerQuote.getLast());
 				
 				if(tickerQuote != null) {
 					tradeStats.setRate(tickerQuote);
 				}
 				
 			} else {
 				
 				
 				TickerQuote tickerQuote = tradeStats.getRate();
 				tickerQuote.setTime(System.currentTimeMillis()/1000L);
 				
 				tradeStats.setRate(tickerQuote);
 				
 			}
 			
 			
 			Boolean resetOldRate =
 					( (tradeStats.getRate().getLast() - tradeStats.getOldRate() > tradeStats.getAutoTradingOptions().getProfitTarget()) && 
 							tradeStats.getRate().getLast() < tradeStats.getAutoTradingOptions().getBuyCeiling() ) ||
 					( (tradeStats.getRate().getLast() - tradeStats.getOldRate() < - tradeStats.getAutoTradingOptions().getProfitTarget()) &&
 							tradeStats.getRate().getLast() > tradeStats.getAutoTradingOptions().getSellFloor() );
 			
 			if(tradeStats.getOldRate() == 0.0 || resetOldRate) {
 				tradeStats.setOldRate(tradeStats.getRate().getLast());
 			}
 			
 			tradeStatsRepository.save(tradeStats);
 			
 			//System.out.println(tradeStats.getPair()+"("+tradeStats.getId()+"): "+tradeStats.getRate());
 			
 			if(tradeStats.getAutoTradingOptions().getTradeAuto() == true && tradeStats.getRate().getLast() != 0.0) {
 				AutoTrader autoTrader = new AutoTrader(tradeStats, tradeStatsRepository, transactionRepository, tradeRepository);
 				autoTrader.autoTrade();
 			}
 			
 			
 		}
 		
 		
 	}
 	
 	
 	protected void updateUsers() {
 		
 		
 		
 	}
 	
 	
 }
