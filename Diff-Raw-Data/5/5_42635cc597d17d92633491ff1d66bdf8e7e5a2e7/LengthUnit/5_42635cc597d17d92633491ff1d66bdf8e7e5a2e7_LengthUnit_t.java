 package net.pizey.jclu;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /** 
  * A definite magnitude of a physical quantity: length.
  * 
  * @author timp
  * @since 2013-08-13
  */
 public enum LengthUnit  {
 
   IN ("in", 0.0254),
  FT ("ft", 0.3048),
   YD ("yd", 0.9144),
  M  ("m",  1.0),
   CM ("cm", 0.01);
   
   private static Map<String, LengthUnit> units = new HashMap<String, LengthUnit>();
   static { 
     units.put(IN.getSymbol(), IN);
     units.put(FT.getSymbol(), FT);
     units.put(YD.getSymbol(), YD);
     units.put(M.getSymbol(), M);
     units.put(CM.getSymbol(), CM);
   }
   
   private String symbol;
   private Double metres;
 
   LengthUnit(String symbol, double metres) {
     this.setSymbol(symbol);
     this.setMetres(new Double(metres)); // none of this new fangled autoboxing :) 
   }
 
   public static LengthUnit fromSymbol(String symbol){
     return units.get(symbol);
   }
 
   public String getSymbol() {
     return symbol;
   }
 
   public void setSymbol(String symbol) {
     this.symbol = symbol;
   }
 
   public Double getMetres() {
     return metres;
   }
 
   public void setMetres(Double metres) {
     this.metres = metres;
   }
 
 }
