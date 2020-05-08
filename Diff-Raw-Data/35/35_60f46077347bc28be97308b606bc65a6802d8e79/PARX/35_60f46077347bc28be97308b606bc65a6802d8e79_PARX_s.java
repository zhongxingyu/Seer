 package edacc.api.costfunctions;
 
 import java.util.List;
 
 import edacc.model.ExperimentResult;
 
 public class PARX implements CostFunction {
 	private int penaltyFactor;
 
 	/**
 	 * Default constructor initialising the PARX cost function to PAR10.
 	 */
 	public PARX() {
 		this(10);
 	}
 
 	/**
 	 * Initialises the cost function to PARX with X = <code>penaltyFactor</code>
 	 * 
 	 * @param penaltyFactor
 	 */
 	public PARX(int penaltyFactor) {
 		if (penaltyFactor <= 0)
 			throw new IllegalArgumentException("penalty factor should be greater than 0");
 		this.penaltyFactor = penaltyFactor;
 	}
 
 	/**
 	 * Calculates the cost according to a PARX function. Only finished jobs are taken into consideration. 
 	 * Running, not started or crashed jobs are not used for the computation!
 	 */
 	@Override
 	public float calculateCost(List<ExperimentResult> results) {
 		float sum = 0.0f;
 		if (results.size() == 0)
 			return 0;
 		for (ExperimentResult res : results) {
			if (res.getStatus().getStatusCode() > 0) {
				if (String.valueOf(res.getResultCode().getResultCode()).startsWith("1")) {
					sum += res.getResultTime();
				} else {
					sum += res.getCPUTimeLimit() * (float) penaltyFactor;
				}
 			}
 		}
 		return sum / results.size();
 	}
 
 	/**
 	 * Calcultate the total runtime of all finished jobs i.e resultCode > 0!
 	 * 
 	 */
 	@Override
 	public float calculateCumulatedCost(List<ExperimentResult> results) {
 		// TODO: Take into account if the cost or runtime is wanted!
 		float sum = 0.0f;
 		if (results.size() == 0)
 			return 0;
 		for (ExperimentResult res : results) {
 			if (res.getStatus().getStatusCode() > 0) {
 				if (String.valueOf(res.getResultCode().getResultCode()).startsWith("1")) {
 					sum += res.getResultTime();
 				} else {
 					sum += res.getCPUTimeLimit() * (float) penaltyFactor;
 				}
 			}
 		}
 		return sum;
 	}
 
 	@Override
 	public String databaseRepresentation() {
 		return "par" + String.valueOf(penaltyFactor);
 	}
 }
