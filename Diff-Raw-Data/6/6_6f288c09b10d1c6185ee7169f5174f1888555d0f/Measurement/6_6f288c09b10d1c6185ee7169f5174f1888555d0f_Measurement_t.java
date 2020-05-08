 import lombok.EqualsAndHashCode;
 import lombok.Getter;
 
 @EqualsAndHashCode
 public class Measurement {
     @Getter
     protected Double count;
 
     @Getter
     MeasurementType measurementType;
 
     public Measurement(MeasurementType measurementType, double count) {
         this.measurementType = measurementType;
         this.count = count;
     }
 
    public Measurement expressedIn(MeasurementType type) {
        if (measurementType.measurementClass.equals(type.measurementClass)) {
            return new Measurement(type, translateTo(type));
         }
         return new Measurement(MeasurementType.InvalidConversion, 0.0);
 
     }
 
     public Measurement plus(Measurement measurement) {
         return new Measurement(measurement.getMeasurementType(), combineCounts(measurement));
     }
 
 
     double translateTo(MeasurementType outType) {
         return count * outType.toBaseMultiplier() / measurementType.toBaseMultiplier();
     }
 
     public String toString() {
         return count + " " + measurementType.name();
     }
 
     double combineCounts(Measurement measurement) {
         return expressedIn(measurement.getMeasurementType()).getCount() + count;
     }
 }
