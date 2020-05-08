 package com.ghlh.autotrade;
 
 import java.util.Date;
 import java.util.List;
 
 import com.ghlh.data.db.StocktradeDAO;
 import com.ghlh.data.db.StocktradeVO;
 import com.ghlh.stockquotes.StockQuotesBean;
 import com.ghlh.strategy.TradeConstants;
 import com.ghlh.strategy.TradeUtil;
 import com.ghlh.strategy.stair.StairConstants;
 import com.ghlh.tradeway.SoftwareTrader;
 
 public class StockTradeIntradyUtil {
 
 	public static void processSell(StockTradeIntradyMonitor monitor,
 			StockQuotesBean sqb) {
 		List possibleSellList = monitor.getPossibleSellList();
 		List pendingBuyList = monitor.getPendingBuyList();
 		for (int i = 0; i < possibleSellList.size(); i++) {
 			StocktradeVO stocktradeVO = (StocktradeVO) possibleSellList.get(i);
			if (stocktradeVO.getStatus() != TradeConstants.STATUS_FINISH) {
 				if (sqb.getHighestPrice() >= stocktradeVO.getSellprice()) {
 					String message = TradeUtil.getConfirmedSellMessage(
 							stocktradeVO.getStockid(),
 							stocktradeVO.getNumber(),
 							stocktradeVO.getSellprice());
 					EventRecorder.recordEvent(StockTradeIntradyUtil.class,
 							message);
 					stocktradeVO.setStatus(TradeConstants.STATUS_FINISH);
 					stocktradeVO.setSelldate(new Date());
 					StocktradeDAO
 							.updateStocktradeFinished(stocktradeVO.getId());
 					if (Boolean.valueOf(monitor.getMonitorstockVO()
 							.getOnmonitoring())) {
 						reBuy(stocktradeVO, pendingBuyList);
 					}
 				}
 			}
 		}
 	}
 
 	private static void reBuy(StocktradeVO stocktradeVO, List pendingBuyList) {
 		String message = TradeUtil.getPendingBuyMessage(
 				stocktradeVO.getStockid(), stocktradeVO.getNumber(),
 				stocktradeVO.getBuyprice());
 		EventRecorder.recordEvent(StockTradeIntradyUtil.class, message);
 
 		TradeUtil.dealBuyStock(stocktradeVO.getStockid(),
 				stocktradeVO.getBuyprice(), stocktradeVO.getSellprice(),
 				stocktradeVO.getTradealgorithm(), stocktradeVO.getNumber(),
 				stocktradeVO.getId());
 		refreshPendingBuyList(stocktradeVO.getStockid(), pendingBuyList);
 	}
 
 	private static void refreshPendingBuyList(String stockId,
 			List pendingBuyList) {
 		int size = pendingBuyList.size();
 		for (int i = 0; i < size; i++) {
 			pendingBuyList.remove(0);
 		}
 
 		List newPendingBuy = StocktradeDAO.getPendingBuyTradeRecords(stockId,
 				StairConstants.STAIR_STRATEGY_NAME);
 		pendingBuyList.addAll(newPendingBuy);
 
 	}
 
 	public static void processBuy(StockTradeIntradyMonitor monitor,
 			StockQuotesBean sqb) {
 		List pendingBuyList = monitor.getPendingBuyList();
 		for (int j = 0; j < pendingBuyList.size(); j++) {
 			StocktradeVO stVO = (StocktradeVO) pendingBuyList.get(j);
			if (stVO.getStatus() != TradeConstants.STATUS_T_0_BUY) {
 				if (sqb.getCurrentPrice() <= stVO.getBuyprice()) {
 					String message = TradeUtil.getConfirmedBuyMessage(monitor
 							.getMonitorstockVO().getStockid(),
 							stVO.getNumber(), stVO.getBuyprice());
 
 					EventRecorder.recordEvent(StockTradeIntradyUtil.class,
 							message);
 					SoftwareTrader.getInstance().buyStock(stVO.getStockid(),
 							stVO.getNumber());
 					StocktradeDAO.updateStocktradeStatus(stVO.getId(),
 							TradeConstants.STATUS_T_0_BUY);
 					stVO.setStatus(TradeConstants.STATUS_T_0_BUY);
 				}
 			}
 		}
 	}
 
 }
