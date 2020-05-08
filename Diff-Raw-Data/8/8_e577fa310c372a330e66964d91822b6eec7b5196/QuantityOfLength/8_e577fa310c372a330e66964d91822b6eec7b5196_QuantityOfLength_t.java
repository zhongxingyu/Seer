 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.github.ryancwilliams.Starways.engine.math.ValuesOfQuantities;
 
 /**
  * This enum provides quantities of length for working with lengths. 
  * @author ryancwilliams
  */
 public enum QuantityOfLength implements Quantity {
    METRE("metre","m",1d),
    KILOMETRE("kilometre","km",1000d),
    ASTRONOMICAL_UNIT("astronomical unit","au",149597870700d),
    LIGHT_YEAR("light-year","ly",9460730472580800d);
     
     /**
      * The name of the unit.
      */
     private final String Name;
     /**
      * the symbol for the unit.
      */
     private final String Symbol;
     /**
      * The conversion factor for converting from the base unit 
      * (conversionFactor = 1) to this unit.
      */
     private final double ConversionFactor;
 
     /**
      * Creates a QuantityOfLength object
      * @param NAME the name of the unit.
      * @param SYMBOL the symbol for the unit.
      * @param CONVERSION_FACTOR the conversion factor for converting from the base unit to this unit.
      */
     private QuantityOfLength(String Name, String Symbol, double ConversionFactor) {
         this.Name = Name;
         this.Symbol = Symbol;
         this.ConversionFactor = ConversionFactor;
     }
 
     /**
      * Gets the name of this unit.
      * @return the name of this unit.
      */
     @Override
     public String getName() {
         return Name;
     }
 
     /**
      * Gets the symbol of this unit.
      * @return the symbol of this unit.
      */
     @Override
     public String getSymbol() {
         return Symbol;
     }
 
     /**
      * Gets the conversion factor for converting from the base unit 
      * (conversionFactor = 1) to this unit.
      * @return the conversion factor for converting from the base unit to this unit.
      */
     @Override
     public double getConversionFactor() {
         return ConversionFactor;
     }
     
     /**
      * Calculates a conversion factor for converting between the source unit 
      * and the destination unit.
      * @param source the unit to convert from.
      * @param destination the unit to convert to.
      * @return the conversion factor.
      */
     public static double caculateConversionFactor(QuantityOfLength source, 
             QuantityOfLength destination) {
         return (destination.ConversionFactor / source.ConversionFactor);
     }
 }
