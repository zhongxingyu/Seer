 package see.functions.bool;
 
 import see.functions.VarArgFunction;
 
 import javax.annotation.Nonnull;
 import java.util.List;
 
 import static com.google.common.base.Preconditions.checkArgument;
 import static com.google.common.base.Preconditions.checkNotNull;
 
 public class Not implements VarArgFunction<Boolean, Boolean> {
     @Override
     public Boolean apply(@Nonnull List<Boolean> input) {
         checkArgument(input.size() == 1, "Not takes only one argument");
        Boolean value = input.get(0);
        checkNotNull(value);

        return !value;
     }
 
     @Override
     public String toString() {
         return "not";
     }
 }
