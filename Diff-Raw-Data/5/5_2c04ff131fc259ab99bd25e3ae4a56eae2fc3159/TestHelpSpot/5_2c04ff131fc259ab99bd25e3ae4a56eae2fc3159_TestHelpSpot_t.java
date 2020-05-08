 package com.github.stinkbird.helpspot.private_api;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.security.GeneralSecurityException;
 import java.security.KeyManagementException;
 import java.security.KeyStore;
 import java.util.Properties;
 
 import javax.xml.bind.JAXBException;
 
 import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
 
 import com.github.stinkbird.helpspot.common.FakeSslClientSocketFactory;
 import com.github.stinkbird.helpspot.common.KeystoreCertSslClientSocketFactory;
 
 public class TestHelpSpot {
 	
 	private static final Properties properties = new Properties();
 	
 	private static String helpSpotHostNameOrIPAddress;	
 	private static int portNumber;
 	private static boolean isSecureHttp;	
 	private static boolean isUseFakeCertificate;	
 	private static String keystoreFileNameWithFullPath;	
 	private static String helpSpotAdminUsername;
 	private static String helpSpotAdminPassword;
 	
 	private static boolean proceed = true;
 	
 	static {			
 			try {
 			    properties.load(TestHelpSpot.class.getResourceAsStream("HelpSpot.properties"));
 			    helpSpotHostNameOrIPAddress = properties.getProperty("helpSpotHostNameOrIPAddress");
 			    portNumber = Integer.parseInt(properties.getProperty("portNumber"));
 			    isSecureHttp = Boolean.parseBoolean(properties.getProperty("isSecureHttp"));
 			    isUseFakeCertificate = Boolean.parseBoolean(properties.getProperty("isUseFakeCertificate"));
 			    keystoreFileNameWithFullPath = properties.getProperty("keystoreFileNameWithFullPath");
 			    helpSpotAdminUsername = properties.getProperty("helpSpotAdminUsername");
 			    helpSpotAdminPassword = properties.getProperty("helpSpotAdminPassword");
 			} catch (Throwable t) {
 				t.printStackTrace();
 				proceed = false;
 			}
 	}
 	
 	private static ProtocolSocketFactory createProtocolSocketFactoryFromKeyStoreFile(String fullPathOfKeyStoreFile) 
 	throws IOException, KeyManagementException, GeneralSecurityException {
 		 FileInputStream fIn = new FileInputStream(fullPathOfKeyStoreFile);
          KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
          keystore.load(fIn, null);
          return new KeystoreCertSslClientSocketFactory(keystore);
 	}
 	
 	public static void main(String[] args) throws IOException, GeneralSecurityException, JAXBException {
 		if(proceed) {
 			
 			ProtocolSocketFactory protocolSocketFactory = null;
 			
 			if(isSecureHttp) {
 				if(isUseFakeCertificate) {
 					protocolSocketFactory = new FakeSslClientSocketFactory();
 				} else {
					protocolSocketFactory = createProtocolSocketFactoryFromKeyStoreFile(keystoreFileNameWithFullPath);
 				}
 			}
 			
 			com.github.stinkbird.helpspot.private_api.PrivateApiUtil privateApiUtil =
 					com.github.stinkbird.helpspot.private_api.PrivateApiUtil.getInstanceOf( helpSpotHostNameOrIPAddress, 
 																							portNumber, 
 																							isSecureHttp, 
 																							protocolSocketFactory, 
 																							helpSpotAdminUsername, 
 																							helpSpotAdminPassword );
 			
 			com.github.stinkbird.helpspot.private_api.response_for.request.search.Requests allCustomerRequests = privateApiUtil.callPrivateRequestSearch("", true);
 			System.out.println("\nRequests contains total = " + allCustomerRequests.getRequest().size() + " historical request for customer");
 			System.out.println("\nFirst xRequest is " + allCustomerRequests.getRequest().get(0).getXRequest() + ". \n");
 			
 		} else {
 			System.out.println(false);
 		}
 	}
 	
 }
