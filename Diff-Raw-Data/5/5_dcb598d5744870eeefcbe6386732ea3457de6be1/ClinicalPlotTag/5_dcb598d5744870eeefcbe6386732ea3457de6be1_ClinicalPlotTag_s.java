 package gov.nih.nci.rembrandt.web.taglib;
 
 import gov.nih.nci.caintegrator.application.cache.BusinessTierCache;
 import gov.nih.nci.caintegrator.dto.de.KarnofskyClinicalEvalDE;
 import gov.nih.nci.caintegrator.enumeration.ClinicalFactorType;
 import gov.nih.nci.caintegrator.enumeration.DiseaseType;
 import gov.nih.nci.caintegrator.ui.graphing.chart.CaIntegratorChartFactory;
 import gov.nih.nci.caintegrator.ui.graphing.data.clinical.ClinicalDataPoint;
 import gov.nih.nci.caintegrator.ui.graphing.util.ImageMapUtil;
 import gov.nih.nci.rembrandt.cache.RembrandtPresentationTierCache;
 import gov.nih.nci.rembrandt.queryservice.resultset.DimensionalViewContainer;
 import gov.nih.nci.rembrandt.queryservice.resultset.Resultant;
 import gov.nih.nci.rembrandt.queryservice.resultset.ResultsContainer;
 import gov.nih.nci.rembrandt.queryservice.resultset.sample.SampleResultset;
 import gov.nih.nci.rembrandt.queryservice.resultset.sample.SampleViewResultsContainer;
 import gov.nih.nci.rembrandt.web.bean.ReportBean;
 import gov.nih.nci.rembrandt.web.factory.ApplicationFactory;
 import gov.nih.nci.rembrandt.web.helper.RembrandtImageFileHandler;
 
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpSession;
 import javax.servlet.jsp.JspException;
 import javax.servlet.jsp.JspWriter;
 
 import org.apache.log4j.Logger;
 import org.jfree.chart.ChartRenderingInfo;
 import org.jfree.chart.ChartUtilities;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.entity.StandardEntityCollection;
 
 /**
  * this class generates a Clinical Plot tag which will take a taskId, the components
  * with which to compare, and possibly a colorBy attribute which colors the
  * samples either by Disease or Gender. Disease is colored by default.
  * @author rossok
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
 
 public class ClinicalPlotTag extends AbstractGraphingTag {
 
 	private String beanName = "";
 	private String taskId = "";
     private String colorBy = "";
     private String components ="";
     private Collection<ClinicalDataPoint> clinicalData = new ArrayList();
 	private List<JFreeChart> jFreeChartsList;
     private JFreeChart chart = null;
     private static Logger logger = Logger.getLogger(ClinicalPlotTag.class);
 	private RembrandtPresentationTierCache presentationTierCache = ApplicationFactory.getPresentationTierCache();
 	private BusinessTierCache businessTierCache = ApplicationFactory.getBusinessTierCache();
     
 	public int doStartTag() {
 		chart = null;
 		clinicalData.clear();
 
 		
 		ServletRequest request = pageContext.getRequest();
 		HttpSession session = pageContext.getSession();
 		Object o = request.getAttribute(beanName);
 		JspWriter out = pageContext.getOut();
 		ServletResponse response = pageContext.getResponse();
 		
 		try {
 			
 			//
             //retrieve the Finding from cache and build the list of  Clinical Data points
             //ClinicalFinding clinicalFinding = (ClinicalFinding)businessTierCache.getSessionFinding(session.getId(),taskId);
             ReportBean clincalReportBean = presentationTierCache.getReportBean(session.getId(),taskId);
             Resultant clinicalResultant = clincalReportBean.getResultant();
             ResultsContainer  resultsContainer = clinicalResultant.getResultsContainer();
             SampleViewResultsContainer sampleViewContainer = null;
             if(resultsContainer instanceof DimensionalViewContainer){
                 DimensionalViewContainer dimensionalViewContainer = (DimensionalViewContainer) resultsContainer;
                 sampleViewContainer = dimensionalViewContainer.getSampleViewResultsContainer();
             }
             if(sampleViewContainer!=null){
                 Collection<ClinicalFactorType> clinicalFactors = new ArrayList<ClinicalFactorType>();
                     clinicalFactors.add(ClinicalFactorType.AgeAtDx);
                     //clinicalFactors.add(ClinicalFactorType.Survival);
                     Collection<SampleResultset> samples = sampleViewContainer.getSampleResultsets();
                 
                 if(samples!=null){
                 int numDxvsKa=0;
                 int numDxvsSl = 0;
                     for (SampleResultset rs:samples){
                         //String id = rs.getBiospecimen().getValueObject();
                     	String id = rs.getSampleIDDE().getValueObject();
                         ClinicalDataPoint clinicalDataPoint = new ClinicalDataPoint(id);
                                
                         String diseaseName = rs.getDisease().getValueObject();
                         if(diseaseName!=null){
                             clinicalDataPoint.setDiseaseName(diseaseName);
                         }
                         else{
                         	clinicalDataPoint.setDiseaseName(DiseaseType.NON_TUMOR.name());
                         }
                         
                         
                         Long sl = rs.getSurvivalLength();
                         double survivalDays = -1.0;
                         double survivalMonths = -1.0;
                         if (sl != null) {
                           survivalDays = sl.doubleValue();
                           survivalMonths = survivalDays/30.0;
                           //if ((survivalMonths > 0.0)&&(survivalMonths < 1000.0)) {
                             clinicalDataPoint.setSurvival(survivalDays);
                           //}
                         }
                         
                         Long dxAge = rs.getAge();
                         if (dxAge != null) {
                           clinicalDataPoint.setAgeAtDx(dxAge.doubleValue());
                         }
                         
                         KarnofskyClinicalEvalDE ka = rs.getKarnofskyClinicalEvalDE();
                         if (ka != null) {
                           String kaStr = ka.getValueObject();
                           if (kaStr != null) {
                        	if(kaStr.contains(",")) { 
                        		String [] kaStrArray = kaStr.split(",");
 	                        	for(int i =0;i<kaStrArray.length;i++){
 	                        		if (i==0) {
 	                        		  //first score is baseline just use this for now
 	                        		 //later we will need to use all score in a series for each patient
 	                        		  double kaVal = Double.parseDouble(kaStrArray[i].trim());
 	                        		  clinicalDataPoint.setKarnofskyScore(kaVal);
 	                        		}
 	                        	}	                            
                         	}
                         	else{
                         		double kaVal = Double.parseDouble(kaStr);
                         		clinicalDataPoint.setKarnofskyScore(kaVal);
                         	}
                         	
                           }
                         }
                         
                         if ((dxAge!= null)&&(ka!=null)) {
                           numDxvsKa++;
                         }
                         
                         if ((dxAge!=null) && (sl!=null)) {
                           numDxvsSl++;
                         }
                         
                         
 //                        Object dx = rs.getAgeGroup();
 //                            if(sl !=null && dx !=null){
 //                                clinicalDataPoint.setSurvival(new Double(sl.toString()));
 //                                clinicalDataPoint.setAgeAtDx(new Double(dx.toString()));
 //                            }
 //                        Object ks = rs.getKarnofskyClinicalEvalDE();
 //                        Object dx = rs.getAgeGroup();
 //                            if(ks !=null && dx !=null){
 //                                clinicalDataPoint.setNeurologicalAssessment(new Double(ks.toString()));
 //                                clinicalDataPoint.setAgeAtDx(new Double(dx.toString()));
 //                            }
                             
                        clinicalData.add(clinicalDataPoint);
                     }
                 }
             }
             
             System.out.println("Done creating points!");
            
 			//-------------------------------------------------------------
 			//GET THE CLINICAL DATA AND POPULATE THE clinicalData list
 			//Note the ClinicalFinding is currently an empty class
 			//----------------------------------------------------------
 			
 			
             //check the components to see which graph to get
 			if(components.equalsIgnoreCase("SurvivalvsAgeAtDx")){
                 chart = (JFreeChart) CaIntegratorChartFactory.getClinicalGraph(clinicalData,ClinicalFactorType.SurvivalLength, "Survival Length (Months)",ClinicalFactorType.AgeAtDx, "Age At Diagnosis (Years)");
             }
             if(components.equalsIgnoreCase("KarnofskyScorevsAgeAtDx")){
                 chart = (JFreeChart) CaIntegratorChartFactory.getClinicalGraph(clinicalData,ClinicalFactorType.KarnofskyAssessment, "Karnofsky Score", ClinicalFactorType.AgeAtDx, "Age At Diagnosis (Years)");
             }
           
             
             RembrandtImageFileHandler imageHandler = new RembrandtImageFileHandler(session.getId(),"png",600,500);
 			//The final complete path to be used by the webapplication
 			String finalPath = imageHandler.getSessionTempFolder();
             String finalURLpath = imageHandler.getFinalURLPath();
 			/*
 			 * Create the actual charts, writing it to the session temp folder
 			*/ 
             ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());
             String mapName = imageHandler.createUniqueMapName();
            
 			ChartUtilities.writeChartAsPNG(new FileOutputStream(finalPath),chart, 600,500,info);
            
 			
 			/*	This is here to put the thread into a loop while it waits for the
 			 *	image to be available.  It has an unsophisticated timer but at 
 			 *	least it is something to avoid an endless loop.
 			 **/ 
             boolean imageReady = false;
             int timeout = 1000;
             FileInputStream inputStream = null;
             while(!imageReady) {
                 timeout--;
                 try {
                     inputStream = new FileInputStream(finalPath);
                     inputStream.available();
                     imageReady = true;
                     inputStream.close();
                 }catch(IOException ioe) {
                     imageReady = false;  
                     if(inputStream != null){
                     	inputStream.close();
                     }
                 }
                 if(timeout <= 1) {
                     
                     break;
                 }
              }
             
             out.print(ImageMapUtil.getBoundingRectImageMapTag(mapName,false,info));
             //finalURLpath = finalURLpath.replace("\\", "/");
             finalURLpath = finalURLpath.replace("\\", "/");
             long randomness = System.currentTimeMillis(); //prevent image caching
 		    out.print("<img id=\"geneChart\" name=\"geneChart\" src=\""+finalURLpath+"?"+randomness+"\" usemap=\"#"+mapName + "\" border=\"0\" />");
           
 		    //out.print("<img id=\"geneChart\" name=\"geneChart\" src=\""+finalURLpath+"\" usemap=\"#"+mapName + "\" border=\"0\" />");
             
         
 		}catch (IOException e) {
 			logger.error(e);
 		}catch(Exception e) {
 			logger.error(e);
 		}catch(Throwable t) {
 			logger.error(t);
 		}
 	
 		return EVAL_BODY_INCLUDE;
 	}
 	
 
 
 	public int doEndTag() throws JspException {
 		return doAfterEndTag(EVAL_PAGE);
 	}
 	public void reset() {
 		//chartDefinition = createChartDefinition();
 	}
 	/**
 	 * @return Returns the bean.
 	 */
 	public String getBean() {
 		return beanName;
 	}
 	/**
 	 * @param bean The bean to set.
 	 */
 	public void setBean(String bean) {
 		this.beanName = bean;
 	}
 
     /**
      * @return Returns the taskId.
      */
     public String getTaskId() {
         return taskId;
     }
 
     /**
      * @param taskId The taskId to set.
      */
     public void setTaskId(String taskId) {
         this.taskId = taskId;
     }
 
     /**
      * @return Returns the colorBy.
      */
     public String getColorBy() {
         return colorBy;
     }
 
     /**
      * @param colorBy The colorBy to set.
      */
     public void setColorBy(String colorBy) {
         this.colorBy = colorBy;
     }
 
     /**
      * @return Returns the components.
      */
     public String getComponents() {
         return components;
     }
 
     /**
      * @param components The components to set.
      */
     public void setComponents(String components) {
         this.components = components;
     }
 	
 }
