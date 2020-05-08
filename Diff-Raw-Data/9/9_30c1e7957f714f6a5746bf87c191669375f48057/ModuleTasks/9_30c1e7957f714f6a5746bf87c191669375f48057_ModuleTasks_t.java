 package com.redhat.qe.sm.tasks;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import org.apache.xmlrpc.XmlRpcException;
 
 import com.redhat.qe.auto.testng.BzChecker;
 import com.redhat.qe.auto.testopia.Assert;
 import com.redhat.qe.tools.RemoteFileTasks;
 import com.redhat.qe.tools.SSHCommandResult;
 import com.redhat.qe.tools.SSHCommandRunner;
 import com.redhat.qe.sm.abstractions.EntitlementCert;
 import com.redhat.qe.sm.abstractions.SubscriptionPool;
 import com.redhat.qe.sm.abstractions.InstalledProduct;
 import com.redhat.qe.sm.abstractions.ProductSubscription;
 
 public class ModuleTasks {
 
 	protected static Logger log = Logger.getLogger(ModuleTasks.class.getName());
 	protected /*NOT static*/ SSHCommandRunner sshCommandRunner = null;
 	protected String redhatRepoFile = "/etc/yum.repos.d/redhat.repo";
 	
 
 	public ModuleTasks() {
 		super();
 		// TODO Auto-generated constructor stub
 	}
 	
 	public ModuleTasks(SSHCommandRunner runner) {
 		super();
 		setSSHCommandRunner(runner);
 	}
 	
 	public void setSSHCommandRunner(SSHCommandRunner runner) {
 		sshCommandRunner = runner;
 	}
 	
 	public List<SubscriptionPool> getCurrentlyAvailableSubscriptionPools() {
 		return SubscriptionPool.parse(listAvailable().getStdout());
 	}
 	
 	public List<ProductSubscription> getCurrentlyConsumedProductSubscriptions() {
 		return ProductSubscription.parse(listConsumed().getStdout());
 	}
 	
 	public List<InstalledProduct> getCurrentlyInstalledProducts() {
 		return InstalledProduct.parse(list().getStdout());
 	}
 	
 	public List<EntitlementCert> getCurrentEntitlementCerts() {
 		sshCommandRunner.runCommandAndWait("find /etc/pki/entitlement/product/ -name '*.pem' | xargs -I '{}' openssl x509 -in '{}' -noout -text");
 		String certificates = sshCommandRunner.getStdout();
 		return EntitlementCert.parse(certificates);
 	}
 	
 //	public EntitlementCert getEntitlementCertFromCertFile(String certFile) {
 //		sshCommandRunner.runCommandAndWait("openssl x509 -in "+certFile+" -noout -text");
 //		String certificate = sshCommandRunner.getStdout();
 //		return EntitlementCert.parse(certificate).get(0);
 //	}
 //	public SubscriptionPool getSubscriptionPoolFromCertFile(String certFile) {
 //		sshCommandRunner.runCommandAndWait("openssl x509 -in "+certFile+" -noout -text");
 //		String certificate = sshCommandRunner.getStdout();
 //		return SubscriptionPool.parseCerts(certificate);
 //	}
 //	SubscriptionPool pool = new SubscriptionPool(cert.from_productId, cert.from_poolId);
 	public Map<Integer,SubscriptionPool> getCurrentSerialMapOfSubscriptionPools() {
 		sshCommandRunner.runCommandAndWait("find /etc/pki/entitlement/product/ -name '*.pem' | xargs -I '{}' openssl x509 -in '{}' -noout -text");
 		String certificates = sshCommandRunner.getStdout();
 		return SubscriptionPool.parseCerts(certificates);
 	}
 	
 	public List<String> getCurrentEntitlementCertFiles() {
 		sshCommandRunner.runCommandAndWait("find /etc/pki/entitlement/product/ -name '*.pem'");
 		String files = sshCommandRunner.getStdout().trim();
 		List<String> certFiles = new ArrayList<String>();
 		if (!files.equals("")) certFiles=Arrays.asList(files.split("\n"));
 		return certFiles;
 	}
 	
 	public HashMap<String,String[]> getPackagesCorrespondingToSubscribedRepos(){
 		int min = 3;
 		sshCommandRunner.runCommandAndWait("killall -9 yum");
 		log.info("timeout of "+min+" minutes for next command");
 		sshCommandRunner.runCommandAndWait("yum list available",Long.valueOf(min*60000));
 		HashMap<String,String[]> pkgMap = new HashMap<String,String[]>();
 		
 		String[] packageLines = sshCommandRunner.getStdout().split("\\n");
 		
 		int pkglistBegin = 0;
 		
 		for(int i=0;i<packageLines.length;i++){
 			pkglistBegin++;
 			if(packageLines[i].contains("Available Packages"))
 				break;
 		}
 		
 		for(ProductSubscription sub : getCurrentlyConsumedProductSubscriptions()){
 			ArrayList<String> pkgList = new ArrayList<String>();
 			for(int i=pkglistBegin;i<packageLines.length;i++){
 				String[] splitLine = packageLines[i].split(" ");
 				String pkgName = splitLine[0];
 				String repoName = splitLine[splitLine.length - 1];
 				if(repoName.toLowerCase().contains(sub.productName.toLowerCase()))
 					pkgList.add(pkgName);
 			}
 			pkgMap.put(sub.productName, (String[])pkgList.toArray());
 		}
 		
 		return pkgMap;
 	}
 
 	public SubscriptionPool getSubscriptionPoolFromProductSubscription(ProductSubscription productSubscription) {
 		
 		// if already known, return the SubscriptionPool from which ProductSubscription came
 		if (productSubscription.fromPool != null) return productSubscription.fromPool;
 		
 		productSubscription.fromPool = getCurrentSerialMapOfSubscriptionPools().get(productSubscription.serialNumber);
 
 		return productSubscription.fromPool;
 	}
 	
 	public List<EntitlementCert> getEntitlementCertsFromProductSubscription(ProductSubscription productSubscription) {
 		String certFile = "/etc/pki/entitlement/product/"+productSubscription.serialNumber+".pem";
 		sshCommandRunner.runCommandAndWait("openssl x509 -in '"+certFile+"' -noout -text");
 		String certificates = sshCommandRunner.getStdout();
 		List<EntitlementCert> entitlementCerts = EntitlementCert.parse(certificates);
 		return entitlementCerts;
 	}
 	
 	// register module tasks ************************************************************
 	
 	/**
 	 * register without asserting results
 	 */
 	public SSHCommandResult register_(String username, String password, String type, String consumerId, Boolean autosubscribe, Boolean force) {
 
 		// assemble the register command
 		String										command  = "subscription-manager-cli register";	
 		if (username!=null)							command += " --username="+username;
 		if (password!=null)							command += " --password="+password;
 		if (type!=null)								command += " --type="+type;
 		if (consumerId!=null)						command += " --consumerid="+consumerId;
 		if (autosubscribe!=null && autosubscribe)	command += " --autosubscribe";
 		if (force!=null && force)					command += " --force";
 		
 		// register without asserting results
 		return sshCommandRunner.runCommandAndWait(command);
 	}
 	
 	/**
 	 * @param username
 	 * @param password
 	 * @param type <br>
 	 * <i>system</i>		Used for example registering a plain RHEL machine (Default)<br>
 	 * <i>person</i>		Used for registering as a RH Personal<br>
 	 * <i>domain</i>		Used for IPA tests<br>
 	 * <i>candlepin</i>		Used for a connected Candlepin, export tests<br>
 	 * @param consumerId
 	 * @param autosubscribe
 	 * @param force
 	 */
 	public SSHCommandResult register(String username, String password, String type, String consumerId, Boolean autosubscribe, Boolean force) {
 		
 		SSHCommandResult sshCommandResult = register_(username, password, type, consumerId, autosubscribe, force);
 
 		// assert results for a successful registration
		if (sshCommandResult.getStdout().startsWith("This system is already registered.")) return sshCommandResult;
 		Assert.assertEquals(sshCommandResult.getExitCode(), Integer.valueOf(0), "The register command was a success.");
 		Assert.assertContainsMatch(sshCommandResult.getStdout().trim(), "[a-f,0-9,\\-]{36} "+username);
 		
 		// FIXME: may want to assert this output and save or return it.  - jsefler 7/8/2010
 		// Stdout: 3f92221c-4b26-4e49-96af-b31abd7bd28c admin admin
 		// FIXME: should assert stdout: SYSTEM_UUID  USER_SET_SYSTEM_NAME  USERNAME_OF_REGISTERER
 		// ([a-z,0-9,\-]+) (admin) (XEOPS|xeops)
 		// https://bugzilla.redhat.com/show_bug.cgi?id=616065
 		// Stdout: 3f92221c-4b26-4e49-96af-b31abd7bd28c admin
 		// ([a-f,0-9,\-]{36}) (admin)
 		
 		// assert certificate files are dropped into /etc/pki/consumer
 		String consumerKeyFile = "/etc/pki/consumer/key.pem";
 		Assert.assertEquals(sshCommandRunner.runCommandAndWait("stat "+consumerKeyFile).getExitCode().intValue(), 0, consumerKeyFile+" is present after register");
 		String consumerCertFile = "/etc/pki/consumer/cert.pem";
 		Assert.assertEquals(sshCommandRunner.runCommandAndWait("stat "+consumerCertFile).getExitCode().intValue(), 0, consumerCertFile+" is present after register");
 		
 // Moved to ValidRegistration_Test()   UPDATE moved back ^
 //		Assert.assertEquals(
 //				sshCommandRunner.runCommandAndWait("stat /etc/pki/consumer/key.pem").intValue(), 0,
 //						"/etc/pki/consumer/key.pem is present after register");
 //		Assert.assertEquals(
 //				sshCommandRunner.runCommandAndWait("stat /etc/pki/consumer/cert.pem").intValue(),0,
 //						"/etc/pki/consumer/cert.pem is present after register");
 		return sshCommandResult; // from the register command
 	}
 	
 
 	
 //	public void registerToCandlepin(String username, String password){
 //		// FIXME may want to make force an optional arg
 //		sshCommandRunner.runCommandAndWait("subscription-manager-cli register --username="+username+" --password="+password + " --force");
 //
 //		Assert.assertEquals(
 //				sshCommandRunner.runCommandAndWait("stat /etc/pki/consumer/key.pem").intValue(), 0,
 //						"/etc/pki/consumer/key.pem is present after register");
 //		Assert.assertEquals(
 //				sshCommandRunner.runCommandAndWait("stat /etc/pki/consumer/cert.pem").intValue(),0,
 //						"/etc/pki/consumer/cert.pem is present after register");
 //	}
 //	
 //	public void registerToCandlepinAutosubscribe(String username, String password){
 //		sshCommandRunner.runCommandAndWait("subscription-manager-cli register --username="+username+" --password="+password + " --force --autosubscribe");
 //		Assert.assertEquals(
 //				sshCommandRunner.runCommandAndWait("stat /etc/pki/consumer/key.pem").intValue(),0,
 //						"/etc/pki/consumer/key.pem is present after register");
 //		Assert.assertEquals(
 //				sshCommandRunner.runCommandAndWait("stat /etc/pki/consumer/cert.pem").intValue(),0,
 //						"/etc/pki/consumer/cert.pem is present after register");
 //	}
 	
 	
 	// unregister module tasks ************************************************************
 
 	/**
 	 * unregister without asserting results
 	 */
 	public SSHCommandResult unregister_() {
 
 		// assemble the unregister command
 		String command  = "subscription-manager-cli unregister";	
 		
 		// register without asserting results
 		return sshCommandRunner.runCommandAndWait(command);
 	}
 	
 	/**
 	 * "subscription-manager-cli unregister"
 	 */
 	public SSHCommandResult unregister() {
 		SSHCommandResult sshCommandResult = unregister_();
 		
 		// assert results for a successful registration
 		if (sshCommandResult.getStdout().startsWith("This system is currently not registered.")) return sshCommandResult;
 		Assert.assertEquals(sshCommandResult.getExitCode(), Integer.valueOf(0), "The unregister command was a success.");
 		
 		// assert that the consumer cert and key have been removed
 		RemoteFileTasks.runCommandExpectingNonzeroExit(sshCommandRunner,"ls /etc/pki/entitlement/product | grep pem");
 //		RemoteFileTasks.runCommandExpectingNonzeroExit(sshCommandRunner,"stat /etc/pki/consumer/key.pem");
 //		RemoteFileTasks.runCommandExpectingNonzeroExit(sshCommandRunner,"stat /etc/pki/consumer/cert.pem");
 //		Assert.assertEquals(RemoteFileTasks.testFileExists(sshCommandRunner, "/etc/pki/consumer/key.pem"),0,"The identify key has been removed after unregistering.");
 //		Assert.assertEquals(RemoteFileTasks.testFileExists(sshCommandRunner, "/etc/pki/consumer/cert.pem"),0,"The identify certificate has been removed after unregistering.");
 		Assert.assertEquals(sshCommandRunner.runCommandAndWait("stat /etc/pki/consumer/cert.pem").getExitCode().intValue(),1,"The identify certificate has been removed after unregistering.");
 		Assert.assertEquals(sshCommandRunner.runCommandAndWait("stat /etc/pki/consumer/key.pem").getExitCode().intValue(),1,"The identify key has been removed after unregistering.");
 		
 		return sshCommandResult; // from the unregister command
 	}
 	
 	// list module tasks ************************************************************
 	
 	/**
 	 * @return SSHCommandResult from "subscription-manager-cli list --consumed"
 	 */
 	public SSHCommandResult listConsumed() {
 		return RemoteFileTasks.runCommandExpectingNoTracebacks(sshCommandRunner,"subscription-manager-cli list --consumed");
 	}
 	
 	/**
 	 * @return SSHCommandResult from "subscription-manager-cli list --available"
 	 */
 	public SSHCommandResult listAvailable() {
 		return RemoteFileTasks.runCommandExpectingNoTracebacks(sshCommandRunner,"subscription-manager-cli list --available");
 	}
 	
 	/**
 	 * @return SSHCommandResult from "subscription-manager-cli list"
 	 */
 	public SSHCommandResult list() {
 		return RemoteFileTasks.runCommandExpectingNoTracebacks(sshCommandRunner,"subscription-manager-cli list");
 	}
 	
 	// subscribe module tasks ************************************************************
 
 	/**
 	 * subscribe without asserting results
 	 */
 	public SSHCommandResult subscribe_(String poolId, String productId, String regtoken, String email, String locale) {
 		
 		// assemble the subscribe command
 		String					command  = "subscription-manager-cli subscribe";	
 		if (poolId!=null)		command += " --pool="+poolId;
 		if (productId!=null)	command += " --product="+productId;
 		if (regtoken!=null)		command += " --regtoken="+regtoken;
 		if (email!=null)		command += " --email="+email;
 		if (locale!=null)		command += " --locale="+locale;
 		
 		// subscribe without asserting results
 		return sshCommandRunner.runCommandAndWait(command);
 	}
 
 	public SSHCommandResult subscribe(String poolId, String productId, String regtoken, String email, String locale) {
 
 		SSHCommandResult sshCommandResult = subscribe_(poolId, productId, regtoken, email, locale);
 		
 		// assert results
 		if (sshCommandResult.getStderr().startsWith("This consumer is already subscribed")) return sshCommandResult;
 		Assert.assertEquals(sshCommandResult.getExitCode(), Integer.valueOf(0), "The subscribe command was a success.");
 		return sshCommandResult;
 	}
 	
 	/**
 	 * subscribe without asserting results
 	 */
 	public SSHCommandResult subscribe_(List<String> poolIds, List<String> productIds, List<String> regtokens, String email, String locale) {
 
 		// assemble the subscribe command
 		String														command  = "subscription-manager-cli subscribe";	
 		if (poolIds!=null)		for (String poolId : poolIds)		command += " --pool="+poolId;
 		if (productIds!=null)	for (String productId : productIds)	command += " --product="+productId;
 		if (regtokens!=null)	for (String regtoken : regtokens)	command += " --regtoken="+regtoken;
 		if (email!=null)											command += " --email="+email;
 		if (locale!=null)											command += " --locale="+locale;
 
 		// subscribe without asserting results
 		return sshCommandRunner.runCommandAndWait(command);
 	}
 	
 	public SSHCommandResult subscribe(List<String> poolIds, List<String> productIds, List<String> regtokens, String email, String locale) {
 
 		SSHCommandResult sshCommandResult = subscribe_(poolIds, productIds, regtokens, email, locale);
 		
 		// assert results
 		if (sshCommandResult.getStderr().startsWith("This consumer is already subscribed")) return sshCommandResult;
 		Assert.assertEquals(sshCommandResult.getExitCode(), Integer.valueOf(0), "The subscribe command was a success.");
 		return sshCommandResult;
 	}
 	
 	public void subscribeToProduct(String product) {
 		RemoteFileTasks.runCommandExpectingNonzeroExit(sshCommandRunner,"subscription-manager-cli subscribe --product="+product);
 	}
 	
 	public void subscribeToSubscriptionPoolUsingPoolId(SubscriptionPool pool) {
 		List<ProductSubscription> beforeProductSubscriptions = getCurrentlyConsumedProductSubscriptions();
 		log.info("Subscribing to subscription pool: "+pool);
 		subscribe(pool.poolId, null, null, null, null);
 
 		// assert that consumed ProductSubscriptions has NOT decreased
 		List<ProductSubscription> afterProductSubscriptions = getCurrentlyConsumedProductSubscriptions();
 		Assert.assertTrue(afterProductSubscriptions.size() >= beforeProductSubscriptions.size() && afterProductSubscriptions.size() > 0,
 				"The list of currently consumed product subscriptions has increased (from "+beforeProductSubscriptions.size()+" to "+afterProductSubscriptions.size()+"), or has remained the same after subscribing (using poolID="+pool.poolId+") to pool: "+pool+"  Note: The list of consumed product subscriptions can remain the same when all the products from this subscription pool are a subset of those from a previously subscribed pool.");
 
 		// assert that the remaining SubscriptionPools does NOT contain the pool just subscribed to
 		List<SubscriptionPool> afterSubscriptionPools = getCurrentlyAvailableSubscriptionPools();
 		Assert.assertTrue(!afterSubscriptionPools.contains(pool),
 				"The available subscription pools no longer contains pool: "+pool);
 		
 		// assert that the remaining SubscriptionPools do NOT contain the same productId just subscribed to
 		for (SubscriptionPool afterSubscriptionPool : afterSubscriptionPools) {
 			Assert.assertTrue(!afterSubscriptionPool.productId.equals(pool.productId),
 					"After subscribing to pool "+pool+", this remaining available pool "+afterSubscriptionPool+" does NOT contain the already subscribed to product "+pool.productId+".");
 		}
 	}
 	
 	public void subscribeToSubscriptionPoolUsingProductId(SubscriptionPool pool) {
 		log.warning("Subscribing to a Subscription Pool using --product Id has been removed in subscription-manager-0.71-1.el6.i686.  Forwarding this subscribe request to use --pool Id...");
 		subscribeToSubscriptionPoolUsingPoolId(pool); return;
 		
 		/* jsefler 7/22/2010
 		List<ProductSubscription> before = getCurrentlyConsumedProductSubscriptions();
 		log.info("Subscribing to subscription pool: "+pool);
 		subscribe(null, pool.productId, null, null, null);
 		String stderr = sshCommandRunner.getStderr().trim();
 		
 		List<ProductSubscription> after = getCurrentlyConsumedProductSubscriptions();
 		if (stderr.equals("This consumer is already subscribed to the product '"+pool.productId+"'")) {
 			Assert.assertTrue(after.size() == before.size() && after.size() > 0,
 					"The list of currently consumed product subscriptions has remained the same (from "+before.size()+" to "+after.size()+") after subscribing (using productID="+pool.productId+") to pool: "+pool+"   Note: The list of consumed product subscriptions can remain the same when this product is already a subset from a previously subscribed pool.");
 		} else {
 			Assert.assertTrue(after.size() >= before.size() && after.size() > 0,
 					"The list of currently consumed product subscriptions has increased (from "+before.size()+" to "+after.size()+"), or has remained the same after subscribing (using productID="+pool.productId+") to pool: "+pool+"  Note: The list of consumed product subscriptions can remain the same when this product is already a subset from a previously subscribed pool.");
 			Assert.assertTrue(!getCurrentlyAvailableSubscriptionPools().contains(pool),
 					"The available subscription pools no longer contains pool: "+pool);
 		}
 		*/
 	}
 	
 	public void subscribeToSubscriptionPoolUsingPoolId(SubscriptionPool pool, boolean withPoolID){
 		log.info("Subscribing to subscription pool: "+ pool);
 		if(withPoolID){
 			log.info("Subscribing to pool with pool ID:"+ pool.subscriptionName);
 			sshCommandRunner.runCommandAndWait("subscription-manager-cli subscribe --pool="+pool.poolId);
 		}
 		else{
 			log.info("Subscribing to pool with pool name:"+ pool.subscriptionName);
 			sshCommandRunner.runCommandAndWait("subscription-manager-cli subscribe --product=\""+pool.productId+"\"");
 		}
 		Assert.assertTrue(getCurrentlyConsumedProductSubscriptions().size() > 0, "Successfully subscribed to pool with pool ID: "+ pool.poolId +" and pool name: "+ pool.subscriptionName);
 		//TODO: add in more thorough product subscription verification
 		// first improvement is to assert that the count of consumedProductIDs is at least one greater than the count of consumedProductIDs before the new pool was subscribed to.
 	}
 	
 	public void subscribeToRegToken(String regtoken) {
 		log.info("Subscribing to registration token: "+ regtoken);
 		RemoteFileTasks.runCommandExpectingNoTracebacks(sshCommandRunner, "subscription-manager-cli subscribe --regtoken="+regtoken);
 		Assert.assertTrue((getCurrentlyConsumedProductSubscriptions().size() > 0),
 				"At least one entitlement consumed by regtoken subscription");
 	}
 	
 	/**
 	 * Individually subscribe to each of the currently available subscription pools one at a time
 	 */
 	public void subscribeToEachOfTheCurrentlyAvailableSubscriptionPools() {
 
 		// individually subscribe to each available subscription pool
 		for (SubscriptionPool pool : getCurrentlyAvailableSubscriptionPools()) {
 			subscribeToSubscriptionPoolUsingPoolId(pool);
 		}
 		
 		// TEMPORARY WORKAROUND FOR BUG: https://bugzilla.redhat.com/show_bug.cgi?id=613635 - jsefler 7/14/2010
 		boolean invokeWorkaroundWhileBugIsOpen = true;
 		try {String bugId="613635"; if (BzChecker.getInstance().isBugOpen(bugId)&&invokeWorkaroundWhileBugIsOpen) {log.fine("Invoking workaround for "+BzChecker.getInstance().getBugState(bugId).toString()+" Bugzilla bug "+bugId+".  (https://bugzilla.redhat.com/show_bug.cgi?id="+bugId+")");} else {invokeWorkaroundWhileBugIsOpen=false;}} catch (XmlRpcException xre) {/* ignore exception */} catch (RuntimeException re) {/* ignore exception */}
 		if (invokeWorkaroundWhileBugIsOpen) {
 			Assert.assertContainsMatch(listAvailable().getStdout(),"^No Available subscription pools to list$","Asserting that no available subscription pools remain after individually subscribing to them all.");
 			return;
 		} // END OF WORKAROUND
 		
 		
 		// TEMPORARY WORKAROUND FOR BUG: https://bugzilla.redhat.com/show_bug.cgi?id=622839 - jsefler 8/10/2010
 		invokeWorkaroundWhileBugIsOpen = true;
 		try {String bugId="622839"; if (BzChecker.getInstance().isBugOpen(bugId)&&invokeWorkaroundWhileBugIsOpen) {log.fine("Invoking workaround for "+BzChecker.getInstance().getBugState(bugId).toString()+" Bugzilla bug "+bugId+".  (https://bugzilla.redhat.com/show_bug.cgi?id="+bugId+")");} else {invokeWorkaroundWhileBugIsOpen=false;}} catch (XmlRpcException xre) {/* ignore exception */} catch (RuntimeException re) {/* ignore exception */}
 		if (invokeWorkaroundWhileBugIsOpen) {
 			Assert.assertContainsMatch(listAvailable().getStdout(),"^No Available subscription pools to list$","Asserting that no available subscription pools remain after individually subscribing to them all.");
 			return;
 		} // END OF WORKAROUND
 		
 		Assert.assertEquals(listAvailable().getStdout().trim(),
 				"No Available subscription pools to list","Asserting that no available subscription pools remain after individually subscribing to them all.");
 	}
 	
 	
 	/**
 	 * Collectively subscribe to all of the currently available subscription pools in one command call
 	 */
 	public void subscribeToAllOfTheCurrentlyAvailableSubscriptionPools() {
 
 		// assemble a list of all the available SubscriptionPool ids
 		List <String> poolIds = new ArrayList<String>();
 		for (SubscriptionPool pool : getCurrentlyAvailableSubscriptionPools()) {
 			poolIds.add(pool.poolId);
 		}
 		if (!poolIds.isEmpty()) subscribe(poolIds, null, null, null, null);
 		
 		// TEMPORARY WORKAROUND FOR BUG: https://bugzilla.redhat.com/show_bug.cgi?id=613635 - jsefler 7/14/2010
 		boolean invokeWorkaroundWhileBugIsOpen = true;
 		try {String bugId="613635"; if (BzChecker.getInstance().isBugOpen(bugId)&&invokeWorkaroundWhileBugIsOpen) {log.fine("Invoking workaround for "+BzChecker.getInstance().getBugState(bugId).toString()+" Bugzilla bug "+bugId+".  (https://bugzilla.redhat.com/show_bug.cgi?id="+bugId+")");} else {invokeWorkaroundWhileBugIsOpen=false;}} catch (XmlRpcException xre) {/* ignore exception */} catch (RuntimeException re) {/* ignore exception */}
 		if (invokeWorkaroundWhileBugIsOpen) {
 			Assert.assertContainsMatch(listAvailable().getStdout(),"^No Available subscription pools to list$","Asserting that no available subscription pools remain after simultaneously subscribing to them all.");
 			return;
 		} // END OF WORKAROUND
 		
 		// TEMPORARY WORKAROUND FOR BUG: https://bugzilla.redhat.com/show_bug.cgi?id=622839 - jsefler 8/10/2010
 		invokeWorkaroundWhileBugIsOpen = true;
 		try {String bugId="622839"; if (BzChecker.getInstance().isBugOpen(bugId)&&invokeWorkaroundWhileBugIsOpen) {log.fine("Invoking workaround for "+BzChecker.getInstance().getBugState(bugId).toString()+" Bugzilla bug "+bugId+".  (https://bugzilla.redhat.com/show_bug.cgi?id="+bugId+")");} else {invokeWorkaroundWhileBugIsOpen=false;}} catch (XmlRpcException xre) {/* ignore exception */} catch (RuntimeException re) {/* ignore exception */}
 		if (invokeWorkaroundWhileBugIsOpen) {
 			Assert.assertContainsMatch(listAvailable().getStdout(),"^No Available subscription pools to list$","Asserting that no available subscription pools remain after individually subscribing to them all.");
 			return;
 		} // END OF WORKAROUND
 		
 		Assert.assertEquals(listAvailable().getStdout().trim(),
 				"No Available subscription pools to list","Asserting that no available subscription pools remain after simultaneously subscribing to them all.");
 	}
 	
 	// unsubscribe module tasks ************************************************************
 
 	/**
 	 * Issues a call to "subscription-manager-cli unsubscribe" which will unsubscribe from
 	 * all currently consumed product subscriptions and then asserts the list --consumed is empty.
 	 */
 	public void unsubscribeFromAllOfTheCurrentlyConsumedProductSubscriptions() {
 		RemoteFileTasks.runCommandExpectingNoTracebacks(sshCommandRunner,"subscription-manager-cli unsubscribe");
 
 		// assert that there are no product subscriptions consumed
 		Assert.assertEquals(listConsumed().getStdout().trim(),
 				"No Consumed subscription pools to list","Successfully unsubscribed from all consumed products.");
 		
 		// assert that there are no entitlement product cert files
 		Assert.assertTrue(sshCommandRunner.runCommandAndWait("find /etc/pki/entitlement/product/ -name '*.pem'").getStdout().equals(""),
 				"No entitlement product cert files exist after unsubscribing from all subscription pools.");
 		
 		// assert that the yum redhat repo file is gone
 		Assert.assertTrue(RemoteFileTasks.testFileExists(sshCommandRunner, redhatRepoFile)==1,
 				"The redhat repo file '"+redhatRepoFile+"' has been removed after unsubscribing from all subscription pools.");
 	}
 	
 	/**
 	 * Individually unsubscribe from each of the currently consumed product subscriptions.
 	 */
 	public void unsubscribeFromEachOfTheCurrentlyConsumedProductSubscriptions() {
 		log.info("Unsubscribing from each of the currently consumed product subscriptions...");
 		for(ProductSubscription sub : getCurrentlyConsumedProductSubscriptions())
 			unsubscribeFromProductSubscription(sub);
 		Assert.assertTrue(getCurrentlyConsumedProductSubscriptions().size()==0,
 				"Currently no product subscriptions are consumed.");
 		Assert.assertTrue(getCurrentEntitlementCertFiles().size()==0,
 				"This machine has no entitlement certificate files.");			
 	}
 	
 	/**
 	 * Unsubscribe from the given product subscription using its serial number.
 	 * @param productSubscription
 	 * @return - false when the productSubscription has already been unsubscribed at a previous time
 	 */
 	public boolean unsubscribeFromProductSubscription(ProductSubscription productSubscription) {
 		String certFile = "/etc/pki/entitlement/product/"+productSubscription.serialNumber+".pem";
 		boolean certFileExists = RemoteFileTasks.testFileExists(sshCommandRunner,certFile)==1? true:false;
 		
 		log.info("Unsubscribing from product subscription: "+ productSubscription);
 		sshCommandRunner.runCommandAndWait("subscription-manager-cli unsubscribe --serial="+productSubscription.serialNumber);
 		
 		if (certFileExists) {
 			// assert that the cert file was removed
 //			Assert.assertTrue(RemoteFileTasks.testFileExists(sshCommandRunner,certFile)==0,
 //					"After unsubscribing from serial number "+productSubscription.serialNumber+", the entitlement cert file '"+certFile+"' has been removed.");
 			Assert.assertTrue(!getCurrentEntitlementCertFiles().contains(certFile),
 					"After unsubscribing from serial number "+productSubscription.serialNumber+", the entitlement cert file '"+certFile+"' has been removed.");
 		} else {
 			// assert an error message when the product subscription was not found
 			// Example Stderr: Entitlement Certificate with serial number 301 could not be found.
 			Assert.assertEquals(sshCommandRunner.getStderr().trim(), "Entitlement Certificate with serial number "+productSubscription.serialNumber+" could not be found.",
 					"When the entitlement cert file corresponding to a product subscription does not exist, then you cannot unsubscribe from it.");
 		}
 		
 		Assert.assertTrue(!getCurrentlyConsumedProductSubscriptions().contains(productSubscription),
 				"The currently consumed product subscriptions does not contain product: "+productSubscription);
 
 		return certFileExists;
 	}
 	
 	
 	
 	
 	/**
 	 * Assert that the given entitlement certs are displayed in the stdout from "yum repolist all".
 	 * @param entitlementCerts
 	 */
 	public void assertEntitlementCertsAreReportedInYumRepolist(List<EntitlementCert> entitlementCerts) {
 		/* # yum repolist all
 Loaded plugins: refresh-packagekit, rhnplugin, rhsmplugin
 Updating Red Hat repositories.
 This system is not registered with RHN.
 RHN support will be disabled.
 http://redhat.com/foo/path/never/repodata/repomd.xml: [Errno 14] HTTP Error 404 : http://www.redhat.com/foo/path/never/repodata/repomd.xml 
 Trying other mirror.
 repo id                      repo name                                                      status
 always-enabled-content       always-enabled-content                                         disabled
 content-label                content                                                        disabled
 never-enabled-content        never-enabled-content                                          enabled: 0
 rhel-beta                    Red Hat Enterprise Linux 5.90Workstation Beta - x86_64         disabled
 rhel-beta-debuginfo          Red Hat Enterprise Linux 5.90Workstation Beta - x86_64 - Debug disabled
 rhel-beta-optional           Red Hat Enterprise Linux 5.90Workstation Beta (Optional) - x86 disabled
 rhel-beta-optional-debuginfo Red Hat Enterprise Linux 5.90Workstation Beta (Optional) - x86 disabled
 rhel-beta-optional-source    Red Hat Enterprise Linux 5.90Workstation Beta (Optional) - x86 disabled
 rhel-beta-source             Red Hat Enterprise Linux 5.90Workstation Beta - x86_64 - Sourc disabled
 rhel-latest                  Latest RHEL 6                                                  enabled: 0
 repolist: 0
 		*/
 		
 		// assert all of the entitlement certs are displayed in the stdout from "yum repolist all"
 		List<String> stdoutRegexs = new ArrayList<String>();
 		for (EntitlementCert entitlementCert : entitlementCerts) {
 			//stdoutRegexs.add(String.format("^%s\\s+%s\\s+%s", entitlementCert.label.trim(), entitlementCert.name.trim(), entitlementCert.enabled.equals("1")? "enabled":"disabled"));
 			stdoutRegexs.add(String.format("^%s\\s+(?:%s|.*)\\s+%s", entitlementCert.label.trim(), entitlementCert.name.substring(0,Math.min(entitlementCert.name.length(), 25)), entitlementCert.enabled.equals("1")? "enabled":"disabled"));	// 25 was arbitraily picked to be short enough to be displayed by yum repolist all
 		}
 		RemoteFileTasks.runCommandAndAssert(sshCommandRunner, "yum repolist all", 0, stdoutRegexs, null);
 		// FIXME: may want to also assert that the sshCommandRunner.getStderr() does not contains an error on the entitlementCert.download_url e.g.: http://redhat.com/foo/path/never/repodata/repomd.xml: [Errno 14] HTTP Error 404 : http://www.redhat.com/foo/path/never/repodata/repomd.xml 
 		 
 	}
 	
 	// protected methods ************************************************************
 
 	protected boolean poolsNoLongerAvailable(ArrayList<SubscriptionPool> beforeSubscription, ArrayList<SubscriptionPool> afterSubscription) {
 		for(SubscriptionPool beforePool:beforeSubscription)
 			if (afterSubscription.contains(beforePool))
 				return false;
 		return true;
 	}
 	
 
 }
