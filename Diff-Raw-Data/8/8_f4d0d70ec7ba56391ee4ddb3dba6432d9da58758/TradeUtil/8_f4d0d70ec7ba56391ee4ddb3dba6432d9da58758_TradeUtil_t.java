 package com.ghlh.strategy;
 
 import java.util.Date;
 
 import org.apache.log4j.Logger;
 
 import com.common.util.IDGenerator;
 import com.ghlh.data.db.GhlhDAO;
 import com.ghlh.data.db.StocktradeVO;
 import com.ghlh.stockquotes.StockQuotesBean;
 import com.ghlh.tradeway.SoftwareTrader;
 
 public class TradeUtil {
 
 	private static Logger logger = Logger.getLogger(TradeUtil.class);
 
 	public static void dealBuyStock(String stockId, double tradeMoney,
 			double basePrice, double sellPrice, String strategy) {
 		int number = getTradeNumber(tradeMoney, basePrice);
 		dealBuyStock(stockId, basePrice, sellPrice, strategy, number);
 	}
 
 	public static void dealBuyStock(BuyStockBean buyStockBean) {
 		if (buyStockBean.getNumber() == 0) {
 			int number = getTradeNumber(buyStockBean.getTradeMoney(),
 					buyStockBean.getBuyPrice());
 			buyStockBean.setNumber(number);
 		}
 		dealBuyStockWith2Status(buyStockBean);
 	}
 
 	public static boolean isStopTrade(StockQuotesBean sqb) {
		return sqb.getCurrentPrice() == 0;
 	}
 
 	public static void dealBuyStockSuccessfully(String stockId,
 			double tradeMoney, double basePrice, double sellPrice,
 			String strategy) {
 		int number = getTradeNumber(tradeMoney, basePrice);
 		dealBuyStockSuccessfully(stockId, basePrice, sellPrice, strategy,
 				number);
 	}
 	
 	public static void dealBuyStockSuccessfully(BuyStockBean buyStockBean) {
 		if (buyStockBean.getNumber() == 0) {
 			int number = getTradeNumber(buyStockBean.getTradeMoney(),
 					buyStockBean.getBuyPrice());
 			buyStockBean.setNumber(number);
 		}
 		buyStockBean.setConfirm(true);
 		dealBuyStockWith2Status(buyStockBean);
 	}
 
 	public static int getTradeNumber(double tradeMoney, double basePrice) {
 		int number = (int) (tradeMoney / basePrice);
 		number = ((int) (number / 100)) * 100;
 		return number;
 	}
 
 	public static void dealBuyStock(String stockId, double basePrice,
 			double sellPrice, String strategy, int number) {
 		dealBuyStockWith2Status(stockId, basePrice, sellPrice, strategy,
 				number, false, 0);
 	}
 
 	public static void dealBuyStock(String stockId, double basePrice,
 			double sellPrice, String strategy, int number, int previousTradeId) {
 		dealBuyStockWith2Status(stockId, basePrice, sellPrice, strategy,
 				number, false, previousTradeId);
 	}
 
 	public static void dealBuyStockSuccessfully(String stockId,
 			double basePrice, double sellPrice, String strategy, int number) {
 		dealBuyStockWith2Status(stockId, basePrice, sellPrice, strategy,
 				number, true, 0);
 
 	}
 
 	private static void dealBuyStockWith2Status(String stockId,
 			double basePrice, double sellPrice, String strategy, int number,
 			boolean isConfirm, int previousTradeId) {
 		BuyStockBean buyStockBean = new BuyStockBean();
 		buyStockBean.setStockId(stockId);
 		buyStockBean.setBuyPrice(basePrice);
 		buyStockBean.setWinSellPrice(sellPrice);
 		buyStockBean.setStrategy(strategy);
 		buyStockBean.setNumber(number);
 		buyStockBean.setConfirm(isConfirm);
 		buyStockBean.setPreviousTradeId(previousTradeId);
 		dealBuyStockWith2Status(buyStockBean);
 	}
 
 	private static void dealBuyStockWith2Status(BuyStockBean buyStockBean) {
 		StocktradeVO stocktradeVO1 = new StocktradeVO();
 		stocktradeVO1.setId(IDGenerator.generateId("stocktrade"));
 		stocktradeVO1.setStockid(buyStockBean.getStockId());
 		stocktradeVO1.setTradealgorithm(buyStockBean.getStrategy());
 		stocktradeVO1.setBuydate(new Date());
 		stocktradeVO1.setBuybaseprice(buyStockBean.getBuyPrice());
 		stocktradeVO1.setBuyprice(buyStockBean.getBuyPrice());
 		stocktradeVO1.setNumber(buyStockBean.getNumber());
 		stocktradeVO1.setWinsellprice(buyStockBean.getWinSellPrice());
 		stocktradeVO1.setLostsellprice(buyStockBean.getLostSellPrice());
 		stocktradeVO1.setCreatedtimestamp(new Date());
 		stocktradeVO1.setLastmodifiedtimestamp(new Date());
 		if (buyStockBean.getPreviousTradeId() > 0) {
 			stocktradeVO1.setPrevioustradeid(buyStockBean.getPreviousTradeId());
 		}
 		if (buyStockBean.isConfirm()) {
 			stocktradeVO1.setStatus(TradeConstants.STATUS_T_0_BUY);
 			SoftwareTrader.getInstance().buyStock(buyStockBean.getStockId(),
 					buyStockBean.getNumber());
 		} else {
 			stocktradeVO1.setStatus(TradeConstants.STATUS_PENDING_BUY);
 		}
 		GhlhDAO.create(stocktradeVO1);
 	}
 
 	public static void dealSell(StocktradeVO stocktradeVO) {
 		// SoftwareTrader.getInstance().sellStock(stocktradeVO.getStockid(),
 		// stocktradeVO.getNumber(), stocktradeVO.getWinsellprice());
 		StocktradeVO stocktradeVO1 = new StocktradeVO();
 		stocktradeVO1.setId(stocktradeVO.getId());
 		stocktradeVO1.setWhereId(true);
 		stocktradeVO1.setStatus(TradeConstants.STATUS_POSSIBLE_SELL);
 		GhlhDAO.edit(stocktradeVO1);
 	}
 
 	public static String getOpenPriceBuyMessage(String stockId, int number,
 			double price) {
 		return getEventMessage(stockId, number, price, "buy", false, PRICE_OPEN);
 	}
 
 	public static String getIntradyPriceBuyMessage(String stockId, int number,
 			double price, int priceType) {
 		return getEventMessage(stockId, number, price, "buy", false, priceType);
 	}
 
 	public static String getPendingSellMessage(String stockId, int number,
 			double price) {
 		return getEventMessage(stockId, number, price, "sell", true, 0);
 	}
 
 	public static String getPendingSellMessage(String stockId, int number,
 			double winPrice, double lostPrice) {
 		return getEventMessage(stockId, number, winPrice, "sell", true, 0,
 				lostPrice);
 	}
 
 	public static String getConfirmedSellMessage(String stockId, int number,
 			double price) {
 		return getEventMessage(stockId, number, price, "sell", false, 0);
 	}
 
 	public static String getPendingBuyMessage(String stockId, int number,
 			double price) {
 		return getEventMessage(stockId, number, price, "buy", true, 0);
 	}
 
 	public static String getConfirmedBuyMessage(String stockId, int number,
 			double price) {
 		return getEventMessage(stockId, number, price, "buy", false, 0);
 	}
 
 	public static final int PRICE_OPEN = 1;
 	public static final int PRICE_CLOSE = 2;
 	public static final int PRICE_NOON = 3;
 	public static final int PRICE_10 = 10;
 
 	private static String getEventMessage(String stockId, int number,
 			double price, String cmd, boolean isPending, int priceType) {
 		return getEventMessage(stockId, number, price, cmd, isPending,
 				priceType, 0);
 	}
 
 	private static String getEventMessage(String stockId, int number,
 			double winPrice, String cmd, boolean isPending, int priceType,
 			double lostPrice) {
 		String message = "";
 		if (cmd.equals("buy")) {
 			message += "";
 		} else {
 			message += "";
 		}
 		message += "Ʊ:" + stockId;
 		if (isPending) {
 			message += " Ԥµ";
 		} else {
 			message += " ɽ";
 		}
 		message += " :" + number;
 		if (lostPrice == 0) {
 			message += " ۸:" + winPrice;
 		} else {
 			message += " ۸:" + winPrice + "(ֹӯ)" + " " + lostPrice + "(ֹ)";
 		}
 		switch (priceType) {
 		case PRICE_OPEN:
 			message += "(̼)";
 			break;
 		case PRICE_CLOSE:
 			message += "(̼)";
 			break;
 		case PRICE_NOON:
 			message += "(̼)";
 			break;
 		case PRICE_10:
 			message += "(10 ̼)";
 			break;
 
 		default:
 		}
 		return message;
 	}
 
 	public static String getAfterCloseEventMessage(StocktradeVO stVO) {
 		String result = null;
 		if (stVO.getStatus() == TradeConstants.STATUS_POSSIBLE_SELL) {
 			result = "ԤµƱ:" + stVO.getStockid() + "δɹ״̬ûس";
 		}
 		if (stVO.getStatus() == TradeConstants.STATUS_T_0_BUY) {
 			result = "µƱ:" + stVO.getStockid() + "ɹ״̬ûس";
 		}
 		if (stVO.getStatus() == TradeConstants.STATUS_PENDING_BUY) {
 			result = "ԤµƱ:" + stVO.getStockid() + "δɹ׼¼ɾ";
 		}
 
 		return result;
 	}
 
 	public static int getPriceType(String buyPriceStrategy) {
 		if (buyPriceStrategy.equals("̼")) {
 			return PRICE_OPEN;
 		}
 		if (buyPriceStrategy.equals("̼")) {
 			return PRICE_NOON;
 		}
 		return 0;
 	}
 }
