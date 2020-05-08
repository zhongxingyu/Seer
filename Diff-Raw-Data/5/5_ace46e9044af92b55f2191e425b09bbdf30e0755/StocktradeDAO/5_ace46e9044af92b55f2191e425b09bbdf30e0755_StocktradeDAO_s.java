 package com.ghlh.data.db;
 
 import java.util.Date;
 import java.util.List;
 
 import com.ghlh.strategy.TradeConstants;
 
 public class StocktradeDAO {
 
 	public static List getUnfinishedTradeRecords(String stockId, String strategy) {
 		return getTradeRecords(stockId, strategy, 0, true, false);
 	}
 
 	public static List getUnfinishedTradeRecords() {
 		return getTradeRecords(null, null, 0, true, false);
 	}
 
 	public static List getHoldingTradeRecords(String stockId, String strategy) {
 		return getTradeRecords(stockId, strategy,
 				TradeConstants.STATUS_HOLDING, true, false);
 	}
 
 	public static List getIntradyHoldingTradeRecords(String stockId,
 			String strategy) {
 		String sql = "SELECT * FROM stocktrade where  stockId = '" + stockId
				+ "' and tradeAlgorithm = '" + strategy + "' and status = "
 				+ TradeConstants.STATUS_HOLDING + " or status = "
 				+ TradeConstants.STATUS_POSSIBLE_SELL + " or status = "
 				+ TradeConstants.STATUS_T_0_BUY;
		sql += " ORDER BY buyPrice desc ";
 		List result = GhlhDAO.list(sql, "com.ghlh.data.db.StocktradeVO");
 		return result;
 	}
 
 	public static List getT_0_TradeRecords(String stockId, String strategy) {
 		return getTradeRecords(stockId, strategy,
 				TradeConstants.STATUS_T_0_BUY, false, false);
 	}
 
 	public static List getSuccessfulTradeRecords(String stockId, String strategy) {
 		return getTradeRecords(stockId, strategy, TradeConstants.STATUS_SUCCESS,
 				false, false);
 	}
 	
 	public static List getFailedTradeRecords(String stockId, String strategy) {
 		return getTradeRecords(stockId, strategy, TradeConstants.STATUS_FAILURE,
 				false, false);
 	}
 
 	public static List getPossibleSellTradeRecords(String stockId,
 			String strategy) {
 		return getTradeRecords(stockId, strategy,
 				TradeConstants.STATUS_POSSIBLE_SELL, false, false);
 	}
 
 	public static List getPendingBuyTradeRecords(String stockId, String strategy) {
 		return getTradeRecords(stockId, strategy,
 				TradeConstants.STATUS_PENDING_BUY, false, false);
 	}
 
 	public static List getPendingRebuyTradeRecords(String stockId,
 			String strategy) {
 		return getTradeRecords(stockId, strategy,
 				TradeConstants.STATUS_PENDING_BUY, false, true);
 	}
 
 	public static List getOneStockTradeRecords(String stockId, String strategy) {
 		return getTradeRecords(stockId, strategy, 0, false, false);
 	}
 
 	private static List getTradeRecords(String stockId, String strategy,
 			int status, boolean isUnfinished, boolean isRebuy) {
 		String sql = "SELECT * FROM stocktrade";
 		boolean isNeedWhere = true;
 
 		if (stockId != null) {
 			sql = appendConnectWork(sql, isNeedWhere);
 			sql += " stockId = '" + stockId + "'";
 			isNeedWhere = false;
 		}
 		if (strategy != null) {
 			sql = appendConnectWork(sql, isNeedWhere);
 			sql += " tradeAlgorithm = '" + strategy + "'";
 			isNeedWhere = false;
 		}
 
 		if (status != 0) {
 			sql = appendConnectWork(sql, isNeedWhere);
 			sql += " status = " + status;
 			isNeedWhere = false;
 		}
 		if (isUnfinished) {
 			sql = appendConnectWork(sql, isNeedWhere);
 			sql += " isNull(sellDate) ";
 			isNeedWhere = false;
 		}
 		if (isRebuy) {
 			sql = appendConnectWork(sql, isNeedWhere);
 			sql += " NOT ISNULL (previoustradeid) ";
 			isNeedWhere = false;
 		}
 
 		sql += " ORDER BY buyPrice desc ";
 		List result = GhlhDAO.list(sql, "com.ghlh.data.db.StocktradeVO");
 		return result;
 	}
 
 	private static String appendConnectWork(String sql, boolean isNeedWhere) {
 		if (isNeedWhere) {
 			sql += " Where ";
 		} else {
 			sql += " And ";
 		}
 		return sql;
 	}
 
 	public static void updateStocktradeStatus(int id, int status) {
 		StocktradeVO stocktradeVO1 = new StocktradeVO();
 		stocktradeVO1.setId(id);
 		stocktradeVO1.setWhereId(true);
 		stocktradeVO1.setStatus(status);
 		GhlhDAO.edit(stocktradeVO1);
 	}
 
 	public static void updateStocktradeFinished(int id) {
 		StocktradeVO stocktradeVO1 = new StocktradeVO();
 		stocktradeVO1.setId(id);
 		stocktradeVO1.setWhereId(true);
 		stocktradeVO1.setStatus(TradeConstants.STATUS_SUCCESS);
 		stocktradeVO1.setSelldate(new Date());
 		GhlhDAO.edit(stocktradeVO1);
 	}
 	
 		public static void updateStocktradeFailure(int id) {
 		StocktradeVO stocktradeVO1 = new StocktradeVO();
 		stocktradeVO1.setId(id);
 		stocktradeVO1.setWhereId(true);
 		stocktradeVO1.setStatus(TradeConstants.STATUS_FAILURE);
 		stocktradeVO1.setSelldate(new Date());
 		GhlhDAO.edit(stocktradeVO1);
 	}
 
 	public static void removeStocktrade(int id) {
 		StocktradeVO stocktradeVO1 = new StocktradeVO();
 		stocktradeVO1.setId(id);
 		stocktradeVO1.setWhereId(true);
 		GhlhDAO.remove(stocktradeVO1);
 	}
 
 }
