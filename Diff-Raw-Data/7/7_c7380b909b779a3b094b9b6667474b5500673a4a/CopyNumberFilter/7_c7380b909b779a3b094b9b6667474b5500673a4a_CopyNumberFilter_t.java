 package gov.nih.nci.nautilus.resultset.filter;
 
 import gov.nih.nci.nautilus.query.OperatorType;
 import gov.nih.nci.nautilus.resultset.DimensionalViewContainer;
 import gov.nih.nci.nautilus.resultset.Resultant;
 import gov.nih.nci.nautilus.resultset.ResultsContainer;
 import gov.nih.nci.nautilus.resultset.copynumber.CopyNumberSingleViewResultsContainer;
 import gov.nih.nci.nautilus.resultset.copynumber.CytobandResultset;
 import gov.nih.nci.nautilus.resultset.copynumber.SampleCopyNumberValuesResultset;
 import gov.nih.nci.nautilus.resultset.sample.SampleResultset;
 import gov.nih.nci.nautilus.resultset.sample.SampleViewResultsContainer;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 public class CopyNumberFilter {
 	static Logger logger = Logger.getLogger(CopyNumberFilter.class);
 	public static Collection filterCopyNumber(Resultant resultant,
 			Integer noOfConsectiveCalls, Integer percentCall,
 			OperatorType operator) {
 		List sampleIDs = new ArrayList();
 		if (resultant != null) {
 			ResultsContainer resultsContainer = resultant.getResultsContainer();
 			List reporterNames;
 			//Get the samples from the resultset object
 			if (resultsContainer != null) {
 				Collection samples = null;
 				CopyNumberSingleViewResultsContainer copyNumberContainer = null;
 				SampleViewResultsContainer sampleViewContainer = null;
 
 				if (resultsContainer instanceof DimensionalViewContainer) {
 					DimensionalViewContainer dimensionalViewContainer = (DimensionalViewContainer) resultsContainer;
 					sampleViewContainer = dimensionalViewContainer.getSampleViewResultsContainer();
 					copyNumberContainer = dimensionalViewContainer.getCopyNumberSingleViewContainer();
 					samples = sampleViewContainer.getBioSpecimenResultsets();
 				}
 				if (copyNumberContainer != null && samples != null
 						&& samples.size() > 0) {
 					reporterNames = copyNumberContainer.getReporterNames();
 					for (Iterator sampleIterator = samples.iterator(); sampleIterator.hasNext();) {
 						SampleResultset sampleResultset = (SampleResultset) sampleIterator.next();
 						if (processCopyNumberFilterPerSample(sampleResultset,reporterNames, noOfConsectiveCalls, percentCall, operator)) {
 							logger.debug(sampleResultset.getBiospecimen().getValue() + "\t"+ "TRUE");
 							sampleIDs.add(sampleResultset.getBiospecimen().getValue().toString());
 						}
 						else{
 							logger.debug(sampleResultset.getBiospecimen().getValue() + "\t"+ "FALSE");
 						}
 						
 					}
 				}
 			}
 
 		}
 		return sampleIDs;
 	}
 
 	private static boolean processCopyNumberFilterPerSample(
 			SampleResultset sampleResultset, List reporterNames, Integer noOfConsectiveCalls, Integer percentCall,OperatorType operator ) {
		float totalCalls = 0;
 		int consectiveCalls = 0;
 		boolean isConsectiveCall = false;
 		boolean isPercentCall = false;
 		if (sampleResultset.getCopyNumberSingleViewResultsContainer() != null
 				&& reporterNames != null) {
 			CopyNumberSingleViewResultsContainer copyNumberContainer = sampleResultset.getCopyNumberSingleViewResultsContainer();
 			String sampleId = sampleResultset.getBiospecimen().getValue()
 					.toString();
 			Collection cytobands = copyNumberContainer.getCytobandResultsets();
 			Collection labels = copyNumberContainer.getGroupsLabels();
 
 			for (Iterator cytobandIterator = cytobands.iterator(); cytobandIterator.hasNext();) {
 				CytobandResultset cytobandResultset = (CytobandResultset) cytobandIterator.next();
 				String cytoband = cytobandResultset.getCytoband().getValue().toString();
 
 				for (Iterator labelIterator = labels.iterator(); labelIterator.hasNext();) {
 					String label = (String) labelIterator.next();
 
 					for (Iterator reporterIterator = reporterNames.iterator(); reporterIterator.hasNext();) {
 						String reporterName = (String) reporterIterator.next();
 						SampleCopyNumberValuesResultset copyNumberValuesResultset = copyNumberContainer.
 										getSampleCopyNumberValuesResultsets(cytoband,
 										reporterName, label, sampleId);
 						if (copyNumberValuesResultset != null  && copyNumberValuesResultset.getCopyNumber() != null) {
 							consectiveCalls++;
 							totalCalls++;
 							//logger.debug(sampleId + "\t"+ reporterName + copyNumberValuesResultset.getCopyNumber().getValue());
 							if(consectiveCalls >= noOfConsectiveCalls.intValue()){
 								isConsectiveCall = true;
 								logger.debug(sampleId + "TRUE "+consectiveCalls );
 
 							}
 						} else {
 							consectiveCalls = 0;
 							//logger.debug("B " + sampleId + "\t"+ reporterName + " count: " + consectiveCalls);
 						}
 					}
 				}
 			}
			float percent = (totalCalls/reporterNames.size()) * 100;
			if(percentCall.intValue() > 0 && percent >= percentCall.intValue()){
 				isPercentCall = true;
 			}
 		}
 		if(operator.equals(OperatorType.AND)){
 			if(isConsectiveCall && isPercentCall){
 				return true;
 			}
 		}
 		else if(operator.equals(OperatorType.OR)){
 			if(isConsectiveCall || isPercentCall){
 				return true;
 			}
 		}
 		return false;
 	}
 
 }
 
