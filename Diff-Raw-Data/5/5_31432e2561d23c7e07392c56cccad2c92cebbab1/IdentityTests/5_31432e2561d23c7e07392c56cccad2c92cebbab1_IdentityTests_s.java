 package com.redhat.qe.sm.cli.tests;
 
 import org.testng.annotations.Test;
 
 import com.redhat.qe.auto.testng.Assert;
 import com.redhat.qe.sm.base.SubscriptionManagerCLITestScript;
 import com.redhat.qe.sm.data.ConsumerCert;
 import com.redhat.qe.tools.SSHCommandResult;
 
 /**
  * @author jsefler
  *
  *
 
 THIS IS AN EMAIL FROM bkearney@redhat.com INTRODUCING identity
 
 Per Jesus' suggestion, I rolled in the move of re-register to an
 identity command. It will now do the following:
 
 subscription-manager-cli identity
 Spit out the current identity
 
 subscription-manager-cli identity --regenerate
 Create a new certificated based on the UUID in the current cert, and
 useing the cert as authenticatoin
 
 subscription-manager-cli identity --regenerate --username foo --password bar
 Create a new certificated based on the UUID in the current cert, and
 using the username/password as authentication
 
 -- bk
  */
 @Test(groups={"identity"})
 public class IdentityTests extends SubscriptionManagerCLITestScript {
 
 	
 	
 	@Test(	description="subscription-manager-cli: identity",
 			groups={},
 			enabled=true)
 	//@ImplementsTCMS(id="")
 	public void Identity_Test() {
 		
 		// start fresh by unregistering and registering
 		clienttasks.unregister();
 		String consumerId = clienttasks.getCurrentConsumerId(clienttasks.register(clientusername,clientpassword,null,null,null,null, null));
 		
 		// get the current identity
 		SSHCommandResult result = clienttasks.identity(null, null, null);
 		
 		// assert the current identity matches what was returned from register
 		Assert.assertEquals(result.getStdout().trim(), "Current identity is "+consumerId);
 	}
 	
 	
 	@Test(	description="subscription-manager-cli: identity regenerate",
 			groups={},
 			enabled=true)
 	//@ImplementsTCMS(id="")
 	public void IdentityRegenerate_Test() {
 		
 		// start fresh by unregistering and registering
 		clienttasks.unregister();
 		SSHCommandResult registerResult = clienttasks.register(clientusername,clientpassword,null,null,null,null, null);
 		ConsumerCert origConsumerCert = clienttasks.getCurrentConsumerCert();
 		
 		// regenerate the identity... and assert
 		log.info("regenerating identity using the current cert for authentication...");
 		SSHCommandResult result = clienttasks.identity(null,null,Boolean.TRUE);
 		Assert.assertEquals(result.getStdout().trim(), registerResult.getStdout().trim(),
 				"The original registered result is returned from identity regenerate with original authenticator.");
 		
 		// also assert that the newly regenerated cert matches but is newer than the original cert
 		log.info("also asserting that the newly regenerated cert matches but is newer than original cert...");
 		ConsumerCert newConsumerCert = clienttasks.getCurrentConsumerCert();
 		Assert.assertEquals(newConsumerCert.consumerid, origConsumerCert.consumerid, "The consumerids are a match.");
 		Assert.assertEquals(newConsumerCert.issuer, origConsumerCert.issuer, "The issuers are a match.");
 		Assert.assertEquals(newConsumerCert.username, origConsumerCert.username, "The usernames are a match.");
 		Assert.assertEquals(newConsumerCert.validityNotAfter, origConsumerCert.validityNotAfter, "The validity end dates are a match.");
 		Assert.assertTrue(newConsumerCert.validityNotBefore.after(origConsumerCert.validityNotBefore), "The new validity start date is after the original.");
 		Assert.assertNotSame(newConsumerCert.serialNumber, origConsumerCert.serialNumber, "The serial numbers should not match.");
 	}
 	
 	
 	@Test(	description="subscription-manager-cli: identity regenerate with valid username and password",
 			groups={},
 			enabled=true)
 	//@ImplementsTCMS(id="")
 	public void IdentityRegenerateWithValidUsernameAndPasword_Test() {
 		
 		// start fresh by unregistering and registering
 		clienttasks.unregister();
 		SSHCommandResult registerResult = clienttasks.register(client1username,client1password,null,null,null,null, null);
 		
 		// regenerate the identity using the same username and password as used during register... and assert
 		log.info("regenerating identity with the same username and password as used during register...");
 		SSHCommandResult result = clienttasks.identity(client1username,client1password,Boolean.TRUE);
 		Assert.assertEquals(result.getStdout().trim(), registerResult.getStdout().trim(),
 				"The original registered result is returned from identity regenerate with original authenticator.");
 		
 		// regenerate the identity using a different username and password as used during register... and assert
		log.info("Calling subscription-manager-cli identity with the a different username and password used during register...");
 		result = clienttasks.identity(client2username,client2password,Boolean.TRUE);
 		Assert.assertEquals(result.getStdout().trim(), registerResult.getStdout().trim(),
			"The original registered result is returned from identity regenerate with a different but valid authenticator.");
 	}
 	
 	
 	@Test(	description="subscription-manager-cli: identity regenerate with invalid username and password",
 			groups={},
 			enabled=true)
 	//@ImplementsTCMS(id="")
 	public void IdentityRegenerateWithInvalidUsernameAndPasword_Test() {
 		
 		// start fresh by unregistering and registering
 		clienttasks.unregister();
 		clienttasks.register(clientusername,clientpassword,null,null,null,null, null);
 		
 		// retrieve the identity using the same username and password as used during register... and assert
 		log.info("Calling subscription-manager-cli identity with an invalid username and password...");
 		SSHCommandResult result = clienttasks.identity_("FOO","BAR",Boolean.TRUE);
 		Assert.assertNotSame(result.getExitCode(), Integer.valueOf(0), "The identify command was NOT a success.");
 		//Assert.assertEquals(result.getStderr().trim(),"Invalid username or password");	// works against on-premises, not hosted
 		Assert.assertTrue(result.getStderr().trim().startsWith("Invalid username or password"),"Invalid username or password");
 	}
 	
 	
 	
 	
 	
 	// Configuration methods ***********************************************************************
 	
 
 	
 	// Protected methods ***********************************************************************
 
 
 	
 	// Data Providers ***********************************************************************
 
 
 }
