 package org.cagrid.gaards.authentication.client;
 
 import gov.nih.nci.cagrid.authentication.bean.BasicAuthenticationCredential;
 import gov.nih.nci.cagrid.common.FaultHelper;
 import gov.nih.nci.cagrid.common.Utils;
 import gov.nih.nci.cagrid.metadata.ResourcePropertyHelper;
 import gov.nih.nci.cagrid.metadata.exceptions.InvalidResourcePropertyException;
 import gov.nih.nci.cagrid.metadata.exceptions.RemoteResourcePropertyRetrievalException;
 import gov.nih.nci.cagrid.metadata.exceptions.ResourcePropertyRetrievalException;
 import gov.nih.nci.cagrid.opensaml.SAMLAssertion;
 
 import java.io.InputStream;
 import java.io.StringReader;
 import java.rmi.RemoteException;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.xml.namespace.QName;
 
 import org.apache.axis.types.URI.MalformedURIException;
 import org.cagrid.gaards.authentication.AuthenticationProfiles;
 import org.cagrid.gaards.authentication.BasicAuthentication;
 import org.cagrid.gaards.authentication.Credential;
 import org.cagrid.gaards.authentication.faults.AuthenticationProviderFault;
 import org.cagrid.gaards.authentication.faults.CredentialNotSupportedFault;
 import org.cagrid.gaards.authentication.faults.InsufficientAttributeFault;
 import org.cagrid.gaards.authentication.faults.InvalidCredentialFault;
 import org.globus.wsrf.impl.security.authorization.Authorization;
 import org.globus.wsrf.utils.XmlUtils;
 import org.w3c.dom.Element;
 
 
 /**
  * @author <A href="mailto:langella@bmi.osu.edu">Stephen Langella </A>
  * @author <A href="mailto:oster@bmi.osu.edu">Scott Oster </A>
  * @author <A href="mailto:hastings@bmi.osu.edu">Shannon Hastings </A>
  * @version $Id: ArgumentManagerTable.java,v 1.2 2004/10/15 16:35:16 langella
  *          Exp $
  */
 public class AuthenticationClient {
 
     public static final QName AUTHENTICATION_PROFILES_METADATA = new QName("http://gaards.cagrid.org/authentication",
         "AuthenticationProfiles");
 
     private AuthenticationServiceClient client;
     private String serviceURI;
 
 
     public AuthenticationClient(String serviceURI) throws MalformedURIException, RemoteException {
         this.serviceURI = serviceURI;
         client = new AuthenticationServiceClient(serviceURI);
     }
 
 
     /**
      * This method specifies an authorization policy that the client should use
      * for authorizing the server that it connects to.
      * 
      * @param authorization
      *            The authorization policy to enforce
      */
 
     public void setAuthorization(Authorization authorization) {
         client.setAuthorization(authorization);
     }
 
 
     /**
      * This method authenticates with the authentication service using the
      * supplied credential.
      * 
      * @param cred
      *            The credential to use to authenticate with the credential
      *            service
      * @return A SAMLAssertion asserting successful authentication.
      * @throws RemoteException
      * @throws InvalidCredentialFault
      * @throws InsufficientAttributeFault
      * @throws AuthenticationProviderFault
      */
 
     public SAMLAssertion authenticate(Credential cred) throws RemoteException, CredentialNotSupportedFault,
         InvalidCredentialFault, InsufficientAttributeFault, AuthenticationProviderFault {
 
         Set<QName> profiles = null;
         try {
             profiles = getSupportedAuthenticationProfiles();
         } catch (Exception e) {
             throw new RemoteException(
                 "An unexpected error was encountered in trying to determine the supported authentication profiles: "
                     + Utils.getExceptionMessage(e), e);
         }
         if (profiles == null) {
             if (cred.getClass().equals(BasicAuthentication.class)) {
                 BasicAuthentication ba = (BasicAuthentication) cred;
                 BasicAuthenticationCredential bac = new BasicAuthenticationCredential();
                 bac.setUserId(ba.getUserId());
                 bac.setPassword(ba.getPassword());
                 gov.nih.nci.cagrid.authentication.bean.Credential c = new gov.nih.nci.cagrid.authentication.bean.Credential();
                 c.setBasicAuthenticationCredential(bac);
                 try {
                     gov.nih.nci.cagrid.authentication.client.AuthenticationClient ac = new gov.nih.nci.cagrid.authentication.client.AuthenticationClient(
                         this.serviceURI, c);
                     ac.setAuthorization(client.getAuthorization());
                     return ac.authenticate();
                 } catch (gov.nih.nci.cagrid.authentication.stubs.types.InvalidCredentialFault e) {
                     InvalidCredentialFault fault = new InvalidCredentialFault();
                     fault.setFaultString(e.getFaultString());
                     FaultHelper fh = new FaultHelper(fault);
                     fh.addFaultCause(e);
                     fault = (InvalidCredentialFault) fh.getFault();
                     throw fault;
                 } catch (gov.nih.nci.cagrid.authentication.stubs.types.InsufficientAttributeFault e) {
                     InsufficientAttributeFault fault = new InsufficientAttributeFault();
                     fault.setFaultString(e.getFaultString());
                     FaultHelper fh = new FaultHelper(fault);
                     fh.addFaultCause(e);
                     fault = (InsufficientAttributeFault) fh.getFault();
                     throw fault;
                 } catch (gov.nih.nci.cagrid.authentication.stubs.types.AuthenticationProviderFault e) {
                     AuthenticationProviderFault fault = new AuthenticationProviderFault();
                     fault.setFaultString(e.getFaultString());
                     FaultHelper fh = new FaultHelper(fault);
                     fh.addFaultCause(e);
                     fault = (AuthenticationProviderFault) fh.getFault();
                     throw fault;
                 } catch (Exception e) {
                     throw new RemoteException(Utils.getExceptionMessage(e), e);
                 }
             } else {
                 InvalidCredentialFault f = new InvalidCredentialFault();
                f.setFaultString("The service you are authenticated to is an older version of the " +
                		"authentication service and does not support the credential you provided.");
                 throw f;
             }
         } else {
             try {
                 return client.authenticateUser(cred);
             } catch (CredentialNotSupportedFault f) {
                 throw f;
             } catch (InvalidCredentialFault gie) {
                 throw gie;
             } catch (InsufficientAttributeFault ilf) {
                 throw ilf;
             } catch (AuthenticationProviderFault ilf) {
                 throw ilf;
             } catch (Exception e) {
                 throw new RemoteException(Utils.getExceptionMessage(e), e);
             }
         }
     }
 
 
     /**
      * This method obtains the authentication profiles supported by the
      * authentication service that the client is connecting to. The
      * authentication profiles are represented by the resource property:
      * (http://gaards.cagrid.org/authentication,AuthenticationProfiles). 
      * Client side authorization is not enforced when calling this method.
      * 
      * @return If the resource property exists a set is returned containing the
      *         QName(s) of the authentication profiles supported. If the
      *         resource property does not exist null is returned.
      * @throws InvalidResourcePropertyException
      * @throws RemoteResourcePropertyRetrievalException
      * @throws ResourcePropertyRetrievalException
      */
     public Set<QName> getSupportedAuthenticationProfiles() throws ResourcePropertyRetrievalException {
         Element resourceProperty = null;
         try {
             InputStream wsdd = getClass().getResourceAsStream("client-config.wsdd");
             resourceProperty = ResourcePropertyHelper.getResourceProperty(client.getEndpointReference(),
                 AUTHENTICATION_PROFILES_METADATA, wsdd);
         } catch (InvalidResourcePropertyException e) {
             return null;
         }
         try {
             AuthenticationProfiles result = (AuthenticationProfiles) Utils.deserializeObject(new StringReader(XmlUtils
                 .toString(resourceProperty)), AuthenticationProfiles.class);
             Set<QName> profiles = new HashSet<QName>();
             if (result != null) {
                 QName[] list = result.getProfile();
                 if (list != null) {
                     for (int i = 0; i < list.length; i++) {
                         profiles.add(list[i]);
                     }
                 }
             }
             return profiles;
 
         } catch (Exception e) {
             throw new ResourcePropertyRetrievalException("Unable to deserailize: " + e.getMessage(), e);
         }
     }
 }
