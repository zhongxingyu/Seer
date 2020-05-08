 /**
  * 
  */
 
 package com.zygon.exchange.market.model.indication.logic.message;
 
 /**
  *
  * @author zygon
  */
 public class MACDZeroCross extends MACD {
 
     private final boolean crossAboveZero;
     
    public MACDZeroCross(String tradableIdentifier, String id, long timestamp, boolean crossAboveZero) {
         super(tradableIdentifier, id, timestamp, IndicationType.ZERO_CROSS);
         
        this.crossAboveZero = crossAboveZero;
     }
 
     public boolean crossAboveZero() {
         return this.crossAboveZero;
     }
 }
