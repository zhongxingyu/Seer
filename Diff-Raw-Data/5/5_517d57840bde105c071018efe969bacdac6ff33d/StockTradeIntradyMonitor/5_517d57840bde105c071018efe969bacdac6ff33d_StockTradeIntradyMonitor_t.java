 package com.ghlh.autotrade;
 
 import java.util.List;
 
 import com.ghlh.data.db.MonitorstockVO;
 import com.ghlh.data.db.StocktradeDAO;
 import com.ghlh.data.db.StocktradeVO;
 import com.ghlh.stockquotes.StockQuotesBean;
 import com.ghlh.strategy.MonitoringStrategy;
 import com.ghlh.strategy.OneTimeStrategy;
 import com.ghlh.strategy.TradeUtil;
 import com.ghlh.strategy.stair.StairConstants;
 import com.ghlh.util.ReflectUtil;
 
 public class StockTradeIntradyMonitor {
 	public StockTradeIntradyMonitor(MonitorstockVO monitorstockVO,
 			List possibleSellList, List pendingBuyList) {
 		this.monitorstockVO = monitorstockVO;
 		this.possibleSellList = possibleSellList;
 		this.pendingBuyList = pendingBuyList;
 		this.monitorStrategy = (MonitoringStrategy) ReflectUtil
 				.getClassInstance("com.ghlh.strategy",
 						monitorstockVO.getTradealgorithm(), "IntradayStrategy");
 
 	}
 
 	private MonitoringStrategy monitorStrategy;
 
 	private MonitorstockVO monitorstockVO;
 
 	public MonitorstockVO getMonitorstockVO() {
 		return monitorstockVO;
 	}
 
 	public void setMonitorstockVO(MonitorstockVO monitorstockVO) {
 		this.monitorstockVO = monitorstockVO;
 	}
 
 	public List getPendingBuyList() {
 		return pendingBuyList;
 	}
 
 	public void setPendingBuyList(List pendingBuyList) {
 		this.pendingBuyList = pendingBuyList;
 	}
 
 	private List possibleSellList;
 
 	public List getPossibleSellList() {
 		return possibleSellList;
 	}
 
 	public void setPossibleSellList(List possibleSellList) {
 		this.possibleSellList = possibleSellList;
 	}
 
 	private List pendingBuyList;
 
 	public void processSell(StockQuotesBean sqb) {
		monitorStrategy.processSell(this, sqb);
 	}
 
 	public void processBuy(StockQuotesBean sqb) {
		monitorStrategy.processBuy(this, sqb);
 	}
 
 }
