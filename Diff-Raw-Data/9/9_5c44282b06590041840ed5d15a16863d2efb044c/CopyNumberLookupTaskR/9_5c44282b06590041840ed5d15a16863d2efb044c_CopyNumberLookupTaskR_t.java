 package gov.nih.nci.caintegrator.analysis.server;
 
 import gov.nih.nci.caintegrator.analysis.messaging.AnalysisRequest;
 import gov.nih.nci.caintegrator.analysis.messaging.AnalysisResult;
 import gov.nih.nci.caintegrator.analysis.messaging.CategoricalCorrelationRequest;
 import gov.nih.nci.caintegrator.analysis.messaging.CategoricalCorrelationResult;
 import gov.nih.nci.caintegrator.analysis.messaging.CopyNumberLookupRequest;
 import gov.nih.nci.caintegrator.analysis.messaging.CopyNumberLookupResult;
 import gov.nih.nci.caintegrator.analysis.messaging.DataPoint;
 import gov.nih.nci.caintegrator.analysis.messaging.DataPointVector;
 import gov.nih.nci.caintegrator.analysis.messaging.ExpressionLookupRequest;
 import gov.nih.nci.caintegrator.analysis.messaging.ExpressionLookupResult;
 import gov.nih.nci.caintegrator.analysis.messaging.ReporterGroup;
 import gov.nih.nci.caintegrator.analysis.messaging.ReporterInfo;
 import gov.nih.nci.caintegrator.analysis.messaging.SampleGroup;
 import gov.nih.nci.caintegrator.enumeration.AxisType;
 import gov.nih.nci.caintegrator.exceptions.AnalysisServerException;
 
 import java.util.List;
 import java.util.Vector;
 
 import org.apache.log4j.Logger;
 import org.rosuda.JRclient.REXP;
 
 public class CopyNumberLookupTaskR extends AnalysisTaskR {
 
 	private CopyNumberLookupResult result;
 	
 	private static Logger logger = Logger.getLogger(CopyNumberLookupTaskR.class);
 	private CopyNumberLookupRequest lookupRequest;
 
 	
 	public CopyNumberLookupTaskR(AnalysisRequest request) {
 		super(request);
 	}
 
 	public CopyNumberLookupTaskR(AnalysisRequest request, boolean debugRcommands) {
 		super(request, debugRcommands);
 		
 	}
 
 	@Override
 	public void run() {
 		lookupRequest = (CopyNumberLookupRequest) getRequest();
 		result = new CopyNumberLookupResult(getRequest().getSessionId(), getRequest().getTaskId());
 		logger.info(getExecutingThreadName() + " processing expression lookup request="
 						+ lookupRequest);
 							
 		try {
 			
 			
 			//no data vectors were send to look up reporters 
 			ReporterGroup reporters = lookupRequest.getReporters();
 			SampleGroup samples = lookupRequest.getSamples();
 			String rBinaryFileName = lookupRequest.getDataFileName();
 			
 			DataPointVector dpv ;
 			for (String reporterName : reporters) {
 			  dpv = getDataPointVector(rBinaryFileName, reporterName, samples, AxisType.X_AXIS);
 			  if (dpv != null) {
 			    result.addDataVector(dpv);
 			  }
 			  else {
 			    logger.error("Error getting data point vector for reporter =" + reporterName);
 			    return;
 			  }
 			}
 		}			
 		catch (Exception ex) {
 			AnalysisServerException asex = new AnalysisServerException(
 			"Internal Error. Caught Exception in CorrelationTaskR exClass=" + ex.getClass() + " msg=" + ex.getMessage());
 	        asex.setFailedRequest(lookupRequest);
 	        setException(asex);
 	        logger.error("Caught Exception in CorrelationTaskR");
 	        logStackTrace(logger, ex);     
 	        return;  
 		}
 	}
 
 	/**
 	 * Get the expression values for the reporter specified in the ReporterInfo object.
 	 * @param ri
 	 * @param axis
 	 * @return
 	 */
 	private DataPointVector getDataPointVector(String rBinaryFileName, String reporterName, SampleGroup samples, AxisType axis) {
 		
 		try {
			DataPointVector dpVector = new DataPointVector(reporterName);
 			setDataFile(rBinaryFileName);
 		    String cmd;
 		   
 		
 			if ((samples != null)&&(!samples.isEmpty())) {
 			  String sampleIdscmd = getRgroupCmd("sampleIds", samples);
 			  doRvoidEval(sampleIdscmd);
 			
 			  cmd = "SM <- getSubmatrix.onegrp(dataMatrix, sampleIds)";
 			  doRvoidEval(cmd);			  
 			}
 			else {
 			  //use the entire data matrix without restricting on samples
 			  cmd = "SM <- dataMatrix";
 			  doRvoidEval(cmd);
 			}
 			
 			
 			cmd = "RM <- getSubmatrix.rep(SM,\"" + reporterName + "\")";
 			double[] vec =  doREval(cmd).asDoubleArray();
 			//need to make sure that the vectors are in the same order wrt sample ids
 			cmd = "RMlabels <- dimnames(RM)[[1]]";
 			Vector RM_ids = doREval(cmd).asVector();
 			
 		    if (RM_ids == null) {
 		      throw new AnalysisServerException("No expression data found for reporter " + reporterName + " in data file=" + rBinaryFileName + ".");
 		    }
 			
 			DataPoint point;
 			String id;
 			REXP exp;
 			for (int i=0; i < RM_ids.size(); i++) {
 			  exp = (REXP) RM_ids.get(i);
 			  id = exp.asString();
 			  
 			  //logger.info("Adding data point with id=" + id);
 			  point = new DataPoint(id);	
 			  
 			  if (axis == AxisType.X_AXIS) {
 			    point.setX(vec[i]);
 			  }
 			  else if (axis == AxisType.Y_AXIS) {
 			    point.setY(vec[i]);
 			  }
 			  else if (axis == AxisType.Z_AXIS) {
 			    point.setZ(vec[i]);
 			  }
 			  dpVector.addDataPoint(point);
 			}
 			return dpVector;
 		}
 		catch (AnalysisServerException asex) {
 		    asex.setFailedRequest(lookupRequest);
 	        setException(asex);
 	        logger.error("Caught AnalysisServerException");
 	        logStackTrace(logger, asex);
 	        return null;  
 		}
 		catch (Exception ex) {
 			AnalysisServerException asex = new AnalysisServerException(
 			"Internal Error. Caught Exception in CorrelationTaskR exClass=" + ex.getClass() + " msg=" + ex.getMessage());
 	        asex.setFailedRequest(lookupRequest);
 	        setException(asex);
 	        logger.error("Caught Exception in CorrelationTaskR");
 	        logStackTrace(logger, ex);
 	        return null;  
 		}
 	}
 
 	@Override
 	public void cleanUp() {
 		try {
 			setRComputeConnection(null);
 		} catch (AnalysisServerException e) {
 			logger.error("Error in cleanUp method.");
 			logStackTrace(logger, e);
 			setException(e);
 		}
 	}
 
 	@Override
 	public AnalysisResult getResult() {
 		return result;
 	}
 	
 	/**
 	 * 
 	 * @param points
 	 * @param useX
 	 * @param useY
 	 * @return Quoted string for use in R command
 	 */
 //	private String getDataString(List<DataPoint> points, boolean useX, boolean useY) {
 //	  StringBuffer sb = new StringBuffer();
 //	  DataPoint point;
 //	  boolean pointAppended = false;
 //	  for (Iterator i=points.iterator(); i.hasNext(); ) {
 //		 point = (DataPoint) i.next();
 //		 
 //		 if ((point.getX()!=null)&&(point.getY()!=null)) {
 //			 if (pointAppended) {
 //		       sb.append(",");
 //			 }
 //			 if (useX) {
 //			   sb.append("\"").append(point.getX()).append("\"");
 //			 } 
 //			 
 //			 
 //			 if (useY) {
 //			   sb.append("\"").append(point.getY()).append("\"");
 //			 }
 //			 pointAppended = true;
 //		 }
 //	  }
 //	  sb.append(")");
 //	  return sb.toString();
 //      
 //	}
 	  
 }
