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
 
 package test.nchelp.meteor;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.security.Security;
 import java.util.List;
 
 import junit.framework.TestCase;
 import junit.textui.TestRunner;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.nchelp.meteor.provider.DistributedRegistry;
 import org.nchelp.meteor.provider.IndexProvider;
 import org.nchelp.meteor.provider.MeteorParameters;
 import org.nchelp.meteor.registry.Directory;
 import org.nchelp.meteor.registry.DirectoryFactory;
 import org.nchelp.meteor.security.SecurityToken;
 import org.nchelp.meteor.util.exception.DirectoryException;
 import org.nchelp.meteor.util.exception.IndexException;
 import org.nchelp.meteor.util.exception.ParameterException;
 import org.nchelp.meteor.util.exception.SignatureException;
 
 public class IndexProviderTest extends TestCase {
 	
 	private final Log log = LogFactory.getLog(this.getClass());
 
 	public IndexProviderTest(String name) {
 		super(name);
 	}
 
 	public static void main(String args[]) {
 		TestRunner.run(IndexProviderTest.class);
 	}
 
 	public void testSimple() {
 		 DistributedRegistry registry = DistributedRegistry.singleton();
 		 List providers = registry.getIndexProviders();
 		 log.debug(providers);
 		 
 	}
 	
 	public void testIndexProvider(){
 		String identifier = "ED.PTI";
 		
 		IndexProvider ip = new IndexProvider();
 		ip.setIdentifier(identifier);
 
 
 		// If we are using SSL, then set up java.net.URL to handle this
 		System.setProperty("java.protocol.handler.pkgs", "com.sun.net.ssl.internal.www.protocol");
 		Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
 		
 		try {
 			Directory dir = DirectoryFactory.getInstance().getDirectory();
 			URL url = dir.getProviderURL(identifier, Directory.TYPE_INDEX_PROVIDER);
 			ip.setURL(url);
 		} catch(DirectoryException e) {
 			fail("Error getting Index Provider URL: " + e.getMessage());
 		}
 
 		
 		DistributedRegistry reg =  DistributedRegistry.singleton();
 		SecurityToken token = null;
 
 		
 		try {
 			token = reg.getAuthentication(SecurityToken.roleFAA);
 		} catch(SignatureException e) {
 			e.printStackTrace();
 			fail("SignatureException: " + e.getMessage());
 		} catch(ParameterException e) {
 			fail("ParameterException: " + e.getMessage());
 		}
 
 		
 		MeteorParameters params = new MeteorParameters();
 		try {
 			params.setRole(SecurityToken.roleFAA);
 			params.setSsn("987654321");
 		} catch(ParameterException e) {
 			fail("ParameterException: " + e.getMessage());
 		}
 		
 		params.setSecurityToken(token);
 		try {
 			ip.getDataProviders(params);
		} catch(IndexException e) {
 			e.printStackTrace();
 			fail("IndexException: " + e.getMessage());
 		}
 		
 	}
 	
 	public void testIPXML(){
 		IndexProvider ip = new IndexProvider();
 		
 		ip.setIdentifier("ED.1234");
 		ip.setName("TestName");
 		try {
 			ip.setURL(new URL("http://www.yahoo.com"));
 		} catch (MalformedURLException e) {
 			fail("MalformedURLException: " + e.getMessage());
 		}
 		
 		log.debug("Index Provider: " + ip);
 	}
 
 }
 
