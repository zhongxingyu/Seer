 package com.redhat.qe.sm.cli.tasks;
 
 import java.io.File;
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.xmlrpc.XmlRpcException;
 import org.json.JSONObject;
 
 import com.redhat.qe.auto.testng.Assert;
 import com.redhat.qe.auto.testng.BzChecker;
 import com.redhat.qe.sm.base.ConsumerType;
 import com.redhat.qe.sm.base.SubscriptionManagerCLITestScript;
 import com.redhat.qe.sm.data.ConsumerCert;
 import com.redhat.qe.sm.data.ContentNamespace;
 import com.redhat.qe.sm.data.EntitlementCert;
 import com.redhat.qe.sm.data.InstalledProduct;
 import com.redhat.qe.sm.data.ProductCert;
 import com.redhat.qe.sm.data.ProductSubscription;
 import com.redhat.qe.sm.data.SubscriptionPool;
 import com.redhat.qe.tools.RemoteFileTasks;
 import com.redhat.qe.tools.SSHCommandResult;
 import com.redhat.qe.tools.SSHCommandRunner;
 
 /**
  * @author jsefler
  *
  */
 public class SubscriptionManagerTasks {
 
 	protected static Logger log = Logger.getLogger(SubscriptionManagerTasks.class.getName());
 	protected /*NOT static*/ SSHCommandRunner sshCommandRunner = null;
 	public final String command				= "subscription-manager";
 	public final String redhatRepoFile		= "/etc/yum.repos.d/redhat.repo";
 	public final String rhsmConfFile		= "/etc/rhsm/rhsm.conf";
 	public final String rhsmcertdLogFile	= "/var/log/rhsm/rhsmcertd.log";
 	public final String rhsmLogFile			= "/var/log/rhsm/rhsm.log";
 	public final String rhsmPluginConfFile	= "/etc/yum/pluginconf.d/subscription-manager.conf"; // "/etc/yum/pluginconf.d/rhsmplugin.conf"; renamed by dev on 11/24/2010
 	public final String factsDir			= "/etc/rhsm/facts/";
 	
 	// will be initialized by initializeFieldsFromConfigFile()
 	public String productCertDir				= null; // "/etc/pki/product";
 	public String entitlementCertDir			= null; // "/etc/pki/entitlement";
 	public String consumerCertDir				= null; // "/etc/pki/consumer";
 	public String consumerKeyFile				= null; // consumerCertDir+"/key.pem";
 	public String consumerCertFile				= null; // consumerCertDir+"/cert.pem";
 	
 	
 	public SubscriptionManagerTasks(SSHCommandRunner runner) {
 		super();
 		setSSHCommandRunner(runner);
 	}
 	
 	public void setSSHCommandRunner(SSHCommandRunner runner) {
 		sshCommandRunner = runner;
 	}
 	
 	/**
 	 * Must be called after installSubscriptionManagerRPMs(...)
 	 */
 	public void initializeFieldsFromConfigFile() {
 		if (RemoteFileTasks.testFileExists(sshCommandRunner, rhsmConfFile)==1) {
 			this.consumerCertDir	= getConfFileParameter(rhsmConfFile, "consumerCertDir");
 			this.entitlementCertDir	= getConfFileParameter(rhsmConfFile, "entitlementCertDir");
 			this.productCertDir		= getConfFileParameter(rhsmConfFile, "productCertDir");
 			this.consumerCertFile	= consumerCertDir+"/cert.pem";
 			this.consumerKeyFile	= consumerCertDir+"/key.pem";
 			log.info(this.getClass().getSimpleName()+".initializeFieldsFromConfigFile() succeeded on '"+sshCommandRunner.getConnection().getHostname()+"'.");
 		} else {
 			log.warning("Cannot "+this.getClass().getSimpleName()+".initializeFieldsFromConfigFile() on '"+sshCommandRunner.getConnection().getHostname()+"' until file exists: "+rhsmConfFile);
 		}
 	}
 	
 	
 	public void installSubscriptionManagerRPMs(List<String> rpmUrls, String enablerepofordeps) {
 
 		// verify the subscription-manager client is a rhel 6 machine
 		log.info("Verifying prerequisite...  client hostname '"+sshCommandRunner.getConnection().getHostname()+"' is a Red Hat Enterprise Linux .* release 6 machine.");
 		Assert.assertEquals(sshCommandRunner.runCommandAndWait("cat /etc/redhat-release | grep -E \"^Red Hat Enterprise Linux .* release 6.*\"").getExitCode(),Integer.valueOf(0),
 				sshCommandRunner.getConnection().getHostname()+" must be RHEL 6.*");
 
 		// yum clean all
 		SSHCommandResult sshCommandResult = sshCommandRunner.runCommandAndWait("yum clean all");
 		if (sshCommandResult.getExitCode().equals(1)) {
 			sshCommandRunner.runCommandAndWait("rm -f "+redhatRepoFile);
 		}
 //FIXME Failing on client2 with: [Errno 2] No such file or directory: '/var/cache/yum/x86_64/6Server'
 //		Assert.assertEquals(sshCommandRunner.runCommandAndWait("yum clean all").getExitCode(),Integer.valueOf(0),"yum clean all was a success");
 		sshCommandRunner.runCommandAndWait("yum clean all");
 		
 		// only uninstall rpms when there are new rpms to install
 		if (rpmUrls.size() > 0) {
 			log.info("Uninstalling existing subscription-manager RPMs...");
 			sshCommandRunner.runCommandAndWait("rpm -e subscription-manager-gnome");
 			RemoteFileTasks.runCommandAndAssert(sshCommandRunner,"rpm -q subscription-manager-gnome",Integer.valueOf(1),"package subscription-manager-gnome is not installed",null);
 			sshCommandRunner.runCommandAndWait("rpm -e subscription-manager");
 			RemoteFileTasks.runCommandAndAssert(sshCommandRunner,"rpm -q subscription-manager",Integer.valueOf(1),"package subscription-manager is not installed",null);
 			sshCommandRunner.runCommandAndWait("rpm -e python-rhsm");
 			RemoteFileTasks.runCommandAndAssert(sshCommandRunner,"rpm -q python-rhsm",Integer.valueOf(1),"package python-rhsm is not installed",null);
 		}
 
 		// install new rpms
 		for (String rpmUrl : rpmUrls) {
 			rpmUrl = rpmUrl.trim();
 			log.info("Installing RPM from "+rpmUrl+"...");
			String sm_rpm = "/tmp/"+Arrays.asList(rpmUrl.split("/|=")).get(rpmUrl.split("/|=").length-1);
 			RemoteFileTasks.runCommandAndAssert(sshCommandRunner,"wget -O "+sm_rpm+" --no-check-certificate \""+rpmUrl.trim()+"\"",Integer.valueOf(0),null,"“"+sm_rpm+"” saved");
 			// using yum localinstall should enable testing on RHTS boxes right off the bat.
 			String enablerepo_option = enablerepofordeps.trim().equals("")? "":"--enablerepo="+enablerepofordeps;
 			Assert.assertEquals(sshCommandRunner.runCommandAndWait("yum -y localinstall "+sm_rpm+" --nogpgcheck --disablerepo=* "+enablerepo_option).getExitCode(),Integer.valueOf(0),
 					"Yum installed local rpm: "+sm_rpm);
 		}
 		
 		Assert.assertEquals(sshCommandRunner.runCommandAndWait("rpm -q subscription-manager").getExitCode(),Integer.valueOf(0),
 				"subscription-manager is installed"); // subscription-manager-0.63-1.el6.i686
 
 	}
 	
 	
 	public void removeAllCerts(boolean consumers, boolean entitlements/*, boolean products*/) {
 		sshCommandRunner.runCommandAndWait("killall -9 yum");
 		String certDir;
 		
 		if (consumers) {
 			//certDir = getConfigFileParameter("consumerCertDir");
 			certDir = this.consumerCertDir;
 			log.info("Cleaning out certs from consumerCertDir: "+certDir);
 			if (!certDir.startsWith("/etc/pki/")) log.warning("UNRECOGNIZED DIRECTORY.  NOT CLEANING CERTS FROM: "+certDir);
 			else sshCommandRunner.runCommandAndWait("rm -rf "+certDir+"/*");
 		}
 		
 		if (entitlements) {
 			//certDir = getConfigFileParameter("entitlementCertDir");
 			certDir = this.entitlementCertDir;
 			log.info("Cleaning out certs from entitlementCertDir: "+certDir);
 			if (!certDir.startsWith("/etc/pki/")) log.warning("UNRECOGNIZED DIRECTORY.  NOT CLEANING CERTS FROM: "+certDir);
 			else sshCommandRunner.runCommandAndWait("rm -rf "+certDir+"/*");
 		}
 	}
 	
 	public void updateConfFileParameter(String confFile, String parameter, String value){
 		Assert.assertEquals(
 				RemoteFileTasks.searchReplaceFile(sshCommandRunner, confFile, "^"+parameter+"\\s*=.*$", parameter+"="+value.replaceAll("\\/", "\\\\/")),
 				0,"Updated "+confFile+" parameter '"+parameter+"' to value: " + value);
 	}
 	
 	public String getConfFileParameter(String confFile, String parameter){
 		SSHCommandResult result = RemoteFileTasks.runCommandAndAssert(sshCommandRunner, "grep -E ^"+parameter+" "+confFile, 0, "^"+parameter, null);
 		String value = result.getStdout().split("=|:",2)[1];
 		return value.trim();
 	}
 	
 //	public void updateSMConfigFile(String hostname, String port){
 //		Assert.assertEquals(
 //				RemoteFileTasks.searchReplaceFile(sshCommandRunner, defaultConfigFile, "^hostname\\s*=.*$", "hostname="+hostname),
 //				0,"Updated rhsm config hostname to point to:" + hostname);
 //		Assert.assertEquals(
 //				RemoteFileTasks.searchReplaceFile(sshCommandRunner, defaultConfigFile, "^port\\s*=.*$", "port="+port),
 //				0,"Updated rhsm config port to point to:" + port);
 //		
 //		// jsefler - 7/21/2010
 //		// FIXME DELETEME AFTER FIX FROM <alikins> so, just talked to jsefler and nadathur, we are going to temporarily turn ca verification off, till we get a DEV ca or whatever setup, so we don't break QA at the moment
 //		// TEMPORARY WORK AROUND TO AVOID ISSUES:
 //		// https://bugzilla.redhat.com/show_bug.cgi?id=617703 
 //		// https://bugzilla.redhat.com/show_bug.cgi?id=617303
 //		/*
 //		if (isServerOnPremises) {
 //
 //			log.warning("TEMPORARY WORKAROUND...");
 //			sshCommandRunner.runCommandAndWait("echo \"candlepin_ca_file = /tmp/candlepin-ca.crt\"  >> "+defaultConfigFile);
 //		}
 //		*/
 //		/* Hi,
 //		Insecure mode option moved to /etc/rhsm/rhsm.conf file after commandline option(-k, --insecure) failed to gather the popularity votes.
 //
 //		To enable insecure mode, add the following as a new line to rhsm.conf file
 //		insecure_mode=t
 //    
 //
 //		To disable insecure mode, either remove 'insecure_mode' or set it to any value
 //		other than 't', 'True', 'true', 1.
 //
 //		thanks,
 //		Ajay
 //		*/
 //		log.warning("WORKAROUND FOR INSECURITY...");
 //		//sshCommandRunner.runCommandAndWait("echo \"insecure_mode = true\"  >> "+defaultConfigFile);	// prior workaround
 //		Assert.assertEquals(
 //				RemoteFileTasks.searchReplaceFile(sshCommandRunner, defaultConfigFile, "^insecure\\s*=.*$", "insecure=1"),
 //				0,"Updated rhsm config insecure to: 1");
 //
 //	}
 	
 	
 
 	/**
 	 * Update the minutes value for the certFrequency setting in the
 	 * default /etc/rhsm/rhsm.conf file and restart the rhsmcertd service.
 	 * @param certFrequency - Frequency of certificate refresh (in minutes)
 	 * @param waitForMinutes - after restarting, should we wait for the next refresh?
 	 */
 	public void restart_rhsmcertd (int certFrequency, boolean waitForMinutes){
 		updateConfFileParameter(rhsmConfFile, "certFrequency", String.valueOf(certFrequency));
 		RemoteFileTasks.runCommandAndAssert(sshCommandRunner,"service rhsmcertd restart",Integer.valueOf(0),"^Starting rhsmcertd "+certFrequency+"\\[  OK  \\]$",null);
 		RemoteFileTasks.runCommandAndAssert(sshCommandRunner,"service rhsmcertd status",Integer.valueOf(0),"^rhsmcertd \\(pid \\d+\\) is running...$",null);
 		RemoteFileTasks.runCommandAndAssert(sshCommandRunner,"tail -2 "+rhsmcertdLogFile,Integer.valueOf(0),"started: interval = "+certFrequency+" minutes",null);
 
 		if (waitForMinutes) {
 			SubscriptionManagerCLITestScript.sleep(certFrequency*60*1000);
 		}
 		SubscriptionManagerCLITestScript.sleep(10000);	// give the rhsmcertd chance to make its initial check in with the candlepin server and update the certs
 	}
 	public void stop_rhsmcertd (){
 		RemoteFileTasks.runCommandAndAssert(sshCommandRunner,"service rhsmcertd stop",Integer.valueOf(0));
 		RemoteFileTasks.runCommandAndAssert(sshCommandRunner,"service rhsmcertd status",Integer.valueOf(0),"^rhsmcertd is stopped$",null);
 	}
 
 	
 	public List<SubscriptionPool> getCurrentlyAvailableSubscriptionPools() {
 		return SubscriptionPool.parse(listAvailableSubscriptionPools().getStdout());
 	}
 	
 	public List<SubscriptionPool> getCurrentlyAllAvailableSubscriptionPools() {
 		return SubscriptionPool.parse(listAllAvailableSubscriptionPools().getStdout());
 	}
 	
 	public List<ProductSubscription> getCurrentlyConsumedProductSubscriptions() {
 		return ProductSubscription.parse(listConsumedProductSubscriptions().getStdout());
 	}
 	
 	public List<InstalledProduct> getCurrentlyInstalledProducts() {
 		return InstalledProduct.parse(listInstalledProducts().getStdout());
 	}
 
 	public List<EntitlementCert> getCurrentEntitlementCerts() {
 		/*
 		// THIS ORIGINAL IMPLEMENTATION HAS BEEN THROWING A	java.lang.StackOverflowError
 		// REIMPLEMENTING THIS METHOD TO HELP BREAK THE PROBLEM DOWN INTO SMALLER PIECES - jsefler 11/23/2010
 		sshCommandRunner.runCommandAndWait("find "+entitlementCertDir+" -name '*.pem' | grep -v key.pem | xargs -I '{}' openssl x509 -in '{}' -noout -text");
 		String certificates = sshCommandRunner.getStdout();
 		return EntitlementCert.parse(certificates);
 		 */
 		List<EntitlementCert> entitlementCerts = new ArrayList<EntitlementCert>();
 		for (File entitlementCertFile : getCurrentEntitlementCertFiles()) {
 			entitlementCerts.add(getEntitlementCertFromEntitlementCertFile(entitlementCertFile));
 		}
 		return entitlementCerts;
 	}
 	
 	public List<ProductCert> getCurrentProductCerts() {
 		sshCommandRunner.runCommandAndWait("find "+productCertDir+" -name '*.pem' | xargs -I '{}' openssl x509 -in '{}' -noout -text");
 		String certificates = sshCommandRunner.getStdout();
 		return ProductCert.parse(certificates);
 	}
 	
 	/**
 	 * @return a ConsumerCert object corresponding to the current: openssl x509 -noout -text -in /etc/pki/consumer/cert.pem
 	 */
 	public ConsumerCert getCurrentConsumerCert() {
 		sshCommandRunner.runCommandAndWait("openssl x509 -noout -text -in "+this.consumerCertFile);
 		String certificate = sshCommandRunner.getStdout();
 		return ConsumerCert.parse(certificate);
 	}
 	
 	/**
 	 * @return from the contents of the current /etc/pki/consumer/cert.pem
 	 */
 	public String getCurrentConsumerId() {
 		return getCurrentConsumerCert().consumerid;
 	}
 	
 	/**
 	 * @param registerResult
 	 * @return from the stdout of the register command
 	 */
 	public String getCurrentConsumerId(SSHCommandResult registerResult) {
 		
 		// FIXME  This algorithm is incorrect when stdout is:
 		// The system with UUID 4e3675b1-450a-4066-92da-392c204ca5c7 has been unregistered
 		// ca3f9b32-61e7-44c0-94c1-ce328f7a15b0 testuser1
 		
 		// this algorithm assumes the stdout format is: ca3f9b32-61e7-44c0-94c1-ce328f7a15b0 testuser1
 		return registerResult.getStdout().split(" ")[0];
 	}
 	
 	public String getFactValue(String factName) {
 		SSHCommandResult result = facts_(true, false, null, null, null);
 		
 		String regex=factName.replaceAll("\\(","\\\\(").replaceAll("\\)","\\\\)")+":(.*)";
 		Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
 		Matcher matcher = pattern.matcher(result.getStdout());
 		Assert.assertTrue(matcher.find(),"Found fact "+factName); 
 
 //		log.fine("Matches: ");
 //		do {
 //			log.fine(matcher.group());
 //		} while (matcher.find());
 		return matcher.group(1).trim();	// return the contents of the first capturing group
 	}
 	
 	/**
 	 * @return a map of serialNumber to SubscriptionPool pairs.  The SubscriptionPool is the source from where the serialNumber for the currentlyConsumedProductSubscriptions came from.
 	 * @throws Exception 
 	 */
 //	public Map<Long, SubscriptionPool> getCurrentSerialMapToSubscriptionPools() {
 //		sshCommandRunner.runCommandAndWait("find "+entitlementCertDir+" -name '*.pem' | xargs -I '{}' openssl x509 -in '{}' -noout -text");
 //		String certificates = sshCommandRunner.getStdout();
 //		return SubscriptionPool.parseCerts(certificates);
 //	}
 	public Map<BigInteger, SubscriptionPool> getCurrentSerialMapToSubscriptionPools(String owner, String password) throws Exception {
 		
 		Map<BigInteger, SubscriptionPool> serialMapToSubscriptionPools = new HashMap<BigInteger, SubscriptionPool>();
 		String hostname = getConfFileParameter(rhsmConfFile, "hostname");
 		String port = getConfFileParameter(rhsmConfFile, "port");
 		String prefix = getConfFileParameter(rhsmConfFile, "prefix");
 		for (EntitlementCert entitlementCert : getCurrentEntitlementCerts()) {
 			JSONObject jsonEntitlement = CandlepinTasks.getEntitlementUsingRESTfulAPI(hostname,port,prefix,owner,password,entitlementCert.id);
 			String poolHref = jsonEntitlement.getJSONObject("pool").getString("href");
 			JSONObject jsonPool = new JSONObject(CandlepinTasks.getResourceUsingRESTfulAPI(hostname,port,prefix,owner,password,poolHref));
 			String subscriptionName = jsonPool.getString("productName");
 			String productId = jsonPool.getString("productId");
 			String poolId = jsonPool.getString("id");
 			String quantity = jsonPool.getString("quantity");
 			String endDate = jsonPool.getString("endDate");
 			SubscriptionPool fromPool = new SubscriptionPool(subscriptionName,productId,poolId,quantity,endDate);
 			serialMapToSubscriptionPools.put(entitlementCert.serialNumber, fromPool);
 		}
 		return serialMapToSubscriptionPools;
 	}
 	
 	/**
 	 * @param lsOptions - options used when calling ls to populate the order of the returned List (man ls for more info)
 	 * <br>Possibilities:
 	 * <br>"" no sort order preferred
 	 * <br>"-t" sort by modification time
 	 * <br>"-v" natural sort of (version) numbers within text
 	 * @return List of /etc/pki/entitlement/*.pem files sorted using lsOptions (excluding a key.pem file)
 	 */
 	public List<File> getCurrentEntitlementCertFiles(String lsOptions) {
 		if (lsOptions==null) lsOptions = "";
 		//sshCommandRunner.runCommandAndWait("find /etc/pki/entitlement/ -name '*.pem'");
 		//sshCommandRunner.runCommandAndWait("ls -1 "+lsOptions+" "+entitlementCertDir+"/*.pem");
 		sshCommandRunner.runCommandAndWait("ls -1 "+lsOptions+" "+entitlementCertDir+"/*.pem | grep -v key.pem");
 		String lsFiles = sshCommandRunner.getStdout().trim();
 		List<File> files = new ArrayList<File>();
 		if (!lsFiles.isEmpty()) {
 			for (String lsFile : Arrays.asList(lsFiles.split("\n"))) {
 				
 				// exclude the the key.pem file
 				if (lsFile.endsWith("key.pem")) continue;
 				
 				// TEMPORARY WORKAROUND FOR BUG: https://bugzilla.redhat.com/show_bug.cgi?id=640338 - jsefler 10/7/2010
 				if (lsFile.matches(".*\\(\\d+\\)\\.pem")) {
 					boolean invokeWorkaroundWhileBugIsOpen = true;
 					String bugId="640338"; 
 					try {if (invokeWorkaroundWhileBugIsOpen&&BzChecker.getInstance().isBugOpen(bugId)) {log.fine("Invoking workaround for "+BzChecker.getInstance().getBugState(bugId).toString()+" Bugzilla bug "+bugId+".  (https://bugzilla.redhat.com/show_bug.cgi?id="+bugId+")");} else {invokeWorkaroundWhileBugIsOpen=false;}} catch (XmlRpcException xre) {/* ignore exception */} catch (RuntimeException re) {/* ignore exception */}
 					if (invokeWorkaroundWhileBugIsOpen) {
 						continue;
 					}
 				}
 				// END OF WORKAROUND
 				
 				files.add(new File(lsFile));
 			}
 		}
 		return files;
 	}
 	/**
 	 * @return List of /etc/pki/entitlement/*.pem files (excluding a key.pem file)
 	 */
 	public List<File> getCurrentEntitlementCertFiles() {
 		return getCurrentEntitlementCertFiles("-v");
 	}
 
 	
 
 	/**
 	 * @param lsOptions - options used when calling ls to populate the order of the returned List (man ls for more info)
 	 * <br>Possibilities:
 	 * <br>"" no sort order preferred
 	 * <br>"-t" sort by modification time
 	 * <br>"-v" natural sort of (version) numbers within text
 	 * @return List of /etc/pki/product/*.pem files sorted using lsOptions
 	 */
 	public List<File> getCurrentProductCertFiles(String lsOptions) {
 		if (lsOptions==null) lsOptions = "";
 		//sshCommandRunner.runCommandAndWait("find /etc/pki/product/ -name '*.pem'");
 		sshCommandRunner.runCommandAndWait("ls -1 "+lsOptions+" "+productCertDir+"/*.pem");
 		String lsFiles = sshCommandRunner.getStdout().trim();
 		List<File> files = new ArrayList<File>();
 		if (!lsFiles.isEmpty()) {
 			for (String lsFile : Arrays.asList(lsFiles.split("\n"))) {
 				files.add(new File(lsFile));
 			}
 		}
 		return files;
 	}
 	
 	/**
 	 * @return List of /etc/pki/product/*.pem files
 	 */
 	public List<File> getCurrentProductCertFiles() {
 		return getCurrentProductCertFiles("-v");
 	}
 	
 	
 // replaced by getYumListOfAvailablePackagesFromRepo(...)
 //	/**
 //	 * @return
 //	 * @author ssalevan
 //	 */
 //	public HashMap<String,String[]> getPackagesCorrespondingToSubscribedRepos(){
 //		int min = 3;
 //		sshCommandRunner.runCommandAndWait("killall -9 yum");
 //		log.info("timeout of "+min+" minutes for next command");
 //		sshCommandRunner.runCommandAndWait("yum list available",Long.valueOf(min*60000));
 //		HashMap<String,String[]> pkgMap = new HashMap<String,String[]>();
 //		
 //		String[] packageLines = sshCommandRunner.getStdout().split("\\n");
 //		
 //		int pkglistBegin = 0;
 //		
 //		for(int i=0;i<packageLines.length;i++){
 //			pkglistBegin++;
 //			if(packageLines[i].contains("Available Packages"))
 //				break;
 //		}
 //		
 //		for(ProductSubscription sub : getCurrentlyConsumedProductSubscriptions()){
 //			ArrayList<String> pkgList = new ArrayList<String>();
 //			for(int i=pkglistBegin;i<packageLines.length;i++){
 //				String[] splitLine = packageLines[i].split(" ");
 //				String pkgName = splitLine[0];
 //				String repoName = splitLine[splitLine.length - 1];
 //				if(repoName.toLowerCase().contains(sub.productName.toLowerCase()))
 //					pkgList.add(pkgName);
 //			}
 //			pkgMap.put(sub.productName, (String[])pkgList.toArray());
 //		}
 //		
 //		return pkgMap;
 //	}
 
 	/**
 	 * @param productSubscription
 	 * @param owner	- owner of the subscription pool (will be used in a REST api call to the candlepin server)
 	 * @param password
 	 * @return the SubscriptionPool from which this consumed ProductSubscription came from
 	 * @throws Exception
 	 */
 	public SubscriptionPool getSubscriptionPoolFromProductSubscription(ProductSubscription productSubscription, String owner, String password) throws Exception {
 		
 		// if already known, return the SubscriptionPool from which ProductSubscription came
 		if (productSubscription.fromSubscriptionPool != null) return productSubscription.fromSubscriptionPool;
 		
 		productSubscription.fromSubscriptionPool = getCurrentSerialMapToSubscriptionPools(owner, password).get(productSubscription.serialNumber);
 
 		return productSubscription.fromSubscriptionPool;
 	}
 	
 //DELETEME
 //	/**
 //	 * @param fieldName
 //	 * @param fieldValue
 //	 * @param subscriptionPools - usually getCurrentlyAvailableSubscriptionPools()
 //	 * @return - the SubscriptionPool from subscriptionPools that has a matching field (if not found, null is returned)
 //	 */
 //	public SubscriptionPool findSubscriptionPoolWithMatchingFieldFromList(String fieldName, Object fieldValue, List<SubscriptionPool> subscriptionPools) {
 //		
 //		SubscriptionPool subscriptionPoolWithMatchingField = null;
 //		for (SubscriptionPool subscriptionPool : subscriptionPools) {
 //			try {
 //				if (SubscriptionPool.class.getField(fieldName).get(subscriptionPool).equals(fieldValue)) {
 //					subscriptionPoolWithMatchingField = subscriptionPool;
 //				}
 //			} catch (IllegalArgumentException e) {
 //				// TODO Auto-generated catch block
 //				e.printStackTrace();
 //			} catch (SecurityException e) {
 //				// TODO Auto-generated catch block
 //				e.printStackTrace();
 //			} catch (IllegalAccessException e) {
 //				// TODO Auto-generated catch block
 //				e.printStackTrace();
 //			} catch (NoSuchFieldException e) {
 //				// TODO Auto-generated catch block
 //				e.printStackTrace();
 //			}
 //		}
 //		return subscriptionPoolWithMatchingField;
 //	}
 //	
 //	
 //	/**
 //	 * @param fieldName
 //	 * @param fieldValue
 //	 * @param productSubscriptions - usually getCurrentlyConsumedProductSubscriptions()
 //	 * @return - the ProductSubscription from productSubscriptions that has a matching field (if not found, null is returned)
 //	 */
 //	public ProductSubscription findProductSubscriptionWithMatchingFieldFromList(String fieldName, Object fieldValue, List<ProductSubscription> productSubscriptions) {
 //		ProductSubscription productSubscriptionWithMatchingField = null;
 //		for (ProductSubscription productSubscription : productSubscriptions) {
 //			try {
 //				if (ProductSubscription.class.getField(fieldName).get(productSubscription).equals(fieldValue)) {
 //					productSubscriptionWithMatchingField = productSubscription;
 //				}
 //			} catch (IllegalArgumentException e) {
 //				// TODO Auto-generated catch block
 //				e.printStackTrace();
 //			} catch (SecurityException e) {
 //				// TODO Auto-generated catch block
 //				e.printStackTrace();
 //			} catch (IllegalAccessException e) {
 //				// TODO Auto-generated catch block
 //				e.printStackTrace();
 //			} catch (NoSuchFieldException e) {
 //				// TODO Auto-generated catch block
 //				e.printStackTrace();
 //			}
 //		}
 //		return productSubscriptionWithMatchingField;
 //	}
 //	
 //	
 //	/**
 //	 * @param fieldName
 //	 * @param fieldValue
 //	 * @param installedProducts - usually getCurrentProductCerts()
 //	 * @return - the InstalledProduct from installedProducts that has a matching field (if not found, null is returned)
 //	 */
 //	public InstalledProduct findInstalledProductWithMatchingFieldFromList(String fieldName, Object fieldValue, List<InstalledProduct> installedProducts) {
 //		InstalledProduct installedProductWithMatchingField = null;
 //		for (InstalledProduct installedProduct : installedProducts) {
 //			try {
 //				if (InstalledProduct.class.getField(fieldName).get(installedProduct).equals(fieldValue)) {
 //					installedProductWithMatchingField = installedProduct;
 //				}
 //			} catch (IllegalArgumentException e) {
 //				// TODO Auto-generated catch block
 //				e.printStackTrace();
 //			} catch (SecurityException e) {
 //				// TODO Auto-generated catch block
 //				e.printStackTrace();
 //			} catch (IllegalAccessException e) {
 //				// TODO Auto-generated catch block
 //				e.printStackTrace();
 //			} catch (NoSuchFieldException e) {
 //				// TODO Auto-generated catch block
 //				e.printStackTrace();
 //			}
 //		}
 //		return installedProductWithMatchingField;
 //	}
 //	
 //	
 //	/**
 //	 * @param fieldName
 //	 * @param fieldValue
 //	 * @param productCerts - usually getCurrentlyProductCerts()
 //	 * @return - the ProductCert from productCerts that has a matching field (if not found, null is returned)
 //	 */
 //	public ProductCert findProductCertWithMatchingFieldFromList(String fieldName, Object fieldValue, List<ProductCert> productCerts) {
 //		ProductCert productCertWithMatchingField = null;
 //		for (ProductCert productCert : productCerts) {
 //			try {
 //				if (ProductCert.class.getField(fieldName).get(productCert).equals(fieldValue)) {
 //					productCertWithMatchingField = productCert;
 //				}
 //			} catch (IllegalArgumentException e) {
 //				// TODO Auto-generated catch block
 //				e.printStackTrace();
 //			} catch (SecurityException e) {
 //				// TODO Auto-generated catch block
 //				e.printStackTrace();
 //			} catch (IllegalAccessException e) {
 //				// TODO Auto-generated catch block
 //				e.printStackTrace();
 //			} catch (NoSuchFieldException e) {
 //				// TODO Auto-generated catch block
 //				e.printStackTrace();
 //			}
 //		}
 //		return productCertWithMatchingField;
 //	}
 //	
 //	
 //	/**
 //	 * @param fieldName
 //	 * @param fieldValue
 //	 * @param entitlementCerts - usually getCurrentEntitlementCerts()
 //	 * @return - the EntitlementCert from entitlementCerts that has a matching field (if not found, null is returned)
 //	 */
 //	public EntitlementCert findEntitlementCertWithMatchingFieldFromList(String fieldName, Object fieldValue, List<EntitlementCert> entitlementCerts) {
 //		EntitlementCert entitlementCertWithMatchingField = null;
 //		for (EntitlementCert entitlementCert : entitlementCerts) {
 //			try {
 //				if (EntitlementCert.class.getField(fieldName).get(entitlementCert).equals(fieldValue)) {
 //					entitlementCertWithMatchingField = entitlementCert;
 //				}
 //			} catch (IllegalArgumentException e) {
 //				// TODO Auto-generated catch block
 //				e.printStackTrace();
 //			} catch (SecurityException e) {
 //				// TODO Auto-generated catch block
 //				e.printStackTrace();
 //			} catch (IllegalAccessException e) {
 //				// TODO Auto-generated catch block
 //				e.printStackTrace();
 //			} catch (NoSuchFieldException e) {
 //				// TODO Auto-generated catch block
 //				e.printStackTrace();
 //			}
 //		}
 //		return entitlementCertWithMatchingField;
 //	}
 	
 
 
 	
 //KEEPME FOR FUTURE USAGE SOMEWHERE ELSE	
 //	/**
 //	 * Given a List of instances of some class (e.g. getCurrentEntitlementCerts()), this
 //	 * method is useful for finding the first instance (e.g. an EntitlementCert) whose public
 //	 * field by the name "fieldName" has a value of fieldValue.  If no match is found, null is returned.
 //	 * @param <T>
 //	 * @param fieldName
 //	 * @param fieldValue
 //	 * @param dataInstances
 //	 * @return
 //	 */
 //	@SuppressWarnings("unchecked")
 //	public <T> T findFirstInstanceWithMatchingFieldFromList(String fieldName, Object fieldValue, List<T> dataInstances) {
 //		Collection<T> dataInstancesWithMatchingFieldFromList = Collections2.filter(dataInstances, new ByValuePredicate(fieldName,fieldValue));
 //		if (dataInstancesWithMatchingFieldFromList.isEmpty()) return null;
 //		return (T) dataInstancesWithMatchingFieldFromList.toArray()[0];
 //	}
 //	
 //	/**
 //	 * Given a List of instances of some class (e.g. getAllAvailableSubscriptionPools()), this
 //	 * method is useful for finding a subset of instances whose public field by the name "fieldName"
 //	 * has a value of fieldValue.  If no match is found, an empty list is returned.
 //	 * @param <T>
 //	 * @param fieldName
 //	 * @param fieldValue
 //	 * @param dataInstances
 //	 * @return
 //	 */
 //	@SuppressWarnings("unchecked")
 //	public <T> List<T> findAllInstancesWithMatchingFieldFromList(String fieldName, Object fieldValue, List<T> dataInstances) {
 //		Collection<T> dataInstancesWithMatchingFieldFromList = Collections2.filter(dataInstances, new ByValuePredicate(fieldName,fieldValue));
 //		return (List<T>) Arrays.asList(dataInstancesWithMatchingFieldFromList.toArray());
 //	}
 //	
 //	class ByValuePredicate implements Predicate<Object> {
 //		Object value;
 //		String fieldName;
 //		public ByValuePredicate(String fieldName, Object value) {
 //			this.value=value;
 //			this.fieldName=fieldName;
 //		}
 //		public boolean apply(Object toTest) {
 //			try {
 //				return toTest.getClass().getField(fieldName).get(toTest).equals(value);
 //			} catch (IllegalArgumentException e) {
 //				// TODO Auto-generated catch block
 //				e.printStackTrace();
 //			} catch (SecurityException e) {
 //				// TODO Auto-generated catch block
 //				e.printStackTrace();
 //			} catch (IllegalAccessException e) {
 //				// TODO Auto-generated catch block
 //				e.printStackTrace();
 //			} catch (NoSuchFieldException e) {
 //				// TODO Auto-generated catch block
 //				e.printStackTrace();
 //			}
 //			return false;
 //		}
 //	}
 
 	
 	
 	/**
 	 * For the given consumed ProductSubscription, get the corresponding EntitlementCert
 	 * @param productSubscription
 	 * @return
 	 */
 	public EntitlementCert getEntitlementCertCorrespondingToProductSubscription(ProductSubscription productSubscription) {
 		String certFile = entitlementCertDir+"/"+productSubscription.serialNumber+".pem";
 		sshCommandRunner.runCommandAndWait("openssl x509 -text -noout -in '"+certFile+"'");
 		String certificate = sshCommandRunner.getStdout();
 		List<EntitlementCert> entitlementCerts = EntitlementCert.parse(certificate);
 		Assert.assertEquals(entitlementCerts.size(), 1,"Only one EntitlementCert corresponds to ProductSubscription: "+productSubscription);
 		return entitlementCerts.get(0);
 	}
 	
 	public EntitlementCert getEntitlementCertFromEntitlementCertFile(File serialPemFile) {
 		sshCommandRunner.runCommandAndWait("openssl x509 -noout -text -in "+serialPemFile.getPath());
 		String certificates = sshCommandRunner.getStdout();
 		List<EntitlementCert> entitlementCerts = EntitlementCert.parse(certificates);
 		
 		// assert that only one EntitlementCert was parsed and return it
 		Assert.assertEquals(entitlementCerts.size(), 1, "Entitlement cert file '"+serialPemFile+"' parsed only one EntitlementCert.");
 		return entitlementCerts.get(0);
 	}
 	
 	public ProductCert getProductCertFromProductCertFile(File productPemFile) {
 		sshCommandRunner.runCommandAndWait("openssl x509 -noout -text -in "+productPemFile.getPath());
 		String certificates = sshCommandRunner.getStdout();
 		List<ProductCert> productCerts = ProductCert.parse(certificates);
 		
 		// assert that only one ProductCert was parsed and return it
 		Assert.assertEquals(productCerts.size(), 1, "Product cert file '"+productPemFile+"' parsed only one ProductCert.");
 		return productCerts.get(0);
 	}
 	
 	public BigInteger getSerialNumberFromEntitlementCertFile(File serialPemFile) {
 		// example serialPemFile: /etc/pki/entitlement/196.pem
 		// extract the serial number from the certFile name
 		// Note: probably a more robust way to do this is to get it from inside the file
 		//Integer serialNumber = Integer.valueOf(serialPemFile.getName().split("\\.")[0]);
 		String serialNumber = serialPemFile.getName().split("\\.")[0];
 		//return Long.parseLong(serialNumber, 10);
 		//return new Long(serialNumber);
 		return new BigInteger(serialNumber);
 	}
 	
 	public File getEntitlementCertFileFromEntitlementCert(EntitlementCert entitlementCert) {
 		File serialPemFile = new File(entitlementCertDir+File.separator+entitlementCert.serialNumber+".pem");
 		return serialPemFile;
 	}
 	
 	// register module tasks ************************************************************
 	
 	/**
 	 * register without asserting results
 	 * @param username
 	 * @param password
 	 * @param type
 	 * @param name
 	 * @param consumerId
 	 * @param autosubscribe
 	 * @param force
 	 * @param proxy
 	 * @param proxyuser
 	 * @param proxypassword
 	 * @return
 	 */
 	public SSHCommandResult register_(String username, String password, ConsumerType type, String name, String consumerId, Boolean autosubscribe, Boolean force, String proxy, String proxyuser, String proxypassword) {
 
 		// assemble the register command
 		String command = this.command;				command += " register";
 		if (username!=null)							command += " --username="+username;
 		if (password!=null)							command += " --password="+password;
 		if (type!=null)								command += " --type="+type;
 		if (name!=null)								command += " --name="+name;
 		if (consumerId!=null)						command += " --consumerid="+consumerId;
 		if (autosubscribe!=null && autosubscribe)	command += " --autosubscribe";
 		if (force!=null && force)					command += " --force";
 		if (proxy!=null)							command += " --proxy="+proxy;
 		if (proxyuser!=null)						command += " --proxyuser="+proxyuser;
 		if (proxypassword!=null)					command += " --proxypassword="+proxypassword;
 
 		
 		// run command without asserting results
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
 	 * @param name TODO
 	 * @param consumerId
 	 * @param autosubscribe
 	 * @param force
 	 * @param proxy TODO
 	 * @param proxyuser TODO
 	 * @param proxypassword TODO
 	 */
 	public SSHCommandResult register(String username, String password, ConsumerType type, String name, String consumerId, Boolean autosubscribe, Boolean force, String proxy, String proxyuser, String proxypassword) {
 		
 		SSHCommandResult sshCommandResult = register_(username, password, type, name, consumerId, autosubscribe, force, proxy, proxyuser, proxypassword);
 
 		// assert results for a successful registration
 		if (sshCommandResult.getStdout().startsWith("This system is already registered.")) return sshCommandResult;
 		Assert.assertEquals(sshCommandResult.getExitCode(), Integer.valueOf(0), "The exit code from the register command indicates a success.");
 		Assert.assertContainsMatch(sshCommandResult.getStdout().trim(), "[a-f,0-9,\\-]{36} "+(name==null?username:name));
 		
 		// assert that register with consumerId returns the expected uuid
 		if (consumerId!=null) {
 			Assert.assertEquals(sshCommandResult.getStdout().trim(), consumerId+" "+username, "register to an exiting consumer was a success");
 		}
 		
 		// assert certificate files are dropped into /etc/pki/consumer
 		Assert.assertEquals(RemoteFileTasks.testFileExists(sshCommandRunner,this.consumerKeyFile),1, "Consumer key file '"+this.consumerKeyFile+"' must exist after register.");
 		Assert.assertEquals(RemoteFileTasks.testFileExists(sshCommandRunner,this.consumerCertFile),1, "Consumer cert file '"+this.consumerCertFile+"' must exist after register.");
 		
 		// TEMPORARY WORKAROUND FOR BUG: https://bugzilla.redhat.com/show_bug.cgi?id=639417 - jsefler 10/1/2010
 		boolean invokeWorkaroundWhileBugIsOpen = true;
 		String bugId="639417"; 
 		try {if (invokeWorkaroundWhileBugIsOpen&&BzChecker.getInstance().isBugOpen(bugId)) {log.fine("Invoking workaround for "+BzChecker.getInstance().getBugState(bugId).toString()+" Bugzilla bug "+bugId+".  (https://bugzilla.redhat.com/show_bug.cgi?id="+bugId+")");} else {invokeWorkaroundWhileBugIsOpen=false;}} catch (XmlRpcException xre) {/* ignore exception */} catch (RuntimeException re) {/* ignore exception */}
 		if (invokeWorkaroundWhileBugIsOpen) {
 			restart_rhsmcertd(Integer.valueOf(getConfFileParameter(rhsmConfFile, "certFrequency")), false);
 		}
 		// END OF WORKAROUND
 		
 		
 		return sshCommandResult; // from the register command
 	}
 	
 
 	
 	
 	// reregister module tasks ************************************************************
 
 //	/**
 //	 * reregister without asserting results
 //	 */
 //	public SSHCommandResult reregister_(String username, String password, String consumerid) {
 //
 //		// assemble the unregister command
 //		String					command  = "subscription-manager-cli reregister";	
 //		if (username!=null)		command += " --username="+username;
 //		if (password!=null)		command += " --password="+password;
 //		if (consumerid!=null)	command += " --consumerid="+consumerid;
 //		
 //		// register without asserting results
 //		return sshCommandRunner.runCommandAndWait(command);
 //	}
 //	
 //	/**
 //	 * "subscription-manager-cli reregister"
 //	 */
 //	public SSHCommandResult reregister(String username, String password, String consumerid) {
 //		
 //		// get the current ConsumerCert
 //		ConsumerCert consumerCertBefore = null;
 //		if (consumerid==null) {	//if (RemoteFileTasks.testFileExists(sshCommandRunner, consumerCertFile)==1) {
 //			consumerCertBefore = getCurrentConsumerCert();
 //			log.fine("Consumer cert before reregistering: "+consumerCertBefore);
 //		}
 //		
 //		SSHCommandResult sshCommandResult = reregister_(username,password,consumerid);
 //		
 //		// assert results for a successful reregistration
 //		Assert.assertEquals(sshCommandResult.getExitCode(), Integer.valueOf(0), "The exit code from the reregister command indicates a success.");
 //		String regex = "[a-f,0-9,\\-]{36}";			// consumerid regex
 //		if (consumerid!=null) regex=consumerid;		// consumerid
 //		if (username!=null) regex+=" "+username;	// username
 //		Assert.assertContainsMatch(sshCommandResult.getStdout().trim(), regex);
 //
 //		// get the new ConsumerCert
 //		ConsumerCert consumerCertAfter = getCurrentConsumerCert();
 //		log.fine("Consumer cert after reregistering: "+consumerCertAfter);
 //		
 //		// assert the new ConsumerCert from a successful reregistration
 //		if (consumerCertBefore!=null) {
 //			Assert.assertEquals(consumerCertAfter.consumerid, consumerCertBefore.consumerid,
 //				"The consumer cert userid remains unchanged after reregistering.");
 //			Assert.assertEquals(consumerCertAfter.username, consumerCertBefore.username,
 //				"The consumer cert username remains unchanged after reregistering.");
 //			Assert.assertTrue(consumerCertAfter.validityNotBefore.after(consumerCertBefore.validityNotBefore),
 //				"The consumer cert validityNotBefore date has been changed to a newer date after reregistering.");
 //		}
 //		
 //		// assert the new consumer certificate contains the reregistered credentials...
 //		if (consumerid!=null) {
 //			Assert.assertEquals(consumerCertAfter.consumerid, consumerid,
 //				"The reregistered consumer cert belongs to the requested consumerid.");
 //		}
 //		if (username!=null) {
 //			Assert.assertEquals(consumerCertAfter.username, username,
 //				"The reregistered consumer cert belongs to the authenticated username.");
 //		}
 //		
 //		return sshCommandResult; // from the reregister command
 //	}
 	
 	public SSHCommandResult reregisterToExistingConsumer(String username, String password, String consumerId) {
 		log.warning("The subscription-manager-cli reregister module has been eliminated and replaced by register --consumerid (10/4/2010 git hash b3c728183c7259841100eeacb7754c727dc523cd)...");
 		//RemoteFileTasks.runCommandAndWait(sshCommandRunner, "rm -f "+consumerCertFile, LogMessageUtil.action());
 		//removeAllCerts(true, true);
 		clean(null, null, null);
 		return register(username,password,null,null,consumerId,null, null, null, null, null);
 	}
 	
 	
 	
 	// clean module tasks ************************************************************
 
 	/**
 	 * clean without asserting results
 	 * @param proxy TODO
 	 * @param proxyuser TODO
 	 * @param proxypassword TODO
 	 */
 	public SSHCommandResult clean_(String proxy, String proxyuser, String proxypassword) {
 
 		// assemble the unregister command
 		String command = this.command;	command += " clean";
 		if (proxy!=null)				command += " --proxy="+proxy;
 		if (proxyuser!=null)			command += " --proxyuser="+proxyuser;
 		if (proxypassword!=null)		command += " --proxypassword="+proxypassword;
 		
 		// run command without asserting results
 		return sshCommandRunner.runCommandAndWait(command);
 	}
 	
 	/**
 	 * "subscription-manager-cli clean"
 	 * @param proxy TODO
 	 * @param proxyuser TODO
 	 * @param proxypassword TODO
 	 */
 	public SSHCommandResult clean(String proxy, String proxyuser, String proxypassword) {
 		
 		SSHCommandResult sshCommandResult = clean_(proxy, proxyuser, proxypassword);
 		
 		// assert results for a successful clean
 		Assert.assertEquals(sshCommandResult.getExitCode(), Integer.valueOf(0), "The exit code from the clean command indicates a success.");
 		Assert.assertEquals(sshCommandResult.getStdout().trim(), "All local data removed");
 		
 		// assert that the consumer cert directory is gone
 		Assert.assertFalse(RemoteFileTasks.testFileExists(sshCommandRunner,consumerCertDir)==1, consumerCertDir+" does NOT exist after clean.");
 
 		// assert that the entitlement cert directory is gone
 		Assert.assertFalse(RemoteFileTasks.testFileExists(sshCommandRunner,entitlementCertDir)==1, entitlementCertDir+" does NOT exist after clean.");
 
 		return sshCommandResult; // from the clean command
 	}
 	
 	
 	
 	// refresh module tasks ************************************************************
 
 	/**
 	 * refresh without asserting results
 	 * @param proxy TODO
 	 * @param proxyuser TODO
 	 * @param proxypassword TODO
 	 */
 	public SSHCommandResult refresh_(String proxy, String proxyuser, String proxypassword) {
 
 		// assemble the unregister command
 		String command = this.command;	command += " refresh";
 		if (proxy!=null)				command += " --proxy="+proxy;
 		if (proxyuser!=null)			command += " --proxyuser="+proxyuser;
 		if (proxypassword!=null)		command += " --proxypassword="+proxypassword;
 		
 		// run command without asserting results
 		return sshCommandRunner.runCommandAndWait(command);
 	}
 	
 	/**
 	 * "subscription-manager-cli refresh"
 	 * @param proxy TODO
 	 * @param proxyuser TODO
 	 * @param proxypassword TODO
 	 */
 	public SSHCommandResult refresh(String proxy, String proxyuser, String proxypassword) {
 		
 		SSHCommandResult sshCommandResult = refresh_(proxy, proxyuser, proxypassword);
 		
 		// assert results for a successful clean
 		Assert.assertEquals(sshCommandResult.getExitCode(), Integer.valueOf(0), "The exit code from the refresh command indicates a success.");
 		Assert.assertEquals(sshCommandResult.getStdout().trim(), "All local data refreshed");
 		
 		return sshCommandResult; // from the refresh command
 	}
 	
 	
 	
 	// identity module tasks ************************************************************
 
 	/**
 	 * identity without asserting results
 	 * @param proxy TODO
 	 * @param proxyuser TODO
 	 * @param proxypassword TODO
 	 */
 	public SSHCommandResult identity_(String username, String password, Boolean regenerate, String proxy, String proxyuser, String proxypassword) {
 
 		// assemble the unregister command
 		String command = this.command;		command += " identity";
 		if (username!=null)					command += " --username="+username;
 		if (password!=null)					command += " --password="+password;
 		if (regenerate!=null && regenerate)	command += " --regenerate";
 		if (proxy!=null)					command += " --proxy="+proxy;
 		if (proxyuser!=null)				command += " --proxyuser="+proxyuser;
 		if (proxypassword!=null)			command += " --proxypassword="+proxypassword;
 		
 		// run command without asserting results
 		return sshCommandRunner.runCommandAndWait(command);
 	}
 	
 	/**
 	 * "subscription-manager-cli identity"
 	 * @param proxy TODO
 	 * @param proxyuser TODO
 	 * @param proxypassword TODO
 	 */
 	public SSHCommandResult identity(String username, String password, Boolean regenerate, String proxy, String proxyuser, String proxypassword) {
 		
 		SSHCommandResult sshCommandResult = identity_(username, password, regenerate, proxy, proxyuser, proxypassword);
 		
 		// assert results for a successful identify
 		/* Example sshCommandResult.getStdout():
 		 * Current identity is: 8f4dd91a-2c41-4045-a937-e3c8554a5701 name: testuser1
 		 */
 		Assert.assertEquals(sshCommandResult.getExitCode(), Integer.valueOf(0), "The exit code from the identity command indicates a success.");
 		String regex = "[a-f,0-9,\\-]{36}";			// consumerid regex
 		Assert.assertContainsMatch(sshCommandResult.getStdout().trim(), regex);
 		
 		return sshCommandResult; // from the identity command
 	}
 	
 	// unregister module tasks ************************************************************
 
 	/**
 	 * unregister without asserting results
 	 * @param proxy TODO
 	 * @param proxyuser TODO
 	 * @param proxypassword TODO
 	 */
 	public SSHCommandResult unregister_(String proxy, String proxyuser, String proxypassword) {
 
 		// assemble the unregister command
 		String command = this.command;	command += " unregister";
 		if (proxy!=null)				command += " --proxy="+proxy;
 		if (proxyuser!=null)			command += " --proxyuser="+proxyuser;
 		if (proxypassword!=null)		command += " --proxypassword="+proxypassword;
 		
 		// run command without asserting results
 		return sshCommandRunner.runCommandAndWait(command);
 	}
 	
 	/**
 	 * "subscription-manager-cli unregister"
 	 * @param proxy TODO
 	 * @param proxyuser TODO
 	 * @param proxypassword TODO
 	 */
 	public SSHCommandResult unregister(String proxy, String proxyuser, String proxypassword) {
 		SSHCommandResult sshCommandResult = unregister_(proxy, proxyuser, proxypassword);
 		
 		// assert results for a successful registration
 		if (sshCommandResult.getExitCode()==0) {
 			Assert.assertTrue(sshCommandResult.getStdout().trim().equals("System has been un-registered."), "The unregister command was a success.");
 			Assert.assertEquals(sshCommandResult.getExitCode(), Integer.valueOf(0), "The exit code from the unregister command indicates a success.");
 		} else {
 			Assert.assertTrue(sshCommandResult.getStdout().startsWith("This system is currently not registered."),"The unregister command was not necessary.  It was already unregistered");
 			Assert.assertEquals(sshCommandResult.getExitCode(), Integer.valueOf(1), "The unregister command returned exit code 1 meaning that it was already unregistered.");
 		} 
 		
 		// assert that the consumer cert and key have been removed
 		Assert.assertEquals(RemoteFileTasks.testFileExists(sshCommandRunner,this.consumerKeyFile),0, "Consumer key file '"+this.consumerKeyFile+"' does NOT exist after unregister.");
 		Assert.assertEquals(RemoteFileTasks.testFileExists(sshCommandRunner,this.consumerCertFile),0, "Consumer cert file '"+this.consumerCertFile+" does NOT exist after unregister.");
 
 		// assert that all of the entitlement product certs have been removed (Actually, the entitlementCertDir should get removed)
 		Assert.assertTrue(getCurrentEntitlementCertFiles().size()==0, "All of the entitlement product certificates have been removed after unregister.");
 // FIXME UNCOMMENT SOMETIME IN THE FUTURE.  DOES NOT SEEM TO BE ACCURATE AT THIS TIME 10/25/2010
 //		Assert.assertEquals(RemoteFileTasks.testFileExists(sshCommandRunner, entitlementCertDir),0,"Entitlement Cert directory '"+entitlementCertDir+"' should not exist after unregister.");
 
 		return sshCommandResult; // from the unregister command
 	}
 	
 	
 	
 	// list module tasks ************************************************************
 	
 	/**
 	 * list without asserting results
 	 * @param all TODO
 	 * @param available TODO
 	 * @param consumed TODO
 	 * @param installed TODO
 	 * @param proxy TODO
 	 * @param proxyuser TODO
 	 * @param proxypassword TODO
 	 */
 	public SSHCommandResult list_(Boolean all, Boolean available, Boolean consumed, Boolean installed, String proxy, String proxyuser, String proxypassword) {
 
 		// assemble the register command
 		String command = this.command;		command += " list";	
 		if (all!=null && all)				command += " --all";
 		if (available!=null && available)	command += " --available";
 		if (consumed!=null && consumed)		command += " --consumed";
 		if (installed!=null && installed)	command += " --installed";
 		if (proxy!=null)					command += " --proxy="+proxy;
 		if (proxyuser!=null)				command += " --proxyuser="+proxyuser;
 		if (proxypassword!=null)			command += " --proxypassword="+proxypassword;
 		
 		// run command without asserting results
 		return sshCommandRunner.runCommandAndWait(command);
 	}
 	
 	/**
 	 * @return SSHCommandResult from "subscription-manager-cli list --installed"
 	 */
 	public SSHCommandResult listInstalledProducts() {
 		
 		SSHCommandResult sshCommandResult = list_(null,null,null,Boolean.TRUE, null, null, null);
 		
 		Assert.assertEquals(sshCommandResult.getExitCode(), Integer.valueOf(0), "The exit code from the list command indicates a success.");
 
 		if (getCurrentEntitlementCertFiles().isEmpty() && getCurrentProductCertFiles().isEmpty()) {
 			Assert.assertTrue(sshCommandResult.getStdout().trim().equals("No installed Products to list"), "No installed Products to list");
 		} else {
 			Assert.assertContainsMatch(sshCommandResult.getStdout(), "Installed Product Status");
 		}
 
 		return sshCommandResult;
 	}
 	
 	/**
 	 * @return SSHCommandResult from "subscription-manager-cli list --available"
 	 */
 	public SSHCommandResult listAvailableSubscriptionPools() {
 
 		SSHCommandResult sshCommandResult = list_(null,Boolean.TRUE,null, null, null, null, null);
 		
 		Assert.assertEquals(sshCommandResult.getExitCode(), Integer.valueOf(0), "The exit code from the list --available command indicates a success.");
 		//Assert.assertContainsMatch(sshCommandResult.getStdout(), "Available Subscriptions");
 
 		return sshCommandResult;
 	}
 	
 	/**
 	 * @return SSHCommandResult from "subscription-manager-cli list --all --available"
 	 */
 	public SSHCommandResult listAllAvailableSubscriptionPools() {
 
 		// TEMPORARY WORKAROUND FOR BUG: https://bugzilla.redhat.com/show_bug.cgi?id=638266 - jsefler 9/28/2010
 		boolean invokeWorkaroundWhileBugIsOpen = false;
 		String bugId="638266"; 
 		try {if (invokeWorkaroundWhileBugIsOpen&&BzChecker.getInstance().isBugOpen(bugId)) {log.fine("Invoking workaround for "+BzChecker.getInstance().getBugState(bugId).toString()+" Bugzilla bug "+bugId+".  (https://bugzilla.redhat.com/show_bug.cgi?id="+bugId+")");} else {invokeWorkaroundWhileBugIsOpen=false;}} catch (XmlRpcException xre) {/* ignore exception */} catch (RuntimeException re) {/* ignore exception */}
 		if (invokeWorkaroundWhileBugIsOpen) {
 			return list_(Boolean.FALSE,Boolean.TRUE,null, null, null, null, null);
 		}
 		// END OF WORKAROUND
 		
 		SSHCommandResult sshCommandResult = list_(Boolean.TRUE,Boolean.TRUE,null, null, null, null, null);
 		
 		Assert.assertEquals(sshCommandResult.getExitCode(), Integer.valueOf(0), "The exit code from the list --all --available command indicates a success.");
 		//Assert.assertContainsMatch(sshCommandResult.getStdout(), "Available Subscriptions");
 
 		return sshCommandResult;
 		
 	}
 	
 	/**
 	 * @return SSHCommandResult from "subscription-manager-cli list --consumed"
 	 */
 	public SSHCommandResult listConsumedProductSubscriptions() {
 
 		SSHCommandResult sshCommandResult = list_(null,null,Boolean.TRUE, null, null, null, null);
 		
 		List<File> entitlementCertFiles = getCurrentEntitlementCertFiles();
 		Assert.assertEquals(sshCommandResult.getExitCode(), Integer.valueOf(0), "The exit code from the list --consumed command indicates a success.");
 
 		if (entitlementCertFiles.isEmpty()) {
 			Assert.assertTrue(sshCommandResult.getStdout().trim().equals("No Consumed subscription pools to list"), "No Consumed subscription pools to list");
 		} /*else {
 			Assert.assertContainsMatch(sshCommandResult.getStdout(), "Consumed Product Subscriptions");
 		}*/
 
 		return sshCommandResult;
 	}
 	
 	
 	
 	// subscribe module tasks ************************************************************
 
 	/**
 	 * subscribe without asserting results
 	 * @param poolId TODO
 	 * @param productId TODO
 	 * @param regtoken TODO
 	 * @param email TODO
 	 * @param locale TODO
 	 * @param proxy TODO
 	 * @param proxyuser TODO
 	 * @param proxypassword TODO
 	 */
 	public SSHCommandResult subscribe_(String poolId, String productId, String regtoken, String email, String locale, String proxy, String proxyuser, String proxypassword) {
 		
 		// assemble the subscribe command
 		String command = this.command;	command += " subscribe";	
 		if (poolId!=null)				command += " --pool="+poolId;
 		if (productId!=null)			command += " --product="+productId;
 		if (regtoken!=null)				command += " --regtoken="+regtoken;
 		if (email!=null)				command += " --email="+email;
 		if (locale!=null)				command += " --locale="+locale;
 		if (proxy!=null)				command += " --proxy="+proxy;
 		if (proxyuser!=null)			command += " --proxyuser="+proxyuser;
 		if (proxypassword!=null)		command += " --proxypassword="+proxypassword;
 		
 		// run command without asserting results
 		return sshCommandRunner.runCommandAndWait(command);
 	}
 
 	/**
 	 * subscribe without asserting results
 	 * @param proxy TODO
 	 * @param proxyuser TODO
 	 * @param proxypassword TODO
 	 */
 	public SSHCommandResult subscribe_(List<String> poolIds, List<String> productIds, List<String> regtokens, String email, String locale, String proxy, String proxyuser, String proxypassword) {
 
 		// assemble the subscribe command
 		String command = this.command;								command += " subscribe";	
 		if (poolIds!=null)		for (String poolId : poolIds)		command += " --pool="+poolId;
 		if (productIds!=null)	for (String productId : productIds)	command += " --product="+productId;
 		if (regtokens!=null)	for (String regtoken : regtokens)	command += " --regtoken="+regtoken;
 		if (email!=null)											command += " --email="+email;
 		if (locale!=null)											command += " --locale="+locale;
 		if (proxy!=null)											command += " --proxy="+proxy;
 		if (proxyuser!=null)										command += " --proxyuser="+proxyuser;
 		if (proxypassword!=null)									command += " --proxypassword="+proxypassword;
 
 		// run command without asserting results
 		return sshCommandRunner.runCommandAndWait(command);
 	}
 	
 	public SSHCommandResult subscribe(String poolId, String productId, String regtoken, String email, String locale, String proxy, String proxyuser, String proxypassword) {
 
 		SSHCommandResult sshCommandResult = subscribe_(poolId, productId, regtoken, email, locale, proxy, proxyuser, proxypassword);
 		
 		// assert results...
 		
 		// if already subscribed, just return the result
 		// This consumer is already subscribed to the product matching pool with id 'ff8080812c71f5ce012c71f6996f0132'
 		if (sshCommandResult.getStdout().startsWith("This consumer is already subscribed")) return sshCommandResult;	
 
 		// assert the subscribe does NOT report "Entitlement Certificate\\(s\\) update failed due to the following reasons:"
 		Assert.assertContainsNoMatch(sshCommandResult.getStdout(), "Entitlement Certificate\\(s\\) update failed due to the following reasons:","Entitlement Certificate updates should be successful when subscribing.");
 
 		// assert that the entitlement pool was found for subscribing
 		Assert.assertTrue(!sshCommandResult.getStdout().startsWith("No such entitlement pool:"), "The subscription pool was found.");
 		
 		// assert the exit code was a success
 		Assert.assertEquals(sshCommandResult.getExitCode(), Integer.valueOf(0), "The exit code from the subscribe command indicates a success.");
 		
 		return sshCommandResult;
 	}
 	
 	public SSHCommandResult subscribe(List<String> poolIds, List<String> productIds, List<String> regtokens, String email, String locale, String proxy, String proxyuser, String proxypassword) {
 
 		SSHCommandResult sshCommandResult = subscribe_(poolIds, productIds, regtokens, email, locale, proxy, proxyuser, proxypassword);
 		
 		// assert results
 		Assert.assertContainsNoMatch(sshCommandResult.getStdout(), "Entitlement Certificate\\(s\\) update failed due to the following reasons:","Entitlement Certificate updates should be successful when subscribing.");
 		if (sshCommandResult.getStderr().startsWith("This consumer is already subscribed")) return sshCommandResult;
 		Assert.assertEquals(sshCommandResult.getExitCode(), Integer.valueOf(0), "The exit code from the subscribe command indicates a success.");
 		return sshCommandResult;
 	}
 	
 	public void subscribeToProduct(String product) {
 		RemoteFileTasks.runCommandExpectingNonzeroExit(sshCommandRunner,"subscription-manager-cli subscribe --product="+product);
 	}
 	
 	/**
 	 * subscribe to the given SubscriptionPool and return the newly dropped EntitlementCert file to the newly consumed ProductSubscriptions 
 	 * @param pool
 	 * @return
 	 */
 	public File subscribeToSubscriptionPool(SubscriptionPool pool) {
 		List<ProductSubscription> beforeProductSubscriptions = getCurrentlyConsumedProductSubscriptions();
 		List<File> beforeEntitlementCertFiles = getCurrentEntitlementCertFiles();
 		File newCertFile = null;
 		log.info("Subscribing to subscription pool: "+pool);
 		SSHCommandResult sshCommandResult = subscribe(pool.poolId, null, null, null, null, null, null, null);
 
 		// assert that the remaining SubscriptionPools does NOT contain the pool just subscribed to
 		List<SubscriptionPool> afterSubscriptionPools = getCurrentlyAvailableSubscriptionPools();
 		Assert.assertTrue(!afterSubscriptionPools.contains(pool),
 				"The available subscription pools no longer contains the just subscribed to pool: "+pool);
 		
 		// assert that the remaining SubscriptionPools do NOT contain the same productId just subscribed to
 		for (SubscriptionPool afterSubscriptionPool : afterSubscriptionPools) {
 			Assert.assertTrue(!afterSubscriptionPool.productId.equals(pool.productId),
 					"This remaining available pool "+afterSubscriptionPool+" does NOT contain the same productId ("+pool.productId+") after subscribing to pool: "+pool);
 		}
 
 		// when the pool is already subscribe to...
 		if (sshCommandResult.getStdout().startsWith("This consumer is already subscribed")) {
 			
 			// find the existing entitlement cert file corresponding to the already subscribed pool
 			EntitlementCert entitlementCert = null;
 			for (File thisEntitlementCertFile : getCurrentEntitlementCertFiles()) {
 				EntitlementCert thisEntitlementCert = getEntitlementCertFromEntitlementCertFile(thisEntitlementCertFile);
 				if (thisEntitlementCert.orderNamespace.productId.equals(pool.productId)) {
 					entitlementCert = thisEntitlementCert;
 					break;
 				}
 			}
 			
 			Assert.assertNotNull(entitlementCert, "Found an already existing Entitlement Cert whose productId matches the productId from the subscription pool: "+pool);
 			newCertFile = getEntitlementCertFileFromEntitlementCert(entitlementCert); // not really new, just already existing
 		
 			// assert that NO new entitlement cert file has been dropped in /etc/pki/entitlement
 			List<File> afterEntitlementCertFiles = getCurrentEntitlementCertFiles("-t"); // sorted with the newest at index 0
 			Assert.assertEquals(afterEntitlementCertFiles.size(),beforeEntitlementCertFiles.size(),
 					"The entitlement certificate file count has not changed (from "+beforeEntitlementCertFiles.size()+" to "+afterEntitlementCertFiles.size()+") since the productId of the pool we are trying to subscribe to is already consumed.");
 
 			// assert that consumed ProductSubscriptions has NOT changed
 			List<ProductSubscription> afterProductSubscriptions = getCurrentlyConsumedProductSubscriptions();
 			Assert.assertTrue(afterProductSubscriptions.size() == beforeProductSubscriptions.size() && afterProductSubscriptions.size() > 0,
 					"The list of currently consumed product subscriptions has not changed (from "+beforeProductSubscriptions.size()+" to "+afterProductSubscriptions.size()+") since the productId of the pool we are trying to subscribe to is already consumed.");
 
 		// otherwise, when the pool is NOT already subscribe to...
 		} else {
 	
 			// assert that a new entitlement cert file has been dropped in /etc/pki/entitlement
 			List<File> afterEntitlementCertFiles = getCurrentEntitlementCertFiles("-t"); // sorted with the newest at index 0
 			newCertFile = afterEntitlementCertFiles.get(0);
 			Assert.assertTrue(afterEntitlementCertFiles.size()>0 && !beforeEntitlementCertFiles.contains(newCertFile),
 					"A new entitlement certificate has been dropped after subscribing to pool: "+pool);
 	
 			// assert that only ONE new entitlement cert file has been dropped in /etc/pki/entitlement
 			// https://bugzilla.redhat.com/show_bug.cgi?id=640338
 			Assert.assertTrue(afterEntitlementCertFiles.size()==beforeEntitlementCertFiles.size()+1,
 					"Only ONE new entitlement certificate (got '"+String.valueOf(afterEntitlementCertFiles.size()-beforeEntitlementCertFiles.size())+"' new certs; total is now '"+afterEntitlementCertFiles.size()+"') has been dropped after subscribing to pool: "+pool);
 	
 			log.info("The new entitlement certificate file is: "+newCertFile);
 			
 			// assert that the productId from the pool matches the entitlement productId
 			// TEMPORARY WORKAROUND FOR BUG: https://bugzilla.redhat.com/show_bug.cgi?id=650278 - jsefler 11/5/2010
 			boolean invokeWorkaroundWhileBugIsOpen = true;
 			try {String bugId="650278"; if (invokeWorkaroundWhileBugIsOpen&&BzChecker.getInstance().isBugOpen(bugId)) {log.fine("Invoking workaround for "+BzChecker.getInstance().getBugState(bugId).toString()+" Bugzilla bug "+bugId+".  (https://bugzilla.redhat.com/show_bug.cgi?id="+bugId+")");} else {invokeWorkaroundWhileBugIsOpen=false;}} catch (XmlRpcException xre) {/* ignore exception */} catch (RuntimeException re) {/* ignore exception */}
 			if (invokeWorkaroundWhileBugIsOpen) {
 				log.warning("skipping assert that the productId from the pool matches the entitlement productId");
 			// END OF WORKAROUND
 			} else {		
 				EntitlementCert entitlementCert = getEntitlementCertFromEntitlementCertFile(newCertFile);
 				if (pool.subscriptionName.equals(System.getProperty("sm.rhpersonal.subproductName", "Property 'sm.rhpersonal.subproductName' must be set."))) // special case when pool is really a subpool
 					Assert.assertEquals(entitlementCert.orderNamespace.productId, System.getProperty("sm.rhpersonal.productId", "Property 'sm.rhpersonal.productId' must be set."), "New EntitlementCert productId '"+entitlementCert.orderNamespace.productId+"' matches personal SubscriptionPool productId '"+System.getProperty("sm.rhpersonal.productId")+"' after subscribing to the subpool: "+pool);					
 				else
 					Assert.assertEquals(entitlementCert.orderNamespace.productId, pool.productId, "New EntitlementCert productId '"+entitlementCert.orderNamespace.productId+"' matches originating SubscriptionPool productId '"+pool.productId+"' after subscribing to the pool.");
 			}
 		
 			// assert that consumed ProductSubscriptions has NOT decreased
 			List<ProductSubscription> afterProductSubscriptions = getCurrentlyConsumedProductSubscriptions();
 			Assert.assertTrue(afterProductSubscriptions.size() >= beforeProductSubscriptions.size() && afterProductSubscriptions.size() > 0,
 					"The list of currently consumed product subscriptions has increased (from "+beforeProductSubscriptions.size()+" to "+afterProductSubscriptions.size()+"), or has remained the same after subscribing (using poolID="+pool.poolId+") to pool: "+pool+"  Note: The list of consumed product subscriptions can remain the same when all the products from this subscription pool are a subset of those from a previously subscribed pool.");
 		}
 		
 		return newCertFile;
 	}
 	
 	//@Deprecated
 	public File subscribeToSubscriptionPoolUsingProductId(SubscriptionPool pool) {
 		log.warning("Subscribing to a Subscription Pool using --product Id has been removed in subscription-manager-0.71-1.el6.i686.  Forwarding this subscribe request to use --pool Id...");
 		return subscribeToSubscriptionPoolUsingPoolId(pool);
 		
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
 	
 	public File subscribeToSubscriptionPoolUsingPoolId(SubscriptionPool pool/*, boolean withPoolID*/){
 		return subscribeToSubscriptionPool(pool);
 		
 		/* jsefler 11/22/2010
 		if(withPoolID){
 			log.info("Subscribing to pool with poolId: "+ pool.poolId);
 			sshCommandRunner.runCommandAndWait("subscription-manager-cli subscribe --pool="+pool.poolId);
 		}
 		else{
 			log.info("Subscribing to pool with productId: "+ pool.productId);
 			sshCommandRunner.runCommandAndWait("subscription-manager-cli subscribe --product=\""+pool.productId+"\"");
 		}
 		Assert.assertTrue(getCurrentlyConsumedProductSubscriptions().size() > 0,
 				"Successfully subscribed to pool with pool ID: "+ pool.poolId +" and pool name: "+ pool.subscriptionName);
 		//TODO: add in more thorough product subscription verification
 		// first improvement is to assert that the count of consumedProductIDs is at least one greater than the count of consumedProductIDs before the new pool was subscribed to.
 		*/
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
 			subscribeToSubscriptionPool(pool);
 		}
 		
 		// assert
 		assertNoAvailableSubscriptionPoolsToList("Asserting that no available subscription pools remain after individually subscribing to them all.");
 	}
 	
 	
 	/**
 	 * Collectively subscribe to all of the currently available subscription pools in one command call
 	 * @param assumingRegisterType - "system" or "candlepin"
 	 */
 	public void subscribeToAllOfTheCurrentlyAvailableSubscriptionPools(ConsumerType assumingRegisterType) {
 
 		// assemble a list of all the available SubscriptionPool ids
 		List <String> poolIds = new ArrayList<String>();
 		List <SubscriptionPool> poolsBeforeSubscribe = getCurrentlyAvailableSubscriptionPools();
 		for (SubscriptionPool pool : poolsBeforeSubscribe) {
 			poolIds.add(pool.poolId);
 		}
 		if (!poolIds.isEmpty()) subscribe(poolIds, null, null, null, null, null, null, null);
 		
 		// assert results when assumingRegisterType="system"
 		if (assumingRegisterType==null || assumingRegisterType.equals(ConsumerType.system)) {
 			assertNoAvailableSubscriptionPoolsToList("Asserting that no available subscription pools remain after simultaneously subscribing to them all.");
 			return;
 		}
 		
 		// assert results when assumingRegisterType="candlepin"
 		else if (assumingRegisterType.equals(ConsumerType.candlepin)) {
 			List <SubscriptionPool> poolsAfterSubscribe = getCurrentlyAvailableSubscriptionPools();
 			for (SubscriptionPool beforePool : poolsBeforeSubscribe) {
 				boolean foundPool = false;
 				for (SubscriptionPool afterPool : poolsAfterSubscribe) {
 					if (afterPool.equals(beforePool)) {
 						foundPool = true;
 						// assert the quantity has decremented;
 						Assert.assertEquals(Integer.valueOf(afterPool.quantity).intValue(), Integer.valueOf(beforePool.quantity).intValue()-1,
 								"The quantity of entitlements from subscription pool id '"+afterPool.poolId+"' has decremented by one.");
 						break;
 					}
 				}
 				if (!foundPool) {
 					Assert.fail("Could not find subscription pool "+beforePool+" listed after subscribing to it as a registered "+assumingRegisterType+" consumer.");
 				}
 			}
 			return;
 		}
 		
 		Assert.fail("Do not know how to assert subscribeToAllOfTheCurrentlyAvailableSubscriptionPools assumingRegisterType="+assumingRegisterType);
 	}
 //	public void subscribeToAllOfTheCurrentlyAvailableSubscriptionPools() {
 //
 //		// assemble a list of all the available SubscriptionPool ids
 //		List <Integer> poolIds = new ArrayList<Integer>();
 //		for (SubscriptionPool pool : getCurrentlyAvailableSubscriptionPools()) {
 //			poolIds.add(pool.poolId);
 //		}
 //		if (!poolIds.isEmpty()) subscribe(poolIds, null, null, null, null);
 //		
 //		// assert
 //		assertNoAvailableSubscriptionPoolsToList("Asserting that no available subscription pools remain after simultaneously subscribing to them all.");
 //	}
 	
 	public void assertNoAvailableSubscriptionPoolsToList(String assertMsg) {
 		boolean invokeWorkaroundWhileBugIsOpen = true;
 		
 		// TEMPORARY WORKAROUND FOR BUG: https://bugzilla.redhat.com/show_bug.cgi?id=613635 - jsefler 7/14/2010
 		invokeWorkaroundWhileBugIsOpen = true;
 		try {String bugId="613635"; if (invokeWorkaroundWhileBugIsOpen&&BzChecker.getInstance().isBugOpen(bugId)) {log.fine("Invoking workaround for "+BzChecker.getInstance().getBugState(bugId).toString()+" Bugzilla bug "+bugId+".  (https://bugzilla.redhat.com/show_bug.cgi?id="+bugId+")");} else {invokeWorkaroundWhileBugIsOpen=false;}} catch (XmlRpcException xre) {/* ignore exception */} catch (RuntimeException re) {/* ignore exception */}
 		if (invokeWorkaroundWhileBugIsOpen) {
 			Assert.assertContainsMatch(listAvailableSubscriptionPools().getStdout(),"^No Available subscription pools to list$",assertMsg);
 			return;
 		} // END OF WORKAROUND
 		
 		// TEMPORARY WORKAROUND FOR BUG: https://bugzilla.redhat.com/show_bug.cgi?id=622839 - jsefler 8/10/2010
 		invokeWorkaroundWhileBugIsOpen = true;
 		try {String bugId="622839"; if (invokeWorkaroundWhileBugIsOpen&&BzChecker.getInstance().isBugOpen(bugId)) {log.fine("Invoking workaround for "+BzChecker.getInstance().getBugState(bugId).toString()+" Bugzilla bug "+bugId+".  (https://bugzilla.redhat.com/show_bug.cgi?id="+bugId+")");} else {invokeWorkaroundWhileBugIsOpen=false;}} catch (XmlRpcException xre) {/* ignore exception */} catch (RuntimeException re) {/* ignore exception */}
 		if (invokeWorkaroundWhileBugIsOpen) {
 			Assert.assertContainsMatch(listAvailableSubscriptionPools().getStdout(),"^No Available subscription pools to list$",assertMsg);
 			return;
 		} // END OF WORKAROUND
 		
 		// TEMPORARY WORKAROUND FOR BUG: https://bugzilla.redhat.com/show_bug.cgi?id=623657 - jsefler 8/12/2010
 		invokeWorkaroundWhileBugIsOpen = true;
 		try {String bugId="623657"; if (invokeWorkaroundWhileBugIsOpen&&BzChecker.getInstance().isBugOpen(bugId)) {log.fine("Invoking workaround for "+BzChecker.getInstance().getBugState(bugId).toString()+" Bugzilla bug "+bugId+".  (https://bugzilla.redhat.com/show_bug.cgi?id="+bugId+")");} else {invokeWorkaroundWhileBugIsOpen=false;}} catch (XmlRpcException xre) {/* ignore exception */} catch (RuntimeException re) {/* ignore exception */}
 		if (invokeWorkaroundWhileBugIsOpen) {
 			Assert.assertContainsMatch(listAvailableSubscriptionPools().getStdout(),"^No Available subscription pools to list$",assertMsg);
 			return;
 		} // END OF WORKAROUND
 		
 		// assert
 		Assert.assertEquals(listAvailableSubscriptionPools().getStdout().trim(),
 				"No Available subscription pools to list",assertMsg);
 	}
 	
 	
 	
 	// unsubscribe module tasks ************************************************************
 
 	/**
 	 * unsubscribe without asserting results
 	 * @param proxy TODO
 	 * @param proxyuser TODO
 	 * @param proxypassword TODO
 	 */
 	public SSHCommandResult unsubscribe_(Boolean all, BigInteger serial, String proxy, String proxyuser, String proxypassword) {
 
 		// assemble the unsubscribe command
 		String command = this.command;	command += " unsubscribe";
 		if (all!=null && all)			command += " --all";
 		if (serial!=null)				command += " --serial="+serial;
 		if (proxy!=null)				command += " --proxy="+proxy;
 		if (proxyuser!=null)			command += " --proxyuser="+proxyuser;
 		if (proxypassword!=null)		command += " --proxypassword="+proxypassword;
 		
 		// run command without asserting results
 		return sshCommandRunner.runCommandAndWait(command);
 	}
 	
 	public SSHCommandResult unsubscribe(Boolean all, BigInteger serial, String proxy, String proxyuser, String proxypassword) {
 
 		SSHCommandResult sshCommandResult = unsubscribe_(all, serial, proxy, proxyuser, proxypassword);
 		
 		// assert results
 		Assert.assertEquals(sshCommandResult.getExitCode(), Integer.valueOf(0), "The exit code from the subscribe command indicates a success.");
 		return sshCommandResult;
 	}
 	
 	/**
 	 * unsubscribe from entitlement product certificate serial and assert results
 	 * @param serialNumber
 	 * @return - false when no unsubscribe took place
 	 */
 	public boolean unsubscribeFromSerialNumber(BigInteger serialNumber) {
 		String certFile = entitlementCertDir+"/"+serialNumber+".pem";
 		boolean certFileExists = RemoteFileTasks.testFileExists(sshCommandRunner,certFile)==1? true:false;
 		
 		log.info("Unsubscribing from certificate serial: "+ serialNumber);
 		SSHCommandResult result = unsubscribe_(Boolean.FALSE, serialNumber, null, null, null);
 		
 		// assert the results
 		if (!certFileExists) {
 			String regexForSerialNumber = serialNumber.toString();
 			
 			// TEMPORARY WORKAROUND FOR BUG: https://bugzilla.redhat.com/show_bug.cgi?id=639320 - jsefler 10/1/2010
 			boolean invokeWorkaroundWhileBugIsOpen = true;
 			String bugId="639320"; 
 			try {if (invokeWorkaroundWhileBugIsOpen&&BzChecker.getInstance().isBugOpen(bugId)) {log.fine("Invoking workaround for "+BzChecker.getInstance().getBugState(bugId).toString()+" Bugzilla bug "+bugId+".  (https://bugzilla.redhat.com/show_bug.cgi?id="+bugId+")");} else {invokeWorkaroundWhileBugIsOpen=false;}} catch (XmlRpcException xre) {/* ignore exception */} catch (RuntimeException re) {/* ignore exception */}
 			if (invokeWorkaroundWhileBugIsOpen) {
 				regexForSerialNumber = "[\\d,]*";
 			}
 			// END OF WORKAROUND
 						
 			Assert.assertContainsMatch(result.getStderr(), "Entitlement Certificate with serial number "+regexForSerialNumber+" could not be found.",
 				"Entitlement Certificate with serial "+serialNumber+" could not be removed since it was not found.");
 			Assert.assertEquals(result.getExitCode(), Integer.valueOf(255), "The unsubscribe should fail when its corresponding entitlement cert file ("+certFile+") does not exist.");
 			return false;
 		}
 		
 		// assert the certFileExists is removed
 		Assert.assertTrue(RemoteFileTasks.testFileExists(sshCommandRunner,certFile)==0,
 				"Entitlement Certificate with serial "+serialNumber+" ("+certFile+") has been removed.");
 
 		return true;
 	}
 	
 	/**
 	 * Unsubscribe from the given product subscription using its serial number.
 	 * @param productSubscription
 	 * @return - false when the productSubscription has already been unsubscribed at a previous time
 	 */
 	public boolean unsubscribeFromProductSubscription(ProductSubscription productSubscription) {
 		
 		log.info("Unsubscribing from product subscription: "+ productSubscription);
 		boolean unsubscribed = unsubscribeFromSerialNumber(productSubscription.serialNumber);
 		
 		Assert.assertTrue(!getCurrentlyConsumedProductSubscriptions().contains(productSubscription),
 				"The currently consumed product subscriptions does not contain product: "+productSubscription);
 
 		return unsubscribed;
 	}
 	
 	/**
 	 * Issues a call to "subscription-manager-cli unsubscribe" which will unsubscribe from
 	 * all currently consumed product subscriptions and then asserts the list --consumed is empty.
 	 */
 	public void unsubscribeFromAllOfTheCurrentlyConsumedProductSubscriptions() {
 
 		unsubscribe(Boolean.TRUE, null, null, null, null);
 
 		// assert that there are no product subscriptions consumed
 		Assert.assertEquals(listConsumedProductSubscriptions().getStdout().trim(),
 				"No Consumed subscription pools to list","Successfully unsubscribed from all consumed products.");
 		
 		// assert that there are no entitlement product cert files
 		Assert.assertTrue(sshCommandRunner.runCommandAndWait("find "+entitlementCertDir+" -name *.pem | grep -v key.pem").getStdout().equals(""),
 				"No entitlement product cert files exist after unsubscribing from all subscription pools.");
 
 		// assert that the yum redhat repo file is gone
 		/* bad assert...  the repo file is present but empty
 		Assert.assertTrue(RemoteFileTasks.testFileExists(sshCommandRunner, redhatRepoFile)==0,
 				"The redhat repo file '"+redhatRepoFile+"' has been removed after unsubscribing from all subscription pools.");
 		*/
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
 	
 	
 	
 	// facts module tasks ************************************************************
 	
 	/**
 	 * facts without asserting results
 	 * @param proxy TODO
 	 * @param proxyuser TODO
 	 * @param proxypassword TODO
 	 */
 	public SSHCommandResult facts_(Boolean list, Boolean update, String proxy, String proxyuser, String proxypassword) {
 
 		// assemble the register command
 		String command = this.command;	command += " facts";	
 		if (list!=null && list)			command += " --list";
 		if (update!=null && update)		command += " --update";
 		if (proxy!=null)				command += " --proxy="+proxy;
 		if (proxyuser!=null)			command += " --proxyuser="+proxyuser;
 		if (proxypassword!=null)		command += " --proxypassword="+proxypassword;
 		
 		// run command without asserting results
 		return sshCommandRunner.runCommandAndWait(command);
 	}
 	
 	/**
 	 * @param list
 	 * @param update
 	 * @param proxy TODO
 	 * @param proxyuser TODO
 	 * @param proxypassword TODO
 	 * @return
 	 */
 	public SSHCommandResult facts(Boolean list, Boolean update, String proxy, String proxyuser, String proxypassword) {
 		ConsumerCert consumerCert = getCurrentConsumerCert();
 		
 		SSHCommandResult sshCommandResult = facts_(list, update, proxy, proxyuser, proxypassword);
 
 		// assert results for a successful facts
 		Assert.assertEquals(sshCommandResult.getExitCode(), Integer.valueOf(0), "The exit code from the facts command indicates a success.");
 		String regex = "";
 		if (list!=null && list)		regex=".*:.*";					// list
 		if (update!=null && update)	regex=consumerCert.consumerid;	// consumerid
 
 		Assert.assertContainsMatch(sshCommandResult.getStdout().trim(), regex);
 		
 		return sshCommandResult; // from the facts command
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	/**
 	 * Assert that the given entitlement certs are displayed in the stdout from "yum repolist all".
 	 * @param entitlementCerts
 	 */
 	public void assertEntitlementCertsInYumRepolist(List<EntitlementCert> entitlementCerts, boolean areReported) {
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
 		
 		/* [root@jsefler-itclient01 product]# yum repolist all
 Loaded plugins: pidplugin, refresh-packagekit, rhnplugin, rhsmplugin
 Updating Red Hat repositories.
 INFO:repolib:repos updated: 0
 This system is not registered with RHN.
 RHN support will be disabled.
 red-hat-enterprise-linux-6-entitlement-alpha-rpms                                                                         | 4.0 kB     00:00     
 red-hat-enterprise-linux-6-entitlement-alpha-rpms-updates                                                                 |  951 B     00:00     
 repo id                                                                        repo name                                           status
 red-hat-enterprise-linux-6-entitlement-alpha-debug-rpms                        Red Hat Enterprise Linux 6 Entitlement Alpha (Debug disabled
 red-hat-enterprise-linux-6-entitlement-alpha-debug-rpms-updates                Red Hat Enterprise Linux 6 Entitlement Alpha (Debug disabled
 red-hat-enterprise-linux-6-entitlement-alpha-optional-debug-rpms               Red Hat Enterprise Linux 6 Entitlement Alpha - Opti disabled
 red-hat-enterprise-linux-6-entitlement-alpha-optional-debug-rpms-updates       Red Hat Enterprise Linux 6 Entitlement Alpha - Opti disabled
 red-hat-enterprise-linux-6-entitlement-alpha-optional-rpms                     Red Hat Enterprise Linux 6 Entitlement Alpha - Opti disabled
 red-hat-enterprise-linux-6-entitlement-alpha-optional-rpms-updates             Red Hat Enterprise Linux 6 Entitlement Alpha - Opti disabled
 red-hat-enterprise-linux-6-entitlement-alpha-optional-source-rpms              Red Hat Enterprise Linux 6 Entitlement Alpha - Opti disabled
 red-hat-enterprise-linux-6-entitlement-alpha-optional-source-rpms-updates      Red Hat Enterprise Linux 6 Entitlement Alpha - Opti disabled
 red-hat-enterprise-linux-6-entitlement-alpha-rpms                              Red Hat Enterprise Linux 6 Entitlement Alpha (RPMs) enabled: 3,394
 red-hat-enterprise-linux-6-entitlement-alpha-rpms-updates                      Red Hat Enterprise Linux 6 Entitlement Alpha (RPMs) enabled:     0
 red-hat-enterprise-linux-6-entitlement-alpha-source-rpms                       Red Hat Enterprise Linux 6 Entitlement Alpha (Sourc disabled
 red-hat-enterprise-linux-6-entitlement-alpha-source-rpms-updates               Red Hat Enterprise Linux 6 Entitlement Alpha (Sourc disabled
 red-hat-enterprise-linux-6-entitlement-alpha-supplementary-debug-rpms          Red Hat Enterprise Linux 6 Entitlement Alpha - Supp disabled
 red-hat-enterprise-linux-6-entitlement-alpha-supplementary-debug-rpms-updates  Red Hat Enterprise Linux 6 Entitlement Alpha - Supp disabled
 red-hat-enterprise-linux-6-entitlement-alpha-supplementary-rpms                Red Hat Enterprise Linux 6 Entitlement Alpha - Supp disabled
 red-hat-enterprise-linux-6-entitlement-alpha-supplementary-rpms-updates        Red Hat Enterprise Linux 6 Entitlement Alpha - Supp disabled
 red-hat-enterprise-linux-6-entitlement-alpha-supplementary-source-rpms         Red Hat Enterprise Linux 6 Entitlement Alpha - Supp disabled
 red-hat-enterprise-linux-6-entitlement-alpha-supplementary-source-rpms-updates Red Hat Enterprise Linux 6 Entitlement Alpha - Supp disabled
 repolist: 3,394
 		*/
 		
 		sshCommandRunner.runCommandAndWait("killall -9 yum");
 		
 		// assert all of the entitlement certs are reported in the stdout from "yum repolist all"
 		SSHCommandResult result = sshCommandRunner.runCommandAndWait("yum repolist all");	// FIXME, THIS SHOULD MAKE USE OF getYumRepolist
  		for (EntitlementCert entitlementCert : entitlementCerts) {
  			for (ContentNamespace contentNamespace : entitlementCert.contentNamespaces) {
 
  				// Note: When the repo id and repo name are really long, the repo name in the yum repolist all gets crushed (hence the reason for .* in the regex)
 				String regex = String.format("^%s\\s+(?:%s|.*)\\s+%s", contentNamespace.label.trim(), contentNamespace.name.substring(0,Math.min(contentNamespace.name.length(), 25)), contentNamespace.enabled.equals("1")? "enabled:":"disabled$");	// 25 was arbitraily picked to be short enough to be displayed by yum repolist all
 				if (areReported)
 					Assert.assertContainsMatch(result.getStdout(), regex);
 				else
 					Assert.assertContainsNoMatch(result.getStdout(), regex);
 	 		}
  		}
 
 		// assert that the sshCommandRunner.getStderr() does not contains an error on the entitlementCert.download_url e.g.: http://redhat.com/foo/path/never/repodata/repomd.xml: [Errno 14] HTTP Error 404 : http://www.redhat.com/foo/path/never/repodata/repomd.xml 
 		// FIXME EVENTUALLY WE NEED TO UNCOMMENT THIS ASSERT
 		//Assert.assertContainsNoMatch(result.getStderr(), "HTTP Error \\d+", "HTTP Errors were encountered when runnning yum repolist all.");
 	}
 	
 	/**
 	 * @param option [all|enabled|disabled]
 	 * @return
 	 */
 	public ArrayList<String> getYumRepolist(String option){
 		ArrayList<String> repos = new ArrayList<String>();
 		sshCommandRunner.runCommandAndWait("killall -9 yum");
 		
 		sshCommandRunner.runCommandAndWait("yum repolist "+option);
 		String[] availRepos = sshCommandRunner.getStdout().split("\\n");
 		
 		int repolistStartLn = 0;
 		int repolistEndLn = 0;
 		
 		for(int i=0;i<availRepos.length;i++)
 			if (availRepos[i].contains("repo id"))
 				repolistStartLn = i + 1;
 			else if (availRepos[i].contains("repolist:"))
 				repolistEndLn = i;
 		
 		for(int i=repolistStartLn;i<repolistEndLn;i++)
 			repos.add(availRepos[i].split(" ")[0]);
 		
 		return repos;
 	}
 	
 
 	public ArrayList<String> getYumListOfAvailablePackagesFromRepo (String repoLabel) {
 		ArrayList<String> packages = new ArrayList<String>();
 		sshCommandRunner.runCommandAndWait("killall -9 yum");
 
 		int min = 5;
 		log.fine("Using a timeout of "+min+" minutes for next command...");
 		//SSHCommandResult result = sshCommandRunner.runCommandAndWait("yum list available",Long.valueOf(min*60000));
 		SSHCommandResult result = sshCommandRunner.runCommandAndWait("yum list available --disablerepo=* --enablerepo="+repoLabel,Long.valueOf(min*60000));
 
 		// Example result.getStdout()
 		//xmltex.noarch                             20020625-16.el6                      red-hat-enterprise-linux-6-entitlement-alpha-rpms
 		//xmlto.x86_64                              0.0.23-3.el6                         red-hat-enterprise-linux-6-entitlement-alpha-rpms
 		//xmlto-tex.noarch                          0.0.23-3.el6                         red-hat-enterprise-linux-6-entitlement-alpha-rpms
 		//xorg-x11-apps.x86_64                      7.4-10.el6                           red-hat-enterprise-linux-6-entitlement-alpha-rpms
 
 		String regex="(\\S+) +(\\S+) +"+repoLabel+"$";
 		Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
 		Matcher matcher = pattern.matcher(result.getStdout());
 		if (!matcher.find()) {
 			log.fine("Did NOT find any available packages from repoLabel: "+repoLabel);
 			return packages;
 		}
 
 		// assemble the list of packages and return them
 		do {
 			packages.add(matcher.group(1)); // group(1) is the pkg,  group(2) is the version
 		} while (matcher.find());
 		return packages;		
 	}
 	
 	public ArrayList<String> getAvailablePackages (String disablerepo, String enablerepo, String globExpression) {
 		ArrayList<String> packages = new ArrayList<String>();
 		sshCommandRunner.runCommandAndWait("killall -9 yum");
 
 		String							command  = "yum list available";	
 		if (disablerepo!=null)			command += " --disablerepo="+disablerepo;
 		if (enablerepo!=null)			command += " --enablerepo="+enablerepo;
 		if (globExpression!=null)		command += " "+globExpression;
 		
 		// execute the yum command to list available packages
 		int min = 5;
 		log.fine("Using a timeout of "+min+" minutes for next command...");
 		SSHCommandResult result = sshCommandRunner.runCommandAndWait(command,Long.valueOf(min*60000));
 		
 		// Example result.getStderr() 
 		//	INFO:repolib:repos updated: 0
 		//	This system is not registered with RHN.
 		//	RHN support will be disabled.
 		//	Error: No matching Packages to list
 		if (result.getStderr().contains("Error: No matching Packages to list")) {
 			log.info("No matching Packages to list from: "+command);
 			return packages;
 		}
 		
 		// Example result.getStdout()
 		//xmltex.noarch                             20020625-16.el6                      red-hat-enterprise-linux-6-entitlement-alpha-rpms
 		//xmlto.x86_64                              0.0.23-3.el6                         red-hat-enterprise-linux-6-entitlement-alpha-rpms
 		//xmlto-tex.noarch                          0.0.23-3.el6                         red-hat-enterprise-linux-6-entitlement-alpha-rpms
 		//xorg-x11-apps.x86_64                      7.4-10.el6                           red-hat-enterprise-linux-6-entitlement-alpha-rpms
 		if (enablerepo==null||enablerepo.equals("*")) enablerepo="(\\S+)";
 		String regex="^(\\S+) +(\\S+) +"+enablerepo+"$";
 		Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
 		Matcher matcher = pattern.matcher(result.getStdout());
 		if (!matcher.find()) {
 			log.info("Did NOT find any available packages from: "+command);
 			return packages;
 		}
 
 		// assemble the list of packages and return them
 		do {
 			packages.add(matcher.group(1)); // group(1) is the pkg,  group(2) is the version
 		} while (matcher.find());
 		return packages;		
 	}
 	
 	public String findUniqueAvailablePackageFromRepo (String repo) {
 		
 		for (String pkg : getAvailablePackages("*",repo,null)) {
 			if (!getAvailablePackages(repo,null,pkg).contains(pkg)) {
 				return pkg;
 			}
 		}
 		return null;
 	}
 	
 	public void installPackageUsingYumFromRepo (String pkg, String repoLabel) {
 		RemoteFileTasks.runCommandAndAssert(sshCommandRunner,"yum -y install "+pkg, 0, "^Complete!$",null);
 		RemoteFileTasks.runCommandAndAssert(sshCommandRunner,"yum list installed "+pkg, 0, "^"+pkg+" .*"+repoLabel+"$",null);
 	}
 	
 	public void removePackageUsingYum (String pkg) {
 		RemoteFileTasks.runCommandAndAssert(sshCommandRunner,"yum -y remove "+pkg, 0, "^Complete!$",null);
 		RemoteFileTasks.runCommandAndAssert(sshCommandRunner,"yum list installed "+pkg, 1, null,"Error: No matching Packages to list");
 	}
 	
 	
 	public String getRedhatRelease() {
 //		// verify the grinder hostname is a rhel 5 machine
 //		log.info("Verifying prerequisite...  hostname '"+grinderHostname+"' is a Red Hat Enterprise Linux .* release 5 machine.");
 //		Assert.assertEquals(sshCommandRunner.runCommandAndWait("cat /etc/redhat-release | grep -E \"^Red Hat Enterprise Linux .* release 5.*\"").getExitCode(),Integer.valueOf(0),"Grinder hostname must be RHEL 5.*");
 		return sshCommandRunner.runCommandAndWait("cat /etc/redhat-release").getStdout();
 	}
 	
 	
 	// protected methods ************************************************************
 
 	protected boolean poolsNoLongerAvailable(ArrayList<SubscriptionPool> beforeSubscription, ArrayList<SubscriptionPool> afterSubscription) {
 		for(SubscriptionPool beforePool:beforeSubscription)
 			if (afterSubscription.contains(beforePool))
 				return false;
 		return true;
 	}
 	
 	protected void runRHSMCallAsLang(String lang,String rhsmCall){
 		sshCommandRunner.runCommandAndWait("export LANG="+lang+"; " + rhsmCall);
 	}
 	
 	protected void setLanguage(String lang){
 		sshCommandRunner.runCommandAndWait("export LANG="+lang);
 	}
 }
