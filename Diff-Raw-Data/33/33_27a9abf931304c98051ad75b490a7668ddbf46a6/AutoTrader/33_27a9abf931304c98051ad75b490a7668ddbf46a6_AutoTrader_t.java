 package com.lenin.project.service;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.lenin.project.domain.AutoTradingOptions;
 import com.lenin.project.domain.TradeStats;
 import com.lenin.project.domain.Transaction;
 import com.lenin.project.domain.User;
 import com.lenin.project.repositories.TradeRepository;
 import com.lenin.project.repositories.TradeStatsRepository;
 import com.lenin.project.repositories.TransactionRepository;
 import com.lenin.project.repositories.UserRepository;
 
 public class AutoTrader extends UserTrader {
 	
 	public AutoTrader(TradeStats tradeStats, TradeStatsRepository tradeStatsRepository, 
 			TransactionRepository transactionRepository, TradeRepository tradeRepository) {
 		
 		super(tradeStats, tradeStatsRepository, transactionRepository, tradeRepository);
 		
 	}
 	
 	
 	public void autoTrade() {
 		
 		AutoTradingOptions options = tradeStats.getAutoTradingOptions();
 		
 		Double highestSell = highestSell();
 		if(highestSell == null) {
 			highestSell = tradeStats.getOldRate();
 		}
 		
 		Double lowestBuy = lowestBuy();
 		if(lowestBuy == null) {
 			lowestBuy = tradeStats.getOldRate();
 		}
 		
 		List<Transaction> reversibleBuys = getReversibleBuys();
 		List<Transaction> reversibleSells = getReversibleSells();
 		
 		for(Transaction reversibleBuy : reversibleBuys) {
 			reverseTrade(reversibleBuy, false);
 		}
 		
 		for(Transaction reversibleSell : reversibleSells) {
 			reverseTrade(reversibleSell, false);
 		}
 		
 		//System.out.println("autotrading...");
 		
 		if(reversibleBuys.size() == 0 && reversibleSells.size() == 0) {
 			
 			Double tradeChunk = options.getTradeChunk();
 			
 			Double sellRateChange = tradeStats.getRate().getSell() - highestSell;
 			Double buyRateChange = tradeStats.getRate().getBuy() - lowestBuy;
 			
 			//System.out.println("sellRateChange="+tradeStats.getRate().getSell()+"-"+highestSell+"="+sellRateChange+"/buyRateChange="+buyRateChange);
 			
 			if(sellRateChange >= options.getProfitTarget() && 
 				tradeStats.getFundsRight() >= tradeChunk && tradeStats.getRate().getSell() > options.getSellFloor()) {
 				
 				Transaction sellTransaction = 
 						BtceApi.createTransaction(tradeStats.getCurrencyRight()+"_"+tradeStats.getCurrencyLeft(), 
 								tradeChunk, actualTradeRate("sell"), "sell");
 				
 				sellTransaction.setSave(true);
 				
 				trade(sellTransaction);
 				tradeStats.setOldRate(tradeStats.getRate().getLast());
 				tradeStatsRepository.save(tradeStats);
 				
 			
 			} else if(buyRateChange <= -(options.getProfitTarget()*1) && 
 					tradeStats.getRate().getBuy() < options.getBuyCeiling() &&
 					tradeStats.getFundsLeft() >= (tradeChunk * actualTradeRate("buy"))) {
 			
 				Transaction buyTransaction = 
 						BtceApi.createTransaction(tradeStats.getCurrencyRight()+"_"+tradeStats.getCurrencyLeft(), 
 								tradeChunk, actualTradeRate("buy"), "buy");
 				
 				buyTransaction.setSave(true);
 				
 				trade(buyTransaction);
 				tradeStats.setOldRate(tradeStats.getRate().getLast());
 				tradeStatsRepository.save(tradeStats);
 				
 			}
 			
 		}
 		
 	}
 	
 	
 	
 
 
 	
 	public Double lowestBuy() {
 		
 		List<Transaction> transactions = transactionRepository.findByTradeStatsAndType(tradeStats, "buy");
 		
 		Double lowest = null;
 		
 		for(Transaction transaction : transactions) {
 			
			Double difference = Math.abs(transaction.getRate()-tradeStats.getRate().getSell());
			Boolean withinRange = difference < tradeStats.getAutoTradingOptions().getProfitTarget();
			
			if(withinRange || transaction.getFilledAmount() >= transaction.getBrokerAmount()) {
 				if(lowest == null) {
 					lowest = transaction.getRate();
 				} else {
 					if(transaction.getRate() < lowest) {
 						lowest = transaction.getRate();
 					}
 				}
 			}
 			
 		}
 		
 		return lowest;
 		
 	}
 	
 	
 	public Double highestSell() {
 		
 		List<Transaction> transactions = transactionRepository.findByTradeStatsAndType(tradeStats, "sell");
 		
 		Double highest = null;
 		
 		for(Transaction transaction : transactions) {
			
			Double difference = Math.abs(transaction.getRate()-tradeStats.getRate().getBuy());
			Boolean withinRange = difference < tradeStats.getAutoTradingOptions().getProfitTarget();
			
			if(withinRange || transaction.getFilledAmount() >= transaction.getBrokerAmount()) {
 				if(highest == null) {
 					highest = transaction.getRate();
 				} else {
 					if(transaction.getRate() > highest) {
 						highest = transaction.getRate();
 					}
 				}
 			}
 		}
 		
 		return highest;
 		
 	}
 	
 	
 	private List<Transaction> getReversibleSells() {
    		
 		List<Transaction> transactions = transactionRepository.findByTradeStatsAndType(tradeStats, "sell");
 		//System.out.println(transactions);
 		
 		Double calculatedBuyAmount = 0.0;
 		
 		List<Transaction> reversibleTransactions = new ArrayList<Transaction>();
 		
 		for(Transaction transaction : transactions) {
 			
 			Double rateVal = transaction.getRate();
 			Double amountVal = transaction.getAmount();
 			
 			//System.out.println(tradeStats.getRate().getBuy()+" <= ("+rateVal+" - "+tradeStats.getProfitTarget()+")");
 			
 			Boolean isFilled = transaction.getFilledAmount() >= transaction.getBrokerAmount();
 			
 			if(isFilled && tradeStats.getRate().getBuy() <= (rateVal - tradeStats.getAutoTradingOptions().getProfitTarget())) {
 					
 				Double actualBuyRate = actualTradeRate("buy");
 				
 				Double newBuyAmount = 
 					calculatedBuyAmount + amountVal;
 				
 				if(tradeStats.getFundsLeft() > (newBuyAmount * actualBuyRate)) {
 					calculatedBuyAmount = newBuyAmount;
 					reversibleTransactions.add(transaction);
 					//System.out.println("buy "+amountVal+" for "+(amountVal * actualBuyRate));
 				} else {
 					//System.out.println("OUT OF USD!");
 					break;
 				}
 				
 			}
 				
 		}
 			
 		return reversibleTransactions;
 			
 	};
 		
 	
 	private List<Transaction> getReversibleBuys() {
    		
 		List<Transaction> transactions = transactionRepository.findByTradeStatsAndType(tradeStats, "buy");
 		
 		Double calculatedSellAmount = 0.0;
 		
 		List<Transaction> reversibleTransactions = new ArrayList<Transaction>();
 		
 		for(Transaction transaction : transactions) {
 			
 			Double rateVal = transaction.getRate();
 			Double amountVal = transaction.getAmount();
 			
 			Boolean isFilled = transaction.getFilledAmount() >= transaction.getBrokerAmount();
 			
 			if(isFilled && tradeStats.getRate().getSell() >= (rateVal + tradeStats.getAutoTradingOptions().getProfitTarget())) {
 				
 				Double actualSellRate = actualTradeRate("sell");
 				Double newSellAmount = calculatedSellAmount + amountVal;
 				
 				if(tradeStats.getFundsRight() > newSellAmount) {
 					calculatedSellAmount = newSellAmount;
 					reversibleTransactions.add(transaction);
 					//System.out.println("sell "+amountVal+" for "+(amountVal * actualSellRate));
 				} else {
 					//System.out.println("OUT OF LTC!");
 					break;
 				}
 					
 			}
 				
 			
 		}
 		
 		return reversibleTransactions;
 			
 	}
 	
 	
 
 }
