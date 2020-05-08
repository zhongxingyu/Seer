 package gov.nih.nci.rembrandt.web.struts.form;
 
 
 
 import gov.nih.nci.caintegrator.dto.critieria.DiseaseOrGradeCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.SampleCriteria;
 import gov.nih.nci.caintegrator.dto.de.ChromosomeNumberDE;
 import gov.nih.nci.caintegrator.dto.de.CytobandDE;
 import gov.nih.nci.caintegrator.dto.de.DiseaseNameDE;
 import gov.nih.nci.caintegrator.dto.de.SampleIDDE;
 import gov.nih.nci.rembrandt.dto.lookup.DiseaseTypeLookup;
 import gov.nih.nci.rembrandt.dto.lookup.LookupManager;
 import gov.nih.nci.rembrandt.web.helper.GroupRetriever;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.TreeSet;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import org.apache.log4j.Logger;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.util.LabelValueBean;
 
 
  /**
  * This class encapsulates the properties of an caintergator
  * BaseForm object, it is a parent class for all form objects 
  * 
  * 
  * @author BhattarR,ZhangD
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
 
 public class BaseForm extends ActionForm implements Serializable{
     
     private static Logger logger = Logger.getLogger(BaseForm.class);
 		
 	// Collections used for Lookup values.  
 	private ArrayList<LabelValueBean> diseaseType;
 	private ArrayList<LabelValueBean> geneTypeColl;
 	
     private String method;
     protected DiseaseOrGradeCriteria diseaseOrGradeCriteria;
     protected SampleCriteria sampleCriteria;
     protected String[] tumorType;   
 	protected String sampleList;
 	protected transient String sampleFile;
 	protected transient HttpServletRequest thisRequest;	
     protected static Collection savedSampleList;
     protected String sampleGroup;
 
 
 
 	public BaseForm(){
 			
 		// Create Lookups for Gene Expression screens 
         setLookups(); 
        
 	}
     
     public void reset(ActionMapping mapping, HttpServletRequest request) {
         GroupRetriever groupRetriever = new GroupRetriever();
         savedSampleList = groupRetriever.getClinicalGroupsCollectionNoPath(request.getSession());
     }
     
     /**
      * @return Returns the savedSampleList.
      */
     public Collection getSavedSampleList() {
         return BaseForm.savedSampleList;
     }
 
 
     /**
      * @param savedSampleList The savedSampleList to set.
      */
     public void setSavedSampleList(Collection savedSampleList) {
         BaseForm.savedSampleList = savedSampleList;
     }
 	
 	private ArrayList addDiseaseTypes( DiseaseTypeLookup diseaseTypeLookup) {
 		String diseaseTypeStr = diseaseTypeLookup.getDiseaseType();
 		String diseaseDesc = diseaseTypeLookup.getDiseaseDesc();
 		diseaseType.add(new LabelValueBean(diseaseTypeStr,diseaseDesc ) );
 		return diseaseType;	
 		
 	}
 	private void setDiseaseTypes() {
 		try {
 			
 			DiseaseTypeLookup[] diseaseTypeLookup = LookupManager.getDiseaseType();
 			for(int i = 0; i<diseaseTypeLookup.length;i++) {
 				addDiseaseTypes(diseaseTypeLookup[i]);
 			
 			}
 		}catch(Exception e) {
 			diseaseType = null;
 			logger.error("Unable to get disease names  from the LookupManager");
 			logger.error(e);
 		}
 	}
 	public void setLookups() {
 
 		diseaseType = new ArrayList<LabelValueBean>();
 		geneTypeColl = new ArrayList<LabelValueBean>();
 		
 		setDiseaseTypes();
 		
 		//geneTypeColl.add( new LabelValueBean( "All Genes", "allgenes" ) );
 		geneTypeColl.add( new LabelValueBean( "Name/Symbol", "genesymbol" ) );
 		geneTypeColl.add( new LabelValueBean( "Locus Link Id", "genelocus" ) );
 		geneTypeColl.add( new LabelValueBean( "GenBank AccNo.", "genbankno" ) );
 
 	}
 
     //Getter methods for Gene Expression Lookup
 	public ArrayList getDiseaseType() 
 	{ return diseaseType; }
 
 	public ArrayList getGeneTypeColl() 
 	{ return geneTypeColl; }
 
 	/**
 	 * @return Returns the method.
 	 */
 	public String getMethod() {
 		return method;
 	}
 	/**
 	 * @param method The method to set.
 	 */
 	public void setMethod(String method) {
 		this.method = method;
 	}
 	
 	public Collection getChromosomeValue(){
 
 			Collection<LabelValueBean> returnColl = new ArrayList<LabelValueBean>();
 			ChromosomeNumberDE[] chromosomes;
 			try {
 				chromosomes = LookupManager.getChromosomeDEs();
 				TreeSet<Integer> chrNum = new TreeSet<Integer>();
 				TreeSet<String> chrStr = new TreeSet<String>();
 
 				if(chromosomes != null){
 					for(int i =0; i < chromosomes.length; i++){
 
 						try {
 							chrNum.add(new Integer(chromosomes[i].getValueObject()));
 						}catch(NumberFormatException ex){
 							chrStr.add(chromosomes[i].getValueObject());
 						}
 
 					}
 				}
 				returnColl.add(new LabelValueBean("",""));
 				for (Iterator iter = chrNum.iterator(); iter.hasNext();) {
 					String idxValue = ((Integer)iter.next()).toString(); 
 					returnColl.add(new LabelValueBean(idxValue, idxValue));
 				}
 				for (Iterator iter = chrStr.iterator(); iter.hasNext();) {
 					String idxValue = (String) iter.next();
 					returnColl.add(new LabelValueBean(idxValue, idxValue));
 				}
 			} catch (Exception e) {
 			    logger.error("Error reading Chromosome values from table:");
 			    logger.error(e);
 			}
 			return returnColl;
 
 		}
 
 	public HashMap getCytoBandForChr(){
 		
 			Collection chrValues = this.getChromosomeValue();
 			HashMap<String,String> cytobandCollections = new HashMap<String,String>();
 			
 			for (Iterator chrVal = chrValues.iterator(); chrVal.hasNext();) {
 				LabelValueBean lbl = (LabelValueBean) chrVal.next();
 				String thisChromosome = lbl.getValue();
 				if (thisChromosome.length() > 0){
 					try {
 						CytobandDE[] cytobands = LookupManager.getCytobandDEs(new ChromosomeNumberDE(thisChromosome));
 						String cytoString = "";
 						for (int cytoIndex = 1; cytoIndex < cytobands.length; cytoIndex++) {
 							if (cytoString.length() > 0) cytoString += ",";
 							cytoString += '"'+cytobands[cytoIndex].getValueObject()+'"';
 						}
 						cytobandCollections.put(thisChromosome,cytoString);
 					
 					}catch(Exception ex){
 					    logger.error("Error reading Cytobands from table");
 					    logger.error(ex);
 					}
 				}
 			}
 			
 			return cytobandCollections;
 	}
 	 /**
      * Returns the tumorType.
      * 
      * @return String
      */
     public String[] getTumorType() {
         return tumorType;
     }
 	/**
      * Set the tumorType.
      * 
      * @param tumorType
      *            The tumorType to set
      */  
 	 public void setTumorType(String[] tumorType) {
 		  
 	        this.tumorType = tumorType;
 	        if(tumorType!=null){ 
 	        	
 	            diseaseOrGradeCriteria = new DiseaseOrGradeCriteria();           	
 	             
	            if(tumorType.length >= 1){
	            	List<String> tumorTypes = new ArrayList<String>();
                    for(String t: tumorType){
                        tumorTypes.add(t);
                    }
	                    //if we are trying to process an ALL GLIOMA
        		        if (tumorTypes.contains("ALL GLIOMA")) {
         		            ArrayList allDiseases = this.getDiseaseType();
        		            for (Iterator diseaseIter = allDiseases.iterator(); diseaseIter.hasNext();) {
        		                LabelValueBean thisLabelBean = (LabelValueBean) diseaseIter.next();
         		                String thisDiseaseType = thisLabelBean.getValue();               
         		              
         		                if (thisDiseaseType.equalsIgnoreCase("ASTROCYTOMA")|| thisDiseaseType.equalsIgnoreCase("GBM")
         		                    || thisDiseaseType.equalsIgnoreCase("MIXED")||thisDiseaseType.equalsIgnoreCase("OLIGODENDROGLIOMA")
         		                    ||thisDiseaseType.equalsIgnoreCase("UNCLASSIFIED") ) {
         		                	DiseaseNameDE diseaseDE = new DiseaseNameDE(thisDiseaseType);
         		 	                diseaseOrGradeCriteria.setDisease(diseaseDE);
         		                }
         		            }
         		        }
         		        else {
        		        	//DiseaseNameDE diseaseDE = new DiseaseNameDE(this.tumorType[0]);
        		           // diseaseOrGradeCriteria.setDisease(diseaseDE);
        		            diseaseOrGradeCriteria.setDiseases(tumorTypes);
         		        }
         	       }
	            /*
                 else{
                     List<String> tumorTypes = new ArrayList<String>();
                     for(String t: tumorType){
                         tumorTypes.add(t);
                     }
                    //see if we are doing all, other, other,...as this doesnt sense and causes an error down the road
                    //if they select all, ignore the other selections
                     diseaseOrGradeCriteria.setDiseases(tumorTypes);
                    
                 }
                */
             }
      
 	       }
 	 public DiseaseOrGradeCriteria getDiseaseOrGradeCriteria() {
 			return this.diseaseOrGradeCriteria;
 		}
 	 
 	 /**
 		 * Returns the sampleList.
 		 * 
 		 * @return String
 		 */
 		public String getSampleList() {
 
 			return sampleList;
 		}
 
 		/**
 		 * Set the sampleList.
 		 * 
 		 * @param sampleList
 		 *            The sampleList to set
 		 */
 		public void setSampleList(String sampleList) {
 			this.sampleList = sampleList;
 			//if (thisRequest != null) {
 
 				String thisSampleGroup = this.getSampleGroup();
 				
 				if ((thisSampleGroup != null)
 						&& thisSampleGroup.equalsIgnoreCase("Specify")
 						&& (this.sampleList.length() > 0)) {
 					sampleCriteria = new SampleCriteria();
 
 					String[] splitSampleValue = this.sampleList.split("\\x2C");
 
 					for (int i = 0; i < splitSampleValue.length; i++) {
 					    SampleIDDE sampleIDDEObj = new SampleIDDE(splitSampleValue[i].trim());						                                       						
 					    sampleCriteria.setSampleID(sampleIDDEObj);	
 						
 					}
 				}
 
 			//}
 		}
 
 		/**
 		 * Returns the sampleFile.
 		 * 
 		 * @return String
 		 */
 		public String getSampleFile() {
 			return sampleFile;
 		}
 		/**
 		 * Set the sampleFile.
 		 * 
 		 * @param sampleFile
 		 *            The sampleFile to set
 		 */
 		public void setSampleFile(String sampleFile) {
             this.sampleFile = sampleFile;
             
 		}
 		public SampleCriteria getSampleCriteria() {
 			return this.sampleCriteria;
 		}
 		
 		/**
 		 * Set the sampleGroup.
 		 * 
 		 * @param sampleGroup
 		 *            The sampleGroup to set
 		 */
 		public void setSampleGroup(String sampleGroup) {
 			this.sampleGroup = sampleGroup;
 		}
 		/**
 		 * Returns the geneGroup.
 		 * 
 		 * @return String
 		 */
 		public String getSampleGroup() {
 			return sampleGroup;
 		}
 		
 
        
 }
