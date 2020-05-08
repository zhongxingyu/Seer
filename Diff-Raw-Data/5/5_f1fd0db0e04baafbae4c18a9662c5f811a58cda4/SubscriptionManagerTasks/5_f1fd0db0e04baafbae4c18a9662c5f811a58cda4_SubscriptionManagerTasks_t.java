 package com.redhat.qe.sm.cli.tasks;
 
 import java.io.File;
 import java.io.IOException;
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.LogRecord;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.xmlrpc.XmlRpcException;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.redhat.qe.auto.testng.Assert;
 import com.redhat.qe.auto.testng.BzChecker;
 import com.redhat.qe.auto.testng.LogMessageUtil;
 import com.redhat.qe.sm.base.ConsumerType;
 import com.redhat.qe.sm.base.SubscriptionManagerBaseTestScript;
 import com.redhat.qe.sm.base.SubscriptionManagerCLITestScript;
 import com.redhat.qe.sm.data.ConsumerCert;
 import com.redhat.qe.sm.data.ContentNamespace;
 import com.redhat.qe.sm.data.EntitlementCert;
 import com.redhat.qe.sm.data.InstalledProduct;
 import com.redhat.qe.sm.data.ProductCert;
 import com.redhat.qe.sm.data.ProductNamespace;
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
 	public final String rhsmFactsJsonFile	= "/var/lib/rhsm/facts/facts.json";
 	public final String rhnSystemIdFile		= "/etc/sysconfig/rhn/systemid";
 	public final String factsDir			= "/etc/rhsm/facts";
 	public final String rhsmComplianceD		= "/usr/libexec/rhsm-complianced";
 	public final String varLogMessagesFile	= "/var/log/messages";
 	
 	// will be initialized by initializeFieldsFromConfigFile()
 	public String productCertDir				= null; // "/etc/pki/product";
 	public String entitlementCertDir			= null; // "/etc/pki/entitlement";
 	public String consumerCertDir				= null; // "/etc/pki/consumer";
 	public String caCertDir						= null; // "/etc/rhsm/ca";
 	public String consumerKeyFile				= null; // consumerCertDir+"/key.pem";
 	public String consumerCertFile				= null; // consumerCertDir+"/cert.pem";
 
 	
 	public String hostname						= null;	// of the client
 	public String arch							= null;	// of the client
 	public String sockets						= null;	// of the client
 	public String variant						= null;	// of the client
 	
 	protected String currentAuthenticator				= null;	// most recent username used during register
 	protected String currentAuthenticatorPassword		= null;	// most recent password used during register
 	
 	public SubscriptionManagerTasks(SSHCommandRunner runner) {
 		super();
 		setSSHCommandRunner(runner);
 		hostname = sshCommandRunner.runCommandAndWait("hostname").getStdout().trim();
 		arch = sshCommandRunner.runCommandAndWait("uname -i").getStdout().trim();
 		
 		SSHCommandResult redhatReleaseResult = sshCommandRunner.runCommandAndWait("cat /etc/redhat-release");
 		if (redhatReleaseResult.getStdout().contains("Server")) variant = "Server";
 		if (redhatReleaseResult.getStdout().contains("Client")) variant = "Client";
 		if (redhatReleaseResult.getStdout().contains("Workstation")) variant = "Workstation";
 		if (redhatReleaseResult.getStdout().contains("ComputeNode")) variant = "ComputeNode";
 		if (redhatReleaseResult.getStdout().contains("release 5")) sockets = sshCommandRunner.runCommandAndWait("dmidecode | grep 'Socket Designation' | grep 'CPU' | wc -l").getStdout().trim();
 		if (redhatReleaseResult.getStdout().contains("release 6")) sockets = sshCommandRunner.runCommandAndWait("lscpu | grep 'CPU socket'").getStdout().split(":")[1].trim();
 	}
 	
 	public void setSSHCommandRunner(SSHCommandRunner runner) {
 		sshCommandRunner = runner;
 	}
 	
 	/**
 	 * Must be called after installSubscriptionManagerRPMs(...)
 	 */
 	public void initializeFieldsFromConfigFile() {
 		if (RemoteFileTasks.testFileExists(sshCommandRunner, rhsmConfFile)==1) {
 			this.consumerCertDir	= getConfFileParameter(rhsmConfFile, "consumerCertDir").replaceFirst("/$", "");
 			this.entitlementCertDir	= getConfFileParameter(rhsmConfFile, "entitlementCertDir").replaceFirst("/$", "");
 			this.productCertDir		= getConfFileParameter(rhsmConfFile, "productCertDir").replaceFirst("/$", "");
 			this.caCertDir			= getConfFileParameter(rhsmConfFile, "ca_cert_dir").replaceFirst("/$", "");
 			this.consumerCertFile	= consumerCertDir+"/cert.pem";
 			this.consumerKeyFile	= consumerCertDir+"/key.pem";
 			log.info(this.getClass().getSimpleName()+".initializeFieldsFromConfigFile() succeeded on '"+sshCommandRunner.getConnection().getHostname()+"'.");
 		} else {
 			log.warning("Cannot "+this.getClass().getSimpleName()+".initializeFieldsFromConfigFile() on '"+sshCommandRunner.getConnection().getHostname()+"' until file exists: "+rhsmConfFile);
 		}
 	}
 	
 	
 	/**
 	 * Must be called after initializeFieldsFromConfigFile(...)
 	 * @param repoCaCertUrls
 	 */
 	public void installRepoCaCerts(List<String> repoCaCertUrls) {
 		// transfer copies of CA certs that cane be used when generating yum repo configs 
 		for (String repoCaCertUrl : repoCaCertUrls) {
 			String repoCaCert = Arrays.asList(repoCaCertUrl.split("/")).get(repoCaCertUrl.split("/").length-1);
 			log.info("Copying repo CA cert '"+repoCaCert+"' from "+repoCaCertUrl+"...");
 			//File repoCaCertFile = new File(serverCaCertDir.replaceFirst("/$","/")+Arrays.asList(repoCaCertUrl.split("/|=")).get(repoCaCertUrl.split("/|=").length-1));
 			RemoteFileTasks.runCommandAndAssert(sshCommandRunner,"cd "+caCertDir+"; wget --no-clobber --no-check-certificate \""+repoCaCertUrl+"\"",Integer.valueOf(0),null,"“"+repoCaCert+"” saved|File “"+repoCaCert+"” already there");
 		}
 	}
 	
 	
 	/**
 	 * Must be called after initializeFieldsFromConfigFile(...)
 	 * @param repoCaCertFile
 	 * @param toNewName
 	 * @throws IOException
 	 */
 	public void installRepoCaCert(File repoCaCertFile, String toNewName) throws IOException {
 		if (repoCaCertFile==null) return;
 		if (toNewName==null) toNewName = repoCaCertFile.getName();
 		
 		// transfer the CA Cert File from the candlepin server to the clients so we can test in secure mode
 		RemoteFileTasks.putFile(sshCommandRunner.getConnection(), repoCaCertFile.getPath(), caCertDir+"/"+toNewName, "0644");
 		updateConfFileParameter(rhsmConfFile, "insecure", "0");
 	}
 	
 	
 	/**
 	 * Must be called after installProductCerts(...)
 	 * @param productCerts
 	 * @throws IOException
 	 */
 	public void installProductCerts(List <File> productCerts) throws IOException {
		if (productCerts.size() > 0) {
			// directory must exist otherwise the copy will fail
			sshCommandRunner.runCommandAndWait("mkdir -p "+productCertDir);
		}

 		for (File file : productCerts) {
 			RemoteFileTasks.putFile(sshCommandRunner.getConnection(), file.getPath(), productCertDir+"/", "0644");
 		}
 	}
 
 	public void installSubscriptionManagerRPMs(List<String> rpmUrls, String enablerepofordeps) {
 
 		// verify the subscription-manager client is a rhel 6 machine
 //		log.info("Verifying prerequisite...  client hostname '"+sshCommandRunner.getConnection().getHostname()+"' is a Red Hat Enterprise Linux .* release 6 machine.");
 //		Assert.assertEquals(sshCommandRunner.runCommandAndWait("cat /etc/redhat-release | grep -E \"^Red Hat Enterprise Linux .* release 6.*\"").getExitCode(),Integer.valueOf(0),
 //				sshCommandRunner.getConnection().getHostname()+" must be RHEL 6.*");
 
 		// make sure the client's time is accurate
 		RemoteFileTasks.runCommandAndAssert(sshCommandRunner, "service ntpd stop; ntpdate clock.redhat.com; service ntpd start; chkconfig ntpd on", /*Integer.valueOf(0) DON"T CHECK EXIT CODE SINCE IT RETURNS 1 WHEN STOP FAILS EVEN THOUGH START SUCCEEDS*/null, "Starting ntpd:\\s+\\[  OK  \\]", null);
 
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
 			for (String pkg : new String[]{"subscription-manager-firstboot","subscription-manager-gnome","subscription-manager","python-rhsm"}) {
 				sshCommandRunner.runCommandAndWait("rpm -e "+pkg);
 				RemoteFileTasks.runCommandAndAssert(sshCommandRunner,"rpm -q "+pkg,Integer.valueOf(1),"package "+pkg+" is not installed",null);
 			}
 		}
 
 		// install new rpms
 		for (String rpmUrl : rpmUrls) {
 			rpmUrl = rpmUrl.trim();
 			log.info("Installing RPM from "+rpmUrl+"...");
 			String sm_rpm = "/tmp/"+Arrays.asList(rpmUrl.split("/|=")).get(rpmUrl.split("/|=").length-1);
 			RemoteFileTasks.runCommandAndAssert(sshCommandRunner,"wget -O "+sm_rpm+" --no-check-certificate \""+rpmUrl.trim()+"\"",Integer.valueOf(0),null,"."+sm_rpm+". saved");
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
 		log.info("Updating config file '"+confFile+"' parameter '"+parameter+"' value to: "+value);
 		Assert.assertEquals(
 				RemoteFileTasks.searchReplaceFile(sshCommandRunner, confFile, "^"+parameter+"\\s*=.*$", parameter+"="+value.replaceAll("\\/", "\\\\/")),
 				0,"Updated '"+confFile+"' parameter '"+parameter+"' to value: " + value);
 		
 		// also update this "cached" value for these config file parameters
 		if (parameter.equals("consumerCertDir"))	this.consumerCertDir = value;
 		if (parameter.equals("entitlementCertDir"))	this.entitlementCertDir = value;
 		if (parameter.equals("productCertDir"))		this.productCertDir = value;
 		if (parameter.equals("ca_cert_dir"))		this.caCertDir = value;
 	}
 	
 	public void commentConfFileParameter(String confFile, String parameter){
 		log.info("Commenting out config file '"+confFile+"' parameter: "+parameter);
 		Assert.assertEquals(
 				RemoteFileTasks.searchReplaceFile(sshCommandRunner, confFile, "^"+parameter+"\\s*=", "#"+parameter+"="),
 				0,"Commented '"+confFile+"' parameter: "+parameter);
 	}
 	
 	public void uncommentConfFileParameter(String confFile, String parameter){
 		log.info("Uncommenting config file '"+confFile+"' parameter: "+parameter);
 		Assert.assertEquals(
 				RemoteFileTasks.searchReplaceFile(sshCommandRunner, confFile, "^#\\s*"+parameter+"\\s*=", parameter+"="),
 				0,"Uncommented '"+confFile+"' parameter: "+parameter);
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
 				
 		// TEMPORARY WORKAROUND FOR BUG: https://bugzilla.redhat.com/show_bug.cgi?id=691137 - jsefler 03/26/2011
 		if (this.arch.equals("s390x") || this.arch.equals("ppc64")) {
 			boolean invokeWorkaroundWhileBugIsOpen = true;
 			String bugId="691137"; 
 			try {if (invokeWorkaroundWhileBugIsOpen&&BzChecker.getInstance().isBugOpen(bugId)) {log.fine("Invoking workaround for "+BzChecker.getInstance().getBugState(bugId).toString()+" Bugzilla bug "+bugId+".  (https://bugzilla.redhat.com/show_bug.cgi?id="+bugId+")");} else {invokeWorkaroundWhileBugIsOpen=false;}} catch (XmlRpcException xre) {/* ignore exception */} catch (RuntimeException re) {/* ignore exception */}
 			if (invokeWorkaroundWhileBugIsOpen) {
 				RemoteFileTasks.runCommandAndWait(sshCommandRunner,"service rhsmcertd restart", LogMessageUtil.action());
 			} else {
 				RemoteFileTasks.runCommandAndAssert(sshCommandRunner,"service rhsmcertd restart",Integer.valueOf(0),"^Starting rhsmcertd "+certFrequency+"\\[  OK  \\]$",null);	
 			}
 		} else {
 		// END OF WORKAROUND
 		RemoteFileTasks.runCommandAndAssert(sshCommandRunner,"service rhsmcertd restart",Integer.valueOf(0),"^Starting rhsmcertd "+certFrequency+"\\[  OK  \\]$",null);	
 		}
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
 	
 	public void waitForRegexInRhsmcertdLog(String logRegex, int timeoutMinutes) {
 		RemoteFileTasks.runCommandAndAssert(sshCommandRunner,"tail -1 "+rhsmcertdLogFile,Integer.valueOf(0));
 		int retryMilliseconds = Integer.valueOf(getConfFileParameter(rhsmConfFile, "certFrequency"))*60*1000;  // certFrequency is in minutes
 		int t = 0;
 		
 		while(!sshCommandRunner.runCommandAndWait("tail -1 "+rhsmcertdLogFile).getStdout().trim().matches(logRegex) && (t*retryMilliseconds < timeoutMinutes*60*1000)) {
 			// pause for the sleep interval
 			SubscriptionManagerCLITestScript.sleep(retryMilliseconds); t++;	
 		}
 		if (t*retryMilliseconds > timeoutMinutes*60*1000) sshCommandRunner.runCommandAndWait("tail -24 "+rhsmLogFile);
 		
 		// assert that the state was achieved within the timeout
 		Assert.assertFalse((t*retryMilliseconds > timeoutMinutes*60*1000), "The rhsmcertd log matches '"+logRegex+"' within '"+t*retryMilliseconds+"' milliseconds (timeout="+timeoutMinutes+" min)");
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
 		if (RemoteFileTasks.testFileExists(sshCommandRunner, this.consumerCertFile)!=1) {
 			log.info("Currently, there is no consumer registered.");
 			return null;
 		}
 		sshCommandRunner.runCommandAndWait("openssl x509 -noout -text -in "+this.consumerCertFile);
 		String certificate = sshCommandRunner.getStdout();
 		return ConsumerCert.parse(certificate);
 	}
 	
 	/**
 	 * @return from the contents of the current /etc/pki/consumer/cert.pem
 	 */
 	public String getCurrentConsumerId() {
 		ConsumerCert currentConsumerCert = getCurrentConsumerCert();
 		if (currentConsumerCert==null) return null;
 		return currentConsumerCert.consumerid;
 	}
 	
 	/**
 	 * @param registerResult
 	 * @return from the stdout of the register command
 	 */
 	public String getCurrentConsumerId(SSHCommandResult registerResult) {
 		
 		// Example stdout:
 		// ca3f9b32-61e7-44c0-94c1-ce328f7a15b0 jsefler.usersys.redhat.com
 		
 		// Example stdout:
 		// The system with UUID 4e3675b1-450a-4066-92da-392c204ca5c7 has been unregistered
 		// ca3f9b32-61e7-44c0-94c1-ce328f7a15b0 testuser1
 		
 		//return registerResult.getStdout().split(" ")[0]; // FIXME: fails when stdout format is: ca3f9b32-61e7-44c0-94c1-ce328f7a15b0 testuser1 
 		
 		Pattern pattern = Pattern.compile("^[a-f,0-9,\\-]{36} [^ ]*$", Pattern.MULTILINE/* | Pattern.DOTALL*/);
 		Matcher matcher = pattern.matcher(registerResult.getStdout());
 		Assert.assertTrue(matcher.find(),"Found the registered UUID and name in the register result."); 
 		return matcher.group().split(" ")[0];
 	}
 	
 	/**
 	 * @param factName
 	 * @return The fact value that subscription-manager lists for factName is returned.  If factName is not listed, null is returned.
 	 */
 	public String getFactValue(String factName) {
 		SSHCommandResult result = facts_(true, false, null, null, null);
 		
 		// # subscription-manager facts --list
 		// cpu.architecture: x86_64
 		// cpu.bogomips: 4600.03
 		// cpu.core(s)_per_socket: 1
 		// cpu.cpu(s): 2
 		// uname.sysname: Linux
 		// uname.version: #1 SMP Mon Mar 21 10:20:35 EDT 2011
 		// virt.host_type: ibm_systemz
 		// ibm_systemz-zvm
 		// uname.sysname: Linux
 		// network.ipaddr: 10.16.66.203
 		// system.compliant: False
 		
 		String factNameRegex = factName.replaceAll("\\(","\\\\(").replaceAll("\\)","\\\\)");
 		//String regex=factNameRegex+":(.*)";	// works with single-line fact values
 		String regex=factNameRegex+":(.*(\n.*?)+)(?:^.+?:|$)";	// works with multi-line fact values
 		
 		Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
 		Matcher matcher = pattern.matcher(result.getStdout());
 		//Assert.assertTrue(matcher.find(),"Found fact "+factName);
 		if (!matcher.find()) {
 			log.warning("Did not find fact '"+factName+"'.");
 			return null;
 		}
 
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
 	public Map<BigInteger, SubscriptionPool> getCurrentSerialMapToSubscriptionPools(String owner, String password) throws Exception  {
 		
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
 		if (name!=null)								command += " --name="+String.format(name.contains("\"")?"'%s'":"\"%s\"", name./*escape backslashes*/replace("\\", "\\\\")./*escape backticks*/replace("`", "\\`"));
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
 		this.currentAuthenticator = null;
 		this.currentAuthenticatorPassword = null;
 		
 		// assert results for a successful registration
 		if (sshCommandResult.getStdout().startsWith("This system is already registered.")) return sshCommandResult;
 		Assert.assertEquals(sshCommandResult.getExitCode(), Integer.valueOf(0), "The exit code from the register command indicates a success.");
 		if (type==ConsumerType.person) name = username;		// https://bugzilla.redhat.com/show_bug.cgi?id=661130
 		if (name==null) name = this.hostname;				// https://bugzilla.redhat.com/show_bug.cgi?id=669395
 		Assert.assertContainsMatch(sshCommandResult.getStdout().trim(), "[a-f,0-9,\\-]{36} "+name);
 		
 		// assert that register with consumerId returns the expected uuid
 		if (consumerId!=null) {
 			//Assert.assertEquals(sshCommandResult.getStdout().trim(), consumerId+" "+username, "register to an exiting consumer was a success");
 			Assert.assertContainsMatch(sshCommandResult.getStdout().trim(), "^"+consumerId, "register to an exiting consumer was a success");	// removed name from assert to account for https://bugzilla.redhat.com/show_bug.cgi?id=669395
 		}
 		
 		// assert certificate files are installed into /etc/pki/consumer
 		Assert.assertEquals(RemoteFileTasks.testFileExists(sshCommandRunner,this.consumerKeyFile),1, "Consumer key file '"+this.consumerKeyFile+"' must exist after register.");
 		Assert.assertEquals(RemoteFileTasks.testFileExists(sshCommandRunner,this.consumerCertFile),1, "Consumer cert file '"+this.consumerCertFile+"' must exist after register.");
 		this.currentAuthenticator = username;
 		this.currentAuthenticatorPassword = password;
 		
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
 		this.currentAuthenticator = null;
 		this.currentAuthenticatorPassword = null;
 		
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
 	 * @param username
 	 * @param password
 	 * @param regenerate
 	 * @param force
 	 * @param proxy
 	 * @param proxyuser
 	 * @param proxypassword
 	 * @return
 	 */
 	public SSHCommandResult identity_(String username, String password, Boolean regenerate, Boolean force, String proxy, String proxyuser, String proxypassword) {
 
 		// assemble the unregister command
 		String command = this.command;		command += " identity";
 		if (username!=null)					command += " --username="+username;
 		if (password!=null)					command += " --password="+password;
 		if (regenerate!=null && regenerate)	command += " --regenerate";
 		if (force!=null && force)			command += " --force";
 		if (proxy!=null)					command += " --proxy="+proxy;
 		if (proxyuser!=null)				command += " --proxyuser="+proxyuser;
 		if (proxypassword!=null)			command += " --proxypassword="+proxypassword;
 		
 		// run command without asserting results
 		return sshCommandRunner.runCommandAndWait(command);
 	}
 	
 	/**
 	 * "subscription-manager-cli identity"
 	 * @param username
 	 * @param password
 	 * @param regenerate
 	 * @param force
 	 * @param proxy
 	 * @param proxyuser
 	 * @param proxypassword
 	 * @return
 	 */
 	public SSHCommandResult identity(String username, String password, Boolean regenerate, Boolean force, String proxy, String proxyuser, String proxypassword) {
 		
 		SSHCommandResult sshCommandResult = identity_(username, password, regenerate, force, proxy, proxyuser, proxypassword);
 		
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
 		this.currentAuthenticator = null;
 		this.currentAuthenticatorPassword = null;
 		
 		// assert that all of the entitlement certs have been removed (Actually, the entitlementCertDir should get removed)
 		Assert.assertTrue(getCurrentEntitlementCertFiles().size()==0, "All of the entitlement certificates have been removed after unregister.");
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
 			//Assert.assertContainsMatch(sshCommandResult.getStdout(), "Installed Product Status"); // produces too much logging
 			String title = "Installed Product Status";
 			Assert.assertTrue(sshCommandResult.getStdout().contains(title),"The list of installed products is entitled '"+title+"'.");
 		}
 
 		return sshCommandResult;
 	}
 	
 	/**
 	 * @return SSHCommandResult from "subscription-manager-cli list --available"
 	 */
 	public SSHCommandResult listAvailableSubscriptionPools() {
 
 		SSHCommandResult sshCommandResult = list_(null,Boolean.TRUE,null, null, null, null, null);
 		
 		Assert.assertEquals(sshCommandResult.getExitCode(), Integer.valueOf(0), "The exit code from the list --available command indicates a success.");
 		//Assert.assertContainsMatch(sshCommandResult.getStdout(), "Available Subscriptions"); // produces too much logging
 
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
 		//Assert.assertContainsMatch(sshCommandResult.getStdout(), "Available Subscriptions"); // produces too much logging
 
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
 		} else {
 			//Assert.assertContainsMatch(sshCommandResult.getStdout(), "Consumed Product Subscriptions"); // produces too much logging
 			String title = "Consumed Product Subscriptions";
 			Assert.assertTrue(sshCommandResult.getStdout().contains(title),"The list of consumed products is entitled '"+title+"'.");
 		}
 
 		return sshCommandResult;
 	}
 	
 	
 	
 	// subscribe module tasks ************************************************************
 
 	/**
 	 * subscribe without asserting results
 	 * @param auto TODO
 	 * @param poolId TODO
 	 * @param productId TODO
 	 * @param regtoken TODO
 	 * @param email TODO
 	 * @param locale TODO
 	 * @param proxy TODO
 	 * @param proxyuser TODO
 	 * @param proxypassword TODO
 	 */
 	public SSHCommandResult subscribe_(Boolean auto, String poolId, String productId, String regtoken, String email, String locale, String proxy, String proxyuser, String proxypassword) {
 		
 		// assemble the subscribe command
 		String command = this.command;	command += " subscribe";
 		if (auto!=null && auto)			command += " --auto";
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
 	
 	public SSHCommandResult subscribe(Boolean auto, String poolId, String productId, String regtoken, String email, String locale, String proxy, String proxyuser, String proxypassword) {
 
 		SSHCommandResult sshCommandResult = subscribe_(auto, poolId, productId, regtoken, email, locale, proxy, proxyuser, proxypassword);
 		
 		// assert results...
 		
 		// if already subscribed, just return the result
 		// This consumer is already subscribed to the product matching pool with id 'ff8080812c71f5ce012c71f6996f0132'
 		if (sshCommandResult.getStdout().startsWith("This consumer is already subscribed")) return sshCommandResult;	
 
 		// if no free entitlements, just return the result
 		// No free entitlements are available for the pool with id 'ff8080812e16e00e012e16e1f6090134'
 		if (sshCommandResult.getStdout().startsWith("No free entitlements are available")) return sshCommandResult;	
 		
 		// if rule failed, just return the result
 		// Unable to entitle consumer to the pool with id '8a90f8b42e3e7f2e012e3e7fc653013e': rulefailed.virt.only
 		if (sshCommandResult.getStdout().startsWith("Unable to entitle consumer")) return sshCommandResult;	
 		
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
 	
 	public File subscribeToProductId(String productId) {
 		//RemoteFileTasks.runCommandExpectingNonzeroExit(sshCommandRunner,"subscription-manager-cli subscribe --product="+product);
 		
 		SubscriptionPool pool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("productId", productId, getCurrentlyAvailableSubscriptionPools());
 		Assert.assertNotNull(pool,"Found an available pool to subscribe to productId '"+productId+"': "+pool);
 		return subscribeToSubscriptionPool(pool);
 	}
 	
 	/**
 	 * subscribe to the given SubscriptionPool 
 	 * @param pool
 	 * @return the newly installed EntitlementCert file to the newly consumed ProductSubscriptions 
 	 */
 	public File subscribeToSubscriptionPool(SubscriptionPool pool)  {
 		List<ProductSubscription> beforeProductSubscriptions = getCurrentlyConsumedProductSubscriptions();
 		List<File> beforeEntitlementCertFiles = getCurrentEntitlementCertFiles();
 		log.info("Subscribing to subscription pool: "+pool);
 		SSHCommandResult sshCommandResult = subscribe(null, pool.poolId, null, null, null, null, null, null, null);
 
 		// assert that the remaining SubscriptionPools does NOT contain the pool just subscribed to
 		List<SubscriptionPool> afterSubscriptionPools = getCurrentlyAvailableSubscriptionPools();
 		Assert.assertTrue(!afterSubscriptionPools.contains(pool),
 				"The available subscription pools no longer contains the just subscribed to pool: "+pool);
 		
 		// assert that the remaining SubscriptionPools do NOT contain the same productId just subscribed to
 		log.warning("Due to subscription-manager design change, we will no longer assert that the remaining available pools do not contain the same productId ("+pool.productId+") as the pool that was just subscribed.  Reference: https://bugzilla.redhat.com/show_bug.cgi?id=663455");
 		/*
 		for (SubscriptionPool afterSubscriptionPool : afterSubscriptionPools) {
 			Assert.assertTrue(!afterSubscriptionPool.productId.equals(pool.productId),
 					"This remaining available pool "+afterSubscriptionPool+" does NOT contain the same productId ("+pool.productId+") after subscribing to pool: "+pool);
 		}
 		*/
 
 		// is this a personal subpool?
 		String poolProductId = pool.productId;
 		boolean isSubpool = false; 
 		try {
 			JSONArray personSubscriptionPoolProductData;
 //			personSubscriptionPoolProductData = new JSONArray(System.getProperty("sm.person.subscriptionPoolProductData", "<>").replaceAll("<", "[").replaceAll(">", "]")); // hudson parameters use <> instead of []
 			personSubscriptionPoolProductData = new JSONArray(SubscriptionManagerBaseTestScript.getProperty("sm.person.subscriptionPoolProductData", "[]").replaceFirst("^\"", "").replaceFirst("\"$", "").replaceAll("<", "[").replaceAll(">", "]")); // hudson JSONArray parameters get surrounded with double quotes that need to be stripped
 			for (int j=0; j<personSubscriptionPoolProductData.length(); j++) {
 				JSONObject poolProductDataAsJSONObject = (JSONObject) personSubscriptionPoolProductData.get(j);
 				String personProductId = poolProductDataAsJSONObject.getString("personProductId");
 				JSONObject subpoolProductDataAsJSONObject = poolProductDataAsJSONObject.getJSONObject("subPoolProductData");
 				String systemProductId = subpoolProductDataAsJSONObject.getString("systemProductId");
 				if (poolProductId.equals(systemProductId)) { // special case when pool's productId is really a personal subpool
 					poolProductId = personProductId;
 					isSubpool = true;
 					break;
 				}
 			}
 		} catch (JSONException e) {
 			e.printStackTrace();
 			Assert.fail(e.getMessage());
 		} 
 		
 		// figure out which entitlement cert file has been newly installed into /etc/pki/entitlement after attempting to subscribe to pool
 		/* OLD WAY - THIS ALGORITHM BREAKS DOWN WHEN MODIFIER ENTITLEMENTS ARE IN PLAY
 		File newCertFile = null;
 		List<File> afterEntitlementCertFiles = getCurrentEntitlementCertFiles();
 		for (File file : afterEntitlementCertFiles) {
 			if (!beforeEntitlementCertFiles.contains(file)) {
 				newCertFile = file; break;
 			}
 		}
 		*/
 		/* VALID BUT INEFFICIENT
 		List<File> afterEntitlementCertFiles = getCurrentEntitlementCertFiles();
 		File newCertFile = null;
 		Map<BigInteger, SubscriptionPool> map = new HashMap<BigInteger, SubscriptionPool>();
 		try {
 			map = getCurrentSerialMapToSubscriptionPools(this.currentAuthenticator,this.currentAuthenticatorPassword);
 		} catch (Exception e) {
 			e.printStackTrace();
 			Assert.fail(e.getMessage());
 		}
 		for (BigInteger serial: map.keySet()) {
 			if (map.get(serial).poolId.equals(pool.poolId)) {
 				newCertFile = new File(this.entitlementCertDir+"/"+serial+".pem");
 				break;
 			}
 		}
 		*/
 		File newCertFile = null;
 		List<File> afterEntitlementCertFiles = getCurrentEntitlementCertFiles();
 		String hostname = getConfFileParameter(rhsmConfFile, "hostname");
 		String port = getConfFileParameter(rhsmConfFile, "port");
 		String prefix = getConfFileParameter(rhsmConfFile, "prefix");
 		for (File entitlementCertFile : afterEntitlementCertFiles) {
 			if (!beforeEntitlementCertFiles.contains(entitlementCertFile)) {
 				EntitlementCert entitlementCert = getEntitlementCertFromEntitlementCertFile(entitlementCertFile);
 				try {
 					JSONObject jsonEntitlement = CandlepinTasks.getEntitlementUsingRESTfulAPI(hostname,port,prefix,this.currentAuthenticator,this.currentAuthenticatorPassword,entitlementCert.id);
 					JSONObject jsonPool = new JSONObject(CandlepinTasks.getResourceUsingRESTfulAPI(hostname,port,prefix,this.currentAuthenticator,this.currentAuthenticatorPassword,jsonEntitlement.getJSONObject("pool").getString("href")));
 					if (jsonPool.getString("id").equals(pool.poolId)) {
 						newCertFile = entitlementCertFile; break;
 					}
 				} catch (Exception e) {
 					e.printStackTrace();
 					Assert.fail(e.getMessage());
 				}
 			}
 		}
 		
 		// when the pool is already subscribed to...
 		if (sshCommandResult.getStdout().startsWith("This consumer is already subscribed")) {
 			
 			// assert that NO new entitlement cert file has been installed in /etc/pki/entitlement
 			/*Assert.assertNull(newCertFile,
 					"A new entitlement certificate has NOT been installed after attempting to subscribe to an already subscribed to pool: "+pool);
 			*/
 			Assert.assertEquals(beforeEntitlementCertFiles.size(), afterEntitlementCertFiles.size(),
 					"The existing entitlement certificate count remains unchanged after attempting to subscribe to an already subscribed to pool: "+pool);
 
 			// find the existing entitlement cert file corresponding to the already subscribed pool
 			/* ALREADY FOUND USING ALGORITHM ABOVE 
 			EntitlementCert entitlementCert = null;
 			for (File thisEntitlementCertFile : getCurrentEntitlementCertFiles()) {
 				EntitlementCert thisEntitlementCert = getEntitlementCertFromEntitlementCertFile(thisEntitlementCertFile);
 				if (thisEntitlementCert.orderNamespace.productId.equals(poolProductId)) {
 					entitlementCert = thisEntitlementCert;
 					break;
 				}
 			}
 			Assert.assertNotNull(entitlementCert, isSubpool?
 					"Found an already existing Entitlement Cert whose personal productId matches the system productId from the subscription pool: "+pool:
 					"Found an already existing Entitlement Cert whose productId matches the productId from the subscription pool: "+pool);
 			newCertFile = getEntitlementCertFileFromEntitlementCert(entitlementCert); // not really new, just already existing
 			*/
 			
 			// assert that consumed ProductSubscriptions has NOT changed
 			List<ProductSubscription> afterProductSubscriptions = getCurrentlyConsumedProductSubscriptions();
 			Assert.assertTrue(afterProductSubscriptions.size() == beforeProductSubscriptions.size() && afterProductSubscriptions.size() > 0,
 					"The list of currently consumed product subscriptions has not changed (from "+beforeProductSubscriptions.size()+" to "+afterProductSubscriptions.size()+") since the productId of the pool we are trying to subscribe to is already consumed.");
 
 		// when no free entitlements exist...
 		} else if (sshCommandResult.getStdout().startsWith("No free entitlements are available")) {
 			
 			// assert that the depleted pool Quantity is zero
 			SubscriptionPool depletedPool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("poolId", pool.poolId, getCurrentlyAllAvailableSubscriptionPools());
 			/* behavior changed on list --all --available  (3/4/2011)
 			Assert.assertNotNull(depletedPool,
 					"Found the depleted pool amongst --all --available after having consumed all of its available entitlements: ");
 			*/
 			Assert.assertNull(depletedPool,
 					"Should no longer find the depleted pool amongst --all --available after having consumed all of its available entitlements: ");
 //			Assert.assertEquals(depletedPool.quantity, "0",
 //					"Asserting the pool's quantity after having consumed all of its available entitlements is zero.");
 			JSONObject jsonPool = null;
 			int consumed = 0;
 			int quantity = Integer.valueOf(pool.quantity);
 			try {
 				jsonPool = new JSONObject(CandlepinTasks.getResourceUsingRESTfulAPI(hostname,port,prefix,this.currentAuthenticator,this.currentAuthenticatorPassword,"/pools/"+pool.poolId));
 				consumed = jsonPool.getInt("consumed");
 				quantity = jsonPool.getInt("quantity");
 			} catch (Exception e) {
 				e.printStackTrace();
 				Assert.fail(e.getMessage());
 			} 
 			Assert.assertEquals(consumed, quantity,
 					"Asserting the pool's consumed attribute equals it's total quantity after having consumed all of its available entitlements.");
 
 			//  assert that NO new entitlement cert file has been installed in /etc/pki/entitlement
 			Assert.assertNull(newCertFile,
 					"A new entitlement certificate has NOT been installed after attempting to subscribe to depleted pool: "+depletedPool);
 			Assert.assertEquals(beforeEntitlementCertFiles.size(), afterEntitlementCertFiles.size(),
 					"The existing entitlement certificate count remains unchanged after attempting to subscribe to depleted pool: "+depletedPool);
 
 			
 		// otherwise, the pool is NOT already subscribe to...
 		} else {
 	
 			// assert that only ONE new entitlement cert file has been installed in /etc/pki/entitlement
 			// https://bugzilla.redhat.com/show_bug.cgi?id=640338
 			Assert.assertTrue(afterEntitlementCertFiles.size()==beforeEntitlementCertFiles.size()+1,
 					"Only ONE new entitlement certificate has been installed (count was '"+beforeEntitlementCertFiles.size()+"'; is now '"+afterEntitlementCertFiles.size()+"') after subscribing to pool: "+pool);
 
 			// assert that the other cert files remain unchanged
 			/* CANNOT MAKE THIS ASSERT/ASSUMPTION ANYMORE BECAUSE ADDITION OF AN ENTITLEMENT CAN AFFECT A MODIFIER PRODUCT THAT PROVIDES EXTRA CONTENT FOR THIS PRODUCT (A MODIFIER PRODUCT IS ALSO CALLED EUS) 2/21/2011 jsefler
 			if (!afterEntitlementCertFiles.remove(newCertFile)) Assert.fail("Failed to remove certFile '"+newCertFile+"' from list.  This could be an automation logic error.");
 			Assert.assertEquals(afterEntitlementCertFiles,beforeEntitlementCertFiles,"After subscribing to pool id '"+pool+"', the other entitlement cert serials remain unchanged");
 			*/
 			
 			// assert the new entitlement cert file has been installed in /etc/pki/entitlement
 			Assert.assertNotNull(newCertFile, "A new entitlement certificate has been installed after subscribing to pool: "+pool);
 			log.info("The new entitlement certificate file is: "+newCertFile);
 			
 			// assert that the productId from the pool matches the entitlement productId
 			// TEMPORARY WORKAROUND FOR BUG: https://bugzilla.redhat.com/show_bug.cgi?id=650278 - jsefler 11/5/2010
 			boolean invokeWorkaroundWhileBugIsOpen = true;
 			try {String bugId="650278"; if (invokeWorkaroundWhileBugIsOpen&&BzChecker.getInstance().isBugOpen(bugId)) {log.fine("Invoking workaround for "+BzChecker.getInstance().getBugState(bugId).toString()+" Bugzilla bug "+bugId+".  (https://bugzilla.redhat.com/show_bug.cgi?id="+bugId+")");} else {invokeWorkaroundWhileBugIsOpen=false;}} catch (XmlRpcException xre) {/* ignore exception */} catch (RuntimeException re) {/* ignore exception */}
 			if (invokeWorkaroundWhileBugIsOpen) {
 				log.warning("skipping assert that the productId from the pool matches the entitlement productId");
 			} else {
 			// END OF WORKAROUND
 			EntitlementCert entitlementCert = getEntitlementCertFromEntitlementCertFile(newCertFile);
 			Assert.assertEquals(entitlementCert.orderNamespace.productId, poolProductId, isSubpool?
 					"New EntitlementCert productId '"+entitlementCert.orderNamespace.productId+"' matches originating Personal SubscriptionPool productId '"+poolProductId+"' after subscribing to the subpool.":
 					"New EntitlementCert productId '"+entitlementCert.orderNamespace.productId+"' matches originating SubscriptionPool productId '"+poolProductId+"' after subscribing to the pool.");
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
 	
 	public File subscribeToSubscriptionPoolUsingPoolId(SubscriptionPool pool/*, boolean withPoolID*/) {
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
 		}
 		// END OF WORKAROUND
 		
 		// TEMPORARY WORKAROUND FOR BUG: https://bugzilla.redhat.com/show_bug.cgi?id=622839 - jsefler 8/10/2010
 		invokeWorkaroundWhileBugIsOpen = true;
 		try {String bugId="622839"; if (invokeWorkaroundWhileBugIsOpen&&BzChecker.getInstance().isBugOpen(bugId)) {log.fine("Invoking workaround for "+BzChecker.getInstance().getBugState(bugId).toString()+" Bugzilla bug "+bugId+".  (https://bugzilla.redhat.com/show_bug.cgi?id="+bugId+")");} else {invokeWorkaroundWhileBugIsOpen=false;}} catch (XmlRpcException xre) {/* ignore exception */} catch (RuntimeException re) {/* ignore exception */}
 		if (invokeWorkaroundWhileBugIsOpen) {
 			Assert.assertContainsMatch(listAvailableSubscriptionPools().getStdout(),"^No Available subscription pools to list$",assertMsg);
 			return;
 		}
 		// END OF WORKAROUND
 		
 		// TEMPORARY WORKAROUND FOR BUG: https://bugzilla.redhat.com/show_bug.cgi?id=623657 - jsefler 8/12/2010
 		invokeWorkaroundWhileBugIsOpen = true;
 		try {String bugId="623657"; if (invokeWorkaroundWhileBugIsOpen&&BzChecker.getInstance().isBugOpen(bugId)) {log.fine("Invoking workaround for "+BzChecker.getInstance().getBugState(bugId).toString()+" Bugzilla bug "+bugId+".  (https://bugzilla.redhat.com/show_bug.cgi?id="+bugId+")");} else {invokeWorkaroundWhileBugIsOpen=false;}} catch (XmlRpcException xre) {/* ignore exception */} catch (RuntimeException re) {/* ignore exception */}
 		if (invokeWorkaroundWhileBugIsOpen) {
 			Assert.assertContainsMatch(listAvailableSubscriptionPools().getStdout(),"^No Available subscription pools to list$",assertMsg);
 			return;
 		}
 		// END OF WORKAROUND
 		
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
 	 * unsubscribe from entitlement certificate serial and assert results
 	 * @param serialNumber
 	 * @return - false when no unsubscribe took place
 	 */
 	public boolean unsubscribeFromSerialNumber(BigInteger serialNumber) {
 		String certFilePath = entitlementCertDir+"/"+serialNumber+".pem";
 		File certFile = new File(certFilePath);
 		boolean certFileExists = RemoteFileTasks.testFileExists(sshCommandRunner,certFilePath)==1? true:false;
 		List<File> beforeEntitlementCertFiles = getCurrentEntitlementCertFiles();
 
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
 			Assert.assertEquals(result.getExitCode(), Integer.valueOf(255), "The unsubscribe should fail when its corresponding entitlement cert file ("+certFilePath+") does not exist.");
 			return false;
 		}
 		
 		// assert the certFileExists is removed
 		Assert.assertTrue(RemoteFileTasks.testFileExists(sshCommandRunner,certFilePath)==0,
 				"Entitlement Certificate with serial '"+serialNumber+"' ("+certFilePath+") has been removed.");
 
 		// assert that only ONE entitlement cert file was removed
 		List<File> afterEntitlementCertFiles = getCurrentEntitlementCertFiles();
 		Assert.assertTrue(afterEntitlementCertFiles.size()==beforeEntitlementCertFiles.size()-1,
 				"Only ONE entitlement certificate has been removed (count was '"+beforeEntitlementCertFiles.size()+"'; is now '"+afterEntitlementCertFiles.size()+"') after unsubscribing from serial: "+serialNumber);
 
 		// assert that the other cert files remain unchanged
 		/* CANNOT MAKE THIS ASSERT/ASSUMPTION ANYMORE BECAUSE REMOVAL OF AN ENTITLEMENT CAN AFFECT A MODIFIER PRODUCT THAT PROVIDES EXTRA CONTENT FOR THIS SERIAL (A MODIFIER PRODUCT IS ALSO CALLED EUS) 2/21/2011 jsefler
 		if (!beforeEntitlementCertFiles.remove(certFile)) Assert.fail("Failed to remove certFile '"+certFile+"' from list.  This could be an automation logic error.");
 		Assert.assertEquals(afterEntitlementCertFiles,beforeEntitlementCertFiles,"After unsubscribing from serial '"+serialNumber+"', the other entitlement cert serials remain unchanged");
 		*/
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
 	 * Issues a call to "subscription-manager unsubscribe --all" which will unsubscribe from
 	 * all currently consumed product subscriptions and then asserts the list --consumed is empty.
 	 */
 	public void unsubscribeFromAllOfTheCurrentlyConsumedProductSubscriptions() {
 
 		unsubscribe(Boolean.TRUE, null, null, null, null);
 
 		// assert that there are no product subscriptions consumed
 		Assert.assertEquals(listConsumedProductSubscriptions().getStdout().trim(),
 				"No Consumed subscription pools to list","Successfully unsubscribed from all consumed products.");
 		
 		// assert that there are no entitlement cert files
 		Assert.assertTrue(sshCommandRunner.runCommandAndWait("find "+entitlementCertDir+" -name *.pem | grep -v key.pem").getStdout().equals(""),
 				"No entitlement cert files exist after unsubscribing from all subscription pools.");
 
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
 		
 		SSHCommandResult sshCommandResult = facts_(list, update, proxy, proxyuser, proxypassword);
 
 		// assert results for a successful facts
 		Assert.assertEquals(sshCommandResult.getExitCode(), Integer.valueOf(0), "The exit code from the facts command indicates a success.");
 		String regex = "";
 		if (list!=null && list)		regex=".*:.*";								// list
 		if (update!=null && update)	regex=getCurrentConsumerCert().consumerid;	// consumerid
 
 		Assert.assertContainsMatch(sshCommandResult.getStdout().trim(), regex);
 		
 		return sshCommandResult; // from the facts command
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 //	public boolean areAllRequiredTagsInContentNamespaceProvidedByProductCerts(ContentNamespace contentNamespace, List<ProductCert> productCerts) {
 //
 //		// get all of the provided tags from the productCerts
 //		List<String> providedTags = new ArrayList<String>();
 //		for (ProductCert productCert : productCerts) {
 //			for (ProductNamespace productNamespace : productCert.productNamespaces) {
 //				if (productNamespace.providedTags!=null) {
 //					for (String providedTag : productNamespace.providedTags.split("\\s*,\\s*")) {
 //						providedTags.add(providedTag);
 //					}
 //				}
 //			}
 //		}
 //		
 //		// get all of the required tags from the contentNamespace
 //		List<String> requiredTags = new ArrayList<String>();
 //		if (contentNamespace.requiredTags!=null) {
 //			for (String requiredTag : contentNamespace.requiredTags.split("\\s*,\\s*")) {
 //				requiredTags.add(requiredTag);
 //			}
 //		}
 //		
 //		// are ALL of the requiredTags provided?  Note: true is returned (and should be) when requiredTags.isEmpty()
 //		return providedTags.containsAll(requiredTags);
 //	}
 	public boolean areAllRequiredTagsInContentNamespaceProvidedByProductCerts(ContentNamespace contentNamespace, List<ProductCert> productCerts) {
 		return areAllRequiredTagsProvidedByProductCerts(contentNamespace.requiredTags, productCerts);
 	}
 	
 	public boolean areAllRequiredTagsProvidedByProductCerts(String requiredTagsAsString, List<ProductCert> productCerts) {
 
 		// get all of the provided tags from the productCerts
 		List<String> providedTags = new ArrayList<String>();
 		for (ProductCert productCert : productCerts) {
 			for (ProductNamespace productNamespace : productCert.productNamespaces) {
 				if (productNamespace.providedTags!=null) {
 					for (String providedTag : productNamespace.providedTags.split("\\s*,\\s*")) {
 						providedTags.add(providedTag);
 					}
 				}
 			}
 		}
 		
 		// get all of the required tags from the contentNamespace
 		List<String> requiredTags = new ArrayList<String>();
 		if (requiredTagsAsString!=null) {
 			for (String requiredTag : requiredTagsAsString.split("\\s*,\\s*")) {
 				requiredTags.add(requiredTag);
 			}
 		}
 		
 		// are ALL of the requiredTags provided?  Note: true is returned (and should be) when requiredTags.isEmpty()
 		return providedTags.containsAll(requiredTags);
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
 		List<ProductCert> currentProductCerts = this.getCurrentProductCerts();
 		
 		// assert all of the entitlement certs are reported in the stdout from "yum repolist all"
 		SSHCommandResult result = sshCommandRunner.runCommandAndWait("yum repolist all --disableplugin=rhnplugin");	// FIXME, THIS SHOULD MAKE USE OF getYumRepolist
  		for (EntitlementCert entitlementCert : entitlementCerts) {
  			for (ContentNamespace contentNamespace : entitlementCert.contentNamespaces) {
 
  				// Note: When the repo id and repo name are really long, the repo name in the yum repolist all gets crushed (hence the reason for .* in the regex)
 				String regex = String.format("^%s\\s+(?:%s|.*)\\s+%s", contentNamespace.label.trim(), contentNamespace.name.substring(0,Math.min(contentNamespace.name.length(), 25)), contentNamespace.enabled.equals("1")? "enabled:":"disabled$");	// 25 was arbitraily picked to be short enough to be displayed by yum repolist all
 //				if (areReported)	// before development of conditional content tagging
 				if (areReported && areAllRequiredTagsInContentNamespaceProvidedByProductCerts(contentNamespace,currentProductCerts))
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
 	 * @param options [all|enabled|disabled] [--option=...]
 	 * @return array of repo labels returned from a call to yum repolist [options]
 	 */
 	public ArrayList<String> getYumRepolist(String options){
 		ArrayList<String> repos = new ArrayList<String>();
 		sshCommandRunner.runCommandAndWait("killall -9 yum");
 		sshCommandRunner.runCommandAndWait("yum repolist "+options+" --disableplugin=rhnplugin"); // --disableplugin=rhnplugin helps avoid: up2date_client.up2dateErrors.AbuseError
 		// WARNING: DO NOT MAKE ANYMORE CALLS TO sshCommandRunner.runCommand* IN THE REST OF THIS METHOD.
 		// getYumRepolistPackageCount() ASSUMES sshCommandRunner.getStdout() CAME FROM THE CALL TO yum repolist
 
 		// Example sshCommandRunner.getStdout()
 		//	[root@jsefler-itclient01 product]# yum repolist all
 		//	Loaded plugins: pidplugin, refresh-packagekit, rhnplugin, rhsmplugin
 		//	Updating Red Hat repositories.
 		//	INFO:repolib:repos updated: 0
 		//	This system is not registered with RHN.
 		//	RHN support will be disabled.
 		//	red-hat-enterprise-linux-6-entitlement-alpha-rpms                                                                         | 4.0 kB     00:00     
 		//	red-hat-enterprise-linux-6-entitlement-alpha-rpms-updates                                                                 |  951 B     00:00     
 		//	repo id                                                                        repo name                                           status
 		//	red-hat-enterprise-linux-6-entitlement-alpha-debug-rpms                        Red Hat Enterprise Linux 6 Entitlement Alpha (Debug disabled
 		//	red-hat-enterprise-linux-6-entitlement-alpha-debug-rpms-updates                Red Hat Enterprise Linux 6 Entitlement Alpha (Debug disabled
 		//	red-hat-enterprise-linux-6-entitlement-alpha-optional-debug-rpms               Red Hat Enterprise Linux 6 Entitlement Alpha - Opti disabled
 		//	red-hat-enterprise-linux-6-entitlement-alpha-optional-debug-rpms-updates       Red Hat Enterprise Linux 6 Entitlement Alpha - Opti disabled
 		//	repolist: 3,394
 
 		
 		String[] availRepos = sshCommandRunner.getStdout().split("\\n");
 		
 		int repolistStartLn = 0;
 		int repolistEndLn = 0;
 		
 		for(int i=0;i<availRepos.length;i++)
 			if (availRepos[i].startsWith("repo id"))
 				repolistStartLn = i + 1;
 			else if (availRepos[i].startsWith("repolist:"))
 				repolistEndLn = i;
 		
 		for(int i=repolistStartLn;i<repolistEndLn;i++)
 			repos.add(availRepos[i].split(" ")[0]);
 		
 		return repos;
 	}
 	
 	/**
 	 * @param options [all|enabled|disabled] [--option=...]
 	 * @return the value reported at the bottom of a call to yum repolist [options] (repolist: value)
 	 */
 	public Integer getYumRepolistPackageCount(String options){
 		getYumRepolist(options);
 
 		// Example sshCommandRunner.getStdout()
 		//	[root@jsefler-itclient01 product]# yum repolist all
 		//	Loaded plugins: pidplugin, refresh-packagekit, rhnplugin, rhsmplugin
 		//	Updating Red Hat repositories.
 		//	INFO:repolib:repos updated: 0
 		//	This system is not registered with RHN.
 		//	RHN support will be disabled.
 		//	red-hat-enterprise-linux-6-entitlement-alpha-rpms                                                                         | 4.0 kB     00:00     
 		//	red-hat-enterprise-linux-6-entitlement-alpha-rpms-updates                                                                 |  951 B     00:00     
 		//	repo id                                                                        repo name                                           status
 		//	red-hat-enterprise-linux-6-entitlement-alpha-debug-rpms                        Red Hat Enterprise Linux 6 Entitlement Alpha (Debug disabled
 		//	red-hat-enterprise-linux-6-entitlement-alpha-debug-rpms-updates                Red Hat Enterprise Linux 6 Entitlement Alpha (Debug disabled
 		//	red-hat-enterprise-linux-6-entitlement-alpha-optional-debug-rpms               Red Hat Enterprise Linux 6 Entitlement Alpha - Opti disabled
 		//	red-hat-enterprise-linux-6-entitlement-alpha-optional-debug-rpms-updates       Red Hat Enterprise Linux 6 Entitlement Alpha - Opti disabled
 		//	repolist: 3,394
 		
 		// Example sshCommandRunner.getStderr()
 		//	INFO:rhsm-app.repolib:repos updated: 63
 		//	https://cdn.redhat.com/FOO/content/beta/rhel/server/6/6Server/x86_64/os/repodata/repomd.xml: [Errno 14] PYCURL ERROR 22 - "The requested URL returned error: 403"
 		//	https://cdn.redhat.com/content/beta/rhel/client/6/x86_64/supplementary/source/SRPMS/repodata/repomd.xml: [Errno 14] PYCURL ERROR 22 - "The requested URL returned error: 404"
 
 		Assert.assertTrue(!sshCommandRunner.getStderr().contains("The requested URL returned error:"),"The requested URL did NOT return an error.");
 		
 		// parse out the value from repolist: value
 		String regex="repolist:(.*)";
 		
 		Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
 		Matcher matcher = pattern.matcher(sshCommandRunner.getStdout());
 		//Assert.assertTrue(matcher.find(),"Found fact "+factName);
 		if (!matcher.find()) {
 			log.warning("Did not find repolist package count.");
 			return null;
 		}
 		
 		Integer packageCount = Integer.valueOf(matcher.group(1).replaceAll(",","").trim());
 
 		return packageCount;
 	}
 	
 	@Deprecated
 	public ArrayList<String> getYumListOfAvailablePackagesFromRepo (String repoLabel) {
 		ArrayList<String> packages = new ArrayList<String>();
 		sshCommandRunner.runCommandAndWait("killall -9 yum");
 
 		int min = 5;
 		log.fine("Using a timeout of "+min+" minutes for next command...");
 		//SSHCommandResult result = sshCommandRunner.runCommandAndWait("yum list available",Long.valueOf(min*60000));
 		SSHCommandResult result = sshCommandRunner.runCommandAndWait("yum list available --disablerepo=* --enablerepo="+repoLabel+" --disableplugin=rhnplugin",Long.valueOf(min*60000));  // --disableplugin=rhnplugin helps avoid: up2date_client.up2dateErrors.AbuseError
 
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
 	
 //	public ArrayList<String> yumListAvailable (String disableplugin, String disablerepo, String enablerepo, String globExpression) {
 	/**
 	 * @param options
 	 * @return array of packages returned from a call to yum list available [options]
 	 */
 	public ArrayList<String> getYumListAvailable (String options) {
 		ArrayList<String> packages = new ArrayList<String>();
 		sshCommandRunner.runCommandAndWait("killall -9 yum");
 
 //		String							command  = "yum list available";
 //		if (disableplugin!=null)		command += " --disableplugin="+disableplugin;
 //		if (disablerepo!=null)			command += " --disablerepo="+disablerepo;
 //		if (enablerepo!=null)			command += " --enablerepo="+enablerepo;
 //		if (globExpression!=null)		command += " "+globExpression;
 		String							command  = "yum list available "+options+" --disableplugin=rhnplugin"; // --disableplugin=rhnplugin helps avoid: up2date_client.up2dateErrors.AbuseError
 		
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
 		//	xmltex.noarch                             20020625-16.el6                      red-hat-enterprise-linux-6-entitlement-alpha-rpms
 		//	xmlto.x86_64                              0.0.23-3.el6                         red-hat-enterprise-linux-6-entitlement-alpha-rpms
 		//	xmlto-tex.noarch                          0.0.23-3.el6                         red-hat-enterprise-linux-6-entitlement-alpha-rpms
 		//	xorg-x11-apps.x86_64                      7.4-10.el6                           red-hat-enterprise-linux-6-entitlement-alpha-rpms
 		//if (enablerepo==null||enablerepo.equals("*")) enablerepo="(\\S+)";
 		//String regex="^(\\S+) +(\\S+) +"+enablerepo+"$";
 		String regex="^(\\S+) +(\\S+) +(\\S+)$";
 		Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
 		Matcher matcher = pattern.matcher(result.getStdout());
 		if (!matcher.find()) {
 			log.info("Did NOT find any available packages from: "+command);
 			return packages;
 		}
 
 		// assemble the list of packages and return them
 		do {
 			packages.add(matcher.group(1)); // group(1) is the pkg,  group(2) is the version,  group(3) is the repo
 		} while (matcher.find());
 		return packages;		
 	}
 	
 	public ArrayList<String> yumGroupList (String Installed_or_Available, String options) {
 		ArrayList<String> groups = new ArrayList<String>();
 		sshCommandRunner.runCommandAndWait("killall -9 yum");
 
 		String command = "yum grouplist "+options+" --disableplugin=rhnplugin"; // --disableplugin=rhnplugin helps avoid: up2date_client.up2dateErrors.AbuseError
 		
 		// execute the yum command to list available packages
 		int min = 5;
 		log.fine("Using a timeout of "+min+" minutes for next command...");
 		SSHCommandResult result = sshCommandRunner.runCommandAndWait(command,Long.valueOf(min*60000));
 		
 		// Example result.getStdout()
 		//	[root@jsefler-betaqa-1 product]# yum grouplist --disablerepo=* --enablerepo=rhel-entitlement-beta
 		//	Loaded plugins: product-id, refresh-packagekit, rhnplugin, subscription-manager
 		//	Updating Red Hat repositories.
 		//	INFO:rhsm-app.repolib:repos updated: 0
 		//	This system is not registered with RHN.
 		//	RHN support will be disabled.
 		//	Setting up Group Process
 		//	rhel-entitlement-beta                                                                                                                                 | 4.0 kB     00:00     
 		//	rhel-entitlement-beta/group_gz                                                                                                                        | 190 kB     00:00     
 		//	Installed Groups:
 		//	   Additional Development
 		//	   Assamese Support
 		//	   Base
 		//	Available Groups:
 		//	   Afrikaans Support
 		//	   Albanian Support
 		//	   Amazigh Support
 		//	Done
 
 		String regex = Installed_or_Available+" Groups:((\\n\\s{3}.*)+)";
 		Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
 		Matcher matcher = pattern.matcher(result.getStdout());
 		if (!matcher.find()) {
 			log.info("Did NOT find any "+Installed_or_Available+" Groups from: "+command);
 			return groups;
 		}
 
 		// assemble the list of groups and return them
 		for (String group : matcher.group(1).trim().split("\\n\\s{3}")) groups.add(group);
 
 		return groups;		
 	}
 	
 	
 	public String findUniqueAvailablePackageFromRepo (String repo) {
 		
 		for (String pkg : getYumListAvailable("--disablerepo=* --enablerepo="+repo)) {
 			if (!getYumListAvailable("--disablerepo="+repo+" "+pkg).contains(pkg)) {
 				return pkg;
 			}
 		}
 		return null;
 	}
 	
 	public String findAnAvailableGroupFromRepo(String repo) {
 		List <String> groups = yumGroupList("Available", "--disablerepo=* --enablerepo="+repo);
 		for (int i=0; i<groups.size(); i++) {
 			String group = groups.get(i);
 
 			// choose a group that has "Mandatory Packages:"
 			String mandatoryPackages = "Mandatory Packages:";
 			if (sshCommandRunner.runCommandAndWait("yum groupinfo \""+groups.get(i)+"\" | grep \""+mandatoryPackages+"\"").getStdout().trim().equals(mandatoryPackages)) return group;
 		}
 		return null;
 	}
 
 	public String findAnInstalledGroupFromRepo(String repo) {
 		List <String> groups = yumGroupList("Installed", "--disablerepo=* --enablerepo="+repo);
 		for (int i=0; i<groups.size(); i++) {
 			String group = groups.get(i);
 			// don't consider these very important groups
 			if (group.equals("Base")) continue;
 			if (group.equals("X Window System")) continue;
 			if (group.startsWith("Network")) continue;	// Network Infrastructure Server, Network file system client, Networking Tools
 			
 			return group;
 		}
 		return null;
 	}
 	
 	public SSHCommandResult yumInstallPackageFromRepo (String pkg, String repoLabel) {
 		String command = "yum -y install "+pkg+" --enablerepo="+repoLabel+" --disableplugin=rhnplugin"; // --disableplugin=rhnplugin helps avoid: up2date_client.up2dateErrors.AbuseError
 		SSHCommandResult result = RemoteFileTasks.runCommandAndAssert(sshCommandRunner,command, 0, "^Complete!$",null);
 		RemoteFileTasks.runCommandAndAssert(sshCommandRunner,"yum list installed "+pkg+" --disableplugin=rhnplugin", 0, "^"+pkg+" .*@"+repoLabel+"$",null);
 		return result;
 	}
 	
 	public SSHCommandResult yumRemovePackage (String pkg) {
 		String command = "yum -y remove "+pkg+" --disableplugin=rhnplugin"; // --disableplugin=rhnplugin helps avoid: up2date_client.up2dateErrors.AbuseError
 		SSHCommandResult result = RemoteFileTasks.runCommandAndAssert(sshCommandRunner,command, 0, "^Complete!$",null);
 		RemoteFileTasks.runCommandAndAssert(sshCommandRunner,"yum list installed "+pkg+" --disableplugin=rhnplugin", 1, null,"Error: No matching Packages to list");
 		return result;
 	}
 	
 	public SSHCommandResult yumInstallGroup (String group) {
 		String command = "yum -y groupinstall \""+group+"\" --disableplugin=rhnplugin"; // --disableplugin=rhnplugin helps avoid: up2date_client.up2dateErrors.AbuseError
 		SSHCommandResult result = RemoteFileTasks.runCommandAndAssert(sshCommandRunner,command, 0, "^Complete!$",null);
 		Assert.assertFalse(this.yumGroupList("Available", ""/*"--disablerepo=* --enablerepo="+repo*/).contains(group),"Yum group is Available after calling '"+command+"'.");
 		return result;
 	}
 	
 	public SSHCommandResult yumRemoveGroup (String group) {
 		String command = "yum -y groupremove \""+group+"\" --disableplugin=rhnplugin"; // --disableplugin=rhnplugin helps avoid: up2date_client.up2dateErrors.AbuseError
 		SSHCommandResult result = RemoteFileTasks.runCommandAndAssert(sshCommandRunner,command, 0, "^Complete!$",null);
 		Assert.assertFalse(this.yumGroupList("Installed", ""/*"--disablerepo=* --enablerepo="+repo*/).contains(group),"Yum group is Installed after calling '"+command+"'.");
 		return result;
 	}
 	
 	public SSHCommandResult yumClean (String option) {
 		String command = "yum clean \""+option+"\" --disableplugin=rhnplugin"; // --disableplugin=rhnplugin helps avoid: up2date_client.up2dateErrors.AbuseError
 		SSHCommandResult result = RemoteFileTasks.runCommandAndAssert(sshCommandRunner,command, 0, "^Cleaning",null);
 		return result;
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
