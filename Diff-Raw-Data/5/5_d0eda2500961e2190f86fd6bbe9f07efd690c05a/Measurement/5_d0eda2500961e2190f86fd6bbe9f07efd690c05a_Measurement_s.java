 package com.customfit.ctg.model;
 
 import com.customfit.ctg.controller.*;
 import java.lang.reflect.*;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * A Measurement represents what, in science, is typically
  * called a vector quantity. That is, it is two things: a value,
  * and a unit.
  * 
  * @author David, Drew
  */
 public class Measurement implements Comparable<Measurement> {
 	
     /**
      * The quantity of the Measurement.
      */
     private double quantity = 0.0;
     
     /**
      * The unit of the Measurement.
      */
     private String unit = "";
 
     /**
      * Create a new Measurement with the quantity and unit specified.
      * 
      * @param quantity The quantity of the Measurement. (e.g., 1.0, 99)
      * @param unit The unit of the Measurement. (e.g., grams, gallons, miles, millenia)
      */
     public Measurement(double quantity, String unit) {
         this.quantity = quantity;
         this.unit = unit;
     }
 
     /**
      * Create a new Measurement with the quantity and unit specified
      * in the measurableUnitString.
      * 
      * @param measurableUnitString Amount and unit as specified by a previous Measurement.toString().
      */
     public Measurement(String measurableUnitString) {
        //grab amount quantity
        this.quantity = Double.parseDouble(measurableUnitString.replaceAll("[A-Z,a-z, ]", ""));
         //grab amount unit
         this.unit = measurableUnitString.replaceAll("\\.|[0-9]", "").trim();
     }
 
     /**
      * Gets the quantity of the Measurement.
      * 
      * @return The quantity of the Measurement.
      */
     public double getQuantity() {
             return quantity;
     }
     /**
      * Gets the unit of the Measurement.
      * 
      * @return The unit of the Measurement.
      */
     public String getUnit() {
             return unit;
     }
     
     /**
      * Sets the unit of the Measurement.
      * 
      * @param unit The unit of the Measurement.
      */
     public void setUnit(String unit) {
             this.unit = unit;
     }
     
     /**
      * Sets the quantity of the Measurement.
      * 
      * @param quantity The quantity of the Measurement.
      */
     public void setQuantity(double quantity) {
             this.quantity = quantity;
     }
 
     /**
      * Gets the quantity and the unit in a string.
      * 
      * Example: 1.0 grams
      * 
      * @return The quantity and the unit in a string.
      */
     @Override
     public String toString()
     {
             return this.getQuantity() + " " + this.getUnit();
     }
 
     /**
      * Compares this Measurement with another Measurement.
      * The units are ignored and only the numerical comparison
      * is made. It calls Double.compare().
      * 
      * @param o The Measurement to compare it with.
      * 
      * @return The result of a Double.compare().
      */
     public int compareTo(Measurement o)
     {
             if (this.getUnit() != o.getUnit())
             {
                     System.err.println("compareTo() hit on MeasurableUnit with different units specified. You shouldn't compare apples and oranges if you expect a similar result. Will supply mathematical differenc, but fix your units.");
             }
 
             return Double.compare(this.getQuantity(), o.getQuantity());
     }
 
     /**
      * Scales a Measurement's quantity field.
      * 
      * @param measurableUnit A Measurement instance.
      * @param scaleFactor The scale factor, e.g. 0.5, 2.0, 5.0, ...
      * 
      * @return The new Measurement with the scaled field.
      */
     public static Measurement scaleMeasurableUnit(Measurement measurement, double scaleFactor)
     {
         Measurement newMeasurement = null;
         try {
             newMeasurement = (Measurement)measurement.clone();
             newMeasurement.quantity *= scaleFactor;
         } catch (CloneNotSupportedException ex) {
             Application.dumpException("The was a problem cloning the Measurable Unit object. Clone is not supported.", ex);
         }
         return newMeasurement;
     }
 
     /**
      * Scales quantity field into a new Measurement.
      * 
      * @param scaleFactor The scale factor, e.g. 0.5, 2.0, 5.0, ...
      * 
      * @return The new Measurement with the scaled field.
      */
     public Measurement scale(double scaleFactor)
     {
         return Measurement.scaleMeasurableUnit(this, scaleFactor);
     }
 
     //TODO: Update whether this is dry or fluid ounces.
     /**
      * Converts a Measurement to ounces.
      * 
      * @param measurement The Measurement to convert.
      * 
      * @return The Measurement in ounces.
      */
     public static Measurement measurementToOunces(Measurement measurement)
     {
         //TODO: Update whether this is dry or fluid ounces.
         return new Measurement(measurementToOunces(measurement.getQuantity(), measurement.getUnit()), "ounces");
     }
 	
     //TODO: Document whether this is dry or fluid ounces.
     /**
      * Converts a Measurement to ounces.
      * 
      * @param quantity The quantity of the Measurement.
      * @param unit The unit of the Measurement.
      * 
      * @return The Measurement in ounces.
      */
     public static double measurementToOunces(double quantity, String unit)
     {
         double ounces = quantity;		
         if (unit.equals(USAUnits.POUNDS) || unit.equals(USAUnits.PINTS))
             ounces *= 16.0;
         else if (unit.equals(USAUnits.GALLONS))
             ounces *= 128.0;
         else if (unit.equals(USAUnits.QUARTS))
             ounces *= 32.0;
         else if (unit.equals(USAUnits.CUPS))
             ounces *= 8.0;
         else if (unit.equals(USAUnits.TABLESPOONS))
             ounces *= .5;
         else if (unit.equals(USAUnits.TEASPOONS))
             ounces *= .167;
         else if (unit.equals(USAUnits.LIQUID_OUNCES) || unit.equals(USAUnits.DRY_OUNCES))
         {
             //ounces = ounces;
             //so do nothing			
         }
         else
         {
             ounces = 0.0;
             System.err.println("Error: Unknown measurement unit provided for conversion.");
         }
         return ounces;
     }
     
     /**
      * Converts Measurement to ounces.
      * 
      * @return A new Measurement in ounces.
      */
     public Measurement toOunces()
     {
         return Measurement.measurementToOunces(this);
     }
     
     @Override
     public boolean equals(Object object)
     {
        Measurement measurement = (Measurement)object;
        if (this.unit.equals(measurement.unit)
                && this.quantity == measurement.quantity)
            return true;
        return false;
     }
 
     /**
      * Represents a sample of the US Customary measurement units.
      */
     public static class USAUnits
     {
         //USA units
         public static final String POUNDS         = "pounds";
         public static final String GALLONS        = "gallons";
         public static final String QUARTS         = "quarts";
         public static final String PINTS          = "pints";
         public static final String CUPS           = "cups";
         public static final String LIQUID_OUNCES  = "fluid ounces"; 	// Volume
         public static final String DRY_OUNCES     = "ounces (dry)"; 	// Weight
         public static final String TABLESPOONS    = "tablespoons";
         public static final String TEASPOONS      = "teaspoons";
         /**
          * All US Customary measurement units.
          */
         public static final String[] ALL_UNITS = new String[] {
             POUNDS,
             GALLONS,
             QUARTS,
             PINTS,
             CUPS,
             LIQUID_OUNCES,
             DRY_OUNCES,
             TABLESPOONS,
             TEASPOONS
         };
         
         public static String[] getAllUSAUnits()
         {
             ArrayList<String> fields = new ArrayList<String>();
             USAUnits usaUnits = new USAUnits();
             for (Field field : usaUnits.getClass().getDeclaredFields())
             {
                 if (field.getType().getName().equals(new String().getClass().getName()))
                     try {
                         fields.add((String)field.get(new USAUnits()));
                     } catch (IllegalAccessException ex) {
                         Application.dumpException("Error getting unit from USAUnits.", ex);
                     } catch (IllegalArgumentException ex) {
                         Application.dumpException("Error getting unit from USAUnits.", ex);
                     }
             }
             return fields.toArray(new String[] {});
         }
 
     }
 
     /**
      * Represents a sample of the Metric measurement units.
      */
     public static class MetricUnits
     {
         //metric units
         public static final String GRAMS        = "grams";
         public static final String MILLIGRAMS   = "milligrams";
         /**
          * All Metric units.
          */
         public static final String[] ALL_UNITS = new String[] {
             GRAMS,
             MILLIGRAMS
         };
         
         public static String[] getAllMetricUnits()
         {
             ArrayList<String> fields = new ArrayList<String>();
             MetricUnits metricUnits = new MetricUnits();
             for (Field field : metricUnits.getClass().getFields())
             {
                 if (field.getType().getName().equals(new String().getClass().getName()))
                     try {
                         fields.add((String)field.get(new MetricUnits()));
                     } catch (IllegalAccessException ex) {
                         Application.dumpException("Error getting unit from MetricUnits.", ex);
                     } catch (IllegalArgumentException ex) {
                         Application.dumpException("Error getting unit from MetricUnits.", ex);
                     }
             }
             return fields.toArray(new String[] {});
         }
     }
 
     /**
      * Represents a sample of the Food measurement units.
      */
     public static class FoodUnits
     {
         public static final String CALORIES = "Calories"; //capitalize, because calories != Calories, sometimes also called kCals.
         /**
          * All Food units.
          */
         public static final String[] ALL_UNITS = new String[] {
             CALORIES
         };
         
         public static String[] getAllFoodUnits()
         {
             ArrayList<String> fields = new ArrayList<String>();
             FoodUnits foodUnits = new FoodUnits();
             for (Field field : foodUnits.getClass().getDeclaredFields())
             {
                 if (field.getType().getName().equals(new String().getClass().getName()))
                     try {
                         fields.add((String)field.get(new FoodUnits()));
                     } catch (IllegalAccessException ex) {
                         Application.dumpException("Error getting unit from FoodUnits.", ex);
                     } catch (IllegalArgumentException ex) {
                         Application.dumpException("Error getting unit from FoodUnits.", ex);
                     }
             }
             return fields.toArray(new String[] {});
         }
    }	
 
 
     /**
      * Lists all USAUnits, MetricUnits, and FoodUnits into an array.
      * 
      * @return All units array.
      */
     public static String[] getAllUnits()
     {
         ArrayList<String> allUnits = new ArrayList<String>();
         allUnits.addAll(Arrays.asList(USAUnits.getAllUSAUnits()));
         allUnits.addAll(Arrays.asList(MetricUnits.getAllMetricUnits()));
         allUnits.addAll(Arrays.asList(FoodUnits.getAllFoodUnits()));
         return allUnits.toArray(new String[] {});
     }
     
     /**
      * Lists all USAUnits and MetricUnits into an array. Ignores FoodUnits.
      * 
      * @return All units array.
      */
     public static String[] getAllMeasurementUnits()
     {
         ArrayList<String> allUnits = new ArrayList<String>();
         allUnits.addAll(Arrays.asList(USAUnits.getAllUSAUnits()));
         allUnits.addAll(Arrays.asList(MetricUnits.getAllMetricUnits()));
         return allUnits.toArray(new String[] {});
     }
    
 }
