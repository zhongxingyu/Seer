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
 
 package org.investovator.ann.nngaming;
 
 import org.investovator.ann.data.DataRetriever;
 import org.investovator.ann.data.datanormalizing.DataNormalizer;
 import org.investovator.ann.nngaming.util.GameTypes;
 import org.investovator.core.data.api.utils.StockTradingData;
 import org.investovator.core.data.api.utils.TradingDataAttribute;
 
 import java.util.*;
 
 /**
  * @author: Hasala Surasinghe
  * @version: ${Revision}
  */
 public class AnalysisPredictionManager {
 
     private NNPredictor nnPredictor;
     private ArrayList<TradingDataAttribute> attributes;
     private StockTradingData stockTradingData;
     private HashMap<Date,HashMap<TradingDataAttribute,String>> tradingData;
     private double[] inputData;
     private HashMap<TradingDataAttribute,String> tradingValues;
     private DataNormalizer dataNormalizer;
 
     private ArrayList<ArrayList<Object>> graphData;
     private ArrayList<Object> dateArrayList;
     private ArrayList<Object> closingPrices;
     private ArrayList<Object> predictedPrices;
 
 
     public AnalysisPredictionManager(){
 
         nnPredictor = new NNPredictor();
         attributes = new ArrayList<>();
         attributes.add(TradingDataAttribute.HIGH_PRICE);
         attributes.add(TradingDataAttribute.LOW_PRICE);
         attributes.add(TradingDataAttribute.CLOSING_PRICE);
         attributes.add(TradingDataAttribute.SHARES);
         attributes.add(TradingDataAttribute.TRADES);
         attributes.add(TradingDataAttribute.TURNOVER);
 
         graphData = new ArrayList<>();
         dateArrayList = new ArrayList<>();
         closingPrices = new ArrayList<>();
         predictedPrices = new ArrayList<>();
 
     }
 
     public ArrayList<ArrayList<Object>> getGraphData(Date start, Date end, String stockID, GameTypes gameType,double analysisValue,
                                   String analysisStockID){
 
         int tradingAttributeCount = attributes.size();
 
         dataNormalizer = new DataNormalizer(stockID,gameType);
 
         DataRetriever dataRetriever = new DataRetriever();
         stockTradingData = dataRetriever.retrieveTrainingData(stockID,attributes);
 
         tradingData = stockTradingData.getTradingData();
 
         Set<Date> dateSet = dataRetriever.getDates(start,end,stockID,attributes);
 
         inputData = new double[tradingAttributeCount + 1];
 
         for (Iterator<Date> iterator = dateSet.iterator(); iterator.hasNext();) {
             Date next = iterator.next();
 
             dateArrayList.add(next);
 
             tradingValues = tradingData.get(next);
             for(int j = 0; j < tradingAttributeCount; j++){
 
                inputData[j] = Float.valueOf(tradingValues.get(attributes.get(j)));

 
             }
             closingPrices.add(inputData[attributes.indexOf(TradingDataAttribute.CLOSING_PRICE)]);
 
             inputData[tradingAttributeCount] = analysisValue;
 
             int rowCount = inputData.length;
 
             for(int i = 0; i < rowCount - 1; i++){
                 inputData[i] = dataNormalizer.getNormalizedValue(inputData[i],attributes.get(i));
             }
             DataNormalizer dataNormalizer1 = new DataNormalizer(analysisStockID,gameType);
             inputData[rowCount - 1] = dataNormalizer1.getNormalizedValue(inputData[rowCount - 1],
                     TradingDataAttribute.CLOSING_PRICE);
 
             float value = (float) nnPredictor.getPredictedValue(stockID, TradingDataAttribute.CLOSING_PRICE,inputData,gameType);
             value = Float.valueOf(String.format("%.1f", value));
             predictedPrices.add(value);
 
         }
 
         graphData.add(dateArrayList);
         graphData.add(closingPrices);
         graphData.add(predictedPrices);
 
         return graphData;
 
     }
 
 }
