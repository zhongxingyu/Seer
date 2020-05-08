 /*
  * investovator, Stock Market Gaming framework
  * Copyright (C) 2013  investovator
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
 
 package org.investovator.core.commons.utils;
 
 import java.util.HashMap;
 
 /**
  * @author rajith
  * @version $Revision$
  */
 public class PortfolioImpl implements Terms, Portfolio {
 
     private String username;
     private double cashBalance;
     private double blockedCashAmount;
     private HashMap <String, HashMap <String, Double>> shares;
 
     public PortfolioImpl(String username, double cashBalance, double blockedCashAmount){
         this.username = username;
         this.cashBalance = cashBalance;
         this.blockedCashAmount = blockedCashAmount;
         this.shares = new HashMap<String, HashMap<String, Double>>();
     }
 
     public PortfolioImpl(String username, double cashBalance, double blockedCashAmount,
                      HashMap <String, HashMap <String, Double>> shares){
         this.username = username;
         this.cashBalance = cashBalance;
         this.blockedCashAmount = blockedCashAmount;
         this.shares = shares;
     }
 
     @Override
     public int compareTo(Portfolio comparePortfolio) {
         return (int) (this.cashBalance - comparePortfolio.getCashBalance());
     }
 
     @Override
     public double getCashBalance() {
         return cashBalance;
     }
 
     @Override
     public void setCashBalance(double cashBalance) {
         this.cashBalance = cashBalance;
     }
 
     @Override
     public void setBlockedCash(double amount) {
        this.blockedCashAmount = amount;
     }
 
     @Override
     public double getBlockedCash() {
         return blockedCashAmount;
     }
 
     @Override
     public HashMap<String, HashMap<String, Double>> getShares() {
         return shares;
     }
 
     @Override
     public void setShares(HashMap<String, HashMap<String, Double>> shares) {
         this.shares = shares;
     }
 
     @Override
     public void removeStock(String symbol){
         shares.remove(symbol);
     }
 
     @Override
     public String getUsername(){
         return username;
     }
 
     /**
      *
      * {@inheritDoc}
      */
     public void boughtShares(String symbol, double quantity, double price){
         if (shares.containsKey(symbol)){
             HashMap<String, Double> stockData = shares.get(symbol);
            double oldQty = stockData.get(QNTY);
            double oldPrice = stockData.get(PRICE);
            /*Average of the prices*/
            double newPrice = ((quantity * price) + (oldQty * oldPrice)) / (quantity + oldQty);

            stockData.put(QNTY, oldQty + quantity);
            stockData.put(PRICE, newPrice);
         } else {
             HashMap<String, Double> stockData = new HashMap<String, Double>();
             stockData.put(QNTY, quantity);
             stockData.put(PRICE, price);
             shares.put(symbol, stockData);
         }
         blockedCashAmount -= price*quantity;
     }
 
     /**
      *
      * {@inheritDoc}
      */
     public void soldShares(String symbol, double quantity, double price){
         if (shares.get(symbol).get(QNTY) == quantity){
             removeStock(symbol);
         } else {
             HashMap<String, Double> stockData = shares.get(symbol);
             stockData.put(QNTY, stockData.get(QNTY) - quantity);
         }
     }
 }
