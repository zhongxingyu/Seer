 /**
  *  @author 
  *  
 *  $Id: AnimalModelTreePopulateAction.java,v 1.44 2007-03-26 12:02:31 pandyas Exp $
  *  
  *  $Log: not supported by cvs2svn $
  *  Revision 1.43  2006/11/09 17:20:25  pandyas
  *  Commented out debug code
  *
  *  Revision 1.42  2006/10/17 16:11:00  pandyas
  *  modified during development of caMOD 2.2 - various
  *
  *  Revision 1.41  2006/05/03 20:04:37  pandyas
  *  Modified to add Morpholino object data to application
  *
  *  Revision 1.40  2006/04/19 15:09:16  georgeda
  *  Fixed issue w/ display of induced mutation
  *
  *  Revision 1.39  2006/04/17 19:09:40  pandyas
  *  caMod 2.1 OM changes
  *
  *  Revision 1.38  2005/12/06 18:49:10  georgeda
  *  Defect #247 - real fix this time for the problem
  *
  *  Revision 1.37  2005/11/14 14:19:51  georgeda
  *  Cleanup
  *
  *  Revision 1.36  2005/11/07 17:43:55  pandyas
  *  cleaned up item from problem tab
  *
  *  Revision 1.35  2005/11/03 18:54:10  pandyas
  *  Modified for histopathology screens
  *
  *  Revision 1.34  2005/11/01 18:14:28  schroedn
  *  Implementing 'Enter Publication' for CellLines and Therapy, fixed many bugs with Publication. Remaining known bug with "Fill in Fields" button
  *
  *  Revision 1.33  2005/10/31 13:46:28  georgeda
  *  Updates to handle back arrow
  *
  *  Revision 1.32  2005/10/27 17:17:34  schroedn
  *  Enter Assoc Expression to Engineered Transgene, Genomic Segment, Targeted Modification
  *
  *  Revision 1.31  2005/10/26 20:40:51  schroedn
  *  Added AssocExpression to EngineeredTransgene submission page
  *
  *  Revision 1.30  2005/10/26 20:14:42  pandyas
  *  implemented model availability
  *
  *  Revision 1.29  2005/10/24 21:04:47  schroedn
  *  Bug fixes, Javascript and Other fields
  *
  *  Revision 1.28  2005/10/24 13:28:17  georgeda
  *  Cleanup changes
  *
  *  Revision 1.27  2005/10/21 16:07:01  pandyas
  *  implementation of animal availability
  *
  *  Revision 1.26  2005/10/12 20:10:49  schroedn
  *  Added Validation
  *
  *  Revision 1.25  2005/10/11 20:52:55  schroedn
  *  EngineeredTransgene and GenomicSegment edit/save works, not image
  *
  *  EngineeredTransgene - 'Other' Species not working
  *
  *  Revision 1.24  2005/10/10 14:11:45  georgeda
  *  Changes for comment curation
  *
  *  Revision 1.23  2005/10/06 20:40:12  schroedn
  *  InducedMutation, TargetedMutation, GenomicSegment changes
  *
  *  Revision 1.22  2005/10/06 19:27:55  pandyas
  *  modified for Therapy screen
  *
  *  Revision 1.21  2005/10/04 20:13:20  schroedn
  *  Added Spontaneous Mutation, InducedMutation, Histopathology, TargetedModification and GenomicSegment
  *
  *  Revision 1.20  2005/09/28 15:13:57  schroedn
  *  Merged changes, tested
  *
  *  Revision 1.18  2005/09/22 15:17:20  georgeda
  *  More changes
  *
  *  Revision 1.17  2005/09/20 19:32:40  pandyas
  *  Added Cell Line functionality
  *
  *  Revision 1.16  2005/09/20 19:10:57  schroedn
  *  Added check for Agent != null, GeneDelivery has no Agent, uses GeneDelivery
  *
  *  Revision 1.15  2005/09/20 18:45:03  schroedn
  *  Merged many changes, added GeneDelivery
  *
  *  Revision 1.14  2005/09/19 18:15:28  georgeda
  *  Organized imports and changed boolean to Boolean
  *
  *  Revision 1.13  2005/09/16 15:52:56  georgeda
  *  Changes due to manager re-write
  *
  *  
  */
 package gov.nih.nci.camod.webapp.action;
 
 import gov.nih.nci.camod.Constants;
 import gov.nih.nci.camod.domain.*;
 import gov.nih.nci.camod.service.AnimalModelManager;
 
 import java.util.ArrayList;
 import java.util.*;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.*;
 
 /**
  * 
  * Populate the lists necessary to display an animal model.
  * 
  */
 public class AnimalModelTreePopulateAction extends BaseAction {
 
 	/**
 	 * Create the links for the submission subMenu
 	 * 
 	 * @param mapping
 	 * @param form
 	 * @param request
 	 * @param response
 	 * @return
 	 * @throws Exception
 	 */
 	public ActionForward execute(ActionMapping mapping, ActionForm form,
 			HttpServletRequest request, HttpServletResponse response)
 			throws Exception {
 
 		System.out
 				.println("<AnimalModelTreePopulateAction populate> Entering... ");
 		String speciesName = (String) request.getSession().getAttribute(Constants.AMMODELSPECIESCOMMONNAME);
 		log.info("speciesName: " + speciesName);
 
 		// Grab the current modelID from the session
 		String modelID = (String) request.getSession().getAttribute(
 				Constants.MODELID);
 
 		AnimalModelManager animalModelManager = (AnimalModelManager) getBean("animalModelManager");
 
 		// Transfer back to request scope
 		Object theErrors = request.getSession().getAttribute(
 				org.apache.struts.Globals.ERROR_KEY);
 		request.getSession().setAttribute(org.apache.struts.Globals.ERROR_KEY,
 				null);
 
 		if (theErrors != null) {
 			request
 					.setAttribute(org.apache.struts.Globals.ERROR_KEY,
 							theErrors);
 		} // end of if
 
 		try {
 
 			AnimalModel animalModel = animalModelManager.get(modelID);
 
 			// Retrieve a list of all publications assoicated with this Animal
 			// model
 			Set publicationSet = animalModel.getPublicationCollection();
 			Iterator it = publicationSet.iterator();
 			List<Publication> pubList = new ArrayList<Publication>();
 
 			while (it.hasNext()) {
 				Publication pub = (Publication) it.next();
 				pubList.add(pub);
 			}
 
 			// Print the list of GeneDelivery viralVectors for the Gene Delivery
 			// (Cardiogenic Intervention) Section
 			Set geneDeliverySet = animalModel.getGeneDeliveryCollection();
 			it = geneDeliverySet.iterator();
 			List<GeneDelivery> geneList = new ArrayList<GeneDelivery>();
 
 			while (it.hasNext()) {
 				GeneDelivery geneDelivery = (GeneDelivery) it.next();
 				geneList.add(geneDelivery);
 			}
 
 			// Retrieve a list of all cell lines assoicated with this Animal
 			// model
 			Set cellLineSet = animalModel.getCellLineCollection();
 			it = cellLineSet.iterator();
 			List<CellLine> cellList = new ArrayList<CellLine>();
 
 			while (it.hasNext()) {
 				CellLine cellLine = (CellLine) it.next();
 				cellList.add(cellLine);
 			}
 
 			// Retrieve a list of all Morpholinos associated with this Animal
 			// model
 			Set transientInterferenceSet = animalModel
 					.getTransientInterferenceCollection();
 			it = transientInterferenceSet.iterator();
 			List<TransientInterference> morpholinoList = new ArrayList<TransientInterference>();
 			List<TransientInterference> sirnaList = new ArrayList<TransientInterference>();
 
 			while (it.hasNext()) {
 				TransientInterference transInt = (TransientInterference) it
 						.next();
 				if (transInt.getTransientInterferenceMethod() != null) {
 					if (transInt.getTransientInterferenceMethod()
 							.getConceptCode().equals("C60700")) {
 						morpholinoList.add(transInt);
 					} else if (transInt.getTransientInterferenceMethod()
 							.getConceptCode().equals("C2191")) {
 						sirnaList.add(transInt);
 					}
 				}
 			}
 
 			// Retrieve a list of all availablty entries assoicated with this
 			// Animal model
 			Set availabilitySet = animalModel.getAnimalAvailabilityCollection();
 			it = availabilitySet.iterator();
 			List<AnimalAvailability> investigatorList = new ArrayList<AnimalAvailability>();
 			List<AnimalAvailability> jacksonLabList = new ArrayList<AnimalAvailability>();
 			List<AnimalAvailability> mmhccList = new ArrayList<AnimalAvailability>();
 			List<AnimalAvailability> imsrList = new ArrayList<AnimalAvailability>();
 			List<AnimalAvailability> zfinList = new ArrayList<AnimalAvailability>();			
 
 			while (it.hasNext()) {
 				AnimalAvailability availability = (AnimalAvailability) it
 						.next();
 				if (availability.getAnimalDistributor() != null) {
 					if (availability.getAnimalDistributor().getName().equals(
 							"Jackson Laboratory")) {
 
 						//System.out.println("\tAdded Jackson Laboratory Availability = "+ availability);
 						jacksonLabList.add(availability);
 					}
 					if (availability.getAnimalDistributor().getName().equals(
 							"Investigator")) {
 
 						//System.out.println("\tAdded Investigator Availability = "+ availability);
 						investigatorList.add(availability);
 					}
 
 					if (availability.getAnimalDistributor().getName().equals(
 							"MMHCC Repository")) {
 
 						//System.out.println("\tAdded MMHCC Repository Availability = "+ availability);
 						mmhccList.add(availability);
 					}
 					if (availability.getAnimalDistributor().getName().equals(
 							"IMSR")) {
 
 						//System.out.println("\tAdded IMSR Repository Availability = "+ availability);
 						imsrList.add(availability);
 					}
 					if (availability.getAnimalDistributor().getName().equals(
 					"ZFIN")) {
 
						//System.out.println("\tAdded IMSR Repository Availability = "+ availability);
 						zfinList.add(availability);
 					}					
 					
 				}
 			}
 
 			// Retrive the list of all Xenograft transplants assoicated with
 			// this Animal Model
 			Set xenograftSet = animalModel.getXenograftCollection();
 			it = xenograftSet.iterator();
 			List<Xenograft> xenoList = new ArrayList<Xenograft>();
 
 			while (it.hasNext()) {
 				Xenograft xenograft = (Xenograft) it.next();
 				xenoList.add(xenograft);
 			}
 
 			// Retrieve a list of all Histopathology entries associated with
 			// this Animal model
 			Set histopathologySet = animalModel.getHistopathologyCollection();
 			it = histopathologySet.iterator();
 			List<Histopathology> histopathList = new ArrayList<Histopathology>();
 			// The associatedMetatasisList and clinMarkerList are populated in
 			// subSubmit.jsp
 			List<Histopathology> associatedMetatasisList = new ArrayList<Histopathology>();
 			List<Histopathology> clinicalMarkerList = new ArrayList<Histopathology>();
 
 			while (it.hasNext()) {
 				Histopathology histopathology = (Histopathology) it.next();
 				histopathList.add(histopathology);
 			}
 
 			// Retrieve the list all SpontaneousMutations assoc with this
 			// AnimalModel
 			Set spontaneousMutationSet = animalModel
 					.getSpontaneousMutationCollection();
 			it = spontaneousMutationSet.iterator();
 			List<SpontaneousMutation> mutationList = new ArrayList<SpontaneousMutation>();
 
 			while (it.hasNext()) {
 				SpontaneousMutation spontaneousMutation = (SpontaneousMutation) it
 						.next();
 				mutationList.add(spontaneousMutation);
 			}
 
 			// Retrieve the list all EngineeredGenes assoc with this AnimalModel
 			Set engineeredGeneSet = animalModel.getEngineeredGeneCollection();
 			System.out
 					.println("<AnimalModelTreePopulateAction> Building engineeredGeneSet Tree ...");
 			it = engineeredGeneSet.iterator();
 
 			List<InducedMutation> inducedList = new ArrayList<InducedMutation>();
 			List<TargetedModification> targetedList = new ArrayList<TargetedModification>();
 			List<GenomicSegment> segmentList = new ArrayList<GenomicSegment>();
 			List<Transgene> engineeredList = new ArrayList<Transgene>();
 			List associatedExpressionList = new ArrayList();
 
 			while (it.hasNext()) {
 				//System.out.println("<AnimalModelTreePopulateAction> inside while EG loop ...");
 				EngineeredGene engineeredGene = (EngineeredGene) it.next();
 				if (engineeredGene instanceof InducedMutation) {
 					//System.out.println("Added InducedMutation to left menu ...");
 					inducedList.add((InducedMutation) engineeredGene);
 				}
 
 				else if (engineeredGene instanceof TargetedModification) {
 					//System.out.println("Added TargetedModification to left menu ...");
 					targetedList.add((TargetedModification) engineeredGene);
 				}
 
 				else if (engineeredGene instanceof GenomicSegment) {
 					//System.out.println("Added GenomicSegment to left menu ...");
 					segmentList.add((GenomicSegment) engineeredGene);
 				}
 
 				else if (engineeredGene instanceof Transgene) {
 					//System.out.println("Added Transgene to left menu ...");
 					engineeredList.add((Transgene) engineeredGene);
 				} else {
 
 				}
 			}
 
 			// Retrieve List of Images in Images Category
 			Set imageCollection = animalModel.getImageCollection();
 
 			// Print the list of Therapies for the Therapy
 			// Interventions Section
 			Set tySet = animalModel.getTherapyCollection();
 			it = tySet.iterator();
 			List<Therapy> therapyList = new ArrayList<Therapy>();
 
 			//System.out.println("<AnimalModelTreePopulateAction> Building Tree ...");
 
 			if (tySet == null || tySet.size() == 0) {
 				//System.out.println("<AnimalModelTreePopulateAction populate> no Therapy to add!");
 			} else {
 				while (it.hasNext()) {
 					Therapy ty = (Therapy) it.next();
 					Agent agent = ty.getAgent();
 					if (agent != null) {
 						// System.out.println("\tAdded therapy to therapyList");
 						therapyList.add(ty);
 					}
 				}
 
 			} // end of else
 
 			// Print the list of EnvironmentalFactors for the Cardiogenic
 			// Exposure Section
 			Set ceSet = animalModel.getCarcinogenExposureCollection();
 			it = ceSet.iterator();
 
 			List<CarcinogenExposure> surgeryList = new ArrayList<CarcinogenExposure>();
 			List<CarcinogenExposure> hormoneList = new ArrayList<CarcinogenExposure>();
 			List<CarcinogenExposure> growthFactorList = new ArrayList<CarcinogenExposure>();
 			List<CarcinogenExposure> viraltreatmentList = new ArrayList<CarcinogenExposure>();
 			List<CarcinogenExposure> chemicaldrugList = new ArrayList<CarcinogenExposure>();
 			List<CarcinogenExposure> environFactorList = new ArrayList<CarcinogenExposure>();
 			List<CarcinogenExposure> radiationList = new ArrayList<CarcinogenExposure>();
 			List<CarcinogenExposure> nutritionalFactorList = new ArrayList<CarcinogenExposure>();
 
 			if (ceSet.size() == 0) {
 				//System.out.println("<AnimalModelTreePopulateAction populate> no CarcinogenExposure to add!");
 			} else {
 				while (it.hasNext()) {
 					CarcinogenExposure ce = (CarcinogenExposure) it.next();
 					EnvironmentalFactor ef = ce.getEnvironmentalFactor();
 					if (ef != null) {
 						if (ef.getType().equals("Other")) {
 							//System.out.println("\tAdded CarcinogenExposure to surgeryList");
 							surgeryList.add(ce);
 						}
 						if (ef.getType().equals("Hormone")) {
 							//System.out.println(" CarcinogenExposure to hormoneList");
 							hormoneList.add(ce);
 						} // end of if
 						if (ef.getType().equals("Growth Factor")) {
 							//System.out.println("\tAdded CarcinogenExposure to growthFactorList");
 							growthFactorList.add(ce);
 						} // end of if
 						if (ef.getType().equals("Viral")) {
 							//System.out.println("\tAdded CarcinogenExposure to viraltreatmentList");
 							viraltreatmentList.add(ce);
 						} // end of if
 						if (ef.getType().equals("Chemical / Drug")) {
 							//System.out.println("\tAdded CarcinogenExposure to chemicaldrugList");
 							chemicaldrugList.add(ce);
 						} // end of if
 						if (ef.getType().equals("Environment")) {
 							//System.out.println("\tAdded CarcinogenExposure to environFactorList");
 							environFactorList.add(ce);
 						} // end of if
 
 						if (ef.getType().equals("Nutrition")) {
 							//System.out.println("\tAdded CarcinogenExposure to nutritionalFactorList");
 							nutritionalFactorList.add(ce);
 						} // end of if
 
 						if (ef.getType().equals("Radiation")) {
 							//System.out.println("\tAdded CarcinogenExposure to radiationList");
 							radiationList.add(ce);
 						} // end of if
 					}
 				}
 
 			} // end of else
 
 			/* CarcinogenExposure Lists */
 			request.getSession().setAttribute(
 					Constants.Submit.GROWTHFACTORS_LIST, growthFactorList);
 			request.getSession().setAttribute(Constants.Submit.HORMONE_LIST,
 					hormoneList);
 			request.getSession().setAttribute(
 					Constants.Submit.SURGERYOTHER_LIST, surgeryList);
 			request.getSession().setAttribute(
 					Constants.Submit.VIRALTREATMENT_LIST, viraltreatmentList);
 			request.getSession().setAttribute(
 					Constants.Submit.CHEMICALDRUG_LIST, chemicaldrugList);
 			request.getSession().setAttribute(
 					Constants.Submit.ENVIRONMENTALFACTOR_LIST,
 					environFactorList);
 			request.getSession().setAttribute(Constants.Submit.RADIATION_LIST,
 					radiationList);
 			request.getSession().setAttribute(
 					Constants.Submit.NUTRITIONALFACTORS_LIST,
 					nutritionalFactorList);
 			request.getSession().setAttribute(
 					Constants.Submit.GENEDELIVERY_LIST, geneList);
 			/* Publication List */
 			request.getSession().setAttribute(
 					Constants.Submit.PUBLICATION_LIST, pubList);
 
 			request.getSession().setAttribute(Constants.Submit.XENOGRAFT_LIST,
 					xenoList);
 
 			/* GeneticDescription Lists */
 			request.getSession().setAttribute(
 					Constants.Submit.SPONTANEOUSMUTATION_LIST, mutationList);
 			request.getSession().setAttribute(
 					Constants.Submit.INDUCEDMUTATION_LIST, inducedList);
 			request.getSession().setAttribute(
 					Constants.Submit.TARGETEDMODIFICATION_LIST, targetedList);
 			request.getSession().setAttribute(
 					Constants.Submit.GENOMICSEGMENT_LIST, segmentList);
 			request.getSession().setAttribute(
 					Constants.Submit.ENGINEEREDTRANSGENE_LIST, engineeredList);
 			request.getSession().setAttribute(
 					Constants.Submit.ASSOCIATEDEXPRESSION_LIST,
 					associatedExpressionList);
 
 			request.getSession().setAttribute(Constants.Submit.CELLLINE_LIST,
 					cellList);
 			request.getSession().setAttribute(Constants.Submit.THERAPY_LIST,
 					therapyList);
 			request.getSession().setAttribute(Constants.Submit.IMAGE_LIST,
 					imageCollection);
 
 			/* Animal Availability Lists */
 			request.getSession().setAttribute(
 					Constants.Submit.INVESTIGATOR_LIST, investigatorList);
 			request.getSession().setAttribute(Constants.Submit.JACKSONLAB_LIST,
 					jacksonLabList);
 			request.getSession().setAttribute(Constants.Submit.MMHCC_LIST,
 					mmhccList);
 			request.getSession().setAttribute(Constants.Submit.IMSR_LIST,
 					imsrList);
 			request.getSession().setAttribute(Constants.Submit.ZFIN_LIST,
 					zfinList);			
 
 			/* Histopathology Lists */
 			request.getSession().setAttribute(
 					Constants.Submit.HISTOPATHOLOGY_LIST, histopathList);
 			request.getSession().setAttribute(
 					Constants.Submit.ASSOCMETASTSIS_LIST,
 					associatedMetatasisList);
 			request.getSession().setAttribute(
 					Constants.Submit.CLINICALMARKER_LIST, clinicalMarkerList);
 
 			// Transient Interference Lists
 			request.getSession().setAttribute(Constants.Submit.MORPHOLINO_LIST,
 					morpholinoList);
 			request.getSession().setAttribute(Constants.Submit.SIRNA_LIST,
 					sirnaList);
 
 		} // end of try
 		catch (Exception e) {
 			log.error("Caught an exception populating the data: ", e);
 
 			// Encountered an error populating the data
 			ActionMessages theMsg = new ActionMessages();
 			theMsg.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
 					"errors.admin.message"));
 			saveErrors(request, theMsg);
 		} // end of catch
 
 		return mapping.findForward("next");
 	} // end of execute method
 }// end of class
