 package gov.nih.nci.cabig.labviewer.grid;
 
 import gov.nih.nci.caxchange.ctom.viewer.util.LabViewerAuthorizationHelper;
 import gov.nih.nci.ccts.grid.HealthcareSiteType;
 import gov.nih.nci.ccts.grid.IdentifierType;
 import gov.nih.nci.ccts.grid.ParticipantType;
 import gov.nih.nci.ccts.grid.Registration;
 import gov.nih.nci.ccts.grid.StudyRefType;
 import gov.nih.nci.ccts.grid.StudySiteType;
 import gov.nih.nci.ccts.grid.common.RegistrationConsumer;
 import gov.nih.nci.ccts.grid.service.globus.RegistrationConsumerAuthorization;
 import gov.nih.nci.ccts.grid.stubs.types.InvalidRegistrationException;
 import gov.nih.nci.ccts.grid.stubs.types.RegistrationConsumptionException;
 import gov.nih.nci.ctom.ctlab.domain.HealthCareSite;
 import gov.nih.nci.ctom.ctlab.domain.Identifier;
 import gov.nih.nci.ctom.ctlab.domain.Participant;
 import gov.nih.nci.ctom.ctlab.domain.Protocol;
 import gov.nih.nci.ctom.ctlab.domain.StudyParticipantAssignment;
 import gov.nih.nci.ctom.ctlab.persistence.CTLabDAO;
 
 import java.rmi.RemoteException;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.util.Calendar;
 import java.util.HashMap;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  * LabViewerRegistrationConsumer is the implementing class for this grid service
  * for Lab Viewer. It consumes the patient registration message that is sent from
  * the patient registry application via the hub.
  * <P>
  * @author Anupama Sharma
  */
 public class LabViewerRegistrationConsumer implements RegistrationConsumer
 {
 	private static final int MILLIS_PER_MINUTE = 60 * 1000;
 	private static final int THRESHOLD_MINUTE = 2;
 	private static final Log logger = LogFactory.getLog(LabViewerRegistrationConsumer.class);
 	private HashMap<String,ParticipantPersistTime> map = new HashMap<String,ParticipantPersistTime>();
 	private CTLabDAO dao = new CTLabDAO();
 	private Connection con;
 	
 	/**
 	 * commit is not currently implemented but is included in the grid service to possibly
 	 * provide a double commit transaction process.
 	 */
 	public void commit(Registration registration) throws RemoteException, InvalidRegistrationException
 	{
 		// TODO Auto-generated method stub
 		
 	}
 
 	/* (non-Javadoc)
 	 * @see gov.nih.nci.ccts.grid.common.RegistrationConsumer#rollback(gov.nih.nci.ccts.grid.Registration)
 	 */
 	public void rollback(Registration registration) throws RemoteException, InvalidRegistrationException
 	{
 		ParticipantType participant = registration.getParticipant();
 		String participantGridId = participant.getGridId();
 		long epochPersistTime=0;
 		long epochcurrentTime=0;
 		
 		/* Authorization code currently disabled
 		String username = RegistrationConsumerAuthorization.getCallerIdentity();
 		
 		if (username == null)
 		{
 			logger.error("Error saving participant no username provided");
 			RegistrationConsumptionException rce = new RegistrationConsumptionException();
 			rce.setFaultString("No user credentials provided");
 			throw rce;
 		}
 		else
 		{
 			System.out.println("User who called was " + username);
 			
 			LabViewerAuthorizationHelper lvaHelper = new LabViewerAuthorizationHelper();
 			
 			boolean authorized = lvaHelper.isAuthorized(username);
 			
 			if (!authorized)
 			{
 				logger.error("Error saving participant");
 				RegistrationConsumptionException rce = new RegistrationConsumptionException();
 				rce.setFaultString("User not authorized for this operation");
 				throw rce;
 			}
 		}
 		*/
 		
 		//need to get the hashmap from the application context
 		con = dao.getConnection();
 		
 		if(map.containsKey(participantGridId))
 		{
 			try
 			{ 
 				ParticipantPersistTime ppt = map.get(participantGridId);
 				Calendar persistTime = ppt.getPersistTime();
 				epochPersistTime = persistTime.getTime().getTime();
 				Calendar currentTime = Calendar.getInstance();
 				epochcurrentTime=currentTime.getTime().getTime();
 				double minutes = (double)(epochcurrentTime-epochPersistTime)/MILLIS_PER_MINUTE;
 				if(minutes < THRESHOLD_MINUTE)
 				{	
 					dao.rollbackParticipant(con, ppt.getParticipant());
 				}
 			}
 			catch(SQLException se)
 			{
 				logger.error("Error deleting participant", se);
 			}
 			finally
 			{
 				try
 				{
 					con.close();
 				}
 				catch (SQLException e)
 				{
 					logger.error("Error closing connection",e);
 				}
 			}
 		}
 		else
 		{
 			InvalidRegistrationException ire = new InvalidRegistrationException();
			ire.setFaultString("Invalid patient rollback message- no patient found with given gridid");
 			throw ire;
 		}
 		
 		logger.info("deleted participant");
 		cleanupHashMap(epochcurrentTime);
 	}
 	
 	/* (non-Javadoc)
 	 * @see gov.nih.nci.ccts.grid.common.RegistrationConsumer#register(gov.nih.nci.ccts.grid.Registration)
 	 */
 	public Registration register(Registration registration)
 		throws RemoteException, InvalidRegistrationException, RegistrationConsumptionException
 	{
 		logger.info("Lab Viewer Registration message received");
 		StudyRefType studyRef = registration.getStudyRef();
 		
 		/* Authorization code currently disabled
 		String username = RegistrationConsumerAuthorization.getCallerIdentity();
 		
 		if (username == null)
 		{
 			logger.error("Error saving participant no username provided");
 			RegistrationConsumptionException rce = new RegistrationConsumptionException();
 			rce.setFaultString("No user credentials provided");
 			throw rce;
 		}
 		else
 		{
 			System.out.println("User who called was " + username);
 			
 			LabViewerAuthorizationHelper lvaHelper = new LabViewerAuthorizationHelper();
 			
 			boolean authorized = lvaHelper.isAuthorized(username);
 			
 			if (!authorized)
 			{
 				logger.error("Error saving participant");
 				RegistrationConsumptionException rce = new RegistrationConsumptionException();
 				rce.setFaultString("User not authorized for this operation");
 				throw rce;
 			}
 		}
 		*/
 	
 		// save the study data
 		Protocol protocol = new Protocol();
 		protocol.setLongTxtTitle(studyRef.getLongTitleText());
 		protocol.setShortTxtTitle(studyRef.getShortTitleText());
 	   	IdentifierType identifiers[] = studyRef.getIdentifier();
 	   	
         // save the identifier data	
 		for(IdentifierType ident: identifiers)
 		{
 			if (ident.getPrimaryIndicator())
 			{
 				Identifier id = new Identifier();
 				id.setExtension(ident.getValue());
 				id.setSource(ident.getSource());
 				id.setRoot(studyRef.getGridId());
 				protocol.setIdentifier(id);
 				protocol.setNciIdentifier(id.getRoot() + "." + id.getExtension());
 			}
 		}
 		
 		// save the study site data
 		StudySiteType studySite = registration.getStudySite();
 		HealthcareSiteType hcsType = studySite.getHealthcareSite(0);
 		HealthCareSite healthCare = new HealthCareSite();
 		//String tmpstr = studySite.getGridId()+"."+hcsType.getNciInstituteCode();
 		String tmpstr = hcsType.getNciInstituteCode();
 		healthCare.setNciInstituteCd(tmpstr);
 		
 		//save participant data
 		ParticipantType participant = registration.getParticipant();
 		Participant part = new Participant();
 		part.setFirstName(participant.getFirstName());
 		part.setLastName(participant.getLastName());
 		char[] initials= new char[2];
 		initials[0]=participant.getFirstName().charAt(0);
 		initials[1]=participant.getLastName().charAt(0);
 		part.setInitials(new String(initials));
 		part.setAdminGenderCode(participant.getAdministrativeGenderCode());
 		part.setBirthDate(participant.getBirthDate());
 		part.setEthnicGroupCode(participant.getEthnicGroupCode());
 		part.setRaceCode(participant.getRaceCode());
 		 
 		// Assume that only one identifier was sent and use that
 		IdentifierType[] ids = participant.getIdentifier();
 		IdentifierType id = null;
 		Identifier partIdent = new Identifier();
 		if (ids != null && ids.length > 0)
 		{
 			id = ids[0];
 			partIdent.setExtension(id.getValue());
 			partIdent.setSource(id.getSource());
 			partIdent.setRoot(participant.getGridId());
 			part.setIdentifier(partIdent);
 		}
 		else
 		{
 			InvalidRegistrationException ire = new InvalidRegistrationException();
 			ire.setFaultString("Invalid patient registration message missing patient identifier");
 			throw ire;
 		}
 		
 		StudyParticipantAssignment studyPartAssig = new StudyParticipantAssignment();
 		studyPartAssig.setParticipant(part);
 		String tmp = participant.getGridId()+"."+id.getValue();
 		studyPartAssig.setStudyPartIdOrig(tmp);
 		studyPartAssig.setIdentifier(partIdent);
 		healthCare.setStudyParticipantAssignment(studyPartAssig);
 		protocol.setHealthCareSite(healthCare);
 		
 		con = dao.getConnection();
 		logger.info("Lab Viewer Registration message validated");
 		
 		try
 		{
 			dao.saveProtocol(con, protocol);
 			logger.info("Persisted the study with patient information");
 			//After you persist the protocol, put the participant associated with the
 			//protocol into a HashMap. 
 			//In case of roll back, check if the participant was just persisted
 			//then call roll back on that object.
 			Calendar persistTime = Calendar.getInstance();
 			ParticipantPersistTime partPersistTime = new ParticipantPersistTime();
 			partPersistTime.setParticipant(part);
 			partPersistTime.setPersistTime(persistTime);
 		    map.put(participant.getGridId(),partPersistTime);
 			//need to store the map in the application context		
 		}
 		catch (SQLException e)
 		{
 			logger.error("Error saving participant", e);
 			RegistrationConsumptionException rce = new RegistrationConsumptionException();
 			rce.setFaultString(e.getMessage());
 			throw rce;
 		}
 		catch (Exception e)
 		{
 			logger.error("Error saving participant", e);
 			RegistrationConsumptionException rce = new RegistrationConsumptionException();
 			rce.setFaultString(e.getMessage());
 			throw rce;
 		}
 		finally
 		{
 			try
 			{
 				con.close();
 			}
 			catch (SQLException e)
 			{
 				logger.error("Error closing connection",e);
 			}
 		}
 		
 		logger.info("Lab Viewer Registration message stored");
 		
 		return registration;
 	}
 	
 	/**
 	 * Cleans up the hash map - looks for the time stamp value in all the ParticipantPersistTime
 	 * if it is difference between the current time and persist time is greater than the threshold
 	 * value -then the ParticipantPersistTime is removed from the hash map.
 	 */
 	private void cleanupHashMap(long currentTime)
 	{
 		for(ParticipantPersistTime ppt: map.values())
 		{
 			
 			long persistTime = ppt.getPersistTime().getTime().getTime();
 			double diffTime = (double)(currentTime-persistTime)/MILLIS_PER_MINUTE;
 			if(diffTime > THRESHOLD_MINUTE )
 			{
 				map.remove(ppt.getParticipant().getIdentifier().getRoot());
 			}
 		}
 	}
 	
 }
