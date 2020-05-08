 /**
  *  @author sguruswami
  *  
 *  $Id: ViewModelAction.java,v 1.53 2008-01-31 22:23:22 pandyas Exp $
  *  
  *  $Log: not supported by cvs2svn $
  *  Revision 1.52  2008/01/31 17:09:54  pandyas
  *  Modified to send new gene identifier (entrez gene id) to caBIO from new object location
  *
  *  Revision 1.51  2008/01/28 18:45:18  pandyas
  *  Modified to debug caBIO data not returning to caMOD on dev
  *
  *  Revision 1.50  2008/01/16 20:09:31  pandyas
  *  removed caBIO logging so the page renders when connection to caBIO fails
  *
  *  Revision 1.49  2008/01/16 18:29:57  pandyas
  *  Renamed value to Transplant for #8290
  *
  *  Revision 1.48  2008/01/10 15:55:01  pandyas
  *  modify output for final dev deployment
  *
  *  Revision 1.47  2008/01/02 17:57:44  pandyas
  *  modified for #816  	Connection to caELMIR - retrieve data for therapy search page
  *
  *  Revision 1.46  2007/12/27 22:32:33  pandyas
  *  Modified  for feature #8816  	Connection to caELMIR - retrieve data for therapy search page
  *  Also added code to display Therapy link when only caELMIR data is available for a study
  *
  *  Revision 1.45  2007/12/27 21:44:00  pandyas
  *  re-commit - changes did not show up in project
  *
  *  Revision 1.44  2007/12/18 13:31:32  pandyas
  *  Added populate method for study data from caELMIRE for integration of Therapy study data
  *
  *  Revision 1.43  2007/12/17 18:03:22  pandyas
  *  Removed * in searchFilter used for getting e-mail from LDAP
  *  Apps Support ticket was submitted (31169 - incorrect e-mail associated with my caMOD account) stating:
  *
  *  Cheryl Marks submitted a ticket to NCICB Application Support in which she requested that the e-mail address associated with her account in the "User Settings" screen in caMOD be corrected. She has attempted to correct it herself, but because the program queries the LDAP Server for the e-mail address, her corrections were not retained.
  *
  *  Revision 1.42  2007/12/04 13:49:19  pandyas
  *  Modified code for #8816  	Connection to caELMIR - retrieve data for therapy search page
  *
  *  Revision 1.41  2007/11/25 23:34:23  pandyas
  *  Initial version for feature #8816  	Connection to caELMIR - retrieve data for therapy search page
  *
  *  Revision 1.40  2007/10/31 18:39:30  pandyas
  *  Fixed #8188 	Rename UnctrlVocab items to text entries
  *  Fixed #8290 	Rename graft object into transplant object
  *
  *  Revision 1.39  2007/09/14 18:53:37  pandyas
  *  Fixed Bug #8954:  link to invivo detail page does not work
  *
  *  Revision 1.38  2007/09/12 19:36:40  pandyas
  *  modified debug statements for build to stage tier
  *
  *  Revision 1.37  2007/08/07 19:49:46  pandyas
  *  Removed reference to Transplant as per VCDE comments and after modification to object definition for CDE
  *
  *  Revision 1.36  2007/08/07 18:26:20  pandyas
  *  Renamed to GRAFT as per VCDE comments
  *
  *  Revision 1.35  2007/07/31 12:02:55  pandyas
  *  VCDE silver level  and caMOD 2.3 changes
  *
  *  Revision 1.34  2007/06/19 20:42:59  pandyas
  *  Users not logged in can not access the session property to check the model species.  Therefore, we must show the attribute for all models.
  *
  *  Revision 1.33  2007/06/19 18:39:21  pandyas
  *  Constant for species common name needs to be set for viewModelCharacteristics so it shows up for Zebrafish models
  *
  *  Revision 1.32  2006/08/17 18:10:44  pandyas
  *  Defect# 410: Externalize properties files - Code changes to get properties
  *
  *  Revision 1.31  2006/05/24 18:37:27  georgeda
  *  Workaround for bug in caBIO
  *
  *  Revision 1.30  2006/05/09 18:57:54  georgeda
  *  Changes for searching on transient interfaces
  *
  *  Revision 1.29  2006/05/08 13:43:15  georgeda
  *  Reformat and clean up warnings
  *
  *  Revision 1.28  2006/04/19 19:31:58  georgeda
  *  Fixed display issue w/ GeneDelivery
  *
  *  Revision 1.27  2006/04/19 18:50:01  georgeda
  *  Fixed issue w/ engineered genes displaying
  *
  *  Revision 1.26  2006/04/17 19:09:41  pandyas
  *  caMod 2.1 OM changes
  *
  *  Revision 1.25  2005/11/21 18:38:31  georgeda
  *  Defect #35.  Trim whitespace from items that are freeform text
  *
  *  Revision 1.24  2005/11/15 22:13:46  georgeda
  *  Cleanup of drug screening
  *
  *  Revision 1.23  2005/11/14 14:21:44  georgeda
  *  Added sorting and spontaneous mutation
  *
  *  Revision 1.22  2005/11/11 18:39:30  georgeda
  *  Removed unneeded call
  *
  *  Revision 1.21  2005/11/10 22:07:36  georgeda
  *  Fixed part of bug #21
  *
  *  Revision 1.20  2005/11/10 18:12:23  georgeda
  *  Use constant
  *
  *  Revision 1.19  2005/11/07 13:57:39  georgeda
  *  Minor tweaks
  *
  *  Revision 1.18  2005/11/03 15:47:11  georgeda
  *  Fixed slow invivo results
  *
  *  Revision 1.17  2005/10/27 18:13:48  guruswas
  *  Show all publications in the publications display page.
  *
  *  Revision 1.16  2005/10/20 21:35:37  georgeda
  *  Fixed xenograft display bug
  *
  *  Revision 1.15  2005/10/19 18:56:00  guruswas
  *  implemented invivo details page
  *
  *  Revision 1.14  2005/10/11 18:15:25  georgeda
  *  More comment changes
  *
  *  Revision 1.13  2005/10/10 14:12:24  georgeda
  *  Changes for comment curation
  *
  *  Revision 1.12  2005/10/07 21:15:03  georgeda
  *  Added caarray variables
  *
  *  Revision 1.11  2005/10/06 13:37:01  georgeda
  *  Removed informational message
  *
  *  Revision 1.10  2005/09/30 18:42:24  guruswas
  *  intial implementation of drug screening search and display page
  *
  *  Revision 1.9  2005/09/22 21:34:51  guruswas
  *  First stab at carcinogenic intervention pages
  *
  *  Revision 1.8  2005/09/22 15:23:41  georgeda
  *  Cleaned up warnings
  *
  *  Revision 1.7  2005/09/21 21:02:24  guruswas
  *  Display the organ, disease names from NCI Thesaurus
  *
  *  Revision 1.6  2005/09/21 20:47:16  georgeda
  *  Cleaned up
  *
  *  Revision 1.5  2005/09/16 19:30:00  guruswas
  *  Display invivo data (from DTP) in the therapuetic approaches page
  *
  *  Revision 1.4  2005/09/16 15:52:56  georgeda
  *  Changes due to manager re-write
  *
  *  
  */
 package gov.nih.nci.camod.webapp.action;
 
 import edu.wustl.common.util.CaElmirInterfaceManager;
 import gov.nih.nci.cabio.domain.Gene;
 import gov.nih.nci.cabio.domain.impl.GeneImpl;
 import gov.nih.nci.camod.Constants;
 import gov.nih.nci.camod.domain.Agent;
 import gov.nih.nci.camod.domain.AnimalModel;
 import gov.nih.nci.camod.domain.CaelmirStudyData;
 import gov.nih.nci.camod.domain.CarcinogenExposure;
 import gov.nih.nci.camod.domain.Comments;
 import gov.nih.nci.camod.domain.EngineeredGene;
 import gov.nih.nci.camod.domain.GeneIdentifier;
 import gov.nih.nci.camod.domain.GenomicSegment;
 import gov.nih.nci.camod.domain.Transplant;
 import gov.nih.nci.camod.domain.InducedMutation;
 import gov.nih.nci.camod.domain.Person;
 import gov.nih.nci.camod.domain.SpontaneousMutation;
 import gov.nih.nci.camod.domain.TargetedModification;
 import gov.nih.nci.camod.domain.Therapy;
 import gov.nih.nci.camod.domain.Transgene;
 import gov.nih.nci.camod.service.AgentManager;
 import gov.nih.nci.camod.service.AnimalModelManager;
 import gov.nih.nci.camod.service.CommentsManager;
 import gov.nih.nci.camod.service.PersonManager;
 import gov.nih.nci.camod.service.TransplantManager;
 import gov.nih.nci.camod.service.impl.QueryManagerSingleton;
 import gov.nih.nci.camod.util.EvsTreeUtil;
 import gov.nih.nci.common.domain.DatabaseCrossReference;
 import gov.nih.nci.common.domain.impl.DatabaseCrossReferenceImpl;
 import gov.nih.nci.system.applicationservice.ApplicationService;
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import java.util.Vector;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import net.sf.json.JSONArray;
 import net.sf.json.JSONObject;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 
 public class ViewModelAction extends BaseAction
 {
 
     /**
      * sets the cancer model object in the session
      * 
      * @param request
      *            the httpRequest
      */
     private void setCancerModel(HttpServletRequest request)
     {
         String modelID = request.getParameter(Constants.Parameters.MODELID);
         log.debug("<setCancerModel> modelID: " + modelID);
         AnimalModelManager animalModelManager = (AnimalModelManager) getBean("animalModelManager");
         AnimalModel am = null;
         try
         {
             am = animalModelManager.get(modelID);
         }
         catch (Exception e)
         {
             log.error("Unable to get cancer model in setCancerModel");
             e.printStackTrace();
         }
         request.getSession().setAttribute(Constants.ANIMALMODEL, am);
         // Set model id to display on subViewModelMenu on left menu bar
         request.getSession().setAttribute(Constants.MODELID, am.getId().toString());        
     }
 
     /**
      * sets the cancer model object in the session
      * 
      * @param request
      *            the httpRequest
      * @throws Exception
      */
     private void setComments(HttpServletRequest request,
                              String inSection) throws Exception
     {
 
         String theCommentsId = request.getParameter(Constants.Parameters.COMMENTSID);
 
         CommentsManager theCommentsManager = (CommentsManager) getBean("commentsManager");
 
         log.debug("Comments id: " + theCommentsId);
         List<Comments> theCommentsList = new ArrayList<Comments>();
         if (theCommentsId != null && theCommentsId.length() > 0)
         {
             Comments theComments = theCommentsManager.get(theCommentsId);
             if (theComments != null)
             {
                 System.out.println("Found a comment: " + theComments.getRemark());
                 theCommentsList.add(theComments);
             }
         }
 
         // Get all comments that are either approved or owned by this user
         else
         {
             PersonManager thePersonManager = (PersonManager) getBean("personManager");
             Person theCurrentUser = thePersonManager.getByUsername((String) request.getSession().getAttribute(Constants.CURRENTUSER));
 
             AnimalModel theAnimalModel = (AnimalModel) request.getSession().getAttribute(Constants.ANIMALMODEL);
 
             theCommentsList = theCommentsManager.getAllBySection(inSection, theCurrentUser, theAnimalModel);
         }
 
         request.setAttribute(Constants.Parameters.COMMENTSLIST, theCommentsList);
     }
 
     /**
      * @param mapping
      * @param form
      * @param request
      * @param response
      * @return
      * @throws Exception
      */
     public ActionForward populateModelCharacteristics(ActionMapping mapping,
                                                       ActionForm form,
                                                       HttpServletRequest request,
                                                       HttpServletResponse response) throws Exception
     {
         setCancerModel(request);
         setComments(request, Constants.Pages.MODEL_CHARACTERISTICS);
         
         // Call method so therapy link displays for models with caELMIR-only data
         populateCaelmirTherapyDetails(mapping, form, request, response);
         
         return mapping.findForward("viewModelCharacteristics");
     }
 
     /**
      * 
      */
     public ActionForward populateEngineeredGene(ActionMapping mapping,
                                                 ActionForm form,
                                                 HttpServletRequest request,
                                                 HttpServletResponse response) throws Exception
     {
         log.debug("<populateEngineeredGene> modelID" + request.getParameter("aModelID"));
         String modelID = request.getParameter("aModelID");
 
         AnimalModelManager animalModelManager = (AnimalModelManager) getBean("animalModelManager");
         AnimalModel am = animalModelManager.get(modelID);
 
         final Set egc = am.getEngineeredGeneCollection();
         final int egcCnt = (egc != null) ? egc.size() : 0;
         final List<EngineeredGene> tgc = new ArrayList<EngineeredGene>();
         int tgCnt = 0;// Transgene
         final List<EngineeredGene> gsc = new ArrayList<EngineeredGene>();
         int gsCnt = 0;// GenomicSegment
         final List<EngineeredGene> tmc = new ArrayList<EngineeredGene>();
         int tmCnt = 0;// TargetedModification
         final Map<Long, Gene> tmGeneMap = new HashMap<Long, Gene>();
         final List<EngineeredGene> imc = new ArrayList<EngineeredGene>();
         final List<SpontaneousMutation> smc = new ArrayList<SpontaneousMutation>(am.getSpontaneousMutationCollection());
         Iterator it = egc.iterator();
         int imCnt = 0;// InducedMutation
         while (it.hasNext())
         {
             EngineeredGene eg = (EngineeredGene) it.next();
             if (eg instanceof Transgene)
             {
                 tgc.add(eg);
                 tgCnt++;
             }
             else if (eg instanceof GenomicSegment)
             {
                 gsc.add(eg);
                 gsCnt++;
             }
             else if (eg instanceof TargetedModification)
             {
                 tmc.add(eg);
                 tmCnt++;
                 // now go to caBIO and query the gene object....
                 TargetedModification tm = (TargetedModification) eg;
                 GeneIdentifier geneIdentifier = tm.getGeneIdentifier();
                 log.debug("geneIdentifier.getEntrezGeneID() " + geneIdentifier.getEntrezGeneID());
                 if (geneIdentifier != null)
                 {
                     log.debug("Connecting to caBIO to look up gene " + geneIdentifier);
                     // the geneId is available
                     try
                     {
                       ApplicationService appService = EvsTreeUtil.getApplicationService();
                         DatabaseCrossReference dcr = new DatabaseCrossReferenceImpl(); 
                         dcr.setCrossReferenceId(geneIdentifier.getEntrezGeneID());
 
                         dcr.setType("gov.nih.nci.cabio.domain.Gene");
                         dcr.setDataSourceName("LOCUS_LINK_ID");
                         List<DatabaseCrossReference> cfcoll = new ArrayList<DatabaseCrossReference>();
                         cfcoll.add(dcr);
 
                         Gene myGene = new GeneImpl();
                         myGene.setDatabaseCrossReferenceCollection(cfcoll);
                         List resultList = appService.search(Gene.class, myGene);
 
                         
                         final int geneCount = (resultList != null) ? resultList.size() : 0;
                         log.debug("Got " + geneCount + " Gene Objects");
                         if (geneCount > 0)
                         {
                             myGene = (Gene) resultList.get(0);
                             //log.info("Gene:" + geneIdentifier + " ==>" + myGene);
                             tmGeneMap.put(tm.getId(), myGene);
                         }
                     }
                     catch (Exception e)
                     {
                         log.error("Unable to get information from caBIO", e);
                     }
                 }
             }
             else if (eg instanceof InducedMutation)
             {
                 imc.add(eg);
                 imCnt++;
             }
         }
 
         log.debug("<populateEngineeredGene> " + "egcCnt=" + egcCnt + "tgc=" + tgCnt + "gsc=" + gsCnt + "tmc=" + tmCnt + "imc=" + imCnt);
         request.getSession().setAttribute(Constants.ANIMALMODEL, am);
         request.getSession().setAttribute(Constants.TRANSGENE_COLL, tgc);
         request.getSession().setAttribute(Constants.GENOMIC_SEG_COLL, gsc);
         request.getSession().setAttribute(Constants.TARGETED_MOD_COLL, tmc);
         request.getSession().setAttribute(Constants.TARGETED_MOD_GENE_MAP, tmGeneMap);
         request.getSession().setAttribute(Constants.INDUCED_MUT_COLL, imc);
         request.getSession().setAttribute(Constants.SPONTANEOUS_MUT_COLL, smc);
        System.out.println("<populateEngineeredGene> set attributes done.");
 
         setComments(request, Constants.Pages.GENETIC_DESCRIPTION);
 
         return mapping.findForward("viewGeneticDescription");
     }
 
     /**
      * Populate the session and/or request with the objects necessary to display
      * the page.
      * 
      * @param mapping
      *            the struts action mapping
      * @param form
      *            the web form
      * @param request
      *            HTTPRequest
      * @param response
      *            HTTPResponse
      * @return
      * @throws Exception
      */
     public ActionForward populateCarcinogenicInterventions(ActionMapping mapping,
                                                            ActionForm form,
                                                            HttpServletRequest request,
                                                            HttpServletResponse response) throws Exception
     {
         setCancerModel(request);
         String modelID = request.getParameter(Constants.Parameters.MODELID);
         AnimalModelManager animalModelManager = (AnimalModelManager) getBean("animalModelManager");
         AnimalModel am = animalModelManager.get(modelID);
 
         final Set ceColl = am.getCarcinogenExposureCollection();
         Iterator it = ceColl.iterator();
         final Map<String, List<Object>> interventionTypeMap = new HashMap<String, List<Object>>();
 
         while (it.hasNext())
         {
             CarcinogenExposure ce = (CarcinogenExposure) it.next();
             if (ce != null)
             {
                 log.debug("Checking agent:" + ce.getEnvironmentalFactor().getNscNumber());
                 String theType = ce.getEnvironmentalFactor().getType();
                 if (theType == null || theType.length() == 0)
                 {
                     theType = ce.getEnvironmentalFactor().getTypeAlternEntry();
                     if (theType == null || theType.length() == 0)
                     {
                         theType = "Not specified";
                     }
                 }
                 List<Object> theTypeColl = interventionTypeMap.get(theType);
                 if (theTypeColl == null)
                 {
                     theTypeColl = new ArrayList<Object>();
                     interventionTypeMap.put(theType, theTypeColl);
                 }
                 theTypeColl.add(ce);
             }
         }
 
         if (am.getGeneDeliveryCollection().size() > 0)
         {
             List<Object> theGeneDeliveryCollection = new ArrayList<Object>(am.getGeneDeliveryCollection());
             interventionTypeMap.put("GeneDelivery", theGeneDeliveryCollection);
         }
 
         request.getSession().setAttribute(Constants.CARCINOGENIC_INTERVENTIONS_COLL, interventionTypeMap);
 
         setComments(request, Constants.Pages.CARCINOGENIC_INTERVENTION);
 
         return mapping.findForward("viewCarcinogenicInterventions");
     }
 
 
     /**
      * Populate the session and/or request with the objects necessary to display
      * the page.
      * 
      * @param mapping
      *            the struts action mapping
      * @param form
      *            the web form
      * @param request
      *            HTTPRequest
      * @param response
      *            HTTPResponse
      * @return
      * @throws Exception
      */
     public ActionForward populatePublications(ActionMapping mapping,
                                               ActionForm form,
                                               HttpServletRequest request,
                                               HttpServletResponse response) throws Exception
     {
         setCancerModel(request);
         String modelID = request.getParameter("aModelID");
         List pubs = null;
         try
         {
             pubs = QueryManagerSingleton.instance().getAllPublications(Long.valueOf(modelID).longValue());
             log.debug("pubs.size(): " + pubs.size());
         }
         catch (Exception e)
         {
             log.error("Unable to get publications");
             e.printStackTrace();
         }
         request.getSession().setAttribute(Constants.PUBLICATIONS, pubs);
         setComments(request, Constants.Pages.PUBLICATIONS);
         return mapping.findForward("viewPublications");
     }
 
     /**
      * Populate the session and/or request with the objects necessary to display
      * the page.
      * 
      * @param mapping
      *            the struts action mapping
      * @param form
      *            the web form
      * @param request
      *            HTTPRequest
      * @param response
      *            HTTPResponse
      * @return
      * @throws Exception
      */
     public ActionForward populateHistopathology(ActionMapping mapping,
                                                 ActionForm form,
                                                 HttpServletRequest request,
                                                 HttpServletResponse response) throws Exception
     {
         setCancerModel(request);
         setComments(request, Constants.Pages.HISTOPATHOLOGY);
 
         return mapping.findForward("viewHistopathology");
     }
 
     /**
      * Populate the session and/or request with the objects necessary to display
      * the page.
      * 
      * @param mapping
      *            the struts action mapping
      * @param form
      *            the web form
      * @param request
      *            HTTPRequest
      * @param response
      *            HTTPResponse
      * @return
      * @throws Exception
      */
     public ActionForward populateTherapeuticApproaches(ActionMapping mapping,
                                                        ActionForm form,
                                                        HttpServletRequest request,
                                                        HttpServletResponse response) throws Exception
     {
         log.debug("<ViewModelAction>  populateTherapeuticApproaches");
         
         setCancerModel(request);
         //
         // query caBIO and load clinical protocols information
         // store clinicalProtocol info in a hashmap keyed by NSC#
         //
         final HashMap<Long, Collection> clinProtocols = new HashMap<Long, Collection>();
         final HashMap<Long, Collection> yeastResults = new HashMap<Long, Collection>();
         final HashMap<Long, Collection> invivoResults = new HashMap<Long, Collection>();
         final List<Therapy> therapeuticApprochesColl = new ArrayList<Therapy>();
 
         String modelID = request.getParameter(Constants.Parameters.MODELID);
         AnimalModelManager animalModelManager = (AnimalModelManager) getBean("animalModelManager");
         AnimalModel am = animalModelManager.get(modelID);
         final Set therapyColl = am.getTherapyCollection();
         Iterator it = therapyColl.iterator();
 
         final int cc = (therapyColl != null) ? therapyColl.size() : 0;
         log.debug("Looking up clinical protocols for " + cc + " agents...");
 
         while (it.hasNext())
         {
             Therapy t = (Therapy) it.next();
             if (t != null)
             {
                 therapeuticApprochesColl.add(t);
             }
             Agent a = t.getAgent();
             AgentManager myAgentManager = (AgentManager) getBean("agentManager");
             if (a != null)
             {
                 Long nscNumber = a.getNscNumber();
                 if (nscNumber != null)
                 {
                     Collection protocols = myAgentManager.getClinicalProtocols(a);
                     clinProtocols.put(nscNumber, protocols);
                     
                     // get the yeast data
                     List yeastStages = myAgentManager.getYeastResults(a, true);
                     if (yeastStages.size() > 0)
                     {
                         yeastResults.put(a.getId(), yeastStages);
                     }
                     // now get invivo/Transplant data
                     List transplantResults = QueryManagerSingleton.instance().getInvivoResults(a, true);
                     invivoResults.put(a.getId(), transplantResults);
                 }
             }
         }
 
         request.getSession().setAttribute(Constants.THERAPEUTIC_APPROACHES_COLL, therapeuticApprochesColl);
         request.getSession().setAttribute(Constants.CLINICAL_PROTOCOLS, clinProtocols);
         request.getSession().setAttribute(Constants.YEAST_DATA, yeastResults);
         request.getSession().setAttribute(Constants.INVIVO_DATA, invivoResults);
 
         setComments(request, Constants.Pages.THERAPEUTIC_APPROACHES);
         
         populateCaelmirTherapyDetails(mapping, form, request, response);
 
         return mapping.findForward("viewTherapeuticApproaches");
     }
 
 	/**
 	 * Populate the session and/or request with the objects necessary to display
 	 * the page.
 	 * 
 	 * @param mapping
 	 *            the struts action mapping
 	 * @param form
 	 *            the web form
 	 * @param request
 	 *            HTTPRequest
 	 * @param response
 	 *            HTTPResponse
 	 * @return
 	 * @throws Exception
 	 */
 	public ActionForward populateCaelmirTherapyDetails(ActionMapping mapping,
 			ActionForm form, HttpServletRequest request,
 			HttpServletResponse response) throws Exception {
 		//log.debug<"<ViewModelAction>  populateCaelmirTherapyDetails Enter");
 
 		setCancerModel(request);
 		JSONArray jsonArray = new JSONArray();
 		JSONObject jobj = new JSONObject();
 		Vector h = new Vector();
 		ArrayList caelmirStudyData = new ArrayList();
 
 		String modelID = request.getParameter(Constants.Parameters.MODELID);
         AnimalModelManager theAnimalModelManager = (AnimalModelManager) getBean("animalModelManager");
         AnimalModel theAnimalModel = theAnimalModelManager.get(modelID);  
 
 		try {
 			log.debug("<ViewModelAction>  populateCaelmirTherapyDetails Enter try");
 			// Link to the inteface provided by caElmir
 			URL url = new URL("http://chichen-itza.compmed.ucdavis.edu:8080/"
 					+ CaElmirInterfaceManager.getStudyInfoUrl());
 			// set your proxy server and port
 			//System.setProperty("proxyHost", "ptproxy.persistent.co.in");
 			//System.setProperty("proxyPort", "8080");
 
 			URLConnection urlConnection = url.openConnection();
 			//log.debug("populateCaelmirTherapyDetails open connection");
 			// needs to be set to True for writing to the output stream.This
 			// allows to pass data to the url.
 			urlConnection.setDoOutput(true);
 
 			JSONObject jsonObj = new JSONObject();
 			// setting the model id.
 			jsonObj.put(CaElmirInterfaceManager.getModelIdParameter(), modelID);
 			PrintWriter out = new PrintWriter(urlConnection.getOutputStream());
 			out.write(jsonObj.toString());
 			out.flush();
 			out.close();
 			//log.debug("populateCaelmirTherapyDetails created JSONObject");
 
 			// start reading the responce
 			BufferedReader bufferedReader = new BufferedReader(
 					new InputStreamReader(urlConnection.getInputStream()));
 			if (bufferedReader != null) {
 				String resultStr = (String) bufferedReader.readLine();
 				jsonArray = new JSONArray(resultStr);
 				String status = null;
 				status = ((JSONObject) jsonArray.get(0)).get(
 						CaElmirInterfaceManager.getStatusMessageKey())
 						.toString();
 				//log.debug("populateCaelmirTherapyDetails status: " + status);
 				// Imporatant:first check for the status
 				if (!CaElmirInterfaceManager.getSuccessKey().equals(status)) {
 					// prints the error message and return
 					System.out.println(status);
 					// return;
 				}
 
                 CaelmirStudyData studyData = new CaelmirStudyData();
                 
 				// start reading study data from index 1
 				for (int i = 1; i < jsonArray.length(); i++) {
 					jobj = (JSONObject) jsonArray.get(i);
                     
                     studyData = new CaelmirStudyData();
 					studyData.setDescription(jobj.getString(CaElmirInterfaceManager.getStudyDesrciptionKey()));
 					studyData.setEmail(jobj.getString(CaElmirInterfaceManager.getEmailKey()));
 					studyData.setHypothesis(jobj.getString(CaElmirInterfaceManager.getStudyHypothesisKey()));
 					studyData.setInstitution(jobj.getString(CaElmirInterfaceManager.getInstitutionKey()));
 					studyData.setInvestigatorName(jobj.getString(CaElmirInterfaceManager.getPrimaryInvestigatorKey()));
 					studyData.setStudyName(jobj.getString(CaElmirInterfaceManager.getStudyName()));
 					studyData.setUrl(jobj.getString(CaElmirInterfaceManager.getStudyUrlKey()));
 
                     caelmirStudyData.add(studyData);
                     //log.debug("studyData.toString(): " + studyData.toString());                
 				}    
 			}
 		} catch (MalformedURLException me) {
 			System.out.println("MalformedURLException: " + me);
 		} catch (IOException ioe) {
 			System.out.println("IOException: " + ioe);
 		}
 		
 		// Set collection so therapy link will display if caELMIR data is available
         // Needed for models with caELMIR data but no caMOD data 
 		theAnimalModel.setCaelmirStudyDataCollection(caelmirStudyData);
 		
 		request.getSession().setAttribute(Constants.CAELMIR_STUDY_DATA,
 				caelmirStudyData);
         
          //log.debug("caelmirStudyData.toString(): " + caelmirStudyData.toString());
 
 		return mapping.findForward("viewTherapeuticApproaches");
 	}
     
     /**
      * Populate the session and/or request with the objects necessary to display
      * the page.
      * 
      * @param mapping
      *            the struts action mapping
      * @param form
      *            the web form
      * @param request
      *            HTTPRequest
      * @param response
      *            HTTPResponse
      * @return
      * @throws Exception
      */
     public ActionForward populateCellLines(ActionMapping mapping,
                                            ActionForm form,
                                            HttpServletRequest request,
                                            HttpServletResponse response) throws Exception
     {
         setCancerModel(request);
         setComments(request, Constants.Pages.CELL_LINES);
 
         return mapping.findForward("viewCellLines");
     }
 
     /**
      * Populate the session and/or request with the objects necessary to display
      * the page.
      * 
      * @param mapping
      *            the struts action mapping
      * @param form
      *            the web form
      * @param request
      *            HTTPRequest
      * @param response
      *            HTTPResponse
      * @return
      * @throws Exception
      */
     public ActionForward populateTransientInterference(ActionMapping mapping,
                                                        ActionForm form,
                                                        HttpServletRequest request,
                                                        HttpServletResponse response) throws Exception
     {
         setCancerModel(request);
         setComments(request, Constants.Pages.TRANSIENT_INTERFERENCE);
 
         return mapping.findForward("viewTransientInterference");
     }
 
 
     /**
      * Populate the session and/or request with the objects necessary to display
      * the page.
      * 
      * @param mapping
      *            the struts action mapping
      * @param form
      *            the web form
      * @param request
      *            HTTPRequest
      * @param response
      *            HTTPResponse
      * @return
      * @throws Exception
      */
     public ActionForward populateImages(ActionMapping mapping,
                                         ActionForm form,
                                         HttpServletRequest request,
                                         HttpServletResponse response) throws Exception
     {
         setCancerModel(request);
         setComments(request, Constants.Pages.IMAGES);
 
         return mapping.findForward("viewImages");
     }
 
     /**
      * Populate the session and/or request with the objects necessary to display
      * the page.
      * 
      * @param mapping
      *            the struts action mapping
      * @param form
      *            the web form
      * @param request
      *            HTTPRequest
      * @param response
      *            HTTPResponse
      * @return
      * @throws Exception
      */
     public ActionForward populateMicroarrays(ActionMapping mapping,
                                              ActionForm form,
                                              HttpServletRequest request,
                                              HttpServletResponse response) throws Exception
     {
         setCancerModel(request);
         //Get external properties file
 		Properties camodProperties = new Properties();
 		String camodPropertiesFileName = null;
 
 		camodPropertiesFileName = System.getProperty("gov.nih.nci.camod.camodProperties");
 		
 		try {
 		
 		FileInputStream in = new FileInputStream(camodPropertiesFileName);
 		camodProperties.load(in);
 	
 		} 
 		catch (FileNotFoundException e) {
 			log.error("Caught exception finding file for properties: ", e);
 			e.printStackTrace();			
 		} catch (IOException e) {
 			log.error("Caught exception finding file for properties: ", e);
 			e.printStackTrace();			
 		}
 
         request.setAttribute("uri_start", camodProperties.getProperty("caarray.uri_start"));
         request.setAttribute("uri_end", camodProperties.getProperty("caarray.uri_end"));
 
         setComments(request, Constants.Pages.MICROARRAY);
 
         return mapping.findForward("viewMicroarrays");
     }
 
     /**
      * Populate the session and/or request with the objects necessary to display
      * the page.
      * 
      * @param mapping
      *            the struts action mapping
      * @param form
      *            the web form
      * @param request
      *            HTTPRequest
      * @param response
      *            HTTPResponse
      * @return
      * @throws Exception
      */
     public ActionForward populateTransplant(ActionMapping mapping,
                                                      ActionForm form,
                                                      HttpServletRequest request,
                                                      HttpServletResponse response) throws Exception
     {
         log.debug("<populateTransplant> Enter:");    	
         setCancerModel(request);
         setComments(request, Constants.Pages.TRANSPLANT);
         log.debug("<populateTransplant> Exit:"); 
         return mapping.findForward("viewTransplant");
     }
 
     /**
      * Populate the session and/or request with the objects necessary to display
      * the page.
      * 
      * @param mapping
      *            the struts action mapping
      * @param form
      *            the web form
      * @param request
      *            HTTPRequest
      * @param response
      *            HTTPResponse
      * @return
      * @throws Exception
      */
     public ActionForward populateTransplantDetails(ActionMapping mapping,
                                                   ActionForm form,
                                                   HttpServletRequest request,
                                                   HttpServletResponse response) throws Exception
     {
         log.debug("<populateTransplantDetails> Enter:");    	
         String modelID = request.getParameter("tModelID");
         request.getSession().setAttribute(Constants.MODELID, modelID);
         String nsc = request.getParameter("nsc");
         if (nsc != null && nsc.length() == 0)
             return mapping.findForward("viewModelCharacteristics");
         log.debug("<populateTransplantDetails> modelID:" + modelID);
         log.debug("<populateTransplantDetails> nsc:" + nsc);
         TransplantManager mgr = (TransplantManager) getBean("transplantManager");
 
         Transplant t = mgr.get(modelID);
 
         request.getSession().setAttribute(Constants.TRANSPLANTMODEL, t);
         request.getSession().setAttribute(Constants.NSC_NUMBER, nsc);
         request.getSession().setAttribute(Constants.TRANSPLANTRESULTLIST, t.getInvivoResultCollectionByNSC(nsc));
         return mapping.findForward("viewInvivoDetails");
     }
 }
