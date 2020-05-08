 package com.redhat.qe.sm.cli.tasks;
 
 import java.io.File;
 import java.io.IOException;
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
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
 import com.redhat.qe.sm.data.Org;
 import com.redhat.qe.sm.data.ProductCert;
 import com.redhat.qe.sm.data.ProductNamespace;
 import com.redhat.qe.sm.data.ProductSubscription;
 import com.redhat.qe.sm.data.Repo;
 import com.redhat.qe.sm.data.SubscriptionPool;
 import com.redhat.qe.sm.data.YumRepo;
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
 	public final String rhsmUpdateFile		= "/var/run/rhsm/update";
 	public final String rhsmLogFile			= "/var/log/rhsm/rhsm.log";
 	public final String rhsmPluginConfFile	= "/etc/yum/pluginconf.d/subscription-manager.conf"; // "/etc/yum/pluginconf.d/rhsmplugin.conf"; renamed by dev on 11/24/2010
 	public final String rhsmFactsJsonFile	= "/var/lib/rhsm/facts/facts.json";
 	public final String rhnSystemIdFile		= "/etc/sysconfig/rhn/systemid";
 	public final String factsDir			= "/etc/rhsm/facts";
 	public final String factsOverrideFile	= factsDir+"/override.facts";
 	public final String brandingDir			= "/usr/share/rhsm/subscription_manager/branding";
 	public final String varLogMessagesFile	= "/var/log/messages";
 	public final String varLogAuditFile		= "/var/log/audit/audit.log";
 	public       String rhsmComplianceD		= null; // "/usr/libexec/rhsmd"; RHEL62 RHEL57		// /usr/libexec/rhsm-complianced; RHEL61
 
 	
 	// will be initialized by initializeFieldsFromConfigFile()
 	public String productCertDir				= null; // "/etc/pki/product";
 	public String entitlementCertDir			= null; // "/etc/pki/entitlement";
 	public String consumerCertDir				= null; // "/etc/pki/consumer";
 	public String caCertDir						= null; // "/etc/rhsm/ca";
 	public String baseurl						= null;
 	public String consumerKeyFile				= null; // consumerCertDir+"/key.pem";
 	public String consumerCertFile				= null; // consumerCertDir+"/cert.pem";
 
 	
 	public String hostname						= null;	// of the client
 	public String ipaddr						= null;	// of the client
 	public String arch							= null;	// of the client
 	public String sockets						= null;	// of the client
 	public String variant						= null;	// of the client
 	public String releasever					= null;	// of the client
 	
 	protected String currentlyRegisteredUsername	= null;	// most recent username used during register
 	protected String currentlyRegisteredPassword	= null;	// most recent password used during register
 	protected String currentlyRegisteredOrg			= null;	// most recent owner used during register
 	protected ConsumerType currentlyRegisteredType	= null;	// most recent consumer type used during register
 	
 	public String redhatRelease	= null;
 	
 	public SubscriptionManagerTasks(SSHCommandRunner runner) {
 		super();
 		sshCommandRunner = runner;
 		hostname		= sshCommandRunner.runCommandAndWait("hostname").getStdout().trim();
 		ipaddr			= sshCommandRunner.runCommandAndWait("ifconfig eth0 | grep 'inet addr:' | cut -d: -f2 | sed s/'  Bcast'//g").getStdout().trim();
 		arch			= sshCommandRunner.runCommandAndWait("uname --machine").getStdout().trim();  // uname -i --hardware-platform :print the hardware platform or "unknown"	// uname -m --machine :print the machine hardware name
 		releasever		= sshCommandRunner.runCommandAndWait("rpm -q --qf \"%{VERSION}\\n\" --whatprovides system-release").getStdout().trim();  // cut -f 5 -d : /etc/system-release-cpe	// rpm -q --qf "%{VERSION}\n" --whatprovides system-release
 		rhsmComplianceD	= sshCommandRunner.runCommandAndWait("rpm -ql subscription-manager | grep libexec/rhsm").getStdout().trim();
 		redhatRelease	= sshCommandRunner.runCommandAndWait("cat /etc/redhat-release").getStdout().trim();
 		if (redhatRelease.contains("Server")) variant = "Server";	//69.pem
 		if (redhatRelease.contains("Client")) variant = "Client";	//68.pem   (aka Desktop)
 		if (redhatRelease.contains("Workstation")) variant = "Workstation";	//71.pem
 		if (redhatRelease.contains("ComputeNode")) variant = "ComputeNode";	//76.pem
 		//if (redhatRelease.contains("IBM POWER")) variant = "IBM Power";	//74.pem	Red Hat Enterprise Linux for IBM POWER	// TODO  Not sure if these are correct or if they are just Server on a different arch
 		//if (redhatRelease.contains("IBM System z")) variant = "System Z";	//72.pem	Red Hat Enterprise Linux for IBM System z	// TODO
 		if (redhatRelease.contains("release 5")) sockets = sshCommandRunner.runCommandAndWait("for cpu in `ls -1 /sys/devices/system/cpu/ | egrep cpu[[:digit:]]`; do echo \"cpu `cat /sys/devices/system/cpu/$cpu/topology/physical_package_id`\"; done | grep cpu | uniq | wc -l").getStdout().trim();  // Reference: Bug 707292 - cpu socket detection fails on some 5.7 i386 boxes
 		if (redhatRelease.contains("release 6")) sockets = sshCommandRunner.runCommandAndWait("lscpu | grep 'CPU socket'").getStdout().split(":")[1].trim();
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
 			this.baseurl			= getConfFileParameter(rhsmConfFile, "baseurl").replaceFirst("/$", "");
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
 			RemoteFileTasks.runCommandAndAssert(sshCommandRunner,"cd "+caCertDir+"; wget --no-clobber --no-check-certificate \""+repoCaCertUrl+"\"",Integer.valueOf(0),null,"."+repoCaCert+". saved|File ."+repoCaCert+". already there");
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
 
 	public void installSubscriptionManagerRPMs(List<String> rpmUrls, String installOptions) {
 
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
 				//sshCommandRunner.runCommandAndWait("rpm -e "+pkg);
 				sshCommandRunner.runCommandAndWait("yum remove -y "+pkg);
 				RemoteFileTasks.runCommandAndAssert(sshCommandRunner,"rpm -q "+pkg,Integer.valueOf(1),"package "+pkg+" is not installed",null);
 			}
 		}
 
 		// install new rpms
 		for (String rpmUrl : rpmUrls) {
 			rpmUrl = rpmUrl.trim();
 			log.info("Installing RPM from "+rpmUrl+"...");
 			String sm_rpm = "/tmp/"+Arrays.asList(rpmUrl.split("/|=")).get(rpmUrl.split("/|=").length-1);
 			if (!sm_rpm.endsWith(".rpm")) sm_rpm+=".rpm";
 			RemoteFileTasks.runCommandAndAssert(sshCommandRunner,"wget -O "+sm_rpm+" --no-check-certificate \""+rpmUrl.trim()+"\"",Integer.valueOf(0),null,"."+sm_rpm+". saved");
 			// using yum localinstall should enable testing on RHTS boxes right off the bat.
 			Assert.assertEquals(sshCommandRunner.runCommandAndWait("yum -y localinstall "+sm_rpm+" "+installOptions).getExitCode(),Integer.valueOf(0),
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
 			if (!certDir.startsWith("/etc/pki/") && !certDir.startsWith("/tmp/")) log.warning("UNRECOGNIZED DIRECTORY.  NOT CLEANING CERTS FROM: "+certDir);
 			else sshCommandRunner.runCommandAndWait("rm -rf "+certDir+"/*");
 		}
 		
 		if (entitlements) {
 			//certDir = getConfigFileParameter("entitlementCertDir");
 			certDir = this.entitlementCertDir;
 			log.info("Cleaning out certs from entitlementCertDir: "+certDir);
 			if (!certDir.startsWith("/etc/pki/") && !certDir.startsWith("/tmp/")) log.warning("UNRECOGNIZED DIRECTORY.  NOT CLEANING CERTS FROM: "+certDir);
 			else sshCommandRunner.runCommandAndWait("rm -rf "+certDir+"/*");
 		}
 	}
 	
 	public void updateYumRepoParameter(String yumRepoFile, String repoid, String parameter, String value){
 		log.info("Updating yumrepo file '"+yumRepoFile+"' repoid '"+repoid+"' parameter '"+parameter+"' value to: "+value);
 		String command = "sed -i \"/\\["+repoid+"\\]/,/\\[/ s/^"+parameter+"\\s*=.*/"+parameter+"="+value+"/\" "+yumRepoFile;
 		RemoteFileTasks.runCommandAndAssert(sshCommandRunner,command,Integer.valueOf(0));
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
 		if (parameter.equals("baseurl"))			this.baseurl = value;
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
 	
 
 	/**
 	 * This method should be deleted and replaced with calls to getConfFileParameter(String confFile, String section, String parameter)
 	 * @param confFile
 	 * @param parameter
 	 * @return
 	 */
 	public String getConfFileParameter(String confFile, String parameter){
 		// Note: parameter can be case insensitive
 		SSHCommandResult result = RemoteFileTasks.runCommandAndAssert(sshCommandRunner, "grep -iE ^"+parameter+" "+confFile, 0/*, "^"+parameter, null*/);
 		String value = result.getStdout().split("=|:",2)[1];
 		return value.trim();
 	}
 	/**
 	 * @param confFile
 	 * @param section
 	 * @param parameter
 	 * @return value of the section.parameter config (null when not found)
 	 */
 	public String getConfFileParameter(String confFile, String section, String parameter){
 
 		SSHCommandResult result = RemoteFileTasks.runCommandAndAssert(sshCommandRunner, "egrep -v  \"^\\s*(#|$)\" "+confFile, 0);
 		
 		//	[root@jsefler-onprem-62server ~]# egrep -v  "^\s*(#|$)" /etc/rhsm/rhsm.conf
 		//	[server]
 		//	hostname=jsefler-onprem-62candlepin.usersys.redhat.com
 		//	prefix=/candlepin
 		//	port=8443
 		//	insecure=0
 		//	ssl_verify_depth = 3
 		//	ca_cert_dir=/etc/rhsm/ca/
 		//	proxy_hostname =
 		//	proxy_port = 
 		//	proxy_user =
 		//	proxy_password =
 		//	[rhsm]
 		//	baseurl=https://cdn.redhat.com
 		//	repo_ca_cert=%(ca_cert_dir)sredhat-uep.pem
 		//	productCertDir=/etc/pki/product
 		//	entitlementCertDir=/etc/pki/entitlement
 		//	consumercertdir=/etc/pki/consumer
 		//	certfrequency=2400
 		//	proxy_port = BAR
 		//	[rhsmcertd]
 		//	certFrequency=240
 		
 		// ^\[rhsm\](?:\n[^\[]*?)+^(?:consumerCertDir|consumercertdir)\s*[=:](.*)
 		String parameterRegex = "(?:"+parameter+"|"+parameter.toLowerCase()+")";	// note: python may write and tolerate all lowercase parameter names
 		String regex = "^\\["+section+"\\](?:\\n[^\\[]*?)+^"+parameterRegex+"\\s*[=:](.*)";
 		Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
 		Matcher matcher = pattern.matcher(result.getStdout());
 		if (!matcher.find()) {
 			log.warning("Did not find section '"+section+"' parameter '"+parameter+"' in conf file '"+confFile+"'.");
 			return null;
 		}
 
 //		log.fine("Matches: ");
 //		do {
 //			log.fine(matcher.group());
 //		} while (matcher.find());
 		return matcher.group(1).trim();	// return the contents of the first capturing group
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
 	 * Update the rhsmcertd frequency configurations in /etc/rhsm/rhsm.conf file and restart the rhsmcertd service.
 	 * @param certFrequency - Frequency of certificate refresh (in minutes) (passing null will not change the current value)
 	 * @param healFrequency - Frequency of subscription auto healing (in minutes) (passing null will not change the current value)
 	 * @param waitForMinutes - after restarting, should we wait for the next certFrequency refresh?
 	 */
 	public void restart_rhsmcertd (Integer certFrequency, Integer healFrequency, boolean waitForMinutes){
 //		updateConfFileParameter(rhsmConfFile, "certFrequency", String.valueOf(certFrequency));
 //		updateConfFileParameter(rhsmConfFile, "healFrequency", String.valueOf(healFrequency));
 		
 		// use config to set the certFrequency and healFrequency in one call
 		List<String[]> listOfSectionNameValues = new ArrayList<String[]>();
 		if (certFrequency!=null) listOfSectionNameValues.add(new String[]{"rhsmcertd", "certFrequency".toLowerCase(), String.valueOf(certFrequency)});
 		else certFrequency = Integer.valueOf(getConfFileParameter(rhsmConfFile, "rhsmcertd", "certFrequency"));
 		if (healFrequency!=null) listOfSectionNameValues.add(new String[]{"rhsmcertd", "healFrequency".toLowerCase(), String.valueOf(healFrequency)});
 		else healFrequency = Integer.valueOf(getConfFileParameter(rhsmConfFile, "rhsmcertd", "healFrequency"));
 		if (listOfSectionNameValues.size()>0) config(null,null,true,listOfSectionNameValues);
 		
 		// mark the rhsmcertd log file before restarting the deamon
 		String rhsmcertdLogMarker = System.currentTimeMillis()+" Testing service rhsmcertd restart...";
 		RemoteFileTasks.markFile(sshCommandRunner, rhsmcertdLogFile, rhsmcertdLogMarker);
 		
 		// TEMPORARY WORKAROUND FOR BUG: https://bugzilla.redhat.com/show_bug.cgi?id=691137 - jsefler 03/26/2011
 		if (this.arch.equals("s390x") || this.arch.equals("ppc64")) {
 			boolean invokeWorkaroundWhileBugIsOpen = true;
 			String bugId="691137"; 
 			try {if (invokeWorkaroundWhileBugIsOpen&&BzChecker.getInstance().isBugOpen(bugId)) {log.fine("Invoking workaround for "+BzChecker.getInstance().getBugState(bugId).toString()+" Bugzilla bug "+bugId+".  (https://bugzilla.redhat.com/show_bug.cgi?id="+bugId+")");} else {invokeWorkaroundWhileBugIsOpen=false;}} catch (XmlRpcException xre) {/* ignore exception */} catch (RuntimeException re) {/* ignore exception */}
 			if (invokeWorkaroundWhileBugIsOpen) {
 				RemoteFileTasks.runCommandAndWait(sshCommandRunner,"service rhsmcertd restart", LogMessageUtil.action());
 			} else {
 				RemoteFileTasks.runCommandAndAssert(sshCommandRunner,"service rhsmcertd restart",Integer.valueOf(0),"^Starting rhsmcertd "+certFrequency+" "+healFrequency+"\\[  OK  \\]$",null);	
 			}
 		} else {
 		// END OF WORKAROUND
 		RemoteFileTasks.runCommandAndAssert(sshCommandRunner,"service rhsmcertd restart",Integer.valueOf(0),"^Starting rhsmcertd "+certFrequency+" "+healFrequency+"\\[  OK  \\]$",null);	
 		}
 		// # service rhsmcertd restart
 		// rhsmcertd (pid 10172 10173) is running...
 		
 		// # tail -f /var/log/rhsm/rhsmcertd.log
 		// Wed Nov  9 15:21:54 2011: started: interval = 1440 minutes
 		// Wed Nov  9 15:21:54 2011: started: interval = 240 minutes
 		// Wed Nov  9 15:21:55 2011: certificates updated
 		// Wed Nov  9 15:21:55 2011: certificates updated
 
 		//RemoteFileTasks.runCommandAndAssert(sshCommandRunner,"service rhsmcertd status",Integer.valueOf(0),"^rhsmcertd \\(pid \\d+ \\d+\\) is running...$",null);	// RHEL62 branch
 		//RemoteFileTasks.runCommandAndAssert(sshCommandRunner,"service rhsmcertd status",Integer.valueOf(0),"^rhsmcertd \\(pid \\d+) is running...$",null);		// master/RHEL58 branch
 		RemoteFileTasks.runCommandAndAssert(sshCommandRunner,"service rhsmcertd status",Integer.valueOf(0),"^rhsmcertd \\(pid( \\d+){1,2}\\) is running...$",null);	// tolerate 1 or 2 pids for RHEL62 or RHEL58; don't really care which it is since the next assert is really sufficient
 
 		// TEMPORARY WORKAROUND FOR BUG: https://bugzilla.redhat.com/show_bug.cgi?id=752572 - jsefler 11/9/2011
 		boolean invokeWorkaroundWhileBugIsOpen = true;
 		String bugId="752572"; 
 		try {if (invokeWorkaroundWhileBugIsOpen&&BzChecker.getInstance().isBugOpen(bugId)) {log.fine("Invoking workaround for "+BzChecker.getInstance().getBugState(bugId).toString()+" Bugzilla bug "+bugId+".  (https://bugzilla.redhat.com/show_bug.cgi?id="+bugId+")");} else {invokeWorkaroundWhileBugIsOpen=false;}} catch (XmlRpcException xre) {/* ignore exception */} catch (RuntimeException re) {/* ignore exception */}
 		if (invokeWorkaroundWhileBugIsOpen) {
 			log.warning("Skipping assert of the rhsmcertd logging of the started: interval certFrequency and healFrequency while bug "+bugId+" is open.");
 		} else {
 		// END OF WORKAROUND
 		RemoteFileTasks.runCommandAndAssert(sshCommandRunner,"tail -4 "+rhsmcertdLogFile,Integer.valueOf(0),"(.*started: interval = "+healFrequency+" minutes\n.*started: interval = "+certFrequency+" minutes)|(.*started: interval = "+certFrequency+" minutes\n.*started: interval = "+healFrequency+" minutes)",null);
 		}
 		
 		SubscriptionManagerCLITestScript.sleep(10000);	// give the rhsmcertd time to make its initial check in with the candlepin server and update the certs
 
 		// assert the rhsmcertd log file reflected newly updated certificates...
 		String rhsmcertdLogResult = RemoteFileTasks.getTailFromMarkedFile(sshCommandRunner, rhsmcertdLogFile, rhsmcertdLogMarker, "certificates updated");
 		Assert.assertContainsMatch(rhsmcertdLogResult, ".*certificates updated\\n.*certificates updated", "The rhsmcertd is logging its restart.");
 
 		if (waitForMinutes && certFrequency!=null) {
 			SubscriptionManagerCLITestScript.sleep(certFrequency*60*1000);
 		}
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
 
 	/**
 	 * @return list of objects representing the subscription-manager list --available
 	 */
 	public List<SubscriptionPool> getCurrentlyAvailableSubscriptionPools() {
 		return SubscriptionPool.parse(listAvailableSubscriptionPools().getStdout());
 	}
 	
 	/**
 	 * @return list of objects representing the subscription-manager list --all --available
 	 */
 	public List<SubscriptionPool> getCurrentlyAllAvailableSubscriptionPools() {
 		return SubscriptionPool.parse(listAllAvailableSubscriptionPools().getStdout());
 	}
 	
 	/**
 	 * @return list of objects representing the subscription-manager list --consumed
 	 */
 	public List<ProductSubscription> getCurrentlyConsumedProductSubscriptions() {
 		return ProductSubscription.parse(listConsumedProductSubscriptions().getStdout());
 	}
 	
 	/**
 	 * @return list of objects representing the subscription-manager repos --list
 	 */
 	public List<Repo> getCurrentlySubscribedRepos() {
 		return Repo.parse(listSubscribedRepos().getStdout());
 	}
 	
 	/**
 	 * @return list of objects representing the Red Hat Repositories from /etc/yum.repos.d/redhat.repo
 	 */
 	public List<YumRepo> getCurrentlySubscribedYumRepos() {
 		// trigger a yum transaction so that subscription-manager plugin will refresh redhat.repo
 		//sshCommandRunner.runCommandAndWait("killall -9 yum"); // is this needed?
 		sshCommandRunner.runCommandAndWait("yum repolist all --disableplugin=rhnplugin"); // --disableplugin=rhnplugin helps avoid: up2date_client.up2dateErrors.AbuseError
 		
 		return YumRepo.parse(sshCommandRunner.runCommandAndWait("cat "+redhatRepoFile).getStdout());
 	}
 	
 	/**
 	 * @return list of objects representing the subscription-manager list --installed
 	 */
 	public List<InstalledProduct> getCurrentlyInstalledProducts() {
 		return InstalledProduct.parse(listInstalledProducts().getStdout());
 	}
 
 	public List<EntitlementCert> getCurrentEntitlementCerts() {
 
 		// THIS ORIGINAL IMPLEMENTATION HAS BEEN THROWING A	java.lang.StackOverflowError
 		// REIMPLEMENTING THIS METHOD TO HELP BREAK THE PROBLEM DOWN INTO SMALLER PIECES - jsefler 11/23/2010
 //		sshCommandRunner.runCommandAndWait("find "+entitlementCertDir+" -name '*.pem' | grep -v key.pem | xargs -I '{}' openssl x509 -in '{}' -noout -text");
 //		String certificates = sshCommandRunner.getStdout();
 //		return EntitlementCert.parse(certificates);
 
 		// STACK OVERFLOW PROBLEM FIXED
 //		List<EntitlementCert> entitlementCerts = new ArrayList<EntitlementCert>();
 //		for (File entitlementCertFile : getCurrentEntitlementCertFiles()) {
 //			entitlementCerts.add(getEntitlementCertFromEntitlementCertFile(entitlementCertFile));
 //		}
 //		return entitlementCerts;
 		
 		//sshCommandRunner.runCommandAndWait("find "+entitlementCertDir+" -name '*.pem' | grep -v key.pem | xargs -I '{}' openssl x509 -in '{}' -noout -text");
 		sshCommandRunner.runCommandAndWaitWithoutLogging("find "+entitlementCertDir+" -regex \".*/[0-9]+.pem\" -exec openssl x509 -in '{}' -noout -text \\; -exec echo \"    File: {}\" \\;");
 		String certificates = sshCommandRunner.getStdout();
 		return EntitlementCert.parse(certificates);
 	}
 	
 	public List<ProductCert> getCurrentProductCerts() {
 		/* THIS ORIGINAL IMPLEMENTATION DID NOT INCLUDE THE FILE IN THE OBJECT
 		sshCommandRunner.runCommandAndWaitWithoutLogging("find "+productCertDir+" -name '*.pem' | xargs -I '{}' openssl x509 -in '{}' -noout -text");
 		String certificates = sshCommandRunner.getStdout();
 		return ProductCert.parse(certificates);
 		*/
 		/*
 		List<ProductCert> productCerts = new ArrayList<ProductCert>();
 		for (File productCertFile : getCurrentProductCertFiles()) {
 			productCerts.add(ProductCert.parse(sshCommandRunner, productCertFile));
 		}
 		return productCerts;
 		*/
 		sshCommandRunner.runCommandAndWaitWithoutLogging("find "+productCertDir+" -name '*.pem' -exec openssl x509 -in '{}' -noout -text \\; -exec echo \"    File: {}\" \\;");
 		String certificates = sshCommandRunner.getStdout();
 		return ProductCert.parse(certificates);
 		
 	}
 	
 	/**
 	 * @return a ConsumerCert object corresponding to the current identity certificate parsed from the output of: openssl x509 -noout -text -in /etc/pki/consumer/cert.pem
 	 */
 	public ConsumerCert getCurrentConsumerCert() {
 		if (RemoteFileTasks.testFileExists(sshCommandRunner, this.consumerCertFile)!=1) {
 			log.info("Currently, there is no consumer registered.");
 			return null;
 		}
 		sshCommandRunner.runCommandAndWaitWithoutLogging("openssl x509 -noout -text -in "+this.consumerCertFile);
 		String certificate = sshCommandRunner.getStdout();
 		return ConsumerCert.parse(certificate);
 	}
 	
 	/**
 	 * @return consumerid from the Subject CN of the current /etc/pki/consumer/cert.pem identity x509 certificate
 	 */
 	public String getCurrentConsumerId() {
 		ConsumerCert currentConsumerCert = getCurrentConsumerCert();
 		if (currentConsumerCert==null) return null;
 		return currentConsumerCert.consumerid;
 	}
 	
 
 	public String getCurrentlyRegisteredOwnerKey() throws JSONException, Exception {
 		if (this.currentlyRegisteredOrg!=null) return this.currentlyRegisteredOrg;
 		
 //		String hostname = getConfFileParameter(rhsmConfFile, "hostname");
 //		String port = getConfFileParameter(rhsmConfFile, "port");
 //		String prefix = getConfFileParameter(rhsmConfFile, "prefix");
 		
 		return (CandlepinTasks.getOwnerKeyOfConsumerId(this.currentlyRegisteredUsername, this.currentlyRegisteredPassword, SubscriptionManagerBaseTestScript.sm_serverUrl, getCurrentConsumerId()));
 	}
 	
 	/**
 	 * @return from the contents of the current /etc/pki/consumer/cert.pem
 	 */
 	public List<Org> getOrgs(String username, String password) {
 //		List<String> orgs = new ArrayList<String>();
 //		SSHCommandResult result = orgs(username, password, null, null, null);
 //		for (String line : result.getStdout().split("\n")) {
 //			orgs.add(line);
 //		}
 //		if (orgs.size()>0) orgs.remove(0); // exclude the first title line of output...  orgs:
 //		return orgs;
 		
 		return Org.parse(orgs(username, password, null, null, null).getStdout());
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
 		
 		/*
 		Pattern pattern = Pattern.compile("^[a-f,0-9,\\-]{36} [^ ]*$", Pattern.MULTILINE);
 		Matcher matcher = pattern.matcher(registerResult.getStdout());
 		Assert.assertTrue(matcher.find(),"Found the registered UUID in the register result."); 
 		return matcher.group().split(" ")[0];
 		*/
 		
 		// The example output and code above is from RHEL61 and RHEL57, it has changed in RHEL62 to:
 		// The system with UUID 080ee4f9-736e-4195-88e1-8aff83250e7d has been unregistered
 		// The system has been registered with id: 3bc07645-781f-48ef-b3d4-8821dae438f8 
 
 		Pattern pattern = Pattern.compile("^The system has been registered with id: [a-f,0-9,\\-]{36} *$", Pattern.MULTILINE/* | Pattern.DOTALL*/);
 		Matcher matcher = pattern.matcher(registerResult.getStdout());
 		Assert.assertTrue(matcher.find(),"Found the registered UUID in the register result."); 
 		return matcher.group().split(":")[1].trim();
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
 	 * @param factsMap - map of key/values pairs that will get written as JSON to a facts file that will override the true facts on the system.  Note: subscription-manager facts --update may need to be called after this method to realize the override.
 	 */
 	public void createFactsFileWithOverridingValues (Map<String,String> factsMap) {
 		
 		// assemble an echo command and run it to create a facts file
 		String keyvaluesString = "";
 		for (String key : factsMap.keySet()) {
 			keyvaluesString += String.format("\"%s\":\"%s\", ", key, factsMap.get(key));
 		}
 		keyvaluesString = keyvaluesString.replaceFirst(", *$", "");
 		String echoCommand = String.format("echo '{%s}' > %s", keyvaluesString, factsOverrideFile);
         sshCommandRunner.runCommandAndWait(echoCommand);	// create an override facts file
 	}
 	public void deleteFactsFileWithOverridingValues () {
 		String deleteCommand = String.format("rm -f %s", factsOverrideFile);
 		sshCommandRunner.runCommandAndWait(deleteCommand);	// delete the override facts file
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
 	public Map<BigInteger, SubscriptionPool> getCurrentSerialMapToSubscriptionPools(String username, String password) throws Exception  {
 		
 		Map<BigInteger, SubscriptionPool> serialMapToSubscriptionPools = new HashMap<BigInteger, SubscriptionPool>();
 //		String hostname = getConfFileParameter(rhsmConfFile, "hostname");
 //		String port = getConfFileParameter(rhsmConfFile, "port");
 //		String prefix = getConfFileParameter(rhsmConfFile, "prefix");
 		for (EntitlementCert entitlementCert : getCurrentEntitlementCerts()) {
 			JSONObject jsonEntitlement = CandlepinTasks.getEntitlementUsingRESTfulAPI(username,password,SubscriptionManagerBaseTestScript.sm_serverUrl,entitlementCert.id);
 			String poolHref = jsonEntitlement.getJSONObject("pool").getString("href");
 			JSONObject jsonPool = new JSONObject(CandlepinTasks.getResourceUsingRESTfulAPI(username,password,SubscriptionManagerBaseTestScript.sm_serverUrl,poolHref));
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
 	 * @param username	- owner of the subscription pool (will be used in a REST api call to the candlepin server)
 	 * @param password
 	 * @return the SubscriptionPool from which this consumed ProductSubscription came from
 	 * @throws Exception
 	 */
 	public SubscriptionPool getSubscriptionPoolFromProductSubscription(ProductSubscription productSubscription, String username, String password) throws Exception {
 		
 		// if already known, return the SubscriptionPool from which ProductSubscription came
 		if (productSubscription.fromSubscriptionPool != null) return productSubscription.fromSubscriptionPool;
 		
 		productSubscription.fromSubscriptionPool = getCurrentSerialMapToSubscriptionPools(username, password).get(productSubscription.serialNumber);
 
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
 		String serialPemFile = entitlementCertDir+"/"+productSubscription.serialNumber+".pem";
 		sshCommandRunner.runCommandAndWaitWithoutLogging("openssl x509 -text -noout -in "+serialPemFile+"; echo \"    File: "+serialPemFile+"\"");	// openssl x509 -text -noout -in /etc/pki/entitlement/5066044962491605926.pem; echo "    File: /etc/pki/entitlement/5066044962491605926.pem"
 		String certificate = sshCommandRunner.getStdout();
 		List<EntitlementCert> entitlementCerts = EntitlementCert.parse(certificate);
 		Assert.assertEquals(entitlementCerts.size(), 1,"Only one EntitlementCert corresponds to ProductSubscription: "+productSubscription);
 		return entitlementCerts.get(0);
 	}
 	
 	/**
 	 * For the given ProductCert installed in /etc/pki/product, get the corresponding InstalledProduct from subscription-manager list --installed
 	 * @param productCert
 	 * @return instance of InstalledProduct (null if not found)
 	 */
 	public InstalledProduct getInstalledProductCorrespondingToProductCert(ProductCert productCert) {
 		return getInstalledProductCorrespondingToProductCert(productCert,getCurrentlyInstalledProducts());
 	}
 	public InstalledProduct getInstalledProductCorrespondingToProductCert(ProductCert productCert, List<InstalledProduct> fromInstalledProducts) {
 		for (InstalledProduct installedProduct : fromInstalledProducts) {
 			
 			// when a the product cert is missing OIDS, "None" is rendered in the list --installed
 			String name = productCert.productNamespace.name==null?"None":productCert.productNamespace.name;
 			String version = productCert.productNamespace.version==null?"None":productCert.productNamespace.version;
 			String arch = productCert.productNamespace.arch==null?"None":productCert.productNamespace.arch;
 			
 			if (installedProduct.productName.equals(name) &&
 				installedProduct.version.equals(version) &&
 				installedProduct.arch.equals(arch)) {
 				return installedProduct;
 			}
 		}
 		return null; // not found
 	}
 	
 	
 	public EntitlementCert getEntitlementCertCorrespondingToSubscribedPool(SubscriptionPool subscribedPool) {
 //		String hostname = getConfFileParameter(rhsmConfFile, "hostname");
 //		String port = getConfFileParameter(rhsmConfFile, "port");
 //		String prefix = getConfFileParameter(rhsmConfFile, "prefix");
 		
 		for (File entitlementCertFile : getCurrentEntitlementCertFiles("-t")) {
 			EntitlementCert entitlementCert = getEntitlementCertFromEntitlementCertFile(entitlementCertFile);
 			try {
 				JSONObject jsonEntitlement = CandlepinTasks.getEntitlementUsingRESTfulAPI(this.currentlyRegisteredUsername,this.currentlyRegisteredPassword,SubscriptionManagerBaseTestScript.sm_serverUrl,entitlementCert.id);
 				JSONObject jsonPool = new JSONObject(CandlepinTasks.getResourceUsingRESTfulAPI(this.currentlyRegisteredUsername,this.currentlyRegisteredPassword,SubscriptionManagerBaseTestScript.sm_serverUrl,jsonEntitlement.getJSONObject("pool").getString("href")));
 				if (jsonPool.getString("id").equals(subscribedPool.poolId)) {
 					return entitlementCert;
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 				Assert.fail(e.getMessage());
 			}
 		}
 		return null;	// not found
 	}
 	
 	public List<ProductCert> getCurrentProductCertsCorrespondingToSubscriptionPool(SubscriptionPool pool) throws JSONException, Exception {
 		List<ProductCert> currentProductCertsCorrespondingToSubscriptionPool = new ArrayList<ProductCert>();
 //		String hostname = getConfFileParameter(rhsmConfFile, "hostname");
 //		String port = getConfFileParameter(rhsmConfFile, "port");
 //		String prefix = getConfFileParameter(rhsmConfFile, "prefix");
 		List<ProductCert> currentProductCerts = getCurrentProductCerts();
 
 		JSONObject jsonPool = new JSONObject(CandlepinTasks.getResourceUsingRESTfulAPI(this.currentlyRegisteredUsername,this.currentlyRegisteredPassword,SubscriptionManagerBaseTestScript.sm_serverUrl,"/pools/"+pool.poolId));
 		JSONArray jsonProvidedProducts = (JSONArray) jsonPool.getJSONArray("providedProducts");
 		for (int k = 0; k < jsonProvidedProducts.length(); k++) {
 			JSONObject jsonProvidedProduct = (JSONObject) jsonProvidedProducts.get(k);
 			String providedProductId = jsonProvidedProduct.getString("productId");
 			
 			// is this productId among the installed ProductCerts? if so, add them all to the currentProductCertsCorrespondingToSubscriptionPool
 			currentProductCertsCorrespondingToSubscriptionPool.addAll(ProductCert.findAllInstancesWithMatchingFieldFromList("productId", providedProductId, currentProductCerts));
 		}
 		
 		return currentProductCertsCorrespondingToSubscriptionPool;
 	}
 	
 	public List <EntitlementCert> getEntitlementCertsCorrespondingToProductCert(ProductCert productCert) {
 		List<EntitlementCert> correspondingEntitlementCerts = new ArrayList<EntitlementCert>();
 		ProductNamespace productNamespaceMatchingProductCert = null;
 		for (EntitlementCert entitlementCert : getCurrentEntitlementCerts()) {
 			productNamespaceMatchingProductCert = ProductNamespace.findFirstInstanceWithMatchingFieldFromList("id", productCert.productId, entitlementCert.productNamespaces);	
 			if (productNamespaceMatchingProductCert!=null) {
 				correspondingEntitlementCerts.add(entitlementCert);
 			}
 		}
 		return correspondingEntitlementCerts;
 	}
 	
 	public EntitlementCert getEntitlementCertFromEntitlementCertFile(File serialPemFile) {
 		sshCommandRunner.runCommandAndWaitWithoutLogging("openssl x509 -text -noout -in "+serialPemFile+"; echo \"    File: "+serialPemFile+"\"");	// openssl x509 -text -noout -in /etc/pki/entitlement/5066044962491605926.pem; echo "    File: /etc/pki/entitlement/5066044962491605926.pem"
 		String certificates = sshCommandRunner.getStdout();
 		List<EntitlementCert> entitlementCerts = EntitlementCert.parse(certificates);
 		
 		// assert that only one EntitlementCert was parsed and return it
 		Assert.assertEquals(entitlementCerts.size(), 1, "Entitlement cert file '"+serialPemFile+"' parsed only one EntitlementCert.");
 		return entitlementCerts.get(0);
 	}
 	
 	public ProductCert getProductCertFromProductCertFile(File productPemFile) {
 		sshCommandRunner.runCommandAndWaitWithoutLogging("openssl x509 -noout -text -in "+productPemFile.getPath());
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
 	
 	public File getEntitlementCertKeyFileFromEntitlementCert(EntitlementCert entitlementCert) {
 		File serialKeyPemFile = new File(entitlementCertDir+File.separator+entitlementCert.serialNumber+"-key.pem");
 		return serialKeyPemFile;
 	}
 	
 	public File getEntitlementCertKeyFileCorrespondingToEntitlementCertFile(File entitlementCertFile) {
 		// 239223656620993791.pem  => 239223656620993791-key.pem
 		String serialKeyPem = entitlementCertFile.getPath().replaceAll("(\\.\\w*)$", "-key$1");
 		// 239223656620993791      => 239223656620993791-key
 		if (!serialKeyPem.contains("-key.")) serialKeyPem += "-key";
 
 		return new File(serialKeyPem);
 	}
 	
 	// register module tasks ************************************************************
 	
 	/**
 	 * register WITHOUT asserting results.
 	 * @param autoheal TODO
 	 * @throws Exception 
 	 */
 	public SSHCommandResult register_(String username, String password, String org, String environment, ConsumerType type, String name, String consumerId, Boolean autosubscribe, List<String> activationKeys, Boolean force, Boolean autoheal, String proxy, String proxyuser, String proxypassword) {
 		
 		// assemble the command
 		String command = this.command;											command += " register";
 		if (username!=null)														command += " --username="+username;
 		if (password!=null)														command += " --password="+password;
 		if (org!=null)															command += " --org="+org;
 		if (environment!=null)													command += " --environment="+environment;
 		if (type!=null)															command += " --type="+type;
 		if (name!=null)															command += " --name="+String.format(name.contains("\"")?"'%s'":"\"%s\"", name./*escape backslashes*/replace("\\", "\\\\")./*escape backticks*/replace("`", "\\`"));
 		if (consumerId!=null)													command += " --consumerid="+consumerId;
 		if (autosubscribe!=null && autosubscribe)								command += " --autosubscribe";
 		if (activationKeys!=null)	for (String activationKey : activationKeys)	command += " --activationkey="+activationKey;
 		if (force!=null && force)												command += " --force";
 		if (proxy!=null)														command += " --proxy="+proxy;
 		if (proxyuser!=null)													command += " --proxyuser="+proxyuser;
 		if (proxypassword!=null)												command += " --proxypassword="+proxypassword;
 
 		// run command without asserting results
 		SSHCommandResult sshCommandResult = sshCommandRunner.runCommandAndWait(command);
 		
 		// reset this.currentlyRegistered values
 		if (sshCommandResult.getExitCode().equals(Integer.valueOf(0))) {		// The system has been registered with id: 660faf39-a8f2-4311-acf2-5c1bb3c141ef
 			this.currentlyRegisteredUsername = username;
 			this.currentlyRegisteredPassword = password;
 			this.currentlyRegisteredOrg = org;
 			this.currentlyRegisteredType = type;
 		} else
 		if (sshCommandResult.getExitCode().equals(Integer.valueOf(1))) {		// This system is already registered. Use --force to override
 		} else
 		if (sshCommandResult.getExitCode().equals(Integer.valueOf(255))) {		// Error
 			this.currentlyRegisteredUsername = null;
 			this.currentlyRegisteredPassword = null;
 			this.currentlyRegisteredOrg = null;
 			this.currentlyRegisteredType = null;	
 		}
 		
 		// set autoheal for the consumer
 //		if (autoheal!=null && !sshCommandResult.getExitCode().equals(Integer.valueOf(255))) {
 //			// first get the consumerId
 //			if (consumerId==null) {
 //				if (sshCommandResult.getExitCode().equals(Integer.valueOf(0))) {	// The system has been registered with id: 660faf39-a8f2-4311-acf2-5c1bb3c141ef
 //					consumerId = getCurrentConsumerId(sshCommandResult);
 //				} else
 //				if (sshCommandResult.getExitCode().equals(Integer.valueOf(1))) {	// This system is already registered. Use --force to override
 //					consumerId = getCurrentConsumerId();
 //				}
 //			}
 //			// now set the autoheal attribute of the consumer
 //			try {
 //				CandlepinTasks.setAutohealForConsumer(currentlyRegisteredUsername, currentlyRegisteredPassword, SubscriptionManagerBaseTestScript.sm_serverUrl, consumerId, autoheal);
 //			} catch (Exception e) {
 //				e.printStackTrace();
 //				Assert.fail(e.getMessage());
 //			} 
 //		}
 
 		// set autoheal for newly registered consumer only
 		if (autoheal!=null && sshCommandResult.getExitCode().equals(Integer.valueOf(0))) {
 			try {
 				// Note: NullPointerException will likely occur when activationKeys are used because null will likely be passed for username/password
 				CandlepinTasks.setAutohealForConsumer(currentlyRegisteredUsername, currentlyRegisteredPassword, SubscriptionManagerBaseTestScript.sm_serverUrl, getCurrentConsumerId(sshCommandResult), autoheal);
 			} catch (Exception e) {
 				e.printStackTrace();
 				Assert.fail(e.getMessage());
 			} 
 		}
 		
 		return sshCommandResult;
 	}
 	
 	/**
 	 * register WITHOUT asserting results.
 	 * @param autoheal TODO
 	 */
 	public SSHCommandResult register_(String username, String password, String org, String environment, ConsumerType type, String name, String consumerId, Boolean autosubscribe, String activationKey, Boolean force, Boolean autoheal, String proxy, String proxyuser, String proxypassword) {
 		
 		List<String> activationKeys = activationKey==null?null:Arrays.asList(new String[]{activationKey});
 
 		return register_(username, password, org, environment, type, name, consumerId, autosubscribe, activationKeys, force, autoheal, proxy, proxyuser, proxypassword);
 	}
 	
 	
 
 	
 	public SSHCommandResult register(String username, String password, String org, String environment, ConsumerType type, String name, String consumerId, Boolean autosubscribe, List<String> activationKeys, Boolean force, Boolean autoheal, String proxy, String proxyuser, String proxypassword) {
 		
 		SSHCommandResult sshCommandResult = register_(username, password, org, environment, type, name, consumerId, autosubscribe, activationKeys, force, autoheal, proxy, proxyuser, proxypassword);
 	
 		// when already registered, just return without any assertions
 		if ((force==null || !force) && sshCommandResult.getStdout().startsWith("This system is already registered.")) return sshCommandResult;
 
 		// assert results for a successful registration
 		Assert.assertEquals(sshCommandResult.getExitCode(), Integer.valueOf(0), "The exit code from the register command indicates a success.");
 		if (type==ConsumerType.person) name = username;		// https://bugzilla.redhat.com/show_bug.cgi?id=661130
 		if (name==null) name = this.hostname;				// https://bugzilla.redhat.com/show_bug.cgi?id=669395
 		//Assert.assertContainsMatch(sshCommandResult.getStdout().trim(), "[a-f,0-9,\\-]{36} "+name);	// applicable to RHEL61 and RHEL57. changed in RHEL62 due to feedback from mmccune https://engineering.redhat.com/trac/kalpana/wiki/SubscriptionManagerReview - jsefler 6/28/2011
 		Assert.assertContainsMatch(sshCommandResult.getStdout().trim(), "The system has been registered with id: [a-f,0-9,\\-]{36}");
 		
 		// assert that register with consumerId returns the expected uuid
 		if (consumerId!=null) {
 			//Assert.assertEquals(sshCommandResult.getStdout().trim(), consumerId+" "+username, "register to an exiting consumer was a success");
 			//Assert.assertContainsMatch(sshCommandResult.getStdout().trim(), "^"+consumerId, "register to an exiting consumer was a success");	// removed name from assert to account for https://bugzilla.redhat.com/show_bug.cgi?id=669395	// applicable to RHEL61 and RHEL57.
 			Assert.assertContainsMatch(sshCommandResult.getStdout().trim(), "The system has been registered with id: "+consumerId, "register to an exiting consumer was a success");	// removed name from assert to account for https://bugzilla.redhat.com/show_bug.cgi?id=669395
 		}
 		
 		// assert certificate files are installed into /etc/pki/consumer
 		Assert.assertEquals(RemoteFileTasks.testFileExists(sshCommandRunner,this.consumerKeyFile),1, "Consumer key file '"+this.consumerKeyFile+"' must exist after register.");
 		Assert.assertEquals(RemoteFileTasks.testFileExists(sshCommandRunner,this.consumerCertFile),1, "Consumer cert file '"+this.consumerCertFile+"' must exist after register.");
 		
 		// TEMPORARY WORKAROUND FOR BUG: https://bugzilla.redhat.com/show_bug.cgi?id=639417 - jsefler 10/1/2010
 		boolean invokeWorkaroundWhileBugIsOpen = true;
 		String bugId="639417"; 
 		try {if (invokeWorkaroundWhileBugIsOpen&&BzChecker.getInstance().isBugOpen(bugId)) {log.fine("Invoking workaround for "+BzChecker.getInstance().getBugState(bugId).toString()+" Bugzilla bug "+bugId+".  (https://bugzilla.redhat.com/show_bug.cgi?id="+bugId+")");} else {invokeWorkaroundWhileBugIsOpen=false;}} catch (XmlRpcException xre) {/* ignore exception */} catch (RuntimeException re) {/* ignore exception */}
 		if (invokeWorkaroundWhileBugIsOpen) {
 			restart_rhsmcertd(Integer.valueOf(getConfFileParameter(rhsmConfFile, "certFrequency")), null, false);
 		}
 		// END OF WORKAROUND
 		
 
 		return sshCommandResult; // from the register command
 	}
 	
 	public SSHCommandResult register(String username, String password, String org, String environment, ConsumerType type, String name, String consumerId, Boolean autosubscribe, String activationKey, Boolean force, Boolean autoheal, String proxy, String proxyuser, String proxypassword) {
 		
 		List<String> activationKeys = activationKey==null?null:Arrays.asList(new String[]{activationKey});
 
 		return register(username, password, org, environment, type, name, consumerId, autosubscribe, activationKeys, force, autoheal, proxy, proxyuser, proxypassword);
 	}
 	
 	
 	// reregister module tasks ************************************************************
 
 //	/**
 //	 * reregister without asserting results
 //	 */
 //	public SSHCommandResult reregister_(String username, String password, String consumerid) {
 //
 //		// assemble the command
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
 		return register(username,password,null,null,null,null,consumerId, null, new ArrayList<String>(), null, null, null, null, null);
 	}
 	
 	
 	
 	// clean module tasks ************************************************************
 
 	/**
 	 * clean without asserting results
 	 * @param proxy TODO
 	 * @param proxyuser TODO
 	 * @param proxypassword TODO
 	 */
 	public SSHCommandResult clean_(String proxy, String proxyuser, String proxypassword) {
 
 		// assemble the command
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
 		this.currentlyRegisteredUsername = null;
 		this.currentlyRegisteredPassword = null;
 		this.currentlyRegisteredOrg = null;
 		this.currentlyRegisteredType = null;
 		
 		// assert that the entitlement cert directory is gone
 		//Assert.assertFalse(RemoteFileTasks.testFileExists(sshCommandRunner,entitlementCertDir)==1, entitlementCertDir+" does NOT exist after clean.");
 		// assert that the entitlement cert directory is gone (or is empty)
 		if (RemoteFileTasks.testFileExists(sshCommandRunner,entitlementCertDir)==1) {
 			Assert.assertEquals(sshCommandRunner.runCommandAndWait("ls "+entitlementCertDir).getStdout(), "", "The entitlement cert directory is empty after running clean.");
 		}
 
 		return sshCommandResult; // from the clean command
 	}
 	
 	
 	
 	// import module tasks ************************************************************
 
 	/**
 	 * import WITHOUT asserting results
 	 * @param certificates - list of paths to certificate files to be imported
 	 * @param proxy
 	 * @param proxyuser
 	 * @param proxypassword
 	 * @return
 	 */
 	public SSHCommandResult importCertificate_(List<String> certificates/*, String proxy, String proxyuser, String proxypassword*/) {
 
 		// assemble the command
 		String command = this.command;									command += " import";
 		if (certificates!=null)	for (String certificate : certificates)	command += " --certificate="+certificate;
 
 //		if (proxy!=null)				command += " --proxy="+proxy;
 //		if (proxyuser!=null)			command += " --proxyuser="+proxyuser;
 //		if (proxypassword!=null)		command += " --proxypassword="+proxypassword;
 		
 		// run command without asserting results
 		return sshCommandRunner.runCommandAndWait(command);
 	}
 	
 	/**
 	 * import WITHOUT asserting results.
 	 */
 	public SSHCommandResult importCertificate_(String certificate/*, String proxy, String proxyuser, String proxypassword*/) {
 		
 		List<String> certificates = certificate==null?null:Arrays.asList(new String[]{certificate});
 
 		return importCertificate_(certificates/*, proxy, proxyuser, proxypassword*/);
 	}
 	
 	/**
 	 * import with assertions that the results are a success"
 	 * @param certificates - list of paths to certificates file to be imported
 	 * @param proxy
 	 * @param proxyuser
 	 * @param proxypassword
 	 * @return
 	 */
 	public SSHCommandResult importCertificate(List<String> certificates/*, String proxy, String proxyuser, String proxypassword*/) {
 		
 		SSHCommandResult sshCommandResult = importCertificate_(certificates/*, proxy, proxyuser, proxypassword*/);
 		
 		// assert results for a successful import
 		Assert.assertEquals(sshCommandResult.getExitCode(), Integer.valueOf(0), "The exit code from the import command indicates a success.");
 		
 		// Successfully imported certificate {0}
 		for (String certificate: certificates) {
 			String successMsg = "Successfully imported certificate "+(new File(certificate)).getName();
 			Assert.assertTrue(sshCommandResult.getStdout().contains(successMsg),"The stdout from the import command contains expected message: "+successMsg);		
 		}
 	
 		// {0} is not a valid certificate file. Please use a valid certificate.
 		
 		// assert that the entitlement certificate has been extracted to /etc/pki/entitlement
 		//Assert.assertTrue(RemoteFileTasks.testFileExists(sshCommandRunner,consumerCertDir)==1, consumerCertDir+" does NOT exist after clean.");
 
 		// assert that the key has been extracted to /etc/pki/entitlement
 		//Assert.assertTrue(RemoteFileTasks.testFileExists(sshCommandRunner,consumerCertDir)==1, consumerCertDir+" does NOT exist after clean.");
 
 		return sshCommandResult; // from the import command
 	}
 	
 	public SSHCommandResult importCertificate(String certificate/*, String proxy, String proxyuser, String proxypassword*/) {
 		
 		List<String> certificates = certificate==null?null:Arrays.asList(new String[]{certificate});
 
 		return importCertificate(certificates/*, proxy, proxyuser, proxypassword*/);
 	}
 	
 	// refresh module tasks ************************************************************
 
 	/**
 	 * refresh without asserting results
 	 * @param proxy TODO
 	 * @param proxyuser TODO
 	 * @param proxypassword TODO
 	 */
 	public SSHCommandResult refresh_(String proxy, String proxyuser, String proxypassword) {
 
 		// assemble the command
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
 
 		// assemble the command
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
 		regenerate = regenerate==null? false:regenerate;	// the non-null default value for regenerate is false
 
 		// assert results for a successful identify
 		/* Example sshCommandResult.getStdout():
 		 * Current identity is: 8f4dd91a-2c41-4045-a937-e3c8554a5701 name: testuser1
 		 */
 		Assert.assertEquals(sshCommandResult.getExitCode(), Integer.valueOf(0), "The exit code from the identity command indicates a success.");
 		
 		
 		// TEMPORARY WORKAROUND FOR BUG: https://bugzilla.redhat.com/show_bug.cgi?id=719109 - jsefler 7/05/2011
 		boolean invokeWorkaroundWhileBugIsOpen = true;
 		String bugId="719109"; 
 		try {if (invokeWorkaroundWhileBugIsOpen&&BzChecker.getInstance().isBugOpen(bugId)) {log.fine("Invoking workaround for "+BzChecker.getInstance().getBugState(bugId).toString()+" Bugzilla bug "+bugId+".  (https://bugzilla.redhat.com/show_bug.cgi?id="+bugId+")");} else {invokeWorkaroundWhileBugIsOpen=false;}} catch (XmlRpcException xre) {/* ignore exception */} catch (RuntimeException re) {/* ignore exception */}
 		if (invokeWorkaroundWhileBugIsOpen) {
 			// skip the assertion of user feedback in stdout
 			return sshCommandResult;
 		}
 		// END OF WORKAROUND
 		
 		
 		if (regenerate) {
 			Assert.assertEquals(sshCommandResult.getStdout().trim(), "Identity certificate has been regenerated.");
 		} else {
 			Assert.assertContainsMatch(sshCommandResult.getStdout().trim(), "Current identity is: [a-f,0-9,\\-]{36}");
 		}
 		
 		return sshCommandResult; // from the identity command
 	}
 	
 	
 	// orgs module tasks ************************************************************
 
 	/**
 	 * orgs without asserting results
 	 * @param username
 	 * @param password
 	 * @return
 	 */
 	public SSHCommandResult orgs_(String username, String password, String proxy, String proxyuser, String proxypassword) {
 
 		// assemble the command
 		String command = this.command;	command += " orgs";
 		if (username!=null)				command += " --username="+username;
 		if (password!=null)				command += " --password="+password;
 		if (proxy!=null)				command += " --proxy="+proxy;
 		if (proxyuser!=null)			command += " --proxyuser="+proxyuser;
 		if (proxypassword!=null)		command += " --proxypassword="+proxypassword;
 		
 		// run command without asserting results
 		return sshCommandRunner.runCommandAndWait(command);
 	}
 	
 	/**
 	 * "subscription-manager orgs"
 	 * @param username
 	 * @param password
 	 * @return
 	 */
 	public SSHCommandResult orgs(String username, String password, String proxy, String proxyuser, String proxypassword) {
 		
 		SSHCommandResult sshCommandResult = orgs_(username, password, proxy, proxyuser, proxypassword);
 		
 		// assert results...
 		
 		// assert the exit code was a success
 		Assert.assertEquals(sshCommandResult.getExitCode(), Integer.valueOf(0), "The exit code from the orgs command indicates a success.");
 
 		// assert the expected banner
 		/*
 		+-------------------------------------------+
 			        testuser1 Organizations
 		+-------------------------------------------+
 		*/
 		String regex = username+" Organizations";
 		Assert.assertContainsMatch(sshCommandResult.getStdout().trim(), regex);
 		
 		return sshCommandResult; // from the orgs command
 	}
 	
 	
 	// config module tasks ************************************************************
 
 	/**
 	 * config without asserting results
 	 */
 	public SSHCommandResult config_(Boolean list, Boolean remove, Boolean set, List<String[]> listOfSectionNameValues) {
 
 		// assemble the command
 		String command = this.command;				command += " config";
 		if (list!=null && list)						command += " --list";
 		for (String[] section_name_value : listOfSectionNameValues) {
 			if (remove!=null && remove)				command += String.format(" --remove=%s.%s", section_name_value[0],section_name_value[1]);  // expected format section.name
 			if (set!=null && set)					command += String.format(" --%s.%s=%s", section_name_value[0],section_name_value[1],section_name_value[2]);  // expected format section.name=value
 		}
 		
 		// run command without asserting results
 		return sshCommandRunner.runCommandAndWait(command);
 	}
 	
 	/**
 	 * config without asserting results
 	 */
 	public SSHCommandResult config_(Boolean list, Boolean remove, Boolean set, String[] section_name_value) {
 		List<String[]> listOfSectionNameValues = new ArrayList<String[]>();
 		listOfSectionNameValues.add(section_name_value);
 		return config_(list, remove, set, listOfSectionNameValues);
 	}
 	
 	/**
 	 * "subscription-manager config"
 	 */
 	public SSHCommandResult config(Boolean list, Boolean remove, Boolean set, List<String[]> listOfSectionNameValues) {
 		
 		SSHCommandResult sshCommandResult = config_(list, remove, set, listOfSectionNameValues);
 		
 		// assert results...
 		
 		// assert the exit code was a success
 		Assert.assertEquals(sshCommandResult.getExitCode(), Integer.valueOf(0), "The exit code from the config command indicates a success.");
 
 		/*
 		[root@jsefler-onprem-62server ~]# subscription-manager config --list
 		[server]
 		   ca_cert_dir = [/etc/rhsm/ca/]
 		   hostname = jsefler-onprem-62candlepin.usersys.redhat.com
 		   insecure = [0]
 		   port = [8443]
 		   prefix = [/candlepin]
 		   proxy_hostname = []
 		   proxy_password = []
 		   proxy_port = []
 		   proxy_user = []
 		   repo_ca_cert = [/etc/rhsm/ca/redhat-uep.pem]
 		   ssl_verify_depth = [3]
 
 		[rhsm]
 		   baseurl = https://cdn.redhat.com
 		   ca_cert_dir = [/etc/rhsm/ca/]
 		   certfrequency = 2400
 		   consumercertdir = /etc/pki/consumer
 		   entitlementcertdir = /etc/pki/entitlement
 		   hostname = [localhost]
 		   insecure = [0]
 		   port = [8443]
 		   prefix = [/candlepin]
 		   productcertdir = /etc/pki/product
 		   proxy_hostname = []
 		   proxy_password = []
 		   proxy_port = BAR
 		   proxy_user = []
 		   repo_ca_cert = [/etc/rhsm/ca/redhat-uep.pem]
 		   ssl_verify_depth = [3]
 
 		[rhsmcertd]
 		   ca_cert_dir = [/etc/rhsm/ca/]
 		   certfrequency = 240
 		   hostname = [localhost]
 		   insecure = [0]
 		   port = [8443]
 		   prefix = [/candlepin]
 		   proxy_hostname = []
 		   proxy_password = []
 		   proxy_port = []
 		   proxy_user = []
 		   repo_ca_cert = [/etc/rhsm/ca/redhat-uep.pem]
 		   ssl_verify_depth = [3]
 
 		[] - Default value in use
 
 		[root@jsefler-onprem-62server ~]# echo $?
 		0
 		[root@jsefler-onprem-62server ~]# subscription-manager config --remove=rhsmcertd.certfrequency
 		You have removed the value for section rhsmcertd and name certfrequency.
 		[root@jsefler-onprem-62server ~]# echo $?
 		0
 		[root@jsefler-onprem-62server ~]# subscription-manager config --rhsmcertd.certfrequency=240
 		[root@jsefler-onprem-62server ~]# echo $?
 		0
 		[root@jsefler-onprem-62server ~]# 
 		 */
 		
 		// assert remove stdout indicates a success
 		if (remove!=null && remove) {
 			for (String[] section_name_value : listOfSectionNameValues) {
 				String section	= section_name_value[0];
 				String name		= section_name_value[1];
 				String value	= section_name_value[2];
 				//# subscription-manager config --remove rhsmcertd.port
 				//You have removed the value for section rhsmcertd and name port.
 				//The default value for port will now be used.
 				//Assert.assertTrue(sshCommandResult.getStdout().contains("You have removed the value for section "+section+" and name "+name+".\nThe default value for "+name+" will now be used."), "The stdout indicates the removal of config parameter name '"+name+"' from section '"+section+"'.");
 				Assert.assertTrue(sshCommandResult.getStdout().contains("You have removed the value for section "+section+" and name "+name+"."), "The stdout indicates the removal of config parameter name '"+name+"' from section '"+section+"'.");
 				Assert.assertEquals(sshCommandResult.getStdout().contains("The default value for "+name+" will now be used."), defaultConfFileParameterNames().contains(name), "The stdout indicates the default value for '"+name+"' will now be used after having removed it from section '"+section+"'.");
 			}
 		}
 
 		
 		return sshCommandResult; // from the orgs command
 	}
 	
 	public SSHCommandResult config(Boolean list, Boolean remove, Boolean set, String[] section_name_value) {
 		List<String[]> listOfSectionNameValues = new ArrayList<String[]>();
 		listOfSectionNameValues.add(section_name_value);
 		return config(list, remove, set, listOfSectionNameValues);
 	}
 	
 	public List<String> defaultConfFileParameterNames() {
 		
 		// hard-coded list of parameter called DEFAULTS in /usr/lib/python2.6/site-packages/rhsm/config.py
 		// this list of hard-coded parameter names have a hard-coded value (not listed here) that will be used
 		// after a user calls subscription-manager --remove section.name otherwise the remove will set the value to ""
 		List<String> defaultNames = new ArrayList<String>();
 
 		// initialize defaultNames (will appear in all config sections and have a default value)
 		//	DEFAULTS = {
 		//	        'hostname': 'localhost',
 		//	        'prefix': '/candlepin',
 		//	        'port': '8443',
 		//	        'ca_cert_dir': '/etc/rhsm/ca/',
 		//	        'repo_ca_cert': '/etc/rhsm/ca/redhat-uep.pem',
 		//	        'ssl_verify_depth': '3',
 		//	        'proxy_hostname': '',
 		//	        'proxy_port': '',
 		//	        'proxy_user': '',
 		//	        'proxy_password': '',
 		//	        'insecure': '0'
 		//	        }
 		defaultNames.add("hostname");
 		defaultNames.add("prefix");
 		defaultNames.add("port");
 		defaultNames.add("ca_cert_dir");
 		defaultNames.add("repo_ca_cert");
 		defaultNames.add("ssl_verify_depth");
 		defaultNames.add("proxy_hostname");
 		defaultNames.add("proxy_port");
 		defaultNames.add("proxy_user");
 		defaultNames.add("proxy_password");
 		defaultNames.add("insecure");
 		
 		return defaultNames;
 	}
 	
 	// environments module tasks ************************************************************
 
 	/**
 	 * environments without asserting results
 	 * @param username
 	 * @param password
 	 * @param org
 	 * @return
 	 */
 	public SSHCommandResult environments_(String username, String password, String org, String proxy, String proxyuser, String proxypassword) {
 
 		// assemble the command
 		String command = this.command;	command += " environments";
 		if (username!=null)				command += " --username="+username;
 		if (password!=null)				command += " --password="+password;
 		if (org!=null)					command += " --org="+org;
 		if (proxy!=null)				command += " --proxy="+proxy;
 		if (proxyuser!=null)			command += " --proxyuser="+proxyuser;
 		if (proxypassword!=null)		command += " --proxypassword="+proxypassword;
 		
 		// run command without asserting results
 		return sshCommandRunner.runCommandAndWait(command);
 	}
 	
 	/**
 	 * "subscription-manager environments"
 	 * @param username
 	 * @param password
 	 * @param org
 	 * @return
 	 */
 	public SSHCommandResult environments(String username, String password, String org, String proxy, String proxyuser, String proxypassword) {
 		
 		SSHCommandResult sshCommandResult = environments_(username, password, org, proxy, proxyuser, proxypassword);
 		
 		// TODO assert results...
 		
 		// assert the exit code was a success
 		Assert.assertEquals(sshCommandResult.getExitCode(), Integer.valueOf(0), "The exit code from the environments command indicates a success.");
 		
 		return sshCommandResult; // from the environments command
 	}
 	
 	
 	// unregister module tasks ************************************************************
 
 	/**
 	 * unregister without asserting results
 	 * @param proxy TODO
 	 * @param proxyuser TODO
 	 * @param proxypassword TODO
 	 */
 	public SSHCommandResult unregister_(String proxy, String proxyuser, String proxypassword) {
 
 		// assemble the command
 		String command = this.command;	command += " unregister";
 		if (proxy!=null)				command += " --proxy="+proxy;
 		if (proxyuser!=null)			command += " --proxyuser="+proxyuser;
 		if (proxypassword!=null)		command += " --proxypassword="+proxypassword;
 		
 		
 		// run command without asserting results
 		SSHCommandResult sshCommandResult = sshCommandRunner.runCommandAndWait(command);
 		
 		// reset this.currentlyRegistered values
 		if (sshCommandResult.getExitCode().equals(Integer.valueOf(0))) {			// success
 			this.currentlyRegisteredUsername = null;
 			this.currentlyRegisteredPassword = null;
 			this.currentlyRegisteredOrg = null;
 			this.currentlyRegisteredType = null;
 		} else if (sshCommandResult.getExitCode().equals(Integer.valueOf(1))) {		// already registered	
 		} else if (sshCommandResult.getExitCode().equals(Integer.valueOf(255))) {	// failure
 		}
 		
 		// return the results
 		return sshCommandResult;
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
 	 * @param ondate TODO
 	 * @param consumed TODO
 	 * @param installed TODO
 	 * @param proxy TODO
 	 * @param proxyuser TODO
 	 * @param proxypassword TODO
 	 */
 	public SSHCommandResult list_(Boolean all, Boolean available, String ondate, Boolean consumed, Boolean installed, String proxy, String proxyuser, String proxypassword) {
 
 		// assemble the command
 		String command = this.command;		command += " list";	
 		if (all!=null && all)				command += " --all";
 		if (available!=null && available)	command += " --available";
 		if (ondate!=null)					command += " --ondate="+ondate;
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
 		
 		SSHCommandResult sshCommandResult = list_(null,null,null,null, Boolean.TRUE, null, null, null);
 		
 		Assert.assertEquals(sshCommandResult.getExitCode(), Integer.valueOf(0), "The exit code from the list command indicates a success.");
 
 		if (getCurrentProductCertFiles().isEmpty() /*&& getCurrentEntitlementCertFiles().isEmpty() NOT NEEDED AFTER DESIGN CHANGE FROM BUG 736424*/) {
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
 
 		SSHCommandResult sshCommandResult = list_(null,Boolean.TRUE,null, null, null, null, null, null);
 		
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
 			return list_(Boolean.FALSE,Boolean.TRUE,null, null, null, null, null, null);
 		}
 		// END OF WORKAROUND
 		
 		SSHCommandResult sshCommandResult = list_(Boolean.TRUE,Boolean.TRUE,null, null, null, null, null, null);
 		
 		Assert.assertEquals(sshCommandResult.getExitCode(), Integer.valueOf(0), "The exit code from the list --all --available command indicates a success.");
 		//Assert.assertContainsMatch(sshCommandResult.getStdout(), "Available Subscriptions"); // produces too much logging
 
 		return sshCommandResult;
 		
 	}
 	
 	/**
 	 * @return SSHCommandResult from "subscription-manager-cli list --consumed"
 	 */
 	public SSHCommandResult listConsumedProductSubscriptions() {
 
 		SSHCommandResult sshCommandResult = list_(null,null,null, Boolean.TRUE, null, null, null, null);
 		
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
 	
 	
 
 	
 
 	
 	// redeem module tasks ************************************************************
 
 	/**
 	 * redeem without asserting results
 	 * @param email TODO
 	 * @param locale TODO
 	 * @param proxy TODO
 	 * @param proxyuser TODO
 	 * @param proxypassword TODO
 	 */
 	public SSHCommandResult redeem_(String email, String locale, String proxy, String proxyuser, String proxypassword) {
 		
 		// assemble the command
 		String command = this.command;	command += " redeem";
 		if (email!=null)				command += " --email="+email;
 		if (locale!=null)				command += " --locale="+locale;
 		if (proxy!=null)				command += " --proxy="+proxy;
 		if (proxyuser!=null)			command += " --proxyuser="+proxyuser;
 		if (proxypassword!=null)		command += " --proxypassword="+proxypassword;
 		
 		// run command without asserting results
 		return sshCommandRunner.runCommandAndWait(command);
 	}
 
 	public SSHCommandResult redeem(String email, String locale, String proxy, String proxyuser, String proxypassword) {
 
 		SSHCommandResult sshCommandResult = redeem_(email, locale, proxy, proxyuser, proxypassword);
 		
 		// TODO assert results...
 		
 		return sshCommandResult;
 	}
 	
 	
 	
 	// repos module tasks ************************************************************
 
 	/**
 	 * repos without asserting results
 	 * @param list TODO
 	 * @param proxy TODO
 	 * @param proxyuser TODO
 	 * @param proxypassword TODO
 	 */
 	public SSHCommandResult repos_(Boolean list, String proxy, String proxyuser, String proxypassword) {
 		
 		// assemble the command
 		String command = this.command;	command += " repos";
 		if (list!=null && list)			command += " --list";
 		if (proxy!=null)				command += " --proxy="+proxy;
 		if (proxyuser!=null)			command += " --proxyuser="+proxyuser;
 		if (proxypassword!=null)		command += " --proxypassword="+proxypassword;
 		
 		// run command without asserting results
 		return sshCommandRunner.runCommandAndWait(command);
 	}
 
 	public SSHCommandResult repos(Boolean list, String proxy, String proxyuser, String proxypassword) {
 
 		SSHCommandResult sshCommandResult = repos_(list, proxy, proxyuser, proxypassword);
 		
 		// TODO assert results...
 		Assert.assertEquals(sshCommandResult.getExitCode(), Integer.valueOf(0), "The exit code from the repos command indicates a success.");
 		
 		return sshCommandResult;
 	}
 	
 	
 	/**
 	 * @return SSHCommandResult from "subscription-manager repos --list"
 	 */
 	public SSHCommandResult listSubscribedRepos() {
 
 		Calendar now = new GregorianCalendar();
 		now.setTimeInMillis(System.currentTimeMillis());
 		
 		SSHCommandResult sshCommandResult = repos_(Boolean.TRUE, null, null, null);
 		Assert.assertEquals(sshCommandResult.getExitCode(), Integer.valueOf(0), "The exit code from the repos --list command indicates a success.");
 		
 		//List<File> entitlementCertFiles = getCurrentEntitlementCertFiles();
 		List<EntitlementCert> entitlementCerts = getCurrentEntitlementCerts();
 		int numContentNamespaces = 0;
 		for (EntitlementCert entitlementCert : entitlementCerts) {
 			
 			// we should NOT count contentNamespaces from entitlement certs that are not valid now
 			if (entitlementCert.validityNotBefore.after(now) || entitlementCert.validityNotAfter.before(now)) continue;
 
 			for (ContentNamespace contentNamespace : entitlementCert.contentNamespaces) {
 				numContentNamespaces++;
 			}
 		}
 
 		if (numContentNamespaces==0) {
 			Assert.assertTrue(sshCommandResult.getStdout().trim().equals("The system is not entitled to use any repositories."), "The system is not entitled to use any repositories.");
 		} else {
 			String title = "Entitled Repositories in "+redhatRepoFile;
 			Assert.assertTrue(sshCommandResult.getStdout().contains(title),"The list of repositories is entitled '"+title+"'.");
 		}
 
 		return sshCommandResult;
 	}
 	
 	
 	// subscribe module tasks ************************************************************
 
 	/**
 	 * subscribe WITHOUT asserting results
 	 */
 	public SSHCommandResult subscribe_(Boolean auto, List<String> poolIds, List<String> productIds, List<String> regtokens, String quantity, String email, String locale, String proxy, String proxyuser, String proxypassword) {
 		
 		// assemble the command
 		String command = this.command;									command += " subscribe";
 		if (auto!=null && auto)											command += " --auto";
 		if (poolIds!=null)		for (String poolId : poolIds)			command += " --pool="+poolId;
 		if (productIds!=null)	for (String productId : productIds)		command += " --product="+productId;
 		if (regtokens!=null)	for (String regtoken : regtokens)		command += " --regtoken="+regtoken;
 		if (quantity!=null)												command += " --quantity="+quantity;
 		if (email!=null)												command += " --email="+email;
 		if (locale!=null)												command += " --locale="+locale;
 		if (proxy!=null)												command += " --proxy="+proxy;
 		if (proxyuser!=null)											command += " --proxyuser="+proxyuser;
 		if (proxypassword!=null)										command += " --proxypassword="+proxypassword;
 		
 		// run command without asserting results
 		return sshCommandRunner.runCommandAndWait(command);
 	}
 
 	/**
 	 * subscribe WITHOUT asserting results.
 	 */
 	public SSHCommandResult subscribe_(Boolean auto, String poolId, String productId, String regtoken, String quantity, String email, String locale, String proxy, String proxyuser, String proxypassword) {
 		
 		List<String> poolIds	= poolId==null?null:Arrays.asList(new String[]{poolId});
 		List<String> productIds	= productId==null?null:Arrays.asList(new String[]{productId});
 		List<String> regtokens	= regtoken==null?null:Arrays.asList(new String[]{regtoken});
 
 		return subscribe_(auto, poolIds, productIds, regtokens, quantity, email, locale, proxy, proxyuser, proxypassword);
 	}
 
 
 	
 	/**
 	 * subscribe and assert all results are successful
 	 */
 	public SSHCommandResult subscribe(Boolean auto, List<String> poolIds, List<String> productIds, List<String> regtokens, String quantity, String email, String locale, String proxy, String proxyuser, String proxypassword) {
 
 		SSHCommandResult sshCommandResult = subscribe_(auto, poolIds, productIds, regtokens, quantity, email, locale, proxy, proxyuser, proxypassword);
 		auto = auto==null? false:auto;	// the non-null default value for auto is false
 
 		// assert results...
 		String stdoutMessage;
 		
 		// if already subscribed, just return the result
 		// This consumer is already subscribed to the product matching pool with id 'ff8080812c71f5ce012c71f6996f0132'.
 		if (sshCommandResult.getStdout().startsWith("This consumer is already subscribed")) return sshCommandResult;	
 
 		// if no free entitlements, just return the result
 		// No free entitlements are available for the pool with id 'ff8080812e16e00e012e16e1f6090134'.
 		if (sshCommandResult.getStdout().startsWith("No free entitlements are available")) return sshCommandResult;	
 		
 		// if rule failed, just return the result
 		// Unable to entitle consumer to the pool with id '8a90f8b42e3e7f2e012e3e7fc653013e'.: rulefailed.virt.only
 		if (sshCommandResult.getStdout().startsWith("Unable to entitle consumer")) return sshCommandResult;	
 		
 		// assert the subscribe does NOT report "The system is unable to complete the requested transaction"
 		//Assert.assertContainsNoMatch(sshCommandResult.getStdout(), "The system is unable to complete the requested transaction","The system should always be able to complete the requested transaction.");
 		stdoutMessage = "The system is unable to complete the requested transaction";
 		Assert.assertFalse(sshCommandResult.getStdout().contains(stdoutMessage), "The subscribe stdout should NOT report: "+stdoutMessage);
 	
 		// assert the subscribe does NOT report "Entitlement Certificate\\(s\\) update failed due to the following reasons:"
 		//Assert.assertContainsNoMatch(sshCommandResult.getStdout(), "Entitlement Certificate\\(s\\) update failed due to the following reasons:","Entitlement Certificate updates should be successful when subscribing.");
 		stdoutMessage = "Entitlement Certificate(s) update failed due to the following reasons:";
 		Assert.assertFalse(sshCommandResult.getStdout().contains(stdoutMessage), "The subscribe stdout should NOT report: "+stdoutMessage);
 
 		// assert that the entitlement pool was found for subscribing
 		//Assert.assertContainsNoMatch(sshCommandResult.getStdout(),"No such entitlement pool:", "The subscription pool was found.");
 		//Assert.assertContainsNoMatch(sshCommandResult.getStdout(), "Subscription pool .* does not exist.","The subscription pool was found.");
 		//stdoutMessage = "Subscription pool "+(poolId==null?"null":poolId)+" does not exist.";	// Subscription pool {0} does not exist.
 		//Assert.assertFalse(sshCommandResult.getStdout().contains(stdoutMessage), "The subscribe stdout should NOT report: "+stdoutMessage);
 		if (poolIds!=null) {
 			for (String poolId : poolIds) {
 				stdoutMessage = "Subscription pool "+poolId+" does not exist.";	// Subscription pool {0} does not exist.
 				Assert.assertFalse(sshCommandResult.getStdout().contains(stdoutMessage), "The subscribe stdout should NOT report: "+stdoutMessage);
 			}
 		}
 		
 		// assert the stdout msg was a success
 		if (auto)	Assert.assertTrue(sshCommandResult.getStdout().startsWith("Installed Product Current Status:"), "The autosubscribe stdout reports: Installed Product Current Status");
 		else		Assert.assertTrue(sshCommandResult.getStdout().startsWith("Success"), "The subscribe stdout reports: Success");
 
 		// assert the exit code was a success
 		Assert.assertEquals(sshCommandResult.getExitCode(), Integer.valueOf(0), "The exit code from the subscribe command indicates a success.");
 		
 		return sshCommandResult;
 	}
 	
 	/**
 	 * subscribe and assert all results are successful
 	 */
 	public SSHCommandResult subscribe(Boolean auto, String poolId, String productId, String regtoken, String quantity, String email, String locale, String proxy, String proxyuser, String proxypassword) {
 
 		List<String> poolIds	= poolId==null?null:Arrays.asList(new String[]{poolId});
 		List<String> productIds	= productId==null?null:Arrays.asList(new String[]{productId});
 		List<String> regtokens	= regtoken==null?null:Arrays.asList(new String[]{regtoken});
 
 		return subscribe(auto, poolIds, productIds, regtokens, quantity, email, locale, proxy, proxyuser, proxypassword);
 	}
 	
 	
 //	public SSHCommandResult subscribe(List<String> poolIds, List<String> productIds, List<String> regtokens, String quantity, String email, String locale, String proxy, String proxyuser, String proxypassword) {
 //
 //		SSHCommandResult sshCommandResult = subscribe_(null, poolIds, productIds, regtokens, quantity, email, locale, proxy, proxyuser, proxypassword);
 //		
 //		// assert results
 //		Assert.assertContainsNoMatch(sshCommandResult.getStdout(), "Entitlement Certificate\\(s\\) update failed due to the following reasons:","Entitlement Certificate updates should be successful when subscribing.");
 //		if (sshCommandResult.getStderr().startsWith("This consumer is already subscribed")) return sshCommandResult;
 //		Assert.assertEquals(sshCommandResult.getExitCode(), Integer.valueOf(0), "The exit code from the subscribe command indicates a success.");
 //		return sshCommandResult;
 //	}
 	
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
 		
 //		String hostname = getConfFileParameter(rhsmConfFile, "hostname");
 //		String port = getConfFileParameter(rhsmConfFile, "port");
 //		String prefix = getConfFileParameter(rhsmConfFile, "prefix");
 		
 		List<ProductSubscription> beforeProductSubscriptions = getCurrentlyConsumedProductSubscriptions();
 		List<File> beforeEntitlementCertFiles = getCurrentEntitlementCertFiles();
 		log.info("Subscribing to subscription pool: "+pool);
 		SSHCommandResult sshCommandResult = subscribe(null, pool.poolId, null, null, null, null, null, null, null, null);
 
 		// is this pool multi-entitleable?
 		boolean isPoolMultiEntitlement = false;
 		try {
 			isPoolMultiEntitlement = CandlepinTasks.isPoolProductMultiEntitlement(this.currentlyRegisteredUsername,this.currentlyRegisteredPassword,SubscriptionManagerBaseTestScript.sm_serverUrl,pool.poolId);
 		} catch (Exception e) {
 			e.printStackTrace();
 			Assert.fail(e.getMessage());
 		}
 
 		// assert that the remaining SubscriptionPools does NOT contain the pool just subscribed too (unless it is multi-entitleable)
 		List<SubscriptionPool> afterSubscriptionPools = getCurrentlyAvailableSubscriptionPools();
 		if (!isPoolMultiEntitlement || Integer.valueOf(pool.quantity)<=1) {
 			Assert.assertTrue(!afterSubscriptionPools.contains(pool),
 					"The available subscription pools no longer contains the just subscribed to pool: "+pool);
 		} else {
 			Assert.assertTrue(afterSubscriptionPools.contains(pool),
 					"When the pool is multi-entitleable, the available subscription pools still contains the just subscribed to pool: "+pool);
 		}
 		
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
 		/* OLD - THIS ALGORITHM BREAKS DOWN WHEN MODIFIER ENTITLEMENTS ARE IN PLAY
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
 		// NOTE: this block of code is somewhat duplicated in getEntitlementCertCorrespondingToSubscribedPool(...)
 		File newCertFile = null;
 		List<File> afterEntitlementCertFiles = getCurrentEntitlementCertFiles("-t");
 		for (File entitlementCertFile : afterEntitlementCertFiles) {
 			if (!beforeEntitlementCertFiles.contains(entitlementCertFile)) {
 				EntitlementCert entitlementCert = getEntitlementCertFromEntitlementCertFile(entitlementCertFile);
 				try {
 					JSONObject jsonEntitlement = CandlepinTasks.getEntitlementUsingRESTfulAPI(this.currentlyRegisteredUsername,this.currentlyRegisteredPassword,SubscriptionManagerBaseTestScript.sm_serverUrl,entitlementCert.id);
 					JSONObject jsonPool = new JSONObject(CandlepinTasks.getResourceUsingRESTfulAPI(this.currentlyRegisteredUsername,this.currentlyRegisteredPassword,SubscriptionManagerBaseTestScript.sm_serverUrl,jsonEntitlement.getJSONObject("pool").getString("href")));
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
 				jsonPool = new JSONObject(CandlepinTasks.getResourceUsingRESTfulAPI(this.currentlyRegisteredUsername,this.currentlyRegisteredPassword,SubscriptionManagerBaseTestScript.sm_serverUrl,"/pools/"+pool.poolId));
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
 				log.warning("Skipping assert that the productId from the pool matches the entitlement productId");
 			} else {
 			// END OF WORKAROUND
 			EntitlementCert entitlementCert = getEntitlementCertFromEntitlementCertFile(newCertFile);
 			File newCertKeyFile = getEntitlementCertKeyFileFromEntitlementCert(entitlementCert);
 			Assert.assertEquals(entitlementCert.orderNamespace.productId, poolProductId, isSubpool?
 					"New EntitlementCert productId '"+entitlementCert.orderNamespace.productId+"' matches originating Personal SubscriptionPool productId '"+poolProductId+"' after subscribing to the subpool.":
 					"New EntitlementCert productId '"+entitlementCert.orderNamespace.productId+"' matches originating SubscriptionPool productId '"+poolProductId+"' after subscribing to the pool.");
 			Assert.assertEquals(RemoteFileTasks.testFileExists(sshCommandRunner, newCertFile.getPath()), 1,"New EntitlementCert file exists after subscribing to SubscriptionPool '"+pool.poolId+"'.");
 			Assert.assertEquals(RemoteFileTasks.testFileExists(sshCommandRunner, newCertKeyFile.getPath()), 1,"New EntitlementCert key file exists after subscribing to SubscriptionPool '"+pool.poolId+"'.");
 			}
 
 		
 			// assert that consumed ProductSubscriptions has NOT decreased
 			List<ProductSubscription> afterProductSubscriptions = getCurrentlyConsumedProductSubscriptions();
 			Assert.assertTrue(afterProductSubscriptions.size() >= beforeProductSubscriptions.size() && afterProductSubscriptions.size() > 0,
 					"The list of currently consumed product subscriptions has increased (from "+beforeProductSubscriptions.size()+" to "+afterProductSubscriptions.size()+"), or has remained the same after subscribing (using poolID="+pool.poolId+") to pool: "+pool+"  Note: The list of consumed product subscriptions can remain the same when all the products from this subscription pool are a subset of those from a previously subscribed pool.");
 		}
 		
 		return newCertFile;
 	}
 	
 	/**
 	 * subscribe to the given SubscriptionPool without asserting results
 	 * @param pool
 	 * @return the newly installed EntitlementCert file to the newly consumed ProductSubscriptions (null if there was a problem)
 	 * @throws Exception 
 	 * @throws JSONException 
 	 */
 	public File subscribeToSubscriptionPool_(SubscriptionPool pool) throws JSONException, Exception  {
 		
 //		String hostname = getConfFileParameter(rhsmConfFile, "hostname");
 //		String port = getConfFileParameter(rhsmConfFile, "port");
 //		String prefix = getConfFileParameter(rhsmConfFile, "prefix");
 		String ownerKey = getCurrentlyRegisteredOwnerKey();
 		
 		log.info("Subscribing to subscription pool: "+pool);
 		SSHCommandResult sshCommandResult = subscribe(null, pool.poolId, null, null, null, null, null, null, null, null);
 
 		// get the serial of the entitlement that was granted from this pool
 		BigInteger serialNumber = CandlepinTasks.getEntitlementSerialForSubscribedPoolId(this.currentlyRegisteredUsername,this.currentlyRegisteredPassword,SubscriptionManagerBaseTestScript.sm_serverUrl,ownerKey,pool.poolId);
 		//Assert.assertNotNull(serialNumber, "Found the serial number of the entitlement that was granted after subscribing to pool id '"+pool.poolId+"'.");
 		if (serialNumber==null) return null;
 		File serialPemFile = new File(entitlementCertDir+File.separator+serialNumber+".pem");
 		//Assert.assertEquals(RemoteFileTasks.testFileExists(sshCommandRunner, serialPemFile.getPath()),1, "Found the EntitlementCert file ("+serialPemFile+") that was granted after subscribing to pool id '"+pool.poolId+"'.");
 
 		return serialPemFile;
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
 		if (stderr.equals("This consumer is already subscribed to the product '"+pool.productId+"'.")) {
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
 	 * @return SubscriptionPools that were available for subscribing 
 	 */
 	public List <SubscriptionPool> subscribeToTheCurrentlyAvailableSubscriptionPoolsIndividually() {
 
 		// individually subscribe to each available subscription pool
 		List <SubscriptionPool> pools = getCurrentlyAvailableSubscriptionPools();
 		for (SubscriptionPool pool : pools) {
 			subscribeToSubscriptionPool(pool);
 		}
 		
 		// assert
 		assertNoAvailableSubscriptionPoolsToList(true, "Asserting that no available subscription pools remain after individually subscribing to them all.");
 		return pools;
 	}
 	
 	
 	/**
 	 * Collectively subscribe to the currently available subscription pools in one command call
 	 * 
 	 * @return SubscriptionPools that were available for subscribing 
 	 * @throws Exception 
 	 * @throws JSONException 
 	 */
 	public List<SubscriptionPool> subscribeToTheCurrentlyAvailableSubscriptionPoolsCollectively() throws JSONException, Exception {
 		
 		// assemble a list of all the available SubscriptionPool ids
 		List <String> poolIds = new ArrayList<String>();
 		List <SubscriptionPool> poolsBeforeSubscribe = getCurrentlyAvailableSubscriptionPools();
 		for (SubscriptionPool pool : poolsBeforeSubscribe) {
 			poolIds.add(pool.poolId);
 		}
 		if (!poolIds.isEmpty()) subscribe(null,poolIds, null, null, null, null, null, null, null, null);
 		
 		// assert results when assumingRegisterType="system"
 		if (currentlyRegisteredType==null || currentlyRegisteredType.equals(ConsumerType.system)) {
 			assertNoAvailableSubscriptionPoolsToList(true, "Asserting that no available subscription pools remain after collectively subscribing to them all.");
 			return poolsBeforeSubscribe;
 		}
 		
 		// assert results when assumingRegisterType="candlepin"
 		else if (currentlyRegisteredType.equals(ConsumerType.candlepin)) {
 			List <SubscriptionPool> poolsAfterSubscribe = getCurrentlyAvailableSubscriptionPools();
 			for (SubscriptionPool beforePool : poolsBeforeSubscribe) {
 				boolean foundPool = false;
 				for (SubscriptionPool afterPool : poolsAfterSubscribe) {
 					if (afterPool.equals(beforePool)) {
 						foundPool = true;
 						
 						// determine how much the quantity should have decremented
 						int expectedDecrement = 1;
 						String virt_only = CandlepinTasks.getPoolAttributeValue(currentlyRegisteredUsername, currentlyRegisteredPassword, SubscriptionManagerBaseTestScript.sm_serverUrl, afterPool.poolId, "virt_only");
 						String virt_limit = CandlepinTasks.getPoolProductAttributeValue(currentlyRegisteredUsername, currentlyRegisteredPassword, SubscriptionManagerBaseTestScript.sm_serverUrl, afterPool.poolId, "virt_limit");
 						if (virt_only!=null && Boolean.valueOf(virt_only) && virt_limit!=null) expectedDecrement += Integer.valueOf(virt_limit);	// the quantity consumed on a virt pool should be 1 (from the subscribe on the virtual pool itself) plus virt_limit (from the subscribe by the candlepin consumer on the physical pool)
 
 						// assert the quantity has decremented;
 						Assert.assertEquals(Integer.valueOf(afterPool.quantity).intValue(), Integer.valueOf(beforePool.quantity).intValue()-expectedDecrement,
 								"The quantity of entitlements from subscription pool id '"+afterPool.poolId+"' has decremented by "+expectedDecrement+".");
 						break;
 					}
 				}
 				if (!foundPool) {
 					Assert.fail("Could not find subscription pool "+beforePool+" listed after subscribing to it as a registered "+currentlyRegisteredType+" consumer.");
 				}
 			}
 			return poolsBeforeSubscribe;
 		}
 		
 		Assert.fail("Do not know how to assert subscribeToTheCurrentlyAvailableSubscriptionPoolsCollectively when registered as type="+currentlyRegisteredType);
 		return poolsBeforeSubscribe;
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
 	
 	public void assertNoAvailableSubscriptionPoolsToList(boolean ignoreMuliEntitlementSubscriptionPools, String assertMsg) {
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
 		
 		
 		// determine which available pools are multi-entitlement pools
 		List<SubscriptionPool> poolsAvailableExcludingMuliEntitlement = new ArrayList<SubscriptionPool>();
 		List<SubscriptionPool> poolsAvailable = getCurrentlyAvailableSubscriptionPools();
 		for (SubscriptionPool pool : poolsAvailable) {
 			try {
 //				if (!CandlepinTasks.isPoolProductMultiEntitlement(getConfFileParameter(rhsmConfFile, "hostname"),getConfFileParameter(rhsmConfFile, "port"),getConfFileParameter(rhsmConfFile, "prefix"),this.currentlyRegisteredUsername,this.currentlyRegisteredPassword,pool.poolId)) {
 				if (!CandlepinTasks.isPoolProductMultiEntitlement(this.currentlyRegisteredUsername,this.currentlyRegisteredPassword,SubscriptionManagerBaseTestScript.sm_serverUrl,pool.poolId)) {
 					poolsAvailableExcludingMuliEntitlement.add(pool);
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 				Assert.fail(e.getMessage());
 			}
 		}
 		
 		// assert
 		if (ignoreMuliEntitlementSubscriptionPools) {
 			Assert.assertEquals(poolsAvailableExcludingMuliEntitlement.size(),0,
 					assertMsg+" (muti-entitlement pools were excluded.)");
 		} else {
 			Assert.assertEquals(poolsAvailable.size(),0,
 					assertMsg+" (muti-entitlement pools were excluded.)");
 			Assert.assertEquals(listAvailableSubscriptionPools().getStdout().trim(),
 				"No Available subscription pools to list",assertMsg);
 		}
 	}
 	
 	
 	
 	// unsubscribe module tasks ************************************************************
 
 	/**
 	 * unsubscribe without asserting results
 	 * @param proxy TODO
 	 * @param proxyuser TODO
 	 * @param proxypassword TODO
 	 */
 	public SSHCommandResult unsubscribe_(Boolean all, BigInteger serial, String proxy, String proxyuser, String proxypassword) {
 
 		// assemble the command
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
 		String certKeyFilePath = entitlementCertDir+"/"+serialNumber+"-key.pem";
 		File certFile = new File(certFilePath);
 		boolean certFileExists = RemoteFileTasks.testFileExists(sshCommandRunner,certFilePath)==1? true:false;
 		if (certFileExists) Assert.assertTrue(RemoteFileTasks.testFileExists(sshCommandRunner,certKeyFilePath)==1,
 				"Entitlement Certificate file with serial '"+serialNumber+"' ("+certFilePath+") and corresponding key file ("+certKeyFilePath+") exist before unsubscribing.");
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
 		
 		// assert the entitlement certFilePath is removed
 		Assert.assertTrue(RemoteFileTasks.testFileExists(sshCommandRunner,certFilePath)==0,
 				"Entitlement Certificate with serial '"+serialNumber+"' ("+certFilePath+") has been removed.");
 
 		// assert the entitlement certKeyFilePath is removed
 		// TEMPORARY WORKAROUND FOR BUG: https://bugzilla.redhat.com/show_bug.cgi?id=708362 - jsefler 08/25/2011
 		boolean invokeWorkaroundWhileBugIsOpen = true;
 		String bugId="708362"; 
 		try {if (invokeWorkaroundWhileBugIsOpen&&BzChecker.getInstance().isBugOpen(bugId)) {log.fine("Invoking workaround for "+BzChecker.getInstance().getBugState(bugId).toString()+" Bugzilla bug "+bugId+".  (https://bugzilla.redhat.com/show_bug.cgi?id="+bugId+")");} else {invokeWorkaroundWhileBugIsOpen=false;}} catch (XmlRpcException xre) {/* ignore exception */} catch (RuntimeException re) {/* ignore exception */}
 		boolean assertCertKeyFilePathIsRemoved = true;
 		if (invokeWorkaroundWhileBugIsOpen) log.warning("Skipping the assertion that the Entitlement Certificate key with serial '"+serialNumber+"' ("+certKeyFilePath+") has been removed while bug is open."); else
 		// END OF WORKAROUND
 		Assert.assertTrue(RemoteFileTasks.testFileExists(sshCommandRunner,certKeyFilePath)==0,
 				"Entitlement Certificate key with serial '"+serialNumber+"' ("+certKeyFilePath+") has been removed.");
 
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
 		Assert.assertTrue(sshCommandRunner.runCommandAndWait("find "+entitlementCertDir+" -name '*.pem' | grep -v key.pem").getStdout().equals(""),
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
 
 		// assemble the command
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
 		if (list!=null && list)		regex=".*:.*";						// list of the current facts
 		if (update!=null && update)	regex="Successfully updated the system facts\\.";	// regex=getCurrentConsumerCert().consumerid;	// consumerid	// RHEL57 RHEL61
 
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
 //			for (ProductNamespace productNamespace : productCert.productNamespaces) {
 //				if (productNamespace.providedTags!=null) {
 //					for (String providedTag : productNamespace.providedTags.split("\\s*,\\s*")) {
 //						providedTags.add(providedTag);
 //					}
 //				}
 //			}
 			if (productCert.productNamespace.providedTags!=null) {
 				for (String providedTag : productCert.productNamespace.providedTags.split("\\s*,\\s*")) {
 					providedTags.add(providedTag);
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
 		
 		List<ProductCert> currentProductCerts = this.getCurrentProductCerts();
 		
 		// NOTE: THIS COULD ALSO BE A PERMANENT IMPLEMENTATION FOR THIS METHOD
 		// TEMPORARY WORKAROUND FOR BUG: https://bugzilla.redhat.com/show_bug.cgi?id=697087 - jsefler 04/27/2011
 		if (this.redhatRelease.contains("release 5")) {
 			boolean invokeWorkaroundWhileBugIsOpen = true;
 			String bugId="697087"; 
 			try {if (invokeWorkaroundWhileBugIsOpen/*&&BzChecker.getInstance().isBugOpen(bugId)*/) {log.fine("Invoking workaround for "+BzChecker.getInstance().getBugState(bugId).toString()+" Bugzilla bug "+bugId+".  (https://bugzilla.redhat.com/show_bug.cgi?id="+bugId+")");} else {invokeWorkaroundWhileBugIsOpen=false;}} catch (XmlRpcException xre) {/* ignore exception */} catch (RuntimeException re) {/* ignore exception */}
 			if (invokeWorkaroundWhileBugIsOpen) {
 				
 				List<String> yumRepoListAll			= this.getYumRepolist("all");
 				List<String> yumRepoListEnabled		= this.getYumRepolist("enabled");
 				List<String> yumRepoListDisabled	= this.getYumRepolist("disabled");
 				
 		 		for (EntitlementCert entitlementCert : entitlementCerts) {
 		 			for (ContentNamespace contentNamespace : entitlementCert.contentNamespaces) {
 		 				if (areReported && areAllRequiredTagsInContentNamespaceProvidedByProductCerts(contentNamespace,currentProductCerts)) {
 							if (contentNamespace.enabled.equals("1")) {
 								Assert.assertTrue(yumRepoListEnabled.contains(contentNamespace.label),
 										"Yum repolist enabled includes repo id/label '"+contentNamespace.label+"' that comes from entitlement cert "+entitlementCert.id+"'s content namespace: "+contentNamespace);
 							} else if (contentNamespace.enabled.equals("0")) {
 								Assert.assertTrue(yumRepoListDisabled.contains(contentNamespace.label),
 										"Yum repolist disabled includes repo id/label '"+contentNamespace.label+"' that comes from entitlement cert "+entitlementCert.id+"'s content namespace: "+contentNamespace);
 							} else {
 								Assert.fail("Encountered entitlement cert '"+entitlementCert.id+"' whose content namespace has an unexpected enabled field: "+contentNamespace);
 							}
 		 				}
 						else
 							Assert.assertFalse(yumRepoListAll.contains(contentNamespace.label),
 									"Yum repolist all excludes repo id/label '"+contentNamespace.label+"'.");
 			 		}
 		 		}
 		 		return;
 			}
 		}
 		// END OF WORKAROUND
 		
 		
 				
 				
 		// assert all of the entitlement certs are reported in the stdout from "yum repolist all"
 		sshCommandRunner.runCommandAndWait("killall -9 yum");
 		SSHCommandResult result = sshCommandRunner.runCommandAndWait("yum repolist all --disableplugin=rhnplugin");	// FIXME, THIS SHOULD MAKE USE OF getYumRepolist
  		for (EntitlementCert entitlementCert : entitlementCerts) {
  			for (ContentNamespace contentNamespace : entitlementCert.contentNamespaces) {
 
  				// Note: When the repo id and repo name are really long, the repo name in the yum repolist all gets crushed (hence the reason for .* in the regex)
 				String regex = String.format("^%s\\s+(?:%s|.*)\\s+%s", contentNamespace.label.trim(), contentNamespace.name.substring(0,Math.min(contentNamespace.name.length(), 25)), contentNamespace.enabled.equals("1")? "enabled:":"disabled$");	// 25 was arbitraily picked to be short enough to be displayed by yum repolist all
 //				if (areReported)	// before development of conditional content tagging
 				if (areReported && areAllRequiredTagsInContentNamespaceProvidedByProductCerts(contentNamespace,currentProductCerts))
 					Assert.assertContainsMatch(result.getStdout(), regex, null, "ContentNamespace label '"+contentNamespace.label.trim()+"' from EntitlementCert '"+entitlementCert.serialNumber+"' is reported in yum repolist all.");
 				else
 					Assert.assertContainsNoMatch(result.getStdout(), regex, null, "ContentNamespace label '"+contentNamespace.label.trim()+"' from EntitlementCert '"+entitlementCert.serialNumber+"' is NOT reported in yum repolist all.");
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
 				
 		// TEMPORARY WORKAROUND FOR BUG: https://bugzilla.redhat.com/show_bug.cgi?id=697087 - jsefler 04/27/2011
 		if (this.redhatRelease.contains("release 5")) {
 			boolean invokeWorkaroundWhileBugIsOpen = true;
 			String bugId="697087"; 
 			try {if (invokeWorkaroundWhileBugIsOpen/*&&BzChecker.getInstance().isBugOpen(bugId)*/) {log.fine("Invoking workaround for "+BzChecker.getInstance().getBugState(bugId).toString()+" Bugzilla bug "+bugId+".  (https://bugzilla.redhat.com/show_bug.cgi?id="+bugId+")");} else {invokeWorkaroundWhileBugIsOpen=false;}} catch (XmlRpcException xre) {/* ignore exception */} catch (RuntimeException re) {/* ignore exception */}
 			if (invokeWorkaroundWhileBugIsOpen) {
 				
 				// avoid "yum repolist" and assemble the list of repos directly from the redhat repo file
 				List<YumRepo> yumRepoList =   getCurrentlySubscribedYumRepos();
 				for (YumRepo yumRepo : yumRepoList) {
 					if		(options.equals("all"))													repos.add(yumRepo.id);
 					else if (options.equals("enabled")	&& yumRepo.enabled.equals(Boolean.TRUE))	repos.add(yumRepo.id);
 					else if (options.equals("disabled")	&& yumRepo.enabled.equals(Boolean.FALSE))	repos.add(yumRepo.id);
 					else if (options.equals("")			&& yumRepo.enabled.equals(Boolean.TRUE))	repos.add(yumRepo.id);
 				}
 				sshCommandRunner.runCommandAndWait("yum repolist "+options+" --disableplugin=rhnplugin"); // --disableplugin=rhnplugin helps avoid: up2date_client.up2dateErrors.AbuseError
 				return repos;
 			}
 		}
 		// END OF WORKAROUND
 		
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
 	
 	
 	@Deprecated	// replaced by public ArrayList<String> getYumListAvailable (String options)
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
 		//  Loaded plugins: product-id, refresh-packagekit, subscription-manager
 		//  No plugin match for: rhnplugin
 		//  Updating certificate-based repositories.
 		//  Available Packages
 		//	xmltex.noarch                             20020625-16.el6                      red-hat-enterprise-linux-6-entitlement-alpha-rpms
 		//	xmlto.x86_64                              0.0.23-3.el6                         red-hat-enterprise-linux-6-entitlement-alpha-rpms
 		//	xmlto-tex.noarch                          0.0.23-3.el6                         red-hat-enterprise-linux-6-entitlement-alpha-rpms
 		//	xorg-x11-apps.x86_64                      7.4-10.el6                           red-hat-enterprise-linux-6-entitlement-alpha-rpms
 		//if (enablerepo==null||enablerepo.equals("*")) enablerepo="(\\S+)";
 		//String regex="^(\\S+) +(\\S+) +"+enablerepo+"$";
 		String regex="^(\\S+) +(\\S+) +(\\S+)$";	// assume all the packages are on a line with three words
 		Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
 		String stdout = result.getStdout().replaceAll("Updating certificate-based repositories.", "").replaceAll("Loaded plugins:", "Loaded list of yum plugins:");	// strip these messages from stdout since they could break the three word regex assumption for packages.
 		Matcher matcher = pattern.matcher(stdout);
 		if (!matcher.find()) {
 			log.info("Did NOT find any available packages from: "+command);
 			return packages;
 		}
 
 		// assemble the list of packages and return them
 		do {
 			packages.add(matcher.group(1)); // group(1) is the pkg,  group(2) is the version,  group(3) is the repo
 		} while (matcher.find());
 		
 		// flip the packages since the ones at the end of the list are usually easier to install 
 		ArrayList<String> packagesCloned = (ArrayList<String>) packages.clone(); packages.clear();
 		for (int p=packagesCloned.size()-1; p>=0; p--) packages.add(packagesCloned.get(p));
 
 		return packages;
 	}
 	
 	/**
 	 * Disable all of the repos in /etc/yum.repos.d
 	 * NOTE: On RHEL5, yum-utils must be installed first.
 	 */
 	public void yumDisableAllRepos() {
 		for (String repo : getYumRepolist("enabled")) {
 			String command = "yum-config-manager --disable "+repo;
 			SSHCommandResult result = sshCommandRunner.runCommandAndWait(command);
 			Assert.assertEquals(result.getExitCode(), Integer.valueOf(0), "ExitCode from command '"+command+"'.");
 		}
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
 	
 	
 	/**
 	 * Find an available package for install that is unique to the specified repo label.
 	 * @param repo
 	 * @return
 	 * Note: You should consider calling yumDisableAllRepos() before using this method especially when this client was provisioned by Beaker.
 	 */
 	public String findUniqueAvailablePackageFromRepo (String repo) {
 		for (String pkg : getYumListAvailable("--disablerepo=* --enablerepo="+repo)) {
 			if (!getYumListAvailable("--disablerepo="+repo+" "+pkg).contains(pkg)) {
 				if (yumCanInstallPackageFromRepo(pkg,repo,null)) {
 					return pkg;
 				}
 			}
 		}
 		return null;
 	}
 	
 	public String findRandomAvailablePackageFromRepo (String repo) {
 		ArrayList<String> pkgs = getYumListAvailable("--disablerepo=* --enablerepo="+repo);
 		if (pkgs.isEmpty()) return null;
 		return pkgs.get(SubscriptionManagerCLITestScript.randomGenerator.nextInt(pkgs.size()));
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
 	
 	/**
 	 * @param pkg
 	 * @param repoLabel
 	 * @param installOptions
 	 * @return true - when pkg can be cleanly installed from repolLabel with installOptions. <br>
 	 *         false - when the user is not prompted with "Is this ok [y/N]:" to Complete! the install
 	 */
 	public boolean yumCanInstallPackageFromRepo (String pkg, String repoLabel, String installOptions) {
 		
 		// attempt to install the pkg from repo with the installOptions, but say N at the prompt: Is this ok [y/N]: N
 		if (installOptions==null) installOptions=""; installOptions = installOptions.replaceFirst("-y", "");
 		String command = "echo N | yum install "+pkg+" --enablerepo="+repoLabel+" --disableplugin=rhnplugin "+installOptions; // --disableplugin=rhnplugin helps avoid: up2date_client.up2dateErrors.AbuseError
 		SSHCommandResult result = RemoteFileTasks.runCommandAndAssert(sshCommandRunner,command, 1);
 
 		// disregard the package if it was obsoleted...
 		
 		//	Loaded plugins: product-id, refresh-packagekit, security, subscription-manager
 		//	No plugin match for: rhnplugin
 		//	Updating certificate-based repositories.
 		//	Setting up Install Process
 		//	Package gfs-pcmk is obsoleted by cman, trying to install cman-3.0.12.1-21.el6.x86_64 instead
 		//	Resolving Dependencies
 		//	--> Running transaction check
 		//	---> Package cman.x86_64 0:3.0.12.1-21.el6 will be installed
 		String regex="Package "+pkg.split("\\.")[0]+".* is obsoleted by (.+), trying to install .+ instead";
 		Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
 		Matcher matcher = pattern.matcher(sshCommandRunner.getStdout());
 		String obsoletedByPkg = null;
 		if (matcher.find()) {
 			obsoletedByPkg = matcher.group(1);
 			// can the obsoletedByPkg be installed from repoLabel instead? 
 			//return yumCanInstallPackageFromRepo (obsoletedByPkg, repoLabel, installOptions);
 			log.fine("Disregarding package '"+pkg+"' as installable from repo '"+repoLabel+"' because it has been obsoleted.");
 			return false;
 		}
 		
 		//	Total download size: 2.1 M
 		//	Installed size: 4.8 M
 		//	Is this ok [y/N]: N
 		//	Exiting on user Command
 		return result.getStdout().contains("Is this ok [y/N]:");
 	}
 	
 	// 
 	/**
 	 * @param pkg
 	 * @param repoLabel
 	 * @param destdir
 	 * @param downloadOptions
 	 * @return the actual downloaded package File (null if there was an error)
 	 * TODO: on RHEL5, the yum-utils package must be installed first to get yumdownloader
 	 */
 	public File yumDownloadPackageFromRepo (String pkg, String repoLabel, String destdir, String downloadOptions) {
 		
 		// use yumdownloader the package with repoLabel enabled
 		if (downloadOptions==null) downloadOptions=""; //downloadOptions += " -y";
 		String command = "yumdownloader "+pkg+" --destdir="+destdir+" --disablerepo=* --enablerepo="+repoLabel+" --noplugins "+downloadOptions; // --disableplugin=rhnplugin helps avoid: up2date_client.up2dateErrors.AbuseError
 		//SSHCommandResult result = RemoteFileTasks.runCommandAndAssert(sshCommandRunner,command, 0, "^Complete!$",null);
 		SSHCommandResult result = sshCommandRunner.runCommandAndWait(command+"; "+command); // the second command is needed to populate stdout
 		Assert.assertTrue(!result.getStderr().toLowerCase().contains("error"), "Stderr from command '"+command+"' did not report an error.");
 		Assert.assertEquals(result.getExitCode(), Integer.valueOf(0), "ExitCode from command '"+command+"'.");
 
 		//[root@jsefler-stage-6server ~]# yumdownloader --disablerepo=* ricci.x86_64 --enablerepo=rhel-ha-for-rhel-6-server-rpms --destdir /tmp
 		//Loaded plugins: product-id, refresh-packagekit
 		//ricci-0.16.2-35.el6_1.1.x86_64.rpm                                                                                                                                     | 614 kB     00:00     
 		//[root@jsefler-stage-6server ~]# yumdownloader --disablerepo=* ricci.x86_64 --enablerepo=rhel-ha-for-rhel-6-server-rpms --destdir /tmp
 		//Loaded plugins: product-id, refresh-packagekit
 		///tmp/ricci-0.16.2-35.el6_1.1.x86_64.rpm already exists and appears to be complete
 		//[root@jsefler-stage-6server ~]# 
 		
 		// extract the name of the downloaded pkg
		// [/\w\.-]*\.rpm
		String regex = "([/\\w\\.-]*\\.rpm)";	
 		Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
 		Matcher matcher = pattern.matcher(result.getStdout());
 		if (!matcher.find()) {
 			log.warning("Did not find the name of the downloaded pkg using regex '"+regex+"'.");
 			return null;
 		}
 		String rpm = matcher.group(1).trim();	// return the contents of the first capturing group
 		
 		//File pkgFile = new File(destdir+File.separatorChar+rpm);
 		File pkgFile = new File(rpm);
 		
 		// assert the downloaded file exists
 		Assert.assertEquals(RemoteFileTasks.testFileExists(sshCommandRunner,pkgFile.getPath()),1,"Package '"+pkg+"' exists in destdir '"+destdir+"' after yumdownloading.");
 		
 		return pkgFile;
 	}
 	
 	public SSHCommandResult yumInstallPackageFromRepo (String pkg, String repoLabel, String installOptions) {
 		
 		// install the package with repoLabel enabled
 		if (installOptions==null) installOptions=""; installOptions += " -y";
 		String command = "yum install "+pkg+" --enablerepo="+repoLabel+" --disableplugin=rhnplugin "+installOptions; // --disableplugin=rhnplugin helps avoid: up2date_client.up2dateErrors.AbuseError
 		//SSHCommandResult result = RemoteFileTasks.runCommandAndAssert(sshCommandRunner,command, 0, "^Complete!$",null);
 		SSHCommandResult result = sshCommandRunner.runCommandAndWait(command);
 		Assert.assertTrue(!result.getStderr().toLowerCase().contains("error"), "Stderr from command '"+command+"' did not report an error.");
 		Assert.assertTrue(result.getStdout().contains("\nComplete!"), "Stdout from command '"+command+"' reported a successful \"Complete!\".");
 		Assert.assertEquals(result.getExitCode(), Integer.valueOf(0), "ExitCode from command '"+command+"'.");
 		
 		//	201104051837:12.757 - FINE: ssh root@jsefler-betastage-server.usersys.redhat.com yum -y install cairo-spice-debuginfo.x86_64 --enablerepo=rhel-6-server-beta-debug-rpms --disableplugin=rhnplugin (com.redhat.qe.tools.SSHCommandRunner.run)
 		//	201104051837:18.156 - FINE: Stdout: 
 		//	Loaded plugins: product-id, refresh-packagekit, subscription-manager
 		//	No plugin match for: rhnplugin
 		//	Updating Red Hat repositories.
 		//	Setting up Install Process
 		//	Package cairo-spice-debuginfo is obsoleted by spice-server, trying to install spice-server-0.7.3-2.el6.x86_64 instead
 		//	Resolving Dependencies
 		//	--> Running transaction check
 		//	---> Package spice-server.x86_64 0:0.7.3-2.el6 will be installed
 		//	--> Finished Dependency Resolution
 		//
 		//	Dependencies Resolved
 		//
 		//	================================================================================
 		//	 Package          Arch       Version          Repository                   Size
 		//	================================================================================
 		//	Installing:
 		//	 spice-server     x86_64     0.7.3-2.el6      rhel-6-server-beta-rpms     245 k
 		//
 		//	Transaction Summary
 		//	================================================================================
 		//	Install       1 Package(s)
 		//
 		//	Total download size: 245 k
 		//	Installed size: 913 k
 		//	Downloading Packages:
 		//	Running rpm_check_debug
 		//	Running Transaction Test
 		//	Transaction Test Succeeded
 		//	Running Transaction
 		//
 		//	  Installing : spice-server-0.7.3-2.el6.x86_64                              1/1 
 		//	duration: 205(ms)
 		//
 		//	Installed:
 		//	  spice-server.x86_64 0:0.7.3-2.el6                                             
 		//
 		//	Complete!
 		//	 (com.redhat.qe.tools.SSHCommandRunner.runCommandAndWait)
 		//	201104051837:18.180 - FINE: Stderr: 
 		//	INFO:rhsm-app.repolib:repos updated: 63
 		//	Installed products updated.
 		//	 (com.redhat.qe.tools.SSHCommandRunner.runCommandAndWait)
 		//	201104051837:18.182 - FINE: ExitCode: 0 (com.redhat.qe.tools.SSHCommandRunner.runCommandAndWait)
 
 		// EXAMPLE FROM RHEL62
 		//	ssh root@tyan-gt24-03.rhts.eng.bos.redhat.com yum install gfs-pcmk.x86_64 --enablerepo=rhel-rs-for-rhel-6-server-rpms --disableplugin=rhnplugin -y
 		//	Stdout:
 		//	Loaded plugins: product-id, refresh-packagekit, security, subscription-manager
 		//	No plugin match for: rhnplugin
 		//	Updating certificate-based repositories.
 		//	Setting up Install Process
 		//	Package gfs-pcmk is obsoleted by cman, trying to install cman-3.0.12.1-19.el6.x86_64 instead
 		//	Resolving Dependencies
 		//	--> Running transaction check
 		//	---> Package cman.x86_64 0:3.0.12.1-19.el6 will be installed
 		//	--> Processing Dependency: clusterlib = 3.0.12.1-19.el6 for package: cman-3.0.12.1-19.el6.x86_64
 		//	--> Processing Dependency: modcluster >= 0.15.0-3 for package: cman-3.0.12.1-19.el6.x86_64
 		//	--> Processing Dependency: fence-virt >= 0.2.3-1 for package: cman-3.0.12.1-19.el6.x86_64
 		//	--> Processing Dependency: fence-agents >= 3.1.5-1 for package: cman-3.0.12.1-19.el6.x86_64
 		//	--> Processing Dependency: openais >= 1.1.1-1 for package: cman-3.0.12.1-19.el6.x86_64
 		//	--> Processing Dependency: ricci >= 0.15.0-4 for package: cman-3.0.12.1-19.el6.x86_64
 		//	--> Processing Dependency: corosync >= 1.4.1-3 for package: cman-3.0.12.1-19.el6.x86_64
 		//	--> Processing Dependency: libcpg.so.4(COROSYNC_CPG_1.0)(64bit) for package: cman-3.0.12.1-19.el6.x86_64
 		//	--> Processing Dependency: libconfdb.so.4(COROSYNC_CONFDB_1.0)(64bit) for package: cman-3.0.12.1-19.el6.x86_64
 		//	--> Processing Dependency: libSaCkpt.so.3(OPENAIS_CKPT_B.01.01)(64bit) for package: cman-3.0.12.1-19.el6.x86_64
 		//	--> Processing Dependency: libcman.so.3()(64bit) for package: cman-3.0.12.1-19.el6.x86_64
 		//	--> Processing Dependency: libfenced.so.3()(64bit) for package: cman-3.0.12.1-19.el6.x86_64
 		//	--> Processing Dependency: liblogthread.so.3()(64bit) for package: cman-3.0.12.1-19.el6.x86_64
 		//	--> Processing Dependency: libdlm.so.3()(64bit) for package: cman-3.0.12.1-19.el6.x86_64
 		//	--> Processing Dependency: libfence.so.4()(64bit) for package: cman-3.0.12.1-19.el6.x86_64
 		//	--> Processing Dependency: libccs.so.3()(64bit) for package: cman-3.0.12.1-19.el6.x86_64
 		//	--> Processing Dependency: libcpg.so.4()(64bit) for package: cman-3.0.12.1-19.el6.x86_64
 		//	--> Processing Dependency: libconfdb.so.4()(64bit) for package: cman-3.0.12.1-19.el6.x86_64
 		//	--> Processing Dependency: libdlmcontrol.so.3()(64bit) for package: cman-3.0.12.1-19.el6.x86_64
 		//	--> Processing Dependency: libSaCkpt.so.3()(64bit) for package: cman-3.0.12.1-19.el6.x86_64
 		//	--> Running transaction check
 		//	---> Package clusterlib.x86_64 0:3.0.12.1-19.el6 will be installed
 		//	---> Package corosync.x86_64 0:1.4.1-3.el6 will be installed
 		//	--> Processing Dependency: libnetsnmp.so.20()(64bit) for package: corosync-1.4.1-3.el6.x86_64
 		//	---> Package corosynclib.x86_64 0:1.4.1-3.el6 will be installed
 		//	--> Processing Dependency: librdmacm.so.1(RDMACM_1.0)(64bit) for package: corosynclib-1.4.1-3.el6.x86_64
 		//	--> Processing Dependency: libibverbs.so.1(IBVERBS_1.0)(64bit) for package: corosynclib-1.4.1-3.el6.x86_64
 		//	--> Processing Dependency: libibverbs.so.1(IBVERBS_1.1)(64bit) for package: corosynclib-1.4.1-3.el6.x86_64
 		//	--> Processing Dependency: libibverbs.so.1()(64bit) for package: corosynclib-1.4.1-3.el6.x86_64
 		//	--> Processing Dependency: librdmacm.so.1()(64bit) for package: corosynclib-1.4.1-3.el6.x86_64
 		//	---> Package fence-agents.x86_64 0:3.1.5-9.el6 will be installed
 		//	--> Processing Dependency: perl(Net::Telnet) for package: fence-agents-3.1.5-9.el6.x86_64
 		//	--> Processing Dependency: /usr/bin/ipmitool for package: fence-agents-3.1.5-9.el6.x86_64
 		//	--> Processing Dependency: perl-Net-Telnet for package: fence-agents-3.1.5-9.el6.x86_64
 		//	--> Processing Dependency: pexpect for package: fence-agents-3.1.5-9.el6.x86_64
 		//	--> Processing Dependency: python-suds for package: fence-agents-3.1.5-9.el6.x86_64
 		//	--> Processing Dependency: telnet for package: fence-agents-3.1.5-9.el6.x86_64
 		//	--> Processing Dependency: net-snmp-utils for package: fence-agents-3.1.5-9.el6.x86_64
 		//	--> Processing Dependency: sg3_utils for package: fence-agents-3.1.5-9.el6.x86_64
 		//	---> Package fence-virt.x86_64 0:0.2.3-4.el6 will be installed
 		//	---> Package modcluster.x86_64 0:0.16.2-13.el6 will be installed
 		//	--> Processing Dependency: oddjob for package: modcluster-0.16.2-13.el6.x86_64
 		//	---> Package openais.x86_64 0:1.1.1-7.el6 will be installed
 		//	---> Package openaislib.x86_64 0:1.1.1-7.el6 will be installed
 		//	---> Package ricci.x86_64 0:0.16.2-42.el6 will be installed
 		//	--> Processing Dependency: nss-tools for package: ricci-0.16.2-42.el6.x86_64
 		//	--> Running transaction check
 		//	---> Package ipmitool.x86_64 0:1.8.11-12.el6 will be installed
 		//	---> Package libibverbs.x86_64 0:1.1.5-3.el6 will be installed
 		//	---> Package librdmacm.x86_64 0:1.0.14.1-3.el6 will be installed
 		//	---> Package net-snmp-libs.x86_64 1:5.5-37.el6 will be installed
 		//	--> Processing Dependency: libsensors.so.4()(64bit) for package: 1:net-snmp-libs-5.5-37.el6.x86_64
 		//	---> Package net-snmp-utils.x86_64 1:5.5-37.el6 will be installed
 		//	---> Package nss-tools.x86_64 0:3.12.10-4.el6 will be installed
 		//	---> Package oddjob.x86_64 0:0.30-5.el6 will be installed
 		//	---> Package perl-Net-Telnet.noarch 0:3.03-11.el6 will be installed
 		//	---> Package pexpect.noarch 0:2.3-6.el6 will be installed
 		//	---> Package python-suds.noarch 0:0.4.1-3.el6 will be installed
 		//	---> Package sg3_utils.x86_64 0:1.28-4.el6 will be installed
 		//	---> Package telnet.x86_64 1:0.17-47.el6 will be installed
 		//	--> Running transaction check
 		//	---> Package lm_sensors-libs.x86_64 0:3.1.1-10.el6 will be installed
 		//	--> Finished Dependency Resolution
 		//	
 		//	Dependencies Resolved
 		//	
 		//	================================================================================
 		//	Package Arch Version Repository Size
 		//	================================================================================
 		//	Installing:
 		//	cman x86_64 3.0.12.1-19.el6 beaker-HighAvailability 427 k
 		//	Installing for dependencies:
 		//	clusterlib x86_64 3.0.12.1-19.el6 beaker-HighAvailability 92 k
 		//	corosync x86_64 1.4.1-3.el6 beaker-HighAvailability 185 k
 		//	corosynclib x86_64 1.4.1-3.el6 beaker-HighAvailability 169 k
 		//	fence-agents x86_64 3.1.5-9.el6 beaker-HighAvailability 147 k
 		//	fence-virt x86_64 0.2.3-4.el6 beaker-HighAvailability 34 k
 		//	ipmitool x86_64 1.8.11-12.el6 beaker-Server 323 k
 		//	libibverbs x86_64 1.1.5-3.el6 beaker-Server 43 k
 		//	librdmacm x86_64 1.0.14.1-3.el6 beaker-Server 26 k
 		//	lm_sensors-libs x86_64 3.1.1-10.el6 beaker-Server 36 k
 		//	modcluster x86_64 0.16.2-13.el6 beaker-HighAvailability 184 k
 		//	net-snmp-libs x86_64 1:5.5-37.el6 beaker-Server 1.5 M
 		//	net-snmp-utils x86_64 1:5.5-37.el6 beaker-Server 168 k
 		//	nss-tools x86_64 3.12.10-4.el6 beaker-Server 747 k
 		//	oddjob x86_64 0.30-5.el6 beaker-Server 59 k
 		//	openais x86_64 1.1.1-7.el6 beaker-HighAvailability 191 k
 		//	openaislib x86_64 1.1.1-7.el6 beaker-HighAvailability 81 k
 		//	perl-Net-Telnet noarch 3.03-11.el6 beaker-HighAvailability 54 k
 		//	pexpect noarch 2.3-6.el6 beaker-Server 146 k
 		//	python-suds noarch 0.4.1-3.el6 beaker-HighAvailability 217 k
 		//	ricci x86_64 0.16.2-42.el6 beaker-HighAvailability 614 k
 		//	sg3_utils x86_64 1.28-4.el6 beaker-Server 470 k
 		//	telnet x86_64 1:0.17-47.el6 beaker-Server 57 k
 		//	
 		//	Transaction Summary
 		//	================================================================================
 		//	Install 23 Package(s)
 		//	
 		//	Total download size: 5.9 M
 		//	Installed size: 19 M
 		//	Downloading Packages:
 		//	--------------------------------------------------------------------------------
 		//	Total 8.2 MB/s | 5.9 MB 00:00
 		//	Running rpm_check_debug
 		//	Running Transaction Test
 		//	Transaction Test Succeeded
 		//	Running Transaction
 		//	
 		//	Installing : libibverbs-1.1.5-3.el6.x86_64 1/23
 		//	
 		//	Installing : oddjob-0.30-5.el6.x86_64 2/23
 		//	
 		//	Installing : librdmacm-1.0.14.1-3.el6.x86_64 3/23
 		//	
 		//	Installing : fence-virt-0.2.3-4.el6.x86_64 4/23
 		//	
 		//	Installing : lm_sensors-libs-3.1.1-10.el6.x86_64 5/23
 		//	
 		//	Installing : 1:net-snmp-libs-5.5-37.el6.x86_64 6/23
 		//	
 		//	Installing : corosync-1.4.1-3.el6.x86_64 7/23
 		//	
 		//	Installing : corosynclib-1.4.1-3.el6.x86_64 8/23
 		//	
 		//	Installing : openais-1.1.1-7.el6.x86_64 9/23
 		//	
 		//	Installing : openaislib-1.1.1-7.el6.x86_64 10/23
 		//	
 		//	Installing : clusterlib-3.0.12.1-19.el6.x86_64 11/23
 		//	
 		//	Installing : modcluster-0.16.2-13.el6.x86_64 12/23
 		//	
 		//	Installing : 1:net-snmp-utils-5.5-37.el6.x86_64 13/23
 		//	
 		//	Installing : pexpect-2.3-6.el6.noarch 14/23
 		//	
 		//	Installing : perl-Net-Telnet-3.03-11.el6.noarch 15/23
 		//	
 		//	Installing : 1:telnet-0.17-47.el6.x86_64 16/23
 		//	
 		//	Installing : python-suds-0.4.1-3.el6.noarch 17/23
 		//	
 		//	Installing : nss-tools-3.12.10-4.el6.x86_64 18/23
 		//	
 		//	Installing : ricci-0.16.2-42.el6.x86_64 19/23
 		//	
 		//	Installing : sg3_utils-1.28-4.el6.x86_64 20/23
 		//	
 		//	Installing : ipmitool-1.8.11-12.el6.x86_64 21/23
 		//	
 		//	Installing : fence-agents-3.1.5-9.el6.x86_64 22/23
 		//	Stopping kdump:[ OK ]
 		//	Starting kdump:[ OK ]
 		//	
 		//	Installing : cman-3.0.12.1-19.el6.x86_64 23/23
 		//	
 		//	Installed:
 		//	cman.x86_64 0:3.0.12.1-19.el6
 		//	
 		//	Dependency Installed:
 		//	clusterlib.x86_64 0:3.0.12.1-19.el6 corosync.x86_64 0:1.4.1-3.el6
 		//	corosynclib.x86_64 0:1.4.1-3.el6 fence-agents.x86_64 0:3.1.5-9.el6
 		//	fence-virt.x86_64 0:0.2.3-4.el6 ipmitool.x86_64 0:1.8.11-12.el6
 		//	libibverbs.x86_64 0:1.1.5-3.el6 librdmacm.x86_64 0:1.0.14.1-3.el6
 		//	lm_sensors-libs.x86_64 0:3.1.1-10.el6 modcluster.x86_64 0:0.16.2-13.el6
 		//	net-snmp-libs.x86_64 1:5.5-37.el6 net-snmp-utils.x86_64 1:5.5-37.el6
 		//	nss-tools.x86_64 0:3.12.10-4.el6 oddjob.x86_64 0:0.30-5.el6
 		//	openais.x86_64 0:1.1.1-7.el6 openaislib.x86_64 0:1.1.1-7.el6
 		//	perl-Net-Telnet.noarch 0:3.03-11.el6 pexpect.noarch 0:2.3-6.el6
 		//	python-suds.noarch 0:0.4.1-3.el6 ricci.x86_64 0:0.16.2-42.el6
 		//	sg3_utils.x86_64 0:1.28-4.el6 telnet.x86_64 1:0.17-47.el6
 		//	
 		//	Complete!
 		//	Stderr: Installed products updated.
 		//	ExitCode: 0
 
 		//	201111171056:22.839 - FINE: ssh root@jsefler-stage-6server.usersys.redhat.com echo N | yum install ricci-debuginfo.x86_64 --enablerepo=rhel-ha-for-rhel-6-server-htb-debug-rpms --disableplugin=rhnplugin  (com.redhat.qe.tools.SSHCommandRunner.run)
 		//	201111171056:24.774 - FINE: Stdout: 
 		//	Loaded plugins: product-id, refresh-packagekit, subscription-manager
 		//	No plugin match for: rhnplugin
 		//	Updating certificate-based repositories.
 		//	Setting up Install Process
 		//	Resolving Dependencies
 		//	--> Running transaction check
 		//	---> Package ricci-debuginfo.x86_64 0:0.16.2-35.el6 will be installed
 		//	--> Finished Dependency Resolution
 		//	
 		//	Dependencies Resolved
 		//	
 		//	================================================================================
 		//	 Package    Arch   Version       Repository                                Size
 		//	================================================================================
 		//	Installing:
 		//	 ricci-debuginfo
 		//	            x86_64 0.16.2-35.el6 rhel-ha-for-rhel-6-server-htb-debug-rpms 4.4 M
 		//	
 		//	Transaction Summary
 		//	================================================================================
 		//	Install       1 Package(s)
 		//	
 		//	Total download size: 4.4 M
 		//	Installed size: 27 M
 		//	Is this ok [y/N]: Exiting on user Command
 		//	Complete!
 		//	 (com.redhat.qe.tools.SSHCommandRunner.runCommandAndWait)
 		//	201111171056:24.775 - FINE: Stderr:  (com.redhat.qe.tools.SSHCommandRunner.runCommandAndWait)
 		//	201111171056:24.775 - FINE: ExitCode: 1 (com.redhat.qe.tools.SSHCommandRunner.runCommandAndWait)
 		//	201111171056:24.775 - INFO: Asserted: 1 is present in the list [1] (com.redhat.qe.auto.testng.Assert.pass)
 		//	201111171056:28.183 - FINE: ssh root@jsefler-stage-6server.usersys.redhat.com yum install ricci-debuginfo.x86_64 --enablerepo=rhel-ha-for-rhel-6-server-htb-debug-rpms --disableplugin=rhnplugin  -y (com.redhat.qe.tools.SSHCommandRunner.run)
 		//	201111171056:30.752 - FINE: Stdout: 
 		//	Loaded plugins: product-id, refresh-packagekit, subscription-manager
 		//	No plugin match for: rhnplugin
 		//	Updating certificate-based repositories.
 		//	Setting up Install Process
 		//	Resolving Dependencies
 		//	--> Running transaction check
 		//	---> Package ricci-debuginfo.x86_64 0:0.16.2-35.el6 will be installed
 		//	--> Finished Dependency Resolution
 		//	
 		//	Dependencies Resolved
 		//	
 		//	================================================================================
 		//	 Package    Arch   Version       Repository                                Size
 		//	================================================================================
 		//	Installing:
 		//	 ricci-debuginfo
 		//	            x86_64 0.16.2-35.el6 rhel-ha-for-rhel-6-server-htb-debug-rpms 4.4 M
 		//	
 		//	Transaction Summary
 		//	================================================================================
 		//	Install       1 Package(s)
 		//	
 		//	Total download size: 4.4 M
 		//	Installed size: 27 M
 		//	Downloading Packages:
 		//	 (com.redhat.qe.tools.SSHCommandRunner.runCommandAndWait)
 		//	201111171056:30.767 - FINE: Stderr: 
 		//	https://cdn.redhat.com/content/htb/rhel/server/6/6Server/x86_64/highavailability/debug/Packages/ricci-debuginfo-0.16.2-35.el6.x86_64.rpm: [Errno 14] PYCURL ERROR 22 - "The requested URL returned error: 404"
 		//	Trying other mirror.
 		//	
 		//	
 		//	Error Downloading Packages:
 		//	  ricci-debuginfo-0.16.2-35.el6.x86_64: failure: Packages/ricci-debuginfo-0.16.2-35.el6.x86_64.rpm from rhel-ha-for-rhel-6-server-htb-debug-rpms: [Errno 256] No more mirrors to try.
 		//	
 		//	 (com.redhat.qe.tools.SSHCommandRunner.runCommandAndWait)
 		//	201111171056:30.775 - FINE: ExitCode: 1 (com.redhat.qe.tools.SSHCommandRunner.runCommandAndWait)
 
 		// check if the package was obsoleted:
 		// Package cairo-spice-debuginfo is obsoleted by spice-server, trying to install spice-server-0.7.3-2.el6.x86_64 instead
 		String regex="Package "+pkg.split("\\.")[0]+".* is obsoleted by (.+), trying to install .+ instead";
 		Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
 		Matcher matcher = pattern.matcher(sshCommandRunner.getStdout());
 		String obsoletedByPkg = null;
 		if (matcher.find()) {
 			obsoletedByPkg = matcher.group(1);
 			log.warning("Package '"+pkg+"' was obsoleted by '"+obsoletedByPkg+"'. The replacement package may NOT get installed from repository '"+repoLabel+"'.");
 			pkg = obsoletedByPkg;
 		}
 		
 		// FIXME, If the package is obsoleted, then the obsoletedByPkg may not come from the same repo and the following assert will fail
 		
 		// assert the installed package came from repoLabel
 		//	spice-server     x86_64     0.7.3-2.el6      rhel-6-server-beta-rpms     245 k
 		//  cman x86_64 3.0.12.1-19.el6 beaker-HighAvailability 427 k
 		regex=pkg.split("\\.")[0]+"\\n? +(\\w*) +([\\w\\.-]*) +"+repoLabel;
 		pattern = Pattern.compile(regex, Pattern.MULTILINE);
 		matcher = pattern.matcher(sshCommandRunner.getStdout());
 		Assert.assertTrue(matcher.find(), "Package '"+pkg+"' appears to have been installed from repository '"+repoLabel+"'.");
 		String arch = matcher.group(1);
 		String version = matcher.group(2);
 		
 
 		// finally assert that the package is actually installed
 		//
 		// RHEL 5...
 		//	201106061840:40.270 - FINE: ssh root@jsefler-stage-5server.usersys.redhat.com yum list installed GConf2-debuginfo.x86_64 --disableplugin=rhnplugin (com.redhat.qe.tools.SSHCommandRunner.run)
 		//	201106061840:41.529 - FINE: Stdout: 
 		//	Loaded plugins: product-id, security, subscription-manager
 		//	No plugin match for: rhnplugin
 		//	Updating Red Hat repositories.
 		//	Installed Packages
 		//	GConf2-debuginfo.x86_64                  2.14.0-9.el5                  installed
 		//	 (com.redhat.qe.tools.SSHCommandRunner.runCommandAndWait)
 		//	201106061840:41.530 - FINE: Stderr:  (com.redhat.qe.tools.SSHCommandRunner.runCommandAndWait)
 		//	201106061840:41.530 - FINE: ExitCode: 0 (com.redhat.qe.tools.SSHCommandRunner.runCommandAndWait)
 		//
 		// RHEL 6...
 		//	201104051839:15.836 - FINE: ssh root@jsefler-betastage-server.usersys.redhat.com yum list installed spice-server --disableplugin=rhnplugin (com.redhat.qe.tools.SSHCommandRunner.run)
 		//	201104051839:16.447 - FINE: Stdout: 
 		//	Loaded plugins: product-id, refresh-packagekit, subscription-manager
 		//	No plugin match for: rhnplugin
 		//	Updating Red Hat repositories.
 		//	Installed Packages
 		//	spice-server.x86_64             0.7.3-2.el6             @rhel-6-server-beta-rpms
 		//	 (com.redhat.qe.tools.SSHCommandRunner.runCommandAndWait)
 		//	201104051839:16.453 - FINE: Stderr: INFO:rhsm-app.repolib:repos updated: 63	 (com.redhat.qe.tools.SSHCommandRunner.runCommandAndWait)
 		//	201104051839:16.455 - FINE: ExitCode: 0 (com.redhat.qe.tools.SSHCommandRunner.runCommandAndWait)
 		RemoteFileTasks.runCommandAndAssert(sshCommandRunner,"yum list installed "+pkg+" --disableplugin=rhnplugin", 0, "^"+pkg.split("\\.")[0]+"."+arch+" +"+version+" +(installed|@"+repoLabel+")$",null);
 		
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
 	
 	/**
 	 * @param key - e.g. "REGISTERED_TO_OTHER_WARNING"
 	 * @return the branding string value for the key
 	 */
 	public String getBrandingString(String key) {
 
 		// view /usr/share/rhsm/subscription_manager/branding/__init__.py and search for "self." to find branding message strings e.g. "REGISTERED_TO_OTHER_WARNING"
 		return sshCommandRunner.runCommandAndWait("cd "+brandingDir+"; python -c \"import __init__ as sm;brand=sm.Branding(sm.get_branding());print brand."+key+"\"").getStdout();
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
