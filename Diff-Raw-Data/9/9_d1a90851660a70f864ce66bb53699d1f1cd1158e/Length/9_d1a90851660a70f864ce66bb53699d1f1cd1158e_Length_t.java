 import lombok.EqualsAndHashCode;
 import lombok.Getter;
 
 @EqualsAndHashCode
 public class Length {
 
     @Getter
     private Double count;
 
     @Getter
     private LengthType lengthType;
 
     public Length(LengthType lengthType, double count) {
         this.lengthType = lengthType;
         this.count = count;
     }
 
     public Length expressedIn(LengthType lengthType) {
         return new Length(lengthType, translateTo(lengthType));
     }
 
     private double translateTo(LengthType outType) {
        return count * outType.toBaseMultiplier() / lengthType.toBaseMultiplier();
     }
 }
