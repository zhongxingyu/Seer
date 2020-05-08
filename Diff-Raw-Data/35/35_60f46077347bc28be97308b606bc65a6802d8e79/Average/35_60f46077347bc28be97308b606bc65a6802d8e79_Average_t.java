 package edacc.api.costfunctions;
 
 import java.util.List;
 
 import edacc.model.ExperimentResult;
 
 public class Average implements CostFunction {
 	@Override
 	public float calculateCost(List<ExperimentResult> results) {
 		float sum = 0.0f;
 		if (results.size() == 0) return 0;
 		for (ExperimentResult res: results) 
 				sum += res.getResultTime();
 		return sum / results.size();
 	}
 	@Override
 	public float calculateCumulatedCost(List<ExperimentResult> results) {
 		float sum = 0.0f;
 		if (results.size() == 0) return 0;
 		for (ExperimentResult res: results) 
 			if (res.getStatus().getStatusCode() > 0)
 				sum += res.getResultTime();
 		return sum ;
 	}
 	
 	@Override
 	public String databaseRepresentation() {
 		return "average";
 	}
 
 }
