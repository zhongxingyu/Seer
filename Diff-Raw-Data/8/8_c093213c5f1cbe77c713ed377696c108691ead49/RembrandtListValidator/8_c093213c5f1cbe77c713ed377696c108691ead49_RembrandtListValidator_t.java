 package gov.nih.nci.rembrandt.web.helper;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import javax.naming.OperationNotSupportedException;
 
 import org.apache.log4j.Logger;
 
 import gov.nih.nci.caintegrator.application.lists.ListSubType;
 import gov.nih.nci.caintegrator.application.lists.ListType;
 import gov.nih.nci.caintegrator.application.lists.ListValidator;
 import gov.nih.nci.caintegrator.dto.de.CloneIdentifierDE;
 import gov.nih.nci.caintegrator.dto.de.GeneIdentifierDE;
 import gov.nih.nci.caintegrator.dto.de.SNPIdentifierDE;
 import gov.nih.nci.caintegrator.dto.de.SampleIDDE;
 import gov.nih.nci.rembrandt.queryservice.validation.DataValidator;
 import gov.nih.nci.rembrandt.service.findings.strategies.StrategyHelper;
 
 public class RembrandtListValidator extends ListValidator{
     private static Logger logger = Logger.getLogger(RembrandtListValidator.class);
     
 	public RembrandtListValidator(){};
 	
 	public RembrandtListValidator(ListSubType listSubType, ListType listType, List<String> unvalidatedList) throws OperationNotSupportedException {
         super(listType, listSubType, unvalidatedList);    		
 	}
 	public RembrandtListValidator(ListType listType, List<String> unvalidatedList) throws OperationNotSupportedException {
         super(listType, unvalidatedList);		
 	}
 	
 	public void validate(ListType listType, ListSubType listSubType, List<String> myunvalidatedList) throws OperationNotSupportedException {
         List<String> unvalidatedList = new ArrayList<String>();
         for(String s : myunvalidatedList){         
          unvalidatedList.add(s.toUpperCase());
         }
         //check if this has been run before
 		if(validList.isEmpty() && invalidList.isEmpty()){
 			switch(listType){
 			case PatientDID:
 				try {
 					Collection<SampleIDDE> samples = ListConvertor.convertToSampleIDDEs(unvalidatedList);
 					samples = DataValidator.validateSampleIds(samples);
 					validList = new ArrayList<String>();
 					validList.addAll(StrategyHelper.extractSamples(samples));
 					} catch (OperationNotSupportedException e) {
 						validList.clear();
 		    			logger.error("Error in getValidList");
 		    			logger.error(e.getMessage());
 		    			throw e;
 					} catch (Exception e) {
 						validList.clear();
 		    			logger.error("Error in getValidList");
 		    			logger.error(e.getMessage());
 		    			throw new OperationNotSupportedException(e.getMessage());
 					}
 					break;
 			}
 			if(listSubType != null){
 				switch(listSubType){
 					case GENBANK_ACCESSION_NUMBER:
 					case GENESYMBOL:
 					case LOCUS_LINK:
 						try {
 						Collection<GeneIdentifierDE> genes = ListConvertor.convertToGeneIdentifierDE(unvalidatedList, listSubType);
 						genes = DataValidator.validateGenes(genes);
 						validList = new ArrayList<String>();
 						validList.addAll(StrategyHelper.extractGenes(genes));
 						} catch (OperationNotSupportedException e) {
 							validList.clear();
 			    			logger.error("Error in getValidList");
 			    			logger.error(e.getMessage());
 			    			throw e;
 						} catch (Exception e) {
 							validList.clear();
 			    			logger.error("Error in getValidList");
 			    			logger.error(e.getMessage());
 			    			throw new OperationNotSupportedException(e.getMessage());
 						}
 					break;
 					case PROBE_SET:
 					case IMAGE_CLONE:
 						try {
 							Collection<CloneIdentifierDE> reporters = ListConvertor.convertToCloneIdentifierDE(unvalidatedList, listSubType);
 							reporters = DataValidator.validateReporters(reporters);
 							validList = new ArrayList<String>();
 							validList.addAll(StrategyHelper.extractReporters(reporters));
 							} catch (OperationNotSupportedException e) {
 								validList.clear();
 				    			logger.error("Error in getValidList");
 				    			logger.error(e.getMessage());
 				    			throw e;
 							} catch (Exception e) {
 								validList.clear();
 				    			logger.error("Error in getValidList");
 				    			logger.error(e.getMessage());
 				    			throw new OperationNotSupportedException(e.getMessage());
 							}
 					break;
 					case DBSNP:
 					case SNP_PROBESET:
 						try{
 						Collection<SNPIdentifierDE> reporters = ListConvertor.convertToSNPIdentifierDE(unvalidatedList, listSubType);
 						reporters = DataValidator.validateSNPReporters(reporters);
 						validList = new ArrayList<String>();
 						validList.addAll(StrategyHelper.extractSNPReporters(reporters));
 						} catch (OperationNotSupportedException e) {
 							validList.clear();
 			    			logger.error("Error in getValidList");
 			    			logger.error(e.getMessage());
 			    			throw e;
 						} catch (Exception e) {
 							validList.clear();
 			    			logger.error("Error in getValidList");
 			    			logger.error(e.getMessage());
 			    			throw new OperationNotSupportedException(e.getMessage());
 						}
 					break;
 				}
 			}
			else	{ //theses no subtype passed
				logger.debug("List with no subtype");
				validList = new ArrayList<String>();
				validList.addAll(unvalidatedList);
			}
 		}
         invalidList.addAll(unvalidatedList);
         invalidList.removeAll(validList);
 		
 	}
 
     @Override
     public void validate(ListType listType, List<String> unvalidatedList) throws OperationNotSupportedException {
        validate(listType, null, unvalidatedList);
     }
 
 	
 
 }
