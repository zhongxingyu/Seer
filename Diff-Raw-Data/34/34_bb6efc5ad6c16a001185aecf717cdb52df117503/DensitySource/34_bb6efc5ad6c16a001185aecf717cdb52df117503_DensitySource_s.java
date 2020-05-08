 package model_example;
 
 import edu.berkeley.path.model_mock.*;
 import edu.berkeley.path.model_elements.*;
 
 import java.util.*;
 
 import org.apache.avro.Schema;
 import org.apache.avro.generic.GenericData;
 
 public class DensitySource extends MockDataSource {
   /**
    * Construct a density source with some example density profiles ready
    * to be consumed by the example.
    */
   public DensitySource() {
     super();
 
     insert(makeDp(10.0));
     insert(makeDp(20.0));
   }
   
   public DensityProfile makeDp(double fudge) {
     DensityProfile dp;
     
     dp = new DensityProfile();
    dp.setId(2);
    dp.setVehiclesPerMeterOnLink(3, 1.23 + fudge);
 
     return dp;
   }
   
   public DensityProfile getDensityProfile() {
     return (DensityProfile)get().x();
   }
 }
