 /**
  * 
  * Copyright 2002 NCHELP
  * 
  * Author:		Tim Bornholtz, PriorityTechnologies, Inc.
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
 
 import java.io.FileInputStream;
 import java.security.KeyStore;
 import java.security.PrivateKey;
 import java.security.cert.X509Certificate;
 
 import junit.framework.TestCase;
 import junit.textui.TestRunner;
 import org.nchelp.hpc.HPCMessage;
 import org.nchelp.meteor.logging.Logger;
 import org.nchelp.meteor.message.MeteorDataRequest;
 import org.nchelp.meteor.provider.DistributedRegistry;
 import org.nchelp.meteor.provider.data.DataMessageHandler;
 import org.nchelp.meteor.security.SecurityToken;
 
 public class DataMessageHandlerTest extends TestCase {
 
 	private final Logger log = Logger.create(this.getClass());
 	
 	public DataMessageHandlerTest(String name) {
 		super(name);
 	}
 
 	public static void main(String args[]) {
 		TestRunner.run(DataMessageHandlerTest.class);
 	}
 
 	public void testDataMessageHandler() {
 		DistributedRegistry registry = DistributedRegistry.singleton();
 		SecurityToken token = null;
 		try {
 			token = registry.getAuthentication();
 			token.setRole(SecurityToken.roleFAA);
 			
 		} catch(Exception e) {
 			e.printStackTrace();
			fail();
 		}
 
 		MeteorDataRequest req = new MeteorDataRequest();
 		req.setSSN("987654321");
 		req.setSecurityToken(token);
 		
 		HPCMessage mess = new HPCMessage();
 		mess.setRecipientID("TEST");
 		String strMessage = req.toString();
 		mess.setContent(strMessage, "METEORDATA");
 		System.out.println(strMessage);
 		
 		HPCMessage respMess = null;
 		
 		DataMessageHandler dmh = new DataMessageHandler();
 		try {
 			respMess = dmh.handle(mess);
 		} catch(Exception e) {
 			e.printStackTrace();
			fail();
 		}
 		
 		try {
 			System.out.println("Response:" + new String(respMess.getContent()));
 		} catch(Exception e) {
 			e.printStackTrace();
			fail();
 		}
 	}
 	
 
 }
 
 
 
 
 
 
