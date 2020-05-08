 /**
  * ISBServiceSoapBindingImpl.java
  *
  * This file was auto-generated from WSDL
  * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
  */
 
 package is.idega.idegaweb.pheidippides.webservice.isb.server;
 
 import is.idega.idegaweb.pheidippides.webservice.business.PheidippidesWebService;
 
 import org.springframework.beans.factory.annotation.Autowired;
 
 import com.idega.business.IBOLookupException;
 
 public class ISBServiceSoapBindingImpl implements is.idega.idegaweb.pheidippides.webservice.isb.server.ISBService_PortType{	
 	@Autowired
 	private PheidippidesWebService business;
 
     public is.idega.idegaweb.pheidippides.webservice.isb.server.Session authenticateUser(is.idega.idegaweb.pheidippides.webservice.isb.server.Login in0) throws java.rmi.RemoteException {
     	is.idega.idegaweb.pheidippides.webservice.server.Session session = getBusiness().authenticateUser(in0.getLoginName(), in0.getPassword());
     	
         return new is.idega.idegaweb.pheidippides.webservice.isb.server.Session(session.getSessionID());
     }
 
     public String registerRunner(is.idega.idegaweb.pheidippides.webservice.isb.server.RunnerInfo in0) throws java.rmi.RemoteException, is.idega.idegaweb.pheidippides.webservice.isb.server.SessionTimedOutException {
     	return getBusiness().registerRunner(in0.getSession(), in0.getPersonalID(), in0.getDistance(), in0.getShirtSize(), in0.getShirtSizeGender(), in0.getEmail(), in0.getPhone(), in0.getMobile(), in0.getLeg(), in0.getPartners(), in0.getRegisteredBy(), in0.getCharityPersonalID());
     }
 
     private PheidippidesWebService getBusiness() throws IBOLookupException {
 		return business;
 	}
 }
