 package com.myrontuttle.fin.trade.strategies;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.joda.time.DateTime;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.myrontuttle.fin.trade.api.AlertService;
 import com.myrontuttle.fin.trade.api.AvailableAlert;
 import com.myrontuttle.fin.trade.api.AvailableStrategyParameter;
 import com.myrontuttle.fin.trade.api.PortfolioService;
 import com.myrontuttle.fin.trade.api.QuoteService;
 import com.myrontuttle.fin.trade.api.SelectedAlert;
 import com.myrontuttle.fin.trade.api.TradeStrategyService;
 
 /**
  * Provides a trade manager with a bounded trade policy:
  * When a trade is opened a box is drawn to bound the trade.  Think of a time/price chart.
  * The left side of the box is the time the trade opened
  * The right side of the box is the time the trade will close if nothing else happens
  * The bottom and top of the box is when we close the trade
  * 
  * @author Myron Tuttle
  */
 public class BoundedStrategy {
 	
 	private static final Logger logger = LoggerFactory.getLogger( BoundedStrategy.class );
 	
 	public final static String NAME = "Bounded";
 	public final static String DESCRIPTION = "Creates bounds around a trade to exit after a certain time or " +
 			"percent gain/loss.";
 	
 	public final static String OPEN = "OpenTrade";
 	public final static String CLOSE = "CloseTrade";
 
 	public final static String OPEN_ORDER = "openOrderType";
 	public final static String TRADE_ALLOC = "tradeAllocation";
 	public final static String PERCENT_BELOW = "percentBelow";
 	public final static String TIME_LIMIT = "timeLimit";
 	public final static String PERCENT_ABOVE = "percentAbove";
 	public final static String UPPER = "Upper";
 	public final static String LOWER = "Lower";
 
 	public final static int OPEN_ORDER_LOWER = 0;
 	public final static int OPEN_ORDER_UPPER = 0;
 	public final static int TRADE_ALLOC_LOWER = 0;
 	public final static int TRADE_ALLOC_UPPER = 100;
 	public final static int PERCENT_BELOW_LOWER = 0;
 	public final static int PERCENT_BELOW_UPPER = 100;
 	public final static int TIME_LIMIT_LOWER = 60;
 	public final static int TIME_LIMIT_UPPER = 60*60*24;
 	public final static int PERCENT_ABOVE_LOWER = 0;
 	public final static int PERCENT_ABOVE_UPPER = 100;
 
 	private static AvailableStrategyParameter openOrderType = new AvailableStrategyParameter(OPEN_ORDER, 
 			OPEN_ORDER_LOWER,
 			OPEN_ORDER_UPPER);	
 	private static AvailableStrategyParameter tradeAllocation = new AvailableStrategyParameter(TRADE_ALLOC, 
 			TRADE_ALLOC_LOWER,
 			TRADE_ALLOC_UPPER);
 	private static AvailableStrategyParameter percentBelow = new AvailableStrategyParameter(PERCENT_BELOW,
 			PERCENT_BELOW_LOWER,
 			PERCENT_BELOW_UPPER);
 	private static AvailableStrategyParameter timeLimit = new AvailableStrategyParameter(TIME_LIMIT,
 			TIME_LIMIT_LOWER,
 			TIME_LIMIT_UPPER);
 	private static AvailableStrategyParameter percentAbove = new AvailableStrategyParameter(PERCENT_ABOVE, 
 			PERCENT_ABOVE_LOWER,
 			PERCENT_ABOVE_UPPER);
 	
 	public static AvailableStrategyParameter[] availableParameters = new AvailableStrategyParameter[]{
 			openOrderType,
 			tradeAllocation,
 			percentBelow,
 			timeLimit,
 			percentAbove
 	};
 	
 	public static String[] availableTradeActions = new String[]{
 		OPEN,
 		CLOSE
 	};
 
 	public static AvailableStrategyParameter[] availableParameters() {
 		return availableParameters;
 	}
 
 	public void setParameterLimits(HashMap<String, Integer> limits) {
 		if (limits == null || limits.values().isEmpty()) {
 			logger.warn("No limits set for Bounded With Adjust Strategy. Using defaults");
 			return;
 		}
 		if (limits.containsKey(OPEN_ORDER + LOWER)) {
 			openOrderType.setLower(limits.get(OPEN_ORDER + LOWER));
 		}
 		if (limits.containsKey(OPEN_ORDER + UPPER)) {
 			openOrderType.setUpper(limits.get(OPEN_ORDER + UPPER));
 		}
 		if (limits.containsKey(TRADE_ALLOC + LOWER)) {
 			tradeAllocation.setLower(limits.get(TRADE_ALLOC + LOWER));
 		}
 		if (limits.containsKey(TRADE_ALLOC + UPPER)) {
 			tradeAllocation.setUpper(limits.get(TRADE_ALLOC + UPPER));
 		}
 		if (limits.containsKey(PERCENT_BELOW + LOWER)) {
 			percentBelow.setLower(limits.get(PERCENT_BELOW + LOWER));
 		}
 		if (limits.containsKey(PERCENT_BELOW + UPPER)) {
 			percentBelow.setUpper(limits.get(PERCENT_BELOW + UPPER));
 		}
 		if (limits.containsKey(TIME_LIMIT + LOWER)) {
 			timeLimit.setLower(limits.get(TIME_LIMIT + LOWER));
 		}
 		if (limits.containsKey(TIME_LIMIT + UPPER)) {
 			timeLimit.setUpper(limits.get(TIME_LIMIT + UPPER));
 		}
 		if (limits.containsKey(PERCENT_ABOVE + LOWER)) {
 			percentAbove.setLower(limits.get(PERCENT_ABOVE + LOWER));
 		}
 		if (limits.containsKey(PERCENT_ABOVE + UPPER)) {
 			percentAbove.setUpper(limits.get(PERCENT_ABOVE + UPPER));
 		}
 	}
 
 	public static void takeAction(Event event, Trade t, 
 			PortfolioService portfolioService, QuoteService quoteService,
 			AlertService alertService, TradeStrategyService tradeStrategyService) throws Exception {
 		logger.trace("Taking action for Bounded Strategy");
 		String actionType = event.getActionType();
 		if (actionType.equals(OPEN)) {
 			openTrade(t, portfolioService, quoteService, alertService, tradeStrategyService);
 		} else if (actionType.equals(CLOSE)) {
 			closeTrade(t, portfolioService, alertService, tradeStrategyService);
 		}
 		logger.trace("Finished action for Bounded Strategy");
 	}
 	
 	public static String[] describeTrade(Trade trade, PortfolioService portfolioService) throws Exception {
 		Map<String, Integer> parameters = trade.getParameters();
 		if (parameters == null || parameters.size() == 0) {
 			return new String[0];
 		}
 
 		String userId = trade.getUserId();
 		String[] desc = new String[4];
 		desc[0] = "If an alert fires for " + trade.getSymbol() + " then " + portfolioService.
 				openOrderTypesAvailable(userId)[parameters.get(OPEN_ORDER)] + " it with " +
 				parameters.get(TRADE_ALLOC) + "% of portfolio.";
 		
 		desc[1] = "If the price of " + trade.getSymbol() + " falls below " + parameters.get(PERCENT_BELOW) +
 				"% then " + portfolioService.closeOrderTypesAvailable(userId)[parameters.get(OPEN_ORDER)] + 
 				" it.";
 		
 		desc[2] = "If the price of " + trade.getSymbol() + " rises above " + parameters.get(PERCENT_ABOVE) +
 				"% then " + portfolioService.closeOrderTypesAvailable(userId)[parameters.get(OPEN_ORDER)] + 
 				" it.";
 		
 		desc[3] = "If the position in " + trade.getSymbol() + " lasts longer than " + 
 				parameters.get(TIME_LIMIT) + " seconds then " + 
 				portfolioService.closeOrderTypesAvailable(userId)[parameters.get(OPEN_ORDER)] + 
 				" it.";
 				
 		return desc;
 	}
 
 	protected static void openTrade(Trade trade, 
 			PortfolioService portfolioService,
 			QuoteService quoteService,
 			AlertService alertService,
 			TradeStrategyService tradeStrategyService) throws Exception {
 		logger.trace("Opening trade: {}", trade.getTradeId());
 		if (portfolioService.openOrderTypesAvailable(trade.getUserId()).length != 
 				portfolioService.closeOrderTypesAvailable(trade.getUserId()).length) {
 			throw new Exception("Open and close order types must match.  Trade not made.");
 		}
 		Map<String, Integer> tradeParams = trade.getParameters();
 		
 		String openOrderType = portfolioService.
 									openOrderTypesAvailable(trade.getUserId())[tradeParams.get(OPEN_ORDER)];
 		
 
 		double portfolioBalance = portfolioService.getAvailableBalance(trade.getUserId(), 
 																	trade.getPortfolioId());
 		double maxTradeAmount = portfolioBalance * (tradeParams.get(TRADE_ALLOC) / 100.0);
 		double currentPrice = quoteService.getLast(trade.getUserId(), trade.getSymbol());
 
 		if (currentPrice <= maxTradeAmount) {
 			
 			// Open position
 			int quantity = (int)Math.floor(maxTradeAmount / currentPrice);
 			portfolioService.openPosition(trade.getUserId(), trade.getPortfolioId(), trade.getSymbol(), 
 											quantity, openOrderType);
 
 			// stop loss
 			createStopTrade(trade, currentPrice, true, alertService, tradeStrategyService);
 			
 			// time in trade
 			createTimeLimit(trade, tradeParams.get(TIME_LIMIT), tradeStrategyService);
 			
 			// capture profits
 			createStopTrade(trade, currentPrice, false, alertService, tradeStrategyService);
 			
 		} else {
 			throw new Exception("Not enough allocated to trade " + trade.getSymbol() +
 					". Current Price(" + currentPrice + ") > Max Allowed Trade Amount (" + 
 					maxTradeAmount + ")");
 			
 		}
 		logger.trace("Finished opening trade: {}", trade.getTradeId());
 	}
 
 	protected static void closeTrade(Trade trade, 
 			PortfolioService portfolioService,
 			AlertService alertService,
 			TradeStrategyService tradeStrategyService) throws Exception {
 		logger.trace("Closing trade: {}", trade.getTradeId());
 		if (tradeStrategyService.tradeExists(trade.getTradeId())) {
 			
			String closeOrderType = portfolioService.closeOrderTypesAvailable(
										trade.getUserId())[trade.getParameter(OPEN_ORDER)];
 			portfolioService.closePosition(trade.getUserId(), 
					trade.getPortfolioId(), trade.getSymbol(), 0, 
					closeOrderType);
 			
 			deleteTradeAlerts(trade, tradeStrategyService, alertService);
 			
 			tradeStrategyService.removeTrade(trade.getTradeId());
 		} else {
 			throw new Exception("Trade " + trade.getTradeId() + " already closed.");
 		}
 		logger.trace("Finished closing trade: {}", trade.getTradeId());
 	}
 	
 	protected static void deleteTradeAlerts(Trade trade, TradeStrategyService tradeStrategyService, 
 											AlertService alertService) throws Exception {
 
 		try {
 			for(Event e : trade.getEvents()) {
 				if (!e.getTrigger().equals(StrategyService.MOMENT_PASSED)) {
 					alertService.removeAlert(trade.getAlertUserId(), e.getTrigger());
 				}
 			}
 		} catch (Exception e) {
 			throw new Exception("Unable to remove alerts for user: " + trade.getAlertUserId());
 		}
 	}
 	
 	protected static void createStopTrade(Trade trade, double currentPrice, boolean priceRiseGood,
 					AlertService alertService, TradeStrategyService tradeStrategyService) throws Exception {
 		logger.trace("Creating stopTrade for {}", trade.getTradeId());
 		String alertUserId = trade.getAlertUserId();
 		
 		AvailableAlert alertWhen = (priceRiseGood) ? alertService.getPriceBelowAlert(alertUserId) :
 										alertService.getPriceAboveAlert(alertUserId);
 		String alertParam = (priceRiseGood) ? PERCENT_BELOW : PERCENT_ABOVE;
 		
 		double priceDiff = currentPrice - (trade.getParameters().get(alertParam) / 100.0) * currentPrice;
 		SelectedAlert stopLossAlert = new SelectedAlert(alertWhen.getId(),
 														alertWhen.getCondition(),
 														trade.getSymbol(), 
 														priceDiff);
 		String[] alertIds = alertService.setupAlerts(alertUserId, stopLossAlert);
 		for(String alertId : alertIds) {
 			String event = alertService.parseCondition(alertWhen, trade.getSymbol(), priceDiff);
 			tradeStrategyService.setTradeEvent(trade.getTradeId(), event, CLOSE, alertId);
 		}
 		logger.trace("Finished creating stopTrade for {}", trade.getTradeId());
 	}
 	
 	protected static void createTimeLimit(Trade trade, int time, 
 			TradeStrategyService tradeStrategyService) {
 		
 		DateTime dt = new DateTime().plusSeconds(time);
 		tradeStrategyService.setTradeEvent(trade.getTradeId(), dt.toString(),
 											CLOSE, StrategyService.MOMENT_PASSED);
 	}
 }
