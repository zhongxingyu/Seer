 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.brown.cs32.atian.crassus.backend.demo;
 
 import edu.brown.cs32.atian.crassus.backend.StockHistData;
 import edu.brown.cs32.atian.crassus.backend.StockHistDataMinutely;
 import edu.brown.cs32.atian.crassus.backend.StockRealTimeData;
 import edu.brown.cs32.atian.crassus.backend.StockRealTimeDataImpl;
 import edu.brown.cs32.atian.crassus.backend.StockTimeFrameData;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.Random;
 
 /**
  *
  * @author lyzhang
  */
 public class DemoStockDataImpl implements StockHistData {
 
     private String _ticker;
     
     private List<StockTimeFrameData> _demoData;
     private String _high = "N/A";
     private String _low = "N/A";
     private String _open = "N/A";
     private String _curr = "N/A";
     private String _high52Week = "N/A";
     private String _low52Week = "N/A";
     private String _changeAndPtgChange = "N/A";    
    private int _dataSimFreq = 10;  // in second
     final double STDMEANRATIO = 0.001;
 
     public DemoStockDataImpl(String ticker) {
         _ticker = ticker;
         _demoData = new ArrayList<StockTimeFrameData>();
 
         StockRealTimeData _theTrueStockRealTimeData = new StockRealTimeDataImpl(ticker);
         _theTrueStockRealTimeData.Init();       
         _high = _theTrueStockRealTimeData.getTodayHigh();
         _low = _theTrueStockRealTimeData.getTodayLow();
         _open = _theTrueStockRealTimeData.getOpenPrice();
         _curr = _theTrueStockRealTimeData.getCurrPrice();
         _high52Week = _theTrueStockRealTimeData.getWeek52High();
         _low52Week = _theTrueStockRealTimeData.getWeek52Low();
         _changeAndPtgChange = _theTrueStockRealTimeData.getChgAndPertChg();        
         _theTrueStockRealTimeData = null;
         
         StockHistData _theTrueStockHistData = new StockHistDataMinutely(_ticker);
         _theTrueStockHistData.Init();
         List<StockTimeFrameData> _theTrueData = _theTrueStockHistData.getHistData();    
         _demoData.addAll(_theTrueData); 
         _theTrueStockHistData = null;
     }
     
     private void updateStockTableData(StockTimeFrameData newTFData) {
         Double close = newTFData.getClose();
         
         if(close > Double.parseDouble(this._high)) {
             this._high = close.toString();
         }
         if(close < Double.parseDouble(this._low)) {
             this._low = close.toString();
         }      
         
         this._curr = close.toString();
         if(close > Double.parseDouble(this._high52Week)) {
             this._high52Week = close.toString();
         }
         if(close < Double.parseDouble(this._low52Week)) {
             this._low52Week = close.toString();
         }        
         
         // _changeAndPtgChange   TODO
     }
     
     @Override
     public boolean Init() {
         if (_demoData.size() == 0) {
             return false;
         }
         StockTimeFrameData lastTFData = _demoData.get(_demoData.size() - 1);
         // get time in lastTFData
         Long lastDataReceivedTime = lastTFData.getTimeInNumber();
         Calendar cal = Calendar.getInstance();
         cal.setTime(new Date());
         Long now = (cal.getTimeInMillis()) / 1000;
  
         for (Long curr = lastDataReceivedTime + _dataSimFreq; curr < now; curr += _dataSimFreq) {
             double open = 0.0;   // we just set close and adjustedClose, all others are not used 
             double high = 0.0;;
             double low = 0.0;;
             int volume = 0;
             
             double mean = lastTFData.getClose();
             double std = mean*STDMEANRATIO;          // through STDMEANRATIO, we control standard deviation
             double close = getNormalRandomNumber(mean, std);
             double adjustedClose = close;
             
             StockTimeFrameData newTFData = new StockTimeFrameData(curr.toString(), open, high, low, close, volume, adjustedClose, false);;
            StockTimeFrameData oldSecToLastFrame = _demoData.get(_demoData.size()-2);
            if(oldSecToLastFrame.getTimeInNumber()+ 300 > newTFData.getTimeInNumber()) {
                _demoData.remove(_demoData.size()-1);
            }
             _demoData.add(newTFData);
             updateStockTableData(newTFData);            
         }
 
         return true;
     }
 
     @Override
     public List<StockTimeFrameData> getHistData(){
         return _demoData;
     }
 
     public String getChgAndPertChg() {
         return _changeAndPtgChange;
     }
 
     private String format(String in) {
         try {
             Double tmp = Double.parseDouble(in);
             return String.format("%1$,.2f", tmp);
         } catch (NumberFormatException e) {
             return "N/A";
         }
     }
 
     public String getOpenPrice() {
 
         return format(this._open);
     }
 
     public String getCurrPrice() {
         return format(this._curr);
     }
 
     public String getTodayLow() {
 
         return format(this._low);
     }
 
     public String getTodayHigh() {
         return format(this._high);
     }
 
     public String getWeek52Low() {
         return format(this._low52Week);
     }
 
     public String getWeek52High() {
         return format(this._high52Week);
     }
 
     public void refresh() {
         this.Init();
     }
     
     @Override
     public String getFreq() {
         return "Minutely";
     }    
 
     private double getNormalRandomNumber(double mean, double std)   {
         double result;        
         do {
             Random r = new Random();
             double g = r.nextGaussian();   // g is N(0,1)
             result = std*g + mean;  // result is N(mean, std)
         } while( result <= 0);   // we need a positive price price        
         return result;   
     }    
 }
