 /**
  * 
  * @author pandyas
  * 
 * $Id: HistopathologyPopulateAction.java,v 1.22 2009-06-08 15:59:06 pandyas Exp $
  * 
  * $Log: not supported by cvs2svn $
  * Revision 1.21  2009/06/08 15:43:31  pandyas
  * modified for gforge #TBD
  * Disease not populating in Histopathology for models in edit mode when diagnosis is entered manually
  *
  * Revision 1.20  2009/06/08 15:30:35  pandyas
  * Testing disease issue
  *
  * Revision 1.19  2007/10/31 18:11:45  pandyas
  * Fixed #8188 	Rename UnctrlVocab items to text entries
  *
  * Revision 1.18  2007/09/12 19:36:40  pandyas
  * modified debug statements for build to stage tier
  *
  * Revision 1.17  2007/08/14 17:06:09  pandyas
  * Bug #8414:  getEVSPreferredDiscription needs to be implemented for Zebrafish vocabulary source
  *
  * Revision 1.16  2007/06/18 16:13:20  pandyas
  * EVS preferred name does not work for Zebrafish tree so changed
  * Will add this item to EVS gforge to fix, if possilbe
  *
  * Revision 1.15  2007/06/18 12:21:52  pandyas
  * Fixed update function - always has a concept code
  *
  * Revision 1.14  2007/06/13 20:20:24  pandyas
  * Copy built to dev for EVSTree project
  *
  * Revision 1.13  2007/06/13 17:50:03  pandyas
  * Removed code for EVSPreferredDescription since zebrafish tree was throwing errors
  * Must verify this is valid for this vocabulary
  *
  * Revision 1.12  2007/06/13 12:10:15  pandyas
  * Modified for save of organ/diagnosis for each tree options
  *
  * Revision 1.11  2007/04/30 20:10:17  pandyas
  * Implemented species specific vocabulary trees from EVSTree
  *
  * Revision 1.10  2006/11/09 17:27:36  pandyas
  * Commented out debug code
  *
  * Revision 1.9  2006/11/08 18:05:56  pandyas
  * Modified TumorIncidenceRate float to String (weight of tumor and volume of tumor also needed modified to delete properly)
  *
  * Revision 1.8  2006/10/17 16:11:00  pandyas
  * modified during development of caMOD 2.2 - various
  *
  * Revision 1.7  2006/09/12 15:01:10  georgeda
  * Modified setTumorClassification so it displayed the disease.getEVSPreferredDescription() results instead of disease.getName
  *
  * Revision 1.6  2006/04/17 19:09:41  pandyas
  * caMod 2.1 OM changes
  *
  * Revision 1.5  2005/11/04 14:44:25  georgeda
  * Cleaned up histopathology/assoc metastasis
  *
  * Revision 1.4  2005/11/03 22:29:12  georgeda
  * Added validation/delete capabilities to histo screens
  *
  * Revision 1.3  2005/11/03 21:48:07  georgeda
  * Cleanup
  *
  * Revision 1.2  2005/11/03 18:54:10  pandyas
  * Modified for histopathology screens
  *
  * 
  */
 
 package gov.nih.nci.camod.webapp.action;
 
 import gov.nih.nci.camod.Constants;
 import gov.nih.nci.camod.domain.Disease;
 import gov.nih.nci.camod.domain.Histopathology;
 import gov.nih.nci.camod.service.impl.HistopathologyManagerSingleton;
 import gov.nih.nci.camod.webapp.form.HistopathologyForm;
 import gov.nih.nci.camod.webapp.util.NewDropdownUtil;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.*;
 
 public class HistopathologyPopulateAction extends BaseAction {
 
     /**
      * Pre-populate all field values in the form <FormName> Used by <jspName>
      * 
      */
     public ActionForward populate(ActionMapping mapping, ActionForm form, HttpServletRequest request,
             HttpServletResponse response) throws Exception {
 
         log.info("<HistopathologyPopulateAction populate> Entered");
 
         HistopathologyForm histopathologyForm = (HistopathologyForm) form;
 
         // Grab the current aHistopathID from the session
         String aHistopatholgyID = request.getParameter("aHistopathologyID");
         log.info("aHistopatholgyID: " + aHistopatholgyID);
 
         Histopathology theHistopathology = HistopathologyManagerSingleton.instance().get(aHistopatholgyID);
 
         if (theHistopathology == null) {
             request.setAttribute(Constants.Parameters.DELETED, "true");
         } else {
             request.setAttribute("aHistopathologyID", aHistopatholgyID);
 
             // Prepopulate all dropdown fields, set the global Constants to the
             // following
             this.dropdown(request, response);
 
             /* set Organ attributes */
             log.info("<HistopathologyPopulateAction populate> get the Organ attributes");
 
             // since we are always querying from concept code (save and edit),
             // simply display EVSPreferredDescription, unless concept code is '00000'
             if (theHistopathology.getOrgan().getConceptCode().equals(Constants.Dropdowns.CONCEPTCODEZEROS)) {
 	            histopathologyForm.setOrgan(theHistopathology.getOrgan().getName());
 	            log.info("theHistopathology.getOrgan().getName(): " + theHistopathology.getOrgan().getName());	
 	            histopathologyForm.setOrganTissueCode(theHistopathology.getOrgan().getConceptCode());
 	            log.info("OrganTissueCode: " + theHistopathology.getOrgan().getConceptCode());            	
             	
             } else {
 	            histopathologyForm.setOrgan(theHistopathology.getOrgan().getEVSPreferredDescription());
 	            log.info("theHistopathology.getOrgan().getEVSPreferredDescription(): " + theHistopathology.getOrgan().getEVSPreferredDescription());	
 	            histopathologyForm.setOrganTissueCode(theHistopathology.getOrgan().getConceptCode());
             }
             
             /* Set Disease object attributes - check for other Zebrafish entry*/
             Disease disease = theHistopathology.getDisease();
             if(disease.getNameAlternEntry() != null) {
             	log.info("disease is other in DB");
             	histopathologyForm.setTumorClassification(Constants.Dropdowns.OTHER_OPTION);
             	histopathologyForm.setDiagnosisCode(disease.getConceptCode());            	
             	histopathologyForm.setOtherTumorClassification(disease.getNameAlternEntry());
             	
             } else {
             	log.info("disease getConceptCode().equals(Constants.Dropdowns.CONCEPTCODEZEROS");
             	if (disease.getConceptCode().equals(Constants.Dropdowns.CONCEPTCODEZEROS) || disease.getConceptCode().equals(Constants.Dropdowns.CONCEPTCODEZEROSWITHC)){
             		// Concept code is 00000, so just use name
 	            	histopathologyForm.setDiagnosisName(disease.getName());
 	            	histopathologyForm.setDiagnosisCode(disease.getConceptCode());
 	            	histopathologyForm.setTumorClassification(disease.getName());            		
             		
             	} else {
             		log.info("disease getConceptCode() not equal to Constants.Dropdowns.CONCEPTCODEZEROS");
             		// Concept code is not 000000, so get preferred name from EVS 
 	            	histopathologyForm.setDiagnosisName(disease.getEVSPreferredDescription());
 	            	histopathologyForm.setDiagnosisCode(disease.getConceptCode());
 	            	histopathologyForm.setTumorClassification(disease.getEVSPreferredDescription());
             	}
             }
             
             /* Set Histopathology attributes */
             histopathologyForm.setAgeOfOnset(theHistopathology.getAgeOfOnset());
             histopathologyForm.setAgeOfOnsetUnit(theHistopathology.getAgeOfOnsetUnit());
 
             histopathologyForm.setAgeOfDetection(theHistopathology.getAgeOfDetection());            
             histopathologyForm.setAgeOfDetectionUnit(theHistopathology.getAgeOfDetectionUnit());
             
             histopathologyForm.setAgeOfDetection(theHistopathology.getAgeOfDetection());
             histopathologyForm.setAgeOfDetectionUnit(theHistopathology.getAgeOfDetectionUnit());  
 
             histopathologyForm.setWeightOfTumor(theHistopathology.getWeightOfTumor());
             histopathologyForm.setVolumeOfTumor(theHistopathology.getVolumeOfTumor());
 
             histopathologyForm.setTumorIncidenceRate(theHistopathology.getTumorIncidenceRate());
 
             histopathologyForm.setSurvivalInfo(theHistopathology.getSurvivalInfo());
             histopathologyForm.setGrossDescription(theHistopathology.getGrossDescription());
             histopathologyForm.setMicroscopicDescription(theHistopathology.getMicroscopicDescription());
 
             histopathologyForm.setComparativeData(theHistopathology.getComparativeData());
             histopathologyForm.setComments(theHistopathology.getComments());
 
             /* Set GeneticAlteration attributes */
             if (theHistopathology.getGeneticAlteration() != null) {
                 histopathologyForm.setObservation(theHistopathology.getGeneticAlteration().getObservation());
                 histopathologyForm.setMethodOfObservation(theHistopathology.getGeneticAlteration()
                         .getMethodOfObservation());
             }
         }
         // Prepopulate all dropdown fields, set the global Constants to the
         // following
         this.dropdown(request, response);
 
         return mapping.findForward("submitHistopathology");
     }
 
     /**
      * Populate the dropdown menus
      * 
      * @param mapping
      * @param form
      * @param request
      * @param response
      * @return
      * @throws Exception
      */
     public ActionForward dropdown(ActionMapping mapping, ActionForm form, HttpServletRequest request,
             HttpServletResponse response) throws Exception {
 
         log.info("<HistopathologyPopulateAction dropdown> Entering ActionForward dropdown()");
 
         // setup dropdown menus
         this.dropdown(request, response);
 
         return mapping.findForward("submitHistopathology");
     }
 
     /**
      * Populate all drowpdowns for this type of form
      * 
      * @param request
      * @param response
      * @throws Exception
      */
     public void dropdown(HttpServletRequest request, HttpServletResponse response) throws Exception {
 
         log.info("<HistopathologyPopulateAction dropdown> Entering void dropdown()");
 
         // Prepopulate all dropdown fields, set the global Constants to the
         // following
         NewDropdownUtil.populateDropdown(request, Constants.Dropdowns.AGEUNITSDROP, "");
         NewDropdownUtil.populateDropdown(request, Constants.Dropdowns.ZEBRAFISHDIAGNOSISDROP, Constants.Dropdowns.ADD_BLANK_AND_OTHER_OPTION);
 
     }
 }
