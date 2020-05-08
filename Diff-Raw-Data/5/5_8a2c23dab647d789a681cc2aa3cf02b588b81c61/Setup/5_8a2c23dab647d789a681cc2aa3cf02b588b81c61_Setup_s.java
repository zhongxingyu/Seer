 package com.redhat.qe.sm.tests;
 
 import java.io.IOException;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Random;
 import java.util.logging.Level;
 
 import org.testng.annotations.BeforeSuite;
 import org.testng.annotations.Test;
 
 import com.redhat.qe.tools.RemoteFileTasks;
 import com.redhat.qe.tools.SCPTools;
 import com.redhat.qe.tools.SSHCommandRunner;
 
 import com.redhat.qe.auto.testng.TestScript;
 import com.redhat.qe.auto.testopia.Assert;
 import com.redhat.qe.sm.tasks.Pool;
 import com.redhat.qe.tools.SSHCommandRunner;
 
 public class Setup extends TestScript{
 	protected static final String defaultAutomationPropertiesFile=System.getenv("HOME")+"/sm-tests.properties";
 	public static final String RHSM_LOC = "/usr/sbin/subscription-manager-cli ";
 	
 	String clientHostname				= System.getProperty("rhsm.client.hostname");
 	String serverHostname				= System.getProperty("rhsm.server.hostname");
 	String username						= System.getProperty("rhsm.client.username");
 	String password						= System.getProperty("rhsm.client.password");
 	String regtoken						= System.getProperty("rhsm.client.regtoken");
 	String certFrequency				= System.getProperty("rhsm.client.certfrequency");
 	String rpmLocation					= System.getProperty("rhsm.rpm");
 	String serverPort 					= System.getProperty("rhsm.server.port");
 	String serverBaseUrl				= System.getProperty("rhsm.server.baseurl");
 	String clientsshKeyPrivate			= System.getProperty("rhsm.sshkey.private",".ssh/id_auto_dsa");
 	String clientsshKeyPath				= System.getProperty("automation.dir")+"/"+clientsshKeyPrivate;
 	String clientsshUser				= System.getProperty("rhsm.ssh.user","root");
 	String clientsshkeyPassphrase		= System.getProperty("rhsm.sshkey.passphrase","");
 	
 	String defaultConfigFile			= "/etc/rhsm/rhsm.conf";
 	String rhsmcertdLogFile				= "/var/log/rhsm/rhsmcertd.log";
 	String rhsmYumRepoFile				= "/etc/yum/pluginconf.d/rhsmplugin.conf";
 	
 	ArrayList<Pool> availSubscriptions = new ArrayList<Pool>();
 	ArrayList<Pool> consumedSubscriptions = new ArrayList<Pool>();
 	
 	public static SSHCommandRunner sshCommandRunner = null;
 
 	public void refreshSubscriptions(){
 		availSubscriptions.clear();
 		consumedSubscriptions.clear();
 		
 		log.info("Refreshing subscription information...");
 		sshCommandRunner.runCommandAndWait(RHSM_LOC + "list --available");
 		String[] availSubs = sshCommandRunner.getStdout().split("\\n");
 			
 			//SSHCommandRunner.executeViaSSHWithReturn(clientHostname, "root",
 				//RHSM_LOC + "list --available")
 		
 		//if extraneous output comes out over stdout, figure out where the useful output begins
 		int outputBegin = 2;
 		for(int i=0;i<availSubs.length;i++){
 			if(availSubs[i].contains("productId"))
 				break;
 			outputBegin++;
 		}
 		
 		for(int i=outputBegin;i<availSubs.length;i++)
 			try {
 				availSubscriptions.add(new Pool(availSubs[i].trim()));
 			} catch (ParseException e) {
 				e.printStackTrace();
 				log.warning("Unparseable subscription line: "+ availSubs[i]);
 			}
 		
		String[] consumedSubs = SSHCommandRunner.executeViaSSHWithReturn(clientHostname, "root",
				RHSM_LOC + "list --consumed")[0].split("\\n");
 		
 		//if extraneous output comes out over stdout, figure out where the useful output begins
 		outputBegin = 2;
 		for(int i=0;i<consumedSubs.length;i++){
 			if(consumedSubs[i].contains("productId"))
 				break;
 			outputBegin++;
 		}
 		
 		for(int i=outputBegin;i<consumedSubs.length;i++)
 			try {
 				consumedSubscriptions.add(new Pool(consumedSubs[i].trim()));
 			} catch (ParseException e) {
 				log.warning("Unparseable subscription line: "+ availSubs[i]);
 			}
 	}
 	
 	public ArrayList<Pool> getNonSubscribedSubscriptions(){
 		ArrayList<Pool> nsSubs = new ArrayList<Pool>();
 		for(Pool s:availSubscriptions)
 			if (!consumedSubscriptions.contains(s))
 				nsSubs.add(s);
 		return nsSubs;
 	}
 	
 	public void installLatestSMRPM(){
 		log.info("Retrieving latest subscription-manager RPM...");
 		sshCommandRunner.runCommandAndWait("rm -f ~/subscription-manager.rpm");
 		sshCommandRunner.runCommandAndWait("wget -O ~/subscription-manager.rpm --no-check-certificate "+rpmLocation);
 		
 		log.info("Installing newest subscription-manager RPM...");
 		sshCommandRunner.runCommandAndWait("rpm -e subscription-manager");
 		sshCommandRunner.runCommandAndWait("rpm --force --nodeps -Uvh ~/subscription-manager.rpm");
 	}
 	
 	public void updateSMConfigFile(String hostname, String port){
 		Assert.assertEquals(
 				RemoteFileTasks.searchReplaceFile(sshCommandRunner, 
 						defaultConfigFile, 
 						"^hostname=.*$", 
 						"hostname="+hostname),
 						0,
 						"Updated rhsm config hostname to point to:" + hostname
 				);
 		Assert.assertEquals(
 				RemoteFileTasks.searchReplaceFile(sshCommandRunner, 
 						defaultConfigFile, 
 						"^port=.*$", 
 						"port="+port),
 						0,
 						"Updated rhsm config port to point to:" + port
 				);
 	}
 	
 	public void changeCertFrequency(String frequency){
 		Assert.assertEquals(
 				RemoteFileTasks.searchReplaceFile(sshCommandRunner, 
 						defaultConfigFile, 
 						"^certFrequency=.*$", 
 						"certFrequency="+frequency),
 						0,
 						"Updated rhsmd cert refresh frequency to "+frequency+" minutes"
 				);
 		sshCommandRunner.runCommandAndWait("mv "+rhsmcertdLogFile+" "+rhsmcertdLogFile+".bak");
 		sshCommandRunner.runCommandAndWait("service rhsmcertd restart");
 		Assert.assertEquals(RemoteFileTasks.grepFile(sshCommandRunner,
 				rhsmcertdLogFile,
 				"started: interval = "+frequency),
 				0,
 				"interval reported as "+frequency+" in "+rhsmcertdLogFile);
 	}
 	
 	public void registerToCandlepin(String username, String password){
 		sshCommandRunner.runCommandAndWait(RHSM_LOC +
 				"register --username="+username+" --password="+password);
 		Assert.assertEquals(
 				sshCommandRunner.runCommandAndWait(
 						"stat /etc/pki/consumer/key.pem").intValue(),
 						0,
 						"/etc/pki/consumer/key.pem is present after register");
 		Assert.assertEquals(
 				sshCommandRunner.runCommandAndWait(
 						"stat /etc/pki/consumer/cert.pem").intValue(),
 						0,
 						"/etc/pki/consumer/cert.pem is present after register");
 		/*Assert.assertEquals(
 				sshCommandRunner.runCommandAndWait(
 						"stat /etc/pki/consumer/cert.uuid").intValue(),
 						0,
 						"/etc/pki/consumer/cert.uuid is present after register");*/
 	}
 	
 	public void subscribeToPool(Pool pool, boolean withPoolID){
 		if(withPoolID){
 			log.info("Subscribing to pool with pool ID:"+ pool.productId);
 			sshCommandRunner.runCommandAndWait(RHSM_LOC +
 					"subscribe --pool="+pool.poolId);
 		}
 		else{
 			log.info("Subscribing to pool with productID:"+ pool.productId);
 			sshCommandRunner.runCommandAndWait(RHSM_LOC +
 					"subscribe --product="+pool.productId);
 		}
 		this.refreshSubscriptions();
 		Assert.assertTrue(consumedSubscriptions.contains(pool), "Successfully subscribed to pool with pool ID: "+
 				pool.poolId + " and productID: "+ pool.productId);
 	}
 	
 	public void subscribeToRegToken(String regtoken){
 		log.info("Subscribing to registration token: "+ regtoken);
 		sshCommandRunner.runCommandAndWait(RHSM_LOC +
 				"subscribe --regtoken="+regtoken);
 	}
 	
 	public void unsubscribeFromPool(Pool pool, boolean withPoolID){
 		if(withPoolID){
 			log.info("Unsubscribing from pool with pool ID:"+ pool.poolId);
 			sshCommandRunner.runCommandAndWait(RHSM_LOC +
 					"unsubscribe --pool="+pool.poolId);
 		}
 		else{
 			log.info("Unsubscribing from pool with productID:"+ pool.productId);
 			sshCommandRunner.runCommandAndWait(RHSM_LOC +
 					"unsubscribe --product="+pool.productId);
 		}
 		this.refreshSubscriptions();
 		Assert.assertFalse(consumedSubscriptions.contains(pool),
 				"Successfully unsubscribed from pool with poolID: "+pool.poolId+
 				"and productID: "+ pool.productId);
 	}
 	
 	public void cleanOutAllCerts(){
 		log.info("Cleaning out certs from /etc/pki/consumer, /etc/pki/entitlement/, and /etc/pki/product/");
 		sshCommandRunner.runCommandAndWait("rm -f /etc/pki/consumer/*");
 		sshCommandRunner.runCommandAndWait("rm -rf /etc/pki/entitlement/*");
 		sshCommandRunner.runCommandAndWait("rm -rf /etc/pki/product/*");
 	}
 	
 	public void sleep(long i) {
 		log.info("Sleeping for "+i+" milliseconds...");
 		try {
 			Thread.sleep(i);
 		} catch (InterruptedException e) {
 			log.info("Sleep interrupted!");
 		}
 	}
 	
 	public int getRandInt(){
 		Random gen = new Random();
 		return gen.nextInt();
 	}
 	
 	public ArrayList<String> getYumRepolist(){
 		ArrayList<String> repos = new ArrayList<String>();
 		String[] availRepos = SSHCommandRunner.executeViaSSHWithReturn(clientHostname, "root",
 				"yum repolist")[0].split("\\n");
 		
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
 	
 	public void adjustRHSMYumRepo(boolean enabled){
 		Assert.assertEquals(
 				RemoteFileTasks.searchReplaceFile(sshCommandRunner, 
 						rhsmYumRepoFile, 
 						"^enabled=.*$", 
 						"enabled="+(enabled?'1':'0')),
 						0,
 						"Adjusted RHSM Yum Repo config file, enabled="+(enabled?'1':'0')
 				);
 	}
 	
 	public void unsubscribeFromAllSubscriptions(boolean withPoolID){
 		log.info("Unsubscribing from all subscriptions"+
 				(withPoolID?" (using pool ID)...":"..."));
 		this.refreshSubscriptions();
 		for(Pool sub:this.consumedSubscriptions)
 			this.unsubscribeFromPool(sub, withPoolID);
 		Assert.assertEquals(this.consumedSubscriptions.size(),
 				0,
 				"Asserting that all subscriptions are now unsubscribed");
 		log.info("Verifying that product certificates are no longer present...");
 		RemoteFileTasks.runCommandExpectingNonzeroExit(sshCommandRunner,
 				"ls /etc/pki/entitlement/product/ | grep pem");
 	}
 	
 	public void subscribeToAllSubscriptions(boolean withPoolID){
 		log.info("Subscribing to all subscriptions"+
 				(withPoolID?" (using pool ID)...":"..."));
 		this.refreshSubscriptions();
 		for (Pool sub:this.availSubscriptions)
 			this.subscribeToPool(sub, withPoolID);
 		Assert.assertEquals(this.getNonSubscribedSubscriptions().size(),
 				0,
 				"Asserting that all available subscriptions are now subscribed");
 	}
 	
 	@BeforeSuite(groups={"sm_setup"},description="subscription manager set up",alwaysRun=true)
 	public void setupSM() throws ParseException, IOException{
 		sshCommandRunner = new SSHCommandRunner(clientHostname, 
 				clientsshUser, clientsshKeyPath, clientsshkeyPassphrase, null);
 		this.installLatestSMRPM();
 		this.cleanOutAllCerts();
 		this.updateSMConfigFile(serverHostname, serverPort);
 		this.changeCertFrequency(certFrequency);
 	}
 }
