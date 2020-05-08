 package gov.nih.nci.camod.webapp.action;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.action.ActionMessage;
 import org.apache.struts.action.ActionMessages;
 import java.util.List;
 import gov.nih.nci.camod.Constants;
 import gov.nih.nci.camod.domain.Agent;
 import gov.nih.nci.camod.webapp.form.EnvironmentalFactorForm;
 import gov.nih.nci.camod.domain.Therapy;
 import gov.nih.nci.camod.domain.Treatment;
 import gov.nih.nci.camod.domain.SexDistribution;
 import gov.nih.nci.camod.service.AgentManager;
 import gov.nih.nci.camod.service.AnimalModelManager;
 import gov.nih.nci.camod.service.SexDistributionManager;
 import gov.nih.nci.camod.service.TherapyManager;
 import gov.nih.nci.camod.service.TreatmentManager;
 import gov.nih.nci.camod.domain.AnimalModel;
 
 
 
 /**
  * EnvironmentalFactorAction Class
  */
 public final class EnvironmentalFactorAction extends BaseAction {
 
 	/** Called from submitEnvironmentalFactors.jsp
 	 * 
 	 */ 
     public ActionForward save( ActionMapping mapping, 
     								   ActionForm form,
 							           HttpServletRequest request,
 							           HttpServletResponse response)
     throws Exception {
         if (log.isDebugEnabled()) {
             log.debug("Entering 'save' method");
         }    	
     	
     	EnvironmentalFactorForm envForm = ( EnvironmentalFactorForm ) form;
     	
 		System.out.println( "<EnvironmentalFactorAction save> following Characteristics:" + 
 																"\n\t name: " + envForm.getName() + 
 																"\n\t otherName: " + envForm.getOtherName() + 
 																"\n\t dosage: " + envForm.getDosage() + 
 																"\n\t administrativeRoute: " + envForm.getAdministrativeRoute() +
 																"\n\t regimen: " + envForm.getRegimen() +
 																"\n\t ageAtTreatment: " + envForm.getAgeAtTreatment() +
 																"\n\t type: " + envForm.getType() +
 																"\n\t user: " + (String) request.getSession().getAttribute( "camod.loggedon.username" ) );
 		
 		/* Grab the current modelID from the session */
         String modelID = (String) request.getSession().getAttribute( Constants.MODELID );
         
         /* Create all the manager objects needed for Screen */
         AnimalModelManager animalModelManager = (AnimalModelManager) getBean( "animalModelManager" );
         SexDistributionManager sexDistributionManager = (SexDistributionManager) getBean( "sexDistributionManager" );
         TreatmentManager treatmentManager = ( TreatmentManager ) getBean( "treatmentManager" );
         AgentManager agentManager = ( AgentManager ) getBean( "agentManager" ); 
         
         /* Set modelID in AnimalModel object */
         AnimalModel animalModel = animalModelManager.get( modelID );        
  
 		/* 1.  Create and save SexDistribution Object */
 		SexDistribution sexDistribution = new SexDistribution();
 		sexDistribution.setType(envForm.getType());
         sexDistributionManager.save( sexDistribution );
 		
  		/*2. Create Treatment object, set its sexDistribution property (saved in #1) and other values, and save Treatment object.	*/	
 		Treatment treatment = new Treatment();
 
 		// Append the dose unit onto dose, if not null
 		treatment.setDosage( envForm.getAgeAtTreatment() + " " + envForm.getDoseUnit() );
 		treatment.setRegimen(envForm.getRegimen());
 		treatment.setAgeAtTreatment(envForm.getAgeAtTreatment() + "" + envForm.getAgeUnit());
 		treatment.setAdministrativeRoute(envForm.getAdministrativeRoute());
 		
 		//set its sexDistribution property in treatment		
 		treatment.setSexDistribution( sexDistribution );
 		
 		//save treatment object		
 		treatmentManager.save(treatment);
 		
 		System.out.println( "<EnvironmentalFactorAction save> Created and saved Treatment");
 		
 		/* 3. Create Agent, fill it with data meant for EnvironmentalFactor (since Agent IS-A an EnvironmentalFactor) and then save it.*/
         Agent agent = new Agent();
         agent.setName(envForm.getName() );
 
        agent.setType( "Environmental Factor" );
         agentManager.save(agent);
         
         /*4. Create Therapy object, set its therapeuticExperiment property to false.
     		4.1 set its treatment property (saved in #2).
     		4.2 set its agent property (saved in #3).
     		4.3 Add Therapy to animalModel 
     		4.4 No need to explicity save Therapy object b/c 1...1 relationship with AnimalModel   	
     		When TherapeuticExperiment property is false, tells us that this is an environmentalFactor
     	*/
 
         Therapy therapy = new Therapy();
         therapy.setTherapeuticExperiment( false );
         therapy.setAgent( agent );        
         therapy.setTreatment( treatment ); 
         
         /* 5. Add Therapy to AnimalModel */
         animalModel.addTherapy( therapy );
 
 		/* 6. save the animalModel = saves Therapy (Hibernate saves child in 1...1 relationships)  */  
         animalModelManager.save( animalModel );
         
         System.out.println( "<EnvironmentalFactorAction save> saved the animalModel");
  
     	//Add a message to be displayed in submitOverview.jsp saying you've created a new model successfully 
         ActionMessages msg = new ActionMessages();
         msg.add( ActionMessages.GLOBAL_MESSAGE, new ActionMessage( "environmentalfactor.creation.successful" ) );
         saveErrors( request, msg );
      
         return mapping.findForward("AnimalModelTreePopulateAction");		
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
     	
     	 return mapping.findForward("submitOverview");
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
 
     	// Grab the current modelID from the session  
     	String modelID = (String) request.getSession().getAttribute( Constants.MODELID ); 
     	
     	// Grab the current Therapy we are working with related to this animalModel
     	String aTherapyID = request.getParameter( "aTherapyID" );    	
     	
 		// Create a form to edit
 		EnvironmentalFactorForm envForm = ( EnvironmentalFactorForm ) form;
     	
 		System.out.println( "<EnvironmentalFactorAction save> following Characteristics:" + 
 																"\n\t name: " + envForm.getName() + 
 																"\n\t otherName: " + envForm.getOtherName() + 
 																"\n\t dosage: " + envForm.getDosage() + 
 																"\n\t administrativeRoute: " + envForm.getAdministrativeRoute() +
 																"\n\t regimen: " + envForm.getRegimen() +
 																"\n\t ageAtTreatment: " + envForm.getAgeAtTreatment() +
 																"\n\t type: " + envForm.getType() +
 																"\n\t user: " + (String) request.getSession().getAttribute( "camod.loggedon.username" ) );
 		
         AnimalModelManager animalModelManager = (AnimalModelManager) getBean( "animalModelManager" );
         SexDistributionManager sexDistributionManager = (SexDistributionManager) getBean( "sexDistributionManager" );
         TreatmentManager treatmentManager = ( TreatmentManager ) getBean( "treatmentManager" );
         AgentManager agentManager = ( AgentManager ) getBean( "agentManager" );
         TherapyManager therapyManager = ( TherapyManager ) getBean("therapyManager" );
 		
         AnimalModel animalModel = animalModelManager.get( modelID );
 		
         //retrieve the list of all therapies from the current animalModel
 		List therapyList = animalModel.getTherapyCollection();
 		
 		Therapy therapy = new Therapy();
 		int therapyNumber = 0;
 		
 		//find the specific one we need
 		for ( int i=0; i<therapyList.size(); i++ )
 		{
 			therapy = (Therapy)therapyList.get(i);
 			System.out.println( " searching ... id=" + therapy.getId().toString() + " = " + aTherapyID );
 			if ( therapy.getId().toString().equals( aTherapyID) )
 			{				
 				therapyNumber = i;
 				System.out.println( "found a match!");
 				break;
 			}
 		}       
 		
 		//Set the treatment
 		Treatment treatment = therapy.getTreatment();
         
     	//Set the gender
 		SexDistribution sexDistribution = treatment.getSexDistribution();
 		sexDistribution.setType( envForm.getType() );		
 		//save the sexdistro
 		sexDistributionManager.save( sexDistribution );
 		
 		//save the treatment
 		treatment.setDosage(envForm.getDosage() + " " + envForm.getDoseUnit() );
 		treatment.setAdministrativeRoute(envForm.getAdministrativeRoute());
 		treatment.setRegimen( envForm.getRegimen() );
 		treatment.setSexDistribution( sexDistribution );        
     	//Append the ageunit onto the age at treatment variable
 		treatment.setAgeAtTreatment( envForm.getAgeAtTreatment()+ " " + envForm.getAgeUnit());
     	treatmentManager.save( treatment );
 				
         //Agent IS-A an EnvironmentalFactor
         Agent agent = therapy.getAgent();
         agent.setName( envForm.getName() );
        agent.setType( "Environmental Factor" );
         agentManager.save( agent );
         
         //TherapeuticExperiment property is false, tells us that this is an environmentalFactor
         therapy.setTherapeuticExperiment( false );
         therapy.setAgent( agent );
         therapy.setTreatment( treatment );
         therapyManager.save( therapy );
         therapyList.set( therapyNumber, therapy );
         
         animalModel.setTherapyCollection( therapyList );           
 		
         //Add a message to be displayed in submitOverview.jsp saying you've created a new model successfully 
         ActionMessages msg = new ActionMessages();
         msg.add( ActionMessages.GLOBAL_MESSAGE, new ActionMessage( "environmentalfactor.edit.successful" ) );
         saveErrors( request, msg );
         
         return mapping.findForward("AnimalModelTreePopulateAction");
     }
 
 
 }
