 package com.lenin.project.service;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.HeaderParam;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 
 import org.codehaus.jettison.json.JSONException;
 import org.codehaus.jettison.json.JSONObject;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.mongodb.core.MongoTemplate;
 import org.springframework.scheduling.annotation.Scheduled;
 import org.springframework.stereotype.Service;
 
 import com.lenin.project.domain.AutoTradingOptions;
 import com.lenin.project.domain.TickerQuote;
 import com.lenin.project.domain.Trade;
 import com.lenin.project.domain.TradeStats;
 import com.lenin.project.domain.Transaction;
 import com.lenin.project.domain.User;
 import com.lenin.project.repositories.CommentRepository;
 import com.lenin.project.repositories.TickerRepository;
 import com.lenin.project.repositories.TradeRepository;
 import com.lenin.project.repositories.TradeStatsRepository;
 import com.lenin.project.repositories.TransactionRepository;
 import com.lenin.project.repositories.UserRepository;
 
 @Service
 @Path("/")
 public class TravellerService {
 	
 	@Autowired
 	private MongoTemplate mongoTemplate;
 	
 	@Autowired
 	private CommentRepository commentRepository;
 	
 	@Autowired
 	private UserRepository userRepository;
 	
 	@Autowired
 	private TransactionRepository transactionRepository;
 	
 	@Autowired
 	private TradeRepository tradeRepository;
 	
 	@Autowired
 	private TradeStatsRepository tradeStatsRepository;
 	
 	@Autowired
 	private TickerRepository tickerRepository;
 	
 	DataProcessor dataProcessor;
 	
 	
 	public TravellerService() {
 	}
 	
 	@Scheduled(fixedDelay = 15000)
 	public void update() {
 		
 		if(dataProcessor == null) {
 		
 			dataProcessor = new DataProcessor(userRepository, tradeStatsRepository, transactionRepository, 
 				tradeRepository, tickerRepository);
 		
 		}
 		
 		dataProcessor.updateTicker();
 		
 		dataProcessor.updateTradeHistory();
 		
 		dataProcessor.updateTransactions();
 		
 		dataProcessor.updateTradeStats();
 		
 		
 	}
 	
 	
 	
 	@GET
 	@Path("/rates")
     @Produces({ MediaType.APPLICATION_JSON })
 	public RequestResponse getRates(@QueryParam("pair") String pair, @QueryParam("setType") String setType, 
 			@QueryParam("from") Long from, @QueryParam("until") Long until) {
 		
 		//System.out.println(pair+"/"+setType+"/"+from+"/"+until);
 		
 		RequestResponse response = new RequestResponse();
 		
 		List<TickerQuote> rates = new ArrayList<TickerQuote>();
 		
 		if(pair != null && setType != null && from != null && until != null) {
 			rates = tickerRepository.findByPairAndSetTypeAndTimeBetweenOrderByTimeAsc(pair, setType, from, until);
 		} else if(pair != null && from != null && until != null) {
 			rates = tickerRepository.findByPairAndTimeBetweenOrderByTimeAsc(pair, from, until);
 		} else if(pair != null && from != null) {
 			rates = tickerRepository.findByPairAndTimeGreaterThanOrderByTimeAsc(pair, from);
 		} else if(pair != null && until != null) {
 			rates = tickerRepository.findByPairAndTimeLessThanOrderByTimeAsc(pair, until);
 		} else {
 			rates = tickerRepository.findAll();
 		}
 		
 		response.setSuccess(1);
 		response.setData(rates);
 		
 		return response;
 		
 	}
 	
 	@PUT
     @Path("/rate")
 	@Consumes({ MediaType.APPLICATION_JSON })
 	@Produces({ MediaType.APPLICATION_JSON })
     public RequestResponse setRate(@HeaderParam("User-Id") String userId, 
     		@HeaderParam("TradeStats-Id") String tradeStatsId, TickerQuote rate) {
         
 		RequestResponse response = new RequestResponse();
 		
 		TradeStats tradeStats = tradeStatsRepository.findOne(tradeStatsId);
 		
 		if(tradeStats.getLive() == false) {
 			
 			rate.setTime(System.currentTimeMillis()/1000L);
 			tradeStats.setRate(rate);
 			tradeStatsRepository.save(tradeStats);
 		
 			response.setSuccess(1);
 			response.setData(rate);
 		
 		} else {
 			response.setSuccess(0);
 		}
 		
 		return response;
 		
 	}
 	
 	
 	@PUT
     @Path("/autoTradingOptions")
 	@Consumes({ MediaType.APPLICATION_JSON })
 	@Produces({ MediaType.APPLICATION_JSON })
     public RequestResponse saveAutoTradingOptions(@HeaderParam("User-Id") String userId, 
     		@HeaderParam("TradeStats-Id") String tradeStatsId, AutoTradingOptions autoTradingOptions) {
         
 		RequestResponse response = new RequestResponse();
 		
 		TradeStats tradeStats = tradeStatsRepository.findOne(tradeStatsId);
 		tradeStats.setAutoTradingOptions(autoTradingOptions);
 		
 		/*
 		if(dbTradeStats.getRate() != null) {
 			Long rateTime = dbTradeStats.getRate().getTime();
 			tradeStats.getRate().setTime(rateTime);
 		}
 		*/
 		
 		tradeStatsRepository.save(tradeStats);
 		
 		response.setSuccess(1);
 		response.setData(autoTradingOptions);
 		
 		return response;
 		
 	}
 	
 	@GET
 	@Path("/tradeStatsRefresh")
 	@Consumes({ MediaType.APPLICATION_JSON })
     @Produces({ MediaType.APPLICATION_JSON })
     public RequestResponse getRefreshData(@HeaderParam("User-Id") String userId,
     		@HeaderParam("TradeStats-Id") String tradeStatsId) {
 		
 		RequestResponse response = new RequestResponse();
 		
 		if(tradeStatsId != null) {
 			
 			TradeStats tradeStats = tradeStatsRepository.findOne(tradeStatsId);
 			
 			RefreshData refreshData = new RefreshData();
 			refreshData.setTransactions(transactionRepository.findByTradeStats(tradeStats));
 			refreshData.setFundsLeft(tradeStats.getFundsLeft());
 			refreshData.setFundsRight(tradeStats.getFundsRight());
 			refreshData.setProfitLeft(tradeStats.getProfitLeft());
 			refreshData.setProfitRight(tradeStats.getProfitRight());
 			refreshData.setRate(tradeStats.getRate());
 			
 			response.setData(refreshData);
 		
 		} else {
 			response.setSuccess(0);
 		}
 		
 		response.setSuccess(1);
 		
 		return response;
 		
 	}
 	
 	
 	@GET
 	@Path("/tradeStats")
 	@Consumes({ MediaType.APPLICATION_JSON })
     @Produces({ MediaType.APPLICATION_JSON })
     public RequestResponse getTradeStats(@HeaderParam("User-Id") String userId,
     		@HeaderParam("TradeStats-Id") String tradeStatsId) {
 		
 		
 		RequestResponse response = new RequestResponse();
 		
 		if(tradeStatsId != null) {
 			TradeStats tradeStats = tradeStatsRepository.findOne(tradeStatsId); //user.getCurrentTradeStats();
 			response.setData(tradeStats);
 		} else {
 			List<TradeStats> tradeStats = tradeStatsRepository.findAll();
 			response.setData(tradeStats);
 		}
 		
 		response.setSuccess(1);
 		
 		return response;
 		
 	}
 	
 	
 	@PUT
     @Path("/tradeStats")
 	@Consumes({ MediaType.APPLICATION_JSON })
 	@Produces({ MediaType.APPLICATION_JSON })
     public TradeStats saveUserDetails(@HeaderParam("User-Id") String userId, 
     		@HeaderParam("TradeStats-Id") String tradeStatsId, TradeStats tradeStats) {
         
 		TradeStats dbTradeStats = tradeStatsRepository.findOne(tradeStats.getId());
 		
 		if(dbTradeStats.getRate() != null) {
 			Long rateTime = dbTradeStats.getRate().getTime();
 			tradeStats.getRate().setTime(rateTime);
 		}
 		
 		tradeStatsRepository.save(tradeStats);
 		
 		return tradeStats;
 		
 	}
 	
 	@POST
     @Path("/tradeStats")
 	@Consumes({ MediaType.TEXT_PLAIN })
 	@Produces({ MediaType.APPLICATION_JSON })
     public TradeStats addTradeStats(@HeaderParam("User-Id") String userId, String currencyPair) {
         
 		System.out.println("new pair: ]"+currencyPair+"[");
 		
 		User user = userRepository.findByUsername(userId);
 		
 		String[] currencies = currencyPair.split("_");
 		TradeStats tradeStats = new TradeStats();
 		tradeStats.setAutoTradingOptions(new AutoTradingOptions());
 		tradeStats.setCurrencyRight(currencies[0]);
 		tradeStats.setCurrencyLeft(currencies[1]);
 		tradeStats.setLive(user.getLive());
 		
 		TickerQuote rate = new TickerQuote();
 		if(user.getLive() == false) {
 			rate.setBuy(1.0);
 			rate.setSell(1.0);
 			rate.setLast(1.0);
 		}
 		
 		tradeStats.setRate(rate);
 		
 		System.out.println("Added new tradestats: "+tradeStats.getCurrencyRight()+"/"+tradeStats.getCurrencyLeft());
 		
 		tradeStats = tradeStatsRepository.save(tradeStats);
 		
 		user.addTradeStats(tradeStats);
 		userRepository.save(user);
 		
 		return tradeStats;
 		
 	}
 	
 	
 	@DELETE
     @Path("/tradeStats")
 	@Consumes({ MediaType.APPLICATION_JSON })
 	@Produces({ MediaType.APPLICATION_JSON })
     public RequestResponse deleteTradeStats(@HeaderParam("User-Id") String userId, 
     		@HeaderParam("TradeStats-Id") String tradeStatsId, TradeStats tradeStats) {
         
 		
 		RequestResponse response = new RequestResponse();
 		
 		if(tradeStatsId != null) {
 			
 			tradeStats = tradeStatsRepository.findOne(tradeStatsId); //user.getCurrentTradeStats();
 			
 			User user = userRepository.findByUsername(userId);
 			List<TradeStats> tradeStatsList = user.getTradeStats();
 			System.out.println("Deleting from "+tradeStatsList.size()+" tabs");
 			
 			int index = -1;
 			for(int i=0; i<tradeStatsList.size(); i++) {
 				if(tradeStatsList.get(i).getId().equals(tradeStats.getId())) {
 					index = i;
 					break;
 				}
 			}
 			
 			if(index != -1)  {
 				System.out.println("Deleting tradeStat "+tradeStatsId+" at "+index);
 				tradeStatsList.remove(index);
 			}
 			
 			System.out.print("Remaining tradeStats size "+tradeStatsList.size()+" ... ");
 			user.setTradeStats(tradeStatsList);
 			
 			TradeStats current = null;
 			if(tradeStatsList.size() > 0) {
 				user.setCurrentTradeStats(tradeStatsList.get(0));
 			}
 			
 			user = userRepository.save(user);
 			System.out.println(user.getTradeStats().size());
 			
 			tradeStatsRepository.delete(tradeStats);
 			
 			response.setSuccess(1);
 			response.setData(user.getTradeStats());
 			
 		}
 		
 		return response;
 		
 	}
 	
 	@POST
     @Path("/funds")
 	@Consumes({ MediaType.APPLICATION_JSON })
 	@Produces({ MediaType.APPLICATION_JSON })
     public RequestResponse setFunds(@HeaderParam("User-Id") String userId, 
     		@HeaderParam("TradeStats-Id") String tradeStatsId, 
     		@QueryParam("left") Double left, @QueryParam("right") Double right) {
         
 		RequestResponse response = new RequestResponse();
 		
 		User user = userRepository.findByUsername(userId);
 		Map<String, Double> fundsMap = user.getFunds();
 		
 		TradeStats tradeStats = tradeStatsRepository.findOne(tradeStatsId);
 		
 		if(left != null) {
 			
 			Double tsFundsLeft = tradeStats.getFundsLeft();
 			String tsCurrencyLeft = tradeStats.getCurrencyLeft();
 			Double userFundsLeft = fundsMap.get(tsCurrencyLeft);
 			Double changeLeft = tsFundsLeft - left;
 			
 			userFundsLeft = userFundsLeft + changeLeft;
 			
 			if(userFundsLeft >= 0 || user.getLive() == false) {
 				tradeStats.setFundsLeft(left);
 				fundsMap.put(tsCurrencyLeft, userFundsLeft);
 				user.setFunds(fundsMap);
 				userRepository.save(user);
 				tradeStatsRepository.save(tradeStats);
 				response.setSuccess(1);
 				response.setData(fundsMap);
 			} else {
 				response.setSuccess(0);
 				response.setMessage("Not enough funds");
 			}
 			
 		}
 		
 		if(right != null) {
 			
 			Double tsFundsRight = tradeStats.getFundsRight();
 			String tsCurrencyRight = tradeStats.getCurrencyRight();
 			Double userFundsRight = fundsMap.get(tsCurrencyRight);
 			Double changeRight = tsFundsRight - right;
 			
 			userFundsRight = userFundsRight + changeRight;
 			
 			if(userFundsRight >= 0 || user.getLive() == false) {
 				tradeStats.setFundsRight(right);
 				fundsMap.put(tsCurrencyRight, userFundsRight);
 				user.setFunds(fundsMap);
 				userRepository.save(user);
 				tradeStatsRepository.save(tradeStats);
 				response.setSuccess(1);
 				response.setData(fundsMap);
 			} else {
 				response.setSuccess(0);
 				response.setMessage("Not enough funds");
 			}
 			
 		}
 		
 		return response;
 		
 	}
 	
 	@GET
 	@Path("/info")
 	@Consumes({ MediaType.APPLICATION_JSON })
     @Produces({ MediaType.APPLICATION_JSON })
     public RequestResponse getAccountInfo(@HeaderParam("User-Id") String userId) {
 		
 		RequestResponse response = new RequestResponse();
 		User user = userRepository.findByUsername(userId);
 		
 			
 		JSONObject accountInfoResult = BtceApi.getAccountInfo();
 			
 		if(accountInfoResult == null) {
 			response.setSuccess(0);
 			response.setMessage("Could not get account info.");
 			return response;
 		}
 			
 		try {
 				
 			Integer success = accountInfoResult.getInt("success");
 			response.setSuccess(success);
 				
 			if(success == 1) {
 					
 				JSONObject funds = 
 						accountInfoResult
 							.getJSONObject("return")
 							.getJSONObject("funds");
 					
 				//tradeStats.setFundsRight(funds.getDouble(tradeStats.getCurrencyRight()));
 				//tradeStats.setFundsLeft(funds.getDouble(tradeStats.getCurrencyLeft()));
 					
 				//tradeStatsRepository.save(tradeStats);
 					
 				//response.setData(user);
 					
 			}
 				
 		} catch(JSONException e) {
 				
 			e.printStackTrace();
 				
 			response.setSuccess(-1);
 			response.setMessage(e.getMessage());
 				
 		}
 			
 		//response.setSuccess(1);
 		response.setData(user);
 		
 		return response;
 		
 		
 	}
 	
 	@GET
     @Path("/trade")
 	@Produces({ MediaType.APPLICATION_JSON })
     public List<Trade> getTrades(@HeaderParam("User-Id") String userId, @QueryParam("orderId") String orderId) {
         
 		if(orderId != null) {
 			return tradeRepository.findByOrderId(orderId);
 		} else {
 			return tradeRepository.findAll();
 		}
 		
 	}
 	
 	
 	@GET
     @Path("/transaction")
 	@Produces({ MediaType.APPLICATION_JSON })
     public List<Transaction> getTransactions(@HeaderParam("User-Id") String userId, 
     		@HeaderParam("TradeStats-Id") String tradeStatsId, @QueryParam("type") String type) {
         
 		
 		if(type != null) {
 			User user = userRepository.findByUsername(userId);
 			TradeStats tradeStats = tradeStatsRepository.findOne(tradeStatsId); //user.getCurrentTradeStats();
 			user.setCurrentTradeStats(tradeStats);
 			return transactionRepository.findByTradeStatsAndType(tradeStats, type);
 		} else if(userId != null) {
 			User user = userRepository.findByUsername(userId);
 			TradeStats tradeStats = tradeStatsRepository.findOne(tradeStatsId); //user.getCurrentTradeStats();
 			user.setCurrentTradeStats(tradeStats);
 			return transactionRepository.findByTradeStats(tradeStats);
 		} else {
 			return transactionRepository.findAll();
 		}
 		
 	}
 	
 	
 	@DELETE
 	@Path("/transaction")
 	@Consumes({ MediaType.APPLICATION_JSON })
     @Produces({ MediaType.APPLICATION_JSON })
 	public RequestResponse deleteTransaction(@HeaderParam("User-Id") String userId, 
 			@HeaderParam("TradeStats-Id") String tradeStatsId, Transaction transaction) {
 		
 		transactionRepository.delete(transaction);
 		transaction = transactionRepository.findOne(transaction.getId());
 		
 		RequestResponse response = new RequestResponse();
 		
 		if(transaction == null) {
 			response.setSuccess(1);
 		} else {
 			response.setSuccess(0);
 		}
 		
 		List<Transaction> transactions = getTransactions(userId, tradeStatsId, null);
 		response.setData(transactions);
 		
 		return response;
 		
 	}
 	
 	
 	@POST
 	@Path("/transaction")
 	@Consumes({ MediaType.APPLICATION_JSON })
     @Produces({ MediaType.APPLICATION_JSON })
     public RequestResponse postTransaction(@HeaderParam("User-Id") String userId, 
     		@HeaderParam("TradeStats-Id") String tradeStatsId, 
     		Transaction transaction, @QueryParam("cancel") String cancel) {
 		
 		RequestResponse response = new RequestResponse();
 		
 		User user = userRepository.findByUsername(userId);
 		if(user == null) {
 			response.setSuccess(0);
 			response.setMessage("Could not read user data.");
 			return response;
 		}
 		
 		TradeStats tradeStats = tradeStatsRepository.findOne(tradeStatsId); //user.getCurrentTradeStats();
 		user.setCurrentTradeStats(tradeStats);
 		
 		if(tradeStats.getRate().getLast() == 0.0) {
 			response.setSuccess(0);
 			response.setMessage("Rate not set");
 			return response;
 		}
 		
 		
 		UserTrader userTrader = new UserTrader(tradeStats, tradeStatsRepository, transactionRepository, tradeRepository);
 		
 		if(cancel == null) {
 			response = userTrader.trade(transaction);
 		} else {
 			response = userTrader.cancelOrder(transaction);
 		}
 		
 		List<Transaction> transactions = getTransactions(userId, tradeStatsId, null);
 		response.setData(transactions);
 		
 		return response;
 		
 	}
 	
 	
 	
 	@GET
     @Path("/test")
     @Produces({ MediaType.APPLICATION_JSON })
     public List<Transaction> test(@QueryParam("fromId") String tradeStatsId) {
 		
 		Double feeFactor = 1-0.002;
 		
 		List<Transaction> transactions = transactionRepository.findAll();
 		
 		for(Transaction transaction : transactions) {
 			Double brokerAmount = transaction.getAmount()*(feeFactor-0.001);
 			transaction.setBrokerAmount(brokerAmount);
 		}
 		
 		transactionRepository.save(transactions);
 		
 		return transactions;
     
 	}
 	
 	@GET
 	@Path("/funds")
     @Produces({ MediaType.TEXT_PLAIN })
 	public String changeFunds(@HeaderParam("User-Id") String userId,
 			@HeaderParam("TradeStats-Id") String tradeStatsId,
 			@QueryParam("fund") String fund, @QueryParam("change") Double change) {
 		
 		User user = userRepository.findByUsername(userId);
 		TradeStats tradeStats = tradeStatsRepository.findOne(tradeStatsId); //user.getCurrentTradeStats();
 		user.setCurrentTradeStats(tradeStats);
 		
 		user.setFunds(fund, user.getFunds(fund) + change);
 		
 		tradeStatsRepository.save(user.getCurrentTradeStats());
 		
 		return "OK";
 		
 	}
 	
 	
 	@POST
     @Path("/userfunds")
 	@Consumes({ MediaType.APPLICATION_JSON })
 	@Produces({ MediaType.APPLICATION_JSON })
     public RequestResponse addUserFunds(@HeaderParam("User-Id") String userId,
     		@QueryParam("currency") String currency, @QueryParam("amount") Double amount) {
 		
 		RequestResponse response = new RequestResponse();
 		
 		User user = userRepository.findByUsername(userId);
 		
 		Map<String, Double> userFunds = user.getFunds();
 		userFunds.put(currency, userFunds.get(currency) + amount);
 		user.setFunds(userFunds);
 		
 		user = userRepository.save(user);
 		
 		response.setData(user);
 		response.setSuccess(1);
 		
 		return response;
 		
 	}
 	
 	
 	@GET
     @Path("/init")
     @Produces({ MediaType.TEXT_PLAIN })
     public String initUsers(@QueryParam("fromId") String fromId, @QueryParam("toId") String toId) {
 		
 		//System.out.println(fromId);
 		//System.out.println(toId);
 		
 		String result = "test";
 		
 		deleteAll();
 		
 		User testUser1 = new User();
 		testUser1.setUsername("testUser123");
 		testUser1.setLive(false);
 		Map<String, Double> funds1_1 = new HashMap<String, Double>();
 		funds1_1.put("usd", 100.0);
 		funds1_1.put("ltc", 100.0);
 		funds1_1.put("btc", 100.0);
 		testUser1.setFunds(funds1_1);
 		
 		TradeStats tradeStats1_1 = new TradeStats();
 		tradeStats1_1.setCurrencyLeft("usd");
 		tradeStats1_1.setCurrencyRight("ltc");
 		tradeStats1_1.setLive(false);
 		TickerQuote rate1_1 = new TickerQuote();
 		rate1_1.setTime(System.currentTimeMillis()/1000L);
 		rate1_1.setPair("ltc_usd");
 		tradeStats1_1.setRate(rate1_1);
 		tradeStats1_1 = tradeStatsRepository.save(tradeStats1_1);
 		tradeStats1_1.setAutoTradingOptions(new AutoTradingOptions());
 		
 		TradeStats tradeStats1_2 = new TradeStats();
 		tradeStats1_2.setCurrencyLeft("btc");
 		tradeStats1_2.setCurrencyRight("ltc");
 		tradeStats1_2.setLive(false);
 		TickerQuote rate1_2 = new TickerQuote();
 		rate1_2.setTime(System.currentTimeMillis()/1000L);
 		rate1_2.setPair("ltc_btc");
 		tradeStats1_2.setRate(rate1_2);
 		tradeStats1_2 = tradeStatsRepository.save(tradeStats1_2);
 		tradeStats1_2.setAutoTradingOptions(new AutoTradingOptions());
 		
 		testUser1.addTradeStats(tradeStats1_1);
 		testUser1.addTradeStats(tradeStats1_2);
 		testUser1.setCurrentTradeStats(tradeStats1_1);
 		userRepository.save(testUser1);
 		
 		User testUser2 = new User();
 		testUser2.setUsername("testUser456");
 		testUser2.setLive(true);
 		Map<String, Double> funds2_1 = new HashMap<String, Double>();
 		funds2_1.put("usd", 0.0);
 		funds2_1.put("ltc", 0.0);
 		funds2_1.put("btc", 0.0);
 		testUser2.setFunds(funds2_1);
 		
 		TradeStats tradeStats2 = new TradeStats();
 		tradeStats2.setCurrencyLeft("usd");
 		tradeStats2.setCurrencyRight("ltc");
 		tradeStats2.setLive(true);
 		tradeStats2 = tradeStatsRepository.save(tradeStats2);
 		tradeStats2.setAutoTradingOptions(new AutoTradingOptions());
 		
 		testUser2.addTradeStats(tradeStats2);
 		testUser2.setCurrentTradeStats(tradeStats2);
 		userRepository.save(testUser2);
 		
 		
         return result;
     
 	}
 	
 	
 	@GET
     @Path("/deltrades")
 	@Produces({ MediaType.TEXT_PLAIN })
     public String delTrades(@HeaderParam("User-Id") String userId, @QueryParam("orderId") String orderId) {
         
 		tradeRepository.deleteAll();
 		
 		return "OK";
 		
 	}
 	
 	
 	@GET
     @Path("/deleteall")
     @Produces({ MediaType.TEXT_PLAIN })
     public String deleteAll() {
 		
 		//tickerRepository.deleteAll();
 		tradeStatsRepository.deleteAll();
 		tradeRepository.deleteAll();
 		transactionRepository.deleteAll();
 		userRepository.deleteAll();
 		commentRepository.deleteAll();
 		
 		return "OK";
     
 	}
 	
 	
 	
 	@POST
     @Path("/login")
 	@Consumes({ MediaType.APPLICATION_JSON })
 	@Produces({ MediaType.APPLICATION_JSON })
     public RequestResponse register(@HeaderParam("User-Id") String email, @HeaderParam("password") String password) {
         
 		RequestResponse response = new RequestResponse();
 		
 		User user = userRepository.findByUsername(email);
 		
 		if(user != null) {
 			
 			response.setSuccess(1);
 			
 		} else {
 			
 			user = new User();
 			user.setUsername(email);
 			user.setLive(true);
 			Map<String, Double> funds = new HashMap<String, Double>();
 			funds.put("usd", 0.0);
 			funds.put("ltc", 0.0);
 			funds.put("btc", 0.0);
 			user.setFunds(funds);
 			
 			AutoTradingOptions autoTradingOptions = new AutoTradingOptions();
 			TradeStats tradeStats = new TradeStats();
 			tradeStats.setCurrencyLeft("usd");
 			tradeStats.setCurrencyRight("ltc");
 			tradeStats.setLive(true);
 			tradeStats.setAutoTradingOptions(autoTradingOptions);
 			TickerQuote rate = new TickerQuote();
 			rate.setTime(System.currentTimeMillis()/1000L);
 			rate.setPair("ltc_usd");
 			tradeStats.setRate(rate);
 			tradeStats = tradeStatsRepository.save(tradeStats);
 			user.addTradeStats(tradeStats);
 			user.setCurrentTradeStats(tradeStats);
 			
 			User testUser = new User();
 			testUser.setUsername(email+" (test)");
 			testUser.setLive(false);
 			Map<String, Double> testFunds = new HashMap<String, Double>();
 			testFunds.put("usd", 100.0);
 			testFunds.put("ltc", 100.0);
 			testFunds.put("btc", 100.0);
 			testUser.setFunds(testFunds);
 			
 			AutoTradingOptions testAutoTradingOptions = new AutoTradingOptions();
 			testAutoTradingOptions.setBuyCeiling(1.0);
 			testAutoTradingOptions.setSellFloor(1.0);
 			TradeStats testTradeStats = new TradeStats();
 			testTradeStats.setCurrencyLeft("usd");
 			testTradeStats.setCurrencyRight("ltc");
 			testTradeStats.setLive(false);
 			testTradeStats.setAutoTradingOptions(testAutoTradingOptions);
 			TickerQuote testRate = new TickerQuote();
 			testRate.setBuy(1.0);
 			testRate.setSell(1.0);
 			testRate.setLast(1.0);
 			testRate.setTime(System.currentTimeMillis()/1000L);
 			testRate.setPair("ltc_usd");
 			testTradeStats.setRate(rate);
 			testTradeStats = tradeStatsRepository.save(testTradeStats);
 			testUser.addTradeStats(testTradeStats);
 			testUser.setCurrentTradeStats(testTradeStats);
 			
 			userRepository.save(user);
 			userRepository.save(testUser);
 			
 			response.setSuccess(2);
 			
 		}
 		
 		response.setData(user);
 		
 		return response;
     
 	}
 	
 	
 	@GET
     @Path("/user")
 	@Produces({ MediaType.APPLICATION_JSON })
     public List<User> listUsers(@HeaderParam("User-Id") String userId, @HeaderParam("TradeStats-Id") String tradeStatsId) {
         
 		if(userId != null) {
 			
 			User user = userRepository.findByUsername(userId);
 			
 			if(tradeStatsId != null) {
				user.setCurrentTradeStats(tradeStatsRepository.findOne(tradeStatsId));
 			}
 			
 			List<User> users = new ArrayList<User>();
 			users.add(user);
 			
 			return users;
 		
 		} else {
 			
 			return userRepository.findAll();
 		
 		}
 		
 	}
 	
 	
 	
 }
 
