 package woodsie.avalanche.processor;
 
 import java.math.BigDecimal;
 import java.math.MathContext;
 
 import woodsie.avalanche.data.Hazard;
 import woodsie.avalanche.data.ReductionParams;
 import woodsie.avalanche.data.Steepness;
 import woodsie.avalanche.data.Where;
 
 public class ReductionCalculator {
 	public static final BigDecimal EXTREME = BigDecimal.valueOf(100);
 
 	public BigDecimal process(ReductionParams params) {
 		if (params.hazardLevel == Hazard.VERY_HIGH) {
 			return EXTREME;
 		}
 
 		BigDecimal dangerPotential = new BigDecimal(params.hazardLevel.getDangerPotential(params.higherHazard), MathContext.DECIMAL32);
 
 		int reductionFactor = 1;
 
		if (!(params.hazardLevel.ordinal() >= Hazard.CONSIDERABLE.ordinal() && params.steepness.getReductionFactor() <= 1)
		        || (params.hazardLevel.ordinal() >= Hazard.CONSIDERABLE.ordinal() && params.steepness != Steepness.NOT_STEEP)) {
 			reductionFactor *= params.steepness.getReductionFactor();
 
 			if (!params.allAspects) {
 
 				if (!params.inverse
 				        || (params.inverse && params.where == Where.AVOID_CRITICAL)) {
 					reductionFactor *= params.where.getReductionFactor();
 				}
 
 				reductionFactor *= params.terrain.getReductionFactor();
 			}
 
 			reductionFactor *= params.groupSize.getReductionFactor();
 		}
 
 		return dangerPotential.divide(new BigDecimal(reductionFactor), MathContext.DECIMAL32);
 	}
 }
