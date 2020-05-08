 /**
  * @author dgeorge
  * 
 * $Id: AnimalModelManagerImpl.java,v 1.62 2005-12-21 15:40:29 georgeda Exp $
  *
  * $Log: not supported by cvs2svn $
 * Revision 1.61  2005/12/01 13:43:36  georgeda
 * Defect #226, reuse Taxon objects and do not delete them from Database
 *
  * Revision 1.60  2005/11/16 15:31:05  georgeda
  * Defect #41. Clean up of email functionality
  *
  * Revision 1.59  2005/11/14 14:17:57  georgeda
  * Cleanup
  *
  * Revision 1.58  2005/11/08 16:48:24  georgeda
  * Changes for images
  *
  * Revision 1.57  2005/11/07 19:15:17  pandyas
  * modified for clinical marker screen
  *
  * Revision 1.56  2005/11/04 14:44:25  georgeda
  * Cleaned up histopathology/assoc metastasis
  *
  * Revision 1.55  2005/11/03 18:57:13  pandyas
  * Modified for histopathology screens
  *
  * Revision 1.54  2005/11/03 18:11:04  georgeda
  * Fix old vocab problem
  *
  * Revision 1.53  2005/11/03 17:22:35  schroedn
  * Changed how Associated Expression for Genetic Description were coded, cleaned up
  *
  * Revision 1.52  2005/11/03 17:01:30  georgeda
  * Change taxon creation strat.
  *
  * Revision 1.51  2005/11/02 21:46:09  georgeda
  * Fixed creation of sex distribution
  *
  * Revision 1.50  2005/11/02 20:56:04  schroedn
  * Added Staining to Image submission
  *
  * Revision 1.49  2005/11/02 19:02:55  pandyas
  * Added e-mail functionality
  *
  * Revision 1.48  2005/10/27 17:15:22  schroedn
  * Updated addAssociatedExpression for all Genetic Descriptions
  *
  * Revision 1.45  2005/10/26 20:42:52  schroedn
  * merged changes, Added AssocExpression to EngineeredTransgene submission page
  *
  * Revision 1.44  2005/10/26 20:16:57  pandyas
  * implemented model availability
  *
  * Revision 1.43  2005/10/24 21:00:17  schroedn
  * addImage added
  *
  * Revision 1.42  2005/10/24 18:05:36  georgeda
  * Show the modified date in the returned models
  *
  * Revision 1.41  2005/10/24 17:10:39  georgeda
  * First pass at duplicate
  *
  * Revision 1.40  2005/10/24 13:28:06  georgeda
  * Cleanup changes
  *
  * Revision 1.39  2005/10/21 19:38:37  schroedn
  * Added caImage ftp capabilities for EngineeredTransgene, GenomicSegment and TargetedModification
  *
  * Revision 1.38  2005/10/21 16:07:26  pandyas
  * implementation of animal availability
  *
  * Revision 1.37  2005/10/20 20:39:50  stewardd
  * modified to use constant value instead of a hard coded string in messageKeys
  *
  * Revision 1.36  2005/10/20 18:55:38  stewardd
  * Employs new EmailUtil API supporting e-mail content built from ResourceBundle-stored templates with support for variables (via Velocity API)
  *
  * Revision 1.35  2005/10/19 19:26:35  pandyas
  * added admin route to growth factor
  *
  * Revision 1.34  2005/10/18 16:23:31  georgeda
  * Changed getModelsByUser to return models where the PI is the user as well
  *
  * Revision 1.33  2005/10/13 20:47:25  georgeda
  * Correctly handle the PI
  *
  * Revision 1.32  2005/10/12 15:55:16  georgeda
  * Do not reuse taxon since it has an uncontolled vocab
  *
  * Revision 1.31  2005/10/11 20:52:51  schroedn
  * EngineeredTransgene and GenomicSegment edit/save works, not image
  *
  * EngineeredTransgene - 'Other' Species not working
  *
  * Revision 1.30  2005/10/10 20:05:19  pandyas
  * removed animalmodel reference in populate method
  *
  * Revision 1.29  2005/10/10 14:08:02  georgeda
  * Performance improvement
  *
  * Revision 1.28  2005/10/07 16:27:54  georgeda
  * Implemented paganation
  *
  * Revision 1.27  2005/10/06 20:43:45  schroedn
  * Fixed missing reference
  *
  * Revision 1.26  2005/10/06 20:41:51  schroedn
  * InducedMutation, TargetedMutation, GenomicSegment changes
  *
  * Revision 1.25  2005/10/06 19:33:10  pandyas
  * modified for Therapy screen
  *
  * Revision 1.24  2005/10/06 13:36:09  georgeda
  * Changed ModelCharacteristics interface to be consistent w/ the rest of the interfaces
  *
  * Revision 1.23  2005/10/05 15:17:48  schroedn
  * SpontaneousMutation create and edit now working
  *
  * Revision 1.22  2005/10/04 20:12:52  schroedn
  * Added Spontaneous Mutation, InducedMutation, Histopathology, TargetedModification and GenomicSegment
  *
  * Revision 1.21  2005/10/03 13:51:36  georgeda
  * Search changes
  *
  * Revision 1.20  2005/09/30 18:59:06  pandyas
  * modified for cell line
  *
  * Revision 1.19  2005/09/28 21:20:02  georgeda
  * Finished up converting to new manager
  *
  * Revision 1.18  2005/09/28 15:12:29  schroedn
  * Added GeneDelivery and Xenograft/Transplant, businass logic in Managers
  *
  * Revision 1.17  2005/09/28 14:14:00  schroedn
  * Added saveXenograft and saveGeneDelivery
  *
  * Revision 1.16  2005/09/28 12:46:12  georgeda
  * Cleanup of animal manager
  *
  * Revision 1.15  2005/09/27 19:17:16  georgeda
  * Refactor of CI managers
  *
  * Revision 1.14  2005/09/27 16:44:49  georgeda
  * Added ChemicalDrug handling
  * Revision 1.13  2005/09/26 14:04:36  georgeda
  * Cleanup for cascade fix and common manager code
  *
  * Revision 1.12  2005/09/23 14:55:19  georgeda
  * Made SexDistribution a reference table
  *
  * Revision 1.11  2005/09/22 18:55:53  georgeda
  * Get coordinator from user in properties file
  *
  * Revision 1.10  2005/09/19 18:13:51  georgeda
  * Changed boolean to Boolean
  *
  * Revision 1.9  2005/09/19 12:55:24  georgeda
  * Handle empty sex distribution table
  *
  * Revision 1.8  2005/09/16 15:52:57  georgeda
  * Changes due to manager re-write
  *
  *
  */
 
 package gov.nih.nci.camod.service.impl;
 
 import gov.nih.nci.camod.Constants;
 import gov.nih.nci.camod.domain.*;
 import gov.nih.nci.camod.service.AnimalModelManager;
 import gov.nih.nci.camod.util.DuplicateUtil;
 import gov.nih.nci.camod.util.MailUtil;
 import gov.nih.nci.camod.webapp.form.*;
 import gov.nih.nci.common.persistence.Persist;
 import gov.nih.nci.common.persistence.Search;
 import gov.nih.nci.common.persistence.exception.PersistenceException;
 import gov.nih.nci.common.persistence.hibernate.HibernateUtil;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.ResourceBundle;
 import java.util.StringTokenizer;
 import java.util.TreeMap;
 
 import javax.servlet.http.HttpServletRequest;
 
 /**
  * Manages fetching/saving/updating of animal models
  */
 public class AnimalModelManagerImpl extends BaseManager implements AnimalModelManager {
 
     /**
      * Get all of the animal models in the DB
      * 
      * @return the list of all animal models
      * 
      * @exception throws
      *                an Exception if an error occurred
      */
     public List getAll() throws Exception {
         log.trace("In AnimalModelManagerImpl.getAll");
         return super.getAll(AnimalModel.class);
     }
 
     /**
      * Get all of the animal models submitted by a username
      * 
      * @param inUsername
      *            the username the models are submitted by
      * 
      * @return the list of animal models
      * 
      * @exception throws
      *                an Exception if an error occurred
      */
     public List getAllByUser(String inUsername) throws Exception {
 
         log.trace("In AnimalModelManagerImpl.getAllByUser");
 
         return QueryManagerSingleton.instance().getModelsByUser(inUsername);
     }
 
     /**
      * Get all of the animal models of a specific state
      * 
      * @param inState
      *            the state to query for
      * 
      * @return the list of animal models
      * 
      * @exception Exception
      *                if an error occurred
      */
     public List getAllByState(String inState) throws Exception {
 
         log.trace("Entering AnimalModelManagerImpl.getAllByState");
 
         // The list of AnimalModels to be returned
         List theAnimalModels = new ArrayList();
 
         // The following two objects are needed for eQBE.
         AnimalModel theAnimalModel = new AnimalModel();
         theAnimalModel.setState(inState);
 
         try {
             theAnimalModels = Search.query(theAnimalModel);
         } catch (Exception e) {
             log.error("Exception occurred in getAllByState", e);
             throw e;
         }
 
         log.trace("Exiting AnimalModelManagerImpl.getAllByState");
 
         return theAnimalModels;
     }
 
     /**
      * Get all of the models of a specific state
      * 
      * @param inState
      *            the state to query for
      * 
      * @return the list of models
      * 
      * @exception Exception
      *                if an error occurred
      */
     public List getAllByStateForPerson(String inState, Person inPerson) throws Exception {
 
         log.trace("In CommentsManagerImpl.getAllByStateForPerson");
 
         return QueryManagerSingleton.instance().getModelsByStateForPerson(inState, inPerson);
     }
 
     /**
      * Get a specific animal model
      * 
      * @param id
      *            The unique id for the model
      * 
      * @return the animal model if found, null otherwise
      * @throws Exception
      * 
      * @exception Exception
      *                if an error occurred
      */
     public AnimalModel get(String id) throws Exception {
         log.trace("In AnimalModelManagerImpl.get");
 
         AnimalModel theAnimalModel = (AnimalModel) super.get(id, AnimalModel.class);
 
         // Set the modified date in case we save a change
         theAnimalModel.getAvailability().setModifiedDate(new Date());
 
         return theAnimalModel;
     }
 
     /**
      * Save an animal model
      * 
      * @param id
      *            The unique id for the model
      * 
      * @exception Exception
      *                if an error occurred
      */
     public void save(AnimalModel inAnimalModel) throws Exception {
         log.trace("In AnimalModelManagerImpl.save");
         super.save(inAnimalModel);
     }
 
     /**
      * Do a deep copy of the passed in animal model
      * 
      * @return the list of all animal models
      * 
      * @exception throws
      *                an Exception if an error occurred
      */
     public AnimalModel duplicate(AnimalModel inAnimalModel) throws Exception {
         log.trace("In AnimalModelManagerImpl.duplicate");
 
         AnimalModel theDuplicatedModel = (AnimalModel) DuplicateUtil.duplicateBean(inAnimalModel);
 
         String theNewModelDescriptor = "Copy of " + theDuplicatedModel.getModelDescriptor();
         theDuplicatedModel.setModelDescriptor(theNewModelDescriptor);
 
         theDuplicatedModel.getAvailability().setModifiedDate(null);
         theDuplicatedModel.getAvailability().setEnteredDate(new Date());
 
         save(theDuplicatedModel);
 
         return theDuplicatedModel;
     }
 
     /**
      * Update an animal model and create an associated log entry
      * 
      * @param id
      *            The unique id for the model
      * @throws Exception
      * 
      * @exception Exception
      *                if an error occurred
      */
     public void updateAndAddLog(AnimalModel inAnimalModel, Log inLog) throws Exception {
 
         log.trace("Entering updateAndAddLog");
 
         try {
 
             // Make sure they get saved together
             HibernateUtil.beginTransaction();
             Persist.save(inAnimalModel);
             Persist.save(inLog);
             HibernateUtil.commitTransaction();
 
         } catch (PersistenceException pe) {
             HibernateUtil.rollbackTransaction();
             log.error("PersistenceException in save", pe);
             throw pe;
         } catch (Exception e) {
             HibernateUtil.rollbackTransaction();
             log.error("Exception in save", e);
             throw e;
         }
 
         log.trace("Exiting updateAndAddLog");
     }
 
     /**
      * Create a new/unsaved animal model
      * 
      * @param inModelCharacteristicsData
      *            The values for the model and associated objects
      * 
      * @param inUsername
      *            The submitter
      * 
      * @return the created and unsaved AnimalModel
      * @throws Exception
      */
     public AnimalModel create(ModelCharacteristicsData inModelCharacteristicsData, String inUsername) throws Exception {
 
         log.trace("Entering AnimalModelManagerImpl.create");
 
         AnimalModel theAnimalModel = new AnimalModel();
 
         log.trace("Exiting AnimalModelManagerImpl.create");
         return populateAnimalModel(inModelCharacteristicsData, inUsername, theAnimalModel);
     }
 
     /**
      * Update the animal model w/ the new characteristics and save
      * 
      * @param inModelCharacteristicsData
      *            The new values for the model and associated objects
      * 
      * @param inAnimalModel
      *            The animal model to update
      */
     public void update(ModelCharacteristicsData inModelCharacteristicsData, AnimalModel inAnimalModel) throws Exception {
 
         log.trace("Entering AnimalModelManagerImpl.update");
         log.debug("Updating animal model: " + inAnimalModel.getId());
 
         // Populate w/ the new values and save
         inAnimalModel = populateAnimalModel(inModelCharacteristicsData, null, inAnimalModel);
         save(inAnimalModel);
 
         log.trace("Exiting AnimalModelManagerImpl.update");
     }
 
     /**
      * Remove the animal model from the system. Should remove all associated
      * data as well
      * 
      * @param id
      *            The unique id of the animal model to delete
      * 
      * @throws Exception
      *             An error occurred when attempting to delete the model
      */
     public void remove(String id) throws Exception {
         log.trace("In AnimalModelManagerImpl.remove");
         super.remove(id, AnimalModel.class);
     }
 
     /**
      * Search for animal models based on: - modelName - piName - siteOfTumor -
      * speciesName
      * 
      * Note: This method is currently a dummy search method and simply returns
      * all the animal model objects in the database. Searching using eQBE needs
      * to be done.
      * 
      * @throws Exception
      */
     public List search(SearchData inSearchData) throws Exception {
 
         log.trace("In search");
         List theAnimalModels = QueryManagerSingleton.instance().searchForAnimalModels(inSearchData);
 
         List theDisplayList = new ArrayList();
 
         // Add AnimalModel DTO's so that the paganation works quickly
         for (int i = 0, j = theAnimalModels.size(); i < j; i++) {
             AnimalModel theAnimalModel = (AnimalModel) theAnimalModels.get(i);
             theDisplayList.add(new AnimalModelSearchResult(theAnimalModel));
         }
 
         return theDisplayList;
     }
 
     // Populate the model based on the model characteristics form passed in. It
     // will update associated
     // object if they exist, create them if they don't.
     private AnimalModel populateAnimalModel(ModelCharacteristicsData inModelCharacteristics, String inUsername,
             AnimalModel inAnimalModel) throws Exception {
 
         log.trace("Entering populateAnimalModel");
 
         // Handle the person information
         if (inUsername != null) {
             Person theSubmitter = PersonManagerSingleton.instance().getByUsername(inUsername);
             if (theSubmitter == null) {
 
                 throw new IllegalArgumentException("Unknown user: " + inUsername);
             }
             inAnimalModel.setSubmitter(theSubmitter);
         }
 
         Person thePI = PersonManagerSingleton.instance().getByUsername(
                 inModelCharacteristics.getPrincipalInvestigator());
 
         if (thePI == null) {
             throw new IllegalArgumentException("Unknown principal investigator: " + inUsername);
         }
 
         inAnimalModel.setPrincipalInvestigator(thePI);
 
         // Set the animal model information
         boolean isToolMouse = inModelCharacteristics.getIsToolMouse().equals("yes") ? true : false;
         inAnimalModel.setIsToolMouse(new Boolean(isToolMouse));
         inAnimalModel.setUrl(inModelCharacteristics.getUrl());
         inAnimalModel.setModelDescriptor(inModelCharacteristics.getModelDescriptor());
         inAnimalModel.setExperimentDesign(inModelCharacteristics.getExperimentDesign());
 
         Taxon theOldTaxon = inAnimalModel.getSpecies();
 
         // Create/reuse the taxon
         Taxon theTaxon = TaxonManagerSingleton.instance().getOrCreate(inModelCharacteristics.getScientificName(),
                 inModelCharacteristics.getEthinicityStrain(), inModelCharacteristics.getEthnicityStrainUnctrlVocab());
 
         // Other option
         if (theTaxon.getEthnicityStrainUnctrlVocab() != null) {
 
             // It doesn't match the old one
            if (theOldTaxon == null || !theTaxon.getEthnicityStrainUnctrlVocab().equals(theOldTaxon.getEthnicityStrainUnctrlVocab())) {
                 log.trace("Sending Notification eMail - new EthinicityStrain added");
 
                 ResourceBundle theBundle = ResourceBundle.getBundle("camod");
 
                 // Iterate through all the reciepts in the config file
                 String recipients = theBundle.getString(Constants.BundleKeys.NEW_UNCONTROLLED_VOCAB_NOTIFY_KEY);
                 StringTokenizer st = new StringTokenizer(recipients, ",");
                 String inRecipients[] = new String[st.countTokens()];
                 for (int i = 0; i < inRecipients.length; i++) {
                     inRecipients[i] = st.nextToken();
                 }
 
                 String inSubject = theBundle.getString(Constants.BundleKeys.NEW_UNCONTROLLED_VOCAB_SUBJECT_KEY);
                 String inFrom = inAnimalModel.getSubmitter().getEmailAddress();
 
                 // gather message keys and variable values to build the e-mail
                 // content with
                 String[] messageKeys = { Constants.Admin.NONCONTROLLED_VOCABULARY };
                 Map values = new TreeMap();
                 values.put("type", "EthinicityStrain");
                 values.put("value", inModelCharacteristics.getEthnicityStrainUnctrlVocab());
                 values.put("submitter", inAnimalModel.getSubmitter());
                 values.put("model", inAnimalModel.getModelDescriptor());
                 values.put("modelstate", inAnimalModel.getState());
 
                 // Send the email
                 try {
                     MailUtil.sendMail(inRecipients, inSubject, "", inFrom, messageKeys, values);
                 } catch (Exception e) {
                     log.error("Caught exception sending mail: ", e);
                     e.printStackTrace();
                 }
             }
         }
 
         Phenotype thePhenotype = inAnimalModel.getPhenotype();
         if (thePhenotype == null) {
             thePhenotype = new Phenotype();
         }
 
         // Get/create the sex distribution
         if (inModelCharacteristics.getType() != null) {
             SexDistribution theSexDistribution = SexDistributionManagerSingleton.instance().getByType(
                     inModelCharacteristics.getType());
             thePhenotype.setSexDistribution(theSexDistribution);
         }
 
         // Create the phenotype
         thePhenotype.setDescription(inModelCharacteristics.getDescription());
         thePhenotype.setBreedingNotes(inModelCharacteristics.getBreedingNotes());
 
         // Get the availability
         Availability theAvailability = inAnimalModel.getAvailability();
 
         // When the model was created
         if (theAvailability == null) {
             theAvailability = new Availability();
         } else {
             theAvailability.setModifiedDate(new Date());
         }
         theAvailability.setEnteredDate(new Date());
 
         // Convert the date
         Date theDate = new Date();
         if (!inModelCharacteristics.getReleaseDate().equals("immediately")) {
 
             // Convert the string to a date. Default to "now" if there are any
             // errors
             DateFormat theDateFormat = new SimpleDateFormat("MM/dd/yyyy");
             try {
                 theDate = theDateFormat.parse(inModelCharacteristics.getCalendarReleaseDate());
             } catch (Exception e) {
                 log.error("Error parsing release date, defaulting to now", e);
             }
         }
         theAvailability.setReleaseDate(theDate);
 
         // Associated the created objects
         inAnimalModel.setAvailability(theAvailability);
         inAnimalModel.setPhenotype(thePhenotype);
         inAnimalModel.setSpecies(theTaxon);
 
         log.trace("Exiting populateAnimalModel");
 
         return inAnimalModel;
     }
 
     public void addXenograft(AnimalModel inAnimalModel, XenograftData inXenograftData) throws Exception {
 
         System.out.println("<AnimalModelManagerImpl populate> Entering addXenograft() ");
 
         log.trace("Entering saveXenograft");
 
         Xenograft theXenograft = XenograftManagerSingleton.instance().create(inXenograftData, inAnimalModel);
 
         inAnimalModel.addXenograft(theXenograft);
         save(inAnimalModel);
 
         System.out.println("<AnimalModelManagerImpl populate> Exiting addXenograft() ");
 
         log.trace("Exiting saveXenograft");
     }
 
     /**
      * Add a gene delivery
      * 
      * @param inAnimalModel
      *            the animal model that has the therapy
      * @param inGeneDeliveryData
      *            the gene delivery
      * @throws Exception
      */
     public void addGeneDelivery(AnimalModel inAnimalModel, GeneDeliveryData inGeneDeliveryData) throws Exception {
 
         log.info("<AnimalModelManagerImpl> Entering addGeneDelivery");
 
         GeneDelivery theGeneDelivery = GeneDeliveryManagerSingleton.instance()
                 .create(inAnimalModel, inGeneDeliveryData);
         inAnimalModel.addGeneDelivery(theGeneDelivery);
         save(inAnimalModel);
 
         log.info("<AnimalModelManagerImpl> Exiting addGeneDelivery");
     }
 
     /**
      * Add a chemical/drug therapy
      * 
      * @param inAnimalModel
      *            the animal model that has the therapy
      * @param inChemicalDrugData
      *            the new chemical drug data
      * @throws Exception
      */
     public void addTherapy(AnimalModel inAnimalModel, ChemicalDrugData inChemicalDrugData) throws Exception {
 
         log.trace("Entering AnimalModelManagerImpl.addTherapy");
         Therapy theTherapy = TherapyManagerSingleton.instance().create(inAnimalModel, inChemicalDrugData);
         inAnimalModel.addTherapy(theTherapy);
         save(inAnimalModel);
         log.trace("Exiting AnimalModelManagerImpl.addTherapy");
     }
 
     /**
      * Add an environmental factor therapy
      * 
      * @param inAnimalModel
      *            the animal model that has the therapy
      * @param inEnvironmentalFactorData
      *            the ef data
      * @throws Exception
      */
     public void addTherapy(AnimalModel inAnimalModel, EnvironmentalFactorData inEnvironmentalFactorData)
             throws Exception {
 
         log.trace("Entering AnimalModelManagerImpl.addTherapy");
         Therapy theTherapy = TherapyManagerSingleton.instance().create(inAnimalModel, inEnvironmentalFactorData);
         inAnimalModel.addTherapy(theTherapy);
         save(inAnimalModel);
         log.trace("Exiting AnimalModelManagerImpl.addTherapy");
     }
 
     /**
      * Add an environmental factor therapy
      * 
      * @param inAnimalModel
      *            the animal model that has the therapy
      * @param inRadiationData
      *            the new radiation data
      * @throws Exception
      */
     public void addTherapy(AnimalModel inAnimalModel, RadiationData inRadiationData) throws Exception {
 
         log.trace("Entering AnimalModelManagerImpl.addTherapy");
         Therapy theTherapy = TherapyManagerSingleton.instance().create(inAnimalModel, inRadiationData);
         inAnimalModel.addTherapy(theTherapy);
         save(inAnimalModel);
         log.trace("Exiting AnimalModelManagerImpl.addTherapy");
     }
 
     /**
      * Add an environmental factor therapy
      * 
      * @param inAnimalModel
      *            the animal model that has the therapy
      * @param inViralTreatmentData
      *            the new viral treatment data
      * @throws Exception
      */
     public void addTherapy(AnimalModel inAnimalModel, ViralTreatmentData inViralTreatmentData) throws Exception {
 
         log.trace("Entering AnimalModelManagerImpl.addTherapy");
         Therapy theTherapy = TherapyManagerSingleton.instance().create(inAnimalModel, inViralTreatmentData);
         inAnimalModel.addTherapy(theTherapy);
         save(inAnimalModel);
         log.trace("Exiting AnimalModelManagerImpl.addTherapy");
     }
 
     /**
      * Add a growth factor
      * 
      * @param inAnimalModel
      *            the animal model that has the therapy
      * @param inGrowthFactorData
      *            the new growth factor data
      * @throws Exception
      */
     public void addTherapy(AnimalModel inAnimalModel, GrowthFactorData inGrowthFactorData) throws Exception {
 
         log.trace("Entering AnimalModelManagerImpl.addTherapy");
         Therapy theTherapy = TherapyManagerSingleton.instance().create(inAnimalModel, inGrowthFactorData);
         inAnimalModel.addTherapy(theTherapy);
         save(inAnimalModel);
         log.trace("Exiting AnimalModelManagerImpl.addTherapy");
     }
 
     /**
      * Add a hormone
      * 
      * @param inAnimalModel
      *            the animal model that has the therapy
      * @param inHormoneData
      *            the new growth factor data
      * @throws Exception
      */
     public void addTherapy(AnimalModel inAnimalModel, HormoneData inHormoneData) throws Exception {
 
         log.trace("Entering AnimalModelManagerImpl.addTherapy");
         Therapy theTherapy = TherapyManagerSingleton.instance().create(inAnimalModel, inHormoneData);
         inAnimalModel.addTherapy(theTherapy);
         save(inAnimalModel);
         log.trace("Exiting AnimalModelManagerImpl.addTherapy");
     }
 
     /**
      * Add a nutritional factor
      * 
      * @param inAnimalModel
      *            the animal model that has the therapy
      * @param inNutritionalFactorData
      *            the new nutrional factor data
      * @throws Exception
      */
     public void addTherapy(AnimalModel inAnimalModel, NutritionalFactorData inNutritionalFactorData) throws Exception {
 
         log.trace("Entering AnimalModelManagerImpl.addTherapy");
         Therapy theTherapy = TherapyManagerSingleton.instance().create(inAnimalModel, inNutritionalFactorData);
         inAnimalModel.addTherapy(theTherapy);
         save(inAnimalModel);
         log.trace("Exiting AnimalModelManagerImpl.addTherapy");
     }
 
     /**
      * Add a surgery/other
      * 
      * @param inAnimalModel
      *            the animal model that has the therapy
      * @param inSurgeryData
      *            the new surgery data
      * @throws Exception
      */
     public void addTherapy(AnimalModel inAnimalModel, SurgeryData inSurgeryData) throws Exception {
 
         log.trace("Entering AnimalModelManagerImpl.addTherapy");
         Therapy theTherapy = TherapyManagerSingleton.instance().create(inAnimalModel, inSurgeryData);
         inAnimalModel.addTherapy(theTherapy);
         save(inAnimalModel);
         log.trace("Exiting AnimalModelManagerImpl.addTherapy");
     }
 
     /**
      * Add a cell line
      * 
      * @param inAnimalModel
      *            the animal model that has the cell line
      * @param inSurgeryData
      *            the new cell line data
      * @throws Exception
      */
     public void addCellLine(AnimalModel inAnimalModel, CellLineData inCellLineData) throws Exception {
 
         log.debug("<AnimalModelManagerImpl> Entering saveCellLine");
         CellLine theCellLine = CellLineManagerSingleton.instance().create(inCellLineData);
         inAnimalModel.addCellLine(theCellLine);
         save(inAnimalModel);
 
         log.debug("<AnimalModelManagerImpl> Exiting addCellLine");
     }
 
     /**
      * Add a SpontaneousMutation
      */
     public void addGeneticDescription(AnimalModel inAnimalModel, SpontaneousMutationData inSpontaneousMutationData)
             throws Exception {
 
         log.trace("Entering addGeneticDescription (spontaneousMutation)");
         SpontaneousMutation theSpontaneousMutation = SpontaneousMutationManagerSingleton.instance().create(
                 inSpontaneousMutationData);
         // System.out.println(theSpontaneousMutation.getName());
         inAnimalModel.addSpontaneousMutation(theSpontaneousMutation);
         save(inAnimalModel);
 
         log.trace("Exiting addGeneticDescription (spontaneousMutation)");
     }
 
     /**
      * Add a InducedMutation
      */
     public void addGeneticDescription(AnimalModel inAnimalModel, InducedMutationData inInducedMutationData)
             throws Exception {
 
         log.trace("Entering addGeneticDescription (inducedMutation)");
 
         InducedMutation theInducedMutation = InducedMutationManagerSingleton.instance().create(inAnimalModel,
                 inInducedMutationData);
         inAnimalModel.addEngineeredGene(theInducedMutation);
         save(inAnimalModel);
 
         log.trace("Exiting addGeneticDescription (inducedMutation)");
     }
 
     /**
      * Add a TargetedModification
      */
     public void addGeneticDescription(AnimalModel inAnimalModel, TargetedModificationData inTargetedModificationData,
             HttpServletRequest request) throws Exception {
 
         log.trace("Entering addGeneticDescription (TargetedModification)");
 
         TargetedModification theTargetedModification = TargetedModificationManagerSingleton.instance().create(
                 inAnimalModel, inTargetedModificationData, request);
         // System.out.println(theGene.getName() );
 
         inAnimalModel.addEngineeredGene(theTargetedModification);
         save(inAnimalModel);
 
         log.trace("Exiting addGeneticDescription (TargetedModification)");
     }
 
     public void addGeneticDescription(AnimalModel inAnimalModel, GenomicSegmentData inGenomicSegmentData,
             HttpServletRequest request) throws Exception {
 
         log.trace("Entering addGeneticDescription (GenomicSegment)");
 
         GenomicSegment theGenomicSegment = GenomicSegmentManagerSingleton.instance().create(inAnimalModel,
                 inGenomicSegmentData, request);
         // System.out.println(theGenomicSegment.getName() );
 
         inAnimalModel.addEngineeredGene(theGenomicSegment);
         save(inAnimalModel);
 
         log.trace("Exiting addGeneticDescription (GenomicSegment)");
     }
 
     public void addGeneticDescription(AnimalModel inAnimalModel, EngineeredTransgeneData inEngineeredTransgeneData,
             HttpServletRequest request) throws Exception {
 
         log.trace("Entering addGeneticDescription (EngineeredTransgene)");
 
         Transgene theEngineeredTransgene = EngineeredTransgeneManagerSingleton.instance().create(
                 inEngineeredTransgeneData, request);
         // System.out.println(theGenomicSegment.getName() );
 
         inAnimalModel.addEngineeredGene(theEngineeredTransgene);
         save(inAnimalModel);
 
         log.trace("Exiting addGeneticDescription (EngineeredTransgene)");
     }
 
     public void addImage(AnimalModel inAnimalModel, ImageData inImageData, String inPath) throws Exception {
 
         log.trace("Entering addImage (Image)");
 
         Image theImage = ImageManagerSingleton.instance().create(inAnimalModel, inImageData, inPath,
                 Constants.CaImage.FTPMODELSTORAGEDIRECTORY);
         inAnimalModel.addImage(theImage);
         save(inAnimalModel);
 
         log.trace("Exiting addImage (Image)");
     }
 
     /**
      * Add a therapy
      * 
      * @param inAnimalModel
      *            the animal model that has the therapy
      * @param inChemicalDrugData
      *            the new therapy data
      * @throws Exception
      */
 
     public void addTherapy(AnimalModel inAnimalModel, TherapyData inTherapyData) throws Exception {
 
         System.out.println("<AnimalModelManagerImpl addTherapy>");
 
         log.trace("Entering AnimalModelManagerImpl.addTherapy");
 
         Therapy theTherapy = TherapyManagerSingleton.instance().create(inAnimalModel, inTherapyData);
         inAnimalModel.addTherapy(theTherapy);
         save(inAnimalModel);
 
         log.trace("Exiting AnimalModelManagerImpl.addTherapy");
     }
 
     /**
      * Add an Availability
      * 
      * @param inAnimalModel
      *            the animal model that has the Availability
      * @param inAvailabilityData
      *            the new therapy data
      * @throws Exception
      */
     public void addAvailability(AnimalModel inAnimalModel, AvailabilityData inAvailabilityData) throws Exception {
 
         System.out.println("<AnimalModelManagerImpl addAvailability>");
 
         log.info("Entering AnimalModelManagerImpl.addAvailability");
         AnimalAvailability theAvailability = AvailabilityManagerSingleton.instance().create(inAvailabilityData);
         inAnimalModel.addAnimalAvailability(theAvailability);
         save(inAnimalModel);
         log.info("Exiting AnimalModelManagerImpl.addAvailability");
     }
 
     /**
      * Add an Availability
      * 
      * @param inAnimalModel
      *            the animal model that has the Availability
      * @param inAvailabilityData
      *            the new therapy data
      * @throws Exception
      */
 
     public void addInvestigatorAvailability(AnimalModel inAnimalModel, AvailabilityData inAvailabilityData)
             throws Exception {
 
         System.out.println("<AnimalModelManagerImpl addInvestigatorAvailability>");
 
         log.info("Entering AnimalModelManagerImpl.addInvestigatorAvailability");
         AnimalAvailability theAvailability = AvailabilityManagerSingleton.instance().createInvestigator(
                 inAvailabilityData);
         inAnimalModel.addAnimalAvailability(theAvailability);
         save(inAnimalModel);
         log.info("Exiting AnimalModelManagerImpl.addInvestigatorAvailability");
     }
 
     public void addAssociatedExpression(AnimalModel inAnimalModel, EngineeredGene inEngineeredGene,
             AssociatedExpressionData inAssociatedExpressionData) throws Exception {
 
         System.out.println("<AnimalModelManagerImpl addAssociatedExpression>");
         log.trace("Entering AnimalModelManagerImpl.addAssociatedExpression");
 
         // addAssociatedExpression (ExpressionFeature)
         EngineeredTransgeneManagerSingleton.instance().createAssocExpression(inAssociatedExpressionData,
                 inEngineeredGene);
         save(inAnimalModel);
 
         log.trace("Exiting AnimalModelManagerImpl.addAssociatedExpression");
     }
 
     public void addPublication(AnimalModel inAnimalModel, PublicationData inPublicationData) throws Exception {
 
         log.trace("Entering AnimalModelManagerImpl.addPublication");
 
         // addAssociatedExpression (ExpressionFeature
         Publication thePublication = PublicationManagerSingleton.instance().create(inPublicationData);
         inAnimalModel.addPublication(thePublication);
 
         save(inAnimalModel);
 
         log.trace("Exiting AnimalModelManagerImpl.addAssociatedExpression");
     }
 
     public void addHistopathology(AnimalModel inAnimalModel, HistopathologyData inHistopathologyData) throws Exception {
 
         log.info("Entering AnimalModelManagerImpl.addHistopathology_1");
 
         Histopathology theHistopathology = HistopathologyManagerSingleton.instance().createHistopathology(
                 inHistopathologyData);
         inAnimalModel.addHistopathology(theHistopathology);
 
         save(inAnimalModel);
 
         log.info("Exiting AnimalModelManagerImpl.addHistopathology");
     }
 
     public void addAssociatedMetastasis(AnimalModel inAnimalModel, Histopathology inHistopathology,
             AssociatedMetastasisData inAssociatedMetastasisData) throws Exception {
 
         log.info("Entering AnimalModelManagerImpl.addAssociatedMetastasis_1");
 
         HistopathologyManagerSingleton.instance().createAssociatedMetastasis(inAssociatedMetastasisData,
                 inHistopathology);
         save(inAnimalModel);
 
         log.info("Exiting AnimalModelManagerImpl.addHistopathology");
     }
 
     public void addClinicalMarker(AnimalModel inAnimalModel, Histopathology inHistopathology,
             ClinicalMarkerData inClinicalMarkerData) throws Exception {
 
         log.info("Entering AnimalModelManagerImpl.addHistopathology to inClinicalMarkerData");
 
         ClinicalMarkerManagerSingleton.instance().create(inClinicalMarkerData, inHistopathology);
         save(inAnimalModel);
 
         log.info("Exiting AnimalModelManagerImpl.addHistopathology to inClinicalMarkerData");
     }
 
 }
