 package gov.nih.nci.caintegrator.analysis.server;
 
 import org.apache.log4j.Logger;
 
 import gov.nih.nci.caintegrator.analysis.messaging.AnalysisRequest;
 import gov.nih.nci.caintegrator.analysis.messaging.AnalysisResult;
 import gov.nih.nci.caintegrator.analysis.messaging.ClassComparisonLookupRequest;
 import gov.nih.nci.caintegrator.analysis.messaging.ClassComparisonRequest;
 import gov.nih.nci.caintegrator.analysis.messaging.CompoundAnalysisRequest;
 import gov.nih.nci.caintegrator.analysis.messaging.CompoundAnalysisResult;
 import gov.nih.nci.caintegrator.analysis.messaging.HierarchicalClusteringRequest;
 import gov.nih.nci.caintegrator.analysis.messaging.PrincipalComponentAnalysisRequest;
 import gov.nih.nci.caintegrator.exceptions.AnalysisServerException;
 
 public class CompoundRequestTaskR extends AnalysisTaskR {
 
 	
 	private CompoundAnalysisResult result = null;
 	
 	private static Logger logger = Logger.getLogger(CompoundRequestTaskR.class);
 
 	
 	
 	public CompoundRequestTaskR(CompoundAnalysisRequest compoundRequest) {
 		super(compoundRequest);
 		
 	}
 
 	public CompoundRequestTaskR(CompoundAnalysisRequest compoundRequest, boolean debugRcommands) {
 		super(compoundRequest, debugRcommands);
 		
 	}
 
 	@Override
 	public void run() {
 		CompoundAnalysisRequest compoundRequest = (CompoundAnalysisRequest) getRequest();
 		
 		result = new CompoundAnalysisResult(compoundRequest.getSessionId(), compoundRequest.getTaskId());
 		
 		for (AnalysisRequest request : compoundRequest.getRequests()) {
 			//execute the request
 			logger.info("CompoundRequestTaskR: running request: " + request);
 			runRequest(request);
 			if (getException() != null) {
 			  logger.error(getException());
 			  return;
 			}
 		}
 	}
 
 	private void runRequest(AnalysisRequest request) {
 		
 	  //need to revisit the way we are mapping tasks to requests
 		
 	  AnalysisTaskR task = null;	
 	  if (request instanceof ClassComparisonLookupRequest) {
 		task = new ClassComparisonLookupTaskR((ClassComparisonLookupRequest) request, true);
 	  }
 	  else if (request instanceof ClassComparisonRequest) {
 	    task = new ClassComparisonTaskR((ClassComparisonRequest) request, true);
 	  }
 	  else if (request instanceof PrincipalComponentAnalysisRequest) {
 	    task = new PrincipalComponentAnalysisTaskR((PrincipalComponentAnalysisRequest)request, true);
 	  }
 	  else if (request instanceof HierarchicalClusteringRequest){
 		task = new HierarchicalClusteringTaskR((HierarchicalClusteringRequest) request, true);
 	  }
 	
       try {
 		task.setRComputeConnection(this.getRComputeConnection());
 		task.run();
 		if (task.getException() != null) {
 		  throw task.getException();
 		}
 		result.addResult(task.getResult());
 	  } catch (AnalysisServerException e) {
		this.setException(e);
 	  } finally {
 		task.cleanUp();
 	  }
 	}
 
 	@Override
 	public void cleanUp() {
 		try {
 			setRComputeConnection(null);
 		} catch (AnalysisServerException e) {
 			logger.error("Error in cleanUp method.");
 			logger.error(e);
 			setException(e);
 		}
 	}
 
 	@Override
 	public AnalysisResult getResult() {
 		return result;
 	}
 
 }
