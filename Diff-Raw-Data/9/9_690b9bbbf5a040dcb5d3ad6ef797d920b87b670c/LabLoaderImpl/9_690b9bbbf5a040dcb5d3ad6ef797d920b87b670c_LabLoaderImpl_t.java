 package gov.nih.nci.cagrid.labviewer.service;
 
 import gov.nih.nci.cagrid.labviewer.service.globus.LabLoaderAuthorization;
 import gov.nih.nci.cagrid.labviewer.xml.HL7v3CtLabUnMarshaller;
 import gov.nih.nci.caxchange.ctom.viewer.util.LabViewerAuthorizationHelper;
 import gov.nih.nci.ctom.ctlab.handler.ProtocolHandler;
 
 import java.rmi.RemoteException;
 import java.sql.Connection;
 
 import org.apache.log4j.Logger;
 
 /**
  * LabLoaderImpl is the implementation of the Lab Loader grid service to accept
  * HL7v3 messages and persist them to the CTODS database.
  * 
  * @author Michael Holck
  */
 public class LabLoaderImpl extends LabLoaderImplBase
 {
 	Logger logger = Logger.getLogger(getClass());
 
 	public LabLoaderImpl() throws RemoteException
 	{
 		super();
 	}
 
 	/**
 	 * loadLab method unmarshalls Lab message and calls into the 
 	 * DAO to the persist the Lab message data
 	 * @param string
 	 * @throws RemoteException
 	 */
 	public void loadLab(java.lang.String string) throws RemoteException
 	{
 		logger.info("LabLoader loadLab method called.");
 		String username = LabLoaderAuthorization.getCallerIdentity();
 
          /* Ram commented this below after talking to Anu on 10/30/2009.  Again uncommented this after fixing
            some caGrid config files on 12/02/2009 */
         if (!authorized(username))
 		{
 			logger.error("User " + username
 					+ " not authorized for this operation");
 			RemoteException re =
 					new RemoteException("User " + username
 							+ " not authorized for this operation");
 			throw re;
 
 		}
 		else
 		{

            // Now unmarshall the HL7v3 message
 			HL7v3CtLabUnMarshaller unMarshaller = new HL7v3CtLabUnMarshaller();
 			Object obj = null;
 			Connection con = null;
 			try
 			{
 				obj = unMarshaller.parseXmlToObject(string);
 
 				// Now save the lab
 				ProtocolHandler dao = new ProtocolHandler();
 				// obtain the connection
 				con = dao.getConnection();
 				con.setAutoCommit(false);
 
 				if (obj != null)
 				{
 					// Call into the DAO save Protocol method.
 					dao.persist(con,
 							(gov.nih.nci.ctom.ctlab.domain.Protocol) obj);
 				}
 				// call connection commit
 				con.commit();
 				logger
 						.debug("Message succussfully saved to the CTODS Database");
 			}// end of try
 			catch (Exception ex)
 			{
 				logger.debug(ex.getMessage());
 				try
 				{
 					// issue rollback in case of exception
 					con.rollback();
 				}
 				catch (Exception e)
 				{
 					// throw the remote exception
 					RemoteException re1 = new RemoteException(e.getMessage());
 					throw re1;
 				}
 			}
 			finally
 			{
 				try
 				{
 					con.close();
 				}
 				catch (Exception ex)
 				{
 					logger.error("Error closing connection",ex);
 				}
 			}// end of finally
		}// /*  Ram's comment on 10/30/2009 END end of else */
 	}
 
 	/**
 	 * @param username
 	 * @return
 	 * @throws RemoteException
 	 */
 	private boolean authorized(String username) throws RemoteException
 	{
 		boolean userAuthorized = false;
 		// Authorization code : note No Specific exception for LabLoader
 
 		String user = "";
 		if (username == null)
 		{
 			logger.error("No user credentials provided");
 			RemoteException re =
 					new RemoteException("No user credentials provided");
 			throw re;
 		}
 		else
 		{
 			logger.info("User who is trying to access the service " + username);
 			// instantiate LabViewerAuthorizationHelper
 			LabViewerAuthorizationHelper lvaHelper =
 					new LabViewerAuthorizationHelper();
 			if (username != null)
 			{
 				int beginIndex = username.lastIndexOf("=");
 				int endIndex = username.length();
 				user = username.substring(beginIndex + 1, endIndex);
 			}
 			// call the authorization method
 			userAuthorized = lvaHelper.isAuthorized(user);
 		}
 
 		return userAuthorized;
 
 	}
 
 	/**
 	 * @param string
 	 * @throws RemoteException
 	 */
 	public void rollback(java.lang.String string) throws RemoteException
 	{
 		logger.info("LabLoader rollback method called: Not Implemented");
 	}
 
 }
