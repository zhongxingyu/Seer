 package com.datascience.service;
 
import java.util.Collection;
 
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import com.datascience.core.storages.JSONUtils;
 import com.datascience.executor.JobCommand;
 import com.datascience.gal.AbstractDawidSkene;
import com.datascience.gal.MisclassificationCost;
 import com.datascience.gal.NominalProject;
import com.datascience.gal.commands.CategoriesCommands;
import com.datascience.gal.commands.CostsCommands;
import com.datascience.gal.commands.DatumCommands;
import com.datascience.gal.commands.EvaluationCommands;
import com.datascience.gal.commands.PredictionCommands;
import com.datascience.gal.decision.ILabelProbabilityDistributionCalculator;
import com.datascience.gal.decision.ILabelProbabilityDistributionCostCalculator;
import com.datascience.gal.decision.IObjectLabelDecisionAlgorithm;
import com.datascience.gal.decision.LabelProbabilityDistributionCalculators;
import com.datascience.gal.decision.LabelProbabilityDistributionCostCalculators;
import com.datascience.gal.decision.ObjectLabelDecisionAlgorithms;
import com.datascience.gal.decision.WorkerEstimator;
 import com.datascience.gal.evaluation.DataEvaluator;
 import com.datascience.gal.evaluation.WorkerEvaluator;
 
 
 /**
  * @author Konrad Kurdej
  */
 @Path("/jobs/{id}/")
 public class NominalJobEntry extends JobEntryBase<NominalProject> {
 
 	public NominalJobEntry(){
 		expectedClass = AbstractDawidSkene.class;
 	}
 
 	@Path("categories/")
 	@GET
 	public Response getCategories(){
 		return buildResponseOnCommand(new CategoriesCommands.GetCategories());
 	}
 
 //	@Path("costs/")
 //	@POST
 //	public Response setCosts(@FormParam("costs") String sCosts){
 //		Collection<MisclassificationCost> costs = serializer.parse(sCosts, JSONUtils.misclassificationCostSetType);
 //		return buildResponseOnCommand(new CostsCommands.SetCosts(costs));
 //	}
 	
 	@Path("costs/")
 	@GET
 	public Response getCosts(){
 		return buildResponseOnCommand(new CostsCommands.GetCosts());
 	}
 
 	@Path("objects/{oid:[a-zA-Z_0-9/:.-]+}/categoryProbability")
 	@GET
 	public Response getDatumCategoryProbability(@PathParam("id") String did, 
 			@DefaultValue("DS") @QueryParam("type") String type){
 		ILabelProbabilityDistributionCalculator lpdc = LabelProbabilityDistributionCalculators.get(type);
 		return buildResponseOnCommand(new DatumCommands.GetDatumCategoryProbability( did, lpdc));
 	}
 
 	@Path("objects/prediction/")
 	@GET
 	public Response getPredictionData(@DefaultValue("DS") @QueryParam("algorithm") String lpd,
 			@DefaultValue("MaxLikelihood") @QueryParam("labelChoosing") String lda){
 		ILabelProbabilityDistributionCalculator lpdc = LabelProbabilityDistributionCalculators.get(lpd);
 		IObjectLabelDecisionAlgorithm olda = ObjectLabelDecisionAlgorithms.get(lda);
 		return buildResponseOnCommand(new PredictionCommands.GetPredictedCategory( lpdc, olda));
 	}
 	
 	@Path("objects/cost/estimated")
 	@GET
 	public Response getEstimatedDataCost(@DefaultValue("DS") @QueryParam("algorithm") String lpd,
 			@DefaultValue("ExpectedCost") @QueryParam("costAlgorithm") String lca){
 		ILabelProbabilityDistributionCalculator lpdc = LabelProbabilityDistributionCalculators.get(lpd);
 		ILabelProbabilityDistributionCostCalculator lpdcc = LabelProbabilityDistributionCostCalculators.get(lca);
 		return buildResponseOnCommand(new PredictionCommands.GetCost( lpdc, lpdcc));
 	}
 	
 	@Path("objects/quality/estimated/")
 	@GET
 	public Response getEstimatedDataQuality(@DefaultValue("DS") @QueryParam("algorithm") String lpd,
 			@DefaultValue("ExpectedCost") @QueryParam("costAlgorithm") String lca){
 		ILabelProbabilityDistributionCalculator lpdc = LabelProbabilityDistributionCalculators.get(lpd);
 		ILabelProbabilityDistributionCostCalculator lpdcc = LabelProbabilityDistributionCostCalculators.get(lca);
 		return buildResponseOnCommand(new PredictionCommands.GetQuality( lpdc, lpdcc));
 	}
 
 	@Path("objects/cost/evaluated/")
 	@GET
 	public Response getEvaluatedDataCost(@DefaultValue("DS") @QueryParam("algorithm") String lpd,
 			@DefaultValue("MaxLikelihood") @QueryParam("labelChoosing") String lda){
 		ILabelProbabilityDistributionCalculator lpdc = LabelProbabilityDistributionCalculators.get(lpd);
 		DataEvaluator dataEvaluator= DataEvaluator.get(lda, lpdc);
 		return buildResponseOnCommand(new EvaluationCommands.GetCost( dataEvaluator));
 	}
 	
 	@Path("objects/quality/evaluated/")
 	@GET
 	public Response getEvaluatedDataQuality(@DefaultValue("DS") @QueryParam("algorithm") String lpd,
 			@DefaultValue("MaxLikelihood") @QueryParam("labelChoosing") String lda){
 		ILabelProbabilityDistributionCalculator lpdc = LabelProbabilityDistributionCalculators.get(lpd);
 		DataEvaluator dataEvaluator= DataEvaluator.get(lda, lpdc);
 		return buildResponseOnCommand(new EvaluationCommands.GetQuality( dataEvaluator));
 	}
 	
 	@Path("workers/quality/evaluated/")
 	@GET
 	public Response getEvaluatedWorkersQuality(@DefaultValue("ExpectedCost") @QueryParam("costAlgorithm") String lca){
 		ILabelProbabilityDistributionCostCalculator lpdcc = LabelProbabilityDistributionCostCalculators.get(lca);
 		return buildResponseOnCommand(new PredictionCommands.GetWorkersQuality( new WorkerEvaluator(lpdcc)));
 	}
 	
 	@Path("workers/quality/estimated/")
 	@GET
 	public Response getWorkersQuality(@DefaultValue("ExpectedCost") @QueryParam("costAlgorithm") String lca){
 		ILabelProbabilityDistributionCostCalculator lpdcc = LabelProbabilityDistributionCostCalculators.get(lca);
 		return buildResponseOnCommand(new PredictionCommands.GetWorkersQuality( new WorkerEstimator(lpdcc)));
 	}
 
 	@Override
 	protected JobCommand getPredictionZipCommand(String path){
 		return new PredictionCommands.GetPredictionZip(path);
 	}
 
 }
