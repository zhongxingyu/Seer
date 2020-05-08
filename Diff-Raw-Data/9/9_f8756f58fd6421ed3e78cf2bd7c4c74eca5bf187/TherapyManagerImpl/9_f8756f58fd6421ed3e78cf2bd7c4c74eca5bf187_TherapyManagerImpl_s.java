 /**
  * @author dgeorge
  * 
 * $Id: TherapyManagerImpl.java,v 1.7 2005-10-06 19:30:22 pandyas Exp $
  * 
  * $Log: not supported by cvs2svn $
  * Revision 1.6  2005/09/28 21:20:01  georgeda
  * Finished up converting to new manager
  *
  */
 package gov.nih.nci.camod.service.impl;
 
 import gov.nih.nci.camod.domain.*;
 import gov.nih.nci.camod.service.TherapyManager;
 import gov.nih.nci.camod.webapp.form.*;
 import gov.nih.nci.camod.webapp.form.cibase.*;
 
 /**
  * Implementation of the TherapyManager interface. Creates/saves/updates the
  * different types of therapies based on the specific interface passed in
  */
 public class TherapyManagerImpl extends BaseManager implements TherapyManager {
 
     /**
      * Create a therapy object with the correct data filled in.
      * 
      * @param inSurgeryData
      *            the interface to create the therapy object from
      * 
      * @returns a therapy
      */
     public Therapy create(SurgeryData inSurgeryData) {
 
         log.trace("In TherapyManagerImpl.create");
 
         Therapy theTherapy = new Therapy();
         populateName(inSurgeryData, theTherapy, "Other");
         populateTreatment(inSurgeryData, theTherapy);
         populateAgeGender(inSurgeryData, theTherapy);
 
         return theTherapy;
     }
 
     /**
      * Update a therapy object with the correct data filled in.
      * 
      * @param inSurgeryData
      *            the interface to update the therapy object from
      * 
      * @param inTherapy
      *            the therapy object to update
      * 
      * @exception Exception
      *                when anything goes wrong.
      */
     public void update(SurgeryData inSurgeryData, Therapy inTherapy) throws Exception {
 
         log.trace("In TherapyManagerImpl.update");
 
         populateName(inSurgeryData, inTherapy, "Other");
         populateTreatment(inSurgeryData, inTherapy);
         populateAgeGender(inSurgeryData, inTherapy);
 
         save(inTherapy);
     }
 
     /**
      * Create a therapy object with the correct data filled in.
      * 
      * @param inNutritionalFactorData
      *            the interface to create the therapy object from
      * 
      * @returns a therapy
      */
     public Therapy create(NutritionalFactorData inNutritionalFactorData) {
 
         log.trace("In TherapyManagerImpl.create");
 
         Therapy theTherapy = new Therapy();
         populateName(inNutritionalFactorData, theTherapy, "Nutrition");
         populateTreatment(inNutritionalFactorData, theTherapy);
         populateAgeGender(inNutritionalFactorData, theTherapy);
         populateDose(inNutritionalFactorData, theTherapy);
 
         return theTherapy;
     }
 
     /**
      * Update a therapy object with the correct data filled in.
      * 
      * @param inNutritionalFactorData
      *            the interface to update the therapy object from
      * 
      * @param inTherapy
      *            the therapy object to update
      * 
      * @exception Exception
      *                when anything goes wrong.
      */
     public void update(NutritionalFactorData inNutritionalFactorData, Therapy inTherapy) throws Exception {
 
         log.trace("In TherapyManagerImpl.update");
 
         populateName(inNutritionalFactorData, inTherapy, "Nutrition");
         populateTreatment(inNutritionalFactorData, inTherapy);
         populateAgeGender(inNutritionalFactorData, inTherapy);
         populateDose(inNutritionalFactorData, inTherapy);
 
         save(inTherapy);
     }
 
     /**
      * Create a therapy object with the correct data filled in.
      * 
      * @param inHormoneData
      *            the interface to create the therapy object from
      * 
      * @returns a therapy
      */
     public Therapy create(HormoneData inHormoneData) {
 
         log.trace("In TherapyManagerImpl.create");
 
         Therapy theTherapy = new Therapy();
         populateName(inHormoneData, theTherapy, "Hormone");
         populateTreatment(inHormoneData, theTherapy);
         populateDose(inHormoneData, theTherapy);
 
         return theTherapy;
     }
 
     /**
      * Update a therapy object with the correct data filled in.
      * 
      * @param inHormoneData
      *            the interface to update the therapy object from
      * 
      * @param inTherapy
      *            the therapy object to update
      * 
      * @exception Exception
      *                when anything goes wrong.
      */
     public void update(HormoneData inHormoneData, Therapy inTherapy) throws Exception {
 
         log.trace("In TherapyManagerImpl.update");
 
         populateName(inHormoneData, inTherapy, "Hormone");
         populateTreatment(inHormoneData, inTherapy);
         populateDose(inHormoneData, inTherapy);
 
         save(inTherapy);
     }
 
     /**
      * Create a therapy object with the correct data filled in.
      * 
      * @param inGrowthFactorData
      *            the interface to create the therapy object from
      * 
      * @returns a therapy
      */
     public Therapy create(GrowthFactorData inGrowthFactorData) {
 
         log.trace("In TherapyManagerImpl.create");
 
         Therapy theTherapy = new Therapy();
         populateName(inGrowthFactorData, theTherapy, "Growth Factor");
         populateAgeGender(inGrowthFactorData, theTherapy);
         populateTreatment(inGrowthFactorData, theTherapy);
         populateDose(inGrowthFactorData, theTherapy);
 
         return theTherapy;
     }
 
     /**
      * Update a therapy object with the correct data filled in.
      * 
      * @param inGrowthFactorData
      *            the interface to update the therapy object from
      * 
      * @param inTherapy
      *            the therapy object to update
      * 
      * @exception Exception
      *                when anything goes wrong.
      */
     public void update(GrowthFactorData inGrowthFactorData, Therapy inTherapy) throws Exception {
 
         log.trace("In TherapyManagerImpl.update");
 
         populateName(inGrowthFactorData, inTherapy, "Growth Factor");
         populateAgeGender(inGrowthFactorData, inTherapy);
         populateTreatment(inGrowthFactorData, inTherapy);
         populateDose(inGrowthFactorData, inTherapy);
 
         save(inTherapy);
     }
 
     /**
      * Create a therapy object with the correct data filled in.
      * 
      * @param inViralTreatmentData
      *            the interface to create the therapy object from
      * 
      * @returns a therapy
      */
     public Therapy create(ViralTreatmentData inViralTreatmentData) {
 
         log.trace("In TherapyManagerImpl.create");
 
         Therapy theTherapy = new Therapy();
         populateName(inViralTreatmentData, theTherapy, "Viral");
         populateAgeGender(inViralTreatmentData, theTherapy);
         populateTreatment(inViralTreatmentData, theTherapy);
         populateDose(inViralTreatmentData, theTherapy);
         populateAdministration(inViralTreatmentData, theTherapy);
 
         return theTherapy;
     }
 
     /**
      * Update a therapy object with the correct data filled in.
      * 
      * @param inViralTreatmentData
      *            the interface to update the therapy object from
      * 
      * @param inTherapy
      *            the therapy object to update
      * 
      * @exception Exception
      *                when anything goes wrong.
      */
     public void update(ViralTreatmentData inViralTreatmentData, Therapy inTherapy) throws Exception {
 
         log.trace("In TherapyManagerImpl.update");
 
         populateName(inViralTreatmentData, inTherapy, "Viral");
         populateAgeGender(inViralTreatmentData, inTherapy);
         populateTreatment(inViralTreatmentData, inTherapy);
         populateDose(inViralTreatmentData, inTherapy);
         populateAdministration(inViralTreatmentData, inTherapy);
 
         save(inTherapy);
     }
 
     /**
      * Create a therapy object with the correct data filled in.
      * 
      * @param inRadiationData
      *            the interface to create the therapy object from
      * 
      * @returns a therapy
      */
     public Therapy create(RadiationData inRadiationData) {
 
         log.trace("In TherapyManagerImpl.create");
 
         Therapy theTherapy = new Therapy();
 
         populateName(inRadiationData, theTherapy, "Radiation");
         populateAgeGender(inRadiationData, theTherapy);
         populateTreatment(inRadiationData, theTherapy);
         populateDose(inRadiationData, theTherapy);
         populateAdministration(inRadiationData, theTherapy);
 
         return theTherapy;
     }
 
     /**
      * Update a therapy object with the correct data filled in.
      * 
      * @param inRadiationData
      *            the interface to update the therapy object from
      * 
      * @param inTherapy
      *            the therapy object to update
      * 
      * @exception Exception
      *                when anything goes wrong.
      */
     public void update(RadiationData inRadiationData, Therapy inTherapy) throws Exception {
 
         log.trace("In TherapyManagerImpl.update");
 
         populateName(inRadiationData, inTherapy, "Radiation");
         populateAgeGender(inRadiationData, inTherapy);
         populateTreatment(inRadiationData, inTherapy);
         populateDose(inRadiationData, inTherapy);
         populateAdministration(inRadiationData, inTherapy);
 
         save(inTherapy);
     }
 
     /**
      * Create a therapy object with the correct data filled in.
      * 
      * @param inEnvironmentalFactorData
      *            the interface to create the therapy object from
      * 
      * @returns a therapy
      */
     public Therapy create(EnvironmentalFactorData inEnvironmentalFactorData) {
 
         log.trace("In TherapyManagerImpl.create");
 
         Therapy theTherapy = new Therapy();
 
         populateName(inEnvironmentalFactorData, theTherapy, "Environment");
         populateAgeGender(inEnvironmentalFactorData, theTherapy);
         populateTreatment(inEnvironmentalFactorData, theTherapy);
         populateDose(inEnvironmentalFactorData, theTherapy);
         populateAdministration(inEnvironmentalFactorData, theTherapy);
 
         return theTherapy;
     }
 
     /**
      * Update a therapy object with the correct data filled in.
      * 
      * @param inEnvironmentalFactorData
      *            the interface to update the therapy object from
      * 
      * @param inTherapy
      *            the therapy object to update
      * 
      * @exception Exception
      *                when anything goes wrong.
      */
     public void update(EnvironmentalFactorData inEnvironmentalFactorData, Therapy inTherapy) throws Exception {
 
         log.trace("In TherapyManagerImpl.update");
 
         populateName(inEnvironmentalFactorData, inTherapy, "Environment");
         populateAgeGender(inEnvironmentalFactorData, inTherapy);
         populateTreatment(inEnvironmentalFactorData, inTherapy);
         populateDose(inEnvironmentalFactorData, inTherapy);
         populateAdministration(inEnvironmentalFactorData, inTherapy);
 
         save(inTherapy);
     }
 
     /**
      * Create a therapy object with the correct data filled in.
      * 
      * @param inChemicalDrugData
      *            the interface to create the therapy object from
      * 
      * @returns a therapy
      */
     public Therapy create(ChemicalDrugData inChemicalDrugData) {
 
         log.trace("In TherapyManagerImpl.create");
 
         Therapy theTherapy = new Therapy();
 
         populateName(inChemicalDrugData, theTherapy, "Chemical / Drug");
         populateAgeGender(inChemicalDrugData, theTherapy);
         populateTreatment(inChemicalDrugData, theTherapy);
         populateDose(inChemicalDrugData, theTherapy);
         populateAdministration(inChemicalDrugData, theTherapy);
 
         populateChemicalDrug(inChemicalDrugData, theTherapy);
 
         return theTherapy;
     }
 
     /**
      * Update a therapy object with the correct data filled in.
      * 
      * @param inChemicalDrugData
      *            the interface to update the therapy object from
      * 
      * @param inTherapy
      *            the therapy object to update
      * 
      * @exception Exception
      *                when anything goes wrong.
      */
     public void update(ChemicalDrugData inChemicalDrugData, Therapy inTherapy) throws Exception {
 
         log.trace("In TherapyManagerImpl.update");
 
         populateName(inChemicalDrugData, inTherapy, "Chemical / Drug");
         populateAgeGender(inChemicalDrugData, inTherapy);
         populateTreatment(inChemicalDrugData, inTherapy);
         populateDose(inChemicalDrugData, inTherapy);
         populateAdministration(inChemicalDrugData, inTherapy);
 
         populateChemicalDrug(inChemicalDrugData, inTherapy);
         save(inTherapy);
     }
 
     /**
      * Get a specific Therapy by id
      * 
      * @param id
      *            the unique id for a therapy
      * 
      * @return the matching Therapy object, or null if not found.
      * 
      * @exception Exception
      *                when anything goes wrong.
      */
     public Therapy get(String id) throws Exception {
         log.trace("In TherapyManagerImpl.get");
         return (Therapy) super.get(id, Therapy.class);
     }
 
     /**
      * Save Therapy
      * 
      * @param therapy
      *            the therapy to save
      * 
      * @exception Exception
      *                when anything goes wrong.
      */
     public void save(Therapy therapy) throws Exception {
         log.trace("In TherapyManagerImpl.save");
         super.save(therapy);
     }
 
     /**
      * Remove a specific Therapy by id
      * 
      * @param id
      *            the unique id for a therapy
      * 
      * @exception Exception
      *                when anything goes wrong.
      */
     public void remove(String id) throws Exception {
         log.trace("In TherapyManagerImpl.save");
         super.remove(id, Therapy.class);
     }
 
     /////////////////////////////////////////////////////////
     // Populate methods for the specific interfaces that
     // each interface implements
     /////////////////////////////////////////////////////////
     private void populateChemicalDrug(ChemicalDrugData inChemicalDrug, Therapy theTherapy) {
 
         // Agent IS-A an EnvironmentalFactor
         Agent theAgent = theTherapy.getAgent();
 
         String theNSCNumber = inChemicalDrug.getNSCNumber().trim();
         if (theNSCNumber != null && theNSCNumber.length() > 0) {
             try {
                 theAgent.setNscNumber(Long.valueOf(theNSCNumber));
             } catch (NumberFormatException e) {
                 log.error("Bad NSC number: " + theNSCNumber);
             }
         }
         String theCasNumber = inChemicalDrug.getCASNumber().trim();
         if (theCasNumber != null && theCasNumber.length() > 0) {
             theAgent.setCasNumber(theCasNumber);
         }
     }
 
     private void populateAgeGender(AgeGenderData inAgeGender, Therapy theTherapy) {
 
         // Set the treatment
         Treatment theTreatment = theTherapy.getTreatment();
         if (theTreatment == null) {
             theTreatment = new Treatment();
             theTherapy.setTreatment(theTreatment);
         }
 
         // Set the gender
         SexDistribution sexDistribution = SexDistributionManagerSingleton.instance().getByType(inAgeGender.getType());
 
         // save the treatment
         theTreatment.setSexDistribution(sexDistribution);
 
         // Append the ageunit onto the age at treatment variable
         theTreatment.setAgeAtTreatment(inAgeGender.getAgeAtTreatment() + " " + inAgeGender.getAgeUnit());
     }
 
     private void populateTreatment(TreatmentData inTreatment, Therapy theTherapy) {
 
         // Set the treatment
         Treatment theTreatment = theTherapy.getTreatment();
         if (theTreatment == null) {
             theTreatment = new Treatment();
             theTherapy.setTreatment(theTreatment);
         }
 
         // save the treatment
         theTreatment.setRegimen(inTreatment.getRegimen());
     }
 
     private void populateAdministration(AdministrationData inAdministrationData, Therapy theTherapy) {
 
         // Set the treatment
         Treatment theTreatment = theTherapy.getTreatment();
         if (theTreatment == null) {
             theTreatment = new Treatment();
             theTherapy.setTreatment(theTreatment);
         }
 
         theTreatment.setAdministrativeRoute(inAdministrationData.getAdministrativeRoute());
 
         // Agent IS-A an EnvironmentalFactor
         Agent theAgent = theTherapy.getAgent();
         if (theAgent == null) {
             theAgent = new Agent();
             theTherapy.setAgent(theAgent);
         }
     }
 
     private void populateDose(DoseData inDoseData, Therapy theTherapy) {
 
         // Set the treatment
         Treatment theTreatment = theTherapy.getTreatment();
         if (theTreatment == null) {
             theTreatment = new Treatment();
             theTherapy.setTreatment(theTreatment);
         }
 
         theTreatment.setDosage(inDoseData.getDosage() + " " + inDoseData.getDoseUnit());
     }
 
     private void populateName(NameData inNameData, Therapy theTherapy, String inType) {
 
         // Set the treatment
         Treatment theTreatment = theTherapy.getTreatment();
         if (theTreatment == null) {
             theTreatment = new Treatment();
             theTherapy.setTreatment(theTreatment);
         }
 
         // Agent IS-A an EnvironmentalFactor
         Agent theAgent = theTherapy.getAgent();
         if (theAgent == null) {
             theAgent = new Agent();
             theTherapy.setAgent(theAgent);
         }
         theAgent.setType(inType);
         theAgent.setName(inNameData.getName());
         theTherapy.setTherapeuticExperiment(new Boolean(false));
     }
     
  
     /**
      * Create a therapy object with the correct data filled in.
      * 
      * @param inTherapyData
      *            the interface to create the therapy object from
      * 
      * @returns a therapy
      */
     public Therapy create(TherapyData inTherapyData) throws Exception {
 
         log.trace("In TherapyManagerImpl.create");
 
         Therapy theTherapy = new Therapy();
 
         populateAgeGender(inTherapyData, theTherapy);
         populateDose(inTherapyData, theTherapy);
         populateTherapy(inTherapyData, theTherapy);
 
         return theTherapy;
     }    
     
     public void update(TherapyData inTherapyData, Therapy inTherapy) 
     	throws Exception {
 
         log.trace("In TherapyManagerImpl.update");
 
         populateAgeGender(inTherapyData, inTherapy);
         populateDose(inTherapyData, inTherapy);
         populateTherapy(inTherapyData, inTherapy);
         save(inTherapy);
 
     }
     
     private void populateTherapy(TherapyData inTherapyData, Therapy theTherapy)
     throws Exception {
     	
     	log.trace("Entering populateTherapy");
     	
     	/* populateName method without otherName */
     	
         // Set the treatment
         Treatment theTreatment = theTherapy.getTreatment();
         if (theTreatment == null) {
             theTreatment = new Treatment();
             theTherapy.setTreatment(theTreatment);
         }
 
         // Agent IS-A an EnvironmentalFactor
         Agent theAgent = theTherapy.getAgent();
         if (theAgent == null) {
             theAgent = new Agent();
             theTherapy.setAgent(theAgent);
         }
         //theAgent.setType(inType);
         theAgent.setName(inTherapyData.getName());
         theTherapy.setTherapeuticExperiment(new Boolean(true)); 
         
         // Set the administrative route
         theTreatment.setAdministrativeRoute(inTherapyData.getAdministrativeRoute());
 
         //Set NSC and CAS
         String theNSCNumber = inTherapyData.getNSCNumber().trim();
         if (theNSCNumber != null && theNSCNumber.length() > 0) {
             try {
                 theAgent.setNscNumber(Long.valueOf(theNSCNumber));
             } catch (NumberFormatException e) {
                 log.error("Bad NSC number: " + theNSCNumber);
             }
         }        
         String theCasNumber = inTherapyData.getCASNumber().trim();
         if (theCasNumber != null && theCasNumber.length() > 0) {
             theAgent.setCasNumber(theCasNumber);
         }    	
         
     	//Therapy object attributes
         theTherapy.setToxicityGrade(inTherapyData.getToxicityGrade());		
         theTherapy.setBiomarker(inTherapyData.getBiomarker());
        theTherapy.setTumorResponse(inTherapyData.getTumorResponse());
         theTherapy.setExperiment(inTherapyData.getExperiment());
         theTherapy.setResults(inTherapyData.getResults());		
         theTherapy.setComments(inTherapyData.getComments());    	
     	System.out.println("Got Therapy attributes in populateTherapy");
     	
         //Get/create the ChemicalClass
         //TODO Loop through all the selections
         //List chemicalClassList = new Array();
         ChemicalClass theChemicalClass = ChemicalClassManagerSingleton.instance().getByName(
         		inTherapyData.getChemicalClassName());
         if (theChemicalClass == null) {
         	theChemicalClass = new ChemicalClass();
         	theChemicalClass.setChemicalClassName(inTherapyData.getChemicalClassName());
         }
         System.out.println("Got ChemicalClass attributes in populateTherapy");
         
         //Get/create the BiologicalProcess
         BiologicalProcess theBiologicalProcess = BiologicalProcessManagerSingleton.instance().getByName(
         		inTherapyData.getProcessName());
         if (theBiologicalProcess == null) {
         	theBiologicalProcess = new BiologicalProcess();
         	theBiologicalProcess.setProcessName(inTherapyData.getProcessName());
         }
         System.out.println("Got BiologicalProcess attributes in populateTherapy");
         
         //Get/create the AgentTarget
         AgentTarget theAgentTarget = AgentTargetManagerSingleton.instance().getByName(
         		inTherapyData.getTargetName());
         if (theAgentTarget == null) {
         	theAgentTarget = new AgentTarget();
         	theAgentTarget.setTargetName(inTherapyData.getTargetName());
         }        
         log.trace("Exiting populateTherapy");
     }    
 }
