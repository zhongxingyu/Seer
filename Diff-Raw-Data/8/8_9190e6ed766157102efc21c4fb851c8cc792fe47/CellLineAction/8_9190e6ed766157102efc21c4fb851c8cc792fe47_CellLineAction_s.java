 package gov.nih.nci.camod.webapp.action;
 
 import gov.nih.nci.camod.Constants;
 import gov.nih.nci.camod.domain.*;
 import gov.nih.nci.camod.service.AnimalModelManager;
import gov.nih.nci.camod.service.GeneDeliveryManager;
 import gov.nih.nci.camod.webapp.form.CellLineForm;
 
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.*;
 
 /**
  * CellLineAction Class
  */
 public final class CellLineAction extends BaseAction {
 	
     /**
      * Delete
      * @param mapping
      * @param form
      * @param request
      * @param response
      * @return
      * @throws Exception
      */
     public ActionForward delete(ActionMapping mapping, ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
     throws Exception {
         if (log.isDebugEnabled()) {
             log.debug("Entering 'delete' method");
         }
 
         return mapping.findForward("");
     }
 
 	/**
 	 * Cancel
 	 * @param mapping
 	 * @param form
 	 * @param request
 	 * @param response
 	 * @return
 	 * @throws Exception
 	 */
     public ActionForward duplicate(ActionMapping mapping, ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
     throws Exception {
     	
     	 return mapping.findForward("");
     }    
     
     
     /**
      * Edit
      * @param mapping
      * @param form
      * @param request
      * @param response
      * @return
      * @throws Exception
      */
     public ActionForward edit(ActionMapping mapping, ActionForm form,
                               HttpServletRequest request,
                               HttpServletResponse response)
     throws Exception {
         if (log.isDebugEnabled()) {
             log.debug("Entering 'edit' method");
         }
         
         CellLineForm cellLineForm = ( CellLineForm ) form;
 
     	// Grab the current CellLine we are working with related to this animalModel
     	String aCellID = request.getParameter( "aCellID" );
     	
 		System.out.println( "<EnvironmentalFactorAction save> following Characteristics:" + 
 								"\n\t name: " + cellLineForm.getCellLineName() + 
 								"\n\t otherName: " + cellLineForm.getOrganName() + 
 								"\n\t dosage: " + cellLineForm.getExperiment() + 
 								"\n\t administrativeRoute: " + cellLineForm.getResults() +
 								"\n\t regimen: " + cellLineForm.getComments() +
 								"\n\t user: " + (String) request.getSession().getAttribute( "camod.loggedon.username" ) );
 		
 		/* Grab the current modelID from the session */
         String modelID = (String) request.getSession().getAttribute( Constants.MODELID );
         
 		// Use the current animalModel based on the ID stored in the session
         AnimalModelManager animalModelManager = (AnimalModelManager) getBean( "animalModelManager" );
 		AnimalModel am = animalModelManager.get( modelID );
 		
 		//retrieve the list of all Cell Lines from the current animalModel		
 		List cellList = am.getCellLineCollection();
 		
 		//Create a CellLine object
 		CellLine cell = new CellLine();
 		
 		//find the specific cell line that we need
 		for ( int i=0; i<cellList.size(); i++ ) {
 			cell = (CellLine)cellList.get(i);
 			if ( cell.getId().toString().equals( aCellID ) )
 				break;
 		}	
 		
 		/*  Set the attributes in the CellLine form */
 		cell.setName( cellLineForm.getCellLineName() );
 		cell.setExperiment( cellLineForm.getExperiment() );
 		cell.setResults( cellLineForm.getResults() );
 		cell.setComments( cellLineForm.getComments() );	
 
 		//Create Organ object and set name attribute in form
         Organ organ = new Organ();
         organ.setName( cellLineForm.getOrganName() );		
 		
 
     	//Add a message to be displayed in submitOverview.jsp saying you've created a new model successfully 
         ActionMessages msg = new ActionMessages();
         msg.add( ActionMessages.GLOBAL_MESSAGE, new ActionMessage( "cellline.edit.successful" ) );
         saveErrors( request, msg );
         
         return mapping.findForward("AnimalModelTreePopulateAction");
     }
 
     /**
      * Save
      * @param mapping
      * @param form
      * @param request
      * @param response
      * @return
      * @throws Exception
      */
     public ActionForward save(ActionMapping mapping, ActionForm form,
                               HttpServletRequest request,
                               HttpServletResponse response)
     throws Exception {
         if (log.isDebugEnabled()) {
             log.debug("Entering 'save' method");
         }
         
         CellLineForm cellLineForm = ( CellLineForm ) form;
 
 		System.out.println( "<EnvironmentalFactorAction save> following Characteristics:" + 
 								"\n\t name: " + cellLineForm.getCellLineName() + 
 								"\n\t otherName: " + cellLineForm.getOrganName() + 
 								"\n\t dosage: " + cellLineForm.getExperiment() + 
 								"\n\t administrativeRoute: " + cellLineForm.getResults() +
 								"\n\t regimen: " + cellLineForm.getComments() +
 								"\n\t user: " + (String) request.getSession().getAttribute( "camod.loggedon.username" ) );
 		
 		/* Grab the current modelID from the session */
         String modelID = (String) request.getSession().getAttribute( Constants.MODELID );
         
         /* Create all the manager objects needed for Screen */
         AnimalModelManager animalModelManager = (AnimalModelManager) getBean( "animalModelManager" );
         
         //TODO: use managers to implement evs tree (organ code)
         //CellLineManager cellLineManager = (CellLineManager) getBean( "cellLineManager" );
         //OrganManager organManager = (OrganManager) getBean( "organManager" );
        GeneDeliveryManager geneDeliveryManager = (GeneDeliveryManager)getBean( "geneDeliveryManager" ); 
         
         /* Set modelID in AnimalModel object */
         AnimalModel animalModel = animalModelManager.get( modelID );         
         
         //Create CellLine, add all necessary attributes 
         //The CellLine object added to AnimalModel before saving below
         CellLine cellLine = new CellLine();
         cellLine.setName(cellLineForm.getCellLineName());
         cellLine.setExperiment(cellLineForm.getExperiment());
         cellLine.setResults(cellLineForm.getResults());
         cellLine.setComments(cellLineForm.getComments());
         //cellLineManager.save(cellLine);
 
         
         //Create Organ, add organName attribute
         Organ organ = new Organ();
         organ.setName(cellLineForm.getOrganName());
         
         //Create GeneDelivery object, set its organName, and save GeneDelivery object
         GeneDelivery geneDelivery = new GeneDelivery();
         geneDelivery.setOrgan(organ);
        geneDeliveryManager.save(geneDelivery);
         
 		System.out.println( "<CellLineAction save> Created and saved GeneDelivery object");
 		
 		//add CellLine to AnimalModel
 		animalModel.addCellLine(cellLine);
 		
         //Add GeneDelivery to animalModel
 		animalModel.addGeneDelivery(geneDelivery);		
 		
         //Persist all changes to the db for CellLine and Organ/GeneDelivey
         animalModelManager.save( animalModel );
         
         System.out.println( "<CellLineAction save> Persisted all changes to the db");        
 		
     	//Add a message to be displayed in submitOverview.jsp saying you've created a new model successfully 
         ActionMessages msg = new ActionMessages();
         msg.add( ActionMessages.GLOBAL_MESSAGE, new ActionMessage( "cellLine.creation.successful" ) );
         saveErrors( request, msg );	
         
         return mapping.findForward("AnimalModelTreePopulateAction");
     }
 }
