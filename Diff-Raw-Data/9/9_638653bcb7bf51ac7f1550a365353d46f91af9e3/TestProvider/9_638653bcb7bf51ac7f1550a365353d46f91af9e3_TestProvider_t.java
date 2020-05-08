 /**
  * 
  * Copyright 2002 NCHELP
  * 
  * Author:		Tim Bornholtz,  Priority Technologies, Inc.
  * 
  * 
  * This code is part of the Meteor system as defined and specified 
  * by the National Council of Higher Education Loan Programs, Inc. 
  * (NCHELP) and the Meteor Sponsors, and developed by Priority 
  * Technologies, Inc. (PTI). 
  *
  * 
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *	
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *	
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  ********************************************************************************/
 
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.PrintStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.security.KeyStore;
 import java.security.PrivateKey;
 import java.security.Security;
 import java.security.cert.X509Certificate;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.nchelp.hpc.util.FileUtils;
 import org.nchelp.meteor.message.MeteorIndexResponse;
 import org.nchelp.meteor.provider.DataProvider;
 import org.nchelp.meteor.provider.IndexProvider;
 import org.nchelp.meteor.provider.MeteorParameters;
 import org.nchelp.meteor.security.SecurityToken;
 import org.nchelp.meteor.util.Resource;
 import org.nchelp.meteor.util.ResourceFactory;
 import org.nchelp.meteor.util.exception.DataException;
 import org.nchelp.meteor.util.exception.IndexException;
 import org.nchelp.meteor.util.exception.ParameterException;
 
 public class TestProvider {
 	
 	private final Log log = LogFactory.getLog(this.getClass());
 	private SecurityToken token;
 	private String url;
 	private String destinationId;
 	private String ssn;
 	private String id = "ED.TIM";
 	private String providerType = "DP";
 	private String role = SecurityToken.roleFAA; 
 	private int    authLevel = 3;
 	private MeteorParameters parms;
 	private boolean shouldSign = true;
 	private PrintStream out = System.out;
 	
 	public static void main(String[] args) {
 		
 		TestProvider provider = new TestProvider();
 		
 		provider.setParameters(args);
 		
 		if((provider.url == null && provider.destinationId == null) || provider.ssn == null){
 			usage();
 			return;
 		}
 		
 		provider.init();
 		
 		if("DP".equalsIgnoreCase(provider.providerType)){
 			provider.testDataProvider();
 		} else if("IP".equalsIgnoreCase(provider.providerType)){
 			provider.testIndexProvider();	
 		} else {
 			provider.out.println("Provider type '" + provider.providerType + "' is not valid.  It must be 'DP' or 'IP'");	
 		}
 	}
 
 	private void init(){
 		// If we are using SSL, then set up java.net.URL to handle this
 		System.setProperty("java.protocol.handler.pkgs", "com.sun.net.ssl.internal.www.protocol");
 		Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());		
 
 
 		parms = new MeteorParameters();
 		parms.setSsn(this.ssn);
 		
 		parms.setCurrentUser("somerandomopaqueid");
 		parms.setInstitutionID(this.id);
 		try {
 			parms.setRole(this.role);
 		} catch(ParameterException e) {
 			e.printStackTrace();
 			System.exit(0);
 		}
 		parms.setLenderID("LENDER1");
 		
 		if(token == null){
 			token = new SecurityToken();
 			try {
 				token.setAuthenticationProcessID("1");
 				token.setCurrentAuthLevel(authLevel);
 				token.setRole(parms.getRole());
 				token.setUserid(parms.getCurrentUser());
 				token.setInstitutionID(parms.getInstitutionID());
 
 				if(SecurityToken.roleBORROWER.equals(role)){
 					token.setAttribute("SSN", parms.getSsn());
 				}
 				
 				if(SecurityToken.roleLENDER.equals(role)) {
 					token.setAttribute(SecurityToken.roleLENDER, parms.getLenderID());
 				}
 
 				if(this.shouldSign){
 					this.signToken();
 				}
 				
 			} catch(Exception e) {
 				e.printStackTrace();
 				return;
 			}
 		}
 	}
 	
 	public void testDataProvider(){
 
 		DataProvider d1 = new DataProvider();
 
 		if(this.url != null){
 			try {
 				d1.setURL(new URL(this.url));
 			} catch(MalformedURLException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		if(this.destinationId != null){
 			log.fatal("Setting Destination ID to " + destinationId);
 			d1.setId(this.destinationId);
 		} else {
 			d1.setId(token.getInstitutionID());
 		}
 		
 		d1.setParams(parms);						
 		d1.setToken(token);
 		
 		try {
 			d1.getData();
 		} catch(DataException e) {
 			e.printStackTrace();
 			return;
 		}
 		
 		String mdr = d1.getResponse();
 		
 		out.println(mdr);
 		
 	}
 	
 	private void testIndexProvider(){
 		IndexProvider ip = new IndexProvider();
 		
 		if(this.destinationId != null){
 			log.fatal("Setting Destination ID to " + destinationId);
 			ip.setIdentifier(this.destinationId);
 		} else {
 			ip.setIdentifier(token.getInstitutionID());
 		}
 		
 		if(this.url != null) {
 			try {
 				ip.setURL(new URL(this.url));
 			} catch(MalformedURLException e) {
 				e.printStackTrace();
 			}
 		}
 				
 		this.parms.setSecurityToken(this.token);
 		try {
 			MeteorIndexResponse mir = ip.getDataProviders(this.parms);
 			
 			out.println(mir);
		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private void setParameters(String[] args){
 		for(int i = 0; i < args.length; i++){
 			String key = args[i];
 			log.error("Parsing key: " + key);
 			String value = args[i + 1];
 			log.error("Parsing value: " + value);
 			
 			if(key.startsWith("-")){
 				if("-ssn".equalsIgnoreCase(key)){
 					this.ssn = value;
 				} else if ("-assertion".equalsIgnoreCase(key)){
 					out.println("Setting Security token from: " + value);
 					this.token = this.readToken(value);
 				} else if ("-url".equalsIgnoreCase(key)){
 					this.url = value;
 				} else if ("-id".equalsIgnoreCase(key)){
 					this.id = value;
 				} else if ("-destinationid".equalsIgnoreCase(key)){
 					this.destinationId = value;
 				} else if ("-type".equalsIgnoreCase(key)){
 					this.providerType = value;	
 				} else if ("-authlevel".equalsIgnoreCase(key)){
 					this.authLevel = Integer.parseInt(value);
 				} else if ("-sign".equalsIgnoreCase(key)){
 					this.shouldSign = "true".equalsIgnoreCase(value);
 				} else if ("-role".equalsIgnoreCase(key)){
 					this.role = value;
 				} else if ("-file".equalsIgnoreCase(key)){
 					try {
 						out = new PrintStream(new FileOutputStream(value));
 					} catch (FileNotFoundException e) {
 						e.printStackTrace();
 					}
 				}
 				
 			} else {
 				out.println("Ech!  Bad Arguments.  Got a key of '" + key + "' and a value of '" + value + "'");
 				System.exit(0);
 			}
 			
 			i++;
 		}
 		
 	}
 	
 	private static void usage(){
 		System.out.println("Usage: java TestProvider -url DataProviderURL -ssn SSN [-type ( DP | IP )] [-assertion AssertionFile] [-sign (true | false) [-destinationid ID]\n" +
 		                  "     ex: java TestProvider -url http://localhost:8080/DataProvider/rpcrouter -ssn 987654321 -assertion signedassertion.txt\n\n");
 	}
 	
 	private SecurityToken readToken(String filename){
 		SecurityToken token;
 		try {
 			String strToken = new String(FileUtils.readFile(filename));
 			token = new SecurityToken(strToken);
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 		return token;	
 	}
 	
 	private void signToken(){
 		Resource res = ResourceFactory.createResource("authentication.properties");
 
 
 		// OK, now we need to get the private key stuff
 	    String keystoreType = res.getProperty("authentication.keystore.type");
 	    String keystoreFile = res.getProperty("authentication.keystore.file");
 	    String keystorePass = res.getProperty("authentication.keystore.password");
 		String privateKeyAlias = res.getProperty("authentication.privatekey.alias");
 		String privateKeyPass = res.getProperty("authentication.privatekey.password");
 		Boolean includeCertificate = Boolean.valueOf(res.getProperty("authentication.certificate.include"));
 		String certificateAlias = res.getProperty("authentication.certificate.alias");
 
 	    PrivateKey privateKey = null;
 	    X509Certificate cert = null;
 
 		try {
 			KeyStore ks = KeyStore.getInstance(keystoreType);
 			FileInputStream fis = new FileInputStream(keystoreFile);
 			
 			ks.load(fis, keystorePass.toCharArray());
 			
 			privateKey = (PrivateKey) ks.getKey(privateKeyAlias, privateKeyPass.toCharArray());
 	         
 	        cert = (X509Certificate) ks.getCertificate(certificateAlias);
 			token.setPrivateKey(privateKey);
 
 		} catch(Exception e) {
 			e.printStackTrace();
 			System.exit(0);
 		}
 	
 		
 	}
 }
