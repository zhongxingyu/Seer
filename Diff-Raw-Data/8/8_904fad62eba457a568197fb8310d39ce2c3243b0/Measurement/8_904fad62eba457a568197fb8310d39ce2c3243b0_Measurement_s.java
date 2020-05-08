 import lombok.EqualsAndHashCode;
 import lombok.Getter;
 
 @EqualsAndHashCode
 public class Measurement {
     @Getter
     private final double baseUnitCount;
 
     @Getter
     private final MeasurementType measurementType;
 
     public Measurement(MeasurementType measurementType, double count) {
         this.measurementType = measurementType;
         this.baseUnitCount = translateToBaseCount(count);
     }
 
     private double translateToBaseCount(double count) {
         return measurementType.additive + count / measurementType.toBaseMultiplier();
     }
 
     public double measurementUnitCount() {
         return (baseUnitCount - measurementType.additive) * measurementType.toBaseMultiplier();
     }
 
     public Measurement expressedIn(MeasurementType newMeasurementType) {
         if (measurementType.measurementClass.equals(newMeasurementType.measurementClass)) {
             return new Measurement(newMeasurementType, (baseUnitCount -  newMeasurementType.additive) * newMeasurementType.toBaseMultiplier());
         }
         return new Measurement(MeasurementType.InvalidConversion, 0.0);
     }
 
     public Measurement plus(Measurement measurement) {
         Double totalBaseUnitCount = baseUnitCount + measurement.getBaseUnitCount();
        return new Measurement(measurement.measurementType, totalBaseUnitCount * measurement.measurementType.toBaseMultiplier());
     }
 
     public String toString() {
         return measurementUnitCount() + " " + measurementType;
     }
 }
