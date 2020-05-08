 import java.util.Map;
 
 public class Investor {
 
     double _money;
     double _oldmoney;
     int _confidence;
     double _stockVal;
     double _stockValChanged;
     //lets the investor access the market
     Market _market;
     //list of the companies pointing to the int amount they have of the stock
     Map<Company, Integer> _stocks;
 
     public Investor(Market mar){
         _money = 5000.0;
         _oldmoney = 0;
        _confidence = _confidence = 43 + random(16);
         _stockVal = 0;
         _stockValChanged = 0;
         _market = mar;
         _stocks = new TreeMap<Company, Integer>();
     }
     
     //find list of all the companies with risk < comfidence.
     
     public void changeStockVal(){
         double old = _stockVal;
         _stockVal = 0; 
         for(Company x: _stocks.keySet()){
             _stockVal = x.getPrice() * _stocks.get(x); 
             //Need and accessor method in Company that returns the price.
         }
         _stockValChanged = _stockVal - old;
          
     }
     
     
     public double getStockChange(){
         return _stockValChanged;
     }
 
     public double getStockVal(){
         return _stockVal;
     }
     
     public double getPort(){
         return _money + _stockVal;
     }
 
     public void invest() {
 
     }
 
     //sell those stocks with the risk > than confidence
     public void sell() {
     }
 
     public void resetConfidence() {
         _confidence += (_money + _stockValChanged - _oldmoney) * Math.random()/50;
     }
     
     public double getMoney(){
         return _money;
     }
     
     public int getConfidence(){
         return _confidence;
     }
 }
