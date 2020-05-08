 package it.polimi.elet.selflet.istantiator;
 
 import it.polimi.elet.amazon.AmazonFrontend;
 import it.polimi.elet.selflet.configuration.DispatcherConfiguration;
 import it.polimi.elet.selflet.id.ISelfLetID;
 import it.polimi.elet.selflet.id.SelfLetID;
 import it.polimi.elet.selflet.schema.SchemaLoader;
 import it.polimi.elet.selflet.ssh.SSHConnection;
 
 import java.io.InputStream;
 import java.util.Set;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import org.apache.log4j.Logger;
 
 import com.amazonaws.auth.AWSCredentials;
 import com.google.common.collect.ImmutableSet;
 
 /**
  * An implementation of selflet istantiator
  * 
  * @author Nicola Calcavecchia <calcavecchia@gmail.com>
  * */
 public class SelfletIstantiator implements ISelfletIstantiator {
 
 	private static final Logger LOG = Logger.getLogger(SelfletIstantiator.class);
 
 	private static final String USERNAME = DispatcherConfiguration.username;
 	private static final String PASSWORD = DispatcherConfiguration.password;
 	private static final int PORT_NUMBER = 22;
 
 	private static final String SETUP_SELFLET_SH = "setupSelflet.sh";
 	private static final String MAVEN_LIFECYCLE_SH = "mavenLifeCyle.sh";
 	private static final String START_SELFLET_SH = "start_selflet.sh";
 	private static final String KILLSELFLET_SH = "kill_selflet.sh";
 	private static final String START_BROKER_AND_DISPATCHER_SH = "start_broker_and_dispatcher.sh";
 	private static final String CREATE_AWS_CREDENTIALS_FILE = "create_aws_credentials_file.sh";
 
 	private static final String LOCAL_SHELL_SCRIPT_FOLDER = "/shell_scripts/";
 	private static final String REMOTE_FOLDER_FOR_SELFLET = "selflet";
 
 	private static Integer initialNumberOfSelflets = DispatcherConfiguration.initialNumberOfSelflets;
 
 	private final IVirtualMachineIPManager virtualMachineIPGenerator = VirtualMachineIPManager.getInstance();
 
 	private static ISelfletIstantiator instance;
 
 	private static final int THREAD_POOL_SIZE = 30;
 	private static ExecutorService THREAD_POOL = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
 
 	private SelfletIstantiator() {
 		// private constructor
 	}
 
 	public static synchronized ISelfletIstantiator getInstance() {
 		if (instance == null) {
 			instance = new SelfletIstantiator();
 		}
 		return instance;
 	}
 
 	@Override
 	public void resetAllInstances() {
 		Set<String> allIPs = virtualMachineIPGenerator.getAllIPAddresses();
 		// resetSelflets(allIPs);
 		for (String ip : allIPs) {
 			THREAD_POOL.submit(new ResetThread(ip));
 		}
 	}
 
 	private void resetMachine(String ipAddress) {
 		removeBaseFolder(ipAddress);
 		copyDataToVM(ipAddress);
 		executeSetup(ipAddress);
 		createCredentialFile(ipAddress);
 	}
 
 	private void removeBaseFolder(String ipAddress) {
 		SSHConnection sshConnection = createNewSSHConnection(ipAddress);
 		sshConnection.executeWithoutOutput("rm -rf " + REMOTE_FOLDER_FOR_SELFLET);
 	}
 
 	@Override
 	public String istantiateBrokerAndDispatcher() {
 		if (virtualMachineIPGenerator.isDispatcherSet()) {
 			LOG.debug("Dispatcher is already set");
 			return virtualMachineIPGenerator.getDispatcherIpAddress();
 		}
 		LOG.debug("Istantiating new broker and dispatcher");
 		// resetSelflets(virtualMachineIPGenerator.getAllIPAddresses());
 		String ipAddress = virtualMachineIPGenerator.getNewIpAddress();
 		virtualMachineIPGenerator.setDispatcherIpAddress(ipAddress);
 		createCredentialFile(ipAddress);
 		startBrokerAndDispatcher(ipAddress);
 		return ipAddress;
 	}
 
 	private void createCredentialFile(String ipAddress) {
 		SSHConnection sshConnection = createNewSSHConnection(ipAddress);
 		AWSCredentials credentials = AmazonFrontend.getInstance().getAWSCredentials();
 		String arguments = credentials.getAWSAccessKeyId() + " " + credentials.getAWSSecretKey();
 		sshConnection.executeWithoutOutput("cd " + REMOTE_FOLDER_FOR_SELFLET + " ; source "
 				+ CREATE_AWS_CREDENTIALS_FILE + " " + arguments);
 	}
 
 	private void startBrokerAndDispatcher(String ipAddress) {
 		SSHConnection sshConnection = createNewSSHConnection(ipAddress);
 		sshConnection.executeWithoutOutput("cd " + REMOTE_FOLDER_FOR_SELFLET + " ; source "
 				+ START_BROKER_AND_DISPATCHER_SH);
 	}
 
 	public AllocatedSelflet istantiateNewSelflet(String template) {
 		LOG.debug("Istantiating new selflet");
 		String ipAddress = virtualMachineIPGenerator.getNewIpAddress();
 		ISelfLetID newSelfletID = getNewSelfletID();
 		istantiateNewSelflet(ipAddress, newSelfletID, template);
 		virtualMachineIPGenerator.setVmToSelfletBinding(ipAddress, newSelfletID);
 		LOG.debug("Selflet " + newSelfletID + " istantiated with ip " + ipAddress);
 
 		AllocatedSelflet allocatedSelflet = new AllocatedSelflet(ipAddress, newSelfletID);
 		return allocatedSelflet;
 	}
 
 	private void copyDataToVM(String ipAddress) {
 
 		createRemoteFolder(ipAddress);
 
 		ImmutableSet<String> files = ImmutableSet.of(SETUP_SELFLET_SH, MAVEN_LIFECYCLE_SH, START_SELFLET_SH,
 				KILLSELFLET_SH, START_BROKER_AND_DISPATCHER_SH, CREATE_AWS_CREDENTIALS_FILE);
 
 		for (String fileName : files) {
 			copySetupFiles(fileName, ipAddress);
 		}
 
 	}
 
 	private void istantiateNewSelflet(String ipAddress, ISelfLetID newSelfletID, String template) {
		copyDataToVM(ipAddress);
		executeSetup(ipAddress);
 		startSelflet(ipAddress, newSelfletID, template);
 	}
 
 	private void createRemoteFolder(String ipAddress) {
 		SSHConnection sshConnection = createNewSSHConnection(ipAddress);
 		sshConnection.execute("mkdir " + REMOTE_FOLDER_FOR_SELFLET);
 	}
 
 	private void startSelflet(String ipAddress, ISelfLetID newSelfletID, String template) {
 		LOG.debug("Starting selflet");
 		SSHConnection sshConnection = createNewSSHConnection(ipAddress);
 		String brokerAddress = virtualMachineIPGenerator.getDispatcherIpAddress();
 		sshConnection.executeWithoutOutput("cd " + REMOTE_FOLDER_FOR_SELFLET + " ; source " + START_SELFLET_SH + " "
 				+ newSelfletID.getID() + " " + brokerAddress + ":" + DispatcherConfiguration.redsPort + " " + template);
 	}
 
 	private void executeSetup(String ipAddress) {
 		SSHConnection sshConnection = createNewSSHConnection(ipAddress);
 		sshConnection.execute("cd " + REMOTE_FOLDER_FOR_SELFLET + " ; source " + SETUP_SELFLET_SH);
 	}
 
 	private void copySetupFiles(String file, String ipAddress) {
 		LOG.debug("Copying " + file + "...");
 		copyFile(file, ipAddress);
 	}
 
 	private void copyFile(String fileName, String ipAddress) {
 		SSHConnection sshConnection = createNewSSHConnection(ipAddress);
 
 		String fullPath = LOCAL_SHELL_SCRIPT_FOLDER + fileName;
 		InputStream stream = SchemaLoader.class.getResourceAsStream(fullPath);
 		String remoteFolder = REMOTE_FOLDER_FOR_SELFLET + "/" + fileName;
 		sshConnection.putFile(stream, remoteFolder);
 	}
 
 	private ISelfLetID getNewSelfletID() {
 		return new SelfLetID(++initialNumberOfSelflets);
 	}
 
 	private SSHConnection createNewSSHConnection(String ipAddress) {
 		return new SSHConnection(USERNAME, ipAddress, PORT_NUMBER, PASSWORD);
 	}
 
 	/**
 	 * Thread executing a reset
 	 * */
 	class ResetThread extends Thread {
 
 		private String ip;
 
 		public ResetThread(String ip) {
 			this.ip = ip;
 		}
 
 		@Override
 		public void run() {
 			LOG.info("Resetting ip address: " + ip);
 			resetMachine(ip);
 		}
 
 	}
 }
