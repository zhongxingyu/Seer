 package org.fiteagle.interactors.sfa.register;
 
 import java.io.StringReader;
 import java.security.cert.X509Certificate;
 import java.util.HashMap;
 import java.util.UUID;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 
 import org.fiteagle.core.aaa.x509.X509Util;
 import org.fiteagle.core.groupmanagement.Group;
 import org.fiteagle.core.groupmanagement.GroupDBManager;
import org.fiteagle.core.groupmanagement.SQLiteGroupDatabase.CouldNotCreateGroup;
 import org.fiteagle.core.util.URN;
 import org.fiteagle.interactors.sfa.common.AMResult;
 import org.fiteagle.interactors.sfa.common.ListCredentials;
 import org.fiteagle.interactors.sfa.common.SFAv3RequestProcessor;
 import org.fiteagle.interactors.sfa.getSelfCredential.jaxbClasses.Credential;
 import org.fiteagle.interactors.sfa.getSelfCredential.jaxbClasses.Signatures;
 import org.fiteagle.interactors.sfa.getSelfCredential.jaxbClasses.SignedCredential;
 import org.fiteagle.interactors.sfa.util.CredentialFactory;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class RegisterRequestProcessor extends SFAv3RequestProcessor{
 Logger log = LoggerFactory.getLogger(getClass()); 
   @Override
   public AMResult processRequest(ListCredentials credentials, Object... specificArgs) {
     
     throw new NotImplemented();
   }
   
   public HashMap<String, Object> register(HashMap<String, Object> parameters){
     URN slicUrn = getUrn(parameters);
     String type = getType(parameters);
     String credentialString = getCredential(parameters);
     SignedCredential cred = null;
     X509Certificate userCertificate = null;
     try {
        cred =buildCredential(credentialString);
        userCertificate  =X509Util.buildX509Certificate(cred.getCredential().getOwnerGid());
     } catch (JAXBException e) {
       log.error(e.getMessage(),e);
     }
     
     if(isSliceType(type)){
     	URN userURN = new URN(cred.getCredential().getOwnerURN());
     	Group slice = new Group(slicUrn.getSubjectAtDomain(), userURN.getSubjectAtDomain() );
    	try{
    		GroupDBManager groupmananger = GroupDBManager.getInstance();
    		groupmananger.addGroup(slice);
    	}catch(CouldNotCreateGroup e){
    		HashMap<String, Object> returnMap =  new HashMap<String, Object>();
        	returnMap.put("code", new Integer(17));
        	returnMap.put("value","");
        	returnMap.put("output", "");
        	return returnMap;
    	}
     	String sliceCredential = createSignedCredential(slice,userCertificate);
     	HashMap<String, Object> returnMap =  new HashMap<String, Object>();
     	returnMap.put("code", new Integer(0));
     	returnMap.put("value", sliceCredential);
     	returnMap.put("output", "");
     	return returnMap;
     }
     else {
       return new HashMap<String, Object>();
     }
 	
     
   }
   
   private String createSignedCredential(Group slice, X509Certificate userCertificate) {
 	
 	Credential credential = createCredential(slice, userCertificate);
 	String sliceCredential = CredentialFactory.signCredential(credential);
 	return sliceCredential;
   }
 
 private Signatures createSignature() {
 	// TODO Auto-generated method stub
 	return null;
 }
 
 private Credential createCredential(Group slice, X509Certificate userCertificate) {
 	Credential credential = CredentialFactory.newCredential(userCertificate, URN.getURNFromGroup(slice));
 	return credential;
 }
 
 public class NotImplemented extends RuntimeException{
     private static final long serialVersionUID = 1L;
   }
 
   public String getType(HashMap<String, Object> hashMap) {
     return hashMap.get("type").toString();
     
   }
 
   public boolean isSliceType(String string) {
    return string.equalsIgnoreCase("Slice");
   }
 
   
   public URN  getUrn(HashMap<String, Object> hashMap) {
     return new URN(hashMap.get("urn").toString());
   }
 
   
 
   public String getCredential(HashMap<String, Object> hashMap) {
     return hashMap.get("credential").toString();
   }
 
   public SignedCredential buildCredential(String credential) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance("org.fiteagle.interactors.sfa.getSelfCredential.jaxbClasses");
       Unmarshaller unmarshaller = context.createUnmarshaller();
       StringReader reader = new StringReader(credential);
       SignedCredential sc = (SignedCredential) unmarshaller.unmarshal(reader);
       return sc;
   }
   
 }
