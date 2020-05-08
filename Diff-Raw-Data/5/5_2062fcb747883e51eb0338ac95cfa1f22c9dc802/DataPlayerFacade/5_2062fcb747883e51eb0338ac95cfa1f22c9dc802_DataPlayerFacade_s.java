 /*
  * investovator, Stock Market Gaming Framework
  *     Copyright (C) 2013  investovator
  *
  *     This program is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  *
  *     This program is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  *
  *     You should have received a copy of the GNU General Public License
  *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 
 package org.investovator.dataplaybackengine;
 
 import org.investovator.core.data.api.CompanyStockTransactionsData;
 import org.investovator.core.data.api.utils.StockTradingData;
 import org.investovator.core.data.api.utils.TradingDataAttribute;
 import org.investovator.core.data.exeptions.DataAccessException;
 import org.investovator.core.data.exeptions.DataNotFoundException;
 import org.investovator.dataplaybackengine.exceptions.player.PlayerStateException;
 import org.investovator.dataplaybackengine.player.DailySummaryDataPLayer;
 import org.investovator.dataplaybackengine.player.RealTimeDataPlayer;
 import org.investovator.dataplaybackengine.player.type.PlayerTypes;
 
 import java.util.ArrayList;
 import java.util.Date;
 
 /**
  * @author: ishan
  * @version: ${Revision}
  */
 public class DataPlayerFacade {
     private static DataPlayerFacade facade;
 
     private DailySummaryDataPLayer dailySummaryDataPLayer;
     private RealTimeDataPlayer realTimeDataPlayer;
     private PlayerTypes playerType;
 
    private int DATA_ITEMS_TO_QUERY=10000;
 
     private DataPlayerFacade() {
 
     }
 
     public static synchronized DataPlayerFacade getInstance(){
         if(facade==null){
             facade=new DataPlayerFacade();
         }
 
         return facade;
     }
 
     public void createPlayer(PlayerTypes playerType,String[] stocks,Date startDate,
                                  ArrayList<TradingDataAttribute> attributes,
                                  TradingDataAttribute attributeToMatch, boolean isMultiplayer) {
         //if a daily summary player is needed
         if(playerType==PlayerTypes.DAILY_SUMMARY_PLAYER){
             dailySummaryDataPLayer =new DailySummaryDataPLayer(stocks, attributes, attributeToMatch,isMultiplayer );
             this.dailySummaryDataPLayer.setStartDate(startDate);
             this.playerType=playerType;
         }
         //if a real time data player is needed
         else if(playerType==PlayerTypes.REAL_TIME_DATA_PLAYER){
             realTimeDataPlayer=new RealTimeDataPlayer(stocks,startDate,attributes,attributeToMatch,isMultiplayer);
             this.playerType=playerType;
 
         }
     }
 
     public DailySummaryDataPLayer getDailySummaryDataPLayer() throws PlayerStateException {
         if(dailySummaryDataPLayer !=null){
             return dailySummaryDataPLayer;
         }
         else{
             throw new PlayerStateException("Daily Summary player is not initialized yet.");
         }
     }
 
     public RealTimeDataPlayer getRealTimeDataPlayer() throws PlayerStateException {
         if (realTimeDataPlayer!=null){
             return realTimeDataPlayer;
         }
         else{
             throw new PlayerStateException("Real time data player is not initialized yet.");
         }
     }
 
     public PlayerTypes getCurrentPlayerType(){
         return  this.playerType;
     }
 
     /**
      * Returns data up to the present date of the currently running player
      *
      * @return
      */
     public StockTradingData getDataUpToToday(String symbol,Date startingDate,
                                              ArrayList<TradingDataAttribute> attribute)
             throws DataAccessException, DataNotFoundException {
         if(this.playerType==PlayerTypes.DAILY_SUMMARY_PLAYER){
 
             return this.dailySummaryDataPLayer.getTransactionsDataAPI().getTradingData(
                     CompanyStockTransactionsData.DataType.OHLC,
                    symbol,startingDate,realTimeDataPlayer.getCurrentTime(),DATA_ITEMS_TO_QUERY,attribute);
 
         }
         else if(this.playerType==PlayerTypes.REAL_TIME_DATA_PLAYER){
             return this.realTimeDataPlayer.getTransactionsDataAPI().getTradingData(
                     CompanyStockTransactionsData.DataType.TICKER,
                     symbol,startingDate,realTimeDataPlayer.getCurrentTime(),DATA_ITEMS_TO_QUERY,attribute);
         }
 
         return null;
 
     }
 
 
 }
