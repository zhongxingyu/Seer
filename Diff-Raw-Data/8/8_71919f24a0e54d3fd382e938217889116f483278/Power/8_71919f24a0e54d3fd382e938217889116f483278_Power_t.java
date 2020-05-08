 package see.functions.arithmetic;
 
 import see.functions.VarArgFunction;
 
 import javax.annotation.Nonnull;
 import java.math.BigDecimal;
 import java.util.List;
 
 import static com.google.common.base.Preconditions.checkArgument;
 
 /**
  * Power function. Returns first argument raised to the power of the second argument.
  * Special case: if base is less than zero and exponent is fractional, apply throws IllegalArgumentException.
  */
 public class Power implements VarArgFunction<BigDecimal, Number> {
     @Override
     public Number apply(@Nonnull List<BigDecimal> input) {
         checkArgument(input.size() == 2, "Power takes only two arguments");
 
         return power(input.get(0), input.get(1));
     }
 
     private Number power(BigDecimal base, BigDecimal exponent) {
         if (isInteger(exponent)) {
             return base.pow(exponent.intValue());
         } else {
             if (base.compareTo(BigDecimal.ZERO) < 0)
                 throw new IllegalArgumentException("Cannot raise negative number to fractional power");
             
             return Math.pow(base.doubleValue(), exponent.doubleValue());
         }
     }
 
     private boolean isInteger(BigDecimal exponent) {
        return exponent.stripTrailingZeros().scale() <= 0 && exponent.compareTo(BigDecimal.ZERO) > 0;
     }
 
     @Override
     public String toString() {
         return "pow";
     }
 }
 
