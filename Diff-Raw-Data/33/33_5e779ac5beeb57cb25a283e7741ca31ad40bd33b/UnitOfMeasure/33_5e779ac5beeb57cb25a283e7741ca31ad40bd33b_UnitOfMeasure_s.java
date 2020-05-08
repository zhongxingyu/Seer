 package com.example.oob.uom;
 
 import java.text.DecimalFormat;
 
 import static com.example.oob.uom.UnitOfMeasure.Measurement.*;
 import static com.example.oob.uom.UnitOfMeasure.MeasurementType.*;
 
 /**
  * Created by Saleem Siddiqui on 10/5/12 at 3:47 AM
  */
 public class UnitOfMeasure {
 
     private final double magnitude;
     private final Unit unit;
     private static DecimalFormat SCALE = new DecimalFormat("#.########");
 
     public static enum MeasurementType {
         INTERVAL, ARITHMETIC
     }
 
     public static enum Measurement {
         LENGTH(ARITHMETIC), VOLUME(ARITHMETIC), TEMPERATURE(INTERVAL);
         private final MeasurementType type;
 
         Measurement(MeasurementType type) {
             this.type = type;
         }
     }
 
     /**
      * Units for various Measurement objects.
      * Each Unit has three attributes:
      * <ol>
      *     <li>Factor: a positive number which is the answer to this question "what is the ratio between a magnitude of one of <i>this</i> unit
      *     and the canonical unit?". This is always 1 for canonical unit</li>
      *     <li>Offset: a negative, zero or positive number which is the answer to this question "when <i>this</i> unit has a magnitude of zero,
      *     what is the corresponding magnitude in the canonical unit?" This is always 0 for canonical unit.</li>
      *     <li>Measurement: the Measurement enum that this unit measures</li>
      * </ol>
      */
     public static enum Unit {
         INCH(1, 0, LENGTH),
         FOOT(12, 0, LENGTH),
         CM(1/2.54, 0, LENGTH),
         MM(1/25.4, 0, LENGTH),
 
         LITER(1, 0, VOLUME),
         US_GALLON(3.78541, 0, VOLUME),
         IMP_GALLON(4.54609, 0, VOLUME),
 
         KELVIN(1, 0, TEMPERATURE),
         CELSIUS(1, 273.15, TEMPERATURE),
         FAHRENHEIT(5.0/9.0, 255.372222222, TEMPERATURE);
 
         public final double factor;
         public final double offset;
         private final Measurement measurement;
 
         Unit(double factor, double offset, Measurement measurement) {
             this.factor = factor;
             this.offset = offset;
             this.measurement = measurement;
         }
 
         public Unit getCanonicalUnit(Measurement measurement) {
             for (Unit unit : Unit.values()) {
                 if (unit.measurement == measurement && unit.factor == 1D && unit.offset == 0D) {
                     return unit;
                 }
             }
             throw new RuntimeException("Configuration Error: No canonical unit (with a factor of 1 and offset of 0) found for Physical Property " + measurement + "!");
         }
     }
 
     public static UnitOfMeasure create(double magnitude, Unit unit) {
         return new UnitOfMeasure(magnitude, unit);
     }
 
     public UnitOfMeasure add(UnitOfMeasure uom) {
        preventAdditionOfDifferentMeasurements(uom);
        preventArithmeticOperationOnNonArithmeticMeasurementTypes();
         return create(canonicalMagnitude() + uom.canonicalMagnitude(), unit.getCanonicalUnit(unit.measurement));
     }
 
    private void preventAdditionOfDifferentMeasurements(UnitOfMeasure uom) {
         if(uom.unit.measurement != unit.measurement) {
            throw new IllegalOperationException("Cannot add a " + unit.measurement + " to a " + uom.unit.measurement + "!");
         }
     }
 
     private void preventArithmeticOperationOnNonArithmeticMeasurementTypes() {
         if(unit.measurement.type != ARITHMETIC) {
            throw new IllegalOperationException("Cannot perform arithmetic operation on a " + unit.measurement + "!");
         }
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         UnitOfMeasure that = (UnitOfMeasure) o;
         if (unit.measurement != that.unit.measurement) return false;
         return Double.compare(that.canonicalMagnitude(), canonicalMagnitude()) == 0;
     }
 
     @Override
     public int hashCode() {
         int result;
         long temp;
         temp = magnitude != +0.0d ? Double.doubleToLongBits(magnitude) : 0L;
         result = (int) (temp ^ (temp >>> 32));
         result = 31 * result + unit.hashCode();
         return result;
     }
 
     @Override
     public String toString() {
         final StringBuilder sb = new StringBuilder();
         sb.append("UnitOfMeasure");
         sb.append("{magnitude=").append(magnitude);
         sb.append(", unit=").append(unit);
         sb.append('}');
         return sb.toString();
     }
 
     private UnitOfMeasure(double magnitude, Unit unit) {
         this.magnitude = magnitude;
         this.unit = unit;
     }
 
     private double canonicalMagnitude() {
         return Double.valueOf(SCALE.format(magnitude * unit.factor +  unit.offset));
     }
 }
