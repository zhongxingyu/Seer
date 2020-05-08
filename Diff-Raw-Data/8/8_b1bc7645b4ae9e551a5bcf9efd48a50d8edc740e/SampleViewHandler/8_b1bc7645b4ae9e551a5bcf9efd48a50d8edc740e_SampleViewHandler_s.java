 /*
  *  @author: SahniH
  *  Created on Oct 22, 2004
  *  @version $ Revision: 1.0 $
  * 
  *	The caBIO Software License, Version 1.0
  *
  *	Copyright 2004 SAIC. This software was developed in conjunction with the National Cancer 
  *	Institute, and so to the extent government employees are co-authors, any rights in such works 
  *	shall be subject to Title 17 of the United States Code, section 105.
  * 
  *	Redistribution and use in source and binary forms, with or without modification, are permitted 
  *	provided that the following conditions are met:
  *	 
  *	1. Redistributions of source code must retain the above copyright notice, this list of conditions 
  *	and the disclaimer of Article 3, below.  Redistributions in binary form must reproduce the above 
  *	copyright notice, this list of conditions and the following disclaimer in the documentation and/or 
  *	other materials provided with the distribution.
  * 
  *	2.  The end-user documentation included with the redistribution, if any, must include the 
  *	following acknowledgment:
  *	
  *	"This product includes software developed by the SAIC and the National Cancer 
  *	Institute."
  *	
  *	If no such end-user documentation is to be included, this acknowledgment shall appear in the 
  *	software itself, wherever such third-party acknowledgments normally appear.
  *	 
  *	3. The names "The National Cancer Institute", "NCI" and "SAIC" must not be used to endorse or 
  *	promote products derived from this software.
  *	 
  *	4. This license does not authorize the incorporation of this software into any proprietary 
  *	programs.  This license does not authorize the recipient to use any trademarks owned by either 
  *	NCI or SAIC-Frederick.
  *	 
  *	
  *	5. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED 
  *	WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
  *	MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE) ARE 
  *	DISCLAIMED.  IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE, SAIC, OR 
  *	THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
  *	EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
  *	PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
  *	PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY 
  *	OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING 
  *	NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
  *	SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *	
  */
 package gov.nih.nci.rembrandt.queryservice.resultset.sample;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 
 import gov.nih.nci.caintegrator.dto.de.BioSpecimenIdentifierDE;
 import gov.nih.nci.caintegrator.dto.de.DatumDE;
 import gov.nih.nci.caintegrator.dto.de.DiseaseNameDE;
 import gov.nih.nci.caintegrator.dto.de.GenderDE;
 import gov.nih.nci.caintegrator.dto.de.KarnofskyClinicalEvalDE;
 import gov.nih.nci.caintegrator.dto.de.LanskyClinicalEvalDE;
 import gov.nih.nci.caintegrator.dto.de.MRIClinicalEvalDE;
 import gov.nih.nci.caintegrator.dto.de.NeuroExamClinicalEvalDE;
 import gov.nih.nci.caintegrator.dto.de.RaceDE;
 import gov.nih.nci.caintegrator.dto.de.SampleIDDE;
 import gov.nih.nci.caintegrator.dto.view.GroupType;
 import gov.nih.nci.rembrandt.dbbean.PatientData;
 import gov.nih.nci.rembrandt.queryservice.queryprocessing.ge.GeneExpr;
 import gov.nih.nci.rembrandt.queryservice.resultset.ClinicalResultSet;
 import gov.nih.nci.rembrandt.queryservice.resultset.ResultSet;
 import gov.nih.nci.rembrandt.queryservice.resultset.copynumber.CopyNumberSingleViewHandler;
 import gov.nih.nci.rembrandt.queryservice.resultset.copynumber.CopyNumberSingleViewResultsContainer;
 import gov.nih.nci.rembrandt.queryservice.resultset.gene.GeneExprSingleViewHandler;
 import gov.nih.nci.rembrandt.queryservice.resultset.gene.GeneExprSingleViewResultsContainer;
 import gov.nih.nci.rembrandt.queryservice.validation.ClinicalDataValidator;
 import gov.nih.nci.rembrandt.queryservice.queryprocessing.cgh.CopyNumber;
 
 /**
  * @author SahniH
  * Date: Oct 22, 2004
  * 
  */
 public class SampleViewHandler {
 	private static Logger logger = Logger.getLogger(SampleViewHandler.class);
     public static SampleViewResultsContainer handleSampleView(SampleViewResultsContainer sampleViewContainer, ResultSet resultObj, GroupType groupType){
     	SampleResultset sampleResultset = null;
     	if (sampleViewContainer != null && resultObj instanceof GeneExpr.GeneExprSingle){
     		GeneExpr.GeneExprSingle geneExprObj = (GeneExpr.GeneExprSingle)resultObj;
       		//sampleResultset = handleBioSpecimenResultset(sampleViewContainer,geneExprObj);
     	   	sampleResultset = (SampleResultset) sampleViewContainer.getSampleResultset(geneExprObj.getSampleId());
       		if(sampleResultset == null){ // no record found
       			sampleResultset = new SampleResultset();
       		}
           	//Propulate the GeneExprSingleResultsContainer
       		GeneExprSingleViewResultsContainer geneExprSingleViewContainer = sampleResultset.getGeneExprSingleViewResultsContainer();
         	if(geneExprSingleViewContainer == null){
         		geneExprSingleViewContainer = new GeneExprSingleViewResultsContainer();
         	}
         	geneExprSingleViewContainer = GeneExprSingleViewHandler.handleGeneExprSingleView(geneExprSingleViewContainer,geneExprObj, groupType);
       		sampleResultset.setGeneExprSingleViewResultsContainer(geneExprSingleViewContainer);
            	//Populate the SampleViewResultsContainer
       		sampleViewContainer.addSampleResultset(sampleResultset);
     	}
       	else if(sampleViewContainer != null && resultObj instanceof CopyNumber){
       		CopyNumber copyNumberObj = (CopyNumber)resultObj;
       		//sampleResultset = handleBioSpecimenResultset(sampleViewContainer,copyNumberObj);
     	   	sampleResultset = (SampleResultset) sampleViewContainer.getSampleResultset(copyNumberObj.getSampleId());
       		if(sampleResultset == null){ // no record found
       			sampleResultset = new SampleResultset();
       		}
           	//Propulate the GeneExprSingleResultsContainer
    
           	//Propulate the GeneExprSingleResultsContainer
       		CopyNumberSingleViewResultsContainer copyNumberSingleViewResultsContainer = sampleResultset.getCopyNumberSingleViewResultsContainer();
         	if(copyNumberSingleViewResultsContainer == null){
         		copyNumberSingleViewResultsContainer = new CopyNumberSingleViewResultsContainer();
         	}
         	copyNumberSingleViewResultsContainer = CopyNumberSingleViewHandler.handleCopyNumberSingleView(copyNumberSingleViewResultsContainer,copyNumberObj, groupType);
       		sampleResultset.setCopyNumberSingleViewResultsContainer(copyNumberSingleViewResultsContainer);
            	//Populate the SampleViewResultsContainer
       		sampleViewContainer.addSampleResultset(sampleResultset);
     	}
 
       	return sampleViewContainer;
     }
     public static SampleResultset handleBioSpecimenResultset(SampleViewResultsContainer sampleViewContainer, ClinicalResultSet clinicalObj){
  		//get the gene accessesion number for this record
   		//check if the gene exsists in the GeneExprSingleViewResultsContainer, otherwise add a new one.
     	SampleResultset sampleResultset = (SampleResultset) sampleViewContainer.getSampleResultset(clinicalObj.getSampleId());
   		if(sampleResultset == null){ // no record found
   			sampleResultset = new SampleResultset();
   		}
 		//find out the biospecimenID associated with the GeneExpr.GeneExprSingle
 		//populate the BiospecimenResuluset
 		sampleResultset.setBiospecimen(new BioSpecimenIdentifierDE(clinicalObj.getBiospecimenId()));
 		sampleResultset.setSampleIDDE(new SampleIDDE(clinicalObj.getSampleId().toString()));
 		sampleResultset.setAgeGroup(new DatumDE(DatumDE.AGE_GROUP,clinicalObj.getAgeGroup()));
 		sampleResultset.setSurvivalLengthRange(new DatumDE(DatumDE.SURVIVAL_LENGTH_RANGE,clinicalObj.getSurvivalLengthRange()));
 		sampleResultset.setGenderCode(new GenderDE(clinicalObj.getGenderCode()));
 		sampleResultset.setDisease(new DiseaseNameDE(clinicalObj.getDiseaseType()));			
 /*	    sampleResultset.setWhoGrade(clinicalObj.getWhoGrade());	
 		sampleResultset.setRaceDE(new RaceDE(clinicalObj.getRace()));
 		if(clinicalObj.getAge() != null){
 			sampleResultset.setAge(clinicalObj.getAge());
 		}
 		sampleResultset.setRaceDE(new RaceDE(clinicalObj.getRace()));
 		if(clinicalObj.getSurvivalLength() != null){
 			sampleResultset.setSurvivalLength(clinicalObj.getSurvivalLength());
 		}
 		if(clinicalObj.getCensoringStatus() != null){
 			sampleResultset.setCensor(new DatumDE(DatumDE.CENSOR,clinicalObj.getCensoringStatus()));
 		}
 
 		if(clinicalObj.getKarnofskyScore() != null) {
 		   sampleResultset.setKarnofskyClinicalEvalDE(new KarnofskyClinicalEvalDE(clinicalObj.getKarnofskyScore().toString()));
 		}
 		
 		if(clinicalObj.getKarnofskyScores() != null) {
 			   //sampleResultset.setKarnofskyScores(clinicalObj.getKarnofskyScores());
 			   sampleResultset.setKarnofskyClinicalEvalDE(new KarnofskyClinicalEvalDE(clinicalObj.getKarnofskyScores().toString()));
 				
 		}
 		
 		if(clinicalObj.getLanskyScore() != null) {
 			   sampleResultset.setLanskyClinicalEvalDE(new LanskyClinicalEvalDE(clinicalObj.getLanskyScore().toString()));
 			}
 			   
 	    if(clinicalObj.getLanskyScores() != null) {
 				//sampleResultset.setLanskyClinicalEvalDE(clinicalObj.getLanskyScores());
 				 sampleResultset.setLanskyClinicalEvalDE(new LanskyClinicalEvalDE(clinicalObj.getLanskyScores().toString()));
 					
 			}
 		if(clinicalObj.getNeuroExam() != null) {
 			   sampleResultset.setNeuroExamClinicalEvalDE(new NeuroExamClinicalEvalDE(clinicalObj.getNeuroExam().toString()));
 			}
 		
 		if(clinicalObj.getNeuroExams() != null) {
 			  // sampleResultset.setNeuroExamClinicalEvalDE(clinicalObj.getNeuroExams());
 			   sampleResultset.setNeuroExamClinicalEvalDE(new NeuroExamClinicalEvalDE(clinicalObj.getNeuroExams().toString()));
 				
 			}
 		
 		if(clinicalObj.getMriCtScore() != null) {
 			   sampleResultset.setMriClinicalEvalDE(new MRIClinicalEvalDE(clinicalObj.getMriCtScores().toString()));
 			}
 			
 		
 		if(clinicalObj.getMriCtScores() != null) {
 			  // sampleResultset.setMriClinicalEvalDE(clinicalObj.getMriCtScores());
 			   sampleResultset.setMriClinicalEvalDE(new MRIClinicalEvalDE(clinicalObj.getMriCtScores().toString()));
 				
 			}
 		
 		if(clinicalObj.getTimePoint() != null) {
 			   sampleResultset.setTimePoint(new String(clinicalObj.getTimePoint()));
 			}
 			
 		if(clinicalObj.getTimePoints() != null) {
 			   sampleResultset.setTimePoints(new String(clinicalObj.getTimePoints()));
 			}
 		
 		    if(clinicalObj.getFollowupDate() != null) {
 			   sampleResultset.setFollowupDate(clinicalObj.getFollowupDate());
 			}
 			
 		if(clinicalObj.getFollowupDates() != null) {
 			   sampleResultset.setFollowupDates(clinicalObj.getFollowupDates());
 			}
 		if(clinicalObj.getFollowupMonth() != null) {
 			   sampleResultset.setFollowupMonth(clinicalObj.getFollowupMonth());
 			}
 		
 		if(clinicalObj.getFollowupMonths() != null) {
 			   sampleResultset.setFollowupMonths(clinicalObj.getFollowupMonths());
 			}
 		if(clinicalObj.getNeuroEvaluationDate() != null) {
 			   sampleResultset.setNeuroEvaluationDate(clinicalObj.getNeuroEvaluationDate());
 			}
 		
 		if(clinicalObj.getNeuroEvaluationDates() != null) {
 			   sampleResultset.setNeuroEvaluationDates(clinicalObj.getNeuroEvaluationDates());
 			}
 		if(clinicalObj.getSteroidDoseStatus() != null) {
 			   sampleResultset.setSteroidDoseStatus(clinicalObj.getSteroidDoseStatus());
 			}
 		
 		if(clinicalObj.getSteroidDoseStatuses() != null) {
 			   sampleResultset.setSteroidDoseStatuses(clinicalObj.getSteroidDoseStatuses());
 			}
 		if(clinicalObj.getAntiConvulsantStatus() != null) {
 			   sampleResultset.setAntiConvulsantStatus(clinicalObj.getAntiConvulsantStatus());
 			}
 			
 		if(clinicalObj.getAntiConvulsantStatuses() != null) {
 			   sampleResultset.setAntiConvulsantStatuses(clinicalObj.getAntiConvulsantStatuses());
 			}
 		if(clinicalObj.getPriorRadiationTimePoints() != null) {
 			   sampleResultset.setPriorRadiationTimePoints(clinicalObj.getPriorRadiationTimePoints());
 			}
 		
 		if(clinicalObj.getPriorRadiationRadiationSites() != null) {
 			   sampleResultset.setPriorRadiationRadiationSites(clinicalObj.getPriorRadiationRadiationSites());
 			}
 		
 		if(clinicalObj.getPriorRadiationDoseStartDates() != null) {
 			   sampleResultset.setPriorRadiationDoseStartDates(clinicalObj.getPriorRadiationDoseStartDates());
 			}
 		
 		if(clinicalObj.getPriorRadiationDoseStopDates() != null) {
 			   sampleResultset.setPriorRadiationDoseStopDates(clinicalObj.getPriorRadiationDoseStopDates());
 			}
 		
 		if(clinicalObj.getPriorRadiationFractionDoses() != null) {
 			   sampleResultset.setPriorRadiationFractionDoses(clinicalObj.getPriorRadiationFractionDoses());
 			}
 		
 		if(clinicalObj.getPriorRadiationFractionNumbers() != null) {
 			   sampleResultset.setPriorRadiationFractionNumbers(clinicalObj.getPriorRadiationFractionNumbers());
 			}
 		
 		if(clinicalObj.getPriorRadiationRadiationTypes() != null) {
 			   sampleResultset.setPriorRadiationRadiationTypes(clinicalObj.getPriorRadiationRadiationTypes());
 			}
 		
 		if(clinicalObj.getPriorChemoTimePoints() != null) {
 			   sampleResultset.setPriorChemoTimePoints(clinicalObj.getPriorChemoTimePoints());
 			}
 		
 		if(clinicalObj.getPriorChemoagentIds() != null) {
 			   sampleResultset.setPriorChemoagentIds(clinicalObj.getPriorChemoagentIds());
 			}
 		
 		if(clinicalObj.getPriorChemoAgentNames() != null) {
 			   sampleResultset.setPriorChemoAgentNames(clinicalObj.getPriorChemoAgentNames());
 			}
 		
 		if(clinicalObj.getPriorChemoCourseCounts() != null) {
 			   sampleResultset.setPriorChemoCourseCounts(clinicalObj.getPriorChemoCourseCounts());
 			}
 		
 		if(clinicalObj.getPriorChemoDoseStartDates() != null) {
 			   sampleResultset.setPriorChemoDoseStartDates(clinicalObj.getPriorChemoDoseStartDates());
 			}
 		
 		if(clinicalObj.getPriorChemoDoseStopDates() != null) {
 			   sampleResultset.setPriorChemoDoseStopDates(clinicalObj.getPriorChemoDoseStopDates());
 			}
 		
 		if(clinicalObj.getPriorChemoStudySources() != null) {
 			   sampleResultset.setPriorChemoStudySources(clinicalObj.getPriorChemoStudySources());
 			}
 		
 		if(clinicalObj.getPriorChemoProtocolNumbers() != null) {
 			   sampleResultset.setPriorChemoProtocolNumbers(clinicalObj.getPriorChemoProtocolNumbers());
 			}
 		
 		if(clinicalObj.getPriorSurgeryTimePoints() != null) {
 			   sampleResultset.setPriorSurgeryTimePoints(clinicalObj.getPriorSurgeryTimePoints());
 			}
 		
 		if(clinicalObj.getPriorSurgeryProcedureTitles() != null) {
 			   sampleResultset.setPriorSurgeryProcedureTitles(clinicalObj.getPriorSurgeryProcedureTitles());
 			}
 		
 		if(clinicalObj.getPriorSurgeryTumorHistologys() != null) {
 			   sampleResultset.setPriorSurgeryTumorHistologys(clinicalObj.getPriorSurgeryTumorHistologys());
 			}
 		
 		if(clinicalObj.getPriorSurgerySurgeryDates() != null) {
 			   sampleResultset.setPriorSurgerySurgeryDates(clinicalObj.getPriorSurgerySurgeryDates());
 			}
 		
 		if(clinicalObj.getPriorSurgerySurgeryOutcomes() != null) {
 			   sampleResultset.setPriorSurgerySurgeryOutcomes(clinicalObj.getPriorSurgerySurgeryOutcomes());
 			}
 		
 		
 		if(clinicalObj.getOnStudyRadiationTimePoints() != null) {
 			   sampleResultset.setOnStudyRadiationTimePoints(clinicalObj.getOnStudyRadiationTimePoints());
 			}
 		
 		if(clinicalObj.getOnStudyRadiationRadiationSites() != null) {
 			   sampleResultset.setOnStudyRadiationRadiationSites(clinicalObj.getOnStudyRadiationRadiationSites());
 			}
 		
 		if(clinicalObj.getOnStudyRadiationDoseStartDates() != null) {
 			   sampleResultset.setOnStudyRadiationDoseStartDates(clinicalObj.getOnStudyRadiationDoseStartDates());
 			}
 		
 		if(clinicalObj.getOnStudyRadiationDoseStopDates() != null) {
 			   sampleResultset.setOnStudyRadiationDoseStopDates(clinicalObj.getOnStudyRadiationDoseStopDates());
 			}
 		
 		if(clinicalObj.getOnStudyRadiationFractionDoses() != null) {
 			   sampleResultset.setOnStudyRadiationFractionDoses(clinicalObj.getOnStudyRadiationFractionDoses());
 			}
 		
 		if(clinicalObj.getOnStudyRadiationFractionNumbers() != null) {
 			   sampleResultset.setOnStudyRadiationFractionNumbers(clinicalObj.getOnStudyRadiationFractionNumbers());
 			}
 		
 		if(clinicalObj.getOnStudyRadiationNeurosisStatuses() != null) {
 			   sampleResultset.setOnStudyRadiationNeurosisStatuses(clinicalObj.getOnStudyRadiationNeurosisStatuses());
 			}
 		if(clinicalObj.getOnStudyRadiationRadiationTypes() != null) {
 			   sampleResultset.setOnStudyRadiationRadiationTypes(clinicalObj.getOnStudyRadiationRadiationTypes());
 			}
 		
 		//starts onstudy chemo
 		if(clinicalObj.getOnStudyChemoTimePoints() != null) {
 			   sampleResultset.setOnStudyChemoTimePoints(clinicalObj.getOnStudyChemoTimePoints());
 			}
 		
 		if(clinicalObj.getOnStudyChemoagentIds() != null) {
 			   sampleResultset.setOnStudyChemoagentIds(clinicalObj.getOnStudyChemoagentIds());
 			}
 		
 		if(clinicalObj.getOnStudyChemoAgentNames() != null) {
 			   sampleResultset.setOnStudyChemoAgentNames(clinicalObj.getOnStudyChemoAgentNames());
 			}
 		
 		if(clinicalObj.getOnStudyChemoRegimenNumbers() != null) {
 			   sampleResultset.setOnStudyChemoRegimenNumbers(clinicalObj.getOnStudyChemoRegimenNumbers());
 			}
 		
 		if(clinicalObj.getOnStudyChemoCourseCounts() != null) {
 			   sampleResultset.setOnStudyChemoCourseCounts(clinicalObj.getOnStudyChemoCourseCounts());
 			}
 		
 		if(clinicalObj.getOnStudyChemoDoseStartDates() != null) {
 			   sampleResultset.setOnStudyChemoDoseStartDates(clinicalObj.getOnStudyChemoDoseStartDates());
 			}
 		
 		if(clinicalObj.getOnStudyChemoDoseStopDates() != null) {
 			   sampleResultset.setOnStudyChemoDoseStopDates(clinicalObj.getOnStudyChemoDoseStopDates());
 			}
 		
 		if(clinicalObj.getOnStudyChemoStudySources() != null) {
 			   sampleResultset.setOnStudyChemoStudySources(clinicalObj.getOnStudyChemoStudySources());
 			}
 		
 		if(clinicalObj.getOnStudyChemoProtocolNumbers() != null) {
 			   sampleResultset.setOnStudyChemoProtocolNumbers(clinicalObj.getOnStudyChemoProtocolNumbers());
 			}
 		
 		
 		
 	 // starts onstudy surgery
 		
 		
 		if(clinicalObj.getOnStudySurgeryTimePoints() != null) {
 			   sampleResultset.setOnStudySurgeryTimePoints(clinicalObj.getOnStudySurgeryTimePoints());
 			}
 		
 		if(clinicalObj.getOnStudySurgeryProcedureTitles() != null) {
 			   sampleResultset.setOnStudySurgeryProcedureTitles(clinicalObj.getOnStudySurgeryProcedureTitles());
 			}
 		
 		if(clinicalObj.getOnStudySurgeryHistoDiagnoses() != null) {
 			   sampleResultset.setOnStudySurgeryHistoDiagnoses(clinicalObj.getOnStudySurgeryHistoDiagnoses());
 			}
 		
 		if(clinicalObj.getOnStudySurgerySurgeryDates() != null) {
 			   sampleResultset.setOnStudySurgerySurgeryDates(clinicalObj.getOnStudySurgerySurgeryDates());
 			}
 		
 		if(clinicalObj.getOnStudySurgerySurgeryOutcomes() != null) {
 			   sampleResultset.setOnStudySurgerySurgeryOutcomes(clinicalObj.getOnStudySurgerySurgeryOutcomes());
 			}
 		
 		if(clinicalObj.getOnStudySurgeryIndications() != null) {
 			   sampleResultset.setOnStudySurgeryIndications(clinicalObj.getOnStudySurgeryIndications());
 			}
 		*/
 		
 		return sampleResultset;
     }
     public static SampleResultset handleBioSpecimenResultset(SampleViewResultsContainer sampleViewContainer, PatientData clinicalObj){
  		//get the gene accessesion number for this record
   		//check if the gene exsists in the GeneExprSingleViewResultsContainer, otherwise add a new one.
     	SampleResultset sampleResultset = (SampleResultset) sampleViewContainer.getSampleResultset(clinicalObj.getSampleId());
   		if(sampleResultset == null){ // no record found
   			sampleResultset = new SampleResultset();
   		}
 		//find out the biospecimenID associated with the GeneExpr.GeneExprSingle
 		//populate the BiospecimenResuluset
 		sampleResultset.setBiospecimen(new BioSpecimenIdentifierDE(clinicalObj.getBiospecimenId()));
 		sampleResultset.setSampleIDDE(new SampleIDDE(clinicalObj.getSampleId().toString()));
 		sampleResultset.setAgeGroup(new DatumDE(DatumDE.AGE_GROUP,clinicalObj.getAgeGroup()));
 		sampleResultset.setSurvivalLengthRange(new DatumDE(DatumDE.SURVIVAL_LENGTH_RANGE,clinicalObj.getSurvivalLengthRange()));
 		sampleResultset.setGenderCode(new GenderDE(clinicalObj.getGenderCode()));
 		sampleResultset.setDisease(new DiseaseNameDE(clinicalObj.getDiseaseType()));			
 	    sampleResultset.setWhoGrade(clinicalObj.getWhoGrade());	
 		sampleResultset.setRaceDE(new RaceDE(clinicalObj.getRace()));
 		if(clinicalObj.getAge() != null){
 			sampleResultset.setAge(clinicalObj.getAge());
 		}
 		sampleResultset.setRaceDE(new RaceDE(clinicalObj.getRace()));
 		if(clinicalObj.getSurvivalLength() != null){
 			sampleResultset.setSurvivalLength(clinicalObj.getSurvivalLength());
 		}
 		if(clinicalObj.getCensoringStatus() != null){
 			sampleResultset.setCensor(new DatumDE(DatumDE.CENSOR,clinicalObj.getCensoringStatus()));
 		}
 
 		if(clinicalObj.getKarnofskyScore() != null) {
 		   sampleResultset.setKarnofskyClinicalEvalDE(new KarnofskyClinicalEvalDE(clinicalObj.getKarnofskyScore().toString()));
 		}
 		
 		if(clinicalObj.getKarnofskyScores() != null) {
 			   //sampleResultset.setKarnofskyScores(clinicalObj.getKarnofskyScores());
 			   sampleResultset.setKarnofskyClinicalEvalDE(new KarnofskyClinicalEvalDE(clinicalObj.getKarnofskyScores().toString()));
 				
 		}
 		
 		if(clinicalObj.getLanskyScore() != null) {
 			   sampleResultset.setLanskyClinicalEvalDE(new LanskyClinicalEvalDE(clinicalObj.getLanskyScore().toString()));
 			}
 			   
 	    if(clinicalObj.getLanskyScores() != null) {
 				//sampleResultset.setLanskyClinicalEvalDE(clinicalObj.getLanskyScores());
 				 sampleResultset.setLanskyClinicalEvalDE(new LanskyClinicalEvalDE(clinicalObj.getLanskyScores().toString()));
 					
 			}
 		if(clinicalObj.getNeuroExam() != null) {
 			   sampleResultset.setNeuroExamClinicalEvalDE(new NeuroExamClinicalEvalDE(clinicalObj.getNeuroExam().toString()));
 			}
 		
 		if(clinicalObj.getNeuroExams() != null) {
 			  //sampleResultset.setNeuroExamClinicalEvalDE(clinicalObj.getNeuroExams());
 			   sampleResultset.setNeuroExamClinicalEvalDE(new NeuroExamClinicalEvalDE(clinicalObj.getNeuroExams().toString()));
 				
 			}
 		
 		if(clinicalObj.getNeuroExamDescs() != null) {
 			   sampleResultset.setNeuroExamDescs(new String(clinicalObj.getNeuroExamDescs()));	
 			}
 		
 		if(clinicalObj.getMriCtScore() != null) {
 			   sampleResultset.setMriClinicalEvalDE(new MRIClinicalEvalDE(clinicalObj.getMriCtScores().toString()));
 			}
 			
 		
 		if(clinicalObj.getMriCtScores() != null) {
 			  // sampleResultset.setMriClinicalEvalDE(clinicalObj.getMriCtScores());
 			   sampleResultset.setMriClinicalEvalDE(new MRIClinicalEvalDE(clinicalObj.getMriCtScores().toString()));
 				
 			}
 		
 		if(clinicalObj.getMriScoreDescs() != null) {
 			   sampleResultset.setMriScoreDescs(new String(clinicalObj.getMriScoreDescs()));	
 			}
 		
 		if(clinicalObj.getTimePoint() != null) {
 			   sampleResultset.setTimePoint(new String(clinicalObj.getTimePoint()));
 			}
 			
 		if(clinicalObj.getTimePoints() != null) {
 			   sampleResultset.setTimePoints(new String(clinicalObj.getTimePoints()));
 			}
 		
 		    if(clinicalObj.getFollowupDate() != null) {
 			   sampleResultset.setFollowupDate(clinicalObj.getFollowupDate());
 			}
 			
 		if(clinicalObj.getFollowupDates() != null) {
 			   sampleResultset.setFollowupDates(clinicalObj.getFollowupDates());
 			}
 		if(clinicalObj.getFollowupMonth() != null) {
 			   sampleResultset.setFollowupMonth(clinicalObj.getFollowupMonth());
 			}
 		
 		if(clinicalObj.getFollowupMonths() != null) {
 			   sampleResultset.setFollowupMonths(clinicalObj.getFollowupMonths());
 			}
 		if(clinicalObj.getNeuroEvaluationDate() != null) {
 			   sampleResultset.setNeuroEvaluationDate(clinicalObj.getNeuroEvaluationDate());
 			}
 		
 		if(clinicalObj.getNeuroEvaluationDates() != null) {
 			   sampleResultset.setNeuroEvaluationDates(clinicalObj.getNeuroEvaluationDates());
 			}
 		if(clinicalObj.getSteroidDoseStatus() != null) {
 			   sampleResultset.setSteroidDoseStatus(clinicalObj.getSteroidDoseStatus());
 			}
 		
 		if(clinicalObj.getSteroidDoseStatuses() != null) {
 			   sampleResultset.setSteroidDoseStatuses(clinicalObj.getSteroidDoseStatuses());
 			}
 		if(clinicalObj.getAntiConvulsantStatus() != null) {
 			   sampleResultset.setAntiConvulsantStatus(clinicalObj.getAntiConvulsantStatus());
 			}
 			
 		if(clinicalObj.getAntiConvulsantStatuses() != null) {
 			   sampleResultset.setAntiConvulsantStatuses(clinicalObj.getAntiConvulsantStatuses());
 			}
 		if(clinicalObj.getPriorRadiationTimePoints() != null) {
 			   sampleResultset.setPriorRadiationTimePoints(clinicalObj.getPriorRadiationTimePoints());
 			}
 		
 		if(clinicalObj.getPriorRadiationRadiationSites() != null) {
 			   sampleResultset.setPriorRadiationRadiationSites(clinicalObj.getPriorRadiationRadiationSites());
 			}
 		
 		if(clinicalObj.getPriorRadiationDoseStartDates() != null) {
 			   sampleResultset.setPriorRadiationDoseStartDates(clinicalObj.getPriorRadiationDoseStartDates());
 			}
 		
 		if(clinicalObj.getPriorRadiationDoseStopDates() != null) {
 			   sampleResultset.setPriorRadiationDoseStopDates(clinicalObj.getPriorRadiationDoseStopDates());
 			}
 		
 		if(clinicalObj.getPriorRadiationFractionDoses() != null) {
 			   sampleResultset.setPriorRadiationFractionDoses(clinicalObj.getPriorRadiationFractionDoses());
 			}
 		
 		if(clinicalObj.getPriorRadiationFractionNumbers() != null) {
 			   sampleResultset.setPriorRadiationFractionNumbers(clinicalObj.getPriorRadiationFractionNumbers());
 			}
 		
 		if(clinicalObj.getPriorRadiationRadiationTypes() != null) {
 			   sampleResultset.setPriorRadiationRadiationTypes(clinicalObj.getPriorRadiationRadiationTypes());
 			}
 		
 		if(clinicalObj.getPriorChemoTimePoints() != null) {
 			   sampleResultset.setPriorChemoTimePoints(clinicalObj.getPriorChemoTimePoints());
 			}
 		
 		if(clinicalObj.getPriorChemoagentIds() != null) {
 			   sampleResultset.setPriorChemoagentIds(clinicalObj.getPriorChemoagentIds());
 			}
 		
 		if(clinicalObj.getPriorChemoAgentNames() != null) {
 			   sampleResultset.setPriorChemoAgentNames(clinicalObj.getPriorChemoAgentNames());
 			}
 		
 		if(clinicalObj.getPriorChemoCourseCounts() != null) {
 			   sampleResultset.setPriorChemoCourseCounts(clinicalObj.getPriorChemoCourseCounts());
 			}
 		
 		if(clinicalObj.getPriorChemoDoseStartDates() != null) {
 			   sampleResultset.setPriorChemoDoseStartDates(clinicalObj.getPriorChemoDoseStartDates());
 			}
 		
 		if(clinicalObj.getPriorChemoDoseStopDates() != null) {
 			   sampleResultset.setPriorChemoDoseStopDates(clinicalObj.getPriorChemoDoseStopDates());
 			}
 		
 		if(clinicalObj.getPriorChemoStudySources() != null) {
 			   sampleResultset.setPriorChemoStudySources(clinicalObj.getPriorChemoStudySources());
 			}
 		
 		if(clinicalObj.getPriorChemoProtocolNumbers() != null) {
 			   sampleResultset.setPriorChemoProtocolNumbers(clinicalObj.getPriorChemoProtocolNumbers());
 			}
 		
 		if(clinicalObj.getPriorSurgeryTimePoints() != null) {
 			   sampleResultset.setPriorSurgeryTimePoints(clinicalObj.getPriorSurgeryTimePoints());
 			}
 		
 		if(clinicalObj.getPriorSurgeryProcedureTitles() != null) {
 			   sampleResultset.setPriorSurgeryProcedureTitles(clinicalObj.getPriorSurgeryProcedureTitles());
 			}
 		
 		if(clinicalObj.getPriorSurgeryTumorHistologys() != null) {
 			   sampleResultset.setPriorSurgeryTumorHistologys(clinicalObj.getPriorSurgeryTumorHistologys());
 			}
 		
 		if(clinicalObj.getPriorSurgerySurgeryDates() != null) {
 			   sampleResultset.setPriorSurgerySurgeryDates(clinicalObj.getPriorSurgerySurgeryDates());
 			}
 		
 		if(clinicalObj.getPriorSurgerySurgeryOutcomes() != null) {
 			   sampleResultset.setPriorSurgerySurgeryOutcomes(clinicalObj.getPriorSurgerySurgeryOutcomes());
 			}
 		
 		
 		if(clinicalObj.getOnStudyRadiationTimePoints() != null) {
 			   sampleResultset.setOnStudyRadiationTimePoints(clinicalObj.getOnStudyRadiationTimePoints());
 			}
 		
 		if(clinicalObj.getOnStudyRadiationRadiationSites() != null) {
 			   sampleResultset.setOnStudyRadiationRadiationSites(clinicalObj.getOnStudyRadiationRadiationSites());
 			}
 		
 		if(clinicalObj.getOnStudyRadiationDoseStartDates() != null) {
 			   sampleResultset.setOnStudyRadiationDoseStartDates(clinicalObj.getOnStudyRadiationDoseStartDates());
 			}
 		
 		if(clinicalObj.getOnStudyRadiationDoseStopDates() != null) {
 			   sampleResultset.setOnStudyRadiationDoseStopDates(clinicalObj.getOnStudyRadiationDoseStopDates());
 			}
 		
 		if(clinicalObj.getOnStudyRadiationFractionDoses() != null) {
 			   sampleResultset.setOnStudyRadiationFractionDoses(clinicalObj.getOnStudyRadiationFractionDoses());
 			}
 		
 		if(clinicalObj.getOnStudyRadiationFractionNumbers() != null) {
 			   sampleResultset.setOnStudyRadiationFractionNumbers(clinicalObj.getOnStudyRadiationFractionNumbers());
 			}
 		
 		if(clinicalObj.getOnStudyRadiationNeurosisStatuses() != null) {
 			   sampleResultset.setOnStudyRadiationNeurosisStatuses(clinicalObj.getOnStudyRadiationNeurosisStatuses());
 			}
 		if(clinicalObj.getOnStudyRadiationRadiationTypes() != null) {
 			   sampleResultset.setOnStudyRadiationRadiationTypes(clinicalObj.getOnStudyRadiationRadiationTypes());
 			}
 		
 		//starts onstudy chemo
 		if(clinicalObj.getOnStudyChemoTimePoints() != null) {
 			   sampleResultset.setOnStudyChemoTimePoints(clinicalObj.getOnStudyChemoTimePoints());
 			}
 		
 		if(clinicalObj.getOnStudyChemoagentIds() != null) {
 			   sampleResultset.setOnStudyChemoagentIds(clinicalObj.getOnStudyChemoagentIds());
 			}
 		
 		if(clinicalObj.getOnStudyChemoAgentNames() != null) {
 			   sampleResultset.setOnStudyChemoAgentNames(clinicalObj.getOnStudyChemoAgentNames());
 			}
 		
 		if(clinicalObj.getOnStudyChemoRegimenNumbers() != null) {
 			   sampleResultset.setOnStudyChemoRegimenNumbers(clinicalObj.getOnStudyChemoRegimenNumbers());
 			}
 		
 		if(clinicalObj.getOnStudyChemoCourseCounts() != null) {
 			   sampleResultset.setOnStudyChemoCourseCounts(clinicalObj.getOnStudyChemoCourseCounts());
 			}
 		
 		if(clinicalObj.getOnStudyChemoDoseStartDates() != null) {
 			   sampleResultset.setOnStudyChemoDoseStartDates(clinicalObj.getOnStudyChemoDoseStartDates());
 			}
 		
 		if(clinicalObj.getOnStudyChemoDoseStopDates() != null) {
 			   sampleResultset.setOnStudyChemoDoseStopDates(clinicalObj.getOnStudyChemoDoseStopDates());
 			}
 		
 		if(clinicalObj.getOnStudyChemoStudySources() != null) {
 			   sampleResultset.setOnStudyChemoStudySources(clinicalObj.getOnStudyChemoStudySources());
 			}
 		
 		if(clinicalObj.getOnStudyChemoProtocolNumbers() != null) {
 			   sampleResultset.setOnStudyChemoProtocolNumbers(clinicalObj.getOnStudyChemoProtocolNumbers());
 			}
 		
 		
 		
 	 // starts onstudy surgery
 		
 		
 		if(clinicalObj.getOnStudySurgeryTimePoints() != null) {
 			   sampleResultset.setOnStudySurgeryTimePoints(clinicalObj.getOnStudySurgeryTimePoints());
 			}
 		
 		if(clinicalObj.getOnStudySurgeryProcedureTitles() != null) {
 			   sampleResultset.setOnStudySurgeryProcedureTitles(clinicalObj.getOnStudySurgeryProcedureTitles());
 			}
 		
 		if(clinicalObj.getOnStudySurgeryHistoDiagnoses() != null) {
 			   sampleResultset.setOnStudySurgeryHistoDiagnoses(clinicalObj.getOnStudySurgeryHistoDiagnoses());
 			}
 		
 		if(clinicalObj.getOnStudySurgerySurgeryDates() != null) {
 			   sampleResultset.setOnStudySurgerySurgeryDates(clinicalObj.getOnStudySurgerySurgeryDates());
 			}
 		
 		if(clinicalObj.getOnStudySurgerySurgeryOutcomes() != null) {
 			   sampleResultset.setOnStudySurgerySurgeryOutcomes(clinicalObj.getOnStudySurgerySurgeryOutcomes());
 			}
 		
 		if(clinicalObj.getOnStudySurgeryIndications() != null) {
 			   sampleResultset.setOnStudySurgeryIndications(clinicalObj.getOnStudySurgeryIndications());
 			}
 		
 		
 		return sampleResultset;
     }
 	/**
 	 * @param sampleViewResultsContainer
 	 * @param patientDataObj
 	 * @return
 	 */
 	public static SampleViewResultsContainer handleSampleView(SampleViewResultsContainer sampleViewResultsContainer, PatientData patientDataObj) {
     	SampleResultset sampleResultset = null;
     	if (sampleViewResultsContainer != null && patientDataObj != null){
       		sampleResultset = handleBioSpecimenResultset(sampleViewResultsContainer,patientDataObj);
            	//Populate the SampleViewResultsContainer
       		sampleViewResultsContainer.addSampleResultset(sampleResultset);
     	}
     	return sampleViewResultsContainer;
 	}
 	public static SampleViewResultsContainer populateWithClinicalData(ClinicalResultSet[] clinicalObjs ) throws Exception {
 		SampleViewResultsContainer sampleViewResultsContainer = new SampleViewResultsContainer();
 		if(clinicalObjs !=null){
 			Set<String> sampleIDset = new HashSet<String>();			
 			for (int i = 0; i < clinicalObjs.length; i++) {
 				if(clinicalObjs[i] != null){
 					sampleIDset.add(clinicalObjs[i].getSampleId());
 				}
 			}
 			try {
				Collection <SampleResultset> sampleResultsets = ClinicalDataValidator.executeClinicalQueryForSampleList(sampleIDset);
				for(SampleResultset sampleResultset:sampleResultsets){
					sampleViewResultsContainer.addSampleResultset(sampleResultset);
 				}
 				
 			} catch (Exception e) {
 				logger.error(e.getMessage());
 				throw e;
 			}
 		}
 		return sampleViewResultsContainer;
 	}
 }
