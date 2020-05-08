 /* Ara - capture species and specimen data
  * 
  * Copyright (C) 2009  INBio ( Instituto Naciona de Biodiversidad )
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */ 
 /*
  * SpecimenGenerationSessionBean.java
  *
  * Created on June 14, 2008, 8:01 PM
  * Copyright roaguilar
  */
 package org.inbio.ara.web.gathering;
 
 import com.sun.rave.web.ui.appbase.AbstractSessionBean;
 import com.sun.webui.jsf.model.Option;
 import java.util.Date;
 import javax.faces.FacesException;
 import org.inbio.ara.facade.specimen.SpecimenGenParms;
 import org.inbio.ara.web.ApplicationBean1;
 import org.inbio.ara.web.AraApplicationBean;
 import org.inbio.ara.web.SessionManager;
 import org.inbio.ara.web.util.MessageBean;
 import org.inbio.ara.web.util.SelectionListBean;
 
 /**
  * <p>Session scope data bean for your application.  Create properties
  *  here to represent cached data that should be made available across
  *  multiple HTTP requests for an individual user.</p>
  *
  * <p>An instance of this class will be created for you automatically,
  * the first time your application evaluates a value binding expression
  * or method binding expression that references a managed bean using
  * this class.</p>
  */
 public class SpecimenGenerationSessionBean extends AbstractSessionBean {
     // <editor-fold defaultstate="collapsed" desc="Managed Component Definition">
     private int __placeholder;
     private Option[] specimenCategoryOption;
     private Long selectedSpecimenCategory = -1L;
     private Option[] specimenTypeOption;
     private Long selectedSpecimenType = -1L;
     private Option[] originOption;
     private Long selectedOrigin = -1L;
     private Option[] preservationMediumOption;
     private Long selectedPreservationMedium = -1L;
     private Option[] storageTypeOption;
     private Long selectedStorageType = -1L;
     private Option[] substrateOption;
     private Long selectedSubstrate = -1L;
     private Option[] lifeFormOption;
     private Long[] selectedLifeForm;
     private Option[] lifeStageOption;
     private Long selectedLifeStage = -1L;
     private Option[] sexOption;
     private Long selectedSex = -1L;
     private Option[] taxonomicalRangeOption;
     private Long selectedTaxonomicalRange = -1L;
     private Option[] taxonCategoryOption;
     private Long selectedTaxonCategory = -1L;
     private Option[] taxonList;
     private Long[] selectedTaxon;
     private Option[] identificationStatusOption;
     private Long selectedIdentificationStatus = -1L;
     private Option[] identificationTypeOption;
     private Long selectedIdentificationType = -1L;
     private Option[] identificationValidatorOption;
     private Long selectedIdentificationValidator = -1L;
     private Option[] identifierOption;
     private Long[] selectedIdentifier;
     private LifeStageSimpleDataProvider lifeStageSexSimpleDataProvider = new LifeStageSimpleDataProvider();
     private SpecimenGenParms genParameters;
     private Option[] gatheringMethods;
     private Long selectedGatheringMethod = -1L;
     private Option[] extractionMethod;
     private Long selectedExtractionMethod = -1L;
     private Option[] institution;
     private Long selectedInstitution = 1L;
     private Date observationDate;
     private int hour;
     private int minute;
     public GeneratedSpecimenDataProvider generatedSpecimenDataProvider = new GeneratedSpecimenDataProvider();
     
     /**
      * <p>Automatically managed component initialization.  <strong>WARNING:</strong>
      * This method is automatically generated, so any user-specified code inserted
      * here is subject to being replaced.</p>
      */
     private void _init() throws Exception {
     }
     // </editor-fold>
     
     /**
      * <p>Construct a new session data bean instance.</p>
      */
     public SpecimenGenerationSessionBean() {
     }
     
     /**
      * <p>This method is called when this bean is initially added to
      * session scope.  Typically, this occurs as a result of evaluating
      * a value binding or method binding expression, which utilizes the
      * managed bean facility to instantiate this bean and store it into
      * session scope.</p>
      *
      * <p>You may customize this method to initialize and cache data values
      * or resources that are required for the lifetime of a particular
      * user session.</p>
      */
     public void init() {
 		// Perform initializations inherited from our superclass
         super.init();
         // Perform application initialization that must complete
         // *before* managed components are initialized
         // TODO - add your own initialiation code here
         
         // <editor-fold defaultstate="collapsed" desc="Managed Component Initialization">
         // Initialize automatically managed components
         // *Note* - this logic should NOT be modified
         try {
             _init();
         } catch (Exception e) {
             log("SpecimenGenerationSessionBean Initialization Failure", e);
             throw e instanceof FacesException ? (FacesException) e: new FacesException(e);
         }
         
         // </editor-fold>
         // Perform application initialization that must complete
         // *after* managed components are initialized
         // TODO - add your own initialization code here
 
 		this.populateOptions();
     }
     
     /**
      * <p>This method is called when the session containing it is about to be
      * passivated.  Typically, this occurs in a distributed servlet container
      * when the session is about to be transferred to a different
      * container instance, after which the <code>activate()</code> method
      * will be called to indicate that the transfer is complete.</p>
      *
      * <p>You may customize this method to release references to session data
      * or resources that can not be serialized with the session itself.</p>
      */
     public void passivate() {
     }
     
     /**
      * <p>This method is called when the session containing it was
      * reactivated.</p>
      *
      * <p>You may customize this method to reacquire references to session
      * data or resources that could not be serialized with the
      * session itself.</p>
      */
     public void activate() {
     }
     
     /**
      * <p>This method is called when this bean is removed from
      * session scope.  Typically, this occurs as a result of
      * the session timing out or being terminated by the application.</p>
      *
      * <p>You may customize this method to clean up resources allocated
      * during the execution of the <code>init()</code> method, or
      * at any later time during the lifetime of the application.</p>
      */
     public void destroy() {
     }
 
     /**
      * <p>Return a reference to the scoped data bean.</p>
      */
     protected ApplicationBean1 getApplicationBean1() {
         return (ApplicationBean1)getBean("ApplicationBean1");
     }
 
     /**
      * <p>Return a reference to the scoped data bean.</p>
      */
     protected AraApplicationBean getAraApplicationBean() {
         return (AraApplicationBean)getBean("AraApplicationBean");
     }
 
     /**
      * <p>Return a reference to the scoped data bean.</p>
      */
     protected MessageBean getutil$MessageBean() {
         return (MessageBean)getBean("util$MessageBean");
     }
 
     public Option[] getSpecimenCategoryOption() {
         return specimenCategoryOption;
     }
 
     public void setSpecimenCategoryOption(Option[] specimenCategoryOption) {
         this.specimenCategoryOption = specimenCategoryOption;
     }
 
     public Long getSelectedSpecimenCategory() {
         return selectedSpecimenCategory;
     }
 
     public void setSelectedSpecimenCategory(Long selectedSpecimenCategory) {
         this.selectedSpecimenCategory = selectedSpecimenCategory;
     }
 
     public Option[] getSpecimenTypeOption() {
         return specimenTypeOption;
     }
 
     public void setSpecimenTypeOption(Option[] specimenTypeOption) {
         this.specimenTypeOption = specimenTypeOption;
     }
 
     public Long getSelectedSpecimenType() {
         return selectedSpecimenType;
     }
 
     public void setSelectedSpecimenType(Long selectedSpecimenType) {
         this.selectedSpecimenType = selectedSpecimenType;
     }
 
     public Option[] getOriginOption() {
         return originOption;
     }
 
     public void setOriginOption(Option[] originOption) {
         this.originOption = originOption;
     }
 
     public Long getSelectedOrigin() {
         return selectedOrigin;
     }
 
     public void setSelectedOrigin(Long selectedOrigin) {
         this.selectedOrigin = selectedOrigin;
     }
 
     public Option[] getPreservationMediumOption() {
         return preservationMediumOption;
     }
 
     public void setPreservationMediumOption(Option[] preservationMediumOption) {
         this.preservationMediumOption = preservationMediumOption;
     }
 
     public Long getSelectedPreservationMedium() {
         return selectedPreservationMedium;
     }
 
     public void setSelectedPreservationMedium(Long selectedPreservationMedium) {
         this.selectedPreservationMedium = selectedPreservationMedium;
     }
 
     public Option[] getStorageTypeOption() {
         return storageTypeOption;
     }
 
     public void setStorageTypeOption(Option[] storageTypeOption) {
         this.storageTypeOption = storageTypeOption;
     }
 
     public Long getSelectedStorageType() {
         return selectedStorageType;
     }
 
     public void setSelectedStorageType(Long selectedStorageType) {
         this.selectedStorageType = selectedStorageType;
     }
 
     public Option[] getSubstrateOption() {
         return substrateOption;
     }
 
     public void setSubstrateOption(Option[] substrateOption) {
         this.substrateOption = substrateOption;
     }
 
     public Long getSelectedSubstrate() {
         return selectedSubstrate;
     }
 
     public void setSelectedSubstrate(Long selectedSubstrate) {
         this.selectedSubstrate = selectedSubstrate;
     }
 
     public Option[] getLifeFormOption() {
         return lifeFormOption;
     }
 
     public void setLifeFormOption(Option[] lifeFormOption) {
         this.lifeFormOption = lifeFormOption;
     }
 
     public Long[] getSelectedLifeForm() {
         return selectedLifeForm;
     }
 
     public void setSelectedLifeForm(Long[] selectedLifeForm) {
         this.selectedLifeForm = selectedLifeForm;
     }
 
     public Option[] getLifeStageOption() {
         return lifeStageOption;
     }
 
     public void setLifeStageOption(Option[] lifeStageOption) {
         this.lifeStageOption = lifeStageOption;
     }
 
     public Long getSelectedLifeStage() {
         return selectedLifeStage;
     }
 
     public void setSelectedLifeStage(Long selectedLifeStage) {
         this.selectedLifeStage = selectedLifeStage;
     }
 
     public Option[] getSexOption() {
         return sexOption;
     }
 
     public void setSexOption(Option[] sexOption) {
         this.sexOption = sexOption;
     }
 
     public Long getSelectedSex() {
         return selectedSex;
     }
 
     public void setSelectedSex(Long selectedSex) {
         this.selectedSex = selectedSex;
     }
 
     public Option[] getTaxonomicalRangeOption() {
         return taxonomicalRangeOption;
     }
 
     public void setTaxonomicalRangeOption(Option[] taxonomicalRangeOption) {
         this.taxonomicalRangeOption = taxonomicalRangeOption;
     }
 
     public Long getSelectedTaxonomicalRange() {
         return selectedTaxonomicalRange;
     }
 
     public void setSelectedTaxonomicalRange(Long selectedTaxonomicalRange) {
         this.selectedTaxonomicalRange = selectedTaxonomicalRange;
     }
 
     public Option[] getTaxonCategoryOption() {
         return taxonCategoryOption;
     }
 
     public void setTaxonCategoryOption(Option[] taxonCategoryOption) {
         this.taxonCategoryOption = taxonCategoryOption;
     }
 
     public Long getSelectedTaxonCategory() {
         return selectedTaxonCategory;
     }
 
     public void setSelectedTaxonCategory(Long selectedTaxonCategory) {
         this.selectedTaxonCategory = selectedTaxonCategory;
     }
 
     public Option[] getTaxonList() {
         return taxonList;
     }
 
     public void setTaxonList(Option[] taxonList) {
         this.taxonList = taxonList;
     }
 
     public Long[] getSelectedTaxon() {
         return selectedTaxon;
     }
 
     public void setSelectedTaxon(Long[] selectedTaxon) {
         this.selectedTaxon = selectedTaxon;
     }
 
     public Option[] getIdentificationStatusOption() {
         return identificationStatusOption;
     }
 
     public void setIdentificationStatusOption(Option[] identificationStatusOption) {
         this.identificationStatusOption = identificationStatusOption;
     }
 
     public Long getSelectedIdentificationStatus() {
         return selectedIdentificationStatus;
     }
 
     public void setSelectedIdentificationStatus(Long selectedIdentificationStatus) {
         this.selectedIdentificationStatus = selectedIdentificationStatus;
     }
 
     public Option[] getIdentificationTypeOption() {
         return identificationTypeOption;
     }
 
     public void setIdentificationTypeOption(Option[] identificationTypeOption) {
         this.identificationTypeOption = identificationTypeOption;
     }
 
     public Long getSelectedIdentificationType() {
         return selectedIdentificationType;
     }
 
     public void setSelectedIdentificationType(Long selectedIdentificationType) {
         this.selectedIdentificationType = selectedIdentificationType;
     }
 
     public Option[] getIdentificationValidatorOption() {
         return identificationValidatorOption;
     }
 
     public void setIdentificationValidatorOption(Option[] identificationValidatorOption) {
         this.identificationValidatorOption = identificationValidatorOption;
     }
 
     public Long getSelectedIdentificationValidator() {
         return selectedIdentificationValidator;
     }
 
     public void setSelectedIdentificationValidator(Long selectedIdentificationValidator) {
         this.selectedIdentificationValidator = selectedIdentificationValidator;
     }
 
     public Option[] getIdentifierOption() {
         return identifierOption;
     }
 
     public void setIdentifierOption(Option[] identifierOption) {
         this.identifierOption = identifierOption;
     }
 
     public Long[] getSelectedIdentifier() {
         return selectedIdentifier;
     }
 
     public void setSelectedIdentifier(Long[] selectedIdentifier) {
         this.selectedIdentifier = selectedIdentifier;
     }
     
     /**
      * <p>Return a reference to the scoped data bean.</p>
      */
     protected SelectionListBean getSelectionListBean() {
         return (SelectionListBean)getBean("util$SelectionListBean");
     }
     
     /**
      * <p>Return a reference to the scoped data bean.</p>
      */
     protected SessionManager getSessionManager() {
         return (SessionManager)getBean("SessionManager");
     }
     
     public void populateOptions() {
         // Estas listas corresponden a la generacion de especimenes
         this.setSpecimenCategoryOption(this.getSelectionListBean().getSpecimenCategory());
         this.setSpecimenTypeOption(this.getSelectionListBean().getSpecimenType());
         this.setSubstrateOption(this.getSelectionListBean().getSubstrate());
         this.setGatheringMethods(this.getSelectionListBean().getGatheringObservationMethod());
         // this.setSelectedGatheringMethod(this.getSessionManager().getDefaultGatheringMethod());
         this.setExtractionMethod(this.getSelectionListBean().getExtractionType());
         this.setInstitution(this.getSelectionListBean().getInstitution());
         
         if (this.selectedSpecimenCategory == 1L) {
             // La categoria de especimen es observacion, las siguientes listas se tornan nulas.
             this.setStorageTypeOption(new Option[]{});
             this.selectedStorageType = -1L;
             this.setOriginOption(new Option[]{});
             this.selectedOrigin = -1L;
             this.setPreservationMediumOption(new Option[]{});
             this.selectedPreservationMedium = -1L;
         } else {
             if (this.selectedSpecimenCategory != -1L) {
                 this.setStorageTypeOption(this.getSelectionListBean().getStorageType());
                 this.setOriginOption(this.getSelectionListBean().getOrigin());
                 this.setPreservationMediumOption(this.getSelectionListBean().getPreservationMedium());
             }
         }
         // Esta lista se define mediante un protocolo de coleccion
         if (this.getSessionManager().useLifeForms()) {
             this.setLifeFormOption(this.getSelectionListBean().getLifeForm());
         }
         // Estas listas corresponden a la generacion de identificaciones
         this.setLifeStageOption(this.getSelectionListBean().getLifeStage());
         this.setSexOption(this.getSelectionListBean().getSex());
         this.setTaxonomicalRangeOption(this.getSelectionListBean().getTaxonomicalRange());
         this.setTaxonCategoryOption(this.getSelectionListBean().getTaxonCategory());
         this.setIdentificationTypeOption(this.getSelectionListBean().getIdentificationType());
         this.setIdentificationValidatorOption(this.getSelectionListBean().getIdentificationValidator());
         this.setIdentifierOption(this.getSelectionListBean().getIdentifier());
         this.setTaxonList(this.getSelectionListBean().getFilteredTaxonList(this.getSelectedTaxonomicalRange(), this.getSelectedTaxonCategory()));
         this.setIdentificationStatusOption(this.getSelectionListBean().getIdentificationStatus());
     }
     
     private void populateListForObservation() {
         this.setSpecimenCategoryOption(this.getSelectionListBean().getSpecimenCategory());
         this.setSpecimenTypeOption(this.getSelectionListBean().getSpecimenType());
         this.setSubstrateOption(this.getSelectionListBean().getSubstrate());
     }
 
     public LifeStageSimpleDataProvider getLifeStageSexSimpleDataProvider() {
         return lifeStageSexSimpleDataProvider;
     }
 
     public void setLifeStageSexSimpleDataProvider(LifeStageSimpleDataProvider lifeStageSexSimpleDataProvider) {
         this.lifeStageSexSimpleDataProvider = lifeStageSexSimpleDataProvider;
     }
     
     public void setGenerationParms(SpecimenGenParms parms) {
         //genParameters = new SpecimenGenParms();
         this.genParameters = parms;       
     }
     
     public boolean generate() {
         //setGenerationParms();
         return false;
     }
 
     public Option[] getGatheringMethods() {
         return gatheringMethods;
     }
 
     public void setGatheringMethods(Option[] gatheringMethods) {
         this.gatheringMethods = gatheringMethods;
     }
 
     public Long getSelectedGatheringMethod() {
         return selectedGatheringMethod;
     }
 
     public void setSelectedGatheringMethod(Long selectedGatheringMethod) {
         this.selectedGatheringMethod = selectedGatheringMethod;
     }
 
     public Option[] getExtractionMethod() {
         return extractionMethod;
     }
 
     public void setExtractionMethod(Option[] extractionMethod) {
         this.extractionMethod = extractionMethod;
     }
 
     public Long getSelectedExtractionMethod() {
         if (selectedExtractionMethod == null) {
             return -1L;
         } else {
             return selectedExtractionMethod;
         }        
     }
 
     public void setSelectedExtractionMethod(Long selectedExtractionMethod) {
         this.selectedExtractionMethod = selectedExtractionMethod;
     }
 
     public Option[] getInstitution() {
         return institution;
     }
 
     public void setInstitution(Option[] institution) {
         this.institution = institution;
     }
 
     public Long getSelectedInstitution() {
        return (institution == null) ? -1L : selectedInstitution;
     }
 
     public void setSelectedInstitution(Long selectedInstitution) {
         this.selectedInstitution = selectedInstitution;
     }
     
     public void cleanParameters() {
         specimenCategoryOption = null;
         selectedSpecimenCategory = -1L;
         specimenTypeOption = null;
         selectedSpecimenType = -1L;
         originOption = null;
         selectedOrigin = -1L;
         preservationMediumOption = null;
         selectedPreservationMedium = -1L;
         storageTypeOption = null;
         selectedStorageType = -1L;
         substrateOption = null;
         selectedSubstrate = -1L;
         lifeFormOption = null;
         selectedLifeForm = null;
         lifeStageOption = null;
         selectedLifeStage = -1L;
         sexOption = null;
         selectedSex = -1L;
         taxonomicalRangeOption = null;
         selectedTaxonomicalRange = -1L;
         taxonCategoryOption = null;
         selectedTaxonCategory = -1L;
         taxonList = null;
         selectedTaxon = null;
         identificationStatusOption = null;
         selectedIdentificationStatus = -1L;
         identificationTypeOption = null;
         selectedIdentificationType = -1L;
         identificationValidatorOption = null;
         selectedIdentificationValidator = -1L;
         identifierOption = null;
         selectedIdentifier = null;
         lifeStageSexSimpleDataProvider = new LifeStageSimpleDataProvider();
         genParameters = null;
         gatheringMethods = null;
         selectedGatheringMethod = -1L;
         extractionMethod = null;
         selectedExtractionMethod = -1L;
         institution = null;
         selectedInstitution = -1L;
     }
 
     public Date getObservationDate() {
         return observationDate;
     }
 
     public void setObservationDate(Date observationDate) {
         this.observationDate = observationDate;
     }
 
     public int getHour() {
         return hour;
     }
 
     public void setHour(int hour) {
         this.hour = hour;
     }
 
     public int getMinute() {
         return minute;
     }
 
     public void setMinute(int minute) {
         this.minute = minute;
     }
 
     public GeneratedSpecimenDataProvider getGeneratedSpecimenDataProvider() {
         return generatedSpecimenDataProvider;
     }
 
     public void setGeneratedSpecimenDataProvider(GeneratedSpecimenDataProvider generatedSpecimenDataProvider) {
         this.generatedSpecimenDataProvider = generatedSpecimenDataProvider;
     }
 }
