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
 
 
 package org.investovator.dataplaybackengine.player;
 
 import org.investovator.core.commons.utils.Portfolio;
 import org.investovator.core.commons.utils.PortfolioImpl;
 import org.investovator.core.data.api.CompanyStockTransactionsData;
 import org.investovator.core.data.api.utils.StockTradingData;
 import org.investovator.core.data.api.utils.TradingDataAttribute;
 import org.investovator.core.data.exeptions.DataAccessException;
 import org.investovator.core.data.exeptions.DataNotFoundException;
 import org.investovator.dataplaybackengine.events.PlaybackEventListener;
 import org.investovator.dataplaybackengine.events.StockUpdateEvent;
 import org.investovator.dataplaybackengine.exceptions.*;
 import org.investovator.dataplaybackengine.market.OrderType;
 import org.investovator.dataplaybackengine.market.TradingSystem;
 import org.investovator.dataplaybackengine.scheduler.DailySummaryEventTask;
 import org.investovator.dataplaybackengine.utils.DateUtils;
 
 import java.text.ParseException;
 import java.util.*;
 
 /**
  * @author: ishan
  * @version: ${Revision}
  */
 public class DailySummaryDataPLayer extends DataPlayer {
 
     //amount of money a person get at the begining
     private static int initialCredit=10000;
 
     //max amount of stocks that a person can buy/sell
     private static int maxOrderSize=5000;
 
     //used to determine the cache size
     public static int CACHE_SIZE=100;
 
     //to keep track of the date
     Date today;
     //to keep the game state
     boolean gameStarted;
     //to keep track of the attributes needed
     ArrayList<TradingDataAttribute> attributes;
 
     HashMap<String,Portfolio> userPortfolios;
 
     TradingSystem tradingSystem;
 
     private boolean isMultiplayer;
 
     private DailySummaryEventTask task;
     private Timer timer;
 
 
 
 
 
     //to cache the stock trading data items
    HashMap<String, HashMap<Date, HashMap<TradingDataAttribute, String>>> ohlcDataCache;
 
     public DailySummaryDataPLayer(String[] stocks, ArrayList<TradingDataAttribute> attributes,
                                   TradingDataAttribute attributeToMatch, boolean isMultiplayer) {
 
        this.ohlcDataCache = new HashMap<String, HashMap<Date, HashMap<TradingDataAttribute, String>>>();
         this.attributes = attributes;
         userPortfolios=new HashMap<String, Portfolio>();
         tradingSystem=new TradingSystem(attributes,attributeToMatch);
 
         this.isMultiplayer=isMultiplayer;
 
         if(isMultiplayer){
             task= new DailySummaryEventTask(this);
             this.timer = new Timer();
 
         }
 
 
 
         //initialize the stocks
         for (String stock : stocks) {
             ohlcDataCache.put(stock, new HashMap<Date, HashMap<TradingDataAttribute, String>>());
         }
 
 
         gameStarted = false;
 
     }
 
     /**
      * Starts the game.
      * <p/>
      * Assumption - Assumes that a suitable start date has been set by the setStartDate() method.
      *
      * @return list of events needed to start the game
      * @throws GameAlreadyStartedException
      */
     public StockUpdateEvent[] startGame() throws GameAlreadyStartedException {
 
         ArrayList<StockUpdateEvent> events = new ArrayList<StockUpdateEvent>();
         //if the game has not started yet
         if (!gameStarted) {
             //search all the stocks
             for (String stock : ohlcDataCache.keySet()) {
 
                 try {
                     StockTradingData data = transactionDataAPI.getTradingData(CompanyStockTransactionsData.DataType.OHLC,
                             stock, today,new Date(), DailySummaryDataPLayer.CACHE_SIZE,attributes);
 
                     //if any data was returned
                     if (data != null) {
                         //get the relevant data
                         events.add(new StockUpdateEvent(stock, data.getTradingDataEntry(today), today));
 
                         //add the data to the Trading system as well
                         //only add if price information exists
                         if(data.getTradingDataEntry(today)!=null){
                             tradingSystem.updateStockPrice(stock,data.getTradingDataEntry(today));
                         }
                         //remove that entry from map
                         data.getTradingData().remove(today);
 
                         //add the rest of the data to the cache
                         ohlcDataCache.put(stock, data.getTradingData());
 
                     }
 
                 } catch (DataNotFoundException e) {
                     e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                 } catch (DataAccessException e) {
                     e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                 }
             }
 
             //indicate that the game started
             gameStarted=true;
 
 
         } else {
             throw new GameAlreadyStartedException(this);
         }
 
         today=DateUtils.incrementTimeByDays(1,today);
         return events.toArray(new StockUpdateEvent[events.size()]);
 
     }
 
     /**
      * Used to set the game starting date at the start of the game.
      *
      * @param startDate
      * @throws ParseException
      */
     public void setStartDate(Date startDate) {
         this.today = startDate;
 
     }
 
     /**
      * Used to set the game starting date at the start of the game.
      *
      * @param startDate
      * @param dateFormat
      * @throws ParseException
      */
     public void setStartDate(String startDate, String dateFormat ) throws ParseException {
         today = DateUtils.dateStringToDateObject(startDate, dateFormat);
 
     }
 
     /**
      * returns the  stock events in the next day. If a stock does not contain the price information
      * for the requested day, its <b>data</> map will be null.
      *
      * @return An array of StockUpdateEvent's if the data is present for at least a single stock. If data is not present
      * for any stock, returns a null.
      */
     public StockUpdateEvent[] playNextDay() throws GameFinishedException {
         ArrayList<StockUpdateEvent> events = new ArrayList<StockUpdateEvent>();
 
         //to track data availability
         boolean dataExists=false;
 
         //iterate all the stocks
         for(String stock:ohlcDataCache.keySet()) {
             //if the date is in the cache
 
             //to check the data avalability ofr this stock
             boolean hasDAtaForStock=false;
             if (ohlcDataCache.get(stock).containsKey(today)) {
                 events.add(new StockUpdateEvent(stock,ohlcDataCache.get(stock).get(today),today));
 
                 //add the data to the Trading system as well
                 tradingSystem.updateStockPrice(stock,ohlcDataCache.get(stock).get(today));
 
                 //remove the used items from the cache
                 ohlcDataCache.get(stock).remove(today);
 
                 hasDAtaForStock=true;
 
 
             }
             else{
                 events.add(new StockUpdateEvent(stock,null,today));
             }
 
             //if at least a single stock has data
             if(hasDAtaForStock){
                 dataExists=true;
             }
 
         }
 
         //if no data has been found in the cache
         if (!dataExists) {
             events.clear();
 
             //to track whether at least a single stock has data in the future
             boolean hasData=false;
 
             //search all the stocks
             for (String stock : ohlcDataCache.keySet()) {
 
                 try {
 
                     StockTradingData data = transactionDataAPI.getTradingData(CompanyStockTransactionsData.DataType.OHLC,
                             stock, today, new Date(), DailySummaryDataPLayer.CACHE_SIZE,attributes);
 
                     //if any data was returned
                     if (data != null && data.getTradingData()!=null) {
                         HashMap<TradingDataAttribute,String> dataMap= data.getTradingDataEntry(today);
                         //get the relevant data
                         events.add(new StockUpdateEvent(stock,dataMap , today));
 
                         //add the data to the Trading system as well
                         if(dataMap!=null){
                             tradingSystem.updateStockPrice(stock,dataMap);
                         }
 
                         //remove that entry from map
                         data.getTradingData().remove(today);
 
                         //clear the cache first
                         ohlcDataCache.remove(stock);
                         //add the rest of the data to the cache
                         ohlcDataCache.put(stock, data.getTradingData());
 
                         hasData=true;
 
                     }
 
                 } catch (DataNotFoundException e) {
                     //TODO - change this exception handling code to act on whatever the exception that the
                     //core module will throw when no data is present for a given stock from the given time
                     //onwards.
                     //TODO - catch that exception before the DataAccessException ;)
 
                     ///////
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                 } catch (DataAccessException e) {
                     hasData=false;
                 }
             }
 
             //if none of the stocks has future data
             if(!hasData){
                 throw new GameFinishedException("OHLC");
             }
 
         }
 
         //if no data has been found from the DB query too
         if(events.size()==0){
             return null;
         }
 
         today=DateUtils.incrementTimeByDays(1,today);
         return events.toArray(new StockUpdateEvent[events.size()]);
 
     }
 
 
     /**
      * Be cautious enough to call this method before playNextDay() has been called.
      * Else it will show the next day
      *
      * @return next day in the game
      */
     public Date getNextDay(){
         return today;
     }
 
     /**
      *
      * @return current day in the game
      */
     public Date getToday(){
         return DateUtils.decrementTimeByDays(1,today);
     }
 
     /**
      * Allows a user to join the running game
      *
      * @return
      */
     public boolean joinMultiplayerGame(PlaybackEventListener observer,String userName)
             throws UserAlreadyJoinedException {
 
         boolean joined=false;
 
         //check whether the user has already joined the game
         if(!userPortfolios.containsKey(userName)){
             userPortfolios.put(userName,new PortfolioImpl(userName, DailySummaryDataPLayer.initialCredit,0));
             joined=true;
             setObserver(observer);
         }
         else{
             throw new UserAlreadyJoinedException(userName);
         }
 
 
         return joined;
 
     }
 
     /**
      * Allows a user to join the running game
      *
      * @return
      */
     public boolean joinSingleplayerGame(String userName) throws UserAlreadyJoinedException {
 
 
         boolean joined=false;
 
         //check whether the user has already joined the game
         if(!userPortfolios.containsKey(userName)){
             userPortfolios.put(userName,new PortfolioImpl(userName, DailySummaryDataPLayer.initialCredit,0));
             joined=true;
 
         }
         else{
             throw new UserAlreadyJoinedException(userName);
         }
 
 
         return joined;
 
     }
 
     public boolean executeOrder(String stockId, int quantity, OrderType side,String userName)
             throws InvalidOrderException,
             UserJoinException {
 
         //order validity checks
         if(quantity> DailySummaryDataPLayer.maxOrderSize){
             throw new InvalidOrderException("Cannot place more than "+ DailySummaryDataPLayer.maxOrderSize+" orders.");
         }
         if(!ohlcDataCache.containsKey(stockId)){
             throw new InvalidOrderException("Invalid stock ID : "+stockId);
         }
 
         //if the user has not joined the game
         if(!userPortfolios.containsKey(userName)){
             throw new UserJoinException("User "+userName+ " has not joined the game");
 
         }
 
 
         float executedPrice= tradingSystem.executeOrder(stockId,quantity,userPortfolios.get(userName).getCashBalance());
 
         //update the cash balance
         if(side==OrderType.BUY){
             userPortfolios.get(userName).boughtShares(stockId,quantity,executedPrice);
         }
         else if(side==OrderType.SELL){
             userPortfolios.get(userName).soldShares(stockId,quantity,executedPrice);
         }
         return true;
 
 
     }
 
     public Portfolio getMyPortfolio(String userName) throws UserJoinException {
 
         //if the user has not joined the game
         if(!userPortfolios.containsKey(userName)){
             throw new UserJoinException("User "+userName+ " has not joined the game");
 
         }
 
         return userPortfolios.get(userName);
     }
 
     /**
      * To set observers
      *
      * @param observer
      */
     private void setObserver(PlaybackEventListener observer){
         task.setObserver(observer);
     }
 
     public boolean isGameStarted() {
         return gameStarted;
     }
 
     /**
      * Start playing the data
      * @param resolution the time gaps between pushing events
      */
     public void startMultiplayerGame(int resolution) {
 
         timer.schedule(task, 0, resolution * 1000);
     }
 
     /**
      * Stop the data playback
      */
     public void stopPlayback() {
         if (isMultiplayer()){
             task.cancel();
             timer.cancel();
         }
     }
 
     public void setTransactionDataAPI(CompanyStockTransactionsData api){
         this.transactionDataAPI=api;
 
     }
 
     /**
      * returns whether this is a multplayer game or not
      * @return
      */
     public boolean isMultiplayer() {
         return isMultiplayer;
     }
 
     public CompanyStockTransactionsData getTransactionsDataAPI(){
         return this.transactionDataAPI;
     }
 
     /**
      * Informs whether the given user has already joined the current game
      * @param name
      * @return
      */
     public boolean hasUserJoined(String name){
         return userPortfolios.containsKey(name);
     }
 
 
 }
