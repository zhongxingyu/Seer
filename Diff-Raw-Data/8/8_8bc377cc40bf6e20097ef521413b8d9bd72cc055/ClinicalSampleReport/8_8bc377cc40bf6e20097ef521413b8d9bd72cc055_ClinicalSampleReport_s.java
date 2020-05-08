 package gov.nih.nci.rembrandt.web.xml;
 
 import gov.nih.nci.rembrandt.queryservice.resultset.DimensionalViewContainer;
 import gov.nih.nci.rembrandt.queryservice.resultset.Resultant;
 import gov.nih.nci.rembrandt.queryservice.resultset.ResultsContainer;
 import gov.nih.nci.rembrandt.queryservice.resultset.sample.SampleResultset;
 import gov.nih.nci.rembrandt.queryservice.resultset.sample.SampleViewResultsContainer;
 import gov.nih.nci.rembrandt.util.DEUtils;
 
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.dom4j.Document;
 import org.dom4j.DocumentHelper;
 import org.dom4j.Element;
 
 /**
  * @author LandyR
  * Feb 8, 2005
  * 
  */
 
 
 /**
 * caIntegrator License
 * 
 * Copyright 2001-2005 Science Applications International Corporation ("SAIC"). 
 * The software subject to this notice and license includes both human readable source code form and machine readable, 
 * binary, object code form ("the caIntegrator Software"). The caIntegrator Software was developed in conjunction with 
 * the National Cancer Institute ("NCI") by NCI employees and employees of SAIC. 
 * To the extent government employees are authors, any rights in such works shall be subject to Title 17 of the United States
 * Code, section 105. 
 * This caIntegrator Software License (the "License") is between NCI and You. "You (or "Your") shall mean a person or an 
 * entity, and all other entities that control, are controlled by, or are under common control with the entity. "Control" 
 * for purposes of this definition means (i) the direct or indirect power to cause the direction or management of such entity,
 *  whether by contract or otherwise, or (ii) ownership of fifty percent (50%) or more of the outstanding shares, or (iii) 
 * beneficial ownership of such entity. 
 * This License is granted provided that You agree to the conditions described below. NCI grants You a non-exclusive, 
 * worldwide, perpetual, fully-paid-up, no-charge, irrevocable, transferable and royalty-free right and license in its rights 
 * in the caIntegrator Software to (i) use, install, access, operate, execute, copy, modify, translate, market, publicly 
 * display, publicly perform, and prepare derivative works of the caIntegrator Software; (ii) distribute and have distributed 
 * to and by third parties the caIntegrator Software and any modifications and derivative works thereof; 
 * and (iii) sublicense the foregoing rights set out in (i) and (ii) to third parties, including the right to license such 
 * rights to further third parties. For sake of clarity, and not by way of limitation, NCI shall have no right of accounting
 * or right of payment from You or Your sublicensees for the rights granted under this License. This License is granted at no
 * charge to You. 
 * 1. Your redistributions of the source code for the Software must retain the above copyright notice, this list of conditions
 *    and the disclaimer and limitation of liability of Article 6, below. Your redistributions in object code form must reproduce 
 *    the above copyright notice, this list of conditions and the disclaimer of Article 6 in the documentation and/or other materials
 *    provided with the distribution, if any. 
 * 2. Your end-user documentation included with the redistribution, if any, must include the following acknowledgment: "This 
 *    product includes software developed by SAIC and the National Cancer Institute." If You do not include such end-user 
 *    documentation, You shall include this acknowledgment in the Software itself, wherever such third-party acknowledgments 
 *    normally appear.
 * 3. You may not use the names "The National Cancer Institute", "NCI" "Science Applications International Corporation" and 
 *    "SAIC" to endorse or promote products derived from this Software. This License does not authorize You to use any 
 *    trademarks, service marks, trade names, logos or product names of either NCI or SAIC, except as required to comply with
 *    the terms of this License. 
 * 4. For sake of clarity, and not by way of limitation, You may incorporate this Software into Your proprietary programs and 
 *    into any third party proprietary programs. However, if You incorporate the Software into third party proprietary 
 *    programs, You agree that You are solely responsible for obtaining any permission from such third parties required to 
 *    incorporate the Software into such third party proprietary programs and for informing Your sublicensees, including 
 *    without limitation Your end-users, of their obligation to secure any required permissions from such third parties 
 *    before incorporating the Software into such third party proprietary software programs. In the event that You fail 
 *    to obtain such permissions, You agree to indemnify NCI for any claims against NCI by such third parties, except to 
 *    the extent prohibited by law, resulting from Your failure to obtain such permissions. 
 * 5. For sake of clarity, and not by way of limitation, You may add Your own copyright statement to Your modifications and 
 *    to the derivative works, and You may provide additional or different license terms and conditions in Your sublicenses 
 *    of modifications of the Software, or any derivative works of the Software as a whole, provided Your use, reproduction, 
 *    and distribution of the Work otherwise complies with the conditions stated in this License.
 * 6. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, 
 *    THE IMPLIED WARRANTIES OF MERCHANTABILITY, NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED. 
 *    IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE, SAIC, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT, 
 *    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE 
 *    GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
 *    LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
 *    OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 */
 
 public class ClinicalSampleReport implements ReportGenerator {
 
 	/**
 	 * 
 	 */
 	public ClinicalSampleReport () {
 		super();
 	}
 
 	/* (non-Javadoc)
 	 * @see gov.nih.nci.nautilus.ui.report.ReportGenerator#getTemplate(gov.nih.nci.nautilus.resultset.Resultant, java.lang.String)
 	 */
 	@SuppressWarnings("unchecked")
 	public Document getReportXML(Resultant resultant, Map filterMapParams) {
 
 		//String theColors[] = { "B6C5F2","F2E3B5","DAE1F9","C4F2B5","819BE9", "E9CF81" };
 		DecimalFormat resultFormat = new DecimalFormat("0.0000");
 		String defaultV = "-";
 		
 			Document document = DocumentHelper.createDocument();
 
 			try	{
 			Element report = document.addElement( "Report" );
 			Element cell = null;
 			Element data = null;
 			Element dataRow = null;
 			//add the atts
 	        report.addAttribute("reportType", "Clinical");
 	        //fudge these for now
 	        report.addAttribute("groupBy", "none");
 	        String queryName = resultant.getAssociatedQuery().getQueryName();
 	        
 	        //set the queryName to be unique for session/cache access
 	        report.addAttribute("queryName", queryName);
 	        report.addAttribute("sessionId", "the session id");
 	        report.addAttribute("creationTime", "right now");
 
 	        
 		    boolean gLinks = false;
 			boolean cLinks = false;
 			StringBuffer sb = new StringBuffer();
 			
 			ResultsContainer  resultsContainer = resultant.getResultsContainer();
 			SampleViewResultsContainer sampleViewContainer = null;
 			if(resultsContainer instanceof DimensionalViewContainer)	{
 				
 				DimensionalViewContainer dimensionalViewContainer = (DimensionalViewContainer) resultsContainer;
 						// Are we making hyperlinks?
 						if(dimensionalViewContainer.getGeneExprSingleViewContainer() != null)	{
 							// show the geneExprHyperlinks
 							gLinks = true;						
 						}
 						if(dimensionalViewContainer.getCopyNumberSingleViewContainer() != null)	{
 							// show the copyNumberHyperlinks
 							cLinks = true;
 						}
 
 				sampleViewContainer = dimensionalViewContainer.getSampleViewResultsContainer();
 				
 			}
 			else if (resultsContainer instanceof SampleViewResultsContainer)	{
 				sampleViewContainer = (SampleViewResultsContainer) resultsContainer;
 			}
 			
 			Collection samples = sampleViewContainer.getSampleResultsets();
 
 			//	set up the headers for this table 
 			Element headerRow = report.addElement("Row").addAttribute("name", "headerRow");
 			
 			for(String h : ClinicalSampleReport.getClinicalHeaderValues()){
 				cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 			        data = cell.addElement("Data").addAttribute("type", "header").addText(h);
 			        data = null;
 		        cell = null;			
 			}
 			//append these to header if necessary
 			Iterator si = samples.iterator(); 
 			if(si.hasNext())	{
 				SampleResultset sampleResultset =  (SampleResultset)si.next();
    				if(sampleResultset.getGeneExprSingleViewResultsContainer() != null)	{
 					cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 				        data = cell.addElement("Data").addAttribute("type", "header").addText("GeneExp");
 				        data = null;
 			        cell = null;
    				}
    	 		   	if(sampleResultset.getCopyNumberSingleViewResultsContainer()!= null)	{
 	   	 		   	cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 				        data = cell.addElement("Data").addAttribute("type", "header").addText("CopyNumber");
 				        data = null;
 			        cell = null;
    	 		   	}
 			}
 			//DONE header row			
 			
    			for (Iterator sampleIterator = samples.iterator(); sampleIterator.hasNext();) {
 
    				SampleResultset sampleResultset =  (SampleResultset)sampleIterator.next();   			
 				
 				dataRow = report.addElement("Row").addAttribute("name", "dataRow");
 				
 				List rows = new ArrayList();
 				rows = ClinicalSampleReport.getClinicalRowValues(sampleResultset);
 				/*
 				rows.add(sampleResultset.getSampleIDDE());
 				rows.add(sampleResultset.getAgeGroup());
 				if(!DEUtils.checkNV(sampleResultset.getGenderCode()).equalsIgnoreCase("O"))	{
 					rows.add(sampleResultset.getGenderCode());
 				}
 				else	{	
 					rows.add(defaultV);
 				}
 				rows.add(sampleResultset.getSurvivalLengthRange());
 				rows.add(sampleResultset.getDisease());
 				rows.add(sampleResultset.getWhoGrade());
 				rows.add(sampleResultset.getRaceDE());
 				rows.add(sampleResultset.getKarnofskyClinicalEvalDE());
 				rows.add(sampleResultset.getNeuroExamDescs());
 				rows.add(sampleResultset.getMriScoreDescs());
 				rows.add(sampleResultset.getFollowupMonths());
 				rows.add(sampleResultset.getSteroidDoseStatuses());
 				rows.add(sampleResultset.getAntiConvulsantStatuses());
 				
 				rows.add(sampleResultset.getPriorRadiationRadiationSites());
 				rows.add(sampleResultset.getPriorRadiationFractionDoses());
 				rows.add(sampleResultset.getPriorRadiationFractionNumbers());
 				rows.add(sampleResultset.getPriorRadiationRadiationTypes());
 				rows.add(sampleResultset.getPriorChemoAgentNames());
 				rows.add(sampleResultset.getPriorChemoCourseCounts());
 				rows.add(sampleResultset.getPriorSurgeryProcedureTitles());
 				rows.add(sampleResultset.getPriorSurgeryTumorHistologys());
 				rows.add(sampleResultset.getPriorSurgerySurgeryOutcomes());
 				
 				rows.add(sampleResultset.getOnStudyRadiationRadiationSites());
 				rows.add(sampleResultset.getOnStudyRadiationFractionDoses());
 				rows.add(sampleResultset.getOnStudyRadiationFractionNumbers());
 				rows.add(sampleResultset.getOnStudyRadiationNeurosisStatuses());
 				rows.add(sampleResultset.getOnStudyRadiationRadiationTypes());
 				rows.add(sampleResultset.getOnStudyChemoAgentNames());
 				rows.add(sampleResultset.getOnStudyChemoCourseCounts());
 				rows.add(sampleResultset.getOnStudySurgeryProcedureTitles());
 				rows.add(sampleResultset.getOnStudySurgeryIndications());
 				rows.add(sampleResultset.getOnStudySurgeryHistoDiagnoses());
 				rows.add(sampleResultset.getOnStudySurgerySurgeryOutcomes());
 				*/
				for(int i=0; i<rows.size(); i++)	{
 					 cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
 				        data = cell.addElement("Data").addAttribute("type", "data").addText(DEUtils.checkNull(rows.get(i)));
 		        		data = null;
 		        	 cell = null;
 				}
 				
 
 	   			if(sampleResultset.getGeneExprSingleViewResultsContainer() != null)	{
 	   				//TODO: create the links
 					cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
    						data = cell.addElement("Data").addAttribute("type", "data").addText("G");
    					    data = null;
    					cell = null;
 	   			}
 	   			if(sampleResultset.getCopyNumberSingleViewResultsContainer()!= null)	{
 	   				//	TODO: create the links
 	   				cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
    						data = cell.addElement("Data").addAttribute("type", "data").addText("C");
    					    data = null;
    					cell = null;
 	   			}
     		}
 			}
 			catch(Exception e)	{
 				System.out.println(e);
 			}
 		    return document;		     
 	}
 	
 	@SuppressWarnings("unchecked")
 	public static List getClinicalRowValues(SampleResultset sampleResultset){
 		String defaultV = "-";
 		List rows = new ArrayList();
 		rows.add(sampleResultset.getSampleIDDE());
 		rows.add(sampleResultset.getAgeGroup());
 		if(!DEUtils.checkNV(sampleResultset.getGenderCode()).equalsIgnoreCase("O"))	{
 			rows.add(sampleResultset.getGenderCode());
 		}
 		else	{	
 			rows.add(defaultV);
 		}
 		rows.add(sampleResultset.getSurvivalLengthRange());
 		rows.add(sampleResultset.getDisease());
 		rows.add(sampleResultset.getWhoGrade());
 		rows.add(sampleResultset.getRaceDE());
 		rows.add(sampleResultset.getKarnofskyClinicalEvalDE());
 		rows.add(sampleResultset.getNeuroExamDescs());
 		rows.add(sampleResultset.getMriScoreDescs());
 		rows.add(sampleResultset.getFollowupMonths());
 		rows.add(sampleResultset.getSteroidDoseStatuses());
 		rows.add(sampleResultset.getAntiConvulsantStatuses());
 		
 		rows.add(sampleResultset.getPriorRadiationRadiationSites());
 		rows.add(sampleResultset.getPriorRadiationFractionDoses());
 		rows.add(sampleResultset.getPriorRadiationFractionNumbers());
 		rows.add(sampleResultset.getPriorRadiationRadiationTypes());
 		rows.add(sampleResultset.getPriorChemoAgentNames());
 		rows.add(sampleResultset.getPriorChemoCourseCounts());
 		rows.add(sampleResultset.getPriorSurgeryProcedureTitles());
 		rows.add(sampleResultset.getPriorSurgeryTumorHistologys());
 		rows.add(sampleResultset.getPriorSurgerySurgeryOutcomes());
 		
 		rows.add(sampleResultset.getOnStudyRadiationRadiationSites());
 		rows.add(sampleResultset.getOnStudyRadiationFractionDoses());
 		rows.add(sampleResultset.getOnStudyRadiationFractionNumbers());
 		rows.add(sampleResultset.getOnStudyRadiationNeurosisStatuses());
 		rows.add(sampleResultset.getOnStudyRadiationRadiationTypes());
 		rows.add(sampleResultset.getOnStudyChemoAgentNames());
 		rows.add(sampleResultset.getOnStudyChemoCourseCounts());
 		rows.add(sampleResultset.getOnStudySurgeryProcedureTitles());
 		rows.add(sampleResultset.getOnStudySurgeryIndications());
 		rows.add(sampleResultset.getOnStudySurgeryHistoDiagnoses());
 		rows.add(sampleResultset.getOnStudySurgerySurgeryOutcomes());
 		
 		return rows;
 	}
 	
 	public static List<String> getClinicalHeaderValues()	{
 		String headers = "Sample,Age at Dx (years),Gender,Survival (months),Disease,Grade,Race,Karnofsky,Neurological Exam Outcome,MRI Desc,Followup Month,Steroid Dose Status,Anti-Convulsant Status,Prior Therapy Radiation Site,Prior Therapy Radiation Fraction Dose,Prior Therapy Radiation Fraction Number,Prior Therapy Radiation Type,Prior Therapy Chemo Agent Name,Prior Therapy Chemo Course Count,Prior Therapy Surgery Procedure Title,Prior Therapy Surgery Tumor Histology,Prior Therapy Surgery Outcome,OnStudy Therapy Radiation Site,OnStudy Therapy Radiation Neurosis Status,OnStudy Therapy Radiation Fraction Dose,OnStudy Therapy Radiation Fraction Number,OnStudy Therapy Radiation Type,OnStudy Therapy Chemo Agent Name,OnStudy Therapy Chemo Course Count,OnStudy Therapy Surgery Procedure Title,OnStudy Therapy Surgery Indication,OnStudy Therapy Surgery Histo Diagnosis,OnStudy Therapy Surgery Outcome";
 		List<String> heads = new ArrayList<String>();
 		heads = Arrays.asList(StringUtils.split(headers, ","));
 		return heads;
 	}
 }
